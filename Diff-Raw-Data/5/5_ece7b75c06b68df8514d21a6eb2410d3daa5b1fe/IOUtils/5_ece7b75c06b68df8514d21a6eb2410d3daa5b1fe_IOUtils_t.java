 package org.esidoc.core.utils.io;
 
 import net.sf.oval.guard.Guarded;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.validation.constraints.NotNull;
 import java.io.ByteArrayOutputStream;
 import java.io.Closeable;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 
 /**
  * @author <a href="mailto:mail@eduard-hildebrandt.de">Eduard Hildebrandt</a>
  */
 @Guarded(applyFieldConstraintsToConstructors = true, applyFieldConstraintsToSetters = true,
         assertParametersNotNull = false, checkInvariants = true, inspectInterfaces = true)
 public final class IOUtils {
 
     private final static Logger LOG = LoggerFactory.getLogger(IOUtils.class);
 
     private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
 
     private IOUtils() {
     }
 
     public static void copy(@NotNull final InputStream input, @NotNull final OutputStream output) throws IOException {
        copy(input, output, DEFAULT_BUFFER_SIZE);
     }
 
     public static void copyAndCloseInput(@NotNull final InputStream input, @NotNull final OutputStream output)
             throws IOException {
         try {
            copy(input, output, DEFAULT_BUFFER_SIZE);
         } finally {
             closeStream(input);
         }
     }
 
     public static int copyAndCloseInput(@NotNull final InputStream input, @NotNull final OutputStream output,
                                         final int bufferSize) throws IOException {
         try {
             return copy(input, output, bufferSize);
         } finally {
             closeStream(input);
         }
     }
 
     public static int copy(@NotNull final InputStream input, @NotNull final OutputStream output, int bufferSize)
             throws IOException {
         int avail = input.available();
         if(avail > 262144) {
             avail = 262144;
         }
         if(avail > bufferSize) {
             bufferSize = avail;
         }
         final byte[] buffer = new byte[bufferSize];
         int n = input.read(buffer);
         int total = 0;
         while(- 1 != n) {
             output.write(buffer, 0, n);
             total += n;
             n = input.read(buffer);
         }
         return total;
     }
 
     public static byte[] readBytesFromStream(@NotNull final InputStream input) throws IOException {
         int i = input.available();
         if(i < DEFAULT_BUFFER_SIZE) {
             i = DEFAULT_BUFFER_SIZE;
         }
         final ByteArrayOutputStream bos = new ByteArrayOutputStream(i);
         copy(input, bos);
         closeStream(input);
         return bos.toByteArray();
     }
 
     public static String newStringFromBytes(final byte[] bytes, final String charsetName) {
         try {
             return new String(bytes, charsetName);
         } catch(UnsupportedEncodingException e) {
             throw new RuntimeException(
                     "Impossible failure: Charset.forName(\"" + charsetName + "\") returns " + "invalid name.");
         }
     }
 
     public static String newStringFromBytes(final byte[] bytes) {
         return newStringFromBytes(bytes, Encodings.UTF8);
     }
 
     public static String newStringFromBytes(final byte[] bytes, final String charsetName, final int start,
                                             final int length) {
         try {
             return new String(bytes, start, length, charsetName);
         } catch(UnsupportedEncodingException e) {
             throw new RuntimeException(
                     "Impossible failure: Charset.forName(\"" + charsetName + "\") returns invalid " + "name.");
 
         }
     }
 
     public static String newStringFromBytes(final byte[] bytes, final int start, final int length) {
         return newStringFromBytes(bytes, Encodings.UTF8, start, length);
     }
 
     public static String newStringFromStream(final InputStream input, final String charsetName) throws IOException {
         int i = input.available();
         if(i < DEFAULT_BUFFER_SIZE) {
             i = DEFAULT_BUFFER_SIZE;
         }
         final ByteArrayOutputStream bos = new ByteArrayOutputStream(i);
         copy(input, bos);
         closeStream(input);
         return bos.toString(charsetName);
     }
 
     public static String newStringFromStream(final InputStream input) throws IOException {
         return newStringFromStream(input, Encodings.UTF8);
     }
 
     public static void closeStream(final Closeable closeable) {
         if(closeable == null) {
             return;
         }
         try {
             closeable.close();
         } catch(final IOException e) {
             LOG.error("Error on closing stream.", e);
         }
     }
 
 }
