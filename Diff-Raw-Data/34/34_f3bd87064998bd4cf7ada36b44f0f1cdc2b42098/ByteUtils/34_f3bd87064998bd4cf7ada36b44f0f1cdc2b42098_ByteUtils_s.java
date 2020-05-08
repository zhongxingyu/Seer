 package jpower.core.utils;
 
 public class ByteUtils {
     public static byte[] reverse(byte[] original) {
         byte[] reversed = new byte[original.length];
        for (int i = 0, originalLength = original.length; i < originalLength; i++) {
            byte b = original[i];
            reversed[i] = b;
         }
         return reversed;
     }
 }
