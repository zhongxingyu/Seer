 /*
  * Created on 20-may-2004
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 package zz.utils.ui.popup;
 
 import java.awt.AWTEvent;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.AWTEventListener;
 
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 
 import zz.utils.Utils;
 
 
 /**
  * A popup that lays at a specific position in a container
  */
 public class RelativePopup extends AbstractPopup implements AWTEventListener
 {
 	private JComponent itsParent;
 	
 	private Point itsPosition;
 
 	/**
 	 * Creates a relative popup.
 	 * @param aContent The component that is displayed when the popup is shown
 	 * @param aParent The parent component in which the popup appears.
 	 * @param aPosition Coordinates of the popup relative to the parent element.
 	 */
 	public RelativePopup(JComponent aContent, JComponent aParent, Point aPosition)
 	{
 		super(aContent);
 		itsParent = aParent;
 		itsPosition = aPosition;
 	}
 
 	
 	public JComponent getOwner()
 	{
 		return itsParent;
 	}
 	
 	protected Rectangle getPopupBounds ()
 	{
 		Point theParentPosition = itsParent.getLocationOnScreen();
 		Point theOwnerPosition = getOwnerFrame().getLocationOnScreen();
 		
 		int theX = itsPosition.x + theParentPosition.x - theOwnerPosition.x;
 		int theY = itsPosition.y + theParentPosition.y - theOwnerPosition.y;
 		Dimension thePreferredSize = getContent().getPreferredSize();
 		int theW = thePreferredSize.width;
 		int theH = thePreferredSize.height;
 		return new Rectangle (theX, theY, theW, theH);
 	}
 
 	protected JFrame getOwnerFrame()
 	{
 		return (JFrame) Utils.getFrame(itsParent);
 	}
 
 	public void eventDispatched(AWTEvent arg0)
 	{
 		// TODO Auto-generated method stub
 		
 	}
 }
