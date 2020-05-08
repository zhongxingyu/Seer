 package org.codehaus.xfire.generator.imports;
 
 
 import org.codehaus.xfire.gen.Wsdl11Generator;
 import org.codehaus.xfire.generator.GenerationTestSupport;
 
 import com.sun.codemodel.JCodeModel;
 import com.sun.codemodel.JDefinedClass;
 
 public class WsdlImportTest
     extends GenerationTestSupport
 {
    public void testXFIRE503() throws Exception
    {
        Wsdl11Generator generator = new Wsdl11Generator();
        generator.setWsdl(getTestFilePath("src/wsdl/wsdl-xfire-503.wsdl"));
        generator.setOutputDirectory(getTestFilePath("target/test-services"));
        generator.setDestinationPackage("org.codehaus.xfire.generator.imports");
        generator.setOverwrite(true);
        
        generator.generate();
        
        JCodeModel model = generator.getCodeModel();
        JDefinedClass soap = model._getClass("org.codehaus.xfire.generator.imports.MyServiceSoap");
        assertNotNull(soap);
    }
    
    public void testImports2() throws Exception
     {
         Wsdl11Generator generator = new Wsdl11Generator();
         generator.setWsdl(getTestFilePath("src/wsdl/import-test/main.wsdl"));
         generator.setOutputDirectory(getTestFilePath("target/test-services"));
         generator.setDestinationPackage("org.codehaus.xfire.generator.imports");
         generator.setBinding("jaxb");
         generator.setBaseURI(getTestFilePath("src/wsdl/import-test/"));
         generator.setOverwrite(true);
         
         generator.generate();
         
         JCodeModel model = generator.getCodeModel();
         JDefinedClass echo = model._getClass("org.codehaus.xfire.generator.imports.EchoPortType");
         assertNotNull(echo);
     }
 }
