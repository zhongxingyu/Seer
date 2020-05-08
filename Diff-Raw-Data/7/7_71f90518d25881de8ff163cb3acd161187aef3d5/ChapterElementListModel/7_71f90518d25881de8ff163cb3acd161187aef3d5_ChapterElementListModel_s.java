 package de.uni_tuebingen.wsi.ct.slang2.dbc.tools;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.Iterator;
 
 import javax.swing.AbstractListModel;
 
 import de.uni_tuebingen.wsi.ct.slang2.dbc.data.ChapterElement;
 
 public class ChapterElementListModel<E extends ChapterElement>
 	extends AbstractListModel
 	implements Collection<E>
 {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 6279236503821583372L;
 	ArrayList<E> elements;
 	
 	public ChapterElementListModel() {
 		this.elements = new ArrayList<E>();
 	}
 	
 	public E getElementWithHigherStartPosition(int startPos) {
 		for (E iterable_element : this.elements) {
 			if(iterable_element.getStartPosition() > startPos )
 				return iterable_element;
 		}
 		return null;
 	}
 	
 	public E getElementWithLowerStartPosition(int startPos) {
 		int i = 0;
 		for (; i < this.elements.size(); i++) {
 			if(this.elements.get(i).getStartPosition() > startPos)
 				break;
 		}
 		if( --i >= 0)
 			return this.elements.get(i);
 		else
 			return null;
 	}
 	
 	public E getElementContainingPosition(int position) {
 		for (E e : this) {
 			if(e.getStartPosition() <= position && e.getEndPosition() >= position)
 				return e;
 		}
 		return null;
 	}
 	
 	private class ChapterElementComparator implements Comparator<E> {
 		public int compare(E arg0, E arg1) {
 			if (arg0.getStartPosition() < arg1.getStartPosition()) {
 				return -1;
 			} else if (arg0.getStartPosition() > arg1.getStartPosition()) {
 				return 1;
 			} else return 0;
 		}
 		
 	}
 
 	public Object getElementAt(int arg0) {
 		return this.elements.get(arg0);
 	}
 
 	public int getSize() {
 		return size();
 	}
 
 	public void addElement(E arg0) {
 		add(arg0);
 	}
 
 	public boolean add(E o) {
 		boolean ret = false;
 		for (int i = 0; i < size(); i++) {
 			if(this.elements.get(i).getStartPosition() > o.getStartPosition()) {
 				this.elements.add(i, o);
 				ret = this.elements.get(i) == o;
 				break;
 			}
 		}
		ret = this.elements.add(o);
		if(ret) {
 			int index = this.elements.indexOf(o);
 			fireIntervalAdded(this, index, index);
 		}
 		return ret;
 	}
 
 	public boolean addAll(Collection<? extends E> c) {
 		boolean ret = false;
 		for (E e : c) {
 			boolean tmp = add(e);
 			ret = (ret) ? true : tmp;
 		}
 		return ret;
 	}
 
 	public void clear() {
 	    	if(this.elements.size() == 0)
 	    	    return;
 		int index1 = this.elements.size()-1;
 		this.elements.clear();
 		this.fireIntervalRemoved(this, 0, index1);
 	}
 
 	public boolean contains(Object o) {
 		return this.elements.contains(o);
 	}
 
 	public boolean containsAll(Collection<?> c) {
 		return this.elements.containsAll(c);
 	}
 
 	public boolean isEmpty() {
 		return this.elements.isEmpty();
 	}
 
 	public Iterator<E> iterator() {
 		// iterator.remove() does not fire any change notice
 		return this.elements.iterator();
 	}
 
 	public boolean remove(Object o) {
 		int index = this.elements.indexOf(o);
 		if(this.elements.remove(o)) {
 			this.fireIntervalRemoved(this, index, index);
 			return true;
 		}
 		return false;
 	}
 
 	public boolean removeAll(Collection<?> c) {
 		boolean ret = false;
 		for (Object object : c) {
 			boolean tmp = remove(object);
 			ret = (ret) ? true : tmp;
 		}
 		return ret;
 	}
 
 	public boolean retainAll(Collection<?> c) {
 		boolean ret = false;
 		for (Object object : this.elements) {
 			if( ! c.contains(object) ) {
 				boolean tmp = remove(object);
 				ret = (ret) ? true : tmp;
 			}
 		}
 		return ret;
 	}
 
 	public int size() {
 		return this.elements.size();
 	}
 
 	public Object[] toArray() {
 		return this.elements.toArray();
 	}
 
 	public <T> T[] toArray(T[] a) {
 		return this.elements.toArray(a);
 	}
 
 
 }
