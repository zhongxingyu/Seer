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
  * @author Ivar Meikas
  ******************************************************************************/
 package org.eclipse.bpmn2.modeler.core.di;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import org.eclipse.bpmn2.Association;
 import org.eclipse.bpmn2.BaseElement;
 import org.eclipse.bpmn2.BoundaryEvent;
 import org.eclipse.bpmn2.ChoreographyActivity;
 import org.eclipse.bpmn2.ConversationLink;
 import org.eclipse.bpmn2.DataAssociation;
 import org.eclipse.bpmn2.DataInput;
 import org.eclipse.bpmn2.DataObject;
 import org.eclipse.bpmn2.DataObjectReference;
 import org.eclipse.bpmn2.DataOutput;
 import org.eclipse.bpmn2.Definitions;
 import org.eclipse.bpmn2.Event;
 import org.eclipse.bpmn2.FlowElement;
 import org.eclipse.bpmn2.FlowNode;
 import org.eclipse.bpmn2.ItemAwareElement;
 import org.eclipse.bpmn2.Lane;
 import org.eclipse.bpmn2.MessageFlow;
 import org.eclipse.bpmn2.Participant;
 import org.eclipse.bpmn2.Process;
 import org.eclipse.bpmn2.SequenceFlow;
 import org.eclipse.bpmn2.SubChoreography;
 import org.eclipse.bpmn2.SubProcess;
 import org.eclipse.bpmn2.di.BPMNDiagram;
 import org.eclipse.bpmn2.di.BPMNEdge;
 import org.eclipse.bpmn2.di.BPMNPlane;
 import org.eclipse.bpmn2.di.BPMNShape;
 import org.eclipse.bpmn2.di.BpmnDiFactory;
 import org.eclipse.bpmn2.modeler.core.ModelHandler;
 import org.eclipse.bpmn2.modeler.core.preferences.Bpmn2Preferences;
 import org.eclipse.bpmn2.modeler.core.preferences.ShapeStyle;
 import org.eclipse.bpmn2.modeler.core.utils.AnchorUtil;
 import org.eclipse.bpmn2.modeler.core.utils.BusinessObjectUtil;
 import org.eclipse.bpmn2.modeler.core.utils.FeatureSupport;
 import org.eclipse.bpmn2.modeler.core.utils.GraphicsUtil;
 import org.eclipse.bpmn2.modeler.core.utils.GraphicsUtil.Size;
 import org.eclipse.bpmn2.modeler.core.utils.ModelUtil;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.dd.dc.Bounds;
 import org.eclipse.dd.dc.Point;
 import org.eclipse.dd.di.DiagramElement;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.transaction.RecordingCommand;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.graphiti.datatypes.ILocation;
 import org.eclipse.graphiti.features.IAddFeature;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.ILayoutFeature;
 import org.eclipse.graphiti.features.IUpdateFeature;
 import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
 import org.eclipse.graphiti.features.context.impl.AddContext;
 import org.eclipse.graphiti.features.context.impl.AreaContext;
 import org.eclipse.graphiti.features.context.impl.LayoutContext;
 import org.eclipse.graphiti.features.context.impl.UpdateContext;
 import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
 import org.eclipse.graphiti.mm.pictograms.Connection;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.FixPointAnchor;
 import org.eclipse.graphiti.mm.pictograms.FreeFormConnection;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.mm.pictograms.impl.FreeFormConnectionImpl;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.IGaService;
 import org.eclipse.graphiti.services.IPeService;
 import org.eclipse.graphiti.ui.editor.DiagramEditor;
 import org.eclipse.swt.widgets.Display;
 
 @SuppressWarnings("restriction")
 public class DIImport {
 
 	public static final String IMPORT_PROPERTY = DIImport.class.getSimpleName().concat(".import");
 
 	private DiagramEditor editor;
 //	private Diagram diagram;
 	private TransactionalEditingDomain domain;
 	private ModelHandler modelHandler;
 	private IFeatureProvider featureProvider;
 	private HashMap<BaseElement, PictogramElement> elements;
 	private Bpmn2Preferences preferences;
 	private ImportDiagnostics diagnostics;
 	private final IPeService peService = Graphiti.getPeService();
 	private final IGaService gaService = Graphiti.getGaService();
 	
 	public DIImport(DiagramEditor editor) {
 		this.editor = editor;
 		domain = editor.getEditingDomain();
 		featureProvider = editor.getDiagramTypeProvider().getFeatureProvider();
 	}
 	
 	/**
 	 * Look for model diagram interchange information and generate all shapes for the diagrams.
 	 * 
 	 * NB! Currently only first found diagram is generated.
 	 */
 	public void generateFromDI() {
 		final List<BPMNDiagram> bpmnDiagrams = modelHandler.getAll(BPMNDiagram.class);
 		
 		diagnostics = new ImportDiagnostics(modelHandler.getResource());
 		preferences = (Bpmn2Preferences) editor.getAdapter(Bpmn2Preferences.class);		
 		elements = new HashMap<BaseElement, PictogramElement>();
 		domain.getCommandStack().execute(new RecordingCommand(domain) {
 			@Override
 			protected void doExecute() {
 
 				Diagram diagram = editor.getDiagramTypeProvider().getDiagram();
 				
 				if (bpmnDiagrams.size() == 0) {
 					BPMNPlane plane = BpmnDiFactory.eINSTANCE.createBPMNPlane();
 					plane.setBpmnElement(modelHandler.getOrCreateProcess(modelHandler.getInternalParticipant()));
 
 					BPMNDiagram d = BpmnDiFactory.eINSTANCE.createBPMNDiagram();
 					d.setPlane(plane);
 
 					modelHandler.getDefinitions().getDiagrams().add(d);
 					
 					// don't forget to add the new Diagram to our list for processing
 					bpmnDiagrams.add(d);
 				}
 				featureProvider.link(diagram, bpmnDiagrams.get(0));
 				
 				// First: add all IDs to our ID mapping table
 				Definitions definitions = modelHandler.getDefinitions();
 				TreeIterator<EObject> iter = definitions.eAllContents();
 				while (iter.hasNext()) {
 					ModelUtil.addID( iter.next() );
 				}
 				
 				// do the import
 				// do the import
 				for (BPMNDiagram d : bpmnDiagrams) {
 					diagram = DIUtils.getOrCreateDiagram(editor,d);
 				}
 				for (BPMNDiagram d : bpmnDiagrams) {
 					
 					diagram = DIUtils.findDiagram(editor,d);
 					editor.getDiagramTypeProvider().init(diagram, editor);
 
 					BPMNPlane plane = d.getPlane();
 					if (plane.getBpmnElement() == null) {
 						plane.setBpmnElement(modelHandler.getOrCreateProcess(modelHandler.getInternalParticipant()));
 					}
 					elements.put(plane.getBpmnElement(), diagram);
 					List<DiagramElement> ownedElement = plane.getPlaneElement();
 
 					importShapes(ownedElement);
 					importConnections(ownedElement);
 
 //					relayoutLanes(ownedElement);
 
 					// search for BPMN elements that do not have the DI elements
 					// needed to render them in the editor
 					Display.getDefault().asyncExec(new Runnable() {
 
 						@Override
 						public void run() {
 							final DIGenerator generator = new DIGenerator(DIImport.this);
 							if (generator.hasMissingDIElements()) {
 								// and generate them
 								domain.getCommandStack().execute(new RecordingCommand(domain) {
 									@Override
 									protected void doExecute() {
 										generator.generateMissingDIElements();
 									}
 								});
 							}
 						}
 						
 					});
 				}
 				
 				layoutAll();
 			}
 
 		});
 		
 		diagnostics.report();
 	}
 	
 	public HashMap<BaseElement, PictogramElement> getImportedElements() {
 		return elements;
 	}
 	
 	public ImportDiagnostics getDiagnostics() {
 		return diagnostics;
 	}
 	
 	public DiagramEditor getEditor() {
 		return editor;
 	}
 	
 	private void layoutAll() {
 		final List<BPMNDiagram> diagrams = modelHandler.getAll(BPMNDiagram.class);
 //		for (BPMNDiagram d : diagrams) {
 //			BPMNPlane plane = d.getPlane();
 //			for (DiagramElement de : plane.getPlaneElement()) {
 //				if (de instanceof BPMNShape) {
 //					BaseElement be = ((BPMNShape) de).getBpmnElement();
 //					PictogramElement pe = elements.get(be);
 //					if (pe instanceof Shape ) {
 //						Graphiti.getPeService().sendToFront((Shape)pe);
 //					}
 //				}
 //			}
 //		}
 
 		for (BaseElement be : elements.keySet()) {
 			PictogramElement pe = elements.get(be);
 
 			if (be instanceof SubProcess) { // we need the layout to hide children if collapsed
 				LayoutContext context = new LayoutContext(pe);
 				ILayoutFeature feature = featureProvider.getLayoutFeature(context);
 				if (feature!=null && feature.canLayout(context))
 					feature.layout(context);
 			}
 //			else if (be instanceof FlowNode) {
 //				LayoutContext context = new LayoutContext(pe);
 //				ILayoutFeature feature = featureProvider.getLayoutFeature(context);
 //				if (feature!=null && feature.canLayout(context))
 //					feature.layout(context);
 //			}
 //
 //			if (pe instanceof Connection) {
 //				UpdateContext context = new UpdateContext(pe);
 //				IUpdateFeature feature = featureProvider.getUpdateFeature(context);
 //				if (feature!=null && feature.updateNeeded(context).toBoolean()) {
 //					feature.update(context);
 //				}
 //			}
 		}
  
 	}
 
 	public void setModelHandler(ModelHandler modelHandler) {
 		this.modelHandler = modelHandler;
 	}
 	
 	public void importShape(BPMNShape bpmnShape) {
 		if (!elements.containsKey(bpmnShape.getBpmnElement())) {
 			List<DiagramElement> newElements = new ArrayList<DiagramElement>();
 			newElements.add(bpmnShape);
 			importShapes(newElements);
 		}
 	}
 	
 	/**
 	 * Imports shapes from DI. Since we don't know the order of shapes in DI,
 	 * we may get an inner element like a boundary element before its parent.
 	 * Therefore we use a queue to postpone the import of such elements, and
 	 * prevent the layouting from crashing.
 	 * 
 	 * @param ownedElement
 	 */
 	private void importShapes(List<DiagramElement> ownedElement) {
 		Queue<BPMNShape> shapeQueue = new ConcurrentLinkedQueue<BPMNShape>();
 		
 		// Enqueue shapes
 		for (DiagramElement diagramElement : ownedElement) {
 			if (diagramElement instanceof BPMNShape) {
 				BPMNShape diShape = (BPMNShape) diagramElement;
 				if (diShape.getBpmnElement() != null) {
 					shapeQueue.offer(diShape);
 				}
 			}
 		}
 		
 		// Process Queue
 		// First pass tries to find the missing BPMNShape container
 		// Second pass synthesizes missing containers 
 		int queueLength = shapeQueue.size();
 		for (int pass=0; pass<=1; ++pass) {
 			int requeueCount = 0;
 			while (!shapeQueue.isEmpty() && requeueCount < queueLength) {
 				BPMNShape currentShape = shapeQueue.remove();
 				BaseElement bpmnElement = currentShape.getBpmnElement();
 				boolean postpone = false;
 
 				if (bpmnElement instanceof BoundaryEvent
 						&& !elements.containsKey(((BoundaryEvent) bpmnElement).getAttachedToRef())) {
 					postpone = true;
 				} else if (bpmnElement instanceof FlowNode) {
 	
 					EObject container = bpmnElement.eContainer();
 					if ((container instanceof SubProcess || container instanceof SubChoreography)
 							&& !elements.containsKey(container)) {
 						postpone = true;
 					} else if (!((FlowNode) bpmnElement).getLanes().isEmpty()) {
 						List<Lane> lanes = ((FlowNode) bpmnElement).getLanes();
 						if (pass==0) {
 							for (Lane lane : lanes) {
 								if (!elements.containsKey(lane)) {
 									postpone = true;
 									break;
 								}
 							}
 						}
 						else {
 							// synthesize missing Lane shapes
 							for (Lane lane : lanes) {
 								synthesizeLane(lane);
 							}
 						}
 					}
 				}
 	
 				if (postpone) {
 					// post-pone
 					shapeQueue.offer(currentShape);
 					++requeueCount;
 				} else {
 					createShape(currentShape);
 					requeueCount = 0;
 				}
 			}
 		}
 		
 		if (shapeQueue.size()!=0) {
 			for (Iterator<BPMNShape> iterator = shapeQueue.iterator(); iterator.hasNext();) {
 				BPMNShape currentShape = iterator.next();
 				BaseElement bpmnElement = currentShape.getBpmnElement();
 				if (bpmnElement!=null) {
 					diagnostics.add(IStatus.WARNING, bpmnElement, "Dependency not found");
 				}
 				
 			}
 		}
 	}
 
 	private void synthesizeLane(Lane lane) {
 		if (!elements.containsKey(lane)) {
 			List<BPMNDiagram> diagrams = modelHandler.getAll(BPMNDiagram.class);
 			// this is a new one
 			int xMin = Integer.MAX_VALUE;
 			int yMin = Integer.MAX_VALUE;
 			int width = 0;
 			int height = 0;
 			for (FlowNode flowNode : lane.getFlowNodeRefs()) {
 				BPMNShape flowNodeBPMNShape = (BPMNShape)DIUtils.findDiagramElement(diagrams,flowNode);
 				if (flowNodeBPMNShape!=null) {
 					// adjust bounds of Lane
 					Bounds bounds = flowNodeBPMNShape.getBounds();
 					int x = (int)bounds.getX();
 					int y = (int)bounds.getY();
 					int w = (int)bounds.getWidth();
 					int h = (int)bounds.getHeight();
 					if (x<xMin)
 						xMin = x;
 					if (y<yMin) 
 						yMin = y;
 					if (xMin+width < x + w)
 						width = x - xMin + w;
 					if (yMin+height < y + h)
 						height = y - yMin + h;
 				}
 			}
 			if (width>0 && height>0) {
 				// create a new BPMNShape for this Lane
 				AddContext context = new AddContext(new AreaContext(), lane);
 				context.setX(xMin-10);
 				context.setY(yMin-10);
 				context.setWidth(width+20);
 				context.setHeight(height+20);
 				context.putProperty(IMPORT_PROPERTY, true);
 				// determine the container into which to place the new Lane
 				handleLane(lane, context, null);
 				IAddFeature addFeature = featureProvider.getAddFeature(context);
 				ContainerShape newContainer = (ContainerShape)addFeature.add(context);
 				newContainer.getGraphicsAlgorithm().setTransparency(0.5);
 				Graphiti.getPeService().sendToBack(newContainer);
 				
 				elements.put(lane, newContainer);
 			}									
 		}
 	}
 	
 	public void importConnection(BPMNEdge bpmnEdge) {
 		if (!elements.containsKey(bpmnEdge.getBpmnElement())) {
 			List<DiagramElement> newElements = new ArrayList<DiagramElement>();
 			newElements.add(bpmnEdge);
 			importConnections(newElements);
 		}
 	}
 	
 	private void importConnections(List<DiagramElement> ownedElement) {
 		for (DiagramElement diagramElement : ownedElement) {
 			if (diagramElement instanceof BPMNEdge) {
 				createEdge((BPMNEdge) diagramElement);
 			}
 		}
 	}
 
 	private void relayoutLanes(List<DiagramElement> ownedElement) {
 		Diagram diagram = null;
 		for (DiagramElement diagramElement : ownedElement) {
 			if (diagramElement instanceof BPMNShape && ((BPMNShape) diagramElement).getBpmnElement() instanceof Lane) {
 				if (diagram==null) {
 					diagram = getDiagram(diagramElement);
 				}
 				BaseElement lane = ((BPMNShape) diagramElement).getBpmnElement();
 				ContainerShape shape = (ContainerShape) BusinessObjectUtil.getFirstBaseElementFromDiagram(diagram, lane);
 				FeatureSupport.redraw(shape);
 			}
 		}
 	}
 
 	private Diagram getDiagram(EObject object) {
 		while (object!=null && !(object instanceof BPMNDiagram))
 			object = object.eContainer();
		return DIUtils.findDiagram(editor, (BPMNDiagram)object);
 	
 	}
 	
 	/**
 	 * Find a Graphiti feature for given shape and generate necessary diagram elements.
 	 * 
 	 * @param shape
 	 */
 	private void createShape(BPMNShape shape) {
 		BaseElement bpmnElement = shape.getBpmnElement();
 		if (shape.getChoreographyActivityShape() != null) {
 			// FIXME: we currently generate participant bands automatically
 			return;
 		}
 		AddContext context = new AddContext(new AreaContext(), bpmnElement);
 		IAddFeature addFeature = featureProvider.getAddFeature(context);
 
 		if (addFeature == null) {
 			diagnostics.add(IStatus.WARNING, bpmnElement, "Cannot add graphics");
 			return;
 		}
 
 		Diagram diagram = getDiagram(shape);
 		context.putProperty(IMPORT_PROPERTY, true);
 		context.setNewObject(bpmnElement);
 		boolean defaultSize = false;
 		ShapeStyle ss = preferences.getShapeStyle(bpmnElement);
 		if (ss!=null)
 			defaultSize = ss.isDefaultSize();
 		
 		if (defaultSize) {
 			Size size = GraphicsUtil.getShapeSize(bpmnElement,diagram);
 			if (size!=null)
 				context.setSize(size.getWidth(),size.getHeight());
 			else
 				defaultSize = false;
 		}
 		
 		if (!defaultSize) {
 			context.setSize((int) shape.getBounds().getWidth(), (int) shape.getBounds().getHeight());
 		}
 
 		if ( (bpmnElement instanceof SubProcess) && !shape.isIsExpanded()) {
 			context.setSize(GraphicsUtil.getActivitySize(diagram).getWidth(), GraphicsUtil.getActivitySize(diagram).getHeight());
 		}
 
 		if (bpmnElement instanceof Lane) {
 			handleLane((Lane)bpmnElement, context, shape);
 		} else if (bpmnElement instanceof FlowNode ||
 				bpmnElement instanceof DataObject ||
 				bpmnElement instanceof DataObjectReference) {
 			handleFlowElement((FlowElement) bpmnElement, context, shape);
 		} else if (bpmnElement instanceof Participant) {
 			handleParticipant((Participant) bpmnElement, context, shape);
 		} else if (bpmnElement instanceof DataInput || bpmnElement instanceof DataOutput) {
 			handleItemAwareElement((ItemAwareElement)bpmnElement, context, shape);
 		} else {
 			context.setTargetContainer(diagram);
 			context.setLocation((int) shape.getBounds().getX(), (int) shape.getBounds().getY());
 		}
 
 		if (canAdd(addFeature,context)) {
 			PictogramElement newContainer = addFeature.add(context);
 			featureProvider.link(newContainer, new Object[] { bpmnElement, shape });
 			if (bpmnElement instanceof Participant) {
 				// If the Participant ("Pool") references a Process, add it to our list of elements;
 				// its ContainerShape is the same as the Participant's.
 				Process process = ((Participant) bpmnElement).getProcessRef();
 				if (process!=null)
 					elements.put(process, newContainer);
 			}
 			else if (bpmnElement instanceof ChoreographyActivity) {
 				ChoreographyActivity ca = (ChoreographyActivity)bpmnElement;
 				for (PictogramElement pe : ((ContainerShape)newContainer).getChildren()) {
 					Object o = Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement(pe);
 					if (o instanceof Participant)
 						elements.put((Participant)o, pe);
 				}
 			}
 //			else if (bpmnElement instanceof Event) {
 //				GraphicsUtil.setEventSize(context.getWidth(), context.getHeight(), diagram);
 //			} else if (bpmnElement instanceof Gateway) {
 //				GraphicsUtil.setGatewaySize(context.getWidth(), context.getHeight(), diagram);
 //			} else if (bpmnElement instanceof Activity && !(bpmnElement instanceof SubProcess)) {
 //				GraphicsUtil.setActivitySize(context.getWidth(), context.getHeight(), diagram);
 //			}
 			
 			elements.put(bpmnElement, newContainer);
 			handleEvents(bpmnElement, newContainer);
 		}
 		
 		ModelUtil.addID(bpmnElement);
 	}
 
 	private void handleEvents(BaseElement bpmnElement, PictogramElement newContainer) {
 		if (bpmnElement instanceof Event) {
 			EList<EObject> contents = bpmnElement.eContents();
 			for (EObject obj : contents) {
 
 				AddContext context = new AddContext();
 				context.setTargetContainer((ContainerShape) newContainer);
 				context.setNewObject(obj);
 
 				IAddFeature addFeature = featureProvider.getAddFeature(context);
 				if (canAdd(addFeature,context)) {
 					addFeature.add(context);
 				}
 			}
 		}
 	}
 
 	private void handleParticipant(Participant participant, AddContext context, BPMNShape shape) {
 		Diagram diagram = getDiagram(shape);
 		context.setTargetContainer(diagram);
 		context.setLocation((int) shape.getBounds().getX(), (int) shape.getBounds().getY());
 		FeatureSupport.setHorizontal(context, shape.isIsHorizontal());
 	}
 	
 	private void handleLane(Lane lane, AddContext context, BPMNShape shape) {
 		BaseElement parent = (BaseElement)lane.eContainer().eContainer();
 		ContainerShape targetContainer = null;
 
 		// find the process this lane belongs to
 		for (BaseElement be : elements.keySet()) {
 			if (be instanceof Participant) {
 				Process processRef = ((Participant) be).getProcessRef();
 				if (processRef != null && parent.getId().equals(processRef.getId())) {
 					targetContainer = (ContainerShape) elements.get(be);
 					break;
 				}
 			} else if (be instanceof Process) {
 				if (be.getId().equals(parent.getId())) {
 					targetContainer = (ContainerShape) elements.get(be);
 					break;
 				}
 			} else if (be instanceof Lane) {
 				if (be.getId().equals(parent.getId())) {
 					targetContainer = (ContainerShape) elements.get(be);
 					break;
 				}
 			}
 		}
 		if (targetContainer==null)
 			targetContainer = getDiagram(shape);
 		context.setTargetContainer(targetContainer);
 
 		if (shape!=null) {
 			int x = (int) shape.getBounds().getX();
 			int y = (int) shape.getBounds().getY();
 			ILocation loc = Graphiti.getPeLayoutService().getLocationRelativeToDiagram(targetContainer);
 			x -= loc.getX();
 			y -= loc.getY();
 	
 			context.setLocation((int) x, y);
 			FeatureSupport.setHorizontal(context, shape.isIsHorizontal());
 		}
 	}
 
 	private void handleFlowElement(FlowElement element, AddContext context, BPMNShape shape) {
 		Diagram diagram = getDiagram(shape);
 		ContainerShape targetContainer = diagram;
 		int x = (int) shape.getBounds().getX();
 		int y = (int) shape.getBounds().getY();
 
 		// find a correct container element
 		List<Lane> lanes = null;
 		if (element instanceof FlowNode)
 			lanes = ((FlowNode)element).getLanes();
 
 		EObject parent = element.eContainer();
 		if (	(parent instanceof SubProcess
 				|| parent instanceof Process
 				|| parent instanceof SubChoreography)
 				&& (lanes==null || lanes.isEmpty())
 		) {
 			targetContainer = (ContainerShape) elements.get(parent);
 			if (targetContainer == null) {
 				BPMNDiagram childDiagram = DIUtils.findBPMNDiagram(element, true);
 				if (childDiagram!=null) {
 					targetContainer = DIUtils.findDiagram(editor, childDiagram);
 				}
 			}
 			if (!(targetContainer instanceof Diagram)) {
 				ILocation loc = Graphiti.getPeLayoutService().getLocationRelativeToDiagram(targetContainer);
 				x -= loc.getX();
 				y -= loc.getY();
 			
 			}
 		}
 		else if (lanes!=null && !lanes.isEmpty()) {
 			for (Lane lane : lanes) {
 				targetContainer = (ContainerShape) elements.get(lane);
 				ILocation loc = Graphiti.getPeLayoutService().getLocationRelativeToDiagram(targetContainer);
 				x -= loc.getX();
 				y -= loc.getY();
 			}
 		}
 		context.setTargetContainer(targetContainer);
 		context.setLocation(x, y);
 	}
 
 	private void handleItemAwareElement(ItemAwareElement element, AddContext context, BPMNShape shape) {
 		ContainerShape targetContainer = null;
 		int x = (int) shape.getBounds().getX();
 		int y = (int) shape.getBounds().getY();
 		int w = (int) shape.getBounds().getWidth();
 		int h = (int) shape.getBounds().getHeight();
 
 		// find a correct container element
 		// if this Data Object is contained within a Lane, make the Lane the target container
 		for (Entry<BaseElement, PictogramElement> entry : elements.entrySet()) {
 			if (entry.getKey() instanceof Lane) {
 				ContainerShape laneShape = (ContainerShape)entry.getValue();
 				if (GraphicsUtil.intersects(laneShape, x, y, w, h)) {
 					targetContainer = (ContainerShape) laneShape;
 					ILocation loc = Graphiti.getPeLayoutService().getLocationRelativeToDiagram(targetContainer);
 					x -= loc.getX();
 					y -= loc.getY();
 					break;
 				}
 			}
 		}
 		if (targetContainer==null)
 			targetContainer = getDiagram(shape);
 		context.setTargetContainer(targetContainer);
 		context.setLocation(x, y);
 	}
 	
 	/**
 	 * Find a Graphiti feature for given edge and generate necessary connections and bendpoints.
 	 * 
 	 * @param shape
 	 */
 	private void createEdge(BPMNEdge bpmnEdge) {
 		BaseElement bpmnElement = bpmnEdge.getBpmnElement();
 		EObject source = null;
 		EObject target = null;
 		PictogramElement se = null;
 		PictogramElement te = null;
 
 		// for some reason connectors don't have a common interface
 		if (bpmnElement instanceof MessageFlow) {
 			source = ((MessageFlow) bpmnElement).getSourceRef();
 			target = ((MessageFlow) bpmnElement).getTargetRef();
 			se = elements.get(source);
 			te = elements.get(target);
 		} else if (bpmnElement instanceof SequenceFlow) {
 			source = ((SequenceFlow) bpmnElement).getSourceRef();
 			target = ((SequenceFlow) bpmnElement).getTargetRef();
 			se = elements.get(source);
 			te = elements.get(target);
 		} else if (bpmnElement instanceof Association) {
 			source = ((Association) bpmnElement).getSourceRef();
 			target = ((Association) bpmnElement).getTargetRef();
 			se = elements.get(source);
 			te = elements.get(target);
 		} else if (bpmnElement instanceof ConversationLink) {
 			source = ((ConversationLink) bpmnElement).getSourceRef();
 			target = ((ConversationLink) bpmnElement).getTargetRef();
 			se = elements.get(source);
 			te = elements.get(target);
 		} else if (bpmnElement instanceof DataAssociation) {
 			// Data Association allows connections for multiple starting points, we don't support it yet
 			List<ItemAwareElement> sourceRef = ((DataAssociation) bpmnElement).getSourceRef();
 			ItemAwareElement targetRef = ((DataAssociation) bpmnElement).getTargetRef();
 			if (sourceRef != null) {
 				source = sourceRef.get(0);
 			}
 			target = targetRef;
 			do {
 				se = elements.get(source);
 				source = source.eContainer();
 			} while (se == null && source.eContainer() != null);
 			do {
 				te = elements.get(target);
 				target = target.eContainer();
 			} while (te == null && target.eContainer() != null);
 		}
 
 		ModelUtil.addID(bpmnElement);
 		
 
 		if (se != null && te != null) {
 			if (source != null && target != null) {
 				addSourceAndTargetToEdge(bpmnEdge, source, target);
 			}
 
 			Connection conn = createConnectionAndSetBendpoints(bpmnEdge, se, te);
 			elements.put(bpmnElement, conn);
 			
 		} else {
 			String message;
 			if (se==null && te==null)
 				message = "Source and Target shapes do not exist";
 			else if (se==null)
 				message = "Source shape does not exist";
 			else
 				message = "Target shape does not exist";
 
 			diagnostics.add(IStatus.WARNING, bpmnElement, message);
 		}
 	}
 
 	private void addSourceAndTargetToEdge(BPMNEdge bpmnEdge, EObject source, EObject target) {
 		// We get most of the information from the BpmnEdge, not from the referencing business object. Because of this
 		// we must ensure, that the edge contains necessary information.
 		DiagramElement sourceElement = null;
 		DiagramElement targetElement = null;
 		
 		try {
 			sourceElement = bpmnEdge.getSourceElement();
 			targetElement = bpmnEdge.getTargetElement();
 		}catch (ClassCastException e) {
 			// some other modelers like Yaoqiang BPMN are doing it wrong, they reference business objects instead of 
 			// DiagramElements (see BPMN 2.0 spec, p. 405, 12.2.3.5). this will cause an execption 
 			// in the BPMN 2.0 metamodel implementation
 		}
 		
 		if (sourceElement == null) {
 			bpmnEdge.setSourceElement(DIUtils.findBPMNEdge((BaseElement) source));
 		}
 		if (targetElement == null) {
 			bpmnEdge.setTargetElement(DIUtils.findBPMNEdge((BaseElement) target));
 		}
 	}
 
 	private Connection createConnectionAndSetBendpoints(BPMNEdge bpmnEdge, PictogramElement sourcePE,
 			PictogramElement targetPE) {
 
 		FixPointAnchor sourceAnchor = createAnchor(sourcePE, bpmnEdge, true);
 		FixPointAnchor targetAnchor = createAnchor(targetPE, bpmnEdge, false);
 
 		AddConnectionContext context = new AddConnectionContext(sourceAnchor, targetAnchor);
 		BaseElement bpmnElement = bpmnEdge.getBpmnElement();
 		context.setNewObject(bpmnElement);
 
 		IAddFeature addFeature = featureProvider.getAddFeature(context);
 		if (canAdd(addFeature,context)) {
 			context.putProperty(IMPORT_PROPERTY, true);
 			Connection connection = (Connection) addFeature.add(context);
 			if (AnchorUtil.useAdHocAnchors(sourcePE, connection)) {
 				peService.setPropertyValue(connection, AnchorUtil.CONNECTION_SOURCE_LOCATION,
 						AnchorUtil.pointToString(sourceAnchor.getLocation()));
 			}
 			if (AnchorUtil.useAdHocAnchors(targetPE, connection)) {
 				peService.setPropertyValue(connection, AnchorUtil.CONNECTION_TARGET_LOCATION,
 						AnchorUtil.pointToString(targetAnchor.getLocation()));
 			}
 
 			if (connection instanceof FreeFormConnectionImpl) {
 				List<Point> waypoints = bpmnEdge.getWaypoint();
 				for (int i=1; i<waypoints.size()-1; ++i) {
 					DIUtils.addBendPoint((FreeFormConnection)connection, waypoints.get(i));
 				}
 			}
 			
 			featureProvider.link(connection, new Object[] { bpmnElement, bpmnEdge });
 			return connection;
 		} else {
 			diagnostics.add(IStatus.WARNING, bpmnElement,"Cannot create graphics");
 		}
 		return null;
 	}
 
 	private FixPointAnchor createAnchor(PictogramElement pictogramElement, BPMNEdge bpmnEdge, boolean isSource) {
 		FixPointAnchor sa;
 		
 		if (pictogramElement instanceof FreeFormConnection) {
 			Shape connectionPointShape = AnchorUtil.createConnectionPoint(featureProvider,
 					(FreeFormConnection)pictogramElement,
 					Graphiti.getPeLayoutService().getConnectionMidpoint((FreeFormConnection)pictogramElement, 0.5));
 			sa = AnchorUtil.getConnectionPointAnchor(connectionPointShape);
 		}
 		else
 		{
 			BaseElement baseElement = BusinessObjectUtil.getFirstBaseElement(pictogramElement);
 			BaseElement flowElement = bpmnEdge.getBpmnElement();
 			Point waypoint = null;
 			if (isSource) {
 				waypoint = bpmnEdge.getWaypoint().get(0);
 			}
 			else {
 				waypoint = bpmnEdge.getWaypoint().get(bpmnEdge.getWaypoint().size()-1);
 				
 			}
 			
 			int x = (int)waypoint.getX();
 			int y = (int)waypoint.getY();
 			org.eclipse.graphiti.mm.algorithms.styles.Point anchorPoint = gaService.createPoint(x,y);
 			
 			if (AnchorUtil.useAdHocAnchors(baseElement, flowElement)) {
 				ILocation loc = Graphiti.getPeLayoutService().getLocationRelativeToDiagram((Shape)pictogramElement);
 				anchorPoint.setX(x - loc.getX());
 				anchorPoint.setY(y - loc.getY());
 				sa = AnchorUtil.createAdHocAnchor((AnchorContainer)pictogramElement, anchorPoint);
 				setAnchorLocation(pictogramElement, sa, waypoint);
 			}
 			else {
 				sa = AnchorUtil.findNearestAnchor((AnchorContainer)pictogramElement, anchorPoint);
 			}
 		}
 		return sa;
 	}
 
 	private void setAnchorLocation(PictogramElement elem, FixPointAnchor anchor, Point point) {
 		org.eclipse.graphiti.mm.algorithms.styles.Point p = gaService.createPoint((int) point.getX(),
 				(int) point.getY());
 
 		ILocation loc;
 		if (elem instanceof Connection)
 			loc = Graphiti.getPeLayoutService().getConnectionMidpoint((Connection)elem, 0.5);
 		else
 			loc = Graphiti.getPeLayoutService().getLocationRelativeToDiagram((Shape) elem);
 
 		int x = p.getX() - loc.getX();
 		int y = p.getY() - loc.getY();
 
 		p.setX(x);
 		p.setY(y);
 
 		anchor.setLocation(p);
 	}
 	
 	private boolean canAdd(IAddFeature addFeature, AddContext context) {
 		if (addFeature==null)
 			return false;
 		
 		if (context.getTargetContainer() instanceof Diagram) {
 			Diagram diagram = (Diagram)context.getTargetContainer();
 			if (diagram!=featureProvider.getDiagramTypeProvider().getDiagram())
 				featureProvider.getDiagramTypeProvider().init(diagram, editor);
 		}
 		return addFeature.canAdd(context);
 	}
 }
