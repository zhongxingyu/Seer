 /*
  * Created on Oct 28, 2005
  */
 package org.jikespg.uide.parser;
 
 import java.util.Collections;
 import java.util.List;
 import lpg.lpgjavaruntime.IToken;
 import lpg.lpgjavaruntime.Monitor;
import lpg.lpgjavaruntime.IMessageHandler;

 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.uide.parser.IASTNodeLocator;
 import org.eclipse.uide.parser.ILexer;
 import org.eclipse.uide.parser.IParseController;
 import org.eclipse.uide.parser.IParser;
 import org.eclipse.uide.parser.ParseError;
 import org.jikespg.uide.parser.JikesPGParser.ASTNode;
 
 public class ParseController implements IParseController {
     private String filePath;
     private IProject project;
     private JikesPGParser parser;
     private JikesPGLexer lexer;
     private ASTNode currentAst;
     private char keywords[][];
     private boolean isKeyword[];
 
     public void initialize(String filePath, IProject project, IMessageHandler handler) {
 	this.filePath= filePath;
 	this.project= project;
 //	parser.setMessageHandler(handler);
     }
 
     public IParser getParser() {
 	return parser;
     }
 
     public ILexer getLexer() {
 	return lexer;
     }
 
     public Object getCurrentAst() {
 	return currentAst;
     }
 
     public char[][] getKeywords() {
 	return keywords;
     }
 
     public boolean isKeyword(int kind) {
 	return isKeyword[kind];
     }
 
     public int getTokenIndexAtCharacter(int offset) {
 	int index= parser.getParseStream().getTokenIndexAtCharacter(offset);
 	return (index < 0 ? -index : index);
     }
 
     public IToken getTokenAtCharacter(int offset) {
 	return parser.getParseStream().getTokenAtCharacter(offset);
     }
 
     public IASTNodeLocator getNodeLocator() {
 	return new NodeLocator(this);
     }
 
     public boolean hasErrors() {
 	return currentAst == null;
     }
 
     public List getErrors() {
 	return Collections.singletonList(new ParseError("parse error", null));
     }
 
     public ParseController() {
 	lexer= new JikesPGLexer(); // Create the lexer
 	parser= new JikesPGParser(lexer.getLexStream()); // Create the parser
     }
 
     class MyMonitor implements Monitor {
 	IProgressMonitor monitor;
 	boolean wasCancelled= false;
 
 	MyMonitor(IProgressMonitor monitor) {
 	    this.monitor= monitor;
 	}
 
 	public boolean isCancelled() {
 	    if (!wasCancelled)
 		wasCancelled= monitor.isCanceled();
 	    return wasCancelled;
 	}
     }
 
     public Object parse(String contents, boolean scanOnly, IProgressMonitor monitor) {
 	MyMonitor my_monitor= new MyMonitor(monitor);
 	char[] contentsArray= contents.toCharArray();
 
 	lexer.initialize(contentsArray, filePath);
 	parser.getParseStream().resetTokenStream();
 	lexer.lexer(my_monitor, parser.getParseStream()); // Lex the stream to produce the token stream
 	if (my_monitor.isCancelled())
 	    return currentAst; // TODO currentAst might (probably will) be inconsistent wrt the lex stream now
 	currentAst= (ASTNode) parser.parser(my_monitor, 0);
 	if (currentAst == null)
 	    parser.dumpTokens();
 	if (keywords == null)
 	    initKeywords();
 	return currentAst;
     }
 
     private void initKeywords() {
 	String tokenKindNames[]= parser.getParseStream().orderedTerminalSymbols();
 
 	this.isKeyword= new boolean[tokenKindNames.length];
 	this.keywords= new char[tokenKindNames.length][];
 
 	int[] keywordKinds= lexer.getKeywordKinds();
 
 	for(int i= 1; i < keywordKinds.length; i++) {
 	    int index= parser.getParseStream().mapKind(keywordKinds[i]);
 	    isKeyword[index]= true;
 	    keywords[index]= parser.getParseStream().orderedTerminalSymbols()[index].toCharArray();
 	}
     }
 }
