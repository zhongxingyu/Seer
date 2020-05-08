 package malice_grammar;
 
 import java.io.IOException;
 
 import org.antlr.runtime.ANTLRFileStream;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.TokenStream;
 
 public class MaliceParser {
 	public static void main(String[] args) throws IOException, RecognitionException {
 		
 		String control_eventually = 
 				"eventually (c==0) because" +  '\n' +
 				" opened" +
 					" abs(y) spoke. \n" +
 					" x was a number. \n" +
 					" x was a letter too. \n" +
 					" x had 10 number. \n" +
 					" x became \"string\". \n" +
 					" Alice found x. \n" +
 					" what was x? ." +
 				" closed" +
 				" enough times.";
 		String[] programs = 
 			{
 				"x was a number of 3. " + '\n' +
 				" y had 12 number. \n" +
 				"The looking-glass hatta() " + '\n' +
 				"opened " + '\n' +
 					"The room private() contained a letter " + '\n' +
 					"opened " + '\n' +
 						"Alice found 1. " + '\n' +
 					"closed" + '\n' +
 					"x became 'y'. " + '\n' +
 				"closed " + '\n' +
 				"The room abs(number b) contained a letter " + '\n' +
 				"opened " + '\n' +
 					"b spoke. " + '\n' +
 				"closed " + '\n'
 			,
 				"x was a letter of 'c'. \n" +
 				"The looking-glass hatta() " + '\n' +
 				"opened " + '\n' +
 					"The room private() contained a letter " + '\n' +
 					"opened " + '\n' +
 						"Alice found abs(x) and " + '\n' +
 						"abs(y) spoke. " + '\n' + 
 					"closed" + '\n' +
 					"x became 'y'. " + '\n' +
 				"closed " + '\n' +
 				"The room abs(number b) contained a letter " + '\n' +
 				"opened " + '\n' +
 					"b spoke. " + '\n' +
 				"closed " + '\n'
 			,
 				"x was a letter of 'c'. \n" +
 				"The looking-glass hatta() " + '\n' +
 				"opened " + '\n' +
 					"perhaps (x<2) so \n" +
 						"c spoke. \n" +
 					"maybe (x<1) so \n" +
 						"y spoke. \n" +
 					"or \n" +
 						"c spoke. \n"+
 					"because Alice was unsure which. \n" +
 					control_eventually +
 				"closed " + '\n' +
 				"The room abs(number b) contained a letter " + '\n' +
 				"opened " + '\n' +
 					"b spoke. " + '\n' +
 				"closed " + '\n'
 			};
 		/*
 		CharStream input = new ANTLRStringStream("result==3"); 
 		malice_grammarLexer lexer = new malice_grammarLexer(input );
 		TokenStream tokens = new CommonTokenStream(lexer);
 		malice_grammarParser parser = new malice_grammarParser(tokens ) ;
 		parser.expr();
 		System.out.println("done");
 		*/
 		/*
 		int i = 0 ;
 		for (String p: programs) {
 			i++;
 			CharStream input = new ANTLRStringStream(p); 
 			malice_grammarLexer lexer = new malice_grammarLexer(input );
 			TokenStream tokens = new CommonTokenStream(lexer);
 			malice_grammarParser parser = new malice_grammarParser(tokens ) ;
 			malice_grammarParser.program_return tree = parser.program() ;
 			System.out.println(tree.toString());
 			System.out.println("done program " + i + "...");
 		}*/
 		
 		
 		String[] ps = ("ackermann.alice binarySearch.alice bubbleSort.alice fibonacciIterative.alice " +
 				"fibonacciRecursive.alice gcdIterative.alice gcdRecursive.alice " +
 				"sieveOfEratosthenes.alice test01.alice test02.alice test03.alice " +
 				"test04.alice test05.alice test06.alice test07.alice test08.alice test09.alice " +
 				"test10.alice test11.alice test12.alice test13.alice test14.alice test15.alice vectorFunctions.alice").split(" ") ;
 		for(String p: ps) {
 			System.out.println(p + " started");
			CharStream input = new ANTLRFileStream("/homes/vv311/Malice/malice_examples/valid/" + p); 
 			malice_grammarLexer lexer = new malice_grammarLexer(input );
 			TokenStream tokens = new CommonTokenStream(lexer);
 			malice_grammarParser parser = new malice_grammarParser(tokens ) ;
			parser.program() ;
 			System.out.println(p + " done");
 		}
 		
 	}
 }
