 package com.zhuri.util;
 
 public class Base64Codec {
 	static final byte[] base64codes = 
 		"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".getBytes();
 
 	int total = 0;
 	int bitcnt = 0;
 	int bitvalues = 0;
 
 	int lastIn = 0;
 	int lastOut = 0;
 	boolean finish = false;
 
 	public Base64Codec() {
 		
 	}
 
 	int encodeTransfer(byte[] dst, int off, int len,
 			byte[] src, int offin, int lenin) {
 
 		int origLenin = lenin;
 		int origLenout = len;
 
 		int index;
 		while (lenin > 0 && len > 0) {
 			lenin--;
 			bitcnt += 8;
 			bitvalues <<= 8;
			bitvalues |= src[offin++];
 
 			while (bitcnt >= 6 && len > 0) {
 				len--;
 				total++;
 				bitcnt -= 6;
 				index = (bitvalues >> bitcnt);
 				dst[off++] = base64codes[index & 0x3F];
 			}
 		}
 
 		while (bitcnt > 6 && len > 0) {
 			len--;
 			total++;
 			bitcnt -= 6;
 			index = (bitvalues >> bitcnt);
 			dst[off++] = base64codes[index & 0x3F];
 		}
 
 		if (finish && len > 0) {
 			if (bitcnt > 0) {
 				len--;
 				total++;
 				index = (bitvalues << (6 - bitcnt));
 				dst[off++] = base64codes[index & 0x3F];
 				bitcnt = 0;
 			}
 
 			while (len > 0 && (total & 0x3) != 0) {
 				dst[off++] = '=';
 				total++;
 				len--;
 			}
 		}
 
 		lastIn = origLenin - lenin;
 		lastOut = origLenout - len;
 		return 0;
 	}
 
 	int encodeFinish(byte[] swap64, int off, int len) {
 		finish = true;
 		return encodeTransfer(swap64, off, len, null, 0, 0);
 	}
 
 	public static String encode(byte[] data) {
 		int off, count;
 		byte[] swap64 = new byte[1024];
 		String base64title = "";
 		Base64Codec codec = new Base64Codec();
 
 		off = 0;
 		count = data.length;
 
 		do {
 			codec.encodeTransfer(swap64, 0, 1024, data, off, count);
 			base64title += new String(swap64, 0, codec.lastOut);
 			count -= codec.lastIn;
 			off += codec.lastIn;
 		} while (codec.lastOut > 0);
 
 		codec.encodeFinish(swap64, 0, 1024);
 		base64title += new String(swap64, 0, codec.lastOut);
 		swap64 = null;
 		return base64title;
 	}
 };
 
