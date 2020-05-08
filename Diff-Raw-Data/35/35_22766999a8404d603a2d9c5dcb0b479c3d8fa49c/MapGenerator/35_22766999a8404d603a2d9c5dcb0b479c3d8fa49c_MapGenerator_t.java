 package org.berlinvegan.generators;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 import org.apache.commons.cli.HelpFormatter;
 
 import com.google.gdata.util.AuthenticationException;
 import freemarker.template.Configuration;
 import freemarker.template.Template;
 
 /**
  * User: smeier Date: 3/30/13 Time: 10:09 PM
  */
 public class MapGenerator extends WebsiteGenerator {
 
     public MapGenerator() throws AuthenticationException {
     }
 
 
     public static void main(String[] args) throws Exception {
 
         if (args.length == 6) {  // 3 options with 1 value -> 6 cli args
             parseOptions(args);
             MapGenerator generator = new MapGenerator();
             generator.generateMap("de");
         } else {
             final HelpFormatter helpFormatter = new HelpFormatter();
             helpFormatter.printHelp("generatemap", constructOptions());
         }
 
     }
 
     private void generateMap(String language) throws Exception {
         ResourceBundle bundle = ResourceBundle.getBundle("i18n", new Locale(language));
 
         final ArrayList<Restaurant> restaurants = getRestaurantsfromServer();
         // Configuration
         Writer file = null;
         Configuration cfg = new Configuration();
 
         try {
             // Set Directory for templates
             cfg.setClassForTemplateLoading(MapGenerator.class, "");
             // load template
            Template template = cfg.getTemplate("map.ftl","ISO-8859-1");
            template.setOutputEncoding("ISO-8859-1");
 
             // data-model
             Map<String, Object> input = new HashMap<>();
             input.put("reviewbase", REVIEW_BASE_LOCATION_DE);
             input.put("i18n", bundle);
             input.put("language", language);
             input.put("restaurants", restaurants);
 
 
             // File output
             file = new FileWriter(new File(outputDir + File.separator + "map.html"));
             template.process(input, file);
             file.flush();
 
         } catch (Exception e) {
             System.out.println(e.getMessage());
 
         } finally {
             if (file != null) {
                 try {
                     file.close();
                 } catch (Exception ignored) {
                 }
             }
         }
 
     }
 }
