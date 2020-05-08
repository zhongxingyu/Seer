 /******************************************************************************* 
  * Copyright (c) 2011, 2012 Red Hat, Inc. 
  *  All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  *
  * @author Innar Made
  ******************************************************************************/
 package org.eclipse.bpmn2.modeler.ui.diagram;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 
 import org.eclipse.bpmn2.BaseElement;
 import org.eclipse.bpmn2.Group;
 import org.eclipse.bpmn2.di.BPMNDiagram;
 import org.eclipse.bpmn2.modeler.core.features.CompoundCreateFeature;
 import org.eclipse.bpmn2.modeler.core.features.CompoundCreateFeaturePart;
 import org.eclipse.bpmn2.modeler.core.features.IBpmn2AddFeature;
 import org.eclipse.bpmn2.modeler.core.features.IBpmn2CreateFeature;
 import org.eclipse.bpmn2.modeler.core.features.ShowPropertiesFeature;
 import org.eclipse.bpmn2.modeler.core.features.activity.ActivitySelectionBehavior;
 import org.eclipse.bpmn2.modeler.core.features.event.EventSelectionBehavior;
 import org.eclipse.bpmn2.modeler.core.runtime.CustomTaskDescriptor;
 import org.eclipse.bpmn2.modeler.core.runtime.ModelDescriptor;
 import org.eclipse.bpmn2.modeler.core.runtime.ModelEnablementDescriptor;
 import org.eclipse.bpmn2.modeler.core.runtime.TargetRuntime;
 import org.eclipse.bpmn2.modeler.core.runtime.ToolPaletteDescriptor;
 import org.eclipse.bpmn2.modeler.core.runtime.ToolPaletteDescriptor.CategoryDescriptor;
 import org.eclipse.bpmn2.modeler.core.runtime.ToolPaletteDescriptor.ToolDescriptor;
 import org.eclipse.bpmn2.modeler.core.runtime.ToolPaletteDescriptor.ToolPart;
 import org.eclipse.bpmn2.modeler.core.utils.BusinessObjectUtil;
 import org.eclipse.bpmn2.modeler.core.utils.GraphicsUtil;
 import org.eclipse.bpmn2.modeler.core.utils.ModelUtil;
 import org.eclipse.bpmn2.modeler.core.utils.ModelUtil.Bpmn2DiagramType;
 import org.eclipse.bpmn2.modeler.core.validation.ValidationStatusAdapter;
 import org.eclipse.bpmn2.modeler.ui.Activator;
 import org.eclipse.bpmn2.modeler.ui.FeatureMap;
 import org.eclipse.bpmn2.modeler.ui.IConstants;
 import org.eclipse.bpmn2.modeler.ui.ImageProvider;
 import org.eclipse.bpmn2.modeler.ui.editor.BPMN2Editor;
 import org.eclipse.bpmn2.modeler.ui.features.activity.task.CustomTaskFeatureContainer;
 import org.eclipse.bpmn2.modeler.ui.features.choreography.ChoreographySelectionBehavior;
 import org.eclipse.bpmn2.modeler.ui.features.choreography.ChoreographyUtil;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.gef.Tool;
 import org.eclipse.gef.palette.PaletteDrawer;
 import org.eclipse.gef.palette.PaletteRoot;
 import org.eclipse.gef.palette.ToolEntry;
 import org.eclipse.graphiti.IExecutionInfo;
 import org.eclipse.graphiti.datatypes.ILocation;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.features.FeatureCheckerAdapter;
 import org.eclipse.graphiti.features.ICreateConnectionFeature;
 import org.eclipse.graphiti.features.ICreateFeature;
 import org.eclipse.graphiti.features.IFeature;
 import org.eclipse.graphiti.features.IFeatureAndContext;
 import org.eclipse.graphiti.features.IFeatureChecker;
 import org.eclipse.graphiti.features.IFeatureCheckerHolder;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.IContext;
 import org.eclipse.graphiti.features.context.IDoubleClickContext;
 import org.eclipse.graphiti.features.context.IPictogramElementContext;
 import org.eclipse.graphiti.features.context.impl.AddBendpointContext;
 import org.eclipse.graphiti.features.context.impl.AddContext;
 import org.eclipse.graphiti.features.context.impl.CreateConnectionContext;
 import org.eclipse.graphiti.features.context.impl.CreateContext;
 import org.eclipse.graphiti.features.context.impl.CustomContext;
 import org.eclipse.graphiti.features.context.impl.MoveBendpointContext;
 import org.eclipse.graphiti.features.context.impl.MoveShapeContext;
 import org.eclipse.graphiti.features.context.impl.UpdateContext;
 import org.eclipse.graphiti.features.custom.ICustomFeature;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.algorithms.Polyline;
 import org.eclipse.graphiti.mm.algorithms.Text;
 import org.eclipse.graphiti.mm.algorithms.styles.Point;
 import org.eclipse.graphiti.mm.pictograms.Anchor;
 import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
 import org.eclipse.graphiti.mm.pictograms.Connection;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.FreeFormConnection;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.palette.IPaletteCompartmentEntry;
 import org.eclipse.graphiti.palette.IToolEntry;
 import org.eclipse.graphiti.palette.impl.ConnectionCreationToolEntry;
 import org.eclipse.graphiti.palette.impl.ObjectCreationToolEntry;
 import org.eclipse.graphiti.palette.impl.PaletteCompartmentEntry;
 import org.eclipse.graphiti.platform.IPlatformImageConstants;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.tb.ContextButtonEntry;
 import org.eclipse.graphiti.tb.DefaultToolBehaviorProvider;
 import org.eclipse.graphiti.tb.IContextButtonPadData;
 import org.eclipse.graphiti.tb.IDecorator;
 import org.eclipse.graphiti.tb.IImageDecorator;
 import org.eclipse.graphiti.tb.ImageDecorator;
 import org.eclipse.graphiti.ui.editor.DiagramBehavior;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.widgets.Display;
 
 public class BpmnToolBehaviourFeature extends DefaultToolBehaviorProvider implements IFeatureCheckerHolder {
 
 	public final static String DEFAULT_PALETTE_ID = "org.bpmn2.modeler.toolpalette.default.categories";
 	
 	BPMNFeatureProvider featureProvider;
 	ModelEnablementDescriptor modelEnablements;
 	ModelDescriptor modelDescriptor;
 	Hashtable<String, PaletteCompartmentEntry> categories = new Hashtable<String, PaletteCompartmentEntry>();
 	List<IPaletteCompartmentEntry> palette;
 	
 	protected class ProfileSelectionToolEntry extends ToolEntry {
 		BPMN2Editor editor;
 		
 		ProfileSelectionToolEntry(BPMN2Editor editor, String label) {
 			super(label, null, null, null, null);
 			this.editor = editor;
 		}
 		
 		public Tool createTool() {
 			String profile = getLabel();
 			editor.setModelEnablementProfile(profile);
 			Display.getDefault().asyncExec(new Runnable() {
 
 				@Override
 				public void run() {
 					editor.updatePalette();
 				}
 				
 			});
 			return null;
 		}
 
 		@Override
 		public ImageDescriptor getLargeIcon() {
 			return super.getSmallIcon();
 		}
 
 		@Override
 		public ImageDescriptor getSmallIcon() {
 			String profile = editor.getModelEnablementProfile();
 			if (getLabel().equals(profile))
 				return Activator.getDefault().getImageDescriptor(IConstants.ICON_CHECKBOX_CHECKED_16);
 			return Activator.getDefault().getImageDescriptor(IConstants.ICON_CHECKBOX_UNCHECKED_16);
 		}
 	}
 	
 	public BpmnToolBehaviourFeature(IDiagramTypeProvider diagramTypeProvider) {
 		super(diagramTypeProvider);
 	}
 
 	public void createPaletteProfilesGroup(BPMN2Editor editor, PaletteRoot paletteRoot) {
 		TargetRuntime rt = editor.getTargetRuntime();
 		PaletteDrawer drawer = new PaletteDrawer("Profiles", null);
 		int size = 0;
 		Bpmn2DiagramType diagramType = ModelUtil.getDiagramType(editor);
 
 		for (ModelEnablementDescriptor med : rt.getModelEnablements(diagramType)) {
 			String profile = med.getProfile();
 			if (profile==null)
 				profile = "Unnamed "+(size+1);
 			drawer.add(new ProfileSelectionToolEntry(editor, profile));
 			++size;
 		}
 		if (size>1) {
 			drawer.setInitialState(PaletteDrawer.INITIAL_STATE_CLOSED);
 			paletteRoot.add(1,drawer);
 		}
 	}
 	
 	@Override
 	public IPaletteCompartmentEntry[] getPalette() {
 
 		BPMN2Editor editor = (BPMN2Editor)getDiagramTypeProvider().getDiagramEditor();
 		Diagram diagram = getDiagramTypeProvider().getDiagram();
 		EObject object = Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement(diagram);
 		
 		palette = new ArrayList<IPaletteCompartmentEntry>();
 
 		if (object!=null) {
 			Bpmn2DiagramType diagramType = ModelUtil.getDiagramType(object);
 			String profile = editor.getModelEnablementProfile();
 			TargetRuntime rt = editor.getTargetRuntime();
 			modelEnablements = rt.getModelEnablements(diagramType, profile);
 			featureProvider = (BPMNFeatureProvider)getFeatureProvider();
 			modelDescriptor = rt.getModelDescriptor();
 			
 			PaletteCompartmentEntry compartmentEntry = null;
 			categories.clear();
 			ToolPaletteDescriptor toolPaletteDescriptor = rt.getToolPalette(diagramType, profile);
 			if (toolPaletteDescriptor!=null) {
 				for (CategoryDescriptor category : toolPaletteDescriptor.getCategories()) {
 					if (DEFAULT_PALETTE_ID.equals(category.getId())) {
 						createDefaultpalette();
 						continue;
 					}
 					
 					category = getRealCategory(rt, category);
 					compartmentEntry = categories.get(category.getName());
 					for (ToolDescriptor tool : category.getTools()) {
 						tool = getRealTool(rt, tool);
 						IFeature feature = getCreateFeature(tool);
 						if (feature!=null) {
 							if (compartmentEntry==null) {
 								compartmentEntry = new PaletteCompartmentEntry(category.getName(), category.getIcon());
 								compartmentEntry.setInitiallyOpen(false);
 								categories.put(category.getName(), compartmentEntry);
 							}
 							createEntry(feature, compartmentEntry);
 						}
 					}
 					// if there are no tools defined for this category, check if it will be
 					// used for only Custom Tasks. If so, create the category anyway.
 					if (compartmentEntry==null) {
 						for (CustomTaskDescriptor tc : rt.getCustomTasks()) {
 							if (category.getName().equals(tc.getCategory())) {
 								compartmentEntry = new PaletteCompartmentEntry(category.getName(), category.getIcon());
 								compartmentEntry.setInitiallyOpen(false);
 								categories.put(category.getName(), compartmentEntry);
 								palette.add(compartmentEntry);
 								break;
 							}
 						}
 					}
 					else if (compartmentEntry.getToolEntries().size()>0)
 						palette.add(compartmentEntry);
 				}
 				createCustomTasks(palette);
 			}
 			else
 			{
 				// create a default toolpalette
 				createDefaultpalette();
 			}
 		}
 		
 		return palette.toArray(new IPaletteCompartmentEntry[palette.size()]);
 	}
 	
 	private CategoryDescriptor getRealCategory(TargetRuntime rt, CategoryDescriptor category) {
 		String fromPalette = category.getFromPalette();
 		String id = category.getId();
 		if (fromPalette!=null && id!=null) {
 			for (TargetRuntime otherRt : TargetRuntime.getAllRuntimes()) {
 				if (otherRt!=rt) {
 					for (ToolPaletteDescriptor tp : otherRt.getToolPalettes()) {
 						if ( fromPalette.equals(tp.getId())) {
 							for (CategoryDescriptor c : tp.getCategories()) {
 								if (id.equals(c.getId())) {
 									return c;
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		return category;
 	}
 	
 	private ToolDescriptor getRealTool(TargetRuntime rt, ToolDescriptor tool) {
 		String fromPalette = tool.getFromPalette();
 		String id = tool.getId();
 		if (fromPalette!=null && id!=null) {
 			for (TargetRuntime otherRt : TargetRuntime.getAllRuntimes()) {
 				if (otherRt!=rt) {
 					for (ToolPaletteDescriptor tp : otherRt.getToolPalettes()) {
 						if ( fromPalette.equals(tp.getId())) {
 							for (CategoryDescriptor c : tp.getCategories()) {
 								for (ToolDescriptor t : c.getTools()) {
 									if (id.equals(t.getId())) {
 										return t;
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		return tool;
 	}
 	
 	private void createDefaultpalette() {
 		createConnectors(palette);
 		createTasksCompartments(palette);
 		createGatewaysCompartments(palette);
 		createEventsCompartments(palette);
 		createEventDefinitionsCompartments(palette);
 		createDataCompartments(palette);
 		createOtherCompartments(palette);
 		createCustomTasks(palette);
 	}
 	
 	public List<IToolEntry> getTools() {
 		List<IToolEntry> tools = new ArrayList<IToolEntry>();
 		if (palette==null)
 			getPalette();
 		
 		for (IPaletteCompartmentEntry ce : palette) {
 			for (IToolEntry te : ce.getToolEntries()) {
 				tools.add(te);
 			}
 		}
 		return tools;
 	}
 
 	public IPaletteCompartmentEntry getCategory(IToolEntry tool) {
 		if (palette==null)
 			getPalette();
 		
 		for (IPaletteCompartmentEntry ce : palette) {
 			for (IToolEntry te : ce.getToolEntries()) {
 				if (te == tool)
 					return ce;
 			}
 		}
 		return null;
 	}
 	
 	private IFeature getCreateFeature(ToolDescriptor tool) {
 		if (tool.getToolParts().size()==1)
 			return getCreateFeature(tool, null, null, tool.getToolParts().get(0));
 		else {
 			CompoundCreateFeature compoundFeature = null;
 			for (ToolPart tp : tool.getToolParts()) {
 				if (compoundFeature==null)
 					compoundFeature = new CompoundCreateFeature(featureProvider,tool);
 				getCreateFeature(tool, compoundFeature, null, tp);
 			}
 			return compoundFeature;
 		}
 	}
 	
 	private IFeature getCreateFeature(ToolDescriptor tool, CompoundCreateFeature root, CompoundCreateFeaturePart node, ToolPart toolPart) {
 		IFeature parentFeature = null;
 		String name = toolPart.getName();
 		EClassifier eClass = modelDescriptor.getClassifier(name);
 		if (eClass!=null) {
 			parentFeature = featureProvider.getCreateFeatureForBusinessObject(eClass.getInstanceClass());
 		}
 		if (root!=null) {
 			if (node!=null) {
 				CompoundCreateFeaturePart n = node.addChild(parentFeature);
 				if (toolPart.hasProperties()) {
 					n.setProperties(toolPart.getProperties());
 				}
 			}
 			else {
 				node = root.addChild(parentFeature);
 				if (toolPart.hasProperties()) {
 					node.setProperties(toolPart.getProperties());
 				}
 			}
 		}
 		else if (toolPart.hasProperties()) {
 			root = new CompoundCreateFeature(featureProvider, tool);
 			node = root.addChild(parentFeature);
 			node.setProperties(toolPart.getProperties());
 			parentFeature = root;
 		}
 		
 		for (ToolPart childToolPart : toolPart.getChildren()) {
 			if (root==null) {
 				root = new CompoundCreateFeature(featureProvider, tool);
 				node = root.addChild(parentFeature);
 				parentFeature = root;
 			}
 			getCreateFeature(tool, root, node, childToolPart);
 		}
 		
 		return parentFeature;
 	}
 	
 	private void createEventsCompartments(List<IPaletteCompartmentEntry> palette) {
 		PaletteCompartmentEntry compartmentEntry = new PaletteCompartmentEntry("Events", null);
 
 		createEntries(FeatureMap.EVENTS, compartmentEntry);
 
 		if (compartmentEntry.getToolEntries().size()>0)
 			palette.add(compartmentEntry);
 	}
 
 	private void createOtherCompartments(List<IPaletteCompartmentEntry> palette) {
 		PaletteCompartmentEntry compartmentEntry = new PaletteCompartmentEntry("Other", null);
 		compartmentEntry.setInitiallyOpen(false);
 
 		createEntries(FeatureMap.OTHER, compartmentEntry);
 
 		if (compartmentEntry.getToolEntries().size()>0)
 			palette.add(compartmentEntry);
 	}
 
 	private void createDataCompartments(List<IPaletteCompartmentEntry> palette) {
 		PaletteCompartmentEntry compartmentEntry = new PaletteCompartmentEntry("Data Items", null);
 		compartmentEntry.setInitiallyOpen(false);
 
 		createEntries(FeatureMap.DATA, compartmentEntry);
 
 		if (compartmentEntry.getToolEntries().size()>0)
 			palette.add(compartmentEntry);
 	}
 
 	private void createEventDefinitionsCompartments(List<IPaletteCompartmentEntry> palette) {
 		PaletteCompartmentEntry compartmentEntry = new PaletteCompartmentEntry("Event Definitions", null);
 		compartmentEntry.setInitiallyOpen(false);
 
 		createEntries(FeatureMap.EVENT_DEFINITIONS, compartmentEntry);
 
 		if (compartmentEntry.getToolEntries().size()>0)
 			palette.add(compartmentEntry);
 	}
 
 	private void createGatewaysCompartments(List<IPaletteCompartmentEntry> palette) {
 		PaletteCompartmentEntry compartmentEntry = new PaletteCompartmentEntry("Gateways", null);
 
 		createEntries(FeatureMap.GATEWAYS, compartmentEntry);
 
 		if (compartmentEntry.getToolEntries().size()>0)
 			palette.add(compartmentEntry);
 	}
 
 	private void createTasksCompartments(List<IPaletteCompartmentEntry> palette) {
 		PaletteCompartmentEntry compartmentEntry = new PaletteCompartmentEntry("Tasks", null);
 
 		createEntries(FeatureMap.TASKS, compartmentEntry);
 
 		if (compartmentEntry.getToolEntries().size()>0)
 			palette.add(compartmentEntry);
 	}
 
 	private void createConnectors(List<IPaletteCompartmentEntry> palette) {
 		PaletteCompartmentEntry compartmentEntry = new PaletteCompartmentEntry("Connectors", null);
 
 		createEntries(FeatureMap.CONNECTORS, compartmentEntry);
 
 		if (compartmentEntry.getToolEntries().size()>0)
 			palette.add(compartmentEntry);
 	}
 
 	private void createEntries(List<Class> neededEntries, PaletteCompartmentEntry compartmentEntry) {
 		for (Object o : neededEntries) {
 			if (o instanceof Class) {
 				createEntry((Class)o, compartmentEntry);
 			}
 		}
 	}
 	
 	private void createEntry(Class c, PaletteCompartmentEntry compartmentEntry) {
 		if (modelEnablements.isEnabled(c.getSimpleName())) {
 			IFeature feature = featureProvider.getCreateFeatureForBusinessObject(c);
 			if (feature instanceof ICreateFeature) {
 				ICreateFeature cf = (ICreateFeature)feature;
 				ObjectCreationToolEntry objectCreationToolEntry = new ObjectCreationToolEntry(cf.getCreateName(),
 					cf.getCreateDescription(), cf.getCreateImageId(), cf.getCreateLargeImageId(), cf);
 				compartmentEntry.addToolEntry(objectCreationToolEntry);
 			}
 			else if (feature instanceof ICreateConnectionFeature) {
 				ICreateConnectionFeature cf = (ICreateConnectionFeature)feature;
 				ConnectionCreationToolEntry connectionCreationToolEntry = new ConnectionCreationToolEntry(
 						cf.getCreateName(), cf.getCreateDescription(), cf.getCreateImageId(),
 						cf.getCreateLargeImageId());
 				connectionCreationToolEntry.addCreateConnectionFeature(cf);
 				compartmentEntry.addToolEntry(connectionCreationToolEntry);
 			}
 		}
 	}
 	
 	private void createEntry(IFeature feature, PaletteCompartmentEntry compartmentEntry) {
 		if (modelEnablements.isEnabled(feature) || feature instanceof CompoundCreateFeature) {
 			IFeature targetFeature = feature;
 			if (feature instanceof CompoundCreateFeature) {
 				CompoundCreateFeature cf = (CompoundCreateFeature)feature;
 				targetFeature = ((CompoundCreateFeaturePart)cf.getChildren().get(0)).getFeature();
 			}
 			if (targetFeature instanceof ICreateFeature) {
 				ICreateFeature cf = (ICreateFeature)feature;
 				ObjectCreationToolEntry objectCreationToolEntry = new ObjectCreationToolEntry(cf.getCreateName(),
 					cf.getCreateDescription(), cf.getCreateImageId(), cf.getCreateLargeImageId(), cf);
 				compartmentEntry.addToolEntry(objectCreationToolEntry);
 			}
 			else if (targetFeature instanceof ICreateConnectionFeature) {
 				ICreateConnectionFeature cf = (ICreateConnectionFeature)feature;
 				ConnectionCreationToolEntry connectionCreationToolEntry = new ConnectionCreationToolEntry(
 						cf.getCreateName(), cf.getCreateDescription(), cf.getCreateImageId(),
 						cf.getCreateLargeImageId());
 				connectionCreationToolEntry.addCreateConnectionFeature(cf);
 				compartmentEntry.addToolEntry(connectionCreationToolEntry);
 			}
 		}
 	}
 
 	private void createCustomTasks(List<IPaletteCompartmentEntry> ret) {
 
 		PaletteCompartmentEntry compartmentEntry = null;
 		BPMN2Editor editor = (BPMN2Editor) getDiagramTypeProvider().getDiagramEditor();
 		TargetRuntime rt = editor.getTargetRuntime();
 		
 		try {
 			for (IPaletteCompartmentEntry e : ret) {
 				categories.put(e.getLabel(), (PaletteCompartmentEntry) e);
 			}
 			
 			for (CustomTaskDescriptor tc : rt.getCustomTasks()) {
 				CustomTaskFeatureContainer container = (CustomTaskFeatureContainer)tc.getFeatureContainer();
 				if (!container.isAvailable(featureProvider))
 					continue;
 
 				String id = tc.getId();
 				container.setId(id);
 				featureProvider.addFeatureContainer(id, container);
 				ICreateFeature cf = container.getCreateFeature(featureProvider);
 				ObjectCreationToolEntry objectCreationToolEntry = new ObjectCreationToolEntry(tc.getName(),
 						cf.getCreateDescription(), cf.getCreateImageId(), cf.getCreateLargeImageId(), cf);
 				
 				String category = tc.getCategory();
 				if (category==null || category.isEmpty())
 					category = "Custom Tasks";
 				
 				compartmentEntry = categories.get(category);
 				if (compartmentEntry==null) {
 					compartmentEntry = new PaletteCompartmentEntry(category, null);
 					compartmentEntry.setInitiallyOpen(false);
 					ret.add(compartmentEntry);
 					categories.put(category, compartmentEntry);
 				}
 				
 				compartmentEntry.addToolEntry(objectCreationToolEntry);
 			}
 		} catch (Exception ex) {
 			Activator.logError(ex);
 		}
 	}
 
 	@Override
 	public IFeatureChecker getFeatureChecker() {
 		return new FeatureCheckerAdapter(false) {
 			@Override
 			public boolean allowAdd(IContext context) {
 				return super.allowAdd(context);
 			}
 
 			@Override
 			public boolean allowCreate() {
 				return super.allowCreate();
 			}
 		};
 	}
 
 	@Override
 	public GraphicsAlgorithm[] getClickArea(PictogramElement pe) {
 		if (ActivitySelectionBehavior.canApplyTo(pe)) {
 			return ActivitySelectionBehavior.getClickArea(pe);
 		} else if (EventSelectionBehavior.canApplyTo(pe)) {
 			return EventSelectionBehavior.getClickArea(pe);
 		} else if (ChoreographySelectionBehavior.canApplyTo(pe)) {
 			return ChoreographySelectionBehavior.getClickArea(pe);
 		}
 		else {
 			if (pe instanceof ContainerShape) {
 				BaseElement be = BusinessObjectUtil.getFirstBaseElement((ContainerShape)pe);
 				if (be instanceof Group) {
 					System.out.println();
 				}
 			}
 		}
 		return super.getClickArea(pe);
 	}
 
 	@Override
 	public int getLineSelectionWidth(Polyline polyline) {
 		PictogramElement pe = polyline.getPictogramElement();
 		if (pe!=null && BusinessObjectUtil.getFirstBaseElement(pe) instanceof Group)
 			return 20;
 		return super.getLineSelectionWidth(polyline);
 	}
 
 	@Override
 	public GraphicsAlgorithm getSelectionBorder(PictogramElement pe) {
 		if (ActivitySelectionBehavior.canApplyTo(pe)) {
 			return ActivitySelectionBehavior.getSelectionBorder(pe);
 		} else if (EventSelectionBehavior.canApplyTo(pe)) {
 			return EventSelectionBehavior.getSelectionBorder(pe);
 		} else if (ChoreographySelectionBehavior.canApplyTo(pe)) {
 			return ChoreographySelectionBehavior.getSelectionBorder(pe);
 		}
 		else if (pe instanceof ContainerShape) {
 			if (((ContainerShape)pe).getChildren().size()>0) {
 				GraphicsAlgorithm ga = ((ContainerShape)pe).getChildren().get(0).getGraphicsAlgorithm();
 				if (!(ga instanceof Text) && !(ga instanceof Polyline))
 					return ga;
 				ga = ((ContainerShape)pe).getGraphicsAlgorithm();
 				if (ga.getGraphicsAlgorithmChildren().size()>0)
 					return ga.getGraphicsAlgorithmChildren().get(0);
 				return ga;
 			}
 		}
 		else if (pe instanceof Shape) {
 			return ((Shape)pe).getGraphicsAlgorithm();
 		}
 		return super.getSelectionBorder(pe);
 	}
 
 	public static Point getMouseLocation(IFeatureProvider fp) {
 		DiagramBehavior db = (DiagramBehavior) fp.getDiagramTypeProvider().getDiagramBehavior();
 		org.eclipse.draw2d.geometry.Point p = db.getMouseLocation();
 		p = db.calculateRealMouseLocation(p);
 		Point point = GraphicsUtil.createPoint(p.x, p.y);
 		return point;
 	}
 	
 	@Override
 	public IContextButtonPadData getContextButtonPad(IPictogramElementContext context) {
 		IContextButtonPadData data = super.getContextButtonPad(context);
 		PictogramElement pe = context.getPictogramElement();
 		IFeatureProvider fp = getFeatureProvider();
 
 		String labelProperty = Graphiti.getPeService().getPropertyValue(pe, GraphicsUtil.LABEL_PROPERTY);
 		if (Boolean.parseBoolean(labelProperty)) {
 			// labels don't have a buttonpad
 			setGenericContextButtons(data, pe, 0);
 			return data;
 		}
 
 		if( pe.getGraphicsAlgorithm()!= null && pe.getGraphicsAlgorithm().getWidth() < 40 ){
 		    ILocation origin = getAbsoluteLocation(pe.getGraphicsAlgorithm());
 		    data.getPadLocation().setRectangle(origin.getX(), origin.getY(), 40, 40);
 		}
 		
 		// 1. set the generic context buttons
 		// Participant bands can only be removed from the choreograpy task
 		int genericButtons = CONTEXT_BUTTON_DELETE;
 		if (ChoreographyUtil.isChoreographyParticipantBand(pe)) {
 			genericButtons |= CONTEXT_BUTTON_REMOVE;
 		}
 		setGenericContextButtons(data, pe, genericButtons);
 
 		// 2. set the expand & collapse buttons
 		CustomContext cc = new CustomContext(new PictogramElement[] { pe });
 		for (ICustomFeature cf : fp.getCustomFeatures(cc)) {
 			if (cf.canExecute(cc)) {
 				ContextButtonEntry button = new ContextButtonEntry(cf, cc);
 				button.setText(cf.getName()); //$NON-NLS-1$
 				button.setIconId(cf.getImageId());
 				button.setDescription(cf.getDescription());
 				
 				data.getDomainSpecificContextButtons().add(button);
 			}
 		}
 
 		// 3. add one domain specific context-button, which offers all
 		// available connection-features as drag&drop features...
 
 		// 3.a. create new CreateConnectionContext
 		CreateConnectionContext ccc = new CreateConnectionContext();
 		ccc.setSourcePictogramElement(pe);
 		Anchor anchor = null;
 		if (pe instanceof Anchor) {
 			anchor = (Anchor) pe;
 		} else if (pe instanceof AnchorContainer) {
 			// assume, that our shapes always have chopbox anchors
 			anchor = Graphiti.getPeService().getChopboxAnchor((AnchorContainer) pe);
 		}
 		ccc.setSourceAnchor(anchor);
 
 		// 3.b. create context button and add "Create Connections" feature
 		ContextButtonEntry button = new ContextButtonEntry(null, context);
 		button.setText("Create Connection"); //$NON-NLS-1$
 		String description = null;
 		ArrayList<String> names = new ArrayList<String>();
 		button.setIconId(ImageProvider.IMG_16_SEQUENCE_FLOW);
 		for (IToolEntry te : getTools()) {
 			if (te instanceof ConnectionCreationToolEntry) {
 				ConnectionCreationToolEntry cte = (ConnectionCreationToolEntry)te;
 				for (IFeature f : cte.getCreateConnectionFeatures()) {
 					ICreateConnectionFeature ccf = (ICreateConnectionFeature)f;
 					if (ccf.isAvailable(ccc) && ccf.canStartConnection(ccc)) {
 						button.addDragAndDropFeature(ccf);
 						names.add(ccf.getCreateName());
 					}
 				}
 			}
 		}
 		
 		// 3.c. build a reasonable description for the context button action 
 		for (int i=0; i<names.size(); ++i) {
 			if (description==null)
 				description = "Click and drag to create a\n";
 			description += names.get(i);
 			if (i+2 == names.size())
 				description += " or ";
 			else if (i+1 < names.size())
 				description += ", ";
 		}
 		button.setDescription(description);
 
 		// 3.d. add context button, button only if it contains at least one feature
 		if (button.getDragAndDropFeatures().size() > 0) {
 			data.getDomainSpecificContextButtons().add(button);
 		}
 
 		return data;
 	}
 
 	@Override
 	public void postExecute(IExecutionInfo executionInfo) {
 		BPMN2Editor editor = (BPMN2Editor)getDiagramTypeProvider().getDiagramEditor();
 		for (IFeatureAndContext fc : executionInfo.getExecutionList()) {
 			IContext context = fc.getContext();
 			IFeature feature = fc.getFeature();
 			if (context instanceof AddContext) {
 				if (feature instanceof IBpmn2AddFeature) {
 					((IBpmn2AddFeature)feature).postExecute(executionInfo);
 				}
 			}
 			else if (context instanceof CreateContext) {
 				if (feature instanceof IBpmn2CreateFeature) {
 					((IBpmn2CreateFeature)feature).postExecute(executionInfo);
 				}
 			}
 			else if (context instanceof UpdateContext) {
 				PictogramElement pe = ((UpdateContext)context).getPictogramElement();
 				if (!(pe instanceof Connection)) {
 					editor.setPictogramElementForSelection(pe);
 				}
 				editor.refresh();
 			}
 			else if (context instanceof MoveShapeContext) {
 				PictogramElement pe = ((MoveShapeContext)context).getPictogramElement();
 				editor.setPictogramElementForSelection(pe);
 				editor.refresh();
 			}
 			else if (context instanceof AddBendpointContext) {
 				PictogramElement pe = ((AddBendpointContext)context).getConnection();
 				editor.setPictogramElementForSelection(pe);
 				editor.refresh();
 			}
 			else if (context instanceof MoveBendpointContext) {
 				PictogramElement pe = ((MoveBendpointContext)context).getConnection();
 				editor.setPictogramElementForSelection(pe);
 				editor.refresh();
 			}
 		}
 	}
 
 	@Override
 	public ICustomFeature getDoubleClickFeature(IDoubleClickContext context) {
 		ICustomFeature[] cf = getFeatureProvider().getCustomFeatures(context);
 		for (int i = 0; i < cf.length; i++) {
 			ICustomFeature iCustomFeature = cf[i];
 			if (iCustomFeature instanceof ShowPropertiesFeature &&
 					iCustomFeature.canExecute(context)) {
 				return iCustomFeature;
 			}
 		}
 		// temp debugging stuff to dump connection routing info
 		for (PictogramElement pe : context.getPictogramElements()) {
 			String id = Graphiti.getPeService().getPropertyValue(pe, "ROUTING_NET_CONNECTION");
 			System.out.println("id="+id);
 			if (pe instanceof FreeFormConnection) {
 				FreeFormConnection c = (FreeFormConnection)pe;
 				int i=0;
 				ILocation loc = Graphiti.getPeService().getLocationRelativeToDiagram(c.getStart());
 				System.out.println("0: "+loc.getX()+","+loc.getY());
 				for (Point p : c.getBendpoints()) {
 					System.out.println(++i+": "+p.getX()+","+p.getY());
 				}
 				loc = Graphiti.getPeService().getLocationRelativeToDiagram(c.getEnd());
 				System.out.println(++i+": "+loc.getX()+","+loc.getY());
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public GraphicsAlgorithm getChopboxAnchorArea(PictogramElement pe) {
 		return super.getChopboxAnchorArea(pe);
 	}
 
     @Override
     public IDecorator[] getDecorators(PictogramElement pe) {
         List<IDecorator> decorators = new ArrayList<IDecorator>();
 
         // labels should not be decorated
 		String labelProperty = Graphiti.getPeService().getPropertyValue(pe, GraphicsUtil.LABEL_PROPERTY);
 		if (!Boolean.parseBoolean(labelProperty)) {
 	        IFeatureProvider featureProvider = getFeatureProvider();
 	        Object bo = featureProvider.getBusinessObjectForPictogramElement(pe);
 	        if (bo!=null) {
 		        ValidationStatusAdapter statusAdapter = (ValidationStatusAdapter) EcoreUtil.getRegisteredAdapter((EObject) bo,
 		                ValidationStatusAdapter.class);
 		        if (statusAdapter != null) {
 		            final IImageDecorator decorator;
 		            final IStatus status = statusAdapter.getValidationStatus();
 		            switch (status.getSeverity()) {
 		            case IStatus.INFO:
 		                decorator = new ImageDecorator(IPlatformImageConstants.IMG_ECLIPSE_INFORMATION_TSK);
 		                break;
 		            case IStatus.WARNING:
 		                decorator = new ImageDecorator(IPlatformImageConstants.IMG_ECLIPSE_WARNING_TSK);
 		                break;
 		            case IStatus.ERROR:
 		                decorator = new ImageDecorator(IPlatformImageConstants.IMG_ECLIPSE_ERROR_TSK);
 		                break;
 		            default:
 		                decorator = null;
 		                break;
 		            }
 		            if (decorator != null) {
 		                GraphicsAlgorithm ga = getSelectionBorder(pe);
 		                if (ga == null) {
 		                    ga = pe.getGraphicsAlgorithm();
 		                }
 		                decorator.setX(-5);
 		                decorator.setY(-5);
 		                decorator.setMessage(status.getMessage());
 		                decorators.add(decorator);
 		            }
 		        }
 	        }
 		}
 		
         return decorators.toArray(new IDecorator[decorators.size()]);
     }
 }
