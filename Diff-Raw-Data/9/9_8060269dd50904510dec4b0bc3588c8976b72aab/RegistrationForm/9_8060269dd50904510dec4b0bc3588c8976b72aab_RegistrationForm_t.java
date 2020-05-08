 package au.com.showcase.application.client.scroll;
 
 import au.com.showcase.application.client.bundle.ApplicationResources;
 import au.com.showcase.application.client.bundle.DecoratedPopupPanel;
 import au.com.showcase.application.client.ui.event.AlphabetTextBoxBlurHandler;
 import au.com.showcase.application.client.ui.event.AlphabetTextBoxFocusHandler;
 import au.com.showcase.application.client.ui.event.ConfirmPasswordTextBoxBlurHandler;
 import au.com.showcase.application.client.ui.event.ConfirmPasswordTextBoxFocusHandler;
 import au.com.showcase.application.client.ui.event.ListBoxBlurHandler;
 import au.com.showcase.application.client.ui.event.ListBoxFocusHandler;
 import au.com.showcase.application.client.ui.event.PasswordTextBoxBlurHandler;
 import au.com.showcase.application.client.ui.event.PasswordTextBoxFocusHandler;
 import au.com.showcase.application.client.ui.event.PasswordTextBoxKeyPressHandler;
 import au.com.showcase.application.client.ui.event.TextBoxBlurHandler;
 import au.com.showcase.application.client.ui.event.TextBoxFocusHandler;
 import au.com.showcase.application.client.ui.event.UsernameTextBoxBlurHandler;
 import au.com.showcase.application.client.ui.event.UsernameTextBoxFocusHandler;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.DivElement;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.PasswordTextBox;
 import com.google.gwt.user.client.ui.SubmitButton;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 
 public class RegistrationForm extends Composite {
 
 	private static RegistrationFormUiBinder uiBinder = GWT
 			.create(RegistrationFormUiBinder.class);
 
 	interface RegistrationFormUiBinder extends
 			UiBinder<Widget, RegistrationForm> {
 	}
 
 	public RegistrationForm() {
 		initWidget(uiBinder.createAndBindUi(this));
 		ApplicationResources.INSTANCE.registrationFormStyle().ensureInjected();
 	}
 
 	@UiField
 	TextBox firstName;
 
 	@UiField
 	TextBox lastName;
 
 	@UiField
 	TextBox username;
 
 	@UiField
 	PasswordTextBox password;
 
 	@UiField
 	PasswordTextBox confirmPassword;
 
 	@UiField
 	TextBox dobDate;
 
 	@UiField
 	TextBox dobYear;
 
 	@UiField
 	TextBox mobileNumber;
 
 	@UiField
 	TextBox emailAddress;
 
 	@UiField
 	TextBox captchaText;
 
 	@UiField
 	ListBox dobMonth;
 
 	@UiField
 	ListBox gender;
 
 	@UiField
 	ListBox location;
 
 	@UiField
 	CheckBox agreement;
 
 	@UiField
 	SubmitButton submit;
 
 	@UiField
 	Label nameError;
 
 	@UiField
 	Label usernameError;
 
 	@UiField
 	Label passwordError;
 
 	@UiField
 	Label confirmPasswordError;
 
 	@UiField
 	Label dobError;
 
 	@UiField
 	Label genderError;
 
 	@UiField
 	Label mobileNumberError;
 
 	@UiField
 	Label emailAddressError;
 
 	@UiField
 	Label locationError;
 
 	@UiField
 	Label captchaTextError;
 
 	@UiField
	Label agreementError;

	@UiField
 	DivElement dobMonthBlock;
 
 	@UiField
 	DivElement genderBlock;
 
 	@UiField
 	DivElement locationBlock;
 
 	AlphabetTextBoxBlurHandler firstNameBlurHandler = new AlphabetTextBoxBlurHandler();
 	AlphabetTextBoxFocusHandler firstNameFocusHandler = new AlphabetTextBoxFocusHandler();
 	AlphabetTextBoxBlurHandler lastNameBlurHandler = new AlphabetTextBoxBlurHandler();
 	AlphabetTextBoxFocusHandler lastNameFocusHandler = new AlphabetTextBoxFocusHandler();
 	UsernameTextBoxBlurHandler usernameBlurHandler = new UsernameTextBoxBlurHandler();
 	UsernameTextBoxFocusHandler usernameFocusHandler = new UsernameTextBoxFocusHandler();
 	PasswordTextBoxBlurHandler passwordBlurHandler = new PasswordTextBoxBlurHandler();
 	PasswordTextBoxFocusHandler passwordFocusHandler = new PasswordTextBoxFocusHandler();
 	PasswordTextBoxKeyPressHandler passwordKeyPressHandler = new PasswordTextBoxKeyPressHandler();
 	ConfirmPasswordTextBoxBlurHandler confirmPasswordBlurHandler = new ConfirmPasswordTextBoxBlurHandler();
 	ConfirmPasswordTextBoxFocusHandler confirmPasswordFocusHandler = new ConfirmPasswordTextBoxFocusHandler();
 	TextBoxBlurHandler dobDateBlurHandler = new TextBoxBlurHandler();
 	TextBoxFocusHandler dobDateFocusHandler = new TextBoxFocusHandler();
 	TextBoxBlurHandler dobYearBlurHandler = new TextBoxBlurHandler();
 	TextBoxFocusHandler dobYearFocusHandler = new TextBoxFocusHandler();
 	ListBoxBlurHandler dobMonthBlurHandler = new ListBoxBlurHandler();
 	ListBoxFocusHandler dobMonthFocusHandler = new ListBoxFocusHandler();
 
 	ListBoxBlurHandler genderBlurHandler = new ListBoxBlurHandler();
 	ListBoxFocusHandler genderFocusHandler = new ListBoxFocusHandler();
 	TextBoxBlurHandler mobileNumberBlurHandler = new TextBoxBlurHandler();
 	TextBoxFocusHandler mobileNumberFocusHandler = new TextBoxFocusHandler();
 	TextBoxBlurHandler emailAddressBlurHandler = new TextBoxBlurHandler();
 	TextBoxFocusHandler emailAddressFocusHandler = new TextBoxFocusHandler();
 	ListBoxBlurHandler locationBlurHandler = new ListBoxBlurHandler();
 	ListBoxFocusHandler locationFocusHandler = new ListBoxFocusHandler();
 	TextBoxBlurHandler captchaTextBlurHandler = new TextBoxBlurHandler();
 	TextBoxFocusHandler captchaTextFocusHandler = new TextBoxFocusHandler();
 
 	DecoratedPopupPanel firstNamePopupPanel = new DecoratedPopupPanel(
 			(short) 285, (short) 5);
 
 	DecoratedPopupPanel lastNamePopupPanel = new DecoratedPopupPanel(
 			(short) 462, (short) 5);
 
 	DecoratedPopupPanel usernamePopupPanel = new DecoratedPopupPanel(
 			(short) 285, (short) 5);
 
 	DecoratedPopupPanel passwordPopupPanel = new DecoratedPopupPanel(
 			(short) 285, (short) 5);
 
 	DecoratedPopupPanel confirmPasswordPopupPanel = new DecoratedPopupPanel(
 			(short) 285, (short) 5);
 
 	DecoratedPopupPanel dobMonthPopupPanel = new DecoratedPopupPanel(
 			(short) 285, (short) 5);
 
 	DecoratedPopupPanel dobDatePopupPanel = new DecoratedPopupPanel(
 			(short) 413, (short) 4);
 
 	DecoratedPopupPanel dobYearPopupPanel = new DecoratedPopupPanel(
 			(short) 526, (short) 4);
 
 	DecoratedPopupPanel genderPopupPanel = new DecoratedPopupPanel((short) 285,
 			(short) 5);
 
 	DecoratedPopupPanel mobileNumberPopupPanel = new DecoratedPopupPanel(
 			(short) 285, (short) 5);
 
 	DecoratedPopupPanel emailAddressPopupPanel = new DecoratedPopupPanel(
 			(short) 285, (short) 5);
 
 	DecoratedPopupPanel locationPopupPanel = new DecoratedPopupPanel(
 			(short) 285, (short) 5);
 
 	DecoratedPopupPanel captchaTextPopupPanel = new DecoratedPopupPanel(
 			(short) 285, (short) 5);
 
 	DecoratedPopupPanel thirdPopupPanel = new DecoratedPopupPanel((short) 452,
 			(short) 5);
 
 	@Override
 	protected void onLoad() {
 		super.onLoad();
 
 		firstName.getElement().setAttribute("placeholder", "First");
 		lastName.getElement().setAttribute("placeholder", "Last");
 		dobDate.getElement().setAttribute("placeholder", "Date");
 		dobYear.getElement().setAttribute("placeholder", "Year");
 
 		firstName.addFocusHandler(firstNameFocusHandler);
 		firstName.addBlurHandler(firstNameBlurHandler);
 		lastName.addFocusHandler(lastNameFocusHandler);
 		lastName.addBlurHandler(lastNameBlurHandler);
 		username.addFocusHandler(usernameFocusHandler);
 		username.addBlurHandler(usernameBlurHandler);
 		password.addFocusHandler(passwordFocusHandler);
 		password.addBlurHandler(passwordBlurHandler);
 		password.addKeyPressHandler(passwordKeyPressHandler);
 		confirmPassword.addFocusHandler(confirmPasswordFocusHandler);
 		confirmPassword.addBlurHandler(confirmPasswordBlurHandler);
 		dobMonth.addFocusHandler(dobMonthFocusHandler);
 		dobMonth.addBlurHandler(dobMonthBlurHandler);
 		dobDate.addFocusHandler(dobDateFocusHandler);
 		dobDate.addBlurHandler(dobDateBlurHandler);
 		dobYear.addFocusHandler(dobYearFocusHandler);
 		dobYear.addBlurHandler(dobYearBlurHandler);
 		gender.addFocusHandler(genderFocusHandler);
 		gender.addBlurHandler(genderBlurHandler);
 		mobileNumber.addFocusHandler(mobileNumberFocusHandler);
 		mobileNumber.addBlurHandler(mobileNumberBlurHandler);
 		emailAddress.addFocusHandler(emailAddressFocusHandler);
 		emailAddress.addBlurHandler(emailAddressBlurHandler);
 		location.addFocusHandler(locationFocusHandler);
 		location.addBlurHandler(locationBlurHandler);
 		captchaText.addFocusHandler(captchaTextFocusHandler);
 		captchaText.addBlurHandler(captchaTextBlurHandler);
 
 		passwordBlurHandler.setDependentPassword(confirmPassword);
 		confirmPasswordBlurHandler.setDependentPassword(password);
 		passwordKeyPressHandler.setDependentPassword(confirmPassword);
 		dobMonthBlurHandler.setContainer(dobMonthBlock);
 		dobMonthFocusHandler.setContainer(dobMonthBlock);
 		genderBlurHandler.setContainer(genderBlock);
 		genderFocusHandler.setContainer(genderBlock);
 		locationBlurHandler.setContainer(locationBlock);
 		locationFocusHandler.setContainer(locationBlock);

 		firstNameFocusHandler.setDecoratedPopupPanel(firstNamePopupPanel);
 		firstNameBlurHandler.setDecoratedPopupPanel(firstNamePopupPanel);
 		lastNameFocusHandler.setDecoratedPopupPanel(lastNamePopupPanel);
 		lastNameBlurHandler.setDecoratedPopupPanel(lastNamePopupPanel);
 		usernameFocusHandler.setDecoratedPopupPanel(usernamePopupPanel);
 		usernameBlurHandler.setDecoratedPopupPanel(usernamePopupPanel);
 		passwordFocusHandler.setDecoratedPopupPanel(passwordPopupPanel);
 		passwordBlurHandler.setDecoratedPopupPanel(passwordPopupPanel);
 		confirmPasswordFocusHandler
 				.setDecoratedPopupPanel(confirmPasswordPopupPanel);
 		confirmPasswordBlurHandler
 				.setDecoratedPopupPanel(confirmPasswordPopupPanel);
 		dobDateFocusHandler.setDecoratedPopupPanel(dobDatePopupPanel);
 		dobDateBlurHandler.setDecoratedPopupPanel(dobDatePopupPanel);
 		dobYearFocusHandler.setDecoratedPopupPanel(dobYearPopupPanel);
 		dobYearBlurHandler.setDecoratedPopupPanel(dobYearPopupPanel);
 
 		genderFocusHandler.setDecoratedPopupPanel(genderPopupPanel);
 		genderBlurHandler.setDecoratedPopupPanel(genderPopupPanel);
 		mobileNumberFocusHandler.setDecoratedPopupPanel(mobileNumberPopupPanel);
 		mobileNumberBlurHandler.setDecoratedPopupPanel(mobileNumberPopupPanel);
 		emailAddressFocusHandler.setDecoratedPopupPanel(emailAddressPopupPanel);
 		emailAddressBlurHandler.setDecoratedPopupPanel(emailAddressPopupPanel);
 		locationFocusHandler.setDecoratedPopupPanel(locationPopupPanel);
 		locationBlurHandler.setDecoratedPopupPanel(locationPopupPanel);
 		captchaTextFocusHandler.setDecoratedPopupPanel(captchaTextPopupPanel);
 		captchaTextBlurHandler.setDecoratedPopupPanel(captchaTextPopupPanel);
 		// dobYearFocusHandler.setDecoratedPopupPanel(dobYearPopupPanel);
 		// dobYearBlurHandler.setDecoratedPopupPanel(dobYearPopupPanel);
 
 		firstNameFocusHandler.setErrorLabel(nameError);
 		firstNameBlurHandler.setErrorLabel(nameError);
 		lastNameFocusHandler.setErrorLabel(nameError);
 		lastNameBlurHandler.setErrorLabel(nameError);
 		usernameFocusHandler.setErrorLabel(usernameError);
 		usernameBlurHandler.setErrorLabel(usernameError);
 		passwordFocusHandler.setErrorLabel(passwordError);
 		passwordBlurHandler.setErrorLabel(passwordError);
 		confirmPasswordFocusHandler.setErrorLabel(confirmPasswordError);
 		confirmPasswordBlurHandler.setErrorLabel(confirmPasswordError);
 		dobMonthFocusHandler.setErrorLabel(dobError);
 		dobMonthBlurHandler.setErrorLabel(dobError);
 		dobDateFocusHandler.setErrorLabel(dobError);
 		dobDateBlurHandler.setErrorLabel(dobError);
 		dobYearFocusHandler.setErrorLabel(dobError);
 		dobYearBlurHandler.setErrorLabel(dobError);
 
 		genderFocusHandler.setErrorLabel(genderError);
 		genderBlurHandler.setErrorLabel(genderError);
 		mobileNumberFocusHandler.setErrorLabel(mobileNumberError);
 		mobileNumberBlurHandler.setErrorLabel(mobileNumberError);
 		emailAddressFocusHandler.setErrorLabel(emailAddressError);
 		emailAddressBlurHandler.setErrorLabel(emailAddressError);
 		locationFocusHandler.setErrorLabel(locationError);
 		locationBlurHandler.setErrorLabel(locationError);
 		captchaTextFocusHandler.setErrorLabel(captchaTextError);
 		captchaTextBlurHandler.setErrorLabel(captchaTextError);
 
 		usernameFocusHandler.setDecoratedPopupPanel(usernamePopupPanel);
 		usernameBlurHandler.setDecoratedPopupPanel(usernamePopupPanel);
 		passwordFocusHandler.setDecoratedPopupPanel(passwordPopupPanel);
 		passwordBlurHandler.setDecoratedPopupPanel(passwordPopupPanel);
 		confirmPasswordFocusHandler
 				.setDecoratedPopupPanel(confirmPasswordPopupPanel);
 		confirmPasswordBlurHandler
 				.setDecoratedPopupPanel(confirmPasswordPopupPanel);
 		dobMonthFocusHandler.setDecoratedPopupPanel(dobMonthPopupPanel);
 		dobMonthBlurHandler.setDecoratedPopupPanel(dobMonthPopupPanel);
 		dobDateFocusHandler.setDecoratedPopupPanel(dobDatePopupPanel);
 		dobDateBlurHandler.setDecoratedPopupPanel(dobDatePopupPanel);
 		dobYearFocusHandler.setDecoratedPopupPanel(dobYearPopupPanel);
 		dobYearBlurHandler.setDecoratedPopupPanel(dobYearPopupPanel);
 
 		firstNamePopupPanel.setMessage("Enter First Name");
 		lastNamePopupPanel.setMessage("Enter Last Name");
 		usernamePopupPanel
 				.setMessage("You can use letters, numbers and periods");
 		passwordPopupPanel.setMessage("Choose Strong Password");
 		confirmPasswordPopupPanel.setMessage("Confirm your Password");
 		dobMonthPopupPanel.setMessage("Date Of Birth, Month");
 		dobDatePopupPanel.setMessage("Date Of Birth, Date");
 		dobYearPopupPanel.setMessage("Date Of Birth, Year");
 		genderPopupPanel.setMessage("Select Your Gender");
 		mobileNumberPopupPanel.setMessage("Enter your mobile number");
 		emailAddressPopupPanel.setMessage("Enter valid email address");
 		locationPopupPanel.setMessage("Select your location");
 		captchaTextPopupPanel.setMessage("Enter the character in the image");
 
 		// Window.alert("Before Wrap" +
 		// Document.get().getElementById("firstName"));
 		// TextBox.wrap(Document.get().getElementById("firstName"));
 		// Window.alert("After Wrap");
 	}
 
 	public TextBox getFirstName() {
 		return firstName;
 	}
 
 	public void setFirstName(TextBox firstName) {
 		this.firstName = firstName;
 	}
 
 	public TextBox getLastName() {
 		return lastName;
 	}
 
 	public void setLastName(TextBox lastName) {
 		this.lastName = lastName;
 	}
 
 	public TextBox getUsername() {
 		return username;
 	}
 
 	public void setUsername(TextBox username) {
 		this.username = username;
 	}
 
 	public PasswordTextBox getPassword() {
 		return password;
 	}
 
 	public void setPassword(PasswordTextBox password) {
 		this.password = password;
 	}
 
 	public PasswordTextBox getConfirmPassword() {
 		return confirmPassword;
 	}
 
 	public void setConfirmPassword(PasswordTextBox confirmPassword) {
 		this.confirmPassword = confirmPassword;
 	}
 
 	public TextBox getDobDate() {
 		return dobDate;
 	}
 
 	public void setDobDate(TextBox dobDate) {
 		this.dobDate = dobDate;
 	}
 
 	public TextBox getDobYear() {
 		return dobYear;
 	}
 
 	public void setDobYear(TextBox dobYear) {
 		this.dobYear = dobYear;
 	}
 
 	public TextBox getMobileNumber() {
 		return mobileNumber;
 	}
 
 	public void setMobileNumber(TextBox mobileNumber) {
 		this.mobileNumber = mobileNumber;
 	}
 
 	public TextBox getEmailAddress() {
 		return emailAddress;
 	}
 
 	public void setEmailAddress(TextBox emailAddress) {
 		this.emailAddress = emailAddress;
 	}
 
 	public TextBox getCaptchaText() {
 		return captchaText;
 	}
 
 	public void setCaptchaText(TextBox captchaText) {
 		this.captchaText = captchaText;
 	}
 
 	public ListBox getDobMonth() {
 		return dobMonth;
 	}
 
 	public void setDobMonth(ListBox dobMonth) {
 		this.dobMonth = dobMonth;
 	}
 
 	public ListBox getGender() {
 		return gender;
 	}
 
 	public void setGender(ListBox gender) {
 		this.gender = gender;
 	}
 
 	public ListBox getLocation() {
 		return location;
 	}
 
 	public void setLocation(ListBox location) {
 		this.location = location;
 	}
 
 	public CheckBox getAgreement() {
 		return agreement;
 	}
 
 	public void setAgreement(CheckBox agreement) {
 		this.agreement = agreement;
 	}
 
 	public SubmitButton getSubmit() {
 		return submit;
 	}
 
 	public void setSubmit(SubmitButton submit) {
 		this.submit = submit;
 	}
 
 	public Label getNameError() {
 		return nameError;
 	}
 
 	public void setNameError(Label nameError) {
 		this.nameError = nameError;
 	}
 
 	public Boolean hasErrors() {
 		return firstNameBlurHandler.isError() || lastNameBlurHandler.isError();
 	}
 
 	public DivElement getDobMonthBlock() {
 		return dobMonthBlock;
 	}
 
 	public void setDobMonthBlock(DivElement dobMonthBlock) {
 		this.dobMonthBlock = dobMonthBlock;
 	}
 
 	public DivElement getGenderBlock() {
 		return genderBlock;
 	}
 
 	public void setGenderBlock(DivElement genderBlock) {
 		this.genderBlock = genderBlock;
 	}
 
 	public DivElement getLocationBlock() {
 		return locationBlock;
 	}
 
 	public void setLocationBlock(DivElement locationBlock) {
 		this.locationBlock = locationBlock;
 	}
 
 }
