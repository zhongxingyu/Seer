 package be.betty.gwtp.client.presenters;
 
 import com.gwtplatform.dispatch.shared.DispatchAsync;
 import com.gwtplatform.mvp.client.Presenter;
 import com.gwtplatform.mvp.client.View;
 import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
 import com.gwtplatform.mvp.client.annotations.NameToken;
 
 import be.betty.gwtp.client.action.LoginAction;
 import be.betty.gwtp.client.action.SubscribeAction;
 import be.betty.gwtp.client.place.NameTokens;
 
 import com.gwtplatform.mvp.client.proxy.PlaceManager;
 import com.gwtplatform.mvp.client.proxy.ProxyPlace;
 import com.google.inject.Inject;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PasswordTextBox;
 import com.google.gwt.user.client.ui.TextBox;
 import com.gwtplatform.mvp.client.proxy.RevealRootContentEvent;
 
 public class SubscribePresenter extends Presenter<SubscribePresenter.MyView, SubscribePresenter.MyProxy> {
 
 	public interface MyView extends View {
 
 		public AbsolutePanel getAbsoluteSubscribePanel();
 
 		public DockPanel getDockSubscribePanel();
 
 		public Button getSubscibeButton();
 
 		public Label getEmailSubLabel();
 
 		public Label getEmailVerSubLabel();
 
 		public TextBox getUserSubTextbox();
 
 		public PasswordTextBox getPassSubTextbox();
 
 		public PasswordTextBox getPassVerSubTextbox();
 
 		public TextBox getEmailSubTextbox();
 
 		public TextBox getEmailVerSubTextbox();
 
 		public Label getUserSubErrorLabel();
 
 		public Label getPassSubErrorLabel();
 
 		public Label getPassVerSubErrorLabel();
 
 		public Label getEmailSubErrorLabel();
 
 		public Label getEmailVerSubErrorLabel();
 
 	}
 
 	@ProxyCodeSplit
 	@NameToken(NameTokens.subscribe)
 	public interface MyProxy extends ProxyPlace<SubscribePresenter> {
 	}
 
 	@Inject
 	public SubscribePresenter(final EventBus eventBus, final MyView view,
 			final MyProxy proxy) {
 		super(eventBus, view, proxy);
 	}
 
 	@Override
 	protected void revealInParent() {
 		RevealRootContentEvent.fire(this, this);
 	}
 
 	@Override
 	protected void onBind() {
 		super.onBind();
 	}
 
 	public void errorCompleteField(boolean bool, Label lb) {
 		if (bool) {
 			lb.setText("Complete this field");
 		} else
 			lb.setText("");
 	}
 
 	public void errorSameStr(String pass, String passVer, Label lb) {
 		if (!pass.isEmpty() || !passVer.isEmpty()) {
 			if (!pass.equals(passVer)) {
 				lb.setText("Not the same");
 			}
 		}
 		
 	}
 	
 
 	@Override
 	protected void onReset() {
 		super.onReset();
 
 		getView().getSubscibeButton().addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				// TODO Auto-generated method stub
 
 				errorCompleteField(getView().getUserSubTextbox().getText().isEmpty(), getView().getUserSubErrorLabel());
 				errorCompleteField(getView().getPassSubTextbox().getText().isEmpty(), getView().getPassSubErrorLabel());
 				errorCompleteField(getView().getPassVerSubTextbox().getText().isEmpty(), getView().getPassVerSubErrorLabel());
 				errorCompleteField(getView().getEmailSubTextbox().getText().isEmpty(), getView().getEmailSubErrorLabel());
 				errorCompleteField(getView().getEmailVerSubTextbox().getText().isEmpty(), getView().getEmailVerSubErrorLabel());
 				errorSameStr(getView().getPassSubTextbox().getText(), getView().getPassVerSubTextbox().getText(), getView().getPassVerSubErrorLabel());
 				errorSameStr(getView().getEmailSubTextbox().getText(), getView().getEmailVerSubTextbox().getText(), getView().getEmailVerSubErrorLabel());
 				
 				if(
 						getView().getUserSubErrorLabel().getText().isEmpty() &&
 						getView().getPassSubErrorLabel().getText().isEmpty() &&
 						getView().getPassVerSubErrorLabel().getText().isEmpty() &&
 						getView().getEmailSubErrorLabel().getText().isEmpty() &&
 						getView().getEmailVerSubErrorLabel().getText().isEmpty()
 						){
					System.out.println("plus d'erreur");				
 					
				}else
					System.out.println("encore des erreurs");
				
 			}
 		});
 	}
 }
