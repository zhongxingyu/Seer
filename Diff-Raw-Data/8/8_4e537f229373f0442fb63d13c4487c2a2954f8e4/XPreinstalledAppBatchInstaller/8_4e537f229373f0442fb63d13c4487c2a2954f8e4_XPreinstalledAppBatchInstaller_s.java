 
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
 
 package com.polyvi.xface.ams;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.polyvi.xface.app.XAppInfo;
 import com.polyvi.xface.app.XIApplication;
 import com.polyvi.xface.configXml.XPreInstallPackageItem;
 import com.polyvi.xface.core.XConfiguration;
 import com.polyvi.xface.core.XISystemContext;
 import com.polyvi.xface.core.XRuntime;
 import com.polyvi.xface.util.XAppUtils;
 import com.polyvi.xface.util.XAssetsFileUtils;
 import com.polyvi.xface.util.XConstant;
 import com.polyvi.xface.util.XLog;
 
 /**
  * 负责所有app的预装
  *
  */
 public class XPreinstalledAppBatchInstaller extends XAbstractPreIntaller {
 
     private static final String CLASS_NAME = XPreinstalledAppBatchInstaller.class
             .getSimpleName();
     protected XISystemContext mContext;
     protected XRuntime mRuntime;
     protected XAMSComponent mAms;
 
     public XPreinstalledAppBatchInstaller(XISystemContext ctx,
             XAMSComponent ams, XRuntime runtime, XIPreInstallListener listener) {
         super(listener);
         mContext = ctx;
         mRuntime = runtime;
         mAms = ams;
     }
 
     /**
      * 获得icon的路径
      *
      * @param app
      * @return
      */
     private String getIconPathInAsset(XIApplication app) {
         return XConstant.PRE_INSTALL_SOURCE_ROOT + app.getAppId()
                 + File.separator + app.getAppInfo().getIcon();
     }
 
     @Override
     public boolean install() {
         // 安装预装app
         List<XPreInstallPackageItem> apps = getPreInstallAppItems();
         for (XPreInstallPackageItem app : apps) {
             XIApplication application = buildApplication(app);
             if ( null == application ) {
                 continue;
             }
             File destFile = new File(XAppUtils.generateAppIconPath(
                     application.getAppId(), application.getAppInfo().getIcon()));
             XAssetsFileUtils
                     .copyAssetsToTarget(mContext.getContext(),
                             getIconPathInAsset(application),
                             destFile.getAbsolutePath());
         }
         return true;
     }
 
     private List<XPreInstallPackageItem> getPreInstallAppItems() {
         List<XPreInstallPackageItem> apps = new ArrayList<XPreInstallPackageItem>();
         List<XPreInstallPackageItem> items = XConfiguration.getInstance()
                 .getPreinstallPackages();
         if (null == items) {
             return apps;
         }
         for (int i = 1; i < items.size(); i++) {
             apps.add(items.get(i));
         }
         return apps;
     }
 
     /**
      * 构建一个application
      *
      * @param app
      * @return
      */
     final protected XIApplication buildApplication(XPreInstallPackageItem app) {
         // startapp 在asset目录下
         String appDirNameInAsset = XConstant.PRE_INSTALL_SOURCE_ROOT
                 + app.packageName;
         // 解析app的app.xml
         InputStream is = null;
         try {
             is = mContext
                     .getContext()
                     .getAssets()
                     .open(appDirNameInAsset + File.separator
                             + XConstant.APP_CONFIG_FILE_NAME);
         } catch (IOException e) {
             e.printStackTrace();
             XLog.e(CLASS_NAME, "parse app.xml error.");
             return null;
         }
         XAppInfo info = XAppUtils.parseAppXml(is);
         if (null == info) {
             XLog.e(CLASS_NAME, "Preinstalled app app.xml config error.");
             return null;
         }
         if (!app.appId.equals(info.getAppId())) {
             XLog.e(CLASS_NAME,
                     "appId in app.xml not match to appId in config.xml");
             return null;
         }
        return buildApplication(info, XConstant.ASSERT_PROTACAL + appDirNameInAsset);
     }
 
     private XIApplication buildApplication(XAppInfo info,
             String appDirNameInAsset) {
         XIApplication buildingApp = mAms.getAppById(info.getAppId());
         if (null != buildingApp) {
            info.setSrcRoot(appDirNameInAsset);
             mAms.updateApp(info, buildingApp);
         } else {
             buildingApp = mAms.getAppManagement().getAppCreator().create(info);
            buildingApp.getAppInfo().setSrcRoot(appDirNameInAsset);
             mAms.add(buildingApp);
         }
 
         // 解析完后将app.xml copy到对应app目录下
         String targetPath = getAppRoot(buildingApp.getAppId())
                 + XConstant.APP_CONFIG_FILE_NAME;
         boolean ret = XAssetsFileUtils.copyAssetsToTarget(mContext.getContext(),
                 appDirNameInAsset + File.separator
                         + XConstant.APP_CONFIG_FILE_NAME, targetPath);
         if (!ret) {
             XLog.e(CLASS_NAME, "copy app.xml error");
             return null;
         }
         XAppUtils.initAppData(mContext.getContext(), buildingApp.getAppId());
         return buildingApp;
     }
 
     private String getAppRoot(String appId) {
         return XConfiguration.getInstance().getAppInstallDir() + appId
                 + File.separator;
     }
 
 }
