 package net.cyklotron.cms.modules.jobs.syndication;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.ComponentInitializationError;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.event.CoralEventWhiteboard;
 import org.objectledge.coral.event.ResourceDeletionListener;
 import org.objectledge.coral.schema.ResourceClass;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.scheduler.Job;
 
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.site.SiteService;
 import net.cyklotron.cms.syndication.IncomingFeedResource;
 import net.cyklotron.cms.syndication.IncomingFeedsManager;
 import net.cyklotron.cms.syndication.SyndicationException;
 import net.cyklotron.cms.syndication.SyndicationService;
 
 /**
  * A job that updates incoming feeds defined for the sites.
  *
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: UpdateIncomingFeeds.java,v 1.4 2008-11-13 15:31:28 rafal Exp $
  */
 public class UpdateIncomingFeeds
 extends Job
 implements ResourceDeletionListener
 {
     // instance variables ////////////////////////////////////////////////////
 
     /** logging service */
     protected Logger log;
 
     private SiteService siteService;
     private ResourceClass incomingFeedResourceClass;
     private SyndicationService syndicationService;
     private CoralSessionFactory coralSessionFactory;
 
     /** system root subject */
     protected Subject rootSubject;
 
     /** deleted resources ids */
     private Set deletedFeedsIds = new HashSet();
 
 
     // initialization ///////////////////////////////////////////////////////
 
     public UpdateIncomingFeeds(Logger log, CoralSessionFactory coralSessionFactory,
         SiteService siteService, SyndicationService syndicationService)
     {
         this.log = log;
         this.coralSessionFactory = coralSessionFactory;
         this.syndicationService = syndicationService; 
         this.siteService = siteService;
 
         CoralSession coralSession = coralSessionFactory.getRootSession();
         try
         {
             incomingFeedResourceClass = coralSession.getSchema()
                                 .getResourceClass(IncomingFeedResource.CLASS_NAME);
         }
         catch(EntityDoesNotExistException e)
         {
             throw new ComponentInitializationError("Could not find '"
                 + IncomingFeedResource.CLASS_NAME + "' resource class");
         }
         finally
         {
             coralSession.close();
         }
         
     }
 
 
     // Job interface ////////////////////////////////////////////////////////
 
     @Override
     public void run(String[] arguments)
     {
         IncomingFeedsManager manager = syndicationService.getIncomingFeedsManager();
         CoralSession coralSession = coralSessionFactory.getRootSession();
        CoralEventWhiteboard coralEventWhiteboard = coralSession.getEvent();
 
         try
         {
             // prepare for feeds updates
             deletedFeedsIds.clear();
             coralEventWhiteboard.addResourceDeletionListener(this, incomingFeedResourceClass);
     
             Calendar cal = Calendar.getInstance();
             Date now = cal.getTime();
     
             SiteResource[] sites = siteService.getSites(coralSession);
             for(int i=0; i<sites.length; i++)
             {
                 IncomingFeedResource[] feeds = null;
                 try
                 {
                     feeds = manager.getFeeds(coralSession, sites[i]);
     
                     for(int j=0; j<feeds.length; j++)
                     {
                         IncomingFeedResource feed = feeds[j];
     
                         // check if feed needs to be refreshed
                         boolean makeRefresh = (feed.getFailedUpdates() > 0)
                                               || (feed.getLastUpdate() == null)
                                               || (feed.getContents() == null);
                         if(!makeRefresh)
                         {
                             // calculate next update time
                             cal.setTime(feed.getLastUpdate());
                             cal.add(Calendar.MINUTE, feed.getInterval());
                             Date nextUpdate = cal.getTime();
     
                             makeRefresh = nextUpdate.before(now);
                         }
     
                         if(makeRefresh)
                         {
                             synchronized(feed)
                             {
                                 if(!deletedFeedsIds.contains(feed.getIdObject()))
                                 {
                                     try
                                     {
                                         syndicationService.getIncomingFeedsManager().refreshFeed(coralSession, feed);
                                     }
                                     catch(Exception e)
                                     {
                                         log.error("Could not refresh feed '"+feed.getName()+
                                             "' for site '"+sites[i].getName()+"'", e);
                                     }
                                 }
                             }
                         }
                     }
                 }
                 catch(SyndicationException e)
                 {
                     log.error("Cannot get feeds for the site '"+sites[i].getName()+"'", e);
                 }
             }
     
             // cleanup after feed updates
             coralEventWhiteboard.removeResourceDeletionListener(this, incomingFeedResourceClass);
             deletedFeedsIds.clear();
         }
         finally
         {
             coralSession.close();
         }
     }
 
     /** Called when <code>Resource</code>'s data change.
      *
      * @param Resource the resource that changed.
      *
      */
     public void resourceDeleted(Resource resource)
     {
         synchronized(deletedFeedsIds)
         {
             deletedFeedsIds.add(resource.getIdObject());
         }
     }
 }
