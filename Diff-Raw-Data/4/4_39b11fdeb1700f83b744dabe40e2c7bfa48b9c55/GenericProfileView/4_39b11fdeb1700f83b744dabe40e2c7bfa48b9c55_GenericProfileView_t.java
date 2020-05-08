 package kornell.gui.client.presentation.profile.generic;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import kornell.api.client.Callback;
 import kornell.api.client.KornellClient;
 import kornell.core.shared.to.UserInfoTO;
 import kornell.gui.client.KornellConstants;
 import kornell.gui.client.event.LogoutEvent;
 import kornell.gui.client.presentation.course.CoursePlace;
 import kornell.gui.client.presentation.profile.ProfilePlace;
 import kornell.gui.client.presentation.profile.ProfileView;
 import kornell.gui.client.presentation.util.SimpleDatePicker;
 import kornell.gui.client.presentation.util.ValidatorHelper;
 import kornell.gui.client.presentation.vitrine.VitrinePlace;
 import kornell.gui.client.util.ClientProperties;
 
 import com.github.gwtbootstrap.client.ui.Form;
 import com.github.gwtbootstrap.client.ui.ListBox;
 import com.github.gwtbootstrap.client.ui.PasswordTextBox;
 import com.github.gwtbootstrap.client.ui.TextBox;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.place.shared.PlaceController;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.web.bindery.event.shared.EventBus;
 
 public class GenericProfileView extends Composite implements ProfileView {
 	interface MyUiBinder extends UiBinder<Widget, GenericProfileView> {
 	}
 
 	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
 
 	private KornellClient client;
 	private PlaceController placeCtrl;
 	private final EventBus bus;
 	private KornellConstants constants = GWT.create(KornellConstants.class);
 	private boolean isEditMode, isCurrentUser, isUserCreation;
 	SimpleDatePicker birthDate;
 	
 	// TODO fix this
 	private String IMAGE_PATH = "skins/first/icons/profile/";
 	@UiField Form form;
 	@UiField FlowPanel profileFields;
 	@UiField FlowPanel titlePanel;
 	@UiField Image imgTitle;
 	@UiField Label lblTitle;
 	@UiField FlowPanel editPanel;
 	@UiField Label lblEdit;
 	@UiField Button btnOK;
 	@UiField Button btnCancel;
 	@UiField Button btnClose;
 	@UiField Image imgProfile;
 	@UiField TextBox username;
 	@UiField PasswordTextBox password;
 	@UiField PasswordTextBox password2;
 	@UiField TextBox email;
 	@UiField TextBox firstName;
 	@UiField TextBox lastName;
 	@UiField TextBox company;
 	@UiField TextBox title;
 	@UiField ListBox sex;
 	@UiField FlowPanel birthDatePickerPanel;
 	@UiField Label usernameError;
 	@UiField Label emailError;
 	@UiField Label passwordError;
 	@UiField Label password2Error;
 	@UiField Label firstNameError;
 	@UiField Label lastNameError;
 	@UiField Label companyError;
 	@UiField Label titleError;
 	@UiField Label sexError;
 	@UiField Label birthDateError;
 	@UiField Label usernameTxt;
 	@UiField Label emailTxt;
 	@UiField Label firstNameTxt;
 	@UiField Label lastNameTxt;
 	@UiField Label companyTxt;
 	@UiField Label titleTxt;
 	@UiField Label sexTxt;
 	@UiField Label birthDateTxt;
 	@UiField FlowPanel passwordPanel;
 	@UiField FlowPanel password2Panel;
 	@UiField FlowPanel emailPanel;
 	@UiField FlowPanel sexPanel;
 	@UiField FlowPanel birthDatePanel;
 	@UiField Image passwordSeparator;
 	@UiField Image password2Separator;
 	@UiField Image emailSeparator;
 	@UiField Image sexSeparator;
 	@UiField Image birthDateSeparator;
 	@UiField Label imageExtraInfo;
 	@UiField Label usernameExtraInfo;
 	@UiField Label usernameExtraInfo2;
 	@UiField Label emailExtraInfo;
 	@UiField Label titleExtraInfo;
 	@UiField Label titleExtraInfo2;
 	
 	Map<Widget, Field> fieldsToErrorLabels;
 	private UserInfoTO user;
 
 	public GenericProfileView(EventBus bus, KornellClient client,
 			final PlaceController placeCtrl) {
 		this.bus = bus;
 		this.client = client;
 		this.placeCtrl = placeCtrl;
 		initWidget(uiBinder.createAndBindUi(this));
 		initData();
 		// i18n
 		btnOK.setText("OK".toUpperCase());
 		btnCancel.setText("Cancelar".toUpperCase());
 		btnClose.setText("Fechar".toUpperCase());
 	}
 
 	private void initData() {
 		client.getCurrentUser(new Callback<UserInfoTO>() {
 			@Override
 			protected void ok(UserInfoTO userTO) {
 				user = userTO;
 				isCurrentUser = userTO.getUsername().equals(((ProfilePlace) placeCtrl.getWhere()).getUsername());
 				display();
 			}
 			@Override
 			protected void unauthorized() {
 				isCurrentUser = true;
 				isUserCreation = true;
 				isEditMode = true;
 				display();
 			}
 		});
 	}
 
 	private boolean validateFields() {
 		ValidatorHelper validator = new ValidatorHelper();
 		if (!validator.lengthValid(username.getText(), 3, 50)){
 			fieldsToErrorLabels.get(username).getError().setText("Mínimo de 3 caracteres.");
 		} else if (!validator.usernameValid(username.getText())){
 			fieldsToErrorLabels.get(username).getError().setText("Campo inválido.");
 		}
 
 		if (!validator.emailValid(email.getText())){
 			fieldsToErrorLabels.get(email).getError().setText("Email inválido.");
 		}
 
 		if (!validator.passwordValid(password.getText())){
 			fieldsToErrorLabels.get(password).getError().setText("Senha inválida.");
 		}
 
 		if (!password.getText().equals(password2.getText())){
 			fieldsToErrorLabels.get(password2).getError().setText("As senhas não conferem.");
 		}
 		
 		if(!validator.lengthValid(firstName.getText(), 2, 50)){
 			fieldsToErrorLabels.get(firstName).getError().setText("Mínimo de 2 caracteres.");
 		}
 		
 		if(!validator.lengthValid(lastName.getText(), 2, 50)){
 			fieldsToErrorLabels.get(lastName).getError().setText("Mínimo de 2 caracteres.");
 		}
 		
 		if(sex.getSelectedIndex() <= 0){
 			fieldsToErrorLabels.get(sex).getError().setText("Escolha uma alternativa.");
 		}
 		
 		if(!birthDate.isSelected()){
 			fieldsToErrorLabels.get(birthDate).getError().setText("Insira sua data de nascimento.");
 		}
 		return !checkErrors();
 	}
 
 	@UiHandler("lblEdit")
 	void doEdit(ClickEvent e) {
 		isEditMode = true;
 		editPanel.setVisible(!isEditMode);
 		displayFields();
 	}
 
 	@UiHandler("btnOK")
 	void doOK(ClickEvent e) { 
 		btnOK.setEnabled(false);
 		clearErrors();
 		if(validateFields()){
 			client.checkUser(username.getText().toLowerCase().trim(), email.getText().toLowerCase().trim(), new Callback<UserInfoTO>(){
 				@Override
 				protected void ok(UserInfoTO user){
 					if(user.getPerson() != null){
 						fieldsToErrorLabels.get(username).getError().setText("O usuário já existe.");
 					} 
 					if(user.getEmail() != null){
 						fieldsToErrorLabels.get(email).getError().setText("O email já existe.");
 					}
 					if(!checkErrors()){
 						String data = username.getText().toLowerCase().trim() + "###" + 
 								password.getText().trim() + "###" +
 								email.getText().toLowerCase().trim() + "###" +
 								firstName.getText().trim() + "###" +
 								lastName.getText().trim() + "###" +
 								company.getText().trim() + "###" +
 								title.getText().trim() + "###" +
 								(sex.getSelectedIndex() == 1 ? "F" : "M") + "###" +
 								birthDate.toString() + "###" +
 								Window.Location.getHref().split("#")[0];
 						client.createUser(data, new Callback<UserInfoTO>(){
 							@Override
 							protected void ok(UserInfoTO user){
 								GWT.log("User created");
 								isEditMode = false;
 								
 								editPanel.setVisible(!isEditMode);
 								btnOK.setEnabled(true);
 								//TODO remove this
 								ClientProperties.remove("Authorization");
 								VitrinePlace vitrinePlace = new VitrinePlace();
 								vitrinePlace.setUserCreated(true);
 								placeCtrl.goTo(vitrinePlace);
 							}
 						});
 						
 					}
 					btnOK.setEnabled(true);
 				}
 			});
 		}
 		btnOK.setEnabled(true);
 	}
 
 	@UiHandler("btnCancel")
 	void doCancel(ClickEvent e) {
 		bus.fireEvent(new LogoutEvent());
 		/*
 		isEditMode = false;
 		editPanel.setVisible(!isEditMode);
 		clearErrors();
 		displayFields();*/
 	}
 
 	@UiHandler("btnClose")
 	void doClose(ClickEvent e) {
 		placeCtrl.goTo(new CoursePlace("d9aaa03a-f225-48b9-8cc9-15495606ac46"));
 	}
 
 	private void clearErrors() {
 		for (Field field : fieldsToErrorLabels.values()) {
 			field.getError().setText("");
 		}
 	}
 
 	private boolean checkErrors() {
 		boolean errors = false;
 		for (Field field : fieldsToErrorLabels.values()) {
 			if(!"".equals(field.getError().getText())){
 				errors = true;
 			}
 		}
 		return errors;
 	}
 	
 	private void showFields(){
 		for (Field field : fieldsToErrorLabels.values()) {
 			field.getField().setVisible(isEditMode);
 			field.getError().setVisible(isEditMode);
 			field.getValue().setVisible(!isEditMode);
 		}
 		passwordPanel.setVisible(isEditMode && isUserCreation);
 		passwordSeparator.setVisible(isEditMode && isUserCreation);
 		password2Panel.setVisible(isEditMode && isUserCreation);
 		password2Separator.setVisible(isEditMode && isUserCreation);
 		emailPanel.setVisible(isCurrentUser);
 		emailSeparator.setVisible(isCurrentUser);
 		sexPanel.setVisible(isCurrentUser);
 		sexSeparator.setVisible(isCurrentUser);
 		birthDatePanel.setVisible(isCurrentUser);
 		birthDateSeparator.setVisible(isCurrentUser);
 		
 		imageExtraInfo.setVisible(isEditMode);
 		usernameExtraInfo.setVisible(isEditMode);
 		usernameExtraInfo2.setVisible(isEditMode);
 		emailExtraInfo.setVisible(isEditMode);
 		titleExtraInfo.setVisible(isEditMode);
 		titleExtraInfo2.setVisible(isEditMode);
 
 		btnOK.setVisible(isEditMode);
 		btnCancel.setVisible(isEditMode);
 		btnClose.setVisible(!isEditMode);
 	}
 	
 	private void display() {
 		// TODO i18n
 		displayTitle();
 		displayFields();
 		form.removeStyleName("shy");
 	}
 
 	private void displayFields() {
 		imgProfile.setUrl(IMAGE_PATH + "profilePic.png");
 		sex.clear();
         sex.addItem("");
         sex.addItem("Feminino");
         sex.addItem("Masculino");
         birthDatePickerPanel.clear();
         if (isUserCreation){
 			birthDate = new SimpleDatePicker();
 			birthDatePickerPanel.add(birthDate);
 		} else if(isEditMode){
 			username.setText(user.getUsername());
 			email.setText(user.getPerson().getEmail());
 			firstName.setText(user.getPerson().getFirstName());
 			lastName.setText(user.getPerson().getLastName());
 			company.setText(user.getPerson().getCompany());
 			title.setText(user.getPerson().getTitle());
 	        sex.setSelectedIndex("F".equals(user.getPerson().getSex()) ? 1 : 2);
 			birthDate = new SimpleDatePicker(user.getPerson().getBirthDate());
 			birthDatePickerPanel.add(birthDate);
 		} else {
 			usernameTxt.setText(user.getUsername());
 			emailTxt.setText(user.getPerson().getEmail());
 			firstNameTxt.setText(user.getPerson().getFirstName());
 			lastNameTxt.setText(user.getPerson().getLastName());
 			companyTxt.setText(user.getPerson().getCompany());
 			titleTxt.setText(user.getPerson().getTitle());
 			sexTxt.setText("F".equals(user.getPerson().getSex()) ? "Feminino" : "Masculino");
			birthDateTxt.setText(DateTimeFormat.getShortDateFormat().format(user.getPerson().getBirthDate()));
 		}
 		mapFieldsToErrorLabels();
 		showFields();
 	}
 
 	private void displayTitle() {
 		imgTitle.setUrl(IMAGE_PATH + "course.png");
 		lblTitle.setText("Perfil");
 		editPanel.setVisible(!isEditMode);
 		lblEdit.setText("Editar");
 	}
 
 	private void mapFieldsToErrorLabels() {
 		fieldsToErrorLabels = new HashMap<Widget, Field>();
 		fieldsToErrorLabels.put(username, new Field(username, usernameError, usernameTxt));
 		fieldsToErrorLabels.put(email, new Field(email, emailError, emailTxt));
 		fieldsToErrorLabels.put(password, new Field(password, passwordError, new Label()));
 		fieldsToErrorLabels.put(password2, new Field(password2, password2Error, new Label()));
 		fieldsToErrorLabels.put(firstName, new Field(firstName, firstNameError, firstNameTxt));
 		fieldsToErrorLabels.put(lastName, new Field(lastName, lastNameError, lastNameTxt));
 		fieldsToErrorLabels.put(company, new Field(company, companyError, companyTxt));
 		fieldsToErrorLabels.put(title, new Field(title, titleError, titleTxt));
 		fieldsToErrorLabels.put(sex, new Field(sex, sexError, sexTxt));
 		fieldsToErrorLabels.put(birthDate, new Field(birthDatePickerPanel, birthDateError, birthDateTxt));
 	}
 
 	@Override
 	public void setPresenter(Presenter presenter) {
 		// TODO Auto-generated method stub
 	}
 	
 	class Field{
 		Widget field;
 		Label error;
 		Label value;
 		public Field(Widget field, Label error, Label value) {
 			this.field = field;
 			this.error = error;
 			this.value = value;
 		}
 		public Widget getField() {
 			return field;
 		}
 		public Label getError() {
 			return error;
 		}
 		public Label getValue() {
 			return value;
 		}
 	}
 
 }
