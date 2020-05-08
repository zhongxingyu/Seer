 package org.codehaus.xfire.gen.jaxb;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Field;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Map.Entry;
 
 import javax.xml.namespace.QName;
 
 import org.codehaus.xfire.aegis.AegisBindingProvider;
 import org.codehaus.xfire.gen.GenerationContext;
 import org.codehaus.xfire.gen.GenerationException;
 import org.codehaus.xfire.gen.SchemaSupport;
 import org.codehaus.xfire.jaxb2.JaxbServiceFactory;
 import org.codehaus.xfire.jaxb2.JaxbTypeRegistry;
 import org.codehaus.xfire.service.binding.BindingProvider;
 import org.codehaus.xfire.wsdl11.parser.SchemaInfo;
 import org.jdom.Element;
 import org.xml.sax.EntityResolver;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import com.sun.codemodel.JCodeModel;
 import com.sun.codemodel.JExpr;
 import com.sun.codemodel.JExpression;
 import com.sun.codemodel.JInvocation;
 import com.sun.codemodel.JType;
 import com.sun.tools.xjc.Options;
 import com.sun.tools.xjc.api.Mapping;
 import com.sun.tools.xjc.api.S2JJAXBModel;
 import com.sun.tools.xjc.api.SchemaCompiler;
 import com.sun.tools.xjc.api.TypeAndAnnotation;
 import com.sun.tools.xjc.api.XJC;
 
 public class JAXBSchemaSupport implements SchemaSupport
 {
     SchemaCompiler schemaCompiler;
     
     private AegisBindingProvider provider = new AegisBindingProvider(new JaxbTypeRegistry());
 
     private S2JJAXBModel model;
     private JCodeModel jaxbModel;
     
     public void initialize(GenerationContext context) throws Exception
     {
         // Gross hack to get around NPE
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         Thread.currentThread().setContextClassLoader(new URLClassLoader(new URL[0], cl));
 
         ErrorReceiverImpl er = new ErrorReceiverImpl();
 
         schemaCompiler = XJC.createSchemaCompiler();
         schemaCompiler.setErrorListener(er);
         
         if (context.isExplicitAnnotation()) 
         {
 	        try
 	        {
 	            Field f = schemaCompiler.getClass().getDeclaredField("opts");
 	            f.setAccessible(true);
 	            Options opts = (Options) f.get(schemaCompiler);
 	            opts.runtime14 = true;
 	        } 
 	        catch( Exception e )
 	        {
 	            System.err.println("Could not set JDK 1.4 compatibility");
 	            // Continue
 	        }
         }
         
         ArrayList<SchemaInfo> elements = (ArrayList<SchemaInfo>) context.getSchemas();
 
         int schemaElementCount = 1;
         for(SchemaInfo schema : elements)
         {
             schemaCompiler.setEntityResolver(new RelativeEntityResolver(schema.getDefinition().getDocumentBaseURI()));
             
             String systemId = schema.getSchema().getSourceURI();
             if (!schema.isImported())
             {
                systemId += "#types?schema"+schemaElementCount++;
             }
             
             schemaCompiler.parseSchema(systemId, schema.getSchemaElement());
         }
         
         if (context.getExternalBindings() != null)
         {
             for (Entry<String, InputStream> e : context.getExternalBindings().entrySet())
             {
                 InputSource source = new InputSource(e.getValue());
                 source.setSystemId(e.getKey());
                 schemaCompiler.parseSchema(source);
             }
         }
         
         model = schemaCompiler.bind();
 
         if (er.hasFatalErrors()) throw new GenerationException("Error generating JAXB model.");
 
         jaxbModel = model.generateCode(null, er);
         jaxbModel.build(context.getOutputDirectory());
 
         Thread.currentThread().setContextClassLoader(cl);
     }
     
     public JType getType(GenerationContext context, QName concreteType, QName schemaType) 
         throws GenerationException
     {
         JCodeModel codeModel = context.getCodeModel();
 
         Mapping mapping = model.get(concreteType);
 
         if (mapping == null)
         {
             mapping = model.get(schemaType);
         }
         
         JType typeClass = null;
         
         if (mapping != null)
         {
             typeClass = mapping.getType().getTypeClass();
         }
         
         if (typeClass == null)
         {
         	TypeAndAnnotation def = model.getJavaType(concreteType);
         	if(def == null)
         	{
         		def = model.getJavaType(schemaType);
         	}
         	if(def != null)
         	{
         		return def.getTypeClass();
         	}
         }
         
         if (typeClass == null)
         {
             Class clazz = provider.getTypeClass(concreteType, null);
             if (clazz == null)
             {
                 clazz = provider.getTypeClass(schemaType, null);
             }
             
             if (clazz == null)
             {
                 throw new GenerationException("Could not find type " + concreteType);
             }
             
             typeClass = codeModel._ref(clazz);
         }
         
         return typeClass;
     }
 
     public BindingProvider getBindingProvider()
     {
         return provider;
     }
 
     public JExpression getBindingProviderExpr(GenerationContext context)
     {
         JType aegis = context.getCodeModel()._ref(AegisBindingProvider.class);
         JInvocation expr = JExpr._new(aegis);
         
         JType xbean = context.getCodeModel()._ref(JaxbTypeRegistry.class);
         expr.arg(JExpr._new(xbean));
         
         return expr;
     }
     
     /**
      * Resolves Schemas relative to the WSDL document they're included in.
      * @author Dan Diephouse
      */
     static class RelativeEntityResolver  implements EntityResolver
     {
         private String baseURI;
 
         public RelativeEntityResolver(String baseURI)
         {
             super();
             this.baseURI = baseURI;
         }
 
         public InputSource resolveEntity(String publicId, String systemId)
             throws SAXException, IOException
         {
            File file = new File(baseURI, systemId);
            if (file.exists())
             {
                return new InputSource(new FileInputStream(file));
             }
           
             return null;
         }
     }
 
     public String getServiceFactory()
     {
         return JaxbServiceFactory.class.getName();
     }
     
     public Element getServiceFactoryBean()
     {
         return null;
     }
 }
