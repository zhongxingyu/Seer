 /*
  * Copyright (C) 2011-2012 sakuramilk <c.sakuramilk@gmail.com>
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
 
 package net.sakuramilk.widget;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 
 import net.sakuramilk.tweaklibrary.R;
 import net.sakuramilk.util.Constant;
 import net.sakuramilk.util.Misc;
 
 import android.annotation.SuppressLint;
 import android.content.Intent;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceManager;
 import android.preference.PreferenceScreen;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.util.Log;
 import android.view.KeyEvent;
 
 public abstract class FilePickerActivity extends PreferenceActivity
     implements OnPreferenceClickListener {
 
     private static final String TAG = "FilePickerActivity";
     private boolean mFileMode = false;
     private boolean mDirMode = false;
     private String mExecMode = "";
     private String mRootPath = Constant.MNT_ROOT;
     private String mChRootPath;
     private String mCurPath = "";
     private String mFilter = null;
 
     public abstract void onFilePicked(String path, String mode);
 
     @SuppressLint("SdCardPath")
 	protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         Intent intent = getIntent();
         setTitle(intent.getStringExtra("title"));
         String path = intent.getStringExtra("path");
         mChRootPath = intent.getStringExtra("chroot");
         if (mChRootPath == null) {
             mChRootPath = "";
         }
         mFilter = intent.getStringExtra("filter");
         mExecMode = intent.getStringExtra("mode");
         if ("dir".equals(intent.getStringExtra("select"))) {
             mDirMode = true;
         }
 
         if (Misc.isNullOfEmpty(path)) {
             File file = new File("/dev/block/mmcblk1");
             if (file.exists()) {
                 // SD select
                 setPreferenceScreen(createSdcardList());                
             } else {
                 // file select
                 mFileMode = true;
                 path = "/sdcard";
                 setPreferenceScreen(createFileList(path));
             }
         } else {
             // file select
             mRootPath = path;
             mFileMode = true;
             setPreferenceScreen(createFileList(path));
         }
     }
 
     private PreferenceScreen createSdcardList() {
         PreferenceManager prefManager = getPreferenceManager();
         PreferenceScreen rootPref = prefManager.createPreferenceScreen(this);
 
         mCurPath = Constant.MNT_ROOT;
 
         // get sdcard path
         String internalSdcardPath = Misc.getSdcardPath(true);
         String externalSdcardPath = Misc.getSdcardPath(false);

         PreferenceScreen pref;
         pref = prefManager.createPreferenceScreen(this);
         pref.setTitle(R.string.internal_sdcard);
         pref.setKey(internalSdcardPath);
         pref.setOnPreferenceClickListener(this);
         rootPref.addPreference(pref);
 
         pref = prefManager.createPreferenceScreen(this);
         pref.setTitle(R.string.external_sdcard);
         pref.setKey(externalSdcardPath);
         pref.setOnPreferenceClickListener(this);
         rootPref.addPreference(pref);
 
         return rootPref;
     }
 
     private PreferenceScreen createFileList(String path) {
         // Root
         PreferenceManager prefManager = getPreferenceManager();
         PreferenceScreen rootPref = prefManager.createPreferenceScreen(this);
 
         mCurPath = path;
 
         PreferenceCategory categoryPref;
         PreferenceScreen pref = prefManager.createPreferenceScreen(this);
         pref.setTitle(R.string.path);
         pref.setSummary(mCurPath.substring(mChRootPath.length()));
         pref.setSelectable(false);
         rootPref.addPreference(pref);
 
         File[] directories = new File(path).listFiles(new FilenameFilter() {
             @Override
             public boolean accept(File dir, String filename) {
                 File file = new File(dir + "/" + filename); 
                 return (!file.isHidden() && file.isDirectory());
             }
         });
         File[] zipFiles = new File(path).listFiles(new FilenameFilter() {
             @Override
             public boolean accept(File dir, String filename) {
                 if (mFilter == null || "".endsWith(mFilter)) {
                     return true;
                 }
                 return filename.endsWith(mFilter);
             }
         });
 
         if (zipFiles != null && zipFiles.length > 0 && mDirMode == false) {
             categoryPref = new PreferenceCategory(this);
             categoryPref.setTitle(R.string.file);
             rootPref.addPreference(categoryPref);
             for (File file : zipFiles) {
                 pref = prefManager.createPreferenceScreen(this);
                 pref.setTitle(file.getName());
                 pref.setKey(file.getPath());
                 double size = file.length();
                 String value = "";
                 if (size >= (1024 * 1024)) {
                     value = new BigDecimal(size/1024/1024).setScale(2, RoundingMode.HALF_UP).toString() + "MByte";
                 } else if (size >= (1024)) {
                     value = new BigDecimal(size/1024).setScale(2, RoundingMode.HALF_UP).toString() + "KByte";
                 } else {
                     value = size + "Byte";
                 }
                 pref.setSummary(value);
                 pref.setOnPreferenceClickListener(this);
                 rootPref.addPreference(pref);
             }
         }
 
         if (directories != null && directories.length > 0) {
             categoryPref = new PreferenceCategory(this);
             categoryPref.setTitle(R.string.directory);
             rootPref.addPreference(categoryPref);
             for (File dir : directories) {
                 Log.d(TAG, "dir=" + dir.getPath());
                 File[] childDirs = new File(dir.getPath()).listFiles(new FilenameFilter() {
                     @Override
                     public boolean accept(File dir, String filename) {
                         File file = new File(dir + "/" + filename); 
                         return (!file.isHidden() && file.isDirectory()); 
                     }
                 });
                 File[] childZipFiles = new File(dir.getPath()).listFiles(new FilenameFilter() {
                     @Override
                     public boolean accept(File dir, String filename) {
                         if (mFilter == null || "".endsWith(mFilter)) {
                             return true;
                         }
                         return filename.endsWith(mFilter);
                     }
                 });
                 if ((childDirs != null && childDirs.length > 0) ||
                     (childZipFiles != null && childZipFiles.length > 0)) {
                     pref = prefManager.createPreferenceScreen(this);
                     pref.setTitle(dir.getName());
                     pref.setKey(dir.getPath());
                     pref.setSummary(childDirs.length + " directories, " + childZipFiles.length + " files");
                     pref.setOnPreferenceClickListener(this);
                     rootPref.addPreference(pref);
                 }
             }
         }
 
         return rootPref;
     }
 
     @Override
     public boolean onPreferenceClick(Preference preference) {
         if (mFileMode) {
             final File file = new File(preference.getKey());
             if (Constant.MNT_ROOT.equals(file.getPath())) {
                 mFileMode = false;
                 setPreferenceScreen(createSdcardList());
             } else if (file.isDirectory()) {
                 if (mDirMode) {
                     // selected directory
                     onFilePicked(file.getPath().substring(mChRootPath.length()), mExecMode);
                 } else {
                     // selected directory
                     setPreferenceScreen(createFileList(file.getPath()));
                 }
             } else {
                 // selected file
                 onFilePicked(file.getPath().substring(mChRootPath.length()), mExecMode);
             }
         } else {
             // selected sdcard
             mCurPath = "";
             mFileMode = true;
             setPreferenceScreen(createFileList(preference.getKey()));
         }
         return false;
     }
 
     @Override
     public boolean dispatchKeyEvent(KeyEvent e) {
         if(e.getKeyCode() == KeyEvent.KEYCODE_BACK) {
             if(e.getAction() == KeyEvent.ACTION_UP) {
                 if (mRootPath.equals(mCurPath)) {
                     finish();
                 } else {
                     File file = new File(mCurPath);
                     if (Constant.MNT_ROOT.equals(file.getParent())) {
                         mFileMode = false;
                         setPreferenceScreen(createSdcardList());
                     } else {
                         setPreferenceScreen(createFileList(file.getParent()));
                     }
                 }
                 return false;
             }
             return true;
         }
         return super.dispatchKeyEvent(e);
     }
 
     public void reload() {
         setPreferenceScreen(createFileList(mCurPath));
     }
 }
