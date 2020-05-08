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
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.SplitLayoutPanel;
 import com.google.gwt.user.client.ui.TabPanel;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 import edu.caltech.cs141b.hw2.gwt.collab.shared.AbstractDocument;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.UnlockedDocument;
 
 /**
  * Main class for a single Collaborator widget.
  */
 public class Collaborator extends Composite implements ClickHandler {
 
 	final private int maxTabTextLen = 15;
 	final private int maxConsoleEnt = 6;
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
 
 	
 	// Status tracking.
 	private VerticalPanel statusArea = new VerticalPanel();
 
 	/**
 	 * UI initialization.
 	 * 
 	 * @param collabService
 	 */
 	public Collaborator(CollaboratorServiceAsync collabService) {
 		this.collabService = collabService;
 
 		// the main outer panel - holds everything
 		HorizontalPanel mainOuterPanel = new HorizontalPanel();
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
 		docListPanel.add(new HTML("<h2>Available Documents</h2>"));
 		documentList.setWidth("100%");
 		docListPanel.add(documentList);
 
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
 
 		DecoratorPanel consoleDP = new DecoratorPanel();
 		consoleDP.setStyleName("consoleDP");
 		consoleDP.setWidth("100%");
 		statusArea.setSpacing(10);
 		statusArea.add(new HTML("<h2>Console</h2>"));
 		consoleDP.add(statusArea);
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
 		mainOuterPanel.setHeight("100%");
 		mainOuterPanel.setCellWidth(docsAndConsoleVertPanel, "200px");
 		mainOuterPanel.setCellWidth(openDocsOuterPanel, "100%");
 		innerHp.setCellWidth(leftPanel, "50%");
 		innerHp.setCellWidth(rightPanel, "50%");
		
 		innerHp.setWidth("100%");
 		innerHp.setHeight("100%");
 		
		// Fixing the vertical
		innerHp.setCellHeight(leftPanel, "100%");
		innerHp.setCellHeight(rightPanel, "100%");
		
 		
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
 		
 		
 
 		// buttons
 		refreshList.addClickHandler(this);
 		createNew.addClickHandler(this);
 		refreshDoc.addClickHandler(this);
 		lockButtonL.addClickHandler(this);
 		saveButtonL.addClickHandler(this);
 		lockButtonR.addClickHandler(this);
 		saveButtonR.addClickHandler(this);
 		showButtonL.addClickHandler(this);
 		showButtonR.addClickHandler(this);
 		removeTabR.addClickHandler(this);
 		removeTabL.addClickHandler(this);
 
 		documentsL.addSelectionHandler(new SelectionHandler<Integer>() {
 			public void onSelection(SelectionEvent<Integer> event) {
 				int ind = documentsL.getTabBar().getSelectedTab();
 				leftHPanel.clear();
 				if (documentsLeftList.get(ind) instanceof LockedDocument) {
 					leftHPanel.clear();
 					leftHPanel.add(saveButtonL);
 					leftHPanel.add(removeTabL);
 
 					// Enable the fields since have the lock
 					titleL.get(ind).setEnabled(true);
 					contentsL.get(ind).setEnabled(true);
 
 				} else {
 					leftHPanel.clear();
 					leftHPanel.add(lockButtonL);
 					leftHPanel.add(removeTabL);
 
 					// Disabling the fields since you don't have the lock
 					titleL.get(ind).setEnabled(false);
 					contentsL.get(ind).setEnabled(false);
 				}
 			}
 		});
 
 		documentsR.addSelectionHandler(new SelectionHandler<Integer>() {
 			public void onSelection(SelectionEvent<Integer> event) {
 				int ind = documentsR.getTabBar().getSelectedTab();
 				if (documentsRightList.get(ind) instanceof LockedDocument) {
 					rightHPanel.clear();
 					rightHPanel.add(saveButtonR);
 					rightHPanel.add(removeTabR);
 
 					// Enable the fields since have the lock
 					titleR.get(ind).setEnabled(true);
 					contentsR.get(ind).setEnabled(true);
 				} else {
 					rightHPanel.clear();
 					rightHPanel.add(lockButtonR);
 					rightHPanel.add(removeTabR);
 
 					// Disabling the fields since you don't have the lock
 					titleR.get(ind).setEnabled(false);
 					contentsR.get(ind).setEnabled(false);
 				}
 			}
 		});
 
 		documentList.addClickHandler(this);
 		documentList.setVisibleItemCount(20);
 
 		// the 'get lock' button should be initially disabled since there are no
 		// tabs open
 		lockButtonL.setEnabled(false);
 		lockButtonR.setEnabled(false);
 
 		initWidget(mainOuterPanel);
 		lister.getDocumentList();
 	}
 
 	private void setTabText(String text, int ind, String side) {
 		if (text.length() > maxTabTextLen) {
 			text = text.substring(0, maxTabTextLen - 3) + "...";
 		}
 		
 		if (side.equals("left")) {
 			documentsL.getTabBar().setTabText(ind, text);
 		} else if (side.equals("right")) {
 			documentsR.getTabBar().setTabText(ind, text);
 		}
 	}
 
 	/**
 	 * Adds a tab to either the left or the right tab panel.
 	 * 
 	 * @param title
 	 * @param content
 	 * @param left
 	 */
 	public void addTab(String title, String content, boolean left) {
 		VerticalPanel vp = new VerticalPanel();
 		vp.setSpacing(5);
 
 		// the document title
 		TextBox titleBox = new TextBox();
 		titleBox.setValue(title);
 		titleBox.setEnabled(true);
 		titleBox.setWidth("250px");
 		vp.add(titleBox);
 		vp.setCellHeight(titleBox, "1.5em");
 		//vp.setCellHorizontalAlignment(titleBox, HasHorizontalAlignment.ALIGN_CENTER);
 		
 		// prevent spacing issues
 		// titleBox.setHeight("1.2em");
 
 		// the document contents
 		TextArea areaBox = new TextArea();
 		areaBox.setWidth("97%");
 		areaBox.setHeight("100%");
 		areaBox.setStyleName("documentTextBox");
 		//areaBox.setHeight("200px");
 		// areaBox.setHTML(content);
 		areaBox.setText(content);
 
 		areaBox.setEnabled(true);
 		vp.add(areaBox);
 		
 
 		// add the doc title and contents to the appropriate tabpanel
 		if (left) {
 			// enable the left 'get lock' and 'remove tab' buttons
 			lockButtonL.setEnabled(true);
 			removeTabL.setEnabled(true);
 			final int ind = titleL.size();
 
 			titleBox.addValueChangeHandler(new ValueChangeHandler<String>() {
 
 				@Override
 				public void onValueChange(ValueChangeEvent<String> event) {
 					setTabText(titleL.get(ind).getText(), ind, "left");
 				}
 
 			});
 			
 			titleBox.addKeyUpHandler(new KeyUpHandler() {
 
 				@Override
 				public void onKeyUp(KeyUpEvent event) {
 					setTabText(titleL.get(ind).getText(), ind, "left");
 				}
 			});
 
 			// add the title and contents to the lists for bookkeeping
 			titleL.add(titleBox);
 			contentsL.add(areaBox);
 
 			// add the doc to the left tab panel
 
 			// what is the number of the last tab open?
 			int leftTabCount = documentsL.getTabBar().getTabCount() + 1;
 
 			// add the doc to the left tab panel
 			documentsL.add(vp, Integer.toString(leftTabCount));
 		} else {
 			// enable the right 'get lock' and 'remove tab' buttons
 			lockButtonR.setEnabled(true);
 			removeTabR.setEnabled(true);
 
 			final int ind = titleR.size();
 
 			titleBox.addValueChangeHandler(new ValueChangeHandler<String>() {
 
 				@Override
 				public void onValueChange(ValueChangeEvent<String> event) {
 					setTabText(titleR.get(ind).getText(), ind, "right");
 				}
 
 			});
 			
 			titleBox.addKeyUpHandler(new KeyUpHandler() {
 
 				@Override
 				public void onKeyUp(KeyUpEvent event) {
 					setTabText(titleR.get(ind).getText(), ind, "right");
 
 				}
 			});
 
 			// add the title and contents to the lists for bookkeeping
 			titleR.add(titleBox);
 			contentsR.add(areaBox);
 
 			// what is the number of the last tab open?
 			int rightTabCount = documentsR.getTabBar().getTabCount() + 1;
 
 			// add the doc to the right tab panel
 			documentsR.add(vp, Integer.toString(rightTabCount));
 		}
 	}
 
 	/**
 	 * Behaves similarly to locking a document, except without a key/lock obj.
 	 */
 	private void createNewDocument(String side) {
 		LockedDocument ld = new LockedDocument(null, null, null,
 				"Enter the document title.", "Enter the document contents.");
 		int ind = 0;
 		if (side.equals("left")) {
 			documentsLeftList.add(ld);
 			addTab(ld.getTitle(), ld.getContents(), true);
 			ind = documentsLeftList.size()- 1;
 		} else {
 			documentsRightList.add(ld);
 			addTab(ld.getTitle(), ld.getContents(), false);
 			ind = documentsRightList.size() - 1;
 		}
 		setTabText(ld.getTitle(), ind, side);
 		
 		openLatestTab(side);
 	}
 
 	public void openLatestTab(String side) {
 		if (side.equals("left")) {
 			int last = documentsL.getTabBar().getTabCount() - 1;
 			documentsL.getTabBar().selectTab(last);
 		} else {
 			int last = documentsR.getTabBar().getTabCount() - 1;
 			documentsR.getTabBar().selectTab(last);
 		}
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
 			DocReader
 					.readDoc(this, key, "right", documentsRightList.size() - 1);
 		}
 
 		openLatestTab(side);
 
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
 
 	/*
 	 * (non-Javadoc) Receives button events.
 	 * 
 	 * @see
 	 * com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event
 	 * .dom.client.ClickEvent)
 	 */
 	@Override
 	public void onClick(ClickEvent event) {
 
 		// pressed 'refresh document list' button
 		if (event.getSource().equals(refreshList))
 			lister.getDocumentList();
 
 		// pressed 'new doc' button
 		else if (event.getSource().equals(createNew))
 			createNewDocument("left");
 
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
 
 		else if (event.getSource().equals(documentList)) {
 			String key = documentList.getValue(documentList.getSelectedIndex());
 			// if we arent already showing this doc, disable showLeft +
 			// showRight
 			if (contained(key, documentsLeftList, documentsRightList)) {
 				showButtonL.setEnabled(false);
 				showButtonR.setEnabled(false);
 			} else {
 				showButtonL.setEnabled(true);
 				showButtonR.setEnabled(true);
 			}
 		}
 	}
 
 	/**
 	 * Remove a tab from the correct side. Claled after user presses either
 	 * right or left removeTab button.
 	 * 
 	 * @param left
 	 *            true if we want to remove left tab, fasle if right.
 	 */
 	private void removeTabButtonHandler(boolean left) {
 		TabPanel tabPanel = null;
 		ArrayList<AbstractDocument> docList = null;
 		ArrayList<TextArea> contentsList = null;
 		ArrayList<TextBox> titleList = null;
 		Button lockButton = null;
 		Button removeTabButton = null;
 		Button saveDocButton = null;
 
 		if (left) {
 			tabPanel = documentsL;
 			docList = documentsLeftList;
 			contentsList = contentsL;
 			titleList = titleL;
 			lockButton = lockButtonL;
 			removeTabButton = removeTabL;
 			saveDocButton = saveButtonL;
 		} else {
 			tabPanel = documentsR;
 			docList = documentsRightList;
 			contentsList = contentsR;
 			titleList = titleR;
 			lockButton = lockButtonR;
 			removeTabButton = removeTabR;
 			saveDocButton = saveButtonR;
 		}
 
 		int ind = tabPanel.getTabBar().getSelectedTab();
 		tabPanel.remove(ind);
 
 		// remove from the lists of things on the right
 		docList.remove(ind);
 		contentsList.remove(ind);
 		titleList.remove(ind);
 
 		// if we have another open tab before the deleted one
 		if (ind > 0) {
 			// select the previous tab
 			tabPanel.selectTab(ind - 1);
 
 			// set the fields of the prev doc to non-editable
 			titleList.get(ind - 1).setEnabled(false);
 			contentsList.get(ind - 1).setEnabled(false);
 		}
 		// otherwise, if this tab has no tabs to its left
 		else {
 			int numTabsLeft = tabPanel.getTabBar().getTabCount();
 
 			// if we still have tabs left, select the next tab to the right
 			if (numTabsLeft > 0) {
 				tabPanel.selectTab(numTabsLeft - 1);
 
 				// enable lock and remove buttons
 				lockButton.setEnabled(true);
 				removeTabButton.setEnabled(true);
 
 			}
 			// if no longer have any tabs on the left, disable lock and
 			// removeTab
 			else {
 				saveDocButton.setEnabled(false);
 				lockButton.setEnabled(false);
 				removeTabButton.setEnabled(false);
 			}
 
 		}
 
 		// enable show left and show right
 		showButtonL.setEnabled(true);
 		showButtonR.setEnabled(true);
 	}
 
 	/**
 	 * Called after user presses either right or left 'save doc' button.
 	 * 
 	 * @param left
 	 *            true if side is left, false if right.
 	 */
 	private void saveDocumentButtonHandler(boolean left) {
 		TabPanel tabPanel = null;
 		ArrayList<AbstractDocument> docList = null;
 		ArrayList<TextArea> contentsList = null;
 		ArrayList<TextBox> titleList = null;
 		Button removeTabButton = null;
 		Button saveButton = null;
 		Button lockButton = null;
 		String side = null;
 		HorizontalPanel hPanel = null;
 
 		if (left) {
 			tabPanel = documentsL;
 			docList = documentsLeftList;
 			contentsList = contentsL;
 			titleList = titleL;
 			removeTabButton = removeTabL;
 			saveButton = saveButtonL;
 			lockButton = lockButtonL;
 			side = "left";
 			hPanel = leftHPanel;
 		} else {
 			tabPanel = documentsR;
 			docList = documentsRightList;
 			contentsList = contentsR;
 			titleList = titleR;
 			removeTabButton = removeTabR;
 			saveButton = saveButtonR;
 			lockButton = lockButtonR;
 			side = "right";
 			hPanel = rightHPanel;
 		}
 
 		int ind = tabPanel.getTabBar().getSelectedTab();
 		AbstractDocument doc = docList.get(ind);
 
 		// if we can save this document
 		if (doc instanceof LockedDocument) {
 			// if title and contents have not been changed, no need to save
 			if (doc.getTitle().equals(titleList.get(ind).getValue())
 					&& doc.getContents()
 							.equals(contentsList.get(ind).getText()))
 				statusUpdate("No document changes; not saving.");
 
 			// otherwise if stuff was changed, save
 			else {
 				LockedDocument ld = (LockedDocument) doc;
 				ld.setTitle(titleList.get(ind).getValue());
 
 				ld.setContents(contentsList.get(ind).getText());
 
 				DocSaver.saveDoc(this, ld, side, ind);
 				saveButton.setEnabled(true);
 				saveButton.removeFromParent();
 				hPanel.add(lockButton);
 				hPanel.add(removeTabButton);
 			}
 		}
 	}
 
 	/**
 	 * Called after user presses either right of left 'lock doc' button.
 	 * 
 	 * @param left
 	 */
 	private void lockDocumentButtonHandler(boolean left) {
 		TabPanel tabPanel = null;
 		ArrayList<AbstractDocument> docList = null;
 		Button removeTabButton = null;
 		Button saveButton = null;
 		String side = null;
 		HorizontalPanel hPanel = null;
 
 		if (left) {
 			tabPanel = documentsL;
 			docList = documentsLeftList;
 			removeTabButton = removeTabL;
 			saveButton = saveButtonL;
 			side = "left";
 			hPanel = leftHPanel;
 		} else {
 			tabPanel = documentsR;
 			docList = documentsRightList;
 			removeTabButton = removeTabR;
 			saveButton = saveButtonR;
 			side = "right";
 			hPanel = rightHPanel;
 		}
 
 		// get the index of the selected tab on the right tabpanel
 		int ind = tabPanel.getTabBar().getSelectedTab();
 
 		// get the selected doc
 		AbstractDocument doc = docList.get(ind);
 
 		// Lock only if it can be locked.
 		if (doc instanceof UnlockedDocument) {
 			DocLocker.lockDoc(this, doc.getKey(), side, ind);
 
 			hPanel.clear();
 			hPanel.add(saveButton);
 			hPanel.add(removeTabButton);
 		}
 	}
 
 	/**
 	 * Called after user presses either right of left 'show' button.
 	 * 
 	 * @param left
 	 */
 	private void showDocumentButtonHandler(boolean left) {
 		String side = null;
 
 		if (left)
 			side = "left";
 		else
 			side = "right";
 
 		String key = documentList.getValue(documentList.getSelectedIndex());
 		// if we arent already showing this doc, add it to the panel
 		if (!contained(key, documentsLeftList, documentsRightList))
 			openDocument(side);
 
 		// this is already up on the tabpanels, so disable these buttons
 		showButtonL.setEnabled(false);
 		showButtonR.setEnabled(false);
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
 		boolean contains = false;
 
 		for (AbstractDocument doc : list1) {
 			if (doc.getKey().equals(key))
 				contains = true;
 		}
 
 		for (AbstractDocument doc : list2) {
 			if (doc.getKey().equals(key))
 				contains = true;
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
 	 * @param result
 	 *            the unlocked document that should be displayed
 	 */
 	protected void setDoc(UnlockedDocument result, int index, String side) {
 		// from saver: refresh and lock are enabled
 		// save and fields are disabled
 
 		setTabText(result.getTitle(), index, side);
 		
 		if (side.equals("left")) {
 			documentsLeftList.set(index, result);
 
 			titleL.get(index).setValue(result.getTitle());
 
 			contentsL.get(index).setValue(result.getContents());
 
 			titleL.get(index).setEnabled(false);
 			contentsL.get(index).setEnabled(false);
 		} else {
 			documentsRightList.set(index, result);
 			titleR.get(index).setValue(result.getTitle());
 
 			contentsR.get(index).setValue(result.getContents());
 
 			titleR.get(index).setEnabled(false);
 			contentsR.get(index).setEnabled(false);
 		}
 	}
 
 }
