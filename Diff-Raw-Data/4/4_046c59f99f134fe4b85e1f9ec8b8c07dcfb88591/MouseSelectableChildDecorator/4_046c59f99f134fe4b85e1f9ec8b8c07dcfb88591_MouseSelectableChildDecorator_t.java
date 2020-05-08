 /**
  * 
  */
 package org.eclipse.jst.pagedesigner.editpolicies;
 
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.Locator;
 import org.eclipse.draw2d.MouseEvent;
 import org.eclipse.draw2d.MouseListener;
 import org.eclipse.draw2d.MouseMotionListener;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.PrecisionRectangle;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.GraphicalEditPart;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jst.pagedesigner.PDPlugin;
 import org.eclipse.jst.pagedesigner.parts.ElementEditPart;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Display;
 
 class MouseSelectableChildDecorator extends NonVisualChildDecorator
 {
     // no visual or affordance showing
     private static final int           STATE_START = 0;
     
     // the host is showing hover feedback, but is not selected
     private static final int           STATE_HOST_HOVER = 1;
     
     // the host has primary selection
     private static final int           STATE_HOST_SELECTED = 2;
     
     // the selection handle for the decorator has mouse hover
     private static final int           STATE_HANDLE_HOVER = 3;
     
     // the selection handle has been selected (is showing)
     private static final int           STATE_HANDLE_MENU_BAR_SHOWING = 4;
     
     // the menu bar has hover
     private static final int           STATE_HANDLE_MENU_BAR_HOVER = 5;
     
     // the menu bar has primary selection
     private static final int           STATE_HANDLE_MENU_BAR_SELECTED = 6;
     
     public static final int           EVENT_HOST_HOVER_RECEIVED = 31;
     public static final int           EVENT_HOST_HOVER_LOST = 32;
     public static final int           EVENT_HOST_SELECTION_RECEIVED = 33;
     public static final int           EVENT_HOST_SELECTION_LOST = 34;
     private static final int          EVENT_HANDLE_HOVER_RECEIVED = 35;
     private static final int          EVENT_HANDLE_HOVER_LOST = 36;
     private static final int          EVENT_HANDLE_SELECTED = 37;
     private static final int          EVENT_ALL_SELECTION_LOST = 38;
     private static final int          EVENT_MENU_BAR_SELECTION_RECEIVED = 39;
     
     private MouseMotionListener      _motionListener;
     private MouseListener            _mouseListener;
     private boolean                  _isMouseOver = false;
     private ElementMenuBar           _elementMenuBar;
 
     private DisplayStateMachine      _stateMachine;
     private VerticalMenuLocator      _menuLocator;
     private AnimatedHideLocator      _hideLocator;
     private IFigure                  _hoverParent;
     private IFigure                  _selectionParent;
 
     private ISelectionChangedListener _menuSelectionListener;
     
     MouseSelectableChildDecorator(final GraphicalEditPart hostPart, int location, 
             IFigure hoverParent, IFigure selectionParent) {
         super(hostPart, location);
         _menuLocator = new VerticalMenuLocator(hostPart, this);
         _hideLocator = new AnimatedHideLocator();
         _elementMenuBar = ((ElementEditPart)hostPart).getElementMenuBar();
         _stateMachine = new DisplayStateMachine();
         _hoverParent = hoverParent;
         _selectionParent = selectionParent;
         
         _motionListener = new MouseMotionListener.Stub()
         {
             public void mouseEntered(MouseEvent me) {
                 _isMouseOver = true;
                 updateState(EVENT_HANDLE_HOVER_RECEIVED);
             }
     
             public void mouseExited(MouseEvent me) {
                 _isMouseOver = false;
                 updateState(EVENT_HANDLE_HOVER_LOST);
             }
         };
         addMouseMotionListener(_motionListener);
             
         _mouseListener = new MouseListener.Stub()
         {
             public void mousePressed(MouseEvent me) {
                 updateState(EVENT_HANDLE_SELECTED);
             }
         };
         addMouseListener(_mouseListener);
         
         _menuSelectionListener = new ISelectionChangedListener()
         {
             public void selectionChanged(SelectionChangedEvent event) {
                 IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                 if (selection.size() == 0)
                 {
                     // if the host part has been given back selection, then
                     // we have a host selection event
                     if (getOwner().getSelected() == EditPart.SELECTED_PRIMARY)
                     {
                         updateState(EVENT_HOST_SELECTION_RECEIVED);
                     }
                     // otherwise, both the host and the non-visual children are lost,
                     // so fire all selection lost
                     else
                     {
                         updateState(EVENT_ALL_SELECTION_LOST);
                     }
                 }
                 // otherwise, one or more non-visual children have selection
                 else
                 {
                     updateState(EVENT_MENU_BAR_SELECTION_RECEIVED);
                 }
             }
         };
         _elementMenuBar.addSelectionChangedListener(_menuSelectionListener);
     }
     
     public void paintFigure(Graphics g) {
         // TODO: could we use an image label toggle button here instead?    
         Image  arrowImage = null;
         
         if (_stateMachine.isMenuShowing())
         {
             arrowImage = PDPlugin.getDefault().getImage("pin_down.gif");
         }
         else
         {
             arrowImage = PDPlugin.getDefault().getImage("pin_up.gif");
         }
         
         Rectangle r = getBounds();
         g.setAlpha(75);
         g.setBackgroundColor(getFillColor());
         g.fillRectangle(r.x, r.y, r.width, r.height);
         g.setAlpha(getAlpha());
         g.drawImage(arrowImage, r.x+1, r.y+1);
         g.setForegroundColor(getBorderColor()); 
         g.drawRectangle(r.x, r.y, r.width-1, r.height-1);
     }
 
     /**
      * @param newState
      */
     public void updateState(int event)
     {
         int oldState = _stateMachine.doTransition(event);
         updateVisual(oldState);
     }
     
     protected void updateVisual(int oldState)
     {
         // overriding all other considerations is whether the menu bar even has
         // any items to show.  If not don't show anything
         if (!_elementMenuBar.hasChildParts())
         {
             if (getParent() != null)
             {
                 getParent().remove(this);
             }
             
             return;
         }
         
         switch (_stateMachine._curState)
         {
             case STATE_START:
                 hide(_elementMenuBar, false);
                 IFigure parent = getParent();
                 if (parent != null)
                 {
                     parent.remove(this);
                 }
             break;
             
             case STATE_HOST_HOVER:
                 if (_hoverParent != null)
                 {
                     _hoverParent.add(this);
                     validate();
                 }
                 show(_elementMenuBar, false);
                 setVisible(false);
             break;
             
             case STATE_HOST_SELECTED:
                 if (_selectionParent != null)
                 {
                     _selectionParent.add(this);
                     validate();
                 }
 
                 setVisible(true);
 
                 if (oldState != STATE_HOST_SELECTED
                         && oldState != STATE_HANDLE_HOVER)
                 {
                     show(_elementMenuBar, true);
                     hide(_elementMenuBar, true);
                 }
                 else
                 {
                     if (!_hideLocator._isAnimating)
                     {
                         hide(_elementMenuBar, false);
                     }
                 }
                 repaint();
             break;
             
             case STATE_HANDLE_HOVER:
                 if (_stateMachine.isMenuShowing(oldState))
                 {
                     hide(_elementMenuBar, false);
                 }
                 else
                 {
                     show(_elementMenuBar, false);
                 }
                 repaint();
             break;
             
             case STATE_HANDLE_MENU_BAR_SHOWING:
                 show(_elementMenuBar, true);
                 repaint();
             break;
             
             case STATE_HANDLE_MENU_BAR_HOVER:
             case STATE_HANDLE_MENU_BAR_SELECTED:
                 //revalidate();
             break;
             
             
             default:
                 
         }
     }
 
     protected void init() {
         setPreferredSize(new Dimension(12, 12));
     }
     
     public void dispose()
     {
         hide(_elementMenuBar, false);
         
         if (_motionListener != null)
         {
             removeMouseMotionListener(_motionListener);
             _motionListener = null;
         }
         
         if (_mouseListener != null)
         {
             removeMouseListener(_mouseListener);
             _mouseListener = null;
         }
         
         if (_menuSelectionListener != null)
         {
             _elementMenuBar.removeSelectionChangedListener(_menuSelectionListener);
             _menuSelectionListener = null;
         }
     }
     
     private void hide(ElementMenuBar menuBar, boolean animate)
     {
         if (animate)
         {
             final Point endPoint = this.getLocation().getCopy();
            //TODO: don't understand when translation is necessary...
            //this.translateToAbsolute(endPoint);
            
             endPoint.x += this.getBounds().width / 2;
             endPoint.y += this.getBounds().height / 2;
             _hideLocator.setHideEndPoint(endPoint);
             _hideLocator.relocate(menuBar);
         }
         else 
         {
             if (menuBar.getParent() != null)
             {
                 getParent().remove(menuBar);
             }
         }
     }
     
     private void show(ElementMenuBar menuBar, boolean enabled)
     {
         menuBar.setEnabled(enabled);
         getParent().add(menuBar);
         _menuLocator.relocate(menuBar);
     }
     
     protected int getAlpha() 
     {
         return (_isMouseOver || _stateMachine.isMenuShowing()) ? 255 : 75;
     }
     
     private class DisplayStateMachine
     {
         private int _curState = STATE_START;
         
         public int doTransition(int event)
         {
             final int     oldState = _curState;
             
             switch(_curState)
             {
                 case STATE_START:
                     // can only transition from start state
                     // on a host event
                     if (event == EVENT_HOST_HOVER_RECEIVED)
                     {
                         _curState = STATE_HOST_HOVER;
                     }
                     else if (event == EVENT_HOST_SELECTION_RECEIVED)
                     {
                         _curState = STATE_HOST_SELECTED;
                     }
                 break;
                 
                 case STATE_HOST_HOVER:
                     if (event == EVENT_HOST_SELECTION_RECEIVED)
                     {
                         _curState = STATE_HOST_SELECTED;
                     }
                     else if (event == EVENT_HOST_SELECTION_LOST
                             || event == EVENT_HOST_HOVER_LOST)
                     {
                         _curState = STATE_START;
                     }
                     else if (event == EVENT_HOST_HOVER_RECEIVED)
                     {
                         // preserve state in this case
                     }
                 break;
 
                 case STATE_HOST_SELECTED:
                     // once the host is selected,the only host event that
                     // that can change state is selection lost
                     if (event == EVENT_HOST_SELECTION_LOST)
                     {
                         _curState = STATE_START;
                     }
                     else if (event == EVENT_HANDLE_HOVER_RECEIVED)
                     {
                         _curState = STATE_HANDLE_HOVER;
                     }
                     else if (event == EVENT_HANDLE_SELECTED)
                     {
                         _curState = STATE_HANDLE_MENU_BAR_SHOWING;
                     }
                     else if (event == EVENT_ALL_SELECTION_LOST)
                     {
                         _curState = STATE_START;
                     }
                 break;
                     
                 case STATE_HANDLE_HOVER:
                     if (event == EVENT_HANDLE_HOVER_LOST)
                     {
                         _curState = STATE_HOST_SELECTED;
                     }
                     else if (event == EVENT_HANDLE_SELECTED)
                     {
                         _curState = STATE_HANDLE_MENU_BAR_SHOWING;
                     }
                     else if (event == EVENT_HOST_SELECTION_LOST)
                     {
                         _curState = STATE_START;
                     }
                 break;
                 case STATE_HANDLE_MENU_BAR_SHOWING:
                     if (event == EVENT_HANDLE_SELECTED)
                     {
                         _curState = STATE_HANDLE_HOVER;
                     }
                     else if (event == EVENT_MENU_BAR_SELECTION_RECEIVED)
                     {
                         _curState = STATE_HANDLE_MENU_BAR_SELECTED;
                     }
                     else if (event == EVENT_ALL_SELECTION_LOST)
                     {
                         _curState = STATE_START;
                     }
                 break;
 
                 case STATE_HANDLE_MENU_BAR_HOVER:
                 break;                    
 
                 case STATE_HANDLE_MENU_BAR_SELECTED:
                     if (event == EVENT_ALL_SELECTION_LOST)
                     {
                         _curState = STATE_START;
                     }
                     else if (event == EVENT_HANDLE_SELECTED)
                     {
                         _curState = STATE_HANDLE_HOVER;
                     }
                 break;
                 
             }
             
             
             return oldState;
         }
 
         public boolean isMenuShowing()
         {
             return isMenuShowing(_curState);
         }
 
         public boolean isMenuShowing(int state)
         {
             return _curState == STATE_HANDLE_MENU_BAR_SHOWING 
                     || _curState == STATE_HANDLE_MENU_BAR_HOVER
                     || _curState == STATE_HANDLE_MENU_BAR_SELECTED;
         }
     }
     
     private static class VerticalMenuLocator implements Locator
     {
         private IFigure  _referenceFigure;
         
         VerticalMenuLocator(GraphicalEditPart owner, IFigure reference)
         {
             _referenceFigure = reference;
         }
         
         public void relocate(IFigure target) 
         {
             final Rectangle finalBounds = getFinalMenuBounds(target);
             target.setBounds(finalBounds);
         }
         
         
         private Rectangle getInitialMenuBounds(final IFigure target)
         {
             Rectangle targetBounds = 
                 new PrecisionRectangle(_referenceFigure.getBounds().getResized(-1, -1));
             _referenceFigure.translateToAbsolute(targetBounds);
             target.translateToRelative(targetBounds);
             return targetBounds;
         }
 
         private Rectangle getFinalMenuBounds(final IFigure target)
         {
             final IFigure referenceFigure =  _referenceFigure;
             
             Rectangle targetBounds = getInitialMenuBounds(target);
             Dimension targetSize = target.getPreferredSize();
 
             // copied from super.relocate because relativeX/Y are private in super
             // changed from super to remove div by 2 that centers target; we want
             // it to be corner-to-corner
             targetBounds.x
                 += targetBounds.width+4;
             targetBounds.y
                   -= (targetSize.height / 2) - referenceFigure.getBounds().height/2;
             targetBounds.setSize(targetSize);
             //target.setBounds(targetBounds);
 
 //            final Rectangle viewPortRect = 
 //                ((IHTMLGraphicalViewer)_owner.getViewer()).getViewport().getBounds();
 //            final Rectangle targetRect = targetBounds.getCopy();
 //            
 //            targetRect.intersect(viewPortRect);
 
 //            int width = targetBounds.width - targetRect.width;
 //            int height = targetBounds.height - targetRect.height;
             
 //            if (width != 0)
 //            {
 //                targetBounds.x -= width;
 //            }
 //            
 //            if (height != 0)
 //            {
 //                targetBounds.y += height;
 //            }
             
             return targetBounds;
         }
     }
     
     private static class AnimatedHideLocator implements Locator
     {
         private Point _endPoint;
         private boolean _isAnimating;
         
         /**
          * @param endPoint -- must be absolute coordinate
          */
         public void setHideEndPoint(Point endPoint)
         {
             _endPoint = endPoint;
         }
         
         public void relocate(IFigure target) 
         {
             final Point newEndPoint = _endPoint.getCopy();
             target.translateToRelative(_endPoint);
             Rectangle startBounds = target.getBounds().getCopy();
             animateBoundsChange(target, startBounds, newEndPoint);
         }
 
         private void animateBoundsChange(final IFigure target, 
                 final Rectangle startBounds, 
                 final Point endPoint)
         {
             final int numSteps = 5;
             final int numMs = 500;
             final int timeSteps = numMs/numSteps;
             
             int xDelta = endPoint.x - startBounds.x;
             int yDelta = endPoint.y - startBounds.y;
             
             final int widthIncrement = -1 * startBounds.width / numSteps;
             final int heightIncrement = -1 * startBounds.height / numSteps;
             int xIncrement = xDelta / numSteps;
             int yIncrement = yDelta  / numSteps;
             
             target.setBounds(startBounds);
             if (widthIncrement != 0 || heightIncrement != 0)
             {
                 _isAnimating = true;
                 doAnimation(numMs, timeSteps, widthIncrement, heightIncrement, xIncrement, yIncrement, endPoint, target);
             }
         }
         
         private void doAnimation(final int remainingTime, 
                 final int timeIncrement, 
                 final int widthIncrement, final int heightIncrement
                 , final int xIncrement, final int yIncrement
                 , final Point endPoint
                 , final IFigure target)
         {
             Display.getCurrent().timerExec(timeIncrement, 
             new Runnable()
             {
             public void run() 
             {
                if (remainingTime <= 0)
                {
                    if (target.getParent() != null)
                    {
                        target.getParent().remove(target);
                    }
                    _isAnimating = false;
                }
                else
                {
                    final Rectangle curBounds = target.getBounds().getCopy();
                    curBounds.width += widthIncrement;
                    curBounds.height += heightIncrement;
                    curBounds.x += xIncrement;
                    curBounds.y += yIncrement;
                    target.setBounds(curBounds);
                    target.revalidate();
                    doAnimation(remainingTime-timeIncrement, timeIncrement, widthIncrement, heightIncrement, xIncrement, yIncrement, endPoint, target);
                }
             }
             });
          }
     }
 }
 
