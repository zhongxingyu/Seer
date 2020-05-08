 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.sohail.online_quizzing_app.tests;
 
 import com.sohail.online_quizzing_app.model.pojo.OptionStructure;
 import com.sohail.online_quizzing_app.model.pojo.QuestionStructure;
 import com.sohail.online_quizzing_app.model.pojo.QuizStructure;
 import java.io.File;
 import java.util.ArrayList;
 import org.simpleframework.xml.Serializer;
 import org.simpleframework.xml.core.Persister;
 
 /**
  *
  * @author SOHAIL
  */
 public class TestDeserialize {
 
     public static void main(String[] args) {
         Serializer serializer = new Persister();
         File source = new File("D:/Quiz.xml");
         QuizStructure quiz = null;
         try {
             quiz = serializer.read(QuizStructure.class, source);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
 
         ArrayList<QuestionStructure> questions = quiz.getQuestions();
        ArrayList<OptionStructure> options = quiz.getOptionList();
 
         System.out.println("Quiz Details:");
         System.out.println("Quiz Subject: " + quiz.getSubject());
         System.out.println("Quiz Topic: " + quiz.getTopic());
         System.out.println("Quiz Description: " + quiz.getDescription());
         System.out.println("Quiz Time Limit: " + quiz.getTimeLimit());
         System.out.println("Quiz Submission Date: " + quiz.getSubmission_date());
         System.out.println("Quiz Due Date: " + quiz.getDue_date());
         System.out.println("Quiz Total Questions in Quiz: " + quiz.getTotal_questions_in_quiz());
         System.out.println("Quiz Total Questions To Solve: " + quiz.getTotal_questions_to_solve());
         System.out.println("---------------------------------------------------");
         for (QuestionStructure questionStructure : questions) {
             System.out.println("Question: " + questionStructure.getQuestion());
             System.out.println("Question Image: " + questionStructure.getQuestionImage());
             System.out.println("Question Difficulty: " + questionStructure.getDifficulty());
             System.out.println("Question UUID: " + questionStructure.getUuid());
             System.out.println("Quiz UUID: " + questionStructure.getUuid_quiz());
             System.out.println("---------------------------------------------------");
             for (OptionStructure optionStructure : options) {
                 System.out.println("Option: " + optionStructure.getOption());
                 System.out.println("Option Image: " + optionStructure.getOptionImage());
                 System.out.println("Option IsCorrectAns: " + optionStructure.isCorrectAns());
                 System.out.println("Option UUID: " + optionStructure.getUuid());
                 System.out.println("Question UUID: " + optionStructure.getUuid_question());
                 System.out.println("Quiz UUID: " + optionStructure.getUuid_quiz());
                 System.out.println("---------------------------------------------------");
             }
         }
     }
 }
