 package net.cyklotron.cms.forum.internal;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import net.cyklotron.cms.CmsNodeResourceImpl;
 import net.cyklotron.cms.forum.CommentaryResource;
 import net.cyklotron.cms.forum.CommentaryResourceImpl;
 import net.cyklotron.cms.forum.DiscussionResource;
 import net.cyklotron.cms.forum.DiscussionResourceImpl;
 import net.cyklotron.cms.forum.ForumException;
 import net.cyklotron.cms.forum.ForumNodeResource;
 import net.cyklotron.cms.forum.ForumResource;
 import net.cyklotron.cms.forum.ForumResourceImpl;
 import net.cyklotron.cms.forum.ForumService;
 import net.cyklotron.cms.forum.MessageResource;
 import net.cyklotron.cms.security.SecurityService;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.site.SiteService;
 import net.cyklotron.cms.structure.NavigationNodeResource;
 import net.cyklotron.cms.workflow.StateChangeListener;
 import net.cyklotron.cms.workflow.StateResource;
 import net.cyklotron.cms.workflow.StatefulResource;
 import net.cyklotron.cms.workflow.WorkflowService;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.coral.BackendException;
 import org.objectledge.coral.datatypes.WeakResourceList;
 import org.objectledge.coral.entity.AmbigousEntityNameException;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.security.Role;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.coral.store.ValueRequiredException;
 import org.objectledge.event.EventWhiteboard;
 
 /**
  * Implementation of Forum Service
  *
  * @author <a href="mailto:publo@ngo.pl">Pawel Potempski</a>
 * @version $Id: ForumServiceImpl.java,v 1.2 2005-01-18 10:39:26 pablo Exp $
  */
 public class ForumServiceImpl
     implements ForumService, StateChangeListener
 {
     // instance variables ////////////////////////////////////////////////////
 
     /** logging facility */
     private Logger log;
 
     /** site serivce */
     private SiteService siteService;
 
     /** workflow service */
     private WorkflowService workflowService;
 
     /** cms security service */
     private SecurityService cmsSecurityService;
 
 	/** event service */
 	private EventWhiteboard eventWhiteboard;
     
     /** session factory */
     private CoralSessionFactory sessionFactory;
 
     // initialization ////////////////////////////////////////////////////////
 
     /**
      * Initializes the service.
      */
     public ForumServiceImpl(Logger logger, SiteService siteService, 
         WorkflowService workflowService, SecurityService cmsSecurityService,
        CoralSessionFactory sessionFactory)
     {
         this.log = logger;
         this.sessionFactory = sessionFactory;
         this.siteService = siteService;
         this.workflowService = workflowService;
         this.cmsSecurityService = cmsSecurityService;
         eventWhiteboard.addListener(StateChangeListener.class,this,null);
     }
 
      
     /**
      * Creates forum associated with a site.
      *
      * @param site the site.
      * @param mailboxOwner the owner of the mailbox where mailng lists will
      * reside.
      * @param subject the subject that performs the operation.
      * @return a ForumResource object.
      */
     public ForumResource createForum(CoralSession coralSession, SiteResource site, Subject mailboxOwner)
         throws ForumException
     {
         Resource[] res = coralSession.getStore().getResource(site, "applications");
         if(res == null || res.length != 1)
         {
             throw new ForumException("Applications root for site: "+site.getName()+" not found");
         }
         ForumResource forum;
         try
         {
         	Resource[] resources = coralSession.getStore().getResource(res[0],"forum");
         	if(resources.length > 1)
         	{
 				throw new ForumException("Strange - there is more than one forum node in site '"+site.getName()+"'");
         	}
 			if(resources.length == 1)
 			{
 				return (ForumResource)resources[0];
 			}
             forum = ForumResourceImpl.createForumResource(
                 coralSession, "forum", res[0], site);
         }
         catch(ValueRequiredException e)
         {
             throw new ForumException("Unexpected ARL exception", e);
         }
         try
         {
             cmsSecurityService.createRole(coralSession, site.getAdministrator(), 
                 "cms.forum.administrator", forum);
         }
         catch(Exception e)
         {
             throw new ForumException("Failed to setup security", e);
         }
         return forum;
     }
 
     /**
      * Creates a new discussion.
      *
      * @param forum the forum to create discussion in.
      * @param path the pathanme of the discussion relative to forum.
      * @param admin the discussion's administrator.
      * @param subject the subject that performs the operation.
      */
     public DiscussionResource createDiscussion(CoralSession coralSession, ForumResource forum, String path)
         throws ForumException
     {
         String name;
         Resource parent;
         try
         {
             parent = preparePath(coralSession, forum, path);
             int i = path.lastIndexOf('/');
             if(i > 0)
             {
                 name = path.substring(i+1);
             }
             else
             {
                 name = path;
             }
         }
         catch(AmbigousEntityNameException e)
         {
             throw new ForumException("Ambigous discussion pathname", e);
         }
 
         SiteResource site = forum.getSite();
         DiscussionResource discussion;
         try
         {
             discussion = DiscussionResourceImpl.
                 createDiscussionResource(coralSession, name, parent,
                                          forum);
             Resource workflowRoot = null;
             if(site != null)
             {
                 workflowRoot = site.getParent().getParent();
             }
             workflowService.assignState(coralSession, workflowRoot, discussion);
         }
         catch(Exception e)
         {
             throw new ForumException("failed to create discussion", e);
         }
         return discussion;
     }
 
     /**
      * Creates a new commentary.
      *
      * @param forum the forum to create commentary in.
      * @param path the pathanme of the commentary relative to forum.
      * @param admin the commentary's administrator.
      * @param subject the subject that performs the operation.
      */
     public CommentaryResource createCommentary(CoralSession coralSession, ForumResource forum, String path, Resource resource)
         throws ForumException
     {
         String name;
         String docTitle = "";
         Resource parent;
         try
         {
             parent = preparePath(coralSession, forum, path);
             int i = path.lastIndexOf('/');
             if(i > 0)
             {
                 name = path.substring(i+1);
             }
             else
             {
                 name = path;
             }
             if(resource instanceof NavigationNodeResource)
             {
                 docTitle = ((NavigationNodeResource)resource).getTitle();
             }
         }
         catch(AmbigousEntityNameException e)
         {
             throw new ForumException("Ambigous commentary pathname", e);
         }
 
         CommentaryResource commentary;
         try
         {
             commentary = createCommentaryNode(coralSession, name, parent, forum);
             commentary.setResourceId(resource.getId());
             commentary.setDocumentTitle(docTitle);
             commentary.setState(getInitialCommentaryState(coralSession, forum));
             commentary.update();
         }
         catch(Exception e)
         {
             throw new ForumException("failed to create commentary", e);
         }
         return commentary;
     }
     
     private synchronized CommentaryResource createCommentaryNode(CoralSession coralSession, String name, Resource parent,
     	ForumResource forum)
     	throws Exception
     {
         Resource[] resources = coralSession.getStore().getResource(parent, name);
         if(resources.length > 0)
         {
             return (CommentaryResource)resources[0];
         }
         else
         {
             return CommentaryResourceImpl.
             			createCommentaryResource(coralSession, name, parent, forum);
         }
     }
     
     
 
     /**
      * Returns a discussion within a given forum.
      *
      * @param forum the forum.
      * @param path the pathname of the discussion relative to forum.
      * @return a discussion resource, or <code>null</code> if not found.
      */
     public DiscussionResource getDiscussion(CoralSession coralSession, ForumResource forum, String path)
         throws ForumException
     {
         Resource[] res = coralSession.getStore().getResourceByPath(forum, path);
         if(res.length == 1)
         {
             return (DiscussionResource)res[0];
         }
         else
         {
             return null;
         }
     }
 
     /**
      * Return the forum of a specific site.
      *
      * @param site the site resource.
      * @return the forum root resource.
      * @throws ForumException.
      */
     public ForumResource getForum(CoralSession coralSession, SiteResource site)
         throws ForumException
     {
         Resource[] roots = coralSession.getStore().getResource(site, "applications");
         if(roots == null || roots.length != 1)
         {
             throw new ForumException("Applications root for site: "+site.getName()+" not found");
         }
         roots = coralSession.getStore().getResource(roots[0], "forum");
         if(roots == null || roots.length != 1)
         {
             throw new ForumException("Forum resource for site: "+site.getName()+" not found");
         }
         return (ForumResource)roots[0];
     }
 
     /**
      * Return the messages in a discussion as a flat list.
      *
      * @param discussion the discussion resource.
      * @param subject the subject.
      * @return the messages list.
      * @throws ForumException.
      */
     public List listMessages(CoralSession coralSession, DiscussionResource discussion)
         throws ForumException
     {
         List target = new ArrayList();
         getSubPost(coralSession, discussion,target);
         return target;
     }
     
     /**
      * Returns the initial state of commentaries created on demand.
      */
     public StateResource getInitialCommentaryState(CoralSession coralSession, ForumResource forum)
         throws ForumException
     {
         if(forum.getInitialCommentaryState() != null)
         {
             return forum.getInitialCommentaryState();
         }
         else
         {
             try
             {
                 return (StateResource)coralSession.getStore().getUniqueResourceByPath(
                     "/cms/workflow/automata/forum.discussion/states/moderated");
             }
             catch(Exception e)
             {
                 throw new ForumException("failed to lookup default state", e);
             }
         }
     }
 
     /**
      * Sets the initial state of commentaries created on demand.
      */
     public void setInitialCommentaryState(CoralSession coralSession, ForumResource forum, StateResource state)
         throws ForumException
     {
         try
         {
             forum.setInitialCommentaryState(state);
             forum.update();
         }
         catch(Exception e)
         {
             throw new ForumException("failed to set default state", e);
         }
     }
 
     // implementation ////////////////////////////////////////////////////////
 
     /**
      * Unroll a tree into a flat list
      *
      * @param resource the (partial) tree root.
      * @param target the node list.
      */
     private void getSubPost(CoralSession coralSession, Resource resource, List target)
         throws ForumException
     {
         Resource[] resources = coralSession.getStore().getResource(resource);
         for(int i = 0; i< resources.length; i++)
         {
             target.add(resources[i]);
             getSubPost(coralSession, resources[i], target);
         }
     }
 
     /**
      * Prepare a location for creating a new resource.
      *
      * TODO: this method could me moved to some sort of ARLUtils class
      *
      * <p>This method creates resources of type <code>node</code> that are
      * neccessary for creating a resource at the specific path.
      *
      * @param rs the CoralSession.
      * @param resource the path is considered to be relative to this resource,
      *        <code>null</code> for resource #1.
      * @param path the path.
      * @param subject the subject that performs the operation.
      * @return the immediate parent of the resource described by the pathname
      */
     private Resource preparePath(CoralSession rs, Resource resource, String path)
         throws AmbigousEntityNameException
     {
         StringTokenizer st = new StringTokenizer(path, "/");
         if(resource == null)
         {
             try
             {
                 resource = rs.getStore().getResource(1L);
             }
             catch(EntityDoesNotExistException e)
             {
                 throw new BackendException("root resource not found");
             }
         }
         Resource orig = resource;
         ArrayList tokens = new ArrayList();
         while(st.hasMoreTokens())
         {
             tokens.add(st.nextToken());
         }
 
         for(int i=0; i<tokens.size() - 1; i++)
         {
             String nc = (String)tokens.get(i);
             Resource[] res = rs.getStore().getResource(resource, nc);
             if(res.length == 0)
             {
                 resource = CmsNodeResourceImpl.createCmsNodeResource(rs, nc, resource);
             }
             else if(res.length > 1)
             {
                 throw new AmbigousEntityNameException("pathname "+orig.getPath()+
                                                 "/"+path+" is amibigous");
             }
             else
             {
                 resource = res[0];
             }
         }
         return resource;
     }
     
 	public void messageAccepted(CoralSession coralSession, MessageResource message)
 		throws ForumException
 	{
 		addLastAdded(coralSession, message.getDiscussion(), message);
 		addLastAdded(coralSession, (ForumNodeResource)message.getDiscussion().getParent(), message);
 		addLastAdded(coralSession, message.getDiscussion().getForum(), message);
 	}
 	
 	public void addLastAdded(CoralSession coralSession, ForumNodeResource node, MessageResource message)
 		throws ForumException
 	{
 		WeakResourceList list = node.getLastlyAdded();
         log.debug("Forum service: addLastAdded: node "+node.getIdString()+" for message: "
                   +message);
 		int size = node.getLastlyAddedSize(10);
 		LinkedList fifo = new LinkedList();
 		if(list != null)
 		{
             log.debug("Forum service: printListStatus: list size = "+list.size());
             for(int i = 0; i < list.size(); i++)
             {
                 Object ob = list.get(i);
                 if(ob != null && ob instanceof MessageResource)
                 {
                     fifo.add(ob);
                 }    
             }
         }
         log.debug("Forum service: addLastAdded: print fifo status before action");
         printListStatus(fifo);
 		while(fifo.size() > size)
 		{
 			fifo.removeLast();
 		}
         log.debug("Forum service: addLastAdded: print fifo status after cut");
         printListStatus(fifo);        
 		if(message != null)
 		{
             log.debug("Forum service: addLastAdded: adding message "+message.getIdString());
 			if(fifo.size() == size)
 			{
                 log.debug("Forum service: addLastAdded: fifo size exceed max size: "+
                           fifo.size()+" > "+size);
 				fifo.removeLast();
 			}
 			fifo.addFirst(message);
 		}
         log.debug("Forum service: addLastAdded: fifo after update:");
         printListStatus(fifo);        
 		list = new WeakResourceList(coralSession.getStore(), fifo);
 		node.setLastlyAdded(list);
 		node.update();
         log.debug("Forum service: addLastAdded: fifo from resource:");
         printListStatus(node.getLastlyAdded());
 	}
 	
     private void printListStatus(List list)
     {
         log.debug("Forum service: printListStatus: list size = "+list.size());
         for(int i = 0; i< list.size(); i++)
         {
             Object obj = list.get(i);
             if(obj == null)
             {
                 log.debug("Forum service: printListStatus: obj "+i+
                           " = null");
                 continue;                          
             }
             if(obj instanceof MessageResource)
             {
                 log.debug("Forum service: printListStatus: obj "+i+
                           " is message with id = "+((Resource)obj).getIdString());
                 continue;
             }
             if(obj instanceof Resource)
             {
                 log.debug("Forum service: printListStatus: obj "+i+
                           " is not message but other resource with id = "+((Resource)obj).getIdString());
             }
             log.debug("Forum service: printListStatus: obj "+i+ 
                       " object class = "+obj.getClass().getName());
         }
     }
     
 	// state change listener
 	
 	
 	public void stateChanged(Role role, StatefulResource resource)
 	{
         
         log.debug("Forum service: state change listener: resource "+resource.getIdString()+" changed");
 		if(resource instanceof MessageResource)
 		{
             log.debug("Forum service: state change listener: resource "+resource.getIdString()+
                       " recognized as forum message in state: "+
             resource.getState().getName());
 			if(resource.getState().getName().equals("visible"))
 			{
                 CoralSession coralSession = sessionFactory.getRootSession();
 				try
 				{
                  	messageAccepted(coralSession, (MessageResource)resource);
 				}
 				catch(Exception e)
 				{
 					log.error("Couldn't add to the last added queue", e);
 				}
                 finally
                 {
                     coralSession.close();
                 }
 			}
 		}
 	}
 }
 
