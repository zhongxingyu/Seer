 package com.example.wecharades.views;
 
 
 import android.content.pm.ActivityInfo;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.VideoView;
 
 import com.example.wecharades.R;
 import com.example.wecharades.presenter.CaptureVideo;
 import com.example.wecharades.presenter.VideoUploadPresenter;
 
 
 /**
  * @author Adam
  * TODO: Fix the  design in the xml file showvideo.xml.
  */
 
 public class VideoUploadActivity extends GenericActivity{
 	protected static final String TAG = "";
 	private String path = "";
 	public static String fileName;
 	private VideoView videoView;
 	private VideoUploadPresenter presenter;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState, new VideoUploadPresenter(this));
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //Forces portrait orientation which is what the camera uses.
 		setContentView(R.layout.showvideo);
 		presenter = (VideoUploadPresenter) super.getPresenter(); //DOES NOT WORK.
 		videoView = (VideoView) findViewById(R.id.satisfiedVideoView);
 		path = getPathFromURI(CaptureVideo.uriVideo);	
 	}
 	
 	@Override
 	protected void onStart(){
 		super.onStart();
 		presenter.playVideo(videoView,path);
 	}
 
 	/**
 	 * Handles the Yes button
 	 * @param view
 	 */
 	public void onClickYes(View view) {
		videoView.stopPlayback();
 		presenter.uploadVideo(VideoUploadActivity.this, path);
 	}
 
 	/**
 	 * Handles the No button
 	 * @param w
 	 */
 	public void onClickNo(View view) {
 		presenter.reRecord(path);
 	}
 	
 	//TODO: fix a new method that's not using managedQuery(); @adam
 	private String getPathFromURI(Uri contentUri) {
 		String[] proj = { MediaStore.Images.Media.DATA };
 		Cursor cursor = managedQuery(contentUri, proj, null, null, null);
 		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
 		cursor.moveToFirst();
 		return cursor.getString(column_index);
 	}
 
 	@Override
 	public TextView getTextArea() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
 
 
 
 
 
