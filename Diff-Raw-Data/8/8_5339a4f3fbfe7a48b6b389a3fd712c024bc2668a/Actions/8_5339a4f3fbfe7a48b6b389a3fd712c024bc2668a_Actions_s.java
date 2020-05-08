 package ar.com.nivel7.kernelgesturesbuilder;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.analytics.tracking.android.EasyTracker;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Toast;
 public class Actions extends Activity  {
 
 	public static final String[] titles = new String[] { "Strawberry",
         "Banana", "Orange", "Mixed" };
 
 	public static final String[] descriptions = new String[] {
         "It is an aggregate accessory fruit",
         "It is the largest herbaceous flowering plant", "Citrus Fruit",
         "Mixed Fruits" };
 	
     public static final Integer[] images = {  R.drawable.ic_launcher,
         R.drawable.ic_launcher, R.drawable.ic_launcher, R.drawable.ic_launcher  };
         
 
     List<ActionsRowItem> rowItems;
 	  
       @Override
 	  public void onCreate(Bundle savedInstanceState) {
 		  
 	    super.onCreate(savedInstanceState);
 
 	    setContentView(R.layout.actions);
 	    
 	    rowItems = new ArrayList<ActionsRowItem>();
         for (int i = 0; i < titles.length; i++) {
             ActionsRowItem item = new ActionsRowItem(images[i], titles[i], descriptions[i]);
             rowItems.add(item);
         }
  
         ListView actions_list = (ListView) findViewById(R.id.actions_list);
         ActionsAdapter adapter = new ActionsAdapter(this, R.layout.actionsrow , rowItems);
         actions_list.setAdapter(adapter);
         actions_list.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> arg0, View v, int position,
                     long id) {
            	/*
             	Toast toast = Toast.makeText(getApplicationContext(),
                         "Item " + (position + 1) + ": " + rowItems.get(position),
                         Toast.LENGTH_SHORT);
                     toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
                     toast.show();
                    */
                    
                    Actions.this.finish();
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
 
 
 class ActionsAdapter extends ArrayAdapter<ActionsRowItem> {
 	 
     Context context;
  
     public ActionsAdapter(Context context, int resourceId,
             List<ActionsRowItem> items) {
         super(context, resourceId, items);
         this.context = context;
     }
  
 
     private class ViewHolder {
         ImageView imageView;
         TextView txtTitle;
         TextView txtDesc;
     }
  
     public View getView(int position, View convertView, ViewGroup parent) {
         ViewHolder holder = null;
         ActionsRowItem rowItem = getItem(position);
  
         LayoutInflater mInflater = (LayoutInflater) context
                 .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
         if (convertView == null) {
             convertView = mInflater.inflate(R.layout.actionsrow , null);
             holder = new ViewHolder();
             holder.txtTitle = (TextView) convertView.findViewById(R.id.actiontitle);
             holder.imageView = (ImageView) convertView.findViewById(R.id.actionicon);
             holder.txtDesc = (TextView) convertView.findViewById(R.id.actiondesc);
             convertView.setTag(holder);
         } else
             holder = (ViewHolder) convertView.getTag();
  
        holder.txtDesc.setText(rowItem.getDesc());
        holder.txtTitle.setText(rowItem.getTitle());
        holder.imageView.setImageResource(rowItem.getImageId());
  
         return convertView;
     }
 }
