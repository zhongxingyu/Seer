 package trees;
 
 /** Copyright 2011 (C) Felix Langenegger & Jonas Ruef */
 
 import javax.swing.tree.DefaultTreeModel;
 
abstract class CANTree extends DefaultTreeModel {
    public CANTree(CNATreeNode node) {
 	super(node);
     }
 }
