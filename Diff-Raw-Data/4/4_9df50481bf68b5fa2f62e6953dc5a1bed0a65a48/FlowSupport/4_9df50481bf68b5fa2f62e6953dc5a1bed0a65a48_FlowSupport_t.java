 /*
  * Copyright 2005-2006 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.beanflow;
 
import org.apache.servicemix.beanflow.support.FieldIntrospector;
import org.apache.servicemix.beanflow.support.Introspector;
 
 import java.util.Iterator;
 
 /**
  * A useful base class which allows simple bean flows to be written easily. When
  * this flow is started it will listen to all the state values which can be
  * found by the introspector (such as all the fields by default) calling the
  * {@link run} method when the state changes so that the flow can be evaluted.
  * 
  * @version $Revision: $
  */
 public abstract class FlowSupport implements Runnable, Flow {
 
     private State<Transitions> state = new DefaultState<Transitions>(Transitions.Initialised);
     private State<String> failed = new DefaultState<String>();
     private Introspector introspector = new FieldIntrospector();
 
     /**
      * Starts the flow
      */
     public void start() {
         if (state.compareAndSet(Transitions.Initialised, Transitions.Starting)) {
             doStart();
             state.set(Transitions.Started);
         }
     }
 
     /**
      * Stops the flow
      */
     public void stop() {
         if (state.compareAndSet(Transitions.Started, Transitions.Stopping)) {
             state.set(Transitions.Stopped);
             doStop();
         }
     }
 
     /**
      * Stops the flow with a failed state, giving the reason for the failure
      */
     public void fail(String reason) {
         stop();
         failed.set(reason);
     }
     
     /**
      * Returns the current running state of this flow
      */
     public State<Transitions> getState() {
         return state;
     }
 
     public boolean isStopped() {
         return state.is(Transitions.Stopped);
     }
 
     public boolean isFailed() {
         return getFailedReason() != null;
     }
     
     public String getFailedReason() {
         return failed.get();
     }
     
     // Implementation methods
     // -------------------------------------------------------------------------
     protected void doStart() {
         addListeners(this);
     }
 
     protected void doStop() {
         removeListeners(this);
     }
 
     protected Introspector getIntrospector() {
         return introspector;
     }
 
     protected void setIntrospector(Introspector introspector) {
         this.introspector = introspector;
     }
 
     protected void addListeners(Runnable listener) {
         Iterator<State> iter = introspector.iterator(this);
         while (iter.hasNext()) {
             iter.next().addRunnable(listener);
         }
     }
 
     protected void removeListeners(Runnable listener) {
         Iterator<State> iter = introspector.iterator(this);
         while (iter.hasNext()) {
             iter.next().removeRunnable(listener);
         }
     }
 }
