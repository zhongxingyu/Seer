 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 
import fetch.MegaSenaFetch;

 import models.*;
 
 public class Application extends Controller {
 
     public static void index() {
    	Lottery lottery = new MegaSenaFetch(1280).fetch();
        render(lottery);
     }
 }
