 package edu.caltech.cs141b.hw2.gwt.collab.client;
 
 import java.util.ArrayList;
 
 import com.google.gwt.dom.client.NativeEvent;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DecoratorPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.RichTextArea;
 import com.google.gwt.user.client.ui.TabPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 import edu.caltech.cs141b.hw2.gwt.collab.shared.AbstractDocument;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.UnlockedDocument;
 
 /**
  * Main class for a single Collaborator widget.
  */
 public class Collaborator extends Composite implements ClickHandler {
 
 	private int leftTabCount = 1;
 	private int rightTabCount = 1;
 
 	protected CollaboratorServiceAsync collabService;
 
 	// Track document information.
 	protected ArrayList<AbstractDocument> documentsLeftList = new ArrayList<AbstractDocument>();
 	protected ArrayList<AbstractDocument> documentsRightList = new ArrayList<AbstractDocument>();
 
 	// Managing available documents.
 	protected ListBox documentList = new ListBox();
 	private Button refreshList = new Button("Refresh Document List");
 	private Button createNew = new Button("Create New Document");
 
 	// For displaying document information and editing document content.
 	protected ArrayList<TextBox> titleL = new ArrayList<TextBox>();
 	protected ArrayList<RichTextArea> contentsL = new ArrayList<RichTextArea>();
 	protected ArrayList<TextBox> titleR = new ArrayList<TextBox>();
 	protected ArrayList<RichTextArea> contentsR = new ArrayList<RichTextArea>();
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
 		DecoratorPanel openDocsDP = new DecoratorPanel();
 		openDocsDP.setStyleName("openDocsDP");
 		openDocsDP.setWidth("100%");
 
 		VerticalPanel openDocsInnerPanel = new VerticalPanel();
 		openDocsInnerPanel.setStyleName("openDocsInnerPanel");
 		openDocsInnerPanel.add(new HTML("<h2>Open Documents</h2>"));
 
 		// holds the left tab panel
 		HorizontalPanel innerHp = new HorizontalPanel();
 		innerHp.setSpacing(10);
 		leftPanel.add(documentsL);
 
 		// holds the buttons for the left tab panel
 		leftHPanel.add(lockButtonL);
 		// leftHPanel.add(saveButtonL);
 		leftHPanel.add(removeTabL);
 		leftPanel.add(leftHPanel);
 
 		// holds the right tab panel
 		rightPanel.add(documentsR);
 
 		// holds the buttons for the right tab panel
 		rightHPanel.add(lockButtonR);
 		// rightHPanel.add(saveButtonR);
 		rightHPanel.add(removeTabR);
 		rightPanel.add(rightHPanel);
 
 		innerHp.add(leftPanel);
 		innerHp.add(rightPanel);
 
 		openDocsInnerPanel.add(innerHp);
 
 		openDocsDP.add(openDocsInnerPanel);
 		openDocsOuterPanel.add(openDocsDP);
 		mainOuterPanel.add(openDocsOuterPanel);
 
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

 					titleR.get(ind).setEnabled(true);
 					contentsR.get(ind).setEnabled(true);					
 				} else {
 					rightHPanel.clear();
 					rightHPanel.add(lockButtonR);
 					rightHPanel.add(removeTabR);
 
 					titleR.get(ind).setEnabled(false);
 					contentsR.get(ind).setEnabled(false);
 				}
 			}
 		});
 
 		documentList.addClickHandler(this);
 		documentList.setVisibleItemCount(20);
 
 		initWidget(mainOuterPanel);
 		lister.getDocumentList();
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
 		vp.add(titleBox);
 
 		// the document contents
 		RichTextArea areaBox = new RichTextArea();
 		areaBox.setText(content);
 		areaBox.setEnabled(true);
 		vp.add(areaBox);
 
 		// add the doc title and contents to the appropriate tabpanel
 		if (left) {
 			// add the title and contents to the lists for bookkeeping
 			titleL.add(titleBox);
 			contentsL.add(areaBox);
 
 			// add the doc to the left tab panel
 			documentsL.add(vp, Integer.toString(leftTabCount));
 			leftTabCount++;
 		} else {
 			// add the title and contents to the lists for bookkeeping
 			titleR.add(titleBox);
 			contentsR.add(areaBox);
 
 			// add the doc to the right tab panel
 			documentsR.add(vp, Integer.toString(rightTabCount));
 			rightTabCount++;
 		}
 	}
 
 	/**
 	 * Behaves similarly to locking a document, except without a key/lock obj.
 	 */
 	private void createNewDocument(String side) {
 		LockedDocument ld = new LockedDocument(null, null, null,
 				"Enter the document title.", "Enter the document contents.");
 		if (side.equals("left")) {
 			documentsLeftList.add(ld);
 			addTab(ld.getTitle(), ld.getContents(), true);
 		} else {
 			documentsRightList.add(ld);
 			addTab(ld.getTitle(), ld.getContents(), false);
 		}
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
 			reader = DocReader.readDoc(this, key, "left", documentsLeftList
 					.size() - 1);
 
 		} else {
 			documentsRightList.add(null);
 			addTab(title, "", false);
 			reader = DocReader.readDoc(this, key, "right", documentsRightList
 					.size() - 1);
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
 		while (statusArea.getWidgetCount() > 10)
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
 		else if (event.getSource().equals(lockButtonL)) {
 			// get the index of the selected tab on the left tabpanel
 			int ind = documentsL.getTabBar().getSelectedTab();
 
 			// get the selected doc
 			AbstractDocument doc = documentsLeftList.get(ind);
			// Lock only if it can be locked.

			if (doc instanceof UnlockedDocument) {
 				DocLocker.lockDoc(this, doc.getKey(), "left", ind);
				leftHPanel.clear();
				leftHPanel.add(saveButtonL);
				leftHPanel.add(removeTabL);
			}
 		}
 
 		// pressed right 'get lock' button
 		else if (event.getSource().equals(lockButtonR)) {
 			// get the index of the selected tab on the right tabpanel
 			int ind = documentsR.getTabBar().getSelectedTab();
 
 			// get the selected doc
 			AbstractDocument doc = documentsRightList.get(ind);
			
			// Lock only if it can be locked.
			if (doc instanceof UnlockedDocument) {
 				DocLocker.lockDoc(this, doc.getKey(), "right", ind);
				rightHPanel.clear();
				rightHPanel.add(saveButtonR);
				rightHPanel.add(removeTabR);
			}
 		}
 
 		// pressed left 'save doc' button
 		else if (event.getSource().equals(saveButtonL)) {
 			int ind = documentsL.getTabBar().getSelectedTab();
 			AbstractDocument doc = documentsLeftList.get(ind);
 
 			if (doc instanceof LockedDocument) {
 				if (doc.getTitle().equals(titleL.get(ind).getValue())
 						&& doc.getContents().equals(
 								contentsL.get(ind).getHTML())) {
 					statusUpdate("No document changes; not saving.");
 				} else {
 					LockedDocument ld = (LockedDocument) doc;
 					ld.setTitle(titleL.get(ind).getValue());
 					ld.setContents(contentsL.get(ind).getHTML());
 					DocSaver.saveDoc(this, ld, "left", ind);
 					saveButtonL.setEnabled(true);
 					saveButtonL.removeFromParent();
 					leftHPanel.add(lockButtonL);
 					leftHPanel.add(removeTabL);
 
 				}
 			}
 		}
 
 		// pressed right 'save doc' button
 		else if (event.getSource().equals(saveButtonR)) {
 			int ind = documentsR.getTabBar().getSelectedTab();
 			AbstractDocument doc = documentsRightList.get(ind);
 			if (doc instanceof LockedDocument) {
 				if (doc.getTitle().equals(titleR.get(ind).getValue())
 						&& doc.getContents().equals(
 								contentsR.get(ind).getHTML())) {
 					statusUpdate("No document changes; not saving.");
 				} else {
 					LockedDocument ld = (LockedDocument) doc;
 					ld.setTitle(titleR.get(ind).getValue());
 					ld.setContents(contentsR.get(ind).getHTML());
 					DocSaver.saveDoc(this, ld, "right", ind);
 					saveButtonR.setEnabled(true);
 					saveButtonR.removeFromParent();
 					rightHPanel.add(lockButtonR);
 					rightHPanel.add(removeTabR);
 				}
 			}
 		}
 
 		// if show left is pressed, add doc to the left tab panel
 		else if (event.getSource().equals(showButtonL)) {
 			String key = documentList.getValue(documentList.getSelectedIndex());
 			// if we arent already showing this doc, add it to the panel
 			if (!contained(key, documentsLeftList, documentsRightList)) {
 				openDocument("left");
 			}
 
 			// this is already up on the tabpanels, so disable these buttons
 			showButtonL.setEnabled(false);
 			showButtonR.setEnabled(false);
 		}
 
 		// if show right is pressed, add doc to the right tab panel
 		else if (event.getSource().equals(showButtonR)) {
 			String key = documentList.getValue(documentList.getSelectedIndex());
 			// if we arent already showing this doc, add it to the panel
 			if (!contained(key, documentsLeftList, documentsRightList)) {
 				openDocument("right");
 			}
 
 			// this is already up on the tabpanels, so disable these buttons
 			showButtonL.setEnabled(false);
 			showButtonR.setEnabled(false);
 		}
 
 		// TODO - does not work
 		// if user wants to remove current tab on left
 		else if (event.getSource().equals(removeTabL)) {
 			int ind = documentsL.getTabBar().getSelectedTab();
 			documentsL.remove(ind); // remove from the tabpanel
 			// statusUpdate("" + ind + ", " +
 			// documentsLeftList.get(ind).getContents());
 			documentsLeftList.remove(ind); // remove from the list of things on
			// the left
 		}
 
 		// TODO - does not work
 		// if user wants to remove current tab on right
 		else if (event.getSource().equals(removeTabR)) {
 			int ind = documentsR.getTabBar().getSelectedTab();
 			documentsR.remove(ind); // remove from the tabpanel
 			documentsRightList.remove(ind); // remove from the list of things on
			// the right
 		}
 
 		else if (event.getSource().equals(documentList)) {
 			String key = documentList.getValue(documentList.getSelectedIndex());
 			// if we aren already showing this doc, disable show left and show
 			// right
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
 		statusUpdate("hello " + side);
 		if (side.equals("left")) {
 			documentsLeftList.set(index, result);
 			titleL.get(index).setValue(result.getTitle());
 			contentsL.get(index).setHTML(result.getContents());
 
 			// titleL.get(index).setEnabled(false);
 			// contentsL.get(index).setEnabled(false);
 		}
 		// if (side.equals("right")
 		else {
 			statusUpdate("in " + side);
 			documentsRightList.set(index, result);
 			titleR.get(index).setValue(result.getTitle());
 			contentsR.get(index).setHTML(result.getContents());
 
 			// titleR.get(index).setEnabled(false);
 			// contentsR.get(index).setEnabled(false);
 		}
 	}
 }
