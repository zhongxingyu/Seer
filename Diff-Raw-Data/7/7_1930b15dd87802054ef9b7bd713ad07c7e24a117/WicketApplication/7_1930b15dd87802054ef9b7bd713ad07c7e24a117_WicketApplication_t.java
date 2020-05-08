 package at.ac.tuwien;
 
 import org.apache.wicket.protocol.http.WebApplication;
 
import at.ac.tuwien.out.StartPage;
 
 public class WicketApplication extends WebApplication {
     @Override
    public Class<StartPage> getHomePage() {
        return StartPage.class;
     }
 
     @Override
     public void init() {
         super.init();
 
         // add your configuration here
     }
 }
