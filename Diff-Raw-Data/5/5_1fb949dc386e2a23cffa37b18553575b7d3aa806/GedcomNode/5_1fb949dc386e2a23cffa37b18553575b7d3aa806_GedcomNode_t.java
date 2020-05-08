 /******************************************************************************
 * GedcomNode
 * A generic node for a gedcom line. 
 * Holds level, tag, reference, data and children nodes.
 * 
 * Author:       Mitchell Bowden <mitchellbowden AT gmail DOT com>
 * License:      MIT License: http://creativecommons.org/licenses/MIT/
 ******************************************************************************/
 
 package com.msbmsb.genealoj;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ArrayList;
 
 /**
  * Generic node for holding and traversing through a GEDCOM line
  */
 public class GedcomNode {
   private int m_level;
   private String m_tag;
   private String m_reference = null;
   private String m_data = null;
   private Map<String, List<GedcomNode> > m_childrenByTag = new HashMap<String, List<GedcomNode> >();
   private Map<String, GedcomNode> m_referencedNodes = new HashMap<String, GedcomNode>();
 
   /**
    * Constructors
    * Basic: level and tag
    * Alternate: level, tag, data
    * Alternate: level, tag, data, reference
    */
   public GedcomNode(int level, String tag) {
     m_level = level;
     m_tag = tag;
   }
 
   public GedcomNode(int level, String tag, String data) {
     m_level = level;
     m_tag = tag;
     m_data = data;
   }
 
   public GedcomNode(int level, String tag, String data, String reference) {
     m_level = level;
     m_tag = tag;
     m_data = data;
     m_reference = reference;
   }
 
   /**
    * @return level of this node
    */
   public int level() {
     return m_level;
   }
 
   /**
    * @return tag of this node
    */
   public String tag() {
     return m_tag;
   }
 
   /**
    * Set the reference for this node
    * @param ref the reference element of this node
    */
   public void reference(String ref) {
     m_reference = ref;
   }
 
   /**
    * @return reference for this node
    */
   public String reference() {
     return m_reference;
   }
 
   /**
    * Set the data for this node
    * @param data the data element of this node
    */
   public void data(String data) {
     m_data = data;
   }
 
   /**
    * @return data element of this node
    */
   public String data() {
     return m_data;
   }
 
   /**
    * Add a GedcomNode as a child of this node.
    * If child is a reference, also add it to the map for reference-&gt;node
    * @param child the node that is to be set as child
    */
   public void addChildNode(GedcomNode child) {
     List<GedcomNode> nodes = getChildrenWithTag(child.tag());
     if(nodes == null) {
       m_childrenByTag.put(child.tag(), nodes = new ArrayList<GedcomNode>());
     }
     nodes.add(child);
 
     // if this child is a reference node, add it to map of ref nodes
     // if this child is not a reference node, nothing will be done
    if(m_level != 0) {
       addReferencedNode(child);
     }
   }
 
   /**
    * Get the list of all children nodes of m_level+1 given a tag
    * @param tag the tag to retrieve on
    * @return List<GedcomNode> of nodes matching tag;
    *         null if no matches found
    */
   public List<GedcomNode> getChildrenWithTag(String tag) {
     return m_childrenByTag.get(tag);
   }
 
   /**
    * Recursively get the list of all descendant nodes given a tag
    * Builds a list from all children of children
    * @param tag the tag to retrieve on
    * @return List<GedcomNode> of recursively retrieved nodes matching tag;
    *         null if no matches found
    */
   public List<GedcomNode> getDescendantsWithTag(String tag) {
     List<GedcomNode> decList = getChildrenWithTag(tag);
 
     Iterator nodes = m_childrenByTag.entrySet().iterator();
     while(nodes.hasNext()) {
       List<GedcomNode> childrenList = (ArrayList<GedcomNode>)((Map.Entry)nodes.next()).getValue();
       for(GedcomNode n : childrenList) {
         decList.addAll(n.getDescendantsWithTag(tag));
       }
     }
     return decList;
   }
 
   /**
    * Add this node to the reference-&gt;GedcomNode map
    * If node is not a reference or this is not a level=0 node,
    * then nothing will be added to the map
    * @param node the node to add
    */
   public void addReferencedNode(GedcomNode node) {
     String ref = node.reference();
    if((m_level != 0) || (ref != null)) {
       m_referencedNodes.put(ref, node);
     }
   }
 
   /**
    * Return the node with the parameter reference
    * @param ref the reference for which to retrieve the GedcomNode
    * @return the GedcomNode with the given ref reference
    */
   public GedcomNode getReferencedNode(String ref) {
     return m_referencedNodes.get(ref);
   }
 
   /**
    * Perform any necessary finalization once this and all children node
    * have been constructed
    */
   public void finalize() {
     // intended to be used by any node extending this
     return;
   }
 
   /**
    * Build a string representation of this node and its children
    * This builds a string in the GEDCOM format just as it was input
    * but will not necessarily return in the same order as it was given
    * @return string representation of this node and children
    */
   public String toString() {
     StringBuilder sb = new StringBuilder();
     sb.append(m_level);
     sb.append(" ");
     // if this node is at level=0 and has a reference, it should be 
     // printed reference before tag
     if(m_level == 0 && m_reference != null) {
       sb.append(m_reference);
       sb.append(" ");
       sb.append(m_tag);
       sb.append(" ");
     } else {
       // otherwise always print tag before reference
       sb.append(m_tag);
       sb.append(" ");
       if(m_reference != null) {
         sb.append(m_reference);
         sb.append(" ");
       }
     }
     // print the data last 
     if(m_data != null) {
       sb.append(m_data);
     }
 
     // iterate through children and recurse
     Iterator nodes = m_childrenByTag.entrySet().iterator();
     while(nodes.hasNext()) {
       List<GedcomNode> childrenList = (ArrayList<GedcomNode>)((Map.Entry)nodes.next()).getValue();
       for(GedcomNode n : childrenList) {
         sb.append("\n");
         sb.append(n.toString());
       }
       /*
       sb.append("\n");
       sb.append(((Map.Entry)nodes.next()).getValue().toString());*/
     }
 
     return sb.toString();
   }
 }
