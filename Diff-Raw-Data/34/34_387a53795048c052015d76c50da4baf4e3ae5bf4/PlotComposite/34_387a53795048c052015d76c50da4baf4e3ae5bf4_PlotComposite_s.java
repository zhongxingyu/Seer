 package de.ptb.epics.eve.editor.views.scanmoduleview.plotcomposite;
 
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IWorkbenchActionConstants;
 
 import de.ptb.epics.eve.editor.views.DelColumnEditingSupport;
 import de.ptb.epics.eve.editor.views.scanmoduleview.ScanModuleView;
 import de.ptb.epics.eve.editor.views.scanmoduleview.ActionComposite;
 
 /**
  * <code>PlotComposite</code> is part of the 
  * {@link de.ptb.epics.eve.editor.views.scanmoduleview.ScanModuleView}.
  * 
  * @author Hartmut Scherr
  * @author Marcus Michalsky
  */
 public class PlotComposite extends ActionComposite {
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param parentView the parent view
 	 * @param parent the parent
 	 * @param style the style
 	 */
 	public PlotComposite(final ScanModuleView parentView, final Composite parent, 
 						 final int style) {
 		super(parentView, parent, style);
 		this.setLayout(new GridLayout());
 		createViewer();
 	}
 	
 	/*
 	 * 
 	 */
 	private void createViewer() {
 		this.tableViewer = new TableViewer(this, SWT.NONE);
 		GridData gridData = new GridData();
 		gridData.minimumHeight = 120;
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		this.tableViewer.getTable().setLayoutData(gridData);
		createColumns(this.tableViewer);
 		this.tableViewer.getTable().setHeaderVisible(true);
 		this.tableViewer.getTable().setLinesVisible(true);
 		this.tableViewer.setContentProvider(new ContentProvider());
 		this.tableViewer.setLabelProvider(new LabelProvider());
 		this.tableViewer.getTable().addFocusListener(
 				new TableViewerFocusListener());
 		
 		// create context menu
 		MenuManager menuManager = new MenuManager();
 		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
 		menuManager.setRemoveAllWhenShown(true);
 		this.tableViewer.getTable().setMenu(
 				menuManager.createContextMenu(this.tableViewer.getTable()));
 		// register menu
 		parentView.getSite().registerContextMenu(
 			"de.ptb.epics.eve.editor.views.scanmoduleview.plotcomposite.popup", 
 			menuManager, this.tableViewer);
 	}
 	
 	/*
 	 * 
 	 */
	private void createColumns(TableViewer viewer) {
 		TableViewerColumn delColumn = new TableViewerColumn(
 				this.tableViewer, SWT.CENTER);
 		delColumn.getColumn().setText("");
 		delColumn.getColumn().setWidth(22);
 		delColumn.setEditingSupport(new DelColumnEditingSupport(
 				this.tableViewer,
 				"de.ptb.epics.eve.editor.command.removeplotwindow"));
 		
 		TableViewerColumn idColumn = new TableViewerColumn(
 				this.tableViewer, SWT.LEFT);
 		idColumn.getColumn().setText("Id");
 		idColumn.getColumn().setWidth(30);
 		
 		TableViewerColumn nameColumn = new TableViewerColumn(
 				this.tableViewer, SWT.LEFT);
 		nameColumn.getColumn().setText("Name");
 		nameColumn.getColumn().setWidth(120);
 		
 		TableViewerColumn xColumn = new TableViewerColumn(
 				this.tableViewer, SWT.LEFT);
 		xColumn.getColumn().setText("x Axis");
 		xColumn.getColumn().setWidth(100);
 		
 		TableViewerColumn y1Column = new TableViewerColumn(
 				this.tableViewer, SWT.LEFT);
 		y1Column.getColumn().setText("y Axis1");
 		y1Column.getColumn().setWidth(100);
 		
 		TableViewerColumn y2Column = new TableViewerColumn(
 				this.tableViewer, SWT.LEFT);
 		y2Column.getColumn().setText("y Axis2");
 		y2Column.getColumn().setWidth(100);
 	}
 }
