 /**
  * Created on 07.02.2011
  */
 package edu.kit.asa.alloy2key.key;
 
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 
 import edu.kit.asa.alloy2key.key.TermBinOp.Op;
 import edu.kit.asa.alloy2key.modules.KeYModule;
 import edu.kit.asa.alloy2key.util.Util;
 
 /**
  * capturing output to a .key file
  * 
  * @author Ulrich Geilmann
  *
  */
 public class KeYFile {
 
 	public KeYFile () {
 		includes = new LinkedList<String>();
 		sorts = new LinkedList<String>();
 		funcs  =  new LinkedList<String>();
 		preds  = new LinkedList<String>();
 		rules  = new LinkedList<Taclet>();
 		assump = new LinkedList<Term>();
 		concl  = new LinkedList<Term>();
 		asserts = new LinkedList<Term>();
 	}
 	
 	/** referred modules */
 	public Queue<KeYModule> modules = KeYModule.NIL;
 	
 	/**
 	 * Add a function declaration (free text)
 	 * @param fun
 	 * declaration in SMT syntax, e.g. (declare-fun MyFun bool);
 	 */
 	public void addFunction (String fun) {
 		funcs.add(fun);
 	}
 	
 	/**
 	 * Add a sort declaration (free text)
 	 * @param sortDeclaration
 	 * declaration in SMT/Z3 syntax, e.g. (declare-sort MySort);
 	 * @return TRUE if successfully added, FALSE if omitted
 	 */
 	public boolean addSort (String sortDeclaration) {
 		if(!sorts.contains(sortDeclaration)){
 			sorts.add(sortDeclaration);
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 	
 	/**
 	 * Add a typed function declaration with parameters
 	 * @return TRUE if successfully added, FALSE if omitted
 	 * @throws ModelException 
 	 */
 	public boolean addFunction(String type, String name, String...params) {		
 		String checkedParams = params == null ? "" : Util.join(params, " ");
 		String fun = String.format("(declare-fun %s (%s) %s)", name, checkedParams, type);
 		if(!funcs.contains(fun)){
 			funcs.add(fun);
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 	
 	/** 
 	 * Add an SMT assertion (assert expression)
 	 * @param term 
 	 * Expression to be asserted (declare in SMT syntax)
 	 */
 	public void addAssertion(Term term) {
 		asserts.add(term);
 	}	
 	
 	/**
 	 * Add a predicate declaration
 	 * @param pred
 	 * declaration in KeY syntax, e.g. p(Rel1);
 	 */
 	public void addPredicate (String pred) {
 		preds.add(pred);
 	}
 	
 	/**
 	 * Add a rule definition
 	 * @param rule
 	 * the taclet object
 	 */
 	public void addRule (Taclet rule) {
 		rules.add(rule);
 	}
 	
 	/**
 	 * Add an assumption
 	 * @param f
 	 * formula in KeY syntax to be added as assumption
 	 */
 	public void addAssumption (Term f) {
 		if (f != Term.TRUE)
 			assump.add(f);
 	}
 	
 	/**
 	 * Add a conclusion
 	 * @param f
 	 * formula in KeY syntax to be added as conclusion
 	 */
 	public void addConclusion (Term f) {
 		concl.add(f);
 	}
 	
 	/**
 	 * Add an include statement
 	 * @param f
 	 * the filename to include
 	 */
 	public void addInclude (String f) {
 		includes.add(f);
 	}
 	
 	private Collection<String> sorts;
 	private Collection<Term> asserts;
 	private Collection<String> includes;
 	private Collection<String> funcs;
 	private Collection<String> preds;
 	private Collection<Taclet> rules;
 	private Collection<Term> assump;
 	private Collection<Term> concl;
 	
 	public void output(OutputStream os) {
 		PrintStream out = new PrintStream(os);
 		//printTheory(out);
 //		out.println ("\\include \"theory/alloyHeader.key\";");
 //		for (String s : includes)
 //			out.println ("\\include \""+s+"\";");
 //		for (KeYModule m: modules) {
 //			out.println ("\\include \"theory/"+m.filename()+"\";");
 //		}
 		out.println (";; sorts");
 		out.println (Util.join(sorts, "\n"));
 		out.println (";; --end sorts\n");
 		out.println (";; functions");
 		out.println (Util.join(funcs, "\n"));
 		out.println (";; --end functions\n");
 		out.println (";; assertions");
 		for (Term a : asserts) {
 			out.println (String.format("(assert\n  %s\n)", a.toString()));
 		}
 		out.println (";; --end assertions\n");
 		
 		
 		out.println (";; -- key stuff for debugging --");
 //		out.println ("\\rules {");
 //		out.println (Util.join(rules, "\n"));
 //		out.println ("}\n");
 		out.println (";\\problem {(");
 		out.print(";");
 		out.println (Util.join(assump, " &\n"));
 		out.println (";)-> (");
 		out.print(";");
 		out.println (Util.join(concl, " &\n"));
 		out.println (";;\\predicates {");
 		out.println (Util.join(preds, "\n"));
 		out.println (";;}\n");
 		out.println (";; -- END key stuff --");
 		
 		out.println ("(check-sat)");
 		out.close();
 	}
 
 	/** adds declaration and theory for Atom */
 	public void declareAtom() {
 		this.addSort("(declare-sort Atom)");
 	}
 	
 	/** adds a declaration and theory for disjoint 
 	 * @param arity arity of the expression
 	 */
 	public void declareDisjoint(int ar) {
 		declareAtom();
 		declareRel(ar);
 		String relAr = "Rel" + ar;
 		this.addFunction("Bool", "disjoint_"  +ar, relAr, relAr);
 		
 		// forall a in A, b in B | not a in B.
 		// or maybe: forall a in A, b in B | not a in B and not b in A...?
 		TermVar a, b, A, B;
 		a = TermVar.var("Atom", "a");
 		b = TermVar.var("Atom", "b");
 		A = TermVar.var(relAr, "A");
 		B = TermVar.var(relAr, "B");
 		Term aInA = Term.reverseIn(B, b);
 		Term bInB = Term.reverseIn(A, a);
 		Term typeRestriction = aInA.and(bInB);		
 		Term notAinB = new TermUnary(TermUnary.Op.NOT, Term.reverseIn(B, a));		
		Term axiom = Term.call("disjoint_", A, B).iff(typeRestriction.and(notAinB)).forall(a, A, b, B);;
 		this.addAssertion(axiom); // add this axiom to the list of assertions
 	}
 
 	/** adds a declaration and theory for subset and subrel 
 	 * @param arity of the set expression
 	 */
 	public void declareSubset(int ar) {
 		// declare prerequisite sorts
 		declareAtom();
 		declareRel(ar);
 		// declare subset function, e.g. (declare-fun subset_2 (Rel2, Rel2) bool)
 		// add declaration
 		String relSort = "Rel"+ar;
 		if(this.addFunction("Bool", "subset_" + ar, relSort, relSort)){			
 			// if successfully added; add axiom(s) as well
 			// prepare parameter list for in()
 			List<TermVar> atomVars = new LinkedList<TermVar>();
 			for(int i = 0; i < ar; i++){
 				atomVars.add(TermVar.var("Atom", "a" + i));
 			}
 			// declare variables
 			TermVar x = TermVar.var(relSort, "x");	// a Set or Relation
 			TermVar y = TermVar.var(relSort, "y");	// another Set or Relation
 			Term subset = Term.call("subset_"+ar, x, y);  // x is subset of y
 			Term inImpliesIn = new TermBinOp(Term.in(atomVars, x), Op.IMPLIES, Term.in(atomVars, y));  // if an atom is in x, it is also in y 
 			// now quantify the two expressions for all x, y and atoms
 			List<TermVar> quantVars = new LinkedList<TermVar>(atomVars);
 			quantVars.add(x);
 			quantVars.add(y);
 			Term axiom = Term.forall(quantVars, (Term)new TermBinOp(subset, Op.IFF, inImpliesIn));
 			this.addAssertion(axiom);  // add this axiom to the list of assertions
 		}
 	}
 
 	private void declareRel(int ar) {
 		this.addSort("(declare-sort Rel" + ar + ")");
 	}
 
 	/** adds a declaration and theory for the converter function 
 	 * @param arity arity of the expression
 	 */
 	public void declareA2r(int ar) {
 		// declare prerequisite sorts and functions
 		declareAtom();
 		declareRel(ar);
 		declareIn(ar);
 		// prepare declaration
 		String[] params = new String[ar];
 		for(int i = 0; i < ar; i++){
 			params[i] = "Atom";
 		}
 		// declaration		
 		if(this.addFunction("Rel" + ar, "a2r_" + ar, params)){
 			// TODO: axiom of higher arity
 			// axiom 
 			Term xInX, inImpliesEqual;
 			Term x = TermVar.var("x");
 			Term y = TermVar.var("y");
 			
 			xInX = Term.call("in_1", x, Term.call("a2r_" + ar, x));
 			inImpliesEqual = new TermBinOp(Term.call("in_1", y, Term.call("a2r_" + ar, x)), Op.IMPLIES, new TermBinOp(x, Op.EQUALS, y));
 			Term axiom = Term.forall("Atom", "x", xInX.and(Term.forall("Atom", "y", inImpliesEqual)));
 					
 			this.addAssertion(axiom);
 		}	
 	}
 
 	/** adds a declaration for in (no theory) 
 	 * @param arity arity of the expression
 	 * @throws ModelException couldn't declare this add this function because of "addFunction"
 	 */
 	public void declareIn(int ar)   {
 		declareAtom();
 		declareRel(ar);
 		String[] params = new String[ar + 1];		
 		for(int i = 0; i < ar; i++)
 			params[i] = "Atom";
 		params[ar]  = "Rel" + ar;
 		this.addFunction("Bool", "in_" + ar, params);
 		// axiom omitted: "in" does not have an axiom (uninterpreted)
 	}
 
 	/** adds a declaration and theory for "none" 
 	 * @param arity arity of none
 	 */
 	public void declareNone(int ar) {
 		this.addFunction("Bool", "none_" + ar, "Rel" + ar);
 		//TODO: add axiom
 	}
 
 	public void declareProduct(int lar, int rar) throws ModelException {
 		if(lar < 1 || rar < 1)
 			throw new ModelException("The product is not defined for arguments of arity 0.");
 		// careful with overloaded + operator
 		this.addFunction("Rel" + /*concatenation*/ (lar + /*sum*/ rar), String.format("prod_%dx%d", lar, rar), "Rel"+lar, "Rel"+rar);
 		//TODO: add axiom
 	}
 
 	public void declareJoin(int lar, int rar) {
 		this.addFunction("Bool", "join_" + lar + "x" + rar);
 		// TODO: add axiom
 	}
 
 	public void declareUnion(int ar) {
 		declareRel(ar);
 		this.addFunction("Rel" + ar, "union_" + ar);	
 		// TODO: add axiom
 	}
 
 	public void declareOne(int ar)  {		
 		declareRel(ar);
 		this.addFunction("Bool", "one_" + ar, "Rel" + ar);
 		// TODO: add axiom
 	}
 
 	public void declareLone(int ar) {
 		declareRel(ar);
 		this.addFunction("Bool", "lone_" + ar, "Rel" + ar);
 		//TODO: add axiom
 	}
 
 	public void declareSome(int ar) {
 		declareRel(ar);
 		this.addFunction("Bool", "some_" + ar, "Rel" + ar);
 		//TODO: add axiom
 	}
 
 	// arity is always 2
 	public void declareTranspose() {
 		declareRel(2);
 		this.addFunction("Rel2", "transpose", "Rel2");
 		//TODO: add axiom
 	}
 
 	// arity is always 2
 	public void declareTransitiveClosure() {
 		declareRel(2);
 		this.addFunction("Rel2", "tCl", "Rel2");
 		//TODO: add axiom
 	}
 
 	public void declareReflexiveTransitiveClosure() {
 		declareRel(2);
 		this.addFunction("Rel2", "rtCl", "Rel2");
 		//TODO: add axiom
 	}
 
 	public void declareCardinality(int ar) {
 		declareRel(ar);
 		this.addFunction("Int", "card_" + ar, "Rel" + ar);
 		//TODO: add axiom
 	}
 
 	/** Declares the domain restriction operator
 	 * @param rar arity of the right-hand parameter
 	 * @rem left-hand arity is required to be 1 (type:set)
 	 */
 	public void declareDomainRestriction(int rar) {
 		declareRel(1);
 		declareRel(rar);
 		String relar = "Rel"+rar;
 		this.addFunction(relar, "domRestr_" + rar, "Rel1", relar);
 		// TODO Auto-generated method stub
 	}
 
 	public void declareDifference(int ar) {
 		declareRel(ar);
 		String relar = "Rel"+ar;
 		this.addFunction(relar, "diff_" + ar, relar, relar);
 		//TODO: add axiom
 	}
 
 	public void declareOverride(int ar) {
 		declareRel(ar);
 		String relar = "Rel"+ar;
 		this.addFunction(relar, "overr_" + ar, relar, relar);
 		//TODO: add axiom
 	}
 
 	public void declareIntersection(int ar) {
 		declareRel(ar);
 		String relar = "Rel"+ar;
 		this.addFunction(relar, "inter_" + ar, relar, relar);
 		//TODO: add axiom
 	}
 
 	/** Declares the range restriction operator
 	 * @param lar arity of the left-hand parameter
 	 * @rem right-hand arity is required to be 1 (type:set)
 	 */
 	public void declareRangeRestriction(int lar) {
 		declareRel(1);
 		declareRel(lar);
 		String relar = "Rel"+lar;
 		this.addFunction(relar, "rangeRestr_" + lar, relar, "Rel1");
 		//TODO: add axiom
 	}
 
 	public void declareIdentity() {
 		declareRel(2);		
 		this.addFunction("Rel"+2, "iden");
 		//TODO: add axiom
 	}
 }
