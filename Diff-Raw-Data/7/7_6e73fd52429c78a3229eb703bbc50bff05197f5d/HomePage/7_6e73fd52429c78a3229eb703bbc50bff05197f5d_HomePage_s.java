 package saturn.web;
 
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 
 public class HomePage extends WebPage {
 
     public HomePage() {
        add(new Label("user", "World"));
     }
 }
