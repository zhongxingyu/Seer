 package edu.northwestern.bioinformatics.studycalendar.restlets;
 import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Source;
 import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createSource;
 import edu.northwestern.bioinformatics.studycalendar.restlets.representations.SourceListJsonRepresentation;
 
 import java.util.List;
 import java.util.Arrays;
 
 import static org.easymock.EasyMock.expect;
 import org.restlet.data.Status;
 import org.restlet.data.MediaType;
 
 /**
  * @author Jalpa Patel
  */
public class SourcesResourceTest  extends ResourceTestCase<SourcesResource> {
     private SourceDao sourceDao;
 
     @Override
     public void setUp() throws Exception {
         super.setUp();
         sourceDao = registerDaoMockFor(SourceDao.class);
     }
 
     @Override
    protected SourcesResource createResource() {
         SourcesResource resource = new SourcesResource();
         resource.setSourceDao(sourceDao);
         resource.setXmlSerializer(xmlSerializer);
         return resource;
     }
 
     public void testGetAllowed() throws Exception {
         assertAllowedMethods("GET");
     }
 
 
     public void testGetXmlForAllSources() throws Exception {
         Source source = createSource("TestSource");
         List<Source> sources = Arrays.asList(source);
         expect(sourceDao.getAll()).andReturn(sources);
         expect(xmlSerializer.createDocumentString(sources)).andReturn(MOCK_XML);
 
         doGet();
         assertResponseStatus(Status.SUCCESS_OK);
         assertResponseIsCreatedXml();
     }
 
     public void testGetJsonForAllSources() throws Exception {
         Source source = createSource("TestSource");
         List<Source> sources = Arrays.asList(source);
         expect(sourceDao.getAll()).andReturn(sources);
         setAcceptedMediaTypes(MediaType.APPLICATION_JSON);
 
         doGet();
         assertResponseStatus(Status.SUCCESS_OK);
         assertTrue("Response entity is wrong type", response.getEntity() instanceof SourceListJsonRepresentation);
         assertSame("Response entity is for wrong sources", sources,
             ((SourceListJsonRepresentation) response.getEntity()).getSources());
     }
 }
 
