 package org.dawb.common.ui.plot;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.dawb.common.ui.Activator;
 import org.dawb.common.ui.menu.MenuAction;
 import org.dawb.common.ui.util.EclipseUtils;
 import org.dawb.common.ui.util.GridUtils;
 import org.dawb.common.ui.widgets.ActionBarWrapper;
 import org.dawb.common.ui.widgets.EmptyActionBars;
 import org.dawnsci.plotting.api.ActionType;
 import org.dawnsci.plotting.api.EmptyPageSite;
 import org.dawnsci.plotting.api.IPlotActionSystem;
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.ITraceActionProvider;
 import org.dawnsci.plotting.api.ManagerType;
 import org.dawnsci.plotting.api.PlotType;
 import org.dawnsci.plotting.api.tool.IToolChangeListener;
 import org.dawnsci.plotting.api.tool.IToolPage;
 import org.dawnsci.plotting.api.tool.IToolPage.ToolPageRole;
 import org.dawnsci.plotting.api.tool.ToolChangeEvent;
 import org.dawnsci.plotting.api.trace.ITrace;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IContributionManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StackLayout;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IViewReference;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PartInitException;
 import org.osgi.framework.Bundle;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * Class to deal with the actions we need in the plotting system.
  * 
  * This class provides generic actions which are read from extension points, such as tool actions.
  * 
  * This class provides switching between actions registered in different roles.
  * 
  * @author fcp94556
  *
  */
 public class PlottingActionBarManager implements IPlotActionSystem {
 
 	private static final Logger logger = LoggerFactory.getLogger(PlottingActionBarManager.class);
 	
 	// Extrac actions for 1D and image viewing
 	protected Map<String, IToolPage> toolPages;
 	protected AbstractPlottingSystem system;
 	protected Map<ActionType, List<ActionContainer>> actionMap;
 	protected MenuAction                imageMenu;
 	protected MenuAction                xyMenu;
 	protected ITraceActionProvider      traceActionProvider;
 	
 	public PlottingActionBarManager(AbstractPlottingSystem system) {
 		this.system = system;
 		this.actionMap = new HashMap<ActionType, List<ActionContainer>>(ActionType.values().length);
 	}
 	
 	private final static String defaultGroupName = "org.dawb.common.ui.plot.groupAll";
 	/**
      * 
      * @param traceActionProvider may be null
      */
 	public void init(ITraceActionProvider traceActionProvider) {
 		
 		system.getActionBars().getToolBarManager().add(new Separator(system.getPlotName()+"/"+defaultGroupName));
 		system.getActionBars().getMenuManager().add(new Separator(system.getPlotName()+"/"+defaultGroupName));
 		
 		xyMenu =  new MenuAction("X/Y Plot");
 		if (system.getActionBars()!=null) {
 			system.getActionBars().getMenuManager().add(xyMenu);
 			system.getActionBars().getMenuManager().add(new Separator());
 		}
 
 		imageMenu = new MenuAction("Image");
 		if (system.getActionBars()!=null) {
 			system.getActionBars().getMenuManager().add(imageMenu);
 			system.getActionBars().getMenuManager().add(new Separator());
 		}
 		
 		this.traceActionProvider = traceActionProvider;
 	}       
 
 	
 	private PlotType lastPlotTypeUpdate = null;
 	
 	public boolean switchActions(final PlotType type) {
 		
 		if (type == lastPlotTypeUpdate) return false;
 		lastPlotTypeUpdate = type;
 		
 		final IActionBars bars = system.getActionBars();
     	if (bars==null) return false;
     	
     	imageMenu.setEnabled(type==PlotType.IMAGE);
     	xyMenu.setEnabled(type.is1D());
 
     	for (ActionType actionType : ActionType.values()) {
     		
     		final List<ActionContainer> actions = actionMap.get(actionType);
         	if (actions!=null) for (ActionContainer ac : actions) {
         		if (actionType.isCompatible(type)) {
         			ac.insert(false);
         		} else {
         			ac.remove();
         		}
     		}
 
     	}
       	
     	if (bars.getToolBarManager()!=null)    bars.getToolBarManager().update(true);
     	if (bars.getMenuManager()!=null)       bars.getMenuManager().update(true);
     	if (bars.getStatusLineManager()!=null) bars.getStatusLineManager().update(true);
     	bars.updateActionBars();
     	
     	// If we are 1D we must deactivate 2D tools. If we are 
     	// 2D we must deactivate 1D tools.
     	IAction action = null;
     	if (type.is1D()) {
     		clearTool(ToolPageRole.ROLE_2D);
     		action = findAction(ToolPageRole.ROLE_1D.getId());
     	} else if (type.is2D()) {
     		clearTool(ToolPageRole.ROLE_1D);
     		action = findAction(ToolPageRole.ROLE_2D.getId());
     	}
     	
     	if (action instanceof MenuAction) {
     		((MenuAction)action).run();
     	}
     	
 		return true;
 	}
 	
 	public IActionBars createEmptyActionBars() {
 		return new EmptyActionBars();
 	}
 
 	public void dispose() {
 		
 		if (toolPages!=null) toolPages.clear();
 		toolPages = null;
 		
 	    actionMap.clear();
 	}
 
 	private boolean isToolsRequired = true;
 
 	public void setToolsRequired(boolean isToolsRequired) {
 		this.isToolsRequired = isToolsRequired;
 	}
 
 	public void createToolDimensionalActions(final ToolPageRole role,
 			                                 final String       viewId) {
 
 		final IActionBars bars = system.getActionBars();
 		if (bars!=null) {
 
 			try {
 				MenuAction toolSet = createToolActions(role, viewId);
 				if (toolSet==null) return;
 
 				if (role.is1D()&&!role.is2D()) {
 					final String groupName=role.getId()+".group1D";
 					registerToolBarGroup(groupName);
 					registerAction(groupName, toolSet, ActionType.XY);
 				}
 				if (role.is2D()&&!role.is1D()) {
 					final String groupName=role.getId()+".group2D";
 					registerToolBarGroup(groupName);
 					registerAction(groupName, toolSet, ActionType.IMAGE);
 				}
 				if (role.is3D()) {
 					final String groupName=role.getId()+".group3D";
 					registerToolBarGroup(groupName);
 					registerAction(groupName, toolSet, ActionType.THREED);
 				}
 
 				if (role.is2D()) {
 					toolSet.addActionsTo(imageMenu);
 					this.imageMenu.addSeparator();
 				}
 				if (role.is1D()) {
 					toolSet.addActionsTo(xyMenu);
 					this.xyMenu.addSeparator();
 				}
 
 			} catch (Exception e) {
 				logger.error("Reading extensions for plotting tools", e);
 			}
 		}	
 	}
 
 	/**
 	 * Return a MenuAction which can be attached to the part using the plotting system.
 	 * 
 	 * 
 	 * @return
 	 */
 	protected MenuAction createToolActions(final ToolPageRole role, final String viewId) throws Exception {
 		
 		if (!isToolsRequired) return null;
 		
 		final IWorkbenchPart part = system.getPart();
 		if (part==null)  return null;
 		
 		final MenuAction toolActions = new MenuAction(role.getLabel());
 		toolActions.setToolTipText(role.getTooltip());
 		toolActions.setImageDescriptor(Activator.getImageDescriptor(role.getImagePath()));
 		toolActions.setId(role.getId());
 	       	
 		// This list will not be large so we loop over it more than once for each ToolPageRole type
 	    final IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor("org.dawb.common.ui.toolPage");
 	    boolean foundSomeActions = false;
 	    for (final IConfigurationElement e : configs) {
 	    	
 	    	foundSomeActions = true;
 	    	
 	    	// Check if tool should not have action
 	    	if ("false".equals(e.getAttribute("visible"))) continue;
 
 	    	final IToolPage tool = createToolPage(e, role);
 	    	if (tool==null) continue;
 	    	
 	    	final Action    action= new Action(tool.getTitle()) {
 	    		public void run() {		
 	    			
 	    			IToolPage registeredTool = toolPages.get(tool.getToolId());
 	    			if (registeredTool==null || registeredTool.isDisposed()) registeredTool = createToolPage(e, role);
 	    			 
 	    			if (toolComposite!=null) {
 	    				createToolOnComposite(registeredTool);
 	    			} else {
 	    			    createToolOnView(registeredTool, viewId);
 	    			}
 	    			
 	    			final IToolPage old = system.getCurrentToolPage(role);
 	    			system.setCurrentToolPage(registeredTool);
 	    			system.clearRegionTool();
 	    			system.fireToolChangeListeners(new ToolChangeEvent(this, old, registeredTool, system.getPart()));
 	    			
 	    			toolActions.setSelectedAction(this);
 	    			
 	    		}
 	    	};
 	    	action.setId(e.getAttribute("id"));
 	    	final String   icon  = e.getAttribute("icon");
 	    	if (icon!=null) {
 		    	final String   id    = e.getContributor().getName();
 		    	final Bundle   bundle= Platform.getBundle(id);
 		    	final URL      entry = bundle.getEntry(icon);
 		    	final ImageDescriptor des = ImageDescriptor.createFromURL(entry);
 		    	action.setImageDescriptor(des);
 		    	tool.setImageDescriptor(des);
 	    	}
 	    	
 	    	final String    tooltip = e.getAttribute("tooltip");
 	    	if (tooltip!=null) action.setToolTipText(tooltip);
 	    	
 	    	toolActions.add(action);
 		}
 	
 	    if (!foundSomeActions) return null;
 	    
 	    if (toolActions.size()<1) return null; // Nothing to show!
 	    
      	final Action    clear = new Action("Clear tool") {
 
 			public void run() {		
     			clearTool(role);
     			toolActions.setSelectedAction(this);		
 			}
     	};
     	clear.setImageDescriptor(Activator.getImageDescriptor("icons/axis.png"));
     	clear.setToolTipText("Clear tool previously used if any.");
 	    toolActions.add(clear);
 
 	    return toolActions;
 	}
 	
 	private Composite             toolComposite;
 	private Map<String,Composite> toolPseudoRecs;
 
 	protected void createToolOnComposite(IToolPage registeredTool) {
 		
 		if (toolPseudoRecs==null) {
 			toolComposite.setLayout(new StackLayout());
 			toolPseudoRecs = new HashMap<String, Composite>(7);
 		}
 		
 		String toolId  = registeredTool.getToolId();
 		if (toolId!=null) {
 		
 			IToolPage tool = system.getToolPage(toolId);
 			IToolPage old  = system.getCurrentToolPage(tool.getToolPageRole());
 			if (!toolPseudoRecs.containsKey(tool.getToolId())) {
 				try {
 					final Composite toolPseudoRec = new Composite(toolComposite, SWT.NONE);
 					toolPseudoRec.setLayout(new GridLayout(1, false));
 					GridUtils.removeMargins(toolPseudoRec);
 					
 					ActionBarWrapper toolWrapper = ActionBarWrapper.createActionBars(toolPseudoRec, new EmptyActionBars());
 					tool.init(new EmptyPageSite(toolPseudoRec.getShell(), toolWrapper));
 					
 					final Composite toolContent = new Composite(toolPseudoRec, SWT.NONE);
 					toolContent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 					toolContent.setLayout(new FillLayout());
 					tool.createControl(toolContent);
 				
 					toolPseudoRecs.put(tool.getToolId(), toolPseudoRec);
 					 
 					toolWrapper.update(true);
 				} catch (PartInitException e) {
 					logger.error("Cannot init "+toolId, e);
 				}
 			}
 				
 			StackLayout stack = (StackLayout)toolComposite.getLayout();
 			stack.topControl = toolPseudoRecs.get(tool.getToolId());
 	   		toolComposite.layout();
 
 			if (old!=null && old.isActive()) old.deactivate();
 			tool.activate();
 			
 			system.fireToolChangeListeners(new ToolChangeEvent(this, old, tool, system.getPart()));		
 
 		}
 	}
 	
 	protected void createToolOnView(IToolPage registeredTool, String viewId) {
 		
 		// If we have a dedicated tool for this tool, we do not open another
 		String toolId  = registeredTool.getToolId();
 		if (toolId!=null) {
 			IViewReference ref = EclipseUtils.getPage().findViewReference("org.dawb.workbench.plotting.views.toolPageView.fixed", toolId);
 		    if (ref!=null) {
 		    	final IViewPart view = ref.getView(true);
 		    	EclipseUtils.getPage().activate(view);
 		    	return;
 		    }
 		    
 		    if (registeredTool.isAlwaysSeparateView()) {
 				try {
 					final IViewPart view = EclipseUtils.getPage().showView("org.dawb.workbench.plotting.views.toolPageView.fixed",
 							                                                toolId,
 																			IWorkbenchPage.VIEW_ACTIVATE);
 			    	EclipseUtils.getPage().activate(view);
 					return;
 				} catch (PartInitException e1) {
 					logger.error("Cannot open fixed view for "+toolId, e1);
 				}
                 
 		    }
 		}
 		
 		IViewPart viewPart=null;
 		try {
 			viewPart = EclipseUtils.getActivePage().showView(viewId);
 			
 			if (viewPart!=null && viewPart instanceof IToolChangeListener) {
 				system.addToolChangeListener((IToolChangeListener)viewPart);
 			}
 		} catch (PartInitException e) {
 			logger.error("Cannot find a view with id org.dawb.workbench.plotting.views.ToolPageView", e);
 		}
 
 	}
 	
 	protected void clearTool(ToolPageRole role) {
 		final IToolPage old = system.getCurrentToolPage(role);
 		
 		final EmptyTool empty = system.getEmptyTool(role);
 		system.setCurrentToolPage(empty);
 		system.clearRegionTool();
 		system.fireToolChangeListeners(new ToolChangeEvent(this, old, empty, system.getPart()));		
 	}
 
 
 	/**
 	 * 
 	 * @param e
 	 * @param role, may be null
 	 * @return
 	 */
 	private IToolPage createToolPage(IConfigurationElement e, ToolPageRole role) {
     	
 		IToolPage tool = null;
     	try {
     		tool  = (IToolPage)e.createExecutableExtension("class");
     	} catch (Throwable ne) {
     		logger.error("Cannot create tool page "+e.getAttribute("class"), ne);
     		return null;
     	}
     	if (role==null) role = tool.getToolPageRole();
     	if (tool.getToolPageRole()!=role) return null;
 	    
     	tool.setToolSystem(system);
     	tool.setPlottingSystem(system);
     	tool.setTitle(e.getAttribute("label"));
     	tool.setPart(system.getPart());
     	tool.setToolId(e.getAttribute("id"));
     	tool.setCheatSheetId(e.getAttribute("cheat_sheet_id"));
     	
     	// Save tool page
     	if (toolPages==null) toolPages = new HashMap<String, IToolPage>(7);
     	toolPages.put(tool.getToolId(), tool);
     	
     	return tool;
 	}
 
 	protected IToolPage getToolPage(final String id) {
 		if (toolPages==null) return null;
 		if (id==null)        return null;
 		IToolPage page = toolPages.get(id);
 		if (page==null) {
 		    final IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor("org.dawb.common.ui.toolPage");
 		    for (final IConfigurationElement e : configs) {
 		    	if (id.equals(e.getAttribute("id"))) {
 		    		page = createToolPage(e, null);
 		    		break;
 		    	}
 		    }
 			
 		}
 		return page;
 	}
 	
 	public void disposeToolPage(String id) throws Exception {
 		if (toolPages==null) return;
 		if (id==null)        return;
 		IToolPage page = toolPages.get(id);
 		if (page==null) return;
 		if (page.getControl()==null) return; // Already is a stub
 
 		IToolPage clone = page.cloneTool();
 		page.dispose();
 		toolPages.put(clone.getToolId(), clone);
 	}
 	
 	/**
 	 * returns false if no tool was shown
 	 * @param toolId
 	 * @return
 	 * @throws Exception 
 	 */
 	public boolean setToolVisible(final String toolId, final ToolPageRole role, final String viewId) throws Exception {
 	    
 		final IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor("org.dawb.common.ui.toolPage");
 	    for (IConfigurationElement e : configs) {
 			
 	    	if (!toolId.equals(e.getAttribute("id"))) continue;
 	    	final IToolPage page  = (IToolPage)e.createExecutableExtension("class");
 	    	if (page.getToolPageRole()!=role) continue;
 		    
 	    	final String    label = e.getAttribute("label");
 	    	page.setToolSystem(system);
 	    	page.setPlottingSystem(system);
 	    	page.setTitle(label);
 	    	page.setPart(system.getPart());
 	    	page.setToolId(toolId);
 	    	
 
 			if (toolComposite!=null) {
 				createToolOnComposite(page);
 			} else {
 			    createToolOnView(page, viewId);
 			}
 
 	    	final IToolPage old = system.getCurrentToolPage(role);
 	    	system.setCurrentToolPage(page);
 	    	system.clearRegionTool();
 	    	system.fireToolChangeListeners(new ToolChangeEvent(this, old, page, system.getPart()));
 	    			
 	    	return true;
 	    }
 	    
 	    return false;
 	}
 	
 
 	public void addXYAction(IAction a) {
 		xyMenu.add(a);
 	}
 	public void addXYSeparator() {
 		xyMenu.addSeparator();
 	}
 	public void addImageAction(IAction a) {
 		imageMenu.add(a);
 	}
 	public void addImageSeparator() {
 		imageMenu.addSeparator();
 	}
 	
 	public void registerToolBarGroup(final String groupName) {
 		registerGroup(groupName, ManagerType.TOOLBAR);
 	}
 
 	public void registerMenuBarGroup(final String groupName) {
 		registerGroup(groupName, ManagerType.MENUBAR);
 	}
 	
 	public void registerGroup(String groupName, ManagerType type) {
 		
 		groupName = system.getPlotName()+"/"+groupName;
 
 		if (getActionBars()!=null) {
 			IContributionManager man=null;
 			if (type==ManagerType.TOOLBAR) {
 				man = getActionBars().getToolBarManager();
 			} else {
 				man = getActionBars().getMenuManager();
 			}
 			if (man.find(groupName)!=null) {
 				man.remove(groupName);
 			}
 			final Separator group = new Separator(groupName);
 			man.add(group);
 		}
 	}
 	
 	public void registerAction(IAction action, ActionType actionType) {
 		registerAction(defaultGroupName,  action, actionType);
 	}
 	
 	public void registerAction(IAction action, ActionType actionType, ManagerType manType) {
 		registerAction(defaultGroupName,  action, actionType, manType);
 	}
 
 	/**
 	 * Registers with the toolbar
 	 * @param groupName
 	 * @param action
 	 * @return
 	 */
 	public void registerAction(String groupName, IAction action, ActionType actionType) {
 		registerAction(groupName, action, actionType, ManagerType.TOOLBAR);
 	}
 
 
 
 	public void registerAction(String groupName, IAction action, ActionType actionType, ManagerType manType) {
 
 		groupName = system.getPlotName()+"/"+groupName;
 		
 		// We generate an id!
 		if (action.getId()==null) {
 			action.setId(groupName+action.getText());
 		}
 		final IContributionManager man = manType==ManagerType.MENUBAR 
 				                       ? getActionBars().getMenuManager() 
 				                       : getActionBars().getToolBarManager();
 				                       
 		final ActionContainer ac = new ActionContainer(groupName, action, man);
 		List<ActionContainer> actions = actionMap.get(actionType);
 		if (actions==null) {
 			actions = new ArrayList<ActionContainer>(7);
 			actionMap.put(actionType, actions);
 		}
 		actions.add(ac);
 		ac.insert(true);
 	}
 
 	private IActionBars getActionBars() {
 		return system.getActionBars();
 	}
 
 
 	@Override
 	public void fillZoomActions(IContributionManager man) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	public void fillRegionActions(IContributionManager man) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	public void fillUndoActions(IContributionManager man) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	public void fillPrintActions(IContributionManager man) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	public void fillAnnotationActions(IContributionManager man) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	public void fillToolActions(IContributionManager man, ToolPageRole role) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void fillTraceActions(IContributionManager toolBarManager, ITrace trace, IPlottingSystem system) {
 		if (traceActionProvider!=null) traceActionProvider.fillTraceActions(toolBarManager, trace, system);
 	}
 
 	@Override
 	public void remove(String id) {
         //super.remove(id);
 
 		for (ActionType actionType : ActionType.values()) {
 
 			final List<ActionContainer> actions = actionMap.get(actionType);
 			if (actions!=null) {
 				for (Iterator<ActionContainer> it= actions.iterator(); it.hasNext(); ) {
 					ActionContainer ac = it.next();
 					if (ac.isId(id)) it.remove();
 				}
 			}
 
 		}
 
 		if (system.getActionBars()!=null) {
 			system.getActionBars().getToolBarManager().remove(id);
 			system.getActionBars().getMenuManager().remove(id);
 			system.getActionBars().getStatusLineManager().remove(id);
 		}
 	}
 	
 	/**
 	 * Returns the first action with the id
 	 * @param string
 	 * @return
 	 */
 	public IAction findAction(String id) {
 		for (ActionType actionType : ActionType.values()) {
 
 			final List<ActionContainer> actions = actionMap.get(actionType);
 			if (actions!=null) {
 				for (Iterator<ActionContainer> it= actions.iterator(); it.hasNext(); ) {
 					ActionContainer ac = it.next();
 					if (ac.isId(id)) return ac.getAction();
 				}
 			}
 
 		}
 		return null;
 	}
 
 	/**
 	 * Sets specific widget for using to show tools on rather than pages.
 	 * @param toolComposite
 	 */
 	public void setToolComposite(Composite toolComposite) {
         this.toolComposite = toolComposite;
 	}
 
 }
