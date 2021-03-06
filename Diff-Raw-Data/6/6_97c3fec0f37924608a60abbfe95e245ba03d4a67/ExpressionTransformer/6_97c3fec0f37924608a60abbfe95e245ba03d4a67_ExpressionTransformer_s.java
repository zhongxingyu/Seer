 package com.redhat.ceylon.compiler.codegen;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 
 import com.redhat.ceylon.compiler.typechecker.model.Declaration;
 import com.redhat.ceylon.compiler.typechecker.model.Functional;
 import com.redhat.ceylon.compiler.typechecker.model.Getter;
 import com.redhat.ceylon.compiler.typechecker.model.Method;
 import com.redhat.ceylon.compiler.typechecker.model.Parameter;
 import com.redhat.ceylon.compiler.typechecker.model.ParameterList;
 import com.redhat.ceylon.compiler.typechecker.model.ProducedType;
 import com.redhat.ceylon.compiler.typechecker.model.TypedDeclaration;
 import com.redhat.ceylon.compiler.typechecker.model.Value;
 import com.redhat.ceylon.compiler.typechecker.tree.Node;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree.AndOp;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree.AssignOp;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree.BaseMemberOrTypeExpression;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree.BaseTypeExpression;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree.BinaryOperatorExpression;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree.Expression;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree.InvocationExpression;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree.NamedArgument;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree.OrOp;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree.Outer;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree.PositionalArgument;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree.PositionalArgumentList;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree.Primary;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree.QualifiedMemberOrTypeExpression;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree.QualifiedTypeExpression;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree.SequenceEnumeration;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree.Super;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree.Term;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree.This;
 import com.redhat.ceylon.compiler.util.Util;
 import com.sun.tools.javac.code.Flags;
 import com.sun.tools.javac.code.TypeTags;
 import com.sun.tools.javac.tree.JCTree;
 import com.sun.tools.javac.tree.JCTree.JCBinary;
 import com.sun.tools.javac.tree.JCTree.JCClassDecl;
 import com.sun.tools.javac.tree.JCTree.JCExpression;
 import com.sun.tools.javac.tree.JCTree.JCLiteral;
 import com.sun.tools.javac.tree.JCTree.JCNewClass;
 import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
 import com.sun.tools.javac.tree.JCTree.JCUnary;
 import com.sun.tools.javac.util.Context;
 import com.sun.tools.javac.util.List;
 import com.sun.tools.javac.util.ListBuffer;
 
 /**
  * This transformer deals with expressions only
  */
 public class ExpressionTransformer extends AbstractTransformer {
 
     public static ExpressionTransformer getInstance(Context context) {
         ExpressionTransformer trans = context.get(ExpressionTransformer.class);
         if (trans == null) {
             trans = new ExpressionTransformer(context);
             context.put(ExpressionTransformer.class, trans);
         }
         return trans;
     }
 
 	private ExpressionTransformer(Context context) {
         super(context);
     }
 
     JCExpression transformExpression(final Tree.Term expr) {
         CeylonVisitor v = new CeylonVisitor(gen());
         if (expr instanceof Tree.Expression) {
             expr.visitChildren(v);
         } else {
             expr.visit(v);
         }
         return v.getSingleResult();
     }
 
     JCExpression transformExpression(final Tree.Term expr, ProducedType targetType) {
         JCExpression result = transformExpression(expr);
         
         ProducedType exprType = expr.getTypeModel();
         result = boxUnboxIfNecessary(result, exprType, targetType);
         
         return result;
     }
     
     public JCExpression transformStringExpression(Tree.StringTemplate expr) {
         ListBuffer<JCExpression> strings = new ListBuffer<JCExpression>();
         for (Tree.Expression t : expr.getExpressions()) {
             strings.append(transformExpression(t));
         }
 
         // FIXME Do something with a StringBuffer and multiple appends()s
         return make().Apply(null, makeSelect(makeIdent(syms().ceylonStringType), "instance"), strings.toList());
     }
 
     private static Map<Class<? extends Tree.UnaryOperatorExpression>, String> unaryOperators;
     private static Map<Class<? extends Tree.BinaryOperatorExpression>, String> binaryOperators;
 
     static {
         unaryOperators = new HashMap<Class<? extends Tree.UnaryOperatorExpression>, String>();
         binaryOperators = new HashMap<Class<? extends Tree.BinaryOperatorExpression>, String>();
 
         // Unary operators
         unaryOperators.put(Tree.NegativeOp.class, "inverse");
         unaryOperators.put(Tree.NotOp.class, "complement");
         unaryOperators.put(Tree.FormatOp.class, "string");
 
         // Binary operators that act on types
         binaryOperators.put(Tree.SumOp.class, "plus");
         binaryOperators.put(Tree.DifferenceOp.class, "minus");
         binaryOperators.put(Tree.ProductOp.class, "times");
         binaryOperators.put(Tree.QuotientOp.class, "divided");
         binaryOperators.put(Tree.PowerOp.class, "power");
         binaryOperators.put(Tree.RemainderOp.class, "remainder");
         binaryOperators.put(Tree.IntersectionOp.class, "and");
         binaryOperators.put(Tree.UnionOp.class, "or");
         binaryOperators.put(Tree.XorOp.class, "xor");
         binaryOperators.put(Tree.EqualOp.class, "equalsXXX");
         binaryOperators.put(Tree.IdenticalOp.class, "identical");
         binaryOperators.put(Tree.CompareOp.class, "compare");
 
         // Binary operators that act on intermediary Comparison objects
         binaryOperators.put(Tree.LargerOp.class, "larger");
         binaryOperators.put(Tree.SmallerOp.class, "smaller");
         binaryOperators.put(Tree.LargeAsOp.class, "largeAs");
         binaryOperators.put(Tree.SmallAsOp.class, "smallAs");
     }
 
     // FIXME: I'm pretty sure sugar is not supposed to be in there
     public JCExpression transform(Tree.NotEqualOp op) {
         Tree.EqualOp newOp = new Tree.EqualOp(op.getAntlrTreeNode());
         newOp.setLeftTerm(op.getLeftTerm());
         newOp.setRightTerm(op.getRightTerm());
         Tree.NotOp newNotOp = new Tree.NotOp(op.getAntlrTreeNode());
         newNotOp.setTerm(newOp);
         return transform(newNotOp);
     }
 
     public JCExpression transform(Tree.NotOp op) {
         JCExpression term = transformExpression(op.getTerm());
         JCUnary jcu = at(op).Unary(JCTree.NOT, term);
         return jcu;
     }
 
     public JCExpression transform(Tree.AssignOp op) {
         return transformAssignment(op, op.getLeftTerm(), op.getRightTerm());
     }
 
     JCExpression transformAssignment(Node op, Term leftTerm, Term rightTerm) {
         // right side is easy
         JCExpression rhs = transformExpression(rightTerm, leftTerm.getTypeModel());
         
         // left side depends
         // FIXME: can this be anything else than a Primary?
         Declaration decl = ((Tree.Primary)leftTerm).getDeclaration();
 
         // FIXME: can this be anything else than a Value or a TypedDeclaration?
         boolean variable = false;
         if (decl instanceof Value) {
             variable = ((Value)decl).isVariable();
         } else if (decl instanceof TypedDeclaration) {
             variable = ((TypedDeclaration)decl).isVariable();
         }
         if(decl.isToplevel()){
             // must use top level setter
             return globalGen().setGlobalValue(
                 makeIdent(decl.getContainer().getQualifiedNameString()),
                 decl.getName(), rhs);
         } else if ((decl instanceof Getter)) {
             // must use the setter
             if (decl.getContainer() instanceof Method){
                 return at(op).Apply(List.<JCTree.JCExpression>nil(), makeIdent(decl.getName() + "$setter", Util.getSetterName(decl.getName())), 
                         List.<JCTree.JCExpression>of(rhs));
             } else {
                 return at(op).Apply(List.<JCTree.JCExpression>nil(), makeIdent(Util.getSetterName(decl.getName())), 
                         List.<JCTree.JCExpression>of(rhs));            
             }
         } else if(variable && (Util.isClassAttribute(decl))){
             // must use the setter
             return at(op).Apply(List.<JCTree.JCExpression>nil(), makeIdent(Util.getSetterName(decl.getName())), 
                     List.<JCTree.JCExpression>of(rhs));
         } else if(variable && decl.isCaptured()){
             // must use the qualified setter
             return at(op).Apply(List.<JCTree.JCExpression>nil(), makeIdent(decl.getName(), Util.getSetterName(decl.getName())), 
                     List.<JCTree.JCExpression>of(rhs));
         } else {
             return at(op).Assign(make().Ident(names().fromString(decl.getName())), rhs);
         }
     }
 
     public JCExpression transform(Tree.IsOp op) {
         JCExpression type = makeJavaType(op.getType().getTypeModel());
         return at(op).TypeTest(transformExpression(op.getTerm()), type);
     }
 
     public JCExpression transform(Tree.RangeOp op) {
         JCExpression lower = boxType(transformExpression(op.getLeftTerm()), op.getLeftTerm().getTypeModel());
         JCExpression upper = boxType(transformExpression(op.getRightTerm()), op.getRightTerm().getTypeModel());
         ProducedType rangeType = typeFact().makeRangeType(op.getLeftTerm().getTypeModel());
         JCExpression typeExpr = makeJavaType(rangeType, CeylonTransformer.CLASS_NEW);
         return at(op).NewClass(null, null, typeExpr, List.<JCExpression> of(lower, upper), null);
     }
 
     public JCExpression transform(Tree.EntryOp op) {
         JCExpression key = boxType(transformExpression(op.getLeftTerm()), op.getLeftTerm().getTypeModel());
         JCExpression elem = boxType(transformExpression(op.getRightTerm()), op.getRightTerm().getTypeModel());
         ProducedType entryType = typeFact().makeEntryType(op.getLeftTerm().getTypeModel(), op.getRightTerm().getTypeModel());
         JCExpression typeExpr = makeJavaType(entryType, CeylonTransformer.CLASS_NEW);
         return at(op).NewClass(null, null, typeExpr , List.<JCExpression> of(key, elem), null);
     }
 
     public JCExpression transform(Tree.UnaryOperatorExpression op) {
         at(op);
         Tree.Term term = op.getTerm();
         if (term instanceof Tree.NaturalLiteral && op instanceof Tree.NegativeOp) {
             Tree.NaturalLiteral lit = (Tree.NaturalLiteral) term;
             return makeInteger(-Long.parseLong(lit.getText()));
         } else if (term instanceof Tree.NaturalLiteral && op instanceof Tree.PositiveOp) {
             Tree.NaturalLiteral lit = (Tree.NaturalLiteral) term;
             return makeInteger(Long.parseLong(lit.getText()));
         }
         return make().Apply(null, makeSelect(transformExpression(term), unaryOperators.get(op.getClass())), List.<JCExpression> nil());
     }
 
     public JCExpression transform(Tree.ArithmeticAssignmentOp op){
         // desugar it
         Tree.BinaryOperatorExpression newOp;
         if(op instanceof Tree.AddAssignOp)
             newOp = new Tree.SumOp(op.getAntlrTreeNode());
         else if(op instanceof Tree.SubtractAssignOp)
             newOp = new Tree.DifferenceOp(op.getAntlrTreeNode());
         else if(op instanceof Tree.MultiplyAssignOp)
             newOp = new Tree.ProductOp(op.getAntlrTreeNode());
         else if(op instanceof Tree.DivideAssignOp)
             newOp = new Tree.QuotientOp(op.getAntlrTreeNode());
         else if(op instanceof Tree.RemainderAssignOp)
             newOp = new Tree.RemainderOp(op.getAntlrTreeNode());
         else
             throw new RuntimeException("Unsupported operator: "+op);
         return desugarAssignmentOp(op, newOp);
     }
     
     public JCExpression transform(Tree.BitwiseAssignmentOp op){
         // desugar it
         Tree.BinaryOperatorExpression newOp;
         if(op instanceof Tree.ComplementAssignOp)
             newOp = new Tree.ComplementOp(op.getAntlrTreeNode());
         else if(op instanceof Tree.UnionAssignOp)
             newOp = new Tree.UnionOp(op.getAntlrTreeNode());
         else if(op instanceof Tree.XorAssignOp)
             newOp = new Tree.XorOp(op.getAntlrTreeNode());
         else if(op instanceof Tree.IntersectAssignOp)
             newOp = new Tree.IntersectionOp(op.getAntlrTreeNode());
         else
             throw new RuntimeException("Unsupported operator: "+op);
         return desugarAssignmentOp(op, newOp);
     }
 
     public JCExpression transform(Tree.LogicalAssignmentOp op){
         // desugar it
         Tree.BinaryOperatorExpression newOp;
         if(op instanceof Tree.AndAssignOp)
             newOp = new Tree.AndOp(op.getAntlrTreeNode());
         else if(op instanceof Tree.OrAssignOp)
             newOp = new Tree.OrOp(op.getAntlrTreeNode());
         else
             throw new RuntimeException("Unsupported operator: "+op);
         return desugarAssignmentOp(op, newOp);
     }
 
     // FIXME GET RID OF THIS, IT'S WRONG!!
     private JCExpression desugarAssignmentOp(Tree.AssignmentOp op, BinaryOperatorExpression newOp) {
         newOp.setLeftTerm(op.getLeftTerm());
         newOp.setRightTerm(op.getRightTerm());
         
         AssignOp assignOp = new Tree.AssignOp(op.getAntlrTreeNode());
         assignOp.setLeftTerm(op.getLeftTerm());
         assignOp.setRightTerm(newOp);
         assignOp.setTypeModel(op.getTypeModel());
         newOp.setTypeModel(op.getTypeModel());
         return transform(assignOp);
     }
 
     public JCExpression transform(Tree.BinaryOperatorExpression op) {
         JCExpression result = null;
         Class<? extends Tree.OperatorExpression> operatorClass = op.getClass();
 
         boolean loseComparison = op instanceof Tree.SmallAsOp || op instanceof Tree.SmallerOp || op instanceof Tree.LargerOp || op instanceof Tree.LargeAsOp;
 
         if (loseComparison)
             operatorClass = Tree.CompareOp.class;
 
         JCExpression left = transformExpression(op.getLeftTerm());
         JCExpression right = transformExpression(op.getRightTerm());
         result = at(op).Apply(null, makeSelect(left, binaryOperators.get(operatorClass)), List.of(right));
 
         if (loseComparison) {
             result = at(op).Apply(null, makeSelect(result, binaryOperators.get(op.getClass())), List.<JCExpression> nil());
         }
 
         return result;
     }
 
     public JCExpression transform(Tree.LogicalOp op) {
         JCExpression left = transformExpression(op.getLeftTerm());
         JCExpression right = transformExpression(op.getRightTerm());
 
         JCBinary jcb = null;
         if (op instanceof AndOp) {
             jcb = at(op).Binary(JCTree.AND, left, right);
         }
         if (op instanceof OrOp) {
             jcb = at(op).Binary(JCTree.OR, left, right);
         }
         return jcb;
     }
     
     JCExpression transform(Tree.PostfixOperatorExpression expr) {
         String methodName;
         boolean successor;
         if (expr instanceof Tree.PostfixIncrementOp){
             successor = true;
             methodName = "getPredecessor";
         }else if (expr instanceof Tree.PostfixDecrementOp){
             successor = false;
             methodName = "getSuccessor";
         }else
             throw new RuntimeException("Not implemented: " + expr.getNodeType());
         JCExpression op = makePrefixOp(expr, expr.getTerm(), successor);
         return at(expr).Apply(null, makeSelect(op, methodName), List.<JCExpression>nil());
     }
 
     public JCExpression transform(Tree.PrefixOperatorExpression expr) {
         boolean successor;
         if (expr instanceof Tree.IncrementOp)
             successor = true;
         else if (expr instanceof Tree.DecrementOp)
             successor = false;
         else
             throw new RuntimeException("Not implemented: " + expr.getNodeType());
         return makePrefixOp(expr, expr.getTerm(), successor);
     }
 
     private JCExpression makePrefixOp(Node expr, Term term, boolean successor) {
         String methodName;
         if (successor)
             methodName = "getSuccessor";
         else
             methodName = "getPredecessor";
         JCExpression operand = transformExpression(term);
         return at(expr).Assign(operand, at(expr).Apply(null, makeSelect(operand, methodName), List.<JCExpression>nil()));
     }
 
     JCExpression transform(Tree.InvocationExpression ce) {
         if (ce.getPositionalArgumentList() != null) {
             return transformPositionalInvocation(ce);
         } else if (ce.getNamedArgumentList() != null) {
             return transformNamedInvocation(ce);
         } else {
             throw new RuntimeException("Illegal State");
         }
     }
     
     /**
      * <p>Converts a type to the corresponding Java type 
      * <strong>suitable for a named argument invocation</strong>.</p> 
      * 
      * <p>The rules for converting to Java types for invocation are different to 
      * elsewhere, because we need to produce Java wrapper types 
      * (for the purposes of casts and type parameters) for Ceylon types which 
      * would otherwise be erased to Java primitive types. For example
      * given {@code ceylon.language.Natural} this method will return 
      * {@code java.lang.Long}, whereas {@link #makeJavaType(ProducedType, int)} 
      * would return either {@code long} or {@code ceylon.language.Natural} 
      * depending on the flags.</p>
      *   
      * @param type The type to convert
      * @return The converted type
      */
     private JCExpression makeJavaTypeForInvocation(ProducedType type) {
         if (!typeFact().isOptional(type)) {
             if (isCeylonBoolean(type)) {
                 return make().Type(syms().booleanObjectType);
             } else if (isCeylonNatural(type)) {
                 return make().Type(syms().longObjectType);
             } else if (isCeylonInteger(type)) {
                 return make().Type(syms().integerObjectType);
             } else if (isCeylonFloat(type)) {
                 return make().Type(syms().doubleObjectType);
             } else if (isCeylonCharacter(type)) {
                 return make().Type(syms().characterObjectType);
             }
         }
         return makeJavaType(type);
     }
 
     private JCExpression transformNamedInvocation(InvocationExpression ce) {
         final ListBuffer<JCExpression> callArgs = new ListBuffer<JCExpression>();
         final ListBuffer<JCExpression> passArgs = new ListBuffer<JCExpression>();
         final ProducedType resultType;
         final String methodName;
         final java.util.List<ParameterList> parameterLists;
         final Primary primary = ce.getPrimary();
         final Declaration primaryDecl = primary.getDeclaration();
         if (primaryDecl instanceof Method) {
             Method methodDecl = (Method)primaryDecl;
             methodName = methodDecl.getName();
             resultType = methodDecl.getType();
             parameterLists = methodDecl.getParameterLists();
         } else if (primaryDecl instanceof com.redhat.ceylon.compiler.typechecker.model.Class) {
             com.redhat.ceylon.compiler.typechecker.model.Class methodDecl = (com.redhat.ceylon.compiler.typechecker.model.Class)primaryDecl;
             methodName = methodDecl.getName();
             resultType = methodDecl.getType();
             parameterLists = methodDecl.getParameterLists();
         } else {
             throw new RuntimeException("Illegal State: " + (primaryDecl != null ? primaryDecl.getClass() : "null"));
         }
         java.util.List<NamedArgument> namedArguments = ce.getNamedArgumentList().getNamedArguments();
         java.util.List<Parameter> declaredParams = parameterLists.get(0).getParameters();
         for (Parameter declaredParam : declaredParams) {
             boolean found = false;
             int index = 0;
             for (NamedArgument namedArg : namedArguments) {
                 if (declaredParam.getName().equals(namedArg.getIdentifier().getText())) {
                     callArgs.append(make().TypeCast(makeJavaTypeForInvocation(declaredParam.getType()), make().Indexed(makeSelect("this", "args"), makeInteger(index))));
                     found = true;
                     break;
                 }
                 index += 1;
             }
             if (!found) {
                 throw new RuntimeException("No value specified for argument '" + declaredParam.getName()+ "' and default values not implemented yet");
             }
         }
 
         for (NamedArgument namedArg : namedArguments) {
             passArgs.append(transformArg(namedArg));
         }
 
         // Here I need to get everything 'but the last bit' from the Primary
         // e.g. f() -> this
         //   foo().bar() -> foo()
         //   bar("fdfklvnjk").baz() -> bar("fdfklvnjk")
         JCExpression receiverType;
         final boolean generateNew;
         final boolean haveInstance;
         if (primary instanceof BaseMemberOrTypeExpression) {
             passArgs.prepend(make().Literal(TypeTags.BOT, null));
             receiverType = makeIdent("java.lang.Void");
             generateNew = primary instanceof BaseTypeExpression;
             haveInstance = false;
         } else if (primary instanceof QualifiedMemberOrTypeExpression) {
             QualifiedMemberOrTypeExpression memberExpr = (QualifiedMemberOrTypeExpression)primary;
             CeylonVisitor visitor = new CeylonVisitor(gen(), callArgs);
             memberExpr.getPrimary().visit(visitor);
             passArgs.prepend((JCExpression)visitor.getSingleResult());
             receiverType = makeJavaType(memberExpr.getPrimary().getTypeModel(), this.TYPE_PARAM);
             generateNew = primary instanceof QualifiedTypeExpression;
             haveInstance = true;
         } else {
             // TODO: Handle all the other possibilities: Should use visitor?
             throw new RuntimeException("Not Implemented: Named argument calls only implemented on member and type expressions");
         }
 
         // Construct the call$() method
         final String callMethodName = "call$";
         MethodDefinitionBuilder callMethod = MethodDefinitionBuilder.method(gen(), callMethodName);
         callMethod.modifiers(Flags.PUBLIC);
         callMethod.resultType(makeJavaTypeForInvocation(resultType));
         if (generateNew) {
             callMethod.body(make().Return(make().NewClass(null,
                     null, makeJavaTypeForInvocation(resultType), callArgs.toList(), null)));
         } else {
             if (haveInstance) {
                 callMethod.body(make().Return(make().Apply(null,
                         makeSelect("this", "instance", methodName), callArgs.toList())));
             } else {
                 callMethod.body(make().Return(make().Apply(null,
                         makeIdent(methodName), callArgs.toList())));
             }
         }
 
         // Construct the class
         JCExpression namedArgsClass = make().TypeApply(makeIdent(syms().ceylonNamedArgumentCall),
                 List.<JCExpression>of(makeJavaTypeForInvocation(resultType), 
                         receiverType));
 
         JCClassDecl classDecl = make().ClassDef(make().Modifiers(0),
                 names().empty,
                 List.<JCTypeParameter>nil(),
                 namedArgsClass,
                 List.<JCExpression>nil(),
                 List.<JCTree>of(callMethod.build()));
 
         // Create an instance of the class
         JCNewClass newClass = make().NewClass(null,
                 null,
                 namedArgsClass,
                 passArgs.toList(),
                 classDecl);
 
         // Call the call$() method
         return make().Apply(null,
                 makeSelect(newClass, callMethodName), List.<JCExpression>nil());
     }
 
     private JCExpression transformPositionalInvocation(InvocationExpression ce) {
         final ListBuffer<JCExpression> args = new ListBuffer<JCExpression>();
 
         boolean isVarargs = false;
         Declaration primaryDecl = ce.getPrimary().getDeclaration();
         PositionalArgumentList positional = ce.getPositionalArgumentList();
         if (primaryDecl instanceof Method) {
             Method methodDecl = (Method)primaryDecl;
             java.util.List<Parameter> declaredParams = methodDecl.getParameterLists().get(0).getParameters();
             int numDeclared = declaredParams.size();
             java.util.List<PositionalArgument> passedArguments = positional.getPositionalArguments();
             int numPassed = passedArguments.size();
             Parameter lastDeclaredParam = declaredParams.isEmpty() ? null : declaredParams.get(declaredParams.size() - 1); 
             if (lastDeclaredParam != null 
                     && lastDeclaredParam.isSequenced()
                     && positional.getEllipsis() == null) {// foo(sequence...) syntax => no need to box
                 // => call to a varargs method
                 isVarargs = true;
                 // first, append the normal args
                 for (int ii = 0; ii < numDeclared - 1; ii++) {
                     Tree.PositionalArgument arg = positional.getPositionalArguments().get(ii);
                     args.append(transformArg(arg));
                 }
                 JCExpression boxed;
                 // then, box the remaining passed arguments
                 if (numDeclared -1 == numPassed) {
                     // box as Empty
                     boxed = makeEmpty();
                 } else {
                     // box with an ArraySequence<T>
                     List<Expression> x = List.<Expression>nil();
                     for (int ii = numDeclared - 1; ii < numPassed; ii++) {
                         Tree.PositionalArgument arg = positional.getPositionalArguments().get(ii);
                         x = x.append(arg.getExpression());
                     }
                     ProducedType seqElemType = typeFact().getIteratedType(lastDeclaredParam.getType());
                     boxed = makeSequence(x, seqElemType);
                 }
                 args.append(boxed);
             }
         }
 
         if (!isVarargs) {
             for (Tree.PositionalArgument arg : positional.getPositionalArguments())
                 args.append(transformArg(arg));
         }
 
         CeylonVisitor visitor = new CeylonVisitor(gen(), args);
         ce.getPrimary().visit(visitor);
 
         JCExpression expr = visitor.getSingleResult();
         if (expr == null) {
             throw new RuntimeException();
         } else if (expr instanceof JCTree.JCNewClass) {
             return expr;
         } else {
             return at(ce).Apply(null, expr, args.toList());
         }
     }
 
     JCExpression transformArg(Tree.PositionalArgument arg) {
         return transformExpression(arg.getExpression(), refinedParameter(arg.getParameter()).getType());
     }
 
     JCExpression transformArg(Tree.NamedArgument arg) {
         if (arg instanceof Tree.SpecifiedArgument) {
             return transformExpression(((Tree.SpecifiedArgument)arg).getSpecifierExpression().getExpression(), refinedParameter(arg.getParameter()).getType());
         } else if (arg instanceof Tree.TypedArgument) {
             throw new RuntimeException("Not yet implemented");
         } else {
             throw new RuntimeException("Illegal State");
         }
     }
 
     private Parameter refinedParameter(Parameter parameter) {
         java.util.List<Parameter> params = ((Functional)parameter.getDeclaration().getRefinedDeclaration()).getParameterLists().get(0).getParameters();
         for (Parameter p : params) {
             if (p.getName().equals(parameter.getName())) {
                 return p;
             }
         }
         throw new RuntimeException("Parameter not found in refined declaration!"); // Should never happen
     }
 
     JCExpression ceylonLiteral(String s) {
         JCLiteral lit = make().Literal(s);
         return lit;
     }
 
     public JCExpression transform(Tree.StringLiteral string) {
         String value = string
                 .getText()
                 .substring(1, string.getText().length() - 1)
                 .replace("\r\n", "\n")
                 .replace("\r", "\n");
         at(string);
         return ceylonLiteral(value);
     }
 
     public JCExpression transform(Tree.CharLiteral lit) {
         JCExpression expr = make().Literal(TypeTags.CHAR, (int) lit.getText().charAt(1));
         // XXX make().Literal(lit.value) doesn't work here... something
         // broken in javac?
         return expr;
     }
 
     public JCExpression transform(Tree.FloatLiteral lit) {
         JCExpression expr = make().Literal(Double.parseDouble(lit.getText()));
         return expr;
     }
 
     public JCExpression transform(Tree.NaturalLiteral lit) {
         JCExpression expr = make().Literal(Long.parseLong(lit.getText()));
         return expr;
     }
     
     public JCExpression transform(final Tree.QualifiedMemberExpression access) {
         final String memberName = Util.quoteMethodName(access.getIdentifier().getText());
         final Tree.Primary operand = access.getPrimary();
 
         at(access);
         CeylonVisitor v = new CeylonVisitor(gen());
         operand.visit(v);
         JCExpression expr = v.getSingleResult();
         
         JCExpression result;
         if (willEraseToObject(operand.getTypeModel())) {
             // Erased types need a type cast
             JCExpression targetType = makeJavaType(access.getTarget().getDeclaringType());
             result = makeSelect(make().TypeCast(targetType, expr), memberName);
         } else if (sameType(syms().ceylonStringType, operand.getTypeModel())) {
             // Java Strings need to be boxed
             result = makeBox(syms().ceylonStringType, expr, memberName);
         } else if (sameType(syms().ceylonBooleanType, operand.getTypeModel())) {
             // Java native types need to be boxed
             result = makeBox(syms().ceylonBooleanType, expr, memberName);
         } else if (sameType(syms().ceylonIntegerType, operand.getTypeModel())) {
             // Java native types need to be boxed
             result = makeBox(syms().ceylonIntegerType, expr, memberName);
         } else {
             result = makeSelect(expr, memberName);
            if (Util.isErasedAttribute(access.getIdentifier().getText())) {
                result = make().Apply(null, result, List.<JCExpression>nil());
            }
         }
         return result;
     }
 
     private JCExpression makeBox(com.sun.tools.javac.code.Type type, JCExpression expr, String memberName) {
         JCExpression result = makeSelect(makeIdent(type), "instance");
         result = make().Parens(make().Apply(null, result, List.of(expr)));
         result = makeSelect(result, memberName);
         return result;
     }
     
     public JCExpression transform(Tree.BaseMemberExpression member) {
         JCExpression result = null;
         Declaration decl = member.getDeclaration();
         if (decl instanceof Getter) {
             // invoke the getter
             if (decl.isToplevel()) {
                 result = globalGen().getGlobalValue(
                         makeIdent(decl.getContainer().getQualifiedNameString()),
                         decl.getName(), decl.getName());
             } else if (decl.isClassMember()) {
                 result =  at(member).Apply(List.<JCExpression>nil(), 
                         makeIdent(Util.getGetterName(decl.getName())), 
                         List.<JCExpression>nil());
             } else {// method local attr
                 result = at(member).Apply(List.<JCExpression>nil(), 
                         makeIdent(decl.getName() + "$getter", Util.getGetterName(decl.getName())), 
                         List.<JCExpression>nil());
             }
         } else if (decl instanceof Value) {
             if (decl.isToplevel()) {
                 // ERASURE
                 if ("null".equals(decl.getName())) {
                     // FIXME this is a pretty brain-dead way to go about erase I think
                     result = at(member).Literal(TypeTags.BOT, null);
                 } else if (isBooleanTrue(decl)) {
                     result = makeBoolean(true);
                 } else if (isBooleanFalse(decl)) {
                     result = makeBoolean(false);
                 } else {
                     // it's a toplevel attribute
                     result = globalGen().getGlobalValue(
                             makeIdent(decl.getContainer().getQualifiedNameString()),
                             decl.getName());
                 }
             } else if(Util.isClassAttribute(decl)) {
                 // invoke the getter
                 result = at(member).Apply(List.<JCExpression>nil(), 
                        makeIdent(Util.getGetterName(decl.getName())), 
                        List.<JCExpression>nil());
              } else if(decl.isCaptured()) {
                  // invoke the qualified getter
                  result = at(member).Apply(List.<JCExpression>nil(), 
                         makeIdent(decl.getName(), Util.getGetterName(decl.getName())), 
                         List.<JCExpression>nil());
             }
         } else if (decl instanceof Method) {
             if (Util.isInnerMethod(decl)
             		|| decl.isToplevel()) {
                 java.util.List<String> path = new LinkedList<String>();
                 path.add(decl.getName());
                 path.add(decl.getName());
                 result = makeIdent(path);
             } else {
                 result = at(member).Ident(names().fromString(Util.quoteMethodName(member.getIdentifier().getText())));
             }
         }
         if (result == null) {
             result = at(member).Ident(names().fromString(substitute(member.getIdentifier().getText())));
         }
         return result;
     }
 
     public JCExpression transform(Tree.Type type, List<JCExpression> args) {
         // A constructor
         return at(type).NewClass(null, null, makeJavaType(type.getTypeModel()), args, null);
     }
 
     public JCExpression transform(Tree.BaseTypeExpression typeExp, List<JCExpression> args) {
         // A constructor
         return at(typeExp).NewClass(null, null, makeIdent(typeExp.getIdentifier().getText()), args, null);
     }
 
     public JCExpression transform(SequenceEnumeration value) {
         at(value);
         if (value.getExpressionList() == null) {
             return makeEmpty();
         } else {
             java.util.List<Expression> list = value.getExpressionList().getExpressions();
             ProducedType seqElemType = value.getTypeModel().getTypeArgumentList().get(0);
             return makeSequence(list, seqElemType);
         }
     }
 
     public JCTree transform(This expr) {
         at(expr);
         return makeIdent("this");
     }
 
     public JCTree transform(Super expr) {
         at(expr);
         return makeIdent("super");
     }
 
     public JCTree transform(Outer expr) {
         at(expr);
         ProducedType outerClass = com.redhat.ceylon.compiler.typechecker.model.Util.getOuterClassOrInterface(expr.getScope());
         return makeIdent(outerClass.getDeclaration().getName(), "this");
     }
 }
