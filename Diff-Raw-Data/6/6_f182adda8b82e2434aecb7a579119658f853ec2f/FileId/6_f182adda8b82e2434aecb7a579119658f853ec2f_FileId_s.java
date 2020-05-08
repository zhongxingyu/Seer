 package com.codingstory.polaris.indexing;
 
 import com.google.common.base.Preconditions;
 import org.apache.commons.codec.DecoderException;
 import org.apache.commons.codec.binary.Hex;
 
 import java.util.Arrays;
import java.util.Objects;
 
 /**
  * An unique identifier of a source file, which is the SHA1 hash of file content.
  */
 public class FileId {
     private static final int LENGTH = 20;
     private final byte[] value;
 
     public FileId(byte[] value) {
         Preconditions.checkNotNull(value);
         Preconditions.checkArgument(value.length == LENGTH);
         this.value = Arrays.copyOf(value, value.length);
     }
 
     public FileId(String value) {
         Preconditions.checkNotNull(value);
         Preconditions.checkArgument(value.length() == LENGTH * 2);
         try {
             this.value = Hex.decodeHex(value.toCharArray());
         } catch (DecoderException e) {
             throw new IllegalArgumentException(e);
         }
     }
 
     public byte[] getValue() {
         return Arrays.copyOf(value, value.length);
     }
 
     public String getValueAsString() {
         return Hex.encodeHexString(value);
     }
 
     @Override
     public int hashCode() {
         return Objects.hashCode(value);
     }
 
     @Override
     public boolean equals(Object obj) {
        return obj instanceof FileId && Objects.equals(value, ((FileId) obj).value);
     }
 }
