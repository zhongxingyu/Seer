 package scanner;
 
 import java.util.List;
 
 import com.google.common.collect.Lists;
 
 import htmlscanner.Automaton;
 import htmlscanner.Token;
 import htmlscanner.TokenType;
 import junit.framework.TestCase;
 
 public class ForDemandTests extends AutomatonTests {
 	public void testname() throws Exception {
 		Automaton au = new Automaton();
 		au.init("simpleconf.yaml");
 		au.setInputToScan("12.23 12314 i78sidu 'a' \"hola\"");
 		Token t = null;
 		List<Token> result = Lists.newArrayList(); 
 		while((t=au.getNextToken())!=null){
 			result.add(t);
 		}
 		assert(isEqual(token(TokenType.REAL,"12.23")
 						.and(TokenType.SEP)
 						.and(TokenType.INT,"12314")
 						.and(TokenType.SEP)
 						.and(TokenType.ID)
 						.and(TokenType.SEP)
 						.and(TokenType.CHAR,"a")
 						.and(TokenType.SEP)
 						.and(TokenType.STRING,"hola").build(), result));
 	}
 }
