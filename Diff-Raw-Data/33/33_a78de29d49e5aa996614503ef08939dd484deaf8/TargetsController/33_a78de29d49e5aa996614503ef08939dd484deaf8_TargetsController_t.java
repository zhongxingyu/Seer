 /*
  * Copyright 2012 Tomas Joelsson
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.tnc.android.graphite.controllers;
 
 
 import java.util.ArrayList;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.preference.PreferenceManager;
 import com.tnc.android.graphite.GraphiteApp;
 import com.tnc.android.graphite.daos.TargetDao;
 import com.tnc.android.graphite.models.Target;
 import com.tnc.android.graphite.utils.GraphiteConnection;
 
 
 public class TargetsController extends Controller
   implements OnSharedPreferenceChangeListener
 {
   @SuppressWarnings("unused")
   private static final String TAG=TargetsController.class.getSimpleName();
   private HandlerThread workerThread;
   private Handler workerHandler;
 
   public static final int MESSAGE_GET_TARGETS=1;
   public static final int MESSAGE_MODEL_NEW=2;
   public static final int MESSAGE_MODEL_UPDATED=3;
   public static final int MESSAGE_RELOAD_TARGETS=4;
   public static final int MESSAGE_UPDATE_ENABLED=5;
   public static final int MESSAGE_START_LOADING=6;
   public static final int MESSAGE_STOP_LOADING=7;
   public static final int MESSAGE_FAIL_GO_BACK=8;
   public static final int MESSAGE_FAIL_STAY=9;
 
   private String serverUrl;
   private String targetFilter;
 
   private ArrayList<Target> model;
 
   public ArrayList<Target> getModel()
   {
     return model;
   }
 
   public TargetsController(ArrayList<Target> model)
   {
     this.model=model;
     workerThread=new HandlerThread("Targets Worker Thread");
     workerThread.start();
     workerHandler=new Handler(workerThread.getLooper());
 
     setPrefs();
   }
 
   @Override
   public void dispose()
   {
     super.dispose();
     workerThread.getLooper().quit();
 
     SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(
       GraphiteApp.getContext());
     prefs.unregisterOnSharedPreferenceChangeListener(this);
   }
 
   @Override
   public boolean handleMessage(int what, Object data)
   {
     switch(what)
     {
       case MESSAGE_GET_TARGETS:
         if(null!=data)
         {
           getTargets((Target)data);
         }
         else
         {
           getTargets();
         }
         return true;
       case MESSAGE_RELOAD_TARGETS:
         reloadTargets();
         return true;
       case MESSAGE_UPDATE_ENABLED:
         updateEnabled((Integer)data);
         return true;
     }
     return false;
   }
 
   private void updateEnabled(final int id)
   {
     workerHandler.post(new Runnable() {
       @Override
       public void run()
       {
         synchronized(model)
         {
           TargetDao dao=new TargetDao();
           dao.update(model.get(id)); // not same as Target ID value
         }
       }
     });
   }
 
   private void getTargets()
   {
     getTargets(null);
   }
 
   private void getTargets(final Target baseTarget)
   {
    if(null!=baseTarget)
    {
      notifyOutboxHandlers(MESSAGE_START_LOADING, 0, 0, null);
      workerHandler.post(new Runnable() {
        @Override
        public void run()
         {
           // Load children of baseTarget
           ArrayList<Target> targets;
           try
           {
             targets=GraphiteConnection.getTargets(
               serverUrl,
               baseTarget.getName()+".*",
               1
               );
           }
           catch(Exception e)
           {
             e.printStackTrace();
             notifyOutboxHandlers(MESSAGE_STOP_LOADING, 0, 0, null);
             notifyOutboxHandlers(MESSAGE_FAIL_STAY, 0, 0, e);
             return;
           }
           synchronized(model)
           {
             int idx=model.indexOf(baseTarget);
             model.get(idx).setLoaded(true);
             idx++;
             for(int i=0; i<targets.size(); ++i)
             {
               model.add(idx+i, targets.get(i));
             }
            persistModel();
           }
           notifyOutboxHandlers(MESSAGE_MODEL_UPDATED, targets.size(), 0,
             baseTarget);
           notifyOutboxHandlers(MESSAGE_STOP_LOADING, 0, 0, null);
         }
      });
      return;
    }
    workerHandler.post(new Runnable() {
      @Override
      public void run()
      {
         TargetDao dao=new TargetDao();
         ArrayList<Target> targets=dao.getAll();
         String hash=String.valueOf((serverUrl+targetFilter).hashCode());
         if(0<targets.size()&&targets.get(0).getHash().equals(hash))
         {
           synchronized(model)
           {
             while(model.size()>0)
             {
               model.remove(0);
             }
             for(Target target : targets)
             {
               model.add(target);
             }
           }
          notifyOutboxHandlers(MESSAGE_MODEL_NEW, 0, 0, null);
         }
         else
         {
           // Force reload
           reloadTargets();
         }
       }
     });
   }
 
   private void reloadTargets()
   {
     notifyOutboxHandlers(MESSAGE_START_LOADING, 0, 0, null);
     workerHandler.post(new Runnable() {
       @Override
       public void run()
       {
         ArrayList<Target> targets;
         try
         {
           targets=GraphiteConnection.getTargets(
             serverUrl,
             targetFilter,
             1 // TODO make configurable
           );
         }
         catch(Exception e)
         {
           e.printStackTrace();
           notifyOutboxHandlers(MESSAGE_STOP_LOADING, 0, 0, null);
           notifyOutboxHandlers(MESSAGE_FAIL_GO_BACK, 0, 0, e);
           return;
         }
         synchronized(model)
         {
           while(model.size()>0)
           {
             model.remove(0);
           }
           for(Target target : targets)
           {
             model.add(target);
           }
           persistModel();
         }
         notifyOutboxHandlers(MESSAGE_STOP_LOADING, 0, 0, null);
         notifyOutboxHandlers(MESSAGE_MODEL_NEW, 0, 0, null);
       }
     });
   }
 
   @Override
   public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
     String key)
   {
     setPrefs();
   }
 
   private void setPrefs()
   {
     SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(
       GraphiteApp.getContext());
     prefs.registerOnSharedPreferenceChangeListener(this);
     serverUrl=prefs.getString("server_url", "");
     targetFilter=prefs.getString("target_filter", "*");
   }
 
   private void persistModel()
   {
     TargetDao dao=new TargetDao();
     dao.deleteAll();
     for(Target target : model)
     {
       target.setId(-1);
       long id=dao.insert(target);
       target.setId((int)id);
     }
   }
 }
