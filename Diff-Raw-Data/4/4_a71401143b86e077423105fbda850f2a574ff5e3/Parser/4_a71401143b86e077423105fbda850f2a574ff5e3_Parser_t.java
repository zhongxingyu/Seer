 package regexpath;
 import java.text.CharacterIterator;
 import java.text.StringCharacterIterator;
 import java.util.Stack;
 
 //TODO: in the end, check if we reached the end of the string!
 public class Parser {
 	
 	public static final char ESCAPECHAR ='%'; 
 
 	public AST parseNode(String str) {
 		return parseNode(new StringCharacterIterator(str));
 	}
 	
 	AST parseNode(CharacterIterator it) { 
 		AST.Axis axis;
 		switch (it.current()) {
 		case '/':
 			if (it.next() == '/' ) {
 				axis = AST.Axis.Descendant;
 				it.next();
 			} else {
 				axis = AST.Axis.Child;
 			}
 			break;
 		case '@':
 			axis = AST.Axis.Attribute;
 			it.next();
 			break;
 		case '=':
 			if (it.next() == '~' ) {
 				axis = AST.Axis.Match;
 				it.next();
 			} else {
 				axis = AST.Axis.Text;
 			}
 			break;
 		case CharacterIterator.DONE: // EOF
 			return null;
 		default:
			//TODO: throw a proper error message
			throw new RuntimeException("Parsing xpath expression failed");
 		}
 		
 		return new AST
 		( axis
 		, parseRegex(it)
 		, parsePredicates(it)
 		, parseBranches(it)
 		, parseNode(it)
 		);
 	}
 	
 	AST[] parsePredicates(CharacterIterator it) {
 		if (it.current() != '[')
 			return new AST[] {};
 
 		Stack<AST> predicates = new Stack<AST>();
 
 		it.next();
 		predicates.push(parseNode(it));
 		for (;;) {
 			switch (it.current()) {
 			case ',':
 				predicates.push(parseNode(it));
 				break;
 			case ']':
 				return predicates.toArray(new AST[] {});
 			default:
 				return null; //TODO raise exception
 			}
 		}
 	}
 	
 	//TODO: eliminate code duplication with parsePredicates
 	AST[] parseBranches(CharacterIterator it) {
 		if (it.current() != '(')
 			return new AST[] {};
 
 		Stack<AST> predicates = new Stack<AST>();
 
 		it.next();
 		predicates.push(parseNode(it));
 		for (;;) {
 			switch (it.current()) {
 			case ',':
 				it.next();
 				predicates.push(parseNode(it));
 				break;
 			case ')':
 				it.next();
 				return predicates.toArray(new AST[] {});
 			default:
 //				System.out.println(it.previous());
 //				System.out.println(it.next());
 				System.out.println(it.current());
 				return null; //TODO raise exception
 			}
 		}
 	}
 	
 	String parseRegex(CharacterIterator it) {
 		StringBuilder builder = new StringBuilder();
 
 		while (true) {
 			switch (it.current()) {
 			case ESCAPECHAR:
 				// escaped character, append the next without question
 				// provided that it's not the end of the parsed string
 				it.next();
 				assert it.current() != CharacterIterator.DONE;
 				builder.append(it.current());
 				break;
 			
 			// for anything else we immediately stop the loop
 			// and return the string we've parsed
 			case '/':
 			case '@':
 			case '=':
 			case '[':
 			case ']':
 			case '(':
 			case ')':
 			case ',':
 			case CharacterIterator.DONE: // EOF
 				return builder.toString();
 				
 			default:
 				builder.append(it.current());
 			}
 			
 			it.next();
 		}
 	}
 }
