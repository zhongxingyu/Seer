 package org.wickedsource.hooked.svn;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.admin.SVNLookClient;
import org.wickedsource.hooked.plugins.api.collector.CollectorPlugin;
import org.wickedsource.hooked.plugins.api.collector.CommittedFile;
import org.wickedsource.hooked.plugins.api.collector.FileMetaData;
 import org.wickedsource.hooked.plugins.api.notifier.CommitData;
import org.wickedsource.hooked.plugins.api.notifier.FileMetrics;
 import org.wickedsource.hooked.plugins.api.notifier.NotifierPlugin;
import org.wickedsource.hooked.svn.data.SvnCommitData;
import org.wickedsource.hooked.svn.data.SvnFileMetaData;
 
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
 import java.util.Set;
 
 /**
  * @author Tom Hombergs <tom.hombergs@gmail.com>
  */
 public class NotifierPluginVisitor {
 
     public void visitNotifierPlugins(CommitData data) {
         Set<NotifierPlugin> plugins = PluginRegistry.INSTANCE.getNotifierPlugins();
        for(NotifierPlugin plugin : plugins){
            plugin.notify(data);
         }
     }
 
 }
