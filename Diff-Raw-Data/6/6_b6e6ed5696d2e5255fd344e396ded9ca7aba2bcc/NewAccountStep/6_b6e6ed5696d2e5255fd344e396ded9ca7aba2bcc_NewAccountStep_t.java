 package au.org.scoutmaster.views.wizards.setup;
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.vaadin.teemu.wizards.WizardStep;
 
 import au.com.vaadinutils.crud.ValidatingFieldGroup;
 import au.org.scoutmaster.dao.DaoFactory;
 import au.org.scoutmaster.domain.access.User;
 import au.org.scoutmaster.util.SMMultiColumnFormLayout;
 import au.org.scoutmaster.validator.PasswordValidator;
 import au.org.scoutmaster.validator.UsernameValidator;
 
 import com.vaadin.data.validator.EmailValidator;
 import com.vaadin.shared.ui.label.ContentMode;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Notification;
 import com.vaadin.ui.PasswordField;
 import com.vaadin.ui.TextField;
 
 public class NewAccountStep extends SingleEntityStep<User> implements WizardStep
 {
 	@SuppressWarnings("unused")
 	private static Logger logger = Logger.getLogger(NewAccountStep.class);
 
 	private TextField username;
 
 	private PasswordField password;
 
 	private PasswordField confirmPassword;
 
 	private TextField emailAddress;
 
 	private TextField confirmEmailAddress;
 
 	public NewAccountStep(SetupWizardView setupWizardView)
 	{
 		super(setupWizardView, new DaoFactory().getUserDao(), User.class);
 	}
 
 	@Override
 	public String getCaption()
 	{
 		return "Create Account";
 	}
 
 
 	@Override
 	protected Component getContent(ValidatingFieldGroup<User> fieldGroup)
 	{
		SMMultiColumnFormLayout<User> formLayout = new SMMultiColumnFormLayout<>(2, fieldGroup);
 		formLayout.setWidth("600px");
 		formLayout.setColumnFieldWidth(0, 400);
		formLayout.setColumnFieldWidth(1, 20);
 
		formLayout.colspan(2);
 		Label label = new Label("<h1>Start by creating an account to login to Scoutmaster.</h1>");
 		label.setContentMode(ContentMode.HTML);
 		formLayout.bindLabel(label);
 
 		username = formLayout.bindTextField("Username:", "username");
 		username.setInputPrompt("Enter a username");
 		username.addValidator(new UsernameValidator());
 		username.setRequired(true);
 
 		// Create the password input field
 		password = formLayout.bindPasswordField("Password:", "password");
 		password.setDescription("Enter a password  containing at least 2 digits and 2 non alphanumeric characters.");
 		password.addValidator(new PasswordValidator("Password"));
 		password.setRequired(true);
 
 		// Create the confirm password input field but we don't bind it.
 		confirmPassword = formLayout.addPasswordField("Confirm Password:");
 		confirmPassword.addValidator(new PasswordValidator("Confirm Password"));
 		confirmPassword.setRequired(true);
 		
 
 		// Create the email address input field
 		emailAddress = formLayout.bindTextField("Email Address:", "emailAddress");
 		emailAddress.addValidator(new EmailValidator("Enter your email address."));
 		emailAddress.setRequired(true);
 		emailAddress.setNullRepresentation("");
 
 		// Create the confirmEmail input field
 		confirmEmailAddress = formLayout.addTextField("Confirm Email Address:");
 		confirmEmailAddress.addValidator(new EmailValidator("Enter your email address."));
 		confirmEmailAddress.setRequired(true);
 		confirmEmailAddress.setNullRepresentation("");
 
 		// focus the username field when user arrives to the login view
 		username.focus();
 
 		return formLayout;
 	}
 
 	@Override
 	public boolean validate()
 	{
 		boolean valid = false;
 		if (confirmPassword.getValue().equals(password.getValue()))
 		{
 			if (confirmEmailAddress.getValue().equals(emailAddress.getValue()))
 			{
 				valid = super.validate();
 			}
 			else
 			{
 				// Non matching email addresses clear the password field
 				// and refocuses it
 				this.confirmEmailAddress.focus();
 				Notification.show("The email and confirm email fields do not match.");
 			}
 		}
 		else
 		{
 			// Non matching password field
 			this.password.setValue(null);
 			this.confirmPassword.setValue(null);
 			this.password.focus();
 			Notification.show("The password and confirm password fields do not match.");
 
 		}
 		return valid;
 	}
 
 	@Override
 	protected void initEntity(User entity)
 	{
 		// No Op
 
 	}
 
 	@Override
 	protected User findEntity()
 	{
 		User user = null;
 		List<User> users = new DaoFactory().getUserDao().findAll();
 		if (users.size() > 1)
 			throw new IllegalStateException(
 					"More than one user has been found which is not valid during initial setup.");
 		if (users.size() == 1)
 			user = users.get(0);
 		return user;
 	}
 
 }
