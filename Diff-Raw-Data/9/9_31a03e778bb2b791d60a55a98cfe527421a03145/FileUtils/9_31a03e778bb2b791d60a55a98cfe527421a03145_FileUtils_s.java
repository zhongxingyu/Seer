 package com.lincbio.lincxmap.android.utils;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.os.Environment;
 
 public final class FileUtils {
 	private static final File SDCARD = Environment.getExternalStorageDirectory();
 	private static final File ROOT_DIR = new File(SDCARD + "/.lincxmap");
 	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
 
 	public static File getTempDir() {
 		return ROOT_DIR;
 	}
 
 	public static File newTempFile() {
 		String name = sdf.format(new Date());
 		return new File(ROOT_DIR + "/" + name);
 	}
 
 	public static File newTempFile(String ext) {
 		String name = sdf.format(new Date());
		return new File(ROOT_DIR + "/" + name + ext);
 	}
 
 }
