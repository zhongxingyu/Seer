 package com.gmail.altakey.mint;
 
 import android.app.Service;
 import android.app.IntentService;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Binder;
 import android.os.IBinder;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonArray;
 import com.google.gson.reflect.TypeToken;
 
 import java.util.List;
 import java.util.LinkedList;
 import java.util.Date;
 import java.io.IOException;
 
 public class ToodledoClientService extends IntentService {
     public static final String ACTION_REAUTH = "com.gmail.altakey.mint.REAUTH";
     public static final String ACTION_COMMIT = "com.gmail.altakey.mint.COMMIT";
     public static final String ACTION_COMPLETE = "com.gmail.altakey.mint.COMPLETE";
     public static final String ACTION_UPDATE = "com.gmail.altakey.mint.UPDATE";
     public static final String ACTION_ADD = "com.gmail.altakey.mint.ADD";
 
     public static final String EXTRA_TASKS = "tasks";
     public static final String EXTRA_TASK_FIELDS = "task_fields";
 
     private NetworkTask mNetworkTask;
     private DB mDB;
     private ToodledoClient mClient;
 
     public ToodledoClientService() {
         super("ToodledoClientService");
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
         mDB = new DB(this);
         mClient = new ToodledoClient(Authenticator.create(this), this);
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         mDB.close();
     }
 
    public static String asListOfTasks(Task... tasks) {
        return getGson().toJson(tasks, Task[].class);
     }
 
     @Override
     protected void onHandleIntent(Intent intent) {
         final String action = intent.getAction();
 
         try {
             mDB.open();
 
             try {
                 if (ACTION_REAUTH.equals(action)) {
                     reauth();
                 } else if (ACTION_UPDATE.equals(action)) {
                     update();
                 } else {
                     final List<Task> tasks = getGson().fromJson(intent.getStringExtra(EXTRA_TASKS), new TypeToken<LinkedList<Task>>(){}.getType());
 
                     if (ACTION_COMPLETE.equals(action)) {
                         complete(tasks);
                     } else {
                         final String[] fields = intent.getStringArrayExtra(EXTRA_TASK_FIELDS);
 
                         if (ACTION_COMMIT.equals(action)) {
                             commit(tasks, fields);
                         } else if (ACTION_ADD.equals(action)) {
                             add(tasks, fields);
                         }
                     }
                 }
             } catch (IOException e) {
             } catch (Authenticator.Exception e) {
             }
         } finally {
             mDB.close();
         }
     }
 
     private static Gson getGson() {
         GsonBuilder builder = new GsonBuilder();
         builder.registerTypeAdapter(TaskStatus.class, new TaskStatus.JsonAdapter());
         builder.registerTypeAdapter(TaskFolder.class, new TaskFolder.JsonAdapter());
         builder.registerTypeAdapter(Task.class, new Task.JsonAdapter());
         builder.registerTypeAdapter(TaskContext.class, new TaskContext.JsonAdapter());
         return builder.create();
     }
 
     private void reauth() {
         mClient.setAuthenticator(Authenticator.create(ToodledoClientService.this));
     }
 
     private void commit(List<Task> tasks, String[] fields) throws IOException, Authenticator.Exception {
         mClient.commitTasks(tasks, fields);
     }
 
     private void complete(List<Task> tasks) throws IOException, Authenticator.Exception {
         final long now = new Date().getTime();
         for (Task t : tasks) {
             t.completed = now / 1000;
         }
         mClient.commitTasks(tasks, null);
     }
 
     private void add(List<Task> tasks, String[] fields) throws IOException, Authenticator.Exception {
         mClient.addTasks(tasks, fields);
     }
 
     private void update() throws IOException, Authenticator.Exception {
         mDB.update(mClient);
     }
 }
