 package saturn;
 
 import org.apache.wicket.Page;
 import org.apache.wicket.protocol.http.WebApplication;
 
 import saturn.web.HomePage;
 
 public class SaturnApplication extends WebApplication {
 
     @Override
     public Class<? extends Page> getHomePage() {
         return HomePage.class;
     }

 }
