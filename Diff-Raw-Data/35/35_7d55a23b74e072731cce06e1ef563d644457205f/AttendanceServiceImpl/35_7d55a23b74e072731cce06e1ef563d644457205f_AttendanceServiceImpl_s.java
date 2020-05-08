 package com.ritchey.attendance.server;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.ibatis.session.RowBounds;
 import org.joda.time.LocalDate;
 import org.joda.time.LocalTime;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.BeanFactoryAware;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.AuthenticationException;
 import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
 
 import com.browsexml.ldap.LdapUserDetails;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import com.ritchey.attendance.client.AttendanceService;
 import com.ritchey.attendance.domain.powercampus.Attendance;
 import com.ritchey.attendance.domain.powercampus.AttendanceMapper;
 import com.ritchey.attendance.domain.powercampus.AttendanceSummary;
 import com.ritchey.attendance.domain.powercampus.CodeAttendanceStatusDetail;
 import com.ritchey.attendance.domain.powercampus.CodeAttendanceStatusDetailMapper;
 import com.ritchey.attendance.domain.powercampus.Course;
 import com.ritchey.attendance.domain.powercampus.CoursesMapper;
 import com.ritchey.attendance.domain.powercampus.GradeValues;
 import com.ritchey.attendance.domain.powercampus.GradeValuesMapper;
 import com.ritchey.attendance.domain.powercampus.ListCount;
 import com.ritchey.attendance.domain.powercampus.People;
 import com.ritchey.attendance.domain.powercampus.PeopleMapper;
 import com.ritchey.attendance.domain.powercampus.SimpleLoginType;
 import com.ritchey.attendance.domain.progress.CourseMapper;
 import com.ritchey.attendance.domain.progress.Person;
 import com.ritchey.attendance.domain.progress.PersonDao;
 import com.ritchey.attendance.domain.progress.PersonMapper;
 import com.ritchey.attendance.domain.progress.Progress;
 import com.ritchey.attendance.domain.progress.ProgressMapper;
 import com.ritchey.attendance.domain.progress.Session;
 import com.ritchey.attendance.domain.progress.SessionMapper;
 
 /**
  * The server side implementation of the RPC service.
  */
 @Configurable
 @SuppressWarnings("serial")
 public class AttendanceServiceImpl extends RemoteServiceServlet implements
 		AttendanceService, BeanFactoryAware {
 	private static Log log = LogFactory.getLog(AttendanceServiceImpl.class);
 
 	CoursesMapper coursesMapper;
 	PeopleMapper peopleMapper;
 	GradeValuesMapper gradeValuesMapper;
 	AttendanceMapper attendanceMapper;
 	CodeAttendanceStatusDetailMapper codeAttendanceMapper;
 	static Properties properties = new Properties();
 	static {
 		try {
 			properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("com/ritchey/attendance/client/view/AppConstants.properties"));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public String getContext() {
 		log.debug("in server; properties = " + properties);
 		String context  = properties.get("version") + "~" + ((this.getThreadLocalRequest()==null)?"null":this.getThreadLocalRequest().getRequestURL());
 		log.debug("contxt returning = " + context);
 		return context;// this.getThreadLocalRequest().getQueryString();
 	}
 	
 	public PeopleMapper getPeopleMapper() {
 		return peopleMapper;
 	}
 
 	LdapAuthenticationProvider secondLdapProvider = null;
 
 	private CourseMapper courseMapper;
 
 	private SessionMapper sessionMapper;
 
 	private PersonMapper personMapper;
 
 	private ProgressMapper progressMapper;
 
 	@Autowired
 	public void setSecondLdapProvider(LdapAuthenticationProvider secondLdapProvider) {
 		//new Exception("secondLdapProvider").printStackTrace();
 		this.secondLdapProvider = secondLdapProvider;
 	}
 	
 	@Autowired
 	public void setSessionMapper(SessionMapper sessionMapper) {
 		new Exception("SET session MAPPER").printStackTrace();
 		this.sessionMapper = sessionMapper;
 	}
 	
 	@Autowired
 	public void setPersonMapper(PersonMapper personMapper) {
 		new Exception("SET person MAPPER").printStackTrace();
 		this.personMapper = personMapper;
 	}
 	
 	@Autowired
 	public void setProgressMapper(ProgressMapper progressMapper) {
 		new Exception("SET Progress MAPPER").printStackTrace();
 		this.progressMapper = progressMapper;
 	}
 	
 	@Autowired
 	public void setCourseMapper(CourseMapper courseMapper) {
 		new Exception("SET course MAPPER").printStackTrace();
 		this.courseMapper = courseMapper;
 	}
 	
 	@Autowired
 	public void setPeopleMapper(PeopleMapper peopleMapper) {
 		new Exception("SET PEOPLE MAPPER").printStackTrace();
 		this.peopleMapper = peopleMapper;
 	}
 	
 	@Autowired
 	public void setGradeValuesMapper(GradeValuesMapper gradeValuesMapper) {
 		new Exception("SET gradeValuesMapper").printStackTrace();
 		this.gradeValuesMapper = gradeValuesMapper;
 	}
 
 	public AttendanceMapper getAttendanceMapper() {
 		return attendanceMapper;
 	}
 
 	@Autowired
 	public void setPersonDao(PersonDao personDao) {
 		this.personDao = personDao;
 	}
 	
 	@Autowired
 	public void setAttendanceMapper(AttendanceMapper attendanceMapper) {
 		this.attendanceMapper = attendanceMapper;
 	}
 
 	public CodeAttendanceStatusDetailMapper getCodeAttendanceMapper() {
 		return codeAttendanceMapper;
 	}
 	
 	Map<String, List<CodeAttendanceStatusDetail>> codeMap;
 	HashMap<String, String> simpleCodeMap;
 	
 
 	public HashMap<String, String> getSimpleCodeMap() {
 		return simpleCodeMap;
 	}
 
 	public void setSimpleCodeMap(HashMap<String, String> simpleCodeMap) {
 		this.simpleCodeMap = simpleCodeMap;
 	}
 
 	@Autowired
 	public void setCodeAttendanceMapper(
 			CodeAttendanceStatusDetailMapper codeAttendanceMapper) {
 		this.codeAttendanceMapper = codeAttendanceMapper;
 		// Use this area to build hashmap of lists for Present, Absent and Tardy
 		Map<String, CodeAttendanceStatusDetail> x = codeAttendanceMapper.getCodes();
 		// 1, 2  Absent
 		// 5     Present
 		// 3, 4  Tardy
 		
 		simpleCodeMap = new HashMap<String, String>();
 		codeMap = new HashMap<String, List<CodeAttendanceStatusDetail>>();
 		//log.debug("codeMap = " + codeMap);
 
 		String[] strAbsent = new String[] {properties.getProperty("absent"), properties.getProperty("present"), properties.getProperty("tardy")};
 		
 		for (String items: strAbsent) {
 			String[] list = items.split(", *");
 			List<CodeAttendanceStatusDetail> lstList = new ArrayList<CodeAttendanceStatusDetail>();
 			for (String item: list) {
 				CodeAttendanceStatusDetail codeAttendanceStatusDetail = x.get(item);
 				lstList.add(codeAttendanceStatusDetail);
 				simpleCodeMap.put(item, codeAttendanceStatusDetail.getDescription());
 			}
 			//log.debug ("LIST  " + list[0] + " = " + lstList);
 			codeMap.put(list[0], lstList);
 		}
 	}
 	
 	public CodeAttendanceStatusDetail getNextAttendanceDescription(String group, String description) {
 		if (group == null) 
 			return null;
 		List<CodeAttendanceStatusDetail> list = codeMap.get(group);
 		if (list == null || list.get(0) == null)
 			return null;
 		
 		if (description == null || "".equals(description)) {
 			return list.get(0);
 		}
 		
 		boolean next = false;
 		for (CodeAttendanceStatusDetail d: list) {
 			if (next) {
 				return d;
 			}
 			if (d.getDescription().equals(description)) {
 				next = true;
 			}
 		}
 		return list.get(0);
 	}
 	
 	public Map<String, List<CodeAttendanceStatusDetail>> getCodeMap() {
 		return codeMap;
 	}
 
 	public static String formatTime(Date date) {
 		SimpleDateFormat time = new SimpleDateFormat("h:mm a");
 		return time.format(date);
 	}
 
 	public CoursesMapper getCoursesMapper() {
 		return coursesMapper;
 	}
 
 	@Autowired
 	public void setCoursesMapper(CoursesMapper coursesMapper) {
 		this.coursesMapper = coursesMapper;
 	}
 
 	public ListCount<Course> getClassList(String peopleCodeId) {
 		try {
 			List<Course> i = coursesMapper.getSectionPeople(peopleCodeId);
 			return new ListCount<Course>(i, i.size());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	Course course;
 	Map<String, AttendanceSummary> summary;
 
 	private PersonDao personDao;
 	
 	public List<GradeValues> getGrades(Course course) {
 		return gradeValuesMapper.getGradeValues(course);
 	}
 	
 	public void getProgressCourse(Course course) {
 		
     	Map m = courseMapper.getProgressSessionAndCourseByCourse(course);
     	Integer sessionId = (Integer) m.get("sessionId");
     	Integer courseId = (Integer) m.get("courseId");
 
 		if (sessionId == null) {
 			Session session = new Session();
 			session.setYear(course.getYear());
 			session.setTerm(course.getTerm());
 			session.setSession(course.getSession());
 			sessionMapper.insert(session);
 			sessionId = session.getId();
 		}
 
 		if (courseId == null) {
 			com.ritchey.attendance.domain.progress.Course prgCourse = new com.ritchey.attendance.domain.progress.Course();
 			prgCourse.setEventId(course.getEvent());
 			prgCourse.setEventSubType(course.getSubtype());
 			prgCourse.setSection(course.getSection());
 			prgCourse.setSession(course.getProgressSessionId());
 
 			courseMapper.insert(prgCourse);
 			courseId = course.getId();
 		}
 		
 		course.setProgressCourseId(courseId);
 	}
 	
 	@Override
 	public ListCount<People> getPeopleForCourseList(Course course, int start, int length, 
 			boolean recount, boolean unsetOnly, boolean athleteOnly, Integer sessionId, Integer courseId) {
 		try {
 			log.debug("attendance: getPeopleForCourseList... Start = " + start + "  course = " + course);
 			if (this.course == null || !course.equals(this.course)) {
 				summary = getAttendanceSummary(course);
 				this.course = course;
 			}
 			// List<People> ret = peopleMapper.getSectionPeopleByCourse(course);
 			course.setDate(new Date());
 			//log.debug("get Section People Attendance by course");
 			int count = 0;
 			if (start < 0) 
 				start = 0;
 			if (recount) {
 				//log.debug("unsetOnly = " + unsetOnly);
 				count = peopleMapper.getSectionPeopleAttendanceByCourseCount(course, unsetOnly, athleteOnly);
 				//log.debug("COUNT = " + count);
 				if (start >= count) {
 					start = 0;
 				}
 			}
 			log.debug("getPeopleForCourseList2... Start = " + start);
 			List<People> ret = peopleMapper
 					.getSectionPeopleAttendanceByCourse(course, unsetOnly, athleteOnly, new RowBounds(start, length));
 			
			Map<String, Object> createProgressMap = createPersonsInProgressDb(course, ret, sessionId, courseId);
 			Map<String, Progress> progressList = (Map<String, Progress>) createProgressMap.get("progressList");
 			
 			for (People p: ret) {
 				AttendanceSummary x = summary.get(p.getId());
 				if (x == null) 
 					x = new AttendanceSummary();
 				p.setSummary(x);
 				Progress progress = progressList.get(p.getId());
 				assert(progress != null);
 				p.setProgress(progress);
 				if (p.getAttendance() != null) {
 					log.debug("comment server: " + p.getLastName() + "  " + p.getAttendance().getTranattendanceid() + "  " + p.getAttendance().getComments());
 				}
 			}
 			//int count = peopleMapper.getSectionPeopleAttendanceByCourseCount(course);
 			createProgressMap.remove("progressList");
 			
 			
 			log.debug("get Section People Attendance by course  count = " + count + "   ret = " + ret + "   DONE");
 			return new ListCount<People>(ret, count, createProgressMap);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 
 	/**
 	 * Escape an html string. Escaping data received from the client helps to
 	 * prevent cross-site script vulnerabilities.
 	 * 
 	 * @param html
 	 *            the html string to escape
 	 * @return the escaped string
 	 */
 	private void escapeHtml(String... html) {
 		if (html == null) {
 			return;
 		}
 		for (int i = 0; i < html.length; i++) {
 			html[i] = html[i].replaceAll("&", "&amp;").replaceAll("<", "&lt;")
 					.replaceAll(">", "&gt;");
 		}
 	}
 
 	@Override
 	public SimpleLoginType authenticate(String user, String password) {
 		log.debug("Authenticate " + user);
 
 		Authentication y = new UsernamePasswordAuthenticationToken(user,
 				password);
 		if (secondLdapProvider != null) {
 			log.debug("AUTHENTICATING...");
 			log.debug("AUTHENTICATING...");
 			log.debug("AUTHENTICATING...");
 			try {
 				y = secondLdapProvider.authenticate(y);
 			} catch (AuthenticationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return null;
 			}
 			log.debug("result is " + y.isAuthenticated());
 		}
 		else {
 			log.debug("ldap is null");
 		}
 		LdapUserDetails ldap = ((LdapUserDetails) y.getPrincipal());
 		SimpleLoginType s = new SimpleLoginType();
 		s.setAuthenticated(y.isAuthenticated());
 		ldap.copy(s);
 		if (s.getAuthenticated()) {
 			s.setAuthenticated(s.getRoles().contains("ROLE_TECHNOLOGY")
 							|| s.getRoles().contains("ROLE_FACULTY"));
 			log.debug("peopleId = " + s.getPeopleId());
 			log.debug("email = " + s.getEmail());
 			log.debug("Name = " + s.getName());
 		}
 		return s;
 	}
 
 	@Override
 	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
 		log.debug("SET BEAN FACTORY");
 		secondLdapProvider = (LdapAuthenticationProvider) beanFactory
 				.getBean("secondLdapProvider");
 
 	}
 
 	@Override
 	public String updateDatabase(People people, String buttonCode) {
 		Attendance att = people.getAttendance();
 		log.debug("people id = " + people.getCodeId() + "  att: " + people.getAttendance().getPeopleCodeId());
 		log.debug("comment = " + people.getAttendance().getComments());
 		boolean insert = att.getCreateDate() == null;
 		CodeAttendanceStatusDetail status = getNextAttendanceDescription(buttonCode, att.getAttendanceStatusDescription());
 		if (status != null) {
 			att.setAttendanceStatus(status.getKey());
 		}
         att.setAttendanceDate(new LocalDate().toDate());
         
         att.setRevisionDate(new LocalDate().toDate());
         att.setRevisionTime(new Date(new LocalTime().getMillisOfDay()));
 
         int count = 0;
  
 			
 			if (insert) {
 				// this probably doesn't update the Attendance Object at the client side
 				// therefore, check count after insert and do an update if necessary
 
 				att.setCreateDate(new LocalDate().toDate());
 				att.setCreateTime(new Date(new LocalTime().getMillisOfDay()));
 
 				try {
 					count = attendanceMapper.insertSelective(att);
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			if (count == 0) {
 				count = attendanceMapper.updateByPrimaryKeySelective(att);
 			}
 			
 			if (count == 0) {
 				return null;
 			}
 
 			log.debug("UPDATE COUNT = " + count);
 		
 			return (status == null)?"":status.getDescription();
 		
 	}
 	
 	@Override
 	public Map<String, AttendanceSummary> getAttendanceSummary(Course course) {
 		log.debug("att hist course = " + course);
 		return peopleMapper.getAttendanceHistory(course);
 	}
 	
 	@Override
 	public int insertPresent(Course course) {
 		return peopleMapper.insertPresent(course);
 	}
 	
 	@Override
 	public int updateProgress(Progress progress) {
 		if (progress.getId() != null) {
 			int x = progressMapper.updateByPrimaryKeySelective(progress);
 			assert(x != 0);
 		}
 		else {
 			int x = progressMapper.insert(progress);
 			assert(x != 0);
 		}
 		return progress.getId();
 	}
 	
 	/**
 	 * Query for the progress (grade and attendance) of a list of people in a course
 	 * Create the course and session if it doesn't exist; their Ids are mapped as sessionId and courseId
 	 * in the returned Map. progressList is the key for the actual list of students.
 	 */
 	public Map<String, Object> createPersonsInProgressDb(Course course, List<People> attendanceByCourse, Integer sessionId, Integer courseId) {
 		Map<String, Object> ret = new HashMap<String, Object>();
 		
 		if (sessionId == null || courseId == null) {
 			log.debug("createPersons In Progress Db  course = " + course);
 			Map<String, Integer> session = courseMapper.getProgressSessionAndCourseByCourse(course);
 			log.debug("course mapper done");
 			
 			if (session != null) {
 				sessionId = session.get("sessionId");
 				courseId = session.get("courseId");
 			}
 			
 			//sessionId and courseId
 			if (sessionId == null) {
 				Session s = new Session();
 				s.setYear(course.getYear());
 				s.setTerm(course.getTerm());
 				s.setSession(course.getSession());
 				sessionMapper.insert(s);
 				sessionId = s.getId();
 			}
 			if (courseId == null) {
 				com.ritchey.attendance.domain.progress.Course c = new com.ritchey.attendance.domain.progress.Course();
 				c.setSession(sessionId);
 				c.setEventId(course.getEvent());
 				c.setEventSubType(course.getSubtype());
 				c.setSection(course.getSection());
 		
 				courseMapper.insert(c);
 				courseId = c.getId();
 			}
 		}
 		
 		ret.put("sessionId", sessionId);
 		ret.put("courseId", courseId);
 		
 		List<String> peopleIds = new ArrayList();
 		
 		for (People p: attendanceByCourse) {
 			peopleIds.add(p.getId());
 		}
 		Map<String, Progress> progressList = null;
 		if (peopleIds.size() > 0) {
 			progressList = personMapper.getProgressDetailsByCourseAndPeopleId(courseId, peopleIds);
 		}
 		log.debug("People Create ret = " + attendanceByCourse);
 		
 		log.debug("Progress list = " + progressList);
 		
 		List<Person> batchInsert = new ArrayList<Person>();
 		for (People p: attendanceByCourse) {
 			if (progressList.get(p.getId()) == null) {
 				Person person = new Person(p);
 				batchInsert.add(person);
 				ret.put(person.getPeopleId(), person);
 			}
 		}
 		
 		if (batchInsert.size() > 0) {
 			personDao.batchInsert(batchInsert);
 			progressList = personMapper.getProgressDetailsByCourseAndPeopleId(courseId, peopleIds);
 		}
 
 		ret.put("progressList", progressList);
 		return ret;
 	}
 	
 	
 }
