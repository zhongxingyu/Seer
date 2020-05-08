 package ar.com.nivel7.kernelgesturesbuilder;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.google.analytics.tracking.android.EasyTracker;
 
 
 public class SelectGesture extends Activity  {
 
       List<SelectGestureRowItem> rowItems;
 	  
       @Override
 	  public void onCreate(Bundle savedInstanceState) {
 		  
     	super.onCreate(savedInstanceState);
 	    setContentView(R.layout.selectgesture);
 	    
 	    rowItems = new ArrayList<SelectGestureRowItem>();
         for (int i = 0; i < 30; i++) {
        	SelectGestureRowItem item = new SelectGestureRowItem("Gesture "+i, "");
             rowItems.add(item);
         }
  
         ListView actions_list = (ListView) findViewById(R.id.selectgesture_list);
         final SelectGestureAdapter adapter = new SelectGestureAdapter(this, R.layout.selectgesturerow , rowItems);
         actions_list.setAdapter(adapter);
         actions_list.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> arg0, View v, int position,
                     long id) {
             	
        	    KernelGesturesBuilder.setGesturenumber(position);
             	SelectGesture.this.finish();
             	    
             }
             
         });
         
         
 	  }
       
 	  @Override
 	  public void onStart() {
 	    super.onStart();
 	    EasyTracker.getInstance().activityStart(this); 
 	  }
 
 	  @Override
 	  public void onStop() {
 	    super.onStop();
 	    EasyTracker.getInstance().activityStop(this);
 	  }
 	  
 }
 
 
 class SelectGestureAdapter extends ArrayAdapter<SelectGestureRowItem> {
 	 
     Context context;
  
     public SelectGestureAdapter(Context context, int resourceId,
             List<SelectGestureRowItem> items) {
         super(context, resourceId, items);
         this.context = context;
     }
  
 
     private class ViewHolder {
         TextView txtTitle;
     }
  
     public View getView(int position, View convertView, ViewGroup parent) {
         ViewHolder holder = null;
         SelectGestureRowItem rowItem = getItem(position);
  
         LayoutInflater mInflater = (LayoutInflater) context
                 .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
         if (convertView == null) {
             convertView = mInflater.inflate(R.layout.actionsrow , null);
             holder = new ViewHolder();
             holder.txtTitle = (TextView) convertView.findViewById(R.id.actiontitle);
             convertView.setTag(holder);
         } else
             holder = (ViewHolder) convertView.getTag();
  
        holder.txtTitle.setText(rowItem.getTitle());
  
         return convertView;
     }
 }
 
 class SelectGestureRowItem {
     private String title;
     private String desc;
  
     public SelectGestureRowItem(String title, String desc) {
         this.title = title;
         this.desc = desc;
     }
     public String getTitle() {
         return title;
     }
     public void setTitle(String title) {
         this.title = title;
     }
     public String getDesc() {
         return desc;
     }
     public void setDesc(String desc) {
         this.desc = desc;
     }
     @Override
     public String toString() {
         return title + "\n" + desc;
     }
 }
