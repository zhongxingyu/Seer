 /*
  * Copyright (c) 2006 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *    Dmitry Stadnik (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.examples.taipan.gmf.editor.edit.policies;
 
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.Label;
 import org.eclipse.draw2d.RectangleFigure;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.gef.LayerConstants;
 import org.eclipse.gef.editpolicies.SelectionEditPolicy;
 
 /**
  * @generated
  */
 public class TaiPanTextSelectionEditPolicy extends SelectionEditPolicy {
 
 	/**
 	 * @generated
 	 */
 	private IFigure selectionFeedbackFigure;
 
 	/**
 	 * @generated
 	 */
 	private IFigure focusFeedbackFigure;
 
 	/**
 	 * @generated
 	 */
 	protected void showPrimarySelection() {
 		showSelection();
 		showFocus();
 	}
 
 	/**
 	 * @generated
 	 */
 	protected void showSelection() {
 		hideSelection();
 		addFeedback(selectionFeedbackFigure = createSelectionFeedbackFigure());
 		refreshSelectionFeedback();
 		hideFocus();
 	}
 
 	/**
 	 * @generated
 	 */
 	protected void hideSelection() {
 		if (selectionFeedbackFigure != null) {
 			removeFeedback(selectionFeedbackFigure);
 			selectionFeedbackFigure = null;
 		}
 		hideFocus();
 	}
 
 	/**
 	 * @generated
 	 */
 	protected void showFocus() {
 		hideFocus();
 		addFeedback(focusFeedbackFigure = createFocusFeedbackFigure());
 		refreshFocusFeedback();
 	}
 
 	/**
 	 * @generated
 	 */
 	protected void hideFocus() {
 		if (focusFeedbackFigure != null) {
 			removeFeedback(focusFeedbackFigure);
 			focusFeedbackFigure = null;
 		}
 	}
 
 	/**
 	 * @generated
 	 */
 	protected IFigure getFeedbackLayer() {
 		return getLayer(LayerConstants.SCALED_FEEDBACK_LAYER);
 	}
 
 	/**
 	 * @generated
 	 */
 	protected Rectangle getFeedbackBounds() {
 		Rectangle bounds;
 		if (getHostFigure() instanceof Label) {
 			bounds = ((Label) getHostFigure()).getTextBounds();
 			bounds.intersect(getHostFigure().getBounds());
 		} else {
 			bounds = getHostFigure().getBounds().getCopy();
 		}
 		return bounds;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected IFigure createSelectionFeedbackFigure() {
 		if (getHostFigure() instanceof Label) {
 			Label feedbackFigure = new Label();
 			feedbackFigure.setOpaque(true);
 			feedbackFigure.setBackgroundColor(ColorConstants.menuBackgroundSelected);
 			feedbackFigure.setForegroundColor(ColorConstants.menuForegroundSelected);
 			return feedbackFigure;
 		} else {
 			RectangleFigure feedbackFigure = new RectangleFigure();
 			feedbackFigure.setFill(false);
 			return feedbackFigure;
 		}
 	}
 
 	/**
 	 * @generated
 	 */
 	protected IFigure createFocusFeedbackFigure() {
 		return new Figure() {
 
 			protected void paintFigure(Graphics graphics) {
 				graphics.drawFocus(getBounds().getResized(-1, -1));
 			}
 		};
 	}
 
 	/**
 	 * @generated
 	 */
 	protected void updateLabel(Label target) {
 		Label source = (Label) getHostFigure();
 		target.setText(source.getText());
 		target.setTextAlignment(source.getTextAlignment());
 		target.setFont(source.getFont());
 	}
 
 	/**
 	 * @generated
 	 */
 	protected void refreshSelectionFeedback() {
 		if (selectionFeedbackFigure != null) {
 			if (selectionFeedbackFigure instanceof Label) {
 				updateLabel((Label) selectionFeedbackFigure);
 				selectionFeedbackFigure.setBounds(getFeedbackBounds());
 			} else {
 				selectionFeedbackFigure.setBounds(getFeedbackBounds().expand(5, 5));
 			}
 		}
 	}
 
 	/**
 	 * @generated
 	 */
 	protected void refreshFocusFeedback() {
 		if (focusFeedbackFigure != null) {
 			focusFeedbackFigure.setBounds(getFeedbackBounds());
 		}
 	}
 
 	/**
 	 * @generated
 	 */
 	public void refreshFeedback() {
 		refreshSelectionFeedback();
 		refreshFocusFeedback();
 	}
 }
