 package jp.i09158knct.simplelauncher2;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.SparseBooleanArray;
 import android.view.View;
 import android.widget.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class NewCategoryActivity extends Activity {
 
     private AppListManager mAppsManager;
     private List<String[]> mAppInfos;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_new_category);
 
         mAppsManager = new AppListManager(this);
         mAppsManager.cacheAllApps();
         mAppInfos = mAppsManager.getCategoryOfAllApps();
         initializeAppList(mAppInfos);
         initializeEventLiteners();
     }
 
     private void initializeEventLiteners() {
         Button add = (Button) findViewById(R.id.new_category_add);
         add.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 NewCategoryActivity.this.onFinishSelection();
             }
         });
     }
 
     private void onFinishSelection() {
         String categoryName = getCategoryName();
         List<String[]> checkedAppList = geCheckedAppList();
         mAppsManager.saveCategory(categoryName, checkedAppList);
         finish();
     }
 
     private String getCategoryName() {
         EditText editText = (EditText) findViewById(R.id.new_category_name);
         return editText.getText().toString();
     }
 
     private List<String[]> geCheckedAppList() {
         ListView list = (ListView) findViewById(R.id.new_category_app_list);
         SparseBooleanArray checkingList = list.getCheckedItemPositions();
         List<String[]> checkedAppList = new ArrayList<String[]>(64);
 
         for (int i = 0; i < checkingList.size(); i++) {
            if (checkingList.get(i)) {
                checkedAppList.add(mAppInfos.get(i));
             }
         }
         return checkedAppList;
     }
 
     private void initializeAppList(final List<String[]> appInfos) {
         ListView list = (ListView) findViewById(R.id.new_category_app_list);
         ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_checked);
         for (String[] appInfo : appInfos) {
             String appname = appInfo[2];
             adapter.add(appname);
         }
         list.setAdapter(adapter);
     }
 }
