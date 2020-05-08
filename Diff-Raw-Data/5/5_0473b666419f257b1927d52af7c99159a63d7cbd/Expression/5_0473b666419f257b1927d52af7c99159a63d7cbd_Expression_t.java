 /*******************************************************************************
  * Copyright (c) 2004, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  *  $RCSfile: Expression.java,v $
 *  $Revision: 1.15 $  $Date: 2005/11/11 15:49:30 $ 
  */
 package org.eclipse.jem.internal.proxy.core;
 
 import java.text.MessageFormat;
 import java.util.*;
 
 import org.eclipse.jem.internal.proxy.initParser.tree.*;
  
 /**
  * This is implementation of IExpression. It encapsulates much of the processing required
  * into a common form that will be turned into simple push/pop/evaluate type of interaction with the
  * actual other side. All registry specific implementations of IExpression must subclass this class.
  * <p>
  * It will maintain a stack of the expressions. As the expressions come in they will be stacked if not
  * able to be executed immediately. The expressions come to this class in an  outside to inside order,
  * but they need to be processed in an inside-out order instead. 
  * <p>
  * Subclasses will be used for the different types of proxy interfaces. The abstract methods will
  * then be the simple interface. 
  * <p>
  * It is not meant for subclasses to override the actual create expression methods because the processing the stack
  * is very sensitive and must execute in the proper sequence. So the create methods are final for this reason.
  * <p>
  * This class is not thread-safe.
  * <p>
  * This class also has API of its own and can be used by customers for advanced usage. Those advanced API are
  * listed on each method as to whether it is customer API or implementers API (i.e. API for implementers of
  * expression subclasses to use). 
  * 
  * 
  * @since 1.0.0
  */
 public abstract class Expression implements IExpression {
 
 	/*
 	 * We have stack here, but rather than create a class that does the
 	 * stack protocol, will simply have some private methods to do
 	 * the same thing for the stack. (Note: Can't use java.util.Stack
 	 * because that is a synchronized class, and don't want the overhead). 
 	 * 
 	 * The purpose of the stack is to stack up expressions that have not yet
 	 * been evaluated. 
 	 * 
 	 * Each expression type will control the content of what it pushes and pops.
 	 * The expression type will be the last thing it pushes so that on popping
 	 * we know what kind of expression is now completed and ready for evaluation.
 	 */
 	private ArrayList controlStack = new ArrayList(30);
 	
 	protected final ProxyFactoryRegistry registry;
 	protected final IStandardBeanProxyFactory beanProxyFactory;
 	protected Boolean traceFlag;
 	
 	/**
 	 * Answer whether trace has been explicitly set.
 	 * This is not in the IExpression interface because it is for advanced users.
 	 * @return
 	 * 
 	 * @since 1.1.0
 	 */
 	public boolean isTraceSet() {
 		return traceFlag != null; 
 	}
 	
 	/**
 	 * Answer if trace is on. If not explicitly set this will answer false.
 	 * Use {@link Expression#isTraceSet()} first to determine if this
 	 * should be called or not.
 	 * This is not in the IExpression interface because it is for advanced users.
 	 * @return
 	 * 
 	 * @since 1.1.0
 	 */
 	public boolean isTrace() {
 		return traceFlag != null ? traceFlag.booleanValue() : false;
 	}
 	
 	/**
 	 * Explicitly set the trace flag. This will only be honoured before any
 	 * expressions have been created. After that this will be ignored.
 	 * The trace is initially set to use default. Once set it cannot be unset.
 	 * This is not in the IExpression interface because it is for advanced users.
 	 * @param trace
 	 * 
 	 * @since 1.1.0
 	 */
 	public void setTrace(boolean trace) {
 		traceFlag = Boolean.valueOf(trace);
 	}
 	
 	/**
 	 * Push an object onto the control stack.
 	 * 
 	 * @param o
 	 * 
 	 * @since 1.0.0
 	 */
 	protected final void push(Object o) {
 		controlStack.add(o);
 	}
 	
 	/**
 	 * Pop an object off of the control stack
 	 * @return
 	 * 
 	 * @since 1.0.0
 	 */
 	protected final Object pop() {
 		return controlStack.remove(controlStack.size()-1);
 	}
 	
 	/**
 	 * Peek at an object from the control stack. <code>fromTop</code> is how far from the top of the stack to look.
 	 * If it one, then it is the top entry, two is the next one down. Zero is an invalid value for the parameter.
 	 * @param fromTop How far from the top to peek. <code>1</code> is the top, not zero.
 	 * @return
 	 * 
 	 * @since 1.0.0
 	 */
 	protected final Object peek(int fromTop) {
 		// 1 means the top, 2 is the next one down.
 		return controlStack.get(controlStack.size()-fromTop);
 	}
 	
 	/*
 	 * Expression type constants.
 	 */
 	
 	/*
 	 * ARRAY ACCESS expression.
 	 * The expression stack will have:
 	 * 	IExpression.ARRAYACCESS_ARRAY
 	 * 	IExpression.ARRAYACCESS_INDEX (for 1 to n times depending on index count)
 	 * 	PROCESS_EXPRESSION
 	 * 
 	 * The value stack will have
 	 * 	ARRAYACCESS
 	 * 	Integer(index count) 
 	 */
 	private static final Integer ARRAYACCESS_INDEX_1 = new Integer(1);	// Use in normal case of one index count. Saves object creation.
 	
 	/*
 	 * ARRAY CREATION expression.
 	 * The expression stack will have:
 	 * 	ARRAYCREATION_INITIALIZER - if hasInitializer
 	 *  IExpression.ARRAYCREATION_DIMENSION (for 0 to n times depending on dimension count)
 	 *  PROCESS_EXPRESSION
 	 * 
 	 * The value stack will have
 	 *  ARRAYCREATION
 	 *  type (either a string representing the type, or an IBeanProxyType representing the type).
 	 *  Integer(dimension count) (if zero then there is an initializer) 
 	 * 
 	 * 
 	 * Note: Array Initializer works with this in that it will peek into the value stack two entries down
 	 * to find the type of array it should be creating.
 	 */
 	private static final Integer ARRAY_CREATION_DIMENSION_1 = new Integer(1);	// Use in normal case of one dimension. Save object creation.
 	private static final Integer ARRAY_CREATION_DIMENSION_0 = new Integer(0);	// Use in normal case of initializer. Save object creation.
 	private static final ForExpression ARRAY_INITIALIZER = new ExpressionEnum(Integer.MIN_VALUE+1, "Array Initializer Internal"); //$NON-NLS-1$
 	
 	/*
 	 * ARRAY INITIALIZER expression
 	 * The expression stack will have:
 	 * 	IExpression.ARRAYINITIALIZER_EXPRESSION (for n times depending on number of expressions count)
 	 * 	PROCESS_EXPRESSION
 	 * 
 	 * The value stack will have
 	 * 	ARRAYINITIALIZER
 	 *  type (either a string representing the type, or an IBeanProxyType representing the type).
 	 * 		I.e. if array being created is int[][], the value pushed here will be int[]. This is because when created
 	 * 		the array will wind up with int[expressioncount][] in the end.
 	 * 	Integer (expression count)
 	 * 
 	 * Note: Imbedded Array Initializers works with this in that it will peek into the value stack two entries down
 	 * to find the type of array it should be creating.
 	 */
 	private static final Integer ARRAYINITIALIZER_COUNT_0 = new Integer(0);	// Use in normal case of empty array. Save object creation.
 	private static final Integer ARRAYINITIALIZER_COUNT_1 = new Integer(1);	// Use in normal case of one element array. Save object creation.
 	private static final Integer ARRAYINITIALIZER_COUNT_2 = new Integer(2);	// Use in normal case of two element array. Save object creation.	
 	
 	/*
 	 * CAST expression.
 	 * The expression stack will have:
 	 * 	IExpression.CAST_EXPRESSION
 	 * 	PROCESS_EXPRESSION
 	 * 
 	 * The value stack will have:
 	 * 	CAST
 	 * 	type (either a string representing the type, or an IBeanProxyType representing the type).
 	 */
 
 	/*
 	 * CLASS INSTANCE CREATION expression.
 	 * The expression stack will have:
 	 *  IExpression.CLASSINSTANCECREATION_ARGUMENT (for 0 to n times depending on argument count)
 	 *  PROCESS_EXPRESSION
 	 * 
 	 * The value stack will have
 	 *  CLASSINSTANCECREATION
 	 *  type (either a string representing the type, or an IBeanProxyType representing the type).
 	 *  Integer(argument count) 
 	 * 
 	 * 
 	 * Note: Array Initializer works with this in that it will peek into the value stack two entries down
 	 * to find the type of array it should be creating.
 	 */
 	private static final Integer CLASS_INSTANCE_CREATION_ARGUMENTS_1 = new Integer(1);	// Use in normal case of one argument. Save object creation.
 	private static final Integer CLASS_INSTANCE_CREATION_ARGUMENTS_0 = new Integer(0);	// Use in normal case of no arguments (default ctor). Save object creation.
 
 	/*
 	 * CONDITIONAL expression.
 	 * Since this can cause skipping of expressions (e.g. if condition is false, then the true condition should not be evaluated),
 	 * we need to have a process expression and process call to the other side for each expression so that it can
 	 * determine if it should be ignored or not.
 	 * 
 	 * The expression stack will have:
 	 * 	IExpression.CONDITIONAL_CONDITION
 	 * 	PROCESS_EXPRESSION
 	 * 	IExpression.CONDITIONAL_TRUE
 	 * 	PROCESS_EXPRESSION
 	 * 	IExpression.CONDITIONAL_FALSE
 	 * 	PROCESS_EXPRESSION
 	 * 
 	 * The value stack will have:
 	 * 	CONDITIONAL
 	 * 	CONDITIONAL_CONDITION
 	 * 	CONDITIONAL
 	 * 	CONDITIONAL_TRUE
 	 * 	CONDITIONAL
 	 * 	CONDITIONAL_FALSE
 	 * 
 	 */
 	
 	/*
 	 * PREFIX expression.
 	 * The expression stack will have:
 	 * 	IExpression.PREFIX_OPERAND
 	 * 	PROCESS_EXPRESSION
 	 * 
 	 * The value stack will have:
 	 * 	PREFIX
 	 * 	operator (using Integer prefix operator constants defined here) 
 	 */
 	
 	/*
 	 * INFIX expression.
 	 * Since two types of infix operators (conditional and AND or) can cause skipping of expressions (e.g. once
 	 * conditional and sees a false expression, the rest of the expressions are skipped and should not be evaluated),
 	 * we need to have a process expression and process call to the other side for each expression so that it can
 	 * determine if it should be ignored or not.
 	 * 
 	 * The expression stack will have:
 	 * 	IExpression.INFIX_LEFT
 	 * 	PROCESS_EXPRESSION
 	 * 	IExpression.INFIX_RIGHT
 	 *  PROCESS_EXPRESSION
 	 * 		(for 0 to n times depending upon extended count)
 	 * 		IExpression.INFIX_EXTENDED 
 	 * 		PROCESS_EXPRESSION
 	 * 
 	 * The value stack will have:
 	 * 	INFIX
 	 * 	operator (using Integer infix operator constants defined here)
 	 *  IN_LEFT
 	 * 		(for (extendedCount) times) This will cover the right one and all but last extended
 	 * 		INFIX
 	 * 		operator (using Integer infix operator constants defined here)
 	 *  	IN_OTHER
 	 * INFIX
 	 * 	operator (using Integer infix operator constants defined here)
 	 *  IN_LAST (this is covers either the right one if no extended, or the last extended)
 	 */
 	
 	/*
 	 * INSTANCEOF expression.
 	 * The expression stack will have:
 	 * 	IExpression.INSTANCEOF_EXPRESSION
 	 * 	PROCESS_EXPRESSION
 	 * 
 	 * The value stack will have:
 	 * 	INSTANCEOF
 	 * 	type (either a string representing the type, or an IBeanProxyType representing the type).
 	 */
 
 	/*
 	 * Field access expression.
 	 * The expression stack will have:
 	 * 	IExpression.FIELD_RECEIVER (if hasReceiver is true)
 	 * 	PROCESS_EXPRESSION
 	 * 
 	 * The value stack will have:
 	 * 	FIELDACCESS
 	 * 	name (the name of the field)
 	 *  Boolean (true if has receiver)
 	 */
 
 	/*
 	 * Method invocation expression.
 	 * The expression stack will have:
 	 * 	IExpression.METHOD_RECEIVER (if hasReceiver is true)
 	 * 	IExpression.METHOD_ARGUMENT (0 to n times for how many arguments).
 	 * 	PROCESS_EXPRESSION
 	 * 
 	 * The value stack will have:
 	 * 	METHODINVOCATION
 	 * 	name (the name of the method)
 	 *  Boolean (true if has receiver)
 	 *  argCount (the number of arguments).
 	 */
 	private static final Integer METHOD_ARGUMENTS_1 = new Integer(1);	// Use in normal case of one argument. Save object creation.
 	private static final Integer METHOD_ARGUMENTS_0 = new Integer(0);	// Use in normal case of no arguments. Save object creation.
 	
 
 	/*
 	 * Assignment expression
 	 * The expression stack will have:
 	 *  IExpression.ASSIGNMENT_RIGHT
 	 *  IExpression.ASSIGNMENT_LEFT
 	 *  PROCESS_EXPRESSION
 	 * 
 	 * The value stack will have:
 	 *  ASSIGNMENT
 	 *  left expression (variable reference)
 	 *  right expression
 	 */
 
 	/*
 	 * Assignment proxy expression
 	 * The expression stack will have:
 	 *  IExpression.ASSIGNMENT_RIGHT
 	 *  PROCESS_EXPRESSION
 	 * 
 	 * The value stack will have:
 	 *  ASSIGNMENT_PROXY
 	 *  expression proxy (an expression proxy)
 	 */
 	
 	/*
 	 * Next valid for expression stack. This is kept as a stack also.
 	 * As the expressions come in, the appropriate order (in reverse)
 	 * of expression types will be pushed, and then popped as they 
 	 * come in.
 	 * 
 	 * Since we can't have an array list of ints, will simulate the
 	 * stack here.
 	 */
 	private ForExpression[] nextForExpressionStack = new ForExpression[30];
 	private int nextForExpressionStackPos = -1;	// Position of top entry in stack.
 	private boolean expressionValid = true;	// Is the expression currently valid.
 	private String invalidMsg = null;	// Msg for being invalid if default msg not sufficient.
 	private List expressionProxies;	// List of expression proxies. The index of the proxy is its id. This list must never shrink in size.
 
 	// A MarkEntry. To allow restore in case of error.
 	private static class MarkEntry {
 		public int markID;
 		public int controlStackPos;	// Position of control stack at time of mark.
 		public int nextExpressionStackPos;	// Position of nextForExpression stack at time of mark.
 		public int expressionProxiesPos;	// Position of expressionProxies list at time of mark.
 	}
 	
 	private int highestMarkID = 0;	// Next mark id. '0' is invalid, as in no marks. This is incremented for each new mark. Never decremented.
 	private MarkEntry currentMarkEntry;	// Just a convienence to the current mark entry so no need to look into the list every time.
 	private List markEntries;	// Stack of mark entries.
 	
 	// This class is here so we can add our special internal ForExpression: PROCESS_EXPRESSION. These are never used outside Expression.
 	private static class ExpressionEnum extends ForExpression {
 
 		public ExpressionEnum(int value, String name) {
 			super(value, name);
 		}
 		
 	}
 	
 	// This is pushed onto the next expression stack, and when it is popped, then the expression is complete and ready to be pushed to the proxy side.
 	private static final ForExpression PROCESS_EXPRESSION = new ExpressionEnum(Integer.MIN_VALUE, "Process Expression"); //$NON-NLS-1$
 	
 	// This is pushed onto the next expression stack for end block and will test if this there to make sure that it is being called correctly.
 	private static final ForExpression BLOCKEND_EXPRESSION = new ExpressionEnum(PROCESS_EXPRESSION.getValue()-2, "End Block Expression"); //$NON-NLS-1$
 
 	// This is pushed onto the next expression stack for end try and will test if this there to make sure that it is being called correctly.
 	private static final ForExpression TRYEND_EXPRESSION = new ExpressionEnum(BLOCKEND_EXPRESSION.getValue()-1, "End Try Expression"); //$NON-NLS-1$
 
 	// This is pushed onto the next expression stack for catch and will test if this there to make sure that it is being called correctly.
 	private static final ForExpression TRYCATCH_EXPRESSION = new ExpressionEnum(TRYEND_EXPRESSION.getValue()-1, "Catch Expression"); //$NON-NLS-1$
 	
 
 	// This is pushed onto the next expression stack for begin thread transfer and will test if this there to make sure that it is being called correctly.
 	private static final ForExpression THREADTRANSFER_EXPRESSION = new ExpressionEnum(TRYCATCH_EXPRESSION.getValue()-1, "Catch Expression"); //$NON-NLS-1$
 
 	// This is pushed onto the next expression stack for end subexpression and will test if this there to make sure that it is being called correctly.
 	private static final ForExpression SUBEXPRESSIONEND_EXPRESSION = new ExpressionEnum(THREADTRANSFER_EXPRESSION.getValue()-2, "End Subexpression"); //$NON-NLS-1$
 
 	/**
 	 * Check the for expression, and if legal, set to the next valid for expression type,
 	 * if it can. If the stack entry is ROOTEXPRESSION, and the forExpression is ROOTEXPRESSION,
 	 * then the expression is allowed, but it is not popped. It must be popped later when appropriate.
 	 * <p>
 	 * This is for "block" expressions. We don't want to pop down the stack passed the ROOTEXPRESSION
 	 * that got added by the create block until we get an end block. That allows root expressions to
 	 * be added to the block without the stack popping up past the block start in the stack.
 	 * 
 	 * @param forExpression
 	 * @throws IllegalStateException
 	 * 
 	 * @since 1.0.0
 	 */
 	protected final void checkForExpression(ForExpression forExpression) throws IllegalStateException {
 		if (expressionValid) {
 			if (nextForExpressionStackPos == -1)
 				if (forExpression == ForExpression.ROOTEXPRESSION)
 					return;	// valid. We are at the root (i.e. nothing is waiting).
 				else
 					;	// invalid. drop through
 			else if (nextForExpressionStack[nextForExpressionStackPos] == forExpression) {
 				// Valid, either the root expression matched (We don't implicitly pop those. That needs to be done explicitly). 
 				// Or we matched non-root, those will be popped.
 				if (forExpression != ForExpression.ROOTEXPRESSION) {
 					popForExpression();	// Pop the stack since stack not a root expression.
 				}
 				return;	
 			}
 		} else {
 			String expMsg = invalidMsg != null ? MessageFormat.format(ProxyMessages.Expression_InInvalidStateDueTo_EXC_, new Object[] {invalidMsg}) : ProxyMessages.Expression_InInvalidState_EXC_; 
 			throw new IllegalStateException(expMsg);
 		}
 		
 		// If we got here, then invalid.
		ForExpression expected = nextForExpressionStackPos >= 0 ? nextForExpressionStack[nextForExpressionStackPos] : ForExpression.ROOTEXPRESSION;
 		expressionValid = false;
 		throw new IllegalStateException(MessageFormat.format(ProxyMessages.Expression_TypeSentInInvalidOrder_EXC_, new Object[] {forExpression, expected})); 
 	}
 	
 	/**
 	 * Pop the top for expression, whatever it is.
 	 * @throws IllegalStateException thrown if try to pop through through the current mark entry. The endMark is the only one who can do this.
 	 * @since 1.1.0
 	 */
 	protected final void popForExpression() throws IllegalStateException {
 		if (expressionValid && nextForExpressionStackPos >= 0) {
 			nextForExpressionStackPos--;
 			if (currentMarkEntry != null && nextForExpressionStackPos < currentMarkEntry.nextExpressionStackPos) {
 				nextForExpressionStackPos++;	// Restore to what it was
 				throwInvalidMarkNesting();
 			}
 		}
 	}
 
 	/*
 	 * @throws IllegalStateException
 	 * 
 	 * @since 1.1.0
 	 */
 	private void throwInvalidMarkNesting() throws IllegalStateException {
 		expressionValid = false;
 		throw new IllegalStateException(MessageFormat.format(ProxyMessages.Expression_InvalidMarkNesting, new Object[] {new Integer(currentMarkEntry != null ? currentMarkEntry.markID : 0)})); 
 	}
 	
 	/**
 	 * Peek into the for expression stack to see if the top entry is the passed in value. It will
 	 * not pop the stack nor throw any exceptions.
 	 * 
 	 * @param forExpression The top expression flag will be compared against this value.
 	 * @return <code>true</code> if the top expression equals the parameter passed in.
 	 * 
 	 * @since 1.0.0
 	 */
 	protected final boolean peekForExpression(ForExpression forExpression) {
 		if (expressionValid) {
 			if (nextForExpressionStackPos == -1)
 				if (forExpression == ForExpression.ROOTEXPRESSION)
 					return true;	// valid. We are at the root (i.e. nothing is waiting).
 				else
 					;	// invalid. drop through
 			else if (nextForExpressionStack[nextForExpressionStackPos] == forExpression)
 				return true;	// Valid, the top expression matched.
 		} 
 		return false;
 	}	
 	
 	/**
 	 * Mark this expression as now invalid.
 	 */
 	protected final void markInvalid() {
 		expressionValid = false;
 	}
 	
 	/**
 	 * Mark this expression as now invalid, but supply a message to go with it.
 	 * 
 	 * @param msg
 	 * 
 	 * @since 1.0.0
 	 */
 	protected final void markInvalid(String msg) {
 		invalidMsg = msg;
 		markInvalid();
 	}
 	
 	public void close() {
 		nextForExpressionStackPos = -1;
 		controlStack.clear();
 		if (expressionProxies != null)
 			markAllProxiesNotResolved(expressionProxies);	// They weren't processed, close must of been called early.
 		expressionProxies = null;
 		markEntries = null;
 		expressionValid = false;
 		closeProxy();
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#isValid()
 	 */
 	public boolean isValid() {
 		return expressionValid;
 	}
 	
 	/*
 	 * Check if the pending expression is ready for evaluation.
 	 * It is complete if the next entry on the stack is a PROCESS_EXPRESSION
 	 */
 	private boolean expressionReady() {
 		if (nextForExpressionStackPos >= 0 && nextForExpressionStack[nextForExpressionStackPos] == PROCESS_EXPRESSION) {
 			checkForExpression(PROCESS_EXPRESSION);	// pop it
 			return true;
 		} else
 			return false;
 	}
 	
 	/*
 	 * Push the next expression type.
 	 */
 	private void pushForExpression(ForExpression nextExpression) {
 		if (++nextForExpressionStackPos >= nextForExpressionStack.length) {
 			// Increase stack size.
 			ForExpression[] newStack = new ForExpression[nextForExpressionStackPos*2];	// So room to grow without excessive allocations.
 			System.arraycopy(nextForExpressionStack, 0, newStack, 0, nextForExpressionStack.length);
 			nextForExpressionStack = newStack;
 		}
 		nextForExpressionStack[nextForExpressionStackPos] = nextExpression;
 	}
 	
 	/*
 	 * Check if expression is complete, and if it is, process it.
 	 */
 	private void processExpression() {
 		while (expressionReady()) {
 			try {
 				// We've received all of the expressions for the expression, so process it.
 				int expType = ((InternalExpressionTypes) pop()).getValue();
 				switch (expType) {
 					case InternalExpressionTypes.CAST_EXPRESSION_VALUE:
 						pushCastToProxy((IProxyBeanType) pop());
 						break;
 					case InternalExpressionTypes.INSTANCEOF_EXPRESSION_VALUE:
 						pushInstanceofToProxy((IProxyBeanType) pop());
 						break;
 					case InternalExpressionTypes.PREFIX_EXPRESSION_VALUE:
 						pushPrefixToProxy((PrefixOperator)pop());
 						break;
 					case InternalExpressionTypes.INFIX_EXPRESSION_VALUE:
 						pushInfixToProxy((InfixOperator) pop(), (InternalInfixOperandType) pop());
 						break;
 					case InternalExpressionTypes.ARRAY_ACCESS_EXPRESSION_VALUE:
 						pushArrayAccessToProxy(((Integer) pop()).intValue());
 						break;
 					case InternalExpressionTypes.ARRAY_CREATION_EXPRESSION_VALUE:
 						pushArrayCreationToProxy((IProxyBeanType) pop(), ((Integer) pop()).intValue());
 						break;
 					case InternalExpressionTypes.ARRAY_INITIALIZER_EXPRESSION_VALUE:
 						pushArrayInitializerToProxy((IProxyBeanType) pop(), ((Integer) pop()).intValue(), ((Integer) pop()).intValue());
 						break;
 					case InternalExpressionTypes.CLASS_INSTANCE_CREATION_EXPRESSION_VALUE:
 						pushClassInstanceCreationToProxy((IProxyBeanType) pop(), ((Integer) pop()).intValue());
 						break;
 					case InternalExpressionTypes.FIELD_ACCESS_EXPRESSION_VALUE:
 						pushFieldAccessToProxy(pop(), ((Boolean) pop()).booleanValue());
 						break;
 					case InternalExpressionTypes.METHOD_EXPRESSION_VALUE:
 						pushMethodInvocationToProxy(pop(), ((Boolean) pop()).booleanValue(), ((Integer) pop()).intValue());
 						break;
 					case InternalExpressionTypes.CONDITIONAL_EXPRESSION_VALUE:
 						pushConditionalToProxy((InternalConditionalOperandType) pop());
 						break;
 					case InternalExpressionTypes.ASSIGNMENT_PROXY_EXPRESSION_VALUE:
 						pushAssignmentToProxy((ExpressionProxy) pop());
 						break;
 					case InternalExpressionTypes.ASSIGNMENT_EXPRESSION_VALUE:
 						pushAssignmentToProxy();
 						break;
 					case InternalExpressionTypes.BLOCK_END_EXPRESSION_VALUE:
 						pushBlockEndToProxy(((Integer) pop()).intValue());
 						break;
 					case InternalExpressionTypes.TRY_END_EXPRESSION_VALUE:
 						pushTryEndToProxy(((Integer) pop()).intValue());
 						break;
 					case InternalExpressionTypes.THROW_EXPRESSION_VALUE:
 						pushThrowToProxy();
 						break;
 					case InternalExpressionTypes.IF_TEST_EXPRESSION_VALUE:
 						pushIfTestToProxy();
 						break;												
 					case InternalExpressionTypes.IF_ELSE_EXPRESSION_VALUE:
 						pushIfElseToProxy((InternalIfElseOperandType) pop());
 						break;	
 					case InternalExpressionTypes.SUBEXPRESSION_END_EXPRESSION_VALUE:
 						pushSubexpressionEndToProxy(((Integer) pop()).intValue());
 						break;						
 					default:
 						internalProcessUnknownExpressionType(expType);
 						break;
 				}
 			} catch (RuntimeException e) {
 				markInvalid();
 				throw e;
 			}
 		}
 	}
 	
 
 	private void internalProcessUnknownExpressionType(int expressionType) throws IllegalArgumentException {
 		if (!processUnknownExpressionType(expressionType))
 			throw new IllegalArgumentException();
 	}
 
 	/**
 	 * An unknown expression type was found in the processing of expression stack. Subclasses can override
 	 * to process new types of expressions. 
 	 * <p>
 	 * Overrides must return <code>true</code> if they processed the expression type. If they return <code>false</code>
 	 * it means they didn't understand it either and we should do default processing for unknow type.
 	 * @param expressionType
 	 * @return <code>true</code> if type was processed, <code>false</code> if not known by subclass either.
 	 * 
 	 * @since 1.0.0
 	 */
 	protected boolean processUnknownExpressionType(int expressionType) {
 		return false;
 	}
 
 	/**
 	 * Create the expression.
 	 * 
 	 * @param registry
 	 * 
 	 * @since 1.0.0
 	 */
 	protected Expression(ProxyFactoryRegistry registry) {
 		this.registry = registry;
 		this.beanProxyFactory = this.registry.getBeanProxyFactory();
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#getRegistry()
 	 */
 	public ProxyFactoryRegistry getRegistry() {
 		return registry;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#invokeExpression()
 	 */
 	public final void invokeExpression() throws ThrowableProxy, IllegalStateException, NoExpressionValueException {
 		try {
 			checkForExpression(ForExpression.ROOTEXPRESSION); // We are at the root.
 			popForExpression();	// Get rid of any intermediate roots.
 			checkForExpression(ForExpression.ROOTEXPRESSION);	// We should be at true root now. We don't have more than one intermediate root pushed in sequence.
 			List proxies = expressionProxies;
 			expressionProxies = null;
 			pushInvoke(processExpressionProxyCallbacks(proxies), proxies);
 		} finally {
 			markInvalid(); // Mark invalid so any new calls after this will fail.
 			close();
 		}
 	}
 		
 	/*
 	 * Process the expression proxy callbacks, if any.
 	 * @return the number of proxies that have callbacks.
 	 */
 	private int processExpressionProxyCallbacks(List proxies) {
 		if (proxies != null) {
 			// Strip list down to only those with callbacks and send on.
 			int proxiesWithCallbacks = 0;
 			for (ListIterator eps = proxies.listIterator(); eps.hasNext();) {
 				ExpressionProxy proxy = (ExpressionProxy) eps.next();
 				if (!proxy.hasListeners())
 					eps.set(null);	// Remove it from the list. No one cares.
 				else
 					proxiesWithCallbacks++;
 			}
 			return proxiesWithCallbacks;
 		}
 		return 0;
 	}
 	
 	/**
 	 * Called to validate this is a valid proxy for this expression. This could happen
 	 * if a proxy from another expression is sent to this expression. If the proxy
 	 * is a bean proxy or is an expression proxy for this expression, then this
 	 * just returns. Else it will throw the {@link IllegalArgumentException}. 
 	 * @param proxy
 	 * @throws IllegalArgumentException if the proxy is an expression proxy for another expression.
 	 * 
 	 * @since 1.1.0.1
 	 */
 	private void validateProxy(IProxy proxy) throws IllegalArgumentException {
 		if (proxy != null && (proxy.isExpressionProxy() && ((ExpressionProxy) proxy).getExpression() != this))
 			throw new IllegalArgumentException(ProxyMessages.Expression_InvalidProxy);
 	}
 		
 	/**
 	 * Called by subclass to fill in the value of an expression proxy. See {@link Expression#pullProxyValue(int, List))} for an example of who would call it.
 	 * @param ep
 	 * @param beanproxy
 	 * 
 	 * @since 1.1.0
 	 */
 	protected void fireProxyResolved(ExpressionProxy ep, IBeanProxy beanproxy) {
 		ep.fireResolved(beanproxy);
 	}
 	
 	/**
 	 * Called by subclass to fire proxy was not resolved. See {@link Expression#pullProxyValue(int, List))} for an example of who would call it.
 	 * @param ep
 	 * 
 	 * @since 1.1.0
 	 */
 	protected void fireProxyNotResolved(ExpressionProxy ep) {
 		ep.fireNotResolved();
 	}
 	
 	/**
 	 * Called by subclass to fire proxy resolved to a void return type. See {@link Expression#pullProxyValue(int, List))} for an example of who would call it.
 	 * @param ep
 	 * 
 	 * @since 1.1.0
 	 */
 	protected void fireProxyVoid(ExpressionProxy ep) {
 		ep.fireVoidResolved();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#getExpressionValue()
 	 */
 	public final IBeanProxy getExpressionValue() throws ThrowableProxy, NoExpressionValueException, IllegalStateException {
 		try {
 			checkForExpression(ForExpression.ROOTEXPRESSION); // We are at the root.
 			popForExpression();	// Get rid of any intermediate roots.
 			checkForExpression(ForExpression.ROOTEXPRESSION);	// We should be at true root now. We don't have more than one intermediate root pushed in sequence.
 			List proxies = expressionProxies;
 			expressionProxies = null;
 			return pullProxyValue(processExpressionProxyCallbacks(proxies), proxies); // Get the top value.
 		} finally {
 			markInvalid();	// Mark invalid so any new calls after this will fail. 
 			close();
 		}
 	}
 
 	
 	/**
 	 * Mark the list of proxies as not resolved. 
 	 * 
 	 * @since 1.1.0
 	 */
 	protected void markAllProxiesNotResolved(List proxies) {
 		if (proxies != null) {
 			for (ListIterator eps = proxies.listIterator(); eps.hasNext();) {
 				ExpressionProxy proxy = (ExpressionProxy) eps.next();
 				if (proxy != null && proxy.hasListeners())
 					fireProxyNotResolved(proxy);
 			}
 		}
 	}
 
 	private int blockNumber = -1;	// Current block number. This is always incrementing.
 	
 	/*
 	 *  (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createBlockBegin()
 	 */
 	public final int createBlockBegin() throws IllegalStateException {
 		try {
 			// Blocks are special, they can be anywhere at root, of could be the true or else clause of an if/else.
 			if (peekForExpression(ForExpression.ROOTEXPRESSION))
 				checkForExpression(ForExpression.ROOTEXPRESSION);
 			else if (peekForExpression(ForExpression.IF_TRUE))
 				checkForExpression(ForExpression.IF_TRUE);
 			else
 				checkForExpression(ForExpression.IF_ELSE);
 			
 			pushForExpression(PROCESS_EXPRESSION);
 			pushForExpression(BLOCKEND_EXPRESSION);
 			pushForExpression(ForExpression.ROOTEXPRESSION);
 
 			pushBlockBeginToProxy(++blockNumber);
 			push(new Integer(blockNumber));
 			push(InternalExpressionTypes.BLOCK_END_EXPRESSION);
 			processExpression();
 			return blockNumber;
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createBlockBreak(int)
 	 */
 	public final void createBlockBreak(int blockNumber) throws IllegalStateException {
 		try {
 			checkForExpression(ForExpression.ROOTEXPRESSION);
 			pushBlockBreakToProxy(blockNumber);
 			processExpression();
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 
 	/*
 	 *  (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createBlockEnd()
 	 */
 	public final void createBlockEnd() throws IllegalStateException {
 		try {
 			checkForExpression(ForExpression.ROOTEXPRESSION);
 			popForExpression(); // Remove the root expression since block is done.
 			checkForExpression(BLOCKEND_EXPRESSION); // This needs to be next for it to be valid.
 			processExpression(); // Now let it handle the previously pushed end block, containing the block number being ended.
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createArrayAccess(int, int)
 	 */
 	public final void createArrayAccess(ForExpression forExpression, int indexCount) {
 		try {
 			checkForExpression(forExpression);
 			pushForExpression(PROCESS_EXPRESSION);
 			int i = indexCount;
 			while (i-- > 0)
 				pushForExpression(ForExpression.ARRAYACCESS_INDEX);
 			pushForExpression(ForExpression.ARRAYACCESS_ARRAY);
 
 			push(indexCount == 1 ? ARRAYACCESS_INDEX_1 : new Integer(indexCount));
 			push(InternalExpressionTypes.ARRAY_ACCESS_EXPRESSION);
 			processExpression(); // See if previous expression is ready for processing.
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}		
 	}
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createArrayCreation(int, java.lang.String, int)
 	 */
 	public final void createArrayCreation(ForExpression forExpression, String type, int dimensionExpressionCount)
 		throws IllegalStateException {
 		pushArrayCreation(forExpression, getProxyBeanType(type), dimensionExpressionCount);
 	}
 
 	/*
 	 *  (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createArrayCreation(org.eclipse.jem.internal.proxy.initParser.tree.ForExpression, org.eclipse.jem.internal.proxy.core.IProxyBeanType, int)
 	 */
 	public final void createArrayCreation(ForExpression forExpression, IProxyBeanType type, int dimensionExpressionCount)
 		throws IllegalStateException, IllegalArgumentException {
 		pushArrayCreation(forExpression, type, dimensionExpressionCount);
 	}
 
 	private void pushArrayCreation(ForExpression forExpression, IProxyBeanType type, int dimensionExpressionCount) throws IllegalStateException, IllegalArgumentException {
 		try {
 			checkForExpression(forExpression);
 			validateProxy(type);
 			switch (dimensionExpressionCount) {
 				case 0:
 					push(ARRAY_CREATION_DIMENSION_0);
 					break;
 				case 1:
 					push(ARRAY_CREATION_DIMENSION_1);
 					break;
 				default:
 					push(new Integer(dimensionExpressionCount));
 					break;
 			}
 			push(type);
 			push(InternalExpressionTypes.ARRAY_CREATION_EXPRESSION);
 
 			pushForExpression(PROCESS_EXPRESSION);
 			if (dimensionExpressionCount == 0)
 				pushForExpression(ARRAY_INITIALIZER);
 			else {
 				while (dimensionExpressionCount-- > 0)
 					pushForExpression(ForExpression.ARRAYCREATION_DIMENSION);
 			}
 			processExpression(); // See if previous expression is ready for processing.		
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createArrayInitializer(int)
 	 */
 	public final void createArrayInitializer(int expressionCount) throws IllegalStateException {
 		try {
 			// This is special, we could be waiting for an array initializer or an array initializer expression.
 			// We will peek to see what it is and handle it.
 			if (peekForExpression(ARRAY_INITIALIZER))
 				checkForExpression(ARRAY_INITIALIZER);
 			else
 				checkForExpression(ForExpression.ARRAYINITIALIZER_EXPRESSION);
 
 			// At this point in time that stack may either have:
 			// array_type, array_creation
 			// strip_count, array_type, array_initializer
 			// So we can get the array type from peek(2), and get the command type from peek(1).
 			// Then if the command type is array_creation, strip_count will be inited to 0, while
 			// else it will be inited to peek(3). From that we can increment the strip_count to
 			// use for this initializer.
 			//
 			// We need to peek here because we will be adding various pushes to the stack and we
 			// need to get the info while it is still at the top of the stack.
 			Object arrayType = peek(2); 
 			int stripCount = 0;
 			if (peek(1) == InternalExpressionTypes.ARRAY_INITIALIZER_EXPRESSION)
 				stripCount = ((Integer) peek(3)).intValue();
 
 			switch (expressionCount) {
 				case 0:
 					push(ARRAYINITIALIZER_COUNT_0);
 					break;
 				case 1:
 					push(ARRAYINITIALIZER_COUNT_1);
 					break;
 				case 2:
 					push(ARRAYINITIALIZER_COUNT_2);
 					break;
 				default:
 					push(new Integer(expressionCount));
 					break;
 			}
 
 			if (arrayType instanceof String) {
 				String at = (String) arrayType;
 				int i = at.lastIndexOf("[]"); //$NON-NLS-1$
 				if (i == -1)
 					throw new IllegalArgumentException(MessageFormat.format(
 							ProxyMessages.Expression_ArrayTypeNotAnArray_EXC_, new Object[] { arrayType})); 
 				arrayType = getProxyBeanType(at);
 			} else if (!(arrayType instanceof IProxyBeanType)) {
 				throw new IllegalArgumentException(MessageFormat.format(
 						ProxyMessages.Expression_ArrayTypeNotAnArray_EXC_, new Object[] { arrayType})); 
 			}
 			push(new Integer(++stripCount));
 			push(arrayType);
 			push(InternalExpressionTypes.ARRAY_INITIALIZER_EXPRESSION);
 
 			pushForExpression(PROCESS_EXPRESSION);
 			while (expressionCount-- > 0)
 				pushForExpression(ForExpression.ARRAYINITIALIZER_EXPRESSION);
 
 			processExpression(); 
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
  
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createCastExpression(int, java.lang.String)
 	 * A cast expression has one nested expression.
 	 */
 	public final void createCastExpression(ForExpression forExpression, String type) throws IllegalStateException {
 		pushCast(forExpression, getProxyBeanType(type)); // Push this onto the local stack to wait for completion.
 	}
 
 	/*
 	 *  (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createCastExpression(org.eclipse.jem.internal.proxy.initParser.tree.ForExpression, org.eclipse.jem.internal.proxy.core.IProxyBeanType)
 	 */
 	public final void createCastExpression(ForExpression forExpression, IProxyBeanType type) throws IllegalStateException, IllegalArgumentException {
 		pushCast(forExpression, type); // Push this onto the local stack to wait for completion.
 	}
 	
 	/*
 	 * Push for a cast.
 	 */
 	private void pushCast(ForExpression forExpression, IProxyBeanType type) throws IllegalStateException, IllegalArgumentException {
 		try {
 			checkForExpression(forExpression);
 			validateProxy(type);
 			push(type);
 			push(InternalExpressionTypes.CAST_EXPRESSION);
 			pushForExpression(PROCESS_EXPRESSION);
 			pushForExpression(ForExpression.CAST_EXPRESSION); // The next expression must be for the cast expression.
 			processExpression(); 
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}	
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createClassInstanceCreation(int, java.lang.String, int)
 	 */
 	public final void createClassInstanceCreation(ForExpression forExpression, String type, int argumentCount)
 		throws IllegalStateException {
 		pushClassInstanceCreation(forExpression, getProxyBeanType(type), argumentCount);	// Push this onto the local stack to wait for completion.
 	}
 
 	/*
 	 *  (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createClassInstanceCreation(org.eclipse.jem.internal.proxy.initParser.tree.ForExpression, org.eclipse.jem.internal.proxy.core.IProxyBeanType, int)
 	 */
 	public final void createClassInstanceCreation(ForExpression forExpression, IProxyBeanType type, int argumentCount)
 		throws IllegalStateException, IllegalArgumentException {
 		pushClassInstanceCreation(forExpression, type, argumentCount);	// Push this onto the local stack to wait for completion.
 	}
 
 	/*
 	 * Push for a class instance creation
 	 */
 	private void pushClassInstanceCreation(ForExpression forExpression, IProxyBeanType type, int argumentCount) throws IllegalStateException, IllegalArgumentException {
 		try {
 			checkForExpression(forExpression);
 			validateProxy(type);
 			switch (argumentCount) {
 				case 0:
 					push(CLASS_INSTANCE_CREATION_ARGUMENTS_0);
 					break;
 				case 1:
 					push(CLASS_INSTANCE_CREATION_ARGUMENTS_1);
 					break;
 				default:
 					push(new Integer(argumentCount));
 					break;
 			}
 			push(type);
 			push(InternalExpressionTypes.CLASS_INSTANCE_CREATION_EXPRESSION);
 
 			pushForExpression(PROCESS_EXPRESSION);
 			while (argumentCount-- > 0)
 				pushForExpression(ForExpression.CLASSINSTANCECREATION_ARGUMENT);
 			processExpression(); // See if previous expression is ready for processing.						
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createConditionalExpression(int)
 	 */
 	public final void createConditionalExpression(ForExpression forExpression) throws IllegalStateException {
 		try {
 			checkForExpression(forExpression);
 			pushForExpression(PROCESS_EXPRESSION);
 			pushForExpression(ForExpression.CONDITIONAL_FALSE);
 			pushForExpression(PROCESS_EXPRESSION);
 			pushForExpression(ForExpression.CONDITIONAL_TRUE);
 			pushForExpression(PROCESS_EXPRESSION);
 			pushForExpression(ForExpression.CONDITIONAL_CONDITION);
 
 			push(InternalConditionalOperandType.CONDITIONAL_FALSE);
 			push(InternalExpressionTypes.CONDITIONAL_EXPRESSION);
 			push(InternalConditionalOperandType.CONDITIONAL_TRUE);
 			push(InternalExpressionTypes.CONDITIONAL_EXPRESSION);
 			push(InternalConditionalOperandType.CONDITIONAL_TEST);
 			push(InternalExpressionTypes.CONDITIONAL_EXPRESSION);
 			processExpression();
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createFieldAccess(int, java.lang.String, boolean)
 	 */
 	public final void createFieldAccess(ForExpression forExpression, String fieldName, boolean hasReceiver) throws IllegalStateException, IllegalArgumentException {
 		try {
 			// Only for string fieldnames is this invalid when no receiver because no way to determine receiver. (Don't handle implicit "this" yet for fields). 
 			// For the accessor that takes a IFieldProxy we can get away with no receiver because the field proxy can determine if static or not, and if not
 			// static it will fail at evaluation time.
 			if (!hasReceiver)
 				throw new IllegalArgumentException(MessageFormat.format(
 						ProxyMessages.Expression_CannotHandleNoReceiveOnFieldAccess_EXC_, new Object[] { fieldName})); 
 			pushFieldAccess(forExpression, fieldName, hasReceiver);
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createIfElse(boolean)
 	 */
 	public final void createIfElse(boolean hasElseClause) throws IllegalStateException {
 		try {
 			checkForExpression(ForExpression.ROOTEXPRESSION);
 			
 			pushForExpression(PROCESS_EXPRESSION);
 			if (hasElseClause) {
 				pushForExpression(ForExpression.IF_ELSE);
 			}
 			pushForExpression(PROCESS_EXPRESSION);
 			pushForExpression(ForExpression.IF_TRUE);
 			pushForExpression(PROCESS_EXPRESSION);
 			pushForExpression(ForExpression.IF_CONDITION);
 
 			// We still push an else clause so that we know when finished. We don't have a pushForExpression for it because there
 			// won't be any. But the else clause processing will be on the push stack so that we can clean up when end of if stmt occurs.
 			push(InternalIfElseOperandType.ELSE_CLAUSE);	
 			push(InternalExpressionTypes.IF_ELSE_EXPRESSION);
 			
 			push(InternalIfElseOperandType.TRUE_CLAUSE);
 			push(InternalExpressionTypes.IF_ELSE_EXPRESSION);
 			push(InternalExpressionTypes.IF_TEST_EXPRESSION);
 			processExpression();
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 
 	/*
 	 * Push the field access.
 	 * @param forExpression
 	 * @param field String if field name, or IProxyField.
 	 * @param hasReceiver
 	 * @throws IllegalAccessException
 	 * 
 	 * @since 1.1.0
 	 */
 	private void pushFieldAccess(ForExpression forExpression, Object field, boolean hasReceiver) throws IllegalStateException {
 		try {
 			checkForExpression(forExpression);
 			push(hasReceiver ? Boolean.TRUE : Boolean.FALSE); // We have a receiver
 			push(field);
 			push(InternalExpressionTypes.FIELD_ACCESS_EXPRESSION);
 
 			pushForExpression(PROCESS_EXPRESSION);
 			if (hasReceiver)
 				pushForExpression(ForExpression.FIELD_RECEIVER);
 			processExpression(); // See if previous expression is ready for processing.
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createInfixExpression(int, int, int)
 	 */
 	public final void createInfixExpression(ForExpression forExpression, InfixOperator operator, int extendedOperandCount) throws IllegalStateException {
 		try {
 			checkForExpression(forExpression);
 			push(InternalInfixOperandType.INFIX_LAST_OPERAND);
 			push(operator);
 			push(InternalExpressionTypes.INFIX_EXPRESSION);
 			int i = extendedOperandCount;
 			while (i-- > 0) {
 				push(InternalInfixOperandType.INFIX_OTHER_OPERAND);
 				push(operator);
 				push(InternalExpressionTypes.INFIX_EXPRESSION);
 			}
 			push(InternalInfixOperandType.INFIX_LEFT_OPERAND);
 			push(operator);
 			push(InternalExpressionTypes.INFIX_EXPRESSION);
 
 			i = extendedOperandCount;
 			while (i-- > 0) {
 				pushForExpression(PROCESS_EXPRESSION);
 				pushForExpression(ForExpression.INFIX_EXTENDED);
 			}
 			pushForExpression(PROCESS_EXPRESSION);
 			pushForExpression(ForExpression.INFIX_RIGHT);
 			pushForExpression(PROCESS_EXPRESSION);
 			pushForExpression(ForExpression.INFIX_LEFT);
 			processExpression(); // See if previous expression is ready for processing.
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createInstanceofExpression(int, java.lang.String)
 	 */
 	public final void createInstanceofExpression(ForExpression forExpression, String type) throws IllegalStateException {
 		pushInstanceof(forExpression, getProxyBeanType(type));	// Push this onto the local stack to wait for completion.
 	}
 
 	/*
 	 *  (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createInstanceofExpression(org.eclipse.jem.internal.proxy.initParser.tree.ForExpression, org.eclipse.jem.internal.proxy.core.IProxyBeanType)
 	 */
 	public final void createInstanceofExpression(ForExpression forExpression, IProxyBeanType type) throws IllegalStateException, IllegalArgumentException {
 		pushInstanceof(forExpression, type);	// Push this onto the local stack to wait for completion.
 	}
 	
 	/*
 	 * Push for a cast.
 	 */
 	private void pushInstanceof(ForExpression forExpression, IProxyBeanType type) throws IllegalStateException, IllegalArgumentException {
 		try {
 			checkForExpression(forExpression);
 			validateProxy(type);
 			push(type);
 			push(InternalExpressionTypes.INSTANCEOF_EXPRESSION);
 			pushForExpression(PROCESS_EXPRESSION);
 			pushForExpression(ForExpression.INSTANCEOF_VALUE); // The next expression must be for the instance of expression.
 			processExpression(); 
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createMethodInvocation(int, java.lang.String, boolean, int)
 	 */
 	public final void createMethodInvocation(ForExpression forExpression, String name, boolean hasReceiver, int argumentCount)
 		throws IllegalStateException, IllegalArgumentException {
 		try {
 			// Only for string methodnames is this invalid when no receiver because no way to determine receiver. (Don't handle implicit "this" yet for methods). 
 			// For the accessor that takes a IFieldProxy we can get away with no receiver because the field proxy can determine if static or not, and if not
 			// static it will fail at evaluation time.
 			if (!hasReceiver)
 				throw new IllegalArgumentException(MessageFormat.format(
 						ProxyMessages.Expression_MethodsNeedReceiver_EXC_, new Object[] { name})); 
 
 			pushMethodInvocation(forExpression, name, hasReceiver, argumentCount);
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 	
 	/**
 	 * @param forExpression
 	 * @param method String for method name, IMethodProxy otherwise.
 	 * @param hasReceiver 
 	 * @param argumentCount
 	 * @throws ThrowableProxy
 	 * @throws NoExpressionValueException
 	 * 
 	 * @since 1.1.0
 	 */
 	private void pushMethodInvocation(ForExpression forExpression, Object method, boolean hasReceiver, int argumentCount) throws IllegalArgumentException, IllegalStateException {
 		try {
 			checkForExpression(forExpression);
 			switch (argumentCount) {
 				case 0:
 					push(METHOD_ARGUMENTS_0);
 					break;
 				case 1:
 					push(METHOD_ARGUMENTS_1);
 					break;
 				default:
 					push(new Integer(argumentCount));
 					break;
 			}
 			push(hasReceiver ? Boolean.TRUE : Boolean.FALSE);
 			push(method);
 			push(InternalExpressionTypes.METHOD_EXPRESSION);
 
 			pushForExpression(PROCESS_EXPRESSION);
 			while (argumentCount-- > 0)
 				pushForExpression(ForExpression.METHOD_ARGUMENT);
 			if (hasReceiver)
 				pushForExpression(ForExpression.METHOD_RECEIVER);
 			processExpression(); // See if previous expression is ready for processing.
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 
 	/*
 	 *  (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createPrefixExpression(int, org.eclipse.jem.internal.proxy.initParser.tree.PrefixOperator)
 	 */
 	public final void createPrefixExpression(ForExpression forExpression, PrefixOperator operator) throws IllegalStateException {
 		try {
 			checkForExpression(forExpression);
 			push(operator);
 			push(InternalExpressionTypes.PREFIX_EXPRESSION);
 			
 			pushForExpression(PROCESS_EXPRESSION);
 			pushForExpression(ForExpression.PREFIX_OPERAND);
 			processExpression();	// See if previous expression is ready for processing.
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 	
 	/**
 	 * Create a new instance using the initialization string. The result must be compatible with the
 	 * given type. This is not on the IExpression interface because it is not for use of regular
 	 * customers. It is here for the allocation processer to create entries that are just strings.
 	 * <p>
 	 * This is not customer advanced API. This API for the implementers of registries and expression subclasses.
 	 * 
 	 * @param forExpression
 	 * @param initializationString
 	 * @param type
 	 * @throws IllegalStateException
 	 * @throws IllegalArgumentException
 	 * 
 	 * @since 1.1.0
 	 */
 	public final void createNewInstance(ForExpression forExpression, String initializationString, IProxyBeanType type) throws IllegalStateException, IllegalArgumentException{
 		try {
 			checkForExpression(forExpression);
 			validateProxy(type);
 			pushNewInstanceToProxy(initializationString, type);
 			processExpression();
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createNull(int)
 	 */
 	public final void createNull(ForExpression forExpression) throws IllegalStateException {
 		try {
 			checkForExpression(forExpression);
 			pushToProxy(null);
 			processExpression();	// See if previous expression is ready for processing.
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createTypeLiteral(int, java.lang.String)
 	 */
 	public final void createTypeLiteral(ForExpression forExpression, String type) throws IllegalStateException {
 		createProxyExpression(forExpression, getProxyBeanType(type));
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createTypeReceiver(java.lang.String)
 	 */
 	public final void createTypeReceiver(String type) throws IllegalStateException {
 		pushTypeReceiver(getProxyBeanType(type));
 	}
 
 	/*
 	 *  (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createTypeReceiver(org.eclipse.jem.internal.proxy.core.IProxyBeanType)
 	 */
 	public final void createTypeReceiver(IProxyBeanType type) throws IllegalStateException, IllegalArgumentException {
 		validateProxy(type);
 		pushTypeReceiver(type);
 	}
 
 	/*
 	 * Push for a type receiver.
 	 * @param type
 	 * 
 	 * @since 1.0.0
 	 */
 	private void pushTypeReceiver(IProxyBeanType type) throws IllegalStateException {
 		try {
 			// This is special because type receivers are only valid as the receiver for a field access or a method access.
 			// Since each has a different forExpression we need to test for one or the other. It doesn't make any difference
 			// which one it is, but it must be one or the other.
 			if (peekForExpression(ForExpression.FIELD_RECEIVER))
 				checkForExpression(ForExpression.FIELD_RECEIVER);
 			else
 				checkForExpression(ForExpression.METHOD_RECEIVER);
 			
 			pushTypeReceiverToProxy(type);
 			processExpression();	// See if previous expression is ready for processing.
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}			
 	}
 	
 	/*
 	 * For all of the primitive types we will be creating a IBeanProxy for them. That is because that
 	 * would be the expected result of the expression, and no need to get the other side involved.
 	 */
 
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createPrimitiveLiteral(int, boolean)
 	 */
 	public final void createPrimitiveLiteral(ForExpression forExpression, boolean value) throws IllegalStateException {
 		try {
 			checkForExpression(forExpression);
 			pushToProxy(beanProxyFactory.createBeanProxyWith(value));
 			processExpression();
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createPrimitiveLiteral(int, char)
 	 */
 	public final void createPrimitiveLiteral(ForExpression forExpression, char value) throws IllegalStateException {
 		try {
 			checkForExpression(forExpression);
 			pushToProxy(beanProxyFactory.createBeanProxyWith(value));
 			processExpression();
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createPrimitiveLiteral(int, byte)
 	 */
 	public final void createPrimitiveLiteral(ForExpression forExpression, byte value) throws IllegalStateException {
 		try {
 			checkForExpression(forExpression);
 			pushToProxy(beanProxyFactory.createBeanProxyWith(value));
 			processExpression();
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createPrimitiveLiteral(int, double)
 	 */
 	public final void createPrimitiveLiteral(ForExpression forExpression, double value) throws IllegalStateException {
 		try {
 			checkForExpression(forExpression);
 			pushToProxy(beanProxyFactory.createBeanProxyWith(value));
 			processExpression();
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createPrimitiveLiteral(int, float)
 	 */
 	public final void createPrimitiveLiteral(ForExpression forExpression, float value) throws IllegalStateException {
 		try {
 			checkForExpression(forExpression);
 			pushToProxy(beanProxyFactory.createBeanProxyWith(value));
 			processExpression();
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createPrimitiveLiteral(int, int)
 	 */
 	public final void createPrimitiveLiteral(ForExpression forExpression, int value) throws IllegalStateException {
 		try {
 			checkForExpression(forExpression);
 			pushToProxy(beanProxyFactory.createBeanProxyWith(value));
 			processExpression();
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createPrimitiveLiteral(int, long)
 	 */
 	public final void createPrimitiveLiteral(ForExpression forExpression, long value) throws IllegalStateException {
 		try {
 			checkForExpression(forExpression);
 			pushToProxy(beanProxyFactory.createBeanProxyWith(value));
 			processExpression();
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createPrimitiveLiteral(int, short)
 	 */
 	public final void createPrimitiveLiteral(ForExpression forExpression, short value) throws IllegalStateException {
 		try {
 			checkForExpression(forExpression);
 			pushToProxy(beanProxyFactory.createBeanProxyWith(value));
 			processExpression();
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createStringLiteral(int, java.lang.String)
 	 */
 	public final void createStringLiteral(ForExpression forExpression, String value) throws IllegalStateException {
 		try {
 			checkForExpression(forExpression);
 			pushToProxy(beanProxyFactory.createBeanProxyWith(value));
 			processExpression();
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 
 	/*
 	 *  (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createProxyExpression(int, org.eclipse.jem.internal.proxy.core.IProxy)
 	 */
 	public final void createProxyExpression(ForExpression forExpression, IProxy proxy) throws IllegalStateException, IllegalArgumentException {
 		try {
 			checkForExpression(forExpression);
 			validateProxy(proxy);
 			pushToProxy(proxy);
 			processExpression();
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createAssignmentExpression(int)
 	 */
 	public final void createAssignmentExpression(ForExpression forExpression) throws IllegalStateException {
 		try {
 			checkForExpression(forExpression);
 			push(InternalExpressionTypes.ASSIGNMENT_EXPRESSION);
 			
 			pushForExpression(PROCESS_EXPRESSION);
 			pushForExpression(ForExpression.ASSIGNMENT_RIGHT);
 			pushForExpression(ForExpression.ASSIGNMENT_LEFT);
 			processExpression();	// See if previous expression is ready for processing.
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 
 	/*
 	 *  (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createAssignmentExpression(int)
 	 */
 	public final ExpressionProxy createProxyAssignmentExpression(ForExpression forExpression) throws IllegalStateException {
 		try {
 			checkForExpression(forExpression);
 			ExpressionProxy proxy = allocateExpressionProxy(NORMAL_EXPRESSION_PROXY);
 			push(proxy);
 			push(InternalExpressionTypes.ASSIGNMENT_PROXY_EXPRESSION);
 
 			pushForExpression(PROCESS_EXPRESSION);
 			pushForExpression(ForExpression.ASSIGNMENT_RIGHT);
 			processExpression(); // See if previous expression is ready for processing.
 			return proxy;
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 	
 	/**
 	 * Called by registries to create an expression proxy for a bean type. It is not in the interface because it should
 	 * only be called by the proxy registry to create an expression proxy. It shouldn't be called outside of the registries
 	 * because there may already exist in the registry the true IBeanTypeProxy, and that one should be used instead.
 	 * <p>
 	 * This is not customer advanced API. This API for the implementers of registries and expression subclasses.
 	 * 
 	 * @param typeName
 	 * @return expression proxy that is hooked up and will notify when resolved. It can be called at any time. The resolution will occur at this point in the
 	 * execution stack, but since it will not interfere with the stack this is OK, other than it could throw a ClassNotFoundException on the
 	 * execution.
 	 * 
 	 * @since 1.1.0
 	 */
 	public final IProxyBeanType createBeanTypeExpressionProxy(String typeName) {
 		IBeanTypeExpressionProxy proxy = (IBeanTypeExpressionProxy) allocateExpressionProxy(BEANTYPE_EXPRESSION_PROXY);
 		proxy.setTypeName(typeName);
 		// This can be sent at any time. It doesn't matter what is on the expression stack. It will be sent to be resolved immediately.
 		pushBeanTypeToProxy(proxy);
 		return proxy;
 	}
 
 	/**
 	 * Called by registries to create an expression proxy for a method. It is not in the interface because it should
 	 * only be called by the proxy registry to create an expression proxy. It shouldn't be called outside of the registries
 	 * because there may already exist in the registry the true IMethodProxy, and that one should be used instead.
 	 * <p>
 	 * This is not customer advanced API. This API for the implementers of registries and expression subclasses.
 	 * 
 	 * @param declaringType
 	 * @param methodName
 	 * @param parameterTypes parameter types or <code>null</code> if no parameter types.
 	 * @return
 	 * 
 	 * @throws IllegalArgumentException
 	 * @since 1.1.0
 	 */
 	public final IProxyMethod createMethodExpressionProxy(IProxyBeanType declaringType, String methodName, IProxyBeanType[] parameterTypes) throws IllegalArgumentException{
 		validateProxy(declaringType);
 		if (parameterTypes != null && parameterTypes.length > 0) {
 			for (int i = 0; i < parameterTypes.length; i++) {
 				validateProxy(parameterTypes[i]);
 			}
 		}
 		ExpressionProxy proxy = allocateExpressionProxy(METHOD_EXPRESSION_PROXY);
 		// This can be sent at any time. It doesn't matter what is on the expression stack. It will be sent to be resolved immediately.
 		pushMethodToProxy(proxy, declaringType, methodName, parameterTypes);
 		return (IProxyMethod) proxy;
 	}
 	
 	/**
 	 * Called by registries to create an expression proxy for a field. It is not in the interface because it should
 	 * only be called by the proxy registry to create an expression proxy. It shouldn't be called outside of the registries
 	 * because there may already exist in the registry the true IFieldProxy, and that one should be used instead.
 	 * <p>
 	 * This is not customer advanced API. This API for the implementers of registries and expression subclasses.
 	 * 
 	 * @param declaringType
 	 * @param fieldName
 	 * @return
 	 * 
 	 * @throws IllegalArgumentException
 	 * @since 1.1.0
 	 */
 	public final IProxyField createFieldExpressionProxy(IProxyBeanType declaringType, String fieldName) throws IllegalArgumentException {
 		validateProxy(declaringType);
 		ExpressionProxy proxy = allocateExpressionProxy(FIELD_EXPRESSION_PROXY);
 		// This can be sent at any time. It doesn't matter what is on the expression stack. It will be sent to be resolved immediately.
 		pushFieldToProxy(proxy, declaringType, fieldName);
 		return (IProxyField) proxy;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createProxyReassignmentExpression(org.eclipse.jem.internal.proxy.initParser.tree.ForExpression, org.eclipse.jem.internal.proxy.core.ExpressionProxy)
 	 */
 	public final void createProxyReassignmentExpression(ForExpression forExpression, ExpressionProxy proxy) throws IllegalStateException, IllegalArgumentException {
 		try {
 			checkForExpression(forExpression);
 			if (!proxy.isValidForReassignment())
 				throw new IllegalArgumentException(MessageFormat.format(ProxyMessages.Expression_CreateProxyReassignmentExpression_InvalidForReassignment_EXC_, new Object[]{proxy.toString()})); 
 			push(proxy);
 			push(InternalExpressionTypes.ASSIGNMENT_PROXY_EXPRESSION);
 
 			pushForExpression(PROCESS_EXPRESSION);
 			pushForExpression(ForExpression.ASSIGNMENT_RIGHT);
 			processExpression(); // See if previous expression is ready for processing.
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 		
 	protected static final int NORMAL_EXPRESSION_PROXY = 0;
 	protected static final int BEANTYPE_EXPRESSION_PROXY = 1;
 	protected static final int METHOD_EXPRESSION_PROXY = 2;
 	protected static final int FIELD_EXPRESSION_PROXY = 3;
 	/**
 	 * Allocate a new ExpressionProxy
 	 * @return new ExpressionProxy.
 	 * 
 	 * @since 1.1.0
 	 */
 	protected final ExpressionProxy allocateExpressionProxy(int proxyType) {
 		if (expressionProxies == null)
 			expressionProxies = new ArrayList();
 		// It is very important that this always creates a proxy id that is greater than all previous. This is
 		// so that it can be assured that proxies will be resolved in order of creation.
 		// Currently this is done here by using expressionProxies.size().
 		ExpressionProxy proxy = createExpressionProxy(proxyType, expressionProxies.size());
 		expressionProxies.add(proxy);
 		return proxy;
 	}
 
 	/*
 	 *  (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createFieldAccess(org.eclipse.jem.internal.proxy.initParser.tree.ForExpression, org.eclipse.jem.internal.proxy.core.IProxyField, boolean)
 	 */
 	public final void createFieldAccess(ForExpression forExpression, IProxyField fieldProxy, boolean hasReceiver) throws IllegalStateException, IllegalArgumentException {
 		validateProxy(fieldProxy);
 		pushFieldAccess(forExpression, fieldProxy, hasReceiver);
 	}
 	
 	/*
 	 *  (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createMethodInvocation(org.eclipse.jem.internal.proxy.initParser.tree.ForExpression, org.eclipse.jem.internal.proxy.core.IProxyMethod, boolean, int)
 	 */
 	public final void createMethodInvocation(ForExpression forExpression, IProxyMethod methodProxy, boolean hasReceiver, int argumentCount) throws IllegalArgumentException,
 			IllegalStateException {
 		validateProxy(methodProxy);
 		pushMethodInvocation(forExpression, methodProxy, hasReceiver, argumentCount);
 	}
 	
 	/*
 	 *  (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createSimpleFieldAccess(org.eclipse.jem.internal.proxy.core.IProxyField, org.eclipse.jem.internal.proxy.core.IProxy)
 	 */
 	public final ExpressionProxy createSimpleFieldAccess(IProxyField field, IProxy receiver) throws IllegalStateException, IllegalArgumentException {
 		validateProxy(field);
 		validateProxy(receiver);
 		ExpressionProxy result = createProxyAssignmentExpression(ForExpression.ROOTEXPRESSION);
 		createFieldAccess(ForExpression.ASSIGNMENT_RIGHT, field, receiver != null);
 		if (receiver != null)
 			createProxyExpression(ForExpression.FIELD_RECEIVER, receiver);
 		return result;
 	}
 	
 	/*
 	 *  (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createSimpleFieldSet(org.eclipse.jem.internal.proxy.core.IProxyField, org.eclipse.jem.internal.proxy.core.IProxy, org.eclipse.jem.internal.proxy.core.IProxy, boolean)
 	 */
 	public final ExpressionProxy createSimpleFieldSet(IProxyField field, IProxy receiver, IProxy value, boolean wantResult) throws IllegalStateException, IllegalArgumentException {
 		validateProxy(field);
 		validateProxy(receiver);
 		ExpressionProxy result = null;
 		ForExpression forExpression = ForExpression.ROOTEXPRESSION;
 		if (wantResult) {
 			result = createProxyAssignmentExpression(forExpression);
 			forExpression = ForExpression.ASSIGNMENT_RIGHT;			
 		}		
 		createAssignmentExpression(forExpression);
 		createFieldAccess(ForExpression.ASSIGNMENT_LEFT, field, receiver != null);
 		if (receiver != null)
 			createProxyExpression(ForExpression.FIELD_RECEIVER, receiver);
 		createProxyExpression(ForExpression.ASSIGNMENT_RIGHT, value);
 		return result;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createSimpleMethodInvoke(org.eclipse.jem.internal.proxy.core.IMethodProxy, org.eclipse.jem.internal.proxy.core.IProxy, org.eclipse.jem.internal.proxy.core.IProxy[], boolean)
 	 */
 	public final ExpressionProxy createSimpleMethodInvoke(IProxyMethod method, IProxy receiver, IProxy[] arguments, boolean wantResult)
 			throws IllegalStateException, IllegalArgumentException {
 		validateProxy(method);
 		validateProxy(receiver);
 		if (arguments != null && arguments.length > 0) {
 			for (int i = 0; i < arguments.length; i++) {
 				validateProxy(arguments[i]);
 			}
 		}
 		ForExpression nextExpression = ForExpression.ROOTEXPRESSION;
 		ExpressionProxy result = null;
 		if (wantResult) {
 			result = createProxyAssignmentExpression(nextExpression);
 			nextExpression = ForExpression.ASSIGNMENT_RIGHT;
 		}
 		createMethodInvocation(nextExpression, method, receiver != null, arguments != null ? arguments.length : 0);
 		if (receiver != null)
 			createProxyExpression(ForExpression.METHOD_RECEIVER, receiver);
 		if (arguments != null) {
 			for (int i = 0; i < arguments.length; i++) {
 				createProxyExpression(ForExpression.METHOD_ARGUMENT, arguments[i]);
 			}
 		}
 		return result;
 	}
 	
 	private int subexpressionNumber = -1;	// Current subexpression number. This is always incrementing.
 	
 	public void createSubexpression() throws IllegalStateException {
 		try {
 			// Subexpressions are special, they can be anywhere.
 			pushForExpression(PROCESS_EXPRESSION);
 			pushForExpression(SUBEXPRESSIONEND_EXPRESSION);
 			pushForExpression(ForExpression.ROOTEXPRESSION);
 
 			pushSubexpressionBeginToProxy(++subexpressionNumber);
 			push(new Integer(subexpressionNumber));
 			push(InternalExpressionTypes.SUBEXPRESSION_END_EXPRESSION);
 			processExpression();
 			return;
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 
 	public void createSubexpressionEnd() throws IllegalStateException {
 		try {
 			checkForExpression(ForExpression.ROOTEXPRESSION);
 			popForExpression(); // Remove the root expression since block is done.
 			checkForExpression(SUBEXPRESSIONEND_EXPRESSION); // This needs to be next for it to be valid.
 			processExpression(); // Now let it handle the previously pushed end subexpression, containing the subexpression number being ended.
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 
 	
 	private int tryNumber = -1;	// Current try number. This is always incrementing.
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createTry()
 	 */
 	public final void createTry() throws IllegalStateException {
 		try {
 			checkForExpression(ForExpression.ROOTEXPRESSION);
 			pushForExpression(PROCESS_EXPRESSION); // Set up so that when reached we can process the TRY_END that we've pushed data for later in this method.
 			pushForExpression(TRYEND_EXPRESSION); // Must get a try end before we can process it.
 			pushForExpression(TRYCATCH_EXPRESSION); // Must get a catch/finally clause (or try end, which knows how to handle this).
 			pushForExpression(ForExpression.ROOTEXPRESSION); // Expecting root expressions for the try clause.
 
 			pushTryBeginToProxy(++tryNumber);
 			push(new Integer(tryNumber));
 			push(InternalExpressionTypes.TRY_END_EXPRESSION);
 			processExpression();
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 
 	/*
 	 *  (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createTryCatchClause(org.eclipse.jem.internal.proxy.core.IProxyBeanType, boolean)
 	 */
 	public final ExpressionProxy createTryCatchClause(IProxyBeanType exceptionType, boolean wantExceptionReturned)
 			throws IllegalStateException, IllegalArgumentException {
 		validateProxy(exceptionType);
 		return pushTryCatch(exceptionType, wantExceptionReturned);
 	}
 	
 	/*
 	 *  (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createTryCatchClause(java.lang.String, boolean)
 	 */
 	public final ExpressionProxy createTryCatchClause(String exceptionType, boolean wantExceptionReturned)
 		throws IllegalStateException {
 		return pushTryCatch(getProxyBeanType(exceptionType), wantExceptionReturned);
 	}
 
 	/**
 	 * @param exceptionType
 	 * @param wantExceptionReturned
 	 * @return
 	 * @throws IllegalStateException
 	 * 
 	 * @since 1.1.0
 	 */
 	private ExpressionProxy pushTryCatch(IProxyBeanType exceptionType, boolean wantExceptionReturned) throws IllegalStateException {
 		try {
 			checkForExpression(ForExpression.ROOTEXPRESSION);
 			popForExpression(); // Remove the root expression since try or previous catch clause is done.
 			checkForExpression(TRYCATCH_EXPRESSION); // This needs to be next for it to be valid.
 			pushForExpression(TRYCATCH_EXPRESSION); // Set up for a following catch/finally clause.
 			pushForExpression(ForExpression.ROOTEXPRESSION); // Root expressions are next for the catch clause.
 
 			int tryNumber = ((Integer) peek(2)).intValue(); // Get the try#. It should be in this place on the stack.
 
 			ExpressionProxy ep = null;
 			if (wantExceptionReturned)
 				ep = allocateExpressionProxy(NORMAL_EXPRESSION_PROXY);
 			pushTryCatchClauseToProxy(tryNumber, exceptionType, ep);
 
 			processExpression();
 			return ep;
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createTryEnd()
 	 */
 	public final void createTryEnd() throws IllegalStateException {
 		try {
 			checkForExpression(ForExpression.ROOTEXPRESSION);
 			popForExpression(); // Remove the root expression since try or previous catch clause is done.
 			if (peekForExpression(TRYCATCH_EXPRESSION))
 				checkForExpression(TRYCATCH_EXPRESSION); // This may of been next if no finally clause was added. If a finally clause was added this would not be here.
 			checkForExpression(TRYEND_EXPRESSION); // And this needs to be after that to be valid.
 
 			processExpression();
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createTryFinallyClause()
 	 */
 	public final void createTryFinallyClause() throws IllegalStateException {
 		try {
 			checkForExpression(ForExpression.ROOTEXPRESSION);
 			popForExpression(); // Remove the root expression since try or previous catch clause is done.
 			checkForExpression(TRYCATCH_EXPRESSION); // This needs to be next for it to be valid.
 			pushForExpression(ForExpression.ROOTEXPRESSION); // Root expressions are next for the finally clause.
 
 			int tryNumber = ((Integer) peek(2)).intValue(); // Get the try#. It should be in this place on the stack.
 
 			pushTryFinallyClauseToProxy(tryNumber);
 			processExpression();
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 	
 	
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createRethrow()
 	 */
 	public final void createRethrow() throws IllegalStateException {
 		try {
 			checkForExpression(ForExpression.ROOTEXPRESSION);
 			popForExpression(); // Remove the root expression since try or previous catch clause is done.
 			checkForExpression(TRYCATCH_EXPRESSION); // This needs to be next for it to be valid.
 			// It is in a valid state, so put the catch and root back on so that things work correctly.
 			pushForExpression(TRYCATCH_EXPRESSION);
 			pushForExpression(ForExpression.ROOTEXPRESSION); 
 
 			int tryNumber = ((Integer) peek(2)).intValue(); // Get the try#. It should be in this place on the stack.
 
 			pushRethrowToProxy(tryNumber);
 			processExpression();
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.IExpression#createThrow()
 	 */
 	public final void createThrow() throws IllegalStateException {
 		try {
 			checkForExpression(ForExpression.ROOTEXPRESSION);
 			push(InternalExpressionTypes.THROW_EXPRESSION);
 			pushForExpression(PROCESS_EXPRESSION);
 			pushForExpression(ForExpression.THROW_OPERAND); // The next expression must be for the throw value.
 			processExpression(); 
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 	
 	
 	public final int mark() throws IllegalStateException {
 		try {
 			checkForExpression(ForExpression.ROOTEXPRESSION);
 			++highestMarkID;
 			currentMarkEntry = new MarkEntry();
 			currentMarkEntry.markID = highestMarkID;
 			currentMarkEntry.controlStackPos = controlStack.size() - 1;
 			currentMarkEntry.nextExpressionStackPos = nextForExpressionStackPos;
 			currentMarkEntry.expressionProxiesPos = expressionProxies != null ? expressionProxies.size() - 1 : -1;
 			if (markEntries == null)
 				markEntries = new ArrayList(5);
 			markEntries.add(currentMarkEntry);
 			pushMarkToProxy(highestMarkID);
 			return highestMarkID;
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 	
 	public void endMark(int markNumber) throws IllegalStateException {
 		if (isValid()) {
 			// Can only do a valid end mark if we are at root. If not at root, we fall through and treat as invalid.
 			if (peekForExpression(ForExpression.ROOTEXPRESSION)) {
 				checkForExpression(ForExpression.ROOTEXPRESSION);	// Now remove it if it should be removed. 
 				// If the current mark number is not the same as the incoming mark number, we have improper nesting.
 				if (currentMarkEntry == null || currentMarkEntry.markID != markNumber)
 					throwInvalidMarkNesting();	// We have improper nesting.
 				// We are popping the current mark. Since we are valid, just move up one in the mark stack.
 				MarkEntry me = (MarkEntry) markEntries.remove(markEntries.size()-1);
 				if (!markEntries.isEmpty())
 					currentMarkEntry = (MarkEntry) markEntries.get(markEntries.size()-1);
 				else
 					currentMarkEntry = null;
 				pushEndmarkToProxy(markNumber, false);
 				if (me.controlStackPos != controlStack.size()-1 || me.nextExpressionStackPos != nextForExpressionStackPos)
 					throwInvalidMarkNesting();	// The stacks should be back to the same size at this point for a valid end mark.
 				return;
 			}
 		} 
 		
 		// It was invalid, or became invalid.
 		if (markEntries == null)
 			throwInvalidMarkNesting();	// We have no marks, so this is an invalid end mark.
 		
 		// We are invalid, need to pop to the given markNumber.
 		// Starting from the end we search back to find the entry for the given mark number. We do it
 		// from the end because it is more likely to be closer to the end than to the beginning.
 		for (int i = markEntries.size()-1; i >=0; i--) {
 			MarkEntry me = (MarkEntry) markEntries.get(i);
 			if (me.markID == markNumber) {
 				// Found it.
 				// Trim the control stack down to the size at time of mark. (No easy way to do this other than repeated remove's.
 				// We do it backwards to eliminate repeated shuffling of entries.
 				for (int j = controlStack.size()-1; j > me.controlStackPos; j--) {
 					controlStack.remove(j);
 				}
 				
 				// Trim the expression stack. This is simple, just reset the next entry pointer.
 				nextForExpressionStackPos = me.nextExpressionStackPos;
 				
 				if (expressionProxies != null) {
 					// Now we need to mark all of the expression proxies that occured after the mark as
 					// not resolved (since someone may be listening), and remove them, and reuse the proxies.
 					for (int j = expressionProxies.size()-1; j > me.expressionProxiesPos; j--) {
 						ExpressionProxy proxy = (ExpressionProxy) expressionProxies.remove(j);
 						if (proxy != null && proxy.hasListeners())
 							fireProxyNotResolved(proxy);
 					}
 				}
 				
 				// Now that we know it is valid, we want to remove all of the mark entries above it in the stack
 				// since those are now invalid. We couldn't remove them as we were searching for the entry because
 				// if the entry wasn't found we didn't want to wipe out the probably valid ones.
 				for (int j = markEntries.size()-1; j >= i; j--) {
 					markEntries.remove(j);
 				}
 				
 				if (!markEntries.isEmpty())
 					currentMarkEntry = (MarkEntry) markEntries.get(markEntries.size()-1);
 				else
 					currentMarkEntry = null;					
 				pushEndmarkToProxy(markNumber, true);
 				expressionValid = true;
 				return;
 			} 
 		}
 		throwInvalidMarkNesting();	// The mark number wasn't found, so this is an invalid end mark.
 	}
 	
 	/**
 	 * Begin the transfer of the expression to another thread.
 	 * <p>
 	 * This is used when the expression needs to continue to be built up, but it needs
 	 * to be done on a different thread. The reason for doing something special other
 	 * than just using it on the other thread is that some proxy registries connections are
 	 * tied through the thread. If you switched to another thread the connections would not
 	 * be properly set up.
 	 * This is not on the IExpression interface because even though it is API, it is tricky
 	 * to use and so not exposed to everyone. Users can legitimately cast to Expression and 
 	 * use this as API for advanced use. 
 	 * <p>
 	 * This is used to begin the transfer. It puts it into a state ready for the transfer. Calling this
 	 * method will cause a synchronization of the expression up to the current level. This means
 	 * that it will not return until the expression has been completely processed in the proxy registry
 	 * up to this point. Typically the connection is a pipe where the instructions are just pushed onto
 	 * it and the caller is not held up waiting for the registry to process it. 
 	 * <p>
 	 * Then when the other thread is done, it will call beginTransferThread itself to signal that it is done
 	 * and that the old thread can pick it up. Then the old thread will call transferThread to pick up processing.
 	 * <p>
 	 * It will be:
 	 * <pre><code>
 	 *   ... expression stuff ...
 	 *   expression.beginTransferThread()
 	 *   ... do what is necessary to get to the other thread ...
 	 *   ... on other thread:
 	 *   expression.transferThread();
 	 *   try {
 	 *     ... do your expression stuff on this thread ...
 	 *   } finally {
 	 *     expression.beginTransferThread(); // This is to return it to old thread.
 	 *   }
 	 *   ... tell old thread to pick up ...
 	 *   ... back on old thread:
 	 *   expression.transferThread();
 	 *   ... do more expression stuff ...
 	 *   expression.invokeExpression();
 	 * </code></pre>
 	 * 
 	 * @throws IllegalStateException
 	 * @throws ThrowableProxy Thrown if there was an exception with the remote vm during this request.
 	 * @since 1.1.0
 	 */
 	public final void beginTransferThread() throws IllegalStateException, ThrowableProxy {
 		try {
 			pushForExpression(THREADTRANSFER_EXPRESSION);
 			pushBeginTransferThreadToProxy();
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}
 	}
 		
 	/**
 	 * Transfer the expression to the current thread.
 	 * <p>
 	 * This is called to actually transfer to the current thread. It must be the next call against
 	 * the expression after the beginTransferThread, but on the new thread.
 	 * <p>
 	 * This is not on the IExpression interface because even though it is API, it is tricky
 	 * to use and so not exposed to everyone. Users can legitimately cast to Expression and 
 	 * use this as API for advanced use. 
 	 * @see Expression#beginTransferThread() for a full explanation.
 	 * @throws IllegalStateException
 	 * 
 	 * @since 1.1.0
 	 */
 	public final void transferThread() throws IllegalStateException {
 		try {
 			checkForExpression(THREADTRANSFER_EXPRESSION);
 			pushTransferThreadToProxy();
 		} catch (RuntimeException e) {
 			markInvalid();
 			throw e;
 		}		
 	}
 	
 	
 	/**
 	 * Get the IProxyBeanType for the type string sent in.
 	 * @param type
 	 * @return
 	 * 
 	 * @since 1.1.0
 	 */
 	protected IProxyBeanType getProxyBeanType(String type) {
 		return getRegistry().getBeanTypeProxyFactory().getBeanTypeProxy(this, type);
 	}
 	
 	/**
 	 * Create the expression proxy subclass that is applicable for this kind of processor. 
 	 * @param proxyType type of proxy. {@link Expression#NORMAL_EXPRESSION_PROXY
 	 * @param proxyID the id of the new expression proxy.
 	 * 
 	 * @return
 	 * 
 	 * @since 1.1.0
 	 */
 	protected abstract ExpressionProxy createExpressionProxy(int proxyType, int proxyID);
 	
 	/**
 	 * Push this proxy to the other side. It will simply take the proxy and push it onto
 	 * its evaluation stack. It will be treated as the result of an expression. It's just 
 	 * that the expression was evaluatable on this side (since it is already a proxy).
 	 * 
 	 * @param proxy
 	 * 
 	 * @since 1.0.0
 	 */
 	protected abstract void pushToProxy(IProxy proxy);
 
 	/**
 	 * Tell the other side we are complete. This will always be called after expression evaluation, or
 	 * if expression was prematurely closed.
 	 * <p>
 	 * <b>Note:</b> The implementation must be able to handle multiple invocations, where the first call is a valid close and any
 	 * subsequent call should be ignored.
 	 * 
 	 * @throws ThrowableProxy
 	 * 
 	 * @since 1.0.0
 	 */
 	protected abstract void closeProxy();
 	
 	/**
 	 * Do invoke. This should simply make sure everything is done and throw any pending errors.
 	 * <p>
 	 * <b>Note:</b> The expression proxies MUST be resolved (callbacks called) in the order they are found in the expressionProxies list. This
 	 * is so that the contract is followed that resolution notifications will occur in the order of creation.
 	 * 
 	 * @param proxycount Number of Expression Proxies that need a callback.
 	 * @param list of expression proxies. If proxycount > 0, then process the non-null entries in the list. They will be of type ExpressionProxy.
 	 * @throws ThrowableProxy
 	 * 
 	 * @since 1.0.0
 	 */
 	protected abstract void pushInvoke(int proxycount, List expressionProxies) throws ThrowableProxy, NoExpressionValueException;
 	
 	/**
 	 * Pull the top expression value from the evaluation stack. It will also under
 	 * the covers call closeProxy.  It also must process the expression proxy callbacks. It must do the expression proxy callbacks first, and then
 	 * process the result value. If an error had occured sometime during processing, it should still process the proxy callbacks before throwing
 	 * an exception.
 	 * <p>
 	 * <b>Note:</b> The expression proxies MUST be resolved (callbacks called) in the order they are found in the expressionProxies list. This
 	 * is so that the contract is followed that resolution notifications will occur in the order of creation. Also <b>REQUIRED</b> is that
 	 * the entire list must be processed of proxies must be processed by this call. It cannot do some or none.
 	 * 
 	 * @param proxycount Number of Expression Proxies that need a callback.
 	 * @param list of expression proxies. If proxycount > 0, then process the non-null entries in the list. They will be of type ExpressionProxy.
 	 * @return The top level evaluation stack value.
 	 * @throws ThrowableProxy
 	 * @throws NoExpressionValueException
 	 * 
 	 * @since 1.0.0
 	 */
 	protected abstract IBeanProxy pullProxyValue(int proxycount, List expressionProxies) throws ThrowableProxy, NoExpressionValueException;
 	
 	/**
 	 * Push to proxy the cast expression. The expression to use will be on the top of its evaluation stack.
 	 * The result of the cast expression will be placed onto the evaluation stack.
 	 *  
 	 * @param type Cast type. 
 	 * 
 	 * @since 1.0.0
 	 */
 	protected abstract void pushCastToProxy(IProxyBeanType type);
 
 	/**
 	 * Push to proxy the instanceof expression. The expression to use will be on the top of its evaluation stack.
 	 * The result of the instanceof expression will be placed onto the evaluation stack.
 	 *  
 	 * @param type Instanceof type.
 	 * 
 	 * @since 1.0.0
 	 */
 	protected abstract void pushInstanceofToProxy(IProxyBeanType type);
 	
 	/**
 	 * Push to proxy the infix operation. This is called on the completion of each operand of the expression.
 	 * So it will be called a minimum of two times.
 	 * 
 	 * @param operator The operator.
 	 * @param operandType The operand type. left, other, or last.
 	 * 
 	 * @since 1.0.0
 	 */
 	protected abstract void pushInfixToProxy(InfixOperator operator, InternalInfixOperandType operandType);
 	
 	/**
 	 * Push to proxy the prefix expression. The expression to use will be on top of its evaluation stack.
 	 * The result of the prefix operation will be placed onto the evaluation stack.
 	 * 
 	 * @param operator 
 	 * 
 	 * @see IExpressionConstants#PRE_MINUS
 	 * @since 1.0.0
 	 */
 	protected abstract void pushPrefixToProxy(PrefixOperator operator);	
 
 	
 	/**
 	 * Push to proxy the array access. The result will be placed onto the evaluation stack.
 	 * 
 	 * @param indexCount
 	 * 
 	 * @since 1.0.0
 	 */
 	protected abstract void pushArrayAccessToProxy(int indexCount);
 	
 	/**
 	 * Push to proxy the array creation. The result will be placed onto the evaluation stack.
 	 * @param type The array type. 
 	 * @param dimensionCount
 	 * 
 	 * @since 1.0.0
 	 */
 	protected abstract void pushArrayCreationToProxy(IProxyBeanType type, int dimensionCount);
 	
 	/**
 	 * Push to proxy the array initializer. The resulting array will be placed onto the evaluation stack.
 	 * @param type The array type. (must be an array type).
 	 * @param stripDimCount the number of dimensions that must be stripped from the array type. This is needed
 	 * because the first array initializer needs to be for the component type of the array (array minus one dimension), and
 	 * each initializer after that needs one more dimension stripped off. But since we are working with possible expression
 	 * proxies for "type", we can't create the appropriate component types of the array. So we need to tell the
 	 * processor how many dims to strip from the original type (which is what is sent in on every initializer push, the original type).
 	 * @param expressionCount
 	 * 
 	 * @since 1.0.0
 	 */
 	protected abstract void pushArrayInitializerToProxy(IProxyBeanType type, int stripDimCount, int expressionCount);
 	
 	/**
 	 * Push to proxy the class instance creation. The resulting class instance will be placed onto the evaluation stack.
 	 * 
 	 * @param type Class type. 
 	 * @param argumentCount The number of arguments.
 	 * 
 	 * @since 1.0.0
 	 */
 	protected abstract void pushClassInstanceCreationToProxy(IProxyBeanType type, int argumentCount);
 	
 	/**
 	 * Push to proxy the type receiver. The resulting class will be placed onto the evaluation stack, along with it also
 	 * being the expression type.
 	 * @param type Class type. 
 	 * 
 	 * @since 1.0.0
 	 */
 	protected abstract void pushTypeReceiverToProxy(IProxyBeanType type);
 
 	/**
 	 * Push to proxy the field access. The result value will be placed onto the evaluation stack.
 	 * @param field The name of the field if string, or an IFieldProxy.
 	 * @param hasReceiver Has receiver flag.
 	 * 
 	 * @since 1.0.0
 	 */
 	protected abstract void pushFieldAccessToProxy(Object field, boolean hasReceiver);
 	
 	/**
 	 * Push to proxy the method invocation. The result value will be placed onto the evaluation stack.
 	 * 
 	 * @param method String for method name or IProxyMethod
 	 * @param hasReceiver
 	 * @param argCount
 	 * 
 	 * @since 1.0.0
 	 */
 	protected abstract void pushMethodInvocationToProxy(Object method, boolean hasReceiver, int argCount);
 	
 	/**
 	 * Push to proxy the conditional expression. This will be called on each part of expression. The expression type
 	 * will be the current part (e.g. test, true, false).
 	 * 
 	 * @param expressionType The expression type.
 	 * 
 	 * @since 1.0.0
 	 */
 	protected abstract void pushConditionalToProxy(InternalConditionalOperandType expressionType);
 	
 	/**
 	 * Push to the proxy the expression proxy. Whatever the last expression value is will be assigned to the ExpressionProxy.
 	 * 
 	 * @param proxy
 	 * 
 	 * @since 1.1.0
 	 */
 	protected abstract void pushAssignmentToProxy(ExpressionProxy proxy);
 	
 	/**
 	 * Push the assignment expression. The operands are already on the stack.
 	 * 
 	 * @since 1.1.0
 	 */
 	protected abstract void pushAssignmentToProxy();
 
 	
 	/**
 	 * Push the begin block expression. 
 	 * @param blockNumber 
 	 * 
 	 * @since 1.1.0
 	 */
 	protected abstract void pushBlockBeginToProxy(int blockNumber);
 	
 	/**
 	 * Push the end block expression.
 	 * @param blockNumber
 	 * 
 	 * @since 1.1.0
 	 */
 	protected abstract void pushBlockEndToProxy(int blockNumber);
 
 	/**
 	 * Push the break block expression.
 	 * @param blockNumber
 	 * 
 	 * @since 1.1.0
 	 *
 	 */
 	protected abstract void pushBlockBreakToProxy(int blockNumber);
 	
 	/**
 	 * Push the begin try expression. 
 	 * @param tryNumber 
 	 * 
 	 * @since 1.1.0
 	 */
 	protected abstract void pushTryBeginToProxy(int tryNumber);
 
 	/**
 	 * Push the catch clause to proxy.
 	 * @param tryNumber
 	 * @param exceptionType 
 	 * @param ep ExpressionProxy to be assigned with the exception or <code>null</code> if exception is not to be assigned.
 	 * 
 	 * @since 1.1.0
 	 */
 	protected abstract void pushTryCatchClauseToProxy(int tryNumber, IProxyBeanType exceptionType, ExpressionProxy ep);
 
 	/**
 	 * Push the finally clause to proxy.
 	 * @param tryNumber
 	 * 
 	 * @since 1.1.0
 	 */
 	protected abstract void pushTryFinallyClauseToProxy(int tryNumber);
 
 	/**
 	 * Push try end to proxy.
 	 * @param tryNumber
 	 * 
 	 * @since 1.1.0
 	 */
 	protected abstract void pushTryEndToProxy(int tryNumber);
 	
 	/**
 	 * Push the throw of the exception to proxy.
 	 * @param exception
 	 * 
 	 * @since 1.1.0
 	 */
 	protected abstract void pushThrowToProxy();
 	
 	/**
 	 * Push a rethrow to proxy.
 	 * @param tryNumber
 	 * 
 	 * @since 1.1.0
 	 */
 	protected abstract void pushRethrowToProxy(int tryNumber);
 
 	/**
 	 * Push the BeanType Expression proxy to be resolved on the execution side.
 	 * @param proxy
 	 * 
 	 * @since 1.1.0
 	 */
 	protected abstract void pushBeanTypeToProxy(IBeanTypeExpressionProxy proxy);
 	
 	/**
 	 * Push the Method Expression proxy to be resolved on the execution side.
 	 * @param proxy
 	 * @param declaringType
 	 * @param methodName
 	 * @param parameterTypes parameter types or <code>null</code> if no parameters.
 	 * 
 	 * @since 1.1.0
 	 */
 	protected abstract void pushMethodToProxy(ExpressionProxy proxy, IProxyBeanType declaringType, String methodName, IProxyBeanType[] parameterTypes);
 
 	/**
 	 * Push the Field Expression Proxy to be resolved on the execution side.
 	 * @param proxy
 	 * @param declaringType
 	 * @param fieldName
 	 * 
 	 * @since 1.1.0
 	 */
 	protected abstract void pushFieldToProxy(ExpressionProxy proxy, IProxyBeanType declaringType, String fieldName);
 	
 	/**
 	 * Push the If test condition to proxy.
 	 * 
 	 * @since 1.1.0
 	 */
 	protected abstract void pushIfTestToProxy();
 	
 	/**
 	 * Push a true or else clause to proxy.
 	 * @param clauseType
 	 * 
 	 * @since 1.1.0
 	 */
 	protected abstract void pushIfElseToProxy(InternalIfElseOperandType clauseType);
 	
 	/**
 	 * Push to proxy a new instance using an initialization string.
 	 * @param initializationString
 	 * @param resultType
 	 * 
 	 * @since 1.1.0
 	 */
 	protected abstract void pushNewInstanceToProxy(String initializationString, IProxyBeanType resultType);
 	
 	/**
 	 * Push the mark id to proxy.
 	 * 
 	 * @param markID
 	 * 
 	 * @since 1.1.0
 	 */
 	protected abstract void pushMarkToProxy(int markID);
 	
 	/**
 	 * Push the end mark id to proxy.
 	 * 
 	 * @param markID
 	 * @param restore <code>true</code> if this is a restore due to error, <code>false</code> if this is just a normal end mark.
 	 * 
 	 * @since 1.1.0
 	 */
 	protected abstract void pushEndmarkToProxy(int markID, boolean restore);
 	
 	/**
 	 * Push the begin transfer thread to proxy.
 	 * 
 	 * 
 	 * @since 1.1.0
 	 */
 	protected abstract void pushBeginTransferThreadToProxy() throws ThrowableProxy;
 	
 	/**
 	 * Push the actual transfer to the current thread to proxy.
 	 * 
 	 * 
 	 * @since 1.1.0
 	 */
 	protected abstract void pushTransferThreadToProxy();
 	
 	/**
 	 * Push the subexpression begin to proxy.
 	 * @param subexpressionNumber
 	 * 
 	 * @since 1.1.0
 	 */
 	protected abstract void pushSubexpressionBeginToProxy(int subexpressionNumber);
 	
 	/**
 	 * Push the subexpression end to proxy.
 	 * @param subexpressionNumber
 	 * 
 	 * @since 1.1.0
 	 */
 	protected abstract void pushSubexpressionEndToProxy(int subexpressionNumber);
 }
