 /**
  * 
  */
 package com.github.podd.api.file.test;
 
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Set;
 import java.util.UUID;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.openrdf.model.URI;
 import org.openrdf.model.impl.ValueFactoryImpl;
 import org.openrdf.repository.Repository;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.sail.SailRepository;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.sail.memory.MemoryStore;
 
 import com.github.podd.api.file.FileReference;
 import com.github.podd.api.file.FileReferenceManager;
 import com.github.podd.api.file.FileReferenceProcessorFactoryRegistry;
 import com.github.podd.api.test.TestConstants;
 
 /**
  * Abstract test class for FileReferenceManager.
  * 
  * @author kutila
  */
 public abstract class AbstractFileReferenceManagerTest
 {
     
     protected FileReferenceManager testFileReferenceManager;
     
     private FileReferenceProcessorFactoryRegistry testRegistry;
     
     private Repository testRepository;
     
     protected RepositoryConnection testRepositoryConnection;
     
     /**
      * @return A new FileReferenceManager instance for use by this test
      */
     public abstract FileReferenceManager getNewFileReferenceManager();
     
     /**
      * @return A new FileReferenceProcessorFactory Registry for use by this test
      */
     public abstract FileReferenceProcessorFactoryRegistry getNewPoddFileReferenceProcessorFactoryRegistry();
     
     @Before
     public void setUp() throws Exception
     {
         this.testRegistry = this.getNewPoddFileReferenceProcessorFactoryRegistry();
         Assert.assertNotNull("Null implementation of test Registry", this.testRegistry);
         
         this.testFileReferenceManager = this.getNewFileReferenceManager();
         Assert.assertNotNull("Null implementation of test FileReferenceManager", this.testFileReferenceManager);
         
         this.testRepository = new SailRepository(new MemoryStore());
         this.testRepository.initialize();
         
         this.testRepositoryConnection = this.testRepository.getConnection();
         this.testRepositoryConnection.begin(); // Transaction per each test
         
         this.testFileReferenceManager.setProcessorFactoryRegistry(this.testRegistry);
     }
     
     @After
     public void tearDown() throws Exception
     {
         this.testRegistry.clear();
         this.testRepositoryConnection.rollback();
         this.testRepositoryConnection.close();
         this.testRepository.shutDown();
         
         this.testFileReferenceManager = null;
     }
     
     @Test
     public void testExtractFileReferencesFromEmptyRepository() throws Exception
     {
         final URI randomContext =
                 ValueFactoryImpl.getInstance().createURI("urn:random:" + UUID.randomUUID().toString());
         
         final Set<FileReference> extractedFileReferences =
                 this.testFileReferenceManager.extractFileReferences(this.testRepositoryConnection, randomContext);
         Assert.assertTrue("Should not have found any file references", extractedFileReferences.isEmpty());
     }
     
     @Test
     public void testExtractFileReferencesFromRepositoryWithoutFileReferences() throws Exception
     {
         final URI randomContext =
                 ValueFactoryImpl.getInstance().createURI("urn:random:" + UUID.randomUUID().toString());
         final InputStream resourceStream = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
         this.testRepositoryConnection.add(resourceStream, "", RDFFormat.TURTLE, randomContext);
         
         final Set<FileReference> extractedFileReferences =
                 this.testFileReferenceManager.extractFileReferences(this.testRepositoryConnection, randomContext);
         Assert.assertTrue("Should not have found any file references", extractedFileReferences.isEmpty());
     }
     
     @Test
     public void testExtractFileReferencesFromRepositoryWith1FileRef() throws Exception
     {
         final URI randomContext =
                 ValueFactoryImpl.getInstance().createURI("urn:random:" + UUID.randomUUID().toString());
         final InputStream resourceStream =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_OBJECT);
         this.testRepositoryConnection.add(resourceStream, "", RDFFormat.RDFXML, randomContext);
         
         final Set<FileReference> extractedFileReferences =
                 this.testFileReferenceManager.extractFileReferences(this.testRepositoryConnection, randomContext);
         
         // verify:
         Assert.assertFalse("Could not find file references", extractedFileReferences.isEmpty());
         Assert.assertEquals("Not the expected number of file references", 1, extractedFileReferences.size());
         final FileReference fileReference = extractedFileReferences.iterator().next();
         Assert.assertNull("Artifact ID should be NULL", fileReference.getArtifactID());
        Assert.assertNull("Parent IRI should be NULL", fileReference.getParentIri());
         Assert.assertEquals("Not the expected IRI", "urn:temp:uuid:object-rice-scan-34343-a", fileReference
                 .getObjectIri().toString());
         Assert.assertEquals("Not the expected label", "Rice tree scan 003454-98", fileReference.getLabel());
         Assert.assertEquals("Not the expected alias", "csiro_dap", fileReference.getRepositoryAlias());
     }
     
     @Test
     public void testExtractFileReferencesFromRepositoryWith2FileRefs() throws Exception
     {
         // populate Repository with a test artifact
         final URI randomContext =
                 ValueFactoryImpl.getInstance().createURI("urn:random:" + UUID.randomUUID().toString());
         final InputStream resourceStream =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_PURLS_2_FILE_REFS);
         this.testRepositoryConnection.add(resourceStream, "", RDFFormat.RDFXML, randomContext);
         
         final Set<FileReference> extractedFileReferences =
                 this.testFileReferenceManager.extractFileReferences(this.testRepositoryConnection, randomContext);
         
         // verify:
         Assert.assertNotNull("NULL extracted references", extractedFileReferences);
         Assert.assertEquals("Not the expected number of file references", 2, extractedFileReferences.size());
         
         final List<String> expectedObjectIris =
                 Arrays.asList("http://purl.org/podd-test/130326f/object-rice-scan-34343-a",
                         "http://purl.org/podd-test/130326f/object-rice-scan-34343-b");
         final List<String> expectedLabels = Arrays.asList("Rice tree scan 003454-98", "Rice tree scan 003454-99");
         final List<String> expectedAliases = Arrays.asList("csiro_dap");
         
         for(final FileReference fileReference : extractedFileReferences)
         {
             Assert.assertNull("Artifact ID should be NULL", fileReference.getArtifactID());
            Assert.assertNull("Parent IRI should be NULL", fileReference.getParentIri());
             Assert.assertTrue("File Reference URI is not an expected one",
                     expectedObjectIris.contains(fileReference.getObjectIri().toString()));
             Assert.assertTrue("Label is not an expected one", expectedLabels.contains(fileReference.getLabel()));
             Assert.assertTrue("Alias is not an expected one",
                     expectedAliases.contains(fileReference.getRepositoryAlias()));
         }
         
     }
     
     // TODO add more tests
 }
