 package $PACKAGE_NAME$;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.List;
 
 import lpg.lpgjavaruntime.IToken;
 import lpg.lpgjavaruntime.Monitor;
 import lpg.lpgjavaruntime.IMessageHandler;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.uide.parser.AstLocator;
 import org.eclipse.uide.parser.IASTNodeLocator;
 import org.eclipse.uide.parser.ILexer;
 import org.eclipse.uide.parser.IParseController;
 import org.eclipse.uide.parser.IParser;
 import org.eclipse.uide.parser.ParseError;
 
 import org.eclipse.core.resources.IMarker;			// SMS 5 May 2006
 import org.eclipse.core.resources.IResource;		// SMS 5 May 2006
 import org.eclipse.core.runtime.CoreException;		// SMS 5 May 2006
 
 import $AST_PKG_NODE$;
 
 public class $CLASS_NAME_PREFIX$ParseController implements IParseController
 {
     private IProject project;
     private String filePath;
     private $PARSER_TYPE$ parser;
     private $LEXER_TYPE$ lexer;
     private $AST_NODE$ currentAst;
 
     private char keywords[][];
     private boolean isKeyword[];
 
 
     // SMS 5 May 2006:
     // Version of initialize method corresponding to change in IParseController
     /**
      * @param filePath		Project-relative path of file
      * @param project		Project that contains the file
      * @param handler		A message handler to receive error messages (or any others)
      * 						from the parser
      */
     public void initialize(String filePath, IProject project, IMessageHandler handler) {
     	this.filePath= filePath;
     	this.project= project;
     	String fullFilePath = project.getLocation().toString() + "/" + filePath;
         createLexerAndParser(fullFilePath);
     	
     	parser.setMessageHandler(handler);
     }
     
     // SMS 5 May 2006:
     // To make this available to users of the controller
     public IProject getProject() { return project; }
     
     
     
     public IParser getParser() { return parser; }
     public ILexer getLexer() { return lexer; }
     public Object getCurrentAst() { return currentAst; }
     public char [][] getKeywords() { return keywords; }
     public boolean isKeyword(int kind) { return isKeyword[kind]; }
     public int getTokenIndexAtCharacter(int offset)
     {
         int index = parser.getParseStream().getTokenIndexAtCharacter(offset);
         return (index < 0 ? -index : index);
     }
     public IToken getTokenAtCharacter(int offset) {
     	return parser.getParseStream().getTokenAtCharacter(offset);
     }											// SMS 3 Oct 2006:  trying new locator
     public IASTNodeLocator getNodeLocator() { return new $CLASS_NAME_PREFIX$ASTNodeLocator(); }	//return new AstLocator(); }
 
     public boolean hasErrors() { return currentAst == null; }
     public List getErrors() { return Collections.singletonList(new ParseError("parse error", null)); }
     
     public $CLASS_NAME_PREFIX$ParseController()
     {
     }
 
     private void createLexerAndParser(String filePath) {
         try {
             lexer = new $LEXER_TYPE$(filePath); // Create the lexer
             parser = new $PARSER_TYPE$(lexer.getLexStream() /*, project*/);  // Create the parser
         } catch (IOException e) {
             throw new Error(e);
         }
     }
 
     class MyMonitor implements Monitor
     {
         IProgressMonitor monitor;
         boolean wasCancelled= false;
         MyMonitor(IProgressMonitor monitor)
         {
             this.monitor = monitor;
         }
         public boolean isCancelled() {
         	if (!wasCancelled)
         		wasCancelled = monitor.isCanceled();
         	return wasCancelled;
         }
     }
     
     /**
      * setFilePath() should be called before calling this method.
      */
     public Object parse(String contents, boolean scanOnly, IProgressMonitor monitor)
     {
     	MyMonitor my_monitor = new MyMonitor(monitor);
     	char[] contentsArray = contents.toCharArray();
 
         lexer.initialize(contentsArray, filePath);
         parser.getParseStream().resetTokenStream();
         
         // SMS 5 May 2006:
         // Clear the problem markers on the file
         // It should be okay to do this here because ...
         // Whoever is doing the parsing should have passed in whatever
         // listener they were interested in having receive messages
         // and presumably in creating whatever annotations or markers
         // those messages require (and is that a good reason?)
         IResource file = project.getFile(filePath);
        try {
         	file.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
         } catch(CoreException e) {
        	System.err.println("JsdivParseController.parse:  caught CoreException while deleting problem markers; continuing to parse regardless");
         }
         
         lexer.lexer(my_monitor, parser.getParseStream()); // Lex the stream to produce the token stream
         if (my_monitor.isCancelled())
         	return currentAst; // TODO currentAst might (probably will) be inconsistent wrt the lex stream now
 
         currentAst = ($AST_NODE$) parser.parser(my_monitor, 0);
         parser.resolve(currentAst);
 
         cacheKeywordsOnce(); // better place/time to do this?
 
         return currentAst;
     }
 
     private void cacheKeywordsOnce() {
         if (keywords == null) {
             String tokenKindNames[] = parser.getParseStream().orderedTerminalSymbols();
             this.isKeyword = new boolean[tokenKindNames.length];
             this.keywords = new char[tokenKindNames.length][];
 
             int [] keywordKinds = lexer.getKeywordKinds();
             for (int i = 1; i < keywordKinds.length; i++)
             {
                 int index = parser.getParseStream().mapKind(keywordKinds[i]);
 
                 isKeyword[index] = true;
                 keywords[index] = parser.getParseStream().orderedTerminalSymbols()[index].toCharArray();
             }
         }
     }
 }
