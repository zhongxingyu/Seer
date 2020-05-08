 package com.craigmile.ali.jirahelper.Demos;
 
 // http://blog.msbbc.co.uk/2007/06/simple-saxon-java-example.html
 
 // NOTE: I Broke this when I started using InputStreams...
 
 import java.io.InputStream;
 
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.URIResolver;
//import javax.xml.transform.URIResolver;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
import uk.co.bbc.fmt.scotland.build.MyBBCResolver;
 
 public class SaxonDemo0 {
 
 	private static void handleException(Exception ex) {
 		System.out.println("EXCEPTION: " + ex);
 		ex.printStackTrace();
 	}
 
 	private static void myTransformer (String sourceID, String xslID)
 	throws TransformerException, TransformerConfigurationException {
 
 		//URIResolver uriResolver =  new MyBBCResolver();
 		
 		// Create a transform factory instance.
 		TransformerFactory tfactory = TransformerFactory.newInstance();
 		//tfactory.setURIResolver(uriResolver);
 
 		// Create a transformer for the stylesheet.
 
 		//Transformer transformer = tfactory.newTransformer(new StreamSource(File(xslID)));
 		
 		
 		InputStream xslInputStream = SaxonDemo0.class.getResourceAsStream("resources/" + xslID);
 		//System.err.println(xslInputStream.available);
 		Transformer transformer = tfactory.newTransformer(new StreamSource(xslInputStream));
 
 		//URL xslUrl = SaxonDemo0.class.getResource("resources/" + xslID);
 		//Transformer transformer = tfactory.newTransformer(new StreamSource(new File(xslUrl)));
 
 
 		// Transform the source XML to System.out.
 		InputStream sourceInputStream = SaxonDemo0.class.getResourceAsStream("resources/" + sourceID);
 		//		transformer.transform(new StreamSource(new File(sourceID)),
 		//				new StreamResult(System.out));
 		transformer.transform(new StreamSource(sourceInputStream),
 				new StreamResult(System.out));
 	}
 
 	public static void main(String[] args) {
 		/*
 		if (args.length != 3)
 		{
 			System.err.println("Syntax: x ca-cert.jks yourcert.p12 yourpassword");
 			System.exit(1);
 		}
 		;
 		String cacerts = args[0];
 		String mycert = args[1];
 		String passwd = args[2];
 		 */
 
 		// set the TransformFactory to use the Saxon TransformerFactoryImpl method
 		System.setProperty("javax.xml.transform.TransformerFactory",
 		"net.sf.saxon.TransformerFactoryImpl");
 
 		String foo_xml = "data/persons.xml"; //input xml
 		String foo_xsl = "data/persons.xsl"; //input xsl
 
 		try {
 			myTransformer(foo_xml, foo_xsl);
 		} catch (Exception e) {
 			handleException(e);
 
 		}
 	}
 
 
 }
 
