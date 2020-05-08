 // ----------------------------------------------------------------------------
 // Copyright (C) 2003 Rafael H. Bordini, Jomi F. Hubner, et al.
 // 
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 // 
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 // Lesser General Public License for more details.
 // 
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 // 
 // To contact the authors:
 // http://www.dur.ac.uk/r.bordini
 // http://www.inf.furb.br/~jomi
 //
 //----------------------------------------------------------------------------
 
 package jason.asSyntax;
 
 import jason.JasonException;
 import jason.asSemantics.Agent;
 import jason.asSemantics.Unifier;
 import jason.asSyntax.parser.as2j;
 
 import java.io.StringReader;
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 
 /**
  * A Literal is a Pred with strong negation (~).
  */
 public class Literal extends Pred implements LogicalFormula {
 
 	private static final long serialVersionUID = 1L;
 
 	public static final boolean LPos   = true;
     public static final boolean LNeg   = false;
     public static final Literal LTrue  = new Literal("true");
     public static final Literal LFalse = new Literal("false");
 
     static private Logger logger = Logger.getLogger(Literal.class.getName());
 
 	private boolean type = LPos;
 
 	/** creates a positive literal */
 	public Literal(String functor) {
 		super(functor);
 	}
 
 	/** if pos == true, the literal is positive, otherwise it is negative */
 	public Literal(boolean pos, String functor) {
 		super(functor);
 		type = pos;
 	}
 
 	/** if pos == true, the literal is positive, otherwise it is negative */
 	public Literal(boolean pos, Pred p) {
 		super(p);
 		type = pos;
 	}
 
 	public Literal(Literal l) {
 		super((Pred) l);
 		type = l.type;
 	}
 
 
 	public static Literal parseLiteral(String sLiteral) {
 		as2j parser = new as2j(new StringReader(sLiteral));
 		try {
 			return parser.literal();
 		} catch (Exception e) {
 			logger.log(Level.SEVERE,"Error parsing literal " + sLiteral,e);
 			return null;
 		}
 	}
 
 	public boolean isInternalAction() {
 		return getFunctor() != null && getFunctor().indexOf('.') >= 0;
 	}
 	
     @Override
 	public boolean isLiteral() {
 		return true;
 	}
 
 	@Override
 	public boolean isAtom() {
 		return super.isAtom() && !negated();
 	}
 	
 	public boolean negated() {
 		return (type == LNeg);
 	}
     
     public void setNegated(boolean b) {
         type = b;
         hashCodeCache = null;
     }
 
     /** 
      * logCons checks whether one particular predicate
      * is a log(ical)Cons(equence) of the belief base.
      * 
      * Returns an iterator for all unifiers that are logCons.
      */
     @SuppressWarnings("unchecked")
     public Iterator<Unifier> logicalConsequence(final Agent ag, final Unifier un) {
         if (isInternalAction()) {
             try {
             	// clone terms array
                 Term[] current = getTermsArray();
                 Term[] clone = new Term[current.length];
                 for (int i=0; i<clone.length; i++) {
                     clone[i] = (Term)current[i].clone();
                     clone[i].apply(un);
                 }
 
             	// calls execute
                 Object oresult = ag.getIA(this).execute(ag.getTS(), un, clone);
                 if (oresult instanceof Boolean && (Boolean)oresult) {
                     return LogExpr.createUnifIterator(un);
                 } else if (oresult instanceof Iterator) {
                     return ((Iterator<Unifier>)oresult);
                 }
             } catch (Exception e) {
                 logger.log(Level.SEVERE, getErrorMsg(ag) + ": " +	e.getMessage(), e);
             }
             return LogExpr.EMPTY_UNIF_LIST.iterator();  // empty iterator for unifier
        } else if (this.equals(LTrue)) {
             return LogExpr.createUnifIterator(un);            
        } else if (this.equals(LFalse)) {
             return LogExpr.EMPTY_UNIF_LIST.iterator();            
         } else {
             final Iterator<Literal> il = ag.getBB().getRelevant(this);
             if (il == null)
                 return LogExpr.EMPTY_UNIF_LIST.iterator();
 
             return new Iterator<Unifier>() {
                 Unifier current = null;
                 Iterator<Unifier> ruleIt = null; // current rule solutions iterator
                 Rule rule; // current rule
                 
                 public boolean hasNext() {
                     if (current == null)
                         get();
                     return current != null;
                 }
 
                 public Unifier next() {
                     if (current == null)
                         get();
                     Unifier a = current;
                     current = null; //get();
                     return a;
                 }
 
                 private void get() {
                     //logger.info("*"+Literal.this+" in get, ruleit =  "+ruleIt);
                     current = null;
                     
                     // try rule iterator
                     while (ruleIt != null && ruleIt.hasNext()) {
                         // unifies the rule head with the result of rule evaluation
                         Unifier ruleUn = ruleIt.next(); // evaluation result
                         Literal rhead = rule.headClone();
                         rhead.apply(ruleUn);
                         
                         Unifier unC = (Unifier) un.clone();
                         if (unC.unifies(Literal.this, rhead)) {
                             current = unC;
                             return;
                         }
                     }
                     
                     // try literal iterator
                     while (il.hasNext()) {
                         Literal b = il.next(); // b is the relevant entry in BB
                         if (b.isRule()) {
                             rule = (Rule)b;
                             
                             // create a copy of this literal, ground it and 
                             // make its vars annonym, it is
                             // used to define what will be the unifier used
                             // inside the rule.
                             // Only vars from rule head should get value in the
                             // unifier used inside the rule evaluation.
                             Literal h = (Literal)Literal.this.clone();
                             h.apply(un);
                             h.makeVarsAnnon();
                             Unifier ruleUn = new Unifier();
                             if (ruleUn.unifies(h, rule)) {
                                 //logger.info("go "+h+" rule="+rule+" un="+ruleUn);
                                 ruleIt = rule.getBody().logicalConsequence(ag,ruleUn);
                                 //logger.info("ruleIt for "+h+" ="+ruleIt);
                                 get();
                                 //logger.info("ret from "+h+" get="+current+" - "+il.hasNext());
                                 if (current != null) { // if it get a value
                                     return;
                                 }
                             }
                         } else {
                             Unifier unC = (Unifier) un.clone();
                             if (unC.unifies(Literal.this, b)) {
                                 current = unC;
                                 return;
                             }
                         }
                     }
                 }
 
                 public void remove() {
                 }
             };
         }
     }   
 
     @Override
     public boolean equals(Object o) {
         if (o == null) return false;
         if (o == this) return true;
 
         if (o instanceof Literal) {
 			final Literal l = (Literal) o;
 			return type == l.type && hashCode() == l.hashCode() && super.equals(l);
 		} else if (o instanceof Structure) {
 			return !negated() && super.equals(o);
 		}
         return false;
 	}
 
     public String getErrorMsg(Agent ag) {
     	String line = "";
     	if (getSrcLine() >= 0) {
     		line = ":"+getSrcLine();
     	}
     	String ia = "";
     	if (isInternalAction()) {
     		ia = " internal action";
     	}
         return "Error in "+ia+"'"+this+"' ("+ ag.getASLSource() + line + ")";    	
     }
     
     @Override
     public int compareTo(Term t) {
         if (t.isLiteral()) {
             Literal tl = (Literal)t;
             if (!negated() && tl.negated()) {
                 return -1;
             } if (negated() && !tl.negated()) {
                 return 1;
             }
         }
         int c = super.compareTo(t);
         if (c != 0)
             return c;
         return 0;
     }        
 
 	public Object clone() {
         Literal c = new Literal(this);
         c.predicateIndicatorCache = this.predicateIndicatorCache;
         c.hashCodeCache = this.hashCodeCache;
         return c;
 	}
 
     
     @Override
     protected int calcHashCode() {
         int result = super.calcHashCode();
         if (negated()) {
             result += 3271;
         }
         return result;
     }
 
 	
 	/** return [~] super.getFunctorArity */
 	@Override 
     public PredicateIndicator getPredicateIndicator() {
 		if (predicateIndicatorCache == null) {
 		    predicateIndicatorCache = new PredicateIndicator(((type == LPos) ? "" : "~")+getFunctor(),getTermsSize());
 		}
 		return predicateIndicatorCache;
 	}
 	
 	/** returns this literal as a list [<functor>, <list of terms>, <list of annots>] */
 	public ListTerm getAsListOfTerms() {
 		ListTerm l = new ListTermImpl();
 		l.add(new Literal(type, getFunctor()));
 		ListTerm lt = new ListTermImpl();
 		if (getTerms() != null) {
 			lt.addAll(getTerms());
 		}
 		l.add(lt);
 		if (hasAnnot()) {
 			l.add((ListTerm)getAnnots().clone());
 		} else {
 			l.add(new ListTermImpl());
 		}
 		return l;
 	}
 
 	/** creates a literal from a list [<functor>, <list of terms>, <list of annots>] */
 	public static Literal newFromListOfTerms(ListTerm lt) throws JasonException {
 		try {
 			Iterator<Term> i = lt.iterator();
 			
 			Term tfunctor = i.next();
 
 			boolean pos = Literal.LPos;
 			if (tfunctor.isLiteral() && ((Literal)tfunctor).negated()) {
 				pos = Literal.LNeg;
 			}
 
 			Literal l = new Literal(pos,((Structure)tfunctor).getFunctor());
 
 			if (i.hasNext()) {
 				l.setTerms((ListTerm)((ListTerm)i.next()).clone());
 			}
 			if (i.hasNext()) {
 				l.setAnnots((ListTerm)((ListTerm)i.next()).clone());
 			}
 			return l;
 		} catch (Exception e) {
 			throw new JasonException("Error creating literal from "+lt);
 		}
 	}
 	
 	public String toString() {
 		if (type == LPos)
 			return super.toString();
 		else
 			return "~" + super.toString();
 	}
 
     /** get as XML */
     @Override
     public Element getAsDOM(Document document) {
         Element u = (Element) document.createElement("literal");
         if (isInternalAction()) {
             u.setAttribute("ia", isInternalAction()+"");
         }
         u.setAttribute("negated", negated()+"");
         u.appendChild(super.getAsDOM(document));
         return u;
     }    
     
 }
