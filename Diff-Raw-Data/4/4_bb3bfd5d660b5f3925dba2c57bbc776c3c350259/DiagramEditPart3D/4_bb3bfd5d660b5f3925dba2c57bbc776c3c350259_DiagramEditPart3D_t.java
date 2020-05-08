 /*******************************************************************************
  * Copyright (c) 2009 Jens von Pilgrim and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Kristian Duske - initial API and implementation
  ******************************************************************************/
 package org.eclipse.gef3d.examples.ecore.diagram.parts;
 
 import java.util.List;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.draw2d.FigureListener;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.draw3d.Figure3D;
 import org.eclipse.draw3d.IFigure3D;
 import org.eclipse.draw3d.geometry.Vector3fImpl;
 import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecoretools.diagram.edit.parts.EPackageEditPart;
 import org.eclipse.emf.ecoretools.diagram.edit.policies.EPackageCanonicalEditPolicy;
 import org.eclipse.emf.ecoretools.diagram.edit.policies.EcoretoolsEditPolicyRoles;
 import org.eclipse.emf.ecoretools.diagram.part.EcoreVisualIDRegistry;
 import org.eclipse.gef.DragTracker;
 import org.eclipse.gef.Request;
 import org.eclipse.gef.requests.SelectionRequest;
 import org.eclipse.gef.tools.DeselectAllTracker;
 import org.eclipse.gef3d.gmf.runtime.diagram.ui.figures.DiagramFigure3D;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.internal.tools.RubberbandDragTracker;
 import org.eclipse.gmf.runtime.diagram.ui.tools.DragEditPartsTrackerEx;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.widgets.Display;
 
 /**
  * 3D plane for displaying ecore diagrams in 3D, 3D version of
  * {@link DiagramEditPart}. Plane is automatically resized to fit its content.
  * <p>
  * This edit part keeps model and diagram partially synchronized, i.e. nodes are
  * created if new classes, enumerations or data types are created.
  * </p>
  * 
  * @todo sync feature should be configurable via properties or preferences
  *       (however, this is an Ecore Tools problem)
  * @see http://www.eclipse.org/forums/index.php?t=msg&th=168993&start=0&
  * @author Kristian Duske
  * @author Jens von Pilgrim
  * @version $Revision$
  * @since 02.09.2009
  */
public class DiagramEditPart3D extends EPackageEditPart { // DiagramEditPart {
 
 	/**
 	 * Creates a new edit part for the given view.
 	 * 
 	 * @param i_diagramView the view
 	 */
 	public DiagramEditPart3D(View i_diagramView) {
 		super(i_diagramView);
 	}
 
 	/**
 	 * {@inheritDoc} Replaces original {@link EPackageCanonicalEditPolicy} with
 	 * syncing policy, i.e. views are created for newly added semantic nodes.
 	 * 
 	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart#createDefaultEditPolicies()
 	 */
 	@Override
 	protected void createDefaultEditPolicies() {
 		super.createDefaultEditPolicies();
 		installEditPolicy(EcoretoolsEditPolicyRoles.PSEUDO_CANONICAL_ROLE,
 			new EPackageCanonicalEditPolicy() {
 
 				/**
 				 * {@inheritDoc}
 				 * 
 				 * @see org.eclipse.emf.ecoretools.diagram.edit.policies.EPackageCanonicalEditPolicy#refreshSemantic()
 				 */
 				@Override
 				protected void refreshSemantic() {
 					// delete orphans and update connections
 					super.refreshSemantic();
 					// create views for newly added children
 					List<IAdaptable> createdViews = refreshSemanticChildren();
 					makeViewsImmutable(createdViews);
 				}
 
 				/**
 				 * {@inheritDoc}
 				 * 
 				 * @see <a
 				 *      href="http://www.eclipse.org/forums/index.php?t=msg&&th=162860&goto=515348#msg_515348">Sven
 				 *      Krause: Re: Synchronize semantic and notation
 				 *      information on editor start</a>
 				 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.CanonicalEditPolicy#getFactoryHint(org.eclipse.core.runtime.IAdaptable)
 				 */
 				@Override
 				protected String getFactoryHint(IAdaptable elementAdapter) {
 					CanonicalElementAdapter element =
 						(CanonicalElementAdapter) elementAdapter;
 					int visualID =
 						EcoreVisualIDRegistry.getNodeVisualID((View) getHost()
 							.getModel(), (EObject) element.getRealObject());
 					return EcoreVisualIDRegistry.getType(visualID);
 				}
 
 			});
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart#createFigure()
 	 */
 	@Override
 	protected IFigure createFigure() {
 		IFigure3D f = new DiagramFigure3D() {
 			@Override
 			public void add(IFigure i_figure, Object i_constraint, int i_index) {
 				super.add(i_figure, i_constraint, i_index);
 				i_figure.addFigureListener(new FigureListener() {
 
 					public void figureMoved(IFigure i_source) {
 						autoResize();
 
 					}
 				});
 			}
 		};
 		// Figure3D f = new ClassDiagramFigure3DEmbedded();
 
 		f.getPosition3D().setLocation3D(new Vector3fImpl(0, 0, 0));
 		f.getPosition3D().setSize3D(new Vector3fImpl(400, 400, 30));
 
 		f.setBackgroundColor(new Color(Display.getCurrent(), 255, 255, 255));
 		f.setAlpha((byte) (255 / 2));
 
 		return f;
 	}
 
 	/**
 	 * Replaces the {@link RubberbandDragTracker} with an old school
 	 * {@link DragEditPartsTrackerEx}, since the former performs a cast which
 	 * fails.
 	 * 
 	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart#getDragTracker(org.eclipse.gef.Request)
 	 */
 	@Override
 	public DragTracker getDragTracker(Request req) {
 		if (req instanceof SelectionRequest
 			&& ((SelectionRequest) req).getLastButtonPressed() == 3)
 			return new DeselectAllTracker(this);
 		return new DragEditPartsTrackerEx(this);
 	}
 
 	/**
 	 * 
 	 */
 	private void autoResize() {
 		int maxX = 400;
 		int maxY = 400;
 		Rectangle rect = getChildrenBounds();
 		int border = 30;
 		int depth = 20;
 
 		rect.width += border;
 		rect.height += border;
 
 		if (maxX < rect.width)
 			maxX = rect.width;
 		if (maxY < rect.height)
 			maxY = rect.height;
 		((Figure3D) getFigure()).getPosition3D().setSize3D(
 			new Vector3fImpl(maxX + border, maxY + border, depth));
 	}
 }
