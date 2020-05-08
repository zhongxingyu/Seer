 package kits.vdroid;
 import java.util.List;
 import java.util.ListIterator;
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class vdr_show extends Activity {
		
 	private ListView menulist;
 	private TextView head;
 	private TextView vdr_status;
 	private TextView vdr_status_head;
 	private UpdateThread updthread;
 	private SVDRP vdrcon;
 	private String host;
 	
 	final Intent menustart = new Intent(Intent.ACTION_VIEW);
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
              
         //Parse intent data
         Uri data = getIntent().getData();
         host = data.getHost();
         String hostname = data.getQuery();
         
         //Setup intents
         Uri vdruri = Uri.parse("vdr://" + host + "/");
         menustart.setData(vdruri);
         
 		//Create UI
         setContentView(R.layout.vdrshow);
         head = (TextView) findViewById(R.id.vdr_show_head);
         menulist = (ListView) findViewById(R.id.vdr_actions);
         vdr_status = (TextView) findViewById(R.id.vdr_status_chan);
         vdr_status_head = (TextView) findViewById(R.id.vdr_status_head);
                     
         //Set Head Message
         String headmsg = hostname + " (" + host + ")";
         head.setText(headmsg);
         
         //Set Menuarray
         String[] VDR_MENUITEMS = getResources().getStringArray(R.array.vdr_menu);
         menulist.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, VDR_MENUITEMS));
      
         menulist.setOnItemClickListener(new OnItemClickListener()
 		{
 		    public void onItemClick(AdapterView<?> parent, android.view.View view,int position, long id)
 		    {
 		    	switch(position)
 		    	{
 			    	case 0:
 			    		stopUpdateThread();
 			    		menustart.setClass(kits.vdroid.vdr_show.this, kits.vdroid.vdr_show_channels.class);
 			    		startActivity(menustart);
 			    		break;
 			    	case 1:
 			    		stopUpdateThread();
 			    		menustart.setClass(kits.vdroid.vdr_show.this, kits.vdroid.vdr_timers.class);
 			    		startActivity(menustart);
 			    		break;
 			    	case 2:
 			    		stopUpdateThread();
 			    		menustart.setClass(kits.vdroid.vdr_show.this, kits.vdroid.vdr_recordings.class);
 			    		startActivity(menustart);
 			    		break;
 			    	default:
 			    		Toast.makeText(vdr_show.this,"Not implemented!", Toast.LENGTH_LONG).show();
 		    	}
 		    }
 		});
         
         vdrcon = new SVDRP(host,2001);
     	String server_greeting = vdrcon.getGreeting();
         	
     	vdrcon.close();
     	if(server_greeting == "N/A")
     	{
     		Toast.makeText(vdr_show.this,"Keine Verbindung!", Toast.LENGTH_LONG).show();
     		finish();
     	}
     	else if(server_greeting.contains("Access denied"))
     	{
     		Toast.makeText(vdr_show.this,"VDR weist Verbindung ab", Toast.LENGTH_LONG).show();
     		finish();
     	}
     	else if(server_greeting == null)
     	{
     		Toast.makeText(vdr_show.this,"VDR-Interface blockiert!", Toast.LENGTH_LONG).show();
     		finish();
     	}
     	else    		
     		Toast.makeText(vdr_show.this,server_greeting, Toast.LENGTH_LONG).show();
     	
     }
 	
 	@Override
 	public void onResume() {
         super.onResume();
         updthread = new UpdateThread(upd_handler);
         updthread.start();
 	}
 	
 	@Override
 	public void onPause() {
 		vdrcon.close();
         super.onPause();
         stopUpdateThread();
 	}
 	
 	
 	final Handler upd_handler = new Handler() {
         public void handleMessage(Message msg) {
             String status = msg.getData().getString("status");
             String disk = msg.getData().getString("disk");
             vdr_status.setText(status);
             vdr_status_head.setText("VDR Status - " + disk + " Speicher belegt");
         }
     };
     
     
     private void stopUpdateThread()
 	{
 		updthread.stopUpdates();
     	try {
 			updthread.join();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
     
     private class UpdateThread extends Thread {
         Handler mHandler;
         
         int state = 0;
         
         UpdateThread(Handler h) {
             mHandler = h;
         }
         
         public void stopUpdates()
         {
         	state = 1;
         }
         
         public void run() {
 
         	try {
 				Thread.sleep(2000);
 			} catch (InterruptedException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
         	String act_title = "N/A";
         	while(state == 0) //run forever
         	{
         		
            		try {
 		            Thread.sleep(500);
 		        } catch (InterruptedException e) {
 		            Log.e("ERROR", "Thread Interrupted");
 		        }
 		        
 		        SVDRP thvdr = new SVDRP(host,2001);
 				
 		        String[] actchan = null;
 		        String actdisk = null;
 		        
 		        String actchan_line = thvdr.getData("CHAN");
 		        String actdisk_line = thvdr.getData("STAT DISK");
 		        
 				if(actchan_line.startsWith("250") && actdisk_line.startsWith("250"))
 				{
 					actchan = actchan_line.split(" ", 3); //1 - kanal; 2 - Sendername
 					actdisk = actdisk_line.split(" ")[3];
 				}
 				else
 				{
 					thvdr.close();
 					continue;
 				}
 				
 				//Get act Program
 					
 				List<String> act_epg = thvdr.getListData("LSTE "+ actchan[1] + " now");
 				thvdr.close(); //Close VDR-Connection we dont need it anymore
 				ListIterator<String> epg_it = act_epg.listIterator();
 				while(epg_it.hasNext())
 				{
 					String line = epg_it.next();
 					if(line.startsWith("215-T"))
 					{
 						act_title = line.split(" ", 2)[1];
 						break;
 					}
 				}
 				
 		        String line = actchan[1] + " - " + actchan[2] + "\nEs Läuft: " + act_title;
 				        
 		        Message msg = mHandler.obtainMessage();
 		        Bundle b = new Bundle();
 		        b.putString("status", line);
 		        b.putString("disk", actdisk);
 		        msg.setData(b);
 		        mHandler.sendMessage(msg);
 		        
 		    }
         	
         	Log.d("VDRTHREAD", "Ended");
         }
         
     }
 
 	
 }
