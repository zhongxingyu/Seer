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
 import java.awt.Window;
 import java.awt.datatransfer.*;
 
 import javax.swing.*;
 import javax.swing.event.*;
 
 import com.organic.maynard.util.string.*;
 
 public class IconKeyListener implements KeyListener, MouseListener {
 
 	// Constants for setting cursor position.
 	private static final int POSITION_FIRST = 0;
 	private static final int POSITION_CURRENT = 1;
 	private static final int POSITION_LAST = 2;
 
 
 	// Instance Fields
 	private OutlinerCellRendererImpl textArea = null;
 
 
 	// The Constructors
 	public IconKeyListener() {}
 	
 	public void destroy() {
 		textArea = null;
 	}
 
 	
 	private void recordRenderer(Component c) {
 		if (c instanceof OutlineButton) {
 			textArea = ((OutlineButton) c).renderer;
 		} else if (c instanceof OutlineLineNumber) {
 			textArea = ((OutlineLineNumber) c).renderer;
 		}
 	}
 
 	// MouseListener Interface
 	public void mouseEntered(MouseEvent e) {}
 	
 	public void mouseExited(MouseEvent e) {}
 	
 	public void mousePressed(MouseEvent e) {
 		recordRenderer(e.getComponent());
 		
 		// This is detection for Solaris
 		if (e.isPopupTrigger() && textArea.node.isSelected()) {
 			Outliner.macroPopup.show(e.getComponent(),e.getX(), e.getY());
 		}
 		
 		// Handle clicks. The modulo is to deal with rapid clicks that would register as a triple click or more.
 		if ((e.getClickCount() % 2) == 1) {
 			processSingleClick(e);
 		} else if ((e.getClickCount() % 2) == 0){
 			processDoubleClick(e);
 		}
 		
 		// Record the EditingNode and CursorPosition and ComponentFocus
  		TreeContext tree = textArea.node.getTree();
 		tree.setEditingNode(textArea.node);
 		tree.setComponentFocus(OutlineLayoutManager.ICON);
 		
		// Store the node
 		Node node = textArea.node;
 		
 		// Redraw and set focus
		textArea.button.requestFocus();
		tree.doc.panel.layout.draw();
 		
 		// Consume the current event and then propogate a new event to
 		// the DnD listener since if a drawUp() happened, the old event
 		// will most likely have an invalid component.
 		e.consume();
 
 		MouseEvent eNew = new MouseEvent(
 			tree.doc.panel.layout.getUIComponent(node).button, 
 			e.getID(), 
 			e.getWhen(), 
 			e.getModifiers(), 
 			e.getX(), 
 			e.getY(), 
 			e.getClickCount(), 
 			e.isPopupTrigger()
 		);
 		tree.doc.panel.layout.dndListener.mousePressed(eNew);
 	}
 	
 	public void mouseReleased(MouseEvent e) {
 		recordRenderer(e.getComponent());
 		
 		// This is detection for Windows
 		if (e.isPopupTrigger() && textArea.node.isSelected()) {
 			Outliner.macroPopup.show(e.getComponent(),e.getX(), e.getY());
 		}
 	}
 	
 	public void mouseClicked(MouseEvent e) {}
 	    
 	protected void processSingleClick(MouseEvent e) {
 		Node node = textArea.node;
 		TreeContext tree = node.getTree();
 		
 		if (e.isShiftDown()) {
 			tree.selectRangeFromMostRecentNodeTouched(node);
 		
 		} else if (e.isControlDown()) {
 			if (node.isSelected() && (tree.selectedNodes.size() != 1)) {
 				tree.removeNodeFromSelection(node);
 			} else if (tree.getSelectedNodesParent() == node.getParent()) {
 				tree.addNodeToSelection(node);
 			}
 			
 		} else if (!node.isSelected()) {
 			tree.setSelectedNodesParent(node.getParent());
 			tree.addNodeToSelection(node);
 		}	
 	}
 	
 	protected void processDoubleClick(MouseEvent e) {
 		if (textArea.node.isExpanded()) {
 			textArea.node.setExpanded(false);
 		} else {
 			textArea.node.setExpanded(true);
 		}
 		
 		if (textArea.node.isSelected()) {
 			textArea.node.getTree().setSelectedNodesParent(textArea.node.getParent());
 			textArea.node.getTree().addNodeToSelection(textArea.node);
 		}		
 	}
 	
 	
 	// KeyListener Interface
 	public void keyPressed(KeyEvent e) {
 		recordRenderer(e.getComponent());
 	
 		// Create some short names for convienence
 		TreeContext tree = textArea.node.getTree();
 		OutlineLayoutManager layout = tree.doc.panel.layout;
 
 		switch(e.getKeyCode()) {
 			case KeyEvent.VK_PAGE_DOWN:
 				toggleExpansion(tree,layout);
 				break;
 
 			case KeyEvent.VK_PAGE_UP:
 				toggleComment(tree,layout);
 				break;
 			
 			case KeyEvent.VK_DELETE:
 				delete(tree,layout,true);
 				break;
 
 			case KeyEvent.VK_BACK_SPACE:
 				delete(tree,layout,false);
 				break;
 				
 			case KeyEvent.VK_UP:
 				if (e.isShiftDown()) {
 					moveUp(tree,layout);
 				} else {
 					navigate(tree, layout, UP);
 				}
 				break;
 
 			case KeyEvent.VK_DOWN:
 				if (e.isShiftDown()) {
 					moveDown(tree,layout);
 				} else {
 					navigate(tree,layout, DOWN);
 				}
 				break;
 
 			case KeyEvent.VK_LEFT:
 				if (e.isShiftDown()) {
 					moveLeft(tree,layout);
 				} else {
 					navigate(tree,layout, LEFT);
 				}
 				break;
 
 			case KeyEvent.VK_RIGHT:
 				if (e.isShiftDown()) {
 					moveRight(tree,layout);
 				} else {
 					navigate(tree,layout, RIGHT);
 				}
 				break;
 
 			case KeyEvent.VK_ENTER:
 				insert(tree,layout);
 				break;
 
 			case KeyEvent.VK_INSERT:
 				if (e.isShiftDown()) {
 					changeToParent(tree, layout);
 				} else {
 					changeFocusToTextArea(tree, layout, POSITION_CURRENT);
 				}
 				break;
 
 			case KeyEvent.VK_HOME:
 				if (tree.selectedNodes.size() > 1) {
 					changeSelectionToNode(tree, layout, POSITION_FIRST);
 				} else {
 					changeFocusToTextArea(tree, layout, POSITION_FIRST);
 				}
 				break;
 
 			case KeyEvent.VK_END:
 				if (tree.selectedNodes.size() > 1) {
 					changeSelectionToNode(tree, layout, POSITION_LAST);
 				} else {
 					changeFocusToTextArea(tree, layout, POSITION_LAST);
 				}
 				break;
 
 			case KeyEvent.VK_TAB:
 				if (e.isShiftDown()) {
 					promote(tree,layout);
 				} else {
 					demote(tree,layout);
 				}
 				break;
 
 			case KeyEvent.VK_C:
 				if (e.isControlDown()) {
 					copy(tree,layout);
 					break;
 				} else {
 					return;
 				}
 
 			case KeyEvent.VK_X:
 				if (e.isControlDown()) {
 					cut(tree,layout);
 					break;
 				} else {
 					return;
 				}
 
 			case KeyEvent.VK_V:
 				if (e.isControlDown()) {
 					paste(tree,layout);
 					break;
 				} else {
 					return;
 				}
 
 			case KeyEvent.VK_I:
 				if (e.isControlDown()) {
 					selectInverse(tree,layout);
 					break;
 				} else {
 					return;
 				}
 
 			case KeyEvent.VK_A:
 				if (e.isControlDown() && !e.isShiftDown()) {
 					selectAll(tree,layout);
 					break;
 				} else {
 					return;
 				}
 
 			case KeyEvent.VK_D:
 				if (e.isControlDown() && !e.isShiftDown()) {
 					selectNone(tree,layout);
 					break;
 				} else {
 					return;
 				}
 
 			case KeyEvent.VK_M:
 				if (e.isControlDown()) {
 					if (e.isShiftDown()) {
 						merge(tree,layout,true);
 					} else {
 						merge(tree,layout,false);
 					}
 					break;
 				} else {
 					return;
 				}
 
 			default:
 				return;
 		}
 		
 		e.consume();
 		return;
 	}
 	
 	public void keyTyped(KeyEvent e) {
 		recordRenderer(e.getComponent());
 		
 		// Catch any unwanted chars that slip through
 		if (e.isControlDown() ||
 			(e.getKeyChar() == KeyEvent.VK_BACK_SPACE) ||
 			(e.getKeyChar() == KeyEvent.VK_TAB) ||
 			(e.getKeyChar() == KeyEvent.VK_ENTER) ||
 			(e.getKeyChar() == KeyEvent.VK_INSERT)
 		) {
 			return;
 		}
 				
 		//System.out.println("ICON Typed: " + e.paramString());
 
 		// Create some short names for convienence
 		Node currentNode = textArea.node;
 		TreeContext tree = currentNode.getTree();
 		OutlineLayoutManager layout = tree.doc.panel.layout;
 		Node youngestNode = tree.getYoungestInSelection();
 
 		// Clear the selection since focus will change to the textarea.
 		tree.clearSelection();
 		
 		// Replace the text with the character that was typed
 		String oldText = youngestNode.getValue();
 		String newText = String.valueOf(e.getKeyChar());
 		youngestNode.setValue(newText);
 
 		// Record the EditingNode and CursorPosition and ComponentFocus
 		tree.setCursorPosition(1);
 		tree.setComponentFocus(OutlineLayoutManager.TEXT);
 
 		// Put the Undoable onto the UndoQueue
 		tree.doc.undoQueue.add(new UndoableEdit(youngestNode,oldText,newText,0,1,0,1));
 		
 		// Redraw and Set Focus
 		layout.draw(youngestNode,OutlineLayoutManager.TEXT);
 
 		e.consume();
 		return;
 	}
 	public void keyReleased(KeyEvent e) {}
 
 
 
 	// Key Handlers
 	private void toggleExpansion(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		
 		for (int i = 0; i < tree.selectedNodes.size(); i++) {
 			Node node = (Node) tree.selectedNodes.get(i);
 			if (node.isExpanded()) {
 				node.setExpanded(false);
 			} else {
 				node.setExpanded(true);
 			}
 		}
 
 		layout.draw(currentNode, OutlineLayoutManager.ICON);
 	}
 
 	private void toggleComment(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		
 		CompoundUndoablePropertyChange undoable = new CompoundUndoablePropertyChange(tree);
 		
 		for (int i = 0; i < tree.selectedNodes.size(); i++) {
 			Node node = (Node) tree.selectedNodes.get(i);
 			
 			if (!node.isAncestorComment()) {
 				boolean oldValue = node.isComment();
 				boolean newValue = false;
 
 				if (node.isComment()) {
 					node.setComment(false);
 				} else {
 					node.setComment(true);
 					newValue = true;
 				}
 				
 				undoable.addPrimitive(new PrimitiveUndoableCommentChange(node, oldValue, newValue));
 			}
 		}
 		
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 		}
 
 		layout.draw(currentNode, OutlineLayoutManager.ICON);
 	}
 
 	private void changeFocusToTextArea(TreeContext tree, OutlineLayoutManager layout, int positionType) {
 		Node currentNode = textArea.node;
 		
 		if (positionType == POSITION_FIRST) {
 			tree.setCursorPosition(0);
 			tree.doc.setPreferredCaretPosition(0);
 		} else if (positionType == POSITION_LAST) {
 			int index = textArea.getText().length();
 			tree.setCursorPosition(index);
 			tree.doc.setPreferredCaretPosition(index);		
 		}
 		
 		tree.setComponentFocus(OutlineLayoutManager.TEXT);
 		tree.clearSelection();
 		layout.draw(currentNode,OutlineLayoutManager.TEXT);
 	}
 
 	private void changeSelectionToNode(TreeContext tree, OutlineLayoutManager layout, int positionType) {
 		Node selectedNode = null;
 		
 		if (positionType == POSITION_FIRST) {
 			selectedNode = tree.getYoungestInSelection();
 		} else if (positionType == POSITION_LAST) {
 			selectedNode = tree.getOldestInSelection();
 		}
 		
 		// Update Selection
 		tree.clearSelection();
 		tree.addNodeToSelection(selectedNode);
 
 		// Record State
 		tree.setEditingNode(selectedNode);
 		tree.setCursorPosition(0);
 		tree.doc.setPreferredCaretPosition(0);
 		
 		// Redraw and Set Focus	
 		layout.draw(selectedNode, OutlineLayoutManager.ICON);
 	}
 
 	private void moveUp(TreeContext tree, OutlineLayoutManager layout) {
 		Node youngestNode = tree.getYoungestInSelection();
 		Node node = youngestNode.prevSibling();
 		if (node == youngestNode) {
 			return;
 		}
 
 		// Put the Undoable onto the UndoQueue
 		CompoundUndoableMove undoable = new CompoundUndoableMove(node.getParent(),node.getParent());
 		int targetIndex = node.currentIndex();
 		
 		for (int i = 0; i < tree.selectedNodes.size(); i++) {
 			// Record the Insert in the undoable
 			Node nodeToMove = (Node) tree.selectedNodes.get(i);
 			int currentIndex = nodeToMove.currentIndex();
 			undoable.addPrimitive(new PrimitiveUndoableMove(undoable, nodeToMove, currentIndex, targetIndex));
 			targetIndex++;
 		}
 
 		tree.doc.undoQueue.add(undoable);
 		
 		undoable.redo();
 	}
 	
 	private void moveDown(TreeContext tree, OutlineLayoutManager layout) {
 		Node oldestNode = tree.getOldestInSelection();
 		Node node = oldestNode.nextSibling();
 		if (node == oldestNode) {
 			return;
 		}
 
 		// Put the Undoable onto the UndoQueue
 		CompoundUndoableMove undoable = new CompoundUndoableMove(node.getParent(),node.getParent());
 		int targetIndex = node.currentIndex();
 		
 		// Do the move
 		for (int i = tree.selectedNodes.size() - 1; i >= 0; i--) {
 			// Record the Insert in the undoable
 			Node nodeToMove = (Node) tree.selectedNodes.get(i);
 			int currentIndex = nodeToMove.currentIndex();
 			undoable.addPrimitive(new PrimitiveUndoableMove(undoable, nodeToMove, currentIndex, targetIndex));
 			targetIndex--;
 		}
 
 		tree.doc.undoQueue.add(undoable);
 		
 		undoable.redo();
 	}
 
 	private void moveLeft(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		Node youngestNode = tree.getYoungestInSelection();
 		Node node = tree.getPrevNode(youngestNode);
 		if (node == null) {
 			return;
 		}
 
 		// Put the Undoable onto the UndoQueue
 		CompoundUndoableMove undoable = new CompoundUndoableMove(currentNode.getParent(), node.getParent());
 		int targetIndex = node.currentIndex();
 		int currentIndexAdj = 0;
 		
 		for (int i = 0; i < tree.selectedNodes.size(); i++) {
 			// Record the Insert in the undoable
 			Node nodeToMove = (Node) tree.selectedNodes.get(i);
 			int currentIndex = nodeToMove.currentIndex() + currentIndexAdj;
 			undoable.addPrimitive(new PrimitiveUndoableMove(undoable, nodeToMove, currentIndex, targetIndex));
 			
 			if (nodeToMove.getParent() != node.getParent()) {
 				currentIndexAdj--;
 			}
 			targetIndex++;
 		}
 
 		tree.doc.undoQueue.add(undoable);
 		
 		undoable.redo();		
 	}
 
 
 	private void moveRight(TreeContext tree, OutlineLayoutManager layout) {
 		Node oldestNode = tree.getOldestInSelection();
 		Node node = tree.getNextNode(oldestNode.getLastViewableDecendent());
 		if (node == null) {
 			return;
 		}
 
 		// Put the Undoable onto the UndoQueue
 		CompoundUndoableMove undoable;
 		int targetIndex = -1;
 		if ((!node.isLeaf() && node.isExpanded())) {
 			undoable = new CompoundUndoableMove(oldestNode.getParent(),node);
 			targetIndex = 0;
 		} else if (tree.getSelectedNodesParent() != node.getParent()) {
 			undoable = new CompoundUndoableMove(oldestNode.getParent(),node.getParent());
 			targetIndex = node.currentIndex() + 1;
 		} else {
 			undoable = new CompoundUndoableMove(oldestNode.getParent(),node.getParent());
 			targetIndex = node.currentIndex();
 		}
 	
 		for (int i = tree.selectedNodes.size() - 1; i >= 0; i--) {
 			// Record the Insert in the undoable
 			Node nodeToMove = (Node) tree.selectedNodes.get(i);
 			int currentIndex = nodeToMove.currentIndex();
 			
 			undoable.addPrimitive(new PrimitiveUndoableMove(undoable, nodeToMove, currentIndex, targetIndex));
 
 			if ((!node.isLeaf() && node.isExpanded()) || (nodeToMove.getParent() != node.getParent())) {
 				// Do Nothing.
 			} else {
 				targetIndex--;
 			}
 		}
 
 		tree.doc.undoQueue.add(undoable);
 		
 		undoable.redo();
 	}
 
 	private void insert(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		
 		// Create a new node and insert it as a sibling immediatly after the last selected node.
 		Node node = tree.getOldestInSelection();
 		int nodeIndex = -1;
 		
 		Node newNode = new NodeImpl(tree,"");
 		int newNodeIndex = -1;
 		Node newNodeParent = null;
 		
 		if ((!node.isLeaf()) && (node.isExpanded())) {
 			newNodeIndex = 0;
 			newNodeParent = node;
 			newNode.setDepth(node.getDepth() + 1);
 			node.insertChild(newNode, newNodeIndex);
 		} else {
 			nodeIndex = node.currentIndex();
 			newNodeIndex = nodeIndex + 1;
 			newNodeParent = node.getParent();
 			newNode.setDepth(node.getDepth());
 			newNodeParent.insertChild(newNode, newNodeIndex);
 		}
 		
 		int visibleIndex = tree.insertNodeAfter(node, newNode);
 
 		// Record the EditingNode and CursorPosition and ComponentFocus
 		tree.setEditingNode(newNode);
 		tree.setCursorPosition(0);
 		tree.doc.setPreferredCaretPosition(0);
 		tree.setComponentFocus(OutlineLayoutManager.TEXT);
 
 		// Put the Undoable onto the UndoQueue
 		CompoundUndoableInsert undoable = new CompoundUndoableInsert(newNodeParent);
 		undoable.addPrimitive(new PrimitiveUndoableInsert(newNodeParent, newNode, newNodeIndex));
 		tree.doc.undoQueue.add(undoable);
 
 		// Redraw and Set Focus
 		tree.clearSelection();
 		layout.draw(newNode, visibleIndex, OutlineLayoutManager.TEXT);
 	}
 
 	private void promote(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		
 		if (currentNode.getParent().isRoot()) {
 			return;
 		}
 
 		// Put the Undoable onto the UndoQueue
 		Node targetNode = currentNode.getParent().getParent();
 		int targetIndex = currentNode.getParent().currentIndex() + 1;
 		
 		CompoundUndoableMove undoable = new CompoundUndoableMove(currentNode.getParent(),targetNode);
 
 		for (int i = tree.selectedNodes.size() - 1; i >= 0; i--) {
 			// Record the Insert in the undoable
 			Node nodeToMove = (Node) tree.selectedNodes.get(i);
 			undoable.addPrimitive(new PrimitiveUndoableMove(undoable, nodeToMove, nodeToMove.currentIndex(), targetIndex));
 		}
 		
 		tree.doc.undoQueue.add(undoable);
 		
 		undoable.redo();
 	}
 
 	private void demote(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 
 		if (tree.getYoungestInSelection().isFirstChild()) {
 			return;
 		}
 		
 		// Put the Undoable onto the UndoQueue
 		Node targetNode = tree.getYoungestInSelection().prevSibling();
 
 		CompoundUndoableMove undoable = new CompoundUndoableMove(currentNode.getParent(),targetNode);
 		
 		int existingChildren = targetNode.numOfChildren();
 		for (int i = tree.selectedNodes.size() - 1; i >= 0; i--) {
 			// Record the Insert in the undoable
 			Node nodeToMove = (Node) tree.selectedNodes.get(i);
 			undoable.addPrimitive(new PrimitiveUndoableMove(undoable, nodeToMove, nodeToMove.currentIndex(), existingChildren));
 		}
 		
 		tree.doc.undoQueue.add(undoable);
 		
 		undoable.redo();
 	}
 
 	private void copy(TreeContext tree, OutlineLayoutManager layout) {
 		StringBuffer buffer = new StringBuffer();
 		for (int i = 0; i < tree.selectedNodes.size(); i++) {
 			((Node) tree.selectedNodes.get(i)).depthPaddedValue(buffer, Preferences.LINE_END_STRING);
 			
 			//buffer.append(((Node) tree.selectedNodes.get(i)).depthPaddedValue(Preferences.LINE_END_STRING));
 		}
 		
 		// Put the text onto the clipboard
 		Outliner.clipboard.setContents(new StringSelection(buffer.toString()), Outliner.outliner);
 	}
 
 	private void cut(TreeContext tree, OutlineLayoutManager layout) {
 		StringBuffer buffer = new StringBuffer();
 		for (int i = 0; i < tree.selectedNodes.size(); i++) {
 			((Node) tree.selectedNodes.get(i)).depthPaddedValue(buffer, Preferences.LINE_END_STRING);
 			
 			//buffer.append(((Node) tree.selectedNodes.get(i)).depthPaddedValue(Preferences.LINE_END_STRING));
 		}
 		
 		// Put the text onto the clipboard
 		Outliner.clipboard.setContents(new StringSelection(buffer.toString()), Outliner.outliner);
 		
 		delete(tree,layout,false);
 	}
 
 	private void paste(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 
 		// Put the Undoable onto the UndoQueue
 		CompoundUndoableInsert undoable = new CompoundUndoableInsert(currentNode.getParent());
 		tree.doc.undoQueue.add(undoable);
 
 		// Get the text from the clipboard and turn it into a tree
 		String text = "";
 		try {
 			Transferable selection = (Transferable) Outliner.clipboard.getContents(this);
 			if (selection != null) {
 				text = (String) selection.getTransferData(DataFlavor.stringFlavor);
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		
 		Node oldestNode = tree.getOldestInSelection();
 		
 		Node tempRoot = PadSelection.pad(text, tree, oldestNode.getDepth(),Preferences.LINE_END_STRING);
 		
 		Node parentForNewNode = oldestNode.getParent();
 		int indexForNewNode = parentForNewNode.getChildIndex(oldestNode);
 		for (int i = tempRoot.numOfChildren() - 1; i >= 0; i--) {
 			Node node = tempRoot.getChild(i);
 			parentForNewNode.insertChild(node, indexForNewNode + 1);
 			tree.insertNode(node);
 
 			// Record the Insert in the undoable
 			int index = node.currentIndex() + i;
 			undoable.addPrimitive(new PrimitiveUndoableInsert(parentForNewNode,node,index));
 		}
 				
 		tree.clearSelection();
 
 		for (int i = tempRoot.numOfChildren() - 1; i >= 0; i--) {
 			tree.addNodeToSelection(tempRoot.getChild(i));
 		}
 
 		// Record the EditingNode and CursorPosition and ComponentFocus
 		tree.setEditingNode(tree.getYoungestInSelection());
 
 		// Redraw and Set Focus
 		layout.draw(tree.getYoungestInSelection(),OutlineLayoutManager.ICON);
 	}
 
 	private void selectAll(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 
 		// select all siblings
 		Node parent = currentNode.getParent();
 		
 		tree.addNodeToSelection(parent.getChild(0));
 		tree.selectRangeFromMostRecentNodeTouched(parent.getChild(parent.numOfChildren() - 1));
 
 		// Redraw and Set Focus
 		layout.draw(currentNode,OutlineLayoutManager.ICON);
 	}
 
 	private void selectInverse(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 
 		// select all siblings
 		Node parent = currentNode.getParent();
 		
 		for (int i = 0; i < parent.numOfChildren(); i++) {
 			Node child = parent.getChild(i);
 			
 			if (child.isSelected()) {
 				tree.removeNodeFromSelection(child);
 			} else {
 				tree.addNodeToSelection(child);
 			}
 		}
 		
 		if (tree.getNumberOfSelectedNodes() == 0) {
 			// Change to text node if all nodes were deselected.
 			changeFocusToTextArea(tree, layout, POSITION_FIRST);
 		} else {
 			// Redraw and Set Focus
 			layout.draw(currentNode,OutlineLayoutManager.ICON);
 		}
 	}
 
 	private void selectNone(TreeContext tree, OutlineLayoutManager layout) {
 		changeFocusToTextArea(tree, layout, POSITION_FIRST);
 	}
 
 	private void changeToParent(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		Node newSelectedNode = currentNode.getParent();
 		if (newSelectedNode.isRoot()) {return;}
 		
 		tree.setSelectedNodesParent(newSelectedNode.getParent());
 		tree.addNodeToSelection(newSelectedNode);
 		
 		tree.setEditingNode(newSelectedNode);
 		
 		// Redraw and Set Focus
 		layout.draw(newSelectedNode, OutlineLayoutManager.ICON);		
 	}
 	
 	private static final int UP = 1;
 	private static final int DOWN = 2;
 	private static final int LEFT = 3;
 	private static final int RIGHT = 4;
 	
 	private void navigate(TreeContext tree, OutlineLayoutManager layout, int type) {
 		Node node = null;
 		Node youngestNode = null;
 		Node oldestNode = null;
 		
 		switch(type) {
 			case UP:
 				youngestNode = tree.getYoungestInSelection();
 				node = youngestNode.prevSibling();
 				if (node == youngestNode) {return;}
 				tree.clearSelection();
 				break;
 
 			case DOWN:
 				oldestNode = tree.getOldestInSelection();
 				node = oldestNode.nextSibling();
 				if (node == oldestNode) {return;}
 				tree.clearSelection();
 				break;
 
 			case LEFT:
 				youngestNode = tree.getYoungestInSelection();
 				node = tree.getPrevNode(youngestNode);
 				if (node == null) {return;}
 				tree.setSelectedNodesParent(node.getParent());
 				break;
 
 			case RIGHT:
 				oldestNode = tree.getOldestInSelection();
 				node = tree.getNextNode(oldestNode);
 				if (node == null) {return;}
 				tree.setSelectedNodesParent(node.getParent());
 				break;
 				
 			default:
 				return;
 		}
 		
 		tree.addNodeToSelection(node);
 
 		// Record the EditingNode and CursorPosition and ComponentFocus
 		tree.setEditingNode(node);
 
 		// Redraw and Set Focus
 		layout.draw(node,OutlineLayoutManager.ICON);
 	}
 	
 	protected void delete (TreeContext tree, OutlineLayoutManager layout, boolean deleteMode) {
 		Node youngestNode = tree.getYoungestInSelection();
 		Node parent = tree.getEditingNode().getParent();
 		CompoundUndoableReplace undoable = new CompoundUndoableReplace(parent, deleteMode);
 
 		if (tree.isWholeDocumentSelected()) {
 			if (tree.isDocumentEmpty()) {return;}
 			
 			Node newNode = new NodeImpl(tree,"");
 			newNode.setDepth(0);
 			undoable.addPrimitive(new PrimitiveUndoableReplace(parent,youngestNode,newNode));
 
 			// Iterate over the remaining selected nodes deleting each one
 			for (int i = 1; i < tree.getNumberOfSelectedNodes(); i++) {
 				Node node = (Node) tree.selectedNodes.get(i);
 				undoable.addPrimitive(new PrimitiveUndoableReplace(parent,node,null));
 			}
 		} else {
 			// Iterate over the remaining selected nodes deleting each one
 			for (int i = 0; i < tree.getNumberOfSelectedNodes(); i++) {
 				Node node = (Node) tree.selectedNodes.get(i);
 				undoable.addPrimitive(new PrimitiveUndoableReplace(parent,node,null));
 			}
 		}
 
 		tree.doc.undoQueue.add(undoable);
 		undoable.redo();
 		return;
 	}
 
 	private void merge(TreeContext tree, OutlineLayoutManager layout, boolean withSpaces) {
 		Node youngestNode = tree.getYoungestInSelection();
 		int youngestNodeDepth = youngestNode.getDepth();
 		boolean youngestNodeIsComment = youngestNode.isComment();
 		Node parent = tree.getEditingNode().getParent();
 		CompoundUndoableReplace undoable = new CompoundUndoableReplace(parent);
 
 		// Get merged text
 		StringBuffer buf = new StringBuffer();
 		
 		if (withSpaces) {
 			for (int i = 0; i < tree.selectedNodes.size(); i++) {
 				((Node) tree.selectedNodes.get(i)).getMergedValueWithSpaces(buf);
 			}
 		} else {
 			for (int i = 0; i < tree.selectedNodes.size(); i++) {
 				((Node) tree.selectedNodes.get(i)).getMergedValue(buf);
 			}		
 		}
 
 		Node newNode = new NodeImpl(tree,buf.toString());
 		newNode.setDepth(youngestNodeDepth);
 		newNode.setComment(youngestNodeIsComment);
 		undoable.addPrimitive(new PrimitiveUndoableReplace(parent, youngestNode, newNode));
 
 		// Iterate over the remaining selected nodes deleting each one
 		for (int i = 1; i < tree.getNumberOfSelectedNodes(); i++) {
 			Node node = (Node) tree.selectedNodes.get(i);
 			undoable.addPrimitive(new PrimitiveUndoableReplace(parent,node,null));
 		}
 
 		tree.doc.undoQueue.add(undoable);
 		undoable.redo();
 		return;
 	}
 
 
 	// Additional Outline Methods
 	public static void hoist(TreeContext tree) {
 		if (tree.selectedNodes.size() != 1) {
 			return;
 		}
 		
 		TextKeyListener.hoist(tree.getYoungestInSelection());
 		return;
 	}
 
 	public static void expandAllSubheads(TreeContext tree) {
 		for (int i = 0; i < tree.selectedNodes.size(); i++) {
 			((Node) tree.selectedNodes.get(i)).ExpandAllSubheads();
 		}
 		tree.doc.panel.layout.draw();
 		return;
 	}
 
 	public static void expandEverything(TreeContext tree) {
 		TextKeyListener.expandEverything(tree);
 		return;
 	}
 
 	public static void collapseToParent(TreeContext tree) {
 		TextKeyListener.collapseToParent(tree.getEditingNode());
 		return;
 	}
 
 	public static void collapseEverything(TreeContext tree) {
 		TextKeyListener.collapseEverything(tree);
 		return;
 	}
 }
