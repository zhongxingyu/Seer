 /*******************************************************************************
  * Copyright (c) 2011, 2012, 2013 Red Hat, Inc.
  * All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * 	Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.eclipse.bpmn2.modeler.ui.editor;
 
 import org.eclipse.bpmn2.BaseElement;
 import org.eclipse.bpmn2.MessageFlow;
 import org.eclipse.bpmn2.SubProcess;
 import org.eclipse.bpmn2.modeler.core.utils.BusinessObjectUtil;
 import org.eclipse.bpmn2.modeler.ui.features.flow.MessageFlowFeatureContainer;
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.IClippingStrategy;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.GraphicalEditPart;
 import org.eclipse.gef.GraphicalViewer;
 import org.eclipse.gef.LayerConstants;
 import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.pictograms.Connection;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.services.Graphiti;
 
 public class ConnectionLayerClippingStrategy implements IClippingStrategy {
 
 	protected Diagram diagram;
 	protected GraphicalViewer graphicalViewer;
 	
 	public static void applyTo(GraphicalViewer graphicalViewer) {
 		ScalableFreeformRootEditPart rootEditPart = (ScalableFreeformRootEditPart) graphicalViewer.getRootEditPart();
 		Figure connectionLayer = (Figure) rootEditPart.getLayer(LayerConstants.CONNECTION_LAYER);
 		if (connectionLayer.getClippingStrategy()==null) {
 			EditPart editPart = graphicalViewer.getContents();
 			Diagram diagram = (Diagram)editPart.getModel();
 			IClippingStrategy clippingStrategy = new ConnectionLayerClippingStrategy(graphicalViewer, diagram);
 			connectionLayer.setClippingStrategy(clippingStrategy);
 		}
 	}
 
 	public ConnectionLayerClippingStrategy(GraphicalViewer graphicalViewer, Diagram diagram) {
 		this.diagram = diagram;
 		this.graphicalViewer = graphicalViewer;
 	}
 	
 	@Override
 	public Rectangle[] getClip(IFigure childFigure) {
 		for (Object value : graphicalViewer.getEditPartRegistry().values()) {
 			GraphicalEditPart part = (GraphicalEditPart)value;
 			if (part.getFigure() == childFigure) {
 				Object model = part.getModel();
 				if (model instanceof Connection) {
 					Connection connection = (Connection)model;
 					BaseElement businessObject = BusinessObjectUtil.getFirstBaseElement(connection);
 					if (businessObject instanceof MessageFlow) {
 						ContainerShape messageShape = MessageFlowFeatureContainer.findMessageShape(connection);
 						if (messageShape!=null) {
 							Rectangle inner = getClip(messageShape)[0];
 							Rectangle outer = childFigure.getBounds();
 							return getClip(outer,inner);
 						}
 					}
 					else {
 						EObject container = businessObject.eContainer();
 						if (container instanceof SubProcess) {
 							for (PictogramElement pe : Graphiti.getLinkService().getPictogramElements(diagram, container)) {
 								if (pe instanceof ContainerShape) {
 									return getClip((ContainerShape)pe);
 								}
 							}
 							
 						}
 					}
 				}
 			}
 		}
 		return new Rectangle[] {childFigure.getBounds()};
 	}
 	
 	private Rectangle[] getClip(Rectangle outer, Rectangle inner) {
 		if (outer.width > inner.width) {
 			if (outer.height > inner.height) {
 				Rectangle[] clip = new Rectangle[4];
 				clip[0] = new Rectangle(
 						outer.x, outer.y,
 						outer.width, inner.y - outer.y);
 				clip[1] = new Rectangle(
 						outer.x, inner.y,
 						inner.x - outer.x,
 						inner.height
 						);
 				clip[2] = new Rectangle(
 						inner.x + inner.width, inner.y,
 						(outer.x + outer.width) - (inner.x + inner.width),
 						inner.height
 						);
 				clip[3] = new Rectangle(
 						outer.x, inner.y + inner.height,
 						outer.width, (outer.y + outer.height) - (inner.y + inner.height));
 				
 				return clip;
 			}
 		}
 		return new Rectangle[] {outer};
 	}
 	
 	private Rectangle[] getClip(ContainerShape pe) {
 		GraphicsAlgorithm ga = pe.getGraphicsAlgorithm();
 		return new Rectangle[] { new Rectangle(ga.getX(), ga.getY(), ga.getWidth(), ga.getHeight()) };
 	}
 }
