 package com.operativus.senacrs.audit.model;
 
 import java.util.Random;
 
 import org.apache.commons.lang3.RandomStringUtils;
 import org.junit.Assert;
 import org.junit.Test;
 
 
 public class EvaluationActivityTest {
 	
 	private static Random RAND = new Random(System.currentTimeMillis());
 	private static int MIN_RAND = 10;
 	private static int MAX_RAND = 100;
 	
 	@Test
 	public void testCompareToNull() {
 		
 		EvaluationActivity o1 = null;
 		EvaluationActivity o2 = null;
 		int result = 0;
 		boolean expected = false;
 		String msg = null;
 		
 		o1 = getBaselineObject(0);
 		result = o1.compareTo(o2);
 		expected = result > 0;
 		msg = errorMsg(result, expected);
 		Assert.assertTrue(msg, expected);
 	}
 
 	private EvaluationActivity getBaselineObject(int sequence) {
 
		EvaluationActivity o1;
		o1 = new EvaluationActivity(0, EvaluationType.SENAC_LEVEL, null, null);
		return o1;
 	}
 
 	private String errorMsg(int result, boolean expected) {
 
 		return String.format("Returned: %s and expected %s", String.valueOf(result), String.valueOf(expected));
 	}
 	@Test
 	public void testCompareToSelf() {
 		
 		EvaluationActivity o1 = null;
 		int result = 0;
 		boolean expected = false;
 		String msg = null;
 		
 		o1 = getBaselineObject(0);
 		result = o1.compareTo(o1);
 		expected = result == 0;
 		msg = errorMsg(result, expected);
 		Assert.assertTrue(msg, expected);
 	}
 
 	@Test
 	public void testCompareToHigherSeq() {
 		
 		EvaluationActivity o1 = null;
 		EvaluationActivity o2 = null;
 		int result = 0;
 		boolean expected = false;
 		String msg = null;
 		
 		o1 = getBaselineObject(0);
 		o2 = getBaselineObject(1);
 		result = o1.compareTo(o2);
 		expected = result < 0;
 		msg = errorMsg(result, expected);
 		Assert.assertTrue(msg, expected);
 	}
 
 	@Test
 	public void testCompareToLowerSeq() {
 		
 		EvaluationActivity o1 = null;
 		EvaluationActivity o2 = null;
 		int result = 0;
 		boolean expected = false;
 		String msg = null;
 		
 		o1 = getBaselineObject(0);
 		o2 = getBaselineObject(-1);
 		result = o1.compareTo(o2);
 		expected = result > 0;
 		msg = errorMsg(result, expected);
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
 		o1.setName(randomString());
 		o2.setName(randomString());
 		result = o1.compareTo(o2);
 		expected = o1.getName().compareTo(o2.getName());
 		Assert.assertEquals(expected, result);
 	}
 	
 	private String randomString() {
 		
 		String result = null;
 		int amount = 0;
 		
 		amount = MIN_RAND + RAND.nextInt(MAX_RAND - MIN_RAND);
 		result = RandomStringUtils.random(amount);
 		
 		return result;
 	}
 	
 }
 
