 package timetracker;
 
 import org.chromattic.api.Chromattic;
 import org.chromattic.api.ChromatticBuilder;
 import org.exoplatform.services.jcr.RepositoryService;
 import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
 import org.exoplatform.services.jcr.ext.common.SessionProvider;
 import org.exoplatform.services.jcr.impl.core.ExtendedNamespaceRegistry;
 import timetracker.integration.CurrentRepositoryLifeCycle;
 import timetracker.model.*;
 
 import javax.inject.Inject;
 import javax.jcr.Session;
 import java.io.InputStream;
 
 public class ChromatticService {
 
   Chromattic chromattic;
 
   RepositoryService repoService_;
 
   @Inject
   public ChromatticService(RepositoryService repositoryService)
   {
     repoService_ = repositoryService;
   }
 
   public Chromattic init()
   {
 
     registerNodetypes(repoService_);
 
     ChromatticBuilder builder = ChromatticBuilder.create();
     builder.add(Task.class);
     builder.add(User.class);
     builder.add(Week.class);
     builder.add(Column.class);
     builder.add(Columns.class);
 
     builder.setOptionValue(ChromatticBuilder.SESSION_LIFECYCLE_CLASSNAME, CurrentRepositoryLifeCycle.class.getName());
     builder.setOptionValue(ChromatticBuilder.CREATE_ROOT_NODE, true);
     builder.setOptionValue(ChromatticBuilder.ROOT_NODE_PATH, "/Documents");
 
     chromattic = builder.build();
 
     return chromattic;
   }
 
   private void registerNodetypes(RepositoryService repoService)
   {
     SessionProvider sessionProvider = SessionProvider.createSystemProvider();
     try
     {
       //get info
       Session session = sessionProvider.getSession("dms-system", repoService.getCurrentRepository());
 
       ExtendedNamespaceRegistry namespaceRegistry = (ExtendedNamespaceRegistry) session.getWorkspace().getNamespaceRegistry();
      if (namespaceRegistry.getNamespacePrefixByURI("http://juzu.org/jcr/timetracker")==null)
         namespaceRegistry.registerNamespace("tt", "http://juzu.org/jcr/timetracker");
 
       ExtendedNodeTypeManager nodeTypeManager = (ExtendedNodeTypeManager) session.getWorkspace().getNodeTypeManager();
       InputStream is = ChromatticService.class.getResourceAsStream("model/nodetypes.xml");
       nodeTypeManager.registerNodeTypes(is, ExtendedNodeTypeManager.REPLACE_IF_EXISTS);
 
     }
     catch (Exception e)
     {
       e.printStackTrace();
     }
     finally {
       sessionProvider.close();
     }
   }
 
 
 }
