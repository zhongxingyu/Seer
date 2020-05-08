 package controllers;
 
 import models.Problem;
 /**
  * Competitions
  *
  * Controller for the Competition model.
  *
  * @author Kenny Meyer <knny.myer@gmail.com>
  */
 public class Problems extends Application {
 
     /**
      * Render description of Problem() given the ID
      *
      * @param id ID of the Problem
      */
     public static void getDescription(Long id) {
         Problem problem = Problem.findById(id);
         String description = problem.description;
 
        renderText(description);
    }

    public static void create() {

        render();
     }
 }
