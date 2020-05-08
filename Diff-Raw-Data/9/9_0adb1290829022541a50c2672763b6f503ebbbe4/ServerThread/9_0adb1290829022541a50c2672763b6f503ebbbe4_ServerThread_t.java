 package com.arlandis;
 
 import com.arlandis.interfaces.Responder;
 
 public class ServerThread implements Runnable {
 
     private final Responder responder;
 
     public ServerThread(Responder responder) {
         this.responder = responder;
     }
 
     public void run() {
         responder.respond();
     }
 }
