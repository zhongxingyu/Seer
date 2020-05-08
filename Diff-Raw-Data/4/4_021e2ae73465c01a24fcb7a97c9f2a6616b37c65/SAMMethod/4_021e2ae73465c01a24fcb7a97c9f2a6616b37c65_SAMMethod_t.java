 /////////////////////////////////////////////////////////////////////////
 //
 // © University of Southampton IT Innovation Centre, 2011
 //
 // Copyright in this library belongs to the University of Southampton
 // University Road, Highfield, Southampton, UK, SO17 1BJ
 //
 // This software may not be used, sold, licensed, transferred, copied
 // or reproduced in whole or in part in any manner or form or in or
 // on any media by any person other than in accordance with the terms
 // of the Licence Agreement supplied with the software, or otherwise
 // without the prior written consent of the copyright owners.
 //
 // This software is distributed WITHOUT ANY WARRANTY, without even the
 // implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 // PURPOSE, except where stated in the Licence Agreement supplied with
 // the software.
 //
 //	Created By :			Thomas Leonard
 //	Created Date :			2011-04-04
 //	Created for Project :		SERSCIS
 //
 /////////////////////////////////////////////////////////////////////////
 //
 //  License : GNU Lesser General Public License, version 2.1
 //
 /////////////////////////////////////////////////////////////////////////
 
 package eu.serscis.sam;
 
 import java.util.HashSet;
 import java.util.Set;
 import eu.serscis.sam.node.*;
 import java.util.LinkedList;
 import java.util.List;
 import org.deri.iris.api.basics.IPredicate;
 import org.deri.iris.api.basics.IRule;
 import org.deri.iris.api.basics.ITuple;
 import org.deri.iris.api.basics.ILiteral;
 import org.deri.iris.api.terms.ITerm;
 import org.deri.iris.storage.IRelation;
 import static org.deri.iris.factory.Factory.*;
 import eu.serscis.sam.parser.ParserException;
 import static eu.serscis.sam.Constants.*;
 
 class SAMMethod {
 	private Set<String> locals = new HashSet<String>();
 	private SAMClass parent;
 	private String localPrefix;
 	private AMethod method;
 	private ITerm methodNameFull;
 	private int nextCallSite = 1;
 
 
 	public SAMMethod(SAMClass parent, AMethod m, ITerm methodNameFull) throws Exception {
 		this.parent = parent;
 		this.method = m;
 		this.methodNameFull = methodNameFull;
 		this.localPrefix = methodNameFull.getValue() + ".";
 	}
 
 	private String parsePattern(PPattern parsed) {
 		if (parsed instanceof ANamedPattern) {
 			return ((ANamedPattern) parsed).getName().getText();
 		} else {
 			return "*";
 		}
 	}
 
 	public void addDatalog() throws Exception {
 		for (PAnnotation a : method.getAnnotation()) {
 			processAnnotation(a);
 		}
 
 		ACode code = (ACode) method.getCode();
 
 		// mayAccept(type, param, pos)
 		IRelation acceptRel = parent.model.getRelation(mayAccept3P);
 		AParams params = (AParams) method.getParams();
 		if (params != null) {
 			if (method.getStar() != null) {
 				addParam(methodNameFull, acceptRel, params.getParam(), -1);
 				if (params.getParamsTail().size() > 0) {
 					throw new RuntimeException("Can't have multiple parameters with *; sorry");
 				}
 			} else {
 				int pos = 0;
 				addParam(methodNameFull, acceptRel, params.getParam(), pos);
 
 				for (PParamsTail tail : params.getParamsTail()) {
 					pos += 1;
 					AParam param2 = (AParam) ((AParamsTail) tail).getParam();
 					addParam(methodNameFull, acceptRel, param2, pos);
 				}
 			}
 		}
 
 		processCode(code.getStatement());
 	}
 
 	private void processAnnotation(PAnnotation a) throws Exception {
 		TName name;
 		AStringArgs args = null;
 
 		if (a instanceof ANoargsAnnotation) {
 			ANoargsAnnotation annotation = (ANoargsAnnotation) a;
 			name = annotation.getName();
 		} else {
 			AArgsAnnotation annotation = (AArgsAnnotation) a;
 			name = annotation.getName();
 			args = (AStringArgs) annotation.getStringArgs();
 		}
 
 		ITerm[] values = getAnnotationArgs(methodNameFull, args);
 		IPredicate pred = BASIC.createPredicate(name.getText(), values.length);
 		parent.model.requireDeclared(name, pred);
 		IRelation rel = parent.model.getRelation(pred);
 
 		rel.add(BASIC.createTuple(values));
 	}
 
 	public List<ILiteral> processJavaDl(PTerm value, ALiterals parsed) throws ParserException {
 		final Set<String> javaVars = new HashSet<String>();
 		final List<ILiteral> extraLiterals = new LinkedList<ILiteral>();
 
 		TermProcessor termFn = new TermProcessor() {
 			public ITerm process(PTerm term) throws ParserException {
 				if (term instanceof AJavavarTerm) {
 					TName tname = ((AJavavarTerm) term).getName();
 					ITerm var = TERM.createVariable("Java_" + tname.getText());
 					extraLiterals.add(getValue(var, tname));
 					return var;
 				}
 				return null;
 			}
 		};
 
 		List<ILiteral> literals = parent.model.parseLiterals(parsed, termFn);
		ITerm term = parent.model.parseTerm(value, termFn);
 
 		literals.addAll(extraLiterals);
 
 		/* local(?Caller, ?CallerInvocation, 'var', ?Value) :-
 		 *	isA(?Caller, type),
 		 *	live(?Caller, ?CallerInvocation, method),
 		 *	?Value = term,
 		 *	lits.
 		 */
 		ILiteral isA = BASIC.createLiteral(true, BASIC.createAtom(isAP, BASIC.createTuple(
 					TERM.createVariable("Caller"),
 					TERM.createString(this.parent.name))));
 		ILiteral live = BASIC.createLiteral(true, BASIC.createAtom(liveMethodP, BASIC.createTuple(
 					TERM.createVariable("Caller"),
 					TERM.createVariable("CallerInvocation"),
 					methodNameFull)));
 		ILiteral eq = BASIC.createLiteral(true,
 				BUILTIN.createEqual(
 					TERM.createVariable("Value"),
					term));
 
 		literals.add(isA);
 		literals.add(live);
 		literals.add(eq);
 		return literals;
 	}
 
 	private void processCode(List<PStatement> statements) throws Exception {
 		for (PStatement ps : statements) {
 			if (ps instanceof AAssignStatement) {
 				AAssignStatement s = (AAssignStatement) ps;
 
 				AAssign assign = (AAssign) s.getAssign();
 				PExpr expr = (PExpr) s.getExpr();
 
 				String callSite = methodNameFull.getValue() + "-" + nextCallSite;
 				nextCallSite++;
 
 				// hasCallSite(methodFull, callSite).
 				IRelation rel = parent.model.getRelation(hasCallSiteP);
 				rel.add(BASIC.createTuple(methodNameFull, TERM.createString(callSite)));
 
 				IPredicate valueP = null;
 
 				if (expr instanceof ACallExpr) {
 					ACallExpr callExpr = (ACallExpr) expr;
 
 					// mayCallObject(?Caller, ?CallerInvocation, ?CallSite, ?Value) :-
 					//	isA(?Caller, ?Type),
 					//	liveMethod(?Caller, ?CallerInvocation, method),
 					//	value(?Caller, ?CallerInvocation, ?TargetVar, ?Value).
 
 					ITuple tuple = BASIC.createTuple(
 							TERM.createVariable("Caller"),
 							TERM.createVariable("CallerInvocation"),
 							TERM.createString(callSite),
 							TERM.createVariable("Value"));
 
 					ILiteral head = BASIC.createLiteral(true, BASIC.createAtom(mayCallObjectP, tuple));
 
 					ILiteral isA = BASIC.createLiteral(true, BASIC.createAtom(isAP, BASIC.createTuple(
 								TERM.createVariable("Caller"),
 								TERM.createString(this.parent.name))));
 					ILiteral liveMethod = BASIC.createLiteral(true, BASIC.createAtom(liveMethodP, BASIC.createTuple(
 								TERM.createVariable("Caller"),
 								TERM.createVariable("CallerInvocation"),
 								methodNameFull)));
 
 					IRule rule = BASIC.createRule(makeList(head), makeList(isA, liveMethod, getValue(callExpr.getName())));
 					//System.out.println(rule);
 					parent.model.rules.add(rule);
 
 					addArgs(callSite, (AArgs) callExpr.getArgs(), callExpr.getStar());
 
 					// callsMethod(callSite, method)
 					String targetMethod = parsePattern(callExpr.getMethod());
 					if ("*".equals(targetMethod)) {
 						rel = parent.model.getRelation(callsAnyMethodP);
 						rel.add(BASIC.createTuple(TERM.createString(callSite)));
 					} else {
 						rel = parent.model.getRelation(callsMethodP);
 						rel.add(BASIC.createTuple(TERM.createString(callSite), TERM.createString(targetMethod)));
 					}
 
 					valueP = didGetP;
 				} else if (expr instanceof ANewExpr) {
 					ANewExpr newExpr = (ANewExpr) expr;
 
 					String varName = assign.getName().getText();
 
 					// mayCreate(callSite, newType, name)
 					rel = parent.model.getRelation(mayCreateP);
 					String newType = ((AType) newExpr.getType()).getName().getText();
 					rel.add(BASIC.createTuple(TERM.createString(callSite),
 								  TERM.createString(newType),
 								  TERM.createString(varName)));
 
 					addArgs(callSite, (AArgs) newExpr.getArgs(), newExpr.getStar());
 
 					valueP = didCreateP;
 				} else if (expr instanceof ACopyExpr) {
 					// a = b
 					ACopyExpr copyExpr = (ACopyExpr) expr;
 
 					if (assign == null) {
 						throw new RuntimeException("Pointless var expression");
 					}
 
 					ILiteral isA = BASIC.createLiteral(true, BASIC.createAtom(isAP,
 								BASIC.createTuple(
 									TERM.createVariable("Caller"),
 									TERM.createString(parent.name))));
 
 					TName sourceVar = copyExpr.getName();
 
 					String targetVar = assign.getName().getText();
 					boolean assigningToLocal = assign.getType() != null || locals.contains(targetVar);
 
 					if (assigningToLocal && !locals.contains(sourceVar)) {
 						// (need to limit CallerInvocation in this case)
 						ILiteral isInvocation = BASIC.createLiteral(true, BASIC.createAtom(isInvocationP,
 										BASIC.createTuple(TERM.createVariable("CallerInvocation"))));
 						assignVar(assign, makeList(isA, isInvocation, getValue(sourceVar)));
 					} else {
 						assignVar(assign, makeList(isA, getValue(sourceVar)));
 					}
 				} else {
 					throw new RuntimeException("Unknown expr type: " + expr);
 				}
 
 				if (assign != null && valueP != null) {
 					ITuple tuple = BASIC.createTuple(
 							TERM.createVariable("Caller"),
 							TERM.createVariable("CallerInvocation"),
 							TERM.createString(callSite),
 							TERM.createVariable("Value"));
 					ILiteral value = BASIC.createLiteral(true, BASIC.createAtom(valueP, tuple));
 
 					assignVar(assign, makeList(value));
 				}
 			} else if (ps instanceof ADeclStatement) {
 				ADeclStatement decl = (ADeclStatement) ps;
 				declareLocal(decl.getType(), decl.getName());
 			} else if (ps instanceof AReturnStatement) {
 				boolean returnsVoid = "void".equals(((AType) method.getType()).getName().getText());
 				TName expr = ((AReturnStatement) ps).getName();
 				if (returnsVoid && expr.getText() != null) {
 					throw new ParserException(expr, "Return with a value in method declared to return void!");
 				}
 				returnOrThrow(mayReturnP, methodNameFull, expr);
 			} else if (ps instanceof AThrowStatement) {
 				returnOrThrow(mayThrowP, methodNameFull, ((AThrowStatement) ps).getName());
 			} else if (ps instanceof ATryStatement) {
 				ATryStatement ts = (ATryStatement) ps;
 				processCode(ts.getStatement());
 				for (PCatchBlock c : ts.getCatchBlock()) {
 					ACatchBlock cb = (ACatchBlock) c;
 					declareLocal(cb.getType(), cb.getName());
 
 					// local(?Object, ?Innovation, name, ?Exception) :-
 					//	didGetException(?Object, ?Invocation, ?CallSite, ?Exception),
 					//	hasCallSite(?Method, ?CallSite).
 					// (note: we currently catch all exceptions from this method, not just those in the try block)
 					ILiteral head = BASIC.createLiteral(true, BASIC.createAtom(localP,
 								BASIC.createTuple(
 									TERM.createVariable("Object"),
 									TERM.createVariable("Invocation"),
 									TERM.createString(expandLocal(cb.getName().getText())),
 									TERM.createVariable("Exception"))));
 					ILiteral didGetException = BASIC.createLiteral(true, BASIC.createAtom(didGetExceptionP,
 								BASIC.createTuple(
 									TERM.createVariable("Object"),
 									TERM.createVariable("Invocation"),
 									TERM.createVariable("CallSite"),
 									TERM.createVariable("Exception"))));
 					ILiteral hasCallSite = BASIC.createLiteral(true, BASIC.createAtom(hasCallSiteP,
 								BASIC.createTuple(
 									methodNameFull,
 									TERM.createVariable("CallSite"))));
 					IRule rule = BASIC.createRule(makeList(head), makeList(didGetException, hasCallSite));
 					//System.out.println(rule);
 					parent.model.rules.add(rule);
 
 					processCode(cb.getStatement());
 				}
 			} else if (ps instanceof AAssignDlStatement) {
 				AAssignDlStatement s = (AAssignDlStatement) ps;
 
 				AAssign assign = (AAssign) s.getAssign();
 				PTerm term = s.getTerm();
 				List<ILiteral> lits = processJavaDl(term, (ALiterals) s.getLiterals());
 
 				assignVar(assign, lits);
 			} else {
 				throw new RuntimeException("Unknown statement type: " + ps);
 			}
 		}
 	}
 
 	private void returnOrThrow(IPredicate pred, ITerm methodNameFull, TName name) throws ParserException {
 		// mayReturn(?Target, ?TargetInvocation, ?Method, ?Value) :-
 		//	isA(?Target, name),
 		//	liveMethod(?Target, ?TargetInvocation, ?Method),
 		//	(value)
 		ITuple tuple = BASIC.createTuple(
 				// XXX: badly named: should be Target, but getValue uses "Caller"
 				TERM.createVariable("Caller"),
 				TERM.createVariable("CallerInvocation"),
 				methodNameFull,
 				TERM.createVariable("Value"));
 
 		ILiteral head = BASIC.createLiteral(true, BASIC.createAtom(pred, tuple));
 
 		ILiteral isA = BASIC.createLiteral(true, BASIC.createAtom(isAP, BASIC.createTuple(
 					TERM.createVariable("Caller"),
 					TERM.createString(this.parent.name))));
 		ILiteral live = BASIC.createLiteral(true, BASIC.createAtom(liveMethodP, BASIC.createTuple(
 					TERM.createVariable("Caller"),
 					TERM.createVariable("CallerInvocation"),
 					methodNameFull)));
 
 		IRule rule = BASIC.createRule(makeList(head), makeList(isA, live, getValue(name)));
 		//System.out.println(rule);
 		parent.model.rules.add(rule);
 	}
 
 	private void declareLocal(AAssign assign) throws ParserException {
 		AType type = (AType) assign.getType();
 		if (type != null) {
 			declareLocal(type, assign.getName());
 		}
 	}
 
 	private void declareLocal(PType type, TName aName) throws ParserException {
 		String name = aName.getText();
 		if (locals.contains(name)) {
 			throw new ParserException(aName, "Duplicate definition of local " + name);
 		} else if (parent.fields.contains(name)) {
 			throw new ParserException(aName, "Local variable shadows field of same name: " + name);
 		} else {
 			locals.add(name);
 		}
 	}
 
 	/* Assign a local or field, as appropriate:
 	 *   local(?Caller, ?CallerInvocation, 'var', ?Value) :- body
 	 * or
 	 *   field(?Caller, 'var', ?Value) :- body
 	 */
 	private void assignVar(AAssign assign, List<ILiteral> body) throws ParserException {
 		ILiteral head;
 
 		declareLocal(assign);
 
 		String varName = assign.getName().getText();
 		if (locals.contains(varName)) {
 			ITuple tuple = BASIC.createTuple(TERM.createVariable("Caller"),
 							 TERM.createVariable("CallerInvocation"),
 							 TERM.createString(expandLocal(varName)),
 							 TERM.createVariable("Value"));
 			head = BASIC.createLiteral(true, BASIC.createAtom(localP, tuple));
 		} else if (parent.fields.contains(varName)) {
 			ITuple tuple = BASIC.createTuple(TERM.createVariable("Caller"),
 							 TERM.createString(varName),
 							 TERM.createVariable("Value"));
 			head = BASIC.createLiteral(true, BASIC.createAtom(fieldP, tuple));
 		} else {
 			throw new RuntimeException("Undeclared variable: " + varName);
 		}
 
 		IRule rule = BASIC.createRule(makeList(head), body);
 		//System.out.println(rule);
 		parent.model.rules.add(rule);
 	}
 
 	private void addParam(ITerm method, IRelation acceptRel, PParam param, int pos) throws ParserException {
 		String name = ((AParam) param).getName().getText();
 		acceptRel.add(BASIC.createTuple(method, TERM.createString(expandLocal(name)), CONCRETE.createInt(pos)));
 
 		if (locals.contains(name)) {
 			throw new ParserException(((AParam) param).getName(), "Duplicate definition of local " + name);
 		} else if (parent.fields.contains(name)) {
 			throw new ParserException(((AParam) param).getName(), "Local variable shadows field of same name: " + name);
 		} else {
 			locals.add(name);
 		}
 	}
 
 	/* maySend(?Target, ?TargetInvocation, ?Method, ?Pos, ?Value) :-
 	 * 	didCall(?Caller, ?CallerInvocation, ?CallSite, ?Target, ?TargetInvocation, ?Method),
 	 *	local|field,
 	 */
 	private void addArg(String callSite, int pos, PExpr expr) throws ParserException {
 		ILiteral didCall = BASIC.createLiteral(true, didCallP, BASIC.createTuple(
 					TERM.createVariable("Caller"),
 					TERM.createVariable("CallerInvocation"),
 					TERM.createString(callSite),
 					TERM.createVariable("Target"),
 					TERM.createVariable("TargetInvocation"),
 					TERM.createVariable("Method")
 					));
 
 		IRule rule;
 
 		if (expr instanceof AStringExpr) {
 			ILiteral head = BASIC.createLiteral(true, maySendP, BASIC.createTuple(
 						TERM.createVariable("Target"),
 						TERM.createVariable("TargetInvocation"),
 						TERM.createVariable("Method"),
 						CONCRETE.createInt(pos),
 						TERM.createString(getString(((AStringExpr) expr).getStringLiteral()))
 						));
 
 			rule = BASIC.createRule(makeList(head), makeList(didCall));
 		} else {
 			TName varName = ((ACopyExpr) expr).getName();
 			ILiteral head = BASIC.createLiteral(true, maySendP, BASIC.createTuple(
 						TERM.createVariable("Target"),
 						TERM.createVariable("TargetInvocation"),
 						TERM.createVariable("Method"),
 						CONCRETE.createInt(pos),
 						TERM.createVariable("Value")
 						));
 
 			rule = BASIC.createRule(makeList(head), makeList(didCall, getValue(varName)));
 		}
 
 		parent.model.rules.add(rule);
 		//System.out.println(rule);
 	}
 
 	private void addArgs(String callSite, AArgs args, TStar star) throws ParserException {
 		if (args == null) {
 			if (star != null) {
 				throw new ParserException(star, "No argument for *");
 			}
 			return;
 		}
 		int pos = 0;
 		PExpr arg0 = args.getExpr();
 
 		if (star != null) {
 			addArg(callSite, -1, arg0);
 			if (args.getArgsTail().size() != 0) {
 				throw new ParserException(star, "Can't use multiple arguments with *; sorry");
 			}
 			return;
 		}
 
 		addArg(callSite, pos, arg0);
 
 		for (PArgsTail tail : args.getArgsTail()) {
 			pos += 1;
 			PExpr arg = ((AArgsTail) tail).getExpr();
 			addArg(callSite, pos, arg);
 		}
 	}
 
 	private ITerm[] getAnnotationArgs(ITerm methodName, AStringArgs args) throws ParserException {
 		if (args == null) {
 			return new ITerm[] {methodName};
 		}
 
 		int n = args.getStringArgsTail().size() + 2;
 		ITerm[] values = new ITerm[n];
 
 		values[0] = methodName;
 
 		int pos = 1;
 		values[pos] = TERM.createString(getString(args.getStringLiteral()));
 
 		for (PStringArgsTail tail : args.getStringArgsTail()) {
 			pos += 1;
 			String value = getString(((AStringArgsTail) tail).getStringLiteral());
 			values[pos] = TERM.createString(value);
 		}
 
 		return values;
 	}
 
 	private ILiteral getValue(TName var) throws ParserException {
 		return getValue(TERM.createVariable("Value"), var);
 	}
 
 	/* Returns
 	 *   local(?Caller, ?CallerInvocation, 'var', targetVar)
 	 * or
 	 *   field(?Caller, 'var', targetVar)
 	 * or
 	 *   equals(?Caller, targetVar)  (for "this")
 	 * depending on whether varName refers to a local or a field.
 	 */
 	private ILiteral getValue(ITerm targetVar, TName var) throws ParserException {
 		String sourceVar = var.getText();
 		if (locals.contains(sourceVar)) {
 			ITuple tuple = BASIC.createTuple(
 					TERM.createVariable("Caller"),
 					TERM.createVariable("CallerInvocation"),
 					TERM.createString(expandLocal(sourceVar)),
 					targetVar);
 			return BASIC.createLiteral(true, BASIC.createAtom(localP, tuple));
 		} else if (parent.fields.contains(sourceVar)) {
 			ITuple tuple = BASIC.createTuple(
 					TERM.createVariable("Caller"),
 					TERM.createString(sourceVar),
 					targetVar);
 			return BASIC.createLiteral(true, BASIC.createAtom(fieldP, tuple));
 		} else if (sourceVar.equals("this")) {
 			return BASIC.createLiteral(true, BUILTIN.createEqual(
 					TERM.createVariable("Caller"),
 					targetVar));
 		} else {
 			throw new ParserException(var, "Unknown variable " + sourceVar);
 		}
 	}
 
 	private String expandLocal(String local) {
 		return localPrefix + local;
 	}
 }
