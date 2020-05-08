 package net.cyklotron.cms.modules.actions.poll;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.entity.EntityInUseException;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.InvalidResourceNameException;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.utils.StackTrace;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.poll.AnswerResource;
 import net.cyklotron.cms.poll.AnswerResourceImpl;
 import net.cyklotron.cms.poll.PollResource;
 import net.cyklotron.cms.poll.PollResourceImpl;
 import net.cyklotron.cms.poll.PollService;
 import net.cyklotron.cms.poll.QuestionResource;
 import net.cyklotron.cms.poll.QuestionResourceImpl;
 import net.cyklotron.cms.poll.VoteResource;
 import net.cyklotron.cms.poll.VoteResourceImpl;
 import net.cyklotron.cms.poll.util.Answer;
 import net.cyklotron.cms.poll.util.Question;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.workflow.StateResource;
 import net.cyklotron.cms.workflow.WorkflowException;
 import net.cyklotron.cms.workflow.WorkflowService;
 
 /**
  * @author <a href="mailo:pablo@ngo.pl">Pawel Potempski</a>
  * @version $Id: UpdatePoll.java,v 1.8 2005-10-10 13:46:00 rafal Exp $
  */
 public class UpdateVote
     extends BasePollAction
 {
 
     public UpdateVote(Logger logger, StructureService structureService,
         CmsDataFactory cmsDataFactory, PollService pollService, WorkflowService workflowService)
     {
         super(logger, structureService, cmsDataFactory, pollService, workflowService);
 
     }
 
     /**
      * Performs the action.
      */
     public void execute(Context context, Parameters parameters, MVCContext mvcContext,
         TemplatingContext templatingContext, HttpContext httpContext, CoralSession coralSession)
         throws ProcessingException
     {
         Subject subject = coralSession.getUserSubject();
         saveVote(httpContext, parameters);
 
         int vid = parameters.getInt("vid", -1);
         if(vid == -1)
         {
             throw new ProcessingException("Vote id not found");
         }
         Map answers = (Map)httpContext.getSessionAttribute(VOTE_KEY);
         if(answers == null)
         {
             throw new ProcessingException("Answers map not found");
         }
 
         String title = parameters.get("title", "");
         String description = parameters.get("description", "");
         String senderAddress = parameters.get("sender_address","");
         if(title.length() < 1 || title.length() > 64)
         {
             route(mvcContext, templatingContext, "poll.EditVote", "invalid_title");
             return;
         }
         if(description.length() < 0 || description.length() > 255)
         {
             route(mvcContext, templatingContext, "poll.EditVote", "invalid_description");
             return;
         }
        if(!senderAddress.matches("([a-zA-Z0-9.-_]+@[a-zA-Z0-9.-_]+.[a-zA-Z]{1,4})?"))
         {
             route(mvcContext, templatingContext, "poll.EditVote", "invalid_email");
             return;
         }
 
         if(answers.size() == 0)
         {
             route(mvcContext, templatingContext, "poll.EditVote", "no_question_definied");
             return;
         }
         else if(answers.size() < 2)
         {
             route(mvcContext, templatingContext, "poll.EditVote", "too_few_answers");
             return;
         }
 
         for(int i = 0; i < answers.size(); i++)
         {
             Answer answer = (Answer)answers.get(i);
             if(answer.getTitle().length() < 1)
             {
                 route(mvcContext, templatingContext, "poll.EditVote", "invalid_answer");
                 return;
             }
         }
 
         try
         {
             VoteResource voteResource = VoteResourceImpl.getVoteResource(coralSession, vid);
             if(!title.equals(voteResource.getName()))
             {
                 coralSession.getStore().setName(voteResource, title);
             }
             if(!description.equals(voteResource.getDescription()))
             {
                 voteResource.setDescription(description);
             }
             if(!senderAddress.equals(voteResource.getSenderAddress()))
             {
                 voteResource.setSenderAddress(senderAddress);
             }
             voteResource.update();
 
             Set doNotDeleteResources = new HashSet();
 
             for(int i = 0; i < answers.size(); i++)
             {
                 Answer answer = (Answer)answers.get(new Integer(i));
                 AnswerResource answerResource = null;
                 if(answer.getId() < 1)
                 {
                     answerResource = AnswerResourceImpl.createAnswerResource(coralSession, answer
                         .getTitle(), voteResource);
                     answerResource.setSequence(i);
                     answerResource.setVotesCount(0);
                     answerResource.update();
                 }
                 else
                 {
                     answerResource = AnswerResourceImpl.getAnswerResource(coralSession, answer
                         .getId());
                     if(!answerResource.getName().equals(answer.getTitle()))
                     {
                         coralSession.getStore().setName(answerResource, answer.getTitle());
                     }
                     if(answerResource.getSequence() != i)
                     {
                         answerResource.setSequence(i);
                         answerResource.update();
                     }
                 }
                 doNotDeleteResources.add(answerResource);
             }
             purifyDefinition(voteResource, doNotDeleteResources, coralSession);
         }
         catch(EntityDoesNotExistException e)
         {
             templatingContext.put("result", "exception");
             templatingContext.put("trace", new StackTrace(e));
             logger.error("PollException: ", e);
             return;
         }
         catch(EntityInUseException e)
         {
             templatingContext.put("result", "exception");
             templatingContext.put("trace", new StackTrace(e));
             logger.error("PollException: ", e);
             return;
         }
         catch(InvalidResourceNameException e)
         {
             templatingContext.put("result", "exception");
             templatingContext.put("trace", new StackTrace(e));
             logger.error("PollException: ", e);
             return;
         }
         templatingContext.put("result", "updated_successfully");
     }
 
     private void purifyDefinition(Resource definition, Set doNotDeleteResources,
         CoralSession coralSession)
         throws EntityInUseException
     {
         // delete all useless resources in poll tree
         Resource[] answers = coralSession.getStore().getResource(definition);
 
         for(int i = 0; i < answers.length; i++)
         {
             if(!doNotDeleteResources.contains(answers[i]))
             {
                 coralSession.getStore().deleteResource(answers[i]);
             }
         }
     }
 }
