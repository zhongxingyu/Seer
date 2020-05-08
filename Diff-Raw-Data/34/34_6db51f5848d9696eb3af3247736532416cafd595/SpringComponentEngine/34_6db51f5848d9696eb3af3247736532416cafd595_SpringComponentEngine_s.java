 package org.romaframework.core.component;
 
import java.io.File;
import java.util.List;
 import java.util.Map;
 
import org.romaframework.aspect.logging.LoggingHelper;
 import org.romaframework.core.Utility;
 import org.romaframework.core.config.AbstractServiceable;
 import org.romaframework.core.config.ContextException;
import org.romaframework.core.config.RomaApplicationContext;
 import org.romaframework.core.schema.SchemaAction;
 import org.romaframework.core.schema.SchemaClassDefinition;
 import org.romaframework.core.schema.SchemaEvent;
 import org.romaframework.core.schema.SchemaField;
import org.romaframework.core.util.FileUtils;
 import org.springframework.beans.BeansException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;
 
 @SuppressWarnings("unchecked")
 public class SpringComponentEngine extends AbstractServiceable implements ComponentAspect {
	private static final String					COMPONENT_SRV_DIRECTORY			= "/WEB-INF/classes/META-INF";
	private static final String					COMPONENT_SRV_FILE_PATTERN	= "applicationContext.*\\.xml";
 
	protected GenericApplicationContext	springContext;
 
 	public boolean existComponent(Class<? extends Object> iComponentClass) {
 		return springContext.containsBean(Utility.getClassName(iComponentClass));
 	}
 
 	public boolean existComponent(String iComponentName) {
 		return springContext.containsBean(iComponentName);
 	}
 
 	public <T> T getComponent(Class<T> iComponentClass) throws ContextException {
 		try {
 			return (T) springContext.getBean(Utility.getClassName(iComponentClass), iComponentClass);
 		} catch (BeansException e) {
 			throw new ContextException("Error on retrieving component '" + Utility.getClassName(iComponentClass) + "'", e);
 		}
 	}
 
 	public <T> T getComponent(String iComponentName) throws ContextException {
 		try {
 			return (T) springContext.getBean(iComponentName);
 		} catch (BeansException e) {
 			throw new ContextException("Error on retrieving component '" + iComponentName + "'", e);
 		}
 	}
 
 	public <T> Map<String, T> getComponentsOfClass(Class<T> iComponentClass) throws ContextException {
 		try {
 			return springContext.getBeansOfType(iComponentClass);
 		} catch (BeansException e) {
 			throw new ContextException("Error on retrieving components of class '" + iComponentClass + "'", e);
 		}
 	}
 
 	/**
 	 * Load all Spring configuration files and start the context.
 	 */
 	public void startup() throws RuntimeException {
 		status = STATUS_STARTING;
 
		springContext = new GenericApplicationContext();
		XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(springContext);

		List<File> cfgFiles = FileUtils.searchAllFiles(RomaApplicationContext.getApplicationPath() + COMPONENT_SRV_DIRECTORY, COMPONENT_SRV_FILE_PATTERN, true);

		if (cfgFiles == null || cfgFiles.size() == 0)
			LoggingHelper.raiseCfgException(getClass(), "Error on loading configuration from path: " + RomaApplicationContext.getApplicationPath()
					+ COMPONENT_SRV_DIRECTORY + ". No files found.");

		for (File f : cfgFiles) {
			xmlReader.loadBeanDefinitions(new FileSystemResource(f));
		}
 
 		springContext.refresh();
 		status = STATUS_UP;
 	}
 
 	public void shutdown() throws RuntimeException {
 		status = STATUS_SHUTDOWNING;
 
 		if (springContext != null) {
 			springContext.destroy();
 		}
 
 		status = STATUS_DOWN;
 	}
 
 	public String aspectName() {
 		return ASPECT_NAME;
 	}
 
 	public void beginConfigClass(SchemaClassDefinition iClass) {
 	}
 
 	public void endConfigClass(SchemaClassDefinition iClass) {
 	}
 
 	public void configAction(SchemaAction action) {
 	}
 
 	public void configClass(SchemaClassDefinition class1) {
 	}
 
 	public void configEvent(SchemaEvent event) {
 	}
 
 	public void configField(SchemaField field) {
 	}
 
 	public Object getUnderlyingComponent() {
 		return springContext;
 	}
 }
