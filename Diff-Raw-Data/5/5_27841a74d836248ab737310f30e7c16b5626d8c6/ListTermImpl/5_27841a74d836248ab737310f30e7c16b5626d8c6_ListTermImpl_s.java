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
 //   Revision 1.5  2006/01/04 02:54:41  jomifred
 //   using java log API instead of apache log
 //
 //   Revision 1.4  2006/01/03 00:17:05  jomifred
 //   change in =.. (using two lists, list of terms and list of annots)
 //
 //   Revision 1.3  2005/12/30 20:40:16  jomifred
 //   new features: unnamed var, var with annots, TE as var
 //
 //   Revision 1.2  2005/12/23 12:44:04  jomifred
 //   fix a bug in VarTerm (isTail)
 //
 //   Revision 1.1  2005/12/22 00:04:34  jomifred
 //   ListTerm is now an interface implemented by ListTermImpl
 //
 //   Revision 1.6  2005/08/12 22:26:08  jomifred
 //   add cvs keywords
 //
 //
 //----------------------------------------------------------------------------
 
 package jason.asSyntax;
 
 import jason.asSyntax.parser.as2j;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 /**
  * Each nth-ListTerm has both a term and the next ListTerm.
  * The last ListTem is a emptyListTerm (term==null).
  * In lists with tail ([a|X]), next is the Tail (next=X).
  *
  * @author jomi
  */
 public class ListTermImpl extends Term implements ListTerm {
 	
 	private Term term;
 	private Term next;
 
 	static private Logger logger = Logger.getLogger(ListTermImpl.class.getName());
 	
 	public ListTermImpl() {
 		super();
 	}
 
     public static ListTerm parseList(String sList) {
         as2j parser = new as2j(new StringReader(sList));
         try {
             return (ListTerm)parser.list();
         } catch (Exception e) {
             logger.log(Level.SEVERE,"Error parsing list "+sList,e);
 			return null;
         }
     }
 	
 	/** make a hard copy of the terms */
 	public Object clone() {
 		// do not call constructor with term parameter!
 		ListTermImpl t = new ListTermImpl();
 		if (term != null) {
 			t.term = (Term)this.term.clone();
 		}
 		if (next != null) {
 			t.next = (Term)this.next.clone();
 		}
 		return t;
 	}
 	
 
 	public boolean equals(Object t) {
 		try {
 			ListTermImpl tAsTerm = (ListTermImpl)t;
 			if (term == null && tAsTerm.term != null) {
 				return false;
 			}
 			if (term != null && !term.equals(tAsTerm.term)) {
 				return false;
 			}
 			if (next != null) {
 				return next.equals(tAsTerm.next);
 			}
 			return true;
 		} catch (ClassCastException e) {
 			return false;
 		}
 	}
 	
 	public void setTerm(Term t) {
 		term = t;
 	}
 	
 	/** gets the term of this ListTerm */
 	public Term getTerm() {
 		return term;
 	}
 	
 	public void setNext(Term l) {
 		next = l;
 	}
 	
 	public ListTerm getNext() {
 		try {
 			return (ListTerm)next;
 		} catch (Exception e){}
 		return null;
 	}
 	
 	
 	// for unifier compatibility
 	public int getTermsSize() {
 		if (isEmpty()) {
 			return 0;
 		} else {
 			return 2; // term and next
 		}
 	}
 	// for unifier compatibility
 	public Term getTerm(int i) {
 		if (i == 0) {
 			return term;
 		}
 		if (i == 1) {
 			return next;
 		}
 		return null;
 	}
 	
 	/** return the this ListTerm elements (0=Term, 1=ListTerm) */
 	public List getTerms() {
 		List l = new ArrayList(2);
 		if (term != null) {
 			l.add(term);
 		}
 		if (next != null) {
 			l.add(next);
 		}
 		return l;
 	}
 	
 	public void addTerm(Term t) {
 		logger.warning("Do not use addTerm in lists! Use add.");
 	}
 
 	public int size() {
 		if (isEmpty()) {
 			return 0;
 		} else if (isTail()) {
 			return 1;
 		} else {
 			return getNext().size() + 1;
 		}
 	}
 	
 	public boolean isList() {
 		return true;
 	}
 	public boolean isEmpty() {
 		return term == null;
 	}
 	public boolean isEnd() {
 		return isEmpty() || isTail();
 	}
 
 	public boolean isGround() {
 		Iterator i = iterator();
 		while (i.hasNext()) {
 			Term t = (Term)i.next();
 			if (!t.isGround()) {
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	public boolean isTail() {
 		return next != null && next.isVar();
 	}
 	
 	/** returns this ListTerm's tail element in case the List has the Tail, otherwise, returns null */
 	public VarTerm getTail() {
 		if (isTail()) {
 			return (VarTerm)next;
 		} else if (next != null) {
 			return getNext().getTail();
 		} else {
 			return null;
 		}
 	}
 	
 	/** set the tail of this list */
 	public void setTail(VarTerm v) {
 		if (getNext().isEmpty()) {
 			next = v;
 		} else {
 			getNext().setTail(v);
 		}
 	}
 	
 	/** get the last ListTerm of this List */
 	public ListTerm getLast() {
 		if (isEnd()) {
 			return this;
 		} else if (next != null) {
 			return getNext().getLast();
 		} 
 		return null; // !!! no last!!!!
 	}
 	
 	
 	/** 
 	 * add a term in the end of the list
 	 * @return the ListTerm where the term was added
 	 */
 	public ListTerm add(Term t) {
 		if (isEmpty()) {
 			term = t;
 			next = new ListTermImpl();
 			return this;
 		} else if (isTail()) {
 			// What to do?
 			return null;
 		} else {
 			return getNext().add(t);
 		}
 	}
 
 	/** add a term in the end of the list
 	 * @return the ListTerm where the term was added
 	 */
 	public ListTerm add(int index, Term t) {
 		if (index == 0) {
 			ListTermImpl n = new ListTermImpl();
 			n.term = this.term;
 			n.next = this.next;
 			this.term = t;
 			this.next = n;
 			return n;
 		} else if (index > 0 && getNext() != null) {
 			return getNext().add(index-1,t);
 		} else {
 			return null;
 		}
 		
 	}
 	
 	/** Add a list in the end of this list.
 	 * This method do not clone <i>lt</i>.
 	 * @return the last ListTerm of the new list
 	 */
 	public ListTerm concat(ListTerm lt) {
		if ( ((ListTerm)next).isEmpty() ) {
 			next = (Term)lt;
 		} else {
 			((ListTerm)next).concat(lt);
 		}
 		return lt.getLast();
 	}
 
 	
 	/** returns an iterator where each element is a ListTerm */
 	public Iterator listTermIterator() {
 		final ListTermImpl lt = this;
 		return new Iterator() {
 			ListTerm nextLT  = lt;
 			ListTerm current = null;
 			public boolean hasNext() {
 				return nextLT != null && !nextLT.isEmpty() && nextLT.isList(); 
 			}
 			public Object next() {
 				current = nextLT;
 				nextLT = nextLT.getNext();
 				return current;
 			}
 			public void remove() {
 				if (current != null) {
 					if (nextLT != null) {
 						current.setTerm(nextLT.getTerm());
 						current.setNext((Term)nextLT.getNext());
 						nextLT = current;
 					}
 				}
 			}
 		};
 	}
 
 	/** returns an iterator where each element is a Term of this list */
 	public Iterator iterator() {
 		final Iterator i = this.listTermIterator();
 		return new Iterator() {
 			public boolean hasNext() {
 				return i.hasNext();
 			}
 			public Object next() {
 				return ((ListTerm)i.next()).getTerm();
 			}
 			public void remove() {
 				i.remove();
 			}
 		};
 	}
 	
 	
 	/** 
 	 * Returns this ListTerm as a Java List. 
 	 * Note: the list Tail is considered just as the last element of the list!
 	 */
     public List getAsList() {
         List l = new ArrayList();
 		Iterator i = iterator();
 		while (i.hasNext()) {
 			l.add( i.next() );
 		}
 		return l;
     }
 
 	
 	public String toString() {
 		StringBuffer s = new StringBuffer("[");
 		Iterator i = listTermIterator();
 		while (i.hasNext()) {
 			ListTerm lt = (ListTerm)i.next();
 			//System.out.println(s+"/cur="+lt.getTerm()+"/"+lt.getNext()+"/"+lt.getClass());
 			s.append( lt.getTerm() );
 			if (lt.isTail()) {
 				s.append("|");
 				s.append(lt.getNext());
 			} else if (i.hasNext()) {
 				s.append(",");
 			}
 		}
 		s.append("]");
 		return s.toString();
 	}
 
 	//
 	// Java List interface methods
 	//
 	
 	public void add(int index, Object o) {
 		add(index, (Term)o);
 	}
 	public boolean add(Object o) {
 		return add((Term)o) != null;
 	}
 	public boolean addAll(Collection c) {
 		ListTerm lt = this; // where to add
 		Iterator i = c.iterator();
 		while (i.hasNext()) {
 			lt = lt.add((Term)i.next());
 		}
 		return true;
 	}
 	public boolean addAll(int index, Collection c) {
 		Iterator i = c.iterator();
 		int p = index;
 		while (i.hasNext()) {
 			add(p, i.next()); 
 			p++;
 		}
 		return true;
 	}
 	public void clear() {
 		term = null;
 		next = null;
 	}
 
 	public boolean contains(Object o) {
 		Term t = (Term)o;
 		if (term != null && term.equals(t)) {
 			return true;
 		} else if (getNext() != null) {
 			return getNext().contains(o);
 		}
 		return false;
 	}
 
 	public boolean containsAll(Collection c) {
 		boolean r = true;
 		Iterator i = c.iterator();
 		while (i.hasNext() && r) {
 			r = r && contains(i.next()); 
 		}
 		return r;
 	}
 
 	public Object get(int index) {
 		if (index == 0) {
 			return this.term;
 		} else if (getNext() != null) {
 			return getNext().get(index-1);
 		}
 		return null;
 	}
 
 	public int indexOf(Object o) {
 		Term t = (Term)o;
 		if (this.term.equals(t)) {
 			return 0;
 		} else if (getNext() != null) {
 			int n = getNext().indexOf(o);
 			if (n >= 0) {
 				return n+1;
 			}
 		}
 		return -1;
 	}
 	public int lastIndexOf(Object arg0) {
 		return getAsList().lastIndexOf(arg0);
 	}
 
 	public ListIterator listIterator() {
 		logger.warning("listIterator() is not implemented!");
 		return null;
 	}
 	public ListIterator listIterator(int arg0) {
 		logger.warning("listIterator() is not implemented!");
 		return null;
 	}
 
 	public Object remove(int index) {
 		if (index == 0) {
 			Term bt = this.term;
 			if (getNext() != null) {
 				this.term = getNext().getTerm();
 				this.next = (Term)getNext().getNext();
 			} else {
 				clear();
 			}
 			return bt;
 		} else if (getNext() != null) {
 			return getNext().remove(index-1);
 		}
 		return null;
 	}
 
 	public boolean remove(Object o) {
 		Term t = (Term)o;
 		if (term != null && term.equals(t)) {
 			if (getNext() != null) {
 				this.term = getNext().getTerm();
 				this.next = (Term)getNext().getNext();
 			} else {
 				clear();
 			}
 			return true;
 		} else if (getNext() != null) {
 			return getNext().remove(o);
 		}
 		return false;
 	}
 
 	public boolean removeAll(Collection c) {
 		boolean r = true;
 		Iterator i = c.iterator();
 		while (i.hasNext() && r) {
 			r = r && remove(i.next()); 
 		}
 		return r;
 	}
 
 	public boolean retainAll(Collection c) {
 		boolean r = true;
 		Iterator i = iterator();
 		while (i.hasNext()) {
 			Term t = (Term)i.next();
 			if (!c.contains(t)) {
 				r = r && remove(t);
 			}
 		}
 		return r;
 	}
 
 	public Object set(int index, Object o) {
 		if (index == 0) {
 			this.term = (Term)o;
 			return o;
 		} else if (getNext() != null) {
 			return getNext().set(index-1, o);
 		}
 		return null;
 	}
 
 	public List subList(int arg0, int arg1) {
 		return getAsList().subList(arg0, arg1);
 	}
 
 	public Object[] toArray() {
 		return getAsList().toArray();
 	}
 
 	public Object[] toArray(Object[] arg0) {
 		return getAsList().toArray(arg0);
 	}
 }
