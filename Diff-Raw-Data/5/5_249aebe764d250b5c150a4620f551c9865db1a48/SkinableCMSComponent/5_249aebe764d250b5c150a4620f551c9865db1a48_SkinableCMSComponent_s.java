 package net.cyklotron.cms.modules.components;
 
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.Template;
 import org.objectledge.templating.Templating;
 import org.objectledge.web.mvc.finders.MVCFinder;
 
 import net.cyklotron.cms.CmsComponentData;
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.skins.SkinService;
 
 /**
  * The base class for skinable CMS components
  *
  * @author <a href="mailto:zwierzem@ngo.pl">Damian Gajda</a>
 * @version $Id: SkinableCMSComponent.java,v 1.6 2005-03-08 13:02:22 pablo Exp $
  */
 public abstract class SkinableCMSComponent
     extends BaseCMSComponent
 {
     protected SkinService skinService;
 
     protected MVCFinder finderService;
 
     public SkinableCMSComponent(org.objectledge.context.Context context, Logger logger,
         Templating templating, CmsDataFactory cmsDataFactory,
         SkinService skinService, MVCFinder mvcFinder)
     {
         super(context, logger, templating, cmsDataFactory);
         this.skinService = skinService;
         this.finderService = mvcFinder;
     }
 
     private Map methodMap = new HashMap();
 
 
     /**
      * Returns a components template.
      *
      * @return a template to be used for rendering this block.
      */
     public Template getTemplate()
         throws ProcessingException
     {
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         CmsData cmsData = null;
         SiteResource site = null;
         CmsComponentData componentData = null;
         String skin = "default"; 
         try
         {
             cmsData = getCmsData();
             componentData = cmsData.getComponent();
             site = cmsData.getSite();
             if(site == null || componentData.isGlobal())
             {
                 site = cmsData.getGlobalComponentsDataSite();
                 if(site == null)
                 {
                     // no site - this may be a not skinnable component.
                     return super.getTemplate();
                 }
             }
             // 1. get skin name
             skin = skinService.getCurrentSkin(coralSession, site);
         }
         catch(Exception e)
         {
             logger.error("exception occured", e);
             return super.getTemplate();
         }
 
 
         // 2. get component info
         String app = componentData.getApp();
         String component = componentData.getClazz();
         String variant = componentData.getVariant();
         String state = "Default";
         state = getState(context);
 
         // 3. get template object
         Template templ = null;
 
         try
         {
             // if skin defines a template for the variant
             if(skinService.hasComponentTemplate(coralSession, site, skin, app, component, variant, state))
             {
                 templ = skinService.getComponentTemplate(coralSession, site, skin, app, component, variant, state);
             }
             else
             {
                 templ = getAppComponentTemplate(context, app, component, state);
             }
         }
         catch(Exception e)
         {
             logger.error("failed to lookup component template for component "+
                       app+":"+component+" site "+site.getName()+" skin "+skin+
                       " variant "+variant+" state "+state, e);
             templ = null;
         }
 
         // this one throws an exception - we cannot generate component's UI without any templates.
         if(templ == null)
         {
             templ = super.getTemplate();
         }
 
         return templ;
     }
 
     protected Template getAppComponentTemplate(Context context, String app, String component, String state)
     {
         if(!state.equalsIgnoreCase("Default"))
         {
             component = component + state;
         }
        Template template = finderService.findBuilderTemplate(component);
         return template;
     }
 
     /**
      * Returns the current state of the component.
      *
      * <p>The base implemenation always returns state "default" which is OK
      * for stateless components, and inintial state of stateful
      * components.</p>
      *
      * @param  context the request context.
      * @return current state of the component.
      */
     public String getState(Context context)
         throws ProcessingException
     {
         return "Default";
     }
 
     /**
      * Runns the prepare&lt;state&gt;(RunData, Context) method of the child
      * class.
      */
     protected void prepareState(Context context)
         throws ProcessingException
     {
         String state = getState(context);
         Method method = (Method)methodMap.get(state);
         if(method == null)
         {
             Class[] args = new Class[] { Context.class };
             try
             {
                 method = getClass().getMethod("prepare"+state, args);
             }
             catch(NoSuchMethodException e)
             {
                 throw new ProcessingException("method prepare"+state+
                                               "(RunData, Context) not declared in class "+
                                               getClass().getName(), e);
             }
             methodMap.put(state, method);
         }
         try
         {
             method.invoke(this, new Object[] { context });
         }
         catch(Exception e)
         {
             throw new ProcessingException("failed to invoke prepare"+state+
                                               "(RunData, Context) not declared in class "+
                                               getClass().getName(), e);
         }                 
     }
 
 	/**
 	 * Default blank implementaion of prepareDefault method()
 	 */
 	public void prepareDefault(Context context)
 		throws ProcessingException
 	{
         // does nothing
 	}
 }
