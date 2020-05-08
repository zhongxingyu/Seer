 package org.jasig.portlet.blackboardvcportlet.dao.impl;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 
 import java.util.Set;
 import java.util.concurrent.Callable;
 
 import org.jasig.portlet.blackboardvcportlet.dao.BlackboardSessionDao;
 import org.jasig.portlet.blackboardvcportlet.dao.BlackboardUserDao;
 import org.jasig.portlet.blackboardvcportlet.data.BlackboardSession;
 import org.jasig.portlet.blackboardvcportlet.data.BlackboardUser;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import com.elluminate.sas.SessionResponse;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = "classpath:jpaTestContext.xml")
 public class BlackboardSessionDaoImplTest extends BaseJpaDaoTest {
     @Autowired
     private BlackboardSessionDao blackboardSessionDao;
     @Autowired
     private BlackboardUserDao blackboardUserDao;
     
     
     @Test
     public void testEmptyQueries() throws Exception {
         this.execute(new Callable<Object>() {
             @Override
             public Object call() {
                 final BlackboardSession session = blackboardSessionDao.getSession(1);
                 assertNull(session);
                 
                 return null;
             }
         });
         
         this.execute(new Callable<Object>() {
             @Override
             public Object call() {
                 final BlackboardSession session = blackboardSessionDao.getSessionByBlackboardId(1);
                 assertNull(session);
                 
                 return null;
             }
         });
         
         this.execute(new Callable<Object>() {
             @Override
             public Object call() {
                 final Set<BlackboardUser> sessionChairs = blackboardSessionDao.getSessionChairs(1);
                 assertNull(sessionChairs);
                 
                 return null;
             }
         });
         
         this.execute(new Callable<Object>() {
             @Override
             public Object call() {
                 final Set<BlackboardUser> sessionNonChairs = blackboardSessionDao.getSessionNonChairs(1);
                 assertNull(sessionNonChairs);
                 
                 return null;
             }
         });
     }
     
     @Test
     public void testCreateUpdate() throws Exception {
         this.execute(new Callable<Object>() {
             @Override
             public Object call() {
                 final SessionResponse sessionResponse = new SessionResponse();
                 sessionResponse.setSessionId(106582);
                 sessionResponse.setSessionName("Test Session");
                 sessionResponse.setStartTime(1364566500000l);
                sessionResponse.setEndTime(13645674000000l);
                 sessionResponse.setCreatorId("admin@example.com");
                 sessionResponse.setBoundaryTime(30);
                 sessionResponse.setAccessType(2);
                 sessionResponse.setRecordings(false);
                 sessionResponse.setChairList("admin@example.com,dalquist@example.com");
                 sessionResponse.setNonChairList("levett@example.com");
                 sessionResponse.setOpenChair(false);
                 sessionResponse.setPermissionsOn(true);
                 sessionResponse.setMustBeSupervised(true);
                 sessionResponse.setRecordingModeType(3);
                 sessionResponse.setMaxTalkers(6);
                 sessionResponse.setMaxCameras(6);
                 sessionResponse.setRaiseHandOnEnter(false);
                 sessionResponse.setReserveSeats(0);
                 sessionResponse.setSecureSignOn(false);
                 sessionResponse.setVersionId(111);
                 sessionResponse.setAllowInSessionInvites(true);
                 sessionResponse.setHideParticipantNames(true);
                 
                 final BlackboardSession session = blackboardSessionDao.createSession(sessionResponse);
                 assertNotNull(session);
 
                 verifyCreatedSession();
                 verifyCreatedUsers();
                 
                 return null;
             }
         });
         
         //Verify the session exists and the session users exist
         this.execute(new Callable<Object>() {
             @Override
             public Object call() {
                 verifyCreatedSession();
                 
                 return null;
             }
         });
         
         //Verify the users are setup correctly
         this.execute(new Callable<Object>() {
             @Override
             public Object call() {
                 verifyCreatedUsers();
                 
                 return null;
             }
         });
         
         this.execute(new Callable<Object>() {
             @Override
             public Object call() {
                 final SessionResponse sessionResponse = new SessionResponse();
                 sessionResponse.setSessionId(106582);
                 sessionResponse.setSessionName("Test Session");
                 sessionResponse.setStartTime(1364566500000l);
                sessionResponse.setEndTime(13645674000000l);
                 sessionResponse.setCreatorId("admin@example.com");
                 sessionResponse.setBoundaryTime(30);
                 sessionResponse.setAccessType(2);
                 sessionResponse.setRecordings(false);
                 sessionResponse.setChairList("admin@example.com");
                 sessionResponse.setNonChairList("levett@example.com,dalquist@example.com");
                 sessionResponse.setOpenChair(false);
                 sessionResponse.setPermissionsOn(true);
                 sessionResponse.setMustBeSupervised(true);
                 sessionResponse.setRecordingModeType(3);
                 sessionResponse.setMaxTalkers(6);
                 sessionResponse.setMaxCameras(6);
                 sessionResponse.setRaiseHandOnEnter(false);
                 sessionResponse.setReserveSeats(0);
                 sessionResponse.setSecureSignOn(false);
                 sessionResponse.setVersionId(111);
                 sessionResponse.setAllowInSessionInvites(true);
                 sessionResponse.setHideParticipantNames(true);
                 
                 final BlackboardSession session = blackboardSessionDao.updateSession(sessionResponse);
                 assertNotNull(session);
                 
                 verifyUpdatedSession();
                 verifyUpdatedUsers();
                 
                 return null;
             }
         });
         
         //Verify the session exists and the session users exist
         this.execute(new Callable<Object>() {
             @Override
             public Object call() {
                 verifyUpdatedSession();
                 
                 return null;
             }
         });
         
         //Verify the users are setup correctly
         this.execute(new Callable<Object>() {
             @Override
             public Object call() {
                 verifyUpdatedUsers();
                 
                 return null;
             }
         });
     }
 
     private void verifyUpdatedSession() {
         final BlackboardSession session = blackboardSessionDao.getSessionByBlackboardId(106582);
         assertNotNull(session);
         assertEquals(106582, session.getBbSessionId());
         
         final Set<BlackboardUser> sessionChairs = blackboardSessionDao.getSessionChairs(session.getSessionId());
         assertNotNull(sessionChairs);
         assertEquals(1, sessionChairs.size());
         
         final Set<BlackboardUser> sessionNonChairs = blackboardSessionDao.getSessionNonChairs(session.getSessionId());
         assertNotNull(sessionNonChairs);
         assertEquals(2, sessionNonChairs.size());
     }
 
     private void verifyUpdatedUsers() {
         final BlackboardUser admin = blackboardUserDao.getBlackboardUser("admin@example.com");
         final Set<BlackboardSession> adminChaired = blackboardUserDao.getChairedSessionsForUser(admin.getUserId());
         assertNotNull(adminChaired);
         assertEquals(1, adminChaired.size());
         final Set<BlackboardSession> adminNonChaired = blackboardUserDao.getNonChairedSessionsForUser(admin.getUserId());
         assertNotNull(adminNonChaired);
         assertEquals(0, adminNonChaired.size());
         
 
         final BlackboardUser dalquist = blackboardUserDao.getBlackboardUser("dalquist@example.com");
         final Set<BlackboardSession> dalquistChaired = blackboardUserDao.getChairedSessionsForUser(dalquist.getUserId());
         assertNotNull(dalquistChaired);
         assertEquals(0, dalquistChaired.size());
         final Set<BlackboardSession> dalquistNonChaired = blackboardUserDao.getNonChairedSessionsForUser(dalquist.getUserId());
         assertNotNull(dalquistNonChaired);
         assertEquals(1, dalquistNonChaired.size());
         
 
         final BlackboardUser levett = blackboardUserDao.getBlackboardUser("levett@example.com");
         final Set<BlackboardSession> levettChaired = blackboardUserDao.getChairedSessionsForUser(levett.getUserId());
         assertNotNull(levettChaired);
         assertEquals(0, levettChaired.size());
         final Set<BlackboardSession> levettNonChaired = blackboardUserDao.getNonChairedSessionsForUser(levett.getUserId());
         assertNotNull(levettNonChaired);
         assertEquals(1, levettNonChaired.size());
     }
 
     private void verifyCreatedUsers() {
         final BlackboardUser admin = blackboardUserDao.getBlackboardUser("admin@example.com");
         final Set<BlackboardSession> adminChaired = blackboardUserDao.getChairedSessionsForUser(admin.getUserId());
         assertNotNull(adminChaired);
         assertEquals(1, adminChaired.size());
         final Set<BlackboardSession> adminNonChaired = blackboardUserDao.getNonChairedSessionsForUser(admin.getUserId());
         assertNotNull(adminNonChaired);
         assertEquals(0, adminNonChaired.size());
         
 
         final BlackboardUser dalquist = blackboardUserDao.getBlackboardUser("dalquist@example.com");
         final Set<BlackboardSession> dalquistChaired = blackboardUserDao.getChairedSessionsForUser(dalquist.getUserId());
         assertNotNull(dalquistChaired);
         assertEquals(1, dalquistChaired.size());
         final Set<BlackboardSession> dalquistNonChaired = blackboardUserDao.getNonChairedSessionsForUser(dalquist.getUserId());
         assertNotNull(dalquistNonChaired);
         assertEquals(0, dalquistNonChaired.size());
         
 
         final BlackboardUser levett = blackboardUserDao.getBlackboardUser("levett@example.com");
         final Set<BlackboardSession> levettChaired = blackboardUserDao.getChairedSessionsForUser(levett.getUserId());
         assertNotNull(levettChaired);
         assertEquals(0, levettChaired.size());
         final Set<BlackboardSession> levettNonChaired = blackboardUserDao.getNonChairedSessionsForUser(levett.getUserId());
         assertNotNull(levettNonChaired);
         assertEquals(1, levettNonChaired.size());
     }
 
     private void verifyCreatedSession() {
         final BlackboardSession session = blackboardSessionDao.getSessionByBlackboardId(106582);
         assertNotNull(session);
         assertEquals(106582, session.getBbSessionId());
         
         final Set<BlackboardUser> sessionChairs = blackboardSessionDao.getSessionChairs(session.getSessionId());
         assertNotNull(sessionChairs);
         assertEquals(2, sessionChairs.size());
         
         final Set<BlackboardUser> sessionNonChairs = blackboardSessionDao.getSessionNonChairs(session.getSessionId());
         assertNotNull(sessionNonChairs);
         assertEquals(1, sessionNonChairs.size());
     }
 }
