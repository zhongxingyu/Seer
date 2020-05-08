 /*
  * Copyright (C) 1998-2001 Semiotek Inc.  All Rights Reserved.  
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted under the terms of either of the following
  * Open Source licenses:
  *
  * The GNU General Public License, version 2, or any later version, as
  * published by the Free Software Foundation
  * (http://www.fsf.org/copyleft/gpl.html);
  *
  *  or 
  *
  * The Semiotek Public License (http://webmacro.org/LICENSE.)  
  *
  * This software is provided "as is", with NO WARRANTY, not even the 
  * implied warranties of fitness to purpose, or merchantability. You
  * assume all risks and liabilities associated with its use.
  *
  * See www.webmacro.org for more information on the WebMacro project.  
  */
 
 /*
  * This class is inspired by the class com.go.trouve.util.IdentityMap
  * from the "Tea" project.
  * For more information about Tea, please see http://opensource.go.com/.
  */
 
 package org.webmacro.util;
 
 import java.lang.ref.WeakReference;
 import java.lang.ref.ReferenceQueue;
 
 /**
  * Identity map, that is especially suited to canonical mappings.
  * This map is identical to SimpleMap, except two special featurs.
  * <br>
  * 1. Two keys are compared for equality only be reference. There
  * equals() and hashCode() method are not called. Instead a special
  * system has function is used, that is based upon the reference.
  * This leads to a superior performance to hashFunctions used for
  * strings for example.
  * <br>
  * 2. Keys are only held via a WeakReference. This means, that if
  * the key object is not referenced at another place, it will be
  * removed from this map. This makes sense, as you cannot access
  * the value mapped to this key anymore, since you do not have
  * a reference to the key anymore.
  * <br>
  * @author skanthak@muehlheim.de
  * @since 0.96
  **/
 public final class SimpleIdentityMap implements SimpleMap {
     private Node[] tab;
     private Object[] locks;
     private ReferenceQueue queue;
 
    /**
      * Create a new SimpleMap with 1001 LRU buckets
      */
     public SimpleIdentityMap() {
         this(1001);
     }
 
    /**
      * Create a new SimpleMap with 'size' LRU buckets
      */
     public SimpleIdentityMap(int size) {
         tab = new Node[size];
         locks = new Object[size];
         for (int i=0; i < size; i++) {
             locks[i] = new Object();
         }
         queue = new ReferenceQueue();
     }
     
     private void processQueue() {
         // reference queue is sychronized, so no need to do it ourself
         Node node;
         while ((node = (Node)queue.poll()) != null) {
            // search for node to remove it from map
             int hash = (node.hash & 0x7FFFFFFF) % tab.length;
             Node last=null;
             synchronized (locks[hash]) {
                 if (!node.cleaned) {
                    // search linked list for node
                     Node current = tab[hash];

                     while (current != null) {
                         if (current == node) {
                             if (last == null) {
                                 tab[hash] = current.next;
                             } else {
                                 last.next = current.next;
                             }
                             current.cleaned = true;
                             break;
                         }
                         last = current;
                         current = current.next;
                     }
                 }
             }
         }
     }
         
    /**
      * Add a key to the SimpleMap. 
      */
     public void put(Object key,Object value) {
         processQueue();
         if (key == null) {
             return;
         }
         if (value == null) {
             remove(key);
             return;
         }
 
         int hash = (System.identityHashCode(key) & 0x7FFFFFFF) % tab.length;
         boolean found = false;
         synchronized (locks[hash]) {
             Node node = tab[hash];
             while (node != null) {
                 if (node.get() == key) {
                     node.value = value;
                     return;
                 }
                 node = node.next;
             }
             node = new Node(key,queue);
             node.value = value;
             node.next = tab[hash];
             tab[hash] = node;
         }
     }
 
    /**
      * Get the value of 'key' back. Returns null if no such key.
      * Remember that you have to use the identical object (same
      * reference) as the key, that was used when you placed the
      * object into the map.
      */
     public Object get(Object key) {
         int hash = (System.identityHashCode(key) & 0x7FFFFFFF) % tab.length;
         Node last = null;
         Object nodeKey;
         synchronized (locks[hash]) {
             Node node = tab[hash];
             while (node != null) {
                 if (node.get() == key) {
                     if (last != null) {
                         last.next = node.next;
                         node.next = tab[hash];
                         tab[hash] = node;
                     }
                     return node.value;
                 }
                 last = node;
                 node = node.next;
             }
         }
         return null;
     }
 
    /**
      * Ensure that the key does not appear in the map
      * Remember that you have to use the identical object (same
      * reference) as the key, that was used when you placed the
      * object into the map.
      */
     public Object remove(Object key) {
         //processQueue();
         int hash = (System.identityHashCode(key) & 0x7FFFFFFF) % tab.length;
         Node last = null;
         synchronized (locks[hash]) {
             Node node = tab[hash];
             while (node != null) {
                 if (node.get() == key) {
                     // we found our key or we found
                     // a collected weak-ref. In either case,
                     // we can remove this entry
                     if (last != null) {
                         last.next = node.next;
                     } else {
                         tab[hash] = node.next;
                     }
                     node.clear();
                     node.cleaned = true;
                     return node.value;
                 }
                 last = node;
                 node = node.next;
             }
         }
         return null;
     }
 
 
     public void clear() {
         for (int i = 0; i < tab.length; i++) {
             synchronized(locks[i]) {
                 Node node = tab[i];
                 while (node != null) {
                     node.clear();
                     node.cleaned = true;
                     node = node.next;
                 }
             }
         }
     }
     
     static class Node extends WeakReference {
         Object value;
         Node next;
         boolean cleaned=false;
         final int hash;
         
         Node(Object key,ReferenceQueue queue) {
             super(key,queue);
             hash = System.identityHashCode(key);
         }
     }
 
 }
