 package com.rackspace.plugins.model;
 
 import hudson.model.Cause;
 import hudson.triggers.SCMTrigger;
 import hudson.triggers.TimerTrigger;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Created by IntelliJ IDEA.
  * User: john.madrid
  * Date: 5/3/11
  * Time: 10:34 AM
  * To change this template use File | Settings | File Templates.
  */
 public class BuildTriggers {
 
     private Map<Class<? extends Cause>, Boolean> causes;
 
     @DataBoundConstructor
     public BuildTriggers(boolean triggerPeriodically, boolean triggerScm, boolean triggerManually) {
         causes = new HashMap<Class<? extends Cause>, Boolean>();
 
         causes.put(SCMTrigger.SCMTriggerCause.class, triggerScm);
         causes.put(Cause.UserCause.class, triggerManually);
         causes.put(TimerTrigger.TimerTriggerCause.class, triggerPeriodically);
     }
 
     public boolean isTriggerPeriodically() {
         return causes.get(TimerTrigger.TimerTriggerCause.class);
     }
 
     public boolean isTriggerScm() {
         return causes.get(SCMTrigger.SCMTriggerCause.class);
     }
 
     public boolean isTriggerManually() {
         return causes.get(Cause.UserCause.class);
     }
 
     public boolean isTriggeredBy(Class<? extends Cause> cause) {
        Boolean triggered = causes.get(cause);

        return triggered!=null?triggered:false;
     }
 }
