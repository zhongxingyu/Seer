 package applets.Termumformungen$in$der$Technik_03_Logistik;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Set;
 import java.util.TreeSet;
 
 
 public class EquationSystem {
 
 	Set<String> variableSymbols = new HashSet<String>();
 	void registerVariableSymbol(String var) { variableSymbols.add(var); }	
 
 	static class Equation /*extends Expression*/ implements Comparable<Equation> {
 
 		OperatorTree left, right;
 		@Override public String toString() { return left.toString() + " = " + right.toString(); }
 		public int compareTo(Equation o) {
 			int r = left.compareTo(o.left); if(r != 0) return r;
 			return right.compareTo(o.right);
 		}
 		@Override public boolean equals(Object obj) {
 			if(!(obj instanceof Equation)) return false;
 			return compareTo((Equation) obj) == 0;
 		}
 		OperatorTree normalizedSum() { return OperatorTree.MergedEquation(left, right).normalized(); }
 		Equation normalize() { return new Equation(normalizedSum(), OperatorTree.Zero()); }
 		boolean isTautology() { return normalizedSum().isZero(); }
 		/*boolean equalNorm(Equation other) {
 			Sum myNorm = normalizedSum();
 			Sum otherNorm = other.normalizedSum();
 			if(myNorm.equals(otherNorm)) return true;
 			if(myNorm.equals(otherNorm.minusOne())) return true;
 			return false;
 		} */
 		Iterable<String> vars() { return Utils.concatCollectionView(left.vars(), right.vars()); }
 		Equation() { left = new OperatorTree(); right = new OperatorTree(); }
 		Equation(OperatorTree left, OperatorTree right) { this.left = left; this.right = right; }
 		Equation(OperatorTree ot) throws ParseError {
 			try {
 				if(ot.entities.size() == 0) throw new ParseError("Please give me an equation.", "Bitte Gleichung eingeben.");
 				if(!ot.op.equals("=")) throw new ParseError("'=' at top level required.", "'=' benötigt.");
 				if(ot.entities.size() == 1) throw new ParseError("An equation with '=' needs two sides.", "'=' muss genau 2 Seiten haben.");
 				if(ot.entities.size() > 2) throw new ParseError("Sorry, only two parts for '=' allowed.", "'=' darf nicht mehr als 2 Seiten haben.");
 				left = ot.entities.get(0).asTree();
 				right = ot.entities.get(1).asTree();
 			}
 			catch(ParseError e) {
 				e.ot = ot;
 				throw e;
 			}
 		}
 		Equation(String str) throws ParseError { this(OTParser.parse(str)); }
 		Equation(OperatorTree ot, Set<String> allowedVars) throws ParseError { this(ot); assertValidVars(allowedVars); }
 		Equation(String str, Set<String> allowedVars) throws ParseError { this(str); assertValidVars(allowedVars); }
 		
 		void assertValidVars(Set<String> allowedVars) throws ParseError {
 			for(String var : vars()) {
 				if(!allowedVars.contains(var))
 					throw new ParseError("Variable '" + var + "' is unknown.", "Die Variable '" + var + "' ist unbekannt.");
 			}
 		}
 
 		static class ParseError extends Exception {
 			private static final long serialVersionUID = 1L;
 			String english, german;
 			OperatorTree ot;
 			public ParseError(String english, String german) { super(english); this.english = english; this.german = german; }
 		}
 		
 	}
 
 	Collection<Equation> equations = new LinkedList<Equation>();
 	
 	EquationSystem() {}
 	EquationSystem(Collection<Equation> equations, Set<String> variableSymbols) {
 		this.equations = equations;
 		this.variableSymbols = variableSymbols;
 	}
 
 	Iterable<OperatorTree> normalizedSums() {
 		return Utils.map(equations, new Utils.Function<Equation,OperatorTree>() {
 			public OperatorTree eval(Equation obj) { return obj.normalizedSum(); }
 		});
 	}
 	Iterable<OperatorTree> normalizedAndReducedSums() {
 		return Utils.map(normalizedSums(), new Utils.Function<OperatorTree,OperatorTree>() {
 			public OperatorTree eval(OperatorTree obj) { return obj.reducedSum(); }
 		});
 	}
 
 	Equation add(String equStr) throws Equation.ParseError {
 		Equation eq = new Equation(equStr, variableSymbols);
 		equations.add(eq);
 		return eq;
 	}
 
 	Equation addAuto(String equStr) {
 		try {
 			Equation eq = new Equation(equStr);
 			variableSymbols.addAll(Utils.collFromIter(eq.vars()));
 			equations.add(eq);
 			return eq;
 		} catch (Equation.ParseError e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	boolean contains(Equation eq) {
 		for(Equation e : equations) {
 			if(eq.equals(e))
 				return true;
 		}
 		return false;
 	}
 	
 	boolean containsNormed(Equation eq) {
 		OperatorTree eqSum = eq.normalizedSum();
 		for(OperatorTree s : normalizedSums()) {
 			if(eqSum.equals(s) || eqSum.minusOne().equals(s))
 				return true;
 		}
 		return false;		
 	}
 	
 	// IMPORTANT:
 	// This resolution algorithm currently assumes that we have all variables != 0.
 	// This is often the case for electronic circuits (because otherwise we would have short circuits etc).
 	// If it is ever needed to handle the more general case, the functions to fix would be:
 	//  - calcAllOneSideConclusions
 	//  - OperatorTree.reducedSum
 	
 	private static int debugVerbose = 0;
 	private final static int DEPTH_LIMIT = 4;
 	
 	private static boolean _canConcludeTo(Collection<OperatorTree> baseEquationSums, OperatorTree eqSum, Set<OperatorTree> usedEquationSumList, int depth) {
 		if(debugVerbose >= 1) System.out.println(Utils.multiplyString(" ", Utils.countStackFrames("_canConcludeTo")) + "canConcludeTo: " + eqSum);
 
 		Set<OperatorTree> equationSums = new TreeSet<OperatorTree>(baseEquationSums);
 		if(equationSums.contains(eqSum)) {
 			if(debugVerbose >= 1) System.out.println(Utils.multiplyString(" ", Utils.countStackFrames("_canConcludeTo")) + "YES: eq already included");
 			return true;
 		}
 
 		if(depth > DEPTH_LIMIT) return false;		
 
 		for(OperatorTree myEq : baseEquationSums) {
 			Collection<OperatorTree> allConclusionSums = calcAllOneSideConclusions(eqSum, myEq);
 			if(allConclusionSums.isEmpty()) {
 				if(debugVerbose >= 3) System.out.println(Utils.multiplyString(" ", Utils.countStackFrames("_canConcludeTo")) + "no conclusions with " + myEq);				
 				continue;
 			}
 			
 			equationSums.remove(myEq);
 			for(OperatorTree resultingEqSum : allConclusionSums) {
 				if(usedEquationSumList.contains(resultingEqSum)) continue;
 				usedEquationSumList.add(resultingEqSum);
 				
 				if(resultingEqSum.isZero()) {
 					if(debugVerbose >= 1) System.out.println(Utils.multiplyString(" ", Utils.countStackFrames("_canConcludeTo")) + "YES: conclusion with " + myEq + " gives tautology " + resultingEqSum);
 					return true;
 				}
 				
 				if(debugVerbose >= 1) System.out.println(Utils.multiplyString(" ", Utils.countStackFrames("_canConcludeTo")) + "conclusion with " + myEq + " : " + resultingEqSum);
 				if(_canConcludeTo(equationSums, resultingEqSum, usedEquationSumList, depth + 1)) {
 					if(debugVerbose >= 1) System.out.println(Utils.multiplyString(" ", Utils.countStackFrames("_canConcludeTo")) + "YES");
 					return true;
 				}
 			}
 			equationSums.add(myEq);
 		}
 		if(debugVerbose >= 2) System.out.println(Utils.multiplyString(" ", Utils.countStackFrames("_canConcludeTo")) + "NO");		
 		return false;
 	}
 
 	boolean canConcludeTo(Equation eq) {
 		return _canConcludeTo(new TreeSet<OperatorTree>(Utils.collFromIter(normalizedAndReducedSums())), eq.normalizedSum(), new TreeSet<OperatorTree>(), 0);
 //		return _canConcludeTo(new TreeSet<Equation.Sum>(Utils.collFromIter(normalizedAndReducedSums())), eq.normalizedSum().reduce(), new TreeSet<Equation.Sum>(), 0);
 	}
 	
 	private static Set<String> commonVars(OperatorTree eq1, OperatorTree eq2) {
 		Set<String> commonVars = new HashSet<String>(Utils.collFromIter(eq1.vars()));
 		commonVars.retainAll(new HashSet<String>(Utils.collFromIter(eq2.vars())));
 		return commonVars;
 	}
 
 	private static Set<OperatorTree> calcAllOneSideConclusions(OperatorTree fixedEq, OperatorTree otherEq) {
 		Set<OperatorTree> results = new TreeSet<OperatorTree>();
 		
 		if(fixedEq.isZero()) return results;
 		if(otherEq.isZero()) return results;
 
 		Set<String> commonVars = commonVars(fixedEq, otherEq);
 		
 		for(String var : commonVars) {					
 			OperatorTree.ExtractedVar extract1 = fixedEq.extractVar(var);
 			OperatorTree.ExtractedVar extract2 = otherEq.extractVar(var);
 			if(extract1 == null) { // can happen if we have higher order polynoms
				if(debugVerbose >= 3) System.out.print("cannot extract " + var + " in " + fixedEq);
 				continue;
 			}
 			if(extract2 == null) { // can happen if we have higher order polynoms
				if(debugVerbose >= 3) System.out.print("cannot extract " + var + " in " + otherEq);
 				continue;
 			}
 
 			OperatorTree fac = extract1.varMult.divide(extract2.varMult).minusOne();
 			if(debugVerbose >= 2) System.out.print("var: " + var + " in " + otherEq);
 			if(debugVerbose >= 2) System.out.print(", fac: " + fac.debugStringDouble());
 			fac = fac.mergeDivisions().simplifyDivision();
 			if(debugVerbose >= 2) System.out.println(" -> " + fac.debugStringDouble());
 			OperatorTree newSum = otherEq.multiply(fac);
 			if(debugVerbose >= 2) System.out.println("-> " + newSum + "; in " + fixedEq + " and " + otherEq + ": extracting " + var + ": " + extract1 + " and " + extract2);
 			// NOTE: here would probably the starting place to allow vars=0.
 			//if(newSum.nextDivision() != null) { if(debugVerbose >= 3) System.out.println("newSum.nextDiv != null"); continue; }
 			
 			OperatorTree resultingEquSum = fixedEq.sum(newSum).normalized();
 			if(debugVerbose >= 3) System.out.println(".. result: " + resultingEquSum);
 			results.add(resultingEquSum);
 		}
 		
 		return results;
 	}
 		
 	void dump() {
 		System.out.println("equations: [");
 		for(Equation e : equations)
 			System.out.println("  " + e + " ,");
 		System.out.println(" ]");
 	}
 
 	void assertCanConcludeTo(String eqStr) throws Equation.ParseError {
 		Equation eq = new Equation(eqStr, variableSymbols);
 		if(!canConcludeTo(eq)) {
 			System.out.println("Error: assertCanConcludeTo failed for: " + eqStr + " // " + eq.normalizedSum());
 			System.out.println("system:");
 			for(Equation e : equations)
 				System.out.println("  " + e + " // " + e.normalize() + " // " + e.normalizedSum().reducedSum());
 
 			System.out.println("equation:");
 			debugEquationParsing(eqStr);
 			debugVerbose = 3;
 			canConcludeTo(eq);			
 			debugVerbose = 0;
 
 			throw new AssertionError("must follow: " + eq + " ; (as normed sum: " + eq.normalizedSum() + ")");
 		}
 	}
 
 	void assertCanNotConcludeTo(String eqStr) throws Equation.ParseError {
 		Equation eq = new Equation(eqStr, variableSymbols);
 		if(canConcludeTo(eq)) {
 			System.out.println("Error: assertCanNotConcludeTo failed for: " + eqStr + " // " + eq.normalizedSum());
 			System.out.println("system:");
 			for(Equation e : equations)
 				System.out.println("  " + e + " // " + e.normalize() + " // " + e.normalizedSum().reducedSum());
 			
 			System.out.println("equation:");
 			debugEquationParsing(eqStr);
 			debugVerbose = 3;
 			canConcludeTo(eq);			
 			debugVerbose = 0;
 
 			throw new AssertionError("must not follow: " + eq);
 		}
 	}
 
 	void assertAndAdd(String eqStr) throws Equation.ParseError {
 		assertCanConcludeTo(eqStr);
 		add(eqStr);
 	}
 		
 	static void debugEquationParsing(String equ) throws Equation.ParseError {
 		OperatorTree ot = OTParser.parse(equ);
 		if(!ot.op.equals("=")) throw new Equation.ParseError("'" + equ + "' must have '=' at the root.", "");
 		if(ot.entities.size() != 2) throw new Equation.ParseError("'" + equ + "' must have '=' with 2 sides.", "");		
 		OperatorTree left = ot.entities.get(0).asTree(), right = ot.entities.get(1).asTree();
 
 		System.out.println("equ (" + left.debugStringDouble() + ") = (" + right.debugStringDouble() + "): {");
 
 		// These steps match the processing in OperatorTree.normalized().
 
 		ot = OperatorTree.MergedEquation(left, right);
 		System.out.println("  merge: " + ot.debugStringDouble());
 		
 		ot = ot.transformMinusToPlus();
 		System.out.println("  transformMinusToPlus: " + ot.debugStringDouble());
 
 		ot = ot.simplify();
 		System.out.println("  simplify: " + ot.debugStringDouble());
 		
 		ot = ot.transformMinusPushedDown();
 		System.out.println("  transformMinusPushedDown: " + ot.debugStringDouble());
 		
 		ot = ot.multiplyAllDivisions();
 		System.out.println("  multiplyAllDivisions: " + ot.debugStringDouble());
 		
 		ot = ot.pushdownAllMultiplications();
 		System.out.println("  pushdownAllMultiplications: " + ot.debugStringDouble());
 
 		ot = ot.transformMinusPushedDown();
 		System.out.println("  transformMinusPushedDown: " + ot.debugStringDouble());
 
 		ot = ot.normedSum();
 		System.out.println("  normedSum: " + ot.debugStringDouble());
 
 		System.out.println("}");
 	}
 	
 	static void assertEqual(OperatorTree a, OperatorTree b) {
 		if(!a.toString().equals(b.toString()))
 			throw new AssertionError("not equal: " + a + " and " + b);
 	}
 	
 	static void assertEqual(OTEntity a, OTEntity b) {
 		if(!a.equals(b))
 			throw new AssertionError("not equal: " + a + " and " + b);
 	}
 
 	static void assertEqual(OperatorTree a, String b) {
 		if(!a.toString().equals(b))
 			throw new AssertionError("not equal: " + a + " and " + b);
 	}
 
 	static void debugSimplifications() throws Equation.ParseError {
 		assertEqual(new Equation("U1 / I1 = 1 / (1 / R2 + 1 / R3)").normalizedSum(), "I1 ∙ R2 ∙ R3 + -R2 ∙ U1 + -R3 ∙ U1");
 		assertEqual(OTParser.parse("-I1"), OTParser.parse("-I1"));
 		assertEqual(OTParser.parse("-I1").unaryPrefixedContent(), OTParser.parse("-I1").unaryPrefixedContent());
 		assertEqual(OTParser.parse("-I1 / -I1").mergeDivisions().simplifyDivision(), OperatorTree.One());
 		assertEqual(OTParser.parse("(-R1 + -R4) / (-R1 + -R4)").mergeDivisions().simplifyDivision(), OperatorTree.One());
 		assertEqual(OTParser.parse("-1 / -x").mergeDivisions().simplifyDivision(), OTParser.parse("1 / x"));
 		assertEqual(OTParser.parse("(I1 ∙ U3) / I1").mergeDivisions().simplifyDivision(), OTParser.parse("U3"));
 		assertEqual(OTParser.parse("(I2 ∙ I3) / -I2").mergeDivisions().simplifyDivision(), OTParser.parse("-I3"));
 		assertEqual(OTParser.parse("-R2 ∙ U3 / R2").mergeDivisions().simplifyDivision(), OTParser.parse("-U3"));
 		assertEqual(OTParser.parse("-I1 ∙ R1 + -I1 ∙ R4 + I2 ∙ R2 + -1").mergeDivisions(), OTParser.parse("-I1 ∙ R1 + -I1 ∙ R4 + I2 ∙ R2 + -1"));
 		assertEqual(OTParser.parse("-I1 ∙ R1 + -I1 ∙ R4 + I2 ∙ R2 + -1").mergeDivisions().simplifyDivision(), OTParser.parse("-I1 ∙ R1 + -I1 ∙ R4 + I2 ∙ R2 + -1"));
 		assertEqual(OTParser.parse("-I2 ∙ (U3 / -I2)").mergeDivisions().simplifyDivision(), OTParser.parse("U3"));		
 		assertEqual(OTParser.parse("(R1 ∙ R2 ∙ U3 + R2 ∙ R4 ∙ U3) / (-R2 ∙ U3)").simplifyDivision(), OTParser.parse("-R1 + -R4"));
 	}
 	
 	@SuppressWarnings({"ConstantConditions"})
 	static void debug() {
 		EquationSystem sys = new EquationSystem();
 		for(int i = 1; i <= 10; ++i) sys.registerVariableSymbol("x" + i);		
 		for(int i = 1; i <= 10; ++i) sys.registerVariableSymbol("U" + i);		
 		for(int i = 1; i <= 10; ++i) sys.registerVariableSymbol("R" + i);		
 		for(int i = 1; i <= 10; ++i) sys.registerVariableSymbol("I" + i);		
 		for(int i = 1; i <= 10; ++i) sys.registerVariableSymbol("C" + i);
 		for(Character c : Utils.iterableString("QUC")) sys.registerVariableSymbol("" + c);
 		try {
 			//debugEquationParsing("x3 - x4 - x5 = 0");
 			//debugEquationParsing("x1 * x5 = x2 * x5 + x3 * x5");
 			//debugEquationParsing("U3 = I1 * (R1 + R4)");
 			//debugEquationParsing("U3 / I3 = R2 - R2 * I1 / (I1 + I2)");
 			//debugEquationParsing("U1 / I1 = 1 / (1 / R2 + 1 / R3)");
 			//debugEquationParsing("I1 ∙ I4 ∙ R1 ∙ R4 + I1 ∙ I4 ∙ R1 ^ 2 + I4 ^ 2 ∙ R1 ∙ R4 + I4 ^ 2 ∙ R4 ^ 2 + -(U3 ^ 2) + I1 ∙ R1 ∙ (-I4 ∙ R1 ∙ R4 + -I4 ∙ R1 ^ 2) / (R1 + R4) + I1 ∙ R4 ∙ (-I4 ∙ R1 ∙ R4 + -I4 ∙ R1 ^ 2) / (R1 + R4) + -U3 ∙ (-I4 ∙ R1 ∙ R4 + -I4 ∙ R1 ^ 2) / (R1 + R4) = 0");
 			debugSimplifications();
 
 			sys.addAuto("Df = -r∙f∙(1 - f/k)");
 			sys.addAuto("D1f = -Df/f^2");
 			sys.assertCanConcludeTo("D1f = −r/f + r/k");
 			sys.equations.clear();
 
 			sys.addAuto("D(f) = -r∙f∙(1 - f/k)");
 			sys.addAuto("D(1/f) = -D(f)/f^2");
 			sys.assertCanConcludeTo("D(1/f) = −r/f + r/k");
 			sys.equations.clear();
 
 			sys.add("Q = C1 * U1");
 			sys.add("Q = C2 * U2");
 			sys.add("Q = C3 * U3");
 			sys.add("Q = C * U");
 			sys.add("U = U1 + U2 + U3");
 			sys.assertAndAdd("U1 = Q / C1");
 			sys.assertAndAdd("U2 = Q / C2");
 			sys.assertAndAdd("U3 = Q / C3");
 			sys.assertAndAdd("U = Q / C");
 			sys.assertAndAdd("Q / C = U1 + U2 + U3");
 			sys.assertAndAdd("Q / C = Q / C1 + U2 + U3");
 			sys.assertAndAdd("Q / C = Q / C1 + Q / C2 + U3");
 			sys.assertAndAdd("Q / C = Q / C1 + Q / C2 + Q / C3");
 			sys.assertCanConcludeTo("1 / C = 1 / C1 + 1 / C2 + 1 / C3");
 			sys.equations.clear();
 
 			sys.add("U1 / I1 = U1 / (U1 / R2 + U1 / R3)");
 			sys.assertCanConcludeTo("U1 / I1 = 1 / (1 / R2 + 1 / R3)");
 			sys.equations.clear();
 			
 			sys.add("x3 - x4 - x5 = 0");
 			sys.add("-x2 + x5 = 0");
 			sys.add("-x1 + x2 = 0");
 			sys.assertAndAdd("-x1 + x2 + x3 - x4 - x5 = 0"); // this step is needed if we have DEPTH_LIMIT=1
 			sys.assertCanConcludeTo("x1 - x3 + x4 = 0");
 			sys.assertCanNotConcludeTo("x1 = 0");
 			sys.equations.clear();
 			
 			sys.add("x1 * x2 = 0");
 			sys.add("x1 = x3");
 			sys.assertCanConcludeTo("x2 * x3 = 0");			
 			//sys.assertCanNotConcludeTo("x2 * x4 = 0"); // NOTE: this fails because the assumption that all vars!=0 does not hold!
 			sys.assertCanNotConcludeTo("x1 = x2");			
 			//sys.assertCanNotConcludeTo("x1 = 0"); // NOTE: this fails because the assumption that all vars!=0 does not hold!
 			sys.equations.clear();
 
 			sys.add("x1 = x2 + x3");
 			sys.assertCanConcludeTo("x1 / x5 = (x2 + x3) / x5");			
 			sys.assertCanConcludeTo("x1 * x5 = x2 * x5 + x3 * x5");
 			sys.equations.clear();
 
 			sys.add("x1 + x2 * x3 + x4 + x5 * x6 = 0");
 			sys.equations.clear();
 			
 			sys.add("x1 = x2 ∙ x3 - x4 - x5");
 			sys.assertCanConcludeTo("x1 / x2 = x3 - x4 / x2 - x5 / x2");
 			sys.equations.clear();
 
 			sys.add("x1 = x2 ∙ x3 - x4 - x5");
 			sys.add("x6 = x2");
 			sys.assertCanConcludeTo("x1 / x6 = x3 - x4 / x6 - x5 / x6");
 			sys.equations.clear();
 
 			sys.add("U3 = R2 ∙ I2");
 			sys.add("I3 = I4 + I2");
 			sys.add("U3 / I3 = R2 ∙ I2 / I3");
 			sys.assertCanConcludeTo("I2 = U3 / R2");
 			sys.assertCanConcludeTo("U3 / I3 = R2 ∙ I2 / (I4 + I2)");
 			sys.equations.clear();
 			
 			sys.add("I2 = I4");
 			sys.add("I4 = I3");
 			sys.assertCanConcludeTo("I3 = I2");
 			sys.equations.clear();
 			
 			sys.add("I1 = I3");
 			sys.add("I2 = I3");
 			sys.add("U2 = R1 ∙ I1 + R3 ∙ I3");
 			sys.assertCanConcludeTo("I1 = I2");
 			sys.equations.clear();
 			
 			sys.add("U3 / I3 = R2 - R2 / (1 + U3 / (R2 ∙ I1))"); // I1 ∙ R2 ∙ U3 + -I3 ∙ R2 ∙ U3 + U3^2
 			sys.add("I1 = U3 / (R1 + R4)"); // I1 ∙ R1 + I1 ∙ R4 + -U3
 			sys.assertCanConcludeTo("U3 / I3 = R2 - R2 / (1 + U3 / (R2 * U3 / (R1 + R4)))"); // -I3 ∙ R1 ∙ R2 ∙ U3 + -I3 ∙ R2 ∙ R4 ∙ U3 + R1 ∙ U3^2 + R2 ∙ U3^2 + R4 ∙ U3^2
 			sys.equations.clear();
 			
 			sys.add("U3 = R2 * I2");
 			sys.add("U3 = R4 * I4 + R1 * I1");
 			sys.add("I4 = I1");
 			sys.assertAndAdd("U3 = I1 * (R1 + R4)");
 			sys.assertAndAdd("I2 = U3 / R2");
 			sys.assertAndAdd("I1 = U3 / (R1 + R4)");
 			sys.assertAndAdd("I2 / I1 = (U3 / R2) / (U3 / (R1 + R4))");
 			sys.assertAndAdd("I2 / I1 = (R1 + R4) / R2");
 			sys.add("I1 + I2 = I3");
 			sys.assertAndAdd("U3 = R2 * (I3 - I1)");
 			sys.assertAndAdd("U3/I3 = R2 - R2 * I1 / I3");
 			sys.assertAndAdd("U3 / I3 = R2 - R2 * I1 / (I1 + I2)");
 			sys.assertAndAdd("U3 / I3 = R2 - R2 / (1 + I2 / I1)");
 			sys.assertAndAdd("U3 / I3 = R2 - R2 / (1 + (U3 / R2) / I1)");
 			sys.assertAndAdd("U3 / I3 = R2 - R2 / (1 + U3 / (R2 * I1))");
 			sys.assertAndAdd("U3 / I3 = R2 - R2 / (1 + U3 / (R2 * U3 / (R1 + R4)))");
 			sys.assertAndAdd("U3 / I3 = R2 - R2 / (1 + (R1 + R4) / R2)");
 			sys.assertAndAdd("U3 / I3 = R2 - R2 * R2 / (R2 + R1 + R4)");
 			sys.assertAndAdd("U3 / I3 = (R2*R2 + R2*R1 + R2*R4 - R2*R2) / (R2 + R1 + R4)");
 			sys.assertAndAdd("U3 / I3 = (R2*R1 + R2*R4) / (R2 + R1 + R4)");
 			sys.assertAndAdd("U3 / I3 = (R2 * (R1 + R4)) / (R2 + (R1 + R4))");
 			sys.equations.clear();
 
 			// same thing as above. just a test if we can conclude it directly. 
 			sys.add("U3 = R2 * I2");
 			sys.add("U3 = R4 * I4 + R1 * I1");
 			sys.add("I4 = I1");
 			sys.add("I1 + I2 = I3");
 			sys.assertCanConcludeTo("U3 / I3 = (R2 * (R1 + R4)) / (R2 + (R1 + R4))");
 			sys.equations.clear();
 
 			sys.add("x1 = x2");
 			sys.add("x2 * x3 = x4");
 			sys.assertCanConcludeTo("x1 = x4 / x3");
 			sys.equations.clear();
 
 			sys.add("x1 = x4 / x3"); // -> x1 * x3 = x4
 			sys.add("x2 * x3 = x4");
 			// NOTE: again, this fails because we assume that we always have all vars != 0
 			//sys.assertCanNotConcludeTo("x1 = x2"); // not because we also need x3 != 0
 			sys.equations.clear();
 
 		} catch (Equation.ParseError e) {
 			System.out.println("Error: " + e.english);
 			System.out.println(" in " + e.ot.debugStringDouble());
 			try {
 				debugEquationParsing(e.ot.toString());
 			} catch (Equation.ParseError ignored) {}
 			e.printStackTrace(System.out);
 			
 		} catch (Throwable e) {
 			e.printStackTrace(System.out);
 		}		
 	}
 }
