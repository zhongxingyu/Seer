 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 
 import models.*;
 
 public class Application extends Controller {
 
     public static void index() {
     	List<Task> tasks = Task.findAll();
         render(tasks);
     }
 
     public static void add(String name) {
     	Task task = new Task(name).save();
     	renderJSON(task);
     }
 
     public static void cleanup() {
         List<Task> tasks = Task.findAll();
         for (Task task : tasks) {
             if (task.done) {
                 task.delete();
             }
         }
         ok();
     }
 
     public static void done(Long id, Boolean done) {
     	Task task = Task.findById(id);
     	task.done = done;
    	task.save();
     	renderJSON(task);
     }
 }
