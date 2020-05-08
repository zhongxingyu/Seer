 package com.ritchey.chapelManage.server;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.security.Security;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.activation.DataHandler;
 import javax.activation.DataSource;
 import javax.mail.Address;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.PasswordAuthentication;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 import javax.mail.util.ByteArrayDataSource;
 import javax.servlet.http.HttpServletRequest;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.apache.commons.logging.LogFactory;
 import org.apache.ibatis.session.RowBounds;
 import org.joda.time.DateMidnight;
 import org.joda.time.LocalDate;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.AuthenticationException;
 import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
 import org.springframework.transaction.annotation.Transactional;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 import com.browsexml.core.XMLBuildException;
 import com.browsexml.core.XmlObject;
 import com.browsexml.core.XmlParser;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import com.ritchey.chapelManage.client.ChapelPunchService;
 import com.ritchey.chapelManage.domain.chapel.ChapelPerson;
 import com.ritchey.chapelManage.domain.chapel.ChapelPersonExample;
 import com.ritchey.chapelManage.domain.chapel.ChapelPersonExample.Criteria;
 import com.ritchey.chapelManage.domain.chapel.Event;
 import com.ritchey.chapelManage.domain.chapel.EventExample;
 import com.ritchey.chapelManage.domain.chapel.PropertiesExample;
 import com.ritchey.chapelManage.domain.chapel.Punch;
 import com.ritchey.chapelManage.domain.chapel.ScheduledEvent;
 import com.ritchey.chapelManage.domain.chapel.ScheduledEventExample;
 import com.ritchey.chapelManage.domain.chapel.Status;
 import com.ritchey.chapelManage.domain.chapel.StatusExample;
 import com.ritchey.chapelManage.domain.chapel.SimpleLoginType;
 import com.ritchey.chapelManage.domain.powercampus.People;
 import com.ritchey.chapelManage.domain.powercampus.PeopleExample;
 import com.ritchey.chapelManage.mapper.chapel.ChapelPersonMapper;
 import com.ritchey.chapelManage.mapper.chapel.EventMapper;
 import com.ritchey.chapelManage.mapper.chapel.PropertiesMapper;
 import com.ritchey.chapelManage.mapper.chapel.PunchMapper;
 import com.ritchey.chapelManage.mapper.chapel.ScheduledEventMapper;
 import com.ritchey.chapelManage.mapper.chapel.StatusMapper;
 import com.ritchey.chapelManage.mapper.powercampus.AcademicCalendarMapper;
 import com.ritchey.chapelManage.mapper.powercampus.PeopleMapper;
 import com.ritchey.chapelManage.shared.ListCount;
 import com.ritchey.chapelManage.shared.Log;
 import com.ritchey.ldap.LdapUserDetails;
 
 import edu.bxml.format.Sql;
 
 /**
  * The server side implementation of the RPC service.
  */
 @Configurable
 @SuppressWarnings("serial")
 public class ChapelPunchServiceImpl extends RemoteServiceServlet implements
 		ChapelPunchService, ApplicationContextAware {
 	static org.apache.commons.logging.Log log = LogFactory
 			.getLog(ChapelPunchServiceImpl.class);
 
 	@Autowired
 	LdapAuthenticationProvider ldap = null;
 
 	static Properties properties = new Properties();
 	static {
 		try {
 			properties
 					.load(Thread
 							.currentThread()
 							.getContextClassLoader()
 							.getResourceAsStream(
 									"com/ritchey/chapelManage/client/AppConstants.properties"));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	private PeopleMapper peopleMapper;
 	private AcademicCalendarMapper academicCalendarMapper;
 	private StatusMapper statusMapper;
 	private EventMapper eventMapper;
 	private PunchMapper punchMapper;
 	private PropertiesMapper propertiesMapper;
 	private ChapelPersonMapper chapelPersonMapper;
 	private ScheduledEventMapper scheduledEventMapper;
 
 	public ScheduledEventMapper getScheduledEventMapper() {
 		return scheduledEventMapper;
 	}
 
 	@Autowired
 	public void setScheduledEventMapper(
 			ScheduledEventMapper scheduledEventMapper) {
 		this.scheduledEventMapper = scheduledEventMapper;
 	}
 
 	public StatusMapper getStatusMapper() {
 		return statusMapper;
 	}
 
 	@Autowired
 	public void setStatusMapper(StatusMapper statusMapper) {
 		this.statusMapper = statusMapper;
 	}
 
 	public ChapelPersonMapper getChapelPersonMapper() {
 		return chapelPersonMapper;
 	}
 
 	@Autowired
 	public void setChapelPersonMapper(ChapelPersonMapper chapelPersonMapper) {
 		this.chapelPersonMapper = chapelPersonMapper;
 	}
 
 	public PeopleMapper getPeopleMapper() {
 		return peopleMapper;
 	}
 
 	@Autowired
 	public void setPeopleMapper(PeopleMapper peopleMapper) {
 		this.peopleMapper = peopleMapper;
 	}
 
 	public PropertiesMapper getPropertiesMapper() {
 		return propertiesMapper;
 	}
 
 	@Autowired
 	public void setPropertiesMapper(PropertiesMapper propertiesMapper) {
 		this.propertiesMapper = propertiesMapper;
 	}
 
 	public EventMapper getEventMapper() {
 		return eventMapper;
 	}
 
 	@Autowired
 	public void setEventMapper(EventMapper eventMapper) {
 		this.eventMapper = eventMapper;
 	}
 
 	public PunchMapper getPunchMapper() {
 		return punchMapper;
 	}
 
 	@Autowired
 	public void setPunchMapper(PunchMapper punchMapper) {
 		this.punchMapper = punchMapper;
 	}
 
 	@Autowired
 	public void setAcademicCalendarMapper(AcademicCalendarMapper mapper) {
 		this.academicCalendarMapper = mapper;
 	}
 
 	public List<String> getYearTerms() {
 		try {
 			RowBounds r = new RowBounds(0, 10);
 			return academicCalendarMapper.selectYearTerm(r);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@Override
 	public Map<String, Object> getStartTerm() {
 		Map<String, Object> ret = new HashMap<String, Object>();
 		new Exception("Get start term").printStackTrace();
 		ret = academicCalendarMapper.selectCurrentStartDate();
 		if (ret != null) {
 			ret.put("currentStudentCount", getCurrentStudentCount());
 		}
 		log.debug("start term env = " + ret);
 		return ret;
 	}
 
 	@Override
 	public SimpleLoginType authenticate(String user, String password, Date date) {
 		System.err.println("Authenticate " + user + "  password: " + password);
 
 		Authentication y = new UsernamePasswordAuthenticationToken(user,
 				password);
 		if (ldap != null) {
 			System.err.println("AUTHENTICATING...");
 			try {
 				y = ldap.authenticate(y);
 			} catch (AuthenticationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return null;
 			}
 			System.err.println("result is " + y.isAuthenticated());
 		}
 		else {
 			log.debug("ldap not auto-injected");
 			return null;
 		}
 		LdapUserDetails ldap = ((LdapUserDetails) y.getPrincipal());
 		SimpleLoginType s = new SimpleLoginType();
 		s.setAuthenticated(y.isAuthenticated());
 		ldap.copy(s);
 
 		s.setAuthenticated(s.getAuthenticated()
 				&& (s.getRoles().contains("ROLE_STUDENT AFFAIRS") || s
 						.getRoles().contains("ROLE_TECHNOLOGY")));
 		System.err.println("peopleId = " + s.getPeopleId());
 		System.err.println("email = " + s.getEmail());
 		System.err.println("Name = " + s.getName());
 		s.setAccount(getPeople(s.getPeopleId()));
 
 		return s;
 	}
 
 	@Override
 	public ListCount<Map> selectPunchedEvents(int offset, int limit, Date date,
 			Integer id, Boolean scheduleOnly, Boolean punchesOnly) {
 		log.debug("select punched Events date = " + date + "  id = " + id
 				+ " scheduleOnly = " + scheduleOnly);
 		Log.debug("ScheduleOnly = " + scheduleOnly);
 		if (scheduleOnly == null)
 			scheduleOnly = false;
 		scheduleOnly = scheduleOnly ? null : scheduleOnly;
 		
 		if (punchesOnly == null)
 			punchesOnly = false;
 		punchesOnly = punchesOnly ? true : null;
 		
 		Log.debug("Same call ScheduleOnly = " + scheduleOnly);
 		Map<String, Object> termBounds = getStartTerm();
 		Date startday = (Date) termBounds.get("startTerm");
 		RowBounds r = new RowBounds(offset, limit);
 		log.debug("select punched Events2  startday = " + startday + " date = "
 				+ date + "  id = " + id + " scheduleOnly = " + scheduleOnly);
 		List<Map> list = punchMapper.selectPunchedEvents(startday, date, id,
 				scheduleOnly, punchesOnly, r);
 		Integer count = punchMapper.selectCountPunchedEvents(startday, date,
 				id, scheduleOnly, punchesOnly);
 		ListCount ret = new ListCount(list, count);
 		return ret;
 	}
 
 	/**
 	 * * List of people in database
 	 * 
 	 * @param page
 	 * @param size
 	 * @param filter
 	 *            restricts list by lastname, firstname or campusId
 	 * @param onlyCurrentStudents
 	 *            true means only show people who are current students, false
 	 *            shows former students in the chapel database and null shows
 	 *            everyone in the database (depending on onlyExempt setting)
 	 * @param onlyExempt
 	 *            true means only show people who are exempt from chapel, false
 	 *            shows students not exempt, and null shows everyone (depending
 	 *            on onlyCurrentStudent setting)
 	 * @param onlyCurrentStudents
 	 * @param onlyExempt
 	 * @return
 	 */
 	@Override
 	public ListCount<Map> listChapelPerson(int page, int size, String filter,
 			Boolean onlyCurrentStudents, Integer exemptDaysPerWeek) {
 		ChapelPersonExample person = new ChapelPersonExample();
 		Criteria c = person.createCriteria();
 		if (filter != null) {
 			if (filter.contains("EMAIL")) {
 				filter.replace("EMAIL", "");
 				c.andEmailBad();
 			} 
 			else {
 				if (filter.matches("P?[0-9]{9}")) {
 					if (filter.startsWith("P"))
 						filter = filter.substring(1);
 					c.andPeopleIdEqualTo(filter);
 				} else {
 					String[] filters = filter.split(" *, *");
 					c.andLastnameLike(filters[0] + "%");
 					if (filters.length > 1) {
 						c.andFirstnameLike(filters[1] + "%");
 					}
 				}
 			}
 		}
 		if (onlyCurrentStudents != null) {
 			c.andCurrentstudentEqualTo(onlyCurrentStudents);
 		}
 		if (exemptDaysPerWeek != null) {
 			c.andExemptEqualTo(exemptDaysPerWeek);
 		}
 
 		int count = chapelPersonMapper.countByExample(person);
 		List<ChapelPerson> list = chapelPersonMapper.selectByExample(person,
 				new RowBounds(page, size));
 		List<Map> retList = new ArrayList<Map>();
 		for (ChapelPerson cp : list) {
 			Map m = new HashMap();
 			m.put("firstName", cp.getFirstname());
 			m.put("lastName", cp.getLastname());
 			m.put("middleName", cp.getMiddlename());
 			m.put("campusId", cp.getPeopleId());
 			m.put("currentstudent", cp.getCurrentstudent());
 			m.put("exempt", cp.getExempt());
 			m.put("comment", cp.getComment());
 			m.put("email", cp.getEmail());
 			retList.add(m);
 		}
 		return new ListCount<Map>(retList, count);
 	}
 
 	@Override
 	public People storePunch(String campusId, String loginId) throws Exception {
 		if (0 == punchMapper.punch(campusId, loginId)) {
 			throw new Exception("The current event has not been set up");
 		}
 
 		PeopleExample example = new PeopleExample();
 		example.createCriteria().andPeopleIdEqualTo(campusId);
 		List<People> people = peopleMapper.selectByExample(example);
 		Log.debug("size = " + people.size());
 		if (people.size() != 1) {
 			return null;
 		}
 		return people.get(0);
 	}
 
 	@Override
 	public Integer updateExempt(String campusId, Integer exemptions)
 			throws Exception {
 		ChapelPersonExample example = new ChapelPersonExample();
 		example.createCriteria().andPeopleIdEqualTo(campusId);
 		ChapelPerson person = new ChapelPerson();
 		person.setExempt(exemptions);
 		int count = chapelPersonMapper
 				.updateByExampleSelective(person, example);
 		return count;
 	}
 	
 	@Override
 	public Integer updateEmail(String campusId, String email)
 			throws Exception {
 		ChapelPersonExample example = new ChapelPersonExample();
 		example.createCriteria().andPeopleIdEqualTo(campusId);
 		ChapelPerson person = new ChapelPerson();
 		person.setEmail(email);
 		int count = chapelPersonMapper
 				.updateByExampleSelective(person, example);
 		return count;
 	}
 
 	@Override
 	public void setApplicationContext(ApplicationContext applicationContext)
 			throws BeansException {
 		for (String name : applicationContext.getBeanDefinitionNames()) {
 			log.debug("Bean: " + name + "   type = "
 					+ applicationContext.getBean(name).getClass().getName());
 		}
 	}
 
 	@Override
 	public ListCount<Event> getEventList(Boolean showActiveOnly, Integer include) {
 		EventExample example = new EventExample();
 		if (showActiveOnly) {
 			example.createCriteria().andActiveEqualTo(true);
 			if (include != null) {
 				example.or(example.createCriteria().andIdEqualTo(include));
 			}
 		}
 		List<Event> list = eventMapper.selectByExample(example);
 		ListCount<Event> ret = new ListCount<Event>(list, list.size());
 		return ret;
 	}
 
 	@Override
 	public void updateEvent(Event event) throws Exception {
 		try {
 			log.debug("UPDATE event = " + event);
 			eventMapper.updateByPrimaryKey(event);
 		} catch (Exception e) {
 			throw new Exception(e.getMessage());
 		}
 	}
 
 	@Override
 	public Integer createEvent(Event event) throws Exception {
 		try {
 			eventMapper.insert(event);
 		} catch (Exception e) {
 			throw new Exception(e.getMessage()); // Translate unchecked
 													// exception to checked
 		}
 		return event.getId();
 	}
 
 	@Override
 	public Event getEvent(Integer id) {
 		return eventMapper.selectByPrimaryKey(id);
 	}
 
 	public void createNewProperty(Integer id) {
 		com.ritchey.chapelManage.domain.chapel.Properties p = new com.ritchey.chapelManage.domain.chapel.Properties();
 		p.setStrvalue("" + id);
 		p.setKeyvalue("CUR_EVENT");
 		propertiesMapper.insert(p);
 	}
 
 	@Override
 	public void setActive(Integer eventId, Boolean value) throws Exception {
 		Event event = eventMapper.selectByPrimaryKey(eventId);
 		event.setActive(value);
 		try {
 			eventMapper.updateByPrimaryKey(event);
 		} catch (Exception e) {
 			new Exception(e.getMessage());
 		}
 	}
 
 	@Override
 	public Integer updateComment(String campusId, String value) {
 		Log.debug("server value = '" + value + "'");
 		ChapelPersonExample example = new ChapelPersonExample();
 		example.createCriteria().andPeopleIdEqualTo(campusId);
 		ChapelPerson person = new ChapelPerson();
 		person.setComment(value);
 		int count = chapelPersonMapper
 				.updateByExampleSelective(person, example);
 		return count;
 	}
 
 	@Override
 	public String getContext() {
 		log.debug("in server; properties = " + properties);
 		HttpServletRequest local = this.getThreadLocalRequest();
 		StringBuffer request = new StringBuffer("");
 		if (local != null)
 			local.getRequestURL();
 		String context = properties.get("version") + "~" + request;
 		log.debug("contxt returning = " + context);
 		return context;// this.getThreadLocalRequest().getQueryString();
 	}
 
 	@Override
 	public Map<String, Object> getEmailEnv() {
 		Date startterm = new Date(0);
 		List<Map> list = chapelPersonMapper.selectChapelStatus(startterm, new RowBounds(0,1));
 		if (list.size() > 0) {
 			return list.get(0);
 		}
 		return null;
 	}
 	
 	final private static SimpleDateFormat fmt = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
 	@Override
 	public String recordTime(String propertyName, String msg) {
 		Date time = new Date();
 		String value = fmt.format(time) + ((msg==null)?"": ": " + msg);
 		saveProperty(propertyName, value);
 		return value;
 	}
 	
 	@Override
 	public String sendEmail(Date startterm, String responseEmail, String emailHost, String emailPort) throws Exception{
 			log.debug("send email  startterm = " + startterm);
			String emailText = getEmail();
			
 			try {
 				//recordTime("EMAILSENDS", null);
 				List<Map> list = chapelPersonMapper.selectChapelStatus(startterm);
 				log.debug("count = " + list.size());
				
 				int count = 0;
 				for (Map env: list) {
 					try {
						BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(emailText.getBytes())));
 						log.debug("ENV = " + env);
 						count ++;
						String email = (String) env.get("email");
						sendEmailTemplate(email, br, env,  responseEmail, emailHost, emailPort);
						br.close();
						//sendEmailTemplate("geoff.ritchey@lcu.edu", br, env, responseEmail, emailHost, emailPort); // FOR TESTING one send
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
//					if (count > 10) {
//						break;  //FOR TESTING  ten sends
//					}
					//break;  //FOR TESTING one send
 				}
 				return recordTime("EMAILSENDE", "  " + count + " sent.");
 			} catch (Exception e) {
 				e.printStackTrace();
 				return recordTime("EMAILSENDE", e.getMessage());
 			}
 			
 	}
 	
 	@Override
 	public void sendEmailTest(Date startterm, String sendTo, String responseEmail, String emailHost, String emailPort) throws Exception{
 			log.debug("send email  startterm = " + startterm);
 			try {
 				List<Map> list = chapelPersonMapper.selectChapelStatus(startterm, new RowBounds(0, 1));
 				log.debug("count = " + list.size());
 				BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(getEmail().getBytes())));
 				if (list.size() > 0) {
 					sendEmailTemplate(sendTo, br, list.get(0), responseEmail, emailHost, emailPort);
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 	}
 	
 	/**
 	 * Send an email to emailName with the text of emailBody replacing variables declared in env.  Attach any 
 	 * pdf documents listed in documents.
 	 * @param emailName is the recipient of the email.
 	 * @param emailBody is the text of the email.
 	 * @param env is a hashmap of environmental variables to replace strings embedded in the email.  This should be
 	 * 	specific to the recipient of the email if it is used.  Otherwise the email body can be constant.
 	 * @param documents represents a list of database stored pdf files to be attached to the email.  It can be null.
 	 * @return
 	 * @throws Exception
 	 */
 	public static String sendEmailTemplate(String emailName, BufferedReader emailBody,
 			Map<String, Object> env, String responseEmail, 
 			String emailHost, String emailPort)
 			throws Exception {
	
 		StringBuffer out = new StringBuffer();
 		try {
 			String strInline;
 			Pattern pattern = Pattern.compile("\\$\\{[^}]*\\}");
 			Matcher matcher = null;
 			boolean print = true;
 			while ((strInline = emailBody.readLine()) != null) {
 				log.debug("str = '" + strInline + "'");
 				if (strInline.startsWith("#if ")) {
 
 					String var = strInline.substring(4).trim();
 					log.debug("#if '" + var + "'");
 					if (env != null) {
 						if (env.get(var) == null) {
 							print = false;
 						} else
 							print = true;
 					} else
 						print = false;
 				} else if (strInline.startsWith("#else")) {
 					log.debug("#else");
 					print = !print;
 				} else if (strInline.startsWith("#endif")) {
 					log.debug("#endif");
 					print = true;
 				} else if (print) {
 					log.debug("PRINT");
 					matcher = pattern.matcher(strInline);
 					boolean found = false;
 					StringBuffer output = new StringBuffer();
 					int lastEnd = 0;
 					String rest = null;
 					while (matcher.find()) {
 						output.append(strInline.substring(lastEnd,
 								matcher.start()));
 						if (env != null) {
 							String var = matcher.group().substring(2);
 							var = var.substring(0, var.length() - 1);
 							Object x = env.get(var);
 							log.debug(" matching '" + matcher.group() + "'");
 							if (x != null) {
 								output.append(x.toString());
 	
 							} else {
 								log.debug("No value for '" + matcher.group()
 										+ "'");
 							}
 						}
 						lastEnd = matcher.end();
 						found = true;
 						rest = strInline.substring(matcher.end());
 					}
 					if (found)
 						strInline = output.append(rest).toString();
 					strInline = strInline + "\r\n";
 					out.append(strInline);
 				}
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
		log.debug("create text part for " + out.toString());
 		List<MimeBodyPart> parts = new ArrayList<MimeBodyPart>();
 		MimeBodyPart textPart = createTextPart(out.toString());
 		parts.add(textPart);
		
 		try {
 			sendEmail(responseEmail,
			  /* send to this address */emailName,
			//"geoff.ritchey@lcu.edu",
 			/* email subject */"chapel status",
 			/* email body */parts, emailHost, emailPort);
 		} catch (AddressException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (MessagingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	public static MimeBodyPart createTextPart(String body)
 			throws MessagingException {
 		MimeBodyPart textBodyPart = new MimeBodyPart();
 		textBodyPart.setText(body, "utf-8", "html");
 
 		return textBodyPart;
 	}
 
 	public static String camelCase(String in) {
 		in = in.toLowerCase();
 		Pattern pat = Pattern.compile(" (\\p{Alpha})");
 		Matcher m = pat.matcher(in);
 		StringBuffer out = new StringBuffer();
 		String rest = null;
 		int lastEnd = 0;
 		while (m.find()) {
 			out.append(in.substring(lastEnd, m.start()));
 			out.append(m.group(0).toUpperCase());
 			rest = in.substring(m.end());
 			lastEnd = m.end();
 		}
 		return (out.toString() + rest).replaceAll("\\W", "");
 	}
 
 	public static void sendEmail(String sender, String recipients,
 			String subject, List<MimeBodyPart> parts, String host, String port) throws AddressException,
 			MessagingException {
 		
 		try {
 			log.debug("sendEmail");
 			Properties props = new Properties();
 			props.setProperty("mail.transport.protocol", "smtp");
 			props.setProperty("mail.host", host);
 			props.put("mail.smtp.port", port);
 			props.put("mail.smtp.socketFactory.port", port);
 			props.put("mail.smtp.socketFactory.fallback", "false");
 			props.setProperty("mail.smtp.quitwait", "false");
 
 			Session session = Session.getDefaultInstance(props);
 			if (!session.getProperty("mail.host").equals(host)) {
 				
 				session.getProperties().setProperty("mail.host", host);
 			};
 			if (!session.getProperty("mail.smtp.port").equals(port)) {
 				
 				session.getProperties().setProperty("mail.smtp.port", port);
 			};
 			log.debug("session set = " + session);
 			
 			MimeMessage message = new MimeMessage(session);
 			message.setSender(new InternetAddress(sender));
 			message.setFrom(new InternetAddress(sender));
 			message.setSubject(subject);
 
 			if (recipients.indexOf(',') > 0)
 				message.setRecipients(Message.RecipientType.TO,
 						InternetAddress.parse(recipients));
 			else
 				message.setRecipient(Message.RecipientType.TO, new InternetAddress(
 						recipients));
 
 			MimeMultipart mimeMultipart = new MimeMultipart();
 
 			for (MimeBodyPart part : parts) {
				log.debug("add part!!");
 				mimeMultipart.addBodyPart(part);
 			}
 
 			message.setContent(mimeMultipart);
 			log.debug("SEND");
 			Transport.send(message);
 			log.debug("SENT");
 		} catch (Exception e) {
 			log.debug("EXCEPTION");
 			e.printStackTrace();
 		}
 	}
 
 	People getPeople(String peopleId) {
 		PeopleExample example = new PeopleExample();
 		List<People> ret = null;
 		try {
 			example.createCriteria().andPeopleIdEqualTo(peopleId);
 			ret = peopleMapper.selectByExample(example);
 			if (ret.size() != 1) {
 				return null;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			People p = new People();
 			p.setFirstName("geoffrey");
 			p.setLastName("ritchey");
 			p.setMiddleName("walter");
 			p.setPeopleId("000244930");
 
 			return p;
 		}
 		return ret.get(0);
 	}
 	
 	public void sendAllEmails() {
 		ChapelPersonExample person = new ChapelPersonExample();
 		person.createCriteria().andCurrentstudentEqualTo(true);
 		List<ChapelPerson> currentStudents = chapelPersonMapper.selectByExample(person, null);
 		
 		
 	}
 
 	public final String EMAIL_TEXT_KEY = "EMAIL_TEXT";
 
 	@Override
 	public String getEmail() {
 		PropertiesExample pe = new PropertiesExample();
 		pe.createCriteria().andKeyvalueEqualTo(EMAIL_TEXT_KEY);
 		List<com.ritchey.chapelManage.domain.chapel.Properties> ret = propertiesMapper
 				.selectByExample(pe);
 		if (ret.size() > 0) {
 			return ret.get(0).getStrvalue();
 		}
 		return "";
 	}
 
 	@Override
 	public void saveEmail(String emailText) {
 		try {
 			com.ritchey.chapelManage.domain.chapel.Properties emailProperty = null;
 			PropertiesExample pe = new PropertiesExample();
 			pe.createCriteria().andKeyvalueEqualTo(EMAIL_TEXT_KEY);
 			List<com.ritchey.chapelManage.domain.chapel.Properties> ret = propertiesMapper
 					.selectByExample(pe);
 			if (ret.size() > 0) {
 				emailProperty = ret.get(0);
 				emailProperty.setStrvalue(emailText);
 				propertiesMapper.updateByPrimaryKey(emailProperty);
 			} else {
 				emailProperty = new com.ritchey.chapelManage.domain.chapel.Properties();
 				emailProperty.setKeyvalue(EMAIL_TEXT_KEY);
 				emailProperty.setStrvalue(emailText);
 				propertiesMapper.insert(emailProperty);
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public Map<String, String> getProperties() {
 		PropertiesExample pe = new PropertiesExample();
 		List<com.ritchey.chapelManage.domain.chapel.Properties> ret = propertiesMapper
 				.selectByExample(pe);
 		if (ret.size() > 0) {
 			Map<String, String> map = new HashMap<String, String>();
 			for (com.ritchey.chapelManage.domain.chapel.Properties prop: ret) {
 				map.put(prop.getKeyvalue(), prop.getStrvalue());
 			}
 			return map;
 		}
 		return null;
 	}
 	
 	@Override
 	public String getProperty(String key) {
 		PropertiesExample pe = new PropertiesExample();
 		pe.createCriteria().andKeyvalueEqualTo(key);
 		List<com.ritchey.chapelManage.domain.chapel.Properties> ret = propertiesMapper
 				.selectByExample(pe);
 		if (ret.size() > 0) {
 			return ret.get(0).getStrvalue();
 		}
 		return "";
 	}
 	
 	@Override
 	public void saveProperty(String key, String value) {
 		com.ritchey.chapelManage.domain.chapel.Properties emailProperty = null;
 		PropertiesExample pe = new PropertiesExample();
 		pe.createCriteria().andKeyvalueEqualTo(key);
 		List<com.ritchey.chapelManage.domain.chapel.Properties> ret = propertiesMapper
 				.selectByExample(pe);
 		if (ret.size() > 0) {
 			emailProperty = ret.get(0);
 			emailProperty.setStrvalue(value);
 			propertiesMapper.updateByPrimaryKey(emailProperty);
 		} else {
 			emailProperty = new com.ritchey.chapelManage.domain.chapel.Properties();
 			emailProperty.setKeyvalue(key);
 			emailProperty.setStrvalue(value);
 			propertiesMapper.insert(emailProperty);
 		}
 	}
 
 	/**
 	 * This is the detailed punch list for a scheduled event. start - where to
 	 * start paging length - page length filter - campusId, last name and first
 	 * name to filter on. Something like 'rit,g' or 'P000244930' to get geoff
 	 * ritchey. Leave null or empty string if filtering is not desired.
 	 * eventDescription - the name of the event if filtering on that is desired.
 	 * otherwise null or empty string. schedEvent - database key of the
 	 * scheduled event that the punches are associated with
 	 */
 	@Override
 	public ListCount<Map> getChapelHistory(Integer start, Integer length,
 			String filter, String eventDescription, Integer schedEvent, Boolean searchPunchedOnly) {
 		if (searchPunchedOnly == false)
 			searchPunchedOnly = null;
 		if (schedEvent == null)
 			return null;
 		String campusId = null;
 		String lastName = null;
 		String firstName = null;
 		if (filter != null && !filter.trim().equals("")) {
 			if (filter.matches("[Pp]?[0-9]{9}")) {
 				campusId = filter.replace("[pP]", "");
 			} else {
 				String[] param = filter.split(" *, *");
 				lastName = param[0] + "%";
 				if (param.length == 2) {
 					firstName = param[1] + "%";
 				}
 			}
 		}
 
 		ListCount ret = null;
 
 		try {
 			List<Map> history = punchMapper.selectChapelHistory(lastName,
 					firstName, campusId, eventDescription, schedEvent, searchPunchedOnly,
 					new RowBounds(start, length));
 			Integer count = punchMapper.selectCountChapelHistory(lastName,
 					firstName, campusId, eventDescription, schedEvent,searchPunchedOnly);
 			ret = new ListCount<Map>(history, count);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}
 
 	@Override
 	public Double setTardy(Integer id, Boolean isTardy) {
 		Double value = null;
 		try {
 			Punch punch = punchMapper.selectByPrimaryKey(id);
 			log.debug("id = " + id);
 			log.debug("punch = " + punch);
 			Event event = eventMapper.selectByPrimaryKey(punch.getEvent());
 			log.debug("event = " + event);
 			log.debug("isTardy = " + isTardy);
 			value = (isTardy) ? event.getTardyvalue() : event
 					.getAttendancevalue();
 
 			punch.setTardy(isTardy);
 			punch.setValue(value);
 			punchMapper.updateByPrimaryKey(punch);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return value;
 	}
 
 	void copyPeople() {
 		// PeopleExampel
 	}
 
 	@Override
 	public ListCount<Status> getStatusList() {
 		StatusExample example = new StatusExample();
 		List<Status> list = statusMapper.selectByExample(example);
 		ListCount<Status> ret = new ListCount<Status>(list, list.size());
 		return ret;
 	}
 
 	/**
 	public void updatePunchStatus(Integer punch, Double value, Boolean tardy, String comment) {
 		log.debug("SERVER 	update Punch Status comment = " + comment);
 		Punch record = new Punch();
 		record.setId(punch);
 		record.setValue(value);
 		record.setTardy(tardy);
 		record.setComment(comment);
 		log.debug("server punch object = " + record);
 		punchMapper.updateByPrimaryKeySelective(record);
 	}*/
 	
 	@Override
 	public Double updatePunchStatus(Integer punch, String status, String comment, String eventDescription) {
 
 		Event event = null;
 		if (eventDescription != null) {
 			EventExample example = new EventExample();
 			example.createCriteria().andDescriptionEqualTo(eventDescription);
 			List<Event> el = eventMapper.selectByExample(example);
 			event = el.get(0);
 		}
 		Boolean tardy =  null;
 		Double value = null;
 		if (status != null && event != null) {
 			tardy = false;
 			if ("tardy".equals(status)) {
 				tardy = true;
 				value = event.getTardyvalue();
 			}
 			else if ("present".equals(status)) {
 				value = event.getAttendancevalue();
 			}
 			else {
 				value = 0D;
 			}
 		}
 		log.debug("SERVER 	update Punch Status comment = " + comment);
 		Punch record = new Punch();
 		record.setId(punch);
 		record.setValue(value);
 		record.setTardy(tardy);
 		record.setComment(comment);
 		log.debug("server punch object = " + record);
 		punchMapper.updateByPrimaryKeySelective(record);
 		return value;
 	}
 
 	@Override
 	public Integer insertPunchStatus(Punch punch) {
 		punchMapper.insert(punch);
 		return punch.getId();
 	}
 
 	@Override
 	public ScheduledEvent getScheduledEvent(Integer id) throws Exception {
 		return scheduledEventMapper.selectByPrimaryKey(id);
 	}
 
 	@Override
 	public void setAllcredit(Integer id, Boolean value) {
 		try {
 			ScheduledEvent scheduledEvent = new ScheduledEvent();
 			scheduledEvent.setId(id);
 			scheduledEvent.setAllcredit(value);
 			scheduledEventMapper.updateByPrimaryKeySelective(scheduledEvent);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public Integer getCurrentStudentCount() {
 		ChapelPersonExample example = new ChapelPersonExample();
 		example.createCriteria().andCurrentstudentEqualTo(true);
 		return chapelPersonMapper.countByExample(example);
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	@Override
 	public Integer updateScheduledEvent(ScheduledEvent scheduledEvent,
 			String template, Date endTerm, Date today) throws Exception {
 		log.debug("updateScheduledEvent scheduledEvent = " + scheduledEvent + "  template = " + template);
 		
 		try {
 			if (!templatesEqual(scheduledEvent.getTemplate(), template)) {
 				checkFuture(scheduledEvent, endTerm, today, template);
 				if (template != null) {
 					log.debug("remove future");
 					removeFutureScheduledEvents(template, today);
 					String newTemplate = newGuid()
 							+ scheduledEvent.getTemplate().substring(scheduledEvent.getTemplate().indexOf(":"));
 					scheduledEvent.setTemplate(newTemplate);
 				}
 				else {
 					log.debug("delete by primary");
 					scheduledEventMapper.deleteByPrimaryKey(scheduledEvent.getId());
 				}
 				log.debug("create future");
 				createFutureScheduledEvents(scheduledEvent, endTerm, today);
 				ScheduledEventExample example = new ScheduledEventExample();
 				DateMidnight x = new LocalDate(today).toDateMidnight();
 				example.createCriteria().andTemplateEqualTo(scheduledEvent.getTemplate()).andStarttimeGreaterThanOrEqualTo(x.toDate());
 				return scheduledEventMapper.updateByExampleExtra(scheduledEvent, example);		
 			}
 			else if (scheduledEvent.getTemplate() != null) {
 				checkFuture(scheduledEvent, endTerm, today, scheduledEvent.getTemplate());
 				ScheduledEventExample example = new ScheduledEventExample();
 				example.createCriteria().andTemplateEqualTo(scheduledEvent.getTemplate()).andStarttimeGreaterThanOrEqualTo(new DateMidnight(today).toDate());
 				return scheduledEventMapper.updateByExampleExtra(scheduledEvent, example);		
 			}
 			else {
 				checkFuture(scheduledEvent, endTerm, today, null);
 				return scheduledEventMapper.updateByPrimaryKeySelective(scheduledEvent);
 			}
 		} catch (Exception e) {
 			Log.debug("MESSAGE + " + e.getMessage());
 			e.printStackTrace();
 			if (e.getMessage().contains("REFERENCE")) {
 				throw new RuntimeException("This scheduled event has people punched in");
 			}
 			throw new RuntimeException(e.getMessage());
 		}
 	}
 	
 	public Boolean templatesEqual(String template1, String template2) {
 		log.debug("template1 = " + template1 + "   template2 = " + template2);
 		if (template1 == template2) {
 			log.debug("template1 == template2 -- equal is true");
 			return true;
 		}
 		if (template1 == null) {
 			log.debug("template1 == null -- equal is false");
 			return false;
 		}
 		log.debug("equal is " + template1.equals(template2));
 		return template1.equals(template2);
 	}
 
 	public static SimpleDateFormat fmtDate = new SimpleDateFormat("MMM dd, yyyy");
 	public static SimpleDateFormat fmtTime = new SimpleDateFormat("HH:mm:ss");
 
 	@Transactional(rollbackFor = RuntimeException.class)
 	@Override
 	public Integer createScheduledEvent(ScheduledEvent scheduledEvent,
 			Date endTerm, Date today) throws RuntimeException {
 
 			try {
 				checkFuture(scheduledEvent, endTerm, today, null);
  
 				if (scheduledEvent.getTemplate() != null) {
 					log.debug("createScheduleEvent template = " + scheduledEvent.getTemplate());
 					String template = scheduledEvent.getTemplate();
 					template = newGuid()
 							+ template.substring(template.indexOf(":"));
 					log.debug("template CREATE = " + template);
 					scheduledEvent.setTemplate(template);
 					return createFutureScheduledEvents(scheduledEvent, endTerm, today);
 				}
 				else {
 					return scheduledEventMapper.insert(scheduledEvent);
 				}
 			} catch (Exception e) {
 				Log.debug("MESSAGE + " + e.getMessage());
 				e.printStackTrace();
 				if (e.getMessage().contains("REFERENCE")) {
 					throw new RuntimeException("This scheduled has people punched in");
 				}
 				throw new RuntimeException(e.getMessage());
 			}
 	}
 	
 	public static String newGuid() {
 		return java.util.UUID.randomUUID().toString();
 	}
 	
 	public void checkFuture(ScheduledEvent scheduledEvent, Date endTerm, Date today, String ignoreTemplate) throws Exception {
 		// Check for conflicts - if recurring then for whole term, otherwise
 		// just for today
 		Date range = null;
 		List<String> daysOfWeek = null;
 	
 			log.debug("checkFuture  " + scheduledEvent + "  " + endTerm + "   " + today);
 			range = (scheduledEvent.getTemplate() == null) ? today : endTerm;
 			
 			String template = scheduledEvent.getTemplate();
 			if (template == null) {
 				List<ScheduledEvent> conflicts = scheduledEventMapper.timeConflicts(scheduledEvent.getStarttime(), 
 						scheduledEvent.getEndtime(), scheduledEvent.getEvent(), scheduledEvent.getId());
 				if (conflicts.size() > 0) {
 					log.debug("checkFuture  THROW");
 					Date time = conflicts.get(0).getStarttime();
 					throw new Exception("Not saved:  This event has time conflicts with "
 							+ " the same event already scheduled on "
 							+ fmtDate.format(time) + " at " + fmtTime.format(time));
 				}
 				return;	
 			}
 			String[] templateParts = template.split(":");
 
 			String days = "NMTWHFS";
 			String dow = templateParts[2];
 			daysOfWeek = new ArrayList<String>();
 			for (Character dayOfWeek : dow.toCharArray()) {
 				daysOfWeek.add(datenames[days.indexOf(dayOfWeek)]);
 			}
 			
 		List<ScheduledEvent> futureConflicts = scheduledEventMapper
 				.futureConflicts(scheduledEvent.getStarttime(),
 						scheduledEvent.getEndtime(), scheduledEvent.getEvent(),
 						today, range, daysOfWeek,
 						scheduledEvent.getId(), ignoreTemplate);
 		if (futureConflicts.size() > 0) {
 			log.debug("checkFuture  THROW");
 			Date time = futureConflicts.get(0).getStarttime();
 			throw new Exception("Not saved:  This event has time conflicts with "
 					+ " the same event already scheduled on "
 					+ fmtDate.format(time) + " at " + fmtTime.format(time));
 		}
 	}
 
 	static String datenames[] = { "Sunday", "Monday", "Tuesday", "Wednesday",
 			"Thursday", "Friday", "Saturday" };
 
 	public Integer createFutureScheduledEvents(ScheduledEvent scheduledEvent,
 			Date endTerm, Date today) {
 		if (today == null) {
 			today = new Date();
 		}
 		String template = scheduledEvent.getTemplate();
 		if (template == null) {
 			return 0;
 		}
 		String[] templateParts = template.split(":");
 
 		String days = "NMTWHFS";
 		String dow = templateParts[2];
 		List<String> daysOfWeek = new ArrayList<String>();
 		for (Character dayOfWeek : dow.toCharArray()) {
 			daysOfWeek.add(datenames[days.indexOf(dayOfWeek)]);
 		}
 		
 		return scheduledEventMapper.createFutureEvents(scheduledEvent
 				.getEvent(), today, endTerm, scheduledEvent.getStarttime()
 				.getHours(), scheduledEvent.getStarttime().getMinutes(),
 				scheduledEvent.getEndtime().getHours(), scheduledEvent
 						.getEndtime().getMinutes(), daysOfWeek, scheduledEvent
 						.getTemplate());
 	}
 
 	public Integer countPeopleInFutureScheduledEvents(
 			ScheduledEvent scheduledEvent) {
 		return null;
 	}
 
 	@Transactional(rollbackFor = RuntimeException.class)
 	@Override
 	public Integer deleteEvent(ScheduledEvent scheduledEvent) throws Exception {
 		log.debug("delete " + scheduledEvent);
 		try {
 			if (scheduledEvent.getTemplate() != null) {
 				Integer count = removeFutureScheduledEvents(scheduledEvent.getTemplate(), scheduledEvent.getStarttime());
 				log.debug("count = " + count);
 				return count;
 			}
 			else {
 				Integer count = scheduledEventMapper.deleteByPrimaryKey(scheduledEvent.getId());
 				log.debug("remove by PK count = " + count);
 				return count;
 			}
 		} catch (Exception e) {
 			Log.debug("MESSAGE + " + e.getMessage());
 			e.printStackTrace();
 			if (e.getMessage().contains("REFERENCE")) {
 				throw new RuntimeException("This scheduled event can't be deleted because people are punched in");
 			}
 			throw new RuntimeException(e.getMessage());
 		}
 	}
 	
 	@Override
 	public Integer removeFutureScheduledEvents(String template, Date today) {
 		ScheduledEventExample example = new ScheduledEventExample();
 		example.createCriteria().andTemplateEqualTo(template)
 				.andStarttimeGreaterThanOrEqualTo(today);
 		return scheduledEventMapper.deleteByExample(example);
 	}
 	
 	@Override
 	public void getReport(String reportName, String filter) {
 		log.debug("get report   name:" + reportName + "  filter: " + filter);
 		Map<String, Object> env = new HashMap<String, Object>();
 		runXmlScript(reportName, null, null, env);
 	}
 	
 	static public XmlObject runXmlScript(String name, String object, String clause, Map env) {
     	SAXParserFactory factory = SAXParserFactory.newInstance();
     	factory.setNamespaceAware(true);
     	
     	
 		Map myMap = new HashMap(env);
 		myMap.put("reportClause", clause);
 		XmlParser f = null;
 		try {
 			f = new XmlParser("docs/scripts/" + name + ".xml", factory, myMap);
 		} catch (SAXParseException e) {
 			e.printStackTrace();
 		} catch (XMLBuildException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (SAXException e) {
 			e.printStackTrace();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		}
     	XmlObject root = f.getRoot();
     	log.debug("root = " + root);
     	try {
     		if (f != null) {
     			f.execute();
     		}
     	} catch (Exception e) {
     		e.printStackTrace();
     	} 
     	return root;
 	}
 	
 	  public String selectChapelStatus(Map<String, Object> params) {
 		  log.debug("selectChapelStatus env = " + params);
 		  new Exception().printStackTrace();
 		  String strQuery = null;
 		try {
 			Map<String, String> env = (Map<String, String>) params.get("env");
 			  params.putAll(env);
 			  strQuery = getMyBatisQuery(env, "errors", "reports/chapelMeetingAttendance.xml"); 
 			  log.debug("errors query = " + strQuery);
 			  log.debug("selectChapelStatus env = " + params);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		log.debug("Return query string  ");
 		  return strQuery;
 	  }
 
 	  public String getMyBatisQuery(Map<String, String> env, String queryName, String fileName) {
 //		  log.debug("myBatisQuery env = " + env);
 //		  	Map<String, Object> st = xmlReport.getSymbolTable();
 //		  	log.debug("myBatisQuery st = " + st);
 //		  		for (String key: env.keySet()) {
 //		  			st.put("_#" + key, env.get(key));
 //		  		}
 //				Sql sql = (Sql) st.get(queryName);
 //				String strQuery = sql.getQuery();
 //				log.debug("myBatisQuery = " + strQuery);
 //				String query = strQuery.replaceAll("__\\{", "#{");
 //				log.debug("getErrors query = "  +query);
 //			  return query;
 		  return null;
 	  }
 }
