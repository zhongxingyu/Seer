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
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.etrice.core.room.ActorClass;
 import org.eclipse.etrice.core.room.ActorContainerClass;
 import org.eclipse.etrice.core.room.ActorContainerRef;
 import org.eclipse.etrice.core.room.ActorRef;
 import org.eclipse.etrice.core.room.InterfaceItem;
 import org.eclipse.etrice.core.room.Port;
 import org.eclipse.etrice.core.room.SPPRef;
 import org.eclipse.etrice.core.room.SubSystemRef;
 import org.eclipse.etrice.core.room.util.RoomHelpers;
 import org.eclipse.etrice.ui.common.support.NoResizeFeature;
 import org.eclipse.etrice.ui.structure.DiagramAccess;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.features.IDeleteFeature;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.IMoveShapeFeature;
 import org.eclipse.graphiti.features.IReason;
 import org.eclipse.graphiti.features.IRemoveFeature;
 import org.eclipse.graphiti.features.IResizeShapeFeature;
 import org.eclipse.graphiti.features.context.IAddContext;
 import org.eclipse.graphiti.features.context.ICreateContext;
 import org.eclipse.graphiti.features.context.ICustomContext;
 import org.eclipse.graphiti.features.context.IDeleteContext;
 import org.eclipse.graphiti.features.context.ILocationContext;
 import org.eclipse.graphiti.features.context.IMoveShapeContext;
 import org.eclipse.graphiti.features.context.IRemoveContext;
 import org.eclipse.graphiti.features.context.IResizeShapeContext;
 import org.eclipse.graphiti.features.context.ITargetContext;
 import org.eclipse.graphiti.features.context.IUpdateContext;
 import org.eclipse.graphiti.features.context.impl.RemoveContext;
 import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
 import org.eclipse.graphiti.features.impl.AbstractAddFeature;
 import org.eclipse.graphiti.features.impl.AbstractCreateFeature;
 import org.eclipse.graphiti.features.impl.AbstractUpdateFeature;
 import org.eclipse.graphiti.features.impl.DefaultMoveShapeFeature;
 import org.eclipse.graphiti.features.impl.DefaultRemoveFeature;
 import org.eclipse.graphiti.features.impl.Reason;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.algorithms.Rectangle;
 import org.eclipse.graphiti.mm.algorithms.Text;
 import org.eclipse.graphiti.mm.algorithms.styles.Color;
 import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.IGaService;
 import org.eclipse.graphiti.services.IPeCreateService;
 import org.eclipse.graphiti.tb.DefaultToolBehaviorProvider;
 import org.eclipse.graphiti.ui.features.DefaultDeleteFeature;
 import org.eclipse.graphiti.ui.features.DefaultFeatureProvider;
 import org.eclipse.graphiti.util.ColorConstant;
 import org.eclipse.graphiti.util.IColorConstant;
 
 public class InterfaceItemSupport {
 	
 	public static final int ITEM_SIZE = StructureClassSupport.MARGIN;
 	public static final int ITEM_SIZE_SMALL = ActorContainerRefSupport.MARGIN;
 
 	protected static final int LINE_WIDTH = 2;
 	protected static final IColorConstant DARK_COLOR = new ColorConstant(0, 0, 0);
 	protected static final IColorConstant INHERITED_COLOR = new ColorConstant(100, 100, 100);
 	protected static final IColorConstant BRIGHT_COLOR = new ColorConstant(255, 255, 255);
 	protected static final String PROP_KIND = "item-kind";
 	
 	protected static class FeatureProvider extends DefaultFeatureProvider {
 
 		protected abstract static class CreateFeature extends AbstractCreateFeature {
 	
 			protected boolean internal;
 			protected boolean doneChanges = false;
 	
 			public CreateFeature(IFeatureProvider fp, boolean internal, String name, String description) {
 				super(fp, name, description);
 				this.internal = internal;
 			}
 	
 			@Override
 			public boolean canCreate(ICreateContext context) {
 				if (context.getTargetContainer().getLink()!=null)
 					if (context.getTargetContainer().getLink().getBusinessObjects().size()==1) {
 						EObject obj = context.getTargetContainer().getLink().getBusinessObjects().get(0);
 						if (obj instanceof ActorContainerClass) {
 							if (obj instanceof ActorClass)
 								return isValidPosition(context, context, internal, StructureClassSupport.MARGIN);
 							else
 								return !internal;
 						}
 					}
 				return false;
 			}
 			
 			@Override
 			public boolean hasDoneChanges() {
 				return doneChanges;
 			}
 		}
 		
 		protected abstract static class AddFeature extends AbstractAddFeature {
 	
 			public AddFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 	
 			@Override
 			public boolean canAdd(IAddContext context) {
 				if (context.getNewObject() instanceof InterfaceItem) {
 					if (context.getTargetContainer().getLink().getBusinessObjects().size()==1) {
 						EObject obj = context.getTargetContainer().getLink().getBusinessObjects().get(0);
 						if (obj instanceof ActorContainerClass) {
 							return true;
 						}
 						if (obj instanceof ActorContainerRef) {
 							return true;
 						}
 					}
 				}
 				return false;
 			}
 	
 			@Override
 			public PictogramElement add(IAddContext context) {
 				InterfaceItem port = (InterfaceItem) context.getNewObject();
 				boolean internal = isInternal(port);
 				ContainerShape acShape = context.getTargetContainer();
 				Object bo = getBusinessObjectForPictogramElement(acShape);
 				boolean inherited = isInherited(port, bo, acShape);
 				boolean refport = (bo instanceof ActorContainerRef);
 	
 				int margin = refport?ITEM_SIZE_SMALL:ITEM_SIZE;
 				int size = refport?ITEM_SIZE_SMALL:ITEM_SIZE;
 
 				// CONTAINER SHAPE WITH RECTANGLE
 				IPeCreateService peCreateService = Graphiti.getPeCreateService();
 				ContainerShape containerShape =
 					peCreateService.createContainerShape(acShape, true);
 	
 				Graphiti.getPeService().setPropertyValue(containerShape, Constants.TYPE_KEY, Constants.PORT_TYPE);
 				
 				String kind = getItemKind(port);
 				Graphiti.getPeService().setPropertyValue(containerShape, PROP_KIND, kind);
 				
 				// the context point is the midpoint relative to the invisible rectangle
 				int x = context.getX();
 				int y = context.getY();
 				int width = acShape.getGraphicsAlgorithm().getWidth();
 				int height = acShape.getGraphicsAlgorithm().getHeight();
 				if (internal) {
 					if (x<2*margin)
 						x = 2*margin;
 					else if (x>width-2*margin)
 						x = width-2*margin;
 					if (y<2*margin)
 						y = 2*margin;
 					else if (y>height-2*margin)
 						y = height-2*margin;
 				}
 				else {
 					// TODOHRR: remove duplicate code
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
 				}
 				// finally we subtract the midpoint to get coordinates of the upper left corner
 				x -= margin;
 				y -= margin;
 				
 				Color dark = manageColor(inherited? INHERITED_COLOR:DARK_COLOR);
 				IGaService gaService = Graphiti.getGaService();
 				{
 					final Rectangle invisibleRectangle = gaService.createInvisibleRectangle(containerShape);
 					gaService.setLocationAndSize(invisibleRectangle, x, y, 2*size, 2*size);
 	
 					createItemFigure(port, refport,
 							containerShape,
 							invisibleRectangle,
 							dark,
 							manageColor(BRIGHT_COLOR));
 	
 					// create link and wire it
 					link(containerShape, port);
 				}
 				
 				{
 					Shape labelShape = peCreateService.createShape(containerShape, false);
 					Text label = gaService.createDefaultText(getDiagram(), labelShape, port.getName());
 					label.setForeground(dark);
 					label.setBackground(dark);
 					label.setHorizontalAlignment(Orientation.ALIGNMENT_CENTER);
 					label.setVerticalAlignment(Orientation.ALIGNMENT_CENTER);
 					gaService.setLocationAndSize(label, 0, 3*size/2, 2*size, size/2);
 					adjustLabel(label, x, y, width, margin, size);
 				}
 
 				// call the layout feature
 				layoutPictogramElement(containerShape);
 	
 				return containerShape;
 	
 			}
 	
 			protected abstract String getItemKind(InterfaceItem item);
 			protected abstract void createItemFigure(InterfaceItem item, boolean refitem,
 					ContainerShape containerShape,
 					GraphicsAlgorithm invisibleRectangle, Color darkColor, Color brightDolor);
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
 					if (bo instanceof InterfaceItem) {
 						InterfaceItem item = (InterfaceItem) bo;
 						
 						ContainerShape acShape = context.getTargetContainer();
 						Object parentBO = getBusinessObjectForPictogramElement(acShape);
 						boolean refport = (parentBO instanceof ActorContainerRef);
 						if (refport)
 							return true;
 						
 						if (isInherited(item, parentBO, acShape))
 							return false;
 						
 						int margin = refport?ActorContainerRefSupport.MARGIN:StructureClassSupport.MARGIN;
 						return isValidPosition(context, context, isInternal(item), margin);
 					}
 					return false;
 				}
 				
 				return canMove;
 			}
 			
 			@Override
 			protected void postMoveShape(IMoveShapeContext context) {
 				ContainerShape shapeToMove = (ContainerShape) context.getShape();
 	
 				InterfaceItem item = (InterfaceItem) getBusinessObjectForPictogramElement(shapeToMove);
 				boolean internal = isInternal(item);
 				ContainerShape acShape = context.getTargetContainer();
 				boolean refport = (getBusinessObjectForPictogramElement(acShape) instanceof ActorContainerRef);
 	
 				int margin = refport?ActorContainerRefSupport.MARGIN:StructureClassSupport.MARGIN;
 				int size = refport?ActorContainerRefSupport.MARGIN:ITEM_SIZE;
 	
 				int x = context.getX();
 				int y = context.getY();
 				int width = context.getTargetContainer().getGraphicsAlgorithm().getGraphicsAlgorithmChildren().get(0).getWidth();
 				int height = context.getTargetContainer().getGraphicsAlgorithm().getGraphicsAlgorithmChildren().get(0).getHeight();
 				
 				if (internal) {
 					// nothing to do
 				}
 				else {
 					// project onto boundary
 					if (refport) {
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
 					else {
 						if (x<=margin)
 							x = 0;
 						if (y<=margin)
 							y = 0;
 						if ((width-margin)<=x)
 							x = width;
 						if ((height-margin)<=y)
 							y = height;
 					}
 				}
 	
 				Graphiti.getGaService().setLocation(shapeToMove.getGraphicsAlgorithm(), x, y, avoidNegativeCoordinates());
 				
 				GraphicsAlgorithm ga = shapeToMove.getChildren().get(0).getGraphicsAlgorithm();
 				if (ga instanceof Text) {
 					adjustLabel((Text) ga, x, y, width, margin, size);
 				}
 			}
 		}
 		
 		protected abstract class UpdateFeature extends AbstractUpdateFeature {
 
 			public UpdateFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 
 			@Override
 			public boolean canUpdate(IUpdateContext context) {
 				Object bo = getBusinessObjectForPictogramElement(context.getPictogramElement());
 				if (bo instanceof EObject && ((EObject)bo).eIsProxy())
 					return true;
 				
 				return bo instanceof InterfaceItem;
 			}
 
 			@Override
 			public IReason updateNeeded(IUpdateContext context) {
 				Object bo = getBusinessObjectForPictogramElement(context.getPictogramElement());
 				if (bo instanceof EObject && ((EObject)bo).eIsProxy()) {
 					return Reason.createTrueReason("InterfaceItem deleted from model");
 				}
 				InterfaceItem port = (InterfaceItem) bo;
 				
 				String reason = "";
 				
 				// check if port still owned/inherited
 				ContainerShape containerShape = (ContainerShape)context.getPictogramElement();
 				bo = getBusinessObjectForPictogramElement(containerShape);
 				if (bo instanceof ActorClass) {
 					ActorClass ac = (ActorClass) bo;
 					boolean found = false;
 					do {
 						if (ac==port.eContainer())
 							found = true;
 						ac = ac.getBase();
 					}
 					while (!found && ac!=null);
 					
 					if (!found)
 						reason += "InterfaceItem not inherited anymore\n";
 				}
 				
 				GraphicsAlgorithm ga = containerShape.getChildren().get(0).getGraphicsAlgorithm();
 				if (ga instanceof Text) {
 					if (!port.getName().equals(((Text)ga).getValue()))
 						reason += "Name is out of date\n";
 
 					String kind = getItemKind(port);
 					if (!kind.equals(Graphiti.getPeService().getPropertyValue(context.getPictogramElement(), PROP_KIND)))
 						reason += "Figure is out of date\n";
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
 				InterfaceItem port = (InterfaceItem) bo;
 				
 				boolean inherited = isInherited(port, bo, containerShape);
 				
 				Color dark = manageColor(inherited? INHERITED_COLOR:DARK_COLOR);
 				updateFigure(port, containerShape, dark, manageColor(BRIGHT_COLOR));
 				String kind = getItemKind(port);
 				Graphiti.getPeService().setPropertyValue(containerShape, PROP_KIND, kind);
 				return true;
 			}
 			
 			protected abstract String getItemKind(InterfaceItem item);
 			protected abstract void updateFigure(InterfaceItem item, PictogramElement pe, Color dark, Color bright);
 		}
 		
 		
 		protected static abstract class PropertyFeature extends AbstractCustomFeature {
 
 			private String name;
 			private String description;
 
 			public PropertyFeature(IFeatureProvider fp, String name, String description) {
 				super(fp);
 				this.name = name;
 				this.description = description;
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
 					if (bo instanceof InterfaceItem) {
 						ContainerShape shape = ((ContainerShape)pes[0]).getContainer();
 						InterfaceItem item = (InterfaceItem) bo;
 						Object parentBO = getBusinessObjectForPictogramElement(shape.getContainer());
 						
 						return !isRefItem(shape) && !isInherited(item, parentBO, shape);
 					}
 				}
 				return false;
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
 				ContainerShape shape = (ContainerShape) context.getPictogramElement();
				InterfaceItem item = (InterfaceItem) getBusinessObjectForPictogramElement(shape);
 				Object parentBO = getBusinessObjectForPictogramElement(shape.getContainer());
 				
 				return !isRefItem(shape) && !isInherited(item, parentBO, shape);
 			}
 		}
 		
 		protected IFeatureProvider fp;
 		
 		protected FeatureProvider(IDiagramTypeProvider dtp, IFeatureProvider fp) {
 			super(dtp);
 			this.fp = fp;
 		}
 
 		protected static boolean isRefItem(PictogramElement pe) {
 			if (!(pe instanceof ContainerShape))
 				return false;
 			
 			ContainerShape acShape = ((ContainerShape)pe).getContainer();
 			
 			if (acShape.getLink()==null || acShape.getLink().getBusinessObjects().isEmpty())
 				return false;
 			
 			Object parent = acShape.getLink().getBusinessObjects().get(0);
 			
 			return (parent instanceof ActorContainerRef);
 		}
 	
 		protected static boolean isInternal(InterfaceItem item) {
 			if (item instanceof Port) {
 				Port port = (Port) item;
 				
 				// NB: the port's container might be a base class of the depicted actor class
 				ActorContainerClass acc = (ActorContainerClass) port.eContainer();
 				if (acc instanceof ActorClass) {
 					ActorClass ac = (ActorClass) acc;
 					if (ac.getIntPorts().contains(port))
 						return true;
 				}
 			}
 			else if (item instanceof SPPRef) {
 				return false;
 			}
 			else {
 				assert(false): "unexpected sub type";
 			}
 
 			return false;
 		}
 		
 		protected static boolean isInherited(InterfaceItem item, Object container, ContainerShape cs) {
 			if (container instanceof ActorClass) {
 				ActorClass ac = (ActorClass) container;
 				return item.eContainer()!=ac;
 			}
 			else if (container instanceof ActorRef) {
 				// have to check whether the ActorRef is inherited
 				ActorRef ar = (ActorRef) container;
 				ContainerShape arCont = cs.getContainer();
 				EObject cls = arCont.getLink().getBusinessObjects().get(0);
 				if (cls instanceof ActorClass)
 					return ar.eContainer()!=cls;
 
 				// cls is a SubSystemClass
 				return false;
 			}
 			else if (container instanceof SubSystemRef) {
 				// SubSystemRefs only occur in LogicalSystems, no inheritance
 				return false;
 			}
 
 			return false;
 		}
 		
 		protected static boolean isValidPosition(ILocationContext loc, ITargetContext tgt, boolean internal, int margin) {
 			//System.out.println("isValidPosition "+tgt.getTargetContainer());
 			if (tgt.getTargetContainer().getGraphicsAlgorithm()==null)
 				return false;
 			if (tgt.getTargetContainer().getGraphicsAlgorithm().getGraphicsAlgorithmChildren().isEmpty())
 				return false;
 
 			int x = loc.getX();
 			int y = loc.getY();
 			if (loc instanceof ICreateContext) {
 				// adjust position as relative to visible rectangle
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
 
 			if (internal)
 				return !inStripe;
 			else
 				return inStripe;
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
 		public IRemoveFeature getRemoveFeature(IRemoveContext context) {
 			return new RemoveFeature(fp);
 		}
 		
 		@Override
 		public IDeleteFeature getDeleteFeature(IDeleteContext context) {
 			return new DeleteFeature(fp);
 		}
 		
 		protected static void adjustLabel(Text label, int x, int y, int width, int margin, int size) {
 			Orientation align = Orientation.ALIGNMENT_CENTER;
 			label.setHorizontalAlignment(align);
 	
 			int pos = 3*size/2;
 			
 			if (x<=margin)
 				align = Orientation.ALIGNMENT_LEFT;
 			else if ((width-margin)<=x)
 				align = Orientation.ALIGNMENT_RIGHT;
 			if (y<=margin)
 				pos = 0;
 	
 			if (align!=label.getHorizontalAlignment()) {
 				label.setHorizontalAlignment(align);
 			}
 			if (pos!=label.getY()) {
 				IGaService gaService = Graphiti.getGaService();
 				gaService.setLocationAndSize(label, 0, pos, 2*size, size/2);
 			}
 		}
 		
 	}
 
 	protected static class BehaviorProvider extends DefaultToolBehaviorProvider {
 
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
 		public String getToolTip(GraphicsAlgorithm ga) {
 			// if this is called we know there is a business object!=null
 			PictogramElement pe = ga.getPictogramElement();
 			
 			EObject bo = Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement(pe);
 			if (bo instanceof InterfaceItem) {
 				String name = ((InterfaceItem) bo).getName();
 				String protocol = ((InterfaceItem) bo).getProtocol().getName();
 				if (bo instanceof Port)
 					if (((Port) bo).isConjugated())
 						protocol = "conj "+protocol;
 				
 				return name+"\n("+protocol+")";
 			}
 			return super.getToolTip(ga);
 		}
 	}
 	
 	public static void createRefItems(ActorContainerRef acr, ContainerShape refShape, IFeatureProvider featureProvider) {
 		
 		ActorContainerClass refClass = (acr instanceof ActorRef)?((ActorRef)acr).getType():((SubSystemRef)acr).getType();
 		List<? extends InterfaceItem> refItems = RoomHelpers.getInterfaceItems(refClass, true);
 		
 		if (refShape!=null && refClass!=null &&!refItems.isEmpty()) {
 			
 			// the diagram will be created and initialized if not present
 			// TODOHRR: how to proceed if diagram not up to date?
 			Diagram refDiag = new DiagramAccess().getDiagram(refClass);
 
 			ResourceSet rs = acr.eResource().getResourceSet();
 			
 			if (!refDiag.getChildren().isEmpty()) {
 				ContainerShape refAcShape = (ContainerShape) refDiag.getChildren().get(0);
 				Object bo = featureProvider.getBusinessObjectForPictogramElement(refAcShape);
 				if (bo instanceof ActorContainerClass) {
 					ActorContainerClass extRefClass = (ActorContainerClass) bo;
 					assert(extRefClass.getName().equals(refClass.getName())): "structure class names must match";
 
 					List<InterfaceItem> extRefItems = RoomHelpers.getInterfaceItems(extRefClass, true);
 					List<InterfaceItem> intRefItems = SupportUtil.getInterfaceItems(refShape, featureProvider);
 					
 					int scaleX = refAcShape.getGraphicsAlgorithm().getWidth()/ActorContainerRefSupport.DEFAULT_SIZE_X;
 					int scaleY = refAcShape.getGraphicsAlgorithm().getHeight()/ActorContainerRefSupport.DEFAULT_SIZE_Y;
 					for (Shape childShape : refAcShape.getChildren()) {
 						bo = featureProvider.getBusinessObjectForPictogramElement(childShape);
 						if (bo instanceof InterfaceItem) {
 							if (extRefItems.contains(bo)) {
 								// this is an interface item, insert it
 
 								EObject ownObject = SupportUtil.getOwnObject((InterfaceItem)bo, rs);
 								if (!intRefItems.contains(ownObject)) {
 									int x = ITEM_SIZE_SMALL/2 + childShape.getGraphicsAlgorithm().getX()/scaleX;
 									int y = ITEM_SIZE_SMALL/2 + childShape.getGraphicsAlgorithm().getY()/scaleY;
 									SupportUtil.addItem(ownObject, x, y, refShape, featureProvider);
 								}
 							}
 						}
 					}
 				}
 			}
 			else {
 				assert(false): "empty referenced structure class diagram";
 			}
 		}
 	}
 
 	protected static void createInheritedRefItems(ActorContainerRef acr, ContainerShape arShape, IFeatureProvider fp) {
 		
 		ActorClass ac = (ActorClass) acr.eContainer();
 		ResourceSet rs = ac.eResource().getResourceSet();
 		List<InterfaceItem> presentObjects = SupportUtil.getInterfaceItems(arShape, fp);
 
 		Diagram refDiag = new DiagramAccess().getDiagram(ac);
 		if (!refDiag.getChildren().isEmpty()) {
 			ContainerShape refAcShape = (ContainerShape) refDiag.getChildren().get(0);
 			Object bo = fp.getBusinessObjectForPictogramElement(refAcShape);
 			if (bo instanceof ActorClass) {
 				ActorClass extRefClass = (ActorClass) bo;
 				assert(extRefClass.getName().equals(ac.getName())): "actor class names must match";
 				
 				for (Shape childShape : refAcShape.getChildren()) {
 					bo = fp.getBusinessObjectForPictogramElement(childShape);
 					if (bo instanceof ActorRef) {
 						EObject ownObject = SupportUtil.getOwnObject((ActorRef)bo, rs);
 						if (ownObject==acr) {
 							int subScaleX = arShape.getGraphicsAlgorithm().getWidth()/ActorContainerRefSupport.DEFAULT_SIZE_X;
 							int subScaleY = arShape.getGraphicsAlgorithm().getHeight()/ActorContainerRefSupport.DEFAULT_SIZE_Y;
 							
 							// add items of actor ref
 							for (Shape grandChildShape : ((ContainerShape)childShape).getChildren()) {
 								bo = fp.getBusinessObjectForPictogramElement(grandChildShape);
 								if (bo instanceof InterfaceItem) {
 									ownObject = SupportUtil.getOwnObject((Port)bo, rs);
 									if (!presentObjects.contains(ownObject)) {
 										int x = ITEM_SIZE_SMALL/2 + grandChildShape.getGraphicsAlgorithm().getX()/subScaleX;
 										int y = ITEM_SIZE_SMALL/2 + grandChildShape.getGraphicsAlgorithm().getY()/subScaleY;
 										SupportUtil.addItem(ownObject, x, y, arShape, fp);
 									}
 								}
 							}
 							break;
 						}
 					}
 				}
 			}
 		}
 	}
 }
