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
import misc.Net;
import xsc.proto.XscProtoDec.XscProtoNode;
import xsc.proto.XscProtoDec.XscProtoTlvPdu;

public class XscProtoTransaction
{
	public static final int XSC_TRANS_PDU_RESERVED = 0x20;
	public static final int XSC_TRANS_PDU_MAX_MSG_NAME = 0x40;
	public static final int XSC_TRANS_PDU_MAX_DESC_LEN = 0x200;

	public byte trans = 0; 
	public byte partSeq = 0; 
	public boolean haveNextPart = false; 
	public boolean refDat = false; 
	public int stid = 0; 
	public int dtid = 0; 
	public int dlen = 0; 
	public byte[] dat = null; 
	public String msg = null; 
	public short ret = 0; 
	public String desc = null; 

	public boolean decode(byte[] dat, int ofst, int len)
	{
		XscProtoNode root = XscProtoDec.decode(dat, ofst, len);
		if (root == null)
		{
			if (Log.isDebug())
				Log.debug("error: \n%s", Net.printBytes(dat, ofst, len));
			return false;
		}
		this.trans = root.self.t;
		boolean ret = false;
		switch (root.self.t)
		{
		case XscProto.XSC_TAG_TRANS_BEGIN:
			ret = this.decodeBegin(root);
			break;
		case XscProto.XSC_TAG_TRANS_END:
			ret = this.decodeEnd(root);
			break;
		case XscProto.XSC_TAG_TRANS_UNIDIRECTION:
			ret = this.decodeUnidirection(root);
			break;
		case XscProto.XSC_TAG_TRANS_PARTIAL:
			ret = this.decodePartial(root);
			break;
		default:
			if (Log.isDebug())
				Log.debug("unexpected XSC_TAG_TRANS: %04X", root.self.t);
			break;
		}
		return ret;
	}

	private boolean decodeBegin(XscProtoNode root)
	{
		Integer stid = XscProtoDec.getInt(root, XscProto.XSC_TAG_STID);
		if (stid == null) 
		{
			if (Log.isDebug())
				Log.debug("missing required field: XSC_TAG_STID");
			return false;
		}
		String msg = XscProtoDec.getStr(root, XscProto.XSC_TAG_MSG);
		if (msg == null)
		{
			if (Log.isDebug())
				Log.debug("missing required field: XSC_TAG_MSG");
			return false;
		}
		if (msg.length() > XscProtoTransaction.XSC_TRANS_PDU_MAX_MSG_NAME)
		{
			if (Log.isDebug())
				Log.debug("over the XSC_TRANS_PDU_MAX_MSG_NAME: %d", XscProtoTransaction.XSC_TRANS_PDU_MAX_MSG_NAME);
			return false;
		}
		byte[] dat = XscProtoDec.getBin(root, XscProto.XSC_TAG_DAT); 
		Byte part = XscProtoDec.getByte(root, XscProto.XSC_TAG_HAVE_NEXT_PART);
		if (part != null && part == 0x01 )
		{
			Byte partSeq = XscProtoDec.getByte(root, XscProto.XSC_TAG_SEQ);
			if (partSeq == null)
			{
				if (Log.isDebug())
					Log.debug("missing required field: XSC_TAG_SEQ");
				return false;
			}
			this.haveNextPart = true;
			this.partSeq = partSeq;
			this.msg = new String(msg);
			this.dat = dat;
			return true;
		}
		this.msg = new String(msg);
		this.dat = dat;
		return true;
	}

	private boolean decodeEnd(XscProtoNode root)
	{
		Integer stid = XscProtoDec.getInt(root, XscProto.XSC_TAG_DTID);
		if (stid == null) 
		{
			if (Log.isDebug())
				Log.debug("missing required field: XSC_TAG_DTID");
			return false;
		}
		Short ret = XscProtoDec.getShort(root, XscProto.XSC_TAG_RET);
		this.ret = ret == null ? XscProto.RET_SUCCESS : ret; 
		String desc = XscProtoDec.getStr(root, XscProto.XSC_TAG_DESC);
		if (desc != null) 
		{
			if (desc.length() > XscProtoTransaction.XSC_TRANS_PDU_MAX_DESC_LEN)
			{
				if (Log.isDebug())
					Log.debug("error description too long, len: %d, desc: %s", desc.length(), desc);
				return false;
			}
			this.desc = desc;
		}
		byte[] dat = XscProtoDec.getBin(root, XscProto.XSC_TAG_DAT);
		String msg = XscProtoDec.getStr(root, XscProto.XSC_TAG_MSG);
		if (dat != null && msg == null) 
		{
			if (Log.isDebug())
				Log.debug("missing required field: XSC_TAG_MSG");
			return false;
		}
		if (msg != null && msg.length() > XscProtoTransaction.XSC_TRANS_PDU_MAX_MSG_NAME) 
		{
			if (Log.isDebug())
				Log.debug("message name too long, XSC_TRANS_PDU_MAX_MSG_NAME: %d, len: %d, msg: %s", XscProtoTransaction.XSC_TRANS_PDU_MAX_MSG_NAME, msg.length(), msg);
			return false;
		}
		Byte part = XscProtoDec.getByte(root, XscProto.XSC_TAG_HAVE_NEXT_PART);
		if (part != null && part == 0x01 )
		{
			if (Log.isDebug())
				Log.debug("it`s a bug, incomplete");
			return false;
		}
		this.msg = msg;
		this.dat = dat;
		return true;
	}

