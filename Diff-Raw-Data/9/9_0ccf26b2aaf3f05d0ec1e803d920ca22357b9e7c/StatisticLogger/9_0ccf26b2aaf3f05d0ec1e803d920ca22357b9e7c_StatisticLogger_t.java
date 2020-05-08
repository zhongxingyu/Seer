 package antw.ant;
 
 import java.util.Date;
 
 import org.apache.tools.ant.BuildEvent;
 
 import antw.model.Projects;
 import antw.model.Target;
 import antw.ui.DurationTable;
 import antw.ui.SummaryTable;
 import antw.ui.Table;
 
 public class StatisticLogger extends LoggerAdapter {
 
     private Projects _projects = new Projects();
     private Table[] _tables = new Table[] { new DurationTable(), new SummaryTable() };
 
     public void buildFinished(BuildEvent event) {
         _projects.setEnd(new Date());
         for (int i = 0; i < _tables.length; i++) {
             _tables[i].logBuildFinished(this, _projects);
         }
         newLine(3);
         if (event.getException() != null) {
             err(event.getException());
             out("BUILD FAILED :(");
         } else {
             out("BUILD SUCCESSFUL :)");
         }
         newLine(2);
     }
 
     @Override
     public void buildStarted(BuildEvent event) {
         _projects.setStart(new Date());
        newLine();
     }
 
     @Override
     public void messageLogged(BuildEvent event) {
     }
 
     @Override
     public void targetFinished(BuildEvent event) {
         String projectName = event.getProject().getName();
         String targetName = event.getTarget().getName();
         Target target = _projects.get(projectName).getTarget(targetName).addFinishTime(new Date());
         for (int i = 0; i < _tables.length; i++) {
             _tables[i].logTargetFinished(this, target);
         }
     }
 
     @Override
     public void targetStarted(BuildEvent event) {
         String projectName = event.getProject().getName();
         String targetName = event.getTarget().getName();
         _projects.get(projectName).getTarget(targetName).addStartTime(new Date()).increment();
         for (int i = 0; i < _tables.length; i++) {
             _tables[i].logTargetStartet(this, _projects.get(projectName).getTarget(targetName));
         }
     }
 
     @Override
     public void subBuildStarted(BuildEvent event) {
         _projects.get(event.getProject().getName()).setSubProject(true);
     }
 
     @Override
     public void subBuildFinished(BuildEvent event) {
         _projects.get(event.getProject().getName()).setSubProject(false);
     }
 
 }
