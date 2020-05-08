 package de.hszg.atocc.kfgedit.transform.internal;
 
 import de.hszg.atocc.core.util.SerializationException;
 import de.hszg.atocc.core.util.XmlUtilsException;
 import de.hszg.atocc.core.util.test.AbstractTestHelper;
 
 import javax.xml.transform.TransformerException;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.w3c.dom.Document;
 
 public final class GrammarToGraphTests extends AbstractTestHelper {
 
 //    @Before
 //    public void setUp() {
 //        startServer();
 //        attachService("/kfgedit/transform/graph", GrammarToGraph.class);
 //    }
 //
 //    @After
 //    public void tearDown() throws Exception {
 //        stopServer();
 //    }
 
     @Test
     public void test1() throws XmlUtilsException, SerializationException, TransformerException {
         final Document grammarDocument = getXmlService().documentFromFile("example_8_14.xml");
 
        final Document graphDocument = getXmlService().documentFromFile("example_8_14_graph.xml");
 
         final Document resultDocument = getWebUtilService().post(
                 getServiceUrl("kfgedit/transform/graph"), grammarDocument);
 
         Assert.assertEquals(getXmlService().xmlToString(graphDocument), getXmlService()
                 .xmlToString(resultDocument));
     }
 
 }
