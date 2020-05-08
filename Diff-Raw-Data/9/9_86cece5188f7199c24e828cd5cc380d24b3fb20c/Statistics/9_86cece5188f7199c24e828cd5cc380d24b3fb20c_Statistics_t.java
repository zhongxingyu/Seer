 package net.cyklotron.cms.modules.views.statistics;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.authentication.UserManager;
 import org.objectledge.coral.query.QueryResults;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.coral.table.comparator.SubjectNameComparator;
 import org.objectledge.coral.table.filter.CreationTimeFilter;
 import org.objectledge.database.Database;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableStateManager;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.utils.StringUtils;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.category.CategoryResource;
 import net.cyklotron.cms.category.CategoryResourceImpl;
 import net.cyklotron.cms.category.CategoryService;
 import net.cyklotron.cms.preferences.PreferencesService;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.structure.NavigationNodeResource;
 import net.cyklotron.cms.structure.table.ValidityStartFilter;
 
 /**
  * CMS Statistics main screen.
  *
  */
 public class Statistics extends BaseStatisticsScreen
 {
     
     public Statistics(org.objectledge.context.Context context, Logger logger,
         PreferencesService preferencesService, CmsDataFactory cmsDataFactory,
         TableStateManager tableStateManager, Database database, UserManager userManager,
         CategoryService categoryService)
     {
         super(context, logger, preferencesService, cmsDataFactory, tableStateManager, database,
                         userManager, categoryService);
         
     }
     public void process(Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, HttpContext httpContext, I18nContext i18nContext, CoralSession coralSession) throws ProcessingException
     {
         SimpleDateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", StringUtils.getLocale("en_US"));
         Resource[] states = coralSession.getStore().getResourceByPath("/cms/workflow/automata/structure.navigation_node/states/*");
         templatingContext.put("states", states);
 
         if (parameters.get("show","").length() == 0)
         {
             return;
         }
 
         SiteResource site = getSite();
         Resource state = null;
         Date validityStart = null;
         Date validityEnd = null;
         Date createdStart = null;
         Date createdEnd = null;
         Subject creator = null;
         CategoryResource category = null;
 
         // prepare the conditions...
         if (parameters.get("validity_start","").length() > 0)
         {
             validityStart = new Date(parameters.getLong("validity_start"));
             templatingContext.put("validity_start", validityStart);
         }
         if (parameters.get("validity_end","").length() > 0)
         {
             validityEnd = new Date(parameters.getLong("validity_end"));
             templatingContext.put("validity_end", validityEnd);
         }
         if (parameters.get("created_start","").length() > 0)
         {
             createdStart = new Date(parameters.getLong("created_start"));
             templatingContext.put("created_start", createdStart);
         }
         if (parameters.get("created_end","").length() > 0)
         {
             createdEnd = new Date(parameters.getLong("created_end"));
             templatingContext.put("created_end", createdEnd);
         }
         ValidityStartFilter validityStartFilter = new ValidityStartFilter(validityStart, validityEnd);
         CreationTimeFilter creationTimeFilter = new CreationTimeFilter(createdStart, createdEnd);
 
         String createdBy = parameters.get("created_by","");
         long stateId = parameters.getLong("selected_state", -1);
        
        
        
 
         int counter = 0;
         try
         {
             if (stateId != -1)
             {
                 state = coralSession.getStore().getResource(stateId);
                 templatingContext.put("selected_state", state);
             }
            if (parameters.get("category_id","").length() > 0)
             {
                long categoryId = parameters.getLong("category_id", -1);
                 category = CategoryResourceImpl.getCategoryResource(coralSession, categoryId);
                 templatingContext.put("category", category);
             }
             if (createdBy.length() > 0)
             {
                 try
                 {
                     String dn = userManager.getUserByLogin(createdBy).getName();
                     creator = coralSession.getSecurity().getSubject(dn);
                     templatingContext.put("created_by", createdBy);
                 }
                 catch (Exception e)
                 {
                     // do nothing...or maybe report that user is unknown!
                     templatingContext.put("result", "unknown_user");
                 }
             }
         }
         catch (Exception e)
         {
             throw new ProcessingException("Exception occured during query preparation");
         }
 
         /*
         Connection conn = null;
         try
         {
         	ResourceClass nodeClass = coralSession.getSchema().getResourceClass("structure.navigation_node");
         	ResourceClass docClass = coralSession.getSchema().getResourceClass("documents.document_node");
         	ArrayList nodes = new ArrayList();
         	
         	
         	conn = databaseService.getConnection();
         	Statement statement = conn.createStatement();
         	ResultSet rs = statement.executeQuery(
         		"SELECT count(*) FROM arl_resource WHERE resource_class_id = " + nodeClass.getId() + " OR resource_class_id = " + docClass.getId());
         	
         	//while (rs.next())
         	//{
         	//	nodes.add(new Long(rs.getLong("resource_id")));
         	//}
         	//counter = nodes.size();
         	
         	if(rs.next())
         	{
         		counter = rs.getInt(1);
         	}
         }
         catch (Exception e)
         {
         	throw new ProcessingException("Exception occured",e);
         }
         finally
         {
         	try
         	{
         		if (conn != null)
         		{
         			conn.close();
         		}
         	}
         	catch (SQLException e)
         	{
         		log.error("failed to close connection", e);
         	}
         }
         templatingContext.put("counter",new Integer(counter));
         */
         boolean nextCondition = false;
         StringBuilder sb = new StringBuilder("FIND RESOURCE FROM documents.document_node");
         if (site != null)
         {
             nextCondition = true;
             sb.append(" WHERE site = ");
             sb.append(site.getIdString());
         }
 
         if (state != null)
         {
             if (nextCondition)
             {
                 sb.append(" AND ");
             }
             else
             {
                 sb.append(" WHERE ");
             }
             sb.append("state = " + state.getIdString());
             nextCondition = true;
         }
 
         if (creator != null)
         {
             if (nextCondition)
             {
                 sb.append(" AND ");
             }
             else
             {
                 sb.append(" WHERE ");
             }
             sb.append("created_by = " + creator.getIdString());
             nextCondition = true;
         }
 
         if (validityStart != null)
         {
             if (nextCondition)
             {
                 sb.append(" AND ");
             }
             else
             {
                 sb.append(" WHERE ");
             }
             sb.append("validity_start > '" + df.format(validityStart) + "'");
             nextCondition = true;
         }
         if (validityEnd != null)
         {
             if (nextCondition)
             {
                 sb.append(" AND ");
             }
             else
             {
                 sb.append(" WHERE ");
             }
             sb.append("validity_start < '" + df.format(validityEnd) + "'");
             nextCondition = true;
         }
         if (createdStart != null)
         {
             if (nextCondition)
             {
                 sb.append(" AND ");
             }
             else
             {
                 sb.append(" WHERE ");
             }
             sb.append("creation_time > '" + df.format(createdStart) + "'");
             nextCondition = true;
         }
         if (createdEnd != null)
         {
             if (nextCondition)
             {
                 sb.append(" AND ");
             }
             else
             {
                 sb.append(" WHERE ");
             }
             sb.append("creation_time < '" + df.format(createdEnd) + "'");
             nextCondition = true;
         }
         String query = sb.toString();
         templatingContext.put("query", query);
         try
         {
             QueryResults results = coralSession.getQuery().executeQuery(query);
             Resource[] nodes = results.getArray(1);
             HashMap editors = new HashMap();
             HashMap redactors = new HashMap();
             HashMap creators = new HashMap();
             if (category == null)
             {
                 counter = nodes.length;
                 if (site != null)
                 {
                     for (int i = 0; i < nodes.length; i++)
                     {
                         countRedactors(nodes[i], redactors, editors, creators);
                     }
                 }
             }
             else
             {
                 Set fromCategorySet = new HashSet();
                 Resource[] resources = categoryService.getResources(coralSession, category, true);
                 for (int i = 0; i < resources.length; i++)
                 {
                     fromCategorySet.add(resources[i]);
                 }
                 counter = 0;
                 for (int i = 0; i < nodes.length; i++)
                 {
                     if (fromCategorySet.contains(nodes[i]))
                     {
                         counter++;
                         countRedactors(nodes[i], redactors, editors, creators);
                     }
                 }
             }
             templatingContext.put("counter", new Integer(counter));
 
 
             if (site != null)
             {
                 templatingContext.put("editors", editors);
                 templatingContext.put("redactors", redactors);
                 templatingContext.put("creators", creators);
                 HashSet users = new HashSet();
                 Iterator it = editors.keySet().iterator();
                 while (it.hasNext())
                 {
                     users.add(it.next());
                 }
                 it = redactors.keySet().iterator();
                 while (it.hasNext())
                 {
                     users.add(it.next());
                 }
                 it = creators.keySet().iterator();
                 while (it.hasNext())
                 {
                     users.add(it.next());
                 }
                 ArrayList usersList = new ArrayList(users);
                 Collections.sort(usersList, new SubjectNameComparator(i18nContext.getLocale()));
                 templatingContext.put("users", usersList);
             }
         }
         catch (Exception e)
         {
             throw new ProcessingException("Exception occured during query execution", e);
         }
     }
 
     private void countRedactors(Resource resource, Map redactors, Map editors, Map creators)
     {
         NavigationNodeResource node = (NavigationNodeResource)resource;
         Subject redactor = node.getOwner();
         Subject editor = node.getLastEditor();
         Subject creator = node.getCreatedBy();
         if (redactor != null)
         {
             Integer redactorCount = (Integer)redactors.get(redactor);
             if (redactorCount == null)
             {
                 redactorCount = new Integer(1);
                 redactors.put(redactor, redactorCount);
             }
             else
             {
                 redactors.put(redactor, new Integer(redactorCount.intValue() + 1));
             }
         }
         if (editor != null)
         {
             Integer editorCount = (Integer)editors.get(editor);
             if (editorCount == null)
             {
                 editorCount = new Integer(1);
                 editors.put(editor, editorCount);
             }
             else
             {
                 editors.put(editor, new Integer(editorCount.intValue() + 1));
             }
         }
         if (creator != null)
         {
             Integer creatorCount = (Integer)creators.get(creator);
             if (creatorCount == null)
             {
                 creatorCount = new Integer(1);
                 creators.put(creator, creatorCount);
             }
             else
             {
                 creators.put(creator, new Integer(creatorCount.intValue() + 1));
             }
         }
     }
 }
