 package hudson.plugins.svncompat13;
 
 import hudson.Plugin;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.TreeSet;
 
 import org.tmatesoft.svn.core.SVNException;
 import org.tmatesoft.svn.core.internal.wc.admin.ISVNAdminAreaFactorySelector;
 import org.tmatesoft.svn.core.internal.wc.admin.SVNAdminAreaFactory;
 
 /**
  * Plugin that allows SVNKit to be compatible with Subversion 1.3. All
  * Subversion operations in Hudson go through SVNKit, which creates working copy
  * only compatible with Subversion 1.4 by default. See SVNKit FAQ for more
  * details.
  * 
  * @author <a href="mailto:jbq@caraldi.com">Jean-Baptiste Quenot</a>
  * @plugin
  */
 public class PluginImpl extends Plugin {
 	@Override
 	public void start() throws Exception {
 		SVNAdminAreaFactory.setSelector(new ISVNAdminAreaFactorySelector() {
 			public Collection getEnabledFactories(File path,
 					Collection factories, boolean writeAccess)
 					throws SVNException {
 				Collection<SVNAdminAreaFactory> enabledFactories = new TreeSet<SVNAdminAreaFactory>();
 				for (Iterator factoriesIter = factories.iterator(); factoriesIter
 						.hasNext();) {
 					SVNAdminAreaFactory factory = (SVNAdminAreaFactory) factoriesIter
 							.next();
					if (factory.getSupportedVersion() == WC_FORMAT_13) {
 						enabledFactories.add(factory);
 					}
 				}
 				return enabledFactories;
 			}
 		});
 
 		System.out.println("SVNKit is now compatible with Subversion 1.3");
 	}

    // taken from org.tmatesoft.svn.core.internal.wc.admin.SVNXMLAdminAreaFactory.WC_FORMAT
    // as it's not public
    private static final int WC_FORMAT_13 = 4;
 }
