 /*
  * Copyright (C) 2012 The ChameleonOS Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.android.thememanager;
 
 import android.content.Context;
 import android.content.res.IThemeManagerService;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Point;
 import android.os.ServiceManager;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.WindowManager;
 
 import java.io.*;
 import java.nio.ByteBuffer;
 import java.util.zip.*;
 
 public class ThemeZipUtils {
     private static final String TAG = "ThemeZipUtils";
 
     /**
      * Simple copy routine given an input stream and an output stream
      */
     private static void copyInputStream(InputStream in, OutputStream out)
             throws IOException {
         byte[] buffer = new byte[1024];
         int len;
 
         while ((len = in.read(buffer)) >= 0) {
             out.write(buffer, 0, len);
         }
 
         out.close();
     }
 
     public static void extractTheme(String src, String dst, Context context) throws IOException {
         ZipInputStream zip = new ZipInputStream(new BufferedInputStream(
                 new FileInputStream(src)));
         ZipEntry ze = null;
 
         while ((ze = zip.getNextEntry()) != null) {
             if (ze.isDirectory()) {
                 // Assume directories are stored parents first then children
                 Log.d(TAG, "Creating directory /data/system/theme/" + ze.getName());
                 File dir = new File(dst + "/" + ze.getName());
                 dir.mkdir();
                 dir.setReadable(true, false);
                 dir.setWritable(true, false);
                 dir.setExecutable(true, false);
                 zip.closeEntry();
                 continue;
             }
 
             Log.d(TAG, "Creating file " + ze.getName());
             if (ze.getName().contains("bootanimation.zip")) {
                 File f = new File(dst + "/" + ze.getName());
                 if (f.exists())
                     f.delete();
                 WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
                 DisplayMetrics dm = new DisplayMetrics();
                 wm.getDefaultDisplay().getRealMetrics(dm);
                 extractBootAnimation(zip, dst + "/" + ze.getName(),
                         new Point(dm.widthPixels, dm.heightPixels));
             } else
                 copyInputStream(zip,
                         new BufferedOutputStream(new FileOutputStream(dst + "/" + ze.getName())));
             (new File(dst + "/" + ze.getName())).setReadable(true, false);
             zip.closeEntry();
         }
 
         zip.close();
     }
 
     public static void extractThemeElement(String src, String dst, int elementType, Context context) throws IOException {
         ZipInputStream zip = new ZipInputStream(new BufferedInputStream(
                 new FileInputStream(src)));
         ZipEntry ze = null;
 
         boolean done = false;
 
         while ((ze = zip.getNextEntry()) != null && !done) {
             switch (elementType) {
                 case Theme.THEME_ELEMENT_TYPE_ICONS:
                     if (ze.getName().equals("icons")) {
                         copyInputStream(zip,
                                 new BufferedOutputStream(new FileOutputStream(dst + "/" + ze.getName())));
                         (new File(dst + "/" + ze.getName())).setReadable(true, false);
                         done = true;
                     }
                     break;
                 case Theme.THEME_ELEMENT_TYPE_WALLPAPER:
                     if (ze.getName().contains("wallpaper")) {
                         if (ze.isDirectory()) {
                             // Assume directories are stored parents first then children
                             Log.d(TAG, "Creating directory " + dst + "/" + ze.getName());
                             File dir = new File(dst + "/" + ze.getName());
                             dir.mkdir();
                             dir.setReadable(true, false);
                             dir.setWritable(true, false);
                             dir.setExecutable(true, false);
                             zip.closeEntry();
                             continue;
                         } else {
                             copyInputStream(zip,
                                     new BufferedOutputStream(new FileOutputStream(dst + "/" + ze.getName())));
                             (new File(dst + "/" + ze.getName())).setReadable(true, false);
                         }
                     }
                     break;
                 case Theme.THEME_ELEMENT_TYPE_SYSTEMUI:
                     if (ze.getName().equals("com.android.systemui")) {
                         copyInputStream(zip,
                                 new BufferedOutputStream(new FileOutputStream(dst + "/" + ze.getName())));
                         (new File(dst + "/" + ze.getName())).setReadable(true, false);
                         done = true;
                     }
                     break;
                 case Theme.THEME_ELEMENT_TYPE_FRAMEWORK:
                     if (ze.getName().equals("framework-res")) {
                         copyInputStream(zip,
                                 new BufferedOutputStream(new FileOutputStream(dst + "/" + ze.getName())));
                         (new File(dst + "/" + ze.getName())).setReadable(true, false);
                         done = true;
                     }
                     break;
                 case Theme.THEME_ELEMENT_TYPE_LOCKSCREEN:
                     if (ze.getName().equals("lockscreen")) {
                         copyInputStream(zip,
                                 new BufferedOutputStream(new FileOutputStream(dst + "/" + ze.getName())));
                         (new File(dst + "/" + ze.getName())).setReadable(true, false);
                         done = true;
                     }
                     break;
                 case Theme.THEME_ELEMENT_TYPE_RINGTONES:
                     if (ze.getName().contains("ringtones")) {
                         if (ze.isDirectory()) {
                             // Assume directories are stored parents first then children
                             Log.d(TAG, "Creating directory /data/system/theme/" + ze.getName());
                             File dir = new File(dst + "/" + ze.getName());
                             dir.mkdir();
                             dir.setReadable(true, false);
                             dir.setWritable(true, false);
                             dir.setExecutable(true, false);
                             zip.closeEntry();
                             continue;
                         } else {
                             copyInputStream(zip,
                                     new BufferedOutputStream(new FileOutputStream(dst + "/" + ze.getName())));
                             (new File(dst + "/" + ze.getName())).setReadable(true, false);
                         }
                     }
                     break;
                 case Theme.THEME_ELEMENT_TYPE_BOOTANIMATION:
                     if (ze.getName().contains("boots")) {
                         if (ze.isDirectory()) {
                             // Assume directories are stored parents first then children
                             Log.d(TAG, "Creating directory /data/system/theme/" + ze.getName());
                             File dir = new File(dst + "/" + ze.getName());
                             dir.mkdir();
                             dir.setReadable(true, false);
                             dir.setWritable(true, false);
                             dir.setExecutable(true, false);
                             zip.closeEntry();
                             continue;
                         } else {
                             File f = new File(dst + "/" + ze.getName());
                             if (f.exists())
                                 f.delete();
                             WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
                             DisplayMetrics dm = new DisplayMetrics();
                             wm.getDefaultDisplay().getRealMetrics(dm);
                             extractBootAnimation(zip, dst + "/" + ze.getName(),
                                     new Point(dm.widthPixels, dm.heightPixels));
                             //copyInputStream(zip,
                               //      new BufferedOutputStream(new FileOutputStream(dst + "/" + ze.getName())));
                             (new File(dst + "/" + ze.getName())).setReadable(true, false);
                         }
                     }
                     break;
             }
             zip.closeEntry();
         }
 
         zip.close();
     }
 
     public static void extractBootAnimation(InputStream input, String dst, Point screenDims)
             throws IOException {
         OutputStream os = new FileOutputStream(dst);
         ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(os));
         ZipInputStream bootAni = new ZipInputStream(input);
         ZipEntry ze = null;
 
         zos.setMethod(ZipOutputStream.STORED);
         byte[] bytes = new byte[1024];
         int len = 0;
         CRC32 crc32 = new CRC32();
         int scaledWidth = 0;
         int scaledHeight = 0;
         if (screenDims.x > screenDims.y) {
             scaledWidth = screenDims.y;
             scaledHeight = screenDims.x;
         } else {
             scaledWidth = screenDims.x;
             scaledHeight = screenDims.y;
         }
         while ((ze = bootAni.getNextEntry()) != null) {
             crc32.reset();
             ZipEntry entry = new ZipEntry(ze.getName());
             entry.setMethod(ZipEntry.STORED);
             entry.setCrc(ze.getCrc());
             entry.setSize(ze.getSize());
             entry.setCompressedSize(ze.getSize());
             if (!ze.getName().equals("desc.txt")) {
                 if (ze.getName().toLowerCase().endsWith(".png") || ze.getName().toLowerCase().endsWith(".jpg")) {
                     Bitmap bmp = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(bootAni, null, null),
                             scaledWidth, scaledHeight, true);
                     ByteArrayOutputStream bos = new ByteArrayOutputStream();
                     bmp.compress(ze.getName().toLowerCase().endsWith("png") ?
                             Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 80, bos);
                     byte[] bitmapdata = bos.toByteArray();
                     crc32.reset();
                     crc32.update(bitmapdata);
                     entry.setSize(bitmapdata.length);
                     entry.setCompressedSize(bitmapdata.length);
                     entry.setCrc(crc32.getValue());
                     zos.putNextEntry(entry);
                     zos.write(bitmapdata);
                     bos.close();
                     bmp.recycle();
                 }
             } else {
                 String line = "";
                 BufferedReader reader = new BufferedReader(new InputStreamReader(bootAni));
                 String[] info = reader.readLine().split(" ");
                 int width = Integer.parseInt(info[0]);
                 int height = Integer.parseInt(info[1]);
 
                 if (width == height)
                     scaledHeight = scaledWidth;
 
                 crc32.reset();
                 int size = 0;
                 ByteBuffer buffer = ByteBuffer.wrap(bytes);
                 line = String.format("%d %d %s\n", scaledWidth, scaledHeight, info[2]);
                 buffer.put(line.getBytes());
                 size += line.getBytes().length;
                 crc32.update(line.getBytes());
                 while ((line = reader.readLine()) != null) {
                     line = String.format("%s\n", line);
                     buffer.put(line.getBytes());
                     size += line.getBytes().length;
                     crc32.update(line.getBytes());
                 }
                 entry.setCrc(crc32.getValue());
                 entry.setSize(size);
                 entry.setCompressedSize(size);
                 zos.putNextEntry(entry);
                 zos.write(buffer.array(), 0, size);
             }
             zos.closeEntry();
         }
         zos.close();
     }
 }
