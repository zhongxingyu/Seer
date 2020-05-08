 /**********************************************************************/
 /* Copyright 2013 KRV                                                 */
 /*                                                                    */
 /* Licensed under the Apache License, Version 2.0 (the "License");    */
 /* you may not use this file except in compliance with the License.   */
 /* You may obtain a copy of the License at                            */
 /*                                                                    */
 /*  http://www.apache.org/licenses/LICENSE-2.0                        */
 /*                                                                    */
 /* Unless required by applicable law or agreed to in writing,         */
 /* software distributed under the License is distributed on an        */
 /* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,       */
 /* either express or implied.                                         */
 /* See the License for the specific language governing permissions    */
 /* and limitations under the License.                                 */
 /**********************************************************************/
 package robot;
 
 // Libraries
 import exception.*;
 import stackable.*;
 import parameters.*;
 import arena.Terrain;
 import stackable.item.*;
 
 /**
  * <b>Assembly functions - class Check</b><br>
  * Provides the funcions for checking
  * terrains and neighborhoods content.
  * 
  * @author Karina Awoki
  * @author Renato Cordeiro Ferreira
  * @author Vinícius Silva
  * @see arena.Terrain
  * @see stackable.Around
  */
 final public class Check
 {
     // No instances of this class allowed
     private Check() {} 
     
     /**
      * Assembly funcion ITEM. <br>
      * Takes out the top of the main stack,
      * checks if it's a terrain. If it has
      * an item, pushes it. In the other case,
      * pushes 0 in the top of the stack.
      *
      * @param rvm Virtual Machine
      */
     static void ITEM(RVM rvm)
         throws StackUnderflowException,
                WrongTypeException
     {
         Stackable stk;
         
         try { stk = rvm.DATA.pop(); }
         catch (Exception e) {
             throw new StackUnderflowException();
         }
         
         if(!(stk instanceof Terrain))
             throw new WrongTypeException("Terrain");
         
         // Get terrain's item
         Terrain t = (Terrain) stk;
         stackable.item.Item item = t.getItem();
         
         // Debug
         String itm = (item != null) ? item.toString() : "NONE";
         Debugger.say("    [ITEM] ", itm); 
         
         // Return Num(0) if no item is avaiable.
         if(item == null) rvm.DATA.push(new Num(0));
         else             rvm.DATA.push(item);
     }
     
     /**
      * Assembly funcion SEEK. <br>
      * Process an 'Around' stackable for 
      * serching if there is an item avaiable.
      * The item to be searched and the around
      * are taken from the top of the stack.
      * (First the item, secondly, around). 
      * The answer is put in the top of the 
      * main stack.
      * 
      * @param rvm Virtual Machine
      */
     static void SEEK(RVM rvm)
      throws StackUnderflowException,
               WrongTypeException,
               InvalidOperationException
     {
         Stackable ar;
         Stackable stk;
         int cont = 0;
         
         // Takes the argument and the neighborhood
         // from the top of the stack.
         try { stk = rvm.DATA.pop(); }
         catch (Exception e) {
             throw new StackUnderflowException();
         }
         try { ar = rvm.DATA.pop(); }
         catch (Exception e) {
             throw new StackUnderflowException();
         }
      
         // Checks if ar is of type around.
         if(!(ar instanceof Around))
             throw new WrongTypeException("Around");
         
         Around a = (Around) ar;  
         String s;
         int index;
         
         // Checks if stk is a text or an item.
         if(stk instanceof Text)
         {
             s = ((Text)stk).getText();
             index = 0;
         }
         else if(stk instanceof stackable.item.Item)
         {
             s = stk.getClass().getName();
             index = 1;
         }
         else throw new WrongTypeException("Text or Item");
         
         // Put in the Stack of the RVM the 
         // direction and the confirmation
         // of the finded object
         for(int i = a.matrix[0].length - 1; i >= 0; i--)
         {
             if(a.matrix[index][i] != null 
             && s.equals(a.matrix[index][i]) )
             {
                 if(i < 7)
                 {
                     rvm.DATA.push(new Direction(0, i));
                     rvm.DATA.push(new Num(1));    
                 }
                 else
                 {
                     rvm.DATA.push(new Direction(1, i));
                     rvm.DATA.push(new Direction(0, i));
                     rvm.DATA.push(new Num(2));
                 }
                 cont++;
             }
         }
         
         rvm.DATA.push(new Num(cont));
         
         // Debug
         String arnd  = (a != null)   
             ? "Pop the around correctly: " 
             : "NONE";
         String stack = (a != null) 
             ? stk.toString() 
             : "NONE";
         Debugger.say("    [SEEK] ", arnd, stack);
     }
 }
