 package com.operativus.senacrs.audit.exceptions;
 
import com.operativus.senacrs.audit.model.AbstractSequenceStringFieldComparable;
 import com.operativus.senacrs.audit.model.EvaluationGrade;
 
 
 @SuppressWarnings("serial")
 public class MismatchingEvaluationType extends IllegalArgumentException {
 	
 	
	public MismatchingEvaluationType(AbstractSequenceStringFieldComparable activity, EvaluationGrade grade) {
 		
 		// TODO Auto-generated constructor stub
 	}
 }
