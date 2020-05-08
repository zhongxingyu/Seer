 package cn.link.imageloader.disc;
 
 import android.graphics.Bitmap;
 import cn.link.imageloader.DefaultConfigurationFactory;
 import cn.link.imageloader.DisplayOptions;
 import cn.link.imageloader.ImageLoaderEngine;
 import cn.link.imageloader.assist.FileNameGenerator;
 import cn.link.imageloader.assist.IoUtils;
 import cn.link.imageloader.decode.ImageDecoder;
 
 import java.io.*;
 
 /**
  * Base disc cache. Implements common functionality for disc cache.
  *
  * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
  * @see DiscCacheAware
  * @see FileNameGenerator
  * @since 1.0.0
  */
 public class BaseDiscCache implements DiscCacheAware {
 
     private static final String ERROR_ARG_NULL = "\"%s\" argument must be not null";
 
     protected File mCacheDir;
 
 
     public BaseDiscCache(File cacheDir) {
         if (cacheDir == null) {
             throw new IllegalArgumentException("cacheDir" + ERROR_ARG_NULL);
         }
         this.mCacheDir = cacheDir;
     }
 
     @Override
     public Bitmap read(String key, DisplayOptions options, ImageDecoder decoder) throws IOException {
         File cacheFile = new File(mCacheDir,
                 ImageLoaderEngine.sConfigOptions.mFileNameGenerator.generate(key));
         if (cacheFile.exists()) {
             BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(cacheFile));
             ByteArrayOutputStream output = new ByteArrayOutputStream();
             IoUtils.copyStream(fileInputStream, output);
             IoUtils.closeSilently(fileInputStream);
             return decoder.decode(output, options);
         }
         return null;
     }
 
 
     @Override
     public Bitmap decodeAndWrite(InputStream input, DisplayOptions options, ImageDecoder decoder) throws IOException {
         Bitmap bitmap = null;
         String fileName = ImageLoaderEngine.sConfigOptions.mFileNameGenerator.generate(options.getDisplayUrl());
         File image = new File(mCacheDir, fileName);
         if (!image.exists()) {
             image.createNewFile();
             BufferedOutputStream fout = null;
             if (options.ifCacheOnDisc()) {
                 fout = new BufferedOutputStream(new FileOutputStream(image));
             }
             ByteArrayOutputStream output = new ByteArrayOutputStream();
             byte[] buf1 = new byte[4098];
             int len = 0;
             int sum = 0;
             while ((len = input.read(buf1)) != -1) {
                if (options.ifCacheOnDisc()) {
                     fout.write(buf1, 0, len);
                 }
                 output.write(buf1, 0, len);
 
                 if (options.getProgressListener() != null) {
                     sum = sum + len;
                     options.getProgressListener().onProgress(sum);
                 }
             }
            if (options.ifCacheOnDisc()) {
                 fout.flush();
             }
             fout.close();
 
 
             if (options.getProgressListener() != null) {
                 options.getProgressListener().onEnd();
 
             }
 
             bitmap = decoder.decode(output, options);
         }
         if (input != null) {
             input.close();
         }
 
         return bitmap;
     }
 
     @Override
     public void clear() {
         File[] files = mCacheDir.listFiles();
         if (files != null) {
             for (File f : files) {
                 f.delete();
             }
         }
     }
 
     @Override
     public File getCacheDir() {
         return mCacheDir;
     }
 }
