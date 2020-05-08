 package com.operativus.senacrs.audit.model;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.operativus.senacrs.audit.exceptions.InvalidEvaluationType;
 import com.operativus.senacrs.audit.exceptions.MismatchingEvaluationType;
 import com.operativus.senacrs.audit.testutils.TestBoilerplateUtils;
 
 public class StudentEvaluationTest {
 
 	@Test
	public void testPutGradeMismatchNullGrade() {

		StudentEvaluation obj = null;
		EvaluationActivity activity = null;

		obj = getBaseline();
		activity = getEvalActivity(EvaluationType.SENAC_LEVEL);
		try {
			obj.putGrade(activity, null);
			Assert.fail();
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(true);
		}

	}

	@Test
	public void testPutGradeMismatchNullActivity() {

		StudentEvaluation obj = null;
		EvaluationGrade grade = null;

		obj = getBaseline();
		grade = getEvalGrade(EvaluationType.SENAC_LEVEL);
		try {
			obj.putGrade(null, grade);
			Assert.fail();
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(true);
		}

	}

	@Test
 	public void testPutGradeMismatch() {
 
 		StudentEvaluation obj = null;
 		EvaluationActivity activity = null;
 		EvaluationGrade grade = null;
 
 		obj = getBaseline();
 		activity = getEvalActivity(EvaluationType.SENAC_LEVEL);
 		grade = getEvalGrade(EvaluationType.NUMERIC);
 		try {
 			obj.putGrade(activity, grade);
 			Assert.fail();
 		} catch (MismatchingEvaluationType e) {
 			Assert.assertTrue(true);
 		}
 
 	}
 
 	private StudentEvaluation getBaseline() {
 
 		return new StudentEvaluation(TestBoilerplateUtils.randomInt(100), TestBoilerplateUtils.randomString());
 	}
 
 	private EvaluationActivity getEvalActivity(EvaluationType type) {
 
 		return new EvaluationActivity(0, type, null, null);
 	}
 
 	private EvaluationGrade getEvalGrade(final EvaluationType type) {
 
 		return new EvaluationGrade() {
 
 			@Override
 			public EvaluationType getType() {
 
 				return type;
 			}
 
 			@Override
 			public void fromString(String str) {
 
 			}
 		};
 	}
 
 	@Test
 	public void testPutGradeInvalidGrade() {
 
 		StudentEvaluation obj = null;
 		EvaluationActivity activity = null;
 		EvaluationGrade grade = null;
 
 		obj = getBaseline();
 		activity = getEvalActivity(EvaluationType.SENAC_LEVEL);
 		grade = getEvalGrade(EvaluationType.INVALID);
 		try {
 			obj.putGrade(activity, grade);
 			Assert.fail();
 		} catch (InvalidEvaluationType e) {
 			Assert.assertTrue(true);
 		}
 
 	}
 
 	@Test
 	public void testPutGradeInvalidActivity() {
 
 		StudentEvaluation obj = null;
 		EvaluationActivity activity = null;
 		EvaluationGrade grade = null;
 
 		obj = getBaseline();
 		activity = getEvalActivity(EvaluationType.INVALID);
 		grade = getEvalGrade(EvaluationType.SENAC_LEVEL);
 		try {
 			obj.putGrade(activity, grade);
 			Assert.fail();
 		} catch (InvalidEvaluationType e) {
 			Assert.assertTrue(true);
 		}
 
 	}
 
 	@Test
 	public void testPutGrade() {
 
 		StudentEvaluation obj = null;
 		EvaluationActivity activity = null;
 		EvaluationGrade grade = null;
 		EvaluationGrade result = null;
 
 		obj = getBaseline();
 		activity = getEvalActivity(EvaluationType.SENAC_LEVEL);
 		grade = getEvalGrade(EvaluationType.SENAC_LEVEL);
 		result = obj.putGrade(activity, grade);
 		Assert.assertNull(result);
 	}
 
 	@Test
 	public void testPutGradeSameActivity() {
 
 		StudentEvaluation obj = null;
 		EvaluationActivity activity = null;
 		EvaluationGrade expected = null;
 		EvaluationGrade grade = null;
 		EvaluationGrade result = null;
 
 		obj = getBaseline();
 		activity = getEvalActivity(EvaluationType.SENAC_LEVEL);
 		expected = getEvalGrade(EvaluationType.SENAC_LEVEL);
 		result = obj.putGrade(activity, expected);
 		grade = getEvalGrade(EvaluationType.SENAC_LEVEL);
 		result = obj.putGrade(activity, grade);
 		Assert.assertEquals(expected, result);
 		Assert.assertNotEquals(grade, result);
 	}
 }
