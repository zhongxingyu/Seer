 /*
  Copyright (c) 2011, The Staccato-Commons Team
 
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation; version 3 of the License.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.
  */
 package net.sf.staccatocommons.collections.stream;
 
 import java.io.IOException;
 import java.io.Writer;
 
 /**
  * Stream interface for printing stream elements to a
  * {@link java.lang.Appendable}
  * 
  * @author flbulgarelli
  */
 public interface Printable<A> {
 
   /**
    * Prints the stream elements to an appendable, like {@link StringBuilder} or
    * a {@link Writer}
    * 
    * @param destination
    *          the appendable were print stream elements
    * @throws IOException
    *           if any io error occurs
    */
   void print(java.lang.Appendable destination) throws IOException;
 
   /**
    * Prints the stream elements to {@link System#out}
    * 
    * @throws IOException
    *           if any io error occurs
    */
  void print();
 
   /**
    * Prints the stream elements to an appendable, like {@link StringBuilder} or
    * a {@link Writer}, followed by a newline character
    * 
    * @param destination
    *          the appendable were print stream elements
    * @throws IOException
    *           if any io error occurs
    */
   void println(java.lang.Appendable o) throws IOException;
 
   /**
    * Prints the stream elements to {@link System#out}, followed by a newline
    * character
    * 
    * @throws IOException
    *           if any io error occurs
    */
  void println();
 
   /**
    * Prints the stream elements to a string
    * 
    * @return a string with the stream elements
    */
   String printString();
 
 }
