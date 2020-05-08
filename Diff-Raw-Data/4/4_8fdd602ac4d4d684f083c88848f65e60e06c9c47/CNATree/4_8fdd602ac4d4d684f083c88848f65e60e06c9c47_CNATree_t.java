 package trees;
 
 /** Copyright 2011 (C) Felix Langenegger & Jonas Ruef */
 
 import javax.swing.tree.DefaultTreeModel;
 
abstract class CNATree extends DefaultTreeModel {
    public CNATree(CNATreeNode node) {
 	super(node);
     }
 }
