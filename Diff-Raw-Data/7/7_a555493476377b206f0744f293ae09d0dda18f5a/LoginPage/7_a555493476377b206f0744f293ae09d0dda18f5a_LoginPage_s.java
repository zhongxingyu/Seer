 package com.ecom.web.login;
 
 import org.apache.wicket.markup.html.form.PasswordTextField;
 import org.apache.wicket.markup.html.form.StatelessForm;
 import org.apache.wicket.markup.html.form.SubmitLink;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.CompoundPropertyModel;
 
 import com.ecom.web.main.EcomSession;
 import com.ecom.web.main.GenericTemplatePage;
 
 public class LoginPage extends GenericTemplatePage {
 
 	private static final long serialVersionUID = 6391262326443881229L;
 
 	public LoginPage() {
 		super();
 
 		LoginRequest login = new LoginRequest();
 		CompoundPropertyModel<LoginRequest> loginModel = new CompoundPropertyModel<LoginRequest>(login);
 		StatelessForm<LoginRequest> loginForm = new StatelessForm<LoginRequest>("loginForm", loginModel);
 		TextField<String> userName = new TextField<String>("userName");
 		PasswordTextField password = new PasswordTextField("password");
 		loginForm.add(userName);
 		loginForm.add(password);
 		
 		
 		add(new SubmitLink("submitLogin", loginForm) {
 
 			private static final long serialVersionUID = 1969220803824994712L;
 
 			@Override
 			public void onSubmit() {
 
 				LoginRequest loginRequest = (LoginRequest) this.getForm().getDefaultModelObject();
 				if (loginRequest != null && loginRequest.getUserName() != null) {
 					EcomSession session = (EcomSession) EcomSession.get();					
 					if (session.signIn(loginRequest.getUserName(), loginRequest.getPassword())) {
 						session.setUserName(loginRequest.getUserName());
 						setResponsePage(UserDetailPage.class);
 					}
 						
 				} else {
 					error("User could not be authenticated due to invalid username or password");
 				}
 
 			}
 
 			@Override
 			public void onError() {
 				error("User could not be authenticated due to invalid username or password");
 			}
 		});
 
 		add(new FeedbackPanel("feedback"));
 		add(loginForm);
 	}
 
 }
