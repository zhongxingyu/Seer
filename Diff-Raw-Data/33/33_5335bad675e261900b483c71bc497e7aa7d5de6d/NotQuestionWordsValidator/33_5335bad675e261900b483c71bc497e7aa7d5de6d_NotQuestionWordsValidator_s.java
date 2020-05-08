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
        if(context.toUpperCase().contains("HOW")) { return false; }
        if(context.toUpperCase().contains("WHY")) { return false; }
        if(context.toUpperCase().contains("WHAT")){ return false; }
        if(context.toUpperCase().contains("WHO")) { return false; }
         return true;
   }
 }
