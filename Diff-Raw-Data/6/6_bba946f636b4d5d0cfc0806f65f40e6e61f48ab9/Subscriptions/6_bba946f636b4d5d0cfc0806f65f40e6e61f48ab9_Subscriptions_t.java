 /*
  * Created on Nov 5, 2003
  *
  * To change the template for this generated file go to
  * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 package net.cyklotron.cms.modules.views.periodicals;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.coral.table.comparator.NameComparator;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.parameters.RequestParameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableStateManager;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.finders.MVCFinder;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.modules.views.BaseSkinableScreen;
 import net.cyklotron.cms.periodicals.EmailPeriodicalResource;
 import net.cyklotron.cms.periodicals.PeriodicalResource;
 import net.cyklotron.cms.periodicals.PeriodicalsException;
 import net.cyklotron.cms.periodicals.PeriodicalsService;
 import net.cyklotron.cms.periodicals.PeriodicalsSubscriptionService;
 import net.cyklotron.cms.periodicals.SubscriptionRequestResource;
 import net.cyklotron.cms.periodicals.UnsubscriptionInfo;
 import net.cyklotron.cms.preferences.PreferencesService;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.skins.SkinService;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.style.StyleService;
 
 /**
  * @author fil
  *
  * To change the template for this generated type comment go to
  * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 public class Subscriptions
     extends BaseSkinableScreen
 {
     /** Periodicals service */
     protected PeriodicalsService periodicalsService;
     private final PeriodicalsSubscriptionService periodicalsSubscriptionService;    
 
     public Subscriptions(org.objectledge.context.Context context, Logger logger,
         PreferencesService preferencesService, CmsDataFactory cmsDataFactory,
         StructureService structureService, StyleService styleService, SkinService skinService,
         MVCFinder mvcFinder, TableStateManager tableStateManager,
         PeriodicalsService periodicalsService,
         PeriodicalsSubscriptionService periodicalsSubscriptionService)
     {
         super(context, logger, preferencesService, cmsDataFactory, structureService, styleService,
                         skinService, mvcFinder, tableStateManager);
         this.periodicalsService = periodicalsService;
         this.periodicalsSubscriptionService = periodicalsSubscriptionService;
     }
     
     public void prepareDefault(Context context)
         throws ProcessingException
     {
         Parameters parameters = RequestParameters.getRequestParameters(context);
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         HttpContext httpContext = HttpContext.getHttpContext(context);
         I18nContext i18nContext = I18nContext.getI18nContext(context);
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
         try
         {
             EmailPeriodicalResource[] periodicals = periodicalsService.
                 getEmailPeriodicals(coralSession, getSite());
             List list = Arrays.asList(periodicals);
             Collections.sort(list, new NameComparator(i18nContext.getLocale()));                
             templatingContext.put("periodicals", list);
         }
         catch(PeriodicalsException e)
         {
             throw new ProcessingException("failed to retrieve data", e);
         }
     }
     
     public void prepareNewTicket(Context context)
         throws ProcessingException
     {
         RequestParameters parameters = RequestParameters.getRequestParameters(context);
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
         String unsubToken = parameters.get("unsub", "");
         unsubToken = parameters.get("unsub_all", unsubToken);
         if(unsubToken.length() > 0)
         {
             try
             {
                 UnsubscriptionInfo unsubsriptionInfo = periodicalsSubscriptionService
                     .decodeUnsubscriptionToken(unsubToken, false);
                 if(unsubsriptionInfo.isValid())
                 {
                     throw new ProcessingException("token is valid, what are we doing here?");
                 }
                 else
                 {
                     templatingContext.put("email", unsubsriptionInfo.getAddress());
                 }
             }
             catch(PeriodicalsException e)
             {
                 throw new ProcessingException("failed to decode unsubscription info", e);
             }
         }
     }
     
     public void prepareTicketSent(Context context)
     {
         // does nothing
     }
     
     public void prepareInvalidTicket(Context context)
     {
         // does nothing
     }
     
     public void prepareEdit(Context context)
         throws ProcessingException
     {
         Parameters parameters = RequestParameters.getRequestParameters(context);
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         HttpContext httpContext = HttpContext.getHttpContext(context);
         I18nContext i18nContext = I18nContext.getI18nContext(context);
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
         try
         {
             String cookie = parameters.get("cookie");
             templatingContext.put("cookie", cookie);
             SiteResource site = getSite();
             SubscriptionRequestResource req = periodicalsSubscriptionService
                 .getSubscriptionRequest(coralSession, cookie);
             templatingContext.put("email", req.getEmail());
             List periodicals = Arrays.asList(periodicalsService.getEmailPeriodicals(coralSession, site));
             Collections.sort(periodicals, new NameComparator(i18nContext.getLocale()));                
             List selectedList = Arrays.asList(periodicalsSubscriptionService
                 .getSubscribedEmailPeriodicals(coralSession, site, req.getEmail()));
             Set selected = new HashSet(selectedList);
             templatingContext.put("periodicals", periodicals);
             templatingContext.put("selected", selected);
         }
         catch(Exception e)
         {
             throw new ProcessingException("failed to retrieve information", e);
         }
     }
     
     public void prepareConfirm(Context context)
         throws ProcessingException
     {
         Parameters parameters = RequestParameters.getRequestParameters(context);
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         HttpContext httpContext = HttpContext.getHttpContext(context);
         I18nContext i18nContext = I18nContext.getI18nContext(context);
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
         try
         {
             String cookie = parameters.get("cookie", "");
             if(cookie.length() > 0)
             {
                 templatingContext.put("cookie", cookie);
                 SubscriptionRequestResource req = periodicalsSubscriptionService
                 .getSubscriptionRequest(coralSession, cookie);
                 templatingContext.put("email", req.getEmail());
                 StringTokenizer st = new StringTokenizer(req.getItems(), " ");
                 List selected = new ArrayList();
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
                 templatingContext.put("subscribe", "true");
                 Collections.sort(selected, new NameComparator(i18nContext.getLocale()));                
                 templatingContext.put("selected", selected);
             }
             else
             {
                 String unsubToken = parameters.get("unsub", "");
                 unsubToken = parameters.get("unsub_all", unsubToken);
                 if(unsubToken.length() > 0)
                 {
                     UnsubscriptionInfo unsubsriptionInfo = periodicalsSubscriptionService
                         .decodeUnsubscriptionToken(unsubToken, false);
                     templatingContext.put("email", unsubsriptionInfo.getAddress());
                     if(unsubsriptionInfo.isValid())
                     {
                         PeriodicalResource periodical = (PeriodicalResource)coralSession.getStore()
                             .getResource(unsubsriptionInfo.getPeriodicalId());
                         List selected;
                         List selectedInv;
                         if(parameters.isDefined("unsub_all"))
                         {
                             selected = Arrays.asList(periodicalsSubscriptionService
                                 .getSubscribedEmailPeriodicals(coralSession, periodical.getSite(),
                                     unsubsriptionInfo.getAddress()));
                             Collections.sort(selected, new NameComparator(i18nContext.getLocale()));                
                             selectedInv = Collections.EMPTY_LIST;
                         }
                         else
                         {
                             selected = Collections.singletonList(periodical);
                            selectedInv = new ArrayList(Arrays
                                .asList(periodicalsSubscriptionService
                                    .getSubscribedEmailPeriodicals(coralSession, periodical
                                        .getSite(), unsubsriptionInfo.getAddress())));
                             selectedInv.remove(periodical);
                         }
                         templatingContext.put("subscribe", null);
                         templatingContext.put("selected", selected);
                         templatingContext.put("selectedInv", selectedInv);
                         templatingContext.put("token", unsubToken);                        
                     }
                     else
                     {
                         throw new ProcessingException("token is not valid, what are we doing here?");                        
                     }
                 }
                 else
                 {
                     throw new ProcessingException("cookie nor unsub(_all) parameters not found");
                 }
             }
         }
         catch(Exception e)
         {
             throw new ProcessingException("failed to retrieve information", e);
         }
     }
     
     public String getState()
         throws ProcessingException
     {
         Parameters parameters = RequestParameters.getRequestParameters(context);
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         HttpContext httpContext = HttpContext.getHttpContext(context);
         I18nContext i18nContext = I18nContext.getI18nContext(context);
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
         
         if("ticket_sent".equals(templatingContext.get("result")))
         {
             return "TicketSent";
         }
         String cookie = parameters.get("cookie","");
         if(cookie.length() > 0)
         {
             try
             {
                 SubscriptionRequestResource req = periodicalsSubscriptionService
                     .getSubscriptionRequest(coralSession, cookie);
                 if (req == null)
                 {
                     return "InvalidTicket";
                 }
                 else if (req.getItems() == null)
                 {
                     return "Edit";
                 }
                 else
                 {
                     return "Confirm";
                 }
             }
             catch(Exception e)
             {
                 throw new ProcessingException("failed to validate cookie", e);
             }
         }
         String unsubToken = parameters.get("unsub", "");
         unsubToken = parameters.get("unsub_all", unsubToken);
         if(unsubToken.length() > 0)
         {
             try
             {
                 UnsubscriptionInfo unsubscriptionInfo = periodicalsSubscriptionService
                     .decodeUnsubscriptionToken(unsubToken, false);
                 if(unsubscriptionInfo.isValid())
                 {
                     return "Confirm";
                 }
                 else
                 {
                     return "NewTicket";
                 }
             }
             catch(PeriodicalsException e)
             {
                 throw new ProcessingException("failed to decode unsubscription info", e);
             }
         }
         if(parameters.isDefined("request_ticket"))
         {
             return "NewTicket";
         }
         return "Default";
     }
 }
