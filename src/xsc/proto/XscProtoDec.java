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

public class XscProtoDec
{
	public static final XscProtoNode decode(byte dat[])
	{
		XscProtoNode node = new XscProtoNode();
		return XscProtoDec.__decode__(dat, 0, dat.length, node) == 0 ? node : null;
	}

	public static final XscProtoNode decode(byte dat[], int ofst, int len)
	{
		XscProtoNode node = new XscProtoNode();
		return XscProtoDec.__decode__(dat, ofst, len, node) == 0 ? node : null;
	}

	public static final byte[] getBin(XscProtoNode node, byte t)
	{
		XscProtoNode x = XscProtoDec.search(node, t);
		if (x == null)
			return null;
		if (x.self.l == 0)
			return null;
		return x.self.v;
	}

	public static final Byte getByte(XscProtoNode node, byte t)
	{
		XscProtoNode x = XscProtoDec.search(node, t);
		if (x == null)
			return null;
		if (x.self.l != 1)
			return null;
		return x.self.v[0];
	}

	public static final String getStr(XscProtoNode node, byte t, int max)
	{
		XscProtoNode x = XscProtoDec.search(node, t);
		if (x == null)
			return null;
		if (x.self.l < 1)
			return null;
		return new String(x.self.v, 0, x.self.l > max ? max : x.self.l);
	}

	public static final String getStr(XscProtoNode node, byte t)
	{
		byte by[] = XscProtoDec.getBin(node, t);
		if (by == null)
			return null;
		return new String(by);
	}

	public static final Short getShort(XscProtoNode node, byte t)
	{
		XscProtoNode x = XscProtoDec.search(node, t);
		if (x == null)
			return null;
		if (x.self.l != 2)
			return null;
		return Net.byte2short(x.self.v, 0);
	}

	public static final Integer getInt(XscProtoNode node, byte t)
	{
		XscProtoNode x = XscProtoDec.search(node, t);
		if (x == null)
			return null;
		if (x.self.l != 4)
			return null;
		return Net.byte2int(x.self.v, 0);
	}

	public static final Long getLong(XscProtoNode node, byte t)
	{
		XscProtoNode x = XscProtoDec.search(node, t);
		if (x == null)
			return null;
		if (x.self.l != 0x08)
			return null;
		return Net.byte2long(x.self.v, 0);
	}

	public static final XscProtoNode getNode(XscProtoNode node, byte t)
	{
		return XscProtoDec.search(node, t);
	}

	public static final XscProtoNode getFirstChild(XscProtoNode node)
	{
		return node.c;
	}

	public static final XscProtoNode getNextSibling(XscProtoNode node)
	{
		return node.s;
	}

	public static final int[] genTl(byte dat[], int offset, int size)
	{
		if (size < 2)
		{
			if (Log.isDebug())
				Log.debug("no enough size for decode tag-field and length-field, dat: %s", Misc.printBytes(dat, offset, size));
			return null;
		}
		int x[] = new int[2];
		x[0] = (dat[offset + 1] == XscProtoNode.__XSC_LEN_0xFE__ ? 3 : (dat[offset + 1] == XscProtoNode.__XSC_LEN_0xFF__ ? 5 : 1));
		if (size < 1 + x[0])
		{
			if (Log.isDebug())
				Log.debug("no enough size for decode length-field, size: %08X, tag: %02X, ll: %02X, offset: %d", size, dat[offset], (byte) x[0], offset);
			return null;
		}
		if (x[0] == 1) 
		{
			x[1] = (dat[offset + 1] & 0x000000FF);
			if (size < 1 + x[0] + x[1])
			{
				if (Log.isDebug())
					Log.debug("no enough size for decode length-field, size: %08X, tag: %02X, ll: %02X, l: %02X, offset: %d", size, dat[offset], x[0], x[1], offset);
				return null;
			}
			return x;
		}
		if (x[0] == 3) 
		{
			x[1] = (Net.byte2short(dat, offset + 2) & 0x0000FFFF);
			if (size < 1 + x[0] + x[1])
			{
				if (Log.isDebug())
					Log.debug("no enough size for decode length-field, size: %08X, tag: %02X, ll: %02X, l: %02X, offset: %d", size, dat[offset], x[0], x[1], offset);
				return null;
			}
			return x;
		}
		if (x[0] == 5) 
		{
			x[1] = (Net.byte2int(dat, offset + 2) & 0x7FFFFFFF);
			if (size < 1 + x[0] + x[1])
			{
				if (Log.isDebug())
					Log.debug("no enough size for decode length-field, size: %08X, tag: %02X, ll: %02X, l: %02X, offset: %d", size, dat[0], x[0], x[1], offset);
				return null;
			}
			return x;
		}
		return null;
	}

	public static final String printNode2Str(XscProtoNode node)
	{
		StringBuilder strb = new StringBuilder();
		XscProtoDec.__print__(node, strb, 0);
		return strb.toString();
	}

	public static final void printNode(XscProtoNode node)
	{
		System.out.println(XscProtoDec.printNode2Str(node));
	}

	public static final void printf(byte by[])
	{
		System.out.println(XscProtoDec.print2Str(by));
	}

	public static final void printPdu(XscProtoTlvPdu pdu)
	{
		System.out.println(XscProtoDec.printPdu2Str(pdu));
	}

