 /*
  * VerdictRequest.java
  * Copyright (C) 2013  Crs Chin <crs.chin@gmail.com>
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330,
  * Boston, MA 02111-1307, USA.
  */
 
 package com.android.avender;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.app.ActivityManager.RunningAppProcessInfo;
 import android.app.NotificationManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.view.LayoutInflater;
 import android.view.ViewGroup;
 import android.view.View;
 import android.view.Menu;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.ListView;
 import android.widget.CheckBox;
 import android.widget.Button;
import android.widget.Toast;
 import android.graphics.drawable.Drawable;
 import android.util.Log;
 
 import com.javender.Javender;
 
 public class VerdictRequest extends Activity {
 
     private final String LOG_TAG = "VERDICTREQ";
 
     private final int NOTIFICATION_VERDICT = 19871127;
 
     private IAvenderService avenderService = null;
     private boolean mVerdicted = false;
     private boolean mAlways = false;
     private int mVerdict = Javender.VERDICT_NONE;
 
     private VerdictInfo mVerdictInfo = null;
 
     private List<Map<String, Object>> mObjsDat = null;
 
     private Button mAllow = null;
     private Button mDeny = null;
     private CheckBox mCheck = null;
     private ListView mObjs = null;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.verdict_request);
 
         mObjsDat = new ArrayList<Map<String, Object>>();
         mAllow = (Button)findViewById(R.id.button_allow);
         mDeny = (Button)findViewById(R.id.button_deny);
         mCheck = (CheckBox)findViewById(R.id.not_again);
         mObjs = (ListView)findViewById(R.id.verdict_objs);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         //getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     private synchronized void verdictDone()  {
         if(! mVerdicted)  {
             if(avenderService != null && mVerdictInfo != null)  {
                 Log.i(LOG_TAG, "send verdict result " + mVerdict);
                 try {
                     avenderService.setVerdict(mVerdictInfo.rid, mVerdict);
                 } catch (RemoteException e)  {
                     e.printStackTrace();
                 }
             }
             mVerdicted = true;
             finish();
         }
     }
 
     private void populateObjs()  {
         PackageManager pm = getPackageManager();
         ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
 
         HashMap<String, Object> map;
         List<RunningAppProcessInfo> procs;
         List<ApplicationInfo> apps;
         Drawable icon;
         String prog, procName;
         int i;
 
         if(mVerdictInfo != null)  {
             for(i = 0; i < mObjsDat.size(); i++)  {
                 mObjsDat.remove(i);
             }
 
             procs = am.getRunningAppProcesses();
             apps = pm.getInstalledApplications(0);
 
             for(i = 0; i < mVerdictInfo.pid.length; i++)  {
                 prog = mVerdictInfo.exe[i];
                 procName = null;
                 icon = null;
 
                 map = new HashMap<String, Object>();
                 for(RunningAppProcessInfo info : procs)  {
                     if(mVerdictInfo.pid[i] == info.pid)  {
                         if(info.processName != null)  {
                             procName = info.processName;
                             for(ApplicationInfo app : apps)  {
                                 if(procName.equals(app.processName))  {
                                     icon = app.loadIcon(pm);
                                     break;
                                 }
                             }
                         }
                         break;
                     }
                 }
                 if(icon == null)
                     icon = pm.getDefaultActivityIcon();
                 map.put("icon", icon);
                 if(procName != null && ! procName.equals(""))
                     prog += "\n" + procName;
                 map.put("prog", prog);
                 map.put("user", "UID:" + mVerdictInfo.uid[i]);
                 map.put("pid", "PID:" + mVerdictInfo.pid[i]);
                 mObjsDat.add(map);
             }
             mObjs.setAdapter(new ObjAdapter(this));
         }
     }
 
     private void updateView()  {
         if(avenderService == null)  {
             mAllow.setEnabled(false);
             mDeny.setEnabled(false);
             mObjs.setEnabled(false);
         }else  {
             mAllow.setEnabled(true);
             mDeny.setEnabled(true);
             mObjs.setEnabled(true);
         }
         populateObjs();
     }
 
     @Override
     protected void onStart()  {
         super.onStart();
         bindService(new Intent(this, AvenderService.class), sc, Context.BIND_AUTO_CREATE);
 
         NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
         nm.cancel(NOTIFICATION_VERDICT);
     }
 
     @Override
     protected void onResume()  {
         super.onResume();
         updateView();
     }
 
     @Override
     protected void onPause()  {
         super.onPause();
         verdictDone();
     }
 
     public void onNotAgainClick(View view)  {
         mAlways = ((CheckBox)view).isChecked();
     }
 
     public void onAllowClick(View view)  {
         mVerdict = mAlways ? Javender.VERDICT_ALLOW_ALWAYS : Javender.VERDICT_ALLOW_ONCE;
         verdictDone();
     }
 
     public void onDenyClick(View view)  {
         mVerdict = mAlways ? Javender.VERDICT_DENY_ALWAYS : Javender.VERDICT_DENY_ONCE;
         verdictDone();
     }
 
     public void onKillClick(View view)  {
         mVerdict = mAlways ? Javender.VERDICT_KILL_ALWAYS : Javender.VERDICT_KILL_ONCE;
         verdictDone();
     }
 
     private void printVerdictInfo(VerdictInfo info)
     {
         int i;
 
         Log.i(LOG_TAG, "VERDICT INFO:");
         for(i = 0; i < info.pid.length; i++)
             Log.i(LOG_TAG, "  " + i + ". PID:" + info.pid[i] + ", UID:" + info.uid[i]
                   + ", EXE:\"" + info.exe[i] + "\"");
     }
 
     private ServiceConnection sc = new ServiceConnection() {
             @Override
             public void onServiceConnected(ComponentName name, IBinder service) {
                 Log.i(LOG_TAG, "AvenderService connected up");
                 avenderService = IAvenderService.Stub.asInterface(service);
                 try {
                     mVerdictInfo = avenderService.getVerdict();
                    if(mVerdictInfo == null)  {
                        Toast.makeText(getApplicationContext(), "Verdict expired", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                     printVerdictInfo(mVerdictInfo);
                 } catch (RemoteException e)  {
                     e.printStackTrace();
                 }
                 updateView();
             }
 
             @Override
             public void onServiceDisconnected(ComponentName name) {
                 Log.i(LOG_TAG, "AvenderService disconnected");
                 avenderService = null;
                 updateView();
             }
         };
 
 
     public final class ViewHolder{
         public ImageView icon;
         public TextView prog;
         public TextView user;
         public TextView pid;
     }
 
     public class ObjAdapter extends BaseAdapter{
         private LayoutInflater mInflater;
 
         public ObjAdapter(Context context){
             this.mInflater = LayoutInflater.from(context);
         }
 
         @Override
         public int getCount() {
             return mObjsDat.size();
         }
 
         @Override
         public Object getItem(int arg0) {
             return null;
         }
 
         @Override
         public long getItemId(int arg0) {
             return 0;
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             ViewHolder holder = null;
             if (convertView == null) {
                 holder = new ViewHolder();
                 convertView = mInflater.inflate(R.layout.verdict_item, null);
                 holder.icon = (ImageView)convertView.findViewById(R.id.icon);
                 holder.prog = (TextView)convertView.findViewById(R.id.prog);
                 holder.user = (TextView)convertView.findViewById(R.id.user);
                 holder.pid = (TextView)convertView.findViewById(R.id.pid);
                 convertView.setTag(holder);
             }else {
                 holder = (ViewHolder)convertView.getTag();
             }
 
             Drawable icon = (Drawable)mObjsDat.get(position).get("icon");
 
             if(icon != null)
                 holder.icon.setImageDrawable(icon);
             holder.prog.setText((String)mObjsDat.get(position).get("prog"));
             holder.user.setText((String)mObjsDat.get(position).get("user"));
             holder.pid.setText((String)mObjsDat.get(position).get("pid"));
             return convertView;
         }
     }
 }
 
 
