 package com.howfun.android.antguide.game;
 
 import android.os.Handler;
 import android.os.Message;
 
 import com.howfun.android.antguide.utils.Utils;
 
 public class TimeManager {
 
    private static final String TAG = "TimeManager";
    private static final int SECOND = 60;
    private int time;
 
    private int minute;
    private int second;
 
    private boolean toRun;
    private boolean flag;
 
    private Handler handler;
 
    private Thread thread = new Thread() {
       public void run() {
          while (toRun) {
             if (flag) {
                try {
                   Message msg = new Message();
                   msg.what = Utils.MSG_TIME_UPDATED;
                   msg.obj = timeStr();
                   handler.sendMessage(msg);
                   Thread.sleep(1000);
                   add();
                   Utils.log(TAG, "add time");
                } catch (InterruptedException e) {
                   e.printStackTrace();
                }
             }
          }
       }
    };
 
    public TimeManager(Handler h) {
       Utils.log(TAG, "new timemanger..............ljjljlj");
       
       handler = h;
       toRun = true;
       time = 0;
       minute = 0;
       second = 0;
       thread.start();
    }
 
    public void start() {
       flag = true;
    }
 
    public void resume() {
       flag = true;
    }
    
   public void restoreTime(int time) {
      this.time = time;
   }
   
   public int getTime() {
      return this.time;
    }
    
    public int getMin() {
       return this.minute;
    }
    
    public int getSec() {
       return this.second;
    }
 
    public void pause() {
       flag = false;
    }
 
    public void stop() {
       flag = false;
    }
 
    public void kill() {
       toRun = false;
    }
 
    public int getMinute() {
 
       return minute;
    }
 
    public int getSecond() {
       return second;
    }
 
    public void reset() {
      Utils.log(TAG, "timer reset");
       time = 0;
       minute = 0;
       second = 0;
    }
 
    /**
     * add one second
     */
    private void add() {
       time++;
       minute = time / SECOND;
       second = time % SECOND;
    }
 
    private String timeStr() {
       String timeStr = "";
       String min = "";
       String sec = "";
       if (minute < 10) {
          min = "0" + String.valueOf(minute);
       } else {
          min = String.valueOf(minute);
       }
 
       if (second < 10) {
          sec = "0" + String.valueOf(second);
       } else {
          sec = String.valueOf(second);
       }
       timeStr = min + sec;
       return timeStr;
    }
 }
