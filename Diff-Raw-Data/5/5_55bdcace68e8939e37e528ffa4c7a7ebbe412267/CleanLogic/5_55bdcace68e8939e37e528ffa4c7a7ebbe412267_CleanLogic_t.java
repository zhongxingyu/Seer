 package org.etp.portalKit.clean.logic;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.etp.portalKit.clean.bean.CleanCommand;
 import org.etp.portalKit.clean.bean.CleanItems;
 import org.etp.portalKit.common.service.PropertiesManager;
 import org.etp.portalKit.common.util.FileUtils;
 import org.etp.portalKit.setting.bean.SettingsCommand;
 import org.springframework.stereotype.Component;
 
 /**
  * The purpose of this class is to provide a logic layer to handle all the
  * operations of clean functionality.
  */
 @Component(value = "cleanLogic")
 public class CleanLogic {
 
     @Resource(name = "propertiesManager")
     private PropertiesManager prop;
 
     private String WIDGET_CACHE_RELATIVE_PATH = "AppData\\Local\\Opera";
     private String WIDGET_HISTORY_RELATIVE_PATH = "AppData\\Roaming\\Opera";
     private String WIDGET_PREFIX = "Widget MSDK Widget";
 
     private String WAR_FILE_PREFIX1 = "portal-";
     private String WAR_FILE_PREFIX2 = "public";
     private String WAR_FILE_PREFIX3 = "private";
     private String WAR_FILE_PREFIX4 = "static";
 
     /**
      * Retrieve all the clean needed stuff. According to user.home and set
      * tomcat path.
      * 
      * @return CleanItems
      */
     public CleanItems retrieveCleanItems() {
         CleanItems items = new CleanItems();
         String userHome = System.getProperty("user.home");
         String webappsHome = prop.get(SettingsCommand.TOMCAT_WEBAPPS_PATH);
         if (StringUtils.isBlank(userHome)) {
             throw new NullPointerException(
                     "user.home could not be null or empty.");
         }
         if (StringUtils.isBlank(webappsHome)) {
             return items;
         }
 
         File cacheBase = new File(userHome, WIDGET_CACHE_RELATIVE_PATH);
         String operaAbsPath = cacheBase.getAbsolutePath();
         File[] caches = FileUtils.FolderFinder(operaAbsPath, WIDGET_PREFIX);
         List<String> widgetCaches = new ArrayList<String>();
         for (File file : caches) {
             widgetCaches.add(file.getName());
         }
         items.setWidgetCaches(widgetCaches);
         File[] portalWars = FileUtils.FileFinder(webappsHome, WAR_FILE_PREFIX1,
                 ".war");
         File[] publicWars = FileUtils.FileFinder(webappsHome, WAR_FILE_PREFIX2,
                 ".war");
         File[] privateWars = FileUtils.FileFinder(webappsHome,
                 WAR_FILE_PREFIX3, ".war");
         File[] staticWars = FileUtils.FileFinder(webappsHome, WAR_FILE_PREFIX4,
                 ".war");
 
         List<String> warFiles = new ArrayList<String>();
         for (File portal : portalWars) {
             warFiles.add(portal.getName());
         }
         for (File pub : publicWars) {
             warFiles.add(pub.getName());
         }
         for (File pri : privateWars) {
             warFiles.add(pri.getName());
         }
         for (File sta : staticWars) {
             warFiles.add(sta.getName());
         }
         items.setWarFiles(warFiles);
         return items;
     }
 
     /**
      * Delete an item with its specified type and item name.
      * 
      * @param cmd
      *            cmd.cleanItem item name which will be used to delete
      *            cmd.cleanType item type which will be used to delete
      * @return true if delete successfully. Otherwise, false.
      */
     public boolean cleanItem(CleanCommand cmd) {
         String userHome = System.getProperty("user.home");
         String webappsHome = prop.get(SettingsCommand.TOMCAT_WEBAPPS_PATH);
         if (StringUtils.isBlank(userHome)) {
             throw new NullPointerException(
                     "user.home could not be null or empty.");
         }
         if (StringUtils.isBlank(webappsHome)) {
             throw new NullPointerException(
                     "You haven't set tomcat's webapps path yet.");
         }
 
         String type = cmd.getCleanType();
         String item = cmd.getCleanItem();
         if (StringUtils.isBlank(type)) {
             throw new NullPointerException(
                     "cleanType could not be null or empty.");
         }
         if (StringUtils.isBlank(item)) {
             throw new NullPointerException("item could not be null or empty.");
         }
         boolean isDeleteSuc = true;
         if ("widget".equals(type)) {
             File cacheBase = new File(userHome, WIDGET_CACHE_RELATIVE_PATH);
             File historyBase = new File(userHome, WIDGET_HISTORY_RELATIVE_PATH);
             File[] caches = FileUtils.FolderFinder(cacheBase.getAbsolutePath(),
                     item);
             File[] historys = FileUtils.FolderFinder(
                     historyBase.getAbsolutePath(), item);
             if (ArrayUtils.isNotEmpty(caches)) {
                 try {
                     org.apache.commons.io.FileUtils.forceDelete(caches[0]);
                 } catch (IOException e) {
                     isDeleteSuc = false;
                     e.printStackTrace();
                 }
             }
             if (ArrayUtils.isNotEmpty(historys)) {
                 try {
                     org.apache.commons.io.FileUtils.forceDelete(historys[0]);
                 } catch (IOException e) {
                     isDeleteSuc = false;
                     e.printStackTrace();
                 }
             }
 
         } else if ("tomcat".equals(type)) {
             File[] portalWars = FileUtils.FileFinder(webappsHome, item, ".war");
             if (ArrayUtils.isNotEmpty(portalWars)) {
                 try {
                     org.apache.commons.io.FileUtils.forceDelete(portalWars[0]);
                     String nameWithoutSuffix = item.substring(0,
                             item.length() - 4);
                     File warPackage = new File(webappsHome, nameWithoutSuffix);
                    while (warPackage.exists()) {
                         try {
                             org.apache.commons.io.FileUtils
                                     .forceDelete(warPackage);
                         } catch (Exception e) {
                             //
                         }
                     }
                     File localhost = new File(
                             new File(webappsHome).getParent(),
                             "work\\Catalina\\localhost");
                     if (!localhost.isDirectory()) {
                         return isDeleteSuc;
                     }
                     File workPackage = new File(localhost, nameWithoutSuffix);
                    while (workPackage.exists()) {
                         try {
                             org.apache.commons.io.FileUtils
                                     .forceDelete(workPackage);
                         } catch (Exception e) {
                             //
                         }
                     }
                 } catch (IOException e) {
                     isDeleteSuc = false;
                     e.printStackTrace();
                 }
             }
         }
         return isDeleteSuc;
     }
 }
