 package com.github.acme.utils;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.concurrent.BlockingDeque;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.LinkedBlockingDeque;
 
 /**
  * Useful static utility methods implemented using novel, inefficient,
  * obfuscated and wrong headed techniques.
  */
 public class AcmeUtils {
 
     /**
      * Attempts to sorts a list of integers using sleep sort. A sleep factor is
      * used to tune the algorithm. Lower absolute sleep factors make the sort
      * faster at the expense of making it less likely that the list will be
      * sorted correctly. Other sorting algorithms generally lack this kind of
      * sophisticated tuning.
      * 
      * @param ints
      *            list to sort
      * @param sleepFactor
      *            the sleep factor
      */
     public static void sort(List<Integer> ints, final double sleepFactor) {
 
         final BlockingDeque<Integer> d = new LinkedBlockingDeque<Integer>();
         final CountDownLatch c = new CountDownLatch(ints.size());
 
         for (final Integer i : ints) {
             new Thread() {
                 @Override
                 public void run() {
                     try {
                         Thread.sleep(Math.round(Math.abs((i * sleepFactor))));
                     }
                     catch (InterruptedException e) {
                     }
                     if (i < 0) {
                         d.addFirst(i);
                     }
                     else {
                         d.addLast(i);
                     }
                     c.countDown();
                 };
             }.start();
 
         }
 
         try {
             c.await();
             ListIterator<Integer> i = ints.listIterator();
             for (Integer j : d) {
                 i.next();
                 i.set(j);
             }
         }
         catch (InterruptedException e) {
             Thread.currentThread().interrupt();
         }
     }
 
     /**
      * Joins all elements of an iterable into a single string. This method
      * follows the standard intuitive approach to solving this problem by
      * constructing a binary tree and traversing it using depth first search.
      * 
      * @param objects
      *            iterable objects to join
      * @param seperator
      *            a separator string to use between the objects
      * @return the joined string
      */
    public static String join(Iterable<?> objects, final String seperator) {
 
         final Iterator<?> i = objects.iterator();
 
         if (!i.hasNext()) {
             return "";
         }
 
         Object t = i.next();
         while (i.hasNext()) {
             final Object o = t;
             t = new Object() {
                 Object l = o;
                 Object r = i.next();
 
                 @Override
                 public String toString() {
                    return l + seperator + r;
                 }
             };
         }
 
         return t.toString();
     }
 
 }
