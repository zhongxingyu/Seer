 package edu.ycp.CS320.client;
 
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.LayoutPanel;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 
 
 
 
 /**
  * @author drew
  *this is the homepage which the logged in user will see after clicking log in
  *right now it is lonely and needs features
  */
 public class HomePageView extends Composite{
 	private Button btnForms = new Button();
 	private FormView fv = new FormView();
 	private Button btnApparatus;
 	private ApparatusView apparatusView = new ApparatusView();
 	private Button btnCalendar;
 	private theCalendar cal = new theCalendar();
 	
 	public HomePageView(){
 
 		final LayoutPanel layout = new LayoutPanel();
 
 		initWidget(layout);		
 		Label hi = new Label("HOMEPAGE...\nAdd Links and Features here");		
 		layout.add(hi);
 		
 		btnForms = new Button("Forms");
 		layout.add(btnForms);
 		layout.setWidgetLeftWidth(btnForms, 21.0, Unit.PX, 120.0, Unit.PX);
		layout.setWidgetTopHeight(btnForms, 141.0, Unit.PX, 30.0, Unit.PX);
 		
 		btnForms.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent click){
 				layout.clear();
 				layout.add(fv);
 			}
 		});
 		
 
 		btnApparatus = new Button("Apparatus");
 		layout.add(btnApparatus);
 		layout.setWidgetLeftWidth(btnApparatus, 21.0, Unit.PX, 120.0, Unit.PX);
		layout.setWidgetTopHeight(btnApparatus, 96.0, Unit.PX, 30.0, Unit.PX);
 		
 		btnApparatus.addClickHandler(new ClickHandler() {
 			/* (non-Javadoc)
 			 * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
 			 * 
 			 * this method creates a local User object this is used to login to the system
 			 * an async callback is created that will confirm if the RPC was successful or not
 			 * 
 			 */
 			@Override
 			public void onClick(ClickEvent event) {
 				System.out.println("Apparatus!");
 				layout.clear();
 				layout.add(apparatusView);				
 			}		
 			
 		});
 		
 		
 		btnCalendar = new Button("Calendar");
 		layout.add(btnCalendar);
		layout.setWidgetLeftWidth(btnCalendar, 21.0, Unit.PX, 120.0, Unit.PX);
 		layout.setWidgetTopHeight(btnCalendar, 48.0, Unit.PX, 30.0, Unit.PX);
 		
 		btnCalendar.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				layout.clear();
 				layout.add(cal);				
 			}			
 		});		
 	}
 }
