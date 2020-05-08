 package com.fastrunningblog.FastRunningFriend;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Comparator;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.BroadcastReceiver;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.widget.TextView;
 import android.widget.Spinner;
 import android.widget.ArrayAdapter;
 
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 import android.view.ContextMenu; 
 import android.view.MenuInflater; 
 import android.view.LayoutInflater; 
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;  
 import android.view.ViewGroup;  
 import android.view.ContextMenu.ContextMenuInfo;  
 
 import android.view.KeyEvent;
 import android.view.WindowManager;
 import android.os.Handler;
 import android.os.SystemClock;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiManager;
 import android.net.wifi.ScanResult;
 import android.net.DhcpInfo;
 import android.util.Log;
 
 import java.util.Calendar;
 import java.util.List;
 
 
 class GPSCoord
 {
   public double lon,lat,dist_to_prev;
   public float speed,bearing,accuracy;
   public long ts;
   public double angle_cos;
   public int angle_sign;
   public boolean angle_valid;
   public boolean good;
   static float[] results = new float[1];
   
   public GPSCoord()
   {
     reset();
   }
   
   public void init(Location loc, ConfigState cfg)
   {
     lon = loc.getLongitude();
     lat = loc.getLatitude();
     speed = loc.getSpeed();
     bearing = loc.getBearing();
     accuracy = loc.getAccuracy();
     dist_to_prev = 0.0;
     ts = FastRunningFriend.running_time(cfg);
     angle_valid = false;
     good = false;
   }
   
   public double get_dist(GPSCoord other)
   {
     Location.distanceBetween(lat,lon,other.lat,other.lon,results);
     return (double)results[0]/1609.34;
   }
 
   public void mark_point(GPSCoord prev, GPSCoord next, ConfigState cfg)
   {
     if (!angle_valid)
     {  
       good = false;
       return;
     }
     
     if (angle_cos > cfg.min_cos)
     {
       good = true;
       return;
     }
     
     boolean good_signs = (angle_sign == prev.angle_sign && angle_sign == next.angle_sign);
     
     if (!good_signs)
     {
       good = false;
       return;
     }
     
     if (prev.angle_cos > cfg.min_neighbor_cos && next.angle_cos > cfg.min_neighbor_cos)
     {
       good = true;
       return;
     }
     
     good = false;
   }
 
   public void set_angle(GPSCoord prev, GPSCoord next, double lat_cos)
   {
     double dx_prev,dy_prev,dx_next,dy_next,dot_p,sq_prod;
     dx_next = (next.lat - lat) * 1000.0;
     dx_prev = (lat - prev.lat) * 1000.0;
     dy_next = (next.lon - lon) * 1000.0 * lat_cos;
     dy_prev = (lon - prev.lon) * 1000.0 * lat_cos;
     dot_p = dx_next*dx_prev + dy_next * dy_prev;
     sq_prod = (dx_next*dx_next+dy_next*dy_next)*(dx_prev*dx_prev + dy_prev*dy_prev);
     
     if (sq_prod == 0.0)
     {
       return;
     }
     
     angle_valid = true;
     angle_cos = dot_p/Math.sqrt(sq_prod);
     double cross_p = dy_next * dx_prev - dy_prev * dx_next;
     
     if (cross_p > 0)
       angle_sign = -1;
     else if (cross_p < 0)
       angle_sign = 1;
     else 
       angle_sign = 0;
   }
 
   public void set_dist_to_prev(GPSCoord prev)
   {
     Location.distanceBetween(lat,lon,prev.lat,prev.lon,results);
     dist_to_prev = (double)results[0];
   }
 
   public void reset()
   {
     lon = lat = 0.0;
     ts = 0;
     angle_valid = false;
     good = false;
   }
     
   public void add(GPSCoord other)
   {
     lon += other.lon;
     lat += other.lat;
     ts += other.ts;
   }
   
   public void div(int n)
   {
     lon /= n;
     lat /= n;
     ts /= n;
   }
 };
 
 class SelectRunAdapter<T> extends ArrayAdapter<T>
 {
    public SelectRunAdapter(Context context, int textViewResourceId, List<T> objects) {
         super(context, textViewResourceId, objects);
 
         //mObjects = Arrays.asList(objects);
         //mOriginalValues = (ArrayList<T>) Arrays.asList(objects);
   }
   
   public View getView(int position, View  cv, ViewGroup  parent) {
     return cv;
     /*
     TextView v = (TextView)cv;
     
     if (v == null)
      {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = (TextView)inflater.inflate(R.layout.review, null);
      }
      
      v.setText("Hello, world!");
      return v;
     */
 }  
 }
 
 class DistInfo
 {
   public double dist,pace_t;
   public long ts;
   public enum ConfidenceLevel { NORMAL,INITIAL,BAD_SIGNAL,SUSPECT_SIGNAL,SIGNAL_LOST,
           SIGNAL_SEARCH,
           SIGNAL_RECOVERY, 
           SIGNAL_RESTORED,
           SIGNAL_DISABLED
   };
   public ConfidenceLevel conf_level = ConfidenceLevel.SIGNAL_DISABLED;
   public String debug_msg = "";
   
   public String get_status_str()
   {
     switch (conf_level)
     {
       case NORMAL:
         return "Good signal";
       case INITIAL:
         return "Initial guess";
       case BAD_SIGNAL:
         return "Bad signal";
       case SUSPECT_SIGNAL:
         return "Suspect signal";
       case SIGNAL_LOST:
         return "Signal lost";
       case SIGNAL_DISABLED:
         return "Signal disabled";
       case SIGNAL_SEARCH:
         return "Searching for signal";
       case SIGNAL_RECOVERY:
         return "Lost signal recovery";
       case SIGNAL_RESTORED:
         return "Signal restored";
       default:
         return "Unknown";
     }
   }
 };
 
 class GPSCoordBuffer
 {
     public static final int COORD_BUF_SIZE = 1024;
     public static final int SAMPLE_SIZE = 8;
     protected GPSCoord[] buf = new GPSCoord[COORD_BUF_SIZE];
     protected int buf_start = 0, buf_end = 0, flush_ind = 0;
     long last_dist_time = 0;
     protected GPSCoord from_coord = new GPSCoord();
     protected GPSCoord to_coord = new GPSCoord();
     protected long points = 0, points_since_signal = 0;
     protected double total_dist = 0.0;
     protected int last_trusted_ind = 0;
     protected long last_trusted_ts = 0;
     protected double last_trusted_d = 0.0;
     protected double lat_cos = 0.0;
     protected boolean lat_cos_inited = false;
     protected int last_processed_ind = 0, last_good_ind = 0;
     protected double last_pace_t = 0.0;
     protected double pause_pace_t = 0.0;
     public DistInfo.ConfidenceLevel conf_level = DistInfo.ConfidenceLevel.INITIAL;
     
     protected ConfigState cfg = null;
     protected RunInfo run_info = null;
     public static final String TAG = "FastRunningFriend";
     
     public native boolean init_data_dir(String dir_name);
     public native boolean open_data_file();
     public native boolean close_data_file();
     public native boolean flush();
     public native void debug_log(String msg);
     
     public GPSCoordBuffer(ConfigState cfg,RunInfo run_info)
     {
       this.cfg = cfg;
       this.run_info = run_info;
       
       for (int i = 0; i < COORD_BUF_SIZE; i++)
         buf[i] = new GPSCoord();
     }
     
     public void reset(boolean reset_dist)
     {
       flush();
       last_processed_ind = flush_ind = buf_start = buf_end = 0;
       last_trusted_d = 0.0;
       last_trusted_ind = 0;
       last_trusted_ts = 0;
       last_pace_t = 0.0;
       last_good_ind = 0;
       
       if (reset_dist)
        total_dist = 0.0;
       
       points_since_signal = points = 0;
     }
     
     public GPSCoord get_prev()
     {
       if (points < 2)
         return null;
       
       int ind = buf_end - 1;
       
       if (ind < 0)
         ind += COORD_BUF_SIZE;
       
       return buf[ind];
     }
     
     public void update_dist(boolean final_update)
     {
       int i,update_end = buf_end - 2, start_ind = last_good_ind + 1;
       
       debug_log(String.format("total_dist=%f last_trusted_d=%f, start_ind=%d,update_end=%d",
                               total_dist, last_trusted_d, start_ind, update_end));
       
       if (final_update)
         update_end++;
       
       if (update_end < 0)
         update_end += COORD_BUF_SIZE;
       
       if (final_update)
         buf[update_end].good = true;
       
       if (start_ind == COORD_BUF_SIZE)
         start_ind = 0;
       
       for (i = start_ind; i != update_end; )
       {
         debug_log(String.format("Point %d at %d is %s", points, i, buf[i].good ? "good" : "bad"));
         
         if (buf[i].good)
         {
           last_trusted_d += buf[i].get_dist(buf[last_good_ind]);
           last_good_ind = i;
           debug_log(String.format("i=%d last_trusted_d=%f",i,last_trusted_d));
           
           if (last_trusted_d < cfg.min_d_last_trusted)
             return;
           
           long dt = buf[i].ts - buf[last_trusted_ind].ts;
           
           if (dt == 0)
             return;
           
           double pace_t = (double)dt/last_trusted_d;
           boolean good_pace = (
                        (last_pace_t == 0.0 && pace_t > cfg.top_pace_t) ||
                        Math.abs(last_pace_t/pace_t - 1.0) < cfg.max_pace_diff
                       );
           if (good_pace || last_trusted_d > cfg.max_d_last_trusted || 
               (final_update && i == update_end))
           {
             if (good_pace)
             {
               total_dist += last_trusted_d;
               last_trusted_d = 0.0;
               last_trusted_ts = buf[i].ts;
               last_pace_t = pace_t;
               last_trusted_ind = i;
               conf_level = DistInfo.ConfidenceLevel.NORMAL;
               debug_log("Trusted point at index " + i + ", good pace " + pace_t);
               return;
             }
             
             double dx_direct = buf[i].get_dist(buf[last_trusted_ind]);
             double direct_pace_t = 0.0;
             
             if (dx_direct > 0.0)
               direct_pace_t = dt/dx_direct;
             
             boolean use_direct = false;
             
             if (last_pace_t == 0.0)
             {
               if (pace_t < cfg.top_pace_t)
                 use_direct = true;
             }
             else
             {
               if (Math.abs(last_pace_t - direct_pace_t) < 
                   Math.abs(last_pace_t - pace_t) && cfg.top_pace_t > pace_t)
                 use_direct = true;
             }
             
             if (use_direct)
             {  
               total_dist += dx_direct;
               last_pace_t = direct_pace_t;
               conf_level = DistInfo.ConfidenceLevel.BAD_SIGNAL;
               debug_log("BAD_SIGNAL: Trusted point at index " + i 
                 + ", direct pace " + direct_pace_t + ", dx_direct=" + dx_direct );
             }  
             else
             {  
               total_dist += last_trusted_d;
               last_pace_t = pace_t;
               conf_level = DistInfo.ConfidenceLevel.SUSPECT_SIGNAL;
               debug_log("SUSPECT_SIGNAL: Trusted point at index " + i 
                 + ", integrated pace " + pace_t );
             }  
             
             last_trusted_ind = i;
             last_trusted_d = 0.0;
             
             last_trusted_ts = buf[i].ts;
             return;
           }
           
           break;
         }
         
         if (++i == COORD_BUF_SIZE)
           i = 0;
       }
     }
     
     public void push(Location coord)
     {
       if (buf_end < COORD_BUF_SIZE)
       {
         int save_buf_end = buf_end;
         buf[buf_end++].init(coord,cfg);
         
         if (buf_end == COORD_BUF_SIZE)
         {
           buf_end = 0;
           buf_start++;
         }
         else if (buf_end <= buf_start)
           buf_start++;
          
         if (buf_start == COORD_BUF_SIZE)
             buf_start = 0;
 
         if (!lat_cos_inited)
         {  
           lat_cos = Math.cos(buf[save_buf_end].lat*Math.PI/180.0);
           lat_cos_inited = true;
         }
           
         if (points > 0)
         {  
           int prev_ind = save_buf_end - 1;
           
           if (prev_ind < 0)
             prev_ind += COORD_BUF_SIZE;
           
           //buf[save_buf_end].set_dist_to_prev(buf[prev_ind]);
           if (points > 1)
           {
             int p_prev_ind = prev_ind - 1;
             
             if (p_prev_ind < 0)
               p_prev_ind += COORD_BUF_SIZE;
             
             buf[prev_ind].set_angle(buf[p_prev_ind],buf[save_buf_end],lat_cos);
             
             if (points > 2)
             {
               int pp_prev_ind = p_prev_ind - 1;
               
               if (pp_prev_ind < 0)
                 pp_prev_ind += COORD_BUF_SIZE;
               
               buf[p_prev_ind].mark_point(buf[pp_prev_ind],buf[prev_ind], cfg);
             }
           }
         }
           
         points++;
         points_since_signal++;
         flush();
         
         switch(conf_level)
         {
           case SIGNAL_LOST:
             if (points_since_signal > 1)
             {  
               handle_no_signal();
               conf_level = DistInfo.ConfidenceLevel.SIGNAL_RESTORED;
             }
             break;
           case SIGNAL_SEARCH:
             conf_level = DistInfo.ConfidenceLevel.INITIAL;
             break;
           default:
             break;
         }
         
         if (points_since_signal >= 3)
           update_dist(false);
       }  
     }
  
   
     public long get_last_ts()
     {
       if (points == 0)
         return 0;
       
       int last_ind = buf_end - 1;
       
       if (last_ind < 0)
         last_ind += COORD_BUF_SIZE;
         
       return buf[last_ind].ts;    
     } 
    
     public void sync_dist_info(DistInfo di, long run_time, boolean update_total_dist)
     {
       if (points_since_signal >= 3)
         update_dist(true);
       
       get_dist_info(di,run_time);
       
       if (update_total_dist)
         total_dist = di.dist;
     }
    
     public void handle_pause(DistInfo di)
     {
       long run_time = run_info.t_total;
       double save_pace_t = last_pace_t;
       
       sync_dist_info(di,run_time,true);
       reset(false);
       last_trusted_ts = run_time;
       last_pace_t = save_pace_t;
     }
    
     /* 
     public void handle_pause_old_buggy(DistInfo di)
     {
       double dx;
       long pause_dt = (cfg.pause_time - cfg.start_time) - last_trusted_ts;
       double pace_t;
       
       if (points_since_signal == 0)
       {
         pace_t = (last_pace_t > 0.0) ? last_pace_t : cfg.start_pace_t;
         dx = (double)pause_dt/pace_t;
         total_dist += dx;
         di.dist = total_dist;
         di.pace_t = pace_t;
         last_trusted_ts = (cfg.pause_time - cfg.start_time);
         return;
       }
       
       int cur_ind = buf_end - 1;
       
       if (cur_ind < 0)
         cur_ind += COORD_BUF_SIZE;
       
       long dt = buf[cur_ind].ts - last_trusted_ts;
       pace_t = (last_trusted_d > 0.0) ? dt/last_trusted_d : last_pace_t ;
       
       if (pace_t == 0.0)
         pace_t = cfg.start_pace_t;
       
       boolean good_pace =
          (pace_t > cfg.top_pace_t || (Math.abs(last_pace_t/pace_t-1.0) < 2.0 * cfg.max_pace_diff));
       
       if (pause_dt < 0)
         pause_dt = 0;
       
       if (good_pace)
       {
         dx = last_trusted_d + (double)pause_dt/pace_t;
       }  
       else
       {
         double direct_dx = buf[cur_ind].get_dist(buf[last_trusted_ind]);
         double direct_pace_t = (direct_dx > 0) ? dt/direct_dx : 0.0;
         boolean use_direct = false;
         
         if (last_pace_t == 0.0)
           use_direct = true;
         else if (direct_pace_t > 0.0 && 
               Math.abs(direct_pace_t - last_pace_t) < Math.abs(pace_t - last_pace_t))
         {
           use_direct = true;
         }
         
         if (use_direct)
         {
           dx = direct_dx + (double)pause_dt/last_pace_t;
           pace_t = direct_pace_t;
         }
         else
           dx = last_trusted_d + (double)pause_dt/pace_t;
       }
       
       if (dx > 0.0)
         total_dist += dx;
      
       pause_pace_t = (pace_t == 0.0) ? cfg.start_pace_t : pace_t;
       di.dist = total_dist ;
       di.pace_t = pause_pace_t;
       
       last_trusted_ind = 0;
       last_good_ind = 0;
       points_since_signal = 0;
       last_trusted_ts = (cfg.pause_time - cfg.start_time);
       last_trusted_d = 0.0;
     }
     */
     
     public void handle_no_signal()
     {
       int cur_ind = buf_end - 1;
       
       if (cur_ind < 0)
         cur_ind += COORD_BUF_SIZE;
       
       last_trusted_ind = cur_ind;
       last_trusted_d = 0.0;
       last_good_ind = cur_ind;
       points_since_signal = 1;
       long dt = buf[cur_ind].ts - last_trusted_ts;
       double pace_t = (last_pace_t > 0.0) ? last_pace_t : cfg.start_pace_t;
       double dx = (double)dt/pace_t;
       
       if (dx > 0.0)
         total_dist += dx;
       
       last_trusted_ts = buf[cur_ind].ts;
     }
     
     public void get_dist_info(DistInfo di, long now_ts) // need now_ts to avoid checking time twice
     {
       di.dist = total_dist;
       di.pace_t = (last_pace_t > 0.0) ? last_pace_t : cfg.start_pace_t;
       di.ts = last_trusted_ts;
       di.conf_level = conf_level;      
       long dt = now_ts - di.ts; // if last_trusted_ts is 0 it still works
       
       if (dt > cfg.max_t_no_signal && points_since_signal > 0)
       {
         di.conf_level = conf_level = DistInfo.ConfidenceLevel.SIGNAL_LOST;
         debug_log("Signal lost");
       }
       
       if (dt > 0 && di.pace_t > 0.0)
       {  
         di.dist += (double)dt/di.pace_t;
       }  
     }
     
     /*
     public double get_dist_old()
     {
       if (points < SAMPLE_SIZE)
         return 0.0;
         
       int from_ind = buf_end - SAMPLE_SIZE ;
       int mid_ind = buf_end - SAMPLE_SIZE/2;
       int to_ind = buf_end ;
       
       if (from_ind < 0)
        from_ind += COORD_BUF_SIZE;
        
       if (to_ind < 0)
        to_ind += COORD_BUF_SIZE;
        
       if (mid_ind < 0)
        mid_ind += COORD_BUF_SIZE;
       
       from_coord.reset();
       to_coord.reset();
       
       GPSCoord col_coord = from_coord; 
       
       for (int i = from_ind; i != to_ind; )
       {
         col_coord.add(buf[i]);  
         i++;
         if (i == COORD_BUF_SIZE)
           i = 0;
           
         if (i == mid_ind)
           col_coord = to_coord;  
           
       }
       
       from_coord.div(SAMPLE_SIZE/2);
       to_coord.div(SAMPLE_SIZE/2);
       
       last_dist_time = (to_coord.ts - from_coord.ts) /
          (SAMPLE_SIZE/2);         
       return from_coord.get_dist(to_coord)/(SAMPLE_SIZE/2);
     }
     
     long get_last_dist_time() { return last_dist_time; }
     */
 };
 
 class SelectRunListener implements OnItemSelectedListener
 {
   public FastRunningFriend f = null;
   
   public SelectRunListener(FastRunningFriend f)
   {
     this.f = f;
   }
   
   public void onItemSelected(AdapterView<?> parent, View view, int pos,long id)
   {
     f.handle_select_run(parent.getItemAtPosition(pos).toString());
   }
   
   @Override
   public void onNothingSelected(AdapterView<?> arg0) 
   {
   }
 }
 
 public class FastRunningFriend extends Activity implements LocationListener
 {
     LocationManager lm;
     TextView total_time_tv,total_dist_tv,pace_tv,status_tv,time_of_day_tv,battery_tv;
     TextView split_time_tv, split_dist_tv, leg_time_tv, leg_dist_tv;
     TextView review_tv;
     Spinner select_run;
     ViewGroup container;
     public static final String PREFS_NAME = "FastRunningFriendPrefs";
     public static final String TAG = "FastRunningFriend";
 
     Handler timer_h = new Handler();
     Handler tod_timer_h = new Handler();
     ConfigState cfg = new ConfigState();
     WifiConfiguration wifi_cfg = new WifiConfiguration();
     WifiManager wifi = null;
     RunInfo run_info = new RunInfo();
     GPSCoordBuffer coord_buf = new GPSCoordBuffer(cfg,run_info);
     protected long dist_uppdate_ts = 0;
     protected DistInfo dist_info = new DistInfo();
     protected enum TimerState {RUNNING,PAUSED,INITIAL;}
     protected enum TimerAction {START,SPLIT,PAUSE,RESET,RESUME,
       START_LEG,START_GPS,STOP_GPS,
       IGNORE,MENU,PASS;}
     protected enum ButtonCode {START,SPLIT,IGNORE,BACK;};
     enum ViewType { VIEW_MAIN, VIEW_REVIEW, VIEW_NONE};
     ViewType view_type = ViewType.VIEW_NONE;
     
     TimerState timer_state = TimerState.INITIAL; 
     boolean gps_running = false;
     int wifi_id = -1;
     boolean wifi_on = false;
     boolean disconnect_wifi_on_exit = true;
     boolean wifi_found = false;
     Calendar cal = Calendar.getInstance();
     protected long dist_update_ts = 0;
     
     public native void set_system_time(long t_ms);
    MenuInflater menu_inflater = getMenuInflater();
     
     BroadcastReceiver battery_receiver = new BroadcastReceiver() {
         
         @Override
         public void onReceive(Context context, Intent intent) 
         {
             update_battery_status(intent);
         }
     };
     
     Runnable update_tod_task = new Runnable()
     {
       public void run()
       {
         if (time_of_day_tv != null)
         {
           cal.setTimeInMillis(System.currentTimeMillis());
           int h = cal.get(Calendar.HOUR_OF_DAY);
           final boolean is_am = (h < 12);
           
           if (h > 12)
             h -= 12;
           
           time_of_day_tv.setText(String.format("%02d:%02d:%02d%s %02d/%02d/%04d",
                                                 h,
                                                 cal.get(Calendar.MINUTE),
                                                 cal.get(Calendar.SECOND),
                                                 is_am ? "am":"pm",
                                                 cal.get(Calendar.MONTH) + 1,
                                                 cal.get(Calendar.DAY_OF_MONTH),
                                                 cal.get(Calendar.YEAR)
                                                ));
         }
         tod_timer_h.postAtTime(this,SystemClock.uptimeMillis()+1000);
       }
     };
     
     Runnable update_time_task = new Runnable()
      {
        public void run()
        {
          final long now = time_now();
          
          if (RunTimer.get_run_info(run_info))
          {  
            update_run_info(false);
          }  
          timer_h.postAtTime(this,SystemClock.uptimeMillis()+100);
          
          if (dist_update_ts == 0 || now - dist_update_ts > cfg.dist_update_interval)
          {
            coord_buf.get_dist_info(dist_info,run_info.t_total);
            show_dist_info(dist_info);
            dist_update_ts = now;
          }
        }
      };
      
      static
      {
        System.loadLibrary("fast_running_friend");  
      }
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle b)
     {
         super.onCreate(b);
         set_main_view();
         lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         
         getWindow().addFlags(
           WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
         RunTimer.init(cfg.data_dir + "/");          
         update_tod_task.run(); // do one run so we do not wait a second before the time_now
                                // appears
         start_tod_timer();
         IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
         registerReceiver(battery_receiver, filter);
         
         if (!coord_buf.init_data_dir(cfg.data_dir))
         {
           update_status("Directory " + cfg.data_dir + 
            " does not exist and could not be created, will not save data");
         }
         else
         {
           if (!cfg.read_config("default"))
             update_status("Error reading config file");
           cfg.write_config("test");
         }
         
         try
         {
           wifi_init();
         }
         catch (Exception e)
         {
           Log.e(TAG, "WiFi Exception:" + e);
         }
     }
     
     StringBuffer config_url_msg = new StringBuffer(128);
     
     void init_select_run()
     {
       ArrayAdapter<String> da = new ArrayAdapter<String>(this, 
                  android.R.layout.simple_spinner_item, RunTimer.get_run_list());
       da.sort(new Comparator<String>() 
         {
           public int compare(String s1, String s2)
           {
             return s2.compareTo(s1);
           }
         });
       da.setDropDownViewResource(android.R.layout.simple_spinner_item);
       select_run.setAdapter(da);
       select_run.setPrompt("Select Workout");
       select_run.setOnItemSelectedListener(new SelectRunListener(this));
     }
     
     void set_review_view()
     {
       setContentView(R.layout.review); 
       review_tv = (TextView) findViewById(R.id.review_tv);
       select_run = (Spinner) findViewById(R.id.select_run);
       init_select_run();
       view_type = ViewType.VIEW_REVIEW;
     }
     
     void update_battery_status(Intent intent)
     {
       try
       {
         if (intent == null)
         {
           intent = getApplicationContext().registerReceiver(null,
                                                             new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
         }
         
         if (battery_tv != null)
         {  
           int level = intent.getIntExtra("level", -1);
           int scale = intent.getIntExtra("scale", -1);
           battery_tv.setText(String.format("Battery %d%%", level*100/scale));
         }  
       }  
       catch (Exception e)
       {
         update_status("Error updating battery info");
       }  
         
     }
     
     void set_main_view()
     {
       setContentView(R.layout.main);
       total_dist_tv = (TextView) findViewById(R.id.total_dist_tv);
       total_time_tv = (TextView) findViewById(R.id.total_time_tv);
       leg_dist_tv = (TextView) findViewById(R.id.leg_dist_tv);
       leg_time_tv = (TextView) findViewById(R.id.leg_time_tv);
       split_dist_tv = (TextView) findViewById(R.id.split_dist_tv);
       split_time_tv = (TextView) findViewById(R.id.split_time_tv);
       pace_tv = (TextView) findViewById(R.id.pace_tv);
       status_tv = (TextView) findViewById(R.id.status_tv);
       time_of_day_tv = (TextView) findViewById(R.id.time_of_day_tv);
       battery_tv = (TextView) findViewById(R.id.battery_tv);
       container = (ViewGroup) findViewById(R.id.container);
       registerForContextMenu(container);      
       update_run_info(true);
       show_dist_info(dist_info);
       update_battery_status(null);
       view_type = ViewType.VIEW_MAIN;
     }
     
     void show_review()
     {
       set_review_view();
       String review_text = RunTimer.get_review_info(cfg.data_dir + "/",null);
       
       if (review_text == null)
         review_text = "Error fetching splits";
       
       if (review_tv != null)
         review_tv.setText(review_text);
     }
     
     void handle_dhcp_acquire()
     {
       if (wifi == null || !wifi_on)
         return;
       
       DhcpInfo dhcp = wifi.getDhcpInfo();
       
       if (dhcp != null)
       {
         int ip = dhcp.ipAddress;
         
         if (ip != 0)
         {  
           start_config_daemon();
           config_url_msg.setLength(0);
           config_url_msg.append("http://");
           config_url_msg.append(ip & 0xff);
           config_url_msg.append('.');
           config_url_msg.append((ip >> 8) & 0xff);
           config_url_msg.append('.');
           config_url_msg.append((ip >> 16) & 0xff);
           config_url_msg.append('.');
           config_url_msg.append((ip >> 24) & 0xff);
           config_url_msg.append(':');
           config_url_msg.append(cfg.http_port);
           update_status(config_url_msg.toString());
         }
       }
     }
     
     @Override
     public boolean onContextItemSelected(MenuItem item) 
     {
       switch (item.getItemId())
       {
         case R.id.menu_reset:
           switch (timer_state)
           {
             case INITIAL:
               finish();
               break;
             default:
               reset_timer();
               break;
           }
           break;
         case R.id.menu_wifi: 
           if (wifi_on)
             wifi_disconnect();
           else
             wifi_connect();
           break;
         case R.id.menu_review:
           show_review();
           break;
         case R.id.menu_gps:
           if (gps_running)
             stop_gps();
           else
             start_gps();
           break;
         case R.id.menu_wifi_exit:
           disconnect_wifi_on_exit = !disconnect_wifi_on_exit;
           break;
       }
       return super.onContextItemSelected(item);
     }
     
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) 
     {  
       super.onCreateContextMenu(menu, v, menuInfo); 
       menu_inflater.inflate(R.layout.menu,menu);
       update_menu_items(menu);
     }  
     
     public void start_config_daemon()
     {
       if (cfg.daemon_running())
         return;
       
       new Thread(new Runnable()
         {
           public void run()
           {
             cfg.run_daemon();
           }
         }).start();  
     }
     
     public void stop_config_daemon()
     {
       cluck("Stack in stop_config_daemon():");
       cfg.stop_daemon();
     }
     
     public void update_menu_items(Menu menu)
     {
       MenuItem reset_item = menu.findItem(R.id.menu_reset);
       MenuItem wifi_item = menu.findItem(R.id.menu_wifi);
       MenuItem gps_item = menu.findItem(R.id.menu_gps);
       MenuItem wifi_exit_item = menu.findItem(R.id.menu_wifi_exit);
       
       if (reset_item != null)
       {
         switch (timer_state)
         {
           case INITIAL:
             reset_item.setTitle("Exit");
             break;
           default:
             reset_item.setTitle("Reset");
             break;
         }
       }
       
       if (wifi_item != null)
       {
         wifi_item.setTitle(wifi_on ? "Turn Off WiFi" : "Turn On WiFi");
         Log.d(TAG, "Fixed wifi_item");
       }
       
       if (gps_item != null)
       {
         gps_item.setTitle(gps_running ? "Turn Off GPS" : "Turn On GPS");
       }
       
       if (wifi_exit_item != null)
       {
         wifi_exit_item.setTitle(disconnect_wifi_on_exit ? "Disconnect Wifi on Exit" : "Keep Wifi On Exit");
       }
     }
     
     public void show_menu()
     {
       if (view_type != ViewType.VIEW_MAIN)
         set_main_view();
       else
         openContextMenu(container);
     }
     
     public void update_run_info(boolean sync_split)
     {
       post_time(run_info.t_total,total_time_tv,PrecType.TOTAL);
       post_time(run_info.t_leg,leg_time_tv,PrecType.LEG);
       
       if (sync_split || run_info.t_split == run_info.t_leg || run_info.t_split > cfg.split_display_pause)
       {  
         post_time(run_info.t_split,split_time_tv,PrecType.SPLIT);
         post_dist(dist_info.dist - run_info.d_last_split, split_dist_tv);
       }  
       post_dist(dist_info.dist - run_info.d_last_leg, leg_dist_tv);
     }
     
     public void wifi_scan()
     {
       if (wifi == null)
         return;
       
       wifi.startScan();
     }
 
     public void wifi_init_open()
     {
       wifi_cfg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
     }
     
     public void wifi_init_wpa()
     {
       wifi_cfg.preSharedKey = cfg.get_wifi_key(64);
       wifi_cfg.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
       wifi_cfg.allowedProtocols.set(WifiConfiguration.Protocol.WPA); // For WPA
       wifi_cfg.allowedProtocols.set(WifiConfiguration.Protocol.RSN); // For WPA2
       wifi_cfg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
       wifi_cfg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
       wifi_cfg.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
       wifi_cfg.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
       wifi_cfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
       wifi_cfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);      
     }
     
     public void wifi_init_wep()
     {
       wifi_cfg.wepKeys[0] = cfg.get_wifi_key(10, 26, 58);
       wifi_cfg.hiddenSSID = true;
       wifi_cfg.status = WifiConfiguration.Status.ENABLED;  
       wifi_cfg.wepTxKeyIndex = 0;
       wifi_cfg.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
       wifi_cfg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
       wifi_cfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
       wifi_cfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
       wifi_cfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
       wifi_cfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
     }
     
     public void wifi_init()
     {
       if (cfg.wifi_ssid.length() == 0)
       {
         update_status("WiFi not configured");
         return;
       }
       
       wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
       
       if (wifi == null)
       {
         update_status("WiFi Manager not available");
         return;
       }
       
       wifi_cfg.SSID = "\"" + cfg.wifi_ssid + "\"";
       List<WifiConfiguration> configs = wifi.getConfiguredNetworks();
       
       for (WifiConfiguration cfg: configs)
       {
         if (cfg.SSID.equals(wifi_cfg.SSID))
         {
           wifi_cfg.networkId = cfg.networkId;
         }
       }
       
       IntentFilter i_f = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
       i_f.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
       registerReceiver(wifi_receiver, i_f);
       registerReceiver(dhcp_receiver,new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
     }
     
     public void test_gps_bug()
     {
       /*
       40.314353,-111.655104,937778340,3.000639,16.000000,9.487171,3.280000
       40.314385,-111.655104,937779341,2950.528076,19.000000,9.487171,3.230000
       */
       GPSCoord p1 = new GPSCoord();
       GPSCoord p2 = new GPSCoord();
       p1.lat = 40.314353;
       p1.lon = -111.655104;
       p2.lat = 40.314385;
       p2.lon = -111.655104;
       double d1 = p1.get_dist(p2);
       double d2 = p2.get_dist(p1);
       update_status(String.format("Bug test: d1=%f,d2=%f",d1,d2));
     }
     
     public void start_tod_timer()
     {
        tod_timer_h.removeCallbacks(update_tod_task);
        tod_timer_h.postDelayed(update_tod_task,1000);
     }
     
     public void onLocationChanged(Location arg0) 
     {
       if (timer_state != TimerState.RUNNING)
       {  
          if (arg0 != null)
          {  
            update_status(String.format("Have signal at %.3f,%.3f",
                                        arg0.getLatitude(),
                                        arg0.getLongitude()));
                                        
            set_system_time(arg0.getTime());                            
          }
          return;
       }
       
       coord_buf.push(arg0);
     }
     
     public boolean wifi_connect_low()
     {
       try
       {
         if ((wifi_id = wifi.updateNetwork(wifi_cfg)) == -1)
         {  
           if ((wifi_id = wifi.addNetwork(wifi_cfg)) == -1)
           {  
             update_status("Error adding wireless network");
             return false;
           }  
           
           Log.d(TAG, "wifi_id = " + wifi_id);
         }  
   
         if (wifi.enableNetwork(wifi_id, true))
         {  
           update_status("Enabled " + cfg.wifi_ssid + " wifi");
           return true;
         }  
         else
           update_status("Failed to enable wifi network");
       }
       catch (Exception e)
       {
         update_status("Wifi error");
         Log.e(TAG,"Wifi Exception: " + e);
       }
       
       return false;
     }
     
     public boolean wifi_connect()
     {
       if (wifi == null)
       {
         update_status("WiFi not configured");
         return false;
       }
       
       try
       {
         if (!wifi.isWifiEnabled())
         {
           update_status("Enabling wifi");
           if (wifi.setWifiEnabled(true))
             update_status("Wifi enabled");
           else
           {
             update_status("Failed to enable Wifi");
             return false;
           }
         }
         
         return (wifi_on = true);
       }
       catch (Exception e)
       {
         update_status("Error in Wifi connect");
         Log.e(TAG,"WiFi Exception: " + e);
       }
       return false;
     }
     
     public void cluck(String msg)
     {
       StringBuffer stack_msg = new StringBuffer(4096);
       stack_msg.append(msg);
       
       for (StackTraceElement ste : Thread.currentThread().getStackTrace()) 
       {
          stack_msg.append(ste.toString() + "\n");
       }
          
       Log.e(TAG,stack_msg.toString());
     }
     
     public boolean wifi_disconnect()
     {
       wifi_found = false;
       if (wifi == null)
         return false;
       
       try
       {
         cluck("stack in wifi_disconnect()");
         
         if (wifi.disconnect())
           update_status("Wifi disconnected");
         else
           update_status("Wifi disconnect failed");
         
         if (wifi.setWifiEnabled(false))
         {
           update_status("Wifi disabled");
           stop_config_daemon();
           wifi_on = false;
           return true;
         }
         else
         {
           update_status("Wifi disable failed");
           return false;
         }
       }
       catch (Exception e)
       {
         update_status("Wifi Error");
         Log.e(TAG, "WiFi Exception: " + e);
         return false;
       }
     }
     
     private void wifi_scan_low()
     {
       if (wifi == null || wifi_found)
         return;
       
       List<ScanResult> nl =  wifi.getScanResults();
       boolean found_match = false;
       
       for (ScanResult n : nl)
       {
         Log.d(TAG,"network '" + n.SSID + "'" + ", cap='" + n.capabilities + "'");
         String quoted_network = "\"" + n.SSID + "\"";
         
         if (quoted_network.equals(wifi_cfg.SSID))
         {
           if (n.capabilities.contains("[WEP]"))
             wifi_init_wep();
           else if (n.capabilities.contains("[WPA]"))
             wifi_init_wpa();
           else
             wifi_init_open();
           
           found_match = true;
         }
       }
       
       if (found_match)
       {
         wifi_found = true;
         wifi_connect_low();
       }  
       else
         update_status("No configured WiFi networks in range");
     }
     
     private BroadcastReceiver dhcp_receiver = new BroadcastReceiver()
     {
       @Override
       public void onReceive(Context context, Intent intent)
       {
         handle_dhcp_acquire();
       }
     };
     
     private BroadcastReceiver wifi_receiver = new BroadcastReceiver()
     {
       @Override
       public void onReceive(Context context, Intent intent) 
       {
         if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction()))
         {
           if (wifi_on)
             wifi_scan_low();
           else
           {  
             Log.e(TAG, "Spurious WiFi scan");
           }  
           return;
         }
         
         int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                       WifiManager.WIFI_STATE_UNKNOWN);
         
         switch (state)
         {
            case WifiManager.WIFI_STATE_DISABLED:
              update_status("WIFI DISABLED");
              break;
            case WifiManager.WIFI_STATE_DISABLING:
              update_status("WIFI DISABLING");
              break;
            case WifiManager.WIFI_STATE_ENABLED:
              update_status("WIFI ENABLED");
              if (wifi_on)
              {
                wifi_scan();
              }  
              else
                wifi_disconnect();
              break;
            case WifiManager.WIFI_STATE_ENABLING:
              update_status("WIFI ENABLING");
              break;
            default:
              update_status("WIFI STATE UNKNOWN");
              break;         
         }
       }
     };
     
     public void onProviderDisabled(String arg0) 
     {
     }
     
     public void onProviderEnabled(String arg0) 
     {
     }
     
     public void onStatusChanged(String arg0, int arg1, Bundle arg2) 
     {
     }
     
     public void update_status(String msg)
     {
       status_tv.setText(msg);
     }
     
     @Override
     public boolean dispatchKeyEvent(KeyEvent ev)
     {
       if (ev.getAction() == KeyEvent.ACTION_UP)
         return true;
       //dist_tv.setText("key=" + key + ", scan_code = " + ev.getScanCode());
       switch (get_timer_action(ev))
       {
         case START:
           start_timer();
           return true;
         case RESUME:
           resume_timer();
           return true;
         case RESET:
           reset_timer();
           return true;
         case START_LEG:
           start_leg();
           return true;
         case START_GPS:
           start_gps();
           return true;
         case STOP_GPS:
           stop_gps();
           return true;
         case SPLIT:
           split_timer();
           return true;
         case PAUSE:
           pause_timer();
           return true;
         case MENU:
           show_menu();
           return true;
         case IGNORE:
           return true;
         case PASS: 
         default:   
           break;
       }
       
       return super.dispatchKeyEvent(ev);
     }
 
     protected ButtonCode get_button_code(KeyEvent ev)
     {
       switch (ev.getKeyCode())
       {
         case KeyEvent.KEYCODE_BACK:
           return ButtonCode.BACK;
         case KeyEvent.KEYCODE_VOLUME_UP:
           return ButtonCode.START;
         case KeyEvent.KEYCODE_VOLUME_DOWN:
           return ButtonCode.SPLIT;
         default:
           break;
       }
       switch (ev.getScanCode())
       {
         case 220:
           return ButtonCode.START;
         case 387:
           return ButtonCode.SPLIT; 
         default:
           return ButtonCode.IGNORE;  
       }
     }
     
     protected TimerAction get_timer_action(KeyEvent ev)
     {
       switch (get_button_code(ev)) 
       {
         case START:
           switch(timer_state)
           {
             case INITIAL:
                return TimerAction.START;
             case PAUSED:
                return TimerAction.RESUME;   
             case RUNNING:
                return TimerAction.PAUSE;  
           }  
           break;
         case SPLIT:
           switch (timer_state)
           {
             case INITIAL:
               return gps_running ? TimerAction.STOP_GPS : TimerAction.START_GPS;
             case RUNNING:
               return TimerAction.SPLIT;
             case PAUSED:
               return TimerAction.START_LEG;
           }
           break;
         
         case BACK:
           switch (timer_state)
           {
             case INITIAL:
             case PAUSED:
               return TimerAction.MENU;
             default:
               return TimerAction.IGNORE;
           }
         case IGNORE:
         default:
           break;
       }
       
       return TimerAction.PASS;  
     }
 
     protected long get_start_time()
     {
       return time_now();
     }
     
     protected void suspend_timer_display()
     {
       timer_h.removeCallbacks(update_time_task);
     }
 
     protected void start_gps()
     {
         Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
         lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
             cfg.gps_update_interval, 1.0f, this);
         String status_msg = "Searching for GPS signal";
         
         Log.i(TAG,"GPS updates every " + cfg.gps_update_interval + " ms");
         
         if (loc != null)
           status_msg += String.format(", last %.3f,%.3f",loc.getLatitude(),
                                       loc.getLongitude());
         update_status(status_msg);    
         gps_running = true;    
         coord_buf.conf_level = DistInfo.ConfidenceLevel.SIGNAL_SEARCH;
    }
     
     protected void stop_gps()
     {
       lm.removeUpdates(this);
       update_status("Stopped GPS updates");    
       gps_running = false;
       coord_buf.conf_level = DistInfo.ConfidenceLevel.SIGNAL_DISABLED;
     }
 
     protected void pause_timer()
     {
       coord_buf.sync_dist_info(dist_info,RunTimer.now(),false);
       RunTimer.pause(dist_info.dist);
       cfg.save_time();
       cfg.pause_time = get_start_time();
       timer_state = TimerState.PAUSED;
       suspend_timer_display();
       coord_buf.handle_pause(dist_info);
       
       if (RunTimer.get_run_info(run_info))
       {  
         update_run_info(false);
       }  
       
       show_dist_info(dist_info);
       coord_buf.debug_log("Timer paused");
     }   
     
     public void onDestroy()
     {
       RunTimer.reset();
       stop_gps();
       
       if (wifi_on && disconnect_wifi_on_exit)
         wifi_disconnect();
       
       stop_config_daemon();
       unregisterReceiver(battery_receiver);
       
       if (wifi != null)
       {  
         unregisterReceiver(wifi_receiver);
         unregisterReceiver(dhcp_receiver);
       }
       super.onDestroy();
     }
       
     protected void reset_timer()
     {
       RunTimer.reset();
       cfg.save_time();
       cfg.pause_time = cfg.start_time ;
       cfg.resume_time = 0;
       coord_buf.reset(true);
       timer_state = TimerState.INITIAL;
       
       if (RunTimer.get_run_info(run_info))
         update_run_info(true);
       
       post_pace(0);
       post_dist(0.0, total_dist_tv);
       post_dist(0.0, split_dist_tv);
       post_dist(0.0, leg_dist_tv);
       suspend_timer_display();
       stop_gps();
       dist_update_ts = 0;
       coord_buf.debug_log("Timer reset");
       coord_buf.close_data_file();
     }     
 
 
     @Override
     public void onRestoreInstanceState(Bundle b) 
     {
       super.onRestoreInstanceState(b);
     }  
     
     @Override
     public void onSaveInstanceState(Bundle b) 
     {
       super.onSaveInstanceState(b);  
     }
 
 
     protected void start_timer()
     {
        RunTimer.start();
        cfg.save_time();
        cfg.resume_time = cfg.start_time = get_start_time();
        cfg.pause_time = 0;
        timer_state = TimerState.RUNNING;
        
        if (!coord_buf.open_data_file())
          update_status("Error opening GPS data file, cannot save data");
        
        coord_buf.debug_log("Timer started");
        resume_timer_display();
    }   
     
     void post_pace(int pace)
     {
       int min = pace / 60;
       int sec = pace % 60;
       pace_tv.setText(String.format("%02d:%02d", min, sec));
     }
     
     public void show_dist_info(DistInfo di)
     {
       post_dist(di.dist,total_dist_tv);
       post_pace((int)di.pace_t/1000);
       update_status("GPS: " + di.get_status_str());
     }
         
     void post_dist(double total_dist, TextView tv)
     {
       tv.setText(String.format("%.3f", total_dist));
     }
     
     enum PrecType { TOTAL,LEG,SPLIT};
     
     void post_time(long ts, TextView tv,PrecType prec_type)
     {
          long ss = ts/1000;
          long mm = ss / 60;
          long hh = mm / 60;
          long fract = (ts/100) % 10;
          ss = ss % 60;
          mm = mm % 60;
          String t = null;
          
          switch (prec_type)
          {
            case LEG:
              t = String.format("%02d:%02d:%02d.%d",
                      hh, mm, ss, fract);
              break;
            case TOTAL:  
              t = String.format("%d:%02d:%02d", hh, mm, ss);
              break;        
            case SPLIT:  
              t = String.format("%02d:%02d.%d",  mm, ss, fract);
              break;        
          }
          
          if (t != null)
            tv.setText(t);
            
     }
     
     public static long running_time(ConfigState cfg)
     {
       //return time_now() - cfg.start_time;
       return RunTimer.now();
     }
     
     public static long time_now()
     {
       return SystemClock.elapsedRealtime();
     }
   
     protected void split_timer()
     {
       coord_buf.sync_dist_info(dist_info,RunTimer.now(),false);
       if (RunTimer.get_run_info(run_info))
       {  
          update_run_info(true);
       }  
 
       RunTimer.split(dist_info.dist);
     }
     
     protected void start_leg()
     {
       coord_buf.sync_dist_info(dist_info,RunTimer.now(),false);
       if (RunTimer.get_run_info(run_info))
       {  
          update_run_info(true);
       }  
       
       RunTimer.resume();
       RunTimer.start_leg(dist_info.dist);
       timer_state = TimerState.RUNNING;
       resume_timer_display();
     }
     
     protected void resume_timer()
     {
       RunTimer.resume();
       cfg.save_time();
       cfg.resume_time = get_start_time();
       cfg.start_time += cfg.resume_time - cfg.pause_time;
       cfg.pause_time = 0;
       timer_state = TimerState.RUNNING;
       resume_timer_display();
       coord_buf.debug_log("Timer resumed");
     }     
    
     protected void resume_timer_display()
     {
        timer_h.removeCallbacks(update_time_task);
        timer_h.postDelayed(update_time_task,100);
     }
     
     public void handle_select_run(String workout)
     {
       String review_text = RunTimer.get_review_info(cfg.data_dir + "/", workout);
 
       if (review_tv != null)
       {
         if (review_text == null)
           review_text = "Error retrieving workout info";
 
         review_tv.setText(review_text);
       }
     }
 }
