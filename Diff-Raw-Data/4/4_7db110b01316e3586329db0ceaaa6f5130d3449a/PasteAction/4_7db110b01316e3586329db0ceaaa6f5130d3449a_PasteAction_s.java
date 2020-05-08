 /**
  * Copyright (C) 2002 Maynard Demmon, maynard@organic.com
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or 
  * without modification, are permitted provided that the 
  * following conditions are met:
  * 
  *  - Redistributions of source code must retain the above copyright 
  *    notice, this list of conditions and the following disclaimer. 
  * 
  *  - Redistributions in binary form must reproduce the above 
  *    copyright notice, this list of conditions and the following 
  *    disclaimer in the documentation and/or other materials provided 
  *    with the distribution. 
  * 
  *  - Neither the names "Java Outline Editor", "JOE" nor the names of its 
  *    contributors may be used to endorse or promote products derived 
  *    from this software without specific prior written permission. 
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
  * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
  * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
  * POSSIBILITY OF SUCH DAMAGE.
  */
  
 package com.organic.maynard.outliner.actions;
 
 import com.organic.maynard.outliner.*;
 import com.organic.maynard.outliner.util.preferences.*;
 import com.organic.maynard.outliner.util.undo.*;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.Window;
 import java.awt.datatransfer.*;
 
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.text.*;
 
 import com.organic.maynard.util.string.*;
 
 /**
  * @author  $Author$
  * @version $Revision$, $Date$
  */
  
 public class PasteAction extends AbstractAction {
 
 	public void actionPerformed(ActionEvent e) {
 		//System.out.println("PasteAction");
 		
 		OutlinerCellRendererImpl textArea  = null;
 		boolean isIconFocused = true;
 		Component c = (Component) e.getSource();
 		if (c instanceof OutlineButton) {
 			textArea = ((OutlineButton) c).renderer;
 		} else if (c instanceof OutlineLineNumber) {
 			textArea = ((OutlineLineNumber) c).renderer;
 		} else if (c instanceof OutlineCommentIndicator) {
 			textArea = ((OutlineCommentIndicator) c).renderer;
 		} else if (c instanceof OutlinerCellRendererImpl) {
 			textArea = (OutlinerCellRendererImpl) c;
 			isIconFocused = false;
 		}
 		
 		// Shorthand
 		Node node = textArea.node;
 		JoeTree tree = node.getTree();
 		OutlineLayoutManager layout = tree.getDocument().panel.layout;
 
 		//System.out.println(e.getModifiers());
 		switch (e.getModifiers()) {
 			case 2:
 				if (isIconFocused) {
 					paste(textArea, tree, layout);
 				} else {
 					pasteText(textArea, tree, layout);
 				}
 				break;
 		}
 	}
 
 
 	// KeyFocusedMethods
 	public static void pasteText(OutlinerCellRendererImpl textArea, JoeTree tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		OutlinerDocument doc = tree.getDocument();
 
 		// Get the text from the clipboard and turn it into a tree
 		boolean isNodeSet = false;
 		String text = "";
 		NodeSet nodeSet = new NodeSet();
 		try {
 			Transferable selection = (Transferable) Outliner.clipboard.getContents(null);
 			if (selection != null) {
 				if (selection instanceof NodeSetTransferable) {
 					nodeSet = (NodeSet) selection.getTransferData(NodeSetTransferable.nsFlavor);
 					isNodeSet = true;
 				} else {
 					text = (String) selection.getTransferData(DataFlavor.stringFlavor);
 					
 					// Need to make a check for inline pastes
 					if ((text.indexOf(Preferences.LINE_END_STRING) == -1) && (text.indexOf(Preferences.DEPTH_PAD_STRING) == -1)) {
 						String oldText = textArea.getText();
 						int oldCaretPosition = textArea.getCaretPosition();
 						int oldMarkPosition = textArea.getCaret().getMark();
 
 						String newText = new StringBuffer().append(oldText.substring(0,textArea.getSelectionStart())).append(text).append(oldText.substring(textArea.getSelectionEnd(),oldText.length())).toString();
 						int newCaretPosition = textArea.getSelectionStart() + text.length();
 						doc.getUndoQueue().add(new UndoableEdit(currentNode, oldText, newText, oldCaretPosition, newCaretPosition, oldMarkPosition, newCaretPosition));
 						UndoableEdit.freezeUndoEdit(currentNode);
 						
 						textArea.node.setValue(newText);
 						textArea.setText(newText);
 						textArea.setCaretPosition(newCaretPosition);
 						textArea.moveCaretPosition(newCaretPosition);
 
 						// Record the EditingNode, Mark and CursorPosition
 						tree.setEditingNode(currentNode);
 						tree.setCursorMarkPosition(newCaretPosition);
 						tree.setCursorPosition(newCaretPosition, false);
 						tree.getDocument().setPreferredCaretPosition(newCaretPosition);
 
 						// Do the Redraw if we have wrapped or if we are currently off screen.
 						if (textArea.getPreferredSize().height != textArea.height || !currentNode.isVisible()) {
 							layout.draw(currentNode, OutlineLayoutManager.TEXT);
 						}
 						
 						return;
 					}
 				}
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		
 		// We're not doing an inline paste.
 
 		// Figure out where to do the insert
 		Node parentForNewNode = null;
 		int indexForNewNode = 0;
 		int depth = 0;
 
 		if ((!currentNode.isLeaf()) && (currentNode.isExpanded())) {
 			parentForNewNode = currentNode;
 			depth = parentForNewNode.getDepth() + 1;
 			indexForNewNode = 0;
 		} else {
 			parentForNewNode = currentNode.getParent();
 			depth = currentNode.getDepth();
 			indexForNewNode = currentNode.currentIndex() + 1;
 		}
 
 		tree.setSelectedNodesParent(parentForNewNode);
 
 		// Put the Undoable onto the UndoQueue
 		CompoundUndoableInsert undoable = new CompoundUndoableInsert(parentForNewNode);
 		doc.getUndoQueue().add(undoable);
 
 		if (isNodeSet) {
 			for (int i = nodeSet.getSize() - 1; i >= 0; i--) {
 				Node node = nodeSet.getNode(i);
 				node.setTree(tree, true);
 				parentForNewNode.insertChild(node, indexForNewNode);
 				node.setDepthRecursively(depth);
 				tree.insertNode(node);
 
 				// Record the Insert in the undoable
 				int index = node.currentIndex() + i;
 				undoable.addPrimitive(new PrimitiveUndoableInsert(parentForNewNode, node, index));
 
 				tree.addNodeToSelection(node);
 			}
 		} else {
 			Node tempRoot = PadSelection.pad(text, tree, depth, Preferences.LINE_END_STRING);
 		
 			for (int i = tempRoot.numOfChildren() - 1; i >= 0; i--) {
 				Node node = tempRoot.getChild(i);
 				parentForNewNode.insertChild(node, indexForNewNode);
 				tree.insertNode(node);
 
 				// Record the Insert in the undoable
 				int index = node.currentIndex() + i;
 				undoable.addPrimitive(new PrimitiveUndoableInsert(parentForNewNode, node, index));
 
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
 		
 
 	// IconFocusedMethods
 	public static void paste(OutlinerCellRendererImpl textArea, JoeTree tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 
 		// Abort if node is not editable
 		if (!tree.getOldestInSelection().isEditable()) {
 			return;
 		}
 			
 		// Get the text from the clipboard and turn it into a tree
 		boolean isNodeSet = false;
 		String text = "";
 		NodeSet nodeSet = new NodeSet();
 		try {
 			Transferable selection = (Transferable) Outliner.clipboard.getContents(null);
 			if (selection != null) {
 				if (selection instanceof NodeSetTransferable) {
 					nodeSet = (NodeSet) selection.getTransferData(NodeSetTransferable.nsFlavor);
 					isNodeSet = true;
 				} else {
 					text = (String) selection.getTransferData(DataFlavor.stringFlavor);
 				}
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 
 		// Figure out where to do the insert
 		Node oldestNode = tree.getOldestInSelection();
 		Node parentForNewNode = oldestNode.getParent();
 		int indexForNewNode = oldestNode.currentIndex() + 1;
 		int depth = oldestNode.getDepth();
 
 		tree.clearSelection();
 		tree.setSelectedNodesParent(parentForNewNode);
 
 		// Put the Undoable onto the UndoQueue
 		CompoundUndoableInsert undoable = new CompoundUndoableInsert(parentForNewNode);
 		
 		if (isNodeSet) {
 			for (int i = nodeSet.getSize() - 1; i >= 0; i--) {
 				Node node = nodeSet.getNode(i);
 				node.setTree(tree, true);
 				parentForNewNode.insertChild(node, indexForNewNode);
 				node.setDepthRecursively(depth);
 				tree.insertNode(node);
 
 				// Record the Insert in the undoable
 				int index = node.currentIndex() + i;
 				undoable.addPrimitive(new PrimitiveUndoableInsert(parentForNewNode, node, index));
 
 				tree.addNodeToSelection(node);
 			}
 		} else {
 			Node tempRoot = PadSelection.pad(text, tree, depth, Preferences.LINE_END_STRING);
 		
 			for (int i = tempRoot.numOfChildren() - 1; i >= 0; i--) {
 				Node node = tempRoot.getChild(i);
 				parentForNewNode.insertChild(node, indexForNewNode);
 				tree.insertNode(node);
 
 				// Record the Insert in the undoable
 				int index = node.currentIndex() + i;
 				undoable.addPrimitive(new PrimitiveUndoableInsert(parentForNewNode, node, index));
 
 				tree.addNodeToSelection(node);
 			}
 		}
 
 		tree.getDocument().getUndoQueue().add(undoable);
 
 		Node nodeThatMustBeVisible = tree.getYoungestInSelection();
 
 		// Record the EditingNode and CursorPosition and ComponentFocus
 		tree.setEditingNode(nodeThatMustBeVisible);
 
 		// Redraw and Set Focus
 		layout.draw(nodeThatMustBeVisible, OutlineLayoutManager.ICON);
 	}
 }
