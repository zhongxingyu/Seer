 package chameleon.support.input;
 
 import java.util.List;
 
 import org.antlr.runtime.CommonToken;
 import org.antlr.runtime.Parser;
 import org.antlr.runtime.ParserRuleReturnScope;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.RecognizerSharedState;
 import org.antlr.runtime.Token;
 import org.antlr.runtime.TokenStream;
 
 import chameleon.core.compilationunit.CompilationUnit;
 import chameleon.core.element.Element;
 import chameleon.core.language.Language;
 import chameleon.core.namespace.Namespace;
 import chameleon.core.namespace.RootNamespace;
 import chameleon.input.InputProcessor;
 
 
 public abstract class ChameleonParser<L extends Language> extends Parser {
 	
 	 public ChameleonParser(TokenStream input, RecognizerSharedState state) {
 		super(input,state);
 	}
 
 	public abstract Object compilationUnit() throws RecognitionException;
 
 	   public void check_null(Object o) {
//	     if(o == null) {
//	       throw new RuntimeException("Object returned by parsing rule is null.");
//	     }
 	   }
 	   
 	   public void check_stack(Object s) {
 	    if(s == null) {
 	      throw new RuntimeException("The stack element is null.");
 	    }
 	   }
 
 	   public void setLocation(Element element, Token start, Token stop) {
 	     CommonToken begin = (CommonToken)start;
 	     CommonToken end = (CommonToken)stop;
 	         if(begin != null && end != null) {
 	         	int offset = begin.getStartIndex();
 	         	int length = end.getStopIndex() - offset;
 	         	for(InputProcessor processor: inputProcessors()) {
 	         		//processor.setLocation(element, new Position2D(begin.getLine(), begin.getCharPositionInLine()), new Position2D(end.getLine(), end.getCharPositionInLine()));
 	         		processor.setLocation(element, offset, length, getCompilationUnit());
 	         	}
 	         }
 	   }
 
 		public List<InputProcessor> inputProcessors() {
 			return language().processors(InputProcessor.class);
 		}
 
 	   public void setLocation(Element element, Token start, Token stop, String tagType) {
 	     List<InputProcessor> processors = inputProcessors();
 	     CommonToken begin = (CommonToken)start;
 	     CommonToken end = (CommonToken)stop;
 	         if(begin != null && end != null) {
 	         	int offset = offset(begin);
 	         	int length = length(begin,end);
 	         	for(InputProcessor processor: processors) {
 	         		//processor.setLocation(element, new Position2D(begin.getLine(), begin.getCharPositionInLine()), new Position2D(end.getLine(), end.getCharPositionInLine()));
 	         		processor.setLocation(element, offset, length, getCompilationUnit(), tagType);
 	         	}
 	         }
 	   }
 	   
 	   public int offset(CommonToken token) {
 	  	 return token.getStartIndex();
 	   }
 	   
 	   public int length(CommonToken start, CommonToken stop) {
 	  	 return stop.getStopIndex() - offset(start);
 	   }
 	   
 	   /**
 	    * Add locations to the given element
 	    * @param element
 	    * @param first
 	    * @param second
 	    */
 	   public void setLocation(Element element, ParserRuleReturnScope first, ParserRuleReturnScope second) {
 	     Token end = first.stop;
 	     if(second != null) {
 	       end = second.stop;
 	     }
 	     setLocation(element, first.start, end);
 	   }
 	   
 	   
 	   public void setLocation(Element element, Token token, String tagType) {
 	     if(token != null) {
 	       setLocation(element, (CommonToken)token, (CommonToken)token, tagType);
 	     }
 	   }
 	   
 	   public void setKeyword(Element element, Token token) {
 	     if(token != null) {
 	       setLocation(element, (CommonToken)token, (CommonToken)token, "__KEYWORD");
 	     }
 	   }
 
 	   public void setAllLocation(Element element, Token token) {
 	     if(token != null) {
 	       setLocation(element, (CommonToken)token, (CommonToken)token, "__ALL");
 	     }
 	   }
 
 	   Language _lang;
 	   
 	   public Language language() {
 	     return _lang;
 	   }
 	   
 	   public void setLanguage(L language) {
 	     _lang = language;
 	     _root = _lang.defaultNamespace();
 	   }
 	   
 	   RootNamespace _root;
 
 	   public Namespace getDefaultNamespace() {
 	     return _root;
 	   }
 
 	   CompilationUnit _cu = new CompilationUnit();
 	   
 	   public CompilationUnit getCompilationUnit() {
 	     return _cu;
 	   }
 	   
 	   public void setCompilationUnit(CompilationUnit compilationUnit) {
 	    if(compilationUnit == null) {
 	      throw new IllegalArgumentException("The compilation unit cannot be null.");
 	    }
 	     _cu = compilationUnit;
 	   }
 	   
 	   @Override
 	   public void displayRecognitionError(String[] tokenNames, RecognitionException exc) {
 	   	 CommonToken token = (CommonToken) exc.token;
 	   	 int offset = offset(token);
 	   	 int length = length(token,token);
 	   	 // Message construction copied from the super method. It is not available as a separate inspector.
 	   	 String message = getErrorMessage(exc, tokenNames);
        for(InputProcessor processor: inputProcessors()) {
       	 processor.markParseError(offset, length, message, getCompilationUnit());
        }
 	   }
 }
