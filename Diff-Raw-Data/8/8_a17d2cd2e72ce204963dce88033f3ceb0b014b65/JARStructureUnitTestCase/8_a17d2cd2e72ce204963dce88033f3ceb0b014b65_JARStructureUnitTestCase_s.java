 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
 package org.jboss.test.deployers.structure.jar.test;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 import org.jboss.deployers.plugins.structure.vfs.jar.JARStructure;
 import org.jboss.deployers.spi.structure.DeploymentContext;
 import org.jboss.deployers.spi.structure.vfs.StructureDeployer;
 import org.jboss.test.deployers.BaseDeployersTest;
 
 /**
  * JARStructureUnitTestCase.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision: 1.1 $
  */
 public class JARStructureUnitTestCase extends BaseDeployersTest
 {
    /** The jar structure deployer */
    private static final JARStructure structure = new JARStructure();
    
    public static Test suite()
    {
       return new TestSuite(JARStructureUnitTestCase.class);
    }
    
    public JARStructureUnitTestCase(String name)
    {
       super(name);
    }
 
    @Override
    protected void setUp() throws Exception
    {
       super.setUp();
       enableTrace("org.jboss.deployers");
    }
 
    @Override
    protected StructureDeployer getStrucutureDeployer()
    {
       return structure;
    }
    
    protected DeploymentContext getValidContext(String root, String path) throws Exception
    {
       DeploymentContext context = createDeploymentContext(root, path);
       assertTrue("Structure should be valid: " + context.getName(), determineStructure(context));
       return context;
    }
 
    protected DeploymentContext assertValidContext(String root, String path) throws Exception
    {
       return getValidContext(root, path);
    }
    
    protected DeploymentContext assertNotValidContext(String root, String path, boolean other) throws Exception
    {
       DeploymentContext context = createDeploymentContext(root, path);
       assertFalse("Structure should not be valid: " + context.getName(), determineStructure(context));
       assertEmpty(context.getChildren());
       return context;
    }
    
    public void testSimple() throws Exception
    {
       DeploymentContext context = getValidContext("/structure/", "jar/simple");
       
       // Test it got all the candidates
       Map<String, Boolean> expected = new HashMap<String, Boolean>();
       expected.put(getURL("/structure/jar/simple/simple1.txt"), false);
       expected.put(getURL("/structure/jar/simple/simple2.txt"), false);
       assertContexts(expected, context.getChildren());
 
       assertCandidatesNotValid(context);
    }
    
    public void testRootNotAnArchive() throws Exception
    {
       assertNotValidContext("/structure/", "jar/notanarchive/NotAnArchive.jar", true);
       assertNotValidContext("/structure/", "jar/notanarchive/NotAnArchive.zip", true);
    }
    
    public void testSubdeploymentNotAnArchive() throws Exception
    {
       DeploymentContext context = getValidContext("/structure/", "jar/notanarchive");
       
       // Test it got all the candidates
       Map<String, Boolean> expected = new HashMap<String, Boolean>();
       expected.put(getURL("/structure/jar/notanarchive/NotAnArchive.jar"), false);
       expected.put(getURL("/structure/jar/notanarchive/NotAnArchive.zip"), false);
       assertContexts(expected, context.getChildren());
 
       assertCandidatesNotValid(context);
    }
    
    public void testJarAsRoot() throws Exception
    {
       assertValidContext("/structure/", "jar/indirectory/archive.jar");
       assertValidContext("/structure/", "jar/indirectory/archive.zip");
    }
    
    public void testJarInDirectory() throws Exception
    {
       DeploymentContext context = getValidContext("/structure/", "jar/indirectory");
       
       // Test it got all the candidates
       Map<String, Boolean> expected = new HashMap<String, Boolean>();
       expected.put(getJarURL("/structure/jar/indirectory/archive.jar"), true);
       expected.put(getJarURL("/structure/jar/indirectory/archive.zip"), true);
       assertContexts(expected, context.getChildren());
       
       assertCandidatesValid(context);
    }
    
    public void testSubdirectoryNotAJar() throws Exception
    {
       DeploymentContext context = getValidContext("/structure/", "jar/subdirnotajar");
       
       // Test it got all the candidates
       Map<String, Boolean> expected = new HashMap<String, Boolean>();
      expected.put(getURL("/structure/jar/subdirnotajar/sub"), false);
       assertContexts(expected, context.getChildren());
       
       assertCandidatesNotValid(context);
    }
    
    public void testSubdirectoryIsAJar() throws Exception
    {
       DeploymentContext context = getValidContext("/structure/", "jar/subdirisajar");
       
       // Test it got all the candidates
       Map<String, Boolean> expected = new HashMap<String, Boolean>();
      expected.put(getURL("/structure/jar/subdirisajar/sub.jar"), true);
       assertContexts(expected, context.getChildren());
       
       assertCandidatesValid(context);
    }
    
    public void testdirectoryHasMetaInf() throws Exception
    {
       assertValidContext("/structure/", "jar/subdirhasmetainf/sub");
    }
    
    public void testSubdirectoryHasMetaInf() throws Exception
    {
       DeploymentContext context = getValidContext("/structure/", "jar/subdirhasmetainf");
       
       // Test it got all the candidates
       Map<String, Boolean> expected = new HashMap<String, Boolean>();
      expected.put(getURL("/structure/jar/subdirhasmetainf/sub"), true);
       assertContexts(expected, context.getChildren());
       
       assertCandidatesValid(context);
    }
    
    public void testSubdeploymentIsKnownFile() throws Exception
    {
       DeploymentContext context = getValidContext("/structure/file", "simple");
       
       // Test it got all the candidates
       Map<String, Boolean> expected = new HashMap<String, Boolean>();
       expected.put(getURL("/structure/file/simple/simple-service.xml"), true);
       assertContexts(expected, context.getChildren());
 
       assertCandidatesNotValid(context);
    }
    
    public void testSubdeploymentIsUnknownFile() throws Exception
    {
       DeploymentContext context = getValidContext("/structure/file", "notknown");
       
       // Test it got all the candidates
       Map<String, Boolean> expected = new HashMap<String, Boolean>();
       expected.put(getURL("/structure/file/notknown/test-unknown.xml"), false);
       expected.put(getURL("/structure/file/notknown/unknown.xml"), false);
       assertContexts(expected, context.getChildren());
 
       assertCandidatesNotValid(context);
    }
    
    protected void assertContexts(Map<String, Boolean> expected, Set<DeploymentContext> actual) throws Exception
    {
       assertCandidateContexts(expected, actual);
    }
 }
