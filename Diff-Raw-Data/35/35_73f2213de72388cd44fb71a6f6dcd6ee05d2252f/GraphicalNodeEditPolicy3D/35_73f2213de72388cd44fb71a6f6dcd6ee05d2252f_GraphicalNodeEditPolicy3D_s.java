 /*******************************************************************************
  * Copyright (c) 2008 Jens von Pilgrim and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Jens von Pilgrim - initial API and implementation
  ******************************************************************************/
 package org.eclipse.gef3d.editpolicies;
 
 import org.eclipse.draw2d.Connection;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw3d.PolylineConnection3D;
 import org.eclipse.draw3d.RenderContext;
 import org.eclipse.gef.Request;
 import org.eclipse.gef.editpolicies.FeedbackHelper;
 import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
 import org.eclipse.gef.requests.CreateConnectionRequest;
 
 /**
  * GraphicalNodeEditPolicy3D, creates a 3D feedback connection line instead of a
  * simple 2D line.
  * 
  * @author Jens von Pilgrim
  * @version $Revision$
  * @since May 5, 2008
  */
 public abstract class GraphicalNodeEditPolicy3D extends GraphicalNodeEditPolicy {
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#createDummyConnection(org.eclipse.gef.Request)
 	 */
 	@Override
 	protected Connection createDummyConnection(Request req) {
 		return new PolylineConnection3D() {
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.draw3d.Polyline3D#render(org.eclipse.draw3d.RenderContext)
 			 */
 			@Override
 			public void render(RenderContext i_renderContext) {
 				// TODO remove this method, only for testing
 				super.render(i_renderContext);
 			}
 
 		};
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getFeedbackHelper(org.eclipse.gef.requests.CreateConnectionRequest)
 	 */
 	@Override
 	protected FeedbackHelper getFeedbackHelper(CreateConnectionRequest request) {
 		if (feedbackHelper == null) {
 			feedbackHelper = new FeedbackHelper3D();
 			Point p = request.getLocation();
 			connectionFeedback = createDummyConnection(request);
 			connectionFeedback
 				.setConnectionRouter(getDummyConnectionRouter(request));
 			connectionFeedback
 				.setSourceAnchor(getSourceConnectionAnchor(request));
 			feedbackHelper.setConnection(connectionFeedback);
 			addFeedback(connectionFeedback);
 			feedbackHelper.update(null, p);
 		}
 
		if (feedbackHelper instanceof FeedbackHelper3D)
			((FeedbackHelper3D) feedbackHelper).setHostFigure(getHostFigure());

 		return feedbackHelper;
 	}
 
 }
