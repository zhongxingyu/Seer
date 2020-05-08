 /*
  *  Copyright 2008 Sun Microsystems, Inc. All rights reserved.
  *  SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
  */
 
 package org.netbeans.modules.javafx.fxd.composer.source;
        
 import com.sun.javafx.tools.fxd.FXDReference;
 import java.io.Reader;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import javax.swing.JEditorPane;
 import javax.swing.SwingUtilities;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import org.netbeans.editor.BaseDocument;
 import org.netbeans.editor.EditorUI;
 import org.netbeans.editor.StatusBar;
 import org.netbeans.editor.Utilities;
 import org.netbeans.modules.editor.NbEditorUtilities;
 import org.netbeans.modules.editor.structure.api.DocumentElement;
 import org.netbeans.modules.editor.structure.api.DocumentModel;
 import org.netbeans.modules.editor.structure.api.DocumentModel.DocumentChange;
 import org.netbeans.modules.editor.structure.api.DocumentModel.DocumentModelModificationTransaction;
 import org.netbeans.modules.editor.structure.api.DocumentModel.DocumentModelTransactionCancelledException;
 import org.netbeans.modules.editor.structure.api.DocumentModelException;
 import org.netbeans.modules.editor.structure.spi.DocumentModelProvider;
 
 import org.netbeans.modules.javafx.fxd.composer.misc.CharSeqReader;
 import org.netbeans.modules.javafx.fxd.composer.model.FXDFileModel;
 import org.openide.cookies.EditorCookie;
 import org.openide.loaders.DataObject;
 import org.openide.util.Exceptions;
 
 import com.sun.javafx.tools.fxd.container.scene.fxd.*;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Pavel Benes
  */
 public final class FXDDocumentModelProvider implements DocumentModelProvider {
         
     private static final Map<String,String> NO_ATTRS = new HashMap<String,String>(1);
     
     private static final class NodeBuilder {
         private final String             m_typeName;
         private final int                m_startOffset;
         /** map with node's attributes */
         private final Map<String,String> m_attributes = new HashMap<String, String>();
         /** map with child attributes that have Node or List with Nodes as value.
          * Is used to check if child nodes should be removed from the model. 
          * The only possible way because ContentHandler doesn't give relations between nodes.
          */
         private final Map<String, Object> m_childNodes = new HashMap<String, Object>();
         
         private NodeBuilder( String typeName, int startOff) {
             m_typeName = typeName;
             m_startOffset = startOff;
         }
         
         public Map<String,String> getAttributeMap() {
             return m_attributes;
         }
         
         protected void addAttribute( String name, String value, int startOff, int endOff) {
             if ( !m_attributes.containsKey(name)) {
                 m_attributes.put(name, value);
             }
         }
         
         protected void build(DocumentModelModificationTransaction trans, int endOffset) throws BadLocationException, DocumentModelTransactionCancelledException {
             trans.addDocumentElement( m_typeName, FXDFileModel.FXD_NODE, m_attributes, m_startOffset, endOffset);
         }
 
         /**
          * checks if DocumentElement type and name are equal to current node
          * @param de
          * @return
          */
         public boolean isEqual(final DocumentElement de) {
             assert de != null;
             return FXDFileModel.FXD_NODE.equals(de.getType())
                     && m_typeName.equals(de.getName());
         }
 
         /**
          * Checks if given AttributeSet describes the same list of attributes as
          * current node contain.
          * @param attrs
          * @return
          */
         public boolean areAttributesEqual(final AttributeSet attrs) {
             assert attrs != null;
             if ( attrs.getAttributeCount() == m_attributes.size()) {
                 for (Entry<String, String> entry : m_attributes.entrySet()) {
                     String value1 = entry.getValue();
                     Object value2 = attrs.getAttribute(entry.getKey());
 
                     if ( value1 != value2) {
                         if ( value1 == null || !value1.equals(value2)) {
                             return false;
                         }
                     }
                 }
                 return true;
             }
             return false;
         }
 
         @Override
         public String toString() {
             return "NodeBuilder [name="+m_typeName+" startOff="+m_startOffset+"]";
         }
 
         /**
          * returns map with attributes that contain Node or List of Nodes as value.
          * @return Map with pairs (name,value).
          */
         protected Map<String, Object> getChildNodesMap(){
             return m_childNodes;
         }
 
         /**
          * adds Node or List with nodes to the map with child Nodes.
          * Do not add attributes that has primitive value or array of primitives as value.
          * @param name - name of the attribute that has given Node or
          * List of Nodes as value.
          * @param value Node or List of Nodes
          */
         protected void addNodeToChildrenMap(String name, Object value) {
             if (!m_childNodes.containsKey(name)) {
                 m_childNodes.put(name, value);
             }
         }
 
         protected boolean hasNodeInAttrValue(String nodeName) {
             Object attrNodeName = getChildNodesMap().get(nodeName);
             // attr is not present in updated node or it's type is changed to array.
             if ((attrNodeName == null || !(attrNodeName instanceof String))) {
                 return false;
             }
             return true;
         }
 
         protected List<String> getNodesNamesFromAttrValue(String nodeName) {
             Object nodesList = getChildNodesMap().get(nodeName);
             if (nodesList != null && nodesList instanceof List) {
                 return (List<String>) nodesList;
             } else {
                 return new ArrayList<String>();
             }
         }
 
     }
     
     public static final String PROP_PARSE_ERROR = "parse-error"; // NOI18N
     
     /*
     private static FXDNode s_root = null;    
     public static synchronized FXDNode getRoot() {
         return s_root;
     }*/
     
     public synchronized void updateModel(final DocumentModelModificationTransaction trans, 
             final DocumentModel model, final DocumentChange[] changes)
             throws DocumentModelException, DocumentModelTransactionCancelledException {
         //DocumentModelUtils.dumpElementStructure( model.getRootElement());
         
         BaseDocument doc = (BaseDocument) model.getDocument();
         final DataObject dObj = NbEditorUtilities.getDataObject(doc);
         
         DocumentElement rootDE = model.getRootElement();
         if ( rootDE.getElementCount() == 1) {
             DocumentElement childDE = rootDE.getElement(0);
             if ( FXDFileModel.isError(childDE)) {
                 trans.removeDocumentElement(rootDE.getElement(0), true);
             }            
         }
 
         Reader docReader = new CharSeqReader(doc.getText());
         final StringBuilder statMsg = new StringBuilder(" "); // NOI18N
                     
         try {
             FXDParser fxdParser = new FXDParser(docReader, new ContentHandler() {
                 /** is last processed element was node or not (then array) */
                 private boolean m_isLastNode = true;
                 /** last processed element - String (Node name)
                  * or List<String> ( array attribute with nodes == list of node names) */
                 private Object  m_lastElem = null;
 
                 public Object startNode(String typeName, int startOff, boolean isExtension) {
                     return new NodeBuilder(typeName, startOff);
                 }
 
                 public void attribute(Object node, String name, String value,
                         int startOff, int endOff, boolean isMeta) throws FXDException {
                     NodeBuilder deb = (NodeBuilder) node;
                     if ( value == null) {
                         try {
                             if ( m_isLastNode) {
                                 //System.err.println(String.format("Adding attribute %s <%d, %d>", name, startOff, endOff));
                                 trans.addDocumentElement(name, FXDFileModel.FXD_ATTRIBUTE, NO_ATTRS, startOff, endOff);
                             } else {
                                 //System.err.println(String.format("Adding array attribute %s <%d, %d>", name, startOff, endOff));
                                 trans.addDocumentElement(name, FXDFileModel.FXD_ATTRIBUTE_ARRAY, NO_ATTRS, startOff, endOff);
                             }
                             deb.addNodeToChildrenMap(name, m_lastElem);
                             m_lastElem = null;
                        } catch( Exception e) {
                            throw new FXDException(e);
                        }
                     }
                     // TODO parse value. This will allow to handle functions and references
                     // TODO handle meta separately
                     deb.addAttribute( name, value, startOff, endOff);
                 }
 
                 public void endNode(Object node, int endOff) throws FXDException {
                     //System.err.println("Node ended");
                     NodeBuilder deb = (NodeBuilder) node;
                     DocumentElement de = model.getLeafElementForOffset(deb.m_startOffset);
                     try {
                         if ( de != model.getRootElement() && deb.isEqual(de)) {
                             if ( !deb.areAttributesEqual(de.getAttributes())) {
                                 //System.err.println("Attributes changes for " + deb.m_typeName);
                                 trans.updateDocumentElementAttribs(de, deb.getAttributeMap());
                             }
                             synchRemovedChildNodes(deb, de);
                         } else {
                             deb.build(trans, endOff);
                         }
                     } catch( Exception e) {
                         throw new FXDException(e);
                     }
                     m_isLastNode = true;
                     m_lastElem = deb.m_typeName;
                 }
 
                 private void synchRemovedChildNodes(NodeBuilder deb, DocumentElement de)
                         throws DocumentModelTransactionCancelledException {
                     List<DocumentElement> deChildren = de.getChildren();
                     for (DocumentElement deChild : deChildren) {
                         if (FXDFileModel.FXD_ATTRIBUTE_ARRAY.equals(deChild.getType())) {
                            synchArrayOfChildNodes(deb, deChild);
                         } else if (FXDFileModel.FXD_ATTRIBUTE.equals(deChild.getType())) {
                             if (!deb.hasNodeInAttrValue(deChild.getName())) {
                                 trans.removeDocumentElement(deChild, true);
                             }
                         }
                     }
                 }
 
                 private void synchArrayOfChildNodes(NodeBuilder deb, DocumentElement de) 
                         throws DocumentModelTransactionCancelledException {
                     List<String> nodeNamesList = deb.getNodesNamesFromAttrValue(de.getName());
                     for (DocumentElement child : de.getChildren()) {
                         if (FXDFileModel.FXD_NODE.equals(child.getType())) {
                            // TODO: fix the case when one of children with same names was removed.
                            // e.g. content with several polygons.
                             if (!nodeNamesList.contains(child.getName())) {
                                 trans.removeDocumentElement(child, true);
                             }
                         }
                     }
                 }
 
                 public Object startNodeArray(int startOff) {
                     //System.err.println("Array started");
                     return new ArrayList<String>();
                 }
 
                 public void arrayElement(Object array, String value, int startOff, int endOff) throws FXDException {
                     if ( value != null) {
                         try {
                             trans.addDocumentElement(value, FXDFileModel.FXD_ARRAY_ELEM, NO_ATTRS, startOff, endOff);
                         } catch (Exception ex) {
                             throw new FXDException( ex);
                         }
                     } else {
                         ((List)array).add(m_lastElem);
                         m_lastElem = null;
                     }
                 }
 
                 public void endNodeArray(Object array, int endOff) {
                     m_lastElem = array;
                     m_isLastNode = false;
                 }
 
                 public void parsingStarted(FXDParser parser) {
                     // do nothing. we do not need parser onstance
                 }
 
                 public FXDReference createReference(String str) throws FXDException {
                     // TODO: should implement?
                     // implementation is necessary if we parse attribute or array element value.
                     // But we use them as they come.
                     return null;
                 }
 
             });
 
             showStatusText(dObj, " Parsing text..."); // NOI18N
 
             fxdParser.parseObject();
             reportDeletedElements(trans, model.getRootElement());
             doc.putProperty( PROP_PARSE_ERROR, null);
         } catch( DocumentModelTransactionCancelledException e) {
             //s_root = null;
             throw e;
         } catch (Exception ex) {
             if ( ex instanceof FXDSyntaxErrorException) {
                 statMsg.append( "Syntax error: "); //NOI18N
             } else {
                 statMsg.append( "Unknown error: "); //NOI18N
             }
             String msg = ex.getLocalizedMessage();
             statMsg.append(msg);
             doc.putProperty( PROP_PARSE_ERROR, msg);
             cleanModel(trans, model);
             try {
                 trans.addDocumentElement("Invalid FXD syntax", FXDFileModel.FXD_ERROR, NO_ATTRS, 0, doc.getLength()); // NOI18N
             } catch (BadLocationException ex1) {
                 Exceptions.printStackTrace(ex1);
             }
         } finally {
             showStatusText(dObj, statMsg.toString());
         }
     }
 
     protected static void showStatusText( DataObject dObj, final String msg) {
         final EditorCookie ec = dObj.getCookie( EditorCookie.class);
         if ( ec != null) {
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                     final JEditorPane [] panes = ec.getOpenedPanes();
                     if ( panes != null && panes.length > 0 && panes[0] != null) {
                         EditorUI eui = Utilities.getEditorUI(panes[0]);
                         StatusBar sb = eui == null ? null : eui.getStatusBar();
                         if (sb != null) {
                             sb.setText(SourceEditorWrapper.CELL_ERROR, msg);
                         }
                     }
                 }
             });
         }
     }
     
     protected void cleanModel( final DocumentModelModificationTransaction trans, DocumentModel model) throws DocumentModelTransactionCancelledException {
         DocumentElement root = model.getRootElement();
         for ( int i = root.getElementCount() - 1; i >= 0; i--) {
             trans.removeDocumentElement( root.getElement(i), true);
         }
     }
     
     protected void reportDeletedElements(final DocumentModelModificationTransaction trans, DocumentElement de) throws DocumentModelTransactionCancelledException {
         assert de != null;
         if ( de.getStartOffset() == de.getEndOffset()) {
             trans.removeDocumentElement(de, true);
         } else {
             for (int i = de.getElementCount()-1; i >= 0; i--) {
                 reportDeletedElements(trans, de.getElement(i));
             }
         }
     }
 }
