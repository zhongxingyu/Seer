 package com.sdc.abstractLanguage;
 
 import com.sdc.ast.OperationType;
 import com.sdc.ast.controlflow.*;
 import com.sdc.ast.expressions.*;
 import com.sdc.ast.expressions.InstanceInvocation;
 import com.sdc.ast.expressions.Invocation;
 import com.sdc.ast.expressions.identifiers.Field;
 import com.sdc.ast.expressions.identifiers.Identifier;
 import com.sdc.ast.expressions.identifiers.Variable;
 import com.sdc.ast.expressions.nestedclasses.LambdaFunction;
 import com.sdc.cfg.nodes.Node;
 import com.sdc.cfg.nodes.Switch;
 import com.sdc.util.ConstructionBuilder;
 import com.sdc.util.DeclarationWorker;
 import com.sdc.util.DominatorTreeGenerator;
 import org.objectweb.asm.*;
 import org.objectweb.asm.util.Printer;
 
 import java.util.*;
 
 import static com.sdc.ast.OperationType.*;
 import static com.sdc.ast.expressions.IntConstant.*;
 import static org.objectweb.asm.Opcodes.ASM4;
 
 public abstract class AbstractMethodVisitor extends MethodVisitor {
     protected AbstractMethod myDecompiledMethod;
 
     protected final String myDecompiledOwnerFullClassName;
     protected final String myDecompiledOwnerSuperClassName;
 
     protected Stack<Expression> myBodyStack = new Stack<Expression>();
     protected List<Statement> myStatements = new ArrayList<Statement>();
 
     protected List<Node> myNodes = new ArrayList<Node>();
     protected List<Label> myLabels = new ArrayList<Label>();
     protected Map<Label, List<Integer>> myMap1 = new HashMap<Label, List<Integer>>();  // for GOTO
     protected Map<Integer, Label> myMap2 = new HashMap<Integer, Label>(); // for IF ELSE Branch
     protected List<Label> myNodeInnerLabels = new ArrayList<Label>();
 
     protected boolean myHasDebugInformation = false;
 
     protected String myClassFilesJarPath = "";
 
     protected AbstractLanguagePartFactory myLanguagePartFactory;
     protected AbstractVisitorFactory myVisitorFactory;
 
     protected DeclarationWorker.SupportedLanguage myLanguage;
 
     public AbstractMethodVisitor(final AbstractMethod abstractMethod, final String decompiledOwnerFullClassName, final String decompiledOwnerSuperClassName) {
         super(ASM4);
         this.myDecompiledMethod = abstractMethod;
         this.myDecompiledOwnerFullClassName = decompiledOwnerFullClassName;
         this.myDecompiledOwnerSuperClassName = decompiledOwnerSuperClassName;
     }
 
     protected abstract boolean checkForAutomaticallyGeneratedAnnotation(final String annotationName);
 
     protected AbstractFrame getCurrentFrame() {
         return myDecompiledMethod.getCurrentFrame();
     }
 
     public AbstractMethod getDecompiledMethod() {
         return myDecompiledMethod;
     }
 
     public void setClassFilesJarPath(final String classFilesJarPath) {
         this.myClassFilesJarPath = classFilesJarPath;
     }
 
     public String getDecompiledOwnerFullClassName() {
         return myDecompiledOwnerFullClassName;
     }
 
     public String getDecompiledOwnerSuperClassName() {
         return myDecompiledOwnerSuperClassName;
     }
 
     protected AnnotationVisitor visitAnnotation(final int parameter, final String desc, final boolean visible) {
         List<String> annotationsImports = new ArrayList<String>();
         final String annotationName = getDescriptor(desc, 0, annotationsImports);
         if (!checkForAutomaticallyGeneratedAnnotation(annotationName)) {
             AbstractAnnotation annotation = myLanguagePartFactory.createAnnotation();
             annotation.setName(annotationName);
             if (parameter == -1) {
                 myDecompiledMethod.appendAnnotation(annotation);
             } else {
                 myDecompiledMethod.appendParameterAnnotation(parameter, annotation);
             }
             myDecompiledMethod.getImports().addAll(annotationsImports);
             return myVisitorFactory.createAnnotationVisitor(annotation);
         } else {
             return null;
         }
     }
 
     @Override
     public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
         return visitAnnotation(-1, desc, visible);
     }
 
     @Override
     public void visitAttribute(final Attribute attr) {
     }
 
     @Override
     public AnnotationVisitor visitAnnotationDefault() {
         return null;
     }
 
     @Override
     public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
         return visitAnnotation(parameter, desc, visible);
     }
 
     @Override
     public void visitCode() {
     }
 
     @Override
     public void visitFrame(final int type, final int nLocal, final Object[] local, final int nStack, final Object[] stack) {
         if (type == 2) {
             // F_CHOP
             myDecompiledMethod.setCurrentFrame(getCurrentFrame().getParent());
         } else if (type == 3) {
             // F_SAME
             AbstractFrame newAbstractFrame = myLanguagePartFactory.createFrame();
             newAbstractFrame.setSameFrame(getCurrentFrame());
             if (getCurrentFrame().getParent() != null) {
                 getCurrentFrame().getParent().addChild(newAbstractFrame);
                 newAbstractFrame.setParent(getCurrentFrame().getParent());
             } else {
                 getCurrentFrame().addChild(newAbstractFrame);
                 newAbstractFrame.setParent(getCurrentFrame());
             }
 
             myDecompiledMethod.setCurrentFrame(newAbstractFrame);
         } else {
             AbstractFrame newAbstractFrame = myLanguagePartFactory.createFrame();
 
             newAbstractFrame.setParent(getCurrentFrame());
             getCurrentFrame().addChild(newAbstractFrame);
 
             myDecompiledMethod.setCurrentFrame(newAbstractFrame);
 
             if (nStack > 0) {
                 String stackedVariableType;
 
                 if (stack[0] instanceof Integer) {
                     stackedVariableType = DeclarationWorker.getDescriptorByInt((Integer) stack[0], myLanguage);
                 } else {
                     final String className = (String) stack[0];
                     myDecompiledMethod.addImport(DeclarationWorker.getDecompiledFullClassName(className));
                     stackedVariableType = getClassName(className) + " ";
                 }
 
                 getCurrentFrame().setStackedVariableType(stackedVariableType);
             }
         }
     }
 
     @Override
     public void visitInsn(final int opcode) {
         int size = myBodyStack.size();
         final String opString = Printer.OPCODES[opcode];
         if (opString.contains("ADD") || opString.contains("SUB")
                 || opString.contains("MUL") || opString.contains("DIV") || opString.contains("REM")
                 || opString.contains("USHR") || opString.contains("SHL")
                 || opString.contains("XOR") || opString.contains("SHR")
                 || opString.contains("OR") || opString.contains("AND")) {
             Expression e1 = getTopOfBodyStack();
             Expression e2 = getTopOfBodyStack();
             String type = opString.substring(1);
             if (opString.contains("OR") || opString.contains("AND")) {
                 type = "BITWISE_" + type;
             }
             Expression res;
             res = new BinaryExpression(OperationType.valueOf(type), e2, e1);
             myBodyStack.push(res);
         } else if (opString.contains("NEG")) {
             Expression e = getTopOfBodyStack();
             myBodyStack.push(new UnaryExpression(NEGATE, e));
         } else if (opString.contains("CONST_M1")) {
             myBodyStack.push(M_ONE);
         } else if (opString.contains("CONST_NULL")) {
             myBodyStack.push(new Constant("null", false));
         } else if (opString.contains("CONST_")) {
             if (opString.contains("ICONST_")) {
                 myBodyStack.push(new IntConstant(Integer.valueOf(opString.substring(7).toLowerCase())));
             } else {
                 myBodyStack.push(new Constant(opString.substring(7).toLowerCase(), false));
             }
         } else if (opString.equals("RETURN")) {
             replaceInvocationsFromExpressionsToStatements();
             Return returnStatement = new Return();
             returnStatement.setNeedToPrintReturn(!myDecompiledMethod.getDecompiledClass().isLambdaFunctionClass());
             myStatements.add(returnStatement);
         } else if (opString.contains("RETURN")) {
             Expression expression = replaceBooleanConstant(getTopOfBodyStack());
             replaceInvocationsFromExpressionsToStatements();
             Return returnStatement = new Return(expression);
             returnStatement.setNeedToPrintReturn(!myDecompiledMethod.getDecompiledClass().isLambdaFunctionClass());
             myStatements.add(returnStatement);
         } else if (opString.contains("CMP")) {
             Expression b = getTopOfBodyStack();
             Expression a = getTopOfBodyStack();
             myBodyStack.push(new TernaryExpression(
                     new BinaryExpression(EQ, a, b),
                     ZERO,
                     new TernaryExpression(new BinaryExpression(LT, a, b), M_ONE, ONE)
             ));
         } else if (opString.contains("ATHROW")) {
             myStatements.add(new Throw(getTopOfBodyStack()));
         } else if (opString.equals("SWAP")) {
             if (size < 2) return;
             Expression expr1 = myBodyStack.pop();
             Expression expr2 = myBodyStack.pop();
             if (expr1.hasDoubleLength() || expr2.hasDoubleLength()) {
                 //There is a wrong condition, we return elements of stack as they were there
                 multiPush(expr2, expr1);
             } else {
                 multiPush(expr1, expr2);
             }
         } else if (opString.equals("DUP") && !myBodyStack.isEmpty()) {
             Expression expr = myBodyStack.peek();
             if (expr.hasDoubleLength()) return;
             myBodyStack.push(expr);
         } else if (opString.equals("DUP_X1")) {
             if (size < 2) return;
             Expression expr1 = myBodyStack.pop();
             Expression expr2 = myBodyStack.pop();
             if (expr1.hasDoubleLength() || expr2.hasDoubleLength()) {
                 //There is a wrong condition, we return elements of stack as they were there
                 multiPush(expr2, expr1);
                 return;
             }
             multiPush(expr1, expr2, expr1);
         } else if (opString.equals("DUP_X2")) {
             if (size < 2) return;
             Expression expr1 = myBodyStack.pop();
             Expression expr2 = myBodyStack.pop();
             if (expr1.hasDoubleLength()) {
                 //There is a wrong condition, we return elements of stack as they were there
                 multiPush(expr2, expr1);
                 return;
             }
             if (expr2.hasDoubleLength()) {
                 //Form 2
                 multiPush(expr1, expr2, expr1);
             } else {
                 if (size < 3 || myBodyStack.peek().hasDoubleLength()) {
                     //There is a wrong condition, we return elements of stack as they were there
                     multiPush(expr2, expr1);
                     return;
                 }
                 Expression expr3 = myBodyStack.pop();
                 //Form 1
                 multiPush(expr1, expr3, expr2, expr1);
             }
         } else if (opString.equals("DUP2")) {
             if (size < 1) return;
             Expression expr1 = myBodyStack.pop();
             if (expr1.hasDoubleLength()) {
                 //Form 2
                 multiPush(expr1, expr1);
             } else {
                 if (size < 2 || myBodyStack.peek().hasDoubleLength()) {
                     //There is a wrong condition, we return elements of stack as they were there
                     multiPush(expr1);
                     return;
                 }
                 Expression expr2 = myBodyStack.pop();
                 //Form 1
                 multiPush(expr2, expr1, expr2, expr1);
             }
         } else if (opString.equals("DUP2_X1")) {
             if (size < 2) return;
             Expression expr1 = myBodyStack.pop();
             Expression expr2 = myBodyStack.pop();
             if (expr1.hasDoubleLength() && !expr2.hasDoubleLength()) {
                 //Form 2
                 multiPush(expr1, expr2, expr1);
             } else {
                 if (size < 3 || myBodyStack.peek().hasDoubleLength()
                         || expr1.hasDoubleLength() || expr2.hasDoubleLength()) {
                     //There is a wrong condition, we return elements of stack as they were there
                     multiPush(expr2, expr1);
                     return;
                 }
                 Expression expr3 = myBodyStack.pop();
                 //Form 1
                 multiPush(expr2, expr1, expr3, expr2, expr1);
             }
         } else if (opString.equals("DUP2_X2")) {
             if (size < 2) return;
             Expression expr1 = myBodyStack.pop();
             Expression expr2 = myBodyStack.pop();
             if (!expr1.hasDoubleLength() && !expr2.hasDoubleLength()) {
                 if (size < 3) {
                     //There is a wrong condition, we return elements of stack as they were there
                     multiPush(expr2, expr1);
                     return;
                 }
                 Expression expr3 = myBodyStack.pop();
                 if (expr3.hasDoubleLength()) {
                     //Form 3
                     multiPush(expr2, expr1, expr3, expr2, expr1);
                     return;
                 }
                 if (size < 4 || myBodyStack.peek().hasDoubleLength()) {
                     //There is a wrong condition, we return elements of stack as they were there
                     multiPush(expr3, expr2, expr1);
                     return;
                 }
                 Expression expr4 = myBodyStack.pop();
                 //Form 1
                 multiPush(expr2, expr1, expr4, expr3, expr2, expr1);
             } else if (expr1.hasDoubleLength() && !expr2.hasDoubleLength()) {
                 if (size < 3 || myBodyStack.peek().hasDoubleLength()) {
                     //There is a wrong condition, we return elements of stack as they were there
                     multiPush(expr2, expr1);
                     return;
                 }
                 Expression expr3 = myBodyStack.pop();
                 //Form 2
                 multiPush(expr1, expr3, expr2, expr1);
             } else if (expr1.hasDoubleLength() && expr2.hasDoubleLength()) {
                 //Form 4
                 multiPush(expr1, expr2, expr1);
             }
         } else if (opString.equals("POP")) {
             if (size < 1 || myBodyStack.peek().hasDoubleLength()) return;
             myBodyStack.pop();
         } else if (opString.equals("POP2")) {
             if (size < 1) return;
             Expression expr1 = myBodyStack.pop();
             if (expr1.hasDoubleLength()) {
                 //Form 2
             } else {
                 if (size < 2 || myBodyStack.peek().hasDoubleLength()) {
                     //There is a wrong condition, we return elements of stack as they were there
                     multiPush(expr1);
                     return;
                 }
                 //Form 1
                 myBodyStack.pop();
             }
         } else if (opString.contains("ALOAD")) {
             final Expression arrayIndex = getTopOfBodyStack();
             myBodyStack.push(new Variable(arrayIndex, (Identifier) getTopOfBodyStack()));
         } else if (opString.contains("ASTORE")) {
             final Expression expr = getTopOfBodyStack();
             final Expression arrayIndex = getTopOfBodyStack();
             final Identifier v = new Variable(arrayIndex, (Identifier) getTopOfBodyStack());
 
             myStatements.add(new Assignment(v, expr));
         } else if (opString.equals("NOP")) {
             //do nothing
         } else if ((opString.contains("I2L") || opString.contains("F2L") || opString.contains("D2L")) && !myBodyStack.empty()) {
             myBodyStack.push(new UnaryExpression(LONG_CAST, getTopOfBodyStack()));
         } else if ((opString.contains("I2D") || opString.contains("F2D") || opString.contains("L2D")) && !myBodyStack.isEmpty()) {
             myBodyStack.push(new UnaryExpression(DOUBLE_CAST, getTopOfBodyStack()));
         } else if ((opString.contains("I2F") || opString.contains("L2F") || opString.contains("D2F")) && !myBodyStack.isEmpty()) {
             myBodyStack.push(new UnaryExpression(FLOAT_CAST, getTopOfBodyStack()));
         } else if ((opString.contains("L2I") || opString.contains("F2I") || opString.contains("D2I")) && !myBodyStack.isEmpty()) {
             myBodyStack.push(new UnaryExpression(INT_CAST, getTopOfBodyStack()));
         } else if ((opString.contains("I2B")) && !myBodyStack.isEmpty()) {
             myBodyStack.push(new UnaryExpression(BYTE_CAST, getTopOfBodyStack()));
         } else if ((opString.contains("I2C")) && !myBodyStack.isEmpty()) {
             myBodyStack.push(new UnaryExpression(CHAR_CAST, getTopOfBodyStack()));
         } else if ((opString.contains("I2S")) && !myBodyStack.isEmpty()) {
             myBodyStack.push(new UnaryExpression(SHORT_CAST, getTopOfBodyStack()));
         }
         // All opcodes :
         //  +NOP, +ACONST_NULL, +ICONST_M1, +CONST_0, +ICONST_1, +ICONST_2, +ICONST_3, +ICONST_4, +ICONST_5,
         //  +LCONST_0, +LCONST_1, +FCONST_0, +FCONST_1, +FCONST_2, +DCONST_0, +DCONST_1, +IALOAD, +LALOAD, +FALOAD, +DALOAD,
         //  +AALOAD, +BALOAD, +CALOAD, +SALOAD, +IASTORE, +LASTORE, +FASTORE, +DASTORE, +AASTORE, +BASTORE, +CASTORE, +SASTORE,
         //  +POP, +POP2, +DUP, +DUP_X1, +DUP_X2, +DUP2, +DUP2_X1, +DUP2_X2, +SWAP, +IADD, +LADD, +FADD, +DADD, +ISUB, +LSUB, +FSUB,
         //  +DSUB, +IMUL, +LMUL, +FMUL, +DMUL, +IDIV, +LDIV, +FDIV, +DDIV, +IREM, +LREM, +FREM, +DREM, +INEG, +LNEG, +FNEG, +DNEG,
         //  +ISHL, +LSHL, +ISHR, +LSHR, +IUSHR, +LUSHR, +IAND, +LAND, +IOR, +LOR, +IXOR, +LXOR, +I2L, +I2F, +I2D, +L2I, +L2F, +L2D,
         //  +F2I, +F2L, +F2D, +D2I, +D2L, +D2F, +I2B, +I2C, +I2S, LCMP, FCMPL, FCMPG, DCMPL, DCMPG, +IRETURN, +LRETURN, +FRETURN,
         //  +DRETURN, +ARETURN, +RETURN, ARRAYLENGTH, +ATHROW, MONITORENTER, or MONITOREXIT.
     }
 
     @Override
     public void visitIntInsn(final int opcode, final int operand) {
         final String opString = Printer.OPCODES[opcode];
 
         if (opString.contains("IPUSH")) {
             myBodyStack.push(new IntConstant(operand));
         } else if (opString.contains("NEWARRAY")) {
             List<Expression> dimensions = new ArrayList<Expression>();
             dimensions.add(getTopOfBodyStack());
             myBodyStack.push(new NewArray(1, Printer.TYPES[operand].substring(2).toLowerCase(), dimensions));
         }
     }
 
     @Override
     public void visitVarInsn(final int opcode, final int var) {
         final String opString = Printer.OPCODES[opcode];
 
         final boolean currentFrameHasStack = getCurrentFrame().checkStack();
         String variableType = null;
 
         if (opString.contains("LOAD")) {
             if (myStatements.isEmpty()) {
                 myBodyStack.push(new Variable(var, getCurrentFrame()));
             } else {
                 int lastStatementIndex = myStatements.size() - 1;
                 Statement lastStatement = myStatements.get(lastStatementIndex);
                 if (opString.contains("ILOAD") && lastStatement instanceof Increment
                         && ((Increment) lastStatement).getVariable().getIndex() == var) {
                     Increment increment = (Increment) lastStatement;
                     myStatements.remove(lastStatementIndex);
                     OperationType type = increment.getType();
                     switch (type) {
                         case INC:
                             type = INC_REV;
                             break;
                         case DEC:
                             type = DEC_REV;
                             break;
                         default:
                     }
                     myBodyStack.push(new ExprIncrement(increment.getVariable(), increment.getIncrementExpression(), type));
                 } else {
                     myBodyStack.push(new Variable(var, getCurrentFrame()));
                 }
             }
         } else if (opString.contains("STORE") && !currentFrameHasStack) {
             Identifier v = new Variable(var, getCurrentFrame());
             final Expression expr = getTopOfBodyStack();
             myStatements.add(new Assignment(v, expr));
             if (expr instanceof Invocation) {
                 variableType = ((Invocation) expr).getReturnType();
             } else if (expr instanceof New) {
                 variableType = ((New) expr).getReturnType();
             } else if (expr instanceof NewArray) {
                 variableType = ((NewArray) expr).getFullType();
             } else if (expr instanceof Identifier) {
                 variableType = ((Identifier) expr).getType();
             } else if (expr instanceof LambdaFunction) {
                 variableType = ((LambdaFunction) expr).getType();
             }
 
             int lastIndex = myStatements.size() - 1;
             Expression expr2 = (myBodyStack.empty() ? null : myBodyStack.peek());
             if (expr instanceof BinaryExpression && ((BinaryExpression) expr).isArithmeticType()) {
                 BinaryExpression binaryExpression = (BinaryExpression) expr;
                 Expression left = binaryExpression.getLeft();
                 Expression right = binaryExpression.getRight();
                 if (left instanceof Variable && ((Variable) left).getIndex() == var /*&& right instanceof IntConstant*/) {
                     myStatements.remove(lastIndex);
                     if (expr.equals(expr2)) {
                         myBodyStack.pop();
                         myBodyStack.add(new ExprIncrement(left,right, binaryExpression.getOperationType()));
                     } else {
                         myStatements.add(new Increment((Variable) left,  right, binaryExpression.getOperationType()));
                     }
                 } else if (right instanceof Variable && ((Variable) right).getIndex() == var/* && left instanceof IntConstant*/) {
                     myStatements.remove(lastIndex);
                     if (expr.equals(expr2)) {
                         myBodyStack.pop();
                         myBodyStack.add(new ExprIncrement(right, left, binaryExpression.getOperationType()));
                     } else {
                         myStatements.add(new Increment((Variable) right, left, binaryExpression.getOperationType()));
                     }
                 }
             }
         }
 
         if (!opString.contains("LOAD") && var > myDecompiledMethod.getLastLocalVariableIndex()) {
             final String name = "y" + var;
             myDecompiledMethod.addLocalVariableName(var, name);
 
             String descriptorType;
             if (currentFrameHasStack) {
                 descriptorType = getCurrentFrame().getStackedVariableType();
                 getCurrentFrame().setStackedVariableIndex(var);
             } else {
                 descriptorType = getDescriptor(opString, 0, myDecompiledMethod.getImports());
             }
 
             if (!descriptorType.equals("Object ") && !descriptorType.equals("Any") || variableType == null) {
                 variableType = descriptorType;
             }
 
             myDecompiledMethod.addLocalVariableType(var, variableType);
         }
     }
 
     @Override
     public void visitTypeInsn(final int opcode, final String type) {
         final String opString = Printer.OPCODES[opcode];
 
         if (opString.contains("NEWARRAY")) {
             List<Expression> dimensions = new ArrayList<Expression>();
             dimensions.add(getTopOfBodyStack());
             myBodyStack.push(new NewArray(1, getClassName(type), dimensions));
         } else if (opString.contains("INSTANCEOF")) {
             myBodyStack.push(new InstanceOf(getClassName(type), getTopOfBodyStack()));
         } else if (opString.contains("CHECKCAST") && !myBodyStack.empty()) {
             //type is for name of class
             myBodyStack.push(new UnaryExpression(CHECK_CAST, myBodyStack.pop(), type));
         }
     }
 
     @Override
     public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
         final String opString = Printer.OPCODES[opcode];
         final String fieldName = myDecompiledMethod.getDecompiledClass().isLambdaFunctionClass() ? name.substring(1) : name;
         Field field = new Field(fieldName, getDescriptor(desc, 0, myDecompiledMethod.getImports()));
 
         Expression e = null;
         if (opString.contains("PUTFIELD") || opString.contains("PUTSTATIC")) {
             e = getTopOfBodyStack();
         }
 
         if (opString.contains("PUTFIELD") || opString.contains("GETFIELD")) {
             final Identifier fieldOwner = (Identifier) getTopOfBodyStack();
             field.setOwner(fieldOwner);
         } else {
             final String fieldOwner = getClassName(owner);
             field.setStaticOwnerName(fieldOwner);
         }
 
         if (opString.contains("PUTFIELD") || opString.contains("PUTSTATIC")) {
             if (myDecompiledOwnerFullClassName.endsWith(myDecompiledMethod.getName()) && e instanceof Constant) {
                 myDecompiledMethod.addInitializerToField(name, e);
             } else {
                 myStatements.add(new Assignment(field, e));
             }
         } else if (opString.contains("GETFIELD") || opString.contains("GETSTATIC")) {
             myBodyStack.push(field);
         }
     }
 
     @Override
     public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
         final String opString = Printer.OPCODES[opcode];
 
         final String decompiledOwnerFullClassName = DeclarationWorker.getDecompiledFullClassName(owner);
         final String ownerClassName = getClassName(owner);
 
         List<Expression> arguments = getInvocationArguments(desc);
         String returnType = getInvocationReturnType(desc);
         String invocationName = name;
 
         boolean isStaticInvocation = false;
 
         if (opString.contains("INVOKEVIRTUAL") || opString.contains("INVOKEINTERFACE")
                 || (decompiledOwnerFullClassName.equals(myDecompiledOwnerFullClassName) && !name.equals("<init>"))) {
             if (!myBodyStack.isEmpty() && myBodyStack.peek() instanceof Variable) {
                 appendInstanceInvocation(name, returnType, arguments, (Variable) myBodyStack.pop());
                 return;
             } else {
                 invocationName = "." + name;
             }
         }
 
         if (opString.contains("INVOKESPECIAL")) {
             if (name.equals("<init>")) {
                 myDecompiledMethod.addImport(decompiledOwnerFullClassName);
                 invocationName = ownerClassName;
                 returnType = invocationName + " ";
             } else {
                 invocationName = "super." + name;
             }
         }
 
         if (opString.contains("INVOKESTATIC")) {
             myDecompiledMethod.addImport(decompiledOwnerFullClassName);
             invocationName = ownerClassName + "." + name;
             isStaticInvocation = true;
         }
 
         appendInvocationOrConstructor(isStaticInvocation, name, invocationName, returnType, arguments);
     }
 
     @Override
     public void visitInvokeDynamicInsn(final String name, final String desc, final Handle bsm, final Object... bsmArgs) {
     }
 
     @Override
     public void visitJumpInsn(final int opcode, final Label label) {
         final String opString = Printer.OPCODES[opcode];
         if (opString.contains("IF")) {
             final Label myLastIFLabel = label;
             if (myNodes.isEmpty() || !myNodeInnerLabels.isEmpty() || (myNodes.get(getLeftEmptyNodeIndex() - 1).getCondition() == null)) {
                 myLabels.add(myLastIFLabel);
                 myMap2.put(myNodes.size(), label);
                 applyNode();
                 final int last = myNodes.size() - 1;
                 Expression e1 = getTopOfBodyStack();
                 Expression e2 = getTopOfBodyStack();
                 Expression cond = new BinaryExpression(OperationType.valueOf(opString.substring(7)), e2, e1);
                 myNodes.get(last).setCondition(cond);
                 myNodes.get(last).setEmpty(true);
             }
         } else if (opString.contains("GOTO")) {
             myLabels.add(label);
             final int value = getLeftEmptyNodeIndex();
             if (!myMap1.containsKey(label)) {
                 List<Integer> list = new ArrayList<Integer>();
                 list.add(value);
                 myMap1.put(label, list);
             } else {
                 myMap1.get(label).add(value);
             }
         }
     }
 
     @Override
     public void visitLabel(final Label label) {
         if (myLabels.contains(label)) {
             applyNode();
             myLabels.remove(label);
         }
         myNodeInnerLabels.add(label);
     }
 
     @Override
     public void visitLdcInsn(final Object cst) {
         final boolean hasDoubleLength = true;
         myBodyStack.push(new Constant(cst, cst instanceof String, hasDoubleLength));
     }
 
     @Override
     public void visitIincInsn(final int var, final int increment) {
         if (!myBodyStack.empty()) {
             Expression expr = myBodyStack.peek();
             if (expr != null && expr instanceof Variable && ((Variable) expr).getIndex() == var) {
                 myBodyStack.pop();
                 myBodyStack.push(new ExprIncrement(expr, increment));
                 return;
             }
         }
         myStatements.add(new Increment(new Variable(var, getCurrentFrame()), increment));
     }
 
     @Override
     public void visitTableSwitchInsn(final int min, final int max, final Label dflt, final Label... labels) {
         int[] keys = new int[max - min + 1];
         List<Label> list = new ArrayList<Label>();
         for (int i = 0; i < labels.length; i++) {
             myLabels.add(labels[i]);
             keys[i] = min + i;
             list.add(labels[i]);
         }
         list.add(dflt);
         myLabels.add(dflt);
         Node switch_node = new Switch(myBodyStack.pop(), keys, list, myNodes.size());
         myNodes.add(switch_node);
     }
 
     @Override
     public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) {
         List<Label> list = new ArrayList<Label>();
         for (Label label : labels) {
             myLabels.add(label);
             list.add(label);
         }
         list.add(dflt);
         myLabels.add(dflt);
         Node switch_node = new Switch(myBodyStack.pop(), keys, list, myNodes.size());
         myNodes.add(switch_node);
     }
 
     @Override
     public void visitMultiANewArrayInsn(final String desc, final int dims) {
         List<Expression> dimensions = new ArrayList<Expression>();
         for (int i = 0; i < dims; i++) {
             dimensions.add(0, getTopOfBodyStack());
         }
 
         final String className = getDescriptor(desc.substring(dims), 0, myDecompiledMethod.getImports()).trim();
         myBodyStack.push(new NewArray(dims, className, dimensions));
     }
 
 /*    @Override
     public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
         //System.out.println(start + " " + end + " " + handler + " " + type);
 
         ExceptionHandler exceptionHandler = new ExceptionHandler(type);
         myNodes.add(exceptionHandler);
         //  applyNode();
         final Node node = new Node();
         myNodes.add(node);
         final int temp = getLeftEmptyNodeIndex();
 //        myNodes.get(temp - 1).addTail(myNodes.get(myNodes.size() - 1));
         myLabels.add(start);
 //        myLabels.add(end);
         myLabels.add(handler);
         List<Integer> list = new ArrayList<Integer>();
         list.add(temp);
         if (type == null) {
         } else {
             myMap1.put(handler, list);
 
         }
         //  myMap1.put(handler, list);
     }*/
 
     @Override
     public void visitLocalVariable(final String name, final String desc,
                                    final String signature, final Label start, final Label end,
                                    final int index) {
         if (!myHasDebugInformation) {
             myHasDebugInformation = true;
         }
 
         myDecompiledMethod.addLocalVariableName(index, name);
         final String description = signature != null ? signature : desc;
         myDecompiledMethod.addLocalVariableFromDebugInfo(index, name, getDescriptor(description, 0, myDecompiledMethod.getImports()));
     }
 
     @Override
     public void visitLineNumber(final int line, final Label start) {
     }
 
     @Override
     public void visitMaxs(final int maxStack, final int maxLocals) {
     }
 
     @Override
     public void visitEnd() {
         applyNode();
 
         placeEdges();
 
         DominatorTreeGenerator gen = new DominatorTreeGenerator(myNodes);
         ConstructionBuilder cb = new ConstructionBuilder(myNodes, gen);
 
         myDecompiledMethod.setBegin(cb.build());
     }
 
     private void placeEdges() {
         // GOTO
         for (final Label lbl : myMap1.keySet()) {
             for (final Node node : myNodes) {
                 if (node.containsLabel(lbl)) {
                     for (final Integer i : myMap1.get(lbl)) {
                         if (i != myNodes.indexOf(node)) {
                             myNodes.get(i).addTail(node);
                             node.addAncestor(myNodes.get(i));
                         }
                     }
                     break;
                 }
             }
         }
         // Switch + sequence
         for (int i = 0; i < myNodes.size(); i++) {
             final Node node = myNodes.get(i);
             if (node instanceof Switch) {
                 for (final Label label : ((Switch) node).getLabels()) {
                     for (int j = i + 1; j < myNodes.size(); j++) {
                         if (myNodes.get(j).containsLabel(label)) {
                             node.addTail(myNodes.get(j));
                             myNodes.get(j).addAncestor(node);
                             break;
                         }
                     }
                 }
             } else if (node.getListOfTails().isEmpty() && !node.isLastStatementReturn()) {
                 node.addTail(myNodes.get(i + 1));
                 myNodes.get(i + 1).addAncestor(node);
             }
         }
         // IF ELSE Branch
         for (final Integer index : myMap2.keySet()) {
             for (final Node node : myNodes) {
                 if (node.containsLabel(myMap2.get(index))) {
                     myNodes.get(index).addTail(node);
                     node.addAncestor(myNodes.get(index));
                     break;
                 }
             }
         }
     }
 
     protected Integer getLeftEmptyNodeIndex() {
         for (Node node : myNodes) {
             if (node.statementsIsEmpty() && !node.isEmpty()) {
                 return myNodes.indexOf(node);
             }
         }
         return myNodes.size();
     }
 
     protected void applyNode() {
         Integer i = getLeftEmptyNodeIndex();
         if (i != myNodes.size()) {
             myNodes.get(i).setStatements(new ArrayList<Statement>(myStatements));
             myNodes.get(i).setInnerLabels(new ArrayList<Label>(myNodeInnerLabels));
             if (myNodes.get(i).getStatements().isEmpty()) {
                 myNodes.get(i).setEmpty(true);
             }
         } else {
             Node node = new Node(new ArrayList<Statement>(myStatements), new ArrayList<Label>(myNodeInnerLabels), myNodes.size());
             if (node.getStatements().isEmpty()) {
                 node.setEmpty(true);
             }
             myNodes.add(node);
         }
         myNodeInnerLabels.clear();
         myStatements.clear();
     }
 
     protected Expression getTopOfBodyStack() {
         if (myBodyStack.isEmpty()) {
             final int lastIndex = myStatements.size() - 1;
             final Statement lastStatement = myStatements.get(lastIndex);
 
             if (lastStatement instanceof com.sdc.ast.controlflow.InstanceInvocation) {
                 com.sdc.ast.controlflow.InstanceInvocation invoke = (com.sdc.ast.controlflow.InstanceInvocation) lastStatement;
                 myStatements.remove(lastIndex);
                 return new com.sdc.ast.expressions.InstanceInvocation(invoke.getFunction(), invoke.getReturnType(), invoke.getArguments(), invoke.getVariable());
             } else if (lastStatement instanceof com.sdc.ast.controlflow.Invocation) {
                 com.sdc.ast.controlflow.Invocation invoke = (com.sdc.ast.controlflow.Invocation) lastStatement;
                 myStatements.remove(lastIndex);
                 return new com.sdc.ast.expressions.Invocation(invoke.getFunction(), invoke.getReturnType(), invoke.getArguments());
             } else if (lastStatement instanceof Assignment) {
                 return ((Assignment) lastStatement).getRight();
             }
         }
         // ternary expression under construction
         /*
             else if ((myNodes.size() > 1) && (myBodyStack.size() >= 2) && (myNodes.get(myNodes.size() - 2).getCondition() != null)) {
             final int lastIndex = myNodes.size() - 1;
             myNodes.remove(lastIndex);
             final Node ifNode = myNodes.get(lastIndex - 1);
             final Expression condition = ifNode.getCondition();
             ifNode.setCondition(null);
             final TernaryExpression ternaryExpression = new TernaryExpression(condition, getTopOfBodyStack(), getTopOfBodyStack());
             myBodyStack.push(ternaryExpression);
         }
         */
         return myBodyStack.pop();
     }
 
     protected void removeThisVariableFromStack() {
         if (myDecompiledMethod.isNormalClassMethod() && !myBodyStack.isEmpty()
                 && myBodyStack.peek() instanceof Variable && ((Variable) myBodyStack.peek()).getName().equals("this")) {
             myBodyStack.pop();
         }
     }
 
     protected void replaceInvocationsFromExpressionsToStatements() {
         for (final Expression expression : myBodyStack) {
             if (expression instanceof InstanceInvocation) {
                 final InstanceInvocation invocation = (InstanceInvocation) expression;
                 myStatements.add(new com.sdc.ast.controlflow.InstanceInvocation(invocation.getFunction(), invocation.getReturnType(), invocation.getArguments(), invocation.getVariable()));
             } else if (expression instanceof Invocation) {
                 final Invocation invocation = (Invocation) expression;
                 myStatements.add(new com.sdc.ast.controlflow.Invocation(invocation.getFunction(), invocation.getReturnType(), invocation.getArguments()));
             }
         }
     }
 
     protected void appendInstanceInvocation(final String function, final String returnType, final List<Expression> arguments, final Variable variable) {
         if (myBodyStack.isEmpty()) {
             myStatements.add(new com.sdc.ast.controlflow.InstanceInvocation(function, returnType, arguments, variable));
         } else {
             myBodyStack.push(new com.sdc.ast.expressions.InstanceInvocation(function, returnType, arguments, variable));
         }
     }
 
     protected void appendInvocation(final String function, final String returnType, final List<Expression> arguments) {
         if (myBodyStack.isEmpty()) {
             myStatements.add(new com.sdc.ast.controlflow.Invocation(function, returnType, arguments));
         } else {
             myBodyStack.push(new com.sdc.ast.expressions.Invocation(function, returnType, arguments));
         }
     }
 
     protected List<Expression> getInvocationArguments(final String descriptor) {
         List<Expression> arguments = new ArrayList<Expression>();
         for (int i = 0; i < DeclarationWorker.getParametersCount(descriptor); i++) {
             arguments.add(0, getTopOfBodyStack());
         }
         return arguments;
     }
 
     protected String getInvocationReturnType(final String descriptor) {
         final int returnTypeIndex = descriptor.indexOf(')') + 1;
         return getDescriptor(descriptor, returnTypeIndex, myDecompiledMethod.getImports());
     }
 
     protected boolean checkForSuperClassConstructor(final String invocationName) {
         return myDecompiledOwnerFullClassName.endsWith(myDecompiledMethod.getName()) && myDecompiledOwnerSuperClassName.endsWith(invocationName);
     }
 
     protected void processSuperClassConstructorInvocation(final String invocationName, final String returnType, final List<Expression> arguments) {
         myStatements.add(new com.sdc.ast.controlflow.Invocation("super", returnType, arguments));
     }
 
     protected void appendInvocationOrConstructor(final boolean isStaticInvocation, final String visitMethodName,
                                                  final String invocationName, final String returnType, final List<Expression> arguments) {
         if (visitMethodName.equals("<init>")) {
             if (checkForSuperClassConstructor(invocationName)) {
                 removeThisVariableFromStack();
                 processSuperClassConstructorInvocation(invocationName, returnType, arguments);
             } else {
                 if (!myDecompiledMethod.getDecompiledClass().hasAnonymousClass(invocationName)) {
                     myBodyStack.push(new New(new com.sdc.ast.expressions.Invocation(invocationName, returnType, arguments)));
                 } else {
                     myBodyStack.push(new com.sdc.ast.expressions.nestedclasses.AnonymousClass(myDecompiledMethod.getDecompiledClass().getAnonymousClass(invocationName), arguments));
                 }
             }
         } else {
             if (!isStaticInvocation) {
                 removeThisVariableFromStack();
             }
 
             appendInvocation(invocationName, returnType, arguments);
         }
     }
 
     private void multiPush(Expression... expressions) {
         for (Expression expr : expressions) {
             myBodyStack.push(expr);
         }
     }
 
     protected Expression replaceBooleanConstant(final Expression expression) {
         if (expression instanceof Constant && myDecompiledMethod.getReturnType().toLowerCase().contains("boolean")) {
             return new Constant(((Constant) expression).getValue().toString().equals("1"), false);
         } else {
             return expression;
         }
     }
 
     protected String getClassName(final String fullClassName) {
         return myDecompiledMethod.getDecompiledClass().getClassName(fullClassName);
     }
 
     protected String getDescriptor(final String descriptor, final int pos, List<String> imports) {
         return myDecompiledMethod.getDecompiledClass().getDescriptor(descriptor, pos, imports, myLanguage);
     }
 }
