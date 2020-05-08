 /**
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.tools.recess;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.maven.plugin.testing.AbstractMojoTestCase;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 import org.mockito.MockitoAnnotations;
 import org.mule.tools.rhinodo.impl.SystemOutConsole;
 import org.mule.tools.rhinodo.impl.WrappingConsoleFactory;
 import org.sonatype.plexus.build.incremental.BuildContext;
 
 import java.io.File;
 
 @RunWith(JUnit4.class)
 public class RecessMojoTest extends AbstractMojoTestCase {
 
     private RecessMojo mojo;
     private File helloLessFile;
     private File helloCssFile;
     private File outputFile;
     private boolean stripColors;
     private boolean noIDs;
     private boolean noJSPrefix;
     private boolean noOverqualifying;
     private boolean noUnderscores;
     private boolean noUniversalSelectors;
     private boolean prefixWhitespace;
     private boolean strictPropertyOrder;
     private boolean zeroUnits;
 
     @MockitoAnnotations.Mock
     private BuildContext buildContext;
 
     @Before
     public void setUp() throws Exception {
 
         MockitoAnnotations.initMocks(this);
 
         helloLessFile = File.createTempFile("hello", ".less").getAbsoluteFile();
         helloCssFile = File.createTempFile("hello", ".css").getAbsoluteFile();
 
         mojo = new RecessMojo();
 
         FileUtils.writeStringToFile(helloLessFile, ".my-class {" +
                 "  color:red;" +
                 "}");
 
         assertTrue(helloLessFile.exists());
 
         setVariableValueToObject(mojo, "buildContext", buildContext);
         setVariableValueToObject(mojo, "sourceDirectory", helloLessFile.getParentFile());
         setVariableValueToObject(mojo, "outputFile", null);
         setVariableValueToObject(mojo, "stripColors", stripColors);
         setVariableValueToObject(mojo, "noIDs", noIDs);
         setVariableValueToObject(mojo, "noJSPrefix", noJSPrefix);
         setVariableValueToObject(mojo, "noOverqualifying", noOverqualifying);
         setVariableValueToObject(mojo, "noUnderscores", noUnderscores);
         setVariableValueToObject(mojo, "noUniversalSelectors", noUniversalSelectors);
         setVariableValueToObject(mojo, "prefixWhitespace", prefixWhitespace);
         setVariableValueToObject(mojo, "strictPropertyOrder", strictPropertyOrder);
         setVariableValueToObject(mojo, "zeroUnits", zeroUnits);
     }
 
     @Test
     public void testExecution() throws Exception {
        new Recess(new WrappingConsoleFactory(new SystemOutConsole()), new Object[] {helloLessFile.getAbsoluteFile().toString()}, mojo.getRecessConfig(), helloCssFile.getAbsolutePath(), false, "target/rhinodo", null).run();
     }
 
 }
