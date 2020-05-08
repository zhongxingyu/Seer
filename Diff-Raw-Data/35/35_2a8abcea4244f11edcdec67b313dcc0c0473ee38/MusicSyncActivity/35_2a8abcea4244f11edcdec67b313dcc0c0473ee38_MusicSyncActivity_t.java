 package com.we.android.music;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.AlarmManager;
 import android.app.ListActivity;
 import android.app.PendingIntent;
 import android.content.ComponentName;
 import android.content.ContentUris;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.IBinder;
 import android.provider.MediaStore;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class MusicSyncActivity extends ListActivity implements ServiceConnection, IMusicSyncListener {
     class SyncFolderAdapter extends BaseAdapter {
 	private static final int PROGRESS_UNDFINED = -1;
 	private List<String> mMissingFiles = new ArrayList<String>();
 	private int mProgress = PROGRESS_UNDFINED;
 	private Cursor mLocalFilesCursor;
 
 	@Override
 	public int getCount() {
 	    return getLocalFilesCount() + mMissingFiles.size();
 	}
 	
 	@Override
 	public Object getItem(int position) {
 	    // TODO Auto-generated method stub
 	    return null;
 	}
 
 	public void addMissingFiles(List<String> missingFiles) {
 	    mMissingFiles.clear();
 	    mMissingFiles.addAll(missingFiles);
 	    notifyDataSetChanged();
 	}
 	
 	public void addLocalFiles(Cursor cursor) {
 	    mLocalFilesCursor = cursor;
 	    notifyDataSetChanged();
 	}
 	
 	public void showProgress(int progress) {
 	    mProgress = progress;
 	    notifyDataSetChanged();
 	}
 
 	@Override
 	public long getItemId(int position) {
 	    if (position < getLocalFilesCount()) {
 		mLocalFilesCursor.moveToPosition(position);
 		return mLocalFilesCursor.getLong(mLocalFilesCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID));
 	    }
 	    return -1;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 	    int localFilesCount = getLocalFilesCount();
 	    View view = null;
 	    if (position < localFilesCount) {
 		view = getLayoutInflater().inflate(R.layout.listitem, parent, false);
 		mLocalFilesCursor.moveToPosition(position);
 		TextView title = (TextView) view.findViewById(R.id.title);
 		title.setText(mLocalFilesCursor.getString(mLocalFilesCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE)));
 		TextView artist = (TextView) view.findViewById(R.id.artist);
 		artist.setText(mLocalFilesCursor.getString(mLocalFilesCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST)));
 	    } else if (position == localFilesCount) {
 		view = getLayoutInflater().inflate(R.layout.downloadinglistitem, parent, false);
 		TextView text = (TextView) view.findViewById(R.id.text);
 		text.setTextColor(Color.DKGRAY);
 		text.setText(mMissingFiles.get(position - localFilesCount));
 		if (mProgress != PROGRESS_UNDFINED) {
 		    ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressbar);
 		    progressBar.setProgress(mProgress);
 		    TextView percentage = (TextView) view.findViewById(R.id.percentage);
 		    percentage.setText(mProgress + "%");
 		}
 	    } else if (position > localFilesCount) {
 		view = getLayoutInflater().inflate(R.layout.listitem, parent, false);
 		TextView title = (TextView) view.findViewById(R.id.title);
 		title.setText(mMissingFiles.get(position - localFilesCount));
 		title.setTextColor(Color.DKGRAY);
 	    }
 	    return view;
 	}
 	
 	private int getLocalFilesCount() {
 	    int localFilesCount = 0;
 	    if (mLocalFilesCursor != null) {
 		localFilesCount = mLocalFilesCursor.getCount();
 	    }
 	    return localFilesCount;
 	}
     }
 
     private IMusicSyncControl mMusicSyncControl;
     private View mFooter;
     private SyncFolderAdapter mAdapter;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
 
 	mFooter = getLayoutInflater().inflate(R.layout.footer, null);
 	ListView listView = getListView();
 	listView.setFooterDividersEnabled(false);
 	listView.addFooterView(mFooter, null, false);
 	listView.setEmptyView(getLayoutInflater().inflate(R.layout.empty, null));
 
 	mAdapter = new SyncFolderAdapter();
 	getListView().setAdapter(mAdapter);
 	
 	mAdapter.addLocalFiles(getLocalFiles());
 	
 	getListView().setOnItemClickListener(new OnItemClickListener() {
 	    @Override
 	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		Intent intent = new Intent(Intent.ACTION_VIEW, ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id));
 		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
 		startActivity(intent);
 	    }
 	});
     }
     
     @Override
     protected void onResume() {
 	super.onResume();
 	AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
 	Intent intent = new Intent(this, MusicSyncService.class);
 	PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//	alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, 0, AlarmManager.INTERVAL_HALF_HOUR, pendingIntent);
 	alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, 0, 1000 * 60, pendingIntent);
 
 	bindService(new Intent(MusicSyncService.class.getName()), this, Context.BIND_AUTO_CREATE);
     }
     
     @Override
     protected void onPause() {
 	mMusicSyncControl.unregisterSyncListener(this);
 	unbindService(this);
         super.onPause();
     }
     
     private Cursor getLocalFiles() {
 	String[] projection = new String[] {
 		android.provider.MediaStore.Audio.Media._ID,
 		android.provider.MediaStore.Audio.Media.ARTIST,
 		android.provider.MediaStore.Audio.Media.TITLE,
 		android.provider.MediaStore.Audio.Media.DATA,
 		android.provider.MediaStore.Audio.Media.DURATION
 	};
 	File externalStorageDirectory = Environment.getExternalStorageDirectory();
 	File syncFolder = new File(externalStorageDirectory, "musicsync");
 	
 	StringBuilder where = new StringBuilder();
 	where.append(android.provider.MediaStore.Audio.Media.DATA +" LIKE '" + syncFolder.getAbsolutePath() + "%'");
 	Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
 		projection,
 		where.toString(), null, null);
 	return cursor;
     }
 
     @Override
     public void onServiceConnected(ComponentName name, IBinder service) {
 	mMusicSyncControl = ((MusicSyncService.LocalBinder) service).getService();
 	mMusicSyncControl.registerSyncListener(this);
 	onFilesMissing(mMusicSyncControl.getMissingFiles());
     }
 
     @Override
     public void onServiceDisconnected(ComponentName name) {
     }
 
     @Override
     public void onProgressUpdate(int progress) {
 	mAdapter.showProgress(progress);    
     }
 
     @Override
     public void onSyncFinished() {
 	Toast.makeText(getApplicationContext(), "Folder synced", Toast.LENGTH_SHORT).show();
     }
 
     @Override
     public void onFilesMissing(List<String> missingFiles) {
 	getListView().removeFooterView(mFooter);
 	mAdapter.addMissingFiles(missingFiles);
 	mAdapter.addLocalFiles(getLocalFiles());
     }

    @Override
    public void onFilesDeletet() {
	mAdapter.addLocalFiles(getLocalFiles());
    }
 }
