 package toolbus_ide.editor;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.Iterator;
 
 import java_cup.runtime.Symbol;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.imp.model.ISourceProject;
 import org.eclipse.imp.parser.IMessageHandler;
 import org.eclipse.imp.parser.IParseController;
 import org.eclipse.imp.parser.ISourcePositionLocator;
 import org.eclipse.imp.services.IAnnotationTypeInfo;
 import org.eclipse.imp.services.ILanguageSyntaxProperties;
 import org.eclipse.jface.text.IRegion;
 
 import toolbus.ToolBus;
 import toolbus.parsercup.Lexer;
 import toolbus.parsercup.sym;
 import toolbus.parsercup.parser.SyntaxErrorException;
 import toolbus.parsercup.parser.UndeclaredVariableException;
 
 public class ParseController implements IParseController {
 	private volatile IMessageHandler handler;
 	private volatile ISourceProject project;
 	private volatile String absPath;
 	
 	private volatile Lexer lexer;
 
 	public IAnnotationTypeInfo getAnnotationTypeInfo() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	public static class SymbolHolder{
 		public final Symbol symbol;
 		public final int startOffset;
 		public final int endOffset;
 		
 		public SymbolHolder(Symbol symbol, int startOffset, int endOffset){
 			this.symbol = symbol;
 			this.startOffset = startOffset;
 			this.endOffset = endOffset;
 		}
 	}
 	
     public Iterator<?> getTokenIterator(IRegion region){
     	class TokenIterator implements Iterator<SymbolHolder>{
     		private int currentOffset;
     		private Symbol nextSymbol;
     		
     		public TokenIterator(){
     			super();
     			prepareNext();
     		}
     		
     		public void prepareNext(){
     			try{
     				nextSymbol = lexer.next_token();
     				currentOffset = lexer.getPosition();
 				}catch(IOException ioex){
 					// Ignore this, since it can't happen.
				}catch(Error e){
					// This doesn't matter. it'll just generate error symbols.
 				}
     		}
 
 			public boolean hasNext(){
				return !(nextSymbol.sym == sym.EOF || nextSymbol.sym == sym.error);
 			}
 
 			public SymbolHolder next(){
 				if(!hasNext()) return null;
 				
 				int offset = currentOffset;
 				Symbol symbol = nextSymbol;
 
 				prepareNext();
 				
				System.out.println(offset+"\t"+currentOffset+"\t"+symbol.sym+"\t"+symbol.value);
				
 				return new SymbolHolder(symbol, offset, currentOffset);
 			}
 
 			public void remove(){
 				throw new UnsupportedOperationException("Removing is not supported by this iterator.");
 			}
     	}
     	
     	return new TokenIterator();
     }
 
 	public Object getCurrentAst() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public ISourcePositionLocator getNodeLocator() {
 		return new TokenLocator();
 	}
 
 	public IPath getPath() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public ISourceProject getProject() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public ILanguageSyntaxProperties getSyntaxProperties() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void initialize(IPath filePath, ISourceProject project, IMessageHandler handler) {
 		this.handler = handler;
 		this.project = project;
 		
 		// Try to make the path absolute
 		IFile file = project.getRawProject().getFile(filePath);
 		
 		if(file.exists()){
 			absPath = file.getLocation().toOSString();
 		}else{
 			absPath = filePath.toOSString();
 		}
 	}
 
 	public Object parse(String input, boolean scanOnly, IProgressMonitor monitor) {
 		lexer = new Lexer(new StringReader(input));
 		
 		ToolBus toolbus = new ToolBus(new String[] {"-S"+absPath, "-I."});
 		try{
 			toolbus.parsecup();
 	    }catch(SyntaxErrorException see){ // Parser.
 	    	handler.handleSimpleMessage(see.getMessage(), see.position, see.position, see.column, see.column, see.line, see.line);
 		}catch(UndeclaredVariableException uvex){ // Parser.
 			handler.handleSimpleMessage(uvex.getMessage(), uvex.position, uvex.position, uvex.column, uvex.column, uvex.line, uvex.line);
 		}catch(Error e){ // Scanner.
 			e.printStackTrace();	
 	    }catch(Exception ex){ // Something else.
 	    	ex.printStackTrace();
 	    }
 		
 		return null;
 	}
 }
