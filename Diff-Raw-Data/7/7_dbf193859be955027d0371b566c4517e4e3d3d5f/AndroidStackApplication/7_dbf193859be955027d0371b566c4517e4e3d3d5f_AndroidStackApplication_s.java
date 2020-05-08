 package com.praeses.androidstack;
 
 import android.app.Application;
 
 import com.praeses.androidstack.di.EventBusModule;
 import com.praeses.androidstack.di.HelloModule;
 
 import dagger.ObjectGraph;
 
 public class AndroidStackApplication extends Application {
 
     private ObjectGraph graph;
 
     @Override
     public void onCreate() {
         super.onCreate();
        EventBusModule events = new EventBusModule();
        HelloModule hello = new HelloModule();
 
         graph = ObjectGraph.create(
                new HelloModule(),
                new EventBusModule()
         );
     }
 
     public void inject(Object o) {
         graph.inject(o);
     }
 }
