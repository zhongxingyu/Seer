 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 
 import models.Audit;
 
 /**
  * The audit class handles audits and their metadata.
  */
 // @With(Secure.class)
 public class Audits extends Controller {
 
  public static void index(long budgetId) {
    List<Audit> audits = Audit.find("audited_id", budgetId).asList();
    // List<Audit> audits = Audit.findAll();
     renderJSON(audits);
   }
 }
