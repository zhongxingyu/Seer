 /*
  * GametableCanvas.java: GameTable is in the Public Domain.
  */
 
 
 package com.galactanet.gametable;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.JButton;
 
 import com.galactanet.gametable.tools.NullTool;
 
 
 
 /**
  * TODO: comment
  * 
  * @author sephalon
  */
 public class GametableCanvas extends JButton implements MouseListener, MouseMotionListener, KeyListener,
     ComponentListener, ItemSelectable, MouseWheelListener
 {
     public final static int     VALUE_X               = 0;
     public final static int     VALUE_Y               = 1;
 
     public final static int     SNAP_DISTANCE         = 8;
 
     // drag modes
     public final static int     TOOL_MODE_ARROW       = 0;
     public final static int     TOOL_MODE_POINT       = 1;
     public final static int     TOOL_MODE_PEN         = 2;
     public final static int     TOOL_MODE_HAND        = 3;
     public final static int     TOOL_MODE_ERASER      = 4;
     public final static int     TOOL_MODE_LINE        = 5;
 
     public final static int     NUM_POINT_CURSORS     = 8;
     public final static int     POINT_CURSOR_OFFSET_X = 5;
     public final static int     POINT_CURSOR_OFFSET_Y = 6;
 
     // the size of a square at max zoom level (0)
     public final static int     BASE_SQUARE_SIZE      = 64;
 
     public final static int     NUM_ZOOM_LEVELS       = 5;
 
     public Image                m_mapBk;
 
     // this is the map (or layer) that all players share
     private GametableMap        m_sharedMap           = new GametableMap(true);
 
     // this points to whichever map is presently active
     private GametableMap        m_activeMap;
 
     // some cursors
     Cursor                      m_handCursor;
     Cursor                      m_penCursor;
     Cursor                      m_emptyCursor;
     Cursor                      m_eraserCursor;
     Image[]                     m_pointCursorImages   = new Image[NUM_POINT_CURSORS];
 
     // the frame
     private GametableFrame      m_gametableFrame;
 
     // zoom and top-left state
 
     // This is the number of screen pixels that are
     // used per model pixel. It's never less than 1
     public int                  m_zoom = 1;
 
     // the size of a square at the current zoom level
     public int                  m_squareSize = getSquareSizeForZoom(m_zoom);
 
     // hand tool
     int                         m_handToolStartX;
     int                         m_handToolStartY;
     int                         m_scrollStartX;
     int                         m_scrollStartY;
 
     // misc flags
     boolean                     m_bSpaceKeyDown;
     boolean                     m_bShiftKeyDown;
     boolean                     m_bControlKeyDown;
 
     // drag stuff
     // where they first clicked
     int                         m_clickX;
     int                         m_clickY;
 
     // where the mouse is now
     int                         m_dragX;
     int                         m_dragY;
 
     // hand tool
     int                         m_preClickScrollX;
     int                         m_preClickScrollY;
 
     // Pen tool
     PenAsset                    m_penAsset;
 
     boolean                     m_bLDragging;
     boolean                     m_bRDragging;
     boolean                     m_bNeedToDrawPointCursor;
     int                         m_toolMode;
 
     Image                       m_offscreen;
     int                         m_offscreenX;
     int                         m_offscreenY;
 
     boolean                     m_bPogBeingDragged;
     Pog                         m_pogBeingDragged;
     int                         m_pogDragInsetX;
     int                         m_pogDragInsetY;
 
     private Point               m_mouseModelAnchor;
     private Point               m_mouseModelFloat;
     int                         m_currentMouseX;
     int                         m_currentMouseY;
     boolean                     m_bMouseOnView;
 
     Pog                         m_pogMouseOver;
 
     boolean                     m_bHexMode;
     private Image[]             m_hexImages           = new Image[NUM_ZOOM_LEVELS];  // one hex
     // image per
     // zoom
     // level
     private int[]               m_hexImageOffsets     = new int[NUM_ZOOM_LEVELS];    // how far
     // in along
     // the
     // x-axis
     // the corner of the top-left hex is
 
     private int                 m_activeToolId        = -1;
     private static final Tool   NULL_TOOL             = new NullTool();
 
     public final static boolean SPECIAL_RIGHT_CLICK   = true;                        // true if
     // special
     // right-click
     // behaviour
     // is
     // enabled
     public boolean              m_bRightClicking;                                    // true if
     // the
     // current
     // mouse
     // action
     // was
     // initiated
     // with a
     // right-click
     public int                  m_preRightClickToolID;                               // the id of
 
     // the tool
     // that we
     // switched
     // out of to
     // go to
     // hand tool
     // for a
     // right-click
 
     public GametableCanvas()
     {
         addMouseListener(this);
         addMouseMotionListener(this);
         addKeyListener(this);
         addComponentListener(this);
 
         m_activeMap = m_sharedMap;
         m_bHexMode = false;
     }
 
     public void init(GametableFrame frame)
     {
         m_gametableFrame = frame;
         m_mapBk = UtilityFunctions.getImage("assets/mapbk.png");
 
         m_pointCursorImages[0] = UtilityFunctions.getImage("assets/whiteHand.png");
         m_pointCursorImages[1] = UtilityFunctions.getImage("assets/brownHand.png");
         m_pointCursorImages[2] = UtilityFunctions.getImage("assets/purpleHand.png");
         m_pointCursorImages[3] = UtilityFunctions.getImage("assets/blueHand.png");
         m_pointCursorImages[4] = UtilityFunctions.getImage("assets/redHand.png");
         m_pointCursorImages[5] = UtilityFunctions.getImage("assets/greenHand.png");
         m_pointCursorImages[6] = UtilityFunctions.getImage("assets/greyHand.png");
         m_pointCursorImages[7] = UtilityFunctions.getImage("assets/yellowHand.png");
 
         setPrimaryScroll(m_sharedMap, 0, 0);
 
         // set up the hex images
         m_hexImages[0] = UtilityFunctions.getImage("assets/hexes_64.png");
         m_hexImages[1] = UtilityFunctions.getImage("assets/hexes_48.png");
         m_hexImages[2] = UtilityFunctions.getImage("assets/hexes_32.png");
         m_hexImages[3] = UtilityFunctions.getImage("assets/hexes_16.png");
         m_hexImages[4] = null; // no lines are drawn at this zoom level. So there's no hex image
         // for it.
 
         // magic numbers - these represent the distance in along the x-axis that the corner
         // of the hex is.
         // Note that the top left corner of the first hex is not aligned with the left of
         // the image
         // | ----------
         // | /
         // |/
         // |\
         // | \
         // | ----------
         // That distance is what is represented here.
 
         m_hexImageOffsets[0] = 19;
         m_hexImageOffsets[1] = 15;
         m_hexImageOffsets[2] = 10;
         m_hexImageOffsets[3] = 5;
         m_hexImageOffsets[4] = 0; // irrelevant. There is no image for this level. Lines aren't
         // drawn at this zoom level.
 
         addMouseWheelListener(this);
         setZoom(0);
         setActiveTool(0);
     }
 
     public int getModifierFlags()
     {
         return ((m_bControlKeyDown ? Tool.MODIFIER_CTRL : 0) | (m_bSpaceKeyDown ? Tool.MODIFIER_SPACE : 0) | (m_bShiftKeyDown ? Tool.MODIFIER_SHIFT
             : 0));
     }
 
     /**
      * Sets the mouse cursor to be the cursor at the specified index for the currently active tool.
      * 
      * @param index The cursor of the given index for this tool. A negative number means no cursor.
      */
     public void setToolCursor(int index)
     {
         if (index < 0)
         {
             setCursor(m_emptyCursor);
         }
         else
         {
             setCursor(m_gametableFrame.getToolManager().getToolInfo(m_activeToolId).getCursor(index));
         }
     }
 
     public void setActiveTool(int index)
     {
         Tool oldTool = getActiveTool();
         oldTool.deactivate();
 
         m_activeToolId = index;
 
         Tool tool = getActiveTool();
         tool.activate(this);
         setToolCursor(0);
         m_gametableFrame.setToolSelected(m_activeToolId);
     }
 
     public Tool getActiveTool()
     {
         if (m_activeToolId < 0)
         {
             return NULL_TOOL;
         }
         return m_gametableFrame.getToolManager().getToolInfo(m_activeToolId).getTool();
     }
 
     public GametableMap getSharedMap()
     {
         return m_sharedMap;
     }
 
     public GametableMap getActiveMap()
     {
         return m_activeMap;
     }
 
     public PogsPanel getActivePogsArea()
     {
         PogsPanel ret = (PogsPanel)m_gametableFrame.m_pogsTabbedPane.getSelectedComponent();
         return ret;
     }
 
     // conversion from pogsview coords
     public Point pogsViewToCanvasView(int x, int y)
     {
         x -= getActivePogsArea().getWidth();
         x -= m_gametableFrame.m_mapPogSplitPane.getDividerSize();
         y += getActivePogsArea().getY();
         return new Point(x, y);
     }
 
     public static int getSquareSizeForZoom(int level)
     {
         int ret = BASE_SQUARE_SIZE;
         switch (level)
         {
             case 0:
             {
                 ret = BASE_SQUARE_SIZE;
             }
             break;
 
             case 1:
             {
                 ret = (BASE_SQUARE_SIZE / 4) * 3;
             }
             break;
 
             case 2:
             {
                 ret = BASE_SQUARE_SIZE / 2;
             }
             break;
 
             case 3:
             {
                 ret = BASE_SQUARE_SIZE / 4;
             }
             break;
 
             case 4:
             {
                 ret = BASE_SQUARE_SIZE / 8;
             }
             break;
         }
 
         return ret;
     }
 
     public void setZoom(int zoomLevel)
     {
         if (zoomLevel < 0)
         {
             zoomLevel = 0;
         }
 
         if (zoomLevel >= NUM_ZOOM_LEVELS)
         {
             zoomLevel = NUM_ZOOM_LEVELS - 1;
         }
 
         if (m_zoom != zoomLevel)
         {
             m_zoom = zoomLevel;
             m_squareSize = getSquareSizeForZoom(m_zoom);
             repaint();
         }
     }
 
     // returns a good line width to draw things
     public int getLineStrokeWidth()
     {
         switch (m_zoom)
         {
             case 0:
             {
                 return 3;
             }
 
             case 1:
             {
                 return 2;
             }
 
             case 2:
             {
                 return 2;
             }
 
             case 3:
             {
                 return 1;
             }
 
             default:
             {
                 return 1;
             }
         }
     }
 
     // conversion from model to view coordinates
     public Point modelToDraw(Point modelPoint)
     {
         double squaresX = (double)modelPoint.x / (double)BASE_SQUARE_SIZE;
         double squaresY = (double)modelPoint.y / (double)BASE_SQUARE_SIZE;
 
         int viewX = (int)Math.round(squaresX * m_squareSize);
         int viewY = (int)Math.round(squaresY * m_squareSize);
 
         return new Point(viewX, viewY);
     }
 
     public Point modelToDraw(int modelX, int modelY)
     {
         return modelToDraw(new Point(modelX, modelY));
     }
 
     public Point modelToView(Point modelPoint)
     {
         double squaresX = (double)modelPoint.x / (double)BASE_SQUARE_SIZE;
         double squaresY = (double)modelPoint.y / (double)BASE_SQUARE_SIZE;
 
         int viewX = (int)Math.round(squaresX * m_squareSize);
         int viewY = (int)Math.round(squaresY * m_squareSize);
 
         viewX -= getActiveMap().getScrollX();
         viewY -= getActiveMap().getScrollY();
 
         return new Point(viewX, viewY);
     }
 
     public Point modelToView(int modelX, int modelY)
     {
         return modelToView(new Point(modelX, modelY));
     }
 
     public Point drawToModel(Point drawPoint)
     {
         double squaresX = (double)(drawPoint.x) / (double)m_squareSize;
         double squaresY = (double)(drawPoint.y) / (double)m_squareSize;
 
         int modelX = (int)(squaresX * BASE_SQUARE_SIZE);
         int modelY = (int)(squaresY * BASE_SQUARE_SIZE);
 
         return new Point(modelX, modelY);
     }
 
     public Point drawToModel(int modelX, int modelY)
     {
         return drawToModel(new Point(modelX, modelY));
     }
 
     public Point viewToModel(Point viewPoint)
     {
         double squaresX = (double)(viewPoint.x + getActiveMap().getScrollX()) / (double)m_squareSize;
         double squaresY = (double)(viewPoint.y + getActiveMap().getScrollY()) / (double)m_squareSize;
 
         int modelX = (int)(squaresX * BASE_SQUARE_SIZE);
         int modelY = (int)(squaresY * BASE_SQUARE_SIZE);
 
         return new Point(modelX, modelY);
     }
 
     public Point viewToModel(int viewX, int viewY)
     {
         return viewToModel(new Point(viewX, viewY));
     }
 
     public double modelToSquares(double m)
     {
         return (m / BASE_SQUARE_SIZE);
     }
 
     /** *********************************************************** */
     // MouseListener/MouseMotionListener overrides:
     /** *********************************************************** */
     public void mouseDragged(MouseEvent e)
     {
         // We handle dragging ourselves - don't tread on me, Java!
         mouseMoved(e);
     }
 
     public void mouseMoved(MouseEvent e)
     {
         m_mouseModelFloat = viewToModel(e.getX(), e.getY());
         if (isPointing())
         {
             return;
         }
 
         getActiveTool().mouseMoved(m_mouseModelFloat.x, m_mouseModelFloat.y, getModifierFlags());
         Pog prevPog = m_pogMouseOver;
         m_pogMouseOver = getActiveMap().getPogAt(m_mouseModelFloat);
         if (prevPog != m_pogMouseOver)
         {
             repaint();
         }
     }
 
     public void mouseClicked(MouseEvent e)
     {
         // Ignore this because java has sucky mouse clicking
     }
 
     public void mousePressed(MouseEvent e)
     {
         m_mouseModelAnchor = viewToModel(e.getX(), e.getY());
         m_mouseModelFloat = m_mouseModelAnchor;
         if (isPointing())
         {
             return;
         }
         if (SPECIAL_RIGHT_CLICK)
         {
             // this code deals with making a right click automatically be the hand tool
             if (e.getButton() == MouseEvent.BUTTON3)
             {
                 m_bRightClicking = true;
                 m_preRightClickToolID = m_activeToolId;
                 setActiveTool(1); // HACK -- To hand tool
                 getActiveTool().mouseButtonPressed(m_mouseModelAnchor.x, m_mouseModelAnchor.y, getModifierFlags());
             }
             else
             {
                 m_bRightClicking = false;
             }
         }
 
         if (e.getButton() == MouseEvent.BUTTON1)
         {
             getActiveTool().mouseButtonPressed(m_mouseModelAnchor.x, m_mouseModelAnchor.y, getModifierFlags());
         }
 
     }
 
     public void mouseReleased(MouseEvent e)
     {
         m_mouseModelFloat = viewToModel(e.getX(), e.getY());
         m_mouseModelAnchor = null;
         if (isPointing())
         {
             return;
         }
         getActiveTool().mouseButtonReleased(m_mouseModelFloat.x, m_mouseModelFloat.y, getModifierFlags());
 
         if (SPECIAL_RIGHT_CLICK)
         {
             // yes, these two ifs could have been made into one compound statement.
             // but SPECIAL_RIGHT_CLICK is basically a precompiler step, so I wanted to
             // visually differentiate it.
             if (m_bRightClicking)
             {
                 // return to arrow too
                 setActiveTool(m_preRightClickToolID);
                 m_bRightClicking = false;
             }
         }
     }
 
     public void mouseEntered(MouseEvent e)
     {
         m_bMouseOnView = true;
     }
 
     public void mouseExited(MouseEvent e)
     {
         m_bMouseOnView = false;
     }
 
     public void mouseWheelMoved(MouseWheelEvent e)
     {
         if (e.getWheelRotation() < 0)
         {
             // zoom in
             centerZoom(-1);
         }
         else if (e.getWheelRotation() > 0)
         {
             // zoom out
             centerZoom(1);
         }
         repaint();
     }
 
     public Pog getPogByID(int id)
     {
         for (int i = 0; i < getActiveMap().getNumPogs(); i++)
         {
             Pog pog = getActiveMap().getPogAt(i);
             if (pog.m_ID == id)
             {
                 return pog;
             }
         }
         return null;
     }
 
     public void recenterView(int modelCenterX, int modelCenterY, int zoomLevel)
     {
         m_gametableFrame.push(PacketManager.makeRecenterPacket(modelCenterX, modelCenterY, zoomLevel));
 
         if (m_gametableFrame.m_netStatus != GametableFrame.NETSTATE_JOINED)
         {
             doRecenterView(modelCenterX, modelCenterY, zoomLevel);
         }
     }
 
     /*
      * This function will set the scroll for all maps, keeping their relative offsets preserved. The x,y values sent in
      * will become the scroll values for the desired map. All others maps will preserve offsets from that.
      */
     public void setPrimaryScroll(GametableMap mapToSet, int x, int y)
     {
         int dx = x - mapToSet.getScrollX();
         int dy = y - mapToSet.getScrollY();
        m_sharedMap.setScroll(dx + mapToSet.getScrollX(), dy + mapToSet.getScrollY());
     }
 
     public void doRecenterView(int modelCenterX, int modelCenterY, int zoomLevel)
     {
         // make the sent in x and y our center, ad the sent in zoom.
         // So start with the zoom
         setZoom(zoomLevel);
 
         // find the view coordinate for the model center
         setPrimaryScroll(m_sharedMap, 0, 0);
 
         // we need to get the coords for the shared map, even if we're not on that at the moment
         // so we cheezily set to the shared map, then return it to normal after the
         // call to modelToView
         GametableMap storedMap = m_activeMap;
         m_activeMap = m_sharedMap;
         Point viewCenter = modelToView(modelCenterX, modelCenterY);
         m_activeMap = storedMap;
 
         // find where the top left would have to be, based on our size
         int tlX = viewCenter.x - getWidth() / 2;
         int tlY = viewCenter.y - getHeight() / 2;
 
         // that is our new scroll position
         setPrimaryScroll(m_sharedMap, tlX, tlY);
 
         repaint();
     }
 
     public void setPogData(int id, String s)
     {
         m_gametableFrame.push(PacketManager.makePogDataPacket(id, s));
 
         if (m_gametableFrame.m_netStatus != GametableFrame.NETSTATE_JOINED)
         {
             doSetPogData(id, s);
         }
     }
 
     public void doSetPogData(int id, String s)
     {
         Pog pog = getPogByID(id);
         if (pog == null)
         {
             return;
         }
 
         pog.m_dataStr = s;
         repaint();
     }
 
     public void movePog(int id, int newX, int newY)
     {
         m_gametableFrame.push(PacketManager.makeMovePogPacket(id, newX, newY));
 
         if (m_gametableFrame.m_netStatus != GametableFrame.NETSTATE_JOINED)
         {
             doMovePog(id, newX, newY);
         }
     }
 
     public void doMovePog(int id, int newX, int newY)
     {
         Pog toMove = getPogByID(id);
         if (toMove == null)
         {
             return;
         }
 
         toMove.setPosition(newX, newY);
 
         // this pog moves to the end of the array
         getActiveMap().removePog(toMove);
         getActiveMap().addPog(toMove);
 
         repaint();
     }
 
     public void removePog(int id)
     {
         int removeArray[] = new int[1];
         removeArray[0] = id;
         removePogs(removeArray);
     }
 
     public void removePogs(int ids[])
     {
         m_gametableFrame.push(PacketManager.makeRemovePogsPacket(ids));
 
         if (m_gametableFrame.m_netStatus != GametableFrame.NETSTATE_JOINED)
         {
             doRemovePogs(ids);
         }
     }
 
     public void doRemovePogs(int ids[])
     {
         for (int i = 0; i < ids.length; i++)
         {
             doRemovePog(ids[i]);
         }
     }
 
     public void doRemovePog(int id)
     {
         Pog toRemove = getPogByID(id);
         if (toRemove != null)
         {
             getActiveMap().removePog(toRemove);
         }
 
         // if they were dragging that pog, stop it
         if (m_bLDragging && m_pogBeingDragged != null && m_pogBeingDragged.m_ID == id)
         {
             m_pogBeingDragged = null;
             m_gametableFrame.logSystemMessage("The pog you were holding disappears out of your hands!");
         }
         repaint();
     }
 
     public void addPog(Pog toAdd)
     {
         toAdd.getUniqueID();
         m_gametableFrame.push(PacketManager.makeAddPogPacket(toAdd));
 
         if (m_gametableFrame.m_netStatus != GametableFrame.NETSTATE_JOINED)
         {
             doAddPog(toAdd);
         }
     }
 
     public void doAddPog(Pog toAdd)
     {
         getActiveMap().addPog(toAdd);
         repaint();
     }
 
     public void addLineSegments(LineSegment[] lines)
     {
         // if we're the host, push it to everyone and add the lines.
         // if we're a joiner, just push it to the host
         m_gametableFrame.push(PacketManager.makeLinesPacket(lines));
 
         // if we're the host or if we're offline, go ahead and add them now
         if (m_gametableFrame.m_netStatus != GametableFrame.NETSTATE_JOINED)
         {
             doAddLineSegments(lines);
         }
     }
 
     public void doAddLineSegments(LineSegment[] lines)
     {
         if (lines != null)
         {
             for (int i = 0; i < lines.length; i++)
             {
                 getActiveMap().addLine(lines[i]);
             }
         }
         repaint();
     }
 
     public void erase(Rectangle r, boolean bColorSpecific, int color)
     {
         // if we're the host, push it to everyone and add the lines.
         // if we're a joiner, just push it to the host
         m_gametableFrame.push(PacketManager.makeErasePacket(r, bColorSpecific, color));
         if (m_gametableFrame.m_netStatus != GametableFrame.NETSTATE_JOINED)
         {
             doErase(r, bColorSpecific, color);
         }
 
     }
 
     public void doErase(Rectangle r, boolean bColorSpecific, int color)
     {
         Point modelStart = new Point(r.x, r.y);
         Point modelEnd = new Point(r.x + r.width, r.y + r.height);
 
         ArrayList survivingLines = new ArrayList();
         for (int i = 0; i < getActiveMap().getNumLines(); i++)
         {
             LineSegment ls = getActiveMap().getLineAt(i);
 
             if (!bColorSpecific || ls.getColor().getRGB() == color)
             {
                 // we are the color being erased, or we're in erase all
                 // mode
                 LineSegment[] result = ls.crop(modelStart, modelEnd);
 
                 if (result != null)
                 {
                     // this line segment is still alive
                     for (int j = 0; j < result.length; j++)
                     {
                         survivingLines.add(result[j]);
                     }
                 }
             }
             else
             {
                 // we are not affected by this erasing because we
                 // aren't the color being erased.
                 survivingLines.add(ls);
             }
         }
 
         // now we have just the survivors
         // replace all the lines with this list
         getActiveMap().clearLines();
         for (int i = 0; i < survivingLines.size(); i++)
         {
             getActiveMap().addLine((LineSegment)survivingLines.get(i));
         }
         repaint();
     }
 
     // called by the pogs area when a pog is being dragged
     public void pogDrag()
     {
         m_bPogBeingDragged = true;
         updatePogDropLoc();
 
         repaint();
     }
 
     public void pogDrop()
     {
         m_bPogBeingDragged = false;
         updatePogDropLoc();
 
         // only add the pog if it's in the viewport
         if (pogInViewport(getActivePogsArea().m_selectedPog))
         {
             // add this pog to the list
             addPog(getActivePogsArea().m_selectedPog);
         }
 
         // make the arrow the current tool
         setActiveTool(0);
     }
 
     public boolean isPointVisible(Point modelPoint)
     {
         Point portalTL = viewToModel(0, 0);
         Point portalBR = viewToModel(getWidth(), getHeight());
         if (modelPoint.x > portalBR.x)
         {
             return false;
         }
 
         if (modelPoint.y > portalBR.y)
         {
             return false;
         }
 
         if (modelPoint.x < portalTL.x)
         {
             return false;
         }
 
         if (modelPoint.y < portalTL.y)
         {
             return false;
         }
         return true;
     }
 
     public boolean pogInViewport(Pog pog)
     {
         // only add the pog if they dropped it in the visible area
         int width = pog.getFaceSize() * BASE_SQUARE_SIZE;
 
         // get the model coords of the viewable area
         Point portalTL = viewToModel(0, 0);
         Point portalBR = viewToModel(getWidth(), getHeight());
 
         if (pog.getX() > portalBR.x)
         {
             return false;
         }
         if (pog.getY() > portalBR.y)
         {
             return false;
         }
         if (pog.getX() + width < portalTL.x)
         {
             return false;
         }
         if (pog.getY() + width < portalTL.y)
         {
             return false;
         }
         return true;
     }
 
     public void updatePogDropLoc()
     {
         int pogViewX = getActivePogsArea().m_dragX;
         int pogViewY = getActivePogsArea().m_dragY;
 
         // convert to our coordinates
         Point canvasView = pogsViewToCanvasView(pogViewX, pogViewY);
 
         // now convert to model coordinates
         Point canvasModel = viewToModel(canvasView.x, canvasView.y);
 
         // now, snap to grid if they don't have the control key down
 
         // massage the pog location
         m_pogDragInsetX = getActivePogsArea().m_pogDragMouseInsetX;
         m_pogDragInsetY = getActivePogsArea().m_pogDragMouseInsetY;
         getActivePogsArea().m_selectedPog.setPosition(canvasModel.x - m_pogDragInsetX, canvasModel.y - m_pogDragInsetY);
 
         // this function is only called when we're dragging from the pogs
         // area. So we have to cheexe our "current Mouse" coordinates
         m_currentMouseX = canvasView.x;
         m_currentMouseY = canvasView.y;
 
         if (!m_bControlKeyDown)
         {
             snapPogToGrid(getActivePogsArea().m_selectedPog);
         }
     }
 
     public void snapPogToGrid(Pog pog)
     {
         Point snappedPoint = snapPointEx(new Point(pog.getX(), pog.getY()), true, BASE_SQUARE_SIZE * pog.getFaceSize());
         pog.setPosition(snappedPoint.x, snappedPoint.y);
     }
 
     public void scrollMapTo(int modelX, int modelY)
     {
         Point target = modelToDraw(modelX, modelY);
         // System.out.println("scrollMapTo(" + target.x + ", " + target.y + ")");
         m_sharedMap.setScroll(target.x, target.y);
         repaint();
     }
 
     public void updateDrag(MouseEvent e)
     {
         updateLocation(e);
 
         // if they're doing a hand drag, we respons immediately
         if (m_toolMode == TOOL_MODE_HAND)
         {
             int scrX = m_preClickScrollX + m_clickX - m_dragX;
             int scrY = m_preClickScrollY + m_clickY - m_dragY;
             setPrimaryScroll(getActiveMap(), scrX, scrY);
         }
 
         if (m_toolMode == TOOL_MODE_PEN)
         {
             // add a point to the Pen asset
             Point modelMouse = viewToModel(e.getX(), e.getY());
             m_penAsset.addPoint(modelMouse.x, modelMouse.y);
         }
 
         if (m_toolMode == TOOL_MODE_LINE)
         {
             if (m_bShiftKeyDown)
             {
                 // force to 90 degree increments
                 int dx = Math.abs(m_dragX - m_clickX);
                 int dy = Math.abs(m_dragY - m_clickY);
 
                 if (dx > dy)
                 {
                     // force to horizontal
                     m_dragY = m_clickY;
                 }
                 else
                 {
                     // force to vertical
                     m_dragX = m_clickX;
                 }
             }
 
             checkSnap();
         }
 
         if (m_toolMode == TOOL_MODE_ARROW)
         {
             // get the model loc of the current mouse position
             Point modelClick = viewToModel(m_dragX, m_dragY);
 
             if (m_pogBeingDragged != null)
             {
                 m_pogBeingDragged.setPosition(modelClick);
 
                 if (m_bControlKeyDown)
                 {
                     m_pogBeingDragged.setPosition(m_pogBeingDragged.getX() - m_pogDragInsetX, m_pogBeingDragged.getY()
                         - m_pogDragInsetY);
                 }
                 else
                 {
                     m_pogBeingDragged.setPosition(m_pogBeingDragged.getX() - m_pogBeingDragged.getFaceSize()
                         * BASE_SQUARE_SIZE / 2, m_pogBeingDragged.getY() - m_pogBeingDragged.getFaceSize()
                         * BASE_SQUARE_SIZE / 2);
                     snapPogToGrid(m_pogBeingDragged);
                 }
             }
         }
 
         repaint();
         getActivePogsArea().repaint();
     }
 
     public void checkSnap()
     {
         if (!m_bLDragging)
         {
             return;
         }
 
         if (m_toolMode != TOOL_MODE_LINE)
         {
             return;
         }
 
         // snap to grid if control is not pressed
         if (!m_bControlKeyDown)
         {
             Point snappedViewPoint = snapViewPoint(new Point(m_dragX, m_dragY));
             int snapToX = snappedViewPoint.x;
             int snapToY = snappedViewPoint.y;
 
             if (Math.abs(snapToX - m_dragX) < SNAP_DISTANCE)
             {
                 m_dragX = snapToX;
             }
             if (Math.abs(snapToY - m_dragY) < SNAP_DISTANCE)
             {
                 m_dragY = snapToY;
             }
         }
         else
         {
             m_dragX = m_currentMouseX;
             m_dragY = m_currentMouseY;
         }
     }
 
     // Takes a model coordinate. Snaps it to the XY grid
     private int getGridSnap(int i)
     {
         if (i < 0)
         {
             return ((i - BASE_SQUARE_SIZE / 2) / BASE_SQUARE_SIZE) * BASE_SQUARE_SIZE;
         }
         return ((i + BASE_SQUARE_SIZE / 2) / BASE_SQUARE_SIZE) * BASE_SQUARE_SIZE;
     }
 
     public Point snapViewPoint(Point viewPoint)
     {
         Point modelPoint = viewToModel(viewPoint);
         Point modelSnap = snapPoint(modelPoint);
         Point viewSnap = modelToView(modelSnap);
         return viewSnap;
     }
 
     private boolean isOffsetColumn(int columnNumber)
     {
         if (columnNumber < 0)
         {
             columnNumber = -columnNumber;
         }
         if (columnNumber % 2 == 1)
         {
             return true;
         }
         return false;
     }
 
     private double pointDistance(Point p1, Point p2)
     {
         int dx = p1.x - p2.x;
         int dy = p1.y - p2.y;
         double dist = Math.sqrt(dx * dx + dy * dy);
         return dist;
     }
 
     private Point getClosestPoint(Point target, Point candidates[])
     {
         double minDist = -1.0;
         Point winner = null;
         for (int i = 0; i < candidates.length; i++)
         {
             double distance = pointDistance(target, candidates[i]);
             if (minDist == -1.0 || distance < minDist)
             {
                 minDist = distance;
                 winner = candidates[i];
             }
         }
 
         return winner;
     }
 
     public Point snapPoint(Point modelPoint)
     {
         return snapPointEx(modelPoint, false, 0);
     }
 
     // if bSnapForPog is true, it will return snap locations where a pog of
     // the sent in size could snap to. Note this is not the same as ANY
     // snap points, cause you don't want your pogs snapping to the
     // vertex of a hex.
     // pogSize is ignored if bSnapForPog is false. And it's expected to be the
     // pog's size in model coordinate pixels.
     public Point snapPointEx(Point modelPointIn, boolean bSnapForPog, int pogSize)
     {
         Point modelPoint = new Point(modelPointIn);
         if (m_bHexMode)
         {
             if (bSnapForPog)
             {
                 // we're snapping for a pog. We've been sent the upper left corner of that
                 // pog. We need to know it's center.
                 modelPoint.x += pogSize / 2;
                 modelPoint.y += pogSize / 2;
             }
 
             // in hex mode, we have to snap to any of the vertices of a hex,
             // plus the center. How annoying is that, eh?
 
             // start with the grid snap location for the x coordinate
             int x = getGridSnap(modelPoint.x);
 
             // from that, get the grid location.
             int gridX = x / BASE_SQUARE_SIZE;
 
             // note that items in the odd columns are half a grid square down
             int offsetY = 0;
             if (isOffsetColumn(gridX))
             {
                 offsetY = BASE_SQUARE_SIZE / 2;
             }
 
             // now work out which "grid" (hex, really) the y value is in
             int y = getGridSnap(modelPoint.y - offsetY);
 
             // add back the offset
             y += offsetY;
 
             // add in the x offset needed to put it on the corner of the hex
             x += m_hexImageOffsets[0] / 2; // [0] is the model coordinate size
 
             // let's number the hexagon points 0 through 5. Let's number them
             // clockwise starting from the upper left one. What we have done so
             // far is snap to the nearest point 0. That's not good enough.
             // There are 3 hexagon points adjacent to a point 0 that are not
             // other hexagon point 0's. And we might be closer to one of them
             // than to the point we just snapped to.
             // so we now have 4 "candidates" for nearest point. The point 0 we just
             // found, and the other three points nearby. Those other three points
             // will be:
             //
             // --Our hex's point 1
             // --Our hex's point 5
             // --Our upstairs neighbor's point 5
             //
             // In addition to that, there are 3 hex centers we need to check:
             // --Our hex center
             // --Our upstairs neighbor's hex center
             // -- Our neighbor to the left's hex center
 
             Point closest = null;
 
             if (bSnapForPog)
             {
                 // we're snapping to valid pog locations. We have been sent the
                 // upp left corner of the graphic. We converted that to the center already.
                 // now, we need to stick that to either vertices or hex centers, depending
                 // on the size of the pog. If it's a size 1 (64 px_ pog, we snap to centers only.
                 // if it's size 2, we snap to vertices only. If size 3, back to centers. etc.
                 int face = pogSize / BASE_SQUARE_SIZE;
                 if (face % 2 == 1)
                 {
                     // odd faces snap to centers
                     Point candidates[] = new Point[3];
 
                     Point point1 = new Point(x + BASE_SQUARE_SIZE - m_hexImageOffsets[0], y); // Our
                     // hex's
                     // point
                     // 1,
                     // for
                     // use
                     // in
                     // calculating
                     // the
                     // center
 
                     candidates[0] = new Point(x + (point1.x - x) / 2, y + BASE_SQUARE_SIZE / 2); // Our
                     // hex
                     // center
                     candidates[1] = new Point(candidates[0].x, candidates[0].y - BASE_SQUARE_SIZE); // Our
                     // upstairs
                     // neighbor's
                     // center
                     candidates[2] = new Point(candidates[0].x - BASE_SQUARE_SIZE, candidates[0].y - BASE_SQUARE_SIZE
                         / 2); // Our upstairs neighbor's center
 
                     closest = getClosestPoint(modelPoint, candidates);
                 }
                 else
                 {
                     // even faces snap to vertices
                     Point candidates[] = new Point[4];
                     candidates[0] = new Point(x, y); // Our hex's point 0
                     candidates[1] = new Point(x + BASE_SQUARE_SIZE - m_hexImageOffsets[0], y); // Our
                     // hex's
                     // point
                     // 1
                     candidates[2] = new Point(x - m_hexImageOffsets[0], y + BASE_SQUARE_SIZE / 2); // Our
                     // hex's
                     // point
                     // 5
                     candidates[3] = new Point(candidates[2].x, candidates[2].y - BASE_SQUARE_SIZE); // Our
                     // upstairs
                     // neighbor's
                     // point
                     // 5
 
                     closest = getClosestPoint(modelPoint, candidates);
                 }
 
                 if (closest != null)
                 {
                     // offset the values for the pog size
                     closest.x -= pogSize / 2;
                     closest.y -= pogSize / 2;
                 }
             }
             else
             {
                 // we're snapping to any vertex
                 Point candidates[] = new Point[7];
                 candidates[0] = new Point(x, y); // Our hex's point 0
                 candidates[1] = new Point(x + BASE_SQUARE_SIZE - m_hexImageOffsets[0], y); // Our
                 // hex's
                 // point
                 // 1
                 candidates[2] = new Point(x - m_hexImageOffsets[0], y + BASE_SQUARE_SIZE / 2); // Our
                 // hex's
                 // point
                 // 5
                 candidates[3] = new Point(candidates[2].x, candidates[2].y - BASE_SQUARE_SIZE); // Our
                 // upstairs
                 // neighbor's
                 // point
                 // 5
                 candidates[4] = new Point(candidates[0].x + (candidates[1].x - candidates[0].x) / 2, y
                     + BASE_SQUARE_SIZE / 2); // Our hex center
                 candidates[5] = new Point(candidates[4].x, candidates[4].y - BASE_SQUARE_SIZE); // Our
                 // upstairs
                 // neighbor's
                 // center
                 candidates[6] = new Point(candidates[4].x - BASE_SQUARE_SIZE, candidates[4].y - BASE_SQUARE_SIZE / 2); // Our
                 // upstairs
                 // neighbor's
                 // center
 
                 closest = getClosestPoint(modelPoint, candidates);
             }
 
             if (closest == null)
             {
                 // uh... if we're here something went wrong
                 // defensive coding, just return that nearest Point 0
                 System.out.println("Error snapping to point");
                 return new Point(x, y);
             }
             return closest;
         }
 
         // snapping for a pog or not is irrelevant in square mode.
         int x = getGridSnap(modelPoint.x);
         int y = getGridSnap(modelPoint.y);
         return new Point(x, y);
     }
 
     /*
      * // takes VIEW coords private int getSnapX(int x) { return modelToView(getSnap(viewToModel(x, 0).x), 0).x; } //
      * takes VIEW coords private int getSnapY(int y) { return modelToView(getSnap(viewToModel(0, y).y), 0).y; }
      */
 
     public void updateLocation(MouseEvent e)
     {
         if (e.getSource() == getActivePogsArea())
         {
             Point canvasView = pogsViewToCanvasView(e.getX(), e.getY());
             m_dragX = canvasView.x;
             m_dragY = canvasView.y;
         }
         else
         {
             m_dragX = e.getX();
             m_dragY = e.getY();
 
             m_currentMouseX = e.getX();
             m_currentMouseY = e.getY();
         }
     }
 
     public void setToolMode(int mode)
     {
         m_toolMode = mode;
     }
 
     public void centerZoom(int delta)
     {
         // can't do this at all if we're dragging
         if (m_bLDragging || m_bRDragging || m_bPogBeingDragged)
         {
             return;
         }
         // note the model location of the center
         Point modelCenter = viewToModel(getWidth() / 2, getHeight() / 2);
 
         // do the zoom
         setZoom(m_zoom + delta);
 
         // note the view location of the model center
         Point viewCenter = modelToView(modelCenter.x, modelCenter.y);
 
         // note the present actual center
         int presentCenterX = getWidth() / 2;
         int presentCenterY = getHeight() / 2;
 
         // set up the scroll to enforce the center being where it's supposed to be
         int scrX = getActiveMap().getScrollX() - (presentCenterX - viewCenter.x);
         int scrY = getActiveMap().getScrollY() - (presentCenterY - viewCenter.y);
         setPrimaryScroll(getActiveMap(), scrX, scrY);
     }
 
     /** *********************************************************** */
     // KeyListener events
     /** *********************************************************** */
     public void addItemListener(ItemListener l)
     {
         listenerList.add(ItemListener.class, l);
     }
 
     public void removeItemListener(ItemListener l)
     {
         listenerList.remove(ItemListener.class, l);
     }
 
     public Object[] getSelectedObjects()
     {
         if (isSelected() == false)
         {
             return null;
         }
         Object[] selectedObjects = new Object[1];
         selectedObjects[0] = getText();
         return selectedObjects;
     }
 
     public void keyTyped(KeyEvent e)
     {
     }
 
     public void keyPressed(KeyEvent e)
     {
         int code = e.getKeyCode();
         switch (code)
         {
             case KeyEvent.VK_SPACE:
                 if (!m_bMouseOnView || getActiveTool().isBeingUsed())
                 {
                     // no pointing if the mouse is outside the view area, or the active tool is
                     // being used.
                     return;
                 }
 
                 // we're only interested in doing this if they aren't already
                 // holding the space key.
                 if (m_bSpaceKeyDown == false)
                 {
                     m_bSpaceKeyDown = true;
 
                     pointAt(m_mouseModelFloat);
                     e.consume();
                 }
             break;
             case KeyEvent.VK_SHIFT:
                 m_bShiftKeyDown = true;
                 repaint();
                 e.consume();
             break;
 
             case KeyEvent.VK_CONTROL:
                 m_bControlKeyDown = true;
                 checkSnap();
                 e.consume();
             break;
 
             case KeyEvent.VK_MINUS:
             case KeyEvent.VK_SUBTRACT:
                 centerZoom(1);
                 e.consume();
             break;
 
             case KeyEvent.VK_EQUALS:
             case KeyEvent.VK_ADD:
                 centerZoom(-1);
                 e.consume();
             break;
 
             case KeyEvent.VK_ENTER:
                 // shift focus to the chat area
                 m_gametableFrame.m_textEntry.requestFocus();
             break;
             case KeyEvent.VK_SLASH:
                 m_gametableFrame.m_textEntry.setText("/");
                 m_gametableFrame.m_textEntry.requestFocus();
             break;
         }
         // repaint();
     }
 
     private boolean isPointing()
     {
         Player me = m_gametableFrame.getMePlayer();
         return me.isPointing();
     }
 
     /**
      * 
      */
     private void pointAt(Point pointLocation)
     {
         Player me = m_gametableFrame.getMePlayer();
 
         if (pointLocation == null)
         {
             me.setPointing(false);
             m_gametableFrame.push(PacketManager.makePointPacket(m_gametableFrame.m_myPlayerIdx, 0, 0, false));
             repaint();
             return;
         }
 
         me.setPointing(true);
         me.setPoint(pointLocation);
 
         m_gametableFrame.push(PacketManager.makePointPacket(m_gametableFrame.m_myPlayerIdx, me.getPoint().x, me
             .getPoint().y, true));
 
         setToolMode(TOOL_MODE_POINT);
         setToolCursor(-1);
         getActivePogsArea().updateToolState();
 
         repaint();
     }
 
     public void keyReleased(KeyEvent e)
     {
         switch (e.getKeyCode())
         {
             case KeyEvent.VK_SPACE:
                 m_bSpaceKeyDown = false;
                 pointAt(null);
                 e.consume();
             break;
             case KeyEvent.VK_SHIFT:
                 m_bShiftKeyDown = false;
                 repaint();
                 e.consume();
             break;
             case KeyEvent.VK_CONTROL:
                 m_bControlKeyDown = false;
                 checkSnap();
                 e.consume();
             break;
         }
     }
 
     /** *********************************************************** */
     // drawing
     /** *********************************************************** */
     public void update(Graphics g)
     {
         paint(g);
     }
 
     public void paint(Graphics g)
     {
         g.setFont(Font.decode("sans-12"));
         if (false)
         {
             final Map HINTS = new HashMap();
             HINTS.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
             HINTS.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
             HINTS.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
             HINTS.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
             HINTS.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
             HINTS.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
             HINTS.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
             HINTS.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
             HINTS.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
             ((Graphics2D)g).addRenderingHints(HINTS);
         }
         else
         {
             final Map HINTS = new HashMap();
             HINTS.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
             HINTS.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
             HINTS.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
             HINTS.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
             HINTS.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
             HINTS.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
             HINTS.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
             HINTS.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
             HINTS.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
             ((Graphics2D)g).addRenderingHints(HINTS);
         }
 
         g.setColor(Color.WHITE);
         g.fillRect(0, 0, getWidth(), getHeight());
         g.translate(-getActiveMap().getScrollX(), -getActiveMap().getScrollY());
 
         drawMatte(g, getActiveMap().getScrollX(), getActiveMap().getScrollY(), getWidth(), getHeight());
 
         // draw all the underlays here
         for (int i = 0; i < getActiveMap().getNumPogs(); i++)
         {
             Pog pog = getActiveMap().getPogAt(i);
             if (pog.isUnderlay())
             {
                 pog.drawToCanvas(g);
             }
         }
 
         // if they're dragging an underlay, draw it here
         // there could be a pog drag in progress
         if (m_bPogBeingDragged)
         {
             if (getActivePogsArea().m_selectedPog.isUnderlay())
             {
                 getActivePogsArea().m_selectedPog.drawToCanvas(g);
             }
         }
 
         // there could be an internal pog move being done. we draw it again here to ensure
         // it's on top of the heap
         if (m_pogBeingDragged != null)
         {
             if (m_pogBeingDragged.isUnderlay())
             {
                 m_pogBeingDragged.drawToCanvas(g);
             }
         }
 
         drawLines(g, getActiveMap().getScrollX(), getActiveMap().getScrollY(), getWidth(), getHeight());
 
         // lines
         for (int i = 0; i < getActiveMap().getNumLines(); i++)
         {
             LineSegment ls = getActiveMap().getLineAt(i);
 
             // LineSegments police themselves, performance wise. If they won't touch the current
             // viewport, they don't draw
             ls.draw(g, this);
         }
 
         // pogs
         for (int i = 0; i < getActiveMap().getNumPogs(); i++)
         {
             Pog pog = getActiveMap().getPogAt(i);
             if (!pog.isUnderlay())
             {
                 pog.drawToCanvas(g);
             }
         }
 
         // there could be a pog drag in progress
         if (m_bPogBeingDragged)
         {
             if (!getActivePogsArea().m_selectedPog.isUnderlay())
             {
                 getActivePogsArea().m_selectedPog.drawToCanvas(g);
             }
         }
 
         // draw the cursor overlays
         for (int i = 0; i < m_gametableFrame.m_players.size(); i++)
         {
             Player plr = (Player)m_gametableFrame.m_players.get(i);
             if (plr.isPointing())
             {
                 // draw this player's point cursor
                 Point pointingAt = modelToDraw(plr.getPoint().x, plr.getPoint().y);
                 int idx = i % m_pointCursorImages.length;
                 Image hand = m_pointCursorImages[idx];
 
                 // 5px offset to align with mouse pointer
                 g.drawImage(hand, pointingAt.x, pointingAt.y - 5, null);
             }
         }
 
         // mousing around
         if (m_bMouseOnView)
         {
             Pog mouseOverPog = getActiveMap().getPogAt(m_mouseModelFloat);
             if (m_bShiftKeyDown)
             {
                 // this shift key is down. Show all pog data
                 for (int i = 0; i < getActiveMap().getNumPogs(); i++)
                 {
                     Pog pog = getActiveMap().getPogAt(i);
                     if (pog != mouseOverPog)
                     {
                         pog.drawDataStringToCanvas(g, false);
                     }
                 }
             }
 
             if (mouseOverPog != null)
             {
                 mouseOverPog.drawDataStringToCanvas(g, true);
             }
         }
         getActiveTool().paint(g);
         g.translate(getActiveMap().getScrollX(), getActiveMap().getScrollY());
     }
 
     // topLeftX and topLeftY are the coordinates of where the
     // top left of the map area is in whatever coordinate system g is set up to be
     public void drawMatte(Graphics g, int topLeftX, int topLeftY, int width, int height)
     {
         // background image
         int qx = Math.abs(topLeftX) / m_mapBk.getWidth(null);
         if (topLeftX < 0)
         {
             qx++;
             qx = -qx;
         }
 
         int qy = Math.abs(topLeftY) / m_mapBk.getHeight(null);
         if (topLeftY < 0)
         {
             qy++;
             qy = -qy;
         }
 
         int linesXOffset = qx * m_mapBk.getWidth(null);
         int linesYOffset = qy * m_mapBk.getHeight(null);
         int vLines = width / m_mapBk.getWidth(null) + 2;
         int hLines = height / m_mapBk.getHeight(null) + 2;
 
         for (int i = 0; i < vLines; i++)
         {
             for (int j = 0; j < hLines; j++)
             {
                 g.drawImage(m_mapBk, i * m_mapBk.getWidth(null) + linesXOffset, j * m_mapBk.getHeight(null)
                     + linesYOffset, null);
             }
         }
     }
 
     public void drawLines(Graphics g, int topLeftX, int topLeftY, int width, int height)
     {
         if (m_zoom == 4)
         {
             // we don't draw lines at the furthest zoom level
             return;
         }
         // This code works out which is the first square to draw on the visible
         // portion of the map, and how many to draw
 
         // we are "tiling" an image across the visible area. In the case of square mode,
         // we do this just by drawing lines. For hexes we have an actual image to tile.
         // A trick here we have to deal with is that hexes are not horizontally interchangeable
         // across one unit size. That is to say: If you shift a hex map over 1 hex width to the
         // left or right, it will not look the same as it used to. Because the hexes in row N
         // are 1/2 a hex higher than the nexes in row N-1 and row N+1. Because of this, when in hex
         // mode,
         // we have to make our "tiling square size" twice as wide.
 
         int tilingSquareX = m_squareSize;
         int tilingSquareY = m_squareSize;
 
         if (m_bHexMode)
         {
             tilingSquareX *= 2;
         }
 
         int qx = Math.abs(topLeftX) / tilingSquareX;
         if (topLeftX < 0)
         {
             qx++;
             qx = -qx;
         }
 
         int qy = Math.abs(topLeftY) / tilingSquareY;
         if (topLeftY < 0)
         {
             qy++;
             qy = -qy;
         }
 
         int linesXOffset = qx * tilingSquareX;
         int linesYOffset = qy * tilingSquareY;
         int vLines = width / tilingSquareX + 2;
         int hLines = height / tilingSquareY + 2;
 
         if (m_bHexMode)
         {
             // draw a hex grid
             Image toTile = m_hexImages[m_zoom];
 
             // this offsets the hexes to be "centered" in the square grid that would
             // be there if we were in square mode (Doing things this way means that the
             // x-position treatment of pogs doesn't have to change while in hex mode.
             int offsetX = -m_hexImageOffsets[m_zoom] / 2;
 
             // each tiling of hex images is 4 hexes high. so we incrememt by 4
             for (int j = 0; j < hLines; j += 4)
             {
                 // every "tiling" of the hex map draws 4 vertical rows of hexes
                 // that works out to be 2 tileSquare sizes (cause each tileSquare width is
                 // 2 columns of hexes.
                 for (int i = 0; i < vLines; i += 2)
                 {
                     // the x value:
                     // starts at the "linesXOffset" calculated at the top of this routine.
                     // that represents the first vertical column of hexes visible on screen.
                     // add to that i*m_squareSize, to offset horizontally as we traverse the loop.
                     // then add our little offsetX, whose purpose is described in it's declaration.
                     int x = linesXOffset + i * tilingSquareX + offsetX;
 
                     // the y location is much the same, except we need no x offset nudge.
                     int y = linesYOffset + j * tilingSquareY;
                     g.drawImage(toTile, x, y, this);
                 }
             }
 
         }
         else
         {
             // draw a square grid
 
             g.setColor(Color.GRAY);
 
             if (m_zoom < 4)
             {
                 for (int i = 0; i < vLines; i++)
                 {
                     g.drawLine(i * m_squareSize + linesXOffset, topLeftY, i * m_squareSize + linesXOffset, height
                         + topLeftY);
                 }
                 for (int i = 0; i < hLines; i++)
                 {
                     g.drawLine(topLeftX, i * m_squareSize + linesYOffset, width + topLeftX, i * m_squareSize
                         + linesYOffset);
                 }
             }
         }
     }
 
     public static void drawDottedRect(Graphics g, int x, int y, int width, int height)
     {
         Graphics2D g2d = (Graphics2D)g;
         Stroke oldStroke = g2d.getStroke();
         g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] {
             2f
         }, 0f));
 
         if (width < 0)
         {
             x += width;
             width = -width;
         }
         if (height < 0)
         {
             y += height;
             height = -height;
         }
         g.drawRect(x, y, width, height);
         g2d.setStroke(oldStroke);
     }
 
     public static void drawDottedLine(Graphics g, int x, int y, int x2, int y2)
     {
         Graphics2D g2d = (Graphics2D)g;
         Stroke oldStroke = g2d.getStroke();
         g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] {
             2f
         }, 0f));
         g.drawLine(x, y, x2, y2);
         g2d.setStroke(oldStroke);
     }
 
     /** *********************************************************** */
     // component listening
     /** *********************************************************** */
     public void componentResized(ComponentEvent e)
     {
     }
 
     public void componentMoved(ComponentEvent e)
     {
     }
 
     public void componentShown(ComponentEvent e)
     {
     }
 
     public void componentHidden(ComponentEvent e)
     {
     }
 }
