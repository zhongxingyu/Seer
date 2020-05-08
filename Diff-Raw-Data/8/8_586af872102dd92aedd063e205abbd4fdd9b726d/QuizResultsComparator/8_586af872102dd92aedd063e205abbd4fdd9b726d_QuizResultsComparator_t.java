 package com.blogspot.jmelon.portlet.quiz.model.utilities;
 
 import java.util.Comparator;
 
 import com.blogspot.jmelon.portlet.quiz.model.QuizResult;
 
 /**
  * See the file "LICENSE" for the full license governing this code.
  * @author <a href="jmelon.blogspot.com">Michał Mela</a>
  */
 public class QuizResultsComparator implements Comparator<QuizResult> {
 
 	@Override
     public int compare(QuizResult o1, QuizResult o2) {
	    return ((Integer)(o1.getBound())).compareTo(o2.getBound());
     }
 	
 }
