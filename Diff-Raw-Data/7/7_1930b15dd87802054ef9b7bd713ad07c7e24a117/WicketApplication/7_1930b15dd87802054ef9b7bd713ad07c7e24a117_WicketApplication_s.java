 package at.ac.tuwien;
 
 import org.apache.wicket.protocol.http.WebApplication;
 
import at.ac.tuwien.out.OutPage;
 
 public class WicketApplication extends WebApplication {
     @Override
    public Class<OutPage> getHomePage() {
        return OutPage.class;
     }
 
     @Override
     public void init() {
         super.init();
 
         // add your configuration here
     }
 }
