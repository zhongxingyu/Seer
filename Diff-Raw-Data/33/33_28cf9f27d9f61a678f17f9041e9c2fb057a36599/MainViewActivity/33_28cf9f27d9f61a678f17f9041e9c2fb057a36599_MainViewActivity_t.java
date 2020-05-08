 package com.qrsphere;
 
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.Gravity;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.SimpleAdapter;
 import android.widget.Toast;
 
 public class MainViewActivity extends Activity{
 
     private int[] mImages={  
             R.drawable.scan_icon,  
             R.drawable.history_icon,  
             R.drawable.fav_icon,  
             R.drawable.gear_icon, 
             

 
   
     }; 
     
     private String[] mTexts ={
     		"Scan",
     		"History",
     		"Favorite",
     		"More",
     		

     		
     };
     GridView mGridView;
     Button bt_ad;
     Context context;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);  
         setContentView(R.layout.mainview); 
       
         
        
         Init();
 	}
 	
 	private void Init(){
 		context = this;
 		
 		mGridView=(GridView) findViewById(R.id.gridview);  
         //  
         ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();  
           
         for (int i = 0; i < mImages.length; i++) {  
             HashMap<String, Object> map = new HashMap<String, Object>();  
             map.put("ItemImage", mImages[i]);// 
             map.put("ItemText", mTexts[i]);// 
             lstImageItem.add(map);  
         }  
         //
         SimpleAdapter simple = new SimpleAdapter(this, lstImageItem,  
                 R.layout.gridviewitem,  
                 new String[] { "ItemImage", "ItemText" }, new int[] {  
                         R.id.ItemImage, R.id.ItemText });  
         mGridView.setAdapter(simple);  
         //  
         mGridView.setOnItemClickListener(new GridView.OnItemClickListener(){  
   
             @Override  
             public void onItemClick(AdapterView<?> parent, View view,  
                     int position, long id) {  
                 Toast toast=Toast.makeText(getApplicationContext(),
                 		"you chose"+(position+1)+"#picture", Toast.LENGTH_SHORT);  
                 toast.setGravity(Gravity.BOTTOM, 0, 0);  
                 toast.show();  
                 
                 if (position == 1){
                 	 startActivity(new Intent("com.example.clienttest.HistoryActivity"));
                 }
             }  
               
         });  
         
         bt_ad = (Button) findViewById(R.id.btn_ad_bar);
         bt_ad.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 	    		StartBrowser sb = new StartBrowser("www.qrsphere.com",context);
 	    		sb.startBrowse();
 			}
 		});
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		// TODO Auto-generated method stub
 		super.onCreateContextMenu(menu, v, menuInfo);
 	}
 	
 
 
 }
