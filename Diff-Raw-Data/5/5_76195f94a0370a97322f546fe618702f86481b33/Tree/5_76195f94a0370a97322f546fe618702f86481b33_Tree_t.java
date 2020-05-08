 /*
  * Hyperchron, a timeseries data management solution.
  * Copyright (C) 2011 Tobias Wegner
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
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package org.hyperchron.impl;
 
 import java.util.Arrays;
 import java.util.Date;
 
 import org.hyperchron.impl.blocks.BlockStore;
 
 public class Tree {
 	TreeNode rootNode = null;
 
 	public static int RESERVED_SIZE = 4;
 	
 	public static int MAX_CHILDREN = 16;
 	
 	public String uuid;	
 	
 	public Tree (EntityDescriptor entityDescriptor) {
 		uuid = entityDescriptor.uuid;
 		
 		rootNode = new TreeNode(null, entityDescriptor);
 		
 		TreeLeaf leaf = null; //new TreeLeaf(BlockStore.instance.getNextBlockID(), entityDescriptor.entityID);
 		
 		int chunk = 0;
 		int chunkOffset = 0;
 		
 		while (true) {
 			long entityID = BlockStore.instance.ReadFromSuperblock(chunk, chunkOffset, 0);
 			
 			if (entityID == -1)
 				break;
 			
 			if (entityID == entityDescriptor.entityID) {
 				leaf = new TreeLeaf(chunkOffset + BlockStore.instance.blocksPerSuperblock * chunk, entityDescriptor.entityID);
 
 				leaf.parent = rootNode;
 				leaf.entityDescriptor = entityDescriptor;
 			
 /*				if (rootNode.children.size() == 0)
 				{
 					//first child so start with 0
 					leaf.startingTimestamp = 0;
 				}
 				else*/
 				leaf.startingTimestamp = BlockStore.instance.ReadFromSuperblock(chunk, chunkOffset, 1);
 				leaf.length = (int)BlockStore.instance.ReadFromSuperblock(chunk, chunkOffset, 2);
 				
 				AddLeaveToNode (leaf, rootNode);
 			}
 			
 			if (++chunkOffset >= BlockStore.instance.blocksPerSuperblock) {
 				chunkOffset = 0;
 				chunk++;
 			}
 		}
 		
 		if (rootNode.children.size() > 0) 
 		{			
 			//go to first leaf
 			TreeLeaf lastLeaf = getFirstLeaf();
 			
 			while (lastLeaf.nextSibling != null)
 			{
 					TreeLeaf currentLeaf = lastLeaf.nextSibling; 
 					
 					lastLeaf.nextSibling = currentLeaf;
 					currentLeaf.previousSibling = lastLeaf;
 					
 					lastLeaf.endingTimestamp = currentLeaf.startingTimestamp;
 					
 					lastLeaf = currentLeaf;
 			}
 		}
 		else {
 			leaf = new TreeLeaf(BlockStore.instance.getNextBlockID(), entityDescriptor.entityID);
 
 			leaf.parent = rootNode;
 			leaf.entityDescriptor = entityDescriptor;
 			
 			leaf.startingTimestamp = 0;
 			
 			rootNode.children.add(leaf);			
 		}
 	}
 	
 	/*
 	 * TreeElement
 	 */
 	public long GetStartingTimeStamp (TreeElement element) {
 		return element.startingTimestamp;
 	}
 
 	public long GetEndingTimeStamp(TreeElement element) {
 		return element.endingTimestamp;
 	}
 	
 	public TreeElement getChildByTimeStamp(TreeElement element, long timeStamp) {
 		for (int i = 0; i < element.children.size(); i++) {
 			if (
 					(element.children.get(i).startingTimestamp <= timeStamp) && 
 					(element.children.get(i).endingTimestamp > timeStamp)
 				) {
 				if (element.children.get(i) instanceof TreeNode)
 					return getChildByTimeStamp(element.children.get(i), timeStamp);
 				else
 					return element.children.get(i);
 			}
 		}
 		
 		return null;
 	}
 	
 	/*
 	 * TreeNode
 	 */
 	public TreeNode getPreviousSibling(TreeNode node) {
 		return node.previousSibling;
 	}
 	
 	public TreeNode getNextSibling(TreeNode node) {
 		return node.nextSibling;
 	}
 			
 	public TreeElement getFirstChild(TreeNode node) {
 		return node.children.get(0);
 	}
 	
 	public TreeElement getLastChild(TreeNode node) {
 		return node.children.get(node.children.size() - 1);
 	}
 
 	public boolean SaveTimestamp(TreeNode node, long timestamp) {
 		TreeElement child = getChildByTimeStamp(node, timestamp);
 		
 		if (child instanceof TreeNode) {
 			return SaveTimestamp((TreeNode)child, timestamp);
 		}
 		
 		if (child instanceof TreeLeaf) {
 			TreeLeaf leaf = (TreeLeaf) child;
 			
 			return SaveTimestamp(leaf, timestamp);
 		}
 		
 		return false;
 	}		
 	
 	public void AddNodeToNode (TreeNode newNode, TreeNode node) {
 		newNode.parent = node;
 		
 		TreeElement currentElement = node.children.get(0);
 		
 		//find insert position
 		for (int i = 1; i < node.children.size(); i++) {
 			currentElement = node.children.get(i);
 			
 			if (newNode.startingTimestamp < currentElement.startingTimestamp) {
 				node.children.add(i, newNode);
 				
 				if (node.children.size() > MAX_CHILDREN)
 					Split(node);
 
 				return;
 			}
 		}
 		
 		//add at end			
 		node.children.add(newNode);
 		
 		if (node.children.size() > MAX_CHILDREN)
 			Split(node);		
 	}
 	
 	public void AddLeaveToNode (TreeLeaf leaf, TreeNode node) {
 		if (node.children.size() == 0) {
 			node.children.add(leaf);
 
 			return;
 		};
 		
 		TreeElement currentElement = node.children.get(0);
 		
 		//find insert position
		for (int i = 0; i < node.children.size(); i++) {
 			currentElement = node.children.get(i);
 			
 			if (leaf.startingTimestamp < currentElement.startingTimestamp) {
 				//insert at i
 				if (currentElement instanceof TreeNode) {
					AddLeaveToNode(leaf, ((TreeNode)currentElement).previousSibling);
 				} else {
 					TreeLeaf currentLeaf = (TreeLeaf)currentElement;
 					
 					node.children.add(i, leaf);
 
 					leaf.nextSibling = currentLeaf;
 					leaf.previousSibling = currentLeaf.previousSibling;
 					currentLeaf.previousSibling = leaf;
 					
 					if (leaf.previousSibling != null)
 						leaf.previousSibling.nextSibling = leaf;
 					
 					if (node.children.size() > MAX_CHILDREN)
 						Split(node);
 				}
 				
 				return;
 			}
 		}
 		
 		if (currentElement instanceof TreeNode) {
 			AddLeaveToNode(leaf, (TreeNode)currentElement);
 		} else {
 			//add at end			
 			TreeLeaf currentLeaf = (TreeLeaf)currentElement;
 			
 			node.children.add(leaf);
 
 			leaf.nextSibling = currentLeaf.nextSibling;
 			currentLeaf.nextSibling = leaf;
 			leaf.previousSibling = currentLeaf;
 			
 			if (leaf.nextSibling != null)
 				leaf.nextSibling.nextSibling = leaf;
 			
 			if (node.children.size() > MAX_CHILDREN)
 				Split(node);
 		}
 	}
 	
 	public void Split(TreeNode treeNode) {
 		if (treeNode.parent == null) {
 			//split in half
 			TreeNode node1 = new TreeNode(treeNode, treeNode.entityDescriptor);
 			TreeNode node2 = new TreeNode(treeNode, treeNode.entityDescriptor);
 			
 			int splitElement = (int)(treeNode.children.size() * (3.0/4.0));
 			
 			node1.startingTimestamp = treeNode.startingTimestamp;
 			node2.startingTimestamp = treeNode.children.get(splitElement).startingTimestamp;
 			node1.endingTimestamp = node2.startingTimestamp;
 			node2.endingTimestamp = treeNode.endingTimestamp;
 			
 			node1.nextSibling = node2;
 			node2.previousSibling = node1;
 			
 			for (int i = 0; i < treeNode.children.size(); i++)
 			{
 				if (i < splitElement) {
 					TreeElement child = treeNode.children.get(i);
 					
 					child.parent = node1;
 					
 					node1.children.add(child);
 				} else {
 					TreeElement child = treeNode.children.get(i);
 					
 					child.parent = node2;
 
 					node2.children.add(child);
 				}
 			}
 			
 			treeNode.children.clear();
 			
 			node1.parent = treeNode;
 			node2.parent = treeNode;
 
 			treeNode.children.add(node1);
 			treeNode.children.add(node2);
 		} else {
 			TreeNode newNode = new TreeNode(treeNode, treeNode.entityDescriptor);
 
 			int splitElement = (int)(treeNode.children.size() * (3.0/4.0));
 			
 			newNode.startingTimestamp = treeNode.children.get(splitElement).startingTimestamp;
 			newNode.endingTimestamp = treeNode.endingTimestamp;
 			treeNode.endingTimestamp = newNode.startingTimestamp;
 			
 			newNode.nextSibling = treeNode.nextSibling;
 			treeNode.nextSibling = newNode;
 			newNode.previousSibling = treeNode;
 			
 			for (int i = splitElement; i < treeNode.children.size(); i++) {
 				TreeElement child = treeNode.children.get(i);
 				
 				child.parent = newNode;
 				
 				newNode.children.add(child);
 			}
 			
 			while (treeNode.children.size() > splitElement)
 				treeNode.children.remove(splitElement);
 					
 			AddNodeToNode(newNode, treeNode.parent);
 		}
 	}
 	
 	/*
 	 * TreeLeaf
 	 */
 	public TreeLeaf getPreviousSibling(TreeLeaf leaf) {
 		return leaf.previousSibling;
 	}
 	
 	public TreeLeaf getNextSibling(TreeLeaf leaf) {
 		return leaf.nextSibling;
 	}
 	
 	public int GetIndexForTimestamp(TreeLeaf leaf, long timestamp) {
 		if (leaf.length > 0) {
 			if (leaf.timeStamps == null)
 				BlockStore.instance.LoadDataIntoLeaf(leaf.entityDescriptor.uuid, leaf, true);
 				
 			synchronized (leaf) {
 				int index = Arrays.binarySearch(leaf.timeStamps, 0, leaf.length - 1, timestamp);
 
 				if (index < 0)
 					index = -(index + 1);
 				
 				return index;
 			}		
 		}
 		
 		return 0;
 	}	
 	
 	public boolean SaveTimestamp(TreeLeaf leaf, long timestamp) {
 		if (leaf.timeStamps == null)
 			BlockStore.instance.LoadDataIntoLeaf(rootNode.entityDescriptor.uuid, leaf, true);
 			
 		int index = -1; 
 		
 		synchronized (leaf) {
 			index = GetIndexForTimestamp(leaf, timestamp);
 			
 			if ((leaf.timeStamps[index] == timestamp) && (index < leaf.length))
 			{
 				//already in index
 				return true;
 			}
 		}			
 
 		if (leaf.length == BlockStore.BLOCK_SIZE) {
 			Split(leaf, timestamp > leaf.timeStamps[BlockStore.BLOCK_SIZE - 1]);
 			
 			return false;
 		}
 			
 		synchronized (leaf) {
 			if (leaf.length == 0) {
 				leaf.timeStamps[0] = timestamp;
 				
 				leaf.length = 1;
 			} else if (timestamp > leaf.timeStamps[leaf.length - 1]) {
 				leaf.timeStamps[leaf.length] = timestamp;
 				
 				leaf.length++;
 			} else {
 				System.arraycopy(leaf.timeStamps, index, leaf.timeStamps, index + 1, leaf.length - index);
 				
 				leaf.timeStamps[index] = timestamp;
 				
 				leaf.length++;
 			}
 			
 			leaf.lastWrite = new Date().getTime();
 		}
 		
 		return true;
 	}
 	
 	public void Split(TreeLeaf leaf, boolean append) {
 		if (append) {
 			//last block, so just append a new one
 			leaf.endingTimestamp = leaf.timeStamps[leaf.timeStamps.length - 1];
 			
 			TreeLeaf newLeaf = new TreeLeaf(BlockStore.instance.getNextBlockID(), leaf.entityDescriptor.entityID);
 			
 			newLeaf.startingTimestamp = leaf.endingTimestamp + 1;
 			
 			newLeaf.entityDescriptor = leaf.entityDescriptor;
 			
 			newLeaf.previousSibling = leaf;
 			leaf.nextSibling = newLeaf;
 			
 			newLeaf.parent = leaf.parent;
 			
 			leaf.parent.children.add(newLeaf);
 		} else {
 			synchronized (leaf) {
 				//split in half
 				int splitPosition = BlockStore.BLOCK_SIZE / 2;
 				
 				long splitTime = leaf.timeStamps[splitPosition];
 				
 				TreeLeaf newLeaf = new TreeLeaf(BlockStore.instance.getNextBlockID(), leaf.entityDescriptor.entityID);
 				
 				synchronized (newLeaf) {
 					newLeaf.entityDescriptor = leaf.entityDescriptor;
 					newLeaf.parent = leaf.parent;
 					
 					newLeaf.nextSibling = leaf.nextSibling;
 					newLeaf.previousSibling = leaf;
 					leaf.nextSibling = newLeaf;
 					
 					newLeaf.endingTimestamp = leaf.endingTimestamp;
 					newLeaf.startingTimestamp = splitTime;
 					leaf.endingTimestamp = splitTime;
 					
 					newLeaf.length = leaf.length - splitPosition;
 					leaf.length = splitPosition;
 
 					newLeaf.timeStamps = new long [BlockStore.BLOCK_SIZE];
 					
 					System.arraycopy(leaf.timeStamps, splitPosition, newLeaf.timeStamps, 0, newLeaf.length);
 					
 					leaf.lastWrite = new Date().getTime();
 					newLeaf.lastWrite = leaf.lastWrite;					
 				}
 
 				//insert at right position
 				for (int i = 0; i < newLeaf.parent.children.size(); i++){
 					TreeLeaf currentLeaf = (TreeLeaf)newLeaf.parent.children.get(i);
 					
 					if (newLeaf.startingTimestamp < currentLeaf.startingTimestamp) {
 						newLeaf.parent.children.add(i, newLeaf);
 						
 						break;
 					}
 				}
 				
 				BlockStore.instance.InsertLeafIntoLRUList(newLeaf);
 			}
 		}
 		
 		if (leaf.parent.children.size() > MAX_CHILDREN) {
 			Split (leaf.parent);
 		}
 	}
 /*	
 	public TreeLeaf (TreeNode parent, EntityDescriptor entityDescriptor) {
 		this.entityDescriptor = entityDescriptor;
 		this.blockID = entityDescriptor.getNextBlockID();
 		this.parent = parent;
 		
 		timeStamps = new long[BLOCK_SIZE]; 
 		IDs = new long[BLOCK_SIZE]; 
 	}
 */	
 	
 	public long getTimeStampForIndex(TreeLeaf leaf, int index) {
 		if (index < leaf.length) {
 			synchronized (leaf) {
 				if (leaf.timeStamps == null)
 					BlockStore.instance.LoadDataIntoLeaf(rootNode.entityDescriptor.uuid, leaf, true);
 					
 				return leaf.timeStamps[index];				
 			}
 		}
 
 		return -1;
 	}
 
 	public int getLastIndex (TreeLeaf leaf) {
 		return leaf.length - 1;
 	}	
 	
 	/*
 	 * API
 	 */
 	public void SaveTimestamp(long timestamp) {
 		while (!SaveTimestamp(rootNode, timestamp));
 	}	
 	
 	public TreeLeaf getFirstLeaf() {
 		TreeElement currentElement = rootNode;
 		
 		while (currentElement instanceof TreeNode) {
 			TreeNode node = (TreeNode)currentElement;
 			currentElement = getFirstChild(node);
 		}
 		
 		return (TreeLeaf)currentElement;
 	}
 	
 	public TreeLeaf getLastLeaf() {
 		TreeElement currentElement = rootNode;
 
 		while (currentElement instanceof TreeNode) {
 			TreeNode node = (TreeNode)currentElement;
 			currentElement = getLastChild(node);
 		}		
 
 		return (TreeLeaf)currentElement;
 	}
 	
 	public TreeLeaf GetLeafForTimestamp (long timestamp) {
 		TreeElement rootElement = rootNode;
 		
 		while (rootElement instanceof TreeNode) {
 			TreeNode node = (TreeNode)rootElement;
 			rootElement = getFirstChild(node);
 			
 			while (timestamp >= GetEndingTimeStamp(rootElement)) {
 				if (rootElement instanceof TreeNode)
 					rootElement = getNextSibling((TreeNode)rootElement);
 
 				if (rootElement instanceof TreeLeaf)
 					rootElement = getNextSibling((TreeLeaf)rootElement);
 			}
 		}
 		
 		if (rootElement instanceof TreeLeaf)
 			return (TreeLeaf) rootElement;
 			
 		return null;
 	}
 }
