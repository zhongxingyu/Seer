 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2012 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *    Benjamin Schmeling - mwenz - Bug 367483 - Support composite connections
  *    mgorning - Bug 376572 - Generic context buttons name changeable via getName() method
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.features.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.IRemoveFeature;
 import org.eclipse.graphiti.features.context.IContext;
 import org.eclipse.graphiti.features.context.IRemoveContext;
 import org.eclipse.graphiti.features.context.impl.RemoveContext;
 import org.eclipse.graphiti.internal.Messages;
 import org.eclipse.graphiti.internal.services.GraphitiInternal;
 import org.eclipse.graphiti.mm.pictograms.AdvancedAnchor;
 import org.eclipse.graphiti.mm.pictograms.Anchor;
 import org.eclipse.graphiti.mm.pictograms.CompositeConnection;
 import org.eclipse.graphiti.mm.pictograms.Connection;
 import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.services.Graphiti;
 
 /**
  * The Class DefaultRemoveFeature.
  */
 public class DefaultRemoveFeature extends AbstractFeature implements IRemoveFeature {
 
 	private static final String NAME = Messages.DefaultRemoveFeature_0_xfld;
 	private static final String DESC = Messages.ContextEntryHelper_3_xfld;
 
 	/**
 	 * Creates a new {@link DefaultRemoveFeature}.
 	 * 
 	 * @param fp
 	 *            the feature provider
 	 */
 	public DefaultRemoveFeature(IFeatureProvider fp) {
 		super(fp);
 	}
 
 	public boolean canRemove(IRemoveContext context) {
 		return !(context.getPictogramElement() instanceof Diagram);
 	}
 
	public final void remove(IRemoveContext context) {
 		if (!getUserDecision()) {
 			return;
 		}
 		preRemove(context);
 
 		PictogramElement pe = context.getPictogramElement();
 
 		if (pe instanceof Shape) {
 			Shape shape = (Shape) pe;
 			removeAllConnections(shape);
 		} else if (pe instanceof AdvancedAnchor) {
 			removeAllConnections((AdvancedAnchor) pe);
 		} else if (pe instanceof CompositeConnection) {
 			removeCompositeConnections((CompositeConnection) pe);
 		}
 
 		Graphiti.getPeService().deletePictogramElement(pe);
 
 		postRemove(context);
 	}
 
 	private void removeCompositeConnections(CompositeConnection composite) {
 		List<Connection> children = new ArrayList<Connection>(composite.getChildren());
 		for (Connection childConnection : children) {
 			RemoveContext context = new RemoveContext(childConnection);
 			remove(context);
 		}
 	}
 
 	public void preRemove(IRemoveContext context) {
 	}
 
 	/**
 	 * Removes the all connections.
 	 * 
 	 * @param shape
 	 *            the shape
 	 */
 	protected void removeAllConnections(Shape shape) {
 		List<Anchor> anchors = shape.getAnchors();
 		for (Anchor anchor : anchors) {
 			removeAllConnections(anchor);
 		}
 	}
 
 	/**
 	 * @since 0.9
 	 */
 	protected void removeAllConnections(Anchor anchor) {
 		IFeatureProvider featureProvider = getFeatureProvider();
 		List<Connection> allConnections = Graphiti.getPeService().getAllConnections(anchor);
 		for (Connection connection : allConnections) {
 			if (GraphitiInternal.getEmfService().isObjectAlive(connection)) {
 				IRemoveContext rc = new RemoveContext(connection);
 				IRemoveFeature removeFeature = featureProvider.getRemoveFeature(rc);
 				if (removeFeature != null) {
 					ConnectionDecorator decorators[] = connection.getConnectionDecorators().toArray(
 							new ConnectionDecorator[0]);
 					for (ConnectionDecorator decorator : decorators) {
 						if (decorator != null && GraphitiInternal.getEmfService().isObjectAlive(decorator)) {
 							EcoreUtil.delete(decorator, true);
 						}
 					}
 					removeFeature.remove(rc);
 				}
 			}
 		}
 	}
 
 	public void postRemove(IRemoveContext context) {
 	}
 
 	public boolean canExecute(IContext context) {
 		boolean ret = false;
 		if (context instanceof IRemoveContext) {
 			ret = canRemove((IRemoveContext) context);
 		}
 		return ret;
 	}
 
 	public void execute(IContext context) {
 		if (context instanceof IRemoveContext) {
 			remove((IRemoveContext) context);
 		}
 	}
 
 	@Override
 	public String getName() {
 		return NAME;
 	}
 
 	@Override
 	public String getDescription() {
 		return DESC;
 	}
 }
