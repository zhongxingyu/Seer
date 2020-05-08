 package controllers;
 
 import play.*;
 import play.data.validation.*;
 import play.mvc.*;
 
 import models.*;
 
 import java.util.*;
 
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 
 import models.Logic.StatusCode;
 import models.Woman.Event;
 import models.Woman.Outcome;
 
 
 /**
  * The Class Forms.
  */
 public class Forms extends Controller {
     
     /**
      * Index.
      */
     public static void index() {
     
     }
     
     /**
      * View Form details page.
      * 
      * @param id
      *            the id
      */
     public static void view(Long id) {
     
         Form form = Form.findById(id);
         List<Form> forms = Form.findAll();
         render(form, forms);
     }
     
     /**
      * Edit Form.
      * 
      * @param id
      *            the id
      */
     public static void edit(Long id) {
     
        List<Form> forms = Form.findAll();
        forms.add(0, new Form("Not Applicable"));
         Form form = Form.findById(id);
        render(form, forms);
     }
     
     /**
      * List all Form.
      */
     public static void list() {
     
         List<Form> forms = Form.findAll();
         render(forms);
     }
     
     /**
      * Creates new Form.
      */
     public static void create() {
     
         render();
     }
     
     /**
      * Submit new Form.
      * 
      * @param form
      *            the form
      */
     public static void submit(@Valid Form form) {
     
         if (validation.hasErrors()) {
             render("@create", form);
         }
         form.save();
         list();
     }
     
     /**
      * Adds logic to Form.
      * 
      * @param formId
      *            the form id
      * @param status
      *            the status
      * @param base
      *            the base Date
      * @param outcome
      *            the outcome
      * @param destination_id
      *            the destination Form
      * @param duration
      *            the duration in days
      * @param event
      *            the event
      */
     public static void addLogic(Long formId, Logic.StatusCode status, Event base, Outcome outcome, Long destination_id, int duration, Event event) {
     
         Form form = Form.findById(formId);
         
         // Debug
         /*
         System.out.println(status);
         System.out.println(base);
         System.out.println(outcome);
         System.out.println(destination_id);
         System.out.println(duration);
         System.out.println(event);
         */
         
         if (validation.hasErrors()) {
             List<Form> forms = Form.findAll();
             render("@view", form, forms);
         }
         
         Form destination = Form.findById(destination_id);
         form.addLogic(status, base, outcome, destination, duration, event);
         view(formId);
     }
 }
