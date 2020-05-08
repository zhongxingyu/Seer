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
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.etrice.core.naming.RoomNameProvider;
 import org.eclipse.etrice.core.room.ChoicePoint;
 import org.eclipse.etrice.core.room.State;
 import org.eclipse.etrice.core.room.StateGraph;
 import org.eclipse.etrice.core.room.StructureClass;
 import org.eclipse.etrice.core.room.TrPoint;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.features.IAddFeature;
 import org.eclipse.graphiti.features.IDeleteFeature;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.ILayoutFeature;
 import org.eclipse.graphiti.features.IReason;
 import org.eclipse.graphiti.features.IRemoveFeature;
 import org.eclipse.graphiti.features.IResizeShapeFeature;
 import org.eclipse.graphiti.features.IUpdateFeature;
 import org.eclipse.graphiti.features.context.IAddContext;
 import org.eclipse.graphiti.features.context.ICustomContext;
 import org.eclipse.graphiti.features.context.IDeleteContext;
 import org.eclipse.graphiti.features.context.IDoubleClickContext;
 import org.eclipse.graphiti.features.context.ILayoutContext;
 import org.eclipse.graphiti.features.context.IRemoveContext;
 import org.eclipse.graphiti.features.context.IResizeShapeContext;
 import org.eclipse.graphiti.features.context.IUpdateContext;
 import org.eclipse.graphiti.features.context.impl.RemoveContext;
 import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
 import org.eclipse.graphiti.features.custom.ICustomFeature;
 import org.eclipse.graphiti.features.impl.AbstractAddFeature;
 import org.eclipse.graphiti.features.impl.AbstractLayoutFeature;
 import org.eclipse.graphiti.features.impl.AbstractUpdateFeature;
 import org.eclipse.graphiti.features.impl.DefaultRemoveFeature;
 import org.eclipse.graphiti.features.impl.DefaultResizeShapeFeature;
 import org.eclipse.graphiti.features.impl.Reason;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.algorithms.Rectangle;
 import org.eclipse.graphiti.mm.algorithms.RoundedRectangle;
 import org.eclipse.graphiti.mm.algorithms.Text;
 import org.eclipse.graphiti.mm.algorithms.styles.Font;
 import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.IGaService;
 import org.eclipse.graphiti.services.IPeCreateService;
 import org.eclipse.graphiti.tb.DefaultToolBehaviorProvider;
 import org.eclipse.graphiti.tb.IToolBehaviorProvider;
 import org.eclipse.graphiti.ui.features.DefaultDeleteFeature;
 import org.eclipse.graphiti.ui.features.DefaultFeatureProvider;
 import org.eclipse.graphiti.util.ColorConstant;
 import org.eclipse.graphiti.util.IColorConstant;
 
 public class StateGraphSupport {
 	
 	public static final int MARGIN = 20;
 	
 	private static final int CORNER_SIZE = 20;
 	private static final int LINE_WIDTH = 4;
 	public static final int DEFAULT_SIZE_X = 800;
 	public static final int DEFAULT_SIZE_Y = 500;
 	private static final IColorConstant LINE_COLOR = new ColorConstant(0, 0, 0);
 	private static final IColorConstant BACKGROUND = new ColorConstant(255, 255, 255);
 
 	private static class FeatureProvider extends DefaultFeatureProvider {
 	
 		private class AddFeature extends AbstractAddFeature {
 	
 			public AddFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 	
 			@Override
 			public boolean canAdd(IAddContext context) {
 				if (context.getNewObject() instanceof StateGraph) {
 					if (context.getTargetContainer() instanceof Diagram) {
 						return true;
 					}
 				}
 				return false;
 			}
 	
 			@Override
 			public PictogramElement add(IAddContext context) {
 				StateGraph sg = (StateGraph) context.getNewObject();
 				ContainerShape targetContainer = (ContainerShape) context.getTargetContainer();
 	
 				// CONTAINER SHAPE WITH RECTANGLE
 				IPeCreateService peCreateService = Graphiti.getPeCreateService();
 				ContainerShape containerShape =
 					peCreateService.createContainerShape(targetContainer, true);
 				
 				Graphiti.getPeService().setPropertyValue(containerShape, Constants.TYPE_KEY, Constants.SG_TYPE);
 
 				// check whether the context has a size (e.g. from a create feature)
 				// otherwise define a default size for the shape
 				int width = context.getWidth() <= 0 ? DEFAULT_SIZE_X : context.getWidth();
 				int height = context.getHeight() <= 0 ? DEFAULT_SIZE_Y : context.getHeight();
 	
 				IGaService gaService = Graphiti.getGaService();
 				{
 
 					// create invisible outer rectangle expanded by
 					// the width needed for the ports
 					Rectangle invisibleRectangle =
 						gaService.createInvisibleRectangle(containerShape);
 	
 					gaService.setLocationAndSize(invisibleRectangle,
 							context.getX(), context.getY(), width + 2*MARGIN, height + 2*MARGIN);
 	
 					// create and set visible rectangle inside invisible rectangle
 					RoundedRectangle rect = gaService.createRoundedRectangle(invisibleRectangle, CORNER_SIZE, CORNER_SIZE);
 					rect.setForeground(manageColor(LINE_COLOR));
 					rect.setBackground(manageColor(BACKGROUND));
 					rect.setLineWidth(LINE_WIDTH);
 					gaService.setLocationAndSize(rect, MARGIN, MARGIN, width, height);
 	
 					// create link and wire it
 					link(containerShape, sg);
 				}
 				
 				{
 					Shape labelShape = peCreateService.createShape(containerShape, false);
 					Text label = gaService.createDefaultText(labelShape, RoomNameProvider.getStateGraphLabel(sg));
 					label.setForeground(manageColor(LINE_COLOR));
 					label.setBackground(manageColor(LINE_COLOR));
 					label.setHorizontalAlignment(Orientation.ALIGNMENT_RIGHT);
 					label.setVerticalAlignment(Orientation.ALIGNMENT_TOP);
 					Font font = label.getFont();
 					font.setSize((int) (label.getFont().getSize()*1.2));
 					font.setBold(true);
 					label.setFont(font);
 					gaService.setLocationAndSize(label, 0, MARGIN, width, 2*MARGIN);
 				}
 	
 				// call the layout feature
 				layoutPictogramElement(containerShape);
 	
 				return containerShape;
 			}
 		}
 	
 		private class LayoutFeature extends AbstractLayoutFeature {
 	
 			private static final int MIN_HEIGHT = 100;
 			private static final int MIN_WIDTH = 250;
 	
 			public LayoutFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 	
 			@Override
 			public boolean canLayout(ILayoutContext context) {
 				// return true, if pictogram element is linked to an ActorClass
 				PictogramElement pe = context.getPictogramElement();
 				if (!(pe instanceof ContainerShape))
 					return false;
 	
 				EList<EObject> businessObjects = pe.getLink().getBusinessObjects();
 				return businessObjects.size() == 1
 						&& businessObjects.get(0) instanceof StateGraph;
 			}
 	
 			@Override
 			public boolean layout(ILayoutContext context) {
 				boolean anythingChanged = false;
 				ContainerShape containerShape = (ContainerShape) context
 						.getPictogramElement();
 	
 				GraphicsAlgorithm containerGa = containerShape.getGraphicsAlgorithm();
 	
 				// height
 				if (containerGa.getHeight() < MIN_HEIGHT) {
 					containerGa.setHeight(MIN_HEIGHT);
 					anythingChanged = true;
 				}
 	
 				// width
 				if (containerGa.getWidth() < MIN_WIDTH) {
 					containerGa.setWidth(MIN_WIDTH);
 					anythingChanged = true;
 				}
 	
 				int w = containerGa.getWidth();
 				int h = containerGa.getHeight();
 	
				if (containerGa.getGraphicsAlgorithmChildren().size()>=1) {
 					GraphicsAlgorithm ga = containerGa.getGraphicsAlgorithmChildren().get(0);
 					ga.setWidth(w-2*MARGIN);
 					ga.setHeight(h-2*MARGIN);
 					anythingChanged = true;
 				}
				
				if (containerShape.getChildren().size()>=1) {
					GraphicsAlgorithm ga = containerShape.getChildren().get(0).getGraphicsAlgorithm();
					ga.setWidth(w-2*MARGIN);
				}
 	
 				return anythingChanged;
 			}
 	
 		}
 		
 		private static class GoUpFeature extends AbstractCustomFeature implements
 				ICustomFeature {
 
 			public GoUpFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 
 			@Override
 			public String getName() {
 				return "Switch to SuperGraph";
 			}
 			
 			@Override
 			public String getDescription() {
 				return "Switch Context to SuperGraph";
 			}
 
 			@Override
 			public boolean canExecute(ICustomContext context) {
 				ContainerShape container = (ContainerShape)context.getPictogramElements()[0];
 				Object bo = getBusinessObjectForPictogramElement(container);
 				if (bo instanceof StateGraph) {
 					if (((StateGraph) bo).eContainer() instanceof State)
 						return true;
 				}
 				return false;
 			}
 			
 			@Override
 			public void execute(ICustomContext context) {
 				ContainerShape container = (ContainerShape)context.getPictogramElements()[0];
 				Object bo = getBusinessObjectForPictogramElement(container);
 				if (bo instanceof StateGraph) {
 					StateGraph sg = (StateGraph) bo;
 					getDiagramEditor().selectPictogramElements(new PictogramElement[] {});
 					ContextSwitcher.goUp(getDiagram(), sg);
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
 				
 				return bo instanceof StructureClass;
 			}
 
 			@Override
 			public IReason updateNeeded(IUpdateContext context) {
 				Object bo = getBusinessObjectForPictogramElement(context.getPictogramElement());
 				if (bo instanceof EObject && ((EObject)bo).eIsProxy()) {
 					return Reason.createTrueReason("State Graph deleted from model");
 				}
 				
 				// TODOHRR: update of state graph - check for refs added in model not present in diagram
 				// also inherited
 				// also label
 				
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
 
 				// TODOHRR: check for refs added in model not present in diagram
 				
 				return true;
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
 
 				ContainerShape containerShape = (ContainerShape)context.getShape();
 				Object bo = getBusinessObjectForPictogramElement(containerShape);
 				if (bo instanceof EObject && ((EObject)bo).eIsProxy())
 					return false;
 				
 				int width = context.getWidth()-MARGIN;
 				int height = context.getHeight()-MARGIN;
 				int xmax = 0;
 				int ymax = 0;
 				StateGraph sg = (StateGraph) bo;
 				for (Shape childShape : containerShape.getChildren()) {
 					if (isOnInterface(sg, getBusinessObjectForPictogramElement(childShape)))
 						continue;
 					
 					bo = getBusinessObjectForPictogramElement(childShape);
 					int margin = -1;
 					if (bo instanceof State)
 						margin = StateSupport.MARGIN;
 					else if (bo instanceof TrPoint)
 						margin = StateSupport.MARGIN;
 					else if (bo instanceof ChoicePoint)
 						margin = StateSupport.MARGIN;
 					if (margin>=0) {
 						GraphicsAlgorithm ga = childShape.getGraphicsAlgorithm();
 						int x = ga.getX()+ga.getWidth()-margin;
 						int y = ga.getY()+ga.getHeight()-margin;
 						if (x>xmax)
 							xmax = x;
 						if (y>ymax)
 							ymax = y;
 					}
 				}
 				if (width>0 && width<xmax)
 					return false;
 				if (height>0 && height<ymax)
 					return false;
 				
 				return true;
 			}
 			
 			@Override
 			public void resizeShape(IResizeShapeContext context) {
 				ContainerShape containerShape = (ContainerShape) context.getShape();
 				StateGraph sg = (StateGraph) getBusinessObjectForPictogramElement(containerShape);
 				
 				if (containerShape.getGraphicsAlgorithm()!=null) {
 					GraphicsAlgorithm containerGa = containerShape.getGraphicsAlgorithm();
 					if (containerGa.getGraphicsAlgorithmChildren().size()==1) {
 						// scale interface item coordinates
 						// we refer to the visible rectangle which defines the border of our structure class
 						// since the margin is not scaled
 						GraphicsAlgorithm ga = containerGa.getGraphicsAlgorithmChildren().get(0);
 						double sx = (context.getWidth()-2*MARGIN)/(double)ga.getWidth();
 						double sy = (context.getHeight()-2*MARGIN)/(double)ga.getHeight();
 						
 						for (Shape childShape : containerShape.getChildren()) {
 							if (isOnInterface(sg, getBusinessObjectForPictogramElement(childShape))) {
 								ga = childShape.getGraphicsAlgorithm();
 								ga.setX((int) (ga.getX()*sx));
 								ga.setY((int) (ga.getY()*sy));
 							}
 						}
 					}
 				}
 				super.resizeShape(context);
 			}
 
 			private boolean isOnInterface(StateGraph sg, Object childBo) {
 				return (childBo instanceof TrPoint);
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
 				return false;
 			}
 		}
 		
 		private IFeatureProvider fp;
 	
 		public FeatureProvider(IDiagramTypeProvider dtp, IFeatureProvider fp) {
 			super(dtp);
 			this.fp = fp;
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
 			return new ICustomFeature[] { new GoUpFeature(fp) };
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
 			return new FeatureProvider.GoUpFeature(getDiagramTypeProvider().getFeatureProvider());
 		}
 	}
 
 	private FeatureProvider afp;
 	private BehaviorProvider tbp;
 		
 	public StateGraphSupport(IDiagramTypeProvider dtp, IFeatureProvider fp) {
 		afp = new FeatureProvider(dtp, fp);
 		tbp = new BehaviorProvider(dtp);
 	}
 	
 	public IFeatureProvider getFeatureProvider() {
 		return afp;
 	}
 	
 	public IToolBehaviorProvider getToolBehaviorProvider() {
 		return tbp;
 	}
 }
