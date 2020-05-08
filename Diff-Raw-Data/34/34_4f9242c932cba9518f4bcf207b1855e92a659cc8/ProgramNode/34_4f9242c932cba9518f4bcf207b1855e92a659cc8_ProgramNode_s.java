 package cs4120.der34dlc287lg342.xi.ast;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 
 import cs4120.der34dlc287lg342.xi.XiInterfaceParser;
 import cs4120.der34dlc287lg342.xi.ir.Seq;
 import cs4120.der34dlc287lg342.xi.ir.context.IRContext;
 import cs4120.der34dlc287lg342.xi.ir.context.IRContextStack;
 import cs4120.der34dlc287lg342.xi.ir.context.InvalidIRContextException;
 import cs4120.der34dlc287lg342.xi.ir.translate.IRTranslation;
 import cs4120.der34dlc287lg342.xi.ir.translate.IRTranslationStmt;
 import cs4120.der34dlc287lg342.xi.typechecker.ContextList;
 import cs4120.der34dlc287lg342.xi.typechecker.XiFunctionType;
 import cs4120.der34dlc287lg342.xi.typechecker.XiPrimitiveType;
 import cs4120.der34dlc287lg342.xi.typechecker.XiType;
 
 import edu.cornell.cs.cs4120.util.VisualizableTreeNode;
 import edu.cornell.cs.cs4120.xi.AbstractSyntaxNode;
 import edu.cornell.cs.cs4120.xi.CompilationException;
 import edu.cornell.cs.cs4120.xi.Position;
 
 public class ProgramNode extends AbstractSyntaxTree {
 
 	public Position position;
 	protected ArrayList<VisualizableTreeNode> children = new ArrayList<VisualizableTreeNode>();
 	
 	public ProgramNode(Position position){
 		this.position = position;
 	}
 	
 	public void add(AbstractSyntaxNode expr){
 		children.add(expr);
 	}
 	
 	@Override
 	public Position position() {
 		return position;
 	}
 
 	@Override
 	public Iterable<VisualizableTreeNode> children() {
 		return children;
 	}
 
 	@Override
 	public String label() {
 		return "PROGRAM";
 	}
 	
 	/** Type checks this program node. A program node type checks if
 	 * all its function declaration children type check as XiFunctionTypes.
 	 * 
 	 * @param stack The current context stack. 
 	 */
 	@Override
  	public XiType typecheck(ContextList stack) throws CompilationException {
 		for( VisualizableTreeNode childTree : children) {
 			if( childTree instanceof FuncDeclNode) {
 				XiType childType = ((AbstractSyntaxTree)childTree).typecheck(stack);
 				
 				if(!(childType instanceof XiFunctionType))
 					throw new CompilationException("Invalid program",position);
 			}
 		}
 		
 		type = XiPrimitiveType.UNIT;
 		return type;
 	}
 
 	// add function declarations found within interfaces into the global context
 	// TODO: refactor into a single pass
 	void include(String id, IRContextStack stack) throws IOException, InvalidIRContextException{
 		FileReader reader = new FileReader(id+".ixi");
 		
 		String src = "";
 		BufferedReader input =  new BufferedReader(reader);
 		String line = null;
 		while (( line = input.readLine()) != null){
 			src += line.replace("\r", "") + "\n";
 		}
 		
 		XiInterfaceParser parser = new XiInterfaceParser(new StringReader(src), id+".ixi");
 		AbstractSyntaxNode declarations;
 		try{
 			declarations = parser.parse();
 		} catch (CompilationException e){
 			throw new InvalidIRContextException(e.getMessage());
 		}
 		for (VisualizableTreeNode child : declarations.children()){
 			FuncDeclNode decl = (FuncDeclNode)child;
			stack.name(decl);
 		}
 	}
 	
 	@Override
 	public IRTranslation to_ir(IRContextStack stack) throws InvalidIRContextException{
 		/*
 		 * 2 passes
 		 * first one builds the context with globals
 		 * second one addes a sequence of funcdecls
 		 */
 		
 		// First pass: build context
 		IRContext global = new IRContext();
 		stack.push(global);
 		
 		for (VisualizableTreeNode child : children()){
 			if (child instanceof UseNode){
 				UseNode use = (UseNode) child;
 				try {
 					include(use.lib.id, stack);
 				} catch (IOException e) {
 					throw new InvalidIRContextException(e.getMessage());
 				}
 			} else if (child instanceof FuncDeclNode){
 				FuncDeclNode func = (FuncDeclNode) child;
				stack.name(func);
 			}
 		}
 		
 		// Second pass: build translation rules
 		Seq seq = new Seq();
 		for (VisualizableTreeNode child : children()){
 			if (child instanceof FuncDeclNode){
 				FuncDeclNode func = (FuncDeclNode) child;
 				IRTranslation tr = func.to_ir(stack);
 				seq.add(tr.stmt());
 			}
 		}
 		
 		return new IRTranslationStmt(seq);
 	}
 }
