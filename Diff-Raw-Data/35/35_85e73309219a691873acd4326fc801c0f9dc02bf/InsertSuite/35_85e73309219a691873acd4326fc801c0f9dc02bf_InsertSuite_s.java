 /* Copyright (c) 2001 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root 
  * application directory.
  */
 package org.vfny.geoserver.requests;
 
 import java.io.*;
 import java.util.*;
 import java.util.logging.Logger;
 import java.util.logging.Level;
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.LinearRing;
 import com.vividsolutions.jts.geom.Polygon;
 import com.vividsolutions.jts.geom.PrecisionModel;
 import org.geotools.resources.Geotools;
 import org.geotools.feature.FeatureType;
 import org.geotools.feature.Feature;
 import org.geotools.feature.FeatureFactory;
import org.geotools.feature.FlatFeatureFactory;
import org.geotools.feature.FeatureTypeFlat;
import org.geotools.feature.AttributeTypeDefault;
 import org.geotools.feature.AttributeType;
 import org.geotools.feature.SchemaException;
import org.geotools.feature.IllegalFeatureException;
 import org.vfny.geoserver.config.TypeRepository;
 import org.vfny.geoserver.config.TypeInfo;
 import org.vfny.geoserver.config.ConfigInfo;
 import org.vfny.geoserver.responses.WfsTransactionException;
 
 /**
  * Tests the Update request handling.
  *
  * @version $VERSION$
   * @author Chris Holmes, TOPP
  */
 public class InsertSuite extends TestCase {
 
     /** Class logger */
     private static final Logger LOGGER =  
         Logger.getLogger("org.vfny.geoserver.requests");
 
     /** Unit test data directory */
     private static final String CONFIG_DIR = 
         System.getProperty("user.dir") + "/misc/documents/configuration.xml";
 
     /** Unit test data directory */
     private static final String TYPE_DIR = 
         System.getProperty("user.dir") + "/misc/testData/featureTypes";
 
 
 
     private ConfigInfo config;
     private TypeRepository repo;
 
     /** Unit test data directory */
     private static final String DATA_DIRECTORY = 
         System.getProperty("user.dir") + "/misc/unit/requests";
 
     private FeatureType schema;
 
     private Feature testFeature;
 
     private FeatureFactory featureFactory;
 
    /* Initializes the logger. */
    static {
        Geotools.init("Log4JFormatter", Level.FINEST);
    }
    
     /** Constructor with super. */
     public InsertSuite (String testName) { super(testName); }
 
 
         /** Handles test set up details. */
     public void setUp() {
 	config = ConfigInfo.getInstance(CONFIG_DIR);
         config.setTypeDir(TYPE_DIR);
         repo = TypeRepository.getInstance();
 	
 
 	AttributeType[] atts = { 
	    new AttributeTypeDefault("fid", Integer.class),
	    new AttributeTypeDefault("geom", Polygon.class),
	    new AttributeTypeDefault("name", String.class)};
 	try {
	schema = (new FeatureTypeFlat(atts)).setTypeName("rail");
 	} catch (SchemaException e) {
 	    LOGGER.finer("problem with creating schema");
 	}
 
	featureFactory = new FlatFeatureFactory(schema);

 
 	Coordinate[] points = { new Coordinate(15, 15),
 				new Coordinate(15, 25),
 				new Coordinate(25, 25),
 				new Coordinate(25, 15),
 				 new Coordinate(15, 15) };
 	PrecisionModel precModel = new PrecisionModel();
 	int srid = 2035;
 	LinearRing shell = new LinearRing(points, precModel, srid);
 	Polygon the_geom = new Polygon(shell, precModel, srid);
 
 
 	Integer featureId = new Integer(44);
 	String name = "insert polygon";
 	Object[] attributes = { featureId, the_geom, name };
 	try{
 	    
	testFeature = featureFactory.create(attributes, 
 						String.valueOf(featureId));
	} catch (IllegalFeatureException ife) {
 	    LOGGER.warning("problem in setup " + ife);
 	}
 
     }
 
 
     /************************ ************************************************
      * STATIC METHODS FOR TEST RUNNING                                       *
      *************************************************************************/
     /**
      * Handles actual XML test running details.
      * @param baseRequest Base request, for comparison.
      * @param fileName File name to parse.
      * @param match Whether or not base request and parse request should match.
      * @throws Exception If there is any problem running the test.
      */
     private static boolean runXmlTest(TransactionRequest baseRequest,
 				       String fileName, 
                                        boolean match)
 	throws Exception {
 	// Read the file and parse it
 	File inputFile = new File(DATA_DIRECTORY + "/" + fileName + ".xml");
 	Reader inputStream = new FileReader(inputFile);
         TransactionRequest request = XmlRequestReader.
             readTransaction(new BufferedReader(inputStream));
 	LOGGER.info("base request: " + baseRequest);
 	LOGGER.info("read request: " + request);
 	LOGGER.info("XML " + fileName +" test passed: " +  
 		    baseRequest.toString().equals(request.toString()));
 	
 	//no implement insert request equals method, since there is no
 	//geotools feature and featureType equals methods.7
         if(match) { 
 	    //return baseRequest.equals(request);
 	    return baseRequest.toString().equals(request.toString());
 	} else { 
 	    return !baseRequest.equals(request);
         } 
 	}
 
 
     /* These tests need a geom_test feature type with an info.xml and
      * an info.xml added to rail to properly work. */
    public void testXml1() throws Exception { 
             // make base comparison objects        
        InsertRequest insert = new InsertRequest();
        
        insert.setHandle("insert 1");
        insert.addFeature(testFeature);
        TransactionRequest baseRequest = new TransactionRequest();
        baseRequest.addSubRequest(insert);
        baseRequest.setHandle("my insert");
         // run test       
        assertTrue(runXmlTest( baseRequest, "insert1", true));
     }
 
     
     public void testXml2() throws Exception { 
         // make base comparison objects        
        InsertRequest insert = new InsertRequest();
 	insert.setHandle("insert 2");
 	insert.addFeature(testFeature);
        TransactionRequest baseRequest = new TransactionRequest();
        baseRequest.addSubRequest(insert);
        
 	Coordinate[] points = { new Coordinate(5, 5),
 				new Coordinate(5, 15),
 				new Coordinate(15, 15),
 				new Coordinate(15, 5),
 				 new Coordinate(5, 5) };
 	PrecisionModel precModel = new PrecisionModel();
 	int srid = 2035;
 	LinearRing shell = new LinearRing(points, precModel, srid);
 	Polygon the_geom = new Polygon(shell, precModel, srid);
 
 
 	Integer featureId = new Integer(23);
 	String name = "polygon2";
 	Object[] attributes = { featureId, the_geom, name };
 	//try{
 	    
 	Feature feature2 = featureFactory.create(attributes, 
 						String.valueOf(featureId));
 
 	insert.addFeature(feature2);
        	baseRequest.setHandle("my second insert");
         // run test       
         assertTrue(runXmlTest( baseRequest, "insert2", true));
     }
 
     /*    
     public void testDiffFeatures() throws Exception{
 	TransactionRequest baseRequest = new TransactionRequest();
 	try {
 	    runXmlTest(baseRequest, "insert3", true);
 	} catch (WfsTransactionException e) {
 	    LOGGER.fine("caught exception: " + e.getMessage());
 	    assertTrue(e.getMessage().equals("Problem adding features: features"
 					   + " do not match- added typeName: "
 					   + "rail, set typeName: geom_test"));
 	}
     }
 
     */
 }
