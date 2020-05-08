 package kits.vdroid;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ListIterator;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class vdr_recordings extends Activity {
 	
 	private String host;
 	private RecordAdapter recadp;
 	private ListView rec_list;
 	private LoadThread ldThread;
 	
 	private class RecordInfo {
 		String name;
 		String timeline;
 	}
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
       //Create UI
         setContentView(R.layout.recordings);
         
         recadp = new RecordAdapter();
         
         rec_list = (ListView) findViewById(R.id.vdr_rec_list);
         host = getIntent().getData().getHost();
         
         ldThread = new LoadThread(loadHandler);
         ldThread.start();
         
         rec_list.setAdapter(recadp);
         
         //Klick auf Aufnahme startet wiedergabe
         rec_list.setOnItemClickListener(new OnItemClickListener()
 		{
 		    public void onItemClick(AdapterView<?> parent, android.view.View view,int position, long id)
 		    {
 		    	Toast.makeText(vdr_recordings.this,"Starte Wiedergabe...", Toast.LENGTH_LONG).show();
 		    	SVDRP vdr = new SVDRP(host,2001);
 		    	vdr.getData("PLAY "+ (position + 1) + " begin");
 		    	finish();
 		    }
 		});
 	}
 	
 	////////////////////////
 	//ListView Adapter START
 	
     private class RecordAdapter extends BaseAdapter
     {
     	private LayoutInflater mInflater;
     	private List<RecordInfo> recdata;
     	 
         public RecordAdapter() {
             mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             recdata = new ArrayList<RecordInfo>();
         }
     	
 		public int getCount() {
 			return recdata.size();
 		}
 
 		public Object getItem(int position) {
 			recdata.get(position);
 			return null;
 		}
 
 		public long getItemId(int position) {
 			return position;
 		}
 
 		public void addRecording(RecordInfo input)
 		{
 			recdata.add(input);
 			this.notifyDataSetChanged();
 		}
 		
 		public View getView(int position, View convertView, ViewGroup parent) {
 					
 			if (convertView == null)
 				convertView = mInflater.inflate(R.layout.recordings_li, parent, false);
 			
 			((TextView) convertView.findViewById(R.id.rec_li_name)).setText(recdata.get(position).name);
 			((TextView) convertView.findViewById(R.id.rec_li_time)).setText(recdata.get(position).timeline);
 			return convertView;
 		}
     }
     //ListView Adapter END
     //////////////////////
     
     final Handler loadHandler = new Handler() {
         public void handleMessage(Message msg) {
         	RecordInfo ri = new RecordInfo();
         	
         	if(msg.getData().getBoolean("error"))
         	{
         		Toast.makeText(vdr_recordings.this,"VDR-Interface blockiert!", Toast.LENGTH_LONG).show();
         		finish();
         	}
         	
         	if(msg.getData().getBoolean("hasrec"))
         	{
 	        	ri.name =	msg.getData().getString("name");
 	        	ri.timeline = msg.getData().getString("time");
 	        	recadp.addRecording(ri);
         	}
         	else
         	{
         		Toast.makeText(vdr_recordings.this,"Keine Aufnahmen vorhanden!", Toast.LENGTH_LONG).show();
         		finish();
         	}
         }
     };
     
     private class LoadThread extends Thread
 	{
 		Handler mHandler;
         
         LoadThread(Handler h) {
             mHandler = h;
         }
                 
         public void run() {
         	try {
     			Thread.sleep(1000);
     		} catch (InterruptedException e) {
     			e.printStackTrace();
     		}
 
     		Log.d("RECTHREAD", "Requesting Recordings");
     		
     		
             //Read Channels
             SVDRP vdr = new SVDRP(host,2001);
             
             List<String> records = vdr.getListData("LSTR");
             
             if(records == null)
             {
             	Message msg = mHandler.obtainMessage();
     		    Bundle b = new Bundle();
             	b.putBoolean("error", true);
    		     	
    		     	msg.setData(b);
    		     	mHandler.sendMessage(msg);	
    		     	vdr.close();
    		     	return;
             }
             
             ListIterator<String> rec_it = records.listIterator();
 
             int i = 1;
             
             while(rec_it.hasNext())
     		{
     			String line = rec_it.next();
     			String cnr = null;
     			String cname = null;
     			String ctime = null;
     			
     			Message msg = mHandler.obtainMessage();
     		    Bundle b = new Bundle();
     			
     			if(line.startsWith("250-"))
     			{
     				cnr = String.valueOf(i);
 	    			cname = line.split(" ",4)[3];
 	    			ctime = line.split(" ")[1] + " - " + line.split(" ")[2];
 	    			b.putBoolean("hasrec", true);
 	    			b.putString("name", cname);
 	   		     	b.putString("num", cnr);
 	   		     	b.putString("time", ctime);
 	    			
     			}
     			else if(line.startsWith("250 "))
     			{
     				cnr = String.valueOf(i);
 	    			cname = line.split(" ",5)[4];
 	    			ctime = line.split(" ")[2] + " - " + line.split(" ")[3];
 	    			b.putBoolean("hasrec", true);
 	    			b.putString("name", cname);
 	   		     	b.putString("num", cnr);
 	   		     	b.putString("time", ctime);
     			}
     			else if(line.startsWith("550"))
     			{
     				Log.d("RECTHREAD", "No Recordings");
     				b.putBoolean("hasrec", false);
     			}
    		     	msg.setData(b);
    		     	mHandler.sendMessage(msg);	
    		     	i++;
     		}
                         
     		vdr.close();
         }
 	}
     
     
 }
