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

import java.util.Date;
import java.util.HashMap;

import misc.DateMisc;
import misc.Net;
import xsc.proto.XscProtoDec.XscProtoNode;

public class XscProto
{
	public static final byte XSC_TAG_TRANSM_PING = (byte) 0x80; 
	public static final byte XSC_TAG_TRANSM_PONG = (byte) 0xC0; 
	public static final byte XSC_TAG_TRANSM_HEADER = (byte) 0xC1; 
	public static final byte XSC_TAG_TRANSM_SECURITY = (byte) 0xC4; 
	public static final byte XSC_TAG_TRANSM_OOB = (byte) 0xC6; 
	public static final byte XSC_TAG_TRANS_BEGIN = 0x60; 
	public static final byte XSC_TAG_TRANS_END = 0x61; 
	public static final byte XSC_TAG_TRANS_UNIDIRECTION = 0x66; 
	public static final byte XSC_TAG_TRANS_PARTIAL = 0x67; 
	public static final byte XSC_TAG_STID = 0x00; 
	public static final byte XSC_TAG_DTID = 0x01; 
	public static final byte XSC_TAG_TID = 0x02; 
	public static final byte XSC_TAG_UID = 0x03; 
	public static final byte XSC_TAG_SID = 0x04; 
	public static final byte XSC_TAG_MSG = 0x05; 
	public static final byte XSC_TAG_RET = 0x06; 
	public static final byte XSC_TAG_DAT = 0x07; 
	public static final byte XSC_TAG_SNE = 0x08; 
	public static final byte XSC_TAG_MNE = 0x09; 
	public static final byte XSC_TAG_DNE = 0x0A; 
	public static final byte XSC_TAG_HAVE_NEXT_PART = 0x0B; 
	public static final byte XSC_TAG_SEQ = 0x0C; 
	public static final byte XSC_TAG_ACTION = 0x0D; 
	public static final byte XSC_TAG_DESC = 0x0E; 
	public static final byte XSC_TAG_QOS = 0x0F; 
	public static final byte XSC_TAG_ALG = 0x10; 
	public static final byte XSC_TAG_CHECKSUM = 0x11; 
	public static final byte XSC_TAG_TRACE_ID = 0x12; 
	public static final byte XSC_TAG_SPAN_ID = 0x13; 
	public static final byte XSC_TAG_P_SPAN_ID = 0x14; 
	public static final byte XSC_TAG_TS = 0x15; 
	public static final byte XSC_TAG_GTS = 0x16; 
	public static final byte XSC_TAG_UTS = 0x17; 
	public static final byte XSC_TAG_PLATFORM = 0x18; 
	public static final byte XSC_TAG_CGT = 0x19; 
	public static final byte XSC_TAG_INTERCEPT = 0x1A; 
	public static final byte XSC_TAG_CLIENT_OOB = 0x1B; 
	public static final byte XSC_TAG_DEVICE_ID = 0x1C; 
	public static final byte XSC_TAG_NE_GROUP = 0x1D; 
	public static final short RET_SUCCESS = 0x0000; 
	public static final short RET_FAILURE = 0x0001; 
	public static final short RET_INVALID = 0x0002; 
	public static final short RET_PRESENT = 0x0003; 
	public static final short RET_NOT_PRESENT = 0x0004; 
	public static final short RET_EXCEPTION = 0x0005; 
	public static final short RET_NOT_FOUND = 0x0006; 
	public static final short RET_TIME_OUT = 0x0007; 
	public static final short RET_FORBIDDEN = 0x0008; 
	public static final short RET_FORMAT_ERROR = 0x0009; 
	public static final short RET_MISSING_PARAMETER = 0x000A; 
	public static final short RET_UNSUPPORTED = 0x000B; 
	public static final short RET_NO_AUTH = 0x000C; 
	public static final short RET_NO_PERMISSION = 0x000D; 
	public static final short RET_NO_RECORD = 0x000E; 
	public static final short RET_OVER_LIMIT = 0x000F; 
	public static final short RET_DUPLICATE_OPER = 0x0010; 
	public static final short RET_UPDATE = 0x0011; 
	public static final short RET_NOT_UPDATE = 0x0012; 
	public static final short RET_ROUTE_FAILED = 0x0013; 
	public static final short RET_CONNECTION_DISC = 0x0014; 
	public static final short RET_USR_OR_PASSWORD_ERROR = 0x0015; 
	public static final short RET_USR_DEFINED = 0x0100; 

	private static final String __xsc_ret__[] = { 
			 "SUCCESS", 
			 "FAILURE", 
			 "INVALID", 
			 "PRESENT", 
			 "NOT_PRESENT", 
			 "EXCEPTION", 
			 "NOT_FOUND", 
			 "TIME_OUT", 
			 "FORBIDDEN", 
			 "FORMAT_ERROR", 
			 "MISSING_PARAMETER", 
			 "UNSUPPORTED", 
			 "NO_AUTH", 
			 "NO_PERMISSION", 
			 "NO_RECORD", 
			 "OVER_LIMIT", 
			 "DUPLICATE_OPER", 
			 "UPDATE", 
			 "NOT_UPDATE", 
			 "ROUTE_FAILED", 
			 "CONNECTION_DISC", 
			 "USR_OR_PASSWORD_ERROR",
	};

	private static final HashMap<Short, String> tags = new HashMap<Short, String>();