	private boolean decodeUnidirection(XscProtoNode root)
	{
		byte[] dat = XscProtoDec.getBin(root, XscProto.XSC_TAG_DAT);
		String msg = XscProtoDec.getStr(root, XscProto.XSC_TAG_MSG);
		if (dat != null && msg == null) 
		{
			if (Log.isDebug())
				Log.debug("missing required field: XSC_TAG_MSG");
			return false;
		}
		if (msg != null && msg.length() > XscProtoTransaction.XSC_TRANS_PDU_MAX_MSG_NAME) 
		{
			if (Log.isDebug())
				Log.debug("message name too long, XSC_TRANS_PDU_MAX_MSG_NAME: %d, len: %d, msg: %s", XscProtoTransaction.XSC_TRANS_PDU_MAX_MSG_NAME, msg.length(), msg);
			return false;
		}
		Byte part = XscProtoDec.getByte(root, XscProto.XSC_TAG_HAVE_NEXT_PART);
		if (part != null && part == 0x01 )
		{
			if (Log.isDebug())
				Log.debug("it`s a bug, incomplete");
			return false;
		}
		this.msg = msg;
		this.dat = dat;
		return true;
	}

	private boolean decodePartial(XscProtoNode root)
	{
		if (Log.isDebug())
			Log.debug("it`s a bug, incomplete");
		return false;
	}

	public static final void encodeBegin(XscProtoTransaction trans, XscProtoTlvPdu xtp)
	{
		XscProtoTransaction.encodeBegin(trans.stid, trans.msg, trans.dat, xtp);
	}

	public static final void encodeBegin(int stid, String msg, byte[] dat, XscProtoTlvPdu xtp)
	{
		if (dat != null)
			XscProtoEnc.addBin(xtp, XscProto.XSC_TAG_DAT, dat);
		XscProtoEnc.addStr(xtp, XscProto.XSC_TAG_MSG, msg);
		XscProtoEnc.addInt(xtp, XscProto.XSC_TAG_STID, stid);
		XscProtoEnc.addTag(xtp, XscProto.XSC_TAG_TRANS_BEGIN);
	}

	public static final void encodeEnd(XscProtoTransaction trans, XscProtoTlvPdu xtp)
	{
		XscProtoTransaction.encodeEnd(trans.dtid, trans.ret, trans.desc, trans.msg, trans.dat, xtp);
	}

	public static final void encodeEnd(int dtid, short ret, String desc, String msg, byte[] dat, XscProtoTlvPdu xtp)
	{
		if (dat != null)
			XscProtoEnc.addBin(xtp, XscProto.XSC_TAG_DAT, dat);
		if (msg != null)
			XscProtoEnc.addStr(xtp, XscProto.XSC_TAG_MSG, msg);
		if (desc != null)
			XscProtoEnc.addStr(xtp, XscProto.XSC_TAG_DESC, desc);
		if (ret != XscProto.RET_SUCCESS) 
			XscProtoEnc.addShort(xtp, XscProto.XSC_TAG_RET, ret);
		XscProtoEnc.addInt(xtp, XscProto.XSC_TAG_DTID, dtid);
		XscProtoEnc.addTag(xtp, XscProto.XSC_TAG_TRANS_END);
	}

	public static final void encodeUnidirection(XscProtoTransaction trans, XscProtoTlvPdu xtp)
	{
		XscProtoTransaction.encodeUnidirection(trans.msg, trans.dat, xtp);
	}

	public static final void encodeUnidirection(String msg, byte[] dat, XscProtoTlvPdu xtp)
	{
		if (dat != null)
			XscProtoEnc.addBin(xtp, XscProto.XSC_TAG_DAT, dat);
		XscProtoEnc.addStr(xtp, XscProto.XSC_TAG_MSG, msg);
		XscProtoEnc.addTag(xtp, XscProto.XSC_TAG_TRANS_UNIDIRECTION);
	}
}
