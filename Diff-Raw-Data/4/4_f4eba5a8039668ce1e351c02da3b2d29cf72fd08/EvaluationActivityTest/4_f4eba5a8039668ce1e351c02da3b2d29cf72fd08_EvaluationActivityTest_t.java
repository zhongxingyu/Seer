 package com.operativus.senacrs.audit.model;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.operativus.senacrs.audit.testutils.TestBoilerplateUtils;
 
 
 public class EvaluationActivityTest {
 	
 	@Test
 	public void testCompareToNull() {
 		
 		EvaluationActivity o1 = null;
 		int result = 0;
 		boolean expected = false;
 		TestBoilerplateUtils.NumericComparisonToZero what = null;
 		String msg = null;
 		
 		o1 = getBaselineObject(0);
		result = o1.compareTo(null);
 		what = TestBoilerplateUtils.NumericComparisonToZero.HIGHER;
 		expected = TestBoilerplateUtils.compare(result, what);
 		msg = TestBoilerplateUtils.errorNumericComparisonToZeroMsg(result, what);
 		Assert.assertTrue(msg, expected);
 	}
 
 	private EvaluationActivity getBaselineObject(int sequence) {
 
 		return new EvaluationActivity(sequence, EvaluationType.SENAC_LEVEL, null, null);
 	}
 
 	@Test
 	public void testCompareToSelf() {
 		
 		EvaluationActivity o1 = null;
 		int result = 0;
 		boolean expected = false;
 		TestBoilerplateUtils.NumericComparisonToZero what = null;
 		String msg = null;
 		
 		o1 = getBaselineObject(0);
 		result = o1.compareTo(o1);
 		what = TestBoilerplateUtils.NumericComparisonToZero.EQUAL;
 		expected = TestBoilerplateUtils.compare(result, what);
 		msg = TestBoilerplateUtils.errorNumericComparisonToZeroMsg(result, what);
 		Assert.assertTrue(msg, expected);
 	}
 
 	@Test
 	public void testCompareToHigherSeq() {
 		
 		EvaluationActivity o1 = null;
 		EvaluationActivity o2 = null;
 		int result = 0;
 		boolean expected = false;
 		TestBoilerplateUtils.NumericComparisonToZero what = null;
 		String msg = null;
 		
 		o1 = getBaselineObject(0);
 		o2 = getBaselineObject(1);
 		result = o1.compareTo(o2);
 		what = TestBoilerplateUtils.NumericComparisonToZero.LOWER;
 		expected = TestBoilerplateUtils.compare(result, what);
 		msg = TestBoilerplateUtils.errorNumericComparisonToZeroMsg(result, what);
 		Assert.assertTrue(msg, expected);
 	}
 
 	@Test
 	public void testCompareToLowerSeq() {
 		
 		EvaluationActivity o1 = null;
 		EvaluationActivity o2 = null;
 		int result = 0;
 		boolean expected = false;
 		TestBoilerplateUtils.NumericComparisonToZero what = null;
 		String msg = null;
 		
 		o1 = getBaselineObject(0);
 		o2 = getBaselineObject(-1);
 		result = o1.compareTo(o2);
 		what = TestBoilerplateUtils.NumericComparisonToZero.HIGHER;
 		expected = TestBoilerplateUtils.compare(result, what);
 		msg = TestBoilerplateUtils.errorNumericComparisonToZeroMsg(result, what);
 		Assert.assertTrue(msg, expected);
 	}
 
 	@Test
 	public void testCompareToSameSeqDiffNames() {
 		
 		EvaluationActivity o1 = null;
 		EvaluationActivity o2 = null;
 		int result = 0;
 		int expected = 0;		
 		
 		o1 = getBaselineObject(0);
 		o2 = getBaselineObject(0);
 		o1.setName(TestBoilerplateUtils.randomString());
 		o2.setName(TestBoilerplateUtils.randomString());
 		result = o1.compareTo(o2);
 		expected = o1.getName().compareTo(o2.getName());
 		Assert.assertEquals(expected, result);
 	}
 	
 }
 
