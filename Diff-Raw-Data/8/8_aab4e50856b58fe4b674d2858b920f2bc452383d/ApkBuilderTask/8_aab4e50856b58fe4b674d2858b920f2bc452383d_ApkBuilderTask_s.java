 /*
  * Copyright (C) 2009 The Android Open Source Project
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
 
 package com.android.ant;
 
 import com.android.sdklib.build.ApkBuilder;
 import com.android.sdklib.build.ApkCreationException;
 import com.android.sdklib.build.DuplicateFileException;
 import com.android.sdklib.build.SealedApkException;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 import org.apache.tools.ant.types.Path;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.ArrayList;
 import java.util.regex.Pattern;
 
 public class ApkBuilderTask extends Task {
 
     private final static Pattern PATTERN_JAR_EXT = Pattern.compile("^.+\\.jar$",
             Pattern.CASE_INSENSITIVE);
 
     private String mOutFolder;
     private String mApkFilepath;
     private String mResourceFile;
     private boolean mVerbose = false;
     private boolean mDebug = false;
     private boolean mHasCode = true;
     private String mAbiFilter = null;
 
     private Path mDexPath;
 
     private final ArrayList<Path> mZipList = new ArrayList<Path>();
     private final ArrayList<Path> mFileList = new ArrayList<Path>();
     private final ArrayList<Path> mSourceList = new ArrayList<Path>();
     private final ArrayList<Path> mJarfolderList = new ArrayList<Path>();
     private final ArrayList<Path> mJarfileList = new ArrayList<Path>();
     private final ArrayList<Path> mNativeList = new ArrayList<Path>();
 
     /**
      * Sets the value of the "outfolder" attribute.
      * @param outFolder the value.
      */
     public void setOutfolder(Path outFolder) {
         mOutFolder = TaskHelper.checkSinglePath("outfolder", outFolder);
     }
 
     /**
      * Sets the full filepath to the apk to generate.
      * @param filepath
      */
     public void setApkfilepath(String filepath) {
         mApkFilepath = filepath;
     }
 
     /**
      * Sets the resourcefile attribute
      * @param resourceFile
      */
     public void setResourcefile(String resourceFile) {
         mResourceFile = resourceFile;
     }
 
     /**
      * Sets the value of the "verbose" attribute.
      * @param verbose the value.
      */
     public void setVerbose(boolean verbose) {
         mVerbose = verbose;
     }
 
     /**
      * Sets the value of the "debug" attribute.
      * @param debug the debug mode value.
      */
     public void setDebug(boolean debug) {
         mDebug = debug;
     }
 
     /**
      * Sets an ABI filter. If non <code>null</code>, then only native libraries matching the given
      * ABI will be packaged with the APK.
      * @param abiFilter the ABI to accept (and reject all other). If null or empty string, no ABIs
      * are rejected. This must be a single ABI name as defined by the Android NDK. For a list
      * of valid ABI names, see $NDK/docs/CPU-ARCH-ABIS.TXT
      */
     public void setAbifilter(String abiFilter) {
         if (abiFilter != null && abiFilter.length() > 0) {
             mAbiFilter = abiFilter.trim();
         } else {
             mAbiFilter = null;
         }
     }
 
     /**
      * Sets the hascode attribute. Default is true.
      * If set to false, then <dex> and <sourcefolder> nodes are ignored and not processed.
      * @param hasCode the value of the attribute.
      */
     public void setHascode(boolean hasCode) {
         mHasCode   = hasCode;
     }
 
     /**
      * Returns an object representing a nested <var>zip</var> element.
      */
     public Object createZip() {
         Path path = new Path(getProject());
         mZipList.add(path);
         return path;
     }
 
     /**
      * Returns an object representing a nested <var>dex</var> element.
      * This is similar to a nested <var>file</var> element, except when {@link #mHasCode}
      * is <code>false</code> in which case it's ignored.
      */
     public Object createDex() {
         if (mDexPath == null) {
             return mDexPath = new Path(getProject());
         } else {
             throw new BuildException("Only one <dex> inner element can be provided");
         }
     }
 
     /**
      * Returns an object representing a nested <var>file</var> element.
      */
     public Object createFile() {
         System.out.println("WARNING: Using deprecated <file> inner element in ApkBuilderTask." +
         "Use <dex path=...> instead.");
         Path path = new Path(getProject());
         mFileList.add(path);
         return path;
     }
 
     /**
      * Returns an object representing a nested <var>sourcefolder</var> element.
      */
     public Object createSourcefolder() {
         Path path = new Path(getProject());
         mSourceList.add(path);
         return path;
     }
 
     /**
      * Returns an object representing a nested <var>jarfolder</var> element.
      */
     public Object createJarfolder() {
         Path path = new Path(getProject());
         mJarfolderList.add(path);
         return path;
     }
 
     /**
      * Returns an object representing a nested <var>jarfile</var> element.
      */
     public Object createJarfile() {
         Path path = new Path(getProject());
         mJarfileList.add(path);
         return path;
     }
 
     /**
      * Returns an object representing a nested <var>nativefolder</var> element.
      */
     public Object createNativefolder() {
         Path path = new Path(getProject());
         mNativeList.add(path);
         return path;
     }
 
     @Override
     public void execute() throws BuildException {
 
         File outputFile;
         if (mApkFilepath != null) {
             outputFile = new File(mApkFilepath);
         } else {
             throw new BuildException("missing attribute 'apkFilepath'");
         }
 
         // check dexPath is only one file.
         File dexFile = null;
         if (mHasCode) {
             String[] dexFiles = mDexPath.list();
             if (dexFiles.length != 1) {
                 throw new BuildException(String.format(
                         "Expected one dex file but path value resolve to %d files.",
                         dexFiles.length));
             }
             dexFile = new File(dexFiles[0]);
         }
 
         try {
             if (mDebug) {
                 System.out.println(String.format(
                         "Creating %s and signing it with a debug key...", outputFile.getName()));
             } else {
                 System.out.println(String.format(
                         "Creating %s for release...", outputFile.getName()));
             }
 
             ApkBuilder apkBuilder = new ApkBuilder(
                     outputFile,
                     new File(mOutFolder, mResourceFile),
                     dexFile,
                     mDebug ? ApkBuilder.getDebugKeystore() : null,
                     mVerbose ? System.out : null);
             apkBuilder.setDebugMode(mDebug);
 
 
             // add the content of the zip files.
             for (Path pathList : mZipList) {
                 for (String path : pathList.list()) {
                     apkBuilder.addZipFile(new File(path));
                 }
             }
 
             // add the files that go to the root of the archive (this is deprecated)
             for (Path pathList : mFileList) {
                 for (String path : pathList.list()) {
                     File f = new File(path);
                     apkBuilder.addFile(f, f.getName());
                 }
             }
 
             // now go through the list of file to directly add the to the list.
             if (mHasCode) {
                 for (Path pathList : mSourceList) {
                     for (String path : pathList.list()) {
                         apkBuilder.addSourceFolder(new File(path));
                     }
                 }
             }
 
             // now go through the list of jar folders.
             for (Path pathList : mJarfolderList) {
                 for (String path : pathList.list()) {
                     // it's ok if top level folders are missing
                     File folder = new File(path);
                     if (folder.isDirectory()) {
                         String[] filenames = folder.list(new FilenameFilter() {
                             public boolean accept(File dir, String name) {
                                 return PATTERN_JAR_EXT.matcher(name).matches();
                             }
                         });
 
                         for (String filename : filenames) {
                             apkBuilder.addResourcesFromJar(new File(folder, filename));
                         }
                     }
                 }
             }
 
             // now go through the list of jar files.
             for (Path pathList : mJarfileList) {
                 for (String path : pathList.list()) {
                     apkBuilder.addResourcesFromJar(new File(path));
                 }
             }
 
             // now the native lib folder.
             for (Path pathList : mNativeList) {
                 for (String path : pathList.list()) {
                     // it's ok if top level folders are missing
                     File folder = new File(path);
                     if (folder.isDirectory()) {
                         apkBuilder.addNativeLibraries(folder, mAbiFilter);
                     }
                 }
             }
 
 
             // close the archive
             apkBuilder.sealApk();
 
         } catch (DuplicateFileException e) {
             System.err.println(String.format(
                     "Found duplicate file for APK: %1$s\nOrigin 1: %2$s\nOrigin 2: %3$s",
                     e.getArchivePath(), e.getFile1(), e.getFile2()));
             throw new BuildException(e);
         } catch (ApkCreationException e) {
             throw new BuildException(e);
         } catch (SealedApkException e) {
             throw new BuildException(e);
         } catch (IllegalArgumentException e) {
             throw new BuildException(e);
         }
     }
 }
