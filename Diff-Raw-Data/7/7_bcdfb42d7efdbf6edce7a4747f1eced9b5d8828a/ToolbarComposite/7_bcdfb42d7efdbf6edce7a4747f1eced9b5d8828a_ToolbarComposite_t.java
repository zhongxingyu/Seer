 package collector.desktop.view.composites;
 
 import java.io.InputStream;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import collector.desktop.controller.GuiController;
 import collector.desktop.controller.events.EventObservable;
 import collector.desktop.controller.events.Observer;
 import collector.desktop.controller.events.SammelboxEvent;
 import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
 import collector.desktop.model.database.exceptions.ExceptionHelper;
 import collector.desktop.model.database.operations.DatabaseOperations;
 import collector.desktop.view.ApplicationUI;
 import collector.desktop.view.browser.BrowserFacade;
 import collector.desktop.view.internationalization.DictKeys;
 import collector.desktop.view.internationalization.Translator;
 import collector.desktop.view.sidepanes.AddAlbumItemSidepane;
 import collector.desktop.view.sidepanes.AdvancedSearchSidepane;
 import collector.desktop.view.sidepanes.CreateAlbumSidepane;
 import collector.desktop.view.sidepanes.EmptySidepane;
 import collector.desktop.view.sidepanes.SynchronizeSidepane;
 import collector.desktop.view.various.ComponentFactory;
 import collector.desktop.view.various.PanelType;
 
 public class ToolbarComposite extends Composite implements Observer {
 	private final static Logger LOGGER = LoggerFactory.getLogger(ToolbarComposite.class);
 	
 	private static Composite toolbarComposite = null;
 	private static Image home = null, addAlbum = null, addEntry = null,
 			detailedView = null, pictureView = null, search = null,
 			sync = null, help = null;
 	private static Image homeActive = null, addAlbumActive = null,
 			addEntryActive = null, searchActive = null, syncActive = null,
 			helpActive = null;
 	private static Button homeBtn = null, addAlbumBtn = null, addEntryBtn = null,
 			viewBtn = null, searchBtn = null, syncBtn = null, helpBtn = null;
 	private static PanelType lastSelectedPanelType = PanelType.Empty;
 
 	private static void disableActiveButtons() {
 		homeBtn.setImage(home);
 		addAlbumBtn.setImage(addAlbum);
 		addEntryBtn.setImage(addEntry);
 		viewBtn.setImage(detailedView);
 		searchBtn.setImage(search);
 		syncBtn.setImage(sync);
 		helpBtn.setImage(help);
 	}
 
 	public ToolbarComposite(final Composite parentComposite) {
 		super(parentComposite, SWT.NONE);
 		toolbarComposite = new Composite(parentComposite, SWT.NONE);
 
 		EventObservable.registerObserver(this);
 		
 		GridLayout gridLayout = new GridLayout(1, false);
 		gridLayout.marginHeight = 0;
 		gridLayout.marginWidth = 0;
 
 		toolbarComposite.setLayout(gridLayout);
 
 		Composite innerComposite = new Composite(toolbarComposite, SWT.NONE);
 
 		gridLayout = new GridLayout(7, false);
 		gridLayout.marginHeight = 0;
 		gridLayout.marginWidth = 0;
 
 		innerComposite.setLayout(gridLayout);
 
 		InputStream istream = this.getClass().getClassLoader()
 				.getResourceAsStream("graphics/home.png");
 		home = new Image(Display.getCurrent(), istream);
 		istream = this.getClass().getClassLoader()
 				.getResourceAsStream("graphics/home-active.png");
 		homeActive = new Image(Display.getCurrent(), istream);
 
 		istream = this.getClass().getClassLoader()
 				.getResourceAsStream("graphics/add.png");
 		addAlbum = new Image(Display.getCurrent(), istream);
 		istream = this.getClass().getClassLoader()
 				.getResourceAsStream("graphics/add-active.png");
 		addAlbumActive = new Image(Display.getCurrent(), istream);
 
 		istream = this.getClass().getClassLoader()
 				.getResourceAsStream("graphics/additem.png");
 		addEntry = new Image(Display.getCurrent(), istream);
 		istream = this.getClass().getClassLoader()
 				.getResourceAsStream("graphics/additem-active.png");
 		addEntryActive = new Image(Display.getCurrent(), istream);
 
 		istream = this.getClass().getClassLoader()
 				.getResourceAsStream("graphics/detailedview.png");
 		detailedView = new Image(Display.getCurrent(), istream);
 		istream = this.getClass().getClassLoader()
 				.getResourceAsStream("graphics/pictureview.png");
 		pictureView = new Image(Display.getCurrent(), istream);
 
 		istream = this.getClass().getClassLoader()
 				.getResourceAsStream("graphics/search.png");
 		search = new Image(Display.getCurrent(), istream);
 		istream = this.getClass().getClassLoader()
 				.getResourceAsStream("graphics/search-active.png");
 		searchActive = new Image(Display.getCurrent(), istream);
 
 		istream = this.getClass().getClassLoader()
 				.getResourceAsStream("graphics/sync.png");
 		sync = new Image(Display.getCurrent(), istream);
 		istream = this.getClass().getClassLoader()
 				.getResourceAsStream("graphics/sync-active.png");
 		syncActive = new Image(Display.getCurrent(), istream);
 
 		istream = this.getClass().getClassLoader()
 				.getResourceAsStream("graphics/help.png");
 		help = new Image(Display.getCurrent(), istream);
 		istream = this.getClass().getClassLoader()
 				.getResourceAsStream("graphics/help-active.png");
 		helpActive = new Image(Display.getCurrent(), istream);
 
 		homeBtn = new Button(innerComposite, SWT.PUSH);
 		homeBtn.setImage(homeActive);
 		homeBtn.setText(Translator.get(DictKeys.BUTTON_HOME));
 		homeBtn.setToolTipText(Translator.get(DictKeys.BUTTON_TOOLTIP_HOME));
 
 		addAlbumBtn = new Button(innerComposite, SWT.PUSH);
 		addAlbumBtn.setImage(addAlbum);
 		addAlbumBtn.setText(Translator.get(DictKeys.BUTTON_ADD_ALBUM));
 		addAlbumBtn.setToolTipText(Translator.get(DictKeys.BUTTON_TOOLTIP_ADD_ALBUM));
 
 		addEntryBtn = new Button(innerComposite, SWT.PUSH);
 		addEntryBtn.setImage(addEntry);
 		addEntryBtn.setText(Translator.get(DictKeys.BUTTON_ADD_ENTRY));
 		addEntryBtn.setToolTipText(Translator.get(DictKeys.BUTTON_TOOLTIP_ADD_ENTRY));
 		addEntryBtn.setEnabled(false);
 
 		viewBtn = new Button(innerComposite, SWT.PUSH);
 		viewBtn.setImage(detailedView);
 		viewBtn.setText(Translator.get(DictKeys.BUTTON_TOGGLE));
 		viewBtn.setToolTipText(Translator.get(DictKeys.BUTTON_TOOLTIP_TOGGLE_TO_GALLERY));
 		viewBtn.setEnabled(false);
 
 		searchBtn = new Button(innerComposite, SWT.PUSH);
 		searchBtn.setImage(search);
 		searchBtn.setText(Translator.get(DictKeys.BUTTON_SEARCH));
 		searchBtn.setToolTipText(Translator.get(DictKeys.BUTTON_TOOLTIP_SEARCH));
 		searchBtn.setEnabled(false);
 
 		syncBtn = new Button(innerComposite, SWT.PUSH);
 		syncBtn.setImage(sync);
 		syncBtn.setText(Translator.get(DictKeys.BUTTON_SYNCHRONIZE));
 		syncBtn.setToolTipText(Translator.get(DictKeys.BUTTON_TOOLTIP_SYNCHRONIZE));
 
 		helpBtn = new Button(innerComposite, SWT.PUSH);
 		helpBtn.setImage(help);
 		helpBtn.setText(Translator.get(DictKeys.BUTTON_HELP));
 		helpBtn.setToolTipText(Translator.get(DictKeys.BUTTON_TOOLTIP_HELP));
 
 		// ---------- Add Mouse Listeners ----------
 
 		homeBtn.addMouseListener(new MouseListener() {
 			@Override
 			public void mouseUp(MouseEvent arg0) {
 				disableActiveButtons();
 
 				ApplicationUI.changeRightCompositeTo(PanelType.Empty, EmptySidepane.build(ApplicationUI.getThreePanelComposite()));
 				
 				lastSelectedPanelType = PanelType.Empty;
 				
 				homeBtn.setImage(homeActive);
 
 				BrowserFacade.loadWelcomePage();
 
 				addEntryBtn.setEnabled(false);
 
 				viewBtn.setEnabled(false);
 				viewBtn.setImage(detailedView);
 				viewBtn.setToolTipText(Translator.get(DictKeys.BUTTON_TOOLTIP_TOGGLE_TO_GALLERY));
 
 				searchBtn.setEnabled(false);
 				
 				ApplicationUI.setSelectedAlbum("");
 			}
 
 			@Override
 			public void mouseDown(MouseEvent arg0) {
 			}
 
 			@Override
 			public void mouseDoubleClick(MouseEvent arg0) {
 			}
 		});
 
 		addAlbumBtn.addMouseListener(new MouseListener() {
 			@Override
 			public void mouseUp(MouseEvent arg0) {
 				if (ApplicationUI.getCurrentRightPanelType() != PanelType.AddAlbum) {
 					ApplicationUI.changeRightCompositeTo(PanelType.AddAlbum,
 							CreateAlbumSidepane
 									.build(ApplicationUI
 											.getThreePanelComposite()));
 					StatusBarComposite.getInstance(parentComposite.getShell())
 							.writeStatus(Translator.get(DictKeys.STATUSBAR_ADD_ALBUM_OPENED));
 
 					disableActiveButtons();
 					addAlbumBtn.setImage(addAlbumActive);
 					ApplicationUI.setCurrentRightPanelType(PanelType.AddAlbum);
 					lastSelectedPanelType = PanelType.AddAlbum;
 				} else {
 					ApplicationUI.changeRightCompositeTo(PanelType.Empty,
 							EmptySidepane.build(ApplicationUI
 									.getThreePanelComposite()));
 					StatusBarComposite.getInstance(parentComposite.getShell())
 							.writeStatus(Translator.get(DictKeys.STATUSBAR_PROGRAM_STARTED));
 
 					disableActiveButtons();
 					ApplicationUI.setCurrentRightPanelType(PanelType.Empty);
 				}
 			}
 
 			@Override
 			public void mouseDown(MouseEvent arg0) {
 			}
 
 			@Override
 			public void mouseDoubleClick(MouseEvent arg0) {
 			}
 		});
 
 		addEntryBtn.addMouseListener(new MouseListener() {
 			@Override
 			public void mouseUp(MouseEvent arg0) {
 				BrowserFacade.rerunLastQuery();
 				
 				if (!ApplicationUI.hasSelectedAlbum()) {
 					ComponentFactory.showErrorDialog(
 							ApplicationUI.getShell(), 
 							Translator.get(DictKeys.DIALOG_TITLE_NO_ALBUM_SELECTED), 
 							Translator.get(DictKeys.DIALOG_CONTENT_NO_ALBUM_SELECTED));
 					
 					return;
 				}
 				if (ApplicationUI.getCurrentRightPanelType() != PanelType.AddEntry) {
 					ApplicationUI.changeRightCompositeTo(PanelType.AddAlbum,
 							AddAlbumItemSidepane.build(
 									ApplicationUI.getThreePanelComposite(),
 									ApplicationUI.getSelectedAlbum()));
 					StatusBarComposite
 							.getInstance(parentComposite.getShell())
 							.writeStatus(Translator.get(DictKeys.STATUSBAR_ADD_ITEM_OPENED));
 
 					disableActiveButtons();
 					addEntryBtn.setImage(addEntryActive);
 					ApplicationUI.setCurrentRightPanelType(PanelType.AddEntry);
 					lastSelectedPanelType = PanelType.AddEntry;
 				} else {
 					ApplicationUI.changeRightCompositeTo(PanelType.Empty,
 							EmptySidepane.build(ApplicationUI
 									.getThreePanelComposite()));
 					StatusBarComposite.getInstance(parentComposite.getShell())
 							.writeStatus(Translator.get(DictKeys.STATUSBAR_PROGRAM_STARTED));
 
 					disableActiveButtons();
 					ApplicationUI.setCurrentRightPanelType(PanelType.Empty);
 				}
 			}
 
 			@Override
 			public void mouseDown(MouseEvent arg0) {
 			}
 
 			@Override
 			public void mouseDoubleClick(MouseEvent arg0) {
 			}
 		});
 
 		viewBtn.addMouseListener(new MouseListener() {
 			@Override
 			public void mouseUp(MouseEvent arg0) {
 				if (GuiController.getGuiState().isViewDetailed()) {
 					viewBtn.setImage(pictureView);
					viewBtn.setToolTipText(Translator.get(DictKeys.BUTTON_TOOLTIP_TOGGLE_TO_DETAILS));
					GuiController.getGuiState().setViewDetailed(false);
 				} else {
 					viewBtn.setImage(detailedView);
					viewBtn.setToolTipText(Translator.get(DictKeys.BUTTON_TOOLTIP_TOGGLE_TO_GALLERY));
 					GuiController.getGuiState().setViewDetailed(true);
 				}
 				
 				BrowserFacade.rerunLastQuery();
 			}
 
 			@Override
 			public void mouseDown(MouseEvent arg0) {
 			}
 
 			@Override
 			public void mouseDoubleClick(MouseEvent arg0) {
 			}
 		});
 
 		searchBtn.addMouseListener(new MouseListener() {
 			@Override
 			public void mouseUp(MouseEvent arg0) {
 				BrowserFacade.rerunLastQuery();
 				
 				if (!ApplicationUI.hasSelectedAlbum()) {
 					ComponentFactory.showErrorDialog(
 							ApplicationUI.getShell(), 
 							Translator.get(DictKeys.DIALOG_TITLE_NO_ALBUM_SELECTED), 
 							Translator.get(DictKeys.DIALOG_CONTENT_NO_ALBUM_SELECTED));
 					return;
 				}
 				if (ApplicationUI.getCurrentRightPanelType() != PanelType.AdvancedSearch) {
 					ApplicationUI.changeRightCompositeTo(PanelType.AdvancedSearch,
 							AdvancedSearchSidepane.build(
 									ApplicationUI.getThreePanelComposite(),
 									ApplicationUI.getSelectedAlbum()));
 					StatusBarComposite
 							.getInstance(parentComposite.getShell())
 							.writeStatus(Translator.get(DictKeys.STATUSBAR_SEARCH_OPENED));
 
 					disableActiveButtons();
 					searchBtn.setImage(searchActive);
 					ApplicationUI
 							.setCurrentRightPanelType(PanelType.AdvancedSearch);
 					lastSelectedPanelType = PanelType.AdvancedSearch;
 				} else {
 					ApplicationUI.changeRightCompositeTo(PanelType.Empty,
 							EmptySidepane.build(ApplicationUI
 									.getThreePanelComposite()));
 					StatusBarComposite.getInstance(parentComposite.getShell())
 							.writeStatus(Translator.get(DictKeys.STATUSBAR_PROGRAM_STARTED));
 
 					disableActiveButtons();
 					ApplicationUI.setCurrentRightPanelType(PanelType.Empty);
 				}
 			}
 
 			@Override
 			public void mouseDown(MouseEvent arg0) {
 			}
 
 			@Override
 			public void mouseDoubleClick(MouseEvent arg0) {
 			}
 		});
 
 		syncBtn.addMouseListener(new MouseListener() {
 			@Override
 			public void mouseUp(MouseEvent arg0) {
 				BrowserFacade.rerunLastQuery();				
 				
 				if (ApplicationUI.getCurrentRightPanelType() != PanelType.Synchronization) {
 					ApplicationUI.changeRightCompositeTo(PanelType.Synchronization,
 							SynchronizeSidepane.build(ApplicationUI
 									.getThreePanelComposite()));
 					StatusBarComposite
 							.getInstance(parentComposite.getShell())
 							.writeStatus(Translator.get(DictKeys.STATUSBAR_SYNCHRONIZE_OPENED));
 
 					disableActiveButtons();
 					syncBtn.setImage(syncActive);
 					ApplicationUI
 							.setCurrentRightPanelType(PanelType.Synchronization);
 					lastSelectedPanelType = PanelType.Synchronization;
 				} else {
 					ApplicationUI.changeRightCompositeTo(PanelType.Empty,
 							EmptySidepane.build(ApplicationUI
 									.getThreePanelComposite()));
 					StatusBarComposite.getInstance(parentComposite.getShell())
 							.writeStatus(Translator.get(DictKeys.STATUSBAR_PROGRAM_STARTED));
 
 					disableActiveButtons();
 					ApplicationUI.setCurrentRightPanelType(PanelType.Empty);
 				}
 			}
 
 			@Override
 			public void mouseDown(MouseEvent arg0) {
 			}
 
 			@Override
 			public void mouseDoubleClick(MouseEvent arg0) {
 			}
 		});
 
 		helpBtn.addMouseListener(new MouseListener() {
 			@Override
 			public void mouseUp(MouseEvent arg0) {
 				if (ApplicationUI.getCurrentRightPanelType() != PanelType.Help) {
 					BrowserFacade.loadHelpPage();
 					ApplicationUI.changeRightCompositeTo(PanelType.Help,
 							EmptySidepane.build(ApplicationUI
 									.getThreePanelComposite()));
 					StatusBarComposite.getInstance(parentComposite.getShell())
 							.writeStatus(Translator.get(DictKeys.STATUSBAR_HELP_OPENED));
 
 					disableActiveButtons();
 					helpBtn.setImage(helpActive);
 					ApplicationUI.setCurrentRightPanelType(PanelType.Empty);
 					lastSelectedPanelType = PanelType.Empty;
 				} else {
 					ApplicationUI.changeRightCompositeTo(PanelType.Empty,
 							EmptySidepane.build(ApplicationUI
 									.getThreePanelComposite()));
 					StatusBarComposite.getInstance(parentComposite.getShell())
 							.writeStatus(Translator.get(DictKeys.STATUSBAR_PROGRAM_STARTED));
 
 					disableActiveButtons();
 					ApplicationUI.setCurrentRightPanelType(PanelType.Empty);
 				}
 			}
 
 			@Override
 			public void mouseDown(MouseEvent arg0) {
 			}
 
 			@Override
 			public void mouseDoubleClick(MouseEvent arg0) {
 			}
 		});
 
 		GridData seperatorGridData = new GridData(GridData.FILL_BOTH);
 		seperatorGridData.minimumHeight = 0;
 
 		// separator
 		new Label(toolbarComposite, SWT.SEPARATOR | SWT.HORIZONTAL)
 				.setLayoutData(seperatorGridData);
 	}
 	
 	@Override
 	public void update(SammelboxEvent event) {
 		if (event.equals(SammelboxEvent.RIGHT_SIDEPANE_CHANGED)) {
 			if (lastSelectedPanelType != ApplicationUI.getCurrentRightPanelType()) {
 				disableActiveButtons();
 			}
 		}
 	}
 
 	public static void enableAlbumButtons(String albumName) {
 		homeBtn.setImage(home);
 		addEntryBtn.setEnabled(true);
 
 		try {
 			if (DatabaseOperations.isPictureAlbum(albumName)) {
 				viewBtn.setImage(pictureView);
 				viewBtn.setToolTipText(Translator.get(DictKeys.BUTTON_TOOLTIP_TOGGLE_TO_GALLERY));
 				viewBtn.setEnabled(true);
 			} else {
 				viewBtn.setImage(detailedView);
 				viewBtn.setEnabled(false);
 			}
 		} catch (DatabaseWrapperOperationException ex) {
 			LOGGER.error("An error occured while checking whether the following album contains pictures: '" + albumName + "'" + 
 					" \n Stacktrace:" + ExceptionHelper.toString(ex));
 		}
 
 		GuiController.getGuiState().setViewDetailed(true);
 		BrowserFacade.rerunLastQuery();
 		searchBtn.setEnabled(true);
 	}
 }
