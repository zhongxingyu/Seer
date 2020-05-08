 /*
  *  This file is part of the jasm project (http://code.google.com/p/jasm).
  *
  *  This file is licensed to you under the BSD License; You may not use
  *  this file except in compliance with the License. See the LICENSE.txt
  *  file distributed with this work for a copy of the License and information
  *  regarding copyright ownership.
  */
 package jasm.tools.cisc.x86;
 
 import jasm.tools.InstructionDescription;
 import java.util.Arrays;
 import java.util.List;
 
 public final class ModRMDescription
     extends InstructionDescription<ModRMDescription> {
 
   private final ModRMOpcode _opcode;
   private final String _name;
 
   public static ModRMDescription modRM(ModRMOpcode opcode, String name, Object... specifications) {
     return new ModRMDescription(opcode, name, Arrays.asList(specifications));
   }
 
   public static ModRMDescription getDescriptionFrom(final ModRMOpcode opcode, final ModRMDescription[] descriptions) {
     for (ModRMDescription instructionDescription : descriptions) {
       if (instructionDescription.opcode() == opcode) {
         return instructionDescription;
       }
     }
     return null;
   }
 
   private ModRMDescription(ModRMOpcode opcode, String name, List<Object> specifications) {
    super(null, specifications);
     _opcode = opcode;
     _name = name;
   }
 
   public ModRMOpcode opcode() {
     return _opcode;
   }
 
   public String name() {
     return _name;
   }
 }
