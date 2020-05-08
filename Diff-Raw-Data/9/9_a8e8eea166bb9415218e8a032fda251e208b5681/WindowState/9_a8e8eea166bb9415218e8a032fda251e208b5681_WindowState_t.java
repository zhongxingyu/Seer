 package kkckkc.jsourcepad;
 
 import com.google.common.collect.Lists;
 import kkckkc.jsourcepad.model.Application;
 import kkckkc.jsourcepad.model.Window;
 import kkckkc.jsourcepad.model.WindowManager;
 import kkckkc.jsourcepad.model.settings.SettingsManager;
 
 import java.io.File;
 import java.util.List;
 
 public class WindowState implements SettingsManager.Setting {
 
     private List<String> projectDirs = Lists.newArrayList();
     private String focusedProject;
 
     public List<String> getProjectDirs() {
         return projectDirs;
     }
 
     public void setProjectDirs(List<String> projectDirs) {
         this.projectDirs = projectDirs;
     }
 
     public String getFocusedProject() {
         return focusedProject;
     }
 
     public void setFocusedProject(String focusedProject) {
         this.focusedProject = focusedProject;
     }
 
     @Override
     public SettingsManager.Setting getDefault() {
         return new WindowState();
     }
 
     public static void save() {
         List<String> projectDirs = Lists.newArrayList();
         String focusedWindow;
 
         WindowManager wm = Application.get().getWindowManager();
         for (Window window : wm.getWindows()) {
             File projectDir = window.getProject().getProjectDir();
             projectDirs.add(projectDir == null ? null : projectDir.toString());
 
             window.saveState();
         }
 
        File focusedProjectDir = wm.getFocusedWindow() == null ? null : wm.getFocusedWindow().getProject().getProjectDir();
         focusedWindow = focusedProjectDir == null ? null : focusedProjectDir.toString();
 
         WindowState windowState = new WindowState();
         windowState.setFocusedProject(focusedWindow);
         windowState.setProjectDirs(projectDirs);
 
         Application.get().getSettingsManager().update(windowState);
     }
 
     public static void restore() {
         WindowState windowState = Application.get().getSettingsManager().get(WindowState.class);
 
         Window windowToFocus = null;
 
         WindowManager wm = Application.get().getWindowManager();
         for (String projectDir : windowState.getProjectDirs()) {
             Window window = wm.newWindow(projectDir == null ? null : new File(projectDir));
 
             if ((windowState.getFocusedProject() == null && projectDir == null) ||
                 (windowState.getFocusedProject() != null &&
                         windowState.getFocusedProject().equals(projectDir))) {
                 windowToFocus = window;
             }
         }
 
         if (windowToFocus != null) {
             windowToFocus.requestFocus();
         }
     }
 
 
 }
