 /* Copyright (c) 2013, Dźmitry Laŭčuk
    All rights reserved.
 
    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met: 
 
    1. Redistributions of source code must retain the above copyright notice, this
       list of conditions and the following disclaimer.
    2. Redistributions in binary form must reproduce the above copyright notice,
       this list of conditions and the following disclaimer in the documentation
       and/or other materials provided with the distribution.
 
    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
    ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
    DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
    ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */
 package afc.ant.modular;
 
 import java.io.File;
 import java.util.Collections;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.types.Path;
 
 import junit.framework.TestCase;
 
 public class ManifestModuleLoaderTest extends TestCase
 {
     private Project project;
     private File baseDir;
     private ManifestModuleLoader loader;
     
     @Override
     protected void setUp()
     {
         baseDir = new File("test/data/ManifestModuleLoader");
         project = new Project();
         project.setBaseDir(baseDir);
         loader = new ManifestModuleLoader();
         loader.setProject(project);
     }
     
     @Override
     protected void tearDown()
     {
         loader = null;
         project = null;
         baseDir = null;
     }
     
     public void testNormalisePath_NullPath()
     {
         try {
             loader.normalisePath(null);
             fail();
         }
         catch (NullPointerException ex) {
             // expected
         }
     }
     
     public void testNormalisePath_EmptyPath()
     {
         assertEquals(".", loader.normalisePath(""));
     }
     
     public void testNormalisePath_PathPointsToBaseDir()
     {
         assertEquals(".", loader.normalisePath("foo/.."));
     }
     
     public void testNormalisePath_SomeModulePath()
     {
         assertEquals("foo/bar", loader.normalisePath("foo/bar/"));
     }
     
     public void testLoadModule_CustomEntry_NoDependencies_NoAttributes() throws Exception
     {
         loader.setManifestEntry("Build");
         
         final ModuleInfo moduleInfo = loader.loadModule("NoDeps_NoAttributes");
         
         assertNotNull(moduleInfo);
         assertEquals("NoDeps_NoAttributes", moduleInfo.getPath());
         assertEquals(Collections.emptySet(), moduleInfo.getDependencies());
         assertEquals(Collections.emptyMap(), moduleInfo.getAttributes());
     }
     
     public void testLoadModule_CustomEntry_EmptyDependencies_NoAttributes() throws Exception
     {
         loader.setManifestEntry("Build");
         
         final ModuleInfo moduleInfo = loader.loadModule("EmptyDeps_NoAttributes/");
         
         assertNotNull(moduleInfo);
         assertEquals("EmptyDeps_NoAttributes", moduleInfo.getPath());
         assertEquals(Collections.emptySet(), moduleInfo.getDependencies());
         assertEquals(Collections.emptyMap(), moduleInfo.getAttributes());
     }
     
     public void testLoadModule_CustomEntry_WithDependencies_NoAttributes() throws Exception
     {
         loader.setManifestEntry("Build");
         
         final ModuleInfo moduleInfo = loader.loadModule("WithDeps_NoAttributes");
         
         assertNotNull(moduleInfo);
         assertEquals("WithDeps_NoAttributes", moduleInfo.getPath());
         assertEquals(TestUtil.set("foo", "\u0142aska", "baz/quux"), moduleInfo.getDependencies());
         assertEquals(Collections.emptyMap(), moduleInfo.getAttributes());
     }
     
     public void testLoadModule_CustomEntry_WithDependencies_WithAttributes_NoClasspathAttributes() throws Exception
     {
         loader.setManifestEntry("Build");
         
         final ModuleInfo moduleInfo = loader.loadModule("WithDeps_WithAttributes");
         
         assertNotNull(moduleInfo);
         assertEquals("WithDeps_WithAttributes", moduleInfo.getPath());
         assertEquals(TestUtil.set("foo", "bar/baz"), moduleInfo.getDependencies());
         assertEquals(TestUtil.map("Attrib1", "", "Attrib2", "a b  c+c/%C5%81/e",
                 "Attrib3", "hello, world!", "aTTRIB4", "12345"), moduleInfo.getAttributes());
     }
     
     public void testLoadModule_CustomEntry_WithDependencies_WithAttributes_SingleClasspathAttribute() throws Exception
     {
         final File moduleDir = new File(baseDir, "WithDeps_WithAttributes");
         
         loader.setManifestEntry("Build");
         loader.createClasspathAttribute().setName("ATTRIB2"); // manifest attribute names are case-insensitive
         
         final ModuleInfo moduleInfo = loader.loadModule("WithDeps_WithAttributes");
         
         assertNotNull(moduleInfo);
         assertEquals("WithDeps_WithAttributes", moduleInfo.getPath());
         assertEquals(TestUtil.set("foo", "bar/baz"), moduleInfo.getDependencies());
         assertEquals(TestUtil.set("Attrib1", "ATTRIB2", "Attrib3", "aTTRIB4"), moduleInfo.getAttributes().keySet());
         assertEquals("", moduleInfo.getAttributes().get("Attrib1"));
         assertEquals("hello, world!", moduleInfo.getAttributes().get("Attrib3"));
         assertEquals("12345", moduleInfo.getAttributes().get("aTTRIB4"));
         
         assertPath(moduleInfo.getAttributes().get("ATTRIB2"),
                 new File(moduleDir, "a"), new File(moduleDir, "b"), new File(moduleDir, "c c/\u0141/e"));
     }
     
     public void testLoadModule_CustomEntry_WithDependencies_WithAttributes_MultipleClasspathAttribute() throws Exception
     {
         final File moduleDir = new File(baseDir, "WithDeps_WithAttributes");
         
         loader.setManifestEntry("Build");
         loader.createClasspathAttribute().setName("Attrib2");
         loader.createClasspathAttribute().setName("Attrib4"); // manifest attribute names are case-insensitive
         
         final ModuleInfo moduleInfo = loader.loadModule("WithDeps_WithAttributes");
         
         assertNotNull(moduleInfo);
         assertEquals("WithDeps_WithAttributes", moduleInfo.getPath());
         assertEquals(TestUtil.set("foo", "bar/baz"), moduleInfo.getDependencies());
         assertEquals(TestUtil.set("Attrib1", "Attrib2", "Attrib3", "Attrib4"), moduleInfo.getAttributes().keySet());
         assertEquals("", moduleInfo.getAttributes().get("Attrib1"));
         assertEquals("hello, world!", moduleInfo.getAttributes().get("Attrib3"));
         
         assertPath(moduleInfo.getAttributes().get("Attrib2"),
                 new File(moduleDir, "a"), new File(moduleDir, "b"), new File(moduleDir, "c c/\u0141/e"));
         
         assertPath(moduleInfo.getAttributes().get("Attrib4"), new File(moduleDir, "12345"));
     }
     
     public void testLoadModule_CustomEntry_ClasspathAttributeNameIsTheNameOfTheDependenciesAttribute() throws Exception
     {
         final File moduleDir = new File(baseDir, "WithDeps_WithAttributes");
         
         loader.setManifestEntry("Build");
         loader.createClasspathAttribute().setName("Attrib2");
         // This attribute is used to read dependencies from and must be ignored.
         loader.createClasspathAttribute().setName("Depends");
         
         final ModuleInfo moduleInfo = loader.loadModule("WithDeps_WithAttributes");
         
         assertNotNull(moduleInfo);
         assertEquals("WithDeps_WithAttributes", moduleInfo.getPath());
         assertEquals(TestUtil.set("foo", "bar/baz"), moduleInfo.getDependencies());
         assertEquals(TestUtil.set("Attrib1", "Attrib2", "Attrib3", "aTTRIB4"), moduleInfo.getAttributes().keySet());
         assertEquals("", moduleInfo.getAttributes().get("Attrib1"));
         assertEquals("hello, world!", moduleInfo.getAttributes().get("Attrib3"));
         assertEquals("12345", moduleInfo.getAttributes().get("aTTRIB4"));
         
         assertPath(moduleInfo.getAttributes().get("Attrib2"),
                 new File(moduleDir, "a"), new File(moduleDir, "b"), new File(moduleDir, "c c/\u0141/e"));
     }
     
     public void testLoadModule_CustomEntry_WithDependencies_WithAttributes_ClasspathAttributeNotFound() throws Exception
     {
         final File moduleDir = new File(baseDir, "WithDeps_WithAttributes");
         
         loader.setManifestEntry("Build");
         loader.createClasspathAttribute().setName("Attrib2");
         loader.createClasspathAttribute().setName("NoSuchAttribute");
         
         final ModuleInfo moduleInfo = loader.loadModule("WithDeps_WithAttributes");
         
         assertNotNull(moduleInfo);
         assertEquals("WithDeps_WithAttributes", moduleInfo.getPath());
         assertEquals(TestUtil.set("foo", "bar/baz"), moduleInfo.getDependencies());
         assertEquals(TestUtil.set("Attrib1", "Attrib2", "Attrib3", "aTTRIB4"), moduleInfo.getAttributes().keySet());
         assertEquals("", moduleInfo.getAttributes().get("Attrib1"));
         assertEquals("hello, world!", moduleInfo.getAttributes().get("Attrib3"));
         assertEquals("12345", moduleInfo.getAttributes().get("aTTRIB4"));
         
         assertPath(moduleInfo.getAttributes().get("Attrib2"),
                 new File(moduleDir, "a"), new File(moduleDir, "b"), new File(moduleDir, "c c/\u0141/e"));
     }
     
     public void testLoadModule_CustomEntry_ClasspathAttributeHasNoName() throws Exception
     {
         loader.setManifestEntry("Build");
         loader.createClasspathAttribute().setName("Attrib2");
         
         try {
             loader.createClasspathAttribute(); // no name is set
             loader.loadModule("WithDeps_NoAttributes");
             
             fail();
         }
         catch (BuildException ex) {
             assertEquals("A 'classpathAttribute' element with undefined name is encountered.", ex.getMessage());
         }
     }
     
     public void testCannotLoadModule_ModuleDoesNotExist() throws Exception
     {
         loader.setManifestEntry("Build");
         
         try {
             loader.loadModule("NoSuchModule");
             
             fail();
         }
         catch (ModuleNotLoadedException ex) {
             assertEquals("The module 'NoSuchModule' ('" +
                     new File(baseDir, "NoSuchModule").getAbsolutePath() + "') does not exist.", ex.getMessage());
         }
     }
     
     public void testCannotLoadModule_ModuleDoesNotHaveManifest() throws Exception
     {
         loader.setManifestEntry("Build");
         
         try {
             loader.loadModule("ModuleWithNoManifest");
             
             fail();
         }
         catch (ModuleNotLoadedException ex) {
             assertEquals("The module 'ModuleWithNoManifest' does not have the manifest ('" +
                     new File(baseDir, "ModuleWithNoManifest/META-INF/MANIFEST.MF").getAbsolutePath() + "').",
                     ex.getMessage());
         }
     }
     
     public void testCannotLoadModule_ModuleDoesNotHaveMETAINFDirectory() throws Exception
     {
         loader.setManifestEntry("Build");
         
         try {
             loader.loadModule("ModuleWithNoMetainf");
             
             fail();
         }
         catch (ModuleNotLoadedException ex) {
             assertEquals("The module 'ModuleWithNoMetainf' does not have the manifest ('" +
                     new File(baseDir, "ModuleWithNoMetainf/META-INF/MANIFEST.MF").getAbsolutePath() + "').",
                     ex.getMessage());
         }
     }
     
     public void testCannotLoadModule_ModuleManifestIsNotAFile() throws Exception
     {
         loader.setManifestEntry("Build");
         
         try {
             loader.loadModule("ModuleWithManifestAsDir");
             
             fail();
         }
         catch (ModuleNotLoadedException ex) {
             assertEquals("The module 'ModuleWithManifestAsDir' has the manifest that is not a file ('" +
                     new File(baseDir, "ModuleWithManifestAsDir/META-INF/MANIFEST.MF").getAbsolutePath() + "').",
                     ex.getMessage());
         }
     }
     
     public void testCannotLoadModule_ModuleManifestIsMalformed() throws Exception
     {
         loader.setManifestEntry("Build");
         
         try {
             loader.loadModule("ModuleWithMalformedManifest");
             
             fail();
         }
         catch (ModuleNotLoadedException ex) {
             /* The message is not the beast for a malformed manifest. It is caused by the fact that
                java.util.jar.Manifest#read does not distinguish real I/O errors and malformed format errors. */
             assertEquals("An I/O error is encountered while loading the manifest of " +
                     "the module 'ModuleWithMalformedManifest' ('" +
                     new File(baseDir, "ModuleWithMalformedManifest/META-INF/MANIFEST.MF").getAbsolutePath() + "').",
                     ex.getMessage());
         }
     }
     
     public void testCannotLoadModule_ModuleManifestDoesNotHaveBuildSection() throws Exception
     {
         loader.setManifestEntry("Build");
         
         try {
             loader.loadModule("NoBuildSection");
             
             fail();
         }
         catch (ModuleNotLoadedException ex) {
            assertEquals("The module 'NoBuildSection' does not have the entry 'Build' in the manifest ('" +
                     new File(baseDir, "NoBuildSection/META-INF/MANIFEST.MF").getAbsolutePath() + "').",
                     ex.getMessage());
         }
     }
     
     public void testCannotLoadModule_ModulePathIsNotADirectory() throws Exception
     {
         loader.setManifestEntry("Build");
         
         try {
             loader.loadModule("ModuleIsNotADirectory");
             
             fail();
         }
         catch (ModuleNotLoadedException ex) {
             assertEquals("The module path 'ModuleIsNotADirectory' ('" +
                     new File(baseDir, "ModuleIsNotADirectory").getAbsolutePath() + "') is not a directory.",
                     ex.getMessage());
         }
     }
     
     public void testCannotLoadModule_InvalidClasspathAttribute() throws Exception
     {
         loader.setManifestEntry("Build");
         loader.createClasspathAttribute().setName("Attrib1");
         
         try {
             loader.loadModule("WithInvalidClasspathAttribute");
             fail();
         }
         catch (ModuleNotLoadedException ex) {
             assertEquals("Unable to load the module 'WithInvalidClasspathAttribute'. " +
                     "The classpath attribute 'Attrib1' contains an invalid URL element: 'c+c/%'.", ex.getMessage());
         }
     }
     
     public void testCannotLoadModule_InvalidDependeeModulePath() throws Exception
     {
         loader.setManifestEntry("Build");
         loader.createClasspathAttribute().setName("Attrib1");
         
         try {
             loader.loadModule("WithInvalidDependeeModulePath");
             fail();
         }
         catch (ModuleNotLoadedException ex) {
             assertEquals("Unable to load the module 'WithInvalidDependeeModulePath'. " +
                     "This dependee module path is an invalid URL: 'foo%'.", ex.getMessage());
         }
     }
     
     public void testMainEntry_CannotLoadModule_InvalidClasspathAttribute() throws Exception
     {
         loader.createClasspathAttribute().setName("Attrib1");
         
         try {
             loader.loadModule("MainEntry_WithInvalidClasspathAttribute");
             fail();
         }
         catch (ModuleNotLoadedException ex) {
             assertEquals("Unable to load the module 'MainEntry_WithInvalidClasspathAttribute'. " +
                     "The classpath attribute 'Attrib1' contains an invalid URL element: 'c+c/%'.", ex.getMessage());
         }
     }
     
     public void testLoadModule_MainEntry_WithDependencies_WithAttributes_NoClasspathAttributes() throws Exception
     {
         final ModuleInfo moduleInfo = loader.loadModule("MainEntry_WithDeps_WithAttributes");
         
         assertNotNull(moduleInfo);
         assertEquals("MainEntry_WithDeps_WithAttributes", moduleInfo.getPath());
         assertEquals(TestUtil.set("foo", "bar/baz"), moduleInfo.getDependencies());
         assertEquals(TestUtil.map("Attrib1", "", "Attrib2", "a b  c+c/%C5%81/e",
                 "Attrib3", "hello, world!", "aTTRIB4", "12345"), moduleInfo.getAttributes());
     }
     
     public void testLoadModule_MainEntry_WithDependencies_WithAttributes_WithClasspathAttributes() throws Exception
     {
         final File moduleDir = new File(baseDir, "MainEntry_WithDeps_WithAttributes");
         
         loader.createClasspathAttribute().setName("Attrib2");
         loader.createClasspathAttribute().setName("Attrib4"); // manifest attribute names are case-insensitive
         
         final ModuleInfo moduleInfo = loader.loadModule("MainEntry_WithDeps_WithAttributes");
         
         assertNotNull(moduleInfo);
         assertEquals("MainEntry_WithDeps_WithAttributes", moduleInfo.getPath());
         assertEquals(TestUtil.set("foo", "bar/baz"), moduleInfo.getDependencies());
         assertEquals(TestUtil.set("Attrib1", "Attrib2", "Attrib3", "Attrib4"), moduleInfo.getAttributes().keySet());
         assertEquals("", moduleInfo.getAttributes().get("Attrib1"));
         assertEquals("hello, world!", moduleInfo.getAttributes().get("Attrib3"));
         
         assertPath(moduleInfo.getAttributes().get("Attrib2"),
                 new File(moduleDir, "a"), new File(moduleDir, "b"), new File(moduleDir, "c c/\u0141/e"));
         
         assertPath(moduleInfo.getAttributes().get("Attrib4"), new File(moduleDir, "12345"));
     }
     
     public void testLoadModule_MainEntry_TheNameOfClasspathAttributeIsTheNameOfTheDependenciesAttribute_DifferentCase()
             throws Exception
     {
         final File moduleDir = new File(baseDir, "MainEntry_WithDeps_WithAttributes");
         
         loader.createClasspathAttribute().setName("Attrib2");
         loader.createClasspathAttribute().setName("Attrib4"); // manifest attribute names are case-insensitive
         loader.createClasspathAttribute().setName("dePEnds");
         
         final ModuleInfo moduleInfo = loader.loadModule("MainEntry_WithDeps_WithAttributes");
         
         assertNotNull(moduleInfo);
         assertEquals("MainEntry_WithDeps_WithAttributes", moduleInfo.getPath());
         assertEquals(TestUtil.set("foo", "bar/baz"), moduleInfo.getDependencies());
         assertEquals(TestUtil.set("Attrib1", "Attrib2", "Attrib3", "Attrib4"), moduleInfo.getAttributes().keySet());
         assertEquals("", moduleInfo.getAttributes().get("Attrib1"));
         assertEquals("hello, world!", moduleInfo.getAttributes().get("Attrib3"));
         
         assertPath(moduleInfo.getAttributes().get("Attrib2"),
                 new File(moduleDir, "a"), new File(moduleDir, "b"), new File(moduleDir, "c c/\u0141/e"));
         
         assertPath(moduleInfo.getAttributes().get("Attrib4"), new File(moduleDir, "12345"));
     }
     
     private static void assertPath(final Object pathObject, final File... expectedElements)
     {
         assertNotNull(pathObject);
         assertTrue(pathObject instanceof Path);
         final Path path = (Path) pathObject;
         final String[] actualElements = path.list();
         assertEquals(expectedElements.length, actualElements.length);
         for (int i = 0; i < expectedElements.length; ++i) {
             assertEquals(expectedElements[i].getAbsolutePath(), actualElements[i]);
         }
     }
 }
