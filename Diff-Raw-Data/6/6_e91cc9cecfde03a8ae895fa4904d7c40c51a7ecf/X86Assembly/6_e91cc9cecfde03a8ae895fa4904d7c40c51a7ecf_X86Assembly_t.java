 /*
  *  This file is part of the jasm project (http://code.google.com/p/jasm).
  *
  *  This file is licensed to you under the BSD License; You may not use
  *  this file except in compliance with the License. See the LICENSE.txt
  *  file distributed with this work for a copy of the License and information
  *  regarding copyright ownership.
  */
 package jasm.tools.cisc.x86;
 
 import jasm.InstructionSet;
 import jasm.tools.Assembly;
 import java.util.List;
 
 public abstract class X86Assembly<Template_Type extends X86Template<Template_Type>>
     extends Assembly<Template_Type> {
 
   public X86Assembly(InstructionSet instructionSet, Class<Template_Type> templateType) {
     super(instructionSet, templateType);
   }
 
   private static <Template_Type extends X86Template<Template_Type>> boolean paramsMatch(final Template_Type original,
                                                                                         final Template_Type candidate,
                                                                                         Class argumentType) {
     int i = 0;
     int j = 0;
     final List<? extends X86Parameter> oParams = original.parameters();
     final List<? extends X86Parameter> cParams = candidate.parameters();
     final int oParamCount = oParams.size();
     final int cParamCount = cParams.size();
     while (i < oParamCount && j < cParamCount) {
       final Class originalType = oParams.get(i).type();
       Class candidateType = cParams.get(j).type();
       if (originalType == argumentType) {
         if (candidateType != byte.class) {
           return false;
         }
         j++;
         candidateType = cParams.get(j).type();
       }
       if (originalType != candidateType) {
         return false;
       }
       i++;
       j++;
     }
     return true;
   }
 
   public static <Template_Type extends X86Template<Template_Type>> Template_Type getModVariantTemplate(Iterable<Template_Type> templates,
                                                                                                        Template_Type original,
                                                                                                        Class argumentType) {
     for (Template_Type candidate : templates) {
      if (candidate.description().opcode1() == original.description().opcode1() &&
          candidate.description().opcode2() == original.description().opcode2() &&
           candidate.description().operandPrefix() == original.description().operandPrefix() &&
          candidate.description().mandatoryGroup1Prefix() == original.description().mandatoryGroup1Prefix() &&
           candidate.modRMGroupOpcode() == original.modRMGroupOpcode() &&
           candidate.addressSizeAttribute() == original.addressSizeAttribute() &&
           candidate.operandSizeAttribute() == original.operandSizeAttribute() &&
           paramsMatch(original, candidate, argumentType)) {
         return candidate;
       }
     }
     throw new IllegalStateException("could not find mod variant for: " + original);
   }
 }
