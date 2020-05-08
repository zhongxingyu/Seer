 package com.kshark.listview.multiselect;
 
 
 
 
 
 
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 
 public class MultiselectExampleActivity extends ListActivity {
 	
 	  private static final String CHECK_STATES = "mylist:check_states";
 	  private boolean[] mCheckStates=null;
 	  private static String[] Content=null;
	  private CustomAdapter mAdapter = new CustomAdapter();
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         Content = new String[28]; 
         for(int i =0 ;i<=27 ; i++){
         	Content[i]="List Text " + i ;
         }
         
         
         if (savedInstanceState != null) {
             mCheckStates = savedInstanceState.getBooleanArray(CHECK_STATES);
         } else {
             mCheckStates = new boolean[Content.length];
         }
         
       
         setListAdapter(mAdapter);
     }
     
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putBooleanArray(CHECK_STATES, mCheckStates);
     }
 
    
     
     public void onClick(View view){
 		switch (view.getId()) {
 		case R.id.selectAllButton:
 			for(int i = 0 ; i<Content.length ; i++){
 				mCheckStates[i]=true;
 			}
 	        mAdapter.notifyDataSetChanged();  /*This is a hacky way to refresh listview but i have realised it 
 	                                      is a better option  as this works in all versions of android upto jellybean */
 			
 			break;
 		case R.id.unselectButton:
 			for(int i = 0 ; i<Content.length ; i++){
 				mCheckStates[i]=false;
 			}
 	        mAdapter.notifyDataSetChanged();
 
 			break;
 
 		case R.id.showSelectedButton:
 			int len = Content.length;
 	    	String check="";
 	    	for(int i=0;i<len;i++){
 	    		if(mCheckStates[i]==true)
 	    		check = check +"\n"+ Content[i];
 	    	}
 	        Toast.makeText(MultiselectExampleActivity.this,check, Toast.LENGTH_SHORT).show();
 			break;
 		}
 	}
     
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
     	mCheckStates[position]= !mCheckStates[position];
 
     }
 
     
     
     private static class ViewHolder {
         public CheckBox select=null;
         public TextView label=null;
         public ImageView icon=null;
     }
 
    
     private class CustomAdapter extends ArrayAdapter<String>{
 
     	private CustomAdapter() {
 			super(MultiselectExampleActivity.this, R.layout.listitem, Content);
 		}
     	
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
 
             ViewHolder holder;
 
             if (convertView == null) {
                 convertView = getLayoutInflater().inflate(R.layout.listitem, null);
 
                 holder = new ViewHolder();
                 holder.select= (CheckBox) convertView.findViewById(R.id.select_icon);
                 holder.label = (TextView) convertView.findViewById(R.id.label);
                 holder.icon =(ImageView)convertView.findViewById(R.id.icon);
                 
 
 
                 convertView.setTag(holder);
             } else {
                 holder = (ViewHolder) convertView.getTag();
             }
 
             /*
              * The Android API provides the OnCheckedChangeListener interface
              * and its onCheckedChanged(CompoundButton buttonView, boolean
              * isChecked) method. Unfortunately, this implementation suffers
              * from a big problem: you can't determine whether the checking
              * state changed from code or because of a user action. As a result
              * the only way we have is to prevent the CheckBox from callbacking
              * our listener by temporary removing the listener.
              */
             holder.select.setOnCheckedChangeListener(null);
             holder.select.setChecked(mCheckStates[position]);
             holder.select.setOnCheckedChangeListener(mCheckBoxChangeListener);
 
             holder.label.setText(Content[position]);
             holder.icon.setImageResource(R.drawable.ic_launcher);
             return convertView;
         }
 
 		
     }
     
     private OnCheckedChangeListener mCheckBoxChangeListener = new OnCheckedChangeListener() {
         @Override
         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
             final int position = getListView().getPositionForView(buttonView);
             if (position != ListView.INVALID_POSITION) {
                 mCheckStates[position] = isChecked;
             }
         }
     };
 
     
     
 }
