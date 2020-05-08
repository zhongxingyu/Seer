 /*******************************************************************************
  * Copyright (c) 2010 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * CONTRIBUTORS:
  * 		Thomas Schuetz and Henrik Rentz-Reichert (initial contribution)
  * 
  *******************************************************************************/
 
 package org.eclipse.etrice.ui.structure.support;
 
 import java.util.List;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.etrice.core.naming.RoomNameProvider;
 import org.eclipse.etrice.core.room.ActorClass;
 import org.eclipse.etrice.core.room.ActorContainerClass;
 import org.eclipse.etrice.core.room.ActorContainerRef;
 import org.eclipse.etrice.core.room.ActorRef;
 import org.eclipse.etrice.core.room.InterfaceItem;
 import org.eclipse.etrice.core.room.LogicalSystem;
 import org.eclipse.etrice.core.room.RoomFactory;
 import org.eclipse.etrice.core.room.RoomPackage;
 import org.eclipse.etrice.core.room.StructureClass;
 import org.eclipse.etrice.core.room.SubSystemRef;
 import org.eclipse.etrice.core.room.util.RoomHelpers;
 import org.eclipse.etrice.ui.structure.DiagramAccess;
 import org.eclipse.etrice.ui.structure.DiagramTypeProvider;
 import org.eclipse.etrice.ui.structure.ImageProvider;
 import org.eclipse.etrice.ui.structure.dialogs.ActorContainerRefPropertyDialog;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.features.IAddFeature;
 import org.eclipse.graphiti.features.ICreateConnectionFeature;
 import org.eclipse.graphiti.features.ICreateFeature;
 import org.eclipse.graphiti.features.IDeleteFeature;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.ILayoutFeature;
 import org.eclipse.graphiti.features.IMoveShapeFeature;
 import org.eclipse.graphiti.features.IReason;
 import org.eclipse.graphiti.features.IRemoveFeature;
 import org.eclipse.graphiti.features.IResizeShapeFeature;
 import org.eclipse.graphiti.features.IUpdateFeature;
 import org.eclipse.graphiti.features.context.IAddContext;
 import org.eclipse.graphiti.features.context.ICreateContext;
 import org.eclipse.graphiti.features.context.ICustomContext;
 import org.eclipse.graphiti.features.context.IDeleteContext;
 import org.eclipse.graphiti.features.context.IDoubleClickContext;
 import org.eclipse.graphiti.features.context.ILayoutContext;
 import org.eclipse.graphiti.features.context.IMoveShapeContext;
 import org.eclipse.graphiti.features.context.IPictogramElementContext;
 import org.eclipse.graphiti.features.context.IRemoveContext;
 import org.eclipse.graphiti.features.context.IResizeShapeContext;
 import org.eclipse.graphiti.features.context.IUpdateContext;
 import org.eclipse.graphiti.features.context.impl.CreateConnectionContext;
 import org.eclipse.graphiti.features.context.impl.RemoveContext;
 import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
 import org.eclipse.graphiti.features.custom.ICustomFeature;
 import org.eclipse.graphiti.features.impl.AbstractAddFeature;
 import org.eclipse.graphiti.features.impl.AbstractCreateFeature;
 import org.eclipse.graphiti.features.impl.AbstractLayoutFeature;
 import org.eclipse.graphiti.features.impl.AbstractUpdateFeature;
 import org.eclipse.graphiti.features.impl.DefaultMoveShapeFeature;
 import org.eclipse.graphiti.features.impl.DefaultRemoveFeature;
 import org.eclipse.graphiti.features.impl.DefaultResizeShapeFeature;
 import org.eclipse.graphiti.features.impl.Reason;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.algorithms.Rectangle;
 import org.eclipse.graphiti.mm.algorithms.Text;
 import org.eclipse.graphiti.mm.algorithms.styles.Color;
 import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
 import org.eclipse.graphiti.mm.pictograms.Anchor;
 import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
 import org.eclipse.graphiti.mm.pictograms.ChopboxAnchor;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.IGaService;
 import org.eclipse.graphiti.services.IPeCreateService;
 import org.eclipse.graphiti.tb.ContextButtonEntry;
 import org.eclipse.graphiti.tb.DefaultToolBehaviorProvider;
 import org.eclipse.graphiti.tb.IContextButtonPadData;
 import org.eclipse.graphiti.tb.IToolBehaviorProvider;
 import org.eclipse.graphiti.ui.features.DefaultDeleteFeature;
 import org.eclipse.graphiti.ui.features.DefaultFeatureProvider;
 import org.eclipse.graphiti.util.ColorConstant;
 import org.eclipse.graphiti.util.IColorConstant;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.xtext.scoping.IScope;
 import org.eclipse.xtext.scoping.IScopeProvider;
 
 public class ActorContainerRefSupport {
 	
 	private static final int LINE_WIDTH = 1;
 	public static final int DEFAULT_SIZE_X = 180;
 	public static final int DEFAULT_SIZE_Y = 80;
 	public static final int MIN_SIZE_X = 100;
 	public static final int MIN_SIZE_Y = 60;
 	public static final int MARGIN = 20;
 	
 	public static final IColorConstant LINE_COLOR = new ColorConstant(0, 0, 0);
 	public static final IColorConstant INHERITED_COLOR = new ColorConstant(100, 100, 100);
 	public static final IColorConstant BACKGROUND = new ColorConstant(200, 200, 200);
 
 	private static class FeatureProvider extends DefaultFeatureProvider {
 		
 		private class CreateFeature extends AbstractCreateFeature {
 	
 			private boolean actorRef;
 			private boolean doneChanges = false;
 
 			public CreateFeature(IFeatureProvider fp, boolean actorRef) {
 				super(fp, actorRef?"ActorRef":"SubSystemRef", "create "+(actorRef?"ActorRef":"SubSystemRef"));
 				this.actorRef = actorRef;
 			}
 			
 			@Override
 			public String getCreateImageId() {
 				return ImageProvider.IMG_REF;
 			}
 	
 			@Override
 			public boolean canCreate(ICreateContext context) {
 				if (context.getTargetContainer().getLink()!=null)
 					if (context.getTargetContainer().getLink().getBusinessObjects().size()==1) {
 						EObject obj = context.getTargetContainer().getLink().getBusinessObjects().get(0);
 						if (obj instanceof ActorContainerClass) {
 							return actorRef;
 						}
 						if (obj instanceof LogicalSystem) {
 							return !actorRef;
 						}
 					}
 				return false;
 			}
 	
 			@Override
 			public Object[] create(ICreateContext context) {
 		        
 		        StructureClass sc = (StructureClass) context.getTargetContainer().getLink().getBusinessObjects().get(0);
 		        
 		        ActorContainerRef newRef = null;
 		        if (sc instanceof ActorContainerClass) {
 		        	ActorContainerClass acc = (ActorContainerClass) sc;
 		        	
 		        	// create ActorRef
 		        	ActorRef ar = RoomFactory.eINSTANCE.createActorRef();
 
 			        acc.getActorRefs().add(ar);
 		        	newRef = ar;
 		        	
 		        }
 		        else if (sc instanceof LogicalSystem) {
 		        	LogicalSystem sys = (LogicalSystem) sc;
 		        	
 		        	// create ActorRef
 		        	SubSystemRef ssr = RoomFactory.eINSTANCE.createSubSystemRef();
 		        	
 		        	sys.getSubSystems().add(ssr);
 		        	newRef = ssr;
 		        }
 		        
 		        newRef.setName(RoomNameProvider.getUniqueActorContainerRefName(sc));
 
 		        IScopeProvider scopeProvider = ((DiagramTypeProvider)getFeatureProvider().getDiagramTypeProvider()).getScopeProvider();
 		        EReference reference = (newRef instanceof ActorRef)?RoomPackage.eINSTANCE.getActorRef_Type():RoomPackage.eINSTANCE.getSubSystemRef_Type();
 				IScope scope = scopeProvider.getScope(newRef.eContainer().eContainer(), reference);
 		        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
 		        ActorContainerRefPropertyDialog dlg = new ActorContainerRefPropertyDialog(shell, newRef, scope, sc, true);
 				if (dlg.open()!=Window.OK) {
 			        if (sc instanceof ActorContainerClass)
 			        	((ActorContainerClass)sc).getActorRefs().remove(newRef);
 			        else if (sc instanceof LogicalSystem)
 			        	((LogicalSystem) sc).getSubSystems().remove(newRef);
 					return EMPTY;
 				}
 		        
 		        addGraphicalRepresentation(context, newRef);
 		        doneChanges = true;
 		        
 		        // return newly created business object(s)
 		        return new Object[] { newRef };
 			}
 			
 			@Override
 			public boolean hasDoneChanges() {
 				return doneChanges;
 			}
 		}
 	
 		private class AddFeature extends AbstractAddFeature {
 	
 			public AddFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 	
 			@Override
 			public boolean canAdd(IAddContext context) {
 				if (context.getNewObject() instanceof ActorContainerRef) {
 					if (context.getTargetContainer().getLink().getBusinessObjects().size()==1) {
 						EObject obj = context.getTargetContainer().getLink().getBusinessObjects().get(0);
 						if (obj instanceof StructureClass) {
 							return true;
 						}
 					}
 				}
 				return false;
 			}
 	
 			@Override
 			public PictogramElement add(IAddContext context) {
 				ActorContainerRef ar = (ActorContainerRef) context.getNewObject();
 				ContainerShape acShape = context.getTargetContainer();
 	
 				// CONTAINER SHAPE WITH RECTANGLE
 				IPeCreateService peCreateService = Graphiti.getPeCreateService();
 				ContainerShape containerShape =
 					peCreateService.createContainerShape(acShape, true);
 				
 				Graphiti.getPeService().setPropertyValue(containerShape, Constants.TYPE_KEY, Constants.REF_TYPE);
 	
 				int width = context.getWidth() <= 0 ? DEFAULT_SIZE_X : context.getWidth();
 				int height = context.getHeight() <= 0 ? DEFAULT_SIZE_Y : context.getHeight();
 			
 				EObject parent = acShape.getLink().getBusinessObjects().get(0);
 				boolean inherited = isInherited(ar, parent);
 				Color lineColor = manageColor(inherited?INHERITED_COLOR:LINE_COLOR);
 				IGaService gaService = Graphiti.getGaService();
 				{
 					final Rectangle invisibleRectangle = gaService.createInvisibleRectangle(containerShape);
 					gaService.setLocationAndSize(invisibleRectangle,
 							context.getX()-(width/2+MARGIN), context.getY()-(height/2+MARGIN), width + 2*MARGIN, height + 2*MARGIN);
 	
 					Rectangle borderRect = gaService.createRectangle(invisibleRectangle);
 					borderRect.setForeground(lineColor);
 					borderRect.setBackground(manageColor(BACKGROUND));
 					borderRect.setLineWidth(LINE_WIDTH);
 					gaService.setLocationAndSize(borderRect, MARGIN, MARGIN, width, height);
 
 					addSubStructureHint(ar, borderRect, lineColor);
 					
 					// anchor for layer connections
 					ChopboxAnchor anchor = peCreateService.createChopboxAnchor(containerShape);
 					anchor.setReferencedGraphicsAlgorithm(borderRect);
 					
 					// create link and wire it
 					link(containerShape, ar);
 				}
 				
 				// the first child shape is the label
 				{
 					Shape labelShape = peCreateService.createShape(containerShape, false);
 					Text label = gaService.createDefaultText(getDiagram(), labelShape, RoomNameProvider.getRefLabelName(ar));
 					label.setForeground(lineColor);
 					label.setBackground(lineColor);
 					label.setHorizontalAlignment(Orientation.ALIGNMENT_CENTER);
 					label.setVerticalAlignment(Orientation.ALIGNMENT_CENTER);
 					gaService.setLocationAndSize(label, MARGIN, MARGIN, width, height);
 				}
 				
 				// other child shapes may follow
 				if (inherited) {
 					InterfaceItemSupport.createInheritedRefItems(ar, containerShape, fp);
 				}
 				else {
 					InterfaceItemSupport.createRefItems(ar, containerShape, fp);
 				}
 	
 				// call the layout feature
 				layoutPictogramElement(containerShape);
 	
 				return containerShape;
 			}
 	
 		}
 		
 		private class LayoutFeature extends AbstractLayoutFeature {
 	
 			public LayoutFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 	
 			@Override
 			public boolean canLayout(ILayoutContext context) {
 				// return true, if pictogram element is linked to an ActorContainerRef
 				PictogramElement pe = context.getPictogramElement();
 				if (!(pe instanceof ContainerShape))
 					return false;
 	
 				EList<EObject> businessObjects = pe.getLink().getBusinessObjects();
 				return businessObjects.size() == 1
 						&& businessObjects.get(0) instanceof ActorContainerRef;
 			}
 	
 			@Override
 			public boolean layout(ILayoutContext context) {
 				boolean anythingChanged = false;
 				ContainerShape containerShape = (ContainerShape) context
 						.getPictogramElement();
 	
 				// our invisible rectangle
 				GraphicsAlgorithm containerGa = containerShape.getGraphicsAlgorithm();
 				
 				int w = containerGa.getWidth();
 				int h = containerGa.getHeight();
 	
 				if (containerGa.getGraphicsAlgorithmChildren().size()==1) {
 					// the visible outer frame
 					GraphicsAlgorithm borderRect = containerGa.getGraphicsAlgorithmChildren().get(0);
 					
 					int nw = w-2*MARGIN;
 					int nh = h-2*MARGIN;
 					
 					borderRect.setWidth(nw);
 					borderRect.setHeight(nh);
 					
 					Object bo = getBusinessObjectForPictogramElement(containerShape);
 					if (bo instanceof ActorContainerRef) {
 						ActorContainerRef acr = (ActorContainerRef) bo;
 						while (!borderRect.getGraphicsAlgorithmChildren().isEmpty()) {
 							EcoreUtil.delete(borderRect.getGraphicsAlgorithmChildren().get(0), true);
 						}
 						EObject parent = containerShape.getContainer().getLink().getBusinessObjects().get(0);
 						Color lineColor = manageColor(isInherited(acr, parent)?INHERITED_COLOR:LINE_COLOR);
 						addSubStructureHint(acr, (Rectangle) borderRect, lineColor);
 					}
 
 					if (!containerShape.getChildren().isEmpty()) {
 						GraphicsAlgorithm childGA = containerShape.getChildren().get(0).getGraphicsAlgorithm();
 						assert(childGA instanceof Text): "label expected";
 						childGA.setWidth(nw);
 						childGA.setHeight(nh);
 					}
 					
 					anythingChanged = true;
 				}
 				
 				return anythingChanged;
 			}
 		}
 
 		private static class PropertyFeature extends AbstractCustomFeature {
 
 			public PropertyFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 
 			@Override
 			public String getName() {
 				return "Edit Reference...";
 			}
 			
 			@Override
 			public String getDescription() {
 				return "Edit Reference Properties";
 			}
 			
 			@Override
 			public boolean canExecute(ICustomContext context) {
 				PictogramElement[] pes = context.getPictogramElements();
 				if (pes != null && pes.length == 1) {
 					Object bo = getBusinessObjectForPictogramElement(pes[0]);
 					if (bo instanceof ActorContainerRef) {
 						EObject parent = (EObject) getBusinessObjectForPictogramElement((PictogramElement) pes[0].eContainer());
 						return !isInherited((ActorContainerRef)bo, parent);
 					}
 				}
 				return false;
 			}
 
 			@Override
 			public void execute(ICustomContext context) {
 				ActorContainerRef acr = (ActorContainerRef) getBusinessObjectForPictogramElement(context.getPictogramElements()[0]);
 				StructureClass sc = (StructureClass)acr.eContainer();
 				
 		        IScopeProvider scopeProvider = ((DiagramTypeProvider)getFeatureProvider().getDiagramTypeProvider()).getScopeProvider();
 		        EReference reference = (acr instanceof ActorRef)?RoomPackage.eINSTANCE.getActorRef_Type():RoomPackage.eINSTANCE.getSubSystemRef_Type();
 				IScope scope = scopeProvider.getScope(acr.eContainer().eContainer(), reference);
 		        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
 				ActorContainerRefPropertyDialog dlg = new ActorContainerRefPropertyDialog(shell, acr, scope, sc, false);
 				if (dlg.open()!=Window.OK)
 					throw new RuntimeException();
 				
 				updateLabel(acr, context.getPictogramElements()[0]);
 			}
 			
 		}
 		
 		private static class OpenRefStructureDiagram extends AbstractCustomFeature {
 
 			public OpenRefStructureDiagram(IFeatureProvider fp) {
 				super(fp);
 			}
 
 			@Override
 			public String getName() {
 				return "Open Ref Structure";
 			}
 			
 			@Override
 			public boolean canExecute(ICustomContext context) {
 				PictogramElement[] pes = context.getPictogramElements();
 				if (pes != null && pes.length == 1) {
 					Object bo = getBusinessObjectForPictogramElement(pes[0]);
 					if (bo instanceof ActorContainerRef) {
 						return true;
 					}
 				}
 				return false;
 			}
 
 			/* (non-Javadoc)
 			 * @see org.eclipse.graphiti.features.custom.ICustomFeature#execute(org.eclipse.graphiti.features.context.ICustomContext)
 			 */
 			@Override
 			public void execute(ICustomContext context) {
 				PictogramElement[] pes = context.getPictogramElements();
 				if (pes != null && pes.length == 1) {
 					Object bo = getBusinessObjectForPictogramElement(pes[0]);
 					if (bo instanceof ActorContainerRef) {
 						ActorContainerRef ref = (ActorContainerRef) bo;
 						DiagramAccess diagramAccess = new DiagramAccess();
 						if (ref instanceof ActorRef) {
 							diagramAccess.openDiagramEditor(((ActorRef) ref).getType());
 						}
 						else if (ref instanceof SubSystemRef) {
 							diagramAccess.openDiagramEditor(((SubSystemRef) ref).getType());
 						}
 					}
 				}
 			}
 			
 			@Override
 			public boolean hasDoneChanges() {
 				return false;
 			}
 		}
 		
 		private static class OpenRefBehaviorDiagram extends AbstractCustomFeature {
 
 			public OpenRefBehaviorDiagram(IFeatureProvider fp) {
 				super(fp);
 			}
 
 			@Override
 			public String getName() {
 				return "Open Ref Behavior";
 			}
 			
 			@Override
 			public boolean canExecute(ICustomContext context) {
 				PictogramElement[] pes = context.getPictogramElements();
 				if (pes != null && pes.length == 1) {
 					Object bo = getBusinessObjectForPictogramElement(pes[0]);
 					if (bo instanceof ActorRef) {
 						return true;
 					}
 				}
 				return false;
 			}
 
 			/* (non-Javadoc)
 			 * @see org.eclipse.graphiti.features.custom.ICustomFeature#execute(org.eclipse.graphiti.features.context.ICustomContext)
 			 */
 			@Override
 			public void execute(ICustomContext context) {
 				PictogramElement[] pes = context.getPictogramElements();
 				if (pes != null && pes.length == 1) {
 					Object bo = getBusinessObjectForPictogramElement(pes[0]);
 					if (bo instanceof ActorContainerRef) {
 						ActorContainerRef ref = (ActorContainerRef) bo;
 						org.eclipse.etrice.ui.behavior.DiagramAccess diagramAccess = new org.eclipse.etrice.ui.behavior.DiagramAccess();
 						if (ref instanceof ActorRef) {
 							diagramAccess.openDiagramEditor(((ActorRef) ref).getType());
 						}
 					}
 				}
 			}
 			
 			@Override
 			public boolean hasDoneChanges() {
 				return false;
 			}
 		}
 		
 		private class UpdateFeature extends AbstractUpdateFeature {
 
 			public UpdateFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 
 			@Override
 			public boolean canUpdate(IUpdateContext context) {
 				Object bo = getBusinessObjectForPictogramElement(context.getPictogramElement());
 				if (bo instanceof EObject && ((EObject)bo).eIsProxy())
 					return true;
 				
 				return bo instanceof ActorContainerRef;
 			}
 
 			@Override
 			public IReason updateNeeded(IUpdateContext context) {
 				Object bo = getBusinessObjectForPictogramElement(context.getPictogramElement());
 				if (bo instanceof EObject && ((EObject)bo).eIsProxy()) {
 					return Reason.createTrueReason("Ref deleted from model");
 				}
 				ActorContainerRef acr = (ActorContainerRef) bo;
 
 				String reason = "";
 				
 				// check if ref still owned/inherited anymore
 				ContainerShape containerShape = (ContainerShape)context.getPictogramElement();
 				bo = getBusinessObjectForPictogramElement(containerShape);
 				if (bo instanceof ActorClass) {
 					ActorClass ac = (ActorClass) bo;
 					boolean found = false;
 					do {
 						if (ac==acr.eContainer())
 							found = true;
 						ac = ac.getBase();
 					}
 					while (!found && ac!=null);
 					
 					if (!found)
 						reason += "Ref not inherited anymore\n";
 				}
 				
 				// check sub structure hint
 				{
 					boolean hasSubStruct = hasSubStructure(acr);
 					GraphicsAlgorithm invisibleRect = containerShape.getGraphicsAlgorithm();
 					if (!invisibleRect.getGraphicsAlgorithmChildren().isEmpty()) {
 						GraphicsAlgorithm borderRect = invisibleRect.getGraphicsAlgorithmChildren().get(0);
 						if (!borderRect.getGraphicsAlgorithmChildren().isEmpty()) {
 							GraphicsAlgorithm hint = borderRect.getGraphicsAlgorithmChildren().get(0);
 							if (hasSubStruct && !hint.getLineVisible())
 								reason += "state has sub structure now\n";
 							if (!hasSubStruct && hint.getLineVisible())
 								reason += "state has no sub structure anymore\n";
 						}
 					}
 				}
 				
 				// check class name
 				if (!containerShape.getChildren().isEmpty()) {
 					GraphicsAlgorithm ga = containerShape.getChildren().get(0).getGraphicsAlgorithm();
 					if (ga instanceof Text) {
 						if (bo instanceof ActorContainerRef) {
 							String label = RoomNameProvider.getRefLabelName((ActorContainerRef) bo);
 							if (!((Text)ga).getValue().equals(label))
 								reason += "Class name is out of date\n";
 						}
 					}
 				}
 				
 				// check interface ports and spps added to model not present in diagram
 				{
 					ActorContainerClass acc = (acr instanceof ActorRef)?((ActorRef)acr).getType():((SubSystemRef)acr).getType();
 					List<InterfaceItem> interfaceItems = RoomHelpers.getInterfaceItems(acc, true);
 					List<InterfaceItem> presentItems = SupportUtil.getInterfaceItems(containerShape, fp);
 					int missing = 0;
 					for (InterfaceItem interfaceItem : interfaceItems) {
 						if (!presentItems.contains(interfaceItem))
 							++missing;
 					}
 					if (missing>0)
 						reason += missing+" interface item(s) missing\n";
 				}
 				
 				if (!reason.isEmpty())
 					return Reason.createTrueReason(reason.substring(0, reason.length()-1));
 				
 				return Reason.createFalseReason();
 			}
 
 			@Override
 			public boolean update(IUpdateContext context) {
 				ContainerShape containerShape = (ContainerShape)context.getPictogramElement();
 				Object bo = getBusinessObjectForPictogramElement(containerShape);
 				if (bo instanceof EObject && ((EObject)bo).eIsProxy()) {
 					IRemoveContext rc = new RemoveContext(containerShape);
 					IFeatureProvider featureProvider = getFeatureProvider();
 					IRemoveFeature removeFeature = featureProvider.getRemoveFeature(rc);
 					if (removeFeature != null) {
 						removeFeature.remove(rc);
 					}
 					EcoreUtil.delete((EObject) bo);
 					return true;
 				}
 				
 				ActorContainerRef acr = (ActorContainerRef) bo;
 				{
 					GraphicsAlgorithm invisibleRect = containerShape.getGraphicsAlgorithm();
 					if (!invisibleRect.getGraphicsAlgorithmChildren().isEmpty()) {
 						GraphicsAlgorithm borderRect = invisibleRect.getGraphicsAlgorithmChildren().get(0);
 						updateSubStructureHint(acr, (Rectangle) borderRect);
 					}
 				}
 
 				updateLabel(acr, context.getPictogramElement());
 				
 				InterfaceItemSupport.createRefItems(acr, containerShape, fp);
 				
 				return true;
 			}
 		}
 		
 		private class RemoveFeature extends DefaultRemoveFeature {
 
 			public RemoveFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 
 			@Override
 			public boolean canRemove(IRemoveContext context) {
 				return false;
 			}
 		}
 		
 		private class DeleteFeature extends DefaultDeleteFeature {
 
 			public DeleteFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 
 			@Override
 			public boolean canDelete(IDeleteContext context) {
 				Object bo = getBusinessObjectForPictogramElement(context.getPictogramElement());
 				
 				if (bo instanceof ActorContainerRef) {
 					ActorContainerRef ar = (ActorContainerRef) bo;
 					ContainerShape acShape = (ContainerShape) context.getPictogramElement().eContainer();
 					EObject parent = acShape.getLink().getBusinessObjects().get(0);
 					return !isInherited(ar, parent);
 				}
 				
 				return false;
 			}
 		}
 		
 		private class MoveShapeFeature extends DefaultMoveShapeFeature {
 	
 			public MoveShapeFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 	
 			@Override
 			public boolean canMoveShape(IMoveShapeContext context) {
 				boolean canMove = super.canMoveShape(context);
 	
 				if (canMove) {
 					Object bo = getBusinessObjectForPictogramElement(context.getPictogramElement());
 					
 					if (bo instanceof ActorContainerRef) {
 						ActorContainerRef ar = (ActorContainerRef) bo;
 						ContainerShape acShape = context.getTargetContainer();
 						EObject parent = acShape.getLink().getBusinessObjects().get(0);
 						
 						// TODOHRR: also check coordinates (no overlap with actor boundaries)
 						
 						return !isInherited(ar, parent);
 					}
 				}
 				
 				return canMove;
 			}
 		}
 		
 		private class ResizeFeature extends DefaultResizeShapeFeature {
 
 			public ResizeFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 			
 			@Override
 			public boolean canResizeShape(IResizeShapeContext context) {
 				if (!super.canResizeShape(context))
 					return false;
 				
 				int width = context.getWidth()-MARGIN;
 				int height = context.getHeight()-MARGIN;
 				if (width>0 && height>0)
 					if (width < MIN_SIZE_X+MARGIN || height < MIN_SIZE_Y+MARGIN)
 						return false;
 				
 				return true;
 			}
 			
 			@Override
 			public void resizeShape(IResizeShapeContext context) {
 				Shape shape = context.getShape();
 				
 				if (shape.getGraphicsAlgorithm()!=null) {
 					GraphicsAlgorithm containerGa = shape.getGraphicsAlgorithm();
 					if (containerGa.getGraphicsAlgorithmChildren().size()==1) {
 						// scale interface item coordinates
 						// we refer to the visible rectangle which defines the border of our structure class
 						// since the margin is not scaled
 						GraphicsAlgorithm ga = containerGa.getGraphicsAlgorithmChildren().get(0);
 						double sx = (context.getWidth()-2*MARGIN)/(double)ga.getWidth();
 						double sy = (context.getHeight()-2*MARGIN)/(double)ga.getHeight();
 						
 						for (Shape childShape : ((ContainerShape)context.getShape()).getChildren()) {
 							Object childBo = getBusinessObjectForPictogramElement(childShape);
 							if (childBo instanceof InterfaceItem) {
 								ga = childShape.getGraphicsAlgorithm();
 								ga.setX((int) (ga.getX()*sx));
 								ga.setY((int) (ga.getY()*sy));
 							}
 							
 						}
 					}
 				}
 				super.resizeShape(context);
 			}
 		}
 		
 		private IFeatureProvider fp;
 	
 		public FeatureProvider(IDiagramTypeProvider dtp, IFeatureProvider fp) {
 			super(dtp);
 			this.fp = fp;
 		}
 	
 		@Override
 		public ICreateFeature[] getCreateFeatures() {
 			return new ICreateFeature[] { new CreateFeature(fp, true), new CreateFeature(fp, false) };
 		}
 	
 		@Override
 		public IAddFeature getAddFeature(IAddContext context) {
 			return new AddFeature(fp);
 		}
 		
 		@Override
 		public ILayoutFeature getLayoutFeature(ILayoutContext context) {
 			return new LayoutFeature(fp);
 		}
 		
 		@Override
 		public IUpdateFeature getUpdateFeature(IUpdateContext context) {
 			return new UpdateFeature(fp);
 		}
 		
 		@Override
 		public IMoveShapeFeature getMoveShapeFeature(IMoveShapeContext context) {
 			return new MoveShapeFeature(fp);
 		}
 		
 		@Override
 		public IResizeShapeFeature getResizeShapeFeature(
 				IResizeShapeContext context) {
 			return new ResizeFeature(fp);
 		}
 		
 		@Override
 		public IRemoveFeature getRemoveFeature(IRemoveContext context) {
 			return new RemoveFeature(fp);
 		}
 		
 		@Override
 		public IDeleteFeature getDeleteFeature(IDeleteContext context) {
 			return new DeleteFeature(fp);
 		}
 		
 		@Override
 		public ICustomFeature[] getCustomFeatures(ICustomContext context) {
 			return new ICustomFeature[] {
 					new PropertyFeature(fp),
 					new OpenRefStructureDiagram(fp),
 					new OpenRefBehaviorDiagram(fp)};
 		}
 		
 		protected static boolean isInherited(ActorContainerRef ar, EObject parent) {
 			return ar.eContainer()!=parent;
 		}
 
 		private static boolean hasSubStructure(ActorContainerRef acr) {
 			if (acr instanceof ActorRef) {
 				if (!((ActorRef) acr).getType().getActorRefs().isEmpty())
 					return true;
 			}
 			else if (acr instanceof SubSystemRef) {
 				if (!((SubSystemRef)acr).getType().getActorRefs().isEmpty())
 					return true;
 			}
 			return false;
 		}
 		
 		private static void addSubStructureHint(ActorContainerRef acr,
 				Rectangle borderRect, Color lineColor) {
 			
 			int x = borderRect.getWidth()-35;
 			int y = borderRect.getHeight()-30;
 			IGaService gaService = Graphiti.getGaService();
 			Rectangle hint1 = gaService.createRectangle(borderRect);
 			hint1.setForeground(lineColor);
 			hint1.setFilled(false);
 			hint1.setLineWidth(LINE_WIDTH);
 			gaService.setLocationAndSize(hint1, x+5, y, 20, 10);
 			Rectangle hint2 = gaService.createRectangle(borderRect);
 			hint2.setForeground(lineColor);
 			hint2.setFilled(false);
 			hint2.setLineWidth(LINE_WIDTH);
 			gaService.setLocationAndSize(hint2, x, y+15, 20, 10);
 
 			if (!hasSubStructure(acr)) {
 				hint1.setLineVisible(false);
 				hint2.setLineVisible(false);
 			}
 		}
 		
 		protected static void updateSubStructureHint(ActorContainerRef acr, GraphicsAlgorithm borderRect) {
 			
 			boolean hasSubStructure = hasSubStructure(acr);
 			GraphicsAlgorithm hint = borderRect.getGraphicsAlgorithmChildren().get(0);
 			hint.setLineVisible(hasSubStructure);
 			hint = borderRect.getGraphicsAlgorithmChildren().get(1);
 			hint.setLineVisible(hasSubStructure);
 		}
 
 		private static void updateLabel(ActorContainerRef acr, PictogramElement pe) {
 			ContainerShape container = (ContainerShape)pe;
 			
 			GraphicsAlgorithm ga = container.getChildren().get(0).getGraphicsAlgorithm();
 			if (ga instanceof Text) {
 				((Text)ga).setValue(RoomNameProvider.getRefLabelName(acr));
 			}
 		}
 	}
 	
 	private class BehaviorProvider extends DefaultToolBehaviorProvider {
 
 		public BehaviorProvider(IDiagramTypeProvider dtp) {
 			super(dtp);
 		}
 		
 		@Override
 		public GraphicsAlgorithm[] getClickArea(PictogramElement pe) {
             GraphicsAlgorithm invisible = pe.getGraphicsAlgorithm();
             GraphicsAlgorithm rectangle =
                             invisible.getGraphicsAlgorithmChildren().get(0);
             return new GraphicsAlgorithm[] { rectangle };
 		}
 		
 		@Override
 		public GraphicsAlgorithm getSelectionBorder(PictogramElement pe) {
             GraphicsAlgorithm invisible = pe.getGraphicsAlgorithm();
 
             GraphicsAlgorithm rectangle =
                 invisible.getGraphicsAlgorithmChildren().get(0);
             return rectangle;
 		}
 		
 		@Override
 		public ICustomFeature getDoubleClickFeature(IDoubleClickContext context) {
 			return new FeatureProvider.OpenRefStructureDiagram(getDiagramTypeProvider().getFeatureProvider());
 		}
 		
 		@Override
 		public IContextButtonPadData getContextButtonPad(
 				IPictogramElementContext context) {
 			
 			IContextButtonPadData data = super.getContextButtonPad(context);
 			PictogramElement pe = context.getPictogramElement();
 
 			CreateConnectionContext ccc = new CreateConnectionContext();
 			ccc.setSourcePictogramElement(pe);
 			Anchor anchor = null;
 			if (pe instanceof AnchorContainer) {
 				// our spp has four fixed point anchor - we choose the first one
 				anchor = ((ContainerShape)pe).getAnchors().get(0);
 			}
 			ccc.setSourceAnchor(anchor);
 			
 			ContextButtonEntry button = new ContextButtonEntry(null, context);
 			button.setText("Create Layer Connection");
 			button.setIconId(ImageProvider.IMG_LAYER_CONNECTION);
 			ICreateConnectionFeature[] features = getFeatureProvider().getCreateConnectionFeatures();
 			for (ICreateConnectionFeature feature : features) {
 				if (feature.isAvailable(ccc) && feature.canStartConnection(ccc))
 					button.addDragAndDropFeature(feature);
 			}
 
 			if (button.getDragAndDropFeatures().size() > 0) {
 				data.getDomainSpecificContextButtons().add(button);
 			}
 
 			return data;
 		}
 	}
 	
 	private FeatureProvider pfp;
 	private BehaviorProvider tbp;
 	
 	public ActorContainerRefSupport(IDiagramTypeProvider dtp, IFeatureProvider fp) {
 		pfp = new FeatureProvider(dtp,fp);
 		tbp = new BehaviorProvider(dtp);
 	}
 	
 	public IFeatureProvider getFeatureProvider() {
 		return pfp;
 	}
 	
 	public IToolBehaviorProvider getToolBehaviorProvider() {
 		return tbp;
 	}
 }
