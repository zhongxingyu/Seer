  package xingu.template.impl.groovy;
 
 import groovy.lang.Writable;
 import groovy.text.SimpleTemplateEngine;
 import groovy.text.Template;
 
 import java.io.File;
 import java.io.Writer;
 
 import org.apache.avalon.framework.activity.Initializable;
 import org.apache.avalon.framework.configuration.Configurable;
 import org.apache.avalon.framework.configuration.Configuration;
 import org.apache.avalon.framework.configuration.ConfigurationException;
 
 import xingu.template.Context;
 import xingu.template.TemplateEngine;
 import xingu.template.TemplateEngineException;
 import xingu.template.impl.TemplateEngineSupport;
import br.com.ibnetwork.xingu.utils.FSUtils;
 
 public class GroovyTemplateEngine
 	extends TemplateEngineSupport
 	implements TemplateEngine, Configurable, Initializable
 {
 	private SimpleTemplateEngine ge;
 	
 	private String templateRoot;
 	
 	@Override
     protected String getDefaultConfigurationFile()
     {
 	    return null;
     }
 
 	@Override
     protected String getDefaultExtension()
     {
 		return ".gtl";
     }
 
 	public void configure(Configuration conf) 
 		throws ConfigurationException
 	{
 		super.configure(conf);
 		templateRoot = conf.getChild("templates").getChild("root").getValue();
 		templateRoot = env.replaceVars(templateRoot);
 		templateRoot = FSUtils.load(templateRoot);
 	}
 
 	public void initialize() 
 		throws Exception
     {
 		ge = new SimpleTemplateEngine(/* true */);
     }
 	
 	public void merge(String templateName, Context context, Writer writer) 
 		throws TemplateEngineException
     {
         String realName = toFileName(templateName);
         if(log.isDebugEnabled()) log.debug("rendering [" + realName + "]");		
 
 		try
         {
 			File fileToRender = getFileToRender(realName);
 	        Template template = ge.createTemplate(fileToRender);
 	        Writable writable = template.make(context.getMap());
 	        writable.writeTo(writer);
         }
         catch (Exception e)
         {
         	throw new TemplateEngineException("Error merging template["+realName+"] ", e);
         }
     }
 
 	private File getFileToRender(String realName)
     {
 		return new File(templateRoot+File.separator+realName);
     }
 
 	public boolean templateExists(String templateName) 
 		throws TemplateEngineException
     {
 	    String realName = toFileName(templateName);
 	    File file = getFileToRender(realName);
 		return file.exists();
     }
 }
