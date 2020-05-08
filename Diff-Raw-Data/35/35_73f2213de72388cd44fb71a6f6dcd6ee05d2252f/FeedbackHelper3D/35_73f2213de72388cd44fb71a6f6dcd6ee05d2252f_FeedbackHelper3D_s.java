 /*******************************************************************************
  * Copyright (c) 2009 Jens von Pilgrim and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Jens von Pilgrim - initial API and implementation
  ******************************************************************************/
 package org.eclipse.gef3d.editpolicies;
 
 import java.util.logging.Logger;
 
 import org.eclipse.draw2d.ConnectionAnchor;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.UpdateManager;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw3d.IFigure3D;
 import org.eclipse.draw3d.ISurface;
 import org.eclipse.draw3d.PickingUpdateManager3D;
 import org.eclipse.draw3d.XYZAnchor;
 import org.eclipse.draw3d.geometry.BoundingBox;
 import org.eclipse.draw3d.geometry.IBoundingBox;
 import org.eclipse.draw3d.geometry.Vector3f;
 import org.eclipse.draw3d.geometry.Vector3fImpl;
 import org.eclipse.draw3d.picking.Picker;
 import org.eclipse.draw3d.util.Draw3DCache;
 import org.eclipse.gef.editpolicies.FeedbackHelper;
 
 /**
  * FeedbackHelper3D There should really be more documentation here.
  * 
  * @author Jens von Pilgrim
  * @version $Revision$
  * @since May 31, 2009
  */
 public class FeedbackHelper3D extends FeedbackHelper {
 
 	@SuppressWarnings("unused")
 	private static final Logger log =
 		Logger.getLogger(FeedbackHelper3D.class.getName());
 
 	/**
 	 * A dummy anchor.
 	 */
 	protected XYZAnchor m_dummyAnchor;
 
 	/**
 	 * The 3D host figure.
 	 */
 	protected IFigure m_hostFigure;
 
 	/**
 	 * The picker, which can be <code>null</code>.
 	 */
 	protected Picker m_picker;
 
 	/**
 	 * Creates a new feedback helper.
 	 */
 	public FeedbackHelper3D() {
 
 		m_dummyAnchor = createDummyAnchor();
 	}
 
 	/**
 	 * Creates a dummy anchor.
 	 * 
 	 * @return a dummy anchor
 	 */
 	protected XYZAnchor createDummyAnchor() {
 
 		return new XYZAnchor(new Vector3fImpl(10, 10, 10));
 	}
 
 	/**
 	 * Sets the bounds of the given feedback figure to the given values,
 	 * expanded by <code>0.01f</code>. The given values are in relation to the
 	 * current surface.
 	 * 
 	 * @param i_feedback the feedback figure to modify
 	 * @param i_sLocation the absolute location in surface coordinates
 	 * @param i_sSize the absolute size in surface coordinates
 	 */
 	public void setAbsoluteFeedbackBounds(IFigure3D i_feedback,
 		Point i_sLocation, Dimension i_sSize) {
 
 		if (i_feedback == null)
 			throw new NullPointerException("i_feedback must not be null");
 
 		BoundingBox bounds = Draw3DCache.getBoundingBox();
 		Vector3f wLocation = Draw3DCache.getVector3f();
 		Vector3f wSize = Draw3DCache.getVector3f();
 		try {
 			ISurface surface = m_picker.getCurrentSurface();
 
 			if (i_sLocation != null) {
 				surface.getWorldLocation(i_sLocation, wLocation);
 				bounds.setLocation(wLocation);
 			} else
 				bounds.setLocation(0, 0, 0);
 
 			if (i_sSize != null) {
 				// surface.getWorldDimension(i_sSize, wSize);
 				bounds.setSize(i_sSize.width, i_sSize.height, 1);
 			} else {
 				bounds.setSize(0, 0, 1);
 			}
			
 			bounds.expand(0.01f);
 			bounds.getPosition(wLocation);
 			bounds.getSize(wSize);
 
 			i_feedback.getPosition3D().setLocation3D(wLocation);
 			i_feedback.getPosition3D().setSize3D(wSize);
 		} finally {
 			Draw3DCache.returnBoundingBox(bounds);
 			Draw3DCache.returnVector3f(wLocation, wSize);
 		}
 	}
 
 	/**
 	 * Moves the given feedback figure by the given move delta and resizes it by
 	 * the given size delta. The given deltas are in relation to the current
 	 * surface.
 	 * 
 	 * @param i_feedback the feedback figure
 	 * @param i_surfaceMoveDelta the move delta
 	 * @param i_surfaceSizeDelta the size delta
 	 */
 	public void setDeltaFeedbackBounds(IFigure3D i_feedback,
 		Point i_surfaceMoveDelta, Dimension i_surfaceSizeDelta) {
 
 		if (i_feedback == null)
 			throw new NullPointerException("i_feedback must not be null");
 
 		BoundingBox bounds = Draw3DCache.getBoundingBox();
 		Vector3f wLocation = Draw3DCache.getVector3f();
 		Vector3f wSize = Draw3DCache.getVector3f();
 		try {
 			bounds.set(i_feedback.getBounds3D());
 			ISurface surface = m_picker.getCurrentSurface();
 
 			if (i_surfaceMoveDelta != null) {
 				// surface.getWorldLocation(i_surfaceMoveDelta, wLocation);
 				wLocation.set(i_surfaceMoveDelta.x, i_surfaceMoveDelta.y, 0);
 				bounds.translate(wLocation);
 			}
 
 			if (i_surfaceSizeDelta != null) {
//				surface.getWorldDimension(i_surfaceSizeDelta, wSize);
//				bounds.resize(wSize);
				bounds.resize(i_surfaceSizeDelta.width,i_surfaceSizeDelta.height,0);
 			}
 
 			bounds.getPosition(wLocation);
 			bounds.getSize(wSize);
 
 			i_feedback.getPosition3D().setLocation3D(wLocation);
 			i_feedback.getPosition3D().setSize3D(wSize);
 		} finally {
 			Draw3DCache.returnBoundingBox(bounds);
 			Draw3DCache.returnVector3f(wLocation, wSize);
 		}
 	}
 
 	/**
 	 * Sets the host figure of this feedback helper.
 	 * 
 	 * @param i_hostFigure the host figure
 	 */
 	public void setHostFigure(IFigure i_hostFigure) {
 
 		m_hostFigure = i_hostFigure;
 
 		UpdateManager updateManager = m_hostFigure.getUpdateManager();
 		if (updateManager instanceof PickingUpdateManager3D)
 			m_picker = ((PickingUpdateManager3D) updateManager).getPicker();
 	}
 
 	/**
 	 * Sets the bounds of the given feedback figure to the bounds of the host
 	 * figure, expanded by <code>0.01f</code>.
 	 * 
 	 * @param i_feedback the feedback figure to modify
 	 */
 	public void setInitialFeedbackBounds(IFigure3D i_feedback) {
 
 		if (m_hostFigure instanceof IFigure3D) {
 			BoundingBox feedbackBounds = Draw3DCache.getBoundingBox();
 			Vector3f wLocation = Draw3DCache.getVector3f();
 			Vector3f wSize = Draw3DCache.getVector3f();
 			try {
 				IFigure3D hostFigure3D = (IFigure3D) m_hostFigure;
 				IBoundingBox hostBounds = hostFigure3D.getBounds3D();
 
 				feedbackBounds.set(hostBounds);
 				feedbackBounds.expand(0.01f);
 
 				feedbackBounds.getPosition(wLocation);
 				feedbackBounds.getSize(wSize);
 
 				i_feedback.getPosition3D().setLocation3D(wLocation);
 				i_feedback.getPosition3D().setSize3D(wSize);
 			} finally {
 				Draw3DCache.returnBoundingBox(feedbackBounds);
 				Draw3DCache.returnVector3f(wLocation, wSize);
 			}
 		} else {
 			Point sLocation = m_hostFigure.getBounds().getLocation();
 			Dimension sSize = m_hostFigure.getBounds().getSize();
 
 			setAbsoluteFeedbackBounds(i_feedback, sLocation, sSize);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * This method is a duplicate of the original one, using the newly defined
 	 * anchor here.
 	 * 
 	 * @see org.eclipse.gef.editpolicies.FeedbackHelper#update(org.eclipse.draw2d.ConnectionAnchor,
 	 *      org.eclipse.draw2d.geometry.Point)
 	 */
 	@Override
 	public void update(ConnectionAnchor anchor, Point p) {
 
 		if (anchor != null)
 			setAnchor(anchor);
 		else {
 			ISurface surface = m_picker.getCurrentSurface();
 
 			Vector3f w = Draw3DCache.getVector3f();
 			try {
 				surface.getWorldLocation(p, w);
 				m_dummyAnchor.setLocation3D(w);
 				setAnchor(m_dummyAnchor);
 			} finally {
 				Draw3DCache.returnVector3f(w);
 			}
 		}
 	}
 
 }
