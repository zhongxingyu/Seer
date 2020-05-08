 /*
  * CodeGenerator.java
  * Copyright (C) 2012 Pre-Alpha Software
  * All rights reserved.
  */
 
 package com.prealpha.diamond.compiler;
 
 import com.google.common.base.Functions;
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.ListMultimap;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Queues;
 import com.prealpha.diamond.compiler.node.AAddExpression;
 import com.prealpha.diamond.compiler.node.AArrayAccessAssignmentTarget;
 import com.prealpha.diamond.compiler.node.AArrayAccessPrimaryExpression;
 import com.prealpha.diamond.compiler.node.AAssignment;
 import com.prealpha.diamond.compiler.node.AAssignmentExpression;
 import com.prealpha.diamond.compiler.node.ABitwiseAndExpression;
 import com.prealpha.diamond.compiler.node.ABitwiseComplementExpression;
 import com.prealpha.diamond.compiler.node.ABitwiseOrExpression;
 import com.prealpha.diamond.compiler.node.ABitwiseXorExpression;
 import com.prealpha.diamond.compiler.node.ABlockStatement;
 import com.prealpha.diamond.compiler.node.ABreakStatement;
 import com.prealpha.diamond.compiler.node.ACaseGroup;
 import com.prealpha.diamond.compiler.node.AClassDeclaration;
 import com.prealpha.diamond.compiler.node.AClassTopLevelStatement;
 import com.prealpha.diamond.compiler.node.AConditionalAndExpression;
 import com.prealpha.diamond.compiler.node.AConditionalExpression;
 import com.prealpha.diamond.compiler.node.AConditionalNotExpression;
 import com.prealpha.diamond.compiler.node.AConditionalOrExpression;
 import com.prealpha.diamond.compiler.node.AConstructorClassStatement;
 import com.prealpha.diamond.compiler.node.AConstructorDeclaration;
 import com.prealpha.diamond.compiler.node.AConstructorInvocation;
 import com.prealpha.diamond.compiler.node.AConstructorInvocationPrimaryExpression;
 import com.prealpha.diamond.compiler.node.AContinueStatement;
 import com.prealpha.diamond.compiler.node.ADefaultCaseGroup;
 import com.prealpha.diamond.compiler.node.ADeleteStatement;
 import com.prealpha.diamond.compiler.node.ADivideExpression;
 import com.prealpha.diamond.compiler.node.ADoStatement;
 import com.prealpha.diamond.compiler.node.AEqualExpression;
 import com.prealpha.diamond.compiler.node.AExpressionQualifiedName;
 import com.prealpha.diamond.compiler.node.AExpressionStatement;
 import com.prealpha.diamond.compiler.node.AFalseLiteral;
 import com.prealpha.diamond.compiler.node.AFieldClassStatement;
 import com.prealpha.diamond.compiler.node.AFieldDeclaration;
 import com.prealpha.diamond.compiler.node.AForStatement;
 import com.prealpha.diamond.compiler.node.AFunctionClassStatement;
 import com.prealpha.diamond.compiler.node.AFunctionDeclaration;
 import com.prealpha.diamond.compiler.node.AFunctionInvocationPrimaryExpression;
 import com.prealpha.diamond.compiler.node.AFunctionTopLevelStatement;
 import com.prealpha.diamond.compiler.node.AGreaterOrEqualExpression;
 import com.prealpha.diamond.compiler.node.AGreaterThanExpression;
 import com.prealpha.diamond.compiler.node.AIdentifierAssignmentTarget;
 import com.prealpha.diamond.compiler.node.AIdentifierPrimaryExpression;
 import com.prealpha.diamond.compiler.node.AIfThenElseStatement;
 import com.prealpha.diamond.compiler.node.AIfThenStatement;
 import com.prealpha.diamond.compiler.node.AIntegralLiteral;
 import com.prealpha.diamond.compiler.node.ALessOrEqualExpression;
 import com.prealpha.diamond.compiler.node.ALessThanExpression;
 import com.prealpha.diamond.compiler.node.ALiteralPrimaryExpression;
 import com.prealpha.diamond.compiler.node.ALocalDeclaration;
 import com.prealpha.diamond.compiler.node.ALocalDeclarationAssignmentTarget;
 import com.prealpha.diamond.compiler.node.AModulusExpression;
 import com.prealpha.diamond.compiler.node.AMultiplyExpression;
 import com.prealpha.diamond.compiler.node.ANotEqualExpression;
 import com.prealpha.diamond.compiler.node.ANumericNegationExpression;
 import com.prealpha.diamond.compiler.node.AParentheticalPrimaryExpression;
 import com.prealpha.diamond.compiler.node.APrimaryExpression;
 import com.prealpha.diamond.compiler.node.AQualifiedArrayAccess;
 import com.prealpha.diamond.compiler.node.AQualifiedFunctionInvocation;
 import com.prealpha.diamond.compiler.node.AQualifiedNameAssignmentTarget;
 import com.prealpha.diamond.compiler.node.AQualifiedNamePrimaryExpression;
 import com.prealpha.diamond.compiler.node.AReturnStatement;
 import com.prealpha.diamond.compiler.node.AShiftLeftExpression;
 import com.prealpha.diamond.compiler.node.AShiftRightExpression;
 import com.prealpha.diamond.compiler.node.AStringLiteral;
 import com.prealpha.diamond.compiler.node.ASubtractExpression;
 import com.prealpha.diamond.compiler.node.ASwitchStatement;
 import com.prealpha.diamond.compiler.node.AThisPrimaryExpression;
 import com.prealpha.diamond.compiler.node.ATrueLiteral;
 import com.prealpha.diamond.compiler.node.ATypeTokenQualifiedName;
 import com.prealpha.diamond.compiler.node.AUnqualifiedArrayAccess;
 import com.prealpha.diamond.compiler.node.AUnqualifiedFunctionInvocation;
 import com.prealpha.diamond.compiler.node.AUnsignedShiftRightExpression;
 import com.prealpha.diamond.compiler.node.AVoidFunctionDeclaration;
 import com.prealpha.diamond.compiler.node.AWhileStatement;
 import com.prealpha.diamond.compiler.node.Node;
 import com.prealpha.diamond.compiler.node.PArrayAccess;
 import com.prealpha.diamond.compiler.node.PCaseGroup;
 import com.prealpha.diamond.compiler.node.PClassStatement;
 import com.prealpha.diamond.compiler.node.PExpression;
 import com.prealpha.diamond.compiler.node.PFunctionDeclaration;
 import com.prealpha.diamond.compiler.node.PIntegralLiteral;
 import com.prealpha.diamond.compiler.node.PLiteral;
 import com.prealpha.diamond.compiler.node.PLocalDeclaration;
 import com.prealpha.diamond.compiler.node.PPrimaryExpression;
 import com.prealpha.diamond.compiler.node.PQualifiedName;
 import com.prealpha.diamond.compiler.node.PStatement;
 import com.prealpha.diamond.compiler.node.PTopLevelStatement;
 import com.prealpha.diamond.compiler.node.TIdentifier;
 
 import java.math.BigInteger;
 import java.util.Deque;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import static com.google.common.base.Preconditions.*;
 
 final class CodeGenerator extends ScopeAwareWalker {
     /**
      * A list of exceptions that have occurred during this phase. The inherited method signatures prevent us from
      * throwing exceptions during the walk, so this buffer is used to store them until the end of the phase. As a side
      * effect, the phase may generate multiple errors if needed, instead of stopping after the first phase.
      */
     private final List<Exception> exceptionBuffer;
 
     /**
      * The expression type information produced by the {@link TypeEnforcer} phase. Each expression (and some other node
      * types) contains an entry in this map which indicates the type of that expression. This type information is
      * needed, for example, to determine the width of an expression result.
      */
     private final Map<Node, TypeToken> types;
 
     /**
      * <p>
      *     The instructions generated by this phase. Each top level statement has its own list of instructions; to
      *     generate the output for the entire file, we attach instructions to JSR to the main method, followed by a BRK,
      *     and then (if BRK isn't supported) an infinite loop. Then we emit the instructions for each method, labeled,
      *     in an undefined order.
      * </p>
      *
      * <p>
      *     During the code generation, instructions should always be added to the list corresponding to the
      *     {@link #context} key. In general, the {@link #write(String)} method, which uses {@code context}, should be
      *     used for this purpose.
      * </p>
      */
     private final ListMultimap<PTopLevelStatement, String> instructions;
 
     /**
      * Models the stack and its contents as they change during program execution. Each local variable should be placed
      * on the stack as soon as it is declared, and removed when it falls out of scope. The stack also contains several
      * placeholder values, used to represent values on the stack that do not correspond to local variables:
      * <ul>
      *     <li>{@link #thisSymbol}, a pointer to {@code this} for instance methods.</li>
      *     <li>A one-word JSR pointer, to account for the stack offset caused by the JSR instruction used to enter
      *     functions. (This and the preceding case are collectively called {@link FunctionPlaceholder}.)</li>
      *     <li>{@link TransientPlaceholder} values, which are used in binary expressions to store the left operand while
      *     the right operand is being evaluated. These POP from the stack as soon as they are accessed.</li>
      * </ul>
      */
     private final Deque<TypedSymbol> stack;
 
     /**
      * A stack containing the {@link FlowStructure} instances enclosing the currently executing code. By flow
      * structures, we mean structures like loops and functions that determine and modify the behavior of these three
      * statements, the flow modifiers:
      * <ul>
      *     <li>{@code break}</li>
      *     <li>{@code continue}</li>
      *     <li>{@code return}</li>
      * </ul>
      */
     private final Deque<FlowStructure> flowStructures;
 
     /**
      * The top level statement enclosing the currently executing code. Used to determine the active key for the
      * {@link #instructions} map.
      */
     private PTopLevelStatement context;
 
     /**
      * The result of the last expression to be executed. There are five possible cases:
      * <ul>
      *     <li>If the expression returned a local variable, {@code expressionResult} is that local variable, which must
      *     already be on the stack.</li>
      *     <li>If the expression returned a field, {@code expressionResult} is that field, which is not placed on the
      *     stack. Instead, the expression must copy the pointer to the field's object into register A.</li>
      *     <li>If the expression (fragment) was an {@link PArrayAccess}, {@code expressionResult} is an
      *     {@link ArrayElementPlaceholder}, and register A contains a pointer to the first word of the element accessed.
      *     The placeholders are never placed on the stack and should always be immediately consumed by the enclosing
      *     primary expression or assignment expression.</li>
      *     <li>If the expression returned a value, {@code expressionResult} is {@code null}, and registers A, B, C, and
      *     X contain the value; if the value's width is less than four words, registers are employed in that order.</li>
      *     <li>If the expression was a void method invocation, {@code expressionResult} is {@code null}; the only
      *     context in which this can occur is as a standalone primary expression.</li>
      * </ul>
      */
     private TypedSymbol expressionResult;
 
     /**
      * A pointer to {@code this}, the implicit parameter to all instance methods. {@code thisSymbol} should be pushed
      * to the stack along with the explicit parameters during all method invocations, and popped when those methods
      * return. If the currently executing code is not part of an instance method, {@code thisSymbol} is {@code null}.
      */
     private TypedSymbol thisSymbol;
 
     public CodeGenerator(ScopeAwareWalker scopeSource, List<Exception> exceptionBuffer, Map<Node, TypeToken> types) {
         super(scopeSource);
         this.exceptionBuffer = exceptionBuffer;
         this.types = ImmutableMap.copyOf(types);
         instructions = ArrayListMultimap.create();
         stack = Queues.newArrayDeque();
         flowStructures = Lists.newLinkedList();
     }
 
     public List<String> getInstructions() throws SemanticException {
         AVoidFunctionDeclaration mainMethod = null;
         for (PTopLevelStatement topLevelStatement : instructions.keySet()) {
             if (topLevelStatement instanceof AFunctionTopLevelStatement) {
                 PFunctionDeclaration functionDeclaration = ((AFunctionTopLevelStatement) topLevelStatement).getFunctionDeclaration();
                 if (functionDeclaration instanceof AVoidFunctionDeclaration) {
                     AVoidFunctionDeclaration voidFunctionDeclaration = (AVoidFunctionDeclaration) functionDeclaration;
                     if (voidFunctionDeclaration.getName().getText().equals("main") && voidFunctionDeclaration.getParameters().isEmpty()) {
                         mainMethod = voidFunctionDeclaration;
                     }
                 }
             }
         }
         if (mainMethod == null) {
             throw new SemanticException("no main method located");
         }
 
         List<String> toReturn = Lists.newArrayList();
         toReturn.add("JSR " + getStartLabel(mainMethod));
         toReturn.add("BRK");
         toReturn.add("SUB PC 1");
         toReturn.addAll(instructions.values());
         return toReturn;
     }
 
     @Override
     protected void onExitScope(Node scopeKey) {
         assert (getScope() == getScope(scopeKey));
         reclaimScope(getScope(scopeKey));
         super.onExitScope(scopeKey);
     }
 
     private void reclaimScope(Scope scope) {
         for (LocalSymbol local : Lists.reverse(scope.getLocals())) {
             reclaimLocal(local);
         }
     }
 
     void reclaimScope() {
         for (LocalSymbol local : Lists.reverse(getScope().getLocals())) {
             doReclaimLocal(local);
         }
     }
 
     private void reclaimLocal(TypedSymbol local) {
         TypedSymbol popped = stack.pop();
         assert (local.equals(popped));
         doReclaimLocal(local);
     }
 
     private void doReclaimLocal(TypedSymbol local) {
         int width = local.getType().getWidth();
         if (local instanceof TransientPlaceholder) {
             width -= ((TransientPlaceholder) local).stackOffset;
         }
         write(String.format("ADD SP 0x%04x", width));
     }
 
     String getStartLabel(Node node) {
         return "start_" + getBaseLabel(node);
     }
 
     String getEndLabel(Node node) {
         return "end_" + getBaseLabel(node);
     }
 
     private final Map<Node, Integer> labels = Maps.newHashMap();
 
     private int nextLabel = 0;
 
     private String getBaseLabel(Node node) {
         LineNumberFinder finder = new LineNumberFinder();
         node.apply(finder);
         if (finder.hasLineNumber()) {
             return String.format("%s_%s_%s", node.getClass().getSimpleName(), finder.getLineNumber(), finder.getColumnNumber());
         } else {
             if (!labels.containsKey(node)) {
                 labels.put(node, nextLabel++);
             }
             return String.format("%s_%s", node.getClass().getSimpleName(), labels.get(node));
         }
     }
 
     private void inline(Node subject) {
         write(":" + getStartLabel(subject));
         subject.apply(this);
         write(":" + getEndLabel(subject));
     }
 
     private String lookup(TypedSymbol symbol, int wordOffset) {
         checkArgument(symbol == null || wordOffset < symbol.getType().getWidth());
         checkArgument(wordOffset >= 0);
         if (symbol instanceof LocalSymbol || symbol instanceof FunctionPlaceholder) {
             assert stack.contains(symbol);
             int symbolOffset = 0;
             for (TypedSymbol stackSymbol : stack) {
                 if (symbol.equals(stackSymbol)) {
                     break;
                 } else {
                     symbolOffset += stackSymbol.getType().getWidth();
                 }
             }
             if (symbolOffset == 0 && wordOffset == 0) {
                 return "[SP]";
             } else {
                 return String.format("[SP+%d]", symbolOffset + wordOffset);
             }
         } else if (symbol instanceof TransientPlaceholder) {
             return ((TransientPlaceholder) symbol).lookup(wordOffset);
         } else if (symbol instanceof ArrayElementPlaceholder) {
             if (wordOffset == 0) {
                 return "[A]";
             } else {
                 return String.format("[A+%d]", wordOffset);
             }
         } else if (symbol instanceof FieldSymbol) {
             if (!symbol.getModifiers().contains(Modifier.STATIC)) {
                 // the object we need for this field is located in register A
                 ClassSymbol fieldClass = ((FieldSymbol) symbol).getDeclaringClass();
                 Scope scope = getScope(fieldClass.getDeclaration());
                 List<FieldSymbol> allFields = scope.getFields();
                 int fieldOffset = 0;
                 for (FieldSymbol field : allFields) {
                     if (field != symbol) {
                         fieldOffset += field.getType().getWidth();
                     } else {
                         break;
                     }
                 }
                 fieldOffset += wordOffset;
                 if (fieldOffset == 0) {
                     return "[A]";
                 } else {
                     return String.format("[A+%d]", fieldOffset);
                 }
             } else {
                 // see #caseAFieldDeclaration(AFieldDeclaration)
                 return String.format("%d_%s", wordOffset, getBaseLabel(symbol.getDeclaration()));
             }
         } else {
             // the expression result is a value, stored in registers A, B, C, and X (as needed)
             assert symbol == null;
             switch (wordOffset) {
                 case 0:
                     return "A";
                 case 1:
                     return "B";
                 case 2:
                     return "C";
                 case 3:
                     return "X";
                 default:
                     throw new AssertionError();
             }
         }
     }
 
     private String lookupSafe(TypedSymbol symbol, int wordOffset) {
         String lookup = lookup(symbol, wordOffset);
         if (lookup.equals("POP")) {
             ((TransientPlaceholder) symbol).stackOffset -= 1;
             return "[SP]";
         } else {
             return lookup;
         }
     }
 
     private void requireValue(TypeToken type) {
         requireValue(type, type);
     }
 
     private void requireValue(TypeToken type, TypeToken promotedType) {
         if (expressionResult != null || !type.equals(promotedType)) {
             assert (expressionResult == null || expressionResult.getType().equals(type));
             checkArgument(!(expressionResult instanceof TransientPlaceholder));
             int lastWord = type.getWidth() - 1;
             boolean signed = (type instanceof IntegralTypeToken && ((IntegralTypeToken) type).isSigned());
             switch (promotedType.getWidth()) {
                 case 4:
                     if (type.getWidth() < 4) {
                         if (signed) {
                             write("SET X " + lookup(expressionResult, lastWord));
                             write("ASR X 16");
                             write("SET C " + lookup(expressionResult, lastWord));
                             write("ASR C 16");
                         } else {
                             write("SET X 0x0000");
                             write("SET C 0x0000");
                         }
                     } else {
                         write("SET X " + lookup(expressionResult, 3));
                         write("SET C " + lookup(expressionResult, 2));
                     }
                 case 2:
                     if (type.getWidth() < 2) {
                         if (signed) {
                             write("SET B " + lookup(expressionResult, lastWord));
                             write("ASR B 16");
                         } else {
                             write("SET B 0x0000");
                         }
                     } else {
                         write("SET B " + lookup(expressionResult, 1));
                     }
                 case 1:
                     write("SET A " + lookup(expressionResult, 0));
                     break;
                 default:
                     assert false;
             }
             expressionResult = null;
         }
     }
 
     private void requireStack(TypeToken type) {
         requireStack(type, type);
     }
 
     private void requireStack(TypeToken type, TypeToken promotedType) {
         if ((expressionResult instanceof LocalSymbol || expressionResult instanceof Placeholder)
                 && type.equals(promotedType)) {
             assert stack.contains(expressionResult);
         } else {
             checkArgument(!(expressionResult instanceof TransientPlaceholder));
             int lastWord = type.getWidth() - 1;
             boolean signed = (type instanceof IntegralTypeToken && ((IntegralTypeToken) type).isSigned());
             switch (promotedType.getWidth()) {
                 case 4:
                     if (type.getWidth() < 4) {
                         if (signed) {
                             write("SET PUSH " + lookup(expressionResult, lastWord));
                             write("ASR [SP] 16");
                             write("SET PUSH " + lookup(expressionResult, lastWord));
                             write("ASR [SP] 16");
                         } else {
                             write("SET PUSH 0x0000");
                             write("SET PUSH 0x0000");
                         }
                     } else {
                         write("SET PUSH " + lookup(expressionResult, 3));
                         write("SET PUSH " + lookup(expressionResult, 2));
                     }
                 case 2:
                     if (type.getWidth() < 2) {
                         if (signed) {
                             write("SET PUSH " + lookup(expressionResult, lastWord));
                             write("ASR [SP] 16");
                         } else {
                             write("SET PUSH 0x0000");
                            write("SET PUSH 0x0000");
                         }
                     } else {
                         write("SET PUSH " + lookup(expressionResult, 1));
                     }
                 case 1:
                     write("SET PUSH " + lookup(expressionResult, 0));
                     break;
                 default:
                     assert false;
             }
            expressionResult = new TransientPlaceholder(promotedType);
         }
     }
 
     void write(String instruction) {
         instructions.put(context, instruction);
     }
 
     private static abstract class Placeholder implements TypedSymbol {
         private final TypeToken type;
 
         protected Placeholder(TypeToken type) {
             checkNotNull(type);
             this.type = type;
         }
 
         @Override
         public Node getDeclaration() {
             return null;
         }
 
         @Override
         public TypeToken getType() {
             return type;
         }
 
         @Override
         public Set<Modifier> getModifiers() {
             return ImmutableSet.of();
         }
     }
 
     private static final class FunctionPlaceholder extends Placeholder {
         public FunctionPlaceholder(TypeToken type) {
             super(type);
         }
     }
 
     private static final class ArrayElementPlaceholder extends Placeholder {
         public ArrayElementPlaceholder(TypeToken type) {
             super(type);
         }
     }
 
    private static final class TransientPlaceholder extends Placeholder {
         private int stackOffset;
 
         public TransientPlaceholder(TypeToken type) {
             super(type);
             stackOffset = 0;
         }
 
         public String lookup(int wordOffset) {
             if (wordOffset == stackOffset) {
                 stackOffset += 1;
                 return "POP";
             } else if (wordOffset > stackOffset) {
                 return String.format("[SP+%d]", wordOffset - stackOffset);
             } else {
                 throw new IllegalArgumentException();
             }
         }
     }
 
     @Override
     public void caseAClassTopLevelStatement(AClassTopLevelStatement topLevelStatement) {
         assert stack.isEmpty();
         context = topLevelStatement;
         inline(topLevelStatement.getClassDeclaration());
         context = null;
         assert stack.isEmpty();
     }
 
     @Override
     public void caseAFunctionTopLevelStatement(AFunctionTopLevelStatement topLevelStatement) {
         assert stack.isEmpty();
         context = topLevelStatement;
         inline(topLevelStatement.getFunctionDeclaration());
         context = null;
         assert stack.isEmpty();
     }
 
     @Override
     public void caseAIfThenStatement(AIfThenStatement statement) {
         evaluateIfThenElse(statement, statement.getCondition(), statement.getThen(), null);
     }
 
     @Override
     public void caseAIfThenElseStatement(AIfThenElseStatement statement) {
         evaluateIfThenElse(statement, statement.getCondition(), statement.getThen(), statement.getElse());
     }
 
     private void evaluateIfThenElse(Node statement, PExpression condition, PStatement thenBody, PStatement elseBody) {
         inline(condition);
 
         write("IFN " + lookup(expressionResult, 0) + " 0x0000");
         write("SET PC " + getStartLabel(thenBody));
 
         if (elseBody != null) {
             inline(elseBody);
         }
         write("SET PC " + getEndLabel(statement));
         inline(thenBody);
     }
 
     @Override
     public void caseAWhileStatement(AWhileStatement statement) {
         flowStructures.push(new WhileFlowStructure(this, statement));
 
         inline(statement.getCondition());
 
         write("IFE " + lookup(expressionResult, 0) + " 0x0000");
         write("SET PC " + getEndLabel(statement));
 
         inline(statement.getBody());
         write("SET PC " + getStartLabel(statement.getCondition()));
 
         flowStructures.pop();
     }
 
     @Override
     public void caseAForStatement(AForStatement statement) {
         super.inAForStatement(statement);
         flowStructures.push(new ForFlowStructure(this, statement));
 
         inline(statement.getInit());
 
         // we actually want the update on top, so skip to the condition
         write("SET PC " + getStartLabel(statement.getCondition()));
 
         inline(statement.getUpdate());
 
         inline(statement.getCondition());
 
         write("IFE " + lookup(expressionResult, 0) + " 0x0000");
         write("SET PC reclaim_" + getBaseLabel(statement));
 
         inline(statement.getBody());
         write("SET PC " + getStartLabel(statement.getUpdate()));
 
         flowStructures.pop();
         write(":reclaim_" + getBaseLabel(statement));
         super.outAForStatement(statement);
     }
 
     @Override
     public void caseADoStatement(ADoStatement statement) {
         flowStructures.push(new DoFlowStructure(this, statement));
 
         inline(statement.getBody());
 
         inline(statement.getCondition());
         write("IFN " + lookup(expressionResult, 0) + " 0x0000");
         write("SET PC " + getStartLabel(statement));
 
         flowStructures.pop();
     }
 
     @Override
     public void caseASwitchStatement(ASwitchStatement statement) {
         flowStructures.push(new SwitchFlowStructure(this, statement));
 
         inline(statement.getValue());
 
         PCaseGroup defaultCaseGroup = null;
         for (PCaseGroup caseGroup : statement.getBody()) {
             for (PIntegralLiteral literal : getCaseGroupValues(caseGroup)) {
                 try {
                     long value = IntegralTypeToken.parseLiteral(literal).longValue();
                     switch (types.get(statement.getValue()).getWidth()) {
                         case 4:
                             write(String.format("IFE %s 0x%04x", lookup(expressionResult, 3), (value & 0xffff000000000000L) >>> 48));
                             write(String.format("IFE %s 0x%04x", lookup(expressionResult, 2), (value & 0x0000ffff00000000L) >>> 32));
                         case 2:
                             write(String.format("IFE %s 0x%04x", lookup(expressionResult, 1), (value & 0x00000000ffff0000L) >>> 16));
                         case 1:
                             write(String.format("IFE %s 0x%04x", lookup(expressionResult, 0), (value & 0x000000000000ffffL)));
                             break;
                         default:
                             assert false; // there shouldn't be any other widths
                     }
                     write("SET PC " + getStartLabel(caseGroup));
                 } catch (SemanticException sx) {
                     exceptionBuffer.add(sx);
                 }
             }
             if (caseGroup instanceof ADefaultCaseGroup) {
                 assert (defaultCaseGroup == null);
                 defaultCaseGroup = caseGroup;
             }
         }
         if (defaultCaseGroup != null) {
             write("SET PC " + getStartLabel(defaultCaseGroup));
         } else if (!statement.getBody().isEmpty()) {
             write("SET PC " + getEndLabel(statement.getBody().descendingIterator().next()));
         }
 
         for (PCaseGroup caseGroup : statement.getBody()) {
             inline(caseGroup);
         }
 
         flowStructures.pop();
     }
 
     private Iterable<PIntegralLiteral> getCaseGroupValues(PCaseGroup caseGroup) {
         if (caseGroup instanceof ACaseGroup) {
             return ((ACaseGroup) caseGroup).getValues();
         } else if (caseGroup instanceof ADefaultCaseGroup) {
             return ((ADefaultCaseGroup) caseGroup).getValues();
         } else {
             throw new UnsupportedOperationException("unknown case group flavor");
         }
     }
 
     @Override
     public void caseADeleteStatement(ADeleteStatement statement) {
         throw new NoHeapException();
     }
 
     @Override
     public void caseABreakStatement(ABreakStatement statement) {
         Iterator<FlowStructure> iterator = flowStructures.iterator();
         boolean flag;
         do {
             if (iterator.hasNext()) {
                 flag = iterator.next().onBreak();
             } else {
                 exceptionBuffer.add(new SemanticException(statement, "invalid break"));
                 break;
             }
         } while (!flag);
     }
 
     @Override
     public void caseAContinueStatement(AContinueStatement statement) {
         Iterator<FlowStructure> iterator = flowStructures.iterator();
         boolean flag;
         do {
             if (iterator.hasNext()) {
                 flag = iterator.next().onContinue();
             } else {
                 exceptionBuffer.add(new SemanticException(statement, "invalid continue"));
                 break;
             }
         } while (!flag);
     }
 
     @Override
     public void caseAReturnStatement(AReturnStatement statement) {
         inline(statement.getReturnValue());
         // if the expression didn't return a value, coerce whatever it did return into one
         // this is done in case the result is a local/field that will fall out of scope
         requireValue(types.get(statement.getReturnValue()));
 
         Iterator<FlowStructure> iterator = flowStructures.iterator();
         boolean flag;
         do {
             if (iterator.hasNext()) {
                 flag = iterator.next().onReturn();
             } else {
                 exceptionBuffer.add(new SemanticException(statement, "invalid return"));
                 break;
             }
         } while (!flag);
     }
 
     @Override
     public void caseAExpressionStatement(AExpressionStatement statement) {
         inline(statement.getExpression());
     }
 
     @Override
     public void caseABlockStatement(ABlockStatement statement) {
         super.inABlockStatement(statement);
         for (PStatement enclosedStatement : statement.getStatement()) {
             inline(enclosedStatement);
         }
         super.outABlockStatement(statement);
     }
 
     @Override
     public void caseACaseGroup(ACaseGroup caseGroup) {
         for (PStatement enclosedStatement : caseGroup.getBody()) {
             inline(enclosedStatement);
         }
     }
 
     @Override
     public void caseADefaultCaseGroup(ADefaultCaseGroup caseGroup) {
         for (PStatement enclosedStatement : caseGroup.getBody()) {
             inline(enclosedStatement);
         }
     }
 
     @Override
     public void caseAClassDeclaration(AClassDeclaration classDeclaration) {
         super.inAClassDeclaration(classDeclaration);
         for (PClassStatement enclosedStatement : classDeclaration.getBody()) {
             inline(enclosedStatement);
         }
         super.outAClassDeclaration(classDeclaration);
     }
 
     @Override
     public void caseAFieldClassStatement(AFieldClassStatement classStatement) {
         inline(classStatement.getFieldDeclaration());
     }
 
     @Override
     public void caseAFunctionClassStatement(AFunctionClassStatement classStatement) {
         inline(classStatement.getFunctionDeclaration());
     }
 
     @Override
     public void caseAConstructorClassStatement(AConstructorClassStatement classStatement) {
         inline(classStatement.getConstructorDeclaration());
     }
 
     @Override
     public void caseAFieldDeclaration(AFieldDeclaration declaration) {
         try {
             FieldSymbol symbol = getScope().resolveField(declaration.getName().getText());
             if (symbol.getModifiers().contains(Modifier.STATIC)) {
                 for (int i = 0; i < symbol.getType().getWidth(); i++) {
                     write(String.format(":%d_%s", i, getBaseLabel(declaration)));
                     write("DAT 0x0000");
                 }
             }
         } catch (SemanticException sx) {
             exceptionBuffer.add(sx);
         }
     }
 
     @Override
     public void caseAFunctionDeclaration(AFunctionDeclaration declaration) {
         evaluateParametrizedDeclaration(declaration, declaration.getName().getText(), declaration.getParameters(), declaration.getBody());
     }
 
     @Override
     public void caseAVoidFunctionDeclaration(AVoidFunctionDeclaration declaration) {
         evaluateParametrizedDeclaration(declaration, declaration.getName().getText(), declaration.getParameters(), declaration.getBody());
     }
 
     @Override
     public void caseAConstructorDeclaration(AConstructorDeclaration declaration) {
         evaluateParametrizedDeclaration(declaration, "new", declaration.getParameters(), declaration.getBody());
     }
 
     /*
      * This method invokes super.onEnterScope(Node) and super.onExitScope(Node) directly, bypassing in particular the
      * overridden onExitScope(Node) implementation in this class. This is because of the unusual positioning of locals
      * on the stack within a function declaration. Specifically, the JSR pointer sits between the parameters and the
      * other locals declared in the scope. This requires us to reclaim each set of locals separately, reclaiming the
      * JSR pointer in between.
      */
     private void evaluateParametrizedDeclaration(Node declaration, String name, List<PLocalDeclaration> parameters, List<PStatement> body) {
         assert stack.isEmpty();
         try {
             List<TypeToken> parameterTypes = Lists.transform(parameters, TypeTokenUtil.getDeclarationFunction());
             ParametrizedSymbol symbol;
             if (declaration instanceof AConstructorDeclaration) {
                 symbol = getScope().resolveConstructor(parameterTypes);
             } else {
                 symbol = getScope().resolveFunction(name, parameterTypes);
             }
 
             flowStructures.push(new ParametrizedFlowStructure(this));
 
             super.onEnterScope(declaration);
 
             if ((!symbol.getModifiers().contains(Modifier.STATIC) || symbol instanceof ConstructorSymbol)
                     && symbol.getDeclaringClass() != null) {
                 thisSymbol = new FunctionPlaceholder(new UserDefinedTypeToken(symbol.getDeclaringClass().getName()));
                 stack.push(thisSymbol);
             }
 
             for (PLocalDeclaration parameterDeclaration : parameters) {
                 inline(parameterDeclaration);
             }
 
             FunctionPlaceholder jsrPointer = new FunctionPlaceholder(IntegralTypeToken.UNSIGNED_SHORT);
             stack.push(jsrPointer);
 
             for (PStatement enclosedStatement : body) {
                 inline(enclosedStatement);
             }
 
             // TRY to put the last expression as the return value if we get to this point
             // we need to do this here because it could be a local that's about to fall out of scope
             // this behavior is UNDEFINED because it's not type-checked
             if (symbol.getReturnType() != null) {
                 requireValue(symbol.getReturnType());
             }
 
             // reclaim all locals that are NOT parameters
             for (LocalSymbol local : Lists.reverse(getScope().getLocals())) {
                 if (stack.peek() == jsrPointer) {
                     break;
                 } else {
                     reclaimLocal(local);
                 }
             }
             write("SET PC POP");
             TypedSymbol poppedJsr = stack.pop();
             assert (poppedJsr == jsrPointer);
 
             for (LocalSymbol parameter : Lists.reverse(symbol.getParameters())) {
                 TypedSymbol poppedParameter = stack.pop();
                 assert (parameter.equals(poppedParameter));
             }
 
             if (thisSymbol != null) {
                 TypedSymbol poppedThis = stack.pop();
                 assert (poppedThis == thisSymbol);
                 thisSymbol = null;
             }
 
             super.onExitScope(declaration);
 
             flowStructures.pop();
         } catch (SemanticException sx) {
             exceptionBuffer.add(sx);
         }
         assert stack.isEmpty();
     }
 
     @Override
     public void caseALocalDeclaration(ALocalDeclaration declaration) {
         try {
             expressionResult = getScope().resolveLocal(declaration.getName().getText());
             stack.push(expressionResult);
         } catch (SemanticException sx) {
             exceptionBuffer.add(sx);
         }
     }
 
     @Override
     public void caseALiteralPrimaryExpression(ALiteralPrimaryExpression primaryExpression) {
         inline(primaryExpression.getLiteral());
     }
 
     @Override
     public void caseAIdentifierPrimaryExpression(AIdentifierPrimaryExpression primaryExpression) {
         enforceIdentifier(primaryExpression.getIdentifier());
     }
 
     @Override
     public void caseAQualifiedNamePrimaryExpression(AQualifiedNamePrimaryExpression primaryExpression) {
         inline(primaryExpression.getQualifiedName());
     }
 
     @Override
     public void caseAThisPrimaryExpression(AThisPrimaryExpression primaryExpression) {
         assert thisSymbol != null;
         expressionResult = thisSymbol;
     }
 
     @Override
     public void caseAParentheticalPrimaryExpression(AParentheticalPrimaryExpression primaryExpression) {
         inline(primaryExpression.getExpression());
     }
 
     @Override
     public void caseAFunctionInvocationPrimaryExpression(AFunctionInvocationPrimaryExpression primaryExpression) {
         inline(primaryExpression.getFunctionInvocation());
     }
 
     @Override
     public void caseAConstructorInvocationPrimaryExpression(AConstructorInvocationPrimaryExpression primaryExpression) {
         inline(primaryExpression.getConstructorInvocation());
     }
 
     @Override
     public void caseAArrayAccessPrimaryExpression(AArrayAccessPrimaryExpression primaryExpression) {
         inline(primaryExpression.getArrayAccess());
 
         // see the comment above evaluateArrayAccess(PArrayAccess, PExpression)
         switch (types.get(primaryExpression).getWidth()) {
             case 4:
                 write("SET X [A+3]");
                 write("SET C [A+2]");
             case 2:
                 write("SET B [A+1]");
             case 1:
                 write("SET A [A]"); // note that this has to be done last!
                 break;
             default:
                 assert false; // there shouldn't be any other widths
         }
         expressionResult = null;
     }
 
     @Override
     public void caseAIntegralLiteral(AIntegralLiteral literal) {
         try {
             long value = IntegralTypeToken.parseLiteral(literal.getIntegralLiteral()).longValue();
             switch (types.get(literal).getWidth()) {
                 case 4:
                     write(String.format("SET X 0x%04x", (value & 0xffff000000000000L) >>> 48));
                     write(String.format("SET C 0x%04x", (value & 0x0000ffff00000000L) >>> 32));
                 case 2:
                     write(String.format("SET B 0x%04x", (value & 0x00000000ffff0000L) >>> 16));
                 case 1:
                     write(String.format("SET A 0x%04x", (value & 0x000000000000ffffL)));
                     break;
                 default:
                     assert false; // there shouldn't be any other widths
             }
             expressionResult = null;
         } catch (SemanticException sx) {
             exceptionBuffer.add(sx);
         }
     }
 
     @Override
     public void caseAStringLiteral(AStringLiteral literal) {
         throw new NoHeapException();
     }
 
     @Override
     public void caseATrueLiteral(ATrueLiteral literal) {
         write("SET A 0x0001");
         expressionResult = null;
     }
 
     @Override
     public void caseAFalseLiteral(AFalseLiteral literal) {
         write("SET A 0x0000");
         expressionResult = null;
     }
 
     /*
      * TODO: duplicates TypeEnforcer.outAUnqualifiedFunctionInvocation(AUnqualifiedFunctionInvocation)
      */
     @Override
     public void caseAUnqualifiedFunctionInvocation(AUnqualifiedFunctionInvocation invocation) {
         try {
             List<TypeToken> parameterTypes = Lists.transform(invocation.getParameters(), Functions.forMap(types));
             FunctionSymbol symbol = getScope().resolveFunction(invocation.getFunctionName().getText(), parameterTypes);
             if (!symbol.getModifiers().contains(Modifier.STATIC) && symbol.getDeclaringClass() != null) {
                 inline(new AThisPrimaryExpression());
             }
             evaluateParametrizedInvocation(symbol, invocation.getParameters());
         } catch (SemanticException sx) {
             exceptionBuffer.add(sx);
         }
     }
 
     /*
      * TODO: duplicates TypeEnforcer.outAQualifiedFunctionInvocation(AQualifiedFunctionInvocation)
      */
     @Override
     public void caseAQualifiedFunctionInvocation(AQualifiedFunctionInvocation invocation) {
         try {
             PQualifiedName qualifiedName = invocation.getFunctionName();
             TypeToken type;
             String functionName;
             if (qualifiedName instanceof AExpressionQualifiedName) {
                 AExpressionQualifiedName expressionName = (AExpressionQualifiedName) qualifiedName;
                 PPrimaryExpression expression = expressionName.getTarget();
                 type = types.get(expression);
                 functionName = expressionName.getName().getText();
                 inline(expression);
             } else if (qualifiedName instanceof ATypeTokenQualifiedName) {
                 ATypeTokenQualifiedName typeName = (ATypeTokenQualifiedName) qualifiedName;
                 type = (typeName.getTarget() == null ? null : TypeTokenUtil.fromNode(typeName.getTarget()));
                 functionName = typeName.getName().getText();
             } else {
                 throw new SemanticException(qualifiedName, "unknown qualified name flavor");
             }
             if (type == null || type instanceof UserDefinedTypeToken) {
                 Scope scope;
                 if (type != null) {
                     ClassSymbol classSymbol = getScope().resolveClass(((UserDefinedTypeToken) type).getTypeName());
                     scope = getScope(classSymbol.getDeclaration());
                 } else {
                     scope = getRootScope();
                 }
                 List<TypeToken> parameterTypes = Lists.transform(invocation.getParameters(), Functions.forMap(types));
                 FunctionSymbol symbol = scope.resolveFunction(functionName, parameterTypes);
                 evaluateParametrizedInvocation(symbol, invocation.getParameters());
             } else {
                 throw new SemanticException("built-in types do not currently support any functions");
             }
         } catch (SemanticException sx) {
             exceptionBuffer.add(sx);
         }
     }
 
     /*
      * TODO: duplicates TypeEnforcer.outAConstructorInvocation(AConstructorInvocation)
      */
     @Override
     public void caseAConstructorInvocation(AConstructorInvocation invocation) {
         try {
             Scope scope;
             if (invocation.getTarget() != null) {
                 TypeToken scopeToken = TypeTokenUtil.fromNode(invocation.getTarget());
                 if (scopeToken instanceof UserDefinedTypeToken) {
                     ClassSymbol classSymbol = getScope().resolveClass(((UserDefinedTypeToken) scopeToken).getTypeName());
                     scope = getScope(classSymbol.getDeclaration());
                 } else {
                     throw new SemanticException(invocation, "built-in types do not currently support any constructors");
                 }
             } else {
                 scope = getScope();
             }
             List<TypeToken> parameterTypes = Lists.transform(invocation.getParameters(), Functions.forMap(types));
             ConstructorSymbol symbol = scope.resolveConstructor(parameterTypes);
             evaluateParametrizedInvocation(symbol, invocation.getParameters());
         } catch (SemanticException sx) {
             exceptionBuffer.add(sx);
         }
     }
 
     /*
      * If this is an instance method, the implicit this parameter should be the expressionResult from the invoking
      * context. If this is a constructor, we have to make up our own placeholder.
      */
     private void evaluateParametrizedInvocation(ParametrizedSymbol symbol, List<PExpression> parameters) {
         FunctionPlaceholder thisPlaceholder;
         if ((!symbol.getModifiers().contains(Modifier.STATIC) || symbol instanceof ConstructorSymbol)
                 && symbol.getDeclaringClass() != null) {
             thisPlaceholder = new FunctionPlaceholder(new UserDefinedTypeToken(symbol.getDeclaringClass().getName()));
             if (!(symbol instanceof ConstructorSymbol)) {
                 write("SET PUSH " + lookup(expressionResult, 0));
             } else {
                 throw new NoHeapException();
             }
             stack.push(thisPlaceholder);
         } else {
             thisPlaceholder = null;
         }
 
         assert parameters.size() == symbol.getParameters().size();
         Map<PExpression, FunctionPlaceholder> parameterLocals = Maps.newHashMap();
         for (int i = 0; i < parameters.size(); i++) {
             PExpression parameter = parameters.get(i);
             TypeToken type = symbol.getParameters().get(i).getType();
             int lastWord = types.get(parameter).getWidth() - 1;
             boolean signed = (types.get(parameter) instanceof IntegralTypeToken
                     && ((IntegralTypeToken) types.get(parameter)).isSigned());
 
             FunctionPlaceholder placeholder = new FunctionPlaceholder(type);
             parameterLocals.put(parameter, placeholder);
             inline(parameter);
 
             for (int j = type.getWidth() - 1; j >= 0; j--) {
                 if (types.get(parameter).getWidth() < (j + 1)) {
                     if (signed) {
                         write("SET PUSH " + lookup(expressionResult, lastWord));
                         write("ASR [SP] 16");
                     } else {
                         write("SET PUSH 0x0000");
                     }
                 } else {
                     write("SET PUSH " + lookup(expressionResult, j));
                 }
             }
 
             stack.push(placeholder);
         }
 
         write("JSR " + getStartLabel(symbol.getDeclaration()));
 
         for (PExpression parameter : Lists.reverse(parameters)) {
             reclaimLocal(parameterLocals.get(parameter));
         }
 
         if (thisPlaceholder != null) {
             write("ADD SP 1"); // clear out this
         }
 
         // the function invocation will have already set the relevant registers
         expressionResult = null;
     }
 
     /*
      * TODO: duplicates TypeEnforcer.outAUnqualifiedArrayAccess(AUnqualifiedArrayAccess)
      */
     @Override
     public void caseAUnqualifiedArrayAccess(AUnqualifiedArrayAccess arrayAccess) {
         try {
             try {
                 expressionResult = getScope().resolveLocal(arrayAccess.getArrayName().getText());
                 assert stack.contains(expressionResult);
                 evaluateArrayAccess(arrayAccess, arrayAccess.getIndex());
             } catch (SemanticException sx) {
                 expressionResult = getScope().resolveField(arrayAccess.getArrayName().getText());
                 assert stack.contains(thisSymbol);
                 write("SET A " + lookup(thisSymbol, 0));
                 evaluateArrayAccess(arrayAccess, arrayAccess.getIndex());
             }
         } catch (SemanticException sx) {
             exceptionBuffer.add(sx);
         }
     }
 
     /*
      * TODO: duplicates TypeEnforcer.outAQualifiedArrayAccess(AQualifiedArrayAccess)
      */
     @Override
     public void caseAQualifiedArrayAccess(AQualifiedArrayAccess arrayAccess) {
         try {
             PQualifiedName qualifiedName = arrayAccess.getArrayName();
             TypeToken type;
             String fieldName;
             if (qualifiedName instanceof AExpressionQualifiedName) {
                 AExpressionQualifiedName expressionName = (AExpressionQualifiedName) qualifiedName;
                 PPrimaryExpression expression = expressionName.getTarget();
                 type = types.get(expression);
                 fieldName = expressionName.getName().getText();
                 inline(expression); // so that expressionResult is the object
             } else if (qualifiedName instanceof ATypeTokenQualifiedName) {
                 ATypeTokenQualifiedName typeName = (ATypeTokenQualifiedName) qualifiedName;
                 type = TypeTokenUtil.fromNode(typeName.getTarget());
                 fieldName = typeName.getName().getText();
             } else {
                 throw new SemanticException(qualifiedName, "unknown qualified name flavor");
             }
             if (type instanceof UserDefinedTypeToken) {
                 ClassSymbol classSymbol = getScope().resolveClass(((UserDefinedTypeToken) type).getTypeName());
                 Scope classScope = getScope(classSymbol.getDeclaration());
                 FieldSymbol fieldSymbol = classScope.resolveField(fieldName);
                 write("SET A " + lookup(expressionResult, 0));
                 expressionResult = fieldSymbol;
                 evaluateArrayAccess(arrayAccess, arrayAccess.getIndex());
             } else {
                 throw new SemanticException(arrayAccess, "built-in types do not currently support any fields");
             }
         } catch (SemanticException sx) {
             exceptionBuffer.add(sx);
         }
     }
 
     /*
      * The array should be the expressionResult from the invoking context. Note that the value stored in the array is
      * not actually copied to the registers. This is done in
      * caseAArrayAccessPrimaryExpression(AArrayAccessPrimaryExpression) so that this method may be re-used in evaluating
      * assignments.
      */
     private void evaluateArrayAccess(PArrayAccess arrayAccess, PExpression index) {
         TypeToken elementType = ((ArrayTypeToken) types.get(arrayAccess)).getElementType();
 
         // since we need to evaluate the index expression, push the array
         requireStack(types.get(arrayAccess));
         TypedSymbol array = expressionResult;
 
         inline(index);
         requireValue(types.get(index));
         if (elementType.getWidth() > 1) {
             write("MUL A " + elementType.getWidth());
         }
         write("ADD A " + lookup(array, 0));
         expressionResult = new ArrayElementPlaceholder(elementType);
     }
 
     @Override
     public void caseAExpressionQualifiedName(AExpressionQualifiedName qualifiedName) {
         inline(qualifiedName.getTarget()); // so that the expressionResult is the object
         evaluateQualifiedName(qualifiedName, types.get(qualifiedName.getTarget()), qualifiedName.getName().getText());
     }
 
     @Override
     public void caseATypeTokenQualifiedName(ATypeTokenQualifiedName qualifiedName) {
         if (qualifiedName.getTarget() != null) {
             evaluateQualifiedName(qualifiedName, TypeTokenUtil.fromNode(qualifiedName.getTarget()), qualifiedName.getName().getText());
         } else {
             exceptionBuffer.add(new SemanticException(qualifiedName, "there are no fields in the global scope"));
         }
     }
 
     private void evaluateQualifiedName(PQualifiedName qualifiedName, TypeToken type, String fieldName) {
         try {
             if (type instanceof UserDefinedTypeToken) {
                 ClassSymbol classSymbol = getScope().resolveClass(((UserDefinedTypeToken) type).getTypeName());
                 Scope classScope = getScope(classSymbol.getDeclaration());
                 FieldSymbol symbol = classScope.resolveField(fieldName);
                 write("SET A " + lookup(expressionResult, 0));
                 expressionResult = symbol;
             } else {
                 throw new SemanticException(qualifiedName, "built-in types do not currently support any fields");
             }
         } catch (SemanticException sx) {
             exceptionBuffer.add(sx);
         }
     }
 
     @Override
     public void caseAPrimaryExpression(APrimaryExpression expression) {
         inline(expression.getPrimaryExpression());
     }
 
     @Override
     public void caseANumericNegationExpression(ANumericNegationExpression expression) {
         // handle the edge cases Long.MIN_VALUE, Integer.MIN_VALUE, Short.MIN_VALUE
         if (expression.getValue() instanceof APrimaryExpression) {
             PPrimaryExpression primaryExpression = ((APrimaryExpression) expression.getValue()).getPrimaryExpression();
             if (primaryExpression instanceof ALiteralPrimaryExpression) {
                 PLiteral literal = ((ALiteralPrimaryExpression) primaryExpression).getLiteral();
                 if (literal instanceof AIntegralLiteral) {
                     try {
                         PIntegralLiteral integralLiteral = ((AIntegralLiteral) literal).getIntegralLiteral();
                         BigInteger value = IntegralTypeToken.parseLiteral(integralLiteral);
                         if (value.longValue() == Long.MIN_VALUE) {
                             write("SET A 0x0000");
                             write("SET B 0x0000");
                             write("SET C 0x0000");
                             write("SET X 0x8000");
                             expressionResult = null;
                             return;
                         } else if (value.intValue() == Integer.MIN_VALUE) {
                             write("SET A 0x0000");
                             write("SET B 0x8000");
                             expressionResult = null;
                             return;
                         } else if (value.shortValue() == Short.MIN_VALUE) {
                             write("SET A 0x8000");
                             expressionResult = null;
                             return;
                         }
                     } catch (SemanticException sx) {
                         exceptionBuffer.add(sx);
                     }
                 }
             }
         }
 
         inline(expression.getValue());
         requireValue(types.get(expression.getValue()), types.get(expression)); // could be promoted to signed
         write("XOR A 0xffff");
         write("ADD A 0x0001");
         if (types.get(expression).getWidth() >= 2) {
             write("XOR B 0xffff");
             write("ADD B EX");
         }
         if (types.get(expression).getWidth() >= 4) {
             write("XOR C 0xffff");
             write("ADD C EX");
             write("XOR D 0xffff");
             write("ADD X EX");
         }
         expressionResult = null;
     }
 
     @Override
     public void caseAConditionalNotExpression(AConditionalNotExpression expression) {
         inline(expression.getValue());
         requireValue(types.get(expression));
         write("XOR A 0x0001");
         expressionResult = null;
     }
 
     @Override
     public void caseABitwiseComplementExpression(ABitwiseComplementExpression expression) {
         inline(expression.getValue());
         requireValue(types.get(expression));
         switch (types.get(expression).getWidth()) {
             case 4:
                 write("XOR X 0xffff");
                 write("XOR C 0xffff");
             case 2:
                 write("XOR B 0xffff");
             case 1:
                 write("XOR A 0xffff");
                 break;
             default:
                 assert false; // there shouldn't be any other widths
         }
         expressionResult = null;
     }
 
     /*
      * TODO: arithmetic needs a LOT of unit tests, and I'm sure these methods fail in some (or even most) cases
      */
 
     private void conditionallyNegate(TypeToken type, TypedSymbol symbol, String word, String label) {
         assert symbol == null || type == symbol.getType();
         assert symbol == null || ((IntegralTypeToken) symbol.getType()).isSigned();
         assert symbol == null || (symbol.getType().getWidth() == 2 || symbol.getType().getWidth() == 4);
 
         write(String.format("IFA %s 0xffff", word));
         write(String.format("SET PC " + label));
         for (int i = 0; i < type.getWidth(); i++) {
             write(String.format("XOR %s 0xffff", lookupSafe(symbol, i)));
             if (i == 0) {
                 write(String.format("ADD %s 0x0001", lookupSafe(symbol, i)));
             } else {
                 write(String.format("ADD %s EX", lookupSafe(symbol, i)));
             }
         }
         write(":" + label);
     }
 
     @Override
     public void caseAMultiplyExpression(AMultiplyExpression expression) {
         inline(expression.getLeft());
         requireStack(types.get(expression.getLeft()), types.get(expression));
         TypedSymbol left = expressionResult;
 
         inline(expression.getRight());
         requireValue(types.get(expression.getRight()), types.get(expression));
 
         if (((IntegralTypeToken) types.get(expression)).isSigned()) {
             if (types.get(expression).getWidth() == 1) {
                 write("MLI A " + lookup(left, 0));
             } else {
                 int lastWord = types.get(expression).getWidth() - 1;
                 write("SET Z " + lookupSafe(left, lastWord));
                 write("XOR Z " + lookupSafe(expressionResult, lastWord));
                 conditionallyNegate(types.get(expression), left, lookupSafe(left, lastWord), "negate_left_" + getBaseLabel(expression));
                 conditionallyNegate(types.get(expression), expressionResult, lookupSafe(expressionResult, lastWord), "negate_right_" + getBaseLabel(expression));
                 write("MUL A " + lookup(left, 0));
             }
         } else {
             write("MUL A " + lookup(left, 0));
         }
         if (types.get(expression).getWidth() >= 2) {
             write("SET Y EX");
             write("MUL B " + lookup(left, 1));
             write("ADD B Y");
         }
         if (types.get(expression).getWidth() >= 4) {
             write("SET Y EX");
             write("MUL C " + lookup(left, 2));
             write("ADD C Y");
             write("SET Y EX");
             write("MUL X " + lookup(left, 3));
             write("ADD X Y");
         }
         expressionResult = null;
         if (((IntegralTypeToken) types.get(expression)).isSigned() && types.get(expression).getWidth() > 1) {
             conditionallyNegate(types.get(expression), expressionResult, "Z", "negate_result_" + getBaseLabel(expression));
         }
         if (left instanceof TransientPlaceholder) {
             doReclaimLocal(left);
         }
     }
 
     @Override
     public void caseADivideExpression(ADivideExpression expression) {
         inline(expression.getLeft());
         requireStack(types.get(expression.getLeft()), types.get(expression));
         TypedSymbol left = expressionResult;
 
         inline(expression.getRight());
         requireValue(types.get(expression.getRight()), types.get(expression));
 
         String instruction = ((IntegralTypeToken) types.get(expression)).isSigned() ? "DVI" : "DIV";
         write(String.format("%s A %s", instruction, lookup(left, 0)));
         if (types.get(expression).getWidth() >= 2) {
             write("ADD B EX");
             write(String.format("%s B %s", instruction, lookup(left, 1)));
         }
         if (types.get(expression).getWidth() >= 4) {
             write("ADD C EX");
             write(String.format("%s C %s", instruction, lookup(left, 2)));
             write("ADD X EX");
             write(String.format("%s X %s", instruction, lookup(left, 3)));
         }
         expressionResult = null;
     }
 
     @Override
     public void caseAModulusExpression(AModulusExpression expression) {
         inline(expression.getLeft());
         requireStack(types.get(expression.getLeft()), types.get(expression));
         TypedSymbol left = expressionResult;
 
         inline(expression.getRight());
         requireValue(types.get(expression.getRight()), types.get(expression));
 
         String instruction = ((IntegralTypeToken) types.get(expression)).isSigned() ? "MDI" : "MOD";
         write(String.format("%s A %s", instruction, lookup(left, 0)));
         if (types.get(expression).getWidth() >= 2) {
             write("ADD B EX");
             write(String.format("%s B %s", instruction, lookup(left, 1)));
         }
         if (types.get(expression).getWidth() >= 4) {
             write("ADD C EX");
             write(String.format("%s C %s", instruction, lookup(left, 2)));
             write("ADD X EX");
             write(String.format("%s X %s", instruction, lookup(left, 3)));
         }
         expressionResult = null;
     }
 
     @Override
     public void caseAAddExpression(AAddExpression expression) {
         inline(expression.getLeft());
         requireStack(types.get(expression.getLeft()), types.get(expression));
         TypedSymbol left = expressionResult;
 
         inline(expression.getRight());
         requireValue(types.get(expression.getRight()), types.get(expression));
 
         write("ADD A " + lookup(left, 0));
         if (types.get(expression).getWidth() >= 2) {
             write("ADX B " + lookup(left, 1));
         }
         if (types.get(expression).getWidth() >= 4) {
             write("ADX C " + lookup(left, 2));
             write("ADX D " + lookup(left, 3));
         }
         expressionResult = null;
     }
 
     @Override
     public void caseASubtractExpression(ASubtractExpression expression) {
         inline(expression.getLeft());
         requireStack(types.get(expression.getLeft()), types.get(expression));
         TypedSymbol left = expressionResult;
 
         inline(expression.getRight());
         requireValue(types.get(expression.getRight()), types.get(expression));
         // subtraction isn't commutative, so put our right value in some spare registers
         write("SET Y A");
         if (types.get(expression).getWidth() >= 2) {
             write("SET Z B");
         }
         if (types.get(expression).getWidth() >= 4) {
             write("SET I C");
             write("SET J X");
         }
 
         // now put the stack stuff back in the registers
         write("SET A " + lookup(left, 0));
         if (types.get(expression.getLeft()).getWidth() >= 2) {
             write("SET B " + lookup(left, 1));
         }
         if (types.get(expression.getLeft()).getWidth() >= 4) {
             write("SET C " + lookup(left, 2));
             write("SET X " + lookup(left, 3));
         }
 
         write("SUB A Y");
         if (types.get(expression).getWidth() >= 2) {
             write("SBX B Z");
         }
         if (types.get(expression).getWidth() >= 4) {
             write("SBX C I");
             write("SBX D J");
         }
         expressionResult = null;
     }
 
     @Override
     public void caseAShiftLeftExpression(AShiftLeftExpression expression) {
         inline(expression.getLeft());
         requireStack(types.get(expression));
         TypedSymbol left = expressionResult;
 
         inline(expression.getRight());
         requireValue(types.get(expression.getRight()));
         // this is awkward, but put the right operand in register Y
         // we can discard any other words because they don't matter anyway
         write("SET Y A");
 
         // now put the stack stuff back in the registers
         write("SET A " + lookup(left, 0));
         if (types.get(expression.getLeft()).getWidth() >= 2) {
             write("SET B " + lookup(left, 1));
         }
         if (types.get(expression.getLeft()).getWidth() >= 4) {
             write("SET C " + lookup(left, 2));
             write("SET X " + lookup(left, 3));
         }
 
         // start on the high order words, so we don't have to waste instructions on storing EX in Z
         if (types.get(expression).getWidth() >= 4) {
             write("SHL X Y");
             write("SHL C Y");
             write("AND X EX");
             write("SHL B Y");
             write("AND C EX");
             write("SHL A Y");
             write("AND B EX");
         } else if (types.get(expression).getWidth() >= 2) {
             write("SHL B Y");
             write("SHL A Y");
             write("AND B EX");
         } else {
             write("SHL A Y");
         }
         expressionResult = null;
     }
 
     @Override
     public void caseAShiftRightExpression(AShiftRightExpression expression) {
         inline(expression.getLeft());
         requireStack(types.get(expression));
         TypedSymbol left = expressionResult;
 
         inline(expression.getRight());
         requireValue(types.get(expression.getRight()));
         // this is awkward, but put the right operand in register Y
         // we can discard any other words because they don't matter anyway
         write("SET Y A");
 
         // now put the stack stuff back in the registers
         write("SET A " + lookup(left, 0));
         if (types.get(expression.getLeft()).getWidth() >= 2) {
             write("SET B " + lookup(left, 1));
         }
         if (types.get(expression.getLeft()).getWidth() >= 4) {
             write("SET C " + lookup(left, 2));
             write("SET X " + lookup(left, 3));
         }
 
         write("ASR A Y");
         if (types.get(expression).getWidth() >= 2) {
             write("ASR B Y");
             write("AND A EX");
         }
         if (types.get(expression).getWidth() >= 4) {
             write("ASR C Y");
             write("AND B EX");
             write("ASR D Y");
             write("AND C EX");
         }
         expressionResult = null;
     }
 
     @Override
     public void caseAUnsignedShiftRightExpression(AUnsignedShiftRightExpression expression) {
         inline(expression.getLeft());
         requireStack(types.get(expression));
         TypedSymbol left = expressionResult;
 
         inline(expression.getRight());
         requireValue(types.get(expression.getRight()));
         // this is awkward, but put the right operand in register Y
         // we can discard any other words because they don't matter anyway
         write("SET Y A");
 
         // now put the stack stuff back in the registers
         write("SET A " + lookup(left, 0));
         if (types.get(expression.getLeft()).getWidth() >= 2) {
             write("SET B " + lookup(left, 1));
         }
         if (types.get(expression.getLeft()).getWidth() >= 4) {
             write("SET C " + lookup(left, 2));
             write("SET X " + lookup(left, 3));
         }
 
         write("SHR A Y");
         if (types.get(expression).getWidth() >= 2) {
             write("SHR B Y");
             write("AND A EX");
         }
         if (types.get(expression).getWidth() >= 4) {
             write("SHR C Y");
             write("AND B EX");
             write("SHR D Y");
             write("AND C EX");
         }
         expressionResult = null;
     }
 
     @Override
     public void caseALessThanExpression(ALessThanExpression expression) {
         TypeToken promotedType;
         try {
             promotedType = types.get(expression.getLeft()).performBinaryOperation(types.get(expression.getRight()));
         } catch (SemanticException sx) {
             exceptionBuffer.add(sx);
             throw new AssertionError();
         }
 
         inline(expression.getLeft());
         requireStack(types.get(expression.getLeft()), promotedType);
         TypedSymbol left = expressionResult;
 
         inline(expression.getRight());
         requireValue(types.get(expression.getRight()), promotedType);
 
         String instruction = ((IntegralTypeToken) promotedType).isSigned() ? "IFU" : "IFL";
         switch (promotedType.getWidth()) {
             case 4:
                 write(String.format("%s %s X", instruction, lookup(left, 3)));
                 write("SET PC true_" + getBaseLabel(expression));
                 write(String.format("%s %s C", instruction, lookup(left, 2)));
                 write("SET PC true_" + getBaseLabel(expression));
             case 2:
                 write(String.format("%s %s B", instruction, lookup(left, 1)));
                 write("SET PC true_" + getBaseLabel(expression));
             case 1:
                 write(String.format("%s %s A", instruction, lookup(left, 0)));
                 write("SET PC true_" + getBaseLabel(expression));
                 break;
             default:
                 assert false;
         }
         write("SET A 0x0000");
         write("SET PC reclaim_" + getBaseLabel(expression));
         write(":true_" + getBaseLabel(expression));
         write("SET A 0x0001");
         write(":reclaim_" + getBaseLabel(expression));
         if (left instanceof TransientPlaceholder) {
             reclaimLocal(left);
         }
         expressionResult = null;
     }
 
     @Override
     public void caseAGreaterThanExpression(AGreaterThanExpression expression) {
         TypeToken promotedType;
         try {
             promotedType = types.get(expression.getLeft()).performBinaryOperation(types.get(expression.getRight()));
         } catch (SemanticException sx) {
             exceptionBuffer.add(sx);
             throw new AssertionError();
         }
 
         inline(expression.getLeft());
         requireStack(types.get(expression.getLeft()), promotedType);
         TypedSymbol left = expressionResult;
 
         inline(expression.getRight());
         requireValue(types.get(expression.getRight()), promotedType);
 
         String instruction = ((IntegralTypeToken) promotedType).isSigned() ? "IFA" : "IFG";
         switch (promotedType.getWidth()) {
             case 4:
                 write(String.format("%s %s X", instruction, lookup(left, 3)));
                 write("SET PC true_" + getBaseLabel(expression));
                 write(String.format("%s %s C", instruction, lookup(left, 2)));
                 write("SET PC true_" + getBaseLabel(expression));
             case 2:
                 write(String.format("%s %s B", instruction, lookup(left, 1)));
                 write("SET PC true_" + getBaseLabel(expression));
             case 1:
                 write(String.format("%s %s A", instruction, lookup(left, 0)));
                 write("SET PC true_" + getBaseLabel(expression));
                 break;
             default:
                 assert false;
         }
         write("SET A 0x0000");
         write("SET PC reclaim_" + getBaseLabel(expression));
         write(":true_" + getBaseLabel(expression));
         write("SET A 0x0001");
         write(":reclaim_" + getBaseLabel(expression));
         if (left instanceof TransientPlaceholder) {
             reclaimLocal(left);
         }
         expressionResult = null;
     }
 
     @Override
     public void caseALessOrEqualExpression(ALessOrEqualExpression expression) {
         TypeToken promotedType;
         try {
             promotedType = types.get(expression.getLeft()).performBinaryOperation(types.get(expression.getRight()));
         } catch (SemanticException sx) {
             exceptionBuffer.add(sx);
             throw new AssertionError();
         }
 
         inline(expression.getLeft());
         requireStack(types.get(expression.getLeft()), promotedType);
         TypedSymbol left = expressionResult;
 
         inline(expression.getRight());
         requireValue(types.get(expression.getRight()), promotedType);
 
         String instruction = ((IntegralTypeToken) promotedType).isSigned() ? "IFA" : "IFG";
         switch (promotedType.getWidth()) {
             case 4:
                 write(String.format("%s %s X", instruction, lookup(left, 3)));
                 write("SET PC false_" + getBaseLabel(expression));
                 write(String.format("%s %s C", instruction, lookup(left, 2)));
                 write("SET PC false_" + getBaseLabel(expression));
             case 2:
                 write(String.format("%s %s B", instruction, lookup(left, 1)));
                 write("SET PC false_" + getBaseLabel(expression));
             case 1:
                 write(String.format("%s %s A", instruction, lookup(left, 0)));
                 write("SET PC false_" + getBaseLabel(expression));
                 break;
             default:
                 assert false;
         }
         write("SET A 0x0001");
         write("SET PC reclaim_" + getBaseLabel(expression));
         write(":false_" + getBaseLabel(expression));
         write("SET A 0x0000");
         write(":reclaim_" + getBaseLabel(expression));
         if (left instanceof TransientPlaceholder) {
             reclaimLocal(left);
         }
         expressionResult = null;
     }
 
     @Override
     public void caseAGreaterOrEqualExpression(AGreaterOrEqualExpression expression) {
         TypeToken promotedType;
         try {
             promotedType = types.get(expression.getLeft()).performBinaryOperation(types.get(expression.getRight()));
         } catch (SemanticException sx) {
             exceptionBuffer.add(sx);
             throw new AssertionError();
         }
 
         inline(expression.getLeft());
         requireStack(types.get(expression.getLeft()), promotedType);
         TypedSymbol left = expressionResult;
 
         inline(expression.getRight());
         requireValue(types.get(expression.getRight()), promotedType);
 
         String instruction = ((IntegralTypeToken) promotedType).isSigned() ? "IFU" : "IFL";
         switch (promotedType.getWidth()) {
             case 4:
                 write(String.format("%s %s X", instruction, lookup(left, 3)));
                 write("SET PC false_" + getBaseLabel(expression));
                 write(String.format("%s %s C", instruction, lookup(left, 2)));
                 write("SET PC false_" + getBaseLabel(expression));
             case 2:
                 write(String.format("%s %s B", instruction, lookup(left, 1)));
                 write("SET PC false_" + getBaseLabel(expression));
             case 1:
                 write(String.format("%s %s A", instruction, lookup(left, 0)));
                 write("SET PC false_" + getBaseLabel(expression));
                 break;
             default:
                 assert false;
         }
         write("SET A 0x0001");
         write("SET PC reclaim_" + getBaseLabel(expression));
         write(":false_" + getBaseLabel(expression));
         write("SET A 0x0000");
         write(":reclaim_" + getBaseLabel(expression));
         if (left instanceof TransientPlaceholder) {
             reclaimLocal(left);
         }
         expressionResult = null;
     }
 
     @Override
     public void caseAEqualExpression(AEqualExpression expression) {
         TypeToken promotedType;
         try {
             promotedType = types.get(expression.getLeft()).performBinaryOperation(types.get(expression.getRight()));
         } catch (SemanticException sx) {
             exceptionBuffer.add(sx);
             throw new AssertionError();
         }
 
         inline(expression.getLeft());
         requireStack(types.get(expression.getLeft()), promotedType);
         TypedSymbol left = expressionResult;
 
         inline(expression.getRight());
         requireValue(types.get(expression.getRight()), promotedType);
 
         write("IFE A " + lookup(left, 0));
         if (promotedType.getWidth() >= 2) {
             write("IFE B " + lookup(left, 1));
         }
         if (promotedType.getWidth() >= 4) {
             write("IFE C " + lookup(left, 2));
             write("IFE X " + lookup(left, 3));
         }
         write("SET PC true_" + getBaseLabel(expression));
         write("SET A 0x0000");
         write("SET PC " + getEndLabel(expression));
         write(":true_" + getBaseLabel(expression));
         write("SET A 0x0001");
         expressionResult = null;
     }
 
     @Override
     public void caseANotEqualExpression(ANotEqualExpression expression) {
         TypeToken promotedType;
         try {
             promotedType = types.get(expression.getLeft()).performBinaryOperation(types.get(expression.getRight()));
         } catch (SemanticException sx) {
             exceptionBuffer.add(sx);
             throw new AssertionError();
         }
 
         inline(expression.getLeft());
         requireStack(types.get(expression.getLeft()), promotedType);
         TypedSymbol left = expressionResult;
 
         inline(expression.getRight());
         requireValue(types.get(expression.getRight()), promotedType);
 
         write("IFE A " + lookup(left, 0));
         if (promotedType.getWidth() >= 2) {
             write("IFE B " + lookup(left, 1));
         }
         if (promotedType.getWidth() >= 4) {
             write("IFE C " + lookup(left, 2));
             write("IFE X " + lookup(left, 3));
         }
         write("SET PC false_" + getBaseLabel(expression));
         write("SET A 0x0001");
         write("SET PC " + getEndLabel(expression));
         write(":false_" + getBaseLabel(expression));
         write("SET A 0x0000");
         expressionResult = null;
     }
 
     @Override
     public void caseABitwiseAndExpression(ABitwiseAndExpression expression) {
         inline(expression.getLeft());
         requireStack(types.get(expression.getLeft()), types.get(expression));
         TypedSymbol left = expressionResult;
 
         inline(expression.getRight());
         requireValue(types.get(expression.getRight()), types.get(expression));
 
         write("AND A " + lookup(left, 0));
         if (types.get(expression).getWidth() >= 2) {
             write("AND B " + lookup(left, 1));
         }
         if (types.get(expression).getWidth() >= 4) {
             write("AND C " + lookup(left, 2));
             write("AND X " + lookup(left, 3));
         }
         expressionResult = null;
     }
 
     @Override
     public void caseABitwiseXorExpression(ABitwiseXorExpression expression) {
         inline(expression.getLeft());
         requireStack(types.get(expression.getLeft()), types.get(expression));
         TypedSymbol left = expressionResult;
 
         inline(expression.getRight());
         requireValue(types.get(expression.getRight()), types.get(expression));
 
         write("XOR A " + lookup(left, 0));
         if (types.get(expression).getWidth() >= 2) {
             write("XOR B " + lookup(left, 1));
         }
         if (types.get(expression).getWidth() >= 4) {
             write("XOR C " + lookup(left, 2));
             write("XOR X " + lookup(left, 3));
         }
         expressionResult = null;
     }
 
     @Override
     public void caseABitwiseOrExpression(ABitwiseOrExpression expression) {
         inline(expression.getLeft());
         requireStack(types.get(expression.getLeft()), types.get(expression));
         TypedSymbol left = expressionResult;
 
         inline(expression.getRight());
         requireValue(types.get(expression.getRight()), types.get(expression));
 
         write("BOR A " + lookup(left, 0));
         if (types.get(expression).getWidth() >= 2) {
             write("BOR B " + lookup(left, 1));
         }
         if (types.get(expression).getWidth() >= 4) {
             write("BOR C " + lookup(left, 2));
             write("BOR X " + lookup(left, 3));
         }
         expressionResult = null;
     }
 
     @Override
     public void caseAConditionalAndExpression(AConditionalAndExpression expression) {
         inline(expression.getLeft());
         write(String.format("IFE %s 0x0000", lookup(expressionResult, 0)));
         write("SET PC false_" + getBaseLabel(expression));
 
         inline(expression.getRight());
         write(String.format("IFE %s 0x0000", lookup(expressionResult, 0)));
         write("SET PC false_" + getBaseLabel(expression));
 
         write("SET A 0x0001");
         write("SET PC " + getEndLabel(expression));
         write(":false_" + getBaseLabel(expression));
         write("SET A 0x0000");
         expressionResult = null;
     }
 
     @Override
     public void caseAConditionalOrExpression(AConditionalOrExpression expression) {
         inline(expression.getLeft());
         write(String.format("IFE %s 0x0001", lookup(expressionResult, 0)));
         write("SET PC true_" + getBaseLabel(expression));
 
         inline(expression.getRight());
         write(String.format("IFE %s 0x0001", lookup(expressionResult, 0)));
         write("SET PC true_" + getBaseLabel(expression));
 
         write("SET A 0x0000");
         write("SET PC " + getEndLabel(expression));
         write(":true_" + getBaseLabel(expression));
         write("SET A 0x0001");
         expressionResult = null;
     }
 
     @Override
     public void caseAConditionalExpression(AConditionalExpression expression) {
         inline(expression.getCondition());
         write(String.format("IFE %s 0x0000", lookup(expressionResult, 0)));
         write("SET PC true_" + getBaseLabel(expression));
 
         inline(expression.getIfFalse());
         write("SET PC " + getEndLabel(expression));
 
         inline(expression.getIfTrue());
     }
 
     @Override
     public void caseAAssignmentExpression(AAssignmentExpression expression) {
         inline(expression.getAssignment());
     }
 
     @Override
     public void caseAAssignment(AAssignment assignment) {
         inline(assignment.getTarget());
         assert expressionResult != null;
         assert !(expressionResult instanceof Placeholder) || expressionResult instanceof ArrayElementPlaceholder;
         TypedSymbol targetSymbol = expressionResult;
 
         inline(assignment.getValue());
         for (int i = 0; i < types.get(assignment.getTarget()).getWidth(); i++) {
             if (types.get(assignment.getValue()).getWidth() <= i) {
                 if (types.get(assignment.getValue()) instanceof IntegralTypeToken
                         && ((IntegralTypeToken) types.get(assignment.getValue())).isSigned()) {
                     // do sign extension if we're promoting the value from a signed type
                     int lastWord = types.get(assignment.getValue()).getWidth() - 1;
                     write(String.format("SET %s %s", lookup(targetSymbol, i), lookup(expressionResult, lastWord)));
                     write(String.format("ASR %s 16", lookup(targetSymbol, i)));
                 } else {
                     write(String.format("SET %s 0x0000", lookup(targetSymbol, i)));
                 }
             } else {
                 write(String.format("SET %s %s", lookup(targetSymbol, i), lookup(expressionResult, i)));
             }
         }
         expressionResult = targetSymbol;
     }
 
     @Override
     public void caseALocalDeclarationAssignmentTarget(ALocalDeclarationAssignmentTarget assignmentTarget) {
         inline(assignmentTarget.getLocalDeclaration());
         write(String.format("SUB SP 0x%04x", expressionResult.getType().getWidth()));
     }
 
     @Override
     public void caseAIdentifierAssignmentTarget(AIdentifierAssignmentTarget assignmentTarget) {
         enforceIdentifier(assignmentTarget.getIdentifier());
     }
 
     @Override
     public void caseAQualifiedNameAssignmentTarget(AQualifiedNameAssignmentTarget assignmentTarget) {
         inline(assignmentTarget.getQualifiedName());
     }
 
     @Override
     public void caseAArrayAccessAssignmentTarget(AArrayAccessAssignmentTarget assignmentTarget) {
         inline(assignmentTarget.getArrayAccess());
     }
 
     private void enforceIdentifier(TIdentifier identifier) {
         try {
             try {
                 expressionResult = getScope().resolveLocal(identifier.getText());
                 assert stack.contains(expressionResult);
             } catch (SemanticException sx) {
                 expressionResult = getScope().resolveField(identifier.getText());
                 assert stack.contains(thisSymbol);
                 write("SET A " + lookup(thisSymbol, 0));
             }
         } catch (SemanticException sx) {
             exceptionBuffer.add(sx);
         }
     }
 }
