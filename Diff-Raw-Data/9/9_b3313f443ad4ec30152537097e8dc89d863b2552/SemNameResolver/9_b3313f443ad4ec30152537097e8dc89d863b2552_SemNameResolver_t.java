 package compiler.semanal;
 
 import compiler.abstree.AbsVisitor;
 import compiler.abstree.tree.*;
 import compiler.report.Report;
 
 public class SemNameResolver implements AbsVisitor {
 	
	public boolean error = false;
 	private boolean debug = false;
 	
 	private int record = 0;
 	private boolean notRecord() {
 		return record == 0;
 	}
 	
 	public void warningMsgRedefined(int line, String name) {
 		Report.warning(String.format("line %d: '%s' is redefined", line, name));
 		error = true;
 	}
 	
 	public void warningMsgUndefined(int line, String name) {
 		Report.warning(String.format("line %d: '%s' is undefined", line, name));
 		error = true;
 	}
 	
 	public void warningMsgDivisionByZero(int line) {
 		Report.warning(String.format("line %d: division by zero", line));
 		error = true;
 	}
 
 	@Override
 	public void visit(AbsAlloc acceptor) {
 		if(debug) System.out.println(acceptor.begLine + " AbsAlloc");
 		acceptor.type.accept(this);
 	}
 
 	@Override
 	public void visit(AbsArrayType acceptor) {
 		if(debug) System.out.println(acceptor.begLine + " AbsArrayType");
 		acceptor.type.accept(this);
 		acceptor.loBound.accept(this);
 		acceptor.hiBound.accept(this);
 	}
 
 	@Override
 	public void visit(AbsAssignStmt acceptor) {
 		if(debug) System.out.println(acceptor.begLine + " AbsAssignStmt");
 		acceptor.srcExpr.accept(this);
 		acceptor.dstExpr.accept(this);
 	}
 
 	@Override
 	public void visit(AbsAtomConst acceptor) {
 		if(debug) System.out.println(acceptor.begLine + " AbsAtomConst");
 		if(acceptor.type == AbsAtomConst.INT)
 			SemDesc.setActualConst(acceptor, Integer.parseInt(acceptor.value));
 	}
 
 	@Override
 	public void visit(AbsAtomType acceptor) {
 		if(debug) System.out.println(acceptor.begLine + " AbsAtomType");
 	}
 
 	@Override
 	public void visit(AbsBinExpr acceptor) {
 		if(debug) System.out.println(acceptor.begLine + " AbsBinExpr");
 		
 		acceptor.fstExpr.accept(this);
 		
 		if(acceptor.oper == AbsBinExpr.RECACCESS)
 			record++;
 		
 		acceptor.sndExpr.accept(this);
 
 		Integer a = SemDesc.getActualConst(acceptor.fstExpr);
 		Integer b = SemDesc.getActualConst(acceptor.sndExpr);
 		if(a != null && b != null) {
 			switch (acceptor.oper) {
 			case AbsBinExpr.ADD:
 				SemDesc.setActualConst(acceptor, a+b);
 				break;
 			case AbsBinExpr.SUB:
 				SemDesc.setActualConst(acceptor, a-b);
 				break;
 			case AbsBinExpr.MUL:
 				SemDesc.setActualConst(acceptor, a*b);
 				break;
 			case AbsBinExpr.DIV:
 				if(b==0)
 					warningMsgDivisionByZero(acceptor.begLine);
 				else
 					SemDesc.setActualConst(acceptor, a/b);
 				break;
 			}
 		}
 		
 		if(acceptor.oper == AbsBinExpr.RECACCESS)
 			record--;
 	}
 
 	@Override
 	public void visit(AbsBlockStmt acceptor) {
 		if(debug) System.out.println(acceptor.begLine + " AbsBlockStmt");
 		acceptor.stmts.accept(this);
 	}
 
 	@Override
 	public void visit(AbsCallExpr acceptor) {
 		if(debug) System.out.println(acceptor.begLine + " AbsCallExpr");
 		acceptor.name.accept(this);
 		acceptor.args.accept(this);
 	}
 
 	@Override
 	public void visit(AbsConstDecl acceptor) {
 		if(debug) System.out.println(acceptor.begLine + " AbsConstDecl");
 		try {
 			SemTable.ins(acceptor.name.name, acceptor);
 		} catch (SemIllegalInsertException e) {
 			warningMsgRedefined(acceptor.begLine, acceptor.name.name);
 		}
 		acceptor.value.accept(this);
 		
 		SemDesc.setActualConst(acceptor, SemDesc.getActualConst(acceptor.value));
 	}
 
 	@Override
 	public void visit(AbsDeclName acceptor) {
 		if(debug) System.out.println("How did u get there oO!!!");
 		//never visited
 	}
 
 	@Override
 	public void visit(AbsDecls acceptor) {
 		if(debug) System.out.println(acceptor.begLine + " AbsDecls");
 		for(AbsDecl e: acceptor.decls) {
 			e.accept(this);
 		}
 	}
 
 	@Override
 	public void visit(AbsExprStmt acceptor) {
 		if(debug) System.out.println(acceptor.begLine + " AbsExprStmt");
 		acceptor.expr.accept(this);
 	}
 
 	@Override
 	public void visit(AbsForStmt acceptor) {
 		if(debug) System.out.println(acceptor.begLine + " AbsForStmt");
 		acceptor.name.accept(this);
 		acceptor.loBound.accept(this);
 		acceptor.hiBound.accept(this);
 		acceptor.stmt.accept(this);
 	}
 
 	@Override
 	public void visit(AbsFunDecl acceptor) {
 		if(debug) System.out.println(acceptor.begLine + " AbsFunDecl");
 		SemTable.newScope();
 			try {
 				SemTable.ins(acceptor.name.name, acceptor);
 			} catch (SemIllegalInsertException e) {}		
 			acceptor.pars.accept(this);
 			acceptor.type.accept(this);
 			acceptor.decls.accept(this);
 			acceptor.stmt.accept(this);
 		SemTable.oldScope();
 		try {
 			SemTable.ins(acceptor.name.name, acceptor);
 		} catch (SemIllegalInsertException e) {
 			warningMsgRedefined(acceptor.begLine, acceptor.name.name);
 		}
 	}
 
 	@Override
 	public void visit(AbsIfStmt acceptor) {
 		if(debug) System.out.println(acceptor.begLine + " AbsIfStmt");
 		acceptor.cond.accept(this);
 		acceptor.thenStmt.accept(this);
 		acceptor.elseStmt.accept(this);
 	}
 
 	@Override
 	public void visit(AbsNilConst acceptor) {
 		if(debug) System.out.println(acceptor.begLine + " AbsNilConst");
 		//empty visitor
 	}
 
 	@Override
 	public void visit(AbsPointerType acceptor) {
 		if(debug) System.out.println(acceptor.begLine + " AbsPointerType");
 		acceptor.type.accept(this);
 	}
 
 	@Override
 	public void visit(AbsProcDecl acceptor) {
 		if(debug) System.out.println(acceptor.begLine + " AbsProcDecl");	
 		SemTable.newScope();
 			try {
 				SemTable.ins(acceptor.name.name, acceptor);
 			} catch (SemIllegalInsertException e) {}		
 			acceptor.pars.accept(this);
 			acceptor.decls.accept(this);
 			acceptor.stmt.accept(this);
 		SemTable.oldScope();
 		try {
 			SemTable.ins(acceptor.name.name, acceptor);
 		} catch (SemIllegalInsertException e) {
 			warningMsgRedefined(acceptor.begLine, acceptor.name.name);
 		}
 	}
 
 	@Override
 	public void visit(AbsProgram acceptor) {
 		if(debug) System.out.println(acceptor.begLine + " AbsProgram");
 		acceptor.decls.accept(this);
 		acceptor.stmt.accept(this);
 	}
 
 	@Override
 	public void visit(AbsRecordType acceptor) {
 		if(debug) System.out.println(acceptor.begLine + " AbsRecordType");
 		record++;
 		acceptor.fields.accept(this);
 		record--;
 	}
 
 	@Override
 	public void visit(AbsStmts acceptor) {
 		if(debug) System.out.println(acceptor.begLine + " AbsStmts");
 		for(AbsStmt e: acceptor.stmts)
 			e.accept(this);
 	}
 
 	@Override
 	public void visit(AbsTypeDecl acceptor) {
 		if(debug) System.out.println(acceptor.begLine + " AbsTypeDecl");
 		if (notRecord()) {
 			try {
 				SemTable.ins(acceptor.name.name, acceptor);
 			} catch (SemIllegalInsertException e) {
 				warningMsgRedefined(acceptor.begLine, acceptor.name.name);
 			}
 		}
 		acceptor.type.accept(this);
 	}
 
 	@Override
 	public void visit(AbsTypeName acceptor) {
 		if(debug) System.out.println(acceptor.begLine + " AbsTypeName");
 		AbsDecl decl = SemTable.fnd(acceptor.name);
 		if(decl == null) {
 			warningMsgUndefined(acceptor.begLine, acceptor.name);
 		} else {
 			SemDesc.setNameDecl(acceptor, decl);
 		}
 		
 	}
 
 	@Override
 	public void visit(AbsUnExpr acceptor) {
 		if(debug) System.out.println(acceptor.begLine + " AbsUnExpr");
 		acceptor.expr.accept(this);
 		Integer a = SemDesc.getActualConst(acceptor.expr);
 		if(a != null) {
 			switch (acceptor.oper) {
 			case AbsUnExpr.ADD:
 				SemDesc.setActualConst(acceptor, a);
 				break;
 			case AbsUnExpr.SUB:
 				SemDesc.setActualConst(acceptor, -a);
 				break;
 			}
 		}
 	}
 
 	@Override
 	public void visit(AbsValExprs acceptor) {
 		if(debug) System.out.println(acceptor.begLine + " AbsValExprs");
 		for(AbsValExpr e: acceptor.exprs)
 			e.accept(this);
 	}
 
 	@Override
 	public void visit(AbsValName acceptor) {
 		if(debug) System.out.println(acceptor.begLine + " AbsValName");
 		if(notRecord()) {
 			AbsDecl decl = SemTable.fnd(acceptor.name);
 			if(decl == null) {
 				warningMsgUndefined(acceptor.begLine, acceptor.name);
 			} else {
 				SemDesc.setNameDecl(acceptor, decl);
 				Integer a = SemDesc.getActualConst(decl);
 				if(a != null)
 					SemDesc.setActualConst(acceptor, a);
 			}
 		}
 	}
 
 	@Override
 	public void visit(AbsVarDecl acceptor) {
 		if(debug) System.out.println(acceptor.begLine + " AbsVarDecl");
 		try {
 			SemTable.ins(acceptor.name.name, acceptor);
 		} catch (SemIllegalInsertException e) {
 			warningMsgRedefined(acceptor.begLine, acceptor.name.name);
 		}
 		acceptor.type.accept(this);
 	}
 
 	@Override
 	public void visit(AbsWhileStmt acceptor) {
 		if(debug) System.out.println(acceptor.begLine + " AbsWhileStmt");
 		acceptor.cond.accept(this);
 		acceptor.stmt.accept(this);
 	}
 	
 }
