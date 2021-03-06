 package roman10.media.dash;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import roman10.quickactionwindow.ActionItem3;
 import roman10.quickactionwindow.QuickAction3;
 import roman10.ui.iconifiedtextselectedlist.IconifiedTextSelected;
 import roman10.ui.iconifiedtextselectedlist.IconifiedTextSelectedView;
 import roman10.utils.FileUtilsStatic;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnClickListener;
 import android.content.res.Configuration;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.AsyncTask.Status;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.AbsListView;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AbsListView.OnScrollListener;
 
 public class VideoBrowser extends ListActivity implements ListView.OnScrollListener {
 
 	private Context mContext;
 	public static VideoBrowser self;
 	public static long mTotalNumStreamlets;
     public static long mCurrProcessStreamletNum;
 	
 	/**
 	 * activity life cycle: this part of the source code deals with activity life cycle
 	 */
 	@Override
 	public void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 		mContext = this.getApplicationContext();
 		self = this;
 		FileUtilsStatic.initDirs();
 		initUI();
 	}
 	
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		unbindDisplayEntries();
 	}
 	
 	/*release the resources used by the list entries*/
 	public void unbindDisplayEntries() {
 		if (displayEntries!=null) {
 			int l_count = displayEntries.size();
 			for (int i = 0; i < l_count; ++i) {
 				IconifiedTextSelected l_its = displayEntries.get(i);
 				if (l_its != null) {
 					Drawable l_dr = l_its.getIcon();
 					if (l_dr != null) {
 						l_dr.setCallback(null);
 						l_dr = null;
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Data: this part of the code deals with data processing
 	 */
 	//displayEntries and mSelected stores the current display data
 	public static List<IconifiedTextSelected> displayEntries = new ArrayList<IconifiedTextSelected>();
 	public static List<Boolean> mSelected = new ArrayList<Boolean>();
 	
 	public static void setEntrySelected(int pos) {
 		mSelected.set(pos, true);
 	}
 	
 	public static boolean isEntrySelected(int pos) {
 		return mSelected.get(pos);
 	}
 	
 	public static void setEntryUnselected(int pos) {
 		mSelected.set(pos, false);
 	}
 	
 	public static void setAllEntriesSelected() {
 		for (int i = 0; i < mSelected.size(); ++i) {
 			mSelected.set(i, true);
 		}
 	}
 	
 	public static void setAllEntriesUnselected() {
 		for (int i = 0; i < mSelected.size(); ++i) {
 			mSelected.set(i, false);
 		}
 	}
 	
 	private boolean checkIfAllEntriesSelected() {
 		for (int i = 0; i < mSelected.size(); ++i) {
 			if  (!mSelected.get(i)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private int getNumOfEntriesSelected() {
 		int l_count = mSelected.size();
 		int l_selected = 0;
 		for (int i = 0; i < l_count; ++i) {
 			if (mSelected.get(i)) {
 				++l_selected;
 			}
 		}
 		return l_selected;
 	}
 	
 	private static int number_of_icons = 0;
 	LoadImageVideoTask loadTask;
 	private void loadVideosFromDirectory(String _dir) {
 		try {
 			if (loadTask != null && loadTask.getStatus() != Status.FINISHED) {
 				//if it's already loading secrets, ignore the request
 				return;
 			}
 			loadTask = new LoadImageVideoTask();
 			loadTask.execute(_dir);
 		} catch (Exception e) {
 			Toast.makeText(this, "Load media fail!", Toast.LENGTH_SHORT).show();
 		}
 	}
 	
 	private void getVideosFromDirectoryNonRecur(File _dir) {
 		//add the 
 		if (!_dir.isDirectory()) {
 			return;
 		}
 		File[] files = _dir.listFiles();
 		if (files == null) {
 			return;
 		}
 		Drawable videoIcon = null;
 		int l_iconType = 0;
 		for (File currentFile : files) {
 			if (!currentFile.isDirectory()) {
 				String l_filename = currentFile.getName();
 				if (checkEndsWithInStringArray(l_filename,
 							getResources().getStringArray(R.array.fileEndingVideo))) {
 					if (number_of_icons < 10) {
 						videoIcon = null;
 						++number_of_icons;
 						l_iconType = 22;
 					} else {
 						videoIcon = null;
 						l_iconType = 2;
 					}
 					VideoBrowser.displayEntries.add(new IconifiedTextSelected(
 							currentFile.getPath(),
 							videoIcon, false, true, l_iconType, 1, -1));
 					mSelected.add(false);
 				}
 			}
 		}
 	}
 	
 	private boolean checkEndsWithInStringArray(String _checkItsEnd, 
 			String[] _fileEndings){
 		String checkItsEnd = _checkItsEnd.toLowerCase();
 		for(String aEnd : _fileEndings){
 			if(checkItsEnd.endsWith(aEnd))
 				return true;
 		}
 		return false;
 	}
 	
 	private class LoadImageVideoTask extends AsyncTask<String, Void, Void> {
 		@Override
 		protected void onPreExecute() {
 			System.gc();
 			displayEntries.clear();
 			mSelected.clear();
 			showDialog(DIALOG_LOAD_MEDIA);
 		}
 		@Override
 		protected Void doInBackground(String... params) {
 			File l_root = new File(params[0]);
 			if (l_root.isDirectory()) {
 				number_of_icons = 0;
 				getVideosFromDirectoryNonRecur(new File(FileUtilsStatic.DEFAULT_DIR));
 			}
 			return null;
 		}
 		@Override
 		protected void onPostExecute(Void n) {
 			refreshUI();
 			try {
 				dismissDialog(DIALOG_LOAD_MEDIA);
 			} catch (Exception e) {
 				Log.v("VideoBrowser", e.getLocalizedMessage());
 			}
 		}
 	}
 	
 	/**
 	 * UI: this part of the source code deals with UI
 	 */
 	//bottom menu
 	private int currentFocusedBtn = 1;
 	private Button btn_bottommenu1;
 	private Button btn_bottommenu2;
 	private Button btn_bottommenu3;
 	//title bar
 	private TextView text_titlebar_text;
 	private ImageButton btn_titlebar_right_btn1;
 	private void initUI() {
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		this.setContentView(R.layout.video_browser);
 		//bottom menu
 		int l_btnWidth = this.getWindowManager().getDefaultDisplay().getWidth()/4;
 		btn_bottommenu1 = (Button) findViewById(R.id.video_browser_btn1);
 		btn_bottommenu1.setWidth(l_btnWidth);
 		btn_bottommenu1.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				/*capture video*/
 				Intent lIntent = new Intent();
 				lIntent.setClass(getApplicationContext(), roman10.media.camcorder.VideoCapture.class);
 				startActivityForResult(lIntent, REQUESET_CAP_VIDEO);
 			}
 		});
 		btn_bottommenu2 = (Button) findViewById(R.id.video_browser_btn2);
 		btn_bottommenu2.setWidth(l_btnWidth);
 		btn_bottommenu2.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				/*generate streamlet*/
 			}
 		});
 		btn_bottommenu3 = (Button) findViewById(R.id.video_browser_btn3);
 		btn_bottommenu3.setWidth(l_btnWidth);
 		btn_bottommenu3.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				/*upload video*/
 				//count the number of entries selected
 				int l_selected = getNumOfEntriesSelected();
 				if(l_selected == 0) {
 					Toast.makeText(mContext, "No Video is Selected!", Toast.LENGTH_SHORT).show();
 					return;
 				}
 				//if there're videos selected, start
 			}
 		});
 		btn_titlebar_right_btn1 = (ImageButton) findViewById(R.id.titlebar_right_btn1);
 		btn_titlebar_right_btn1.setBackgroundResource(R.drawable.top_menu_more);
 		btn_titlebar_right_btn1.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				createTopRightMenu(v);
 			}
 		});
 		loadVideosFromDirectory(FileUtilsStatic.DEFAULT_DIR);
 	}
 	
 	private void convertSelectedVideo() {
 		Intent lAndzopIntent = new Intent();
 		//lAndzopIntent.setClass(mContext, );
 		ArrayList<String> lFullPathList = new ArrayList<String>();
 		//TODO: get all selected videos
 		int lSize = this.mSelected.size();
 		for (int i = 0; i < lSize; ++i) {
 			if (this.mSelected.get(i)) {
 				lFullPathList.add(this.displayEntries.get(i).getText());
 			}
 		}
 		lAndzopIntent.putExtra(pucVideoFileNameList, lFullPathList);
 		startActivity(lAndzopIntent);
 	}
 	
 	private static final int REQUEST_CONVERT_OPTIONS = 0;
 	private static final int REQUESET_CAP_VIDEO = 1;
 	@Override
     protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
     	super.onActivityResult(requestCode, resultCode, intent);
     	switch (requestCode) {
     	case REQUEST_CONVERT_OPTIONS:
     		if (resultCode == RESULT_OK) {
     			//start conversion
     			convertSelectedVideo();
     		}
     		break;
     	case REQUESET_CAP_VIDEO:
     		if (resultCode == RESULT_OK) {
     			loadVideosFromDirectory(FileUtilsStatic.DEFAULT_DIR);
     		}
     		break;
     	}
 	}
 	
 	//refresh the UI when the directoryEntries changes
 	private static int last_list_view_pos = 0;
 	public void refreshUI() {
 		int l_btnWidth = this.getWindowManager().getDefaultDisplay().getWidth()/4;
 		btn_bottommenu1.setWidth(l_btnWidth);
 		btn_bottommenu2.setWidth(l_btnWidth);
 		btn_bottommenu3.setWidth(l_btnWidth);
 		SlowAdapter itla = new SlowAdapter(this);
 		itla.setListItems(this.displayEntries);		
 		this.setListAdapter(itla);
         getListView().setOnScrollListener(this);
         int l_size = this.displayEntries.size();
         if (l_size > 50) {
         	getListView().setFastScrollEnabled(true);
         } else {
         	getListView().setFastScrollEnabled(false);
         }
         if (l_size > 0) {
 	        if (last_list_view_pos < l_size) {
 				getListView().setSelection(last_list_view_pos);
 			} else {
 				getListView().setSelection(l_size-1);
 			}
         }
 		registerForContextMenu(getListView());
 	}
 	
 	private void createTopRightMenu(View _v) {
 		final ActionItem3 l_select_all = new ActionItem3();
 		if (checkIfAllEntriesSelected() == false) {
 			l_select_all.setTitle("Select All");
 			l_select_all.setIcon(this.getResources().getDrawable(R.drawable.topmenu_selectall));
 		} else {
 			l_select_all.setTitle("Unselect All");
 			l_select_all.setIcon(this.getResources().getDrawable(R.drawable.topmenu_unselectall));
 		}
 		
 		final QuickAction3 l_dropdown = new QuickAction3(mContext);
 		l_dropdown.addActionItem(l_select_all);
 		
 		l_dropdown.setOnActionItemClickListener(new QuickAction3.OnActionItemClickListener() {			
 			//@Override
 			public void onItemClick(int pos) {
 				if (pos == 0) {
 					//select all
 					if (checkIfAllEntriesSelected() == false) {
 						VideoBrowser.setAllEntriesSelected();
 						VideoBrowser.self.refreshUI();
 					} else {
 						VideoBrowser.setAllEntriesUnselected();
 						VideoBrowser.self.refreshUI();
 					}						
 				} 
 				l_dropdown.dismiss();
 			}
 		});
 		l_dropdown.show(_v);
 		l_dropdown.setAnimStyle(QuickAction3.ANIM_REFLECT);
 	}
 	
 	@Override
     public void onConfigurationChanged (Configuration newConfig) {
     	super.onConfigurationChanged(newConfig);
     	refreshUI();
     }
 	
 	static final int DIALOG_LOAD_MEDIA = 1;
 	static final int DIALOG_HELP = 2;
 	@Override
 	protected Dialog onCreateDialog(int id) {
         switch(id) {
         case DIALOG_LOAD_MEDIA:
         	ProgressDialog dialog = new ProgressDialog(this);
 	        dialog.setTitle("Load Files");
 	        dialog.setMessage("Please wait while loading...");
 	        dialog.setIndeterminate(true);
 	        dialog.setCancelable(true);
         	return dialog;
         default:
         	return null;
         }
 	}
 	/**
 	 * scroll events methods: this part of the source code contain the control source code
 	 * for handling scroll events
 	 */
 	private boolean mBusy = false;
 	public void onScroll(AbsListView view, int firstVisibleItem,
 			int visibleItemCount, int totalItemCount) {
 		last_list_view_pos = view.getFirstVisiblePosition();
 	}
 
 	//private boolean mSaveMemory = false;
 	public void onScrollStateChanged(AbsListView view, int scrollState) {		
 		switch (scrollState) {
         case OnScrollListener.SCROLL_STATE_IDLE:
             mBusy = false;
             int first = view.getFirstVisiblePosition();
             int count = view.getChildCount();
             int l_releaseTarget;
             for (int i=0; i<count; i++) {
             	IconifiedTextSelectedView t = (IconifiedTextSelectedView)view.getChildAt(i);
             	if (t.getTag()!=null) {
             		String filename = this.displayEntries.get(first+i).getText();
             		t.setText(filename);
             		int l_type = this.displayEntries.get(first+i).getType();
             		Drawable l_icon = null;
             		try {
 	            		if (l_type == 1) {
 	            			l_icon = null;
 	            		} else if (l_type == 2) {
 	            			l_icon = null;
 	            		}
             		} catch (OutOfMemoryError e) {
             			//if outofmemory, we try to clean up 10 view image resources,
             			//and try again
             			for (int j = 0; j < 10; ++j) {
             				l_releaseTarget = first - count - j;
 	    	            	if (l_releaseTarget > 0) {
 	    	    				IconifiedTextSelected l_its = displayEntries.get(l_releaseTarget);
 	    	    				IconifiedTextSelectedView l_itsv = (IconifiedTextSelectedView)
 	    	    					this.getListView().getChildAt(l_releaseTarget);
 	    	    				if (l_itsv!=null) {
 	    	    					l_itsv.setIcon(null);
 	    	    				}
 	    	    				if (l_its != null) {
 	    	    					Drawable l_dr = l_its.getIcon();
 	    	    					l_its.setIcon(null);
 	    	    					if (l_dr != null) {
 	    	    						l_dr.setCallback(null);
 	    	    						l_dr = null;
 	    	    					}
 	    	    				}
 	    	            	}
             			}
             			System.gc();
             			//after clean up, we try again
             			if (l_type == 1) {
 	            			l_icon = null;
 	            		} else if (l_type == 2) {
 	            			l_icon = null;
 	            		}
             		}
             		this.displayEntries.get(first+i).setIcon(l_icon);
             		if (l_icon != null) {
             			t.setIcon(l_icon);
             			t.setTag(null);
             		}
             	}
             }
             //System.gc();
             break;
         case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
         	//disableButtons();			//todo: temporarily comment out, see if this will cause crash or not
             mBusy = true;
             break;
         case OnScrollListener.SCROLL_STATE_FLING:
         	//disableButtons();         //todo: temporarily comment out, see if this will cause crash or not
             mBusy = true;
             break;
         }
 	}
 	
 	/**
 	 * List item click action
 	 */
 	private File currentFile;
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		last_list_view_pos = position;
 		String selectedFileOrDirName = this.displayEntries.get((int)id).getText();
 		File l_clickedFile = new File(this.displayEntries.get((int)id).getText());
 		if (l_clickedFile != null) {
 			showContextMenuForFile(l_clickedFile, position);
 		}
 	}
 	
 	private static final int CON_SEL_UNSEL = 0;
 	private static final int CON_VIEW = 1;
 	private static final int CON_DELETE = 2;
 	private static final int CON_BACK = 3;
 	final CharSequence[] items = {
 			"Select the File",
 			"View the File",
 			"Delete the File",
 			"Back"};
 	final CharSequence[] items2 = {
 			"Unselect the File",
 			"View the File",
 			"Delete the File",
 			"Back"};
 	public static final String pucVideoFileNameList = "pucVideoFileName";
 	private void showContextMenuForFile(final File _file, final int _pos) {
 		currentFile = _file;
		final CharSequence[] l_items;
 		final boolean l_selected = VideoBrowser.isEntrySelected(_pos);
 		if (l_selected) {
			l_items = items2;
 		} else {
			l_items = items;
 		}
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Choose an Action");
		builder.setItems(l_items, new DialogInterface.OnClickListener() {
 		    public void onClick(DialogInterface dialog, int item) {
 		    	switch (item) {
 		    	case CON_SEL_UNSEL:
 		    		if (l_selected) {
 		    			VideoBrowser.setEntryUnselected(_pos);
 		    			refreshUI();
 		    		} else {
 		    			VideoBrowser.setEntrySelected(_pos);
 			    		refreshUI();
 		    		}
 		    		break;
 		    	case CON_VIEW:
 		    		Uri data = Uri.parse("file://" + _file.getAbsolutePath());
 		    		Intent l_intent = new Intent(android.content.Intent.ACTION_VIEW);
 		    		if (checkEndsWithInStringArray(_file.getName(),
 							getResources().getStringArray(R.array.fileEndingVideo))) {
 		    			l_intent.setDataAndType(data, "video/*");
 			    		try {
 			    			startActivity(l_intent);
 			    		} catch (Exception e) {
 			    			Toast.makeText(mContext, "Sorry, Andzop cannot find an viewer for the file.", Toast.LENGTH_LONG);
 			    		}
 		    		}
 		    		break;
 		    	case CON_DELETE:
 		    		OnClickListener yesButtonListener = new OnClickListener() {
 		    			public void onClick(DialogInterface arg0, int arg1) {	
 		    				_file.delete();
 		    				refreshUI();
 		    			}
 		    		};
 		    		OnClickListener noButtonListener = new OnClickListener() {
 		    			public void onClick(DialogInterface arg0, int arg1) {
 		    				//do nothing
 		    			}
 		    		};
 		    		AlertDialog.Builder builder = new AlertDialog.Builder(VideoBrowser.this);
 		    		builder.setMessage("Are you sure you want to delete this file?")
 		    		.setCancelable(false)
 		    		.setPositiveButton("Yes", yesButtonListener)
 		    		.setNegativeButton("No", noButtonListener);
 		    		AlertDialog deleteFileAlert = builder.create();
 		    		deleteFileAlert.show();
 		    		return;
 		    	case CON_BACK:
 		    		return;
 		    	}
 		    }
 		});
 		AlertDialog contextDialog = builder.create();
 		contextDialog.show();
 	}
 	
 	public void updateNumOfSelected() {
 		try {
 			int lNumOfSelected = this.getNumOfEntriesSelected(); 
 			if (text_titlebar_text != null) {
 				CharSequence lMsg = text_titlebar_text.getText();
 				int lIndex = lMsg.toString().lastIndexOf("-");
 				lMsg = lMsg.subSequence(0, lIndex);
 				text_titlebar_text.setText(lMsg + "- " + lNumOfSelected);
 			}
 		} catch (Exception e) {
 			Log.e("updateNumOfSelected", e.toString());
 		}
 	}
 	
 	/**
 	 * Slow adapter: this part of the code implements the list adapter
 	 * Will not bind views while the list is scrolling
 	 */
 	private class SlowAdapter extends BaseAdapter {
     	/** Remember our context so we can use it when constructing views. */
     	private Context mContext;
 
     	private List<IconifiedTextSelected> mItems = new ArrayList<IconifiedTextSelected>();
 
     	public SlowAdapter(Context context) {
     		mContext = context;
     	}
 
     	public void setListItems(List<IconifiedTextSelected> lit) 
     	{ mItems = lit; }
 
     	/** @return The number of items in the */
     	public int getCount() { return mItems.size(); }
 
     	public Object getItem(int position) 
     	{ return mItems.get(position); }
 
     	/** Use the array index as a unique id. */
     	public long getItemId(int position) {
     		return position;
     	}
 
     	/** @param convertView The old view to overwrite, if one is passed
     	 * @returns a IconifiedTextSelectedView that holds wraps around an IconifiedText */
     	public View getView(int position, View convertView, ViewGroup parent) {
     		IconifiedTextSelectedView btv = null;
     		try {
 	    		if (convertView == null) {
 	    			btv = new IconifiedTextSelectedView(mContext, mItems.get(position));
 	    		} else { // Reuse/Overwrite the View passed
 	    			// We are assuming(!) that it is castable! 
 	    			btv = (IconifiedTextSelectedView) convertView;
 	    			btv.setText(mItems.get(position).getText());
 	    		}
 	    		//in busy mode
 	    		if (mBusy){
 	    			//if icon is NULL: the icon is not loaded yet; load default icon
 	    			if (mItems.get(position).getIcon()==null) {
 	    				btv.setIcon(R.drawable.video);
 	    				//mark this view, indicates the icon is not loaded
 	    				btv.setTag(this);
 	    			} else {
 	    				//if icon is not null, just display the icon
 	    				btv.setIcon(mItems.get(position).getIcon());
 	    				//mark this view, indicates the icon is loaded
 	    				btv.setTag(null);
 	    			}
 	    		} else {
 	    			//if not busy
 	    			Drawable d = mItems.get(position).getIcon();
 	    			if (d == null) {
 	    				//icon is not loaded, load now
 	    				btv.setIcon(R.drawable.video);
 	    				btv.setTag(this);
 	    			} else {
 	    				btv.setIcon(mItems.get(position).getIcon());
 	    				btv.setTag(null);
 	    			}
 	    		}
 	    		if (mItems.get(position).getVisibility()) {
 	    			btv.setVisibility(true);
 	    		} else {
 	    			btv.setVisibility(false);
 	    		}
 	    		btv.mCheckbox.setTag(position);
 	    		btv.mCheckbox.setOnClickListener(new View.OnClickListener() {		
 					public void onClick(View v) {
 						int pos = (Integer)v.getTag();
 						//select all change
 	    					if (VideoBrowser.mSelected.get(pos)==false) {
 	    						VideoBrowser.setEntrySelected(pos);
 	    						updateNumOfSelected();
 	    					} else {
 	    						VideoBrowser.setEntryUnselected(pos);
 	    						updateNumOfSelected();
 	    					}
 	//    				}
 					}
 				});
 	    		btv.mCheckbox.setChecked(VideoBrowser.mSelected.get(position));
     		} catch (Exception e) {
     			Log.e("VideoBrowser-SlowAdapter", e.getMessage());
     		}
 
     		return btv;
     	}
     }
 }
