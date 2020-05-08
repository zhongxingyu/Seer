 package org.jasig.portlet.blackboardvcportlet.dao.impl;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Set;
 import java.util.concurrent.Callable;
 
 import org.jasig.portlet.blackboardvcportlet.dao.ConferenceUserDao;
 import org.jasig.portlet.blackboardvcportlet.dao.MultimediaDao;
 import org.jasig.portlet.blackboardvcportlet.dao.PresentationDao;
 import org.jasig.portlet.blackboardvcportlet.dao.SessionDao;
 import org.jasig.portlet.blackboardvcportlet.dao.UserSessionUrlDao;
 import org.jasig.portlet.blackboardvcportlet.data.ConferenceUser;
 import org.jasig.portlet.blackboardvcportlet.data.Multimedia;
 import org.jasig.portlet.blackboardvcportlet.data.Presentation;
 import org.jasig.portlet.blackboardvcportlet.data.Session;
 import org.jasig.portlet.blackboardvcportlet.data.UserSessionUrl;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.support.DataAccessUtils;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import com.elluminate.sas.BlackboardMultimediaResponse;
 import com.elluminate.sas.BlackboardPresentationResponse;
 import com.elluminate.sas.BlackboardSessionResponse;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = "classpath:jpaTestContext.xml")
 public class SessionDaoImplTest extends BaseJpaDaoTest {
 	
 	final private long SESSION_ID = 106582;
     @Autowired
     private SessionDao sessionDao;
     @Autowired
     private ConferenceUserDao conferenceUserDao;
     @Autowired
     private MultimediaDao multimediaDao;
     @Autowired
     private PresentationDao presentationDao;
     @Autowired
     private UserSessionUrlDao userSessionUrlDao;
     
     
     @Test
     public void testEmptyQueries() throws Exception {
         this.execute(new Callable<Object>() {
             @Override
             public Object call() {
                 final Session session = sessionDao.getSession(1);
                 assertNull(session);
                 
                 return null;
             }
         });
         
         this.execute(new Callable<Object>() {
             @Override
             public Object call() {
                 final Session session = sessionDao.getSessionByBlackboardId(1);
                 assertNull(session);
                 
                 return null;
             }
         });
         
         this.execute(new Callable<Object>() {
             @Override
             public Object call() {
                 final Session session = sessionDao.getSession(1);
                 final Set<ConferenceUser> sessionChairs = sessionDao.getSessionChairs(session);
                 assertNull(sessionChairs);
                 
                 return null;
             }
         });
         
         this.execute(new Callable<Object>() {
             @Override
             public Object call() {
                 final Session session = sessionDao.getSession(1);
                 final Set<ConferenceUser> sessionNonChairs = sessionDao.getSessionNonChairs(session);
                 assertNull(sessionNonChairs);
                 
                 return null;
             }
         });
     }
     
     @Test
     public void testCreateSessionURLThenDeletingSession() {
     	//create a session with id SESSION_ID
     	this.execute(new Callable<Object>() {
             @Override
             public Object call() {
             	BlackboardSessionResponse sessionResponse = generateSessionResponse();
                 
                 final Session session = sessionDao.createSession(sessionResponse, "http://www.example.com/session");
                 assertNotNull(session);
 
                 verifyCreatedSession();
                 verifyCreatedUsers();
                 
                 final ConferenceUser user = conferenceUserDao.getUser("admin@example.com");
     			assertNotNull(user);
     			UserSessionUrl url = userSessionUrlDao.createUserSessionUrl(session, user, "http://www.example.com/aliens");
     			assertNotNull(url);
                 
                 return null;
             }
         });
     	
     	//Create url and link it to the session
     	this.execute(new Callable<Object>() {
     		@Override
     		public Object call() {
     			final Session session = sessionDao.getSessionByBlackboardId(SESSION_ID);
     			assertNotNull(session);
     			
     			sessionDao.deleteSession(session);
     			
                 return null;
     		}
     	});
     }
     
     @Test
     public void testCreateSessionAddsCreatorAsAModeratorAKAChair() {
     	//create a session with id SESSION_ID
     	this.execute(new Callable<Object>() {
             @Override
             public Object call() {
             	BlackboardSessionResponse sessionResponse = generateSessionResponse();
                 
                 final Session session = sessionDao.createSession(sessionResponse, "http://www.example.com/session");
                 assertNotNull(session);
 
                 verifyCreatedSession();
                 verifyCreatedUsers();
 
                 return null;
             }
         });
     	
     	//Create url and link it to the session
     	this.execute(new Callable<Object>() {
     		@Override
     		public Object call() {
     			final Session session = sessionDao.getSessionByBlackboardId(SESSION_ID);
     			assertNotNull(session);
     			
     			Set<ConferenceUser> sessionChairs = sessionDao.getSessionChairs(session);
     			
     			assertNotNull(sessionChairs);
     			assertEquals(2,sessionChairs.size());
     			assertTrue(sessionChairs.contains(session.getCreator()));
     			
                 return null;
     		}
     	});
     }
     
     @Test
 	public void testLinkMultimediaToSession() {
     	//create a session with id sessionId
     	this.execute(new Callable<Object>() {
             @Override
             public Object call() {
             	BlackboardSessionResponse sessionResponse = generateSessionResponse();
                 
                 final Session session = sessionDao.createSession(sessionResponse, "http://www.example.com/session");
                 assertNotNull(session);
 
                 verifyCreatedSession();
                 verifyCreatedUsers();
                 
                 return null;
             }
         });
     	
     	//Create multimedia and link it to the session
     	this.execute(new Callable<Object>() {
     		@Override
     		public Object call() {
     			final BlackboardMultimediaResponse response = new BlackboardMultimediaResponse();
                 response.setCreatorId("test@example.com");
                 response.setDescription("super sweet media");
                 response.setMultimediaId(183838);
                 response.setSize(1024);
                 
                 final Multimedia mm = multimediaDao.createMultimedia(response, "aliens_exist.pdf");
                 assertNotNull(mm);
                 
                 //add link
                 final Session session = sessionDao.getSessionByBlackboardId(SESSION_ID);
                 sessionDao.addMultimediaToSession(session, mm);
                 return null;
     		}
     	});
     	
     	//Verify it exists in session lists
     	this.execute(new Callable<Object>() {
     		@Override
     		public Object call() {
     			Session session = sessionDao.getSessionByBlackboardId(SESSION_ID);
     			Set<Multimedia> multimedias = sessionDao.getSessionMultimedias(session);
     			assertNotNull(multimedias);
     			assertEquals(1,multimedias.size());
     			assertEquals(183838, DataAccessUtils.singleResult(multimedias).getBbMultimediaId());
     			
     			return null;
     		}
     	});
     	
     	//drop link
     	this.execute(new Callable<Object>() {
     		@Override
     		public Object call() {
     			Session session = sessionDao.getSessionByBlackboardId(SESSION_ID);
     			assertNotNull(session);
     			Multimedia mm = multimediaDao.getMultimediaByBlackboardId(183838);
     			sessionDao.deleteMultimediaFromSession(session, mm);
     			
     			return null;
     		}
     	});
     	
     	//Verify the drop worked
     	this.execute(new Callable<Object>() {
     		@Override
     		public Object call() {
     			Session session = sessionDao.getSessionByBlackboardId(SESSION_ID);
     			Set<Multimedia> multimedias = sessionDao.getSessionMultimedias(session);
     			assertNotNull(multimedias);
     			assertEquals(0,multimedias.size());
     			
     			return null;
     		}
     	});
 	}
     
     @Test
     public void testCreateSessionAndAddCreatorToTheChairListDOTShouldOnlyAddThemOnce() {
     	//create a session with id SESSION_ID
     	this.execute(new Callable<Object>() {
             @Override
             public Object call() {
             	BlackboardSessionResponse sessionResponse = generateSessionResponse();
                 sessionResponse.setChairList(sessionResponse.getChairList() + ",admin@example.com");
                 final Session session = sessionDao.createSession(sessionResponse, "http://www.example.com/session");
                 
                 Set<ConferenceUser> sessionChairs = sessionDao.getSessionChairs(session);
                 assertEquals(2,sessionChairs.size());
 
                 
                 
                 return null;
             }
         });
     }
 	
 	@Test 
 	public void testPresentationIntegration() {
 		//create a session with id sessionId
     	this.execute(new Callable<Object>() {
             @Override
             public Object call() {
             	BlackboardSessionResponse sessionResponse = generateSessionResponse();
                 
                 final Session session = sessionDao.createSession(sessionResponse, "http://www.example.com/session");
                 assertNotNull(session);
 
                 verifyCreatedSession();
                 verifyCreatedUsers();
                 
                 return null;
             }
         });
     	
     	//Create presentation and link it to the session
     	this.execute(new Callable<Object>() {
     		@Override
     		public Object call() {
     			final BlackboardPresentationResponse response = new BlackboardPresentationResponse();
                 response.setCreatorId("test@example.com");
                 response.setDescription("super sweet media");
                 response.setPresentationId(183838);
                 response.setSize(1024);
                 
                 final Presentation pres = presentationDao.createPresentation(response, "aliens_exist.pdf");
                 assertNotNull(pres);
                 
                 Session session = sessionDao.getSessionByBlackboardId(SESSION_ID);
                 sessionDao.addPresentationToSession(session,	pres);
                 
                 session = sessionDao.getSessionByBlackboardId(SESSION_ID);
                 assertNotNull(session.getPresentation());
                 assertEquals(session.getPresentation(),pres);
                 
                 sessionDao.removePresentationFromSession(session);
                 session = sessionDao.getSessionByBlackboardId(SESSION_ID);
                 assertNull(session.getPresentation());
                 
                 
                 return null;
     		}
     	});
 	}
     
     @Test
     public void testCreateUpdate() throws Exception {
         this.execute(new Callable<Object>() {
             @Override
             public Object call() {
             	BlackboardSessionResponse sessionResponse = generateSessionResponse();
                 
                 final Session session = sessionDao.createSession(sessionResponse, "http://www.example.com/session");
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
                 final BlackboardSessionResponse sessionResponse = new BlackboardSessionResponse();
                 sessionResponse.setSessionId(SESSION_ID);
                 sessionResponse.setSessionName("Test Session");
                 sessionResponse.setStartTime(1364566500000l);
                 sessionResponse.setEndTime(1364567400000l);
                 sessionResponse.setCreatorId("admin@example.com");
                 sessionResponse.setBoundaryTime(30);
                 sessionResponse.setAccessType(2);
                 sessionResponse.setRecordings(false);
                sessionResponse.setChairList("admin@example.com,test@example.com");
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
                 
                 final Session session = sessionDao.updateSession(sessionResponse);
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
         final Session session = sessionDao.getSessionByBlackboardId(SESSION_ID);
         assertNotNull(session);
         assertEquals(SESSION_ID, session.getBbSessionId());
         
         final Set<ConferenceUser> sessionChairs = sessionDao.getSessionChairs(session);
         assertNotNull(sessionChairs);
         assertEquals(2, sessionChairs.size());
         
         final Set<ConferenceUser> sessionNonChairs = sessionDao.getSessionNonChairs(session);
         assertNotNull(sessionNonChairs);
         assertEquals(2, sessionNonChairs.size());
     }
 
     private void verifyUpdatedUsers() {
         final ConferenceUser admin = conferenceUserDao.getUser("admin@example.com");
         final Set<Session> adminChaired = conferenceUserDao.getChairedSessionsForUser(admin);
         assertNotNull(adminChaired);
         assertEquals(1, adminChaired.size());
         final Set<Session> adminNonChaired = conferenceUserDao.getNonChairedSessionsForUser(admin);
         assertNotNull(adminNonChaired);
         assertEquals(0, adminNonChaired.size());
         
 
         final ConferenceUser dalquist = conferenceUserDao.getUser("dalquist@example.com");
         final Set<Session> dalquistChaired = conferenceUserDao.getChairedSessionsForUser(dalquist);
         assertNotNull(dalquistChaired);
         assertEquals(0, dalquistChaired.size());
         final Set<Session> dalquistNonChaired = conferenceUserDao.getNonChairedSessionsForUser(dalquist);
         assertNotNull(dalquistNonChaired);
         assertEquals(1, dalquistNonChaired.size());
         
 
         final ConferenceUser levett = conferenceUserDao.getUser("levett@example.com");
         final Set<Session> levettChaired = conferenceUserDao.getChairedSessionsForUser(levett);
         assertNotNull(levettChaired);
         assertEquals(0, levettChaired.size());
         final Set<Session> levettNonChaired = conferenceUserDao.getNonChairedSessionsForUser(levett);
         assertNotNull(levettNonChaired);
         assertEquals(1, levettNonChaired.size());
     }
 
     private void verifyCreatedUsers() {
         final ConferenceUser admin = conferenceUserDao.getUser("admin@example.com");
         final Set<Session> adminChaired = conferenceUserDao.getChairedSessionsForUser(admin);
         assertNotNull(adminChaired);
         assertEquals(1, adminChaired.size());
         final Set<Session> adminNonChaired = conferenceUserDao.getNonChairedSessionsForUser(admin);
         assertNotNull(adminNonChaired);
         assertEquals(0, adminNonChaired.size());
         
 
         final ConferenceUser dalquist = conferenceUserDao.getUser("dalquist@example.com");
         final Set<Session> dalquistChaired = conferenceUserDao.getChairedSessionsForUser(dalquist);
         assertNotNull(dalquistChaired);
         assertEquals(1, dalquistChaired.size());
         final Set<Session> dalquistNonChaired = conferenceUserDao.getNonChairedSessionsForUser(dalquist);
         assertNotNull(dalquistNonChaired);
         assertEquals(0, dalquistNonChaired.size());
         
 
         final ConferenceUser levett = conferenceUserDao.getUser("levett@example.com");
         final Set<Session> levettChaired = conferenceUserDao.getChairedSessionsForUser(levett);
         assertNotNull(levettChaired);
         assertEquals(0, levettChaired.size());
         final Set<Session> levettNonChaired = conferenceUserDao.getNonChairedSessionsForUser(levett);
         assertNotNull(levettNonChaired);
         assertEquals(1, levettNonChaired.size());
     }
 
     private void verifyCreatedSession() {
         final Session session = sessionDao.getSessionByBlackboardId(SESSION_ID);
         assertNotNull(session);
         assertEquals(SESSION_ID, session.getBbSessionId());
         
         final Set<ConferenceUser> sessionChairs = sessionDao.getSessionChairs(session);
         assertNotNull(sessionChairs);
         assertEquals(2, sessionChairs.size());
         
         final Set<ConferenceUser> sessionNonChairs = sessionDao.getSessionNonChairs(session);
         assertNotNull(sessionNonChairs);
         assertEquals(1, sessionNonChairs.size());
     }
     
     private BlackboardSessionResponse generateSessionResponse() {
     	final BlackboardSessionResponse sessionResponse = new BlackboardSessionResponse();
         sessionResponse.setSessionId(SESSION_ID);
         sessionResponse.setSessionName("Test Session");
         sessionResponse.setStartTime(1364566500000l);
         sessionResponse.setEndTime(1364567400000l);
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
         return sessionResponse;
     }
 }
