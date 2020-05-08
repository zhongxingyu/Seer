 package me.tehbeard.utils.testSuite.expressions;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import me.tehbeard.utils.expressions.InFixExpressionV2;
 import me.tehbeard.utils.expressions.VariableProvider;
 import me.tehbeard.utils.expressions.functions.FunctionCatalogue;
 import me.tehbeard.utils.expressions.functions.FunctionProvider;
 import me.tehbeard.utils.expressions.functions.built.AvgFunction;
 import me.tehbeard.utils.expressions.functions.built.ModFunction;
 import me.tehbeard.utils.expressions.functions.built.SumFunction;
 
 public class ExpressionTest {
 
     @Test
     public void TestComplex(){
 
         String sub = "@avg(8,@sum(1,2,3)) + @mod(22,12)";
         InFixExpressionV2 i = new InFixExpressionV2(sub);
 
         FunctionProvider funcProvider = new FunctionCatalogue(
                 new AvgFunction(),
                 new SumFunction(),
                 new ModFunction()
                 );
         assertEquals("Maths works",17,i.getValue(null, funcProvider ));
 
     }
     
     @Test
     public void TestProvider(){
     	VariableProvider provider = new VariableProvider() {
 			
 			public int resolveVariable(String var) {
 				if(var.equals("a")){return 1;}
 				if(var.equals("b")){return 2;}
 				if(var.equals("c")){return 3;}
 				if(var.equals("d")){return 4;}
 				return 0;
 			}
 			
 			public int[] resolveReference(String var) {
 				
 				if(var.equals("a")){return new int[]{1,2,6,9,32};}
 				if(var.equals("b")){return new int[]{2,6};}
 				
 				return new int[0];
 			}
 		};
 		
		String expr = "@avg(#b)";
 		InFixExpressionV2 i = new InFixExpressionV2(expr);
 		assertEquals("reference test",29,i.getValue(provider, new AvgFunction()));
     }
 }
