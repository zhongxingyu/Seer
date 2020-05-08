 package com.tools.tvguide.activities;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import com.tools.tvguide.R;
 import com.tools.tvguide.components.MyProgressDialog;
 import com.tools.tvguide.data.Category;
 import com.tools.tvguide.managers.AppEngine;
 import com.tools.tvguide.managers.AdManager.AdSize;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Handler.Callback;
 import android.os.Message;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class CategorylistActivity extends Activity implements Callback 
 {
     private static final String TAG = "CategorylistActivity";
     private ListView mCategoryListView;
     private SimpleAdapter mListViewAdapter;
     private List<HashMap<String, Object>> mItemList;
     private TextView mTitleTextView;
     private Category mCurrentCategory;
     private MyProgressDialog mProgressDialog;
     private Handler mUiHandler;
     private enum SelfMessage { Show_Category };
     
     @Override
     protected void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_categorylist);
         mCategoryListView = (ListView)findViewById(R.id.categorylist_listview);
         mTitleTextView = (TextView)findViewById(R.id.categorylist_text_title);
         mProgressDialog = new MyProgressDialog(this);
         mItemList = new ArrayList<HashMap<String, Object>>();
         mListViewAdapter = new SimpleAdapter(CategorylistActivity.this, mItemList, R.layout.home_list_item,
                 new String[]{"name"}, new int[]{R.id.home_item_text});
         mCategoryListView.setAdapter(mListViewAdapter);
         mUiHandler = new Handler(this);
 
         mCurrentCategory = (Category) getIntent().getSerializableExtra("category");
         if (mCategoryListView == null)
             return;
         
         mTitleTextView.setText(mCurrentCategory.name);
         update();
         
         mCategoryListView.setOnItemClickListener(new OnItemClickListener() 
         {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
             {
                 Intent intent = new Intent(CategorylistActivity.this, ChannellistActivity.class);
                intent.putExtra("category", (Serializable) mItemList.get(position).get("category"));
                 startActivity(intent);
             }
         });
         
         AppEngine.getInstance().getAdManager().addAdView(CategorylistActivity.this, R.id.adLayout, AdSize.NORMAL_SIZE);
     }
 
     public void back(View view)
     {
         if (view instanceof Button)
         {
             // The same effect with press back key
             finish();
         }
     }
         
     private void update()
     {
         mUiHandler.obtainMessage(SelfMessage.Show_Category.ordinal()).sendToTarget();
     }
     
     private boolean shouldBeFirst(String categoryName)
     {
         String userLocaion = AppEngine.getInstance().getDnsManager().getDeviceLocation();
         if (categoryName == null)
             return false;
         
         if (userLocaion.contains(categoryName))
             return true;
         
         return false;
     }
 
     @Override
     public boolean handleMessage(Message msg) 
     {
         SelfMessage selfMsg = SelfMessage.values()[msg.what];
         switch (selfMsg)
         {
             case Show_Category:
                 mProgressDialog.dismiss();
                 mItemList.clear();
                 for (int i=0; i<mCurrentCategory.categoryList.size(); ++i)
                 {
                     HashMap<String, Object> item = new HashMap<String, Object>();
                     item.put("name", mCurrentCategory.categoryList.get(i).name);
                    item.put("category", mCurrentCategory.categoryList.get(i));
                     if (shouldBeFirst(mCurrentCategory.categoryList.get(i).name))
                         mItemList.add(0, item);
                     else
                         mItemList.add(item);
                 }
                 mListViewAdapter.notifyDataSetChanged();
                 break;
         }
         return true;
     }
 }
