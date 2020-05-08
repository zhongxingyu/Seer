 /*
  * NamedNodeMapImpl.java -
  * Copyright (C) 2003 Klaus Rennecke, all rights reserved.
  *
  * Created on Apr 14, 2003 by marion@users.sourceforge.net
  */
 package net.sourceforge.fraglets.zeig.dom;
 
 import java.sql.SQLException;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 import net.sourceforge.fraglets.zeig.model.NodeFactory;
 
 import org.w3c.dom.DOMException;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * @author marion@users.sourceforge.net
 * @version $Revision: 1.5 $
  */
 public class NamedNodeMapImpl implements NamedNodeMap, NodeList {
     private NodeFactory nf;
     private NodeImpl nodes[];
     
     public static final NamedNodeMapImpl MT = new NamedNodeMapImpl();
     
     private NamedNodeMapImpl() {
         nodes = new NodeImpl[0];
     }
     
     public NamedNodeMapImpl(NodeImpl node, boolean atts) throws DOMException {
         init(node, atts);
     }
     
     public void init(NodeImpl node, boolean atts) throws DOMException {
         try {
             nf = node.getNodeFactory();
             int nodeIds[] = nf.getNodes(node.getId());
             int length = 0;
             int start = 0;
             int textTag = nf.getText();
             int nodeTag = nf.getNode();
             int piTag = nf.getPi();
             for (int i = 0; i < nodeIds.length; i += 2) {
                 int id = nodeIds[i];
                 if (id == textTag || id == nodeTag || id == piTag) {
                     if (!atts) {
                         if (length == 0) {
                             start = i >> 1;
                         }
                         length++;
                     } else {
                         break;
                     }
                 } else {
                     if (atts) {
                         length++;
                     }
                 }
             }
             if (atts) {
                 this.nodes = new AttrImpl[length];
             } else {
                 this.nodes = new NodeImpl[length];
             }
             for (int i = 0; i < length; i++) {
                 int id = nodeIds[(start + i) << 1];
                 int v = nodeIds[((start + i) << 1) + 1];
                 if (id == textTag) {
                     this.nodes[i] = new TextImpl(node, id, v);
                 } else if (id == nodeTag) {
                     id = v;
                     v = node.getXMLTextFactory().getName(v);
                     this.nodes[i] = new ElementImpl(node, id, v);
                 } else if (id == piTag) {
                     int pi[] = node.getXMLTextFactory().getNodes(v);
                     this.nodes[i] = new PIImpl(node, pi[0], pi[1]);
                 } else {
                     this.nodes[i] = new AttrImpl(node, id, v);
                 }
             }
         } catch (NoSuchElementException ex) {
             throw new DOMException(DOMException.NOT_FOUND_ERR, ex.getMessage());
         } catch (SQLException ex) {
             throw NodeImpl.domException(ex);
         }
     }
 
     /**
      * @see org.w3c.dom.NamedNodeMap#getLength()
      */
     public int getLength() {
         return nodes.length;
     }
     
     public int getIndex(int nm) {
         for (int i = 0; i < nodes.length; i++) {
             if (nodes[i].getId() == nm) {
                 return i;
             }
         }
         return -1;
     }
     
     public int getIndex(Node node) {
         for (int i = 0; i < nodes.length; i++) {
             if (nodes[i] == node) {
                 return i;
             }
         }
         return -1;
     }
     
     public Node getNamedItem(int nm) {
         for (int i = 0; i < nodes.length; i++) {
             if (nodes[i].getId() == nm) {
                 return nodes[i];
             }
         }
         return null;
     }
 
     /**
      * @see org.w3c.dom.NamedNodeMap#getNamedItem(java.lang.String)
      */
     public Node getNamedItem(String name) {
         try {
             return getNamedItem(nf.getName(name));
         } catch (SQLException ex) {
             throw NodeImpl.domException(ex);
         }
     }
 
     /**
      * @see org.w3c.dom.NamedNodeMap#getNamedItemNS(java.lang.String, java.lang.String)
      */
     public Node getNamedItemNS(String namespaceURI, String localName) {
         try {
             return getNamedItem(nf.getName(namespaceURI, localName));
         } catch (SQLException ex) {
             throw NodeImpl.domException(ex);
         }
     }
 
     /**
      * @see org.w3c.dom.NamedNodeMap#item(int)
      */
     public Node item(int index) {
        return nodes[index];
     }
     
     public Iterator iterator() {
         return new NodeIterator(this);
     }
 
     /**
      * @see org.w3c.dom.NamedNodeMap#removeNamedItem(java.lang.String)
      */
     public Node removeNamedItem(String name) throws DOMException {
         throw NodeImpl.notImplemented();
     }
 
     /**
      * @see org.w3c.dom.NamedNodeMap#removeNamedItemNS(java.lang.String, java.lang.String)
      */
     public Node removeNamedItemNS(String namespaceURI, String localName)
         throws DOMException {
         throw NodeImpl.notImplemented();
     }
 
     /**
      * @see org.w3c.dom.NamedNodeMap#setNamedItem(org.w3c.dom.Node)
      */
     public Node setNamedItem(Node arg) throws DOMException {
         throw NodeImpl.notImplemented();
     }
 
     /**
      * @see org.w3c.dom.NamedNodeMap#setNamedItemNS(org.w3c.dom.Node)
      */
     public Node setNamedItemNS(Node arg) throws DOMException {
         throw NodeImpl.notImplemented();
     }
 
     public static class NodeIterator implements Iterator {
         private NodeList nl;
         private int index = 0;
         
         public NodeIterator(NodeList nl) {
             this.nl = nl;
             this.index = 0;
         }
         
         /**
          * @see java.util.Iterator#hasNext()
          */
         public boolean hasNext() {
             return index < nl.getLength();
         }
 
         /**
          * @see java.util.Iterator#next()
          */
         public Object next() {
             if (hasNext()) {
                 return nl.item(index++);
             } else {
                 throw new NoSuchElementException();
             }
         }
 
         /**
          * @see java.util.Iterator#remove()
          */
         public void remove() {
             throw NodeImpl.notImplemented();
         }
 
     }
 }
