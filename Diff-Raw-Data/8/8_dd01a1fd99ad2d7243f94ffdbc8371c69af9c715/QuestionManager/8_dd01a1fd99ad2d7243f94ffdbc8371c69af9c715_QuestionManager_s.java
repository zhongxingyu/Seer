 package ca.usask.gmcte.currimap.action;
 
 
 import java.util.List;
 import java.util.TreeMap;
 
 
 import org.apache.log4j.Logger;
 import org.hibernate.Session;
 
 import ca.usask.gmcte.currimap.model.AnswerOption;
 import ca.usask.gmcte.currimap.model.AnswerSet;
 
 import ca.usask.gmcte.currimap.model.CourseOffering;
 import ca.usask.gmcte.currimap.model.LinkProgramQuestion;
 import ca.usask.gmcte.currimap.model.Program;
 import ca.usask.gmcte.currimap.model.Question;
 import ca.usask.gmcte.currimap.model.QuestionResponse;
 import ca.usask.gmcte.currimap.model.QuestionType;
 
 import ca.usask.gmcte.util.HibernateUtil;
 
 public class QuestionManager
 {
 	private static QuestionManager instance;
 	private static Logger logger = Logger.getLogger(QuestionManager.class);
 
 	public boolean saveQuestion(int id,String display, String questionType, int answerSetId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			Question o = new Question();
 			
 			if(id > -1)
 			{
 				o = (Question)session.get(Question.class, id);
 			}
 			o.setDisplay(display);
 			if(answerSetId > -1)
 			{
 				AnswerSet set = (AnswerSet)session.get(AnswerSet.class, answerSetId);
 				o.setAnswerSet(set);
 			}
 			if(questionType != null)
 			{
 				QuestionType type = (QuestionType)session.createQuery("FROM QuestionType WHERE name=:name").setParameter("name", questionType).uniqueResult();;
 				o.setQuestionType(type);
 			}
 			if(id > -1)
 				session.update(o);
 			else
 				session.save(o);
 			session.getTransaction().commit();
 			return true;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 	}
 
 	public boolean saveAnswerOption(int id,String value, String display, int answerSetId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			AnswerOption o = new AnswerOption();
 			if( id > -1)
 			{
 				o = (AnswerOption) session.get(AnswerOption.class,id);
 				if(!value.equals(o.getValue())) // if the value was already used in question responses, it needs to be updated.
 				{
 					@SuppressWarnings("unchecked")
 					List<QuestionResponse> responsesUsingAnswer = (List<QuestionResponse>)session
 							.createQuery("FROM QuestionResponse WHERE question in (FROM Question WHERE answerSet.id=:answerSetId) AND response=:responseValue")
 							.setParameter("answerSetId",o.getAnswerSet().getId()).setParameter("responseValue",o.getValue()).list();
 					for(QuestionResponse response : responsesUsingAnswer)
 					{
 						response.setResponse(value);
 						session.merge(response);
 					}
 					
 				}
 			}
 			else
 			{
 				AnswerSet set = (AnswerSet)session.get(AnswerSet.class,  answerSetId);
 				o.setAnswerSet(set);
 			}
 			o.setValue(value);
 			o.setDisplay(display);
 			
 			if(id < 0)
 			{
 				int existingCount = session.createQuery("FROM AnswerOption WHERE answerSet.id=:answerSetId").setParameter("answerSetId", answerSetId).list().size();		
 				o.setDisplayIndex(existingCount+1);
 			}
 			session.merge(o);
 			session.getTransaction().commit();
 			return true;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 	}
 	public boolean saveAnswerSet(String name)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			AnswerSet o = new AnswerSet();
 			o.setName(name);
 			session.save(o);
 			session.getTransaction().commit();
 			return true;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 	}
 	public AnswerSet getAnswerSetByName(String name)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		AnswerSet toReturn = null;
 		try
 		{
 			toReturn = (AnswerSet)session.createQuery("from AnswerSet where name=:name")
 					.setParameter("name",name)
 					.uniqueResult();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public boolean moveAnswerOption(int toMoveId, String direction)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			AnswerOption toMove = (AnswerOption)session.get(AnswerOption.class,toMoveId);
 			List<AnswerOption> existing = (List<AnswerOption>)session.createQuery("FROM AnswerOption WHERE answerSet.id=:answerSetId order by displayIndex").setParameter("answerSetId", toMove.getAnswerSet().getId()).list();		
 			if(direction.equals("up"))
 			{
 				AnswerOption prev = null;
 				for(AnswerOption link : existing)
 				{
 					if(link.getId() == toMoveId && prev!=null)
 					{
 						int swap = prev.getDisplayIndex();
 						prev.setDisplayIndex(link.getDisplayIndex());
 						link.setDisplayIndex(swap);
 						session.merge(prev);
 						session.merge(prev);
 						break;
 					}
 					prev = link;
 				}
 			}
 			else if(direction.equals("down"))
 			{
 				AnswerOption prev = null;
 				for(AnswerOption link : existing)
 				{
 					if(prev !=null)
 					{
 						int swap = prev.getDisplayIndex();
 						prev.setDisplayIndex(link.getDisplayIndex());
 						link.setDisplayIndex(swap);
 						session.merge(prev);
 						session.merge(link);
 						break;
 					}
 					if(link.getId() == toMoveId)
 					{
 						prev = link;
 					}
 					
 				}
 			}
 			else if(direction.equals("delete"))
 			{
 				AnswerOption toDelete = null;
 				for(AnswerOption link : existing)
 				{
 					if(toDelete !=null)
 					{
 						link.setDisplayIndex(link.getDisplayIndex()-1);
 						List<QuestionResponse> responsesUsingAnswer = (List<QuestionResponse>)session
 								.createQuery("FROM QuestionResponse WHERE question in (FROM Question WHERE answerSet.id=:answerSetId) AND response=:responseValue")
 								.setParameter("answerSetId",toMove.getAnswerSet().getId()).setParameter("responseValue",toMove.getValue()).list();
 						for(QuestionResponse resp :responsesUsingAnswer)
 						{
 							session.delete(resp);
 						}
 						session.merge(link);
 					}
 					if(link.getId() == toMoveId)
 					{
 						toDelete = link;
 					}
 		
 				}
 				if(toDelete != null)
 				{
 					session.delete(toDelete);
 				}
 			}
 			session.getTransaction().commit();
 			return true;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 	}
 	public LinkProgramQuestion getLinkProgramQuestion(int programId, int questionId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		LinkProgramQuestion toReturn = null;
 		try
 		{
 			toReturn = (LinkProgramQuestion)session.createQuery("from LinkProgramQuestion where program.id = :programId and question.id=:questionId")
 					.setParameter("programId",programId)
 					.setParameter("questionId", questionId)
 					.uniqueResult();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	
 	
 	@SuppressWarnings("unchecked")
 	public boolean moveQuestion(int programId, int questionId, String direction)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			LinkProgramQuestion toMove = (LinkProgramQuestion)session
 					.createQuery("FROM LinkProgramQuestion WHERE program.id=:programId AND question.id=:questionId")
 					.setParameter("programId", programId)
 					.setParameter("questionId",questionId)
 					.uniqueResult();
 		
 			int toMoveId = toMove.getId();
 			List<LinkProgramQuestion> existing = (List<LinkProgramQuestion>)session.createQuery("FROM LinkProgramQuestion WHERE program.id=:programId ORDER BY displayIndex").setParameter("programId", toMove.getProgram().getId()).list();		
 			if(direction.equals("up"))
 			{
 				LinkProgramQuestion prev = null;
 				for(LinkProgramQuestion link : existing)
 				{
 					if(link.getId() == toMoveId && prev!=null)
 					{
 						int swap = prev.getDisplayIndex();
 						prev.setDisplayIndex(link.getDisplayIndex());
 						link.setDisplayIndex(swap);
 						session.merge(prev);
 						session.merge(prev);
 						break;
 					}
 					prev = link;
 				}
 			}
 			else if(direction.equals("down"))
 			{
 				LinkProgramQuestion prev = null;
 				for(LinkProgramQuestion link : existing)
 				{
 					if(prev !=null)
 					{
 						int swap = prev.getDisplayIndex();
 						prev.setDisplayIndex(link.getDisplayIndex());
 						link.setDisplayIndex(swap);
 						session.merge(prev);
 						session.merge(link);
 						break;
 					}
 					if(link.getId() == toMoveId)
 					{
 						prev = link;
 					}
 					
 				}
 			}
 			else if(direction.equals("delete"))
 			{
 				LinkProgramQuestion toDelete = null;
 				for(LinkProgramQuestion link : existing)
 				{
 					if(toDelete !=null)
 					{
 						link.setDisplayIndex(link.getDisplayIndex()-1);
 						
 						session.merge(link);
 					}
 					if(link.getId() == toMoveId)
 					{
 						toDelete = link;
 					}
 		
 				}
 				if(toDelete != null)
 				{
 					List<QuestionResponse> responsesToQuestion = (List<QuestionResponse>)session
 							.createQuery("FROM QuestionResponse WHERE question.id=:questionId")
 							.setParameter("questionId",questionId).list();
 					for(QuestionResponse resp: responsesToQuestion)
 						session.delete(resp);
 					
 					session.delete(toDelete);
 				}
 			}
 			session.getTransaction().commit();
 			return true;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 	}
 
 	public boolean addQuestionToProgram(int questionId,int programId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			Question q = (Question)session.get(Question.class,questionId);
 			Program p = (Program)session.get(Program.class, programId);
 			LinkProgramQuestion newLink = new LinkProgramQuestion();
 			newLink.setProgram(p);
 			newLink.setQuestion(q);
			int max = (Integer)session.createQuery("select max(displayIndex) from LinkProgramQuestion where program.id = :programId").setParameter("programId",programId).uniqueResult();
 			newLink.setDisplayIndex(max+1);
 			session.save(newLink);
 			session.getTransaction().commit();
 			return true;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 	}
 	@SuppressWarnings("unchecked")
 	public List<Question> getAllQuestions()
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<Question> toReturn = null;
 		try
 		{
 			toReturn = (List<Question>) session.createQuery("FROM Question ORDER BY display")
 					.list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<Question> getAllQuestionsForProgram(Program program)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<Question> toReturn = null;
 		try
 		{
 			toReturn = (List<Question>) session.createQuery("SELECT distinct l.question FROM LinkProgramQuestion l WHERE l.program.id=:programId ORDER BY l.displayIndex")
 					.setParameter("programId", program.getId())
 					.list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
  
 	@SuppressWarnings("unchecked")
 	public List<Question> getAllQuestionsNotUsedByProgram(Program program)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<Question> toReturn = null;
 		try
 		{
 			toReturn = (List<Question>) session.createQuery("FROM Question WHERE id not in (SELECT l.question.id FROM LinkProgramQuestion l WHERE l.program.id=:programId) ORDER BY lower(display)")
 					.setParameter("programId", program.getId())
 					.list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	public Question getQuestionById(int id)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		Question toReturn = null;
 		try
 		{
 			toReturn = (Question) session.get(Question.class,id);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	public boolean deleteQuestion(int questionId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			Question q = (Question)session.get(Question.class,questionId);
 			@SuppressWarnings("unchecked")
 			List<QuestionResponse> responsesToQuestion = (List<QuestionResponse>)session
 					.createQuery("FROM QuestionResponse WHERE question.id=:questionId")
 					.setParameter("questionId",q.getId()).list();
 			for(QuestionResponse resp: responsesToQuestion)
 				session.delete(resp);
 			
 			@SuppressWarnings("unchecked")
 			List<LinkProgramQuestion> questionLinks = (List<LinkProgramQuestion>)session
 					.createQuery("FROM LinkProgramQuestion WHERE question.id=:questionId")
 					.setParameter("questionId",q.getId()).list();
 			for(LinkProgramQuestion qLink: questionLinks)
 				session.delete(qLink);
 			
 			
 			session.delete(q);
 			session.getTransaction().commit();
 			return true;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 	}
 
 	public AnswerSet getAnswerSetById(int id)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		AnswerSet toReturn = null;
 		try
 		{
 			toReturn = (AnswerSet) session.get(AnswerSet.class,id);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	public AnswerOption getAnswerOptionById(int id)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		AnswerOption toReturn = null;
 		try
 		{
 			toReturn = (AnswerOption) session.get(AnswerOption.class,id);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<AnswerSet> getAllAnswerSets()
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<AnswerSet> toReturn = null;
 		try
 		{
 			toReturn = (List<AnswerSet>) session.createQuery("FROM AnswerSet ORDER BY name")
 					.list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<Question> getAllQuestionsWithResponsesForProgram(Program program)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<Question> toReturn = null;
 		try
 		{
 			toReturn = (List<Question>) session.createQuery("SELECT distinct l.question FROM QuestionResponse l WHERE l.program.id=:programId")
 					.setParameter("programId", program.getId())
 					.list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<QuestionResponse> getAllQuestionResponsesForProgramAndOffering(Program program, CourseOffering offering)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<QuestionResponse> toReturn = null;
 		try
 		{
 			toReturn = (List<QuestionResponse>)session.createQuery("FROM QuestionResponse WHERE program.id=:programId AND courseOffering.id=:courseOfferingId").setParameter("programId", program.getId()).setParameter("courseOfferingId", offering.getId()).list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<Question> getAllQuestionsWithResponsesForProgramAndOffering(Program program, CourseOffering offering)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<Question> toReturn = null;
 		try
 		{
 			toReturn = (List<Question>)session.createQuery("SELECT distinct l.question FROM QuestionResponse l WHERE l.program.id=:programId AND l.courseOffering.id=:courseOfferingId").setParameter("programId", program.getId()).setParameter("courseOfferingId", offering.getId()).list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<QuestionResponse> getAllQuestionResponsesForProgram(Program program,Session session)
 	{
 		return  (List<QuestionResponse>)session.createQuery("FROM QuestionResponse WHERE program.id=:programId").setParameter("programId", program.getId()).list();
 	}
 	public List<QuestionResponse> getAllQuestionResponsesForProgram(Program program)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<QuestionResponse> toReturn = null;
 		try
 		{
 			toReturn =  getAllQuestionResponsesForProgram( program,session);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<QuestionResponse> getAllQuestionResponsesForOffering(CourseOffering offering)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<QuestionResponse> toReturn = null;
 		try
 		{
 			toReturn = (List<QuestionResponse>)session.createQuery("FROM QuestionResponse WHERE courseOffering.id=:courseOfferingId").setParameter("courseOfferingId", offering.getId()).list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<String> getAllQuestionIdsWithResponses()
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<String> toReturn = null;
 		try
 		{
 			toReturn = (List<String>)session.createQuery("SELECT distinct ''||question.id FROM QuestionResponse").list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<String> getAllQuestionIdsUsedInProgramsOtherThan(int programId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<String> toReturn = null;
 		try
 		{
 			toReturn = (List<String>)session.createQuery("SELECT distinct ''||question.id FROM LinkProgramQuestion WHERE program.id<>:programId").setParameter("programId",programId).list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<String> getAllAnswerSetIdsWithResponses()
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<String> toReturn = null;
 		try
 		{
 			toReturn = (List<String>)session.createQuery("SELECT distinct ''||q.answerSet.id FROM Question q,QuestionResponse qr WHERE qr.question=q").list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	public boolean clearResponsesForOfferingInProgram(Program p, int courseOfferingId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			
 			@SuppressWarnings("unchecked")
 			List<QuestionResponse> questionResponses = (List<QuestionResponse>)session
 						.createQuery("FROM QuestionResponse WHERE program.id=:programId AND courseOffering.id=:courseOfferingId")
 						.setParameter("programId", p.getId())
 						.setParameter("courseOfferingId", courseOfferingId)
 					    .list();
 				
 			for(QuestionResponse responseToDelete : questionResponses)
 			{
 				session.delete(responseToDelete);
 			}	
 			session.getTransaction().commit();
 			return true;
 		}
 		catch(Exception e)
 		{
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			HibernateUtil.logException(logger, e);
 			return false;
 		}
 	}
 	
 	public boolean saveResponses(Program p, int courseOffering, TreeMap<String,String[]> responses)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			CourseOffering offering = (CourseOffering)session.get(CourseOffering.class, courseOffering);
 			
 		
 			for(String key : responses.keySet())
 			{
 				String questionIdString = key.split("_")[2];
 				Question q = (Question)session.get(Question.class,Integer.parseInt(questionIdString));
 				String[] responseValues = responses.get(key);
 				for(String responseString :responseValues)
 				{
 					
 					QuestionResponse response = new QuestionResponse();
 					response.setProgram(p);
 					response.setCourseOffering(offering);
 					response.setQuestion(q);
 					response.setResponse(responseString);
 					session.save(response);
 				}
 			}
 			session.getTransaction().commit();
 			return true;
 		}
 		catch(Exception e)
 		{
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			HibernateUtil.logException(logger, e);
 			return false;
 		}
 	}
 	
 	
 	@SuppressWarnings("unchecked")
 	public List<QuestionType> getQuestionTypes()
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<QuestionType> toReturn = null;
 		try
 		{
 			toReturn = (List<QuestionType>) session.createQuery("FROM QuestionType")
 					.list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	
 	public static QuestionManager instance()
 	{
 		if (instance == null)
 		{
 			instance = new QuestionManager();
 		}
 		return instance;
 
 	}
 	
 	public String inString(List<Integer> in)
 	{
 		StringBuilder sb = new StringBuilder();
 		for(int i : in)
 		{
 			sb.append(",");
 			sb.append(i);
 			
 		}
 		return sb.substring(1);
 	}
 
 }
