 package org.ebayopensource.turmeric.tools.codegen;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import org.ebayopensource.turmeric.junit.utils.MavenTestingUtils;
 import org.ebayopensource.turmeric.tools.codegen.exception.MissingInputOptionException;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TestName;
 
 import com.ebay.kernel.util.FileUtils;
 
 public class SOA29FeatureTests extends AbstractServiceGeneratorTestCase {
 	@Rule public TestName name = new TestName();
 	
 	ServiceGenerator gen = null;
 	
 
 	
 	File destDir = null;
 	Properties implProps = new Properties();
 	final String IMPL_PROPERTIES = "service_impl_project.properties";
 	final String INTF_PROPERTIES = "service_intf_project.properties";
 	File implProperty = null;
 	File intfProperty = null;
 	Properties intfProps = new Properties();
 	ClassLoader originalLoader;
 	@Before
 	public void init() throws Exception{
 		
 		
 
 		
 		gen = new ServiceGenerator();
 		
 		destDir = testingdir.getDir();
 		
 		originalLoader = Thread.currentThread().getContextClassLoader();
 		URL [] urls = {destDir.toURI().toURL()};
 		setURLsInClassLoader(urls);	
 		
 		MavenTestingUtils.ensureEmpty(destDir);
 		try {
 			implProperty =	createPropertyFile(destDir.getAbsolutePath(), IMPL_PROPERTIES);
 			
 			intfProperty =	createPropertyFile(destDir.getAbsolutePath(), INTF_PROPERTIES);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		//enter values to property file
 		implProps = new Properties();
 		
 		
 		
 		intfProps.put("sipp_version","1.1");
 		
 	}
 	
 	@Test
 	public void testKeepAliveInConfig()throws Exception{
 		
 		String [] testArgs=  new String[] {
 				"-servicename","BillingSuService",
 				"-genType", "ClientConfig",
 				"-interface","com.ebay.services.interface.BillingSuService",
 				"-dest", destDir.getAbsolutePath(),
 				"-scv", "1.2.3",
 				"-slayer","COMMON",
 				"-pr",destDir.getAbsolutePath(),
 				"-consumerid","123",
 				"-cn","SampleConsumer",
 				"-environment","production",
 				"-adminname","BillingSuService"
 				
 	
 			};	
 		
 		ServiceGenerator sgen = new ServiceGenerator();
 		sgen.startCodeGen(testArgs);
 		
 		 String path = destDir + "/gen-meta-src/META-INF/soa/client/config/SampleConsumer/production/BillingSuService/ClientConfig.xml";
 		 String xml = readFileAsString(path);
 		 Assert.assertTrue(xml.contains("<option name=\"KEEP_ALIVE\">true</option>"));
 		 
 	}
 	
 	
 	
 	
 	@Test
 	public void testAddedSettersChangedAccessModifiers() throws Exception{
 		File wsdlFile = getProtobufRelatedInput("ShippingService.wsdl");
 		intfProps.put("sipp_version","1.2");
 		fillProperties(intfProps, intfProperty);
 		
 		List<String> list = new ArrayList<String>();
 		String [] testArgs = {"-serviceName","CalculatorService",
 				  "-mdest",destDir +"/meta-src",
 				  "-genType","ServiceFromWSDLIntf",
 				  "-wsdl",wsdlFile.getAbsolutePath(),
 				  "-gip","com.ebay.marketplace.shipping.v1.services",
 				  "-adminname","CalculatorService",
 				  "-slayer","BUSINESS",
 				  "-jdest",destDir +"/gen-src",
 				  "-namespace","http://www.ebay.com/marketplace/shipping/v1/services",
 				  "-pr",destDir.getAbsolutePath(),
 				  "-bin",destDir.getAbsolutePath(),
 				  "-slayer","INTERMEDIATE",
 				  "-scv","1.0.0",
 				  "-dest",destDir.getAbsolutePath(),
 				 
 				 };
 		performDirectCodeGen(testArgs, destDir);
 		
 		Constructor<?> constr = null;
 	    Class<?> cls = loadClass("com.ebay.marketplace.shipping.v1.services.calculatorservice.gen.SharedCalculatorServiceConsumer");
 		Method []  mtds = cls.getDeclaredMethods();
 		boolean present = false;
 		int count =0;
 		
 		for(Constructor<?> c: cls.getDeclaredConstructors() ){
 		 if(c.getParameterTypes().length ==1){
 			 constr = c;
 		 }
 		}
 		Object constructorObj =null;
 		for(Method m : mtds){
 			
 			if(m.getName().equals("setServiceLocation") || m.getName().equals("setAuthToken") || m.getName().equals("setCookies") || m.getName().equals("getServiceLocation") ||
 					 m.getName().equals("getAuthToken") ||  m.getName().equals("getCookies") ||   m.getName().equals("setHostName") || m.getName().equals("getHostName")){
 			
 				count = count+ 1;
 			}
 			
 			if(m.getName().equals("setServiceLocation")){
 			
 				Assert.assertTrue(Modifier.isPublic(m.getModifiers()));
 			}
 			if(m.getName().equals("setAuthToken")){
 			
 				Assert.assertTrue(Modifier.isPublic(m.getModifiers()));
 			}
 			if(m.getName().equals("setCookies")){
 			
 				Assert.assertTrue(Modifier.isPublic(m.getModifiers()));
 			}
 			
 			if(m.getName().equals("setHostName") && m.getParameterTypes().length==1){
 				
 				constructorObj = constr.newInstance(new String("dummy"));
 				m.invoke(constructorObj,"d-sjc-00507487.corp.ebay.com");
 				
 			}
 			
 			if(m.getName().equals("getHostName")){
 				
 				String host = (String) m.invoke(constructorObj);
 				Assert.assertEquals("d-sjc-00507487.corp.ebay.com",host);
 			}
 			
 			
 			
 		}
 		for(Method m : mtds){
 		if(m.getName().equals("getServiceLocation")){
 			
 			URL serviceLocation = (URL) m.invoke(constructorObj);
 			Assert.assertEquals("http://d-sjc-00507487.corp.ebay.com/services",serviceLocation.toString());
 		}
 		}
 
 		Assert.assertTrue("one of the method is missing(setServiceLocation,setAuthToken,setCookies,getServiceLocation,getAuthToken,getCookies,setHostName,getHostName)",count == 8);
 		
 	}
 	
 	@Test
 	public void testServiceLocationFromConfigFile() throws Exception{
 		File wsdlFile = getProtobufRelatedInput("ShippingService.wsdl");
 		intfProps.put("sipp_version","1.2");
 		fillProperties(intfProps, intfProperty);
 		
 		
 		
 		String [] testArgs = {"-serviceName","calculatorservice",
 				  "-mdest",destDir +"/meta-src",
 				  "-genType","ServiceFromWSDLIntf",
 				  "-wsdl",wsdlFile.getAbsolutePath(),
 				  "-gip","com.ebay.marketplace.shipping.v1.services",
				  "-adminname","CalculatorService",
 				  "-slayer","BUSINESS",
 				  "-jdest",destDir +"/gen-src",
 				  "-namespace","http://www.ebay.com/marketplace/shipping/v1/services",
 				  "-dest",destDir.getAbsolutePath(),
 				  "-bin",destDir.getAbsolutePath(),
 				  "-slayer","INTERMEDIATE",
 				  "-scv","1.0.0",
 				  "-pr",destDir.getAbsolutePath()
 				 };
 		performDirectCodeGen(testArgs, destDir);
 		
 		Constructor<?> constr = null;
 	    Class<?> cls = loadClass("com.ebay.marketplace.shipping.v1.services.calculatorservice.gen.SharedCalculatorServiceConsumer");
 		
 	    Thread.currentThread().setContextClassLoader(originalLoader);
 	    
 	    Method []  mtds = cls.getDeclaredMethods();
 		boolean present = false;
 		int count =0;
 		
 		for(Constructor<?> c: cls.getDeclaredConstructors() ){
 		 if(c.getParameterTypes().length ==1){
 			 constr = c;
 		 }
 		}
 		Object constructorObj =null;
 		constructorObj = constr.newInstance(new String("dummy"));	
 			
 		
 		for(Method m : mtds){
 		if(m.getName().equals("getServiceLocation")){
 			
 			URL serviceLocation = (URL) m.invoke(constructorObj);
 			Assert.assertEquals("http://www.ebay.com/services",serviceLocation.toString());
 		}
 		}
 		
 		
 	}
 	@Test
 	public void testServiceLocationFromSharedConsumer() throws Exception{
 		File wsdlFile = getProtobufRelatedInput("ShippingService.wsdl");
 		
 		intfProps.put("sipp_version","1.2");
 		fillProperties(intfProps, intfProperty);
 		
 		String [] testArgs = {"-serviceName","calculatorservice",
 				  "-mdest",destDir +"/meta-src",
 				  "-genType","ServiceFromWSDLIntf",
 				  "-wsdl",wsdlFile.getAbsolutePath(),
 				  "-gip","com.ebay.marketplace.shipping.v1.services",
				  "-adminname","CalculatorService",
 				  "-slayer","BUSINESS",
 				  "-jdest",destDir +"/gen-src",
 				  "-namespace","http://www.ebay.com/marketplace/shipping/v1/services",
 				  "-dest",destDir.getAbsolutePath(),
 				  "-bin",destDir.getAbsolutePath(),
 				  "-slayer","INTERMEDIATE",
 				  "-scv","1.0.0",
 				  "-pr",destDir.getAbsolutePath()
 				 };
 		performDirectCodeGen(testArgs, destDir);
 		
 		Constructor<?> constr = null;
 	    Class<?> cls = loadClass("com.ebay.marketplace.shipping.v1.services.calculatorservice.gen.SharedCalculatorServiceConsumer");
 		
 	    Thread.currentThread().setContextClassLoader(originalLoader);
 	    Method []  mtds = cls.getDeclaredMethods();
 
 		for(Constructor<?> c: cls.getDeclaredConstructors() ){
 		 if(c.getParameterTypes().length ==1){
 			 constr = c;
 		 }
 		}
 		Object constructorObj =null;
 		constructorObj = constr.newInstance(new String("dummy"));	
 			
 		for(Method m : mtds){
 			if(m.getName().equals("setServiceLocation")){
 				
 				 m.invoke(constructorObj,"http://localhost:8080/service/test");
 			}
 			}
 		for(Method m : mtds){
 		if(m.getName().equals("getServiceLocation")){
 			
 			URL serviceLocation = (URL) m.invoke(constructorObj);
 			Assert.assertEquals("http://localhost:8080/service/test",serviceLocation.toString());
 		}
 		}
 		
 		
 	}
 	
 	
 	@Test
 	@Ignore(" this case is not present")
 	public void testServiceLocationFromInvoker() throws Exception{
 		File wsdlFile = getProtobufRelatedInput("ShippingService.wsdl");
 		intfProps.put("sipp_version","1.2");
 		fillProperties(intfProps, intfProperty);
 		File binDir = new File(destDir,"bin");
 		
 		String [] testArgs = {"-serviceName","CalculatorService",
 				  "-mdest",destDir +"/meta-src",
 				  "-genType","ServiceFromWSDLIntf",
 				  "-wsdl",wsdlFile.getAbsolutePath(),
 				  "-gip","com.ebay.marketplace.shipping.v1.services",
 				  "-adminname","CalculatorService",
 				  "-slayer","BUSINESS",
 				  "-jdest",destDir +"/gen-src",
 				  "-namespace","http://www.ebay.com/marketplace/shipping/v1/services",
 				  "-dest",destDir.getAbsolutePath(),
 				  "-bin",binDir.getAbsolutePath(),
 				  "-slayer","INTERMEDIATE",
 				  "-scv","1.0.0",
 				  "-pr",destDir.getAbsolutePath()
 				 };
 		performDirectCodeGen(testArgs,binDir);
 		
 		
 		
 		Constructor<?> constr = null;
 	    Class<?> cls = loadClass("com.ebay.marketplace.shipping.v1.services.calculatorservice.gen.SharedCalculatorServiceConsumer");
 		
 	    Thread.currentThread().setContextClassLoader(originalLoader);
 	    
 	    Method []  mtds = cls.getDeclaredMethods();
 
 		for(Constructor<?> c: cls.getDeclaredConstructors() ){
 		 if(c.getParameterTypes().length ==1){
 			 constr = c;
 		 }
 		}
 		Object constructorObj =null;
 		constructorObj = constr.newInstance(new String("dummy"));	
 		Object obj = null;	
 		for(Method m : mtds){
 			if(m.getName().equals("getService")){
 				
 				 obj = m.invoke(constructorObj);
 			}
 		}
 		Class<?> cl = obj.getClass();
 		Method [] mtd = cl.getDeclaredMethods();
 		for(Method mt : mtd){
 			if(mt.getName().equals("getInvokerOptions")){
 				
 				 obj = mt.invoke(obj);
 			}
 		}
 		for(Method m : mtds){
 		if(m.getName().equals("getServiceLocation")){
 			
 			URL serviceLocation = (URL) m.invoke(obj);
 			Assert.assertEquals("http://localhost:8080/service/test",serviceLocation.toString());
 		}
 		}
 		
 		
 	}
 	
 	@Test
 	public void testChangesBefore12() throws Exception{
 		File wsdlFile = getProtobufRelatedInput("ShippingService.wsdl");
 		intfProps.put("sipp_version","1.1");
 		fillProperties(intfProps, intfProperty);
 	
 		
 		String [] testArgs = {"-serviceName","CalculatorService",
 				  "-mdest",destDir +"/meta-src",
 				  "-genType","ServiceFromWSDLIntf",
 				  "-wsdl",wsdlFile.getAbsolutePath(),
 				  "-gip","com.ebay.marketplace.shipping.v1.services",
 				  "-adminname","CalculatorService",
 				  "-slayer","BUSINESS",
 				  "-jdest",destDir +"/gen-src",
 				  "-namespace","http://www.ebay.com/marketplace/shipping/v1/services",
 				  "-dest",destDir.getAbsolutePath(),
 				  "-bin",destDir.getAbsolutePath(),
 				  "-slayer","INTERMEDIATE",
 				  "-scv","1.0.0",
 				  "-pr",destDir.getAbsolutePath()
 				 };
 		performDirectCodeGen(testArgs, destDir);
 		
 
 	    Class<?> cls = loadClass("com.ebay.marketplace.shipping.v1.services.calculatorservice.gen.SharedCalculatorServiceConsumer");
 		Method []  mtds = cls.getDeclaredMethods();
 		boolean present = true;
 		
 	
 		for(Method m : mtds){
 			
 			
 			if(m.getName().equals("setServiceLocation")){
 				
 				present = false;
 			
 			}
 			if(m.getName().equals("setAuthToken")){
 			
 				present = false;
 				
 			}
 			if(m.getName().equals("setCookies")){
 			
 				present = false;
 			}
 			
 			if(m.getName().equals("setHostName")){
 				
 				present = false;
 				
 			}
 			
 			
 		}
 		
 		Assert.assertFalse("Methods not present before 1.2 version is available",false);
 		
 	}	
 	@Test
 	@Ignore("proto compiler fails in linux machine.Ignoring till the issue is fixed")
 	public void testProtoEprotofilepath() throws Exception{
 		File wsdlFile = getProtobufRelatedInput("TestWsdl.wsdl");
 		intfProps.put("nonXSDFormats","protobuf");
 		fillProperties(intfProps, intfProperty);
 		
 		File binDir = new File(destDir,"bin");
 		String [] testArgs = {"-serviceName","CalculatorService",
 				  "-mdest",destDir +"/meta-src",
 				  "-genType","ServiceFromWSDLIntf",
 				  "-wsdl",wsdlFile.getAbsolutePath(),
 				  "-gip","com.ebay.marketplace.shipping.v1.services",
 				  "-adminname","CalculatorService",
 				  "-slayer","BUSINESS",
 				  "-jdest",destDir +"/gen-src/client",
 				  "-namespace","http://www.ebay.com/marketplace/shipping/v1/services",
 				  "-dest",destDir.getAbsolutePath(),
 				  "-bin",binDir.getAbsolutePath(),
 				  "-slayer","INTERMEDIATE",
 				  "-scv","1.0.0",
 				  "-enablednamespacefolding",
 				  "-pr",destDir.getAbsolutePath()
 				 };
 		performDirectCodeGen(testArgs, binDir);
 		
 		File protoFile = new File(destDir+"/meta-src/META-INF/soa/services/proto/CalculatorService/CalculatorService.proto");
 		Assert.assertTrue("File does not exist in " + protoFile.getAbsolutePath(), protoFile.exists());
 		//codegen.tools.soaframework.test.ebay.com
 		File jProtoFile = new File(destDir + "/gen-src/client/com/ebay/test/soaframework/tools/codegen/proto/CalculatorService.java");
 		Assert.assertTrue("File does not exist in " + jProtoFile.getAbsolutePath(), jProtoFile.exists());
 	
 		File eProtoFile = new File(destDir + "/gen-src/client/com/ebay/test/soaframework/tools/codegen/proto/extended/CalculatorService.java");
 		Assert.assertTrue("File does not exist in " + eProtoFile.getAbsolutePath(), jProtoFile.exists());
 	}
 	// admin name different from the service name
 	@Test
 	@Ignore("proto compiler fails in linux machine.Ignoring till the issue is fixed")
 	public void testProtoEprotofilepath2() throws Exception{
 		File wsdlFile = getProtobufRelatedInput("TestWsdl.wsdl");
 		intfProps.put("nonXSDFormats","protobuf");
 		fillProperties(intfProps, intfProperty);
 		File binDir = new File(destDir,"bin");
 		
 		String [] testArgs = {"-serviceName","CalculatorService",
 				  "-mdest",destDir +"/meta-src",
 				  "-genType","ServiceFromWSDLIntf",
 				  "-wsdl",wsdlFile.getAbsolutePath(),
 				  "-gip","com.ebay.marketplace.shipping.v1.services",
 				  "-adminname","CalculatorServiceV1",
 				  "-slayer","BUSINESS",
 				  "-jdest",destDir +"/gen-src/client",
 				  "-namespace","http://www.ebay.com/marketplace/shipping/v1/services",
 				  "-dest",destDir.getAbsolutePath(),
 				  "-bin",binDir.getAbsolutePath(),
 				  "-slayer","INTERMEDIATE",
 				  "-scv","1.0.0",
 				  "-enablednamespacefolding",
 				  "-pr",destDir.getAbsolutePath()
 				 };
 		performDirectCodeGen(testArgs, binDir);
 		
 		File protoFile = new File(destDir+"/meta-src/META-INF/soa/services/proto/CalculatorServiceV1/CalculatorServiceV1.proto");
 		Assert.assertTrue("File does not exist in " + protoFile.getAbsolutePath(), protoFile.exists());
 		
 		File jProtoFile = new File(destDir + "/gen-src/client/com/ebay/test/soaframework/tools/codegen/proto/CalculatorServiceV1.java");
 		Assert.assertTrue("File does not exist in " + jProtoFile.getAbsolutePath(), jProtoFile.exists());
 	
 		File eProtoFile = new File(destDir + "/gen-src/client/com/ebay/test/soaframework/tools/codegen/proto/extended/CalculatorServiceV1.java");
 		Assert.assertTrue("File does not exist in " + eProtoFile.getAbsolutePath(), jProtoFile.exists());
 	}
 	
 	
 	// change the package with ns2pkg option
 	@Test
 	@Ignore("proto compiler fails in linux machine.Ignoring till the issue is fixed")
 	public void testProtoEprotofilepath3() throws Exception{
 		File wsdlFile = getProtobufRelatedInput("TestWsdl.wsdl");
 		intfProps.put("nonXSDFormats","protobuf");
 		intfProps.put("ns2pkg","http://codegen.tools.soaframework.test.ebay.com=com.ebay.test.protobuf");
 		fillProperties(intfProps, intfProperty);
 		File binDir = new File(destDir,"bin");
 		
 		String [] testArgs = {"-serviceName","CalculatorService",
 				  "-mdest",destDir +"/meta-src",
 				  "-genType","ServiceFromWSDLIntf",
 				  "-wsdl",wsdlFile.getAbsolutePath(),
 				  "-gip","com.ebay.marketplace.shipping.v1.services",
 				  "-adminname","CalculatorServiceV1",
 				  "-slayer","BUSINESS",
 				  "-jdest",destDir +"/gen-src/client",
 				  "-namespace","http://www.ebay.com/marketplace/shipping/v1/services",
 				  "-dest",destDir.getAbsolutePath(),
 				  "-bin",binDir.getAbsolutePath(),
 				  "-slayer","INTERMEDIATE",
 				  "-scv","1.0.0",
 				  "-enablednamespacefolding",
 				  "-pr",destDir.getAbsolutePath()
 				 };
 		performDirectCodeGen(testArgs, binDir);
 		
 		File protoFile = new File(destDir+"/meta-src/META-INF/soa/services/proto/CalculatorServiceV1/CalculatorServiceV1.proto");
 		Assert.assertTrue("File does not exist in " + protoFile.getAbsolutePath(), protoFile.exists());
 		
 		File jProtoFile = new File(destDir + "/gen-src/client/com/ebay/test/protobuf/proto/CalculatorServiceV1.java");
 		Assert.assertTrue("File does not exist in " + jProtoFile.getAbsolutePath(), jProtoFile.exists());
 	
 		File eProtoFile = new File(destDir + "/gen-src/client/com/ebay/test/protobuf/proto/extended/CalculatorServiceV1.java");
 		Assert.assertTrue("File does not exist in " + eProtoFile.getAbsolutePath(), jProtoFile.exists());
 	}
 	
 	
 	@Test
 	public void testSupportZeroConfigProperty() throws Exception{
 		
 		
 		File wsdlFile = getProtobufRelatedInput("ShippingService.wsdl");
 		intfProps.put("support_zero_config","true");
 		fillProperties(intfProps, intfProperty);
 	
 		
 		String [] testArgs = {"-serviceName","CalculatorService",
 				  "-mdest",destDir +"/meta-src",
 				  "-genType","ServiceFromWSDLIntf",
 				  "-wsdl",wsdlFile.getAbsolutePath(),
 				  "-gip","com.ebay.marketplace.shipping.v1.services",
 				  "-adminname","CalculatorService",
 				  "-slayer","BUSINESS",
 				  "-jdest",destDir +"/gen-src",
 				  "-namespace","http://www.ebay.com/marketplace/shipping/v1/services",
 				  "-dest",destDir.getAbsolutePath(),
 				  "-bin",destDir.getAbsolutePath(),
 				  "-slayer","INTERMEDIATE",
 				  "-scv","1.0.0",
 				  "-pr",destDir.getAbsolutePath()
 				 };
 		performDirectCodeGen(testArgs, destDir);
 		
 		File metadata = new File(destDir+"/meta-src/META-INF/soa/common/config/CalculatorService/service_metadata.properties");
 		Properties prop = loadProperties(metadata);
 		Assert.assertEquals("true",prop.getProperty("support_zero_config"));
 		
 		
 	}	
 	
 	//test service config uses impl tag, if no intf property file is present. 
 	@Test
 	public void testImplFactory() throws Exception{
 		File wsdlFile = getProtobufRelatedInput("ShippingService.wsdl");
 		//File destDir = new File("generated");
 		
 	
 		
 		String [] testArgs = {"-serviceName","CalculatorService",
 				  "-mdest",destDir +"/meta-src",
 				  "-genType","ServerConfig",
 				  "-interface","com.ebay.marketplace.blogs.v1.services.suchservice.BlogsSuchServiceV1",
 				  "-scgn","MarketplaceServiceGroup",
 				  "-sicn","com.ebay.test.ServiceImpl",
 				  "-namespace","http://www.ebay.com/marketplace/blogs/v1/services",
 				  "-dest",destDir.getAbsolutePath(),
 				  "-bin",destDir.getAbsolutePath(),
 				  "-slayer","INTERMEDIATE",
 				  "-scv","1.0.0"
 				 };
 		
 		performDirectCodeGen(testArgs, destDir);
 		
 		
 		String path = destDir + "/meta-src/META-INF/soa/services/config/CalculatorService/ServiceConfig.xml";
 		 String xml = readFileAsString(path);
 		 Assert.assertTrue(xml.contains("<service-impl-class-name>com.ebay.test.ServiceImpl</service-impl-class-name>"));
 		
 		
 	}	
 	
 	//test service config uses impl factory tag, useExternalServiceFactory = true. 
 	@Test
 	public void testImplFactory2() throws Exception{
 		
 		String svc = "https://svcs.ebay.com/services/shipping/v1/ShippingService";
 		implProps.put("useExternalServiceFactory","true");
 		implProps.put("serviceImplFactoryClassName","dummyvalue");
 		
 		fillProperties(implProps, implProperty);
 		
 		intfProps.put("envMapper","org.ebayopensource.turmeric.tools.codegen.EnvironmentMapperImpl");
 		fillProperties(intfProps, intfProperty);
 		
 		File wsdlFile = getProtobufRelatedInput("ShippingService.wsdl");
 		
 	
 
 		String [] testArgs = {"-serviceName","CalculatorService",
 				  "-genType","ServiceFromWSDLIntf",
 				  "-wsdl",wsdlFile.getAbsolutePath(),
 				  "-gip","com.ebay.marketplace.shipping.v1.services",
 				  "-adminname","CalculatorService",
 				  "-slayer","BUSINESS",
 				  "-jdest",destDir.getAbsolutePath() +"/gen-src",
 				  
 				  "-namespace","http://www.ebay.com/marketplace/shipping/v1/services",
 				  "-dest",destDir.getAbsolutePath(),
 				  "-bin",destDir.getAbsolutePath(),
 				  "-slayer","INTERMEDIATE",
 				  "-envmapper","org.ebayopensource.turmeric.tools.codegen.EnvironmentMapperImpl",
 				  "-scv","1.0.0",
 				  "-pr",destDir.getAbsolutePath()
 				 };
 			
 			
 		String [] testArgs1 = {"-serviceName","CalculatorService",
 				  "-mdest",destDir.getAbsolutePath() +"/meta-src",
 				  "-genType","ServerConfig",
 				  "-interface","com.ebay.marketplace.shipping.v1.services.CalculatorService",
 				  "-scgn","MarketplaceServiceGroup",
 				 "-sicn","com.ebay.test.ServiceImpl",
 				  "-namespace","http://www.ebay.com/marketplace/blogs/v1/services",
 				  "-dest",destDir.getAbsolutePath(),
 				  "-bin",destDir.getAbsolutePath(),
 				  "-slayer","INTERMEDIATE",
 				  "-scv","1.0.0",
 				  "-pr",destDir.getAbsolutePath()
 				 };
 		
 			
 			
 		String [] testArgs2 = {"-serviceName","CalculatorService",
 				  "-mdest",destDir +"/meta-src",
 				  "-genType","ServiceFromWSDLImpl",
 				  "-interface","com.ebay.marketplace.shipping.v1.services.CalculatorService",
 				  "-scgn","MarketplaceServiceGroup",
 				  "-adminname","CalculatorService",
 				  "-environment","production",
 				  "-src",destDir.getAbsolutePath(),
 				  "-gt",
 				  "-jdest",destDir.getAbsolutePath() +"/gen-src",
 				  "-cn","BlogsSuchServiceV1_Test",
 				  "-sicn","com.ebay.test.ServiceImpl",
 				  "-namespace","http://www.ebay.com/marketplace/shipping/v1/services",
 				  "-dest",destDir.getAbsolutePath(),
 				  "-bin",destDir.getAbsolutePath(),
 				  "-slayer","INTERMEDIATE",
 				  "-scv","1.0.0",
 				  "-pr",destDir.getAbsolutePath()
 				 };
 		
 		performDirectCodeGen(testArgs, destDir);
 		performDirectCodeGen(testArgs1, destDir);
 		performDirectCodeGen(testArgs2, destDir);
 		
 		
 		
 		
 		String path = destDir + "/meta-src/META-INF/soa/services/config/CalculatorService/ServiceConfig.xml";
 		 String xml = readFileAsString(path);
 		 Assert.assertTrue(xml.contains("<service-impl-factory-class-name>dummyvalue</service-impl-factory-class-name>"));
 		
 		Properties metadata = new Properties();
 		File metaProps = new File(destDir +"/gen-meta-src/META-INF/soa/common/config/CalculatorService/service_metadata.properties");
 		FileInputStream in = new FileInputStream(metaProps);
 		metadata.load(in);
 		String svcLoc = (String) metadata.get("service_location");
 		
 		Assert.assertTrue("Service location is not what is present in wsdl",svc.equals(svcLoc));
 		
 	}
 	
 	public File copyToDir(File source,File dir){
 		
 		File dest = new File(dir,source.getName());
 		
 		try {
 			FileUtils.copyFile(source.getAbsolutePath(),dest.getAbsolutePath());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return dest;
 		
 	}
 	
 	@Test
 	public void testBug18551() throws Exception{
 		File wsdlFile = getProtobufRelatedInput("ShippingService.wsdl");
 		
 		
 		intfProps.put("interface_source_type","WSDL");
 		fillProperties(intfProps,intfProperty);
 		File wsdlPath = new File(destDir.getAbsolutePath()+"/meta-src/META-INF/soa/services/wsdl/ShippingService");
 		wsdlPath.mkdirs();
 		copyToDir(wsdlFile, wsdlPath);
 		
 		String [] testArgs = {"-serviceName","ShippingService",
 				  "-mdest",destDir.getAbsolutePath() +"/meta-src",
 				  "-genType","ServiceFromWSDLIntf",
 				  "-gip","com.ebay.intf.shipping",
 				  "-namespace","http://www.ebay.com/marketplace/shipping/v1/services",
 				  "-dest",destDir.getAbsolutePath(),
 				  "-bin",destDir.getAbsolutePath(),
 				  "-slayer","INTERMEDIATE",
 				  "-scv","1.0.0",
 				  "-pr",destDir.getAbsolutePath()
 				 };
 		
 	
 		try{
 			performDirectCodeGen(testArgs, destDir);
 		}catch(MissingInputOptionException  e){
 		 Assert.assertTrue(e.getMessage().contains("Pls check whether the service_intf_project.properties file exists in the project root and make sure the file contains the property interface_source_type"));
 		}
 	}	
 	
 	@Test
 	public void testImplFactory3() throws Exception{
 		
 		implProps.put("useExternalServiceFactory","falase");
 		implProps.put("serviceImplFactoryClassName","");
 		
 		fillProperties(implProps, implProperty);
 		
 		File wsdlFile = getProtobufRelatedInput("ShippingService.wsdl");
 	
 		
 
 		String [] testArgs = {"-serviceName","CalculatorService",
 				  "-mdest",destDir.getAbsolutePath() +"/meta-src",
 				  "-genType","ServiceFromWSDLIntf",
 				  "-wsdl",wsdlFile.getAbsolutePath(),
 				  "-gip","com.ebay.marketplace.shipping.v1.services",
 				  "-adminname","CalculatorService",
 				  "-slayer","BUSINESS",
 				  "-jdest",destDir.getAbsolutePath() +"/gen-src",
 				  "-sicn","com.ebay.test.ServiceImpl",
 				  "-namespace","http://www.ebay.com/marketplace/shipping/v1/services",
 				  "-dest",destDir.getAbsolutePath(),
 				  "-bin",destDir.getAbsolutePath(),
 				  "-slayer","INTERMEDIATE",
 				  "-scv","1.0.0",
 				  "-pr",destDir.getAbsolutePath()
 				 };
 			
 			
 		String [] testArgs1 = {"-serviceName","CalculatorService",
 				  "-mdest",destDir.getAbsolutePath() +"/meta-src",
 				  "-genType","ServerConfig",
 				  "-interface","com.ebay.marketplace.shipping.v1.services.CalculatorService",
 				  "-scgn","MarketplaceServiceGroup",
 				  "-sicn","com.ebay.test.ServiceImpl",
 				  "-namespace","http://www.ebay.com/marketplace/blogs/v1/services",
 				  "-dest",destDir.getAbsolutePath(),
 				  "-bin",destDir.getAbsolutePath(),
 				  "-slayer","INTERMEDIATE",
 				  "-scv","1.0.0",
 				  "-pr",destDir.getAbsolutePath()
 				 };
 			
 			
 			
 		String [] testArgs2 = {"-serviceName","CalculatorService",
 				  "-mdest",destDir.getAbsolutePath() +"/meta-src",
 				  "-genType","ServiceFromWSDLImpl",
 				  "-interface","com.ebay.marketplace.shipping.v1.services.CalculatorService",
 				  "-scgn","MarketplaceServiceGroup",
 				  "-adminname","CalculatorService",
 				  "-environment","production",
 				  "-gt",
 				  "-jdest",destDir.getAbsolutePath() +"/gen-src",
 				  "-cn","BlogsSuchServiceV1_Test",
 				  "-sicn","com.ebay.test.ServiceImpl",
 				  "-namespace","http://www.ebay.com/marketplace/shipping/v1/services",
 				  "-dest",destDir.getAbsolutePath(),
 				  "-bin",destDir.getAbsolutePath(),
 				  "-slayer","INTERMEDIATE",
 				  "-scv","1.0.0",
 				  "-pr",destDir.getAbsolutePath()
 				 };
 		
 		performDirectCodeGen(testArgs, destDir);
 		performDirectCodeGen(testArgs1, destDir);
 		performDirectCodeGen(testArgs2, destDir);
 		
 		String path = destDir + "/meta-src/META-INF/soa/services/config/CalculatorService/ServiceConfig.xml";
 		 String xml = readFileAsString(path);
 		 Assert.assertTrue(xml.contains("<service-impl-class-name>com.ebay.test.ServiceImpl</service-impl-class-name>"));
 		
 		
 	}
 	
 	@Test
 	public void testBaseConsumerForNoClientConfigChange() throws Exception{
 		intfProps.put("sipp_version","1.0");
 		fillProperties(intfProps,intfProperty);
 		File wsdlFile = getProtobufRelatedInput("TestWsdlComplexType.wsdl");
 		
 		
 		
 		String [] testArgs = {"-serviceName","FindingService",
 				  "-genType","Consumer",
 				  "-wsdl",wsdlFile.getAbsolutePath(),
 				  "-gip","com.ebay.marketplace.shipping.v1.services",
 				  "-adminname","FindingService",
 				  "-dest",destDir.getAbsolutePath(),
 				  "-bin",destDir.getAbsolutePath(),
 				  "-pr",destDir.getAbsolutePath()
 				 };
 		
 		
 		try{
 			performDirectCodeGen(testArgs, destDir);}
 		catch(Exception e){
 			e.printStackTrace();
 			
 		}
 			
 	}
 	
 	       
 	
 
 }
