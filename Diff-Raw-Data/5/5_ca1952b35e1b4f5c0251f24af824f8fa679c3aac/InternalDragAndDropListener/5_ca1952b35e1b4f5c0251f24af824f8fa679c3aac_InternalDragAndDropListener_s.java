 /**
  * Copyright (C) 2000 Maynard Demmon, maynard@organic.com
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
  
 package com.organic.maynard.outliner;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 
 public class InternalDragAndDropListener implements MouseListener {
 
 	private static final int ICON = 0;
 	private static final int TEXT = 1;
 	private static final int OTHER = -1;
 	
 	protected boolean isDragging = false;
 	protected Node targetNode = null;
 	protected int componentType = OTHER;
 	
 	protected OutlinerCellRendererImpl currentRenderer = null;
 	protected OutlinerCellRendererImpl prevRenderer = null;
 	
 	// The Constructor
 	public InternalDragAndDropListener() {}
 	
 	
 	// MouseListener Interface
 	public void mouseEntered(MouseEvent e) {
 		if (isDragging) {
 			//System.out.println("DND Mouse Entered: " + e.paramString());
 			
 			componentType = getUIComponents(e.getSource());
 			targetNode = getNodeFromSource(e.getSource());
 			
 			// Update the UI
 			if (!targetNode.isAncestorSelected()) {
 				if (componentType == ICON) {
 					currentRenderer.button.setIcon(OutlineButton.ICON_DOWN_ARROW);
 				} else if (componentType == TEXT) {
 					currentRenderer.button.setIcon(OutlineButton.ICON_SE_ARROW);
 				}
 			} else if (targetNode.isSelected() && !targetNode.isFirstChild() && (componentType == TEXT)) {
 				outlineLayoutManager layout = targetNode.getTree().doc.panel.layout;
 				OutlinerCellRendererImpl renderer = layout.getUIComponent(targetNode.prevSibling());
 				if (renderer != null) {
 					renderer.button.setIcon(OutlineButton.ICON_SE_ARROW);
 				}
 			}
 		}
 	}
 	
 	public void mouseExited(MouseEvent e) {
 		if (isDragging) {
 			//System.out.println("DND Mouse Exited: " + e.paramString());
 			
 			// Update the UI
 			if (targetNode.isSelected() && !targetNode.isFirstChild() && (componentType == TEXT)) {
 				outlineLayoutManager layout = targetNode.getTree().doc.panel.layout;
 				OutlinerCellRendererImpl renderer = layout.getUIComponent(targetNode.prevSibling());
 				if (renderer != null) {
 					renderer.button.updateIcon();
 				}
 			} else {
 				currentRenderer.button.updateIcon();
 			}
 			
 			// Update targetNode
 			targetNode = null;
 		}
 	}
 	
 	public void mousePressed(MouseEvent e) {
 		//System.out.println("DND Mouse Pressed: " + e.paramString());
 		
 		// Initiate Drag and Drop
 		targetNode = getNodeFromSource(e.getSource());
 		componentType = getUIComponents(e.getSource());
 		
 		if ((componentType == ICON) && targetNode.isSelected()) {
 			isDragging = true;
 		} else {
 			reset();
 		}
 	}
 
 	public void mouseReleased(MouseEvent e) {
 		if (isDragging) {
 			//System.out.println("DND Mouse Released: " + e.paramString());
 			
 			// Handle the drop
 			currentRenderer.button.updateIcon();
 			
 			if (targetNode != null) {
 				if (!targetNode.isAncestorSelected()) {
 					if (componentType == ICON) {
 						moveAsOlderSibling();
 					} else if (componentType == TEXT) {
 						moveAsFirstChild();
 					}
 				} else if (targetNode.isSelected() && !targetNode.isFirstChild() && (componentType == TEXT)) {
 					outlineLayoutManager layout = targetNode.getTree().doc.panel.layout;
 					OutlinerCellRendererImpl renderer = layout.getUIComponent(targetNode.prevSibling());
 					if (renderer != null) {
 						renderer.button.updateIcon();
 					}
 					
 					targetNode = targetNode.prevSibling();
 					moveAsFirstChild();
 				}
 			}
 			
 			// Terminate Drag and Drop
 			reset();
 		}
 	}
 
 	public void mouseClicked(MouseEvent e) {
 		//System.out.println("DND Mouse Clicked: " + e.paramString());
 	}
 
 
 	private void moveAsOlderSibling() {
 		TreeContext tree = targetNode.getTree();
 		outlineLayoutManager layout = tree.doc.panel.layout;
 
 		// Store nodeToDrawFrom if neccessary. Used when the selection is dissconnected.
 		Node nodeToDrawFromTmp = layout.getNodeToDrawFrom().nextUnSelectedNode();
 
 		CompoundUndoableMove undoable = new CompoundUndoableMove(tree.getSelectedNodesParent(),targetNode.getParent());
 		
 		for (int i = tree.selectedNodes.size() - 1; i >= 0; i--) {
 			// Record the Insert in the undoable
 			Node nodeToMove = (Node) tree.selectedNodes.get(i);
 			int currentIndex = nodeToMove.currentIndex();
 			int targetIndex = targetNode.currentIndex();
			if (currentIndex > targetIndex) {
 				targetIndex++;
 			}
 			undoable.addPrimitive(new PrimitiveUndoableMove(undoable, nodeToMove, currentIndex, targetIndex));
 			tree.moveNodeBelowAsSibling(nodeToMove,targetNode);
 		}
 		
 		tree.doc.undoQueue.add(undoable);
 		
 		// Update the selection model
 		tree.setSelectedNodesParent(targetNode.getParent(),false);
 
 		// Redraw and Set Focus
 		if (layout.getNodeToDrawFrom().isAncestorSelected()) { // Makes sure we dont' stick at the top when multiple nodes are selected.
 			Node visNode = layout.getNodeToDrawFrom().prev();
 			int ioVisNode = tree.visibleNodes.indexOf(visNode);
 			int ioNodeToDrawFromTmp = tree.visibleNodes.indexOf(nodeToDrawFromTmp);
 			if (ioVisNode < ioNodeToDrawFromTmp) {
 				layout.setNodeToDrawFrom(visNode, ioVisNode);
 			} else {
 				layout.setNodeToDrawFrom(nodeToDrawFromTmp, ioNodeToDrawFromTmp);
 			}
 		}
 		
 		layout.draw(targetNode.getLastViewableDecendent(), outlineLayoutManager.ICON);
 	}
 	
 	private void moveAsFirstChild() {
 		TreeContext tree = targetNode.getTree();
 		outlineLayoutManager layout = tree.doc.panel.layout;
 
 		// Store nodeToDrawFrom if neccessary. Used when the selection is dissconnected.
 		Node nodeToDrawFromTmp = layout.getNodeToDrawFrom().nextUnSelectedNode();
 
 		CompoundUndoableMove undoable = new CompoundUndoableMove(tree.getSelectedNodesParent(),targetNode);
 		
 		for (int i = tree.selectedNodes.size() - 1; i >= 0; i--) {
 			// Record the Insert in the undoable
 			Node nodeToMove = (Node) tree.selectedNodes.get(i);
 			int currentIndex = nodeToMove.currentIndex();
 			int targetIndex = 0;
 			undoable.addPrimitive(new PrimitiveUndoableMove(undoable, nodeToMove, currentIndex, targetIndex));
 			nodeToMove.getParent().removeChild(nodeToMove);
 			targetNode.insertChild(nodeToMove,0);
 			nodeToMove.setDepthRecursively(targetNode.getDepth() + 1);
 			tree.removeNode(nodeToMove);
 			tree.insertNode(nodeToMove);	
 		}
 		
 		tree.doc.undoQueue.add(undoable);
 		
 		// Update the selection model
 		tree.setSelectedNodesParent(targetNode,false);
 
 		// Redraw and Set Focus
 		if (layout.getNodeToDrawFrom().isAncestorSelected()) { // Makes sure we dont' stick at the top when multiple nodes are selected.
 			Node visNode = layout.getNodeToDrawFrom().prev();
 			int ioVisNode = tree.visibleNodes.indexOf(visNode);
 			int ioNodeToDrawFromTmp = tree.visibleNodes.indexOf(nodeToDrawFromTmp);
 			if (ioVisNode < ioNodeToDrawFromTmp) {
 				layout.setNodeToDrawFrom(visNode, ioVisNode);
 			} else {
 				layout.setNodeToDrawFrom(nodeToDrawFromTmp, ioNodeToDrawFromTmp);
 			}
 		}
 		
 		layout.draw(tree.getOldestInSelection(), outlineLayoutManager.ICON);
 	}
 	
 		
 	private Node getNodeFromSource(Object source) {
 		if (source instanceof OutlinerCellRendererImpl) {
 			return ((OutlinerCellRendererImpl) source).node;
 		} else if (source instanceof OutlineButton) {
 			return ((OutlineButton) source).renderer.node;
 		} else {
 			return null;
 		}
 	}
 
 	private int getUIComponents(Object source) {
 		if (source instanceof OutlinerCellRendererImpl) {
 			prevRenderer = currentRenderer;
 			currentRenderer = (OutlinerCellRendererImpl) source;
 			return TEXT;
 		} else if (source instanceof OutlineButton) {
 			prevRenderer = currentRenderer;
 			currentRenderer = ((OutlineButton) source).renderer;
 			return ICON;
 		} else {
 			// Something went wrong.
 			return OTHER;
 		}
 	}
 	
 	private void reset() {
 		isDragging = false;
 		targetNode = null;
 		componentType = OTHER;
 	
 		currentRenderer = null;
 		prevRenderer = null;
 	}
 }
