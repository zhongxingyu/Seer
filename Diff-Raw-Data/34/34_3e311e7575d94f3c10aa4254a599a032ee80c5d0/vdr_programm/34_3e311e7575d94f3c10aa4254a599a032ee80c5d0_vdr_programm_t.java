 package kits.vdroid;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.ListIterator;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
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
 

 public class vdr_programm extends Activity {
 
 	SVDRP vdr;
 	String host;
 	private ProgrammAdapter progadp;
 	private LoadThread ldThread;
 	private String channr;
 	
 	private class ProgInfo {
 		String title;
 		String time_start;
 		String time_end;
 		String date;
 		int timestamp;
 	}
 	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.programm);
         
         Uri uridata = getIntent().getData();
         host = uridata.getHost();
         channr = uridata.getQueryParameter("chan");
                 
         //Kanalname raussuchen
         vdr = new SVDRP(host,2001);
         String chanline = vdr.getData("LSTC " + channr);
         
         if(chanline == null)
         {
         	Toast.makeText(vdr_programm.this,"VDR-Interface blockiert!", Toast.LENGTH_LONG).show();
         	finish();
         }
         
         progadp = new ProgrammAdapter();
         
         String channame = chanline.split(" ",3)[2].split(";",2)[0];
         TextView header = ((TextView) findViewById(R.id.vdr_prog_head));
         header.setText("Programm - " + channame);
 
         ListView proglist = (ListView) findViewById(R.id.vdr_prog_list);
         proglist.setAdapter(progadp);
         vdr.close();
         
       //Klick auf Kanal wechselt zum Kanal
         proglist.setOnItemClickListener(new OnItemClickListener()
 		{
 		    public void onItemClick(AdapterView<?> parent, android.view.View view,int position, long id)
 		    {
 		    	stopFetchThread();
 
 		    	Intent showInfos = new Intent(Intent.ACTION_VIEW);
 		    	String timestamp = String.valueOf(progadp.getTimestamp(position));
 		        Uri infoUri = Uri.parse("vdr://" + host + "/info?time="+ timestamp + "&chan=" + channr);
 		        Log.d("VDRPROG", "Request info for:" + infoUri.toString());
 		        showInfos.setData(infoUri);
 		        showInfos.setClass(vdr_programm.this,kits.vdroid.vdr_info.class);
 		        startActivity(showInfos);
 		    	
 		    	finish();
 		    }
 		});
         
         
         ldThread = new LoadThread(loadHandler);
         ldThread.start();
 	}
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		vdr.close();
         //stopFetchThread();
 	}
 	
 	//Stop Thread
 	private void stopFetchThread()
 	{
 		ldThread.stopFetching();
 		try {
 			ldThread.join();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	//ListView Adapter
     private class ProgrammAdapter extends BaseAdapter
     {
     	private LayoutInflater mInflater;
     	private List<ProgInfo> progdata;
     	    	 
         public ProgrammAdapter() {
             mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             progdata = new ArrayList<ProgInfo>();
         }
     	
 		public int getCount() {
 			return progdata.size();
 		}
 
 		public int getTimestamp(int pos)
 		{
 			return progdata.get(pos).timestamp;
 		}
 		
 		public Object getItem(int position) {
 			progdata.get(position);
 			return null;
 		}
 
 		public void clear()
 		{
 			progdata.clear();
 			this.notifyDataSetInvalidated();
 		}
 		
 		public long getItemId(int position) {
 			return position;
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 					
 			if (convertView == null)
 				convertView = mInflater.inflate(R.layout.programm_li, parent, false);
 			
 			String timeline = progdata.get(position).time_start + " - " + progdata.get(position).time_end; 
 			
 			((TextView) convertView.findViewById(R.id.prog_li_title)).setText(progdata.get(position).title);
 			((TextView) convertView.findViewById(R.id.prog_li_time)).setText(timeline);
 			((TextView) convertView.findViewById(R.id.prog_li_date)).setText(progdata.get(position).date);
 			return convertView;
 		}
 		
 		public void addProg(ProgInfo input)
 		{
 			progdata.add(input);
 			this.notifyDataSetChanged();
 		}
     }
 
     //Handler
     final Handler loadHandler = new Handler() {
         public void handleMessage(Message msg) {
         	ProgInfo pi = new ProgInfo();
         	
         	if(msg.getData().getBoolean("error"))
         	{
         		//chanload_prog.dismiss();
         		Toast.makeText(vdr_programm.this,"VDR-Interface blockiert!", Toast.LENGTH_LONG).show();
         		finish();
         	}
         	
         	pi.title =	msg.getData().getString("title");
         	pi.time_end = msg.getData().getString("time_end");
         	pi.time_start = msg.getData().getString("time_start");
         	pi.date = msg.getData().getString("date");
         	pi.timestamp = msg.getData().getInt("stamp");
         	progadp.addProg(pi);
         	//chanload_prog.dismiss();
             
         }
     };
     
     //Fetch Thread
     
     //Thread
     private class LoadThread extends Thread
 	{
 		Handler mHandler;
         int state;
 		
         LoadThread(Handler h) {
             mHandler = h;
         }
           
         public void stopFetching()
         {
         	state = 1;
         }
         
         public void run() {
         	state = 0;
  
         	
         	Log.d("PROGTHREAD", "Requesting Programm");
             //Read Channels
             SVDRP vdr = new SVDRP(host,2001);
             
             List<String> progdata = vdr.getListData("LSTE " + channr);
                 		
             if(progdata == null)
             {
             	Message msg = mHandler.obtainMessage();
    		     	Bundle b = new Bundle();
    		     	b.putBoolean("error", true);
    		     	
    		     	msg.setData(b);
    		     	mHandler.sendMessage(msg);	
    		     	vdr.close();
    		     	return;
             }
             
             String title = "";
             String time_start = "";
             String time_end = "";
             String date = "";
             int stamp = 0;
             
             ListIterator<String> prog_it = progdata.listIterator();
             while(prog_it.hasNext() && state == 0)
     		{
             	String line = prog_it.next();
     			Log.d("VDRPROG",line);
     			if(line.startsWith("250 "))
     				break;
     			
     			//EPG Eintrag Ende gesammelte Daten an UI puschen
     			if(line.startsWith("215-e"))
     			{
     				Log.d("VDRPROG", "EPG-Entry complete - " + title);
     				Message msg = mHandler.obtainMessage();
        		     	Bundle b = new Bundle();
        		     	b.putString("title", title);
        		     	b.putString("date", date);
        		     	b.putString("time_start", time_start);
        		     	b.putString("time_end", time_end);
        		     	b.putInt("stamp", stamp);
        		     	msg.setData(b);
     		     	mHandler.sendMessage(msg);
     		     	title = "";
     		     	time_start = "";
     		     	time_end = "";
     		     	date = "";
     		     	
     			}
     			else if(line.startsWith("215-T"))
     			{
     				title = line.split(" ",2)[1];
     			}
     			else if(line.startsWith("215-E"))
     			{
     				stamp = Integer.parseInt(line.split(" ")[2]);
     				long time = Long.parseLong(line.split(" ")[2]);
     				long dur = Long.parseLong(line.split(" ")[3]);
     				long time_start_l = time * 1000;
     				long time_end_l = (time + dur) * 1000;
     				Date start = new Date(time_start_l);
     	        	Date end = new Date(time_end_l);
     	        	
     	        	String start_h = String.valueOf(start.getHours());
     	        	String start_m = String.valueOf(start.getMinutes());
     	        	String end_h = String.valueOf(end.getHours());
     	        	String end_m = String.valueOf(end.getMinutes());
     	        	
 
     	        	if(start_h.length() == 1)
     	        		start_h = "0" + start_h;
     	        	if(start_m.length() == 1)
     	        		start_m = "0" + start_m;
     	        	if(end_h.length() == 1)
     	        		end_h = "0" + end_h;
     	        	if(end_m.length() == 1)
     	        		end_m = "0" + end_m;
     	        	
     	        	time_start = start_h + ":" + start_m;
     	        	time_end = end_h + ":" + end_m;
     	        	
     	        	//Datum
     	        	String date_d = String.valueOf(start.getDate());
     	        	String date_m = String.valueOf(start.getMonth() + 1);
     	        	String date_y = String.valueOf(start.getYear() + 1900);
     	        	
     	        	if(date_d.length() == 1)
     	        		date_d = "0" + date_d;
     	        	if(date_m.length() == 1)
     	        		date_m = "0" + date_m;
     	        	
     	        	date = date_d + "." + date_m + "." + date_y;
     	        	
     			}
 	
     		}
     		vdr.close();
         }
 	}
     
     
 }
