 package soot.jimple.infoflow.test.junit;
 
 import static org.junit.Assert.assertTrue;
 
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 
 import soot.jimple.infoflow.Infoflow;
 
 public class StringTests extends JUnitTests {
 	
 	@Test
     public void multipleSourcesTest(){
     	Infoflow infoflow = initInfoflow();
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void multipleSources()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 		checkInfoflow(infoflow, 1);
     }
 	
 	@Test
     public void substringTest(){
     	Infoflow infoflow = initInfoflow();
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodSubstring()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 		checkInfoflow(infoflow, 1);
     }
 	
 	@Test
     public void lowerCaseTest(){
     	Infoflow infoflow = initInfoflow();
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodStringLowerCase()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 		checkInfoflow(infoflow, 1);
     }
 	
 	@Test
     public void upperCaseTest(){
     	Infoflow infoflow = initInfoflow();
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodStringUpperCase()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 		checkInfoflow(infoflow, 1);
     }
 
     @Test
     public void concatTest1(){
     	Infoflow infoflow = initInfoflow();
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodStringConcat1()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 		checkInfoflow(infoflow, 1);
     }
     
     @Test
     public void concatTest1b(){
     	Infoflow infoflow = initInfoflow();
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodStringConcat1b()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 		checkInfoflow(infoflow, 1);
     }
     
     @Test
     public void concatTest1c(){
     	Infoflow infoflow = initInfoflow();
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodStringConcat1c(java.lang.String)>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 		checkInfoflow(infoflow, 1);
     }
     
     @Test
     public void concatTest2(){
     	Infoflow infoflow = initInfoflow();
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodStringConcat2()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 		checkInfoflow(infoflow, 1);
     }
     
     @Test
     public void stringConcatTest(){
 	   Infoflow infoflow = initInfoflow();
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void stringConcatTest()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 		checkInfoflow(infoflow, 1);
     }
    
    @Test
   public void stringConcatTestSmall1(){
 	   Infoflow infoflow = initInfoflow();
    		List<String> epoints = new ArrayList<String>();
   		epoints.add("<soot.jimple.infoflow.test.StringTestCode: void stringConcatTestSmall1()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 		checkInfoflow(infoflow, 1);
    }
    
    @Test
    public void stringConcatTestSmall2(){
 	   Infoflow infoflow = initInfoflow();
    		List<String> epoints = new ArrayList<String>();
    		epoints.add("<soot.jimple.infoflow.test.StringTestCode: void stringConcatTestSmall2()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 		checkInfoflow(infoflow, 1);
    }
     
     @Test
     public void concatTestNegative(){
     	Infoflow infoflow = initInfoflow();
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodStringConcatNegative()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 		negativeCheckInfoflow(infoflow);
     }
     
     @Test
     public void concatPlusTest1(){
     	Infoflow infoflow = initInfoflow();
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodStringConcatPlus1()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 		checkInfoflow(infoflow, 1);
     }
     
     @Test
     public void concatPlusTest2(){
     	Infoflow infoflow = initInfoflow();
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodStringConcatPlus2()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 		checkInfoflow(infoflow, 1);	
     }
     
     @Test
     public void valueOfTest1(){
     	Infoflow infoflow = initInfoflow();
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodValueOf()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 		checkInfoflow(infoflow, 1);
     }
     
     @Test
     public void toStringTest1(){
     	Infoflow infoflow = initInfoflow();
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodtoString()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 		checkInfoflow(infoflow, 1);
     }
     
     @Test
     public void stringBufferTest1(){
     	Infoflow infoflow = initInfoflow();
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodStringBuffer1()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 		checkInfoflow(infoflow, 1);
     }
     
     @Test
     public void stringBufferTest2(){
     	Infoflow infoflow = initInfoflow();
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodStringBuffer2()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 		checkInfoflow(infoflow, 1);
    }
     
   
     @Test
     public void stringBuilderTest1(){
     	Infoflow infoflow = initInfoflow();
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodStringBuilder1()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 		checkInfoflow(infoflow, 1);
     }
     
     @Test
     public void stringBuilderTest2(){
     	Infoflow infoflow = initInfoflow();
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodStringBuilder2()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 		checkInfoflow(infoflow, 1);		
     }
     
     @Test
     public void stringBuilderTest3(){
     	Infoflow infoflow = initInfoflow();
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodStringBuilder3()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 		checkInfoflow(infoflow, 1);	
     }
     
     @Test
     public void stringBuilderTest4(){
     	Infoflow infoflow = initInfoflow();
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodStringBuilder4(java.lang.String)>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 		checkInfoflow(infoflow, 1);	
     }
     
     @Test
     public void stringBuilderTest5(){
     	Infoflow infoflow = initInfoflow();
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodStringBuilder5()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 		checkInfoflow(infoflow, 1);	
     }
 
     @Test
     public void stringBuilderTest6(){
     	Infoflow infoflow = initInfoflow();
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodStringBuilder6()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 		checkInfoflow(infoflow, 1);	
     }
 
     @Test
     public void test133(){
     	Infoflow infoflow = initInfoflow();
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.IndexOutOfBoundsException: void method()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
     }
     
     @Test
     public void testcharArray(){
     	Infoflow infoflow = initInfoflow();
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void getChars()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 		checkInfoflow(infoflow, 1);
     }
     
     @Test
     public void testStringConcat(){
     	Infoflow infoflow = initInfoflow();
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodStringConcat()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 		checkInfoflow(infoflow, 1);
 		assertTrue(infoflow.getResults().isPathBetweenMethods(sink, sourcePwd));
     }
 
     @Test
     public void testPrototyp() throws FileNotFoundException{
     	Infoflow infoflow = initInfoflow();
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void originalFromPrototyp()>");
     	infoflow.computeInfoflow(path, epoints,sources, sinks);
     }
 
 }
