 /*
  * Created on Oct 29, 2003
  *
  * To change the template for this generated file go to
  * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 package net.cyklotron.cms.modules.views.periodicals;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.util.Date;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableStateManager;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.upload.FileDownload;
import org.objectledge.utils.StringUtils;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.periodicals.PeriodicalsService;
 import net.cyklotron.cms.periodicals.PeriodicalsTemplatingService;
 import net.cyklotron.cms.preferences.PreferencesService;
 import net.cyklotron.cms.site.SiteResource;
 
 /**
  * @author fil
  *
  * To change the template for this generated type comment go to
  * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 public class DownloadTemplate
     extends BasePeriodicalsScreen
 {
     protected FileDownload fileDownload;
     private final PeriodicalsTemplatingService periodicalsTemplatingService;
     
     public DownloadTemplate(Context context, Logger logger, PreferencesService preferencesService,
         CmsDataFactory cmsDataFactory, TableStateManager tableStateManager,
         PeriodicalsService periodicalsService,
         PeriodicalsTemplatingService periodicalsTemplatingService, FileDownload fileDownload)
     {
         super(context, logger, preferencesService, cmsDataFactory, tableStateManager,
                         periodicalsService);
         this.periodicalsTemplatingService = periodicalsTemplatingService;
         this.fileDownload = fileDownload;
     }
     
 
     /**
      * {@inheritDoc}
      */
     public void process(Parameters parameters, MVCContext mvcContext, 
         TemplatingContext templatingContext, HttpContext httpContext,
         I18nContext i18nContext, CoralSession coralSession)
         throws ProcessingException
     {
         SiteResource site = getSite();
         String renderer = parameters.get("renderer");
         String name = parameters.get("name");
         boolean asText =
             parameters.get("type","binary").equals("text");
         boolean asXML =
             parameters.get("type","binary").equals("xml");
         try
         {
             String contentType;
             long lastModified = (new Date()).getTime();
             String content = "";
             
             if (asText)
             {
                 contentType = "text/plain";
                 content = periodicalsTemplatingService.getTemplateVariantContents(site, renderer, name);
             }
             else if(asXML)
             {
                 contentType = "text/xml";
                 content = periodicalsTemplatingService.getTemplateVariantContents(site, renderer, name);
                 content = 
                     "<?xml version=\"1.0\" encoding=\""+httpContext.getEncoding()+"\"?>\n"+
                     "<contents>\n"+
                     "  <![CDATA["+content+"]]>\n"+
                     "</contents>\n";
             }
             else
             {
                 contentType = "application/octet-stream";
                 content = periodicalsTemplatingService.getTemplateVariantContents(site, renderer, name);
             }
            httpContext.disableCache();
            httpContext.setResponseLength(StringUtils.getByteCount(content, httpContext
                .getEncoding()));
            fileDownload.dumpData(new ByteArrayInputStream(content.getBytes(httpContext
                .getEncoding())), contentType, (new Date()).getTime());
         }
         catch (Exception e)
         {
             throw new ProcessingException("failed to send file", e);
         }
     }
 }
