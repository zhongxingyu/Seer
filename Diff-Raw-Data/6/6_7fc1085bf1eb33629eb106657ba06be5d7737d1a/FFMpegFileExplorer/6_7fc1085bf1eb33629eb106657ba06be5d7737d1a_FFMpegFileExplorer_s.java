 package cz.havlena.ffmpeg.ui;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.Comparator;
 
 import com.media.ffmpeg.FFMpeg;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class FFMpegFileExplorer extends ListActivity {
 
 	private static final String TAG = "FFMpegFileExplorer";
 
 	private String 			mRoot = "/";
 	private TextView 		mTextViewLocation;
 	private File[]			mFiles;
 
 
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 
 		Bundle extra = getIntent().getExtras();
 
 		if (extra != null)
 		{
 			String url = extra.getString("StreamUrl");
 
 			if (url != null)
 			{
 				Log.d(TAG, " Get URL from extra : " + url);
 				startPlayer(url);
 
 				extra.remove("StreamUrl");
 
 				finish();
 				return; 
 
 			}
 		}
 
 
 		setContentView(R.layout.ffmpeg_file_explorer);
 		mTextViewLocation = (TextView) findViewById(R.id.textview_path);
 		getDirectory(mRoot);
 
 
 		Button rtmpBtn = (Button) findViewById(R.id.testRtmp) ;
 		rtmpBtn.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				//String url = "rtmp://118.140.96.190:1935/live/cam2.stream";
 				//String url = "http://118.140.96.190:1935/live/cam2.stream/playlist.m3u8";
 				
				String url = "http://192.168.5.102:8090/aaa";
 				//String url = "rtsp://118.140.96.190:1935/live/cam1.stream"; 
 				//String url =  "http://115.77.220.87:49383/?action=appletvastream";
 				//String url = "rtsp://192.168.5.102:554/amrtest";
 				Log.d(TAG, " Try to stream  from url : " + url);
 				startPlayer(url);
 
 			}
 		});
 		
 		
 		Button rtmp2Btn = (Button) findViewById(R.id.testRtmp1) ;
 		rtmp2Btn.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				
 				//String url = "rtsp://192.168.5.102:554/amrtest";
 				
 				//gst rtsp
 				String url = "rtsp://192.168.5.102:8554/test";
 				Log.d(TAG, " Try to stream  from url : " + url);
 				startPlayer(url);
 
 			}
 		});
 		
 
 
 
 	}
 
 	protected void onStart()
 	{
 		super.onStart();
 
 
 
 
 
 	}
 
 
 	protected static boolean checkExtension(File file) {
 		String[] exts = FFMpeg.EXTENSIONS;
 		for(int i=0;i<exts.length;i++) {
 			if(file.getName().indexOf(exts[i]) > 0) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private void sortFilesByDirectory(File[] files) {
 		Arrays.sort(files, new Comparator<File>() {
 
 			public int compare(File f1, File f2) {
 				return Long.valueOf(f1.length()).compareTo(f2.length());
 			}
 
 		});
 	}
 
 	private void getDirectory(String dirPath) {
 		try {
 			mTextViewLocation.setText("Location: " + dirPath);
 
 			File f = new File(dirPath);
 			File[] temp = f.listFiles();
 
 			sortFilesByDirectory(temp);
 
 			File[] files = null;
 			if(!dirPath.equals(mRoot)) {
 				files = new File[temp.length + 1];
 				System.arraycopy(temp, 0, files, 1, temp.length);
 				files[0] = new File(f.getParent());
 			} else {
 				files = temp;
 			}
 
 			mFiles = files;
 			setListAdapter(new FileExplorerAdapter(this, files, temp.length == files.length));
 		} catch(Exception ex) {
 			FFMpegMessageBox.show(this, "Error", ex.getMessage());
 		}
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		File file = mFiles[position];
 
 		if (file.isDirectory()) {
 			if (file.canRead())
 				getDirectory(file.getAbsolutePath());
 			else {
 				FFMpegMessageBox.show(this, "Error", "[" + file.getName() + "] folder can't be read!");
 			}
 		} else {
 			if(!checkExtension(file)) {
 				StringBuilder strBuilder = new StringBuilder();
 				for(int i=0;i<FFMpeg.EXTENSIONS.length;i++)
 					strBuilder.append(FFMpeg.EXTENSIONS[i] + " ");
 				FFMpegMessageBox.show(this, "Error", "File must have this extensions: " + strBuilder.toString());
 				return;
 			}
 
 
 
 			//String url = "rtmp://112.213.86.13:1935/live/cam1.stream";
 			String url = file.getAbsolutePath();
 			Log.d(TAG, "set to view from " + url );
 			startPlayer(url);
 		}
 	}
 
 	private void startPlayer(String filePath) {
 		Intent i = new Intent(this, FFMpegPlayerActivity.class);
 		i.putExtra(getResources().getString(R.string.input_file), filePath);
 		startActivity(i);
 	}
 
 }
