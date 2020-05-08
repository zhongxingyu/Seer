 package com.zotyo.diary.web;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.namespace.QName;
 import javax.xml.ws.BindingProvider;
 import javax.xml.ws.handler.MessageContext;
 
 import org.apache.log4j.Logger;
 
 import com.zotyo.diary.client.Day;
 import com.zotyo.diary.client.Event;
 import com.zotyo.diary.client.Diary;
 import com.zotyo.diary.client.DiaryImplService;
 
 public class DiaryServlet extends HttpServlet {
 	
 	private static Logger logger = Logger.getLogger(DiaryServlet.class); 
 	
 	private Diary diary;
 	private DiaryHelper diaryHelper;
 	private DatatypeFactory df;
 	private String keyword;
 	private DiaryCache diaryCache;
 	
 	public void init() throws ServletException {
 		try {
 			InputStream inputStream = ClassLoader.getSystemResourceAsStream("diary.properties");
 			Properties props = new Properties();
 			props.load(inputStream);
 			
 			keyword = props.getProperty("keyword");
 			
 			URL wsdlURL = new URL(props.getProperty("wsdlURL"));
 			DiaryImplService diaryService = new DiaryImplService(wsdlURL, new QName("http://ws.diary.zotyo.com/", "DiaryImplService")); 
 			diary = diaryService.getDiaryImplPort();
 			diaryHelper = new DiaryHelper();
             df = DatatypeFactory.newInstance();
             diaryCache = DiaryCache.getInstance();
 		} catch(IOException ioex) {
 			ioex.printStackTrace();
 		} catch (DatatypeConfigurationException dce) {
             throw new IllegalStateException("Exception while obtaining DatatypeFactory instance", dce);
 		}
 	}
 	
 	@Override
 	public void doGet(HttpServletRequest request, HttpServletResponse response) 
 											throws ServletException, IOException {
 		
 		String theDayString = request.getParameter("theDay");
 		String command = request.getParameter("cmd");
 		if ("addday".equals(command)) {
 			RequestDispatcher rd = getServletContext().getRequestDispatcher("/addday.jsp");
 			rd.forward(request, response);
 			
 			return;
 		}
 		if ("addevent".equals(command)) {
 			RequestDispatcher rd = getServletContext().getRequestDispatcher("/addevent.jsp");
 			rd.forward(request, response);
 			
 			return;
 		}
 		if ("allday".equals(command)) {
 			List<Day> days = new ArrayList<Day>();
 			String yearString = request.getParameter("year");
 			String monthString = request.getParameter("months");
 			if (yearString != null && yearString.length() > 0 && monthString != null && monthString.length() > 0) {
 				int year = Integer.parseInt(yearString);
 				int months = Integer.parseInt(monthString);
 				days = diary.getDaysForAMonth(year, months);
 				request.setAttribute("year", year);
 				request.setAttribute("months", months);
 			}
 			else {
 				GregorianCalendar theDayCal = new GregorianCalendar();
 				theDayCal.setTime(new Date());
 				days = diary.getDaysForAMonth(theDayCal.get(Calendar.YEAR), theDayCal.get(Calendar.MONTH));
 				request.setAttribute("year", theDayCal.get(Calendar.YEAR));
 				request.setAttribute("months", theDayCal.get(Calendar.MONTH));
 			}
 			request.setAttribute("alldays", days);
 			RequestDispatcher rd = getServletContext().getRequestDispatcher("/alldays.jsp");
 			rd.forward(request, response);
 			
 			return;
 		}
 		if ("latests".equals(command)) {
 			List<Event> events = diary.getAllEvents();
 			List<Event> latests = new ArrayList<Event>();
 			for (int i=0; i<5; i++) {
 				latests.add(events.get(i));
 			}
 			request.setAttribute("latests", latests);
 			RequestDispatcher rd = getServletContext().getRequestDispatcher("/latests.jsp");
 			rd.forward(request, response);
 			
 			return;
 		}
 		if ("videos".equals(command)) {
 			RequestDispatcher rd = getServletContext().getRequestDispatcher("/videos.jsp");
 			rd.forward(request, response);
 			
 			return;
 		}
 	
 		if ("getDays".equals(command)) {
 			String key = request.getParameter("key");
 			List<Integer> days = diaryCache.getEventDays(key, diary);
 			StringBuilder sb = new StringBuilder();
 			sb.append("[");
 			for (int i = 0; i < days.size(); i++) {
 				sb.append(days.get(i));
 				if (i != days.size() - 1) {
 					sb.append(",");	
 				}
 			}
 			sb.append("]");
 			request.setAttribute("days", sb.toString());
 			RequestDispatcher rd = getServletContext().getRequestDispatcher("/calendardays.jsp");
 			rd.forward(request, response);
 			
 			return;
 		}
 		
 		SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
 		if (theDayString != null && theDayString.length() > 0) {
 			Date theDay = null;
 			try {
 				theDay = format.parse(request.getParameter("theDay"));
 			} catch (ParseException e) {
 				logger.error(e);
 			}
 			if (theDay == null) theDay = new Date();
 			
 			GregorianCalendar theDayCal = new GregorianCalendar();
 			theDayCal.setTime(theDay);
 			Day d = diary.getDay(df.newXMLGregorianCalendar(theDayCal));
 			if (d != null) {
 				request.setAttribute("eventsOfTheDay", d.getEventsOfTheDay());
 				request.setAttribute("descriptionOfTheDay", d.getDescriptionOfTheDay());
 			} else {
 				request.setAttribute("eventsOfTheDay", new ArrayList<Event>());
 			}
 			request.setAttribute("theDay", theDayString);
 			
 
 			RequestDispatcher rd = getServletContext().getRequestDispatcher("/events.jsp");
 			rd.forward(request, response);
 		}
 		else {
 			Date theDay = new Date();
 			GregorianCalendar theDayCal = new GregorianCalendar();
 			theDayCal.setTime(theDay);
 			Day d = diary.getDay(df.newXMLGregorianCalendar(theDayCal));
 			if (d != null) {
 				theDayString = format.format(new Date());
 				request.setAttribute("theDay", theDayString);
 				request.setAttribute("eventsOfTheDay", d.getEventsOfTheDay());
 				request.setAttribute("descriptionOfTheDay", d.getDescriptionOfTheDay());
 			} else {
 				request.setAttribute("eventsOfTheDay", new ArrayList<Event>());
 			}
 			
 			GregorianCalendar birthDate = new GregorianCalendar();
 			birthDate.set(2011, 8, 17, 13, 55, 0);
 			long delta = theDayCal.getTimeInMillis() - birthDate.getTimeInMillis();
 			
 			GregorianCalendar deltaDate = new GregorianCalendar();
 			deltaDate.setTimeInMillis(delta);
 			 
 			StringBuilder sb = new StringBuilder();
 			sb.append(deltaDate.get(Calendar.WEEK_OF_YEAR));
 			sb.append(" hónapos és ");
 			sb.append(deltaDate.get(Calendar.DATE));
 			sb.append(" napos ");
 			request.setAttribute("age", sb.toString());
 			
 	        RequestDispatcher rd = getServletContext().getRequestDispatcher("/diary.jsp");
 	        rd.forward(request, response);
 		}
 	}
 
 	@Override
 	public void doPost(HttpServletRequest request, HttpServletResponse response) 
 											throws ServletException, IOException {
 		String command = request.getParameter("cmd");
 		if ("search".equals(command)) {
 			String searchTerm = request.getParameter("searchTerm");	
 			List<Event> result = diary.searchEvents(searchTerm);
 			request.setAttribute("result", result);
 			request.setAttribute("searchTerm", searchTerm);
 			RequestDispatcher rd = getServletContext().getRequestDispatcher("/result.jsp");
 			rd.forward(request, response);
 			
 			return;
 		}
 		String key = request.getParameter("keyword");
 		if (!diaryHelper.md5(key).equals(keyword)) {
 			response.sendRedirect("/diaryweb");
 			return;
 		}
 		String theDay = request.getParameter("theDay");
 		String descriptionOfTheDay = request.getParameter("descriptionOfTheDay");
 		String duration = request.getParameter("duration");
 		String initialEvent = request.getParameter("initialEvent");
 		String startDate = request.getParameter("startDate");
 		
 		if ("add_day".equals(command)) {
 			if (theDay != null && theDay.length() > 0) {
 				Day day = new Day();
 				GregorianCalendar theDayCal = diaryHelper.getDayCal(theDay);
 				day.setTheDay(df.newXMLGregorianCalendar(theDayCal));
 				day.setDescriptionOfTheDay(descriptionOfTheDay);
 				
 				Event event = new Event();
 				event.setDescription(initialEvent);
 				event.setDuration(diaryHelper.getDuration(duration));
 				GregorianCalendar startDateCal = diaryHelper.getStartDateCal(startDate);
 				event.setStartTime(df.newXMLGregorianCalendar(startDateCal));
 				day.getEventsOfTheDay().add(event);
 				//addKeyword(key, ((BindingProvider)diary).getRequestContext());
 				((BindingProvider)diary).getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS,
 					    Collections.singletonMap("keyword",Collections.singletonList(key)));
 				diary.addDay(day);
 				diaryCache.purgeKey(theDayCal.get(Calendar.YEAR) + "-" + theDayCal.get(Calendar.MONTH));
 			}
 		} else if ("add_event".equals(command)) {
 			GregorianCalendar theDayCal = diaryHelper.getDayCal(theDay);
 			Event event = new Event();
 			event.setDescription(initialEvent);
 			event.setDuration(diaryHelper.getDuration(duration));
 			GregorianCalendar startDateCal = diaryHelper.getStartDateCal(startDate);
 			event.setStartTime(df.newXMLGregorianCalendar(startDateCal));
 			//addKeyword(key, ((BindingProvider)diary).getRequestContext());
 			((BindingProvider)diary).getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS,
 				    Collections.singletonMap("keyword",Collections.singletonList(key)));
 			diary.addEvent(df.newXMLGregorianCalendar(theDayCal), event);
 		}
 		
 		response.sendRedirect("/diaryweb");
 	}
 
 	private void addKeyword(String key, Map<String, Object> requestContext) {
 		Map<String, List<String>> header = new HashMap<String, List<String>>();
 		header.put("keyword", Collections.singletonList(key));
 		requestContext.put(MessageContext.HTTP_REQUEST_HEADERS, header);
 	}
 	
 }
