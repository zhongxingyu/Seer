 package compiler.imcode;
 
 import java.util.LinkedList;
 
 import compiler.abstree.AbsVisitor;
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
 import compiler.frames.FrmAccess;
 import compiler.frames.FrmArgAccess;
 import compiler.frames.FrmCmpAccess;
 import compiler.frames.FrmDesc;
 import compiler.frames.FrmFrame;
 import compiler.frames.FrmLabel;
 import compiler.frames.FrmLocAccess;
 import compiler.frames.FrmVarAccess;
 import compiler.semanal.SemDesc;
 import compiler.semanal.type.SemArrayType;
 import compiler.semanal.type.SemRecordType;
 import compiler.semanal.type.SemType;
 
 public class IMCodeGenerator implements AbsVisitor {
 	/**
 	 * Dostop do arraya: FP - offset + (type of integer * (odmik - lowbound))
 	 * Dostop do recorda: podobno kot array
 	 * 
 	 * Pozor: Bin operator od arraya vrne dejansko vrednost namesto naslova. (en
 	 * MEM prevec, ki ga je potrebno odstraniti).
 	 * 
 	 * Izracuni dreves: 1. hash tabela, 2. spremenljivka v kateri vracamo.
 	 */
 
 	/**
 	 * V pascalu imamo lahko funkcije znotraj funkcij. Kako prevesti funkcijo ki
 	 * ima znotraj sebe se 12 drugih fincij edini pameten nacin: kodo za prvo
 	 * funkcijo (brez 12 notranjih) gre v en blok assembly kode, drugo funkcijo
 	 * (eno od 12ih) damo v svoj blok itd.
 	 * 
 	 * v assembly vse te funkcije se napise zaporedno... Za vsako funkcijo zato
 	 * naredis en chunk (to so code chunki), pozor funkciji se lahko enako
 	 * imenujejo (razlicno nestano), ne smejo imeti iste labele. internim
 	 * funkcijam se daje imena, ki so sigurno unikatna
 	 * 
 	 * data chunk - namenjeni staticnim spremenljivkam - vse kar je pod program
 	 * pod vars. Vsaka spremenljivka dobi svoj chunk.
 	 * 
 	 * 
 	 */
 	public LinkedList<ImcChunk> chunks = new LinkedList<ImcChunk>();
 	private ImcCode code = null;
 	private FrmFrame currFrame;
 
 	@Override
 	public void visit(AbsAlloc acceptor) {
 		int size = SemDesc.getActualType(acceptor.type).size();
 		ImcCALL call = new ImcCALL(FrmLabel.newLabel("malloc"));
 		call.args.add(new ImcCONST(size));
 		call.args.add(new ImcCONST(size));
 		call.size.add(4);
 		call.size.add(4);
 		code = call;
 	}
 
 	@Override
 	public void visit(AbsArrayType acceptor) {
 
 	}
 
 	@Override
 	public void visit(AbsAssignStmt acceptor) {
 		acceptor.srcExpr.accept(this);
 		ImcExpr srcExpr = (ImcExpr) code;
 		acceptor.dstExpr.accept(this);
 		ImcExpr dstExpr = (ImcExpr) code;
 
 		code = new ImcMOVE(dstExpr, srcExpr);
 	}
 
 	@Override
 	public void visit(AbsAtomConst acceptor) {
 		switch (acceptor.type) {
 		case AbsAtomConst.BOOL:
 			code = new ImcCONST(acceptor.value.equals("true") ? 1 : 0);
 			break;
 		case AbsAtomConst.CHAR:
 			code = new ImcCONST((int) acceptor.value.charAt(1));
 			break;
 		case AbsAtomConst.INT:
 			code = new ImcCONST(Integer.parseInt(acceptor.value));
 			break;
 		}
 
 	}
 
 	@Override
 	public void visit(AbsAtomType acceptor) {
 
 	}
 
 	@Override
 	public void visit(AbsBinExpr acceptor) {
 
 		acceptor.fstExpr.accept(this);
 		ImcExpr fstExpr = null;
 		ImcExpr sndExpr = null;
 		fstExpr = (ImcExpr) code;
 
 		acceptor.sndExpr.accept(this);
 		if(code instanceof ImcExpr)	
 			sndExpr = (ImcExpr)code;
 		
 		if (acceptor.oper == AbsBinExpr.ARRACCESS) {
 			// AbsDecl adecl = SemDesc.getNameDecl(acceptor.fstExpr);
 			SemArrayType atype = (SemArrayType) SemDesc
 					.getActualType(acceptor.fstExpr);
 
 			code = new ImcBINOP(ImcBINOP.SUB, sndExpr, new ImcCONST(
 					atype.loBound));
 			code = new ImcBINOP(ImcBINOP.MUL, (ImcExpr) code, new ImcCONST(
 					atype.type.size()));
 
 			if (fstExpr instanceof ImcMEM)
 				fstExpr = (ImcExpr) (((ImcMEM) fstExpr).expr);
 
 			code = new ImcBINOP(ImcBINOP.ADD, fstExpr, (ImcExpr) code);
 			code = new ImcMEM((ImcExpr) code);
 		}
 		else if (acceptor.oper == AbsBinExpr.RECACCESS) { 
 			SemRecordType atype = (SemRecordType)SemDesc.getActualType(acceptor.fstExpr);
 			String fieldName = ((AbsValName)acceptor.sndExpr).name;
 			AbsDeclName declName = atype.getFieldNameDecl(fieldName);
 			FrmCmpAccess cacc = (FrmCmpAccess)FrmDesc.getAccess(SemDesc.getNameDecl(declName));
 
 			fstExpr = (ImcExpr)(((ImcMEM)fstExpr).expr);
 			code = new ImcMEM(new ImcBINOP(ImcBINOP.ADD,fstExpr,new ImcCONST(cacc.offset))); 
 		}    
 		else {
 			code = new ImcBINOP(acceptor.oper, fstExpr, sndExpr);
 		}
 	}
 
 	@Override
 	public void visit(AbsBlockStmt acceptor) {
 		acceptor.stmts.accept(this);
 	}
 
 	@Override
 	public void visit(AbsCallExpr acceptor) {
 
 		LinkedList<ImcExpr> args = new LinkedList<ImcExpr>();
 		LinkedList<Integer> sizes = new LinkedList<Integer>();
 		FrmFrame frame = FrmDesc.getFrame(SemDesc.getNameDecl(acceptor.name));
 		
 		int diff = currFrame.level - frame.level;
 		ImcExpr link = null;
 
 		link = new ImcTEMP(currFrame.FP);
 		for(int i = 0; i <= diff; i++) 
 			link = new ImcMEM(link);
 
 		args.add(link);
 		sizes.add(4);
 		for (AbsValExpr arg : acceptor.args.exprs) {
 			arg.accept(this);
 			args.add((ImcExpr) code);
 			sizes.add(SemDesc.getActualType(arg).size());
 		}
 		ImcCALL call = new ImcCALL(frame.label);
 		call.args = args;
 		call.size = sizes;
 		code = call;
 	}
 
 	@Override
 	public void visit(AbsConstDecl acceptor) {
 
 	}
 
 	@Override
 	public void visit(AbsDeclName acceptor) {
 
 	}
 
 	@Override
 	public void visit(AbsDecls acceptor) {
 		for (AbsDecl decl : acceptor.decls) {
 			if (decl instanceof AbsFunDecl) {
 				AbsFunDecl funDecl = (AbsFunDecl) decl;
 				FrmFrame frame = FrmDesc.getFrame(decl);
 				currFrame = frame;
 				funDecl.stmt.accept(this);
 				ImcCodeChunk codeChunk = new ImcCodeChunk(frame, (ImcStmt) code);
 				chunks.add(codeChunk);
 				funDecl.decls.accept(this);
 			} else if (decl instanceof AbsProcDecl) {
 				AbsProcDecl procDecl = (AbsProcDecl) decl;
 				FrmFrame frame = FrmDesc.getFrame(decl);
 				currFrame = frame;
 				procDecl.stmt.accept(this);
 				ImcCodeChunk codeChunk = new ImcCodeChunk(frame, (ImcStmt) code);
 				chunks.add(codeChunk);
 				procDecl.decls.accept(this);
 			}
 		}
 	}
 
 	@Override
 	public void visit(AbsExprStmt acceptor) {
 
 		acceptor.expr.accept(this);
 		code = new ImcEXP((ImcExpr) code);
 	}
 
 	@Override
 	public void visit(AbsForStmt acceptor) {
 
 		ImcSEQ seq = new ImcSEQ();
 
 		acceptor.name.accept(this);
 		ImcExpr varNameExpr = (ImcExpr) code;
 		acceptor.loBound.accept(this);
 		ImcExpr loBound = (ImcExpr) code;
 		seq.stmts.add(new ImcMOVE(varNameExpr, loBound));
 
 		ImcLABEL trueL = new ImcLABEL(FrmLabel.newLabel());
 		ImcLABEL startL = new ImcLABEL(FrmLabel.newLabel());
 		ImcLABEL falseL = new ImcLABEL(FrmLabel.newLabel());
 		acceptor.hiBound.accept(this);
 		ImcExpr hiBound = (ImcExpr) code;
 		code = new ImcCJUMP((ImcExpr) (new ImcBINOP(ImcBINOP.LEQ,
 				(ImcExpr) (varNameExpr), hiBound)), startL.label, falseL.label);
 		seq.stmts.add((ImcStmt) code);
 
 		seq.stmts.add((ImcStmt) trueL);
 		code = new ImcMOVE((ImcExpr) (varNameExpr), (ImcExpr) (new ImcBINOP(
 				ImcBINOP.ADD, (ImcExpr) (varNameExpr), (ImcExpr) (new ImcCONST(
 						1)))));
 		seq.stmts.add((ImcStmt) code);
 
 		seq.stmts.add((ImcStmt) startL);
 		acceptor.stmt.accept(this);
 		seq.stmts.add((ImcStmt) code);
 		code = new ImcCJUMP((ImcExpr) (new ImcBINOP(ImcBINOP.LTH,
 				(ImcExpr) (varNameExpr), hiBound)), trueL.label, falseL.label);
 		seq.stmts.add((ImcStmt) code);
 		seq.stmts.add((ImcStmt) falseL);
 
 		code = seq;
 
 	}
 
 	@Override
 	public void visit(AbsFunDecl acceptor) {
 		acceptor.decls.accept(this);
 		acceptor.stmt.accept(this);
 		FrmFrame frame = FrmDesc.getFrame(acceptor);
 		currFrame = frame;
 		ImcCodeChunk codeChunk = new ImcCodeChunk(frame, (ImcStmt)code);
 		chunks.add(codeChunk);
 
 	}
 
 	@Override
 	public void visit(AbsIfStmt acceptor) {
 
 		ImcSEQ seq = new ImcSEQ();
 		ImcLABEL trueL = new ImcLABEL(FrmLabel.newLabel());
 		ImcLABEL falseL = new ImcLABEL(FrmLabel.newLabel());
 
 		acceptor.cond.accept(this);
 		ImcExpr condExpr = (ImcExpr) code;
 
 		code = new ImcCJUMP(condExpr, trueL.label, falseL.label);
 		seq.stmts.add((ImcStmt) code);
 
 		seq.stmts.add((ImcStmt) trueL);
 		acceptor.thenStmt.accept(this);
 		seq.stmts.add((ImcStmt) code);
 
 		seq.stmts.add((ImcStmt) falseL);
 		acceptor.elseStmt.accept(this);
 		seq.stmts.add((ImcStmt) code);
 
 		code = seq;
 	}
 
 	@Override
 	public void visit(AbsNilConst acceptor) {
 		code = new ImcCONST(0);
 	}
 
 	@Override
 	public void visit(AbsPointerType acceptor) {
 
 	}
 
 	@Override
 	public void visit(AbsProcDecl acceptor) {
 		acceptor.decls.accept(this);
 		acceptor.stmt.accept(this);
 		FrmFrame frame = FrmDesc.getFrame(acceptor);
 		currFrame = frame;
 		ImcCodeChunk codeChunk = new ImcCodeChunk(frame, (ImcStmt)code);
 		chunks.add(codeChunk);  
 	}
 
 	@Override
 	public void visit(AbsProgram acceptor) {
 		FrmFrame frame = FrmDesc.getFrame(acceptor);
 		currFrame = frame;
 		acceptor.stmt.accept(this);
 		ImcCodeChunk codeChunk = new ImcCodeChunk(frame, (ImcStmt) code);
 		chunks.add(codeChunk);
 
 		for (AbsDecl decl : acceptor.decls.decls) {
 			if (decl instanceof AbsVarDecl) {
 				AbsVarDecl varDecl = (AbsVarDecl) decl;
 				FrmVarAccess access = (FrmVarAccess) FrmDesc.getAccess(varDecl);
 				SemType type = SemDesc.getActualType(varDecl.type);
 				ImcDataChunk dataChunk = new ImcDataChunk(access.label,
 						(type != null ? type.size() : 0));
 				chunks.add(dataChunk);
 			}
 		}
 		acceptor.decls.accept(this);
 
 	}
 
 	@Override
 	public void visit(AbsRecordType acceptor) {
 
 	}
 
 	@Override
 	public void visit(AbsStmts acceptor) {
 		ImcSEQ seq = new ImcSEQ();
 		for (AbsStmt stmt : acceptor.stmts) {
 			stmt.accept(this);
 			seq.stmts.add((ImcStmt) code);
 		}
 		code = seq;
 
 	}
 
 	@Override
 	public void visit(AbsTypeDecl acceptor) {
 
 	}
 
 	@Override
 	public void visit(AbsTypeName acceptor) {
 
 	}
 
 	@Override
 	public void visit(AbsUnExpr acceptor) {
 
 		acceptor.expr.accept(this);
 
 		switch (acceptor.oper) {
 		case AbsUnExpr.ADD:
 			code = new ImcBINOP(ImcBINOP.ADD, (ImcExpr) (new ImcCONST(0)),
 					(ImcExpr) code);
 			break;
 		case AbsUnExpr.SUB:
 			code = new ImcBINOP(ImcBINOP.SUB, (ImcExpr) (new ImcCONST(0)),
 					(ImcExpr) code);
 			break;
 		case AbsUnExpr.NOT:
 			code = new ImcBINOP(ImcBINOP.NEQ, (ImcExpr) code, (ImcExpr) code);
 			break;
 		case AbsUnExpr.MEM:
 			code = ((ImcMEM) code).expr;
 			break;
 		case AbsUnExpr.VAL:
 			code = new ImcMEM((ImcExpr) code);
 			break;
 		}
 	}
 
 	@Override
 	public void visit(AbsValExprs acceptor) {
 
 		for (AbsValExpr expr : acceptor.exprs)
 			expr.accept(this);
 	}
 
 	@Override
 	public void visit(AbsValName acceptor) {
 
 		FrmLabel label = null;
 		AbsDecl decl = SemDesc.getNameDecl(acceptor);
 		FrmAccess access = FrmDesc.getAccess(decl);
 
 		if (access instanceof FrmArgAccess) {
 			FrmArgAccess argAccess = (FrmArgAccess) access;
 			code = new ImcTEMP(currFrame.FP);
 			code = new ImcBINOP(ImcBINOP.ADD, (ImcExpr) code, new ImcCONST(
 					argAccess.offset));
 			code = new ImcMEM((ImcExpr) code);
 		} else if (access instanceof FrmLocAccess) {
 			FrmLocAccess laccess = (FrmLocAccess) access;
 			label = laccess.frame.label;
 			
 			int diff = currFrame.level - laccess.frame.level;
 			ImcExpr limc = null;
 			
 			limc = new ImcTEMP(currFrame.FP);
 			for(int i = 0; i < diff; i++) 
 				limc = new ImcMEM(limc);
 				
 			ImcCONST rimc = new ImcCONST(laccess.offset); 
 			ImcBINOP expr = new ImcBINOP(ImcBINOP.ADD, limc, rimc);
 			code = new ImcMEM(expr);
 		} else if (access instanceof FrmVarAccess) {
 			label = ((FrmVarAccess) access).label;
 			code = new ImcMEM(new ImcNAME(label));
 		} else if (access == null) {
 			Integer val = SemDesc.getActualConst(decl);
 
 			if (val != null)
 				code = new ImcCONST(val);
 		}
 
 	}
 
 	@Override
 	public void visit(AbsVarDecl acceptor) {
 		FrmVarAccess varAccess = (FrmVarAccess) FrmDesc.getAccess(acceptor);
 		SemType type = SemDesc.getActualType(acceptor.type);
 		ImcDataChunk dataChunk = new ImcDataChunk(varAccess.label, type.size());
 		chunks.add(dataChunk);
 	}
 
 	@Override
 	public void visit(AbsWhileStmt acceptor) {
 
 		ImcSEQ seq = new ImcSEQ();
 
 		// pogoj zanke
 		ImcLABEL trueL = new ImcLABEL(FrmLabel.newLabel());
 		ImcLABEL falseL = new ImcLABEL(FrmLabel.newLabel());
 		acceptor.cond.accept(this);
 
 		ImcExpr hiBound = (ImcBINOP) code;
 		code = new ImcCJUMP(hiBound, trueL.label, falseL.label);
 		seq.stmts.add((ImcStmt) code);
 
 		// jedro zanke
 		seq.stmts.add((ImcStmt) trueL);
 		acceptor.stmt.accept(this);
 		seq.stmts.add((ImcStmt) code);
 		code = new ImcCJUMP(hiBound, trueL.label, falseL.label);
 		seq.stmts.add((ImcStmt) code);
 		seq.stmts.add((ImcStmt) falseL);
 
 		code = seq;
 
 	}
 
 }
