 /*-
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 
 package org.dawnsci.plotting.system;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.csstudio.swt.xygraph.undo.ZoomType;
 import org.dawb.common.services.IPaletteService;
 import org.dawb.common.ui.menu.CheckableActionGroup;
 import org.dawb.common.ui.menu.MenuAction;
 import org.dawb.common.ui.util.EclipseUtils;
 import org.dawb.common.ui.wizard.PlotDataConversionWizard;
 import org.dawb.common.ui.wizard.persistence.PersistenceExportWizard;
 import org.dawnsci.plotting.PlottingActionBarManager;
 import org.dawnsci.plotting.api.ActionType;
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.ManagerType;
 import org.dawnsci.plotting.api.preferences.BasePlottingConstants;
 import org.dawnsci.plotting.api.preferences.PlottingConstants;
 import org.dawnsci.plotting.api.preferences.ToolbarConfigurationConstants;
 import org.dawnsci.plotting.api.tool.IToolPage.ToolPageRole;
 import org.dawnsci.plotting.api.trace.IImageTrace;
 import org.dawnsci.plotting.api.trace.IPaletteTrace;
 import org.dawnsci.plotting.api.trace.ITrace;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.action.IContributionManager;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.wizard.IWizard;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.graphics.PaletteData;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.PlatformUI;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This class manages switching actions between different plotting system
  * modes.
  * 
  * @author fcp94556
  *
  */
 public class PlotActionsManagerImpl extends PlottingActionBarManager {
 
 	private static final Logger logger = LoggerFactory.getLogger(PlotActionsManagerImpl.class);
 
 	private PlottingSystemImpl        system;
 
 	protected PlotActionsManagerImpl(PlottingSystemImpl system) {
 		super(system);
 		this.system = system;
 	}
 
 	public IPlottingSystem getSystem() {
 		return system;
 	}
 
 	private static String lastscreeshot_filename;
 
 	public void createExportActions() {
 		final IAction exportActionDropDown = getExportActions();
 		registerToolBarGroup(ToolbarConfigurationConstants.EXPORT.getId());
 		registerAction(ToolbarConfigurationConstants.EXPORT.getId(), exportActionDropDown, ActionType.XYANDIMAGE, ManagerType.TOOLBAR);
 
 		registerMenuBarGroup("lightweight.plotting.print.action.menubar");
 		registerAction("lightweight.plotting.print.action.menubar", exportActionDropDown, ActionType.XYANDIMAGE, ManagerType.MENUBAR);
 	}
 
 	/**
 	 *  Create export and print buttons in tool bar 
 	 */
 	protected void createExportActions(IContributionManager toolbarManager) {
 		if (toolbarManager==null) return;
 		final IAction exportActionDropDown = getExportActions();
 		toolbarManager.add(exportActionDropDown);
 	}
 
 	private IAction getExportActions() {
 		
 		final MenuAction exportActionsDropDown = new MenuAction("Export/Print");
 		
 		Action exportSaveButton = new Action("Save plot screenshot as...", PlottingSystemActivator.getImageDescriptor("icons/picture_save.png")){
 			// Cache file name otherwise they have to keep
 			// choosing the folder.
 			public void run(){
 				try {
 					lastscreeshot_filename = system.savePlotting(lastscreeshot_filename);
 				} catch (Exception e) {
 					logger.error("Cannot save "+lastscreeshot_filename, e);
 					return;
 				}
 				exportActionsDropDown.setSelectedAction(this);
 			}
 		};
 
 		Action copyToClipboardButton = new Action("Copy to clip-board       Ctrl+C", PlottingSystemActivator.getImageDescriptor("icons/copy_edit_on.gif")) {
 			public void run() {
 				system.copyPlotting();
 				exportActionsDropDown.setSelectedAction(this);
 			}
 		};
 
 		Action snapShotButton = new Action("Print plot", PlottingSystemActivator.getImageDescriptor("icons/camera.gif")) {
 			public void run(){
 				system.printScaledPlotting();
 				exportActionsDropDown.setSelectedAction(this);
 			}
 		};
 		Action printButton = new Action("Print scaled plot         Ctrl+P", PlottingSystemActivator.getImageDescriptor("icons/printer.png")) {
 			public void run() {
 				system.printPlotting();
 				exportActionsDropDown.setSelectedAction(this);
 			}
 		};
 
 		final Action export = new Action("Export plot data to HDF5...", PlottingSystemActivator.getImageDescriptor("icons/mask-export-wiz.png")) {
 			public void run() {
 				try {
 					system.setFocus();
 					IWizard wiz = EclipseUtils.openWizard(PersistenceExportWizard.ID, false);
 					WizardDialog wd = new  WizardDialog(Display.getCurrent().getActiveShell(), wiz);
 					wd.setTitle(wiz.getWindowTitle());
 					wd.open();
 					exportActionsDropDown.setSelectedAction(this);
 				} catch (Exception e) {
 					logger.error("Problem opening export!", e);
 				}
 			}			
 		};
 		
 		final Action convert = new Action("Convert plot data to tiff/dat...", PlottingSystemActivator.getImageDescriptor("icons/convert.png")) {
 			public void run() {
 				try {
 					IWizard wiz = EclipseUtils.openWizard(PlotDataConversionWizard.ID, false);
 					WizardDialog wd = new  WizardDialog(Display.getCurrent().getActiveShell(), wiz);
 					wd.setTitle(wiz.getWindowTitle());
 					if (wiz instanceof PlotDataConversionWizard) ((PlotDataConversionWizard)wiz).setPlottingSystem(system);
 					wd.open();
 					exportActionsDropDown.setSelectedAction(this);
 				} catch (Exception e) {
 					logger.error("Problem opening convert!", e);
 				}
 			}			
 		};
 		
 		exportActionsDropDown.setImageDescriptor(PlottingSystemActivator.getImageDescriptor("icons/printer.png"));
 		exportActionsDropDown.setSelectedAction(printButton);
 		exportActionsDropDown.add(exportSaveButton);
 		exportActionsDropDown.add(copyToClipboardButton);
 		exportActionsDropDown.addSeparator();
 		exportActionsDropDown.add(snapShotButton);
 		exportActionsDropDown.add(printButton);
 		exportActionsDropDown.addSeparator();
 		exportActionsDropDown.add(export);
 		exportActionsDropDown.add(convert);
 		return exportActionsDropDown;
 	}
 
 	@Override
 	public void fillZoomActions(IContributionManager man) {
 
 		IContributionItem action = system.getActionBars().getToolBarManager().find(BasePlottingConstants.AUTO_SCALE);
 		if (action!=null) man.add(((ActionContributionItem)action).getAction());
 
 		for(final ZoomType zoomType : ZoomType.values()) {
 			action = system.getActionBars().getToolBarManager().find(zoomType.getId());
 			if (action!=null) man.add(((ActionContributionItem)action).getAction());
 		}
 	}
 
 
 	@Override
 	public void fillRegionActions(IContributionManager man) {
 			
 		IContributionItem action = system.getActionBars().getToolBarManager().find(BasePlottingConstants.ADD_REGION);
 		if (action!=null) man.add(((ActionContributionItem)action).getAction());
 		
 		action = system.getActionBars().getToolBarManager().find(BasePlottingConstants.REMOVE_REGION);
 		if (action!=null) man.add(((ActionContributionItem)action).getAction());
 	}
 
 	@Override
 	public void fillUndoActions(IContributionManager man) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	public void fillPrintActions(IContributionManager man) {
 		createExportActions(man);
 	}
 
 
 	@Override
 	public void fillAnnotationActions(IContributionManager man) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	public void fillToolActions(IContributionManager man, ToolPageRole role) {
 		// Find the drop down action for the role.
 		final IContributionItem action = system.getActionBars().getToolBarManager().find(role.getId());
 		if (action!=null) man.add(((ActionContributionItem)action).getAction());
 	}
 
 	private IMenuListener paletteMenuListener;
 
 	protected void createPalleteActions() {
 
 		final IPaletteService pservice = (IPaletteService)PlatformUI.getWorkbench().getService(IPaletteService.class);
 		final Collection<String> names = pservice.getColorSchemes();
 
 
 		String schemeName = PlottingSystemActivator.getPlottingPreferenceStore().getString(PlottingConstants.COLOUR_SCHEME);
 
 		final MenuAction lutCombo = new MenuAction("Color");
 		lutCombo.setId(getClass().getName()+lutCombo.getText());
 		lutCombo.setImageDescriptor(PlottingSystemActivator.getImageDescriptor("icons/color_wheel.png"));
 
 		final Map<String, IAction> paletteActions = new HashMap<String, IAction>(11);
 		CheckableActionGroup group      = new CheckableActionGroup();
 		for (final String paletteName : names) {
 			final Action action = new Action(paletteName, IAction.AS_CHECK_BOX) {
 				public void run() {
 					try {
 						PlottingSystemActivator.getPlottingPreferenceStore().setValue(PlottingConstants.COLOUR_SCHEME, paletteName);
 						final PaletteData data = pservice.getPaletteData(paletteName);
 						final Collection<ITrace> traces = system.getTraces();
 						if (traces!=null) for (ITrace trace: traces) {
 							if (trace instanceof IPaletteTrace) {
 								IPaletteTrace paletteTrace = (IPaletteTrace) trace;
 								paletteTrace.setPaletteData(data);
 								paletteTrace.setPaletteName(paletteName);
 							}
 						}
 					} catch (Exception ne) {
 						logger.error("Cannot create palette data!", ne);
 					}
 				}
 			};
 			action.setId(paletteName);
 			group.add(action);
 			lutCombo.add(action);
 			action.setChecked(paletteName.equals(schemeName));
 			paletteActions.put(paletteName, action);
 		}
 		lutCombo.setToolTipText("Histogram");
 
 		registerMenuBarGroup(lutCombo.getId()+".group");
		registerAction(lutCombo.getId()+".group", lutCombo, ActionType.ALL, ManagerType.MENUBAR);
 
 		this.paletteMenuListener = new IMenuListener() {
 			@Override
 			public void menuAboutToShow(IMenuManager manager) {
 				Collection<ITrace> traces = system.getTraces();
 				for (Iterator<ITrace> iterator = traces.iterator(); iterator.hasNext();) {
 					ITrace trace = (ITrace) iterator.next();
 					if (trace instanceof IImageTrace) {
 						IImageTrace image = (IImageTrace) trace;
 						String colorSchemeName = image.getPaletteName();
 						if (colorSchemeName!=null) {
 							IAction scheme = paletteActions.get(colorSchemeName);
 							if (scheme!=null) scheme.setChecked(true);
 						}
 					}
 				}
 			}
 		};
 		getActionBars().getMenuManager().addMenuListener(paletteMenuListener);
 	}
 
 	public void dispose() {
 		if (paletteMenuListener!=null && getActionBars()!=null) {
 			getActionBars().getMenuManager().removeMenuListener(paletteMenuListener);
 		}
 		super.dispose();
 	}
 }
