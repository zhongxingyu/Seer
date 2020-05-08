 package org.vaadin.marcus.clearablesearchfield.client.ui;
 
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.event.dom.client.BlurEvent;
 import com.google.gwt.event.dom.client.BlurHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.FocusEvent;
 import com.google.gwt.event.dom.client.FocusHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.ui.Accessibility;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.vaadin.terminal.gwt.client.ApplicationConnection;
 import com.vaadin.terminal.gwt.client.Paintable;
 import com.vaadin.terminal.gwt.client.UIDL;
 import com.vaadin.terminal.gwt.client.ui.Field;
 import com.vaadin.terminal.gwt.client.ui.Icon;
 
 /**
  * Client side widget which communicates with the server. Messages from the
  * server are shown as HTML and mouse clicks are sent to the server.
  */
 public class VClearableSearchField extends FlowPanel implements Paintable,
         Field, FocusHandler, BlurHandler, ClickHandler, KeyPressHandler {
 
     /** Set the CSS class name to allow styling. */
     public static final String CLASSNAME = "v-clearable-searchfield";
     public static final String FOCUS_CLASSNAME = "focus";
     public static final String PROMPT_CLASSNAME = "prompt";
     public static final String BUTTON_CLASSNAME = "search-button";
     public static final String BOX_CLASSNAME = "search-field";
 
     public static final String SEARCH_IDENTIFIER = "searchterm";
     protected static final String CLEAR_BUTTON_STYLE = "clear";
     protected static final int REVERT_DELAY = 200;
 
     /** The client side widget identifier */
     protected String paintableId;
 
     /** Reference to the server connection object. */
     protected ApplicationConnection client;
 
     protected TextBox searchField;
     protected Button searchButton;
 
     protected String currentSearchTerm = "";
     protected String inputPrompt = "";
 
     protected ResetSearchTimer resetTimer;
 
     protected boolean buttonInClearMode;
 
     protected HandlerRegistration boxFocusRegistration;
     protected HandlerRegistration boxBlurRegistration;
     protected HandlerRegistration buttonFocusRegistration;
     protected HandlerRegistration buttonBlurRegistration;
 
     protected String searchButtonCaption = "";
     protected String clearButtonCaption = "";
 
     // For button
     protected final Element buttonWrapper = DOM.createSpan();
     protected final Element buttonCaption = DOM.createSpan();
     protected Icon searchIcon;
     protected Icon clearIcon;
     private boolean searchFieldFocused;
     private boolean promptVisible;
     private HandlerRegistration keyPressRegistration;
 
     public VClearableSearchField() {
         super();
         setStyleName(CLASSNAME);
 
         buildLayout();
     }
 
     private void buildLayout() {
         searchField = new TextBox();
         searchField.setStyleName(BOX_CLASSNAME);
         boxFocusRegistration = searchField.addFocusHandler(this);
         boxBlurRegistration = searchField.addBlurHandler(this);
         keyPressRegistration = searchField.addKeyPressHandler(this);
 
         searchButton = new Button();
         searchButton.setStyleName(BUTTON_CLASSNAME);
         buttonFocusRegistration = searchButton.addFocusHandler(this);
         buttonBlurRegistration = searchButton.addBlurHandler(this);
         searchButton.addClickHandler(this);
         Accessibility.setRole(searchButton.getElement(),
                 Accessibility.ROLE_BUTTON);
 
         buttonWrapper.setClassName(BUTTON_CLASSNAME + "-wrap");
         buttonCaption.setClassName(BUTTON_CLASSNAME + "-caption");
         buttonWrapper.appendChild(buttonCaption);
         searchButton.getElement().appendChild(buttonWrapper);
 
         add(searchField);
         add(searchButton);
     }
 
     /**
      * Called whenever an update is received from the server
      */
     public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
         // This call should be made first.
         // It handles sizes, captions, tooltips, etc. automatically.
         if (client.updateComponent(this, uidl, true)) {
             // If client.updateComponent returns true there has been no changes
             // and we
             // do not need to update anything.
             return;
         }
 
         // Save reference to server connection object to be able to send
         // user interaction later
         this.client = client;
 
         // Save the client side identifier (paintable id) for the widget
         paintableId = uidl.getId();
 
         if (uidl.hasAttribute("prompt")) {
             inputPrompt = uidl.getStringAttribute("prompt");
         }
 
         if (uidl.hasVariable(SEARCH_IDENTIFIER)) {
             currentSearchTerm = uidl.getStringVariable(SEARCH_IDENTIFIER);
         }
 
         setSearchFieldValue();
 
         searchButtonCaption = uidl.getStringAttribute("searchButtonCaption");
         clearButtonCaption = uidl.getStringAttribute("clearButtonCaption");
 
         updateButton();
 
     }
 
     // Search box logic
     private void setSearchFieldValue() {
         if (shouldShowPrompt()) {
             searchField.setValue(inputPrompt);
             searchField.addStyleDependentName(PROMPT_CLASSNAME);
             promptVisible = true;
         } else {
             searchField.setValue(currentSearchTerm);
             searchField.removeStyleDependentName(PROMPT_CLASSNAME);
             promptVisible = false;
         }
     }
 
     private boolean shouldShowPrompt() {
         return currentSearchTerm.isEmpty() && !searchFieldFocused;
     }
 
     private void hidePromptIfVisible() {
         if (promptVisible) {
             searchField.setText("");
             searchField.removeStyleDependentName(PROMPT_CLASSNAME);
             promptVisible = false;
         }
     }
 
     private void clearSearchBox() {
         searchField.setText("");
         runSearch();
         setSearchFieldValue();
     }
 
     private void runSearch() {
         currentSearchTerm = searchField.getText();
         searchField.setFocus(false);
         if (!currentSearchTerm.isEmpty()) {
             setClearButton(true);
         }
         client.updateVariable(paintableId, SEARCH_IDENTIFIER,
                 searchField.getText(), true);
     }
 
     private void revertSearch() {
         if (searchField.getText().isEmpty()) {
             runSearch();
         }
         if (!currentSearchTerm.isEmpty()) {
             setClearButton(true);
         }
         setSearchFieldValue();
     }
 
     // Button methods
     private void updateButton() {
         if (buttonInClearMode) {
             setButtonText(clearButtonCaption);
         } else {
             setButtonText(searchButtonCaption);
         }
     }
 
     protected void setButtonText(String text) {
         buttonCaption.setInnerText(text);
     }
 
     private void setClearButton(boolean clear) {
         if (clear) {
             buttonInClearMode = true;
             searchButton.addStyleName(CLEAR_BUTTON_STYLE);
         } else {
             buttonInClearMode = false;
             searchButton.removeStyleName(CLEAR_BUTTON_STYLE);
         }
 
         updateButton();
     }
 
     // Event handling
     public void onFocus(FocusEvent event) {
         if (event.getSource().equals(searchField)) {
             searchFieldFocused = true;
             searchField.addStyleDependentName(FOCUS_CLASSNAME);
 
             hidePromptIfVisible();
             setClearButton(false);
         } else if (event.getSource().equals(searchButton)) {
             cancelResetTimer();
         }
     }
 
     public void onBlur(BlurEvent event) {
         if (event.getSource().equals(searchField)) {
             startResetTimer();
             searchFieldFocused = false;
             searchField.removeStyleDependentName(FOCUS_CLASSNAME);
         } else if (event.getSource().equals(searchButton)) {
             startResetTimer();
         }
     }
 
     public void onClick(ClickEvent event) {
         cancelResetTimer();
 
         if (buttonInClearMode) {
             clearSearchBox();
         } else {
             runSearch();
         }
     }
 
     public void onKeyPress(KeyPressEvent event) {
        if (event.getCharCode() == KeyCodes.KEY_ENTER) {
             runSearch();
         }
     }
 
     private class ResetSearchTimer extends Timer {
 
         @Override
         public void run() {
             revertSearch();
         }
     }
 
     private void startResetTimer() {
         // We do not want two timers running at the same time at any point
         cancelResetTimer();
 
         resetTimer = new ResetSearchTimer();
         resetTimer.schedule(REVERT_DELAY);
     }
 
     private void cancelResetTimer() {
         if (resetTimer != null) {
             resetTimer.cancel();
             resetTimer = null;
         }
     }
 
     @Override
     protected void onDetach() {
         if (resetTimer != null) {
             resetTimer.cancel();
             resetTimer = null;
         }
 
         boxBlurRegistration.removeHandler();
         boxFocusRegistration.removeHandler();
         buttonBlurRegistration.removeHandler();
         buttonFocusRegistration.removeHandler();
         keyPressRegistration.removeHandler();
 
         super.onDetach();
     }
 
 }
