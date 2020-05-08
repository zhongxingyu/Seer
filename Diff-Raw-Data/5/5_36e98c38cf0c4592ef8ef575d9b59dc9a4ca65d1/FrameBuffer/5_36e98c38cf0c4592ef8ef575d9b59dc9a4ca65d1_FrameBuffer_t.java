 /*
  * Copyright (C) 2009-2010 Autch.net
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package net.autch.androcast;
 
 import java.awt.image.BufferedImage;
 import java.nio.ByteBuffer;
 
 public class FrameBuffer {
 	public int version;
 	public int bpp;
 	public int size;
 	public int width;
 	public int height;
 
 	public int r_offset, r_length;
 	public int b_offset, b_length;
 	public int g_offset, g_length;
 	public int a_offset, a_length;
 
 	public byte[] data, rotated_data;
 	public int rot_w, rot_h;
 
 	public boolean readHeader(int version, ByteBuffer buf)
 	{
 		this.version = version;
 
 		if(version == 16) {
 			// compatibility mode
 			this.bpp = 16;
 
 			this.size = buf.getInt();
 			this.width = buf.getInt();
 			this.height = buf.getInt();
 
 			this.r_offset = 11;
 			this.r_length = 5;
 			this.g_offset = 5;
 			this.g_length = 6;
 			this.b_offset = 0;
 			this.b_length = 5;
 			this.a_offset = 0;
 			this.a_length = 0;
 		} else if(version == 1) {
 			this.bpp = buf.getInt();
 
 			this.size = buf.getInt();
 			this.width = buf.getInt();
 			this.height = buf.getInt();
 
 			// R*B*GA.
 			this.r_offset = buf.getInt();
 			this.r_length = buf.getInt();
 			this.b_offset = buf.getInt();
 			this.b_length = buf.getInt();
 			this.g_offset = buf.getInt();
 			this.g_length = buf.getInt();
 			this.a_offset = buf.getInt();
 			this.a_length = buf.getInt();
 		} else {
 			return false;
 		}
 
 		return true;
 	}
 
 	public static int getHeaderSize(int version) {
 		switch(version) {
 		case 16:
 			return 3;
 		case 1:
 			return 12;
 		default:
 			return 0;
 		}
 	}
 
 	void rotate() {
 		rot_w = this.height;
 		rot_h = this.width;
 
 		int count = data.length;
 		rotated_data = new byte[count];
 		int bytes = bpp >> 3;
 		final int w = this.width, h = this.height;
 		for(int y = 0; y < h; y++) {
 			for(int x = 0; x < w; x++) {
 				System.arraycopy(this.data, (y * w + x) * bytes,
 						rotated_data, ((w - x - 1) * h + y) * bytes,
 						bytes);
 			}
 		}
 	}
 
 
 	private int getMask(int l, int o) {
 		int res = ((1 << l) - 1) << o;
 
 		if(bpp == 32) return Integer.reverseBytes(res);
 		return res;
 	}
 
 	public int getMaskR() {
 		return getMask(r_length, r_offset);
 	}
 	public int getMaskG() {
 		return getMask(g_length, g_offset);
 	}
 	public int getMaskB() {
 		return getMask(b_length, b_offset);
 	}
 
	private static int getPelPart(int v, int l, int o) {
 		return ((v >>> o) & ((1 << l) - 1)) << (8-l); 
 	}
 
 	void render(boolean landscape, BufferedImage image) {
 		if (landscape) {
 			rotate();
 			transformRawImageP(rotated_data, rot_w, rot_h, image);
 		} else {
 			transformRawImageP(data, width, height, image);
 		}
 	}
 
 	public void transformRawImageP(byte[] buffer, int w, int h, BufferedImage image) {
 		int index = 0;
 		for (int y = 0; y < h; y++) {
 			for (int x = 0; x < w; x++) {
 
 				int value = buffer[index++] & 0x00FF;
 				value |= (buffer[index++] << 8) & 0x0FF00;
				if(bpp >= 24) value |= (buffer[index++] << 16) & 0x00FF0000;
				if(bpp == 32) value |= (buffer[index++] << 24) & 0xFF000000;
 
 				int r = getPelPart(value, r_length, r_offset);
 				int g = getPelPart(value, g_length, g_offset);
 				int b = getPelPart(value, b_length, b_offset);
 				int a = 0xff;
 				if(a_length > 0) {
 					a = getPelPart(value, a_length, a_offset);
 				}
 
 				value = a << 24 | r << 16 | g << 8 | b;
 
 				image.setRGB(x, y, value);
 			}
 		}
 	}
 }
