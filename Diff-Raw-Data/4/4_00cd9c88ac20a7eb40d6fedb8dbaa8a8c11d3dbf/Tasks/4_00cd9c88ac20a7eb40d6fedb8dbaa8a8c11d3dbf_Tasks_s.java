 package controllers;
 
 import com.mortennobel.imagescaling.ResampleOp;
 import controllers.utils.TaskIndex;
 import models.Attachment;
 import models.Member;
 import models.Task;
 import org.apache.commons.lang.StringUtils;
 import play.Play;
 import play.data.validation.Valid;
 import play.data.validation.Validation;
 import play.mvc.Controller;
 import play.mvc.With;
 
 import javax.imageio.ImageIO;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * User: soyoung
  * Date: Dec 21, 2010
  */
 @With(Secure.class)
 public class Tasks extends Controller {
     private static final String FILES_DIR = Play.configuration.getProperty("fileStorage.location");
 
 
     public static void index(Long taskId) {
         List<Task> tasks;
 
         if (taskId != null) {
             tasks = new ArrayList<Task>();
             Task task = Task.findById(taskId);
             tasks.add(task);
         } else {
             tasks = Task.find("from Task t where t.isActive = true order by sortOrder desc").fetch();
         }
 
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
             if (task.createdDate == null) {
                 task.createdDate = new Date();
             }
 
             if (task.owner == null) {
                 task.owner = loggedInUser;
             }
 
             // overwrite tags todo: find a better way of doing this
             if (task.tags != null) task.tags.clear();
 
             // todo: tags are stored in BOTH db and lucene index. <-- is this ok?
             // todo: we need to enforce the mutex rules for tagGroups
             // todo: remove hardcoding
 
             // get selected tags
             String[] selectedTags = params.getAll("selectedTags");
             if (selectedTags != null) {
                 for (String selectedTagName : selectedTags) {
                     if (!StringUtils.isEmpty(selectedTagName)) {
                         task.tagItWith(selectedTagName);
                     }
                 }
             }
 
             // add attachments
             if (attachments != null) {
                 for (File file : attachments) {
                     Attachment attachment = createAttachment(file);
 
                     attachment.task = task;
                     task.attachments.add(attachment);
                 }
             }
 
             task.isActive = true;
             task.save();
 
             // todo: nasty hack: need to set sortOrder to id for newly created tasks
 
             if (task.sortOrder == null) {
                 task.sortOrder = task.id;
                 task.save();
             }
 
             TaskIndex.addTaskToIndex(task);
 
             renderTemplate("Tasks/task.html", task);
         }
     }
 
     public static void addAttachment(Long taskId, File file) throws Exception {
         Task task = Task.findById(taskId);
 
         Attachment attachment = createAttachment(file);
 
         attachment.task = task;
         task.attachments.add(attachment);
 
        renderTemplate("Tasks/attachment.json", attachment);
     }
 
     public static void deleteAttachment(Long id) {
         Attachment attachment = Attachment.findById(id);
         attachment.delete();
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
 
 
     public static void listTagged(String tag) throws Exception {
         List<Task> tasks = TaskIndex.searchTasks("tags", tag, true);
 
         // a placeholder for a new task
         Task task = new Task();
 
         renderTemplate("Tasks/index.html", tag, tasks, task);
     }
 
     public static void trash() {
         List<Task> tasks = Task.find("from Task t where t.isActive = false order by sortOrder desc").fetch();
 
         // a placeholder for a new task
         Task task = new Task();
 
         render(tasks);
     }
 
     public static void filter(String[] checkedTags) throws Exception {
         List<Task> tasks;
 
         if (checkedTags != null) {
             StringBuffer queryString = new StringBuffer();
             for (int i = 0, l = checkedTags.length; i < l; i++) {
                 String checkedTag = checkedTags[i];
                 queryString.append(checkedTag);
                 if (i < l - 1) {
                     queryString.append(" AND ");
                 }
             }
             tasks = TaskIndex.searchTasks("tags", queryString.toString(), true);
         } else {
             tasks = Task.findAll();
         }
 
         // a placeholder for a new task
         Task task = new Task();
 
         renderTemplate("Tasks/index.html", checkedTags, tasks, task);
     }
 
     public static void sort(Long[] order) {
 
         // work out ordering
         List<Task> tasks = Task.find("select t from models.Task t where t.id in (:taskIds) order by t.sortOrder desc").bind("taskIds", order).fetch();
 
         ArrayList<Long> ordering = new ArrayList<Long>();
         for (Iterator<Task> iterator = tasks.iterator(); iterator.hasNext();) {
             Task task = iterator.next();
 
             ordering.add(task.sortOrder);
         }
 
         // assign ordering
         for (int i = 0; i < order.length; i++) {
             Task task = Task.findById(order[i]);
             task.sortOrder = ordering.get(i);
             task.save();
         }
 
 //        // order will come in the form: task-3,task-2,task-1
 //        StringTokenizer taskTokens = new StringTokenizer(order, ",", false);
 //
 //        int index = 0;
 //
 //        while (taskTokens.hasMoreTokens()) {
 //            String taskToken = taskTokens.nextToken();
 //
 //            // get the task id from the token
 //            String taskId = taskToken.substring(taskToken.lastIndexOf('-') + 1);
 //
 //            Task task = Task.findById(Long.parseLong(taskId));
 //
 //            task.sortOrder = index;
 //
 //            task.save();
 //
 //            index++;
 //        }
     }
 
     public static void search(String field, String queryText) throws Exception {
         if (StringUtils.isEmpty(queryText)) {
             renderTemplate("Tasks/searchTerms.html");
         } else {
             List<Task> tasks = TaskIndex.searchTasks(field, queryText, false);
 
             render(tasks, field, queryText);
         }
     }
 
 
     private static Attachment createAttachment(File file) throws IOException {
         // Destination directory
         File dir = new File(FILES_DIR);
 
         String filename = file.getName();
         String extension = filename.substring(filename.lastIndexOf('.') + 1);
 
         // generate a filename
         String uuid = UUID.randomUUID().toString();
 
         // attach it to task
         Attachment attachment = new Attachment();
 
         attachment.title = filename;
         attachment.name = uuid;
         attachment.createdDate = new Date();
         attachment.filename = attachment.name + "." + extension;
 
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
         File outFile = new File(dir, uuid + "." + extension);
 
         FileOutputStream out = new FileOutputStream(outFile);
         byte[] buf = new byte[1024];
         int len;
         while ((len = in.read(buf)) > 0) {
             out.write(buf, 0, len);
         }
         in.close();
         out.close();
         return attachment;
     }
 }
