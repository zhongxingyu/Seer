 package cc.rylander.intellij.plugin.polopoly;
 
 import com.intellij.notification.Notification;
 import com.intellij.notification.NotificationType;
 import com.intellij.notification.Notifications;
 import com.intellij.openapi.actionSystem.AnAction;
 import com.intellij.openapi.actionSystem.AnActionEvent;
 import com.intellij.openapi.actionSystem.PlatformDataKeys;
 import com.intellij.openapi.application.ApplicationManager;
 import com.intellij.openapi.editor.Document;
 import com.intellij.openapi.fileEditor.FileDocumentManager;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.ui.Messages;
 import com.intellij.openapi.vfs.VirtualFile;
 import com.intellij.openapi.wm.StatusBar;
 import com.intellij.openapi.wm.WindowManager;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
import java.util.NoSuchElementException;
 import java.util.Scanner;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Import extends AnAction {
     @Override
     public void update(AnActionEvent e) {
         super.update(e);
         VirtualFile[] files = PlatformDataKeys.VIRTUAL_FILE_ARRAY.getData(e.getDataContext());
         boolean enabled = files != null &&
                 files.length == 1 &&
                 "xml".equalsIgnoreCase(files[0].getExtension());
         e.getPresentation().setEnabled(enabled);
 
         final Project project = e.getData(PlatformDataKeys.PROJECT);
         if (project != null) {
             e.getPresentation().setText("-> " + project.getComponent(PolopolyPlugin.class).getChosenSystem().url);
         }
     }
 
     public void actionPerformed(AnActionEvent event) {
         final Project project = event.getData(PlatformDataKeys.PROJECT);
         if (project == null) {
             return;
         }
         final PolopolyPlugin settings = project.getComponent(PolopolyPlugin.class);
         if (null == settings.getChosenSystem().username || null == settings.getChosenSystem().password || null == settings.getChosenSystem().url) {
             Messages.showMessageDialog(project,
                     "Please configure url, user and password for importing to Atex Polopoly", "Error",
                     Messages.getErrorIcon());
             return;
         }
 
         final VirtualFile[] files = PlatformDataKeys.VIRTUAL_FILE_ARRAY.getData(event.getDataContext());
         if (null == files || files.length != 1) {
             return;
         }
         final Document document = FileDocumentManager.getInstance().getDocument(files[0]);
         if (null == document) {
             return;
         }
 
         final StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
         new Thread(new Runnable() {
             public void run() {
                 try {
                     Import.this.notify("Importing to " + settings.getChosenSystem().url, NotificationType.INFORMATION);
 
                     byte[] fileContents = document.getText().getBytes(files[0].getCharset());
                     String contentType = "text/xml;charset=" + files[0].getCharset();
                     URL url = new URL(settings.getChosenSystem().url +
                                       "?username=" + settings.getChosenSystem().username +
                                       "&password=" + settings.getChosenSystem().password +
                                       "&result=true");
                     HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                     connection.setDoOutput(true);
                     connection.setDoInput(true);
                     connection.setRequestMethod("PUT");
                     connection.setRequestProperty("Content-Type", contentType);
                     connection.connect();
                     connection.getOutputStream().write(fileContents);
 
                     final int result = connection.getResponseCode();
                     if (result < 200 || result > 299) {
                         String msg = "See server log for details, no error msg found in response";
                         final InputStream errorStream = connection.getErrorStream();
                         if (errorStream != null) {
                            String response = null;
                            try {
                                response = new Scanner(errorStream).useDelimiter("\\A").next();
                            } catch (NoSuchElementException e) {
                                // Use generic error message
                            }
                             Matcher matcher = Pattern.compile("<input type='hidden' value='(.+)' id='exceptionstring'", Pattern.DOTALL).matcher(response);
                             if (matcher.find()) {
                                 msg = matcher.group(1);
                             }
                         }
                         Import.this.notify("Error when importing file: " + msg, NotificationType.ERROR);
                     } else {
                         Import.this.notify("Finished import to " + settings.getChosenSystem().url,
                                 NotificationType.INFORMATION);
                     }
 
                 } catch (IOException e) {
                     setStatus(statusBar, "Error when importing file to " + settings.getChosenSystem().url + ": " + e.getMessage());
                 }
             }
         }).start();
     }
 
     private void notify(String content, NotificationType type) {
         Notifications.Bus.notify(new Notification("Atex Polopoly", "Atex Polopoly",
                 content, type));
         // setStatus(statusBar, content);
     }
 
     private static void setStatus(final StatusBar statusBar, final String msg) {
         ApplicationManager.getApplication().invokeLater(new Runnable() {
             public void run() {
                 if (null != statusBar) {
                     statusBar.setInfo(msg);
                 }
             }
         });
     }
 }
