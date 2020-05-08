 package com.couple.services.impl;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.couple.model.AnswerOption;
 import com.couple.model.Couple;
 import com.couple.model.Result;
 import com.couple.persistence.CoupleDao;
 import com.couple.services.ResultsService;
 import com.couple.web.dto.SurveyAnswers;
 import com.couple.web.dto.SurveyResults;
 
 @Service
 public class ResultsServiceImpl implements ResultsService {
 	private CoupleDao coupleDao;
 	
 	@Autowired
 	public void setCoupleDao(CoupleDao coupleDao) {
 		this.coupleDao = coupleDao;
 	}
 	
 	@Override
 	public long saveAnswers(SurveyAnswers answers) {
 		Couple couple = coupleDao.findForPartners(answers.getUserId(), answers.getPartnerId());
 		if (couple == null) {
 			couple = coupleDao.create(answers.getUserId(), answers.getPartnerId());
 		} else {
 			if (! couple.getAnswersFor(answers.getUserId()).isEmpty()) {
				throw new RuntimeException();
 			}
 		}
 		
 		for(Map.Entry<Long, AnswerOption> pair: answers.getAnswers().entrySet()) {
 			couple.setAnswer(answers.getUserId(), pair.getKey(), pair.getValue());
 		}
 		
 		couple.calculateScore();
 		
 		return couple.getId();
 	}
 	
 	public SurveyResults getSurveyResults(long coupleId, String currentUserId) {
 		Couple couple = coupleDao.find(coupleId);
 		
 		if (!couple.getPartnerIds().contains(currentUserId)) {
 			throw new IllegalArgumentException("The user cannot see results for this couple.");
 		}
 		
 		SurveyResults results;
 		if (couple.getScore() != null) {
 			results = new SurveyResults(couple.getScore());
 		} else {
 			results = new SurveyResults();
 		}
 		
 		
 		Iterator<String> it = couple.getPartnerIds().iterator();
 		String first = it.next();
 		String partnerId = first.equals(currentUserId) ? it.next() : first;
 		results.setMyId(currentUserId);
 		results.setPartnerId(partnerId);
 		
 		return results;
 	}
 	
 	@Override
 	public List<Result> getResults(boolean positive) {
 		List<Result> result = new ArrayList<Result>();
 
 
 		//TODO: load answers and other info for output
 		for (int i=0; i<10; i++) {
 			Result q = new Result();
 			q.setPercent(45+i);
 			q.setQuestion("Вопрос, Question about smth number " + i);
 
 			if (positive && q.isPositive()) {
 				result.add(q);
 			} else if (!positive && !q.isPositive()){
 				result.add(q);
 			}
 		}
 		//*
 
 		return result;
 	}
 }
