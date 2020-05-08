 /**
  * Copyright (C) 2012 Primitive Team <jpalka@gmail.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.primitive.javascript.interpreter;
 
 import static net.primitive.javascript.core.Convertions.toBoolean;
 import static net.primitive.javascript.core.Reference.getValue;
 import static net.primitive.javascript.core.Reference.putValue;
 import static net.primitive.javascript.interpreter.LexicalEnvironment.getIdentifierReference;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.primitive.javascript.core.Callable;
 import net.primitive.javascript.core.Constructor;
 import net.primitive.javascript.core.Convertions;
 import net.primitive.javascript.core.Reference;
 import net.primitive.javascript.core.Scope;
 import net.primitive.javascript.core.ScopeBindings;
 import net.primitive.javascript.core.Scriptable;
 import net.primitive.javascript.core.TypeErrorException;
 import net.primitive.javascript.core.Types;
 import net.primitive.javascript.core.Undefined;
 import net.primitive.javascript.core.ast.Arguments;
 import net.primitive.javascript.core.ast.ArrayLiteral;
 import net.primitive.javascript.core.ast.AssignmentExpression;
 import net.primitive.javascript.core.ast.BinaryExpression;
 import net.primitive.javascript.core.ast.CallExpression;
 import net.primitive.javascript.core.ast.CompoundAssignment;
 import net.primitive.javascript.core.ast.ConditionalExpression;
 import net.primitive.javascript.core.ast.Expression;
 import net.primitive.javascript.core.ast.ExpressionVisitor;
 import net.primitive.javascript.core.ast.FunctionExpression;
 import net.primitive.javascript.core.ast.Identifier;
 import net.primitive.javascript.core.ast.Literal;
 import net.primitive.javascript.core.ast.MemberExpression;
 import net.primitive.javascript.core.ast.NameValuePair;
 import net.primitive.javascript.core.ast.NewExpression;
 import net.primitive.javascript.core.ast.ObjectLiteral;
 import net.primitive.javascript.core.ast.This;
 import net.primitive.javascript.core.ast.UnaryExpression;
 import net.primitive.javascript.core.ast.WrappedExpression;
 import net.primitive.javascript.core.natives.JSArray;
 
 public class ExpressionVisitorImpl implements ExpressionVisitor {
 
 	private Object result;
 	private final RuntimeContext context;
 
 	protected ExpressionVisitorImpl(RuntimeContext context) {
 		super();
 		this.context = context;
 	}
 
 	public Object getResult() {
 		return result;
 	}
 
 	@Override
 	public void visitBinaryExpression(BinaryExpression binaryExpression) {
 		binaryExpression.getOp1().accept(this);
 		Object result1 = getValue(result);
 
 		binaryExpression.getOp2().accept(this);
 		Object result2 = getValue(result);
 
 		result = binaryExpression.getOperator().operator(result1, result2);
 	}
 
 	@Override
 	public void visitLiteral(Literal literal) {
 		result = literal.getValue();
 	}
 
 	@Override
 	public void visitWrappedExpression(WrappedExpression wrappedExpression) {
 		wrappedExpression.getExpression1().accept(this);
 	}
 
 	@Override
 	public void visitIdentifier(Identifier identifier) {
 
 		result = getIdentifierReference(context.currentExecutionContext().getLexicalEnvironment(), identifier.getIdentfierName());
 	}
 
 	@Override
 	public void visitAssignmentExpression(AssignmentExpression assignmentExpression) {
 		assignmentExpression.getRightHandSideExpression().accept(this);
 		Object value = getValue(result);
 		assignmentExpression.getLeftHandSideExpression().accept(this);
 		putValue(result, value);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.primitive.javascript.core.visitors.ExpressionVisitor#
 	 * visitLeftHandSideExpression
 	 * (net.primitive.javascript.core.ast.LeftHandSideExpression)
 	 */
 	@Override
 	public void visitLeftHandSideExpression(Expression leftHandSideExpression) {
 		leftHandSideExpression.accept(this);
 	}
 
 	@Override
 	public void visitUnaryExpression(UnaryExpression unaryExpression) {
 
 		unaryExpression.getOperand().accept(this);
 
 		result = unaryExpression.getOperator().operator(result);
 	}
 
 	@Override
 	public void visitMemberExpression(MemberExpression memberExpression) {
 		memberExpression.getExpression().accept(this);
 
 		Expression[] suffixes = memberExpression.getExpresionSuffixes();
 		int len = suffixes != null ? suffixes.length : 0;
 		if (len > 0) {
 			Object baseReference = result;
 			Object baseValue = null;
 			Expression suffix;
 			for (int i = 0; i < len; i++) {
 				suffix = suffixes[i];
 				String propertyNameString;
 				if (Identifier.class.equals(suffix.getClass())) {
 					propertyNameString = ((Identifier) suffix).getIdentfierName();
 				} else {
 					suffix.accept(this);
 					Object propertyNameReference = result;
 					Object propertyNameValue = Reference.getValue(propertyNameReference);
 					propertyNameString = Convertions.toString(propertyNameValue);
 				}
 				baseValue = Reference.getValue(baseReference);
 
 				baseReference = new ObjectReference(baseValue, propertyNameString);
 			}
 			result = baseReference;
 		}
 	}
 
 	@Override
 	public void visitCallExpression(CallExpression callExpression) {
 		Expression memberExpression = callExpression.getMemberExpression();
 		memberExpression.accept(this);
 		Object ref = result;
 		Object func = Reference.getValue(ref);
 		Object thisValue = Undefined.Value;
 
 		// resolving this
 		if (ref instanceof Reference) {
 			Reference reference = (Reference) ref;
 			if (reference.isPropertyReference()) {
 				thisValue = reference.getBase();
 			} else {
 				thisValue = ((ScopeBindings) reference.getBase()).implicitThisValue();
 			}
 		} else {
 			thisValue = context.getGlobalObject();
 		}
 
 		// binding parameters
 		Expression arguments = callExpression.getArguments();
 		// evaluate parameters
 		arguments.accept(this);
 
 		@SuppressWarnings("unchecked")
 		List<Object> values = (List<Object>) result;
 
 		Callable callable = (Callable) func;
 
 		Object[] vals = callable.bindParameters(values.toArray(new Object[] {}));
 
 		result = callable.call(callable.getScope(), (Scriptable) thisValue, vals);
 
 	}
 
 	@Override
 	public void visitObjectLiteral(ObjectLiteral objectLiteral) {
 		List<NameValuePair> nameValuePairs = objectLiteral.getNameValuePairs();
 		Scriptable scriptableObject = context.newObject();
 		for (NameValuePair pair : nameValuePairs) {
 			pair.getValue().accept(this);
 			scriptableObject.put((String) pair.getName(), Reference.getValue(result));
 		}
 		result = scriptableObject;
 	}
 
 	@Override
 	public void visitFunctionExpression(FunctionExpression functionExpression) {
 
 		StatementExecutionContext executionContext = context.currentExecutionContext();
 		Scope lexenv = executionContext.getLexicalEnvironment();
 		JSNativeFunction function = new JSNativeFunction(lexenv, functionExpression.getFunctionName(), functionExpression.getParameterList(), functionExpression.getFunctionBody());
 		function.setPrototype(context.getObjectPrototype());
 		result = function;
 		
 	}
 
 	@Override
 	public void visitThis(This this1) {
 		result = context.currentExecutionContext().getThisBinding();
 	}
 
 	@Override
 	public void visitConditionalExpression(ConditionalExpression conditionalExpression) {
 
 		conditionalExpression.getOp1().accept(this);
 
 		Object lref = result;
 
 		if (toBoolean(getValue(lref))) {
 			conditionalExpression.getOp2().accept(this);
 			result = getValue(result);
 			return;
 		}
 
 		conditionalExpression.getOp3().accept(this);
 		result = getValue(result);
 	}
 
 	@Override
 	public void visitNewExpression(NewExpression newExpression) {
 		newExpression.getExpression().accept(this);
 		Object ref = result;
 		Object constructor = getValue(ref);
 		if (Types.isConstructor(constructor)) {
 			result = ((Constructor) constructor).construct(null, null);
 			return;
 		}
 		throw new TypeErrorException();
 	}
 
 	@Override
 	public void visitArguments(Arguments arguments) {
 		List<Expression> argumentsList = arguments.getArgumentsList();
 		List<Object> values = new ArrayList<Object>();
 		for (Expression exp : argumentsList) {
 			exp.accept(this);
 			values.add(result);
 		}
 		result = values;
 	}
 
 	@Override
 	public void visitCompoundAssignment(CompoundAssignment compoundAssignment) {
 		compoundAssignment.getLeftExpression().accept(this);
 		Object lref = result;
 		Object lval = getValue(lref);
 
 		compoundAssignment.getRightExpression().accept(this);
 		Object rref = result;
 		Object rval = getValue(rref);
 
 		Object r = compoundAssignment.getOperator().operator(lval, rval);
 
 		Reference.putValue(lref, r);
 
 		result = r;
 
 	}
 
 	@Override
 	public void visitArrayLiteral(ArrayLiteral arrayLiteral) {
 
 		Scriptable array = context.newArray();
 
		List values = arrayLiteral.getValues();
 		ArrayList<Object> eval = new ArrayList<Object>();
 		for (Object obj : values) {
 			((Expression) obj).accept(this);
			eval.add(result);
 		}
 
 		JSArray.push(array, eval.toArray(new Object[eval.size()]));
 
 		result = array;
 
 	}
 
 }
