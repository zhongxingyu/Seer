 package net.cyklotron.cms.modules.actions.appearance;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.filesystem.FileSystem;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.integration.IntegrationService;
 import net.cyklotron.cms.preferences.PreferencesService;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.skins.ScreenVariantResource;
 import net.cyklotron.cms.skins.SkinService;
 import net.cyklotron.cms.structure.NavigationNodeResource;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.style.StyleService;
 
 public class SelectScreenVariant
 	extends BaseAppearanceAction
 {
 	protected PreferencesService preferencesService;
 
     
     public SelectScreenVariant(Logger logger, StructureService structureService,
         CmsDataFactory cmsDataFactory, StyleService styleService, FileSystem fileSystem,
         SkinService skinService, IntegrationService integrationService,
         PreferencesService preferencesService)
     {
         super(logger, structureService, cmsDataFactory, styleService, fileSystem, skinService,
                         integrationService);
         this.preferencesService = preferencesService;
 	}
 
 	public void execute(Context context, Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, HttpContext httpContext, CoralSession coralSession)
 		throws ProcessingException
 	{
 		SiteResource site = getSite(context);
 		NavigationNodeResource node = getNode(context);
 		Parameters prefs = preferencesService.getCombinedNodePreferences(coralSession, node);
 		String app = prefs.get("screen.app",null);
		String screen = prefs.get("screen.class",null);
        String screenVariantKey = "screen.variant."+app+"."+screen.replace(',','.');
 		String currentVariant = prefs.get(screenVariantKey,"Default");
 		String newVariant  = parameters.get("selected","Default");
 
 		if(currentVariant.equals(newVariant))
 		{
 			return;
 		}
 
 		ScreenVariantResource[] variants;
 		try
 		{
 			String skin = skinService.getCurrentSkin(coralSession, site);
 			variants = skinService.getScreenVariants(coralSession, site, skin, app, screen);
 		}
 		catch(Exception e)
 		{
 			throw new ProcessingException("failed to retrieve variant information");
 		}
 
 		boolean variantExists = false;
 		for(int i=0; i<variants.length; i++)
 		{
 			if(variants[i].getName().equals(currentVariant))
 			{
 				variantExists = true;
 				break;
 			}
 		}
 
 		if(variantExists)
 		{
 			prefs = preferencesService.getNodePreferences(node);
 			prefs.set(screenVariantKey, newVariant);
 			node.update();
 		}
 		else
 		{
 			throw new ProcessingException("cannot set a non existant variant");
 		}
 	}
 }
