 package ${package}.client;
 
 
 import com.google.gwt.core.client.EntryPoint;
 
 import de.saumya.gwt.translation.common.client.route.ScreenController;
 import de.saumya.gwt.translation.gui.client.GUIContainer;
 
 public class Application implements EntryPoint {
 
     @Override
     public void onModuleLoad() {
        final GUIContainer container = new GUIContainer();
	// build the ixtlan stuff with default locale "en"
        final ScreenController screenController = container.build("en");
     }
 }
