 /*
  * Copyright 2011 Jon S Akhtar (Sylvanaar)
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 package com.sylvanaar.idea.Lua.lang.psi.controlFlow.impl;
 
 import com.intellij.openapi.diagnostic.*;
 import com.intellij.openapi.project.*;
 import com.intellij.openapi.util.*;
 import com.intellij.psi.*;
 import com.intellij.psi.util.*;
 import com.sylvanaar.idea.Lua.lang.parser.*;
 import com.sylvanaar.idea.Lua.lang.psi.*;
 import com.sylvanaar.idea.Lua.lang.psi.controlFlow.*;
 import com.sylvanaar.idea.Lua.lang.psi.expressions.*;
 import com.sylvanaar.idea.Lua.lang.psi.lists.*;
 import com.sylvanaar.idea.Lua.lang.psi.statements.*;
 import com.sylvanaar.idea.Lua.lang.psi.symbols.*;
 import com.sylvanaar.idea.Lua.lang.psi.util.*;
 import com.sylvanaar.idea.Lua.lang.psi.visitor.*;
 import org.jetbrains.annotations.*;
 
 import java.util.*;
 
 
 /**
  * @author ven
  */
 public class ControlFlowBuilder extends LuaRecursiveElementVisitor {
     private static final Logger log = Logger.getInstance("Lua.ControlFlowBuilder");
 
     private List<InstructionImpl> myInstructions;
 
     private static List<LuaPsiElement> ourProcessingScopes =
             Collections.synchronizedList(new ArrayList<LuaPsiElement>());
 
     private static class CantAnalyzeException extends RuntimeException {
     }
 
     private Stack<InstructionImpl> myProcessingStack;
 //  private final ConstantExpressionEvaluator myConstantEvaluator;
 
     public ControlFlowBuilder(Project project) {
 //        myConstantEvaluator =  LanguageConstantExpressionEvaluator.INSTANCE.forLanguage(LuaFileType.LUA_LANGUAGE);
     }
 
 
     private InstructionImpl myHead;
     private boolean         myNegate;
     private boolean         myAssertionsOnly;
 
     private List<Pair<InstructionImpl, LuaPsiElement>> myPending;
 
     private int myInstructionNumber;
 
     public void visitBlock(LuaBlock block) {
         super.visitBlock(block);
     }
 
     @Override
     public void visitFunctionDef(LuaFunctionDefinitionStatement e) {
         InstructionImpl funcInstruction = startNode(e);
         final LuaParameter[] parameters = e.getParameters().getLuaParameters();
         for (LuaParameter parameter : parameters) {
             addNode(new ReadWriteVariableInstructionImpl(parameter, myInstructionNumber++, true));
         }
 
         addPendingEdge(e, myHead);
         LuaBlock body = e.getBlock();
         if (body != null) {
             final InstructionImpl instruction = startNode(body);
             body.accept(this);
             finishNode(instruction);
         }
 
 
         finishNode(funcInstruction);
     }
 
     public Instruction[] buildControlFlow(LuaPsiElement scope) {
         myInstructions = new ArrayList<InstructionImpl>();
         myProcessingStack = new Stack<InstructionImpl>();
         myPending = new ArrayList<Pair<InstructionImpl, LuaPsiElement>>();
         myInstructionNumber = 0;
 
         log.debug("Scope: " + scope + " parent: " + scope.getParent());
 
         startNode("START");
 
 
         try {
             scope.accept(this);
         } catch (CantAnalyzeException e) {
             log.debug("CANT ANALYIZE");
             return null;
         }
 
 
         final InstructionImpl end = startNode("END");
 
         checkPending(end); //collect return edges
 
         for (Instruction i : myInstructions)
             log.debug(i.toString());
 
         return myInstructions.toArray(new Instruction[myInstructions.size()]);
     }
 
     private void addNode(InstructionImpl instruction) {
         myInstructions.add(instruction);
         if (myHead != null) {
             addEdge(myHead, instruction);
         }
         myHead = instruction;
     }
 
     static void addEdge(InstructionImpl beg, InstructionImpl end) {
         if (!beg.mySucc.contains(end)) {
             beg.mySucc.add(end);
         }
 
         if (!end.myPred.contains(beg)) {
             end.myPred.add(beg);
         }
     }
 
     @Override
     public void visitFile(PsiFile file) {
         visitBlock((LuaBlock) file);
     }
 
     @Override
     public void visitDoStatement(LuaDoStatement e) {
         final LuaBlock body = e.getBlock();
         if (body != null) {
             final InstructionImpl instruction = startNode(body);
             body.accept(this);
             finishNode(instruction);
         }
     }
 
     @Override
     public void visitErrorElement(PsiErrorElement element) {
         throw new CantAnalyzeException();
     }
 
     public void visitBreakStatement(LuaBreakStatement breakStatement) {
         interruptFlow();
     }
 
 
     public void visitReturnStatement(LuaReturnStatement returnStatement) {
         boolean isNodeNeeded = myHead == null || myHead.getElement() != returnStatement;
         final LuaExpression value = returnStatement.getReturnValue();
         if (value != null) value.accept(this);
 
         if (isNodeNeeded) {
             InstructionImpl retInsn = startNode(returnStatement);
             addPendingEdge(null, myHead);
             finishNode(retInsn);
         } else {
             addPendingEdge(null, myHead);
         }
         interruptFlow();
     }
 
     private void interruptFlow() {
         myHead = null;
     }
 
     @Override
     public void visitAssignment(LuaAssignmentStatement e) {
 //        super.visitAssignment(e);
 
         LuaIdentifierList lValues = e.getLeftExprs();
         LuaExpressionList rValues = e.getRightExprs();
         if (rValues != null) rValues.accept(this);
         if (lValues != null) lValues.accept(this);
     }
 
     @Override
     public void visitParenthesizedExpression(LuaParenthesizedExpression expression) {
         final LuaExpression operand = expression.getOperand();
         if (operand != null) operand.accept(this);
     }
 
     @Override
     public void visitUnaryExpression(LuaUnaryExpression expression) {
         final LuaExpression operand = expression.getOperand();
         if (operand != null) {
             final boolean negation = expression.getOperationTokenType() == LuaElementTypes.NOT;
             if (negation) {
                 myNegate = !myNegate;
             }
             operand.accept(this);
             if (negation) {
                 myNegate = !myNegate;
             }
         }
     }
 
     @Override
     public void visitReferenceElement(LuaReferenceElement e) {
         super.visitReferenceElement(e);
         buildIdentifierUsage(e);
     }
 
     private void buildIdentifierUsage(LuaReferenceElement id) {
         final ReadWriteVariableInstructionImpl i = new ReadWriteVariableInstructionImpl(id, myInstructionNumber++,
                 !myAssertionsOnly && LuaPsiUtils.isLValue(id));
 
         if (PsiUtil.hasErrorElementChild(id)) return;
         addNode(i);
         checkPending(i);
     }
 
     public void visitIfThenStatement(LuaIfThenStatement ifStatement) {
         InstructionImpl ifInstruction = startNode(ifStatement);
         final LuaExpression condition = ifStatement.getIfCondition();
 
         final InstructionImpl head = myHead;
         final LuaBlock thenBranch = ifStatement.getIfBlock();
         InstructionImpl thenEnd = null;
         if (thenBranch != null) {
             if (condition != null) {
                 condition.accept(this);
             }
             thenBranch.accept(this);
             thenEnd = myHead;
         }
 
 
         LuaBlock[] elseIfBlocks = ifStatement.getElseIfBlocks();
         InstructionImpl[] elifEnd = new InstructionImpl[elseIfBlocks.length];
         for (int i = 0, elseIfBlocksLength = elseIfBlocks.length; i < elseIfBlocksLength; i++) {
             LuaBlock block = elseIfBlocks[i];
             LuaExpression elifcondition = ifStatement.getElseIfConditions()[i];
 
             elifcondition.accept(this);
             block.accept(this);
             elifEnd[i] = myHead;
         }
 
         
         
         
         myHead = head;
         final LuaBlock elseBranch = ifStatement.getElseBlock();
         InstructionImpl elseEnd = null;
         if (elseBranch != null) {
             if (condition != null) {
                 myNegate = !myNegate;
                 final boolean old = myAssertionsOnly;
                 myAssertionsOnly = true;
                 condition.accept(this);
                 myNegate = !myNegate;
                 myAssertionsOnly = old;
             }
 
             elseBranch.accept(this);
             elseEnd = myHead;
         }
 
 
         if (thenBranch != null || elseBranch != null) {
             final InstructionImpl end = new IfEndInstruction(ifStatement, myInstructionNumber++);
             addNode(end);
             if (thenEnd != null) addEdge(thenEnd, end);


                for (InstructionImpl instruction : elifEnd) {
                    if (instruction != null)
                        addEdge(instruction, end);
                }
 
             if (elseEnd != null) addEdge(elseEnd, end);
         }
         finishNode(ifInstruction);
     }
 
 
     @Override
     public void visitNumericForStatement(LuaNumericForStatement e) {
         final LuaExpression index = e.getIndex();
         final LuaExpression start = e.getStart();
         final LuaExpression end = e.getEnd();
         final LuaExpression step = e.getStep();
 
         if (index != null)
             index.accept(this);
         if (start != null)
             start.accept(this);
         if (end != null)
             end.accept(this);
         if (step != null)
             step.accept(this);
 
         InstructionImpl forLoop = startNode(e);
         addPendingEdge(e, myHead); //break cycle
         final LuaBlock body = e.getBlock();
         if (body != null) {
             final InstructionImpl instruction = startNode(body);
             body.accept(this);
             finishNode(instruction);
         }
         checkPending(forLoop);
 
         if (myHead != null) addEdge(myHead, forLoop);  //loop
         interruptFlow();
 
         finishNode(forLoop);
     }
 
     @Override
     public void visitGenericForStatement(LuaGenericForStatement e) {
         final LuaExpression[] indicies = e.getIndices();
         final LuaExpression inclause = e.getInClause();
 
         for (LuaExpression variable : indicies) {
             ReadWriteVariableInstructionImpl writeInsn =
                     new ReadWriteVariableInstructionImpl((LuaSymbol) variable, myInstructionNumber++, true);
             checkPending(writeInsn);
             addNode(writeInsn);
             variable.accept(this);
         }
 
         InstructionImpl forLoop = startNode(e);
         addPendingEdge(e, myHead); //break cycle
         if (inclause != null)
             inclause.accept(this);
 
         final LuaBlock body = e.getBlock();
         if (body != null) {
             final InstructionImpl instruction = startNode(body);
             body.accept(this);
             finishNode(instruction);
         }
         checkPending(forLoop);
 
         if (myHead != null) addEdge(myHead, forLoop);  //loop
         interruptFlow();
 
         finishNode(forLoop);
     }
 
     private void checkPending(InstructionImpl instruction) {
         final PsiElement element = instruction.getElement();
         if (element == null) {
             //add all
             for (Pair<InstructionImpl, LuaPsiElement> pair : myPending) {
                 addEdge(pair.getFirst(), instruction);
             }
             myPending.clear();
         } else {
             for (int i = myPending.size() - 1; i >= 0; i--) {
                 final Pair<InstructionImpl, LuaPsiElement> pair = myPending.get(i);
                 final PsiElement scopeWhenToAdd = pair.getSecond();
                 if (scopeWhenToAdd == null) continue;
                 if (!PsiTreeUtil.isAncestor(scopeWhenToAdd, element, false)) {
                     addEdge(pair.getFirst(), instruction);
                     myPending.remove(i);
                 } else {
                     break;
                 }
             }
         }
     }
 
     //add edge when instruction.getElement() is not contained in scopeWhenAdded
     private void addPendingEdge(LuaPsiElement scopeWhenAdded, InstructionImpl instruction) {
         if (instruction == null) return;
 
         int i = 0;
         if (scopeWhenAdded != null) {
             for (; i < myPending.size(); i++) {
                 Pair<InstructionImpl, LuaPsiElement> pair = myPending.get(i);
 
                 final LuaPsiElement currScope = pair.getSecond();
                 if (currScope == null) continue;
                 if (!PsiTreeUtil.isAncestor(currScope, scopeWhenAdded, true)) break;
             }
         }
         myPending.add(i, new Pair<InstructionImpl, LuaPsiElement>(instruction, scopeWhenAdded));
     }
 
     public void visitWhileStatement(LuaWhileStatement whileStatement) {
         final InstructionImpl instruction = startNode(whileStatement);
         final LuaConditionalExpression condition = whileStatement.getCondition();
         if (condition != null) {
             condition.accept(this);
         }
         if (!alwaysTrue(condition)) {
             addPendingEdge(whileStatement, myHead); //break
         }
 
         if (!alwaysFalse(condition)) {
             final LuaBlock body = whileStatement.getBlock();
             if (body != null) {
                 body.accept(this);
             }
         }
         checkPending(instruction); //check for breaks targeted here
         if (myHead != null) addEdge(myHead, instruction); //loop
         interruptFlow();
         finishNode(instruction);
     }
 
     private boolean alwaysTrue(LuaConditionalExpression condition) {
         return Boolean.TRUE.equals(condition.evaluate());
     }
 
     private boolean alwaysFalse(LuaConditionalExpression condition) {
         return Boolean.FALSE.equals(condition.evaluate());
     }
 
 
     private InstructionImpl startNode(@Nullable LuaPsiElement element) {
         return startNode(element, true);
     }
 
     private InstructionImpl startNode(LuaPsiElement element, boolean checkPending) {
         return startNodeImpl(checkPending, new InstructionImpl(element, myInstructionNumber++));
     }
 
     private InstructionImpl startNodeImpl(boolean checkPending, InstructionImpl instruction) {
         addNode(instruction);
         if (checkPending) checkPending(instruction);
         return myProcessingStack.push(instruction);
     }
 
     private InstructionImpl startNode(String text) {
         return startNode(text, true);
     }
 
     private InstructionImpl startNode(String text, boolean checkPending) {
         return startNodeImpl(checkPending, new InstructionImpl(text, myInstructionNumber++));
     }
 
     private void finishNode(InstructionImpl instruction) {
         assert instruction.equals(myProcessingStack.pop());
         myHead = myProcessingStack.peek();
     }
 
     //
 //  public void visitField(LuaField field) {
 //  }
 //
 //  public void visitParameter(LuaParameter parameter) {
 //  }
 //
 //  public void visitMethod(LuaMethod method) {
 //  }
 //
 //  public void visitTypeDefinition(LuaTypeDefinition typeDefinition) {
 //    if (typeDefinition instanceof LuaAnonymousClassDefinition) {
 //      super.visitTypeDefinition(typeDefinition);
 //    }
 //  }
 //
 //  public void visitVariable(LuaVariable variable) {
 //    super.visitVariable(variable);
 //    if (variable.getInitializerLua() != null ||
 //        variable.getParent() instanceof LuaTupleDeclaration && ((LuaTupleDeclaration)variable.getParent())
 // .getInitializerLua() != null) {
 //      ReadWriteVariableInstructionImpl writeInsn = new ReadWriteVariableInstructionImpl(variable,
 // myInstructionNumber++);
 //      checkPending(writeInsn);
 //      addNode(writeInsn);
 //    }
 //  }
 //
     @Nullable
     private InstructionImpl findInstruction(PsiElement element) {
         for (int i = myProcessingStack.size() - 1; i >= 0; i--) {
             InstructionImpl instruction = myProcessingStack.get(i);
             if (element.equals(instruction.getElement())) return instruction;
         }
         return null;
     }
 
     static class CallInstructionImpl extends InstructionImpl implements CallInstruction {
         private final InstructionImpl myCallee;
 
         public String toString() {
             return super.toString() + " CALL " + myCallee.num();
         }
 
         public Iterable<? extends Instruction> succ(CallEnvironment env) {
             getStack(env, myCallee).push(this);
             return Collections.singletonList(myCallee);
         }
 
         public Iterable<? extends Instruction> allSucc() {
             return Collections.singletonList(myCallee);
         }
 
         protected String getElementPresentation() {
             return "";
         }
 
         CallInstructionImpl(int num, InstructionImpl callee) {
             super("CALL", num);
             myCallee = callee;
         }
     }
 
     static class PostCallInstructionImpl extends InstructionImpl implements AfterCallInstruction {
         private final CallInstructionImpl myCall;
         private       RetInstruction      myReturnInsn;
 
         public String toString() {
             return super.toString() + "AFTER CALL " + myCall.num();
         }
 
         public Iterable<? extends Instruction> allPred() {
             return Collections.singletonList(myReturnInsn);
         }
 
         public Iterable<? extends Instruction> pred(CallEnvironment env) {
             getStack(env, myReturnInsn).push(myCall);
             return Collections.singletonList(myReturnInsn);
         }
 
         protected String getElementPresentation() {
             return "";
         }
 
         PostCallInstructionImpl(int num, CallInstructionImpl call) {
             super("POSTCALL", num);
             myCall = call;
         }
 
         public void setReturnInstruction(RetInstruction retInstruction) {
             myReturnInsn = retInstruction;
         }
     }
 
     static class RetInstruction extends InstructionImpl {
         RetInstruction(int num) {
             super("RETURN", num);
         }
 
         public String toString() {
             return super.toString() + " RETURN";
         }
 
         protected String getElementPresentation() {
             return "";
         }
 
         public Iterable<? extends Instruction> succ(CallEnvironment env) {
             final Stack<CallInstruction> callStack = getStack(env, this);
             if (callStack.isEmpty())
                 return Collections.emptyList();     //can be true in case env was not populated (e.g. by DFA)
 
             final CallInstruction callInstruction = callStack.peek();
             final List<InstructionImpl> succ = ((CallInstructionImpl) callInstruction).mySucc;
             final Stack<CallInstruction> copy = (Stack<CallInstruction>) callStack.clone();
             copy.pop();
             for (InstructionImpl instruction : succ) {
                 env.update(copy, instruction);
             }
 
             return succ;
         }
     }
 
     private static boolean hasDeclaredVariable(String name, LuaBlock scope, PsiElement place) {
         PsiElement prev = null;
         while (place != null) {
             if (place instanceof LuaBlock) {
                 LuaStatementElement[] statements = ((LuaBlock) place).getStatements();
                 for (LuaStatementElement statement : statements) {
                     if (statement == prev) break;
                     if (statement instanceof LuaDeclarationStatement) {
                         LuaSymbol[] variables = ((LuaDeclarationStatement) statement).getDefinedSymbols();
                         for (LuaSymbol variable : variables) {
                             if (name.equals(variable.getName())) return true;
                         }
                     }
                 }
             }
 
             if (place == scope) {
                 break;
             } else {
                 prev = place;
                 place = place.getParent();
             }
         }
 
         return false;
     }
 
 
 }
