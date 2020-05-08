 package org.nosreme.app.urlhelper;
 
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Intent;
import android.content.Context;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 
 import org.nosreme.app.urlhelper.UrlStore;
 
 public class URLHelperActivity extends ListActivity {
 	private final String[] colFields = { "url", "seen" };
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         UrlStore urlstore = new UrlStore(getApplicationContext());
 
         Intent intent = getIntent();
         if (intent.getAction().equals(android.content.Intent.ACTION_VIEW))
         {
         	urlstore.addUrl(intent.getDataString());
         	/*
         	urlstore.addUrl(intent.toString());
         	PackageManager pm = getPackageManager();
         	intent.setComponent(null);
         	List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
         	for (ResolveInfo ri: activities)
         	{
         		ActivityInfo ai = ri.activityInfo;
         		urlstore.addUrl(ai.packageName);
         		urlstore.addUrl(ai.toString());
         	}
         	*/
         }
         
         Cursor urls = urlstore.getUrlCursor();
         
         //setContentView(R.layout.main);
         int[] to = { R.id.tv1 };
         
         setListAdapter(new SimpleCursorAdapter(getApplicationContext(),
         									   R.layout.main, urls, 
         									   colFields,to));
         
         
         ListView lv = getListView();
         /* Thanks to tranbinh.bino@gmail.com in the thread at:
          * http://groups.google.com/group/android-developers/browse_thread/thread/14ba131c3ebc49eb
          * for this snippet. 
          */
         lv.setOnItemLongClickListener( new AdapterView.OnItemLongClickListener 
         		(){ 
         		                //@Override 
         		                
         		                public boolean onItemLongClick(AdapterView<?> av, View v, int	pos, long id) { 
         		                        onLongListItemClick(v,pos,id); 
         		                        return true; 
         		        } 
         		}); 
 
     }
     
     private void onLongListItemClick(View v, int pos, long id)
     {
    	AlertDialog dlg = new AlertDialog.Builder(this).create();
     	
     	dlg.setMessage("List item long click" + Long.toString(id));
     	dlg.show();
     }
     
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id)
     {
  
     	PackageManager pm = getPackageManager();
     	Intent intent = new Intent();
     	intent.setComponent(null);
         UrlStore urlstore = new UrlStore(getApplicationContext());
     	
     	Uri uri = Uri.parse(urlstore.getUrl(id));
     	
     	
     	intent.setData(uri);
     	intent.setAction(android.content.Intent.ACTION_VIEW);
     	List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
 //    	String msg = "Possible handlers:\n";
 //    	String pkg = null;
     	for (ResolveInfo ri: activities)
     	{
     		ActivityInfo ai = ri.activityInfo;
 //    		msg = msg + ai.packageName + "/" + ai.applicationInfo.className + "\n";
 //    		msg = msg + ai.name + "\n";
     		if (!ai.name.startsWith("org.nosreme.app.urlhelper"))
     		{
     	    	intent.setClassName(ai.packageName, ai.name);
     			break;
     		}
     	
 //    		urlstore.addUrl(ai.packageName);
     	}
 
     	startActivity(intent);
 //    	AlertDialog dlg = new AlertDialog.Builder(this).create();
     	
 //    	dlg.setMessage(msg);
 //    	dlg.show();
     }
 }
