 	package visitor;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import ast.AccessNode;
 import ast.BlockNode;
 import ast.DataTypeDeclNode;
 import ast.ExponentNode;
 import ast.FactorNode;
 import ast.GlobalDeclListNode;
 import ast.GlobalDeclNode;
 import ast.IdNode;
 import ast.LengthFunctionNode;
 import ast.LocalDeclListNode;
 import ast.ParameterListNode;
 import ast.VarDeclNode;
 import ast.VarInitNode;
 import ast.VarTypeNode;
 import ast.expression.BinaryExprNode;
 import ast.expression.ConcatExprNode;
 import ast.expression.EqualExprNode;
 import ast.expression.ExprListNode;
 import ast.expression.ExprNode;
 import ast.expression.GreaterThanEqualExprNode;
 import ast.expression.GreaterThanExprNode;
 import ast.expression.InExprNode;
 import ast.expression.LessThanEqualExprNode;
 import ast.expression.LessThanExprNode;
 import ast.expression.MinusExprNode;
 import ast.expression.NotEqualExprNode;
 import ast.expression.OrExprNode;
 import ast.expression.ParExprNode;
 import ast.expression.ParSeqExprNode;
 import ast.expression.PlusExprNode;
 import ast.literal.BoolLiteralNode;
 import ast.literal.CharLiteralNode;
 import ast.literal.FloatLiteralNode;
 import ast.literal.IntLiteralNode;
 import ast.sequence.ListSeqNode;
 import ast.sequence.StringSeqNode;
 import ast.sequence.TupleSeqNode;
 import ast.statement.AssignStmtNode;
 import ast.statement.FunctionCallStmtNode;
 import ast.statement.IfElseStmtNode;
 import ast.statement.RepeatUntilStmtNode;
 import ast.statement.ReturnStmtNode;
 import ast.statement.StmtListNode;
 import ast.statement.StmtNode;
 import ast.statement.WhileStmtNode;
 import ast.term.AndTermNode;
 import ast.term.DivideTermNode;
 import ast.term.MultiplyTermNode;
 import ast.term.PowerTermNode;
 
 public class SementicVisitor implements Visitor {
 
 	public SymbolTable table;
 	public int numberOfErrors = 0;
 
 	private final static String FLOAT = "FLOAT";
 	private final static String INT = "INT";
 	private final static String BOOL = "BOOL";
 	private final static String CHAR = "CHAR";
 	private final static String STRING = "STRING";
 	private final static String VOID = "VOID";
 	private final static String LIST = "LIST";
 	private final static String TUPLE = "TUPLE";
 
 	public SementicVisitor() {
 		table = new SymbolTable();
 	}
 
 	@Override
 	public Boolean visit(BlockNode node) {
 
 		table = table.beginScope();
 
 		// Visit the Local Declaration List
 		if (node.ldl != null) {
 			node.ldl.accept(this);
 		}
 		// Visit the Statement List
 		if (node.sl != null) {
 			node.sl.accept(this);
 		}
 		table = table.endScope();
 		return null;
 	}
 
 	@Override
 	public Boolean visit(MultiplyTermNode node) {
 		if (node.lhs != null) {
 			node.lhs.accept(this);
 		}
 		if (node.rhs != null) {
 			node.rhs.accept(this);
 		}
 		if ((node.lhs.type.equals(INT) && node.rhs.type.equals(FLOAT))
 				|| (node.lhs.type.equals(FLOAT) && node.rhs.type.equals(INT))) {
 			node.type = FLOAT;
 		} else if (node.lhs.type.equals(INT) && node.rhs.type.equals(INT)) {
 			node.type = INT;
 		} else if (node.lhs.type.equals(FLOAT) && node.rhs.type.equals(FLOAT)) {
 			node.type = FLOAT;
 		} else if (!node.lhs.type.equals(node.rhs.type)) {
 			System.out.println("Type error found: " + node.lhs.type
 					+ " does not match " + node.rhs.type);
 		} else {
 			System.out
 					.println("Type error found: Incorrect types for multiplication operation.");
 		}
 		return null;
 	}
 
 	@Override
 	public Boolean visit(AndTermNode node) {
 		if (node.lhs != null) {
 			node.lhs.accept(this);
 		}
 		if (node.rhs != null) {
 			node.rhs.accept(this);
 		}
 		if (node.lhs.type.equals(BOOL) && node.rhs.type.equals(BOOL)) {
 			node.type = BOOL;
 		} else if (!node.lhs.type.equals(node.rhs.type)) {
 			System.out.println("Type error found: " + node.lhs.type
 					+ " does not match " + node.rhs.type);
 		} else {
 			System.out
 					.println("Type error found: Incorrect types for and operation.");
 		}
 		return null;
 	}
 
 	@Override
 	public Boolean visit(DivideTermNode node) {
 		if (node.lhs != null) {
 			node.lhs.accept(this);
 		}
 		if (node.rhs != null) {
 			node.rhs.accept(this);
 		}
 		if ((node.lhs.type.equals(INT) && node.rhs.type.equals(FLOAT))
 				|| (node.lhs.type.equals(FLOAT) && node.rhs.type.equals(INT))) {
 			node.type = FLOAT;
 		} else if (node.lhs.type.equals(INT) && node.rhs.type.equals(INT)) {
 			node.type = INT;
 		} else if (node.lhs.type.equals(FLOAT) && node.rhs.type.equals(FLOAT)) {
 			node.type = FLOAT;
 		} else if (!node.lhs.type.equals(node.rhs.type)) {
 			System.out.println("Type error found: " + node.lhs.type
 					+ " does not match " + node.rhs.type);
 		} else {
 			System.out
 					.println("Type error found: Incorrect types for division operation.");
 		}
 		return null;
 	}
 
 	@Override
 	public Boolean visit(PowerTermNode node) {
 		if (node.lhs != null) {
 			node.lhs.accept(this);
 		}
 		if (node.rhs != null) {
 			node.rhs.accept(this);
 		}
 		if ((node.lhs.type.equals(INT) && node.rhs.type.equals(FLOAT))
 				|| (node.lhs.type.equals(FLOAT) && node.rhs.type.equals(INT))) {
 			node.type = FLOAT;
 		} else if (node.lhs.type.equals(INT) && node.rhs.type.equals(INT)) {
 			node.type = INT;
 		} else if (node.lhs.type.equals(FLOAT) && node.rhs.type.equals(FLOAT)) {
 			node.type = FLOAT;
 		} else if (!node.lhs.type.equals(node.rhs.type)) {
 			System.out.println("Type error found: " + node.lhs.type
 					+ " does not match " + node.rhs.type);
 		} else {
 			System.out.println("Type error found: Incorrect types for power operation.");
 		}
 		return null;
 	}
 
 	@Override
 	public Boolean visit(AssignStmtNode node) {
 		//TODO add casting if extra time 
 		if(node.a!=null){
 			if(!node.a.type.equals(node.rhs.type)){
 				System.out.println("Type error found: When assigning type "+node.a.type+" doesn't match type "+node.rhs.type);
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public Boolean visit(FunctionCallStmtNode node) {
 
 		if (!table.lookup(node.id)) {
 			reportError("Function " + node.id + " has not been declared.");
 			return false;
 		}
 
 		// Looks at the number of parameters
 		Symbol functSymbol = table.search(node.id);
 		List<VarTypeNode> parameterTypes = functSymbol.getTypes();
 		int parameterSize = parameterTypes.size() - 1;
 
 		if (parameterSize != getParameterSize(node.parameters)) {
 			reportError("Function " + node.id
 					+ " is called with incorrect parameter(s).");
 		}
 
 		if (parameterSize != 0) {
 			if (!node.parameters.e.equals(parameterTypes.get(0).type)) {
 				reportError("Function " + node.id
 						+ " contains invalid parameter.");
 			}
 			for (int i = 1; i < parameterSize; i++) {
 				String actualType = node.parameters.el.expressions.get(i - 1).type;
 				String expectedType = parameterTypes.get(i).type;
 
 				if (!actualType.equals(expectedType)) {
 					reportError("Function " + node.id
 							+ " contains unexpected an parameter");
 					return false;
 				}
 			}
 		}
 
 		if (parameterSize == 0) {
 			node.type = parameterTypes.get(0).type;
 		} else {
 			node.type = parameterTypes.get(parameterSize - 1).type;
 		}
 		return null;
 	}
 
 	private int getParameterSize(ExprListNode node) {
 		int i = 0;
 
 		if (node.e != null) {
 			i++;
 		}
 
 		if (node.el != null) {
 			i += node.el.expressions.size();
 		}
 
 		return i;
 	}
 
 	@Override
 	public Boolean visit(IfElseStmtNode node) {
 		if (node.condition != null) {
 			node.condition.accept(this);
 		} else {
 			reportError("Condition missing in While statement.");
 			return false;
 		}
 
 		if (node.ifbody != null) {
 			node.ifbody.accept(this);
 		}
 
 		if (node.elsebody != null) {
 			node.elsebody.accept(this);
 		}
 
 		if (!node.condition.type.equals(BOOL)) {
 			reportError("Condition must evaluate to boolean.");
 		}
 
 		return null;
 	}
 
 	@Override
 	public Boolean visit(RepeatUntilStmtNode node) {
 		if (node.condition != null) {
 			node.condition.accept(this);
 		} else {
 			reportError("Condition missing in While statement.");
 			return false;
 		}
 
 		if (node.body != null) {
 			node.body.accept(this);
 		}
 
 		if (!node.condition.type.equals(BOOL)) {
 			reportError("Condition must evaluate to boolean.");
 		}
 
 		return null;
 	}
 
 	@Override
 	public Boolean visit(ReturnStmtNode node) {
 		if (node.ret != null) {
 			node.ret.accept(this);
 			node.type = node.ret.type;
 		} else {
 			node.type = VOID;
 		}
 
 		return null;
 	}
 
 	@Override
 	public Boolean visit(WhileStmtNode node) {
 
 		if (node.condition != null) {
 			node.condition.accept(this);
 		} else {
 			reportError("Condition missing in While statement.");
 			return false;
 		}
 
 		if (node.body != null) {
 			node.body.accept(this);
 		}
 
 		if (!node.condition.type.equals(BOOL)) {
 			reportError("Condition must evaluate to boolean.");
 		}
 
 		return null;
 	}
 
 	@Override
 	public Boolean visit(BinaryExprNode node) {
 
 		// Probably don't need this.
 		return null;
 	}
 
 	@Override
 	public Boolean visit(ConcatExprNode node) {
 		if (node.lhs != null) {
 			node.lhs.accept(this);
 		}
 		if (node.rhs != null) {
 			node.lhs.accept(this);
 		}
 		if (node.lhs.type.equals(LIST) || node.lhs.type.equals(TUPLE)
 				|| node.lhs.type.equals(STRING)) {
 			if (node.rhs.type.equals(LIST) || node.rhs.type.equals(TUPLE)
 					|| node.rhs.type.equals(STRING)) {
 				if (node.lhs.type.equals(node.rhs.type)) {
 					node.type = node.lhs.type;
 				}
 			} else {
 				System.out
 						.println("Type error found: Applying concatination on type "
 								+ node.rhs.type);
 			}
 		} else {
 			System.out
 					.println("Type error found: Applying concatination on type "
 							+ node.lhs.type);
 		}
 		return null;
 	}
 
 	@Override
 	public Boolean visit(EqualExprNode node) {
 		if (node.lhs != null) {
 			node.lhs.accept(this);
 		}
 		if (node.rhs != null) {
 			node.rhs.accept(this);
 		}
 		if ((node.lhs.type.equals(INT) && node.rhs.type.equals(INT))
 				|| (node.lhs.type.equals(CHAR) && node.rhs.type.equals(CHAR))
 				|| (node.lhs.type.equals(FLOAT) && node.rhs.type.equals(FLOAT))) {
 			node.type = BOOL;
 		} else if (node.lhs.type.equals(BOOL) && node.rhs.type.equals(BOOL)) {
 			node.type = BOOL;
 		} else if (!node.lhs.type.equals(node.rhs.type)) {
 			System.out.println("Type error found: " + node.lhs.type
 					+ " does not match " + node.rhs.type);
 		} else {
 			System.out
 					.println("Type error found: Incorrect types used with equal to operation.");
 		}
 		return null;
 	}
 
 	@Override
 	public Boolean visit(GreaterThanEqualExprNode node) {
 		if (node.lhs != null) {
 			node.lhs.accept(this);
 		}
 		if (node.rhs != null) {
 			node.rhs.accept(this);
 		}
 		if ((node.lhs.type.equals(INT) && node.rhs.type.equals(INT))
 				|| (node.lhs.type.equals(CHAR) && node.rhs.type.equals(CHAR))
 				|| (node.lhs.type.equals(FLOAT) && node.rhs.type.equals(FLOAT))) {
 			node.type = BOOL;
 		} else if (node.lhs.type.equals(BOOL) && node.rhs.type.equals(BOOL)) {
 			System.out
 					.println("Type error found: Greater than or equal expression used to compare boolean values.");
 		} else if (!node.lhs.type.equals(node.rhs.type)) {
 			System.out.println("Type error found: " + node.lhs.type
 					+ " does not match " + node.rhs.type);
 		} else {
 			System.out
 					.println("Type error found: Incorrect types used with greater than or equal operation.");
 		}
 		return null;
 	}
 
 	@Override
 	public Boolean visit(GreaterThanExprNode node) {
 		if (node.lhs != null) {
 			node.lhs.accept(this);
 		}
 		if (node.rhs != null) {
 			node.rhs.accept(this);
 		}
 		if ((node.lhs.type.equals(INT) && node.rhs.type.equals(INT))
 				|| (node.lhs.type.equals(CHAR) && node.rhs.type.equals(CHAR))
 				|| (node.lhs.type.equals(FLOAT) && node.rhs.type.equals(FLOAT))) {
 			node.type = BOOL;
 		} else if (node.lhs.type.equals(BOOL) && node.rhs.type.equals(BOOL)) {
 			System.out
 					.println("Type error found: Greater than expression used to compare boolean values.");
 		} else if (!node.lhs.type.equals(node.rhs.type)) {
 			System.out.println("Type error found: " + node.lhs.type
 					+ " does not match " + node.rhs.type);
 		} else {
 			System.out
 					.println("Type error found: Incorrect types used with greater than operation.");
 		}
 		return null;
 	}
 
 	@Override
 	public Boolean visit(InExprNode node) {
 		if (node.lhs != null) {
 			node.lhs.accept(this);
 		}
 		if (node.rhs != null) {
 			node.lhs.accept(this);
 		}
 		if (node.rhs.type.contains(LIST)) {
 			if (node.rhs.type.contains(node.lhs.type))
 				node.type = BOOL;
 			else
 				System.out.println("Type error found: List does not include "
 						+ node.lhs.type + ".");
 
 		} else if (node.rhs.type.contains(TUPLE)) {
 			if (node.rhs.type.contains(node.lhs.type))
 				node.type = BOOL;
 			else
 				System.out.println("Type error found: Tuple does not include "
 						+ node.lhs.type + ".");
 		} else if (node.rhs.type.equals(STRING)) {
 			if (node.lhs.type.equals(CHAR))
 				node.type = BOOL;
 			else
 				System.out.println("Type error found: Searching string for "
 						+ node.lhs.type);
 		} else {
 			System.out
 					.println("Type error found: Operation In used on a type other than List, Tuple or String.");
 		}
 
 		return null;
 	}
 
 	@Override
 	public Boolean visit(LessThanEqualExprNode node) {
 		if (node.lhs != null) {
 			node.lhs.accept(this);
 		}
 		if (node.rhs != null) {
 			node.rhs.accept(this);
 		}
 		if ((node.lhs.type.equals(INT) && node.rhs.type.equals(INT))
 				|| (node.lhs.type.equals(CHAR) && node.rhs.type.equals(CHAR))
 				|| (node.lhs.type.equals(FLOAT) && node.rhs.type.equals(FLOAT))) {
 			node.type = BOOL;
 		} else if (node.lhs.type.equals(BOOL) && node.rhs.type.equals(BOOL)) {
 			System.out
 					.println("Type error found: Less than or equal expression used to compare boolean values.");
 		} else if (!node.lhs.type.equals(node.rhs.type)) {
 			System.out.println("Type error found: " + node.lhs.type
 					+ " does not match " + node.rhs.type);
 		} else {
 			System.out
 					.println("Type error found: Incorrect types used with less than or equal operation.");
 		}
 		return null;
 	}
 
 	@Override
 	public Boolean visit(LessThanExprNode node) {
 		if (node.lhs != null) {
 			node.lhs.accept(this);
 		}
 		if (node.rhs != null) {
 			node.rhs.accept(this);
 		}
 		if ((node.lhs.type.equals(INT) && node.rhs.type.equals(INT))
 				|| (node.lhs.type.equals(CHAR) && node.rhs.type.equals(CHAR))
 				|| (node.lhs.type.equals(FLOAT) && node.rhs.type.equals(FLOAT))) {
 			node.type = BOOL;
 		} else if (node.lhs.type.equals(BOOL) && node.rhs.type.equals(BOOL)) {
 			System.out
 					.println("Type error found: Less than expression used to compare boolean values.");
 		} else if (!node.lhs.type.equals(node.rhs.type)) {
 			System.out.println("Type error found: " + node.lhs.type
 					+ " does not match " + node.rhs.type);
 		} else {
 			System.out
 					.println("Type error found: Incorrect types used with less than operation.");
 		}
 		return null;
 	}
 
 	@Override
 	public Boolean visit(MinusExprNode node) {
 		if (node.lhs != null) {
 			node.lhs.accept(this);
 		}
 		if (node.rhs != null) {
 			node.rhs.accept(this);
 		}
 		if ((node.lhs.type.equals(INT) && node.rhs.type.equals(FLOAT))
 				|| (node.lhs.type.equals(FLOAT) && node.rhs.type.equals(INT))) {
 			node.type = FLOAT;
 		} else if (node.lhs.type.equals(INT) && node.rhs.type.equals(INT)) {
 			node.type = INT;
 		} else if (node.lhs.type.equals(FLOAT) && node.rhs.type.equals(FLOAT)) {
 			node.type = FLOAT;
 		} else if (!node.lhs.type.equals(node.rhs.type)) {
 			System.out.println("Type error found: " + node.lhs.type
 					+ " does not match " + node.rhs.type);
 		} else {
 			System.out
 					.println("Type error found: Incorrect types for subtraction operation.");
 		}
 		return null;
 	}
 
 	@Override
 	public Boolean visit(NotEqualExprNode node) {
 		if (node.lhs != null) {
 			node.lhs.accept(this);
 		}
 		if (node.rhs != null) {
 			node.rhs.accept(this);
 		}
 		if ((node.lhs.type.equals(INT) && node.rhs.type.equals(INT))
 				|| (node.lhs.type.equals(CHAR) && node.rhs.type.equals(CHAR))
 				|| (node.lhs.type.equals(FLOAT) && node.rhs.type.equals(FLOAT))) {
 			node.type = BOOL;
 		} else if (node.lhs.type.equals(BOOL) && node.rhs.type.equals(BOOL)) {
 			node.type = BOOL;
 		} else if (!node.lhs.type.equals(node.rhs.type)) {
 			System.out.println("Type error found: " + node.lhs.type
 					+ " does not match " + node.rhs.type);
 		} else {
 			System.out
 					.println("Type error found: Incorrect types used with not equal operation.");
 		}
 		return null;
 	}
 
 	@Override
 	public Boolean visit(OrExprNode node) {
 		if (node.lhs != null) {
 			node.lhs.accept(this);
 		}
 		if (node.rhs != null) {
 			node.rhs.accept(this);
 		}
 		if (node.lhs.type.equals(BOOL) && node.rhs.type.equals(BOOL)) {
 			node.type = BOOL;
 		} else if (!node.lhs.type.equals(node.rhs.type)) {
 			System.out.println("Type error found: " + node.lhs.type
 					+ " does not match " + node.rhs.type);
 		} else {
 			System.out
 					.println("Type error found: Incorrect types for or operation.");
 		}
 		return null;
 	}
 
 	@Override
 	public Boolean visit(PlusExprNode node) {
 		if (node.lhs != null) {
 			node.lhs.accept(this);
 		}
 		if (node.rhs != null) {
 			node.rhs.accept(this);
 		}
 		if ((node.lhs.type.equals(INT) && node.rhs.type.equals(FLOAT))
 				|| (node.lhs.type.equals(FLOAT) && node.rhs.type.equals(INT))) {
 			node.type = FLOAT;
 		} else if (node.lhs.type.equals(INT) && node.rhs.type.equals(INT)) {
 			node.type = INT;
 		} else if (node.lhs.type.equals(FLOAT) && node.rhs.type.equals(FLOAT)) {
 			node.type = FLOAT;
 		} else if (!node.lhs.type.equals(node.rhs.type)) {
 			System.out.println("Type error found: " + node.lhs.type
 					+ " does not match " + node.rhs.type);
 		} else {
 			System.out
 					.println("Type error found: Incorrect types for addition operation.");
 		}
 		return null;
 	}
 
 	@Override
 	public Boolean visit(ListSeqNode node) {
 		List<ExprNode> exprs = node.exprList.el.expressions;
 		ExprNode firstElem = exprs.get(0);
 		firstElem.accept(this);
 		String prevNode = firstElem.type;
 		for (ExprNode e : exprs) {
 			e.accept(this);
 			if (!prevNode.equals(e.type)) {
 				System.out.println("List Type Error: List contains " + e.type
 						+ " and " + prevNode);
 				prevNode = null;
 				break;
 			}
 			prevNode = e.type;
 		}
 		node.type = "LIST" + prevNode;
 		return null;
 	}
 
 	@Override
 	public Boolean visit(TupleSeqNode node) {
 		List<ExprNode> exprs = node.exprList.el.expressions;
 		String tupleListType = null;
 		for (ExprNode e : exprs) {
 			e.accept(this);
 			tupleListType += e.type + ',';
 		}
 		node.type = "TUPLE:" + tupleListType;
 		return null;
 	}
 
 	@Override
 	public Boolean visit(StringSeqNode node) {
 		node.type = STRING;
 		return null;
 	}
 
 	@Override
 	public Boolean visit(StmtListNode node) {
 		for (StmtNode child : node.statements) {
 			child.accept(this);
 		}
 		return null;
 	}
 
 	@Override
 	public Boolean visit(GlobalDeclListNode node) {
 		for (GlobalDeclNode child : node.statements) {
 			child.accept(this);
 		}
 		return null;
 	}
 
 	@Override
 	public Boolean visit(LocalDeclListNode node) {
 		for (VarDeclNode child : node.varDecls) {
 			child.accept(this);
 		}
 		return null;
 	}
 
 	@Override
 	public Boolean visit(LengthFunctionNode node) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Boolean visit(ParameterListNode node) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Boolean visit(VarDeclNode node) {
 
 		if (node.vt != null) {
 			node.vt.accept(this);
 		}
 		if (node.vi != null) {
 			node.vi.accept(this);
 		}
 
 		if (!node.vt.type.equals(node.vi.type)) {
 			reportError("Invalid variable declaration for variable "
 					+ node.vt.id + ".");
 		}
 
 		return null;
 	}
 
 	@Override
 	public Boolean visit(ExponentNode node) {
 		if(node!=null){
 			node.accept(this);
 		}
 		if(!(node.type.equals(INT)||node.type.equals(FLOAT))){
 			System.out.println("Type error found: Exponent type of "+node.type);
 		}
 		return null;
 	}
 
 	@Override
 	public Boolean visit(FactorNode node) {
 		if (node.lhs != null) {
 			node.lhs.accept(this);
 		}
 		if (node.rhs != null) {
 			node.rhs.accept(this);
 		}
 		if ((node.lhs.type.equals(INT) && node.rhs.type.equals(FLOAT))
 				|| (node.lhs.type.equals(FLOAT) && node.rhs.type.equals(INT))) {
 			node.type = FLOAT;
 		} else if (node.lhs.type.equals(INT) && node.rhs.type.equals(INT)) {
 			node.type = INT;
 		} else if (node.lhs.type.equals(FLOAT) && node.rhs.type.equals(FLOAT)) {
 			node.type = FLOAT;
 		} else if (!node.lhs.type.equals(node.rhs.type)) {
 			System.out.println("Type error found: " + node.lhs.type
 					+ " does not match " + node.rhs.type);
 		} else {
 			System.out.println("Type error found: Incorrect types for power operation.");
 		}
 		return null;
 	}
 
 	@Override
 	public Object visit(DataTypeDeclNode node) {
 		return null;
 	}
 
 	@Override
 	public Object visit(VarTypeNode node) {
 		Symbol symbol = new Symbol();
 		symbol.setId(node.id);
 
 		List<String> types = new ArrayList<String>();
 		types.add(node.type);
 		symbol.setKind(Kind.VAR);
 
 		node.type = types.get(0);
 
 		if (!table.put(symbol)) {
 			reportError("Variable " + node.id + " not within scope.");
 		}
 
 		return null;
 	}
 
 	@Override
 	public Object visit(VarInitNode node) {
 		if (node.el != null) {
 			node.el.accept(this);
 		}
 		node.type = node.el.type;
 		return null;
 	}
 
 	private void reportError(String message) {
 		System.out.println(message);
 		numberOfErrors++;
 	}
 
 	@Override
 	public Object visit(AccessNode node) {
 		List<IdNode> ids = node.ids;
 		if (ids != null) {
 			IdNode lastAccessor = ids.get(ids.size() - 1);
 			lastAccessor.accept(this);
 		}
 		return null;
 	}
 
 	@Override
 	public Object visit(BoolLiteralNode node) {
 		node.type = BOOL;
 		return null;
 	}
 
 	@Override
 	public Object visit(CharLiteralNode node) {
 		node.type = CHAR;
 		return null;
 	}
 
 	@Override
 	public Object visit(FloatLiteralNode node) {
 		node.type = FLOAT;
 		return null;
 	}
 
 	@Override
 	public Object visit(IntLiteralNode node) {
 		node.type = INT;
 		return null;
 	}
 
 	@Override
 	public Object visit(IdNode node) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Boolean visit(FunctionDeclNode node) {
 		table = table.beginScope();
 		Symbol functionSymbol = new Symbol();
 
 		if (!table.lookup(node.id)) {
 			reportError("The function name " + node.id + " already exists.");
 			return false;
 		}
 
 		functionSymbol.setId(node.id);
 		functionSymbol.setKind(Kind.METHOD);
 		List<VarTypeNode> varTypeNodes = new ArrayList<VarTypeNode>();
 
 		for (VarTypeNode varType : node.pl.parameterListSNode.varTypes) {
 			Symbol symbol = new Symbol();
 			symbol.setId(varType.id);
 			symbol.setKind(Kind.ARGS);
 			List<VarTypeNode> types = new ArrayList<VarTypeNode>();
 			types.add(varType);
 			table.put(symbol);
 		}
 
 		varTypeNodes.addAll(node.pl.parameterListSNode.varTypes);
 
 		table = table.endScope();
 
 		VarTypeNode methodTypeNode = new VarTypeNode(node.id, node.ft);
 		varTypeNodes.add(methodTypeNode);
 
 		functionSymbol.setTypes(varTypeNodes);
 
 		table.put(functionSymbol);
 
 		return null;
 	}
 
 	@Override
 	public Object visit(ParExprNode node) {
 		if(node.outExpr != null){
 			node.outExpr.accept(this);
 		}
 		if(node.expr1 != null){
 			node.expr1.accept(this);
 		}
 		return null;
 	}
 
 	@Override
 	public Object visit(ParSeqExprNode node) {
 		if(node.outExpr != null){
 			node.outExpr.accept(this);
 		}
 		if(node.expr1 != null){
 			node.expr1.accept(this);
 		}
 		if(node.expr2 != null){
 			node.expr2.accept(this);
 		}
 		if(!(node.outExpr.type.contains(LIST))||(node.outExpr.type.contains(TUPLE))||(node.outExpr.type.contains(STRING))){
 			System.out.println("List slicing not called on a List or Tuple");
 		}else 
 		if((!node.expr1.type.equals(INT))||(!node.expr2.type.equals(INT))){
 			System.out.println("List slicing not called with integer parameters");
 		}
 		
 		return null;
 	}
 
 }
