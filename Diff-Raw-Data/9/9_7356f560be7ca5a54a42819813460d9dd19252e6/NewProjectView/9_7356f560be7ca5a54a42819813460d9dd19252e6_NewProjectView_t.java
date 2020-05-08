 package be.betty.gwtp.client.views;
 
 import be.betty.gwtp.client.presenters.NewProjectPresenter;
 import be.betty.gwtp.client.presenters.NewProjectPresenter.MyView;
 
 import com.gwtplatform.mvp.client.PopupViewImpl;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.shared.EventBus;
 import com.google.inject.Inject;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.FormPanel;
 import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
 
 public class NewProjectView extends PopupViewImpl implements
 		NewProjectPresenter.MyView {
 
 	private final Widget widget;
 
 	public interface Binder extends UiBinder<Widget, NewProjectView> {
 	}
 
 	@Inject
 	public NewProjectView(final EventBus eventBus, final Binder binder) {
 		super(eventBus);
 		widget = binder.createAndBindUi(this);
 		
		//TODO: the first "setAction()" is not working on tomcat (but does on eclipse), 
		// and but the second one is ugly!! but works ..
		//formPanel.setAction(GWT.getModuleBaseURL()+"upload");
		formPanel.setAction("http://betty.sytes.net/betty/upload");
 		formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
 		formPanel.setMethod(FormPanel.METHOD_POST);

 		
 	}
 
 	@Override
 	public Widget asWidget() {
 		return widget;
 	}
 	
 	
 	@Override
 	public Button getCancel_button() {
 		return cancel_button;
 	}
 	
 	@UiField Button cancel_button;
 	@UiField TextBox project_name;
 	@UiField Button send_button;
 	@UiField FormPanel formPanel;
 	
 	@Override
 	public TextBox getProject_name() {
 		return project_name;
 	}
 
 	@Override
 	public Button getSend_button() {
 		return send_button;
 	}
 
 	@Override
 	public FormPanel getFormPanel() {
 		return formPanel;
 	}
 
 }
