 package treicco.client;
 
 import java.util.Set;
 
 import treicco.shared.TaskProxy;
 import treicco.shared.TaskRequest;
 
 import com.google.gwt.activity.shared.AbstractActivity;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.requestfactory.shared.Receiver;
 import com.google.gwt.requestfactory.shared.Request;
 import com.google.gwt.requestfactory.shared.RequestContext;
 import com.google.gwt.requestfactory.shared.Violation;
 import com.google.gwt.user.client.ui.AcceptsOneWidget;
 
 public class TaskActivity extends AbstractActivity implements TaskPresenter {
 
 	ClientFactory clientFactory;
 	TaskPlace place;
 	AcceptsOneWidget panel;
 
 	TaskView taskView;
 	TaskView taskEdit;
 
 	private static TaskView.EditorDriver editorDriver = GWT.create(TaskView.EditorDriver.class);
 
 	public TaskActivity(TaskPlace place, ClientFactory clientFactory) {
 		this.place = place;
 		this.clientFactory = clientFactory;
 		taskView = clientFactory.getTaskView();
 		taskView.setPresenter(this);
 		taskEdit = clientFactory.getTaskEdit();
 		taskEdit.setPresenter(this);
 	}
 
 	public void start(AcceptsOneWidget _panel, EventBus eventBus) {
 		this.panel = _panel;
 
 		editorDriver.initialize(taskView);
 
 		Request<TaskProxy> fetchRequest = clientFactory.getRequestFactory().taskRequest().findTask(place.getId());
 
		fetchRequest.with(editorDriver.getPaths());
 
 		fetchRequest.to(new Receiver<TaskProxy>() {
 			@Override
 			public void onSuccess(TaskProxy response) {
 				editorDriver.display(response);
 
 				panel.setWidget(taskView);
 			}
 		}).fire();
 	}
 
 	public void startEdit() {
 		editorDriver.initialize(taskEdit);
 
 		Request<TaskProxy> fetchRequest = clientFactory.getRequestFactory().taskRequest().findTask(place.getId());
 
 		fetchRequest.with(editorDriver.getPaths());
 
 		fetchRequest.to(new Receiver<TaskProxy>() {
 			@Override
 			public void onSuccess(TaskProxy response) {
 				TaskRequest context = clientFactory.getRequestFactory().taskRequest();
 				context.update().using(response);
 
 				editorDriver.edit(response, context);
 
 				panel.setWidget(taskEdit);
 			}
 		}).fire();
 	}
 
 	public void stopEdit() {
 		panel.setWidget(taskView);
 	}
 
 	public void commitEdit() {
 		RequestContext context = editorDriver.flush();
 
 		if (editorDriver.hasErrors()) {
 			clientFactory.getLogger().severe("Errors detected locally");
 			return;
 		}
 
 		context.fire(new Receiver<Void>() {
 			@Override
 			public void onSuccess(Void response) {
 				stopEdit();
 			}
 
 			@Override
 			public void onViolation(Set<Violation> errors) {
 				clientFactory.getLogger().severe("Errors detected on the server");
 				editorDriver.setViolations(errors);
 			}
 		});
 	}
 
 	public TaskPlace getPlace() {
 		return place;
 	}
 
 	public void requestImageUploadUrl(Receiver<String> receiver) {
 		Request<String> r = clientFactory.getRequestFactory().imageRequest().createUploadURL();
 
 		r.to(receiver).fire();
 	}
 }
