 package com.omerjerk.cheatbox.utils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import android.app.Activity;
 import android.os.Environment;
 
 import com.omerjerk.cheatbox.R;
 
 public class copy extends Activity{ 
 	
 	File sdcard = Environment.getExternalStorageDirectory();
 	
 	String input, filename;
 	public copy(String in, String fname){
 		this.input =in; 
 		this.filename = fname;
 	}
 	
 	public boolean task(){
		File sourceOrg = new File(new File(sdcard, input),"filename");
 		File backupFolder = new File( new File(sdcard, input), "/backup");
 		
 		if(!backupFolder.exists()){
 			backupFolder.mkdirs();
 		}
 		
		File backupFile = new File (backupFolder, "filename");
 		
 		byte[] buff = new byte[1024];
 	    int read = 0;
 	    
 	    InputStream inOrig = null;
 	    OutputStream outOrig = null;
 	    try {
 	    	inOrig = new FileInputStream(sourceOrg);
 	    	outOrig = new FileOutputStream(backupFile);
 	    	while ((read = inOrig.read(buff)) > 0) {
 		          outOrig.write(buff, 0, read);
 		       }
 	       InputStream inRaw = getResources().openRawResource(R.raw.gamedata);
 	       FileOutputStream outPatched = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.imangi.templerun2/files/gamedata.txt");
 	       while ((read = inRaw.read(buff)) > 0) {
 	          outPatched.write(buff, 0, read);
 	       }
 	       
 	       inOrig.close();
 	       outOrig.close();
 	       inRaw.close();
 	       outPatched.close();
 	       return true;
 	    } catch (Exception e){
 	        return false;
 	    }
 	}
 	
 }
