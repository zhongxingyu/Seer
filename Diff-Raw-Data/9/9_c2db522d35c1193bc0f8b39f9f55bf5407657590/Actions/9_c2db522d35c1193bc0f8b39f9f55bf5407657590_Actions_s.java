 package ar.com.nivel7.kernelgesturesbuilder;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.google.analytics.tracking.android.EasyTracker;
 
 
 public class Actions extends Activity  {
 
     public static final Integer[] images = {  
     	R.drawable.ic_mdnie,
        R.drawable.ic_power, 
         R.drawable.ic_stweaks, 
     	R.drawable.ic_lovecall,
         R.drawable.ic_camera, 
         R.drawable.ic_bluetooth, 
     	R.drawable.ic_wifi,
         R.drawable.ic_playpause, 
         R.drawable.ic_mute, 
    	R.drawable.ic_home,
        R.drawable.ic_alttab,
         R.drawable.ic_volumeup, 
         R.drawable.ic_volumedown, 
         R.drawable.ic_mediastop, 
         R.drawable.ic_media_next, 
         R.drawable.ic_mediaprevious, 
         R.drawable.ic_recentapps, 
         R.drawable.ic_closeapp,
         R.drawable.ic_question, 
         R.drawable.ic_question, 
         R.drawable.ic_question, 
         R.drawable.ic_question, 
         R.drawable.ic_question, 
         R.drawable.ic_question, 
         R.drawable.ic_question, 
         R.drawable.ic_question, 
         R.drawable.ic_question, 
         R.drawable.ic_question, 
         R.drawable.ic_question, 
         R.drawable.ic_question
         };
     List<ActionsRowItem> rowItems;
 	  
       @Override
 	  public void onCreate(Bundle savedInstanceState) {
 		  
     	Resources res = getResources();
     	String[] titles = res.getStringArray(R.array.action_titles);
     	String[] actions =  res.getStringArray(R.array.actions);
 
     	super.onCreate(savedInstanceState);
 
 	    setContentView(R.layout.actions);
 	    
 	    rowItems = new ArrayList<ActionsRowItem>();
         for (int i = 0; i < titles.length; i++) {
             ActionsRowItem item = new ActionsRowItem(images[i], titles[i], actions[i]);
             rowItems.add(item);
         }
  
         ListView actions_list = (ListView) findViewById(R.id.actions_list);
         final ActionsAdapter adapter = new ActionsAdapter(this, R.layout.actionsrow , rowItems);
         actions_list.setAdapter(adapter);
         actions_list.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> arg0, View v, int position,
                     long id) {
             	
             	ActionsRowItem rowitem=adapter.getItem(position);
         	    String action=rowitem.getDesc();
             	int gesturenumber = KernelGesturesBuilder.getGesturenumber();
             	FileOutputStream fos;
             	
             	String FILENAME = "gesture-"+gesturenumber+".sh";
             		try {
             			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
             			fos.write(action.getBytes());
             			fos.close();
             		} catch (FileNotFoundException e) {
             			e.printStackTrace();
             		} catch (IOException e) {
             			e.printStackTrace();
             		}
             	    
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
             convertView.setTag(holder);
         } else
             holder = (ViewHolder) convertView.getTag();
  
        holder.txtTitle.setText(rowItem.getTitle());
        holder.imageView.setImageResource(rowItem.getImageId());
  
         return convertView;
     }
 }
 
 class ActionsRowItem {
 	private int imageId;
     private String title;
     private String desc;
  
     public ActionsRowItem(int imageId, String title, String desc) {
         this.imageId = imageId;
         this.title = title;
         this.desc = desc;
     }
     public int getImageId() {
         return imageId;
     }
     public void setImageId(int imageId) {
         this.imageId = imageId;
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
