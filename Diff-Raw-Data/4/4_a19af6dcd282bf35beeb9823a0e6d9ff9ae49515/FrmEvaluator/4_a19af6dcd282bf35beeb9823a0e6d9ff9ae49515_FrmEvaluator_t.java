 package compiler.frames;
 
 import compiler.abstree.AbsCallVisitor;
 import compiler.abstree.AbsEmptyVisitor;
 import compiler.abstree.tree.AbsAlloc;
 import compiler.abstree.tree.AbsArrayType;
 import compiler.abstree.tree.AbsAssignStmt;
 import compiler.abstree.tree.AbsAtomConst;
 import compiler.abstree.tree.AbsAtomType;
 import compiler.abstree.tree.AbsBinExpr;
 import compiler.abstree.tree.AbsBlockStmt;
 import compiler.abstree.tree.AbsCallExpr;
 import compiler.abstree.tree.AbsConstDecl;
 import compiler.abstree.tree.AbsDecl;
 import compiler.abstree.tree.AbsDeclName;
 import compiler.abstree.tree.AbsDecls;
 import compiler.abstree.tree.AbsExprStmt;
 import compiler.abstree.tree.AbsForStmt;
 import compiler.abstree.tree.AbsFunDecl;
 import compiler.abstree.tree.AbsIfStmt;
 import compiler.abstree.tree.AbsNilConst;
 import compiler.abstree.tree.AbsPointerType;
 import compiler.abstree.tree.AbsProcDecl;
 import compiler.abstree.tree.AbsProgram;
 import compiler.abstree.tree.AbsRecordType;
 import compiler.abstree.tree.AbsStmt;
 import compiler.abstree.tree.AbsStmts;
 import compiler.abstree.tree.AbsTypeDecl;
 import compiler.abstree.tree.AbsTypeName;
 import compiler.abstree.tree.AbsUnExpr;
 import compiler.abstree.tree.AbsValExpr;
 import compiler.abstree.tree.AbsValExprs;
 import compiler.abstree.tree.AbsValName;
 import compiler.abstree.tree.AbsVarDecl;
 import compiler.abstree.tree.AbsWhileStmt;
 import compiler.semanal.SemDesc;
 
 public class FrmEvaluator extends AbsEmptyVisitor implements AbsCallVisitor{
 
 	public void visit(AbsAtomType acceptor) {
 	}
 
 	public void visit(AbsConstDecl acceptor) {
 	}
 
 	public void visit(AbsFunDecl acceptor) {
 		FrmFrame frame = new FrmFrame(acceptor, SemDesc.getScope(acceptor));
 		for (AbsDecl decl : acceptor.pars.decls) {
 			if (decl instanceof AbsVarDecl) {
 				AbsVarDecl varDecl = (AbsVarDecl)decl;
 				FrmArgAccess access = new FrmArgAccess(varDecl, frame);
 				FrmDesc.setAccess(varDecl, access);
 			}
 			decl.accept(this);
 		}
 		for (AbsDecl decl : acceptor.decls.decls) {
 			if (decl instanceof AbsVarDecl) {
 				AbsVarDecl varDecl = (AbsVarDecl)decl;
 				FrmLocAccess access = new FrmLocAccess(varDecl, frame);
 				FrmDesc.setAccess(varDecl, access);
 			}
 			decl.accept(this);
 		}
 		frame.sizeArgs = acceptor.stmt.callVisit(this);
 		FrmDesc.setFrame(acceptor, frame);
 	}
 
 	public void visit(AbsProgram acceptor) {
 		FrmFrame frame = new FrmFrame(acceptor, -1);
 		for (AbsDecl decl : acceptor.decls.decls) {
 			if (decl instanceof AbsVarDecl) {
 				AbsVarDecl varDecl = (AbsVarDecl)decl;
 				FrmVarAccess access = new FrmVarAccess(varDecl);
 				FrmDesc.setAccess(varDecl, access);
 			}
 			decl.accept(this);
 		}
 		frame.sizeArgs = acceptor.stmt.callVisit(this);
 		FrmDesc.setFrame(acceptor, frame);
 		
 	}
 
 	public void visit(AbsProcDecl acceptor) {
 		FrmFrame frame = new FrmFrame(acceptor, SemDesc.getScope(acceptor));
 		for (AbsDecl decl : acceptor.pars.decls) {
 			if (decl instanceof AbsVarDecl) {
 				AbsVarDecl varDecl = (AbsVarDecl)decl;
 				FrmArgAccess access = new FrmArgAccess(varDecl, frame);
 				FrmDesc.setAccess(varDecl, access);
 			}
 		}
 		for (AbsDecl decl : acceptor.decls.decls) {
 			if (decl instanceof AbsVarDecl) {
 				AbsVarDecl varDecl = (AbsVarDecl)decl;
 				FrmLocAccess access = new FrmLocAccess(varDecl, frame);
 				FrmDesc.setAccess(varDecl, access);
 			}
 			decl.accept(this);
 		}
 		frame.sizeArgs = acceptor.stmt.callVisit(this);
 		FrmDesc.setFrame(acceptor, frame);
 	}
 
 	public void visit(AbsRecordType acceptor) {
 		int offset = 0;
 		for (AbsDecl decl : acceptor.fields.decls){
 			if (decl instanceof AbsVarDecl) {
 				AbsVarDecl varDecl = (AbsVarDecl)decl;
 				FrmCmpAccess access = new FrmCmpAccess(varDecl, offset);
 				FrmDesc.setAccess(varDecl, access);
 				offset = offset + SemDesc.getActualType(varDecl.type).size();
 			}
 		}
 	}
 
 	public void visit(AbsTypeDecl acceptor) {
 		if (acceptor.type instanceof AbsRecordType){
 			acceptor.type.accept(this);
 		}
 	}	
 
 	public void visit(AbsVarDecl acceptor) {
 		if (acceptor.type instanceof AbsTypeName){
 			SemDesc.getNameDecl(acceptor.type).accept(this);
 		}else{
 			acceptor.type.accept(this);
 		}
 	}
 
 
 	public void visit(AbsPointerType acceptor) {
 		
 	}
 	
 	
 	
 	@Override
 	public int callVisit(AbsAlloc acceptor) {
 		//Thread.dumpStack();
 		return 0; 
 	}
 
 	@Override
 	public int callVisit(AbsArrayType acceptor) {
 		//Thread.dumpStack();
 		return 0; 
 	}
 
 	@Override
 	public int callVisit(AbsAssignStmt acceptor) {
 		return acceptor.srcExpr.callVisit(this);
 	}
 
 	@Override
 	public int callVisit(AbsAtomConst acceptor) {
 		//Thread.dumpStack();
 		return 0; 
 	}
 
 	@Override
 	public int callVisit(AbsAtomType acceptor) {
 		//Thread.dumpStack();
 		return 0; 
 	}
 
 	@Override
 	public int callVisit(AbsBinExpr acceptor) {
 		return Math.max(acceptor.fstExpr.callVisit(this), acceptor.sndExpr.callVisit(this));
 	}
 
 	@Override
 	public int callVisit(AbsBlockStmt acceptor) {
 		return acceptor.stmts.callVisit(this);
 	}
 
 	@Override
 	public int callVisit(AbsCallExpr acceptor) {
 		int parsize = 4;
 		AbsDecl decl = SemDesc.getNameDecl(acceptor.name);
 		if (decl instanceof AbsProcDecl){
 			parsize = ((AbsProcDecl) decl).pars.decls.size()*4;
 		}
 		if (decl instanceof AbsFunDecl){
 			parsize = ((AbsFunDecl) decl).pars.decls.size()*4;
 		}
 		return parsize; //Math.max(parsize,typesize); 
 	}
 
 	@Override
 	public int callVisit(AbsConstDecl acceptor) {
 		//Thread.dumpStack();
 		return 0; 
 	}
 
 	@Override
 	public int callVisit(AbsDeclName acceptor) {
 		//Thread.dumpStack();
 		return 0; 
 	}
 
 	@Override
 	public int callVisit(AbsDecls acceptor) {
 		//Thread.dumpStack();
 		return 0; 
 	}
 
 	@Override
 	public int callVisit(AbsExprStmt acceptor) {
 		return acceptor.expr.callVisit(this);
 	}
 
 	@Override
 	public int callVisit(AbsForStmt acceptor) {
 		int res = 0;
 		res = Math.max(res, acceptor.loBound.callVisit(this));
 		res = Math.max(res, acceptor.hiBound.callVisit(this));
 		res = Math.max(res, acceptor.stmt.callVisit(this));
 		return res;
 	}
 
 	@Override
 	public int callVisit(AbsFunDecl acceptor) {
 		// TODO Auto-generated method stub
 		//Thread.dumpStack();
 		return 0; 
 	}
 
 	@Override
 	public int callVisit(AbsIfStmt acceptor) {
 		int res = 0;
 		res = Math.max(res, acceptor.cond.callVisit(this));
 		res = Math.max(res, acceptor.thenStmt.callVisit(this));
 		res = Math.max(res, acceptor.elseStmt.callVisit(this));
 		return res;
 	}
 
 	@Override
 	public int callVisit(AbsNilConst acceptor) {
 		//Thread.dumpStack();
 		return 0; 
 	}
 
 	@Override
 	public int callVisit(AbsPointerType acceptor) {
 		//Thread.dumpStack();
 		return 0; 
 	}
 
 	@Override
 	public int callVisit(AbsProcDecl acceptor) {
 		// TODO Auto-generated method stub
 		//Thread.dumpStack();
 		return 0; 
 	}
 
 	@Override
 	public int callVisit(AbsProgram acceptor) {
 		//Thread.dumpStack();
 		return 0; 
 	}
 
 	@Override
 	public int callVisit(AbsRecordType acceptor) {
 		// TODO Auto-generated method stub
 		//Thread.dumpStack();
 		return 0; 
 	}
 
 	@Override
 	public int callVisit(AbsStmts acceptor) {
 		int res = 0;
 		for (AbsStmt stmt: acceptor.stmts){
 			res = Math.max(res, stmt.callVisit(this));
 		}
 		return res; 
 	}
 
 	@Override
 	public int callVisit(AbsTypeDecl acceptor) {
 		// TODO Auto-generated method stub
 		//Thread.dumpStack();
 		return 0; 
 	}
 
 	@Override
 	public int callVisit(AbsTypeName acceptor) {
 		// TODO Auto-generated method stub
 		//Thread.dumpStack();
 		return 0; 
 	}
 
 	@Override
 	public int callVisit(AbsUnExpr acceptor) {
 		return acceptor.expr.callVisit(this); 
 	}
 
 	@Override
 	public int callVisit(AbsValExprs acceptor) {
 		int res = 0;
 		for (AbsValExpr stmt: acceptor.exprs){
 			res = Math.max(res, stmt.callVisit(this));
 		}
 		return res; 
 	}
 
 	@Override
 	public int callVisit(AbsValName acceptor) {
 		// TODO Auto-generated method stub
 		//Thread.dumpStack();
 		return 0; 
 	}
 
 	@Override
 	public int callVisit(AbsVarDecl acceptor) {
 		return acceptor.callVisit(this);
 	}
 
 	@Override
 	public int callVisit(AbsWhileStmt acceptor) {
		return Math.max(acceptor.cond.callVisit(this), acceptor.stmt.callVisit(this)); 
 	}
 }
