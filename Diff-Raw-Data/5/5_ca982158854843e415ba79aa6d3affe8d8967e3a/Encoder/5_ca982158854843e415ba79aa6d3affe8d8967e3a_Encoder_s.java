 package jp.gr.java_conf.neko_daisuki.fsyscall;
 
 import java.util.Arrays;
 
 public class Encoder {
 
     public static byte[] encodeLong(long n) {
         if (n == 0) {
             return new byte[] { 0 };
         }
 
         byte[] buffer = new byte[64 / 7 + 1];
         long m = n;
         int pos = 0;
        while (0 < m) {
             long l = m >>> 7;
             buffer[pos] = (byte)((m & 0x7f) + (0 < l ? 0x80 : 0));
 
             m = l;
             pos++;
         }
 
         return Arrays.copyOf(buffer, pos);
     }
 
     public static byte[] encodeInteger(int n) {
         if (n == 0) {
             return new byte[] { 0 };
         }
 
         byte[] buffer = new byte[32 / 7 + 1];
         int m = n;
         int pos = 0;
        while (0 < m) {
             int l = m >>> 7;
             buffer[pos] = (byte)((m & 0x7f) + (0 < l ? 0x80 : 0));
 
             m = l;
             pos++;
         }
 
         return Arrays.copyOf(buffer, pos);
     }
 
     public static byte[] encode(Errno errno) {
         return encodeInteger(errno.toInteger());
     }
 }
 
 /**
  * vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
  */
