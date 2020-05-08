 package visitor;
 
 import ast.AccessNode;
 import ast.BlockNode;
 import ast.DataTypeDeclNode;
 import ast.ExponentNode;
 import ast.FactorNode;
 import ast.GlobalDeclListNode;
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
 import ast.statement.WhileStmtNode;
 import ast.term.AndTermNode;
 import ast.term.DivideTermNode;
 import ast.term.MultiplyTermNode;
 import ast.term.PowerTermNode;
 
 public interface Visitor {
 	
 	/* Base */
 	public Object visit(BlockNode node); //Done
 	
 	/* Term Nodes */
 	public Object visit(MultiplyTermNode node);
 	public Object visit(AndTermNode node);
 	public Object visit(DivideTermNode node);
 	public Object visit(PowerTermNode node);
 	
 	/* Statement Nodes */
 	public Object visit(StmtListNode node);
 	public Object visit(AssignStmtNode node);
 	public Object visit(FunctionCallStmtNode node);
 	public Object visit(IfElseStmtNode node);
 	public Object visit(RepeatUntilStmtNode node);
 	public Object visit(ReturnStmtNode node);
 	public Object visit(WhileStmtNode node);
 	
 	/* Expression Nodes */
 	public Object visit(BinaryExprNode node);
 	public Object visit(ConcatExprNode node);
 	public Object visit(EqualExprNode node);
 	public Object visit(GreaterThanEqualExprNode node);
 	public Object visit(GreaterThanExprNode node);
 	public Object visit(InExprNode node);
 	public Object visit(LessThanEqualExprNode node);
 	public Object visit(LessThanExprNode node);
 	public Object visit(MinusExprNode node);
 	public Object visit(NotEqualExprNode node);
 	public Object visit(OrExprNode node);
 	public Object visit(PlusExprNode node);
 	public Object visit(ParExprNode node);
 	public Object visit(ParSeqExprNode node);
 
 	
 	
 	
 	/* Literal Nodes */
 	public Object visit(BoolLiteralNode node);
 	public Object visit(CharLiteralNode node);
 	public Object visit(FloatLiteralNode node);
 	public Object visit(IntLiteralNode node);
 	
 	/* Sequence Nodes */
 	public Object visit(ListSeqNode node);
 	public Object visit(TupleSeqNode node);
 	public Object visit(StringSeqNode node);
 	
 	/* Parameters, Declaration, etc */
 	public Object visit(IdNode node);
 	public Object visit(AccessNode node);
 	public Object visit(GlobalDeclListNode node);
 	public Object visit(LocalDeclListNode node);
 	public Object visit(LengthFunctionNode node);
 	public Object visit(FunctionDeclNode node);
 	public Object visit(ParameterListNode node);
 	public Object visit(DataTypeDeclNode node);
 	public Object visit(VarDeclNode node);
 	public Object visit(VarTypeNode node);
 	public Object visit(VarInitNode node);
 	public Object visit(ExponentNode node);
 	public Object visit(FactorNode node);
 	
 }
