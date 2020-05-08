 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 
 import models.*;
 
 public class Application extends Controller {
 
     public static void index() {
         render();
     }
 
     public static void lessons() {
         List<Lesson> lessons = Lesson.all().fetch();
         render(lessons);
     }
 
     public static void students() {
         List<Student> students = Student.all().fetch();
         render(students);
     }
 
     public static void empty() {
         List<Object> empty = new ArrayList<Object>();
         render(empty);
     }

    public static void badList() {
        List<String> badModels = new ArrayList<String>();
        badModels.add("foo");
        render(badModels);
    }
 }
