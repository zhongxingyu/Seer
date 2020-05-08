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
 
 package com.polyvi.xface.core;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.os.Build;
 import android.os.Looper;
 
 import com.polyvi.xface.XStartParams;
 import com.polyvi.xface.configXml.XPreInstallPackageItem;
 import com.polyvi.xface.configXml.XSysConfigInfo;
 import com.polyvi.xface.configXml.XSysConfigParser;
 import com.polyvi.xface.configXml.XTagNotFoundException;
 import com.polyvi.xface.util.XConstant;
 import com.polyvi.xface.util.XFileUtils;
 import com.polyvi.xface.util.XLog;
 import com.polyvi.xface.util.XStrings;
 
 /**
  * 负责所有配置的管理，单例
  */
 public class XConfiguration {
 
     private static final String CLASS_NAME = XConfiguration.class
             .getSimpleName();
 
     private static final String APPS_FOLDER_NAME = "apps";
 
     /** 系统数据路径 */
     public static final String SYS_DATA_DIR_NAME = "sys_data";
 
     private static final int TAG_SYSTEM_EXIT_CODE = 1;
 
     private boolean mWorkDirectoryChanged = false;
     // 解析config.xml得到的configInfo对象
     private XSysConfigInfo mSysConfigInfo;
 
     // 表示工作目录的三种配置方式
     private enum WorkDirConfig {
         TAG_MAIN_MEMORY_ONLY, // 仅手机内存
         TAG_EXTERNAL_MEMORY_CARD_ONLY, // 仅外部存储（FlashROM及SD/TF扩展卡）
         TAG_EXTERNAL_MEMORY_CARD_FIRST// 外部存储优先
     };
 
     // 表示最终工作目录配置的配置 目前有两种表示方式
     static public enum WorkDirStrategy {
         MEMORY, SDCARD
     };
 
     /** singleton */
     private static XConfiguration instance = null;
 
     /** 工作目录路径 */
     private String mWorkDir;
 
     /** 是否支持点击view上的数字触发联系人添加操作 */
     private boolean mTelLinkEnabled = true;
 
     private XConfiguration() {
     }
 
     public static XConfiguration getInstance() {
         if (instance == null) {
             instance = new XConfiguration();
         }
         return instance;
     }
 
     /**
      * 从配置文件读取系统配置数据
      *
      * @param context
      * @throws XTagNotFoundException
      */
     public void readConfig(Context context) throws XTagNotFoundException {
         XSysConfigParser sysConfigParser = new XSysConfigParser(context);
         mSysConfigInfo = sysConfigParser.parseConfig();
     }
 
     /**
      * 设置程序的工作空间
      *
      * @param workDir
      *            工作空间绝对路径
      */
     public void setWorkDirectory(String workDir) {
         this.mWorkDir = workDir;
     }
 
     /**
      * 获取所有的application安装目录
      *
      * @return 所有application安装目录绝对路径
      */
     public String getAppInstallDir() {
         return this.mWorkDir + APPS_FOLDER_NAME + File.separator;
     }
 
     /**
      * 获取sys_data目录
      *
      * @return sys_data目录绝对路径
      */
     public String getSysDataDir() {
         return this.getAppInstallDir() + SYS_DATA_DIR_NAME + File.separator;
     }
 
     /**
      * 获取程序的工作空间
      *
      * @return 工作空间绝对路径
      */
     public String getWorkDirectory() {
         return this.mWorkDir;
     }
 
     /**
      * 获取预安装的应用包列表，每一项代表一个应用的包名
      */
     public List<XPreInstallPackageItem> getPreinstallPackages() {
         return (null == mSysConfigInfo) ? null : mSysConfigInfo
                 .getPreinstallPackages();
     }
 
     /**
      * 获取startapp的id
      *
      * @param sysCtx
      *            系统上下文环境
      * @return startAppId
      */
     public String getStartAppId(XISystemContext sysCtx) {
         XStartParams params = sysCtx.getStartParams();
         if (null != params && null != params.appId) {
             // 如果系统指定了启动的app则从启动参数中读取appid,packagename
             // 并新建一个XPreInstallPackageItem
             return params.appId;
         }
         return (null == mSysConfigInfo) ? null : mSysConfigInfo.getStartAppId();
     }
 
     /**
      * 从config.xml配置文件中读取LOG输出等级
      */
     public String readLogLevel() {
         return (null == mSysConfigInfo) ? null : mSysConfigInfo.getLogLevel();
     }
 
     /**
      * 从config.xml配置文件中读取是否需要自动隐藏splash图片
      *
      * @return
      */
     public boolean readAutoHideSplash() {
         return (null == mSysConfigInfo) ? true : mSysConfigInfo
                 .getAutoHideSplash();
     }
 
     /**
      * 从config.xml配置文件中读取引擎版本
      *
      * @return
      */
     public String readEngineVersion() {
         return (null == mSysConfigInfo) ? null : mSysConfigInfo
                 .getEngineVersion();
     }
 
     /**
      * 从config.xml配置文件中读取build号
      *
      * @return
      */
     public String readEngineBuild() {
         return (null == mSysConfigInfo) ? null : mSysConfigInfo
                 .getEngineBuild();
     }
 
     /**
      * 从config.xml配置文件中读取加载应用时等待xface.js是否加载完成的时间
      *
      * @return
      */
     public String readLoadUrlTimeout() {
         return (null == mSysConfigInfo) ? null : mSysConfigInfo
                 .getLoadUrlTimeout();
     }
 
     /** 因为只加载config.xml配置文件中xFace标签中的内容，该方法用于判断是否到了配置文件结尾或者遇到xFace结束标签 */
 
     /**
      * 配置系统的工作目录
      *
      * @param context
      *            android程序对应的Context对象
      * @param workDirName
      *            工作目录名称
      */
     public void configWorkDirectory(Context context, String workDirName) {
         if (null == workDirName) {
             alerExitMessage(
                     XStrings.getInstance().getString(
                             XStrings.EXIT_MESSAGE_TITLE),
                     XStrings.getInstance().getString(
                             XStrings.EXIT_MESSAGE_CONTENT), context);
         } else {
             // 设置系统的工作目录到config中
             setWorkDirectory(workDirName);
         }
     }
 
     /**
      * 获取程序的工作空间目录,'/'结尾
      *
      * @param context
      *            android程序对应的Context对象
      * @param workDirName
      *            工作目录名称
      */
     public String getWorkDirectory(Context context, String workDirName) {
         String dirType = mSysConfigInfo.getWorkDir();
         String baseDir = null;
         int work_dir_config;
         WorkDirConfig configType;
         try {// 捕获用户所有可能的错误配置输入
             work_dir_config = Integer.parseInt(dirType);
             configType = WorkDirConfig.values()[--work_dir_config];
         } catch (Exception e) {
             return null;
         }
         // FIXME：如果android版本是3.2则直接安装在系统内存中
         if (Build.VERSION.SDK_INT == 13) {
             configType = WorkDirConfig.TAG_MAIN_MEMORY_ONLY;
         }
         switch (configType) {
         case TAG_MAIN_MEMORY_ONLY:
             try {
                 baseDir = context.getFilesDir().getCanonicalPath();
                 setWorkDirStrategy(context, WorkDirStrategy.MEMORY);
             } catch (IOException e) {
                 XLog.e(CLASS_NAME,
                         "error when get work directory:" + e.getMessage());
                 e.printStackTrace();
             }
             break;
         case TAG_EXTERNAL_MEMORY_CARD_ONLY: {// 如果外部存储卡不可用,则报错,并退出程序。
             if (null == (baseDir = XFileUtils.getSdcardPath())) {// 外存卡不可用
                 return null;
             }
             workDirName = XConstant.ANDROID_DIR + File.separator
                     + XConstant.APP_DATA_DIR_NAME + File.separator
                     + workDirName;
             setWorkDirStrategy(context, WorkDirStrategy.SDCARD);
         }
             break;
         case TAG_EXTERNAL_MEMORY_CARD_FIRST: {
             if (null == (baseDir = XFileUtils.getSdcardPath())) {// 外存卡不可用
                 try {
                     baseDir = context.getFilesDir().getCanonicalPath();
                     setWorkDirStrategy(context, WorkDirStrategy.MEMORY);
                 } catch (IOException e) {
                     XLog.e(CLASS_NAME,
                             "error when getWorkDirectory:" + e.getMessage());
                     e.printStackTrace();
                 }
             } else {
                 workDirName = XConstant.ANDROID_DIR + File.separator
                         + XConstant.APP_DATA_DIR_NAME + File.separator
                         + workDirName;
                 setWorkDirStrategy(context, WorkDirStrategy.SDCARD);
             }
         }
             break;
         }
         StringBuffer sb = new StringBuffer();
         sb.append(baseDir);
         if (!baseDir.endsWith(File.separator)) {
             sb.append(File.separatorChar);
         }
         sb.append(workDirName);
         sb.append(File.separatorChar);
         // 工作目录，例如:内存卡到/data/data/com.polyvi.xface/files/com.polyvi.xface/
         String workDir = sb.toString();
         // xface3工作目录，例如:内存卡到/data/data/com.polyvi.xface/files/com.polyvi.xface/xface3/
         File xface3Dir = new File(sb.toString(),
                 XConstant.PRE_INSTALL_SOURCE_ROOT);
         if (!xface3Dir.exists()) {
             xface3Dir.mkdirs();
             // 修改文件夹的权限为其它用户可执行
             XFileUtils.setPermission(XFileUtils.EXECUTABLE_BY_OTHER, workDir);
            XFileUtils.setPermissionUntilDir(XFileUtils.EXECUTABLE_BY_OTHER,
                    xface3Dir.getAbsolutePath(), "/");
         }
         return workDir;
     }
 
     /**
      * 设置工作目录配置策略
      *
      * @param ctx
      * @param wds
      */
     private void setWorkDirStrategy(Context ctx, WorkDirStrategy wds) {
         if (getWorkDirStrategy(ctx) != wds.ordinal()) {
             mWorkDirectoryChanged = true;
         }
 
         SharedPreferences pref = ctx.getSharedPreferences(
                 XConstant.PREF_SETTING_FILE_NAME, Context.MODE_WORLD_READABLE
                         | Context.MODE_WORLD_WRITEABLE);
         SharedPreferences.Editor editor = pref.edit();
         editor.putInt(XConstant.TAG_WD_STRATEGY, wds.ordinal());
         editor.commit();
     }
 
     /**
      * 工作目录是否变化的标志
      *
      * @return
      */
     public boolean isWorkDirectoryChanged() {
         return mWorkDirectoryChanged;
     }
 
     /**
      * 获得工作目录最终的配置策略
      *
      * @return
      */
     private int getWorkDirStrategy(Context ctx) {
         SharedPreferences pref = ctx.getSharedPreferences(
                 XConstant.PREF_SETTING_FILE_NAME, Context.MODE_WORLD_READABLE
                         | Context.MODE_WORLD_WRITEABLE);
         int ret = pref.getInt(XConstant.TAG_WD_STRATEGY, -1);
         return ret;
     }
 
     /**
      * 弹出错误消息,用户点击后程序退出
      *
      * @param title
      *            弹出框标题
      * @param exitMessage
      *            弹出框的错误信息
      * @return
      */
     private void alerExitMessage(String title, String exitMessage,
             Context context) {
         AlertDialog.Builder builder = new Builder(context);
         builder.setTitle(title);
         builder.setPositiveButton(
                 XStrings.getInstance().getString(XStrings.CONFIRM),
                 new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         System.exit(TAG_SYSTEM_EXIT_CODE);
                     }
                 });
         builder.setIcon(android.R.drawable.ic_dialog_info);
         builder.setMessage(exitMessage);
         builder.show();
         // 将对话框变成阻塞式对话框
         Looper.loop();
     }
 
     /** 设置该应用是否支持点击view上的数字触发联系人添加操作 */
     public void setTelLinkEnabled(boolean telLinkEnabled) {
         this.mTelLinkEnabled = telLinkEnabled;
     }
 
     /** 返回该应用是否支持点击view上的数字触发联系人添加操作 */
     public boolean isTelLinkEnabled() {
         return mTelLinkEnabled;
     }
 
     /**
      * 加载平台的string常量
      *
      * @param context
      */
     public void loadPlatformStrings(Context context) {
         XStrings.getInstance().loadPlatformStrings(context);
     }
 
     public String getOfflineCachePath() {
         return getSysDataDir() + XConstant.APP_CACHE_PATH;
     }
 }