	public static final String print2Str(byte by[])
	{
		return XscProtoDec.print2Str(by, 0, by.length);
	}

	public static final String print2Str(byte by[], int ofst, int len)
	{
		StringBuilder strb = new StringBuilder(Misc.printBytes(by, ofst, len)).append("\n");
		XscProtoNode root = XscProtoDec.decode(by, ofst, len);
		if (root != null)
		{
			strb.append(XscProtoDec.printNode2Str(root));
			return strb.toString();
		}
		if (Log.isDebug())
			Log.debug("can not decode this xsc pdu, stack: %s", Misc.getStackInfo());
		return strb.toString();
	}

	public static final String printPdu2Str(XscProtoTlvPdu pdu)
	{
		byte by[] = new byte[pdu.buff.length - pdu.rm];
		System.arraycopy(pdu.buff, pdu.rm, by, 0, by.length);
		return XscProtoDec.print2Str(by);
	}

	private static final void __print__(XscProtoNode node, StringBuilder strb, int space)
	{
		for (int i = 0; i < space; ++i)
			strb.append(Misc.printf2str("%s", i == space - 1 ? " |" : " "));
		String tstr = XscProto.tagDesc(node.self.t);
		int ll = XscProto.tlvLen(node.self.l);
		if ((node.self.t & 0xFF00) != 0 ? (node.self.t & 0x4000) != 0 : (node.self.t & 0x40) != 0) 
		{
			space += 4;
			if (ll == 1)
				strb.append(Misc.printf2str((node.self.t & 0xFF00) != 0 ? ("--%04X(%s)[%02X]\n") : ("----%02X(%s)[%02X]\n"), node.self.t, tstr, node.self.l));
			else if (ll == 3)
				strb.append(Misc.printf2str((node.self.t & 0xFF00) != 0 ? ("--%04X(%s)[%04X]\n") : ("----%02X(%s)[%04X]\n"), node.self.t, tstr, node.self.l));
			else
				strb.append(Misc.printf2str((node.self.t & 0xFF00) != 0 ? ("--%04X(%s)[%08X]\n") : ("----%02X(%s)[%08X]\n"), node.self.t, tstr, node.self.l));
			for (int i = 0; i < space; ++i)
				strb.append(Misc.printf2str("%s", i == space - 1 ? " |\n" : " "));
		} else
		{
			if (ll == 1)
				strb.append(Misc.printf2str((node.self.t & 0xFF00) != 0 ? ("--%04X(%s)[%02X]={") : ("----%02X(%s)[%02X]={"), node.self.t, tstr, node.self.l));
			else if (ll == 3)
				strb.append(Misc.printf2str((node.self.t & 0xFF00) != 0 ? ("--%04X(%s)[%04X]={") : ("----%02X(%s)[%04X]={"), node.self.t, tstr, node.self.l));
			else
				strb.append(Misc.printf2str((node.self.t & 0xFF00) != 0 ? ("--%04X(%s)[%08X]={") : ("----%02X(%s)[%08X]={"), node.self.t, tstr, node.self.l));
			strb.append(Misc.printf2str("%s}\n", XscProto.v2str(node.self)));
			space += 4;
		}
		if (node.c != null)
			XscProtoDec.__print__(node.c, strb, space);
		if (node.s != null)
			XscProtoDec.__print__(node.s, strb, space - 4);
	}

	private static final XscProtoNode search(XscProtoNode node, byte t)
	{
		return node.c == null ? null : XscProtoDec.search_s(node.c, t);
	}

	private static final XscProtoNode search_s(XscProtoNode node, byte t)
	{
		if (node.self.t == t)
			return node;
		return node.s == null ? null : XscProtoDec.search_s(node.s, t);
	}

	private static final int __decode__(byte dat[], int ofst, int size, XscProtoNode node)
	{
		if (size == 1)
		{
			node.self.t = dat[ofst];
			node.self.l = 0;
			node.self.v = null;
			return 0;
		}
		int ret = 1;
		int x[] = XscProtoDec.genTl(dat, ofst, size);
		if (x == null)
			return ret;
		node.self.t = dat[ofst];
		node.self.l = x[1];
		if ((node.self.t & 0x40) != 0) 
		{
			node.c = new XscProtoNode();
			if (XscProtoDec.__decode__(dat, ofst + 1 + x[0], node.self.l, node.c) != 0)
				return ret;
		} else
		{
			node.self.v = new byte[node.self.l];
			System.arraycopy(dat, ofst + 1 + x[0], node.self.v, 0, node.self.v.length);
		}
		int cur = (1 + x[0] + node.self.l);
		int rem = size - cur;
		if (rem != 0)
		{
			node.s = new XscProtoNode();
			return XscProtoDec.__decode__(dat, ofst + cur, rem, node.s);
		}
		return 0;
	}

	public static final class XscProtoNode
	{
		public static final byte __XSC_LEN_0xFE__ = (byte) 0xFE;
		public static final byte __XSC_LEN_0xFF__ = (byte) 0xFF;
		public static final String __IEI_UNKNOWN__ = "IEI_UNKNOWN";

		public XscProtoTlv self = new XscProtoTlv();
		public XscProtoNode s = null;
		public XscProtoNode c = null;
	}

	public static final class XscProtoTlvPdu
	{
		public int rm = 0;
		public int p = 0;
		public byte buff[] = null;
	}
}
