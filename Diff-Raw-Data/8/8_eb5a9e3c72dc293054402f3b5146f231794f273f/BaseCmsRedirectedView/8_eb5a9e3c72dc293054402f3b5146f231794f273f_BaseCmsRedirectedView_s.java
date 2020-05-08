 // 
 // Copyright (c) 2003, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
 // All rights reserved. 
 // 
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  
 // AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED  
 // WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 // IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,  
 // INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,  
 // BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 // OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,  
 // WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)  
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE  
 // POSSIBILITY OF SUCH DAMAGE. 
 // 
 package net.cyklotron.cms.modules.views;
 
 import org.objectledge.context.Context;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.Template;
 import org.objectledge.templating.TemplateNotFoundException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 import org.objectledge.web.mvc.builders.AbstractBuilder;
 import org.objectledge.web.mvc.builders.BuildException;
 import org.objectledge.web.mvc.builders.EnclosingView;
 
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.preferences.PreferencesService;
 import net.cyklotron.cms.site.SiteException;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.site.SiteService;
 import net.cyklotron.cms.skins.SkinException;
 import net.cyklotron.cms.skins.SkinService;
 
 /**
  * A default view.
  *  
  * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
  * @version $Id$
  */
 public class BaseCmsRedirectedView extends AbstractBuilder
 {
     protected SiteService siteService;
     
     protected PreferencesService preferencesService;
     
     protected SkinService skinService;
     
     public BaseCmsRedirectedView(Context context, SiteService siteService,
         PreferencesService preferencesService, SkinService skinService)
     {
         super(context);
         this.siteService = siteService;
         this.preferencesService = preferencesService;
         this.skinService = skinService;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void process(TemplatingContext templatingContext) throws ProcessingException
     {
         HttpContext httpContext = HttpContext.getHttpContext(context);
         MVCContext mvcContext = MVCContext.getMVCContext(context);
         templatingContext.put("http_context", httpContext);
         templatingContext.put("mvc_context", mvcContext);
     }
     
     
     /**
      * {@inheritDoc}
      */
     public String build(Template template, String embeddedBuildResults)
         throws BuildException, ProcessingException
     {
         HttpContext httpContext = HttpContext.getHttpContext(context);
         MVCContext mvcContext = MVCContext.getMVCContext(context);
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         CmsData cmsData = CmsDataFactory.getCmsDataIfExists(context);
         SiteResource site = null;
         if(cmsData != null)
         {
             site = cmsData.getSite();
         }
         if(site == null)
         {
             try
             {
                 site = siteService.
                     getSite(coralSession, httpContext.getRequest().getServerName());
             }
             catch(SiteException e)
             {
                 //ignore it!
             }
         }
         Template newTemplate = template;
         if(site != null)
         {
            String view = mvcContext.getView();
             try
             {
                 String skin = skinService.getCurrentSkin(coralSession, site);
                 if(skinService.hasSystemScreenTemplate(coralSession, site, skin, view))
                 {
                     newTemplate = skinService.getSystemScreenTemplate(coralSession, site, skin, view);
                 }
             }
             catch(SkinException e)
             {
                 //ignore it!
             }
             catch(TemplateNotFoundException e)
             {
                 //ignore it!
             }
         }
         return super.build(newTemplate, embeddedBuildResults); 
     }
     
     /**
      * {@inheritDoc}
      */
     public EnclosingView getEnclosingView(String thisViewName)
     {
         return new EnclosingView("Page");
     }
 }
