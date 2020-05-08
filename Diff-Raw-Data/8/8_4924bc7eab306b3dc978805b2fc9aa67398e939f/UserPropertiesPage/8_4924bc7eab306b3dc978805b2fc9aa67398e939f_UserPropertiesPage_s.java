 package ar.edu.itba.paw.grupo1.web.Query;
 
 import org.apache.wicket.markup.html.basic.Label;
 
 import ar.edu.itba.paw.grupo1.model.EntityModel;
 import ar.edu.itba.paw.grupo1.model.User;
 import ar.edu.itba.paw.grupo1.web.QueryListPanel;
 import ar.edu.itba.paw.grupo1.web.Base.BasePage;
 
 @SuppressWarnings("serial")
 public class UserPropertiesPage extends BasePage {
 	
 	public UserPropertiesPage(User user) {
 		
 		add(new Label("page.subtitle", getLocalizer().getString("page.subtitle", this, new EntityModel<User>(User.class, user))));
 		
 		QueryListPanel queryListPanel = new QueryListPanel("queryListPanel", user);
 		add(queryListPanel, queryListPanel.isVisible());
 		
 	}
 }
