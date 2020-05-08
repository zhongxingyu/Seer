 package net.cyklotron.cms.modules.views.appearance;
 
 import java.util.Arrays;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableStateManager;
 import org.objectledge.templating.Templating;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsComponentData;
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.integration.IntegrationService;
 import net.cyklotron.cms.preferences.PreferencesService;
 import net.cyklotron.cms.site.SiteException;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.site.SiteService;
 import net.cyklotron.cms.skins.ComponentVariantResource;
 import net.cyklotron.cms.skins.SkinException;
 import net.cyklotron.cms.skins.SkinService;
 import net.cyklotron.cms.structure.NavigationNodeResource;
 import net.cyklotron.cms.style.StyleService;
 
 public class VariantList
     extends BaseAppearanceScreen
 {
     protected SiteService siteService;
 
     public VariantList(org.objectledge.context.Context context, Logger logger,
         PreferencesService preferencesService, CmsDataFactory cmsDataFactory,
         TableStateManager tableStateManager, StyleService styleService, SkinService skinService,
         IntegrationService integrationService, Templating templating,
         SiteService siteService)
     {
         super(context, logger, preferencesService, cmsDataFactory, tableStateManager, styleService,
                         skinService, integrationService, templating);
         this.siteService = siteService;
     }
     public void process(Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, HttpContext httpContext, I18nContext i18nContext, CoralSession coralSession)
         throws ProcessingException
     {
         CmsData cmsData = getCmsData();
         SiteResource site = cmsData.getSite();
         NavigationNodeResource node = cmsData.getNode();
         Parameters preferences;
         if(node != null)
         {
             preferences = preferencesService.getCombinedNodePreferences(coralSession, node);
         }
         else
         {
             preferences = preferencesService.getSystemPreferences(coralSession);
             String dataSite = preferences.get("globalComponentsData","");
             try
             {
                 site = siteService.getSite(coralSession, dataSite);
             }
             catch(SiteException e)
             {
                 throw new ProcessingException("failed to lookup global components data site");
             }
         }
         String instance = parameters.get("component_instance");
         templatingContext.put("component_instance", instance);
 
         String app = CmsComponentData.getParameter(preferences, "component."+instance+".app",null);
         String component = CmsComponentData.getParameter(preferences,"component."+instance+".class",null);
         String variant = CmsComponentData.getParameter(preferences,"component."+instance+".variant."+
        	app+"."+component.replace(',','.'),"Default");
         templatingContext.put("current_name", variant);
 
         String skin;
         try
         {
             skin = skinService.getCurrentSkin(coralSession, site);
         }
         catch(SkinException e)
         {
             throw new ProcessingException("failed to retrieve skin information", e);
         }
 
         try
         {
             ComponentVariantResource[] variants =
                             skinService.getComponentVariants(coralSession, site, skin, app, component);
             templatingContext.put("variants", Arrays.asList(variants));
             
             for(int i=0; i<variants.length; i++)
             {
                 if(variants[i].getName().equals(variant))
                 {
                     templatingContext.put("current_variant", variants[i]);
                     break;
                 }
             }
         }
         catch(SkinException e)
         {
             // WARN: silent fail - do not display variant info
         }
     }
 }
