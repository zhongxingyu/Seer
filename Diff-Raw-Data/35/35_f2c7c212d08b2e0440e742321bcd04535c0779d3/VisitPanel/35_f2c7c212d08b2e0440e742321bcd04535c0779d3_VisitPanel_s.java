 package layoutPanels;
 
 import ict4mpower.AppSession;
 
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.PropertyModel;
 
 public class VisitPanel extends Panel {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public VisitPanel(String id) {
 		super(id);
 		AppSession session = (AppSession) getSession();
		DropDownChoice<String> visits = new DropDownChoice<String>("visitList", new PropertyModel(this, "session.CurrentVisit"), session.getAllVisits());
 		add(visits);
 		add( new Label("visitStage", "VISIT SIGNED"));
 	}
 	
 }
