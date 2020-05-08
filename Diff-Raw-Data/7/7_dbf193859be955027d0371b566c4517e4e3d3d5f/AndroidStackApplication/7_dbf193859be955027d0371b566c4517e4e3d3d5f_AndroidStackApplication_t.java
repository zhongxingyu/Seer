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
 
         graph = ObjectGraph.create(
                new EventBusModule(),
                new HelloModule()
         );
     }
 
     public void inject(Object o) {
         graph.inject(o);
     }
 }
