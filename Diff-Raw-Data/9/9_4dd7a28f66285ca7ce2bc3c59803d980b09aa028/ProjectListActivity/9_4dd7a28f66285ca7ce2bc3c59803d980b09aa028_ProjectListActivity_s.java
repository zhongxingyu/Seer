 package io.appery.tester;
 
 import android.annotation.TargetApi;
 import android.content.SharedPreferences;
 import android.graphics.Canvas;
 import android.graphics.ColorMatrix;
 import android.graphics.ColorMatrixColorFilter;
 import android.graphics.Paint;
 import android.os.Build;
 import android.preference.PreferenceManager;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.widget.*;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.SubMenu;
 import com.slidingmenu.lib.SlidingMenu;
 import io.appery.tester.adaptors.FolderAdapter;
 import io.appery.tester.adaptors.ProjectListAdapter;
 import io.appery.tester.comparators.ProjectComparator;
 import io.appery.tester.data.Project;
 import io.appery.tester.net.api.BaseResponse;
 import io.appery.tester.net.api.GetProjectList;
 import io.appery.tester.net.api.callback.ProjectListCallback;
 import io.appery.tester.tasks.DownloadFileTask;
 import io.appery.tester.tasks.callback.DownloadFileCallback;
 import io.appery.tester.utils.Constants;
 import io.appery.tester.utils.FileUtils;
 import io.appery.tester.utils.IntentUtils;
 import io.appery.tester.utils.ProjectStorageManager;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.*;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView.OnItemClickListener;
 
 /**
  * @author Daniel Lukashevich
  */
 public class ProjectListActivity extends BaseActivity implements ProjectListCallback, DownloadFileCallback {
 
     // UI
     private ListView mProjectListView;
 
     // Data
     private List<Project> projectList = new ArrayList<Project>();
     private Project selectedProject;
     private int sortBy = ProjectComparator.BY_EDIT_DATE;
 
     // User
     private Long userId = 0l;
 
     private String[] PROJECTS_ACTIONS;
 
     private String DEBUG_ON_SERVICE_PARAM = "debug=true";
 
     private String DEBUG_OFF_SERVICE_PARAM = "debug=false";
 
     private String RUN_PROJECT_ACTION;
     public static  final String ALL_FOLDERS = "ALL";
     public static  final String MY_FOLDER = "My folder";
     public static  final int ALL_FOLDERS_POSITION = 0;
     public static  final int MY_FOLDER_POSITION = 1;
 
 
     private ProjectListAdapter mProjectAdapter;
     private SlidingMenu mSlidingMenu;
     FolderAdapter mFolderAdapter;
 
     List<String> mFolders = new ArrayList<String>();
 
     // used to anabled hardware acceleration for sliding menu
     private final Paint paint = new Paint();
     private final ColorMatrix matrix = new ColorMatrix();
     private ImageView  mIndicatorView;
 
     @SuppressWarnings("unchecked")
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
         
         setContentView(R.layout.projects);
 
 
         if (savedInstanceState != null) {
             sortBy = savedInstanceState.getInt(Constants.EXTRAS.SORT_BY, ProjectComparator.BY_EDIT_DATE);
             projectList = (List<Project>) savedInstanceState.get(Constants.EXTRAS.PROJECTS_LIST);
             selectedProject = (Project) savedInstanceState.get(Constants.EXTRAS.SELECTED_PROJECT);
         }
 
         mProjectListView = (ListView) findViewById(R.id.project_list);
         mProjectListView.setOnItemClickListener(new OnItemClickListener() {
 
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                 selectedProject = (Project) mProjectListView.getItemAtPosition(pos);
                 showDialog(Constants.DIALOGS.PROJECT_ACTION);
             }
         });
         mProjectAdapter = new ProjectListAdapter(ProjectListActivity.this, projectList);
         mProjectListView.setAdapter(mProjectAdapter);
 
         userId = getIntent().getLongExtra(Constants.EXTRAS.USER_ID, -1);
 
         // request for projects
         GetProjectList getProjectList = new GetProjectList(getRestManager(), this);
         showDialog(Constants.DIALOGS.PROGRESS);
         getProjectList.execute();
 
         // preparing project actions list
         this.RUN_PROJECT_ACTION = getString(R.string.project_action_run);
         String[] PROJECTS_ACTIONS = { this.RUN_PROJECT_ACTION, //this.DEBUG_PROJECT_ACTION,
                 getString(R.string.project_action_cancel) };
         this.PROJECTS_ACTIONS = PROJECTS_ACTIONS;
 
         // folders
         mFolders.add(ALL_FOLDERS);
 
         mFolderAdapter  = new FolderAdapter(this, mFolders ,getResources().getColor(android.R.color.holo_blue_light));
         customizeActionBar();
         mSlidingMenu = addSlidingMenu();
 
     }
 
     /***
      * add custom sliding menu
      * @return
      */
     private SlidingMenu addSlidingMenu(){
         SlidingMenu menu = new SlidingMenu(this);
         menu.setMode(SlidingMenu.LEFT);
         menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
         menu.setShadowWidthRes(R.dimen.shadow_width);
         menu.setShadowDrawable(R.drawable.shadow);
         menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
         menu.setFadeDegree(0.35f);
         menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
         menu.setMenu(R.layout.slidingmenu);
 
         ListView lwFolders = (ListView)findViewById(R.id.mn_lw_folders);
         lwFolders.setAdapter(mFolderAdapter);
         lwFolders.setOnItemClickListener(onFolderClick);
 
         findViewById(R.id.mn_bt_refresh).setOnClickListener(onRefreshClick);
 
         menu.setBehindCanvasTransformer(new SlidingMenu.CanvasTransformer() {
             @Override
             public void transformCanvas(Canvas canvas, float percentOpen) {
                 //TODO uncommit when 4.2 maven repository android released
                 //boolean API_17 = Build.VERSION.SDK_INT >= 17;
                 //boolean API_16 = Build.VERSION.SDK_INT == 16;
                 boolean API_17 = false;
                 boolean API_16 = Build.VERSION.SDK_INT >= 16;
 
                if (API_16) {
                    prepareLayerHack();
                }
 
                manageLayers(percentOpen);
                 updateColorFilter(percentOpen);
                 updatePaint(API_17, API_16);
             }
         });
 
         menu.setOnOpenListener(new SlidingMenu.OnOpenListener() {
             @Override
             public void onOpen() {
                 mIndicatorView.setImageResource(R.drawable.icon_menu_open_unpressed);
             }
         });
         menu.setOnClosedListener(new SlidingMenu.OnClosedListener() {
             @Override
             public void onClosed() {
                 mIndicatorView.setImageResource(R.drawable.icon_menu_closed_unpressed);
             }
         });
         return menu;
     }
 
     @TargetApi(17)
     private void updatePaint(boolean API_17, boolean API_16) {
         View backView = mSlidingMenu.getMenu();
         if (API_17) {
             //TODO uncommit when 4.2 maven repository android released
             //   backView.setLayerPaint(paint);
         } else {
             if (API_16) {
                 if (sHackAvailable) {
                     try {
                         sRecreateDisplayList.setBoolean(backView, true);
                         sGetDisplayList.invoke(backView, (Object[]) null);
                     } catch (IllegalArgumentException e) {
                     } catch (IllegalAccessException e) {
                     } catch (InvocationTargetException e) {
                     }
                 } else {
                     // This solution is slow
                     mSlidingMenu.getMenu().invalidate();
                 }
             }
 
             // API level < 16 doesn't need the hack above, but the invalidate is required
             ((View) backView.getParent()).postInvalidate(backView.getLeft(), backView.getTop(),
                     backView.getRight(), backView.getBottom());
         }
     }
     private void updateColorFilter(float percentOpen) {
         matrix.setSaturation(percentOpen);
         ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
         paint.setColorFilter(filter);
     }
 
     private void manageLayers(float percentOpen) {
         boolean layer = percentOpen > 0.0f && percentOpen < 1.0f;
         int layerType = layer ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;
 
         if (layerType != mSlidingMenu.getContent().getLayerType()) {
             mSlidingMenu.getContent().setLayerType(layerType, null);
             mSlidingMenu.getMenu().setLayerType(layerType, Build.VERSION.SDK_INT <= 16 ? paint : null);
         }
     }
 
     private static boolean sHackReady;
     private static boolean sHackAvailable;
     private static Field sRecreateDisplayList;
     private static Method sGetDisplayList;
 
     private static void prepareLayerHack() {
         if (!sHackReady) {
             try {
                 sRecreateDisplayList = View.class.getDeclaredField("mRecreateDisplayList");
                 sRecreateDisplayList.setAccessible(true);
 
                 sGetDisplayList = View.class.getDeclaredMethod("getDisplayList", (Class<?>) null);
                 sGetDisplayList.setAccessible(true);
 
                 sHackAvailable = true;
             } catch (NoSuchFieldException e) {
             } catch (NoSuchMethodException e) {
             }
             sHackReady = true;
         }
     }
 
 
 
     OnItemClickListener onFolderClick = new OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {
             mFolderAdapter.setSelected(i,view);
             updateProjectsList(sortBy);
             mSlidingMenu.toggle();
         }
     };
     View.OnClickListener onRefreshClick = new View.OnClickListener() {
 
         @Override
         public void onClick(View view) {
             GetProjectList getProjectList = new GetProjectList(ProjectListActivity.this.getRestManager(), ProjectListActivity.this);
             showDialog(Constants.DIALOGS.PROGRESS);
             getProjectList.execute();
         }
     } ;
 
     /***
      * Add indicator  ,  tittle and submenu to actionbar
      */
     private void customizeActionBar(){
         ActionBar bar = getSupportActionBar();
         View title = LayoutInflater.from(this).inflate(R.layout.actionbar_tittle , null , false);
         mIndicatorView = (ImageView)title.findViewById(R.id.im_indicator);
         mIndicatorView.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                    mSlidingMenu.toggle(true);
             }
         });
 
         FrameLayout.LayoutParams prms = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                 FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL);
 
         title.setLayoutParams(prms);
         bar.setCustomView(title);
         bar.setDisplayShowCustomEnabled(true);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         /*
          * selectedProject = null; projectList = null;
          */
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         if (selectedProject != null) {
             outState.putSerializable(Constants.EXTRAS.SELECTED_PROJECT, selectedProject);
             outState.putSerializable(Constants.EXTRAS.PROJECTS_LIST, (Serializable) projectList);
             outState.putSerializable(Constants.EXTRAS.SORT_BY, sortBy);
         }
     }
 
     @Override
     public void onProjectListReceived(final List<Project> projects, final BaseResponse response) {
 
         runOnUiThread(new Runnable() {
 
             @Override
             public void run() {
 
                 try {
                     dismissDialog(Constants.DIALOGS.PROGRESS);
                 } catch (Exception e) {
                 }
 
                 if (response.hasError()) {
                     showToast(response.getMessage());
 
                     //finish();
                     return;
                 }
 
                 projectList = projects;
                 updateFolders(projects);
                 updateProjectsList(ProjectComparator.BY_EDIT_DATE);
             }
         });
 
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
         case Constants.DIALOGS.PROJECT_ACTION:
             AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder.setItems(PROJECTS_ACTIONS, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int item) {
                     // Debug mode
                     removeDialog(Constants.DIALOGS.PROJECT_ACTION);
                     // Run without debug
                     if (PROJECTS_ACTIONS[item].equals(RUN_PROJECT_ACTION)) {
                         DownloadFileTask getApkTask = new DownloadFileTask(ProjectListActivity.this,
                                 Constants.FILENAME_ZIP, ProjectListActivity.this);
                         getApkTask.execute(selectedProject.getResourcesLink() + "?" + DEBUG_OFF_SERVICE_PARAM);
                     }
 
                 }
             });
 
             return builder.create();
         case Constants.DIALOGS.SORT:
             builder = new AlertDialog.Builder(this);
             builder.setTitle(R.string.sort_order);
             builder.setItems(new CharSequence[] { getString(R.string.sort_by_name),
                     getString(R.string.sort_by_createdate ) , getString(R.string.sort_by_modifydate) },
                     new DialogInterface.OnClickListener() {
 
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             updateProjectsList(which);
                         }
                     });
 
             return builder.create();
         default:
             break;
         }
         return super.onCreateDialog(id);
     }
 
 
     @Override
     protected void onPrepareDialog(int id, Dialog dialog) {
         switch (id) {
         case Constants.DIALOGS.PROJECT_ACTION:
             dialog.setTitle(selectedProject.getName());
             break;
         default:
             break;
         }
         super.onPrepareDialog(id, dialog);
     }
 
     @Override
     public void onFileDownloaded(File file) {
 
         if (file == null) {
             Toast.makeText(this, getString(R.string.file_download_error_toast), Toast.LENGTH_LONG).show();
             return;
         }
 
 
         //  TODO - Check filename and do what you need
         // apk file - install
         String fName = file.getName();
         if (Constants.FILENAME_APK.equals(fName)) {
             boolean install = new IntentUtils(this).installApk(file);
             if (!install) {
                 showToast(getString(R.string.application_download_error_toast));
             }
         } else if (Constants.FILENAME_ZIP.equals(fName)) {
             // Unzip
             String dirPath = ProjectStorageManager.getWORK_DIRECTORY();
 
             try {
                 FileUtils.checkDir(dirPath);
                 FileUtils.clearDirectory(dirPath);
 
                 FileUtils.unzip(ProjectStorageManager.getPROJECT_ZIP_FILE(), dirPath);
                 startActivity(ApperyActivity.class);
             } catch (IOException e) {
                 e.printStackTrace();
                 showToast(getString(R.string.preview_error_toast));
             }
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
 
         SubMenu subMenu = menu.addSubMenu(getString(R.string.sort_mi));
         subMenu.add(0, Constants.MENU_OPTIONS.REFRESH, Menu.NONE, R.string.refresh_mi);
         subMenu.add(0, Constants.MENU_OPTIONS.SELECT_FOLDER,Menu.NONE, R.string.folders);
         subMenu.add(0, Constants.MENU_OPTIONS.SORT_BY_NAME, Menu.NONE, R.string.sort_by_name);
         subMenu.add(0, Constants.MENU_OPTIONS.SORT_BY_CREATE,Menu.NONE, R.string.sort_by_createdate);
         subMenu.add(0, Constants.MENU_OPTIONS.SORT_BY_EDIT,Menu.NONE, R.string.sort_by_modifydate);
         subMenu.add(0, Constants.MENU_OPTIONS.LOGOUT,Menu.NONE, R.string.logout_mi);
 
         MenuItem subMenu1Item = subMenu.getItem();
         subMenu1Item.setIcon(R.drawable.abs__ic_menu_moreoverflow_normal_holo_light);
         subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS );
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 
         switch (item.getItemId()) {
         case Constants.MENU_OPTIONS.REFRESH:
             GetProjectList getProjectList = new GetProjectList(getRestManager(), this);
             showDialog(Constants.DIALOGS.PROGRESS);
             getProjectList.execute();
             return true;
         case Constants.MENU_OPTIONS.SORT:
             showDialog(Constants.DIALOGS.SORT);
             return true;
         case Constants.MENU_OPTIONS.SORT_BY_NAME:
             updateProjectsList(ProjectComparator.BY_NAME);
             break;
         case Constants.MENU_OPTIONS.SORT_BY_EDIT:
             updateProjectsList(ProjectComparator.BY_EDIT_DATE);
             break;
         case Constants.MENU_OPTIONS.SORT_BY_CREATE:
             updateProjectsList(ProjectComparator.BY_CREATE_DATE);
             break;
         case  Constants.MENU_OPTIONS.SELECT_FOLDER:
             mSlidingMenu.showMenu(true);
             break;
          case Constants.MENU_OPTIONS.LOGOUT :
              // for action logout we simply go on login activity
              // in login activity onresume there is action to perform logout
             finish();
             break;
 
         default:
             break;
         }
 
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         // The activity is about to be destroyed.
         Log.e("ProjectListActivity", "***onDestroy***");
     }
 
     private void updateProjectsList(int orderBy) {
         Comparator<Project> comparator;
         sortBy = orderBy;
 
         switch (orderBy){
             case  ProjectComparator.BY_NAME :
                 comparator = new ProjectComparator(ProjectComparator.BY_NAME, ProjectComparator.ASC);
                 break;
             case ProjectComparator.BY_CREATE_DATE:
                 comparator = new ProjectComparator(ProjectComparator.BY_CREATE_DATE, ProjectComparator.DESC);
                 break;
             default:
                 comparator = new ProjectComparator(ProjectComparator.BY_EDIT_DATE, ProjectComparator.DESC);
                 break;
         }
 
         Collections.sort(projectList, comparator);
         mProjectAdapter.setmProjectList(getFilteredProjects());
         mProjectAdapter.notifyDataSetChanged();
     }
 
     private void updateFolders(List<Project> projects){
         List<String> owners = new ArrayList<String>();
         String  username = getPreferenceAsString(Constants.PREFERENCES.USERNAME, "");
         for(Project pr : projects)
             if (!username.equals(pr.getOwner())&& !owners.contains(pr.getOwner()))
                 owners.add(pr.getOwner());
         Collections.sort(owners);
 
         mFolders.clear();
 
         mFolders.add(ALL_FOLDERS);
         mFolders.add(MY_FOLDER);
         mFolders.addAll(owners);
 
         mFolderAdapter.selected=0;
         mFolderAdapter.setFolders(mFolders);
         mFolderAdapter.notifyDataSetChanged();
 
     }
 
     /**
      * get list projects by owner
      * if user select ALL = return all )
      * @return
      */
     private List<Project> getFilteredProjects(){
         if (mFolderAdapter.getSelected()==ALL_FOLDERS_POSITION) return projectList;
         String owner ;
         if (mFolderAdapter.getSelected()==MY_FOLDER_POSITION)
             owner = getPreferenceAsString(Constants.PREFERENCES.USERNAME,"");
         else
             owner = mFolders.get(mFolderAdapter.getSelected());
 
         List<Project> res = new ArrayList<Project>();
         for(Project p:projectList){
             if(owner.equals(p.getOwner()))
                 res.add(p);
         }
         return res;
     }
 
 
 
 
 }
