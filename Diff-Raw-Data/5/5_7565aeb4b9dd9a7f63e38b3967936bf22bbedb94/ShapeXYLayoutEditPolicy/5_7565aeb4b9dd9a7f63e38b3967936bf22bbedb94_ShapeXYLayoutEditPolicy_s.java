 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2011 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *    jpasch - BUG 341180: Graphiti fails to handle resize after custom feature addition in the tutorial
  *    mwenz - Bug 346067: Current Milestone Build does no longer build against Eclipse 3.6
  *    mwenz - Bug 355027: Move of connection decorators when zoom level != 100 behaves weird
  *    mgorning - Bug 342262 - enhanced resize behavior for container shapes
  *    Bug 336488 - DiagramEditor API
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.internal.policy;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.draw2d.PositionConstants;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.EditPolicy;
 import org.eclipse.gef.GraphicalEditPart;
 import org.eclipse.gef.GraphicalViewer;
 import org.eclipse.gef.Request;
 import org.eclipse.gef.SnapToGrid;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gef.commands.UnexecutableCommand;
 import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
 import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
 import org.eclipse.gef.requests.ChangeBoundsRequest;
 import org.eclipse.gef.requests.CreateRequest;
 import org.eclipse.graphiti.datatypes.IDimension;
 import org.eclipse.graphiti.features.ICreateFeature;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.IMoveAnchorFeature;
 import org.eclipse.graphiti.features.IMoveConnectionDecoratorFeature;
 import org.eclipse.graphiti.features.IMoveShapeFeature;
 import org.eclipse.graphiti.features.IResizeShapeFeature;
 import org.eclipse.graphiti.features.context.ICreateContext;
 import org.eclipse.graphiti.features.context.IMoveAnchorContext;
 import org.eclipse.graphiti.features.context.IMoveConnectionDecoratorContext;
 import org.eclipse.graphiti.features.context.IMoveShapeContext;
 import org.eclipse.graphiti.features.context.IResizeShapeContext;
 import org.eclipse.graphiti.features.context.impl.AreaAnchorContext;
 import org.eclipse.graphiti.features.context.impl.CreateContext;
 import org.eclipse.graphiti.features.context.impl.MoveConnectionDecoratorContext;
 import org.eclipse.graphiti.features.context.impl.MoveShapeContext;
 import org.eclipse.graphiti.features.context.impl.ResizeShapeContext;
 import org.eclipse.graphiti.internal.command.CommandContainer;
 import org.eclipse.graphiti.internal.command.GenericFeatureCommandWithContext;
 import org.eclipse.graphiti.internal.command.ICommand;
 import org.eclipse.graphiti.internal.command.MoveShapeFeatureCommandWithContext;
 import org.eclipse.graphiti.internal.command.ResizeShapeFeatureCommandWithContext;
 import org.eclipse.graphiti.internal.services.GraphitiInternal;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.pictograms.Anchor;
 import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
 import org.eclipse.graphiti.mm.pictograms.Connection;
 import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.ui.editor.DiagramEditor;
 import org.eclipse.graphiti.ui.internal.command.AddModelObjectCommand;
 import org.eclipse.graphiti.ui.internal.command.CreateModelObjectCommand;
 import org.eclipse.graphiti.ui.internal.command.GefCommandWrapper;
 import org.eclipse.graphiti.ui.internal.command.MoveAnchorFeatureCommandWithContext;
 import org.eclipse.graphiti.ui.internal.config.IConfigurationProvider;
 import org.eclipse.graphiti.ui.internal.contextbuttons.IContextButtonManager;
 import org.eclipse.graphiti.ui.internal.parts.ShapeEditPart;
 import org.eclipse.graphiti.ui.internal.services.GraphitiUiInternal;
 import org.eclipse.jface.viewers.ISelection;
 
 /**
  * An EditPolicy, where the Layout of the EditParts is important: they must have
  * an XYLayout. It assumes, that this EditPart is a parent, whose children can
  * be added/deleted/moved.
  * 
  * @see org.eclipse.graphiti.ui.internal.policy.IEditPolicyFactory#createShapeXYLayoutEditPolicy()
  * @noinstantiate This class is not intended to be instantiated by clients.
  * @noextend This class is not intended to be subclassed by clients.
  */
 public class ShapeXYLayoutEditPolicy extends XYLayoutEditPolicy {
 
 	private IConfigurationProvider _configurationProvider;
 
 	/**
 	 * Creates a new ShapeXYLayoutEditPolicy.
 	 * 
 	 * @param configurationProvider
 	 *            The IConfigurationProvider.
 	 */
 	protected ShapeXYLayoutEditPolicy(IConfigurationProvider configurationProvider) {
 		_configurationProvider = configurationProvider;
 	}
 
 	protected final IConfigurationProvider getConfigurationProvider() {
 		return _configurationProvider;
 	}
 
 	/**
 	 * Is called, when a child EditPart shall be moved from another
 	 * parent-EditPart into this parent-EditPart. It creates an
 	 * ICommandCombiner.createSetParentReferenceCommand().
 	 * 
 	 * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#createAddCommand(org.eclipse.gef.EditPart,
 	 *      java.lang.Object)
 	 */
 	@Override
 	protected Command createAddCommand(EditPart child, Object constraint) {
 
 		Object model = child.getModel();
 		Rectangle rectangle = getHostFigure().getBounds();
 
 		if (model instanceof ConnectionDecorator && constraint instanceof Rectangle && rectangle != null) {
 			ICommand cmd = getMoveConnectionDecoratorCommand((ConnectionDecorator) model, (Rectangle) constraint,
 					rectangle.x, rectangle.y);
 			if (cmd != null) {
 				IConfigurationProvider configurationProvider = getConfigurationProvider();
 				return new GefCommandWrapper(cmd, configurationProvider.getDiagramEditor().getEditingDomain());
 			}
 		}
 		return null;
 	}
 
 	@Override
 	protected EditPolicy createChildEditPolicy(EditPart child) {
 		if (!(child instanceof ShapeEditPart)) {
 			return new NonResizableEditPolicy();
 		}
 		PictogramElement pictogramElement = ((ShapeEditPart) child).getPictogramElement();
 		Shape shape = (Shape) pictogramElement;
 		ResizeShapeContext resizeShapeContext = new ResizeShapeContext(shape);
 		return new GFResizableEditPolicy(getConfigurationProvider(), resizeShapeContext);
 	}
 
 	/**
 	 * Is called, when a child EditPart shall be moved inside this
 	 * parent-EditPart (resized or changed XY-position). It creates an
 	 * ICommandFactory.createChangeModelObjectConstraintCommand(().
 	 * 
 	 * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#createChangeConstraintCommand(org.eclipse.gef.EditPart,
 	 *      java.lang.Object)
 	 */
 	@Override
 	protected Command createChangeConstraintCommand(ChangeBoundsRequest request, EditPart child, Object constraint) {
 
 		IConfigurationProvider configurationProvider = getConfigurationProvider();
 		IFeatureProvider featureProvider = configurationProvider.getDiagramTypeProvider().getFeatureProvider();
 		CommandContainer ret = new CommandContainer(featureProvider);
 
 		Object model = child.getModel();
 
 		if (!(model instanceof EObject) || GraphitiInternal.getEmfService().isObjectAlive((EObject) model)) {
 
 			// connection decorators
 			if (model instanceof ConnectionDecorator) {
 				if (constraint instanceof Rectangle) {
 					ICommand cmd = getMoveConnectionDecoratorCommand((ConnectionDecorator) model,
 							(Rectangle) constraint, 0, 0);
 					if (cmd != null) {
 						ret.add(cmd);
 					}
 				}
 			} else
 			// anchors
 			if (model instanceof Anchor) {
 				Anchor anchor = (Anchor) model;
 				AnchorContainer anchorContainer = anchor.getParent();
 
 				IMoveAnchorContext context = createLayoutAnchorContext(anchor, anchorContainer, anchorContainer,
 						constraint);
 
 				IMoveAnchorFeature moveAnchorFeature = featureProvider.getMoveAnchorFeature(context);
 				if (moveAnchorFeature != null) {
 					if (child instanceof GraphicalEditPart) {
 						ret.add(new MoveAnchorFeatureCommandWithContext(moveAnchorFeature, context,
 								(GraphicalEditPart) child));
 					}
 				}
 			} else
 			// shapes
 			if (model instanceof Shape) {
 				Shape shape = (Shape) model;
 				ContainerShape containerShape = shape.getContainer();
 
 				if (constraint instanceof Rectangle) {
 					Rectangle rectangle = (Rectangle) constraint;
 
 					{
 						IMoveShapeContext context = createMoveShapeContext(shape, containerShape, containerShape,
 								constraint);
 
 						IMoveShapeFeature moveShapeFeature = featureProvider.getMoveShapeFeature(context);
 						if (moveShapeFeature != null) {
 							if (child instanceof ShapeEditPart) {
 								// Check if size has changed. If yes we do a
 								// resize and no move. In this case do
 								// not add a move feature to the command because
 								// Move might not be allowed while
 								// Resize is allowed. Adding both Move and
 								// Resize leads to Resizing not possible.
 								if (!isDifferentSize(shape, rectangle)) {
 									// Not in resize
 									ret.add(new MoveShapeFeatureCommandWithContext(moveShapeFeature, context));
 								}
 							}
 						}
 					}
 
 					{
 						if (isDifferentSize(shape, rectangle)) {
 							IResizeShapeContext context = createResizeShapeContext(shape, constraint,
 									request.getResizeDirection());
 
 							IResizeShapeFeature resizeShapeFeature = featureProvider.getResizeShapeFeature(context);
 							if (resizeShapeFeature != null) {
 								ret.add(new ResizeShapeFeatureCommandWithContext(resizeShapeFeature, context));
 								// } else if (child instanceof ShapeEditPart) {
 								// ret.add(new
 								// ResizeShapeFeatureCommandWithContext(resizeShapeFeature,
 								// context));
 							}
 
 						}
 
 					}
 				}
 			}
 		}
 
 		if (ret.containsCommands()) {
 			// hide context-buttons, if the user resizes/moves the shape
 			IContextButtonManager contextButtonManager = getConfigurationProvider().getContextButtonManager();
 			contextButtonManager.hideContextButtonsInstantly();
 
 			DiagramEditor editor = getConfigurationProvider().getDiagramEditor();
 			return new GefCommandWrapper(ret, editor.getEditingDomain());
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * @param constraint
 	 * @param coll
 	 * @param container
 	 * @param container2
 	 * @return
 	 */
 	protected IMoveShapeContext createMoveShapeContext(Shape shape, ContainerShape source, ContainerShape target,
 			Object constraint) {
 		MoveShapeContext ret = new MoveShapeContext(shape);
 
 		ret.setSourceContainer(source);
 		ret.setTargetContainer(target);
 
 		Point loc = null;
 		if (constraint instanceof Rectangle) {
 			Rectangle rect = (Rectangle) constraint;
 			loc = rect.getLocation();
 		} else if (constraint instanceof Point) {
 			loc = (Point) constraint;
 		}
 		if (loc != null) {
 			ret.setX(loc.x);
 			ret.setY(loc.y);
 
 			// calculate and store deltas
 			if (shape != null) {
 				GraphicsAlgorithm graphicsAlgorithm = shape.getGraphicsAlgorithm();
 				if (graphicsAlgorithm != null) {
 					ret.setDeltaX(loc.x - graphicsAlgorithm.getX());
 					ret.setDeltaY(loc.y - graphicsAlgorithm.getY());
 				}
 			}
 		}
 
 		return ret;
 	}
 
 	protected IResizeShapeContext createResizeShapeContext(Shape shape, Object constraint, int resizeDirection) {
 		ResizeShapeContext ret = new ResizeShapeContext(shape);
 
 		Point loc = null;
 		Dimension dim = null;
 		if (constraint instanceof Rectangle) {
 			Rectangle rect = (Rectangle) constraint;
 			dim = rect.getSize();
 			loc = rect.getLocation();
 		} else if (constraint instanceof Dimension) {
 			dim = (Dimension) constraint;
 		}
 		if (dim != null) {
 			ret.setWidth(dim.width);
 			ret.setHeight(dim.height);
 		}
 		if (loc != null) {
 			ret.setX(loc.x);
 			ret.setY(loc.y);
 		}
 
 		int direction = 0;
 		switch (resizeDirection) {
 		case PositionConstants.NORTH:
 			direction = IResizeShapeContext.DIRECTION_NORTH;
 			break;
 		case PositionConstants.SOUTH:
 			direction = IResizeShapeContext.DIRECTION_SOUTH;
 			break;
 		case PositionConstants.WEST:
 			direction = IResizeShapeContext.DIRECTION_WEST;
 			break;
 		case PositionConstants.EAST:
 			direction = IResizeShapeContext.DIRECTION_EAST;
 			break;
 		case PositionConstants.NORTH_WEST:
 			direction = IResizeShapeContext.DIRECTION_NORTH_WEST;
 			break;
 		case PositionConstants.NORTH_EAST:
 			direction = IResizeShapeContext.DIRECTION_NORTH_EAST;
 			break;
 		case PositionConstants.SOUTH_WEST:
 			direction = IResizeShapeContext.DIRECTION_SOUTH_WEST;
 			break;
 		case PositionConstants.SOUTH_EAST:
 			direction = IResizeShapeContext.DIRECTION_SOUTH_EAST;
 			break;
 		}
 		ret.setDirection(direction);
 
 		return ret;
 	}
 
 	protected IMoveAnchorContext createLayoutAnchorContext(Anchor shape, AnchorContainer source,
 			AnchorContainer target, Object constraint) {
 		AreaAnchorContext ret = new AreaAnchorContext(shape);
 
 		ret.setSourceContainer(source);
 		ret.setTargetContainer(target);
 
 		if (constraint instanceof Rectangle) {
 			Rectangle rect = (Rectangle) constraint;
 			ret.setX(rect.x);
 			ret.setY(rect.y);
 			ret.setWidth(rect.width);
 			ret.setHeight(rect.height);
 		}
 
 		return ret;
 	}
 
 	/**
 	 * @param constraint
 	 * @param coll
 	 * @param container
 	 * @param container2
 	 * @return
 	 */
 	public static ICreateContext createCreateContext(ContainerShape target, Rectangle rect) {
 		CreateContext ret = new CreateContext();
 
 		ret.setTargetContainer(target);
 
 		ret.setX(rect.x);
 		ret.setY(rect.y);
 		ret.setWidth(rect.width);
 		ret.setHeight(rect.height);
 
 		return ret;
 	}
 
 	/**
 	 * Is called, when a new child EditPart shall be created inside this
 	 * parent-EditPart (with the CreationTool). It creates an
 	 * ICommandCombiner.createCreateModelObjectCommand().
 	 * 
 	 * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#getCreateCommand(org.eclipse.gef.requests.CreateRequest)
 	 */
 	@Override
 	protected Command getCreateCommand(CreateRequest request) {
 		Command cmd = UnexecutableCommand.INSTANCE;
 
 		// check if _target is valid
 		Object parentObject = getHost().getModel();
 		if (!(parentObject instanceof ContainerShape))
 			return cmd;
 		Object createdObject = request.getNewObject();
 
 		// determine constraint
 		Rectangle rectangle = null;
 		if (request.getLocation() != null) {
 			rectangle = (Rectangle) getConstraintFor(request);
 		}
 
 		if (request.getNewObjectType() == ICreateFeature.class) {
 			ICreateContext context = createCreateContext((ContainerShape) parentObject, rectangle);
 			updateCreateContext((CreateContext) context);
 			ICreateFeature createFeature = (ICreateFeature) createdObject;
 			cmd = new CreateModelObjectCommand(getConfigurationProvider(), createFeature, context, rectangle);
 			cmd.setLabel(createFeature.getDescription());
 		} else if (request.getNewObjectType() == ISelection.class) {
 			cmd = new AddModelObjectCommand(getConfigurationProvider(), (ContainerShape) parentObject,
 					(ISelection) createdObject, rectangle);
 		}
 
 		return cmd;
 	}
 
 	/**
 	 * Usage unknown, returns null.
 	 * 
 	 * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#getDeleteDependantCommand(org.eclipse.gef.Request)
 	 */
 	@Override
 	protected Command getDeleteDependantCommand(Request request) {
 		return null;
 	}
 
 	private ICommand getMoveConnectionDecoratorCommand(ConnectionDecorator decorator, Rectangle constraint,
 			int offsetX, int offsetY) {
 
 		ICommand ret = null;
 
 		int x = constraint.x + offsetX;
 		int y = constraint.y + offsetY;
 
 		Connection connection = decorator.getConnection();
 		double location = decorator.getLocation();
 
 		if (decorator.isLocationRelative()) {
 			Point connectionMidpoint = GraphitiUiInternal.getGefService().getConnectionPointAt(connection, location);
 			x = x - connectionMidpoint.x;
 			y = y - connectionMidpoint.y;
 		} else {
 			// absoluteLocation
 			Point absolutePointOnConnection = GraphitiUiInternal.getGefService().getAbsolutePointOnConnection(
 					connection, location);
 			x = x - absolutePointOnConnection.x;
 			y = y - absolutePointOnConnection.y;
 		}
 
 		/**
 		 * allow move of connection decorator only, if both connection ends are
 		 * not in the selection of moved objects
 		 */
 		boolean isExecuteAllowed = true;
 		PictogramElement[] selectedPictogramElements = getConfigurationProvider().getDiagramEditor()
 				.getSelectedPictogramElements();
 		List<PictogramElement> pes = Arrays.asList(selectedPictogramElements);
 		if (pes.size() > 1) {
 			PictogramElement startAnchorContainer = Graphiti.getPeService().getActiveContainerPe(
 					decorator.getConnection().getStart());
 			PictogramElement endAnchorContainer = Graphiti.getPeService().getActiveContainerPe(
 					decorator.getConnection().getEnd());
 			if (pes.contains(startAnchorContainer) || pes.contains(endAnchorContainer)) {
 				isExecuteAllowed = false;
 			}
 		}
 
 		IMoveConnectionDecoratorContext context = new MoveConnectionDecoratorContext(decorator, x, y, isExecuteAllowed);
 		IMoveConnectionDecoratorFeature feature = getFeatureProvider().getMoveConnectionDecoratorFeature(context);
 		if (feature != null) {
 			ret = new GenericFeatureCommandWithContext(feature, context);
 		}
 
 		return ret;
 	}
 
 	private IFeatureProvider getFeatureProvider() {
 		return getConfigurationProvider().getFeatureProvider();
 	}
 
 	/**
 	 * Checks if the given shape and the given rectangle are different in size
 	 * 
 	 * @param shape
 	 * @param constraint
 	 * @return
 	 */
 	private boolean isDifferentSize(Shape shape, Rectangle constraint) {
 		Rectangle rect = constraint;
 		IDimension sizeOfGA = Graphiti.getGaService().calculateSize(shape.getGraphicsAlgorithm(), false);
 		return rect.width != sizeOfGA.getWidth() || rect.height != sizeOfGA.getHeight();
 
 	}
 
 	private void updateCreateContext(CreateContext ctx) {
 		GraphicalViewer viewer = getConfigurationProvider().getDiagramEditor().getGraphicalViewer();
 		boolean gridVisible = (Boolean) viewer.getProperty(SnapToGrid.PROPERTY_GRID_VISIBLE);
 		boolean snapToGrid = (Boolean) viewer.getProperty(SnapToGrid.PROPERTY_GRID_ENABLED);
 		if (gridVisible && snapToGrid) {
 			Dimension dimension = (Dimension) viewer.getProperty(SnapToGrid.PROPERTY_GRID_SPACING);
 			int snappedX = getSnapValue(ctx.getX(), dimension.width);
 			int snappedY = getSnapValue(ctx.getY(), dimension.height);
 			ctx.setX(snappedX);
 			ctx.setY(snappedY);
 		}
 	}
 
 	private int getSnapValue(int currentValue, int gridUnit) {
 
 		int units = currentValue / gridUnit;
 		if ((currentValue % gridUnit) > (gridUnit / 2)) {
 			units++;
 		}
 		return gridUnit * units;
 	}
 }
