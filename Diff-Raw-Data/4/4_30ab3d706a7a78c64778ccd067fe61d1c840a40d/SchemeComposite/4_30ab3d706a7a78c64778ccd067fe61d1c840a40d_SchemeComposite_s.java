 package de.hswt.hrm.scheme.ui.part;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 import javax.swing.text.html.FormSubmitEvent;
 
 import org.eclipse.e4.ui.workbench.modeling.EPartService;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.window.IShellProvider;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DragSource;
 import org.eclipse.swt.dnd.DropTarget;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseWheelListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Slider;
 import org.eclipse.ui.dialogs.FilteredTree;
 import org.eclipse.ui.dialogs.PatternFilter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 
 import de.hswt.hrm.common.database.exception.SaveException;
 import de.hswt.hrm.common.ui.swt.constants.SearchFieldConstants;
 import de.hswt.hrm.common.ui.swt.forms.FormUtil;
 import de.hswt.hrm.common.ui.swt.layouts.LayoutUtil;
 import de.hswt.hrm.component.service.ComponentService;
 import de.hswt.hrm.plant.model.Plant;
 import de.hswt.hrm.scheme.model.RenderedComponent;
 import de.hswt.hrm.scheme.model.Scheme;
 import de.hswt.hrm.scheme.model.SchemeComponent;
 import de.hswt.hrm.scheme.service.ComponentConverter;
 import de.hswt.hrm.scheme.service.SchemeService;
 import de.hswt.hrm.scheme.ui.ComponentLoadThread;
 import de.hswt.hrm.scheme.ui.ItemClickListener;
 import de.hswt.hrm.scheme.ui.SchemeGrid;
 import de.hswt.hrm.scheme.ui.SchemeGridItem;
 import de.hswt.hrm.scheme.ui.SchemeTreePatternFilter;
 import de.hswt.hrm.scheme.ui.dnd.DragData;
 import de.hswt.hrm.scheme.ui.dnd.DragDataTransfer;
 import de.hswt.hrm.scheme.ui.dnd.GridDragListener;
 import de.hswt.hrm.scheme.ui.dnd.GridDropTargetListener;
 import de.hswt.hrm.scheme.ui.dnd.TreeDragListener;
 import de.hswt.hrm.scheme.ui.tree.SchemeTreeLabelProvider;
 import de.hswt.hrm.scheme.ui.tree.TreeContentProvider;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.forms.widgets.Section;
 import org.eclipse.swt.graphics.Point;
 
 public class SchemeComposite extends Composite {
 
 	private static final int MOVE_AMOUNT = 3;
 
 	private final static Logger LOG = LoggerFactory
 			.getLogger(SchemeComposite.class);
 	private static final String DELETE = "Löschen";
 
 	@Inject
 	SchemeService schemeService;
 
 	@Inject
 	EPartService service;
 
 	@Inject
 	ComponentService componentsService;
 
 	@Inject
 	private IShellProvider shellProvider;
 
 	private IContributionItem saveContribution;
 	private IContributionItem moveXContribution;
 	private IContributionItem moveYContribution;
 
 	private List<IContributionItem> contributionItems = new ArrayList<IContributionItem>();
 
 	/**
 	 * The DND transfer type
 	 */
 	private static final Transfer[] TRANSFER = new Transfer[] { DragDataTransfer
 			.getInstance() };
 
 	/**
 	 * The background color of the scheme editor.
 	 */
 	private static final RGB EDITOR_BACKGROUND = new RGB(255, 255, 255);
 
 	private static final int DRAG_OPS = DND.DROP_COPY,
 			DROP_OPS = DND.DROP_COPY;
 
 	/**
 	 * The pixel per grid range. Defines how far you can zoom in and out
 	 */
 	private static final int MIN_PPG = 20, MAX_PPG = 70;
 
 	/**
 	 * The topmost gui parent
 	 */
 	// private Composite root;
 
 	private SchemeGrid grid;
 
 	private TreeViewer tree;
 
 	/**
 	 * The PatternFilter defines which TreeItems are visible for a given search
 	 * pattern
 	 */
 	private PatternFilter filter;
 
 	private List<RenderedComponent> components;
 
 	private Plant plant;
 
 	/*
 	 * DND items for the grid
 	 */
 	private DragSource gridDragSource;
 	private DropTarget gridDropTarget;
 
 	private GridDropTargetListener gridListener;
 	private GridDragListener gridDragListener;
 	private TreeDragListener treeDragListener;
 
 	private ScrolledComposite scrolledComposite;
 
 	private Composite treeComposite;
 	private Slider zoomSlider;
 	private final FormToolkit formToolkit = new FormToolkit(
 			Display.getDefault());
 	private Section schemeSection;
 
 	/**
 	 * Create the composite.
 	 * 
 	 * @param parent
 	 * @param style
 	 */
 	private SchemeComposite(Composite parent, int style) {
 		super(parent, style);
 		createControls();
 	}
 
 	/**
 	 * Create the composite.
 	 * 
 	 * @param parent
 	 */
 	public SchemeComposite(Composite parent) {
 		super(parent, SWT.NONE);
 	}
 
 	@PostConstruct
 	private void createControls() {
 		setLayout(new FillLayout());
 
 		if (schemeService == null) {
 			LOG.error("SchemeService not properly injected to SchemePart.");
 		}
 
 		schemeSection = formToolkit.createSection(this, Section.TITLE_BAR);
 		schemeSection.setBackgroundMode(SWT.INHERIT_NONE);
 		formToolkit.paintBordersFor(schemeSection);
 		schemeSection.setText("Scheme");
 		schemeSection.setExpanded(true);
 		FormUtil.initSectionColors(schemeSection);
 
 		Composite composite = new Composite(schemeSection, SWT.NONE);
 		composite.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
 		composite.setBackgroundMode(SWT.INHERIT_DEFAULT);
 		schemeSection.setClient(composite);
 		composite.setLayout(new GridLayout(2, false));
 
 		treeComposite = new Composite(composite, SWT.NONE);
 		treeComposite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false,
 				true, 1, 3));
 		
 		GridLayout gl = new GridLayout();
 		gl.marginHeight = 0;
 		gl.marginWidth = 0;
 		treeComposite.setLayout(gl);
 
 
 		scrolledComposite = new ScrolledComposite(composite, SWT.BORDER
 				| SWT.H_SCROLL | SWT.V_SCROLL);
 		scrolledComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
 		scrolledComposite.setExpandHorizontal(true);
 		scrolledComposite.setExpandVertical(true);
 		scrolledComposite.setLayoutData(LayoutUtil.createFillData());
 
 		zoomSlider = new Slider(composite, SWT.HORIZONTAL);
 		GridData gdZoomSlider = LayoutUtil.createHorzFillData();
 //		gdZoomSlider.minimumHeight = 100;
 		zoomSlider.setLayoutData(gdZoomSlider);
 
 		initTree();
 		initSchemeGrid();
 		initGridDropTarget();
 		initGridDragSource();
 		initGridDropTargetListener();
 		initTreeDND();
 		initGridDND();
 		initSlider();
 		initItemClickListener();
 
 		initSaveContribution();
 		initMoveXContribution();
 		initMoveYContribution();
 		contributionItems.add(moveYContribution);
 		contributionItems.add(moveXContribution);
 		contributionItems.add(saveContribution);
 
 		// TODO remove
 		newScheme(new Plant(1, "bla"));
 		new ComponentLoadThread(this, this.getDisplay(), componentsService).start();
 	}
 
 	/**
 	 * Shows a empty grid. The user can enter a scheme, and save it for the
 	 * given plant.
 	 * 
 	 * @param plant
 	 */
 	public void newScheme(Plant plant) {
 		this.plant = plant;
 		grid.setItems(new ArrayList<SchemeGridItem>());
 		grid.clearDirty();
 	}
 
 	/**
 	 * The given scheme is loaded into the editor. The user can save a new
 	 * scheme for the given plant.
 	 * 
 	 * @param scheme
 	 * @throws IOException
 	 *             If a pdf file could not be read
 	 */
 	public void modifyScheme(Scheme scheme) throws IOException {
 		if (!scheme.getPlant().isPresent()) {
 			throw new IllegalArgumentException("The plant must be present here");
 		}
 		this.plant = scheme.getPlant().get();
 		grid.setItems(toSchemeGridItems(scheme.getSchemeComponents()));
 		grid.clearDirty();
 	}
 
 	/**
 	 * @return Was the grid changed since last modifyScheme() or newScheme()
 	 *         call
 	 */
 	public boolean isDirty() {
 		return grid.isDirty();
 	}
 
 	private List<SchemeGridItem> toSchemeGridItems(
 			Collection<SchemeComponent> sc) throws IOException {
 		List<SchemeGridItem> l = new ArrayList<>();
 		for (SchemeComponent c : sc) {
 			l.add(new SchemeGridItem(ComponentConverter.convert(
 					this.getDisplay(), c.getComponent()), c.getDirection(), c
 					.getX(), c.getY()));
 		}
 		return l;
 	}
 
 	/*
 	 * init gui elements
 	 */
 
 	private void initMoveXContribution() {
 		// TODO translate
 		Action moveXAction = new Action("Move X") {
 			@Override
 			public void run() {
 				super.run();
 				Collection<SchemeGridItem> items = grid.getItems();
 				items = Collections2.transform(items,
 						new Function<SchemeGridItem, SchemeGridItem>() {
 							public SchemeGridItem apply(SchemeGridItem item) {
 								item.setX(item.getX() + MOVE_AMOUNT);
 								return item;
 							}
 						});
 				grid.setItems(items);
 			}
 		};
 		moveXAction.setDescription("Move's the grid elements in x direction.");
 		moveXContribution = new ActionContributionItem(moveXAction);
 	}
 
 	private void initMoveYContribution() {
 		// TODO translate
 		Action moveYAction = new Action("Move Y") {
 			@Override
 			public void run() {
 				super.run();
 				Collection<SchemeGridItem> items = grid.getItems();
 				items = Collections2.transform(items,
 						new Function<SchemeGridItem, SchemeGridItem>() {
 							public SchemeGridItem apply(SchemeGridItem item) {
 								item.setX(item.getX() + MOVE_AMOUNT);
 								return item;
 							}
 						});
 				grid.setItems(items);
 			}
 		};
 		moveYAction.setDescription("Move's the grid elements in y direction.");
 		moveYContribution = new ActionContributionItem(moveYAction);
 	}
 
 	private void initItemClickListener() {
 		grid.setItemClickListener(new ItemClickListener() {
 
 			@Override
 			public void itemClicked(final MouseEvent e,
 					final SchemeGridItem item) {
 				if (e.button == 3) { // right mouse click
 					final Menu menu = new Menu(shellProvider.getShell(),
 							SWT.POP_UP);
 					final MenuItem delete = new MenuItem(menu, SWT.PUSH);
 					delete.addSelectionListener(new SelectionListener() {
 
 						@Override
 						public void widgetSelected(SelectionEvent e) {
 							grid.removeItem(item);
 						}
 
 						@Override
 						public void widgetDefaultSelected(SelectionEvent e) {
 						}
 					});
 					delete.setText(DELETE);
 					menu.setVisible(true);
 				}
 			}
 		});
 	}
 
 	private void initGridDropTargetListener() {
 		gridListener = new GridDropTargetListener(grid, components, this);
 		gridDropTarget.addDropListener(gridListener);
 	}
 
 	private void initGridDragSource() {
 		gridDragSource = new DragSource(grid, DRAG_OPS);
 		gridDragSource.setTransfer(TRANSFER);
 	}
 
 	private void initGridDropTarget() {
 		gridDropTarget = new DropTarget(grid, DROP_OPS);
 		gridDropTarget.setTransfer(TRANSFER);
 	}
 
 	private void initTreeDND() {
 		treeDragListener = new TreeDragListener(tree, components, grid);
 		tree.addDragSupport(DRAG_OPS, TRANSFER, treeDragListener);
 	}
 
 	private void initGridDND() {
 		gridDragListener = new GridDragListener(grid, components);
 		gridDragSource.addDragListener(gridDragListener);
 	}
 
 	private void initSchemeGrid() {
 		grid = new SchemeGrid(scrolledComposite, SWT.NONE, 40, 20, 40);
 		grid.setBackground(new Color(this.getDisplay(), EDITOR_BACKGROUND));
 		scrolledComposite.setContent(grid);
 		grid.addMouseWheelListener(new MouseWheelListener() {
 			@Override
 			public void mouseScrolled(MouseEvent e) {
 				changeZoom(e.count);
 			}
 		});
 	}
 
 	private void initTree() {
 		/*
 		 * A FilteredTree can't be created in XWT, so its done here
 		 */
 		filter = new SchemeTreePatternFilter();
 		final FilteredTree filteredTree = new FilteredTree(treeComposite,
 				SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL, filter, true);
 		filteredTree.getFilterControl().addModifyListener(new ModifyListener() {
 			
 			@Override
 			public void modifyText(ModifyEvent e) {
 				updatePatternFilter(filteredTree.getFilterControl().getText());
 			}
 		});
 		// TODO translate
 		filteredTree.setInitialText(SearchFieldConstants.DEFAULT_SEARCH_STRING);
 		GridData gd = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1);
 		gd.widthHint = 200;
 		filteredTree.setLayoutData(gd);
 		tree = filteredTree.getViewer();
 		tree.setContentProvider(new TreeContentProvider());
 		tree.setLabelProvider(new SchemeTreeLabelProvider());
 	}
 
 	private void initSlider() {
 		zoomSlider.setMaximum(MAX_PPG);
 		zoomSlider.setMinimum(MIN_PPG);
 		zoomSlider.addSelectionListener(new SelectionListener() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				updateZoom();
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 			}
 		});
 		updateZoom();
 	}
 
 	private void initSaveContribution() {
 		// TODO translate
 		Action saveAction = new Action("Save") {
 			@Override
 			public void run() {
 				super.run();
 				Collection<SchemeComponent> schemeComps = Collections2
 						.transform(
 								grid.getItems(),
 								new Function<SchemeGridItem, SchemeComponent>() {
 									public SchemeComponent apply(
 											SchemeGridItem item) {
 										return item.asSchemeComponent();
 									}
 								});
 				try {
 					schemeService.insert(plant, schemeComps);
				} catch (SaveException e1) {
 					// TODO translate
 					MessageDialog.openError(shellProvider.getShell(),
 							"Fehler beim Speichern",
 							"Das Schema konnte nicht gespeichert werden");
 				}
 			}
 		};
 		saveAction.setDescription("Save the grid.");
 		saveContribution = new ActionContributionItem(saveAction);
 	}
 
 	/*
 	 * setter
 	 */
 
 	private void changeZoom(int amount) {
 		zoomSlider.setSelection(zoomSlider.getSelection() + amount);
 		updateZoom();
 	}
 
 	/*
 	 * getter
 	 */
 
 	/*
 	 * updates
 	 */
 
 	private void updateZoom() {
 		grid.setPixelPerGrid(zoomSlider.getSelection());
 	}
 
 	private void updatePatternFilter(String text) {
 		filter.setPattern(text);
 		tree.refresh();
 		if (text.trim().length() == 0) {
 			tree.collapseAll();
 		} else {
 			tree.expandAll();
 		}
 	}
 
 //	private int getRenderedComponentId(RenderedComponent component) {
 //		return this.components.indexOf(component);
 //	}
 //
 //	private RenderedComponent getRenderedComponent(int id) {
 //		return components.get(id);
 //	}
 
 	public DragData getDraggingItem() {
 		DragData i1 = gridDragListener.getDraggingItem();
 		DragData i2 = treeDragListener.getDraggingItem();
 		if (i1 == null && i2 == null) {
 			throw new RuntimeException("No drag and drop is running");
 		}
 		if (i1 != null) {
 			return i1;
 		} else {
 			return i2;
 		}
 	}
 
 	public void setRenderedComponents(List<RenderedComponent> components) {
 		System.out.println("size components: " + components.size());
 		this.components = components;
 		tree.setInput(components);
 	}
 
 	public List<IContributionItem> getContributionItems() {
 		return Collections.unmodifiableList(contributionItems);
 
 	}
 
 	@Override
 	protected void checkSubclass() {
 		// Disable the check that prevents subclassing of SWT components
 	}
 }
