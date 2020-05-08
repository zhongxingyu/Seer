 package be.betty.gwtp.client.presenters;
 
 import be.betty.gwtp.client.action.DeleteProjectAction;
 import be.betty.gwtp.client.action.DeleteProjectActionResult;
 import be.betty.gwtp.client.event.ProjectListModifyEvent;
 import be.betty.gwtp.client.model.Project;
 import be.betty.gwtp.client.place.NameTokens;
 
 import com.gwtplatform.dispatch.shared.DispatchAsync;
 import com.gwtplatform.mvp.client.PresenterWidget;
 import com.gwtplatform.mvp.client.View;
 import com.gwtplatform.mvp.client.proxy.PlaceRequest;
 import com.google.inject.Inject;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.storage.client.Storage;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.Label;
 
 public class SingleProjectPresenter extends
 		PresenterWidget<SingleProjectPresenter.MyView> {
 
 
 	
 	public interface MyView extends View {
 
 		public Hyperlink getProject();
 		public Label getLabel();
 		public Button getDeleteButton();
 	}
 
 	private Project projectModel;
 	private Storage stockStore;
 
 	@Inject
 	public SingleProjectPresenter(final EventBus eventBus, final MyView view) {
 		super(eventBus, view);
 		stockStore = Storage.getLocalStorageIfSupported();
 	}
 
 	@Inject DispatchAsync dispatcher;
 	
 	@Override
 	protected void onBind() {
 		super.onBind();
 		
 		getView().getDeleteButton().addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				// TODO : c'est vrmt cradement ecrit tt ca.. !! 
 				Window.alert("you are about to delete project id "+projectModel.getId());
 				String sess_id ="";
 				if (stockStore != null )
 					sess_id = stockStore.getItem("session_id");
 				DeleteProjectAction action = new DeleteProjectAction(projectModel.getId(), sess_id);
 				dispatcher.execute(action, new AsyncCallback<DeleteProjectActionResult>() {
 
 					@Override
 					public void onFailure(Throwable caught) {
 						// TODO Auto-generated method stub
 						
 					}
 
 					@Override
 					public void onSuccess(DeleteProjectActionResult result) {
						getEventBus().fireEvent(new ProjectListModifyEvent());  //TODO: add the project in parameter
 						
 					}
 				});
				
 			}
 		});
 	}
 
 	public void init(Project project) {
 		this.projectModel = project;
 		getView().getProject().setText(project.getName());
 		
 		
 	}
 }
