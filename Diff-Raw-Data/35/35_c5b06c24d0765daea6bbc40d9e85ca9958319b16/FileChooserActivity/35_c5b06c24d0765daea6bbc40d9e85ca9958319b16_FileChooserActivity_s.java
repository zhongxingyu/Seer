 /*
  *   Copyright 2012 Hai Bison
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 
 package group.pals.android.lib.ui.filechooser;
 
 import group.pals.android.lib.ui.filechooser.io.IFile;
 import group.pals.android.lib.ui.filechooser.services.FileProviderService;
 import group.pals.android.lib.ui.filechooser.services.IFileProvider;
 import group.pals.android.lib.ui.filechooser.services.IFileProvider.FilterMode;
 import group.pals.android.lib.ui.filechooser.services.LocalFileProvider;
 import group.pals.android.lib.ui.filechooser.utils.E;
 import group.pals.android.lib.ui.filechooser.utils.UI;
 import group.pals.android.lib.ui.filechooser.utils.Utils;
 import group.pals.android.lib.ui.filechooser.utils.history.History;
 import group.pals.android.lib.ui.filechooser.utils.history.HistoryFilter;
 import group.pals.android.lib.ui.filechooser.utils.history.HistoryListener;
 import group.pals.android.lib.ui.filechooser.utils.history.HistoryStore;
 import group.pals.android.lib.ui.filechooser.utils.ui.Dlg;
 import group.pals.android.lib.ui.filechooser.utils.ui.LoadingDialog;
 import group.pals.android.lib.ui.filechooser.utils.ui.TaskListener;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.Manifest;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.graphics.Rect;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.view.inputmethod.EditorInfo;
 import android.widget.AbsListView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.GridView;
 import android.widget.HorizontalScrollView;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.ViewFlipper;
 
 /**
  * Main activity for this library.
  * 
  * @author Hai Bison
  * 
  */
 public class FileChooserActivity extends FragmentActivity {
 
     /**
      * The full name of this class. Generally used for debugging.
      */
     public static final String _ClassName = FileChooserActivity.class.getName();
 
     /**
      * Types of view.
      * 
      * @author Hai Bison
      * @since v4.0 beta
      */
     public static enum ViewType {
         /**
          * Use {@link ListView} to display file list.
          */
         List,
         /**
          * Use {@link GridView} to display file list.
          */
         Grid
     }
 
     /*---------------------------------------------
      * KEYS
      */
 
     /**
      * Key to hold the root path.<br>
      * <br>
      * If {@link LocalFileProvider} is used, then default is sdcard, if sdcard
      * is not available, "/" will be used.<br>
      * <br>
      * <b>Note</b>: The value of this key is a {@link IFile}
      */
     public static final String _Rootpath = "rootpath";
 
     /**
      * Key to hold the service class which implements {@link IFileProvider}.<br>
      * Default is {@link LocalFileProvider}
      */
     public static final String _FileProviderClass = "file_provider_class";
 
     // ---------------------------------------------------------
 
     /**
      * Key to hold {@link IFileProvider.FilterMode}, default is
      * {@link IFileProvider.FilterMode#FilesOnly}.
      */
     public static final String _FilterMode = IFileProvider.FilterMode.class.getName();
 
     // flags
 
     // ---------------------------------------------------------
 
     /**
      * Key to hold max file count that's allowed to be listed, default =
      * {@code 1024}
      */
     public static final String _MaxFileCount = "max_file_count";
     /**
      * Key to hold multi-selection mode, default = {@code false}
      */
     public static final String _MultiSelection = "multi_selection";
     /**
      * Key to hold regex filename filter, default = {@code null}
      */
     public static final String _RegexFilenameFilter = "regex_filename_filter";
     /**
      * Key to hold display-hidden-files, default = {@code false}
      */
     public static final String _DisplayHiddenFiles = "display_hidden_files";
 
     // ---------------------------------------------------------
 
     /**
      * Key to hold {@link IFileProvider.SortType}, default =
      * {@link IFileProvider.SortType#SortByName}
      */
     public static final String _SortType = IFileProvider.SortType.class.getName();
 
     // ---------------------------------------------------------
 
     /**
      * Key to hold {@link IFileProvider.SortOrder}, default =
      * {@link IFileProvider.SortOrder#Ascending}
      */
     public static final String _SortOrder = IFileProvider.SortOrder.class.getName();
 
     // ---------------------------------------------------------
 
     /**
      * Key to hold property save-dialog, default = {@code false}
      */
     public static final String _SaveDialog = "save_dialog";
     /**
      * Key to hold default filename, default = {@code null}
      */
     public static final String _DefaultFilename = "default_filename";
     /**
      * Key to hold results (can be one or multiple files)
      */
     public static final String _Results = "results";
 
     /**
      * Key to hold view type. Can be one of:<br>
      * - {@link ViewType#List}<br>
      * - {@link ViewType#Grid}<br>
      * <br>
      * Default = {@link ViewType#List}
      */
     public static final String _ViewType = ViewType.class.getName();
 
     /**
      * This key holds current location (an {@link IFile}), to restore it after
      * screen orientation changed
      */
     static final String _CurrentLocation = "current_location";
     /**
      * This key holds current history (a {@link History}&lt;{@link IFile}&gt;),
      * to restore it after screen orientation changed
      */
     static final String _History = "history";
 
     /*
      * "constant" variables
      */
 
     /**
      * The file provider service.
      */
     private IFileProvider mFileProvider;
     /**
      * The service connection.
      */
     private ServiceConnection mServiceConnection;
 
     /**
      * Used to store preferences. Currently it stores:
      * {@link IFileProvider.SortType}, {@link IFileProvider.SortOrder},
      * {@link ViewType}
      * 
      * @since v2.0 alpha
      */
     private SharedPreferences mPrefs;
 
     private IFile mRoot;
     private boolean mIsMultiSelection;
     private boolean mIsSaveDialog;
 
     /**
      * The history.
      */
     private History<IFile> mHistory;
 
     /**
      * The adapter of list view.
      */
     private FileAdapter mFileAdapter;
 
     /*
      * controls
      */
     private HorizontalScrollView mViewLocationsContainer;
     private ViewGroup mViewLocations;
     private ViewGroup mViewFilesContainer;
     private TextView mTxtFullDirName;
     private AbsListView mViewFiles;
     private TextView mFooterView;
     private Button mBtnOk;
     private EditText mTxtSaveas;
     private ImageButton mBtnGoBack;
     private ImageButton mBtnGoForward;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.afc_file_chooser);
         /*
          * Thanks to Matthias.
          * http://stackoverflow.com/questions/1362723/how-can-i-get-a
          * -dialog-style-activity-window-to-fill-the-screen
          * 
          * But I can't check if you set the theme in xml to dialog or another
          * else. The SDK does not mention this.
          */
         // if (getTheme().??? == android.R.style.Theme_Dialog)
         getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
 
         loadPreferences();
 
         mIsMultiSelection = getIntent().getBooleanExtra(_MultiSelection, false);
 
         mIsSaveDialog = getIntent().getBooleanExtra(_SaveDialog, false);
         if (mIsSaveDialog) {
             mIsMultiSelection = false;
         }
 
         mBtnGoBack = (ImageButton) findViewById(R.id.afc_filechooser_activity_button_go_back);
         mBtnGoForward = (ImageButton) findViewById(R.id.afc_filechooser_activity_button_go_forward);
         mViewLocations = (ViewGroup) findViewById(R.id.afc_filechooser_activity_view_locations);
         mViewLocationsContainer = (HorizontalScrollView) findViewById(R.id.afc_filechooser_activity_view_locations_container);
         mTxtFullDirName = (TextView) findViewById(R.id.afc_filechooser_activity_textview_full_dir_name);
         mViewFilesContainer = (ViewGroup) findViewById(R.id.afc_filechooser_activity_view_files_container);
         mFooterView = (TextView) findViewById(R.id.afc_filechooser_activity_view_files_footer_view);
         mTxtSaveas = (EditText) findViewById(R.id.afc_filechooser_activity_textview_saveas_filename);
         mBtnOk = (Button) findViewById(R.id.afc_filechooser_activity_button_ok);
 
         if (savedInstanceState != null && savedInstanceState.get(_History) instanceof HistoryStore<?>)
             mHistory = savedInstanceState.getParcelable(_History);
         else
             mHistory = new HistoryStore<IFile>(0);
         mHistory.addListener(new HistoryListener<IFile>() {
 
             @Override
             public void onChanged(History<IFile> history) {
                 int idx = history.indexOf(getLocation());
                 mBtnGoBack.setEnabled(idx > 0);
                 mBtnGoForward.setEnabled(idx >= 0 && idx < history.size() - 1);
             }
         });
 
         // make sure RESULT_CANCELED is default
         setResult(RESULT_CANCELED);
 
         bindService(savedInstanceState);
     }// onCreate()
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.afc_file_chooser_activity, menu);
         return true;
     }// onCreateOptionsMenu()
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getGroupId() == R.id.afc_filechooser_activity_menugroup_sorter) {
             doResortFileList(item.getItemId());
         }// group_sorter
         else if (item.getItemId() == R.id.afc_filechooser_activity_menuitem_new_folder) {
             doCreateNewDir();
         } else if (item.getItemId() == R.id.afc_filechooser_activity_menuitem_switch_viewmode) {
            doSwitchViewMode();
         } else if (item.getItemId() == R.id.afc_filechooser_activity_menuitem_home) {
             doGoHome();
         }
 
         return true;
     }// onOptionsItemSelected()
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         /*
          * sorting
          */
 
         // clear all icons
         final int[] fSorterIds = { R.id.afc_filechooser_activity_menuitem_sort_by_name,
                 R.id.afc_filechooser_activity_menuitem_sort_by_size,
                 R.id.afc_filechooser_activity_menuitem_sort_by_date };
         for (int id : fSorterIds)
             menu.findItem(id).setIcon(0);
 
         IFileProvider.SortType sortType = IFileProvider.SortType.SortByName;
         try {
             sortType = IFileProvider.SortType.valueOf(mPrefs.getString(_SortType,
                     IFileProvider.SortType.SortByName.name()));
         } catch (Exception e) {
         }
 
         final boolean fSortAscending = IFileProvider.SortOrder.Ascending.name().equals(
                 mPrefs.getString(_SortOrder, IFileProvider.SortOrder.Ascending.name()));
 
         switch (sortType) {
         case SortByName:
             menu.findItem(R.id.afc_filechooser_activity_menuitem_sort_by_name).setIcon(
                     fSortAscending ? R.drawable.afc_ic_menu_sort_up : R.drawable.afc_ic_menu_sort_down);
             break;
         case SortBySize:
             menu.findItem(R.id.afc_filechooser_activity_menuitem_sort_by_size).setIcon(
                     fSortAscending ? R.drawable.afc_ic_menu_sort_up : R.drawable.afc_ic_menu_sort_down);
             break;
         case SortByDate:
             menu.findItem(R.id.afc_filechooser_activity_menuitem_sort_by_date).setIcon(
                     fSortAscending ? R.drawable.afc_ic_menu_sort_up : R.drawable.afc_ic_menu_sort_down);
             break;
         }
 
         /*
          * view type
          */
 
         MenuItem menuItem = menu.findItem(R.id.afc_filechooser_activity_menuitem_switch_viewmode);
         if (ViewType.List.name().equals(mPrefs.getString(_ViewType, ViewType.List.name()))) {
             menuItem.setIcon(R.drawable.afc_ic_menu_gridview);
             menuItem.setTitle(R.string.afc_cmd_grid_view);
         } else {
             menuItem.setIcon(R.drawable.afc_ic_menu_listview);
             menuItem.setTitle(R.string.afc_cmd_list_view);
         }
 
         return true;
     }// onPrepareOptionsMenu()
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         outState.putParcelable(_CurrentLocation, getLocation());
         outState.putParcelable(_History, mHistory);
 
         unbindService(mServiceConnection);
         mServiceConnection = null;
     }// onSaveInstanceState()
 
     @Override
     protected void onStart() {
         super.onStart();
         if (!mIsMultiSelection && !mIsSaveDialog)
             Dlg.toast(this, R.string.afc_hint_double_tap_to_select_file, Dlg.LENGTH_SHORT);
     }// onStart()
 
     /**
      * Loads preferences.
      */
     private void loadPreferences() {
         mPrefs = getSharedPreferences(FileChooserActivity.class.getName(), 0);
 
         Editor editor = mPrefs.edit();
 
         /*
          * sort
          */
 
         if (getIntent().hasExtra(_SortType))
             editor.putString(_SortType, ((IFileProvider.SortType) getIntent().getSerializableExtra(_SortType)).name());
         else if (!mPrefs.contains(_SortType))
             editor.putString(_SortType, IFileProvider.SortType.SortByName.name());
 
         if (getIntent().hasExtra(_SortOrder))
             editor.putString(_SortOrder,
                     ((IFileProvider.SortOrder) getIntent().getSerializableExtra(_SortOrder)).name());
         else if (!mPrefs.contains(_SortOrder))
             editor.putString(_SortOrder, IFileProvider.SortOrder.Ascending.name());
 
         /*
          * view
          */
 
         if (getIntent().hasExtra(_ViewType))
             editor.putString(_ViewType, ((ViewType) getIntent().getSerializableExtra(_ViewType)).name());
         else if (!mPrefs.contains(_ViewType))
             editor.putString(_ViewType, ViewType.List.name());
 
         editor.commit();
     }// loadPreferences()
 
     /**
      * Connects to file provider service, then loads root directory. If can not,
      * then finishes this activity with result code =
      * {@link Activity#RESULT_CANCELED}
      * 
      * @param fSavedInstanceState
      */
     private void bindService(final Bundle fSavedInstanceState) {
         Class<?> serviceClass = (Class<?>) getIntent().getSerializableExtra(_FileProviderClass);
         if (serviceClass == null)
             serviceClass = LocalFileProvider.class;
 
         mServiceConnection = new ServiceConnection() {
 
             public void onServiceConnected(ComponentName className, IBinder service) {
                 try {
                     mFileProvider = ((FileProviderService.LocalBinder) service).getService();
                 } catch (Throwable t) {
                     Log.e(_ClassName, "mServiceConnection.onServiceConnected() -> " + t);
                 }
             }// onServiceConnected()
 
             public void onServiceDisconnected(ComponentName className) {
                 mFileProvider = null;
             }// onServiceDisconnected()
         };
 
         bindService(new Intent(this, serviceClass), mServiceConnection, Context.BIND_AUTO_CREATE);
 
         new LoadingDialog(this, R.string.afc_msg_loading, false) {
 
             private static final int WaitTime = 200;
             private static final int MaxWaitTime = 3000; // 3 seconds
 
             @Override
             protected Object doInBackground(Void... params) {
                 int totalWaitTime = 0;
                 while (mFileProvider == null) {
                     try {
                         totalWaitTime += WaitTime;
                         Thread.sleep(WaitTime);
                         if (totalWaitTime >= MaxWaitTime)
                             break;
                     } catch (InterruptedException e) {
                         break;
                     }
                 }
 
                 return null;
             }
 
             @Override
             protected void onPostExecute(Object result) {
                 super.onPostExecute(result);
 
                 if (mFileProvider == null) {
                     Dlg.showError(FileChooserActivity.this, R.string.afc_msg_cannot_connect_to_file_provider_service,
                             new DialogInterface.OnCancelListener() {
 
                                 @Override
                                 public void onCancel(DialogInterface dialog) {
                                     setResult(RESULT_CANCELED);
                                     finish();
                                 }
                             });
                 } else {
                     setupService();
                     setupHeader();
                     setupViewFiles();
                     setupFooter();
 
                     final Object fPath = fSavedInstanceState != null ? fSavedInstanceState.get(_CurrentLocation) : null;
                     setLocation(fPath instanceof IFile ? (IFile) fPath : mRoot, new TaskListener() {
 
                         @Override
                         public void onFinish(boolean ok, Object any) {
                             if (fPath == null)
                                 mHistory.push(getLocation(), getLocation());
                             else
                                 mHistory.notifyHistoryChanged();
                         }
                     });
                 }
             }// onPostExecute()
         }.execute();// LoadingDialog
     }// bindService()
 
     /**
      * Setup the file provider:<br>
      * - filter mode;<br>
      * - display hidden files;<br>
      * - max file count;<br>
      * - ...
      */
     private void setupService() {
         /*
          * set root path, if not specified, try using
          * IFileProvider#defaultPath()
          */
         if (getIntent().getSerializableExtra(_Rootpath) != null)
             mRoot = (IFile) getIntent().getSerializableExtra(_Rootpath);
         if (mRoot == null || !mRoot.isDirectory())
             mRoot = mFileProvider.defaultPath();
 
         IFileProvider.FilterMode filterMode = (FilterMode) getIntent().getSerializableExtra(_FilterMode);
         if (filterMode == null)
             filterMode = IFileProvider.FilterMode.FilesOnly;
 
         IFileProvider.SortType sortType = IFileProvider.SortType.SortByName;
         try {
             sortType = IFileProvider.SortType.valueOf(mPrefs.getString(_SortType,
                     IFileProvider.SortType.SortByName.name()));
         } catch (Exception e) {
             // ignore it
         }
 
         boolean sortAscending = IFileProvider.SortOrder.Ascending.name().equals(
                 mPrefs.getString(_SortOrder, IFileProvider.SortOrder.Ascending.name()));
 
         mFileProvider.setDisplayHiddenFiles(getIntent().getBooleanExtra(_DisplayHiddenFiles, false));
         mFileProvider.setFilterMode(mIsSaveDialog ? IFileProvider.FilterMode.FilesOnly : filterMode);
         mFileProvider.setMaxFileCount(getIntent().getIntExtra(_MaxFileCount, 1024));
         mFileProvider.setRegexFilenameFilter(mIsSaveDialog ? null : getIntent().getStringExtra(_RegexFilenameFilter));
         mFileProvider.setSortOrder(sortAscending ? IFileProvider.SortOrder.Ascending
                 : IFileProvider.SortOrder.Descending);
         mFileProvider.setSortType(sortType);
     }// setupService()
 
     /**
      * Setup:<br>
      * - title of activity;<br>
      * - button go back;<br>
      * - button location;<br>
      * - button go forward;
      */
     private void setupHeader() {
         if (mIsSaveDialog) {
             setTitle(R.string.afc_title_save_as);
         } else {
             switch (mFileProvider.getFilterMode()) {
             case FilesOnly:
                 setTitle(R.string.afc_title_choose_files);
                 break;
             case FilesAndDirectories:
                 setTitle(R.string.afc_title_choose_files_and_directories);
                 break;
             case DirectoriesOnly:
                 setTitle(R.string.afc_title_choose_directories);
                 break;
             }
         }// title of activity
 
         mBtnGoBack.setEnabled(false);
         mBtnGoBack.setOnClickListener(mBtnGoBackOnClickListener);
 
         mBtnGoForward.setEnabled(false);
         mBtnGoForward.setOnClickListener(mBtnGoForwardOnClickListener);
     }// setupHeader()
 
     /**
      * Setup:<br>
      * - {@link #mViewFiles}<br>
      * - {@link #mViewFilesContainer}<br>
      * - {@link #mFileAdapter}
      */
     private void setupViewFiles() {
         if (ViewType.List.name().equals(mPrefs.getString(_ViewType, ViewType.List.name())))
             mViewFiles = (AbsListView) getLayoutInflater().inflate(R.layout.afc_listview_files, null);
         else
             mViewFiles = (AbsListView) getLayoutInflater().inflate(R.layout.afc_gridview_files, null);
 
         mViewFilesContainer.removeAllViews();
         mViewFilesContainer.addView(mViewFiles, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                 ViewGroup.LayoutParams.FILL_PARENT, 1));
 
         mViewFiles.setOnTouchListener(new View.OnTouchListener() {
 
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 return mListviewFilesGestureDetector.onTouchEvent(event);
             }
         });
 
         if (mFileAdapter == null)
             mFileAdapter = new FileAdapter(FileChooserActivity.this, new ArrayList<DataModel>(),
                     mFileProvider.getFilterMode(), mIsMultiSelection);
         /*
          * API 13+ does not recognize AbsListView.setAdapter(), so we cast it to
          * explicit class
          */
         if (mViewFiles instanceof ListView)
             ((ListView) mViewFiles).setAdapter(mFileAdapter);
         else
             ((GridView) mViewFiles).setAdapter(mFileAdapter);
 
         // no comments :-D
         mFooterView.setOnLongClickListener(new View.OnLongClickListener() {
 
             @Override
             public boolean onLongClick(View v) {
                 E.show(FileChooserActivity.this);
                 return false;
             }
         });
     }// setupListviewFiles()
 
     /**
      * Setup:<br>
      * - button Cancel;<br>
      * - text field "save as" filename;<br>
      * - button Ok;
      */
     private void setupFooter() {
         if (mIsSaveDialog) {
             mTxtSaveas.setVisibility(View.VISIBLE);
             mTxtSaveas.setText(getIntent().getStringExtra(_DefaultFilename));
             mTxtSaveas.setOnEditorActionListener(mTxtFilenameOnEditorActionListener);
 
             mBtnOk.setVisibility(View.VISIBLE);
             mBtnOk.setOnClickListener(mBtnOk_SaveDialog_OnClickListener);
         }// this is in save mode
         else {
             mTxtSaveas.setVisibility(View.GONE);
 
             if (mIsMultiSelection) {
                 mBtnOk.setVisibility(View.VISIBLE);
                 mBtnOk.setOnClickListener(mBtnOk_OpenDialog_OnClickListener);
             } else
                 mBtnOk.setVisibility(View.GONE);
         }// this is in open mode
     }// setupFooter()
 
     private void doGoHome() {
         if (mRoot.equalsToPath(getLocation()))
             return;
 
         /*
          * we can't use mRoot, because if the first item in history is mRoot,
          * then when we push new location (which equals to mRoot) to the
          * history, the history will be cleared
          */
         setLocation(mFileProvider.fromPath(mRoot.getAbsolutePath()), new TaskListener() {
 
             @Override
             public void onFinish(boolean ok, Object any) {
                 mHistory.push(getLocation(), getLocation());
             }
         });
     }// doGoHome()
 
     /**
      * Resort file list when user clicks menu item.
      * 
      * @param menuItemId
      *            the ID of menu item
      */
     private void doResortFileList(int menuItemId) {
         IFileProvider.SortType lastSortType = IFileProvider.SortType.SortByName;
         try {
             lastSortType = IFileProvider.SortType.valueOf(mPrefs.getString(_SortType,
                     IFileProvider.SortType.SortByName.name()));
         } catch (Throwable t) {
             // TODO
         }
 
         boolean lastSortAscending = IFileProvider.SortOrder.Ascending.name().equals(
                 mPrefs.getString(_SortOrder, IFileProvider.SortOrder.Ascending.name()));
 
         Editor editor = mPrefs.edit();
 
         if (menuItemId == R.id.afc_filechooser_activity_menuitem_sort_by_name) {
             if (lastSortType == IFileProvider.SortType.SortByName)
                 editor.putString(_SortOrder, lastSortAscending ? IFileProvider.SortOrder.Descending.name()
                         : IFileProvider.SortOrder.Ascending.name());
             else {
                 editor.putString(_SortType, IFileProvider.SortType.SortByName.name());
                 editor.putString(_SortOrder, IFileProvider.SortOrder.Ascending.name());
             }
         } else if (menuItemId == R.id.afc_filechooser_activity_menuitem_sort_by_size) {
             if (lastSortType == IFileProvider.SortType.SortBySize)
                 editor.putString(_SortOrder, lastSortAscending ? IFileProvider.SortOrder.Descending.name()
                         : IFileProvider.SortOrder.Ascending.name());
             else {
                 editor.putString(_SortType, IFileProvider.SortType.SortBySize.name());
                 editor.putString(_SortOrder, IFileProvider.SortOrder.Ascending.name());
             }
         } else if (menuItemId == R.id.afc_filechooser_activity_menuitem_sort_by_date) {
             if (lastSortType == IFileProvider.SortType.SortByDate)
                 editor.putString(_SortOrder, lastSortAscending ? IFileProvider.SortOrder.Descending.name()
                         : IFileProvider.SortOrder.Ascending.name());
             else {
                 editor.putString(_SortType, IFileProvider.SortType.SortByDate.name());
                 editor.putString(_SortOrder, IFileProvider.SortOrder.Ascending.name());
             }
         }
 
         editor.commit();
 
         /*
          * Re-sort the listview by re-loading current location; NOTE: re-sort
          * the adapter does not repaint the listview, even if we call
          * notifyDataSetChanged(), invalidateViews()...
          */
         try {
             mFileProvider.setSortType(IFileProvider.SortType.valueOf(mPrefs.getString(_SortType,
                     IFileProvider.SortType.SortByName.name())));
             mFileProvider.setSortOrder(IFileProvider.SortOrder.valueOf(mPrefs.getString(_SortOrder,
                     IFileProvider.SortOrder.Ascending.name())));
         } catch (Exception e) {
             // TODO
         }
         setLocation(getLocation(), null);
         supportInvalidateOptionsMenu();
     }// doResortFileList()
 
    private void doSwitchViewMode() {
         new LoadingDialog(this, R.string.afc_msg_loading, false) {
 
             @Override
             protected void onPreExecute() {
                 // call this first, to let the parent prepare the dialog
                 super.onPreExecute();
 
                 if (ViewType.List.name().equals(mPrefs.getString(_ViewType, ViewType.List.name())))
                     mPrefs.edit().putString(_ViewType, ViewType.Grid.name()).commit();
                 else
                     mPrefs.edit().putString(_ViewType, ViewType.List.name()).commit();
 
                 setupViewFiles();
                 supportInvalidateOptionsMenu();
             }// onPreExecute()
 
             @Override
             protected Object doInBackground(Void... params) {
                 // do nothing :-)
                 return null;
             }// doInBackground()
         }.execute();
    }// doSwitchViewMode()
 
     /**
      * Confirms user to create new directory.
      */
     private void doCreateNewDir() {
         if (mFileProvider instanceof LocalFileProvider
                 && !Utils.havePermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
             Dlg.toast(this, R.string.afc_msg_app_doesnot_have_permission_to_create_files, Dlg.LENGTH_SHORT);
             return;
         }
 
         final AlertDialog fDlg = new AlertDialog.Builder(this).create();
 
         View view = getLayoutInflater().inflate(R.layout.afc_simple_text_input_view, null);
         final EditText fTxtFile = (EditText) view.findViewById(R.id.afc_simple_text_input_view_text1);
         fTxtFile.setHint(R.string.afc_hint_folder_name);
         fTxtFile.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 
             @Override
             public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                 if (actionId == EditorInfo.IME_ACTION_DONE) {
                     UI.hideSoftKeyboard(FileChooserActivity.this, fTxtFile.getWindowToken());
                     fDlg.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                     return true;
                 }
                 return false;
             }
         });
 
         fDlg.setView(view);
         fDlg.setTitle(R.string.afc_cmd_new_folder);
         fDlg.setIcon(android.R.drawable.ic_menu_add);
         fDlg.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel),
                 new DialogInterface.OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         dialog.cancel();
                     }
                 });
         fDlg.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.ok),
                 new DialogInterface.OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         String name = fTxtFile.getText().toString().trim();
                         if (!Utils.isFilenameValid(name)) {
                             Dlg.toast(FileChooserActivity.this, getString(R.string.afc_pmsg_filename_is_invalid, name),
                                     Dlg.LENGTH_SHORT);
                             return;
                         }
 
                         IFile dir = mFileProvider.fromPath(String
                                 .format("%s/%s", getLocation().getAbsolutePath(), name));
                         if (dir.mkdir()) {
                             Dlg.toast(FileChooserActivity.this, getString(R.string.afc_msg_done), Dlg.LENGTH_SHORT);
                             setLocation(getLocation(), null);
                         } else
                             Dlg.toast(FileChooserActivity.this,
                                     getString(R.string.afc_pmsg_cannot_create_folder, name), Dlg.LENGTH_SHORT);
                     }// onClick()
                 });
         fDlg.show();
     }// doCreateNewDir()
 
     /**
      * Deletes a file.
      * 
      * @param file
      *            {@link IFile}
      */
     private void doDeleteFile(final DataModel fData) {
         if (mFileProvider instanceof LocalFileProvider
                 && !Utils.havePermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
             Dlg.toast(this, R.string.afc_msg_app_doesnot_have_permission_to_delete_files, Dlg.LENGTH_SHORT);
             return;
         }
 
         Dlg.confirmYesno(
                 this,
                 getString(R.string.afc_pmsg_confirm_delete_file,
                         fData.getFile().isFile() ? getString(R.string.afc_file) : getString(R.string.afc_folder), fData
                                 .getFile().getName()), new DialogInterface.OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         new LoadingDialog(FileChooserActivity.this, getString(R.string.afc_pmsg_deleting_file, fData
                                 .getFile().isFile() ? getString(R.string.afc_file) : getString(R.string.afc_folder),
                                 fData.getFile().getName()), true) {
 
                             private Thread mThread = Utils.createDeleteFileThread(fData.getFile(), mFileProvider, true);
                             private final boolean fIsFile = fData.getFile().isFile();
 
                             private void notifyFileDeleted() {
                                 mFileAdapter.remove(fData);
                                 mFileAdapter.notifyDataSetChanged();
 
                                 mHistory.removeAll(new HistoryFilter<IFile>() {
 
                                     final String fPath = fData.getFile().getAbsolutePath();
 
                                     @Override
                                     public boolean accept(IFile item) {
                                         return item.getAbsolutePath().equals(fPath);
                                     }
                                 });
                                 // TODO remove all duplicate history items
 
                                 Dlg.toast(
                                         FileChooserActivity.this,
                                         getString(
                                                 R.string.afc_pmsg_file_has_been_deleted,
                                                 fIsFile ? getString(R.string.afc_file) : getString(R.string.afc_folder),
                                                 fData.getFile().getName()), Dlg.LENGTH_SHORT);
                             }// notifyFileDeleted()
 
                             @Override
                             protected void onPreExecute() {
                                 super.onPreExecute();
                                 mThread.start();
                             }// onPreExecute()
 
                             @Override
                             protected Object doInBackground(Void... arg0) {
                                 while (mThread.isAlive()) {
                                     try {
                                         Thread.sleep(500);
                                     } catch (InterruptedException e) {
                                         mThread.interrupt();
                                     }
                                 }
                                 return null;
                             }// doInBackground()
 
                             @Override
                             protected void onCancelled() {
                                 mThread.interrupt();
 
                                 if (fData.getFile().exists())
                                     Dlg.toast(FileChooserActivity.this, R.string.afc_msg_cancelled, Dlg.LENGTH_SHORT);
                                 else
                                     notifyFileDeleted();
 
                                 super.onCancelled();
                             }// onCancelled()
 
                             @Override
                             protected void onPostExecute(Object result) {
                                 super.onPostExecute(result);
 
                                 if (fData.getFile().exists())
                                     Dlg.toast(
                                             FileChooserActivity.this,
                                             getString(R.string.afc_pmsg_cannot_delete_file,
                                                     fData.getFile().isFile() ? getString(R.string.afc_file)
                                                             : getString(R.string.afc_folder), fData.getFile().getName()),
                                             Dlg.LENGTH_SHORT);
                                 else
                                     notifyFileDeleted();
                             }// onPostExecute()
                         }.execute();// LoadingDialog
                     }// onClick()
                 });
     }// doDeleteFile()
 
     /**
      * As the name means.
      * 
      * @param filename
      * @since v1.91
      */
    private void checkSaveasFilenameAndFinish(String filename) {
         if (filename.length() == 0) {
             Dlg.toast(this, R.string.afc_msg_filename_is_empty, Dlg.LENGTH_SHORT);
         } else {
             final IFile fFile = mFileProvider.fromPath(getLocation().getAbsolutePath() + File.separator + filename);
 
             if (!Utils.isFilenameValid(filename)) {
                 Dlg.toast(this, getString(R.string.afc_pmsg_filename_is_invalid, filename), Dlg.LENGTH_SHORT);
             } else if (fFile.isFile()) {
                 new AlertDialog.Builder(FileChooserActivity.this)
                         .setMessage(getString(R.string.afc_pmsg_confirm_replace_file, fFile.getName()))
                         .setNegativeButton(android.R.string.cancel, null)
                         .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 doFinish(fFile);
                             }
                         }).show();
             } else if (fFile.isDirectory()) {
                 Dlg.toast(this, getString(R.string.afc_pmsg_filename_is_directory, fFile.getName()), Dlg.LENGTH_SHORT);
             } else
                 doFinish(fFile);
         }
    }// checkSaveasFilenameAndFinish()
 
     /**
      * Gets current location.
      * 
      * @return current location.
      */
     private IFile getLocation() {
         return (IFile) mViewLocations.getTag();
     }// getLocation()
 
     /**
      * Sets current location
      * 
      * @param fPath
      *            the path
      * @param fListener
      *            {@link TaskListener}
      */
     private void setLocation(final IFile fPath, final TaskListener fListener) {
         // TODO: let the user to be able to cancel the task
         new LoadingDialog(this, R.string.afc_msg_loading, false) {
 
             // IFile[] files = new IFile[0];
             List<IFile> files;
             boolean hasMoreFiles[] = { false };
             int shouldBeSelectedIdx = -1;
             final String fLastPath = getLocation() != null ? getLocation().getAbsolutePath() : null;
 
             @Override
             protected Object doInBackground(Void... params) {
                 try {
                     files = mFileProvider.listAllFiles(fPath, hasMoreFiles);
                    if (files != null && fLastPath != null) {
                         for (int i = 0; i < files.size(); i++) {
                             IFile f = files.get(i);
                             if (f.isDirectory() && fLastPath.startsWith(f.getAbsolutePath())) {
                                 shouldBeSelectedIdx = i;
                                 break;
                             }
                         }
                     }
                 } catch (Throwable t) {
                     setLastException(t);
                     cancel(false);
                 }
                 return null;
             }// doInBackground()
 
             @Override
             protected void onPostExecute(Object result) {
                 super.onPostExecute(result);
 
                 if (files == null) {
                     Dlg.toast(FileChooserActivity.this,
                             getString(R.string.afc_pmsg_cannot_access_dir, fPath.getName()), Dlg.LENGTH_SHORT);
                     if (fListener != null)
                         fListener.onFinish(false, null);
                     return;
                 }
 
                 // update list view
 
                 mFileAdapter.clear();
                 for (IFile f : files)
                     mFileAdapter.add(new DataModel(f));
                 mFileAdapter.notifyDataSetChanged();
 
                 // update footers
 
                 mFooterView.setVisibility(hasMoreFiles[0] || mFileAdapter.isEmpty() ? View.VISIBLE : View.GONE);
                 if (hasMoreFiles[0])
                     mFooterView.setText(getString(R.string.afc_pmsg_max_file_count_allowed,
                             mFileProvider.getMaxFileCount()));
                 else if (mFileAdapter.isEmpty())
                     mFooterView.setText(R.string.afc_msg_empty);
 
                 if (shouldBeSelectedIdx >= 0 && shouldBeSelectedIdx < mFileAdapter.getCount())
                     mViewFiles.setSelection(shouldBeSelectedIdx);
                 else if (!mFileAdapter.isEmpty())
                     mViewFiles.setSelection(0);
 
                 /*
                  * navigation buttons
                  */
                 createLocationButtons(fPath);
 
                 if (fListener != null)
                     fListener.onFinish(true, null);
             }// onPostExecute()
         }.execute();// new LoadingDialog()
     }// setLocation()
 
     private void createLocationButtons(IFile path) {
         mViewLocations.setTag(path);
         mViewLocations.removeAllViews();
 
         LinearLayout.LayoutParams lpBtnLoc = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                 LinearLayout.LayoutParams.WRAP_CONTENT);
         lpBtnLoc.gravity = Gravity.CENTER;
         LinearLayout.LayoutParams lpDivider = null;
         LayoutInflater inflater = getLayoutInflater();
         final int fDim = getResources().getDimensionPixelSize(R.dimen.afc_dim_5dp);
         int count = 0;
         while (path != null) {
             Button btnLoc = (Button) inflater.inflate(R.layout.afc_button_location, null);
             btnLoc.setText(path.parentFile() != null ? path.getName() : getString(R.string.afc_root));
             btnLoc.setTag(path);
             btnLoc.setOnClickListener(mBtnLocationOnClickListener);
             btnLoc.setOnLongClickListener(mBtnLocationOnLongClickListener);
             mViewLocations.addView(btnLoc, 0, lpBtnLoc);
 
             if (count++ == 0) {
                 Rect r = new Rect();
                 btnLoc.getPaint().getTextBounds(path.getName(), 0, path.getName().length(), r);
                if (r.width() >= getResources().getDimensionPixelSize(R.dimen.afc_button_location_max_width)) {
                     mTxtFullDirName.setText(path.getName());
                     mTxtFullDirName.setVisibility(View.VISIBLE);
                 } else
                     mTxtFullDirName.setVisibility(View.GONE);
             }
 
             path = path.parentFile();
             if (path != null) {
                 View divider = inflater.inflate(R.layout.afc_view_locations_divider, null);
 
                 if (lpDivider == null) {
                     lpDivider = new LinearLayout.LayoutParams(fDim, fDim);
                     lpDivider.gravity = Gravity.CENTER;
                     lpDivider.setMargins(fDim, fDim, fDim, fDim);
                 }
                 mViewLocations.addView(divider, 0, lpDivider);
             }
         }
 
         mViewLocationsContainer.postDelayed(new Runnable() {
 
             public void run() {
                 mViewLocationsContainer.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
             }
         }, 100);
     }// createLocationButtons()
 
     /**
      * Finishes this activity.
      * 
      * @param files
      *            list of {@link IFile}
      */
     private void doFinish(IFile... files) {
         List<IFile> list = new ArrayList<IFile>();
         for (IFile f : files)
             list.add(f);
         doFinish((ArrayList<IFile>) list);
     }
 
     /**
      * Finishes this activity.
      * 
      * @param files
      *            list of {@link IFile}
      */
     private void doFinish(ArrayList<IFile> files) {
         Intent intent = new Intent();
 
         // set results
         intent.putExtra(_Results, files);
 
         // return flags for further use (in case the caller needs)
         intent.putExtra(_FilterMode, mFileProvider.getFilterMode());
         intent.putExtra(_SaveDialog, mIsSaveDialog);
 
         setResult(RESULT_OK, intent);
         finish();
     }
 
     /**********************************************************
      * BUTTON LISTENERS
      */
 
     private final View.OnClickListener mBtnGoBackOnClickListener = new View.OnClickListener() {
 
         @Override
         public void onClick(View v) {
             /*
              * if user deleted a dir which was one in history, then maybe there
              * are duplicates, so we check and remove them here
              */
             IFile currentLoc = getLocation();
             IFile preLoc = null;
             while (currentLoc.equalsToPath(preLoc = mHistory.prevOf(currentLoc)))
                 mHistory.remove(preLoc);
 
             if (preLoc != null) {
                 setLocation(preLoc, new TaskListener() {
 
                     @Override
                     public void onFinish(boolean ok, Object any) {
                         if (ok) {
                             mBtnGoBack.setEnabled(mHistory.prevOf(getLocation()) != null);
                             mBtnGoForward.setEnabled(true);
                         }
                     }
                 });
             } else {
                 mBtnGoBack.setEnabled(false);
             }
         }
     };// mBtnGoBackOnClickListener
 
     private final View.OnClickListener mBtnLocationOnClickListener = new View.OnClickListener() {
 
         @Override
         public void onClick(View v) {
             IFile path = (IFile) v.getTag();
             if (!getLocation().equals(path)) {
                 final IFile fLastPath = getLocation();
                 setLocation(path, new TaskListener() {
 
                     @Override
                     public void onFinish(boolean ok, Object any) {
                         if (ok)
                             mHistory.push(fLastPath, getLocation());
                     }
                 });// setLocation()
             }
         }
     };// mBtnLocationOnClickListener
 
     private final View.OnLongClickListener mBtnLocationOnLongClickListener = new View.OnLongClickListener() {
 
         @Override
         public boolean onLongClick(View v) {
             if (mFileProvider.getFilterMode() == IFileProvider.FilterMode.FilesOnly || mIsSaveDialog)
                 return false;
 
             doFinish((IFile) v.getTag());
 
             return false;
         }
 
     };// mBtnLocationOnLongClickListener
 
     private final View.OnClickListener mBtnGoForwardOnClickListener = new View.OnClickListener() {
 
         @Override
         public void onClick(View v) {
             /*
              * if user deleted a dir which was one in history, then maybe there
              * are duplicates, so we check and remove them here
              */
             IFile currentLoc = getLocation();
             IFile nextLoc = null;
             while (currentLoc.equalsToPath(nextLoc = mHistory.nextOf(currentLoc)))
                 mHistory.remove(nextLoc);
 
             if (nextLoc != null) {
                 setLocation(nextLoc, new TaskListener() {
 
                     @Override
                     public void onFinish(boolean ok, Object any) {
                         if (ok) {
                             mBtnGoBack.setEnabled(true);
                             mBtnGoForward.setEnabled(mHistory.nextOf(getLocation()) != null);
                         }
                     }
                 });
             } else {
                 mBtnGoForward.setEnabled(false);
             }
         }
     };// mBtnGoForwardOnClickListener
 
     private final TextView.OnEditorActionListener mTxtFilenameOnEditorActionListener = new TextView.OnEditorActionListener() {
 
         @Override
         public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
             if (actionId == EditorInfo.IME_ACTION_DONE) {
                 UI.hideSoftKeyboard(FileChooserActivity.this, mTxtSaveas.getWindowToken());
                 mBtnOk.performClick();
                 return true;
             }
             return false;
         }
     };// mTxtFilenameOnEditorActionListener
 
     private final View.OnClickListener mBtnOk_SaveDialog_OnClickListener = new View.OnClickListener() {
 
         @Override
         public void onClick(View v) {
             UI.hideSoftKeyboard(FileChooserActivity.this, mTxtSaveas.getWindowToken());
             String filename = mTxtSaveas.getText().toString().trim();
            checkSaveasFilenameAndFinish(filename);
         }
     };// mBtnOk_SaveDialog_OnClickListener
 
     private final View.OnClickListener mBtnOk_OpenDialog_OnClickListener = new View.OnClickListener() {
 
         @Override
         public void onClick(View v) {
             List<IFile> list = new ArrayList<IFile>();
             for (int i = 0; i < mViewFiles.getAdapter().getCount(); i++) {
                 // NOTE: header and footer don't have data
                 Object obj = mViewFiles.getAdapter().getItem(i);
                 if (obj instanceof DataModel) {
                     DataModel dm = (DataModel) obj;
                     if (dm.isSelected())
                         list.add(dm.getFile());
                 }
             }
             doFinish((ArrayList<IFile>) list);
         }
     };// mBtnOk_OpenDialog_OnClickListener
 
     private final GestureDetector mListviewFilesGestureDetector = new GestureDetector(
             new GestureDetector.SimpleOnGestureListener() {
 
                 private Animation mInAnimation;
                 private Animation mOutAnimation;
 
                 private void prepareAnimations(boolean isLeftToRight) {
                     mInAnimation = AnimationUtils.loadAnimation(FileChooserActivity.this,
                             isLeftToRight ? R.anim.afc_push_left_in : R.anim.afc_push_right_in);
                     mOutAnimation = AnimationUtils.loadAnimation(FileChooserActivity.this,
                             isLeftToRight ? R.anim.afc_push_left_out : R.anim.afc_push_right_out);
                 }
 
                 private Object getData(float x, float y) {
                     int i = getSubViewId(x, y);
                     if (i >= 0)
                         return mViewFiles.getItemAtPosition(mViewFiles.getFirstVisiblePosition() + i);
                     return null;
                 }// getSubView()
 
                 private View getSubView(float x, float y) {
                     int i = getSubViewId(x, y);
                     if (i >= 0)
                         return mViewFiles.getChildAt(i);
                     return null;
                 }// getSubView()
 
                 private int getSubViewId(float x, float y) {
                     Rect r = new Rect();
                     for (int i = 0; i < mViewFiles.getChildCount(); i++) {
                         mViewFiles.getChildAt(i).getHitRect(r);
                         if (r.contains((int) x, (int) y))
                             return i;
                     }
 
                     return -1;
                 }// getSubViewId()
 
                 @Override
                 public boolean onSingleTapConfirmed(MotionEvent e) {
                     Object o = getData(e.getX(), e.getY());
                     if (!(o instanceof DataModel))
                         return true;
 
                     DataModel data = (DataModel) o;
                     if (data.getFile().isDirectory()) {
                         final IFile fLastPath = getLocation();
                         setLocation(data.getFile(), new TaskListener() {
 
                             @Override
                             public void onFinish(boolean ok, Object any) {
                                 if (ok)
                                     mHistory.push(fLastPath, getLocation());
                             }
                         });
                     } else {
                         if (mIsSaveDialog)
                             mTxtSaveas.setText(data.getFile().getName());
                     }
 
                     return false;
                 }// onSingleTapConfirmed()
 
                 @Override
                 public boolean onDoubleTap(MotionEvent e) {
                     if (mIsMultiSelection)
                         return false;
 
                     Object o = getData(e.getX(), e.getY());
                     if (!(o instanceof DataModel))
                         return false;
 
                     DataModel data = (DataModel) o;
 
                     if (data.getFile().isDirectory()
                             && mFileProvider.getFilterMode() == IFileProvider.FilterMode.FilesOnly)
                         return false;
 
                     // if fFilterMode == DirectoriesOnly, files won't be
                     // shown
 
                     doFinish(data.getFile());
 
                     return false;
                 }// onDoubleTap()
 
                 @Override
                 public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                     final int fMax_y_distance = 19;// 10 is too short :-D
                     final int fMin_x_distance = 80;
                     final int fMin_x_velocity = 200;
                     if (Math.abs(e1.getY() - e2.getY()) < fMax_y_distance
                             && Math.abs(e1.getX() - e2.getX()) > fMin_x_distance
                             && Math.abs(velocityX) > fMin_x_velocity) {
                         Object o = getData(e1.getX(), e1.getY());
                         if (o instanceof DataModel) {
                             View v = getSubView(e1.getX(), e1.getY());
                             if (v != null && v instanceof ViewFlipper) {
                                 prepareAnimations(velocityX <= 0);
                                 ((ViewFlipper) v).setInAnimation(mInAnimation);
                                 ((ViewFlipper) v).setOutAnimation(mOutAnimation);
                                 ((ViewFlipper) v).showNext();
                             }
                             doDeleteFile((DataModel) o);
                         }
                     }
 
                     return false;
                 }
             });// mListviewFilesGestureDetector
 }
