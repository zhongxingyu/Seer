 package net.cyklotron.cms.modules.actions.appearance.skin;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.filesystem.FileSystem;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.upload.FileUpload;
 import org.objectledge.upload.UploadContainer;
 import org.objectledge.upload.UploadLimitExceededException;
 import org.objectledge.utils.StackTrace;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.integration.IntegrationService;
 import net.cyklotron.cms.modules.actions.appearance.BaseAppearanceAction;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.skins.SkinService;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.style.LayoutResource;
 import net.cyklotron.cms.style.StyleException;
 import net.cyklotron.cms.style.StyleService;
 
 /**
  * 
  * 
  * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CreateLayout.java,v 1.7 2005-12-28 12:47:11 rafal Exp $
  */
 public class CreateLayout extends BaseAppearanceAction
 {
     protected FileUpload fileUpload;
     
     public CreateLayout(Logger logger, StructureService structureService,
         CmsDataFactory cmsDataFactory, StyleService styleService, FileSystem fileSystem,
         SkinService skinService, IntegrationService integrationService,
         FileUpload fileUpload)
     {
         super(logger, structureService, cmsDataFactory, styleService, fileSystem, skinService,
                         integrationService);
         this.fileUpload = fileUpload;
     }
     /* overriden */
     public void execute(Context context, Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, HttpContext httpContext, CoralSession coralSession)
         throws ProcessingException
     {
         String layout = parameters.get("layout");
         String skin = parameters.get("skin");
         boolean useFile = parameters.get("source").
             equals("file");
         UploadContainer file;
         try
         {
             file = fileUpload.getContainer("file");
         }
         catch(UploadLimitExceededException e)
         {
             templatingContext.put("result", "file_size_exceeded");
             return;
         }
         SiteResource site = getSite(context);
         try
         {
             String contents = null;
             if(file == null)
             {
                 if(useFile == true)
                 {
                     templatingContext.put("result","file_not_selected");
                 }
                 else
                 {
                     contents = "";
                 }
             }
             else
             {
                 contents = file.getString();
             }
                 
             if(contents != null)
             {
                 skinService.createLayoutTemplate(coralSession, site, skin, layout, contents, coralSession.getUserSubject());
             }
             
             String[] templateSockets = null;
             try
             {
                 styleService.findSockets(contents);
             }
             catch(StyleException e)
             {
                 templatingContext.put("result", "template_saved_parse_error");
                 templatingContext.put("parse_trace", new StackTrace(e));
                 mvcContext.setView("appearance.skin.EditLayout");
                 return;
             }
             if(templateSockets != null)
             {
                 LayoutResource layoutRes = styleService.getLayout(coralSession, site, layout);
                 if(!styleService.matchSockets(coralSession, layoutRes, templateSockets))
                 {
                     mvcContext.setView("appearance.skin.ValidateLayout");
                 }
             }
         }
         catch(Exception e)
         {
             templatingContext.put("result", "exception");
             templatingContext.put("trace", new StackTrace(e));
         }
         if(templatingContext.containsKey("result"))
         {
            mvcContext.setView("appearance.skin.CreateLayout");
         }
         else
         {
             templatingContext.put("result","file_created");
         }
     }
 }
