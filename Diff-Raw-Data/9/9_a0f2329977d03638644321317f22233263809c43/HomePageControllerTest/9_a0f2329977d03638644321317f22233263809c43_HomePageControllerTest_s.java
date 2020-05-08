 package com.forum.controller;
 
import com.forum.repository.Question;
 import com.forum.repository.ShowQuestions;
 import org.hamcrest.core.IsEqual;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.util.ArrayList;
 
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 public class HomePageControllerTest extends BaseController {
     private ShowQuestions mockShowQuestions;
     public static final int QUESTIONS_PER_PAGE = 5;
     private HomePageController homePageController;
     @Before
     public void setUp() throws Exception {
         mockHttpServletRequest.setRequestURI("/home");
         mockHttpServletRequest.setMethod("GET");
         homePageController = new HomePageController();
         mockShowQuestions = (ShowQuestions) createMock(homePageController, "showQuestions", ShowQuestions.class);
     }
 
     @Test
     public void shouldDirectToHomePage() throws Exception {
         ArrayList<Question> questions = getQuestionsForHomePage();
         when(mockShowQuestions.show(1, QUESTIONS_PER_PAGE)).thenReturn(questions);
         ModelAndView modelAndView = handlerAdapter.handle(mockHttpServletRequest, mockHttpServletResponse, homePageController);
         verify(mockShowQuestions).show(1, QUESTIONS_PER_PAGE);
         assertThat((ArrayList<Question>) modelAndView.getModel().get("questionList"), IsEqual.equalTo(questions));
         assertThat(modelAndView.getViewName(), IsEqual.equalTo("home"));
     }
 
     private ArrayList<Question> getQuestionsForHomePage() {
         ArrayList<Question> questions = new ArrayList<Question>();
         questions.add(new Question("1", "what is nano", "12", "Anil"));
         return questions;
     }
 }
