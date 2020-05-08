 package com.kii.qb.savable_messagedigest;
 
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.DataInputStream;
 
 public final class MD5State
 {
     final int state[] = new int[4];
     long count;
     final byte buffer[] = new byte[64];
 
     public MD5State() {
         this.state[0] = 0x67452301;
        this.state[1] = 0xefcdab89;
        this.state[2] = 0X98badcfe;
         this.state[3] = 0x10325476;
     }
 
     public MD5State(MD5State src) {
         System.arraycopy(src.state, 0, this.state, 0, this.state.length);
         this.count = src.count;
         System.arraycopy(src.buffer, 0, this.buffer, 0, this.buffer.length);
     }
 
     public void save(DataOutputStream output) throws IOException
     {
         for (int i = 0; i < this.state.length; ++i) {
             output.writeInt(this.state[i]);
         }
         output.writeLong(count);
         output.write(this.buffer, 0, this.buffer.length);
     }
 
     public void load(DataInputStream input) throws IOException
     {
         for (int i = 0; i < this.state.length; ++i) {
             this.state[i] = input.readInt();
         }
         this.count = input.readLong();
         if (input.read(this.buffer, 0, this.buffer.length) !=
                 this.buffer.length)
             throw new IOException("Too short to load buffer");
     }
 
     public byte[] getStateBytes() {
         return Utils.getBytes(this.state);
     }
 
     public int getOffset() {
         return ((int)this.count) & 0x3F;
     }
 }
