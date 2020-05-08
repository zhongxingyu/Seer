 package no.shhsoft.basus.language.eval;
 
 import no.shhsoft.basus.language.AdditiveExpression;
 import no.shhsoft.basus.language.AssignableExpression;
 import no.shhsoft.basus.language.AssignmentStatement;
 import no.shhsoft.basus.language.BreakpointStatement;
 import no.shhsoft.basus.language.CallStatement;
 import no.shhsoft.basus.language.ConditionalAndExpression;
 import no.shhsoft.basus.language.ConditionalOrExpression;
 import no.shhsoft.basus.language.ConstantExpression;
 import no.shhsoft.basus.language.ExponentialExpression;
 import no.shhsoft.basus.language.Expression;
 import no.shhsoft.basus.language.ForStatement;
 import no.shhsoft.basus.language.FunctionExpression;
 import no.shhsoft.basus.language.FunctionStatement;
 import no.shhsoft.basus.language.IfStatement;
 import no.shhsoft.basus.language.IndexExpression;
 import no.shhsoft.basus.language.MultiplicativeExpression;
 import no.shhsoft.basus.language.OperatorType;
 import no.shhsoft.basus.language.RelationalExpression;
 import no.shhsoft.basus.language.RepeatStatement;
 import no.shhsoft.basus.language.Reserved;
 import no.shhsoft.basus.language.ReturnStatement;
 import no.shhsoft.basus.language.Statement;
 import no.shhsoft.basus.language.StatementList;
 import no.shhsoft.basus.language.UnaryExpression;
 import no.shhsoft.basus.language.VariableExpression;
 import no.shhsoft.basus.language.WhileStatement;
 import no.shhsoft.basus.utils.ErrorUtils;
 import no.shhsoft.basus.utils.TextLocation;
 import no.shhsoft.basus.utils.TextLocationHolder;
 import no.shhsoft.basus.value.ArrayValue;
 import no.shhsoft.basus.value.BooleanValue;
 import no.shhsoft.basus.value.IntegerValue;
 import no.shhsoft.basus.value.NumericValue;
 import no.shhsoft.basus.value.Value;
 
 /**
  * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
  */
 public final class Evaluator {
 
     private final StatementListener statementListener;
     private volatile boolean terminate;
 
     private static void ignore() {
     }
 
     private boolean shouldStopGlobalExecution(final EvaluationContext context) {
         return terminate || context.isStopProgram();
     }
 
     private boolean shouldStopLocalExecution(final EvaluationContext context) {
         return shouldStopGlobalExecution(context) || context.isReturnFromFunction();
     }
 
     private Value evaluateFunctionExpression(final FunctionExpression expression,
                                              final EvaluationContext context) {
         final Value[] args = new Value[expression.getNumExpressions()];
         for (int q = 0; q < args.length; q++) {
             args[q] = evaluateExpression(expression.getExpression(q), context);
         }
         return context.callFunction(expression.getFunctionName(), args, expression);
     }
 
     private static class ArrayAndIndex {
 
         private final ArrayValue array;
         private final int index;
 
         public ArrayAndIndex(final ArrayValue array, final int index) {
             this.array = array;
             this.index = index;
         }
 
         public ArrayValue getArray() {
             return array;
         }
 
         public int getIndex() {
             return index;
         }
 
     }
 
     private static void setVariable(final String name, final Value value,
                                     final EvaluationContext context,
                                     final boolean local, final TextLocationHolder holder) {
         if (local) {
             context.setLocalVariable(name, value, holder);
         } else {
             context.setVariable(name, value, holder);
         }
     }
 
     private static String getArrayIndexDescription(final int[] indexes, final int n) {
         final StringBuilder sb = new StringBuilder();
         sb.append('[');
         for (int q = 0; q < n; q++) {
             if (q > 0) {
                 sb.append(", ");
             }
             sb.append(indexes[q]);
         }
         if (n < indexes.length) {
             sb.append(", ...");
         }
         sb.append(']');
         return sb.toString();
     }
 
     /* TODO: this is a hotspot for optimizations */
     @SuppressWarnings("boxing")
     private ArrayAndIndex evaluateArrayAndIndex(final IndexExpression expression,
                                                 final EvaluationContext context,
                                                 final boolean writeOperation,
                                                 final boolean local) {
         /* writeOperations may index outside existing arrays, and create new
          * variables.  other operations may not. */
         Value expectedArrayValue = null;
         if (expression.getArray() instanceof VariableExpression) {
             final VariableExpression variableExpression = (VariableExpression) expression.getArray();
             if (context.isDefined(variableExpression.getVariableName()) || !writeOperation) {
                 expectedArrayValue = context.getVariable(variableExpression.getVariableName(), expression);
             } else {
                 expectedArrayValue = new ArrayValue();
                 setVariable(variableExpression.getVariableName(), expectedArrayValue,
                             context, local, expression);
             }
         } else {
             expectedArrayValue = evaluateExpression(expression.getArray(), context);
         }
         int index = -1;
         final int numExpressions = expression.getNumExpressions();
         if (numExpressions <= 0) {
             throw new RuntimeException("Number of expressions is <= 0");
         }
         final int[] indexes = new int[numExpressions];
         for (int q = 0; q < numExpressions; q++) {
             if (!(expectedArrayValue instanceof ArrayValue)) {
                 error("err.expectedArrayValue", expression, getArrayIndexDescription(indexes, q));
             }
             final ArrayValue arrayValue = (ArrayValue) expectedArrayValue;
             final Expression indexExpression = expression.getExpression(q);
             final Value indexValue = evaluateExpression(indexExpression, context);
             if (!(indexValue instanceof NumericValue)) {
                 error("err.expectedNumericArrayIndex", expression, q + 1);
             }
             index = ((NumericValue) indexValue).getValueAsInteger();
             if (index < 0) {
                 error("err.expectedNonNegativeArrayIndex", expression, q + 1);
             }
             indexes[q] = index;
             if (q == numExpressions - 1) {
                 break;
             }
             Value newExpectedArrayValue = null;
             if (arrayValue.hasValue(index)) {
                 newExpectedArrayValue = arrayValue.getValue(index);
             } else if (writeOperation) {
                 newExpectedArrayValue = new ArrayValue();
                 arrayValue.setValue(index, newExpectedArrayValue);
             }
             expectedArrayValue = newExpectedArrayValue;
         }
         final ArrayValue arrayValue = (ArrayValue) expectedArrayValue;
         if (index >= arrayValue.getLength() && !writeOperation) {
             error("err.indexOutsideArray", expression,
                   getArrayIndexDescription(indexes, numExpressions));
         }
         return new ArrayAndIndex(arrayValue, index);
     }
 
     private Value evaluateVariableExpression(final VariableExpression expression,
                                              final EvaluationContext context) {
         return context.getVariable(expression.getVariableName(), expression);
     }
 
     private Value evaluateIndexExpression(final IndexExpression expression,
                                           final EvaluationContext context) {
         final ArrayAndIndex arrayAndIndex = evaluateArrayAndIndex(expression, context, false, false);
        final ArrayValue array = arrayAndIndex.getArray();
        final int index = arrayAndIndex.getIndex();
        if (!array.hasValue(index)) {
            error("err.expectedUndefinedArrayElement", expression, Integer.valueOf(index));
        }
         return arrayAndIndex.getArray().getValue(arrayAndIndex.getIndex());
     }
 
     private Value evaluateConstantExpression(final ConstantExpression expression) {
         return expression.getConstant();
     }
 
     private Value evaluateUnaryExpression(final UnaryExpression expression,
                                           final EvaluationContext context) {
         Value ret = evaluateExpression(expression.getExpression(), context);
         if (expression.isNegate()) {
             ret = ValueCalc.negate(ret, expression);
         }
         return ret;
     }
 
     private Value evaluateExponentialExpression(final ExponentialExpression expression,
                                                 final EvaluationContext context) {
         Value ret = evaluateExpression(expression.getExpression(expression.getNumExpressions() - 1),
                                        context);
         /* right to left */
         for (int q = expression.getNumExpressions() - 2; q >= 0; q--) {
             final Value value = evaluateExpression(expression.getExpression(q), context);
             final OperatorType operator = expression.getOperator(q);
             switch (operator) {
                 case EXPONENTIATE:
                     ret = ValueCalc.pow(value, ret, expression);
                     break;
                 default:
                     throw new RuntimeException("Wrong operator found");
             }
         }
         return ret;
     }
 
     private Value evaluateMultiplicativeExpression(final MultiplicativeExpression expression,
                                                    final EvaluationContext context) {
         Value ret = evaluateExpression(expression.getExpression(0), context);
         for (int q = 1; q < expression.getNumExpressions(); q++) {
             final Value value = evaluateExpression(expression.getExpression(q), context);
             final OperatorType operator = expression.getOperator(q - 1);
             switch (operator) {
                 case MULTIPLY:
                     ret = ValueCalc.multiply(ret, value, expression);
                     break;
                 case DIVIDE:
                     ret = ValueCalc.divide(ret, value, expression);
                     break;
                 case MODULUS:
                     ret = ValueCalc.modulus(ret, value, expression);
                     break;
                 default:
                     throw new RuntimeException("Wrong operator found");
             }
         }
         return ret;
     }
 
     private Value evaluateAdditiveExpression(final AdditiveExpression expression,
                                              final EvaluationContext context) {
         Value ret = evaluateExpression(expression.getExpression(0), context);
         for (int q = 1; q < expression.getNumExpressions(); q++) {
             final Value value = evaluateExpression(expression.getExpression(q), context);
             final OperatorType operator = expression.getOperator(q - 1);
             switch (operator) {
                 case PLUS:
                     ret = ValueCalc.add(ret, value, expression);
                     break;
                 case MINUS:
                     ret = ValueCalc.subtract(ret, value, expression);
                     break;
                 default:
                     throw new RuntimeException("wrong operator found");
             }
         }
         return ret;
     }
 
     private Value evaluateRelationalExpression(final RelationalExpression expression,
                                                final EvaluationContext context) {
         final Value lhs = evaluateExpression(expression.getLeftHandSide(), context);
         final Value rhs = evaluateExpression(expression.getRightHandSide(), context);
         switch (expression.getOperator()) {
             case EQUAL:
                 return BooleanValue.valueOf(ValueCalc.equal(lhs, rhs, expression));
             case NOT_EQUAL:
                 return BooleanValue.valueOf(ValueCalc.notEqual(lhs, rhs, expression));
             case LESS:
                 return BooleanValue.valueOf(ValueCalc.less(lhs, rhs, expression));
             case LESS_OR_EQUAL:
                 return BooleanValue.valueOf(ValueCalc.lessOrEqual(lhs, rhs, expression));
             case GREATER:
                 return BooleanValue.valueOf(ValueCalc.greater(lhs, rhs, expression));
             case GREATER_OR_EQUAL:
                 return BooleanValue.valueOf(ValueCalc.greaterOrEqual(lhs, rhs, expression));
             default:
                 throw new RuntimeException("Unhandled relational operator "
                                            + expression.getOperator().toString());
         }
     }
 
     private boolean evaluateBooleanExpression(final Expression expression,
                                               final EvaluationContext context) {
         final Value value = evaluateExpression(expression, context);
         if (!(value instanceof BooleanValue)) {
             error("err.expectedBoolean", expression);
         }
         return ((BooleanValue) value).getValue();
     }
 
     private Value evaluateConditionalAndExpression(final ConditionalAndExpression expression,
                                                    final EvaluationContext context) {
         for (int q = 0; q < expression.getNumExpressions(); q++) {
             if (!evaluateBooleanExpression(expression.getExpression(q), context)) {
                 return BooleanValue.FALSE;
             }
         }
         return BooleanValue.TRUE;
     }
 
     private Value evaluateConditionalOrExpression(final ConditionalOrExpression expression,
                                                   final EvaluationContext context) {
         for (int q = 0; q < expression.getNumExpressions(); q++) {
             if (evaluateBooleanExpression(expression.getExpression(q), context)) {
                 return BooleanValue.TRUE;
             }
         }
         return BooleanValue.FALSE;
     }
 
     private Value evaluateExpression(final Expression expression, final EvaluationContext context) {
         if (expression instanceof VariableExpression) {
             return evaluateVariableExpression((VariableExpression) expression, context);
         }
         if (expression instanceof IndexExpression) {
             return evaluateIndexExpression((IndexExpression) expression, context);
         }
         if (expression instanceof FunctionExpression) {
             return evaluateFunctionExpression((FunctionExpression) expression, context);
         }
         if (expression instanceof ConditionalOrExpression) {
             return evaluateConditionalOrExpression((ConditionalOrExpression) expression, context);
         }
         if (expression instanceof ConditionalAndExpression) {
             return evaluateConditionalAndExpression((ConditionalAndExpression) expression, context);
         }
         if (expression instanceof RelationalExpression) {
             return evaluateRelationalExpression((RelationalExpression) expression, context);
         }
         if (expression instanceof AdditiveExpression) {
             return evaluateAdditiveExpression((AdditiveExpression) expression, context);
         }
         if (expression instanceof MultiplicativeExpression) {
             return evaluateMultiplicativeExpression((MultiplicativeExpression) expression, context);
         }
         if (expression instanceof ExponentialExpression) {
             return evaluateExponentialExpression((ExponentialExpression) expression, context);
         }
         if (expression instanceof UnaryExpression) {
             return evaluateUnaryExpression((UnaryExpression) expression, context);
         }
         if (expression instanceof ConstantExpression) {
             return evaluateConstantExpression((ConstantExpression) expression);
         }
         throw new RuntimeException("Unhandled Expression type " + expression.getClass().getName());
     }
 
     private void evaluateCallStatement(final CallStatement statement,
                                        final EvaluationContext context) {
         evaluateFunctionExpression(statement.getFunctionExpression(), context);
     }
 
     private void assign(final AssignableExpression lhs,
                         final EvaluationContext context, final Value value,
                         final boolean local, final TextLocationHolder holder) {
         if (lhs instanceof VariableExpression) {
             final VariableExpression variable = (VariableExpression) lhs;
             final String variableName = variable.getVariableName();
             if (local) {
                 context.setLocalVariable(variableName, value, holder);
             } else {
                 context.setVariable(variableName, value, holder);
             }
         } else if (lhs instanceof IndexExpression) {
             final IndexExpression indexExpression = (IndexExpression) lhs;
             final ArrayAndIndex arrayAndIndex = evaluateArrayAndIndex(indexExpression, context,
                                                                       true, local);
             arrayAndIndex.getArray().setValue(arrayAndIndex.getIndex(), value);
         } else {
             throw new RuntimeException("something is forgotten in the state of Denmark");
         }
     }
 
     private void evaluateAssignmentStatement(final AssignmentStatement statement,
                                              final EvaluationContext context) {
         final Value value = evaluateExpression(statement.getRightHandSide(), context);
         assign(statement.getLeftHandSide(), context, value, statement.isLocal(), statement);
     }
 
     private void evaluateForStatement(final ForStatement statement,
                                       final EvaluationContext context) {
         final AssignableExpression assignable = statement.getAssignable();
         Value current = evaluateExpression(statement.getFrom(), context);
         if (!(current instanceof NumericValue)) {
             error("err.expectedNumericValue", statement.getFrom(), "from");
         }
         for (;;) {
             if (shouldStopLocalExecution(context)) {
                 return;
             }
             assign(assignable, context, current, true, statement);
             Value step = null;
             if (statement.getStep() != null) {
                 step = evaluateExpression(statement.getStep(), context);
                 if (!(step instanceof NumericValue)) {
                     error("err.expectedNumericValue", statement.getStep(), Reserved.STEP.toString());
                 }
             } else {
                 step = IntegerValue.ONE;
             }
             final Value to = evaluateExpression(statement.getTo(), context);
             if (!(to instanceof NumericValue)) {
                 error("err.expectedNumericValue", statement.getTo(), Reserved.TO.toString());
             }
             final double dCurrent = ((NumericValue) current).getValueAsDouble();
             final double dTo = ((NumericValue) to).getValueAsDouble();
             final double dStep = ((NumericValue) step).getValueAsDouble();
             if (dStep > 0.0 && dCurrent > dTo || dStep < 0.0 && dCurrent < dTo) {
                 break;
             }
             evaluateStatementList(statement.getStatements(), context);
             current = ValueCalc.add(current, step, statement.getStep());
         }
     }
 
     private void evaluateRepeatStatement(final RepeatStatement statement,
                                          final EvaluationContext context) {
         final Value times = evaluateExpression(statement.getTimes(), context);
         if (!(times instanceof NumericValue)) {
             error("err.expectedNumericValue", statement.getTimes(), Reserved.TIMES.toString());
         }
         for (int q = ((NumericValue) times).getValueAsInteger() - 1; q >= 0; q--) {
             if (shouldStopLocalExecution(context)) {
                 return;
             }
             evaluateStatementList(statement.getStatements(), context);
         }
     }
 
     private void evaluateWhileStatement(final WhileStatement statement,
                                         final EvaluationContext context) {
         while (evaluateBooleanExpression(statement.getCondition(), context)) {
             if (shouldStopLocalExecution(context)) {
                 return;
             }
             evaluateStatementList(statement.getStatements(), context);
         }
     }
 
     private void evaluateIfStatement(final IfStatement statement,
                                      final EvaluationContext context) {
         final int n = statement.getConditions().size();
         for (int q = 0; q < n; q++) {
             if (evaluateBooleanExpression(statement.getConditions().get(q), context)) {
                 evaluateStatementList(statement.getConditionStatements().get(q), context);
                 return;
             }
         }
         final StatementList elseStatements = statement.getElseStatements();
         if (elseStatements != null) {
             evaluateStatementList(elseStatements, context);
         }
     }
 
     private void evaluateReturnStatement(final ReturnStatement statement,
                                          final EvaluationContext context) {
         final Value value = evaluateExpression(statement.getExpression(), context);
         context.setLocalVariable(UserFunction.RETURN_VARIABLE_NAME, value, statement);
         context.setReturnFromFunction(true);
     }
 
     private void evaluateStatement(final Statement statement, final EvaluationContext context) {
         if (shouldStopLocalExecution(context)) {
             return;
         }
         if (statementListener != null) {
             statementListener.startExecuting(statement, context);
         }
         if (statement instanceof CallStatement) {
             evaluateCallStatement((CallStatement) statement, context);
         } else if (statement instanceof AssignmentStatement) {
             evaluateAssignmentStatement((AssignmentStatement) statement, context);
         } else if (statement instanceof ForStatement) {
             evaluateForStatement((ForStatement) statement, context);
         } else if (statement instanceof RepeatStatement) {
             evaluateRepeatStatement((RepeatStatement) statement, context);
         } else if (statement instanceof WhileStatement) {
             evaluateWhileStatement((WhileStatement) statement, context);
         } else if (statement instanceof IfStatement) {
             evaluateIfStatement((IfStatement) statement, context);
         } else if (statement instanceof ReturnStatement) {
             evaluateReturnStatement((ReturnStatement) statement, context);
         } else if (statement instanceof FunctionStatement) {
             /* Already handled */
             ignore();
         } else if (statement instanceof BreakpointStatement) {
             /* TODO: implement. */
             ignore();
         } else {
             throw new RuntimeException("Unhandled Statement type " + statement.getClass().getName());
         }
         if (statementListener != null) {
             statementListener.endExecuting(statement, context);
         }
     }
 
     private void evaluateStatementList(final StatementList statementList,
                                        final EvaluationContext context) {
         for (int q = 0; q < statementList.getNumStatements(); q++) {
             if (shouldStopLocalExecution(context)) {
                 return;
             }
             evaluateStatement(statementList.getStatement(q), context);
         }
     }
 
     private void registerFunctions(final StatementList statementList, final EvaluationContext context) {
         for (int q = 0; q < statementList.getNumStatements(); q++) {
             final Statement statement = statementList.getStatement(q);
             if (statement instanceof FunctionStatement) {
                 final UserFunction userFunction = new UserFunction((FunctionStatement) statement,
                                                                    this);
                 context.registerFunction(userFunction, statement);
             }
         }
     }
 
     public Evaluator(final StatementListener currentStatementListener) {
         this.statementListener = currentStatementListener;
     }
 
     public static void error(final String key, final TextLocationHolder holder,
                              final Object... args) {
         TextLocation loc = null;
         if (holder != null) {
             loc = holder.getStartLocation();
         }
         throw new EvaluationException(ErrorUtils.getMessage(key, loc, args), loc);
     }
 
     public void evaluate(final StatementList statementList, final EvaluationContext context) {
         registerFunctions(statementList, context);
         evaluateStatementList(statementList, context);
     }
 
     public void terminate() {
         terminate = true;
     }
 
 }
