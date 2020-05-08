 package com.dhbw_db.control;
 
import java.io.File;

 import com.dhbw_db.model.beans.User;
 import com.dhbw_db.model.io.logging.FileLogger;
 import com.dhbw_db.model.io.logging.LoggingService;
 import com.dhbw_db.model.io.logging.LoggingService.LogLevel;
 import com.dhbw_db.model.request.Request;
 import com.dhbw_db.view.ApplicationWindow;
 import com.dhbw_db.view.DetailsPage;
 import com.dhbw_db.view.PostButtonPage;
 import com.dhbw_db.view.StartPage;
 import com.vaadin.annotations.Theme;
 import com.vaadin.server.VaadinRequest;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.UI;
 
 /**
  * Main UI class. This is the entry point of the Vaadin Framework.
  * 
  * @author jhoppmann
  */
 @SuppressWarnings("serial")
 @Theme("ourTheme")
 public class MainUI extends UI {
 
 	private MainController mc;
 
 	@Override
 	protected void init(VaadinRequest request) {
 
 		getPage().setTitle("DHBW - Notebookverleih");
 
 		String url = this.getPage()
 							.getLocation()
 							.toString();
 
 		mc = new MainController(null);
 		LoggingService.getInstance()
						.registerLogger(new FileLogger(	new File("C:\\logs\\notebookrental.log"),
														true));
 		// check if the url contains a valid hash
 		if (url.contains("hash=")) {
 			Component c = new PostButtonPage(	"Kein Antrag gefunden",
 												"Der angeforderte Antrag existiert nicht.");
 			User u = new User();
 			{
 				// create a dummy user
 				u.setAdmin(false);
 				u.setStudent(false);
 				u.setLecturer(false);
 			}
 			mc.setUser(u);
 			int parameterPosition = url.indexOf("hash=");
 			if (url.length() >= parameterPosition + 37) {
 				String hash = url.substring(parameterPosition + 5,
 											(parameterPosition + 37));
 				LoggingService.getInstance()
 								.log(	"Entering for request with hash "
 												+ hash,
 										LogLevel.INFO);
 
 				Request rq = mc.getDataAccess()
 								.getRequestForHash(hash);
 				if (rq != null) {
 					u = mc.getDataAccess()
 							.getUserForID(rq.getApproverId());
 					mc.setUser(u);
 					c = new DetailsPage(rq);
 				}
 			}
 
 			repaint(c);
 
 		} else {
 			repaint(new StartPage());
 		}
 	}
 
 	public MainController getController() {
 		return mc;
 	}
 
 	public void repaint(Component c) {
 		ApplicationWindow view = null;
 		view = new ApplicationWindow(c);
 		setContent(view);
 		mc.setWindow(view);
 	}
 
 }
