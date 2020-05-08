 package org.codehaus.xfire.xmlbeans;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.xml.namespace.QName;
 import javax.xml.stream.XMLStreamException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.xmlbeans.SchemaProperty;
 import org.apache.xmlbeans.SchemaType;
 import org.apache.xmlbeans.XmlCursor;
 import org.apache.xmlbeans.XmlException;
 import org.apache.xmlbeans.XmlObject;
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.aegis.MessageReader;
 import org.codehaus.xfire.aegis.MessageWriter;
 import org.codehaus.xfire.aegis.stax.ElementReader;
 import org.codehaus.xfire.aegis.stax.ElementWriter;
 import org.codehaus.xfire.aegis.type.Type;
 import org.codehaus.xfire.fault.XFireFault;
 import org.codehaus.xfire.soap.SoapConstants;
 import org.codehaus.xfire.util.STAXUtils;
 import org.codehaus.yom.Document;
 import org.codehaus.yom.Element;
 import org.codehaus.yom.Elements;
 import org.codehaus.yom.stax.StaxBuilder;
 import org.codehaus.yom.xpath.YOMXPath;
 import org.jaxen.JaxenException;
 import org.jaxen.XPath;
 
 /**
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  * @since Nov 13, 2004
  */
 public class XmlBeansType 
     extends Type
 {
     private SchemaType schemaType;
 
     private final static StaxBuilder builder = new StaxBuilder();
     private final static Log logger = LogFactory.getLog(XmlBeansType.class); 
     
     public XmlBeansType()
     {
     }
     
     public XmlBeansType(SchemaType schemaType)
     {
         this.schemaType = schemaType;
     }
 
     public XmlBeansType(Class clazz)
     {
         this.schemaType = XmlBeansServiceFactory.getSchemaType(clazz);
         setTypeClass(clazz);
     }
 
     public void writeSchema(Element root)
     {
         try
         {
             Element schema = builder.buildElement(null, getSchema().newXMLStreamReader());
             Document schemaDoc = new Document(schema);
             
             String ns = getSchemaType().getNamespaceURI();
             String expr = "//xsd:schema[@targetNamespace='" + ns + "']";
 
             List nodes = getMatches(schema, expr);
             if (nodes.size() == 0)
             {
                 logger.warn("No schema found for " + expr);
                 return;
             }
             
             Element node = (Element) nodes.get(0);
             Elements children = node.getChildElements();
             
             for (int i = 0; i < children.size(); i++)
             {
                 Element child = children.get(i);
                 
                 if (hasChild(root, child)) return;
                 
                 child.detach();
                 root.appendChild(child);
             }
         }
         catch (XMLStreamException e)
         {
             throw new XFireRuntimeException("Couldn't parse schema.", e);
         }
     }
 
     private boolean hasChild(Element root, Element child)
     {
         String expr = "//xsd:" + child.getLocalName() + 
            "[@name='" + child.getAttributeValue("name") + "']";
         
         List children = getMatches(root, expr);
         
         if (children.size() > 0) return true;
         
         return false;
     }
 
     private List getMatches(Object doc, String xpath)
     {
         try
         {
             XPath path = new YOMXPath(xpath);
             path.addNamespace("xsd", SoapConstants.XSD);
             List result = path.selectNodes(doc);
             return result;
         }
         catch(JaxenException e)
         {
             throw new XFireRuntimeException("Error evaluating xpath " + xpath, e);
         }
     }
 
     public XmlObject getSchema()
     {
         String name = schemaType.getSourceName();
         ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
         try
         {
             return XmlObject.Factory.parse(classLoader.getResourceAsStream("schema/src/" + name));
         }
         catch (Exception e)
         {
             throw new XFireRuntimeException("Couldn't load schema.", e);
         }
     }
     
     public boolean isComplex()
     {
         return !schemaType.isPrimitiveType();
     }
 
     public boolean isAbstract()
     {
         return schemaType.isAbstract();
     }
 
     public Set getDependencies()
     {
         SchemaProperty[] properties = schemaType.getProperties();
         HashSet deps = new HashSet();
         for (int i = 0; i < properties.length; i++)
         {
             SchemaType etype = properties[i].getType();
             SchemaProperty[] iprops = etype.getElementProperties();
             for (int j = 0; j < iprops.length; j++)
             {
                 SchemaType itype = iprops[j].getType();
                 
                 if (!itype.isPrimitiveType())
                 {
                     deps.add(new XmlBeansType(itype));
                 }
             }
         }
         return deps;
     }
 
     public QName getSchemaType()
     {
         if (schemaType.isDocumentType())
             return schemaType.getDocumentElementName();
         else
             return schemaType.getName();
     }
 
     public Object readObject(MessageReader reader, MessageContext context)
         throws XFireFault
     {
         try
         {
             return XmlObject.Factory.parse(((ElementReader)reader).getXMLStreamReader());
         }
         catch( XmlException e )
         {
             throw new XFireFault("Could not read request.", e, XFireFault.SENDER);
         }
     }
 
     public void writeObject(Object value, MessageWriter writer, MessageContext context)
         throws XFireFault
     {
         try
         {
             XmlObject obj = (XmlObject) value; 
        
             XmlCursor cursor = obj.newCursor();
             if (cursor.toFirstChild() && cursor.toFirstChild())
             {
                 do
                 {
                     STAXUtils.copy(cursor.newXMLStreamReader(), 
                                    ((ElementWriter) writer).getXMLStreamWriter());
                 }
                 while(cursor.toNextSibling());
             }
         } 
         catch (XMLStreamException e)
         {
             throw new XFireFault("Could not write response.", e, XFireFault.SENDER);
         }
     }
 }
