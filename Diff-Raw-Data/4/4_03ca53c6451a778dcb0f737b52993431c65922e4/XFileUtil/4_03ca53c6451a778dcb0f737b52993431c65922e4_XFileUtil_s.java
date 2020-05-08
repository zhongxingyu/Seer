 package com.xengine.android.utils;
 
 import android.text.TextUtils;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 import java.util.zip.ZipOutputStream;
 
 /**
  * Created with IntelliJ IDEA.
  * User: tujun
  * Date: 13-9-6
  * Time: 下午5:45
  * To change this template use File | Settings | File Templates.
  */
 public class XFileUtil {
 
     /**
      * 复制文件
      * @param oldFile
      * @param newFile
      * @return
      */
     public static boolean copyFile(File oldFile, File newFile) {
         if (!oldFile.exists())  // 文件存在时
             return false;
         InputStream is = null;
         FileOutputStream fs = null;
         try {
             is = new FileInputStream(oldFile); // 读入原文件
             fs = new FileOutputStream(newFile);
             byte[] buffer = new byte[1024];
             int read;
             while ((read = is.read(buffer)) != -1) {
                 fs.write(buffer, 0, read);
             }
             return true;
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             try {
                 if (is != null)
                     is.close();
                 if (fs != null)
                     fs.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
 
         }
         return false;
     }
 
     /**
      * 将File转换为byte[]
      * @param file
      * @return
      * @throws java.io.IOException
      */
     public static byte[] file2byte(File file) throws IOException {
         if (file == null)
             return null;
 
         InputStream is = new FileInputStream(file);
         // 判断文件大小
         long length = file.length();
         if (length > Integer.MAX_VALUE) // 文件太大，无法读取
             throw new IOException("File is to large "+file.getName());
         // 创建一个数据来保存文件数据
         byte[] bytes = new byte[(int)length];
         // 读取数据到byte数组中
         int offset = 0;
         int numRead = 0;
         while (offset < bytes.length &&
                 (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
             offset += numRead;
         }
         is.close();
 
         // 确保所有数据均被读取
         if (offset < bytes.length)
             throw new IOException("Could not completely read file "+file.getName());
         return bytes;
     }
 
     /**
      * 将byte[]转换为File
      * @param bytes
      * @param file
      * @return
      */
     public static boolean byte2file(byte[] bytes, File file) {
         BufferedOutputStream bos = null;
         FileOutputStream fos = null;
         try {
             fos = new FileOutputStream(file);
             bos = new BufferedOutputStream(fos);
             bos.write(bytes);
             return true;
         } catch (FileNotFoundException e) {
             e.printStackTrace();
             return false;
         } catch (IOException e) {
             e.printStackTrace();
             return false;
         } finally {
             try {
                 if (bos != null)
                     bos.close();
                 if (fos != null)
                     fos.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * String转换为File。
      * 如果创建失败，会删除文件。
      * @param res 字符内容
      * @param file 文件
      * @return 如果创建成功，返回true；否则返回false
      */
     public static boolean string2File(String res, File file) {
         if (file == null || res == null)
             return false;
         BufferedWriter bufferedWriter = null;
         try {
             bufferedWriter = new BufferedWriter(new FileWriter(file));
             bufferedWriter.write(res);
             bufferedWriter.flush();
             return true;
         } catch (IOException e) {
             e.printStackTrace();
             if (file.exists())
                 file.delete();
             return false;
         } finally {
             try {
                 if (bufferedWriter != null)
                     bufferedWriter.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * File转换为String。
      * @param file 文件
      * @return 如果读取失败，返回null；否则返回字符串形式。
      */
     public static String file2String(File file) {
         FileInputStream fis = null;
         ByteArrayOutputStream baos = null;
         try {
             fis = new FileInputStream(file);
             baos = new ByteArrayOutputStream();
             int i;
             while ((i = fis.read()) != -1) {
                 baos.write(i);
             }
             String str = baos.toString();
             return str;
         } catch (FileNotFoundException e) {
             e.printStackTrace();
             return null;
         } catch (IOException e) {
             e.printStackTrace();
             return null;
         } finally {
             try {
                 if (fis != null)
                     fis.close();
                 if (baos != null)
                     baos.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * 清空文件夹。
      * @param dir 文件夹
      * @param removeSelf 是否删除自身
      */
     public static void clearDirectory(File dir, boolean removeSelf) {
         if (dir == null || !dir.exists())
             return;
 
         File[] files = dir.listFiles();
         if (files != null) // 如果dir不是文件夹，files会为null
             for (File file : files)
                 clearDirectory(file, true);// 递归
 
         if (removeSelf)
             dir.delete();
     }
 
     /**
      * 将多个文件压缩成一个.zip压缩文件
      * @param originFilePaths 多个指定文件的路径
      * @param zipFilePath 压缩生成的.zip文件的路径
      * @return 压缩成功返回true;否则返回false
      */
     public static boolean zipFile(List<String> originFilePaths, String zipFilePath) {
         // 防止外部修改列表
         List<String> copyFilePaths = new ArrayList<String>(originFilePaths);
         // 创建.zip文件所在的文件夹(如果不存在)，以及删除同名的.zip文件(如果存在)
         File zipFile = new File(zipFilePath);
         File zipFileDir = zipFile.getParentFile();
         if (zipFileDir != null && !zipFileDir.exists()) {
             zipFileDir.mkdirs();
         }
         if (zipFile.exists()) {
             zipFile.delete();
         }
         FileOutputStream out = null;
         ZipOutputStream zipOut = null;
         try {
             out = new FileOutputStream(zipFilePath);// 根据文件路径构造一个文件输出流
             zipOut = new ZipOutputStream(out);// 创建ZIP数据输出流对象
             // 循环待压缩的文件列表
             byte[] buffer = new byte[512];
             for (String originFilePath : copyFilePaths) {
                 if (TextUtils.isEmpty(originFilePath))
                     continue;
                 File originFile = new File(originFilePath);
                 if (!originFile.exists())
                     continue;
                 // 创建文件输入流对象
                 FileInputStream in = new FileInputStream(originFile);
                 // 创建指向压缩原始文件的入口
                 ZipEntry entry = new ZipEntry(originFile.getName());
                 zipOut.putNextEntry(entry);
                 // 向压缩文件中输出数据
                 int nNumber = 0;
                 while ((nNumber = in.read(buffer)) != -1) {
                     zipOut.write(buffer, 0, nNumber);
                 }
                 // 关闭创建的流对象
                 in.close();
             }
             return true;
         } catch (IOException e) {
             e.printStackTrace();
             return false;
         } finally {
             try {
                 if (out != null)
                     out.close();
                 if (zipOut != null)
                     zipOut.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * 解压.zip文件
      * @param zipFile
      * @param dirPath
      * @param override
      * @return
      */
     public static boolean unzipFile(String zipFile, String dirPath,
                                     boolean override) {
         if (TextUtils.isEmpty(zipFile))
             return false;
         File file = new File(zipFile);
         if (!file.exists())
             return false;
         try {
             InputStream is = new FileInputStream(file);
             return unzipFile(is, dirPath, override);
         } catch (FileNotFoundException e) {
             e.printStackTrace();
             return false;
         }
     }
 
     /**
      * 解压.zip文件
      * @param zipInput
      * @param dirPath
      * @param override
      * @return
      */
     public static boolean unzipFile(InputStream zipInput, String dirPath,
                                     boolean override) {
         if (TextUtils.isEmpty(dirPath))
             return false;
 
         File df = new File(dirPath);
         df.mkdirs();
         if (!df.exists())
             return false;
 
         ZipInputStream zis = null;
         BufferedInputStream bis = null;
         try {
             zis = new ZipInputStream(zipInput);
             bis = new BufferedInputStream(zis);
             File file = null;
             ZipEntry entry;
             while ((entry = zis.getNextEntry()) != null && !entry.isDirectory()) {
                 file = new File(dirPath, entry.getName());
                 if (file.exists()) {// 如果文件存在，根据override字段决定是否要覆盖
                     if (override)
                         file.delete();// 覆盖，先删除原先的文件
                     else
                         continue;// 不覆盖，直接跳过
                 }
                 FileOutputStream out = new FileOutputStream(file);
                 BufferedOutputStream bos = new BufferedOutputStream(out);
                 byte buffer[] = new byte[512];
                 int realLength = 0;
                 while ((realLength = bis.read(buffer)) != -1) {
                     out.write(buffer, 0, realLength);
                 }
                 bos.flush();
                 bos.close();
                 out.close();
             }
             return true;
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             try {
                 if (bis != null)
                     bis.close();
                 if (zis != null)
                     zis.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         return false;
     }
 }
