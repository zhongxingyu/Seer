 package net.cyklotron.cms.modules.views.documents;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableStateManager;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.files.DirectoryResource;
 import net.cyklotron.cms.files.DirectoryResourceImpl;
 import net.cyklotron.cms.modules.views.BaseCMSScreen;
 import net.cyklotron.cms.preferences.PreferencesService;
 import net.cyklotron.cms.structure.NavigationNodeResourceImpl;
 
 /**
  * A screen for configuring calendar screen.
  * 
  * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
  * @version $Id: ProposeDocumentConf.java,v 1.3 2008-10-30 17:46:03 rafal Exp $
  */
 public class ProposeDocumentConf
     extends BaseCMSScreen
 {
     public ProposeDocumentConf(org.objectledge.context.Context context, Logger logger,
         PreferencesService preferencesService, CmsDataFactory cmsDataFactory,
         TableStateManager tableStateManager)
     {
         super(context, logger, preferencesService, cmsDataFactory, tableStateManager);
 
     }
 
     public void process(Parameters parameters, MVCContext mvcContext,
         TemplatingContext templatingContext, HttpContext httpContext, I18nContext i18nContext,
         CoralSession coralSession)
         throws ProcessingException
     {
         try
         {
             Parameters screenConfig = getScreenConfig();
             long root1 = screenConfig.getLong("category_id_1", -1);
             long root2 = screenConfig.getLong("category_id_2", -1);
             if(root1 != -1)
             {
                 templatingContext.put("category_1", coralSession.getStore().getResource(root1));
             }
             if(root2 != -1)
             {
                 templatingContext.put("category_2", coralSession.getStore().getResource(root2));
             }
             templatingContext.put("attachments_enabled", screenConfig.getBoolean(
                 "attachments_enabled", false));
             long dirId = screenConfig.getLong("attachments_dir_id", -1);
             templatingContext.put("attachments_dir_id", dirId);
             if(dirId != -1)
             {
                 CmsData cmsData = cmsDataFactory.getCmsData(context);
                 DirectoryResource dir = DirectoryResourceImpl.getDirectoryResource(coralSession,
                     dirId);
                 String dirLocalPath = dir.getPath().substring(cmsData.getSite().getPath().length())
                     .substring(6);
                 templatingContext.put("attachments_dir", screenConfig.get("attachments_dir",
                     dirLocalPath));
             }
             int fileCount = screenConfig.getInt("attachments_max_count", -1);
             if(fileCount != -1)
             {
                 templatingContext.put("attachments_max_count", fileCount);
             }
             int fileSize = screenConfig.getInt("attachments_max_size", -1);
             if(fileCount != -1)
             {
                 templatingContext.put("attachments_max_size", fileSize);
             }
             String formats = screenConfig.get(
                 "attachments_allowed_formats", "jpg gif doc rtf pdf xls");
             templatingContext.put("attachments_allowed_formats", formats);
             templatingContext.put("inherit_categories", screenConfig.getBoolean("inherit_categories", false));
             templatingContext.put("calendar_tree", screenConfig.getBoolean("calendar_tree", false));
             long parentId = screenConfig.getLong("parent_id", -1L);
             if(parentId != -1)
             {
                 try
                 {
                     String parentPath = NavigationNodeResourceImpl.getNavigationNodeResource(
                         coralSession, parentId).getSitePath();
                     templatingContext.put("parent_id", parentId);
                     templatingContext.put("parent_path", parentPath);
                 }
                 catch(EntityDoesNotExistException e)
                 {
                     // ignore
                 }
             }
             templatingContext.put("editing_enabled", screenConfig.getBoolean(
                 "editing_enabled", false));
         }
         catch(Exception e)
         {
             throw new ProcessingException("Exception occurred", e);
         }
     }
 }
