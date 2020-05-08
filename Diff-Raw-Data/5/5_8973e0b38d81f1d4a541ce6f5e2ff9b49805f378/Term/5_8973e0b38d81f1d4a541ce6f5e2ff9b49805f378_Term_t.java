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
 // http://www.csc.liv.ac.uk/~bordini
 // http://www.inf.furb.br/~jomi
 //----------------------------------------------------------------------------
 
 package jason.asSyntax;
 
 import jason.asSyntax.parser.as2j;
 
 import java.io.Serializable;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 public class Term implements Cloneable, Comparable, Serializable {
 
 	protected String funcSymb = null;
 	protected List terms;
 
 	public Term() {
 	}
 
 	public Term(String fs) {
 		setFunctor(fs);
 	}
 
 	public Term(Term t) {
 		set(t);
 	}
 
 	public static Term parse(String sTerm) {
 		as2j parser = new as2j(new StringReader(sTerm));
 		try {
 			return parser.t(); // parse.t() may returns a Pred/List...
 		} catch (Exception e) {
 			System.err.println("Error parsing term " + sTerm);
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	// use Object as parameter to simply the Unifier.apply
 	public void set(Object o) {
 		try {
 			Term t = (Term)o;
 			setFunctor(t.funcSymb);
 			terms = t.getHardCopyOfTerms();
 		} catch (Exception e) {
 			System.err.println("Error setting value for term ");
 			e.printStackTrace();
 		}
 	}
 
 	public void setFunctor(String fs) {
 		funcSymb = fs;
 		functorArityBak = null;
 	}
 
 	public String getFunctor() {
 		return funcSymb;
 	}
 
 	public boolean hasFunctor(String fs) {
 		return funcSymb.equals(fs);
 	}
 
 	protected String functorArityBak = null; // to not compute it all the time (is is called many many times)
 	
 	/** return <functor symbol> "/" <arity> */
 	public String getFunctorArity() {
 		if (functorArityBak == null) {
 			if (terms == null) {
 				functorArityBak = funcSymb + "/0";
 			} else {
 				functorArityBak = funcSymb + "/" + terms.size();
 			}
 		}
 		return functorArityBak;
 	}
 
 	public int hashCode() {
 		return getFunctorArity().hashCode();
 	}
 
 	/** returns the i-th term */
 	public Term getTerm(int i) {
 		if (terms != null && terms.size() > i) {
 			return (Term)terms.get(i);
 		} else {
 			return null;
 		}
 	}
 
 	public void addTerm(Term t) {
 		if (terms == null)
 			terms = new ArrayList();
 		terms.add(t);
 		functorArityBak = null;
 	}
 
 	public int getTermsSize() {
 		if (terms != null) {
 			return terms.size();
 		} else {
 			return 0;
 		}
 	}
 	public List getTerms() {
 		return terms;
 	}
 	
 	public Term[] getTermsArray() {
 		Term ts[] = null;
 		if (terms == null) {
 			ts = new Term[0];
 		} else {
 			ts = new Term[terms.size()];
 			int i = 0;
 			Iterator j = terms.iterator();
 			while (j.hasNext()) {
 				ts[i++] = (Term)j.next();
 			}
 		}
 		return ts;
 	}
 
 	public boolean isVar() {
 		if (funcSymb == null) {
 			return false;
 		} else {
 			return Character.isUpperCase(funcSymb.charAt(0));
 		}
 	}
 
 	public boolean isList() {
 		return false;
 	}
 	public boolean isString() {
 		return false;
 	}
 	
 	public boolean isGround() {
 		if (funcSymb == null) // empty predicate
 			return true;
 		if (isVar()) // variable
 			return false;
 		if (terms == null) // atom
 			return true;
 		
 		for (int i=0; i<getTermsSize(); i++) {
 			if (!getTerm(i).isGround()) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public boolean hasVar(Term t) {
 		if (this.equals(t))
 			return true;
 		for (int i=0; i<getTermsSize(); i++) {
 			if (getTerm(i).hasVar(t)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean equals(Object t) {
 		if (t == null)
 			return false;
 		try {
 			Term tAsTerm = (Term)t;
 			if (funcSymb == null && tAsTerm.funcSymb != null) {
 				return false;
 			}
 			if (funcSymb != null && !funcSymb.equals(tAsTerm.funcSymb))
 				return false;
 			if (terms == null && tAsTerm.terms == null)
 				return true;
 			if (terms == null || tAsTerm.terms == null)
 				return false;
 			if (terms.size() != tAsTerm.terms.size())
 				return false;
 
 			for (int i=0; i<getTermsSize(); i++) {
 				if (!getTerm(i).equals(tAsTerm.getTerm(i))) {
 					return false;
 				}
 			}
 			return true;
 		} catch (ClassCastException e) {
 			return false;
 		}
 	}
 
 	public int compareTo(Object t) {
 		int c;
 		if (((Term) t).funcSymb == null)
 			return 1;
 		if (funcSymb == null)
 			return -1;
 		c = funcSymb.compareTo(((Term) t).funcSymb);
 		if (c != 0)
 			return c;
 		if (terms == null && ((Term) t).terms == null)
 			return 0;
 		if (terms == null)
 			return -1;
 		if (((Term) t).terms == null)
 			return 1;
 		if (terms.size() < ((Term) t).terms.size())
 			return -1;
 		else if (terms.size() > ((Term) t).terms.size())
 			return 1;
 		Iterator i = terms.iterator();
 		Iterator j = ((Term) t).terms.iterator();
 		while (i.hasNext() && j.hasNext()) {
 			c = ((Term) i.next()).compareTo((Term) j.next());
 			if (c != 0)
 				return c;
 		}
 		return 0;
 	}
 
 	/** make a hard copy of the terms */
 	public Object clone() {
 		return new Term(this);
 	}
 
 	protected List getHardCopyOfTerms() {
 		if (terms == null) {
 			return null;
 		}
 		List l = new ArrayList(terms.size());
 		Iterator i = terms.iterator();
 		while (i.hasNext()) {
 			Term ti = (Term)i.next();
 			l.add(ti.clone());
 		}
 		return l;
 	}
 	
 	public String toString() {
		StringBuffer s = new StringBuffer();
		if (funcSymb != null) {
			s.append(funcSymb);
		}
 		if (terms != null) {
 			s.append("(");
 			Iterator i = terms.iterator();
 			while (i.hasNext()) {
 				s.append((Term) i.next());
 				if (i.hasNext())
 					s.append(",");
 			}
 			s.append(")");
 		}
 		return s.toString();
 	}
 }
