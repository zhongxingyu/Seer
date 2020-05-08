 /**
  * 
  */
 package ch.ethz.e4mooc.client;
 
 import java.util.LinkedList;
 
 import ch.ethz.e4mooc.client.page.eiffel.EiffelPagePresenter;
 import ch.ethz.e4mooc.client.page.eiffel.EiffelPageView;
 import ch.ethz.e4mooc.client.page.eiffel.EiffelPageViewImpl;
 import ch.ethz.e4mooc.client.page.root.RootPagePresenter;
 import ch.ethz.e4mooc.client.page.root.RootPageView;
 import ch.ethz.e4mooc.client.page.root.RootPageViewImpl;
 
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.SimpleEventBus;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.web.bindery.event.shared.EventBus;
 
 /**
  * @author hce
  *
  */
 public class AppController implements ValueChangeHandler<String> {
 
 	/** the event bus */
 	private EventBus eventBus;
 	
 	/** the view of the root page */
 	RootPageView rootPageView;
 	/** the presenter of the root page */
 	RootPagePresenter rootPagePresenter;
 	
 	/** the view instance of the Eiffel page */
 	EiffelPageView eiffelView;
 	/** the presenter instance of the Eiffel page */
 	EiffelPagePresenter eiffelPagePresenter;
 	
 	
 	/**
 	 * Constructor.
 	 */
 	public AppController(EventBus eventBus) {
 		this.eventBus = eventBus;
 					
 		// bind listeners
 		bind();
 	}
 	
 	
 	/**
 	 * Register to receive History events.
 	 */
 	private void bind() {
 		History.addValueChangeHandler(this);
 	}
 	
 	
 	/**
 	 * Clears the page and displays the root page.
 	 */
 	private void switchToRootPage() {
 		RootPanel.get().clear();
 		rootPageView = new RootPageViewImpl();
		// HACK: for now, we create a new EventBus for each page because we all presenters don't unbind(), meaning that "invisible" presenters keep listening to eventbus
		eventBus = new SimpleEventBus();
 		rootPagePresenter = new RootPagePresenter(this.eventBus, rootPageView);
 		
 		rootPagePresenter.go(RootPanel.get());
 	}
 	
 	
 	/**
 	 * Clears the page and displays the Eiffel page.
 	 * @param projectName the name of the project that should be displayed
 	 */
 	private void switchToEiffelPage(String projectName) {
 		RootPanel.get().clear();
 		eiffelView = new EiffelPageViewImpl();
		// HACK: for now, we create a new EventBus for each page because we all presenters don't unbind(), meaning that "invisible" presenters keep listening to eventbus
		eventBus = new SimpleEventBus();
 		eiffelPagePresenter = new EiffelPagePresenter(this.eventBus, eiffelView, projectName);
 		
 		eiffelPagePresenter.go(RootPanel.get());
 	}
 	
 	
 	@Override
 	public void onValueChange(ValueChangeEvent<String> event) {
 		
 		final String token = event.getValue();
 		
 		// check if the server has a project which name equals the token
 		// if yes, then load the Eiffel page for this project
 		
 		if(token != null) {
 			
 			E4mooc.projectService.hasProject(token, new AsyncCallback<Boolean>() {
 
 				@Override
 				public void onSuccess(Boolean result) {
 					if(result)
 						switchToEiffelPage(token);
 					else
 						switchToRootPage();
 				}
 				
 				@Override
 				public void onFailure(Throwable caught) {
 					// TODO: useful handling ?
 				}
 			});
 		}
 	}
 	
 
 	/**
 	 * Call the go method after everything has been wired up.
 	 */
 	public void go() {
 		
 		// the rootPage should be displayed at first
 		switchToRootPage();
 		
 		// get all the project names available from the server
 		E4mooc.projectService.getAllProjectNames(new AsyncCallback<LinkedList<String>>() {
 
 			// if the URL has a history token equaling a project name, we switch to the Eiffel page for that project
 			@Override
 			public void onSuccess(LinkedList<String> result) {
 				// check if the there's a project with the name of the history token; if yes, switch to Eiffel page
 				if(result.contains(History.getToken()))
 					switchToEiffelPage(History.getToken());
 			}
 			
 			@Override
 			public void onFailure(Throwable caught) {
 				// TODO: useful handling?
 			}
 		});
 	}
 	
 }
