 package com.xengine.android.utils;
 
 import android.content.Context;
 import android.os.Build;
 import android.os.Environment;
 import android.os.storage.StorageManager;
 import android.text.TextUtils;
 
 import java.io.*;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * <pre>
  * 设备根路径相关的工具类。
  * User: jasontujun
  * Date: 14-6-3
  * Time: 下午5:03
  * </pre>
  */
 public class XRootUtil {
     private static final String TAG = XRootUtil.class.getSimpleName();
     private static final String TMPFS = "tmpfs";
 
     /**
      * 通过Environment.getExternalStorage()获取外部存储根路径
      * @return 如果没有存储设备，返回null；否则返回外部存储路径
      */
     public static String getRootByApi() {
         if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
             File file = Environment.getExternalStorageDirectory();
             if (file == null)
                 return null;
             String path = file.getAbsolutePath();
             if (isValidRoot(path))
                 return path;
         }
         return null;
     }
 
     /**
      * 通过反射getVolumePath()放射，获取所有外部存储的路径。
      * 注：
      * 1.Android 3.2及以上，StorageManager才有getVolumePath()这个方法
      * 2.Android 2.3及以上，才可以调用这个方法；否则返回null
      * @param context
      * @return 如果是Android 2.3，返回null；否则当前返回所有外部存储的根路径
      */
     public static List<String> getRootsByReflection(Context context) {
         // Android 2.3以下，没有StorageManager，所以无法执行下面的逻辑
         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
             return null;
 
         XLog.d(TAG, "getRootsByReflection()");
         StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
         try {
             String[] paths = (String[]) sm.getClass()
                     .getMethod("getVolumePaths", null)
                     .invoke(sm, null);
             if (paths == null)
                 return null;
 
             List<String> results = new ArrayList<String>();
             for (String path : paths) {
                 if (isValidRoot(path))
                     results.add(path);
             }
             return results;
         } catch (IllegalAccessException e) {
             XLog.d(TAG, "IllegalAccessException!!!");
             e.printStackTrace();
         } catch (InvocationTargetException e) {
             XLog.d(TAG, "InvocationTargetException!!!");
             e.printStackTrace();
         } catch (NoSuchMethodException e) {
             XLog.d(TAG, "NoSuchMethodException!!!");
             e.printStackTrace();
         }
         return null;
     }
 
     /**
      * 通过Linux的df和mount命令，经过筛选排除，获取所有外部存储的路径。
      * 注：由于一些山寨手机，无法通过getExternalStorage()等方法获取外部存储的路径
      * @return 返回所有外部存储的根路径
      */
     public static List<String> getRootsByCmd() {
         XLog.d(TAG, "getRootsByCmd()");
         List<String> dfPaths = new ArrayList<String>();
         Map<String, String> devPathMap = new HashMap<String, String>();
 
         // 通过DF命令来获取可用路径。 DF命令：检查文件系统的磁盘空间占用情况
         // m1手机，df命令第一列不是挂载点路径
         Runtime runtime = Runtime.getRuntime();
         Process dfProcess = null;
         try {
             XLog.d(TAG, ">df...................");
             dfProcess = runtime.exec("df");
             InputStream input = dfProcess.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(input));
             String strLine;
             while ((strLine = br.readLine()) != null) {
                 if (TextUtils.isEmpty(strLine))
                     continue;
                 XLog.d(TAG, ">" + strLine);
                 // 取出df命令第一列的路径名
                 String path = strLine;
                 int splitIndex = strLine.indexOf(" ");
                 if (splitIndex > 0)
                     path = strLine.substring(0, splitIndex);
                 if (path.length() > 1) {
                     // 去除结尾异常字符
                     char c = path.charAt(path.length() - 1);
                     if (!Character.isLetterOrDigit(c) && c != '-' && c != '_')
                         path = path.substring(0, path.length() - 1);
                     // 判断该路径是否存在并可写
                     if (isValidRoot(path) && !dfPaths.contains(path))
                         dfPaths.add(path);
                 }
             }
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             try {
                 if (dfProcess != null)
                     dfProcess.destroy();
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         for (String df : dfPaths)
             XLog.d(TAG, "df-result: " + df);
 
         // 用mount命令去除dfPaths中的属性为tmpfs的路径，并生成devPathMap
         Process mountProcess = null;
         try {
             XLog.d(TAG, ">mount...................");
             mountProcess = runtime.exec("mount");
             InputStream input = mountProcess.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(input));
             String strLine;
             while (null != (strLine = br.readLine())) {
                 if (TextUtils.isEmpty(strLine))
                     continue;
                 XLog.d(TAG, ">" + strLine);
                 // 判断mount这一行是否含有df中的路径
                 int indexOfDfName = getIndexOfDfNames(dfPaths, strLine);
                 if (indexOfDfName == -1)
                     continue;
                 // mount这一行路径为tmpfs,则去除dfPaths中该path
                 if (strLine.contains(TMPFS)) {
                     dfPaths.remove(indexOfDfName);
                 }
                 // 否则，该path为有效的，添加进devPathMap
                 else {
                     String path = dfPaths.get(indexOfDfName);
                     int index = strLine.indexOf(" ");
                     if (index != -1) {
                         String devName = strLine.substring(0, index);
                         if (!devPathMap.containsKey(devName))
                             devPathMap.put(devName, path);
                         else {
                             // 如果同一设备挂载点有多个，则保留路径名短的挂载点
                             String sameDfName = devPathMap.get(devName);
                             if (path.length() < sameDfName.length())
                                 devPathMap.put(devName, path);
                         }
                     }
                 }
             }
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             try {
                 if (mountProcess != null)
                     mountProcess.destroy();
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
 
         // 返回结果
         List<String> results = new ArrayList<String>(devPathMap.values());
         for (String result : results)
             XLog.d(TAG, "mount-result: " + result);
         return results;
     }
 
     /**
      * 根据当前的mount命令结果的一行，找到对应的dfPaths中的索引
      * @param mountLine 当前的mount命令行
      * @return 找到则返回对应index，否则返回-1
      */
     private static int getIndexOfDfNames(List<String> dfPaths, String mountLine) {
         String[] mountColumns = mountLine.split(" ");
         for (int i = 0; i < dfPaths.size(); i++) {
             String path = dfPaths.get(i);
             boolean match = false;
             for (String mountColumn : mountColumns) {
                 if (mountColumn.equals(path))
                     match = true;
             }
             if (match)
                 return i;
         }
         return -1;
     }
 
     /**
      * 判断指定路径是否从属app专属存储大文件的路径
      * @param context app的Context
      * @param path 指定路径
      * @return 如果是返回true；否则返回false
      */
     public static boolean isAppFilesPath(Context context, String path) {
         return !TextUtils.isEmpty(path) &&
                 path.contains("/Android/data/" + context.getPackageName() + "/files");
     }
 
     /**
      * 判断指定路径是否从属app专属存储临时文件的路径
      * @param context app的Context
      * @param path 指定路径
      * @return 如果是返回true；否则返回false
      */
     public static boolean isAppCachePath(Context context, String path) {
         return !TextUtils.isEmpty(path) &&
                 path.contains("/Android/data/" + context.getPackageName() + "/cache");
     }
 
     /**
      * 将根路径转换成app专属的存储大文件的路径。
      * 格式：/root/Android/data/"package name"/files
      * @param context app的Context
      * @param root 外部存储的根路径
      * @return 返回app专属的存储大文件的路径
      * @see android.content.Context#getExternalFilesDir(String)
      */
     public static String root2AppFilesPath(Context context, String root) {
         if (context == null || TextUtils.isEmpty(root))
             return null;
         return root + "/Android/data/" + context.getPackageName() + "/files";
     }
 
     /**
      * 将根路径转换成app专属的存储临时文件的路径。
      * 格式：/root/Android/data/"package name"/cache
      * @param context app的Context
      * @param root 外部存储的根路径
      * @return 返回app专属的存储临时文件的路径
      * @see android.content.Context#getExternalCacheDir()
      */
     public static String root2AppCachePath(Context context, String root) {
         if (context == null || TextUtils.isEmpty(root))
             return null;
         return root + "/Android/data/" + context.getPackageName() + "/cache";
     }
 
     /**
      * 将app专属的存储大文件的路径转换成根路径。
      * 格式：/root/Android/data/"package name"/files -> /root
      * @param context app的Context
      * @param path 外部存储的根路径
      * @return 返回app专属的存储大文件的路径
      * @see android.content.Context#getExternalFilesDir(String)
      */
     public static String appFilesPath2Root(Context context, String path) {
         if (context == null || TextUtils.isEmpty(path))
             return path;
         String suffix = "/Android/data/" + context.getPackageName() + "/files";
         return path.replace(suffix, "");
     }
 
     /**
      * 将app专属的存储临时文件的路径转换成根路径。
      * 格式：/root/Android/data/"package name"/cache -> /root
      * @param context app的Context
      * @param path 外部存储的根路径
      * @return 返回app专属的存储临时文件的路径
      * @see android.content.Context#getExternalCacheDir()
      */
     public static String appCachePath2Root(Context context, String path) {
         if (context == null || TextUtils.isEmpty(path))
             return path;
         String suffix = "/Android/data/" + context.getPackageName() + "/cache";
         return path.replace(suffix, "");
     }
 
     /**
      * 测试某个路径下IO读写操作是否正常。
      * 新建一个test.dat文件，写一段数据，再读出来，比较，最后删掉。
      * 如果这一系列操作都成功，则表示正常，否则不正常。
      * @param path 测试读写的路径
      * @return 如果IO正常，返回true；否则返回false
      */
     public static boolean isIOWorks(String path) {
         if (TextUtils.isEmpty(path))
             return false;
 
         File dir = new File(path);
         if (!dir.exists())
             dir.mkdirs();
         File testFile = new File(dir, "test.dat");
         String testStr = "test";
        // 写数据进文件
        if (!XFileUtil.string2File(testStr, testFile))
            return false;
        // 从文件中读数据
        String readStr = XFileUtil.file2String(testFile);
        if (readStr == null)
            return false;
        // 比较字符串内容
        if (!testStr.equals(readStr))
            return false;
        // 删除测试文件
        return testFile.delete();
     }
 
     /**
      * 判断该根路径是否合法可用。
      * @param root 根路径
      * @return 如果合法可用，则返回true；否则返回false
      */
     public static boolean isValidRoot(String root) {
         if (TextUtils.isEmpty(root))
             return false;
         File rootFile = new File(root);
         return rootFile.exists() && rootFile.canRead() && rootFile.canWrite();
     }
 
     /**
      * 判断某个路径path是否是已知路径集合中的软链接。
      * @param path 待判断的路径
      * @param rootPaths 已知路径集合
      * @return 如果是软链接，返回链接的目的地址；否则，返回null
      */
     public static String isSoftLink(String path, List<String> rootPaths) {
         if (TextUtils.isEmpty(path) || rootPaths == null || rootPaths.size() == 0)
             return null;
 
         // softTag后面就是软连接的目的路径
         File file = new File(path);
         String softTag = file.getName() + " ->";
         // 执行“ls -l”命令
         Runtime runtime = Runtime.getRuntime();
         try {
             String cmd = "ls -l " + path;
             XLog.d(TAG, ">" + cmd);
             Process process = runtime.exec(cmd);
             InputStream input = process.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(input));
             String strLine;
             while ((strLine = br.readLine()) != null) {
                 if (TextUtils.isEmpty(strLine))
                     continue;
                 XLog.d(TAG, ">" + strLine);
                 int index = strLine.indexOf(softTag);
                 XLog.d(TAG, ">____softTag index:" + index);
                 if (index == -1)
                     continue;
                 int softLinkIndex = index + softTag.length();
                 if (softLinkIndex >= strLine.length())
                     continue;
                 String softLinkPath = strLine.substring(softLinkIndex);//
                 softLinkPath = softLinkPath.replace(" ", "");// 去掉空格
                 XLog.d(TAG, ">____softLinkPath:" + softLinkPath);
                 if (rootPaths.contains(softLinkPath))
                     return softLinkPath;// 是软连接
             }
             return null;
         } catch (IOException e) {
             e.printStackTrace();
             return null;
         }
     }
 }
