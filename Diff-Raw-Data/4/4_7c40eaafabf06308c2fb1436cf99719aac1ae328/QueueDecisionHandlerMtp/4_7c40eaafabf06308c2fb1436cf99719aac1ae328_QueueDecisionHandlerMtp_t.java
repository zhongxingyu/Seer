 package matrixtieparent;
 
 import hudson.Extension;
 import hudson.model.Action;
 import hudson.model.Queue.Task;
 import hudson.model.Queue.QueueDecisionHandler;
 import java.util.List;
 import java.io.IOException;
 import hudson.matrix.MatrixProject;
import hudson.model.Hudson;
 import hudson.model.Label;
 import hudson.model.labels.LabelAtom;
 import hudson.tasks.BuildWrapper;
 
 /**
  * Maybe assign a Label to limit where the parent
  * build for a Matrix project may run.
  *
  * @author Ken Bertelson
  */
 @Extension
 public class QueueDecisionHandlerMtp extends QueueDecisionHandler {
     
     @Override
     public boolean shouldSchedule(Task p, List<Action> actions) {
         if (p instanceof MatrixProject) {
             MatrixProject projectParent = (MatrixProject)p;
             for (BuildWrapper bw : projectParent.getBuildWrappers().values()) {
                 if (bw instanceof BuildWrapperMtp) {
                     BuildWrapperMtp bwMtp = (BuildWrapperMtp)bw;
                    Label labelMtp = Hudson.getInstance().getLabel(bwMtp.getLabelName());
 
                     try {
                         projectParent.setAssignedLabel(labelMtp);
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
             }
         }
         return true;
     }
 }
