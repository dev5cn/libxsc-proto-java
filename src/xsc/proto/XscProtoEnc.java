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

import misc.Net;
import xsc.proto.XscProtoDec.XscProtoNode;
import xsc.proto.XscProtoDec.XscProtoTlvPdu;

public class XscProtoEnc
{
	public static void addByte(XscProtoTlvPdu xp, byte t, byte v)
	{
		XscProtoEnc.addBin(xp, t, new byte[] { v }, 0, 1);
	}

	public static void addStr(XscProtoTlvPdu xp, byte t, String str)
	{
		byte by[] = str.getBytes();
		XscProtoEnc.addBin(xp, t, by, 0, by.length);
	}

	public static void addShort(XscProtoTlvPdu xp, byte t, short v)
	{
		XscProtoEnc.addBin(xp, t, Net.short2byte(v), 0, 2);
	}

	public static void addInt(XscProtoTlvPdu xp, byte t, int v)
	{
		XscProtoEnc.addBin(xp, t, Net.int2byte(v), 0, 4);
	}

	public static void addLong(XscProtoTlvPdu xp, byte t, long v)
	{
		XscProtoEnc.addBin(xp, t, Net.long2byte(v), 0, 8);
	}

	public static final void addBin(XscProtoTlvPdu xp, byte t, byte[] v)
	{
		XscProtoEnc.addBin(xp, t, v, 0, v.length);
	}

	public static final void addBin(XscProtoTlvPdu xp, byte t, byte[] v, int offset, int length)
	{
		int ll = XscProto.tlvLen(length);
		xp.rm -= (ll + 1 + length);
		xp.buff[xp.rm] = t;
		if (ll == 1)
			xp.buff[xp.rm + 1] = (byte) length;
		else if (ll == 3)
		{
			xp.buff[xp.rm + 1] = XscProtoNode.__XSC_LEN_0xFE__;
			System.arraycopy(Net.short2byte((short) length), 0, xp.buff, xp.rm + 2, 2);
		} else if (ll == 5)
		{
			xp.buff[xp.rm + 1] = XscProtoNode.__XSC_LEN_0xFF__;
			System.arraycopy(Net.int2byte(length), 0, xp.buff, xp.rm + 2, 4);
		}
		System.arraycopy(v, offset, xp.buff, xp.rm + 1 + ll, length);
	}

	public static final void setPoint(XscProtoTlvPdu xp)
	{
		xp.p = xp.rm;
	}

	public static final void addTag(XscProtoTlvPdu xp, byte t)
	{
		XscProtoEnc.__addTag__(xp, t, xp.buff.length - xp.rm);
	}

	public static final void addTag4Point(XscProtoTlvPdu xp, byte t)
	{
		XscProtoEnc.__addTag__(xp, t, xp.p - xp.rm);
	}

	public static final void reset(XscProtoTlvPdu xp)
	{
		xp.rm = xp.buff.length;
	}

	private static final void __addTag__(XscProtoTlvPdu xp, byte t, int len)
	{
		int ll = XscProto.tlvLen(len);
		xp.rm -= (ll + 1);
		xp.buff[xp.rm] = t;
		if (ll == 1)
			xp.buff[xp.rm + 1] = (byte) len;
		else if (ll == 3)
		{
			xp.buff[xp.rm + 1] = XscProtoNode.__XSC_LEN_0xFE__;
			System.arraycopy(Net.short2byte((short) len), 0, xp.buff, xp.rm + 2, 2);
		} else if (ll == 5)
		{
			xp.buff[xp.rm + 1] = XscProtoNode.__XSC_LEN_0xFF__;
			System.arraycopy(Net.int2byte(len), 0, xp.buff, xp.rm + 2, 4);
		}
	}
}
