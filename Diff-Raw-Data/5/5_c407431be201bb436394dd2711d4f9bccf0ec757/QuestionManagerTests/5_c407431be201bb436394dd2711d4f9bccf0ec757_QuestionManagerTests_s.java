 package com.ece.superkids.testing;
 
 import com.ece.superkids.questions.QuestionDatabase;
 import com.ece.superkids.questions.QuestionDatabaseFactory;
 import com.ece.superkids.questions.QuestionManager;
 import com.ece.superkids.questions.builders.QuestionBuilder;
 import com.ece.superkids.questions.entities.Question;
 import com.ece.superkids.questions.enums.QuestionCategory;
 import com.ece.superkids.questions.enums.QuestionLevel;
 import com.ece.superkids.questions.enums.QuestionMode;
 import com.ece.superkids.questions.enums.QuestionType;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import static junit.framework.Assert.assertEquals;
 
 public class QuestionManagerTests {
 
     private QuestionDatabase questionDatabase;
     private QuestionManager questionManager;
     private Question question;
     private int oldNumberOfQuestions;
 
 
     @Before
     public void setup() {
         questionDatabase = QuestionDatabaseFactory.aQuestionDatabaseWithOnlyCustomQuestions();
         questionManager = QuestionDatabaseFactory.aQuestionManager();
         question = QuestionBuilder.aQuestion()
                 .asking("What has four sides?")
                 .ofType(QuestionType.TEXT)
                 .withChoices("Square", "Circle", "Triangle", "Oval")
                 .withAnswer("Square")
                 .withExplaination("A square has four equal sides")
                 .ofLevel(QuestionLevel.LEVEL_1)
                 .inCategory(QuestionCategory.SHAPES)
                 .build();
         oldNumberOfQuestions = questionDatabase.getNumberOfQuestions(question.getLevel());
     }
 
     @After
     public void cleanup() {
         questionDatabase = QuestionDatabaseFactory.aQuestionDatabaseWithOnlyCustomQuestions();
         int numberToDelete = questionDatabase.getNumberOfQuestions(question.getLevel()) - oldNumberOfQuestions;
         for (int ii = 0; ii < numberToDelete; ii++) {
             Question questionToDelete = questionDatabase.getQuestion(question.getLevel(), question.getCategory(),
                     oldNumberOfQuestions + ii);
             questionManager.deleteQuestion(questionToDelete);
         }
 
     }
 
     @Test
     public void canAddQuestion() {
         questionManager.addQuestion(question);
         questionDatabase.switchMode(QuestionMode.CUSTOM_ONLY);
         int number = questionDatabase.getNumberOfQuestions(question.getLevel()) ;
         Question loadedQuestion = questionDatabase.getQuestion(question.getLevel(), question.getCategory(), number-1);
         assertEquals(oldNumberOfQuestions+1, number);
         assertEquals("Questions do not match", question, loadedQuestion);
     }

     @Test
     public void canDeleteQuestion() {
         canAddQuestion();
         questionManager.deleteQuestion(question);
         questionDatabase = QuestionDatabaseFactory.aQuestionDatabaseWithOnlyCustomQuestions();
         int number = questionDatabase.getNumberOfQuestions(question.getLevel());
         assertEquals(oldNumberOfQuestions, number);
     }

     @Test
     public void canEditQuestion() {
         final String newQuestionString = "HELLO THIS IS ME";
 
         questionManager.addQuestion(question);
         questionDatabase = QuestionDatabaseFactory.aQuestionDatabaseWithOnlyCustomQuestions();
         Question newQuestion = QuestionBuilder.aQuestion().copiedFrom(question).build();
         newQuestion.setQuestion(newQuestionString);
         questionManager.editQuestion(question, newQuestion);
         questionDatabase = QuestionDatabaseFactory.aQuestionDatabaseWithOnlyCustomQuestions();
 
         int number = questionDatabase.getNumberOfQuestions(question.getLevel()) - 1;
         Question loadedQuestion = questionDatabase.getQuestion(question.getLevel(), question.getCategory(), number);
 
         assertEquals("Question not edited", newQuestion, loadedQuestion);
     }
 }
