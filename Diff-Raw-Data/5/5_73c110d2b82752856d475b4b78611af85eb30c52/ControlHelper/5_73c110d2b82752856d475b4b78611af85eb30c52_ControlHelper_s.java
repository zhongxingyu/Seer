 /*---------------------------------------------------------------------------*
 * $Id$
 *----------------------------------------------------------------------------*
 * 08/06/2013 - shane
 * 
 * Initial version
 *---------------------------------------------------------------------------*/
 package com.thegaragelab.quickui.controls;
 
 //--- Imports
 import java.util.*;
 import com.thegaragelab.quickui.*;
 
 /** Control event dispatcher
  * 
  * This class provides static methods for allow listening to events from a
  * control and dispatching events to any registered listeners. It is a private
  * class that is not accessible outside the package.
  * 
  * TODO: The icons should really be part of the system font and have the same
  *       size.
  *       
  * TODO: Elements of this class would be useful for custom controls and should
  *       be available for them.
  */
 class ControlHelper {
   //--- Constants (internal)
   private static final String COMMON_CONTROLS = "controls";
   
   //--- Constants (public)
   public static final int ICON_WIDTH   = 14; //! Width of the icons (in pixels)
   public static final int ICON_HEIGHT  = 14; //! Height of the icons (in pixels)
   public static final int ICON_PADDING = 2;  //! Padding between the icons and text
   
   //--- Supported icons for controls
   public static final int ICON_CHECK_EMPTY    = 0;
   public static final int ICON_CHECK_SELECTED = 1;
   public static final int ICON_RADIO_EMPTY    = 2;
   public static final int ICON_RADIO_SELECTED = 3;
   
   //--- Static instance variables
   private static Icon m_icons; //! Icons for common controls
   private static WeakHashMap<ControlEventSource, IControlEventHandler> m_dispatcher;
   
   //-------------------------------------------------------------------------
   // Helper methods
   //-------------------------------------------------------------------------
 
   /** Dispatch a message from a control to any registered listeners.
    * 
    * @param control the control sending the event.
    * @param event the event ID being sent.
    * @param params parameters for the event.
    */
   public static synchronized void fireEvent(IControl control, int event, Object params) {
     // Do we have any handlers registered ?
     if(m_dispatcher==null)
       return;
     // Do we have one for this source and event ?
     ControlEventSource source = new ControlEventSource(control, event);
     if(!m_dispatcher.containsKey(source))
       return;
     IControlEventHandler handler = m_dispatcher.get(source);
     if(handler!=null)
       handler.onEvent(control, event, params);
     }
   
   /** Set the listener for an event.
    * 
    * @param control the control generating the event.
    * @param event the event ID to listen to.
    * @param handler the handler to process the event.
    */
   public static synchronized void setEventHandler(IControl control, int event, IControlEventHandler handler) {
     // Make sure we have something to hold the mapping
     if(m_dispatcher==null)
       m_dispatcher = new WeakHashMap<ControlEventSource, IControlEventHandler>();
     // Add it
     m_dispatcher.put(new ControlEventSource(control, event), handler);
     }
   
   /** Draw a control icon at the requested co-ordinates
    * 
    * @param surface the surface to draw on.
    * @param where where to draw the icon.
    * @param icon the icon number to draw.
    * @param color the color to draw the icon in.
    */
   public static void drawControlIcon(ISurface surface, IPoint where, int icon, Color color) {
     // Make sure we have icons
     synchronized(ControlHelper.class) {
       if(m_icons==null) {
         m_icons = Asset.loadIcon(COMMON_CONTROLS);
         }
       if(m_icons==null)
         return;
       }
     // Draw the requested icon
     // TODO: This is not very generic, it assume all icons are on the same line.
     surface.drawIcon(
       where,
       m_icons,
       color,
       new Rectangle(
         icon * ICON_WIDTH,
         0,
         ICON_WIDTH,
         ICON_HEIGHT
         )
       );
     }
   
   /** Determine where to position an element given padding and alignment.
    * 
    * @param container the rectangle describing the container.
    * @param content the dimension of the content to place.
    * @param padding the padding to apply to the positioning.
    * @param halign the horizontal alignment for positioning.
    * @param valign the vertical alignment for positioning.
    * 
    * @return a point indicating where to position the object.
    */
   public static final Point getPosition(IRectangle container, IDimension content, Padding padding, int halign, int valign) {
     Point p = new Point(0, 0);
     // Calculate the horizontal position
     switch(halign) {
       case IControl.LEFT:
         p.x = padding.getPaddingLeft();
         break;
       case IControl.CENTER:
        p.x = (container.getWidth() - padding.getPaddingLeft() - padding.getPaddingRight() - content.getWidth()) / 2;
         break;
       case IControl.RIGHT:
         p.x = container.getWidth() - padding.getPaddingRight() - content.getWidth();
         break;
       }
     // Calculate the vertical position
     switch(valign) {
       case IControl.TOP:
         p.y = padding.getPaddingTop();
         break;
       case IControl.MIDDLE:
        p.y = (container.getHeight() - padding.getPaddingTop() - padding.getPaddingBottom() - content.getHeight()) / 2;
         break;
       case IControl.BOTTOM:
         p.y = container.getHeight() - padding.getPaddingBottom() - content.getHeight();
         break;
       }
     // Adjust for any container offset and return
     p.translate(container);
     return p;
     }
   
   }
