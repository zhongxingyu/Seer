 package org.icefaces.ace.model.tree;
 
 import javax.swing.tree.MutableTreeNode;
 import javax.swing.tree.TreeNode;
 import java.io.Serializable;
 import java.util.*;
 
 public class ListNodeDataModel extends NodeDataModel<TreeNode> implements Serializable {
     private final KeySegmentConverter DEFAULT_CONVERTER = new NodeModelListSequenceKeyConverter(this);
     private KeySegmentConverter converter;
     List<TreeNode> roots;
 
     public ListNodeDataModel(List<TreeNode> nodeList) {
         roots = nodeList;
     }
 
     public ListNodeDataModel(final TreeNode rootNode) {
         this.roots = new ArrayList<TreeNode>() {{
             add(rootNode);
         }};
     }
 
     @Override
     public Object getWrappedData() {
         return roots;
     }
 
     @Override
     public void setWrappedData(Object data) {
         if (data instanceof List)
             if (((List)data).get(0) instanceof TreeNode) {
                 roots = (List<TreeNode>) data;
                 return;
             }
 
         throw new IllegalArgumentException(String.valueOf(data));
     }
 
     public boolean isMutable() {
         return roots != null && roots.size() > 0
                 && roots.get(0) instanceof MutableTreeNode
                 && (getData() == null || getData() instanceof MutableTreeNode);
     }
 
     @Override
     public TreeNode navToParent() {
         if (atNullRoot()) return null;
         else {
             data = getData().getParent();
             key = getKey().getParent();
             return data;
         }
     }
 
     @Override
     public TreeNode navToKey(NodeKey key) {
         if (key == null)
             throw new IllegalArgumentException("null");
 
         Object[] keys = key.getKeys();
 
         // If we are navigating to a key with null segments
         // this indicates the first root, and we will
         // navigate to and change state to indicate such
         if ((keys == null || keys.length == 0)) {
             if (roots != null && roots.size() > 0) {
                 // Reset position to beginning of roots
                 this.key = key;
                 this.data = null;
                 return null;
             } else {
                 throw new IllegalArgumentException("Navigating to first root of empty model.");
             }
         }
 
         Object rootKey = keys[0];
         KeySegmentConverter converter = getConverter();
 
         // Move to correct root
         for (TreeNode root : roots)
             if (converter.getSegment(root).equals(rootKey)) {
                 this.key = new NodeKey(rootKey);
                 setData(root);
             }
 
         if (keys.length == 0)
             return getData();
 
         int i;
         TreeNode c = null;
         for (i = 1; i < keys.length; i++)
             c = navToChild(keys[i]);
 
         return c;
     }
 
     @Override
     public TreeNode navToChild(Object keySegment) {
         // Navigate straight to index if default integer index
         // based key is used
         if (getConverter() == DEFAULT_CONVERTER) {
             Integer index = (Integer) keySegment;
 
             // Index from root
             if (getKey().equals(NodeKey.ROOT_KEY)) {
                 key = new NodeKey(index);
                 data = roots.get(index);
                 return data;
             } else {
                 key = getKey().append(index);
                 data = getData().getChildAt(index);
                 return data;
             }
         }
         // Otherwise search for matching key in children
         else {
             for (Iterator<Map.Entry<NodeKey, TreeNode>> children = children();
                  children.hasNext();) {
                 Map.Entry<NodeKey, TreeNode> childEntry = children.next();
                 Object[] keys = childEntry.getKey().getKeys();
 
                 if (keys[keys.length-1].equals(keySegment)) {
                     key = childEntry.getKey();
                     data = childEntry.getValue();
                     return data;
                 }
             }
         }
 
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean isNodeAvailable() {
         return getData() != null;
     }
 
     @Override
     public boolean isLeaf() {
         return getData().isLeaf();
     }
 
     @Override
     public Iterator<Map.Entry<NodeKey, TreeNode>> children() {
         if (atNullRoot())
             return new NodeCollectionToNodeEntryIterator<TreeNode>(
                     getConverter(),
                     getKey(),
                     roots );
         else
             return new NodeEnumerationToNodeEntryIterator<TreeNode>(
                     getConverter(),
                     getKey(),
                     getData().children() );
     }
 
     @Override
     public KeySegmentConverter getConverter() {
         if (converter == null) return DEFAULT_CONVERTER;
         return converter;
     }
 
     @Override
     public void setConverter(KeySegmentConverter converter) {
         this.converter = converter;
     }
 
     @Override
     public void insert(TreeNode imNode, int index) {
         if (!isMutable()) throw new UnsupportedOperationException();
 
         MutableTreeNode current = (MutableTreeNode) getData();
         MutableTreeNode node = (MutableTreeNode) imNode;
 
        if (current == null) {
             node.removeFromParent();
             node.setParent(null);
             roots.add(index, node);
         }
         else {
            node.removeFromParent();
             current.insert(node, index);
         }
     }
 
     @Override
     public void remove(Object segOrNode, boolean isSegment) {
         if (!isMutable()) throw new UnsupportedOperationException();
         MutableTreeNode current = (MutableTreeNode) getData();
 
         if (!isSegment) {
             if (current == null)
                 roots.remove((MutableTreeNode)segOrNode);
             else
                 current.remove((MutableTreeNode)segOrNode);
         }
         else if (segOrNode instanceof Integer) {
             if (current == null)
                 roots.remove(((Integer)segOrNode).intValue());
             else
                 current.remove((Integer) segOrNode);
         }
         else if (current != null)
             for (Enumeration children = current.children(); children.hasMoreElements(); ) {
                 MutableTreeNode child = (MutableTreeNode) children.nextElement();
                 if (getConverter().getSegment(child).equals(segOrNode)) {
                     current.remove(child);
                     return;
                 }
             }
         else
             for (TreeNode node : roots) {
                 if (getConverter().getSegment(node).equals(segOrNode)) {
                     roots.remove(node);
                     return;
                 }
             }
     }
 }
