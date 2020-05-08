 /**
  * Lisp Subinterpreter, an interpreter for a sublanguage of Lisp
  * Copyright (C) 2011  Meisam Fathi Salmi <fathi@cse.ohio-state.edu>
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package edu.osu.cse.meisam.interpreter;
 
 /**
  * A class that provides input to the program via a string. This class is
  * supposed to be used for testing mostly.
  * 
  * @author Meisam Fathi Salmi <fathi@cse.ohio-state.edu>
  * 
  */
 public class StringInputProvider implements InputProvider {
 
     /**
      * The input 
      */
     private final String input;
 
     /**
      * Keeps track of the current location in the string.
      */
     private int currentLocation;
 
     /**
      * Constructs a new StringInputProvider with the given string.
      * @param input
      */
     public StringInputProvider(final String input) {
         if (input==null){
             throw new NullPointerException("Input provided for lexing is null.");
         }
         this.input = input;
         currentLocation = 0;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see edu.osu.cse.meisam.interpreter.InputProvider#hasMore()
      */
     public boolean hasMore() {
         if (currentLocation < input.length())
             return true;
         return false;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see edu.osu.cse.meisam.interpreter.InputProvider#nextChar()
      */
     public char nextChar() {
        char charAt = input.charAt(currentLocation);
         currentLocation++;        
        return charAt;
     }
 
 }
