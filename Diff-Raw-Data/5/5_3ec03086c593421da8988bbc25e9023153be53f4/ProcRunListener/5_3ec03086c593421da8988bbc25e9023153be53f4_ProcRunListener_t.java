 package hudson.plugins.proc;
 
import hudson.model.AbstractBuild;
 import hudson.model.listeners.RunListener;
 import hudson.model.Run;
 import hudson.model.TaskListener;
 import hudson.Extension;
 import hudson.EnvVars;
 import hudson.util.ProcessTree;
 
 import java.util.List;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.SortedMap;
 import java.io.IOException;
 
 /**
  * @author Jitendra Kotamraju
  */
 @Extension
 public class ProcRunListener extends RunListener<Run> {
 
     public ProcRunListener() {
         super(Run.class);
     }
 
     public void onStarted(Run run, TaskListener listener) {
        if (run instanceof AbstractBuild)
            run.addAction(new ProcAction((AbstractBuild) run));
     }
 
     public void onCompleted(Run run, TaskListener listener) {
         run.getActions().remove(run.getAction(ProcAction.class));
     }
 }
