 package com.google.gwt.forum.client;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Iterator;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.event.dom.client.MouseDownEvent;
 import com.google.gwt.event.dom.client.MouseDownHandler;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RichTextArea;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 
 public class Forum implements EntryPoint {
  
 	private Button insert_text_button = new Button("Insert Text");
 
   private AbsolutePanel mainPanel = new AbsolutePanel();
   private HorizontalPanel addPanel = new HorizontalPanel();
   private HorizontalPanel insertPanel = new HorizontalPanel();
   private HorizontalPanel toolbarPanel = new HorizontalPanel();
   private HorizontalPanel loginPanel = new HorizontalPanel();
   private VerticalPanel vp = new VerticalPanel();
   
   private FlexTable forumFlexTable = new FlexTable();
   private TextBox username_textbox = new TextBox();
   private TextBox password_textbox = new TextBox();
   private Button login_button = new Button("Login");
   private Label lastUpdatedLabel = new Label();
  
   private RichTextArea messages_editor = new RichTextArea();
   
   private TextArea insertCityTextA = new TextArea();
   private Button insertProjectButton = new Button("Insert");
   
   private ArrayList<Topics> topics  = new ArrayList<Topics>();
   private ArrayList<Thread> threads = new ArrayList<Thread>();
   private ArrayList<Message> messages = new ArrayList<Message>();
  // private ArrayList<Topics> results;
   
   //private static final int REFRESH_INTERVAL = 5000; // ms
   private Label errorMsgLabel = new Label();
   public int currentElementId = -1;
   public char currentElementType = 'X';
   private int topics_index = -1;
   private User current_user = null;
   
   
   
   
   /**
    * Entry point method.
    */
   public void onModuleLoad() {
 	  
 
 
     setUncaughtExceptionHandler();	// Useful!
         
 	// Setting panels
 	RootPanel.get().setWidth("1000px");
 	mainPanel.setWidth("1000px");
 	RootPanel.get().addStyleName("rootStyle");
 	mainPanel.addStyleName("mainStyle");
 
 
  	// Create table for stock data.
 	forumFlexTable.setText(0, 0, "Subject");
 	forumFlexTable.setText(0, 1, "Messages No.");
 	forumFlexTable.setText(0, 2, "Last Message");
 	forumFlexTable.setText(0, 3, "Enter");
 		 
 	// Add styles to elements in the stock list table.
 	forumFlexTable.getRowFormatter().addStyleName(0, "watchListHeader");
 	forumFlexTable.addStyleName("watchList");
 	forumFlexTable.getCellFormatter().addStyleName(0, 1, "watchListNumericColumn");
 	forumFlexTable.getCellFormatter().addStyleName(0, 2, "watchListNumericColumn");
 	forumFlexTable.getCellFormatter().addStyleName(0, 3, "watchListNumericColumn");
     
     // Assemble Insert Stock panel.
     insertCityTextA.setVisibleLines(4);
     insertCityTextA.addStyleName("textBox");
     insertProjectButton.addStyleName("button");
     insertProjectButton.setWidth("100px");
     insertPanel.add(insertCityTextA);
     insertPanel.add(insertProjectButton);
 	    
     insertPanel.addStyleName("addPanel");
 
     // Assemble Error panel.
     errorMsgLabel.setStyleName("errorMessage");
     errorMsgLabel.setVisible(false);
     mainPanel.add(errorMsgLabel);
 	    
     createToolbar();
 
     // Assemble Main panel.
     mainPanel.add(forumFlexTable);
     mainPanel.add(addPanel);
     mainPanel.add(insertPanel);
     mainPanel.add(lastUpdatedLabel);
 
 
 	
     // Add panels to the root panel
 	RootPanel.get().add(toolbarPanel);
     RootPanel.get().add(mainPanel);
 
   // refreshTimer.scheduleRepeating(REFRESH_INTERVAL);
     
     // Listen for mouse events on the Add button.
     insertProjectButton.addClickHandler(new ClickHandler() {
       public void onClick(ClickEvent event) {
        // insertCity();
       }
     });
     
     // Listen for keyboard events in the input box.
     insertCityTextA.addKeyPressHandler(new KeyPressHandler() {
       public void onKeyPress(KeyPressEvent event) {
         if (event.getCharCode() == KeyCodes.KEY_ENTER) {
        //   insertCity();
         }
       }
     });
     
     // Adding topics to the flextable
 	load_topics();
 	
 	pruebas_mary();
   }
 
   /**
 	 * Show the list of topics.
 	 */
 	private void showTopics() {
 		
 		clean_table();
 		forumFlexTable.setText(0, 1, "Messages No.");
 		
 		currentElementType = 'P';
 		for(Topics top:topics){
 			addDataToSource(top.subject, "N\\A", null, top.id);
 		}
 		
 	}
 
 	/**
 	 * Show the list of topics.
 	 */
 	private void showThreads() {
 		
 		clean_table();
 		forumFlexTable.setText(0, 1, "Messages No.");
 		
 		currentElementType = 'T';
 		for(Thread th:threads){
 			if(th.parent_topic_id == currentElementId)
 				addDataToSource(th.title, String.valueOf( th.no_messages ), null, th.id);
 		}
 		
 	}
 	
 	/**
 	 * Show the list of topics.
 	 */
 	private void showMessages() {
 		
 		clean_table();
 		forumFlexTable.setText(0, 1, "Author");
 		
 		currentElementType = 'M';
 		for(Message ms:messages){
 			if(ms.parent_thread_id == currentElementId)
 				addDataToSource( ms.content, ms.author, ms.time_stamp.toString(), ms.id);
 		}
 		
 	}
   
 	/**
 	 * Refresh the forum table 
 	 * */
 	private void refresh(){
 		
 		clean_table();
 		if(currentElementType == 'M'){
 
 			for(Message ms:messages){
 				if(ms.parent_thread_id == currentElementId)
 					addDataToSource( ms.content, ms.author, ms.time_stamp.toString(), ms.id);
 			}
 		}else if(currentElementType == 'T'){
 			for(Thread th:threads){
 				if(th.parent_topic_id == currentElementId)
 					addDataToSource(th.title, String.valueOf( th.no_messages ), null, th.id);
 			}
 		}else if(currentElementType == 'P'){
 			for(Topics top:topics){
 				addDataToSource(top.subject, "N\\A", null, top.id);
 			}
 		}
 		
 	}
   
 	  /**
 	   * Add a new topic to FlexTable adn db.
 	   */
 	  private void addTopic() {
 		  
 		  	// The constructor does the work of insert the new topic in the database
 		  	final Topics n_top = new Topics( username_textbox.getText().toUpperCase() );
 		 
 		  	username_textbox.setFocus(true);
 		    
 		  	// TODO: Check there is no topic with the same subject
 		  	/*
 			if (!topics.contains(n_top)){
 				Window.alert("The inserted city: '" + city + "' is not a valid city.");
 			      username_textbox.selectAll();
 			      return;
 			}*/
 			
 			if (topics.contains(n_top)){
 				Window.alert("The inserted topic: '" + n_top + "' is already in the system.");
 			      username_textbox.selectAll();
 			      return;
 			}
 			
 			// Add the city data	TODO: Add the date of the last message
 		    addDataToSource(n_top.subject, "N\\A" ,null, n_top.id);
 	
 		    // Get the stock price.
 		    showTopics();
 		    username_textbox.setText("");
 	
 	  }
   
 	  /**
 	   * Insert data on the source table
 	   * */
 	  private void addDataToSource(final String col1, final String col2, final String col3, final int id){
 		  
 		  int row = forumFlexTable.getRowCount();
 		    
 		  HorizontalPanel optionsPanel = new HorizontalPanel();
 		  
 		  final Label column_1 = new Label(col1);
 		  forumFlexTable.setWidget(row, 0, column_1);
 		  
 		  final Label column_2 = new Label(col2);
 		  forumFlexTable.setWidget(row, 1, column_2);
 		  
 		  final Label column_3 = new Label(col3);
 		  forumFlexTable.setWidget(row, 2, column_3);
 		  
 		  // Style
 		  forumFlexTable.getCellFormatter().addStyleName(row, 0, "watchListCell");
 		  forumFlexTable.getCellFormatter().addStyleName(row, 1, "watchListCell");
 		  forumFlexTable.getCellFormatter().addStyleName(row, 2, "watchListCell");
 		  forumFlexTable.getCellFormatter().addStyleName(row, 3, "watchListCell");
 	
 		  // Add a click listener to save the information about the row
 		  // Column1 used to have a click listener, but we will avoid it for now
 	
 		  // Add a button to enter in the element (topic or thread).
 		  Button enterButton = new Button(">");
 		  enterButton.addStyleDependentName("enter");
 		  enterButton.addClickHandler(new ClickHandler() {
 		    public void onClick(ClickEvent event) {
 
 		    	currentElementId = id;
 		    	System.out.println("CLICK");
 		    	  if(currentElementType == 'P'){
 		    		  System.out.println("Loading threads");
 		    		  load_threads();
 		    	  }else if(currentElementType == 'T'){
 		    		  System.out.println("Loading messages");
 		    		  load_messages();
 		    	  }else{
 		    		  System.out.println("Not able to check current type");
 		    	  }
 		    }
 		  });
 		  
 		  
 		  // Add a button to enter in the element (topic or thread).
 		  Button removeButton = new Button("X");
 		  removeButton.addStyleDependentName("remove");
 		  removeButton.addClickHandler(new ClickHandler() {
 			  
 				int current_id = id;
 			    char current_type = currentElementType;
 			    
 			    public void onClick(ClickEvent event) {
 			    	remove_row(current_id, current_type);
 			    }
 		    
 		  });  
 		  
 		  // The option you see depends on who is logged in
 		  optionsPanel.add(enterButton);
 		  if( current_user != null ){	// If someone is logged in
 			  if(!current_user.is_admin) optionsPanel.add(removeButton);
 		  }
 		  forumFlexTable.setWidget(row, 3, optionsPanel);
 		  
 	  }
 	
 	  /**
 	   * Removes a row from the local array
 	   * */
 	  public void remove_row(int id, char type){
 		  
 		  if(type == 'P'){
 			  for(Topics x:topics){
 				  if(x.id == id) topics.remove(x);
 				  // TODO: Erase it also from database
 				  return;
 			  }
 		  }else if(type == 'T'){
 			  for(Thread x:threads){
 				  if(x.id == id) threads.remove(x);
 				  // TODO: Erase it also from database
 				  return;
 			  }
 		  }else if(type == 'M'){
 			  for(Message x:messages){
 				  if(x.id == id) messages.remove(x);
 				  // TODO: Erase it also from database
 				  return;
 			  }
 		  }else{
 			  System.out.println("Error: fail when trying to erase a row.");
 		  }
 	  }
 	  
 	  /**
 	   * Class to load the topics int the topic object from the database
 	   */
 	  private void load_topics(){
 		  	
 		    MyServiceAsync dbService = (MyServiceAsync) GWT.create(MyService.class);
 	
 		    String temp = " cadena ";
 		    dbService.get_topics(temp, new AsyncCallback<ArrayList<Topics>>(){
 		    	public void onSuccess(ArrayList<Topics> result) {
 		    		System.out.println("TOPICS:" + result);
 		    		topics = result;
 		 
 		    		showTopics();
 		          }
 	
 		          public void onFailure(Throwable caught) {
 		        	Window.alert("RPC to initialize_db() failed.");
 		      		System.out.println("Fail\n" + caught);
 		          }
 		    } );	  
 	  }
   
 	  /**
 	   * Class to load the threads in the topic object from the database
 	   */
 	  private void load_threads(){
 
 		  final ArrayList<Thread> result = new ArrayList<Thread>();	
 		  MyServiceAsync Service = (MyServiceAsync) GWT.create(MyService.class);
 		  System.out.println(" :"+currentElementId +" "+ topics_index);  
 		  if( (currentElementId != -1) ){		    	
 
 		    Service.get_threads(currentElementId, new AsyncCallback<ArrayList<Thread>>(){
 		    	public void onSuccess(ArrayList<Thread> results) {
 		    		System.out.println("RESULTADO GET THREADS:" + results);	    		
 		    		// Save the threads in their parent topic arraylist
 		    		threads = results;
 		    		for(Thread x:results)
 		    			System.out.println("ID: "+x.id+" Subject: "+x.title+" N.Messages: "+x.no_messages+" Parent: "+x.parent_topic_id);
 		    		for(Thread x:threads)
 		    			System.out.println("ID: "+x.id+" Subject: "+x.title+" N.Messages: "+x.no_messages+" Parent: "+x.parent_topic_id);
 		    		showThreads();
 		    		
 		          }
 		    	
 		          public void onFailure(Throwable caught) {
 		        	Window.alert("Threads retrive attempt failed.");
 		      		System.out.println("Fail\n" + caught);
 		          }
 		    } );		    
 		  }	    
 	  }
 	  
 	  
 	  /**
 	   * Class to load the threads in the topic object from the database
 	   */
 	  private void load_messages(){
 
 		  final ArrayList<Message> result = new ArrayList<Message>();	
 		  MyServiceAsync Service = (MyServiceAsync) GWT.create(MyService.class);
 		    
 		  if( (currentElementId != -1) ){		    	
 
 		    Service.get_messages(currentElementId, new AsyncCallback<ArrayList<Message>>(){
 		    	public void onSuccess(ArrayList<Message> results) {
 		    		System.out.println("RESULTADO GET Messages:" + results.get(0).content);
 	
 		    		// Save the threads in their parent topic arraylist
 		    		messages = results;
 		    		for(Message x:messages){
 		    			System.out.println("ID: "+x.id+" Content: "+x.content+
 		    					" Parent: "+x.parent_thread_id+" Date: "+x.time_stamp+" Author:"+x.author);
 		    		}
 		    		showMessages();
 		          }
 		          public void onFailure(Throwable caught) {
 		        	Window.alert("Messages retrieve attempt failed.");
 		      		System.out.println("Fail\n" + caught);
 		          }
 		    } );		    
 		  }	    
 	  }
 	
 	  
 	  /**
 	   * Update a single row in the stock table.
 	   *
 	   * @param price Stock data for a single row.
 	   */
 	/*
 	 private void updateTable(InvestData ammount) {
 	    // Make sure the stock is still in the stock table.
 	    if (!awards.contains(ammount.getCity())) {
 	      return;
 	    }
 
 	    int row = awards.indexOf(ammount.getCity()) + 1;
 
 	    // Format the data in the Price and Change fields.
 	    String priceText = NumberFormat.getFormat("#,##0.00").format(
 	    		ammount.getAmmount());
 	    NumberFormat changeFormat = NumberFormat.getFormat("+#,##0.00;-#,##0.00");
 	    String changeText = changeFormat.format(ammount.getChange());
 
 	    // Populate the Price and Change fields with new data.
 	    forumFlexTable.setText(row, 1, priceText);
 	    Label changeWidget = (Label)forumFlexTable.getWidget(row, 2);
 	    changeWidget.setText(   changeText + "%");
 	    
 	    // Change the color of text in the Change field based on its value.
 	    String changeStyleName = "noChange";
 	    if (ammount.getChange() < -0.1f) {
 	      changeStyleName = "negativeChange";
 	    }
 	    else if (ammount.getChange() > 0.1f) {
 	      changeStyleName = "positiveChange";
 	    }
 
 	    changeWidget.setStyleName(changeStyleName);
 	    
 	  }
 	 */
 	/**
 	 * Inserts a new City on the table that didn't exists until now
 	 * */
 	/*
 	 private void insertCity( ) {
 
 	  final String text = insertCityTextA.getText().toUpperCase().trim();
 	  insertCityTextA.setFocus(true);
 	  String[] result = text.split("\\s");
 	  int size = result.length;
 	  int j= 0;
 	  int obt_amount=0;
 
 	 
 	 Checks for invalid input: 
 	  * 	1 - less than 2 parameters
 	  *     2 - City already in the system
 	  *	    3 - Non-numeric character in the amount
 	*//*
 	  if (size <2){
 	    	Window.alert("It must content: CITY AMMOUNT");
 		      return;
 	  }
 	  if (cities.contains(result[j])){
 		  
 	    	Window.alert("The city: '" + result[j] + "' is already in the system.");
 		      return;
 	  }	  
 	  String money = result[j+1];
 	  
 	  try {  
 		  //int temp = Integer.parseInt(money);
 	  
 	  }catch(NumberFormatException nfe){  
 		  Window.alert("The parameter amount must be numeric ");
 	      return;  
 	  }  
 
 	  obt_amount = Integer.parseInt(money);
 	  elements.add(new InvestData(result[j],obt_amount,0));
 	  addTopic(result[j]);
 	  cities.add(result[j]);
 	  amounts.add(obt_amount);
 	  insertCityTextA.setText("");
 	  
 	  
 	    MyServiceAsync emailService = (MyServiceAsync) GWT.create(MyService.class);
 	    emailService.insert_into_db("cities", "('"+result[j]+"', "+ obt_amount +")", new AsyncCallback<String>(){
 	    	public void onSuccess(String result) {
 	    		results = result;
 	          }
 
 	          public void onFailure(Throwable caught) {
 	        	Window.alert("Inserting new city in the DataBase failed.");
 	      		System.out.println("Fail\n" + caught);
 	          }
 	    } );
 	    // Depuration prints
 	    System.out.println(cities);
 	    System.out.println(amounts);
 	}*/
 	  
 	  
 	  
 	  /**
 	   * Logs out. Just set the current user to null
 	   * */
 	  public void logout(){
 		  current_user = null;
 		  
 		  loginPanel.clear();
 		  loginPanel.removeFromParent();
 		  login_zone();
 		  vp.clear();
		  vp.removeFromParent();
 	  }
 	  
 	  /**
 	   * Attemps to login in the system. Gets the current user only
 	   * if the username and the password matches with a row of the
 	   * users table in the database
 	   * */
 	  public void login(){
 		  
 		  String username = username_textbox.getText();
 		  String password = password_textbox.getText();
 		  	
 		  MyServiceAsync Service = (MyServiceAsync) GWT.create(MyService.class);
 		    
 		  Service.check_user(username, password, new AsyncCallback<User>(){
 			  
 			  public void onSuccess(User result) {
 				  System.out.println("RESULTADO check user:" + result.user_name);
 	    		
 				  current_user = result;
 				  
 				  // Things to be done when a user logs in
 				  loginPanel.clear();
 				  loginPanel.removeFromParent();
 				  logged_message();
 				  new_message_panel();
 	          }
 	    	
 	          public void onFailure(Throwable caught) {
 	        	Window.alert("Login attempt failed.");
 	      		System.out.println("Fail\n" + caught);
 	          }
 	    } );
 		 	  
 	  }
 	  
 	  /**
 	   * Creates a toolbar for the forum
 	   * */
 	  public void createToolbar(){
 		  
 		  back_button();
 		  refresh_button();
 		  
 		  if(current_user == null){
 			  login_zone();
 		  }else{
 			  if(!current_user.is_admin){
 				  // Ver usuarios (para poder gestionarlos)
 			  }
 			  // Add here another logged functionalities
 			  logged_message();
 			  
 			  // This is not part of the toolbar, but it should be shown
 			  // only when the user is logged
 			  new_message_panel();
 		  }
 		  
 		  
 	  }
 	  
 	  /**
 	   * Creates the login boxes and the login button
 	   * */
 	  public void login_zone(){
 		  
 		  Label username_label = new Label("Username:");
 		  Label password_label = new Label("Password:");
 		  username_label.addStyleName("loginText");
 		  password_label.addStyleName("loginText");
 		  
 		  // Assemble Login panel.
 		  username_textbox.addStyleName("username_textbox");
 		  password_textbox.addStyleName("password_texrbox");
 		  login_button.addStyleName("button");
 		  login_button.setWidth("70px");
 			
 		  loginPanel.add(username_label);
 		  loginPanel.add(username_textbox);
 		  loginPanel.add(password_label);
 		  loginPanel.add(password_textbox);
 		  loginPanel.add(login_button);
 		  loginPanel.addStyleName("loginPanel");
 		  toolbarPanel.add(loginPanel);
 		  
 		  // Listen for mouse events on the Login button.
 		  login_button.addClickHandler(new ClickHandler() {
 			  public void onClick(ClickEvent event) {
 				   login();
 			  }
 		  });
 		    
 		  // Listen for keyboard events in the input box.
 		  password_textbox.addKeyPressHandler(new KeyPressHandler() {
 		      public void onKeyPress(KeyPressEvent event) {
 		    	  if (event.getCharCode() == KeyCodes.KEY_ENTER) {
 		    		  login();
 		    	  }
 		      }
 		  });
 	  }
 	  
 	  /**
 	   * Shows de username and the logout button in the toolbar panel
 	   * */
 	  public void logged_message(){
 		  
 		  Label username_label = new Label("Welcome " + current_user.user_name + "   ");
 		  username_label.addStyleName("loginText");
 		  
 		  // Assemble Login panel.
 		  
 		  login_button.addStyleName("button");
 		  login_button.setWidth("70px");
 		  login_button.setText("Logout");
 			
 		  loginPanel.add(username_label);
 		  loginPanel.add(login_button);
 		  loginPanel.addStyleName("loggedPanel");
 		  toolbarPanel.add(loginPanel);
 		  
 		  // Listen for mouse events on the Login button.
 		  login_button.addClickHandler(new ClickHandler() {
 			  public void onClick(ClickEvent event) {
				  logout();
 			  }
 		  });
 	  }
 	  
 	  /**
 	   * Creates the back button and its functionality
 	   * */
 	  public void back_button(){
 		  
 		  Button backButton = new Button("<");
 		  backButton.addStyleDependentName("remove");
 		  backButton.addClickHandler(new ClickHandler() {
 		    public void onClick(ClickEvent event) {
 		      //int removedIndex = 1;		// TODO: CAMBIAr ESto PARA QUE SIRVA PARA ALGO
 		      //awards.remove(removedIndex);        
 		      //forumFlexTable.removeRow(removedIndex + 1);
 		    	
 		    	  if(currentElementType == 'P'){
 		    		  // Do nothing. Topics is the upper node of the hierarchy
 		    	  }else if(currentElementType == 'T'){
 		    		  showTopics();	// Show but NOT update. Be careful with that.
 		    	  }else if(currentElementType == 'M'){
 		    		  
 		    		  // Look for the id of the parent topic
 		    		  int index = 0;
 		    		  for(index=0; index < threads.size(); index++){
 		    			  if(threads.get(index).id == currentElementId){
 		    				  currentElementId = threads.get(index).parent_topic_id;
 		    				  index = messages.size();
 		    			  }
 		    		  }
 		    		  
 		    		  // Show the threads of the parent topic
 		    		  showThreads();
 		    	  }else{
 		    		  System.out.println("Not able to check current type");
 		    	  }
 		    }
 		  });
 		  toolbarPanel.add(backButton);
 	  }
 	  
 	  /**
 	   * Creates the back button and its functionality
 	   * */
 	  public void refresh_button(){
 		  
 		  Button refreshButton = new Button("Refresh");
 		  refreshButton.addStyleDependentName("remove");
 		  refreshButton.addClickHandler(new ClickHandler() {
 		    public void onClick(ClickEvent event) {
 		    	refresh();
 		    }
 		  });
 		  
 		  toolbarPanel.add(refreshButton);
 	  }
 	  
 	  /**
 	   * Creates and shows the new_message_panel
 	   * */
 	  public void new_message_panel(){
 		  
 		  final RichTextArea textArea = new RichTextArea();
 		  final RichTextToolbar toolBar = new RichTextToolbar(textArea);
 	      textArea.setWidth("100%");  
 	      vp.add(toolBar);
 	      vp.add(textArea);
 	      vp.add(insert_text_button);
 	      vp.addStyleName("messagesPanel");
 		  
 	      //Text Editor Panel
 	  	  RootPanel.get().add(vp);
 
 		  // Listen for mouse events on the Login button.
 		  insert_text_button.addClickHandler(new ClickHandler() {
 			  public void onClick(ClickEvent event) { 
 				  //TODO: obtener el parent_topic (yo diria mas bien parent_thread, a ver si esto rula)
 				  Message insert = new Message(textArea.getText(), currentElementId, current_user.user_name);
 				  
 				  textArea.setText(""); //TODO: limpiar el texto despues de guardarlo
 			  }
 		  });
 	  }
 	  
 	  /**
 	   * Cleans the forumFlexTable exept for the headding
 	   * */
 	  public void clean_table(){
 		  for(int i=forumFlexTable.getRowCount()-1; i > 0 ; --i){
 			  forumFlexTable.removeRow(i);
 		  }
 	  }
 	  
 	  /**
 	   * Set an uncaught exception handler
 	   * This code is here to make it cleaner
 	   * */
 	  public void setUncaughtExceptionHandler(){
 		  
 		// Set uncaught exception handler
 	    GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
 	      public void onUncaughtException(Throwable throwable) {
 	        String text = "Uncaught exception: ";
 	        while (throwable != null) {
 	          StackTraceElement[] stackTraceElements = throwable.getStackTrace();
 	          text += throwable.toString() + "\n";
 	          for (int i = 0; i < stackTraceElements.length; i++) {
 	            text += "    at " + stackTraceElements[i] + "\n";
 	          }
 	          throwable = throwable.getCause();
 	          if (throwable != null) {
 	            text += "Caused by: ";
 	          }
 	        }
 	        DialogBox dialogBox = new DialogBox(true, false);
 	        DOM.setStyleAttribute(dialogBox.getElement(), "backgroundColor", "#ABCDEF");
 	        System.err.print(text);
 	        text = text.replaceAll(" ", "&nbsp;");
 	        dialogBox.setHTML("<pre>" + text + "</pre>");
 	        dialogBox.center();
 	      }
 	    });
 	    
 	  }
 		public void pruebas_mary(){
 			
 			
 			
 		};
 
 
 }
 
 
