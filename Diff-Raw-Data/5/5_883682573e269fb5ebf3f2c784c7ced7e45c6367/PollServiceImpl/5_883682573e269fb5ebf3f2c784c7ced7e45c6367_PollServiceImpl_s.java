 package net.cyklotron.cms.poll.internal;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.Cookie;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.coral.entity.AmbigousEntityNameException;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.entity.EntityExistsException;
 import org.objectledge.coral.event.ResourceDeletionListener;
 import org.objectledge.coral.query.QueryResults;
 import org.objectledge.coral.relation.Relation;
 import org.objectledge.coral.relation.RelationModification;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.coral.store.InvalidResourceNameException;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.picocontainer.Startable;
 
 import net.cyklotron.cms.poll.AnswerResource;
 import net.cyklotron.cms.poll.BallotResource;
 import net.cyklotron.cms.poll.PollException;
 import net.cyklotron.cms.poll.PollResource;
 import net.cyklotron.cms.poll.PollService;
 import net.cyklotron.cms.poll.PollsResource;
 import net.cyklotron.cms.poll.PollsResourceImpl;
 import net.cyklotron.cms.poll.PoolResource;
 import net.cyklotron.cms.poll.PoolResourceImpl;
 import net.cyklotron.cms.poll.QuestionResource;
 import net.cyklotron.cms.poll.VoteResource;
 import net.cyklotron.cms.poll.VoteResourceImpl;
 import net.cyklotron.cms.poll.util.Answer;
 import net.cyklotron.cms.poll.util.Question;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.workflow.ProtectedTransitionResource;
 import net.cyklotron.cms.workflow.WorkflowException;
 import net.cyklotron.cms.workflow.WorkflowService;
 
 /**
  * Implementation of Poll Service
  *
  * @author <a href="mailto:publo@ngo.pl">Pawel Potempski</a>
  * @version $Id: PollServiceImpl.java,v 1.12 2005-08-02 17:11:23 pablo Exp $
  */
 public class PollServiceImpl
     implements PollService, ResourceDeletionListener, Startable
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
         CoralSession coralSession = sessionFactory.getRootSession();
         try
         {
             coralSession.getEvent().addResourceDeletionListener(this, null);
         }
         finally
         {
             coralSession.close();
         }
     }
 
     public void start()
     {
     }
 
     public void stop()
     {
         
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
             try
             {
                 return PollsResourceImpl
                     .createPollsResource(coralSession, "polls", applications[0]);
             }
             catch(InvalidResourceNameException e)
             {
                 throw new PollException("unexpected exception", e);
             }
         }
         throw new PollException("Too much polls root resources for site: "+site.getName());
     }
     
     public PollsResource getPollsParent(CoralSession coralSession, SiteResource site, String name)
         throws PollException
     {
         PollsResource appsPollsRoot = getPollsRoot(coralSession, site);
         if(appsPollsRoot == null)
         {
             throw new PollException("Polls root for site: " + site.getName() + " not found");
         }
         Resource[] roots = coralSession.getStore().getResource(appsPollsRoot, name);
         if(roots.length == 1 && roots[0] instanceof PollsResource)
         {
             return (PollsResource)roots[0];
         }
         if(roots.length == 0)
         {
             try
             {
                 return PollsResourceImpl.createPollsResource(coralSession, name, appsPollsRoot);
             }
             catch(InvalidResourceNameException e)
             {
                 throw new PollException("unexpected exception", e);
             }
         }
         throw new PollException("Too much polls." + name + " resources for site: " + site.getName());
     }
     
     
     public PollsResource getPollsParent(CoralSession coralSession, int psid, String name)
         throws PollException, EntityDoesNotExistException
     {
         Resource pollsRoot = coralSession.getStore().getResource(psid);
         if(pollsRoot == null
             || !(pollsRoot instanceof PollsResource && "applications".equals(pollsRoot
                 .getParent().getName())))
         {
             throw new PollException("polls." + name + " root not found");
         }
         Resource[] roots = coralSession.getStore().getResource(pollsRoot, name);
         if(roots.length == 1 && roots[0] instanceof PollsResource)
         {
             return (PollsResource)roots[0];
         }
         if(roots.length == 0)
         {
             try
             {
                 return PollsResourceImpl.createPollsResource(coralSession, name, pollsRoot);
             }
             catch(InvalidResourceNameException e)
             {
                 throw new PollException("unexpected exception", e);
             }
         }
         throw new PollException("Too much polls." + name + " resources");
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
      * return the vote resource for configuration.
      *
      * @param config the configuration.
      * @return the vote resource.
      * @throws PollException if the operation fails.
      */
     public VoteResource getVote(CoralSession coralSession, Parameters config)
         throws PollException
     {
         long voteId = config.getLong("vote_id",-1);
         if(voteId != -1)
         {
             VoteResource voteResource = null;
             try
             {
                 voteResource = VoteResourceImpl.getVoteResource(coralSession, voteId);
                 return voteResource;
             }
             catch(EntityDoesNotExistException e)
             {
                 throw new PollException("Vote not found",e);
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
      * {@inheritDoc}
      */
     public boolean hasVoted(HttpContext httpContext, 
         TemplatingContext templatingContext, VoteResource vote)
         throws PollException
     {
         try
         {
             if(templatingContext.get("already_voted") != null && 
               ((Boolean)templatingContext.get("already_voted")).booleanValue())
             {
                 return true;
             }
             if(vote == null)
             {
                 return false;
             }
             String cookieKey = "vote_"+vote.getId();
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
 		        	percentMap.put(id,new Float(answerResource.getVotesCount()*100/questionResource.getVotesCount()));
 		        }
 		        else
 		        {
 		        	percentMap.put(id,new Float(0));
 		        }
 		    }
 		}
 	}
 	
 	
     /**
      * @param vote
      * @param answers
      * @param resultMap
      * @param percentMap
      */
     public void prepareVoteMaps(CoralSession coralSession, VoteResource vote, Map answers,
         Map resultMap, Map percentMap, Map ballotsMap)
     {
         Resource[] answerResources = coralSession.getStore().getResource(vote);
         int totalCount = 0;
         for(int i = 0; i < answerResources.length; i++)
         {
             AnswerResource answerResource = (AnswerResource)answerResources[i];
             Answer answer = new Answer(answerResource.getName(), answerResource.getId());
             answers.put(new Integer(answerResource.getSequence()), answer);
             resultMap.put(answerResource.getId(), new Integer(answerResource.getVotesCount()));
             Resource[] ballotResources = coralSession.getStore().getResource(answerResource);
             ballotsMap.put(answerResource.getId(), Arrays.asList(ballotResources));
             totalCount += answerResource.getVotesCount();
         }
 
         for(int i = 0; i < answerResources.length; i++)
         {
             AnswerResource answerResource = (AnswerResource)answerResources[i];
             if(totalCount > 0)
             {
                 percentMap.put(answerResource.getId(), new Float(answerResource.getVotesCount()
                     * 100 / totalCount));
             }
             else
             {
                 percentMap.put(answerResource.getId(), new Float(0));
             }
         }
     }
     
     public Set<String> getBallotsEmails(CoralSession coralSession, VoteResource vote)
     {
         Resource[] answerResources = coralSession.getStore().getResource(vote);
         Set<String> emailList = new HashSet<String>();
         for(int i = 0; i < answerResources.length; i++)
         {
            AnswerResource answerResource = (AnswerResource)answerResources[i];;
             Resource[] ballotResources = coralSession.getStore().getResource(answerResource);
             for(int j = 0; j < ballotResources.length; j++)
             {
                emailList.add(((BallotResource)ballotResources[i]).getEmail());
             }
         }
         return emailList;
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
     
     public void resourceDeleted(Resource resource)
     {
         CoralSession coralSession = sessionFactory.getRootSession();
         try
         {
             Relation refs = getRelation(coralSession);
             if(resource instanceof PoolResource)
             {
                 RelationModification diff = new RelationModification();
                 diff.remove((PoolResource)resource);
                 coralSession.getRelationManager().updateRelation(refs, diff);
             }
             if(resource instanceof PollResource)
             {
                 RelationModification diff = new RelationModification();
                 diff.removeInv((PollResource)resource);
                 coralSession.getRelationManager().updateRelation(refs, diff);
             }
         }
         catch (Exception e)
         {
             throw new RuntimeException(e);
         }
         finally
         {
             coralSession.close();
         }
     }
 
 }
 
