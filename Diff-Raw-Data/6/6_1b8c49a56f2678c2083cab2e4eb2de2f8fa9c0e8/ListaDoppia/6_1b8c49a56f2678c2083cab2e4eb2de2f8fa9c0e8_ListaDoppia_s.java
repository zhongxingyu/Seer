 package poo.util;
 
 import java.util.*;
 
 public class ListaDoppia<T extends Comparable<? super T>> extends CollezioneOrdinataAstratta<T> {
 	private static class Nodo<E> {
 		E info;
 		Nodo<E> next, prior;
 	} // Nodo
 	private Nodo<T> testa = null;
 	private int size = 0;
 	public int size() { return size; }
 	public void clear() {
 		testa = null; size = 0;
 	}
 	public boolean isEmpty() { return testa == null; }
 	public void add(T e) {
 		Nodo<T> nuovo = new Nodo<T>();
 		nuovo.info = e;
 		if (testa == null || testa.info.compareTo(e) >= 0) {
 			nuovo.prior = null; nuovo.next = testa;
 			if (testa != null) testa.prior = nuovo;
 			testa = nuovo;
 		} else {
 			Nodo<T> pre = testa, cor = testa.next;
 			while (cor != null && cor.info.compareTo(e) < 0) {
 				pre = cor; cor = cor.next;
 			}
 			nuovo.prior = pre; nuovo.next = cor;
 			if (cor != null) cor.prior = nuovo;
 			pre.next = nuovo;
 		}
 		size++;
 	} // add
 	public void remove(T e) {
 		Nodo<T> cor = testa;
 		while (cor != null && cor.info.compareTo(e) < 0)
 			cor = cor.next;
 		if (cor != null && cor.info.equals(e)) {
 			if (cor == testa) {
 				testa = testa.next;
 				if (testa != null) testa.prior = null;
 			} else {
 				cor.prior.next = cor.next;
 				if (cor.next != null) cor.next.prior = cor.prior;
 				cor.prior = null; cor.next = null;
 			}
 			cor = null; size--;
 		}
 	} // remove
 	public Iterator<T> iterator() {
 		return new ListaDoppiaIterator();
 	} // iterator
 	private class ListaDoppiaIterator implements Iterator<T> {
 		private Nodo<T> cor = null; public boolean rimovibile = false;
 		public boolean hasNext() {
 			if (cor == null) return testa != null;
 			return cor.next != null;
 		} // hasNext
 		public T next() {
 			if (!hasNext()) throw new NoSuchElementException();
 			if (cor == null) cor = testa;
 			else cor = cor.next;
 			rimovibile = true;
 			return cor.info;
 		} // next
 		public void remove() {
 			if (!rimovibile) throw new IllegalStateException();
 			rimovibile = false;
 			if (cor == testa) {
 				testa = testa.next;
 				if (testa != null) testa.prior = null;
 			} else {
 				cor.prior.next = cor.next;
 				if (cor.next != null) cor.next.prior = cor.prior;
 				cor.next = null; cor.prior = null;
 			}
			cor = null; size--;
 		} // remove
 	} // ListaDoppiaIterator
 } // ListaDoppia
