 /*
  * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 42):
  * "Sven Strittmatter" <ich(at)weltraumschaf(dot)de> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a beer in return.
  *
  */
 
 package de.weltraumschaf.jebnf.ast;
 
 import java.util.Map;
 
 /**
  * Interface of an homogenous AST node.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public interface Node extends Visitable {
 
     /**
      * Returns the name of a node.
      *
      * @return Return the name of the node.
      */
     String getNodeName();
 
     /**
      * Probes equivalence of itself against an other node and collects all
      * errors in the passed {@link Notification} object.
      *
      * @param other  Node to compare against.
      * @param result Object which collects all equivalence violations.
      */
     void probeEquivalence(Node other, Notification result);
 
     /**
      * Returns the depth of the node.
      *
      * Nodes with no child have a depth of 1. Nodes with children have
      * the max depth of the children plus one. The depth is same as the
      * length of of the longest path in the tree.
      *
      * @return Returns positive integers greater than 0.
      */
     int depth();
 
     /**
      * Get the node type.
      *
      * @return Return node type enum.
      */
     NodeType getType();
 
     /**
      * Whether the node has a parent node or not.
      *
      * @return Return true if the node has a parent node, unless false.
      */
     boolean hasParent();
 
     /**
      * Get the parent.
      *
     * TODO Does it return a Null object or null?
     *
     * @return Return the parent node. Will return null, if {@link #hasParent()} return false.
      */
     Node getParent();
 
     /**
      * Whether the node has attributes.
      *
      * @return Return true if the node has attributes, unless false.
      */
     boolean hasAttributes();
 
     /**
      * Returns map of attributes.
      *
      * @return The returned map may be of size 0, if {@link #hasAttributes()} is false.
      */
     Map<String, String> getAttributes();
 
     /**
      * Whether the node has a particular attribute.
      *
      * @param name Name of the attribute.
      * @return Return true if the attribute exist.
      */
     boolean hasAttribute(String name);
 
     /**
      * Get the attribute by name.
      *
      * @param name Name of the Attribute.
      * @return Return the attribute string. Will throw {@link IllegalArgumentException} when asking
      *        for an attribute not present. Use {@link #hasAttribute(java.lang.String)} to check.
      */
     String getAttribute(String name);
 
     /**
      * Set an attribute.
      *
      * Previously set attribute values will be overridden, if same name is used.
      *
      * @param name Name of the Attribute.
      * @param value Value of the Attribute.
      */
     void setAttribute(String name, String value);
 
     /**
      * Checks if ta node is of a particular type.
      *
      * @param checked Node type to check against.
      * @return Return true, if the node is of the passed type.
      */
     boolean isType(NodeType checked);
 
 }
