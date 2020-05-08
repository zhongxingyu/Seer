 package org.mvnsearch.matrix.template;
 
 import com.github.jknack.handlebars.Handlebars;
 import com.github.jknack.handlebars.Template;
 import freemarker.template.Configuration;
 import freemarker.template.DefaultObjectWrapper;
 import junit.framework.TestCase;
 import org.apache.commons.io.IOUtils;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.Velocity;
 import org.apache.velocity.app.VelocityEngine;
 
 import java.io.File;
 import java.io.StringWriter;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 /**
  * template matrix test
  *
  * @author linux_china
  */
 public class TempateMatrixTest extends TestCase {
     /**
      * handlebars template
      */
     private Template handlebarsTemplate = null;
     /**
      * velocity template
      */
     private org.apache.velocity.Template velocityTemplate = null;
     /**
      * freemarker template
      */
     private freemarker.template.Template freemarkerTemplate = null;
 
     /**
      * init logic
      *
      * @throws Exception exception
      */
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         //init handlebars
         String templateContent = IOUtils.toString(this.getClass().getResourceAsStream("/template/demo.hbs"));
         Handlebars handlebars = new Handlebars();
        this.handlebarsTemplate = handlebars.compileInline(templateContent);
         //velocity
         Velocity.init();
         Properties p = new Properties();
         p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
         p.setProperty("resource.loader", "class");
         p.setProperty("input.encoding", "UTF-8");
         p.setProperty("output.encoding", "UTF-8");
         VelocityEngine engine = new VelocityEngine(p);
         this.velocityTemplate = engine.getTemplate("/template/demo.vm");
         //freemarker
         Configuration cfg = new Configuration();
         cfg.setClassForTemplateLoading(this.getClass(), "/");
         cfg.setObjectWrapper(new DefaultObjectWrapper());
         this.freemarkerTemplate = cfg.getTemplate("template/demo.ftl");
     }
 
     /**
      * spike test
      *
      * @throws Exception exception
      */
     public void testSpike() throws Exception {
         Map<String, Object> context = constructContext();
         VelocityContext context2 = new VelocityContext(context);
         System.out.println(freemarker(context));
     }
 
     /**
      * matrix test
      *
      * @throws Exception exception
      */
     public void testMatrix() throws Exception {
         int count = 100000;
         Map<String, Object> context = constructContext();
         VelocityContext context2 = new VelocityContext(context);
         //hot swap
         for (int i = 0; i < 10000; i++) {
             handlebars(context);
             velocity(context2);
             freemarker(context);
         }
        System.out.println(count + " loop");
         long start = System.currentTimeMillis();
         for (int i = 0; i < count; i++) {
             handlebars(context);
         }
         long end1 = System.currentTimeMillis();
         System.out.println("handlebars:" + (end1 - start));
         for (int i = 0; i < count; i++) {
             velocity(context2);
         }
         long end2 = System.currentTimeMillis();
         System.out.println("velocity:" + (end2 - end1));
         for (int i = 0; i < count; i++) {
             freemarker(context);
         }
         long end3 = System.currentTimeMillis();
         System.out.println("freemarker:" + (end3 - end2));
     }
 
 
     /**
      * handlebars render
      *
      * @param context context
      * @return output
      * @throws Exception exception
      */
     private String handlebars(Map<String, Object> context) throws Exception {
         return handlebarsTemplate.apply(context);
     }
 
     /**
      * velocity render
      *
      * @param context context
      * @return output
      * @throws Exception exception
      */
     private String velocity(VelocityContext context) throws Exception {
         StringWriter writer = new StringWriter();
         velocityTemplate.merge(context, writer);
         return writer.toString();
     }
 
     /**
      * freemarker render
      *
      * @param context context
      * @return output
      * @throws Exception exception
      */
     private String freemarker(Map<String, Object> context) throws Exception {
         StringWriter writer = new StringWriter();
         freemarkerTemplate.process(context, writer);
         return writer.toString();
     }
 
     /**
      * construct context
      *
      * @return context
      */
     private Map<String, Object> constructContext() {
         Map<String, Object> context = new HashMap<String, Object>();
         context.put("nick", "陈立兵");
         context.put("description", "Please give me a good description");
         User user = new User();
         user.setId(1);
         user.setNick("linux_china");
         context.put("user", user);
         return context;
     }
 }
