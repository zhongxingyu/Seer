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
 // CVS information:
 //   $Date$
 //   $Revision$
 //   $Log$
 //   Revision 1.19  2006/01/04 02:54:41  jomifred
 //   using java log API instead of apache log
 //
 //
 //----------------------------------------------------------------------------
 
 package jason.asSyntax;
 
 import jason.asSyntax.parser.as2j;
 
 import java.io.StringReader;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 /**
  * Represents a variable Term: like X (starts with upper case). 
  * It may have a value, afert Unifier.apply.
  * 
  * @author jomi
  */
 public class VarTerm extends Literal implements NumberTerm, ListTerm, StringTerm {
 	static private Logger logger = Logger.getLogger(VarTerm.class.getName());
 	
 	private Term value = null;
 	
 	public VarTerm() {
 		super();
 	}
 
 	public VarTerm(String s) {
 		if (s != null && Character.isLowerCase(s.charAt(0))) {
 			logger.warning("Are you sure you want to create a VarTerm that begins with lowercase ("+s+")? Should it be a Term instead?");			
 		}
 		setFunctor(s);
 	}
 	
 	public static VarTerm parseVar(String sVar) {
 		as2j parser = new as2j(new StringReader(sVar));
 		try {
 			return parser.var();
 		} catch (Exception e) {
 			logger.log(Level.SEVERE,"Error parsing var " + sVar,e);
 			return null;
 		}
 	}
 
 	public Object clone() {
 		// do not call constructor with term parameter!
 		VarTerm t = new VarTerm();
 		t.setFunctor(super.getFunctor());
 		if (value != null) {
 			t.value = (Term)value.clone();
 		} else {
 			if (getAnnots() != null && !getAnnots().isEmpty()) {
 				t.setAnnots((ListTerm)getAnnots().clone());
 			}
 		}
 		return t;
 	}
 	
 	public boolean isVar() {
 		return !isGround();
 	}
 	
 	public boolean isUnnamedVar() {
 		return false;
 	}
 
 	public boolean isGround() {
 		return getValue() != null;
 	}
 	
 	/** grounds a variable, set a value for this var (e.g. X = 10; Y = a(b,c); ...) */
 	public boolean setValue(Term vl) {
 		try {
 			// If vl is a Var, find out a possible loop
 			VarTerm vlvl = (VarTerm)((VarTerm)vl).value; // not getValue! (use the "real" value)
 			while (vlvl != null) {
 				if (vlvl == this) {
 					logger.warning("Attempted loop in VarTerm values of "+this.getFunctor());
 					return false;
 				}
 				vlvl = (VarTerm)vlvl.value;
 			}
 		} catch (Exception e) {}
 		value = vl;
 		return true;
 	}
 	
 	/** returns true if this var has a value */
 	public boolean hasValue() {
 		return value != null;
 	}
 	
 	/** 
 	 * When a var has another var as value, returns the last var in the chain.
 	 * Otherwise, return this. 
 	 * The returned VarTerm is normally used to set/get a value for the var. 
 	 */
 	public VarTerm getLastVarChain() {
 		try {
 			// if value is a var, return it
 			return ((VarTerm)value).getLastVarChain();
 		} catch (Exception e) {}
 		return this;
 	}
 	
 	/** 
 	 * returns the value of this var. 
 	 * if value is also a var, returns the value of this latter var
 	 * 
 	 * @return
 	 */
 	public Term getValue() {
 		return getLastVarChain().value;
 	}
 
 	public boolean equals(Object t) {
 		if (t == null)
 			return false;
 		try {
 			Term tAsTerm = (Term)t;
 			Term vl = getValue();
 			//System.out.println("cheking equals form "+tAsTerm.getFunctor()+" and this "+this.getFunctor()+" my value "+vl);
 			if (vl == null) {
 				// is t also a var? (its value must also be null)
 				try {
 					VarTerm tAsVT = (VarTerm)t;
 					if (tAsVT.getValue() != null) {
 						return false;
 					}
 
 					boolean ok = getFunctor().equals(tAsTerm.getFunctor());
 					if (!ok) {
 						return false;
 					}
 					// no value, the var names and annots must be equal
 					if (getAnnots() == null && tAsVT.getAnnots() != null) {
 						return false;
 					}
 					if (getAnnots() != null && !getAnnots().equals(tAsVT.getAnnots())) {
 						return false;
 					}
 					return true;
 				} catch (Exception e) {
 					return false;
 				}
 				
 			} else {
 				// campare the values
 				return vl.equals(t);
 			}
 		} catch (ClassCastException e) {
 		}
 		return false;
 	}
 	
 	
 	// ----------
 	// Term methods overridden
 	// 
 	// in case this VarTerm has a value, use value's methods
 	// ----------
 	
 	public String getFunctor() {
 		if (value == null) {
 			return super.getFunctor();
 		} else {
 			return getValue().getFunctor();
 		}
 	}
 
 	
 	public String getFunctorArity() {
 		if (value != null)
 			return value.getFunctorArity();
 		else 
 			return null;
 	}
 
 	public int hashCode() {
 		if (value != null)
 			return value.hashCode();
 		else 
 			return super.hashCode();
 	}
 
 	public Term getTerm(int i) {
 		if (value == null) {
 			return null;
 		} else {
 			return getValue().getTerm(i);
 		}
 	}
 
 	public void addTerm(Term t) {
 		if (value != null) {
 			getValue().addTerm(t);
 		}
 	}
 
 	public int getTermsSize() {
 		if (value == null) {
 			return 0;
 		} else {
 			return getValue().getTermsSize();
 		}
 	}
 
 	public List getTerms() {
 		if (value == null) {
 			return null;
 		} else {
 			return getValue().getTerms();
 		}
 	}
 	
 	public void setTerms(List l) {
 		if (value != null) {
 			value.setTerms(l);
 		}
 	}
 	
 	public void addTerms(List l) {
 		if (value != null) {
 			value.addTerms(l);
 		}
 	}
 
 	public Term[] getTermsArray() {
 		if (value == null) {
 			return null;
 		} else {
 			return value.getTermsArray();
 		}
 	}
 
 	public boolean isInternalAction() {
 		return value != null && getValue().isInternalAction();
 	}
 	
 	public boolean isList() {
 		return value != null && getValue().isList();
 	}
 	public boolean isString() {
 		return value != null && getValue().isString();
 	}
 	public boolean isNumber() {
 		return value != null && getValue().isNumber();
 	}
 	public boolean isPred() {
 		return value != null && getValue().isPred();
 	}
 	public boolean isLiteral() {
 		return value != null && getValue().isLiteral();
 	}
 	public boolean isExpr() {
 		return value != null && getValue().isExpr();
 	}
 	
 	public boolean hasVar(Term t) {
 		if (value == null) {
 			return super.hasVar(t);
 		} else {
 			return getValue().hasVar(t);
 		}
 	}
 	
 	public String toString() {
 		if (value == null) {
 			// no value, the var name must be equal
 			String s = getFunctor();
 			if (getAnnots() != null && !getAnnots().isEmpty()) {
 				s += getAnnots();
 			}
 			return s;
 		} else {
 			// campare the values
 			return getValue().toString();
 		}
 	}
 
 	
 	// ----------
 	// Pred methods overridden
 	// 
 	// in case this VarTerm has a value, use value's methods
 	// ----------
 
 	public void setAnnots(ListTerm l) {
 		if (value != null && getValue().isPred())
 			((Pred)getValue()).setAnnots(l);
 		else
 			super.setAnnots(l);
 	}
 
 	public void addAnnot(int index, Term t) {
 		if (value != null && getValue().isPred())
 			((Pred)getValue()).addAnnot(index, t);
 		else
 			super.addAnnot(index,t);
 	}
 
 	public void importAnnots(Pred p) {
 		if (value != null && getValue().isPred())
 			((Pred)getValue()).importAnnots(p);
 		else
 			super.importAnnots(p);
 	}
 
 	public void addAnnot(Term t) {
 		if (value != null && getValue().isPred())
 			((Pred)getValue()).addAnnot(t);
 		else
 			super.addAnnot(t);
 	}
 
 	public void addAnnots(List l) {
 		if (value != null && getValue().isPred())
 			((Pred)getValue()).addAnnots(l);
 		else
 			super.addAnnots(l);
 	}
 
 	public void clearAnnots() {
 		if (value != null && getValue().isPred())
 			((Pred)getValue()).clearAnnots();
 		else
 			super.clearAnnots();
 	}
 
 	public void delAnnot(Pred p) {
 		if (value != null && getValue().isPred())
 			((Pred)getValue()).delAnnot(p);
 		else
 			super.delAnnot(p);
 	}
 
 	public void delAnnot(Term t) {
 		if (value != null && getValue().isPred())
 			((Pred)getValue()).delAnnot(t);
 		else
 			super.delAnnot(t);
 	}
 
 	public boolean hasAnnot(Term t) {
 		if (value != null && getValue().isPred())
 			return ((Pred)getValue()).hasAnnot(t);
 		else
 			return super.hasAnnot(t);
 	}
 
 	public boolean hasAnnot() {
 		if (value != null && getValue().isPred())
 			return ((Pred)getValue()).hasAnnot();
 		else
 			return super.hasAnnot();
 	}
 
 	public ListTerm getAnnots() {
 		if (value != null && getValue().isPred())
 			return ((Pred)getValue()).getAnnots();
 		else 
 			return super.getAnnots();
 	}
 
 	public void addSource(Term t) {
 		if (value != null && getValue().isPred())
 			((Pred)getValue()).addSource(t);
 		else
 			super.addSource(t);
 		
 	}
 
 	public boolean delSource(Term s) {
 		if (value != null && getValue().isPred())
 			return ((Pred)getValue()).delSource(s);
 		else
 			return super.delSource(s);
 	}
 
 	public void delSources() {
 		if (value != null && getValue().isPred())
 			((Pred)getValue()).delSources();
 		else
 			super.delSources();
 	}
 
 	public ListTerm getSources() {
 		if (value != null && getValue().isPred())
 			return ((Pred)getValue()).getSources();
 		else 
 			return super.getSources();
 	}
 
 	public boolean hasSource() {
 		if (value != null && getValue().isPred())
 			return ((Pred)getValue()).hasSource();
 		else
 			return super.hasSource();
 	}
 
 	public boolean hasSource(Term s) {
 		if (value != null && getValue().isPred())
 			return ((Pred)getValue()).hasSource(s);
 		else
 			return super.hasSource(s);
 	}
 	
 	
 	// ----------
 	// Literal methods overridden
 	// 
 	// in case this VarTerm has a value, use value's methods
 	// ----------
 	
 	public boolean negated() {
 		return value != null && getValue().isLiteral() && ((Literal)getValue()).negated();
 	}
 	
 	
 	// ----------
 	// ArithmeticExpression methods overridden
 	// Interface NumberTerm
 	// ----------
 
 	public double solve() {
 		if (hasValue() && value.isNumber()) {
 			return ((NumberTerm)value).solve();
 		} else {
 			logger.warning("Error getting numerical value of VarTerm "+this);
 		}
 		return 0;
 	}
 
 	
 	// ----------
 	//
 	// ListTerm methods overridden
 	// 
 	// ----------
 
 
 	public void add(int index, Object o) {
 		if (value != null && getValue().isList())
 			((ListTerm)getValue()).add(index, o);
 	}
 	public boolean add(Object o) {
 		return value != null && getValue().isList() && ((ListTerm)getValue()).add(o);
 	}
 	public boolean addAll(Collection c) {
 		return value != null && getValue().isList() && ((ListTerm)getValue()).addAll(c);
 	}
 	public boolean addAll(int index, Collection c) {
 		return value != null && getValue().isList() && ((ListTerm)getValue()).addAll(index, c);
 	}
 	public void clear() {
 		if (value != null && getValue().isList())
 			((ListTerm)getValue()).clear();
 	}
 	public boolean contains(Object o) {
 		return value != null && getValue().isList() && ((ListTerm)getValue()).contains(o);
 	}
 	public boolean containsAll(Collection c) {
 		return value != null && getValue().isList() && ((ListTerm)getValue()).containsAll(c);
 	}
 	public Object get(int index) {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).get(index);
 		else 
 			return null;
 	}
 	public int indexOf(Object o) {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).indexOf(o);
 		else 
 			return -1;
 	}
 	public int lastIndexOf(Object o) {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).lastIndexOf(o);
 		else 
 			return -1;
 	}
 	public Iterator iterator() {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).iterator();
 		else 
 			return null;
 	}
 	public ListIterator listIterator() {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).listIterator();
 		else 
 			return null;
 	}
 	public ListIterator listIterator(int index) {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).listIterator(index);
 		else 
 			return null;
 	}
 	public Object remove(int index) {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).remove(index);
 		else 
 			return null;
 	}
 	public boolean remove(Object o) {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).remove(o);
 		else 
 			return false;
 	}
 	public boolean removeAll(Collection c) {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).removeAll(c);
 		else 
 			return false;
 	}
 	public boolean retainAll(Collection c) {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).retainAll(c);
 		else 
 			return false;
 	}
 	public Object set(int index, Object o) {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).set(index, o);
 		else 
 			return null;
 	}
 	public List subList(int arg0, int arg1) {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).subList(arg0, arg1);
 		else 
 			return null;
 	}
 	public Object[] toArray() {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).toArray();
 		else 
 			return null;
 	}
 	public Object[] toArray(Object[] arg0) {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).toArray(arg0);
 		else 
 			return null;
 	}
 
 	// from ListTerm
 	
 	public void setTerm(Term t) {
 		if (value != null && getValue().isList())
 			((ListTerm)getValue()).setTerm(t);
 	}
 
 	public void setNext(Term t) {
 		if (value != null && getValue().isList())
 			((ListTerm)getValue()).setNext(t);
 	}
 
 	public ListTerm add(int index, Term t) {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).add(index, t);
 		else 
 			return null;
 	}
 	public ListTerm add(Term t) {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).add(t);
 		else 
 			return null;
 	}
 	public ListTerm concat(ListTerm lt) {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).concat(lt);
 		else 
 			return null;
 	}
 	public List getAsList() {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).getAsList();
 		else 
 			return null;
 	}
 
 	public ListTerm getLast() {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).getLast();
 		else 
 			return null;
 	}
 
 	public ListTerm getNext() {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).getNext();
 		else 
 			return null;
 	}
 
 	public Term getTerm() {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).getTerm();
 		else 
 			return null;
 	}
 
 	public boolean isEmpty() {
 		return value != null && getValue().isList() && ((ListTerm)getValue()).isEmpty();
 	}
 
 	public boolean isEnd() {
 		return value != null && getValue().isList() && ((ListTerm)getValue()).isEnd();
 	}
 
 	public boolean isTail() {
 		return value != null && getValue().isList() && ((ListTerm)getValue()).isTail();
 	}
 	public void setTail(VarTerm v) {
 		if (value != null && getValue().isList())
 			((ListTerm)getValue()).setTail(v);
 	}
 	public VarTerm getTail() {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).getTail();
 		else 
 			return null;
 	}
 
 
 	public Iterator listTermIterator() {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).listTermIterator();
 		else 
 			return null;
 	}
 
 
 	public int size() {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).size();
 		else 
 			return -1;
 	}
 	
 	// -----------------------
 	// StringTerm interface implementation
 	// -----------------------
 	
 	public void setString(String s) {
 		if (value != null && getValue().isString())
 			((StringTerm)getValue()).setString(s);
 	}
 	public String getString() {
 		if (value != null && getValue().isString())
 			return ((StringTerm)getValue()).getString();
 		else
 			return null;
 	}
 	public int length() {
 		if (value != null && getValue().isString())
 			return ((StringTerm)getValue()).length();
 		else
 			return -1;
 	}
 
 }
