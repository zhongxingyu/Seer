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
 
 import javax.swing.*;
 import java.awt.event.*;
 import java.awt.datatransfer.*;
 
 public class TextKeyListener implements KeyListener, MouseListener {
 	
 	private OutlinerCellRendererImpl textArea = null;
 	
 	private boolean inlinePaste = true;
 	private boolean backspaceMerge = false;
 	private boolean deleteMerge = false;
 
 
 	// The Constructors
 	public TextKeyListener() {}
 
 	public void destroy() {
 		textArea = null;
 	}
 
 
 	// MouseListener Interface
  	public void mouseEntered(MouseEvent e) {}
  	public void mouseExited(MouseEvent e) {}
  	
  	public void mousePressed(MouseEvent e) {
  		textArea = (OutlinerCellRendererImpl) e.getComponent();
  		
  		// Shorthand
  		Node currentNode = textArea.node;
  		TreeContext tree = currentNode.getTree();
  		OutlineLayoutManager layout = tree.doc.panel.layout;
 
 		// This is detection for Solaris, I think mac does this too.
 		if (e.isPopupTrigger() && (currentNode.isAncestorSelected() || (tree.getEditingNode() == currentNode))) {
 			Outliner.macroPopup.show(e.getComponent(),e.getX(), e.getY());
 			e.consume();
 			return;
 		}
 
 		// This is to block clicks when a right click is generated in windows.
 		if ((PlatformCompatibility.isWindows()) && e.getModifiers() == InputEvent.BUTTON3_MASK) {
 			return;
 		}
 			
  		// Clear the selection
  		int selectionSize = tree.selectedNodes.size();
 		tree.clearSelection();
 		
 		// Clear any text selection in the node that was being edited
 		if ((selectionSize == 0) && (currentNode != tree.getEditingNode())) {
 			OutlinerCellRendererImpl renderer = layout.getUIComponent(tree.getEditingNode());
 			renderer.setCaretPosition(0);
 			renderer.moveCaretPosition(0);
 		}
 		
 		// Store the preferred caret position
 		tree.doc.setPreferredCaretPosition(textArea.getCaretPosition());
 		
 		// Record the EditingNode and CursorPosition and ComponentFocus
 		tree.setEditingNode(currentNode);
 		tree.setCursorPosition(textArea.getCaretPosition());
 		tree.setComponentFocus(OutlineLayoutManager.TEXT);
 		
 		// Redraw only if there is a current selection
 		if (selectionSize > 0) {
 			 // This doesn't use redraw() becuase it's important to do a full draw if the node 
 			 // is partially visible.
 			tree.doc.panel.layout.draw(currentNode,OutlineLayoutManager.TEXT);
 		}
 		
 		// Freeze Undo Editing
 		UndoableEdit.freezeUndoEdit(currentNode);
  	}
  	
  	public void mouseReleased(MouseEvent e) {
  		// Catch for Solaris/Mac if they did the popup trigger.
  		if (e.isConsumed()) {
  			return;
  		}
  		
  		textArea = (OutlinerCellRendererImpl) e.getComponent();
  		
 		// Shorthand
 		Node currentNode = textArea.node;
  		TreeContext tree = currentNode.getTree();
 
 		// This is detection for Windows
 		if (e.isPopupTrigger() && (currentNode.isAncestorSelected() || (tree.getEditingNode() == currentNode))) {
 			Outliner.macroPopup.show(e.getComponent(),e.getX(), e.getY());
 			return;
 		}
 	}
 
  	public void mouseClicked(MouseEvent e) {
   		// Catch for Solaris/Mac if they did the popup trigger.
  		if (e.isConsumed()) {
  			return;
  		}
  		
  		textArea = (OutlinerCellRendererImpl) e.getComponent();
  		
 		// Shorthand
 		Node currentNode = textArea.node;
  		TreeContext tree = currentNode.getTree();
 		
 		// Set the Mark
 		tree.setCursorMarkPosition(textArea.getCaret().getMark());
 		tree.setCursorPosition(textArea.getCaretPosition(),false);
 
 		// Lets record changes to the selection state into the current undoable if it is an UndoableEdit
 		UndoableEdit undoable = tree.doc.undoQueue.getIfEdit();
 		if ((undoable != null) && (undoable.getNode() == currentNode)) {
 			undoable.setNewPosition(textArea.getCaretPosition());
 			undoable.setNewMarkPosition(textArea.getCaret().getMark());
 		}	
  	}
 	
 	
 	// KeyListener Interface
 	public void keyPressed(KeyEvent e) {
 		textArea = (OutlinerCellRendererImpl) e.getComponent();
 		
 		//System.out.println("keyPressed");
 		
 		// Shorthand
 		Node currentNode = textArea.node;
 		TreeContext tree = currentNode.getTree();
 		OutlineLayoutManager layout = tree.doc.panel.layout;
 		
 		switch(e.getKeyCode()) {
 			case KeyEvent.VK_PAGE_DOWN:
 				toggleExpansion(tree, layout);
 				break;
 
 			case KeyEvent.VK_PAGE_UP:
 				if (e.isControlDown()) {
 					if (e.isShiftDown()) {
 						clearComment(tree, layout);
 					} else {
 						toggleCommentInheritance(tree,layout);
 					}
 				} else if (e.isShiftDown()) {
 					toggleComment(tree,layout);
 				} else {
 					toggleCommentAndClear(tree, layout);
 				}
 				break;
 
 			case KeyEvent.VK_F11:
 				if (e.isControlDown()) {
 					if (e.isShiftDown()) {
 						clearEditable(tree, layout);
 					} else {
 						toggleEditableInheritance(tree,layout);
 					}
 				} else if (e.isShiftDown()) {
 					toggleEditable(tree,layout);
 				} else {
 					toggleEditableAndClear(tree, layout);
 				}
 				break;
 
 			case KeyEvent.VK_F12:
 				if (e.isControlDown()) {
 					if (e.isShiftDown()) {
 						clearMoveable(tree, layout);
 					} else {
 						toggleMoveableInheritance(tree,layout);
 					}
 				} else if (e.isShiftDown()) {
 					toggleMoveable(tree,layout);
 				} else {
 					toggleMoveableAndClear(tree, layout);
 				}
 				break;
 
 			case KeyEvent.VK_UP:
 				moveUp(tree, layout);
 				break;
 
 			case KeyEvent.VK_DOWN:
 				moveDown(tree, layout);
 				break;
 
 			case KeyEvent.VK_LEFT:
 				if (textArea.getCaretPosition() == 0) {
 					moveLeftToPrevNode(tree, layout);
 					e.consume();
 				} else {
 					moveLeft(tree, layout);
 				}
 
 				// Freeze Undo Editing
 				UndoableEdit.freezeUndoEdit(currentNode);
 
 				return;
 
 			case KeyEvent.VK_RIGHT:
 				if (textArea.getCaretPosition() == textArea.getText().length()) {
 					moveRightToNextNode(tree, layout);
 					e.consume();
 				} else {
 					moveRight(tree, layout);
 				}
 				
 				// Freeze Undo Editing
 				UndoableEdit.freezeUndoEdit(currentNode);
 
 				return;
 
 			case KeyEvent.VK_BACK_SPACE:
 				// Abort if node is not editable
 				if (!currentNode.isEditable()) {
 					return;
 				}
 
 				backspaceMerge = false;
 				int caretPosition = textArea.getCaretPosition();
 				int markPosition = textArea.getCaret().getMark();
 
 				if ((caretPosition == 0) && (caretPosition == markPosition) && textArea.node.isLeaf()) {
 					mergeWithPrevVisibleNode(tree, layout);
 					e.consume();
 				}
 
 				return;
 
 			case KeyEvent.VK_DELETE:
 				// Abort if node is not editable
 				if (!currentNode.isEditable()) {
 					return;
 				}
 
 				caretPosition = textArea.getCaretPosition();
 				markPosition = textArea.getCaret().getMark();
 
 				if ((caretPosition == textArea.getText().length()) && (caretPosition == markPosition) && textArea.node.isLeaf()) {
 					mergeWithNextVisibleNode(tree, layout);
 					e.consume();
 				} else {
 					String oldText = currentNode.getValue();
 					String newText = null;
 					int oldCaretPosition = textArea.getCaretPosition();
 					int oldMarkPosition = textArea.getCaret().getMark();
 					int newCaretPosition = -1;
 					int newMarkPosition = -1;
 					
 					int startSelection = Math.min(oldCaretPosition, oldMarkPosition);
 					int endSelection = Math.max(oldCaretPosition, oldMarkPosition);
 					
 					if (startSelection != endSelection) {
 						newCaretPosition = startSelection;
 						newMarkPosition = startSelection;
 						newText = oldText.substring(0, startSelection) + oldText.substring(endSelection, oldText.length());				
 					} else if (startSelection == oldText.length()) {
 						newCaretPosition = oldText.length();
 						newMarkPosition = oldText.length();
 						newText = oldText;				
 					} else {
 						newCaretPosition = startSelection;
 						newMarkPosition = startSelection;
 						newText = oldText.substring(0, newCaretPosition) + oldText.substring(newCaretPosition + 1, oldText.length());				
 					}
 	
 					UndoableEdit undoable = tree.doc.undoQueue.getIfEdit();
 					if ((undoable != null) && (undoable.getNode() == currentNode) && (!undoable.isFrozen())) {
 						undoable.setNewText(newText);
 						undoable.setNewPosition(newCaretPosition);
 						undoable.setNewMarkPosition(newMarkPosition);
 					} else {
 						UndoableEdit newUndoable = new UndoableEdit(
 							currentNode, 
 							oldText, 
 							newText, 
 							oldCaretPosition, 
 							newCaretPosition, 
 							oldMarkPosition, 
 							newMarkPosition
 						);
 						tree.doc.undoQueue.add(newUndoable);
 					}
 	
 					currentNode.setValue(newText);
 				}
 
 				return;
 
 			case KeyEvent.VK_ENTER:
 				if (e.isControlDown()) {
 					split(tree,layout);
 				} else {
 					doInsert(textArea.node, tree,layout);
 				}
 				break;
 
 			case KeyEvent.VK_INSERT:
 				changeFocusToIcon(tree,layout);
 				break;
 
 			case KeyEvent.VK_TAB:
 				if (e.isShiftDown()) {
 					promote(tree,layout);
 				} else {
 					demote(tree,layout);
 				}
 				break;
 
 			case KeyEvent.VK_V:
 				if (e.isControlDown()) {
 					// Abort if node is not editable
 					if (!currentNode.isEditable()) {
 						e.consume();
 						return;
 					}
 				
 					paste(tree,layout);
 					if (!inlinePaste) {
 						e.consume();
 					}
 				}
 				return;
 
 			case KeyEvent.VK_M:
 				if (e.isControlDown()) {
 					break;
 				}
 				return;
 
 			case KeyEvent.VK_D:
 				if (e.isControlDown()) {
 					selectNone(tree,layout);
 				}
 				break;
 				
 			default:
 				// If we're read-only then abort
 				if (!currentNode.isEditable()) {
 					if (!e.isControlDown() && !e.isAltDown()) {
 						Outliner.outliner.getToolkit().beep();
 					}
 				}
 				return;
 		}
 		
 		e.consume();
 		return;
 	}
 	
 	public void keyTyped(KeyEvent e) {
 		textArea = (OutlinerCellRendererImpl) e.getComponent();
 
 		Node currentNode = textArea.node;
 		
 		// Abort if not editable
 		if (!currentNode.isEditable()) {
 			return;
 		}
 		
 		if(e.paramString().indexOf("Backspace") != -1) {
 			if (backspaceMerge) {
 				backspaceMerge = false;
 				e.consume();
 			} else {
 				TreeContext tree = currentNode.getTree();
 				
 				String oldText = currentNode.getValue();
 				String newText = null;
 				int oldCaretPosition = textArea.getCaretPosition();
 				int oldMarkPosition = textArea.getCaret().getMark();
 				int newCaretPosition = -1;
 				int newMarkPosition = -1;
 				
 				int startSelection = Math.min(oldCaretPosition, oldMarkPosition);
 				int endSelection = Math.max(oldCaretPosition, oldMarkPosition);
 				
 				if (startSelection != endSelection) {
 					newCaretPosition = startSelection;
 					newMarkPosition = startSelection;
 					newText = oldText.substring(0, startSelection) + oldText.substring(endSelection, oldText.length());				
 				} else if (startSelection == 0) {
 					newCaretPosition = 0;
 					newMarkPosition = 0;
 					newText = oldText;				
 				} else {
 					newCaretPosition = startSelection - 1;
 					newMarkPosition = startSelection - 1;
 					newText = oldText.substring(0, newCaretPosition) + oldText.substring(newCaretPosition + 1, oldText.length());				
 				}
 
 				UndoableEdit undoable = tree.doc.undoQueue.getIfEdit();
 				if ((undoable != null) && (undoable.getNode() == currentNode) && (!undoable.isFrozen())) {
 					undoable.setNewText(newText);
 					undoable.setNewPosition(newCaretPosition);
 					undoable.setNewMarkPosition(newMarkPosition);
 				} else {
 					UndoableEdit newUndoable = new UndoableEdit(
 						currentNode, 
 						oldText, 
 						newText, 
 						oldCaretPosition, 
 						newCaretPosition, 
 						oldMarkPosition, 
 						newMarkPosition
 					);
 					tree.doc.undoQueue.add(newUndoable);
 				}
 
 				currentNode.setValue(newText);
 			}
 		}
 	}
 	
 	public void keyReleased(KeyEvent e) {
 		textArea = (OutlinerCellRendererImpl) e.getComponent();
 
 		// Create some short names for convienence
 		Node currentNode = textArea.node;
 		TreeContext tree = currentNode.getTree();
 		OutlineLayoutManager layout = tree.doc.panel.layout;
 
 		// If we're read-only then abort
 		if (!currentNode.isEditable()) {
 			return;
 		}		
 		
 		// Let control-x and control-v slip through so that cut and paste will be recorded as undoable.
 		if (e.isControlDown()) {
 			if (e.isShiftDown()) {
 				return;
 			} else if ((e.getKeyCode() == KeyEvent.VK_X) || ((e.getKeyCode() == KeyEvent.VK_V) && inlinePaste)) {
 				// Do Nothing
 			} else {
 				return;
 			}
 		}
 		
 		// Keep unwanted keystrokes from effecting undoability.
 		boolean doUndo = true;
 		switch(e.getKeyCode()) {
 			case KeyEvent.VK_SHIFT:
 				return;
 			case KeyEvent.VK_CONTROL:
 				return;
 			case KeyEvent.VK_PAGE_UP:
 				return;
 			case KeyEvent.VK_PAGE_DOWN:
 				return;
 			case KeyEvent.VK_UP:
 				return;
 			case KeyEvent.VK_DOWN:
 				return;
 			case KeyEvent.VK_LEFT:
 				return;
 			case KeyEvent.VK_RIGHT:
 				return;
 			case KeyEvent.VK_ENTER:
 				return;
 			case KeyEvent.VK_INSERT:
 				return;
 			case KeyEvent.VK_TAB:
 				return;
 			// These keystrokes should not effect undoablility, but do 
 			// effect cursor position and redraw.
 			case KeyEvent.VK_BACK_SPACE: // Undoability already recorded in keyTyped.
 				doUndo = false;
 				break;
 			case KeyEvent.VK_DELETE: // Undoability already recorded in keyTyped.
 				doUndo = false;
 				break;
 			case KeyEvent.VK_HOME:
 				doUndo = false;
 				break;
 			case KeyEvent.VK_END:
 				doUndo = false;
 				break;
 		}
 
 		// Record some Values
 		int caretPosition = textArea.getCaretPosition();
 
 		if (doUndo) {
 			// Update the value in the node
 			String oldText = currentNode.getValue();
 			String newText = textArea.getText();
 			currentNode.setValue(newText);
 	
 			// Put the Undoable onto the UndoQueue
 			UndoableEdit undoable = tree.doc.undoQueue.getIfEdit();
 			if ((undoable != null) && (undoable.getNode() == currentNode) && (!undoable.isFrozen())) {
 				if (e.isControlDown() && ((e.getKeyCode() == KeyEvent.VK_X) || (e.getKeyCode() == KeyEvent.VK_V))) {
 					tree.doc.undoQueue.add(new UndoableEdit(currentNode, oldText, newText, tree.getCursorPosition(), caretPosition, tree.getCursorMarkPosition(), caretPosition));
 				} else {
 					undoable.setNewText(newText);
 					undoable.setNewPosition(caretPosition);
 					undoable.setNewMarkPosition(caretPosition);
 				}
 			} else {
 				tree.doc.undoQueue.add(new UndoableEdit(currentNode, oldText, newText, tree.getCursorPosition(), caretPosition, tree.getCursorMarkPosition(), caretPosition));
 			}
 		}
 
 		// Record the EditingNode, Mark and CursorPosition
 		tree.setEditingNode(currentNode);
 		tree.setCursorMarkPosition(textArea.getCaret().getMark());
 		tree.setCursorPosition(caretPosition, false);
 		tree.doc.setPreferredCaretPosition(caretPosition);
 
 		// Do the Redraw if we have wrapped or if we are currently off screen.
 		if (textArea.getPreferredSize().height != textArea.height || !currentNode.isVisible()) {
 			layout.draw(currentNode, OutlineLayoutManager.TEXT);
 		}
 	}
 
 
 	// Key Handlers
 	private void toggleExpansion(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		
 		if (currentNode.isExpanded()) {
 			currentNode.setExpanded(false);
 		} else {
 			currentNode.setExpanded(true);
 		}
 
 		// Redraw
 		layout.draw(currentNode, OutlineLayoutManager.TEXT);
 	}
 
 	// Comments
 	private void clearComment(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		CompoundUndoablePropertyChange undoable = new CompoundUndoablePropertyChange(tree);
 		IconKeyListener.clearCommentForSingleNode(currentNode, undoable);
 
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 		}
 
 		// Redraw
 		layout.draw(currentNode, OutlineLayoutManager.TEXT);
 	}
 
 	private void toggleCommentAndClear(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		CompoundUndoablePropertyChange undoable = new CompoundUndoablePropertyChange(tree);
 		IconKeyListener.toggleCommentAndClearForSingleNode(currentNode, undoable);
 
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 		}
 
 		// Redraw
 		layout.draw(currentNode, OutlineLayoutManager.TEXT);
 	}
 
 	private void toggleComment(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		CompoundUndoablePropertyChange undoable = new CompoundUndoablePropertyChange(tree);
 		IconKeyListener.toggleCommentForSingleNode(currentNode, undoable);
 
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 		}
 
 		// Redraw
 		layout.draw(currentNode, OutlineLayoutManager.TEXT);
 	}
 
 	private void toggleCommentInheritance(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		CompoundUndoablePropertyChange undoable = new CompoundUndoablePropertyChange(tree);
 		IconKeyListener.toggleCommentInheritanceForSingleNode(currentNode, undoable);
 
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 		}
 
 		// Redraw
 		layout.draw(currentNode, OutlineLayoutManager.TEXT);
 	}
 	
 	private void moveUp(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 
 		// Get Prev Node
 		Node prevNode = tree.getPrevNode(currentNode);
 		if (prevNode == null) {
 			return;
 		}
 
 		// Record the EditingNode and CursorPosition
 		tree.setEditingNode(prevNode);
 		tree.setCursorPosition(OutlinerDocument.findNearestCaretPosition(textArea.getCaretPosition(), tree.doc.getPreferredCaretPosition(), prevNode));
 			
 		// Clear Text Selection
 		textArea.setCaretPosition(0);
 		textArea.moveCaretPosition(0);
 
 		// Freeze Undo Editing
 		UndoableEdit.freezeUndoEdit(currentNode);
 
 		// Redraw and Set Focus
 		if (prevNode.isVisible()) {
 			layout.setFocus(prevNode,OutlineLayoutManager.TEXT);
 		} else {
 			layout.draw(prevNode,OutlineLayoutManager.TEXT);
 		}
 	}
 
 	private void moveDown(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 
 		// Get Prev Node
 		Node nextNode = tree.getNextNode(currentNode);
 		if (nextNode == null) {
 			return;
 		}
 
 		// Record the EditingNode and CursorPosition
 		tree.setEditingNode(nextNode);
 		tree.setCursorPosition(OutlinerDocument.findNearestCaretPosition(textArea.getCaretPosition(), tree.doc.getPreferredCaretPosition(), nextNode));
 		
 		// Clear Text Selection
 		textArea.setCaretPosition(0);
 		textArea.moveCaretPosition(0);
 
 		// Freeze Undo Editing
 		UndoableEdit.freezeUndoEdit(currentNode);
 
 		// Redraw and Set Focus
 		if (nextNode.isVisible()) {
 			layout.setFocus(nextNode,OutlineLayoutManager.TEXT);
 		} else {
 			layout.draw(nextNode,OutlineLayoutManager.TEXT);
 		}
 	}
 
 	private void moveLeft(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 
 		// Update Preferred Caret Position
 		tree.doc.setPreferredCaretPosition(textArea.getCaretPosition() - 1);
 
 		// Record the CursorPosition only since the EditingNode should not have changed
 		tree.setCursorPosition(textArea.getCaretPosition() - 1);
 
 		// Redraw and Set Focus if this node is currently offscreen
 		if (!currentNode.isVisible()) {
 			layout.draw(currentNode,OutlineLayoutManager.TEXT);
 		}
 	}
 
 	private void moveLeftToPrevNode(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 
 		// Get Prev Node
 		Node prevNode = tree.getPrevNode(currentNode);
 		if (prevNode == null) {
 			tree.setCursorPosition(tree.getCursorPosition()); // Makes sure we reset the mark
 			return;
 		}
 		
 		// Update Preferred Caret Position
 		int newLength = prevNode.getValue().length();
 		tree.doc.setPreferredCaretPosition(newLength);
 
 		// Record the EditingNode and CursorPosition
 		tree.setEditingNode(prevNode);
 		tree.setCursorPosition(newLength);
 
 		// Clear Text Selection
 		textArea.setCaretPosition(0);
 		textArea.moveCaretPosition(0);
 
 		// Redraw and Set Focus
 		if (prevNode.isVisible()) {
 			layout.setFocus(prevNode,OutlineLayoutManager.TEXT);
 		} else {
 			layout.draw(prevNode,OutlineLayoutManager.TEXT);
 		}
 	}
 
 	private void moveRight(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 
 		// Update Preferred Caret Position
 		tree.doc.setPreferredCaretPosition(textArea.getCaretPosition() + 1);
 
 		// Record the CursorPosition only since the EditingNode should not have changed
 		tree.setCursorPosition(textArea.getCaretPosition() + 1);
 
 		// Redraw and Set Focus if this node is currently offscreen
 		if (!currentNode.isVisible()) {
 			layout.draw(currentNode,OutlineLayoutManager.TEXT);
 		}
 	}
 
 	private void moveRightToNextNode(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 
 		// Get Prev Node
 		Node nextNode = tree.getNextNode(currentNode);
 		if (nextNode == null) {
 			tree.setCursorPosition(tree.getCursorPosition()); // Makes sure we reset the mark
 			return;
 		}
 	
 		// Update Preferred Caret Position
 		int newLength = nextNode.getValue().length();
 		tree.doc.setPreferredCaretPosition(newLength);
 
 		// Record the EditingNode and CursorPosition
 		tree.setEditingNode(nextNode);
 		tree.setCursorPosition(0);
 
 		// Clear Text Selection
 		textArea.setCaretPosition(0);
 		textArea.moveCaretPosition(0);
 
 		// Redraw and Set Focus
 		if (nextNode.isVisible()) {
 			layout.setFocus(nextNode,OutlineLayoutManager.TEXT);
 		} else {
 			layout.draw(nextNode,OutlineLayoutManager.TEXT);
 		}
 	}
 
 	private void changeFocusToIcon(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 
 		tree.setSelectedNodesParent(currentNode.getParent());
 		tree.addNodeToSelection(currentNode);
 
 		// Record the EditingNode and CursorPosition
 		tree.setComponentFocus(OutlineLayoutManager.ICON);
 
 		// Redraw and Set Focus
 		layout.draw(currentNode,OutlineLayoutManager.ICON);
 	}
 	
 	protected static void doInsert(Node node, TreeContext tree, OutlineLayoutManager layout) {
 
 		// Abort if node is not editable
 		if (!node.isEditable()) {
 			return;
 		}
 		
 		Node newNode = new NodeImpl(tree,"");
 		int newNodeIndex = 0;
 		Node newNodeParent = null;
 		
 		if ((!node.isLeaf()) && (node.isExpanded())) {
 			newNodeParent = node;
 			newNode.setDepth(node.getDepth() + 1);
 			node.insertChild(newNode, newNodeIndex);
 		} else {
 			newNodeIndex = node.currentIndex() + 1;
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
 		layout.draw(newNode, visibleIndex, OutlineLayoutManager.TEXT);	
 	}
 
 	private void mergeWithPrevVisibleNode(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		Node prevNode = tree.getPrevNode(currentNode);
 		if (prevNode == null) {
 			return;
 		}
 
 		// Abort if prevNode is not editable
 		if (!prevNode.isEditable()) {
 			return;
 		}
 				
 		Node parent = currentNode.getParent();
 
 		// Get Text for nodes.
 		String prevNodeText = prevNode.getValue();
 		String currentNodeText = currentNode.getValue();
 		String newPrevNodeText = prevNodeText + currentNodeText;
 
 		// Put the Undoable onto the UndoQueue
 		UndoableEdit undoableEdit = new UndoableEdit(
 			prevNode, 
 			prevNodeText, 
 			newPrevNodeText, 
 			0, 
 			prevNodeText.length(), 
 			0, 
 			prevNodeText.length()
 		);
 		
 		CompoundUndoableReplace undoableReplace = new CompoundUndoableReplace(parent);
 		undoableReplace.addPrimitive(new PrimitiveUndoableReplace(parent, currentNode, null));
 		
 		CompoundUndoableImpl undoable = new CompoundUndoableImpl(true);
 		undoable.addPrimitive(undoableReplace);
 		undoable.addPrimitive(undoableEdit);
 		
 		tree.doc.undoQueue.add(undoable);
 				
 		undoable.redo();
 		
 		backspaceMerge = true;
 	}
 
 	private void mergeWithNextVisibleNode(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		Node nextNode = tree.getNextNode(currentNode);
 		if (nextNode == null) {
 			return;
 		}
 
 		// Abort if nextNode is not editable
 		if (!nextNode.isEditable()) {
 			return;
 		}
 				
 		Node parent = currentNode.getParent();
 
 		// Get Text for nodes.
 		String nextNodeText = nextNode.getValue();
 		String currentNodeText = currentNode.getValue();
 		String newNextNodeText = currentNodeText + nextNodeText;
 
 		// Put the Undoable onto the UndoQueue
 		UndoableEdit undoableEdit = new UndoableEdit(
 			nextNode, 
 			nextNodeText, 
 			newNextNodeText, 
 			0, 
 			currentNodeText.length(), 
 			0, 
 			currentNodeText.length()
 		);
 		
 		CompoundUndoableReplace undoableReplace = new CompoundUndoableReplace(parent);
 		undoableReplace.addPrimitive(new PrimitiveUndoableReplace(parent, currentNode, null));
 		
 		CompoundUndoableImpl undoable = new CompoundUndoableImpl(true);
 		undoable.addPrimitive(undoableReplace);
 		undoable.addPrimitive(undoableEdit);
 		
 		tree.doc.undoQueue.add(undoable);
 				
 		undoable.redo();
 	}
 	
 	private void split(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 
 		// Get Text for nodes.
 		String oldText = currentNode.getValue();
 		String oldNodeText = currentNode.getValue().substring(0,textArea.getCaretPosition());
 		String newNodeText = currentNode.getValue().substring(textArea.getCaretPosition(), currentNode.getValue().length());
 		currentNode.setValue(oldNodeText);
 		
 		// Create a new node and insert it as a sibling immediatly after this node, unless
 		// the current node is expanded and has children. Then, we should insert it as the first child of the
 		// current node.
 		Node newNode = new NodeImpl(currentNode.getTree(),newNodeText);
 		
 		if ((!currentNode.isLeaf()) && (currentNode.isExpanded())) {
 			newNode.setDepth(currentNode.getDepth() + 1);
 			currentNode.insertChild(newNode,0);				
 		} else {
 			newNode.setDepth(currentNode.getDepth());
 			currentNode.getParent().insertChild(newNode,currentNode.currentIndex() + 1);
 		}
 		
 		tree.insertNode(newNode);
 
 		// Record the EditingNode and CursorPosition
 		tree.setEditingNode(newNode);
 		tree.setCursorPosition(0);
 
 		// Update Preferred Caret Position
 		tree.doc.setPreferredCaretPosition(0);
 
 		// Put the Undoable onto the UndoQueue
 		CompoundUndoableEdit undoableEdit = new CompoundUndoableEdit(tree);
 		undoableEdit.addPrimitive(new PrimitiveUndoableEdit(currentNode, oldText, oldNodeText));
 		
 		CompoundUndoableInsert undoableInsert = new CompoundUndoableInsert(newNode.getParent());
 		undoableInsert.addPrimitive(new PrimitiveUndoableInsert(newNode.getParent(),newNode,newNode.currentIndex()));
 		
 		CompoundUndoableImpl undoable = new CompoundUndoableImpl(true);
 		undoable.addPrimitive(undoableEdit);
 		undoable.addPrimitive(undoableInsert);
 		
 		tree.doc.undoQueue.add(undoable);
 
 		// Redraw and Set Focus
 		layout.draw(newNode,OutlineLayoutManager.TEXT);
 	}
 
 	private void selectNone(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 
 		// Clear Text Selection
 		int caretPosition = textArea.getCaretPosition();
 		tree.setCursorPosition(caretPosition);
 		textArea.setCaretPosition(caretPosition);
 		textArea.moveCaretPosition(caretPosition);
 		
 		// Update the undoable if neccessary
 		UndoableEdit undoable = tree.doc.undoQueue.getIfEdit();
 		if ((undoable != null) && (undoable.getNode() == currentNode) && (!undoable.isFrozen())) {
 			undoable.setNewPosition(caretPosition);
 			undoable.setNewMarkPosition(caretPosition);
 		}
 	}
 		
 	private void promote(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 
 		// Abort if node is not moveable
 		if (!currentNode.isMoveable()) {
 			return;
 		}
 
 		// Put the Undoable onto the UndoQueue
 		Node targetNode = currentNode.getParent().getParent();
 		int targetIndex = currentNode.getParent().currentIndex() + 1;
 		if (currentNode.getParent().isRoot()) {
 			// Our parent is root. Since we can't be promoted to root level, Abort.
 			return;
 		}
 		
 		CompoundUndoableMove undoable = new CompoundUndoableMove(currentNode.getParent(),targetNode);
 		tree.doc.undoQueue.add(undoable);
 
 		// Record the Insert in the undoable
 		int index = currentNode.currentIndex();
 		undoable.addPrimitive(new PrimitiveUndoableMove(undoable,currentNode,index,targetIndex));
 
 		tree.promoteNode(currentNode, index);
 
 		// Redraw and Set Focus
 		layout.draw(currentNode,OutlineLayoutManager.TEXT);
 	}
 
 	private void demote(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 
 		if (currentNode.isFirstChild()) {
 			return;
 		}
 
 		// Abort if node is not moveable
 		if (!currentNode.isMoveable()) {
 			return;
 		}
 		
 		// Put the Undoable onto the UndoQueue
 		Node targetNode = currentNode.prevSibling();
 
 		CompoundUndoableMove undoable = new CompoundUndoableMove(currentNode.getParent(), targetNode);
 		tree.doc.undoQueue.add(undoable);
 		
 		// Record the Insert in the undoable
 		int index = currentNode.currentIndex();
 		int targetIndex = targetNode.numOfChildren();
 		undoable.addPrimitive(new PrimitiveUndoableMove(undoable,currentNode,index,targetIndex));
 
 		tree.demoteNode(currentNode,targetNode, index);
 
 		// Redraw and Set Focus
 		layout.draw(currentNode,OutlineLayoutManager.TEXT);
 	}
 
 	private void paste(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 
 		inlinePaste = true;
 		
 		// Get the text from the clipboard and turn it into a tree
 		boolean isNodeSet = false;
 		String text = "";
 		NodeSet nodeSet = new NodeSet();
 		try {
 			Transferable selection = (Transferable) Outliner.clipboard.getContents(this);
 			if (selection != null) {
 				if (selection instanceof NodeSetTransferable) {
 					nodeSet = (NodeSet) selection.getTransferData(NodeSetTransferable.nsFlavor);
 					inlinePaste = false;
 					isNodeSet = true;
 				} else {
 					text = (String) selection.getTransferData(DataFlavor.stringFlavor);
 					
 					// Need to make a check for inline pastes
 					if ((text.indexOf(Preferences.LINE_END_STRING) == -1) && (text.indexOf(Preferences.DEPTH_PAD_STRING) == -1)) {
 						return;
 					} else {
 						inlinePaste = false;
 					}
 				}
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 
 
 		// Put the Undoable onto the UndoQueue
 		CompoundUndoableInsert undoable = new CompoundUndoableInsert(currentNode.getParent());
 		tree.doc.undoQueue.add(undoable);
 
 		Node parentForNewNode = currentNode.getParent();
 		int indexForNewNode = parentForNewNode.getChildIndex(currentNode);
 	
 		tree.setSelectedNodesParent(parentForNewNode);
 
 		if (isNodeSet) {
 			for (int i = nodeSet.getSize() - 1; i >= 0; i--) {
 				Node node = nodeSet.getNode(i);
 				node.setTree(tree, true);
 				parentForNewNode.insertChild(node, indexForNewNode + 1);
 				node.setDepthRecursively(parentForNewNode.getDepth() + 1);
 				tree.insertNode(node);
 
 				// Record the Insert in the undoable
 				int index = node.currentIndex() + i;
 				undoable.addPrimitive(new PrimitiveUndoableInsert(parentForNewNode,node,index));
 
 				tree.addNodeToSelection(node);
 			}
 		} else {
 			Node tempRoot = PadSelection.pad(text, tree, currentNode.getDepth(), Preferences.LINE_END_STRING);
 		
 			for (int i = tempRoot.numOfChildren() - 1; i >= 0; i--) {
 				Node node = tempRoot.getChild(i);
 				parentForNewNode.insertChild(node, indexForNewNode + 1);
 				tree.insertNode(node);
 
 				// Record the Insert in the undoable
 				int index = node.currentIndex() + i;
 				undoable.addPrimitive(new PrimitiveUndoableInsert(parentForNewNode,node,index));
 
 				tree.addNodeToSelection(node);
 			}
 		}
 			
 		Node nodeThatMustBeVisible = tree.getYoungestInSelection();
 
 		// Record the EditingNode and CursorPosition and ComponentFocus
 		tree.setEditingNode(nodeThatMustBeVisible);
 		tree.setCursorPosition(0);
 		tree.setComponentFocus(OutlineLayoutManager.ICON);
 
 		// Redraw and Set Focus
 		layout.draw(nodeThatMustBeVisible,OutlineLayoutManager.ICON);
 	}
 
 	// Additional Outline Methods
 	public static void hoist(Node currentNode) {
 		currentNode.getTree().doc.hoistStack.hoist(new HoistStackItem(currentNode));
 		return;
 	}
 
 	public static void dehoist(Node currentNode) {
 		currentNode.getTree().doc.hoistStack.dehoist();
 		return;
 	}
 
 	public static void dehoist_all(Node currentNode) {
 		currentNode.getTree().doc.hoistStack.dehoistAll();
 		return;
 	}
 
 	public static void expandAllSubheads(Node currentNode) {
 		currentNode.ExpandAllSubheads();
 		currentNode.getTree().doc.panel.layout.draw();
 		return;
 	}
 
 	public static void expandEverything(TreeContext tree) {
 		tree.rootNode.ExpandAllSubheads();
 		tree.doc.panel.layout.draw();
 		return;
 	}
 
 	public static void collapseToParent(Node currentNode) {
 		// Shorthand
 		TreeContext tree = currentNode.getTree();
 		OutlineLayoutManager layout = tree.doc.panel.layout;
 		
 		Node parent = currentNode.getParent();
 		if (parent.isRoot()) {
 			// Collapse
 			currentNode.CollapseAllSubheads();
 		
 			// Redraw and Set Focus
 			layout.draw(currentNode,OutlineLayoutManager.ICON);
 		} else {
 			// Collapse
 			parent.CollapseAllSubheads();
 			
 			// Record the EditingNode, Mark and CursorPosition
 			tree.setEditingNode(parent);
 			tree.setComponentFocus(OutlineLayoutManager.ICON);
 	
 			// Update Selection
 			tree.setSelectedNodesParent(parent.getParent());
 			tree.addNodeToSelection(parent);
 			
 			// Redraw and Set Focus
 			layout.draw(parent,OutlineLayoutManager.ICON);
 		}
 		return;
 	}
 
 	public static void collapseEverything(TreeContext tree) {
 		for (int i = 0; i < tree.rootNode.numOfChildren(); i++) {
 			((Node) tree.rootNode.getChild(i)).CollapseAllSubheads();
 		}
 
 		// Record the EditingNode, Mark and CursorPosition
 		Node firstNode = tree.rootNode.getFirstChild();
 		tree.setEditingNode(firstNode);
 		tree.setComponentFocus(OutlineLayoutManager.ICON);
 
 		// Update Selection
 		tree.setSelectedNodesParent(tree.rootNode);
 		tree.addNodeToSelection(firstNode);
 		
 		// Redraw and Set Focus
 		tree.doc.panel.layout.draw(firstNode,OutlineLayoutManager.ICON);
 		
 		return;
 	}
 
 	// Editable
 	private void clearEditable(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		CompoundUndoablePropertyChange undoable = new CompoundUndoablePropertyChange(tree);
 		IconKeyListener.clearEditableForSingleNode(currentNode, undoable);
 
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 		}
 
 		// Redraw
 		layout.draw(currentNode, OutlineLayoutManager.TEXT);
 	}
 
 	private void toggleEditableAndClear(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		CompoundUndoablePropertyChange undoable = new CompoundUndoablePropertyChange(tree);
 		IconKeyListener.toggleEditableAndClearForSingleNode(currentNode, undoable);
 
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 		}
 
 		// Redraw
 		layout.draw(currentNode, OutlineLayoutManager.TEXT);
 	}
 
 	private void toggleEditable(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		CompoundUndoablePropertyChange undoable = new CompoundUndoablePropertyChange(tree);
 		IconKeyListener.toggleEditableForSingleNode(currentNode, undoable);
 
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 		}
 
 		// Redraw
 		layout.draw(currentNode, OutlineLayoutManager.TEXT);
 	}
 
 	private void toggleEditableInheritance(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		CompoundUndoablePropertyChange undoable = new CompoundUndoablePropertyChange(tree);
 		IconKeyListener.toggleEditableInheritanceForSingleNode(currentNode, undoable);
 
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 		}
 
 		// Redraw
 		layout.draw(currentNode, OutlineLayoutManager.TEXT);
 	}
 
 
 	// Moveable
 	private void clearMoveable(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		CompoundUndoablePropertyChange undoable = new CompoundUndoablePropertyChange(tree);
 		IconKeyListener.clearMoveableForSingleNode(currentNode, undoable);
 
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 		}
 
 		// Redraw
 		layout.draw(currentNode, OutlineLayoutManager.TEXT);
 	}
 
 	private void toggleMoveableAndClear(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		CompoundUndoablePropertyChange undoable = new CompoundUndoablePropertyChange(tree);
 		IconKeyListener.toggleMoveableAndClearForSingleNode(currentNode, undoable);
 
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 		}
 
 		// Redraw
 		layout.draw(currentNode, OutlineLayoutManager.TEXT);
 	}
 
 	private void toggleMoveable(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		CompoundUndoablePropertyChange undoable = new CompoundUndoablePropertyChange(tree);
 		IconKeyListener.toggleMoveableForSingleNode(currentNode, undoable);
 
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 		}
 
 		// Redraw
 		layout.draw(currentNode, OutlineLayoutManager.TEXT);
 	}
 
 	private void toggleMoveableInheritance(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		CompoundUndoablePropertyChange undoable = new CompoundUndoablePropertyChange(tree);
 		IconKeyListener.toggleMoveableInheritanceForSingleNode(currentNode, undoable);
 
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 		}
 
 		// Redraw
 		layout.draw(currentNode, OutlineLayoutManager.TEXT);
 	}
 }
