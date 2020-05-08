 package com.rcatolino.smdp;
 
 public class Smdp {
 
   // the documentation for this api is the same as the c API's one in smdp.h
   // well, except for the getServiceField which allows to pick the id/protocol/address/port
   // fields of the service structure by their index.
   public native void nativeFinalize();
   public native String getServiceField(int fieldPosition);
   public native boolean sendServiceBroadcast();
   public native boolean sendQuery();
   public native boolean waitForAnswer();
   public native boolean waitForQuery();
   public native boolean startBroadcastServer();
   public native boolean createService(String id, String protocol, String address, String port);
 
   static {
    System.loadLibrary("smdp");
   }
 }
