 package org.wickedsource.hooked.svn;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.wickedsource.hooked.plugins.api.notifier.CommitData;
 import org.wickedsource.hooked.plugins.api.notifier.NotifierPlugin;
 
 import java.util.Set;
 
 /**
  * @author Tom Hombergs <tom.hombergs@gmail.com>
  */
 public class NotifierPluginVisitor {
 
     private static final Logger logger = LoggerFactory.getLogger(NotifierPluginVisitor.class);
 
     public void visitNotifierPlugins(CommitData data) {
         Set<NotifierPlugin> plugins = PluginRegistry.INSTANCE.getNotifierPlugins();
         for (NotifierPlugin plugin : plugins) {
             try {
                 plugin.notify(data);
             } catch (Exception e) {
                logger.error("Notifier plugin %s did not execute normally. Skipped plugin execution.",
                         plugin.getClass());
             }
         }
     }
 
 }
