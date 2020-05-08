 package com.lancktele.rest.utils;
 
 import org.codehaus.jettison.json.JSONObject;
 import org.codehaus.jettison.mapped.Configuration;
 import org.codehaus.jettison.mapped.MappedNamespaceConvention;
 import org.codehaus.jettison.mapped.MappedXMLStreamReader;
 import org.springframework.stereotype.Service;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.stream.XMLStreamReader;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Created by IntelliJ IDEA.
  * User: azee
  */
 
 @Service
 public class Parser {
 
     public <T>T unmarshal(String response, String rootName, Class<T> objectClass) throws Exception{
         response = "{\"" + rootName + "\": " + response + "}";
         JAXBContext jc = JAXBContext.newInstance(objectClass);
         JSONObject obj = new JSONObject(response);
         Configuration config = new Configuration();
         Map<String, String> xmlToJsonNamespaces = new HashMap<String,String>(1);
        xmlToJsonNamespaces.put("urn:rest.lancktele.com", "");
         config.setXmlToJsonNamespaces(xmlToJsonNamespaces);
         MappedNamespaceConvention con = new MappedNamespaceConvention(config);
         XMLStreamReader xmlStreamReader = new MappedXMLStreamReader(obj, con);
         Unmarshaller unmarshaller = jc.createUnmarshaller();
         return (T)unmarshaller.unmarshal(xmlStreamReader);
     }
 }
