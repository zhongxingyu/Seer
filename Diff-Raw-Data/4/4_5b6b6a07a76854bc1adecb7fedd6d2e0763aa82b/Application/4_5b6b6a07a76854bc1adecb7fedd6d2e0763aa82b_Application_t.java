 package ${package}.gwt.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 
import ${package}.gwt.client.service.ApplicationService;
import ${package}.gwt.client.service.ApplicationServiceAsync;
 
 public class Application implements EntryPoint {
 	private Label label = new Label();
 	
     @Override
     public void onModuleLoad() {
 		Button button = new Button("Click me (local)!");
 		Button buttonRpc = new Button("Click me (spring rpc)!");
 	
         RootPanel.get().add(button);
 		RootPanel.get().add(buttonRpc);
         RootPanel.get().add(label);
 
 		button.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				label.setText("Hello world !");
 			}
 		});
 		
 		buttonRpc.addClickHandler(new ClickHandler() {
             @Override
             public void onClick(ClickEvent event) {
                 ApplicationServiceAsync service = GWT.create(ApplicationService.class);
 
 				service.hello(new AsyncCallback<String>() {
 					@Override
                 	public void onSuccess(String message) {
                     	label.setText(message);
                 	}
 
 					@Override
                 	public void onFailure(Throwable caught) {
                     	label.setText("Failed to query server : " + caught.getMessage());
                 	}
 				});
             }
         });
     }
 }
