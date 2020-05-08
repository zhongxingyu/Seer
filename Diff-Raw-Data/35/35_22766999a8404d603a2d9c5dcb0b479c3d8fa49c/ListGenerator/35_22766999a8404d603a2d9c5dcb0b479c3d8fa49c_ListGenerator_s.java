 package org.berlinvegan.generators;
 
 import com.google.gdata.util.AuthenticationException;
 import freemarker.template.Configuration;
 import freemarker.template.Template;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.lang3.StringUtils;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.Writer;
 import java.util.*;
 
 /**
  * User: smeier
  * Date: 4/13/13
  * Time: 5:45 PM
  */
 public class ListGenerator extends WebsiteGenerator {
     public ListGenerator() throws AuthenticationException {
         super();
     }
 
     public static void main(String[] args) throws Exception {
         if (args.length == 6) {  // 3 options with 1 value -> 6 cli args
             parseOptions(args);
             ListGenerator generator = new ListGenerator();
             generator.generateList("de");
         } else {
             final HelpFormatter helpFormatter = new HelpFormatter();
             helpFormatter.printHelp("generatefactsheets", constructOptions());
         }
     }
 
     private void generateList(String language) throws Exception {
         ResourceBundle bundle = ResourceBundle.getBundle("i18n", new Locale(language));
         final ArrayList<Restaurant> restaurants = getRestaurantsfromServer();
 
         // Configuration
         Writer file = null;
         Configuration cfg = new Configuration();
         try {
             // Set Directory for templates
             cfg.setClassForTemplateLoading(ListGenerator.class, "");
             // load template
            Template template = cfg.getTemplate("list.ftl");
             // data-model
             Map<String, Object> input = new HashMap<String, Object>();
             input.put("reviewbase", REVIEW_BASE_LOCATION_DE);
             input.put("i18n", bundle);
             input.put("language", language);
             ArrayList<Restaurant> uniqueRestaurants = getUniqueRestaurants(restaurants);
             for (Restaurant restaurant : uniqueRestaurants) {
                 String url = WEBSITE_DE + REVIEW_BASE_LOCATION_DE+ restaurant.getReviewURL();
                 Rating rating = getRatingFromWebsite(url);
                 if (rating != null) {
                     restaurant.setRating(rating);
                 }
             }
             input.put("restaurants", uniqueRestaurants);
 
             // File output
             file = new FileWriter(new File(outputDir + File.separator + "list.html"));
             template.process(input, file);
             file.flush();
         } catch (Exception e) {
            System.out.println(e.getMessage());
             e.printStackTrace();
 
         } finally {
             if (file != null) {
                 try {
                     file.close();
                 } catch (Exception ignored) {
                 }
             }
         }
 
     }
 
     /***
      * only 1 restaurant instance per name, without branches and only restaurants with review
      * @param restaurants
      * @return
      */
     private ArrayList<Restaurant> getUniqueRestaurants(ArrayList<Restaurant> restaurants) {
         ArrayList<Restaurant> result = new ArrayList<>();
         HashSet<String> restaurantsDone = new HashSet<>();
         for (Restaurant restaurant : restaurants) {
             String reviewURL = restaurant.getReviewURL();
             if (!StringUtils.isEmpty(reviewURL) && !restaurantsDone.contains(reviewURL)) {
                 result.add(restaurant);
                 restaurantsDone.add(reviewURL);
             }
         }
         return result;
     }
 
 }
