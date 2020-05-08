 /*
  * Copyright (C) 1998-2000 Semiotek Inc.  All Rights Reserved.
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
 
 
 package org.webmacro.util;
 
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 
 /**
  * Allow a Java 1.1 enumeration to be used as a JDK 1.2 style Iterator
  */
 final public class EnumIterator implements Iterator
 {
 
    final Enumeration enum;
     private boolean hasNext;
 
     /**
      * Construct an iterator given an enumeration
      */
     public EnumIterator (Enumeration e)
     {
        enum = e;
         hasNext = e.hasMoreElements();
     }
 
     /**
      * Return true if we have not yet reached the end of the enumeration
      */
     final public boolean hasNext ()
     {
         return hasNext;
     }
 
     /**
      * Advance the iterator and return the next value. Return null if we
      * reach the end of the enumeration.
      */
     final public Object next () throws NoSuchElementException
     {
         if (!hasNext)
         {
             throw new NoSuchElementException("advanced past end of list");
         }
        Object o = enum.nextElement();
        hasNext = enum.hasMoreElements();
         return o;
     }
 
     /**
      * Unsupported
      */
     final public void remove () throws UnsupportedOperationException
     {
         throw new UnsupportedOperationException();
     }
 
     /**
      * Test harness
      */
     static public void main (String arg[])
     {
         java.util.Vector v = new java.util.Vector(arg.length);
         for (int i = 0; i < arg.length; i++)
         {
             v.addElement(arg[i]);
         }
 
         try
         {
             Iterator i = new EnumIterator(v.elements());
             while (i.hasNext())
             {
                 System.out.println("item: " + i.next());
             }
         }
         catch (Exception e)
         {
             e.printStackTrace();
         }
     }
 }
 
