 package com.ritchey.chapelManage.domain;
 
 import static org.junit.Assert.*;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.ibatis.session.RowBounds;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.BeanFactoryAware;
 import org.springframework.beans.factory.annotation.Configurable;
 import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import com.ritchey.chapelManage.mapper.chapel.PunchMapper;
 import com.ritchey.chapelManage.mapper.powercampus.AcademicCalendarMapper;
 
 @Configurable
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:/META-INF/spring/applicationContext.xml", 
 "classpath:/META-INF/spring/applicationContext-security.xml"})
 public class LcuchapelPunchTest implements BeanFactoryAware {
 	private static Log log = LogFactory.getLog(LcuchapelPunchTest.class);
 	ConfigurableListableBeanFactory bf = null;
 	
     @Test
     public void testMarkerMethod() {
 
     }
 
 	@Override
 	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
 		bf = (ConfigurableListableBeanFactory) beanFactory;
 	}
 	
     @Test    
     public void testFindTerms() {  
     	AcademicCalendarMapper p = (AcademicCalendarMapper) bf.getBean("academicCalendarMapper");
     }
     
     @Test 
     public void testMasterDetailCountAttendance() throws ParseException {
     	PunchMapper punchMapper = (PunchMapper) bf.getBean("punchMapper");
     	SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		//(ChapelPunchServiceImpl.java:255) - select punched Events date = null  id = null scheduleOnly = false
     	//select punched Events2  startday = Mon Aug 26 00:00:00 CDT 2013 date = null  id = null scheduleOnly = false
 		RowBounds r = new RowBounds(0, 10);
 		Date startday = fmt.parse("2013-08-26 00:00:00");// Start of the term
 		Date date = null;
 		Integer id = null;
 		Boolean scheduleOnly = false;
 		Boolean punchesOnly = false;
 		List<Map> list = punchMapper.selectPunchedEvents(startday, date, id,scheduleOnly, punchesOnly, r);
 		
 		System.err.println("master list 1st element = " + list.get(0));
 		Integer schedId = (Integer) list.get(0).get("id");
		List<Map> history = punchMapper.selectChapelHistory(null, null, null, null, schedId, true, new RowBounds(0, ((Integer)list.get(0).get("total"))+10));
 		System.err.println("detail list size = " + history.size());
		System.err.println("total list size = " + list.get(0).get("total"));
 		assertTrue(list.get(0).get("total").equals(history.size()));
 
 	}
 }
 	
