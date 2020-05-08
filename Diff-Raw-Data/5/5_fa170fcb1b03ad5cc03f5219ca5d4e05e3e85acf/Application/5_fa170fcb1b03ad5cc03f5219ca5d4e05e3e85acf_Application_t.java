 package controllers;
 
 import play.*;
 import play.data.validation.*;
 import play.mvc.*;
 
 import java.util.*;
 import models.*;
 
 
 /**
  * The Class Application.
  */
 public class Application extends Controller {
     
     /**
      * Index page.
      */
     public static void index() {
     
         List<String> sectors = Woman.find("SELECT DISTINCT w.sectorId AS id FROM Woman w ORDER BY w.sectorId ASC").fetch();
         sectors.add(0, "All");
         
         List<FormEntity> events = FormEntity.find("scheduled >= ?", new Date()).fetch();
         render(events, sectors);
     }
     
     /**
      * Filter Schedules by Sector Id.
      * 
      * @param id
      *            the sector id
      */
     public static void filter(Long id) {
     
         List<FormEntity> events;
         if (id == null) {
             events = FormEntity.find("scheduled >= ?", new Date()).fetch();
         }
         else {
             events = FormEntity.find("SELECT f FROM FormEntity AS f, Woman AS w WHERE f.woman = w AND f.scheduled >= ?  AND w.sectorId = ?",
                     new Date(), id).fetch();
         }
         
         render(events);
     }
     
     /**
      * Update.
      * 
      * @param form_id
      *            the form_id
      * @param status
      *            the status
      */
    public static void update(Long form_id, Logic.StatusCode status, Woman.Outcome outcome) {
     
         FormEntity fe = FormEntity.findById(form_id);
        fe.update(status, outcome);
     }
 }
