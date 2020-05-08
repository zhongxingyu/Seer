 package ch11.ex01;
 
 public class LinkedList<E> {
 	public Item<E> head;
 	
 	public void add(E val) {
 		/**
		 * w̗vfŌɒǉ
 		 */
 		Item<E> aItem = head;
 		Item<E> item = new Item<E>(val);
 		if (aItem == null) {
 			head = item;
 			return;
 		}		
 		while (aItem != null) {
 			if (aItem.getNext() == null) {
 				aItem.setNext(item);
 				return;
 			}
 			aItem = aItem.getNext();
 		}
 	}
 	
 	public E getValue(int index) throws Exception {
 		/**
		 * indexԖڂ̗vf擾
 		 */
 		Item<E> aItem = head;
 		int i = 0;
 		while (aItem != null) {
 			if (i == index) {
 				return aItem.getValue();
 			}
 			aItem = aItem.getNext();
 			i++;
 		}
 		throw new Exception("index is out of range!!");
 	}
 	
 	static class Item<E> {
 		private E value;
 		private Item<E> next;
 		
 		public Item(E val) {
 			value = val;
 		}
 		
 		public E getValue() {
 			return value;
 		}
 		
 		public Item<E> getNext() {
 			return next;
 		}
 		
 		public void setNext(Item<E> nextItem) {
 			next = nextItem;
 		}
 	}
 }
