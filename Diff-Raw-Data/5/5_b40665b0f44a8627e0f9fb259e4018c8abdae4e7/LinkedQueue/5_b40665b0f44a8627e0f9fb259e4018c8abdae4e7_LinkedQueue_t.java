 package com.brandonkimfoster.concrete;
 
 import java.util.NoSuchElementException;
 
 import com.brandonkimfoster.Node;
 import com.brandonkimfoster.api.Queue;
 
 /**
  * A concrete implementation of the Queue interface in com.brandonkimfoster.api
  * that uses an underlying chain of linked nodes.
  * 
  * @author Brandon Foster
  * @version 2013.12.29
  * 
  * @param <T>
 *            the type of elements stored in the queue
  */
 public class LinkedQueue<T> implements Queue<T> {
 
 	private Node<T> head;
 	private Node<T> tail;
 	private int size;
 
 	/**
 	 * Constructor
 	 */
 	public LinkedQueue() {
 		this.head = null;
 		this.tail = null;
 		this.size = 0;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void enqueue(T item) {
 		// Node to wrap the item
 		Node<T> newNode = new Node<T>(item);
 
 		// when queue is empty to begin with, tail is null
 		if (this.tail == null) {
 			// head and tail point to the same node when there is only one item
 			// in the queue
 			this.head = newNode;
 		} else {
 			// make the node at tail point to the newNode
 			this.tail.setLink(newNode);
 		}
 
 		// update tail to point to the newNode
 		this.tail = newNode;
 
 		// increment the size of the queue
 		this.size++;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public T dequeue() {
 
 		if (this.isEmpty()) {
 			throw new NoSuchElementException();
 		}
 
 		// store the item at the head of the queue
 		T item = this.head.data();
 
 		// update head to point to the next item the queue
 		this.head = this.head.getLink();
 
 		// if the update pointer to the next item (the item immediately after
 		// the item being dequeue'd) is null, the queue is empty,
 		// so update tail to be null as well
 		if (this.head == null) {
 			this.tail = null;
 		}
 
 		// decrement the size of the queue
 		this.size--;
 
 		return item;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public T head() {
 		if (this.head == null) {
 			throw new NoSuchElementException();
 		}
 		return this.head.data();
 	}

 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean isEmpty() {
 		return (this.size() == 0);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int size() {
 		return this.size;
 	}
 
 }
