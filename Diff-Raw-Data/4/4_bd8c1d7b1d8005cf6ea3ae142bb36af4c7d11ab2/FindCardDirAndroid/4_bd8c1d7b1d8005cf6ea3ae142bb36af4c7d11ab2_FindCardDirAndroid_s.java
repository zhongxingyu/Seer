 /*
  * Copyright (C) 2009 Timothy Bourke
  * 
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option)
  * any later version.
  * 
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
  * for more details.
  * 
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc., 59
  * Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 
 package mnemogogo.mobile.hexcsv;
 
 import java.util.Vector;
 import java.io.File;
 import java.io.IOException;
 
 public class FindCardDirAndroid
 {
     public static Logger logger = null;
     
     private static final String[] skip_files = { "LOST.DIR", ".thumbnails" };
     private static final String[] skip_paths = { "/etc", "/system",
         "/sys", "/cache", "/sbin", "/proc", "/d", "/dev", "/sdcard/andnav2" };
 
     private static void logInfo(String msg)
     {
         if (logger != null) {
             logger.log(Logger.INFO, "FIND", msg);
         }
     }
 
     private static void logInfo(String msg1, String msg2)
     {
         if (logger != null) {
             logger.log(Logger.INFO, "FIND", msg1 + msg2);
         }
     }
 
     private static void logStatus(boolean exists,
                                   boolean isdir,
                                   boolean canread)
     {
         if (logger != null) {
             logger.log(Logger.INFO, "FIND", "---"
                     + (exists ? "exists," : "doesnotexist,")
                     + (isdir ? "isdir," : "isnotdir,")
                     + (canread ? "canread" : "cannotread"));
         }
     }
 
     private static boolean hasAllFiles(String[] subfiles)
     {
         boolean hasStats = false;
         boolean hasCategories = false;
         boolean hasConfig = false;
         boolean hasCards = false;
 
         for (String sf : subfiles) {
 
             if (sf.equals("STATS.CSV")) {
                 logInfo("---found: STATS.CSV");
                 hasStats = true;
 
             } else if (sf.equals("CATS")) {
                 logInfo("---found: CATS");
                 hasCategories = true;
 
             } else if (sf.equals("CONFIG")) {
                 logInfo("---found: CONFIG");
                 hasConfig = true;
 
             } else if (sf.equals("CARDS")) {
                 logInfo("---found: CARDS");
                 hasCards = true;
             }
         }
 
         return (   hasStats
                 && hasCategories
                 && hasConfig
                 && hasCards);
     }
 
     private static boolean canWrite(File file)
     {
         boolean r = file.canWrite();
         logInfo("---canwrite=", r ? "true" : "false");
         return r;
     }
 
     public static boolean isCardDir(File file)
     {
         String[] subfiles;
 
         try {
             boolean exists = file.exists();
             boolean isdir = file.isDirectory();
             boolean canread = file.canRead();
 
             logStatus(exists, isdir, canread);
             if (!exists || !isdir || !canread)
             {
                 return false;
             }
 
             subfiles = file.list();
 
         } catch (SecurityException e) {
             logInfo("---!isCardDir:SecurityException:", e.toString());
             return false;
         }
 
         return (hasAllFiles(subfiles) && canWrite(file));
     }
 
     public static boolean isCardDir(String path)
     {
         if ((HexCsvAndroid.context != null)
             && path.startsWith(HexCsvAndroid.demo_prefix))
         {
             String subpath = path.substring(HexCsvAndroid.demo_prefix.length());
             
             if (subpath.endsWith(File.separator)) {
                 subpath = subpath.substring(0, subpath.length() - 1);
             }
             
             try {
                 return hasAllFiles(
                         HexCsvAndroid.context.getAssets().list(subpath));
             } catch (IOException e) {
                 logInfo("---!isCardDir:IOException:", e.toString());
                 return false;
             }
         }
 
         return isCardDir(new File(path));
     }
 
     private static boolean skipFile(File f)
     {
         String name = f.getName();
         for (String g : skip_files) {
                 if (name.equals(g)) {
                         return true;
                 }
         }
         
         String path = f.getAbsolutePath();
         for (String g : skip_paths) {
                 if (path.equals(g)) {
                         return true;
                 }
         }
         
         return false;
     }
 
     private static void doDir(File dir, Vector<String> found)
     {
         logInfo("--doDir: ", dir.getPath());
         try {           
             if (isCardDir(dir)) {
                 logInfo("---found!");
                 found.addElement(dir.getPath());
 
             } else {
                 String[] subfiles = dir.list();
                 for (String sf : subfiles) {
                     File subdir = new File(dir, sf);
 
                     if (subdir.isDirectory() && subdir.canRead() && !skipFile(subdir)) {
                         doDir(subdir, found);
                     }
                 }
             }
         } catch (SecurityException e) {
             logInfo("---!SecurityException:", e.toString());
         }
 
         return;
     }
 
 
     protected static Vector<String> list(File[] roots)
     {
         Vector<String> paths = new Vector<String>();
 
         logInfo("FindCardDirAndroid.list: starting...");
         try {
             if (roots != null) {
                 // Check on the filesystem
                 for (File root : roots) {
                     String s = root.toString();
                     int bidx = s.indexOf(0);
                     if (bidx != -1) {
                             // work around an Android bug
                             // http://www.mail-archive.com/android-developers@googlegroups.com/msg42592.html
                             s = s.substring(0, bidx);
                     }
                     logInfo("-root=", s);
                     doDir(new File(s), paths);
                 }
             }
 
             // Check in assets
             if (HexCsvAndroid.context != null) {
                 try {
                     for (String demo : HexCsvAndroid.context.getAssets().list(""))
                     {
                         String path = new File("/android_asset", demo).getPath();
                         if (isCardDir(path)) {
                             paths.addElement(path);
                         }
                     }
                 } catch (IOException e) { }
             }
         } catch (SecurityException e) {
             return null;
         }
 
         logInfo("FindCardDirAndroid.list: done.");
         return paths;
     }
 
     public static Vector<String> list(String[] paths)
     {
         if (paths == null) {
             return list((File[])null);
         }
 
         File[] roots = new File[paths.length];
         for (int i = 0; i < roots.length; ++i) {
             roots[i] = new File(paths[i]);
         }
 
         return list(roots);
     }
 
     public static Vector<String> list(boolean check_filesystem)
     {
         if (check_filesystem) {
             return list(File.listRoots());
         } else {
             return list((File[])null);
         }
     }
 }
 
