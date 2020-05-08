 /*
  *  Copyright 2008 Sun Microsystems, Inc. All rights reserved.
  *  SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
  */
 
 package org.netbeans.modules.javafx.fxd.composer.model;
 
 import com.sun.javafx.tools.fxd.FXDArrayElement;
 import com.sun.javafx.tools.fxd.FXDObjectElement;
 import com.sun.javafx.tools.fxd.FXDReference;
 import com.sun.javafx.tools.fxd.FXDRootElement;
 import com.sun.javafx.tools.fxd.container.scene.fxd.FXDException;
 import com.sun.javafx.tools.fxd.container.scene.fxd.FXDSyntaxErrorException;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 
 import org.netbeans.modules.editor.structure.api.DocumentElement;
 import com.sun.javafx.tools.fxd.container.ContainerEntry;
 
 import com.sun.javafx.tools.fxd.container.scene.fxd.ContentHandler;
 import com.sun.javafx.tools.fxd.container.scene.fxd.FXDParser;
 import java.io.Reader;
 import java.util.Collections;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.netbeans.modules.javafx.fxd.schemamodel.FXDSchemaHelper;
 import org.openide.util.Exceptions;
 
 /**
  *
  * @author Pavel Benes
  */
 final class DocumentElementWrapper {
     private DocumentElementWrapper() {}
 
     private static abstract class FXDElementWrapper implements com.sun.javafx.tools.fxd.FXDElement {
         protected DocumentElement m_de;
         protected WrappingInfo m_info;
         protected List<DocumentElement> m_children;
         private static FXDParser m_parser = createParser();
 
         public FXDElementWrapper( WrappingInfo info, final DocumentElement de) {
             m_de = de;
             m_info = info;
             m_children = m_de.getChildren();
 
             /* it is not necessary. remove in case will not find cases when it is useful
             m_de.addDocumentElementListener(new DocumentElementListener() {
 
                 public void elementAdded(DocumentElementEvent dee) {
                     //System.out.println(m_de.getName()+ " elementAdded "+dee.getChangedChild());
                     m_children = m_de.getChildren();
                 }
 
                 public void elementRemoved(DocumentElementEvent dee) {
                     //System.out.println(m_de.getName()+ " elementRemoved "+dee.getChangedChild());
                     m_children = m_de.getChildren();
                 }
 
                 public void childrenReordered(DocumentElementEvent dee) {
                     //System.out.println(m_de.getName()+ " childrenReordered "+dee.getChangedChild());
                 }
 
                 public void contentChanged(DocumentElementEvent dee) {
                     //System.out.println(m_de.getName()+ " contentChanged "+dee.getChangedChild());
                 }
 
                 public void attributesChanged(DocumentElementEvent dee) {
                     //System.out.println(m_de.getName()+ " attributesChanged "+dee.getChangedChild());
                 }
             });
              */
         }
         
         public void release() {
             m_de = null;
             m_children = null;
         }
 
         private static FXDParser createParser() {
             try {
                 return new FXDParser(new FakeReader(), new FakeContentHandler());
             } catch (IOException ex) {
                 Exceptions.printStackTrace(ex);
             }
             return null;
         }
 
         Object parseValue(String strValue) {
             try {
                 if (isReferenceStr(strValue)){
                     Object property = resolvePropertyValue(strValue);
                     if (property != null){
                         return property;
                     }
                 }
                 return FXDParser.parseValue(strValue, m_parser);
             } catch (IOException ex) {
                 Logger.getLogger(this.getClass().getName()).
                         log(Level.WARNING, "Exception while resolving reference for \"" + strValue + "\" ", ex);
             } catch (FXDSyntaxErrorException ex) {
                 Logger.getLogger(this.getClass().getName()).
                         log(Level.WARNING, "Exception while parsing or resolving reference for \"" + strValue + "\" value", ex);
             } catch (FXDException ex) {
                 Logger.getLogger(this.getClass().getName()).
                         log(Level.WARNING, "Exception while parsing \"" + strValue + "\" value", ex);
             }
             return strValue;
         }
 
         private Object resolvePropertyValue(String strValue)
                 throws FXDSyntaxErrorException, IOException {
             FXDReference reference = FXDReference.parse(strValue, this);
             final Object[] property = new Object[1];
             reference.setResolutionListener(new FXDReference.ResolutionListener() {
 
                 public void resolved(FXDReference ref) {
                     FXDObjectElement elem = ref.getReferencedElement();
                    property[0] = elem.getAttrValue(ref.getProperty());
                     m_info.rmUnresolved(m_de);
                 }
             });
             if (reference.isLocal(m_info.getEntry())) {
                 m_info.addUnresolved(m_de);
                 if (reference.resolveRerefence(m_info.getRoot()) == false) {
                     throw new FXDSyntaxErrorException("The reference '" + reference.getText() + "' at " + reference.getContext() + " cannot be resolved");
                 }
             }
             return property[0];
         }
     }
 
     private static final Enumeration ID_ELEM = new Enumeration() {
         public boolean hasMoreElements() {
             return true;
         }
 
         public Object nextElement() {
             return FXDObjectElement.ATTR_NAME_ID;
         }
     };
 
     private static class FXDExtendedNodeWrapper extends FXDNodeWrapper {
 
         private FXDReference m_reference;
         private com.sun.javafx.tools.fxd.FXDObjectElement m_refObj;
         private String m_typeName;
 
         public FXDExtendedNodeWrapper(WrappingInfo info, final DocumentElement de, com.sun.javafx.tools.fxd.FXDElement parent) {
             super(info, de);
             initReference(parent);
         }
 
         private void initReference(com.sun.javafx.tools.fxd.FXDElement parent) {
             try {
                 m_reference = FXDReference.parse(m_de.getName(), parent);
                 m_reference.setResolutionListener(new FXDReference.ResolutionListener() {
 
                     public void resolved(FXDReference ref) {
                         m_refObj = m_reference.getReferencedElement();
                         m_typeName = m_refObj.getTypeName();
                         m_info.rmUnresolved(m_de);
                     }
                 });
                 if (m_reference.isLocal(m_info.getEntry())) {
                     m_info.addUnresolved(m_de);
                     if (m_reference.resolveRerefence(m_info.getRoot()) == false) {
                         throw new FXDSyntaxErrorException("The reference '" + m_reference.getText() + "' at " + m_reference.getContext() + " cannot be resolved");
                     }
                 }
             } catch (IOException ex) {
                 Logger.getLogger(this.getClass().getName()).
                         log(Level.WARNING, "Exception while parsing \"" + m_de.getName() + "\" node name", ex);
             } catch (FXDSyntaxErrorException ex) {
                 Logger.getLogger(this.getClass().getName()).
                         log(Level.WARNING, "Exception while parsing \"" + m_de.getName() + "\" node name", ex);
             }
         }
 
         @Override
         public String getTypeName() {
             return m_typeName != null ? m_typeName : super.getTypeName();
         }
 
         @Override
         public int getAttrNum() {
             checkState();
             return super.getAttrNum() + m_refObj.getAttrNum();
         }
 
         @Override
         public Object getAttrValue(String name) {
             checkState();
             Object value = super.getAttrValue(name);
             if (value == null) {
                 value = m_refObj.getAttrValue(name);
             }
 //            if (value == null && m_typeName == null) {
 //                if (FXDObjectElement.ATTR_NAME_ID.equals(name)) {
 //                    value = FXDObjectElement.VALUE_NOT_READY;
 //                } else {
 //                    throw new IllegalStateException("The reference " + m_fxdRef + " must be resolved first.");
 //                }
 //            }
             return value;
         }
 
         @Override
         public Enumeration getAttrNames() {
             checkState();
             return merge(super.getAttrNames(), m_refObj.getAttrNames());
         }
 
         @Override
         public boolean isLeaf() {
             checkState();
             return super.isLeaf() && m_refObj.isLeaf();
         }
 
         @Override
         public Enumeration children() {
             checkState();
             final Enumeration e1 = super.children();
             final Enumeration e2 = m_refObj.children();
             return new Enumeration() {
 
                 public boolean hasMoreElements() {
                     return e1.hasMoreElements() && e2.hasMoreElements();
                 }
 
                 public Object nextElement() {
                     if (e1.hasMoreElements()) {
                         return e1.nextElement();
                     } else {
                         return e2.nextElement();
                     }
                 }
             };
         }
 
         @Override
         public String toString() {
             checkState();
             // TODO merge
             return super.toString() + "  references to [ "+m_refObj.toString()+" ]";
         }
 
         private void checkState() {
             if (m_typeName == null) {
                 throw new IllegalStateException("The reference " + m_reference + " must be resolved first.");
             }
         }
 
         private static Enumeration merge(Enumeration toEnum, Enumeration fromEnum){
             ArrayList toArr = Collections.list(toEnum);
             Object el;
             while (fromEnum.hasMoreElements()){
                 el = fromEnum.nextElement();
                 if (!toArr.contains(el)){
                     toArr.add(el);
                 }
             }
             return Collections.enumeration(toArr);
         }
 
     }
 
     private static class FXDNodeWrapper extends FXDElementWrapper implements FXDObjectElement, Enumeration {
         private final boolean     m_injectID;
         private       Enumeration m_attrEnum = null;
         protected     int         m_refID = -1;
 
         public FXDNodeWrapper(WrappingInfo info, final DocumentElement de) {
             super(info, de);
             m_injectID = m_de.getAttributes().isDefined(FXDObjectElement.ATTR_NAME_ID) == false &&
                          isIDSupported(getTypeName());
         }        
 
         public String getTypeName() {
             return m_de.getName();
         }
 
         public int getAttrNum() {
             int attrNum = m_de.getAttributes().getAttributeCount();
             if ( !m_injectID) {
                 attrNum++;
             }
             return attrNum;
         }
 
         public Object getAttrValue(String name) {
           if ( FXDObjectElement.ATTR_NAME_ID.equals(name)) {
                 //provide ID connected to the element start offset only if element doesn't have id.
 
                 return FXDFileModel.getElementId(m_de);
             } else {
                 String strValue = (String) m_de.getAttributes().getAttribute(name);
                 if ( strValue != null) {
                     return parseValue(strValue);
                 } else {
                     if ( m_de.getAttributes().isDefined(name)) {
                         for ( DocumentElement cde : m_children) {
                             if ( name.equals(cde.getName())) {
                                 if ( FXDFileModel.FXD_ATTRIBUTE.equals( cde.getType())) {
                                     assert cde.getElementCount() == 1;
                                     return wrap(m_info, cde.getChildren().get(0), FXDNodeWrapper.this);
                                 } else {
                                     assert FXDFileModel.FXD_ATTRIBUTE_ARRAY.equals( cde.getType());
                                     return wrap(m_info, cde, FXDNodeWrapper.this);
                                 }
                             }
                         }
                         System.err.println("Attribute " + name + " not found on the element " + m_de.getName() + "!"); //NOI18N
                     }
                     return null;
                 }
             }
         }
 
         @SuppressWarnings("unchecked")         
         public Enumeration getAttrNames() {
            Enumeration attrNames = m_de.getAttributes().getAttributeNames();
             assert attrNames != null;
 
             if ( m_injectID) {
                 m_attrEnum = ID_ELEM;
                 return this;
             } else {
                 return attrNames;
             }
         }
 
         public int getKind() {
             return com.sun.javafx.tools.fxd.FXDElement.KIND_OBJECT;
         }
 
         public boolean isLeaf() {            
             return m_de.getElementCount() == 0;
         }
 
         public Enumeration children() {
             final List<DocumentElement> childDE = new ArrayList<DocumentElement>();
             collectChildren(m_de, childDE);
             return new Enumeration() {
                 private int m_index = 0;
 
                 public boolean hasMoreElements() {
                     return m_index < childDE.size();
                 }
 
                 public Object nextElement() {
                     if ( m_index >= childDE.size()) {
                         throw new NoSuchElementException();
                     }
                     return wrap( m_info, childDE.get(m_index++), FXDNodeWrapper.this);
                 }
             };
         }
 
         @Override
         public void release() {
             super.release();
             m_attrEnum = null;
         }
 
         public boolean hasMoreElements() {
             assert m_attrEnum != null;
             return m_attrEnum.hasMoreElements();
         }
 
         public Object nextElement() {
            assert m_attrEnum != null;
             Object o = m_attrEnum.nextElement();
             if ( m_attrEnum == ID_ELEM) {
                 m_attrEnum = m_de.getAttributes().getAttributeNames();
             }
             return o;
         }
 
         public synchronized int getReferenceID() {
             return m_refID;
         }
 
 
         public synchronized void setReferenceID(int refID) {
             m_refID = refID;
         }
 
         // TODO do we need and can support meta? or 
         public int getMetaNum() {
             return 0;
         }
 
         public Enumeration getMetaKeys() {
             return new Enumeration() {
 
                 public boolean hasMoreElements() {
                     return false;
                 }
 
                 public Object nextElement() {
                     return null;
                 }
             };
         }
 
         public Object getMetaValue(String string) {
             return null;
         }
     }
 
     private static void collectChildren( final DocumentElement de, List<DocumentElement> childrenList) {
         List<DocumentElement> children = de.getChildren();
         for (DocumentElement child : children){
             String type = child.getType();
             if ( FXDFileModel.FXD_NODE.equals( type) || 
                  FXDFileModel.FXD_ATTRIBUTE_ARRAY.equals(type)) {
                 childrenList.add( child);
             } else {
                 collectChildren(child, childrenList);
             }
         }
     }
 
     // TODO support extended roon nodfe wrapper
     private static final class FXDRootNodeWrapper extends FXDNodeWrapper implements FXDRootElement {
         public FXDRootNodeWrapper(WrappingInfo info, final DocumentElement de) {
             super(info, de);
             m_info.setRoot(this);
         }
         
         @Override
         public Object getAttrValue(String name) {  //NOI18N
             if ("cache".equals(name)) { // NOI18N
                 return Boolean.TRUE;
             } else {
                 return super.getAttrValue(name);
             }
         }
 
         public int getAllChildrenNum() {
             return -1;
         }
 
     }
     
     private static final class FXDNodeArrayWrapper extends FXDElementWrapper implements FXDArrayElement {
 
         public FXDNodeArrayWrapper(WrappingInfo info, final DocumentElement de) {
             super(info, de);
         }        
 
         public int getLength() {
             return m_de.getElementCount();
         }
 
         public Object elementAt(int index) {
             DocumentElement de = m_children.get(index);
 
             if ( FXDFileModel.FXD_ARRAY_ELEM.equals(de.getType())){
                 return parseValue(de.getName());
             } else {
                 return wrap( m_info, de, FXDNodeArrayWrapper.this);
             }
         }
 
         public int getKind() {
             return com.sun.javafx.tools.fxd.FXDElement.KIND_ARRAY;
         }
 
         public boolean isLeaf() {
             return m_de.getElementCount() == 0;
         }
 
         public Enumeration children() {
             return new Enumeration() {
                 private int m_index = advance(0);
 
                 public boolean hasMoreElements() {
                     return m_index < m_children.size();
                 }
 
                 public Object nextElement() {
                     if ( m_index >= m_children.size()) {
                         throw new NoSuchElementException();
                     }
                     com.sun.javafx.tools.fxd.FXDElement elem =
                             wrap( m_info, m_children.get(m_index), FXDNodeArrayWrapper.this);
                     m_index = advance(m_index+1);
                     return elem;
                 }
                 
                 private int advance(int index) {
                     while( index < m_children.size() &&
                        FXDFileModel.FXD_ARRAY_ELEM.equals(m_children.get(index).getType())) {
                        index++;
                     }
                     return index;
                 }
             };
         }        
     }
 
     /**
      * wraps root DocumentElemnt into com.sun.javafx.tools.fxd.FXDElement.
      * If you want to wrap non-root element, use wrap( DocumentElement, FXDElement) method.
      * @param de root DocumentElement
      * @return FXDElement
      */
     public static com.sun.javafx.tools.fxd.FXDElement wrap( ContainerEntry ce, DocumentElement de) {
         return wrap(new WrappingInfo(ce, null), de, null);
     }
 
     /**
      * wraps non-root DocumentElemnt into com.sun.javafx.tools.fxd.FXDElement.
      * Providing parent FXDElement is necessary.
      * @param de root DocumentElement
      * @param parent parent FXDElement. If null, de is wrapped as root element
      * @return FXDElement
      */
     static com.sun.javafx.tools.fxd.FXDElement wrap(WrappingInfo info,
             final DocumentElement de, com.sun.javafx.tools.fxd.FXDElement parent) {
         if (info.isUnresolved(de)){
             //Logger.getLogger(DocumentElementWrapper.class.getName()).
             //        info("skip wrapping of unresolved refenence in DE: "+de);
             return null;
         }
         String type = de.getType();
         if ( FXDFileModel.FXD_NODE.equals( type)) {
             if (isReferenceStr(de.getName())) {
                 return parent == null ? new FXDRootNodeWrapper(info, de) : new FXDExtendedNodeWrapper(info, de, parent);
             } else {
                 return parent == null ? new FXDRootNodeWrapper(info, de) : new FXDNodeWrapper(info, de);
             }
         } else if ( FXDFileModel.FXD_ATTRIBUTE_ARRAY.equals(type)) {
             return new FXDNodeArrayWrapper(info, de);
         } else if ( FXDFileModel.FXD_ERROR.equals(type)) {
             // nothing to do
             return null;
         } else {    
             throw new RuntimeException( "Unknown DocumentElement type: " + type); //NOI18N
         }
     }
 
     private static Map<String,Boolean> CLASSES_WITH_ID = new HashMap<String, Boolean>();
     private final static String REF_SEPARATOR = String.valueOf((char)FXDReference.REF_SEPARATOR);
 
     public static boolean isIDSupported( String typeName) {
         Boolean state = CLASSES_WITH_ID.get(typeName);
         if ( state == null) {
             state = Boolean.valueOf( isIDSupportedImpl(typeName));
             CLASSES_WITH_ID.put(typeName, state);
         }
         return state.booleanValue();
     }
 
     static boolean isReferenceStr(String str){
         if (str == null || str.length() == 0){
             return false;
         }
         if (str.startsWith(REF_SEPARATOR) || str.startsWith(FXDReference.REF_PREFIX_WITHOUT_COLON)){
             return true;
         }
         return false;
     }
 
     private static boolean isIDSupportedImpl( String typeName) {
         for ( String importStr : FXDSchemaHelper.getDefaultImports()) {
             String className = importStr.concat(typeName);
             try {
                 Class clazz = Class.forName(className);
                 try {
                     Method m = clazz.getMethod("get$id");
                     assert m != null;
                     return true;
                 } catch( Exception e) {
                     return false;
                 }
             } catch (ClassNotFoundException ex) {
             }
         }
         return false;
     }
 
     private static class FakeReader extends Reader {
 
         @Override
         public int read(char[] cbuf, int off, int len) throws IOException {
             return 0;
         }
 
         @Override
         public void close() throws IOException {
         }
     }
 
     private static class FakeContentHandler implements ContentHandler {
 
         public void parsingStarted(FXDParser fxdp) {
         }
 
         public Object startNode(String string, int i, boolean bln) throws FXDException {
             return null;
         }
 
         public Object startNodeArray(int i) throws FXDException {
             return null;
         }
 
         public com.sun.javafx.tools.fxd.FXDReference createReference(String string) throws FXDException {
             // TODO if we have
             // TODO support references?
             return null;
         }
 
         public void attribute(Object o, String string, String string1, int i, int i1, boolean bln) throws FXDException {
         }
 
         public void endNode(Object o, int i) throws FXDException {
         }
 
         public void arrayElement(Object o, String string, int i, int i1) throws FXDException {
         }
 
         public void endNodeArray(Object o, int i) throws FXDException {
         }
     }
 }
