 package controller;
 /*Copyright 2004-2005 Univ.Prof. Dipl.-Ing. Dr.techn. Wolfgang SLANY, 
 Andreas Augustin, Sandra Durasiewicz, Bojan Hrnkas, Markus Köberl, 
 Bernhard Kornberger, Susanne Schöberl
 
 This file is part of Neptune-Robot-Simulation.
 
 Neptune-Robot-Simulation is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
 
 Neptune-Robot-Simulation is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with Neptune-Robot-Simulation; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  US
 */
 import java.util.*;
 /**
  * class represting a List of instructions which are excecuted by the 
  * turing machine. 
  */
 
public class InstructionList extends ArrayList
 {
 //-----------------------------------------------------------------------------
 /**
  * method adds an instruction at the end of an instruction list.
  * @param instruction instruction to insert
  */  
   public void addInstruction(Instruction instruction)
   {
     super.add(instruction);
   }
 
 //-----------------------------------------------------------------------------  
 /**
  * method adds an instruction at the specified position to the instruction list.
  * @param index zero based position of the element to add.
  * @param instruction instruction to insert
  */    
   public void addInstructionAtPos(int index, Instruction instruction)
   {
     super.add(index, instruction);
   }
 
 //-----------------------------------------------------------------------------  
 /**
  * method gets an instruction at the specified position in the instruction list.
  * @param index zero based position of the requested instruction.
  * @return instruction
  */      
   public Instruction getInstructionAtPos(int index)
   {
     return (Instruction)super.get(index);
   }
 
 //-----------------------------------------------------------------------------  
 /**
  * method removes the instruction at the specified position.
  * @param index zero based position of the element to remove. 
  */    
   public void removeInstructionAtPos(int index)
   {
     super.remove(index);
   }
 //-----------------------------------------------------------------------------  
 /**
  * method removes all instruction.
  */    
   public void removeAllInstructions()
   {
     super.clear(); 
   }
   
  //-----------------------------------------------------------------------------  
 /**
  * method replaces the instruction at the specified position in this list with the specified instruction.
  * @param index
  * @param instruction
  */  
   public void setInstruction(int index, Instruction instruction)
   {
 	  super.set(index, instruction);
   }
}
