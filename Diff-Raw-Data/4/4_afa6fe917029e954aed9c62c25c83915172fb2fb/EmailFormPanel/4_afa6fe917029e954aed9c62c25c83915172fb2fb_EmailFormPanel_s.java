 package com.madalla.webapp.email;
 
 import java.text.MessageFormat;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.wicket.Component;
 import org.apache.wicket.RequestCycle;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.behavior.HeaderContributor;
 import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
 import org.apache.wicket.markup.ComponentTag;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.FormComponent;
 import org.apache.wicket.markup.html.form.RequiredTextField;
 import org.apache.wicket.markup.html.form.TextArea;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.panel.ComponentFeedbackPanel;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.markup.html.resources.CompressedResourceReference;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.protocol.http.ClientProperties;
 import org.apache.wicket.protocol.http.WebRequestCycle;
 import org.apache.wicket.protocol.http.request.WebClientInfo;
 import org.apache.wicket.util.value.ValueMap;
 import org.apache.wicket.validation.IValidatable;
 import org.apache.wicket.validation.IValidationError;
 import org.apache.wicket.validation.ValidationError;
 import org.apache.wicket.validation.validator.AbstractValidator;
 import org.apache.wicket.validation.validator.EmailAddressValidator;
 
 import com.madalla.email.IEmailSender;
 import com.madalla.email.IEmailServiceProvider;
 import com.madalla.util.captcha.CaptchaUtils;
 import com.madalla.webapp.scripts.scriptaculous.Scriptaculous;
 import com.madalla.wicket.form.AjaxValidationStyleRequiredTextField;
 
 public class EmailFormPanel extends Panel {
     private static final long serialVersionUID = -1643728343421366820L;
     private static final CompressedResourceReference JS_PROTOTYPE = new CompressedResourceReference(Scriptaculous.class, "prototype.js");
     private static final CompressedResourceReference JS_EFFECTS = new CompressedResourceReference(Scriptaculous.class, "effects.js");
 
     private Log log = LogFactory.getLog(this.getClass());
     //private String imagePass = CaptchaUtils.randomString(4, 6);
     private Integer first = CaptchaUtils.randomInteger(1, 20);
     private Integer second = CaptchaUtils.randomInteger(1, 12);
     
     private final ValueMap properties = new ValueMap();
     String subject ;
 
     
     public class EmailForm extends Form {
         private static final long serialVersionUID = -2684823497770522924L;
         
         //private final CaptchaImageResource captchaImageResource;
         
         public EmailForm(String id) {
             super(id);
             
             //captchaImageResource = new CaptchaImageResource(imagePass);
     
             FeedbackPanel nameFeedback = new FeedbackPanel("nameFeedback");
             add(nameFeedback);
             add(new AjaxValidationStyleRequiredTextField("name",new PropertyModel(properties,"name"), nameFeedback));
             
             FeedbackPanel emailFeedback = new FeedbackPanel("emailFeedback");
             add(emailFeedback);
             TextField email = new AjaxValidationStyleRequiredTextField("email",new PropertyModel(properties,"email"), emailFeedback);
             email.add(EmailAddressValidator.getInstance());
             add(email);
             
             TextArea comment = new TextArea("comment",new PropertyModel(properties,"comment"));
             add(comment);
             
             add(new Label("captchaString", first+" + "+second+" = "));
             
             FeedbackPanel passwordFeedback = new FeedbackPanel("passwordFeedback");
             add(passwordFeedback);
             RequiredTextField password = new AjaxValidationStyleRequiredTextField("password", new PropertyModel(properties, "password"), passwordFeedback){
 				private static final long serialVersionUID = -108228073455105029L;
 				protected final void onComponentTag(final ComponentTag tag) {
                         super.onComponentTag(tag);
                         // clear the field after each render
                         //tag.put("value", "");
                 }
             };
             password.add(new AbstractValidator(){
 				private static final long serialVersionUID = 2572094991300700912L;
 				protected void onValidate(IValidatable validatable) {
                     String password = (String)validatable.getValue();
                     try {
                         int answer = Integer.parseInt(password);
                         if (answer != first.intValue() + second.intValue()){
                         	log.debug("onValidate - entered:"+password+" should be:"+first+"+"+second);
                             IValidationError error = new ValidationError().addMessageKey("message.captcha");
                             validatable.error(error);
                         }
                     } catch (Exception e){
                     	IValidationError error = new ValidationError().addMessageKey("message.captcha.error");
                     	validatable.error(error);
                     	log.debug("password validate Exception.",e);
                     }
 //                    if (!imagePass.equals(password)) {
 //                    	log.debug("onValidate - entered:"+password+" should be:"+imagePass);
 //                        IValidationError error = new ValidationError().addMessageKey("message.captcha");
 //                        validatable.error(error);
 //                        //"You entered '" + password + "' You should have entered '" + imagePass + "'"
 //                    }
 //                    captchaImageResource.invalidate();
                 }
             });
             add(password);
             
         }
 
 		@Override
 		protected void onSubmit() {
 			if (!isSubmitted()) {
 				log.debug("onSumit called- sending email.");
 				if (sendEmail()) {
 					info("Email sent successfully");
 				} else {
 					error("Failed to send email!");
 				}
 			}
 		}
     }
     
     public EmailFormPanel(final String id, final String subject) {
         super(id);
         this.subject = subject;
         
         Form form = new EmailForm("emailForm");
         form.setOutputMarkupId(true);
 
         final FeedbackPanel feedbackPanel = new ComponentFeedbackPanel("feedback",form);
         feedbackPanel.setOutputMarkupId(true);
         form.add(feedbackPanel);
         
         AjaxButton submitButton = new IndicatingAjaxButton("submit", form){
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void onSubmit(AjaxRequestTarget target, Form form) {
 				log.debug("Ajax onsubmit called.");
 				target.addComponent(feedbackPanel);
 				if (sendEmail()){
 					form.info(getString("message.success"));
 	            } else {
 	            	form.error(getString("message.fail"));
 	            }
 				//setResponsePage(this.findPage());
 			}
 			
 			@Override
 			protected void onError(final AjaxRequestTarget target, Form form) {
 				log.debug("Ajax onerror called");
 				target.addComponent(feedbackPanel);
 	           	form.visitChildren(new Component.IVisitor() {
 					public Object component(Component component) {
 	           			log.debug("formVisitor="+component);
 	           			if (component instanceof FormComponent) {
 	           				FormComponent formComponent = (FormComponent) component;
 	           				if (!formComponent.isValid()){
 	           					target.addComponent(formComponent);
 	           					log.debug("Component is invalid. Component MarkupId="+formComponent.getMarkupId()+". Message is " +formComponent.getFeedbackMessage().getMessage());
 	           				}
 	           			} else if (component instanceof ComponentFeedbackPanel){
 	           				log.debug("Ajax onerror - adding feedback to target.");
 	           				ComponentFeedbackPanel feedback = (ComponentFeedbackPanel) component;
 	           				target.addComponent(feedback);
 	           			}
 	           			return null;
 					}
 	           	});
 
 			}
 			
         	
         };
         submitButton.setOutputMarkupId(true);
         form.setDefaultButton(submitButton);
         form.add(submitButton);
         
         add(form);
         
         add(HeaderContributor.forJavaScript(JS_PROTOTYPE));
         add(HeaderContributor.forJavaScript(JS_EFFECTS));
     }
     
     private boolean sendEmail(){
 		
         IEmailSender email = getEmailSender();
         String body = getEmailBody(properties.getString("name"),properties.getString("email"),properties.getString("comment"));
         return email.sendEmail(subject, body);
     }
     
 
     
     private String getEmailBody(String from, String email, String comment){
         Object[] args = {from,email,(comment == null)?"":comment};
         String body = MessageFormat.format(getEmailtemplate(),args);
         RequestCycle requestCycle = getRequestCycle();
         if (requestCycle instanceof WebRequestCycle){
             WebClientInfo clientInfo = (WebClientInfo)((WebRequestCycle) requestCycle).getClientInfo();
             ClientProperties cp = clientInfo.getProperties();
 
             StringBuffer sb = new StringBuffer(body).append(System.getProperty("line.separator"));
             sb.append("NavigatorAppName : ").append(cp.getNavigatorAppName()).append(System.getProperty("line.separator"));
             sb.append("NavigatorAppCodeName : ").append(cp.getNavigatorAppCodeName()).append(System.getProperty("line.separator"));
             sb.append("NavigatorAppVersion : ").append(cp.getNavigatorAppVersion()).append(System.getProperty("line.separator"));
             sb.append("BrowserVersionMajor : ").append(cp.getBrowserVersionMajor()).append(System.getProperty("line.separator"));
             sb.append("BrowserVersionMinor : ").append(cp.getBrowserVersionMinor()).append(System.getProperty("line.separator"));
             body = sb.toString();
         }
         return body;
     }
     
     private String getEmailtemplate(){
         StringBuffer sb = new StringBuffer("Email sent from emalan.com website...").append(System.getProperty("line.separator"));
         sb.append("From: {0} ({1})").append(System.getProperty("line.separator"));
         sb.append("Comment: {2}").append(System.getProperty("line.separator"));
         return sb.toString();
     }
     
     protected IEmailSender getEmailSender(){
     	return ((IEmailServiceProvider)getApplication()).getEmailSender();
     }
     
 }
