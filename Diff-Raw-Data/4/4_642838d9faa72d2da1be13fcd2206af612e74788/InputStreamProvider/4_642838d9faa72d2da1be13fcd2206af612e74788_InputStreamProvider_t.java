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
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PushbackReader;
 
 /**
  * @author Meisam Fathi Salmi <fathi@cse.ohio-state.edu>
  * 
  */
 public class InputStreamProvider implements InputProvider {
 
     private final PushbackReader in;
 
     public InputStreamProvider(final InputStream in) {
         this.in = new PushbackReader(new InputStreamReader(in));
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see edu.osu.cse.meisam.interpreter.InputProvider#hasMore()
      */
     public boolean hasMore() {
         try {
            final int read = this.in.read();
            this.in.unread(read);
            return read != -1;
         } catch (final IOException e) {
             throw new LexerExeption("Cannot read from the input");
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see edu.osu.cse.meisam.interpreter.InputProvider#nextChar()
      */
     public char nextChar() {
         try {
             final int nextChar = this.in.read();
             if (nextChar == -1) {
                 throw new LexerExeption("Trying to read from the end of input");
             }
             return (char) nextChar;
         } catch (final IOException e) {
             throw new LexerExeption("Cannot read from the input");
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see edu.osu.cse.meisam.interpreter.InputProvider#lookaheadChar()
      */
     public char lookaheadChar() {
         try {
             final int nextChar = this.in.read();
             if (nextChar == -1) {
                 throw new LexerExeption("Trying to read from the end of input");
             }
             this.in.unread(nextChar); // push it back
             return (char) nextChar;
         } catch (final IOException e) {
             throw new LexerExeption("Cannot read from the input");
         }
     }
 
 }
