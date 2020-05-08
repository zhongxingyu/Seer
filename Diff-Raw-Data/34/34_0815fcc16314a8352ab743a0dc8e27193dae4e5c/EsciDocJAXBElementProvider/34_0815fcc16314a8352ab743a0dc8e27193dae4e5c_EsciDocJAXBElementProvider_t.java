 package org.escidoc.core.util.xml.internal;
 
 import java.io.IOException;
 import java.io.InputStream;
import java.io.OutputStream;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Type;
import java.util.HashMap;
 import java.util.Map;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.validation.Schema;
 
 import org.apache.cxf.jaxrs.ext.Nullable;
 import org.apache.cxf.jaxrs.provider.JAXBElementProvider;
 import org.apache.cxf.jaxrs.utils.AnnotationUtils;
 import org.apache.cxf.jaxrs.utils.InjectionUtils;
 import org.apache.cxf.jaxrs.utils.schemas.SchemaHandler;
 import org.apache.cxf.staxutils.DepthExceededStaxException;
 import org.apache.cxf.staxutils.transform.TransformUtils;
import org.escidoc.core.domain.properties.java.PropertiesTypeTO;

import de.escidoc.core.common.util.configuration.EscidocConfiguration;
 
 /**
  * eSciDoc specific implementation of {@link JAXBElementProvider}. Generates stylesheet-header with given
  * stylesheet-path
  *
  * @author Michael Hoppe
  */
 @Produces({"application/xml", "application/*+xml", "text/xml"})
 @Consumes({"application/xml", "application/*+xml", "text/xml"})
 @Provider
 public class EsciDocJAXBElementProvider extends JAXBElementProvider<Object> {
 
     private final String XML_HEADERS_PATH = "com.sun.xml.bind.xmlHeaders";
 
     private final String PROPERTIES_DOCTYPE =
         "<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\n";
 
     private JAXBContextProvider jaxbContextProvider;
 
     private SchemaHandler schemaHandler;
 
     private Map<String, Object> maProperties;
 
     private Map<String, Object> maPropertiesWithDoctype;
 
     @Override
     /**
      * Save 2 properties-sets.
      * One without DOCTYPE-element and one With DOCTYPE-element for JavaUtilPropertiesTO.
      */
     public void setMarshallerProperties(Map<String, Object> marshallProperties) {
         overrideXmlHeadersProperty(marshallProperties);
         super.setMarshallerProperties(marshallProperties);
         maProperties = marshallProperties;
         maPropertiesWithDoctype = new HashMap<String, Object>(marshallProperties);
         if (maPropertiesWithDoctype.get(XML_HEADERS_PATH) != null) {
             maPropertiesWithDoctype.put(XML_HEADERS_PATH, maPropertiesWithDoctype.get(XML_HEADERS_PATH)
                 + PROPERTIES_DOCTYPE);
         }
         else {
             maPropertiesWithDoctype.put(XML_HEADERS_PATH, PROPERTIES_DOCTYPE);
         }
     }
 
     @Override
     public void setSchemaHandler(SchemaHandler schemaHandler) {
         this.schemaHandler = schemaHandler;
     }
 
     @Override
     protected Schema getSchema() {
         return schemaHandler.getSchema();
     }
 
     public void setJaxbContextProvider(final JAXBContextProvider jaxbContextProvider) {
         this.jaxbContextProvider = jaxbContextProvider;
     }
 
     /**
      * Hotfix for https://issues.apache.org/jira/browse/CXF-4380
      *
      * TODO: Remove after next CXF update.
      */
     public Object readFrom(Class<Object> type, Type genericType, Annotation[] anns, MediaType mt,
         MultivaluedMap<String, String> headers, InputStream is)
         throws IOException {
 
         if (isPayloadEmpty()) {
             if (AnnotationUtils.getAnnotation(anns, Nullable.class) != null) {
                 return null;
             } else {
                 reportEmptyContentLength();
             }
         }
 
         try {
 
             boolean isCollection = InjectionUtils.isSupportedCollectionOrArray(type);
             Class<?> theGenericType = isCollection ? InjectionUtils.getActualType(genericType) : type;
             Class<?> theType = getActualType(theGenericType, genericType, anns);
 
             Unmarshaller unmarshaller = createUnmarshaller(theType, genericType, isCollection);
             addAttachmentUnmarshaller(unmarshaller);
             Object response = null;
             if (JAXBElement.class.isAssignableFrom(type)
                 || !isCollection && (unmarshalAsJaxbElement
                                      || jaxbElementClassMap != null &&
                                         jaxbElementClassMap.containsKey(theType.getName()))) {
                 XMLStreamReader reader = getStreamReader(is, type, mt);
                 response = unmarshaller.unmarshal(TransformUtils.createNewReaderIfNeeded(reader, is));
             } else {
                 response = doUnmarshal(unmarshaller, type, is, mt);
             }
             if (response instanceof JAXBElement && !JAXBElement.class.isAssignableFrom(type)) {
                 response = ((JAXBElement<?>) response).getValue();
             }
             if (isCollection) {
                 response = ((CollectionWrapper) response).getCollectionOrArray(theType, type,
                     org.apache.cxf.jaxrs.utils.JAXBUtils.getAdapter(theGenericType, anns));
             } else {
                 response = checkAdapter(response, type, anns, false);
             }
             return type.cast(response);
 
         } catch (JAXBException e) {
             handleJAXBException(e, true);
         } catch (DepthExceededStaxException e) {
             throw new WebApplicationException(413);
         } catch (WebApplicationException e) {
             throw e;
         } catch (Exception e) {
             //LOG.warning(getStackTrace(e));
             throw new WebApplicationException(e, Response.status(400).build());
         }
         // unreachable
         return null;
     }
 
     public JAXBContext getJAXBContext(Class<?> type, Type genericType)
         throws JAXBException {
         if (this.jaxbContextProvider != null) {
             return this.jaxbContextProvider.getJAXBContext();
         } else {
             return super.getJAXBContext(type, genericType);
         }
     }
 
     protected Unmarshaller createUnmarshaller(Class<?> cls, Type genericType, boolean isCollection)
         throws JAXBException {
         if (super.getSchema() == null) {
             super.setSchema(getSchema());
         }
         return super.createUnmarshaller(cls, genericType, isCollection);
     }
 
     @Override
     /**
      * set DOCTYPE-element in xml if xml has schema java.util.properties
      *
      */
     protected void marshal(
         Object obj, Class<?> cls, Type genericType, String enc, OutputStream os, MediaType mt, Marshaller ms)
         throws Exception {
        if (obj instanceof PropertiesTypeTO) {
             try {
                 super.setMarshallerProperties(maPropertiesWithDoctype);
                 super.marshal(obj, cls, genericType, enc, os, mt, ms);
             }
             finally {
                 super.setMarshallerProperties(maProperties);
             }
         }
         else {
             super.marshal(obj, cls, genericType, enc, os, mt, ms);
         }
     }
 
     protected void validateObjectIfNeeded(Marshaller marshaller, Object obj)
         throws JAXBException {
         if (super.getSchema() == null) {
             super.setSchema(getSchema());
         }
         super.validateObjectIfNeeded(marshaller, obj);
     }
 
     /**
      * eSciDoc maintains a configurable property escidoc-core.xslt.std that holds the name of an xml-header stylesheet.
      * This name is set as marshallProperty "com.sun.xml.bind.xmlHeaders" for the cxf JAXBElementProvider in
      * applicationContext-cxf.xml. But property has to get surrounded with <?xml-stylesheet type=... or set to empty if
      * escidoc-core.xslt.std is not defined.
      *
      * @param marshallProperties marshallProperties
      */
     private void overrideXmlHeadersProperty(Map<String, Object> marshallProperties) {
         String xmlHeaders = (String) marshallProperties.get(XML_HEADERS_PATH);
         if (xmlHeaders != null && !xmlHeaders.isEmpty()) {
             if (!(xmlHeaders.startsWith("http://") || xmlHeaders.startsWith("https://"))) {
                 String baseurl = EscidocConfiguration.getInstance().get(EscidocConfiguration.ESCIDOC_CORE_BASEURL);
                 if (!baseurl.endsWith("/")) {
                     baseurl += "/";
                 }
 
                 if (xmlHeaders.startsWith("./")) {
                     xmlHeaders = baseurl + xmlHeaders.substring(2, xmlHeaders.length());
                 } else if (xmlHeaders.startsWith("/")) {
                     xmlHeaders = baseurl + xmlHeaders.substring(1, xmlHeaders.length());
                 }
             }
             marshallProperties
                 .put(XML_HEADERS_PATH, "<?xml-stylesheet type=\"text/xsl\" href=\"" + xmlHeaders + "\"?>\n");
         }
     }
 }
