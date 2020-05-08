 package com.madalla.webapp.user;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.MarkupContainer;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.ResourceModel;
 import org.apache.wicket.model.StringResourceModel;
 import org.emalan.cms.bo.SiteData;
 import org.emalan.cms.bo.security.IUser;
 import org.emalan.cms.bo.security.UserData;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.madalla.service.ApplicationService;
 import com.madalla.util.security.ICredentialHolder;
 import com.madalla.util.security.SecureCredentials;
 import com.madalla.webapp.CmsPanel;
 import com.madalla.webapp.CmsSession;
 import com.madalla.webapp.components.email.EmailFormPanel;
 import com.madalla.webapp.login.LoginPanel;
 import com.madalla.webapp.scripts.JavascriptResources;
 import com.madalla.wicket.animation.AnimationOpenSlide;
 
 public class UserLoginPanel extends CmsPanel {
 
 	private static final long serialVersionUID = 5349334518027160490L;
 	private static final Logger log = LoggerFactory.getLogger(UserLoginPanel.class);
 
 	public UserLoginPanel(final String id) {
 		this(id, new SecureCredentials());
 	}
 
 	public UserLoginPanel(final String id, final String username) {
 		this(id, new SecureCredentials().setUsername(username));
 	}
 
 	private UserLoginPanel(final String id, final ICredentialHolder credentials){
 		super(id);
 
 		final CmsSession session = (CmsSession) getSession();
 
 		final Component loginInfo = new Label("loginInfo", new StringResourceModel("login.info",new Model<IUser>(getSessionDataService().getUser()))){
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void onBeforeRender() {
 				setOutputMarkupId(true);
 				setVisibilityAllowed(true);
 				if (!session.isLoggedIn()){
 					setVisible(false);
 				}
 				super.onBeforeRender();
 			}
 
 		};
 		add(loginInfo);
 
 		final Component panel = new LoginPanel("signInPanel", credentials){
             private static final long serialVersionUID = 1L;
 
            	@Override
 			protected void preSignIn(String username) {
 				log.trace("preSignIn - username=" + username);
 
 				UserData user = getRepositoryService().getUser(username);
 				getAppSession().setUser(user);
 
         		preLogin(username);
 			}
 
             @Override
             public boolean signIn(String username, String password) {
             	CmsSession session = (CmsSession) getSession();
             	return session.signIn(username, password);
             }
 
 			@Override
 			protected void onBeforeRender() {
 				setOutputMarkupId(true);
 				if (session.isLoggedIn()){
 					setEnabled(false);
 				} else {
 					setEnabled(true);
 				}
 				super.onBeforeRender();
 			}
 
 
         };
         add(panel);
 
 		add(new AjaxFallbackLink<Object>("logout"){
 
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void onClick(AjaxRequestTarget target) {
 				session.signOut();
 				target.add(this);
 				target.add(loginInfo);
 				target.add(panel);
 			}
 
 			@Override
 			protected void onBeforeRender() {
 				setOutputMarkupId(true);
 				setVisibilityAllowed(true);
 				if (!session.isLoggedIn()){
 					setVisible(false);
 				}
 				super.onBeforeRender();
 			}
 
 		});
 
 		Component emailLink = new Label("emailLink", new ResourceModel("label.support"));
 		add(emailLink);
 
 		MarkupContainer emailDiv = new WebMarkupContainer("emailDiv");
 		add(emailDiv);
 
 		SiteData site = getRepositoryService().getSiteData();
 		Component emailForm = new EmailFormPanel("supportEmail", "Support email - sent from " + site.getName()){
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected boolean sendEmail(ApplicationService emailSender, SiteData site, String body, String subject) {
 				return emailSender.sendEmail(subject, body);
 			}
 
 		};
 		emailDiv.add(emailForm);
 
 		emailLink.add(new AnimationOpenSlide("onclick", emailDiv, 28,"em"));
 
 	}
 	
 	@Override
 	public void renderHead(IHeaderResponse response) {
 		response.renderJavaScriptReference(JavascriptResources.ANIMATOR);
 	}
 
 	protected void preLogin(String username){
 
 	}
 
 }
