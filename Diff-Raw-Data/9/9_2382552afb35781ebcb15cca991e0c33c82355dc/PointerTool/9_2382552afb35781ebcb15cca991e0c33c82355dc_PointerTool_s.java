 /*
  * PointerTool.java: GameTable is in the Public Domain.
  */
 
 
 package com.galactanet.gametable.tools;
 
 import java.awt.Graphics;
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.swing.JOptionPane;
 
 import com.galactanet.gametable.GametableCanvas;
 import com.galactanet.gametable.GametableFrame;
 import com.galactanet.gametable.Pog;
 import com.galactanet.gametable.prefs.PreferenceDescriptor;
 
 
 
 /**
  * The basic pog interaction tool.
  * 
  * @author iffy
  */
 public class PointerTool extends NullTool
 {
     private static final String PREF_DRAG = "com.galactanet.gametable.tools.PointerTool.drag";
 
     /**
      * @return The static, unmodifiable list of preferences for this tool.
      */
     private static final List createPreferenceList()
     {
         List retVal = new ArrayList();
         retVal.add(new PreferenceDescriptor(PREF_DRAG, "Drag map when not over Pog", PreferenceDescriptor.TYPE_FLAG,
             Boolean.TRUE));
         return Collections.unmodifiableList(retVal);
     }
 
     private static final List PREFERENCES = createPreferenceList();
 
     private GametableCanvas   m_canvas;
     private Pog               m_grabbedPog;
     private Pog               m_ghostPog;
     private boolean           m_snapping;
     private Point             m_grabOffset;
     private Point             m_mousePosition;
     private boolean           m_clicked   = true;
     private Point             m_startScroll;
     private Point             m_startMouse;
 
     /**
      * Constructor
      */
     public PointerTool()
     {
     }
 
     /*
      * @see com.galactanet.gametable.AbstractTool#activate(com.galactanet.gametable.GametableCanvas)
      */
     public void activate(GametableCanvas canvas)
     {
         m_canvas = canvas;
         m_grabbedPog = null;
         m_ghostPog = null;
         m_grabOffset = null;
         m_mousePosition = null;
         m_startScroll = null;
         m_startMouse = null;
     }
 
     /*
      * @see com.galactanet.gametable.Tool#isBeingUsed()
      */
     public boolean isBeingUsed()
     {
         return (m_grabbedPog != null) || (m_startScroll != null);
     }
 
     /*
      * @see com.galactanet.gametable.AbstractTool#mouseButtonPressed(int, int)
      */
     public void mouseButtonPressed(int x, int y, int modifierMask)
     {
         m_clicked = true;
         m_mousePosition = new Point(x, y);
         m_grabbedPog = m_canvas.getActiveMap().getPogAt(m_mousePosition);
         if (m_grabbedPog != null)
         {
             m_ghostPog = new Pog(m_grabbedPog);
             m_grabOffset = new Point(m_grabbedPog.getX() - m_mousePosition.x, m_grabbedPog.getY() - m_mousePosition.y);
             setSnapping(modifierMask);
         }
         else if (GametableFrame.g_gameTableFrame.getPreferences().getBooleanValue(PREF_DRAG))
         {
             m_startScroll = m_canvas.drawToModel(m_canvas.getPublicMap().getScrollX(), m_canvas.getPublicMap()
                 .getScrollY());
             m_startMouse = m_canvas.modelToView(x, y);
             m_canvas.setToolCursor(2);
         }
     }
 
     /*
      * @see com.galactanet.gametable.AbstractTool#mouseMoved(int, int)
      */
     public void mouseMoved(int x, int y, int modifierMask)
     {
         setSnapping(modifierMask);
         m_mousePosition = new Point(x, y);
         if (m_grabbedPog != null)
         {
             m_clicked = false;
             if (m_snapping)
             {
                 m_ghostPog.setPosition(m_mousePosition.x + m_grabOffset.x, m_mousePosition.y + m_grabOffset.y);
                 m_canvas.snapPogToGrid(m_ghostPog);
             }
             else
             {
                 m_ghostPog.setPosition(m_mousePosition.x + m_grabOffset.x, m_mousePosition.y + m_grabOffset.y);
             }
             m_canvas.repaint();
         }
         else if (m_startScroll != null)
         {
             Point mousePosition = m_canvas.modelToView(x, y);
             Point viewDelta = new Point(m_startMouse.x - mousePosition.x, m_startMouse.y - mousePosition.y);
             Point modelDelta = m_canvas.drawToModel(viewDelta);
             m_canvas.scrollMapTo(m_startScroll.x + modelDelta.x, m_startScroll.y + modelDelta.y);
         }
         else
         {
             hoverCursorCheck();
         }
     }
 
     /*
      * @see com.galactanet.gametable.AbstractTool#mouseButtonReleased(int, int)
      */
     public void mouseButtonReleased(int x, int y, int modifierMask)
     {
         if (m_grabbedPog != null)
         {
             if (m_clicked)
             {
                 String s = (String)JOptionPane.showInputDialog(GametableFrame.g_gameTableFrame, "Enter new Pog text:",
                     "Pog Text", JOptionPane.PLAIN_MESSAGE, null, null, m_grabbedPog.m_dataStr);
 
                 if (s != null)
                 {
                     m_canvas.setPogData(m_grabbedPog.m_ID, s);
                 }
             }
             else
             {
                 m_grabbedPog.setPosition(m_ghostPog.getPosition());
                 if (!m_canvas.isPointVisible(m_mousePosition))
                 {
                     // they removed this pog
                     m_canvas.removePog(m_grabbedPog.m_ID);
                 }
                 else
                 {
                     m_canvas.movePog(m_grabbedPog.m_ID, m_ghostPog.getX(), m_ghostPog.getY());
                 }
             }
         }
         endAction();
     }
     
     public void endAction()
     {
         m_grabbedPog = null;
         m_ghostPog = null;
         m_grabOffset = null;
         m_startScroll = null;
         m_startMouse = null;
         hoverCursorCheck();
     	m_canvas.repaint();
     }
 
     /*
      * @see com.galactanet.gametable.AbstractTool#paint(java.awt.Graphics)
      */
     public void paint(Graphics g)
     {
         if (m_ghostPog != null && m_canvas.isPointVisible(m_mousePosition))
         {
             m_ghostPog.drawGhostlyToCanvas(g);
         }
     }
 
     /*
      * @see com.galactanet.gametable.Tool#getPreferences()
      */
     public List getPreferences()
     {
         return PREFERENCES;
     }
 
     /**
      * Sets the snapping status based on the specified modifiers.
      * 
      * @param modifierMask the set of modifiers passed into the event.
      */
     private void setSnapping(int modifierMask)
     {
         if ((modifierMask & MODIFIER_CTRL) > 0)
         {
             m_snapping = false;
         }
         else
         {
             m_snapping = true;
         }
     }
 
     private void hoverCursorCheck()
     {
         if (GametableFrame.g_gameTableFrame.getPreferences().getBooleanValue(PREF_DRAG))
         {
             Pog pog = m_canvas.getActiveMap().getPogAt(m_mousePosition);
             if (pog != null)
             {
                 m_canvas.setToolCursor(0);
             }
             else
             {
                 m_canvas.setToolCursor(1);
             }
         }
     }
 }
