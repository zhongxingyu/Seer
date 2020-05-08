 package superkidsapplication;
 
 import com.ece.superkids.*;
 import com.ece.superkids.entities.Question;
 import com.ece.superkids.enums.QuestionLevel;
 import java.util.List;
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author Baris, Marc
  */
 public class QuestionBase {
 
   PanelController panelController;
 
   public QuestionBase(PanelController p) {
     panelController = p;
   }
 
   public QuestionPanel createQuestionPanel(QuestionLevel questionLevel, int level){
 
     // Get a question from the database
     FileQuestionDatabase fileQuestionDatabase =  new FileQuestionDatabase();
     Question question = fileQuestionDatabase.getQuestion(questionLevel, level);
     String questionText = question.getQuestion();
     List<String> choices = question.getChoices();
 
     // create qPanel
     QuestionPanel questionPanel = new QuestionPanel();
    String answer = question.getAnswer();
 
     // set the question on qpanel
     questionPanel.setQuestion(questionText);
     questionPanel.setChoices(choices);
    questionPanel.setAnswer(answer);
 
     // return the questionPanel 
     panelController.addPanel(questionPanel);
     return questionPanel;
 
   }
 }
