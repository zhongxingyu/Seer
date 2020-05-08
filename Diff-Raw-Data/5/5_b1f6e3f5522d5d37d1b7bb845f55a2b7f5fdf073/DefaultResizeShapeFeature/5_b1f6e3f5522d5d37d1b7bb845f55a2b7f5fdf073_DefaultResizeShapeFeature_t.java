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
  *    Patch 185019 from Bug 332360 contributed by Volker Wegert
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.features.impl;
 
 import org.eclipse.graphiti.features.DefaultResizeConfiguration;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.IResizeConfiguration;
 import org.eclipse.graphiti.features.IResizeShapeFeature;
 import org.eclipse.graphiti.features.context.IContext;
 import org.eclipse.graphiti.features.context.IResizeShapeContext;
 import org.eclipse.graphiti.features.context.impl.ResizeShapeContext;
 import org.eclipse.graphiti.internal.Messages;
import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.services.Graphiti;
 
 /**
  * The Class DefaultResizeShapeFeature.
  */
 public class DefaultResizeShapeFeature extends AbstractFeature implements IResizeShapeFeature {
 
 	/**
 	 * Creates a new {@link DefaultResizeShapeFeature}.
 	 * 
 	 * @param fp
 	 *            the feature provider
 	 */
 	public DefaultResizeShapeFeature(IFeatureProvider fp) {
 		super(fp);
 	}
 
 	public boolean canResizeShape(IResizeShapeContext context) {
		if (context.getPictogramElement() instanceof ConnectionDecorator) {
			return false;
		}
 		return true;
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
 
 	public boolean canExecute(IContext context) {
 		boolean ret = false;
 		if (context instanceof IResizeShapeContext) {
 			ret = canResizeShape((IResizeShapeContext) context);
 		}
 		return ret;
 	}
 
 	public void execute(IContext context) {
 		if (context instanceof IResizeShapeContext) {
 			resizeShape((IResizeShapeContext) context);
 		}
 	}
 
 	/**
 	 * Resize shape.
 	 * 
 	 * @param currentShape
 	 *            the current shape
 	 */
 	protected void resizeShape(Shape currentShape) {
 		final ResizeShapeContext resizeShapeContext = new ResizeShapeContext(currentShape);
 		final IResizeShapeFeature resizeShapeFeature = getFeatureProvider().getResizeShapeFeature(resizeShapeContext);
 		if (resizeShapeFeature != null) {
 			if (resizeShapeFeature.canResizeShape(resizeShapeContext)) {
 				resizeShapeFeature.resizeShape(resizeShapeContext);
 			}
 		}
 	}
 
 	@Override
 	public String getName() {
 		return NAME;
 	}
 
 	private static final String NAME = Messages.DefaultResizeShapeFeature_0_xfld;
 
 	public IResizeConfiguration getResizeConfiguration(IResizeShapeContext context) {
 		return new DefaultResizeConfiguration();
 	}
 }
