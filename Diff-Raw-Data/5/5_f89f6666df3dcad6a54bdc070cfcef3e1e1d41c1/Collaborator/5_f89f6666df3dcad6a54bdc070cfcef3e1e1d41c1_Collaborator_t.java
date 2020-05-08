 package edu.caltech.cs141b.hw2.gwt.collab.client;
 
 import java.util.ArrayList;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DecoratorPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasAlignment;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.TabPanel;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 import edu.caltech.cs141b.hw2.gwt.collab.shared.AbstractDocument;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.UnlockedDocument;
 
 /**
  * Main class for a single Collaborator widget.
  */
 public class Collaborator extends Composite implements ClickHandler {
 
 	final private int maxTabTextLen = 15;
 	final private int maxConsoleEnt = 4;
 	final private int maxTabsOnOneSide = 4;
 	final private int maxTitleLength = 100;
 	final private int maxContentsLength = 10000;
 	final private String disabledCSS = "Disabled";
 
 	protected CollaboratorServiceAsync collabService;
 
 	// Track document information.
 	protected ArrayList<AbstractDocument> documentsLeftList = new ArrayList<AbstractDocument>();
 	protected ArrayList<AbstractDocument> documentsRightList = new ArrayList<AbstractDocument>();
 
 	// Managing available documents.
 	protected ListBox documentList = new ListBox();
 	private Button refreshList = new Button("Refresh List");
 	private Button createNew = new Button("New Document");
 
 	// For displaying document information and editing document content.
 	protected ArrayList<TextBox> titleL = new ArrayList<TextBox>();
 	protected ArrayList<TextArea> contentsL = new ArrayList<TextArea>();
 	protected ArrayList<TextBox> titleR = new ArrayList<TextBox>();
 	protected ArrayList<TextArea> contentsR = new ArrayList<TextArea>();
 	protected Button refreshDoc = new Button("Refresh Document");
 	protected Button lockButtonL = new Button("Get Document Lock");
 	protected Button saveButtonL = new Button("Save Document");
 	protected Button lockButtonR = new Button("Get Document Lock");
 	protected Button saveButtonR = new Button("Save Document");
 	protected TabPanel documentsL = new TabPanel();
 	protected TabPanel documentsR = new TabPanel();
 	protected Button showButtonL = new Button("Show Left");
 	protected Button showButtonR = new Button("Show Right");
 	protected Button removeTabL = new Button("Remove Tab");
 	protected Button removeTabR = new Button("Remove Tab");
 	protected Button refreshButtonL = new Button("Refresh Doc");
 	protected Button refreshButtonR = new Button("Refresh Doc");
 	
 	
 	
 	
 
 	// Panels
 	VerticalPanel leftPanel = new VerticalPanel();
 	VerticalPanel rightPanel = new VerticalPanel();
 	HorizontalPanel leftHPanel = new HorizontalPanel();
 	HorizontalPanel rightHPanel = new HorizontalPanel();
 
 	// Callback objects.
 	protected DocReader reader = new DocReader(this);
 	protected DocLister lister = new DocLister(this);
 	protected DocReleaser releaser = new DocReleaser(this);
 	protected String waitingKey = null;
 
 	// Generic objects used on key handlers.
 	protected TabPanel tabPanel = null;
 	protected ArrayList<AbstractDocument> docList = null;
 	protected ArrayList<TextArea> contentsList = null;
 	protected ArrayList<TextBox> titleList = null;
 	protected Button lockButton = null;
 	protected Button removeTabButton = null;
 	protected Button saveDocButton = null;
 	protected Button refresh = null;
 	protected HorizontalPanel hPanel = null;
 	protected String side = null;
 	
 	
 	// Status tracking.
 	private VerticalPanel statusArea = new VerticalPanel();
 
 	// The main outer panel where everything lives.
 	private HorizontalPanel mainOuterPanel;
 
 	/**
 	 * UI initialization.
 	 * 
 	 * @param collabService
 	 */
 	public Collaborator(CollaboratorServiceAsync collabService) {
 		this.collabService = collabService;
 
 		// initialize the UI
 		initUI();
 
 		addClickHandlersToButtons();
 
 		// add selection handler to both tab panels 
 		addSelectionHandlerToTabPanel(true);
 		addSelectionHandlerToTabPanel(false);
 
 		// the list of documents
 		documentList.addClickHandler(this);
 		documentList.setVisibleItemCount(20);
 		documentList.setHeight("100%");
 
 		// the 'get lock' button should be initially disabled 
 		// since there are no tabs open
 		disableButton(lockButtonL);
 		disableButton(lockButtonR);
 
 		// cant refresh doc since no docs open yet
 		disableButton(refreshButtonL);
 		disableButton(refreshButtonR);
 
 		// nothing selected on list yet, so disable these
 		disableButton(showButtonL);
 		disableButton(showButtonR);
 
 
 		initWidget(mainOuterPanel);
 		lister.getDocumentList();
 	}
 
 	/**
 	 * Initialize the UI.
 	 */
 	private void initUI()
 	{
 		// the main outer panel - holds everything
 		mainOuterPanel = new HorizontalPanel();
 		mainOuterPanel.setStyleName("mainOuterPanel");
 		mainOuterPanel.setWidth("100%");
 		mainOuterPanel.setHeight("100%");
 
 		// left side - the doc list and the console
 		VerticalPanel docsAndConsoleVertPanel = new VerticalPanel();
 		docsAndConsoleVertPanel.setStyleName("docsAndConsoleVertPanel");
 		docsAndConsoleVertPanel.setSpacing(20);
 
 		// list of docs
 		VerticalPanel docListPanel = new VerticalPanel();
 		docListPanel.setStyleName("docListPanel");
 		docListPanel.setHeight("100%");
 		docListPanel.setSpacing(10);
 		
 		HTML docListPanelTitle =  new HTML("<h2>Available Documents</h2>");
 		docListPanel.add(docListPanelTitle);
 		documentList.setWidth("100%");
 		docListPanel.add(documentList);
 
 	
 		docListPanel.setWidth("320px");
 
 		// button styles
 		
 		refreshDoc.setStylePrimaryName("refreshButton");
 		lockButtonL.setStylePrimaryName("lockButton");
 		saveButtonL.setStylePrimaryName("saveButton");
 		lockButtonR.setStylePrimaryName("lockButton");
 		saveButtonR.setStylePrimaryName("saveButton");
 		showButtonL.setStylePrimaryName("showLButton");
 		showButtonR.setStylePrimaryName("showRButton");
 		removeTabL.setStylePrimaryName("removeButton");
 		removeTabR.setStylePrimaryName("removeButton");
 		refreshButtonL.setStylePrimaryName("refreshButton");
 		refreshButtonR.setStylePrimaryName("refreshButton");
 		createNew.setStylePrimaryName("createNewButton");
 		refreshList.setStylePrimaryName("refreshButton");
 		
 		
 		refreshDoc.addStyleName("gwt-Button");
 		lockButtonL.addStyleName("gwt-Button");
 		saveButtonL.addStyleName("gwt-Button");
 		lockButtonR.addStyleName("gwt-Button");
 		saveButtonR.addStyleName("gwt-Button");
 		showButtonL.addStyleName("gwt-Button");
 		showButtonR.addStyleName("gwt-Button");
 		removeTabL.addStyleName("gwt-Button");
 		removeTabR.addStyleName("gwt-Button");
 		refreshButtonL.addStyleName("gwt-Button");
 		refreshButtonR.addStyleName("gwt-Button");
 		createNew.addStyleName("gwt-Button");
 		refreshList.addStyleName("gwt-Button");
 
 		// buttons inder the doc list
 		HorizontalPanel docListButtonPanel = new HorizontalPanel();
 		docListButtonPanel.setSpacing(10);
 		docListButtonPanel.add(refreshList);
 		docListButtonPanel.add(createNew);
 		docListButtonPanel.add(showButtonL);
 		docListButtonPanel.add(showButtonR);
 		docListPanel.add(docListButtonPanel);
 		DecoratorPanel dp = new DecoratorPanel();
 		dp.setWidth("100%");
 		dp.add(docListPanel);
 		docsAndConsoleVertPanel.add(dp);
 
 		VerticalPanel consoleDP = new VerticalPanel();
 		consoleDP.setStyleName("consoleDP");
 		consoleDP.setWidth("320px");
 		consoleDP.setHeight("250px");
 		HTML consoleTitle = new HTML("<h2>Console</h2>");
 		statusArea.setSpacing(10);
 		statusArea.add(consoleTitle);
 		statusArea.setCellWidth(consoleTitle, "100%");
 		statusArea.setWidth("100%");
 		consoleDP.add(statusArea);
 
 		consoleDP.setCellVerticalAlignment(statusArea, HasVerticalAlignment.ALIGN_TOP);
 
 		docsAndConsoleVertPanel.add(consoleDP);
 		mainOuterPanel.add(docsAndConsoleVertPanel);
 
 		// right side - open docs
 		VerticalPanel openDocsOuterPanel = new VerticalPanel();
 		openDocsOuterPanel.setStyleName("openDocsOuterPanel");
 		openDocsOuterPanel.setSpacing(20);
 		HorizontalPanel openDocsDP = new HorizontalPanel();
 		openDocsDP.setStyleName("openDocsDP");
 		openDocsDP.setWidth("100%");
 
 		VerticalPanel openDocsInnerPanel = new VerticalPanel();
 		openDocsInnerPanel.setStyleName("openDocsInnerPanel");
 		HTML openDocumentsText = new HTML("<h2>Open Documents</h2>");
 		openDocsInnerPanel.add(openDocumentsText);
 		openDocsInnerPanel.setCellHeight(openDocumentsText, "2em");
 
 		// holds the left tab panel
 		HorizontalPanel innerHp = new HorizontalPanel();
 		innerHp.setSpacing(10);
 		leftPanel.add(documentsL);
 
 		// holds the buttons for the left tab panel
 		leftHPanel.add(lockButtonL);
 		// leftHPanel.add(saveButtonL);
 		// leftHPanel.add(removeTabL);
 		leftPanel.add(leftHPanel);
 
 		// holds the right tab panel
 		rightPanel.add(documentsR);
 
 		// holds the buttons for the right tab panel
 		rightHPanel.add(lockButtonR);
 		rightPanel.add(rightHPanel);
 
 		innerHp.add(leftPanel);
 		innerHp.add(rightPanel);
 
 		openDocsInnerPanel.add(innerHp);
 
 		openDocsDP.add(openDocsInnerPanel);
 		openDocsOuterPanel.add(openDocsDP);
 		mainOuterPanel.add(openDocsOuterPanel);
 
 		// Divide up the horizontal space
 		mainOuterPanel.setWidth("100%");
 		mainOuterPanel.setCellWidth(docsAndConsoleVertPanel, "200px");
 		mainOuterPanel.setCellWidth(openDocsOuterPanel, "100%");
 
 		openDocsOuterPanel.setWidth("100%");
 		innerHp.setCellWidth(leftPanel, "50%");
 		innerHp.setCellWidth(rightPanel, "50%");
 
 		innerHp.setWidth("100%");
 		innerHp.setHeight("100%");
 
 		// Fixing the vertical
 		mainOuterPanel.setCellHeight(docsAndConsoleVertPanel,"100%");
 
 
 		innerHp.setCellHeight(leftPanel, "100%");
 		innerHp.setCellHeight(rightPanel, "100%");
 
 		// Vertical textboxes
 		leftPanel.setCellHeight(documentsL, "100%");
 		rightPanel.setCellHeight(documentsR, "100%");
 		
 
 
 		//Setting up the document sizes
 		// the panels
 
 		openDocsDP.setCellVerticalAlignment(openDocsInnerPanel, HasAlignment.ALIGN_TOP);
 		openDocsDP.setHeight("100%");
 		openDocsOuterPanel.setHeight("100%");
 		openDocsInnerPanel.setHeight("100%");
 
 		leftPanel.setStyleName("leftPanel");
 		rightPanel.setStyleName("rightPanel");
 		leftPanel.setWidth("100%");
 		leftPanel.setHeight("100%");
 		rightPanel.setWidth("100%");
 		rightPanel.setHeight("100%");
 
 
 		// fixing space issues
 		leftPanel.setCellHeight(leftHPanel, "30px");
 		rightPanel.setCellHeight(rightHPanel, "30px");
 
 		// Setting console/document space
 		docsAndConsoleVertPanel.setCellHeight(consoleDP, "200px");
 
 		// Tab bars
 		documentsL.setWidth("100%");
 		documentsR.setWidth("100%");
 
 	}
 
 	/**
 	 * Add a selection handler to the tab panel. This allows us to refresh 
 	 * the doc title in the tab name.
 	 * @param left
 	 */
 	private void addSelectionHandlerToTabPanel(boolean left)
 	{
 		setGenericObjects(left);
 
 		final TabPanel tabPanelFinal = tabPanel;
 		final ArrayList<AbstractDocument> docListFinal = docList;
 		final ArrayList<TextArea> contentsListFinal = contentsList;
 		final ArrayList<TextBox> titleListFinal = titleList;
 		final Button lockButtonFinal = lockButton;
 		final Button removeTabButtonFinal = removeTabButton;
 		final Button refreshFinal = refresh;
 		final HorizontalPanel hPanelFinal = hPanel;
 		final Button saveDocButtonFinal = saveDocButton;
 
 		tabPanelFinal.addSelectionHandler(new SelectionHandler<Integer>() {
 			public void onSelection(SelectionEvent<Integer> event) {
 				int ind = tabPanelFinal.getTabBar().getSelectedTab();
 
 				hPanelFinal.clear();
 
 				if (docListFinal.get(ind) instanceof LockedDocument) {
 					// enable and add the save button
 					enableButton(saveDocButtonFinal);
 					hPanelFinal.add(saveDocButtonFinal);
 
 					// Enable the fields since have the lock
 					titleListFinal.get(ind).setEnabled(true);
 					contentsListFinal.get(ind).setEnabled(true);
 
 					// disable the refresh button
 					disableButton(refreshFinal);
 				} 
 				else {
 					// enable and add the lock button
 					enableButton(lockButtonFinal);
 					hPanelFinal.add(lockButtonFinal);
 
 					// Disabling the fields since you don't have the lock
 					titleListFinal.get(ind).setEnabled(false);
 					contentsListFinal.get(ind).setEnabled(false);
 
 					// enable the refresh button
 					enableButton(refreshFinal);
 				}
 
 				// add removeTab and refresh buttons, enable removeTab
 				enableButton(removeTabButtonFinal);
 				hPanelFinal.add(removeTabButtonFinal);
 				hPanelFinal.add(refreshFinal);
 			}
 		});
 	}
 
 	/**
 	 * Add click handlers to our buttons.
 	 */
 	private void addClickHandlersToButtons()
 	{
 		refreshList.addClickHandler(this);
 		createNew.addClickHandler(this);
 		lockButtonL.addClickHandler(this);
 		saveButtonL.addClickHandler(this);
 		lockButtonR.addClickHandler(this);
 		saveButtonR.addClickHandler(this);
 		showButtonL.addClickHandler(this);
 		showButtonR.addClickHandler(this);
 		removeTabR.addClickHandler(this);
 		removeTabL.addClickHandler(this);
 		refreshButtonL.addClickHandler(this);
 		refreshButtonR.addClickHandler(this);
 	}
 
 	/**
 	 * Adds status lines to the console window to enable transparency of the
 	 * underlying processes.
 	 * 
 	 * @param status
 	 *            the status to add to the console window
 	 */
 	protected void statusUpdate(String status) {
 		while (statusArea.getWidgetCount() > maxConsoleEnt)
 			statusArea.remove(1);
 
 		final HTML statusUpd = new HTML(status);
 		statusArea.add(statusUpd);
 	}
 
 	/**
 	 * Sets the generic private objects to the objects of the correct side. This 
 	 * simply specifies the objects based on their side (since there are a lot of things
 	 * that are the same for the left and the right).
 	 * @param left
 	 */
 	protected void setGenericObjects(boolean left)
 	{
 		if (left) {
 			tabPanel = documentsL;
 			docList = documentsLeftList;
 			contentsList = contentsL;
 			titleList = titleL;
 			lockButton = lockButtonL;
 			removeTabButton = removeTabL;
 			saveDocButton = saveButtonL;
 			refresh = refreshButtonL;
 			hPanel = leftHPanel;
 			side = "left";
 		} else {
 			tabPanel = documentsR;
 			docList = documentsRightList;
 			contentsList = contentsR;
 			titleList = titleR;
 			lockButton = lockButtonR;
 			removeTabButton = removeTabR;
 			saveDocButton = saveButtonR;
 			refresh = refreshButtonR;
 			hPanel = rightHPanel;
 			side = "right";
 		}
 	}
 
 	/**
 	 * Set the text (title) of the specified tab.
 	 * @param text
 	 * @param ind
 	 * @param side
 	 */
 	private void setTabText(String text, int ind, String side) {
 		if (text.length() > maxTabTextLen) 
 			text = text.substring(0, maxTabTextLen - 3) + "...";
 
 		if (side.equals("left")) 
 			documentsL.getTabBar().setTabText(ind, text);
 		else if (side.equals("right"))
 			documentsR.getTabBar().setTabText(ind, text);
 	}
 
 	/**
 	 * Adds a tab to either the left or the right tab panel.
 	 * 
 	 * @param title
 	 * @param content
 	 * @param left
 	 */
 	public void addTab(String title, String content, boolean left) {	
 		// holds the title and the contents
 		VerticalPanel vp = new VerticalPanel();
 		//vp.setSpacing(5);
 
 		// the document title
 		TextBox titleBox = new TextBox();
 		titleBox.setValue(title);
 		titleBox.setEnabled(true);
 		titleBox.setWidth("99%");
 		titleBox.setStyleName("titleBox");
 
 		// prevent spacing issues
 		// titleBox.setHeight("1.2em");
 
 		// the document contents
 		TextArea areaBox = new TextArea();
 		areaBox.setWidth("99%");
 		areaBox.setHeight("100%");
 		areaBox.setStyleName("documentTextBox");
 
 		titleBox.setText(title);
 		areaBox.setText(content);
 
 		titleBox.setEnabled(true);
 		areaBox.setEnabled(true);
 
 		vp.add(titleBox);
 		vp.add(areaBox);
 		vp.setCellHeight(titleBox, "2em");
 
 		// Centering the title box
 		//vp.setCellHorizontalAlignment(titleBox, HasHorizontalAlignment.ALIGN_CENTER);
 
 		setGenericObjects(left);
 
 		// add the doc title and contents to the appropriate tabpanel
 
 		// enable the lock, removeTab, save, and refresh buttons
 		enableButton(lockButton);
 		enableButton(removeTabButton);
 		enableButton(saveDocButton);
 		enableButton(refresh);
 
 		final int ind = titleList.size();
 
 		// set a value-change handler to the title box (so that it updates even when user 
 		// pastes stuff to it
 		titleBox.addValueChangeHandler(new ValueChangeHandler<String>() {
 
 			@Override
 			public void onValueChange(ValueChangeEvent<String> event) {
 				setTabText(titleL.get(ind).getText(), ind, side);
 			}
 
 		});
 
 		// add key handler to the title box - update the tab text
 		// as the user is typing the title
 		titleBox.addKeyUpHandler(new KeyUpHandler() {
 
 			@Override
 			public void onKeyUp(KeyUpEvent event) {
 				setTabText(titleList.get(ind).getText(), ind, side);
 			}
 		});
 
 		// add the title and contents to the lists for bookkeeping
 		titleList.add(titleBox);
 		contentsList.add(areaBox);
 
 		// add the doc to the left tab panel
 		String subString = (title.length() > maxTabTextLen) ? title.substring(0, maxTabTextLen - 3) + "..." : title;
 		tabPanel.add(vp, subString);
 
 		int numLeftTabs = documentsL.getTabBar().getTabCount();
 		int numRightTabs = documentsR.getTabBar().getTabCount();
 
 		// if we have space for new doc, enable the button
 		if (numLeftTabs < maxTabsOnOneSide && numRightTabs < maxTabsOnOneSide)
 			enableButton(createNew);
 		else if (numLeftTabs >= maxTabsOnOneSide && numRightTabs >= maxTabsOnOneSide)
 			disableButton(createNew);
 
 		// can we add more tabs on the left?
 		if (numLeftTabs < maxTabsOnOneSide)
 			enableButton(showButtonL);
 		else 
 			disableButton(showButtonL);
 
 		// can we add more tabs on the right?
 		if (numRightTabs < maxTabsOnOneSide)
 			enableButton(showButtonR);
 		else 
 			disableButton(showButtonR);
 	}
 
 	/*
 	 * (non-Javadoc) Receives button events.
 	 * 
 	 * @see
 	 * com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event
 	 * .dom.client.ClickEvent)
 	 */
 	@Override
 	public void onClick(ClickEvent event) {
 
 		try {
 			// pressed 'refresh document list' button
 			if (event.getSource().equals(refreshList))
 				lister.getDocumentList();
 
 			// pressed 'new doc' button
 			else if (event.getSource().equals(createNew))
 			{
 				if (documentsL.getTabBar().getTabCount() < maxTabsOnOneSide)
 					createNewDocument("left");
 				else if (documentsR.getTabBar().getTabCount() < maxTabsOnOneSide)
 					createNewDocument("right");
 			}
 
 			// pressed left 'get lock' button
 			else if (event.getSource().equals(lockButtonL))
 				lockDocumentButtonHandler(true);
 
 			// pressed right 'get lock' button
 			else if (event.getSource().equals(lockButtonR))
 				lockDocumentButtonHandler(false);
 
 			// pressed left 'save doc' button
 			else if (event.getSource().equals(saveButtonL))
 				saveDocumentButtonHandler(true);
 
 			// pressed right 'save doc' button
 			else if (event.getSource().equals(saveButtonR))
 				saveDocumentButtonHandler(false);
 
 			// if show left is pressed, add doc to the left tab panel
 			else if (event.getSource().equals(showButtonL))
 				showDocumentButtonHandler(true);
 
 			// if show right is pressed, add doc to the right tab panel
 			else if (event.getSource().equals(showButtonR))
 				showDocumentButtonHandler(false);
 
 			// if user wants to remove current tab on left
 			else if (event.getSource().equals(removeTabL))
 				removeTabButtonHandler(true);
 
 			// if user wants to remove current tab on right
 			else if (event.getSource().equals(removeTabR))
 				removeTabButtonHandler(false);
 
 			else if (event.getSource().equals(refreshButtonL))
 				refreshButtonHandler(true);
 
 			else if (event.getSource().equals(refreshButtonR))
 				refreshButtonHandler(false);
 
 			// if user selects a doc from the doc list
 			else if (event.getSource().equals(documentList)) 
 				docListHandler();
 		}
 		catch (Exception e)
 		{
 			statusUpdate("ERROR: " + e.toString());
 		}
 	}
 
 	/**
 	 * Behaves similarly to locking a document, except without a key/lock obj.
 	 */
 	private void createNewDocument(String side) {		
 		LockedDocument ld = new LockedDocument(null, null, null,
 				"Enter the document title.", "Enter the document contents.");
 
 		boolean left = false;
 
 		if (side.equals("left"))
 			left = true;
 		else
 			left = false;
 
 		setGenericObjects(left);
 
 		docList.add(ld);
 		addTab(ld.getTitle(), ld.getContents(), left);
 		setTabText(ld.getTitle(), docList.size() - 1, side);
 		openLatestTab(side);
 
 		
 		disableButton(refresh);
 		disableButton(showButtonL);
 		disableButton(showButtonR);
 	}
 
 	/**
 	 * Opens the latest-opened tab on the appropriate tabpanel.
 	 * @param side
 	 */
 	public void openLatestTab(String side) {
 		if (side.equals("left")) {
 			int last = documentsL.getTabBar().getTabCount() - 1;
 			documentsL.getTabBar().selectTab(last);
 		} 
 		else {
 			int last = documentsR.getTabBar().getTabCount() - 1;
 			documentsR.getTabBar().selectTab(last);
 		}
 	}
 
 	/**
 	 * Handler for the 'refresh doc' button.
 	 * @param left
 	 */
 	private void refreshButtonHandler(boolean left)
 	{
 		setGenericObjects(left);
 
 		int index = tabPanel.getTabBar().getSelectedTab();
 		AbstractDocument currDoc = docList.get(index);
 
 		if (currDoc != null && currDoc.getKey() != null)
 			DocReader.readDoc(this, currDoc.getKey(), side, docList.size() - 1);
 	}
 
 	/**
 	 * Remove a tab from the correct side. Called after user presses either
 	 * right or left removeTab button.
 	 * 
 	 * @param left
 	 *            true if we want to remove left tab, false if right.
 	 */
 	private void removeTabButtonHandler(boolean left) {
 		setGenericObjects(left);
 
 		int ind = tabPanel.getTabBar().getSelectedTab();
 
 		// get the doc that we are removing from the tabpanel
 		AbstractDocument currDoc = docList.get(ind);
 
 		// if this doc is locked, release the lock since we are closing this tab
 		// (dont want a user locking their own document)
 		if (currDoc.getKey() != null && currDoc instanceof LockedDocument)
 			releaser.releaseLock((LockedDocument) currDoc);
 
 		tabPanel.remove(ind);
 		docList.remove(ind);
 		contentsList.remove(ind);
 		titleList.remove(ind);
 
 		// if we have another open tab before the deleted one
 		if (ind > 0)
 		{
 			hPanel.clear();
 
 			// select the previous tab
 			tabPanel.selectTab(ind - 1);
 
 			// if the title (and contents) of the prev tab is non-editable,
 			// then add 'lock', 'removeTab', and 'refresh' buttons
 			if (!titleList.get(ind - 1).isEnabled())
 			{
 				hPanel.add(lockButton);
 				enableButton(lockButton);
 				enableButton(refresh);
 			}
 			// title and contents are editable, so add 'save', 'remove', and 
 			// 'refresh' buttons (refresh must be disabled)
 			else
 			{
 				hPanel.add(saveDocButton);
 				enableButton(saveDocButton);
 				disableButton(refresh);
 			}
 
 			hPanel.add(removeTabButton);
 			hPanel.add(refresh);
 			enableButton(removeTabButton);
 		}
 
 		// otherwise, if this tab has no tabs to its left
 		else {
 			int numTabsLeft = tabPanel.getTabBar().getTabCount();
 
 			// if we still have tabs left (on the right)
 			if (numTabsLeft > 0) 
 			{
 				// select the next tab to the right (the new first tab)
 				tabPanel.selectTab(0);
 
 				hPanel.clear();
 
 				// if the title (and contents) of the next tab is non-editable,
 				// then add 'lock', 'removeTab', and 'refresh' buttons
 				if (!titleList.get(0).isEnabled())
 				{
 					hPanel.add(lockButton);
 					enableButton(lockButton);
 					enableButton(refresh);
 				}
 				// title and contents are editable, so add 'save', 'remove', and 
 				// 'refresh' buttons (refresh must be disabled)
 				else
 				{
 					hPanel.add(saveDocButton);
 					enableButton(saveDocButton);
 					disableButton(refresh);
 				}
 
 				hPanel.add(removeTabButton);
 				hPanel.add(refresh);
 				enableButton(removeTabButton);
 			}
 			// if no longer have any tabs on the left, disable all buttons
 			else {
 				for (Widget w : hPanel)
 					disableButton((Button) w);
 			}
 		}
 
 		// enable 'new doc' button
 		enableButton(createNew);
 	}
 
 	/**
 	 * Called after user presses either right or left 'save doc' button.
 	 * 
 	 * @param left
 	 */
 	private void saveDocumentButtonHandler(boolean left) {
 		setGenericObjects(left);
 
 		int ind = tabPanel.getTabBar().getSelectedTab();
 		AbstractDocument doc = docList.get(ind);
 
 		// if we can save this document
 		if (doc instanceof LockedDocument) {
 			// if title and contents have not been changed, no need to save
 			if (doc.getTitle().equals(titleList.get(ind).getValue())
 					&& doc.getContents().equals(contentsList.get(ind).getText()))
 				statusUpdate("No document changes; not saving.");
 
 			// otherwise if stuff was changed, save
 			else {
 				LockedDocument ld = (LockedDocument) doc;
 				String title = titleList.get(ind).getText();
 				String contents = contentsList.get(ind).getText();
 
 				// if the title and contents are less than the max length,
 				// then save this doc
 				if (title.length() < maxTitleLength && contents.length() < maxContentsLength)
 				{
 					ld.setTitle(title);
 					ld.setContents(contents);
 					DocSaver.saveDoc(this, ld, side, ind);
 				}
 				// otherwise, print error message to console
 				else if (contents.length() >= maxContentsLength)
 					statusUpdate("Error: Can't save; contents must be less than " + maxContentsLength + " characters.");
 				else if (title.length() >= maxTitleLength)
 					statusUpdate("Error: Can't save; title must be less than " + maxTitleLength + " characters.");
 			}
 		}
 	}
 
 	/**
 	 * Called after user presses either right of left 'lock doc' button.
 	 * 
 	 * @param left
 	 */
 	private void lockDocumentButtonHandler(boolean left) {
 		setGenericObjects(left);
 
 		// get the index of the selected tab on the right tabpanel
 		int ind = tabPanel.getTabBar().getSelectedTab();
 
 		// get the selected doc
 		AbstractDocument doc = docList.get(ind);
 
 		// Lock only if it can be locked.
 		// this call can result in either success or failure, both of
 		// which are taken care of in DocLocker
 		if (doc instanceof UnlockedDocument)
 			DocLocker.lockDoc(this, doc.getKey(), side, ind);
 	}
 
 	/**
 	 * Called after user presses either right of left 'show' button.
 	 * 
 	 * @param left
 	 */
 	private void showDocumentButtonHandler(boolean left) {
 		setGenericObjects(left);
 
 		String key = documentList.getValue(documentList.getSelectedIndex());
 
 		// if we arent already showing this doc, add it to the panel
 		if (!contained(key, documentsLeftList, documentsRightList))
 			openDocument(side);
 
 		// this is already up on the tabpanels, so disable these buttons
 		disableButton(showButtonL);
		disableButton(showButtonR);
 	}
 
 	public void openDocument(String side) {
 		int docIndx = documentList.getSelectedIndex();
 		String title = documentList.getItemText(docIndx);
 		String key = documentList.getValue(docIndx);
 
 		if (side.equals("left")) {
 			documentsLeftList.add(null);
 			addTab(title, "", true);
 			DocReader.readDoc(this, key, "left", documentsLeftList.size() - 1);
 		} else {
 			documentsRightList.add(null);
 			addTab(title, "", false);
 			DocReader.readDoc(this, key, "right", documentsRightList.size() - 1);
 		}
 
 		openLatestTab(side);
 	}
 
 	/**
 	 * Called when the user selects a doc from the doc list.
 	 */
 	private void docListHandler()
 	{
 		// if we selected something valid in the doc list
 		if (documentList.getSelectedIndex() >= 0)
 		{
 			String key = documentList.getValue(documentList.getSelectedIndex());
 
 			// if not already showing this doc, disable showLeft + showRight
 			if (contained(key, documentsLeftList, documentsRightList)) {
 				disableButton(showButtonL);
				disableButton(showButtonR);
 			} else {
 				enableButton(showButtonL);
 				enableButton(showButtonR);
 			}
 
 			// disable show left or right based on how many tabs are open
 			int numLeftTabs = documentsL.getTabBar().getTabCount();
 			int numRightTabs = documentsR.getTabBar().getTabCount();
 
 			if (numLeftTabs >= maxTabsOnOneSide)
 				disableButton(showButtonL);
 			if (numRightTabs >= maxTabsOnOneSide)
 				enableButton(showButtonR);
 
 			// disable new doc if no more space anywhere
 			if (numLeftTabs >= maxTabsOnOneSide && numRightTabs >= maxTabsOnOneSide)
 			{
 				disableButton(createNew);
 				statusUpdate("No more space on the tab panels!");
 			}
 		}
 	}
 
 	/**
 	 * Returns true of key is in either of the lists, false otherwise.
 	 * 
 	 * @param key
 	 * @param list
 	 * @return
 	 */
 	private boolean contained(String key, ArrayList<AbstractDocument> list1,
 			ArrayList<AbstractDocument> list2) {		
 
 		if (list1 == null || list2 == null)
 			return false;
 
 		boolean contains = false;
 
 		for (AbstractDocument doc1 : list1) {
 			if (doc1 != null && doc1.getKey() != null)
 			{
 				if (doc1.getKey().equals(key))
 					contains = true;
 			}
 		}
 
 		for (AbstractDocument doc2 : list2) {
 			if (doc2 != null && doc2.getKey() != null)
 			{
 				if (doc2.getKey().equals(key))
 					contains = true;
 			}
 		}
 
 		return contains;
 	}
 
 	/**
 	 * Generalized so that it can be called elsewhere. In particular, after a
 	 * document is saved, it calls this function to simulate an initial reading
 	 * of a document.
 	 * 
 	 * Called by docsaver and docreader.
 	 * 
 	 * @param result the unlocked doc that should be displayed
 	 */
 	protected void setDoc(UnlockedDocument result, int index, String side) {
 		// set the tab text to the doc title
 		setTabText(result.getTitle(), index, side);
 
 		if (side.equals("left"))
 			setGenericObjects(true);
 		else
 			setGenericObjects(false);
 
 		docList.set(index, result);
 
 		// set the title and contents to be the most updated stuff 
 		// from the input fields
 		titleList.get(index).setValue(result.getTitle());
 		contentsList.get(index).setValue(result.getContents());
 
 		// title and contents cannot be edited
 		titleList.get(index).setEnabled(false);
 		contentsList.get(index).setEnabled(false);
 
 		// add lock, remove tab, and refresh buttons
 		hPanel.clear();
 		hPanel.add(lockButton);
 		hPanel.add(removeTabButton);
 		hPanel.add(refresh);
 
 		// enable lock, refreshDoc, and removeTab buttons
 		enableButton(lockButton);
 		enableButton(removeTabButton);
 		enableButton(refresh);
 	}
 	
 	protected void enableButton(Button b) {
 		/* Enable button */
 		b.setEnabled(true);
 
 		/* Replace CSS image */
 		String curClass = b.getStylePrimaryName();
 		if (curClass.contains(disabledCSS)) {
 			curClass = curClass.replace(disabledCSS, "");
 			b.setStylePrimaryName(curClass);
 		}
 	}
 	protected void disableButton(Button b) {
 		/* Disable button */
 		b.setEnabled(false);
 		/* Replace CSS image */
 		String curClass = b.getStylePrimaryName();
 		if (!curClass.contains(disabledCSS)) {
 			curClass += disabledCSS;
 			b.setStylePrimaryName(curClass);
 		}
 	}
 }
