 package uk.me.m0rjc.picstategenerator.picAsmBuilder;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import uk.me.m0rjc.picstategenerator.model.Node;
 import uk.me.m0rjc.picstategenerator.model.RomLocation;
 import uk.me.m0rjc.picstategenerator.model.Transition;
 import uk.me.m0rjc.picstategenerator.model.Variable;
 import uk.me.m0rjc.picstategenerator.model.Variable.Ownership;
 import uk.me.m0rjc.picstategenerator.visitor.IModel;
 import uk.me.m0rjc.picstategenerator.visitor.IModelVisitor;
 import uk.me.m0rjc.picstategenerator.visitor.INode;
 
 /**
  * Build assembly source code for the PIC18. Outputs .asm, .inc and .h for use
  * with C code.
  * 
  * @author Richard Corfield &lt;m0rjc@m0rjc.me.uk&gt;
  */
 public class Pic18AsmBuilder implements IModelVisitor
 {
     /** Maximum ASCII value to represent as a character in assembler. */
     private static final int MAX_PRINTABLE_ASCII = 126;
     /** Minium ASCII value to represent as a character in assembler. */
     private static final int MIN_PRINTABLE_ASCII = 32;
 
     /** Logging. */
     private final Logger m_log = Logger.getLogger(Pic18AsmBuilder.class.getName());
 
     /** Writer for the assembly .asm file. */
     private PicAssemblyWriter m_assembler;
 
     /** Writer for the assembly .inc file. */
     private PicAssemblyWriter m_asmHeader;
 
     /** Writer for the C header file. */
     private AnsiCWriter m_cHeader;
 
     /** Does this model use banked variables? */
     private boolean m_hasBankedVariables;
 
     /** Does this model requires the subroutine stack? */
     private boolean m_requiresSubroutineStack;
 
     /** Have we yet output our own GLOBALs? */
     private boolean m_hasOutputEntryPointGlobal;
 
     /** Variable that holds the state pointer. */
     private Variable m_statePointer;
 
     /** Variable that holds the subroutine stack. (Currently only 1 level deep) */
     private Variable m_subroutineStack;
 
     /** Name of the model's initial state. */
     private INode m_rootState;
 
     /**
      * Number of RAM banks found in the model. If 1 or less then we don't need
      * to be so paranoid about BANKSEL
      */
     private int m_numberOfBanksUsed = 0;
 
     /** Include files. */
     private List<String> m_includes = new ArrayList<String>();
 
     /** Processor for the list directive. */
     private String m_processor;
 
     /** Code to execute to exit from the step method. */
     private List<String> m_returnCode = new ArrayList<String>();
 
     /** True to generate a large ROM model. */
     private boolean m_largeRomModel;
 
     /** Bank currently BANKSEL if known. */
     private int m_currentBank = -1;
 
     /** Counter for creating transition label names. */
     private int m_internalLabelCounter = 0;
 
     /** Model name, read from the model itself. */
     private String m_modelName;
 
     /** Base name for output files. Defaults to the model name. */
     private String m_fileBaseName;
 
     /**
      * The current node has subroutine calls. This means that we cannot
      * guarantee that the state pointer points to this node, so SELF transitions
      * must be encoded explicitly.
      */
     private boolean m_nodeHasSubroutineCalls;
 
     /**
      * Labels in the code block stack. Used with {@link #push()}, {@link #pop()}
      * , {@link #exitCodeBlock(int)} and related.
      */
     private Stack<String> m_codeBlockStack = new Stack<String>();
 
     /**
      * Name of the current node. Used to optimise out code that would switch
      * from current node to current node with no effect.
      */
     private String m_currentNodeName;
 
     /**
      * Processor name for the LIST directive. If null then no LIST directive
      * will be output.
      * 
      * @param processor processor name, for example "18F14K50"
      */
     public void setProcessor(final String processor)
     {
         m_processor = processor;
     }
 
     /** @param largeRomModel if true then 3 byte pointers will be used. */
     public void setLargeRomModel(final boolean largeRomModel)
     {
         m_largeRomModel = largeRomModel;
     }
 
     /**
      * Base name for output files. Defaults to the model name. For example if
      * the base name is "gps" then output files would be "gps.asm", "gps.inc",
      * "gps.h"
      * 
      * @param fileBaseName file base name, for example "gps" or "/home/richard/output/gps"
      */
     public void setFileBaseName(final String fileBaseName)
     {
         m_fileBaseName = fileBaseName;
     }
 
     /**
      * Add the name of a file to #include in the assembler module.
      * 
      * @param includeName name of an include file, for example "p18f14k50.inc"
      */
     public void addInclude(final String includeName)
     {
         m_includes.add(includeName);
     }
 
     /**
      * Add a line to the code that is needed to return control from the step function.
      * 
      * @param line line of code to return, for example "RETURN" or "RETFIE"
      */
     public void addReturnLine(final String line)
     {
         m_returnCode.add(line);
     }
 
     @Override
     public void visitStartModel(final IModel model)
     {
         m_modelName = model.getModelName();
         m_rootState = model.getInitialState();
         m_requiresSubroutineStack = model.requiresSubroutineStack();
 
         if (m_fileBaseName == null)
         {
             m_fileBaseName = m_modelName;
         }
 
         m_assembler = new PicAssemblyWriter(m_fileBaseName + ".asm");
         m_asmHeader = new PicAssemblyWriter(m_fileBaseName + ".inc");
         m_cHeader = new AnsiCWriter(m_fileBaseName + ".h");
 
         if (m_processor != null)
         {
             m_assembler.opCode("list", "p=" + m_processor);
         }
 
         for (String include : m_includes)
         {
             m_assembler.write("#include \"" + include + "\"\n");
         }
 
         m_assembler.blankLine();
 
         String assemblerGuardSymbol = "_" + m_modelName.toUpperCase() + "_H_";
         m_cHeader.write("#ifndef " + assemblerGuardSymbol + "\n");
         m_cHeader.write("#define " + assemblerGuardSymbol + "\n");
         m_asmHeader.write("#ifndef " + assemblerGuardSymbol + "\n");
         m_asmHeader.write("#define " + assemblerGuardSymbol + "\n");
 
     }
 
     @Override
     public void visitDeclareExternalSymbol(final Variable name)
     {
         m_assembler.opCode("EXTERN", name.getName());
     }
 
     @Override
     public void visitDeclareExternalSymbol(final RomLocation name)
     {
         m_assembler.opCode("EXTERN", name.getName());
     }
 
     @Override
     public void visitDeclareGlobalSymbol(final String name)
     {
         if (!m_hasOutputEntryPointGlobal)
         {
             outputEntryPointGlobals();
         }
         m_assembler.opCode("GLOBAL", name);
         m_asmHeader.opCode("EXTERN", name);
     }
 
     /** Output globals for the module entry points. */
     private void outputEntryPointGlobals()
     {
         m_hasOutputEntryPointGlobal = true;
         String initMethodName = getInitMethodName();
         m_assembler.opCode("GLOBAL", initMethodName);
         m_asmHeader.blockComment("Initialisation method.",
                 "Call before first use.");
         m_cHeader.blockComment("Initialisation method. Call before first use.");
         m_asmHeader.opCode("EXTERN", initMethodName);
         m_cHeader.write("extern void " + initMethodName + "(void);\n");
 
         String stepMethodName = getStepMethodName();
         m_assembler.opCode("GLOBAL", stepMethodName);
         m_asmHeader.blockComment("Step method.",
                 "Call to advance state machine after setting input.");
         m_cHeader.blockComment("Step method.",
                 "Call to advance state machine after setting input.");
         m_asmHeader.opCode("EXTERN", stepMethodName);
         m_cHeader.write("extern void " + stepMethodName + "(void);\n");
     }
 
     @Override
     public void visitStartAccessVariables(final boolean modelDefinesAccessVariables)
     {
         if (!m_hasOutputEntryPointGlobal)
         {
             outputEntryPointGlobals();
         }
 
         if (modelDefinesAccessVariables)
         {
             m_assembler.blankLine();
             m_assembler.writeSection(m_modelName + "Acs", "UDATA_ACS");
             
             m_asmHeader.blankLine();
             m_asmHeader.blockComment("Access Variables");
             
             m_cHeader.blankLine();
             m_cHeader.blockComment("Access Variables");
             
             buildInternalPointers(Variable.ACCESS_BANK);
         }
     }
 
     /**
      * @return the public name of the initialise method in the resultant assembler module.
      */
     private String getInitMethodName()
     {
         return m_modelName + "Init";
     }
 
     /**
      * @return the public name of the step method in the resultant assembler module.
      */
     private String getStepMethodName()
     {
         return m_modelName + "Step";
     }
 
     /**
      * Name for the shared entry code for the given node.
      * 
      * @param nodeName node to write a label or GOTO command for
      * @return the name of the label that will be used for the shared entry code for that node.
      */
     private String getNodeEntryLabel(final String nodeName)
     {
         return "enter_" + nodeName;
     }
 
     /**
      * The name to use for the label for the step handling code for a node.
      * 
      * @param nodeName
      *            the name of the node.
      * @return the name of the step label
      */
     private String getNodeStepLabel(final String nodeName)
     {
         return "step_" + nodeName;
     }
 
     @Override
     public void visitCreateVariableDefinition(final Variable v)
     {
         m_assembler.ramResourceAllocation(v.getName(), v.getSize());
         if(v.isMustExport())
         {
             if(v.hasFlags())
             {
                 createHeadersForFlagVariable(v);
             }
             else
             {
                 createHeadersForPlainVariable(v);
             }
         }
     }
 
     /**
      * Output header lines for a flag variable.
      * @param v variable to output header lines for.
      */
     private void createHeadersForFlagVariable(final Variable v)
     {
         String[] flagNames = v.getFlagNames();
 
         // ASM
         m_asmHeader.opCode("EXTERN", v.getName());
         int bit = 0;
         for(String flag : flagNames)
         {
             int bitInByte = bit % 8;
             m_asmHeader.writePreprocessor("#define " + flag + " (" + bitInByte + ")");
            bitInByte++;
         }
         
         // C
         writePragmaVarLocate(v);
         m_cHeader.writeln("extern struct");
         m_cHeader.writeln("{");
         m_cHeader.indent();
         for(String flag : flagNames)
         {
             m_cHeader.writeln("unsigned " + flag + " :1;");
         }
         m_cHeader.unindent();
         m_cHeader.writeln("} " + v.getName() + ";");
     }
 
     /**
      * Output header lines for a plain variable.
      * @param v the variable to output.
      */
     private void createHeadersForPlainVariable(final Variable v)
     {
         // ASM
         m_asmHeader.opCode("EXTERN", v.getName());
         
         // C
         writePragmaVarLocate(v);
         StringBuilder sb = new StringBuilder();
        sb.append("extern char");
         if(v.getSize() > 1)
         {
             sb.append("[]");
         }
        sb.append(' ');
        sb.append(v.getName());
         sb.append(';');
         m_cHeader.writeln(sb.toString());
     }
 
     /** Output the #pragma varlocate line for the given variable in the C header file.
      * No line will be written if the variable is in the access bank.
      * @param v variable to write the line for.
      */
     private void writePragmaVarLocate(final Variable v)
     {
         if(!v.isAccess())
         {
             m_cHeader.writePreprocessor("#pragma varlocate \"" + getRamSectionName(v.getBank()) + "\" " + v.getName());
         }
     }
 
     @Override
     public void visitStartBankedVariables(final int bankNumber,
                                           final boolean modelDefinesVariablesInThisBank)
     {
         if (modelDefinesVariablesInThisBank)
         {
             m_numberOfBanksUsed++;
             String ramSectionName = getRamSectionName(bankNumber);
 
             m_assembler.blankLine();
             m_assembler.writeComment("Please set up the linker to locate this block as required.");
             m_assembler.writeSection(ramSectionName, "UDATA");
             
             m_asmHeader.blankLine();
             m_asmHeader.blockComment("Variables for bank " + ramSectionName);
             
             m_cHeader.blankLine();
             m_cHeader.blockComment("Variables for bank " + ramSectionName);
 
             
             buildInternalPointers(bankNumber);
 
             if (!m_hasBankedVariables)
             {
                 m_log.log(Level.WARNING,
                         "The generated module has banked variables. Please configure the linker.");
                 m_hasBankedVariables = true;
             }
         }
     }
 
     /**
      * @param bankNumber the bank to produce a section name for.
      * @return the section name for the linker script.
      */
     private String getRamSectionName(final int bankNumber)
     {
         return m_modelName + "Bank" + bankNumber;
     }
 
     /**
      * Define and output the internal pointer variables if not already done.
      * This is called during rendering of the bank identified in bankNumber.
      * 
      * @param bankNumber
      *            bank number to store them.
      */
     private void buildInternalPointers(final int bankNumber)
     {
         if (m_statePointer == null)
         {
             m_statePointer = new Variable("_statePtr", Ownership.INTERNAL,
                     bankNumber, m_largeRomModel ? 3 : 2);
             visitCreateVariableDefinition(m_statePointer);
         }
         if (m_subroutineStack == null && m_requiresSubroutineStack)
         {
             m_subroutineStack = new Variable("_subReturn", Ownership.INTERNAL,
                     bankNumber, m_largeRomModel ? 3 : 2);
             visitCreateVariableDefinition(m_subroutineStack);
         }
     }
 
     @Override
     public void visitStartCode()
     {
         if (m_statePointer == null)
         {
             // Place it in ACCESS
             visitStartAccessVariables(true);
         }
 
         m_assembler.blankLine();
         m_assembler.writeSection(m_modelName + "Code", "CODE");
         writeInitMethod();
         writeProcessStateMethod();
     }
 
     /** Output the method that initialises the state machine. */
     private void writeInitMethod()
     {
         clearBankSel();
         m_assembler.blankLine();
         m_assembler.startBlockComment();
         m_assembler.writeBlockCommentLine("State model initialisation.");
         m_assembler.endBlockComment();
 
         m_assembler.writeLabel(getInitMethodName());
         clearBankSel();
         setPointer(m_statePointer, getNodeStepLabel(m_rootState.getStateName()));
         m_assembler.opCode("RETURN");
     }
 
     /** Output the method that processes the current state. */
     private void writeProcessStateMethod()
     {
         clearBankSel();
         m_assembler.blankLine();
         m_assembler.startBlockComment();
         m_assembler.writeBlockCommentLine("State model entry point.");
         m_assembler
                 .writeBlockCommentLine("This method executes the code for the current state.");
         m_assembler.endBlockComment();
 
         m_assembler.writeLabel(getStepMethodName());
         gotoPointer(m_statePointer);
 
     }
 
     @Override
     public void startSharedEntryCode(final INode node)
     {
         assert m_codeBlockStack.empty() : "Code block stack not cleared by previous node";
 
         m_currentNodeName = null; // Do not allow matching
         clearBankSel();
         m_assembler.blankLine();
         m_assembler.startBlockComment();
         m_assembler.writeBlockCommentLine("Node " + node.getStateName() + " entry code.");
         m_assembler.endBlockComment();
         m_assembler.writeLabel(getNodeEntryLabel(node.getStateName()));
     }
 
     @Override
     public void startNode(final INode node)
     {
         assert m_codeBlockStack.empty() : "Code block stack not cleared by previous node";
 
         clearBankSel();
         m_assembler.blankLine();
         m_assembler.startBlockComment();
         m_assembler.writeBlockCommentLine("Node " + node.getStateName()
                 + " step code.");
         m_assembler.endBlockComment();
         m_assembler.writeLabel(getNodeStepLabel(node.getStateName()));
         m_currentNodeName = node.getStateName();
         m_nodeHasSubroutineCalls = false;
     }
 
     @Override
     public void visitTransition(final Transition transition)
     {
         push();
         m_assembler.writeComment("-- Start of transition --");
 
         // At the moment we don't cleverly work out routes through the
         // conditions, so if more than 1 bank is used we are paranoid and
         // banksel after any branch.
         if (m_numberOfBanksUsed > 1)
         {
             clearBankSel();
         }
     }
 
 
     @Override
     public void visitTransitionPreconditionGE(final Variable variable, final int value)
     {
         m_assembler.writeComment(String.format(" Precondition %s >= %s",
                 variable.getName(), formatByte(value)));
         if (value != 0)
         {
             banksel(variable);
             m_assembler.opCode("MOVLW", formatInt(value - 1));
             m_assembler.opCode("CPFSGT", variable.getName(), access(variable));
             exitCodeBlock(0);
         }
     }
 
     @Override
     public void visitTransitionPreconditionEQ(final Variable variable, final int value)
     {
         m_assembler.writeComment(String.format(" Precondition %s == %s",
                 variable.getName(), formatByte(value)));
         banksel(variable);
         m_assembler.opCode("MOVLW", formatInt(value));
         m_assembler.opCode("CPFSEQ", variable.getName(), access(variable));
         exitCodeBlock(0);
     }
 
     @Override
     public void visitTransitionPreconditionLE(final Variable variable, final int value)
     {
         m_assembler.writeComment(String.format(" Precondition %s <= %s",
                 variable.getName(), formatByte(value)));
         if (value < 255)
         {
             banksel(variable);
             m_assembler.opCode("MOVLW", formatInt(value + 1));
             m_assembler.opCode("CPFSLT", variable.getName(), access(variable));
             exitCodeBlock(0);
         }
     }
 
     @Override
     public void visitTransitionPreconditionFlag(final Variable flag, final int bit, final boolean expectedValue)
     {
         m_assembler.writeComment(String.format(
                 " Precondition Flag %s:%d == %b", flag.getName(), bit,
                 expectedValue));
         int offset = bit / 8;
         int bitInByte = bit % 8;
         String opCode = expectedValue ? "BTFSS" : "BTFSC";
         banksel(flag);
         m_assembler.opCode(opCode, offset(flag, offset), Integer.toString(bitInByte), access(flag));
         exitCodeBlock(0);
     }
 
     @Override
     public void visitCommandCopyVariableToIndexedVariable(final Variable source,
             final Variable output, final Variable indexer)
     {
         if (source.getSize() > 1)
         {
             m_log.warning("Indexed variable handling code currently only supports 8 bit values. Variable: "
                     + source.getName());
         }
         
         m_assembler.writeComment(String.format(" Command %s[%s] := %s",
                 output.getName(), indexer.getName(), source.getName()));
         m_assembler.opCode("LFSR", "FSR0", output.getName());
         banksel(indexer);
         m_assembler.opCode("MOVF", indexer.getName(), "W", access(indexer));
         m_assembler.opCode("MOVFF", source.getName(), "PLUSW0");
     }
 
     @Override
     public void visitCommandCopyVariable(final Variable input, final Variable output)
     {
         m_assembler.writeComment(String.format(" Command %s := %s",
                 output.getName(), input.getName()));
         for (int i = 0; i < input.getSize() && i < output.getSize(); i++)
         {
             m_assembler.opCode("MOVFF", offset(input, i), offset(output, i));
         }
     }
 
     @Override
     public void visitCommandClearVariable(final Variable variable)
     {
         m_assembler.writeComment(String.format(" Command %s := 0",
                 variable.getName()));
         banksel(variable);
         for (int i = 0; i < variable.getSize(); i++)
         {
             m_assembler.opCode("CLRF", offset(variable, i), access(variable));
         }
     }
 
     @Override
     public void visitCommandClearIndexedVariable(final Variable variable, final Variable indexer)
     {
         m_assembler.writeComment(String.format(" Command %s[%s] := 0",
                 variable.getName(), indexer.getName()));
         m_assembler.opCode("LFSR", "FSR0", variable.getName());
         banksel(indexer);
         m_assembler.opCode("MOVF", indexer.getName(), "W", access(indexer));
         m_assembler.opCode("CLRF", "PLUSW0", "A");
 
     }
 
     @Override
     public void visitCommandIncrementVariable(final Variable variable)
     {
         m_assembler.writeComment(String.format(" Command %s++",
                 variable.getName()));
         banksel(variable);
         m_assembler.opCode("INCF", variable.getName(), "F", access(variable));
         for (int i = 1; i < variable.getSize(); i++)
         {
             m_assembler.opCode("BTFSC", "STATUS", "C", "A");
             m_assembler.opCode("INCF", offset(variable, i), "F",
                     access(variable));
         }
     }
 
     @Override
     public void visitCommandSetFlag(final Variable flags, final int bit, final boolean newValue)
     {
         m_assembler.writeComment(String.format(" Command %s:%d := %b",
                 flags.getName(), bit, newValue));
         int offset = bit / 8;
         int bitInByte = bit % 8;
         String opCode = newValue ? "BSF" : "BCF";
         banksel(flags);
         m_assembler.opCode(opCode, offset(flags, offset),
                 Integer.toString(bitInByte), access(flags));
     }
 
     @Override
     public void visitCommandMethodCall(final RomLocation method)
     {
         m_assembler.writeComment(" Command CALL " + method.getName());
         m_assembler.opCode("CALL", method.getName());
     }
 
     @Override
     public void visitTransitionGoToSharedEntryCode(final INode node)
     {
         String stateName = node.getStateName();
         m_assembler.writeComment(" Transition GOTO (Shared) " + stateName);
         m_assembler.opCode("GOTO", getNodeEntryLabel(stateName));
     }
 
     @Override
     public void visitTransitionGoToNode(final INode node)
     {
         String stateName = node.getStateName();
 
         if (m_nodeHasSubroutineCalls || !stateName.equals(m_currentNodeName))
         {
             m_assembler.writeComment(" Transition GOTO " + stateName);
             setPointer(m_statePointer, getNodeStepLabel(stateName));
         }
         else
         {
             m_assembler.writeComment(" Transition GOTO SELF");
         }
 
         if (m_returnCode != null && m_returnCode.size() > 0)
         {
             for (String returnStatement : m_returnCode)
             {
                 m_assembler.opCode(returnStatement);
             }
         }
         else
         {
             m_assembler.opCode("RETURN");
         }
     }
 
     @Override
     public void visitTransitionReturnFromSubroutineStack()
     {
         m_assembler.writeComment(" Return from subroutine");
         gotoPointer(m_subroutineStack);
     }
 
     @Override
     public void endTransition(final Transition transition)
     {
         pop();
     }
 
     @Override
     public void push()
     {
         m_codeBlockStack.push(getNextInternalLabel());
         m_assembler.indent();
     }
 
     /**
      * Get the label for the transition referenced by
      * {@link #m_internalLabelCounter}. This will be the label of the
      * corresponding {@link #pop()}
      * 
      * Calling this method increments the counter.
      * 
      * @return the next generated label.
      */
     private String getNextInternalLabel()
     {
         return "lbl_" + m_internalLabelCounter++;
     }
 
     @Override
     public void saveReturnOnSubroutineStack()
     {
         String label = m_codeBlockStack.peek();
         m_assembler
                 .writeComment(" Prepare for Gosub. Subroutine will return to "
                         + label);
         setPointer(m_subroutineStack, label);
         m_nodeHasSubroutineCalls = true;
     }
 
     @Override
     public void exitCodeBlock(final int levels)
     {
         String name = m_codeBlockStack.elementAt(m_codeBlockStack.size() - 1 - levels);
         // m_assembler.writeComment(String.format(" Break from code block (%d levels)",
         // levels));
         m_assembler.opCode("GOTO", name);
     }
 
     @Override
     public void pop()
     {
         String label = m_codeBlockStack.pop();
         m_assembler.unindent();
         m_assembler.writeLabel(label);
     }
 
     @Override
     public void endNode(final Node node)
     {
         assert m_codeBlockStack.empty() : "Node " + m_currentNodeName
                 + " did not leave code block stack empty";
     }
 
     @Override
     public void finished()
     {
         m_assembler.blankLine();
         m_assembler.writeEndMarker();
 
         m_cHeader.blankLine();
         m_cHeader.write("#endif\n");
         m_asmHeader.blankLine();
         m_asmHeader.write("#endif\n");
 
         m_assembler.safeClose();
         m_cHeader.safeClose();
         m_asmHeader.safeClose();
     }
 
     /**
      * Write instructions to go to the location in the given little-endian
      * pointer.
      * 
      * @param pointer pointer variable to go to.
      */
     private void gotoPointer(final Variable pointer)
     {
         banksel(pointer);
 
         if (m_largeRomModel)
         {
             m_assembler.opCode("MOVFF", offset(pointer, 2), "PCLATU");
         }
         else
         {
             m_assembler.opCode("CLRF", "PCLATU", "A");
         }
 
         m_assembler.opCode("MOVFF", offset(pointer, 1), "PCLATH");
         m_assembler.opCode("MOVF", pointer.getName(), "W", access(pointer));
         m_assembler.opCode("MOVWF", "PCL", "A");
     }
 
     /**
      * Write instructions to populate the little-endian pointer with the given
      * label.
      * 
      * @param pointer pointer variable to populate.
      * @param label ASM label to read from.
      */
     private void setPointer(final Variable pointer, final String label)
     {
         banksel(pointer);
         if (m_largeRomModel)
         {
             m_assembler.opCode("MOVLW", "UPPER(" + label + ")");
             m_assembler.opCode("MOVWF", offset(pointer, 2), access(pointer));
         }
 
         m_assembler.opCode("MOVLW", "HIGH(" + label + ")");
         m_assembler.opCode("MOVWF", offset(pointer, 1), access(pointer));
 
         m_assembler.opCode("MOVLW", "LOW(" + label + ")");
         m_assembler.opCode("MOVWF", offset(pointer, 0), access(pointer));
     }
 
     /**
      * If the current bank is not that of the variable, and the variable is not
      * access bank, output a BANKSEL command.
      * 
      * @see #clearBankSel
      * @param variable the variable to be accessed.
      */
     private void banksel(final Variable variable)
     {
         int bank = variable.getBank();
         if (!variable.isAccess() && bank != m_currentBank)
         {
             m_assembler.opCode("BANKSEL", variable.getName());
             m_currentBank = bank;
         }
     }
 
     /**
      * The current bank is unknown - so a banksel will be issued for any banked
      * access.
      */
     private void clearBankSel()
     {
         m_currentBank = -1;
     }
 
     /**
      * Return the location of the variable with the given offset for use in
      * assembler.
      * 
      * @param v the variable to access
      * @param offset the fixed offset.
      * @return assembler constant, for example "(v + .2)"
      */
     private String offset(final Variable v, final int offset)
     {
         if (offset != 0)
         {
             return String.format("(%s + .%d)", v.getName(), offset);
         }
         return v.getName();
     }
 
     /**
      * Return the MPASM access/bank flag (A or nothing) for the variable.
      * 
      * @param v variable to access
      * @return "A" or nothing.
      */
     private String access(final Variable v)
     {
         if (v.isAccess())
         {
             return "A";
         }
         else
         {
             return null;
         }
     }
 
     /**
      * Format an integer for MPASM.
      * 
      * @param x the integer
      * @return an MPASM integer, for example ".10"
      */
     private String formatInt(final int x)
     {
         return "." + x;
     }
 
     /**
      * Format a byte value for assembler as hex or character constant.
      * 
      * @param x value to format
      * @return an MPASM hex constant or character constant, for example 'a' or 0x32.
      */
     private String formatByte(final int x)
     {
         if (x >= MIN_PRINTABLE_ASCII && x <= MAX_PRINTABLE_ASCII)
         {
             return String.format("'%c'", x);
         }
         return String.format("0x%02x", x);
     }
 
 }
