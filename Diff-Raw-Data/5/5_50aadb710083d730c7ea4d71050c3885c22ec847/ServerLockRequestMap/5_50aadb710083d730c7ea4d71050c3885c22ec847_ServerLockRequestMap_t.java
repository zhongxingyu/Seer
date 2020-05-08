 package com.github.julman99.jesync.net;
 
 import com.github.julman99.jesync.core.AbstractLockRequest;
import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * Alias for HashMap<String, ServerLockRequest>
  * @author Julio Viera <julio.viera@gmail.com>
  */
public class ServerLockRequestMap extends ConcurrentHashMap<String, AbstractLockRequest> {
     
 }
