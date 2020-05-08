 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2008, Red Hat Middleware LLC, and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
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
 package org.jboss.test.classloading.metadata.xml.test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jboss.classloading.spi.metadata.*;
 import org.jboss.classloading.spi.version.Version;
 import org.jboss.classloading.spi.version.VersionRange;
 import org.jboss.javabean.plugins.jaxb.JavaBean20;
 import org.jboss.test.classloading.metadata.xml.AbstractJBossXBTest;
 import org.jboss.test.classloading.metadata.xml.support.TestCapability;
 import org.jboss.test.classloading.metadata.xml.support.TestRequirement;
 
 import junit.framework.Test;
 
 /**
  * ClassLoadingMetaDataXmlUnitTestCase.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @author <a href="ales.justin@jboss.org">Ales Justin</a>
  * @version $Revision: 1.1 $
  */
 public class ClassLoadingMetaDataXmlUnitTestCase extends AbstractJBossXBTest
 {
    public static Test suite()
    {
       return suite(ClassLoadingMetaDataXmlUnitTestCase.class);
    }
 
    public ClassLoadingMetaDataXmlUnitTestCase(String name)
    {
       super(name);
    }
 
    public void testModuleName() throws Exception
    {
       ClassLoadingMetaData result = unmarshal();
       assertEquals("test", result.getName());
       assertEquals(Version.DEFAULT_VERSION, result.getVersion());
       assertNull(result.getDomain());
       assertNull(result.getParentDomain());
       assertFalse(result.isTopLevelClassLoader());
       assertNull(result.getExportAll());
       assertNull(result.getIncludedPackages());
       assertNull(result.getExcludedPackages());
       assertNull(result.getExcludedExportPackages());
       assertFalse(result.isImportAll());
       assertTrue(result.isJ2seClassLoadingCompliance());
       assertTrue(result.isCacheable());
       assertTrue(result.isBlackListable());
       assertNull(result.getCapabilities().getCapabilities());
       assertNull(result.getRequirements().getRequirements());
       assertNull(result.getParentPolicy());
    }
 
    public void testModuleVersion() throws Exception
    {
       ClassLoadingMetaData result = unmarshal();
       assertEquals(Version.parseVersion("1.0.0"), result.getVersion());
    }
 
    public void testModuleDomain() throws Exception
    {
       ClassLoadingMetaData result = unmarshal();
       assertEquals("testDomain", result.getDomain());
    }
 
    public void testModuleParentDomain() throws Exception
    {
       ClassLoadingMetaData result = unmarshal();
       assertEquals("testParentDomain", result.getParentDomain());
    }
 
    public void testModuleTopLevelClassLoader() throws Exception
    {
       ClassLoadingMetaData result = unmarshal();
       assertTrue(result.isTopLevelClassLoader());
    }
 
    public void testModuleExportAll() throws Exception
    {
       ClassLoadingMetaData result = unmarshal();
       assertEquals(ExportAll.ALL, result.getExportAll());
    }
 
    public void testModuleIncluded() throws Exception
    {
       ClassLoadingMetaData result = unmarshal();
       assertEquals("Included", result.getIncludedPackages());
    }
 
    public void testModuleExcluded() throws Exception
    {
       ClassLoadingMetaData result = unmarshal();
       assertEquals("Excluded", result.getExcludedPackages());
    }
 
    public void testModuleExcludedExport() throws Exception
    {
       ClassLoadingMetaData result = unmarshal();
       assertEquals("ExcludedExport", result.getExcludedExportPackages());
    }
 
    public void testModuleImportAll() throws Exception
    {
       ClassLoadingMetaData result = unmarshal();
       assertTrue(result.isImportAll());
    }
 
    public void testModuleJ2seClassLoadingCompliance() throws Exception
    {
       ClassLoadingMetaData result = unmarshal();
       assertFalse(result.isJ2seClassLoadingCompliance());
    }
 
    public void testModuleCache() throws Exception
    {
       ClassLoadingMetaData result = unmarshal();
       assertFalse(result.isCacheable());
    }
 
    public void testModuleBlackList() throws Exception
    {
       ClassLoadingMetaData result = unmarshal();
       assertFalse(result.isBlackListable());
    }
 
    public void testExportOneModuleNoVersion() throws Exception
    {
       ClassLoadingMetaData result = unmarshal();
       ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
       assertCapabilities(result, factory.createModule("export1"));
    }
 
    public void testExportOneModuleVersioned() throws Exception
    {
       ClassLoadingMetaData result = unmarshal();
       ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
       assertCapabilities(result, factory.createModule("export1", "1.0.0"));
    }
 
    public void testExportThreeModules() throws Exception
    {
       ClassLoadingMetaData result = unmarshal();
       ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
       assertCapabilities(result, factory.createModule("export1", "1.0.0"), 
                                  factory.createModule("export2", "2.0.0"), 
                                  factory.createModule("export3", "3.0.0"));
    }
 
    public void testExportOnePackageNoVersion() throws Exception
    {
       ClassLoadingMetaData result = unmarshal();
       ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
       assertCapabilities(result, factory.createPackage("export1"));
    }
 
    public void testExportOnePackageVersioned() throws Exception
    {
       ClassLoadingMetaData result = unmarshal();
       ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
       assertCapabilities(result, factory.createPackage("export1", "1.0.0"));
    }
 
    public void testExportThreePackages() throws Exception
    {
       ClassLoadingMetaData result = unmarshal();
       ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
       assertCapabilities(result, factory.createPackage("export1", "1.0.0"), 
                                  factory.createPackage("export2", "2.0.0"), 
                                  factory.createPackage("export3", "3.0.0"));
    }
 
    public void testImportOneModuleNoVersion() throws Exception
    {
       ClassLoadingMetaData result = unmarshal();
       ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
       assertRequirements(result, factory.createRequireModule("export1"));
    }
 
    public void testImportOneModuleVersioned() throws Exception
    {
       ClassLoadingMetaData result = unmarshal();
       ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
       assertRequirements(result, factory.createRequireModule("export1", new VersionRange("1.0.0", "2.0.0")));
    }
 
    public void testImportThreeModules() throws Exception
    {
       ClassLoadingMetaData result = unmarshal();
       ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
       assertRequirements(result, factory.createRequireModule("export1", new VersionRange("1.0.0", "1.1.0")), 
                                  factory.createRequireModule("export2", new VersionRange("2.0.0", "2.1.0")), 
                                  factory.createRequireModule("export3", new VersionRange("3.0.0", "3.1.0")));
    }
 
    public void testImportOnePackageNoVersion() throws Exception
    {
       ClassLoadingMetaData result = unmarshal();
       ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
       assertRequirements(result, factory.createRequirePackage("export1"));
    }
 
    public void testImportOnePackageVersioned() throws Exception
    {
       ClassLoadingMetaData result = unmarshal();
       ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
       assertRequirements(result, factory.createRequirePackage("export1", new VersionRange("1.0.0", "2.0.0")));
    }
 
    public void testImportThreePackages() throws Exception
    {
       ClassLoadingMetaData result = unmarshal();
       ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
       assertRequirements(result, factory.createRequirePackage("export1", new VersionRange("1.0.0", "1.1.0")), 
                                  factory.createRequirePackage("export2", new VersionRange("2.0.0", "2.1.0")), 
                                  factory.createRequirePackage("export3", new VersionRange("3.0.0", "3.1.0")));
    }
 
    public void testImportVersionRange() throws Exception
    {
       ClassLoadingMetaData result = unmarshal();
       ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
       assertRequirements(result, factory.createRequireModule("export1"), 
                                  factory.createRequireModule("export2", new VersionRange("1.0.0")), 
                                  factory.createRequireModule("export3", new VersionRange("0.0.0", "1.0.0")),
                                  factory.createRequireModule("export4", new VersionRange("1.0.0", "2.0.0")),
                                  factory.createRequireModule("export5", new VersionRange("1.0.0", false, "2.0.0", false)),
                                  factory.createRequireModule("export6", new VersionRange("1.0.0", false, "2.0.0", true)),
                                  factory.createRequireModule("export7", new VersionRange("1.0.0", true, "2.0.0", false)),
                                  factory.createRequireModule("export8", new VersionRange("1.0.0", true, "2.0.0", true)));
    }
 
    public void testExportImportMixed() throws Exception
    {
       ClassLoadingMetaData result = unmarshal();
       ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
       assertCapabilities(result, factory.createModule("test2", "2.0.0"), 
                                  factory.createPackage("test2", "2.0.0"), 
                                  factory.createModule("test1", "1.0.0"),
                                  factory.createPackage("test1", "1.0.0"));
       assertRequirements(result, factory.createRequireModule("test2", new VersionRange("2.0.0")), 
                                  factory.createRequirePackage("test2", new VersionRange("2.0.0")), 
                                  factory.createRequireModule("test1", new VersionRange("1.0.0")),
                                  factory.createRequirePackage("test1", new VersionRange("1.0.0")));
    }
 
    public void testWildcardCapability() throws Exception
    {
       ClassLoadingMetaData result = unmarshal(TestCapability.class);
       ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
       assertCapabilities(result, factory.createModule("test1", "1.0.0"), 
                                  factory.createPackage("test1", "1.0.0"),
                                  new TestCapability("test", "1.0.0"));
    }
 
    public void testWildcardRequirement() throws Exception
    {
       ClassLoadingMetaData result = unmarshal(TestRequirement.class);
       ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
       assertRequirements(result, factory.createRequireModule("test1", new VersionRange("1.0.0")), 
                                  factory.createRequirePackage("test1", new VersionRange("1.0.0")),
                                  new TestRequirement("test", new VersionRange("1.0.0")));
    }
 
    public void testOptionalRequirement() throws Exception
    {
       ClassLoadingMetaData result = unmarshal(TestRequirement.class);
       ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
       assertRequirements(result, factory.createRequireModule("test1", new VersionRange("1.0.0"), true, false, false), 
                                  factory.createRequirePackage("test1", new VersionRange("1.0.0"), true, false, false));
    }
 
    public void testDynamicRequirement() throws Exception
    {
       ClassLoadingMetaData result = unmarshal(TestRequirement.class);
       ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
       assertRequirements(result, factory.createRequireModule("test1", new VersionRange("1.0.0"), false, false, true), 
                                  factory.createRequirePackage("test1", new VersionRange("1.0.0"), false, false, true));
    }
 
    public void testReExportRequirement() throws Exception
    {
       ClassLoadingMetaData result = unmarshal(TestRequirement.class);
       ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
       assertRequirements(result, factory.createReExportModule("test1", new VersionRange("1.0.0")), 
                                  factory.createReExportPackage("test1", new VersionRange("1.0.0")));
    }
 
    public void testUsesRequirement() throws Exception
    {
       ClassLoadingMetaData result = unmarshal(TestRequirement.class);
       ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
       assertRequirements(result, factory.createUsesPackage("test1", new VersionRange("1.0.0"))); 
    }
    
    public void testParentPolicyWithName() throws Exception
    {
       ClassLoadingMetaData result = unmarshal();
       ParentPolicyMetaData ppmd = result.getParentPolicy();
       assertNotNull(ppmd);
      assertEquals("BEFORE", ppmd.getName());
      // actual PP and CF instantiation
      assertNotNull(ppmd.createParentPolicy());
    }
 
    public void testParentPolicyWithFilters() throws Exception
    {
       ClassLoadingMetaData result = unmarshal();
       ParentPolicyMetaData ppmd = result.getParentPolicy();
       assertNotNull(ppmd);
       assertNull(ppmd.getName());
       FilterMetaData before = ppmd.getBeforeFilter();
       assertNotNull(before);
       assertEqualStrings(new String[]{"org.jboss.acme", "com.redhat.acme"}, before.getValue());
       FilterMetaData after = ppmd.getAfterFilter();
       assertNotNull(after);
       assertEqualStrings(new String[]{"org.jboss.foobar", "com.redhat.foobar"}, after.getValue());
       assertEquals("Qwert", ppmd.getDescription());
       // actual PP and CF instantiation
       assertNotNull(ppmd.createParentPolicy());
    }
 
    public void testParentPolicyWithJavaBean() throws Exception
    {
       ClassLoadingMetaData result = unmarshal(JavaBean20.class);
       ParentPolicyMetaData ppmd = result.getParentPolicy();
       assertNotNull(ppmd);
       assertNull(ppmd.getName());
       FilterMetaData before = ppmd.getBeforeFilter();
       assertNotNull(before);
       // actual PP and CF instantiation
       assertNotNull(ppmd.createParentPolicy());
    }
 
    public void assertCapabilities(ClassLoadingMetaData metadata, Capability... expected)
    {
       List<Capability> temp = new ArrayList<Capability>();
       for (Capability capability : expected)
          temp.add(capability);
       assertEquals(temp, metadata.getCapabilities().getCapabilities());
    }
 
    public void assertRequirements(ClassLoadingMetaData metadata, Requirement... expected)
    {
       List<Requirement> temp = new ArrayList<Requirement>();
       for (Requirement requirement : expected)
          temp.add(requirement);
       assertEquals(temp, metadata.getRequirements().getRequirements());
    }
 
    public void assertEqualStrings(String[] expected, Object result)
    {
       assertNotNull(expected);
       assertNotNull(result);
       assertTrue(result instanceof String[]);
       String[] strings = (String[]) result;
       assertEquals(expected.length, strings.length);
       for (int i = 0; i < expected.length; i++)
          assertEquals(expected[i], strings[i]);
    }
 
    protected ClassLoadingMetaData unmarshal(Class<?>... extra) throws Exception
    {
       return unmarshalObject(ClassLoadingMetaData10.class, ClassLoadingMetaData10.class, extra);
    }
 }
