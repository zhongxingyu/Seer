 package ca.usask.gmcte.currimap.action;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.TreeMap;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Session;
 
 import ca.usask.gmcte.currimap.model.Assessment;
 import ca.usask.gmcte.currimap.model.AssessmentFeedbackOption;
 import ca.usask.gmcte.currimap.model.AssessmentFeedbackOptionType;
 import ca.usask.gmcte.currimap.model.AssessmentGroup;
 import ca.usask.gmcte.currimap.model.AssessmentTimeOption;
 import ca.usask.gmcte.currimap.model.Characteristic;
 import ca.usask.gmcte.currimap.model.Course;
 import ca.usask.gmcte.currimap.model.CourseAttribute;
 import ca.usask.gmcte.currimap.model.CourseAttributeValue;
 import ca.usask.gmcte.currimap.model.CourseClassification;
 import ca.usask.gmcte.currimap.model.CourseOffering;
 import ca.usask.gmcte.currimap.model.Department;
 import ca.usask.gmcte.currimap.model.Feature;
 import ca.usask.gmcte.currimap.model.Instructor;
 import ca.usask.gmcte.currimap.model.InstructorAttribute;
 import ca.usask.gmcte.currimap.model.InstructorAttributeValue;
 import ca.usask.gmcte.currimap.model.LinkAssessmentCourseOutcome;
 import ca.usask.gmcte.currimap.model.LinkCourseAssessmentFeedbackOption;
 import ca.usask.gmcte.currimap.model.LinkCourseDepartment;
 import ca.usask.gmcte.currimap.model.LinkCourseOfferingAssessment;
 import ca.usask.gmcte.currimap.model.LinkCourseOfferingContributionProgramOutcome;
 import ca.usask.gmcte.currimap.model.LinkCourseOfferingInstructor;
 import ca.usask.gmcte.currimap.model.LinkCourseOfferingOutcome;
 import ca.usask.gmcte.currimap.model.LinkCourseOfferingOutcomeCharacteristic;
 import ca.usask.gmcte.currimap.model.LinkCourseOfferingTeachingMethod;
 import ca.usask.gmcte.currimap.model.LinkCourseOutcomeProgramOutcome;
 import ca.usask.gmcte.currimap.model.LinkCourseProgram;
 import ca.usask.gmcte.currimap.model.Organization;
 import ca.usask.gmcte.currimap.model.Program;
 import ca.usask.gmcte.currimap.model.TeachingMethod;
 import ca.usask.gmcte.currimap.model.TeachingMethodPortionOption;
 import ca.usask.gmcte.currimap.model.Time;
 import ca.usask.gmcte.currimap.model.TimeItTook;
 import ca.usask.gmcte.util.HTMLTools;
 import ca.usask.gmcte.util.HibernateUtil;
 import ca.usask.ocd.ldap.LdapConnection;
 
 public class CourseManager
 {
 	private static CourseManager instance;
 	private static Logger logger = Logger.getLogger( CourseManager.class );
 
 	public boolean save(String subject, String courseNumber, String title, String description)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 	
 			Course c = new Course();
 	
 			c.setCourseNumber(Integer.parseInt(courseNumber.trim()));
 			c.setSubject(subject.trim().toUpperCase());
 			c.setTitle(title);
 			c.setDescription(description);
 			session.save(c);
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
 	public boolean saveCourseOffering(Course course, String sectionNumber, String term, String medium)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 	
 			CourseOffering c = new CourseOffering();
 	
 			c.setCourse(course);
 			c.setSectionNumber(sectionNumber.toUpperCase());
 			c.setTerm(term);
 			c.setMedium(medium);
 			session.save(c);
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
 	
 	public boolean setCommentsForCourseOffering(int id, String comments)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 		CourseOffering c = (CourseOffering)session.get(CourseOffering.class,id);
 		c.setComments(comments);
 		session.merge(c);
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
 	public boolean updateCourseOffering(CourseOffering c, String sectionNumber, String term, String medium)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 		c.setSectionNumber(sectionNumber.toUpperCase());
 		c.setTerm(term);
 		c.setMedium(medium);
 		session.merge(c);
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
 
 	public boolean updateLinkCourseOfferingTeachingMethod(int id, int teachingMethod, int howLongId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 
 		LinkCourseOfferingTeachingMethod o = (LinkCourseOfferingTeachingMethod) session.get(LinkCourseOfferingTeachingMethod.class, id);
 		TeachingMethod tm = (TeachingMethod) session.get(TeachingMethod.class, teachingMethod);
 		TeachingMethodPortionOption howLong = (TeachingMethodPortionOption) session.get(TeachingMethodPortionOption.class, howLongId);
 		o.setHowLong(howLong);
 		o.setTeachingMethod(tm);
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
	public boolean updateLinkCourseOfferingAssessment(int id, double weight, int whenId,String criterionExists, double criterionLevel,String criterionSubmitted, String criterionCompleted,String[] additionQuestionResponses, String additionalInfo)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 		LinkCourseOfferingAssessment o = (LinkCourseOfferingAssessment) session.get(LinkCourseOfferingAssessment.class, id);
 		deleteExistingAdditionalAssessmentOptions(o,session);
 		createNewAdditionalAssessmentOptions(o,additionQuestionResponses,session);
 		AssessmentTimeOption when = (AssessmentTimeOption) session.get(AssessmentTimeOption.class, whenId);
 		o.setWhen(when);
 		o.setWeight(weight);
 		o.setAdditionalInfo(additionalInfo);
 		o.setCriterionExists(criterionExists);
 		o.setCriterionLevel(criterionLevel);
 		o.setCriterionCompleted(criterionCompleted);
 		o.setCriterionSubmitted(criterionSubmitted);
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
 	private void createNewAdditionalAssessmentOptions(LinkCourseOfferingAssessment link, String[] additionQuestionResponses, Session session)
 	{
 		for(String answer:additionQuestionResponses)
 		{
 			AssessmentFeedbackOption questionOption= (AssessmentFeedbackOption)session.get(AssessmentFeedbackOption.class, Integer.parseInt(answer));
 			LinkCourseAssessmentFeedbackOption newLink = new LinkCourseAssessmentFeedbackOption();
 			newLink.setLinkCourseOfferingAssessment(link);
 			newLink.setOption(questionOption);
 			session.save(newLink);
 		}
 		
 	}
 	private void deleteExistingAdditionalAssessmentOptions(LinkCourseOfferingAssessment link, Session session)
 	{
 		@SuppressWarnings("unchecked")
 		List<LinkCourseAssessmentFeedbackOption> existing = (List<LinkCourseAssessmentFeedbackOption>)session.createQuery("FROM LinkCourseAssessmentFeedbackOption WHERE linkCourseOfferingAssessment.id=:linkId").setParameter("linkId",link.getId()).list();
 		for(LinkCourseAssessmentFeedbackOption o: existing)
 		{
 			session.delete(o);
 		}
 		
 	}
 	public boolean saveTeachingMethod(String name, String description)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 		TeachingMethod o = new TeachingMethod();
 		o.setDescription(description);
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
 	public boolean saveAssessment(String name, String description)
 	{
 		Assessment existing = this.getAssessmentByName(name);
 		if(existing!= null) //if it already exists, don't bother adding it
 		{
 			return true;
 		}
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 		Assessment o = new Assessment();
 		o.setDescription(description);
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
 	public Assessment getAssessmentByName(String name)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		Assessment o = null;
 		try
 		{
 		o = (Assessment)session.createQuery("FROM Assessment WHERE lower(name)=:name").setParameter("name",name.trim().toLowerCase()).uniqueResult();
 		session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return o;
 	}
 
 	public boolean saveLinkCourseOfferingTeachingMethod(int courseOfferingId,int teachingMethod, int howLongId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 		LinkCourseOfferingTeachingMethod o = new LinkCourseOfferingTeachingMethod();
 		CourseOffering c = (CourseOffering) session.get(CourseOffering.class, courseOfferingId);
 		TeachingMethod tm = (TeachingMethod) session.get(TeachingMethod.class, teachingMethod);
 		TeachingMethodPortionOption howLong = (TeachingMethodPortionOption) session.get(TeachingMethodPortionOption.class, howLongId);
 		o.setCourseOffering(c);
 		o.setHowLong(howLong);
 		o.setTeachingMethod(tm);
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
 	
 	
 	public boolean saveLinkCourseOfferingAssessment(int courseOfferingId,int assessmentId, double weight, int whenId,String criterionExists, double criterionLevel,String criterionSubmitted, String criterionCompleted,String[] additionQuestionResponses,String additionalInfo)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 		LinkCourseOfferingAssessment o = new LinkCourseOfferingAssessment();
 		CourseOffering c = (CourseOffering) session.get(CourseOffering.class, courseOfferingId);
 		Assessment a= (Assessment) session.get(Assessment.class, assessmentId);
 		AssessmentTimeOption when = (AssessmentTimeOption) session.get(AssessmentTimeOption.class, whenId);
 		o.setCourseOffering(c);
 		o.setAssessment(a);
 		o.setWhen(when);
 		o.setWeight(weight);
 		o.setCriterionExists(criterionExists);
 		o.setCriterionLevel(criterionLevel);
 		o.setAdditionalInfo(additionalInfo);
 		o.setCriterionCompleted(criterionCompleted);
 		o.setCriterionSubmitted(criterionSubmitted);
 		session.save(o);
 		this.createNewAdditionalAssessmentOptions(o, additionQuestionResponses, session);
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
 
 	public boolean update(Course c, String subject, String courseNumber,
 			String title, String description)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 		c.setCourseNumber(Integer.parseInt(courseNumber));
 		c.setSubject(subject);
 		c.setTitle(title);
 		c.setDescription(description);
 		session.merge(c);
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
 	public CourseOffering getOfferingByCourseAndSectionAndTerm(Course course, String sectionNumber, String term)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		CourseOffering toReturn = null;
 		try
 		{
 			toReturn = (CourseOffering)session.createQuery("FROM CourseOffering WHERE course.id=:courseId AND sectionNumber=:sectionNumber AND term=:term")
 				.setParameter("courseId",course.getId())
 				.setParameter("sectionNumber",sectionNumber)
 				.setParameter("term", term)
 				.uniqueResult();
 		session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	public CourseOffering getOfferingByData(String term,String subject,String courseNumber,String sectionNumber) throws Exception
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		CourseOffering toReturn = null;
 		try
 		{
 		
 			Course course = this.getCourseBySubjectAndNumber(subject, courseNumber,session);
 			if(course != null)
 			{
 				toReturn = (CourseOffering)session.createQuery("FROM CourseOffering WHERE course.id = :courseId AND sectionNumber = :sectionNumber AND term = :term")
 							.setParameter("courseId",+course.getId())
 							.setParameter("sectionNumber",sectionNumber)
 							.setParameter("term",term)
 							.uniqueResult();
 			}
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	private Instructor getInstructorByUserid(String userid, Session session)
 	{
 		Instructor o = (Instructor)session.createQuery("FROM Instructor WHERE userid = :userid").setParameter("userid",userid).uniqueResult();
 		if(o == null)
 		{
 			o = new Instructor();
 			o.setUserid(userid);
 			session.save(o);
 		}
 		return o;
 	}
 	public Instructor getInstructorByUserid(String userid)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		Instructor o = null;
 		try
 		{
 			o = getInstructorByUserid(userid, session);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return o;
 	}
 
 	public CourseOffering getCourseOfferingById(int id)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		CourseOffering c = null;
 		try
 		{
 			c = getCourseOfferingById(id, session);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return c;
 	}
 	private CourseOffering getCourseOfferingById(int id,Session session)
 	{
 		return (CourseOffering)session.get(CourseOffering.class, id);
 	}
 	
 	public Course getCourseById(int id)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		Course c = null;
 		try
 		{
 			c = (Course) session.get(Course.class, id);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return c;
 	}
 
 	public Course getCourseBySubjectAndNumber(String subject, String number)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		Course toReturn = null;
 		try
 		{
 			toReturn = getCourseBySubjectAndNumber(subject,number,session);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	public Course getCourseBySubjectAndNumber(String subject, String number,Session session)
 	{
 		return  (Course)session.createQuery("from Course where upper(subject)=:subject and courseNumber=:courseNumber").setParameter("subject",subject.toUpperCase()).setParameter("courseNumber", Integer.parseInt(number)).uniqueResult();
 	}
 	
 	public boolean addDepartmentToCourse(int departmentId, int courseId)
 	{
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			 Department dept = (Department) session.get(Department.class, departmentId);
 			 Course course = (Course) session.get(Course.class, courseId);
 			 LinkCourseDepartment newLink = new LinkCourseDepartment();
 			 newLink.setCourse(course);
 			 newLink.setDepartment(dept);
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
 	public boolean addInstructorToCourseOffering(String userid, int courseOfferingId)
 	{
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			 Instructor inst = getInstructorByUserid(userid, session);
 			 if(inst == null)
 			 {
 				 inst = new Instructor();
 				 inst.setUserid(userid);
 			 }
 			 CourseOffering courseOffering = (CourseOffering) session.get(CourseOffering.class, courseOfferingId);
 			 LinkCourseOfferingInstructor newLink = new LinkCourseOfferingInstructor();
 			 newLink.setCourseOffering(courseOffering);
 			 newLink.setInstructor(inst);
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
 	public boolean removeDepartmentFromCourse(int linkId)
 	{
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			LinkCourseDepartment link = (LinkCourseDepartment) session.get(LinkCourseDepartment.class, linkId);
 			
 			 session.delete(link);
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
 	public boolean removeInstructorFromCourseOffering(int linkId)
 	{
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			LinkCourseOfferingInstructor link = (LinkCourseOfferingInstructor) session.get(LinkCourseOfferingInstructor.class, linkId);
 			
 			 session.delete(link);
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
 	
 	public LinkCourseOfferingAssessment getLinkAssessmentById(int id)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		LinkCourseOfferingAssessment c = null;
 		try
 		{
 			c = (LinkCourseOfferingAssessment) session.get(LinkCourseOfferingAssessment.class, id);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return c;
 	}
 	public LinkCourseOfferingTeachingMethod getLinkTeachingMethodById(int id)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		LinkCourseOfferingTeachingMethod c = null;
 		try
 		{
 			c = (LinkCourseOfferingTeachingMethod) session.get(LinkCourseOfferingTeachingMethod.class, id);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return c;
 	}
 
 	public LinkCourseOfferingTeachingMethod getLinkTeachingMethodByData(CourseOffering co, TeachingMethod tm)
 	{
 		LinkCourseOfferingTeachingMethod c = null;
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			c = getLinkTeachingMethodByData(co,tm,session);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return c;
 	}
 	public LinkCourseOfferingTeachingMethod getLinkTeachingMethodByData(CourseOffering co, TeachingMethod tm,Session session)
 	{
 		LinkCourseOfferingTeachingMethod c = (LinkCourseOfferingTeachingMethod)session.createQuery("FROM LinkCourseOfferingTeachingMethod WHERE courseOffering.id = :coId AND teachingMethod.id=:tmId")
 				.setParameter("coId",co.getId())
 				.setParameter("tmId",tm.getId())
 				.uniqueResult();
 		return c;
 	}
 	@SuppressWarnings("unchecked")
 	public List<AssessmentTimeOption> getAssessmentTimeOptions()
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<AssessmentTimeOption> toReturn = null;
 		try
 		{
 		
 			toReturn = (List<AssessmentTimeOption>)session.createQuery("from AssessmentTimeOption order by displayIndex").list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<Feature> getFeatures()
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<Feature> toReturn = null;
 		try
 		{
 		
 			toReturn = (List<Feature>)session.createQuery("from Feature order by displayIndex").list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<TimeItTook> getTimeItTookOptions()
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<TimeItTook> toReturn = null;
 		try
 		{
 		
 			toReturn = (List<TimeItTook>)session.createQuery("from TimeItTook order by displayIndex").list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 
 	public boolean saveTimeItTook(int courseOfferingId, int timeItTookId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			CourseOffering c = (CourseOffering)session.get(CourseOffering.class, courseOfferingId);
 			TimeItTook time = (TimeItTook)session.get(TimeItTook.class, timeItTookId);
 			c.setTimeItTook(time);
 			session.merge(c);
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
 	public List<TeachingMethodPortionOption> getTeachingMethodPortionOptions()
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<TeachingMethodPortionOption> toReturn =null;
 		try
 		{
 		
 			toReturn = (List<TeachingMethodPortionOption>)session.createQuery("from TeachingMethodPortionOption order by displayIndex").list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<CourseOffering> getCourseOfferingsWithCharacteristicForProgram(Program p, Characteristic c)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<CourseOffering> toReturn = null;
 		try
 		{
 			StringBuilder sql = new StringBuilder();
 			sql.append("SELECT {co.*} ");
 			sql.append("  FROM courseOffering co, ");
 			sql.append("       link_courseOffering_outcome lco,");
 			sql.append("       link_courseOffering_outcome_characteristic lcoc, ");
 			sql.append("       characteristic c, ");
 			sql.append("       link_courseOffering_program lpc");
 			sql.append(" WHERE c.id=lcoc.characteristic_id  ");
 			sql.append("   AND lcoc.link_courseOffering_outcome_id = lco.id ");
 			sql.append("   AND lco.courseOffering_id=co.id " );
 			sql.append("   AND c.id=:charId ");
 			sql.append("   AND lpc.courseOffering_id = c.id");
 			sql.append("   AND lpc.program_id = :programId ");
 			sql.append("ORDER BY co.subject, co.courseOffering_number ");
 			
 			logger.error(sql.toString());
 			
 		
 			toReturn = (List<CourseOffering>)session.createSQLQuery(sql.toString()).addEntity("co",CourseOffering.class).setParameter("charId",c.getId()).setParameter("programId", p.getId()).list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	
 	public boolean isAlreadyPartOfProgram(int programId,int courseId)
 	{
 		return getLinkCourseProgramByCourseAndProgram(programId,courseId) != null;
 	}
 	public boolean deleteTeachingMethod(int courseOfferingId, int teachingMethodId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			CourseOffering c = (CourseOffering)session.get(CourseOffering.class, courseOfferingId);
 			TeachingMethod tm = (TeachingMethod)session.get(TeachingMethod.class, teachingMethodId);
 			LinkCourseOfferingTeachingMethod link = this.getLinkTeachingMethodByData(c, tm,session);
 			session.delete(link);
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
 	public boolean addTeachingMethod(int courseOfferingId, int teachingMethodId, int howLongId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			CourseOffering c = (CourseOffering)session.get(CourseOffering.class, courseOfferingId);
 			TeachingMethod tm = (TeachingMethod)session.get(TeachingMethod.class, teachingMethodId);
 			TeachingMethodPortionOption howLong  =(TeachingMethodPortionOption)session.get(TeachingMethodPortionOption.class,howLongId);
 			LinkCourseOfferingTeachingMethod link = this.getLinkTeachingMethodByData(c, tm,session);
 			if(link == null)
 			{
 				link = new LinkCourseOfferingTeachingMethod();
 				link.setCourseOffering(c);
 				link.setHowLong(howLong);
 				link.setTeachingMethod(tm);
 				session.save(link);
 			}
 			else
 			{
 				link.setHowLong(howLong);
 				session.merge(link);
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
 	
 	public boolean deleteAssessment(int linkId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			@SuppressWarnings("unchecked")
 			List<LinkCourseAssessmentFeedbackOption> exitsingOptions = session.createQuery("FROM LinkCourseAssessmentFeedbackOption WHERE linkCourseOfferingAssessment.id = :linkId").setParameter("linkId",linkId).list();
 			for(LinkCourseAssessmentFeedbackOption toDelete : exitsingOptions)
 			{
 				session.delete(toDelete);
 			}
 			LinkCourseOfferingAssessment link = (LinkCourseOfferingAssessment) session.get(LinkCourseOfferingAssessment.class, linkId);
 			session.delete(link);
 			
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
 	public LinkCourseProgram getLinkCourseProgramByCourseAndProgram(int programId,int courseId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		LinkCourseProgram toReturn = null;
 		try
 		{
 			toReturn = (LinkCourseProgram) session.createQuery("SELECT l FROM LinkCourseProgram l WHERE l.course.id = :courseId and l.program.id=:programId").setParameter("courseId",courseId).setParameter("programId",programId).uniqueResult();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<CourseOffering> getCourseOfferingsForCourse(Course course)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<CourseOffering> toReturn = null;
 		try
 		{
 	
 			toReturn = (List<CourseOffering>) session.createQuery("FROM CourseOffering WHERE course.id = :courseId ORDER BY term, sectionNumber").setParameter("courseId",course.getId()).list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<CourseOffering> getCourseOfferingsForCourseWithProgramOutcomeData(Course course)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<CourseOffering> toReturn = null;
 		try
 		{
 	
 			toReturn = (List<CourseOffering>) session.createQuery("SELECT distinct l.courseOffering FROM LinkCourseOutcomeProgramOutcome l WHERE l.courseOffering.course.id = :courseId").setParameter("courseId",course.getId()).list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<LinkCourseOfferingContributionProgramOutcome> getCourseOfferingsContributionsForCourse(Course course)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<LinkCourseOfferingContributionProgramOutcome> toReturn = null;
 		try
 		{
 	
 			toReturn = (List<LinkCourseOfferingContributionProgramOutcome>) 
 					session.createQuery("FROM LinkCourseOfferingContributionProgramOutcome l "+
 			                           "WHERE l.courseOffering.course.id = :courseId "+
 							            " AND (l.contribution.calculationValue + l.mastery.calculationValue) > 0 "+
 			                           " ORDER BY l.linkProgramOutcome.programOutcome.id,l.courseOffering.term, l.courseOffering.sectionNumber").setParameter("courseId",course.getId()).list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<CourseOffering> getCourseOfferingsWithoutDataForCourse(Course course)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<CourseOffering> toReturn = null;
 		try
 		{
 		
 			toReturn = (List<CourseOffering>) session
 					.createQuery("FROM CourseOffering WHERE course.id = :courseId AND id NOT IN (SELECT l.courseOffering.id FROM LinkCourseOfferingTeachingMethod l WHERE l.courseOffering.course.id=:courseId) ORDER BY term, sectionNumber")
 					.setParameter("courseId",course.getId())
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
 	public List<String> getAvailableTermsForCourse(Course course)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<String> toReturn = null;
 		try
 		{
 		
 			toReturn = (List<String>) session.createQuery("SELECT distinct term FROM CourseOffering WHERE course.id = :courseId ORDER BY term DESC").setParameter("courseId",course.getId()).list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<TeachingMethod> getTeachingMethodsNotUsed(CourseOffering c)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<TeachingMethod> toReturn = null;
 		try
 		{
 			toReturn = (List<TeachingMethod>) session.createQuery("FROM TeachingMethod tm WHERE tm.id NOT IN (SELECT ltm.teachingMethod.id FROM LinkCourseOfferingTeachingMethod ltm WHERE ltm.courseOffering.id = :courseOfferingId) order by tm.name").setParameter("courseOfferingId",c.getId()).list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		
 		return toReturn;
 	}
 	public List<LinkCourseOfferingTeachingMethod> getTeachingMethods(CourseOffering c)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<LinkCourseOfferingTeachingMethod> toReturn = null;
 		try
 		{
 			toReturn = getTeachingMethods(c,session);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<LinkCourseOfferingTeachingMethod> getTeachingMethods(CourseOffering c,Session session)
 	{
 		return  (List<LinkCourseOfferingTeachingMethod>) session.createQuery("FROM LinkCourseOfferingTeachingMethod l WHERE l.courseOffering.id=:courseOfferingId ORDER BY l.teachingMethod.name").setParameter("courseOfferingId",c.getId()).list();
 	}
 	@SuppressWarnings("unchecked")
 	public List<TeachingMethod> getAllTeachingMethods()
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<TeachingMethod> toReturn = null;
 		try
 		{
 			toReturn = (List<TeachingMethod>) session.createQuery("FROM TeachingMethod order by displayIndex").list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	public List<LinkCourseOfferingAssessment> getAssessmentsForCourseOffering(CourseOffering c)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<LinkCourseOfferingAssessment> toReturn = null;
 		try
 		{
 			toReturn = getAssessmentsForCourseOffering(c,session);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<LinkCourseOfferingAssessment> getAssessmentsUsed(int assessmentId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<LinkCourseOfferingAssessment> toReturn = null;
 		try
 		{
 			toReturn = (List<LinkCourseOfferingAssessment>) session
 					.createQuery("FROM LinkCourseOfferingAssessment l WHERE l.assessment.id=:assessmentId")
 					.setParameter("assessmentId",assessmentId).list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<LinkCourseOfferingAssessment> getAssessmentsForCourseOffering(CourseOffering c,Session session)
 	{
 		return (List<LinkCourseOfferingAssessment>) session
 					.createQuery("FROM LinkCourseOfferingAssessment l WHERE l.courseOffering.id=:courseOfferingId ORDER BY l.when.displayIndex")
 					.setParameter("courseOfferingId",c.getId()).list();
 	}
 	
 	
 	public LinkCourseOfferingAssessment getLinkCourseOfferingAssessmentById(int id)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		LinkCourseOfferingAssessment toReturn = null;
 		try
 		{
 			toReturn = 
 				(LinkCourseOfferingAssessment) session.get(LinkCourseOfferingAssessment.class,id);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<Assessment> getAssessments()
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<Assessment> toReturn = null;
 		try
 		{
 			toReturn = (List<Assessment>) session.createQuery("FROM Assessment a ORDER BY a.group.displayIndex, a.displayIndex").list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	public Assessment getAssessmentById(int id)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		Assessment toReturn = null;
 		try
 		{
 			toReturn = (Assessment) session.get(Assessment.class, id);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	public AssessmentGroup getAssessmentGroupById(int id, Session session)
 	{
 		return (AssessmentGroup) session.get(AssessmentGroup.class, id);
 	}
 	public AssessmentGroup getAssessmentGroupById(int id)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		AssessmentGroup toReturn = null;
 		try
 		{
 			toReturn =  getAssessmentGroupById(id, session);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 
 	public boolean saveAssessmentMethodName(int id, String newName)
 	{
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			Assessment o = (Assessment)session.get(Assessment.class, id);
 			o.setName(newName);
 			session.merge(o);
 			return true;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 
 	}
 
 	public boolean saveAssessmentDescriptionById(int id, String newValue)
 	{
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			Assessment o = (Assessment)session.get(Assessment.class, id);
 			o.setDescription(newValue);
 			session.merge(o);
 			return true;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 
 	}
 	public boolean saveAssessmentGroupName(int id, String newName)
 	{
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			AssessmentGroup o = (AssessmentGroup)session.get(AssessmentGroup.class, id);
 			o.setName(newName);
 			session.merge(o);
 			return true;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 	}
 	public boolean saveAssessmentGroupShortName(int id, String newName)
 	{
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			AssessmentGroup o = (AssessmentGroup)session.get(AssessmentGroup.class, id);
 			o.setShortName(newName);
 			session.merge(o);
 			return true;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 	}
 	public boolean addAssessmentMethodToGroup(int groupId, String newName)
 	{
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			 AssessmentGroup group =  getAssessmentGroupById(groupId, session);
 			 List<Assessment> existing = this.getAssessmentsForGroup(group,session);
 			 Assessment o = new Assessment();
 			 o.setGroup(group);
 			 o.setName(newName);
 			 o.setDescription("");
 			 o.setDisplayIndex(existing.size()+1);
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
 	public boolean createAssessmentGroup(String newName)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			List<AssessmentGroup> existing = this.getAssessmentGroups(session);
 			
 			AssessmentGroup group = new AssessmentGroup();
 			group.setName(newName);
 			group.setShortName("Not created yet");
 			group.setDisplayIndex(existing.size()+1);
 			session.save(group);
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
 	public boolean moveAssessmentMethod(int id, int groupId, String direction)
 	{
 		//when moving up, find the one to be moved (while keeping track of the previous one) and swap display_index values
 		//when moving down, find the one to be moved, swap displayIndex values of it and the next one
 		//when deleting, reduce all links following one to be deleted by 1
 		boolean done = false;
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			AssessmentGroup group = (AssessmentGroup)session.get(AssessmentGroup.class, groupId);
 			List<Assessment> existing = this.getAssessmentsForGroup(group,session);
 			if(direction.equals("up"))
 			{
 				Assessment prev = null;
 				for(Assessment a : existing)
 				{
 					if(a.getId() == id && prev!=null)
 					{
 						int swap = prev.getDisplayIndex();
 						prev.setDisplayIndex(a.getDisplayIndex());
 						a.setDisplayIndex(swap);
 						session.merge(prev);
 						session.merge(a);
 						done = true;
 						break;
 					}
 					prev = a;
 				}
 			}
 			else if(direction.equals("down"))
 			{
 				Assessment prev = null;
 				for(Assessment a : existing)
 				{
 					if(prev !=null)
 					{
 						int swap = prev.getDisplayIndex();
 						prev.setDisplayIndex(a.getDisplayIndex());
 						a.setDisplayIndex(swap);
 						session.merge(prev);
 						session.merge(a);
 						done = true;
 						break;
 					}
 					if(a.getId() == id)
 					{
 						prev = a;
 					}
 					
 				}
 			}
 			else if(direction.equals("delete"))
 			{
 				Assessment toDelete = null;
 				for(Assessment a : existing)
 				{
 					if(toDelete !=null)
 					{
 						a.setDisplayIndex(a.getDisplayIndex()-1);
 						session.merge(a);
 					}
 					if(a.getId() == id)
 					{
 						toDelete = a;
 					}
 					
 				}
 				if(toDelete !=null)
 				{
 					session.delete(toDelete);
 					done = true;
 				}
 			}
 			session.getTransaction().commit();
 			return done;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 	}
 	public boolean moveAssessmentMethodGroup(int id, String direction)
 	{
 		//when moving up, find the one to be moved (while keeping track of the previous one) and swap display_index values
 		//when moving down, find the one to be moved, swap displayIndex values of it and the next one
 		//when deleting, reduce all links following one to be deleted by 1
 		boolean done = false;
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			List<AssessmentGroup> existing = this.getAssessmentGroups(session);
 			if(direction.equals("group_up"))
 			{
 				AssessmentGroup prev = null;
 				for(AssessmentGroup a : existing)
 				{
 					if(a.getId() == id && prev!=null)
 					{
 						int swap = prev.getDisplayIndex();
 						prev.setDisplayIndex(a.getDisplayIndex());
 						a.setDisplayIndex(swap);
 						session.merge(prev);
 						session.merge(a);
 						done = true;
 						break;
 					}
 					prev = a;
 				}
 			}
 			else if(direction.equals("group_down"))
 			{
 				AssessmentGroup prev = null;
 				for(AssessmentGroup a : existing)
 				{
 					if(prev !=null)
 					{
 						int swap = prev.getDisplayIndex();
 						prev.setDisplayIndex(a.getDisplayIndex());
 						a.setDisplayIndex(swap);
 						session.merge(prev);
 						session.merge(a);
 						done = true;
 						break;
 					}
 					if(a.getId() == id)
 					{
 						prev = a;
 					}
 					
 				}
 			}
 			else if(direction.equals("group_delete"))
 			{
 				AssessmentGroup toDelete = null;
 				for(AssessmentGroup a : existing)
 				{
 					if(toDelete !=null)
 					{
 						a.setDisplayIndex(a.getDisplayIndex()-1);
 						session.merge(a);
 					}
 					if(a.getId() == id)
 					{
 						toDelete = a;
 					}
 					
 				}
 				if(toDelete !=null)
 				{
 					session.delete(toDelete);
 					done = true;
 				}
 			}
 			session.getTransaction().commit();
 			return done;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<Assessment> getAssessmentsForGroup(AssessmentGroup group, Session session)
 	{
 		return session.createQuery("FROM Assessment a WHERE a.group.id = :groupId ORDER BY a.group.displayIndex, a.displayIndex").setParameter("groupId",group.getId()).list();
 	}
 
 	public List<Assessment> getAssessmentsForGroup(AssessmentGroup group)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<Assessment> toReturn = null;
 		try
 		{
 			toReturn = getAssessmentsForGroup(group, session);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<AssessmentGroup> getAssessmentGroups(Session session)
 	{
 		return  (List<AssessmentGroup>) session.createQuery("FROM AssessmentGroup a ORDER BY displayIndex").list();
 	}
 
 	public List<AssessmentGroup> getAssessmentGroups()
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<AssessmentGroup> toReturn = null;
 		try
 		{
 			toReturn = getAssessmentGroups(session);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	public TeachingMethod getTeachingMethodByName(String name)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		TeachingMethod toReturn = null;
 		try
 		{
 			toReturn = (TeachingMethod)session.createQuery("FROM TeachingMethod a WHERE name=:name").setParameter("name",name).uniqueResult();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	public Course getCourseForLinkProgram(int id)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		Course c = null;
 		try
 		{
 			c = (Course) session.createQuery("SELECT l.course FROM LinkCourseProgram l WHERE l.id = :linkId").setParameter("linkId",id).uniqueResult();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return c;
 	}
 
 	public LinkCourseProgram getLinkCourseProgramById(int id)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		LinkCourseProgram c = null;
 		try
 		{
 			c = (LinkCourseProgram) session.get(LinkCourseProgram.class, id);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return c;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<String> getCourseSubjects()
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<String> toReturn = null;
 		try
 		{
 			toReturn = (List<String>)session.createQuery("select distinct subject from Course order by subject").list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<String> getCourseNumbersForSubject(String subject)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<String> toReturn  = new ArrayList<String>();
 		try
 		{
 			List<Integer> intValuesList = (List<Integer>)session.createQuery("select distinct courseNumber from Course where subject=:subject order by courseNumber" ).setParameter("subject",subject).list();
 		
 			for(Integer n : intValuesList)
 			{
 				toReturn.add(n.toString());
 			}
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	public boolean copyDataFromOfferingToOffering(int sourceOffering, int targetOffering, int programId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 	
 			CourseOffering target = this.getCourseOfferingById(targetOffering,session);
 			CourseOffering source = this.getCourseOfferingById(sourceOffering,session);
 			Department dept = this.getDepartmentForCourse(source.getCourse(),session).get(0);
 		
 			//TeachingMethods
 			List<LinkCourseOfferingTeachingMethod> teachingMethods = this.getTeachingMethods(source,session);
 			for(LinkCourseOfferingTeachingMethod tm:teachingMethods)
 			{
 				LinkCourseOfferingTeachingMethod newLink = new LinkCourseOfferingTeachingMethod();
 				newLink.setCourseOffering(target);
 				newLink.setHowLong(tm.getHowLong());
 				newLink.setTeachingMethod(tm.getTeachingMethod());
 				session.save(newLink);
 			}
 			//assessmentMethods
 			List<LinkCourseOfferingAssessment> assessmentMethods = this.getAssessmentsForCourseOffering(source,session);
 			for(LinkCourseOfferingAssessment am: assessmentMethods)
 			{
 				LinkCourseOfferingAssessment newLink = new LinkCourseOfferingAssessment();
 				newLink.setCourseOffering(target);
 				newLink.setAssessment(am.getAssessment());
 				newLink.setWeight(am.getWeight());
 				newLink.setWhen(am.getWhen());
 				newLink.setCriterionExists(am.getCriterionExists());
 				newLink.setCriterionLevel(am.getCriterionLevel());
 				session.save(newLink);
 				List<AssessmentFeedbackOption> fbOptions = this.getAssessmentOptionsSelectedForLinkOffering(am.getId(),session);
 				for(AssessmentFeedbackOption fbOption : fbOptions)
 				{
 					LinkCourseAssessmentFeedbackOption newfbOptionLink = new LinkCourseAssessmentFeedbackOption();
 					newfbOptionLink.setLinkCourseOfferingAssessment(newLink);
 					newfbOptionLink.setOption(fbOption);
 					session.save(newfbOptionLink);
 				}
 			}
 			//outcomes
 			List<LinkCourseOfferingOutcome> outcomes = OutcomeManager.instance().getLinkCourseOfferingOutcome(source,session);
 			for(LinkCourseOfferingOutcome outcomeLink:outcomes)
 			{
 				LinkCourseOfferingOutcome newLink  = new LinkCourseOfferingOutcome();
 				newLink.setCourseOffering(target);
 				newLink.setCourseOutcome(outcomeLink.getCourseOutcome());
 				session.save(newLink);
 				List<Characteristic> outcomeCharacteristics = OutcomeManager.instance().getCharacteristicsForCourseOfferingOutcome(source, outcomeLink.getCourseOutcome(), dept,session);
 				for(Characteristic characteristic : outcomeCharacteristics)
 				{
 					LinkCourseOfferingOutcomeCharacteristic newCharLink = new LinkCourseOfferingOutcomeCharacteristic();
 					newCharLink.setCharacteristic(characteristic);
 					newCharLink.setCreatedByUserid("exported");
 					newCharLink.setCreatedOn(Calendar.getInstance().getTime());
 					newCharLink.setLinkCourseOfferingOutcome(newLink);
 					session.save(newCharLink);
 				}
 			}
 			
 			List<LinkAssessmentCourseOutcome> existingLinks = OutcomeManager.instance().getLinkAssessmentCourseOutcomes(source.getId(), session);
 			for(LinkAssessmentCourseOutcome outcomeLink : existingLinks)
 			{
 				LinkAssessmentCourseOutcome newLink = new LinkAssessmentCourseOutcome();
 				newLink.setCourseOffering(target);
 				newLink.setOutcome(outcomeLink.getOutcome());
 				newLink.setAssessmentLink(outcomeLink.getAssessmentLink());
 				session.save(newLink);
 			}
 			
 			List<LinkCourseOfferingContributionProgramOutcome> contributionLinks = ProgramManager.instance().getCourseOfferingContributionLinks(source, session);
 			for(LinkCourseOfferingContributionProgramOutcome link: contributionLinks)
 			{
 				
 				LinkCourseOfferingContributionProgramOutcome newLink = ProgramManager.instance().getCourseOfferingContributionLinksForProgramOutcome(target, link.getLinkProgramOutcome(),session);
 				if(newLink == null)
 					newLink = new LinkCourseOfferingContributionProgramOutcome();
 				
 				newLink.setLinkProgramOutcome(link.getLinkProgramOutcome());
 				newLink.setCourseOffering(target);
 				newLink.setContribution(link.getContribution());
 				newLink.setMastery(link.getMastery());
 				session.save(newLink);
 			}
 			List<LinkCourseOutcomeProgramOutcome> links = ProgramManager.instance().getCourseOutcomeLinks(source, session);
 			for(LinkCourseOutcomeProgramOutcome link:links)
 			{
 				LinkCourseOutcomeProgramOutcome newLink = new LinkCourseOutcomeProgramOutcome();
 				newLink.setCourseOffering(target);
 				newLink.setCourseOutcome(link.getCourseOutcome());
 				newLink.setProgramOutcome(link.getProgramOutcome());
 				session.save(newLink);
 			}
 			
 			//comments
 			target.setComments(source.getComments());
 			session.merge(target);
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
 	public List<CourseClassification> getCourseClassifications()
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<CourseClassification> toReturn = null;
 		try
 		{
 			toReturn = (List<CourseClassification>)session.createQuery("from CourseClassification").list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<Time> getCourseTimes()
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<Time> toReturn = null;
 		try
 		{
 			toReturn = (List<Time>)session.createQuery("from Time order by optionDisplayIndex").list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<AssessmentFeedbackOptionType> getAssessmentFeedbackQuestions()
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<AssessmentFeedbackOptionType> toReturn = null;
 		try
 		{
 			toReturn = (List<AssessmentFeedbackOptionType>)session.createQuery("from AssessmentFeedbackOptionType order by displayIndex").list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	
 	public List<AssessmentFeedbackOption> getAssessmentOptionsSelectedForLinkOffering(int linkId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<AssessmentFeedbackOption> toReturn = null;
 		try
 		{
 			toReturn = getAssessmentOptionsSelectedForLinkOffering(linkId,session);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	private  List<AssessmentFeedbackOption> getAssessmentOptionsSelectedForLinkOffering(int linkId,Session session)
 	{
 		return (List<AssessmentFeedbackOption>)session.createQuery("SELECT l.option from LinkCourseAssessmentFeedbackOption l WHERE l.linkCourseOfferingAssessment.id=:linkId").setParameter("linkId",linkId).list();
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<AssessmentFeedbackOption> getAssessmentOptionsForQuestion(int typeId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<AssessmentFeedbackOption> toReturn = null;
 		try
 		{
 			toReturn = (List<AssessmentFeedbackOption>)session.createQuery("from AssessmentFeedbackOption WHERE type.id=:typeId order by displayIndex").setParameter("typeId",typeId).list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	
 	public List<String[]> getTeachingCourses(String userid)
 	{
 		List<String[]> toReturn = new ArrayList<String[]>();
 		
 		try
 		{
 			LdapConnection ldap = LdapConnection.instance();
 			ArrayList<String> sections = ldap.getTeachingGroups(userid);
 			for(String section : sections )
 			{
 				toReturn.add(section.split("_"));	
 			}
 		}
 		catch(Exception e)
 		{
 			logger.error("Something went wrong while retrieveing sections taught by "+userid,e);
 		}
 		HashMap<String,CourseOffering> instructorOfferings = PermissionsManager.instance().getOfferingsForUser(userid, new HashMap<String,Organization>());
 		for(CourseOffering courseOffering : instructorOfferings.values())
 		{
 			if(!containsSection(toReturn, courseOffering))
 			{
 				toReturn.add(new String[]{courseOffering.getTerm(),courseOffering.getCourse().getSubject(),""+courseOffering.getCourse().getCourseNumber(), courseOffering.getSectionNumber()});
 			}
 		}
 		return toReturn;
 	}
 	private boolean containsSection(List<String[]> listOfArray, CourseOffering c)
 	{
 		for(String[] array: listOfArray)
 		{
 			if(array[0].equals(c.getTerm()) && array[1].equals(c.getCourse().getSubject()) && array[2].equals(""+c.getCourse().getCourseNumber()) && array[3].equals(c.getSectionNumber()))
 					return true;
 		}
 		return false;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<LinkCourseOfferingInstructor> getCourseOfferingInstructors(int courseOfferingId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<LinkCourseOfferingInstructor> toReturn = new ArrayList<LinkCourseOfferingInstructor>();
 
 		try
 		{
 			toReturn = session.createQuery("FROM LinkCourseOfferingInstructor WHERE courseOffering.id = :courseOfferingId").setParameter("courseOfferingId",courseOfferingId).list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		
 		return toReturn;
 	}
 	
 	public String getInstructorsString(CourseOffering offering, boolean admin, String programId) throws Exception
 	{
 		StringBuilder output = new StringBuilder();
 		
 		Course course = offering.getCourse();
 		List<String> userids = new ArrayList<String>();
 		try
 		{
 			StringBuilder groupName = new StringBuilder();
 			
 			groupName.append(offering.getTerm());
 			groupName.append("_");
 			groupName.append(course.getSubject());
 			groupName.append("_");
 			groupName.append(course.getCourseNumber());
 			groupName.append("_");
 			groupName.append(offering.getSectionNumber());
 			groupName.append("_leaders");
 			LdapConnection ldapConnection = LdapConnection.instance();
 			userids = ldapConnection.getGroupMembers(groupName.toString());
 			logger.error(groupName.toString());
 		}
 		catch(Exception e)
 		{
 			output.append("Unable to retrieve instructor(s) from ldap! ");
 		}	
 		List<LinkCourseOfferingInstructor> dbInstructors = this.getCourseOfferingInstructors(offering.getId());
 		for(LinkCourseOfferingInstructor instr : dbInstructors)
 		{
 			if(!userids.contains(instr.getInstructor().getUserid()))
 			{
 				userids.add(instr.getInstructor().getUserid());
 			}
 		}
 		
 		if(userids.size()> 0 )
 		{
 			output.append("Instructors : ");
 		}
 		for(int i= 0 ; i < userids.size() ; i++)
 		{
 			String userid = userids.get(i);
 			try
 			{
 				LdapConnection ldapConnection = LdapConnection.instance();
 				TreeMap<String,String> data = ldapConnection.getUserData(userid);	
 				output.append(data.get("givenName"));
 				output.append(" ");
 				output.append(data.get("sn"));
 			
 			}
 			catch(Exception e)
 			{
 				output.append("Error retrieving name");
 			}	
 			
 			output.append("( ");
 			output.append(userid);
 			int programIdInt = HTMLTools.getInt(programId);
 			if(programIdInt > -1)
 			{
 				List<InstructorAttributeValue> attrValues = getInstructorAttributeValues(userid, programIdInt);
 				String prevAttr = "";
 				if(attrValues != null  && !attrValues.isEmpty())
 				{
 					output.append(" [");
 					boolean first = true;
 					for(InstructorAttributeValue av : attrValues)
 					{
 						if(av.getAttribute().getName().equals(prevAttr))
 						{
 							output.append(",");
 							output.append(av.getValue());
 						}
 						else
 						{
 							if(!first)
 								output.append(" | ");
 							else
 								first = false;
 							output.append(av.getAttribute().getName());
 							output.append(":");
 							output.append(av.getValue());	
 						}
 						prevAttr = av.getAttribute().getName();
 					}
 					output.append("] ");
 				}
 			}
 			if(admin)
 			{
 				output.append("<a href=\"javascript:loadModify('/cat/auth/modifyProgram/modifyInstructorAttributes.jsp?program_id=");
 				output.append(programId);
 				output.append("&userid=");
 				output.append(userid);
 				output.append("&course_id=");
 				output.append(course.getId());
 				output.append("');\"><img src=\"/cat/images/edit_16.gif\" alt=\"Edit Instructor Attributes\" title=\"Edit Instructor Attributes\"></a>");
 			}
 			output.append(" ) ");
 			
 			if(i < userids.size() -1)
 			{
 				output.append(" , ");
 			}
 		}
 
 		return output.toString();
 		
 	}
 	public String getCourseAttributesString(Course c, int programId, boolean admin)
 	{
 		List<CourseAttribute> attrTypes = getCourseAttributes(programId);
 		if(attrTypes.isEmpty())
 			return "";
 		
 		StringBuilder output = new StringBuilder();
 		List<CourseAttributeValue> attrValues = getCourseAttributeValues(c.getId(), programId);
 		String prevAttr = "";
 	
 		if(attrValues != null  && !attrValues.isEmpty())
 		{
 			output.append(" [");
 			boolean first = true;
 			for(CourseAttributeValue av : attrValues)
 			{
 				if(av.getAttribute().getName().equals(prevAttr))
 				{
 					output.append(",");
 					output.append(av.getValue());
 				}
 				else
 				{
 					if(!first)
 						output.append(" | ");
 					else
 						first = false;
 					output.append(av.getAttribute().getName());
 					output.append(":");
 					output.append(av.getValue());	
 				}
 				prevAttr = av.getAttribute().getName();
 			}
 			output.append("] ");
 		}
 		if(admin)
 		{
 			output.append("<a href=\"javascript:loadModify('/cat/auth/modifyProgram/modifyCourseAttributes.jsp?program_id=");
 			output.append(programId);
 			output.append("&course_id=");
 			output.append(c.getId());
 			output.append("');\"><img src=\"/cat/images/edit_16.gif\" alt=\"Edit Course Attributes\" title=\"Edit Course Attributes\"></a>");
 		}
 		return output.toString();
 	}
 
 	public boolean editInstructorAttributeValue(int id, String value)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			InstructorAttributeValue o = (InstructorAttributeValue)session.get(InstructorAttributeValue.class,id);
 			o.setValue(value);
 			session.merge(o);
 			session.getTransaction().commit();
 			return true;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			return false;
 		}
 	}	
 	public boolean saveInstructorAttributeValue(int attributeTypeId, String value,String userid)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			Instructor instructor = this.getInstructorByUserid(userid, session);
 			InstructorAttribute attribute = (InstructorAttribute)session.get(InstructorAttribute.class,attributeTypeId);
 			InstructorAttributeValue o = new InstructorAttributeValue();
 			o.setValue(value);
 			o.setAttribute(attribute);
 			o.setInstructor(instructor);
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
 	
 	public InstructorAttributeValue getInstructorAttributeValueById(int id)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		InstructorAttributeValue o = null;
 		try
 		{
 			o = (InstructorAttributeValue)session.get(InstructorAttributeValue.class,id);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return o;
 	}	
 	
 	public InstructorAttribute getInstructorAttributeById(int id)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		InstructorAttribute o = null;
 		try
 		{
 			o = getInstructorAttributeById(id,session);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return o;
 	}	
 	
 	private InstructorAttribute getInstructorAttributeById(int id, Session session)
 	{
 		return (InstructorAttribute)session.get(InstructorAttribute.class,id);
 	}
 	@SuppressWarnings("unchecked")
 	public List<InstructorAttributeValue> getInstructorAttributeValues(String userid, int programId)
 	{
 		Program p = ProgramManager.instance().getProgramById(programId);
 		Organization org = OrganizationManager.instance().getOrganizationByProgram(p);
 		if(org.getParentOrganization() !=null)
 			org = org.getParentOrganization();
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<InstructorAttributeValue> toReturn =  null;
 		try
 		{
 		
 		
 			toReturn =  (List<InstructorAttributeValue>)session
 				 .createQuery("FROM InstructorAttributeValue iav WHERE iav.instructor.userid=:userid AND iav.attribute.organization.id = :orgId ORDER BY iav.attribute.name, iav.value")
 				 .setParameter("userid",userid)
 				 .setParameter("orgId",org.getId())
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
 	public List<InstructorAttribute> getInstructorAttributes(int programId)
 	{
 		Program p = ProgramManager.instance().getProgramById(programId);
 		Organization org = OrganizationManager.instance().getOrganizationByProgram(p);
 		if(org.getParentOrganization() !=null)
 			org = org.getParentOrganization();
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<InstructorAttribute> toReturn =  null;
 		try
 		{
 		
 		
 			toReturn =  (List<InstructorAttribute>)session
 				 .createQuery("FROM InstructorAttribute WHERE organization.id = :orgId ORDER BY name")
 				 .setParameter("orgId",org.getId())
 				 .list();
 		
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}	
 	public boolean editCourseAttributeValue(int id, String value)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			CourseAttributeValue o = (CourseAttributeValue)session.get(CourseAttributeValue.class,id);
 			o.setValue(value);
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
 	public boolean saveCourseAttributeValue(int attributeTypeId, String value,int courseId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			Course course = (Course)session.get(Course.class, courseId);
 			CourseAttribute attribute = (CourseAttribute)session.get(CourseAttribute.class,attributeTypeId);
 			CourseAttributeValue o = new CourseAttributeValue();
 			o.setValue(value);
 			o.setAttribute(attribute);
 			o.setCourse(course);
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
 	
 	public CourseAttributeValue getCourseAttributeValueById(int id)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		CourseAttributeValue o = null;
 		try
 		{
 			o = (CourseAttributeValue)session.get(CourseAttributeValue.class,id);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return o;
 	}	
 	
 	public CourseAttribute getCourseAttributeById(int id)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		CourseAttribute o = null;
 		try
 		{
 			o = getCourseAttributeById(id,session);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return o;
 	}	
 	
 	private CourseAttribute getCourseAttributeById(int id, Session session)
 	{
 		return (CourseAttribute)session.get(CourseAttribute.class,id);
 	}
 	@SuppressWarnings("unchecked")
 	public List<CourseAttributeValue> getCourseAttributeValues(int courseId, int programId)
 	{
 		Program p = ProgramManager.instance().getProgramById(programId);
 		Organization org = OrganizationManager.instance().getOrganizationByProgram(p);
 		if(org.getParentOrganization() !=null)
 			org = org.getParentOrganization();
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<CourseAttributeValue> toReturn =  null;
 		try
 		{
 		
 		
 			toReturn =  (List<CourseAttributeValue>)session
 				 .createQuery("FROM CourseAttributeValue iav WHERE iav.course.id=:courseId AND iav.attribute.organization.id = :orgId ORDER BY iav.attribute.name, iav.value")
 				 .setParameter("courseId",courseId)
 				 .setParameter("orgId",org.getId())
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
 	public List<CourseAttribute> getCourseAttributes(int programId)
 	{
 		Program p = ProgramManager.instance().getProgramById(programId);
 		Organization org = OrganizationManager.instance().getOrganizationByProgram(p);
 		if(org.getParentOrganization() !=null)
 			org = org.getParentOrganization();
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<CourseAttribute> toReturn =  null;
 		try
 		{
 			toReturn =  (List<CourseAttribute>)session
 				 .createQuery("FROM CourseAttribute WHERE organization.id = :orgId ORDER BY name")
 				 .setParameter("orgId",org.getId())
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
 	public boolean removeCourseOfferingFromCourse(int offeringId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			CourseOffering offering = (CourseOffering)session.get(CourseOffering.class,offeringId);
 		
 			List<LinkCourseOfferingOutcome> linkOutcomes = (List<LinkCourseOfferingOutcome>)session.createQuery("FROM LinkCourseOfferingOutcome WHERE courseOffering.id=:offeringId").setParameter("offeringId", offeringId).list();
 			for(LinkCourseOfferingOutcome link:linkOutcomes )
 			{
 				List<LinkCourseOfferingOutcomeCharacteristic> chars  = (List<LinkCourseOfferingOutcomeCharacteristic>)session.createQuery("FROM LinkCourseOfferingOutcomeCharacteristic WHERE linkCourseOfferingOutcome.id =:linkId").setParameter("linkId", link.getId()).list();
 				for(LinkCourseOfferingOutcomeCharacteristic c : chars)
 				{
 					session.delete(c);
 				}
 				session.delete(link);
 			}
 			List<LinkCourseOfferingAssessment> assessments = (List<LinkCourseOfferingAssessment>)session.createQuery("FROM LinkCourseOfferingAssessment WHERE courseOffering.id = :offeringId").setParameter("offeringId", offeringId).list();
 			for(LinkCourseOfferingAssessment assessment: assessments)
 			{
 				List<LinkCourseAssessmentFeedbackOption> exitsingOptions = session.createQuery("FROM LinkCourseAssessmentFeedbackOption WHERE linkCourseOfferingAssessment.id = :linkId").setParameter("linkId",assessment.getId()).list();
 				for(LinkCourseAssessmentFeedbackOption toDelete : exitsingOptions)
 				{
 					session.delete(toDelete);
 				}
 				session.delete(assessment);
 			}
 			List<LinkCourseOfferingTeachingMethod> methods = (List<LinkCourseOfferingTeachingMethod>)session.createQuery("FROM LinkCourseOfferingTeachingMethod WHERE courseOffering.id = :offeringId").setParameter("offeringId", offeringId).list();
 			for(LinkCourseOfferingTeachingMethod method: methods)
 			{
 				session.delete(method);
 			}
 			List<LinkCourseOfferingInstructor> instructors = (List<LinkCourseOfferingInstructor>)session.createQuery("FROM LinkCourseOfferingInstructor WHERE courseOffering.id = :offeringId").setParameter("offeringId", offeringId).list();
 			for(LinkCourseOfferingInstructor instructor: instructors)
 			{
 				session.delete(instructor);
 			}
 			List<LinkAssessmentCourseOutcome> existingLinks = OutcomeManager.instance().getLinkAssessmentCourseOutcomes(offeringId, session);
 			for(LinkAssessmentCourseOutcome outcomeLink : existingLinks)
 			{	
 				session.delete(outcomeLink);
 			}
 			List<LinkCourseOfferingContributionProgramOutcome> contributionLinks = ProgramManager.instance().getCourseOfferingContributionLinks(offering, session);
 			for(LinkCourseOfferingContributionProgramOutcome link: contributionLinks)
 			{
 				session.delete(link);
 			}
 			List<LinkCourseOutcomeProgramOutcome> links = ProgramManager.instance().getCourseOutcomeLinks(offering, session);
 			for(LinkCourseOutcomeProgramOutcome link:links)
 			{
 				session.delete(link);
 			}
 			session.refresh(offering);
 			
 			session.delete(offering);
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
 
 
 	public List<Department> getDepartmentForCourseOffering(CourseOffering offering)
 	{
 		Course c = offering.getCourse();
 		return getDepartmentForCourse(c);
 	}
 	
 	public List<Department> getDepartmentForCourse(Course course)
 	{
 		List<Department> toReturn = null;
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 
 			toReturn = getDepartmentForCourse(course,session);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<LinkCourseDepartment> getDepartmentLinksForCourse(Course course)
 	{
 		List<LinkCourseDepartment> toReturn = null;
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			
 			toReturn = session.createQuery("FROM LinkCourseDepartment WHERE course.id = :courseId ORDER BY lower(department.name)").setParameter("courseId", course.getId()).list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Department> getDepartmentForCourse(Course course,Session session)
 	{
 		List<Department> toReturn = null;
 		StringBuilder sql = new StringBuilder();
 		sql.append(" select {d.*} ");
 		sql.append("   from department d ");
 		sql.append("		 ,link_course_department lcd");
 		sql.append(" where lcd.department_id = d.id");
 		sql.append("   and lcd.course_id = :courseId");
 		toReturn = (List<Department>)session.createSQLQuery(sql.toString())
 				.addEntity("d",Department.class)
 				.setParameter("courseId",course.getId())
 				.list();
 		return toReturn;
 	}
 	public int getOfferingCountForTerm(String term)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		int toReturn = 0;
 		try
 		{
 
 			toReturn = ((BigInteger)session.createSQLQuery("select count(*) from course_offering where term=:term").setParameter("term",term).uniqueResult()).intValue();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 
 	
 	public Boolean[] completedRecord(CourseOffering c, Session session)
 	{
 		Boolean[] completed = new Boolean[6];
 		completed[0] = !session.createQuery("FROM LinkCourseOfferingTeachingMethod WHERE courseOffering.id = :coId")
 					.setParameter("coId",c.getId())
 					.list().isEmpty();
 		completed[1] = 	!getAssessmentsForCourseOffering(c,session).isEmpty();
 		completed[2] = !OutcomeManager.instance().getOutcomesForCourseOffering(c,session).isEmpty();
 		completed[3] = !OutcomeManager.instance().getLinkAssessmentCourseOutcomes(c.getId(),session).isEmpty();
 		completed[4] = !ProgramManager.instance().getCourseOfferingContributionLinks(c, session).isEmpty();
 		completed[5] = ! ProgramManager.instance().getCourseOutcomeLinks(c,session).isEmpty();
 		return completed;
 	}
 
 
 	
 	public CourseManager()
 	{
 
 	}
 
 	public static CourseManager instance()
 	{
 		if (instance == null)
 		{
 			instance = new CourseManager();
 		}
 		return instance;
 
 	}
 
 }
