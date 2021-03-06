 package loop;
 
 import loop.ast.Assignment;
 import loop.ast.BinaryOp;
 import loop.ast.BooleanLiteral;
 import loop.ast.Call;
 import loop.ast.CallArguments;
 import loop.ast.CallChain;
 import loop.ast.ClassDecl;
 import loop.ast.Comprehension;
 import loop.ast.Computation;
 import loop.ast.ConstructorCall;
 import loop.ast.DestructuringPair;
 import loop.ast.Guard;
 import loop.ast.IndexIntoList;
 import loop.ast.InlineListDef;
 import loop.ast.InlineMapDef;
 import loop.ast.IntLiteral;
 import loop.ast.JavaLiteral;
 import loop.ast.ListDestructuringPattern;
 import loop.ast.ListStructurePattern;
 import loop.ast.MapPattern;
 import loop.ast.Node;
 import loop.ast.OtherwiseGuard;
 import loop.ast.PatternRule;
 import loop.ast.PrivateField;
 import loop.ast.RegexLiteral;
 import loop.ast.StringLiteral;
 import loop.ast.StringPattern;
 import loop.ast.TernaryExpression;
 import loop.ast.TypeLiteral;
 import loop.ast.Variable;
 import loop.ast.WildcardPattern;
 import loop.ast.script.ArgDeclList;
 import loop.ast.script.FunctionDecl;
 import loop.ast.script.ModuleDecl;
 import loop.ast.script.Unit;
 import loop.runtime.Scope;
 import org.objectweb.asm.ClassReader;
 import org.objectweb.asm.ClassWriter;
 import org.objectweb.asm.Label;
 import org.objectweb.asm.MethodVisitor;
 import org.objectweb.asm.Opcodes;
 import org.objectweb.asm.util.TraceClassVisitor;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 import java.util.TreeMap;
 import java.util.concurrent.atomic.AtomicInteger;
 
 /**
  * @author dhanji@gmail.com (Dhanji R. Prasanna)
  */
 @SuppressWarnings({"FieldCanBeLocal"}) class AsmCodeEmitter implements Opcodes {
   private static final AtomicInteger functionNameSequence = new AtomicInteger();
 
   private static final String IS_LIST_VAR_PREFIX = "__$isList_";
   private static final String RUNTIME_LIST_SIZE_VAR_PREFIX = "__$runtimeListSize_";
   private static final String RUNTIME_STR_LEN_PREFIX = "__$str_len_";
   private static final String IS_STRING_PREFIX = "__$isStr_";
   private static final String IS_READER_PREFIX = "__$isRdr_";
   private static final String WHERE_SCOPE_FN_PREFIX = "$wh$";
 
   private StringBuilder out = new StringBuilder();
   private final Stack<Context> functionStack = new Stack<Context>();
 
   // Java line and column to map back to our Loop AST.
   private int line;
   private int column;
   private final TreeMap<SourceLocation, Node> emittedNodeMap = new TreeMap<SourceLocation, Node>();
   private final Scope scope;
 
   public static class SourceLocation implements Comparable<SourceLocation> {
     public final int line;
     public final int column;
 
     public SourceLocation(int line, int column) {
       this.line = line;
       this.column = column;
     }
 
     @Override
     public int compareTo(SourceLocation that) {
       return (this.line * 1000000 + this.column) - (that.line * 1000000 + that.column);
     }
 
     @Override
     public boolean equals(Object o) {
       if (this == o) return true;
       if (o == null || getClass() != o.getClass()) return false;
 
       SourceLocation that = (SourceLocation) o;
 
       return column == that.column && line == that.line;
 
     }
 
     @Override
     public int hashCode() {
       return 31 * line + column;
     }
   }
 
   private final Map<Class<?>, Emitter> EMITTERS = new HashMap<Class<?>, Emitter>();
 
   AsmCodeEmitter(Scope scope) {
     this.scope = scope;
 
     EMITTERS.put(Call.class, callEmitter);
     EMITTERS.put(Computation.class, computationEmitter);
     EMITTERS.put(IntLiteral.class, intEmitter);
     EMITTERS.put(BooleanLiteral.class, booleanEmitter);
     EMITTERS.put(TypeLiteral.class, typeLiteralEmitter);
     EMITTERS.put(Variable.class, variableEmitter);
     EMITTERS.put(JavaLiteral.class, javaLiteralEmitter);
     EMITTERS.put(BinaryOp.class, binaryOpEmitter);
     EMITTERS.put(StringLiteral.class, stringLiteralEmitter);
     EMITTERS.put(RegexLiteral.class, regexLiteralEmitter);
     EMITTERS.put(Assignment.class, assignmentEmitter);
     EMITTERS.put(InlineMapDef.class, inlineMapEmitter);
     EMITTERS.put(InlineListDef.class, inlineListEmitter);
     EMITTERS.put(IndexIntoList.class, indexIntoListEmitter);
     EMITTERS.put(CallChain.class, callChainEmitter);
     EMITTERS.put(FunctionDecl.class, functionDeclEmitter);
     EMITTERS.put(ArgDeclList.class, argDeclEmitter);
     EMITTERS.put(PrivateField.class, privateFieldEmitter);
     EMITTERS.put(PatternRule.class, patternRuleEmitter);
     EMITTERS.put(TernaryExpression.class, ternaryExpressionEmitter);
     EMITTERS.put(Comprehension.class, comprehensionEmitter);
     EMITTERS.put(ConstructorCall.class, constructorCallEmitter);
   }
 
   private final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
   private final Stack<MethodVisitor> methodStack = new Stack<MethodVisitor>();
 
   public Class<?> write(Unit unit) {
     return write(unit, false);
   }
 
   public Class<?> write(Unit unit, boolean print) {
     Thread.currentThread().setContextClassLoader(LoopClassLoader.CLASS_LOADER);
 
     // We always emit functions as static into a containing Java class.
     String javaClass = unit.name();
     classWriter.visit(V1_6, ACC_PUBLIC, javaClass, null, "java/lang/Object", new String[0]);
 
     for (FunctionDecl functionDecl : unit.functions()) {
       emit(functionDecl);
     }
 
     classWriter.visitEnd();
 
     if (print) {
       try {
         new ClassReader(new ByteArrayInputStream(classWriter.toByteArray())).accept(
             new TraceClassVisitor(new PrintWriter(System.out)), ClassReader.SKIP_DEBUG);
       } catch (IOException e) {
         e.printStackTrace();
       }
     }
 
     LoopClassLoader.CLASS_LOADER.put(javaClass, classWriter.toByteArray());
     try {
       return LoopClassLoader.CLASS_LOADER.findClass(javaClass);
     } catch (ClassNotFoundException e) {
       throw new RuntimeException(e);
     }
   }
 
   public Class<?> write(FunctionDecl node) {
     Unit unit = new Unit(ModuleDecl.DEFAULT);
     unit.declare(node);
     unit.reduceAll();
 
     return write(unit);
   }
 
 
   public Class<?> write(Node node) {
     // We don't really emit classes.
     if (node instanceof ClassDecl)
       return null;
     else if (node instanceof FunctionDecl)
       return write((FunctionDecl)node);
 
     throw new UnsupportedOperationException("Can't compile nodes of type " + node);
   }
 
   private AsmCodeEmitter append(String str) {
     if (null == str)
       return this;
     out.append(str);
     if (str.contains("\n")) {
       line++;
       column = 0;
     } else
       column += str.length();
 
     return this;
   }
 
   private AsmCodeEmitter append(char c) {
     out.append(c);
     if (c == '\n') {
       line++;
       column = 0;
     } else
       column++;
 
     return this;
   }
 
   private AsmCodeEmitter append(int n) {
     out.append(n);
     column++;
     return this;
   }
 
   private void trackLineAndColumn(Node node) {
     emittedNodeMap.put(new SourceLocation(line, column), node);
   }
 
   private void emitChildren(Node node) {
     for (Node child : node.children()) {
       emit(child);
     }
   }
 
   private void emitOnlyChild(Node node) {
     emit(node.children().get(0));
   }
 
   public void emit(Node node) {
     if (!EMITTERS.containsKey(node.getClass()))
       throw new RuntimeException("Missing emitter for " + node.getClass().getSimpleName());
     EMITTERS.get(node.getClass()).emitCode(node);
   }
 
   public void emitTo(Node node, StringBuilder alt) {
     StringBuilder tmp = out;
     out = alt;
     emit(node);
     out = tmp;
   }
 
   public TreeMap<SourceLocation, Node> getEmittedNodeMap() {
     return emittedNodeMap;
   }
 
   // -------------------------------------------------------------------
   // EMITTERS ----------------------------------------------------------
   // -------------------------------------------------------------------
 
   private final Emitter ternaryExpressionEmitter = new Emitter() {
     @Override
     public void emitCode(Node node) {
       TernaryExpression expression = (TernaryExpression) node;
       MethodVisitor methodVisitor = methodStack.peek();
 
       Label elseBranch = new Label();
       Label end = new Label();
 
       // If condition
       emit(expression.children().get(0));
       methodVisitor.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
       methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
       methodVisitor.visitJumpInsn(IFEQ, elseBranch);
 
       emit(expression.children().get(1));
       methodVisitor.visitJumpInsn(GOTO, end);
 
       methodVisitor.visitLabel(elseBranch);
       emit(expression.children().get(2));
       methodVisitor.visitLabel(end);
     }
   };
 
   private final Emitter computationEmitter = new Emitter() {
     @Override
     public void emitCode(Node node) {
       trackLineAndColumn(node);
       emitChildren(node);
     }
   };
 
   private final Emitter callEmitter = new Emitter() {
     @Override
     public void emitCode(Node node) {
       Call call = (Call) node;
       trackLineAndColumn(call);
 
       MethodVisitor methodVisitor = methodStack.peek();
       Context context = functionStack.peek();
       FunctionDecl resolvedFunction = scope.resolveFunction(call.name());
       boolean isStatic = resolvedFunction != null, isClosure = false;
 
       // The parse-tree knows if we are calling a java method statically.
       if (!isStatic)
         isStatic = call.isJavaStatic();
 
       // This is a special invocation so we emit it without the dot.
       String name;
       if ("@call".equals(call.name())) {
         name = "";
 
         isStatic = true;
         isClosure = true;
       } else if (isStatic && resolvedFunction != null) {
         name = normalizeMethodName(resolvedFunction.scopedName());
       } else
         name = normalizeMethodName(call.name());
 
       // Compute if we should "call as postfix method"
       List<Node> arguments = call.args() == null ? Collections.<Node>emptyList() : call.args().children();
       int callAsPostfixVar = -1;
 
       boolean callAsPostfix = false;
       int argSize = arguments.size();
       if (resolvedFunction != null && resolvedFunction.arguments() != null) {
         // The actual call is 1 less argument than the function takes, so this is a
         // "call-as-method" syntax.
         if (resolvedFunction.arguments().children().size() - argSize == 1) {
           callAsPostfix = true;
           callAsPostfixVar = context.localVarIndex(context.newLocalVariable());
           argSize++;
 
           // Save the top of the stack for use as the first argument.
           methodVisitor.visitVarInsn(ASTORE, callAsPostfixVar);
         }
       }
 
       // push name of containing type if this is a static call.
       if (isStatic && !call.isJavaStatic()) {
         if (isClosure)
           methodVisitor.visitTypeInsn(CHECKCAST, "loop/runtime/Closure");
 
         methodVisitor.visitLdcInsn(scope.getModuleName());
       }
 
       // push method name onto stack
       if (!isClosure)
         methodVisitor.visitLdcInsn(name);
 
       if (argSize > 0) {
         int arrayVar = context.localVarIndex(context.newLocalVariable());
 
         // push args as array.
         methodVisitor.visitIntInsn(BIPUSH, argSize);       // size of array
         methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/Object");
         methodVisitor.visitVarInsn(ASTORE, arrayVar);
         int i = 0;
 
         if (callAsPostfix) {
           methodVisitor.visitVarInsn(ALOAD, arrayVar);          // array
           methodVisitor.visitIntInsn(BIPUSH, i);                // index
           methodVisitor.visitVarInsn(ALOAD, callAsPostfixVar);  // value
           methodVisitor.visitInsn(AASTORE);
 
           i++;
         }
 
         for (Node arg : arguments) {
           methodVisitor.visitVarInsn(ALOAD, arrayVar);      // array
           methodVisitor.visitIntInsn(BIPUSH, i);            // index
           emit(arg);                                        // value
 
           methodVisitor.visitInsn(AASTORE);
           i++;
         }
 
         // Load the array back in.
         methodVisitor.visitVarInsn(ALOAD, arrayVar);
 
         if (isStatic) {
           // If JDK7, use invokedynamic instead for better performance.
           if (isClosure)
             methodVisitor.visitMethodInsn(INVOKESTATIC, "loop/runtime/Caller", "callClosure",
                 "(Lloop/runtime/Closure;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;");
           else
             methodVisitor.visitMethodInsn(INVOKESTATIC, "loop/runtime/Caller", "callStatic",
                 "(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;");
 
         } else {
           // If JDK7, use invokedynamic instead for better performance.
           methodVisitor.visitMethodInsn(INVOKESTATIC, "loop/runtime/Caller", "call",
               "(Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;");
         }
       } else {
         if (isStatic) {
           // If JDK7, use invokedynamic instead for better performance.
           if (isClosure)
             methodVisitor.visitMethodInsn(INVOKESTATIC, "loop/runtime/Caller", "callClosure",
                 "(Lloop/runtime/Closure;Ljava/lang/String;)Ljava/lang/Object;");
           else
             methodVisitor.visitMethodInsn(INVOKESTATIC, "loop/runtime/Caller", "callStatic",
                 "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;");
 
         } else {
           // If JDK7, use invokedynamic instead for better performance.
           methodVisitor.visitMethodInsn(INVOKESTATIC, "loop/runtime/Caller", "call",
               "(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;");
         }
       }
     }
   };
 
   private final Emitter constructorCallEmitter = new Emitter() {
     @Override
     public void emitCode(Node node) {
       ConstructorCall call = (ConstructorCall) node;
       MethodVisitor methodVisitor = methodStack.peek();
       Context context = functionStack.peek();
 
       // Resolve a loop type internally. Note that this makes dynamic linking
       // of Loop types impossible, but we CAN link Java binaries dynamically.
       ClassDecl classDecl = scope.resolve(call.name);
       if (classDecl != null) {
 
         // Instatiate the loop object first. With the correct type
         int objectVar = context.localVarIndex(context.newLocalVariable());
 
         methodVisitor.visitTypeInsn(NEW, "loop/lang/LoopObject");
         methodVisitor.visitInsn(DUP);
 
         methodVisitor.visitTypeInsn(NEW, "loop/lang/LoopClass");
         methodVisitor.visitInsn(DUP);
         methodVisitor.visitLdcInsn(classDecl.name);
         methodVisitor.visitMethodInsn(INVOKESPECIAL, "loop/lang/LoopClass", "<init>",
             "(Ljava/lang/String;)V");
 
         methodVisitor.visitMethodInsn(INVOKESPECIAL, "loop/lang/LoopObject", "<init>",
             "(Lloop/lang/LoopClass;)V");
         methodVisitor.visitVarInsn(ASTORE, objectVar);
 
 
         Map<String, Node> fields = new HashMap<String, Node>();
         for (Node nodeAssign : classDecl.children()) {
           if (nodeAssign instanceof Assignment) {
             Assignment assignment = (Assignment) nodeAssign;
             fields.put(((Variable) assignment.lhs()).name, assignment.rhs());
           }
         }
 
         List<Node> children = call.args().children();
         if (!children.isEmpty() || !fields.isEmpty()) {
 
           // First emit named-args as overrides of defaults.
           for (Node child : children) {
             CallArguments.NamedArg arg = (CallArguments.NamedArg) child;
 
             methodVisitor.visitVarInsn(ALOAD, objectVar);
             methodVisitor.visitLdcInsn(arg.name);
             emit(arg.arg);
             methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put",
                 "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
           }
 
           // Now emit any remaining defaults.
           for (Map.Entry<String, Node> field : fields.entrySet()) {
             methodVisitor.visitVarInsn(ALOAD, objectVar);
             methodVisitor.visitLdcInsn(field.getKey());
             emit(field.getValue());
             methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put",
                 "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
           }
         }
 
         // Leave the object on the stack.
         methodVisitor.visitVarInsn(ALOAD, objectVar);
 
       } else {
         // Emit Java constructor call.
         String javaType;
         if (call.modulePart != null)
           javaType = call.modulePart + call.name;
         else
           javaType = scope.resolveJavaType(call.name);
 
         boolean isNullary = call.args().children().isEmpty();
         if (!isNullary) {
          int arrayIndex = context.localVarIndex(context.newLocalVariable());
          methodVisitor.visitIntInsn(BIPUSH, arrayIndex);       // size of array
           methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/Object");
          methodVisitor.visitVarInsn(ASTORE, arrayIndex);
 
           int i = 0;
           for (Node arg : call.args().children()) {
            methodVisitor.visitVarInsn(ALOAD, arrayIndex);    // array
             methodVisitor.visitIntInsn(BIPUSH, i);            // index
             emit(arg);                                        // value
 
             methodVisitor.visitInsn(AASTORE);
             i++;
           }
 
           // push type and constructor arg array.
           methodVisitor.visitLdcInsn(javaType);
          methodVisitor.visitVarInsn(ALOAD, arrayIndex);
 
           methodVisitor.visitMethodInsn(INVOKESTATIC, "loop/runtime/Caller", "instantiate",
               "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;");
         } else {
           // Otherwise invoke the Java constructor directly. We don't need to resolve it.
           // This is an optimization.
           javaType = javaType.replace('.', '/');
           methodVisitor.visitTypeInsn(NEW, javaType);
           methodVisitor.visitInsn(DUP);
           methodVisitor.visitMethodInsn(INVOKESPECIAL, javaType, "<init>", "()V");
         }
       }
     }
   };
 
   private final Emitter binaryOpEmitter = new Emitter() {
     @Override
     public void emitCode(Node node) {
       BinaryOp binaryOp = (BinaryOp) node;
       emitOnlyChild(binaryOp);
 
       MethodVisitor methodVisitor = methodStack.peek();
       switch (binaryOp.operator.kind) {
         case PLUS:
           methodVisitor.visitMethodInsn(INVOKESTATIC, "loop/runtime/Operations", "plus",
               "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
           break;
         case MINUS:
           methodVisitor.visitMethodInsn(INVOKESTATIC, "loop/runtime/Operations", "minus",
               "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
           break;
         case STAR:
           methodVisitor.visitMethodInsn(INVOKESTATIC, "loop/runtime/Operations", "multiply",
               "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
           break;
         case DIVIDE:
           methodVisitor.visitMethodInsn(INVOKESTATIC, "loop/runtime/Operations", "divide",
               "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
           break;
         case MODULUS:
           methodVisitor.visitMethodInsn(INVOKESTATIC, "loop/runtime/Operations", "remainder",
               "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
           break;
         case LESSER:
           methodVisitor.visitMethodInsn(INVOKESTATIC, "loop/runtime/Operations", "lesserThan",
               "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Boolean;");
           break;
         case LEQ:
           methodVisitor.visitMethodInsn(INVOKESTATIC, "loop/runtime/Operations", "lesserThanOrEqual",
               "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Boolean;");
           break;
         case GREATER:
           methodVisitor.visitMethodInsn(INVOKESTATIC, "loop/runtime/Operations", "greaterThan",
               "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Boolean;");
           break;
         case GEQ:
           methodVisitor.visitMethodInsn(INVOKESTATIC, "loop/runtime/Operations",
               "greaterThanOrEqual",
               "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Boolean;");
           break;
         case EQUALS:
           methodVisitor.visitMethodInsn(INVOKESTATIC, "loop/runtime/Operations", "equal",
               "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Boolean;");
           break;
         case AND:
           methodVisitor.visitInsn(IAND);
           break;
         case OR:
           methodVisitor.visitInsn(IOR);
           break;
         case NOT:
           methodVisitor.visitMethodInsn(INVOKESTATIC, "loop/runtime/Operations", "notEqual",
               "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Boolean;");
           break;
         default:
           throw new UnsupportedOperationException(
               "Unsupported binary operator " + binaryOp.toSymbol());
       }
     }
   };
 
   private final Emitter assignmentEmitter = new Emitter() {
     @Override
     public void emitCode(Node node) {
       Assignment assignment = (Assignment) node;
       trackLineAndColumn(assignment);
       Context context = functionStack.peek();
 
       int lhsVar = context.localVarIndex(context.newLocalVariable((Variable) assignment.lhs()));
       emit(assignment.rhs());
       methodStack.peek().visitVarInsn(ASTORE, lhsVar);
     }
   };
 
   private final Emitter variableEmitter = new Emitter() {
     @Override
     public void emitCode(Node node) {
       Variable var = (Variable) node;
 
       Context context = functionStack.peek();
       Integer index = context.argumentIndex.get(var.name);
       if (index == null)
         index = context.localVarIndex(var.name);
 
       // It could be a function-reference. In which case emit it as a closure.
       FunctionDecl functionDecl = scope.resolveFunction(var.name);
       if (functionDecl != null) {
         MethodVisitor methodVisitor = methodStack.peek();
         methodVisitor.visitTypeInsn(NEW, "loop/runtime/Closure");
         methodVisitor.visitInsn(DUP);
         methodVisitor.visitLdcInsn(functionDecl.scopedName());
         methodVisitor.visitMethodInsn(INVOKESPECIAL, "loop/runtime/Closure", "<init>", "(Ljava/lang/String;)V");
 
       } else
         methodStack.peek().visitVarInsn(ALOAD, index);
     }
   };
 
   private static String normalizeMethodName(String name) {
     return name.replaceFirst("@", "__");
   }
 
   private final Emitter intEmitter = new Emitter() {
     @Override
     public void emitCode(Node node) {
       IntLiteral intLiteral = (IntLiteral) node;
 
       // Emit int wrappers.
       MethodVisitor methodVisitor = methodStack.peek();
       methodVisitor.visitLdcInsn(intLiteral.value);
       methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf",
           "(I)Ljava/lang/Integer;");
 
     }
   };
 
   private final Emitter booleanEmitter = new Emitter() {
     @Override
     public void emitCode(Node node) {
       BooleanLiteral booleanLiteral = (BooleanLiteral) node;
 
       // Emit int wrappers.
       MethodVisitor methodVisitor = methodStack.peek();
       methodVisitor.visitIntInsn(BIPUSH, booleanLiteral.value ? 1 : 0);
       methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf",
           "(Z)Ljava/lang/Boolean;");
 
     }
   };
 
   private final Emitter typeLiteralEmitter = new Emitter() {
     @Override
     public void emitCode(Node node) {
       TypeLiteral type = (TypeLiteral) node;
       trackLineAndColumn(type);
       append(type.name);
     }
   };
 
   private final Emitter javaLiteralEmitter = new Emitter() {
     @Override
     public void emitCode(Node node) {
       JavaLiteral java = (JavaLiteral) node;
       trackLineAndColumn(java);
 
       MethodVisitor methodVisitor = methodStack.peek();
 
       if (java.staticFieldAccess == null)
         methodVisitor.visitLdcInsn(java.value);
       else {
         methodVisitor.visitLdcInsn(java.value);
         methodVisitor.visitLdcInsn(java.staticFieldAccess);
         methodVisitor.visitMethodInsn(INVOKESTATIC, "loop/runtime/Caller", "getStatic",
             "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;");
       }
     }
   };
 
   private final Emitter stringLiteralEmitter = new Emitter() {
     @Override
     public void emitCode(Node node) {
       StringLiteral string = (StringLiteral) node;
       trackLineAndColumn(string);
 
       if (string.parts != null) {
         MethodVisitor methodVisitor = methodStack.peek();
 
         // Emit stringbuilder for string interpolation.
         methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder");
         methodVisitor.visitInsn(DUP);
         methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V");
 
         for (Node part : string.parts) {
           if (part instanceof StringLiteral)
             methodVisitor.visitLdcInsn(((StringLiteral) part).value);
           else
             emit(part);
 
           methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
               "(Ljava/lang/Object;)Ljava/lang/StringBuilder;");
         }
 
         methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString",
             "()Ljava/lang/String;");
 
       } else {
         methodStack.peek().visitLdcInsn(string.value.substring(1, string.value.length() - 1));
       }
     }
   };
 
   private final Emitter regexLiteralEmitter = new Emitter() {
     @Override
     public void emitCode(Node node) {
       RegexLiteral regex = (RegexLiteral) node;
       append('"').append(regex.value).append('"');
     }
   };
 
   private final Emitter functionDeclEmitter = new Emitter() {
     @Override
     public void emitCode(Node node) {
       FunctionDecl functionDecl = (FunctionDecl) node;
       String name = functionDecl.scopedName();
       boolean isClosure = functionDecl.isAnonymous();
       if (isClosure) {
         // Function is anonymous, generate a globally unique name for it.
         name = "$fn_" + functionNameSequence.incrementAndGet();
       }
       Context innerContext = new Context(functionDecl);
 
       // Before we emit the body of this method into the class scope, let's
       // see if this is closure, and if it is, emit it as a function reference.
       List<Variable> freeVariables = null;
       if (isClosure) {
         MethodVisitor currentVisitor = methodStack.peek();
 
         // Discover any free variables and save them to this closure.
         freeVariables = new ArrayList<Variable>();
         detectFreeVariables(functionDecl, functionDecl.arguments(), freeVariables);
         functionDecl.freeVariables = freeVariables;
 
         // Add them to the argument list of the function.
         for (Variable freeVariable : freeVariables) {
           functionDecl.arguments().add(new ArgDeclList.Argument(freeVariable.name, null));
         }
 
         currentVisitor.visitTypeInsn(NEW, "loop/runtime/Closure");
         currentVisitor.visitInsn(DUP);
         currentVisitor.visitLdcInsn(name);
 
         if (!freeVariables.isEmpty()) {
           Context outerContext = functionStack.peek();
           int arrayIndex = outerContext.localVarIndex(outerContext.newLocalVariable());
 
           // push free variables as array.
           currentVisitor.visitIntInsn(BIPUSH, freeVariables.size());       // size of array
           currentVisitor.visitTypeInsn(ANEWARRAY, "java/lang/Object");
           currentVisitor.visitVarInsn(ASTORE, arrayIndex);
           int i = 0;
           for (Variable freeVariable : freeVariables) {
             currentVisitor.visitVarInsn(ALOAD, arrayIndex);    // array
             currentVisitor.visitIntInsn(BIPUSH, i);            // index
             emit(freeVariable);                                // value
 
             currentVisitor.visitInsn(AASTORE);
             i++;
           }
 
           // Load the array back in.
           currentVisitor.visitVarInsn(ALOAD, arrayIndex);
           currentVisitor.visitMethodInsn(INVOKESPECIAL, "loop/runtime/Closure", "<init>", "(Ljava/lang/String;[Ljava/lang/Object;)V");
         } else
           currentVisitor.visitMethodInsn(INVOKESPECIAL, "loop/runtime/Closure", "<init>", "(Ljava/lang/String;)V");
       }
 
       // Start writing this function in its own scope.
       StringBuilder args = new StringBuilder("(");
       List<Node> children = functionDecl.arguments().children();
       for (int functionArgIndex = 0, childrenSize = children.size(); functionArgIndex < childrenSize; functionArgIndex++) {
         Node arg = children.get(functionArgIndex);
         String argName = ((ArgDeclList.Argument) arg).name();
         innerContext.arguments.add(argName);
         innerContext.argumentIndex.put(argName, functionArgIndex);
 
         args.append("Ljava/lang/Object;");
       }
       args.append(")");
       functionStack.push(innerContext);
       scope.pushScope(innerContext);
 
       final MethodVisitor methodVisitor = classWriter.visitMethod(
           (functionDecl.isPrivate ? ACC_PRIVATE : ACC_PUBLIC) + ACC_STATIC,
           normalizeMethodName(name),
           args.append("Ljava/lang/Object;").toString(),
           null,
           null);
       methodStack.push(methodVisitor);
 
       // Emit locally-scoped helper functions and variables.
       for (Node helper : functionDecl.whereBlock) {
         // Rewrite helper functions to be namespaced inside the parent function.
         if (helper instanceof FunctionDecl)
           scopeNestedFunction(functionDecl, innerContext, (FunctionDecl) helper);
         emit(helper);
       }
 
       // Set up some helper local vars to make it easier to pattern match certain types (lists).
       if (functionDecl.patternMatching) {
         boolean checkIfLists = false, checkIfString = false;
         for (Node child : functionDecl.children()) {
           PatternRule patternRule = (PatternRule) child;
 
           for (Node pattern : patternRule.patterns) {
             if (pattern instanceof ListDestructuringPattern ||
                 pattern instanceof ListStructurePattern) {
               checkIfLists = true;
             } else if (pattern instanceof StringPattern) {
               checkIfString = true;
             }
           }
         }
 
         if (checkIfLists) {
           List<Node> children1 = functionDecl.arguments().children();
           for (int i = 0, children1Size = children1.size(); i < children1Size; i++) {
             int isList = innerContext.newLocalVariable(IS_LIST_VAR_PREFIX + i);
             int runtimeListSize = innerContext.newLocalVariable(RUNTIME_LIST_SIZE_VAR_PREFIX + i);
 
             methodVisitor.visitVarInsn(ALOAD, i);
             methodVisitor.visitTypeInsn(INSTANCEOF, "java/util/List");
             methodVisitor.visitIntInsn(ISTORE, isList);
 
             // Initialize register for local var.
             methodVisitor.visitIntInsn(BIPUSH, -1);
             methodVisitor.visitIntInsn(ISTORE, runtimeListSize);
 
             Label notAList = new Label();
             methodVisitor.visitIntInsn(ILOAD, isList);
             methodVisitor.visitJumpInsn(IFEQ, notAList);
 
             methodVisitor.visitVarInsn(ALOAD, i);
             methodVisitor.visitTypeInsn(CHECKCAST, "java/util/List");
             methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "size", "()I");
             methodVisitor.visitIntInsn(ISTORE, runtimeListSize);
             methodVisitor.visitLabel(notAList);
           }
         }
         if (checkIfString) {
           for (int i = 0, childrenSize = children.size(); i < childrenSize; i++) {
             int isString = innerContext.newLocalVariable(IS_STRING_PREFIX + i);
             int isReader = innerContext.newLocalVariable(IS_READER_PREFIX + i);
             int runtimeStringLen = innerContext.newLocalVariable(RUNTIME_STR_LEN_PREFIX + i);
 
             // Initialize all local vars we're going to use.
             methodVisitor.visitIntInsn(BIPUSH, 0);
             methodVisitor.visitVarInsn(ISTORE, isString);
             methodVisitor.visitIntInsn(BIPUSH, 0);
             methodVisitor.visitVarInsn(ISTORE, isReader);
             methodVisitor.visitIntInsn(BIPUSH, -1);
             methodVisitor.visitVarInsn(ISTORE, runtimeStringLen);
 
             methodVisitor.visitVarInsn(ALOAD, i);
             methodVisitor.visitInsn(DUP);
             methodVisitor.visitTypeInsn(INSTANCEOF, "java/lang/String");
             methodVisitor.visitIntInsn(ISTORE, isString);
             methodVisitor.visitTypeInsn(INSTANCEOF, "java/io/Reader");
             methodVisitor.visitIntInsn(ISTORE, isReader);
             methodVisitor.visitIntInsn(BIPUSH, 1);
 
             Label skipStringChecks = new Label();
             methodVisitor.visitJumpInsn(IFNE, skipStringChecks);
             methodVisitor.visitVarInsn(ALOAD, i);
             methodVisitor.visitTypeInsn(CHECKCAST, "java/lang/String");
             methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "length", "()I");
             methodVisitor.visitIntInsn(ISTORE, runtimeStringLen);
 
             methodVisitor.visitLabel(skipStringChecks);
           }
         }
       }
 
       emitChildren(node);
 
       if (functionDecl.patternMatching) {
         methodVisitor.visitLdcInsn("Non-exhaustive pattern rules in " + functionDecl.name());
         methodVisitor.visitInsn(DUP);   // This is necessary just to maintain stack height consistency. =/
         methodVisitor.visitMethodInsn(INVOKESTATIC, "loop/Loop", "error", "(Ljava/lang/String;)V");
       }
 
       methodVisitor.visitLabel(innerContext.endOfFunction);
       methodVisitor.visitInsn(ARETURN);
       methodVisitor.visitMaxs(0, 0);
       methodVisitor.visitEnd();
 
       methodStack.pop();
       functionStack.pop();
       scope.popScope();
     }
   };
 
   private void detectFreeVariables(Node top, ArgDeclList args, List<Variable> vars) {
     // Pre-order traversal.
     for (Node node : top.children()) {
       detectFreeVariables(node, args, vars);
     }
 
     if (top instanceof Variable) {
       Variable local = (Variable) top;
 
       boolean free = true;
       if (args != null)
         for (Node arg : args.children()) {
           ArgDeclList.Argument argument = (ArgDeclList.Argument) arg;
 
           if (argument.name().equals(local.name))
             free = false;
         }
 
       if (free)
         vars.add(local);
 
     } else if (top instanceof Call) {
       Call call = (Call) top;
       detectFreeVariables(call.args(), args, vars);
     }
 
   }
 
   private void scopeNestedFunction(FunctionDecl parent, Context context, FunctionDecl function) {
     String unscopedName = function.name();
 
     String newName;
     if (parent.name().startsWith(WHERE_SCOPE_FN_PREFIX))
       newName = parent.name() + '$' + unscopedName;
     else
       newName = WHERE_SCOPE_FN_PREFIX + parent.name() + '$' + unscopedName;
 
     // Apply the scoped name globally.
     function.scopedName(newName);
     context.newLocalFunction(unscopedName, function);
   }
 
   private final Emitter privateFieldEmitter = new Emitter() {
     @Override
     public void emitCode(Node node) {
       PrivateField privateField = (PrivateField) node;
       trackLineAndColumn(privateField);
       append(normalizeMethodName(privateField.name()));
     }
   };
 
   private final Emitter argDeclEmitter = new Emitter() {
     @Override
     public void emitCode(Node node) {
     }
   };
 
   private final Emitter inlineListEmitter = new Emitter() {
     @Override
     public void emitCode(Node node) {
       InlineListDef inlineListDef = (InlineListDef) node;
 
       MethodVisitor methodVisitor = methodStack.peek();
       Context context = functionStack.peek();
 
       String listType = inlineListDef.isSet ? "java/util/HashSet" : "java/util/ArrayList";
 
       int listVar = context.localVarIndex(context.newLocalVariable());
       methodVisitor.visitTypeInsn(NEW, listType);
       methodVisitor.visitInsn(DUP);
       methodVisitor.visitMethodInsn(INVOKESPECIAL, listType, "<init>", "()V");
       methodVisitor.visitVarInsn(ASTORE, listVar);
 
       for (Node child : inlineListDef.children()) {
         methodVisitor.visitVarInsn(ALOAD, listVar);
         emit(child);
 
         methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Collection", "add",
             "(Ljava/lang/Object;)Z");
         methodVisitor.visitInsn(POP); // discard result of add()
       }
 
       methodVisitor.visitVarInsn(ALOAD, listVar);
     }
   };
 
   private final Emitter inlineMapEmitter = new Emitter() {
     @Override
     public void emitCode(Node node) {
       InlineMapDef inlineMapDef = (InlineMapDef) node;
 
       MethodVisitor methodVisitor = methodStack.peek();
       Context context = functionStack.peek();
 
       String mapType = inlineMapDef.isTree ? "java/util/TreeMap" : "java/util/HashMap";
 
       int mapVar = context.localVarIndex(context.newLocalVariable());
       methodVisitor.visitTypeInsn(NEW, mapType);
       methodVisitor.visitInsn(DUP);
       methodVisitor.visitMethodInsn(INVOKESPECIAL, mapType, "<init>", "()V");
       methodVisitor.visitVarInsn(ASTORE, mapVar);
 
       for (Iterator<Node> iterator = inlineMapDef.children().iterator(); iterator.hasNext(); ) {
         Node key = iterator.next();
         methodVisitor.visitVarInsn(ALOAD, mapVar);
         emit(key);
         emit(iterator.next()); // value.
 
         methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put",
             "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
         methodVisitor.visitInsn(POP);   // disard put() result
       }
 
       methodVisitor.visitVarInsn(ALOAD, mapVar);
     }
   };
 
 
   private final Emitter callChainEmitter = new Emitter() {
     @Override
     public void emitCode(Node node) {
       CallChain callChain = (CallChain) node;
       List<Node> children = callChain.children();
 
       for (int i = 0, childrenSize = children.size(); i < childrenSize; i++) {
         Node child = children.get(i);
 
         if (i == 0
             && callChain.nullSafe
             && child instanceof Variable
             && childrenSize > 1)
           trackLineAndColumn(child);
         emit(child);
       }
     }
   };
 
   private final Emitter indexIntoListEmitter = new Emitter() {
     @Override
     public void emitCode(Node node) {
       IndexIntoList indexIntoList = (IndexIntoList) node;
       MethodVisitor methodVisitor = methodStack.peek();
 
       Node from = indexIntoList.from();
       if (from != null)
         emit(from);
 
       boolean hasTo = indexIntoList.to() != null;
       if (hasTo) {
         emit(indexIntoList.to());
 
         if (indexIntoList.isSlice() && from == null)
           methodVisitor.visitMethodInsn(INVOKESTATIC, "loop/runtime/Collections", "sliceTo",
               "(Ljava/lang/Object;Ljava/lang/Integer;)Ljava/lang/Object;");
         else
           methodVisitor.visitMethodInsn(INVOKESTATIC, "loop/runtime/Collections", "obtain",
               "(Ljava/lang/Object;Ljava/lang/Integer;Ljava/lang/Integer;)Ljava/lang/Object;");
       } else {
         if (indexIntoList.isSlice())
           methodVisitor.visitMethodInsn(INVOKESTATIC, "loop/runtime/Collections", "sliceFrom",
               "(Ljava/lang/Object;Ljava/lang/Integer;)Ljava/lang/Object;");
         else
           methodVisitor.visitMethodInsn(INVOKESTATIC, "loop/runtime/Collections", "obtain",
               "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
       }
     }
   };
 
   private final Emitter comprehensionEmitter = new Emitter() {
     @Override
     public void emitCode(Node node) {
       Comprehension comprehension = (Comprehension) node;
 
       Context context = functionStack.peek();
       MethodVisitor methodVisitor = methodStack.peek();
 
       // First create our output list.
       int outVarIndex = context.localVarIndex(context.newLocalVariable());
 
       methodVisitor.visitTypeInsn(NEW, "java/util/ArrayList");
       methodVisitor.visitInsn(DUP);
       methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V");
       methodVisitor.visitVarInsn(ASTORE, outVarIndex);
 
 
       // Now loop through the target variable.
       int iVarIndex = context.localVarIndex(context.newLocalVariable());
 
       // iterator = collection.iterator()
       emit(comprehension.inList());
       methodVisitor.visitTypeInsn(CHECKCAST, "java/util/Collection");
       methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Collection", "iterator",
           "()Ljava/util/Iterator;");
       methodVisitor.visitVarInsn(ASTORE, iVarIndex);
 
       Label start = new Label();
       Label end = new Label();
       // {
       // if !iterator.hasNext() jump to end
       methodVisitor.visitLabel(start);
       methodVisitor.visitVarInsn(ALOAD, iVarIndex);
       methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z");
       methodVisitor.visitJumpInsn(IFEQ, end);
 
       // var = iterator.next()
       methodVisitor.visitVarInsn(ALOAD, iVarIndex);
       methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next",
           "()Ljava/lang/Object;");
       int nextIndex = context.localVarIndex(context.newLocalVariable(comprehension.var()));
       methodVisitor.visitVarInsn(ASTORE, nextIndex);
 
       // if (filter_expression)
       if (comprehension.filter() != null) {
         emit(comprehension.filter());
         // Convert to primitive type.
         methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue",
             "()Z");
         methodVisitor.visitJumpInsn(IFEQ, start);
       }
 
       // Dump this value into the out list.
       methodVisitor.visitVarInsn(ALOAD, outVarIndex);
       methodVisitor.visitVarInsn(ALOAD, nextIndex);
 
       // Transform the value first using the projection expression.
       if (comprehension.projection() != null) {
         methodVisitor.visitInsn(POP);
         for (Node projection : comprehension.projection()) {
           emit(projection);
         }
       }
 
       methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add",
           "(Ljava/lang/Object;)Z");
 
       methodVisitor.visitInsn(POP);   // Discard result of add()
       methodVisitor.visitJumpInsn(GOTO, start);
       // }
       methodVisitor.visitLabel(end);
 
       // Finally.
       methodVisitor.visitVarInsn(ALOAD, outVarIndex);
     }
   };
 
   private void replaceVarInTree(Node top, Variable var, String with) {
     // Pre-order traversal.
     for (Node node : top.children()) {
       replaceVarInTree(node, var, with);
     }
 
     if (top instanceof Variable) {
       Variable local = (Variable) top;
       if (var.name.equals(local.name)) {
         local.name = with;
       }
     } else if (top instanceof Call) {
       Call call = (Call) top;
       replaceVarInTree(call.args(), var, with);
     }
   }
 
   private final Emitter patternRuleEmitter = new Emitter() {
     @Override
     public void emitCode(Node node) {
       PatternRule rule = (PatternRule) node;
       Context context = functionStack.peek();
       MethodVisitor methodVisitor = methodStack.peek();
 
       if (context.arguments.isEmpty())
         throw new RuntimeException("Incorrect number of arguments for pattern matching");
 
       if (context.arguments.size() != rule.patterns.size())
         throw new RuntimeException("Incorrect number of pattern rules. Expected pattern rules for "
             + context.arguments + " but found " + rule.patterns.size() + " rule(s): "
             + Parser.stringify(rule.patterns));
 
       Label matchedClause = new Label();
       Label endOfClause = new Label();
 
       for (int i = 0, argumentsSize = context.arguments.size(); i < argumentsSize; i++) {
 
         Node pattern = rule.patterns.get(i);
         if (pattern instanceof ListDestructuringPattern) {
           emitListDestructuringPatternRule(rule, methodVisitor, context, matchedClause, endOfClause,
               i);
         } else if (pattern instanceof ListStructurePattern) {
           emitListStructurePatternRule(rule, methodVisitor, context, matchedClause, endOfClause, i);
         } else if (pattern instanceof StringLiteral
             || pattern instanceof IntLiteral
             || pattern instanceof BooleanLiteral) {
 
           methodVisitor.visitVarInsn(ALOAD, i);
           emit(pattern);
 
           if (!(pattern instanceof BooleanLiteral))
             methodVisitor.visitMethodInsn(INVOKESTATIC, "loop/runtime/Operations", "equal",
                 "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Boolean;");
           methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
 
           methodVisitor.visitJumpInsn(IFEQ, endOfClause);
         } else if (pattern instanceof TypeLiteral) {
           emitTypePatternRule(methodVisitor, matchedClause, endOfClause, i, (TypeLiteral) pattern);
         } else if (pattern instanceof RegexLiteral) {
           methodVisitor.visitVarInsn(ALOAD, i);
           methodVisitor.visitTypeInsn(CHECKCAST, "java/lang/String");
           methodVisitor.visitLdcInsn(((RegexLiteral) pattern).value);
           methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "matches",
               "(Ljava/lang/String;)Z");
           methodVisitor.visitJumpInsn(IFEQ, endOfClause);
 
         } else if (pattern instanceof StringPattern) {
           emitStringPatternRule(rule, context, matchedClause, endOfClause, i);
         } else if (pattern instanceof MapPattern) {
           emitMapPatternRule(rule, context, matchedClause, endOfClause, i);
         } else if (pattern instanceof WildcardPattern) {
           // Always matches.
          methodVisitor.visitJumpInsn(GOTO, matchedClause);
         }
       }
 
       methodVisitor.visitLabel(matchedClause);
       emitPatternClauses(rule);
       methodVisitor.visitJumpInsn(GOTO, context.endOfFunction);
       methodVisitor.visitLabel(endOfClause);
     }
   };
 
   private void emitTypePatternRule(MethodVisitor methodVisitor,
                                    Label matchedClause,
                                    Label endOfClause,
                                    int argIndex,
                                    TypeLiteral pattern) {
     String typeName;
     ClassDecl resolved = scope.resolve(pattern.name);
     if (resolved != null) {
       typeName = resolved.name;
     } else {
       typeName = scope.resolveJavaType(pattern.name);
     }
 
     methodVisitor.visitVarInsn(ALOAD, argIndex);
     methodVisitor.visitTypeInsn(INSTANCEOF, typeName.replace('.', '/'));
     methodVisitor.visitJumpInsn(IFEQ, endOfClause);
     methodVisitor.visitJumpInsn(GOTO, matchedClause);
   }
 
   private void emitPatternClauses(PatternRule rule) {
     if (rule.rhs != null) {
       emit(rule.rhs);
     } else
       emitGuards(rule);
   }
 
   private void emitGuards(PatternRule rule) {
     MethodVisitor methodVisitor = methodStack.peek();
 
     List<Node> children = rule.children();
     for (int i = 0, childrenSize = children.size(); i < childrenSize; i++) {
       Node node = children.get(i);
       if (!(node instanceof Guard))
         throw new RuntimeException("Apparent pattern rule missing guards: "
             + Parser.stringify(rule));
       Guard guard = (Guard) node;
       Label endOfClause = new Label();
       Label matchedClause = new Label();
 
       // The "Otherwise" expression is a plain else.
       boolean notElse = !(guard.expression instanceof OtherwiseGuard);
       if (notElse) {
         emit(guard.expression);
 
         methodVisitor.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
         methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
         methodVisitor.visitJumpInsn(IFEQ, endOfClause);
       }
 
       methodVisitor.visitLabel(matchedClause);
       emit(guard.line);
       methodVisitor.visitJumpInsn(GOTO, functionStack.peek().endOfFunction);
       methodVisitor.visitLabel(endOfClause);
 
       if (i == childrenSize - 1 && notElse)
         methodVisitor.visitInsn(ACONST_NULL);
     }
   }
 
   private void emitMapPatternRule(PatternRule rule,
                                   Context context,
                                   Label matchedClause,
                                   Label endOfClause,
                                   int argIndex) {
     MapPattern pattern = (MapPattern) rule.patterns.get(argIndex);
     MethodVisitor methodVisitor = methodStack.peek();
 
     boolean hasType = false;
     List<Node> children = pattern.children();
     for (int i = 0, childrenSize = children.size(); i < childrenSize; i++) {
       Node child = children.get(i);
       if (child instanceof TypeLiteral) {
         hasType = true;
 
         TypeLiteral typeLiteral = (TypeLiteral) child;
         String typeName;
         ClassDecl resolved = scope.resolve(typeLiteral.name);
         if (resolved != null) {
           typeName = resolved.name;
         } else {
           typeName = scope.resolveJavaType(typeLiteral.name);
         }
 
         methodVisitor.visitVarInsn(ALOAD, argIndex);
         methodVisitor.visitTypeInsn(INSTANCEOF, typeName.replace('.', '/'));
         continue;
       }
 
       // Guard the first destructuring pair if there is a type pattern.
       if (hasType && i == 1) {
         methodVisitor.visitJumpInsn(IFEQ, endOfClause);
       }
 
       DestructuringPair pair = (DestructuringPair) child;
 
       int destructuredVar = context.localVarIndex(context.newLocalVariable((Variable) pair.lhs));
       emit(pair.rhs);
 
       methodVisitor.visitVarInsn(ASTORE, destructuredVar);
       methodVisitor.visitVarInsn(ALOAD, destructuredVar);
       methodVisitor.visitJumpInsn(IFNULL, endOfClause);
       methodVisitor.visitJumpInsn(GOTO, matchedClause);
     }
   }
 
   private void emitStringPatternRule(PatternRule rule,
                                                 Context context,
                                                 Label matchedClause,
                                                 Label endOfClause,
                                                 int argIndex) {
     MethodVisitor methodVisitor = methodStack.peek();
 
     methodVisitor.visitVarInsn(ILOAD, context.localVarIndex(IS_STRING_PREFIX + argIndex));
     methodVisitor.visitJumpInsn(IFEQ, endOfClause);   // Not a string, so skip
 
     List<Node> children = rule.patterns.get(argIndex).children();
     int i = 0, childrenSize = children.size();
 
     boolean splittable = false;
     int lastIndex = context.localVarIndex(context.newLocalVariable());      // The last index of split (i.e. pattern delimiter).
     methodVisitor.visitIntInsn(BIPUSH, -1);
     methodVisitor.visitIntInsn(ISTORE, lastIndex);
 
     for (int j = 0; j < childrenSize; j++) {
       Node child = children.get(j);
 
       if (child instanceof Variable) {
         if (j < childrenSize - 1) {
           context.localVarIndex(context.newLocalVariable((Variable) child));
 
           Node next = children.get(j + 1);
           if (next instanceof StringLiteral) {
             // If the next node is a string literal, then we must split this
             // string across occurrences of the given literal.
             int thisIndex = context.localVarIndex(context.newLocalVariable());
 
             methodVisitor.visitVarInsn(ALOAD, argIndex);
             methodVisitor.visitTypeInsn(CHECKCAST, "java/lang/String");
             emit(next);
 
             // If this is the second or greater pattern matcher, seek from the last location.
             if (splittable) {
               methodVisitor.visitIntInsn(ILOAD, lastIndex);
               methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "indexOf",
                   "(Ljava/lang/String;I)I");
             } else {
               methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "indexOf",
                   "(Ljava/lang/String;)I");
             }
             methodVisitor.visitIntInsn(ISTORE, thisIndex);
 
             methodVisitor.visitIntInsn(ILOAD, thisIndex);
             methodVisitor.visitIntInsn(BIPUSH, -1);
             methodVisitor.visitJumpInsn(IF_ICMPLE, endOfClause); // Jump out of this clause
 
             int matchedPieceVar = context.localVarIndex(context.newLocalVariable((Variable) child));
 
             methodVisitor.visitVarInsn(ALOAD, argIndex);
             methodVisitor.visitTypeInsn(CHECKCAST, "java/lang/String");
 
             methodVisitor.visitIntInsn(ILOAD, lastIndex);
 
             Label startFromLastIndex = new Label();
             Label startFromZeroIndex = new Label();
             methodVisitor.visitIntInsn(BIPUSH, -1);
             methodVisitor.visitJumpInsn(IF_ICMPNE, startFromLastIndex);
 
             // Either start from 0 or lastindex of split.
             methodVisitor.visitIntInsn(BIPUSH, 0);
             methodVisitor.visitJumpInsn(GOTO, startFromZeroIndex);
             methodVisitor.visitLabel(startFromLastIndex);
             methodVisitor.visitIntInsn(ILOAD, lastIndex);
             methodVisitor.visitLabel(startFromZeroIndex);
 
             methodVisitor.visitIntInsn(ILOAD, thisIndex);
             methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "substring",
                 "(II)Ljava/lang/String;");
             methodVisitor.visitInsn(DUP);
 
             // Save this piece into our variable.
             methodVisitor.visitVarInsn(ASTORE, matchedPieceVar);
 
             // Advance the index by the length of this match.
             methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "length", "()I");
             methodVisitor.visitIntInsn(ILOAD, thisIndex);
             methodVisitor.visitInsn(IADD);
             methodVisitor.visitIntInsn(ISTORE, lastIndex);
 
             splittable = true;
           } else {
             methodVisitor.visitVarInsn(ALOAD, argIndex);
             int matchedPieceVar = context.localVarIndex(context.newLocalVariable((Variable) child));
             methodVisitor.visitTypeInsn(CHECKCAST, "java/lang/String");
             methodVisitor.visitLdcInsn(i);
             methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C");
             methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf",
                 "(C)Ljava/lang/Character;");
             methodVisitor.visitVarInsn(ASTORE, matchedPieceVar);
           }
         } else {
           int matchedPieceVar = context.localVarIndex(context.newLocalVariable((Variable) child));
           int strLen = context.localVarIndex(RUNTIME_STR_LEN_PREFIX + argIndex);
 
 //          methodVisitor.visitVarInsn(ALOAD, argIndex);
           methodVisitor.visitIntInsn(ILOAD, strLen);
           methodVisitor.visitIntInsn(BIPUSH, 1);
 
           Label restOfString = new Label();
           Label assignToPiece = new Label();
 
           methodVisitor.visitJumpInsn(IF_ICMPNE, restOfString);
           methodVisitor.visitLdcInsn("");
           methodVisitor.visitJumpInsn(GOTO, assignToPiece);
           methodVisitor.visitLabel(restOfString);
 
           methodVisitor.visitVarInsn(ALOAD, argIndex);
           methodVisitor.visitTypeInsn(CHECKCAST, "java/lang/String");
 
           methodVisitor.visitIntInsn(ILOAD, lastIndex);
           methodVisitor.visitIntInsn(BIPUSH, -1);
 
           Label restOfStringFromI = new Label();
           Label reduceString = new Label();
           methodVisitor.visitJumpInsn(IF_ICMPLE, restOfStringFromI);
           methodVisitor.visitIntInsn(ILOAD, lastIndex);
           methodVisitor.visitJumpInsn(GOTO, reduceString);
           methodVisitor.visitLabel(restOfStringFromI);
           methodVisitor.visitLdcInsn(i);
           methodVisitor.visitLabel(reduceString);
           methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "substring",
               "(I)Ljava/lang/String;");
 
           methodVisitor.visitLabel(assignToPiece);
           methodVisitor.visitVarInsn(ASTORE, matchedPieceVar);
         }
         i++;
       }
     }
   }
 
   private void emitListStructurePatternRule(PatternRule rule,
                                             MethodVisitor methodVisitor,
                                             Context context,
                                             Label matchedClause,
                                             Label endOfClause,
                                             int argIndex) {
     ListStructurePattern listPattern = (ListStructurePattern) rule.patterns.get(argIndex);
     List<Node> children = listPattern.children();
 
//    methodVisitor.visitInsn(POP); // get rid of arg, we dont need it (yet)
     int runtimeListSizeVar = context.localVarIndex(RUNTIME_LIST_SIZE_VAR_PREFIX + argIndex);
     methodVisitor.visitIntInsn(ILOAD, runtimeListSizeVar);
     methodVisitor.visitIntInsn(BIPUSH, children.size());
     methodVisitor.visitJumpInsn(IF_ICMPNE, endOfClause);
 
     // Slice the list by terminals in the pattern list.
     for (int j = 0, childrenSize = children.size(); j < childrenSize; j++) {
       Node child = children.get(j);
       if (child instanceof Variable) {
         trackLineAndColumn(child);
 
         // Store into structure vars.
         int localVar = context.localVarIndex(context.newLocalVariable(((Variable) child)));
 
         methodVisitor.visitVarInsn(ALOAD, argIndex);
         methodVisitor.visitIntInsn(BIPUSH, j);      // We dont support matching >128 args ;)
         methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "get",
             "(I)Ljava/lang/Object;");
         methodVisitor.visitVarInsn(ASTORE, localVar);
       }
     }
 
     methodVisitor.visitJumpInsn(GOTO, matchedClause);
   }
 
   private void emitListDestructuringPatternRule(PatternRule rule,
                                                 MethodVisitor methodVisitor,
                                                 Context context,
                                                 Label matchedClause,
                                                 Label endOfClause,
                                                 int argIndex) {
     ListDestructuringPattern listPattern = (ListDestructuringPattern) rule.patterns.get(argIndex);
     Label noMatch = new Label();
 
     int runtimeListSizeVar = context.localVarIndex(RUNTIME_LIST_SIZE_VAR_PREFIX + argIndex);
 
     int size = listPattern.children().size();
     if (size == 0) {
 //      methodVisitor.visitInsn(POP);  // Dont need the list really.
       methodVisitor.visitIntInsn(ILOAD, runtimeListSizeVar);
       methodVisitor.visitJumpInsn(IFEQ, matchedClause);
       methodVisitor.visitJumpInsn(GOTO, endOfClause);
     } else if (size == 1) {
 //      methodVisitor.visitInsn(POP);  // Dont need the list really.
       methodVisitor.visitIntInsn(ILOAD, runtimeListSizeVar);
       methodVisitor.visitIntInsn(BIPUSH, 1);
       methodVisitor.visitJumpInsn(IFNE, matchedClause);
       methodVisitor.visitJumpInsn(GOTO, endOfClause);
     } else {
       // Slice the list by terminals in the pattern list.
       int i = 0;
       List<Node> children = listPattern.children();
       for (int j = 0, childrenSize = children.size(); j < childrenSize; j++) {
         Node child = children.get(j);
         if (child instanceof Variable) {
           trackLineAndColumn(child);
           int localVar = context.localVarIndex(context.newLocalVariable(((Variable) child)));
 
           if (j < childrenSize - 1) {
             methodVisitor.visitVarInsn(ALOAD, argIndex);
             methodVisitor.visitLdcInsn(i);
             methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "get",
                 "(I)Ljava/lang/Object;");
             methodVisitor.visitVarInsn(ASTORE, localVar);
           } else {
             methodVisitor.visitIntInsn(ILOAD, runtimeListSizeVar);
             methodVisitor.visitIntInsn(BIPUSH, 1);
             Label storeEmptyList = new Label();
             methodVisitor.visitJumpInsn(IF_ICMPEQ, storeEmptyList);
 
             // Otherwise store a slice of the list.
             methodVisitor.visitVarInsn(ALOAD, argIndex);
             methodVisitor.visitLdcInsn(i);
             methodVisitor.visitIntInsn(ILOAD, runtimeListSizeVar);
             methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "subList",
                 "(II)Ljava/util/List;");
             methodVisitor.visitVarInsn(ASTORE, localVar);
             methodVisitor.visitJumpInsn(GOTO, matchedClause);
 
             methodVisitor.visitLabel(storeEmptyList);
             methodVisitor.visitFieldInsn(GETSTATIC, "java/util/Collections", "EMPTY_LIST",
                 "Ljava/util/List;");
             methodVisitor.visitVarInsn(ASTORE, localVar);
             methodVisitor.visitJumpInsn(GOTO, matchedClause);
           }
 
           i++;
         }
       }
 
 //      methodVisitor.visitInsn(POP); // discard list as we're done with it.
     }
 
     methodVisitor.visitLabel(noMatch);
   }
 }
