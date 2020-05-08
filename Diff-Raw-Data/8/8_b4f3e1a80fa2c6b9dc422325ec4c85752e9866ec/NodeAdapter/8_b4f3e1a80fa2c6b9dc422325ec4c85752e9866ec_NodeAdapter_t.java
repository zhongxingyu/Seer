 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.kevoree.library.freepastry;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.concurrent.Future;
import org.macaw.annotation.Action;
import org.macaw.annotation.Adapter;
import org.macaw.annotation.Cleanup;
import org.macaw.annotation.Setup;
 
 /**
  *
  * @author sunye
  */
 @Adapter
 public class NodeAdapter {
 
     private PastryPeer peer;
     private InetSocketAddress address;
 
     @Setup
     public void setup() throws IOException {
         peer = new PastryPeer(address);
         peer.createPast();
     }
 
     @Cleanup
     public void cleanup() {
     }
 
     @Action
     public void join() throws InterruptedException, IOException {
         peer.join();
         peer.createPast();
     }
 
     @Action
     public void leave() {
         peer.leave();
 
     }
 
     @Action
     public void put(String key, String value) throws InterruptedException {
         assert peer != null;
         peer.put(key, value);
 
     }
 
     @Action
     public String get(String key) throws InterruptedException {
         assert peer != null;
 
         return peer.get(key);
 
     }
     
     public Future<String> retrieve(String key) {
         
         return null;
         
     }
 } 
