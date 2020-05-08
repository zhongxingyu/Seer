 package com.zipsoft.suggestfield.client;
 
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 import com.google.gwt.aria.client.Roles;
 import com.google.gwt.core.client.Scheduler.ScheduledCommand;
 import com.google.gwt.editor.client.IsEditor;
 import com.google.gwt.editor.client.LeafValueEditor;
 import com.google.gwt.editor.client.adapters.TakesValueEditor;
 import com.google.gwt.event.dom.client.BlurEvent;
 import com.google.gwt.event.dom.client.BlurHandler;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.FocusEvent;
 import com.google.gwt.event.dom.client.FocusHandler;
 import com.google.gwt.event.dom.client.HasAllKeyHandlers;
 import com.google.gwt.event.dom.client.HasChangeHandlers;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyDownEvent;
 import com.google.gwt.event.dom.client.KeyDownHandler;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.event.logical.shared.HasSelectionHandlers;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasEnabled;
 import com.google.gwt.user.client.ui.HasValue;
 import com.google.gwt.user.client.ui.TextBox;
 import com.vaadin.client.Focusable;
 import com.vaadin.client.VTooltip;
 import com.vaadin.client.ui.Field;
 import com.vaadin.client.ui.SubPartAware;
 import com.vaadin.client.ui.VOverlay;
 import com.vaadin.client.ui.aria.AriaHelper;
 import com.vaadin.client.ui.aria.HandlesAriaCaption;
 import com.vaadin.client.ui.aria.HandlesAriaInvalid;
 import com.vaadin.client.ui.aria.HandlesAriaRequired;
 import com.zipsoft.suggestfield.shared.SuggestFieldSuggestionImpl;
 
 /**
  * GWT Widget za Vaadin komponentu
  * 
  * @author marko
  * 
  *         public class VFilterSelect extends Composite implements Field,
  *         KeyDownHandler, KeyUpHandler, ClickHandler, FocusHandler,
  *         BlurHandler, Focusable, SubPartAware, HandlesAriaCaption,
  *         HandlesAriaInvalid, HandlesAriaRequired {
  */
 public class VSuggestFieldWidget extends Composite implements Field,
 		SubPartAware, HasSelectionHandlers<SuggestFieldSuggestionImpl>, HasEnabled,
 		HasAllKeyHandlers, HasValue<SuggestFieldSuggestionImpl>,
 		IsEditor<LeafValueEditor<SuggestFieldSuggestionImpl>>, HandlesAriaRequired,
 		HandlesAriaCaption, HandlesAriaInvalid, HasChangeHandlers,
 		FocusHandler, BlurHandler, Focusable, KeyDownHandler, KeyUpHandler,
 		KeyPressHandler {
		
 	private static final String CLASSNAME = "z-suggestfield";
 	private static final String CLASSNAME_INPUT = "-input";
 	private static final String CLASSNAME_ICON = "-icon";
 	private static final String CLASSNAME_PROMPT = "prompt";
 	private static final String CLASSNAME_DISABLED = "disabled";
 
 	public static interface SuggestionReadyCallback {
 		public void onSuggestionsReady();
 	}
 
 	public static interface SuggestionSelectedCallback {
 		void onSuggestionSelected(SuggestFieldSuggestionImpl suggestion);
 	}
 
 	public static interface SuggestionsLoaderClient {
 		void doLoadSuggestions(String query, int limit);
 	}
 
 	private int limit = 20;
 	private boolean selectsFirstItem = true;
 	private LeafValueEditor<SuggestFieldSuggestionImpl> editor;
 
 	private final FlowPanel panel;
 	private final TextBox textBox;
 	private final HTML searchIcon;
 
 	private String currentText;
 	private SuggestFieldSuggestionImpl currentSuggestion;
 
 	private boolean valueChangeHandlerInitialized;
 	private final SuggestPopup suggestPopup;
 	private int SUGGESTION_SEARCH_DELAY = 500;
 	private boolean scheduledSearch = false;
 	private boolean hideWhenEmpty = true;
 
 	boolean isDisplayStringHTML = false;
 	public String inputPrompt = "";
 	public boolean prompting = false;
 	public boolean focused = false;
 	public boolean enabled;
 	public boolean readonly;
 
 	private ArrayList<SuggestFieldSuggestionImpl> suggestions = new ArrayList<SuggestFieldSuggestionImpl>();
 
 	private VSuggestFieldWidget lastSuggestBox = null;
 
 	private SuggestionsLoaderClient suggestionsLoaderClient;
 
 	/**
 	 * Callback metod za prikazivanje rezultata
 	 */
 	private final SuggestionSelectedCallback suggestionSelectedCallback = new SuggestionSelectedCallback() {
 		public void onSuggestionSelected(SuggestFieldSuggestionImpl suggestion) {
 			// box.setFocus(true);
 			focus();
 			setNewSelection(suggestion);
 		}
 	};
 
 	/**
 	 * Callback koji okida prikazivanje rezultata kada su rezultati preuzeti sa
 	 * serverse strane
 	 */
 	private final SuggestionReadyCallback suggestionReadyCallback = new SuggestionReadyCallback() {
 
 		@Override
 		public void onSuggestionsReady() {
 			showSuggestions(suggestions, isAutoSelectEnabled(),
 					suggestionSelectedCallback);
 		}
 	};
 
 	/**
 	 * Ovaj metod prikazuje sugestije koje su ucitane sa servera
 	 * 
 	 * @param query
 	 */
 	public void showSuggestionsFromServer() {
 		suggestionReadyCallback.onSuggestionsReady();
 	}
 
 	/**
 	 * Delay da bi se smanjio broj okidanja
 	 */
 	private Timer searchSuggestionTrigger = new Timer() {
 
 		@Override
 		public void run() {
 			if (isAttached()) {
 				refreshSuggestions();
 				scheduledSearch = false;
 			}
 		}
 	};
 
 	/**
 	 * Podrazumevani konstruktor
 	 */
 	public VSuggestFieldWidget() {
 		super();
 
 		this.panel = new FlowPanel();
 		this.textBox = new TextBox();
 		this.suggestPopup = new SuggestPopup();
 		this.searchIcon = new HTML("");
 
 		this.panel.add(textBox);
 		this.panel.add(searchIcon);
 
 		initWidget(this.panel);
 
 		Element el = DOM.getFirstChild(getElement());
 		while (el != null) {
 			DOM.sinkEvents(el,
 					(DOM.getEventsSunk(el) | VTooltip.TOOLTIP_EVENTS));
 			el = DOM.getNextSibling(el);
 		}
 
 		addEventsToTextBox();
 
 		setStylePrimaryName(CLASSNAME);
 		addStyleDependentName(CLASSNAME_PROMPT);
 
 		Roles.getComboboxRole().set(panel.getElement());
 	}
 
 	@Override
 	public void setStyleName(String style) {
 		super.setStyleName(style);
 		updateStyleNames();
 	}
 
 	@Override
 	public void setStylePrimaryName(String style) {
 		super.setStylePrimaryName(style);
 		updateStyleNames();
 	}
 
 	protected void updateStyleNames() {
 		this.textBox.setStyleName(getStylePrimaryName() + CLASSNAME_INPUT);
 		this.searchIcon.setStyleName(getStylePrimaryName() + CLASSNAME_ICON);
 	}
 
 	@Override
 	public void setWidth(String width) {
 		super.setWidth(width);
 		if (width.length() != 0) {
 			textBox.setWidth("100%");
 		}
 	}
 
 	public void setPromptingOn() {
 		if (!prompting) {
 			prompting = true;
 			addStyleDependentName(CLASSNAME_PROMPT);
 		}
 		textBox.setText(inputPrompt);
 		Logger.getLogger(VSuggestFieldWidget.class.getName()).info("PromptOn");
 	}
 
 	public void setPromptingOff(String text) {
 		textBox.setText(text);
 		if (prompting) {
 			prompting = false;
 			removeStyleDependentName(CLASSNAME_PROMPT);
 		}
 		Logger.getLogger(VSuggestFieldWidget.class.getName()).info("PromptOff");
 	}
 
 	/**
 	 * Resets the Select to its initial state
 	 */
 	private void reset() {
 		if (currentSuggestion != null) {
 			setPromptingOff(currentSuggestion.getReplacementString());
 		} else {
 			if (focused) {
 				setPromptingOff("");
 			} else {
 				setPromptingOn();
 			}
 		}
 		hideSuggestions();
 		Logger.getLogger(VSuggestFieldWidget.class.getName()).info("Reset");
 	}
 
 	@Override
 	public void setEnabled(boolean enabled) {
 		this.enabled = enabled;
 		this.readonly = !enabled;
 		textBox.setEnabled(enabled);
 		if (!enabled) {
 			hideSuggestions();
 			searchSuggestionTrigger.cancel();
 			scheduledSearch = false;
 			addStyleDependentName(CLASSNAME_DISABLED);
 		} else {
 			removeStyleDependentName(CLASSNAME_DISABLED);
 		}
 		reset();
 	}
 
 	@Override
 	public boolean isEnabled() {
 		return this.enabled;
 	}
 
 	public void focus() {
 		focused = true;
 		if (prompting && !readonly) {
 			setPromptingOff("");
 		}
 		textBox.setFocus(true);
 	}
 
 	@Override
 	public void onBlur(BlurEvent event) {
 
 		focused = false;
 		if (!readonly) {
 			if (currentSuggestion == null) {
 				setPromptingOn();
 			} else {
 				setPromptingOff(currentSuggestion.getReplacementString());
 			}
 		}
 		removeStyleDependentName("focus");
 
 		DOM.eventPreventDefault(DOM.eventGetCurrentEvent());
 		event.stopPropagation();
 
 		Logger.getLogger(VSuggestFieldWidget.class.getName()).info("OnBlur");
 
 	}
 
 	@Override
 	public void onFocus(FocusEvent event) {
 
 		focused = true;
 		if (prompting && !readonly) {
 			setPromptingOff("");
 		}
 		addStyleDependentName("focus");
 
 		DOM.eventPreventDefault(DOM.eventGetCurrentEvent());
 		event.stopPropagation();
 
 		Logger.getLogger(VSuggestFieldWidget.class.getName()).info("OnFocus");
 	}
 
 	@Override
 	protected void onDetach() {
 		super.onDetach();
 		suggestPopup.hide();
 	}
 
 	
 	/**
 	 * Show the current list of suggestions.
 	 */
 	public void showSuggestionList() {
 		if (isAttached()) {
 			currentText = null;
 			refreshSuggestions();
 		}
 	}
 
 	/**
 	 * Refreshes the current list of suggestions.
 	 */
 	public void refreshSuggestionList() {
 		if (isAttached()) {
 			refreshSuggestions();
 		}
 	}
 
 	private void refreshSuggestions() {
 		// Get the raw text.
 		String text = textBox.getText();
 		if (text.equals(currentText)) {
 			return;
 		} else {
 			currentText = text;
 		}
 		findSuggestions(text);
 	}
 
 	/**
 	 * Ovaj metod poziva pretragu na serverskoj strani
 	 * 
 	 * @param query
 	 */
 	void findSuggestions(String query) {
 		if (suggestionsLoaderClient != null) {
 			if (query.length() == 0) {
 				suggestionsLoaderClient.doLoadSuggestions("default-query",
 						limit);
 			} else {
 				suggestionsLoaderClient.doLoadSuggestions(query, limit);
 			}
 		}
 	}	
 	
 	protected SuggestFieldSuggestionImpl getCurrentSelection() {
 		if (!suggestPopup.isShowing()) {
 			return null;
 		}
 		VSuggestionMenuItem item = suggestPopup.menu.getSelectedItem();
 		return item == null ? null : item.getSuggestion();
 	}
 
 	protected void hideSuggestions() {
 		suggestPopup.hide();
 	}
 
 	protected void moveSelectionDown() {
 		if (suggestPopup.isShowing()) {
 			suggestPopup.menu.moveSelectionDown();
 		}
 	}
 
 	protected void moveSelectionUp() {
 		if (suggestPopup.isShowing()) {
 			suggestPopup.menu.moveSelectionUp();
 		}
 	}
 
 	protected void showSuggestions(
 			ArrayList<SuggestFieldSuggestionImpl> suggestions,
 			boolean isAutoSelectEnabled,
 			final SuggestionSelectedCallback callback) {
 
 		// Hide the popup if there are no suggestions to display.
 		boolean anySuggestions = (suggestions != null && suggestions.size() > 0);
 		if (!anySuggestions && hideWhenEmpty) {
 			hideSuggestions();
 			return;
 		}
 
 		// Hide the popup before we manipulate the menu within it. If we do not
 		// do this, some browsers will redraw the popup as items are removed
 		// and added to the menu.
 		if (suggestPopup.isAttached()) {
 			suggestPopup.hide();
 		}
 
 		suggestPopup.menu.clearItems();
 
 		for (final SuggestFieldSuggestionImpl suggestion : suggestions) {
 
 			final VSuggestionMenuItem menuItem = new VSuggestionMenuItem(
 					(SuggestFieldSuggestionImpl) suggestion, isDisplayStringHTML);
 			menuItem.setCommand(new ScheduledCommand() {
 				public void execute() {
 					callback.onSuggestionSelected(suggestion);
 				}
 			});
 			suggestPopup.menu.addItem(menuItem);
 		}
 
 		if (isAutoSelectEnabled && anySuggestions) {
 			// Select the first item in the suggestion menu.
 			suggestPopup.menu.selectItemAtIndex(0);
 		}
 
 		// Link the popup autoHide to the TextBox.
 		if (lastSuggestBox != VSuggestFieldWidget.this) {
 			// If the suggest box has changed, free the old one first.
 			if (lastSuggestBox != null) {
 				suggestPopup.removeAutoHidePartner(lastSuggestBox.getElement());
 			}
 			lastSuggestBox = VSuggestFieldWidget.this;
 			suggestPopup.addAutoHidePartner(VSuggestFieldWidget.this
 					.getElement());
 		}
 
 		// Show the popup under the TextBox.
 		suggestPopup.showRelativeTo(VSuggestFieldWidget.this);
 
 	}
 
 	/**
 	 * Set the new suggestion in the text box.
 	 * 
 	 * @param curSuggestion
 	 *            the new suggestion
 	 */
 	private void setNewSelection(SuggestFieldSuggestionImpl curSuggestion) {
 		assert curSuggestion != null : "suggestion cannot be null";
 		textBox.setText(curSuggestion.getReplacementString());
 		hideSuggestions();
 		searchSuggestionTrigger.cancel();
 		scheduledSearch = false;		
 //		setValue(curSuggestion, true);
 		fireSuggestionEvent(curSuggestion);
 //		this.currentSuggestion = curSuggestion;
 
 	}	
 	
 	@Override
 	public void onKeyPress(KeyPressEvent event) {
 		delegateEvent(VSuggestFieldWidget.this, event);		
 	}
 
 	@Override
 	public void onKeyUp(KeyUpEvent event) {
 		switch (event.getNativeKeyCode()) {
 		case KeyCodes.KEY_DOWN:
 		case KeyCodes.KEY_UP:
 		case KeyCodes.KEY_ENTER:
 //		case KeyCodes.KEY_TAB:
 		case KeyCodes.KEY_ESCAPE:
 		case KeyCodes.KEY_PAGEUP:
 		case KeyCodes.KEY_PAGEDOWN:
 		case KeyCodes.KEY_HOME:
 		case KeyCodes.KEY_END:
 			DOM.eventPreventDefault(DOM.eventGetCurrentEvent());
 			event.stopPropagation();
 			break;
 		default:
 			//AKO JE BILO KOJI DRUGI TASTER - POZIVAM POPUP
 			if (!scheduledSearch) {
 				searchSuggestionTrigger.schedule(SUGGESTION_SEARCH_DELAY);
 				scheduledSearch = true;
 				Logger.getLogger(VSuggestFieldWidget.class.getName()).info("OnKeyUp");
 			}
 			// refreshSuggestions();
 			delegateEvent(VSuggestFieldWidget.this, event);
 			break;
 		}
 		
 	}
 
 	@Override
 	public void onKeyDown(KeyDownEvent event) {
 		switch (event.getNativeKeyCode()) {
 		case KeyCodes.KEY_DOWN:
 			moveSelectionDown();
 			DOM.eventPreventDefault(DOM.eventGetCurrentEvent());
 			event.stopPropagation();
 			break;
 		case KeyCodes.KEY_UP:
 			moveSelectionUp();
 			DOM.eventPreventDefault(DOM.eventGetCurrentEvent());
 			event.stopPropagation();
 			break;
 		case KeyCodes.KEY_ENTER:
 //		case KeyCodes.KEY_TAB:
 			SuggestFieldSuggestionImpl suggestion = getCurrentSelection();
 			if (suggestion == null) {
 				hideSuggestions();
 				searchSuggestionTrigger.cancel();
 				scheduledSearch = false;
 				reset();
 			} else {
 				setNewSelection(suggestion);
 			}
 			DOM.eventPreventDefault(DOM.eventGetCurrentEvent());
 			event.stopPropagation();
 			break;
 		case KeyCodes.KEY_ESCAPE:
 			reset();
 			DOM.eventPreventDefault(DOM.eventGetCurrentEvent());
 			event.stopPropagation();
 			break;
 		}
 		delegateEvent(VSuggestFieldWidget.this, event);
 		
 	}
 	
 	@SuppressWarnings("unused")
 	private void eatEvent(Event event) {
 		DOM.eventCancelBubble(event, true);
 		DOM.eventPreventDefault(event);
 	}
 		
 	/**
 	 * Povezivanje handlera na textbox
 	 */
 	private void addEventsToTextBox() {
 		textBox.addFocusHandler(this);
 		textBox.addBlurHandler(this);
 		textBox.addKeyDownHandler(this);
 		textBox.addKeyUpHandler(this);
 		textBox.addKeyPressHandler(this);		
 	}
 
 	/**
 	 * Ovo je isto kao i da je okinuo ValueChange 
 	 * @param selectedSuggestion
 	 */
 	private void fireSuggestionEvent(SuggestFieldSuggestionImpl selectedSuggestion) {
 		SelectionEvent.fire(this, selectedSuggestion);
 	}
 
 	@Override
 	public LeafValueEditor<SuggestFieldSuggestionImpl> asEditor() {
 		if (editor == null) {
 			editor = TakesValueEditor.of(this);
 		}
 		return editor;
 	}
 
 	@Override
 	public SuggestFieldSuggestionImpl getValue() {
 		return this.currentSuggestion;
 	}
 
 	@Override
 	public void setValue(SuggestFieldSuggestionImpl value) {
 		setValue(value, false);
 	}
 
 	@Override
 	public void setValue(SuggestFieldSuggestionImpl value, boolean fireEvents) {
 		SuggestFieldSuggestionImpl oldValue = getValue();
 		this.currentSuggestion = value;
 		if (value == null) {
 			this.textBox.setText("");
 			Logger.getLogger(VSuggestFieldWidget.class.getName()).info("NewText: []");
 		} else {
 			this.textBox.setText(value.getReplacementString());
 			Logger.getLogger(VSuggestFieldWidget.class.getName()).info("NewText: [" + value.getReplacementString() + "]");
 		}
 		if (fireEvents) {
 			ValueChangeEvent.fireIfNotEqual(this, oldValue, value);
 		}
 		reset();
 	}
 
 	public TextBox getTextField() {
 		return textBox;
 	}
 
 	public int getLimit() {
 		return limit;
 	}
 
 	public void setLimit(int limit) {
 		this.limit = limit;
 	}
 
 	/**
 	 * Returns whether or not the first suggestion will be automatically
 	 * selected. This behavior is on by default.
 	 * 
 	 * @return true if the first suggestion will be automatically selected
 	 */
 	public boolean isAutoSelectEnabled() {
 		return selectsFirstItem;
 	}
 
 	public ArrayList<SuggestFieldSuggestionImpl> getSuggestions() {
 		return suggestions;
 	}
 
 	public void setSuggestions(ArrayList<SuggestFieldSuggestionImpl> suggestions) {
 		this.suggestions = suggestions;
 	}
 
 	/**
 	 * Turns on or off the behavior that automatically selects the first
 	 * suggested item. This behavior is on by default.
 	 * 
 	 * @param selectsFirstItem
 	 *            Whether or not to automatically select the first suggestion
 	 */
 	public void setAutoSelectEnabled(boolean selectsFirstItem) {
 		this.selectsFirstItem = selectsFirstItem;
 	}
 
 	public SuggestionsLoaderClient getSuggestionsLoaderClient() {
 		return suggestionsLoaderClient;
 	}
 
 	public void setSuggestionLoaderClient(
 			SuggestionsLoaderClient loadSuggestionsFromServer) {
 		this.suggestionsLoaderClient = loadSuggestionsFromServer;
 	}
 
 	public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
 		return addDomHandler(handler, KeyDownEvent.getType());
 	}
 
 	public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
 		return addDomHandler(handler, KeyPressEvent.getType());
 	}
 
 	public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
 		return addDomHandler(handler, KeyUpEvent.getType());
 	}
 
 	public HandlerRegistration addSelectionHandler(
 			SelectionHandler<SuggestFieldSuggestionImpl> handler) {
 		return addHandler(handler, SelectionEvent.getType());
 	}
 
 	public HandlerRegistration addValueChangeHandler(
 			ValueChangeHandler<SuggestFieldSuggestionImpl> handler) {
 
 //		if (!valueChangeHandlerInitialized) {
 //			valueChangeHandlerInitialized = true;
 //			addChangeHandler(new ChangeHandler() {
 //				public void onChange(ChangeEvent event) {
 //					ValueChangeEvent.fire(VSuggestFieldWidget.this, event.get);
 //				}
 //			});
 //		}
 		if (!valueChangeHandlerInitialized) {
 			valueChangeHandlerInitialized = true;
 			addSelectionHandler(new SelectionHandler<SuggestFieldSuggestionImpl>() {
 				
 				@Override
 				public void onSelection(SelectionEvent<SuggestFieldSuggestionImpl> event) {
 					ValueChangeEvent.fire(VSuggestFieldWidget.this, event.getSelectedItem());
 					
 				}
 			});
 //			addChangeHandler(new ChangeHandler() {
 //				public void onChange(ChangeEvent event) {
 //					ValueChangeEvent.fire(VSuggestFieldWidget.this, event.get);
 //				}
 //			});
 		}
 		return addHandler(handler, ValueChangeEvent.getType());
 	}
 
 	@Override
 	protected void onEnsureDebugId(String baseID) {
 		super.onEnsureDebugId(baseID);
 	}
 
 	@Override
 	public void setAriaRequired(boolean required) {
 		AriaHelper.handleInputRequired(textBox, required);
 
 	}
 
 	@Override
 	public void setAriaInvalid(boolean invalid) {
 		AriaHelper.handleInputInvalid(textBox, invalid);
 
 	}
 
 	@Override
 	public void bindAriaCaption(Element captionElement) {
 		AriaHelper.bindCaption(textBox, captionElement);
 
 	}
 	
 	public boolean isDisplayStringHTML() {
 		return isDisplayStringHTML;
 	}
 
 	public void setDisplayStringHTML(boolean isDisplayStringHTML) {
 		this.isDisplayStringHTML = isDisplayStringHTML;
 	}
 
 	/**
 	 * Popup u kome se pojavljuju rezultati
 	 */
 	public class SuggestPopup extends VOverlay {
 
 		private static final int Z_INDEX = 30000;
 
 		public final VSuggestionMenu menu;
 
 		public SuggestPopup() {
 			super(true, false, true);
 			this.menu = new VSuggestionMenu();
 			setWidget(menu);
 			setStylePrimaryName("z-suggestfield-popup");
 			getElement().getStyle().setZIndex(Z_INDEX);
 			setOwner(VSuggestFieldWidget.this);
 		}
 	}
 
 	@Override
 	public HandlerRegistration addChangeHandler(ChangeHandler handler) {
 		return addDomHandler(handler, ChangeEvent.getType());
 	}
 
 	@Override
 	public Element getSubPartElement(String subPart) {
 		if ("textbox".equals(subPart)) {
 			return textBox.getElement();
 		} else if ("searchicon".equals(subPart)) {
 			return searchIcon.getElement();
 		}
 		return null;
 	}
 
 	@Override
 	public String getSubPartName(Element subElement) {
 		if (textBox.getElement().isOrHasChild(subElement)) {
 			return "textbox";
 		} else if (searchIcon.getElement().isOrHasChild(subElement)) {
 			return "searchicon";
 		}
 		return null;
 	}
 
 	
 
 }
