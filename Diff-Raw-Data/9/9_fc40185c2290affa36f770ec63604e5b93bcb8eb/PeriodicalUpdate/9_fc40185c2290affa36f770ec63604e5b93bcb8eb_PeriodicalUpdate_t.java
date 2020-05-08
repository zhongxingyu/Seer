 package net.cyklotron.cms.modules.actions.periodicals;
 
 import java.util.Iterator;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.utils.StackTrace;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.category.query.CategoryQueryPoolResource;
 import net.cyklotron.cms.category.query.CategoryQueryPoolResourceImpl;
 import net.cyklotron.cms.files.DirectoryResource;
 import net.cyklotron.cms.files.DirectoryResourceImpl;
 import net.cyklotron.cms.periodicals.EmailPeriodicalResource;
 import net.cyklotron.cms.periodicals.PeriodicalResource;
 import net.cyklotron.cms.periodicals.PeriodicalResourceData;
 import net.cyklotron.cms.periodicals.PeriodicalResourceImpl;
 import net.cyklotron.cms.periodicals.PeriodicalsService;
 import net.cyklotron.cms.periodicals.PublicationTimeData;
 import net.cyklotron.cms.site.SiteService;
 import net.cyklotron.cms.structure.StructureService;
 
 /**
  * Periodical update action.
  *
  * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
  * @version $Id: PeriodicalUpdate.java,v 1.11 2008-10-07 14:47:54 rafal Exp $
  */
 public class PeriodicalUpdate
     extends BasePeriodicalsAction
 {
     
     
     public PeriodicalUpdate(Logger logger, StructureService structureService,
         CmsDataFactory cmsDataFactory, PeriodicalsService periodicalsService,
         SiteService siteService)
     {
         super(logger, structureService, cmsDataFactory, periodicalsService, siteService);
         
     }
     /**
      * Performs the action.
      */
     public void execute(Context context, Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, HttpContext httpContext, CoralSession coralSession)
         throws ProcessingException
     {
         Subject subject = coralSession.getUserSubject();
 
 		long periodicalId = parameters.getLong("periodical_id", -1);
 		if(periodicalId == -1)
 		{
 			throw new ProcessingException("Periodical id couldn't be found");
 		}
 		PeriodicalResource periodical = null;
 		PeriodicalResourceData periodicalData = null;
 		try
 		{
 			periodical = PeriodicalResourceImpl.getPeriodicalResource(coralSession, periodicalId);
 			periodicalData = PeriodicalResourceData.getData(httpContext, periodical, false);
     	    periodicalData.update(parameters);
         	if(periodicalData.getName().equals(""))
         	{
             	templatingContext.put("result", "name_empty");
             	return;
         	}
         	if(!periodicalData.getName().equals(periodical.getName()) &&
 				coralSession.getStore().getResource(periodical.getParent(), periodicalData.getName()).length > 0)
             {
                	templatingContext.put("result","duplicate_periodical_name");
                 return;
             }
             if(periodicalData.getStorePlace()==-1)
             {
 				templatingContext.put("result","store_place_not_choosen");
 				return;
             }
 			if(periodicalData.getCategoryQuerySet()==-1)
 			{
 				templatingContext.put("result","category_query_set_not_choosen");
 				return;
 			}
             if(periodicalData.isEmailPeriodical())
             {
             	if(periodicalData.getFromHeader().length() == 0)
             	{
 					templatingContext.put("result","from_header_not_choosen");
 					return;
             	}
             }
             for(Iterator i = periodicalData.getPublicationTimes().iterator(); i.hasNext();)
             {
                 PublicationTimeData time = (PublicationTimeData)i.next();
                 if(time.getDayOfMonth() == -1 && time.getDayOfWeek() == -1)
                 {
                     templatingContext.put("result", "must_choose_day_of_month_or_week");
                     return;
                 }
                 if(time.getDayOfMonth() != -1 && time.getDayOfWeek() != -1)
                 {
                     templatingContext.put("result", "cant_choose_day_of_month_and_week");
                     return;
                 }
             }
             DirectoryResource storePlace = DirectoryResourceImpl.
             	getDirectoryResource(coralSession, periodicalData.getStorePlace());
 			CategoryQueryPoolResource categoryQuerySet = CategoryQueryPoolResourceImpl.
 				getCategoryQueryPoolResource(coralSession, periodicalData.getCategoryQuerySet());
 			periodical.setDescription(periodicalData.getDescription());
             periodical.setStorePlace(storePlace);
             periodical.setCategoryQuerySet(categoryQuerySet);
             periodical.setSortOrder(periodicalData.getSortOrder());
             periodical.setSortDirection(periodicalData.getSortDirection());
 			periodical.setRenderer(periodicalData.getRenderer());
             periodical.setTemplate(periodicalData.getTemplate());
             periodical.setLocale(periodicalData.getLocale());
             periodical.setEncoding(periodicalData.getEncoding());
 			if(periodicalData.isEmailPeriodical())
 			{
 				((EmailPeriodicalResource)periodical).setAddresses(sortAddresses(periodicalData.getAddresses()));
 				((EmailPeriodicalResource)periodical).setFromHeader(periodicalData.getFromHeader());
 				((EmailPeriodicalResource)periodical).setReplyToHeader(periodicalData.getReplyToHeader());
 	            ((EmailPeriodicalResource)periodical).setSubject(periodicalData.getSubject());
 				((EmailPeriodicalResource)periodical).setFullContent(periodicalData.getFullContent());
 				((EmailPeriodicalResource)periodical).setSendEmpty(periodicalData.getSendEmpty());
                 ((EmailPeriodicalResource)periodical).setNotificationRenderer(periodicalData.getNotificationRenderer());                        
                ((EmailPeriodicalResource)periodical).setNotificationTemplate(periodicalData.getNotificationTemplate()); 
                ((EmailPeriodicalResource)periodical).setSendEmpty(periodicalData.getSendEmpty());
 			}
 			if(!periodicalData.getName().equals(periodical.getName()))
 			{
 				coralSession.getStore().setName(periodical, periodicalData.getName());
 			}
             periodical.setLastPublished(periodicalData.getLastPublished());
             periodical.setPublishAfter(periodicalData.getPublishAfter());
             periodical.update();
 			updatePublicationTimes(coralSession, periodicalData, periodical);
         }
         catch(Exception e)
         {
             templatingContext.put("result","exception");
             templatingContext.put("trace", new StackTrace(e));
             logger.error("problem adding a periodical", e);
             return;
         }
 		PeriodicalResourceData.removeData(httpContext, null);
     	if(periodicalData.isEmailPeriodical())
     	{
     		mvcContext.setView("periodicals.EmailPeriodicals");
     	}
     	else
     	{
     	    mvcContext.setView("periodicals.Periodicals");
     	}
     	templatingContext.put("result","updated_successfully");
     }
 
 }
