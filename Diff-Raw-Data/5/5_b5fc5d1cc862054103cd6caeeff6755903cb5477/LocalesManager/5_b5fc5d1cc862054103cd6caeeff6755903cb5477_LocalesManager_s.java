 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package common;
 
 import java.util.Locale;
 
 /**
  *
  * @author honzap
  */
 public class LocalesManager {
 	
	private static Locale selectedLocale;
 	
 	public LocalesManager() {
		selectedLocale = new Locale("cs","CZ");
 	}
 	
 	public void setLocale(Locale locale) {
 		selectedLocale = locale;
 	}
 	
 	public Locale getLocale() {
 		return selectedLocale;
 	}
 }
