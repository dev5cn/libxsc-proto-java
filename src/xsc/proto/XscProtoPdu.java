/*
  Copyright 2019 www.dev5.cn, Inc. dev5@qq.com
 
  This file is part of X-MSG-IM.
 
  X-MSG-IM is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  X-MSG-IM is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU Affero General Public License
  along with X-MSG-IM.  If not, see <https://www.gnu.org/licenses/>.
 */
package xsc.proto;

import misc.Log;
import misc.Misc;
import misc.Net;
import xsc.proto.XscProtoDec.XscProtoTlvPdu;

public class XscProtoPdu
{
	public XscProtoTransmission transm = new XscProtoTransmission(); 

	public static final XscProtoPdu decode(byte[] dat, int ofst, int len) throws Exception
	{
		if ((dat[ofst] & 0x80) != 0) 
		{
			if (dat[ofst] != 0x80 && dat[ofst] != 0xC0)
			{
				if (Log.isDebug())
					Log.debug("should be ping/pong pdu, dat: %s", Net.printBytes(dat));
				throw new Exception("should be ping/pong pdu");
			}
			XscProtoPdu pdu = new XscProtoPdu();
			pdu.transm.indicator = dat[ofst];
			pdu.transm.len = 1;
			return pdu;
		}
		if (len < 1  + 1  + 3 ) 
			return null;
		int length = (dat[ofst] >> 1) & 0x03; 
		int pduLen = 0; 
		if (length == 0x00) 
		{
			pduLen = Net.byte2int(dat[ofst + 1]);
			if (pduLen < 1  + 1  + 3 ) 
			{
				if (Log.isDebug())
					Log.debug("xsc pdu length must be >= 5 bytes");
				throw new Exception("xsc pdu length must be >= 5 bytes");
			}
			if (pduLen > len) 
				return null;
			return XscProtoPdu.decode(dat, ofst, 1, pduLen);
		}
		if (length == 0x01) 
		{
			pduLen = Net.short2int(Net.byte2short(dat, ofst + 1));
			if (pduLen < 1  + 2  + 3) 
			{
				if (Log.isDebug())
					Log.debug("xsc pdu length must be >= 6 bytes");
				throw new Exception("xsc pdu length must be >= 6 bytes");
			}
			if (pduLen > len) 
				return null;
			return XscProtoPdu.decode(dat, ofst, 2, pduLen);
		}
		if (Log.isDebug())
			Log.debug("unsupported over 2 bytes length field");
		throw new Exception("unsupported over 2 bytes length field");
	}

