 package net.cyklotron.cms.poll.internal;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Map;
 
 import javax.servlet.http.Cookie;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.coral.entity.AmbigousEntityNameException;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.entity.EntityExistsException;
 import org.objectledge.coral.query.QueryResults;
 import org.objectledge.coral.relation.Relation;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 
 import net.cyklotron.cms.poll.AnswerResource;
 import net.cyklotron.cms.poll.PollException;
 import net.cyklotron.cms.poll.PollResource;
 import net.cyklotron.cms.poll.PollService;
 import net.cyklotron.cms.poll.PollsResource;
 import net.cyklotron.cms.poll.PollsResourceImpl;
 import net.cyklotron.cms.poll.PoolResource;
 import net.cyklotron.cms.poll.PoolResourceImpl;
 import net.cyklotron.cms.poll.QuestionResource;
 import net.cyklotron.cms.poll.util.Answer;
 import net.cyklotron.cms.poll.util.Question;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.site.SiteService;
 import net.cyklotron.cms.workflow.ProtectedTransitionResource;
 import net.cyklotron.cms.workflow.WorkflowException;
 import net.cyklotron.cms.workflow.WorkflowService;
 
 /**
  * Implementation of Poll Service
  *
  * @author <a href="mailto:publo@ngo.pl">Pawel Potempski</a>
 * @version $Id: PollServiceImpl.java,v 1.6 2005-04-12 05:42:24 pablo Exp $
  */
 public class PollServiceImpl
     implements PollService
 {
    public static final String RELATION_NAME = "poll.PoolBindings";
     
     // instance variables ////////////////////////////////////////////////////
     
     /** logging facility */
     private Logger log;
 
     /** resource service */
     private CoralSession coralSession;
 
     /** workflow service */
     private WorkflowService workflowService;
     
     /** pds */
     //private PersonalDataService pds;
 
     private CoralSessionFactory sessionFactory;
     
     private Relation pollRelation;
 
 
     // initialization ////////////////////////////////////////////////////////
 
     /**
      * Initializes the service.
      */
     public PollServiceImpl(CoralSessionFactory sessionFactory, Logger logger, 
         WorkflowService workflowService)
     {
         this.log = logger;
         this.workflowService = workflowService;
         this.sessionFactory = sessionFactory;
     }
 
     /**
      * return the polls root resource.
      *
      * @param site the site resource.
      * @return the polls root resource.
      * @throws PollException if the operation fails.
      */
     public PollsResource getPollsRoot(CoralSession coralSession, SiteResource site)
         throws PollException
     {
         Resource[] applications = coralSession.getStore().getResource(site, "applications");
         if(applications == null || applications.length != 1)
         {
             throw new PollException("Applications root for site: "+site.getName()+" not found");
         }
         Resource[] roots = coralSession.getStore().getResource(applications[0], "polls");
         if(roots.length == 1)
         {
             return (PollsResource)roots[0];
         }
         if(roots.length == 0)
         {
             return PollsResourceImpl.createPollsResource(coralSession, "polls", applications[0]);
         }
         throw new PollException("Too much polls root resources for site: "+site.getName());
     }
 
     /**
      * return the poll for poll pool with logic based on specified configuration.
      *
      * @param pollsResource the polls pool.
      * @param config the configuration.
      * @return the poll resource.
      * @throws PollException if the operation fails.
      */
     public PollResource getPoll(CoralSession coralSession, PollsResource pollsResource, Parameters config)
         throws PollException
     {
         long poolId = config.getLong("pool_id",-1);
         if(poolId != -1)
         {
             PoolResource poolResource = null;
             try
             {
                 poolResource = PoolResourceImpl.getPoolResource(coralSession, poolId);
                 return getPoll(coralSession, poolResource);
             }
             catch(EntityDoesNotExistException e)
             {
                 throw new PollException("Pool not found",e);
             }
         }
         return null;
     }
 
     /**
      * return the poll content for indexing purposes.
      *
      * @param pollResource the poll.
      * @return the poll content.
      */
     public String getPollContent(PollResource pollResource)
     {
         return "";
     }
 
 
 
     /**
      * execute logic of the job to check expiration date.
      */
     public void checkPollState(CoralSession coralSession)
     {
 		try
 		{
 			Resource readyState = coralSession.getStore()
 				.getUniqueResourceByPath("/cms/workflow/automata/poll.poll/states/ready");
 			Resource activeState = coralSession.getStore()
 				.getUniqueResourceByPath("/cms/workflow/automata/poll.poll/states/active");				
 			QueryResults results = coralSession.getQuery().
 				executeQuery("FIND RESOURCE FROM cms.poll.poll WHERE state = "+readyState.getIdString());
 			Resource[] nodes = results.getArray(1);
 			log.debug("CheckPollState "+nodes.length+" ready polls found");
 			for(int i = 0; i < nodes.length; i++)
 			{
 				checkPollState(coralSession, (PollResource)nodes[i]);
 			}
 			results = coralSession.getQuery()
 				.executeQuery("FIND RESOURCE FROM cms.poll.poll WHERE state = "+activeState.getIdString());
 			nodes = results.getArray(1);
 			log.debug("CheckPollState "+nodes.length+" active polls found");
 			for(int i = 0; i < nodes.length; i++)
 			{
 				checkPollState(coralSession, (PollResource)nodes[i]);
 			}
 		}
 		catch(Exception e)
 		{
 			log.error("CheckBannerState exception ",e);
 		}
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean hasVoted(HttpContext httpContext, 
         TemplatingContext templatingContext, PollResource poll)
         throws PollException
     {
         try
         {
             if(templatingContext.get("already_voted") != null && 
               ((Boolean)templatingContext.get("already_voted")).booleanValue())
             {
                 return true;
             }
             if(poll == null)
             {
                 return false;
             }
             String cookieKey = "poll_"+poll.getId();
             Cookie[] cookies = httpContext.getRequest().getCookies();
             if(cookies != null)
             {
                 for(int i=0; i<cookies.length; i++)
                 {
                     if(cookies[i].getName().equals(cookieKey))
                     {
                         return true;
                     }
                 }
             }
             return false;
         }
         catch(Exception e)
         {
             throw new PollException("exception occured", e);
         }
     }
     
 	/**
 	 * @param poll
 	 * @param questions
 	 * @param resultMap
 	 * @param percentMap
 	 */
 	public void prepareMaps(CoralSession coralSession, PollResource poll, Map questions, Map resultMap, Map percentMap) {
 		Resource[] questionResources = coralSession.getStore().getResource(poll);
 		for(int i = 0; i < questionResources.length; i++)
 		{
 		    QuestionResource questionResource = (QuestionResource)questionResources[i];
 		    Question question = new Question(questionResource.getName(),questionResource.getId());
 		    questions.put(new Integer(questionResource.getSequence()),question);
 		    Resource[] answerResources = coralSession.getStore().getResource(questionResources[i]);
 		    for(int j = 0; j < answerResources.length; j++)
 		    {
 		        AnswerResource answerResource = (AnswerResource)answerResources[j];
 		        Answer answer = new Answer(answerResource.getName(),answerResource.getId());
 		        question.getAnswers().put(new Integer(answerResource.getSequence()),answer);
 		        Long id = answerResource.getIdObject();
 		        resultMap.put(id, new Integer(answerResource.getVotesCount()));
 		        if(questionResource.getVotesCount() > 0)
 		        {
 		        	percentMap.put(id,new Float(answerResource.getVotesCount()/questionResource.getVotesCount()*100));
 		        }
 		        else
 		        {
 		        	percentMap.put(id,new Float(0));
 		        }
 		    }
 		}
 	}
     
     
     
     /**
      * return the poll for poll pool with logic based on specified configuration.
      *
      * @param poolResource the polls pool.
      * @return the poll resource.
      * @throws PollException if the operation fails.
      */
     private PollResource getPoll(CoralSession coralSession, PoolResource poolResource)
         throws PollException
     {
         Resource[] polls = getRelation(coralSession).get(poolResource);
         ArrayList active = new ArrayList();
         PollResource pollResource = null;
         PollResource temp = null;
         for(int i =0; i < polls.length; i++)
         {
             temp = (PollResource)polls[i];
             if(temp.getState().getName().equals("active"))
             {
                 active.add(temp);
             }
         }
         if(active.size()==0)
         {
             return null;
         }
         pollResource = (PollResource)active.get(0);
         for(int i=1; i < active.size(); i++)
         {
             temp = (PollResource)active.get(i);
             if(temp.getEndDate().before(pollResource.getEndDate()))
             {
                 pollResource = temp;
             }
         }
         return pollResource;
     }
 
     
     // private methods
 
 
     
     /**
      * check state of the poll and expire it if the end date was reached.
      */
     private void checkPollState(CoralSession coralSession, PollResource pollResource)
     {
         try
         {
             Date today = Calendar.getInstance().getTime();
             ProtectedTransitionResource[] transitions = workflowService.getAllowedTransitions(coralSession, pollResource, coralSession.getUserSubject());
             String state = pollResource.getState().getName();
             ProtectedTransitionResource transition = null;
 
             if(state.equals("ready"))
             {
                 if(today.after(pollResource.getEndDate()))
                 {
                     for(int i = 0; i < transitions.length; i++)
                     {
                         if(transitions[i].getName().equals("expire_ready"))
                         {
                             transition = transitions[i];
                             break;
                         }
                     }
                     workflowService.performTransition(coralSession, pollResource, transition);
                     return;
                 }
                 if(today.after(pollResource.getStartDate()))
                 {
                     for(int i = 0; i < transitions.length; i++)
                     {
                         if(transitions[i].getName().equals("activate"))
                         {
                             transition = transitions[i];
                             break;
                         }
                     }
                     workflowService.performTransition(coralSession, pollResource, transition);
                     return;
                 }
             }
             if(state.equals("active"))
             {
                 if(today.after(pollResource.getEndDate()))
                 {
                     for(int i = 0; i < transitions.length; i++)
                     {
                         if(transitions[i].getName().equals("expire_active"))
                         {
                             transition = transitions[i];
                             break;
                         }
                     }
                     workflowService.performTransition(coralSession, pollResource, transition);
                     return;
                 }
             }
         }
         catch(WorkflowException e)
         {
             log.error("Poll Job Exception",e);
         }
 
     }
 
     
     /**
      * {@inheritDoc}
      */
     public Relation getRelation(CoralSession coralSession)
     {     
         if(pollRelation != null)
         {
             return pollRelation;
         }
         try
         {
             pollRelation = coralSession.getRelationManager().
                                    getRelation(RELATION_NAME);
         }
         catch(AmbigousEntityNameException e)
         {
             throw new IllegalStateException("ambiguous related relation");
         }
         catch(EntityDoesNotExistException e)
         {
             //ignore it.
         }
         if(pollRelation != null)
         {
             return pollRelation;
         }
         try
         {
             createRelation(coralSession, RELATION_NAME);
         }
         catch(EntityExistsException e)
         {
             throw new IllegalStateException("the related relation already exists");
         }
         return pollRelation;
     }    
     
     /**
      * Create the  relation.
      * 
      * @param coralSession the coralSession. 
      */
     private synchronized void createRelation(CoralSession coralSession, String name)
         throws EntityExistsException
     {
         if(pollRelation == null)
         {
             pollRelation = coralSession.getRelationManager().
                 createRelation(RELATION_NAME);
         }
     }
 
 }
 
