 package com.android.task.tools;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
 
 import android.app.Activity;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.net.Uri;
 import android.provider.MediaStore;
 import android.util.Log;
 
 public class InsertFileToMediaStore {
 	private File 					mMediaFile 		 = null;
 	private ContentResolver			mContentResolver = null; 
 	private String                  mMimeType	     = null;
	private String 					mMediaFileName	 = null;
 	private final String		    SPLITOR			 = "/";
 	private boolean					mVideo			 = false;
 	private boolean					mImage			 = false;
 	private final String 			TAG				 = "InsertFileToMediaStore";
 	private Activity				a				 = null;
 	public InsertFileToMediaStore(Activity a,File media_file,String mime_type)
 	{
 		this.a						= a;
 		this.mMediaFile 			= media_file;
 		this.mContentResolver		= a.getContentResolver();
 		this.mMimeType				= mime_type;
		this.mMediaFileName		    = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
 		if (this.mMimeType.split(SPLITOR)[0].equalsIgnoreCase("video")){
 			this.mVideo 	=	true;
 		}else if (this.mMimeType.split(SPLITOR)[0].equalsIgnoreCase("image")){
 			this.mImage     = true;
 		}
 	}
 	
 	public Uri insert()
 	{
 		Uri content_uri = null;
 		ContentValues values = new ContentValues();
 		if (this.mVideo){
 			values.put(MediaStore.Video.Media.MIME_TYPE, this.mMimeType);
 			content_uri = this.mContentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
 			
 		}else if (this.mImage){
 			values.put(MediaStore.Images.Media.MIME_TYPE,this.mMimeType);
 			content_uri = this.mContentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
 		}
 		try {
 			BufferedOutputStream os;
 			os = new BufferedOutputStream(this.mContentResolver.openOutputStream(content_uri));
 			BufferedInputStream 	is  = new BufferedInputStream(new FileInputStream(this.mMediaFile));
 			int byte_;
 		    while ((byte_ = is.read()) != -1)
 		    	os.write(byte_);
 
 		    os.flush();
 		    os.close();
 		    is.close();
 		    this.a.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, content_uri));
 		    return content_uri;
 		} catch (FileNotFoundException e) {
 			Log.e(this.TAG,e.getMessage());
 			return null;
 		} catch (IOException e) {
 			Log.e(this.TAG,e.getMessage());
 			return null;
 		}
 	}
 
 }
