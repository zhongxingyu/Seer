 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.rc.rcp.e3.gef.implclasses;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.beanutils.PropertyUtils;
 import org.apache.commons.lang.Validate;
 import org.eclipse.draw2d.Connection;
 import org.eclipse.draw2d.ConnectionAnchor;
 import org.eclipse.draw2d.FigureCanvas;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.gef.ConnectionEditPart;
 import org.eclipse.gef.EditDomain;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.GraphicalEditPart;
 import org.eclipse.gef.GraphicalViewer;
 import org.eclipse.gef.RootEditPart;
 import org.eclipse.gef.palette.PaletteEntry;
 import org.eclipse.gef.ui.palette.PaletteViewer;
 import org.eclipse.jubula.rc.common.driver.ClickOptions;
 import org.eclipse.jubula.rc.common.driver.IRobot;
 import org.eclipse.jubula.rc.common.driver.IRunnable;
 import org.eclipse.jubula.rc.common.exception.StepExecutionException;
import org.eclipse.jubula.rc.common.implclasses.MatchUtil;
import org.eclipse.jubula.rc.common.implclasses.Verifier;
 import org.eclipse.jubula.rc.rcp.e3.gef.factory.DefaultEditPartAdapterFactory;
 import org.eclipse.jubula.rc.rcp.e3.gef.identifier.IEditPartIdentifier;
 import org.eclipse.jubula.rc.rcp.e3.gef.listener.GefPartListener;
 import org.eclipse.jubula.rc.swt.driver.DragAndDropHelperSwt;
 import org.eclipse.jubula.rc.swt.implclasses.AbstractControlImplClass;
 import org.eclipse.jubula.rc.swt.implclasses.MenuUtil;
 import org.eclipse.jubula.tools.objects.event.EventFactory;
 import org.eclipse.jubula.tools.objects.event.TestErrorEvent;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 
 
 /**
  * Implementation class for Figure Canvas (Eclipse GEF).
  *
  * @author BREDEX GmbH
  * @created May 13, 2009
  */
 public class FigureCanvasImplClass extends AbstractControlImplClass {
 
     /**
      * the viewer that contains the EditParts corresponding to the FigureCanvas
      */
     private GraphicalViewer m_viewer = null;
 
     /** the composite in which the IFigures are displayed */
     private Composite m_composite = null;
 
     /**
      * {@inheritDoc}
      */
     public Control getComponent() {
         return m_composite;
     }
 
     /**
      *
      * @return the viewer associated with the canvas to test.
      */
     private GraphicalViewer getViewer() {
         return m_viewer;
     }
 
     /**
      *
      * @return the control for the canvas to test.
      */
     private Control getViewerControl() {
         return getViewer().getControl();
     }
 
     /**
      *
      * @return the root edit part of the viewer.
      */
     private RootEditPart getRootEditPart() {
         return getViewer().getRootEditPart();
     }
 
     /**
      *
      * @return the root of the palette viewer (tool palette).
      */
     private RootEditPart getPaletteRoot() {
         return getViewer().getEditDomain().getPaletteViewer().getRootEditPart();
     }
 
     /**
      *
      * @param textPath The path to the tool.
      * @param operator The operator used for matching.
      * @return the EditPart found at the end of the given path. Returns
      *         <code>null</code> if no EditPart can be found for the given path
      *         or if the EditPart found is not a GraphicalEditPart.
      */
     private GraphicalEditPart findPaletteEditPart(
             String textPath, String operator) {
 
         final String[] pathItems = MenuUtil.splitPath(textPath);
         boolean isExisting = true;
 
         EditPart currentEditPart = getPaletteRoot().getContents();
 
         for (int i = 0; i < pathItems.length && currentEditPart != null; i++) {
             List effectiveChildren = currentEditPart.getChildren();
 
             EditPart [] children =
                 (EditPart [])effectiveChildren.toArray(
                         new EditPart[effectiveChildren.size()]);
             boolean itemFound = false;
             for (int j = 0; j < children.length && !itemFound; j++) {
                 Object model = children[j].getModel();
                 if (model instanceof PaletteEntry) {
                     String entryLabel = ((PaletteEntry)model).getLabel();
                     if (entryLabel != null
                         && MatchUtil.getInstance().match(
                             entryLabel, pathItems[i], operator)) {
                         itemFound = true;
                         currentEditPart = children[j];
                     }
                 }
             }
             if (!itemFound) {
                 isExisting = false;
                 break;
             }
 
         }
 
         return isExisting && currentEditPart instanceof GraphicalEditPart
             ? (GraphicalEditPart)currentEditPart : null;
 
     }
 
     /**
      * {@inheritDoc}
      */
     public String[] getTextArrayFromComponent() {
         return null;
     }
 
     /**
      * {@inheritDoc}
      */
     public void setComponent(final Object graphicsComponent) {
         getEventThreadQueuer().invokeAndWait(getClass().getName() + ".setComponent", new IRunnable() { //$NON-NLS-1$
 
             public Object run() throws StepExecutionException {
                 FigureCanvas figureCanvas = (FigureCanvas)graphicsComponent;
                 Composite parent = figureCanvas;
                 while (parent != null
                         && !(parent.getData(
                                 GefPartListener.TEST_GEF_VIEWER_DATA_KEY)
                                         instanceof GraphicalViewer)) {
                     parent = parent.getParent();
                 }
 
                 if (parent != null) {
                     m_composite = parent;
                     m_viewer =
                         (GraphicalViewer)parent.getData(
                                 GefPartListener.TEST_GEF_VIEWER_DATA_KEY);
                 }
                 return null;
             }
 
         });
     }
 
     /**
      * Checks whether the figure for the EditPart for the given path exists and
      * is visible.
      *
      * @param textPath The path to the figure.
      * @param operator The operator used for matching.
      * @param exists   Whether the figure is expected to exist.
      */
     public void gdCheckFigureExists(
             String textPath, String operator, boolean exists) {
 
         boolean isExisting =
             findFigure(findEditPart(textPath, operator)) != null;
         if (!isExisting) {
             // See if there's a connection anchor at the given path
             isExisting = findConnectionAnchor(textPath, operator) != null;
         }
 
         Verifier.equals(exists, isExisting);
 
     }
 
     /**
      * Checks the given property of the figure at the given path.
      *
      * @param textPath The path to the figure.
      * @param textPathOperator The operator used for matching the text path.
      * @param propertyName The name of the property
      * @param expectedPropValue The value of the property as a string
      * @param valueOperator The operator used to verify
      */
     public void gdVerifyFigureProperty(String textPath,
             String textPathOperator, final String propertyName,
             String expectedPropValue, String valueOperator) {
 
         final IFigure figure =
             findFigure(findEditPart(textPath, textPathOperator));
         if (figure == null) {
             throw new StepExecutionException(
                     "No figure could be found for the given text path.", //$NON-NLS-1$
                     EventFactory.createActionError(
                             TestErrorEvent.NOT_FOUND));
         }
         Object prop = getEventThreadQueuer().invokeAndWait("getProperty",  //$NON-NLS-1$
             new IRunnable() {
 
                 public Object run() throws StepExecutionException {
                     try {
                         return PropertyUtils.getProperty(figure, propertyName);
                     } catch (IllegalAccessException e) {
                         throw new StepExecutionException(
                             e.getMessage(),
                             EventFactory.createActionError(
                                 TestErrorEvent.PROPERTY_NOT_ACCESSABLE));
                     } catch (InvocationTargetException e) {
                         throw new StepExecutionException(
                             e.getMessage(),
                             EventFactory.createActionError(
                                 TestErrorEvent.PROPERTY_NOT_ACCESSABLE));
                     } catch (NoSuchMethodException e) {
                         throw new StepExecutionException(
                             e.getMessage(),
                             EventFactory.createActionError(
                                 TestErrorEvent.PROPERTY_NOT_ACCESSABLE));
                     }
                 }
 
             });
         final String propToStr = String.valueOf(prop);
         Verifier.match(propToStr, expectedPropValue, valueOperator);
 
     }
 
     /**
      * Checks whether the tool for the given path exists and
      * is visible.
      *
      * @param textPath The path to the figure.
      * @param operator The operator used for matching.
      * @param exists   Whether the figure is expected to exist.
      */
     public void gdCheckToolExists(
             String textPath, String operator, boolean exists) {
 
         boolean isExisting = findPaletteFigure(textPath, operator) != null;
 
         Verifier.equals(exists, isExisting);
 
     }
 
     /**
      * Finds and clicks the figure for the given path.
      *
      * @param textPath The path to the figure.
      * @param operator The operator used for matching.
      * @param count The number of times to click.
      * @param button The mouse button to use for the click.
      */
     public void gdClickFigure(String textPath, String operator,
             int count, int button) {
 
         getRobot().click(getViewerControl(),
                 getFigureBoundsChecked(textPath, operator),
                 ClickOptions.create().setScrollToVisible(false)
                     .setClickCount(count).setMouseButton(button));
     }
 
     /**
      * Finds and clicks on a connection between a source figure and a
      * target figure.
      *
      * @param sourceTextPath The path to the source figure.
      * @param sourceOperator The operator to use for matching the source
      *                       figure path.
      * @param targetTextPath The path to the target figure.
      * @param targetOperator The operator to use for matching the target
      *                       figure path.
      * @param count The number of times to click.
      * @param button The mouse button to use for the click.
      */
     public void gdClickConnection(String sourceTextPath, String sourceOperator,
             String targetTextPath, String targetOperator,
             int count, int button) {
 
         GraphicalEditPart sourceEditPart =
             findEditPart(sourceTextPath, sourceOperator);
         GraphicalEditPart targetEditPart =
             findEditPart(targetTextPath, targetOperator);
 
         ConnectionEditPart connectionEditPart = null;
 
         if (sourceEditPart != null) {
             List sourceConnectionList = sourceEditPart.getSourceConnections();
             ConnectionEditPart [] sourceConnections =
                 (ConnectionEditPart [])sourceConnectionList.toArray(
                         new ConnectionEditPart[sourceConnectionList.size()]);
             for (int i = 0; i < sourceConnections.length
                     && connectionEditPart == null; i++) {
                 if (sourceConnections[i].getTarget() == targetEditPart) {
                     connectionEditPart = sourceConnections[i];
                 }
             }
         } else if (targetEditPart != null) {
             List targetConnectionList = targetEditPart.getTargetConnections();
             ConnectionEditPart [] targetConnections =
                 (ConnectionEditPart [])targetConnectionList.toArray(
                         new ConnectionEditPart[targetConnectionList.size()]);
             for (int i = 0; i < targetConnections.length
                     && connectionEditPart == null; i++) {
                 if (targetConnections[i].getSource() == targetEditPart) {
                     connectionEditPart = targetConnections[i];
                 }
             }
         } else {
             throw new StepExecutionException(
                     "No figures could be found for the given text paths.", //$NON-NLS-1$
                     EventFactory.createActionError(TestErrorEvent.NOT_FOUND));
         }
 
         IFigure connectionFigure = findFigure(connectionEditPart);
         if (connectionFigure == null) {
             String missingEnd = sourceEditPart == null ? "source" : "target"; //$NON-NLS-1$ //$NON-NLS-2$
             throw new StepExecutionException(
                     "No connection could be found for the given " + missingEnd + " figure.", //$NON-NLS-1$ //$NON-NLS-2$
                     EventFactory.createActionError(TestErrorEvent.NOT_FOUND));
         }
 
         // Scrolling
         revealEditPart(connectionEditPart);
 
         if (connectionFigure instanceof Connection) {
             Point midpoint =
                 ((Connection)connectionFigure).getPoints().getMidpoint();
             connectionFigure.translateToAbsolute(midpoint);
             getRobot().click(getViewerControl(), null,
                     ClickOptions.create().setScrollToVisible(false)
                         .setClickCount(count).setMouseButton(button),
                     midpoint.x, true, midpoint.y, true);
         } else {
             getRobot().click(getViewerControl(),
                     getBounds(connectionFigure),
                     ClickOptions.create().setScrollToVisible(false)
                         .setClickCount(count).setMouseButton(button));
         }
 
     }
 
     /**
      * Clicks the specified position within the given figure.
      *
      * @param textPath The path to the figure.
      * @param operator The operator used for matching.
      * @param count amount of clicks
      * @param button what button should be clicked
      * @param xPos what x position
      * @param xUnits should x position be pixel or percent values
      * @param yPos what y position
      * @param yUnits should y position be pixel or percent values
      * @throws StepExecutionException if step execution fails.
      */
     public void gdClickInFigure(String textPath, String operator,
         int count, int button, int xPos, String xUnits,
         int yPos, String yUnits) throws StepExecutionException {
 
         getRobot().click(getViewerControl(),
                 getFigureBoundsChecked(textPath, operator),
                 ClickOptions.create().setScrollToVisible(false)
                     .setClickCount(count).setMouseButton(button),
                 xPos, xUnits.equalsIgnoreCase(POS_UNIT_PIXEL),
                 yPos, yUnits.equalsIgnoreCase(POS_UNIT_PIXEL));
     }
 
     /**
      * Simulates the beginning of a Drag. Moves to the specified position
      * within the given figure and stores information related to the drag to
      * be used later by a Drop operation.
      *
      * @param textPath The path to the figure.
      * @param operator The operator used for matching.
      * @param mouseButton the mouse button.
      * @param modifier the modifier, e.g. shift, ctrl, etc.
      * @param xPos what x position
      * @param xUnits should x position be pixel or percent values
      * @param yPos what y position
      * @param yUnits should y position be pixel or percent values
      */
     public void gdDragFigure(String textPath, String operator,
             int mouseButton, String modifier, int xPos,
             String xUnits, int yPos, String yUnits) {
         // Only store the Drag-Information. Otherwise the GUI-Eventqueue
         // blocks after performed Drag!
         final DragAndDropHelperSwt dndHelper = DragAndDropHelperSwt
             .getInstance();
         dndHelper.setMouseButton(mouseButton);
         dndHelper.setModifier(modifier);
         dndHelper.setDragComponent(null);
         gdClickInFigure(textPath, operator, 0, mouseButton,
                 xPos, xUnits, yPos, yUnits);
     }
 
     /**
      * Performs a Drop. Moves to the specified location within the given figure
      * and releases the modifier and mouse button pressed by the previous drag
      * operation.
      *
      * @param textPath The path to the figure.
      * @param operator The operator used for matching.
      * @param xPos what x position
      * @param xUnits should x position be pixel or percent values
      * @param yPos what y position
      * @param yUnits should y position be pixel or percent values
      * @param delayBeforeDrop the amount of time (in milliseconds) to wait
      *                        between moving the mouse to the drop point and
      *                        releasing the mouse button
      */
     public void gdDropOnFigure(final String textPath, final String operator,
             final int xPos, final String xUnits, final int yPos,
             final String yUnits, int delayBeforeDrop) {
 
         final DragAndDropHelperSwt dndHelper =
             DragAndDropHelperSwt.getInstance();
         final IRobot robot = getRobot();
         final String modifier = dndHelper.getModifier();
         final int mouseButton = dndHelper.getMouseButton();
         // Note: This method performs the drag AND drop action in one runnable
         // in the GUI-Eventqueue because after the mousePress, the eventqueue
         // blocks!
         try {
             pressOrReleaseModifiers(modifier, true);
 
             getEventThreadQueuer().invokeAndWait("gdStartDragFigure", new IRunnable() { //$NON-NLS-1$
                 public Object run() throws StepExecutionException {
                     // drag
                     robot.mousePress(dndHelper.getDragComponent(), null,
                             mouseButton);
 
                     shakeMouse();
 
                     // drop
                     gdClickInFigure(textPath, operator, 0,
                             mouseButton, xPos, xUnits, yPos, yUnits);
 
                     return null;
                 }
             });
 
             waitBeforeDrop(delayBeforeDrop);
         } finally {
             getRobot().mouseRelease(null, null, mouseButton);
             pressOrReleaseModifiers(modifier, false);
         }
     }
 
     /**
      * Returns the bounds for the figure for the given path. If no such
      * figure can be found, a {@link StepExecutionException} will be thrown.
      *
      * @param textPath The path to the figure.
      * @param operator The operator used for matching.
      * @return the bounds of the figure for the given path.
      */
     private Rectangle getFigureBoundsChecked(String textPath, String operator) {
         GraphicalEditPart editPart =
             findEditPart(textPath, operator);
         IFigure figure = findFigure(editPart);
         ConnectionAnchor anchor = null;
 
         if (figure == null) {
             // Try to find a connection anchor instead
             anchor = findConnectionAnchor(textPath, operator);
             if (anchor != null) {
                 final String[] pathItems = MenuUtil.splitPath(textPath);
                 final String[] editPartPathItems =
                     new String[pathItems.length - 1];
                 System.arraycopy(
                         pathItems, 0, editPartPathItems, 0,
                         editPartPathItems.length);
                 editPart = findEditPart(operator, editPartPathItems);
             }
             if (anchor == null || findFigure(editPart) == null) {
                 throw new StepExecutionException(
                         "No figure could be found for the given text path.", //$NON-NLS-1$
                         EventFactory.createActionError(
                                 TestErrorEvent.NOT_FOUND));
             }
 
         }
 
         // Scrolling
         revealEditPart(editPart);
 
         return figure != null ? getBounds(figure) : getBounds(anchor);
     }
 
     /**
      * Clicks the tool found at the given path.
      *
      * @param textPath The path to the tool.
      * @param operator The operator used for matching.
      * @param count The number of times to click.
      */
     public void gdSelectTool(String textPath, String operator, int count) {
         Control paletteControl = getPaletteControl();
         IFigure figure =
             findPaletteFigureChecked(textPath, operator);
         getRobot().click(paletteControl, getBounds(figure),
                 ClickOptions.create().setScrollToVisible(false)
                     .setClickCount(count));
     }
 
     /**
      * @return the control associated with the palette viewer.
      */
     private Control getPaletteControl() {
         EditDomain editDomain = getViewer().getEditDomain();
         if (editDomain == null) {
             return null;
         }
 
         PaletteViewer paletteViewer = editDomain.getPaletteViewer();
         if (paletteViewer == null) {
             return null;
         }
 
         return paletteViewer.getControl();
     }
 
     /**
      *
      * @param figure The figure for which to find the bounds.
      * @return the bounds of the given figure.
      */
     private Rectangle getBounds(IFigure figure) {
         org.eclipse.draw2d.geometry.Rectangle figureBounds =
             new org.eclipse.draw2d.geometry.Rectangle(figure.getBounds());
 
         // Take scrolling and zooming into account
         figure.translateToAbsolute(figureBounds);
 
         return new Rectangle(
                 figureBounds.x, figureBounds.y,
                 figureBounds.width, figureBounds.height);
     }
 
     /**
      *
      * @param anchor The anchor for which to find the bounds.
      * @return the "bounds" of the given anchor. Since the location of an
      *         anchor is defined as a single point, the bounds are a small
      *         rectangle with that point at the center.
      */
     private Rectangle getBounds(ConnectionAnchor anchor) {
         Validate.notNull(anchor);
         Point refPoint = anchor.getReferencePoint();
 
         return new Rectangle(
                 refPoint.x - 1, refPoint.y - 1, 3, 3);
     }
 
     /**
      *
      * @param textPath The path to the GraphicalEditPart.
      * @param operator The operator used for matching.
      * @return the GraphicalEditPart for the given path. Returns
      *         <code>null</code> if no EditPart exists for the given path or if
      *         the found EditPart is not a GraphicalEditPart.
      */
     private GraphicalEditPart findEditPart(String textPath, String operator) {
         final String[] pathItems = MenuUtil.splitPath(textPath);
         return findEditPart(operator, pathItems);
     }
 
     /**
      * @param operator The operator used for matching.
      * @param pathItems The path to the GraphicalEditPart. Each element in the
      *                  array represents a single segment of the path.
      * @return the GraphicalEditPart for the given path. Returns
      *         <code>null</code> if no EditPart exists for the given path or if
      *         the found EditPart is not a GraphicalEditPart.
      */
     private GraphicalEditPart findEditPart(String operator,
             final String[] pathItems) {
         boolean isExisting = true;
         EditPart currentEditPart = getRootEditPart().getContents();
 
         for (int i = 0; i < pathItems.length && currentEditPart != null; i++) {
             List effectiveChildren = currentEditPart.getChildren();
             EditPart [] children =
                 (EditPart [])effectiveChildren.toArray(
                         new EditPart[effectiveChildren.size()]);
             boolean itemFound = false;
             for (int j = 0; j < children.length && !itemFound; j++) {
                 IEditPartIdentifier childFigureIdentifier =
                     DefaultEditPartAdapterFactory.loadFigureIdentifier(
                             children[j]);
                 if (childFigureIdentifier != null) {
                     String figureId = childFigureIdentifier.getIdentifier();
                     if (figureId != null
                         && MatchUtil.getInstance().match(
                             figureId, pathItems[i], operator)) {
                         itemFound = true;
                         currentEditPart = children[j];
                     }
                 }
             }
             if (!itemFound) {
                 isExisting = false;
                 break;
             }
 
         }
 
         return isExisting && currentEditPart instanceof GraphicalEditPart
             ? (GraphicalEditPart)currentEditPart : null;
     }
 
     /**
      *
      * @param textPath The path to the figure.
      * @param operator The operator used for matching.
      * @return the figure for the GraphicalEditPart for the given path within
      *         the palette. Returns <code>null</code> if no EditPart exists
      *         for the given path or if the found EditPart does not have a
      *         figure.
      */
     private IFigure findPaletteFigure(String textPath, String operator) {
         GraphicalEditPart editPart = findPaletteEditPart(textPath, operator);
 
         // Scrolling
         revealEditPart(editPart);
 
         return findFigure(editPart);
     }
 
     /**
      * Finds and returns the palette figure for the given path. If no such
      * figure can be found, a {@link StepExecutionException} will be thrown.
      *
      * @param textPath The path to the figure.
      * @param operator The operator used for matching.
      * @return the figure for the GraphicalEditPart for the given path within
      *         the palette.
      */
     private IFigure findPaletteFigureChecked(String textPath, String operator) {
         IFigure figure = findPaletteFigure(textPath, operator);
         if (figure == null) {
             throw new StepExecutionException(
                     "No palette figure could be found for the given text path.", //$NON-NLS-1$
                     EventFactory.createActionError(TestErrorEvent.NOT_FOUND));
         }
         return figure;
 
     }
 
     /**
      *
      * @param editPart The EditPart for which to find the corresponding figure.
      * @return the (visible) figure corresponding to the given EditPart, or
      *         <code>null</code> if no visible figure corresponds to the given
      *         EditPart.
      */
     private IFigure findFigure(GraphicalEditPart editPart) {
         if (editPart != null) {
             IFigure figure = editPart.getFigure();
             if (figure.isShowing()) {
                 return figure;
             }
         }
 
         return null;
     }
 
     /**
      * Attempts to find a connection anchor at the given textpath.
      *
      * @param textPath The path to the anchor.
      * @param operator The operator used for matching.
      * @return the anchor found at the given text path, or <code>null</code>
      *         if no such anchor exists.
      */
     private ConnectionAnchor findConnectionAnchor(
             String textPath, String operator) {
 
         final String[] pathItems = MenuUtil.splitPath(textPath);
         final String[] editPartPathItems = new String[pathItems.length - 1];
         System.arraycopy(
                 pathItems, 0, editPartPathItems, 0, editPartPathItems.length);
         GraphicalEditPart editPart = findEditPart(operator, editPartPathItems);
         if (editPart != null) {
             String anchorPathItem = pathItems[pathItems.length - 1];
             IEditPartIdentifier editPartIdentifier =
                 DefaultEditPartAdapterFactory.loadFigureIdentifier(editPart);
             if (editPartIdentifier != null) {
                 Map anchorMap =
                     editPartIdentifier.getConnectionAnchors();
                 if (anchorMap != null) {
                     Iterator anchorMapIter =
                         anchorMap.keySet().iterator();
                     while (anchorMapIter.hasNext()) {
                         Object anchorMapKey = anchorMapIter.next();
                         Object anchorMapValue =
                             anchorMap.get(anchorMapKey);
                         if (anchorMapKey instanceof String
                                 && anchorMapValue instanceof ConnectionAnchor
                                 && MatchUtil.getInstance().match(
                                     (String)anchorMapKey, anchorPathItem,
                                     operator)) {
 
                             return (ConnectionAnchor)anchorMapValue;
                         }
                     }
                 }
             }
         }
 
         return null;
     }
 
     /**
      * Reveals the given {@link EditPart} within its viewer.
      *
      * @param editPart the {@link EditPart} to reveal.
      */
     private void revealEditPart(final EditPart editPart) {
         if (editPart != null) {
             getEventThreadQueuer().invokeAndWait(getClass().getName() + ".revealEditPart", new IRunnable() { //$NON-NLS-1$
 
                 public Object run() throws StepExecutionException {
                     editPart.getViewer().reveal(editPart);
                     return null;
                 }
 
             });
         }
     }
 }
