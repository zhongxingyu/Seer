 /* Smoke Codec
  * Copyright (C) <2004> Wim Taymans <wim@fluendo.com>
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Library General Public
  * License as published by the Free Software Foundation; either
  * version 2 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Library General Public License for more details.
  *
  * You should have received a copy of the GNU Library General Public
  * License along with this library; if not, write to the
  * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
  * Boston, MA 02111-1307, USA.
  */
 
 package com.fluendo.codecs;
 
 import java.awt.*;
 
 public class SmokeCodec {
   private static final int IDX_TYPE       = 0;
   private static final int IDX_WIDTH      = 1;
   private static final int IDX_HEIGHT     = 3;
   private static final int IDX_FPS_NUM    = 5;
   private static final int IDX_FPS_DENOM  = 9;
   private static final int IDX_FLAGS      = 13;
   private static final int IDX_NUM_BLOCKS = 14;
   private static final int IDX_SIZE       = 16;
   private static final int IDX_BLOCKS     = 18;
   private static final int OFFS_PICT      = 18;
 
   private Image reference;
   private MediaTracker mt;
   private Component component;
   private Toolkit toolkit;
 
   public static final int KEYFRAME = (1<<0);
 
   public int type;
   public int width, height;
   public int fps_num, fps_denom;
   public int flags;
  public int size;
   public int blocks;
 
   public SmokeCodec(Component comp, MediaTracker tracker) {
     component = comp;
     toolkit = comp.getToolkit();
     mt = tracker;
   }
 
   public int parseHeader (byte[] in, int offset, int length) {
     short b1, b2, b3, b4; 
 
     type  = in[IDX_TYPE+offset];
 
     b1 = in[IDX_WIDTH+offset];   if (b1<0) b1 += 256;
     b2 = in[IDX_WIDTH+1+offset]; if (b2<0) b2 += 256;
     width = (b1 << 8) | b2;
     b1 = in[IDX_HEIGHT+offset];   if (b1<0) b1 += 256;
     b2 = in[IDX_HEIGHT+1+offset]; if (b2<0) b2 += 256;
     height = (b1 << 8) | b2;
 
     b1 = in[IDX_FPS_NUM+offset];   if (b1<0) b1 += 256;
     b2 = in[IDX_FPS_NUM+1+offset]; if (b2<0) b2 += 256;
     b3 = in[IDX_FPS_NUM+2+offset]; if (b3<0) b3 += 256;
     b4 = in[IDX_FPS_NUM+3+offset]; if (b4<0) b4 += 256;
     fps_num = (b1<<24) | (b2<<16) | (b3<<8) | b4;
     b1 = in[IDX_FPS_DENOM+offset];   if (b1<0) b1 += 256;
     b2 = in[IDX_FPS_DENOM+1+offset]; if (b2<0) b2 += 256;
     b3 = in[IDX_FPS_DENOM+2+offset]; if (b3<0) b3 += 256;
     b4 = in[IDX_FPS_DENOM+3+offset]; if (b4<0) b4 += 256;
     fps_denom = (b1<<24) | (b2<<16) | (b3<<8) | b4;
 
     flags  = in[IDX_FLAGS+offset];
    
    b1 = in[IDX_SIZE+offset]; if (b1<0) b1 += 256;
    b2 = in[IDX_SIZE+offset]; if (b2<0) b2 += 256;
    size = (b1 << 8) | b2;
 
     b1 = in[IDX_NUM_BLOCKS+offset];   if (b1<0) b1 += 256;
     b2 = in[IDX_NUM_BLOCKS+1+offset]; if (b2<0) b2 += 256;
     blocks = (b1 << 8) | b2;
 
     return 0;
   }
   
   public Image decode (byte[] in, int offset, int length) 
   { 
     int b1,b2; 
 
     parseHeader(in, offset, length);
 
     boolean keyframe = ((flags & KEYFRAME) != 0);
 
     if (reference == null && !keyframe) {
       return null;
     }
 
     //System.out.println("blocks: "+blocks+" width "+width+" height "+height);
 
     int imgoff = blocks*2+OFFS_PICT;
 
     //System.out.println("decoding "+blocks+" "+imgoff+" "+(length-imgoff)+" "+keyframe);
     
     Image src = null;
     try {
       src = toolkit.createImage(in, imgoff+offset, length-imgoff);
     }
     catch (Exception e) {e.printStackTrace();}
 
     if (src == null) {
       System.out.println("failed");
       return null;
     }
       
     try {
       mt.addImage(src, 0);
       mt.waitForID(0);
       mt.removeImage(src, 0);
     }
     catch (Exception e) {e.printStackTrace();}
 
     if (reference == null || keyframe) {
       reference = src;
     }
     else {
       if (blocks > 0 ) {
         int src_w = src.getWidth(null);
         int src_h = src.getHeight(null);
         int blockptr = 0;
         int pos, i, j, x, y;
 
         Graphics refgfx;
 
 	offset += IDX_BLOCKS;
 
 	Image newref = component.createImage(width, height);
 	refgfx = newref.getGraphics();
 	refgfx.drawImage(reference, 0, 0, null);
 	reference = newref;
 
         for (i=0; i<src_h; i+=16) {
           for (j=0; j<src_w; j+=16) {
 	    pos = blockptr*2+offset;
             b1 = in[pos];   if (b1<0) b1 += 256;
             b2 = in[pos+1]; if (b2<0) b2 += 256;
 	    pos = (b1 << 8) | b2;
 
             //System.out.println(i+" "+j+" "+pos);
 
 	    x = (pos % (width/16)) * 16;
 	    y = (pos / (width/16)) * 16;
 
 	    refgfx.drawImage(src, 
 			   x, y, x+16, y+16, 
 			   j, i, j+16, i+16, 
 			   null);
 
 	    blockptr++;
 	    if (blockptr >= blocks)
 	      break;
           }
         }
       }
     }
     //System.out.println("decoded "+blocks+" "+imgoff+" "+(length-imgoff)+" "+keyframe+" "+reference);
     return reference;
   }
 }
