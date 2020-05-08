 package org.moo.android.filebrowser;
 
 /*
  * Copyright (c) 2009, Moo Productions
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, 
  * are permitted provided that the following conditions are met:
  * 
  * - Redistributions of source code must retain the above copyright notice, this list of 
  *   conditions and the following disclaimer.
  * 
  * - Redistributions in binary form must reproduce the above copyright notice, this list 
  *   of conditions and the following disclaimer in the documentation and/or other materials 
  *   provided with the distribution.
  * 
  * - Neither the name of the Moo Productions nor the names of its contributors may be used 
  *   to endorse or promote products derived from this software without specific prior written 
  *   permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
  * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
  * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
  * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Display;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.GridView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 
 public class FileBrowser extends Activity implements
 		AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {
 
 	private GridView mGrid;
 	private File mCurrentDir;
 	private ArrayList<File> mFiles;
 	private String[] mFilters;
 	private String[] mAudioExt;
 	private String[] mImageExt;
 	private String[] mArchiveExt;
 	private String[] mWebExt;
 	private String[] mTextExt;
 	private String[] mVideoExt;
 	private String[] mGeoPosExt;
 	private DirectoryManager dirManager;
 	private boolean mStandAlone;
 	private boolean showHidden;
 	private IconView mLastSelected;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		dirManager = new DirectoryManager();
 		this.showHidden = PreferenceManager.getDefaultSharedPreferences(this)
 				.getBoolean("show_hidden", false);
 
 		mAudioExt = getResources().getStringArray(R.array.fileEndingAudio);
 		mImageExt = getResources().getStringArray(R.array.fileEndingImage);
 		mArchiveExt = getResources().getStringArray(R.array.fileEndingPackage);
 		mWebExt = getResources().getStringArray(R.array.fileEndingWebText);
 		mTextExt = getResources().getStringArray(R.array.fileEndingText);
 		mVideoExt = getResources().getStringArray(R.array.fileEndingVideo);
 		mGeoPosExt = getResources().getStringArray(
 				R.array.fileEndingGeoPosition);
 
 		Intent intent = getIntent();
 		String action = intent.getAction();
 
 		if (action == null || action.compareTo(Intent.ACTION_MAIN) == 0)
 			mStandAlone = true;
 		else
 			mStandAlone = false;
 
 		mFilters = intent.getStringArrayExtra("FileFilter");
 
 		if (intent.getData() == null)
 			browseTo(new File("/sdcard"));
 		else
 			browseTo(new File(intent.getDataString()));
 
 		Display display = getWindowManager().getDefaultDisplay();
 
 		mGrid = new GridView(this);
 		registerForContextMenu(mGrid);
 		mGrid.setNumColumns(display.getWidth() / 60);
 		mGrid.setOnItemClickListener(this);
 		// mGrid.setOnItemLongClickListener(this);
 		mGrid.setOnItemSelectedListener(this);
 		mGrid.setAdapter(new IconAdapter());
 
 		setContentView(mGrid);
 	}
 
 	public void onResume() {
 		super.onResume();
 		this.showHidden = PreferenceManager.getDefaultSharedPreferences(this)
 				.getBoolean("show_hidden", false);
 		if (mCurrentDir == null)
 			browseTo(new File("/sdcard"));
 		else
 			browseTo(mCurrentDir);
 
 	}
 
 	protected Dialog onCreateDialog(int dialogID) {
 		return new CreateFolderDialog(this, new OnReadyListener());
 	}
 
 	private synchronized void browseTo(final File location) {
 		mCurrentDir = location;
 
 		this.setTitle(mCurrentDir.getName().compareTo("") == 0 ? mCurrentDir
 				.getPath() : mCurrentDir.getName());
 
 		dirManager.setShowHidden(showHidden);
 		mFiles = dirManager.getDirectoryListing(location, mFilters);
 
 		if (mGrid != null)
 			mGrid.setAdapter(new IconAdapter());
 	}
 
 	public class IconAdapter extends BaseAdapter {
 		@Override
 		public int getCount() {
 			return mFiles.size();
 		}
 
 		@Override
 		public Object getItem(int index) {
 			return mFiles.get(index);
 		}
 
 		@Override
 		public long getItemId(int index) {
 			return index;
 		}
 
 		@Override
 		public View getView(int index, View convertView, ViewGroup parent) {
 			IconView icon;
 			File currentFile = mFiles.get(index);
 
 			int iconId;
 			String filename;
 
 			if (index == 0
 					&& (currentFile.getParentFile() == null || currentFile
 							.getParentFile().getAbsolutePath().compareTo(
 									mCurrentDir.getAbsolutePath()) != 0)) {
 				iconId = R.drawable.updirectory;
 				filename = new String("..");
 			} else {
 				iconId = getIconId(index);
 				filename = currentFile.getName();
 			}
 
 			if (convertView == null) {
 				icon = new IconView(FileBrowser.this, iconId, filename);
 			} else {
 				icon = (IconView) convertView;
 				icon.setIconResId(iconId);
 				icon.setFileName(filename);
 			}
 
 			return icon;
 		}
 
 		private int getIconId(int index) {
 			File file = mFiles.get(index);
 
 			if (file.isDirectory())
 				return R.drawable.directory;
 
 			for (String ext : mAudioExt) {
 				if (file.getName().endsWith(ext))
 					return R.drawable.audio;
 			}
 
 			for (String ext : mArchiveExt) {
 				if (file.getName().endsWith(ext))
 					return R.drawable.archive;
 			}
 
 			for (String ext : mImageExt) {
 				if (file.getName().endsWith(ext))
 					return R.drawable.image;
 			}
 
 			for (String ext : mWebExt) {
 				if (file.getName().endsWith(ext))
 					return R.drawable.webdoc;
 			}
 
 			for (String ext : mTextExt) {
 				if (file.getName().endsWith(ext))
 					return R.drawable.text;
 			}
 
 			for (String ext : mVideoExt) {
 				if (file.getName().endsWith(ext))
 					return R.drawable.video;
 			}
 
 			for (String ext : mGeoPosExt) {
 				if (file.getName().endsWith(ext))
 					return R.drawable.geoposition;
 			}
 
 			return R.drawable.unknown;
 		}
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long id) {
 		File file = mFiles.get((int) id);
 
 		if (file.isDirectory()) {
 			browseTo(file);
 		} else {
 			if (!mStandAlone) {
 				// Send back the file that was selected
 				Uri path = Uri.fromFile(file);
 				Intent intent = new Intent(Intent.ACTION_PICK, path);
 				setResult(RESULT_OK, intent);
 				finish();
 			} else {
 				// Try to open it
 				Intent intent = new Intent(Intent.ACTION_VIEW);
 				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				intent.setDataAndType(Uri.fromFile(file), getMimeType(file));
 				startActivity(Intent.createChooser(intent, null));
 			}
 		}
 	}
 
 	private String getMimeType(File file) {
 		for (String ext : mAudioExt) {
 			if (file.getName().endsWith(ext))
 				return "audio/*";
 		}
 
 		for (String ext : mArchiveExt) {
 			if (file.getName().endsWith(ext))
 				return "archive/*";
 		}
 
 		for (String ext : mImageExt) {
 			if (file.getName().endsWith(ext))
 				return "image/*";
 		}
 
 		for (String ext : mWebExt) {
 			if (file.getName().endsWith(ext))
 				return "text/html";
 		}
 
 		for (String ext : mTextExt) {
 			if (file.getName().endsWith(ext))
 				return "text/plain";
 		}
 
 		for (String ext : mVideoExt) {
 			if (file.getName().endsWith(ext))
 				return "video/*";
 		}
 
 		for (String ext : mGeoPosExt) {
 			if (file.getName().endsWith(ext))
 				return "audio/*";
 		}
 
 		return "";
 	}
 
 	@Override
 	public void onItemSelected(AdapterView<?> grid, View icon, int arg2,
 			long index) {
 		if (mLastSelected != null) {
 			mLastSelected.deselect();
 		}
 
 		if (icon != null) {
 			mLastSelected = (IconView) icon;
 			mLastSelected.select();
 		}
 
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> grid) {
 		if (mLastSelected != null) {
 			mLastSelected.deselect();
 			mLastSelected = null;
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main, menu);
 		return true;
 	}
 
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		MenuInflater inflater = getMenuInflater();
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
 		menu.setHeaderTitle(((IconView) info.targetView).getFileName());
 		inflater.inflate(R.menu.context_menu, menu);
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
 				.getMenuInfo();
 		String child = ((IconView) info.targetView).getFileName();
 		switch (item.getItemId()) {
 		case R.id.delete_dir:
 			dirManager.delete(new File(mCurrentDir, child));
 			browseTo(mCurrentDir);
 			return true;
 		case R.id.move:
			Log.i("FileBrowser", "move");
 			dirManager.moveFile(new File(mCurrentDir, child));
			return true;
 		case R.id.copy:
 			dirManager.copyFile(new File(mCurrentDir, child));
			return true;
 		default:
 			return true;
 		}
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.preferences:
 			startActivityIfNeeded(new Intent(this, Preferences.class), -1);
 			return true;
 		case R.id.folder_add:
 			showDialog(1);
 			return true;
 		case R.id.paste:
 			try {
 				dirManager.paste(mCurrentDir);
 			} catch (IOException e) {
 				Log.e("FileBrowser", e.getMessage());
 				e.printStackTrace();
 			}
 			browseTo(mCurrentDir);
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private class OnReadyListener implements CreateFolderDialog.ReadyListener {
 		@Override
 		public void ready(String dirname) {
 			dirManager.createDirectory(mCurrentDir, dirname);
 			browseTo(mCurrentDir);
 
 		}
 	}
 
 }
