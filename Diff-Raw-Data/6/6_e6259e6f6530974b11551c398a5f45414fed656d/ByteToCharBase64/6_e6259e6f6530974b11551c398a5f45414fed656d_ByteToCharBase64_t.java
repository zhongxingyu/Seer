 package uk.org.ponder.stringutil;
 
 /** This class encodes supplied byte data as characters in the Base64 system.
  * It accepts data as byte arrays and native types, which are written direct
  * to a supplied CharWrap as plain ASCII characters.
  */
 public class ByteToCharBase64 {
   /** This array maps the characters to their 6-bit values */
   public final static char pem_array[] = {
  //  0   1   2   3   4   5   6   7
     'A','B','C','D','E','F','G','H', // 0
     'I','J','K','L','M','N','O','P', // 1
     'Q','R','S','T','U','V','W','X', // 2
     'Y','Z','a','b','c','d','e','f', // 3
     'g','h','i','j','k','l','m','n', // 4
     'o','p','q','r','s','t','u','v', // 5
     'w','x','y','z','0','1','2','3', // 6
    '4','5','6','7','8','9','-','_'  // 7
     };
// NB !!!!! characters + and / have been replaced with - and _ for
   // file and URL safety.
   //  public static void getOutputWidth(int inputbytes) {
   //    }
 
   /** Writes the supplied byte data to the supplied CharWrap in Base64-encoded
    * form.
    * @param out The CharWrap to receive the encoded characters.
    * @param data A byte array holding the data to be encoded.
    * @param offset The offset within the byte array of the data to be encoded.
    * @param len The length of the data to be encoded.
    * @param pad <code>true</code> if the output data should be right-padded 
    * with the <code>A</code> character. NB, the Base64 standard is actually
    * to right-pad with the <code>=</code> character, look into this.
    */
   public static void writeBytes(CharWrap out, byte data[], int offset, int len,
 				boolean pad) {
     byte b0, b1, b2;
     int index;
     while (len > 0) {
       // not correct! This should be right-aligned!!!
       int thislen = len % 3;
       if (thislen == 1) { // 1 byte - send out aaaaaabb 0000
 	b0 = data[offset];
 	index = (b0 >>> 2) & 0x3F;
 	if (pad || index != 0) {out.append(pem_array[index]); pad = true;}
 	index = (b0 << 4) & 0x30;
 	if (pad || index != 0) {out.append(pem_array[index]); pad = true;}
 	} 
       else if (thislen == 2) { // 2 byte - aaaaaabb bbbbcccc 00
 	b0 = data[offset];
 	b1 = data[offset+1];
 	index = (b0 >>> 2) & 0x3F;
 	if (pad || index != 0) {out.append(pem_array[index]); pad = true;}
 	index = ((b0 << 4) & 0x30) + ((b1 >>> 4) & 0xf);
 	if (pad || index != 0) {out.append(pem_array[index]); pad = true;}
 	index = (b1 << 2) & 0x3c;
 	if (pad || index != 0) {out.append(pem_array[index]); pad = true;}
 	} 
       else { // 3 byte - aaaaaabb bbbbcccc ccdddddd
 	b0 = data[offset];
 	b1 = data[offset+1];
 	b2 = data[offset+2];
 	index = (b0 >>> 2) & 0x3F;
 	if (pad || index != 0) {out.append(pem_array[index]); pad = true;}
 	index = ((b0 << 4) & 0x30) + ((b1 >>> 4) & 0xf);
 	if (pad || index != 0) {out.append(pem_array[index]); pad = true;}
 	index = ((b1 << 2) & 0x3c) + ((b2 >>> 6) & 0x3);
 	if (pad || index != 0) {out.append(pem_array[index]); pad = true;}
 	index = b2 & 0x3F;
 	if (pad || index != 0) {out.append(pem_array[index]); pad = true;}
 	}
       len -= 3; offset += 3;
       }
     }
   /** Writes the specified <code>long</code> encoded as Base64 characters
    * to the supplied CharWrap. The <code>long</code> is interpreted as a
    * big-endian byte sequence in the usual sane way.
    * @param out The CharWrap to receive the encoded characters.
    * @param towrite The <code>long</code> to be encoded as Base64.
    * @param <code>true</code> if the characters are to be right-padded
    * with <code>A</code>
    */
 
   public static void writeLong(CharWrap out, long towrite, boolean pad) {
     // up to 11 bytes
     System.out.println("writeLong: "+ towrite);
     for (int bits = 60; bits >= 0; bits -=6) {
       int index = ((int)(towrite >> bits)) & 0x3f;
       System.out.print(index + " ");
       if (pad || index != 0) {out.append(pem_array[index]); pad = true;}
       }
     }
 
   /** Writes the specified <code>int</code> encoded as Base64 characters
    * to the supplied CharWrap. The <code>int</code> is interpreted as a
    * big-endian byte sequence in the usual sane way.
    * @param out The CharWrap to receive the encoded characters.
    * @param towrite The <code>int</code> to be encoded as Base64.
    * @param <code>true</code> if the characters are to be right-padded
    * with <code>A</code>
    */
 
   public static void writeInt(CharWrap out, int towrite, boolean pad) {
     // up to 6 bytes: aabbbbbb ccccccdd ddddeeee eeffffff
     for (int bits = 30; bits >= 0; bits -=6) {
       int index = (towrite >> bits) & 0x3f;
       if (pad || index != 0) {out.append(pem_array[index]); pad = true;}
       }
     }
   }
