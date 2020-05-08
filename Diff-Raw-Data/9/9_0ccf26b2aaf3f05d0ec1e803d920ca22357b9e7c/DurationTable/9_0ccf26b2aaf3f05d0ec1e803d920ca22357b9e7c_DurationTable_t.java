 package antw.ui;
 
 import antw.ant.Printer;
 import antw.model.Project;
 import antw.model.Projects;
 import antw.model.Target;
 import antw.util.TimeUtil;
 
 public class DurationTable extends Table {
 
     private static final String HEADER_FORMAT = "%-25s %-40s %-15s %-15s %-15s %-15s%n";
     private static final String TARGET_FORMAT = "%-25s %-40s %-15d %-15tT %-15tT %-15s%n";
     private Project _lastProject;
 
     @Override
     public void logBuildFinished(Printer printer, Projects projects) {
     }
 
     @Override
     public void logTargetStartet(Printer printer, Target target) {
         if (!target.getProject().equals(_lastProject)) {
             printer.newLine();
         }
         _lastProject = target.getProject();
         if (!target.getProject().isSubProject()) {
             logHeader(printer);
         }
     }
 
     @Override
     public void logTargetFinished(Printer printer, Target target) {
        if (!target.getProject().equals(_lastProject)) {
            printer.newLine();
        }
         printer.out(
                 TARGET_FORMAT,
                 new Object[] { target.getProject().getName(), target.getName(), target.getCounter(), target.getStart(),
                         target.getFinish(),
                         TimeUtil.formatTimeDuration(target.getFinish().getTime() - target.getStart().getTime()) });
         if (!target.getProject().isSubProject()) {
             printer.newLine(2);
         }
     }
 
     private void logHeader(Printer printer) {
         printer.out(HEADER_FORMAT, new Object[] { "PROJECT", "TARGET", "CALL COUNT", "START", "FINISH", "DURATION" });
         String line = "";
         for (int i = 0; i < 125; i++) {
             line += "-";
         }
         printer.out(line);
     }
 
 }
