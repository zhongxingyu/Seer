 package org.cagrid.cql.utilities.encoding;
 
 import java.io.IOException;
 
 import javax.xml.namespace.QName;
 
 import org.apache.axis.encoding.DeserializationContext;
 import org.apache.axis.encoding.Deserializer;
 import org.apache.axis.encoding.DeserializerImpl;
 import org.apache.axis.message.MessageElement;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.exolab.castor.mapping.Mapping;
 import org.exolab.castor.mapping.MappingException;
 import org.exolab.castor.xml.MarshalException;
 import org.exolab.castor.xml.Unmarshaller;
 import org.exolab.castor.xml.ValidationException;
 import org.w3c.dom.Element;
 import org.xml.sax.InputSource;
 
 public class Cql2Deserializer extends DeserializerImpl implements Deserializer {
     public QName xmlType;
     public Class<?> javaType;
     
     protected static Log LOG = LogFactory.getLog(Cql2Deserializer.class.getName());
 
 
     public Cql2Deserializer(Class<?> javaType, QName xmlType) {
         super();
         this.xmlType = xmlType;
         this.javaType = javaType;
     }
     
     
    public void onEndElement(String namespace, String localName, DeserializationContext context) {
         long start = System.currentTimeMillis();
         
         // load the mapping
         Mapping map = new Mapping();
         try {
             map.loadMapping(new InputSource(Cql2SerialzationHelper.getMappingStream()));
         } catch (IOException ex) {
             String error = "Error loading CQL 2 castor mapping: " + ex.getMessage();
             LOG.error(error, ex);
         }
        
         Unmarshaller unmarshall = new Unmarshaller(javaType);
         try {
             unmarshall.setMapping(map);
         } catch (MappingException ex) {
             String error = "Error setting CQL 2 castor mapping: " + ex.getMessage();
             LOG.error(error, ex);
         }
 
         MessageElement msgElem = context.getCurElement();
         Element asDOM = null;
         try {
             asDOM = msgElem.getAsDOM();
         } catch (Exception ex) {
             String error = "Problem extracting message type! Result will be null! " + ex.getMessage();
             LOG.error(error, ex);
         }
         if (asDOM != null) {
             try {
                 value = unmarshall.unmarshal(asDOM);
             } catch (MarshalException e) {
                LOG.error("Problem with CQL 2 castor unmarshalling!", e);
             } catch (ValidationException e) {
                LOG.error("CQL 2 XML does not match schema!", e);
             }
         }
         LOG.trace("Derialized " + localName + " in " + (System.currentTimeMillis() - start) + " ms");
     }
 }
