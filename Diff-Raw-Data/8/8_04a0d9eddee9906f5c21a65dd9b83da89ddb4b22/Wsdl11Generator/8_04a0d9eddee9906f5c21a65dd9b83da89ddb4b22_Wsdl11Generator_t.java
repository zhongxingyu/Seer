 package org.codehaus.xfire.gen;
 
 import java.io.File;
 import java.util.Iterator;
 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 import org.codehaus.xfire.gen.jsr181.Jsr181Profile;
 import org.codehaus.xfire.util.ClassLoaderUtils;
 import org.codehaus.xfire.wsdl11.parser.WSDLServiceBuilder;
 
 import com.sun.codemodel.JCodeModel;
 
 /**
  * A bean type class which generates client and server stubs from a wsdl.
  * A simple invocation goes like so:
  * <pre>
  * Wsdl11Generator gen = new Wsdl11Generator();
  * gen.setWsdl("src/wsdl/service.wsdl");
  * gen.setOutputDirectory("target/generated-source");
  * gen.generate();
  * </pre>
  * @author Dan Diephouse
  */
 public class Wsdl11Generator
 {
    private static final Log log = LogFactory.getLog(Wsdl11Generator.class);
    
     public static final String JAXB = "jaxb";
     public static final String XMLBEANS = "xmlbeans";
 
     private String wsdl;
     private String baseURI;
     private String outputDirectory;
     private String destinationPackage;
     
     private JCodeModel codeModel = new JCodeModel();
     
     private String profile;
     private String binding = JAXB;
     private SchemaSupport support;
     
     @SuppressWarnings("unchecked")
     public void generate() throws Exception
     {
         File dest = new File(outputDirectory);
         if (!dest.exists()) dest.mkdirs();
 
         if (support == null)
         {
             if (binding.equals(JAXB))
             {
                 support = loadSupport("org.codehaus.xfire.gen.jaxb.JAXBSchemaSupport");
             }
             else if (binding.equals(XMLBEANS))
             {
                 support = loadSupport("org.codehaus.xfire.gen.xmlbeans.XmlBeansSchemaSupport");
             }
             else
             {
                 throw new Exception("Illegal binding: " + binding);
             }
         }
 
         String wsdlUri = wsdl;
         File wsdlFile = new File(wsdl);
         if (wsdlFile.exists()) 
             wsdlUri = wsdlFile.toURI().toString();
 
         if (baseURI == null)
         {
             baseURI = wsdlUri;
         }
         else if (new File(baseURI).exists())
         {
             baseURI = new File(baseURI).toURI().toString();
         }
 
        log.info("Generating code for WSDL at " + wsdlUri + " with a base URI of " + baseURI);
        
         WSDLServiceBuilder builder = new WSDLServiceBuilder(baseURI, new WSDLInputStreamLoader().getInputStream(wsdlUri));
         builder.setBindingProvider(support.getBindingProvider());
         builder.build();
         
         if (profile == null) profile = Jsr181Profile.class.getName();
         PluginProfile profileObj = 
             (PluginProfile) ClassLoaderUtils.loadClass(profile, getClass()).newInstance();
         
         GenerationContext context = new GenerationContext(codeModel, builder.getDefinition());
         context.setOutputDirectory(dest);
         context.setWsdlLocation(wsdlUri);
         context.setBaseURI(baseURI);
         context.setSchemas(builder.getSchemas());
         
         support.initialize(context);
 
         context.setServices(builder.getServices());
         context.setDestinationPackage(getDestinationPackage());
         context.setSchemaGenerator(support);
         
         for (Iterator<GeneratorPlugin> pitr = profileObj.getPlugins().iterator(); pitr.hasNext();)
         {
             GeneratorPlugin plugin = pitr.next();
             
             plugin.generate(context);
         }
 
         // Write the code!
         codeModel.build(dest);
     }
   
     private SchemaSupport loadSupport(String name) throws Exception
     {
         return (SchemaSupport) ClassLoaderUtils.loadClass(name, getClass()).newInstance();
     }
 
     public SchemaSupport getSchemaSupport()
     {
         return support;
     }
 
     public void setSchemaSupport(SchemaSupport support)
     {
         this.support = support;
     }
 
     public String getOutputDirectory()
     {
         return outputDirectory;
     }
 
     public void setOutputDirectory(String outputDirectory)
     {
         this.outputDirectory = outputDirectory;
     }
 
     public JCodeModel getCodeModel()
     {
         return codeModel;
     }
 
     public String getBaseURI()
     {
         return baseURI;
     }
 
     public void setBaseURI(String baseURI)
     {
         this.baseURI = baseURI;
     }
 
     public String getWsdl()
     {
         return wsdl;
     }
 
     public void setWsdl(String wsdl)
     {
         this.wsdl = wsdl;
     }
 
     public String getDestinationPackage()
     {
         return destinationPackage;
     }
 
     public void setDestinationPackage(String destinationPackage)
     {
         this.destinationPackage = destinationPackage;
     }
 
     public String getBinding()
     {
         return binding;
     }
 
     public void setBinding(String binding)
     {
         this.binding = binding;
     }
 
     public String getProfile()
     {
         return profile;
     }
 
     public void setProfile(String profile)
     {
         this.profile = profile;
     }
 }
