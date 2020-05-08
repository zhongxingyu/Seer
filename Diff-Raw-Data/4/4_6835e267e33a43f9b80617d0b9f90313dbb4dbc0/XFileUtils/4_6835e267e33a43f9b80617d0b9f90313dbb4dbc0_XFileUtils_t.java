 /*
  Copyright 2012-2013, Polyvi Inc. (http://polyvi.github.io/openxface)
  This program is distributed under the terms of the GNU General Public License.
 
  This file is part of xFace.
 
  xFace is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  xFace is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with xFace.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.polyvi.xface.util;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.RandomAccessFile;
 import java.nio.channels.FileChannel;
 import java.util.Date;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.content.res.Resources.NotFoundException;
 import android.os.Environment;
 import android.webkit.MimeTypeMap;
 
 import com.polyvi.xface.core.XConfiguration;
 
 public class XFileUtils {
     private final static String CLASS_NAME = XFileUtils.class.getSimpleName();
     // TODO:只加需要的权限，不覆盖已有权限
     public final static String ALL_PERMISSION = "777"; // 文件权限：全部权限
     public final static String EXECUTABLE_BY_OTHER = "701"; // 文件权限：其他用户组可执行
     public final static String READABLE_BY_OTHER = "704"; // 文件权限：其他用户组可读
     public final static String READABLE_AND_EXECUTEBLE_BY_OTHER = "705";// 文件权限:其他用户可读可执行
 
     private static final int OCTAL_RADIX = 8; // 八进制基数
     private static String NO_MEDIA_FILE_NAME = ".nomedia";
     private static final String FILE_DIRECTORY = "raw";
 
     /**
      * 写文件
      *
      * @param filePath
      *            要写入的文件的路径
      * @param data
      *            要写入的数据
      * @return 开始写入数据的位置
      */
     public static long write(String fileName, String data, int position)
             throws FileNotFoundException, IOException {
         boolean append = false;
         if (position > 0) {
             truncateFile(fileName, position);
             append = true;
         }
 
         byte[] rawData = data.getBytes();
         ByteArrayInputStream in = new ByteArrayInputStream(rawData);
         FileOutputStream out = new FileOutputStream(fileName, append);
         byte buff[] = new byte[rawData.length];
         in.read(buff, 0, buff.length);
         out.write(buff, 0, rawData.length);
         out.flush();
         out.close();
 
         return data.length();
     }
 
     /**
      * 清除指定长度后的文件内容
      *
      * @param filePath
      *            要清除的文件的路径
      * @param size
      *            清除size后的文件
      * @return 剩下的文件长度
      */
     public static long truncateFile(String fileName, long size)
             throws FileNotFoundException, IOException {
         RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
 
         if (raf.length() >= size) {
             FileChannel channel = raf.getChannel();
             channel.truncate(size);
             return size;
         }
 
         return raf.length();
     }
 
     /**
      * 获取指定文件类型
      *
      * @param fileName
      *            文件的路径
      * @return 文件的类型
      */
     public static String getMimeType(String filename) {
         MimeTypeMap map = MimeTypeMap.getSingleton();
         return map.getMimeTypeFromExtension(MimeTypeMap
                 .getFileExtensionFromUrl(filename));
     }
 
     /**
      * 创建文件和文件所在的目录
      *
      * @param path
      *            文件的绝对路径
      * @return 成功返回true,失败返回false
      */
     public static Boolean createFile(String path) {
         File file = new File(path);
         File parantFile = file.getParentFile();
         if (null != parantFile && !parantFile.exists() && !parantFile.mkdirs()) {
             return false;
         }
 
         try {
             file.createNewFile();
         } catch (IOException e) {
             e.printStackTrace();
             return false;
         }
         return true;
     }
 
     /**
      * 获得当前系统的sdcard路径
      *
      * @return 如果sdcard可访问，则返回带当前系统的sdcard路径，否则返回null。
      */
     public static String getSdcardPath() {
         String path = null;
         if (Environment.getExternalStorageState().equals(
                 Environment.MEDIA_MOUNTED)) {
             path = Environment.getExternalStorageDirectory().getAbsolutePath();
         } else {
             path = XExternalStorageScanner.getExternalStoragePath();
         }
        if (null == path) {
            XLog.w(CLASS_NAME, "No External Storage");
            return null;
        }
         File filePath = null;
         try {
             filePath = new File(path).getCanonicalFile();
         } catch (IOException e) {
             XLog.e(CLASS_NAME, e.getMessage());
             e.printStackTrace();
             return null;
         }
         return filePath.getAbsolutePath();
     }
 
     /**
      * 返回一个代表文件（夹）的JSON对象
      *
      * @param appWorkSpace
      *            当前应用工作目录
      * @param file
      *            文件流对象
      * @return 代表文件的JSON对象
      * @throws JSONException
      */
     public static JSONObject getEntry(String appWorkspace, File file)
             throws JSONException {
         JSONObject entry = new JSONObject();
         entry.put("isFile", file.isFile());
         entry.put("isDirectory", file.isDirectory());
         String fileName = null;
         String fullPath = null;
         String absolutePath = file.getAbsolutePath();
         if (absolutePath.equals(appWorkspace)) {
             fileName = File.separator;
             fullPath = File.separator;
         } else if (XFileUtils.isFileAncestorOf(appWorkspace, absolutePath)) {
             fullPath = file.getAbsolutePath().substring(appWorkspace.length());
             fileName = file.getName();
         }
         entry.put("name", fileName);
         entry.put("fullPath", fullPath);
         entry.put("start", 0);
         entry.put("end", file.length());
         return entry;
     }
 
     /**
      * 修改文件的权限
      *
      * @param permission
      *            权限
      * @param path
      *            文件路径
      */
     public static void setPermission(String permission, String path) {
         XNativeBridge.chmod(path, Integer.parseInt(permission, OCTAL_RADIX));
     }
 
     /**
      * 检查给定的文件路径是否存在
      *
      * @param filePath
      *            给定的文件路径
      * @return true：存在,false:不存在.
      */
     public static boolean checkFileExist(String filePath) {
         if (XStringUtils.isEmptyString(filePath)) {
             XLog.e(CLASS_NAME, "filePath is empty");
             return false;
         }
         if (!new File(filePath).exists()) {
             XLog.e(CLASS_NAME, filePath + " not exist");
             return false;
         }
         return true;
     }
 
     /**
      * 创建临时目录 返回临时目录所创建的目录
      *
      * @param parent
      *            临时目录所在的父目录 parent必须要存在
      * @return 创建失败 返回null 否则返回目录名
      */
     public static String createTempDir(String parent) {
         if (!parent.endsWith("/")) {
             parent += "/";
         }
         String dirPath = parent + String.valueOf(new Date().getSeconds())
                 + XUtils.generateRandomId() + "tmp";
         File tmpFile = new File(dirPath);
         return tmpFile.mkdir() ? tmpFile.getAbsolutePath() : null;
     }
 
     /**
      * 拷贝文件或者目录
      *
      * @param src
      *            待拷贝的源文件或者目录
      * @param des
      *            拷贝的目的目录
      * @throws IOException
      */
     public static void copy(File srcLocation, File desLocation)
             throws IOException {
         if (srcLocation.isDirectory()) {
             if (!desLocation.exists() && !desLocation.mkdirs()) {
                 throw new IOException("can't create dir "
                         + desLocation.getAbsolutePath());
             }
             String[] children = srcLocation.list();
             for (int i = 0; i < children.length; i++) {
                 copy(new File(srcLocation, children[i]), new File(desLocation,
                         children[i]));
             }
         } else {
             File direcory = desLocation.getParentFile();
             if (direcory != null && !direcory.exists() && !direcory.mkdirs()) {
                 throw new IOException("can't create dir "
                         + direcory.getAbsolutePath());
             }
 
             InputStream in = new FileInputStream(srcLocation);
             OutputStream out = new FileOutputStream(desLocation);
             byte[] buf = new byte[XConstant.BUFFER_LEN];
             int len;
             while ((len = in.read(buf)) > 0) {
                 out.write(buf, 0, len);
             }
             in.close();
             out.close();
         }
     }
 
     /**
      * 递归的遍历目录
      *
      * @param srcDir
      *            需要遍历的目录路径
      * @param handler
      *            文件处理器
      */
     public static void walkDirectory(String srcDir, XFileVisitor visitor) {
         walkDirectory(new File(srcDir), visitor);
     }
 
     /**
      * 递归的遍历目录
      *
      * @param srcDir
      *            需要遍历的目录对象
      * @param visitor
      *            文件处理器
      */
     public static void walkDirectory(File srcDir, XFileVisitor visitor) {
         if (!srcDir.exists() || !srcDir.isDirectory()
                 || !visitor.isContinueTraverse()) {
             return;
         }
         File files[] = srcDir.listFiles();
         for (int i = 0; i < files.length; i++) {
             if (files[i].isFile()) {
                 visitor.visit(files[i].getAbsolutePath());
             } else {
                 walkDirectory(files[i], visitor);
             }
         }
     }
 
     /**
      * 获取url的MIMEType类型
      *
      * @paramm url url地址
      * @return MIMEType类型
      */
     public static String getMIMEType(String url) {
         int dotIndex = url.lastIndexOf(".");
         /* 获取文件的后缀名 */
         return dotIndex < 0 ? "*/*"
                 : MimeTypeMap.getSingleton()
                         .getMimeTypeFromExtension(
                                 url.substring(dotIndex + 1, url.length())
                                         .toLowerCase());
     }
 
     /**
      * 删除给定路径的目录或文件，如果给定路径是一个目录，则递归删除目录
      *
      * @param path
      *            目录/文件路径
      */
     public static boolean deleteFileRecursively(String path) {
         File destFile = new File(path);
         if (!destFile.exists()) {
             return true;
         }
 
         if (destFile.isFile()) {
             destFile.delete();
             return true;
         }
 
         String[] childNames = destFile.list();
         for (String child : childNames) {
             if (!deleteFileRecursively(new File(path, child).getAbsolutePath())) {
                 return false;
             }
         }
         return destFile.delete();
     }
 
     /**
      * 创建一个文件并写入文件内容<br>
      * 如果文件存在，则会被覆写；如果该文件所在目录不存在，会自动被创建
      *
      * @param filePath
      *            新建文件的绝对路径
      * @param is
      *            存放写入文件数据的输入流对象
      */
     public static void createFileByData(String filePath, InputStream is) {
         try {
             File destFile = new File(filePath);
             File parentFile = destFile.getParentFile();
             if (!parentFile.exists()) {
                 parentFile.mkdirs();
             }
             FileOutputStream fos = new FileOutputStream(destFile);
             byte[] buffer = new byte[XConstant.BUFFER_LEN];
             int len = 0;
             while (-1 != (len = is.read(buffer))) {
                 fos.write(buffer, 0, len);
             }
 
             fos.close();
         } catch (IOException e) {
             XLog.e(CLASS_NAME, e.getMessage());
             e.printStackTrace();
         }
     }
 
     /**
      * 解压zip文件
      *
      * @param targetPath
      *            解压的目标路径
      * @param zipFilePath
      *            zip包路径
      *
      * @return 是否成功
      */
     public static boolean unzipFile(String targetPath, String zipFilePath) {
         try {
             XZipper zipper = new XZipper();
             zipper.unzipFile(targetPath, zipFilePath);
             return true;
         } catch (FileNotFoundException e) {
             XLog.e(CLASS_NAME, "The zip file: " + zipFilePath
                     + "does not exist!");
             e.printStackTrace();
             return false;
         } catch (IOException e) {
             XLog.e(CLASS_NAME, "Unzip file: " + zipFilePath + " failed!");
             e.printStackTrace();
             return false;
         }
     }
 
     /**
      * 从assets文件资源中读取zip包进行解压
      *
      * @param targetPath
      *            解压的目标目录路径
      * @param context
      * @param assetFileName
      *            要解压的assets资源文件名
      */
     public static boolean unzipFileFromAsset(String targetPath,
             Context context, String assetFileName) {
         try {
             return new XZipper().unzipFileFromAsset(targetPath, context,
                     assetFileName);
         } catch (IOException e) {
             XLog.e(CLASS_NAME, "Unzip file failed: Can't find assets file: "
                     + assetFileName);
             return false;
         }
     }
 
     /**
      * 读取文件中的数据
      *
      * @param filePath
      *            文件绝对路径
      * @return 字符串形式的文件数据
      */
     public static String readFileContent(String filePath) {
         if (null == filePath) {
             return null;
         }
         try {
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             FileInputStream fis = new FileInputStream(filePath);
 
             byte[] buffer = new byte[XConstant.BUFFER_LEN];
             int len = 0;
             while ((len = fis.read(buffer)) != -1) {
                 bos.write(buffer, 0, len);
             }
 
             String content = bos.toString();
             bos.close();
             fis.close();
             return content;
         } catch (FileNotFoundException e) {
             e.printStackTrace();
             return null;
         } catch (IOException e) {
             e.printStackTrace();
             return null;
         }
     }
 
     /**
      * 判断指定文件是否在另一个文件夹中 当childPath和parentPath相同时也表示childPath在parentPath中
      *
      * @param parentPath
      *            父文件夹路径
      * @param childPath
      *            指定的文件路径
      * @return true:表示在parentPath中，false:表示不在parentPath中
      */
     public static boolean isFileAncestorOf(String parentPath, String childPath) {
         if (!parentPath.endsWith(File.separator)) {
             parentPath += File.separator;
         }
         if (!childPath.endsWith(File.separator)) {
             childPath += File.separator;
         }
         return childPath.startsWith(parentPath);
     }
 
     /**
      * 拷贝xface.js到新安装应用所在目录
      *
      * @param context
      *            android程序运行时上下文
      * @param targetDir
      *            拷贝xface.js的目的目录（绝对路径）
      */
     public static void copyEmbeddedJsFile(Context context, String targetDir) {
         try {
             int id = context.getResources().getIdentifier(
                     XConstant.XFACE_JS_FILE_NAME.substring(0,
                             XConstant.XFACE_JS_FILE_NAME.length() - 3),
                     FILE_DIRECTORY, context.getPackageName());
             InputStream is = context.getResources().openRawResource(id);
             File jsFile = new File(targetDir, XConstant.XFACE_JS_FILE_NAME);
             createFileByData(jsFile.getAbsolutePath(), is);
             is.close();
         } catch (IOException e) {
             e.printStackTrace();
         } catch (NotFoundException e) {
             XLog.e(CLASS_NAME,
                     "Can't find resource file xface.js in res/raw folder!");
             e.printStackTrace();
         }
     }
 
     /**
      * 拷贝xdebug.js到新安装应用所在目录
      *
      * @param context
      *            android程序运行时上下文
      * @param targetDir
      *            拷贝xdebug.js的目的目录（绝对路径）
      */
     public static void copyEmbeddedDebugJsFile(Context context, String targetDir) {
         // todo:将与应用相关的接口集中放在一起
         try {
             int id = context.getResources().getIdentifier(
                     XConstant.DEBUG_JS_FILE_NAME.substring(0,
                             XConstant.DEBUG_JS_FILE_NAME.length() - 3),
                     FILE_DIRECTORY, context.getPackageName());
             InputStream is = context.getResources().openRawResource(id);
             File jsFile = new File(targetDir, XConstant.DEBUG_JS_FILE_NAME);
             createFileByData(jsFile.getAbsolutePath(), is);
             is.close();
         } catch (IOException e) {
             e.printStackTrace();
         } catch (NotFoundException e) {
             XLog.e(CLASS_NAME,
                     "Can't find resource file xdebug.js in res/raw folder!");
             e.printStackTrace();
         }
     }
 
     /**
      * 在xFace的wordDir下创建.nomedia文件
      */
     public static void createNoMediaFileInWorkDir() {
         File file = new File(XConfiguration.getInstance().getWorkDirectory(),
                 NO_MEDIA_FILE_NAME);
         try {
             if (!file.exists()) {
                 file.createNewFile();
             }
         } catch (IOException e) {
             XLog.e(CLASS_NAME, "create .nomedia in workDir failed.");
         }
     }
 
     /**
      * 以字符方式写文件
      *
      * @param filePath
      *            文件的绝对路径
      * @param fileContent
      *            要写的内容
      * @return 成功返回true，失败返回false
      */
     public static Boolean writeFileByByte(String filePath, byte[] fileContent) {
         try {
             if (null == filePath || null == fileContent) {
                 return false;
             }
             FileOutputStream fileOutputStream = new FileOutputStream(filePath);
             fileOutputStream.write(fileContent);
             fileOutputStream.flush();
             fileOutputStream.close();
         } catch (IOException e) {
             e.printStackTrace();
             XLog.d(CLASS_NAME, e.getMessage());
             return false;
         }
         return true;
     }
 
     /**
      * 以字符串的方式写文件
      *
      * @param filePath
      *            文件的绝对路径
      * @param fileContent
      *            要写的文件内容
      * @return 成功返回true，失败返回false
      */
     public static boolean writeFileByString(String filePath, String fileContent) {
         if (null == filePath || null == fileContent) {
             return false;
         }
         try {
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                     new FileOutputStream(filePath));
             outputStreamWriter.write(fileContent, 0, fileContent.length());
             outputStreamWriter.flush();
             outputStreamWriter.close();
         } catch (IOException e) {
             e.printStackTrace();
             XLog.d(CLASS_NAME, e.getMessage());
             return false;
         }
         return true;
     }
 
     /**
      * 以字节流的方式读文件
      *
      * @param filePath
      *            文件的绝对路径
      * @return 成功则返回文件内容的字节流，否则为null
      */
     public static byte[] readBytesFromFile(String filePath) {
         if (null == filePath) {
             return null;
         }
         InputStream is = null;
         try {
             is = new FileInputStream(filePath);
             return XUtils.readBytesFromInputStream(is);
         } catch (Exception e) {
             XLog.d(CLASS_NAME, "readFileByByte:" + e.getMessage());
             e.printStackTrace();
             return null;
         }
     }
 
     /**
      * 判断文件是否存在
      *
      * @param filePath
      *            [in] 文件路径
      *
      * @return
      * */
     public static boolean fileExists(Context context, String filePath) {
         if (null == filePath) {
             return false;
         }
         String absPath = null;
         if (filePath.startsWith(XConstant.ASSERT_PROTACAL)) {
             absPath = filePath.substring(XConstant.ASSERT_PROTACAL.length());
             try {
                 InputStream is = context.getAssets().open(absPath);
                 if (is != null) {
                     return true;
                 }
             } catch (IOException e) {
                 return false;
             }
         } else if (filePath.startsWith(XConstant.FILE_SCHEME)) {
             File file = new File(filePath.substring(XConstant.FILE_SCHEME
                     .length()));
             if (file.exists()) {
                 return true;
             }
         }
         return false;
     }
 }
