 package com.example.wecharades.presenter;
 
 import java.io.File;
 import java.io.FileInputStream;
 
 import org.apache.commons.net.ftp.FTP;
 import org.apache.commons.net.ftp.FTPClient;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.MediaController;
 import android.widget.VideoView;
 
import com.example.wecharades.views.StartScreen;
 import com.example.wecharades.views.VideoUploadActivity;
 
 public class VideoUploadPresenter extends Presenter {
 
 	private VideoUploadActivity activity;
 	private UploadVideo upload;
 	
 	public VideoUploadPresenter(VideoUploadActivity activity) {
 		super(activity);
 		this.activity = activity;
 	}
 
 	public void uploadVideo(Context context, String path) {
 		upload = new UploadVideo(context, path);
 		upload.execute();
 	}
 
 	public void playVideo(VideoView videoView) {
 		videoView.setVideoURI(CaptureVideo.uriVideo);
 		videoView.setMediaController(new MediaController(activity));
 		videoView.start();
 		videoView.requestFocus();
 
 	}
 
 
 	public void reRecord(String path) {
 		File file = new File(path);
 		file.delete();
 		Intent intent = new Intent(activity.getApplicationContext(), CaptureVideo.class);
 		activity.startActivity(intent);
 		activity.finish();	
 	}
 
 	private class UploadVideo extends AsyncTask<Void, Long, Boolean>{
 
 
 		private ProgressDialog mDialog;
 		Context mContext;
 		private String SAVE_PATH;
 		//private Game currentGame;
 
 		public UploadVideo(Context context, String path){
 			mContext = context;
 			SAVE_PATH = path;
 			mDialog = new ProgressDialog(mContext);
 			//currentGame = game;
 		}
 		@Override
 		protected void onPreExecute(){
 			mDialog.setTitle("Uploading Charade");
 			mDialog.setMessage("Please Wait");
 			mDialog.show();
 		}
 
 		@Override
 		protected Boolean doInBackground(Void... params) {
 			FTPClient ftp = null;
 
 			try{
 				ftp = new FTPClient();
 				ftp.connect("ftp.mklcompetencia.se", 21);
 
 				if (ftp.login("mklcompetencia.se", "ypkq4w")){
 					ftp.enterLocalPassiveMode(); // important!
 					ftp.setFileType(FTP.BINARY_FILE_TYPE);
 					FileInputStream in = new FileInputStream(new File(SAVE_PATH));
 					boolean result = ftp.storeFile("/APP/" + "PresentVideo.mp4", in);					
 					in.close();
 					if (result) 
 						Log.v("upload result", "succeeded");
 					ftp.logout();
 					ftp.disconnect();
 				}
 			}
 			catch (Exception e){
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result){
 			if(mDialog.isShowing()){
 				mDialog.setMessage("Successful upload!");
 				
 				//TODO: Show a dialog with the text "Successful upload" and click OK to proceed
 				/*mDialog.setButton("Cancel", new OnClickListener() {
 
 	                @Override
 	                public void onClick(DialogInterface dialog, int which) {
 	                    // TODO Auto-generated method stub
 
 	                myDialog.dismiss();
 	                }
 	            }); */
 				mDialog.dismiss();
 				
 				//Send to startscreen on success
				Intent intent = new Intent(activity.getApplicationContext(), StartScreen.class);
 				activity.startActivity(intent);
 				activity.finish();
 				
 			}
 		}
 	}
 }
