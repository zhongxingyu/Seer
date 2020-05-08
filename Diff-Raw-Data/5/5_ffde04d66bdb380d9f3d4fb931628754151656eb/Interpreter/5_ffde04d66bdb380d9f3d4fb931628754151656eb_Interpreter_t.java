 /*
  * Created on 24.jun.2005
  *
  * Copyright (c) 2004, Karl Trygve Kalleberg <karltk@ii.uib.no>
  * 
  * Licensed under the IBM Common Public License, v1.0
  */
 package org.spoofax.interp;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import jjtraveler.TopDown;
 import jjtraveler.VisitFailure;
 import aterm.AFun;
 import aterm.ATerm;
 import aterm.ATermAppl;
 import aterm.ATermInt;
 import aterm.ATermList;
 import aterm.Visitor;
 import aterm.pure.PureFactory;
 
 public class Interpreter extends ATermed {
 
     HashMap<String, ExtStrategy> externalStrategies;
 
     private ATerm current;
 
     private VarScope varScope;
 
     private DefScope defScope;
 
     public Interpreter() {
         factory = new PureFactory();
 
         externalStrategies = new HashMap<String, ExtStrategy>();
 
         for (ExtStrategy s : Library.getStrategies())
             externalStrategies.put(s.getName(), s);
 
         reset();
     }
 
     public void load(String path) throws IOException, FatalError {
         load(factory.readFromFile(path));
     }
 
     private static class MatchCollector extends Visitor {
 
         List<ATerm> coll = null;
 
         PureFactory factory;
 
         String needle;
 
         MatchCollector(PureFactory factory, String needle) {
             this.factory = factory;
             coll = new ArrayList<ATerm>();
             this.needle = needle;
         }
 
         @Override
         public void visitATerm(ATerm t) throws VisitFailure {
 
             List<ATerm> ats = (List<ATerm>) t.match(needle);
             if (ats != null) {
                 coll.addAll(ats);
             }
         }
 
         List<ATerm> getResult() {
             return coll;
         }
     }
 
     private ATermList collect(String needle, ATerm haystack) {
 
         MatchCollector c = new MatchCollector(factory, needle);
         jjtraveler.Visitor v = new TopDown(c);
 
         try {
             v.visit(haystack);
         } catch (VisitFailure e) {
             e.printStackTrace();
         }
         ATermList r = factory.makeList();
         for (ATerm t : c.getResult())
             r = r.append(t);
         return r;
     }
 
     public void load(ATerm prg) throws FatalError {
         debug("load()");
 
         ATermList x = collect("Constructors(<term>)", prg);
         ATermList constr = Tools.listAt(x, 0);
         for (int i = 0; i < constr.getLength(); i++) {
             ATermAppl t = Tools.applAt(constr, i);
             if (t.getName().equals("OpDecl")) {
                 Constructor c = new Constructor(t);
                 defScope.addConstructor(c);
             } else if (t.getName().equals("OpDeclInj")) {
 
             } else
                 throw new FatalError("Unknown constructor type '" + t.getName()
                         + "'");
         }
 
         ATermList strat = Tools.listAt(collect("Strategies(<term>)", prg), 0);
         for (int i = 0; i < strat.getLength(); i++) {
             Strategy s = StrategyFactory.create(Tools.applAt(strat, i),
                                                 defScope, varScope);
             defScope.addStrategy(s);
         }
     }
 
     public void reset() {
         factory.cleanup();
 
         varScope = new VarScope(null);
         defScope = new DefScope(null);
 
         for (Strategy s : externalStrategies.values())
             defScope.addStrategy(s);
 
         current = makeTerm("[]");
 
     }
 
     private boolean eval(ATermAppl t) throws FatalError {
         String type = t.getName();
         if (type.equals("All"))
             return evalAll(t);
         else if (type.equals("One"))
             return evalOne(t);
         else if (type.equals("Some"))
             return evalSome(t);
         else if (type.equals("Prim"))
             return evalPrim(t);
         else if (type.equals("Build"))
             return evalBuild(t);
         else if (type.equals("Bagof"))
             return evalBagof(t);
         else if (type.equals("GuardedLChoice"))
             return evalGuardedLChoice(t);
         else if (type.equals("LGChoice"))
             return evalLGChoice(t);
         else if (type.equals("Seq"))
             return evalSeq(t);
         else if (type.equals("Scope"))
             return evalScope(t);
         else if (type.equals("Build"))
             return evalBuild(t);
         else if (type.equals("Id"))
             return evalId(t);
         else if (type.equals("Fail"))
             return evalFail(t);
         else if (type.equals("CallT"))
             return evalCall(t);
         else if (type.equals("Let"))
             return evalLet(t);
         else if (type.equals("Match"))
             return evalMatch(t);
         else if (type.equals("PrimT"))
             return evalPrim(t);
 
         throw new FatalError("Unknown construct '" + type + "'");
     }
 
     private boolean evalMatch(ATermAppl t) throws FatalError {
         debug("evalMatch");
         debug(" term   : " + t);
         debug(" current: " + current);
 
         ATermAppl p = (ATermAppl) t.getChildAt(0);
         List<Pair<String, ATerm>> r = match(current, p);
 
         debug(" !" + current + " ; ?" + p);
         debug(" types : " + current.getType() + " " + t.getType());
         debug(" annos : " + current.getAnnotations() + " " + t.getAnnotations());
 
         if (r != null) {
             debug(" results : " + r);
 
             
             debug(" will bind:");
             varScope.dumpScope(" ");
             boolean b = bindVars(r);
             debug(" bound vars:");
             varScope.dumpScope(" ");
             return b;
         }
         debug(" no match!");
         return false;
     }
 
     private boolean bindVars(List<Pair<String, ATerm>> r) {
         for (Pair<String, ATerm> x : r) {
             VarScope s = varScope.scopeOf(x.first);
             if (s == null) {
                 varScope.add(x.first, x.second);
             } else if (s.hasVarInLocalScope(x.first)) {
                 ATerm t = s.lookup(x.first);
                 boolean eq = t.match(x.second) != null;
                 if (!eq) {
                     debug(x.first + " already bound to " + t + ", new: "
                             + x.second);
                     return eq;
                 }
             } else {
                 s.add(x.first, x.second);
             }
         }
         return true;
     }
 
     private boolean evalOne(ATermAppl t) throws FatalError {
         debug("evalOne()");
         throw new FatalError("Not implemented!");
     }
 
     private boolean evalAll(ATermAppl t) throws FatalError {
         debug("evalAll()");
         throw new FatalError("Not implemented!");
     }
 
     private boolean evalSome(ATermAppl t) throws FatalError {
         debug("evalSome()");
         throw new FatalError("Not implemented!");
     }
 
     private boolean evalPrim(ATermAppl t) throws FatalError {
         debug("evalPrim");
         debug(" term : " + t);
         // Check if we have the external strategy on record
 
         String n = Tools.stringAt(t, 0);
         ExtStrategy s = externalStrategies.get(n);
         if (s == null)
             throw new FatalError("Calling non-existent primitive :" + n);
 
         ATermList actualSVars = Tools.listAt(t, 1);
         ATermList actualTVars = Tools.listAt(t, 2);
 
         // Lookup variables in the argument lest
 
         ATermList realTVars = factory.makeList();
         for (int i = 0; i < actualTVars.getLength(); i++) {
             ATermAppl appl = Tools.applAt(actualTVars, i);
             if (appl.getName().equals("Var")) {
                 realTVars = realTVars.append(varScope.lookup(Tools
                         .stringAt(appl, 0)));
             } else {
                 realTVars = realTVars.append(appl);
             }
         }
         debug(" args : " + realTVars);
         for (int i = 0; i < realTVars.getLength(); i++)
             debug("  " + realTVars.getChildAt(i).getClass());
 
         boolean r = s.invoke(this, actualSVars, realTVars);
         if (!r)
             debug("failed");
         return r;
     }
 
     private boolean evalBagof(ATermAppl t) throws FatalError {
         debug("evalBagof()");
         throw new FatalError("Not implemented!");
     }
 
     private boolean evalGuardedLChoice(ATermAppl t) throws FatalError {
         debug("evalGuardedLChoice()");
         debug(" " + t);
         BindingInfo bi = varScope.saveUnboundVars();
         boolean cond = eval(Tools.applAt(t, 0));
         if (cond) {
             return eval(Tools.applAt(t, 1));
         } else {
             varScope.restoreUnboundVars(bi);
             return eval(Tools.applAt(t, 2));
         }
     }
 
     private boolean evalLGChoice(ATermAppl t) throws FatalError {
         debug("evalLGChoice()");
         throw new FatalError("Not implemented");
 
     }
 
     private boolean evalSeq(ATermAppl t) throws FatalError {
         debug("evalSeq()");
         for (int i = 0; i < t.getChildCount(); i++) {
             if (!eval(Tools.applAt(t, i))) {
                debug(" fail : " + t);
                 return false;
             }
         }
         return true;
     }
 
     private boolean evalScope(ATermAppl t) throws FatalError {
         debug("evalScope()");
         enterVarScope();
         ATermList vars = (ATermList) t.getChildAt(0);
         debug(" " + vars);
         varScope.addUndeclaredVars(vars);
         boolean r = eval(Tools.applAt(t, 1));
         exitScope();
         return r;
     }
 
     private void enterDefScope() {
         defScope = new DefScope(defScope);
     }
 
     private void enterDefScope(DefScope parent) {
         defScope = new DefScope(parent);
     }
 
     private void exitDefScope() {
         defScope = defScope.getParent();
     }
 
     private boolean evalBuild(ATermAppl t) throws FatalError {
         debug("evalBuild()");
         debug(" term : " + t.toString());
         current = buildTerm((ATermAppl) t.getChildAt(0));
         if (current == null)
             throw new FatalError("build failed badly!");
         debug(" res  : " + current);
         return true;
     }
 
     public ATerm buildTerm(ATermAppl t) throws FatalError {
         if (t.getName().equals("Anno")) {
             return buildTerm(Tools.applAt(t, 0));
         } else if (t.getName().equals("Op")) {
             String ctr = Tools.stringAt(t, 0);
             ATermList children = (ATermList) t.getChildAt(1);
 
             AFun afun = factory.makeAFun(ctr, children.getLength(), false);
             ATermList kids = factory.makeList();
 
             for (int i = 0; i < children.getLength(); i++) {
                 kids = kids
                         .append(buildTerm((ATermAppl) children.elementAt(i)));
             }
             return factory.makeApplList(afun, kids);
         } else if (t.getName().equals("Int")) {
             ATermAppl x = (ATermAppl) t.getChildAt(0);
             return factory.makeInt(new Integer(x.getName()));
         } else if (t.getName().equals("Str")) {
             ATermAppl x = (ATermAppl) t.getChildAt(0);
             return x;
         } else if (t.getName().equals("Var")) {
             String n = Tools.stringAt(t, 0);
             ATerm x = lookup(n);
             debug(" lookup : " + n + " (= " + x + ")");
             return x;
         }
 
         throw new FatalError("Unknown build constituent '" + t.getName() + "'");
     }
 
     private ATerm lookup(String var) {
         return varScope.lookup(var);
     }
 
     private void debug(String s) {
         System.out.println(s);
     }
 
     private boolean evalId(ATermAppl t) {
         debug("evalId");
         return true;
     }
 
     private boolean evalFail(ATermAppl t) {
         System.out.println("evalFail");
         return false;
     }
 
     private boolean evalCall(ATermAppl t) throws FatalError {
         System.out.println("evalCall");
         debug(" term : " + t);
         ATermAppl sname = Tools.applAt(Tools.applAt(t, 0), 0);
         ATermList actualSVars = Tools.listAt(t, 1);
         ATermList actualTVars = Tools.listAt(t, 2);
         Strategy s = getStrategy(sname.getName());
         debug(" call : " + s.getName());
         List<String> formalTVars = s.getTermParams();
         List<String> formalSVars = s.getStrategyParams();
 
         debug(" args : " + actualSVars);
 
         VarScope newVarScope = new VarScope(s.getVarScope());
         DefScope newDefScope = new DefScope(s.getDefScope());
 
         if (formalSVars.size() != actualSVars.getChildCount()) {
             System.out.println(" takes : " + formalSVars.size());
             System.out.println(" have  : " + actualSVars.getChildCount());
 
             throw new FatalError("Parameter length mismatch!");
         }
 
         for (int i = 0; i < actualSVars.getChildCount(); i++) {
             String varName = Tools.stringAt(Tools.applAt(Tools
                     .applAt(actualSVars, i), 0), 0);
             debug("  " + formalSVars.get(i) + " points to " + varName);
             newVarScope.addSVar(formalSVars.get(i), getStrategy(varName));
         }
 
         for (int i = 0; i < actualTVars.getChildCount(); i++)
             newVarScope.add(formalTVars.get(i), varScope.lookup(Tools
                     .stringAt(Tools.applAt(actualTVars, i), 0)));
 
         VarScope oldVarScope = varScope;
         DefScope oldDefScope = defScope;
         varScope = newVarScope;
         defScope = newDefScope;
 
         boolean r;
         if (s instanceof IntStrategy) {
             r = eval(((IntStrategy) s).getBody());
         } else if (s instanceof ExtStrategy) {
             r = ((ExtStrategy) s).invoke(this, actualSVars, actualTVars);
         } else {
             throw new FatalError("Unknown kind of strategy  "
                     + s.getClass().getName());
         }
         varScope = oldVarScope;
         defScope = oldDefScope;
         return r;
     }
 
     private void exitScope() {
         varScope = varScope.getParent();
     }
 
     private Strategy getStrategy(String name) throws FatalError {
         Strategy s = varScope.lookupSVar(name);
         if (s != null)
             return s;
 
         s = defScope.lookupStrategy(name);
         if (s != null)
             return s;
 
         throw new FatalError("Lookup of strategy '" + name + "' failed");
     }
 
     private Constructor getConstructor(String name) {
         return defScope.lookupConstructor(name);
     }
 
     private boolean evalLet(ATermAppl t) throws FatalError {
         debug("evalLet()");
         enterDefScope();
         varScope.dumpScope("  ");
         ATermList sdefs = Tools.listAt(t, 0);
         for (int i = 0; i < sdefs.getLength(); i++) {
            IntStrategy s = new IntStrategy(Tools.termAt(sdefs, i), defScope,
                                             varScope);
             debug(" adding : " + s.getName());
             defScope.addStrategy(s);
         }
         ATermAppl body = Tools.applAt(t, 1);
         boolean r = eval(body);
         exitDefScope();
         return r;
     }
 
     public boolean eval(ATerm t) throws FatalError {
         if (t.getType() == ATerm.APPL)
             return eval((ATermAppl) t);
 
         throw new FatalError("Internal error: Invalid term type " + t.getType()
                 + " / " + t);
     }
 
     private void enterVarScope() {
         varScope = new VarScope(varScope);
     }
 
     public ATerm getCurrent() {
         return current;
     }
 
     public void setCurrent(ATerm term) {
         current = term;
     }
 
     public boolean invoke(String name, ATermList svars, ATermList tvars)
             throws FatalError {
         debug("Calling " + name + " with " + svars.toString() + " / "
                 + tvars.toString());
 
         return eval(makeTerm(("CallT(SVar(\"" + name + "\"), [], [])")));
     }
 
     private List<Pair<String, ATerm>> emptyList = new ArrayList<Pair<String, ATerm>>();
 
     public List<Pair<String, ATerm>> match(ATermAppl t, ATermAppl p)
             throws FatalError {
         debug(" ?: '" + t.getName() + "' / " + p.getName());
 
         if (p.getName().equals("Anno"))
             return match(t, (ATermAppl) p.getChildAt(0));
         else if (p.getName().equals("Op")) {
 
             ATermList l = (ATermList) p.getChildAt(1);
             if (l.getChildCount() != t.getChildCount())
                 return null;
 
             ATermAppl c = (ATermAppl) p.getChildAt(0);
             if (!t.getName().equals(c.getName()))
                 return null;
 
             List<Pair<String, ATerm>> r = new ArrayList<Pair<String, ATerm>>();
             for (int i = 0; i < t.getChildCount(); i++) {
                 List<Pair<String, ATerm>> m = match((ATerm) t.getChildAt(i),
                                                     (ATermAppl) l.getChildAt(i));
                 if (m != null)
                     r.addAll(m);
                 else
                     return null;
             }
             return r;
         } else if (p.getName().equals("Int")) {
             if (t.getType() == ATerm.INT)
                 return match(Tools.intAt(t, 0), Tools.applAt(p, 0));
             return null;
         } else if (p.getName().equals("Str")) {
             debug(" !" + t + " ?" + p);
             if (t.getName().equals(Tools.stringAt(p, 0)))
                 return emptyList;
             return null;
         } else if (Tools.termType(p, "Var")) {
             List<Pair<String, ATerm>> r = new ArrayList<Pair<String, ATerm>>();
             r.add(new Pair<String, ATerm>(Tools.stringAt(p, 0), t));
             return r;
         } else if (Tools.termType(p, "Explode")) {
             AFun ctor = t.getAFun();
             ATermList args = t.getArguments();
 
             ATermAppl ctor_p = Tools.applAt(p, 0);
             ATerm ctor_t = makeTerm("\"" + ctor.getName() + "\"");
 
             List<Pair<String, ATerm>> r = match(ctor_t, ctor_p);
             if (r == null)
                 return null;
 
             ATermAppl appl_p = Tools.applAt(p, 1);
 
             r.addAll(match(makeList(args), appl_p));
             return r;
         } else if (Tools.termType(p, "As")) {
             List<Pair<String, ATerm>> r = match(t, Tools.applAt(p, 1));
             if (r == null)
                 return null;
             debug("" + p);
             String varName = Tools.stringAt(Tools.applAt(p, 0), 0);
             r.add(new Pair<String, ATerm>(varName, t));
             return r;
         }
 
         throw new FatalError("What?" + p);
     }
 
     public List<Pair<String, ATerm>> match(ATermInt t, ATermAppl p)
             throws FatalError {
         debug(" !" + t + " ?" + p);
 
         if (p.getName().equals("Anno")) {
             return match(t, Tools.applAt(p, 0));
         } else if (p.getName().equals("Int")) {
             Integer i = new Integer(Tools.stringAt(p, 0));
             if (i == t.getInt())
                 return emptyList;
             return null;
         } else if (p.getName().equals("Var")) {
             List<Pair<String, ATerm>> r = new ArrayList<Pair<String, ATerm>>();
             r.add(new Pair<String, ATerm>(((ATermAppl) p.getChildAt(0))
                     .getName(), t));
             return r;
         } else if (p.getName().equals("Op")) {
             return null;
         } else if (p.getName().equals("Explode")) {
             return null;
         } else if (p.getName().equals("Wld")) {
             return emptyList;
         } else if (p.getName().equals("As")) {
             List<Pair<String, ATerm>> r = new ArrayList<Pair<String, ATerm>>();
             String varName = Tools.stringAt(Tools.applAt(p, 0), 0);
             r.add(new Pair<String, ATerm>(varName, t));
             return r;
         }
 
         throw new FatalError("Unknown type '" + p.getName());
     }
 
     public List<Pair<String, ATerm>> match(ATerm t, ATermAppl p)
             throws FatalError {
         if (t.getType() == ATerm.APPL)
             return match((ATermAppl) t, p);
         else if (t.getType() == ATerm.INT)
             return match((ATermInt) t, p);
 
         throw new FatalError("Current term is not an ATermAppl term ["
                 + t.getClass().toString() + " " + ATerm.APPL + " "
                 + t.getType() + "]");
     }
 
 }
