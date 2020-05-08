 /*
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License version 2
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, see <http://www.gnu.org/licenses/>
  *
  */
 
 /**
  *
  */
 package net.FriendsUnited.Util;
 
 import java.nio.charset.Charset;
 import java.util.Vector;
 import java.util.zip.CRC32;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This class defines the Byte Order !
  * @author Lars P&ouml;tter
  * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
  */
 public class ByteConverter
 {
     private Logger log = LoggerFactory.getLogger(this.getClass().getName());
 
     private Vector<Byte> data;
     private int curPos;
 
     /**
      *
      */
     public ByteConverter()
     {
         data = new Vector<Byte>();
         curPos = 0;
     }
 
     /**
      *
      * @param payload
      */
     public ByteConverter(final byte[] payload)
     {
         data = new Vector<Byte>();
         add(payload);
         curPos = 0;
     }
 
     /** Returns the amount of Bytes in this Buffer.
      *
      * @return amount of bytes in this Buffer
      */
     public final int size()
     {
         return data.size();
     }
 
     public final boolean hasMoreBytes()
     {
         if(curPos < data.size())
         {
             return true;
         }
         else
         {
             return false;
         }
     }
 
     private short toUnsigned(final byte b)
     {
         if(b < 0)
         {
              return (short)(b + 256);
         }
         else
         {
             return b;
         }
     }
 
     /**
      *
      * @param b
      */
     public final void add(final byte[] b)
     {
         if(null != b)
         {
             add(b, b.length);
         }
     }
 
     public final void removeBytes(final int startPos, final int amount)
     {
         final int size = data.size();
         try
         {
             for(int i = 0; i < amount; i++)
             {
                 data.remove(startPos);
             }
         }
         catch(final ArrayIndexOutOfBoundsException e)
         {
             log.error("Tried to remove too much bytes! (" + startPos + ":" + amount + ":" + size + ")" );
         }
     }
 
     /**
     *
     * @param b
     */
    public final void add(final byte b)
    {
        data.add(b);
    }
 
     /**
      *
      * @return byte
      */
     public final byte getByte()
     {
         if(data.size() < (curPos + 1))
         {
             return 0;
         }
         final byte res = data.get(curPos);
         curPos = curPos + 1;
         return res;
     }
 
     /**
      *
      * @return
      */
     public final byte readByte(final int pos)
     {
         if(data.size() < (pos + 1))
         {
             return 0;
         }
         final byte res = data.get(pos);
         return res;
     }
 
     /**
     *
     * @param array
     * @param numRead
     */
    public final void add(final byte[] array, final int numRead)
    {
        if(null != array)
        {
            for(int i = 0; i < numRead; i++)
            {
                data.add(array[i]);
            }
        }
    }
 
    /**
    *
    * @param length
    * @return byte array of length
    */
   public final byte[] getByteArray(final int length, final int pos)
   {
       if(data.size() < (pos + length))
       {
          return null;
       }
       final byte[] res = new byte[length];
       for(int i = 0; i < length; i++)
       {
           res[i] = data.get(pos + i);
       }
       return res;
   }
 
     /**
      *
      * @param length
      * @return byte array of length
      */
     public final byte[] getByteArray(final int length)
     {
         if(data.size() < (curPos + length))
         {
            return null;
         }
         final byte[] res = new byte[length];
         for(int i = 0; i < length; i++)
         {
             res[i] = data.get(curPos + i);
         }
         curPos = curPos + length;
         return res;
     }
 
     /**
      *
      * @param s
      */
     public final void add(final short s)
     {
         Byte b = Byte.valueOf((byte)(s&0xff));
         data.add(b);
         b = Byte.valueOf((byte)((short) (s>>>8)&0xff));
         data.add(b);
     }
 
     /**
      *
      * @return short
      */
     public final short getShort()
     {
         if(data.size() < (curPos + 2))
         {
             return 0;
         }
         short res = toUnsigned(data.get(curPos));
         res = (short) (res + (256 * toUnsigned(data.get(curPos + 1))));
         curPos = curPos + 2;
         return res;
     }
 
     /** reads a short without removing it from the buffer.
     *
     * @return short
     */
    public final short readShort(final int pos)
    {
        if(data.size() < (2 + pos))
        {
            return 0;
        }
        short res = toUnsigned(data.get(pos));
        res = (short) (res + (256 * toUnsigned(data.get(pos + 1))));
        return res;
    }
 
     /**
      *
      * @param i
      */
     public final void add(final int i)
     {
         Byte b = Byte.valueOf((byte)(i&0xff));
         data.add(b);
         b = Byte.valueOf((byte)((i>>>8)&0xff));
         data.add(b);
         b = Byte.valueOf((byte)((i>>>16)&0xff));
         data.add(b);
         b = Byte.valueOf((byte)((i>>>24)&0xff));
         data.add(b);
     }
 
     /**
      *
      * @return
      */
     public final int getInt()
     {
         if(data.size() < (curPos + 4))
         {
             return 0;
         }
         int res = toUnsigned(data.get(curPos));
         res = res + (256 * toUnsigned(data.get(curPos + 1)));
         res = res + (256 * 256 * toUnsigned(data.get(curPos + 2)));
         res = res + (256 * 256 * 256 * toUnsigned(data.get(curPos + 3)));
         curPos = curPos + 4;
         return res;
     }
 
     /** reads an 4 byte Integer from the Buffer without removing it.
     *
     * @return
     */
    public final int readInt(final int pos)
    {
        if(data.size() < (pos + 4))
        {
            log.error("read outside array !!!!");
            return 0;
        }
        int res = toUnsigned(data.get(pos));
        res = res + (256 * toUnsigned(data.get(pos + 1)));
        res = res + (256 * 256 * toUnsigned(data.get(pos + 2)));
        res = res + (256 * 256 * 256 * toUnsigned(data.get(pos + 3)));
        log.debug("(" + pos + ")p:" + data.get(pos) + "," +   data.get(pos + 1) + "," +
                data.get(pos + 2) + "," +
                data.get(pos + 3));
        return res;
    }
 
     /**
      *
      * @param l
      */
     public final void add(final long l)
     {
         Byte b = Byte.valueOf((byte)(l&0xff));
         data.add(b);
         b = Byte.valueOf((byte)((l>>>8)&0xff));
         data.add(b);
         b = Byte.valueOf((byte)((l>>>16)&0xff));
         data.add(b);
         b = Byte.valueOf((byte)((l>>>24)&0xff));
         data.add(b);
         b = Byte.valueOf((byte)((l>>>32)&0xff));
         data.add(b);
         b = Byte.valueOf((byte)((l>>>40)&0xff));
         data.add(b);
         b = Byte.valueOf((byte)((l>>>48)&0xff));
         data.add(b);
         b = Byte.valueOf((byte)(l>>>56&0xff));
         data.add(b);
     }
 
     /**
      *
      * @return
      */
     public final long getLong()
     {
         if(data.size() < (curPos + 8))
         {
             return 0;
         }
         long res = toUnsigned(data.get(curPos));
         res = res + (256L * toUnsigned(data.get(curPos + 1)));
         res = res + (256L * 256 * toUnsigned(data.get(curPos + 2)));
         res = res + (256L * 256 * 256 * toUnsigned(data.get(curPos + 3)));
         res = res + (256L * 256 * 256 * 256 * toUnsigned(data.get(curPos + 4)));
         res = res + (256L * 256 * 256 * 256 * 256 * toUnsigned(data.get(curPos + 5)));
         res = res + (256L * 256 * 256 * 256 * 256 * 256 * toUnsigned(data.get(curPos + 6)));
         res = res + (256L * 256 * 256 * 256 * 256 * 256 * 256 * toUnsigned(data.get(curPos + 7)));
         curPos = curPos + 8;
         return res;
     }
 
     /**
      *
      * @param text
      */
     public final void add(final String text)
     {
         final byte[] str = text.getBytes(Charset.forName("UTF-8"));
         add(str.length);
         add(str);
     }
 
     public static final int getStoredLengthForString(final String text)
     {
         final byte[] str = text.getBytes(Charset.forName("UTF-8"));
         return 4 + str.length;
     }
 
     /**
      *
      * @return byte array of length
      */
     public final String getString()
     {
         final int length = getInt();
         final byte[] buf = getByteArray(length);
         final String res = new String(buf, Charset.forName("UTF-8"));
         return res;
     }
 
 
     /**
      *
      * @return
      */
     public final byte[] toByteArray()
     {
         final byte[] b = new byte[data.size()];
         for(int i = 0; i < data.size(); i++)
         {
             b[i] = data.get(i);
         }
         return b;
     }
 
     /** test if the last 4 bytes of this buffer contain the valid checksum for this Buffer.
     *
     */
     public final boolean testCheckSum()
     {
         final int PacketLength = data.size();
         log.debug("PacketLength = " + PacketLength);
         if(PacketLength < 4)
         {
             // last 4 bytes are checksum
             log.error("Length error !(too short)");
             return false;
         }
         final CRC32 crc = new CRC32();
         crc.reset();
         for(int i = 0; i < (PacketLength - 4); i++)
         {
             crc.update(data.get(i));
         }
         final int calculatedSum = (int)crc.getValue();
         final int foundSum = readInt(PacketLength - 4);
 
         if(foundSum == calculatedSum)
         {
             return true;
         }
         else
         {
             log.error("Packet : " + Tool.fromByteBufferToHexString(this.toByteArray()));
             log.error(String.format("Checksum error! (expected : %d(0x%x) (in Packet %d(0x%x)",
                     calculatedSum, calculatedSum, foundSum, foundSum));
             return false;
         }
     }
 
    /**
     *
     */
     public final void addChecksum()
     {
         final CRC32 crc = new CRC32();
         crc.reset();
         for(int i = 0; i < data.size(); i++)
         {
             crc.update(data.get(i));
         }
         final long sum = crc.getValue();
         Byte b = Byte.valueOf((byte)(sum&0xff));
         data.add(b);
         b = Byte.valueOf((byte)((sum>>>8)&0xff));
         data.add(b);
         b = Byte.valueOf((byte)((sum>>>16)&0xff));
         data.add(b);
         b = Byte.valueOf((byte)((sum>>>24)&0xff));
         data.add(b);
     }
 
 }
