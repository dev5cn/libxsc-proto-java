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

import java.util.LinkedList;

import misc.Log;
import misc.Net;
import misc.Pair;
import xsc.proto.XscProtoDec.XscProtoNode;
import xsc.proto.XscProtoDec.XscProtoTlvPdu;

public class XscProtoTransmission
{
	public static final int XSC_TRANSM_MTU = 0x10000; 
	public byte indicator; 
	public int len; 
	public XscProtoTransmissionHeader header = null;
	public XscProtoTransaction trans = null;

	public final boolean decode(byte[] dat, int ofst, int len, int lenlen )
	{
		if ((this.indicator & 0x01) != 0 && !this.decodeHeader(dat, ofst, len, lenlen) )
			return false;
		int remain = this.header == null ? (len - 1 - lenlen) : (len - 1 - lenlen - this.header.len);
		if (remain == 0)
			return true;
		if (remain < 0)
		{
			Log.fault("it`s a bug, dat: %s", Net.byte2hexStrSpace(dat, ofst, len));
			return false;
		}
		this.trans = new XscProtoTransaction();
		return this.trans.decode(dat, ofst + (len - remain), remain);
	}

	public final void addOob(byte tag, String val)
	{
		if (this.header == null)
			this.header = new XscProtoTransmissionHeader();
		if (this.header.oob == null)
			this.header.oob = new XscProtoTransmissionHeaderOob();
		this.header.oob.kv.add(new Pair<>(tag, val));
	}

	public String getOob(byte tag)
	{
		if (this.header == null || this.header.oob == null)
			return null;
		for (Pair<Byte, String> p : this.header.oob.kv)
		{
			if (p.f != tag)
				continue;
			return p.s;
		}
		return null;
	}

	public final boolean haveOob(byte tag)
	{
		if (this.header == null || this.header.oob == null)
			return false;
		for (Pair<Byte, String> p : this.header.oob.kv)
		{
			if (p.f != tag)
				return true;
		}
		return false;
	}

	public static final void encodeHeader(XscProtoTransmissionHeader header, XscProtoTlvPdu xtp)
	{
		boolean filled = false;
		if (header.oob != null)
		{
			for (Pair<Byte, String> p : header.oob.kv)
				XscProtoEnc.addStr(xtp, p.f, p.s);
			XscProtoEnc.addTag(xtp, XscProto.XSC_TAG_TRANSM_OOB);
			filled = true;
		}
		if (header.security != null)
		{
			if (filled)
				XscProtoEnc.setPoint(xtp);
			XscProtoEnc.addInt(xtp, XscProto.XSC_TAG_CHECKSUM, header.security.checkSumVal);
			XscProtoEnc.addByte(xtp, XscProto.XSC_TAG_ALG, header.security.checkSumAlg);
			if (filled)
				XscProtoEnc.addTag4Point(xtp, XscProto.XSC_TAG_TRANSM_SECURITY);
			else
				XscProtoEnc.addTag(xtp, XscProto.XSC_TAG_TRANSM_SECURITY);
			filled = true;
		}
		XscProtoEnc.addTag(xtp, XscProto.XSC_TAG_TRANSM_HEADER);
	}

	private final boolean decodeHeader(byte[] dat, int ofst, int len, int lenlen )
	{
		int arr[] = XscProtoDec.genTl(dat, lenlen + 1, len - 1 - lenlen);
		if (arr == null)
		{
			if (Log.isDebug())
				Log.debug("header format error, dat: \n%s", Net.printBytes(dat, ofst, len));
			return false;
		}
		XscProtoNode root = XscProtoDec.decode(dat, lenlen + 1, 1 + arr[0] + arr[1]);
		if (root == null)
		{
			if (Log.isDebug())
				Log.debug("header format error, dat: \n%s", Net.printBytes(dat, lenlen + 1, 1 + arr[0] + arr[1]));
			return false;
		}
		this.header = new XscProtoTransmissionHeader();
		this.header.len = 1 + arr[0] + arr[1];
		this.decodeHeaderOpt(root);
		return true;
	}

	private final void decodeHeaderOpt(XscProtoNode root)
	{
		XscProtoNode sec = XscProtoDec.getNode(root, XscProto.XSC_TAG_TRANSM_SECURITY);
		if (sec != null)
		{
			this.header.security = new XscProtoTransmissionHeaderSecurity();
			Byte b = XscProtoDec.getByte(sec, XscProto.XSC_TAG_ALG);
			if (b != null)
				this.header.security.checkSumAlg = b;
			Integer i = XscProtoDec.getInt(sec, XscProto.XSC_TAG_CHECKSUM);
			if (i != null)
				this.header.security.checkSumVal = i;
		}
		XscProtoNode oob = XscProtoDec.getNode(root, XscProto.XSC_TAG_TRANSM_OOB);
		if (oob != null)
		{
			this.header.oob = new XscProtoTransmissionHeaderOob();
			XscProtoNode child = XscProtoDec.getFirstChild(oob);
			if (child != null && child.self.l > 0)
			{
				String v = new String(child.self.v);
				this.header.oob.kv.push(new Pair<>(child.self.t, v));
			}
			XscProtoNode sibling = XscProtoDec.getNextSibling(child);
			while (sibling != null)
			{
				if (sibling.self.l < 1)
					continue;
				String v = new String(sibling.self.v);
				this.header.oob.kv.push(new Pair<>(sibling.self.t, v));
				child = sibling;
			}
		}
	}

	public static final int calLength(int len )
	{
		if (len < 0xFF)
			return 1;
		if (len < 0xFFFE)
			return 2;
		if (len < 0xFFFFFD)
			return 3;
		return 4;
	}

	public static final class XscProtoTransmissionHeader
	{
		public int len; 
		public XscProtoTransmissionHeaderSecurity security;
		public XscProtoTransmissionHeaderOob oob;
	}

	public static final class XscProtoTransmissionHeaderSecurity
	{
		public static final byte XSC_TRANSM_SECURITY_ALG_CRC32 = 0x01; 
		public byte checkSumAlg; 
		public int checkSumVal; 
	}

	public static final class XscProtoTransmissionHeaderOob
	{
		public LinkedList<Pair<Byte, String>> kv = new LinkedList<>();
	}
}
