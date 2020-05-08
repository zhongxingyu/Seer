 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.tools.codegen;
 
 import static org.hamcrest.Matchers.allOf;
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.greaterThanOrEqualTo;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.List;
 import java.util.Properties;
 
 import javax.xml.bind.JAXB;
 
 import org.apache.commons.io.IOUtils;
 import org.ebayopensource.turmeric.common.config.ServiceTypeMappingConfig;
 import org.ebayopensource.turmeric.junit.asserts.ClassLoaderAssert;
 import org.ebayopensource.turmeric.junit.utils.MavenTestingUtils;
 import org.ebayopensource.turmeric.runtime.common.types.SOAConstants;
 import org.ebayopensource.turmeric.tools.GeneratedAssert;
 import org.ebayopensource.turmeric.tools.TestResourceUtil;
 import org.ebayopensource.turmeric.tools.codegen.exception.BadInputOptionException;
 import org.ebayopensource.turmeric.tools.codegen.exception.BadInputValueException;
 import org.ebayopensource.turmeric.tools.codegen.exception.CodeGenFailedException;
 import org.ebayopensource.turmeric.tools.codegen.exception.MissingInputOptionException;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Test;
 
 public class ServiceGeneratorTest extends AbstractServiceGeneratorTestCase {
 
 	private void createServiceInterfacePropertiesFile(File destDir)
 			throws IOException {
 		File sipFile = new File(destDir, "service_intf_project.properties");
 
 		Properties props = loadProperties(sipFile);
 		props.setProperty("noObjectFactoryGeneration", "true");
 
 		MavenTestingUtils.ensureDirExists(destDir);
 		writeProperties(sipFile, props);
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Test
 	public void inputOptionsBadOption() throws Exception {
 		// Initialize testing paths
 		MavenTestingUtils.ensureEmpty(testingdir);
 		File srcDir = getTestSrcDir();
 		File destDir = getTestDestDir();
 
 		// Setup arguments
 		// @formatter:off
 		String args[] = {
 			"-servicename", "EbayTestService",
 			"-badoption", "org.ebayopensource.test.soaframework.tools.codegen.UnknownInterface.java",
 			"-gentype", "All",
 			"-dest", destDir.getAbsolutePath(),
 			"-src", srcDir.getAbsolutePath(),
 			"-scv", "1.0.0"
 		};
 		// @formatter:on
 
 		try {
 			performDirectCodeGen(args);
 			Assert.fail("Expected exception of type: " + BadInputOptionException.class.getName());
 		} catch (BadInputOptionException ex) {
 			Assert.assertThat(ex.getMessage(), allOf( 
 					containsString("Unknown option specified"),
 					containsString("-badoption")));
 		}
 	}
 
 	@Test
 	public void inputOptionsPortlet() throws Exception {
 		// Initialize testing paths
 		MavenTestingUtils.ensureEmpty(testingdir);
 		File wsdl = TestResourceUtil.getResource("org/ebayopensource/turmeric/test/tools/codegen/data/RemotePortlet.wsdl");
 		File destDir = getTestDestDir();
 		File binDir = testingdir.getFile("bin");
 		File jdestDir = getTestDestPath("gen-src");
 		
 		MavenTestingUtils.ensureDirExists(getTestDestPath("meta-src"));
 
 		// Setup arguments
 		// @formatter:off
 		String args[] = {
 			"-wsdl", wsdl.getAbsolutePath(),
 			"-namespace", SOAConstants.DEFAULT_SERVICE_NAMESPACE,
 			"-servicename", "RemotePortlet",
 			"-scv", "1.0.0",
 			"-dest", destDir.getAbsolutePath(),
 			"-bin", binDir.getAbsolutePath(),
 			"-gentype", "All",
 			"-gip", "org.ebayopensource.services.remoteportlet.intf",
 			"-jdest", jdestDir.getAbsolutePath()
 		};
 		// @formatter:on
 		
 		performDirectCodeGen(args, binDir);
 		
 		GeneratedAssert.assertFileExists(destDir, "gen-src/service/org/ebayopensource/services/remoteportlet/intf/gen/RemotePortletImplSkeleton.java");
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void inputOptionsBadOption2() throws Exception {
 		// Initialize testing paths
 		MavenTestingUtils.ensureEmpty(testingdir);
 		File destDir = getTestDestDir();
 
 		// Setup arguments
 		// @formatter:off
 		String args[] = {
 			"-servicename", "EbayTestService",
 			"-interface", 	"org.ebayopensource.test.soaframework.tools.codegen.UnknownInterface.java",
 			"-gentype", "All",
 			"-dest", destDir.getAbsolutePath(),
 			"-src", /* null */
 			"-scv", "1.0.0", 
 			"-verbose"
 		};
 		// @formatter:on
 		
 		try {
 			performDirectCodeGen(args);
 			Assert.fail("Expected exception of type: " + BadInputOptionException.class.getName());
 		} catch (BadInputOptionException ex) {
 			Assert.assertThat(ex.getMessage(), allOf( 
 					containsString("Unknown option specified"),
 					containsString("1.0.0")));
 		}
 	}
 
 	@Test
 	public void inputOptionsBadClass() throws Exception {
 		String badClass = "org.ebayopensource.test.soaframework.tools.codegen.UnknownInterface";
 
 		// Initialize testing paths
 		MavenTestingUtils.ensureEmpty(testingdir);
 		File srcDir = getTestSrcDir();
 		File destDir = getTestDestDir();
 
 		// Setup arguments
 		// @formatter:off
 		String args[] = {
 			"-servicename", "EbayTestService",
 			"-interface",  badClass + ".java",
 			"-gentype", "All",
 			"-dest", destDir.getAbsolutePath(),
 			"-src",	srcDir.getAbsolutePath(),
 			"-scv",  "1.0.0"
 		};
 		// @formatter:on
 
 		try {
 			performDirectCodeGen(args);
 			Assert.fail("Expected exception of type: " + CodeGenFailedException.class.getName());
 		} catch (CodeGenFailedException ex) {
 			ex.printStackTrace(System.err);
 			Throwable cause = ex.getCause();
 			Assert.assertNotNull("Cause should not be null", cause);
 			Assert.assertThat(ex.getMessage(), containsString(badClass));
 			Assert.assertThat(cause.getMessage(), containsString("JAVAC Compile Failure"));
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void inputOptionsBadOptionViaFacade() throws Exception {
 		// Initialize testing paths
 		MavenTestingUtils.ensureEmpty(testingdir);
 		File destDir = getTestDestDir();
 
 		// Setup arguments
 		// @formatter:off
 		String args[] = {
 			"-servicename", "EbayTestService",
 			"-interface", 	"org.ebayopensource.test.soaframework.tools.codegen.UnknownInterface.java",
 			"-gentype", "All",
 			"-dest", destDir.getAbsolutePath(),
 			"-src", /* null */ 
 			"-scv", "1.0.0", "-verbose"
 		};
 		// @formatter:on
 
 		try {
 			performDirectCodeGen(args);
 			Assert.fail("Expected exception of type: " + BadInputOptionException.class.getName());
 		} catch (BadInputOptionException ex) {
 			Assert.assertThat(ex.getMessage(), allOf( 
 					containsString("Unknown option specified"),
 					containsString("1.0.0")));
 		}
 	}
 
 	@Test
 	public void inputOptionsMissingServiceName() throws Exception {
 		// Initialize testing paths
 		MavenTestingUtils.ensureEmpty(testingdir);
 		File destDir = getTestDestDir();
 
 		// Setup arguments
 		// @formatter:off
 		String args[] = {
 			"-scv", "1.0.0",
 			"-interface", "org.ebayopensource.test.soaframework.tools.codegen.UnknownInterface.java",
 			"-gentype", "All",
 			"-dest", destDir.getAbsolutePath(),
 			"-src", /* null */ 
 			"-verbose"
 		};
 		// @formatter:on
 		
 		try {
 			performDirectCodeGen(args);
 			Assert.fail("Expected exception of type: " + MissingInputOptionException.class.getName());
 		} catch (MissingInputOptionException ex) {
 			Assert.assertThat(ex.getMessage(), containsString("Service name is missing"));
 		}
 	}
 
 	@Test
 	public void serviceGeneratorClass1() throws Exception {
 		// Initialize testing paths
 		testingdir.ensureEmpty();
 		File srcDir = getTestSrcDir();
 		File destDir = getTestDestDir();
 
 		// Setup arguments
 		// @formatter:off
 		String args[] = {
 			"-servicename", "TestService",
 			"-class", TestService.class.getName() + ".java",
 			"-gentype", "All",
 			"-src", srcDir.getAbsolutePath(),
 			"-dest", destDir.getAbsolutePath(),
 			"-gin", "TestServiceInterface",
 			"-scv", "1.0.0",
 			"-cn", "TestService" 
 		};
 		// @formatter:on
 		
 		performDirectCodeGen(args, new File(destDir, "bin"));
 	}
 
 	@Test
 	public void createSecurityPolicyConfig1() throws Exception {
 		// Initialize testing paths
 		MavenTestingUtils.ensureEmpty(testingdir);
 		File srcDir = getTestSrcDir();
 		File destDir = getTestDestDir();
 
 		// Setup arguments
 		// @formatter:off
 		String args[] =  new String[] {
 			"-servicename", "TestSecuirtyPolicy",
 			"-interface", "NotRequired",
 			"-gentype", "SecurityPolicyConfig",
 			"-src", srcDir.getAbsolutePath(),
 			"-dest", destDir.getAbsolutePath(),
 			"-namespace", "http://www.ebayopensource.org/soa/TestSecuirtyPolicy"
 		};
 		// @formatter:on
 		
 		performDirectCodeGen(args);
 	}
 
 	@Test
 	public void servIntfPropFileForFailureCase1() throws Exception {
 		mavenTestingRules.setFailOnViolation(false);
 		
 		// Initialize testing paths
 		MavenTestingUtils.ensureEmpty(testingdir);
 
 		// Setup arguments
 		// @formatter:off
 		String args[] = { // gentype ServiceIntfProjectProps without -pr should error out
 			"-servicename",	"ShouldNotCreateService",
 			"-wsdl", /* null */
 			"-gentype","ServiceIntfProjectProps",
 			"-sl","www.amazon.com:9089/getAllTracking"
 		};
 		// @formatter:on
 		
 		try {
 			performDirectCodeGen(args);
 			Assert.fail("Expected exception of type: " + MissingInputOptionException.class.getName());
 		} catch (MissingInputOptionException ex) {
 			Assert.assertThat(ex.getMessage(), containsString("input option -pr is mandatory"));
 		}
 	}
 
 	@Test
 	public void servIntfPropFileForFailureCase2() throws Exception {
 		// Initialize testing paths
 		MavenTestingUtils.ensureEmpty(testingdir);
 		File rootDir = testingdir.getDir();
 
 		// Setup arguments
 		// @formatter:off
 		String args[] = { // gentype ServiceIntfProjectProps without -sl should error out
 			"-servicename",	"ShouldNotCreateService",
 			"-wsdl", /* null */
 			"-gentype","ServiceIntfProjectProps",
 			"-pr", rootDir.getAbsolutePath()
 		};
 		// @formatter:on
 		
 		try {
 			performDirectCodeGen(args);
 			Assert.fail("Expected exception of type: " + MissingInputOptionException.class.getName());
 		} catch (MissingInputOptionException ex) {
 			Assert.assertThat(ex.getMessage(), containsString("input option -sl is mandatory"));
 		}
 	}
 
 	@Test
 	public void testDefaultingInputTypeInterface() throws Exception {
 		// Initialize testing paths
 		MavenTestingUtils.ensureEmpty(testingdir.getDir());
 		File srcDir = getTestSrcDir();
 		File destDir = getTestDestDir();
 		File rootDir = testingdir.getDir();
 		
 		MavenTestingUtils.ensureDirExists(new File(destDir, "bin")); // so compile works
 
 		// generate the service_metadata.properties
 		// @formatter:off
 		String args1[] = new String[] {//this is a interface based service
 			"-servicename",	"MyCalcService9021",
 			"-interface", "org.ebayopensource.turmeric.tools.codegen.SimpleServiceInterface.java",
 			"-gentype", "ServiceMetadataProps",
 			"-pr", rootDir.getAbsolutePath(),
 			"-scv","1.2.0",
 			"-slayer","COMMON"
 		}; 
 		// @formatter:on
 		
 		performDirectCodeGen(args1);
 		
 		// generate all the other artifacts
 		String args2[] = new String[] { // not providing the inputType, the code should default to interface based service
 			"-servicename",	"MyCalcService9021",
 			"-gentype", "All",
 			"-pr", rootDir.getAbsolutePath(),
 			"-dest", destDir.getAbsolutePath(),
 			"-src", srcDir.getAbsolutePath()
 		}; 
 		performDirectCodeGen(args2);
 	}
 
 	@Test
 	public void invalidAslContents_1() throws Exception {
 		// Initialize testing paths
 		MavenTestingUtils.ensureEmpty(testingdir.getDir());
 		File asl = TestResourceUtil.getResource("org/ebayopensource/turmeric/test/util/service_layers_invalid_1.txt");
 		File srcDir = getTestSrcDir();
 		File destDir = getTestDestDir();
 
 		// Setup arguments
 		// @formatter:off
 		String args[] =  new String[] {
 			"-servicename", "MyGlobalConfig1",
 			"-interface", "NotRequired",
 			"-gentype", "GlobalServerConfig",
 			"-src", srcDir.getAbsolutePath(),
 			"-dest", destDir.getAbsolutePath(),
 			"-asl",asl.getAbsolutePath(),
 			"-namespace", "http://www.ebayopensource.org/soa/MyGlobalConfig" 
 		};
 		// @formatter:on
 			
 		try {
 			performDirectCodeGen(args);
 			Assert.fail("Should have thrown a " + BadInputValueException.class.getName());
 		} catch (BadInputValueException e) {
 			Assert.assertThat(e.getMessage(),
 					containsString("Service Layer file (-asl) is not valid"));
 		}
 	}
 
 	@Test
 	public void invalidAslContents_2() throws Exception {
 		// Initialize testing paths
 		MavenTestingUtils.ensureEmpty(testingdir.getDir());
 		File asl = TestResourceUtil.getResource("org/ebayopensource/turmeric/test/util/service_layers_invalid_2.txt");
 		File srcDir = getTestSrcDir();
 		File destDir = getTestDestDir();
 
 		// Setup arguments
 		// @formatter:off
 		String args[] =  new String[] {
 			"-servicename", "MyGlobalConfig1",
 			"-interface", "NotRequired",
 			"-gentype", "GlobalServerConfig",
 			"-src", srcDir.getAbsolutePath(),
 			"-dest", destDir.getAbsolutePath(),
 			"-asl", asl.getAbsolutePath(),
 			"-namespace", "http://www.ebayopensource.org/soa/MyGlobalConfig" 
 		};
 		// @formatter:on
 
 		try {
 			performDirectCodeGen(args);
 			Assert.fail("Should have thrown a " + BadInputValueException.class.getName());
 		} catch (BadInputValueException e) {
 			Assert.assertThat(e.getMessage(),
 					containsString("Service Layer file (-asl) is not valid"));
 		}
 	}
 
 	@Test
 	public void validAslContents_1() throws Exception {
 		// Initialize testing paths
 		MavenTestingUtils.ensureEmpty(testingdir.getDir());
 		File asl = TestResourceUtil.getResource("org/ebayopensource/turmeric/test/util/service_layers.txt");
 		File srcDir = getTestSrcDir();
 		File destDir = getTestDestDir();
 
 		// Setup arguments
 		// @formatter:off
 		String args[] =  new String[] {
 			"-servicename", "ServiceASL_1",
 			"-interface", "NotRequired",
 			"-gentype", "GlobalServerConfig",
 			"-src", srcDir.getAbsolutePath(),
 			"-dest", destDir.getAbsolutePath(),
 			"-asl", asl.getAbsolutePath(),
 			"-namespace", "http://www.ebayopensource.org/soa/MyGlobalConfig" 
 		};
 		// @formatter:on
 
 		performDirectCodeGen(args);
 	}
 
 	@Test
 	public void validAslContents_2() throws Exception {
 		// Initialize testing paths
 		MavenTestingUtils.ensureEmpty(testingdir.getDir());
 		File asl = TestResourceUtil.getResource("org/ebayopensource/turmeric/test/util/service_layers_2.txt");
 		File srcDir = getTestSrcDir();
 		File destDir = getTestDestDir();
 
 		// Setup arguments
 		// @formatter:off
 		String args[] =  new String[] {
 			"-servicename", "ServiceASL_2",
 			"-interface", "NotRequired",
 			"-gentype", "GlobalServerConfig",
 			"-src", srcDir.getAbsolutePath(),
 			"-dest", destDir.getAbsolutePath(),
 			"-asl", asl.getAbsolutePath(),
 			"-namespace", "http://www.ebayopensource.org/soa/MyGlobalConfig" 
 		};
 		// @formatter:on
 
 		performDirectCodeGen(args);
 	}
 
 	@Test
 	public void defaultingServiceLayerFromASLfileHavingOneLayer() throws Exception {
 		// Initialize testing paths
 		MavenTestingUtils.ensureEmpty(testingdir.getDir());
 		File asl = TestResourceUtil.getResource("org/ebayopensource/turmeric/test/util/service_layers_having_one_layer.txt");
 		File srcDir = getTestSrcDir();
 		File destDir = getTestDestDir();
 
 		// Setup arguments
 		// @formatter:off
 		String args[] =  new String[] {
 			"-servicename", "ServiceASL_3",
 			"-interface", "NotRequired",
 			"-gentype", "ServiceMetadataProps",
 			"-src", srcDir.getAbsolutePath(),
 			"-dest", destDir.getAbsolutePath(),
 			"-asl", asl.getAbsolutePath(),
 			"-namespace", "http://www.ebayopensource.org/soa/MyGlobalConfig" 
 		};
 		// @formatter:on
 
 		performDirectCodeGen(args);
 	}
 
 	public void defaultingServiceLayerFromASLfileHavingMoreThanOneLayer() throws Exception {
 		// Initialize testing paths
 		MavenTestingUtils.ensureEmpty(testingdir.getDir());
 		File asl = TestResourceUtil.getResource("org/ebayopensource/turmeric/test/util/service_layers_having_many_layers.txt");
 		File srcDir = getTestSrcDir();
 		File destDir = getTestDestDir();
 
 		// Setup arguments
 		// @formatter:off
 		String args[] =  new String[] {
 			"-servicename", "ServiceASL_4",
 			"-interface", "NotRequired",
 			"-gentype", "ServiceMetadataProps",
 			"-src", srcDir.getAbsolutePath(),
 			"-dest", destDir.getAbsolutePath(),
 			"-asl", asl.getAbsolutePath(),
 			"-namespace", "http://www.ebayopensource.org/soa/MyGlobalConfig" 
 		};
 		// @formatter:on
 
 		performDirectCodeGen(args);
 	}
 
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void ns2PkgFailureCase_1() throws Exception {
 		MavenTestingUtils.ensureEmpty(testingdir.getDir());
 		File wsdl = TestResourceUtil.getResource("org/ebayopensource/turmeric/test/tools/codegen/data/ComplexService_100.wsdl");
 		File srcDir = getTestSrcDir();
 		File destDir = getTestDestDir();
 		File binDir = testingdir.getFile("bin");
 
 		// @formatter:off
 		String args[] =  new String[] {
 			"-servicename", "CalcService",
 			"-wsdl", wsdl.getAbsolutePath(),
 			"-ns2pkg","www.abc.com",
 			"-gentype", "All",
 			"-src", srcDir.getAbsolutePath(),
 			"-dest", destDir.getAbsolutePath(),
 			"-namespace", "http://www.ebayopensource.org/soaframework/service/calc",
 			"-scv", "1.0.0",
 			"-gip", "org.ebayopensource.test.soaframework.tools.codegen",
 			"-bin", binDir.getAbsolutePath(),
 			"-cn", "CalcService",
 			"-icsi", 
 			"-gin", "CalculatorSvcIntf" 
 		};
 		// @formatter:on
 
 		try {
 			performDirectCodeGen(args);
 			Assert.fail("Should have thrown a " + BadInputValueException.class.getName());
 		} catch (BadInputValueException e) {
 			Assert.assertThat(e.getMessage(), allOf(
 					containsString("Input value specified for '-ns2pkg' option is not well-formed"),
 					containsString("should be in ns1=pkg1,ns2=pkg2 format")));
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void ns2PkgFailureCase_2() throws Exception {
 
 		MavenTestingUtils.ensureEmpty(testingdir.getDir());
 		File wsdl = TestResourceUtil.getResource("org/ebayopensource/turmeric/test/tools/codegen/data/ComplexService_100.wsdl"); 
 		File srcDir = getTestSrcDir();
 		File destDir = getTestDestDir();
 		File binDir = testingdir.getFile("bin");
 
 		// @formatter:off
 		String args[] =  new String[] {
 			"-servicename", "CalcService",
 			"-wsdl", wsdl.getAbsolutePath(),
 			"-ns2pkg","www.abc.com=",
 			"-gentype", "All",
 			"-src", srcDir.getAbsolutePath(),
 			"-dest", destDir.getAbsolutePath(),
 			"-namespace", "http://www.ebayopensource.org/soaframework/service/calc",
 			"-scv", "1.0.0",
 			"-gip", "org.ebayopensource.test.soaframework.tools.codegen",
 			"-bin", binDir.getAbsolutePath(),
 			"-cn", "CalcService",
 			"-icsi", 
 			"-gin", "CalculatorSvcIntf" 
 		};
 		// @formatter:on
 
 		try {
 			performDirectCodeGen(args);
 			Assert.fail("Should have thrown a " + BadInputValueException.class.getName());
 		} catch (BadInputValueException e) {
 			Assert.assertThat(e.getMessage(), allOf(
 					containsString("Input value specified for '-ns2pkg' option is not well-formed"),
 					containsString("should be in ns1=pkg1,ns2=pkg2 format")));
 		}
 	}
 
 	@Test
 	public void ns2PkgFailureCase_3() throws Exception {
 		MavenTestingUtils.ensureEmpty(testingdir.getDir());
 		File wsdl = TestResourceUtil.getResource("org/ebayopensource/turmeric/test/tools/codegen/data/ComplexService_100.wsdl");
 		File srcDir = getTestSrcDir();
 		File destDir = getTestDestDir();
 		File binDir = testingdir.getFile("bin");
 
 		// @formatter:off
 		String args[] =  new String[] {
 			"-servicename", "CalcService",
 			"-wsdl", wsdl.getAbsolutePath(),
 			"-ns2pkg","www.abc.com/index=abc.def.ghk=",
 			"-gentype", "All",
 			"-src", srcDir.getAbsolutePath(),
 			"-dest", destDir.getAbsolutePath(),
 			"-namespace", "http://www.ebayopensource.org/soaframework/service/calc",
 			"-scv", "1.0.0",
 			"-gip", "org.ebayopensource.test.soaframework.tools.codegen",
 			"-bin", binDir.getAbsolutePath(),
 			"-cn", "CalcService",
 			"-icsi", 
 			"-gin", "CalculatorSvcIntf" 
 		};
 		// @formatter:on
 
 		try {
 			performDirectCodeGen(args);
 			Assert.fail("Should have thrown a " + BadInputValueException.class.getName());
 		} catch (BadInputValueException e) {
 			Assert.assertThat(e.getMessage(), 
 					containsString("provided for the option -ns2pkg is not in the prescribed format of \"ns=pkg\""));
 		}
 	}
 
 	@Test
 	public void ns2Pkg() throws Exception {
 		ClassLoaderAssert.assertClassPresent("org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException");
 		
 		// Initialize testing paths
 		MavenTestingUtils.ensureEmpty(testingdir.getDir());
 		File wsdl = TestResourceUtil.getResource("org/ebayopensource/turmeric/test/tools/codegen/data/ComplexService_100.wsdl");
 		File srcDir = getTestSrcDir();
 		File destDir = getTestDestDir();
 		File binDir = testingdir.getFile("bin");
 
 		// Setup arguments
 		// @formatter:off
 		String args[] =  new String[] {
 			"-servicename", "CalcService",
 			"-wsdl", wsdl.getAbsolutePath(),
 			"-ns2pkg","http://www.ebayopensource.org/soaframework/service/ComplexService=abc.def.ghk",
 			"-gentype", "All",
 			"-src", srcDir.getAbsolutePath(),
 			"-dest", destDir.getAbsolutePath(),
 			"-namespace", "http://www.ebayopensource.org/soaframework/service/calc",
 			"-scv", "1.0.0",
 			"-gip", "org.ebayopensource.test.soaframework.tools.codegen",
 			"-bin", binDir.getAbsolutePath(),
 			"-cn", "CalcService",
 			"-icsi", "-gin", "CalculatorSvcIntf" 
 		};
 		// @formatter:on
 
 		performDirectCodeGen(args, binDir);
 	}
 	
 	@Test
 	public void testTypeLibraryOptionFailureCase() throws Exception {
 		// Initialize testing paths
 		MavenTestingUtils.ensureEmpty(testingdir.getDir());
 		File wsdl = TestResourceUtil.getResource("org/ebayopensource/turmeric/test/tools/codegen/data/CalcServiceWithImport.wsdl");
 		File srcDir = getTestSrcDir();
 		File destDir = getTestDestDir(); 
 		File binDir = testingdir.getFile("bin");
 
 		// Setup arguments
 		// @formatter:off
 		String args[] =  new String[] {
 			"-servicename", "CalcService",
 			"-wsdl", wsdl.getAbsolutePath(),
 			"-gentype", "All",
 			"-src", srcDir.getAbsolutePath(),
 			"-dest", destDir.getAbsolutePath(),
 			"-scv", "1.0.0",
 			"-gip", "org.ebayopensource.test.soaframework.tools.codegen",
 			"-bin", binDir.getAbsolutePath()
 		};
 		// @formatter:on
 		
 		performDirectCodeGen(args, binDir);
 		
 		GeneratedAssert.assertFileExists(destDir, "gen-src/org/ebayopensource/turmeric/services/Add.java");
 		GeneratedAssert.assertFileExists(destDir, "gen-src/org/ebayopensource/turmeric/services/AddResponse.java");
 	}
 
 	@Test
 	public void checkObjectFactoryClassGeneration() throws Exception {
 		MavenTestingUtils.ensureEmpty(testingdir.getDir());
 		File wsdl = TestResourceUtil.getResource("org/ebayopensource/turmeric/test/tools/codegen/data/CalcService.wsdl");
 		File srcDir = getTestSrcDir();
 		File destDir = getTestDestDir();
 		File binDir = testingdir.getFile("bin");
 
 		// @formatter:off
 		String args[] =  new String[] {
 			"-servicename", "CalculatorService",
 			"-wsdl", wsdl.getAbsolutePath(),
 			"-gentype", "All",
 			"-src", srcDir.getAbsolutePath(),
 			"-dest", destDir.getAbsolutePath(),
 			"-scv", "1.0.0",
 			"-noObjectFactoryGeneration","true",
 			"-bin", binDir.getAbsolutePath()
 		};
 		// @formatter:on
 		
 		performDirectCodeGen(args, binDir);
 		
 		GeneratedAssert.assertPathNotExists(destDir, "gen-src/org/ebayopensource/marketplace/servies/ObjectFactory.java");
 	}
 	
 	@Test
 	public void checkObjectFactoryClassGenerationCase2() throws Exception {
 		
 		MavenTestingUtils.ensureEmpty(testingdir.getDir());
 		File wsdl = TestResourceUtil.getResource("org/ebayopensource/turmeric/test/tools/codegen/data/CalcService.wsdl");
 		File srcDir = getTestSrcDir();
 		File destDir = getTestDestDir();
 		File binDir = testingdir.getFile("bin");
 
 		createServiceInterfacePropertiesFile(destDir);
 
 		// @formatter:off
 		String args[] = new String[] {
 			"-servicename", "CalculatorService",
 			"-wsdl", wsdl.getAbsolutePath(),
 			"-gentype", "All", 
 			"-src", srcDir.getAbsolutePath(), 
 			"-dest", destDir.getAbsolutePath(), 
 			"-pr",destDir.getAbsolutePath(),
 			"-scv", "1.0.0", 
 			"-noObjectFactoryGeneration","true",
 			"-bin", binDir.getAbsolutePath() 
 		};
 		// @formatter:on
 		
 		performDirectCodeGen(args, binDir);
 
 		GeneratedAssert.assertPathNotExists(destDir, "gen-src/org/ebayopensource/turmeric/common/v1/services/ObjectFactory.java");
 	}
 
 	@Test
 	public void checkObjectFactoryClassGenerationCase3() throws Exception {
 		MavenTestingUtils.ensureEmpty(testingdir.getDir());
 		File wsdl = TestResourceUtil.getResource("org/ebayopensource/turmeric/test/tools/codegen/data/CalcService.wsdl");
 		File destDir = getTestDestDir();
 		File binDir = testingdir.getFile("bin");
 		MavenTestingUtils.ensureDirExists(getTestDestPath("meta-src"));
 
 		// @formatter:off
 		String args[] = new String[] {
 			"-servicename", "CalculatorService",
 			"-wsdl", wsdl.getAbsolutePath(),
 			"-gentype", "All",
 			"-noObjectFactoryGeneration","trueeee",
 			"-dest",destDir.getAbsolutePath(), 
 			"-scv", "1.0.0", 
 			"-bin", binDir.getAbsolutePath() 
 		};
 		// @formatter:on
 		
 		performDirectCodeGen(args, binDir);
 
 		GeneratedAssert.assertFileExists(destDir, "gen-src/org/ebayopensource/turmeric/common/v1/services/ObjectFactory.java");
 	}
 
 	@Test
 	public void checkObjectFactoryClassGenerationCase4() throws Exception {
 		MavenTestingUtils.ensureEmpty(testingdir.getDir());
 		File wsdl = TestResourceUtil.getResource("org/ebayopensource/turmeric/test/tools/codegen/data/CalcService.wsdl");
 		File destDir = getTestDestDir();
 		File binDir = testingdir.getFile("bin");
 
 		createServiceInterfacePropertiesFile(destDir);
 		MavenTestingUtils.ensureDirExists(getTestDestPath("meta-src"));
 
 		// @formatter:off
 		String args[] = new String[] {
			"-servicename", "CalculatorService1",
 			"-wsdl", wsdl.getAbsolutePath(),
 			"-gentype", "All",
 			"-noObjectFactoryGeneration","false",
 			"-dest", destDir.getAbsolutePath(), 
 			"-scv", "1.0.0", 
 			"-bin", binDir.getAbsolutePath() 
 		};
 		// @formatter:on
 			
 		performDirectCodeGen(args, binDir);
 
 		GeneratedAssert.assertFileExists(destDir, "gen-src/org/ebayopensource/turmeric/common/v1/services/ObjectFactory.java");
 	}
 
 	@Test
 	public void typeMappingsForJavaTypeListSimpleTypeNoJavaFile() throws Exception {
 		MavenTestingUtils.ensureEmpty(testingdir);
 		File wsdl = TestResourceUtil.getResource("org/ebayopensource/turmeric/test/tools/codegen/data/test_wsdl_for_type_mappings.wsdl");
 		File srcDir = getTestSrcDir();
 		File destDir = getTestDestDir();
 		File binDir = testingdir.getFile("bin");
 
 		// @formatter:off
 		String args[] =  new String[] {
			"-servicename", "CalculatorService2",
 			"-wsdl", wsdl.getAbsolutePath(),
 			"-gentype", "All",
 			"-src", srcDir.getAbsolutePath(),
 			"-dest", destDir.getAbsolutePath(),
 			"-namespace", "http://www.ebayopensource.org/soaframework/service/calc",
 			"-scv", "1.0.0",
 			"-gip", "org.ebayopensource.test.soaframework.tools.codegen",
 			"-bin", binDir.getAbsolutePath()
 		};
 		// @formatter:on
 
 		performDirectCodeGen(args, binDir);
 
 		File typeMappingsFile = getTestDestPath("gen-meta-src/META-INF/soa/common/config/CalculatorService2/TypeMappings.xml");
 			
 		ServiceTypeMappingConfig serviceTypeMappingConfig = JAXB.unmarshal(typeMappingsFile, ServiceTypeMappingConfig.class);
 		Assert.assertNotNull("ServiceTypeMappingConfig should not be null", serviceTypeMappingConfig);
 		
 		List<String> listOfJavaTypes = serviceTypeMappingConfig.getJavaTypeList().getJavaTypeName();
 		Assert.assertNotNull("List of Java Types should not be null", listOfJavaTypes);
 		
 		// @formatter:off
 		String expectedTypes[] = {
 			"org.ebayopensource.test.soaframework.tools.codegen.Add",
 			"org.ebayopensource.test.soaframework.tools.codegen.AddResponse",
 			"org.ebayopensource.test.soaframework.tools.codegen.SOne"
 		};
 		// @formatter:on
 		
 		Assert.assertThat("List of Java Types.size", listOfJavaTypes.size(), greaterThanOrEqualTo(expectedTypes.length));
 
 		for (String expectedType : expectedTypes) {
 			Assert.assertTrue("Should have found Java Type in "
 					+ "ServiceTypeMappingConfig.getJavaTypeList(): "
 					+ expectedType, listOfJavaTypes.contains(expectedType));
 		}
 	}
 
 	@Test
 	public void servicegenerationWithEnabledNamespaceFolding() throws Exception {
 		MavenTestingUtils.ensureEmpty(testingdir.getDir());
 		File wsdl = getCodegenDataFileInput("Testing.wsdl");
 		File srcDir = getTestSrcDir();
 		File destDir = getTestDestDir();
 		File binDir = testingdir.getFile("bin");
 		MavenTestingUtils.ensureDirExists(getTestDestPath("meta-src"));
 
 		// @formatter:off
 		String args[] =  new String[] {
 			"-servicename", "PayPalAPIInterfaceService",
 			"-wsdl", wsdl.getAbsolutePath(),
 			"-gentype", "All",
 			"-src", srcDir.getAbsolutePath(),
 			"-dest", destDir.getAbsolutePath(),
 			"-scv", "1.0.0",
 			"-enablednamespacefolding",
 			"-bin", binDir.getAbsolutePath()
 		};
 		// @formatter:on
 	
 		performDirectCodeGen(args, binDir);
 
 		GeneratedAssert.assertFileExists(destDir, "gen-meta-src/META-INF/soa/services/wsdl/PayPalAPIInterfaceService_mns.wsdl");
 	}
 
 	@Test
 	public void createBaseconsumerWithNewMethod() throws Exception {
 		MavenTestingUtils.ensureEmpty(testingdir.getDir());
 		File wsdl = getCodegenDataFileInput("CalcService.wsdl");
 		File srcDir = getTestSrcDir();
 		File destDir = getTestDestDir();
 		File binDir = testingdir.getFile("bin");
 
 		// @formatter:off
 		String args[] =  new String[] {
 			"-servicename", "CalculatorService",
 			"-wsdl", wsdl.getAbsolutePath(),
 			"-gentype", "All",
 			"-src", srcDir.getAbsolutePath(),
 			"-dest", destDir.getAbsolutePath(),
 			"-scv", "1.0.0",
 			"-bin", binDir.getAbsolutePath()
 		};
 		// @formatter:on
 		
 		performDirectCodeGen(args, binDir);
 		
 		assertGeneratedContainsSnippet("gen-src/org/ebayopensource/turmeric/common/v1/services/gen/BaseCalculatorServiceConsumer.java", 
 				"BaseConsumerClass.txt", 
 				null, null, null);
 	}
 
 	
 	@After
 	public void deinit(){
 		
 	}
 }
