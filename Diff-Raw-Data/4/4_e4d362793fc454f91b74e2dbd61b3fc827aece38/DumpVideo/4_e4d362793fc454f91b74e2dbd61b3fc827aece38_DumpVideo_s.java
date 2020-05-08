 /* Copyright (C) <2009> Maik Merten <maikmerten@googlemail.com>
  * Copyright (C) <2004> Wim Taymans <wim@fluendo.com> (TheoraDec.java parts)
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
 package com.fluendo.examples;
 
 import com.fluendo.jheora.Comment;
 import com.fluendo.jheora.Info;
 import com.fluendo.jheora.State;
 import com.fluendo.jheora.YUVBuffer;
 import com.fluendo.utils.Debug;
 import com.fluendo.utils.MemUtils;
 import com.jcraft.jogg.Packet;
 import com.jcraft.jogg.Page;
 import com.jcraft.jogg.StreamState;
 import com.jcraft.jogg.SyncState;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 
 /**
  * This class borrows code from TheoraDec.java
  */
 public class DumpVideo {
 
     public static final Integer OK = new Integer(0);
     public static final Integer ERROR = new Integer(-5);
     private static final byte[] signature = {-128, 0x74, 0x68, 0x65, 0x6f, 0x72, 0x61};
 
     private class TheoraDecoder {
 
         private Info ti;
         private Comment tc;
         private State ts;
         private YUVBuffer yuv;
         private int packet;
         private boolean needKeyframe;
 
         public TheoraDecoder() {
             super();
             ti = new Info();
             tc = new Comment();
             ts = new State();
             yuv = new YUVBuffer();
         }
 
         public int takeHeader(Packet op) {
             int ret;
             byte header;
             ret = ti.decodeHeader(tc, op);
             header = op.packet_base[op.packet];
             if (header == -126) {
                 ts.decodeInit(ti);
             }
             return ret;
         }
 
         public boolean isHeader(Packet op) {
             return (op.packet_base[op.packet] & 0x80) == 0x80;
         }
 
         public boolean isKeyFrame(Packet op) {
             return ts.isKeyframe(op);
         }
 
         public Object decode(Packet op) {
 
             Object result = OK;
 
 
             if (packet < 3) {
                 //System.out.println ("decoding header");
                 if (takeHeader(op) < 0) {
                     // error case; not a theora header
                     Debug.log(Debug.ERROR, "does not contain Theora video data.");
                     return ERROR;
                 }
                 if (packet == 2) {
                     ts.decodeInit(ti);
 
                     Debug.log(Debug.INFO, "theora dimension: " + ti.width + "x" + ti.height);
                     if (ti.aspect_denominator == 0) {
                         ti.aspect_numerator = 1;
                         ti.aspect_denominator = 1;
                     }
                     Debug.log(Debug.INFO, "theora offset: " + ti.offset_x + "," + ti.offset_y);
                     Debug.log(Debug.INFO, "theora frame: " + ti.frame_width + "," + ti.frame_height);
                     Debug.log(Debug.INFO, "theora aspect: " + ti.aspect_numerator + "/" + ti.aspect_denominator);
                     Debug.log(Debug.INFO, "theora framerate: " + ti.fps_numerator + "/" + ti.fps_denominator);
 
                 }
                 packet++;
 
                 return OK;
             } else {
                 if ((op.packet_base[op.packet] & 0x80) == 0x80) {
                     Debug.log(Debug.INFO, "ignoring header");
                     return OK;
                 }
                 if (needKeyframe && ts.isKeyframe(op)) {
                     needKeyframe = false;
                 }
 
 
                 if (!needKeyframe) {
                     try {
                         if (ts.decodePacketin(op) != 0) {
                             Debug.log(Debug.ERROR, "Bad Theora packet. Most likely not fatal, hoping for better luck next packet.");
                         }
                         if (ts.decodeYUVout(yuv) != 0) {
                             Debug.log(Debug.ERROR, "Error getting the picture.");
                             return ERROR;
                         }
                         return yuv.getObject(ti.offset_x, ti.offset_y, ti.frame_width, ti.frame_height);
                     } catch (Exception e) {
                         e.printStackTrace();
                         result = ERROR;
                     }
                 } else {
                     result = OK;
                 }
             }
             packet++;
 
             return result;
         }
     }
 
     private class YUVWriter {
 
         private OutputStream os;
         private boolean wroteHeader = false;
         private byte[] ybytes;
         private byte[] uvbytes;
         private boolean raw;
 
         public YUVWriter(File outfile, boolean raw) {
             this.raw = raw;
             try {
                 os = new FileOutputStream(outfile);
             } catch (FileNotFoundException ex) {
                 ex.printStackTrace();
             }
         }
 
         public void writeYUVFrame(Info ti, YUVBuffer yuv) {
             try {
                 if (!raw) {
                     if (!wroteHeader) {
                         String headerstring = "YUV4MPEG2 W" + ti.width + " H" + ti.height + " F" + ti.fps_numerator + ":" + ti.fps_denominator + " Ip A" + ti.aspect_numerator + ":" + ti.aspect_denominator + "\n";
                         os.write(headerstring.getBytes());
                         wroteHeader = true;
                     }
                     os.write("FRAME\n".getBytes());
                 }
 
                 if (ybytes == null || ybytes.length != yuv.y_width * yuv.y_height) {
                     ybytes = new byte[yuv.y_width * yuv.y_height];
                 }
 
                 int offset = 0;
                 for (int i = 0; i < yuv.y_height; ++i) {
                     int start = yuv.y_offset + (i * yuv.y_stride);
                     for (int j = start; j < start + yuv.y_width; ++j) {
                         ybytes[offset++] = (byte) yuv.data[j];
                     }
 
                 }
                 os.write(ybytes);
 
                 if (uvbytes == null || uvbytes.length != yuv.uv_width * yuv.uv_height) {
                     uvbytes = new byte[yuv.uv_width * yuv.uv_height];
                 }
 
                 offset = 0;
                 for (int i = 0; i < yuv.uv_height; ++i) {
                     int start = yuv.u_offset + (i * yuv.uv_stride);
                     for (int j = start; j < start + yuv.uv_width; ++j) {
                         uvbytes[offset++] = (byte) yuv.data[j];
                     }
                 }
                 os.write(uvbytes);
 
                 offset = 0;
                 for (int i = 0; i < yuv.uv_height; ++i) {
                     int start = yuv.v_offset + (i * yuv.uv_stride);
                     for (int j = start; j < start + yuv.uv_width; ++j) {
                         uvbytes[offset++] = (byte) yuv.data[j];
                     }
                 }
                 os.write(uvbytes);
 
             } catch (IOException ex) {
                 ex.printStackTrace();
             }
         }
     }
 
     public boolean isTheora(Packet op) {
         return typeFind(op.packet_base, op.packet, op.bytes) > 0;
     }
 
     public int typeFind(byte[] data, int offset, int length) {
         if (MemUtils.startsWith(data, offset, length, signature)) {
             return 10;
         }
         return -1;
     }
 
     public void dumpVideo(File videofile, List outfiles, boolean raw) throws IOException {
        InputStream is = videofile.toURI().toURL().openStream();
 
         SyncState oy = new SyncState();
         Page og = new Page();
         Packet op = new Packet();
         byte[] buf = new byte[512];
 
         Map streamstates = new HashMap();
         Map theoradecoders = new HashMap();
         Map yuvwriters = new HashMap();
         Set hasdecoder = new HashSet();
 
         int frames = 0;
 
         int read = is.read(buf);
         while (read > 0) {
             int offset = oy.buffer(read);
             java.lang.System.arraycopy(buf, 0, oy.data, offset, read);
             oy.wrote(read);
 
             while (oy.pageout(og) == 1) {
 
                 Integer serialno = new Integer(og.serialno());
 
                 StreamState state = (StreamState) streamstates.get(serialno);
                 if (state == null) {
                     state = new StreamState();
                     state.init(serialno.intValue());
                     streamstates.put(serialno, state);
                     Debug.info("created StreamState for stream no. " + og.serialno());
                 }
 
                 state.pagein(og);
 
                 while (state.packetout(op) == 1) {
 
                     if (!(hasdecoder.contains(serialno)) && isTheora(op)) {
 
                         TheoraDecoder theoradec = (TheoraDecoder) theoradecoders.get(serialno);
                         if (theoradec == null) {
                             theoradec = new TheoraDecoder();
                             theoradecoders.put(serialno, theoradec);
                             hasdecoder.add(serialno);
                         }
 
                         Debug.info("is Theora: " + serialno);
                     }
 
                     TheoraDecoder theoradec = (TheoraDecoder) theoradecoders.get(serialno);
 
                     if (theoradec != null) {
                         Object result = theoradec.decode(op);
                         if (result instanceof YUVBuffer) {
                             Debug.info("got frame " + ++frames);
 
                             YUVWriter yuvwriter = (YUVWriter) yuvwriters.get(serialno);
                             if (yuvwriter == null && !outfiles.isEmpty()) {
                                 yuvwriter = new YUVWriter((File) outfiles.get(0), raw);
                                 yuvwriters.put(serialno, yuvwriter);
                                 outfiles.remove(0);
                             }
 
                             if (yuvwriter != null) {
                                 YUVBuffer yuvbuf = (YUVBuffer) result;
                                 yuvwriter.writeYUVFrame(theoradec.ti, yuvbuf);
                             }
 
                         }
                     }
                 }
             }
 
             read = is.read(buf);
         }
 
     }
 
     public static void main(String[] args) throws IOException {
 
         if (args.length < 2) {
             System.err.println("usage: DumpVideo <videofile> <outfile_1> ... <outfile_n> [--raw>]");
             System.exit(1);
         }
 
         boolean raw = false;
         File infile = new File(args[0]);
 
         List outfiles = new LinkedList();
         for (int i = 1; i < args.length; ++i) {
             if(args[i].equals("--raw")) {
                 raw = true;
                 break;
             }
             outfiles.add(new File(args[i]));
         }
 
         DumpVideo dv = new DumpVideo();
         dv.dumpVideo(infile, outfiles, raw);
 
     }
 }
