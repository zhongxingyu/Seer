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
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author Meisam Fathi Salmi <fathi@cse.ohio-state.edu>
  * 
  */
 public class FunctionList {
 
     /**
      * 
      */
     private static final int INITIAL_CAPACITY = 1000;
 
     /**
      * 
      */
     private final Map functions;
 
     /**
      * 
      */
     public FunctionList() {
         this.functions = new HashMap(FunctionList.INITIAL_CAPACITY);
 
     }
 
     /**
      * @param functionName
      * @param formalParams
      * @param body
      */
     public void add(final LeafNode functionName, final LeafNode[] formalParams,
             final ParseTree body) {
         this.functions.put(functionName.getToken().getLexval(),
                 new FunctionDefinition(functionName, formalParams, body));
 
     }
 
     /**
      * @author Meisam Fathi Salmi <fathi@cse.ohio-state.edu>
      * 
      */
     class FunctionDefinition {
 
         /**
          * 
          */
         private final LeafNode functionName;
 
         /**
          * 
          */
         private final LeafNode[] formalParams;
 
         /**
          * 
          */
         private final ParseTree body;
 
         /**
          * @param functionName
          * @param formalParams
          * @param body
          */
         public FunctionDefinition(final LeafNode functionName,
                 final LeafNode[] formalParams, final ParseTree body) {
             this.functionName = functionName;
             this.formalParams = formalParams;
             this.body = body;
         }
 
         /**
          * @return the body
          */
         public ParseTree getBody() {
             return this.body;
         }
 
         /**
          * @return the formalParams
          */
         public LeafNode[] getFormalParams() {
             return this.formalParams;
         }
 
     }
 
     public FunctionDefinition lookup(final String functionName) {
         return (FunctionDefinition) this.functions.get(functionName);
 
     }
 
 }
