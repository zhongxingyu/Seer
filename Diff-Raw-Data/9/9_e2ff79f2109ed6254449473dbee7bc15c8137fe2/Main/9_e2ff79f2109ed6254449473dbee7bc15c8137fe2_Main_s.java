 package synchronize;
 
import org.apache.pivot.beans.BXML;
 import org.apache.pivot.beans.BXMLSerializer;
 import org.apache.pivot.collections.Map;
 import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.Component;
 import org.apache.pivot.wtk.DesktopApplicationContext;
 import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.TabPane;
 import org.apache.pivot.wtk.Window;
 
 public class Main implements Application {
     private Window window = null;
     
     public static void main(String[] args) {
     	DesktopApplicationContext.main(Main.class, args);
     }
 
     @Override
     public void startup(Display display, Map<String, String> properties)
         throws Exception {
         BXMLSerializer bxmlSerializer = new BXMLSerializer();
         
         window = (Window)bxmlSerializer.readObject(Main.class, "window.bxml");
         window.open(display);
     }
 
     @Override
     public boolean shutdown(boolean optional) {
         if (window != null) {
             window.close();
         }
 
         return false;
     }
 
     @Override
     public void suspend() {
     }
 
     @Override
     public void resume() {
     }
 }
