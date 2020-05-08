 /*
  * Created on Nov 7, 2003
  *
  * To change the template for this generated file go to
  * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 package net.cyklotron.cms.modules.actions.periodicals;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.utils.StackTrace;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsDataFactory;
import net.cyklotron.cms.CmsTool;
 import net.cyklotron.cms.confirmation.EmailConfirmationRequestResource;
 import net.cyklotron.cms.periodicals.EmailPeriodicalResource;
 import net.cyklotron.cms.periodicals.PeriodicalResource;
 import net.cyklotron.cms.periodicals.PeriodicalsService;
 import net.cyklotron.cms.periodicals.PeriodicalsSubscriptionService;
 import net.cyklotron.cms.periodicals.UnsubscriptionInfo;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.site.SiteService;
 import net.cyklotron.cms.structure.StructureService;
 
 /**
  * @author <a href="rafal@caltha.pl">Rafal Krzewski</a>
  * @version $Id: UpdateSubscriptions.java,v 1.8 2008-10-07 14:47:54 rafal Exp $
  */
 public class UpdateSubscriptions extends BasePeriodicalsAction
 {
     private final PeriodicalsSubscriptionService periodicalsSubscriptionService;
 
     public UpdateSubscriptions(Logger logger, StructureService structureService,
         CmsDataFactory cmsDataFactory, PeriodicalsService periodicalsService,
         PeriodicalsSubscriptionService periodicalsSubscriptionService,
         SiteService siteService)
     {
         super(logger, structureService, cmsDataFactory, periodicalsService, siteService);
         this.periodicalsSubscriptionService = periodicalsSubscriptionService;
         
     }
     /**
      * {@inheritdoc}
      */
     public void execute(Context context, Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, HttpContext httpContext, CoralSession coralSession) throws ProcessingException
     {
         try
         {
             Subject rootSubject = coralSession.getSecurity().getSubject(Subject.ROOT);
             boolean subscribe = parameters.getBoolean("subscribe", false);
             String cookie = null;
             if(subscribe)
             {
                 cookie = parameters.get("cookie","");
                 if (cookie.length() == 0)
                 {
                     templatingContext.put("result", "parameter_not_found");
                     return;
                 }
                 templatingContext.put("cookie", cookie);
                 EmailConfirmationRequestResource req = periodicalsSubscriptionService
                     .getSubscriptionRequest(coralSession, cookie);
                 String email = req.getEmail();
                 if (req == null)
                 {
                     // invalid cookie - screen will complain
                     return;
                 }
                 StringTokenizer st = new StringTokenizer(req.getData(), " ");
                 Set selected = new HashSet();
                 while (st.hasMoreTokens())
                 {
                     long periodicalId = Long.parseLong(st.nextToken());
                     try
                     {
                         Resource periodical = coralSession.getStore().getResource(periodicalId);
                         selected.add(periodical);
                     }
                     catch(EntityDoesNotExistException e)
                     {
                         // periodical was deleted, ignore
                     }
                 }
                 synchronized(periodicalsService)
                 {
                     Iterator i = selected.iterator();
                     while(i.hasNext())
                     {
                         EmailPeriodicalResource periodical = (EmailPeriodicalResource)i.next();
                         subscribe(periodical, email, rootSubject);
                     }
                 }
             }
             else
             {
                 String email;
                 SiteResource site;
                 if(parameters.isDefined("token"))
                 {
                     String unsubToken = parameters.get("token");
                     UnsubscriptionInfo unsubInfo = periodicalsSubscriptionService
                         .decodeUnsubscriptionToken(unsubToken, false);
                     if(!unsubInfo.isValid())
                     {
                         throw new ProcessingException("authorization failed");
                     }
                     email = unsubInfo.getAddress();
                     PeriodicalResource periodical = (PeriodicalResource)coralSession.getStore()
                         .getResource(unsubInfo.getPeriodicalId());
                     site = periodical.getSite();
                 }
                 else
                 {
                     cookie = parameters.get("cookie");
                     templatingContext.put("cookie", cookie);
                     EmailConfirmationRequestResource req = periodicalsSubscriptionService
                         .getSubscriptionRequest(coralSession, cookie);
                     email = req.getEmail();
                 }
                 
                 Set selected = new HashSet();
                 String[] ids = parameters.getStrings("selected");
                 for(int i = 0; i < ids.length; i++)
                 {
                     long periodicalId = Long.parseLong(ids[i]);
                     try
                     {
                         Resource periodical = coralSession.getStore().getResource(periodicalId);
                         selected.add(periodical);
                     }
                     catch(EntityDoesNotExistException e)
                     {
                         // periodical was deleted, ignore
                     }
                 }
                 
                 if(cookie != null && selected.size() > 0)
                 {
                    site = CmsTool.getSite((Resource)selected.toArray()[0]);
                 }
                 else
                 {
                     site = getSite(context);
                 }                
                 
                 EmailPeriodicalResource[] subscribedArray = periodicalsSubscriptionService
                     .getSubscribedEmailPeriodicals(coralSession, site, email);
                 Set subscribed = new HashSet(Arrays.asList(subscribedArray));
                 EmailPeriodicalResource[] periodicals = periodicalsService.getEmailPeriodicals(coralSession, site);
                 for (int i = 0; i < periodicals.length; i++)
                 {
                     EmailPeriodicalResource periodical = periodicals[i];
                     if(selected.contains(periodical) && !subscribed.contains(periodical))
                     {
                         subscribe(periodical, email, rootSubject);
                     }
                     if(!selected.contains(periodical) && subscribed.contains(periodical))
                     {
                         unsubscribe(periodical, email, rootSubject);
                     }
                 }
             }
             // success
             parameters.remove("cookie");
             if(cookie != null)
             {
                 periodicalsSubscriptionService.discardSubscriptionRequest(coralSession, cookie);
             }
             templatingContext.put("result", "updated_successfully");
         }
         catch(Exception e)
         {
             templatingContext.put("result", "exception");
             templatingContext.put("trace", new StackTrace(e));
         }
     }
 
     public void subscribe(EmailPeriodicalResource periodical, String email, Subject subject)
         throws Exception
     {
         if(periodical.getAddresses().indexOf(email) < 0)
         {
             periodical.setAddresses(sortAddresses(periodical.getAddresses()+"\n"+email));
             periodical.update();
         }
     }
     
     public void unsubscribe(EmailPeriodicalResource periodical, String email, Subject subject)
         throws Exception
     {
         if(periodical.getAddresses().indexOf(email) >= 0)
         {
             String addresses = periodical.getAddresses();
             int i1 = addresses.indexOf(email);
             int i2 = addresses.indexOf('\n', i1);
             if(i2 > 0)
             {
                 addresses = addresses.substring(0,i1).concat(addresses.substring(i2+1));
             }
             else
             {
                 addresses = addresses.substring(0, i1);
             }
             periodical.setAddresses(sortAddresses(addresses));
             periodical.update();
         }
     }
     
     public boolean checkAccessRights(Context context) throws ProcessingException
     {
         return true;
     }
     
     /**
      * @{inheritDoc}
      */
     public boolean requiresAuthenticatedUser(Context context)
         throws Exception
     {
         return false;
     }
 }
