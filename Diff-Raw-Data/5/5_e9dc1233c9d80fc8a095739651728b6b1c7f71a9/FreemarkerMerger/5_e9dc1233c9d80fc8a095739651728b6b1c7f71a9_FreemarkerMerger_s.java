 package com.dianping.wizard.widget.merger;
 
 import com.dianping.wizard.config.Configuration;
 import com.dianping.wizard.exception.WidgetException;
 import freemarker.cache.StringTemplateLoader;
 import freemarker.template.ObjectWrapper;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.Map;
 import java.util.Properties;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 /**
  * @author ltebean
  */
 public class FreemarkerMerger implements Merger {
 
     private static final FreemarkerMerger instance = new FreemarkerMerger();
 
     private final freemarker.template.Configuration cfg;
 
     private final ConcurrentMap<String, TemplatePack> cache = new ConcurrentHashMap<String, TemplatePack>();
 
     public static FreemarkerMerger getInstance() {
         return instance;
     }
 
     private FreemarkerMerger() {
         cfg = new freemarker.template.Configuration();
         Properties properties = new Properties();
         String freemarkerProperties = Configuration.get("freemarker.properties", String.class);
         if (StringUtils.isNotEmpty(freemarkerProperties)) {
             try {
                 properties.load(this.getClass().getClassLoader()
                         .getResourceAsStream(freemarkerProperties));
                 cfg.setSettings(properties);
             } catch (Exception e) {
                 throw new WidgetException("freemarker settings error", e);
             }
         }
 
         StringTemplateLoader stringLoader = new StringTemplateLoader();
         cfg.setTemplateLoader(stringLoader);
         cfg.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);
     }
 
     public String merge(Template template, Map<String, Object> context) throws Exception {
         TemplatePack templatePack = getTemplatePackAndUpdateCache(template);
         StringWriter writer = new StringWriter();
         templatePack.compiledTemplate.process(context, writer);
         return writer.getBuffer().toString();
     }
 
     private TemplatePack getTemplatePackAndUpdateCache(Template template) throws Exception{
         TemplatePack templatePack = cache.get(template.name);
         if (templatePack == null) {
             freemarker.template.Template fmTemplate = new freemarker.template.Template(template.name, new StringReader(template.code), cfg);
            TemplatePack pack= new TemplatePack(template.name,fmTemplate);
             cache.putIfAbsent(template.name,pack);
             return pack;
         }
         if(!templatePack.code.equals(template.code)){
             freemarker.template.Template fmTemplate = new freemarker.template.Template(template.name, new StringReader(template.code), cfg);
            TemplatePack pack=new TemplatePack(template.name,fmTemplate);
             cache.put(template.name, pack);
             return pack;
         }
         return templatePack;
     }
 
     private static class TemplatePack{
 
         public final String code;
 
         public final freemarker.template.Template compiledTemplate;
 
         private TemplatePack(String code, freemarker.template.Template compiledTemplate) {
             this.code = code;
             this.compiledTemplate = compiledTemplate;
         }
     }
 }
