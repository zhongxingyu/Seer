 package org.nuxeo.adullact.test;
 
import static junit.framework.Assert.assertNotNull;

 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.nuxeo.ecm.core.storage.sql.DatabaseHelper;
 import org.nuxeo.ecm.directory.Directory;
 import org.nuxeo.ecm.directory.DirectoryException;
 import org.nuxeo.ecm.directory.api.DirectoryService;
 import org.nuxeo.runtime.api.Framework;
 import org.nuxeo.runtime.test.NXRuntimeTestCase;
 
 public class AdullactDirectoryTestCase extends NXRuntimeTestCase {
 
     DirectoryService dirService;
 
     @Before
     public void setUp() throws Exception {
         super.setUp();
         DatabaseHelper.DATABASE.setUp();
 
         deployBundle("org.nuxeo.ecm.core.schema");
         deployBundle("org.nuxeo.ecm.core");
 
         // deploy directory service + sql factory
         deployBundle("org.nuxeo.ecm.directory");
         deployBundle("org.nuxeo.ecm.directory.sql");
         deployBundle("org.nuxeo.ecm.directory.types.contrib");
 
 //        deployContrib("nuxeo-adullact-iparapheur-integration",
 //                "/OSGI-INF/adullact-directory-factory-contrib.xml");
         deployContrib("nuxeo-adullact-iparapheur-integration",
                 "/OSGI-INF/adullact-vocabularies-contrib.xml");
 
         dirService = Framework.getService(DirectoryService.class);
         assertNotNull(dirService);
     }
 
     @After
     public void tearDown() throws Exception {
         DatabaseHelper.DATABASE.tearDown();
         super.tearDown();
     }
 
     @Test
    public void testshouldVocabularyCreated() throws DirectoryException {
         Directory directory = dirService.getDirectory("adu_visibilite");
         assertNotNull(directory);
         directory = dirService.getDirectory("adu_type_technique");
         assertNotNull(directory);
         directory = dirService.getDirectory("adu_sous_type_technique");
         assertNotNull(directory);
     }
 
 }
