 package org.esidoc.core.utils.io;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 
 /**
  * Subclass of ByteArrayOutputStream that allows creation of a ByteArrayInputStream directly without creating a copy of
  * the byte[].
  * <p/>
  * Also, on "toByteArray()" it truncates it's buffer to the current size and returns the new buffer directly.  Multiple
  * calls to toByteArray() will return the exact same byte[] unless a write is called in between.
  * <p/>
  * Note: once the InputStream is created, the output stream should no longer be used.  In particular, make sure not to
  * call reset() and then write as that may overwrite the data that the InputStream is using.
  */
 final class LoadingByteArrayOutputStream extends ByteArrayOutputStream {
 
     public final static int DEFAULT_BUFFER_SIZE = 1024;
 
     public LoadingByteArrayOutputStream() {
         super(DEFAULT_BUFFER_SIZE);
     }
 
     public LoadingByteArrayOutputStream(int i) {
         super(i);
     }
 
    public synchronized ByteArrayInputStream createInputStream() {
         return new ByteArrayInputStream(buf, 0, count) {
             public String toString() {
                 return IOUtils.newStringFromBytes(buf, 0, count);
             }
         };
     }
 
     @Override
     public synchronized byte[] toByteArray() {
         if(count != buf.length) {
             buf = super.toByteArray();
         }
         return buf;
     }
 
    public synchronized byte[] getRawBytes() {
         return buf;
     }
 }
