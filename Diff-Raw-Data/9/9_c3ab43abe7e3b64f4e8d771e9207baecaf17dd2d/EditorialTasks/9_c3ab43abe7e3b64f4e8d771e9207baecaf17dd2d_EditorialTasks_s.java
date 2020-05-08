 package net.cyklotron.cms.modules.views.structure;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.category.CategoryService;
 import net.cyklotron.cms.modules.actions.structure.workflow.MoveToWaitingRoom;
 import net.cyklotron.cms.preferences.PreferencesService;
 import net.cyklotron.cms.related.RelatedService;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.site.SiteService;
 import net.cyklotron.cms.structure.NavigationNodeResource;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.style.StyleService;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.authentication.UserManager;
 import org.objectledge.context.Context;
 import org.objectledge.coral.query.QueryResults;
 import org.objectledge.coral.relation.Relation;
 import org.objectledge.coral.security.Permission;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.coral.table.comparator.CreationTimeComparator;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableStateManager;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 
 /**
  *
  */
 public class EditorialTasks
     extends BaseStructureScreen
 {   
     protected UserManager userManager;
     
     private CategoryService categoryService;
     
       
     public EditorialTasks(org.objectledge.context.Context context, Logger logger,
         PreferencesService preferencesService, CmsDataFactory cmsDataFactory,
         TableStateManager tableStateManager, StructureService structureService,
         StyleService styleService, SiteService siteService, RelatedService relatedService,
         UserManager userManager, CategoryService categoryService)
     {
         super(context, logger, preferencesService, cmsDataFactory, tableStateManager,
                         structureService, styleService, siteService, relatedService);
         this.userManager = userManager;
         this.categoryService = categoryService;
     }
     
     public void process(Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, HttpContext httpContext, I18nContext i18nContext, CoralSession coralSession)
         throws ProcessingException
     {
         HashSet<Long> classifiedNodes = classifiedNodes = new HashSet<Long>();
         boolean showUnclassified = structureService.isShowUnclassifiedNodes();
         templatingContext.put("showUnclassified", showUnclassified);
         if(showUnclassified)
         {
             Relation refs = categoryService.getResourcesRelation(coralSession);
             Resource[] resources = refs.get(structureService.getNegativeCategory());
             for(Resource res: resources)
             {
                 classifiedNodes.add(res.getIdObject());
             }
             resources = refs.get(structureService.getPositiveCategory());
             for(Resource res: resources)
             {
                 classifiedNodes.add(res.getIdObject());
             }
         }
         try
         {
             long ownerId = parameters.getLong("owner_id", -1);
             String ownerLogin = parameters.get("owner_login","");
             if(ownerLogin.length()> 0)
             {
                 String dn = userManager.getUserByLogin(ownerLogin).getName();
                 Subject owner = coralSession.getSecurity().getSubject(dn);
                 ownerId = owner.getId();
             }
             templatingContext.put("owner_id",new Long(ownerId));
             Subject subject = coralSession.getUserSubject();
             Permission redactorPermission = coralSession.getSecurity().getUniquePermission("cms.structure.modify_own");
             Permission editorPermission = coralSession.getSecurity().getUniquePermission("cms.structure.modify");
            SiteResource site = getSite();
             
             String query = null;
             if(ownerId == -1)
             {
                 query = "FIND RESOURCE FROM structure.navigation_node WHERE site = "+site.getIdString();
             }
             else
             {
                 query = "FIND RESOURCE FROM structure.navigation_node WHERE site = "+site.getIdString()+
                         " AND owner = "+ownerId;
                 templatingContext.put("owner",coralSession.getSecurity().getSubject(ownerId));                        
             }
             
             // hack!!!
             Resource homePage = getHomePage();
             Resource[] parents = coralSession.getStore().
                 getResource(homePage,MoveToWaitingRoom.WAITING_ROOM_NAME);
             if(parents.length > 0)
             {
                 query = query + " AND parent != "+parents[0].getIdString();
             }
             // end of hack!!!                        
             
             QueryResults results = coralSession.getQuery().
                 executeQuery(query);
                 
             
             Resource[] nodes = results.getArray(1);
             List assignedNodes = new ArrayList();
             List takenNodes = new ArrayList();
             List lockedNodes = new ArrayList();
             List rejectedNodes = new ArrayList();
             List newNodes = new ArrayList();
             List preparedNodes = new ArrayList();
             List expiredNodes = new ArrayList();
             List unclassifiedNodes = new ArrayList();
             
             templatingContext.put("related", relatedService.getRelation(coralSession));
             
             for(int i = 0; i < nodes.length; i++)
             {
                 NavigationNodeResource node = (NavigationNodeResource)nodes[i];
                 if(node.getState() == null)
                 {
                     continue;
                 }
                 String state = node.getState().getName();
                 if(subject.hasPermission(node, redactorPermission))
                 {
                     if(subject.equals(node.getOwner()) || subject.hasPermission(node, editorPermission))
                     {
                         if(state.equals("assigned"))
                         {
                             assignedNodes.add(node);
                             continue;
                         }
 						if(state.equals("prepared"))
 						{
 							preparedNodes.add(node);
 							continue;
 						}
                         if(state.equals("taken"))
                         {
                             takenNodes.add(node);
                             continue;
                         }
                         if(state.equals("locked"))
                         {
                             lockedNodes.add(node);
                             continue;
                         }
                         if(state.equals("rejected"))
                         {
                             rejectedNodes.add(node);
                             continue;
                         }
                     }
                 }
                 if(subject.hasPermission(node, editorPermission))
                 {
                     if(state.equals("new"))
                     {
                         newNodes.add(node);
                         continue;
                     }
                     if(state.equals("expired"))
                     {
                         expiredNodes.add(node);
                         continue;
                     }
                     if(showUnclassified && state.equals("published") && 
                       (!classifiedNodes.contains(node.getId())))
                     {
                         unclassifiedNodes.add(node);
                         continue;
                     }
                 }
             }
             CreationTimeComparator pc = new CreationTimeComparator();
             Collections.sort(assignedNodes, pc);
             Collections.reverse(assignedNodes);
             Collections.sort(takenNodes, pc);
             Collections.reverse(takenNodes);
             Collections.sort(lockedNodes, pc);
             Collections.reverse(lockedNodes);
             Collections.sort(rejectedNodes, pc);
             Collections.reverse(rejectedNodes);
             Collections.sort(newNodes, pc);
             Collections.reverse(newNodes);
             Collections.sort(preparedNodes, pc);
             Collections.reverse(preparedNodes);
             Collections.sort(expiredNodes, pc);
             Collections.reverse(expiredNodes);
             if(structureService.isShowUnclassifiedNodes())
             {
                 Collections.sort(unclassifiedNodes, pc);
                 Collections.reverse(unclassifiedNodes);
             }
             templatingContext.put("assigned_nodes", assignedNodes);
             templatingContext.put("taken_nodes", takenNodes);
             templatingContext.put("locked_nodes", lockedNodes);
             templatingContext.put("rejected_nodes", rejectedNodes);
             templatingContext.put("new_nodes", newNodes);
             templatingContext.put("prepared_nodes", preparedNodes);
             templatingContext.put("expired_nodes", expiredNodes);
             templatingContext.put("unclassified_nodes", unclassifiedNodes);
         }
         catch(Exception e)
         {
             throw new ProcessingException("Exception occured", e);
         }
     }
     
     public boolean checkAccessRights(Context context)
         throws ProcessingException
     {
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         return coralSession.getUserSubject().hasRole(getSite().getTeamMember());
     }
 }
