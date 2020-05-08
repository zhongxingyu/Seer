 package com.partyrock.gui;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.*;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.*;
 
 import com.partyrock.LightMaster;
 import com.partyrock.anim.ElementAnimation;
 import com.partyrock.element.ElementController;
 import com.partyrock.gui.elements.ElementDisplay;
 import com.partyrock.gui.elements.ElementTableRenderer;
 import com.partyrock.gui.elements.ElementUpdater;
 import com.partyrock.gui.elements.ElementsEditor;
 import com.partyrock.gui.uc.UCEditor;
 import com.partyrock.tools.PartyToolkit;
 import org.eclipse.swt.layout.FillLayout;
 
 /**
  * The main GUI window.
  * @author Matthew
  * 
  */
 public class LightWindow implements ElementTableRenderer, ElementDisplay {
 	private LightMaster master;
 	private LightWindowManager windowManager;
 	private Shell shlPartyMaster;
 	private ToolBar toolbar;
 	private ScrolledComposite tableScroll;
 	private Table table;
 	private ElementsEditor elementsEditor;
 	private UCEditor ucEditor;
 	private LightElementSimulator simulator;
 	private LightWindowElementsTableRenderer tableRenderer;
 
 	public LightWindow(final LightMaster master, LightWindowManager manager) {
 		this.master = master;
 		this.windowManager = manager;
 
 		tableRenderer = new LightWindowElementsTableRenderer(master, this);
 
 		this.windowManager.addElementDisplay(this);
 
 		// Construct the GUI shell
 		this.shlPartyMaster = new Shell(manager.getDisplay());
 		shlPartyMaster.setSize(new Point(900, 600));
 		shlPartyMaster.addListener(SWT.Close, new Listener() {
 			public void handleEvent(Event event) {
 				master.onDispose();
 			}
 		});
 		shlPartyMaster.setText("Party Master");
 
 		// Layout with 1 column, and no equal width columns
 		shlPartyMaster.setLayout(new GridLayout(1, false));
 
 		// Create Toolbar
 		toolbar = new ToolBar(shlPartyMaster, SWT.HORIZONTAL);
 
 		ToolItem tltmSimulator = new ToolItem(toolbar, SWT.NONE);
 		tltmSimulator.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				showSimulator();
 			}
 		});
 		tltmSimulator.setText("Simulator");
 
 		@SuppressWarnings("unused")
         ToolItem toolItem = new ToolItem(toolbar, SWT.SEPARATOR);
 
 		// Generate Toolbar items
 		ToolItem elementsEditorButton = new ToolItem(toolbar, SWT.PUSH);
 		elementsEditorButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				showElementsEditor();
 			}
 		});
 		elementsEditorButton.setText("Elements Editor");
 
 		ToolItem ucEditorButton = new ToolItem(toolbar, SWT.PUSH);
 		ucEditorButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				showUCEditor();
 			}
 		});
 		ucEditorButton.setText("C Editor");
 
 		// Finish Toolbar init
 		toolbar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		toolbar.pack();
 
 		// Main table scroll container
 		tableScroll = new ScrolledComposite(shlPartyMaster, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
 		tableScroll.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		tableScroll.setExpandHorizontal(true);
 		tableScroll.setExpandVertical(true);
 
 		// Actually make the table
 		table = new Table(tableScroll, SWT.FULL_SELECTION | SWT.MULTI);
 		table.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyReleased(KeyEvent e) {
 				tableKeyReleased(e);
 			}
 		});
 		table.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseDown(MouseEvent e) {
 				tableClick(e);
 			}
 		});
 		table.setLinesVisible(false);
 		table.setToolTipText("");
 
 		// Do the custom rendering stuff for the table
 		tableRenderer.addCustomListeners(table);
 
 		// Set the scroll contianer's content
 		tableScroll.setContent(table);
 
 		TableColumn tblclmnColumn = new TableColumn(table, SWT.NONE);
 		tblclmnColumn.setResizable(false);
 		tblclmnColumn.setWidth(100);
 		tblclmnColumn.setText("Column 1");
 
 		TableColumn tblclmnColumn_1 = new TableColumn(table, SWT.NONE);
 		tblclmnColumn_1.setWidth(100);
 		tblclmnColumn_1.setText("Column 2");
 
 		// Load in TableItems
 		ElementUpdater.updateElements(this, table);
 
 		Menu menu = new Menu(shlPartyMaster, SWT.BAR);
 		shlPartyMaster.setMenuBar(menu);
 
 		MenuItem mntmFile = new MenuItem(menu, SWT.CASCADE);
 		mntmFile.setText("File");
 
 		Menu menu_1 = new Menu(mntmFile);
 		mntmFile.setMenu(menu_1);
 
 		MenuItem mntmSaveLocation = new MenuItem(menu_1, SWT.NONE);
 		mntmSaveLocation.setAccelerator(SWT.MOD1 + 'S');
 		mntmSaveLocation.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				saveLocationFile();
 			}
 		});
 		mntmSaveLocation.setText("Save Elements");
 
 		MenuItem mntmSaveLocationAs = new MenuItem(menu_1, SWT.NONE);
 		mntmSaveLocationAs.setAccelerator(SWT.MOD1 + SWT.SHIFT + 'S');
 		mntmSaveLocationAs.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				saveLocationFileAs();
 			}
 		});
 		mntmSaveLocationAs.setText("Save Elements As");
 
 		new MenuItem(menu_1, SWT.SEPARATOR);
 
 		MenuItem mntmLoadLocation = new MenuItem(menu_1, SWT.NONE);
 		mntmLoadLocation.setAccelerator(SWT.MOD1 + 'O');
 		mntmLoadLocation.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				loadLocation();
 			}
 		});
 		mntmLoadLocation.setText("Load Location");
 
 		Canvas timeline = new Canvas(shlPartyMaster, SWT.NONE);
 		timeline.setSize(new Point(0, 30));
 		timeline.setLayout(null);
 		GridData gd_timeline = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
 		gd_timeline.widthHint = 477;
 		gd_timeline.heightHint = 50;
 		timeline.setLayoutData(gd_timeline);
		
		Label lblPartyMaster = new Label(shlPartyMaster, SWT.NONE);
		lblPartyMaster.setAlignment(SWT.CENTER);
		lblPartyMaster.setOrientation(SWT.RIGHT_TO_LEFT);
		lblPartyMaster.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblPartyMaster.setText("Party Master - Developed for Party Rock Illinois @ University of Illinois Urbana-Champaign - Engineering Open House 2013");
 		timeline.addPaintListener(new PaintListener() {
 			public void paintControl(PaintEvent e) {
 				GC gc = e.gc;
 
 				System.out.println("paint");
 
 				gc.setBackground(shlPartyMaster.getDisplay().getSystemColor(SWT.COLOR_BLACK));
 				gc.setForeground(shlPartyMaster.getDisplay().getSystemColor(SWT.COLOR_RED));
 				gc.fillGradientRectangle(e.x, e.y, e.width, e.height, false);
 
 			}
 		});
 
 		shlPartyMaster.open();
 	}
 
 	/**
 	 * Performs the GUI loop. This method will not return until the GUI closes.
 	 */
 	public void loop() {
 		while (!shlPartyMaster.isDisposed()) {
 			if (!windowManager.getDisplay().readAndDispatch()) {
 				windowManager.getDisplay().sleep();
 			}
 		}
 
 		windowManager.getDisplay().dispose();
 	}
 
 	/**
 	 * Adds a specific element to the end of the table, and sets the element as
 	 * the item's data.
 	 * @param element the element to add
 	 */
 	public void addElementAsRow(ElementController element) {
 		TableItem item = new TableItem(table, SWT.NONE);
 		item.setData(element);
 
 		// This is awkwardly necessary here
 		ElementUpdater.packTable(table);
 	}
 
 	/**
 	 * Saves the location file, or pops open the dialog if it doesn't exist
 	 */
 	public void saveLocationFile() {
 		if (master.getLocationManager().getLocation() == null) {
 			// We don't have a location right now, so make a new one
 			this.saveLocationFileAs();
 		} else {
 			// The file exists, just save it
 			master.getLocationManager().saveLocationFile();
 		}
 	}
 
 	/**
 	 * Saves a new location file (pops up the file prompt)
 	 */
 	public void saveLocationFileAs() {
 		File f = getLocationFileFromDialog(SWT.SAVE);
 
 		if (f.exists()) {
 			boolean override = PartyToolkit.openConfirm(shlPartyMaster, "Are you sure you wish to override the existing location file "
 					+ f.getName() + "?", "Overwrite?");
 			if (override) {
 				// If we don't delete, PersistentSettings automatically merges
 				// the files which would be very bad (some of the old elements
 				// might hang around, but only some, and it would depend on the
 				// number of new elements. Basically, it would be very bad.)
 
 				f.delete();
 			} else {
 				return;
 			}
 		}
 		if (f != null) {
 			master.getLocationManager().saveLocationToFile(f);
 		}
 	}
 
 	public void loadLocation() {
 		File f = getLocationFileFromDialog(SWT.OPEN);
 		if (f != null) {
 			master.getLocationManager().loadLocation(f);
 		}
 	}
 
 	/**
 	 * Gets a new location file from a new FileDialog
 	 * @param style
 	 * @return
 	 */
 	public File getLocationFileFromDialog(int style) {
 		FileDialog dialog = new FileDialog(shlPartyMaster, style);
 		dialog.setFilterNames(new String[] { "Location Files", "All Files (*.*)" });
 		dialog.setFilterExtensions(new String[] { "*.loc", "*.*" });
 		dialog.setFilterPath(".");
 		dialog.setFileName("location.loc");
 		String fileName = dialog.open();
 		if (fileName != null && !fileName.trim().equals("")) {
 			try {
 				File file = new File(fileName);
 				return file;
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 		System.err.println("Error saving location file");
 		return null;
 	}
 
 	public LightMaster getMaster() {
 		return master;
 	}
 
 	public Shell getShell() {
 		return shlPartyMaster;
 	}
 
 	/**
 	 * Shows an ElementsEditor
 	 */
 	public void showElementsEditor() {
 		if (elementsEditor != null && !elementsEditor.isDisposed()) {
 			elementsEditor.getShell().forceActive();
 		} else {
 			elementsEditor = new ElementsEditor(this);
 			windowManager.addElementDisplay(elementsEditor);
 			elementsEditor.open();
 		}
 	}
 
 	/**
 	 * Updates the elements in the table
 	 */
 	public void updateElements() {
 		ElementUpdater.updateElements(this, table);
 	}
 
 	public void showUCEditor() {
 		if (ucEditor != null && !ucEditor.isDisposed()) {
 			ucEditor.getShell().forceActive();
 		} else {
 			ucEditor = new UCEditor(this);
 			ucEditor.open();
 		}
 	}
 
 	public void showSimulator() {
 		if (simulator != null && !simulator.isDisposed()) {
 			simulator.getShell().forceActive();
 		} else {
 			simulator = new LightElementSimulator(this);
 			simulator.open();
 		}
 	}
 
 	@Override
 	public boolean isDisposed() {
 		return shlPartyMaster.isDisposed();
 	}
 
 	public LightWindowManager getWindowManager() {
 		return windowManager;
 	}
 
 	/**
 	 * Returns the width of the window
 	 */
 	public int getWidth() {
 		return shlPartyMaster.getSize().x;
 	}
 
 	/**
 	 * Returns the height of the window
 	 */
 	public int getHeight() {
 		return shlPartyMaster.getSize().y;
 	}
 
 	/**
 	 * Returns the size of the window
 	 * @return
 	 */
 	public Point getSize() {
 		return shlPartyMaster.getSize();
 	}
 
 	public Display getDisplay() {
 		return windowManager.getDisplay();
 	}
 
 	public ArrayList<ElementController> getSelectedElements() {
 		ArrayList<ElementController> ret = new ArrayList<ElementController>();
 		for (TableItem item : table.getSelection()) {
 			ElementController element = (ElementController) item.getData();
 			ret.add(element);
 		}
 		return ret;
 	}
 
 	public void tableClick(MouseEvent event) {
 		// 3rd button, right click
 		if (event.button == 3) {
 			// Dynamically construct the popup menu based on what's selected
 			Menu menu = new Menu(table.getShell(), SWT.POP_UP);
 
 			MenuItem previewAnimation = new MenuItem(menu, SWT.CASCADE);
 			previewAnimation.setText("Preview Animation");
 
 			Menu previewAnimationMenu = new Menu(previewAnimation);
 			final ArrayList<ElementController> selectedElements = getSelectedElements();
 			ArrayList<Class<? extends ElementAnimation>> animationList = master.getAnimationManager().getAnimationListForElements(selectedElements);
 
 			// Add each available animation to the menu
 			for (final Class<? extends ElementAnimation> c : animationList) {
 				MenuItem menuItem = new MenuItem(previewAnimationMenu, SWT.PUSH);
 				menuItem.addSelectionListener(new SelectionAdapter() {
 					public void widgetSelected(SelectionEvent e) {
 						ElementAnimation animation = null;
 						try {
 							animation = c.getConstructor(LightMaster.class, int.class, ArrayList.class).newInstance(master, -1, selectedElements);
 							previewAnimation(animation);
 						} catch (Exception ex) {
 							System.out.println("There was an error previewing the animation " + animation
 									+ " - Check that it has the correct constructor");
 							ex.printStackTrace();
 						}
 					}
 				});
 				menuItem.setText(c.getSimpleName());
 				menuItem.setData(c);
 			}
 
 			previewAnimation.setMenu(previewAnimationMenu);
 			Point eventPoint = new Point(event.x, event.y);
 			eventPoint = table.toDisplay(eventPoint);
 			menu.setLocation(eventPoint);
 			menu.setVisible(true);
 
 		}
 	}
 
 	/**
 	 * Executes an animation immediately
 	 * @param animation
 	 */
 	public void previewAnimation(ElementAnimation animation) {
 		animation.setup(getShell());
 		animation.trigger();
 	}
 
 	public void deleteSelectedElements() {
 		ArrayList<ElementController> selected = getSelectedElements();
 
 		if (selected.size() > 0) {
 			boolean delete = PartyToolkit.openQuestion(this.getShell(), "Are you sure you want to delete "
 					+ selected.size() + " element" + (selected.size() > 1 ? "s" : "") + "?", "Delete elements?");
 			if (delete) {
 				for (ElementController victim : selected) {
 					master.removeElement(victim);
 				}
 
 				// Update all elements
 				master.getWindowManager().updateElements();
 			}
 		}
 	}
 
 	public void tableKeyReleased(KeyEvent e) {
 		if (e.keyCode == SWT.BS) {
 			deleteSelectedElements();
 		}
 	}
 	
 	/**
      * Launch the application. This is here for the SWT Designer
      * @param args
      */
     public static void main(String[] args) {
         try {
             LightWindow window = new LightWindow(null, null);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
