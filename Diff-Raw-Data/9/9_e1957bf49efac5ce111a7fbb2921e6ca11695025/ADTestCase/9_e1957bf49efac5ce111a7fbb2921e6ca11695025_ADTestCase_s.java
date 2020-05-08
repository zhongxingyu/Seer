 package generated.test137;
 
 import junit.framework.TestCase;
 
 /**
  * Test pointcut references
  *
  * @author Vaidas Gasiunas
  */
 
 public class ADTestCase extends TestCase
 {
 
 	public ADTestCase()
 	{
 		super("test");
 	}
 
 	public static StringBuffer result = new StringBuffer();
 
 	public String expectedResult =
		":before test:test:test:test";
 
 	public void test()
 	{
 		System.out.println("-------> ADTest 37: Aspect Precedence: start");
 		
 		AspectA a = new AspectA();
 		a.simpleDeploy();
 		
 		new Test().test(3);
 		
 		a.simpleUndeploy();
 
 		System.out.println(result.toString());
 		assertEquals(expectedResult, result.toString());
 
 		System.out.println("-------> ADTest 37: end");
 	}
 }
 
 public cclass Test
 {
     public void test(int n)
     {
    	System.out.println(":test");
     	if (n > 0) {
     		test(n-1);
     	}
     }
 }
 
 public cclass AspectA
 {
	pointcut testMeth() : call(* test());
 	
 	pointcut testCls() : target(Test);
 	
 	pointcut testTest() : testMeth() && testCls();
 	
 	pointcut topTest() : testTest() && !cflowbelow(testTest());
 
 	before() : topTest()
 	{
 		ADTestCase.result.append(":before test");
 	}
 }
 
