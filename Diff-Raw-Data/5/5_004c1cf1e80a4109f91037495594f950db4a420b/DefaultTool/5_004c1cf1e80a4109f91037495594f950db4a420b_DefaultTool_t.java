 /* The MIT License
  * 
  * Copyright (c) 2005 David Rice, Trevor Croft
  * 
  * Permission is hereby granted, free of charge, to any person 
  * obtaining a copy of this software and associated documentation files 
  * (the "Software"), to deal in the Software without restriction, 
  * including without limitation the rights to use, copy, modify, merge, 
  * publish, distribute, sublicense, and/or sell copies of the Software, 
  * and to permit persons to whom the Software is furnished to do so, 
  * subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be 
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
  * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN 
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
  * SOFTWARE.
  */
 package net.rptools.maptool.client.tool;
 
 import java.awt.AlphaComposite;
 import java.awt.BasicStroke;
 import java.awt.Composite;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.Stroke;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.geom.Area;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPopupMenu;
 import javax.swing.JSeparator;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 
 import net.rptools.lib.swing.SwingUtil;
 import net.rptools.maptool.client.AppActions;
 import net.rptools.maptool.client.AppState;
 import net.rptools.maptool.client.AppStyle;
 import net.rptools.maptool.client.AppUtil;
 import net.rptools.maptool.client.CellPoint;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.ScreenPoint;
 import net.rptools.maptool.client.ZonePoint;
 import net.rptools.maptool.client.ui.Tool;
 import net.rptools.maptool.client.ui.token.LightDialog;
 import net.rptools.maptool.client.ui.token.RadiusLightTokenTemplate;
 import net.rptools.maptool.client.ui.token.TokenStates;
 import net.rptools.maptool.client.ui.token.TokenTemplate;
 import net.rptools.maptool.client.ui.zone.ZoneOverlay;
 import net.rptools.maptool.client.ui.zone.ZoneRenderer;
 import net.rptools.maptool.language.I18N;
 import net.rptools.maptool.model.GUID;
 import net.rptools.maptool.model.Player;
 import net.rptools.maptool.model.Pointer;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.TokenSize;
 import net.rptools.maptool.model.Zone;
 
 /**
  */
 public abstract class DefaultTool extends Tool implements MouseListener, MouseMotionListener, ZoneOverlay, MouseWheelListener {
     private static final long serialVersionUID = 3258411729238372921L;
 
     private boolean isShowingPointer; 
     private boolean isDraggingMap;
     private boolean isDraggingToken;
     private boolean isNewTokenSelected;
     private boolean isDrawingSelectionBox;
     private Rectangle selectionBoundBox;
 	private int dragStartX;
 	private int dragStartY;
 	
 	private int mouseX;
 	private int mouseY;
 	
 	private Token tokenBeingDragged;
 	private Token tokenUnderMouse;
 	
     // Offset from token's X,Y when dragging. Values are in cell coordinates.
     private int dragOffsetX;
     private int dragOffsetY;
 
     // This is to manage overflowing of map move events (keep things snappy)
     private long lastMoveRedraw;
     private int mapDX, mapDY;
     private static final int REDRAW_DELAY = 25; // millis
 
     protected ZoneRenderer renderer;
     
     @Override
     protected void attachTo(ZoneRenderer renderer) {
     	this.renderer = renderer;
     }
     
     @Override
     protected void detachFrom(ZoneRenderer renderer) {
     	this.renderer = null;
     }
     
     ////
 	// Mouse
 	
 	public void mousePressed(MouseEvent e) {
  
 		ZoneRenderer renderer = (ZoneRenderer) e.getSource();
 
 		// Potential map dragging
 		dragStartX = e.getX();
 		dragStartY = e.getY();
 
 		// SELECTION
 		// Token
 		Token token = renderer.getTokenAt (e.getX(), e.getY());
 		if (token != null && !isDraggingToken && !isDraggingMap) {
 
 			// Permission
 			if (!AppUtil.playerOwnsToken(token)) {
 				if (!SwingUtil.isShiftDown(e)) {
 					renderer.clearSelectedTokens();
 				}
 				return;
 			}
 			
 			// Don't select if it's already being moved by someone
 			isNewTokenSelected = false;
 			if (!renderer.isTokenMoving(token)) {
 				if (!renderer.getSelectedTokenSet().contains(token.getId()) && 
 						!SwingUtil.isShiftDown(e)) {
 					isNewTokenSelected = true;
                     renderer.clearSelectedTokens();
 				}
 				renderer.selectToken(token.getId());
         
 		        // Dragging offset for currently selected token
 		        ZonePoint pos = ZonePoint.fromScreenPoint(renderer, e.getX(), e.getY());
 		        dragOffsetX = pos.x - token.getX();
 		        dragOffsetY = pos.y - token.getY();
 			}
 		} else {
 
 			if (!SwingUtil.isShiftDown(e)) {
 				renderer.clearSelectedTokens();
 			}
 			
 			if (SwingUtilities.isLeftMouseButton(e)) {
 				// Starting a bound box selection
 				isDrawingSelectionBox = true;
 				selectionBoundBox = new Rectangle(e.getX(), e.getY(), 0, 0);
 			}
 			
 		}
 		
         // Waypoints
         if (SwingUtilities.isRightMouseButton(e) && isDraggingToken) {
             
             ZonePoint zp = ZonePoint.fromScreenPoint(renderer, e.getX(), e.getY());
             
             renderer.toggleMoveSelectionSetWaypoint(tokenBeingDragged.getId(), zp);
             
             MapTool.serverCommand().toggleTokenMoveWaypoint(renderer.getZone().getId(), tokenBeingDragged.getId(), zp.x, zp.y);
             return;
         }
         
 	}
 	
 	public void mouseReleased(MouseEvent e) {
 		
         ZoneRenderer renderer = (ZoneRenderer) e.getSource();
         SwingUtil.showPointer(renderer);
         
         // SELECTION BOUND BOX
         if (isDrawingSelectionBox) {
         	isDrawingSelectionBox = false;
 
         	if (!SwingUtil.isShiftDown(e)) {
         		renderer.clearSelectedTokens();
         	}
         	
         	renderer.selectTokens(selectionBoundBox);
         	
         	selectionBoundBox = null;
         	renderer.repaint();
         	return;
         }
 
 		// POPUP MENU
         if (SwingUtilities.isRightMouseButton(e)) {
         	
         	if (!isDraggingMap && !isDraggingToken && renderer.getSelectedTokenSet().size() > 0) {
         		showTokenContextMenu(e);
         	}
             
             if (isDraggingMap) {
                 isDraggingMap = false;
                 if (AppState.isPlayerViewLinked()) {
         			MapTool.serverCommand().enforceZoneView(renderer.getZone().getId(),
         					renderer.getViewOffsetX(), renderer.getViewOffsetY(),
         					renderer.getScaleIndex());
                 }
             }
         	return;
         }
 
 		// DRAG TOKEN COMPLETE
 		if (isDraggingToken) {
             renderer.commitMoveSelectionSet(tokenBeingDragged.getId()); // TODO: figure out a better way
 
             isDraggingToken = false;
 		}
 		
         // SELECT SINGLE TOKEN
         Token token = renderer.getTokenAt(e.getX(), e.getY());
         if (token != null && SwingUtilities.isLeftMouseButton(e) && !isDraggingToken && !SwingUtil.isShiftDown(e)) {
 
     		// Only if it isn't already being moved
 			if (!renderer.isTokenMoving(token)) {
 	        	renderer.clearSelectedTokens();
 	        	renderer.selectToken(token.getId());
 			}
         }
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
 	 */
 	public void mouseClicked(MouseEvent e) {
 		// TODO Auto-generated method stub
 	
 	}	
 	/* (non-Javadoc)
 	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
 	 */
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 	/* (non-Javadoc)
 	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
 	 */
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 	
 	////
 	// MouseMotion
 	/* (non-Javadoc)
 	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
 	 */
 	public void mouseMoved(MouseEvent e) {
 		
 		mouseX = e.getX();
 		mouseY = e.getY();
 		
 		ZoneRenderer renderer = (ZoneRenderer) e.getSource();
 		
 		CellPoint p = renderer.getCellAt(new ScreenPoint(e.getX(), e.getY()));
 		if (p != null) {	
 			MapTool.getFrame().setStatusMessage("Cell: " + p.x + ", " + p.y);
 		} else {
 		    MapTool.getFrame().setStatusMessage("");
         }
 
 		tokenUnderMouse = renderer.getTokenAt(mouseX, mouseY);
 		renderer.setMouseOver(tokenUnderMouse);
 	}
 	
 	public void mouseDragged(MouseEvent e) {
 
 		ZoneRenderer renderer = (ZoneRenderer) e.getSource();
 
 		CellPoint cellUnderMouse = renderer.getCellAt(new ScreenPoint(e.getX(), e.getY()));
 		if (cellUnderMouse != null) {
 			MapTool.getFrame().setStatusMessage("Cell: " + cellUnderMouse.x + ", " + cellUnderMouse.y);
 		}
 		
 		if (SwingUtilities.isLeftMouseButton(e)) {
 			
 			if (isDrawingSelectionBox) {
 				
 				int x1 = dragStartX;
 				int y1 = dragStartY;
 				
 				int x2 = e.getX();
 				int y2 = e.getY();
 				
 				selectionBoundBox.x = Math.min(x1, x2);
 				selectionBoundBox.y = Math.min(y1, y2);
 				selectionBoundBox.width = Math.abs(x1 - x2);
 				selectionBoundBox.height = Math.abs(y1 - y2);
 				
 				renderer.repaint();
 				return;
 			}
 			
 			if (tokenUnderMouse == null || !renderer.getSelectedTokenSet().contains(tokenUnderMouse.getId())) {
 				return;
 			}
 
 			if (!isDraggingToken && renderer.isTokenMoving(tokenUnderMouse)) {
                 return;
             }
 			
 			if (isNewTokenSelected) {
 				renderer.clearSelectedTokens();
 				renderer.selectToken(tokenUnderMouse.getId());
 			}
 			isNewTokenSelected = false;
 			
 			// Might be dragging a token
 			String playerId = MapTool.getPlayer().getName();
 			Set<GUID> selectedTokenSet = renderer.getSelectedTokenSet();
 			if (selectedTokenSet.size() > 0) {
 				
 				// Make sure we can do this
 				// LATER: This might be able to be removed since you can't 
 				// select an unowned token, check later
 				if (!MapTool.getPlayer().isGM() && MapTool.getServerPolicy().useStrictTokenManagement()) {
 					for (GUID tokenGUID : selectedTokenSet) {
 						Token token = renderer.getZone().getToken(tokenGUID);
 						if (!token.isOwner(playerId)) {
 							return;
 						}
 					}
 				}
 				
 				Point origin = new Point(tokenUnderMouse.getX(), tokenUnderMouse.getY());
 				
 				origin.translate(dragOffsetX, dragOffsetY);
         
 				int x = e.getX();
 				int y = e.getY();
 				
 				ZonePoint zonePoint = ZonePoint.fromScreenPoint(renderer, x, y);
 				if (!isDraggingToken) {
 					tokenBeingDragged = tokenUnderMouse;
 					
 					renderer.addMoveSelectionSet(MapTool.getPlayer().getName(), tokenBeingDragged.getId(), selectedTokenSet, false);
 					MapTool.serverCommand().startTokenMove(MapTool.getPlayer().getName(), renderer.getZone().getId(), tokenBeingDragged.getId(), selectedTokenSet);
 				} else {
 					
 					if (tokenBeingDragged.isSnapToGrid()) {
 
                         renderer.constrainToCell(zonePoint);
 					} else {
 					    zonePoint.translate(-dragOffsetX, -dragOffsetY);
                     }
 					
 					// Make sure it's a valid move
 					if (!validateMove(tokenBeingDragged, renderer.getSelectedTokenSet(), zonePoint)) {
 						return;
 					}
 
 					renderer.updateMoveSelectionSet(tokenBeingDragged.getId(), zonePoint);
 					MapTool.serverCommand().updateTokenMove(renderer.getZone().getId(), tokenBeingDragged.getId(), zonePoint.x, zonePoint.y);
 				}
 				isDraggingToken = true;
                 SwingUtil.hidePointer(renderer);
 			}
 			
 			return;
 		}
 		
 		// MAP MOVEMENT
 		if (SwingUtilities.isRightMouseButton(e)) {
 
 			isDraggingMap = true;
 			mapDX += e.getX() - dragStartX;
 			mapDY += e.getY() - dragStartY;
 	
 			dragStartX = e.getX();
 			dragStartY = e.getY();
 			
             long now = System.currentTimeMillis();
             if (now - lastMoveRedraw > REDRAW_DELAY) {
                 // TODO: does it matter to capture the last map move in the series ?
                 // TODO: This should probably be genericized an put into ZoneRenderer
                 // to prevent over zealous repainting
                 renderer.moveViewBy(mapDX, mapDY);
                 mapDX = 0;
                 mapDY = 0;
                 lastMoveRedraw = now;
             }
 		}
 	}	
 	
 	private static final int FOW_EDGE_FUDGE = 10;
 	private boolean validateMove(Token leadToken, Set<GUID> tokenSet, ZonePoint point) {
 
 		Zone zone = renderer.getZone();
 		if (MapTool.getPlayer().isGM()) {
 			return true;
 		}
 		
 		if (zone.hasFog()) {
 			
 			// Check that the new position for each token is within the exposed area
 			Area fow = zone.getExposedArea();
 			if (fow == null) {
 				return true;
 			}
 
 			int deltaX = point.x - leadToken.getX();
 			int deltaY = point.y - leadToken.getY();
             Rectangle bounds = new Rectangle();
 			for (GUID tokenGUID : tokenSet) {
 				Token token = zone.getToken(tokenGUID);
 				if (token == null) {
 					continue;
 				}
 				
 				int x = token.getX() + deltaX;
 				int y = token.getY() + deltaY;
 	            int width = TokenSize.getWidth(token, zone.getGridSize());
 	            int height = TokenSize.getHeight(token, zone.getGridSize());
 
 	            int fudgeW = Math.min(FOW_EDGE_FUDGE, width/4);
 	            int fudgeH = Math.min(FOW_EDGE_FUDGE, height/4);
 	            bounds.setBounds(x+fudgeW, y+fudgeH, width-fudgeW*2, height-fudgeH*2);
 	            
 	            if (!fow.contains(bounds)) {
 	            	return false;
 	            }
 			}
 		}
 		
 		return true;
 	}
 	
 	/* (non-Javadoc)
 	 * @see net.rptools.maptool.client.Tool#getKeyActionMap()
 	 */
 	protected Map<KeyStroke, Action> getKeyActionMap() {
 		return new HashMap<KeyStroke, Action>() {
 			{
 				put(KeyStroke.getKeyStroke("control C"), AppActions.COPY_TOKENS);
 				put(KeyStroke.getKeyStroke("control V"), AppActions.PASTE_TOKENS);
 				
 				// TODO: Optimize this by making it non anonymous
 				put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), new AbstractAction() {
 				
 					public void actionPerformed(java.awt.event.ActionEvent e) {
 						
 						ZoneRenderer renderer = (ZoneRenderer) e.getSource();
 						
 						Set<GUID> selectedTokenSet = renderer.getSelectedTokenSet();
 						
 						for (GUID tokenGUID : selectedTokenSet) {
 							
 							Token token = renderer.getZone().getToken(tokenGUID);
 							
 							if (AppUtil.playerOwnsToken(token)) {
 	                            renderer.getZone().removeToken(tokenGUID);
 	                            MapTool.serverCommand().removeToken(renderer.getZone().getId(), tokenGUID);
 							}
 						}
 						
 						renderer.clearSelectedTokens();
 					}
 				});
 				
 				put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true), new AbstractAction() {
 					public void actionPerformed(ActionEvent e) {
 						isShowingPointer = false;
 						MapTool.serverCommand().hidePointer(MapTool.getPlayer().getName());
 					}
 				});
 				put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false), new AbstractAction() {
 					public void actionPerformed(ActionEvent e) {
 						if (isShowingPointer || renderer == null) {
 							return;
 						}
 
 						isShowingPointer = true;
 						
 						ZonePoint p = ZonePoint.fromScreenPoint(renderer, mouseX, mouseY);
 						Pointer pointer = new Pointer(renderer.getZone(), p.x, p.y, 0);
 						
 						MapTool.serverCommand().showPointer(MapTool.getPlayer().getName(), pointer);
 					}
 				});
 			}
 		};
 	}
 	
 	//// 
 	// ZoneOverlay
 	/* (non-Javadoc)
 	 * @see net.rptools.maptool.client.ZoneOverlay#paintOverlay(net.rptools.maptool.client.ZoneRenderer, java.awt.Graphics2D)
 	 */
 	public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {
 		
 		if (selectionBoundBox != null) {
 			
 			Stroke stroke = g.getStroke();
 			g.setStroke(new BasicStroke(2));
 			
 			Composite composite = g.getComposite();
 			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, .25f));
 			g.setColor(AppStyle.selectionBoxFill);
 			g.fillRoundRect(selectionBoundBox.x, selectionBoundBox.y, selectionBoundBox.width, selectionBoundBox.height,10, 10);
 			g.setComposite(composite);
 			
 			g.setColor(AppStyle.selectionBoxOutline);
 			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 			g.drawRoundRect(selectionBoundBox.x, selectionBoundBox.y, selectionBoundBox.width, selectionBoundBox.height,10, 10);
 
 			g.setStroke(stroke);
 		}
 
 //		if (tokenUnderMouse != null && renderer.getSelectedTokenSet().contains(tokenUnderMouse)) {
 //
 //			Rectangle rect = renderer.getTokenBounds(tokenUnderMouse);
 //			
 //			g.setColor(Color.black);
 //			g.fillRect(rect.x + rect.width - 10, rect.y + rect.height - 10, 10, 10);
 //
 //			g.setColor(Color.white);
 //			g.fillRect(rect.x + rect.width - 8, rect.y + rect.height - 8, 8, 8);
 //		}
 	}
 	
 	////
 	// Mouse Wheel
 	public void mouseWheelMoved(MouseWheelEvent e) {
 
 		ZoneRenderer renderer = (ZoneRenderer) e.getSource();
 		if (e.getWheelRotation() > 0) {
 			
 			renderer.zoomOut(e.getX(), e.getY());
 		} else {
 			
 			renderer.zoomIn(e.getX(), e.getY());
 		}
         if (AppState.isPlayerViewLinked()) {
 			MapTool.serverCommand().enforceZoneView(renderer.getZone().getId(),
 					renderer.getViewOffsetX(), renderer.getViewOffsetY(),
 					renderer.getScaleIndex());
         }
 		
 	}	
 
 	////
 	// INTERNAL
 	
 	private void showTokenContextMenu(MouseEvent e) {
 
 		if (tokenUnderMouse == null) {
 			return;
 		}
 		
     	JPopupMenu popup = new JPopupMenu();
     	final ZoneRenderer renderer = (ZoneRenderer)e.getSource();
     	
     	boolean enabled = true;
     	if (!MapTool.getPlayer().isGM() && MapTool.getServerPolicy().useStrictTokenManagement()) {
     		for (GUID tokenGUID : renderer.getSelectedTokenSet()) {
     			Token token = renderer.getZone().getToken(tokenGUID);
     			
     			if (!token.isOwner(MapTool.getPlayer().getName())) {
     				enabled = false;
     				break;
     			}
     		}
     	}
     	
     	// SIZE
     	// TODO: Genericize the heck out of this.
     	JMenu sizeMenu = new JMenu("Size");
     	sizeMenu.setEnabled(enabled);
     	
     	JMenuItem freeSize = new JMenuItem("Free Size");
     	freeSize.setEnabled(false);
     	
         sizeMenu.add(freeSize);
         sizeMenu.addSeparator();
         
         for (TokenSize.Size size : TokenSize.Size.values()) {
             JMenuItem menuItem = new JCheckBoxMenuItem(new ChangeSizeAction(size.name(), size));
         	if (tokenUnderMouse.getSize() == size.value()) {
                 menuItem.setSelected(true);
         	}
         	
             sizeMenu.add(menuItem);
         }
         
         // Grid
         boolean snapToGrid = !tokenUnderMouse.isSnapToGrid();
         JCheckBoxMenuItem snapToGridMenuItem = new JCheckBoxMenuItem("placeholder", !snapToGrid); 
         snapToGridMenuItem.setAction(new SnapToGridAction(snapToGrid, renderer));
         snapToGridMenuItem.setEnabled(enabled);
 
         // Visibility
         JCheckBoxMenuItem visibilityMenuItem = new JCheckBoxMenuItem("Visible", tokenUnderMouse.isVisible());
        visibilityMenuItem.setEnabled(enabled);
         // TODO: Make this an action, not aic
        visibilityMenuItem.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent e) {
 
         		for (GUID guid : renderer.getSelectedTokenSet()) {
         			
         			Token token = renderer.getZone().getToken(guid);
         			if (token == null) {
         				continue;
         			}
         			
         			token.setVisible(((JCheckBoxMenuItem )e.getSource()).isSelected());
             		renderer.flush(token);
 
             		MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
         		}
 
         		renderer.repaint();
         	}
         });
         
         // Rename
         // TODO: Make this an action, not aic
     	JMenuItem renameMenuItem = new JMenuItem("Rename");
     	renameMenuItem.setEnabled(enabled);
         if (renderer.getSelectedTokenSet().size() == 1) {
 
         	renameMenuItem.addActionListener(new ActionListener() {
             	public void actionPerformed(ActionEvent e) {
             		
                 	Token token = renderer.getZone().getToken(renderer.getSelectedTokenSet().iterator().next());
                 	
                 	String newName = (String)JOptionPane.showInputDialog(renderer, "Pick a new name for this token", "Rename Token", JOptionPane.QUESTION_MESSAGE, null, null, token.getName());
                 	if (newName == null || newName.length() == 0) {
                 		return;
                 	}
                 	
                 	token.setName(newName);
             		MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
             		MapTool.getFrame().repaint();
             	}
             });
         }
         
         // Arrange
         JMenu arrangeMenu = new JMenu("Arrange");
         arrangeMenu.setEnabled(enabled);
         JMenuItem bringToFrontMenuItem = new JMenuItem("Bring to Front");
         bringToFrontMenuItem.addActionListener(new ActionListener() {
         
             public void actionPerformed(ActionEvent e) {
 
         		MapTool.serverCommand().bringTokensToFront(renderer.getZone().getId(), renderer.getSelectedTokenSet());
             	
             	MapTool.getFrame().repaint();
             }
         });
         
         JMenuItem sendToBackMenuItem = new JMenuItem("Send to Back");
         sendToBackMenuItem.addActionListener(new ActionListener() {
             
             public void actionPerformed(ActionEvent e) {
 
         		MapTool.serverCommand().sendTokensToBack(renderer.getZone().getId(), renderer.getSelectedTokenSet());
 
             	MapTool.getFrame().repaint();
             }
         });
 
         arrangeMenu.add(bringToFrontMenuItem);
         arrangeMenu.add(sendToBackMenuItem);
         
         // Create the state menu
         JMenu stateMenu = I18N.createMenu("defaultTool.stateMenu");
         stateMenu.setEnabled(enabled);
         stateMenu.add(new ChangeStateAction("clear"));
         stateMenu.addSeparator();
         for (String state : TokenStates.getStates())
           createStateItem(state, stateMenu, tokenUnderMouse);
         
         // Ownership
         JMenu ownerMenu = I18N.createMenu("defaultTool.ownerMenu");
         ownerMenu.setEnabled(enabled);
         if (MapTool.getPlayer().isGM() && MapTool.getServerPolicy().useStrictTokenManagement()) {
 	        
         	final Set<GUID> selectedTokenSet = renderer.getSelectedTokenSet();
         	
 	        JCheckBoxMenuItem allMenuItem = new JCheckBoxMenuItem("All");
 	        allMenuItem.addActionListener(new ActionListener() {
 	        	public void actionPerformed(ActionEvent e) {
 		        	for (GUID tokenGUID : selectedTokenSet) {
 		        		Token token = renderer.getZone().getToken(tokenGUID);
 		        		if (token != null) {
 		        			token.setAllOwners();
 		        			MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
 		        		}
 		        	}
 	        	}
 	        });
 	        ownerMenu.add(allMenuItem);
 
 	        JMenuItem removeAllMenuItem = new JMenuItem("Remove All");
 	        removeAllMenuItem.addActionListener(new ActionListener() {
 	        		
 	        	public void actionPerformed(ActionEvent e) {
 		        	for (GUID tokenGUID : selectedTokenSet) {
 		        		Token token = renderer.getZone().getToken(tokenGUID);
 		        		if (token != null) {
 		        			token.clearAllOwners();
 		        			MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
 		        		}
 		        	}
 	        	}
 	        });
 	        ownerMenu.add(removeAllMenuItem);
 	        ownerMenu.add(new JSeparator());
 	        
 	        int playerCount = 0;
 	        for (Player player : (Iterable<Player>)MapTool.getPlayerList()) {
 	        	
 	        	if (player.isGM()) {
 	        		continue;
 	        	}
 	        	
 	        	boolean selected = false;
 	        	
 	        	for (GUID tokenGUID : selectedTokenSet) {
 	        		Token token = renderer.getZone().getToken(tokenGUID);
 	        		if (token.isOwner(player.getName())) {
 	        			selected = true;
 	        			break;
 	        		}
 	        	}
 	        	JCheckBoxMenuItem playerMenu = new PlayerOwnershipMenu(player.getName(), selected, selectedTokenSet, renderer.getZone());
 	        	
 	        	ownerMenu.add(playerMenu);
 	        	playerCount ++;
 	        }
 	        
 	        if (playerCount == 0) {
 	        	JMenuItem noPlayerMenu = new JMenuItem("No players");
 	        	noPlayerMenu.setEnabled(false);
 	        	ownerMenu.add(noPlayerMenu);
 	        }
 	        
         }
         
         // Organize
         popup.add(stateMenu);
     	popup.add(sizeMenu);
         popup.add(arrangeMenu);
         popup.add(new ChangeStateAction("light"));
         popup.add(snapToGridMenuItem);
         popup.add(visibilityMenuItem);
         popup.add(renameMenuItem);
 
         // GM Only
         if (MapTool.getPlayer().isGM() && MapTool.getServerPolicy().useStrictTokenManagement()) {
         	popup.add(ownerMenu);
         }
         
     	popup.show(renderer, e.getX(), e.getY());
 	}
 
 	private static class PlayerOwnershipMenu extends JCheckBoxMenuItem implements ActionListener {
 		
 		private Set<GUID> tokenSet;
 		private Zone zone;
 		private boolean selected;
 		private String name;
 		
 		public PlayerOwnershipMenu(String name, boolean selected, Set<GUID> tokenSet, Zone zone) {
 			super(name, selected);
 			this.tokenSet = tokenSet;
 			this.zone = zone;
 			this.selected = selected;
 			this.name = name;
 			
 			addActionListener(this);
 		}
 		public void actionPerformed(ActionEvent e) {
 			for (GUID guid : tokenSet) {
 				Token token = zone.getToken(guid);
 				
 				if (selected) {
 					for (Player player : (Iterable<Player>)MapTool.getPlayerList()) {
 						token.addOwner(player.getName());
 					}
 					token.removeOwner(name);
 				} else {
 					token.addOwner(name);
 				}
 				
 				MapTool.serverCommand().putToken(zone.getId(), token);
 			}
 		}
 	}
 	
   /**
    * Create a radio button menu item for a particuar state
    * 
    * @param state Create the item for this state
    * @param menu The menu containing all items.
    * @return A menu item for the passed state.
    */
   private JCheckBoxMenuItem createStateItem(String state, JMenu menu, Token token) {
     JCheckBoxMenuItem item = new JCheckBoxMenuItem(new ChangeStateAction(state));
     Object value = token.getState(state);
     if (value != null && value instanceof Boolean && ((Boolean)value).booleanValue())
       item.setSelected(true);
     menu.add(item);
     return item;
   }
   
 	private class SnapToGridAction extends AbstractAction {
 		
 		private boolean snapToGrid;
 		private ZoneRenderer renderer;
 		
 		public SnapToGridAction(boolean snapToGrid, ZoneRenderer renderer) {
 			super("Snap to grid");
 			this.snapToGrid = snapToGrid;
 			this.renderer = renderer;
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 			
 			for (GUID guid : renderer.getSelectedTokenSet()) {
 				
 				Token token = renderer.getZone().getToken(guid);
 				if (token == null) {
 					continue;
 				}
 				
 				token.setSnapToGrid(snapToGrid);
                 MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
 			}
 		}
 	}
 	
   /**
    * Internal class used to handle token state changes.
    * 
    * @author jgorrell
    * @version $Revision$ $Date$ $Author$
    */
   private static class ChangeStateAction extends AbstractAction {
 
     /**
      * Initialize a state action for a given state.
      * 
      * @param state The name of the state set when this action is executed
      */
     public ChangeStateAction(String state) {
       putValue(ACTION_COMMAND_KEY, state); // Set the state command
       
       // Load the name, mnemonic, accelerator, and description if available
       String key = "defaultTool.stateAction." + state;
       String name = net.rptools.maptool.language.I18N.getText(key);
       if (!name.equals(key)) {
         putValue(NAME, name);
         int mnemonic = I18N.getMnemonic(key);
         if (mnemonic != -1) putValue(MNEMONIC_KEY, mnemonic);
         String accel = I18N.getAccelerator(key);
         if (accel != null) putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(accel));
         String description = I18N.getDescription(key);
         if (description != null) putValue(SHORT_DESCRIPTION, description);
       } else {
         
         // Default name if no I18N set
         putValue(NAME, state);
       } // endif
     }
     
     /**
      * Set the state for all of the selected tokens.
      * 
      * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
      */
     public void actionPerformed(ActionEvent aE) {
       ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
       for (GUID tokenGUID : renderer.getSelectedTokenSet()) {
         
         Token token = renderer.getZone().getToken(tokenGUID);
         if (aE.getActionCommand().equals("clear")) {
           for (String state : token.getStatePropertyNames())
             token.setState(state, null);
         } else if (aE.getActionCommand().equals("light")) {
           LightDialog.show(token, "light");
         } else {
           token.setState(aE.getActionCommand(), ((JCheckBoxMenuItem)aE.getSource()).isSelected() ? Boolean.TRUE : null);
         } // endif
         MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
       } // endfor
       renderer.repaint();
     }
   }
   
 	private class ChangeSizeAction extends AbstractAction {
 		
 		private TokenSize.Size size;
 		
 		public ChangeSizeAction(String label, TokenSize.Size size) {
 			super(label);
 			this.size = size;
 		}
 		
 		/* (non-Javadoc)
 		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 		 */
 		public void actionPerformed(ActionEvent e) {
 
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			for (GUID tokenGUID : renderer.getSelectedTokenSet()) {
 				
 				Token token = renderer.getZone().getToken(tokenGUID);
 				token.setSize(size.value());
                 MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
 			}
 			
 			renderer.repaint();
 		}
 		
 	}
 
   @Override
   protected void resetTool() {
     // Do nothing here for now
   }
   
 }
