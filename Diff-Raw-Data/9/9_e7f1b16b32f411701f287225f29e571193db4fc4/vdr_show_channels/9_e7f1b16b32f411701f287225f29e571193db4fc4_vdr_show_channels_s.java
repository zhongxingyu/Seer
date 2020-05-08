 package kits.vdroid;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.ListIterator;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
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
 
 public class vdr_show_channels extends Activity {
 	
 	private ListView chan_list;
 	private String host;
 	private ProgressDialog chanload_prog;
 	private LoadThread ldThread;
 	private ChannelsAdapter chanadp;
 		
 	private class ChannelInfo {
 		String name;
 		String now;
 		String timeline;
 		String cnr;
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         chanload_prog = ProgressDialog.show( vdr_show_channels.this, "" , getResources().getText(R.string.show_chans_load), false);
         
         setContentView(R.layout.channels);
         
         chanadp = new ChannelsAdapter();
         
         chan_list = (ListView) findViewById(R.id.vdr_channel_list);
         host = getIntent().getData().getHost();
         
         
         //Klick auf Kanal wechselt zum Kanal
         chan_list.setOnItemClickListener(new OnItemClickListener()
 		{
 		    public void onItemClick(AdapterView<?> parent, android.view.View view,final int position, long id)
 		    {
 		    	
 		    	final CharSequence[] items = {getResources().getText(R.string.switchc), getResources().getText(R.string.info), getResources().getText(R.string.program)};

 		    	AlertDialog.Builder builder = new AlertDialog.Builder(vdr_show_channels.this);
 		    	builder.setTitle(chanadp.getChanName(position));
 		    	builder.setItems(items, new DialogInterface.OnClickListener() {
 		    	    public void onClick(DialogInterface dialog, int item) {
 		    	        selectMenuItem(item,position);
 		    	    }
 		    	});
 		    	AlertDialog alert = builder.create();
 		    	alert.show();
 		    }
 		});
         
         chan_list.setAdapter(chanadp);
 
 	}
 
 	public void selectMenuItem(int item, int position)
 	{
 		
 		String cnr = chanadp.getChanNr(position);
 	    switch(item) {
 	    case 0:
 	    	SVDRP vdr = new SVDRP(host,vdr_show_channels.this);
 	    	int channum = position + 1;
 	    	vdr.getData("CHAN "+ channum);
 	    	vdr.close();
 	    	finish();
 	        return;
 	    case 1:
 	    	stopFetchThread();
 	    	Intent showInfos = new Intent(Intent.ACTION_VIEW);
 	        Uri infoUri = Uri.parse("vdr://" + host + "/info?time=now&chan=" + cnr);
 	        showInfos.setData(infoUri);
 	        showInfos.setClass(this,kits.vdroid.vdr_info.class);
 	        startActivity(showInfos);
 	        return;
 	    case 2:
 	    	stopFetchThread();
 	    	Intent showProg = new Intent(Intent.ACTION_VIEW);
 	        Uri progUri = Uri.parse("vdr://" + host + "/prog?chan=" + cnr);
 	        showProg.setData(progUri);
 	        showProg.setClass(this,kits.vdroid.vdr_programm.class);
 	        startActivity(showProg);
 	        return;
 	    }
 	}
 	
 	@Override
 	public void onResume() {
         super.onResume();
         Log.d("VDRCHANLIST", "Resumed");
         ldThread = new LoadThread(loadHandler);
         chanadp.clear();
         ldThread.start();
 	}
 	
 	@Override
 	public void onStop() {
         super.onStop();
         Log.d("VDRCHANLIST", "Stopped");
         ldThread.stopFetching();
 	}
 	
 	private void stopFetchThread()
 	{
 		ldThread.stopFetching();
     	try {
 			ldThread.join();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 
 	//Hanlder nimmt geladene Kanäle vom Thread auf und läd in ListView
 	final Handler loadHandler = new Handler() {
         public void handleMessage(Message msg) {
         	ChannelInfo ci = new ChannelInfo();
         	
         	if(msg.getData().getBoolean("error"))
         	{
         		chanload_prog.dismiss();
         		Toast.makeText(vdr_show_channels.this,getResources().getText(R.string.toast_blocked) , Toast.LENGTH_LONG).show();
         		finish();
         	}
         	
         	ci.name =	msg.getData().getString("name");
         	ci.now = msg.getData().getString("now");
         	ci.cnr = msg.getData().getString("cnr");
         	
         	long starttime = msg.getData().getInt("time");
         	long dur = msg.getData().getInt("dur");
         	long endtime = starttime + dur;
         	Date start = new Date(starttime*1000);
         	Date end = new Date(endtime*1000);
         	
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
         	        	
         	String start_str = start_h + ":" + start_m;
         	String end_str = end_h + ":" + end_m;
         	
         	ci.timeline = start_str + " - " + end_str;
         	        	        	
         	chanadp.addChannel(ci);
         	chanload_prog.dismiss();
             
         }
     };
     
     //ListView Adapter
     private class ChannelsAdapter extends BaseAdapter
     {
     	private LayoutInflater mInflater;
     	private List<ChannelInfo> chandata;
     	 
         public ChannelsAdapter() {
             mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             chandata = new ArrayList<ChannelInfo>();
         }
     	
 		public int getCount() {
 			return chandata.size();
 		}
 
 		public String getChanNr(int position) {
 			return chandata.get(position).cnr;
 		}
 		
 		public String getChanName(int position) {
 			return chandata.get(position).name;
 		}
 		
 		public Object getItem(int position) {
 			chandata.get(position);
 			return null;
 		}
 
 		public void clear()
 		{
 			chandata.clear();
 			this.notifyDataSetInvalidated();
 		}
 		
 		public long getItemId(int position) {
 			return position;
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 					
 			if (convertView == null)
 				convertView = mInflater.inflate(R.layout.channels_li, parent, false);
 			
 			((TextView) convertView.findViewById(R.id.channel_li_name)).setText(chandata.get(position).name);
 			((TextView) convertView.findViewById(R.id.channel_li_now)).setText(chandata.get(position).now);
 			((TextView) convertView.findViewById(R.id.channel_li_time)).setText(chandata.get(position).timeline);
 
 			return convertView;
 				
 		}
 		
 		public void addChannel(ChannelInfo input)
 		{
 			chandata.add(input);
 			this.notifyDataSetChanged();
 		}
     }
     
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
         	Log.d("CHANDTHREAD", "Requesting Channellist");
             //Read Channels
             SVDRP vdr = new SVDRP(host,vdr_show_channels.this);
             
             List<String> channels = vdr.getListData("LSTC");
                 		
             if(channels == null)
             {
             	Message msg = mHandler.obtainMessage();
    		     	Bundle b = new Bundle();
    		     	b.putBoolean("error", true);
    		     	
    		     	msg.setData(b);
    		     	mHandler.sendMessage(msg);	
    		     	vdr.close();
    		     	return;
             }
             
             ListIterator<String> chan_it = channels.listIterator();
             while(chan_it.hasNext() && state == 0)
     		{
             	
     			String line = chan_it.next();
     			Log.d("VDRCHAN",line);
     			if(line.startsWith("250 "))
     				break;
     			String cnr = line.split(" ",2)[0].split("-",2)[1];
     			String cname = line.split(" ",2)[1].split(";",2)[0];
     			
     			//Kanalname bereinigen
     			cname = cname.split(",")[0];
     			cname = cname.split(":")[0];
     			
     			String cnow = "Keine EPG Daten";
     			int ctime = 0;
     			int cdur = 0;
     			
     			List<String> epg = vdr.getListData("LSTE "+ cnr + " now");
 				ListIterator<String> epg_it = epg.listIterator();
 				while(epg_it.hasNext())
 				{
 					String epg_line = epg_it.next();
 					if(epg_line.startsWith("215-T"))
 						cnow = epg_line.split(" ", 2)[1];
 					if(epg_line.startsWith("215-E"))
 					{
 						ctime = Integer.parseInt(epg_line.split(" ")[2]);
 						cdur = Integer.parseInt(epg_line.split(" ")[3]);
 					}
 				}
 				
     			Message msg = mHandler.obtainMessage();
    		     	Bundle b = new Bundle();
    		     	b.putString("name", cname);
    		     	b.putString("cnr", cnr);
    		     	b.putString("now", cnow);
    		     	b.putInt("time", ctime);
    		     	b.putInt("dur", cdur);
    		     	
    		     	msg.setData(b);
    		     	mHandler.sendMessage(msg);	
     		}
     		vdr.close();
         }
 	}
 }
