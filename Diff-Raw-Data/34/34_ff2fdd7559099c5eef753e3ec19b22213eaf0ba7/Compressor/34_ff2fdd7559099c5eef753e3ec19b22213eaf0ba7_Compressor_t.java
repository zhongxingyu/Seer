 package elr.core.modules.compressor;
 
 import elr.Starter;
 import elr.core.modules.AES;
 import elr.core.modules.compressor.xz_coder.xz.UnsupportedOptionsException;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import net.lingala.zip4j.core.ZipFile;
 import net.lingala.zip4j.exception.ZipException;
 import net.lingala.zip4j.model.ZipParameters;
 import net.lingala.zip4j.util.Zip4jConstants;
 
 /**
  * Compressor module. Supports basic Zip compression, XZ encode and AES encryptation.
  * @author Infernage
  */
 public class Compressor {
     private static final int MAXSPLITLENGTH = 10485760;
     
     /**
      * Selects the compression level.
      */
     public static enum CompressionLevel { FAST, MIN, NORMAL, MAX, ULTRA }
     
     /**
      * Basic compression.
      * @param src The file to compress.
      * @param dst The Zip file where will be compressed.
      * @param level The compression level.
      * @throws ZipException 
      */
     public static void basicCompression(File src, File dst, CompressionLevel level) throws 
             ZipException{
        if (dst.isDirectory()) throw new RuntimeException("Destiny file can't be a directory");
         ZipFile zip = new ZipFile(dst);
         ZipParameters parameters = new ZipParameters();
         parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
         if (level == null) level = CompressionLevel.NORMAL;
         switch(level){
             case FAST: parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_FASTEST);
                 break;
             case MIN: parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_FAST);
                 break;
             case NORMAL: parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
                 break;
             case MAX: parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
                 break;
             case ULTRA: parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA);
                 break;
         }
         if (src.isDirectory()) zip.createZipFileFromFolder(src, parameters, false, MAXSPLITLENGTH);
         else zip.createZipFile(src, parameters, false, MAXSPLITLENGTH);
     }
     
     /**
      * Crypted compression. Uses AES algorithm to crypt it.
      * @param src The file to compress.
      * @param dst The Zip file where will be compressed.
      * @param level The compression level.
      * @param crypter AES object created to use encryptFile method. Can not be null.
      * @return The file crypted.
      * @throws ZipException 
      */
     public static File cryptedCompression(File src, File dst, CompressionLevel level, AES crypter) 
             throws ZipException{
         basicCompression(src, dst, level);
         File crypted = crypter.encryptFile(dst);
         dst.delete();
         return crypted;
     }
     
     /**
      * Coded compression. Uses XZ Data Compressor.
      * @param src The file to compress.
      * @param dst The Zip file where will be compressed.
      * @param level The compression level.
      * @return The file encoded.
      * @throws ZipException
      * @throws UnsupportedOptionsException
      * @throws IOException 
      */
     public static File codedCompression(File src, File dst, CompressionLevel level) throws
             ZipException, UnsupportedOptionsException, IOException{
         basicCompression(src, dst, level);
         File encoded = Encoder.encode(dst);
         dst.delete();
         return encoded;
     }
     
     /**
      * Combines all compression types.
      * @param src The file to compress.
      * @param dst The Zip file where will be compressed.
      * @param level The compression level.
      * @param crypter AES object created to use encryptFile method. Can not be null.
      * @return The file crypted and encoded.
      * @throws ZipException
      * @throws UnsupportedOptionsException
      * @throws IOException 
      */
     public static File secureCompression(File src, File dst, CompressionLevel level, AES crypter) 
             throws ZipException, UnsupportedOptionsException, IOException{
         basicCompression(src, dst, level);
         File crypted = crypter.encryptFile(dst);
         dst.delete();
         File encoded = Encoder.encode(crypted);
         crypted.delete();
         return encoded;
     }
     
     /**
      * Used only by Starter class.
      * @param src The file to compress.
      * @param dst The Zip file where will be compressed.
      * @param level The compression level.
      * @param obj An instance of Starter.
      * @return The file encoded and crypted.
      * @throws ZipException
      * @throws UnsupportedOptionsException
      * @throws IOException 
      */
     public static File privilegedCompression(File src, File dst, CompressionLevel level, Object obj) throws 
             ZipException, UnsupportedOptionsException, IOException{
         if (obj instanceof Starter){
             basicCompression(src, dst, level);
             AES secure = new AES(Starter.class.getCanonicalName());
             File crypter = new File(dst, ".priv");
             secure.encryptFile(dst, crypter);
             dst.delete();
             File encoded = Encoder.encode(crypter);
             crypter.delete();
             return encoded;
         }
         return null;
     }
     
     /**
      * Basic decompression.
      * @param src The Zip file to decompress.
      * @param dst The file where will be extracted.
      * @return The file extracted.
      * @throws ZipException 
      */
     public static File basicDecompression(File src, File dst) throws ZipException{
         ZipFile zip = new ZipFile(src);
         if (dst.isDirectory()) zip.extractAll(dst.getAbsolutePath());
         else if (dst.isFile()) zip.extractAll(dst.getParent());
         else throw new ZipException("Output file can not be recognized");
         return zip.getFile();
     }
     
     /**
      * Crypted decompression. Uses AES algorithm to decrypt it.
      * @param src The file crypted.
      * @param dst The file where will be extracted.
      * @param decrypter AES object created to use decryptFile method. Can not be null.
      * @return The file extracted.
      * @throws ZipException 
      */
     public static File cryptedDecompression(File src, File dst, AES decrypter) throws ZipException{
         File decrypted = decrypter.decryptFile(src);
         File decompressed = basicDecompression(decrypted, dst);
         decrypted.delete();
         return decompressed;
     }
     
     /**
      * Coded decompression Uses XZ Data Compressor.
      * @param src The file encoded.
      * @param dst The file where will be extracted.
      * @return The file extracted.
      * @throws ZipException
      * @throws FileNotFoundException
      * @throws IOException 
      */
     public static File codedDecompression(File src, File dst) throws ZipException, FileNotFoundException, IOException{
         File decoded = Decoder.decode(src);
         File decompressed = basicDecompression(src, dst);
         decoded.delete();
         return decompressed;
     }
     
     /**
      * Combines all decompression types.
      * @param src The file crypted and coded.
      * @param dst The file where will be extracted.
      * @param decrypter AES object created to use decryptFile method. Can not be null.
      * @return The file extracted.
      * @throws ZipException
      * @throws UnsupportedOptionsException
      * @throws IOException 
      */
     public static File secureDecompression(File src, File dst, AES decrypter) 
             throws ZipException, UnsupportedOptionsException, IOException{
         File decoded = Decoder.decode(src);
         File decrypted = decrypter.decryptFile(decoded);
         decoded.delete();
         File decompressed = basicDecompression(decrypted, dst);
         decrypted.delete();
         return decompressed;
     }
     
     /**
      * Used only by Starter class.
      * @param src The file crypted and encoded.
      * @param dst The file where will be extracted.
      * @param obj An instance of Starter.
      * @return The file extracted.
      * @throws ZipException
      * @throws FileNotFoundException
      * @throws IOException 
      */
     public static File privilegedDecompression(File src, File dst, Object obj) throws ZipException, 
             FileNotFoundException, IOException{
         if (obj instanceof Starter){
             File decoded = Decoder.decode(src);
             File decrypter = new AES(Starter.class.getCanonicalName()).decryptFile(decoded, decoded.getName()
                     .replace(".priv", ""));
             decoded.delete();
             File decompressed = basicDecompression(decrypter, dst);
             decrypter.delete();
             return decompressed;
         }
         return null;
     }
 }
