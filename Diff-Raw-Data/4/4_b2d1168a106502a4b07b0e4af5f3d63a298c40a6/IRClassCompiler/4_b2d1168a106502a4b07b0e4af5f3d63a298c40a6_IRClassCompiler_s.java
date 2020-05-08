 package gwtsu;
 
 import com.google.gwt.thirdparty.guava.common.collect.Lists;
 import com.google.gwt.thirdparty.guava.common.collect.Maps;
 import com.google.gwt.thirdparty.guava.common.collect.Sets;
 import gw.lang.ir.IRAnnotation;
 import gw.lang.ir.IRClass;
 import gw.lang.ir.IRElement;
 import gw.lang.ir.IRExpression;
 import gw.lang.ir.IRStatement;
 import gw.lang.ir.IRSymbol;
 import gw.lang.ir.IRType;
 import gw.lang.ir.expression.IRArithmeticExpression;
 import gw.lang.ir.expression.IRArrayLengthExpression;
 import gw.lang.ir.expression.IRArrayLoadExpression;
 import gw.lang.ir.expression.IRBooleanLiteral;
 import gw.lang.ir.expression.IRCastExpression;
 import gw.lang.ir.expression.IRCharacterLiteral;
 import gw.lang.ir.expression.IRClassLiteral;
 import gw.lang.ir.expression.IRCompositeExpression;
 import gw.lang.ir.expression.IRConditionalAndExpression;
 import gw.lang.ir.expression.IRConditionalOrExpression;
 import gw.lang.ir.expression.IREqualityExpression;
 import gw.lang.ir.expression.IRFieldGetExpression;
 import gw.lang.ir.expression.IRIdentifier;
 import gw.lang.ir.expression.IRInstanceOfExpression;
 import gw.lang.ir.expression.IRMethodCallExpression;
 import gw.lang.ir.expression.IRNegationExpression;
 import gw.lang.ir.expression.IRNewArrayExpression;
 import gw.lang.ir.expression.IRNewExpression;
 import gw.lang.ir.expression.IRNewMultiDimensionalArrayExpression;
 import gw.lang.ir.expression.IRNoOpExpression;
 import gw.lang.ir.expression.IRNotExpression;
 import gw.lang.ir.expression.IRNullLiteral;
 import gw.lang.ir.expression.IRNumericLiteral;
 import gw.lang.ir.expression.IRPrimitiveTypeConversion;
 import gw.lang.ir.expression.IRRelationalExpression;
 import gw.lang.ir.expression.IRStringLiteralExpression;
 import gw.lang.ir.expression.IRTernaryExpression;
 import gw.lang.ir.statement.IRArrayStoreStatement;
 import gw.lang.ir.statement.IRAssignmentStatement;
 import gw.lang.ir.statement.IRBreakStatement;
 import gw.lang.ir.statement.IRCatchClause;
 import gw.lang.ir.statement.IRContinueStatement;
 import gw.lang.ir.statement.IRDoWhileStatement;
 import gw.lang.ir.statement.IREvalStatement;
 import gw.lang.ir.statement.IRFieldDecl;
 import gw.lang.ir.statement.IRFieldSetStatement;
 import gw.lang.ir.statement.IRForEachStatement;
 import gw.lang.ir.statement.IRIfStatement;
 import gw.lang.ir.statement.IRMethodCallStatement;
 import gw.lang.ir.statement.IRMethodStatement;
 import gw.lang.ir.statement.IRMonitorLockAcquireStatement;
 import gw.lang.ir.statement.IRMonitorLockReleaseStatement;
 import gw.lang.ir.statement.IRNoOpStatement;
 import gw.lang.ir.statement.IRReturnStatement;
 import gw.lang.ir.statement.IRStatementList;
 import gw.lang.ir.statement.IRSwitchStatement;
 import gw.lang.ir.statement.IRSyntheticStatement;
 import gw.lang.ir.statement.IRThrowStatement;
 import gw.lang.ir.statement.IRTryCatchFinallyStatement;
 import gw.lang.ir.statement.IRWhileStatement;
 import gw.lang.reflect.IType;
 import gw.lang.reflect.Modifier;
 import gw.lang.reflect.TypeSystem;
 import gw.lang.reflect.gs.IGosuClass;
 import gw.lang.reflect.java.IJavaType;
 import gw.util.GosuClassUtil;
 
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 
 import static gwtsu.CheckedExceptionAnalyzer.*;
 
 /**
  * @author kprevas
  */
 public class IRClassCompiler {
   
   private final static Map<String, String> replacementTypes = Maps.newHashMap();
   private final static Map<String, String> replacementMethods = Maps.newHashMap();
 
   static {
     replacementTypes.put("gw.lang.reflect.IType", "Class");
     replacementTypes.put("gw.lang.parser.EvaluationException", "RuntimeException");
     replacementMethods.put("gw.internal.gosu.ir.transform.statement.ForEachStatementTransformer.makeIterator",
             "gwtsu.Util.makeIterator");
     replacementMethods.put("gw.lang.reflect.TypeSystem.getByFullName",
             "gwtsu.Util.getByFullName");
     replacementMethods.put("gw.internal.gosu.runtime.GosuRuntimeMethods.typeof",
             "gwtsu.Util.typeof");
   }
 
   private IRClass irClass;
   private ExceptionMap exceptionMap;
   private Stack<StringBuilder> auxMethodsBuilders = new Stack<StringBuilder>();
   private Set<IRMethodStatement> ctors = Sets.newHashSet();
   private int uid = 0;
   private boolean isOverlay;
   private boolean isStatic;
 
   public IRClassCompiler(IRClass irClass, ExceptionMap exceptionMap) {
     this.irClass = irClass;
     this.exceptionMap = exceptionMap;
   }
 
   public String compileToJava() {
     StringBuilder builder = new StringBuilder();
     builder.append("package ")
             .append(GosuClassUtil.getPackage(getTypeName(irClass.getThisType())))
             .append(";\n");
     appendClass(builder);
     return builder.toString();
   }
 
   private void appendClass(StringBuilder builder) {
     for (IRAnnotation annotation : irClass.getAnnotations()) {
       appendAnnotation(builder, annotation);
     }
     builder.append(Modifier.toModifierString(
             irClass.getModifiers() & ~Modifier.SYNCHRONIZED));
     if (irClass.getThisType().isInterface()) {
       builder.append(" interface ");
     } else {
       builder.append(" class ");
     }
     String relativeName = getRelativeClassName();
     builder.append(relativeName);
     IRType superType = irClass.getSuperType();
     if (superType != null) {
       if (getTypeName(superType).equals("gwtsu.JSONOverlay")) {
         isOverlay = true;
         builder.append(" extends com.google.gwt.core.client.JavaScriptObject");
       } else {
         builder.append(" extends ")
                 .append(getTypeName(superType));
       }
     }
     List<IRType> interfaces = Lists.newArrayList(irClass.getInterfaces());
     Iterator<IRType> iterator = interfaces.iterator();
     while (iterator.hasNext()) {
       IRType type = iterator.next();
       if (getTypeName(type).equals("gw.lang.reflect.gs.IGosuClassObject")) {
         iterator.remove();
       }
     }
     if (!interfaces.isEmpty()) {
       builder.append(" implements ");
       for (int i = 0, interfacesSize = interfaces.size(); i < interfacesSize; i++) {
         if (i > 0) {
           builder.append(", ");
         }
         builder.append(getTypeName(interfaces.get(i)));
       }
     }
     builder.append(" {\n");
     if (isOverlay) {
       builder.append("protected ")
               .append(getRelativeClassName())
               .append("() {}\n");
     } else {
       for (IRFieldDecl field : irClass.getFields()) {
         appendField(builder, field);
       }
     }
     for (IRMethodStatement method : irClass.getMethods()) {
       if (method.getName().equals("<init>")) {
         ctors.add(method);
       }
     }
     for (IRMethodStatement method : irClass.getMethods()) {
       appendMethod(builder, method);
     }
     builder.append("}");
   }
 
   private void appendField(StringBuilder builder, IRFieldDecl field) {
     for (IRAnnotation annotation : field.getAnnotations()) {
       appendAnnotation(builder, annotation);
     }
     int modifiers = field.getModifiers();
     if (field.getName().startsWith("typeparam$")) {
       modifiers &= ~Modifier.FINAL;
     }
     builder.append(Modifier.toModifierString(modifiers))
             .append(" ")
             .append(getTypeName(field.getType()))
             .append(" ")
             .append(field.getName());
     // TODO kcp - initializer?
     builder.append(";\n");
   }
 
   private void appendMethod(StringBuilder builder, IRMethodStatement method) {
     if (method.getName().equals("getIntrinsicType") || method.getName().startsWith("$")) {
       return;
     }
     isStatic = Modifier.isStatic(method.getModifiers());
     IRStatement methodBody = method.getMethodBody();
     if (isOverlay) {
       if (method.getName().startsWith("get") && method.getParameters().isEmpty()) {
         if (methodBody instanceof IRStatementList) {
           List<IRStatement> statements = ((IRStatementList) methodBody).getStatements();
           if (statements.size() == 1 && statements.get(0) instanceof IRReturnStatement) {
             IRExpression returnValue = ((IRReturnStatement) statements.get(0)).getReturnValue();
             if (returnValue instanceof IRFieldGetExpression) {
               builder.append("public final native ")
                       .append(getTypeName(((IRFieldGetExpression) returnValue).getFieldType()))
                       .append(" ")
                       .append(method.getName())
                       .append("() /*-{ return this.")
                       .append(method.getName().substring("get".length()))
                       .append("; }-*/;\n");
               return;
             }
           }
         }
       }
       if (method.getName().startsWith("set")
               && method.getReturnType().isVoid()
               && method.getParameters().size() == 1) {
         if (methodBody instanceof IRStatementList) {
           List<IRStatement> statements = ((IRStatementList) methodBody).getStatements();
           if (statements.size() == 2
                   && statements.get(0) instanceof IRFieldSetStatement
                   && statements.get(1) instanceof IRReturnStatement) {
             builder.append("public final native void ")
                     .append(method.getName())
                     .append("(")
                     .append(getTypeName(method.getParameters().get(0).getType()))
                     .append(" ")
                     .append(method.getParameters().get(0).getName())
                     .append(") /*-{ this.")
                     .append(method.getName().substring("set".length()))
                     .append(" = ")
                     .append(method.getParameters().get(0).getName())
                     .append("; }-*/;\n");
             return;
           }
         }
       }
       if (method.getName().equals("<init>")) {
         if (method.getParameters().size() == 1
                 && method.getParameters().get(0).getType().getName().equals("java.lang.String")) {
           builder.append("public static native ")
                   .append(getRelativeClassName())
                   .append(" $gwtsu$parse(String json) /*-{ return JSON.parse(json); }-*/;\n");
         }
         return;
       }
     }
     Map<String, IRSymbol> symbols = Maps.newLinkedHashMap();
     for (IRAnnotation annotation : method.getAnnotations()) {
       appendAnnotation(builder, annotation);
     }
     List<String> exceptionsThrown = Lists.newArrayList();
     List<String> exceptionsCaught = Lists.newArrayList();
     if (!method.getName().equals("<clinit>")) {
       builder.append(Modifier.toModifierString(method.getModifiers()))
               .append(" ");
       if (method.getName().equals("<init>")) {
         builder.append(getRelativeClassName());
       } else {
         builder.append(getTypeName(method.getReturnType()))
                 .append(" ")
                 .append(method.getName());
       }
       builder.append("(");
       List<IRSymbol> parameters = method.getParameters();
       for (int i = 0, parametersSize = parameters.size(); i < parametersSize; i++) {
         if (i > 0) {
           builder.append(", ");
         }
         IRSymbol parameter = parameters.get(i);
         String paramName = getSymbolName(parameter);
         builder.append(getTypeName(parameter.getType()))
                 .append(" ")
                 .append(paramName);
         symbols.put(paramName, parameter);
       }
       builder.append(")");
       List<IType> exceptionsFromSuper = getExceptionsFromSuper(method);
       List<IType> exceptionsFromBody;
       if (method.getName().equals("<init>")) {
         exceptionsFromBody = exceptionMap.getExceptions(getCtorInfoFromSymbols(
                 (IGosuClass) irClass.getThisType().getType(), method.getParameters()));
       } else {
         exceptionsFromBody = exceptionMap.getExceptions(getMethodInfoFromSymbols(
                 (IGosuClass) irClass.getThisType().getType(), method.getName(), method.getParameters()));
       }
       if (exceptionsFromSuper == null) {
         for (IType exception : exceptionsFromBody) {
           exceptionsThrown.add(exception.getName());
         }
       } else {
         for (IType exception : exceptionsFromSuper) {
           exceptionsThrown.add(exception.getName());
         }
         for (IType exception : exceptionsFromBody) {
           boolean thrown = false;
           for (IType superException : exceptionsFromSuper) {
             if (superException.isAssignableFrom(exception)) {
               thrown = true;
               break;
             }
           }
           if (!thrown) {
             exceptionsCaught.add(exception.getName());
           }
         }
       }
       for (int i = 0, exceptionsSize = exceptionsThrown.size(); i < exceptionsSize; i++) {
         String exception = exceptionsThrown.get(i);
         if (i > 0) {
           builder.append(", ");
         } else {
           builder.append(" throws ");
         }
         if (replacementTypes.containsKey(exception)) {
           builder.append(replacementTypes.get(exception));
         } else {
           builder.append(exception);
         }
       }
       builder.append(" ");
     }
     if (methodBody != null) {
       if (!exceptionsCaught.isEmpty()) {
         builder.append("{\ntry ");
       }
       if (!(methodBody instanceof IRStatementList)) {
         builder.append("{\n");
       }
       appendStatement(builder, methodBody, symbols);
       if (!(methodBody instanceof IRStatementList)) {
         builder.append("}\n");
       }
       if (!exceptionsCaught.isEmpty()) {
         for (String exception : exceptionsCaught) {
           builder.append("catch (")
                   .append(exception)
                   .append(" e")
                   .append(uid)
                   .append(") {\n")
                   .append("throw new java.lang.RuntimeException(e")
                   .append(uid)
                   .append(");\n")
                   .append("}\n");
           uid++;
         }
         builder.append("}\n");
       }
     } else {
       builder.append(";\n");
     }
     while (!auxMethodsBuilders.empty()) {
       builder.append(auxMethodsBuilders.pop());
     }
   }
 
   private void appendStatement(StringBuilder builder, IRStatement statement, Map<String, IRSymbol> symbols) {
     if (statement instanceof IRFieldDecl) {
       // TODO kcp
       builder.append("/* IRFieldDecl */");
     } else if (statement instanceof IRMethodStatement) {
       // TODO kcp
       builder.append("/* IRMethodStatement */");
     } else if (statement instanceof IRArrayStoreStatement) {
       IRArrayStoreStatement arrayStoreStatement = (IRArrayStoreStatement) statement;
       appendExpression(builder, arrayStoreStatement.getTarget(), symbols);
       builder.append("[");
       appendExpression(builder, arrayStoreStatement.getIndex(), symbols);
       builder.append("] = ");
       appendExpression(builder, arrayStoreStatement.getValue(), symbols);
       builder.append(";\n");
     } else if (statement instanceof IRAssignmentStatement) {
       IRAssignmentStatement assignmentStatement = (IRAssignmentStatement) statement;
       String symbolName = getSymbolName(assignmentStatement.getSymbol());
       if (!symbols.containsKey(symbolName)) {
         builder.append(getTypeName(assignmentStatement.getSymbol().getType()))
                 .append(" ");
       }
       builder.append(symbolName)
               .append(" = ");
       appendExpression(builder, assignmentStatement.getValue(), symbols);
       builder.append(";\n");
       symbols.put(symbolName, assignmentStatement.getSymbol());
     } else if (statement instanceof IRBreakStatement) {
       builder.append("break;\n");
     } else if (statement instanceof IRContinueStatement) {
       builder.append("continue;\n");
     } else if (statement instanceof IRDoWhileStatement) {
       IRDoWhileStatement doWhileStatement = (IRDoWhileStatement) statement;
       builder.append("do ");
       appendStatement(builder, doWhileStatement.getBody(), Maps.newHashMap(symbols));
       builder.append("while(");
       appendExpression(builder, doWhileStatement.getLoopTest(), symbols);
       builder.append(");\n");
     } else if (statement instanceof IREvalStatement) {
       // TODO kcp
       builder.append("/* IREvalStatement */");
     } else if (statement instanceof IRFieldSetStatement) {
       IRFieldSetStatement fieldSetStatement = (IRFieldSetStatement) statement;
       if (fieldSetStatement.getLhs() != null) {
         appendExpression(builder, fieldSetStatement.getLhs(), symbols);
         builder.append(".");
       }
       builder.append(fieldSetStatement.getName())
               .append(" = ");
       appendExpression(builder, fieldSetStatement.getRhs(), symbols);
       builder.append(";\n");
     } else if (statement instanceof IRForEachStatement) {
       IRForEachStatement forEachStatement = (IRForEachStatement) statement;
       Map<String, IRSymbol> innerSymbols = Maps.newHashMap(symbols);
       List<IRStatement> initializers = forEachStatement.getInitializers();
       if (initializers.size() > 1) {
         builder.append("{\n");
         for (IRStatement initializer : initializers) {
           appendStatement(builder, initializer, innerSymbols);
         }
         builder.append("for (");
       } else {
         builder.append("for (");
         for (int i = 0, initializersSize = initializers.size(); i < initializersSize; i++) {
           if (i > 0) {
             builder.append(", ");
           }
           appendStatement(builder, initializers.get(i), innerSymbols);
           if (builder.substring(builder.length() - 2, builder.length()).equals(";\n")) {
             builder.setLength(builder.length() - 2);
           }
         }
       }
       builder.append("; ");
       appendExpression(builder, forEachStatement.getLoopTest(), symbols);
       builder.append("; ");
       List<IRStatement> incrementors = forEachStatement.getIncrementors();
       for (int i = 0, incrementorsSize = incrementors.size(); i < incrementorsSize; i++) {
         if (i > 0) {
           builder.append(", ");
         }
         appendStatement(builder, incrementors.get(i), innerSymbols);
         if (builder.substring(builder.length() - 2, builder.length()).equals(";\n")) {
           builder.setLength(builder.length() - 2);
         }
       }
       builder.append(") ");
       appendStatement(builder, forEachStatement.getBody(), innerSymbols);
       if (initializers.size() > 1) {
         builder.append("}\n");
       }
     } else if (statement instanceof IRIfStatement) {
       IRIfStatement ifStatement = (IRIfStatement) statement;
       if (ifStatement.getExpression() instanceof IRInstanceOfExpression) {
         IRInstanceOfExpression instanceOfExpression = (IRInstanceOfExpression) ifStatement.getExpression();
         IType testType = instanceOfExpression.getTestType().getType();
         IType rootType = instanceOfExpression.getRoot().getType().getType();
         if (!testType.isInterface() && !rootType.isAssignableFrom(testType)
                 && !testType.isAssignableFrom(rootType)) {
           appendStatement(builder, ifStatement.getElseStatement(), symbols);
           return;
         }
       }
       builder.append("if (");
       appendExpression(builder, ifStatement.getExpression(), symbols);
       builder.append(") ");
       appendStatement(builder, ifStatement.getIfStatement(), Maps.newHashMap(symbols));
       if (ifStatement.getElseStatement() != null) {
         builder.append(" else ");
         appendStatement(builder, ifStatement.getElseStatement(), Maps.newHashMap(symbols));
       }
     } else if (statement instanceof IRMethodCallStatement) {
       appendExpression(builder, ((IRMethodCallStatement) statement).getExpression(), symbols);
       builder.append(";\n");
     } else if (statement instanceof IRMonitorLockAcquireStatement) {
       // TODO kcp
       builder.append("/* IRMonitorLockAcquireStatement */");
     } else if (statement instanceof IRMonitorLockReleaseStatement) {
       // TODO kcp
       builder.append("/* IRMonitorLockReleaseStatement */");
     } else if (statement instanceof IRNoOpStatement) {
       // no-op
     } else if (statement instanceof IRReturnStatement) {
       IRElement ancestor = statement.getParent();
       while (ancestor != null) {
         if (ancestor instanceof IRMethodStatement &&
                 ((IRMethodStatement) ancestor).getName().endsWith("init>")) {
           return;
         }
         ancestor = ancestor.getParent();
       }
       builder.append("return");
       IRReturnStatement returnStatement = (IRReturnStatement) statement;
       if (returnStatement.getReturnValue() != null) {
         builder.append(" ");
         appendExpression(builder, returnStatement.getReturnValue(), symbols);
       }
       builder.append(";\n");
     } else if (statement instanceof IRStatementList) {
       List<IRStatement> children = ((IRStatementList) statement).getStatements();
       builder.append("{\n");
       Map<String, IRSymbol> innerSymbols = Maps.newHashMap(symbols);
       IRMethodCallStatement ctorCall = null;
       if (statement.getParent() instanceof IRMethodStatement
               && ((IRMethodStatement) statement.getParent()).getName().equals("<init>")) {
         for (IRStatement child : children) {
           if (child instanceof IRMethodCallStatement) {
             IRMethodCallStatement methodCallStatement = (IRMethodCallStatement) child;
             if (methodCallStatement.getExpression() instanceof IRMethodCallExpression) {
               if (((IRMethodCallExpression) methodCallStatement.getExpression()).getName().equals("<init>")) {
                 ctorCall = methodCallStatement;
                 break;
               }
             }
             if (methodCallStatement.getExpression() instanceof IRCompositeExpression) {
               IRCompositeExpression compositeExpression =
                       (IRCompositeExpression) methodCallStatement.getExpression();
               IRElement lastElement =
                       compositeExpression.getElements().get(compositeExpression.getElements().size() - 1);
               if (lastElement instanceof IRMethodCallExpression
                       && ((IRMethodCallExpression) lastElement).getName().equals("<init>")) {
                 ctorCall = methodCallStatement;
                 break;
               }
             }
           }
         }
       }
       if (ctorCall != null) {
         appendStatement(builder, ctorCall, innerSymbols);
       }
       for (IRStatement child : children) {
         if (child != null && child != ctorCall) {
           appendStatement(builder, child, innerSymbols);
         }
       }
       builder.append("}\n");
     } else if (statement instanceof IRSwitchStatement) {
       // TODO kcp
       builder.append("/* IRSwitchStatement */");
     } else if (statement instanceof IRSyntheticStatement) {
       // TODO kcp
       builder.append("/* IRSyntheticStatement */");
     } else if (statement instanceof IRThrowStatement) {
       builder.append("throw ");
       appendExpression(builder, ((IRThrowStatement) statement).getException(), symbols);
       builder.append(";\n");
     } else if (statement instanceof IRTryCatchFinallyStatement) {
       builder.append("try ");
       IRTryCatchFinallyStatement tryCatchFinallyStatement = (IRTryCatchFinallyStatement) statement;
       appendStatement(builder, tryCatchFinallyStatement.getTryBody(), Maps.newHashMap(symbols));
       for (IRCatchClause catchClause : tryCatchFinallyStatement.getCatchStatements()) {
         builder.append(" catch (")
                 .append(getTypeName(catchClause.getIdentifier().getType()))
                 .append(" ")
                 .append(getSymbolName(catchClause.getIdentifier()))
                 .append(") ");
         appendStatement(builder, catchClause.getBody(), Maps.newHashMap(symbols));
       }
       if (tryCatchFinallyStatement.getFinallyBody() != null) {
         builder.append(" finally ");
         appendStatement(builder, tryCatchFinallyStatement.getFinallyBody(), Maps.newHashMap(symbols));
       }
     } else if (statement instanceof IRWhileStatement) {
       builder.append("while (");
       IRWhileStatement whileStatement = (IRWhileStatement) statement;
       appendExpression(builder, whileStatement.getLoopTest(), symbols);
       builder.append(") ");
       appendStatement(builder, whileStatement.getBody(), Maps.newHashMap(symbols));
     } else {
       throw new IllegalArgumentException("Unknown statement type " + statement.getClass().getSimpleName());
     }
   }
 
   private void appendExpression(StringBuilder builder, IRExpression expression, Map<String, IRSymbol> symbols) {
     if (expression instanceof IRArithmeticExpression) {
       IRArithmeticExpression arithmeticExpression = (IRArithmeticExpression) expression;
       appendExpression(builder, arithmeticExpression.getLhs(), symbols);
       builder.append(" ");
       switch (arithmeticExpression.getOp()) {
         case Addition:
           builder.append('+');
           break;
         case Subtraction:
           builder.append('-');
           break;
         case Multiplication:
           builder.append('*');
           break;
         case Division:
           builder.append('/');
           break;
         case Remainder:
           builder.append('%');
           break;
         case ShiftLeft:
           builder.append("<<");
           break;
         case ShiftRight:
           builder.append(">>");
           break;
         case UnsignedShiftRight:
           builder.append(">>>");
           break;
         case BitwiseAnd:
           builder.append('&');
           break;
         case BitwiseOr:
           builder.append('|');
           break;
         case BitwiseXor:
           builder.append('^');
           break;
       }
       builder.append(" ");
       appendExpression(builder, arithmeticExpression.getRhs(), symbols);
     } else if (expression instanceof IRArrayLengthExpression) {
       appendExpression(builder, ((IRArrayLengthExpression) expression).getRoot(), symbols);
       builder.append(".length");
     } else if (expression instanceof IRArrayLoadExpression) {
       IRArrayLoadExpression arrayLoadExpression = (IRArrayLoadExpression) expression;
       appendExpression(builder, arrayLoadExpression.getRoot(), symbols);
       builder.append("[");
       appendExpression(builder, arrayLoadExpression.getIndex(), symbols);
       builder.append("]");
     } else if (expression instanceof IRBooleanLiteral) {
       builder.append(((IRBooleanLiteral) expression).getValue());
     } else if (expression instanceof IRCastExpression) {
       builder.append("((")
               .append(getTypeName(expression.getType()))
               .append(") ");
       appendExpression(builder, ((IRCastExpression) expression).getRoot(), symbols);
       builder.append(")");
     } else if (expression instanceof IRCharacterLiteral) {
       builder.append("'")
               .append(((IRCharacterLiteral) expression).getValue())
               .append("'");
     } else if (expression instanceof IRClassLiteral) {
       builder.append(getTypeName(((IRClassLiteral) expression).getLiteralType()));
     } else if (expression instanceof IRCompositeExpression) {
       transformCompositeExpression(builder, (IRCompositeExpression) expression, symbols);
     } else if (expression instanceof IRConditionalAndExpression) {
       IRConditionalAndExpression andExpression = (IRConditionalAndExpression) expression;
       appendExpression(builder, andExpression.getLhs(), symbols);
       builder.append(" && ");
       appendExpression(builder, andExpression.getRhs(), symbols);
     } else if (expression instanceof IRConditionalOrExpression) {
       IRConditionalOrExpression orExpression = (IRConditionalOrExpression) expression;
       appendExpression(builder, orExpression.getLhs(), symbols);
       builder.append(" || ");
       appendExpression(builder, orExpression.getRhs(), symbols);
     } else if (expression instanceof IREqualityExpression) {
       IREqualityExpression equalityExpression = (IREqualityExpression) expression;
       appendExpression(builder, equalityExpression.getLhs(), symbols);
       builder.append(" == ");
       appendExpression(builder, equalityExpression.getRhs(), symbols);
     } else if (expression instanceof IRFieldGetExpression) {
       IRFieldGetExpression fieldGetExpression = (IRFieldGetExpression) expression;
       if (fieldGetExpression.getLhs() != null) {
         appendExpression(builder, fieldGetExpression.getLhs(), symbols);
         builder.append(".");
       } else if (!fieldGetExpression.getOwnersType().equals(irClass.getThisType())) {
         builder.append(getTypeName(fieldGetExpression.getOwnersType()))
                 .append(".");
       }
       builder.append(fieldGetExpression.getName());
     } else if (expression instanceof IRIdentifier) {
       builder.append(getSymbolName(((IRIdentifier) expression).getSymbol()));
     } else if (expression instanceof IRInstanceOfExpression) {
       IRInstanceOfExpression instanceOfExpression = (IRInstanceOfExpression) expression;
       appendExpression(builder, instanceOfExpression.getRoot(), symbols);
       builder.append(" instanceof ")
               .append(getTypeName(instanceOfExpression.getTestType()));
     } else if (expression instanceof IRMethodCallExpression) {
       IRMethodCallExpression methodCallExpression = (IRMethodCallExpression) expression;
       if (methodCallExpression.getOwnersType().getName()
               .equals("gw.internal.gosu.parser.expressions.AdditiveExpression")) {
         builder.append('(');
         appendExpression(builder, methodCallExpression.getArgs().get(1), symbols);
         builder.append(" + ");
         appendExpression(builder, methodCallExpression.getArgs().get(2), symbols);
         builder.append(')');
         return;
       }
       if (methodCallExpression.getOwnersType().getName().equals("gw.lang.reflect.TypeSystem")
               && methodCallExpression.getName().equals("get")) {
         appendExpression(builder, methodCallExpression.getArgs().get(0), symbols);
         builder.append(".class");
         return;
       }
       boolean skipName = false;
       String replacement = replacementMethods.get(
               getTypeName(methodCallExpression.getOwnersType()) + "." + methodCallExpression.getName());
       IRExpression root = methodCallExpression.getRoot();
       if (root != null) {
         if (root instanceof IRIdentifier && getSymbolName(((IRIdentifier) root).getSymbol()).equals("this")
                 && methodCallExpression.getName().equals("<init>")) {
           IRElement ancestor = expression.getParent();
           while (ancestor.getParent() != null && !(ancestor instanceof IRMethodStatement)) {
             ancestor = ancestor.getParent();
           }
           boolean isThis = false;
           for (IRMethodStatement ctor : ctors) {
             if (ctor == ancestor) {
               continue;
             }
             List<IRExpression> args = methodCallExpression.getArgs();
             if (args.size() == ctor.getParameters().size()) {
               boolean match = true;
               for (int i = 0, argsSize = args.size(); i < argsSize; i++) {
                 IRExpression arg = args.get(i);
                 if (!arg.getType().equals(ctor.getParameters().get(i).getType())) {
                   match = false;
                   break;
                 }
               }
               if (match) {
                 isThis = true;
                 break;
               }
             }
           }
           if (isThis) {
             builder.append("this(");
           } else {
             builder.append("super(");
           }
           skipName = true;
         } else {
           appendExpression(builder, root, symbols);
           builder.append(".");
         }
       } else {
         if (!irClass.getThisType().isAssignableFrom(methodCallExpression.getOwnersType()) && replacement == null) {
           builder.append(getTypeName(methodCallExpression.getOwnersType()))
                   .append(".");
         }
       }
       if (!skipName) {
         if (replacement != null) {
           builder.append(replacement);
         } else {
           builder.append(methodCallExpression.getName());
         }
         builder.append("(");
       }
       List<IRExpression> args = methodCallExpression.getArgs();
       for (int i = 0, argsSize = args.size(); i < argsSize; i++) {
         if (i > 0) {
           builder.append(", ");
         }
         appendExpression(builder, args.get(i), symbols);
       }
       builder.append(")");
     } else if (expression instanceof IRNegationExpression) {
       builder.append("-");
       appendExpression(builder, ((IRNegationExpression) expression).getRoot(), symbols);
     } else if (expression instanceof IRNewArrayExpression) {
       IRNewArrayExpression newArrayExpression = (IRNewArrayExpression) expression;
       builder.append("new ")
               .append(getTypeName(newArrayExpression.getComponentType()))
               .append("[");
       if (newArrayExpression.getSizeExpression() != null) {
         appendExpression(builder, newArrayExpression.getSizeExpression(), symbols);
       }
       builder.append("]");
     } else if (expression instanceof IRNewExpression) {
       IRNewExpression newExpression = (IRNewExpression) expression;
       List<IRExpression> args = newExpression.getArgs();
       if (TypeSystem.getByFullName("gwtsu.JSONOverlay")
               .isAssignableFrom(newExpression.getOwnersType().getType()) &&
               args.size() == 1 &&
               args.get(0).getType().getName().equals("java.lang.String")) {
         builder.append(getTypeName(newExpression.getOwnersType()))
             .append(".$gwtsu$parse(");
         appendExpression(builder, args.get(0), symbols);
         builder.append(")");
       } else {
         builder.append("new ")
                 .append(getTypeName(newExpression.getOwnersType()))
                 .append("(");
         for (int i = 0, argsSize = args.size(); i < argsSize; i++) {
           if (i > 0) {
             builder.append(", ");
           }
           appendExpression(builder, args.get(i), symbols);
         }
         builder.append(")");
       }
     } else if (expression instanceof IRNewMultiDimensionalArrayExpression) {
       IRNewMultiDimensionalArrayExpression arrayExpression = (IRNewMultiDimensionalArrayExpression) expression;
       builder.append("new ")
               .append(getTypeName(arrayExpression.getResultType()));
       for (IRExpression size : arrayExpression.getSizeExpressions()) {
         builder.append("[");
         appendExpression(builder, size, symbols);
         builder.append("]");
       }
     } else if (expression instanceof IRNotExpression) {
       builder.append("!");
       appendExpression(builder, ((IRNotExpression) expression).getRoot(), symbols);
     } else if (expression instanceof IRNoOpExpression) {
       // no-op
     } else if (expression instanceof IRNullLiteral) {
       builder.append("null");
     } else if (expression instanceof IRNumericLiteral) {
       builder.append(((IRNumericLiteral) expression).getValue());
     } else if (expression instanceof IRPrimitiveTypeConversion) {
       // TODO kcp
       builder.append("/* IRPrimitiveTypeConversion */");
     } else if (expression instanceof IRRelationalExpression) {
       IRRelationalExpression relationalExpression = (IRRelationalExpression) expression;
       appendExpression(builder, relationalExpression.getLhs(), symbols);
       builder.append(" ");
       switch (relationalExpression.getOp()) {
         case GT:
           builder.append('>');
           break;
         case GTE:
           builder.append(">=");
           break;
         case LT:
           builder.append('<');
           break;
         case LTE:
           builder.append("<=");
           break;
       }
       builder.append(" ");
       appendExpression(builder, relationalExpression.getRhs(), symbols);
     } else if (expression instanceof IRStringLiteralExpression) {
       builder.append("\"")
               .append(((IRStringLiteralExpression) expression).getValue()
                       .replace("\"", "\\\""))
               .append("\"");
     } else if (expression instanceof IRTernaryExpression) {
       IRTernaryExpression ternaryExpression = (IRTernaryExpression) expression;
       appendExpression(builder, ternaryExpression.getTest(), symbols);
       builder.append(" ? ");
       appendExpression(builder, ternaryExpression.getTrueValue(), symbols);
       builder.append(" : ");
       appendExpression(builder, ternaryExpression.getFalseValue(), symbols);
     } else {
       throw new IllegalArgumentException("Unknown expression type " + expression.getClass().getSimpleName());
     }
   }
 
   private void appendAnnotation(StringBuilder builder, IRAnnotation annotation) {
     builder.append("@")
             .append(getTypeName(annotation.getDescriptor()))
                     // TODO kcp - args?
             .append("\n");
   }
 
   private void transformCompositeExpression(StringBuilder builder, IRCompositeExpression expression, Map<String, IRSymbol> symbols) {
     StringBuilder auxMethodsBuilder = new StringBuilder();
     auxMethodsBuilders.push(auxMethodsBuilder);
     List<IRElement> elements = expression.getElements();
     IRElement lastElement = elements.get(elements.size() - 1);
     // Null-safe property/method access?
     if (elements.size() == 2 &&
             elements.get(0) instanceof IRAssignmentStatement &&
             elements.get(1) instanceof IRTernaryExpression) {
       IRTernaryExpression ternaryExpression = (IRTernaryExpression) elements.get(1);
       if (ternaryExpression.getTrueValue() instanceof IRCastExpression &&
               ((IRCastExpression) ternaryExpression.getTrueValue()).getRoot() instanceof IRNullLiteral &&
               ternaryExpression.getTest() instanceof IREqualityExpression &&
               ((IREqualityExpression) ternaryExpression.getTest()).getRhs() instanceof IRNullLiteral) {
         IRAssignmentStatement assignmentStatement = (IRAssignmentStatement) elements.get(0);
         String rootTypeName = getTypeName(assignmentStatement.getSymbol().getType());
         String rtnTypeName = getTypeName(ternaryExpression.getResultType());
         String auxMethodName = "$gwtsu$aux" + (uid++);
         String argName = getSymbolName(assignmentStatement.getSymbol());
         builder.append(auxMethodName)
                 .append("(");
         appendExpression(builder, assignmentStatement.getValue(), symbols);
         appendSymbolsAsArgs(builder, symbols, true);
         builder.append(")");
         auxMethodsBuilder.append("private ");
         if (isStatic) {
           auxMethodsBuilder.append("static ");
         }
         auxMethodsBuilder.append(rtnTypeName)
                 .append(auxMethodName)
                 .append("(")
                 .append(rootTypeName)
                 .append(" ")
                 .append(argName);
         appendSymbolsAsParams(auxMethodsBuilder, symbols, true);
         auxMethodsBuilder.append(") {\n")
                 .append("return ");
         HashMap<String, IRSymbol> tempSymbols = Maps.newHashMap(symbols);
         appendExpression(auxMethodsBuilder, ternaryExpression, tempSymbols);
         auxMethodsBuilder.append(";\n}\n");
         return;
       }
     }
     // Field set(s) + ctor (this/super) call?
     if (lastElement instanceof IRMethodCallExpression &&
             ((IRMethodCallExpression) lastElement).getName().equals("<init>")) {
       boolean areAllStatements = true;
       for (IRElement element : elements) {
         areAllStatements = areAllStatements && (element instanceof IRStatement || element == lastElement);
       }
       if (areAllStatements) {
         appendExpression(builder, (IRMethodCallExpression) lastElement, symbols);
         builder.append(";\n");
         for (IRElement element : elements) {
           if (element instanceof IRStatement) {
             appendStatement(builder, (IRStatement) element, symbols);
           }
         }
         return;
       }
     }
     // TODO kcp - check for other types of known composite expressions
     // Fallback: if elements are statements + 1 expression, transform directly into aux method.
     if (lastElement instanceof IRExpression) {
       boolean areAllStatements = true;
       for (IRElement element : elements) {
         areAllStatements = areAllStatements && (element instanceof IRStatement || element == lastElement);
       }
       if (areAllStatements) {
         String auxMethodName = "$gwtsu$aux" + (uid++);
         builder.append(auxMethodName)
                 .append("(");
         appendSymbolsAsArgs(builder, symbols, false);
         builder.append(")");
         auxMethodsBuilder.append("private ");
         if (isStatic) {
           auxMethodsBuilder.append("static ");
         }
         auxMethodsBuilder.append(getTypeName(((IRExpression) lastElement).getType()))
                 .append(" ")
                 .append(auxMethodName)
                 .append("(");
         appendSymbolsAsParams(auxMethodsBuilder, symbols, false);
         auxMethodsBuilder.append(") {\n");
         HashMap<String,IRSymbol> tempSymbols = Maps.newHashMap(symbols);
         for (IRElement element : elements) {
           if (element instanceof IRStatement) {
             appendStatement(auxMethodsBuilder, (IRStatement) element, tempSymbols);
           } else {
             auxMethodsBuilder.append("return ");
             appendExpression(auxMethodsBuilder, (IRExpression) element, tempSymbols);
             auxMethodsBuilder.append(";\n");
           }
         }
         auxMethodsBuilder.append("}\n");
         return;
       }
     }
     // TODO kcp
     builder.append("/* unknown IRCompositeExpression */");
   }
 
   private void appendSymbolsAsArgs(StringBuilder builder, Map<String, IRSymbol> symbols, boolean comma) {
     for (Map.Entry<String, IRSymbol> symbol : symbols.entrySet()) {
       if (comma) {
         builder.append(", ");
       }
       comma = true;
       builder.append(symbol.getKey());
     }
   }
 
   private void appendSymbolsAsParams(StringBuilder builder, Map<String, IRSymbol> symbols, boolean comma) {
     for (Map.Entry<String, IRSymbol> symbol : symbols.entrySet()) {
       if (comma) {
         builder.append(", ");
       }
       comma = true;
       builder.append(getTypeName(symbol.getValue().getType()))
               .append(" ")
               .append(symbol.getKey());
     }
   }
 
   private String getSymbolName(IRSymbol symbol) {
     return symbol.getName().replace("*", "gwtsu$");
   }
 
   private String getTypeName(IRType type) {
     IType iType = type.getType();
     if (replacementTypes.containsKey(iType.getName())) {
       return replacementTypes.get(iType.getName());
     }
     if (iType instanceof IGosuClass) {
       return ((IGosuClass) iType).getBackingClass().getName().replace("$", "__");
     }
     return iType.getName();
   }
 
   private String getRelativeClassName() {
     String typeName = getTypeName(irClass.getThisType());
     return typeName.substring(typeName.lastIndexOf(".") + 1);
   }
 
   private List<IType> getExceptionsFromSuper(IRMethodStatement method) {
     IType type = irClass.getThisType().getType();
     for (IType ancestor : type.getAllTypesInHierarchy()) {
       if (ancestor instanceof IJavaType) {
         Class javaClass = ((IJavaType) ancestor).getBackingClass();
         for (Method ancestorMethod : javaClass.getMethods()) {
           if (ancestorMethod.getName().equals(method.getName()) &&
                   ancestorMethod.getParameterTypes().length == method.getParameters().size()) {
             boolean match = true;
             Class<?>[] parameterTypes = ancestorMethod.getParameterTypes();
             for (int i = 0, parameterTypesLength = parameterTypes.length; i < parameterTypesLength; i++) {
               Class<?> paramType = parameterTypes[i];
               if (!paramType.getName().equals(method.getParameters().get(i).getType().getName())) {
                 match = false;
                 break;
               }
             }
             if (match) {
               List<IType> exceptions = Lists.newArrayList();
               for (Class<?> exceptionType : ancestorMethod.getExceptionTypes()) {
                 exceptions.add(TypeSystem.getByFullNameIfValid(exceptionType.getName()));
               }
               return exceptions;
             }
           }
         }
       }
     }
     return null;
   }
 
 }
