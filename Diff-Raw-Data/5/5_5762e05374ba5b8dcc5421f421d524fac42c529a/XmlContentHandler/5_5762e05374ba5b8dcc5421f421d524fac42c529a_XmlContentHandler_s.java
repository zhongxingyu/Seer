 package org.mule.galaxy;
 
 import java.io.IOException;
import java.util.Set;
 
 import javax.xml.namespace.QName;
 import javax.xml.transform.Source;
 
 import org.w3c.dom.Document;
 
 public interface XmlContentHandler extends ContentHandler {
 
     Object read(Source source, Workspace workspace) throws Exception;
 
     Document getDocument(Object o) throws IOException;
     
     /**
      * Get the name of the root XML element
      * @param o
      * @return
      */
     QName getDocumentType(Object o);
     
    Set<QName> getSupportedDocumentTypes();
     
 }
