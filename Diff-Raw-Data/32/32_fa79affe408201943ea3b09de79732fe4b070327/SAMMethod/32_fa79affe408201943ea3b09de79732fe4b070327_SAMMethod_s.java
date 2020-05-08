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
 	private Set<String> callSites = new HashSet<String>();
 	private List<String> callSitesInCurrentTryBlock = null;	// null => not in a try block
 
 	public SAMMethod(SAMClass parent, AMethod m, ITerm methodNameFull) throws Exception {
 		this.parent = parent;
 		this.method = m;
 		this.methodNameFull = methodNameFull;
 		this.localPrefix = methodNameFull.getValue() + ".";
 	}
 
 	/* This is just used to name the call-site. */
 	private String parsePattern(PPattern parsed) {
 		if (parsed instanceof ANamedPattern) {
 			return ((ANamedPattern) parsed).getName().getText();
 		} else if (parsed instanceof AAnyPattern) {
 			return "*";
 		} else if (parsed instanceof ADollarPattern) {
 			return "$" + ((ADollarPattern) parsed).getName().getText();
 		} else {
 			throw new RuntimeException("Unknown pattern: " + parsed);
 		}
 	}
 
 	public void addDatalog() throws Exception {
 		for (PAnnotation a : method.getAnnotation()) {
 			processAnnotation((AAnnotation) a);
 		}
 
 		PPattern methodPattern = method.getName();
 		if (methodPattern instanceof ADollarPattern) {
			/* local(?Object, ?Context, var, ?MethodName) :-
			 *		didCall(?Caller, ?CallerInvocation, ?CallSite, ?Object, ?Context, method),
                         *              callsMethod(?CallSite, ?MethodName).
			 */
 			String varName = ((ADollarPattern) methodPattern).getName().getText();
			ILiteral head = BASIC.createLiteral(true, localP, BASIC.createTuple(
						TERM.createVariable("Object"),
						TERM.createVariable("Context"),
						TERM.createString(expandLocal(varName)),
						TERM.createVariable("MethodName")));
			ILiteral didCall = BASIC.createLiteral(true, didCallP, BASIC.createTuple(
						TERM.createVariable("Caller"),
						TERM.createVariable("CallerInvocation"),
						TERM.createVariable("CallSite"),
						TERM.createVariable("Object"),
						TERM.createVariable("Context"),
						methodNameFull));
			ILiteral callsMethod = BASIC.createLiteral(true, callsMethodP, BASIC.createTuple(
						TERM.createVariable("CallSite"),
						TERM.createVariable("MethodName")));
			IRule rule = BASIC.createRule(makeList(head), makeList(didCall, callsMethod));
			parent.model.addRule(rule);
 			locals.add(varName);
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
 
 	private void processAnnotation(AAnnotation annotation) throws Exception {
 		TName name;
 		ITerm[] values;
 
 		PAtom a = annotation.getAtom();
 		List<Token> tokens = new LinkedList<Token>();
 
 		if (a instanceof ANullaryAtom) {
 			ANullaryAtom nullary = (ANullaryAtom) a;
 			name = nullary.getName();
 			tokens.add(name);
 			tokens.add(null);
 			values = new ITerm[1];
 		} else {
 			ANormalAtom normal = (ANormalAtom) a;
 			name = normal.getName();
 			tokens.add(name);
 			tokens.add(null);
 			ITuple terms = parent.model.parseTerms((ATerms) normal.getTerms(), null, tokens);
 
 			values = new ITerm[terms.size() + 1];
 
 			for (int i = 1; i < values.length; i++) {
 				values[i] = terms.get(i - 1);
 			}
 		}
 
 		values[0] = methodNameFull;
 
 		IPredicate pred = BASIC.createPredicate(name.getText(), values.length);
 		parent.model.requireDeclared(name, pred);
 		parent.model.addFact(pred, BASIC.createTuple(values), tokens);
 	}
 
 	public List<ILiteral> processJavaDl(PTerm value, ALiterals parsed, List<List<Token>> tokens) throws ParserException {
 		final Set<String> javaVars = new HashSet<String>();
 		final List<ILiteral> extraLiterals = new LinkedList<ILiteral>();
 		final boolean[] need_caller = {false};
 
 		TermProcessor termFn = new TermProcessor() {
 			public ITerm process(PTerm term, List<Token> tokenOut) throws ParserException {
 				if (term instanceof AJavavarTerm) {
 					TName tname = ((AJavavarTerm) term).getName();
 					ITerm var = TERM.createVariable("Java_" + tname.getText());
 					extraLiterals.add(getValue(var, tname));
 					tokenOut.add(tname);
 					return var;
 				} else if (term instanceof AVarTerm) {
 					// Rename user variables to avoid conflicts
 					TName tname = ((AVarTerm) term).getName();
 					String name = tname.getText();
 					if (name.equals("CallerInvocation")) {
 						throw new ParserException(tname, "Possible old use of ?CallerInvocation; use $Context instead");
 					}
 					ITerm var = TERM.createVariable("User_" + name);
 					tokenOut.add(tname);
 					return var;
 				} else if (term instanceof ASpecialTerm) {
 					TName tname = ((ASpecialTerm) term).getName();
 					String name = tname.getText();
 					tokenOut.add(tname);
 					if (name.equals("Context")) {
 						return TERM.createVariable("CallerInvocation");
 					} else if (name.equals("Caller")) {
 						need_caller[0] = true;
 						return TERM.createVariable("CallerOfThisMethod");
 					} else {
 						throw new ParserException(tname, "Unknown special variable: " + tname);
 					}
 				}
 				return null;
 			}
 		};
 
 		List<ILiteral> literals = parent.model.parseLiterals(parsed, termFn, tokens);
 		ITerm term = parent.model.parseTerm(value, termFn, new LinkedList<Token>());
 
 		literals.addAll(extraLiterals);
 		for (int i = extraLiterals.size(); i > 0; i--) {
 			tokens.add(null);
 		}
 
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
 
 		tokens.add(null);
 		tokens.add(null);
 		tokens.add(null);
 
 		if (need_caller[0]) {
 			// didCall(?CallerOfThisMethod, ?Caller_AnyInvocation, ?Caller_CallSite, ?Caller, ?CallerInvocation, method)
 
 			ILiteral didCall = BASIC.createLiteral(true, didCallP, BASIC.createTuple(
 						TERM.createVariable("CallerOfThisMethod"),
 						TERM.createVariable("Caller_AnyInvocation"),
 						TERM.createVariable("Caller_CallSite"),
 						TERM.createVariable("Caller"),			// == this, not our caller! (badly named)
 						TERM.createVariable("CallerInvocation"),	// == our invocation
 						methodNameFull));
 			literals.add(didCall);
 			tokens.add(null);
 		}
 
 		return literals;
 	}
 
 	private String mintCallSite(AAssign optAssign, String base) {
 		String prefix = methodNameFull.getValue() + ":";
 		if (optAssign != null) {
 			prefix += optAssign.getName().getText() + "=";
 		}
 		prefix += base;
 		String result = prefix;
 		int i = 0;
 
 		while (callSites.contains(result)) {
 			i++;
 			result = prefix + "-" + i;
 		}
 
 		callSites.add(result);
 
 		return result;
 	}
 
 	private void processCode(List<PStatement> statements) throws Exception {
 		for (PStatement ps : statements) {
 			if (ps instanceof AAssignStatement) {
 				AAssignStatement s = (AAssignStatement) ps;
 
 				AAssign assign = (AAssign) s.getAssign();
 				PExpr expr = (PExpr) s.getExpr();
 
 				String callSite = null;
 
 				IPredicate valueP = null;
 
 				if (expr instanceof ACallExpr) {
 					ACallExpr callExpr = (ACallExpr) expr;
 					String targetMethod = parsePattern(callExpr.getMethod());
 					callSite = mintCallSite(assign, callExpr.getName().getText() + "." + targetMethod);
 
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
 					parent.model.addRule(rule);
 
 					addArgs(callSite, (AArgs) callExpr.getArgs(), callExpr.getStar());
 
 					// callsMethod(callSite, method)
 					if (callExpr.getMethod() instanceof ADollarPattern) {
 						TName varName = ((ADollarPattern) (callExpr.getMethod())).getName();
 						// callsMethodInLocal(?CallSite, ?LocalVarName).
 						if (!locals.contains(varName.getText())) {
 							throw new ParserException(varName, "Must be a local variable");
 						}
 						parent.model.addFact(callsMethodInLocalP,
 									BASIC.createTuple(
 										TERM.createString(callSite),
 										TERM.createString(expandLocal(varName.getText()))));
 					} else if ("*".equals(targetMethod)) {
 						IRelation callsMethod = parent.model.getRelation(callsMethodP);
 						callsMethod.add(BASIC.createTuple(TERM.createString(callSite), new AnyTerm(Type.StringT)));
 					} else {
 						IRelation callsMethod = parent.model.getRelation(callsMethodP);
 						callsMethod.add(BASIC.createTuple(TERM.createString(callSite), TERM.createString(targetMethod)));
 					}
 
 					valueP = didGetP;
 				} else if (expr instanceof ANewExpr) {
 					ANewExpr newExpr = (ANewExpr) expr;
 					callSite = mintCallSite(assign, "new-" + ((AType) newExpr.getType()).getName().getText());
 
 					String varName = assign.getName().getText();
 
 					// mayCreate(callSite, newType, name)
 					IRelation rel = parent.model.getRelation(mayCreateP);
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
 
 					ILiteral liveMethod = BASIC.createLiteral(true, BASIC.createAtom(liveMethodP, BASIC.createTuple(
 								TERM.createVariable("Caller"),
 								TERM.createVariable("CallerInvocation"),
 								methodNameFull)));
 
 					TName sourceVar = copyExpr.getName();
 
 					assignVar(assign, makeList(liveMethod, getValue(sourceVar)));
 				} else if (expr instanceof ABoolExpr) {
 					if (assign == null) {
 						throw new RuntimeException("Pointless constant expression: " + expr);
 					}
 					String bool = ((ABoolExpr) expr).getBool().getText();
 					assignConstant(assign, CONCRETE.createBoolean(Boolean.valueOf(bool)));
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
 
 				if (callSite != null) {
 					// hasCallSite(methodFull, callSite).
 					IRelation rel = parent.model.getRelation(hasCallSiteP);
 					rel.add(BASIC.createTuple(methodNameFull, TERM.createString(callSite)));
 
 					if (callSitesInCurrentTryBlock != null) {
 						callSitesInCurrentTryBlock.add(callSite);
 					}
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
 				boolean catchesThrowable = false;
 
 				ATryStatement ts = (ATryStatement) ps;
 
 				if (callSitesInCurrentTryBlock != null) {
 					throw new ParserException(ts.getTry(), "Nested try statements not supported");
 				}
 				callSitesInCurrentTryBlock = new LinkedList<String>();
 
 				processCode(ts.getStatement());
 				for (PCatchBlock c : ts.getCatchBlock()) {
 					ACatchBlock cb = (ACatchBlock) c;
 					TName type = ((AType) cb.getType()).getName();
 					if (type.getText().equals("Throwable")) {
 						catchesThrowable = true;
 						type = new TName("Object");
 					}
 					declareLocal(type, cb.getName());
 
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
 					parent.model.addRule(rule);
 
 					processCode(cb.getStatement());
 				}
 
 				if (catchesThrowable) {
 					IRelation rel = parent.model.getRelation(catchesAllExceptionsP);
 					for (String callSite : callSitesInCurrentTryBlock) {
 						rel.add(BASIC.createTuple(TERM.createString(callSite)));
 					}
 				}
 
 				callSitesInCurrentTryBlock = null;
 			} else if (ps instanceof AAssignDlStatement) {
 				AAssignDlStatement s = (AAssignDlStatement) ps;
 
 				AAssign assign = (AAssign) s.getAssign();
 				PTerm term = s.getTerm();
 				List<List<Token>> tokens = new LinkedList<List<Token>>();
 				List<ILiteral> lits = processJavaDl(term, (ALiterals) s.getLiterals(), tokens);
 
 				assignVar(assign, lits, tokens);
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
 		parent.model.addRule(rule);
 	}
 
 	private void declareLocal(AAssign assign) throws ParserException {
 		AType type = (AType) assign.getType();
 		if (type != null) {
 			declareLocal(type, assign.getName());
 		}
 	}
 
 	private void declareLocal(PType type, TName aName) throws ParserException {
 		declareLocal(((AType) type).getName(), aName);
 	}
 
 	private void declareLocal(TName type, TName aName) throws ParserException {
 		Type.validateJavaName(type);
 		String name = aName.getText();
 		if (locals.contains(name)) {
 			throw new ParserException(aName, "Duplicate definition of local " + name);
 		} else if (parent.fields.contains(name)) {
 			throw new ParserException(aName, "Local variable shadows field of same name: " + name);
 		} else {
 			locals.add(name);
 		}
 	}
 
 	private void assignVar(AAssign assign, List<ILiteral> body) throws ParserException {
 		assignVar(assign, body, null);
 	}
 
 	/* Assign a local or field, as appropriate:
 	 *   local(?Caller, ?CallerInvocation, 'var', ?Value) :- body
 	 * or
 	 *   field(?Caller, 'var', ?Value) :- body
 	 */
 	private void assignVar(AAssign assign, List<ILiteral> body, List<List<Token>> tokens) throws ParserException {
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
 			throw new ParserException(assign.getName(), "Undeclared variable: " + varName);
 		}
 
 		List<List<Token>> allTokens;
 		if (tokens == null) {
 			allTokens = null;
 		} else {
 			allTokens = new LinkedList<List<Token>>();
 			allTokens.add(null);
 			allTokens.addAll(tokens);
 		}
 
 		IRule rule = BASIC.createRule(makeList(head), body);
 		//System.out.println(rule);
 		parent.model.addRule(rule, allTokens);
 	}
 
 	/* Assign a local or field, as appropriate:
 	 *   local(?Object, ?Invocation, 'var', value) :- liveMethod(?Object, ?Invocaiton, method).
 	 * or
 	 *   field(?Object, 'var', value) :- liveMethod(?Object, ?Invocaiton, method).
 	 */
 	private void assignConstant(AAssign assign, ITerm value) throws ParserException {
 		ILiteral head;
 
 		declareLocal(assign);
 
 		String varName = assign.getName().getText();
 		if (locals.contains(varName)) {
 			ITuple tuple = BASIC.createTuple(TERM.createVariable("Object"),
 							 TERM.createVariable("Invocation"),
 							 TERM.createString(expandLocal(varName)),
 							 value);
 			head = BASIC.createLiteral(true, BASIC.createAtom(localP, tuple));
 		} else if (parent.fields.contains(varName)) {
 			ITuple tuple = BASIC.createTuple(TERM.createVariable("Object"),
 							 TERM.createString(varName),
 							 value);
 			head = BASIC.createLiteral(true, BASIC.createAtom(fieldP, tuple));
 		} else {
 			throw new ParserException(assign.getName(), "Undeclared variable: " + varName);
 		}
 
 		ILiteral live = BASIC.createLiteral(true, BASIC.createAtom(liveMethodP, BASIC.createTuple(
 							 TERM.createVariable("Object"),
 							 TERM.createVariable("Invocation"),
 							 methodNameFull)));
 
 		IRule rule = BASIC.createRule(makeList(head), makeList(live));
 		//System.out.println(rule);
 		parent.model.addRule(rule);
 	}
 
 	// pos can be -1 if we accept arguments at any position
 	private void addParam(ITerm method, IRelation acceptRel, PParam pparam, int pos) throws ParserException {
 		ITerm posTerm = pos == -1 ? AnyTerm.ANY_INT : CONCRETE.createInt(pos);
 		AParam param = (AParam) pparam;
 		String name = param.getName().getText();
 		TName type = ((AType) param.getType()).getName();
 		Type.validateJavaName(type);
 		acceptRel.add(BASIC.createTuple(method, TERM.createString(expandLocal(name)), posTerm));
 
 		IRelation hasParamRel = parent.model.getRelation(Constants.hasParamP);
 		hasParamRel.add(BASIC.createTuple(method,
 						  TERM.createString(type.getText()),
 						  TERM.createString(name),
 						  posTerm));
 
 		if (locals.contains(name)) {
 			throw new ParserException(((AParam) param).getName(), "Duplicate definition of local " + name);
 		} else if (parent.fields.contains(name)) {
 			throw new ParserException(((AParam) param).getName(), "Local variable shadows field of same name: " + name);
 		} else {
 			locals.add(name);
 		}
 	}
 
 	/* maySend(?Caller, ?CallSite, ?Pos, ?Value) :-
 	 *	string|field|this.
 	 *
 	 * maySend(?Caller, ?CallerInvocation, ?CallSite, ?Pos, ?Value) :-
 	 *	local.
 	 */
 	private void addArg(String callSite, int pos, PExpr expr) throws ParserException {
 		ITerm posTerm = pos == -1 ? AnyTerm.ANY_INT : CONCRETE.createInt(pos);
 		IRule rule;
 
 		if (expr instanceof ACopyExpr) {
 			TName varName = ((ACopyExpr) expr).getName();
 			boolean isLocal = locals.contains(varName.getText());
 
 			ILiteral head;
 
 			if (isLocal) {
 				head = BASIC.createLiteral(true, maySend5P, BASIC.createTuple(
 						TERM.createVariable("Caller"),
 						TERM.createVariable("CallerInvocation"),
 						TERM.createString(callSite),
 						posTerm,
 						TERM.createVariable("Value")));
 			} else {
 				head = BASIC.createLiteral(true, maySendFromAnyContextP, BASIC.createTuple(
 						TERM.createVariable("Caller"),
 						TERM.createString(callSite),
 						posTerm,
 						TERM.createVariable("Value")));
 			}
 
 			if (varName.getText().equals("this")) {
 				ILiteral isA = BASIC.createLiteral(true, isAP, BASIC.createTuple(
 							TERM.createVariable("Caller"),
 							TERM.createString(this.parent.name)));
 				rule = BASIC.createRule(makeList(head), makeList(getValue(varName), isA));
 			} else {
 				rule = BASIC.createRule(makeList(head), makeList(getValue(varName)));
 			}
 		} else {
 			ITerm constant;
 			if (expr instanceof AStringExpr) {
 				constant = TERM.createString(getString(((AStringExpr) expr).getStringLiteral()));
 			} else if (expr instanceof AIntExpr) {
 				String value = ((AIntExpr) expr).getNumber().getText();
 				constant = CONCRETE.createInteger(Integer.valueOf(value));
 			} else if (expr instanceof ANullExpr) {
 				return;			// we always assume null can be sent anyway
 			} else {
 				boolean val = Boolean.valueOf(((ABoolExpr) expr).getBool().getText());
 				constant = CONCRETE.createBoolean(val);
 			}
 
 			ILiteral head = BASIC.createLiteral(true, maySendFromAnyContextP, BASIC.createTuple(
 					TERM.createVariable("Caller"),
 					TERM.createString(callSite),
 					posTerm,
 					constant));
 
 			ILiteral isA = BASIC.createLiteral(true, isAP, BASIC.createTuple(
 						TERM.createVariable("Caller"),
 						TERM.createString(this.parent.name)));
 			rule = BASIC.createRule(makeList(head), makeList(isA));
 		}
 
 		parent.model.addRule(rule);
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
