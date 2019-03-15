package org.jmqtt.group.remoting;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.jmqtt.common.helper.MixAll;
import org.jmqtt.common.helper.Pair;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.group.common.InvokeCallback;
import org.jmqtt.group.common.ResponseFuture;
import org.jmqtt.group.common.SemaphoreReleaseOnlyOnce;
import org.jmqtt.group.processor.ClusterRequestProcessor;
import org.jmqtt.group.protocol.ClusterRemotingCommand;
import org.jmqtt.group.protocol.ClusterRequestCode;
import org.jmqtt.group.protocol.MessageFlag;
import org.jmqtt.remoting.exception.RemotingSendRequestException;
import org.jmqtt.remoting.util.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public abstract class AbstractNettyCluster {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.CLUSTER);

    private final Map<Integer /* opaque */, ResponseFuture> responseTable = new ConcurrentHashMap<>();
    protected final Map<Integer /* code */, Pair<ClusterRequestProcessor, ExecutorService>> processorTable = new ConcurrentHashMap<>();
    /**
     * Semaphore to limit maximum number of on-going asynchronous requests, which protects system memory footprint.
     */
    private Semaphore semaphore;

    public AbstractNettyCluster() {
        this.semaphore = new Semaphore(65536, true);
    }

    public AbstractNettyCluster(int semaphore) {
        this.semaphore = new Semaphore(semaphore, true);
    }

    public void invokeAsyncImpl(final Channel channel, final ClusterRemotingCommand command, final long timeout, InvokeCallback invokeCallback) throws RemotingSendRequestException {
        final int opaque = command.getOpaque();
        try {
            SemaphoreReleaseOnlyOnce semaphoreReleaseOnlyOnce = new SemaphoreReleaseOnlyOnce(semaphore);
            ResponseFuture responseFuture = new ResponseFuture(channel, opaque, timeout, invokeCallback, semaphoreReleaseOnlyOnce);
            boolean tryAquired = semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS);
            if (tryAquired) {
                responseTable.put(opaque, responseFuture);
                final String remotingAddr = RemotingHelper.getRemoteAddr(channel);
                channel.writeAndFlush(command).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if (channelFuture.isSuccess()) {
                            responseFuture.setSendRequestOK(true);
                            return;
                        }
                        requestFail(opaque);
                        log.warn("send a request command to channel <{}> failed.", remotingAddr);
                    }
                });
            } else {
                // TODO 获取失败应该放入重试队列进行重试
                log.warn("Async invoke aquire semaphore failure,waiting threadNums:{},semaphoreAsyncValue:{}", semaphore.getQueueLength(), semaphore.availablePermits());
            }
        } catch (Exception ex) {
            log.info("send request failure", ex);
            throw new RemotingSendRequestException("send request failure");
        }
    }

    protected void processMessageReceived(ChannelHandlerContext ctx, ClusterRemotingCommand cmd) {
        if (cmd != null) {
            if (MessageFlag.COMPRESSED_FLAG == cmd.getFlag()) {
                byte[] body = cmd.getBody();
                try {
                    body = MixAll.uncompress(body);
                    cmd.setBody(body);
                } catch (IOException e) {
                    log.info("uncompress cluster message failure", e);
                }
            }
            switch (cmd.getType()) {
                case REQUEST_COMMAND:
                    processRequest(ctx, cmd);
                    break;
                case RESPONSE_COMMAND:
                    processResponse(ctx, cmd);
                    break;
                default:
                    break;
            }
        }
    }

    private void processRequest(ChannelHandlerContext ctx, ClusterRemotingCommand cmd) {
        final Pair<ClusterRequestProcessor, ExecutorService> pair = this.processorTable.get(cmd.getCode());
        final int opaque = cmd.getOpaque();
        if(pair != null){
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    final ClusterRemotingCommand responseCommand = pair.getObject1().processRequest(ctx,cmd);
                    if(responseCommand != null){
                        responseCommand.setOpaque(opaque);
                        responseCommand.makeResponseType();
                        ctx.writeAndFlush(responseCommand).addListener(new ChannelFutureListener() {
                            @Override
                            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                                if(!channelFuture.isSuccess()){
                                    log.warn("clister transfer message failure,addr={}",RemotingHelper.getRemoteAddr(ctx.channel()));
                                }
                            }
                        });
                    }
                }
            };
            pair.getObject2().submit(runnable);
        } else {
            log.error("cluster request has no processor,code={}",cmd.getCode());
            ClusterRemotingCommand responseCommand = new ClusterRemotingCommand(ClusterRequestCode.REQUEST_CODE_NOT_SUPPORTED);
            responseCommand.setOpaque(opaque);
            responseCommand.makeResponseType();
            ctx.writeAndFlush(responseCommand);
        }
    }

    private void processResponse(ChannelHandlerContext ctx, ClusterRemotingCommand cmd) {
        
    }

    private void requestFail(final int opaque) {
        // TODO 发送失败，从缓存移除该future并放入重试任务队列

    }

    protected void registerProcessor(int requestCode, ClusterRequestProcessor processor, ExecutorService service) {
        this.processorTable.put(requestCode, new Pair(processor, service));
    }


}
