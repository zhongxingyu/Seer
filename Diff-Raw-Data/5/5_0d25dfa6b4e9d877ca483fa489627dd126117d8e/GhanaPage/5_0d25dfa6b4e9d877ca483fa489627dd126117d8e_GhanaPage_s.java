 package org.ghana.national.pages;
 
 import org.jbehave.web.selenium.WebDriverPage;
 import org.jbehave.web.selenium.WebDriverProvider;
 
 import static java.lang.String.format;
 
 public class GhanaPage extends WebDriverPage {
     private String page;
 
     public GhanaPage(WebDriverProvider driverProvider, String page) {
         super(driverProvider);
         this.page = page;
     }
 
     protected String url() {
        return format("%s/%s", "http://localhost:8080/GHANA-National", page);
     }
 
     protected String url(String page) {
        return format("%s/%s", "http://localhost:8080/GHANA-National", page);
     }
 
     public void go() {
         get(url());
     }
 
     public boolean isCurrent() {
         return getCurrentUrl().contains(url());
     }
 
     public void hasErrorText(String error) {
 
     }
 }
