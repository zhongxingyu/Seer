 package test;
 
 import parser.Add;
 import parser.Assgn;
 import parser.Decl;
 import parser.Div;
 import parser.Eq;
 import parser.ExprStmt;
 import parser.ForStmt;
 import parser.FuncCall;
 import parser.Gt;
 import parser.Gte;
 import parser.Id;
 import parser.IfStmt;
 import parser.List;
 import parser.Lt;
 import parser.Lte;
 import parser.Mul;
 import parser.Neq;
 import parser.Numeral;
 import parser.Opt;
 import parser.ParserVisitor;
 import parser.Procedure;
 import parser.SimpleNode;
 import parser.Start;
 import parser.Stmt;
 import parser.Sub;
 import parser.Type;
 
 public class MiniPMSNVisitor implements ParserVisitor {
 
 	@Override
 	public Object visit(SimpleNode node, Object data) {
 		return null;
 	}
 
 	@Override
 	public Object visit(Start node, Object data) {
 		return (Integer) node.getProcedure().jjtAccept(this, null);
 
 	}
 
 	@Override
 	public Object visit(Procedure node, Object data) {
 		Integer max = 0;
 		Integer current;
 		for (Stmt s : node.getStmts()) {
 			current = (Integer) s.jjtAccept(this, null);
 			if (current > max)
 				max = current;
 		}
 		return max - 1;
 	}
 
 	@Override
 	public Object visit(List node, Object data) {
 		return null;
 	}
 
 	@Override
 	public Object visit(ExprStmt node, Object data) {
 		return new Integer(1);
 	}
 
 	@Override
 	public Object visit(IfStmt node, Object data) {
 		Integer max = 0;
 		Integer current;
 		for (Stmt s : node.getIfParts())
 			if ((current = (Integer) s.jjtAccept(this, null)) > max)
 				max = current;
		for (Stmt s : node.getElseParts())
			if ((current = (Integer) s.jjtAccept(this, null)) > max)
				max = current;
 		return max + 1;
 	}
 
 	@Override
 	public Object visit(ForStmt node, Object data) {
 		Integer max = 0;
 		Integer current;
 
 		current = (Integer) node.getInit().jjtAccept(this, null);
 		max = current > max ? current : max;
 		for (Stmt s : node.getBodys())
 			if ((current = (Integer) s.jjtAccept(this, null)) > max)
 				max = current;
 
 		return max + 1;
 	}
 
 	@Override
 	public Object visit(Decl node, Object data) {
 		return new Integer(1);
 	}
 
 	@Override
 	public Object visit(FuncCall node, Object data) {
 		return new Integer(1);
 	}
 
 	@Override
 	public Object visit(Numeral node, Object data) {
 		return null;
 	}
 
 	@Override
 	public Object visit(Id node, Object data) {
 		return null;
 	}
 
 	@Override
 	public Object visit(Opt node, Object data) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Object visit(Assgn node, Object data) {
 		return new Integer(1);
 	}
 
 	@Override
 	public Object visit(Lt node, Object data) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Object visit(Gt node, Object data) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Object visit(Gte node, Object data) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Object visit(Lte node, Object data) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Object visit(Eq node, Object data) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Object visit(Neq node, Object data) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Object visit(Add node, Object data) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Object visit(Sub node, Object data) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Object visit(Mul node, Object data) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Object visit(Div node, Object data) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Object visit(Type node, Object data) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
