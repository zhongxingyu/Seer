 /*
  * Made by Wannes 'W' De Smet
  * (c) 2011 Wannes De Smet
  * All rights reserved.
  * 
  */
 package net.wgr.xenmaster.pool;
 
 import java.io.UnsupportedEncodingException;
 import java.nio.ByteBuffer;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.log4j.Logger;
 
 /**
  * Prefix     Alive - \ / - Master
  * +----------------+ | | - - - - - - - - - - - - - - +
  * +1011010111010000+ 1 1
  * @param packet 
  */
 
 /**
  * 
  * @created Nov 6, 2011
  * @author double-u
  */
 public class XMPacket {
     // Alive packet, does not require further processing
 
     public boolean alive;
     public boolean master;
     public String contents;
 
     public byte[] getContents() {
         ByteBuffer bb = ByteBuffer.allocate(1024);
         bb.put(Integer.valueOf(0b10110101).byteValue());
         bb.put(Integer.valueOf(0b1101_0000).byteValue());
 
        short flags = 0;
         flags += (alive ? 0b1 : 0b0);
         flags += (master ? 0b10 : 0b0);
 
        bb.putShort(flags);
         if (contents != null) {
             try {
                 bb.put(contents.getBytes("UTF-8"));
             } catch (UnsupportedEncodingException ex) {
                 Logger.getLogger(getClass()).error("UTF-8 not recognized, file a bug please", ex);
             }
         }
         return bb.array();
     }
 
     public void setContents(byte[] data, int length) {
         if (length < 2) {
             return;
         }
 
         // 101101011101 ~ 29/09  = birthday of initial commit
         if (!(data[0] == 0b10110101 && data[1] == 0b1101_0000)) {
             Logger.getLogger(getClass()).debug("Illegal prefix for packet");
             return;
         }
 
        short flags = data[2];
        alive = (flags & 0b1) == 1;
        master = (flags & 0b01) == 1;
 
         contents = new String(ArrayUtils.subarray(data, 3, length));
     }
 }
