 package com.tools.tvguide.activities;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.tools.tvguide.managers.AppEngine;
 import com.tools.tvguide.managers.ContentManager;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.util.Pair;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.SimpleAdapter.ViewBinder;
 
 public class ChannelDetailActivity extends Activity 
 {
     private static final String TAG = "ChannelDetailActivity";
     private List<Pair<Integer, Button>> mDaysBtnList = new ArrayList<Pair<Integer,Button>>();
     private int mCurrentSelectedDay;
     private ListView mListView;
     private SimpleAdapter mListViewAdapter;
     private ArrayList<HashMap<String, Object>> mItemList;
     private List<HashMap<String, String>> mProgramList;             // Key: time, title
     private String mChannelId;
     private String mChannelName;
     private List<HashMap<String, String>> mOnPlayingProgram;        // Key: time, title
     private String SEPERATOR                                = ": ";
     private final int MSG_REFRESH_PROGRAM_LIST              = 0;
     private final int MSG_REFRESH_ON_PLAYING_PROGRAM        = 1;
     
     class MySimpleAdapter extends SimpleAdapter
     {
         public MySimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) 
         {
             super(context, data, resource, from, to);
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) 
         {
             String onPlayingProgram = "";
             if (mOnPlayingProgram.size() > 0)
             {
                 onPlayingProgram = mOnPlayingProgram.get(0).get("time") + SEPERATOR + mOnPlayingProgram.get(0).get("title");
             }
             View view = super.getView(position, convertView, parent);
             if (view instanceof RelativeLayout)
             {
                 TextView textView = (TextView) view.findViewById(R.id.detail_item_program);
                 if (((String)(mItemList.get(position).get("program"))).equals(onPlayingProgram) 
                         && (mCurrentSelectedDay == Calendar.getInstance().get(Calendar.DAY_OF_WEEK)))
                 {
                     textView.setTextColor(Color.RED);
                 }
                 else
                 {
                     textView.setTextColor(Color.BLACK);
                 }
             }
             else if (view instanceof ImageView)
             {
                 ImageView iv = (ImageView)view;
                 iv.setImageDrawable(getResources().getDrawable(R.drawable.icon_arrow_2));
             }
             return view;
         }
     }
     
     class MyViewBinder implements ViewBinder
     {
         public boolean setViewValue(View view, Object data, String textRepresentation)
         {
             if((view instanceof ImageView) && (data instanceof Bitmap))
             {
                 ImageView iv = (ImageView)view;
                 Bitmap bm = (Bitmap)data;
                 iv.setImageBitmap(bm);
                 return true;
             }
             return false;
         }
     }
     
     @Override
     protected void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_channel_detail);
         
         mChannelId = getIntent().getStringExtra("id");
         mChannelName = getIntent().getStringExtra("name");
         ((TextView)findViewById(R.id.title)).setText(mChannelName);
         mProgramList = new ArrayList<HashMap<String,String>>();
         mItemList = new ArrayList<HashMap<String, Object>>();
         mOnPlayingProgram = new ArrayList<HashMap<String,String>>();
         
         initViews();
         createAndSetListViewAdapter();
         updateProgramList();
         updateOnplayingProgram();
         
         mListView.setOnItemClickListener(new OnItemClickListener() 
         {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, final int position, long id) 
             {
                 final String time = mProgramList.get(position).get("time");
                 final String title = mProgramList.get(position).get("title");
                 final String program = time + SEPERATOR + title;
                 
                 String hour = time.split(":")[0];
                 String minute = time.split(":")[1];
                 final Calendar calendar = Calendar.getInstance();
                 calendar.setTimeInMillis(System.currentTimeMillis());
                 calendar.set(Calendar.DAY_OF_WEEK, mCurrentSelectedDay);
                 calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
                 calendar.set(Calendar.MINUTE, Integer.parseInt(minute));
                 calendar.set(Calendar.SECOND, 0);
                 calendar.set(Calendar.MILLISECOND, 0);
                 
                // 因为周日算一周的第一天，所以这里要做特殊处理。如果使用API setFirstDayOfWeek, 则在月初时会设置到上月的末尾，故不用该API
                if (mCurrentSelectedDay == Calendar.SUNDAY)
                {
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                    calendar.setTimeInMillis(calendar.getTimeInMillis() + 60*60*24*1000);       // 周六再增加一天时间
                }
                
                 // Choose earlier than now time
                 if (calendar.getTimeInMillis() < System.currentTimeMillis())
                 {
                     AlertDialog dialog = new AlertDialog.Builder(ChannelDetailActivity.this)
                         .setTitle(getResources().getString(R.string.tips))
                         .setMessage(getResources().getString(R.string.alarm_tips_cannot_set))
                         .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() 
                         {
                             @Override
                             public void onClick(DialogInterface dialog, int which) 
                             {
                                 dialog.dismiss();
                             }
                         })
                         .create();
                     dialog.show();
                     return;
                 }
                 
                 List<String> alarmList = new ArrayList<String>();
                 alarmList.add(getResources().getString(R.string.m1_alarm));
                 alarmList.add(getResources().getString(R.string.m5_alarm));
                 alarmList.add(getResources().getString(R.string.m10_alarm));
                 String alarmTimeString[] = (String[]) alarmList.toArray(new String[0]);
                 
                 long alarmTime = AppEngine.getInstance().getAlarmHelper().getAlarmTimeAtMillis(mChannelId, mChannelName, program);
                 int choice = -1;
                 // Hash already set the alarm clock
                 if (alarmTime > 0)
                 {
                     long distance = calendar.getTimeInMillis() - alarmTime;
                     int aheadSetMinute = (int)(distance / 1000 / 60);
                     switch (aheadSetMinute)
                     {
                         case 1:
                             choice = 0;
                             break;
                         case 5:
                             choice = 1;
                             break;
                         case 10:
                             choice = 2;
                             break;
                     }
                 }
                 
                 Dialog alertDialog = new AlertDialog.Builder(ChannelDetailActivity.this)
                         .setTitle(getResources().getString(R.string.alarm_tips))
                         .setSingleChoiceItems(alarmTimeString, choice, new DialogInterface.OnClickListener() 
                         {
                             @Override
                             public void onClick(DialogInterface dialog, int which) 
                             {
                                 int aheadMinute = 0;
                                 switch (which)
                                 {
                                     case 0:
                                         aheadMinute = 1;
                                         break;
                                     case 1:
                                         aheadMinute = 5;
                                         break;
                                     case 2:
                                         aheadMinute = 10;
                                         break;
                                 }
                                 
                                 // Try to remove the alarm first
                                 AppEngine.getInstance().getAlarmHelper().removeAlarm(mChannelId, mChannelName, program);
                                 
                                 // Set alarm clock
                                 long alarmTimeInMillis = calendar.getTimeInMillis() - aheadMinute * 60 * 1000;
                                 AppEngine.getInstance().getAlarmHelper().addAlarm(mChannelId, mChannelName, program, alarmTimeInMillis);
                                 if (alarmTimeInMillis < System.currentTimeMillis())   // The clock will sounds right now
                                 {
                                     mItemList.get(position).put("arrow", BitmapFactory.decodeResource(getResources(), R.drawable.icon_arrow_2));
                                 }
                                 else 
                                 {
                                     mItemList.get(position).put("arrow", BitmapFactory.decodeResource(getResources(), R.drawable.clock));
                                     Toast.makeText(ChannelDetailActivity.this, getResources().getString(R.string.alarm_tips_set), Toast.LENGTH_SHORT).show();
                                 }
                                 mListViewAdapter.notifyDataSetChanged();
                                 dialog.dismiss();
                             }
                         })
                         .setNegativeButton(getResources().getString(R.string.cancel_alarm), new DialogInterface.OnClickListener()
                         {
                             @Override
                             public void onClick(DialogInterface dialog, int which) 
                             {
                                 AppEngine.getInstance().getAlarmHelper().removeAlarm(mChannelId, mChannelName, program);
                                 mItemList.get(position).put("arrow", BitmapFactory.decodeResource(getResources(), R.drawable.icon_arrow_2));
                                 Toast.makeText(ChannelDetailActivity.this, getResources().getString(R.string.alarm_tips_cancel), Toast.LENGTH_SHORT).show();
                                 mListViewAdapter.notifyDataSetChanged();
                             }
                             
                         })
                         .create();
                 alertDialog.show();
             }
         });
         
         Toast.makeText(ChannelDetailActivity.this, getResources().getString(R.string.alarm_tips_can_set), Toast.LENGTH_LONG).show();
     }
     
     @Override
     public void onNewIntent (Intent intent)
     {
         setIntent(intent);
     }
     
     public void initViews()
     {
         mDaysBtnList.add(new Pair<Integer, Button>(Calendar.MONDAY, (Button)findViewById(R.id.Mon)));
         mDaysBtnList.add(new Pair<Integer, Button>(Calendar.TUESDAY, (Button)findViewById(R.id.Tue)));
         mDaysBtnList.add(new Pair<Integer, Button>(Calendar.WEDNESDAY, (Button)findViewById(R.id.Wed)));
         mDaysBtnList.add(new Pair<Integer, Button>(Calendar.THURSDAY, (Button)findViewById(R.id.Thu)));
         mDaysBtnList.add(new Pair<Integer, Button>(Calendar.FRIDAY, (Button)findViewById(R.id.Fri)));
         mDaysBtnList.add(new Pair<Integer, Button>(Calendar.SATURDAY, (Button)findViewById(R.id.Sat)));
         mDaysBtnList.add(new Pair<Integer, Button>(Calendar.SUNDAY, (Button)findViewById(R.id.Sun)));
         
         for (int i=0; i<mDaysBtnList.size(); ++i)
         {
             if (mDaysBtnList.get(i).first.intValue() == Calendar.getInstance().get(Calendar.DAY_OF_WEEK))
             {
                 mDaysBtnList.get(i).second.setBackgroundResource(R.drawable.text_bg2);
                 mCurrentSelectedDay = mDaysBtnList.get(i).first.intValue();
             }
         }
         
         mListView = (ListView)findViewById(R.id.detail_listview);
     }
     
     private void createAndSetListViewAdapter()
     {
         mListViewAdapter = new MySimpleAdapter(ChannelDetailActivity.this, mItemList, R.layout.channeldetail_item,
                 new String[]{"program", "arrow"},
                 new int[]{R.id.detail_item_program, R.id.detail_item_arrow});
         mListViewAdapter.setViewBinder(new MyViewBinder());
         mListView.setAdapter(mListViewAdapter);
     }
 
     private void updateProgramList()
     {
         mProgramList.clear();
         AppEngine.getInstance().getContentManager().loadProgramsByChannel(mChannelId, getHostDay(mCurrentSelectedDay), mProgramList, new ContentManager.LoadListener() 
         {    
             @Override
             public void onLoadFinish(int status) 
             {
                 uiHandler.sendEmptyMessage(MSG_REFRESH_PROGRAM_LIST);
             }
         });
     }
     
     private void updateOnplayingProgram()
     {
         mOnPlayingProgram.clear();
         AppEngine.getInstance().getContentManager().loadOnPlayingProgramByChannel(mChannelId, mOnPlayingProgram, new ContentManager.LoadListener() 
         {    
             @Override
             public void onLoadFinish(int status) 
             {
                 uiHandler.sendEmptyMessage(MSG_REFRESH_ON_PLAYING_PROGRAM);
             }
         });
     }
 
     @Override
     public void onBackPressed() 
     {
         finish();
     };
     
     public void back(View view)
     {
         if (view instanceof Button)
         {
             // The same effect with press back key
             finish();
         }
     }
     
     public void collectChannel(View view)
     {
         if (view instanceof Button)
         {
             HashMap<String, Object> info = new HashMap<String, Object>();
             info.put("name", mChannelName);
             AppEngine.getInstance().getCollectManager().addCollectChannel(mChannelId, info);
             Toast.makeText(this, R.string.collect_success, Toast.LENGTH_SHORT).show();
         }
     }
     
     public void selectDay(View view)
     {
         for (int i=0; i<mDaysBtnList.size(); ++i)
         {
             if (mDaysBtnList.get(i).second.getId() == view.getId())
             {
                 mDaysBtnList.get(i).second.setBackgroundResource(R.drawable.text_bg2);
                 mCurrentSelectedDay = mDaysBtnList.get(i).first.intValue();
                 updateProgramList();
             }
             else
             {
                 mDaysBtnList.get(i).second.setBackgroundResource(R.drawable.text_bg);
             }
         }
     }
     
     /*
      * Trasfer the day to the server host day: Monday~Sunday -> 1~7
      */
     private int getHostDay(int day)
     {
         assert(day >=1 && day <=7);
         int hostDay = 0;
         switch (day)
         {
             case Calendar.MONDAY:
                 hostDay = 1;
                 break;
             case Calendar.TUESDAY:
                 hostDay = 2;
                 break;
             case Calendar.WEDNESDAY:
                 hostDay = 3;
                 break;
             case Calendar.THURSDAY:
                 hostDay = 4;
                 break;
             case Calendar.FRIDAY:
                 hostDay = 5;
                 break;
             case Calendar.SATURDAY:
                 hostDay = 6;
                 break;
             case Calendar.SUNDAY:
                 hostDay = 7;
                 break;
         }
         return hostDay;
     }
     
     private Handler uiHandler = new Handler()
     {
         public void handleMessage(Message msg)
         {
             super.handleMessage(msg);
             switch (msg.what) 
             {
                 case MSG_REFRESH_PROGRAM_LIST:
                     mItemList.clear();
                     for (int i=0; i<mProgramList.size(); ++i)
                     {
                         HashMap<String, Object> item = new HashMap<String, Object>();
                         String program = mProgramList.get(i).get("time") + SEPERATOR + mProgramList.get(i).get("title");
                         item.put("program", program);
                         if (AppEngine.getInstance().getAlarmHelper().isAlarmSet(mChannelId, mChannelName, program))
                             item.put("arrow", BitmapFactory.decodeResource(getResources(), R.drawable.clock));
                         else
                             item.put("arrow", BitmapFactory.decodeResource(getResources(), R.drawable.icon_arrow_2));
                         mItemList.add(item);
                     }
                     mListViewAdapter.notifyDataSetChanged();
                     break;
                 case MSG_REFRESH_ON_PLAYING_PROGRAM:
                     mListViewAdapter.notifyDataSetChanged();
                     String onPlayingProgram = "";
                     if (mOnPlayingProgram.size() > 0)
                     {
                         onPlayingProgram = mOnPlayingProgram.get(0).get("time") + SEPERATOR + mOnPlayingProgram.get(0).get("title");
                     }
                     for (int i=0; i<mItemList.size(); ++i)
                     {
                         if (((String)(mItemList.get(i).get("program"))).equals(onPlayingProgram))
                         {
                             mListView.setSelection(i);
                         }
                     }
                     break;
                 default:
                     break;
             }
             
         }
     };
 }
