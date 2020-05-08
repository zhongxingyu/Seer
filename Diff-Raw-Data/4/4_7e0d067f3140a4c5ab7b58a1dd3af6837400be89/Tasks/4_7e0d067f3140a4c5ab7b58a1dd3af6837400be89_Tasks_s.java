 package controllers;
 
 import com.mortennobel.imagescaling.ResampleOp;
 import models.Attachment;
 import models.Member;
 import models.Tag;
 import models.Task;
 import org.apache.commons.lang.StringUtils;
 import play.Play;
 import play.data.validation.Valid;
 import play.data.validation.Validation;
 import play.mvc.Before;
 import play.mvc.Controller;
 import play.mvc.With;
 
 import javax.imageio.ImageIO;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.util.*;
 
 /**
  * User: soyoung
  * Date: Dec 21, 2010
  */
 @With(Secure.class)
 public class Tasks extends Controller {
     private static final String FILES_DIR = Play.configuration.getProperty("fileStorage.location");
 
     public static void index() {
         List<Task> tasks = Task.find("from Task t where t.isActive = true order by sortOrder").fetch();
 
         // a placeholder for a new task
         Task task = new Task();
 
         render(tasks, task);
     }
 
     public static void show(Long taskId) {
         Task task = Task.findById(taskId);
         boolean editing = Boolean.parseBoolean(params.get("editing"));
         renderTemplate("Tasks/_show.html", task, editing);
     }
 
     public static void save(@Valid Task task, File[] attachments) throws Exception {
 
         Member loggedInUser = Member.connected();
 
         notFoundIfNull(loggedInUser);
 
         if (Validation.hasErrors()) {
             boolean editing = true;
             renderTemplate("Tasks/_show.html", task, editing);
         } else {
             // overwrite tags todo: find a better way of doing this
             if (task.tags != null) task.tags.clear();
 
             // get selected tags
             String[] selectedTags = params.getAll("selectedTags");
             if (selectedTags != null) {
                 for (String selectedTagName : selectedTags) {
                     if (!StringUtils.isEmpty(selectedTagName)) {
                         task.tagItWith(selectedTagName);
                     }
                 }
             }
 
             if (task.createdDate == null) {
                 task.createdDate = new Date();
             }
 
            task.owner = loggedInUser;
 
             // add attachments
             if (attachments != null) {
                 for (File file : attachments) {
                     // stick file into filesystem
 
                     // Destination directory
                     File dir = new File(FILES_DIR);
 
                     String filename = file.getName();
                     String extension = filename.substring(filename.lastIndexOf('.') + 1);
 
                     // generate a filename
                     String uuid = UUID.randomUUID().toString();
 
                     // attach it to task
                     Attachment attachment = new Attachment();
 
                     attachment.filename = file.getName();
                     attachment.name = uuid;
                     attachment.createdDate = new Date();
                     attachment.task = task;
 
                     // check if the file is an image
                     if ("png".equalsIgnoreCase(extension) ||
                             "gif".equalsIgnoreCase(extension) ||
                             "jpg".equalsIgnoreCase(extension)) {
 
                         attachment.type = "image";
 
                         // read the image
                         BufferedImage srcImage = ImageIO.read(file);
 
                         // scale to thumbnail
                         ResampleOp resampleOp = new ResampleOp(50, 50);
                         BufferedImage thumbnail = resampleOp.filter(srcImage, null);
 
                         // write the thumbnail
                         ImageIO.write(thumbnail, "png", new File(dir, "thumbnail_" + uuid + ".png"));
                     }
 
                     // todo: is there a better way?
                     FileInputStream in = new FileInputStream(file);
                     File outFile = new File(dir, file.getName());
 
                     FileOutputStream out = new FileOutputStream(outFile);
                     byte[] buf = new byte[1024];
                     int len;
                     while ((len = in.read(buf)) > 0) {
                         out.write(buf, 0, len);
                     }
                     in.close();
                     out.close();
 
                     task.attachments.add(attachment);
                 }
             }
 
             task.isActive = true;
             task.save();
             renderTemplate("Tasks/_show.html", task);
         }
     }
 
     public static void delete(Long taskId) {
 //        Task.deleteById(taskId);
         Task task = Task.findById(taskId);
         task.deactivate();
     }
 
     public static void undelete(Long taskId) {
 //        Task.deleteById(taskId);
         Task task = Task.findById(taskId);
         task.activate();
     }    
 
 
     public static void listTagged(String tag) {
         List<Task> tasks = Task.findTaggedWith(tag);
 
         // a placeholder for a new task
         Task task = new Task();
 
         renderTemplate("Tasks/index.html", tag, tasks, task);
     }
 
     public static void trash() {
         List<Task> tasks = Task.find("from Task t where t.isActive = false order by sortOrder").fetch();
 
         // a placeholder for a new task
         Task task = new Task();
 
         render(tasks);
     }
 
     public static void filter(String[] checkedTags) {
         List<Task> tasks = Task.findAll();
 
         // a placeholder for a new task
         Task task = new Task();
 
         renderTemplate("Tasks/index.html", checkedTags, tasks, task);
     }
 
     public static void sort(String order) {
         // order will come in the form: task-3,task-2,task-1
         StringTokenizer taskTokens = new StringTokenizer(order, ",", false);
 
         int index = 0;
 
         while (taskTokens.hasMoreTokens()) {
             String taskToken = taskTokens.nextToken();
 
             // get the task id from the token
             String taskId = taskToken.substring(taskToken.lastIndexOf('-') + 1);
 
             Task task = Task.findById(Long.parseLong(taskId));
 
             task.sortOrder = index;
 
             task.save();
 
             index++;
         }
     }
 }
