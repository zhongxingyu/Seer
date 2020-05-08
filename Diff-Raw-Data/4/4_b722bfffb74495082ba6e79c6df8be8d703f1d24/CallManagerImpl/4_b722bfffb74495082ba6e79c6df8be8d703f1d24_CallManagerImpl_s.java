 package com.acmetelecom;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * User: Chris Bates
  * Date: 03/12/12
  */
 public class CallManagerImpl implements CallManager {
 
     private Collection<CallEvent> callLog;
 
     @Override
     public void callInitiated(String caller, String callee) {
         callLog.add(new CallStart(caller, callee));
     }
 
     @Override
     public void callCompleted(String caller, String callee) {
         callLog.add(new CallEnd(caller, callee));
     }
 
     @Override
     public Collection<Call> getCallsFor(String callerNumber) {
 
         List<Call> calls = new ArrayList<Call>();
 
         CallEvent start = null;
         for (CallEvent event : callLog) {
             if(event.getCaller().equals(callerNumber))
             if (event instanceof CallStart) {
                 start = event;
             }
             if (event instanceof CallEnd && start != null) {
                 calls.add(new Call(start, event));
                 start = null;
             }
         }
 
         return calls;
     }
 
     @Override
     public void clearLog() {
         this.callLog.clear();
     }
 }
