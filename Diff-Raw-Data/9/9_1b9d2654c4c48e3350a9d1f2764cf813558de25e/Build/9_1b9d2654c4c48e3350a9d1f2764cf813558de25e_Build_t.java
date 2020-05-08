 /*
  * Evaluation of the StrategoCore Build term
  * 
  * Created on 07.aug.2005
  *
  * Copyright (c) 2005, Karl Trygve Kalleberg <karltk near strategoxt.org>
  * 
  * Licensed under the GNU General Public License, v2
  * 
  * Part of Spoofax
  */
 package org.spoofax.interpreter.stratego;
 
 import org.spoofax.DebugUtil;
 import org.spoofax.interpreter.core.IConstruct;
 import org.spoofax.interpreter.core.IContext;
 import org.spoofax.interpreter.core.InterpreterException;
 import org.spoofax.interpreter.core.Tools;
 import org.spoofax.interpreter.terms.IStrategoAppl;
 import org.spoofax.interpreter.terms.IStrategoConstructor;
 import org.spoofax.interpreter.terms.IStrategoInt;
 import org.spoofax.interpreter.terms.IStrategoList;
 import org.spoofax.interpreter.terms.IStrategoReal;
 import org.spoofax.interpreter.terms.IStrategoString;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.interpreter.terms.ITermFactory;
 import static org.spoofax.interpreter.core.Tools.*;
 
 public class Build extends Strategy {
 
     private IStrategoAppl term;
 
     public Build(IStrategoAppl t) {
         term = t;
     }
 
     public IConstruct eval(IContext env) throws InterpreterException {
         if (DebugUtil.isDebugging()) {
             debug("Build.eval() - ", env.current(), " -> !", term);
         }
 
         IStrategoTerm t = buildTerm(env, term);
         if (t == null) {
         	return getHook().pop().onFailure(env);
         }
         env.setCurrent(t);
 
         return getHook().pop().onSuccess(env);
     }
 
     public IStrategoTerm buildTerm(IContext env, IStrategoAppl t) throws InterpreterException {
 
         ITermFactory factory = env.getFactory();
 
         if (Tools.isAnno(t, env)) {
             return buildAnno(env, t);
         }
         else if (Tools.isOp(t, env)) {
             return buildOp(env, t, factory);
         }
         else if (Tools.isInt(t, env)) {
             return buildInt(t, factory);
         }
         else if (Tools.isReal(t, env)) {
             return buildReal(t, factory);
         }
         else if (Tools.isStr(t, env)) {
             return buildStr(t);
         }
         else if (Tools.isVar(t, env)) {
             return buildVar(env, t);
         }
         else if (Tools.isExplode(t, env)) {
             return buildExplode(env, t);
         }
 
         throw new InterpreterException("Unknown build constituent '" + t.getConstructor() + "'");
     }
 
     private IStrategoTerm buildExplode(IContext env, IStrategoAppl t) throws InterpreterException {
         if (DebugUtil.isDebugging()) {
             debug("buildExplode() : ", t);
         }
 
         ITermFactory factory = env.getFactory();
 
         IStrategoAppl ctor = Tools.applAt(t, 0);
         IStrategoAppl args = Tools.applAt(t, 1);
 
         if (DebugUtil.isDebugging()) {
             debug(" ctor : ", ctor);
         }
         if (DebugUtil.isDebugging()) {
             debug(" args : ", args);
         }
 
         IStrategoTerm actualCtor = buildTerm(env, ctor);
         IStrategoTerm actualArgs = buildTerm(env, args);
 
         if(actualCtor == null || actualArgs == null)
             return null;
         
         if (DebugUtil.isDebugging()) {
             debug(" actualCtor : ", actualCtor);
         }
         if (DebugUtil.isDebugging()) {
             debug(" actualArgs : ", actualArgs);
         }
 
         if (Tools.isTermInt(actualCtor) || Tools.isTermReal(actualCtor)) {
             return actualCtor;
         }
         else if (Tools.isTermString(actualCtor)) {
             return doBuildExplode(factory, actualCtor, actualArgs);
         }
         else if (Tools.isTermList(actualCtor)) {
             return actualArgs;
         }
 
         throw new InterpreterException("Unknown term explosion : '" + t + "' " + t.getTermType()
                                        + " : " + actualCtor + " / " + actualArgs);
     }
 
     private IStrategoTerm doBuildExplode(ITermFactory factory, IStrategoTerm actualCtor, IStrategoTerm actualArgs) throws InterpreterException {
         if (!(Tools.isTermList(actualArgs))) {
             throw new InterpreterException("Not a list: " + actualArgs);
         }
 
         String n = ((IStrategoString)actualCtor).stringValue();
         IStrategoTerm[] realArgs = ((IStrategoList)actualArgs).getAllSubterms();
         
         if (n.equals(""))
             return factory.makeTuple(realArgs);
         
         boolean quoted = false;
         if (n.length() > 1 && n.charAt(0) == '"') {
             n = n.substring(1, n.length() - 1);
             quoted = true;
         }
 
         if(quoted && realArgs.length == 0) {
             return factory.makeString(n);
         }
         
         IStrategoConstructor afun = factory.makeConstructor(n, realArgs.length);
         return factory.makeAppl(afun, realArgs);
     }
 
     private IStrategoTerm buildVar(IContext env, IStrategoAppl t) throws InterpreterException {
 
         String n = Tools.javaStringAt(t, 0);
         return env.lookupVar(n);
     }
 
     private IStrategoString buildStr(IStrategoAppl t) {
         IStrategoString x = Tools.stringAt(t, 0);
 
         return x;
     }
 
     private IStrategoReal buildReal(IStrategoAppl t, ITermFactory factory) {
         String x = Tools.javaStringAt(t, 0);
 
         return factory.makeReal(new Double(x));
     }
 
     private IStrategoInt buildInt(IStrategoAppl t, ITermFactory factory) {
         String x = Tools.javaStringAt(t, 0);
 
         return factory.makeInt(new Integer(x).intValue());
     }
 
     private IStrategoTerm buildOp(IContext env, IStrategoAppl t, ITermFactory factory)
             throws InterpreterException {
         // FIXME memoize constructors
         
         String ctr = Tools.javaStringAt(t, 0);
         IStrategoList children = (IStrategoList) t.getSubterm(1);
         
         if(ctr.length() == 0) {
             return buildTuple(env, t);
         } else if(children.getSubtermCount() == 0 && ctr.equals("Nil")) {
             return buildNil(env);
         } else if(children.getSubtermCount() == 2 && ctr.equals("Cons")) {
             return buildCons(env, t, factory);
         } else {
             return buildOp(ctr, env, t, factory);
         }
     }
     
     private IStrategoList buildNil(IContext env) {
         return env.getFactory().makeList();
     }
 
     private IStrategoTerm buildOp(String ctr, IContext env, IStrategoAppl t, ITermFactory factory) 
     throws  InterpreterException {
         
         IStrategoList children = (IStrategoList) t.getSubterm(1);
 
         IStrategoConstructor ctor = factory.makeConstructor(ctr, children.size());
         IStrategoTerm[] kids = new IStrategoTerm[children.size()];
 
         for (int i = children.size() -1 ; i >= 0; i--) {
             IStrategoTerm kid = buildTerm(env, (IStrategoAppl) children.getSubterm(i));
             if (kid == null) {
                 return null;
             }
             kids[i] = kid;
         }
 
         return factory.makeAppl(ctor, kids);
     }
 
     private IStrategoList buildCons(IContext env, IStrategoAppl t, ITermFactory factory) throws InterpreterException {
 
         IStrategoList children = (IStrategoList) t.getSubterm(1);
         
         IStrategoAppl headPattern = (IStrategoAppl) children.get(0);
         IStrategoAppl tailPattern = (IStrategoAppl) children.get(1);
         
         IStrategoList tail = buildList(env, tailPattern, factory); 
         IStrategoTerm head = buildTerm(env, headPattern);
         
         if(tail == null || head == null)
             return null;
         
         return factory.makeListCons(head, tail);
     }
 
     private IStrategoList buildList(IContext env, IStrategoAppl t, ITermFactory factory) throws InterpreterException {
     	
         // FIXME improve! this is an Anno!
         if(Tools.isAnno(t, env)) {
             t = Tools.applAt(t, 0);
         
             String c = Tools.javaStringAt(t, 0);
             
             if(c.equals("Nil")) {
                 return buildNil(env);
             } else if(c.equals("Cons")) {
                 return buildCons(env, t, factory);
             }
         }
         
         if(Tools.isVar(t, env)) {
             IStrategoTerm r = buildVar(env, t);
            if (r == null) return null;
            if (r.getTermType() == IStrategoTerm.LIST) {
                 return (IStrategoList) r;
            } else {
                System.err.println("Warning: trying to build list with illegal tail: " + t.toString());
                return null;
             }
         }
         
         throw new InterpreterException("List tail must always be a list!");
     }
 
 
     private IStrategoTerm buildTuple(IContext env, IStrategoAppl t) throws InterpreterException {
         
         IStrategoList children = (IStrategoList) t.getSubterm(1);
         IStrategoTerm[] kids = new IStrategoTerm[children.size()];
 
         for (int i = 0; i < children.size(); i++) {
             IStrategoTerm kid = kids[i] = buildTerm(env, (IStrategoAppl) children.getSubterm(i));
             if (kid == null) {
                 return null;
             }
         }
 
         return env.getFactory().makeTuple(kids);
     }
 
     private IStrategoTerm buildAnno(IContext env, IStrategoAppl t) throws InterpreterException {
         IStrategoTerm term = buildTerm(env, applAt(t, 0));
         if (term == null) return null;
         
         IStrategoAppl annos = applAt(t, 1);
         if (term.getAnnotations().size() == 0
                 && "Op".equals(annos.getConstructor().getName())
                 && "Nil".equals(javaStringAt(annos, 0))) {
             return term;
         } else {
             IStrategoTerm annoList = buildTerm(env, annos);
             if (annoList.getTermType() != IStrategoTerm.LIST)
                 annoList = env.getFactory().makeList(annoList);
             
             if (annoList.equals(term.getAnnotations())) {
                 return term;
             } else {
                 return env.getFactory().annotateTerm(term, (IStrategoList) annoList);
             }
         }
     }
 
     public void prettyPrint(StupidFormatter sf) {
         sf.first("Build(" + term.toString() + ")");
     }
 
     @Override
     public String toString() {
     	return "Build(" + term.toString() + ")";
     }
     
     @Override
     protected String getTraceName() {
         return super.getTraceName() + "(" + term + ")";
     }
 }
