 package com.test.hibernate.xmlService;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.StringReader;
 
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 @Service
 public class TransformationService {
 
 
 	@Autowired private Transformer transformer;
 	
 	public void doTransform(String xmlSourceString) throws IOException, TransformerException{
 		
 
 		StreamSource xmlSource=new StreamSource(new StringReader(xmlSourceString));
		transformer.transform(xmlSource, new StreamResult(new FileWriter("/Workspace/testSpringHibernate/GeneratedXML.xml")));
 		
 				}
 }
