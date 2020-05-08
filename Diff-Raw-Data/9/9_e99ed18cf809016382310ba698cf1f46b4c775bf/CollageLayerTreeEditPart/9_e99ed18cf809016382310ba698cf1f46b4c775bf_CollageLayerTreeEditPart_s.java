 /*******************************************************************************
  * Copyright (c) 2004, 2005 Elias Volanakis and others.
  * Copyright (c) 2011, 2012 Alex Bradley.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Elias Volanakis - initial API and implementation
  *    Alex Bradley    - adaptation for Collage layers tree
  *******************************************************************************/
 package org.eclipselabs.collage.parts.tree;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.List;
 
 import org.eclipse.gef.DragTracker;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.EditPolicy;
 import org.eclipse.gef.Request;
 import org.eclipse.gef.RequestConstants;
 import org.eclipse.gef.requests.SelectionRequest;
 import org.eclipse.gef.tools.DragTreeItemsTracker;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipselabs.collage.CollageActivator;
 import org.eclipselabs.collage.model.ModelElement;
 import org.eclipselabs.collage.model.ResourceShapeList;
 import org.eclipselabs.collage.model.CollageLayer;
 import org.eclipselabs.collage.model.commands.SetActiveLayerCommand;
 import org.eclipselabs.collage.parts.LayerComponentEditPolicy;
 import org.eclipselabs.collage.util.CollageUtilities;
 
 /**
  * Tree edit part for a Collage layer. Can be double-clicked (to set its layer as the active layer) and dragged
  * (to move its layer up and down in the layer stack). Accepts direct edit requests to allow renaming of
  * the layer.
  * @author Alex Bradley
  * @author Elias Volanakis
  */
 public class CollageLayerTreeEditPart extends ColourFontTreeEditPart implements PropertyChangeListener {
 	private boolean directEditEnabled = true;
 	
 	/**
 	 * Upon activation, attach to the model element as a property change
 	 * listener.
 	 */
 	@Override
 	public void activate() {
 		if (!isActive()) {
 			super.activate();
 			((ModelElement) getModel()).addPropertyChangeListener(this);
 		}
 	}
 
 	@Override
 	protected void createEditPolicies() {
 		installEditPolicy(EditPolicy.COMPONENT_ROLE,
 					new LayerComponentEditPolicy());
 		installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE,
 				new CollageTreeEditPolicy());
 		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE,
 				new CollageLayerDirectEditPolicy());
 	}
 
 	/**
 	 * Upon deactivation, detach from the model element as a property change
 	 * listener.
 	 */
 	@Override
 	public void deactivate() {
 		if (isActive()) {
 			super.deactivate();
 			((ModelElement) getModel()).removePropertyChangeListener(this);
 		}
 	}
 
 	private CollageLayer getCastedModel() {
 		return (CollageLayer) getModel();
 	}
 
 	@Override
 	protected Image getImage() {
		return getCastedModel().isVisible() ? CollageActivator.getImage(CollageActivator.LAYER_VISIBLE_ICON) : null;
 	}
 
 	@Override
 	protected String getText() {
 		return getCastedModel().getName();
 	}
 
 	@Override
 	protected Font getFont() {
 		if (CollageActivator.getDefault().getDefaultCollageRoot().getCurrentLayer() == getCastedModel()) {
 			return CollageUtilities.getFontRegistry().getBoldSystemFont();
 		}
 		return null;
 	}
 
 	/**
 	 * Convenience method that returns the EditPart corresponding to a given
 	 * child.
 	 * 
 	 * @param child
 	 *            a model element instance
 	 * @return the corresponding EditPart or null
 	 */
 	private EditPart getEditPartForChild(Object child) {
 		return (EditPart) getViewer().getEditPartRegistry().get(child);
 	}
 
 	@Override
 	protected List<ResourceShapeList> getModelChildren() {
 		return getCastedModel().getPopulatedShapeLists(); // a list of shapes
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent evt) {
 		String prop = evt.getPropertyName();
 		Object newValue = evt.getNewValue();
 		if (CollageLayer.CHILD_ADDED_PROP.equals(prop)) {
 			// add a child to this edit part
 			// causes an additional entry to appear in the tree of the outline
 			// view
 			if (newValue != null && newValue instanceof ResourceShapeList && ((ResourceShapeList)newValue).hasChildren()) {
 				addChild(createChild(newValue), -1);
 			}
 		} else if (CollageLayer.CHILD_REMOVED_PROP.equals(prop)) {
 			// remove a child from this edit part
 			// causes the corresponding edit part to disappear from the tree in
 			// the outline view
 			if (newValue != null) {
 				EditPart editPartForChild = getEditPartForChild(newValue);
 				if (editPartForChild != null) {
 					removeChild(editPartForChild);
 				}
 			} else {
 				refreshChildren();
 			}
 		} else if (CollageLayer.CHILD_POPULATED_STATE_CHANGED_PROP.equals(prop)) {
 			if (newValue != null && newValue instanceof ResourceShapeList) {
 				if (((ResourceShapeList)newValue).hasChildren()) {
 					addChild(createChild(newValue), -1);
 				} else {
 					EditPart editPartForChild = getEditPartForChild(newValue);
 					if (editPartForChild != null) {
 						removeChild(editPartForChild);
 					}
 				}
 			}
 		} else {
 			refreshVisuals();
 		}
 	}
 
 	private void performDirectEdit() {
 		if (directEditEnabled) {
 			new LayerNameEditManager(this, new LayerNameCellEditorLocator((TreeItem)getWidget())).show();
 		}
 	}
 
 	@Override
 	public void performRequest(Request req) {
 		// Double-click sets active layer
 		if (req instanceof SelectionRequest && RequestConstants.REQ_OPEN.equals(req.getType())) {
 			getViewer().getEditDomain().getCommandStack().execute(new SetActiveLayerCommand(getCastedModel()));
 		} else if (RequestConstants.REQ_DIRECT_EDIT.equals(req.getType())) {
 			performDirectEdit();
 		}
 		super.performRequest(req);
 	}
 	
 	public void setDirectEditEnabled (boolean enabled) {
 		this.directEditEnabled = enabled;
 	}
 
 	@Override
 	public DragTracker getDragTracker(Request req) {
 		return new DragTreeItemsTracker(this);
 	}
 }
