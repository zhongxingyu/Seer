 package edu.caltech.cs141b.hw2.gwt.collab.client;
 
 import java.util.ArrayList;
 
 import com.google.gwt.appengine.channel.client.Channel;
 import com.google.gwt.appengine.channel.client.ChannelFactory;
 import com.google.gwt.appengine.channel.client.ChannelFactory.ChannelCreatedCallback;
 import com.google.gwt.appengine.channel.client.SocketError;
 import com.google.gwt.appengine.channel.client.SocketListener;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.PushButton;
 import com.google.gwt.user.client.ui.RichTextArea;
 import com.google.gwt.user.client.ui.TabBar;
 import com.google.gwt.user.client.ui.TabPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.ToggleButton;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 import edu.caltech.cs141b.hw2.gwt.collab.server.CollaboratorServiceImpl;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.Messages;
 
 /**
  * Main class for a single Collaborator widget.
  */
 public class Collaborator extends Composite implements ClickHandler, ChangeHandler {
 
 	public static final boolean SHOW_CONSOLE = true;
 	public static final int THINKING_RANGE_START = 0;  // ms
 	public static final int THINKING_RANGE_END = 2000;
 	public static final int EATING_RANGE_START = 0;
 	public static final int EATING_RANGE_END = 200;
 
 	protected CollaboratorServiceAsync collabService;
 
 	// UI elements:
 	protected VerticalPanel statusArea = new VerticalPanel();
 	protected HTML queueStatus = new HTML();
 	protected HorizontalPanel hp = new HorizontalPanel();
 	protected TabPanel tp = new TabPanel();
 	protected TabBar tb = tp.getTabBar();
 	protected ListBox documentList = new ListBox();
 	// Buttons for managing available documents.
 	protected PushButton refreshList = new PushButton(
 			new Image("images/refresh_small.png"));
 	protected PushButton createNew = new PushButton(
 			new Image("images/plus_small.png"));
 	// Buttons for displaying document information and editing document content.
 	protected PushButton refreshDoc = new PushButton(
 			new Image("images/refresh.png"));
 	protected PushButton lockButtonUnlocked = new PushButton(
 			new Image("images/locked.png"));
 	protected PushButton lockButtonLocked = new PushButton(
 			new Image("images/unlocked.png"));
 	protected PushButton lockButtonRequesting = new PushButton(
 			new Image("images/loading.gif"));
 	protected PushButton saveButton = new PushButton(
 			new Image("images/save.png"));
 	protected PushButton closeButton = new PushButton(
 			new Image("images/close.png"));
 	protected ToggleButton simulateButton = new ToggleButton(
 			new Image("images/play_button.png"),
 			new Image("images/pause_button.gif"));
 
 	// Callback objects.
 	protected DocLister lister = new DocLister(this);
 	protected DocReader reader = new DocReader(this);
 	protected DocRequestor requestor = new DocRequestor(this);
 	protected DocUnrequestor unrequestor = new DocUnrequestor(this);
 	protected DocLocker locker = new DocLocker(this);
 	protected DocReleaser releaser = new DocReleaser(this);
 	protected DocSaver saver = new DocSaver(this);
 	protected DocCreator creator = new DocCreator(this);
 	protected ChannelCreator channelCreator = new ChannelCreator(this);
 
 	// Variables for keeping track of current states of the application.
 	protected ArrayList<String> tabKeys = new ArrayList<String>();
 	protected ArrayList<RichTextArea> tabContents = new ArrayList<RichTextArea>();
 	protected ArrayList<TextBox> tabTitles = new ArrayList<TextBox>();
 	protected ArrayList<Integer> tabQueueLengths = new ArrayList<Integer>();
 	protected ArrayList<UiState> uiStates = new ArrayList<UiState>();
 	protected String channelToken = null;
 	protected boolean simulating = false;
 	protected Timer thinkingTimer = null;
 	protected Timer eatingTimer = null;
 
 	/**
 	 * UI initialization.
 	 * 
 	 * @param collabService
 	 */
 	public Collaborator(CollaboratorServiceAsync collabService) {
 		this.collabService = collabService;
 
 		// outerHp is our horizontal panel that includes the majority of the page.
 		HorizontalPanel outerHp = new HorizontalPanel();
 		outerHp.setWidth("100%");
 		outerHp.setHeight("100%");
 
 		// leftColVp holds our document list and console.
 		VerticalPanel leftColVp = new VerticalPanel();
 		leftColVp.add(new HTML("<h2>Docs</h2>"));
 
 		// docsButtonsHp holds relevant buttons (refresh / create new).
 		HorizontalPanel docsButtonsHp = new HorizontalPanel();
 		docsButtonsHp.add(refreshList);
 		docsButtonsHp.add(createNew);
 		leftColVp.add(docsButtonsHp);
 
 		// docsVp holds document list and relevant buttons (refresh / create new).
 		documentList.setStyleName("doc-list");
 		leftColVp.add(documentList);
 		leftColVp.setStyleName("list-column");
 
 		// Add console to leftColVp.
 		if (SHOW_CONSOLE) {
 			statusArea.setSpacing(10);
 			statusArea.add(new HTML("<h2>Console</h2>"));
 			leftColVp.add(statusArea);
 		}
 
 		// We are done packing leftColVp, so add it to outerHp.
 		outerHp.add(leftColVp);
 
 		// Now let's work on the right side of the page, which will include
 		// the tabPanel for documents (as well as some relevant buttons)
 		VerticalPanel rightColVp = new VerticalPanel();
 
 		// Create horizontal panel that holds the document-specific buttons.
 		hp.setSpacing(10);
 		hp.add(refreshDoc);
 		hp.add(lockButtonUnlocked);
 		hp.add(saveButton);
 		hp.add(closeButton);
 		hp.add(simulateButton);
 		hp.add(queueStatus);
 		rightColVp.add(hp);
 
 		// Add tab panel to rightColVp.
 		tp.setWidth("100%");
 		rightColVp.add(tp);
 		rightColVp.setWidth("100%");
 		rightColVp.setHeight("100%");
 		rightColVp.setStyleName("doc-column");
 
 		outerHp.add(rightColVp);
 
 		// Handlers code starts here:
 		// Adding selection handler to tab panel. Note that tabTitles
 		// and tabContents should be updated before the tab selection occurs.
 		tp.addSelectionHandler(new SelectionHandler<Integer>() {
 			public void onSelection(SelectionEvent<Integer> event) {
 				// Changes UI to update to the current selected tab.
 				int currentTabInd = tb.getSelectedTab();
 				setUiStateIfNoSim(uiStates.get(currentTabInd));
 			}
 		});
 
 		refreshList.addClickHandler(this);
 		createNew.addClickHandler(this);
 		refreshDoc.addClickHandler(this);
 		lockButtonUnlocked.addClickHandler(this);
 		lockButtonLocked.addClickHandler(this);
 		lockButtonRequesting.addClickHandler(this);
 		saveButton.addClickHandler(this);
 		closeButton.addClickHandler(this);
 		simulateButton.addClickHandler(this);
 
 		documentList.addChangeHandler(this);
 		documentList.setVisibleItemCount(10);
 
 		setUiStateIfNoSim(UiState.NOT_VIEWING);
 		initWidget(outerHp);
 
 		// Make initial necessary calls to server.
 		lister.getDocumentList();
 		channelCreator.createChannel();
 		
 		// Initialize timers.
 		thinkingTimer = new Timer() {
 			public void run() {
 				// When time is up, become hungry. That is, request the lock.
 				requestor.requestDocument(tabKeys.get(tb.getSelectedTab()));
 			}
 		};
 		eatingTimer = new Timer() {
 			public void run() {
 				// When time is up, go back to thinking. That is, save the document.
 				int currentTabInd = tb.getSelectedTab();
 				LockedDocument lockedDoc = new LockedDocument(null, null,
 						tabKeys.get(currentTabInd), tabTitles.get(currentTabInd).getValue(), 
 						tabContents.get(currentTabInd).getHTML());
 				saver.saveDocument(lockedDoc);
 				simulateThinking();
 			}
 		};
 	}
 
 	/* (non-Javadoc)
 	 * Receives button events.
 	 * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
 	 */
 	@Override
 	public void onClick(ClickEvent event) {
 		// Channel is not set up yet, so don't let the user do anything.
 		if (channelToken == null) {
 			Window.alert("Please wait while the channel is established. " +
 					"If this takes more than a few seconds, try refreshing the page.");
 			return;
 		}
 		
 		Object source = event.getSource();
 		if (source.equals(refreshList)) {
 			lister.getDocumentList();
 		} else if (source.equals(createNew)) {
 			creator.createDocument();
 		} else if (source.equals(refreshDoc)) {
 			reader.getDocument(tabKeys.get(tb.getSelectedTab()));
 		} else if (source.equals(lockButtonUnlocked)) {
 			requestor.requestDocument(tabKeys.get(tb.getSelectedTab()));
 		} else if (source.equals(lockButtonLocked)) {
 			int currentTabInd = tb.getSelectedTab();
 			LockedDocument lockedDoc = new LockedDocument(null, null,
 					tabKeys.get(currentTabInd),
 					tabTitles.get(currentTabInd).getValue(),
 					tabContents.get(currentTabInd).getHTML());
 			releaser.releaseLock(lockedDoc);
 		} else if (source.equals(lockButtonRequesting)) {
 			int currentTabInd = tb.getSelectedTab();
 			unrequestor.unrequestDocument(tabKeys.get(currentTabInd));
 		} else if (source.equals(saveButton)) {
 			// Make async call to save the document (also updates UI).
 			int currentTabInd = tb.getSelectedTab();
 			LockedDocument lockedDoc = new LockedDocument(null, null,
 					tabKeys.get(currentTabInd), tabTitles.get(currentTabInd).getValue(), 
 					tabContents.get(currentTabInd).getHTML());
 			saver.saveDocument(lockedDoc);
 		} else if (source.equals(closeButton)) {
 			// Release locks according to state.
 			int currentTabInd = tb.getSelectedTab();
 			UiState state = uiStates.get(currentTabInd);
 			if (state == UiState.LOCKED || state == UiState.LOCKING) {
 				LockedDocument lockedDoc = new LockedDocument(null, null,
 						tabKeys.get(currentTabInd),
 						tabTitles.get(currentTabInd).getValue(),
 						tabContents.get(currentTabInd).getHTML());
 				releaser.releaseLock(lockedDoc);
 			} else if (state == UiState.REQUESTING) {
 				unrequestor.unrequestDocument(tabKeys.get(currentTabInd));
 			}
 			// Update UI and corresponding variables.
 			removeTabAtInd(currentTabInd);
 		} else if (source.equals(simulateButton)) {
 			simulating = !simulating;
 			if (simulating) {
 				statusUpdate("Simulating...");
 				setUiState(UiState.SIMULATING);
 				// Disable additional UI elements like the doc list, etc.
 				documentList.setEnabled(false);
 				for (int i = 0; i < tb.getTabCount(); i++) {
 					tb.setTabEnabled(i, false);
 				}
 
 				simulateThinking();
 			} else {
 				statusUpdate("Stopped simulating.");
 				int currentTabInd = tb.getSelectedTab();
 				setUiState(uiStates.get(currentTabInd));
 				// Enable UI elements.
 				documentList.setEnabled(true);
 				for (int i = 0; i < tb.getTabCount(); i++) {
 					tb.setTabEnabled(i, true);
 				}
 				
 				thinkingTimer.cancel();
 				eatingTimer.cancel();
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * Intercepts events from the list box.
 	 * @see com.google.gwt.event.dom.client.ChangeHandler#onChange(com.google.gwt.event.dom.client.ChangeEvent)
 	 */
 	@Override
 	public void onChange(ChangeEvent event) {
 		if (event.getSource().equals(documentList)) {
 			String key = documentList.getValue(documentList.getSelectedIndex());
 			loadDoc(key);
 		}
 	}
 
 	protected void loadDoc(String key) {
 		// If it's already open in a tab, save the location of the tab
 		// and retrieve the title and contents from that one.
 		int savedLoc = tabKeys.indexOf(key);
 		if (savedLoc != -1) {
 			// Select the appropriate tab; this should fire the SelectionHandler.
 			tp.selectTab(savedLoc);
 		} else {
 			addNewTab(key);
 			reader.getDocument(key);
 		}
 	}
 
 	protected void setUpChannel() {
 		// Establish the channel handlers with our given channel token.
 		ChannelFactory.createChannel(channelToken, new ChannelCreatedCallback() {
 			@Override
 			public void onChannelCreated(Channel channel) {
 				channel.open(new SocketListener() {
 					@Override
 					public void onOpen() {
 						statusUpdate("Channel successfully opened!");
 					}
 					@Override
 					public void onMessage(String message) {
 						char messageType = message.charAt(0);
 						if (messageType == Messages.CODE_LOCK_READY) {
 							// Doc is ready to be locked. The rest of the string is doc ID.
 							String docKey = message.substring(1).replaceAll("\\s", "");
 							if (tabIsSelected() && tabKeys.contains(docKey)) {
 								locker.lockDocument(docKey);
 							}
 						} else if (messageType == Messages.CODE_LOCK_NOT_READY) {
 							// Doc is not ready to be locked. The rest of the string is
 							// the number of people in front of us in the queue.
 							String restOfString = message.substring(1);
 							int delimiter = restOfString.indexOf(
 									CollaboratorServiceImpl.DELIMITER);
 							int numPeopleLeft = Integer.parseInt(
 									restOfString.substring(0, delimiter));
 							String docId = restOfString.substring(delimiter + 1);
 							docId = docId.replaceAll("\\s", "");
 							statusUpdate("Update: " + numPeopleLeft + " people are now" +
 									" ahead of you for document " + docId + ".");
 							int indOfDoc = tabKeys.indexOf(docId);
 							if (tabIsSelected() && indOfDoc != -1) {
 								tabQueueLengths.set(indOfDoc, numPeopleLeft);
 								queueStatus.setHTML("<br />Position " +
 										numPeopleLeft + " in line");
 							}
 						} else if (messageType == Messages.CODE_LOCK_EXPIRED) {
 							statusUpdate("Timeout occurred: document lock released.");
 							String docKey = message.substring(1).replaceAll("\\s", "");
 							updateVarsAndUi(docKey, UiState.VIEWING);
 						}
 					}
 					@Override
 					public void onError(SocketError error) {
 						statusUpdate("Channel error:" + error.getDescription());
 					}
 					@Override
 					public void onClose() {
 						statusUpdate("Channel closed!");
 					}
 				});
 			}
 		});
 	}
 
 	/**
 	 * Updates relevant state-capturing variables and updates UI
 	 * if necessary.
 	 */
 	protected void updateVarsAndUi(String key, String title, 
 			String contents, UiState state) {
 		// Update local data structures.
 		int indResult = tabKeys.indexOf(key);
 		if (indResult == -1) {
 			return;
 		}
 		tabTitles.get(indResult).setValue(title);
 		tabContents.get(indResult).setHTML(contents);
 		uiStates.set(indResult, state);
 		int currentTabInd = tb.getSelectedTab();
 		if (key.equals(tabKeys.get(currentTabInd))) {
 			setUiStateIfNoSim(state);
 		}
 	}
 
 	/**
 	 * Just update the state corresponding to the key.
 	 */
 	protected void updateVarsAndUi(String key, UiState state) {
 		// Update local data structures.
 		int indResult = tabKeys.indexOf(key);
 		if (indResult == -1) {
 			return;
 		}
 		uiStates.set(indResult, state);
 		int currentTabInd = tb.getSelectedTab();
 		if (key.equals(tabKeys.get(currentTabInd))) {
 			setUiStateIfNoSim(state);
 		}
 	}
 
 	/**
 	 * Sets the UI state if the program is not in a simulating state.
 	 * 
 	 * @param state the UI state to switch to (as defined in UiState.java)
 	 */
 	protected void setUiStateIfNoSim(UiState state) {
 		if (simulating) {
 			return;
 		}
 		setUiState(state);
 	}
 
 	/**
 	 * Resets the state of the buttons and edit objects to the specified state.
 	 * The state of these objects is modified by requesting or obtaining locks
 	 * and trying to or successfully saving.
 	 * 
 	 * @param state the UI state to switch to (as defined in UiState.java)
 	 */
 	protected void setUiState(UiState state) {
 		refreshDoc.setEnabled(state.refreshDocEnabled);
 		lockButtonUnlocked.setEnabled(state.lockButtonUnlockedEnabled);
 		lockButtonLocked.setEnabled(state.lockButtonLockedEnabled);
 		saveButton.setEnabled(state.saveButtonEnabled);
 		closeButton.setEnabled(state.closeButtonEnabled);
 		simulateButton.setEnabled(state.simulateButtonEnabled);
 		if (tabIsSelected()) {
 			int currentTabInd = tb.getSelectedTab();
 			tabTitles.get(currentTabInd).setEnabled(state.titleEnabled);
 			tabContents.get(currentTabInd).setEnabled(state.contentsEnabled);
 		}
 
 		// Handle UI changes for the queue status panel.
 		String statusString = "";
 		if (state.lockState == UiState.LockButton.LOCKED) {
 			statusString = "<br />Lock obtained";
 			hp.remove(lockButtonUnlocked);
 			hp.remove(lockButtonRequesting);
 			hp.insert(lockButtonLocked, 1);
 		} else if (state.lockState == UiState.LockButton.UNLOCKED) {
 			statusString = "<br />No lock";
 			hp.remove(lockButtonLocked);
 			hp.remove(lockButtonRequesting);
 			hp.insert(lockButtonUnlocked, 1);
 		} else if (state.lockState == UiState.LockButton.REQUESTING) {
 			int currentTabInd = tb.getSelectedTab();
 			int numPeopleLeft = tabQueueLengths.get(currentTabInd);
 			if (currentTabInd != -1 && numPeopleLeft != -1) {
 				statusString = "<br />Position " + numPeopleLeft + " in line";
 			}
 			hp.remove(lockButtonLocked);
 			hp.remove(lockButtonUnlocked);
 			hp.insert(lockButtonRequesting, 1);
 		}
 		queueStatus.setHTML(statusString);
 	}
 
 	/**
 	 * Adds status lines to the console window to enable transparency of the
 	 * underlying processes.
 	 * 
 	 * @param status the status to add to the console window
 	 */
 	protected void statusUpdate(String status) {
 		while (statusArea.getWidgetCount() > 6) {
 			statusArea.remove(1);
 		}
 		final HTML statusUpd = new HTML(status);
 		statusArea.add(statusUpd);
 	}
 
 	protected void simulateThinking() {
 		int sleepRange = THINKING_RANGE_END - THINKING_RANGE_START + 1;
 		int waitingTime = 
				(int) (THINKING_RANGE_START + sleepRange * Math.random()) + 1;
 		thinkingTimer.schedule(waitingTime);
 	}
 
 	protected void simulateEating() {
 		// Write token into the document.
 		int currentInd = tb.getSelectedTab();
 		tabContents.get(currentInd).setHTML(
 				tabContents.get(currentInd).getHTML() + channelToken + "<br />");
 		
 		int eatRange = EATING_RANGE_END - EATING_RANGE_START + 1;
		int waitingTime = (int) (EATING_RANGE_START + eatRange * Math.random()) + 1;
 		eatingTimer.schedule(waitingTime);
 	}
 	
 	protected boolean tabIsSelected() {
 		int currentTabInd = tb.getSelectedTab();
 		return currentTabInd >= 0 && currentTabInd < tabKeys.size();
 	}
 
 	protected void addNewTab(String key) {
 		TextBox title = new TextBox();
 		RichTextArea contents = new RichTextArea();
 
 		// Update local variables.
 		tabKeys.add(key);
 		tabTitles.add(title);
 		tabContents.add(contents);
 		tabQueueLengths.add(-1);
 		uiStates.add(UiState.VIEWING);
 		setUiStateIfNoSim(UiState.VIEWING);
 
 		// Update TabPanel's UI:
 		HorizontalPanel tabHeader = new HorizontalPanel();
 		tabHeader.add(title);
 		tp.add(contents, tabHeader);
 		// Select the last tab for the user.
 		tp.selectTab(tb.getTabCount() - 1);
 	}
 
 	private void removeTabAtInd(int i) {
 		// Update local data structures
 		tabKeys.remove(i);
 		tabTitles.remove(i);
 		tabContents.remove(i);
 		tabQueueLengths.remove(i);
 		uiStates.remove(i);
 
 		// Update tab panel
 		tp.remove(i);
 		int tabCount = tb.getTabCount();
 		if (tabCount > 0) {
 			if (i > tabCount - 1) {
 				tp.selectTab(tabCount - 1);
 			} else {
 				tb.selectTab(i);
 			}
 		} else {
 			setUiStateIfNoSim(UiState.NOT_VIEWING);
 		}
 	}
 }
