 /*
  * @(#)PlainArchiveConverterTest.java     23 Jul 2011
  *
  * Copyright © 2010 Andrew Phillips.
  *
  * ====================================================================
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
  * implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * ====================================================================
  */
 package com.xebialabs.deployit.server.api.importer.ear;
 
 import static java.lang.String.format;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import org.junit.BeforeClass;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TemporaryFolder;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.io.Files;
 import com.xebialabs.deployit.plugin.api.boot.PluginBooter;
 import com.xebialabs.deployit.plugin.api.udm.Deployable;
 import com.xebialabs.deployit.plugin.jee.artifact.Ear;
 import com.xebialabs.deployit.server.api.importer.ImportingContext;
 import com.xebialabs.deployit.server.api.importer.PackageInfo;
 import com.xebialabs.deployit.server.api.importer.ear.scanner.FileSource;
 import com.xebialabs.deployit.server.api.importer.ear.scanner.PackageInfoScanner;
 import com.xebialabs.overthere.local.LocalFile;
 
 
 /**
  * Unit tests for the {@link EarImporter}
  */
 public class EarImporterTest {
     public static final String EAR_WITH_MANIFEST = "src/test/resources/ear-with-manifest.ear";
     public static final String EAR_WITHOUT_ATTRIBUTES = "src/test/resources/ear-without-manifest-attributes.ear";
     
     private static final ImportingContext DUMMY_IMPORT_CTX = new DummyImportingContext();
     
     @Rule
     public TemporaryFolder tempFolder = new TemporaryFolder();
   
     @BeforeClass
     public static void boot() {
         PluginBooter.bootWithoutGlobalContext();
     }
     
     @Test
     public void listsEars() {
         assertEquals(ImmutableList.of("ear-with-manifest.ear", "ear-without-manifest-attributes.ear"), 
                 new EarImporter(ImmutableList.<PackageInfoScanner>of())
                 .list(new File("src/test/resources")));
     }
     
     @Test
     public void handlesEars() {
        assertTrue(new EarImporter(ImmutableList.<PackageInfoScanner>of())
                    .canHandle(new FileSource(EAR_WITH_MANIFEST)));
     }
     
     @Test
     public void ignoresDars() throws IOException {
        assertFalse(new EarImporter(ImmutableList.<PackageInfoScanner>of())
                     .canHandle(new FileSource(tempFolder.newFile("myApp.dar"))));
     }
     
     @Test
     public void triesScannersInOrder() {
         // manifest scanner comes first
         PackageInfo result = new EarImporter().preparePackage(
                 new FileSource(EAR_WITH_MANIFEST), DUMMY_IMPORT_CTX);
         assertEquals("myApp", result.getApplicationName());
         assertEquals("3", result.getApplicationVersion());
     }
     
     @Test(expected = IllegalArgumentException.class)
     public void failsIfNoScannerSucceeds() {
         new EarImporter(ImmutableList.<PackageInfoScanner>of())
         .preparePackage(new FileSource(EAR_WITH_MANIFEST), DUMMY_IMPORT_CTX);
     }
     
     @Test
     public void addsEarToPackage() throws IOException {
         File earFile = new File(EAR_WITH_MANIFEST);
         PackageInfo packageInfo = new PackageInfo(new FileSource(earFile));
         packageInfo.setApplicationName("myApp");
         packageInfo.setApplicationVersion("2.0");
         List<Deployable> deployables = 
             new EarImporter(ImmutableList.<PackageInfoScanner>of())
             .importEntities(packageInfo, DUMMY_IMPORT_CTX).getDeployables();
         assertEquals(1, deployables.size());
         assertTrue(format("Expected instance of %s", Ear.class),
                 deployables.get(0) instanceof Ear);
         Ear ear = (Ear) deployables.get(0);
         assertTrue("Expected the files to contain the same bytes",
                 Files.equal(earFile, ((LocalFile) ear.getFile()).getFile()));
         assertEquals("Applications/myApp/2.0/myApp", ear.getId());
         assertEquals("myApp", ear.getName());
     }
     
     private static class DummyImportingContext implements ImportingContext {
         @Override
         public <T> T getAttribute(String name) {
             throw new UnsupportedOperationException("TODO Auto-generated method stub");
         }
 
         @Override
         public <T> void setAttribute(String name, T value) {
             throw new UnsupportedOperationException("TODO Auto-generated method stub");
         }
     }
 }
