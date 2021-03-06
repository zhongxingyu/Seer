 /*
  * Copyright 2008 Google Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.google.gwt.dev.jjs.impl;
 
 import com.google.gwt.dev.jjs.ast.Context;
 import com.google.gwt.dev.jjs.ast.JBinaryOperation;
 import com.google.gwt.dev.jjs.ast.JCastOperation;
 import com.google.gwt.dev.jjs.ast.JConditional;
 import com.google.gwt.dev.jjs.ast.JExpression;
 import com.google.gwt.dev.jjs.ast.JDeclarationStatement;
 import com.google.gwt.dev.jjs.ast.JMethod;
 import com.google.gwt.dev.jjs.ast.JMethodCall;
 import com.google.gwt.dev.jjs.ast.JModVisitor;
 import com.google.gwt.dev.jjs.ast.JNewArray;
 import com.google.gwt.dev.jjs.ast.JParameter;
 import com.google.gwt.dev.jjs.ast.JPrimitiveType;
 import com.google.gwt.dev.jjs.ast.JProgram;
 import com.google.gwt.dev.jjs.ast.JReturnStatement;
 import com.google.gwt.dev.jjs.ast.JType;
 import com.google.gwt.dev.jjs.ast.JValueLiteral;
 
 import java.util.List;
 
 /**
  * Synthesize explicit casts to and from the primitive long type where such a
  * cast would have been implicit. The explicit casts serve as markers for
  * {@link CastNormalizer}.
  */
 public class LongCastNormalizer {
 
   /**
    * Synthesize casts to longs and from long to trigger conversions.
    */
   private class ImplicitCastVisitor extends JModVisitor {
 
     private JMethod currentMethod;
     private final JPrimitiveType longType;
 
     public ImplicitCastVisitor(JPrimitiveType longType) {
       this.longType = longType;
     }
 
     @Override
     public void endVisit(JBinaryOperation x, Context ctx) {
       JType lhsType = x.getLhs().getType();
       JType rhsType = x.getRhs().getType();
       JType resultType = x.getType();
 
       if (resultType == program.getTypeJavaLangString()) {
         // Don't mess with concat.
         return;
       }
 
       // Special case: shift operators always coerce a long RHS to int.
       if (x.getOp().isShiftOperator()) {
         if (rhsType == longType) {
           rhsType = program.getTypePrimitiveInt();
         }
       } else if (lhsType == longType || rhsType == longType) {
         // We must coerce lhs and rhs to the same type, either long or a float.
 
         // Assume a long type.
         JType coerceTo = longType;
 
         // But double / float takes precedence over long.
         JPrimitiveType floatType = program.getTypePrimitiveFloat();
         JPrimitiveType doubleType = program.getTypePrimitiveDouble();
         // See if the lhs can coerce the rhs
         if ((lhsType == floatType || lhsType == doubleType)) {
           coerceTo = lhsType;
         }
         if (x.getOp().isAssignment()) {
           // In an assignment, the lhs must coerce the rhs
           coerceTo = lhsType;
         } else if ((rhsType == floatType || rhsType == doubleType)) {
           coerceTo = rhsType;
         }
         lhsType = rhsType = coerceTo;
       }
 
       JExpression newLhs = checkAndReplace(x.getLhs(), lhsType);
       JExpression newRhs = checkAndReplace(x.getRhs(), rhsType);
       if (newLhs != x.getLhs() || newRhs != x.getRhs()) {
         JBinaryOperation binOp = new JBinaryOperation(program,
             x.getSourceInfo(), resultType, x.getOp(), newLhs, newRhs);
         ctx.replaceMe(binOp);
       }
     }
 
     @Override
     public void endVisit(JConditional x, Context ctx) {
       JExpression newThen = checkAndReplace(x.getThenExpr(), x.getType());
       JExpression newElse = checkAndReplace(x.getElseExpr(), x.getType());
       if (newThen != x.getThenExpr() || newElse != x.getElseExpr()) {
         JConditional newCond = new JConditional(program, x.getSourceInfo(),
             x.getType(), x.getIfTest(), newThen, newElse);
         ctx.replaceMe(newCond);
       }
     }
 
     @Override
     public void endVisit(JDeclarationStatement x, Context ctx) {
       JExpression init = x.getInitializer();
       if (init != null) {
         init = checkAndReplace(init, x.getVariableRef().getType());
         if (init != x.getInitializer()) {
           JDeclarationStatement newStmt = new JDeclarationStatement(program,
               x.getSourceInfo(), x.getVariableRef(), init);
           ctx.replaceMe(newStmt);
         }
       }
     }
 
     @Override
     public void endVisit(JMethod x, Context ctx) {
       currentMethod = null;
     }
 
     @Override
     public void endVisit(JMethodCall x, Context ctx) {
       List<JParameter> params = x.getTarget().params;
       List<JExpression> args = x.getArgs();
       for (int i = 0; i < params.size(); ++i) {
         JParameter param = params.get(i);
         JExpression arg = args.get(i);
         JExpression newArg = checkAndReplace(arg, param.getType());
         if (arg != newArg) {
           args.set(i, newArg);
           this.didChange = true;
         }
       }
     }
 
     @Override
     public void endVisit(JNewArray x, Context ctx) {
       JType elementType = x.getArrayType().getElementType();
       List<JExpression> initializers = x.initializers;
       if (initializers != null) {
         for (int i = 0; i < initializers.size(); ++i) {
           JExpression initializer = initializers.get(i);
           JExpression newInitializer = checkAndReplace(initializer, elementType);
           if (initializer != newInitializer) {
             initializers.set(i, newInitializer);
             this.didChange = true;
           }
         }
       }
     }
 
     @Override
     public void endVisit(JReturnStatement x, Context ctx) {
       JExpression expr = x.getExpr();
       if (expr != null) {
         JExpression newExpr = checkAndReplace(expr, currentMethod.getType());
         if (expr != newExpr) {
           JReturnStatement newStmt = new JReturnStatement(program,
               x.getSourceInfo(), newExpr);
           ctx.replaceMe(newStmt);
         }
       }
     }
 
     @Override
     public boolean visit(JMethod x, Context ctx) {
       currentMethod = x;
       return true;
     }
 
     /**
      * Returns an explicit cast if the target type is long and the input
      * expression is not a long, or if the target type is floating point and the
      * expression is a long.
      */
     private JExpression checkAndReplace(JExpression arg, JType targetType) {
       JType argType = arg.getType();
       if (targetType == argType) {
         return arg;
       }
       if (targetType != longType && argType != longType) {
         return arg;
       }
      if (arg instanceof JValueLiteral && targetType instanceof JPrimitiveType) {
         // Attempt to coerce the literal.
        JPrimitiveType primitiveType = (JPrimitiveType) targetType;
        JValueLiteral coerced = primitiveType.coerceLiteral((JValueLiteral) arg);
         if (coerced != null) {
           return coerced;
         }
       }
       // Synthesize a cast to long to force explicit conversion.
       JCastOperation cast = new JCastOperation(program, arg.getSourceInfo(),
           targetType, arg);
       return cast;
     }
   }
 
   public static void exec(JProgram program) {
     new LongCastNormalizer(program).execImpl();
   }
 
   private final JProgram program;
 
   private LongCastNormalizer(JProgram program) {
     this.program = program;
   }
 
   private void execImpl() {
     ImplicitCastVisitor visitor = new ImplicitCastVisitor(
         program.getTypePrimitiveLong());
     visitor.accept(program);
   }
 
 }
