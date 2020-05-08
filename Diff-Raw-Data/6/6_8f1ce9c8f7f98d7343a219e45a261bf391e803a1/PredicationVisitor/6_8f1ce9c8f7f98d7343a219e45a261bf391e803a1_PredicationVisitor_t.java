 package srt.tool;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import srt.ast.AssertStmt;
 import srt.ast.AssignStmt;
 import srt.ast.AssumeStmt;
 import srt.ast.BinaryExpr;
 import srt.ast.BlockStmt;
 import srt.ast.Decl;
 import srt.ast.DeclRef;
 import srt.ast.Expr;
 import srt.ast.HavocStmt;
 import srt.ast.IfStmt;
 import srt.ast.IntLiteral;
 import srt.ast.Program;
 import srt.ast.Stmt;
 import srt.ast.TernaryExpr;
 import srt.ast.UnaryExpr;
 import srt.ast.visitor.impl.DefaultVisitor;
 
 public class PredicationVisitor extends DefaultVisitor {
 
 	private String freshVariableSeed;
 
 	private DeclRef parentPredicate;
 
 	private DeclRef globalPredicate;
 	private final String globalPredicateName;
 
 	public PredicationVisitor() {
 		super(true);
 		freshVariableSeed = "A";
 		globalPredicateName = "G";
 		globalPredicate = new DeclRef(globalPredicateName);
 	}
 
 	/**
 	 * Fresh variable generator
 	 * 
 	 * @return a fresh variable that has not been used before
 	 */
 	private String getFreshVariable(boolean isPredicate) {
 		if (freshVariableSeed.charAt(freshVariableSeed.length()-1) == 'Z') {
			freshVariableSeed += "A";
 		} else {
 			char lastChar = freshVariableSeed.charAt(freshVariableSeed.length()-1);
 			freshVariableSeed = freshVariableSeed.substring(0, freshVariableSeed.length()-1);
 			freshVariableSeed += (char) (lastChar+1);
 		}
		String result = (isPredicate) ? "$"+ freshVariableSeed : freshVariableSeed;
 		return result;
 	}
 
 	@Override
 	public Object visit(IfStmt ifStmt) {
 		List<Stmt> stmts = new LinkedList<Stmt>();
 		Expr rhs;
 		boolean hasElse = ifStmt.getElseStmt() != null;		
 		DeclRef predQ, predR = null;
 		
 		// Build Q
 		predQ = new DeclRef(getFreshVariable(true));
 		if (parentPredicate == null) {
 			rhs = ifStmt.getCondition();
 		} else {
 			rhs = new BinaryExpr(BinaryExpr.LAND, parentPredicate, ifStmt.getCondition());
 		}
 		AssignStmt predicateIfAssignStmt = new AssignStmt(predQ, rhs);
 		stmts.add(predicateIfAssignStmt);
 		
 		// Build R
 		if (hasElse) {
 			predR = new DeclRef(getFreshVariable(true));
 			if (parentPredicate == null) {
 				rhs = new UnaryExpr(UnaryExpr.LNOT, ifStmt.getCondition());
 			} else {
 				rhs = new BinaryExpr(BinaryExpr.LAND, parentPredicate, new UnaryExpr(UnaryExpr.LNOT, ifStmt.getCondition()));
 			}
 			AssignStmt predicateElseAssignStmt = new AssignStmt(predR, rhs);
 			stmts.add(predicateElseAssignStmt);
 		}
 		
 		DeclRef oldParentPredicate = parentPredicate;
 		
 		// Process IF body with the current predicate set to Q
 		parentPredicate = predQ;
 		Stmt thenStmt = (Stmt) visit(ifStmt.getThenStmt());
 		stmts.add(thenStmt);
 		
 		// Process ELSE body with the current predicate set to R
 		if (hasElse) {
 			parentPredicate = predR;		
 			Stmt elseStmt = (Stmt) visit(ifStmt.getElseStmt());
 			stmts.add(elseStmt);
 		}
 		
 		// Restore the original predicate for the scope
 		parentPredicate = oldParentPredicate;
 		return new BlockStmt(stmts, ifStmt);
 	}
 
 	@Override
 	public Object visit(Program program) {
 		// Declare a global variable G that is set to 1 (represents true)
 		Stmt declStmt = new Decl(globalPredicateName, "int");
 		Stmt assignStmt = new AssignStmt(new DeclRef(globalPredicateName), new IntLiteral(1));
 		Stmt oldBlock = (Stmt) visit(program.getBlockStmt());
 		BlockStmt newBlock = new BlockStmt(new Stmt[] {declStmt, assignStmt, oldBlock});
 		return new Program(program.getFunctionName(), program.getDeclList(), newBlock, program);
 	}
 
 	@Override
 	public Object visit(AssertStmt assertStmt) {
 		// assert(G&&P=>E)
 		Expr lhs = new UnaryExpr(UnaryExpr.LNOT, getGuard());
 		Expr rhs = assertStmt.getCondition();
 		return new AssertStmt(new BinaryExpr(BinaryExpr.LOR, lhs, rhs), assertStmt);
 	}
 
 	@Override
 	public Object visit(AssignStmt assignment) {
 		// x = (G && P) ? E : x;
 		Expr condition = getGuard();
 		Expr e = new TernaryExpr(condition, assignment.getRhs(), assignment.getLhs());
 		return new AssignStmt(assignment.getLhs(), e, assignment);
 	}
 
 	@Override
 	public Object visit(AssumeStmt assumeStmt) {
 		// A = (G && P) => E
 		DeclRef freshA = new DeclRef(getFreshVariable(true));
 		Expr lhs = new UnaryExpr(UnaryExpr.LNOT, getGuard());
 		Expr rhs = assumeStmt.getCondition();
 		Expr assignment = new BinaryExpr(BinaryExpr.LOR, lhs, rhs);
 		Stmt newStatement = new AssignStmt(freshA, assignment);
 		Stmt newG = new AssignStmt(globalPredicate, new BinaryExpr(BinaryExpr.LAND, globalPredicate, freshA));
 		return new BlockStmt(new Stmt[] { newStatement, newG}, /* basedOn= */assumeStmt);
 	}
 
 	@Override
 	public Object visit(HavocStmt havocStmt) {
 		// x = ((G && P) ? h : x)
 		DeclRef x = havocStmt.getVariable();
 		String newDeclRef = getFreshVariable(false);
 		Expr h = new DeclRef(newDeclRef);
 		Stmt declH = new Decl(newDeclRef, "int");
 		Stmt e = new AssignStmt(x, new TernaryExpr(getGuard(), h, x), havocStmt);
 		return new BlockStmt(new Stmt[] { declH, e},/* basedOn= */havocStmt);
 	}
 
 	// Returns G&&P or just G if no P exists
 	private Expr getGuard() {
 		Expr condition;
 		// Check global and parent predicate
 		if (parentPredicate != null) {
 			condition = new BinaryExpr(BinaryExpr.LAND, globalPredicate, parentPredicate);
 		} else {
 			condition = globalPredicate;
 		}
 		return condition;
 	}
 
 }