	static
	{
		XscProto.tags.put((short) XscProto.XSC_TAG_TRANSM_PING, "PING");
		XscProto.tags.put((short) XscProto.XSC_TAG_TRANSM_PONG, "PONG");
		XscProto.tags.put((short) XscProto.XSC_TAG_TRANSM_HEADER, "HEADER");
		XscProto.tags.put((short) XscProto.XSC_TAG_TRANSM_SECURITY, "SECURITY");
		XscProto.tags.put((short) XscProto.XSC_TAG_TRANSM_OOB, "OOB");
		XscProto.tags.put((short) XscProto.XSC_TAG_TRANS_BEGIN, "BEGIN");
		XscProto.tags.put((short) XscProto.XSC_TAG_TRANS_END, "END");
		XscProto.tags.put((short) XscProto.XSC_TAG_TRANS_UNIDIRECTION, "UNIDIRECTION");
		XscProto.tags.put((short) XscProto.XSC_TAG_TRANS_PARTIAL, "PARTIAL");
		XscProto.tags.put((short) XscProto.XSC_TAG_STID, "STID");
		XscProto.tags.put((short) XscProto.XSC_TAG_DTID, "DTID");
		XscProto.tags.put((short) XscProto.XSC_TAG_TID, "TID");
		XscProto.tags.put((short) XscProto.XSC_TAG_UID, "UID");
		XscProto.tags.put((short) XscProto.XSC_TAG_SID, "SID");
		XscProto.tags.put((short) XscProto.XSC_TAG_MSG, "MSG");
		XscProto.tags.put((short) XscProto.XSC_TAG_RET, "RET");
		XscProto.tags.put((short) XscProto.XSC_TAG_DAT, "DAT");
		XscProto.tags.put((short) XscProto.XSC_TAG_SNE, "SNE");
		XscProto.tags.put((short) XscProto.XSC_TAG_MNE, "MNE");
		XscProto.tags.put((short) XscProto.XSC_TAG_DNE, "DNE");
		XscProto.tags.put((short) XscProto.XSC_TAG_HAVE_NEXT_PART, "HAVE_NEXT_PART");
		XscProto.tags.put((short) XscProto.XSC_TAG_SEQ, "SEQ");
		XscProto.tags.put((short) XscProto.XSC_TAG_ACTION, "ACTION");
		XscProto.tags.put((short) XscProto.XSC_TAG_DESC, "DESC");
		XscProto.tags.put((short) XscProto.XSC_TAG_QOS, "QOS");
		XscProto.tags.put((short) XscProto.XSC_TAG_ALG, "ALG");
		XscProto.tags.put((short) XscProto.XSC_TAG_CHECKSUM, "CHECKSUM");
		XscProto.tags.put((short) XscProto.XSC_TAG_TRACE_ID, "TRACE_ID");
		XscProto.tags.put((short) XscProto.XSC_TAG_SPAN_ID, "SPAN_ID");
		XscProto.tags.put((short) XscProto.XSC_TAG_P_SPAN_ID, "P_SPAN_ID");
		XscProto.tags.put((short) XscProto.XSC_TAG_TS, "TS");
		XscProto.tags.put((short) XscProto.XSC_TAG_GTS, "GTS");
		XscProto.tags.put((short) XscProto.XSC_TAG_UTS, "UTS");
		XscProto.tags.put((short) XscProto.XSC_TAG_PLATFORM, "PLATFORM");
		XscProto.tags.put((short) XscProto.XSC_TAG_CGT, "CGT");
		XscProto.tags.put((short) XscProto.XSC_TAG_INTERCEPT, "INTERCEPT");
		XscProto.tags.put((short) XscProto.XSC_TAG_CLIENT_OOB, "CLIENT_OOB");
		XscProto.tags.put((short) XscProto.XSC_TAG_DEVICE_ID, "DEVICE_ID");
		XscProto.tags.put((short) XscProto.XSC_TAG_NE_GROUP, "NE_GROUP");
	}

	public static final String tagDesc(short t)
	{
		String desc = XscProto.tags.get(t);
		return desc == null ? XscProtoNode.__IEI_UNKNOWN__ : desc;
	}

	public static final int tlvLen(int len)
	{
		return len < 0x000000FE ? 1 : (len <= 0x0000FFFF ? 3 : 5);
	}

	public static final String v2str(XscProtoTlv node)
	{
		switch (node.t)
		{
		case XscProto.XSC_TAG_MSG:
		case XscProto.XSC_TAG_SNE:
		case XscProto.XSC_TAG_MNE:
		case XscProto.XSC_TAG_DNE:
		case XscProto.XSC_TAG_ACTION:
		case XscProto.XSC_TAG_DESC:
		case XscProto.XSC_TAG_PLATFORM:
		case XscProto.XSC_TAG_CGT:
		case XscProto.XSC_TAG_DEVICE_ID:
		case XscProto.XSC_TAG_NE_GROUP:
			return new String(node.v);
		case XscProto.XSC_TAG_TRACE_ID:
		case XscProto.XSC_TAG_SPAN_ID:
		case XscProto.XSC_TAG_P_SPAN_ID:
			return Net.byte2hexStr(node.v).toLowerCase();
		case XscProto.XSC_TAG_TS:
		case XscProto.XSC_TAG_GTS:
		case XscProto.XSC_TAG_UTS:
			return DateMisc.to_yyyy_mm_dd_hh_mm_ss_ms(new Date(Net.byte2long(node.v, 0)));
		case XscProto.XSC_TAG_RET:
		{
			short ret = Net.byte2short(node.v, 0);
			return (ret >= XscProto.RET_SUCCESS && ret <= XscProto.RET_USR_OR_PASSWORD_ERROR) ? XscProto.__xsc_ret__[ret] : Net.byte2hexStrSpace(node.v);
		}
		default:
			return Net.byte2hexStrSpace(node.v);
		}
	}
}
