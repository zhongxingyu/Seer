 package scanner;
 import htmlscanner.Automaton;
 import htmlscanner.Token;
 
 import java.util.Arrays;
 import java.util.List;
 
 import com.google.common.base.Preconditions;
 
 import junit.framework.TestCase;
 
 
 public class AutomatonTests extends TestCase {
 	public void testEmtpy() throws Exception {
 		Automaton au = new Automaton();
 		au.init("simpleconf.yaml");
 		
 		// retorna lista de errores y lista de tokens reconocidos
 		ScanResult result = au.scan("");
 		assertTrue(result.getTokens().isEmpty());
 		assertTrue(result.getErrors().isEmpty());
 	}
 	
 	public void testIdent() throws Exception {
 		
 		Automaton au = new Automaton();
 		au.init("simpleconf.yaml");
 		
 		// retorna lista de errores y lista de tokens reconocidos
 		ScanResult result = au.scan("a10 bbb c001");
 		List<Token> tokens = Arrays.asList(new Token("ID","a10"), new Token("SEP",null),
 				new Token("ID","bbb"), new Token("SEP",null),
 				new Token("ID","c001"));
 		assertTrue(String.format("tokens esperado: %s\ntokens resultado: %s",tokens,result.tokens),isEqual(result.getTokens(),tokens));
 		System.out.println(result.tokens);
 	}
 	
 	public void testInt() throws Exception {
 		
 		Automaton au = new Automaton();
 		au.init("simpleconf.yaml");
 		
 		// retorna lista de errores y lista de tokens reconocidos
 		ScanResult result = au.scan("10 00 101");
 		List<Token> tokens = Arrays.asList(new Token("INT","10"), new Token("SEP",null),
 				new Token("INT","00"), new Token("SEP",null),
 				new Token("INT","101"));
 		assertTrue(String.format("tokens esperado: %s\ntokens resultado: %s",tokens,result.tokens),isEqual(result.getTokens(),tokens));
 		System.out.println(result.tokens);
 	}
 	
 	public void testReal() throws Exception {
 		
 		Automaton au = new Automaton();
 		au.init("simpleconf.yaml");
 		
 		// retorna lista de errores y lista de tokens reconocidos
 		ScanResult result = au.scan("10 0.01 101");
 		List<Token> tokens = Arrays.asList(new Token("INT","10"), new Token("SEP",null),
 				new Token("REAL","0.01"), new Token("SEP",null),
 				new Token("INT","101"));
 		assertTrue(String.format("tokens esperado: %s\ntokens resultado: %s",tokens,result.tokens),isEqual(result.getTokens(),tokens));
 		System.out.println(result.tokens);
 	}
 	
 	public void testIntAndIdent() throws Exception {
 		
 		Automaton au = new Automaton();
 		au.init("simpleconf.yaml");
 		
 		// retorna lista de errores y lista de tokens reconocidos
 		ScanResult result = au.scan("10a");
 		System.out.println(result.tokens);
 		System.out.println(result.errors);
		assertTrue(result.tokens.isEmpty());
		assertTrue(!result.errors.isEmpty());
 		
 	}
 
 	public static boolean isEqual(List<Token> tokens, List<Token> tokens2) {
 		Preconditions.checkNotNull(tokens);
 		Preconditions.checkNotNull(tokens2);
 		
 		if(tokens.isEmpty() && tokens2.isEmpty()){
 			return true;
 		}
 		
 		if(tokens.size() != tokens2.size()){
 			return false;
 		}
 		
 		for(int i = 0; i< tokens.size(); i++){
 			if(!(tokens.get(i).equals(tokens2.get(i)))){
 				return false;
 			}
 		}
 		return true;
 	}
 }
