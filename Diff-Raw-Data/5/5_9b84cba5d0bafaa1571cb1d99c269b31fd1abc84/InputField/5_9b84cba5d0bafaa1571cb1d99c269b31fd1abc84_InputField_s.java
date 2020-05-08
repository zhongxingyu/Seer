 package com.witsacco.mockery.client;
 
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.event.shared.GwtEvent;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.event.shared.HasHandlers;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.LayoutPanel;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.Widget;
 
 public class InputField implements HasHandlers {
 
 	// GWT event handler manager
 	private HandlerManager handlerManager;
 
 	// UI elements managed by this class
 	private FlowPanel mainPanel;
 	private LayoutPanel layout;
 	private TextArea inputArea;
 
 	public InputField() {
 
 		// Initialize GWT event handler manager
 		handlerManager = new HandlerManager( this );
 
 		// Initialize UI elements for this class
 		initializeUI();
 	}
 
 	/**
 	 * Initializes the UI elements managed by this class
 	 */
 	private void initializeUI() {
 
 		// Set up the main panel to hold the InputField UI elements
 		mainPanel = new FlowPanel();
 		mainPanel.addStyleName( "input-panel" );
 
 		// Set up input area
 		inputArea = new TextArea();
 		inputArea.addStyleName( "input-textarea" );
 		inputArea.setCharacterWidth( 80 );
 		inputArea.setVisibleLines( 4 );
 		initializeTextAreaListener();
 
 		// Create layout for input area
 		layout = new LayoutPanel();
 		layout.addStyleName( "layout-panel" );
 
 		// Add the TextArea to the layout manager
 		layout.add( inputArea );
 
 		// Align the TextArea within the layout manager
 		layout.setWidgetLeftRight( inputArea, 1, Unit.PCT, 1, Unit.PCT );
 		layout.setWidgetTopBottom( inputArea, 4, Unit.PCT, 4, Unit.PCT );
 
 		// Add the layout manager to the main panel for this class
 		mainPanel.add( layout );
 	}
 
 	/**
 	 * Sets up the listener on the text area
 	 */
 	private void initializeTextAreaListener() {
 		inputArea.addKeyPressHandler( new KeyPressHandler() {
 
 			@Override
 			public void onKeyPress( KeyPressEvent event ) {

 				// Leave if we didn't see the enter key
				if ( event.getCharCode() != KeyCodes.KEY_ENTER ) {
 					return;
 				}
 
 				// Fire off a new message event
 				MessagePostedEvent newPostEvent = new MessagePostedEvent( getText() );
 		        fireEvent( newPostEvent );
 
 		        // Clear the text area
 				inputArea.setText( "" );
 
 				// Prevent enter character from being typed
 				event.preventDefault();
 			}
 		} );
 	}
 
 	/**
 	 * Grabs the main panel (initialized in constructor)
 	 * 
 	 * @return Main panel for this class
 	 */
 	public Widget getPanel() {
 		return mainPanel;
 	}
 
 	/**
 	 * Grabs the contents of the text area
 	 * 
 	 * @return Contents of text area
 	 */
 	public String getText() {
 		return inputArea.getText();
 	}
 
 	private boolean isValid( String text ) {
 		// TODO implement input validation logic (FieldVerifier?)
 		return true;
 	}
 
 	@Override
 	public void fireEvent( GwtEvent< ? > event ) {
 		handlerManager.fireEvent( event );
 	}
 
 	public HandlerRegistration addMessageReceivedEventHandler(
 			MessagePostedEventHandler handler ) {
 		return handlerManager.addHandler( MessagePostedEvent.TYPE, handler );
 	}
 }
