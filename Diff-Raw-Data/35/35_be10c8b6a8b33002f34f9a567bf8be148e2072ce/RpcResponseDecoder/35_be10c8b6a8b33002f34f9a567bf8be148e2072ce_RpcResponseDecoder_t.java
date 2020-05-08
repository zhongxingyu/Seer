 /*
  * Copyright 2012-2013 The Open Source Electronic Health Record Agent
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.osehra.vista.camel.rpc.codec;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.handler.codec.frame.CorruptedFrameException;
 import org.jboss.netty.handler.codec.frame.FrameDecoder;
 import org.osehra.vista.camel.rpc.RpcConstants;
 import org.osehra.vista.camel.rpc.RpcResponse;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 public class RpcResponseDecoder extends FrameDecoder {
 
     private final static Logger LOG = LoggerFactory.getLogger(RpcResponseDecoder.class);
 
     @Override
     protected Object decode(final ChannelHandlerContext ctx,
             final Channel channel, final ChannelBuffer buffer) throws Exception {
 
         int l = framePrefixLen(buffer);
         LOG.trace("Skipping frame buffer of {} bytes", l);
         buffer.skipBytes(l);

        RpcResponse response = new RpcResponse();
        if (buffer.readableBytes() == 0) {
            return response;    // empty frame
        }

         boolean done = false;
         while (!done) {
             final int eol = findEndOfLine(buffer);
             final int len = eol >= 0 ? eol - buffer.readerIndex() : buffer.readableBytes();
             final int dlen = eol < 0 ? 0 : buffer.getByte(eol) == '\r' ? 2 : 1;
 
             final ChannelBuffer frame;
             try {
                 frame = extractFrame(buffer, buffer.readerIndex(), len);
             } finally {
                 buffer.skipBytes(len + dlen);
             }
 
             response.row(parseRow(frame));
             done = eol < 0;
         }
         
         LOG.info("GOT: ", response.getContent().toString());
         return response;
     }
 
     private int framePrefixLen(ChannelBuffer buffer) {
         final int r = buffer.readerIndex();
         final int w = buffer.writerIndex();
         for (int i = r; i < w; i ++) {
             final byte b = buffer.getByte(i);
             if (b != '\0') {
                 return i - r;
             }
         }
        return w - r;
     }
 
     private List<String> parseRow(final ChannelBuffer buffer) {
         List<String> fields = new ArrayList<String>();
         boolean done = false;
         while (!done) {
             final int eof = findEndOfField(buffer);
             final int len = eof >= 0 ? eof - buffer.readerIndex() : buffer.readableBytes();
             final int dlen = eof < 0 ? 0 : 1;
 
             final ChannelBuffer frame;
             try {
                 frame = extractFrame(buffer, buffer.readerIndex(), len);
             } finally {
                 buffer.skipBytes(len + dlen);
             }
 
             fields.add(frame.toString(RpcCodecUtils.DEF_CHARSET));
             done = eof < 0;
         }
         return fields;
     }
 
     private static int findEndOfLine(final ChannelBuffer buffer) {
         final int n = buffer.writerIndex();
         for (int i = buffer.readerIndex(); i < n; i ++) {
             final byte b = buffer.getByte(i);
             if (b == '\n') {
                 return i;
             } else if (b == '\r' && i < n - 1 && buffer.getByte(i + 1) == '\n') {
                 return i;  // \r\n
             }
         }
         return -1;  // Not found.
     }
 
     private static int findEndOfField(final ChannelBuffer buffer) {
         boolean skip = false;
         final int n = buffer.writerIndex();
         for (int i = buffer.readerIndex(); i < n; i ++) {
             final byte b = buffer.getByte(i);
             if (b == RpcConstants.FIELD_DELIM && !skip) {
                 return i;
             } else if (b == '\'') {
                 skip = !skip;
             } 
         }
         return -1;
     }
 
 }
 
