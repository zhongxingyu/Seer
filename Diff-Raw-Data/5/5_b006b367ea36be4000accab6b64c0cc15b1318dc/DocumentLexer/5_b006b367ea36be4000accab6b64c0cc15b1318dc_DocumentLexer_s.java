 package posl.editorkit;
 
 import java.io.InputStream;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import posl.editorkit.DocAttributes.style;
 import posl.engine.api.ILexer;
 import posl.engine.api.IToken;
 import posl.engine.api.TokenVisitor;
 import posl.engine.core.Lexer;
 import posl.engine.core.Parser;
 
 public class DocumentLexer {
 
 	static Logger log = Logger.getLogger(Parser.class.getName());
 
 	private ILexer lexer = null;
 	private List<IToken> tokens = null;
 	private UIVisitor visitor = new UIVisitor();
 
 	public DocumentLexer() {
		lexer = new Lexer();
		tokens = new ArrayList<IToken>();
 	}
 	
 
 	public void tokenize(Reader reader) {
 	    lexer.tokenize(reader);
 	    parse();
 	}
 	
 	private void parse(){
 		while(lexer.hasNext()){
 			IToken next = lexer.next();
 			next.accept(visitor);
 			tokens.add(next);
 		}
 	}
 	
 	public List<IToken> getTokens(){
 		return tokens;
 	}
 
 
 
 	
 
 	private class UIVisitor implements TokenVisitor {
 
 		boolean command = true;
 		IToken comparable = null;
 		DocAttributes attr = new DocAttributes();
 		private Stack<IToken> charStack = new Stack<IToken>();
 
 		@Override
 		public void visitComments(IToken token) {
 			setDoc(token);
 			attr.setStyle(style.COMMENTS);
 		}
 
 		@Override
 		public void visitEol(IToken token) {
 			setDoc(token);
 			command = true;
 		}
 		
 
 		@Override
 		public void visitGrammar(IToken token) {
 			setDoc(token);
 			char ch = token.getString().charAt(0);
 			switch (ch) {
 			case '[':
 				command = true;
 				charStack.push(token);
 				break;
 			case '(':
 				charStack.push(token);
 				break;
 			case '{':
 				command = true;
 				charStack.push(token);
 				break;
 			case ')':
 				comparable = null;
 				if (!charStack.empty()) {
 					comparable = charStack.pop();
 					if (comparable.getString().charAt(0) == '(') {
 						attr.setToken(comparable);
 						((DocAttributes) comparable.getMeta()).setToken(token);
 					}
 				}
 				break;
 			case ']':
 				comparable = null;
 				if (!charStack.empty()) {
 					comparable = charStack.pop();
 					if (comparable.getString().charAt(0) == '[') {
 						attr.setToken(comparable);
 						((DocAttributes) comparable.getMeta()).setToken(token);
 					}
 				}
 				break;
 			case '}':
 				comparable = null;
 				if (!charStack.empty()) {
 					comparable = charStack.pop();
 					if (comparable.getString().charAt(0) == '{') {
 						attr.setToken(comparable);
 						((DocAttributes) comparable.getMeta()).setToken(token);
 					}
 				}
 			}
 			attr.setStyle(style.GRAMMAR);
 		}
 
 		private void setDoc(IToken token) {
 			attr = new DocAttributes();
 			token.setMeta(attr);
 		}
 
 		@Override
 		public void visitIdentifier(IToken token) {
 			setDoc(token);
 			if (command) {
 				attr.setCommand(true);
 				command = false;
 			}
 			attr.setStyle(style.INDENTIFIER);
 		}
 
 		@Override
 		public void visitNumbers(IToken token) {
 			setDoc(token);
 			attr.setStyle(style.NUMBER);
 		}
 
 		@Override
 		public void visitQuote(IToken token) {
 			setDoc(token);
 			attr.setStyle(style.STRING);
 		}
 
 		@Override
 		public void visitWhitespace(IToken token) {
 			setDoc(token);
 		}
 
 	}
 
 
 }
