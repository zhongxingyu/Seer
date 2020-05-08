 package controllers;
 
 import models.Defect;
 import play.mvc.Controller;
 
 import java.util.List;
 
 /**
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  */
 public class Defects extends Controller {
 
     public static void index() {
         List<Defect> defects = Defect.findAll();
         render(defects);
     }
 
     public static void create(Defect defect) {
        defect.save();
         index();
     }
 
 }
