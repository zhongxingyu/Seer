 package ar.edu.itba.paw.grupo1.web.Register;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
 import org.apache.wicket.markup.html.form.Button;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.PasswordTextField;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.form.upload.FileUpload;
 import org.apache.wicket.markup.html.form.upload.FileUploadField;
 import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.ResourceModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.apache.wicket.validation.IValidatable;
 import org.apache.wicket.validation.validator.AbstractValidator;
 import org.apache.wicket.validation.validator.EmailAddressValidator;
 import org.apache.wicket.validation.validator.PatternValidator;
 import org.apache.wicket.validation.validator.StringValidator;
 
 import ar.edu.itba.paw.grupo1.model.User;
 import ar.edu.itba.paw.grupo1.model.User.UserType;
 import ar.edu.itba.paw.grupo1.repository.UserRepository;
 import ar.edu.itba.paw.grupo1.repository.UserRepository.UserAlreadyExistsException;
 import ar.edu.itba.paw.grupo1.service.HashingService;
 import ar.edu.itba.paw.grupo1.web.WicketSession;
 import ar.edu.itba.paw.grupo1.web.Base.BasePage;
 import ar.edu.itba.paw.grupo1.web.Home.HomePage;
 
 @AuthorizeInstantiation(WicketSession.GUEST)
 public class RegisterPage extends BasePage {
 	
 	@SpringBean
 	private UserRepository users;
 	
 	private String name;
 
 	private String surname;
 
 	private String email;
 
 	private String phone;
 
 	private String username;
 
 	private String password;
 
 	private String passwordConfirm;
 
 	private String realEstateName;
 
 	private UserType userType = UserType.REGULAR;
 
 	private FeedbackPanel feedbackPanel;
 	
 	private transient List<FileUpload> realEstateLogo;
 
 	public RegisterPage() {
 		
 		feedbackPanel = new FeedbackPanel("feedback");
 		feedbackPanel.setVisible(false);
 		add(feedbackPanel);
 		
 		add(new RegisterForm("registerForm"));
 	}
 	
 	private class RegisterForm extends Form<RegisterPage> {
 
 		private FileUploadField fileUploadField;
 		private TextField<String> realStateNameField;
 
 		public RegisterForm(String id) {
 			super(id, new CompoundPropertyModel<RegisterPage>(RegisterPage.this));
 			
 			addStringField("name", 50);
 			addStringField("surname", 50);
 			
 			addStringField("email", 50)
 				.add(EmailAddressValidator.getInstance());
 			
 			addStringField("phone", 20)
 				.add(new PatternValidator("^ *[0-9](-?[ 0-9])*[0-9] *$"));
 			
 			addStringField("username", 50);
 
 			PasswordTextField password = new PasswordTextField("password");
 			add(password);
 			
 			PasswordTextField passwordConfirm = new PasswordTextField("passwordConfirm");
 			add(passwordConfirm);
 			
 			add(new EqualPasswordInputValidator(password, passwordConfirm));
 			
 			EnumChoiceRenderer<UserType> choiceRenderer = new EnumChoiceRenderer<UserType>(this);
 			List<? extends UserType> choices = Arrays.asList(UserType.values());
 			
 			DropDownChoice<UserType> typeChoice = new DropDownChoice<UserType>("userType", choices, choiceRenderer) {
 				
 				@Override
 				protected void onSelectionChanged(UserType newSelection) {
 					fileUploadField.setVisible(newSelection == UserType.REAL_ESTATE);
 					fileUploadField.setRequired(newSelection == UserType.REAL_ESTATE);
 					
 					realStateNameField.setVisible(newSelection == UserType.REAL_ESTATE);
 					realStateNameField.setRequired(newSelection == UserType.REAL_ESTATE);
 				}
 				
 				@Override
 				protected boolean wantOnSelectionChangedNotifications() {
 					return true;
 				}
 				
 			};
 			typeChoice.setRequired(true);
 			
 			add(typeChoice);
 			
 			fileUploadField = new FileUploadField("realEstateLogo");
 			
 			fileUploadField.add(new AbstractValidator<Collection<FileUpload>>() {
 				
 				protected void onValidate(IValidatable<Collection<FileUpload>> validatable) {
 					
 					if (userType == UserType.REGULAR) {
 						return;
 					}
 					
 					for (FileUpload fileUpload : validatable.getValue()) {
 						
 	                    String uploadContentType = fileUpload.getContentType();
 	                    if (uploadContentType == null || (!uploadContentType.contains("gif") 
 	                    		&& !uploadContentType.contains("jpg") && 
 	                    		!uploadContentType.contains("jpeg") && !uploadContentType.contains("png"))) {
 	                    	error(validatable);
 	                    }
 					}
 				}
 			}); 
 			
 			setMultiPart(true);
 			
 			fileUploadField.setVisible(userType == UserType.REAL_ESTATE);
 			fileUploadField.setRequired(userType == UserType.REAL_ESTATE);
 			
 			add(fileUploadField);
 			
 			realStateNameField = new TextField<String>("realEstateName");
 			
 			realStateNameField.setVisible(userType == UserType.REAL_ESTATE);
 			realStateNameField.setRequired(userType == UserType.REAL_ESTATE);
 			
 			realStateNameField.add(StringValidator.maximumLength(50));
 			add(realStateNameField);
 			
 			add(new Button("register", new ResourceModel("register")));
 		}
 		
 		protected TextField<String> addStringField(String name, int maxLength) {
 			return addStringField(name, maxLength, true);
 		}
 		
 		protected TextField<String> addStringField(String name, int maxLength, boolean required) {
 			
 			TextField<String> field = new TextField<String>(name);
 			field.add(StringValidator.maximumLength(50));
 			field.setRequired(required);
 			
 			add(field);
 			
 			return field;
 		}
 		
 		@Override
 		protected void onSubmit() {
 			
			feedbackPanel.setVisible(false);
 			String hash = HashingService.hash(password);
 			
 			try {
 				
 				User user = null;
 				if (userType == UserType.REAL_ESTATE) {
 					FileUpload file = realEstateLogo.get(0);
 					user = new User(name, surname, email, phone, username, hash, realEstateName, file.getContentType(), file.getBytes());
 				} else {
 					user = new User(name, surname, email, phone, username, hash);
 				}
 				
 				users.register(user);
 			} catch (UserAlreadyExistsException e) {
 				error(getString("username.taken"));
				feedbackPanel.setVisible(true);
				return;
 			}
 			
 			setResponsePage(HomePage.class);
 		}
 		
 		@Override
 		protected void onError() {
 			feedbackPanel.setVisible(true);
 		}
 	}
 }
