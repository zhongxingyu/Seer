 package net.cyklotron.cms.modules.actions.poll;
 
 import java.util.Map;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.InvalidResourceNameException;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.utils.StackTrace;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.poll.AnswerResource;
 import net.cyklotron.cms.poll.AnswerResourceImpl;
 import net.cyklotron.cms.poll.PollException;
 import net.cyklotron.cms.poll.PollService;
 import net.cyklotron.cms.poll.PollsResource;
 import net.cyklotron.cms.poll.VoteResource;
 import net.cyklotron.cms.poll.VoteResourceImpl;
 import net.cyklotron.cms.poll.util.Answer;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.workflow.WorkflowService;
 
 /**
  *
  * @author <a href="mailo:pablo@ngo.pl">Pawel Potempski</a>
  * @version $Id: CreatePoll.java,v 1.7 2005-10-10 13:46:00 rafal Exp $
  */
 public class CreateVote
     extends BasePollAction
 {
 
     public CreateVote(Logger logger, StructureService structureService,
         CmsDataFactory cmsDataFactory, PollService pollService, WorkflowService workflowService)
     {
         super(logger, structureService, cmsDataFactory, pollService, workflowService);
         
     }
     /**
      * Performs the action.
      */
     public void execute(Context context, Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, HttpContext httpContext, CoralSession coralSession)
         throws ProcessingException
     {
         Subject subject = coralSession.getUserSubject();
         saveVote(httpContext, parameters);
 
         Map answers = (Map)httpContext.getSessionAttribute(VOTE_KEY);
         if(answers == null)
         {
             throw new ProcessingException("answers map not found");
         }
 
         String title = parameters.get("title","");
         String description = parameters.get("description","");
         String senderAddress = parameters.get("sender_address","");
         if(title.length() < 1 || title.length() > 64)
         {
             route(mvcContext, templatingContext, "poll.AddVote", "invalid_title");
             return;
         }
         if(description.length() < 0 || description.length() > 255)
         {
             route(mvcContext, templatingContext, "poll.AddVote", "invalid_description");
             return;
         }
        if(!senderAddress.matches("[a-zA-Z0-9.-_]+@[a-zA-Z0-9.-_]+.[a-zA-Z]{1,4}"))
         {
             route(mvcContext, templatingContext, "poll.AddVote", "invalid_email");
             return;
         }
         if(answers.size() == 0)
         {
             route(mvcContext, templatingContext, "poll.AddVote", "no_answer_definied");
             return;
         }
         else if(answers.size() < 2)
         {
             route(mvcContext, templatingContext, "poll.AddVote", "too_few_answers");
             return;
         }
 
         for(int i = 0; i< answers.size(); i++)
         {
             Answer answer = (Answer)answers.get(new Integer(i));
             if(answer.getTitle().length() < 1)
             {
                 route(mvcContext, templatingContext, "poll.AddVote", "invalid_answer");
                 return;
             }
         }
 
         try
         {
             SiteResource site = cmsDataFactory.getCmsData(context).getSite();
             PollsResource votesRoot = pollService.getPollsParent(coralSession, site, pollService.VOTES_ROOT_NAME);
             VoteResource voteResource = VoteResourceImpl.createVoteResource(coralSession, title, votesRoot);
             voteResource.setDescription(description);
             voteResource.setSenderAddress(senderAddress);
             voteResource.update();
             
             for(int i = 0; i < answers.size(); i++)
             {
                 Answer answer = (Answer)answers.get(i);
                 AnswerResource answerResource = AnswerResourceImpl.createAnswerResource(
                     coralSession, answer.getTitle(), voteResource);
                 answerResource.setSequence(i);
                 answerResource.setVotesCount(0);
                 answerResource.update();
             }
         }
 
         
         catch(PollException e)
         {
             templatingContext.put("result","exception");
             templatingContext.put("trace",new StackTrace(e));
             logger.error("PollException: ",e);
             return;
         }
         catch(InvalidResourceNameException e)
         {
             templatingContext.put("result","exception");
             templatingContext.put("trace",new StackTrace(e));
             logger.error("LinkException: ",e);
             return;
         }
         templatingContext.put("result","added_successfully");
     }
 }
 
 
