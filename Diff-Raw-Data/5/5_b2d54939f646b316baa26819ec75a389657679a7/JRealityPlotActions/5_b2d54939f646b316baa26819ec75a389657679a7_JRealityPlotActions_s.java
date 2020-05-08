 package org.dawnsci.plotting.jreality;
 
 import java.io.File;
 
 import org.dawb.common.ui.menu.CheckableActionGroup;
 import org.dawb.common.ui.menu.MenuAction;
 import org.dawb.common.ui.printing.PrintSettings;
 import org.dawnsci.plotting.api.ActionType;
 import org.dawnsci.plotting.api.IPlotActionSystem;
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.ManagerType;
 import org.dawnsci.plotting.api.tool.IToolPage.ToolPageRole;
 import org.dawnsci.plotting.jreality.impl.SurfPlotStyles;
 import org.dawnsci.plotting.jreality.print.JRealityPrintDialog;
 import org.dawnsci.plotting.jreality.print.PlotExportUtil;
 import org.dawnsci.plotting.jreality.tick.TickFormatting;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.printing.Printer;
 import org.eclipse.swt.printing.PrinterData;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FileDialog;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class JRealityPlotActions {
 	
 	private static final Logger logger = LoggerFactory.getLogger(JRealityPlotActions.class);
 
 	private JRealityPlotViewer        plotter;
 	private IPlottingSystem           system;
 	private IPlotActionSystem         actionMan;
 
 	private Action useTransparency;
 	private Action renderEdgeOnly;
 	private Action uniformSize;
 
 	private Action orthographicProjAction, boundingBox, xCoordGrid, yCoordGrid, zCoordGrid;
 
 	public JRealityPlotActions(JRealityPlotViewer jRealityPlotViewer,
 			                   IPlottingSystem    system) {
 		this.plotter            = jRealityPlotViewer;
 		this.system             = system;
 		this.actionMan          = system.getPlotActionSystem();
 	}
 
 	public void createActions() {
 		// Tools
 		actionMan.createToolDimensionalActions(ToolPageRole.ROLE_3D, "org.dawb.workbench.plotting.views.toolPageView.3D");
 
 		createGridLineActions();
 
 		//create reset action for multiimage
 //		actionMan.registerAction("jreality.plotting.reset.multi2.actions", createResetAction(), 
 //				ActionType.MULTIIMAGE, ManagerType.TOOLBAR);
 
 		// Print/export
 		createExportActions();
 		//axes actions
		createAxesActions();
 		// Others
 		createSurfaceModeActions();
 		createScatter3DModeActions();
 	}
 
 	private void createSurfaceModeActions() {
 		CheckableActionGroup surfaceModeGroup = new CheckableActionGroup();
 		String surfaceActionsGroupName = "jreality.plotting.surface.mode.actions";
 		actionMan.registerGroup(surfaceActionsGroupName, ManagerType.MENUBAR);
 		Action displayFilled = new Action("Filled", IAction.AS_CHECK_BOX) {
 			@Override
 			public void run() {
 				plotter.setPlot2DSurfStyle(SurfPlotStyles.FILLED);
 				plotter.refresh(false);
 				setChecked(true);
 			}
 		};
 		displayFilled.setText("Filled mode");
 		displayFilled.setDescription("Render the graph in filled mode");
		displayFilled.setImageDescriptor(Activator.getImageDescriptor("icons/save.gif"));
 		displayFilled.setChecked(true);
 		surfaceModeGroup.add(displayFilled);
 		actionMan.registerAction(surfaceActionsGroupName, displayFilled, ActionType.SURFACE, ManagerType.MENUBAR);
 
 		Action displayWireframe = new Action("Wireframe", IAction.AS_CHECK_BOX) {
 			@Override
 			public void run() {
 				plotter.setPlot2DSurfStyle(SurfPlotStyles.WIREFRAME);
 				SurfPlotStyles.values();
 				plotter.refresh(false);
 				setChecked(true);
 			}
 		};
 		displayWireframe.setText("Wireframe mode");
 		displayWireframe.setDescription("Render the graph in wireframe mode");
 		surfaceModeGroup.add(displayWireframe);
 		actionMan.registerAction(surfaceActionsGroupName, displayWireframe, ActionType.SURFACE, ManagerType.MENUBAR);
 
 		Action displayLinegraph = new Action("LineGraph", IAction.AS_CHECK_BOX) {
 			@Override
 			public void run() {
 				plotter.setPlot2DSurfStyle(SurfPlotStyles.LINEGRAPH);
 				plotter.refresh(true);
 				setChecked(true);
 			}
 		};
 		displayLinegraph.setText("Linegraph mode");
 		displayLinegraph.setDescription("Render the graph in linegraph mode");
 		surfaceModeGroup.add(displayLinegraph);
 		actionMan.registerAction(surfaceActionsGroupName, displayLinegraph, ActionType.SURFACE, ManagerType.MENUBAR);
 
 		Action displayPoint = new Action("Point", IAction.AS_CHECK_BOX) {
 			@Override
 			public void run() {
 				plotter.setPlot2DSurfStyle(SurfPlotStyles.POINTS);
 				plotter.refresh(false);	
 				setChecked(true);
 			}
 		};
 		displayPoint.setText("Point mode");
 		displayPoint.setDescription("Render the graph in dot mode");
 		surfaceModeGroup.add(displayPoint);
 		actionMan.registerAction(surfaceActionsGroupName, displayPoint, ActionType.SURFACE, ManagerType.MENUBAR);
 	}
 
 	private void createScatter3DModeActions() {
 		String scatterActionsName = "jreality.plotting.scatter3d.mode.actions";
 		actionMan.registerGroup(scatterActionsName, ManagerType.MENUBAR);
 
 		useTransparency = new Action("", IAction.AS_CHECK_BOX) {
 			@Override
 			public void run() {
 				plotter.useTransparency(useTransparency.isChecked());
 				plotter.refresh(false);
 			}
 		};
 		useTransparency.setText("Use transparency");
 		useTransparency.setToolTipText("Switch on/off transparency");
 		actionMan.registerAction(scatterActionsName, useTransparency, ActionType.SCATTER3D, ManagerType.MENUBAR);
 
 		renderEdgeOnly = new Action("", IAction.AS_CHECK_BOX) {
 			@Override
 			public void run() {
 				if (renderEdgeOnly.isChecked())
 					plotter.useTransparency(true);
 				else
 					plotter.useTransparency(useTransparency.isChecked());
 				plotter.useDrawOutlinesOnly(renderEdgeOnly.isChecked());
 			}
 		};
 		renderEdgeOnly.setText("Draw outlines only");
 		renderEdgeOnly.setToolTipText("Switch on/off drawing outlines only");
 		actionMan.registerAction(scatterActionsName, renderEdgeOnly, ActionType.SCATTER3D, ManagerType.MENUBAR);
 
 		uniformSize = new Action("", IAction.AS_CHECK_BOX) {
 			@Override
 			public void run() {
 				plotter.useUniformSize(uniformSize.isChecked());
 				plotter.refresh(false);
 			}
 		};
 		uniformSize.setText("Uniform size");
 		uniformSize.setToolTipText("Switch on/off uniform point size");
 		actionMan.registerAction(scatterActionsName, uniformSize, ActionType.SCATTER3D, ManagerType.MENUBAR);
 
 	}
 
 	private void createAxesActions() {
 		String axesActionsName = "jreality.plotting.axes.actions";
 		actionMan.registerGroup(axesActionsName, ManagerType.MENUBAR);
 		// XAxis menu
 		Action xLabelTypeRound = new Action("", IAction.AS_RADIO_BUTTON) {
 			@Override
 			public void run() {
 				plotter.setXTickLabelFormat(TickFormatting.roundAndChopMode);
 				plotter.refresh(false);
 			}
 		};
 		xLabelTypeRound.setText("X-Axis labels integer");
 		xLabelTypeRound.setToolTipText("Change the labelling on the x-axis to integer numbers");
 		xLabelTypeRound.setChecked(true);
 
 		Action xLabelTypeFloat = new Action("", IAction.AS_RADIO_BUTTON) {
 			@Override
 			public void run() {
 				plotter.setXTickLabelFormat(TickFormatting.plainMode);
 				plotter.refresh(false);
 			}
 		};
 		xLabelTypeFloat.setText("X-Axis labels real");
 		xLabelTypeFloat.setToolTipText("Change the labelling on the x-axis to real numbers");
 
 		Action xLabelTypeExponent = new Action("", IAction.AS_RADIO_BUTTON) {
 			@Override
 			public void run() {
 				plotter.setXTickLabelFormat(TickFormatting.useExponent);
 				plotter.refresh(false);
 			}
 		};
 		xLabelTypeExponent.setText("X-Axis labels exponents");
 		xLabelTypeExponent.setToolTipText("Change the labelling on the x-axis to using exponents");
 
 		Action xLabelTypeSI = new Action("", IAction.AS_RADIO_BUTTON) {
 			@Override
 			public void run() {
 				plotter.setXTickLabelFormat(TickFormatting.useSIunits);
 				plotter.refresh(false);
 			}
 		};
 		MenuAction xAxisMenu = new MenuAction("X-Axis");
 		xAxisMenu.add(xLabelTypeFloat);
 		xAxisMenu.add(xLabelTypeRound);
 		xAxisMenu.add(xLabelTypeExponent);
 		xAxisMenu.add(xLabelTypeSI);
 		actionMan.registerAction(axesActionsName, xAxisMenu, ActionType.THREED, ManagerType.MENUBAR);
 
 		// YAxis menu
 		xLabelTypeSI.setText("X-Axis labels SI units");
 		xLabelTypeSI.setToolTipText("Change the labelling on the x-axis to using SI units");
 		Action yLabelTypeRound = new Action("", IAction.AS_RADIO_BUTTON) {
 			@Override
 			public void run() {
 				plotter.setYTickLabelFormat(TickFormatting.roundAndChopMode);
 				plotter.refresh(false);
 			}
 		};
 		yLabelTypeRound.setText("Y-Axis labels integer");
 		yLabelTypeRound.setToolTipText("Change the labelling on the y-axis to integer numbers");
 		yLabelTypeRound.setChecked(true);
 		Action yLabelTypeFloat = new Action("", IAction.AS_RADIO_BUTTON) {
 			@Override
 			public void run() {
 				plotter.setYTickLabelFormat(TickFormatting.plainMode);
 				plotter.refresh(false);
 			}
 		};
 		yLabelTypeFloat.setText("Y-Axis labels real");
 		yLabelTypeFloat.setToolTipText("Change the labelling on the y-axis to real numbers");
 
 		Action yLabelTypeExponent = new Action("", IAction.AS_RADIO_BUTTON) {
 			@Override
 			public void run() {
 				plotter.setYTickLabelFormat(TickFormatting.useExponent);
 				plotter.refresh(false);
 			}
 		};
 		yLabelTypeExponent.setText("Y-Axis labels exponents");
 		yLabelTypeExponent.setToolTipText("Change the labelling on the y-axis to using exponents");
 
 		Action yLabelTypeSI = new Action("", IAction.AS_RADIO_BUTTON) {
 			@Override
 			public void run() {
 				plotter.setYTickLabelFormat(TickFormatting.useSIunits);
 				plotter.refresh(false);
 			}
 		};
 		yLabelTypeSI.setText("Y-Axis labels SI units");
 		yLabelTypeSI.setToolTipText("Change the labelling on the y-axis to using SI units");
 		Action zLabelTypeRound = new Action("", IAction.AS_RADIO_BUTTON) {
 			@Override
 			public void run() {
 				plotter.setZTickLabelFormat(TickFormatting.roundAndChopMode);
 				plotter.refresh(false);
 			}
 		};
 		MenuAction yAxisMenu = new MenuAction("Y-Axis");
 		yAxisMenu.add(yLabelTypeFloat);
 		yAxisMenu.add(yLabelTypeRound);
 		yAxisMenu.add(yLabelTypeExponent);
 		yAxisMenu.add(yLabelTypeSI);
 		actionMan.registerAction(axesActionsName, yAxisMenu, ActionType.SCATTER3D, ManagerType.MENUBAR);
 
 		// ZAxis menu
 		zLabelTypeRound.setText("Z-Axis labels integer");
 		zLabelTypeRound.setToolTipText("Change the labelling on the z-axis to integer numbers");
 		Action zLabelTypeFloat = new Action("", IAction.AS_RADIO_BUTTON) {
 			@Override
 			public void run() {
 				plotter.setXTickLabelFormat(TickFormatting.plainMode);
 				plotter.refresh(false);
 			}
 		};
 		zLabelTypeFloat.setText("Z-Axis labels real");
 		zLabelTypeFloat.setToolTipText("Change the labelling on the z-axis to real numbers");
 		zLabelTypeFloat.setChecked(true);
 
 		Action zLabelTypeExponent = new Action("", IAction.AS_RADIO_BUTTON) {
 			@Override
 			public void run() {
 				plotter.setXTickLabelFormat(TickFormatting.useExponent);
 				plotter.refresh(false);
 			}
 		};
 		zLabelTypeExponent.setText("Z-Axis labels exponents");
 		zLabelTypeExponent.setToolTipText("Change the labelling on the z-axis to using exponents");
 
 		Action zLabelTypeSI = new Action("", IAction.AS_RADIO_BUTTON) {
 			@Override
 			public void run() {
 				plotter.setZTickLabelFormat(TickFormatting.useSIunits);
 				plotter.refresh(false);
 			}
 		};
 		zLabelTypeSI.setText("Z-Axis labels SI units");
 		zLabelTypeSI.setToolTipText("Change the labelling on the z-axis to using SI units");
 		MenuAction zAxisMenu = new MenuAction("Z-Axis");
 		zAxisMenu.add(zLabelTypeFloat);
 		zAxisMenu.add(zLabelTypeRound);
 		zAxisMenu.add(zLabelTypeExponent);
 		zAxisMenu.add(zLabelTypeSI);
 		actionMan.registerAction(axesActionsName, zAxisMenu, ActionType.THREED, ManagerType.MENUBAR);
 	}
 
 	private void createExportActions() {
 
 		actionMan.registerGroup("jreality.plotting.export.actions", ManagerType.TOOLBAR);
 
 		Action saveGraph = new Action("Save graph") {
 
 			// Cache file name otherwise they have to keep
 			// choosing the folder.
 			private String filename;
 
 			@Override
 			public void run() {
 
 				FileDialog dialog = new FileDialog (Display.getDefault().getActiveShell(), SWT.SAVE);
 
 				String [] filterExtensions = new String [] {"*.jpg;*.JPG;*.jpeg;*.JPEG;*.png;*.PNG", "*.ps;*.eps","*.svg;*.SVG"};
 				if (filename!=null) {
 					dialog.setFilterPath((new File(filename)).getParent());
 				} else {
 					dialog.setFilterPath(File.listRoots()[0].getAbsolutePath());
 				}
 				dialog.setFilterNames (PlotExportUtil.FILE_TYPES);
 				dialog.setFilterExtensions (filterExtensions);
 				filename = dialog.open();
 				if (filename == null)
 					return;
 
 				saveGraph(filename, PlotExportUtil.FILE_TYPES[dialog.getFilterIndex()]);
 			}
 		};
 		saveGraph.setImageDescriptor(Activator.getImageDescriptor("icons/save.gif"));
 		actionMan.registerAction("jreality.plotting.export.actions", saveGraph, ActionType.THREED, ManagerType.TOOLBAR);
 
 		Action copyGraph = new Action("Copy plot to clipboard.") {
 			@Override
 			public void run() {
 				copyGraph();
 			}
 		};
 		copyGraph.setImageDescriptor(Activator.getImageDescriptor("icons/copy.gif"));
 		actionMan.registerAction("jreality.plotting.export.actions", copyGraph, ActionType.THREED, ManagerType.TOOLBAR);
 
 		Action printGraph = new Action("Print current plot") {
 			@Override
 			public void run() {
 				printGraph();
 			}
 		};
 		printGraph.setImageDescriptor(Activator.getImageDescriptor("icons/print.png"));
 		actionMan.registerAction("jreality.plotting.export.actions", printGraph, ActionType.THREED, ManagerType.TOOLBAR);
 
 	}
 	
 	private PrinterData   defaultPrinterData;
 	private PrintSettings settings;
 	
 	private void printGraph() {
 		try {
             plotter.setExporting(true);
             final PlottingMode currentMode = plotter.getPlottingMode();
 			if (currentMode == PlottingMode.ONED || currentMode == PlottingMode.ONED_THREED
 					|| currentMode == PlottingMode.SCATTER2D) {
 				if(defaultPrinterData==null)
 					defaultPrinterData=Printer.getDefaultPrinterData();
 				if (settings==null) settings = new PrintSettings();
 				JRealityPrintDialog dialog = new JRealityPrintDialog(plotter.getViewer(), plotter.getControl().getDisplay(),plotter.getGraphTable(), settings);
 				settings=dialog.open();
 			} else{
 				if(defaultPrinterData==null)
 					defaultPrinterData=Printer.getDefaultPrinterData();
 				if (settings==null) settings = new PrintSettings();
 				JRealityPrintDialog dialog = new JRealityPrintDialog(plotter.getViewer(), plotter.getControl().getDisplay(),null, settings);
 				settings=dialog.open();
 			}
 		} finally {
 			plotter.setExporting(false);
 		}
 
 	}
 
 	
 	/**
 	 * Save the graph with the given filename. If the file name ends with a known extension, this is used as the file
 	 * type otherwise it is the string passed in which is read from the save as dialog form normally.
 	 * 
 	 * @param filename
 	 *            the name under which the graph should be saved
 	 * @param fileType
 	 *            type of the file
 	 */
 
 	private synchronized void saveGraph(String filename, String fileType) {
 		
 		try {
             plotter.setExporting(true);
 
             // Can't reach file permissions error message in JReality. Checking explicitly here. 
             File p = new File(filename).getParentFile();
             if (!p.canWrite()) {
             	String msg = "Saving failed: no permission to write in directory: " + p.getAbsolutePath();
             	logger.error(msg);
             	Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, msg); 
             	ErrorDialog.openError(Display.getDefault().getActiveShell(), "Image export error", "Error saving image file", status);
             	return;
             }
 
             try {
             	PlotExportUtil.saveGraph(filename, fileType, plotter.getViewer());
             } catch (Exception e) {
             	logger.error("writing graph file failed", e);
             	Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e); 
             	ErrorDialog.openError(Display.getDefault().getActiveShell(), "Image export error", "Error saving image file", status);
             }
 		} finally {
 			plotter.setExporting(false);
 		}
 	}
 
 	private synchronized void copyGraph() {
 		try {
 			PlotExportUtil.copyGraph(plotter.getViewer());
 		} catch (Exception e) {
 			logger.error(e.getCause().getMessage(), e);
 			Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e); 
 			ErrorDialog.openError(Display.getDefault().getActiveShell(), "Image copy error", "Error copying image to clipboard", status);
 		}
 	}
 
 	private void createGridLineActions() {
 		String gridLineGroupNameAction = "jreality.plotting.grid.line.actions";
 		actionMan.registerGroup(gridLineGroupNameAction, ManagerType.TOOLBAR);
 
 		orthographicProjAction = new Action ("Toggle orthographic projection", IAction.AS_CHECK_BOX) {
 			@Override
 			public void run() {
 				plotter.setPerspectiveCamera(!orthographicProjAction.isChecked(), true);
 			}
 		};
 		orthographicProjAction.setChecked(false);
 		orthographicProjAction.setImageDescriptor(Activator.getImageDescriptor("icons/orthographic.png"));
 		actionMan.registerAction(gridLineGroupNameAction, orthographicProjAction, ActionType.THREED, ManagerType.TOOLBAR);
 
 		Action reset = createResetAction();
 		actionMan.registerAction(gridLineGroupNameAction, reset, ActionType.THREED, ManagerType.TOOLBAR);
 
 		boundingBox = new Action("Toggle bounding box",IAction.AS_CHECK_BOX) {
 			@Override
 			public void run()
 			{
 				plotter.setBoundingBoxEnabled(boundingBox.isChecked());
 				plotter.refresh(false);
 			}
 		};
 
 		boundingBox.setChecked(true);
 		boundingBox.setImageDescriptor(Activator.getImageDescriptor("icons/box.png"));
 		actionMan.registerAction(gridLineGroupNameAction, boundingBox, ActionType.THREED, ManagerType.TOOLBAR);
 
 		// Configure
 		xCoordGrid = new Action("",IAction.AS_CHECK_BOX)
 		{
 			@Override
 			public void run()
 			{
 				plotter.setTickGridLines(xCoordGrid.isChecked(), yCoordGrid.isChecked(),zCoordGrid.isChecked());
 			}
 		};
 		xCoordGrid.setChecked(true);
 		xCoordGrid.setText("X grid lines");
 		xCoordGrid.setToolTipText("Toggle x axis grid lines");
 		xCoordGrid.setImageDescriptor(Activator.getImageDescriptor("icons/xgrid.png"));
 		actionMan.registerAction(gridLineGroupNameAction, xCoordGrid, ActionType.THREED, ManagerType.TOOLBAR);
 		
 		yCoordGrid = new Action("",IAction.AS_CHECK_BOX)
 		{
 			@Override
 			public void run()
 			{
 				plotter.setTickGridLines(xCoordGrid.isChecked(), yCoordGrid.isChecked(),zCoordGrid.isChecked());
 			}
 		};
 		yCoordGrid.setChecked(true);
 		yCoordGrid.setText("Y grid lines");
 		yCoordGrid.setToolTipText("Toggle y axis grid line");
 		yCoordGrid.setImageDescriptor(Activator.getImageDescriptor("icons/ygrid.png"));		
 		actionMan.registerAction(gridLineGroupNameAction, yCoordGrid, ActionType.THREED, ManagerType.TOOLBAR);
 
 		zCoordGrid = new Action("",IAction.AS_CHECK_BOX)
 		{
 			@Override
 			public void run()
 			{
 				plotter.setTickGridLines(xCoordGrid.isChecked(), yCoordGrid.isChecked(),zCoordGrid.isChecked());
 				
 			}
 		};
 		zCoordGrid.setChecked(true);
 		zCoordGrid.setText("Z grid lines");
 		zCoordGrid.setToolTipText("Toggle z axis grid lines");
 		zCoordGrid.setImageDescriptor(Activator.getImageDescriptor("icons/zgrid.png"));
 		actionMan.registerAction(gridLineGroupNameAction, zCoordGrid, ActionType.THREED, ManagerType.TOOLBAR);
 		
 	}
 
 	private Action createResetAction() {
 		Action reset = new Action("Reset orientation and zoom",IAction.AS_PUSH_BUTTON) {
 			@Override
 			public void run()
 			{
 				plotter.resetView();
 				plotter.refresh(false);
 			}
 		};
 
 		reset.setImageDescriptor(Activator.getImageDescriptor("icons/axis.png"));
 		return reset;
 	}
 
 	public void dispose() {
 		plotter            = null;
 		system             = null;
 	}
 
 	public IPlottingSystem getPlottingSystem(){
 		return system;
 	}
 }
