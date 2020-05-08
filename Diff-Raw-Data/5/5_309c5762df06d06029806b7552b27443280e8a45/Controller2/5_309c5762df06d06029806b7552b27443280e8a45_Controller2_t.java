 package GUI2;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.Set;
 
 import org.apache.commons.codec.binary.Base64;
 
 import javafx.application.Platform;
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.collections.FXCollections;
 import javafx.collections.ObservableList;
 import javafx.event.ActionEvent;
 import javafx.event.Event;
 import javafx.event.EventHandler;
 import javafx.fxml.FXML;
 import javafx.fxml.FXMLLoader;
 import javafx.fxml.Initializable;
 import javafx.fxml.JavaFXBuilderFactory;
 import javafx.scene.Parent;
 import javafx.scene.Scene;
 import javafx.scene.control.Accordion;
 import javafx.scene.control.Button;
 import javafx.scene.control.CheckBox;
 import javafx.scene.control.ComboBox;
 import javafx.scene.control.ContentDisplay;
 import javafx.scene.control.Label;
 import javafx.scene.control.ListCell;
 import javafx.scene.control.ListView;
 import javafx.scene.control.PasswordField;
 import javafx.scene.control.Tab;
 import javafx.scene.control.TabPane;
 import javafx.scene.control.TextArea;
 import javafx.scene.control.TextField;
 import javafx.scene.control.TitledPane;
 import javafx.scene.image.ImageView;
 import javafx.scene.input.ClipboardContent;
 import javafx.scene.input.DragEvent;
 import javafx.scene.input.Dragboard;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.input.TransferMode;
 import javafx.scene.layout.AnchorPane;
 import javafx.scene.layout.FlowPane;
 import javafx.scene.layout.GridPane;
 import javafx.scene.layout.HBox;
 import javafx.scene.layout.Pane;
 import javafx.scene.paint.Color;
 import javafx.scene.text.Font;
 import javafx.scene.text.FontPosture;
 import javafx.scene.text.Text;
 import javafx.stage.Modality;
 import javafx.stage.Stage;
 import javafx.util.Callback;
 import server.AutocorrectEngines;
 import API.Wrapper;
 import API.YummlyAPIWrapper;
 import Email.Sender;
 import UserInfo.Account;
 import UserInfo.Ingredient;
 import UserInfo.Invitation;
 import UserInfo.Kitchen;
 import UserInfo.KitchenEvent;
 import UserInfo.KitchenName;
 import UserInfo.Recipe;
 import client.Client;
 import eu.schudt.javafx.controls.calendar.DatePicker;
 
 public class Controller2 extends AnchorPane implements Initializable {
 
     @FXML private ResourceBundle resources;
     @FXML private URL location;
     @FXML private Label NoSearchResults, communalDietPreferencesList, newKitchenLabel, numberOfInvites;
     @FXML private Button addFridgeIngredient;//, goToRecipeSearchButton;
     @FXML private ComboBox<?> addRecipeEventSelector;
     @FXML private Button addRecipeToEventButton;
     @FXML private ComboBox<String> addShoppingIngredient;
     @FXML private ImageView envelope;
     @FXML private ListView<UserIngredientBox> fridgeList;
     @FXML private ListView<EventIngredientBox> eventShoppingList;
     //@FXML private CheckBox getRecipeChecksButton;
     @FXML private Accordion ingredientsAccordion;
     @FXML private ListView<InvitationBox> invitationsList;
     @FXML private ListView<Text> kitchenChefList;
     @FXML private Pane kitchenHide;
     @FXML private ListView<KitchenIngredientBox> kitchenIngredientList;
     @FXML private ComboBox<String> kitchenSelector, eventIng;
     @FXML private Button leaveKitchenButton;
     @FXML private ComboBox<String> newIngredient;
     @FXML private Text newKitchenActionText;
     @FXML private Button newKitchenButton, newKitchenCancelButton, newKitchenCreateButton;
     @FXML private TextField newKitchenNameField;
     @FXML private AnchorPane newKitchenPane;
     @FXML private FlowPane recipeFlow, kitchenRecipes, eventRecipes;
     @FXML private Tab recipeSearchTab, homeTab, profileTab;
     @FXML private CheckBox removeFridgeIngredient, removeIngFromEvent; 
     @FXML private AnchorPane removeIngredientsButton;
     @FXML private CheckBox removeShoppingIngredient;
     @FXML private FlowPane resultsFlow;
     @FXML private AnchorPane root;
     @FXML private ListView<String> searchAdditionalList;
     @FXML private Button searchButton;
     @FXML private TextField searchField;
     @FXML private ListView<ShoppingIngredientBox> shoppingList;
     @FXML private TabPane tabPane;
     @FXML private Label welcome, weather, nameLabel, locationLabel;
     @FXML private TextField nameField, locationField;
     @FXML private Button profileEditor;
     @FXML private ComboBox<String> addRestrictionBar, addAllergyBar; 
     @FXML private CheckBox removeAllergy, removeRestriction;
     @FXML private ListView<RestrictionBox> restrictionsList;
     @FXML private ListView<AllergyBox> allergiesList;
     @FXML private Label emailLabel;
     @FXML private AnchorPane kitchenJunk;
     @FXML private Label communalAllergiesList;
     @FXML private ListView<DraggableIngredient> kitchenUserIngredients;
     @FXML private Button changePassButton;
     @FXML private PasswordField oldPassField;
     @FXML private PasswordField newPassField1;
     @FXML private PasswordField newPassField2;
     @FXML private Button savePassButton;
     @FXML private Button cancelPassButton;
     @FXML private Pane changePassPane, noRecipesPane;
     @FXML private Label oldPassLabel;
     @FXML private Label newPassLabel1;
     @FXML private Label newPassLabel2;
     @FXML private Label changePassErrorLabel;
     @FXML private TabPane eventTabPane;
     @FXML private Tab kitchenIngTab;
     @FXML private Pane searchPromptPane;
     @FXML private Label lengthRecipeSearchLabel;
 
     @FXML private GridPane eventGridPane;
     @FXML private Button createEventButton;
     @FXML private Text newEventActionText;
     @FXML private TextField newEventNameField;
     @FXML private HBox newEventTimeSelectorBox;
     @FXML private ComboBox<String> hour;
     @FXML private ComboBox<String> min;
     @FXML private ComboBox<String> amPm;
     @FXML private ComboBox<String> eventSelector;
     @FXML private CheckBox removeKitchenIngredients;
     @FXML private AnchorPane eventAnchor;
     @FXML private TextArea eventCommentDisplayField;
     @FXML private TextArea eventCommentWriteField;
     @FXML private Button postMessageButton;
     @FXML private Tab eventTab;
     @FXML private Tab newEventTab;
     @FXML private GridPane timeDateGrid;
     @FXML private Text eventTime;
     @FXML private Text eventDate;
     @FXML private Button deleteEventButton;
     @FXML private Button editEventButton;
     @FXML private GridPane editEventGrid;
     @FXML private Pane editPane;
     @FXML private Button cancelEditButton;
     @FXML private Button saveEditButton;
     @FXML private ComboBox<String> editHour;
     @FXML private ComboBox<String> editMin;
     @FXML private ComboBox<String> editAmPm;
     @FXML private Text eventNameEdit;
     @FXML private Text editEventActionText;
     @FXML private Label addIngredientActionLabel;
     @FXML private Label shoppingListActionLabel;
     @FXML private Pane noEventRecipePane;
 
     @FXML private Label passChangeSuccessfulLabel;
 
     //Date Picker
     private DatePicker eventDatePicker;
     private DatePicker editDatePicker;
     //Time combo box booleans
     boolean _eventSelectorShouldDisplay, _newTimeShouldDisplay;
 
     
     //Local Data
     private Client _client;
     private Account _account;
     private Map<KitchenName, Kitchen> _kitchens;
     private AutocorrectEngines _engines;
     private Wrapper _api;
     private String _currentEventName;
     private KitchenPane _currentKitchenPane;
     private HashSet<String> _setOfAdditionalSearchIngs = new HashSet<String>();//Set of additional ingredients for search.
     private InviteChefController _inviteChefController;
     private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
     private HashMap<String,KitchenName> _kitchenIds;
     private boolean _kitchenSelectorDisplay = false;
    
     
 	@Override
 	public void initialize(URL arg0, ResourceBundle arg1) {
         
         // Initialize the DatePicker for event
         // Date Picker comes from "http://edu.makery.ch/blog/2013/01/07/javafx-date-picker/" Thanks!
         eventDatePicker = new DatePicker(Locale.ENGLISH);
         eventDatePicker.setPromptText("Select a date");
         eventDatePicker.setDateFormat(new SimpleDateFormat("MM/dd/yyyy"));
         eventDatePicker.getCalendarView().todayButtonTextProperty().set("Today");
         eventDatePicker.getCalendarView().setShowWeeks(false);
         eventDatePicker.getStylesheets().add("GUI2/DatePicker.css");
         
         editDatePicker = new DatePicker(Locale.ENGLISH);
         editDatePicker.setPromptText("Select a date");
         editDatePicker.setDateFormat(new SimpleDateFormat("MM/dd/yyyy"));
         editDatePicker.getCalendarView().todayButtonTextProperty().set("Today");
         editDatePicker.getCalendarView().setShowWeeks(false);
         editDatePicker.getStylesheets().add("GUI2/DatePicker.css");
         // Add DatePicker to grid
         eventGridPane.add(eventDatePicker, 2, 2);
         editEventGrid.add(editDatePicker, 0, 0);
         
 	}
 	
 	public void setUp(Client client, Account account, Map<KitchenName,Kitchen> kitchens, AutocorrectEngines engines){
     	_client = client;
     	_account = account;
     	_kitchens = kitchens;
     	_engines = engines;
     	_api = new YummlyAPIWrapper();
     	
     	// Set up Profile tab
     	nameLabel.setText(_account.getName());
     	locationLabel.setText(_account.getAddress());
     	emailLabel.setText(_account.getID());
     	
     	// Set up Home tab
     	populateUserFridge();
     	populateUserRecipes();
     	populateShoppingList();
     	displayPleasantries();
     	
     	// Set up Kitchen tab
     	initializeComboBoxes();
     	populateKitchenSelector();
     	populateInvitations();
         populateNewEventTime();
         populateEventSelector();
     	
     	// Set up Search tab
     	setUpSearchPage();
     	populateSearchIngredients();
 
     	// Select
     	if(_account.getAddress().equals("") || _account.getName().equals("")){
     		tabPane.getSelectionModel().select(profileTab);
     	}
     	else{
     		tabPane.getSelectionModel().select(homeTab);
     	}
 	}
 	
 	public void initializeComboBoxes(){
 		kitchenSelector.getItems().clear();
 		//eventSelector.getItems().clear();
 	//	eventShoppingComboBox.getItems().clear();
     	addRestrictionBar.getItems().clear();
     	newIngredient.getItems().clear();
     	addAllergyBar.getItems().clear();
     	addShoppingIngredient.getItems().clear();
     	//searchAdditionalBox.getItems().clear();
     	addRestrictionBar.getItems().addAll("Vegan", "Lacto vegetarian", "Ovo vegetarian", 
     			"Pescetarian", "Lacto-ovo vegetarian");
     	addAllergyBar.getItems().addAll("Wheat-Free", "Gluten-Free", "Peanut-Free", 
     			"Tree Nut-Free", "Dairy-Free", "Egg-Free", "Seafood-Free", "Sesame-Free", 
     			"Soy-Free", "Sulfite-Free");
     }
 	
 	/*
 	 ********************************************************** 
 	 * ALL PURPOSE
 	 **********************************************************
 	 */
     private abstract class GuiBox extends GridPane {
     	public GuiBox() {
     		this.setHgap(5);
     	}
     	public void remove() {};	
     	public RemoveButton getRemover() {
     		return null;
     	}    	
     }
     
     private class RemoveButton extends Button{
     	public RemoveButton(final GuiBox parent){
     		this.setText("-");
     		this.setOnAction(new EventHandler<ActionEvent>() {
     			@Override
                 public void handle(ActionEvent e) {
     				parent.remove();
     			}
     		});
     	}
     }
     
 	public void disableRemoves(ListView<? extends GuiBox> listview){
 		for(GuiBox gb: listview.getItems()){
 			gb.getRemover().setVisible(false);
 		}
 	}
 	
 	public void displayPleasantries(){
 		if(_account.getName().trim().length()==0||_account.getName().trim().length()==0){
 			welcome.setText("Welcome! Update your profile by clicking on the profile tab.");
 			weather.setText("");
 		}
 		else{
 	        welcome.setText("Welcome " + _account.getName() + "!");
 	        weather.setText("How's the weather in " + _account.getAddress() + "?");
 		}
 
     }
 	
 	public void enablePleasantries(boolean enable){
 		welcome.setVisible(enable);
 		weather.setVisible(enable);
 	}
 	
 	/*
 	 ********************************************************** 
 	 * Profile
 	 **********************************************************
 	 */
 	
 	public void EditOrSaveAccountChanges(){
 		if(locationField.getText().length()>Utils.MAX_FIELD_LEN || 
 				nameField.getText().length()>Utils.MAX_FIELD_LEN){
 			changePassErrorLabel.setText("You may not enter a field" +
 					" greater than " + Utils.MAX_FIELD_LEN + " chars.");
 			changePassErrorLabel.setVisible(true);
 			return;
 		}
 		
 		System.out.println(profileEditor.getText());
 		if(profileEditor.getText().equals("Edit Profile")){
 			nameField.setVisible(true);
 			locationField.setVisible(true);
 			nameLabel.setVisible(false);
 			locationLabel.setVisible(false);
 			nameField.setText(nameLabel.getText());
 			locationField.setText(locationLabel.getText());
 			profileEditor.setText("Save Changes");
 		}
 		else{
 			nameField.setVisible(false);
 			locationField.setVisible(false);
 			nameLabel.setVisible(true);
 			locationLabel.setVisible(true);
 			nameLabel.setText(nameField.getText());
 			locationLabel.setText(locationField.getText());
 			_account.setName(nameLabel.getText());
 			_account.setAddress(locationLabel.getText());
 			_client.storeAccount(_account);
 			this.displayPleasantries();
 			profileEditor.setText("Edit Profile");
 		}
 	}
 	
 	public void addRestrictionListListener(){
     	removeRestriction.setSelected(false);
     	disableRemoves(restrictionsList);
     	String name = addRestrictionBar.getValue();
     	if(name!=null){
 	    	if(name.trim().length()!=0){
 	    		_account.addRestriction(name);
 	    		_client.storeAccount(_account, 2, name);
 	    		populateRestrictions();
 	    	}
     	}
 	    addRestrictionBar.setButtonCell(new ListCell<String>() {
 			private final Label id;	
 			{
 				setContentDisplay(ContentDisplay.TEXT_ONLY);
 				id = new Label("balls");
 		    }
 			
 			@Override
 			protected void updateItem(String name, boolean empty) {
 				super.updateItem(name, empty);
 				
 				if (name == null || empty) {
 					setText("why is this null");
 				} else {
 					//id.setText(kitchenIds.get(name).getName());
 					setText("Select a Restriction to add it");
 				}
 			}
 		});
 
     }
 	
     public void populateRestrictions(){
     	ObservableList<RestrictionBox> listItems = FXCollections.observableArrayList(); 
     	for(String r: _account.getDietaryRestrictions()){
     		RestrictionBox box = new RestrictionBox(r);
     		listItems.add(box);
     	}
     	restrictionsList.setItems(listItems);
     }
 		
 	public void removeRestrictions(){		
 		for(RestrictionBox s: restrictionsList.getItems()){
 			RemoveButton rButton = s.getRemover();
 			rButton.setVisible(!rButton.isVisible());
 		}
 	}
 	
     public void restrictionComboListener(){
     	String text = addRestrictionBar.getEditor().getText();
     	System.out.println("hurr");
     	addRestrictionBar.getEditor().setText("Select a Restriction to add it");
     	List<String> suggs = null;
     	if(text.trim().length()!=0)
     		suggs = _engines.getRestrictionSuggestions(text.toLowerCase());
     	if(suggs!=null){
     		addRestrictionBar.getItems().clear();
     		addRestrictionBar.getItems().addAll(suggs);
     	}
     }
     
     public void addAllergyListener(){
     	removeAllergy.setSelected(false);
     	disableRemoves(allergiesList);
     	String name = addAllergyBar.getValue();
 	    if(name!=null){
     		if(name.trim().length()!=0){
 	    		_account.addAllergy(name);
 	        	_client.storeAccount(_account, 4, name);
 	        	populateAllergies();
 	    	}
 	    }
 	    addAllergyBar.setButtonCell(new ListCell<String>() {
 			private final Label id;
 			{
 				setContentDisplay(ContentDisplay.TEXT_ONLY);
 				id = new Label("balls");
 		    }
 			
 			@Override
 			protected void updateItem(String name, boolean empty) {
 				super.updateItem(name, empty);
 				
 				if (name == null || empty) {
 					setText("why is this null");
 				} else {
 					//id.setText(kitchenIds.get(name).getName());
 					setText("Select an Allergy to add it");
 				}
 			}
 		});
     }
        
     public void populateAllergies(){
     	ObservableList<AllergyBox> listItems = FXCollections.observableArrayList(); 
     	for(String a: _account.getAllergies()){
     		AllergyBox box = new AllergyBox(a);
     		listItems.add(box);
     	}
     	allergiesList.setItems(listItems);
     }
     
 	public void removeAllergies(){		
 		for(AllergyBox s: allergiesList.getItems()){
 			RemoveButton rButton = s.getRemover();
 			rButton.setVisible(!rButton.isVisible());
 		}
 	}
 	
 	private class RestrictionBox extends GuiBox { 
 		protected String _toDisplay;
 	    protected RemoveButton _remove;
 	
     	public RestrictionBox(String display){
     		super();
     		_toDisplay = display;
     	    Label ingred = new Label(display);
     	    this.add(ingred, 1, 0);
     	    _remove = new RemoveButton(this);
     	    _remove.setVisible(false);
     	    this.add(_remove, 0, 0);;
     	}
     	
     	public void remove(){
     		_account.removeRestriction(_toDisplay);
     		_client.storeAccount(_account, 3, _toDisplay);
     		ObservableList<RestrictionBox> listItems = restrictionsList.getItems();
     		listItems.remove(this);
     	}
     	
     	public RemoveButton getRemover(){
     		return _remove;
     	}
     }
 	
 	private class AllergyBox extends GuiBox {
     	protected String _toDisplay;
     	protected RemoveButton _remove;
 
     	public AllergyBox(String display){
     		super();
     		_toDisplay = display;
     	    Label all = new Label(display);
     	    this.add(all, 1, 0);
     	    _remove = new RemoveButton(this);
     	    _remove.setVisible(false);
     	    this.add(_remove, 0, 0);;
     	}
     	
     	public void remove(){
     		//_account.removeRestriction(_toDisplay);
     		_account.removeAllergy(_toDisplay);
     		_client.storeAccount(_account, 5, _toDisplay);
     		ObservableList<AllergyBox> listItems = allergiesList.getItems();
     		listItems.remove(this);
     	}
     	
     	public RemoveButton getRemover(){
     		return _remove;
     	}
     }
 	
 		/**
 		 * 
 		 * Change Password info.
 		 *
 		 */
 		public void changePasswordButtonListener(){
 			setPassFieldsVisible(true);
 			
 		}
 		public void cancelPassButtonListener(){
 			setPassFieldsVisible(false);
 		}
 		public void setPassFieldsVisible(boolean display){
 			changePassPane.setVisible(display);
 		}
 		public void savePassButtonListener(){
 			System.out.println("SAVE PASS");
 			String old = oldPassField.getText();
 			if(old.length()>Utils.MAX_FIELD_LEN){
 				changePassErrorLabel.setText("You may not enter a password greater than " + Utils.MAX_FIELD_LEN + " chars.");
 				changePassErrorLabel.setVisible(true);
 			}
 			if(old!=null&&old.trim().length()!=0){
 				//Will receive a boolean, which will call the changePass method if true.
 				System.out.println("SHOULD CALL CLIENT PASS MATCH.");
 				_client.passwordMatches(_account.getID(), old);
 			}
 			else{
 				changePassErrorLabel.setText("You must enter a valid old password.");
 				changePassErrorLabel.setVisible(true);
 			}
 			
 		}
 		
 		public void changePassword(boolean matches){
 			System.out.println("SHOULD CHANGE THE PASSWORDS!!!: " + matches);
 			String new1 = newPassField1.getText();
 			String new2 = newPassField2.getText();
 			if(new1.length()>Utils.MAX_FIELD_LEN || new2.length()>Utils.MAX_FIELD_LEN){
 				changePassErrorLabel.setText("You may not enter a password greater than " + Utils.MAX_FIELD_LEN + " chars.");
 				changePassErrorLabel.setVisible(true);
 			}
 			if(matches){
 				if(new1!=null&&new2!=null&&new1.trim().length()!=0
 						&&new2.trim().length()!=0){
 					if(new1.equals(new2)){
 						System.out.println("SHOULD CHANGE THE PASSWORD: IN CONTROLLER");
 						_client.changePassword(_account.getID(), new1);
 						passChangeSuccessfulLabel.setText("Password change successful.");
 						passChangeSuccessfulLabel.setVisible(true);
 						setPassFieldsVisible(false);
 					}
 					else{
 						changePassErrorLabel.setText("Your new passwords do not match!");
 						changePassErrorLabel.setVisible(true);
 					}
 					
 				}
 				else{
 					changePassErrorLabel.setText("You must enter something in both fields!");
 					changePassErrorLabel.setVisible(true);
 				}
 			}
 			else{
 				changePassErrorLabel.setText("You did not enter a valid old password.");
 				changePassErrorLabel.setVisible(true);
 			}
 			
 		}
 		
 	
 	/*
 	 ********************************************************** 
 	 * Home Screen
 	 **********************************************************
 	 */
 	
 	private class UserIngredientBox extends GuiBox{
     	protected String _toDisplay;
     	protected RemoveButton _remove;
 
     	public UserIngredientBox(String display) {
     		super();
     		_toDisplay = display;
     	    Label ingred = new Label(display);
     	    this.add(ingred, 1, 0);
     	    _remove = new RemoveButton(this);
     	    _remove.setVisible(false);
     	    this.add(_remove, 0, 0);
     	}
     	
     	public void remove(){
     		System.out.println("removing ingredient " + _toDisplay);
     		Ingredient ing = new Ingredient(_toDisplay);
     		_account.removeIngredient(ing);
     		_client.storeAccount(_account, ing);
     		populateSearchIngredients();
     		populateUserIngredientsInKitchen();
     		ObservableList<UserIngredientBox> listItems = fridgeList.getItems();
     		listItems.remove(this);
     	}
     	
     	public RemoveButton getRemover(){
     		return _remove;
     	}
     }
 	
 	@FXML
     public void addIngredientListener(Event event) {
     	addIngredientActionLabel.setVisible(false);
     	disableRemoves(fridgeList);
     	removeFridgeIngredient.setSelected(false);
     	String name = newIngredient.getValue();
 	    if(name!=null){
 	    	if(name.length()>Utils.MAX_COMBO_LEN){
 	    		addIngredientActionLabel.setVisible(true);
 	    		return;
 	    	}
     		if(name.trim().length()!=0){
 	    		_account.addIngredient(new Ingredient(name.toLowerCase().trim()));
 	    		_client.storeAccount(_account);
 	    		populateUserIngredientsInKitchen();
 	        	populateUserFridge();
 	        	populateSearchIngredients();
 	        	newIngredient.getEditor().setText("");
 	    	}
 	    }
     	//newIngredient.setValue(null);
     	newIngredient.getItems().clear();
     	
     }
     
     public void populateUserFridge() {
     	ObservableList<UserIngredientBox> listItems = FXCollections.observableArrayList();  
     	fridgeList.setItems(listItems);
     	for(Ingredient i: _account.getIngredients()){
     		UserIngredientBox box = new UserIngredientBox(i.getName());
     		listItems.add(box);
     	}
     	fridgeList.setItems(listItems);
     }
     
 	public void removeIngredients(){		
 		for(UserIngredientBox s: fridgeList.getItems()){
 			RemoveButton rButton = s.getRemover();
 			rButton.setVisible(!rButton.isVisible());
 		}
 	}
     
     public void addShoppingListListener() {
     	disableRemoves(shoppingList);
     	removeShoppingIngredient.setSelected(false);
     	String name = addShoppingIngredient.getValue();
     	if(name != null && name.length()>Utils.MAX_COMBO_LEN){
     		shoppingListActionLabel.setVisible(true);
     		return;
     	}
     	if(name!=null){
     		if(name.trim().length()!=0){
     			_account.addShoppingIngredient(new Ingredient(name.toLowerCase().trim()));
     			_client.storeAccount(_account);
     			populateShoppingList();
     			addShoppingIngredient.getEditor().setText("");
     			
     		}
     		addShoppingIngredient.setValue("");
     		addShoppingIngredient.getItems().clear();
     	}
     }
     
     private class ShoppingIngredientBox extends GuiBox{
     	protected String _toDisplay;
     	protected RemoveButton _remove;
     	
     	public ShoppingIngredientBox(String display){
     		super();
     		_toDisplay = display;
     	    Label ingred = new Label(display);
     	    this.add(ingred, 1, 0);
     	    _remove = new RemoveButton(this);
     	    _remove.setVisible(false);
     	    this.add(_remove, 0, 0);
     	    
     	}
     	
     	public void remove(){
     		Ingredient ing = new Ingredient(_toDisplay);
     		_account.removeShoppingIngredient(ing);
     		_client.storeAccount(_account);
     		ObservableList<ShoppingIngredientBox> listItems = shoppingList.getItems();
     		listItems.remove(this);
     	}
     	
     	public RemoveButton getRemover(){
     		return _remove;
     	}
     }
     
     public void populateShoppingList(){
     	ObservableList<ShoppingIngredientBox> listItems = FXCollections.observableArrayList();  
     	shoppingList.setItems(listItems);
     	for(Ingredient i: _account.getShoppingList()){
     		ShoppingIngredientBox box = new ShoppingIngredientBox(i.getName());
     		listItems.add(box);
     	}
     	shoppingList.setItems(listItems);
     }
     
 	public void removeShoppingIngredient(){		
 		for(ShoppingIngredientBox s: shoppingList.getItems()){
 			RemoveButton rButton = s.getRemover();
 			rButton.setVisible(!rButton.isVisible());
 		}
 	}
     
 	
     public void populateUserRecipes(){
     	recipeFlow.getChildren().clear();
     	if (_account.getRecipes().size() != 0) {
     		noRecipesPane.setVisible(false);
     	} 
     	else {
     		noRecipesPane.setVisible(true);
     	}
     	for(Recipe r : _account.getRecipes()){
     		recipeFlow.getChildren().add(new RecipeBox(r, this, _account, _client));
     	}
     	if(recipeFlow.getChildren().size()==0){
     		noRecipesPane.setVisible(true);
     	}
     }
     
     @FXML
     public void wentShopping(ActionEvent event) {
     	HashSet<Ingredient> ings = new HashSet<Ingredient>();
     	
     	for (Ingredient i: _account.getShoppingList()) {
     		ings.add(i);
     	}  
     	
     	for (Ingredient i: ings) {
     		_account.removeShoppingIngredient(i);
     		_account.addIngredient(i);
     	} 
     	
     	populateShoppingList();
     	populateUserFridge();
     	_client.storeAccount(_account);
     }
     
     @FXML
     public void textShoppingList(ActionEvent event) {
     	try {
 			URL location = getClass().getResource("SMSWindow.fxml");
 			FXMLLoader fxmlLoader = new FXMLLoader();
 			fxmlLoader.setLocation(location);
 			fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
 			Parent p = (Parent) fxmlLoader.load(location.openStream());
 	        SMSController smsController = (SMSController) fxmlLoader.getController();
 	        Stage stage = new Stage();
 	        stage.setScene(new Scene(p));
 	        stage.setTitle("Text Shopping List");
 	        smsController.setUp(stage, new ArrayList<Ingredient>(_account.getShoppingList()));
 		    stage.show();
 		} catch (IOException e) {
 			System.out.println("ERROR: IN GUI 2 Frame");
 			e.printStackTrace();
 		}
     }
     
 	/*
 	 ********************************************************** 
 	 * Kitchen
 	 **********************************************************
 	 */
 
 	public void populateKitchenSelector(){
 		System.out.println("POPULATE");
 		_kitchenIds = _client.getKitchenIdMap();
 
 		kitchenSelector.getItems().clear();
 		kitchenSelector.getItems().addAll(_kitchenIds.keySet());
 		kitchenSelector.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
 			@Override
 			public ListCell<String> call(final ListView<String> name) {
 				return new ListCell<String>() {
 					private final Label id;
 					{
 						setContentDisplay(ContentDisplay.TEXT_ONLY);
 						id = new Label("balls");
 				    }
 					
 					@Override
 					protected void updateItem(String name, boolean empty) {
 						super.updateItem(name, empty);
 					
 						if (name == null || empty) {
 							setText("Select Kitchen");
 							super.updateItem("Select Kitchen", empty);
 						} else {
 							System.out.println(_kitchenIds);
 							setText(_kitchenIds.get(name).getName());
 							
 						}
 					}
 				};
 			}
 		});
 		
 		kitchenSelector.setOnAction(new EventHandler<ActionEvent>(){
 			@Override
 			public void handle(ActionEvent e){
 				System.out.println("I have been clicked! " + kitchenSelector.getValue());
 				
 				//disable the pane that hides everything
 				kitchenJunk.setDisable(false);
 				
 	
 				String id = kitchenSelector.getValue();
 				if(id!= null){
 					if(_client.getCurrentKitchen()!= null){
 						if(!_client.getCurrentKitchen().getID().equals(id)){
 							System.out.println(_client.getCurrentKitchen().getID() + " != " + id);
 							System.out.println("SETTING CURRENT EVENT NAME TO NULL");
 							_currentEventName = null;
 							disableEvents();
 							
 						}
 					}
 					kitchenSelector.setButtonCell(new ListCell<String>() {						
 						@Override
 						protected void updateItem(String name, boolean empty) {
 							super.updateItem(name, empty);
 							
 							System.out.println("updataesetButtonCell");
 							System.out.println(name ==null);
 							System.out.println(empty);
 							System.out.println(!_kitchenSelectorDisplay);
 							if (name == null || empty) {
 								setText("Select Kitchen");
 							} else {
 								System.out.println("nameeee ? " + name);
 								System.out.println("kitchen nmae ? " + _kitchenIds.get(name));
 								setText(_kitchenIds.get(name).getName());
 							}
 						}
 					});	
 					displayKitchen(_kitchenIds.get(id));
 				}
 			}
 		});
 		if(kitchenSelector.getValue()==null && _client.getCurrentKitchen()== null){
 			kitchenJunk.setDisable(true);
 		}
 		_kitchenSelectorDisplay = true;
 	}
 	
 	public void displayKitchen(KitchenName kn){
 		System.out.println("I WANT TO DISPLAY KITCHEN: " + kn.getName() + "   -->  " + kn.getID());
 		
 		populateUserIngredientsInKitchen();
 		
 		//Clearing/hiding new kitchen stuff
 		hideNewKitchenStuff();
 		//******************clearEventPane();
 		_client.setCurrentKitchen(kn);
 		kitchenJunk.setDisable(false);
 		//Making sure selector displays correctly
 		final String currentName = _client.getCurrentKitchen().getName();
 		final String currentId = _client.getCurrentKitchen().getID();
 		kitchenSelector.setValue(currentId);
 		kitchenSelector.setButtonCell(new ListCell<String>() {
 			@Override
 			protected void updateItem(String name, boolean empty) {
 				super.updateItem(name, empty);
 				
 				if (name == null || empty) {
 					setText("Select Kitchen");
 				} else {
 					//id.setText(kitchenIds.get(name).getName());
 					setText(currentName);
 				}
 			}
 		});
 		
 		Kitchen k = _client.getKitchens().get(kn);
 		
 		
 		StringBuilder restricts = new StringBuilder("");
 		boolean first = true;
 		for(String r: k.getDietaryRestrictions()){
 			if(!first){
 				restricts.append(", ");
 			}
 			restricts.append(r);
 			first = false;
 		}		
 		communalDietPreferencesList.setText(restricts.toString());	
 		if(communalDietPreferencesList.getText().length()==0){
 			communalDietPreferencesList.setText("No restrictions listed for current users");
 		}		
 				
 		StringBuilder allergies = new StringBuilder("");
 		first = true;
 		for(String r: k.getAllergies()){
 			if(!first){
 				allergies.append(", ");
 			}
 			allergies.append(r);
 			first = false;
 		}		
 		communalAllergiesList.setText(allergies.toString());	
 		if(communalAllergiesList.getText().length()==0){
 			communalAllergiesList.setText("No allergies listed for current users");
 		}
 		
 		
 		kitchenIngredientList.getItems().clear();
 		HashMap<Ingredient, HashSet<String>> map = k.getIngredientsMap();
 		HashSet<String> toAddAll = new HashSet<String>();
 		for(Ingredient ing: map.keySet()){
 			boolean fromUser = false;
 			first = true;
 			String toDisplay = ing.getName() + " (";
 			for(String user: map.get(ing)){
 				if(!first){
 					toDisplay += ", ";
 				}
 				toDisplay += user.split("@")[0];
 				if(user.equals(_account.getID())){
 					fromUser = true;
 				}
 				first =false;
 			}
 			toDisplay += ")";
 			
 			
 			kitchenIngredientList.getItems().add(new KitchenIngredientBox(ing.getName(), toDisplay, fromUser));
 		}
 		
 		if(removeKitchenIngredients.isSelected()){
 			removeKitchenIngredients();
 		}
 		
 		kitchenChefList.getItems().clear();
 		for(String user: k.getActiveUsers()){
 			kitchenChefList.getItems().add(new Text(user));
 		}
 		for(String user: k.getRequestedUsers()){
 			Text t = new Text(user + " (pending)");
 			t.setFont(Font.font("Verdana", FontPosture.ITALIC, 10));
 			t.setFill(Color.GRAY);
 			kitchenChefList.getItems().add(t);
 		}
 		
 		kitchenRecipes.getChildren().clear();
     	//noRecipesPane.setVisible(false);
     	for(Recipe r: k.getRecipes()){
     		kitchenRecipes.getChildren().add(new RecipeBox(r, this, k, _client));
     	}
     	if(recipeFlow.getChildren().size()==0){
     		kitchenRecipes.setVisible(true);
     	}
     	
     	if (_currentEventName == null){
     		_eventSelectorShouldDisplay = false;
 			populateEventSelector();
 			_eventSelectorShouldDisplay = true;
     	} else {
     		populateEventSelector();
     	}
 		System.out.println("ABOVE LOAD EVENT");
 		//if (_currentEventName != null){
 			loadEvent();
 		//}
 	}
 	
 	
 	private class KitchenIngredientBox extends GuiBox{
     	protected String _ing;
     	protected String _toDisplay;
     	protected RemoveButton _remove;
     	protected boolean _addedByUser;
 
     	public KitchenIngredientBox(String ing, String toDisplay, boolean fromUser) {
     		super();
     		_ing= ing;
     		_toDisplay = toDisplay;
     		_addedByUser = fromUser;
     	    Label ingred = new Label(_toDisplay);
     	    this.add(ingred, 1, 0);
     	    _remove = new RemoveButton(this);
     	    _remove.setVisible(false);
     	    this.add(_remove, 0, 0);
     	}
     	
     	public void remove(){
     		System.out.println("removing ingredient " + _ing);
     		Ingredient ing = new Ingredient(_ing);
     		_client.removeIngredient(_client.getCurrentKitchen().getID(), ing);
     		ObservableList<UserIngredientBox> listItems = fridgeList.getItems();
     		listItems.remove(this);
     	}
     	
     	public RemoveButton getRemover(){
     		return _remove;
     	}
     	
     	public boolean isFromUser(){
     		return _addedByUser;
     	}
     }
 
 	public void clearKitchenDisplay(){
 		hideNewKitchenStuff();
 		communalDietPreferencesList.setText("");	
 		communalAllergiesList.setText("");
 		kitchenIngredientList.getItems().clear();
 		kitchenChefList.getItems().clear();
 		kitchenUserIngredients.getItems().clear();
 		kitchenRecipes.getChildren().clear();
 		//clear recipes
 		//set to kitchen ingredient tab
     	eventTabPane.getSelectionModel().select(kitchenIngTab);
 
 		//kitchenSelector.getSelectionModel().select(0);
     	kitchenSelector.setValue(null);
 		kitchenJunk.setDisable(true);
 		_kitchenSelectorDisplay = false;
 
 	}
 	
 	public void reDisplayKitchen() {
 		if(_client.getCurrentKitchen() != null){
 			//System.out.println("I would redisplay");
 			displayKitchen(_client.getCurrentKitchen());
 		}
 	}
 	
 
     public void newKitchenButtonListener(){
     	newKitchenPane.setVisible(true);
 
     }
     
     public void hideNewKitchenStuff(){
     	newKitchenNameField.setText("");
     	newKitchenActionText.setText("");
     	newKitchenActionText.setVisible(false);
     	newKitchenPane.setVisible(false);
     }
 
     
     public void newKitchenCreateButtonListener(){
     	String name = newKitchenNameField.getText();
     	if(name.length()>Utils.MAX_FIELD_LEN){
     		newKitchenActionText.setText("Name too long.");
     		newKitchenActionText.setVisible(true);
     		return;
     	}
     	if (name.length() == 0){
     		newKitchenActionText.setText("Please enter a name.");
     		newKitchenActionText.setVisible(true);
     	} else if (_client.getKitchenNameSet().contains(name)){
     		newKitchenActionText.setText("You've already got a kitchen with that name");
     		newKitchenActionText.setVisible(true);
     	} else {
     		newKitchenActionText.setText("");
     		newKitchenActionText.setVisible(false);
     		_client.setNewKitchen(name);
     		_client.createNewKitchen(name, _account);
     		
     		hideNewKitchenStuff();
     	}
     }
     
 	public void leaveKitchen(){
 
 		if(kitchenSelector.getValue()!=null){
 			System.out.println("leavvving");
 			clearKitchenDisplay();
 			_currentEventName = null;
 			eventSelector.setValue(null);
 			_client.removeKitchen(_client.getCurrentKitchen());
 	
 			_account.removeKitchen(_client.getCurrentKitchen());
 			_client.storeAccount(_account, _client.getCurrentKitchen().getID());
 			kitchenHide.setVisible(true);
 			_client.setCurrentKitchen(null);
 		}
 	}
 	
 	public void removeKitchenIngredients(){
 		for(KitchenIngredientBox s: kitchenIngredientList.getItems()){
 			if(s.isFromUser()){
 				RemoveButton rButton = s.getRemover();
 				rButton.setVisible(!rButton.isVisible());
 			}
 		}
 	}
 	
 	public void addAllIngredientsToKitchen(){
 		_client.addIngredientList(_client.getCurrentKitchen().getID(), _account.getIngredients());
 	}
 	
 	
 	private class DraggableIngredient extends Text {
 		String _i;
 		DraggableIngredient _self;
 		
     	public DraggableIngredient(String ingredient) {
     		super(ingredient);
     		_i = ingredient;
     		_self = this;
 
 			this.setOnDragDetected(new EventHandler <MouseEvent>() {
 	            public void handle(MouseEvent event) {
 	                /* drag was detected, start drag-and-drop gesture*/
 	                System.out.println("onDragDetected");
 	                
 	                /* allow any transfer mode */
 	                Dragboard db = _self.startDragAndDrop(TransferMode.ANY);
 	                
 	                /* put a string on dragboard */
 	                ClipboardContent content = new ClipboardContent();
 	                content.putString(_i);
 	                db.setContent(content);
 	                
 	                event.consume();
 	            }
 	        });
     	}
     }
 	
 	@FXML void hoverIngredient(DragEvent event) {
 		Dragboard db = event.getDragboard();
         if (db.hasString()) {
             event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
         }
         event.consume();
     }
 	
 	@FXML void acceptIngredient(DragEvent event){
 		Dragboard db = event.getDragboard();
         boolean success = false;
         if (db.hasString()) {
             System.out.println("Dropped: " + db.getString());
             success = true;
             _client.addIngredient(_client.getCurrentKitchen().getID(), new Ingredient(db.getString()));
         }
 
 	}
 	
 	@FXML void acceptRecipe(DragEvent event){
 		System.out.println("IN ACCEPT RECIPE");
 		Dragboard db = event.getDragboard();
         boolean success = false;
         if (db.hasString()) {
             System.out.println("RECIPE Dropped: " + db.getString());
             success = true;
             Recipe r  = getRecipeBoxFromString(db.getString()); 
             System.out.println(r);
             
             HashMap<KitchenName, Kitchen> kitchens = _client.getKitchens();
     		Kitchen k = kitchens.get(_client.getCurrentKitchen());
             KitchenEvent e = _client.getKitchens().get(_client.getCurrentKitchen()).getEvent(new KitchenEvent(_currentEventName, null, null));
             e.addRecipe(r);
             Set<Ingredient> diff = r.getIngredientDifference(k.getIngredients());
             System.out.println("ABOVE ADDING ING");
             for(Ingredient i: diff){
             	System.out.println("ADDING INGREDIENT");
             	e.addShoppingIngredient(i);
             }
             System.out.println("EVENT SHOPPING IS NOW: " + e.getShoppingIngredients());
             populateEventShoppingList();
 
             _client.addEvent(_client.getCurrentKitchen().getID(), e);
         }
 
 	}
 	
 	public void populateUserIngredientsInKitchen(){
 		kitchenUserIngredients.getItems().clear();
 		for(Ingredient i: _account.getIngredients()){
 			kitchenUserIngredients.getItems().add(new DraggableIngredient(i.getName()));
 		}
 	}
 	
 	/*
 	 * ********************************************************
 	 * Events
 	 * ********************************************************
 	 */
 	public void populateNewEventTime(){
 		populateEventTime(hour,min,amPm);
 	}
 	
 	public void populateEditEventTime(){
 		populateEventTime(editHour,editMin,editAmPm);
 	}
 	
 	public void populateEventTime(ComboBox<String> a, ComboBox<String> b, ComboBox<String> c){
 		System.out.println("CALLING POPULATE EVENT TIME");
 		a.getItems().clear();
 		//hour.setItems(null);
 		a.setValue(null);
 		a.setButtonCell(new ListCell<String>() {						
 			@Override
 			protected void updateItem(String name, boolean empty) {
 				super.updateItem(name, empty);
 				
 				if (name == null || empty || !_newTimeShouldDisplay) {
 					setText("Hr.");
 				} else {
 					setText(name);
 				}
 			}
 		});
 		a.getButtonCell().setText("Hr.");
 		a.getButtonCell().setItem(null);
 		a.getItems().addAll("1" , "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12");
 		b.getItems().clear();
 		//min.setItems(null);
 		b.setValue(null);
 		b.setButtonCell(new ListCell<String>() {						
 			@Override
 			protected void updateItem(String name, boolean empty) {
 				super.updateItem(name, empty);
 				
 				if (name == null || empty || !_newTimeShouldDisplay) {
 					setText("Min.");
 				} else {
 					setText(name);
 				}
 			}
 		});
 		b.getButtonCell().setText("Min.");
 		b.getButtonCell().setItem(null);
 		b.getItems().addAll("00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55");
 		c.getItems().clear();
 		//amPm.setItems(null);
 		c.setValue(null);
 		c.setButtonCell(new ListCell<String>() {						
 			@Override
 			protected void updateItem(String name, boolean empty) {
 				super.updateItem(name, empty);
 				
 				if (name == null || empty || !_newTimeShouldDisplay) {
 					setText("am/pm");
 				} else {
 					setText(name);
 				}
 			}
 		});
 		c.getButtonCell().setItem(null);
 		c.getButtonCell().setText("am/pm");
 		c.getItems().addAll("am", "pm");
 	}
 	
 	public void removeEventShoppingIngredient(){		
 		for(EventIngredientBox s: eventShoppingList.getItems()){
 			RemoveButton rButton = s.getRemover();
 			rButton.setVisible(!rButton.isVisible());
 		}
 	}
 	
 	public void createEventListener(){
     	String name = newEventNameField.getText();
     	HashMap<KitchenName, Kitchen> kitchens = _client.getKitchens();
     	boolean validDate = false;
     	boolean validName = false;
     	if(kitchens.get(_client.getCurrentKitchen())!=null && name!=null && eventDatePicker.getSelectedDate() !=null && name.trim().length()!= 0
     			&& hour.getValue()!= null && min.getValue() != null && amPm.getValue()!=null){
     		//Name
     		Kitchen k = kitchens.get(_client.getCurrentKitchen());
     		if (k.getEvents().contains(new KitchenEvent(name, null, null, k))){
     			newEventActionText.setText("You already have an event by that name.");
     			return;
     		} else {
     			validName = true;
     		}
 	    	if(name.length()>Utils.MAX_FIELD_LEN){
 	    		newEventActionText.setText("Name too long.");
 	    		newEventActionText.setVisible(true);
 	    		return;
 	    	}
 	    	//Date
 	    	Date date = eventDatePicker.getSelectedDate();
 		    //getting yesterday
 		    Calendar cal = Calendar.getInstance();
 		    cal.add(Calendar.DATE, -1);
 		    Date yesterday = cal.getTime();
 		    validDate = date.after(yesterday);
 		    if (validDate){
 			   	String time = hour.getValue() + ":" + min.getValue() + " " + amPm.getValue();
 		    	newEventActionText.setVisible(false);
 	    		KitchenEvent event = new KitchenEvent(name,date,time,k);
 	    		_currentEventName = name;
 	           	_client.addEvent(k.getID(), event);
 	           	eventTabPane.getSelectionModel().select(eventTab);
 		    } else {
 		    	newEventActionText.setText("Can't make an event in the past.");
 		    }
     	} else {
     		newEventActionText.setText("Please complete all fields");
     		newEventActionText.setVisible(true);
     	}
     	
    // 	System.out.println("TEXT: " + createEventField.getText());
     }
 	
     public void addEventShoppingListListener() {
     	disableRemoves(eventShoppingList);
     	removeIngFromEvent.setSelected(false);
     	String name = eventIng.getValue();
     	if(name != null && name.length()>Utils.MAX_COMBO_LEN){
     		//TODO: HANNAH this for event shipping shoppingListActionLabel.setVisible(true);
     		return;
     	}
     	if(name!=null){
     		if(name.trim().length()!=0){
     			
     			KitchenEvent e = getCurrentEvent();
     			e.addShoppingIngredient(new Ingredient(name.toLowerCase()));
     			_client.addEvent(_client.getCurrentKitchen().getID(), e);
     			populateEventShoppingList();
     			eventIng.getEditor().setText("");
     			
     		}
     		eventIng.setValue("");
     		eventIng.getItems().clear();
     	}
     }
     
     public void populateEventShoppingList(){
     	ObservableList<EventIngredientBox> listItems = FXCollections.observableArrayList();  
     	eventShoppingList.setItems(listItems);
     	for(Ingredient i: getCurrentEvent().getShoppingIngredients()){
     		EventIngredientBox box = new EventIngredientBox(i.getName());
     		listItems.add(box);
     	}
     	eventShoppingList.setItems(listItems);
     }
 	
 	public void loadEvent(){
 		if(eventSelector.getValue()!= null){
 			_currentEventName = eventSelector.getValue();;
 		}
 		else if (_currentEventName != null){
 			eventSelector.setValue(_currentEventName);
 		}
 		
 		if(eventSelector.getValue()!=null){
 			if(getCurrentEvent()!= null){
 				displayEventInfo();
 				enableEvents();
 				populateEventMenu();
 				populateEventShoppingList();
 				//populateEventSelector();
 				displayMessages();
 			}
 		} else {
 			disableEvents();
 		}

		for(EventIngredientBox s: eventShoppingList.getItems()){
			RemoveButton rButton = s.getRemover();
			rButton.setVisible(removeIngFromEvent.isSelected());
		}
 	}
 	
 	private void disableEvents(){
 		eventAnchor.setDisable(true);
 		editEventButton.setVisible(false);
 		deleteEventButton.setVisible(false);
 		eventDate.setVisible(false);
 		eventTime.setVisible(false);
 		eventCommentDisplayField.setText("");
 		eventCommentWriteField.setText("");
 		eventRecipes.getChildren().clear();
 		eventShoppingList.getItems().clear();
 		eventRecipes.getChildren().clear();
 		noEventRecipePane.setVisible(false);
 		
 	}
 	
 	private void disableEventsLite(){
 		eventAnchor.setDisable(true);
 		editEventButton.setVisible(false);
 		deleteEventButton.setVisible(false);
 		eventDate.setVisible(false);
 		eventTime.setVisible(false);
 	}
 	
 	private void enableEvents(){
 		eventAnchor.setDisable(false);
 		editEventButton.setVisible(true);
 		deleteEventButton.setVisible(true);
 		eventDate.setVisible(true);
 		eventTime.setVisible(true);
 	}
 	
 	private class EventIngredientBox extends GuiBox{
     	protected String _toDisplay;
     	protected RemoveButton _remove;
 
     	public EventIngredientBox(String display) {
     		super();
     		_toDisplay = display;
     	    Label ingred = new Label(display);
     	    this.add(ingred, 1, 0);
     	    _remove = new RemoveButton(this);
     	    _remove.setVisible(false);
     	    this.add(_remove, 0, 0);
     	}
     	
     	public void remove(){
     		System.out.println("removing ingredient " + _toDisplay);
     		Ingredient ing = new Ingredient(_toDisplay);
     		
     		KitchenEvent e = getCurrentEvent();
     		e.removeShoppingIngredient(new Ingredient(_toDisplay));
     		_client.addEvent(_client.getCurrentKitchen().getID(), e);
     		
     		ObservableList<EventIngredientBox> listItems = eventShoppingList.getItems();
     		listItems.remove(this);
     	}
     	
     	public RemoveButton getRemover(){
     		return _remove;
     	}
     }
 	
 	private KitchenEvent getCurrentEvent(){
 		HashMap<KitchenName, Kitchen> kitchens = _client.getKitchens();
 		if(kitchens!=null){
     		Kitchen k = kitchens.get(_client.getCurrentKitchen());
 			if(k!=null){
 				KitchenEvent event = k.getEvent(new KitchenEvent(_currentEventName, null, null, k));
 				return event;
 			}
 		}
 		return null;
 	}
 	
 	private void displayEventInfo(){
 		KitchenEvent event = getCurrentEvent();
 		System.out.println("event, mofucka: " + event);
 		eventDate.setText(sdf.format(event.getDate()));
 		eventTime.setText(event.getTime());
 	}
 	
 	public void eventPageSelected(){
 		if (eventSelector.getValue() == null){
 			disableEvents();
 		} else {
 			enableEvents();
 		}
 		editPane.setVisible(false);
 	}
 	
 	public void newEventPageSelected(){
 		newEventNameField.setText("");
 		eventDatePicker.setSelectedDate(null);
 		_newTimeShouldDisplay = false;
 		populateNewEventTime();
 		_newTimeShouldDisplay = true;
 	}
 	
 	public void editEventListener(){
 		disableEventsLite();
 		_newTimeShouldDisplay = false;
 		populateEditEventTime();
 		_newTimeShouldDisplay = true;
 		eventNameEdit.setText(getCurrentEvent().getName());
 		editDatePicker.setSelectedDate(getCurrentEvent().getDate());
 		editPane.setVisible(true);
 		
 	}
 	
 	public void saveEventEdits(){
     	Date date = editDatePicker.getSelectedDate();
     	//getting yesterday
     	Calendar cal = Calendar.getInstance();
     	cal.add(Calendar.DATE, -1);
     	Date yesterday = cal.getTime();
     	boolean validDate = date.after(yesterday);
     	System.out.println("date: " + date.toString());
     	String time = editHour.getValue() + ":" + editMin.getValue() + " " + editAmPm.getValue();
     	System.out.println("time: " + time);
     	HashMap<KitchenName, Kitchen> kitchens = _client.getKitchens();
     	if(date!=null && validDate//TODO: what are the combo boxes' default 
     			&& editHour.getValue()!= null && editMin.getValue() != null && editAmPm.getValue()!=null){
     		editEventActionText.setVisible(false);
     		if(kitchens.get(_client.getCurrentKitchen())!=null){
     			Kitchen k = kitchens.get(_client.getCurrentKitchen());
     			KitchenEvent event = getCurrentEvent();
     			event.setDate(date);
     			event.setTime(time);
             	_client.addEvent(k.getID(), event);
             	postChangeAsMessage(time, date);
             	//eventTabPane.getSelectionModel().select(eventTab);
             	cancelEditListener();
             	System.out.println(_currentEventName);
     		}
     		
     	} else {
     		editEventActionText.setText("");
     		editEventActionText.setVisible(true);
     		/*if (k.getEvents().contains(new KitchenEvent(name, date, kitchens.get(_client.getCurrentKitchen()))) { //TODO: This probably won't work
     			//TODO: do this check outside this else
     			//TODO: Finish this check
     		} else*/ 
     		if (!validDate){
     			editEventActionText.setText("Can't create an event in the past.");
     		} else if (hour.getValue() == null || min.getValue() == null || amPm.getValue() == null){
     			editEventActionText.setText("Invalid time!");
     		}
     	}
 	}
 	
 	public void deleteEvent(){
 
 		System.out.println("leavvving event");
 		_client.removeEvent(_client.getCurrentKitchen().getID(), getCurrentEvent());
 		_currentEventName = null;
 		eventSelector.setValue(null);
 		eventPageSelected();
 		//TODO: figure out what's going on with deleting events and the selector. fix it.
 	}
 	
 	public void cancelEditListener(){
 		
 		enableEvents();
 		editDatePicker.setSelectedDate(null);
 		populateEditEventTime();
 		editPane.setVisible(false);
 	}
 	
 	public void populateEventSelector(){
 		HashMap<KitchenName, Kitchen> kitchens = _client.getKitchens();
 		Kitchen k = kitchens.get(_client.getCurrentKitchen());
 		eventSelector.getItems().clear();
 		//If kitchen doesn't equal null.
 		if(k!=null){
 			HashSet<String> names = k.getEventNames();
 			eventSelector.getItems().addAll(k.getEventNames());
 			eventSelector.setButtonCell(new ListCell<String>() {						
 				@Override
 				protected void updateItem(String name, boolean empty) {
 					super.updateItem(name, empty);
 					
 					if (name == null || empty || !_eventSelectorShouldDisplay) {
 						setText("Select Event");
 					} else {
 						setText(name);
 					}
 				}
 			});
 			//DBUG
 			for (String e : k.getEventNames()){
 				System.out.println("an event: " + e);
 			}
 		}
 		//eventSelector.setValue(null);
 		
 	}
 	
 	public void populateEventMenu(){
 		HashMap<KitchenName, Kitchen> kitchens = _client.getKitchens();
 		eventRecipes.getChildren().clear();
     	if(kitchens!=null){
     		Kitchen k = kitchens.get(_client.getCurrentKitchen());
     		if(k!=null){
     			KitchenEvent e = k.getEvent(new KitchenEvent(_currentEventName, null, k));
     			if (e != null){
     				noEventRecipePane.setVisible(true);
 					for (Recipe recipe : e.getMenuRecipes()){
 						noEventRecipePane.setVisible(false);
 						eventRecipes.getChildren().add(new RecipeBox(recipe, this, k, e, _client));
 					}
     			}
     		}
     	}
 	}
 	
 	public void displayMessages(){
 		HashMap<KitchenName, Kitchen> kitchens = _client.getKitchens();
 		 
     	if(kitchens!=null){
     		Kitchen k = kitchens.get(_client.getCurrentKitchen());
     		if(k!=null){
     			KitchenEvent e = k.getEvent(new KitchenEvent(_currentEventName, null, k));
     			if (e != null){
     				System.out.println("event: " + e);
     				System.out.println("messages: " + e.getMessages());
     				String messages = e.getMessages().toString();
     				//eventCommentDisplayField.setVisible(false);
     				eventCommentDisplayField.setText(messages);
     				System.out.println("about to move caret");
     				eventCommentDisplayField.positionCaret(messages.length());
     				//eventCommentDisplayField.setVisible(true);
     				//TODO: see if we can make this look less shitty.
     			}
     		}
     	}
 	}
 	
 	public void postMessage(){
 		String post = eventCommentWriteField.getText();
 		if (post.length()!=0){
 			eventCommentWriteField.setText("");
 			String pre = "";
 			String mid = "";
 			String email = _account.getID();
 			String id = email.split("@")[0];
 			if (!_account.getName().equals("")){
 				mid = id + " (" + _account.getName() + "): ";
 			} else {
 				mid = id +": ";
 			}
 			if (eventCommentDisplayField.getText().length() != 0){
 				pre = eventCommentDisplayField.getText() + "\n";
 			}
 			eventCommentDisplayField.setText(pre + mid + post);
 			String forServer = mid+post+"\n";
 			System.out.println("trying to add this " + forServer);
 			_client.addMessageToEvent(_currentEventName, forServer, _client.getCurrentKitchen().getID());
 		}
 	}
 	
 	private void postChangeAsMessage(String time, Date date){
 		String post = sdf.format(date) + " at " + time + ".";
 		String mid = "";
 		String email = _account.getID();
 		String id = email.split("@")[0];
 		if (!_account.getName().equals("")){
 			mid = id + " (" + _account.getName() + ")";
 		} else {
 			mid = id;
 		}
 		String forServer = mid+ " changed the date/time of this event to " + post + "\n";
 		System.out.println("trying to add this " + forServer);
 		_client.addMessageToEvent(_currentEventName, forServer, _client.getCurrentKitchen().getID());
 	}
     
 	/*
 	 ********************************************************** 
 	 * Invite
 	 **********************************************************
 	 */
 	
 	public void popupInvite(){
 		final Controller2 control = this;
 		
 		Platform.runLater(new Runnable() {
     		@Override
     		public void run() {
 				try {
 					URL location = getClass().getResource("InviteChefWindow.fxml");
 					FXMLLoader fxmlLoader = new FXMLLoader();
 					fxmlLoader.setLocation(location);
 					fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
 					Parent p = (Parent) fxmlLoader.load(location.openStream());
 			        _inviteChefController = (InviteChefController) fxmlLoader.getController();
 			        _inviteChefController.setController(control);
 			        Stage stage = new Stage();
 			        stage.setScene(new Scene(p));
 			        stage.setTitle("InviteChef");
 				    stage.show();
 				} catch (IOException e) {
 					System.out.println("ERROR: IN GUI 2 Frame");
 					e.printStackTrace();
 				}
     		}
 		});
 	}
 	
 	public void inviteToJoinCWF(String email){
 		String message = "Hi there, \n " + _account.getName() + "(" + _account.getID() +") "
 				+ "wants to invite you to join Cooking with Friends, the social cooking coordinator.";
 		message += "To accept this invitation, you must log in and accept.";
 		System.out.println("SENDING TO : " + email);
 		Sender.send(email, message);
 		
 	}
 	
 	
 	
 	public void checkAndSendEmail(String email){
 		System.out.println("IN CHECK AND SEND EMAIL.");
 		if(email != null){
 			//Will call sendInviteEmail.
 			_client.userInDatabase(email);
 		}
 	}
 		
 	public void sendInviteEmails(boolean userInDatabase){
 		_inviteChefController.sendInviteEmails(userInDatabase);
 	}
 
 	/**
 	 * Returns a string message that longin controller will display.
 	 * @param toInvite
 	 * @return
 	 */
 	public String inviteUserToKitchen(String toInvite){
 		if(_client.getKitchens()!=null){
 			
 			Kitchen k = _client.getKitchens().get(_client.getCurrentKitchen());
 			if(k.getActiveUsers().contains(toInvite)){
 				return "User is already in the kitchen.";
 			}
 			if(k.getRequestedUsers().contains(toInvite)){
 				
 				return "User has already been invited.";
 			}
 			_client.addRequestedKitchenUser(toInvite, _account.getName(), _client.getCurrentKitchen());
 			
 			//instant display update
 			Text t = new Text(toInvite + " (pending)");
 			t.setFont(Font.font("Verdana", FontPosture.ITALIC, 10));
 			t.setFill(Color.GRAY);
 			kitchenChefList.getItems().add(t);
 			
 			String message = "Hi there, \n " + _account.getName() + "(" + _account.getID() +") "
 					+ "wants you to join the kitchen, " + k.getName();
 			message += ". To accept this invitation, you must log in and accept.";
 			Sender.send(toInvite, message);
 			return "Message sent to user.";
 
 		}
 		return "";
 	}
 	
     private class InvitationBox extends GridPane{
     	protected Invitation _invite;
 
     	public InvitationBox(Invitation invite){
     		_invite = invite;
 
     		
     		Label message = new Label(_invite.getMessage());
     		message.setPrefWidth(400);
     		message.setWrapText(true);
 
     		if(message != null){
 	    		Button accept = new Button("Accept");
 	    		accept.setOnAction(new EventHandler<ActionEvent>(){
 	    			@Override
 	    			public void handle(ActionEvent e){
 	    			//	System.out.println("Accept invitatioN!!!");
 	    				
 	    				_account.getInvitions().remove(_invite.getKitchenID());
 	    				_account.getKitchens().add(_invite.getKitchenID());
 	    				_client.storeAccount(_account);
 	    				_client.addActiveKitchenUser(_invite.getKitchenID().getID(), _account);
 	    				populateInvitations();
 	    			}
 	    		});
 	    		
 	    		Button decline = new Button("Decline");
 	    		decline.setOnAction(new EventHandler<ActionEvent>(){
 	    			@Override
 	    			public void handle(ActionEvent e){
 	    			//	System.out.println("REJECT invitatioN!!!");
 	    				
 	    				_account.getInvitions().remove(_invite.getKitchenID());
 	    				_client.storeAccount(_account);
 	    				_client.removeRequestedKitchenUser(_invite.getKitchenID().getID());
 	    				populateInvitations();
 	    			}
 	    		});
 	    		
 	    		this.add(message, 0, 0);
 	    		this.add(accept, 1, 0);
 	    		this.add(decline, 2, 0);
     		}
 
     	}
 
     }
     
 	public void populateInvitations(){
 		
 		invitationsList.getItems().clear();
 		HashMap<KitchenName, Invitation> invites = _account.getInvitions();
 		numberOfInvites.setText(Integer.toString(invites.size()));
 
 		System.out.println("user has " + invites.size() + " invitations!!!");
 		if(invites.size()==0){
 			invitationsList.setVisible(false);
 			enablePleasantries(true);
 		}
 		else{
 			for(KitchenName kn: _account.getInvitions().keySet()){
 				invitationsList.getItems().add(new InvitationBox(invites.get(kn)));
 			}
 		}
 	
 	}
 
 
 	
    // @FXML void addFromMyFridgeListener(ActionEvent event) {
    // }
 
     @FXML void addRtoEMode(ActionEvent event) {
     }
 
 	public void displayInvitations(){
 		if(invitationsList.isVisible()){
 				invitationsList.setVisible(false);
 				enablePleasantries(true);
 		}
 		else{
 			if(_account.getInvitions().size()!=0){
 				invitationsList.setVisible(true);
 				enablePleasantries(false);
 				populateInvitations();
 			}
 		}
 	}
 
     @FXML void goToRecipeTab(ActionEvent event) {
     	tabPane.getSelectionModel().select(recipeSearchTab);
     }
     
     
     /*
 	 ********************************************************** 
 	 * Search Page
 	 **********************************************************
 	 */
     
     public void setUpSearchPage() {
     	ingredientsAccordion.expandedPaneProperty().addListener(new ChangeListener<TitledPane>() {
             @Override 
             public void changed(ObservableValue<? extends TitledPane> property, final TitledPane oldPane, final TitledPane newPane) {
             	if (oldPane != null) 
             		oldPane.setCollapsible(true);
             	if (newPane != null) {
             		Platform.runLater(new Runnable() { 
             			@Override public void run() { 
             				newPane.setCollapsible(false); 
             		 	}
             		});
             	}
             }
         });
     }
     
     @FXML void searchButtonListener(MouseEvent event) {
     	NoSearchResults.setVisible(false);
 		resultsFlow.getChildren().clear();
 		if(searchField.getText().length()>Utils.MAX_FIELD_LEN){
 			lengthRecipeSearchLabel.setVisible(true);
 			lengthRecipeSearchLabel.setText("You may not make a search longer than " + Utils.MAX_FIELD_LEN + " characters long.");
 			return;
 		}
 			
 		if (_currentKitchenPane == null) {
 			NoSearchResults.setText("Please select a kitchen");
 			NoSearchResults.setVisible(true);
 			return;
 		}
 			
 		try { //Attempt to query API
 			List<String> dummyList = Collections.emptyList(); 
 			List<String> selectedIngredients = _currentKitchenPane.getSelectedIngredients();
 						
 			List<? extends Recipe> results = _api.searchRecipes(searchField.getText(), _currentKitchenPane.getSelectedIngredients(), 
 					dummyList, _currentKitchenPane.getRestrictions(), _currentKitchenPane.getAllergies());
 			
 			if (results.size() == 0) {
 				searchPromptPane.setVisible(true);
 				NoSearchResults.setVisible(true);
 			}
 			else {
 		    	searchPromptPane.setVisible(false);
 				for (Recipe recipe : results)
 					resultsFlow.getChildren().add(new RecipeBox(recipe, this));
 			}
 		} catch (IOException ex) {
 			NoSearchResults.setText("Error querying API -- is your internet connection down?");
 			NoSearchResults.setVisible(true);
 		}
     }
     
     public void createPopup(Recipe recipe) {
     	try {
 	    	Recipe completeRecipe = _api.getRecipe(recipe.getID());
 	    	
 	    	URL location = getClass().getResource("RecipeWindow.fxml");
 			FXMLLoader fxmlLoader = new FXMLLoader();
 			fxmlLoader.setLocation(location);
 			fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
 			Parent p = (Parent) fxmlLoader.load(location.openStream());
 			RecipeController recipeControl = (RecipeController) fxmlLoader.getController();
 			recipeControl.setUp(recipe, completeRecipe, _client, _account, this);
 			
 			Stage stage = new Stage();
 	        stage.setScene(new Scene(p));
 	        stage.setTitle("View Recipe");
 	        //stage.sizeToScene();
 		    //stage.centerOnScreen();
 		    stage.show();		
 		    
 //			Popup popup = new Popup();
 //	        popup.getContent().add(p);
 //	        popup.setAutoFix(true);
 //	        popup.setAutoHide(true);
 //	        popup.setHideOnEscape(true);
 //	        popup.show(root, 0, 0);
 //	        popup.sizeToScene();
 //	        popup.centerOnScreen();	
 //          Point2D center = Utils.getCenter(this.get);
 //          popup.show(mainClass.getOptionsStage(),
 //                    center.getX() - popup.getWidth() / 2,
 //                    center.getY() - popup.getHeight() / 2);      
     	} catch (IOException ex) {
     		NoSearchResults.setText("Error querying API -- is your internet connection down?");
 			NoSearchResults.setVisible(true);
     	}
 	}
     
     public void populateSearchIngredients() {
     	List<KitchenPane> kitchenPanes = new ArrayList<>();
         kitchenPanes.add(new KitchenPane("My Fridge", _account.getIngredients(), _account.getDietaryRestrictions(), _account.getAllergies()));
                 
         for (Kitchen kitchen : _kitchens.values())
         	kitchenPanes.add(new KitchenPane(kitchen.getName(), kitchen.getIngredients(), kitchen.getDietaryRestrictions(), kitchen.getAllergies()));
       
         ingredientsAccordion.getPanes().clear();
         ingredientsAccordion.getPanes().addAll(kitchenPanes);
         ingredientsAccordion.setExpandedPane(kitchenPanes.get(0));
         _currentKitchenPane = kitchenPanes.get(0);
 	}
     
     /**
      * Called when the recipe search pane is selected/deselected.
      * If unselected, the recipe search tab is refreshed.
      * @param event
      */
     @FXML
     public void searchSelectionChanged(Event event) {
     	if (!recipeSearchTab.isSelected())
     		populateSearchIngredients();
     }
     
     /**
      * Called when a kitchen is passed back to client -- doesn't update search ingredient list.
      */
     public void refreshSearchAccordion() {
     	if (!recipeSearchTab.isSelected())
     		populateSearchIngredients();		
 	}
     
     private class KitchenPane extends TitledPane {
     	private List<CheckBox> _ingredientBoxes;
     	private KitchenPane _thisPane;
     	private Set<String> _allergies, _restrictions;
     	
     	public KitchenPane(String name, Set<Ingredient> ingredients, Set<String> restrictions, Set<String> allergies) {
     		super();
     		_ingredientBoxes = new ArrayList<>();
     		_thisPane = this;
     		_allergies = allergies;
     		_restrictions = restrictions;
     		
     		this.setText(name); 	
     		this.setContent(this.makeIngredientsList(ingredients)); 	
     		this.expandedProperty().addListener(new ChangeListener<Boolean>() {
 				@Override
 				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
 					if (arg2) _currentKitchenPane = _thisPane;
 				}
     		});
     	}
     	
     	public List<String> getAllergies() {
 			return new ArrayList<String>(_allergies);
 		}
 
 		public List<String> getRestrictions() {
 			return new ArrayList<String>(_restrictions);
 		}
 
 		public List<String> getSelectedIngredients() {
 			List<String> selectedIngredients = new ArrayList<>();
 			for (CheckBox ingredientBox : _ingredientBoxes) {
 				if (ingredientBox.isSelected()) {
 					selectedIngredients.add(ingredientBox.getText());
 				}
 			}
 			return selectedIngredients;
 		}
 
     	public ListView<CheckBox> makeIngredientsList(Set<Ingredient> ingredients) {
     		ListView<CheckBox> ingredientsView = new ListView<>();    		
     		for (Ingredient ing : ingredients)
     			_ingredientBoxes.add(new CheckBox(ing.getName()));
     		CheckBox selectAll = new SelectAllBox(_ingredientBoxes);
     		ingredientsView.getItems().add(selectAll);
     		ingredientsView.getItems().addAll(_ingredientBoxes);
 			return ingredientsView;
     	}
     }
     
     private class SelectAllBox extends CheckBox {
     	private List<CheckBox> _associatedBoxes;
     	private CheckBox _allBox;
     	
     	public SelectAllBox(List<CheckBox> boxes) {
     		_associatedBoxes = boxes;
     		this.setText("Select all");
     		this.getStyleClass().add("selectAllBox");
     		_allBox = this;
     		
     		this.setOnAction(new EventHandler<ActionEvent>() {
 			    @Override
 			    public void handle(ActionEvent event) {
 			    	for (CheckBox box : _associatedBoxes) {
 		        		box.setSelected(_allBox.isSelected());
 		        	}
 			    }
 			});
     	}	
     }
     
     
     /**
      * COMBO BOX LISTENERS. _-----------------------------------------------
      */
     
     /**
      * Listener for adding ingredient to shopping list.
      */
     public void shoppingListComboListener(){
     	String text = addShoppingIngredient.getEditor().getText();
     	if(text != null){
     		addShoppingIngredient.getItems().clear();
     		List<String> suggs = null;
     		if(text.trim().length()!=0){
     			System.out.println("TEXT: " + text);
 	    		suggs = _engines.getIngredientSuggestions(text.toLowerCase());
 
 	    	    if(suggs!=null){
 	    	    	addShoppingIngredient.show();
 		    		addShoppingIngredient.getItems().clear();
 		    		addShoppingIngredient.getItems().addAll(suggs);
 		    	}
     		}
     	}
     	else{
     		 addShoppingListListener();
     	}
     }
     
     public void ingredientComboListener(){
     	String text = newIngredient.getEditor().getText();
     	if(text != null){
     		newIngredient.getItems().clear();
     		List<String> suggs = null;
     		if(text.trim().length()!=0){
     			System.out.println("TEXT: " + text);
 	    		suggs = _engines.getIngredientSuggestions(text.toLowerCase());
 
 	    	    if(suggs!=null){
 	    	    	newIngredient.show();
 	    	    	newIngredient.getItems().clear();
 	    	    	newIngredient.getItems().addAll(suggs);
 		    	}
     		}
     	}
     	else{
     		 addShoppingListListener();
     	}
     }
 
 	public void eventIngredientComboListener(){
 		String text = eventIng.getEditor().getText();
 		System.out.println("EVENT COMBO: " + text);
     	if(text != null){
     		eventIng.getItems().clear();
     		List<String> suggs = null;
     		if(text.trim().length()!=0){
     			System.out.println("TEXT: " + text);
 	    		suggs = _engines.getIngredientSuggestions(text.toLowerCase());
 
 	    	    if(suggs!=null){
 	    	    	eventIng.show();
 	    	    	eventIng.getItems().clear();
 	    	    	eventIng.getItems().addAll(suggs);
 		    	}
     		}
     	}
     	else{
     		addEventShoppingListListener();
     	}
 	}
     
 	public void recieveInvite(Invitation invitation) {
 		_account.addInvitation(invitation);
 		_client.storeAccount(_account);
 		populateInvitations();
 		
 	}
 	
 	public static String getRecipeBoxString(RecipeBox rb) {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos;
 		try {
 			oos = new ObjectOutputStream(baos);
 			oos.writeObject(rb);
 	        oos.close();
 		} catch (IOException e) {
 			System.out.println("ERROR: Could not make serializable object." + e.getMessage());
 		}
 		//Imports all of this so it doesn't conflict with the other Base64 import above.
 		Base64 encoder = new Base64();
         return new String(encoder.encode(baos.toByteArray()));
     }
 	
     private static Recipe getRecipeBoxFromString( String s ) {
     	try{
     		//BASE64Decoder decoder = new BASE64Decoder();
     		Base64 decoder = new Base64();
     		//byte [] data = decoder.decodeBuffer( s );
         	byte [] data = decoder.decode( s );
             ObjectInputStream ois = new ObjectInputStream( 
                                             new ByteArrayInputStream(  data ) );
             Recipe o  =  (Recipe) ois.readObject();
             ois.close();
             return o;
     	} catch(IOException  e){
     		System.out.println("ERROR: Could not convert from object string: " + e.getMessage());
     		return null;
     	} catch(ClassNotFoundException e){
     		System.out.println("ERROR: Could not convert from object string: " + e.getMessage());
     		return null;
     	}
     }
     
     public void addNewKitchenToAccount(Kitchen k){
     	_account.addKitchen(k.getKitchenName());
     	_client.storeAccount(_account);
     }
 
 }
