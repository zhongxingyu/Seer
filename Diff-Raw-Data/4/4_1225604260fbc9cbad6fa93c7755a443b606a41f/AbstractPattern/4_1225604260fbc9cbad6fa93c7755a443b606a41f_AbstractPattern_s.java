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
  *    Volker Wegert - Bug 336828: patterns should support delete,
  *                    remove, direct editing and conditional palette
  *                    creation entry
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.pattern;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.graphiti.features.DefaultResizeConfiguration;
 import org.eclipse.graphiti.features.IDeleteFeature;
 import org.eclipse.graphiti.features.IDirectEditingInfo;
 import org.eclipse.graphiti.features.IReason;
 import org.eclipse.graphiti.features.IRemoveFeature;
 import org.eclipse.graphiti.features.IResizeConfiguration;
 import org.eclipse.graphiti.features.context.IAreaContext;
 import org.eclipse.graphiti.features.context.ICreateContext;
 import org.eclipse.graphiti.features.context.IDeleteContext;
 import org.eclipse.graphiti.features.context.IDirectEditingContext;
 import org.eclipse.graphiti.features.context.ILayoutContext;
 import org.eclipse.graphiti.features.context.IMoveShapeContext;
 import org.eclipse.graphiti.features.context.IRemoveContext;
 import org.eclipse.graphiti.features.context.IResizeShapeContext;
 import org.eclipse.graphiti.features.context.IUpdateContext;
 import org.eclipse.graphiti.features.context.impl.AddContext;
 import org.eclipse.graphiti.features.context.impl.LayoutContext;
 import org.eclipse.graphiti.features.context.impl.UpdateContext;
 import org.eclipse.graphiti.features.impl.DefaultRemoveFeature;
 import org.eclipse.graphiti.features.impl.Reason;
 import org.eclipse.graphiti.func.IDelete;
 import org.eclipse.graphiti.func.IDirectEditing;
 import org.eclipse.graphiti.func.IProposalSupport;
 import org.eclipse.graphiti.func.IRemove;
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
 import org.eclipse.graphiti.ui.features.DefaultDeleteFeature;
 
 /**
  * The Class AbstractPattern.
  */
 public abstract class AbstractPattern extends AbstractBasePattern implements IPattern {
 
 	/**
 	 * An empty string array used in direct editing.
 	 */
 	protected static final String[] EMPTY_STRING_ARRAY = new String[0];
 
 	private IPatternConfiguration patternConfiguration;
 
 	/**
 	 * To avoid code duplication, this base class uses a wrapped default
 	 * implementation of an {@link IDeleteFeature} to provide the default
 	 * deletion behaviour. Subclasses may decide to either override
 	 * {@link #createDeleteFeature(IDeleteContext)} to provide another
 	 * {@link IDeleteFeature} implementation or override and extend the
 	 * individual {@link IDelete} methods or return a {@link IDeleteFeature} by
 	 * overriding the method
 	 * {@link DefaultFeatureProviderWithPatterns#getDeleteFeature(IDeleteContext)}
 	 * .
 	 */
 	private IDeleteFeature wrappedDeleteFeature;
 
 	/**
 	 * To avoid code duplication, this base class uses a wrapped default
 	 * implementation of an {@link IRemoveFeature} to provide the default
 	 * removal behavior. Subclasses may decide to either override
 	 * {@link #createRemoveFeature(IRemoveContext)} to provide another
 	 * {@link IRemoveFeature} implementation or override and extend the
 	 * individual {@link IRemove} methods or return a {@link IRemoveFeature} by
 	 * overriding the method
 	 * {@link DefaultFeatureProviderWithPatterns#getRemoveFeature(IRemoveContext)}
 	 * .
 	 */
 	private IRemoveFeature wrappedRemoveFeature;
 
 	/**
 	 * Creates a new {@link AbstractPattern}.
 	 * 
 	 * @param patternConfiguration
 	 *            the pattern configuration
 	 */
 	public AbstractPattern(IPatternConfiguration patternConfiguration) {
 		setPatternConfiguration(patternConfiguration);
 	}
 
 	@Override
 	public boolean isPaletteApplicable() {
 		return true;
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
 
 	/**
 	 * Creates the {@link IDeleteFeature} instance that handles the deletion of
 	 * business objects and diagram elements. The default implementation just
 	 * creates a {@link DefaultDeleteFeature}. Concrete pattern implementations
 	 * may either override this method to provide their own subclass of
 	 * {@link DefaultDeleteFeature} or override and extend the individual
 	 * methods provided by {@link IDelete}.
 	 * 
 	 * @param context
 	 *            the deletion context
 	 * @return the {@link IDeleteFeature} instance to use for this pattern
 	 * @see #canDelete(IDeleteContext)
 	 * @see #preDelete(IDeleteContext)
 	 * @see #delete(IDeleteContext)
 	 * @see #postDelete(IDeleteContext)
 	 */
 	protected IDeleteFeature createDeleteFeature(IDeleteContext context) {
 		return new DefaultDeleteFeature(getFeatureProvider());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.func.IDelete#canDelete(org.eclipse.graphiti.features
 	 * .context.IDeleteContext)
 	 */
 	public boolean canDelete(IDeleteContext context) {
 		if (wrappedDeleteFeature == null) {
 			wrappedDeleteFeature = createDeleteFeature(context);
 		}
 		return ((wrappedDeleteFeature != null) && wrappedDeleteFeature.canDelete(context));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.func.IDelete#preDelete(org.eclipse.graphiti.features
 	 * .context.IDeleteContext)
 	 */
 	public void preDelete(IDeleteContext context) {
 		if (wrappedDeleteFeature == null) {
 			wrappedDeleteFeature = createDeleteFeature(context);
 		}
 		if (wrappedDeleteFeature != null) {
 			wrappedDeleteFeature.preDelete(context);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.func.IDelete#delete(org.eclipse.graphiti.features
 	 * .context.IDeleteContext)
 	 */
 	public void delete(IDeleteContext context) {
 		if (wrappedDeleteFeature == null) {
 			wrappedDeleteFeature = createDeleteFeature(context);
 		}
 		if (wrappedDeleteFeature != null) {
 			wrappedDeleteFeature.delete(context);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.func.IDelete#postDelete(org.eclipse.graphiti.features
 	 * .context.IDeleteContext)
 	 */
 	public void postDelete(IDeleteContext context) {
 		if (wrappedDeleteFeature == null) {
 			wrappedDeleteFeature = createDeleteFeature(context);
 		}
 		if (wrappedDeleteFeature != null) {
 			wrappedDeleteFeature.postDelete(context);
 		}
 	}
 
 	/**
 	 * Creates the {@link IRemoveFeature} instance that handles the removal of
 	 * diagram elements. The default implementation just creates a
 	 * {@link DefaultRemoveFeature}. Concrete pattern implementations may either
 	 * override this method to provide their own subclass of
 	 * {@link DefaultRemoveFeature} or override and extend the individual
 	 * methods provided by {@link IRemove}.
 	 * 
 	 * @param context
 	 *            the removal context
 	 * @return the {@link IRemoveFeature} instance to use for this pattern
 	 * @see #canRemove(IRemoveContext)
 	 * @see #preRemove(IRemoveContext)
 	 * @see #Remove(IRemoveContext)
 	 * @see #postRemove(IRemoveContext)
 	 */
 	protected IRemoveFeature createRemoveFeature(IRemoveContext context) {
 		return new DefaultRemoveFeature(getFeatureProvider());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.func.IRemove#canRemove(org.eclipse.graphiti.features
 	 * .context.IRemoveContext)
 	 */
 	public boolean canRemove(IRemoveContext context) {
 		if (wrappedRemoveFeature == null) {
 			wrappedRemoveFeature = createRemoveFeature(context);
 		}
 		return wrappedRemoveFeature.canRemove(context);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.func.IRemove#preRemove(org.eclipse.graphiti.features
 	 * .context.IRemoveContext)
 	 */
 	public void preRemove(IRemoveContext context) {
 		if (wrappedRemoveFeature == null) {
 			wrappedRemoveFeature = createRemoveFeature(context);
 		}
 		wrappedRemoveFeature.preRemove(context);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.func.IRemove#remove(org.eclipse.graphiti.features
 	 * .context.IRemoveContext)
 	 */
 	public void remove(IRemoveContext context) {
 		if (wrappedRemoveFeature == null) {
 			wrappedRemoveFeature = createRemoveFeature(context);
 		}
 		wrappedRemoveFeature.remove(context);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.func.IRemove#postRemove(org.eclipse.graphiti.features
 	 * .context.IRemoveContext)
 	 */
 	public void postRemove(IRemoveContext context) {
 		if (wrappedRemoveFeature == null) {
 			wrappedRemoveFeature = createRemoveFeature(context);
 		}
 		wrappedRemoveFeature.postRemove(context);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.func.IDirectEditing#canDirectEdit(org.eclipse.graphiti
 	 * .features.context.IDirectEditingContext)
 	 */
 	@Override
 	public boolean canDirectEdit(IDirectEditingContext context) {
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.func.IDirectEditing#checkValueValid(java.lang.String
 	 * , org.eclipse.graphiti.features.context.IDirectEditingContext)
 	 */
 	@Override
 	public String checkValueValid(String value, IDirectEditingContext context) {
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.func.IDirectEditing#completeValue(java.lang.String,
 	 * int, java.lang.String,
 	 * org.eclipse.graphiti.features.context.IDirectEditingContext)
 	 */
 	@Override
 	public String completeValue(String value, int caretPos, String choosenValue, IDirectEditingContext context) {
 		return choosenValue;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.func.IDirectEditing#getPossibleValues(org.eclipse
 	 * .graphiti.features.context.IDirectEditingContext)
 	 */
 	@Override
 	public String[] getPossibleValues(IDirectEditingContext context) {
 		return EMPTY_STRING_ARRAY;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.func.IDirectEditing#getValueProposals(java.lang.
 	 * String, int, org.eclipse.graphiti.features.context.IDirectEditingContext)
 	 */
 	@Override
 	public String[] getValueProposals(String value, int caretPos, IDirectEditingContext context) {
 		return EMPTY_STRING_ARRAY;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.graphiti.func.IDirectEditing#isAutoCompletionEnabled()
 	 */
 	@Override
 	public boolean isAutoCompletionEnabled() {
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.graphiti.func.IDirectEditing#isCompletionAvailable()
 	 */
 	@Override
 	public boolean isCompletionAvailable() {
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.graphiti.func.IDirectEditing#stretchFieldToFitText()
 	 */
 	@Override
 	public boolean stretchFieldToFitText() {
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.graphiti.func.IDirectEditing#getEditingType()
 	 */
 	public int getEditingType() {
 		return IDirectEditing.TYPE_NONE;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.func.IDirectEditing#getInitialValue(org.eclipse.
 	 * graphiti.features.context.IDirectEditingContext)
 	 */
 	public String getInitialValue(IDirectEditingContext context) {
		throw new UnsupportedOperationException("Subclasses must override this method if they want to provide direct editing."); //$NON-NLS-1$ 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.graphiti.func.IDirectEditing#setValue(java.lang.String,
 	 * org.eclipse.graphiti.features.context.IDirectEditingContext)
 	 */
 	public void setValue(String value, IDirectEditingContext context) {
		throw new UnsupportedOperationException("Subclasses must override this method if they want to provide direct editing."); //$NON-NLS-1$ 
 	}
 
 	@Override
 	public IProposalSupport getProposalSupport() {
 		return null;
 	}
 }
