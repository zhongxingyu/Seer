 /*
  * TypeEnforcer.java
  * Copyright (C) 2012 Pre-Alpha Software
  * All rights reserved.
  */
 
 package com.prealpha.diamond.compiler;
 
 import com.google.common.base.Functions;
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.prealpha.diamond.compiler.node.AAddExpression;
 import com.prealpha.diamond.compiler.node.AArrayAccess;
 import com.prealpha.diamond.compiler.node.AArrayAccessAssignmentTarget;
 import com.prealpha.diamond.compiler.node.AArrayAccessPrimaryExpression;
 import com.prealpha.diamond.compiler.node.AAssignment;
 import com.prealpha.diamond.compiler.node.AAssignmentExpression;
 import com.prealpha.diamond.compiler.node.ABinaryIntegralLiteral;
 import com.prealpha.diamond.compiler.node.ABitwiseAndExpression;
 import com.prealpha.diamond.compiler.node.ABitwiseComplementExpression;
 import com.prealpha.diamond.compiler.node.ABitwiseOrExpression;
 import com.prealpha.diamond.compiler.node.ABitwiseXorExpression;
 import com.prealpha.diamond.compiler.node.ACaseGroup;
 import com.prealpha.diamond.compiler.node.ACastDeclaration;
 import com.prealpha.diamond.compiler.node.ACastInvocation;
 import com.prealpha.diamond.compiler.node.ACastInvocationPrimaryExpression;
 import com.prealpha.diamond.compiler.node.AClassDeclaration;
 import com.prealpha.diamond.compiler.node.AConditionalAndExpression;
 import com.prealpha.diamond.compiler.node.AConditionalExpression;
 import com.prealpha.diamond.compiler.node.AConditionalNotExpression;
 import com.prealpha.diamond.compiler.node.AConditionalOrExpression;
 import com.prealpha.diamond.compiler.node.AConstructorDeclaration;
 import com.prealpha.diamond.compiler.node.AConstructorInvocation;
 import com.prealpha.diamond.compiler.node.AConstructorInvocationPrimaryExpression;
 import com.prealpha.diamond.compiler.node.ADecimalIntegralLiteral;
 import com.prealpha.diamond.compiler.node.ADefaultCaseGroup;
 import com.prealpha.diamond.compiler.node.ADeleteStatement;
 import com.prealpha.diamond.compiler.node.ADivideExpression;
 import com.prealpha.diamond.compiler.node.ADoStatement;
 import com.prealpha.diamond.compiler.node.AEqualExpression;
 import com.prealpha.diamond.compiler.node.AExpressionFieldAccess;
 import com.prealpha.diamond.compiler.node.AExpressionFunctionInvocation;
 import com.prealpha.diamond.compiler.node.AExpressionStatement;
 import com.prealpha.diamond.compiler.node.AFalseLiteral;
 import com.prealpha.diamond.compiler.node.AFieldAccessAssignmentTarget;
 import com.prealpha.diamond.compiler.node.AFieldAccessPrimaryExpression;
 import com.prealpha.diamond.compiler.node.AForStatement;
 import com.prealpha.diamond.compiler.node.AFunctionDeclaration;
 import com.prealpha.diamond.compiler.node.AFunctionInvocationPrimaryExpression;
 import com.prealpha.diamond.compiler.node.AGreaterOrEqualExpression;
 import com.prealpha.diamond.compiler.node.AGreaterThanExpression;
 import com.prealpha.diamond.compiler.node.AHexIntegralLiteral;
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
 import com.prealpha.diamond.compiler.node.ANativeStatement;
 import com.prealpha.diamond.compiler.node.ANotEqualExpression;
 import com.prealpha.diamond.compiler.node.ANumericNegationExpression;
 import com.prealpha.diamond.compiler.node.AOctalIntegralLiteral;
 import com.prealpha.diamond.compiler.node.AParentheticalPrimaryExpression;
 import com.prealpha.diamond.compiler.node.APrimaryExpression;
 import com.prealpha.diamond.compiler.node.AReturnStatement;
 import com.prealpha.diamond.compiler.node.AShiftLeftExpression;
 import com.prealpha.diamond.compiler.node.AShiftRightExpression;
 import com.prealpha.diamond.compiler.node.AStringLiteral;
 import com.prealpha.diamond.compiler.node.ASubtractExpression;
 import com.prealpha.diamond.compiler.node.ASwitchStatement;
 import com.prealpha.diamond.compiler.node.AThisPrimaryExpression;
 import com.prealpha.diamond.compiler.node.ATrueLiteral;
 import com.prealpha.diamond.compiler.node.ATypeTokenFieldAccess;
 import com.prealpha.diamond.compiler.node.ATypeTokenFunctionInvocation;
 import com.prealpha.diamond.compiler.node.AUnqualifiedFunctionInvocation;
 import com.prealpha.diamond.compiler.node.AUnsignedShiftRightExpression;
 import com.prealpha.diamond.compiler.node.AVoidFunctionDeclaration;
 import com.prealpha.diamond.compiler.node.AWhileStatement;
 import com.prealpha.diamond.compiler.node.Node;
 import com.prealpha.diamond.compiler.node.PCaseGroup;
 import com.prealpha.diamond.compiler.node.PExpression;
 import com.prealpha.diamond.compiler.node.PFunctionInvocation;
 import com.prealpha.diamond.compiler.node.PIntegralLiteral;
 import com.prealpha.diamond.compiler.node.TIdentifier;
 
 import java.math.BigInteger;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import static com.google.common.base.Preconditions.*;
 
 final class TypeEnforcer extends ScopeAwareWalker {
     private final Compiler compiler;
 
     private final Map<Node, TypeToken> types;
 
     private AClassDeclaration currentClass;
 
     private ParametrizedSymbol currentFunction;
 
     public TypeEnforcer(Compiler compiler) {
         super(compiler.getScopeSource());
         checkNotNull(compiler);
         this.compiler = compiler;
         this.types = Maps.filterEntries(Maps.<Node, TypeToken>newHashMap(), new Predicate<Map.Entry<Node, TypeToken>>() {
             @Override
             public boolean apply(Map.Entry<Node, TypeToken> input) {
                 return (input.getKey() != null && input.getValue() != null);
             }
         });
     }
 
     public Map<Node, TypeToken> getTypes() {
         return ImmutableMap.copyOf(types);
     }
 
     private void assertAssignableTo(Node node, TypeToken type) {
         if (!types.containsKey(node)) {
             throw new AssertionError("cannot type-check node which was not previously encountered");
         } else if (!types.get(node).isAssignableTo(type)) {
             String message = String.format("expected node with type %s; found %s", type, types.get(node));
             compiler.raise(new SemanticException(node, message));
         }
     }
 
     private void assertIntegral(Node node) {
         if (!types.containsKey(node)) {
             throw new AssertionError("cannot type-check node which was not previously encountered");
         } else if (!types.get(node).isIntegral()) {
             String message = String.format("expected node with type <integral>; found %s", types.get(node));
             compiler.raise(new SemanticException(node, message));
         }
     }
 
     private TypeToken assertIntegralEqual(Node left, Node right) {
         assertIntegral(left);
         assertIntegral(right);
         return assertEqual(left, right);
     }
 
     private void assertPrimitive(Node node) {
         if (!types.containsKey(node)) {
             throw new AssertionError("cannot type-check node which was not previously encountered");
         } else if (types.get(node).isReference()) {
             String message = String.format("expected node with type <primitive>; found %s", types.get(node));
             compiler.raise(new SemanticException(node, message));
         }
     }
 
     private TypeToken assertPrimitiveEqual(Node left, Node right) {
         assertPrimitive(left);
         assertPrimitive(right);
         return assertEqual(left, right);
     }
 
     private void assertReference(Node node) {
         if (!types.containsKey(node)) {
             throw new AssertionError("cannot type-check node which was not previously encountered");
         } else if (!types.get(node).isReference()) {
             String message = String.format("expected node with type <reference>; found %s", types.get(node));
             compiler.raise(new SemanticException(node, message));
         }
     }
 
     private TypeToken assertEqual(Node left, Node right) {
         if (!types.get(left).equals(types.get(right))) {
             String message = String.format("expected two equal types; found %s and %s", types.get(left), types.get(right));
             compiler.raise(new SemanticException(left.parent(), message));
         }
         return types.get(left);
     }
 
     @Override
     public void outAIfThenStatement(AIfThenStatement ifThenStatement) {
         assertAssignableTo(ifThenStatement.getCondition(), PrimitiveTypeToken.BOOLEAN);
     }
 
     @Override
     public void outAIfThenElseStatement(AIfThenElseStatement ifThenElseStatement) {
         assertAssignableTo(ifThenElseStatement.getCondition(), PrimitiveTypeToken.BOOLEAN);
     }
 
     @Override
     public void outAWhileStatement(AWhileStatement whileStatement) {
         assertAssignableTo(whileStatement.getCondition(), PrimitiveTypeToken.BOOLEAN);
     }
 
     @Override
     public void outAForStatement(AForStatement forStatement) {
         assertAssignableTo(forStatement.getCondition(), PrimitiveTypeToken.BOOLEAN);
         super.outAForStatement(forStatement);
     }
 
     @Override
     public void outADoStatement(ADoStatement doStatement) {
         assertAssignableTo(doStatement.getCondition(), PrimitiveTypeToken.BOOLEAN);
     }
 
     @Override
     public void outASwitchStatement(ASwitchStatement switchStatement) {
         assertIntegral(switchStatement.getValue());
 
         // check that there is no more than one case group with a default label
         boolean seenDefault = false;
         for (PCaseGroup caseGroup : switchStatement.getBody()) {
             if (caseGroup instanceof ADefaultCaseGroup) {
                 if (seenDefault) {
                     compiler.raise(new SemanticException(caseGroup, "only one case group may contain a default"));
                 } else {
                     seenDefault = true;
                 }
             }
         }
 
         // check that there are no duplicates among the case labels
         // also check that all literals are assignable to the type of the value expression
         Set<BigInteger> alreadySeen = Sets.newHashSet();
         for (PCaseGroup caseGroup : switchStatement.getBody()) {
             for (PIntegralLiteral literal : getCaseGroupValues(caseGroup)) {
                 assertAssignableTo(literal, types.get(switchStatement.getValue()));
                 try {
                     BigInteger value = TypeTokenUtil.parseIntegralLiteral(literal);
                     if (alreadySeen.contains(value)) {
                         throw new SemanticException(caseGroup, "duplicate case label for " + value);
                     } else {
                         alreadySeen.add(value);
                     }
                 } catch (SemanticException sx) {
                     compiler.raise(sx);
                 }
             }
         }
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
     public void outADeleteStatement(ADeleteStatement deleteStatement) {
         assertReference(deleteStatement.getObject());
     }
 
     @Override
     public void outAReturnStatement(AReturnStatement returnStatement) {
         PExpression returnValue = returnStatement.getReturnValue();
         if (currentFunction != null) {
             if (currentFunction.getReturnType() != null) {
                 assertAssignableTo(returnValue, currentFunction.getReturnType());
             } else if (returnValue != null) {
                 compiler.raise(new SemanticException(returnValue, "expected expression of type void; found " + types.get(returnValue)));
             } else {
                 compiler.raise(new SemanticException(currentFunction.getDeclaration(), "unknown function declaration flavor"));
             }
         } else {
             compiler.raise(new SemanticException(returnStatement, "unexpected return statement"));
         }
     }
 
     @Override
     public void outANativeStatement(ANativeStatement nativeStatement) {
         if (currentFunction == null || !currentFunction.getModifiers().contains(Modifier.NATIVE)) {
             compiler.raise(new SemanticException(nativeStatement, "native statements are only permitted within native functions"));
         }
     }
 
     @Override
     public void inAClassDeclaration(AClassDeclaration classDeclaration) {
         if (currentClass != null) {
             compiler.raise(new SemanticException(classDeclaration, "unexpected class declaration"));
         } else {
             currentClass = classDeclaration;
         }
         super.inAClassDeclaration(classDeclaration);
     }
 
     @Override
     public void outAClassDeclaration(AClassDeclaration classDeclaration) {
         currentClass = null;
         super.outAClassDeclaration(classDeclaration);
     }
 
     @Override
     public void inAFunctionDeclaration(AFunctionDeclaration functionDeclaration) {
         try {
             if (currentFunction != null) {
                 throw new SemanticException(functionDeclaration, "unexpected function declaration");
             } else {
                 List<TypeToken> parameterTypes = Lists.transform(functionDeclaration.getParameters(), TypeTokenUtil.getDeclarationFunction());
                 currentFunction = getScope().resolveFunction(functionDeclaration.getName().getText(), parameterTypes);
             }
         } catch (SemanticException sx) {
             compiler.raise(sx);
         }
         super.inAFunctionDeclaration(functionDeclaration);
     }
 
     @Override
     public void outAFunctionDeclaration(AFunctionDeclaration functionDeclaration) {
         currentFunction = null;
         super.outAFunctionDeclaration(functionDeclaration);
     }
 
     @Override
     public void inAVoidFunctionDeclaration(AVoidFunctionDeclaration functionDeclaration) {
         try {
             if (currentFunction != null) {
                 throw new SemanticException(functionDeclaration, "unexpected function declaration");
             } else {
                 List<TypeToken> parameterTypes = Lists.transform(functionDeclaration.getParameters(), TypeTokenUtil.getDeclarationFunction());
                 currentFunction = getScope().resolveFunction(functionDeclaration.getName().getText(), parameterTypes);
             }
         } catch (SemanticException sx) {
             compiler.raise(sx);
         }
         super.inAVoidFunctionDeclaration(functionDeclaration);
     }
 
     @Override
     public void outAVoidFunctionDeclaration(AVoidFunctionDeclaration functionDeclaration) {
         currentFunction = null;
         super.outAVoidFunctionDeclaration(functionDeclaration);
     }
 
     @Override
     public void inAConstructorDeclaration(AConstructorDeclaration constructorDeclaration) {
         try {
             if (currentFunction != null) {
                 throw new SemanticException(constructorDeclaration, "unexpected constructor declaration");
             } else {
                 List<TypeToken> parameterTypes = Lists.transform(constructorDeclaration.getParameters(), TypeTokenUtil.getDeclarationFunction());
                 currentFunction = getScope().resolveConstructor(parameterTypes);
             }
         } catch (SemanticException sx) {
             compiler.raise(sx);
         }
         super.inAConstructorDeclaration(constructorDeclaration);
     }
 
     @Override
     public void outAConstructorDeclaration(AConstructorDeclaration constructorDeclaration) {
         try {
             ConstructorSymbol symbol = getScope().resolveConstructor(Lists.transform(constructorDeclaration.getParameters(), Functions.forMap(types)));
             if (!symbol.getReturnType().isAssignableTo(new UserDefinedTypeToken(symbol.getDeclaringClass().getName()))) {
                 throw new SemanticException("constructor return type must be assignable to its enclosing type");
             }
         } catch (SemanticException sx) {
             compiler.raise(sx);
         }
 
         currentFunction = null;
         super.outAConstructorDeclaration(constructorDeclaration);
     }
 
     @Override
     public void inACastDeclaration(ACastDeclaration castDeclaration) {
         try {
             if (currentFunction != null) {
                 throw new SemanticException(castDeclaration, "unexpected cast declaration");
             } else {
                 TypeToken valueType = TypeTokenUtil.fromNode(((ALocalDeclaration) castDeclaration.getParameter()).getType());
                 currentFunction = getScope().resolveCast(valueType);
             }
         } catch (SemanticException sx) {
             compiler.raise(sx);
         }
         super.inACastDeclaration(castDeclaration);
     }
 
     @Override
     public void outACastDeclaration(ACastDeclaration castDeclaration) {
         try {
             CastSymbol symbol = getScope().resolveCast(types.get(castDeclaration.getParameter()));
             if (!symbol.getReturnType().isAssignableTo(new UserDefinedTypeToken(symbol.getDeclaringClass().getName()))) {
                 throw new SemanticException("cast return type must be assignable to its enclosing type");
             }
         } catch (SemanticException sx) {
             compiler.raise(sx);
         }
 
         currentFunction = null;
         super.outACastDeclaration(castDeclaration);
     }
 
     @Override
     public void outALiteralPrimaryExpression(ALiteralPrimaryExpression primaryExpression) {
         types.put(primaryExpression, types.get(primaryExpression.getLiteral()));
     }
 
     @Override
     public void outAIdentifierPrimaryExpression(AIdentifierPrimaryExpression primaryExpression) {
         enforceIdentifier(primaryExpression.getIdentifier());
         types.put(primaryExpression, types.get(primaryExpression.getIdentifier()));
     }
 
     @Override
     public void outAThisPrimaryExpression(AThisPrimaryExpression primaryExpression) {
         TypeToken typeToken = new UserDefinedTypeToken(currentClass.getName().getText());
         types.put(primaryExpression, typeToken);
     }
 
     @Override
     public void outAParentheticalPrimaryExpression(AParentheticalPrimaryExpression primaryExpression) {
         types.put(primaryExpression, types.get(primaryExpression.getExpression()));
     }
 
     @Override
     public void outAFunctionInvocationPrimaryExpression(AFunctionInvocationPrimaryExpression primaryExpression) {
         if (types.containsKey(primaryExpression.getFunctionInvocation())) {
             types.put(primaryExpression, types.get(primaryExpression.getFunctionInvocation()));
         } else {
             assert primaryExpression.parent() instanceof APrimaryExpression;
             assert primaryExpression.parent().parent() instanceof AExpressionStatement;
         }
     }
 
     @Override
     public void outAConstructorInvocationPrimaryExpression(AConstructorInvocationPrimaryExpression primaryExpression) {
         types.put(primaryExpression, types.get(primaryExpression.getConstructorInvocation()));
     }
 
     @Override
     public void outACastInvocationPrimaryExpression(ACastInvocationPrimaryExpression primaryExpression) {
         types.put(primaryExpression, types.get(primaryExpression.getCastInvocation()));
     }
 
     @Override
     public void outAFieldAccessPrimaryExpression(AFieldAccessPrimaryExpression primaryExpression) {
         types.put(primaryExpression, types.get(primaryExpression.getFieldAccess()));
     }
 
     @Override
     public void outAArrayAccessPrimaryExpression(AArrayAccessPrimaryExpression primaryExpression) {
         types.put(primaryExpression, types.get(primaryExpression.getArrayAccess()));
     }
 
     @Override
     public void outAIntegralLiteral(AIntegralLiteral literal) {
         types.put(literal, types.get(literal.getIntegralLiteral()));
     }
 
     @Override
     public void outAStringLiteral(AStringLiteral literal) {
         types.put(literal, new UserDefinedTypeToken("String"));
     }
 
     @Override
     public void outATrueLiteral(ATrueLiteral literal) {
         types.put(literal, PrimitiveTypeToken.BOOLEAN);
     }
 
     @Override
     public void outAFalseLiteral(AFalseLiteral literal) {
         types.put(literal, PrimitiveTypeToken.BOOLEAN);
     }
 
     @Override
     public void outADecimalIntegralLiteral(ADecimalIntegralLiteral literal) {
         types.put(literal, TypeTokenUtil.fromIntegralLiteral(literal));
     }
 
     @Override
     public void outAHexIntegralLiteral(AHexIntegralLiteral literal) {
         types.put(literal, TypeTokenUtil.fromIntegralLiteral(literal));
     }
 
     @Override
     public void outAOctalIntegralLiteral(AOctalIntegralLiteral literal) {
         types.put(literal, TypeTokenUtil.fromIntegralLiteral(literal));
     }
 
     @Override
     public void outABinaryIntegralLiteral(ABinaryIntegralLiteral literal) {
         types.put(literal, TypeTokenUtil.fromIntegralLiteral(literal));
     }
 
     @Override
     public void outAUnqualifiedFunctionInvocation(AUnqualifiedFunctionInvocation invocation) {
         try {
             List<TypeToken> parameterTypes = Lists.transform(invocation.getParameters(), Functions.forMap(types));
             FunctionSymbol symbol = getScope().resolveFunction(invocation.getFunctionName().getText(), parameterTypes);
             enforceFunctionInvocation(invocation, symbol);
         } catch (SemanticException sx) {
             compiler.raise(sx);
         }
     }
 
     @Override
     public void outAExpressionFunctionInvocation(AExpressionFunctionInvocation invocation) {
         try {
             TypeToken enclosingType = types.get(invocation.getTarget());
             if (enclosingType instanceof UserDefinedTypeToken) {
                 String enclosingClassName = ((UserDefinedTypeToken) enclosingType).getTypeName();
                 Scope scope = getScope(getScope().resolveClass(enclosingClassName).getDeclaration());
                 List<TypeToken> parameterTypes = Lists.transform(invocation.getParameters(), Functions.forMap(types));
                 FunctionSymbol symbol = scope.resolveFunction(invocation.getFunctionName().getText(), parameterTypes);
                 if (symbol.getModifiers().contains(Modifier.STATIC)) {
                     throw new SemanticException("cannot invoke a static method on an instance");
                 }
                 enforceFunctionInvocation(invocation, symbol);
             } else {
                 throw new SemanticException(invocation, "built-in types do not currently support any functions");
             }
         } catch (SemanticException sx) {
             compiler.raise(sx);
         }
     }
 
     @Override
     public void outATypeTokenFunctionInvocation(ATypeTokenFunctionInvocation invocation) {
         try {
             Scope scope;
             if (invocation.getTarget() != null) {
                 TypeToken enclosingType = types.get(invocation.getTarget());
                 if (enclosingType instanceof UserDefinedTypeToken) {
                     String enclosingClassName = ((UserDefinedTypeToken) enclosingType).getTypeName();
                     scope = getScope(getScope().resolveClass(enclosingClassName).getDeclaration());
                 } else {
                     throw new SemanticException(invocation, "built-in types do not currently support any functions");
                 }
             } else {
                 scope = getScope();
             }
             List<TypeToken> parameterTypes = Lists.transform(invocation.getParameters(), Functions.forMap(types));
             FunctionSymbol symbol = scope.resolveFunction(invocation.getFunctionName().getText(), parameterTypes);
             if (!symbol.getModifiers().contains(Modifier.STATIC)) {
                 throw new SemanticException(invocation, "cannot invoke an instance method statically");
             }
             enforceFunctionInvocation(invocation, symbol);
         } catch (SemanticException sx) {
             compiler.raise(sx);
         }
     }
 
     private void enforceFunctionInvocation(PFunctionInvocation invocation, FunctionSymbol symbol) {
         if (symbol.getReturnType() != null) {
             types.put(invocation, symbol.getReturnType());
         } else {
             // we can't actually put it in the map
             // however, we should throw an exception if this is used other than as a standalone statement
             if (invocation.parent() instanceof AFunctionInvocationPrimaryExpression) {
                 if (invocation.parent().parent() instanceof APrimaryExpression) {
                     if (invocation.parent().parent().parent() instanceof AExpressionStatement) {
                         return;
                     }
                 }
             }
             compiler.raise(new SemanticException(invocation, "cannot invoke a void function except as a standalone statement"));
         }
     }
 
     @Override
     public void outAConstructorInvocation(AConstructorInvocation invocation) {
         try {
             List<TypeToken> parameterTypes = Lists.transform(invocation.getParameters(), Functions.forMap(types));
             Scope scope;
             if (invocation.getTarget() != null) {
                 TypeToken scopeToken = TypeTokenUtil.fromNode(invocation.getTarget());
                 if (scopeToken instanceof UserDefinedTypeToken) {
                     ClassSymbol classSymbol = getScope().resolveClass(((UserDefinedTypeToken) scopeToken).getTypeName());
                     scope = getScope(classSymbol.getDeclaration());
                 } else if (scopeToken instanceof ArrayTypeToken) {
                     if (!parameterTypes.equals(ImmutableList.of(PrimitiveTypeToken.UINT))) {
                         String message = String.format("cannot resolve constructor symbol \"new%s\"", parameterTypes);
                         throw new SemanticException(invocation, message);
                     } else {
                         types.put(invocation, scopeToken);
                         return;
                     }
                 } else {
                     throw new SemanticException(invocation, "built-in types do not currently support any constructors");
                 }
             } else {
                 scope = getScope();
             }
             ConstructorSymbol symbol = scope.resolveConstructor(parameterTypes);
             types.put(invocation, symbol.getReturnType());
         } catch (SemanticException sx) {
             compiler.raise(sx);
         }
     }
 
     @Override
     public void outACastInvocation(ACastInvocation invocation) {
         try {
             Scope scope;
             if (invocation.getTarget() != null) {
                 TypeToken scopeToken = TypeTokenUtil.fromNode(invocation.getTarget());
                 if (scopeToken instanceof UserDefinedTypeToken) {
                     ClassSymbol classSymbol = getScope().resolveClass(((UserDefinedTypeToken) scopeToken).getTypeName());
                     scope = getScope(classSymbol.getDeclaration());
                 } else {
                     types.put(invocation, scopeToken);
                     return;
                 }
             } else {
                 scope = getScope();
             }
             try {
                 CastSymbol symbol = scope.resolveCast(types.get(invocation.getValue()));
                 types.put(invocation, symbol.getReturnType());
             } catch (SemanticException sx) {
                 types.put(invocation, new UserDefinedTypeToken(currentClass.getName().getText()));
             }
         } catch (SemanticException sx) {
             compiler.raise(sx);
         }
     }
 
     @Override
     public void outAExpressionFieldAccess(AExpressionFieldAccess fieldAccess) {
         try {
             TypeToken enclosingType = types.get(fieldAccess.getTarget());
             if (enclosingType instanceof UserDefinedTypeToken) {
                 String enclosingClassName = ((UserDefinedTypeToken) enclosingType).getTypeName();
                 Scope scope = getScope(getScope().resolveClass(enclosingClassName).getDeclaration());
                 FieldSymbol symbol = scope.resolveField(fieldAccess.getFieldName().getText());
                 if (symbol.getModifiers().contains(Modifier.STATIC)) {
                     throw new SemanticException("cannot access a static field using an instance");
                 }
                 types.put(fieldAccess, symbol.getType());
             } else {
                 throw new SemanticException(fieldAccess, "built-in types do not currently support any fields");
             }
         } catch (SemanticException sx) {
             compiler.raise(sx);
         }
     }
 
     @Override
     public void outATypeTokenFieldAccess(ATypeTokenFieldAccess fieldAccess) {
         try {
             TypeToken enclosingType = TypeTokenUtil.fromNode(fieldAccess.getTarget());
             if (enclosingType instanceof UserDefinedTypeToken) {
                 String enclosingClassName = ((UserDefinedTypeToken) enclosingType).getTypeName();
                 Scope scope = getScope(getScope().resolveClass(enclosingClassName).getDeclaration());
                 FieldSymbol symbol = scope.resolveField(fieldAccess.getFieldName().getText());
                 if (!symbol.getModifiers().contains(Modifier.STATIC)) {
                     throw new SemanticException("cannot access an instance field statically");
                 }
                 types.put(fieldAccess, symbol.getType());
             } else {
                 throw new SemanticException(fieldAccess, "built-in types do not currently support any fields");
             }
         } catch (SemanticException sx) {
             compiler.raise(sx);
         }
     }
 
     @Override
     public void outAArrayAccess(AArrayAccess arrayAccess) {
         assertAssignableTo(arrayAccess.getIndex(), PrimitiveTypeToken.UINT);
         TypeToken arrayType = types.get(arrayAccess.getArray());
         if (arrayType instanceof ArrayTypeToken) {
             types.put(arrayAccess, ((ArrayTypeToken) arrayType).getElementType());
         } else {
             String message = String.format("expected array; found %s", arrayType);
             compiler.raise(new SemanticException(arrayAccess, message));
         }
     }
 
     @Override
     public void outAPrimaryExpression(APrimaryExpression primaryExpression) {
         if (types.containsKey(primaryExpression.getPrimaryExpression())) {
             types.put(primaryExpression, types.get(primaryExpression.getPrimaryExpression()));
         } else {
             assert primaryExpression.parent() instanceof AExpressionStatement;
         }
     }
 
     @Override
     public void outANumericNegationExpression(ANumericNegationExpression expression) {
         assertIntegral(expression.getValue());
         types.put(expression, types.get(expression.getValue()));
     }
 
     @Override
     public void outAConditionalNotExpression(AConditionalNotExpression expression) {
         assertAssignableTo(expression.getValue(), PrimitiveTypeToken.BOOLEAN);
         types.put(expression, types.get(expression.getValue()));
     }
 
     @Override
     public void outABitwiseComplementExpression(ABitwiseComplementExpression expression) {
         assertIntegral(expression.getValue());
         types.put(expression, types.get(expression.getValue()));
     }
 
     @Override
     public void outAMultiplyExpression(AMultiplyExpression expression) {
         types.put(expression, assertIntegralEqual(expression.getLeft(), expression.getRight()));
     }
 
     @Override
     public void outADivideExpression(ADivideExpression expression) {
         types.put(expression, assertIntegralEqual(expression.getLeft(), expression.getRight()));
     }
 
     @Override
     public void outAModulusExpression(AModulusExpression expression) {
         types.put(expression, assertIntegralEqual(expression.getLeft(), expression.getRight()));
     }
 
     @Override
     public void outAAddExpression(AAddExpression expression) {
         types.put(expression, assertIntegralEqual(expression.getLeft(), expression.getRight()));
     }
 
     @Override
     public void outASubtractExpression(ASubtractExpression expression) {
         types.put(expression, assertIntegralEqual(expression.getLeft(), expression.getRight()));
     }
 
     @Override
     public void outAShiftLeftExpression(AShiftLeftExpression expression) {
         assertIntegral(expression.getLeft());
         assertAssignableTo(expression.getRight(), PrimitiveTypeToken.UINT);
         types.put(expression, types.get(expression.getLeft()));
     }
 
     @Override
     public void outAShiftRightExpression(AShiftRightExpression expression) {
         assertIntegral(expression.getLeft());
         assertAssignableTo(expression.getRight(), PrimitiveTypeToken.UINT);
         types.put(expression, types.get(expression.getLeft()));
     }
 
     @Override
     public void outAUnsignedShiftRightExpression(AUnsignedShiftRightExpression expression) {
         assertIntegral(expression.getLeft());
         assertAssignableTo(expression.getRight(), PrimitiveTypeToken.UINT);
         types.put(expression, types.get(expression.getLeft()));
     }
 
     @Override
     public void outALessThanExpression(ALessThanExpression expression) {
         assertIntegralEqual(expression.getLeft(), expression.getRight());
         types.put(expression, PrimitiveTypeToken.BOOLEAN);
     }
 
     @Override
     public void outAGreaterThanExpression(AGreaterThanExpression expression) {
         assertIntegralEqual(expression.getLeft(), expression.getRight());
         types.put(expression, PrimitiveTypeToken.BOOLEAN);
     }
 
     @Override
     public void outALessOrEqualExpression(ALessOrEqualExpression expression) {
         assertIntegralEqual(expression.getLeft(), expression.getRight());
         types.put(expression, PrimitiveTypeToken.BOOLEAN);
     }
 
     @Override
     public void outAGreaterOrEqualExpression(AGreaterOrEqualExpression expression) {
         assertIntegralEqual(expression.getLeft(), expression.getRight());
         types.put(expression, PrimitiveTypeToken.BOOLEAN);
     }
 
     @Override
     public void outAEqualExpression(AEqualExpression expression) {
         assertPrimitiveEqual(expression.getLeft(), expression.getRight());
         types.put(expression, PrimitiveTypeToken.BOOLEAN);
     }
 
     @Override
     public void outANotEqualExpression(ANotEqualExpression expression) {
         assertPrimitiveEqual(expression.getLeft(), expression.getRight());
         types.put(expression, PrimitiveTypeToken.BOOLEAN);
     }
 
     @Override
     public void outABitwiseAndExpression(ABitwiseAndExpression expression) {
         types.put(expression, assertPrimitiveEqual(expression.getLeft(), expression.getRight()));
     }
 
     @Override
     public void outABitwiseXorExpression(ABitwiseXorExpression expression) {
         types.put(expression, assertPrimitiveEqual(expression.getLeft(), expression.getRight()));
     }
 
     @Override
     public void outABitwiseOrExpression(ABitwiseOrExpression expression) {
         types.put(expression, assertPrimitiveEqual(expression.getLeft(), expression.getRight()));
     }
 
     @Override
     public void outAConditionalAndExpression(AConditionalAndExpression expression) {
         assertAssignableTo(expression.getLeft(), PrimitiveTypeToken.BOOLEAN);
         assertAssignableTo(expression.getRight(), PrimitiveTypeToken.BOOLEAN);
         types.put(expression, PrimitiveTypeToken.BOOLEAN);
     }
 
     @Override
     public void outAConditionalOrExpression(AConditionalOrExpression expression) {
         assertAssignableTo(expression.getLeft(), PrimitiveTypeToken.BOOLEAN);
         assertAssignableTo(expression.getRight(), PrimitiveTypeToken.BOOLEAN);
         types.put(expression, PrimitiveTypeToken.BOOLEAN);
     }
 
     @Override
     public void outAConditionalExpression(AConditionalExpression expression) {
         assertAssignableTo(expression.getCondition(), PrimitiveTypeToken.BOOLEAN);
         types.put(expression, assertEqual(expression.getIfTrue(), expression.getIfFalse()));
     }
 
     @Override
     public void outAAssignmentExpression(AAssignmentExpression expression) {
         types.put(expression, types.get(expression.getAssignment()));
     }
 
     @Override
     public void outAAssignment(AAssignment assignment) {
         TypeToken targetType = types.get(assignment.getTarget());
         assertAssignableTo(assignment.getValue(), targetType);
         types.put(assignment, targetType);
     }
 
     @Override
     public void outALocalDeclarationAssignmentTarget(ALocalDeclarationAssignmentTarget assignmentTarget) {
        ALocalDeclaration localDeclaration = (ALocalDeclaration) assignmentTarget.getLocalDeclaration();
        types.put(assignmentTarget, TypeTokenUtil.fromNode(localDeclaration.getType()));
     }
 
     @Override
     public void outAIdentifierAssignmentTarget(AIdentifierAssignmentTarget assignmentTarget) {
         enforceIdentifier(assignmentTarget.getIdentifier());
         types.put(assignmentTarget, types.get(assignmentTarget.getIdentifier()));
     }
 
     @Override
     public void outAFieldAccessAssignmentTarget(AFieldAccessAssignmentTarget assignmentTarget) {
         types.put(assignmentTarget, types.get(assignmentTarget.getFieldAccess()));
     }
 
     @Override
     public void outAArrayAccessAssignmentTarget(AArrayAccessAssignmentTarget assignmentTarget) {
         types.put(assignmentTarget, types.get(assignmentTarget.getArrayAccess()));
     }
 
     private void enforceIdentifier(TIdentifier identifier) {
         try {
             try {
                 LocalSymbol localSymbol = getScope().resolveLocal(identifier.getText());
                 types.put(identifier, localSymbol.getType());
             } catch (SemanticException sx) {
                 FieldSymbol fieldSymbol = getScope().resolveField(identifier.getText());
                 types.put(identifier, fieldSymbol.getType());
             }
         } catch (SemanticException sx) {
             compiler.raise(sx);
         }
     }
 }
