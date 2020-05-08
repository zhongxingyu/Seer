 package uk.ac.ebi.arrayexpress.api.validation;
 
 import static org.junit.Assert.*;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.StringReader;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.XMLConstants;
 import javax.xml.transform.Source;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 import javax.xml.validation.Validator;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathExpression;
 
 import org.junit.Test;
 import org.xml.sax.SAXException;
 import org.xmldb.api.DatabaseManager;
 import org.xmldb.api.base.Collection;
 import org.xmldb.api.base.Database;
 import org.xmldb.api.base.ResourceIterator;
 import org.xmldb.api.base.ResourceSet;
 import org.xmldb.api.modules.XPathQueryService;
 
 
 public class XsdValidationSchemaTest {
 
 	
 
 	//@Test
	public void validateSampleSearchInsideGroupSchema() throws Exception {
 
 		URL schemaFile = new URL(
				"http://ruis-imac.windows.ebi.ac.uk:8080/biosamples/assets/xsd/ResultQuerySampleSchema.xsd");
 		Source xmlFile = new StreamSource(
 				"http://ruis-imac.windows.ebi.ac.uk:8080/biosamples/xml/groupsamples/SAMEG23004/query=cancer");
 		SchemaFactory schemaFactory = SchemaFactory
 				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
 		Schema schema = schemaFactory.newSchema(schemaFile);
 		Validator validator = schema.newValidator();
 		try {
 			validator.validate(xmlFile);
 			System.out.println(xmlFile.getSystemId() + " is valid");
 			assertTrue(true);
 		} catch (SAXException e) {
 			assertFalse(true);
 			System.out.println(xmlFile.getSystemId() + " is NOT valid");
 			System.out.println("Reason: " + e.getLocalizedMessage());
 		}
 
 	}
 
 	//@Test
 	public void validateGroupSearchSchema() throws Exception {
 		URL schemaFile = new URL(
				"http://ruis-imac.windows.ebi.ac.uk:8080/biosamples/assets/xsd/ResultQuerySampleGroupSchema.xsd");
 		Source xmlFile = new StreamSource(
 				"http://ruis-imac.windows.ebi.ac.uk:8080/biosamples/xml/group/query=cancer");
 		SchemaFactory schemaFactory = SchemaFactory
 				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
 		Schema schema = schemaFactory.newSchema(schemaFile);
 		Validator validator = schema.newValidator();
 		try {
 			validator.validate(xmlFile);
 			System.out.println(xmlFile.getSystemId() + " is valid");
 			assertTrue(true);
 		} catch (SAXException e) {
 			assertFalse(true);
 			System.out.println(xmlFile.getSystemId() + " is NOT valid");
 			System.out.println("Reason: " + e.getLocalizedMessage());
 		}
 
 	}
 
	
	
	//@Test
	public void validateSampleSearchSchema() throws Exception {
		URL schemaFile = new URL(
				"http://ruis-imac.windows.ebi.ac.uk:8080/biosamples/assets/xsd/ResultQuerySampleSchema.xsd");
		Source xmlFile = new StreamSource(
				"http://ruis-imac.windows.ebi.ac.uk:8080/biosamples/xml/sample/query=cancer");
		SchemaFactory schemaFactory = SchemaFactory
				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = schemaFactory.newSchema(schemaFile);
		Validator validator = schema.newValidator();
		try {
			validator.validate(xmlFile);
			System.out.println(xmlFile.getSystemId() + " is valid");
			assertTrue(true);
		} catch (SAXException e) {
			assertFalse(true);
			System.out.println(xmlFile.getSystemId() + " is NOT valid");
			System.out.println("Reason: " + e.getLocalizedMessage());
		}

	}

	
 	public void validateGroupSchema(String group) throws Exception {
 		URL schemaFile = new URL(
 				"http://ruis-imac.windows.ebi.ac.uk:8080/biosamples/assets/xsd/BioSDSchema.xsd");
 		Source xmlFile = new StreamSource(
 				"http://ruis-imac.windows.ebi.ac.uk:8080/biosamples/xml/group/"
 						+ group);
 		SchemaFactory schemaFactory = SchemaFactory
 				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
 		Schema schema = schemaFactory.newSchema(schemaFile);
 		Validator validator = schema.newValidator();
 		try {
 			validator.validate(xmlFile);
 			System.out.println(xmlFile.getSystemId() + " is valid");
 			assertTrue(true);
 		} catch (SAXException e) {
 			e.printStackTrace();
 			assertFalse(true);
 			System.out.println(xmlFile.getSystemId() + " is NOT valid");
 			System.out.println("Reason: " + e.getLocalizedMessage());
 		}
 
 	}
 
 	//@Test
 	public void validateGroupSchema() throws Exception {
 		validateGroupSchema("SAMEG11413");
 	}
 
 	
 
 	public void validateSampleSchema(String sample) throws Exception {
 		URL schemaFile = new URL(
 				"http://ruis-imac.windows.ebi.ac.uk:8080/biosamples/assets/xsd/BioSDSchema.xsd");
 		Source xmlFile = new StreamSource(
 				"http://ruis-imac.windows.ebi.ac.uk:8080/biosamples/xml/sample/"+sample);
 		SchemaFactory schemaFactory = SchemaFactory
 				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
 		Schema schema = schemaFactory.newSchema(schemaFile);
 		Validator validator = schema.newValidator();
 		try {
 			validator.validate(xmlFile);
 			System.out.println(xmlFile.getSystemId() + " is valid");
 			assertTrue(true);
 		} catch (SAXException e) {
 			e.printStackTrace();
 			assertFalse(true);
 			System.out.println(xmlFile.getSystemId() + " is NOT valid");
 			System.out.println("Reason: " + e.getLocalizedMessage());
 		}
 
 	}
 	
 	
 	//@Test
 	public void validateSampleSchema() throws Exception {
 		validateSampleSchema("SAMEA1523957");
 	}
 	
 	
 	
 	
 	//@Test
 	public void validateAllGroupsSampleSchema() throws Exception {
 		Class<?> c = null;
 		Database db;
 		Collection coll = null;
 		String base="xmldb:basex";
 		String dbHost = "localhost";
 		String dbPort = "1984";
 		String dbName = "biosamplesAEGroup";
 		String documentPath = "//SampleGroup";
 		String connectionString = base + "://" + dbHost
 				+ ":" + dbPort + "/" + dbName;
 		long numberResults = 0;
 		c = Class.forName("org.basex.api.xmldb.BXDatabase");
 		
 		db = (Database) c.newInstance();
 		DatabaseManager.registerDatabase(db);
 
 		coll = DatabaseManager.getCollection(connectionString);
 		XPathQueryService service = (XPathQueryService) coll.getService(
 				"XPathQueryService", "1.0");
 		ResourceSet set = service.query("count(" + documentPath + ")");
 		if (set.getIterator().hasMoreResources()) {
 			numberResults = Integer.parseInt((String) set.getIterator()
 					.nextResource().getContent());
 		}
 		System.out.println("Number of results->" + numberResults);
 		long pageSizeDefault = 50000;
 		// the samplegroup cannot be big otherwise I will obtain a memory
 		// error ... but the sample must b at least one million because the
 		// paging queries are really slow - we need to balance it
 		// (for samples 1million, for samplegroup 50000)
 		if (numberResults > 1000000) {
 			pageSizeDefault = 1000000;
 		}
 
 		long pageNumber = 1;
 		while ((pageNumber * pageSizeDefault) <= (numberResults
 				+ pageSizeDefault - 1)) {
 			long pageInit = (pageNumber - 1) * pageSizeDefault + 1;
 			long pageSize = (pageNumber * pageSizeDefault < numberResults) ? pageSizeDefault
 					: (numberResults - pageInit + 1);
 			service = (XPathQueryService) coll.getService("XPathQueryService",
 					"1.0");
 
 			// xquery paging using subsequence function
 			long time = System.nanoTime();
 
 			// /set =
 			// service.query("for $x in(/Biosamples/SampleGroup/Sample/@id) return string($x)");
 			set = service.query("for $x in(subsequence(" + documentPath
 					+ "/@id," + pageInit + "," + pageSize
 					+ ")) return string($x)");
 			// logger.debug("Number of results of page->" + set.getSize());
 
 			ResourceIterator iter = set.getIterator();
 			XPath xp2;
 			XPathExpression xpe2;
 			List documentNodes;
 			StringReader reader;
 			// cache of distinct attributes fora each sample group
 			int count = 0;
 			while (iter.hasMoreResources()) {
 				count++;
 				String idSample = (String) iter.nextResource().getContent();
 				System.out.println("its beeing processed the number ->" + count*pageNumber
 						+ "->" + idSample);
 				validateGroupSchema(idSample);
 
 			}
 			pageNumber++;
 		}
 		if (coll != null) {
 			try {
 				coll.close();
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		set = null;
 		db = null;
 	}
 	
 	
 	//@Test
 	public void validateAllSamplesSchema() throws Exception {
 		Class<?> c = null;
 		Database db;
 		Collection coll = null;
 		String base="xmldb:basex";
 		String dbHost = "localhost";
 		String dbPort = "1984";
 		String dbName = "biosamplesAEGroup";
 		String documentPath = "//Sample";
 		String connectionString = base + "://" + dbHost
 				+ ":" + dbPort + "/" + dbName;
 		long numberResults = 0;
 		c = Class.forName("org.basex.api.xmldb.BXDatabase");
 		
 		db = (Database) c.newInstance();
 		DatabaseManager.registerDatabase(db);
 
 		coll = DatabaseManager.getCollection(connectionString);
 		XPathQueryService service = (XPathQueryService) coll.getService(
 				"XPathQueryService", "1.0");
 		ResourceSet set = service.query("count(" + documentPath + ")");
 		if (set.getIterator().hasMoreResources()) {
 			numberResults = Integer.parseInt((String) set.getIterator()
 					.nextResource().getContent());
 		}
 		System.out.println("Number of results->" + numberResults);
 		long pageSizeDefault = 50000;
 		// the samplegroup cannot be big otherwise I will obtain a memory
 		// error ... but the sample must b at least one million because the
 		// paging queries are really slow - we need to balance it
 		// (for samples 1million, for samplegroup 50000)
 
 		
 
 		if (numberResults > 100000) {
 			pageSizeDefault = 1000000;
 		}
 
 		long pageNumber = 1;
 
 		
 		while ((pageNumber * pageSizeDefault) <= (numberResults
 				+ pageSizeDefault - 1)) {
 			long pageInit = (pageNumber - 1) * pageSizeDefault + 1;
 			long pageSize = (pageNumber * pageSizeDefault < numberResults) ? pageSizeDefault
 					: (numberResults - pageInit + 1);
 			service = (XPathQueryService) coll.getService("XPathQueryService",
 					"1.0");
 
 			// xquery paging using subsequence function
 			long time = System.nanoTime();
 
 			// /set =
 			// service.query("for $x in(/Biosamples/SampleGroup/Sample/@id) return string($x)");
 			set = service.query("for $x in(subsequence(" + documentPath
 					+ "/@id," + pageInit + "," + pageSize
 					+ ")) return string($x)");
 			// logger.debug("Number of results of page->" + set.getSize());
 
 			ResourceIterator iter = set.getIterator();
 			XPath xp2;
 			XPathExpression xpe2;
 			List documentNodes;
 			StringReader reader;
 			// cache of distinct attributes fora each sample group
 			int count = 0;
 			while (iter.hasMoreResources()) {
 				count++;
 				String idSample = (String) iter.nextResource().getContent();
 				System.out.println("its beeing processed the number ->" + count*pageNumber
 						+ "->" + idSample);
 				validateSampleSchema(idSample);
 
 			}
 			
 			pageNumber++;
 		}
 		if (coll != null) {
 			try {
 				// coll.close();
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		set = null;
 		db = null;
 	}
 
 	
 
 	
 	
 }
