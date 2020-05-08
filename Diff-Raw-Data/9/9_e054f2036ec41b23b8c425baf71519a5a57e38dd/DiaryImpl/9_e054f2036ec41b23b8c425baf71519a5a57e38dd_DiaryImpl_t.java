 package com.zotyo.diary.ws;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.annotation.Resource;
 import javax.annotation.PostConstruct;
 import javax.jws.HandlerChain;
 import javax.jws.WebService;
 import javax.servlet.ServletContext;
 import javax.xml.ws.WebServiceContext;
 
import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.SpringBeanAutowiringSupport;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 
 import com.zotyo.diary.persistence.DiaryDAO;
 import com.zotyo.diary.pojos.Day;
 import com.zotyo.diary.pojos.Event;
 import com.zotyo.diary.util.DateUtil;
 
 
 @WebService(endpointInterface = "com.zotyo.diary.ws.Diary", wsdlLocation="WEB-INF/wsdl/DiaryImplService.wsdl")
 @HandlerChain(file = "handler-chain.xml")
 public class DiaryImpl /* extends SpringBeanAutowiringSupport */ implements Diary {
 	
	private static Logger logger = Logger.getLogger(DiaryImpl.class);
	
 	@Resource
 	private WebServiceContext context; 
 	
 	@Autowired
 	private DiaryDAO diaryDAO;
 
 	public List<Event> getEventsForADay(Date theDay) {
 		return diaryDAO.getEventsForADay(DateUtil.resetHMS(theDay));
 	}
 
 	public List<Day> getDaysForAMonth(int year, int month) {
 		return diaryDAO.getDaysForAMonth(year, month);
 	}
 	
 	public List<Day> getAllDaysInDiary() {
 		return diaryDAO.getAllDaysInDiary();
 	}
 	
 	public Day getDay(Date theDay) {
		Date date = DateUtil.resetHMS(theDay);
		logger.info("Checking the date: " + date);
		return diaryDAO.getDay(date);
 	}
 
 	public List<Event> getAllEvents() {
 		return diaryDAO.getAllEvents();
 	}
 
 	public List<Event> searchEvents(String searchTerm) {
 		return diaryDAO.searchEvents(searchTerm);
 	}
 
 	public List<String> searchTerms(String term) {
 		if (term.length() < 2) {
 			return new ArrayList<String>();
 		}
 		return diaryDAO.searchTerms(term.toLowerCase());
 	}
 	
 	public void addDay(Day day) {
 		day.setTheDay(DateUtil.resetHMS(day.getTheDay()));
 		diaryDAO.addDay(day);
 	}
 
 	public void addEvent(Date theDay, Event event) {
 		theDay = DateUtil.resetHMS(theDay);
 		diaryDAO.addEvent(theDay, event);
 	}
 	
 	
 	@PostConstruct
 	private void getDAOBean() {
 		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);	
 	}	
 	// This method could be used if not extending from SpringBeanAutowiringSupport
 	// problem: getMessageContext() can only be called while serving a request
 	// @PostConstruct
 	private void getDAOBean2() {
 		ServletContext servletContext = (ServletContext) context.getMessageContext().get("javax.xml.ws.servlet.context");
 		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
 		diaryDAO = webApplicationContext.getAutowireCapableBeanFactory().getBean("diaryDAOJPAImpl", DiaryDAO.class);		
 	}
 }
