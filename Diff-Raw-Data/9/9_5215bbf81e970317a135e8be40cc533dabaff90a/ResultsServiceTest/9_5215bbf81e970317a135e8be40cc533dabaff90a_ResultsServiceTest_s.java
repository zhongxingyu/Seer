 package com.couple.services.impl;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.util.Arrays;
 import java.util.Collections;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.couple.model.AnswerOption;
 import com.couple.model.Couple;
 import com.couple.persistence.CoupleDao;
 import com.couple.web.dto.SurveyAnswers;
 import com.couple.web.dto.SurveyResults;
 
 public class ResultsServiceTest {
 	private static final String CURRENT_USER_ID = "me";
 	private static final String PARTNER_ID = "partner";
 	private static final long TEST_COUPLE_ID = 42;
 	
 	private Couple couple = mock(Couple.class);
 	
 	private final CoupleDao coupleDao = mock(CoupleDao.class);
 	
 	private SurveyAnswers surveyAnswers;
 	private ResultsServiceImpl resultsService;
 	
 	@Before
 	public void setUp() {
 		surveyAnswers = new SurveyAnswers(CURRENT_USER_ID, PARTNER_ID);
 		
 		when(couple.getId()).thenReturn(TEST_COUPLE_ID);
 		when(couple.getPartnerIds()).thenReturn(Arrays.asList(PARTNER_ID, CURRENT_USER_ID));
 		when(couple.getScore()).thenReturn(42);
 		when(couple.getAnswersFor(surveyAnswers.getUserId())).thenReturn(Collections.<Long, AnswerOption>emptyMap());
 		
 		when(coupleDao.find(TEST_COUPLE_ID)).thenReturn(couple);
 		when(coupleDao.findForPartners(surveyAnswers.getUserId(), surveyAnswers.getPartnerId())).thenReturn(couple);
 		
 		resultsService = new ResultsServiceImpl();
 		resultsService.setCoupleDao(coupleDao);
 	}
 	
 	@Test(expected = IllegalArgumentException.class)
 	public void shouldFailIfWrongUserPassed() {
 		resultsService.getSurveyResults(TEST_COUPLE_ID, "another");
 	}
 	
 	@Test
 	public void shouldDetermineCurrentUser() {
 		SurveyResults surveyResults = resultsService.getSurveyResults(TEST_COUPLE_ID, CURRENT_USER_ID);
 		
 		assertEquals(CURRENT_USER_ID, surveyResults.getMyId());
 		assertEquals(PARTNER_ID, surveyResults.getPartnerId());
 	}
 	
 	@Test
 	public void shouldLoadResultFromDao() {
 		SurveyResults surveyResults = resultsService.getSurveyResults(TEST_COUPLE_ID, CURRENT_USER_ID);
 		
 		assertEquals(couple.getScore().longValue(), surveyResults.getMatchPercentage());
 		assertTrue(surveyResults.isSurveyCompleted());
 	}
 	
 	@Test
 	public void shouldInformAboutNotCompletedSurvey() {
 		when(couple.getScore()).thenReturn(null);
 		
 		SurveyResults surveyResults = resultsService.getSurveyResults(TEST_COUPLE_ID, CURRENT_USER_ID);
 		
 		assertFalse(surveyResults.isSurveyCompleted());
 	}
 	
 	@Test
 	public void shouldTryToFindCouple() {
 		resultsService.saveAnswers(surveyAnswers);
 		
 		verify(coupleDao).findForPartners(surveyAnswers.getUserId(), surveyAnswers.getPartnerId());
 	}
 	
 	@Test
 	public void shouldCreateCoupleIfNecessaty() {
 		when(coupleDao.findForPartners(surveyAnswers.getUserId(), surveyAnswers.getPartnerId())).thenReturn(null);
 		when(coupleDao.create(surveyAnswers.getUserId(), surveyAnswers.getPartnerId())).thenReturn(couple);
 		
 		resultsService.saveAnswers(surveyAnswers);
 		
 		verify(coupleDao).create(surveyAnswers.getUserId(), surveyAnswers.getPartnerId());
 	}
 	
	@Test(expected = Exception.class)
 	public void shouldFailIfSurveyWasAlreadySubmitted() {
 		when(couple.getAnswersFor(surveyAnswers.getUserId())).thenReturn(Collections.singletonMap(1L, AnswerOption.ALWAYS));
 		
 		resultsService.saveAnswers(surveyAnswers);
 	}
 	
 	@Test
 	public void shouldSaveUserAnswers() {
 		surveyAnswers.addAnswer(1, AnswerOption.NEVER);
 		
 		resultsService.saveAnswers(surveyAnswers);
 		
 		verify(couple).setAnswer(surveyAnswers.getUserId(), 1L, AnswerOption.NEVER);
 	}
 	
 	@Test
 	public void shouldReturnCoupleId() {
 		long coupleId = resultsService.saveAnswers(surveyAnswers);
 		
 		assertEquals(couple.getId().longValue(), coupleId);
 	}
 	
 	@Test
 	public void shouldCalculateScoreIfPossible() {
 		resultsService.saveAnswers(surveyAnswers);
 		
 		verify(couple).calculateScore();
 	}
 }
