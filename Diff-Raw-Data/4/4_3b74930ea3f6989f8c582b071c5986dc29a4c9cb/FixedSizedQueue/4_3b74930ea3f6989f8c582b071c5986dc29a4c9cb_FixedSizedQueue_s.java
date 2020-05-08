 /*
  * Copyright (C) 2007-2009 Solertium Corporation
  *
  * This file is part of the open source GoGoEgo project.
  *
  * Unless you have been granted a different license in writing by the
  * copyright holders for GoGoEgo, you may only modify or redistribute
  * this code under the terms of one of the following licenses:
  * 
  * 1) The Eclipse Public License, v.1.0
  *    http://www.eclipse.org/legal/epl-v10.html
  *
  * 2) The GNU General Public License, version 2 or later
  *    http://www.gnu.org/licenses
  */
 package com.solertium.util.gwt.api;
 
 /**
  * FixedSizeQueue.java
  *
  * @author adam.schwartz
  * @author carl.scott
  *
  */
 public class FixedSizedQueue {
 	
 	private String [] array;
     
     int start;
     int end;
     int capacity;
    
     public FixedSizedQueue(int capacity) {
         array = new String[capacity];
         this.capacity = capacity;
         start = 0;
         end = 0 ;
     }
    
     public void add(String object) {
     	   	
         if( end == capacity ) {
            array[start] = object;
            start = start+1 % capacity;
         } else {
              array[end++] = object;
         }
     }
    
     public String get(int i) {
     	if (i > end)
     		throw new IndexOutOfBoundsException("Index " + i + " out of bounds.");
     	
         if( end == capacity ) {
             //offset i using start
             return array[(i + start) % capacity];
         } else
             return array[i];
     }
 
     public String[] getAll() {
     	int size = end < capacity ? end : capacity; 
     	final String[] out = new String[size];
     	for (int i = 0; i < size; i++)
     		out[i] = array[(i + start) % capacity];
     	return out;
     }
 }
