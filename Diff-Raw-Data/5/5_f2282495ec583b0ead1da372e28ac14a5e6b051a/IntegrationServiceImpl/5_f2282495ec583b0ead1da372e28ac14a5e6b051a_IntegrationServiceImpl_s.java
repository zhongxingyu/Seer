 package net.cyklotron.cms.integration.internal;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.ComponentInitializationError;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.event.ResourceChangeListener;
 import org.objectledge.coral.event.ResourceClassChangeListener;
 import org.objectledge.coral.event.ResourceClassInheritanceChangeListener;
 import org.objectledge.coral.event.ResourceCreationListener;
 import org.objectledge.coral.event.ResourceDeletionListener;
 import org.objectledge.coral.schema.ResourceClass;
 import org.objectledge.coral.schema.ResourceClassInheritance;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.coral.store.Resource;
 import org.picocontainer.Startable;
 
 import net.cyklotron.cms.integration.ApplicationResource;
 import net.cyklotron.cms.integration.ComponentResource;
 import net.cyklotron.cms.integration.ComponentStateResource;
 import net.cyklotron.cms.integration.IntegrationService;
 import net.cyklotron.cms.integration.ResourceClassResource;
 import net.cyklotron.cms.integration.ScreenResource;
 import net.cyklotron.cms.integration.ScreenStateResource;
 
 /**
  * @author <a href="mailto:rkrzewsk@caltha.pl">Rafal Krzewski</a>
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
  * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: IntegrationServiceImpl.java,v 1.9 2005-06-15 05:52:05 pablo Exp $
  */
 public class IntegrationServiceImpl
     implements IntegrationService, Startable,
     ResourceChangeListener, ResourceDeletionListener, ResourceCreationListener,
     ResourceClassInheritanceChangeListener, ResourceClassChangeListener
 {
     // instance variables ////////////////////////////////////////////////////
 
     /** logging facility */
     private Logger log;
 
     /** coral session factory */
     private CoralSessionFactory sessionFactory;
     
     /** the application data root node. */
     protected Resource integrationRoot;
     
     private HashMap<String,ApplicationResource> applicationCache;
     
     private HashMap<String,HashMap<String, ComponentResource>> componentsCache;
     
     private HashMap<String,HashMap<String, ScreenResource>> screensCache;
     
     private HashMap<String,HashMap<String, ResourceClassResource>> resourceClassesCache;
     
     private HashMap<String, ComponentResource> componentsByNameCache;
     
     private HashMap<String, ScreenResource> screensByNameCache;
     
     private HashMap<String, ResourceClassResource> resourceClassesByNameCache;
     
     private HashMap<String, String> parentClassNameMap;
     
     private boolean initialized = false;
     
     // initialization ////////////////////////////////////////////////////////
 
     /**
      * Initializes the service.
      */
     public IntegrationServiceImpl(Logger logger, CoralSessionFactory sessionFactory)
     {
         log = logger;
         this.sessionFactory = sessionFactory;
         applicationCache = new HashMap<String, ApplicationResource>();
         componentsCache = new HashMap<String,HashMap<String, ComponentResource>>();
         screensCache = new HashMap<String,HashMap<String, ScreenResource>>();
         resourceClassesCache = new HashMap<String,HashMap<String, ResourceClassResource>>();
         componentsByNameCache = new HashMap<String, ComponentResource>();
         screensByNameCache = new HashMap<String, ScreenResource>();
         resourceClassesByNameCache = new HashMap<String, ResourceClassResource>();
         parentClassNameMap = new HashMap<String, String>();
         
         CoralSession coralSession = sessionFactory.getRootSession();
         try
         {
             coralSession.getEvent().addResourceCreationListener(this, null);
             coralSession.getEvent().addResourceChangeListener(this, null);
             coralSession.getEvent().addResourceDeletionListener(this, null);
         }
         finally
         {
             coralSession.close();
         }
     }
 
     // public interface //////////////////////////////////////////////////////
     
     
     private synchronized void loadCache(CoralSession coralSession, boolean reload)
     {
         if(reload || !initialized)
         {
             applicationCache = new HashMap<String, ApplicationResource>();
             componentsCache = new HashMap<String,HashMap<String, ComponentResource>>();
             screensCache = new HashMap<String,HashMap<String, ScreenResource>>();
             resourceClassesCache = new HashMap<String,HashMap<String, ResourceClassResource>>();
             componentsByNameCache = new HashMap<String, ComponentResource>();
             screensByNameCache = new HashMap<String, ScreenResource>();
             resourceClassesByNameCache = new HashMap<String, ResourceClassResource>();
             parentClassNameMap = new HashMap<String, String>();
             
             Resource[] apps = coralSession.getStore().
                 getResource(getIntegrationRoot(coralSession));
             for(Resource r: apps)
             {
                 ApplicationResource app = (ApplicationResource)r;
                 HashMap<String, ComponentResource> cMap = new HashMap<String, ComponentResource>();
                 HashMap<String, ScreenResource> sMap = new HashMap<String, ScreenResource>();
                 HashMap<String, ResourceClassResource> rMap = new HashMap<String, ResourceClassResource>();
                 applicationCache.put(app.getName(), app);
                 componentsCache.put(app.getName(), cMap);
                 screensCache.put(app.getName(), sMap);
                 resourceClassesCache.put(app.getName(), rMap);
                 Resource[] res = coralSession.getStore().getResource(app, "components");
                 if(res.length > 0)
                 {
                     res = coralSession.getStore().getResource(res[0]);
                     for(Resource c: res)
                     {
                         ComponentResource cc = (ComponentResource)c;
                         cMap.put(c.getName(), cc);
                         componentsByNameCache.put(cc.getComponentName(), cc);
                     }
                 }
                 res = coralSession.getStore().getResource(app, "screens");
                 if(res.length > 0)
                 {
                     res = coralSession.getStore().getResource(res[0]);
                     for(Resource s: res)
                     {
                         ScreenResource ss = (ScreenResource)s;
                         sMap.put(s.getName(), (ScreenResource)s);
                         screensByNameCache.put(ss.getScreenName(), ss);
                     }
                 }
                 res = coralSession.getStore().getResource(app, "resources");
                 if(res.length > 0)
                 {
                     res = coralSession.getStore().getResource(res[0]);
                     for(Resource s: res)
                     {
                         ResourceClassResource ss = (ResourceClassResource)s;
                         rMap.put(s.getName(), (ResourceClassResource)s);
                         resourceClassesByNameCache.put(ss.getName(), ss);
                         loadParentClassMap(coralSession, ss.getName());
                     }
                 }
             }
             initialized = true;
         }
     }
     
     private void loadParentClassMap(CoralSession coralSession, String target)
     {
         ResourceClass rc = null;
         try
         {
             rc = coralSession.getSchema().getResourceClass(target);
         }
         catch(EntityDoesNotExistException e)
         {
             return;
         }
         ResourceClassInheritance[] inheritance = rc.getInheritance();
         for(int i = 0; i < inheritance.length; i++)
         {
             if(inheritance[i].getParent().equals(rc))
             {
                 String name = inheritance[i].getChild().getName();
                 parentClassNameMap.put(name, target);
                 loadParentClassMap(coralSession, name);
             }
         }
     }
     
     /**
      * Returns the descriptors of all applications deployed in the system.
      */
     public ApplicationResource[] getApplications(CoralSession coralSession)
     {
         loadCache(coralSession, false);
         ApplicationResource[] apps = new ApplicationResource[applicationCache.size()];
         int i = 0;
         for(ApplicationResource app: applicationCache.values())
         {
             apps[i] = app;
             i++;
         }
         return apps;
     }
 
     /**
      * Return the ApplicationResource for app
      * 
      * @param name the application name.
      * @return the application resource.
      */
     public ApplicationResource getApplication(CoralSession coralSession, String name)
     {
         loadCache(coralSession, false);
         return applicationCache.get(name);
     }
     
     /**
      * Returns the descriptors of all components provided by an application.
      *
      * @param app the application.
      */
     public ComponentResource[] getComponents(CoralSession coralSession, ApplicationResource app)
     {
         loadCache(coralSession, false);
         HashMap<String, ComponentResource> cMap = componentsCache.get(app.getName());
         if(cMap == null || cMap.size()==0)
         {
             return new ComponentResource[0];
         }
         ComponentResource[] cs = new ComponentResource[cMap.size()];
         int i = 0;
         for(ComponentResource c: cMap.values())
         {
             cs[i] = c;
             i++;
         }
         return cs;
     }
 
     /**
      * Returns a named component from a specific application.
      * 
      * @param app the application.
      * @param name the component name. 
      * @return the component resource.
      */
     public ComponentResource getComponent(CoralSession coralSession, ApplicationResource app, String name)
     {
         loadCache(coralSession, false);
         HashMap<String, ComponentResource> cMap = componentsCache.get(app.getName());
         if(cMap == null || cMap.size()==0)
         {
             return null;
         }
         return cMap.get(name);
     }
     
     /**
      * Returns the descirptors of all components deployed in the system.
      */
     public ComponentResource[] getComponents(CoralSession coralSession)
     {
         loadCache(coralSession, false);
         ComponentResource[] cs = new ComponentResource[componentsByNameCache.size()];
         int i = 0;
         for(ComponentResource c: componentsByNameCache.values())
         {
             cs[i] = c;
             i++;
         }
         return cs;
     }
     
     /**
      * Returns the application a component belongs to.
      *
      * @param comp the component.
      */
     public ApplicationResource getApplication(CoralSession coralSession, ComponentResource comp)
     {
         Resource p = comp.getParent();
         while(p != null && !(p instanceof ApplicationResource))
         {
             p = p.getParent();
         }
         return (ApplicationResource)p;
     }
 
     /**
      * Returns the component with the given app and component name.
      *
      * @param app the application parameter.
      * @param name the component name.
      * @return the component, or <code>null</code> if not found.
      */
     public ComponentResource getComponent(CoralSession coralSession, String app, String name)
     {
         loadCache(coralSession, false);
         return componentsByNameCache.get(name);
     }
 
     /**
      * Get defined states of a component.
      *
      * @param component the component.
      * @return an array of defined states, or empty array if component is
      *         stateless. 
      */
     public ComponentStateResource[] getComponentStates(CoralSession coralSession, ComponentResource component)
     {
         Resource[] res = coralSession.getStore().getResource(component);
         ArrayList<ComponentStateResource> temp = new ArrayList<ComponentStateResource>();
         for(int i=0; i<res.length; i++)
         {
             if(res[i] instanceof ComponentStateResource)
             {
                 temp.add((ComponentStateResource)res[i]);
             }
         }
         ComponentStateResource[] result = new ComponentStateResource[temp.size()];
         temp.toArray(result);
         return result;
     }
 
     /**
      * Checks if a component has a given state defined.
      *
      * @param component the component.
      * @param state the state.
      */
     public boolean hasState(CoralSession coralSession, ComponentResource component, String state)
     {
         Resource[] res = coralSession.getStore().getResource(component, state);
         return (res.length == 1 && res[0] instanceof ComponentStateResource);
     }
 
     /**
      * Returns the descriptors of all screens provided by an application.
      *
      * @param app the application.
      */
     public ScreenResource[] getScreens(CoralSession coralSession, ApplicationResource app)
     {
         loadCache(coralSession, false);
         HashMap<String, ScreenResource> sMap = screensCache.get(app.getName());
         if(sMap == null || sMap.size()==0)
         {
             return new ScreenResource[0];
         }
         ScreenResource[] cs = new ScreenResource[sMap.size()];
         int i = 0;
         for(ScreenResource c: sMap.values())
         {
             cs[i] = c;
             i++;
         }
         return cs;
     }
 
     /**
      * Returns a named component from a specific application.
      * 
      * @param app the application.
      * @param name the component name. 
      * @return the component resource.
      */
     public ScreenResource getScreen(CoralSession coralSession, ApplicationResource app, String name)
     {
         loadCache(coralSession, false);
         HashMap<String, ScreenResource> sMap = screensCache.get(app.getName());
         if(sMap == null || sMap.size()==0)
         {
             return null;
         }
         return sMap.get(name);
     }
     
     /**
      * Returns the descirptors of all screens deployed in the system.
      */
     public ScreenResource[] getScreens(CoralSession coralSession)
     {
         loadCache(coralSession, false);
         ScreenResource[] cs = new ScreenResource[screensByNameCache.size()];
         int i = 0;
         for(ScreenResource c: screensByNameCache.values())
         {
             cs[i] = c;
             i++;
         }
         return cs;
     }
     
     /**
      * Returns the application a screen belongs to.
      *
      * @param comp the screen.
      */
     public ApplicationResource getApplication(CoralSession coralSession, ScreenResource comp)
     {
         Resource p = comp.getParent();
         while(p != null && !(p instanceof ApplicationResource))
         {
             p = p.getParent();
         }
         return (ApplicationResource)p;
     }
 
     /**
      * Returns the screen with the given app and screen name.
      *
      * @param app the application parameter.
      * @param name the screen name.
      * @return the screen, or <code>null</code> if not found.
      */
     public ScreenResource getScreen(CoralSession coralSession, String app, String name)
     {
         loadCache(coralSession, false);
         return screensByNameCache.get(name);
     }
 
     /**
      * Get defined states of a screen.
      *
      * @param screen the screen.
      * @return an array of defined states, or empty array if screen is
      *         stateless. 
      */
     public ScreenStateResource[] getScreenStates(CoralSession coralSession, ScreenResource screen)
     {
         Resource[] res = coralSession.getStore().getResource(screen);
         ArrayList<ScreenStateResource> temp = new ArrayList<ScreenStateResource>();
         for(int i=0; i<res.length; i++)
         {
             if(res[i] instanceof ScreenStateResource)
             {
                 temp.add((ScreenStateResource)res[i]);
             }
         }
         ScreenStateResource[] result = new ScreenStateResource[temp.size()];
         temp.toArray(result);
         return result;
     }
 
     /**
      * Checks if a screen has a given state defined.
      *
      * @param screen the screen.
      * @param state the state.
      */
     public boolean hasState(CoralSession coralSession, ScreenResource screen, String state)
     {
         Resource[] res = coralSession.getStore().getResource(screen, state);
         return (res.length == 1 && res[0] instanceof ScreenStateResource);
     }
 
     public ResourceClassResource getResourceClass(CoralSession coralSession, String name)
     {
         loadCache(coralSession, false);
         return resourceClassesByNameCache.get(name);
     }
 
     /**
      * Returns the descirptors of all resource classes registered in the system.
      */
     public ResourceClassResource[] getResourceClasses(CoralSession coralSession)
     {
         loadCache(coralSession, false);
        ResourceClassResource[] cs = new ResourceClassResource[componentsByNameCache.size()];
         int i = 0;
         for(ResourceClassResource c: resourceClassesByNameCache.values())
         {
             cs[i] = c;
             i++;
         }
         return cs;
     }
     
     /**
      * Returns the resource class info with the given app and resource class name.
      *
      * @param rc the resource class.
      * @return the resource class, or <code>null</code> if not found.
      */
     public ResourceClassResource getResourceClass(CoralSession coralSession, ResourceClass rc)
     {
         loadCache(coralSession, false);
         ResourceClassResource rcr = getResourceClass(coralSession, rc.getName());
         if(rcr != null)
         {
             return rcr;
         }
         String target = parentClassNameMap.get(rc.getName()); 
         while(target != null)
         {
             rcr = getResourceClass(coralSession, target);
             if(rcr != null)
             {
                 return rcr;
             }
             target = parentClassNameMap.get(target);
         }
         return null;
     }
 
     /**
      * Returns the descirptors of all resource classes provided by an application.
      *
      * @param applicationResource the application resource.
      */
     public ResourceClassResource[] getResourceClasses(CoralSession coralSession, ApplicationResource applicationResource)
     {
         loadCache(coralSession, false);
         HashMap<String, ResourceClassResource> cMap = resourceClassesCache.get(applicationResource.getName());
         if(cMap == null || cMap.size()==0)
         {
             return new ResourceClassResource[0];
         }
         ResourceClassResource[] cs = new ResourceClassResource[cMap.size()];
         int i = 0;
         for(ResourceClassResource c: cMap.values())
         {
             cs[i] = c;
             i++;
         }
         return cs;
     }
     
     /**
      * Returns the resource class fot the given resource class resource.
      *
      * @param rcr the resource class resource.
      * @return the resource class for this resource class resource or <code>null</code>..
      */
     public ResourceClass getResourceClass(CoralSession coralSession, ResourceClassResource rcr)
     {
         try
         {
             return coralSession.getSchema().getResourceClass(rcr.getName());
         }
         catch(EntityDoesNotExistException e)
         {
             return null;
         }
     }
 
     public Map initResourceClassSelection(CoralSession coralSession, String items, String state)
     {
         if(items == null || items.length() == 0)
         {
             return new HashMap();
         }
         StringTokenizer st = new StringTokenizer(items, " ");
         Map<ResourceClassResource, String> map = new HashMap<ResourceClassResource, String>();
         while(st.hasMoreTokens())
         {
             ResourceClassResource res = getResourceClass(coralSession, st.nextToken());
             if(res != null)
             {
                 map.put(res, state);
             }
         }
         return map;
     }
 
     /** 
      * Return the schema role root, null if schema root does not exist.
      *
      * @param rc the resource class.
      * @return the schema role root.
      */
     public Resource getSchemaRoleRoot(CoralSession coralSession, ResourceClass rc)
     {
         ResourceClassResource rcr = getResourceClass(coralSession, rc);
         if(rcr == null)
         {
             return null;
         }
         Resource[] resources = coralSession.getStore().getResource(rcr,"roles");
         if(resources.length == 0)
         {
             ResourceClass resourceClass = null;
             try
             {
                 resourceClass = coralSession.getSchema().getResourceClass(rcr.getName());
                 ResourceClass[] parents = resourceClass.getParentClasses();
                 for(int i = 0; i < parents.length; i++)
                 {
                     Resource schema = getSchemaRoleRoot(coralSession, parents[i]);
                     if(schema != null)
                     {
                         return schema;
                     }
                 }
             }
             catch(EntityDoesNotExistException e)
             {
                 log.error("It should never happen, see sources for more details",e);
             }
             return null;
         }
         return resources[0];
     }
     
     public Resource getIntegrationRoot(CoralSession coralSession)
     {
         if(integrationRoot == null)
         {
             Resource res[] = coralSession.getStore().
             getResourceByPath("/cms/applications");
             if(res.length == 1)
             {   
                 integrationRoot = res[0];
             }
             else
             {
                 throw new ComponentInitializationError("failed to lookup /cms/applications node");
             }
         }
         return integrationRoot;
     }
     
     public void resourceCreated(Resource resource)
     {
         checkChange(resource, false);
     }
 
     public void resourceChanged(Resource resource, Subject subject)
     {
         checkChange(resource, false);
     }
 
     public void resourceDeleted(Resource resource)
     {
         checkChange(resource, false);
     }
     
     public void resourceClassChanged(ResourceClass rc)
     {
         checkChange(null, true);
     }
     
     public void inheritanceChanged(ResourceClassInheritance rci, boolean inheritance)
     {
         checkChange(null, true);
     }
     
     public void checkChange(Resource resource, boolean force)
     {
         if(force ||
            resource instanceof ComponentResource ||
            resource instanceof ScreenResource ||
            resource instanceof ApplicationResource ||
            resource instanceof ResourceClassResource)
         {
             CoralSession coralSession = sessionFactory.getRootSession();
             try
             {
                 loadCache(coralSession, true);
             }
             finally
             {
                 coralSession.close();
             }
         }
     }
     
 
     public void start()
     {
     }
     
     public void stop()
     {
         
     }
 }
