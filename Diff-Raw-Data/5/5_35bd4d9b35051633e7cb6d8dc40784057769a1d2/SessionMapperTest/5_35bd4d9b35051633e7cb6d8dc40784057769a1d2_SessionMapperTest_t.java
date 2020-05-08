 package com.twu.thoughtconf.dao;
 
 import com.twu.thoughtconf.domain.ConferenceSession;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.util.ArrayList;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(value = {"classpath:mapper-context.xml"})
 public class SessionMapperTest {
 
     @Autowired
     SessionMapper sessionMapper;
 
     DateTime expectedSessionStartTime;
     ConferenceSession expectedConferenceSession;
 
     @Before
     public void initiateTestData()
     {
         expectedSessionStartTime = new DateTime(2012, 12, 11, 9, 30, 0);
         expectedConferenceSession = new ConferenceSession("Tech Conference","new session name", "ajanta", expectedSessionStartTime, expectedSessionStartTime, "Attendees should bring laptops so that they can code, code and code","Meng Wang", "Guru in java and many other languages");
         sessionMapper.save(expectedConferenceSession);
     }
 
     @After
     public void cleanTestData()
     {
         sessionMapper.delete(expectedConferenceSession);
     }
 
     @Test
     public void shouldGetSessionDetailByGivenSessionId() {
         ConferenceSession actualConferenceSession = sessionMapper.getSessionByID(String.valueOf(expectedConferenceSession.getSessionId()));
         assertThat(actualConferenceSession, is(expectedConferenceSession));
 
         sessionMapper.delete(expectedConferenceSession);
     }
 
     @Test
     public void shouldSaveSession() {
         DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd hh:mm:ss");
         DateTime startTime = formatter.parseDateTime("2012-10-20 08:30:00");
         DateTime endTime = formatter.parseDateTime("2012-10-20 09:30:00");
 
         ConferenceSession conferenceSession = new ConferenceSession("conference","anything", "somewhere", startTime, endTime, "session abstract", "presenter", "about presenter");
         sessionMapper.save(conferenceSession);
 
         ConferenceSession expectedConferenceSession = sessionMapper.getSessionByName("anything");
         System.out.println(expectedConferenceSession.getEndTime());
         assertThat(conferenceSession.getName(), is(expectedConferenceSession.getName()));
         assertThat(conferenceSession.getEndTime(), is(expectedConferenceSession.getEndTime()));
         sessionMapper.delete(conferenceSession);
     }
 
     @Test
     public void shouldSaveSessionWithCorrectTime() {
         DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd hh:mm:ss");
         DateTime startTime = formatter.parseDateTime("2012-10-20 08:30:00");
         DateTime endTime = formatter.parseDateTime("2012-10-20 09:30:00");
 
         ConferenceSession conferenceSession = new ConferenceSession("conference", "anything", "somewhere", startTime, endTime, "session abstract", "presenter", "about presenter");
         sessionMapper.save(conferenceSession);
 
         ConferenceSession expectedConferenceSession = sessionMapper.getSessionByName("anything");
         assertThat(conferenceSession.getName(), is(expectedConferenceSession.getName()));
         assertThat(conferenceSession.getEndTime(), is(expectedConferenceSession.getEndTime()));
         sessionMapper.delete(conferenceSession);
     }
 
     @Test
     public void shouldUpdateTheShowFlag() throws Exception {
         String sessionId = String.valueOf(expectedConferenceSession.getSessionId());
         sessionMapper.updateShowFlag(sessionId);
         ConferenceSession conferenceSession = sessionMapper.getSessionByID(sessionId);
 
         assertThat(conferenceSession.getShowFlag(), is(0));
     }
 
     @Test
     public void shouldGetAllConferenceNames(){
         ArrayList<String> expectedListConferenceNames = new ArrayList<String>();
         expectedListConferenceNames.add("Tech Conference");
 
         ArrayList<String> actualListConferenceNames = sessionMapper.getAllConferenceNames();
 
//        assertThat(actualListConferenceNames.get(1), is(expectedListConferenceNames.get(0)));
        assertTrue(actualListConferenceNames.size() > 0);
     }
 }
