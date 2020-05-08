 package controllers;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import play.Play;
 import play.i18n.Lang;
 import play.mvc.Controller;
 
 public class Application extends BaseController {
 
 	public static void index() {
 		render();
 	}
 
 	public static void changeLang(String lang) {
 		Lang.change(lang);
 	}
 }
 
