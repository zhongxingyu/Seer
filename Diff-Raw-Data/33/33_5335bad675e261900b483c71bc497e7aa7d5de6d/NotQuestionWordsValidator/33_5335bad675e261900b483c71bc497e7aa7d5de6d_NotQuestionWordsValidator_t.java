 package com.forum.service.validation;
 
 import javax.validation.ConstraintValidator;
 import javax.validation.ConstraintValidatorContext;
 
 /**
  * Created with IntelliJ IDEA.
  * User: wschlegel
  * Date: 07/09/2012
  * Time: 16:43
  * To change this template use File | Settings | File Templates.
  */
 public class NotQuestionWordsValidator implements ConstraintValidator<NotQuestionWords, String> {
 
     private NotQuestionWords notQuestionWords ;
     @Override
     public void initialize(NotQuestionWords notQuestionWords) {
       this.notQuestionWords = notQuestionWords;
     }
 
     @Override
     public boolean isValid(String context, ConstraintValidatorContext constraintValidatorContext) {
        String[] tagWords = context.split(" ");
        for(String word: tagWords){
            if(word.toUpperCase().equals("HOW")) { return false; }
            if(word.toUpperCase().equals("WHY")) { return false; }
            if(word.toUpperCase().equals("WHAT")){ return false; }
            if(word.toUpperCase().equals("WHO")) { return false; }
            if(word.toUpperCase().equals("WHERE")) { return false; }
            if(word.toUpperCase().equals("WHEN")) { return false; }
        }
         return true;
   }
 }
