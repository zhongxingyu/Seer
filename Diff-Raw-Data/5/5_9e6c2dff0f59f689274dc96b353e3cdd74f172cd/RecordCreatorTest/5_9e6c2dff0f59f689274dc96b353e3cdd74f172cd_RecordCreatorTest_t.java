 package dk.statsbiblioteket.doms.ingesters.radiotv;
 
 import dk.statsbiblioteket.doms.client.DomsWSClientImpl;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.w3c.dom.Document;
 
 import dk.statsbiblioteket.doms.central.InvalidCredentialsException;
 import dk.statsbiblioteket.doms.central.InvalidResourceException;
 import dk.statsbiblioteket.doms.central.MethodFailedException;
 import dk.statsbiblioteket.doms.central.RecordDescription;
 import dk.statsbiblioteket.doms.central.SearchResult;
 import dk.statsbiblioteket.doms.client.DomsWSClient;
 import dk.statsbiblioteket.doms.client.exceptions.NoObjectFound;
 import dk.statsbiblioteket.doms.client.exceptions.ServerOperationFailed;
 import dk.statsbiblioteket.doms.client.exceptions.XMLParseException;
 import dk.statsbiblioteket.doms.client.objects.DigitalObjectFactory;
 import dk.statsbiblioteket.doms.client.relations.LiteralRelation;
 import dk.statsbiblioteket.doms.client.relations.Relation;
 import dk.statsbiblioteket.doms.client.utils.Constants;
 import dk.statsbiblioteket.doms.client.utils.FileInfo;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Trivial test of ingester
  */
 public class RecordCreatorTest {
     @Before
     public void setUp() throws Exception {
 
     }
 
     @After
     public void tearDown() throws Exception {
 
     }
 
     @Test
     public void testIngestProgram() throws Exception {
         DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
         documentBuilderFactory.setNamespaceAware(true);
         Document metadataDocument = documentBuilderFactory.newDocumentBuilder().parse(getClass().getResource("/2012-11-14_23-20-00_dr1.xml").getFile());
         DomsWSClient testDomsClient = new TestDomsWSClient();
        new RecordCreator(testDomsClient,true).ingestProgram(metadataDocument);
     }
 
     @Ignore
     @Test
     public void testIngestProgramRealDOMS() throws Exception {
         DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
         documentBuilderFactory.setNamespaceAware(true);
         Document metadataDocument = documentBuilderFactory.newDocumentBuilder().parse(getClass().getResource("/2012-11-15_09-40-00_dr1.xml").getFile());
         DomsWSClient testDomsClient = new DomsWSClientImpl();
         testDomsClient.login(new URL("http://alhena:7880/centralWebservice-service/central/?wsdl"), "fedoraAdmin", "fedoraAdminPass");
        new RecordCreator(testDomsClient,true).ingestProgram(metadataDocument);
     }
 
 
     private static class TestDomsWSClient implements DomsWSClient {
 
         @Override
         public void login(URL domsWSAPIEndpoint, String userName, String password) {
 
     }
 
         @Override
         public List<String> getLabel(List<String> uuids) {
             return null;
         }
             
         @Override
         public DigitalObjectFactory getDigitalObjectFactory() {
             return null;
         }
 
         @Override
         public String getLabel(String uuid) {
             return null;
         }
             
         @Override
         public List<SearchResult> search(String query, int offset, int pageLength) throws ServerOperationFailed {
             return null;
         }
 
         @Override
         public void setCredentials(URL domsWSAPIEndpoint, String userName, String password) {
 
         }
 
         @Override
         public String createObjectFromTemplate(String templatePID, String comment) throws ServerOperationFailed {
             return null;  
         }
 
         @Override
         public String createObjectFromTemplate(String templatePID, List<String> oldIdentifiers, String comment)
                 throws ServerOperationFailed {
             return null;  
         }
 
         @Override
         public String createFileObject(String templatePID, FileInfo fileInfo, String comment)
                 throws ServerOperationFailed {
             return null;  
         }
 
         @Override
         public void addFileToFileObject(String fileObjectPID, FileInfo fileInfo, String comment)
                 throws ServerOperationFailed {
             
         }
 
         @Override
         public String getFileObjectPID(URL fileURL) throws NoObjectFound, ServerOperationFailed {
             return null;  
         }
 
         @Override
         public List<String> getPidFromOldIdentifier(String oldIdentifier)
                 throws NoObjectFound, ServerOperationFailed {
             throw new NoObjectFound();
         }
 
         @Override
         public Document getDataStream(String objectPID, String datastreamID) throws ServerOperationFailed {
             return null;  
         }
 
         @Override
         public void updateDataStream(String objectPID, String dataStreamID, Document newDataStreamContents,
                                      String comment) throws ServerOperationFailed {
             
         }
 
         @Override
         public void addObjectRelation(String pid, String predicate, String objectPid, String comment)
                 throws ServerOperationFailed, XMLParseException {
             
         }
 
         @Override
         public void removeObjectRelation(LiteralRelation relation, String comment) throws ServerOperationFailed {
             
         }
 
         @Override
         public List<Relation> listObjectRelations(String objectPID, String relationType)
                 throws ServerOperationFailed {
             return Collections.emptyList();
         }
 
         @Override
         public void publishObjects(String comment, String... pidsToPublish) throws ServerOperationFailed {
             
         }
 
         @Override
         public void unpublishObjects(String comment, String... pidsToUnpublish) throws ServerOperationFailed {
             
         }
 
         @Override
         public void deleteObjects(String comment, String... pidsToDelete) throws ServerOperationFailed {
             
         }
 
         @Override
         public long getModificationTime(String collectionPID, String viewID, String state)
                 throws ServerOperationFailed {
             return 0;  
         }
 
         @Override
         public List<RecordDescription> getModifiedEntryObjects(String collectionPID, String viewID, long timeStamp,
                                                                String objectState, long offsetIndex,
                                                                long maxRecordCount) throws ServerOperationFailed {
             return null;  
         }
 
         @Override
         public String getViewBundle(String entryObjectPID, String viewID) throws ServerOperationFailed {
             return null;  
         }
 
         @Override
         public void setObjectLabel(String objectPID, String objectLabel, String comment)
                 throws ServerOperationFailed {
             
         }
 
         @Override
         public Constants.FedoraState getState(String pid) throws ServerOperationFailed {
             return null;
         }
 
         @Override
         public InputStream getDatastreamContent(String pid, String ds)
                 throws ServerOperationFailed, InvalidCredentialsException, MethodFailedException,
                 InvalidResourceException {
             return null;
         }
     }
 }
