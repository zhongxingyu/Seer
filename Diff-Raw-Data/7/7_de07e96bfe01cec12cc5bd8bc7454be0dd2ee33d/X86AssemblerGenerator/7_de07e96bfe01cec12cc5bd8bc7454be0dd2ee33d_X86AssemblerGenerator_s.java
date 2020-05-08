 /*
  *  This file is part of the jasm project (http://code.google.com/p/jasm).
  *
  *  This file is licensed to you under the BSD License; You may not use
  *  this file except in compliance with the License. See the LICENSE.txt
  *  file distributed with this work for a copy of the License and information
  *  regarding copyright ownership.
  */
 package jasm.tools.cisc.x86;
 
 import jasm.EnumerableArgument;
 import jasm.LabelAddressInstruction;
 import jasm.LabelOffsetInstruction;
 import jasm.WordWidth;
 import jasm.amd64.AMD64GeneralRegister8;
 import jasm.tools.Assembly;
 import jasm.tools.Parameter;
 import jasm.tools.gen.as.AssemblerGenerator;
 import jasm.tools.util.IndentWriter;
 import jasm.util.ArrayUtil;
 import jasm.util.Enums;
 import jasm.util.HexUtil;
 import jasm.x86.X86InstructionPrefix;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public abstract class X86AssemblerGenerator<Template_Type extends X86Template<Template_Type>>
     extends AssemblerGenerator<Template_Type> {
   private static final String OPCODE1_VARIABLE_NAME = "opcode1";
   private static final String OPCODE2_VARIABLE_NAME = "opcode2";
   private static final String MODRM_GROUP_OPCODE_VARIABLE_NAME = "modRmOpcode";
 
   private final WordWidth _addressWidth;
 
   /**
    * The set of subroutines that are required.
    * Only valid after {@link #classifySubroutines()} completed.
    */
   private final LinkedList<Subroutine<Template_Type>> _requiredSubroutines = new LinkedList<Subroutine<Template_Type>>();
   /**
    * The map of templates to subroutines for subroutines that are required.
    * Only valid after {@link #classifySubroutines()} completed.
    */
   private final Map<Template_Type, Subroutine<Template_Type>> _templateToSubroutine = new HashMap<Template_Type, Subroutine<Template_Type>>();
   /** Map of generated code to subroutine. Only used during {@link #buildSubroutines()}. */
   private final Map<String, Subroutine<Template_Type>> _code2SubroutineMap = new HashMap<String, Subroutine<Template_Type>>();
   private boolean _buildingRequiredSubroutineMaps;
 
   protected X86AssemblerGenerator(Assembly<Template_Type> assembly,
                                   WordWidth addressWidth) {
     super(assembly, sort(assembly.templates()));
     _addressWidth = addressWidth;
   }
 
   @Override
   protected void generateRawAssemblerClass(final String name) throws IOException {
     buildSubroutines();
     classifySubroutines();
     super.generateRawAssemblerClass(name);
   }
 
   //Generate all the subroutines at the start.
   // If a subroutine is only used once then the code can be inlined.
   private void buildSubroutines() {
     _buildingRequiredSubroutineMaps = true;
     _code2SubroutineMap.clear();
     for (Template_Type template : assembly().templates()) {
       buildSubroutine(template, false);
     }
     _buildingRequiredSubroutineMaps = false;
   }
 
   private void classifySubroutines() {
     _templateToSubroutine.clear();
     int idSeq = 1;
     for (Subroutine<Template_Type> subroutine : _code2SubroutineMap.values()) {
       if (subroutine.required()) {
         subroutine.id = idSeq++;
         _requiredSubroutines.add(subroutine);
         for (Template_Type template : subroutine.templates) {
           _templateToSubroutine.put(template, subroutine);
         }
       }
     }
   }
 
   protected final Subroutine<Template_Type> buildSubroutine(final Template_Type template,
                                                             final boolean required) {
     final String code = generateSubroutineCode(template, true);
     Subroutine<Template_Type> subroutine = _code2SubroutineMap.get(code);
     if (subroutine == null) {
       subroutine = new Subroutine<Template_Type>();
       _code2SubroutineMap.put(code, subroutine);
     }
     subroutine.required |= required;
     subroutine.templates.add(template);
     return subroutine;
   }
 
   protected final X86Parameter getParameter(Template_Type template, Class parameterType) {
     for (X86Parameter parameter : template.parameters()) {
       if (parameter.type() == parameterType) {
         return parameter;
       }
     }
     throw new IllegalStateException("found no parameter of type: " + parameterType);
   }
 
   private void printCallWithByteDisplacement(final IndentWriter writer,
                                              final Template_Type template,
                                              final Class argumentType,
                                              final boolean inSubroutine) {
     final Template_Type variant =
         X86Assembly.getModVariantTemplate(assembly().templates(), template, argumentType);
     Subroutine<Template_Type> subroutine = _templateToSubroutine.get(variant);
     if (null == subroutine) {
       if (_buildingRequiredSubroutineMaps) {
         subroutine = buildSubroutine(variant, true);
       } else {
         throw new IllegalStateException("Missing subroutine for " + variant);
       }
     }
     writer.print(subroutine.name() + "( (byte)");
     if (template.opcode2() != null) {
       writer.print(opcode2Value(template, inSubroutine));
     } else {
       writer.print(opcodeValue(template, inSubroutine));
     }
     final ModRMGroup.Opcode opcode = template.modRMGroupOpcode();
     if (opcode != null) {
       writer.print(", (byte)" + HexUtil.toHexLiteral(opcode.byteValue()));
     }
     for (X86Parameter parameter : template.parameters()) {
       if (parameter.type() == argumentType) {
         writer.print(", (byte) 0");
       }
       final String value = passValue(parameter, inSubroutine);
       writer.print(", " + value);
     }
     writer.println(");");
   }
 
   protected final String asIdentifier(final EnumerableArgument argument,
                                       final boolean inSubroutine) {
     return argument.getClass().getSimpleName() + "." +
            argument.name() + (inSubroutine ? ".value()" : "");
   }
 
   @Override
   protected Set<String> getRawAssemblerImports(final List<Template_Type> templates) {
     final Set<String> imports = super.getRawAssemblerImports(templates);
     imports.add(X86InstructionPrefix.class.getPackage().getName());
     return imports;
   }
 
   protected final <Argument_Type extends EnumerableArgument> void printModVariant(final IndentWriter writer,
                                                                                   final Template_Type template,
                                                                                   final boolean inSubroutine,
                                                                                   final Argument_Type... arguments) {
     final Class argumentType = arguments[0].getClass();
     final X86Parameter parameter = getParameter(template, argumentType);
     writer.print("if (");
     String separator = "";
     for (EnumerableArgument argument : arguments) {
       writer.print(separator + parameter.variableName() + " == " + asIdentifier(argument, inSubroutine));
       separator = " || ";
     }
     writer.println(") {");
     writer.indent();
     printCallWithByteDisplacement(writer, template, argumentType, inSubroutine);
     writer.println("return;");
     writer.outdent();
     writer.println("}");
   }
 
   protected abstract void printModVariants(IndentWriter writer, Template_Type template, boolean inSubroutine);
 
   protected void printPrefixes(IndentWriter writer, Template_Type template, final boolean inSubroutine) {
     if (template.addressSizeAttribute() != _addressWidth) {
       writer.println("emitAddressSizePrefix();");
     }
     if (template.operandSizeAttribute() == WordWidth.BITS_16) {
       writer.println("emitOperandSizePrefix();");
     }
     final X86InstructionPrefix prefix = template.instructionSelectionPrefix();
     if (prefix != null) {
       if (prefix == X86InstructionPrefix.OPERAND_SIZE) {
         writer.println("emitOperandSizePrefix();");
       } else if (prefix == X86InstructionPrefix.ADDRESS_SIZE) {
         writer.println("emitAddressSizePrefix();");
       } else {
         writer.println("emitPrefix(X86InstructionPrefix." + prefix.name() + ");");
       }
     }
   }
 
   protected final String asValueInSubroutine(final X86Parameter parameter,
                                              final boolean inSubroutine) {
     if (inSubroutine && promoteParam(parameter)) {
       return parameter.variableName();
     } else {
       return parameter.valueString();
     }
   }
 
   protected final String passValue(final X86Parameter parameter,
                                    final boolean inSubroutine) {
     return (!inSubroutine && promoteParam(parameter)) ? parameter.valueString() : parameter.variableName();
   }
 
   private boolean promoteParam(final X86Parameter parameter) {
     return !(parameter instanceof X86EnumerableParameter &&
              parameter.type() == AMD64GeneralRegister8.class);
   }
 
   private void printModRMByte(final IndentWriter writer,
                               final Template_Type template,
                               final boolean inSubroutine) {
     String mod = String.valueOf(template.modCase().ordinal());
     String rm = "0";
     String reg = "0";
 
     final ModRMGroup.Opcode opcode = template.modRMGroupOpcode();
     if (opcode != null) {
       reg = inSubroutine ? MODRM_GROUP_OPCODE_VARIABLE_NAME : HexUtil.toHexLiteral(opcode.byteValue());
     }
     switch (template.rmCase()) {
       case SIB:
       case SWORD:
       case SDWORD: {
         rm = String.valueOf(template.rmCase().value());
         break;
       }
       default:
         break;
     }
     for (X86Parameter parameter : template.parameters()) {
       switch (parameter.place()) {
         case MOD_REG:
         case MOD_REG_REXR: {
           reg = asValueInSubroutine(parameter, inSubroutine);
           break;
         }
         case MOD_RM:
         case MOD_RM_REXB: {
           rm = asValueInSubroutine(parameter, inSubroutine);
           break;
         }
         default:
           break;
       }
     }
     writer.println("emitModRM(" + mod + "," + rm + "," + reg + ");");
   }
 
   private void printSibByte(final IndentWriter writer,
                             final Template_Type template,
                             final boolean inSubroutine) {
     String base = (template.sibBaseCase() == SibBaseCase.SPECIAL) ? "5" : "0";
     String index = "0";
     String scale = "0";
     for (X86Parameter parameter : template.parameters()) {
       switch (parameter.place()) {
         case SIB_BASE:
         case SIB_BASE_REXB:
           base += " | " + asValueInSubroutine(parameter, inSubroutine);
           break;
         case SIB_INDEX:
         case SIB_INDEX_REXX:
           index = asValueInSubroutine(parameter, inSubroutine);
           break;
         case SIB_SCALE:
           scale = asValueInSubroutine(parameter, inSubroutine);
           break;
         default:
           break;
       }
     }
     writer.println("emitSibByte(" + base + "," + index + "," + scale + ");");
   }
 
   protected final <Argument_Type extends Enum<Argument_Type> & EnumerableArgument>
   void printSibVariant(final IndentWriter writer,
                        final Template_Type template,
                        final boolean inSubroutine,
                        final Argument_Type... arguments) {
     final Class argumentType = arguments[0].getClass();
     final X86Parameter parameter = getParameter(template, argumentType);
     writer.print("if (");
     String separator = "";
     for (EnumerableArgument argument : arguments) {
       writer.print(separator + parameter.variableName() + " == " + asIdentifier(argument, inSubroutine));
       separator = " || ";
     }
     writer.println(") {");
     writer.indent();
     emitByte(writer, (byte) 0x24);
     writer.println(" // SIB byte");
     writer.outdent();
     writer.println("}");
   }
 
   protected abstract void printSibVariants(IndentWriter writer, Template_Type template, final boolean inSubroutine);
 
   private void printImmediateParameter(IndentWriter writer, X86NumericalParameter parameter) {
     switch (parameter.width().numberOfBytes()) {
       case 8:
         writer.println("emitLong(" + parameter.variableName() + ");");
         break;
       case 4:
         writer.println("emitInt(" + parameter.variableName() + ");");
         break;
       case 2:
         writer.println("emitShort(" + parameter.variableName() + ");");
         break;
       case 1:
         writer.println("emitByte(" + parameter.variableName() + ");");
         break;
       default:
         throw new IllegalStateException("Unexpected byte count: " + parameter.width().numberOfBytes());
     }
   }
 
   private void printAppendedEnumerableParameter(final IndentWriter writer,
                                                 final X86EnumerableParameter parameter,
                                                 final boolean inSubroutine) {
     writer.println("emitByte((byte) " + asValueInSubroutine(parameter, inSubroutine) + ");");
   }
 
   private void printAppendedParameter(final IndentWriter writer,
                                       final Template_Type template,
                                       final boolean inSubroutine) {
     for (X86Parameter parameter : template.parameters()) {
       if (parameter.place() == ParameterPlace.IMMEDIATE ||
           parameter.place() == ParameterPlace.DISPLACEMENT) {
         printImmediateParameter(writer, (X86NumericalParameter) parameter);
       } else if (parameter.place() == ParameterPlace.APPEND) {
         printAppendedEnumerableParameter(writer, (X86EnumerableParameter) parameter, inSubroutine);
       }
     }
   }
 
   private void printSubroutine(final IndentWriter writer,
                                final Template_Type template,
                                final boolean inSubroutine) {
     printModVariants(writer, template, inSubroutine);
     printPrefixes(writer, template, inSubroutine);
     if (template.opcode2() != null) {
       X86Parameter p = null;
       for (X86Parameter parameter : template.parameters()) {
         //Because we have several "faked" templates for instructions
         if (parameter.place() == ParameterPlace.OPCODE2 ||
             parameter.place() == ParameterPlace.OPCODE2_REXB) {
           p = parameter;
           break;
         }
       }
      writer.println("emitOpcode2(" + toOpCode(opcode2Value(template, inSubroutine), p, inSubroutine) + ");");
     } else {
       X86Parameter p = null;
       for (X86Parameter parameter : template.parameters()) {
         //Because we have several "faked" templates for instructions
         if (parameter.place() == ParameterPlace.OPCODE1 ||
             parameter.place() == ParameterPlace.OPCODE1_REXB) {
           p = parameter;
           break;
         }
       }
       writer.println("emitByte(" + toOpCode(opcodeValue(template, inSubroutine), p, inSubroutine) + ");");
     }
     if (template.hasModRMByte()) {
       printModRMByte(writer, template, inSubroutine);
       if (template.hasSibByte()) {
         printSibByte(writer, template, inSubroutine);
       } else {
         printSibVariants(writer, template, inSubroutine);
       }
     }
     printAppendedParameter(writer, template, inSubroutine);
   }
 
   private String opcodeValue(final Template_Type template,
                              final boolean inSubroutine) {
     return inSubroutine ? OPCODE1_VARIABLE_NAME : template.opcode1().toString();
   }
 
   private String opcode2Value(final Template_Type template,
                               final boolean inSubroutine) {
     return inSubroutine ? OPCODE2_VARIABLE_NAME : template.opcode2().toString();
   }
 
   private void printSubRoutineArgList(final IndentWriter writer, final Template_Type template) {
     writer.print("(byte ");
     if (template.opcode2() != null) {
       writer.print(OPCODE2_VARIABLE_NAME);
     } else {
       writer.print(OPCODE1_VARIABLE_NAME);
     }
     if (template.modRMGroupOpcode() != null) {
       writer.print(", byte " + MODRM_GROUP_OPCODE_VARIABLE_NAME);
     }
     writer.print(formatParamList(template.parameters()));
     writer.println(") {");
   }
 
   private String toOpCode(final String opCode,
                           final X86Parameter p,
                           final boolean inSubroutine) {
     if (null != p) {
       return "(byte)( " + opCode + " + (" + asValueInSubroutine(p, inSubroutine) + " & 7 ))";
     } else {
       return "(byte)( " + opCode + " )";
     }
   }
 
   private String formatParamList(List<? extends X86Parameter> parameters) {
     String sep = ", ";
     final StringBuilder sb = new StringBuilder();
     for (X86Parameter parameter : parameters) {
       sb.append(sep);
       if (parameter.type().isMemberClass()) {
         sb.append(parameter.type().getEnclosingClass().getSimpleName());
         sb.append(".");
       }
       if (promoteParam(parameter)) {
         if (parameter instanceof X86NumericalParameter) {
           final X86NumericalParameter np = (X86NumericalParameter) parameter;
           if (np.width() == WordWidth.BITS_64) sb.append("long");
           else if (np.width() == WordWidth.BITS_32) sb.append("int");
           else if (np.width() == WordWidth.BITS_16) sb.append("short");
           else sb.append("byte");
         } else {
           //Enumerated
           sb.append("int");
         }
       } else {
         sb.append(parameter.type().getSimpleName());
       }
       sb.append(" ");
       sb.append(parameter.variableName());
       if (!sep.startsWith(", ")) {
         sep = ", " + sep;
       }
     }
     return sb.toString();
   }
 
   private String generateSubroutineCode(final Template_Type template,
                                         final boolean inSubroutine) {
     final StringWriter stringWriter = new StringWriter();
     final IndentWriter writer = new IndentWriter(new PrintWriter(stringWriter));
     printSubroutine(writer, template, inSubroutine);
     return stringWriter.toString();
   }
 
   @Override
   protected final void printMethod(final IndentWriter writer,
                                    final Template_Type template) {
     final Subroutine<Template_Type> subroutine = _templateToSubroutine.get(template);
     if (null != subroutine) writer.println("@Inline");
     writer.print("public final void ");
     writer.print(template.assemblerMethodName() + "(");
     writer.print(formatParameterList("", template.parameters(), false));
     writer.println(") {");
     writer.indent();
     if (null != subroutine) {
       writer.print(subroutine.name() + "(");
       if (template.opcode2() != null) {
         writer.print("(byte) " + HexUtil.toHexLiteral(template.opcode2().byteValue()));
       } else {
         writer.print("(byte) " + HexUtil.toHexLiteral(template.opcode1().byteValue()));
       }
       if (template.modRMGroupOpcode() != null) {
         writer.print(", (byte) " + HexUtil.toHexLiteral(template.modRMGroupOpcode().byteValue()));
       }
       for (X86Parameter parameter : template.parameters()) {
         writer.print(", " + passValue(parameter, false));
       }
       writer.println(");");
     } else {
       printSubroutine(writer, template, false);
     }
     writer.outdent();
     writer.println("}");
   }
 
   @Override
   protected final void printSubroutines(IndentWriter writer) {
     for (Subroutine<Template_Type> subroutine : _requiredSubroutines) {
       writer.println();
       writer.print("private void " + subroutine.name());
       final Template_Type template = subroutine.template();
       printSubRoutineArgList(writer, template);
       writer.indent();
       printSubroutine(writer, template, true);
       writer.outdent();
       writer.println("}");
     }
   }
 
   private boolean parametersMatching(Template_Type candidate, Template_Type original) {
     if (candidate.parameters().size() != original.parameters().size()) {
       return false;
     }
     for (int i = 0; i < candidate.parameters().size(); i++) {
       if (i == original.labelParameterIndex()) {
         assert candidate.parameters().get(i).getClass() == X86OffsetParameter.class || candidate.parameters().get(i).getClass() == X86AddressParameter.class;
         assert candidate.parameters().get(i).getClass() == original.parameters().get(i).getClass();
       } else if (candidate.parameters().get(i).type() != original.parameters().get(i).type()) {
         return false;
       }
     }
     return true;
   }
 
   private int getLabelWidthSequenceIndex(List<LabelWidthCase<Template_Type>> labelWidthCases) {
     final EnumSet<WordWidth> enumSet = EnumSet.noneOf(WordWidth.class);
     for (LabelWidthCase labelWidthCase : labelWidthCases) {
       enumSet.add(labelWidthCase._width);
     }
     return Enums.powerSequenceIndex(enumSet);
   }
 
   private List<LabelWidthCase<Template_Type>> getRelatedLabelTemplatesByWidth(Template_Type template,
                                                                               List<Template_Type> labelTemplates) {
     final LabelWidthCase<Template_Type>[] array = ArrayUtil.create(LabelWidthCase.class, WordWidth.values().length);
     for (Template_Type t : labelTemplates) {
       if (t.assemblerMethodName().equals(template.assemblerMethodName()) &&
           t.labelParameterIndex() == template.labelParameterIndex() &&
           parametersMatching(t, template)) {
         final X86NumericalParameter numericalParameter =
             (X86NumericalParameter) t.parameters().get(template.labelParameterIndex());
         final WordWidth width = numericalParameter.width();
         array[width.ordinal()] = new LabelWidthCase<Template_Type>(width, t);
         t.setLabelMethodWritten(true);
       }
     }
     // Report the found cases in the order of ascending width:
     final ArrayList<LabelWidthCase<Template_Type>> result = new ArrayList<LabelWidthCase<Template_Type>>();
     for (final LabelWidthCase<Template_Type> labelWidthCase : array) {
       if (labelWidthCase != null) {
         assert result.isEmpty() || labelWidthCase._width.greaterThan(result.get(result.size() - 1)._width);
         result.add(labelWidthCase);
       }
     }
     assert result.size() > 0;
     return result;
   }
 
   private void printOffsetLabelMethod(IndentWriter writer, Template_Type template, List<Template_Type> labelTemplates) {
     Template_Type thisTemplate = template;
     final List<? extends Parameter> parameters = printLabelMethodHead(writer, thisTemplate);
     final List<LabelWidthCase<Template_Type>> labelWidthCases = getRelatedLabelTemplatesByWidth(thisTemplate, labelTemplates);
     thisTemplate = labelWidthCases.get(0)._template; // first use the template that will produce the least bytes
     writer.println("final int startOffset = currentOffset();");
     printInitialRawCall(writer, thisTemplate);
     writer.print("new " + LabelOffsetInstruction.class.getSimpleName());
     writer.println("(this, startOffset, currentOffset(), " + parameters.get(thisTemplate.labelParameterIndex()).variableName() + ", " + getLabelWidthSequenceIndex(labelWidthCases) + ") {");
     writer.indent();
     writer.println("@Override");
     writer.println("protected int templateSerial() { return " + template.serial() + "; }");
     writer.println("@Override");
     writer.println("protected void assemble() throws AssemblyException {");
     writer.indent();
     if (labelWidthCases.size() == 1) {
       printRawCall(writer, thisTemplate, parameters);
     } else {
       writer.println("switch (labelWidth()) {");
       writer.indent();
       for (LabelWidthCase<Template_Type> labelWidthCase : labelWidthCases) {
         writer.println("case " + labelWidthCase._width.name() + ": {");
         writer.indent();
         printRawCall(writer, labelWidthCase._template, parameters);
         writer.println("break;");
         writer.outdent();
         writer.println("}");
       }
       if (labelWidthCases.size() < WordWidth.values().length) {
         writer.println("default: {");
         writer.indent();
         writer.println("break;");
         writer.outdent();
         writer.println("}");
       }
       writer.outdent();
       writer.println("}");
     }
     writer.outdent();
     writer.println("}");
     writer.outdent();
     writer.println("};");
     writer.outdent();
     writer.println("}");
     writer.println();
   }
 
   private void printAddressLabelMethod(IndentWriter writer, Template_Type template) {
     final List<? extends Parameter> parameters = printLabelMethodHead(writer, template);
     template.setLabelMethodWritten(true);
     writer.println("final int startOffset = currentOffset();");
     printInitialRawCall(writer, template);
     writer.print("new " + LabelAddressInstruction.class.getSimpleName());
     writer.println("(this, startOffset, currentOffset(), " + parameters.get(template.labelParameterIndex()).variableName() + ") {");
     writer.indent();
     writer.println("@Override");
     writer.println("protected int templateSerial() { return " + template.serial() + "; }");
     writer.println("@Override");
     writer.println("protected void assemble() throws AssemblyException {");
     writer.indent();
     printRawCall(writer, template, parameters);
     writer.outdent();
     writer.println("}");
     writer.outdent();
     writer.println("};");
     writer.outdent();
     writer.println("}");
     writer.println();
   }
 
   @Override
   protected final void printLabelMethod(IndentWriter writer, Template_Type labelTemplate, List<Template_Type> labelTemplates) {
     if (labelTemplate.addressSizeAttribute() == _addressWidth) {
       if (!labelTemplate.isLabelMethodWritten()) {
         final X86Parameter parameter = labelTemplate.parameters().get(labelTemplate.labelParameterIndex());
         if (parameter instanceof X86OffsetParameter) {
           printOffsetLabelMethod(writer, labelTemplate, labelTemplates);
         } else {
           printAddressLabelMethod(writer, labelTemplate);
         }
       }
     }
   }
 
   //<Template_Type extends X86Template>
   private static <Sort_Type extends X86Template<Sort_Type> & Comparable<Sort_Type>> List<Sort_Type> sort(final List<Sort_Type> templates) {
     final ArrayList<Sort_Type> results = new ArrayList<Sort_Type>(templates.size());
     results.addAll(templates);
     Arrays.sort(templates.toArray());
     Collections.sort(results);
     return results;
   }
 
   protected final Template_Type lookupVariant(final Template_Type template, final Class type) {
     return X86Assembly.getModVariantTemplate(assembly().templates(), template, type);
   }
 }
