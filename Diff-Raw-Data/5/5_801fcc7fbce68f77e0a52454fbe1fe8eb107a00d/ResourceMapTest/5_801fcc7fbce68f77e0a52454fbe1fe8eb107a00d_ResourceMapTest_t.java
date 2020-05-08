 package pl.psnc.dl.wf4ever.model.ORE;
 
 import java.net.URI;
 
 import junit.framework.Assert;
 
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.openrdf.rio.RDFFormat;
 
 import pl.psnc.dl.wf4ever.model.BaseTest;
 import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
 import pl.psnc.dl.wf4ever.vocabulary.ORE;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.vocabulary.DCTerms;
 import com.hp.hpl.jena.vocabulary.RDF;
 
 public class ResourceMapTest extends BaseTest {
 
     private URI resourceMapUri;
     private ResourceMap resourceMap;
     private String resourceMapName = "resource-map";
 
 
     @Override
     @Before
     public void setUp()
             throws Exception {
         super.setUp();
         resourceMapUri = researchObject.getUri().resolve(resourceMapName);
        resourceMap = new ResourceMap(userProfile, dataset, false, researchObject, resourceMapUri) {
 
             @Override
             public ResearchObject getResearchObject() {
                 //mock :) 
                 return researchObject;
             }
         };
        resourceMap.setBuilder(builder);
     }
 
 
     @Test
     public void testConstructor() {
         ResourceMap createdResourceMap = new ResourceMap(userProfile, dataset, false, researchObject, resourceMapUri) {
 
             @Override
             public ResearchObject getResearchObject() {
                 return null;
             }
         };
         Assert.assertNotNull(createdResourceMap);
     }
 
 
     @Test
     public void testSave() {
         resourceMap.save();
         Model model = ModelFactory.createDefaultModel();
         model.read(resourceMap.getGraphAsInputStream(RDFFormat.RDFXML), null);
 
         Resource r = model.getResource(researchObject.getUri().toString());
         Assert.assertNotNull(r);
         Assert.assertNotNull(r.getProperty(DCTerms.creator));
         Assert.assertNotNull(r.getProperty(DCTerms.created));
         Assert.assertEquals(resourceMapUri.toString(), r.getPropertyResourceValue(ORE.isDescribedBy).getURI()
                 .toString());
 
         r = model.getResource(resourceMapUri.toString());
         Assert.assertNotNull(r);
         Assert.assertEquals(researchObject.getUri().toString(), r.getPropertyResourceValue(ORE.describes).getURI()
                 .toString());
         Assert.assertEquals(ORE.ResourceMap.getURI(), r.getPropertyResourceValue(RDF.type).getURI().toString());
     }
 
 
     @Test
     public void testDelete() {
         resourceMap.save();
         resourceMap.serialize();
         resourceMap.delete();
     }
 
 
     @Test
     public void saveAggregatedResourceTest() {
         URI aggregatedResourceUri = researchObject.getUri().resolve("aggregated-resource");
         AggregatedResource aggregatedResource = builder.buildAggregatedResource(aggregatedResourceUri, researchObject,
             userProfile, DateTime.now());
         resourceMap.save();
         resourceMap.saveAggregatedResource(aggregatedResource);
         Model model = ModelFactory.createDefaultModel();
         model.read(resourceMap.getGraphAsInputStream(RDFFormat.RDFXML), null);
         Resource aggregated = model.getResource(aggregatedResource.getUri().toString());
         Assert.assertEquals(ORE.AggregatedResource.getURI(), aggregated.getPropertyResourceValue(RDF.type).getURI()
                 .toString());
         Resource r = model.getResource(researchObject.getUri().toString());
         Assert.assertTrue(r.hasProperty(ORE.aggregates, aggregated));
     }
 
 
     @Test
     public void saveProxyTest() {
         URI aggregatedResourceUri = researchObject.getUri().resolve("aggregated-resource");
         AggregatedResource aggregatedResource = builder.buildAggregatedResource(aggregatedResourceUri, researchObject,
             userProfile, DateTime.now());
         aggregatedResource.save();
         URI proxyUri = researchObject.getUri().resolve("proxy");
         Proxy proxy = builder.buildProxy(proxyUri, aggregatedResource, researchObject);
         resourceMap.saveProxy(proxy);
         Model model = ModelFactory.createDefaultModel();
         model.read(resourceMap.getGraphAsInputStream(RDFFormat.RDFXML), null);
         Resource r = model.getResource(proxy.getUri().toString());
         Assert.assertNotNull(r);
         Assert.assertNotNull(r.getProperty(ORE.proxyFor));
         Assert.assertNotNull(r.getProperty(ORE.proxyIn));
 
     }
 
 
     @Test
     public void testGetPath() {
         Assert.assertEquals(resourceMapName, resourceMap.getPath().toString());
     }
 }
