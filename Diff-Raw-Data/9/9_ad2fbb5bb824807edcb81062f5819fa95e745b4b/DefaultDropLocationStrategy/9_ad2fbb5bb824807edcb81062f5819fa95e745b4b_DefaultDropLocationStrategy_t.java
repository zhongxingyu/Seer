 /*******************************************************************************
  * Copyright (c) 2001, 2007 Oracle Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Oracle Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.pagedesigner.viewer;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.Label;
 import org.eclipse.draw2d.LineBorder;
 import org.eclipse.draw2d.RectangleFigure;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.draw2d.geometry.Insets;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.EditPartViewer;
 import org.eclipse.gef.requests.DropRequest;
 import org.eclipse.jst.pagedesigner.parts.ElementEditPart;
 import org.eclipse.jst.pagedesigner.validation.caret.IPositionMediator;
 
 /**
  * A default implementation of the drop location strategy
  * @author cbateman
  *
  */
 public class DefaultDropLocationStrategy extends AbstractDropLocationStrategy 
 {
     // the amount of vertical offset below the mouse pointer to place
     // the upper left of the drop hint tooltip
     private static final int DROP_HINT_VERTICAL_OFFSET = 20;
 
     /**
      * @param viewer
      */
     public DefaultDropLocationStrategy(EditPartViewer viewer) {
         super(viewer);
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.jst.pagedesigner.viewer.AbstractDropLocationStrategy#calculateDesignPosition(org.eclipse.gef.EditPart, org.eclipse.draw2d.geometry.Point, org.eclipse.jst.pagedesigner.validation.caret.IPositionMediator)
      */
     @Override
     public DesignPosition calculateDesignPosition(EditPart host, Point p,
             IPositionMediator validator) {
         return EditPartPositionHelper.findEditPartPosition(
                 host, p, validator);
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.jst.pagedesigner.viewer.AbstractDropLocationStrategy#showTargetFeedback(org.eclipse.gef.EditPart, org.eclipse.jst.pagedesigner.viewer.DesignPosition, org.eclipse.gef.requests.DropRequest)
      */
     @Override
     public List showTargetFeedback(EditPart host, DesignPosition position, DropRequest request) 
     {
         List feedback = new ArrayList(4);
         feedback.add(showFeedbackRect(createCaretBounds(position)));
         feedback.add(showDropHintLabel(request.getLocation(), position));
         
         return feedback;
     }
 
     
     
     /**
      * @param rect
      * @return the default rectangle figure for the requested visual rectangle
      */
     protected final RectangleFigure showFeedbackRect(Rectangle rect) {
         RectangleFigure pf = createFeedbackFigure();
         pf.translateToRelative(rect);
         pf.setBounds(rect);
         return pf;
     }
     
     /**
      * @param position
      * @return the bounding rectangle for the caret at the current
      * position in absolute coords
      */
     protected Rectangle createCaretBounds(DesignPosition position)
     {
         Rectangle rect = EditPartPositionHelper
             .convertToAbsoluteCaretRect(position);
 
         // to avoid enlarge feedback pane.
         rect = rect.intersect(getFeedbackLayer().getBounds());
         
         return rect;
     }
     
     /**
      * @return the newly created feedback figure
      */
     protected RectangleFigure createFeedbackFigure() 
     {
         RectangleFigure feedbackFigure = new RectangleFigure();
         feedbackFigure.setFill(true);
         feedbackFigure.setOutline(true);
         feedbackFigure.setLineWidth(1);
         feedbackFigure.setForegroundColor(ColorConstants.red);
         feedbackFigure.setBounds(new Rectangle(0, 0, 0, 0));
         feedbackFigure.setXOR(true);
         addFeedback(feedbackFigure);
         return feedbackFigure;
     }
 
     /**
      * Shows a label in a position relative to the drop marker
      * that hints where the new component will be dropped in
      * respect of components already there
      * @param mousePosition 
      * @param position 
      * @return the drop hint label
      */
     protected final Label showDropHintLabel(Point mousePosition, DesignPosition position)
     {
         Label    dropHintLabel = new Label();
         dropHintLabel.setOpaque(true);
         dropHintLabel.setBackgroundColor(ColorConstants.tooltipBackground);
         dropHintLabel.setBorder(
                 new LineBorder(ColorConstants.black, 1)
                 {
                     // add an extra pixel of inset to make sure the text
                     // isn't pressed against the border
                     public Insets getInsets(IFigure figure) {
                         return new Insets(getWidth()+1);
                     }
                 }
         );
         addFeedback(dropHintLabel);
 
         final String hintText = getDropHintText(position);
         dropHintLabel.setText(hintText);
         //TODO: need to handle viewport clipping and adjust label location appropriately
         Dimension hintSize = dropHintLabel.getPreferredSize();
         Point hintLocation = new Point(mousePosition.x, mousePosition.y+DROP_HINT_VERTICAL_OFFSET);
         Rectangle hintRect = new Rectangle(hintLocation, hintSize);
 
        //Bug 303524 - [WPE] design view flickers on dnd of jsf html column
        //    (translateToRelative BEFORE intersect, so intersection happens on final display bounds)
        dropHintLabel.translateToRelative(hintRect);
         // we need to intersect the rectangle with the feedback pane, otherwise, when the mouse
         // is dragged near the edge of the viewport with the drop hint active, the canvas will expand
         // away from the mouse.  In future a more ideal solution will be to relocate the tooltip
         // so that is is completely inside the viewport.
         hintRect = hintRect.intersect(getFeedbackLayer().getBounds());

         dropHintLabel.setBounds(hintRect);
         
         return dropHintLabel;
     }
     
     /**
      * @param position
      * @return the drop hint text for the current position
      */
     protected String getDropHintText(DesignPosition position)
     {
         StringBuffer buffer = new StringBuffer("Place"); //$NON-NLS-1$
         
         EditPart prevPart = position.getSiblingEditPart(false);
         EditPart nextPart = position.getSiblingEditPart(true);
 
         if (nextPart instanceof ElementEditPart)
         {
             buffer.append(" before "); //$NON-NLS-1$
             buffer.append(((ElementEditPart)nextPart).getTagConvert().getHostElement().getNodeName());
             buffer.append(","); //$NON-NLS-1$
         }
         
         if (prevPart instanceof ElementEditPart)
         {
             buffer.append(" after "); //$NON-NLS-1$
             buffer.append(((ElementEditPart)prevPart).getTagConvert().getHostElement().getNodeName());
             buffer.append(","); //$NON-NLS-1$
         }
         
         buffer.append(" inside "); //$NON-NLS-1$
         buffer.append(position.getContainerNode().getNodeName());
         
         return buffer.toString();
     }
 }
