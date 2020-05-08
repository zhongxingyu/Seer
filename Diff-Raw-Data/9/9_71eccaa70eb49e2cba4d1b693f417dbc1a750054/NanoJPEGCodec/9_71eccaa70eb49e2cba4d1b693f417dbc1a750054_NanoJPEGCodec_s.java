 // This is a Java port of the C++ class of 'NanoJPEG'. The author of the C++ 
 // port is Scott Graham, and the original author of 'NanoJPEG' is 
 // Martin J. Fielder
 //
 // As a port of a port, I hope there has been nothing lost in translation.
 //
 // Jesse Riddle <jesse@orkey.net>
 //
 // The C++ port's heading follows:
 //
 // This is a cosmetic restructing and port to C++ class of 'NanoJPEG', found
 // at http://keyj.s2000.ws/?p=137. It's been made somewhat thread safe in that
 // all context information is pulled into an object, rather than being global
 // as the original was. Other than that, the original is superior in
 // configurability, comments, cleanliness, portability, etc. and should be
 // preferred. The only other possible benefit this version can claim is that
 // it's crammed into one header file.
 //
 // Scott Graham <scott.jpegdecoder@h4ck3r.net>
 //
 // The original license follows:
 //
 // NanoJPEG -- KeyJ's Tiny Baseline JPEG Decoder
 // version 1.0 (2009-04-29)
 // by Martin J. Fiedler <martin.fiedler@gmx.net>
 //
 // This software is published under the terms of KeyJ's Research License,
 // version 0.2. Usage of this software is subject to the following conditions:
 // 0. There's no warranty whatsoever. The author(s) of this software can not
 //    be held liable for any damages that occur when using this software.
 // 1. This software may be used freely for both non-commercial and commercial
 //    purposes.
 // 2. This software may be redistributed freely as long as no fees are charged
 //    for the distribution and this license information is included.
 // 3. This software may be modified freely except for this license information,
 //    which must not be changed in any way.
 // 4. If anything other than configuration, indentation or comments have been
 //    altered in the code, the original author(s) must receive a copy of the
 //    modified code.
 
 package edu.ius.robotics.robots.codecs.nanojpeg;
 
 public class NanoJPEGCodec
 {	
 	private static class VlcCode
 	{
 		/* ignoring convention to create get and set methods for these properties */
 		public byte bits;
 		public byte code;
 	}
 	
 	private static class Component
 	{
 		public int cid;
 		public int ssx, ssy;
 		public int width, height;
 		public int stride;
 		public int qtsel;
 		public int actabsel, dctabsel;
 		public int dcpred;
 		byte[] pixels;
 	}
 	
 	private static class Context
 	{
 		public DecodeResult error;
 		public byte[] pos;
 		public int posoff;
 		public int size;
 		public int length;
 		public int width, height;
 		public int mbwidth, mbheight;
 		public int mbsizex, mbsizey;
 		public int ncomp;
 		public Component[] comp;
 		public int qtused, qtavail;
 		public byte[][] qtab;
 		public VlcCode[][] vlctab;
 		public int buf, bufbits;
 		public int[] block;
 		public int rstinterval;
 		public byte[] rgb;
 		
 		public Context()
 		{
 			this.posoff = 0;
 			this.comp = new Component[3];
 			this.qtab = new byte[4][64];
 			this.vlctab = new VlcCode[4][65536];
 			this.block = new int[64];
 			this.rgb = new byte[3];
 		}
 	}
 	
 	NanojpegCodec.Context ctx = new NanojpegCodec.Context();
 	byte[] ZZ = new byte[64];
 	
 	private byte clip(int x)
 	{
 		return (x < 0) ? (byte) 0 : ((0xFF < x) ? (byte) (0xFF & 0xFF) : (byte) (0xFF & x));
 	}
 	
 	public static final int W1 = 2841;  
 	public static final int W2 = 2676; 
 	public static final int W3 = 2408; 
 	public static final int W5 = 1609; 
 	public static final int W6 = 1108; 
 	public static final int W7 = 565;
 	
 //	private void rowIDCT(int[] blk)
 //	{
 //		int x0, x1, x2, x3, x4, x5, x6, x7, x8;
 //		if (0 == ((x1 = blk[4] << 11)
 //				| (x2 = blk[6])
 //				| (x3 = blk[2])
 //				| (x4 = blk[1])
 //				| (x5 = blk[7])
 //				| (x6 = blk[5])
 //				| (x7 = blk[3])))
 //		{
 //			blk[0] = blk[1] = blk[2] = blk[3] = blk[4] = blk[5] = blk[6] = blk[7] = blk[0] << 3;
 //			return;
 //		}
 //		 x0 = (blk[0] << 11) + 128;
 //         x8 = W7 * (x4 + x5);
 //         x4 = x8 + (W1 - W7) * x4;
 //         x5 = x8 - (W1 + W7) * x5;
 //         x8 = W3 * (x6 + x7);
 //         x6 = x8 - (W3 - W5) * x6;
 //         x7 = x8 - (W3 + W5) * x7;
 //         x8 = x0 + x1;
 //         x0 -= x1;
 //         x1 = W6 * (x3 + x2);
 //         x2 = x1 - (W2 + W6) * x2;
 //         x3 = x1 + (W2 - W6) * x3;
 //         x1 = x4 + x6;
 //         x4 -= x6;
 //         x6 = x5 + x7;
 //         x5 -= x7;
 //         x7 = x8 + x3;
 //         x8 -= x3;
 //         x3 = x0 + x2;
 //         x0 -= x2;
 //         x2 = (181 * (x4 + x5) + 128) >> 8;
 //         x4 = (181 * (x4 - x5) + 128) >> 8;
 //         blk[0] = (x7 + x1) >> 8;
 //         blk[1] = (x3 + x2) >> 8;
 //         blk[2] = (x0 + x4) >> 8;
 //         blk[3] = (x8 + x6) >> 8;
 //         blk[4] = (x8 - x6) >> 8;
 //         blk[5] = (x0 - x4) >> 8;
 //         blk[6] = (x3 - x2) >> 8;
 //         blk[7] = (x7 - x1) >> 8;
 //	}
 	
 	private void rowIDCT(int[] blk, int off)
 	{
 		int x0, x1, x2, x3, x4, x5, x6, x7, x8;
 		if (0 == ((x1 = blk[off + 4] << 11)
 				| (x2 = blk[off + 6])
 				| (x3 = blk[off + 2])
 				| (x4 = blk[off + 1])
 				| (x5 = blk[off + 7])
 				| (x6 = blk[off + 5])
 				| (x7 = blk[off + 3])))
 		{
 			blk[off + 0] = blk[off + 1] = blk[off + 2] = blk[off + 3] = blk[off + 4] = blk[off + 5] = blk[off + 6] = blk[off + 7] = blk[off + 0] << 3;
 			return;
 		}
 		 x0 = (blk[off + 0] << 11) + 128;
          x8 = W7 * (x4 + x5);
          x4 = x8 + (W1 - W7) * x4;
          x5 = x8 - (W1 + W7) * x5;
          x8 = W3 * (x6 + x7);
          x6 = x8 - (W3 - W5) * x6;
          x7 = x8 - (W3 + W5) * x7;
          x8 = x0 + x1;
          x0 -= x1;
          x1 = W6 * (x3 + x2);
          x2 = x1 - (W2 + W6) * x2;
          x3 = x1 + (W2 - W6) * x3;
          x1 = x4 + x6;
          x4 -= x6;
          x6 = x5 + x7;
          x5 -= x7;
          x7 = x8 + x3;
          x8 -= x3;
          x3 = x0 + x2;
          x0 -= x2;
          x2 = (181 * (x4 + x5) + 128) >> 8;
          x4 = (181 * (x4 - x5) + 128) >> 8;
          blk[off + 0] = (x7 + x1) >> 8;
          blk[off + 1] = (x3 + x2) >> 8;
          blk[off + 2] = (x0 + x4) >> 8;
          blk[off + 3] = (x8 + x6) >> 8;
          blk[off + 4] = (x8 - x6) >> 8;
          blk[off + 5] = (x0 - x4) >> 8;
          blk[off + 6] = (x3 - x2) >> 8;
          blk[off + 7] = (x7 - x1) >> 8;
 	}
 	
 	private void colIDCT(int[] blk, byte[] out, int off, int stride)
 	{
 		int x0, x1, x2, x3, x4, x5, x6, x7, x8;
         if (0 == ((x1 = blk[off + 8*4] << 8)
         		| (x2 = blk[off + 8*6])
         		| (x3 = blk[off + 8*2])
         		| (x4 = blk[off + 8*1])
         		| (x5 = blk[off + 8*7])
         		| (x6 = blk[off + 8*5])
         		| (x7 = blk[off + 8*3])))
         {
             x1 = clip(((blk[off + 0] + 32) >> 6) + 128);
             int i = 0;
             for (x0 = 8; 0 < x0; --x0) 
             {
                 out[off + (i += stride)] = (byte) (0xFF & x1);
             }
             return;
         }
         x0 = (blk[off + 0] << 8) + 8192;
         x8 = W7 * (x4 + x5) + 4;
         x4 = (x8 + (W1 - W7) * x4) >> 3;
         x5 = (x8 - (W1 + W7) * x5) >> 3;
         x8 = W3 * (x6 + x7) + 4;
         x6 = (x8 - (W3 - W5) * x6) >> 3;
         x7 = (x8 - (W3 + W5) * x7) >> 3;
         x8 = x0 + x1;
         x0 -= x1;
         x1 = W6 * (x3 + x2) + 4;
         x2 = (x1 - (W2 + W6) * x2) >> 3;
         x3 = (x1 + (W2 - W6) * x3) >> 3;
         x1 = x4 + x6;
         x4 -= x6;
         x6 = x5 + x7;
         x5 -= x7;
         x7 = x8 + x3;
         x8 -= x3;
         x3 = x0 + x2;
         x0 -= x2;
         x2 = (181 * (x4 + x5) + 128) >> 8;
         x4 = (181 * (x4 - x5) + 128) >> 8;
         int i = 0;
         out[off + (i += stride)] = (byte) (0xFF & clip(((x7 + x1) >> 14) + 128));
         out[off + (i += stride)] = clip(((x3 + x2) >> 14) + 128);
         out[off + (i += stride)] = clip(((x0 + x4) >> 14) + 128);
         out[off + (i += stride)] = clip(((x8 + x6) >> 14) + 128);
         out[off + (i += stride)] = clip(((x8 - x6) >> 14) + 128);
         out[off + (i += stride)] = clip(((x0 - x4) >> 14) + 128);
         out[off + (i += stride)] = clip(((x3 - x2) >> 14) + 128);
         out[off + i] = clip(((x7 - x1) >> 14) + 128);
 	}
 	
 	private int showBits(int bits)
 	{
 		byte newByte;
 		if (0 == bits)
 		{
 			return 0;
 		}
 		
 		while (ctx.bufbits < bits)
 		{
 			if (ctx.size <= 0)
 			{
 				ctx.buf = (ctx.buf << 8) | 0xff;
 				ctx.bufbits += 8;
 				continue;
 			}
 			int i = 0;
 			newByte = (byte) (0xFF & ctx.pos[ctx.posoff + i++]);
 			ctx.size--;
 			ctx.bufbits += 8;
 			ctx.buf = (ctx.buf << 8) | (byte) (0xFF & newByte);
 			if (0xff == (byte) (0xFF & newByte))
 			{
 				if (0 < ctx.size)
 				{
 					byte marker = (byte) (0xFF & ctx.pos[ctx.posoff + i++]);
 					ctx.size--;
 					switch (marker)
 					{
 					case 0:
 						break;
 					case (byte) (0xFF & 0xD9):
 						ctx.size = 0;
 						break;
 					default:
 						if (0xD0 != (byte) (marker & 0xF8))
 						{
 							ctx.error = DecodeResult.SyntaxError;
 						}
 						else
 						{
 							ctx.buf = (ctx.buf << 8) | marker;
 							ctx.bufbits += 8;
 						}
 					}
 				}
 				else
 				{
 					ctx.error = DecodeResult.SyntaxError;
 				}
 			}
 		}
 		return (ctx.buf >> (ctx.bufbits - bits)) & ((1 << bits) - 1);
 	}
 	
 	public void skipBits(int bits)
 	{
 		if (ctx.bufbits < bits)
 		{
 			showBits(bits);
 		}
 		ctx.bufbits -= bits;
 	}
 	
 	public int getBits(int bits)
 	{
 		int res = showBits(bits);
 		skipBits(bits);
 		return res;
 	}
 	
 	public void byteAlign()
 	{
 		ctx.bufbits &= 0xF8;
 	}
 	
 	private void skip(int count)
 	{
 		//ctx.pos += count;
 		ctx.posoff += count;
 		ctx.size -= count;
 		ctx.length -= count;
 		if (ctx.size < 0)
 		{
 			ctx.error = DecodeResult.SyntaxError;
 		}
 	}
 	
 	public int decode16(byte[] pos, int off)
 	{
 		return ((pos[off] << 16) | pos[off + 1]);
 	}
 	
 	private void decodeLength() throws NanojpegException
 	{
 		ctx.length = decode16(ctx.pos, 0);
 		if (ctx.size < ctx.length)
 		{
 			throw new NanojpegException(DecodeResult.SyntaxError); // TODO check this
 		}
 		skip(2);
 	}
 	
 	public void skipMarker() throws NanojpegException
 	{
 		decodeLength();
 		skip(ctx.length);
 	}
 	
 	private void decodeSOF() throws NanojpegException
 	{
 		int i, ssxmax = 0, ssymax = 0;
 		Component c;
 		decodeLength();
 		if (ctx.length < 9)
 		{
 			throw new NanojpegException(DecodeResult.SyntaxError);
 		}
 		if (8 != ctx.pos[ctx.posoff])
 		{
 			throw new NanojpegException(DecodeResult.Unsupported);
 		}
 		ctx.height = decode16(ctx.pos, 1);
 		ctx.width = decode16(ctx.pos, 3);
 		ctx.ncomp = ctx.pos[ctx.posoff + 5];
 		skip(6);
 		switch (ctx.ncomp)
 		{
 		case 1:
 		case 3:
 			break;
 		default:
 			throw new NanojpegException(DecodeResult.Unsupported);
 		}
 		if (ctx.length < 3*ctx.ncomp)
 		{
 			throw new NanojpegException(DecodeResult.SyntaxError);
 		}
 		for (i = 0; i < ctx.ncomp; ++i)
 		{
 			c = ctx.comp[i];
 			c.cid = ctx.pos[ctx.posoff]; // ctx.pos[0]
 			if (0 == (c.ssx = ctx.pos[ctx.posoff + 1] >> 4)) // ctx.pos[1];
 			{
 				throw new NanojpegException(DecodeResult.SyntaxError);
 			}
 			if (0 < (c.ssx & (c.ssx - 1))) // non-power of two
 			{
 				throw new NanojpegException(DecodeResult.Unsupported);
 			}
 			if (0 == (c.ssy = ctx.pos[ctx.posoff + 1] & 15)) // ctx.pos[1];
 			{
 				throw new NanojpegException(DecodeResult.SyntaxError);
 			}
 			if (0 < (c.ssy & (c.ssy - 1))) // non-power of two
 			{
 				throw new NanojpegException(DecodeResult.Unsupported);
 			}
 			if (0 < ((c.qtsel = ctx.pos[ctx.posoff + 2]) & 0xFC)) // ctx.pos[2];
 			{
 				throw new NanojpegException(DecodeResult.SyntaxError);
 			}
 			skip(3);
 			ctx.qtused |= 1 << c.qtsel;
 			if (ssxmax < c.ssx)
 			{
 				ssxmax = c.ssx;
 			}
 			if (ssymax < c.ssy)
 			{
 				ssymax = c.ssy;
 			}
 		}
 		ctx.mbsizex = ssxmax << 3;
 		ctx.mbsizey = ssymax << 3;
 		ctx.mbwidth = (ctx.width + ctx.mbsizex - 1) / ctx.mbsizex;
 		ctx.mbheight = (ctx.height + ctx.mbsizey - 1) / ctx.mbsizey;
 		for (i = 0; i < ctx.ncomp; ++i)
 		{
 			c = ctx.comp[i];
 			c.width = (ctx.width * c.ssx + ssxmax - 1) / ssxmax;
 			c.stride = (c.width + 7) & 0x7FFFFFF8;
 			c.height = (ctx.height * c.ssy + ssymax - 1) / ssymax;
 			c.stride = ctx.mbwidth * ctx.mbsizex * c.ssx / ssxmax;
 			if (((c.width < 3) && (c.ssx != ssxmax)) || ((c.height < 3) && (c.ssy != ssymax)))
 			{
 				throw new NanojpegException(DecodeResult.Unsupported);
 			}
 			c.pixels = new byte[c.stride * (ctx.mbheight * ctx.mbsizey * c.ssy / ssymax)];
 		}
 		if (3 == ctx.ncomp)
 		{
 			ctx.rgb = new byte[ctx.width * ctx.height * ctx.ncomp];
 		}
 		skip(ctx.length);
 	}
 	
 	private void decodeDHT() throws NanojpegException
 	{
 		int codelen, count, remain, spread, i, j, k;
 		VlcCode vlc;
 		int[] counts = new int[16];
 		decodeLength();
 		while (17 <= ctx.length)
 		{
 			i = ctx.pos[ctx.posoff];
 			if (0 < (i & 0xEC))
 			{
 				throw new NanojpegException(DecodeResult.SyntaxError);
 			}
 			if (0 < (i & 0x02))
 			{
 				throw new NanojpegException(DecodeResult.Unsupported);
 			}
 			i = (i | (i >> 3)) & 3; // combined DC/AC + tableid value
 			for (codelen = 0; codelen < 16; ++codelen)
 			{
 				counts[codelen] = (byte) (0xFF & ctx.pos[ctx.posoff + codelen + 1]); // ctx.pos[codelen + 1];
 			}
 			skip(17);
 			vlc = ctx.vlctab[i][0];
 			remain = spread = 65536;
 			for (codelen = 0; codelen < 16; ++codelen)
 			{
 				spread >>= 1;
 				count = counts[codelen];
 				if (0 == count)
 				{
 					continue;
 				}
 				if (ctx.length < count)
 				{
 					throw new NanojpegException(DecodeResult.SyntaxError);
 				}
 				remain -= count << (16 - codelen);
 				if (remain < 0)
 				{
 					throw new NanojpegException(DecodeResult.SyntaxError);
 				}
 				for (i = 0; i < count; ++i)
 				{
 					k = 0;
 					byte code = (byte) (0xFF & ctx.pos[ctx.posoff + i]);
 					for (j = spread; 0 < j; --j)
 					{
 						vlc.bits = (byte) (0xFF & codelen);
 						vlc.code = (byte) (0xFF & code);
 						vlc = ctx.vlctab[i][k++];
 					}
 				}
 				skip(count);
 			}
 			k = 0;
 			while (0 < remain--)
 			{
 				vlc.bits = 0;
 				vlc = ctx.vlctab[i][k++];
 			}
 		}
 		if (0 < ctx.length)
 		{
 			throw new NanojpegException(DecodeResult.SyntaxError);
 		}
 	}
 	
 	private void decodeDQT() throws NanojpegException
 	{
 		int i;
 		byte[] t;
 		decodeLength();
 		while (65 <= ctx.length)
 		{
 			i = ctx.pos[ctx.posoff];
 			if (0 < ((byte) (0xFF & i) & 0xFC))
 			{
 				throw new NanojpegException(DecodeResult.SyntaxError);
 			}
 			ctx.qtavail |= 1 << i;
 			t = ctx.qtab[i];
 			for (i = 0; i < 64; ++i)
 			{
 				t[i] = ctx.pos[ctx.posoff + i + 1];
 			}
 			skip(65);
 		}
 	}
 	
 	private void decodeDRI() throws NanojpegException
 	{
 		decodeLength();
 		if (ctx.length < 2)
 		{
 			throw new NanojpegException(DecodeResult.SyntaxError);
 		}
 		ctx.rstinterval = decode16(ctx.pos, 0);
 		skip(ctx.length);
 	}
 	
 	private int getVLC(VlcCode[] vlc, Reference<Byte> code)
 	{
 		int value = showBits(16);
 		int bits = (byte) (0xFF & vlc[value].bits);
 		if (0 == bits)
 		{
 			ctx.error = DecodeResult.SyntaxError;
 			return 0;
 		}
 		skipBits(bits);
 		value = (byte) (0xFF & vlc[value].code);
 		if (null != code)
 		{
 			code.set((byte) (0xFF & value));
 		}
 		bits = (byte) (0xFF & (value & 15));
 		if (0 == bits)
 		{
 			return 0;
 		}
 		value = getBits(bits);
 		if (value < (1 << (bits - 1)))
 		{
 			value += ((-1) << bits) + 1;
 		}
 		return value;
 	}
 	
 	private void decodeBlock(Component c, int pixoff) throws NanojpegException
 	{
 		Reference<Byte> code = new Reference<Byte>();
 		int value, coef = 0;
 		for (int i = 0, z = ctx.block.length; i < z; ++i)
 		{
 			ctx.block[i] = 0;
 			c.dcpred += getVLC(ctx.vlctab[c.dctabsel], null);
 			ctx.block[0] = c.dcpred * ctx.qtab[c.qtsel][0];
 			do
 			{
 				value = getVLC(ctx.vlctab[c.actabsel], code);
 				if (0 == code.get())
 				{
 					break; // EOB					
 				}
 				if (0 == (code.get() & 0x0F) && (code.get() != 0xF0))
 				{
 					throw new NanojpegException(DecodeResult.SyntaxError);
 				}
 				coef += (code.get() >> 4) + 1;
 				if (63 < coef)
 				{
 					throw new NanojpegException(DecodeResult.SyntaxError);
 				}
 				ctx.block[(int) ZZ[coef]] = value * ctx.qtab[c.qtsel][coef];
 			} while (coef < 63);
 			for (coef = 0; coef < 64; coef += 8)
 			{
 				rowIDCT(ctx.block, coef);
 			}
 			for (coef = 0; coef < 8; ++coef)
 			{
 				colIDCT(ctx.block, c.pixels, pixoff + coef, c.stride); // colIDCT(ctx.block[coef], ...)
 			}
 		}
 	}
 	
 	private void decodeScan() throws NanojpegException
 	{
 		int i, mbx, mby, sbx, sby;
 		int rstcount = ctx.rstinterval, nextrst = 0;
 		Component c;
 		decodeLength();
 		if (ctx.length < (4 + 2*ctx.ncomp))
 		{
 			throw new NanojpegException(DecodeResult.SyntaxError);
 		}
 		if (ctx.pos[ctx.posoff] != ctx.ncomp)
 		{
 			throw new NanojpegException(DecodeResult.Unsupported);
 		}
 		skip(1);
 		for (i = 0; i < ctx.ncomp; ++i)
 		{
 			c = ctx.comp[i];
 			if (ctx.pos[ctx.posoff] != c.cid)
 			{
 				throw new NanojpegException(DecodeResult.SyntaxError);
 			}
 			if (0 != (ctx.pos[ctx.posoff + 1] & 0xEE))
 			{
 				throw new NanojpegException(DecodeResult.SyntaxError);
 			}
 			c.dctabsel = ctx.pos[ctx.posoff + 1] >> 4;
 			c.actabsel = (ctx.pos[ctx.posoff + 1] & 1) | 2;
 			skip(2);
 		}
 		if (0 != ctx.pos[ctx.posoff] || 63 != ctx.pos[ctx.posoff + 1] || 0 != ctx.pos[ctx.posoff + 2])
 		{
 			throw new NanojpegException(DecodeResult.Unsupported);
 		}
 		skip(ctx.length);
 		for (mby = 0; mby < ctx.mbheight; ++mby)
 		{
 			for (mbx = 0; mbx < ctx.mbwidth; ++mbx)
 			{
 				for (i = 0; i < ctx.ncomp; ++i)
 				{
 					c = ctx.comp[i];
 					for (sby = 0; sby < c.ssy; ++sby)
 					{
 						for (sbx = 0; sbx < c.ssx; ++sbx)
 						{
 							decodeBlock(c, ((mby * c.ssy + sby) * c.stride + mbx * c.ssx + sbx) << 3);
 							if (DecodeResult.OK != ctx.error)
 							{
 								return;
 							}
 						}
 					}
 				}
 				if (0 != ctx.rstinterval && 0 < --rstcount)
 				{
 					byteAlign();
 					i = getBits(16);
 					if (0xFFD0 != (i & 0xFFF8) || (i & 7) != nextrst)
 					{
 						throw new NanojpegException(DecodeResult.SyntaxError);
 					}
 					nextrst = (nextrst + 1) & 7;
 					rstcount = ctx.rstinterval;
 					for (i = 0; i < 3; ++i)
 					{
 						ctx.comp[i].dcpred = 0;
 					}
 				}
 			}
 		}
 		ctx.error = DecodeResult.Internal_Finished;
 	}
 	
 	public static int CF4A = -9;
 	public static int CF4B = 111;
 	public static int CF4C = 29;
 	public static int CF4D = -3;
 	public static int CF3A = 28;
 	public static int CF3B = 109;
 	public static int CF3C = -9;
 	public static int CF3X = 104;
 	public static int CF3Y = 27;
 	public static int CF3Z = -3;
 	public static int CF2A = 139;
 	public static int CF2B = -11;
 	
 	private byte CF(int x)
 	{
 		return clip((x + 64) >> 7);
 	}
 	
 	private void upsampleH(Component c)
 	{
 		int xmax = c.width -3;
 		byte[] out, lin, lout;
 		int x, y, linoff = 0, loutoff = 0;
 		out = new byte[(c.width * c.height) << 1];
 		if (null != out)
 		{
 			lin = c.pixels;
 			lout = out;
 			for (y = c.height; 0 < y; --y)
 			{
 				lout[0] = CF(CF2A * lin[0] + CF2B * lin[1]);
 				lout[1] = CF(CF3X * lin[0] + CF3Y * lin[1] + CF3Z * lin[2]);
 				lout[2] = CF(CF3A * lin[0] + CF3B * lin[1] + CF3C * lin[2]);
 				for (x = 0; x < xmax; ++x)
 				{
 					lout[(x << 1) + 3] = CF(CF4A * lin[x] + CF4B * lin[x + 1] + CF4C * lin[x + 2] + CF4D * lin[x + 3]);
 					lout[(x << 1) + 4] = CF(CF4D * lin[x] + CF4C * lin[x + 1] + CF4B * lin[x + 2] + CF4A * lin[x + 3]);
 				}
 				linoff += c.stride;
 				loutoff += c.width << 1;
 				lout[loutoff - 3] = CF(CF3A * lin[linoff - 1] + CF3B * lin[linoff - 2] + CF3C * lin[linoff - 3]);
 				lout[loutoff - 2] = CF(CF3X * lin[linoff - 1] + CF3Y * lin[linoff - 2] + CF3Z * lin[linoff - 3]);
 				lout[loutoff - 1] = CF(CF2A * lin[linoff - 1] + CF2B * lin[linoff - 2]);
 			}
 			c.width <<= 1;
 			c.stride = c.width;
 			c.pixels = null; // free
 			c.pixels = out;
 		}
 	}
 	
 	private void upsampleV(Component c)
 	{
 		int w = c.width, s1 = c.stride, s2 = s1 + s1, t = 0, i = 0;
 		byte[] out, cin, cout;
 		int x, y;
 		out = new byte[(c.width * c.height) << 1];
 		
 		cin = c.pixels; // c.pixels[x];
 		cout = out; // out[x];
 		for (x = 0; x < w; ++x)
 		{
 			i = t = x;
 			cout[t += w] = CF(CF2A * cin[i + 0] + CF2B * cin[i + s1]);
 			cout[t += w] = CF(CF3X * cin[i + 0] + CF3Y * cin[i + s1] + CF3Z * cin[i + s2]);
 			cout[t += w] = CF(CF3A * cin[i + 0] + CF3B * cin[i + s1] + CF3C * cin[i + s2]);
 			i += s1; //cin += s1;
 			for (y = c.height - 3; 0 < y; --y)
 			{
 				cout[t += w] = CF(CF4A * cin[i - s1] + CF4B * cin[i + 0] + CF3C * cin[i + s1] + CF4D * cin[i + s2]);
 				cout[t += w] = CF(CF4D * cin[i - s1] + CF4C * cin[i + 0] + CF4B * cin[i + s1] + CF4A * cin[i + s2]);
 				i += s1;
 			}
 			i += s1;
 			cout[t += w] = CF(CF3A * cin[i + 0] + CF3B * cin[i - s1] + CF3C * cin[i - s2]);
 			cout[t += w] = CF(CF3X * cin[i + 0] + CF3Y * cin[i - s1] + CF3Z * cin[i - s2]);
 			cout[t] = CF(CF2A * cin[i + 0] + CF2B * cin[i - s1]);
 		}
 		c.height <<= 1;
 		c.stride = c.width;
 		c.pixels = null;
 		c.pixels = out;
 	}
 	
 	private void convert()
 	{
 		int i;
 		Component c;
 		int pyoff = 0;
 		int pcboff = 0;
 		int pcroff = 0;
 		int pinoff = 0;
 		int poutoff = 0;
 		int prgboff = 0;
 		
 		for (i = 0; i < ctx.ncomp; ++i)
 		{
 			c = ctx.comp[i];
 			while ((c.width < ctx.width) || (c.height < ctx.height))
 			{
 				if (c.width < ctx.width)
 				{
 					upsampleH(c);
 				}
 				if (DecodeResult.OK != ctx.error)
 				{
 					return;
 				}
 				if (c.height < ctx.height)
 				{
 					upsampleV(c);
 				}
 				if (DecodeResult.OK != ctx.error)
 				{
 					return;
 				}
 			}
 		}
 		if (3 == ctx.ncomp)
 		{
 			// convert to RGB
 			int x, yy;
 			byte[] prgb = ctx.rgb;
 			byte[] py = ctx.comp[0].pixels;
 			byte[] pcb = ctx.comp[1].pixels;
 			byte[] pcr = ctx.comp[2].pixels;
 			for (yy = ctx.height; 0 < yy; --yy)
 			{
 				for (x = 0; x < ctx.width; ++x)
 				{
 					int y = py[pyoff + x] << 8;
 					int cb = pcb[pcboff + x] - 128;
 					int cr = pcr[pcroff + x] - 128;
 					prgb[prgboff++] = clip((y + 359 * cr + 128) >> 8);
 					prgb[prgboff++] = clip((y - 88 * cb - 183 * cr + 128) >> 8);
 					prgb[prgboff++] = clip((y + 454 * cb + 128) >> 8);
 				}
 				//py += ctx.comp[0].stride;
 				//pcb += ctx.comp[1].stride;
 				//pcr += ctx.comp[2].stride;
 				pyoff += ctx.comp[0].stride;
 				pcboff += ctx.comp[1].stride;
 				pcroff += ctx.comp[2].stride;
 			}
 		}
 		else if (ctx.comp[0].width != ctx.comp[0].stride)
 		{
 			// grayscale -> only remove stride
 			//byte[] pin = ctx.comp[0].pixels[ctx.comp[0].stride];
 			//byte[] pout = ctx.comp[0].pixels[ctx.comp[0].width];
 			pinoff = ctx.comp[0].stride;
 			byte[] pin = ctx.comp[0].pixels;
 			poutoff = ctx.comp[0].width;
 			byte[] pout = ctx.comp[0].pixels;
 			
 			int y;
 			for (y = ctx.comp[0].height - 1; 0 < y; --y)
 			{
 				//memcpy(pout, pin, ctx.comp[0].width);
 				for (int j = 0, z = ctx.comp[0].width; j < z; ++j)
 				{
 					pout[poutoff + j] = pin[pinoff + j];
 				}
 				pinoff += ctx.comp[0].stride;
 				poutoff += ctx.comp[0].width;
 			}
 			ctx.comp[0].stride = ctx.comp[0].width;
 		}
 	}
 	
 	DecodeResult decode(byte[] jpeg, int size) throws NanojpegException
 	{
 		ctx.pos = jpeg;
 		ctx.size = size & 0x7FFFFFFF;
 		if (ctx.size < 2)
 		{
 			return DecodeResult.NotAJpeg;
 		}
 		if (0 != ((ctx.pos[ctx.posoff + 0] ^ 0xFF) | (ctx.pos[ctx.posoff + 1] ^ 0xD8)))
 		{
 			return DecodeResult.NotAJpeg;
 		}
 		skip(2);
 		while (DecodeResult.OK == ctx.error)
 		{
 			if ((ctx.size < 2) || (ctx.pos[ctx.posoff + 0] != 0xFF))
 			{
 				return DecodeResult.SyntaxError;
 			}
 			switch (ctx.pos[ctx.posoff - 1])
 			{
 			case (byte) (0xFF & 0xC0): decodeSOF();	break;
 			case (byte) (0xFF & 0xC4): decodeDHT();	break;
 			case (byte) (0xFF & 0xDB): decodeDQT(); break;
 			case (byte) (0xFF & 0xDD): decodeDRI(); break;
 			case (byte) (0xFF & 0xDA): decodeScan(); break;
 			case (byte) (0xFF & 0xFE): skipMarker(); break;
 			default:
 				if (0xE0 == (ctx.pos[ctx.posoff - 1] & 0xF0))
 				{
 					skipMarker();
 				}
 				else
 				{
 					return DecodeResult.Unsupported;
 				}
 			}
 		}
 		if (DecodeResult.Internal_Finished != ctx.error)
 		{
 			return ctx.error;
 		}
 		ctx.error = DecodeResult.OK;
 		convert();
 		return ctx.error;
 	}
 	
	public NanojpegCodec(byte[] data) throws NanojpegException
 	{
 		byte[] tmp = { 0, 1, 8, 16, 9, 2, 3, 10, 17, 24, 32, 25, 18,
 		        11, 4, 5, 12, 19, 26, 33, 40, 48, 41, 34, 27, 20, 13, 6, 7, 14, 21, 28, 35,
 		        42, 49, 56, 57, 50, 43, 36, 29, 22, 15, 23, 30, 37, 44, 51, 58, 59, 52, 45,
 		        38, 31, 39, 46, 53, 60, 61, 54, 47, 55, 62, 63 };
 	    //memcpy(ZZ, tmp, sizeof(ZZ));
 		for (int i = 0; i < ZZ.length; ++i)
 		{
 			ZZ[i] = tmp[i];
 		}
 	    //memset(&ctx, 0, sizeof(Context));
 	    //decode(data, size);
 		decode(data, data.length);
 	}
 	
 	public int getHeight()
 	{
 		return ctx.height;
 	}
 	
 	public int getWidth()
 	{
 		return ctx.width;
 	}
 	
 	/**
 	 * If isColor(), then 24 bit as R, G, B bytes
 	 * else 8 bit luminance
 	 * @return
 	 */
 	public byte[] getImage(byte[] input, int length)
 	{
 		return (ctx.ncomp == 1) ? ctx.comp[0].pixels : ctx.rgb;
 	}
 	
 	public int getImageSize()
 	{
 		return ctx.width * ctx.height * ctx.ncomp;
 	}
 	
 	public boolean isColor()
 	{
 		if (ctx.ncomp != 1)
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 }
