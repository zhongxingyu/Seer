 package com.blogspot.nhu313.mastermind.colorcode;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.blogspot.nhu313.mastermind.result.ResultCode;
 
 public class ColorCodeComparatorTest {
 	
 	private static final ColorCode[] originalColorCode = new ColorCode[]{ColorCode.BLUE, ColorCode.BLACK};
 
 	private List<ResultCode> resultCode;
 	private ColorCodeComparator comparator;
 	
 	@Before
 	public void setUp(){
 		comparator = new ColorCodeComparator();
 		resultCode = new ArrayList<ResultCode>();
 	}
 	
 	@After
 	public void tearDown(){
 		comparator = null;
 		resultCode = null;
 	}
 	
 	@Test
 	public void testConstructor_WithNullColor(){
 		ColorCode[] colors = null;
 		Assert.assertNull(comparator.isSameColors(colors, colors));
 		Assert.assertNull(comparator.isSameColors(originalColorCode, colors));
 		Assert.assertNull(comparator.isSameColors(colors, originalColorCode));
 	}
 
 	@Test
 	public void testConstructor_WithNoColor(){
 		ColorCode[] colors = new ColorCode[0];
 		Assert.assertNull(comparator.isSameColors(colors, colors));
 	}
 
 	@Test
 	public void testisSameColors_IncorrectSize(){
 		ColorCode[] colors = new ColorCode[]{ColorCode.BLACK};
 		Assert.assertNull(comparator.isSameColors(originalColorCode, colors));		
 	}
 	
 	@Test
 	public void testisSameColors_AllCorrect(){
 		resultCode.add(ResultCode.CORRECT);
 		resultCode.add(ResultCode.CORRECT);
 		final ColorCode[] otherCode = new ColorCode[]{ColorCode.BLUE, ColorCode.BLACK};
 		
 		Assert.assertEquals(resultCode, comparator.isSameColors(originalColorCode, otherCode));
 	}
 	
 	@Test
 	public void testisSameColors_AllIncorrect(){
 		resultCode.add(ResultCode.INCORRECT);
 		resultCode.add(ResultCode.INCORRECT);
 		final ColorCode[] otherCode = new ColorCode[]{ColorCode.RED, ColorCode.RED};
 		
 		Assert.assertEquals(resultCode, comparator.isSameColors(originalColorCode, otherCode));
 	}
 	
 	@Test
 	public void testisSameColors_AllColorCorrectOnly(){
 		resultCode.add(ResultCode.CORRECT_COLOR_ONLY);
 		resultCode.add(ResultCode.CORRECT_COLOR_ONLY);
 		final ColorCode[] otherCode = new ColorCode[]{ColorCode.BLACK, ColorCode.BLUE};
 		
 		Assert.assertEquals(resultCode, comparator.isSameColors(originalColorCode, otherCode));
 	}
 	
 	@Test
 	public void testisSameColors_FirstCodeIncorrect(){
 		resultCode.add(ResultCode.CORRECT);
 		resultCode.add(ResultCode.INCORRECT);
 		final ColorCode[] otherCode = new ColorCode[]{ColorCode.RED, ColorCode.BLACK};
 		
 		Assert.assertEquals(resultCode, comparator.isSameColors(originalColorCode, otherCode));
 	}
 	
 	@Test
 	public void testisSameColors_LastCodeIncorrect(){
 		resultCode.add(ResultCode.CORRECT);
 		resultCode.add(ResultCode.INCORRECT);
 		final ColorCode[] otherCode = new ColorCode[]{ColorCode.BLUE, ColorCode.RED};
 		
 		Assert.assertEquals(resultCode, comparator.isSameColors(originalColorCode, otherCode));
 	}
 	
 	@Test
 	public void testisSameColors_MiddleCodeIncorrect(){
 		resultCode.add(ResultCode.CORRECT);
 		resultCode.add(ResultCode.CORRECT);
 		resultCode.add(ResultCode.INCORRECT);
 		final ColorCode[] originalCode = new ColorCode[]{ColorCode.BLUE, ColorCode.BLUE, ColorCode.BLUE};
 		final ColorCode[] otherCode = new ColorCode[]{ColorCode.BLUE, ColorCode.BLACK, ColorCode.BLUE};
 		
 		Assert.assertEquals(resultCode, comparator.isSameColors(originalCode, otherCode));
 	}
 	
 	@Test
 	public void testisSameColors_TwoIncorrectCode(){
 		resultCode.add(ResultCode.CORRECT);
 		resultCode.add(ResultCode.CORRECT);
 		resultCode.add(ResultCode.INCORRECT);
 		resultCode.add(ResultCode.INCORRECT);
 		final ColorCode[] originalCode = new ColorCode[]{ColorCode.BLUE, ColorCode.BLUE, ColorCode.BLUE, ColorCode.BLUE};
 		final ColorCode[] otherCode = new ColorCode[]{ColorCode.BLUE, ColorCode.BLACK, ColorCode.BLACK, ColorCode.BLUE};
 		
 		Assert.assertEquals(resultCode, comparator.isSameColors(originalCode, otherCode));
 	}
 	
 	@Test
 	public void testisSameColors_WithAllPossibleResult(){
 		resultCode.add(ResultCode.CORRECT);
 		resultCode.add(ResultCode.CORRECT);
 		resultCode.add(ResultCode.CORRECT_COLOR_ONLY);
 		resultCode.add(ResultCode.INCORRECT);
 		final ColorCode[] originalCode = new ColorCode[]{ColorCode.BLUE, ColorCode.GREEN, ColorCode.RED, ColorCode.GREEN};
 		final ColorCode[] otherCode = new ColorCode[]{ColorCode.BLUE, ColorCode.RED, ColorCode.BLACK, ColorCode.GREEN};
 		
 		Assert.assertEquals(resultCode, comparator.isSameColors(originalCode, otherCode));
 	}
 	
 	@Test
 	public void testRemoveSameItemInSamePosition(){
 		List<ColorCode> original = new ArrayList<ColorCode>();
 		List<ColorCode> other = new ArrayList<ColorCode>();
 		
 		int listSize = 3;
		for (int i = 0; i < listSize; i++){
			original.add(ColorCode.BLACK);
			other.add(ColorCode.BLACK);
		}
 		
 		original.add(1, ColorCode.BLUE);
 		other.add(1, ColorCode.GREEN);
 		
 		Assert.assertEquals(listSize + 1, original.size());
 		Assert.assertEquals(listSize + 1, other.size());
 		
 		comparator.removeSameItemInSamePosition(original, other);
 		
 		Assert.assertEquals(1, original.size());
 		Assert.assertEquals(1, other.size());
 	}
 }
