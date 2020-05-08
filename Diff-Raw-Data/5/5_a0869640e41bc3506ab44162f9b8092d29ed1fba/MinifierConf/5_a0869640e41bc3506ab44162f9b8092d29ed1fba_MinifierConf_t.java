 package net.contextfw.web.commons.minifier;
 
 import net.contextfw.web.application.configuration.Configuration;
 import net.contextfw.web.application.configuration.SettableProperty;
 
 public class MinifierConf {
 
     private MinifierConf() {
         
     }
     
     public static final MinifierFilter ALL = new MinifierFilter() {
         @Override
         public boolean include(String path) {
             return true;        }
 
         @Override
         public boolean minify(String path) {
             return true;
         }
     };
     
     public static final MinifierFilter NO_JQUERY = new MinifierFilter() {
         @Override
         public boolean include(String path) {
            return !path.contains("jquery");
         }
         @Override
         public boolean minify(String path) {
            return true;
         }
     };
 
     public static final SettableProperty<String> JS_PATH = 
             Configuration.createProperty(String.class, 
                     MinifierConf.class.getName() + ".jsPath");
     
     public static final SettableProperty<String> CSS_PATH = 
             Configuration.createProperty(String.class, 
                     MinifierConf.class.getName() + ".cssPath");
     
     public static final SettableProperty<MinifierFilter> JS_FILTER = 
             Configuration.createProperty(MinifierFilter.class, 
                     MinifierConf.class.getName() + ".jsFilter");
     
     public static final SettableProperty<MinifierFilter> CSS_FILTER = 
             Configuration.createProperty(MinifierFilter.class, 
                     MinifierConf.class.getName() + ".cssFilter");
 }
