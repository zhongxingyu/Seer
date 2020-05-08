 package com.github.seqware.queryengine.plugins;
 
 import com.github.seqware.queryengine.plugins.recipes.FilteredFileOutputPlugin;
 import org.reflections.Reflections;
 import java.util.Set;
 import java.util.List;
 import java.util.ArrayList;
 
 /**
  * Retrieves the list of plugins which extend the
  * FilteredFileOutputPlugin class 
  * @author jho
  *
  */
 public class PluginList {
   public static List<String> list = new ArrayList<String>();
   
   public PluginList() {
     Reflections reflections = new Reflections("com.github.seqware.queryengine.plugins");
     Set<Class<? extends FilteredFileOutputPlugin>> subTypes = reflections.getSubTypesOf(FilteredFileOutputPlugin.class);
     for (Class c: subTypes) {
      list.add(c.getName());
     }
   }
 }
