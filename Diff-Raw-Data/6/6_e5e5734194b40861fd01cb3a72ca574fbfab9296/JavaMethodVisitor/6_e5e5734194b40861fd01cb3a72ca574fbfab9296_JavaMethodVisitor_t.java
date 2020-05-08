 package com.sdc.java;
 
 import com.sdc.abstractLangauge.AbstractMethodVisitor;
 import com.sdc.ast.controlflow.*;
 import com.sdc.ast.controlflow.InstanceInvocation;
 import com.sdc.ast.controlflow.Invocation;
 import com.sdc.ast.expressions.*;
 import com.sdc.ast.expressions.identifiers.Field;
 import com.sdc.ast.expressions.identifiers.Identifier;
 import com.sdc.ast.expressions.identifiers.Variable;
 import com.sdc.cfg.ExceptionHandler;
 import com.sdc.cfg.Node;
 import com.sdc.cfg.Switch;
 import com.sdc.cfg.functionalization.AnonymousClass;
 import com.sdc.cfg.functionalization.Generator;
 import org.objectweb.asm.*;
 import org.objectweb.asm.util.Printer;
 
 import java.util.*;
 
 public class JavaMethodVisitor extends AbstractMethodVisitor {
     private JavaClassMethod myJavaClassMethod;
 
     private final String myDecompiledOwnerFullClassName;
 
     private Stack<Expression> myBodyStack = new Stack<Expression>();
     private List<Statement> myStatements = new ArrayList<Statement>();
 
     private List<Node> myNodes = new ArrayList<Node>();
     private List<Label> myLabels = new ArrayList<Label>();
     private Map<Label, List<Integer>> myMap1 = new HashMap<Label, List<Integer>>();  // for GOTO
     private Map<Integer, Label> myMap2 = new HashMap<Integer, Label>(); // for IF ELSE Branch
     private List<Label> myNodeInnerLabels = new ArrayList<Label>();
 
     private boolean myHasDebugInformation = false;
 
     public JavaMethodVisitor(JavaClassMethod javaClassMethod, final String decompiledOwnerFullClassName) {
         super(Opcodes.ASM4, null);
         this.myJavaClassMethod = javaClassMethod;
         this.myDecompiledOwnerFullClassName = decompiledOwnerFullClassName;
     }
 
     @Override
     public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
         return null;
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
         return null;
     }
 
     @Override
     public void visitCode() {
     }
 
     @Override
     public void visitFrame(final int type, final int nLocal, final Object[] local, final int nStack, final Object[] stack) {
         if (type == 2) {
             // F_CHOP
             myJavaClassMethod.setCurrentFrame(getCurrentFrame().getParent());
         } else if (type == 3) {
             // F_SAME
             Frame newFrame = new Frame();
             newFrame.setSameFrame(getCurrentFrame());
             newFrame.setParent(getCurrentFrame().getParent());
             getCurrentFrame().getParent().addChild(newFrame);
 
             myJavaClassMethod.setCurrentFrame(newFrame);
         } else {
             Frame newFrame = new Frame();
 
             newFrame.setParent(getCurrentFrame());
             getCurrentFrame().addChild(newFrame);
 
             myJavaClassMethod.setCurrentFrame(newFrame);
 
             if (nStack > 0) {
                 String stackedVariableType = "";
 
                 if (stack[0] instanceof Integer) {
                     switch ((Integer) stack[0]) {
                         case 1:
                             stackedVariableType = "int ";
                             break;
                         case 2:
                             stackedVariableType = "float ";
                             break;
                         case 3:
                             stackedVariableType = "double ";
                             break;
                         case 4:
                             stackedVariableType = "long ";
                             break;
                     }
                 } else {
                     final String className = (String) stack[0];
                     myJavaClassMethod.addImport(getDecompiledFullClassName(className));
                     stackedVariableType = getClassName(className) + " ";
                 }
 
                 getCurrentFrame().setStackedVariableType(stackedVariableType);
             }
         }
     }
 
     @Override
     public void visitInsn(final int opcode) {
         final String opString = Printer.OPCODES[opcode];
         //System.out.println(opString);
 
         if (opString.contains("ADD") || opString.contains("SUB")
                 || opString.contains("MUL") || opString.contains("DIV")) {
             Expression e1 = getTopOfBodyStack();
             Expression e2 = getTopOfBodyStack();
             Expression res = new BinaryExpression(BinaryExpression.OperationType.valueOf(opString.substring(1)), e2, e1);
             myBodyStack.push(res);
         } else if (opString.contains("NEG")) {
             myBodyStack.push(new UnaryExpression(UnaryExpression.OperationType.NEGATE, getTopOfBodyStack()));
         } else if (opString.contains("CONST_")) {
             myBodyStack.push(new Constant(opString.substring(7)));
         } else if (opString.equals("RETURN")) {
             myStatements.add(new Return());
         } else if (opString.contains("RETURN")) {
             myStatements.add(new Return(getTopOfBodyStack()));
         } else if (opString.contains("CMP")) {
             Expression e1 = getTopOfBodyStack();
             Expression e2 = getTopOfBodyStack();
             myBodyStack.push(new BinaryExpression(e2, e1));
         } else if (opString.contains("ATHROW")) {
             myStatements.add(new Throw(getTopOfBodyStack()));
         } else if (opString.equals("SWAP")) {
             Expression expr1 = myBodyStack.pop();
             Expression expr2 = myBodyStack.pop();
             myBodyStack.push(expr1);
             myBodyStack.push(expr2);
         } else if (opString.equals("DUP") && !myBodyStack.isEmpty()) {
             myBodyStack.push(myBodyStack.peek());
         } else if (opString.equals("DUP_X1")) {
             Expression expr1 = myBodyStack.pop();
             Expression expr2 = myBodyStack.pop();
             myBodyStack.push(expr1);
             myBodyStack.push(expr2);
             myBodyStack.push(expr1);
         }
     }
 
     @Override
     public void visitIntInsn(final int opcode, final int operand) {
         final String opString = Printer.OPCODES[opcode];
 
         if (opString.contains("IPUSH")) {
             myBodyStack.push(new Constant(operand));
         }
     }
 
     @Override
     public void visitVarInsn(final int opcode, final int var) {
         final String opString = Printer.OPCODES[opcode];
         //System.out.println(opString + " " + var);
 
         final boolean currentFrameHasStack = getCurrentFrame().checkStack();
         String variableType = null;
 
         if (opString.contains("ALOAD") && var == 0) {
             return;
         } else if (opString.contains("LOAD")) {
             myBodyStack.push(new Variable(var, getCurrentFrame()));
         } else if (opString.contains("STORE") && !currentFrameHasStack) {
             Identifier v = new Variable(var, getCurrentFrame());
             final Expression expr = getTopOfBodyStack();
             myStatements.add(new Assignment(v, expr));
             if (expr instanceof com.sdc.ast.expressions.Invocation) {
                 variableType = ((com.sdc.ast.expressions.Invocation) expr).getReturnType();
             } else if (expr instanceof New) {
                 variableType = ((New) expr).getReturnType();
             } else if (expr instanceof Variable) {
                 final String name = ((Variable) expr).getName();
                 variableType = name.substring(0, name.lastIndexOf(" ") + 1);
             }
         }
 
         if (!opString.contains("LOAD") && var > myJavaClassMethod.getLastLocalVariableIndex()) {
             final String name = "y" + var;
             myJavaClassMethod.addLocalVariableName(var, name);
 
             String descriptorType;
             if (currentFrameHasStack) {
                 descriptorType = getCurrentFrame().getStackedVariableType();
                 getCurrentFrame().setStackedVariableIndex(var);
             } else {
                 descriptorType = getDescriptor(opString, 0);
             }
 
             if (!descriptorType.equals("Object ") || variableType == null) {
                 variableType = descriptorType;
             }
 
             myJavaClassMethod.addLocalVariableType(var, variableType);
         }
     }
 
     @Override
     public void visitTypeInsn(final int opcode, final String type) {
     }
 
     @Override
     public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
         myBodyStack.push(new Field(name));
     }
 
     @Override
     public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
         final String opString = Printer.OPCODES[opcode];
         //System.out.println(opString + " " + owner + " " + name + " " + desc);
 
         List<Expression> arguments = new ArrayList<Expression>();
         for (int i = 1; i < desc.indexOf(')'); i++) {
             arguments.add(0, getTopOfBodyStack());
         }
 
         final int returnTypeIndex = desc.indexOf(')') + 1;
         String returnType = getDescriptor(desc, returnTypeIndex);
 
         final String decompiledOwnerClassName = getDecompiledFullClassName(owner);
 
         String invocationName = "";
 
         if (opString.contains("INVOKEVIRTUAL") || opString.contains("INVOKEINTERFACE")
                 || (decompiledOwnerClassName.equals(myDecompiledOwnerFullClassName) && !name.equals("<init>"))) {
             if (!myBodyStack.isEmpty() && myBodyStack.peek() instanceof Variable) {
                 Variable v = (Variable) myBodyStack.pop();
                 if (myBodyStack.isEmpty()) {
                     myStatements.add(new InstanceInvocation(name, returnType, arguments, v));
                 } else {
                     myBodyStack.push(new com.sdc.ast.expressions.InstanceInvocation(name, returnType, arguments, v));
                 }
                 return;
             } else {
                 invocationName = name;
             }
         } else if (opString.contains("INVOKESPECIAL")) {
             if (name.equals("<init>")) {
                 myJavaClassMethod.addImport(decompiledOwnerClassName);
                 invocationName = getClassName(owner);
                 returnType = invocationName + " ";
             } else {
                 invocationName = "super." + name;
             }
         } else if (opString.contains("INVOKESTATIC")) {
             myJavaClassMethod.addImport(decompiledOwnerClassName);
             invocationName = getClassName(owner) + "." + name;
         }
 
         if (name.equals("<init>")) {
             myBodyStack.push(new New(new com.sdc.ast.expressions.Invocation(invocationName, returnType, arguments)));
         } else if (myBodyStack.isEmpty()) {
             myStatements.add(new Invocation(invocationName, returnType, arguments));
         } else {
             myBodyStack.push(new com.sdc.ast.expressions.Invocation(invocationName, returnType, arguments));
         }
     }
 
     @Override
     public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
     }
 
     @Override
     public void visitJumpInsn(final int opcode, final Label label) {
         final String opString = Printer.OPCODES[opcode];
         //System.out.println(opString + ": " + label);
         if (opString.contains("IF")) {
             final Label myLastIFLabel = label;
             if (myNodes.isEmpty() || !myNodeInnerLabels.isEmpty() || (myNodes.get(getLeftEmptyNodeIndex() - 1).getCondition() == null)) {
                 myLabels.add(myLastIFLabel);
                 myMap2.put(myNodes.size(), label);
                 applyNode();
                 final int last = myNodes.size() - 1;
                 myNodes.get(last).setCondition(new BinaryExpression(null, null));
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
         if (myLabels.contains(label) && (!myStatements.isEmpty())) {
             applyNode();
             myLabels.remove(label);
         }
         myNodeInnerLabels.add(label);
 
         //System.out.println(label);
     }
 
     @Override
     public void visitLdcInsn(final Object cst) {
         myBodyStack.push(new Constant(cst));
     }
 
     @Override
     public void visitIincInsn(final int var, final int increment) {
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
         Node switch_node = new Switch(myBodyStack.pop(), keys, list);
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
         Node switch_node = new Switch(myBodyStack.pop(), keys, list);
         myNodes.add(switch_node);
     }
 
     @Override
     public void visitMultiANewArrayInsn(final String desc, final int dims) {
     }
 
     @Override
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
     }
 
     @Override
     public void visitLocalVariable(final String name, final String desc,
                                    final String signature, final Label start, final Label end,
                                    final int index) {
         if (!myHasDebugInformation) {
             myHasDebugInformation = true;
         }
 
         final String description = signature != null ? signature : desc;
         myJavaClassMethod.addLocalVariableFromDebugInfo(index, name, getDescriptor(description, 0));
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
         // GOTO
         for (final Label lbl : myMap1.keySet()) {
             for (final Node node : myNodes) {
                 if (node.containsLabel(lbl)) {
                     for (final Integer i : myMap1.get(lbl)) {
                         myNodes.get(i).addTail(node);
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
                             break;
                         }
                     }
                 }
             } else if (node.getListOfTails().isEmpty() && !node.isLastStatementReturn()) {
                 node.addTail(myNodes.get(i + 1));
             }
         }
         // IF ELSE Branch
         for (final Integer index : myMap2.keySet()) {
             for (final Node node : myNodes) {
                 if (node.containsLabel(myMap2.get(index))) {
                     myNodes.get(index).addTail(node);
                     break;
                 }
             }
         }
 
         Generator generator = new Generator(myNodes);
         AnonymousClass aClass = generator.genAnonymousClass();
         // myJavaClassMethod.setAnonymousClass(aClass);
         myJavaClassMethod.setBody(myStatements);
         myJavaClassMethod.setNodes(myNodes);
         //myJavaClassMethod.drawCFG();
     }
 
     private Integer getLeftEmptyNodeIndex() {
         for (Node node : myNodes) {
             if (node.statementsIsEmpty() && !node.isEmpty()) {
                 return myNodes.indexOf(node);
             }
         }
         return myNodes.size();
     }
 
     private void applyNode() {
         Integer i = getLeftEmptyNodeIndex();
         if (i != myNodes.size()) {
             myNodes.get(i).setStatements(new ArrayList<Statement>(myStatements));
             myNodes.get(i).setInnerLabels(new ArrayList<Label>(myNodeInnerLabels));
             if (myNodes.get(i).getStatements().isEmpty()) {
                 myNodes.get(i).setEmpty(true);
             }
         } else {
             Node node = new Node(new ArrayList<Statement>(myStatements), new ArrayList<Label>(myNodeInnerLabels));
             if (node.getStatements().isEmpty()) {
                 node.setEmpty(true);
             }
             myNodes.add(node);
         }
         myNodeInnerLabels.clear();
        // myStatements.clear();
     }
 
     private String getDescriptor(final String descriptor, final int pos) {
         switch (descriptor.charAt(pos)) {
             case 'B':
                 return "byte ";
             case 'J':
                 return "long ";
             case 'Z':
                 return "boolean ";
             case 'I':
                 return "int ";
             case 'S':
                 return "short ";
             case 'C':
                 return "char ";
             case 'F':
                 return "float ";
             case 'D':
                 return "double ";
             case 'L':
                 if (!descriptor.contains("<")) {
                     final String className = descriptor.substring(pos + 1, descriptor.indexOf(";", pos));
                     myJavaClassMethod.addImport(getDecompiledFullClassName(className));
                     return getClassName(className) + " ";
                 } else {
                     final String className = descriptor.substring(pos + 1, descriptor.indexOf("<", pos));
                     final String[] genericList = descriptor.substring(descriptor.indexOf("<") + 1, descriptor.indexOf(">")).split(";");
                    myJavaClassMethod.addImport(getDecompiledFullClassName(className));
 
                     StringBuilder result = new StringBuilder(getClassName(className));
                     boolean isSingleType = true;
 
                     result.append("<");
                     for (final String generic : genericList) {
                         if (!isSingleType) {
                             result.append(", ");
                         }
                         isSingleType = false;
 
                         final String genericName = getDescriptor(generic + ";", 0).trim();
                         result.append(genericName);
                     }
                     result.append("> ");
 
                     return result.toString();
                 }
             case 'T':
                 return descriptor.substring(pos + 1, descriptor.indexOf(";", pos)) + " ";
            case '+':
                return "? extends " + getDescriptor(descriptor, pos + 1);
            case '-':
                return "? super " + getDescriptor(descriptor, pos + 1);
             default:
                 return "Object ";
         }
     }
 
     private Expression getTopOfBodyStack() {
         if (myBodyStack.isEmpty()) {
             final int lastIndex = myStatements.size() - 1;
             final Statement lastStatement = myStatements.get(lastIndex);
 
             if (lastStatement instanceof InstanceInvocation) {
                 InstanceInvocation invoke = (InstanceInvocation) lastStatement;
                 myStatements.remove(lastIndex);
                 return new com.sdc.ast.expressions.InstanceInvocation(invoke.getFunction(), invoke.getReturnType(), invoke.getArguments(), invoke.getVariable());
             } else if (lastStatement instanceof Invocation) {
                 Invocation invoke = (Invocation) lastStatement;
                 myStatements.remove(lastIndex);
                 return new com.sdc.ast.expressions.Invocation(invoke.getFunction(), invoke.getReturnType(), invoke.getArguments());
             } else if (lastStatement instanceof Assignment) {
                 return ((Assignment) lastStatement).getRight();
             }
         } else if ((myNodes.size() > 1) && (myBodyStack.size() >= 2) && (myNodes.get(myNodes.size() - 2).getCondition() != null)) {
             final int lastIndex = myNodes.size() - 1;
             myNodes.remove(lastIndex);
             final Node ifNode = myNodes.get(lastIndex - 1);
             final Expression condition = ifNode.getCondition();
             ifNode.setCondition(null);
             final TernaryExpression ternaryExpression = new TernaryExpression(condition, getTopOfBodyStack(), getTopOfBodyStack());
             myBodyStack.push(ternaryExpression);
         }
         return myBodyStack.pop();
     }
 
     private String getClassName(final String fullClassName) {
         final String[] classParts = fullClassName.split("/");
         return classParts[classParts.length - 1];
     }
 
     private String getDecompiledFullClassName(final String fullClassName) {
         return fullClassName.replace("/", ".");
     }
 
     private Frame getCurrentFrame() {
         return myJavaClassMethod.getCurrentFrame();
     }
 }
