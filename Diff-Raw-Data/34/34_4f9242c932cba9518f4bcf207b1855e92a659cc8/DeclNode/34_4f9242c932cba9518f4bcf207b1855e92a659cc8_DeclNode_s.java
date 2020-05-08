 package cs4120.der34dlc287lg342.xi.ast;
 
 import java.util.ArrayList;
 
 import cs4120.der34dlc287lg342.xi.ir.Binop;
 import cs4120.der34dlc287lg342.xi.ir.Const;
 import cs4120.der34dlc287lg342.xi.ir.Expr;
 import cs4120.der34dlc287lg342.xi.ir.Move;
 import cs4120.der34dlc287lg342.xi.ir.Seq;
 import cs4120.der34dlc287lg342.xi.ir.Temp;
 import cs4120.der34dlc287lg342.xi.ir.context.IRContextStack;
 import cs4120.der34dlc287lg342.xi.ir.context.InvalidIRContextException;
 import cs4120.der34dlc287lg342.xi.ir.context.Register;
 import cs4120.der34dlc287lg342.xi.ir.translate.IRTranslation;
 import cs4120.der34dlc287lg342.xi.ir.translate.IRTranslationStmt;
 import cs4120.der34dlc287lg342.xi.typechecker.ContextList;
 import cs4120.der34dlc287lg342.xi.typechecker.InvalidXiTypeException;
 import cs4120.der34dlc287lg342.xi.typechecker.XiPrimitiveType;
 import cs4120.der34dlc287lg342.xi.typechecker.XiType;
 
 import edu.cornell.cs.cs4120.util.VisualizableTreeNode;
 import edu.cornell.cs.cs4120.xi.CompilationException;
 import edu.cornell.cs.cs4120.xi.Position;
 /**Represents a variable declaration in the AST*/
 public class DeclNode extends AbstractSyntaxTree {
 
 	public Position position;
 	/**The id of this variable declaration.*/
 	public IdNode id;
 	/**The base type of this variable declaration*/
 	public String type_name;
 	/**If this represents an array declaration than this field contains any dimension
 	 * specifiers.*/
 	public ArrayList<VisualizableTreeNode> brackets;
 	protected ArrayList<VisualizableTreeNode> children = new ArrayList<VisualizableTreeNode>();
 	
 	public DeclNode(IdNode id, String type, ArrayList<VisualizableTreeNode> brackets, Position position){
 		this.id = id;
 		this.type_name = type;
 		this.brackets = brackets;
 		this.position = position;
 		children.add(id);
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
 		return "DECL";
 	}
 	
 	/** This node represents a declaration with no assignment so it
 	 * always type checks as unit.
 	 */
 	// TODO: typecheck brackets
 	@Override
 	public XiType typecheck(ContextList stack) throws CompilationException {
 		
 		// make sure that the expressions within the brackets are ints
 		for (VisualizableTreeNode b : brackets){
 			if (b == null) continue;
 			XiType t = ((AbstractSyntaxTree)b).typecheck(stack);
 			// make sure that t is int type
 			if (!t.equals(XiPrimitiveType.INT))
 				throw new CompilationException("Cannot declare arrays with non-integer dimension", position());
 		}
 		
 		try {
 			XiType t = new XiPrimitiveType(type_name, brackets);
 			stack.add_id(id.id, t);
 			if (! t.equals(((AbstractSyntaxTree)id).typecheck(stack)))
 				throw new CompilationException("Cannot match the type of the object to the declared type", position());
 		} catch (InvalidXiTypeException e) {
 			throw new CompilationException(e.getMessage(), position());
 		}
 		
 		type = XiPrimitiveType.UNIT;
 		return type;
 	}
 	
 	@Override
 	public AbstractSyntaxTree foldConstants(){
 		// id cannot be constant
 		((AbstractSyntaxTree)id).foldConstants();
 		// brackets can contain constant terms however
 		int i = 0;
 		for (VisualizableTreeNode brack : brackets){
 			if (brack != null){
 				AbstractSyntaxTree tree = ((AbstractSyntaxTree)brack).foldConstants();
 				if (tree != null){
 					brackets.set(i, tree);
 				}
 			}
 			i++;
 		}
 		return null;
 	}
 
 	@Override
 	public IRTranslation to_ir(IRContextStack stack) throws InvalidIRContextException{
 		/*
 		 * 2 Cases:
 		 * 
 		 * Primitive only:
 		 * 	stack.register(id)
 		 * 
 		 * Array:
 		 *  stack.register(id) // reference to the 
 		 *  move(id, register(heap))
 		 *  
 		 *  if dimensions are present:
 		 *  for each bracket:
 		 *    register(heap, n+1) 
 		 */
 		String id = ((IdNode)this.id).id;
		Expr r = stack.register(id);
 		
 		// check that brackets is dimensionless
 		if (brackets.isEmpty())
 			return new IRTranslationStmt(new Seq());
 		
 		Seq seq = new Seq();
 		// go backwards from bracket
 		Expr last_r = r;
 		for (int i = 0; i < brackets.size(); i++){
 			AbstractSyntaxTree node = (AbstractSyntaxTree)brackets.get(i);
 			Register base = new Register(); // this will be replaced later
 			Temp base_addr = new Temp(base);
 			
 			if (node == null){
 				int n = 0;
 				seq.add(new Move(last_r, new Temp(Register.Null)));
 				break;
 			} else {
 				IRTranslation tr = node.to_ir(stack);
 				Expr n = tr.expr();
 				
 				seq.add(new Move(base_addr, n));
 				last_r = new Binop(Binop.PLUS, base_addr, new Const(8));
 				//seq.add(new Move(last_r, last_r));
 				
 				// reserve those addresses
 			}
 		}
 		
 		return new IRTranslationStmt(seq);
 	}
 }
