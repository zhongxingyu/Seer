 package com.caa.ui;
 
 import java.util.ArrayList;
 
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 import com.caa.bspace.BSpaceFilesResource;
 import com.caa.bspace.BSpaceFilesResource.*;
 import com.caa.bspace.R;
 
 public class FilesViewActivity extends ListActivity
 {
 	ArrayAdapter<BSpaceFilesItem> adapter;
 	ArrayList<BSpaceFilesItem> listItems=new ArrayList<BSpaceFilesItem>();
 	
 	public void onCreate(Bundle savedInstanceState) {
 		  super.onCreate(savedInstanceState);
 		  
 		  BSpaceFilesResource filesResource = new BSpaceFilesResource(BSpaceMobileActivity.user, ClassViewActivity.bspaceClass);
 		  
 		  for(BSpaceFilesItem subdirectory : filesResource.rootDirectory.subdirectories){
 			  listItems.add(subdirectory);
 		  }
 		  for(BSpaceFilesItem file: filesResource.rootDirectory.files){
			  listItems.add(file);
 		  }
 		  
 		  adapter= new ArrayAdapter<BSpaceFilesItem>(this, R.layout.list_item, listItems);
 		  setListAdapter(adapter);
 
 		  ListView lv = getListView();
 		  lv.setTextFilterEnabled(true);
 		  
 		  lv.setOnItemClickListener(new OnItemClickListener() {
 			    public void onItemClick(AdapterView<?> parent, View view,
 			        int position, long id) {
 			    	BSpaceFilesItem selectedItem = listItems.get(position);
 			    	if (selectedItem instanceof BSpaceDirectory){
 			    		Log.d("filesview", "Pretend I'm doing something with dir " + selectedItem);
 			    	} else {
 			    		Log.d("filesview", "What do I do with this file? " + selectedItem);
 			    	}
 			    	 adapter.notifyDataSetChanged();
 			    }
 			  });
 	}
 }
