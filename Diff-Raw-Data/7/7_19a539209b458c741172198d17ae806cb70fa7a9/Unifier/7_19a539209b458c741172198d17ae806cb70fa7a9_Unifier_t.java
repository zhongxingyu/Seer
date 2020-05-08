 //----------------------------------------------------------------------------
 // Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
 //
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 //
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 //
 // To contact the authors:
 // http://www.dur.ac.uk/r.bordini
 // http://www.inf.furb.br/~jomi
 //
 //----------------------------------------------------------------------------
 
 package jason.asSemantics;
 
 import jason.asSyntax.Literal;
 import jason.asSyntax.Pred;
 import jason.asSyntax.Structure;
 import jason.asSyntax.Term;
 import jason.asSyntax.Trigger;
 import jason.asSyntax.VarTerm;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 public class Unifier implements Cloneable {
 
     static Logger logger = Logger.getLogger(Unifier.class.getName());
 
     private HashMap<VarTerm, Term> function = new HashMap<VarTerm, Term>();
 
     /** 
      * @deprecated use t.apply(un) instead.
      */
     public void apply(Term t) {
     	t.apply(this);
     }
 
     /** 
      * @deprecated use p.apply(un) instead.
      */
     public void apply(Pred p) {
     	p.apply(this);
     }
 
     /**
      * gets the value for a Var, if it is unified with another var, gets this
      * other's value
      */
     public Term get(String var) {
         return get(new VarTerm(var));
     }
 
     /**
      * gets the value for a Var, if it is unified with another var, gets this
      * other's value
      */
     public Term get(VarTerm vtp) {
         return function.get(vtp);
     }
 
     public Term get(Term t) {
         if (t.isVar()) {
             return function.get((VarTerm) t);
         } else {
             return null;
         }
     }
 
     // ----- Unify for Predicates/Literals
     
     public boolean unifies(Term t1g, Term t2g) {
 
 		Pred np1 = null;
 		Pred np2 = null;
 		
     	if (t1g instanceof Pred && t2g instanceof Pred) {
     		np1 = (Pred)t1g;
     		np2 = (Pred)t2g;
         
     		// tests when np1 or np2 are Vars with annots
 	        if ((np1.isVar() && np1.hasAnnot()) || np2.isVar() && np2.hasAnnot()) {
 	            if (!np1.hasSubsetAnnot(np2, this)) {
 	                return false;
 	            }
 	        }
     	}
     	
         // unify as Term
         boolean ok = unifyTerms(t1g, t2g);
 
         // if np1 is a var that was unified, clear its annots, as in
         //      X[An] = p(1)[a]
         // X is mapped to p(1) and not p(1)[a]
         if (ok && np1 != null) { // they are predicates
 	        if (np1.isVar() && np1.hasAnnot()) {
 	        	Term np1vl = function.get((VarTerm) np1);
 	        	if (np1vl.isPred()) {
 	        		((Pred) np1vl).setAnnots(null);
 	        	}
 	        }
 	        if (np2.isVar() && np2.hasAnnot()) {
 	        	Term np2vl = function.get((VarTerm) np2);
 	        	if (np2vl.isPred()) {
 	        		((Pred)np2vl).setAnnots(null);
 	        	}
 	        }
         }
         return ok;
     }
 
     
     // ----- Unify for Terms
 
     private boolean unifyTerms(Term t1g, Term t2g) {
         // if args are expressions, apply them and use their values
         if (t1g.isArithExpr()) {
             t1g = (Term) t1g.clone();
             t1g.apply(this);
         }
         if (t2g.isArithExpr()) {
             t2g = (Term) t2g.clone();
             t2g.apply(this);
         }
 
         // both are vars
         if (t1g.isVar() && t2g.isVar()) {
             VarTerm t1gv = (VarTerm) t1g;
             VarTerm t2gv = (VarTerm) t2g;
             
             // get their values
             Term t1vl = function.get(t1gv);
             Term t2vl = function.get(t2gv);
 
             // if the variable value is a var cluster, it means it has no value
             if (t1vl instanceof VarsCluster)
                 t1vl = null;
             if (t2vl instanceof VarsCluster)
                 t2vl = null;
 
             // both has value, their values should unify
             if (t1vl != null && t2vl != null) {
                 return unifies(t1vl, t2vl);
             }
             // only t1 has value, t1's value should unify with var t2
             if (t1vl != null) {
                 return unifies(t2gv, t1vl);
             }
             // only t2 has value, t2's value should unify with var t1
             if (t2vl != null) {
                 return unifies(t1gv, t2vl);
             }
 
             // both are var (not unnamedvar) with no value, like X=Y
             // we must ensure that these vars will form a cluster
             if (! t1gv.isUnnamedVar() && ! t2gv.isUnnamedVar()) {
                 VarTerm t1c = (VarTerm) t1gv.clone();
                 VarTerm t2c = (VarTerm) t2gv.clone();
                 VarsCluster cluster = new VarsCluster(t1c, t2c, this);
                 if (cluster.hasValue()) {
                     // all vars of the cluster should have the same value
                     for (VarTerm vtc : cluster) {
                         function.put(vtc, cluster);
                     }
                 }
             }
             return true;
         }
 
         // t1 is var that doesn't occur in t2
         if (t1g.isVar()) {
             VarTerm t1gv = (VarTerm) t1g;
             // if t1g is not free, must unify values
             Term t1vl = function.get(t1gv);
             if (t1vl != null && !(t1vl instanceof VarsCluster)) {
                 return unifies(t1vl,t2g);
             } else if (!t2g.hasVar(t1g)) {
                 return setVarValue(t1gv, t2g);
             }
             return false;
         }
 
         // t2 is var that doesn't occur in t1
         if (t2g.isVar()) {
             VarTerm t2gv = (VarTerm) t2g;
             // if t1g is not free, must unify values
             Term t2vl = function.get(t2gv);
             if (t2vl != null && !(t2vl instanceof VarsCluster)) {
                 return unifies(t2vl,t1g);
             } else if (!t1g.hasVar(t2g)) {
                 return setVarValue(t2gv, t1g);
             } 
             return false;
         }
 
         // both terms are not vars
         
         // if any of the terms is not a structure (is a number or a
         // string), they must be equal
         if (!t1g.isStructure() || !t2g.isStructure()) {
         	return t1g.equals(t2g);
         }
 
         // both terms are structures
 
         // if both are literal, they must have the same negated
         if (t1g.isLiteral() && t2g.isLiteral() && ((Literal)t1g).negated() != ((Literal)t2g).negated()) {
         	return false;
         }
         	
         // if one term is literal and the other not, the literal should not be negated
         if (t1g.isLiteral() && !t2g.isLiteral() && ((Literal)t1g).negated()) {
         	return false;
         }
         if (t2g.isLiteral() && !t1g.isLiteral() && ((Literal)t2g).negated()) {
         	return false;
         }
         
         // if the first term is a predicate and the second not, the first should not have annots 
         if (t1g.isPred() && !t2g.isPred() && ((Pred)t1g).hasAnnot()) {
         	return false;
         }
         
         // if both are predicates, the first's annots must be subset of the second's annots
         if (t1g.isPred() && t2g.isPred()) {
         	if ( ! ((Pred)t1g).hasSubsetAnnot((Pred)t2g, this)) {
         		return false;
         	}
         }
         
         Structure t1s = (Structure)t1g;
         Structure t2s = (Structure)t2g;
         
         List t1gts = t1s.getTerms();
         List t2gts = t2s.getTerms();
 
         // different arities
         if ((t1gts == null && t2gts != null) || (t1gts != null && t2gts == null)) {
             return false;
         }
         if (t1s.getTermsSize() != t2s.getTermsSize()) {
             return false;
         }
         
         // different functor
         if (t1s.getFunctor() != null && !t1s.getFunctor().equals(t2s.getFunctor())) {
             return false;
         }
     
         // unify inner terms
         // do not use iterator! (see ListTermImpl class)
         final int ts = t1s.getTermsSize();
         for (int i = 0; i < ts; i++) {
             if (!unifies(t1s.getTerm(i), t2s.getTerm(i))) {
                 return false;
             }
         }
         return true;
     }
 
     private boolean setVarValue(VarTerm vt, Term value) {
         // if the var has a cluster, set value for all cluster
         Term currentVl = function.get(vt);
         if (currentVl != null && currentVl instanceof VarsCluster) {
             VarsCluster cluster = (VarsCluster) currentVl;
             for (VarTerm cvt : cluster) {
                 function.put(cvt, (Term) value.clone());
             }
         } else {
             // no value in cluster
             function.put((VarTerm) vt.clone(), (Term) value.clone());
         }
         return true;
     }
 
     public boolean unifies(Trigger te1, Trigger te2) {
         return te1.sameType(te2) && unifies(te1.getLiteral(), te2.getLiteral());
     }
 
     public void clear() {
         function.clear();
     }
 
     public String toString() {
         return function.toString();
     }
 
     public int size() {
         return function.size();
     }
 
    /** add all unifications from u */
     public void compose(Unifier u) {
        for (VarTerm k: u.function.keySet()) {
            function.put( (VarTerm)k.clone(), (Term)u.function.get(k).clone());
        }
     }
     
     public Object clone() {
         try {
             Unifier newUn = new Unifier();
             for (VarTerm k: function.keySet()) {
                 newUn.function.put( (VarTerm)k.clone(), (Term)function.get(k).clone());
             }
             return newUn;
         } catch (Exception e) {
             logger.log(Level.SEVERE, "Error cloning unifier.",e);
             return null;
         }
     }
     
     public boolean equals(Object o) {
         if (o == null) return false;
         if (o == this) return true;
         if (o instanceof Unifier) {
             return function.equals( ((Unifier)o).function);
         }
         return false;
     }
     
     /** get as XML */
     public Element getAsDOM(Document document) {
         Element u = (Element) document.createElement("unifier");
         for (VarTerm v: function.keySet()) {
             Element ev = v.getAsDOM(document);
             Element vl = (Element) document.createElement("value");
             vl.appendChild( function.get(v).getAsDOM(document));
             Element map = (Element) document.createElement("map");
             map.appendChild(ev);
             map.appendChild(vl);
             u.appendChild(map);
         }
         return u;
     }
 }
