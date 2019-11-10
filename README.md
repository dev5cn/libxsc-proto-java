# libxsc-proto-java

[XSC](http://www.dev5.cn/x_msg_im/start/xsc/)协议的java实现.

* example

```java
public static void main(String[] args) throws Exception
{
    XscProtoPdu pdu = new XscProtoPdu();
    pdu.transm.indicator = 0x00;
    pdu.transm.addOob(XscProto.XSC_TAG_INTERCEPT, "enable");
    pdu.transm.trans = new XscProtoTransaction();
    pdu.transm.trans.trans = XscProto.XSC_TAG_TRANS_BEGIN;
    pdu.transm.trans.stid = 0x00112233;
    pdu.transm.trans.msg = "XmsgImTestMsg";
    pdu.transm.trans.dat = new byte[] { 0x00, 0x11, 0x22, 0x33 };
    byte by[] = pdu.bytes();
    System.out.println(XscProtoPdu.decode(by, 0, by.length).print(by, 0, by.length));
}
```

* output

```js
01 28 C1 0A C6 08 1A 06 65 6E 61 62 6C 65 60 18 .(......enable`.
00 04 00 11 22 33 05 0A 58 6D 73 67 49 6D 54 65 ...."3..XmsgImTe
73 74 07 04 00 11 22 33                         st...."3
indicator: 01, len: 00000028
header: 
C1 0A C6 08 1A 06 65 6E 61 62 6C 65             ......enable
--00C1(HEADER)[0A]
    |
    |--00C6(OOB)[08]
        |
        |----1A(INTERCEPT)[06]={65 6E 61 62 6C 65}

transaction: 
60 18 00 04 00 11 22 33 05 0A 58 6D 73 67 49 6D `....."3..XmsgIm
54 65 73 74 07 04 00 11 22 33                   Test...."3
----60(BEGIN)[18]
    |
    |----00(STID)[04]={00 11 22 33}
    |----05(MSG)[0A]={XmsgImTest}
    |----07(DAT)[04]={00 11 22 33}
```
