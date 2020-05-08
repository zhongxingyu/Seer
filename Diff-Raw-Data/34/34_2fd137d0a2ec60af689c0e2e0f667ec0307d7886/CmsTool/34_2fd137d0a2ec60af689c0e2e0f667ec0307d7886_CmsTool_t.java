 package net.cyklotron.cms;
 import net.cyklotron.cms.integration.IntegrationService;
 import net.cyklotron.cms.integration.ResourceClassResource;
 import net.cyklotron.cms.preferences.PreferencesService;
 import net.cyklotron.cms.site.SiteException;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.structure.NavigationNodeResource;
 import net.cyklotron.cms.structure.NavigationNodeResourceImpl;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.authentication.AuthenticationException;
 import org.objectledge.authentication.UserManager;
 import org.objectledge.context.Context;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.schema.AttributeDefinition;
 import org.objectledge.coral.schema.ResourceClass;
 import org.objectledge.coral.security.Permission;
 import org.objectledge.coral.security.Role;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.parameters.Parameters;
 
 
 /**
  * A context tool used for cms application.
  *
  * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
  * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CmsTool.java,v 1.4 2005-01-27 04:59:12 pablo Exp $
  */
 public class CmsTool
 {
     /** logging service */
     private Logger log;
 
     /** preferences service */
     private PreferencesService preferencesService;
 
     /** authentication service */
     private UserManager userManager;
 
     /** integration service */
     private IntegrationService integrationService;
 
     /** user data */
     private UserData userData;
     
     /** context */
     private Context context;
     
     private CmsDataFactory cmsDataFactory;
     // initialization ////////////////////////////////////////////////////////
 
     public CmsTool(Context context, Logger logger, PreferencesService preferencesService,
         UserManager userManager, IntegrationService integrationService,
         CmsDataFactory cmsDataFactory)
     {
         this.context = context;
         this.log = logger;
         this.preferencesService = preferencesService;
         this.userManager = userManager;
         this.integrationService = integrationService;
         this.cmsDataFactory = cmsDataFactory;
     }
 
     // public interface ///////////////////////////////////////////////////////
 
     // subjects ///////////////////////////////////////////////////////////////
 
     /**
      * Return the user data.
      *
      * @return the subject.
      */
     public UserData getUserData()
     {
         if(userData == null)
         {
             userData = new UserData(context, log, preferencesService,
                 userManager, null);
         }
         return userData;
     }
 
     /**
      * Return the user data.
      *
      * @return the subject.
      */
     public UserData getUserData(Subject subject)
     {
         return new UserData(context, log, preferencesService,
             userManager, subject);
     }    
     
     /**
      * Return current logged subject.
      *
      * @return the subject.
      */
     public Subject getSubject()
     {
         return getUserData().getSubject();
     }
     
     /**
      * Return the subject with the given name.
      */
     public Subject getSubject(String dn)
         throws Exception
     {
         return getCoralSession(context).getSecurity().getSubject(dn);
     }
     
     /**
      * Returns the login of the subject with given dn.
      */
     public String getSubjectLogin(String dn)
         throws Exception
     {
         return userManager.getLogin(dn);
     }
     
     /**
      * Returns the dn of the subject with given login.
      */
     public String getSubjectName(String login)
     {
     	try
     	{
             return userManager.getUserByLogin(login).getName();
     	}
     	catch(AuthenticationException e)
     	{
     		return null;
     	}
     }
 
     /**
      * Return the anonymous subject.
      *
      */
     public Subject getAnonymousSubject()
         throws Exception
     {
         return getCoralSession(context).getSecurity().getSubject(
             Subject.ANONYMOUS);
     }
     
 
     // role & permission checking ////////////////////////////////////////////
 
     /**
      * checks whether subject has a role.
      *
      * @param subject the subject.
      * @param role the role.
      */
     public boolean hasRole(Subject subject, String role)
         throws Exception
     {
        if(subject == null)
        {
            log.error("Emtpy null has no rights");
            return false;
        }
         try
         {
            Role roleEntity = getCoralSession(context).getSecurity().getUniqueRole(role);
             return subject.hasRole(roleEntity);
         }
         catch(Exception e)
         {
             log.error("CmsTool",e);
             throw e;
         }
     }
     
     /**
      * checks right to the resource.
      *
      * @param subject the subject.
      * @param resource the resource.
      * @param permission the permission.
      */
     public boolean hasPermission(Subject subject, Resource resource, String permission)
         throws Exception
     {
         try
         {
            Permission permissionEntity = getCoralSession(context).getSecurity().
                 getUniquePermission(permission);
             return subject.hasPermission(resource, permissionEntity);
         }
         catch(Exception e)
         {
             log.error("CmsTool",e);
             throw e;
         }
     }
 
     // application data access ///////////////////////////////////////////////
 
     /**
      * Returns the aplication data root.
      *
      * @param site the site resource.
      * @return the application resource. 
      */
     public Resource getApplication(String application)
         throws Exception
     {
         try
         {
             SiteResource site = cmsDataFactory.getCmsData(context).getSite();
             return getApplication(site,application);
         }
         catch(Exception e)
         {
             log.error("CmsTool exception ",e);
             throw e;
         }
     }
 
     /**
      * Returns the application data root.
      *
      * @param site the site resource.
      * @return the application resource. 
      */
     public Resource getApplication(SiteResource site, String application)
         throws Exception
     {
         Resource[] match = getCoralSession(context).getStore().
             getResourceByPath(site.getPath()+"/applications/"+application);
         if(match.length != 1)
         {
             throw new SiteException("application "+application+" not found");
         }
         return match[0];
     }
 
     // preferences ///////////////////////////////////////////////////////////
     
     public NavigationNodeResource getNodePreferenceOrigin(NavigationNodeResource node, 
                                                           String preference)
     {
         return preferencesService.getNodePreferenceOrigin(node, preference);
     }
     
     public Parameters getCombinedNodePreferences(NavigationNodeResource node) 
     {
         return preferencesService.getCombinedNodePreferences(node);
     }
 
     // resource lookup (?) ///////////////////////////////////////////////////
 
     public NavigationNodeResource getNavigationNodeResource(long id)
         throws EntityDoesNotExistException
     {
         return NavigationNodeResourceImpl.
            getNavigationNodeResource(getCoralSession(context),id);
     }
     
     public NavigationNodeResource getNavigationNodeResource(String id)
         throws EntityDoesNotExistException
     {
         return NavigationNodeResourceImpl.
            getNavigationNodeResource(getCoralSession(context),(new Long(id)).longValue());
     }
 
     // integration ///////////////////////////////////////////////////////////
 
     public ResourceClassResource getClassDefinition(long id)
         throws EntityDoesNotExistException
     {
        return getClassDefinition(getCoralSession(context).getSchema().getResourceClass(id));
     }
     
     public ResourceClassResource getClassDefinition(Resource res)
     {
         return integrationService.getResourceClass(getCoralSession(context),res.getResourceClass());
     }
     
     public ResourceClassResource getClassDefinition(ResourceClass rc)
     {
         return integrationService.getResourceClass(getCoralSession(context),rc);
     }
 
     public static SiteResource getSite(Resource res)
     {
         Resource save = res;
         while(res != null && !(res instanceof SiteResource))
         {
             res = res.getParent();
         }
         return (SiteResource)res;
     }
     
     public String getSitePath(Resource res)
     throws Exception
     {
         ResourceClassResource rcr = getClassDefinition(res);
         String[] targetPaths = rcr.getAggregationTargetPathsList();
         
         String sitePath = getSite(res).getPath();
         String resPath = res.getPath();
         
         for(int i=0; i<targetPaths.length; i++)
         {
             String targetPath = targetPaths[i];
             if(targetPath.charAt(0) != '/')
             {
                 String startPath = sitePath+'/'+targetPath;
                 if(resPath.startsWith(startPath))
                 {
                     return resPath.substring(startPath.length());
                 }
             }
         }
         return null;
     }
 
     // attribute access (introspection) //////////////////////////////////////
 
     public Object resourceAttribute(Resource resource, String attributeName)
     {
         AttributeDefinition attribute = resource.getResourceClass().
             getAttribute(attributeName);
         return resource.get(attribute);
     }
 
     // current navigation node access //////////////////////////////////////////////////////////////
 
     // current site adminstrative access ///////////////////////////////////////////////////////////
     
     /**
      * Checks if the current user has administrative privileges on the current site.
      */
     /**
     public static boolean checkAdministrator()
         throws ProcessingException
     {
         SiteResource site = cmsDataFactory.getCmsData(context).getSite();
 
         if(site != null)
         {
             if(getSubject(data).hasRole(site.getAdministrator()))
             {
                 return true;
             }
         }
 
         CoralSession resourceService = (CoralSession)data.getBroker().
             getService(CoralSession.SERVICE_NAME);
 
         Role cmsAdministrator = resourceService.getSecurity().
             getUniqueRole("cms.administrator");
         
         return getSubject(data).hasRole(cmsAdministrator);
     }
     */
     
     /**
      * Checks if a resource is an instance of a given class
      *
      * @param resource the resource.
      * @param className the name of resource class. 
      */
     public boolean isInstance(Resource resource, String className)
         throws Exception
     {
         ResourceClass rc = getCoralSession(context).getSchema().getResourceClass(className);
         if(resource.getResourceClass().equals(rc))
         {
             return true;
         }
         ResourceClass[] ancestors = resource.getResourceClass().getParentClasses();
         for (int i = 0; i < ancestors.length; i++)
         {
             if(ancestors[i].equals(rc))
             {
                 return true;
             }            
         }
         return false;
     }
     
     public Resource getResource(long id)
     	throws Exception
     {
		return getCoralSession(context).getStore().getResource(id);    	
     }
     
     
     CoralSession getCoralSession(Context context)
     {   
         return (CoralSession)context.getAttribute(CoralSession.class);
     }
 }
 
