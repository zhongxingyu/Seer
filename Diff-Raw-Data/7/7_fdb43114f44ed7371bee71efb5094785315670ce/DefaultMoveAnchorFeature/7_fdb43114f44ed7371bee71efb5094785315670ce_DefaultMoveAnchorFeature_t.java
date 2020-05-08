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
 /*
  * Created on 06.07.2005
  */
 package org.eclipse.graphiti.features.impl;
 
 import org.eclipse.graphiti.datatypes.IDimension;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.IMoveAnchorFeature;
 import org.eclipse.graphiti.features.context.IContext;
 import org.eclipse.graphiti.features.context.IMoveAnchorContext;
 import org.eclipse.graphiti.internal.Messages;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.AdvancedAnchor;
 import org.eclipse.graphiti.mm.pictograms.Anchor;
 import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
 import org.eclipse.graphiti.mm.pictograms.BoxRelativeAnchor;
 import org.eclipse.graphiti.mm.pictograms.FixPointAnchor;
 import org.eclipse.graphiti.services.Graphiti;
 
 /**
  * The Class DefaultMoveAnchorFeature.
  */
 public class DefaultMoveAnchorFeature extends AbstractFeature implements IMoveAnchorFeature {
 
 	/**
 	 * Creates a new {@link DefaultMoveAnchorFeature}.
 	 * 
 	 * @param fp
 	 *            the feature provider
 	 */
 	public DefaultMoveAnchorFeature(IFeatureProvider fp) {
 		super(fp);
 	}
 
 	public boolean canMoveAnchor(IMoveAnchorContext context) {
 		Anchor anchor = context.getAnchor();
 		return (anchor instanceof FixPointAnchor || anchor instanceof BoxRelativeAnchor);
 	}
 
 	public void moveAnchor(IMoveAnchorContext context) {
 		if (!getUserDecision()) {
 			return;
 		}
 		int posX = context.getX();
 		int posY = context.getY();
 
 		moveAnchor(context.getAnchor(), posX, posY);
 	}
 
 	public void preMoveAnchor(IMoveAnchorContext context) {
 	}
 
 	public void postMoveAnchor(IMoveAnchorContext context) {
 	}
 
 	/**
 	 * Move anchor.
 	 * 
 	 * @param anchor
 	 *            the anchor
 	 * @param posX
 	 *            the pos x
 	 * @param posY
 	 *            the pos y
 	 */
 	protected void moveAnchor(Anchor anchor, int posX, int posY) {
 
		if (anchor instanceof AdvancedAnchor) {
			posX -= anchor.getGraphicsAlgorithm().getX();
			posY -= anchor.getGraphicsAlgorithm().getY();
		}

 		if (anchor instanceof FixPointAnchor) {
 			FixPointAnchor fpAnchor = (FixPointAnchor) anchor;
 			fpAnchor.setLocation(Graphiti.getGaCreateService().createPoint(posX, posY));
 		} else if (anchor instanceof BoxRelativeAnchor) {
 			BoxRelativeAnchor brAnchor = (BoxRelativeAnchor) anchor;
 			AnchorContainer anchorContainer = brAnchor.getParent();
 			GraphicsAlgorithm anchorContainerGa = anchorContainer.getGraphicsAlgorithm();
 			IDimension sizeOfGraphicsAlgorithm = Graphiti.getGaService().calculateSize(anchorContainerGa, false);
 			int width = sizeOfGraphicsAlgorithm.getWidth();
 			double newRelativeX = 1d * posX / width;
 			brAnchor.setRelativeWidth(newRelativeX);
 			int height = sizeOfGraphicsAlgorithm.getHeight();
 			double newRelativeY = 1d * posY / height;
 			brAnchor.setRelativeHeight(newRelativeY);
 		}
 	}
 
 	public boolean canExecute(IContext context) {
 		boolean ret = false;
 		if (context instanceof IMoveAnchorContext) {
 			ret = canMoveAnchor((IMoveAnchorContext) context);
 		}
 		return ret;
 	}
 
 	public void execute(IContext context) {
 		if (context instanceof IMoveAnchorContext) {
 			moveAnchor((IMoveAnchorContext) context);
 		}
 	}
 
 	@Override
 	public String getName() {
 		return NAME;
 	}
 
 	private static final String NAME = Messages.DefaultMoveAnchorFeature_0_xfld;
 }
