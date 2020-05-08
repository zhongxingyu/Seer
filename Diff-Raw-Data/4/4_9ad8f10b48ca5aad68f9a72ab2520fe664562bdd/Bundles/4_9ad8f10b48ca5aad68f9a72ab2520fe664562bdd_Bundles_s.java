 package controllers;
 
 import com.google.javascript.jscomp.*;
 import play.libs.optimization.*;
 import play.mvc.Controller;
 
 /**
  * Serves CSS styles and JavaScripts, merged and compiled to improve performance.
  */
 public class Bundles extends Controller {
     private static final Bundle siteStyles = new StylesheetsBundle(
             "site.css",
             new String[] {
                 "public/css/html5-reset.css",
                 "public/css/main.css",
                 "public/css/tags.css",
                 "public/css/users.css", });
 
     private static final Bundle clientScripts = new ClosureBundle(
             "client.js",
             "public/closure/closure/bin/build/closurebuilder.py",
            null,
//            CompilationLevel.ADVANCED_OPTIMIZATIONS,
             new String[] {
                 "public/closure/closure/goog",
                 "public/closure/third_party/closure",
                 "public/js/client", },
             new String[] { "abperf" });
 
     public static void siteStyles() {
         response.cacheFor("70d");
         siteStyles.applyToResponse(request, response);
     }
 
     public static void clientScripts() {
         clientScripts.getBundleFile().delete();
 
         response.cacheFor("70d");
         clientScripts.applyToResponse(request, response);
     }
 }
