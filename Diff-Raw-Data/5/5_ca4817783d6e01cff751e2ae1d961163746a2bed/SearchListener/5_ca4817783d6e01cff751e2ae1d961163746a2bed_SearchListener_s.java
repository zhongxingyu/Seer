 package net.cyklotron.cms.search;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.coral.security.Role;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.event.EventWhiteboard;
 import org.picocontainer.Startable;
 
 import net.cyklotron.cms.security.SecurityService;
 import net.cyklotron.cms.site.BaseSiteListener;
 import net.cyklotron.cms.site.SiteCreationListener;
 import net.cyklotron.cms.site.SiteDestructionValve;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.site.SiteService;
 
 /**
  * Search Listener implementation
  *
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: SearchListener.java,v 1.5 2005-05-31 17:11:45 pablo Exp $
  */
 public class SearchListener 
     extends BaseSiteListener implements SiteCreationListener, SiteDestructionValve, Startable
 {
     private SearchService searchService;
     
     public SearchListener(Logger logger, CoralSessionFactory sessionFactory,
         SecurityService cmsSecurityService, EventWhiteboard eventWhiteboard,
         SearchService searchService)
     {
         super(logger, sessionFactory, cmsSecurityService, eventWhiteboard);
         this.searchService = searchService;
         eventWhiteboard.addListener(SiteCreationListener.class,this,null);
     }
 
     /**
      * {@inheritDoc}
      */
     public void start()
     {
     }
     
     /**
      * {@inheritDoc}
      */
     public void stop()
     {
     }
    
     // listeners implementation ////////////////////////////////////////////////////////
 
     /**
      * Called when a new site is created.
      *
      * <p>The method will be called after the site Resources are successfully
      * copied from the template.</p>
      *
      * @param template the site template name.
      * @param name the site name.
      */
     public void createSite(SiteService siteService, String template, String name)
     {
         CoralSession coralSession = sessionFactory.getRootSession();
         try
         {
             SiteResource site = siteService.getSite(coralSession, name);
             Role administrator = site.getAdministrator();
             cmsSecurityService.createRole(coralSession, administrator, 
                 "cms.search.administrator", site);
         }
         catch(Exception e)
         {
             log.error("Could not get site root: ",e);
         }
         finally
         {
             coralSession.close();
         }
     }
     
     /**
      * {@inheritDoc}
      */
     public void clearApplication(CoralSession coralSession, SiteService siteService, SiteResource site) throws Exception
     {
         Resource poolsRoot = searchService.getPoolsRoot(coralSession, site);
         if(poolsRoot != null)
         {
             deleteSiteNode(coralSession, poolsRoot);
         }
         Resource root = searchService.getSearchRoot(coralSession, site);        
         if(root == null)
         {
             return;
         }
         unbindAndDelete(coralSession,root);
     }
 
     public void clearSecurity(CoralSession coralSession, SiteService siteService, SiteResource site) throws Exception
     {
         
     }
     
     protected void unbindAndDelete(CoralSession coralSession, Resource node)
         throws Exception
     {
         Resource[] children = coralSession.getStore().getResource(node);
         for(Resource child: children)
         {
             unbindAndDelete(coralSession, child);
         }
         if(node instanceof IndexResource)
         {
             searchService.deleteIndex(coralSession, (IndexResource)node);
             return;
         }
        coralSession.getStore().deleteResource(node);
     }
 }
