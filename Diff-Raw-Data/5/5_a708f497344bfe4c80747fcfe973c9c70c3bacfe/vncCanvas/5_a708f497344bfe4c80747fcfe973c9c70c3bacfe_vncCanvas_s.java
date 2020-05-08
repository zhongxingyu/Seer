 //
 //  Copyright (C) 2000 Tridia Corporation.  All Rights Reserved.
 //  Copyright (C) 1999 AT&T Laboratories Cambridge.  All Rights Reserved.
 //
 //  This is free software; you can redistribute it and/or modify
 //  it under the terms of the GNU General Public License as published by
 //  the Free Software Foundation; either version 2 of the License, or
 //  (at your option) any later version.
 //
 //  This software is distributed in the hope that it will be useful,
 //  but WITHOUT ANY WARRANTY; without even the implied warranty of
 //  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //  GNU General Public License for more details.
 //
 //  You should have received a copy of the GNU General Public License
 //  along with this software; if not, write to the Free Software
 //  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
 //  USA.
 //
 
 import java.awt.*;
 import java.awt.image.*;
 import java.io.*;
 import java.util.zip.*;
 
 
 //
 // vncCanvas is a subclass of Canvas which draws a VNC desktop on it.
 //
 
 class vncCanvas extends Canvas
 {
   vncviewer v;
   rfbProto rfb;
   ColorModel cm;
   Color[] colors;
   Image rawPixelsImage;
   animatedMemoryImageSource amis;
   byte[] pixels;
   byte[] zlibBuf;
   int zlibBufLen = 0;
   Inflater zlibInflater;
   Graphics sg, sg2;
   Image paintImage;
   Graphics pig, pig2;
   boolean needToResetClip;
 
   /* Tight decoder. */
   final static int tightZlibBufferSize = 512;
   Inflater[] tightInflaters;
 
   vncCanvas(vncviewer v1) throws IOException {
     v = v1;
     rfb = v.rfb;
 
     cm = new DirectColorModel(8, 7, (7 << 3), (3 << 6));
 
     rfb.writeSetPixelFormat(8, 8, false, true, 7, 7, 3, 0, 3, 6);
 
     colors = new Color[256];
 
     for (int i = 0; i < 256; i++) {
       colors[i] = new Color(cm.getRGB(i));
     }
 
     pixels = new byte[rfb.framebufferWidth * rfb.framebufferHeight];
 
     amis = new animatedMemoryImageSource(rfb.framebufferWidth,
 					 rfb.framebufferHeight, cm, pixels);
     rawPixelsImage = createImage(amis);
 
     paintImage = v.createImage(rfb.framebufferWidth, rfb.framebufferHeight);
 
     pig = paintImage.getGraphics();
 
     tightInflaters = new Inflater[4];
   }
 
   public Dimension preferredSize() {
     return new Dimension(rfb.framebufferWidth, rfb.framebufferHeight);
   }
 
   public Dimension minimumSize() {
     return new Dimension(rfb.framebufferWidth, rfb.framebufferHeight);
   }
 
   public void update(Graphics g) {
   }
 
   public void paint(Graphics g) {
     g.drawImage(paintImage, 0, 0, this);
   }
 
   //
   // processNormalProtocol() - executed by the rfbThread to deal with the
   // RFB socket.
   //
 
   public void processNormalProtocol() throws IOException {
 
     rfb.writeFramebufferUpdateRequest(0, 0, rfb.framebufferWidth,
 				      rfb.framebufferHeight, false);
 
     sg = getGraphics();
 
     needToResetClip = false;
 
     //
     // main dispatch loop
     //
 
     while (true) {
       int msgType = rfb.readServerMessageType();
 
       switch (msgType) {
       case rfb.FramebufferUpdate:
 	rfb.readFramebufferUpdate();
 
 	for (int i = 0; i < rfb.updateNRects; i++) {
 	  rfb.readFramebufferUpdateRectHdr();
 
 	  if (needToResetClip && (rfb.updateRectEncoding != rfb.EncodingRaw)) {
 	    try {
 	      sg.setClip(0, 0, rfb.framebufferWidth, rfb.framebufferHeight);
 	      pig.setClip(0, 0, rfb.framebufferWidth, rfb.framebufferHeight);
 	    } catch (NoSuchMethodError e) {
 	    }
 	    needToResetClip = false;
 	  }
 
 	  switch (rfb.updateRectEncoding) {
 
 	  case rfb.EncodingRaw:
 	    drawRawRect(rfb.updateRectX, rfb.updateRectY,
 			rfb.updateRectW, rfb.updateRectH);
 	    break;
 
 	  case rfb.EncodingCopyRect:
 	    rfb.readCopyRect();
 	    pig.copyArea(rfb.copyRectSrcX, rfb.copyRectSrcY,
 			 rfb.updateRectW, rfb.updateRectH,
 			 rfb.updateRectX - rfb.copyRectSrcX,
 			 rfb.updateRectY - rfb.copyRectSrcY);
 	    if (v.options.copyRectFast) {
 	      sg.copyArea(rfb.copyRectSrcX, rfb.copyRectSrcY,
 			  rfb.updateRectW, rfb.updateRectH,
 			  rfb.updateRectX - rfb.copyRectSrcX,
 			  rfb.updateRectY - rfb.copyRectSrcY);
 	    } else {
 	      sg.drawImage(paintImage, 0, 0, this);
 	    }
 	    break;
 
 	  case rfb.EncodingRRE:
 	  {
 	    int nSubrects = rfb.is.readInt();
 	    int bg = rfb.is.read();
 	    int pixel, x, y, w, h;
 	    sg.translate(rfb.updateRectX, rfb.updateRectY);
 	    sg.setColor(colors[bg]);
 	    sg.fillRect(0, 0, rfb.updateRectW, rfb.updateRectH);
 	    pig.translate(rfb.updateRectX, rfb.updateRectY);
 	    pig.setColor(colors[bg]);
 	    pig.fillRect(0, 0, rfb.updateRectW, rfb.updateRectH);
 	    for (int j = 0; j < nSubrects; j++) {
 	      pixel = rfb.is.read();
 	      x = rfb.is.readUnsignedShort();
 	      y = rfb.is.readUnsignedShort();
 	      w = rfb.is.readUnsignedShort();
 	      h = rfb.is.readUnsignedShort();
 	      sg.setColor(colors[pixel]);
 	      sg.fillRect(x, y, w, h);
 	      pig.setColor(colors[pixel]);
 	      pig.fillRect(x, y, w, h);
 	    }
 	    sg.translate(-rfb.updateRectX, -rfb.updateRectY);
 	    pig.translate(-rfb.updateRectX, -rfb.updateRectY);
 	    break;
 	  }
 
 	  case rfb.EncodingCoRRE:
 	  {
 	    int nSubrects = rfb.is.readInt();
 	    int bg = rfb.is.read();
 	    int pixel, x, y, w, h;
 
 	    sg.translate(rfb.updateRectX, rfb.updateRectY);
 	    sg.setColor(colors[bg]);
 	    sg.fillRect(0, 0, rfb.updateRectW, rfb.updateRectH);
 	    pig.translate(rfb.updateRectX, rfb.updateRectY);
 	    pig.setColor(colors[bg]);
 	    pig.fillRect(0, 0, rfb.updateRectW, rfb.updateRectH);
 
 	    for (int j = 0; j < nSubrects; j++) {
 	      pixel = rfb.is.read();
 	      x = rfb.is.read();
 	      y = rfb.is.read();
 	      w = rfb.is.read();
 	      h = rfb.is.read();
 
 	      sg.setColor(colors[pixel]);
 	      sg.fillRect(x, y, w, h);
 	      pig.setColor(colors[pixel]);
 	      pig.fillRect(x, y, w, h);
 	    }
 	    sg.translate(-rfb.updateRectX, -rfb.updateRectY);
 	    pig.translate(-rfb.updateRectX, -rfb.updateRectY);
 
 	    break;
 	  }
 
 	  case rfb.EncodingHextile:
 	  {
 	    int bg = 0, fg = 0, sx, sy, sw, sh;
 
 	    for (int ty = rfb.updateRectY;
 		 ty < rfb.updateRectY + rfb.updateRectH;
 		 ty += 16) {
 	      for (int tx = rfb.updateRectX;
 		   tx < rfb.updateRectX + rfb.updateRectW;
 		   tx += 16) {
 
 		int tw = 16, th = 16;
 
 		if (rfb.updateRectX + rfb.updateRectW - tx < 16)
 		  tw = rfb.updateRectX + rfb.updateRectW - tx;
 		if (rfb.updateRectY + rfb.updateRectH - ty < 16)
 		  th = rfb.updateRectY + rfb.updateRectH - ty;
 
 		int subencoding = rfb.is.read();
 
 		if ((subencoding & rfb.HextileRaw) != 0) {
 		  drawRawRect(tx, ty, tw, th);
 		  continue;
 		}
 
 		if (needToResetClip) {
 		  try {
 		    sg.setClip(0, 0,
 			       rfb.framebufferWidth, rfb.framebufferHeight);
 		    pig.setClip(0, 0,
 				rfb.framebufferWidth, rfb.framebufferHeight);
 		  } catch (NoSuchMethodError e) {
 		  }
 		  needToResetClip = false;
 		}
 
 		if ((subencoding & rfb.HextileBackgroundSpecified) != 0)
 		  bg = rfb.is.read();
 
 		sg.setColor(colors[bg]);
 		sg.fillRect(tx, ty, tw, th);
 		pig.setColor(colors[bg]);
 		pig.fillRect(tx, ty, tw, th);
 
 		if ((subencoding & rfb.HextileForegroundSpecified) != 0)
 		  fg = rfb.is.read();
 
 		if ((subencoding & rfb.HextileAnySubrects) == 0)
 		  continue;
 
 		int nSubrects = rfb.is.read();
 
 		sg.translate(tx, ty);
 		pig.translate(tx, ty);
 
 		if ((subencoding & rfb.HextileSubrectsColoured) != 0) {
 
 		  for (int j = 0; j < nSubrects; j++) {
 		    fg = rfb.is.read();
 		    int b1 = rfb.is.read();
 		    int b2 = rfb.is.read();
 		    sx = b1 >> 4;
                     sy = b1 & 0xf;
                     sw = (b2 >> 4) + 1;
 		    sh = (b2 & 0xf) + 1;
 
 		    sg.setColor(colors[fg]);
 		    sg.fillRect(sx, sy, sw, sh);
 		    pig.setColor(colors[fg]);
 		    pig.fillRect(sx, sy, sw, sh);
 		  }
 
 		} else {
 
 		  sg.setColor(colors[fg]);
 		  pig.setColor(colors[fg]);
 
 		  for (int j = 0; j < nSubrects; j++) {
 		    int b1 = rfb.is.read();
 		    int b2 = rfb.is.read();
 		    sx = b1 >> 4;
                     sy = b1 & 0xf;
                     sw = (b2 >> 4) + 1;
 		    sh = (b2 & 0xf) + 1;
 
 		    sg.fillRect(sx, sy, sw, sh);
 		    pig.fillRect(sx, sy, sw, sh);
 		  }
 		}
 
 		sg.translate(-tx, -ty);
 		pig.translate(-tx, -ty);
 	      }
 	    }
 	    break;
 	  }
 
 	  case rfb.EncodingZlib:
 	  {
 	    int nBytes = rfb.is.readInt();
 
             if (( zlibBuf == null ) ||
                 ( zlibBufLen < nBytes )) {
               zlibBuf = new byte[ nBytes * 2 ];
               zlibBufLen = nBytes * 2;
             }
 
             rfb.is.readFully( zlibBuf, 0, nBytes );
 
             if ( zlibInflater == null ) {
               zlibInflater = new Inflater();
             }
             zlibInflater.setInput( zlibBuf, 0, nBytes );
 
             drawZlibRect( rfb.updateRectX, rfb.updateRectY,
                           rfb.updateRectW, rfb.updateRectH );
 
 	    break;
 	  }
 
 	  case rfb.EncodingTight:
 	  {
             drawTightRect( rfb.updateRectX, rfb.updateRectY,
                            rfb.updateRectW, rfb.updateRectH );
 
 	    break;
 	  }
 
 	  default:
 	    throw new IOException("Unknown RFB rectangle encoding " +
 				  rfb.updateRectEncoding);
 	  }
 	}
 	rfb.writeFramebufferUpdateRequest(0, 0, rfb.framebufferWidth,
 					  rfb.framebufferHeight, true);
 	break;
 
       case rfb.SetColourMapEntries:
 	throw new IOException("Can't handle SetColourMapEntries message");
 
       case rfb.Bell:
 	System.out.print((char)7);
 	break;
 
       case rfb.ServerCutText:
 	String s = rfb.readServerCutText();
 	v.clipboard.setCutText(s);
 	break;
 
       default:
 	throw new IOException("Unknown RFB message type " + msgType);
       }
     }
   }
 
 
   //
   // Draw a raw rectangle.
   //
 
   void drawRawRect(int x, int y, int w, int h) throws IOException {
 
     if (v.options.drawEachPixelForRawRects) {
       for (int j = y; j < (y + h); j++) {
 	for (int k = x; k < (x + w); k++) {
 	  int pixel = rfb.is.read();
 	  sg.setColor(colors[pixel]);
 	  sg.fillRect(k, j, 1, 1);
 	  pig.setColor(colors[pixel]);
 	  pig.fillRect(k, j, 1, 1);
 	}
       }
       return;
     }
 
     for (int j = y; j < (y + h); j++) {
       rfb.is.readFully(pixels, j * rfb.framebufferWidth + x, w);
     }
 
     handleUpdatedPixels(x, y, w, h);
 
   }
 
 
   //
   // Draw a zlib rectangle.
   //
 
   void drawZlibRect(int x, int y, int w, int h) throws IOException {
 
     if (v.options.drawEachPixelForRawRects) {
       byte[] nextLine = new byte[ w ];
       Color myColor;
       for (int j = y; j < (y + h); j++) {
         try {
           zlibInflater.inflate( nextLine, 0, w );
         }
         catch( DataFormatException dfe ) {
           throw new IOException( dfe.toString());
         }
 	for (int k = x; k < (x + w); k++) {
           myColor = colors[ 0x000000ff & nextLine[ k - x ]];
 	  sg.setColor(myColor);
 	  sg.fillRect(k, j, 1, 1);
 	  pig.setColor(myColor);
 	  pig.fillRect(k, j, 1, 1);
 	}
       }
       return;
     }
 
     try {
       for (int j = y; j < (y + h); j++) {
         zlibInflater.inflate( pixels, j * rfb.framebufferWidth + x, w );
       }
     }
     catch( DataFormatException dfe ) {
       throw new IOException( dfe.toString());
     }
 
     handleUpdatedPixels(x, y, w, h);
 
   }
 
 
   //
   // Draw a tight rectangle.
   //
 
   void drawTightRect(int x, int y, int w, int h) throws IOException {
 
     int comp_ctl = rfb.is.readUnsignedByte();
 
     // Flush zlib streams if we are told by the server to do so.
     for (int stream_id = 0; stream_id < 4; stream_id++) {
       if ((comp_ctl & 1) != 0 && tightInflaters[stream_id] != null) {
         tightInflaters[stream_id] = null;
       }
       comp_ctl >>= 1;
     }
 
     // Handle solid rectangles.
     if (comp_ctl == rfb.TightFill) {
       int bg = rfb.is.readUnsignedByte();
       sg.setColor(colors[bg]);
       sg.fillRect(x, y, w, h);
       pig.setColor(colors[bg]);
       pig.fillRect(x, y, w, h);
       return;
     }
 
     // Read filter id and parameters.
     int filter_id;
     int numColors = 0;
     byte palette[] = new byte[2];
     if ((comp_ctl & rfb.TightExplicitFilter) != 0) {
       filter_id = rfb.is.readUnsignedByte();
       if (filter_id == rfb.TightFilterPalette) {
         numColors = rfb.is.readUnsignedByte() + 1; // Must be 2.
         if (numColors != 2) {
           throw new IOException("Incorrect tight palette size: " + numColors);
         }
         palette[0] = rfb.is.readByte();
         palette[1] = rfb.is.readByte();
       } else if (filter_id != TightFilterCopy) {
         throw new IOException("Incorrect tight filter id: " + filter_id);
       }
     }
 
     byte[] rawData = new byte[w * h];
     int stream_id = comp_ctl & 0x03;
 
     if (numColors == 2) {
       // Handle bi-color rectangles.
 
       int rowSize = (w + 7) / 8;
       int bicolorDataLen = h * rowSize;
       byte[] bicolorData = new byte[bicolorDataLen];
       if (bicolorDataLen < rfb.TightMinToCompress) {
         rfb.is.readFully(bicolorData, 0, bicolorDataLen);
       } else {
         int compressedDataLen = rfb.readCompactLen();
         byte[] compressedData = new byte[compressedDataLen];
         rfb.is.readFully(compressedData, 0, compressedDataLen);
 
         if (tightInflaters[stream_id] == null) {
           tightInflaters[stream_id] = new Inflater();
         }
         tightInflaters[stream_id].setInput(compressedData, 0,
                                            compressedDataLen);
         try {
           tightInflaters[stream_id].inflate(bicolorData, 0, bicolorDataLen);
         }
         catch(DataFormatException dfe) {
           throw new IOException(dfe.toString());
         }
       }
       tightPaletteFilter(rawData, bicolorData, w, h, palette);
     }
     else {
     // Handle full-color rectangles.
 
       if (w * h < rfb.TightMinToCompress) {
         rfb.is.readFully(rawData, 0, w * h);
       } else {
         int compressedDataLen = rfb.readCompactLen();
         byte[] compressedData = new byte[compressedDataLen];
         rfb.is.readFully(compressedData, 0, compressedDataLen);
 
         if (tightInflaters[stream_id] == null) {
           tightInflaters[stream_id] = new Inflater();
         }
         tightInflaters[stream_id].setInput(compressedData, 0,
                                            compressedDataLen);
         try {
           tightInflaters[stream_id].inflate(rawData, 0, w * h);
         }
         catch(DataFormatException dfe) {
           throw new IOException(dfe.toString());
         }
       }
     }
 
     // Draw rectangle.
     Color myColor;
     if (v.options.drawEachPixelForRawRects) {
       for (int l = 0; l < h; l++) {
         for (int k = 0; k < w; k++) {
           myColor = colors[ 0x000000ff & rawData[l * w + k] ];
           sg.setColor(myColor);
           sg.fillRect(x + k, y + l, 1, 1);
           pig.setColor(myColor);
           pig.fillRect(x + k, y + l, 1, 1);
         }
       }
       return;
     }
 
     for (int l = 0; l < h; l++) { // FIXME: Inefficient.
       for (int k = 0; k < w; k++) {
         pixels[(y + l) * rfb.framebufferWidth + (x + k)] = rawData[l * w + k];
       }
     }
     handleUpdatedPixels(x, y, w, h);
   }
 
 
   //
   // Display newly updated area of pixels.
   //
 
   void tightPaletteFilter(byte[] dst, byte[] src, int w, int h, byte[] pal) {
     int rowBytes = (w + 7) / 8;
     int x, y, b;
     for (y = 0; y < h; y++) {
       for (x = 0; x < w / 8; x++) {
 	for (b = 7; b >= 0; b--)
 	  dst[y*w+x*8+7-b] = pal[src[y*rowBytes+x] >> b & 1];
       }
       for (b = 7; b >= 8 - w % 8; b--) {
 	dst[y*w+x*8+7-b] = pal[src[y*rowBytes+x] >> b & 1];
       }
     }
   }
 
 
   //
   // Display newly updated area of pixels.
   //
 
   void handleUpdatedPixels(int x, int y, int w, int h) throws IOException {
 
     amis.newPixels(x, y, w, h);
 
     try {
       sg.setClip(x, y, w, h);
       pig.setClip(x, y, w, h);
       needToResetClip = true;
     } catch (NoSuchMethodError e) {
       sg2 = sg.create();
       sg.clipRect(x, y, w, h);
       pig2 = pig.create();
       pig.clipRect(x, y, w, h);
     }
 
     sg.drawImage(rawPixelsImage, 0, 0, this);
     pig.drawImage(rawPixelsImage, 0, 0, this);
 
     if (sg2 != null) {
       sg.dispose();    // reclaims resources more quickly
       sg = sg2;
       sg2 = null;
       pig.dispose();
       pig = pig2;
       pig2 = null;
     }
   }
 
 
   //
   // Handle events.
   //
   // Because of a "feature" in the AWT implementation over X, the vncCanvas
   // sometimes loses focus and the only way to get it back is to call
   // requestFocus() explicitly.  However we need to be careful when calling
   // requestFocus() on Windows or other click-to-type systems.  What we do is
   // call requestFocus() whenever there is mouse movement over the window,
   // AND the focus is already in the applet.
   //
 
   public boolean handleEvent(Event evt) {
     if ((rfb != null) && rfb.inNormalProtocol) {
       try {
 	switch (evt.id) {
 	case Event.MOUSE_MOVE:
 	case Event.MOUSE_DOWN:
 	case Event.MOUSE_DRAG:
 	case Event.MOUSE_UP:
 	  if (v.gotFocus) {
 	    requestFocus();
 	  }
 	  rfb.writePointerEvent(evt);
 	  break;
 	case Event.KEY_PRESS:
 	case Event.KEY_RELEASE:
 	case Event.KEY_ACTION:
 	case Event.KEY_ACTION_RELEASE:
 	  rfb.writeKeyEvent(evt);
 	  break;
 	}
       } catch (Exception e) {
 	e.printStackTrace();
       }
       return true;
     }
     return false;
   }
 }
