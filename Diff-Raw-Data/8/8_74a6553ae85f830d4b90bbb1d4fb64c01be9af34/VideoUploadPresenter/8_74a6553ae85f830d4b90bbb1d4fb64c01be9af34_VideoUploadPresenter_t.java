 package com.example.wecharades.presenter;
 /**
  * TODO: CREATE DIRECTORIES AND SHIT ON FTP.
  */
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 
 import org.apache.commons.net.ftp.FTP;
 import org.apache.commons.net.ftp.FTPClient;
 import org.apache.commons.net.ftp.FTPConnectionClosedException;
 import org.apache.commons.net.io.CopyStreamException;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.VideoView;
 
 import com.example.wecharades.model.Database;
 import com.example.wecharades.model.DatabaseException;
 import com.example.wecharades.model.Turn;
 import com.example.wecharades.views.StartActivity;
 import com.example.wecharades.views.VideoUploadActivity;
 
 public class VideoUploadPresenter extends Presenter {
 
 	private VideoUploadActivity activity;
 	private UploadVideo upload;
 	private Turn turn;
 	private String fileName = "PresentVideo.mp4";
 	private String serverPath;
 
 	public VideoUploadPresenter(VideoUploadActivity activity) {
 		super(activity);
 		this.activity = activity;
 		this.turn = (Turn) activity.getIntent().getSerializableExtra(Database.TURN);
 	}
 	
 	/**
 	 * Starts the VideoUpload
 	 * @param context
 	 * @param path
 	 */
 	public void uploadVideo(Context context, String path) {
 		setServerStorageLocation();
 		upload = new UploadVideo(context, path);
 		upload.execute();
 	}
 	
 	/**
 	 * Initiate and start the video.
 	 * @param videoView
 	 */
 	public void playVideo(VideoView videoView,String path) {
 		videoView.setVideoPath(path);
 		//videoView.setVideoURI(CaptureVideo.uriVideo);
 		//videoView.setMediaController(new MediaController(activity));
 		videoView.start();
 		//videoView.requestFocus();
 	}
 	
 	/**
 	 * Sets the storage location of the videofile for the FTP-server. 
 	 * @return
 	 */
 	private void setServerStorageLocation(){
 		String gameID = turn.getGameId();
 		String turnNumber = String.valueOf(turn.getTurnNumber());
 		//String serverPath = "/APP/" + gameID + "/" + turnNumber + "/";
 		String serverPath = "/APP/GAMES/" + gameID + turnNumber + ".mp4";
 		System.out.println(serverPath);
 		this.serverPath = serverPath;
 	}
 	
 	/**	
 	 * Lets the player to re-record the video and deletes the current file.
 	 * @param path
 	 */
 	public void reRecord(String path) {
 		File file = new File(path);
 		file.delete();
 		Intent intent = new Intent(activity.getApplicationContext(), CaptureVideo.class);
 		activity.startActivity(intent);
 		activity.finish();	
 	}
 	private void updateModel(){
 		try {
 			dc.updateTurn(turn);
 		} catch (DatabaseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private class UploadVideo extends AsyncTask<Void, Long, Boolean>{
 
 		private ProgressDialog mDialog;
 		Context mContext;
 		private String SAVE_PATH;
 
 		public UploadVideo(Context context, String path){
 			mContext = context;
 			SAVE_PATH = path;
 			mDialog = new ProgressDialog(mContext);
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
 					ftp.enterLocalPassiveMode();
 					ftp.setFileType(FTP.BINARY_FILE_TYPE);					
 					//ftpCreateDirectoryTree(ftp, serverPath);				
 					FileInputStream in = new FileInputStream(new File(SAVE_PATH));
 					boolean result = ftp.storeFile(serverPath, in);					
 					in.close();
 					if (result) 
 						Log.v("upload result", "succeeded");
 					ftp.logout();
 					ftp.disconnect();
 				}
 			}
			//TODO: prettyPrint-stuff?
 			catch (SocketException e){
 				Log.v("download result", e.getMessage());
 			}
 			catch (UnknownHostException e){
 				Log.v("download result", e.getMessage());
 			}
 			catch (FTPConnectionClosedException e){
 				Log.v("download result", e.getMessage());
 			}
 			catch (CopyStreamException e){
 				Log.v("download result", e.getMessage());
 			}
 			catch (IOException e){
 				Log.v("download result", e.getMessage());
 			}
 			catch (Exception e){
 				Log.v("download result","failed " + e.getMessage());
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result){
 			if(mDialog.isShowing()){
 				mDialog.dismiss();
 				
 				turn.setVideoLink(serverPath);
 				turn.setState(Turn.VIDEO);
 				updateModel();
 				
 				//Send to startscreen on success
 				Intent intent = new Intent(activity.getApplicationContext(), StartActivity.class);
 				activity.startActivity(intent);
 				activity.finish();
 			}
 		}
 		/**
 		* Utility to create an arbitrary directory hierarchy on the remote ftp server 
 		* @param client the FTP-client
 		* @param dirTree  the directory tree only delimited with / chars.  No file name!
 		* @throws Exception
 		*/
 		private void ftpCreateDirectoryTree( FTPClient client, String dirTree ) throws IOException {	
 		  boolean dirExists = true;
 		  //tokenize the string and attempt to change into each directory level.  If you cannot, then start creating.
 		  String[] directories = dirTree.split("/");
 		  for (String dir : directories ) {
 		    if (!dir.isEmpty() ) {
 		      if (dirExists) {
 		        dirExists = client.changeWorkingDirectory(dir);
 		      }
 		      if (!dirExists) {
 		        if (!client.makeDirectory(dir)) {
 		          throw new IOException("Unable to create remote directory '" + dir + "'.  error='" + client.getReplyString()+"'");
 		        }
 		        if (!client.changeWorkingDirectory(dir)) {
 		          throw new IOException("Unable to change into newly created remote directory '" + dir + "'.  error='" + client.getReplyString()+"'");
 		        }
 		      }
 		    }
 		  }     
 		}
 	}
 }
