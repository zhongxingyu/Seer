 package com.slyak.cms.core.support;
 
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import javax.persistence.PersistenceException;
 import javax.servlet.ServletContext;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.support.DefaultListableBeanFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.context.ConfigurableApplicationContext;
 import org.springframework.context.event.ContextRefreshedEvent;
 import org.springframework.core.annotation.AnnotationUtils;
 import org.springframework.core.io.Resource;
 import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
 import org.springframework.core.io.support.ResourcePatternResolver;
 import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
 import org.springframework.core.type.classreading.MetadataReader;
 import org.springframework.core.type.classreading.MetadataReaderFactory;
 import org.springframework.core.type.filter.AnnotationTypeFilter;
 import org.springframework.core.type.filter.TypeFilter;
 import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;
 import org.springframework.util.ClassUtils;
 import org.springframework.web.HttpRequestHandler;
 import org.springframework.web.context.ServletContextAware;
 import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
 import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
 
 import com.slyak.cms.core.annotation.Setting;
 import com.slyak.cms.core.annotation.Widget;
 import com.slyak.cms.core.annotation.Widgets;
 import com.slyak.cms.core.enums.InputType;
 
 import freemarker.template.Configuration;
 import freemarker.template.TemplateException;
 
 public class SimpleWidgetManager implements WidgetManager,ApplicationContextAware,ServletContextAware,InitializingBean{
 	
 	private static final Map<String,Map<String,WidgetInfo>> WIDGET_INFOS = new HashMap<String, Map<String,WidgetInfo>>();
 	private static final Map<String,Object> REGION_HANDLERS = new HashMap<String,Object>();
 	
 	private String[] packagesToScan;
 	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
 
 	private ApplicationContext applicationContext;
 	private ServletContext servletContext;
 	
 	private static final String WIDGET_CLASS_RESOURCE_PATTERN = "/**/*.class";
 	
 	private static final TypeFilter[] widgetTypeFilters = new TypeFilter[] {new AnnotationTypeFilter(Widgets.class, false)};
 	
 	private static final Map<Object,Configuration> HANLDER_CONFIGURATIONS = new HashMap<Object, Configuration>();
 	
 	private static final String WIDGETS_ROOT="classpath:com/slyak/cms/widgets";
 	
 	private String staticResourceLocation = "static/";
 	
 	public void setStaticResourceLocation(String staticResourceLocation) {
 		this.staticResourceLocation = staticResourceLocation;
 	}
 
 	public void setPackagesToScan(String... packagesToScan) {
 		this.packagesToScan = packagesToScan;
 	}
 	
 	private void buildWidgetInfos() throws BeansException, ClassNotFoundException, TemplateException{
 		for (String pkg : this.packagesToScan) {
 			try {
 				String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
 						ClassUtils.convertClassNameToResourcePath(pkg) + WIDGET_CLASS_RESOURCE_PATTERN;
 				Resource[] resources = this.resourcePatternResolver.getResources(pattern);
 				MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);
 				
 				SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
 				Map<String, HttpRequestHandler> urlMap = new LinkedHashMap<String, HttpRequestHandler>();
 				
 				for (Resource resource : resources) {
 					if (resource.isReadable()) {
 						MetadataReader reader = readerFactory.getMetadataReader(resource);
 						if (matchesFilter(reader, readerFactory)) {
 							String className = reader.getClassMetadata().getClassName();
 							Class<?> clazz = Class.forName(className);
 							
 							Widgets widgets = AnnotationUtils.findAnnotation(clazz, Widgets.class);
 							String region = widgets.value();
 							Map<String,WidgetInfo> regionMap = new HashMap<String, WidgetInfo>();
 							WIDGET_INFOS.put(region, regionMap);
 							
 							Object handler = applicationContext.getBean(clazz);
 							REGION_HANDLERS.put(region, handler);
 							
 							Method[] methods = handler.getClass().getDeclaredMethods();
 							
 							Package _pkg =handler.getClass().getPackage();
 							String widgetRoot = "classpath:"+_pkg.getName().replace('.', '/');
 							
 							//freemarker configuration
 							FreeMarkerConfigurationFactoryBean fcfb = new FreeMarkerConfigurationFactoryBean();
 							fcfb.setTemplateLoaderPaths(widgetRoot,WIDGETS_ROOT);
 							fcfb.setDefaultEncoding("UTF-8");
 							Configuration cfg = fcfb.createConfiguration();
 							HANLDER_CONFIGURATIONS.put(handler, cfg);
 							
 							//init static resources
 							ResourceHttpRequestHandler requestHandler = new ResourceHttpRequestHandler();
 							requestHandler.setLocations(Collections.singletonList(applicationContext.getResource(widgetRoot+"/"+staticResourceLocation)));
 							requestHandler.setCacheSeconds(31556926);
 							requestHandler.setServletContext(servletContext);
 							requestHandler.setApplicationContext(applicationContext);
 							urlMap.put("/widgetResource/"+region+"/**", requestHandler);
 							
 							for (Method m : methods) {
 								Widget widget = AnnotationUtils.getAnnotation(m, Widget.class);
 								if(widget!=null){
 									WidgetInfo info = new WidgetInfo();
 									info.setHandler(handler);
 									info.setMethod(m);
 									info.setName(m.getName());
 									info.setRegion(region);
 									if(widget.settings()!=null){
 										for (Setting setting : widget.settings()) {
 											com.slyak.cms.core.support.Setting st = new com.slyak.cms.core.support.Setting();
 											st.setKey(setting.key());
 											st.setInputType(setting.inputType());
 											st.setOptions(setting.options());
 											String[] options = setting.options();
 											String optionLoader = StringUtils.trimToEmpty(setting.optionsLoader());
 											boolean hasOptionLoader = !StringUtils.isEmpty(optionLoader);
 											if(hasOptionLoader||(options.length>1&&(setting.inputType()!=InputType.RADIO||setting.inputType()!=InputType.CHECKBOX))){
 												st.setInputType(InputType.SELECT);
 											}else{
 												st.setInputType(setting.inputType());
 											}
 											if(hasOptionLoader){
 												st.setOptionsLoader(com.slyak.cms.core.support.ClassUtils.getMethodByName(clazz, optionLoader));
 											}
 											st.setOptions(options);
 											st.setValue(setting.value());
 											info.addSetting(st);
 										}
 									}
 									
 									info.setShow(widget.show());
 									
 									if(StringUtils.isNotBlank(widget.onAdd())){
 										info.setOnAdd(com.slyak.cms.core.support.ClassUtils.getMethodByName(clazz, widget.onAdd()));
 									}
 									
 									if(StringUtils.isNotBlank(widget.onEdit())){
 										info.setOnEdit(com.slyak.cms.core.support.ClassUtils.getMethodByName(clazz, widget.onEdit()));
 									}
 									
 									if(StringUtils.isNotBlank(widget.onRemove())){
 										info.setOnRemove(com.slyak.cms.core.support.ClassUtils.getMethodByName(clazz, widget.onRemove()));
 									}
 									
 									regionMap.put(info.getName(),info);
 								}
 							}
 						}
 					}
 				}
 				
 				handlerMapping.setUrlMap(urlMap);
 				applicationContext.getAutowireCapableBeanFactory().initializeBean(handlerMapping, null);
 				
 				ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;  
 				DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();
 				defaultListableBeanFactory.registerSingleton("widgetResourceHandlerMapping", handlerMapping);
 				
 				//refresh context
 				applicationContext.publishEvent(new ContextRefreshedEvent(applicationContext));
 				
 			}
 			catch (IOException ex) {
 				throw new PersistenceException("Failed to scan classpath for unlisted classes", ex);
 			}
 		}
 	}
 	
 	public class Hello{
 		
 	}
 	
 	/**
 	 * Check whether any of the configured entity type filters matches
 	 * the current class descriptor contained in the metadata reader.
 	 */
 	private boolean matchesFilter(MetadataReader reader, MetadataReaderFactory readerFactory) throws IOException {
 		for (TypeFilter filter : widgetTypeFilters) {
 			if (filter.match(reader, readerFactory)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		buildWidgetInfos();
 	}
 
 	@Override
 	public void setApplicationContext(ApplicationContext applicationContext)
 			throws BeansException {
 		this.applicationContext = applicationContext;
 	}
 
 	@Override
 	public WidgetInfo getWidgetInfo(String region,String widgetName) {
 		return WIDGET_INFOS.get(region).get(widgetName);
 	}
 
 	@Override
 	public Map<String,Map<String,WidgetInfo>> getWidgetInfos() {
 		return WIDGET_INFOS;
 	}
 
 	@Override
 	public Configuration getConfiguration(Object handler) {
 		return HANLDER_CONFIGURATIONS.get(handler);
 	}
 
 	@Override
 	public void addRemoteWidgets(URL... urls) {
 		
 	}
 
 	@Override
 	public Object getRegionHandler(String region) {
 		if(region==null){
 			return  REGION_HANDLERS;
 		}else{
 			return REGION_HANDLERS.get(region);
 		}
 	}
 	
 	@Override
 	public void setServletContext(ServletContext servletContext) {
 		this.servletContext = servletContext;
 	}
 	
 }
