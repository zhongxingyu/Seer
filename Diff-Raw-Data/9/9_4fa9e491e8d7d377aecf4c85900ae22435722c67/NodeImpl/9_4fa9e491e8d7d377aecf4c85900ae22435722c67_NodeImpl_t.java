 /**
  * Copyright (C) 2000, 2001 Maynard Demmon, maynard@organic.com
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
  
 package com.organic.maynard.outliner;
 
 import com.organic.maynard.outliner.util.preferences.*;
 import java.util.*;
 import java.awt.*;
 
 /**
  * @author  $Author$
  * @version $Revision$, $Date$
  */
  
 public class NodeImpl extends AttributeContainerImpl implements Node {
 
 	// Constants
 	private static final int INITIAL_ARRAY_LIST_SIZE = 10;
 	public static final String KEY_CREATED = "created";
 	public static final String KEY_MODIFIED = "modified";
 	
 	// Class Fields
 	//public static boolean isSettingCreateModDates = false;
 	//private static SimpleDateFormat dateFormat = null;
 		
 	// Instance Fields		
 	private JoeTree tree = null;
 	private Node parent = null;
 	private String value = null;
 	protected JoeNodeList children = null;
 
 	private int depth = -1; // -1 so that children of root will be depth 0.
 	
 	private boolean expanded = false;
 	private boolean visible = false;
 	private boolean partiallyVisible = false;
 	private boolean selected = false;
 	private boolean hoisted = false;
 
 	private int commentState = Node.COMMENT_INHERITED;
 	private int editableState = Node.EDITABLE_INHERITED;
 	private int moveableState = Node.MOVEABLE_INHERITED;
 	
 	protected int decendantCount = 0;
 	private int decendantCharCount = 0;
 	
 
 	// The Constructors
 	public NodeImpl(JoeTree tree, String value) {
		
 		this.tree = tree;
 		this.value = value;
 		
 		// Set Creation Date
		if (tree != null && tree.getDocument() != null && tree.getDocument().getSettings().getUseCreateModDates().cur) {
 			setAttribute(KEY_CREATED, tree.getDocument().settings.dateFormat.format(new Date()), true);
 		}
			
	} // end constructor NodeImpl
	
 
 	public void destroy() {
 		if (children != null) {
 			for (int i = 0; i < children.size(); i++) {
 				Node child = children.get(i);
 				child.destroy();
 			}
 		}
 		
 		tree = null;
 		parent = null;
 		value = null;
 		children = null;
 	}
 
 	// Explicit Cloning Method
 	public Node cloneClean() {
 		NodeImpl nodeImpl = new NodeImpl(tree,value);
 		
 		nodeImpl.setDepth(depth);
 		nodeImpl.setCommentState(commentState);
 		
 		// clone the attributes
 		Iterator it = getAttributeKeys();
 		if (it != null) {
 			while (it.hasNext()) {
 				String key = (String) it.next();
 				Object value = getAttribute(key); // this should be a clone, but that's impossible!!! Eventually we'll need a copyable interface to make this work.
 				nodeImpl.setAttribute(key, value);
 			}
 		}
 		
 		// And clone the children
 		int childCount = numOfChildren();
 		for (int i = 0; i < childCount; i++) {
 			nodeImpl.insertChild(getChild(i).cloneClean(), i);
 		}
 		
 		return nodeImpl;
 	}
 
 
 	// Statistics Methods
 	private int lineNumber = -1;
 	private int lineNumberUpdateKey = -1;
 	
 	public void setLineNumber(int lineNumber) {
 		this.lineNumber = lineNumber;
 	}
 
 	public void setLineNumberKey(int lineNumberUpdateKey) {
 		this.lineNumberUpdateKey = lineNumberUpdateKey;
 	}
 	
 	public int getLineNumber() {
 		// This for when we need to get a line number but we don't want the
 		// key to get in the way. -2 will never be a normal key for things.
 		return getLineNumber(-2);
 	}
 	
 	public int getLineNumber(int key) {
 		if (lineNumberUpdateKey == key) {
 			return lineNumber;
 		}
 		
 		Node next = tree.getVisibleNodes().get(0);
 		int runningTotal = 0;
 		
 		int siblingCount = 0;
 		
 		while (true) {
 			runningTotal++;
 
 			next.setLineNumber(runningTotal);
 			next.setLineNumberKey(key);
 				
 			if (this.isDecendantOf(next)) {
 				if (next == this) {
 					break;
 				} else {
 					siblingCount = 0;
 					next = next.getChild(siblingCount);
 				}
 			} else {
 				runningTotal += next.getDecendantCount();
 				siblingCount++;
 				next = next.getParent().getChild(siblingCount);
 			}
 		}
 		
 		return lineNumber;		
 	}
 	
 	public void adjustDecendantCount(int amount) {
 		NodeImpl node = this;
 		while(!node.isRoot()) {
 			node.decendantCount += amount;
 			node = (NodeImpl) node.getParent();
 		}
 	}
 	
 	public void adjustDecendantCharCount(int amount) {
 		decendantCharCount += amount;
 		if (!isRoot()) {
 			getParent().adjustDecendantCharCount(amount);
 		}
 	}
 	
 	public int getDecendantCount() {
 		return decendantCount;
 	}
 	
 	public int getDecendantCharCount() {
 		if (isLeaf()) {
 			return 0;
 		} else {
 			int count = 0;
 			for (int i = 0; i < numOfChildren(); i++) {
 				count += getChild(i).getValue().length();
 				count += getChild(i).getDecendantCharCount();
 			}
 			return count;
 		}
 	}
 	
 	// Parent Methods
 	public void setParent(Node node) {
 		this.parent = node;
 	}
 	
 	public Node getParent() {
 		return parent;
 	}
 	
 	// Child Methods
 	public int numOfChildren() {
 		if (children != null) {
 			return children.size();
 		} else {
 			return 0;
 		}
 	}
 	
 	public void appendChild(Node node) {
 		if (children == null) {
 			children = Outliner.newNodeList(INITIAL_ARRAY_LIST_SIZE);
 		}
 		
 		children.add(node);
 		node.setParent(this);
 		
 		// Set the childs Depth
 		node.setDepth(getDepth() + 1);
 		
 		// Adjust Counts
 		adjustDecendantCount(node.getDecendantCount() + 1);
 	}
 	
 	public void removeChild(Node node) {
 		if (children == null) {
 			return;
 		}
 		
 		node.setParent(null);
 		children.remove(children.indexOf(node));
 
 		// Adjust Counts
 		adjustDecendantCount(-(node.getDecendantCount() + 1));
 	}
 
 	public void removeChild(Node node, int index) {
 		if (children == null) {
 			return;
 		}
 		
 		node.setParent(null);
 		children.remove(index);
 
 		// Adjust Counts
 		adjustDecendantCount(-(node.getDecendantCount() + 1));
 	}
 		
 	public Node getChild(int i) {
 		if (children == null) {
 			return null;
 		}
 		
 		try {
 			return children.get(i);
 		} catch (IndexOutOfBoundsException iofbe) {
 			return null;
 		}
 	}
 
 	public Node getFirstChild() {
 		if (children == null) {
 			return null;
 		}
 	
 		if (isLeaf()) {
 			return null;
 		} else {
 			return children.get(0);
 		}
 	}
 	
 	public Node getLastChild() {
 		if (children == null) {
 			return null;
 		}
 
 		if (isLeaf()) {
 			return null;
 		} else {
 			return children.get(children.size() - 1);
 		}
 	}
 	
 	public Node getLastDecendent() {
 		Node node = this;
 		Node child = getLastChild();
 				
 		while (child != null) {
 			node = child;
 			child = node.getLastChild();
 		}
 		
 		return node;
 	}
 
 	public Node getLastViewableDecendent() {
 		// Shortcut since most calls should exit here.
 		if (!isExpanded()) {
 			return this;
 		}
 		
 		Node node = this;
 		Node child = node.getLastChild();
 		
 		while (node.isExpanded() && child != null) {
 			node = child;
 			child = node.getLastChild();		
 		}
 		
 		return node;
 	}
 	
 	public void insertChild(Node node, int i) {
 		if (children == null) {
 			children = Outliner.newNodeList(INITIAL_ARRAY_LIST_SIZE);
 		}
 
 		children.add(i,node);
 		node.setParent(this);
 
 		// Adjust Counts
 		adjustDecendantCount(node.getDecendantCount() + 1);
 	}
 	
 	public int getChildIndex(Node node) {
 		if (children == null) {
 			return -1;
 		}
 
 		return children.indexOf(node);
 	}
 	
 	public boolean isLeaf() {
 		if (children == null) {
 			return true;
 		}
 
 		if (children.isEmpty()) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public boolean isRoot() {
 		if (getParent() == null) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	// Tree Accessor Methods
 	public JoeTree getTree() {
 		return tree;
 	}
 	
 	public void setTree(JoeTree tree, boolean recursive) {
 		this.tree = tree;
 		
 		if (recursive) {
 			for (int i = 0; i < numOfChildren(); i++) {
 				getChild(i).setTree(tree, true);
 			}
 		}
 	}
 	
 	
 	// Visibility Methods	
 	public void setVisible(boolean visible) {
 		this.visible = visible;
 	}
 	
 	public boolean isVisible() {
 		return visible;
 	}
 
 	// Comment Methods	
 	public void setCommentState(int commentState) {
 		this.commentState = commentState;
 	}
 	
 	public int getCommentState() {
 		return commentState;
 	}
 	
 	public boolean isComment() {
 		Node node = this;
 		
 		while (!node.isRoot()) {
 			if (node.getCommentState() == Node.COMMENT_TRUE) {
 				return true;
 			} else if (node.getCommentState() == Node.COMMENT_FALSE) {
 				return false;
 			}		
 			node = node.getParent();
 		}
 		
 		return getTree().getRootNodeCommentState();
 	}
 
 	// Editability Methods
 	public void setEditableState(int editableState) {
 		this.editableState = editableState;
 	}
 	
 	public int getEditableState() {
 		return editableState;
 	}
 
 	public boolean isEditable() {
 		Node node = this;
 		
 		while (!node.isRoot()) {
 			if (node.getEditableState() == Node.EDITABLE_TRUE) {
 				return true;
 			} else if (node.getEditableState() == Node.EDITABLE_FALSE) {
 				return false;
 			}		
 			node = node.getParent();
 		}
 		
 		return getTree().getRootNodeEditableState();
 	}
 	
 	// Moveability Methods
 	public void setMoveableState(int moveableState) {
 		this.moveableState = moveableState;
 	}
 	
 	public int getMoveableState() {
 		return moveableState;
 	}
 
 	public boolean isMoveable() {
 		Node node = this;
 		
 		while (!node.isRoot()) {
 			if (node.getMoveableState() == Node.MOVEABLE_TRUE) {
 				return true;
 			} else if (node.getMoveableState() == Node.MOVEABLE_FALSE) {
 				return false;
 			}		
 			node = node.getParent();
 		}
 		
 		return getTree().getRootNodeMoveableState();
 	}
 	
 	// Hoisting Methods
 	public void setHoisted(boolean hoisted) {
 		this.hoisted = hoisted;
 	}
 	
 	public boolean isHoisted() {
 		return hoisted;
 	}
 	
 	public Node getHoistedAncestorOrSelf() {
 		if (isRoot()) {
 			return null;
 		} else if (isHoisted()) {
 			return this;
 		} else {
 			return getParent().getHoistedAncestorOrSelf();
 		}
 	}
 
 	// Selection Methods	
 	public void setSelected(boolean selected) {
 		this.selected = selected;
 	}
 	
 	public boolean isSelected() {
 		return selected;
 	}
 
 	public boolean isAncestorSelected() {
 		Node node = this;
 		
 		while (!node.isRoot()) {
 			if (node.isSelected()) {
 				return true;
 			}
 			node = node.getParent();
 		}
 		
 		return false;
 	}
 	
 	// Is this a decendant of node?
 	public boolean isDecendantOf(Node decendant) {
 		Node node = this;
 		
 		while (!node.isRoot()) {
 			if (node == decendant) {
 				return true;
 			}
 			node = node.getParent();
 		}
 		
 		return false;
 	}
 
 	// Depth Methods
 	public void setDepth(int depth) {
 		this.depth = depth;
 	}
 	
 	public int getDepth() {
 		return depth;
 	}
 	
 	public void setDepthRecursively(int depth) {
 		setDepth(depth);
 		int childrenCount = numOfChildren();
 		for (int i = 0; i < childrenCount; i++) {
 			getChild(i).setDepthRecursively(depth + 1);
 		}						
 	}
 	
 	// Navigation Methods	
 	public void setExpandedClean(boolean expanded) {
 		this.expanded = expanded;
 	}
 	
 	public void setExpanded(boolean expanded) {
 		setExpanded(expanded, true);
 	}
 	
 	public void setExpanded(boolean expanded, boolean collapseChildrenWhenCollapsing) {
 		if (expanded == isExpanded()) {
 			// Since we have not changed state, Abort.
 			return;
 		}
 		
 		this.expanded = expanded;
 		
 		if (isLeaf()) {
 			return;
 		}
 		
 		int childCount = this.numOfChildren();
 		if (isExpanded()) {
 			int index = tree.getVisibleNodes().indexOf(this) + 1;
 			for (int i = childCount - 1; i >= 0; i--) {
 				tree.insertNodeAndChildren(getChild(i), index);
 			}			
 		} else {
 			for (int i = 0; i < childCount; i++) {
 				Node child = getChild(i);
 				tree.removeNode(child);
 				if (collapseChildrenWhenCollapsing) {
 					if (child.isExpanded()) {
 						child.setExpanded(false);
 					}
 				}
 			}		
 		}
 	}
 	
 	public boolean isExpanded() {
 		return this.expanded;
 	}
 
 	public void ExpandAllSubheads() {
 		setExpanded(true);
 		for (int i = 0; i < this.numOfChildren(); i++) {
 			getChild(i).ExpandAllSubheads();
 		}				
 	}
 
 	public void CollapseAllSubheads() {
 		setExpanded(false);
 		for (int i = 0; i < this.numOfChildren(); i++) {
 			getChild(i).CollapseAllSubheads();
 		}				
 	}
 
 	public void expandAllAncestors() {
 		Node parent = getParent();
 		
 		while (!parent.isRoot()) {
 			parent.setExpandedClean(true);
 			parent = parent.getParent();		
 		}
 	}
 
 	public int currentIndex() {
 		Node parent = getParent();
 		if (parent == null) {
 			return -1;
 		} else {
 			return parent.getChildIndex(this);
 		}
 	}
 	
 	public boolean isFirstChild() {
 		if (currentIndex() == 0) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public boolean isLastChild() {
 		if (currentIndex() == (getParent().numOfChildren() - 1)) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public Node nextSibling() {
 		if (getParent() == null) {
 			return this;
 		}
 		
 		Node node = getParent().getChild(currentIndex() + 1);
 
 		if(node == null) {
 			return this;
 		} else {
 			return node;
 		}
 	}
 	
 	public Node prevSibling() {
 		if (getParent() == null) {
 			return this;
 		}
 
 		Node node = getParent().getChild(currentIndex() - 1);
 		
 		if (node == null) {
 			return this;
 		} else {
 			return node;
 		}	
 	}
 	
 	public Node prevSiblingOrParent() {
 		Node node = prevSibling();
 		if (node == this) {
 			node = getParent();
 			if (node == null || node.isRoot()) {
 				return this;
 			}
 		}
 		return node;		
 	}
 	
 	public Node next() {
 		if (isExpanded() && !isLeaf()) {
 			return getChild(0);
 		} else {
 			Node node = nextSibling();
 			if (node == this) {
 				return nextSiblingOfAnyParent(node);
 			} else {
 				return node;
 			}
 		}
 	}
 
 	public Node nextNode() {
 		if (!isLeaf()) {
 			return getChild(0);
 		} else {
 			Node node = nextSibling();
 			if (node == this) {
 				return nextSiblingOfAnyParent(node);
 			} else {
 				return node;
 			}
 		}
 	}
 	
 	public Node nextUnSelectedNode() {
 		// This does not test the current node.
 		Node node = next();
 		
 		while (node.isAncestorSelected()) {
 			node = node.next();
 		}
 		
 		return node;
 	}
 
 	public Node nextSelectedSibling() {
 		// This does not test the current node.
 		Node node = nextSibling();
 
 		if (node == this) {
 			return null;
 		}
 				
 		while (!node.isAncestorSelected()) {
 			node = node.nextSibling();
 
 			if (node == this) {
 				return null;
 			}
 		}
 		
 		return node;
 	}
 	
 	private static Node nextSiblingOfAnyParent(Node node) {
 		Node parent = node.getParent();
 		if (parent == null) {
 			return node;
 		}	
 		Node nextSiblingOfParent = parent.nextSibling();
 		
 		while (parent == nextSiblingOfParent) {
 			parent = nextSiblingOfParent.getParent();
 			if (parent == null) {
 				return nextSiblingOfParent;
 			}
 			nextSiblingOfParent = parent.nextSibling();
 		}
 		
 		return nextSiblingOfParent;
 	}
 	
 	public Node prev() {
 		Node node = prevSibling();
 		if (node == this) {
 			if (getParent() == null) {
 				return node;
 			} else {
 				return getParent();
 			}
 		} else {
 			return node.getLastViewableDecendent();
 		}
 	}
 
 	public Node prevUnSelectedNode() {
 		// This does not test the current node.
 		Node node = prev();
 		
 		while(node.isAncestorSelected()) {
 			node = node.prev();
 		}
 		
 		return node;
 	}
 
 	public Node prevSelectedSibling() {
 		// This does not test the current node.
 		Node node = prevSibling();
 
 		if (node == this) {
 			return null;
 		}
 				
 		while (!node.isAncestorSelected()) {
 			node = node.prevSibling();
 
 			if (node == this) {
 				return null;
 			}
 		}
 		
 		return node;
 	}
 
 	// Data Methods
 	public void setValue(String value) {
 		this.value = value;
 		
 		// Set Modified Date
 		if (tree.getDocument() != null && tree.getDocument().settings.getUseCreateModDates().cur) {
 			setAttribute(KEY_MODIFIED, tree.getDocument().settings.dateFormat.format(new Date()), true);
 		}
 	}
 	public String getValue() {return value;}
 
 
 	// String Representation Methods	
 	public void depthPaddedValue(StringBuffer buf, String lineEndString) {
 		if (!isRoot()) {
 			for (int i = 0; i < this.depth; i++) {
 				buf.append(Preferences.DEPTH_PAD_STRING);
 			}
 			buf.append(getValue()).append(lineEndString);
 		}
 		
 		// Recursive Part
 		int childCount = numOfChildren();
 		for (int i = 0; i < childCount; i++) {
 			getChild(i).depthPaddedValue(buf, lineEndString);
 		}
 	}
 
 	public void getRecursiveValue(StringBuffer buf, String lineEndString, boolean includeComments) {
 
 		if (includeComments || !isComment()) {
 			buf.append(getValue()).append(lineEndString);
 		}
 		
 		// Recursive Part
 		int childCount = numOfChildren();
 		for (int i = 0; i < childCount; i++) {
 			getChild(i).getRecursiveValue(buf, lineEndString, includeComments);
 		}
 	}
 	
 	public void getMergedValue(StringBuffer buf) {
 		buf.append(getValue());
 		
 		// Recursive Part
 		int childCount = numOfChildren();
 		for (int i = 0; i < childCount; i++) {
 			getChild(i).getMergedValue(buf);
 		}
 	}
 
 	public void getMergedValueWithSpaces(StringBuffer buf) {
 		if (buf.length() > 0) {
 			if (buf.charAt(buf.length() - 1) != ' ') {
 				buf.append(' ').append(getValue());
 			} else {
 				buf.append(getValue());
 			}
 		} else {
 			buf.append(getValue());
 		}
 		
 		// Recursive Part
 		int childCount = numOfChildren();
 		for (int i = 0; i < childCount; i++) {
 			getChild(i).getMergedValue(buf);
 		}
 	}
 	
 	
 	// AttributeContainer Interface
 	public void setAttribute(String key, Object value, boolean isReadOnly) {
 		super.setAttribute(key, value, isReadOnly);
 		
 		// Set Modified Date
 		if (tree.getDocument() != null && tree.getDocument().settings.getUseCreateModDates().cur && !KEY_MODIFIED.equals(key) && !KEY_CREATED.equals(key)) {
 			super.setAttribute(KEY_MODIFIED, tree.getDocument().settings.dateFormat.format(new Date()), true);
 		}
 	}
 
 	public void removeAttribute(String key) {
 		// Set Modified Date
 		if (tree.getDocument() != null && tree.getDocument().settings.getUseCreateModDates().cur && !KEY_MODIFIED.equals(key) && !KEY_CREATED.equals(key)) {
 			super.setAttribute(KEY_MODIFIED, tree.getDocument().settings.dateFormat.format(new Date()), true);
 		}
 
 		super.removeAttribute(key);
 	}
 }
