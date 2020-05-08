 package net.cyklotron.cms.modules.views.link;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableStateManager;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 import org.objectledge.web.mvc.tools.LinkTool;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.link.BaseLinkResource;
 import net.cyklotron.cms.link.CmsLinkResource;
 import net.cyklotron.cms.link.ExternalLinkResource;
 import net.cyklotron.cms.link.LinkService;
 import net.cyklotron.cms.preferences.PreferencesService;
 import net.cyklotron.cms.structure.StructureService;
 
 /**
  * The link search result screen class.
  *
  * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: LinksSearchResult.java,v 1.5 2005-03-10 13:40:00 pablo Exp $
  */
 public class LinksSearchResult
     extends BaseLinkScreen
 {
     
     public LinksSearchResult(Context context, Logger logger, PreferencesService preferencesService,
         CmsDataFactory cmsDataFactory, TableStateManager tableStateManager,
         LinkService linkService, StructureService structureService)
     {
         super(context, logger, preferencesService, cmsDataFactory, tableStateManager, linkService,
                         structureService);
         
     }
     
     
     /* (non-Javadoc)
      * @see net.cyklotron.cms.modules.views.BaseCMSScreen#process(org.objectledge.parameters.Parameters, org.objectledge.web.mvc.MVCContext, org.objectledge.templating.TemplatingContext, org.objectledge.web.HttpContext, org.objectledge.i18n.I18nContext, org.objectledge.coral.session.CoralSession)
      */
     public void process(Parameters parameters, MVCContext mvcContext,
         TemplatingContext templatingContext, HttpContext httpContext, I18nContext i18nContext,
         CoralSession coralSession) throws org.objectledge.pipeline.ProcessingException
     {
         try
         {
             long rid = parameters.getLong("res_id", -1);
             if(rid == -1)
             {
                 throw new ProcessingException("Resource id not found");
             }
             Resource resource = coralSession.getStore().getResource(rid);
             if(!(resource instanceof BaseLinkResource))
             {
                 throw new ProcessingException("Class of the resource '"+resource.getResourceClass().getName()+
                                               "' is does not belong to link application");
             }
             if(resource instanceof CmsLinkResource)
             {
                 LinkTool link = (LinkTool)templatingContext.get("link");
                link = link.unset("view").set("x",((CmsLinkResource)resource).getNode().getIdString());
                 httpContext.sendRedirect(link.toString());
             }
             if(resource instanceof ExternalLinkResource)
             {
                 httpContext.sendRedirect(((ExternalLinkResource)resource).getTarget());
             }
         }
         catch(Exception e)
         {
             throw new ProcessingException("Exception occured during redirecting...",e);
         }
     }
 
     public boolean checkAccessRights(Context context)
         throws ProcessingException
     {
         return true;
     }
 }
