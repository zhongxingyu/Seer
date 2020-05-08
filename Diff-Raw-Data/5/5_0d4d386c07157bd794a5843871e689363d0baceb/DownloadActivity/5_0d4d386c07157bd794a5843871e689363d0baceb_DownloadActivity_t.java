 package com.novel.reader;
 
 import java.util.ArrayList;
 import java.util.TreeMap;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ExpandableListView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.kosbrother.tool.ChildArticle;
 import com.kosbrother.tool.ExpandListDownLoadAdapter;
 import com.kosbrother.tool.Group;
 import com.novel.reader.api.NovelAPI;
 import com.novel.reader.api.Setting;
 import com.novel.reader.entity.Article;
 import com.novel.reader.service.DownloadService;
 
 public class DownloadActivity extends SherlockFragmentActivity {
 
     private static final int                          ID_SELECT_ALL  = 0;
     private static final int                          ID_SELECT_NONE = 1;
     private Bundle                                    mBundle;
     private String                                    novelName;
     private int                                       novelId;
     private Button                                    downLoadButton;
     public static TextView                            downLoadCountText;
     private LinearLayout                              novelLayoutProgress;
     private ArrayList<Article>                        articleList    = new ArrayList<Article>();
     private ExpandableListView                        novelListView;
 
     private final TreeMap<String, ArrayList<Article>> myData         = new TreeMap<String, ArrayList<Article>>();
     private final ArrayList<Group>                    mGroups        = new ArrayList<Group>();
 
     private Boolean                                   downloadBoolean;
     private ProgressDialog                      	  progressDialog = null;
     private ExpandListDownLoadAdapter                 mAdapter;
     private int                                       downloadCount;
     private AlertDialog.Builder                       remindDialog;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.layout_download);
 
         final ActionBar ab = getSupportActionBar();
         mBundle = this.getIntent().getExtras();
         novelName = mBundle.getString("NovelName");
         novelId = mBundle.getInt("NovelId");
 
         ab.setTitle(novelName);
         ab.setDisplayHomeAsUpEnabled(true);
 
         setViews();
 
         if (Setting.getSetting(Setting.keyOpenDownloadPage, DownloadActivity.this) == 0) {
             remindDialog.show();
             Setting.saveSetting(Setting.keyOpenDownloadPage, 1, DownloadActivity.this);
         }
 
         new DownloadArticlesTask().execute();
 
     }
 
     private void setViews() {
 
         novelLayoutProgress = (LinearLayout) findViewById(R.id.novel_layout_progress);
         novelListView = (ExpandableListView) findViewById(R.id.download_artiles_list);
         downLoadButton = (Button) findViewById(R.id.download_button);
         downLoadCountText = (TextView) findViewById(R.id.download_count_text);
 
         downLoadButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View arg0) {
                 new DownloadToDBTask().execute();
             }
         });
 
         setRemindDialog();
 
     }
 
     private void setRemindDialog() {
         remindDialog = new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.remind_title))
                 .setMessage(getResources().getString(R.string.remind_string))
                 .setPositiveButton(getResources().getString(R.string.yes_string), new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
 
                     }
                 });
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         menu.add(0, ID_SELECT_ALL, 0, getResources().getString(R.string.menu_add_all)).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
         menu.add(0, ID_SELECT_NONE, 1, getResources().getString(R.string.menu_remove_all)).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
 
         return true;
     }
 
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
 
         int itemId = item.getItemId();
         switch (itemId) {
         case android.R.id.home:
             finish();
             // Toast.makeText(this, "home pressed", Toast.LENGTH_LONG).show();
             break;
         case ID_SELECT_ALL: // setting
         	if(articleList.size() != 0){
 	            Toast.makeText(DownloadActivity.this, getResources().getString(R.string.menu_add_all), Toast.LENGTH_SHORT).show();
 	            downloadCount = 0;
 	            for (int i = 0; i < mGroups.size(); i++) {
 	                mGroups.get(i).setChecked(true);
 	                for (int j = 0; j < mGroups.get(i).getChildrenCount(); j++) {
 	                    downloadCount = downloadCount + 1;
 	                    mGroups.get(i).getChildItem(j).setChecked(true);
 	                }
 	            }
 	            mAdapter.notifyDataSetChanged();
 	            downLoadCountText.setText(getResources().getString(R.string.toast_all_collect_title) + Integer.toString(downloadCount)
 	                    + getResources().getString(R.string.toast_all_collect_final));
         	}else{
        		Toast.makeText(DownloadActivity.this, getResources().getString(R.string.toast_downloading_wait), Toast.LENGTH_SHORT).show();
         	}
             break;
         case ID_SELECT_NONE: // response
         	if(articleList.size() != 0){
 	            Toast.makeText(DownloadActivity.this, getResources().getString(R.string.menu_remove_all), Toast.LENGTH_SHORT).show();
 	            downloadCount = 0;
 	            for (int i = 0; i < mGroups.size(); i++) {
 	                mGroups.get(i).setChecked(false);
 	                for (int j = 0; j < mGroups.get(i).getChildrenCount(); j++) {
 	                    mGroups.get(i).getChildItem(j).setChecked(false);
 	                }
 	            }
 	            mAdapter.notifyDataSetChanged();
 	            downLoadCountText.setText(getResources().getString(R.string.toast_all_collect_title) + Integer.toString(downloadCount)
 	                    + getResources().getString(R.string.toast_all_collect_final));
         	}else{
        		Toast.makeText(DownloadActivity.this, getResources().getString(R.string.toast_downloading_wait), Toast.LENGTH_SHORT).show();
         	}
             break;
         }
         return true;
     }
 
     private class DownloadArticlesTask extends AsyncTask {
 
     	
     	@Override
         protected void onPreExecute() {
             super.onPreExecute();
              progressDialog = ProgressDialog.show(DownloadActivity.this, "",getResources().getString(R.string.toast_novel_downloading));
              progressDialog.setCancelable(true);
         }
     	
         @Override
         protected Object doInBackground(Object... params) {
             // TODO Auto-generated method stub
             articleList = NovelAPI.getNovelArticles(novelId, true, DownloadActivity.this);
             return null;
         }
 
         @Override
         protected void onPostExecute(Object result) {
             // TODO Auto-generated method stub
             super.onPostExecute(result);
             novelLayoutProgress.setVisibility(View.GONE);
             progressDialog.cancel();
             if (articleList != null && articleList.size() != 0) {
 
                 // use HashMap || TreeMap to make a parent key
                 for (int i = 0; i < articleList.size(); i++) {
                     if (myData.containsKey(articleList.get(i).getSubject())) {
                         myData.get(articleList.get(i).getSubject()).add(articleList.get(i));
                     } else {
                         // groupTitleList.add(articleList.get(i).getSubject());
 
                         mGroups.add(new Group(articleList.get(i).getSubject()));
                         myData.put(articleList.get(i).getSubject(), new ArrayList<Article>());
                         myData.get(articleList.get(i).getSubject()).add(articleList.get(i));
                     }
                 }
 
                 for (int i = 0; i < mGroups.size(); i++) {
                     ArrayList<Article> articles = myData.get(mGroups.get(i).getTitle());
                     for (int j = 0; j < articles.size(); j++) {
                         mGroups.get(i).addChildrenItem(
                                 new ChildArticle(articles.get(j).getId(), articles.get(j).getNovelId(), "", articles.get(j).getTitle(), articles.get(j)
                                         .getSubject(), articles.get(j).isDownload()));
                     }
                 }
 
                 mAdapter = new ExpandListDownLoadAdapter(DownloadActivity.this, mGroups, downLoadCountText);
                 novelListView.setAdapter(mAdapter);
 
             }
 
         }
     }
 
     private class DownloadToDBTask extends AsyncTask {
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             // progressDialog = ProgressDialog.show(DownloadActivity.this, "",getResources().getString(R.string.toast_novel_downloading));
             // progressDialog.setCancelable(true);
         }
 
         @Override
         protected Object doInBackground(Object... params) {
             // TODO Auto-generated method stub
             ArrayList<Article> checkedArticles = new ArrayList<Article>();
             for (int i = 0; i < mGroups.size(); i++) {
                 for (int j = 0; j < mGroups.get(i).getChildrenCount(); j++) {
                     ChildArticle aChildArticle = mGroups.get(i).getChildItem(j);
                     if (aChildArticle.getChecked() && !aChildArticle.isDownload()) {
                         checkedArticles.add(new Article(aChildArticle.getId(), aChildArticle.getNovelId(), aChildArticle.getText(), aChildArticle.getTitle(),
                                 aChildArticle.getSubject(), aChildArticle.isDownload()));
                     }
                 }
             }
             // downloadBoolean = NovelAPI.downloadArticles(novelId, checkedArticles, DownloadActivity.this);
 
             Intent i = new Intent(DownloadActivity.this, DownloadService.class);
             DownloadService.addArticles(checkedArticles);
             DownloadActivity.this.startService(i);
 
             return null;
         }
 
         @Override
         protected void onPostExecute(Object result) {
             // TODO Auto-generated method stub
             super.onPostExecute(result);
             // progressDialog.dismiss();
             // if (downloadBoolean){
             // Toast.makeText(DownloadActivity.this,getResources().getString(R.string.toast_downloading_success), Toast.LENGTH_SHORT).show();
             // }else{
             // Toast.makeText(DownloadActivity.this,getResources().getString(R.string.toast_downloading_fail), Toast.LENGTH_SHORT).show();
             // }
         }
     }
 
 }