	public final byte[] bytes()
	{
		if ((this.transm.indicator & XscProto.XSC_TAG_TRANSM_PING) != 0)
			return new byte[] { this.transm.indicator };
		XscProtoTlvPdu xtp = new XscProtoTlvPdu();
		xtp.rm = XscProtoTransmission.XSC_TRANSM_MTU - 5;
		xtp.p = 0;
		xtp.buff = new byte[xtp.rm];
		if (this.transm.trans.trans == XscProto.XSC_TAG_TRANS_BEGIN)
			XscProtoTransaction.encodeBegin(this.transm.trans, xtp);
		else if (this.transm.trans.trans == XscProto.XSC_TAG_TRANS_END)
			XscProtoTransaction.encodeEnd(this.transm.trans, xtp);
		else if (this.transm.trans.trans == XscProto.XSC_TAG_TRANS_UNIDIRECTION)
			XscProtoTransaction.encodeUnidirection(this.transm.trans, xtp);
		else
		{
			Log.fault("it`s a bug, incomplete");
			return null;
		}
		int sizet = xtp.buff.length - xtp.rm; 
		int sizeh = 0;
		XscProtoTlvPdu xtph = null;
		if (this.transm.header != null)
		{
			xtph = new XscProtoTlvPdu();
			xtph.rm = xtp.buff.length - sizet;
			xtph.p = 0;
			xtph.buff = new byte[xtph.rm];
			XscProtoTransmission.encodeHeader(this.transm.header, xtph);
			sizeh = xtph.buff.length - xtph.rm;
		}
		int lenlen = XscProtoTransmission.calLength(1  + sizeh + sizet);
		int size = 1  + lenlen  + sizeh  + sizet ;
		byte[] reserved = null;
		if (lenlen == 1)
		{
			reserved = new byte[2];
			reserved[0] = (byte) (this.transm.header == null ? 0x00  : 0x01 );
			reserved[1] = (byte) size;
		} else if (lenlen == 2)
		{
			reserved = new byte[3];
			reserved[0] = (byte) (this.transm.header == null ? 0x02  : 0x03 );
			byte[] x = Net.short2byte((short) size);
			System.arraycopy(x, 0, reserved, 1, 2);
		} else if (lenlen == 3)
		{
			reserved = new byte[4];
			reserved[0] = (byte) (transm.header == null ? 0x04  : 0x05 );
			byte[] x = Net.int2byte(size);
			System.arraycopy(x, 1, reserved, 1, 3);
		} else if (lenlen == 4)
		{
			reserved = new byte[5];
			reserved[0] = (byte) (transm.header == null ? 0x06  : 0x07 );
			byte[] x = Net.int2byte(size);
			System.arraycopy(x, 0, reserved, 1, 4);
		} else
		{
			Log.fault("it`s a bug, lenlen: %d", lenlen);
		}
		byte by[] = new byte[size];
		System.arraycopy(reserved, 0, by, 0, reserved.length);
		System.arraycopy(xtph.buff, xtph.rm, by, reserved.length, sizeh);
		System.arraycopy(xtp.buff, xtp.rm, by, reserved.length + sizeh, sizet);
		return by;
	}

	public final void takeoffHeader(boolean oob )
	{
		if (this.transm.header == null)
			return;
		if (this.transm.header.oob != null && oob)
			this.transm.header.oob = null;
		if (this.transm.header.security != null || this.transm.header.oob != null)
			return;
		this.transm.header = null;
	}

	public final String print(byte[] dat, int ofst, int len)
	{
		int length = (dat[ofst] >> 1) & 0x03; 
		int lenlen = 0; 
		if ((dat[ofst] & 0x80) == 0) 
		{
			if (length == 0x00)
				lenlen = 1;
			else if (length == 0x01)
				lenlen = 2;
			else if (length == 0x02)
				lenlen = 3;
			else if (length == 0x03)
				lenlen = 4;
		}
		int headerLen = this.transm.header == null ? 0 : this.transm.header.len;
		StringBuilder strb = new StringBuilder();
		strb.append(Net.printBytes(dat, ofst, len));
		strb.append(Misc.printf2str("\nindicator: %02X, len: %08X\n", this.transm.indicator, this.transm.len));
		if (this.transm.header != null)
		{
			strb.append(Misc.printf2str("header: \n%s\n", XscProtoDec.print2Str(dat, ofst + 1 + lenlen, headerLen)));
		} else
		{
			strb.append(Misc.printf2str("header: NULL\n"));
		}
		if (this.transm.trans != null)
		{
			strb.append(Misc.printf2str("transaction: \n%s\n", XscProtoDec.print2Str(dat, ofst + 1 + lenlen + headerLen, len - 1 - lenlen - headerLen)));
		} else
		{
			strb.append(Misc.printf2str("transaction: NULL\n"));
		}
		return strb.toString();
	}

	private static final XscProtoPdu decode(byte[] dat, int ofst, int lenlen, int pduLen) throws Exception
	{
		XscProtoPdu pdu = new XscProtoPdu();
		pdu.transm.indicator = dat[ofst];
		pdu.transm.len = pduLen;
		if (pdu.transm.decode(dat, ofst, pdu.transm.len, lenlen ))
			return pdu;
		throw new Exception("xsc transmission decode failed");
	}
}
