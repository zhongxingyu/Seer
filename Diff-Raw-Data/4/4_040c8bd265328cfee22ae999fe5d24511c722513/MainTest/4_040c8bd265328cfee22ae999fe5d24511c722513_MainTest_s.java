 package com.schoolquiz.local;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.GenericXmlApplicationContext;
 
 import com.schoolquiz.entity.Answer;
 import com.schoolquiz.entity.Question;
 import com.schoolquiz.entity.QuestionAnswer;
 import com.schoolquiz.entity.QuestionGroup;
 import com.schoolquiz.entity.UserResult;
 import com.schoolquiz.entity.admin.AdminUser;
 import com.schoolquiz.entity.admin.AdminUserSession;
 import com.schoolquiz.entity.admin.decorated.AdminUserSessionSummary;
 import com.schoolquiz.entity.admin.decorated.CustomQuestionGroupResponse;
 import com.schoolquiz.entity.decorated.CheckUserAnswerSummary;
 import com.schoolquiz.entity.decorated.QuestionGroupListSummary;
 import com.schoolquiz.entity.decorated.QuestionListSummary;
 import com.schoolquiz.entity.decorated.QuestionSummary;
 import com.schoolquiz.entity.decorated.UserAnswerSummary;
 import com.schoolquiz.entity.decorated.UserResultSummary;
 import com.schoolquiz.entity.domain.QuestionOutSummary;
 import com.schoolquiz.entity.domain.QuizOutResultSummary;
 import com.schoolquiz.entity.domain.UserResultJsonOutSummary;
 import com.schoolquiz.persistence.AdminDAO;
 import com.schoolquiz.persistence.QuizDAO;
 import com.schoolquiz.service.AdminUserService;
 import com.schoolquiz.service.QuizService;
 
 public class MainTest {
 	
 	public static void main(String[] args){
 		ApplicationContext ctx = new GenericXmlApplicationContext("schoolQuiz-servlet.xml");
 		QuizService quizService = (QuizService) ctx.getBean("quizService");
 		AdminUserService adminUserService = (AdminUserService) ctx.getBean("adminService");
 		
 		AdminUserSessionSummary userSession = adminUserService.checkUser("admin", "nimda");
 //		CustomQuestionGroupResponse result = adminUserService.getGroupsForAdmin(userSession.getAdminUserSession().getSession(), 0, 5);
 //		System.out.println(result);
 		QuizDAO quizDao = (QuizDAO) ctx.getBean("quizDAO");
 		
 		Question question = quizDao.getQuestion(1L);
 		System.out.println(question);
 		
 		Answer answer = quizDao.getAnswer(23L);
 		System.out.println(answer);
 		
 		QuestionAnswer questionAnswer = new QuestionAnswer();
 		questionAnswer.setAnswer(answer);
 		questionAnswer.setQuestion(question);
 		questionAnswer.setRight(true);
 //		questionAnswer.setId(25L);
 		
 //		questionAnswer = quizDao.getQuestionAnswer(16L);
 		
 		System.out.println(questionAnswer);
 		
 		
 //		questionAnswer = quizDao.getQuestionAnswer(17L);
 		
 		System.out.println("questionAnswer from DB - "+questionAnswer);
 		
 		questionAnswer = quizDao.addQuestionAnswer(questionAnswer);
 		System.out.println(questionAnswer);
 		
 //		System.out.println(userSession);
 		
 //		AdminDAO adminDao = (AdminDAO) ctx.getBean("adminUserDAO");
 		
 //		adminDao.saveNewUser("admin", "nimda");
 		
 //		AdminUserSession userSession = adminDao.checkUserCredentials("admin", "nimda");
 		
 //		System.out.println("Logined admin - "+userSession);
 		
 //		QuestionGroupListSummary questionGroups= quizService.getQuestionGroupList("HM827144UCT02PZPSJ6R");
 //		
 //		System.out.println(questionGroups);
 		
 		
 		
 //		UserResultSummary userResult = quizService.createNewUserResult("Denchik", "192.168.0.2");
 //		System.out.println(userResult);
 		
 //		List<Long> answerList = new ArrayList<Long>();
 //		answerList.add(25l);
 //		answerList.add(26l);
 //		answerList.add(27l);
 //		answerList.add(28l);
 //		CheckUserAnswerSummary checkUserAnswer = quizService.checkUserAnswer("7649U568SC2G87HJ0CRS", 4, answerList);
 //		System.out.println("Checked - "+checkUserAnswer);
 		
 //		QuizOutResultSummary quizRes = quizService.getQuizResult("45WYW50V4A50X43K47U1");
 //		System.out.println(quizRes);
 		
 //		QuestionSummary questionSummary = quizService.getRandomQuestion("HM827144UCT02PZPSJ6R");
 //		System.out.println(questionSummary);
 		
 //		QuestionListSummary questionListSummary = quizService.getQuestionsForGroup("HM827144UCT02PZPSJ6R", 1);
 //		System.out.println(questionListSummary);
 		
 //		QuestionOutSummary questionSummary = quizService.getQuestion("HM827144UCT02PZPSJ6R", 5l);
 //		System.out.println("questionSummary - "+questionSummary);
 		
 //		UserResultJsonOutSummary userResultSum = quizService.finishQuiz("W84FJP528T08Y9N6LEKW");
 //		System.out.println("result - "+userResultSum);
 		
 //		QuestionSummary questionSummary = quizService.getQuestion("HM827144UCT02PZPSJ6R", 1);
 //		System.out.println(questionSummary);
 		
 //		List<Long> answerList = new LinkedList<Long>();
 //		answerList.add(12l);
 //		answerList.add(13l);
 //		answerList.add(14l);
 //		CheckUserAnswerSummary userAnswerCheck =  quizService.checkUserAnswer(4,answerList);
 //		System.out.println(userAnswerCheck);
 		
 //		UserAnswerSummary userAnswerSum = quizService.saveUserAnswer(1l, 2l, 1l);
 //		System.out.println(userAnswerSum);
 	}
 
 }
