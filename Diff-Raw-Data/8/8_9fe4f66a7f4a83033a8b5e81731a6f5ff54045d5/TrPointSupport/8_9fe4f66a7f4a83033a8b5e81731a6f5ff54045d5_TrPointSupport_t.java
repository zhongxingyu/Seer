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
 
 package org.eclipse.etrice.ui.behavior.support;
 
 import java.util.List;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.etrice.core.naming.RoomNameProvider;
 import org.eclipse.etrice.core.room.ActorClass;
 import org.eclipse.etrice.core.room.EntryPoint;
 import org.eclipse.etrice.core.room.ExitPoint;
 import org.eclipse.etrice.core.room.RoomFactory;
 import org.eclipse.etrice.core.room.State;
 import org.eclipse.etrice.core.room.StateGraph;
 import org.eclipse.etrice.core.room.TrPoint;
 import org.eclipse.etrice.core.room.TransitionPoint;
 import org.eclipse.etrice.core.validation.ValidationUtil;
 import org.eclipse.etrice.ui.behavior.ImageProvider;
 import org.eclipse.etrice.ui.behavior.dialogs.TrPointPropertyDialog;
 import org.eclipse.etrice.ui.common.support.CommonSupportUtil;
 import org.eclipse.etrice.ui.common.support.NoResizeFeature;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.features.IAddFeature;
 import org.eclipse.graphiti.features.ICreateConnectionFeature;
 import org.eclipse.graphiti.features.ICreateFeature;
 import org.eclipse.graphiti.features.IDeleteFeature;
 import org.eclipse.graphiti.features.IFeatureProvider;
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
 import org.eclipse.graphiti.features.context.ILocationContext;
 import org.eclipse.graphiti.features.context.IMoveShapeContext;
 import org.eclipse.graphiti.features.context.IPictogramElementContext;
 import org.eclipse.graphiti.features.context.IRemoveContext;
 import org.eclipse.graphiti.features.context.IResizeShapeContext;
 import org.eclipse.graphiti.features.context.ITargetContext;
 import org.eclipse.graphiti.features.context.IUpdateContext;
 import org.eclipse.graphiti.features.context.impl.AddContext;
 import org.eclipse.graphiti.features.context.impl.CreateConnectionContext;
 import org.eclipse.graphiti.features.context.impl.RemoveContext;
 import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
 import org.eclipse.graphiti.features.custom.ICustomFeature;
 import org.eclipse.graphiti.features.impl.AbstractAddFeature;
 import org.eclipse.graphiti.features.impl.AbstractCreateFeature;
 import org.eclipse.graphiti.features.impl.AbstractUpdateFeature;
 import org.eclipse.graphiti.features.impl.DefaultMoveShapeFeature;
 import org.eclipse.graphiti.features.impl.DefaultRemoveFeature;
 import org.eclipse.graphiti.features.impl.Reason;
 import org.eclipse.graphiti.mm.algorithms.Ellipse;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.algorithms.Polygon;
 import org.eclipse.graphiti.mm.algorithms.Polyline;
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
 
 public class TrPointSupport {
 	
 	public static final int ITEM_SIZE = StateGraphSupport.MARGIN;
 	public static final int ITEM_SIZE_SMALL = StateSupport.MARGIN/2;
 
 	protected static final int LINE_WIDTH = 2;
 	public static final IColorConstant DARK_COLOR = new ColorConstant(0, 0, 0);
 	public static final IColorConstant INHERITED_COLOR = new ColorConstant(100, 100, 100);
 	protected static final IColorConstant BRIGHT_COLOR = new ColorConstant(255, 255, 255);
 	protected static final String PROP_KIND = "item-kind";
 	
 	enum Type { TRANS_POINT, ENTRY_POINT, EXIT_POINT }
 	
 	private static class FeatureProvider extends DefaultFeatureProvider {
 		
 		private static class CreateFeature extends AbstractCreateFeature {
 	
 			private boolean doneChanges = false;
 			
 			protected Type type;
 	
 			public CreateFeature(IFeatureProvider fp, Type type, String name, String description) {
 				super(fp, name, description);
 				this.type = type;
 			}
 			
 			@Override
 			public String getCreateImageId() {
 				switch (type) {
 				case ENTRY_POINT:
 					return ImageProvider.IMG_ENTRYPOINT;
 				case EXIT_POINT:
 					return ImageProvider.IMG_EXITPOINT;
 				case TRANS_POINT:
 					return ImageProvider.IMG_TRPOINT;
 				}
 				return null;
 			}
 	
 			@Override
 			public Object[] create(ICreateContext context) {
 				ContainerShape targetContainer = context.getTargetContainer();
 		        ActorClass ac = SupportUtil.getActorClass(getDiagram());
 				StateGraph sg = (StateGraph) targetContainer.getLink().getBusinessObjects().get(0);
 				boolean inherited = SupportUtil.isInherited(getDiagram(), sg);
 				if (inherited) {
 					sg = SupportUtil.insertRefinedState(sg, ac, targetContainer, getFeatureProvider());
 				}
 				
 		        // create transition point
 				TrPoint tp = null;
 				switch (type) {
 				case ENTRY_POINT:
 					tp = RoomFactory.eINSTANCE.createEntryPoint();
 					break;
 				case EXIT_POINT:
 					tp = RoomFactory.eINSTANCE.createExitPoint();
 					break;
 				case TRANS_POINT:
 					tp = RoomFactory.eINSTANCE.createTransitionPoint();
 					break;
 				}
 		        tp.setName(RoomNameProvider.getUniqueTrPointName(sg));
 				sg.getTrPoints().add(tp);
 		        
 		        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
 				TrPointPropertyDialog dlg = new TrPointPropertyDialog(shell, tp, false);
 				if (dlg.open()!=Window.OK) {
 					if (inherited) {
 						SupportUtil.undoInsertRefinedState(sg, ac, targetContainer, getFeatureProvider());
 					}
 					else {
 						sg.getTrPoints().remove(tp);
 					}
 					return EMPTY;
 				}
 				
 				doneChanges = true;
 		        
 		        // do the add
 		        addGraphicalRepresentation(context, tp);
 	
 		        // return newly created business object(s)
 		        return new Object[] { tp };
 			}
 	
 			@Override
 			public boolean canCreate(ICreateContext context) {
 				if (context.getTargetContainer().getLink()!=null)
 					if (context.getTargetContainer().getLink().getBusinessObjects().size()==1) {
 						EObject obj = context.getTargetContainer().getLink().getBusinessObjects().get(0);
 						if (obj instanceof StateGraph) {
 							StateGraph sg = (StateGraph) obj;
 							if (sg.eContainer() instanceof ActorClass)
 								if (type!=Type.TRANS_POINT)
 									return false;
 							return isValidPosition(context, context, StateGraphSupport.MARGIN);
 						}
 					}
 				return false;
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
 				if (context.getNewObject() instanceof TrPoint) {
 					if (context.getTargetContainer().getLink().getBusinessObjects().size()==1) {
 						EObject obj = context.getTargetContainer().getLink().getBusinessObjects().get(0);
 						if (obj instanceof StateGraph) {
 							return true;
 						}
 						if (obj instanceof State) {
 							return true;
 						}
 					}
 				}
 				return false;
 			}
 	
 			@Override
 			public PictogramElement add(IAddContext context) {
 				TrPoint tp = (TrPoint) context.getNewObject();
 				ContainerShape parentShape = context.getTargetContainer();
 				Object bo = getBusinessObjectForPictogramElement(parentShape);
 				boolean subtp = (bo instanceof State);
 				boolean inherited = subtp?SupportUtil.isInherited(getDiagram(), (State)bo):SupportUtil.isInherited(tp, parentShape);
 	
 				int margin = subtp?StateSupport.MARGIN:StateGraphSupport.MARGIN;
 				int size = subtp?ITEM_SIZE_SMALL:ITEM_SIZE;
 
 				// CONTAINER SHAPE WITH RECTANGLE
 				IPeCreateService peCreateService = Graphiti.getPeCreateService();
 				ContainerShape containerShape =
 					peCreateService.createContainerShape(parentShape, true);
 	
 				Graphiti.getPeService().setPropertyValue(containerShape, Constants.TYPE_KEY, Constants.TRP_TYPE);
 				
 				String kind = getItemKind(tp);
 				Graphiti.getPeService().setPropertyValue(containerShape, PROP_KIND, kind);
 				
 				// the context point is the midpoint relative to the invisible rectangle
 				int x = context.getX();
 				int y = context.getY();
 				int width = parentShape.getGraphicsAlgorithm().getWidth();
 				int height = parentShape.getGraphicsAlgorithm().getHeight();
 				{
 					int dx = (x<=width/2)? x:width-x;
 					int dy = (y<=height/2)? y:height-y;
 					if (dx>dy) {
 						// keep x, project y
 						if (y<=height/2)
 							y = margin;
 						else
 							y = height-margin;
 						
 						if (x<margin)
 							x = margin;
 						else if (x>width-margin)
 							x = width-margin;
 					}
 					else {
 						// keep y, project x
 						if (x<=width/2)
 							x = margin;
 						else
 							x = width-margin;
 						
 						if (y<margin)
 							y = margin;
 						else if (y>height-margin)
 							y = height-margin;
 					}
 					// finally we subtract the midpoint to get coordinates of the upper left corner
 					x -= margin;
 					y -= margin;
 				}
 				
 				Color dark = manageColor(inherited? INHERITED_COLOR:DARK_COLOR);
 				IGaService gaService = Graphiti.getGaService();
 				{
 					final Rectangle invisibleRectangle = gaService.createInvisibleRectangle(containerShape);
 					gaService.setLocationAndSize(invisibleRectangle, x, y, 2*margin, 2*margin);
 	
 					createFigure(tp, subtp,
 							containerShape,
 							invisibleRectangle,
 							dark,
 							manageColor(BRIGHT_COLOR));
 	
 					// create link and wire it
 					link(containerShape, tp);
 				}
 				
 				{
 					Shape labelShape = peCreateService.createShape(containerShape, false);
 					Text label = gaService.createDefaultText(getDiagram(), labelShape, tp.getName());
 					label.setForeground(dark);
 					label.setBackground(dark);
 					label.setHorizontalAlignment(Orientation.ALIGNMENT_CENTER);
 					label.setVerticalAlignment(Orientation.ALIGNMENT_CENTER);
 					gaService.setLocationAndSize(label, 0, 3*margin/2, 2*margin, margin/2);
 					adjustLabel(label, x, y, width, margin, size);
 				}
 
 				if (!subtp)
 					createParentTP(tp, containerShape, parentShape);
 				
 				// call the layout feature
 				layoutPictogramElement(containerShape);
 	
 				return containerShape;
 	
 			}
 
 			/**
 			 * @param tp
 			 * @param tpShape
 			 * @param subGraphShape 
 			 */
 			private void createParentTP(TrPoint tp, ContainerShape tpShape, ContainerShape subGraphShape) {
 				if ((tp instanceof TransitionPoint))
 					return;
 				
 				if (!(tp.eContainer().eContainer() instanceof State))
 					return;
 				
 				State s = (State) tp.eContainer().eContainer();
 				List<PictogramElement> elements = Graphiti.getLinkService().getPictogramElements(getDiagram(), s);
 				if (elements.isEmpty()) {
 					Object bo = getBusinessObjectForPictogramElement(subGraphShape);
 					assert(bo instanceof StateGraph): "expected state graph";
 					assert(((StateGraph)bo).eContainer() instanceof State): "expected state";
 					s = (State) ((StateGraph)bo).eContainer();
 					elements = Graphiti.getLinkService().getPictogramElements(getDiagram(), s);
 				}
 				assert(elements.size()==1): "expected unique pe";
 				assert(elements.get(0) instanceof ContainerShape): "expected state shape";
 				ContainerShape stateShape = (ContainerShape) elements.get(0);
 				GraphicsAlgorithm invisibleRect = stateShape.getGraphicsAlgorithm();
 				GraphicsAlgorithm borderRect = invisibleRect.getGraphicsAlgorithmChildren().get(0);
 				GraphicsAlgorithm invisibleSubRect = subGraphShape.getGraphicsAlgorithm();
 				GraphicsAlgorithm borderSubRect = invisibleSubRect.getGraphicsAlgorithmChildren().get(0);
 				double scaleX = borderRect.getWidth() /(double)borderSubRect.getWidth();
 				double scaleY = borderRect.getHeight()/(double)borderSubRect.getHeight();
 				
 				// use midpoint coordinates relative to border rectangle
 				// result is again in midpoint coordinates relative to the parent border rectangle
 				int mp = StateGraphSupport.MARGIN+ITEM_SIZE/2;
 				int x = (int) ((tpShape.getGraphicsAlgorithm().getX()+mp-StateGraphSupport.MARGIN)*scaleX);
 				int y = (int) ((tpShape.getGraphicsAlgorithm().getY()+mp-StateGraphSupport.MARGIN)*scaleY);
 				
 				// pass coordinates relative to invisible rectangle
 				addItem(tp, x+StateSupport.MARGIN, y+StateSupport.MARGIN, stateShape, fp);
 			}
 		}
 		
 		protected class MoveShapeFeature extends DefaultMoveShapeFeature {
 	
 			public MoveShapeFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 	
 			@Override
 			public boolean canMoveShape(IMoveShapeContext context) {
 				boolean canMove = super.canMoveShape(context);
 	
 				if (canMove) {
 					Object bo = getBusinessObjectForPictogramElement(context.getPictogramElement());
 					if (bo instanceof TrPoint) {
 						TrPoint tp = (TrPoint) bo;
 						
 						ContainerShape containerShape = context.getTargetContainer();
 						if (SupportUtil.isInherited(tp, containerShape))
 							return false;
 						
 						if (isSubTP(context.getPictogramElement()))
 							return true;
 						
 						return isValidPosition(context, context, StateGraphSupport.MARGIN);
 					}
 					return false;
 				}
 				
 				return canMove;
 			}
 			
 			@Override
 			protected void postMoveShape(IMoveShapeContext context) {
 				ContainerShape shapeToMove = (ContainerShape) context.getShape();
 				boolean subtp = isSubTP(context.getPictogramElement());
 				
 				int margin = subtp?StateSupport.MARGIN:StateGraphSupport.MARGIN;
 				int size = subtp?ITEM_SIZE_SMALL:ITEM_SIZE;
 	
 				int x = context.getX();
 				int y = context.getY();
 				int width = context.getTargetContainer().getGraphicsAlgorithm().getGraphicsAlgorithmChildren().get(0).getWidth();
 				int height = context.getTargetContainer().getGraphicsAlgorithm().getGraphicsAlgorithmChildren().get(0).getHeight();
 				
 				// project onto boundary
 				{
 					int dx = (x<=width/2)? x:width-x;
 					int dy = (y<=height/2)? y:height-y;
 					if (dx>dy) {
 						// keep x, project y
 						if (y<=height/2)
 							y = 0;
 						else
 							y = height-0;
 						
 						if (x<0)
 							x = 0;
 						else if (x>width-0)
 							x = width-0;
 					}
 					else {
 						// keep y, project x
 						if (x<=width/2)
 							x = 0;
 						else
 							x = width-0;
 						
 						if (y<0)
 							y = 0;
 						else if (y>height-0)
 							y = height-0;
 					}
 				}
 	
 				Graphiti.getGaService().setLocation(shapeToMove.getGraphicsAlgorithm(), x, y, avoidNegativeCoordinates());
 				
 				GraphicsAlgorithm ga = shapeToMove.getChildren().get(0).getGraphicsAlgorithm();
 				if (ga instanceof Text) {
 					adjustLabel((Text) ga, x, y, width, margin, size);
 				}
 				
				// as of https://bugs.eclipse.org/bugs/show_bug.cgi?id=347509
				// the position of the sub tp shall not be adjusted
//				if (!subtp)
//					adjustSubTPs(shapeToMove);
 			}
 
 			/**
 			 * @param shapeToMove
 			 */
			@SuppressWarnings("unused")
 			private void adjustSubTPs(ContainerShape shapeToMove) {
 				EObject bo = Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement(shapeToMove);
 				if (!(bo instanceof TrPoint)) {
 					assert(false): "expected TrPoint";
 					return;
 				}
 				if (bo instanceof TransitionPoint)
 					return;
 				
 				List<PictogramElement> elements = Graphiti.getLinkService().getPictogramElements(getDiagram(), bo);
 				if (elements.size()!=2) {
 					assert(false): "should be this TrPoint and one sub TrPoint";
 					return;
 				}
 				PictogramElement subTpShape = elements.get(0);
 				if (subTpShape==shapeToMove)
 					subTpShape = elements.get(1);
 
 				// for the coordinate transformation we need the border rectangles of our
 				// state graph and that of the states state graph which contains the sub tp
 				GraphicsAlgorithm invisibleRect = shapeToMove.getContainer().getGraphicsAlgorithm();
 				GraphicsAlgorithm borderRect = invisibleRect.getGraphicsAlgorithmChildren().get(0);
 				GraphicsAlgorithm invisibleSubRect = ((ContainerShape)subTpShape.eContainer()).getGraphicsAlgorithm();
 				GraphicsAlgorithm borderSubRect = invisibleSubRect.getGraphicsAlgorithmChildren().get(0);
 				double scaleX = borderSubRect.getWidth() /(double)borderRect.getWidth();
 				double scaleY = borderSubRect.getHeight()/(double)borderRect.getHeight();
 				
 				// transform mid point into target coordinates
 				int x = (int) (shapeToMove.getGraphicsAlgorithm().getX()*scaleX);
 				int y = (int) (shapeToMove.getGraphicsAlgorithm().getY()*scaleY);
 				
 				// mid point relative to border rectangle -> upper left corner relative to invisible rectangle
 				// (doesn't change anything)
 				
 				Graphiti.getGaService().setLocation(subTpShape.getGraphicsAlgorithm(), x, y, avoidNegativeCoordinates());
 				
 				GraphicsAlgorithm ga = ((ContainerShape)subTpShape).getChildren().get(0).getGraphicsAlgorithm();
 				if (ga instanceof Text) {
 					adjustLabel((Text) ga, x, y, borderRect.getWidth(), StateGraphSupport.MARGIN, ITEM_SIZE_SMALL);
 				}
 			}
 		}
 	
 		private static class PropertyFeature extends AbstractCustomFeature {
 
 			private String name;
 			private String description;
 			private boolean doneChanges = false;
 
 			public PropertyFeature(IFeatureProvider fp) {
 				super(fp);
 				this.name = "Edit Transition Point";
 				this.description = "Edit Transition Point";
 			}
 
 			@Override
 			public String getName() {
 				return name;
 			}
 			
 			@Override
 			public String getDescription() {
 				return description;
 			}
 			
 			@Override
 			public boolean canExecute(ICustomContext context) {
 				PictogramElement[] pes = context.getPictogramElements();
 				if (pes != null && pes.length == 1 && pes[0] instanceof ContainerShape) {
 					Object bo = getBusinessObjectForPictogramElement(pes[0]);
 					if (bo instanceof TrPoint) {
 						return true;
 					}
 				}
 				return false;
 			}
 
 			@Override
 			public void execute(ICustomContext context) {
 				PictogramElement pe = context.getPictogramElements()[0];
 				TrPoint tp = (TrPoint) getBusinessObjectForPictogramElement(pe);
 				boolean subtp = isSubTP(pe);
 				
 				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
 				TrPointPropertyDialog dlg = new TrPointPropertyDialog(shell, tp, subtp);
 				if (dlg.open()!=Window.OK)
 					return;
 
 				doneChanges = true;
 				String kind = getItemKind(tp);
 				Graphiti.getPeService().setPropertyValue(pe, PROP_KIND, kind);
 				updateTrPointFigure(tp, pe, manageColor(DARK_COLOR), manageColor(BRIGHT_COLOR));
 			}
 			
 			@Override
 			public boolean hasDoneChanges() {
 				return doneChanges;
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
 				
 				return bo instanceof TrPoint;
 			}
 
 			@Override
 			public IReason updateNeeded(IUpdateContext context) {
 				Object bo = getBusinessObjectForPictogramElement(context.getPictogramElement());
 				if (bo instanceof EObject && ((EObject)bo).eIsProxy()) {
 					return Reason.createTrueReason("Transition Point deleted from model");
 				}
 				TrPoint tp = (TrPoint) bo;
 				
 				// check if tp still owned/inherited
 				ContainerShape containerShape = (ContainerShape)context.getPictogramElement();
 				bo = getBusinessObjectForPictogramElement(containerShape);
 				// TODOHRR-B check inheritance
 //				if (bo instanceof StateGraph) {
 //					StateGraph sg = (StateGraph) bo;
 //					boolean found = false;
 //					do {
 //						if (sg==tp.eContainer())
 //							found = true;
 //						sg = sg.getBase();
 //					}
 //					while (!found && sg!=null);
 //					
 //					if (!found)
 //						return Reason.createTrueReason("TransitionPoint not inherited anymore");
 //				}
 				
 				GraphicsAlgorithm ga = containerShape.getChildren().get(0).getGraphicsAlgorithm();
 				if (ga instanceof Text) {
 					if (!tp.getName().equals(((Text)ga).getValue()))
 						return Reason.createTrueReason("Name is out of date");
 
 					String kind = getItemKind(tp);
 					if (!kind.equals(Graphiti.getPeService().getPropertyValue(context.getPictogramElement(), PROP_KIND)))
 						return Reason.createTrueReason("Figure is out of date");
 				}
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
 				TrPoint tp = (TrPoint) bo;
 				
 				boolean inherited = SupportUtil.isInherited(tp, containerShape);
 				
 				Color dark = manageColor(inherited? INHERITED_COLOR:DARK_COLOR);
 				updateTrPointFigure(tp, containerShape, dark, manageColor(BRIGHT_COLOR));
 				String kind = getItemKind(tp);
 				Graphiti.getPeService().setPropertyValue(containerShape, PROP_KIND, kind);
 				return true;
 			}
 			
 		}
 		
 		protected static class RemoveFeature extends DefaultRemoveFeature {
 
 			public RemoveFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 
 			public boolean canRemove(IRemoveContext context) {
 				return false;
 			}
 		}
 		
 		protected static class DeleteFeature extends DefaultDeleteFeature {
 
 			public DeleteFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 			
 			@Override
 			public boolean canDelete(IDeleteContext context) {
 				if (isSubTP(context.getPictogramElement()))
 					return false;
 
 				ContainerShape containerShape = (ContainerShape) context.getPictogramElement();
 				TrPoint tp = (TrPoint) getBusinessObjectForPictogramElement(containerShape);
 				if (SupportUtil.isInherited(tp, containerShape))
 					return false;
 				
 				if (ValidationUtil.isConnectedOutside(tp))
 					return false;
 				
 				return true;
 			}
 			
 			/* (non-Javadoc)
 			 * @see org.eclipse.graphiti.ui.features.DefaultDeleteFeature#preDelete(org.eclipse.graphiti.features.context.IDeleteContext)
 			 */
 			@Override
 			public void preDelete(IDeleteContext context) {
 				super.preDelete(context);
 
 				// remove all associated pictogram elements except the doomed one
 				IFeatureProvider featureProvider = getFeatureProvider();
 				EObject bo = Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement(context.getPictogramElement());
 				List<PictogramElement> elements = Graphiti.getLinkService().getPictogramElements(getDiagram(), bo);
 				for (PictogramElement pe : elements) {
 					if (pe==context.getPictogramElement())
 						continue;
 					
 					IRemoveContext rc = new RemoveContext(pe);
 					IRemoveFeature removeFeature = featureProvider.getRemoveFeature(rc);
 					if (removeFeature != null) {
 						removeFeature.remove(rc);
 					}
 				}
 
 				// delete connections (inside this state machine since isn't connected outside)
 				ContainerShape container = (ContainerShape) context.getPictogramElement();
 				CommonSupportUtil.deleteConnectionsRecursive(container, getFeatureProvider());
 			}
 		}
 		
 		protected IFeatureProvider fp;
 		
 		protected FeatureProvider(IDiagramTypeProvider dtp, IFeatureProvider fp) {
 			super(dtp);
 			this.fp = fp;
 		}
 		
 		@Override
 		public ICreateFeature[] getCreateFeatures() {
 			return new ICreateFeature[] {
 					new CreateFeature(fp, Type.TRANS_POINT, "Transition Point", "Create Transition Point"),
 					new CreateFeature(fp, Type.ENTRY_POINT, "Entry Point", "Create Entry Point"),
 					new CreateFeature(fp, Type.EXIT_POINT, "Exit Point", "Create Exit Point")
 				};
 		}
 		
 		@Override
 		public IAddFeature getAddFeature(IAddContext context) {
 			return new AddFeature(fp);
 		}
 	
 		@Override
 		public IMoveShapeFeature getMoveShapeFeature(IMoveShapeContext context) {
 			return new MoveShapeFeature(fp);
 		}
 		
 		@Override
 		public IResizeShapeFeature getResizeShapeFeature(IResizeShapeContext context) {
 			return new NoResizeFeature(fp);
 		}
 	
 		@Override
 		public ICustomFeature[] getCustomFeatures(ICustomContext context) {
 			return new ICustomFeature[] { new PropertyFeature(fp) };
 		}
 		
 		@Override
 		public IUpdateFeature getUpdateFeature(IUpdateContext context) {
 			return new UpdateFeature(fp);
 		}
 
 		@Override
 		public IRemoveFeature getRemoveFeature(IRemoveContext context) {
 			return new RemoveFeature(fp);
 		}
 
 		@Override
 		public IDeleteFeature getDeleteFeature(IDeleteContext context) {
 			return new DeleteFeature(fp);
 		}
 		
 		protected static void createFigure(TrPoint tp, boolean subtp,
 				ContainerShape containerShape,
 				GraphicsAlgorithm invisibleRectangle, Color darkColor, Color brightColor) {
 
 			int margin = subtp?StateSupport.MARGIN:StateGraphSupport.MARGIN;
 			int size = subtp?ITEM_SIZE_SMALL:ITEM_SIZE;
 			int line = subtp?LINE_WIDTH/2:LINE_WIDTH;
 			
 
 			IGaService gaService = Graphiti.getGaService();
 			
 			Ellipse circle = gaService.createEllipse(invisibleRectangle);
 			circle.setForeground(darkColor);
 			circle.setBackground(brightColor);
 			circle.setLineWidth(line);
 			int s2 = size/2;
 			int offset = margin-s2;
 			gaService.setLocationAndSize(circle, offset, offset, size, size);
 
 			if (tp instanceof TransitionPoint) {
 				if (!((TransitionPoint) tp).isHandler())
 					circle.setBackground(darkColor);
 			}
 			else if (tp instanceof ExitPoint) {
 				int xy[] = new int[] { s2, 0, size, s2, s2, size, 0, s2};
 				Polygon diamond = gaService.createPolygon(invisibleRectangle, xy);
 				diamond.setForeground(darkColor);
 				diamond.setBackground(brightColor);
 				diamond.setLineWidth(line);
 				gaService.setLocation(diamond, offset, offset);
 			}
 			else if (tp instanceof EntryPoint) {
 				int sq = (int) (0.707106*s2);
 				int l1[] = new int[] { margin-sq, margin+sq, margin+sq, margin-sq };
 				Polyline line1 = gaService.createPolyline(invisibleRectangle, l1);
 				line1.setLineWidth(line);
 				line1.setForeground(darkColor);
 				int l2[] = new int[] { margin-sq, margin-sq, margin+sq, margin+sq };
 				Polyline line2 = gaService.createPolyline(invisibleRectangle, l2);
 				line2.setLineWidth(line);
 				line2.setForeground(darkColor);
 			}
 
 			if (containerShape.getAnchors().isEmpty()) {
 				// here we place our anchor
 				IPeCreateService peCreateService = Graphiti.getPeCreateService();
 				// TODOHRR:  EllipseAnchor would be nice
 				ChopboxAnchor anchor = peCreateService.createChopboxAnchor(containerShape);
 				anchor.setReferencedGraphicsAlgorithm(circle);
 			}
 			else {
 				// we just set the referenced GA
 				//containerShape.getAnchors().get(0).setReferencedGraphicsAlgorithm(rect);
 			}
 		}
 
 		private static void updateTrPointFigure(TrPoint tp, PictogramElement pe, Color dark, Color bright) {
 			ContainerShape container = (ContainerShape)pe;
 			
 			// we clear the figure and rebuild it
 			GraphicsAlgorithm invisibleRect = pe.getGraphicsAlgorithm();
 			while (!invisibleRect.getGraphicsAlgorithmChildren().isEmpty()) {
 				EcoreUtil.delete(invisibleRect.getGraphicsAlgorithmChildren().get(0), true);
 			}
 			
 			createFigure(tp, false, container, invisibleRect, dark, bright);
 			
 			GraphicsAlgorithm ga = container.getChildren().get(0).getGraphicsAlgorithm();
 			if (ga instanceof Text) {
 				((Text)ga).setValue(tp.getName());
 			}
 
 		}
 
 		protected static boolean isSubTP(PictogramElement pe) {
 			if (!(pe instanceof ContainerShape))
 				return false;
 			
 			ContainerShape parentShape = ((ContainerShape)pe).getContainer();
 			EObject parent = Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement(parentShape);
 			
 			return (parent instanceof State);
 		}
 		
 		protected static boolean isValidPosition(ILocationContext loc, ITargetContext tgt, int margin) {
 			//System.out.println("isValidPosition "+tgt.getTargetContainer());
 			if (tgt.getTargetContainer().getGraphicsAlgorithm()==null)
 				return false;
 			if (tgt.getTargetContainer().getGraphicsAlgorithm().getGraphicsAlgorithmChildren().isEmpty())
 				return false;
 
 			int x = loc.getX();
 			int y = loc.getY();
 			if (loc instanceof ICreateContext) {
 				// adjust position as relative to border rectangle
 				x -= margin;
 				y -= margin;
 			}
 			int width = tgt.getTargetContainer().getGraphicsAlgorithm().getGraphicsAlgorithmChildren().get(0).getWidth();
 			int height = tgt.getTargetContainer().getGraphicsAlgorithm().getGraphicsAlgorithmChildren().get(0).getHeight();
 			
 			boolean inStripe = false;
 			
 			// may move in a stripe along the actor class border
 			int stripe = margin/2;
 			if (x<=stripe)
 				inStripe = true;
 			else if (y<=stripe)
 				inStripe = true;
 			else if ((width-stripe)<=x)
 				inStripe = true;
 			else if ((height-stripe)<=y)
 				inStripe = true;
 			
 			//System.out.println("w:"+width+" h:"+height+" x:"+x+" y:"+y+" in stripe "+inStripe);
 
 			return inStripe;
 		}
 		
 
 		protected static String getItemKind(TrPoint item) {
 			if (item instanceof TransitionPoint) {
 				return ((TransitionPoint) item).isHandler()? "htp":"tp";
 			}
 			else if (item instanceof EntryPoint) {
 				return "entp";
 			}
 			else if (item instanceof ExitPoint) {
 				return "extp";
 			}
 			return "";
 		}
 
 		protected static void adjustLabel(Text label, int x, int y, int width, int margin, int size) {
 			Orientation halign = Orientation.ALIGNMENT_CENTER;
 			Orientation valign = Orientation.ALIGNMENT_CENTER;
 	
 			int pos = 0;
 			
 			if (x<=margin)
 				halign = Orientation.ALIGNMENT_LEFT;
 			else if ((width-margin)<=x)
 				halign = Orientation.ALIGNMENT_RIGHT;
 			if (y<=margin) {
 				pos = 0;
 				valign = Orientation.ALIGNMENT_BOTTOM;
 			}
 			else {
 				pos = 5*margin/4;
 				valign = Orientation.ALIGNMENT_TOP;
 			}
 	
 			label.setHorizontalAlignment(halign);
 			label.setVerticalAlignment(valign);
 
 			if (pos!=label.getY()) {
 				IGaService gaService = Graphiti.getGaService();
 				gaService.setLocationAndSize(label, 0, pos, 2*margin, 3*margin/4);
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
 			return new FeatureProvider.PropertyFeature(getDiagramTypeProvider().getFeatureProvider());
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
 				// our transition point has four fixed point anchor - we choose the first one
 				anchor = ((ContainerShape)pe).getAnchors().get(0);
 			}
 			ccc.setSourceAnchor(anchor);
 			
 			ContextButtonEntry button = new ContextButtonEntry(null, context);
 			button.setText("Create Transition");
 			button.setIconId(ImageProvider.IMG_TRANSITION);
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
 	
 	public TrPointSupport(IDiagramTypeProvider dtp, IFeatureProvider fp) {
 		pfp = new FeatureProvider(dtp,fp);
 		tbp = new BehaviorProvider(dtp);
 	}
 	
 	public IFeatureProvider getFeatureProvider() {
 		return pfp;
 	}
 	
 	public IToolBehaviorProvider getToolBehaviorProvider() {
 		return tbp;
 	}
 
 	private static void addItem(EObject ownObject, int x, int y,
 			ContainerShape stateShape, IFeatureProvider fp) {
 		AddContext addContext = new AddContext();
 		addContext.setNewObject(ownObject);
 		addContext.setTargetContainer(stateShape);
 
 		addContext.setX(x);
 		addContext.setY(y);
 		ContainerShape pe = (ContainerShape) fp.addIfPossible(addContext);
 		assert(pe!=null): "transition point should have been created";
 		assert(!pe.getAnchors().isEmpty()): "transition point must have an anchor";
 	}
 	
 //	private static EObject getOwnObject(EObject obj, ResourceSet rs) {
 //		URI uri = EcoreUtil.getURI(obj);
 //		EObject own = rs.getEObject(uri, true);
 //		assert(own!=null): "own object must exist";
 //		return own;
 //	}
 }
