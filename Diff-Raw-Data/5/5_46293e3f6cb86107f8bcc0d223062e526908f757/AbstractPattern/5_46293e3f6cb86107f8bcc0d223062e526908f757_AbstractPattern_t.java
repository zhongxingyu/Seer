 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2010 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.pattern;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.graphiti.features.DefaultResizeConfiguration;
 import org.eclipse.graphiti.features.IDirectEditingInfo;
 import org.eclipse.graphiti.features.IReason;
 import org.eclipse.graphiti.features.IResizeConfiguration;
 import org.eclipse.graphiti.features.context.IAreaContext;
 import org.eclipse.graphiti.features.context.ICreateContext;
 import org.eclipse.graphiti.features.context.ILayoutContext;
 import org.eclipse.graphiti.features.context.IMoveShapeContext;
 import org.eclipse.graphiti.features.context.IResizeShapeContext;
 import org.eclipse.graphiti.features.context.IUpdateContext;
 import org.eclipse.graphiti.features.context.impl.AddContext;
 import org.eclipse.graphiti.features.context.impl.LayoutContext;
 import org.eclipse.graphiti.features.context.impl.UpdateContext;
 import org.eclipse.graphiti.features.impl.Reason;
 import org.eclipse.graphiti.mm.algorithms.styles.Point;
 import org.eclipse.graphiti.mm.pictograms.Anchor;
 import org.eclipse.graphiti.mm.pictograms.Connection;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.FreeFormConnection;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.PictogramLink;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.pattern.config.IPatternConfiguration;
 import org.eclipse.graphiti.pattern.mapping.IStructureMapping;
 import org.eclipse.graphiti.pattern.mapping.data.IDataMapping;
 import org.eclipse.graphiti.pattern.mapping.data.IImageDataMapping;
 import org.eclipse.graphiti.pattern.mapping.data.ITextDataMapping;
 import org.eclipse.graphiti.services.Graphiti;
 
 /**
  * The Class AbstractPattern.
  */
 public abstract class AbstractPattern extends AbstractBasePattern implements IPattern {
 
 	private IPatternConfiguration patternConfiguration;
 
 	/**
 	 * Creates a new {@link AbstractPattern}.
 	 * 
 	 * @param patternConfiguration
 	 *            the pattern configuration
 	 */
 	public AbstractPattern(IPatternConfiguration patternConfiguration) {
 		setPatternConfiguration(patternConfiguration);
 	}
 
 	public boolean canCreate(ICreateContext context) {
 		return false;
 	}
 
 	public boolean canLayout(ILayoutContext context) {
 		PictogramElement pictogramElement = context.getPictogramElement();
 		return isPatternControlled(pictogramElement);
 	}
 
 	public boolean canMoveShape(IMoveShapeContext context) {
 		return context.getSourceContainer() != null && context.getSourceContainer().equals(context.getTargetContainer())
 				&& isPatternRoot(context.getPictogramElement());
 	}
 
 	public boolean canResizeShape(IResizeShapeContext context) {
 		return isPatternRoot(context.getPictogramElement());
 	}
 
 	public boolean canUpdate(IUpdateContext context) {
 		PictogramElement pictogramElement = context.getPictogramElement();
 		return isPatternControlled(pictogramElement);
 	}
 
 	public Object[] create(ICreateContext context) {
 		return EMPTY;
 	}
 
 	public String getCreateDescription() {
 		return null;
 	}
 
 	public String getCreateImageId() {
 		return null;
 	}
 
 	public String getCreateLargeImageId() {
 		return getCreateImageId();
 	}
 
 	public String getCreateName() {
 		return null;
 	}
 
 	abstract public boolean isMainBusinessObjectApplicable(Object mainBusinessObject);
 
 	public boolean layout(ILayoutContext context) {
 		return false;
 	}
 
 	final public void moveShape(IMoveShapeContext context) {
 		preMoveShape(context);
 		moveAllBendpoints(context);
 		internalMove(context);
 		postMoveShape(context);
 	}
 
 	/**
 	 * Post move shape.
 	 * 
 	 * @param context
 	 *            the move shape context
 	 */
 	protected void postMoveShape(IMoveShapeContext context) {
 	}
 
 	/**
 	 * Pre move shape.
 	 * 
 	 * @param context
 	 *            the move shape context
 	 */
 	protected void preMoveShape(IMoveShapeContext context) {
 	}
 
 	/**
 	 * Internal move.
 	 * 
 	 * @param context
 	 *            the move shape context
 	 */
 	protected void internalMove(IMoveShapeContext context) {
 		Shape shapeToMove = context.getShape();
 		ContainerShape oldContainerShape = context.getSourceContainer();
 		ContainerShape newContainerShape = context.getTargetContainer();
 
 		int x = context.getX();
 		int y = context.getY();
 
 		if (oldContainerShape != newContainerShape) {
 			// the following is a workaround due to an MMR bug
 			if (oldContainerShape != null) {
 				Collection<Shape> children = oldContainerShape.getChildren();
 				if (children != null) {
 					children.remove(shapeToMove);
 				}
 			}
 
 			shapeToMove.setContainer(newContainerShape);
 			if (shapeToMove.getGraphicsAlgorithm() != null) {
 				Graphiti.getGaService().setLocation(shapeToMove.getGraphicsAlgorithm(), x, y, avoidNegativeCoordinates());
 			}
 		} else { // move within the same container
 			if (shapeToMove.getGraphicsAlgorithm() != null) {
 				Graphiti.getGaService().setLocation(shapeToMove.getGraphicsAlgorithm(), x, y, avoidNegativeCoordinates());
 			}
 		}
 	}
 
 	/**
 	 * Move all bendpoints within a container shape.
 	 * 
 	 * @param context
 	 *            the move shape context
 	 */
 	protected void moveAllBendpoints(IMoveShapeContext context) {
 
 		if (!(context.getShape() instanceof ContainerShape)) {
 			return;
 		}
 
 		ContainerShape shapeToMove = (ContainerShape) context.getShape();
 
 		int x = context.getX();
 		int y = context.getY();
 
 		int deltaX = x - shapeToMove.getGraphicsAlgorithm().getX();
 		int deltaY = y - shapeToMove.getGraphicsAlgorithm().getY();
 
 		if (deltaX != 0 || deltaY != 0) {
 
 			List<Anchor> anchorsFrom = getAnchors(shapeToMove);
 			List<Anchor> anchorsTo = new ArrayList<Anchor>(anchorsFrom);
 
 			for (Anchor anchorFrom : anchorsFrom) {
 
 				Collection<Connection> outgoingConnections = anchorFrom.getOutgoingConnections();
 
 				for (Connection connection : outgoingConnections) {
 					for (Anchor anchorTo : anchorsTo) {
 
 						Collection<Connection> incomingConnections = anchorTo.getIncomingConnections();
 						if (incomingConnections.contains(connection)) {
 							if (connection instanceof FreeFormConnection) {
 								FreeFormConnection ffc = (FreeFormConnection) connection;
 								List<Point> points = ffc.getBendpoints();
 								for (int i = 0; i < points.size(); i++) {
 									Point point = points.get(i);
 									int oldX = point.getX();
 									int oldY = point.getY();
 									points.set(i, Graphiti.getGaService().createPoint(oldX + deltaX, oldY + deltaY));
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private List<Anchor> getAnchors(ContainerShape containerShape) {
 		List<Anchor> ret = new ArrayList<Anchor>();
 		ret.addAll(containerShape.getAnchors());
 
 		List<Shape> children = containerShape.getChildren();
 		for (Shape shape : children) {
 			if (shape instanceof ContainerShape) {
 				ret.addAll(getAnchors((ContainerShape) shape));
 			} else {
 				ret.addAll(shape.getAnchors());
 			}
 		}
 		return ret;
 	}
 
 	public void resizeShape(IResizeShapeContext context) {
 		Shape shape = context.getShape();
		int x = context.getX();
		int y = context.getY();
 		int width = context.getWidth();
 		int height = context.getHeight();
 
 		if (shape.getGraphicsAlgorithm() != null) {
			Graphiti.getGaService().setLocationAndSize(shape.getGraphicsAlgorithm(), x, y, width, height);
 		}
 
 		layoutPictogramElement(shape);
 	}
 
 	public boolean update(IUpdateContext context) {
 		return false;
 	}
 
 	public IReason updateNeeded(IUpdateContext context) {
 		return Reason.createFalseReason();
 	}
 
 	/**
 	 * Adds the graphical representation.
 	 * 
 	 * @param context
 	 *            the area context
 	 * @param newObject
 	 *            the new object
 	 */
 	protected void addGraphicalRepresentation(IAreaContext context, Object newObject) {
 		getFeatureProvider().addIfPossible(new AddContext(context, newObject));
 	}
 
 	/**
 	 * @return true if moving to negative coordinates should not be possible
 	 */
 	protected boolean avoidNegativeCoordinates() {
 		return false;
 	}
 
 	/**
 	 * Gets the image.
 	 * 
 	 * @param structureMapping
 	 *            the structure mapping
 	 * @param link
 	 *            the pictogram link
 	 * @return the image
 	 */
 	protected String getImage(IStructureMapping structureMapping, PictogramLink link) {
 		String ret = null;
 		IDataMapping dm = structureMapping.getDataMapping();
 		if (dm instanceof IImageDataMapping) {
 			ret = ((IImageDataMapping) dm).getImageId(link);
 		}
 		return ret;
 	}
 
 	/**
 	 * Gets the text.
 	 * 
 	 * @param structureMapping
 	 *            the structure mapping
 	 * @param link
 	 *            the pictogram link
 	 * @return the text
 	 */
 	protected String getText(IStructureMapping structureMapping, PictogramLink link) {
 		String ret = null;
 		IDataMapping dm = structureMapping.getDataMapping();
 		if (dm instanceof ITextDataMapping) {
 			ret = ((ITextDataMapping) dm).getText(link);
 		}
 		return ret;
 	}
 
 	/**
 	 * This method must be implemented by the pattern developer / provider.
 	 * 
 	 * @param pictogramElement
 	 *            the pictogram element
 	 * @return true, if is pattern controlled
 	 */
 	abstract protected boolean isPatternControlled(PictogramElement pictogramElement);
 
 	/**
 	 * This method must be implemented by the pattern developer / provider.
 	 * 
 	 * @param pictogramElement
 	 *            the pictogram element
 	 * @return true, if is pattern root
 	 */
 	abstract protected boolean isPatternRoot(PictogramElement pictogramElement);
 
 	/**
 	 * Layout pictogram element.
 	 * 
 	 * @param pe
 	 *            the pictogram element
 	 */
 	protected void layoutPictogramElement(PictogramElement pe) {
 		LayoutContext context = new LayoutContext(pe);
 		getFeatureProvider().layoutIfPossible(context);
 	}
 
 	/**
 	 * Update pictogram element.
 	 * 
 	 * @param pe
 	 *            the pictogram element
 	 */
 	protected void updatePictogramElement(PictogramElement pe) {
 		UpdateContext context = new UpdateContext(pe);
 		getFeatureProvider().updateIfPossible(context);
 		layoutPictogramElement(pe);
 	}
 
 	/**
 	 * Sets the pattern configuration.
 	 * 
 	 * @param patternConfiguration
 	 *            the new patternConfiguration
 	 */
 	protected void setPatternConfiguration(IPatternConfiguration patternConfiguration) {
 		this.patternConfiguration = patternConfiguration;
 	}
 
 	/**
 	 * Gets the pattern configuration.
 	 * 
 	 * @return the patternConfiguration
 	 */
 	protected IPatternConfiguration getPatternConfiguration() {
 		return patternConfiguration;
 	}
 
 	public void completeInfo(IDirectEditingInfo info, Object bo) {
 	}
 
 	public void completeInfo(IDirectEditingInfo info, Object bo, String keyProperty) {
 	}
 
 	public IResizeConfiguration getResizeConfiguration(IResizeShapeContext context) {
 		return new DefaultResizeConfiguration();
 	}
 }
