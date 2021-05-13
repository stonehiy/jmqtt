### mqtt-im 修改协议

[基于MQTT协议的移动IM系统设计与实现](https://www.sohu.com/a/113926583_354885)

[基于MQTT协议的移动IM系统设计与实现](https://www.ixueshu.com/document/c7c50ff0aa56f70b9bf860c35bd87ec3318947a18e7f9386.html)

#### mqtt pulish 控制报文

见：qmtt-im.png

5个字段含义如下：
`flag`:起始标志，1个字节，不可为空 从0(0x00)到127(0x7f) 这里用的0x7f，不是以0x7f开头的数据包将被抛弃 

`type`:消息类型，1个字节，不可为空 从0(0x00)到127(0x7f) 共可以表示127种类型

`from`:发送方字段，变长，可为空，标识发送方的id,唯一标识

`to`:接收方字段，变长，可为空，标识接收方id,唯一标识

`timestamp`:时间戳字段，8个字节，不可为空，可用于消息排序

`content`:消息正文字段，变长，可为空，针对不同`type`值,`content`结构不同.


#### 主题订阅的实现

`im/f/<uid>`:通讯录类主题，这类的主题接收到通讯录内好友的即时消息。

`im/g/<gid>`:群组类消息，这类的主题可以接收群组内的消息

`im/s/<uid>`:状态类主题，这类主题接收状态改变的消息

#### 即时通讯的实现


```$xslt
/**
 * Convert ArrayBuffer/TypedArray to String via TextDecoder
 *
 * @see https://developer.mozilla.org/en-US/docs/Web/API/TextDecoder
 */
function ab2str(
  input: ArrayBuffer | Uint8Array | Int8Array | Uint16Array | Int16Array | Uint32Array | Int32Array,
  outputEncoding: string = 'utf8',
): string {
  const decoder = new TextDecoder(outputEncoding)
  return decoder.decode(input)
}

/**
 * Convert String to ArrayBuffer via TextEncoder
 *
 * @see https://developer.mozilla.org/zh-CN/docs/Web/API/TextEncoder
 */
function str2ab(input: string): ArrayBuffer {
  const view = str2Uint8Array(input)
  return view.buffer
}

/** Convert String to Uint8Array */
function str2Uint8Array(input: string): Uint8Array {
  const encoder = new TextEncoder()
  const view = encoder.encode(input)
  return view
}
```




 
