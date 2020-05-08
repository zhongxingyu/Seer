 package kits.vdroid;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.ListIterator;
 
 import android.app.Activity;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class vdr_info extends Activity {
 	String host;
 	String time;
 	String chan;
 	String cnr;
 	String title_timer;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.info);
         Uri data = getIntent().getData();
         host = data.getHost();
         chan = data.getQueryParameter("chan");
         time = data.getQueryParameter("time");
         Log.d("VDRINFO", "Getting Info's for "+  chan + " at " + time);
         
         final SVDRP vdr = new SVDRP(host,this);
         
         if(!time.contains("now"))
         	time = "at " + time;
         
         String qry = "LSTE " + chan + " " + time;
         Log.d("VDRINFO", qry);
         List<String> epginfo = vdr.getListData(qry);
         ListIterator<String> epg_it = epginfo.listIterator();
         
        //vdr.close();
         String title = null;
         String channame = null;
         String subtitle = null;
         String desc = null;
         long ctime = 0;
         long cdur = 0;
         
         while(epg_it.hasNext())
         {
         	String line = epg_it.next();
         	if(line.startsWith("215-T"))
         		title = line.split(" ",2)[1];
         	if(line.startsWith("215-S"))
         		subtitle = line.split(" ",2)[1];
         	if(line.startsWith("215-D"))
         		desc = line.split(" ",2)[1];
         	if(line.startsWith("215-C"))
         		channame = line.split(" ",3)[2];
         	
         	if(line.startsWith("215-E"))
 			{
 				ctime = Integer.parseInt(line.split(" ")[2]);
 				cdur = Integer.parseInt(line.split(" ")[3]);
 			}
         }
         title_timer = title;
         //Fill Data
         TextView info_title = ((TextView) findViewById(R.id.info_title));
         info_title.setText(title);
         TextView info_subtitle = ((TextView) findViewById(R.id.info_subtitle));
         info_subtitle.setText(subtitle);
         TextView info_desc = ((TextView) findViewById(R.id.info_desc));
         info_desc.setText(desc);
         TextView info_chan = ((TextView) findViewById(R.id.info_channame));
         info_chan.setText(channame);
         
         //Date
         long endtime = ctime + cdur;
     	final Date start = new Date(ctime*1000);
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
     	String timeline = start_str + " - " + end_str;
     	
     	final String start_timer = start_h + start_m;
     	final String end_timer = end_h + end_m;
     	
     	TextView info_time = ((TextView) findViewById(R.id.info_timeline));
         info_time.setText(timeline);
         
         //Buttons
         final Button switch_btn = (Button) findViewById(R.id.info_switch);
 	    switch_btn.setOnClickListener(new OnClickListener()
 	    {
 	        public void onClick(View v)
 	        {
 	            vdr.getData("CHAN " + chan);
 	            vdr.close();
 	            Toast.makeText(vdr_info.this,getResources().getText(R.string.toast_switch_chan), Toast.LENGTH_LONG).show();
 	            finish();
 	        }
 	    }); 
 	    
 	    final Button rec_btn = (Button) findViewById(R.id.info_rec);
 	    rec_btn.setOnClickListener(new OnClickListener()
 	    {
 	        public void onClick(View v)
 	        {
 	        	SimpleDateFormat date_fmt = new SimpleDateFormat("yyyy-MM-dd");
 	        	String timer_date = date_fmt.format(start);
 	        	String timer = "1:" + chan + ":" + timer_date + ":" + start_timer + ":" + end_timer + ":99:99:" + title_timer + ":";
 	            vdr.getData("NEWT " + timer);
 	            vdr.close();
 	            Toast.makeText(vdr_info.this,getResources().getText(R.string.toast_record), Toast.LENGTH_LONG).show();
 	            finish();
 	        }
 	    }); 
 	}
 }
