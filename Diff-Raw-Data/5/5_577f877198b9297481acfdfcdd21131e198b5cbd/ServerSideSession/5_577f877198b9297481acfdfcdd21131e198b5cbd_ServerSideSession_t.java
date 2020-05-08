 // Copyright 2008 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.remote.http;
 
 import java.io.Serializable;
 import java.lang.reflect.Method;
 
 import org.joe_e.array.ConstArray;
 import org.ref_send.promise.Log;
 import org.ref_send.promise.Promise;
 import org.waterken.syntax.json.JSON;
 
 /**
  * The server-side state associated with a messaging session.
  */
 /* package */ final class
 ServerSideSession implements Serializable {
     static private final long serialVersionUID = 1L;
 
     private final String name;                  // GUID of this session
     private final Log log;                      // corresponding log output
     
     private       long current;                 // current window number
     private       ConstArray<Object> returns;   // returns in current window
     
     protected
     ServerSideSession(final String name, final Log log) {
         this.name = name;
         this.log = log;
         
         current = -1;
        returns = new ConstArray<Object>();
     }
     
     static protected Object
     execute(final Promise<?> op) {
         try {
             return op.call();
         } catch (final Exception e) {
             return JSON.Rejected.make(e);
         }
     }
     
     protected Object
     once(final long window, final int message,
          final Method method, final Promise<?> op) {
         if (window == current) {
             if (message != returns.length()) { return returns.get(message); }
         } else {
             current = window;
            returns = new ConstArray<Object>();
         }
         log.got(name + "-" + window + "-" + message, null, method);
         final Object r = execute(op);
         returns = returns.with(r);
         final Class<?> R = method.getReturnType();
         if (null != r || (void.class != R && Void.class != R)) {
             log.returned(name + "-" + window + "-" + message + "-return");
         }
         return r;
     }
 }
