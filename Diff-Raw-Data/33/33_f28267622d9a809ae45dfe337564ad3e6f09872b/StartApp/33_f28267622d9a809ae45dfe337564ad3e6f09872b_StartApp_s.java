 package org.gwtapp.startapp.client;
 
 import org.gwtapp.core.client.ui.DelegatedPanel;
 import org.gwtapp.startapp.client.ui.feedback.FeedbackPanel;
 import org.gwtapp.startapp.client.ui.user.register.UploadDownloadTemplatePanel;
 import org.gwtapp.startapp.client.ui.user.register.UserRegisterTemplatePanel;
 import org.gwtapp.startapp.rpc.data.user.register.UserRegisterModel;
 
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.DeferredCommand;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasValue;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextBox;
 
 public class StartApp extends StartAppEntryPoint {
 
 	public final static String TEMPLATES_DIV = "templates";
 
 	private final UserRegisterTemplatePanel templatePanel = new UserRegisterTemplatePanel();
 	private final FeedbackPanel feedback = new FeedbackPanel();
 
 	@Override
 	public void onStartAppModuleLoad() {
		A a = new A();
		RootPanel.get().add(a);
 		try {
 			template();
 			uploadownload();
 			feedback();
 		} finally {
 			hideLoadingIndicator();
 		}
 	}
 
 	private void template() {
 		RootPanel.get(TEMPLATES_DIV).add(templatePanel);
 	}
 
 	private void uploadownload() {
 		String rpcName = "userregister";
 		UserRegisterModel value = (UserRegisterModel) rpc.getValue(rpcName);
 		new UploadDownloadTemplatePanel(value, "ud").attach();
 	}
 
 	private void feedback() {
 		RootPanel anchor = RootPanel.get("feedbackanchor");
 		if (anchor != null) {
 			anchor.add(feedback);
 		}
 	}
 
 	private void hideLoadingIndicator() {
 		DeferredCommand.addCommand(new Command() {
 			@Override
 			public void execute() {
 				RootPanel indicator = RootPanel.get("laodingindicator");
 				if (indicator != null) {
 					indicator.setVisible(false);
 				}
 			}
 		});
 	}
 
 	class A extends DelegatedPanel<Double, String> {
 
 		private final static double eps = 1e-3;
 		private final TextBox tb = new TextBox();
 		private final HTML state = new HTML();
 		private final HTML thisHandlerState = new HTML();
 		private final HTML delegatedHandlerState = new HTML();
 		private int thisCount = 0;
 		private int delegatedCount = 0;
 
 		public A() {
 			add(tb);
 			add(state);
 			add(delegatedHandlerState);
 			add(thisHandlerState);
 			addValueChangeHandler(new ValueChangeHandler<Double>() {
 				@Override
 				public void onValueChange(ValueChangeEvent<Double> event) {
 					thisHandlerState.setText("Double(" + (++thisCount) + "):"
 							+ event.getValue());
 				}
 			});
 			tb.addValueChangeHandler(new ValueChangeHandler<String>() {
 				@Override
 				public void onValueChange(ValueChangeEvent<String> event) {
 					delegatedHandlerState.setText("String("
 							+ (++delegatedCount) + "):" + event.getValue());
 				}
 			});
 		}
 
 		@Override
 		public HasValue<String> getDelegated() {
 			return tb;
 		}
 
 		@Override
 		public Double convertToX(String value) {
			Double doubleValue = null;
 			try {
 				doubleValue = Double.parseDouble(value);
 				state.setText("OK: " + doubleValue);
 			} catch (Exception e) {
 				state.setText("Invalid value: " + value);
 			}
 			return doubleValue;
 		}
 
 		@Override
 		public String convertToY(Double value) {
 			return "" + value;
 		}
 
 		@Override
 		public boolean isDelegateToUpdate(Double oldValue, Double newValue) {
 			if (oldValue == null ^ newValue == null) {
 				return true;
 			} else if (oldValue == null && newValue == null) {
 				return false;
 			} else {
 				return Math.abs(oldValue - newValue) > eps;
 			}
 		}
 	}
 }
