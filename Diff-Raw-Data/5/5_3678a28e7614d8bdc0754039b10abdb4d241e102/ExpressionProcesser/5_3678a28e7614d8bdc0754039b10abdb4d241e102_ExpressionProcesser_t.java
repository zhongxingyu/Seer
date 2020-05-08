 /*******************************************************************************
  * Copyright (c) 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  *  $RCSfile: ExpressionProcesser.java,v $
 *  $Revision: 1.16 $  $Date: 2005/07/11 18:04:42 $ 
  */
 package org.eclipse.jem.internal.proxy.initParser.tree;
 
 import java.lang.reflect.*;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jem.internal.proxy.common.AmbiguousMethodException;
 import org.eclipse.jem.internal.proxy.common.MethodHelper;
 import org.eclipse.jem.internal.proxy.initParser.InitializationStringEvaluationException;
 import org.eclipse.jem.internal.proxy.initParser.InitializationStringParser;
  
 /**
  * Expression processing. This does the actual expression processing with the live objects.
  * It is meant to be subclassed only to provide additional expression types. All of the
  * current expressions cannot be overridden. This is because the stack is very sensitive to 
  * call order.
  * 
  * @since 1.0.0
  */
 public class ExpressionProcesser {
 
 	/**
 	 * A variable reference for a field access.
 	 * 
 	 * @since 1.1.0
 	 */
 	protected static class FieldAccessReference extends VariableReference {
 		
 		private final Field field;
 		private final Object receiver;
 
 		/**
 		 * Use this to construct a FieldAccessReference. This will do checks to make sure
 		 * it is valid so that exceptions won't be thrown later when actually dereferenced.
 		 * 
 		 * @param field
 		 * @param receiver
 		 * @return
 		 * @throws IllegalArgumentException
 		 * 
 		 * @since 1.1.0
 		 */
 		public static FieldAccessReference createFieldAccessReference(Field field, Object receiver) throws IllegalArgumentException {
 			// If static, then receiver is ignored.
 			if (!Modifier.isStatic(field.getModifiers())) {
 				if (!field.getDeclaringClass().isInstance(receiver))
 					throw new IllegalArgumentException(MessageFormat.format(InitparserTreeMessages.getString("ExpressionProcesser.CreateFieldAccessReference.FieldsTypesNotMatching_EXC_"), new Object[]{field.getType(), (receiver!=null ? receiver.getClass() : null)})); //$NON-NLS-1$
 			}
 			field.setAccessible(true);	// Make it always accessible. Trust it. 
 			return new FieldAccessReference(field, receiver);
 		}
 		
 		protected FieldAccessReference(Field field, Object receiver) {
 			this.field = field;
 			this.receiver = receiver;
 		}
 		
 		
 		/* (non-Javadoc)
 		 * @see org.eclipse.jem.internal.proxy.initParser.tree.ExpressionProcesser.VariableReference#dereference()
 		 */
 		public Object dereference() {
 			try {
 				return field.get(receiver);
 			} catch (IllegalArgumentException e) {
 				// Shouldn't occur. Already tested for this.
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				// Shouldn't occur. Already tested for this.
 				e.printStackTrace();
 			}
 			return null;
 		}
 		
 		
 		/* (non-Javadoc)
 		 * @see org.eclipse.jem.internal.proxy.initParser.tree.ExpressionProcesser.VariableReference#set(java.lang.Object, java.lang.Class)
 		 */
 		public Object set(Object value, Class type) throws IllegalArgumentException, IllegalAccessException {
 			field.set(receiver, value);
 			return field.get(receiver);	// Just in case some conversion happened. Technically it is not the value set but the retrieved when in an assignment.
 		}
 		
 		
 		/* (non-Javadoc)
 		 * @see java.lang.Object#toString()
 		 */
 		public String toString() {
 			return "FieldAccess{"+field.toString()+"} on "+(receiver != null ? receiver.toString() : "<static access>"); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 	}
 	
 	/**
 	 * A variable reference for an Array access. It will reference only the last indexed entry of the array.
 	 * For example if <code>x[3][4]</code> is the access, then what will be given to this reference will be
 	 * the array entry at x[3][4], not the x array itself.
 	 * 
 	 * @since 1.1.0
 	 */
 	protected static class ArrayAccessReference extends VariableReference {
 		
 		
 		private final Object array;
 		private final int index;
 
 		/**
 		 * Use this to construct an array access reference. This will do checks to make sure
 		 * it is valid so that exceptions won't be thrown later when actually dereferenced.
 		 * 
 		 * @param array
 		 * @param index
 		 * @return
 		 * @throws IllegalArgumentException
 		 * 
 		 * @since 1.1.0
 		 */
 		public static ArrayAccessReference createArrayAccessReference(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
 			int len = Array.getLength(array);
 			if (index < 0 || len <= index)
 				throw new ArrayIndexOutOfBoundsException(MessageFormat.format(InitparserTreeMessages.getString("ExpressionProcesser.CreateArrayAccessReference.OutOfBounds_EXC_"), new Object[]{new Integer(index), new Integer(len)})); //$NON-NLS-1$
 			return new ArrayAccessReference(array, index);
 		}
 		/**
 		 * Construct the reference with the array and the index of the entry being referenced.
 		 * @param array
 		 * @param index
 		 * 
 		 * @since 1.1.0
 		 */
 		protected ArrayAccessReference(Object array, int index) {
 			this.array = array;
 			this.index = index;
 		}
 		
 		
 		/* (non-Javadoc)
 		 * @see org.eclipse.jem.internal.proxy.initParser.tree.VariableReference#dereference()
 		 */
 		public Object dereference() {
 			return Array.get(array, index);
 		}
 		
 		
 		/* (non-Javadoc)
 		 * @see org.eclipse.jem.internal.proxy.initParser.tree.VariableReference#set(java.lang.Object, java.lang.Class)
 		 */
 		public Object set(Object value, Class type) throws IllegalArgumentException {
 			Array.set(array, index, value);
 			return Array.get(array, index);	// In case there was some conversion applied. Technically it is not the value set but the retrieved when in an assignment.
 		}
 		
 		
 		/* (non-Javadoc)
 		 * @see java.lang.Object#toString()
 		 */
 		public String toString() {
 			return "ArrayAccess["+index+"]: "+array.toString(); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 	}
 	
 	/**
 	 * The expression result stack and the expression result type stack.
 	 * The type stack is used to be expected type of the corresponding
 	 * expression result. This is needed for converting to primitives
 	 * and for finding correct method call from the argument types. In
 	 * this case, it is not the true value, but the value expected, e.g.
 	 * <code>Object getObject()</code> returns something of type Object.
 	 * This needs to be maintained so that if it goes into another method
 	 * we don't accidently return a more specific method instead of the
 	 * one that takes Object as an argument. 
 	 * 
 	 * expressionStack has result of the expression.
 	 * expressionTypeStack has the computed type of the expression i.e.
 	 * the type that the expression returns, not the type of the value.
 	 * These can be different because the expression (e.g. method) may
 	 * return an Object, but the expression value will be some specific
 	 * subclass. So the expressionTypeStack would have a <code>java.lang.Object.class</code>
 	 * on it in that case.
 	 * Note: if the expressionStack has a <code>null</code> on it, then the type stack
 	 * may either have a specific type in it, or it may be <code>MethodHelper.NULL_TYPE</code>. It
 	 * would be this if it was explicitly pushed in and not as the
 	 * result of a computation. If the result of a computation, it would have the
 	 * true value.
 	 * Note: if the expressionStack has a <code>Void.type</code> on it, then that
 	 * means the previous expression had no result. This is an error if trying to
 	 * use the expression in another expression.
 	 * 
 	 * @see org.eclipse.jem.internal.proxy.initParser.MethodHelper#NULL_TYPE
 	 */
 	private List expressionStack = new ArrayList(10);
 	private List expressionTypeStack = new ArrayList(10);
 	
 	/**
 	 * List of the expression proxies. The index into the list is the
 	 * same as the expression proxy id. 
 	 */
 	private ArrayList expressionProxies;	// It is array list because we want to call ensureCapacity and that is not available on List.
 	
 	/**
 	 * An error has occurred. At this point all subcommands will simply make sure they flush the input stream
 	 * correctly, but they do not process it.
 	 * 
 	 * @since 1.0.0
 	 */
 	private boolean errorOccurred = false;
 	private boolean novalueException = false;
 	
 	private Throwable exception = null;	// Was there another kind of exception that was caught.
 	
 	/**
 	 * Process all other exceptions then the NoExpressionValueException. This can be called from usage code so that if there was an error
 	 * in setting up for a call to the processer it can be logged.
 	 * 
 	 * @param e
 	 * 
 	 * @since 1.0.0
 	 */
 	public final void processException(Throwable e) {
 		// Process all other exceptions.
 		novalueException = false;
 		while (e.getCause() != null)
 			e = e.getCause();
 		if (traceOn) {
 			System.out.println();
 			System.out.print("***** >>>\tException: "); //$NON-NLS-1$
 			System.out.println(e);
 		}
 		throwException(e);	// Treat as a throw to let try/catches expressions handle it.
 	}
 	
 	/**
 	 * This is a syntax exception. This means data coming across is corrupted in
 	 * some way so no further processing should occur. 
 	 * @param e
 	 * 
 	 * @since 1.1.0
 	 */
 	protected final void processSyntaxException(Throwable e) {
 		errorOccurred = true;
 		novalueException = false;
 		exception = e;		
 	}
 		
 	/**
 	 * Process a NoExpressionValueException. Don't wrapper these.
 	 * @param e
 	 * 
 	 * @since 1.1.0
 	 */
 	protected final void processSyntaxException(NoExpressionValueException e) {
 		if (traceOn)
 			printTrace("Expression has no value", false); //$NON-NLS-1$
 		try {
 			errorOccurred = true;
 			novalueException = true;
 			exception = e;
 		} finally {
 			if (traceOn)
 				printTraceEnd();
 		}
 	}
 	
 	/**
 	 * Return whether there are any errors.
 	 * 
 	 * @return <code>true</code> if no errors.
 	 * 
 	 * @since 1.0.0
 	 */
 	public boolean noErrors() {
 		return !errorOccurred;
 	}
 	
 	/**
 	 * Return whether the error is a NoExpressionValueException or not.
 	 * @return
 	 * 
 	 * @since 1.1.0
 	 */
 	public boolean isNoExpressionValue() {
 		return novalueException;
 	}
 	
 	/**
 	 * Return the throwable if a Throwable was caught.
 	 * 
 	 * @return The throwable, or <code>null</code> if not set.
 	 * 
 	 * @since 1.0.0
 	 */
 	public Throwable getErrorThrowable() {
 		return exception;
 	}
 
 	/**
 	 * Push the expression value and its expected type.
 	 * @param o
 	 * @param type
 	 * 
 	 * @since 1.0.0
 	 */
 	protected final void pushExpressionValue(Object o, Class type) {
 		expressionStack.add(o);
 		expressionTypeStack.add(type);
 	}
 
 	/**
 	 * Pop just the expression value. It is imperitive that the expression type
 	 * is popped immediately following. Separated the methods so that we
 	 * don't need to create an array to return two values. This will dereference
 	 * any variable references.
 	 * 
 	 * @return The value.
 	 * @throws NoExpressionValueException
 	 * 
 	 * @since 1.0.0
 	 */
 	protected final Object popExpression() throws NoExpressionValueException {
 		return popExpression(true);
 	}
 	
 	/**
 	 * Pop just the expression value. It is imperitive that the expression type
 	 * is popped immediately following. Separated the methods so that we
 	 * don't need to create an array to return two values.
 	 * 
 	 * @param deReference If the top expression is a Reference, then dereference it.
 	 * @return The value.
 	 * @throws NoExpressionValueException
 	 * 
 	 * @since 1.0.0
 	 */
 	protected final Object popExpression(boolean deReference) throws NoExpressionValueException {
 		try {
 			// Do not pop above the current subexpression pos, if any.
 			if (topSubexpression != -1)
 				if (expressionStack.size() == subexpressionStackPos[topSubexpression])
 					throw new NoExpressionValueException();
 			
 			Object result = expressionStack.remove(expressionStack.size()-1);
 			if (deReference && result instanceof VariableReference)
 				result = ((VariableReference) result).dereference();
 			return result;
 		} catch (IndexOutOfBoundsException e) {
 			throw new NoExpressionValueException();
 		}
 	}
 
 	/**
 	 * Get the expression at <code>fromTop</code> down from the top. This is
 	 * need for when multi-operators happen and they are stored in reverse of 
 	 * what is needed. They would normally be stored left to right, with the
 	 * rightmost one on top. But they need to be processed left to right, so
 	 * to get the left most one requires digging down in the stack.
 	 * <p>
 	 * When done, <code>popExpressions(int count)</code> must be called to
 	 * clean them out since they were processed.
 	 * <p>
 	 * This will not dereference the expression. It is the job of the caller to do this. 
 	 *  
 	 * @param fromTop <code>1</code> is the top one, <code>2</code> is the next one down.
 	 * @return The entry from the top that was requested.
 	 * @throws NoExpressionValueException
 	 * 
 	 * @see IDEExpression#popExpressions(int)
 	 * @since 1.0.0
 	 */
 	protected final Object getExpression(int fromTop) throws NoExpressionValueException {
 		try {
 			// Do not pull above the current subexpression pos, if any.			
 			if (topSubexpression != -1)
 				if (expressionStack.size()-fromTop < subexpressionStackPos[topSubexpression])
 					throw new NoExpressionValueException();
 			
 			return expressionStack.get(expressionStack.size()-fromTop);
 		} catch (IndexOutOfBoundsException e) {
 			throw new NoExpressionValueException();
 		}
 	}
 	
 	/**
 	 * Remove the top <code>count</code> items. This will not cause dereferencing to occur. It
 	 * removes the corresponding type stack entries.
 	 * 
 	 * @param count
 	 * @throws NoExpressionValueException
 	 * 
 	 * @since 1.0.0
 	 */
 	protected final void popExpressions(int count) throws NoExpressionValueException {
 		try {
 			// Do not pop above the current subexpression pos, if any.
 			int stop = topSubexpression != -1 ? subexpressionStackPos[topSubexpression] : -1;
 			int remove = expressionStack.size()-1;
 			while (count-- > 0) {
 				if (expressionStack.size() <= stop)
 					throw new NoExpressionValueException();	// Try to go above the current subexpression.
 				expressionStack.remove(remove);
 				expressionTypeStack.remove(remove--);
 			}
 		} catch (IndexOutOfBoundsException e) {
 			throw new NoExpressionValueException();
 		}
 	}
 	
 	/**
 	 * Pop just the expression type. It is imperitive that the expression type
 	 * is popped immediately following popExpression. Separated the methods so that we
 	 * don't need to create an array to return two values.
 	 * <p>
 	 * If the allowVoid is false and type is void, then a NoExpressionValueException will be thrown.
 	 * This is for the case where the expression was trying to be used in a different
 	 * expression. This will be set to void only on expressions that return no value (only
 	 * method's do this for now).
 	 * 
 	 * @param allowVoid Allow void types if <code>true</code>
 	 * @return The type.
 	 * @throws NoExpressionValueException
 	 * @since 1.0.0
 	 */
 	protected final Class popExpressionType(boolean allowVoid) throws NoExpressionValueException {
 		try {
 			Class result = (Class) expressionTypeStack.remove(expressionTypeStack.size()-1);
 			if (!allowVoid && result == Void.TYPE)
 				throw new NoExpressionValueException(InitparserTreeMessages.getString("ExpressionProcesser.PopExpressionType.ExpressionVoid_EXC_")); //$NON-NLS-1$
 			return result;
 				
 		} catch (IndexOutOfBoundsException e) {
 			throw new NoExpressionValueException();
 		}
 	}
 
 	/**
 	 * Get the expression type at <code>fromTop</code> down from the top. This is
 	 * need for when multi-operators happen and they are stored in reverse of 
 	 * what is needed. They would normally be stored left to right, with the
 	 * rightmost one on top. But they need to be processed left to right, so
 	 * to get the left most one requires digging down in the stack.
 	 * <p>
 	 * When done, <code>popExpressionTypes(int count)</code> must be called to
 	 * clean them out since they were processed.
 
 	 * @param fromTop <code>1</code> is the top one, <code>2</code> is the next one down.
 	 * @param allowVoid Allow void types if <code>true</code>
 	 * @return The type from the top that was requested.
 	 * @throws ThrowableProxy
 	 * @throws NoExpressionValueException
 	 * 
 	 * @see IDEExpression#popExpressionTypes(int)
 	 * @since 1.0.0
 	 */
 	protected final Class getExpressionType(int fromTop, boolean allowVoid) throws NoExpressionValueException {
 		try {
 			Class result = (Class) expressionTypeStack.get(expressionTypeStack.size()-fromTop);
 			if (!allowVoid && result == Void.TYPE)
 				throw new NoExpressionValueException();
 			return result;
 		} catch (IndexOutOfBoundsException e) {
 			throw new NoExpressionValueException();
 		}
 	}
 	
 	/**
 	 * Flag indicating expression should be ignored and not processed.
 	 * This happens because of few cases, like conditional and, that
 	 * if one returns false, the rest of the expressions in that conditional and
 	 * expression should be ignored and not processed.
 	 * <p>
 	 * It is an Object that acts as an enum for the type of expression that initiated the ignore. 
 	 * If it is <code>null</code> then no one is ignoring. 
 	 * <p>
 	 * All of the pushTo...Proxy methods must test this for this to work correctly.
 	 * Each expression has some way of testing that their particular nesting of 
 	 * expressions is complete and they can turn off the ignore flag.
 	 * <p>
 	 * Only one type of ignore can exist at a time.
 	 */
 	protected Object ignoreExpression = null;	
 	
 	
 	private List saveStates;
 	
 	/**
 	 * Are we tracing or not.
 	 */
 	protected final boolean traceOn;
 	private final long thresholdTime;
 	private long startExpressionStepTime;
 	private long startExpressionTime;
 	private long lastExpressionEndTime;
 	
 	/**
 	 * Trace head of this expression. So that traces from different expressions can be distinquished.
 	 * It is simply an monotonically increasing counter. It is the header string for any trace output.
 	 */
 	protected final String traceHeader;
 	
 	private int indent = 0;	// Indented for certain block expressions.
 	
 	/*
 	 * Trace counter. It is incremented once for each expression and assigned to the traceId of the expression.
 	 */
 	private static int TRACE_COUNTER;
 	
 	/**
 	 * Create the Expression without tracing.
 	 * @param registry
 	 * 
 	 * @since 1.0.0
 	 */
 	public ExpressionProcesser() {
 		this(false, -1);
 	}
 	
 	/**
 	 * Create the expression, and set the tracing mode and threshold time. Use -1
 	 * for default time of 100ms.
 	 * @param traceOn
 	 * 
 	 * @since 1.1.0
 	 */
 	public ExpressionProcesser(boolean traceOn, long threshold) {
 		this.traceOn = traceOn;
 		if (traceOn) {
 			traceHeader = "**"+(++TRACE_COUNTER)+':'; //$NON-NLS-1$
 			System.out.print(traceHeader);
 			System.out.println(" Start expression"); //$NON-NLS-1$
 			this.thresholdTime = threshold != -1 ? threshold : 100;
 			lastExpressionEndTime = startExpressionTime = System.currentTimeMillis();
 		} else {
 			traceHeader = null;
 			thresholdTime = 100;
 		}
 	}
 	
 	/**
 	 * Trace msg helper. Should only be called if traceOn is true. This method is only used to start a new trace message.
 	 * The caller is must call printTraceEnd at the end.
 	 *  
 	 * @param msg message to print
 	 * @param ignore are we ignoring the expression, or is it being processed (this just alters the trace output slightly).
 	 * 
 	 * @since 1.1.0
 	 */
 	protected void printTrace(String msg, boolean ignore) {
 		startExpressionStepTime = System.currentTimeMillis();
 		long sinceLastExpression = startExpressionStepTime - lastExpressionEndTime;
 		System.out.print(traceHeader);
 		if (sinceLastExpression > 0) {
 			System.out.print('(');
 			if (sinceLastExpression > thresholdTime)
 				System.out.print("***"); //$NON-NLS-1$
 			System.out.print(sinceLastExpression);
 			System.out.print("ms)"); //$NON-NLS-1$
 		}
 		System.out.print('\t');
 		if (!ignore)
 			System.out.print("\t"); //$NON-NLS-1$
 		else
 			System.out.print("##\t"); //$NON-NLS-1$
 		
 		printIndent();
 		System.out.print(msg);
 	}
 	
 	/**
 	 * print the indent. It will not do a new line before nor after.
 	 * 
 	 * @since 1.1.0
 	 */
 	protected void printIndent() {
 		for(int i=indent; i>0; i--) {
 			System.out.print("  "); //$NON-NLS-1$
 		}
 	}
 
 	protected void printTraceEnd() {
 		long stop = System.currentTimeMillis()-startExpressionStepTime;
 		if (stop > 0) {
 			System.out.print(" ("); //$NON-NLS-1$
 			if (stop > thresholdTime)
 				System.out.print("***"); //$NON-NLS-1$
 			System.out.print(stop);
 			System.out.print("ms)"); //$NON-NLS-1$
 		}
 		System.out.println();
 		lastExpressionEndTime = System.currentTimeMillis();
 	}
 	
 	/**
 	 * Do an indent (undent) according to indent flag.
 	 * @param indent <code>true</code> to increment indent, or otherwise decrement.
 	 * 
 	 * @since 1.1.0
 	 */
 	protected void indent(boolean indent) {
 		this.indent += (indent ? 1 : -1);
 		if (this.indent < 0)
 			this.indent = 0;
 	}
 	
 	/**
 	 * Print the object and type. It will not end with a newline char, so one will be needed afterwards.
 	 * 
 	 * @param o
 	 * @param t
 	 * 
 	 * @since 1.1.0
 	 */
 	protected void printObjectAndType(Object o, Class t) {
 		System.out.print(' ');
 		System.out.print("Object-"); //$NON-NLS-1$
 		System.out.print(o);
 		System.out.print(" Type-"); //$NON-NLS-1$
 		System.out.print(t);
 		System.out.print(' ');
 	}
 	
 	/**
 	 * Close the exception processing
 	 * 
 	 * @since 1.0.0
 	 */
 	public final void close() {
 		boolean firstClose = expressionStack != null;
 		if (firstClose && traceOn) {
 			printTrace("End expression", false); //$NON-NLS-1$
 			long totalTime = System.currentTimeMillis()-startExpressionTime;
 			System.out.print(" Total expression evaluation time: "); //$NON-NLS-1$
 			System.out.print(totalTime);
 			System.out.print("ms."); //$NON-NLS-1$
 		}
 		try {
 			expressionStack = null;
 			expressionTypeStack = null;
 			expressionProxies = null;
 			exception = null;
 			catchThrowable = null;
 			saveStates = null;
 		} finally {
 			if (firstClose && traceOn)
 				printTraceEnd();
 		}
 	}
 
 	/**
 	 * Pull the value. The value will be placed into the array passed in.
 	 * It will be stored as value[0] = value value[1] = valuetype(Class).
 	 * 
 	 * @param value The value array to store the value and type into.
 	 * @throws NoExpressionValueException
 	 * @since 1.0.0
 	 */
 	public final void pullValue(Object[] value) throws NoExpressionValueException {
 		if (traceOn)
 			printTrace("Pull value:", false); //$NON-NLS-1$
 		try {
 			value[0] = popExpression();
 			value[1] = popExpressionType(false);
 		} finally {
 			if (traceOn) {
 				printObjectAndType(value[0], (Class) value[1]);
 				printTraceEnd();
 			}
 		}
 		close();
 	}
 	
 	/**
 	 * Pull the value of the expression proxy, dereferencing it if necessary. This is for resolution only purposes at the
 	 * end of the expression being processed. Not meant for general access to the value of expression proxy. Use 
 	 * {@link ExpressionProcesser#getExpressionProxyValue(int, Object[])} instead for general access to the value.
 	 * 
 	 * @param proxyid
 	 * @param value
 	 * @throws NoExpressionValueException
 	 * 
 	 * @since 1.1.0
 	 */
 	public final void pullExpressionProxyValue(int proxyid, Object[] value) throws NoExpressionValueException {
 		getExpressionProxyValue(proxyid, value, true, true);
 	}
 	
 	/**
 	 * Get the expression proxy value. If the expression has not yet been evaluated it will
 	 * return false. If it has it will return true.
 	 * @param proxyid
 	 * @param value put value into value[0] and the type into value[1].
 	 * @return <code>true</code> if successful, or <code>false</code> if the expression proxy was never resolved or doesn't exist.
 	 * 
 	 * @since 1.1.0
 	 */
 	public boolean getExpressionProxyValue(int proxyid, Object[] value) {
 		try {
 			return getExpressionProxyValue(proxyid, value, true, false);
 		} catch (NoExpressionValueException e) {
 			return false;
 		}
 	}
 	
 	/*
 	 * Internal method use to actually get the value, but to distinquish between pull and get of the public interface.
 	 * Get will process the errors as normal execution errors, while pull will throw the errors. finalTrace is when
 	 * this is the final call to return the values to the client. We will trace the results in that case.
 	 * Return true if successful.
 	 */
 	private boolean getExpressionProxyValue(int proxyid, Object[] value, boolean pull, boolean finalTrace) throws NoExpressionValueException {
 		// Note: This will throw the exceptions right away since this is called from outside to fill in the value and
 		// so we are holding such exceptions.
 		boolean doTrace = finalTrace && traceOn;
 		try {
 			if (expressionProxies != null && expressionProxies.size() > proxyid) {
 				InternalExpressionProxy proxy = (InternalExpressionProxy) expressionProxies.get(proxyid);
 				if (proxy != null && proxy.isSet()) {
 					value[0] = proxy.getValue();
 					if (value[0] instanceof VariableReference)
 						value[0] = ((VariableReference) value[0]).dereference(); // Here we want the final current value.
 					value[1] = proxy.getType();
 					if (doTrace)
 						if (value[1] != Void.TYPE) {
 							printTrace("Return Proxy #" + proxyid + " Resolved to", false); //$NON-NLS-1$ //$NON-NLS-2$
 							printObjectAndType(value[0], (Class) value[1]);
 						} else
 							printTrace("Return Proxy #" + proxyid + " Resolved to void.", false); //$NON-NLS-1$ //$NON-NLS-2$
 					return true;
 
 				} else {
 					if (doTrace)
 						printTrace("Return Proxy #" + proxyid + ": Not resolved", false); //$NON-NLS-1$ //$NON-NLS-2$
 					NoExpressionValueException e = new NoExpressionValueException(InitparserTreeMessages.getString("ExpressionProcesser.GetExpressionProxyValue.ExpressionProxyNotSet_EXC_")); //$NON-NLS-1$
 					if (pull)
 						throw e;
 					else
 						processSyntaxException(e);
 					return false;
 				}
 			} else {
 				if (doTrace)
 					printTrace("Return Proxy #" + proxyid + ": Never created.", false); //$NON-NLS-1$ //$NON-NLS-2$
 				NoExpressionValueException e = new NoExpressionValueException(InitparserTreeMessages.getString("ExpressionProcesser.GetExpressionProxyValue.ExpressionProxyDoesntExist_EXC_")); //$NON-NLS-1$
 				if (pull)
 					throw e;
 				else
 					processSyntaxException(e);
 				return false;
 			}
 		} finally {
 			if (doTrace)
 				printTraceEnd();
 		}
 	}
 	
 	/**
 	 * Push the expression (just a value) onto the stack.
 	 * 
 	 * @param o
 	 * @param t
 	 * 
 	 * @since 1.0.0
 	 */
 	public final void pushExpression(Object o, Class t) {
 		boolean ignore = (ignoreExpression != null || errorOccurred);
 		if (traceOn) {
 			printTrace("Push: ", ignore); //$NON-NLS-1$
 			printObjectAndType(o, t);
 		}
 		try {
 			if (ignore)
 				return;
 			pushExpressionValue(o, t);
 		} finally {
 			if (traceOn)
 				printTraceEnd();
 		}
 	}
 	
 	/**
 	 * Get the value of the expression proxy (from proxy id), and push the value onto the stack.
 	 *  
 	 * @param proxyid The proxy id of the ExpressionProxy to push as a value.
 	 * 
 	 * @since 1.0.0
 	 */
 	public final void pushExpressionProxy(int proxyid) {
 		boolean ignore =(ignoreExpression != null || errorOccurred);
 		if (traceOn)
 			printTrace("Push Expression Proxy #"+proxyid, ignore); //$NON-NLS-1$
 		try {
 			if (ignore)
 				return;
 			if (expressionProxies != null && expressionProxies.size() > proxyid) {
 				InternalExpressionProxy proxy = (InternalExpressionProxy) expressionProxies.get(proxyid);
 				if (proxy != null && proxy.isSet()) {
 					if (traceOn)
 						printObjectAndType(proxy.getValue(), proxy.getType());
 					pushExpressionValue(proxy.getValue(), proxy.getType());	// Can push a VariableReference. This is ok. When used it will then deref with the current value.
 				} else
 					processSyntaxException(new NoExpressionValueException());
 			} else
 				processSyntaxException(new NoExpressionValueException());
 		} finally {
 			if (traceOn)
 				printTraceEnd();
 		}
 	}
 	
 	/**
 	 * Push a cast onto stack. The type passed in is either a String (with classname to cast to) or the
 	 * type to cast to.
 	 * @param type To cast to. If <code>String</code> then convert to type (using something like <code>Class.forName()</code>) or it is a Class
 	 * 
 	 * @since 1.0.0
 	 */
 	public final void pushCast(Class type) {
 		boolean ignore = (ignoreExpression != null || errorOccurred);
 		if (traceOn)
 			printTrace("Cast to: "+type, ignore); //$NON-NLS-1$
 		try {
 			if (ignore)
 				return;
 			
 			try {
 				Object exp = popExpression();
 				Class exptype = popExpressionType(false);
 				
 				pushExpressionValue(castBean(type, exp, exptype), type);
 			} catch (RuntimeException e) {
 				processException(e);
 			} catch (NoExpressionValueException e) {
 				processSyntaxException(e);
 			}
 		} finally {
 			if (traceOn)
 				printTraceEnd();
 		}
 			
 	}
 	
 	/**
 	 * Cast a bean into the return type. If the return type is not primitive, then
 	 * the bean is left alone, however it is checked to be an instance of
 	 * the return type. If the return type is primitive, then the
 	 * correct primitive wrapper is created from the bean (bean must be a number or character or boolean primitve so
 	 * that cast will work).
 	 * <p>
 	 * However if can't be cast for primitive or if not an instance of the
 	 * returntype for objects, a ClassCastException will be raised.
 	 * <p>
 	 * This is a helper method for expression processer to cast a bean. Since it is a helper method it doesn't
 	 * check nor process the exception. It throws it. Callers must handle it as they see fit.
 	 * 
 	 * @param returnType
 	 * @param bean
 	 * @param beanType The type that bean is supposed to be (e.g. even though it is a Number, it actually represents a primitive).
 	 * @return The cast bean (either to the appropriate primitive wrapper type or bean)
 	 * 
 	 * @throws ClassCastException
 	 * @since 1.0.0
 	 */
 	protected final Object castBean(Class returnType, Object bean, Class beanType) throws ClassCastException {
 		// Cast uses true value and true class of bean, not expected type (i.e. not beanType).
 		if (bean == null)
 			if (!returnType.isPrimitive())
 				return bean;	// bean is null, and return type is not primitive, so this is a valid cast.
 			else 
 				throwClassCast(returnType, bean);
 		else if (returnType.equals(bean.getClass()))
 			return bean;	// They are already the same.
 		else if (!returnType.isPrimitive()) {
 			if (!beanType.isPrimitive() && returnType.isInstance(bean))
 				return bean;
 			else
 				throwClassCast(returnType, bean);	// Either bean type was wrappering primitive or not instanceof returntype.
 		} else {
 			if (!beanType.isPrimitive())
 				throwClassCast(returnType, bean);	// bean type was not wrappering a primitive. Can't cast to primitive.
 			// It is return type of primitive. Now convert to correct primitive.
 			if (returnType == Boolean.TYPE)
 				if (bean instanceof Boolean)
 					return bean;
 				else
 					throwClassCast(returnType, bean);
 			else {
 				if (bean instanceof Number) {
 					if (returnType == Integer.TYPE)
 						if (bean instanceof Integer)
 							return bean;
 						else
 							return new Integer(((Number) bean).intValue());
 					else if (returnType == Byte.TYPE)
 						if (bean instanceof Byte)
 							return bean;
 						else
 							return new Byte(((Number) bean).byteValue());
 					else if (returnType == Character.TYPE)
 						if (bean instanceof Character)
 							return bean;
 						else
 							return new Character((char) ((Number) bean).intValue());
 					else if (returnType == Double.TYPE)
 						if (bean instanceof Double)
 							return bean;
 						else
 							return new Double(((Number) bean).doubleValue());
 					else if (returnType == Float.TYPE)
 						if (bean instanceof Float)
 							return bean;
 						else
 							return new Float(((Number) bean).floatValue());
 					else if (returnType == Long.TYPE)
 						if (bean instanceof Long)
 							return bean;
 						else
 							return new Long(((Number) bean).longValue());
 					else if (returnType == Short.TYPE)
 						if (bean instanceof Short)
 							return bean;
 						else
 							return new Short(((Number) bean).shortValue());	
 					else
 						throwClassCast(returnType, bean);
 				} else if (bean instanceof Character) {
 					if (returnType == Character.TYPE)
 						return bean;
 					else if (returnType == Integer.TYPE)
 						return new Integer(((Character) bean).charValue());
 					else if (returnType == Byte.TYPE)
 						return new Byte((byte) ((Character) bean).charValue());
 					else if (returnType == Double.TYPE)
 						return new Double(((Character) bean).charValue());
 					else if (returnType == Float.TYPE)
 						return new Float(((Character) bean).charValue());
 					else if (returnType == Long.TYPE)
 						return new Long(((Character) bean).charValue());
 					else if (returnType == Short.TYPE)
 						return new Short((short) ((Character) bean).charValue());	
 					else
 						throwClassCast(returnType, bean);
 				} else
 					throwClassCast(returnType, bean);
 			}
 			
 		}
 		return null;	// It should never get here;
 	}
 	
 	private void throwClassCast(Class returnType, Object bean) throws ClassCastException {
 		throw new ClassCastException(MessageFormat.format(InitparserTreeMessages.getString("ExpressionProcesser.CannotCastXToY_EXC_"), new Object[] {bean != null ? bean.getClass().getName() : null, returnType.getName()})); //$NON-NLS-1$
 	}
 
 	/**
 	 * Return the primitive type that the wrapper bean represents (i.e. Boolean instance returns Boolean.TYPE) 
 	 * <p>
 	 * This is a helper method for expression processer to get the primitive type. Since it is a helper method it doesn't
 	 * check nor process the exception. It throws it. Callers must handle it as they see fit.
 
 	 * @param bean
 	 * @return the primitive type class of the given bean.
 	 * @throws IllegalArgumentException if bean is <code>null</code> or not of the type that can be converted to a primitive.
 	 * 
 	 * @since 1.0.0
 	 */
 	protected final Class getPrimitiveType(Object bean) throws IllegalArgumentException {
 		if (bean instanceof Boolean)
 			return Boolean.TYPE;
 		else if (bean instanceof Integer)
 			return Integer.TYPE;
 		else if (bean instanceof Byte)
 			return Byte.TYPE;
 		else if (bean instanceof Character)
 			return Character.TYPE;
 		else if (bean instanceof Double)
 			return Double.TYPE;
 		else if (bean instanceof Float)
 			return Float.TYPE;
 		else if (bean instanceof Long)
 			return Long.TYPE;
 		else if (bean instanceof Short)
 			return Short.TYPE;
 		else
 			throw new IllegalArgumentException(bean != null ? bean.getClass().getName() : "null"); //$NON-NLS-1$
 	}
 	
 	private static final Object IFELSE_IGNORE = "IF/ELSE IGNORE";	// Flag for if/else in ingore //$NON-NLS-1$
 	private int ifElseNesting = 0;	// Nesting of if/else expressions.
 	private int ifElseIgnoreNestCount = 0;	// When ignoring if/else expressions, ignore until this nest count.
 	private boolean ifElseSkipTruePart;
 
 	
 	/**
 	 * Push an if test expression.
 	 * @param hasElseClause
 	 * 
 	 * @since 1.0.0
 	 */
 	public final void pushIfElse() {
 		try {
 			boolean ignore = true;
 			try {
 				if (errorOccurred)
 					return;
 				// Slightly different here in that if an ignoring occurred we still need to process at least part of it so that
 				// we can get the expression grouping correct.
 				ifElseNesting++;	// We have the test.
 	
 				if (ignoreExpression != null)
 					return;
 				ignore = false;
 			} finally {
 				if (traceOn)
 					printTrace("If test condition", ignore); //$NON-NLS-1$
 			}
 					
 			try {
 				Object condition = popExpression();
 				Class type = popExpressionType(false);
 				if (type != Boolean.TYPE)
 					throwClassCast(Boolean.TYPE, condition);
 				if (traceOn) {
 					System.out.print(" Test Result="+condition); //$NON-NLS-1$
 					printTraceEnd();
 					indent(true);
 					printTrace("Begin True Expression.", ignore); //$NON-NLS-1$
 					printTraceEnd();
 					indent(true);
 				}				
 				if (((Boolean) condition).booleanValue()) {
 					// Condition was true.
 					// Do nothing. Let true condition be processed.
 				} else {
 					// Condition was false.
 					ifElseSkipTruePart = true;	// Tell the true condition should be ignored.
 					ignoreExpression = IFELSE_IGNORE;
 					ifElseIgnoreNestCount = ifElseNesting;
 				}
 				// We don't put anything back on the stack because the condition test is not ever returned.
 				// The appropriate true or false condition evaluation will be left on the stack.
 			} catch (RuntimeException e) {
 				processException(e);
 			} catch (NoExpressionValueException e) {
 				processSyntaxException(e);
 			}
 		} finally {
 			if (traceOn)
 				printTraceEnd();
 		}
 	}
 
 	/**
 	 * Push an if/else clause. It can be any clause of the if (true, or false clause).
 	 * @param clauseType
 	 * 
 	 * @since 1.0.0
 	 */
 	public final void pushIfElse(InternalIfElseOperandType clauseType) {
 		try {
 			boolean ignore = true;
 			if (errorOccurred)
 				return;
 			// Slightly different here in that if an ignoring occurred we still need to process at least part of it so that
 			// we can get the expression grouping correct.
 			switch (clauseType.getValue()) {
 			case InternalIfElseOperandType.TRUE_CLAUSE_VALUE:
 					if (traceOn) {
 						indent(false);
 						printTrace("Begin False Expression.", ignore); //$NON-NLS-1$
 						printTraceEnd();
 						indent(true);
 					}
 					if (ifElseSkipTruePart && ignoreExpression == IFELSE_IGNORE && ifElseIgnoreNestCount == ifElseNesting) {
 						// stop ignoring, we've ignored the true condition of interest.
 						ignoreExpression = null;
 						return; // However, leave because since this condition was ignored.
 					}
 					break;
 				case InternalIfElseOperandType.ELSE_CLAUSE_VALUE:
 					if (traceOn) {
 						indent(false);
 						indent(false);
 						printTrace("End IF/ELSE Expression.", ignore); //$NON-NLS-1$
 						printTraceEnd();
 					}					
 					int currentNesting = ifElseNesting--;
 					if (ignoreExpression == IFELSE_IGNORE && ifElseIgnoreNestCount == currentNesting) {
 						// stop ignoring, we've ignored the false condition of interest.
 						ignoreExpression = null;
 						return; // However, leave because since this condition was ignored.
 					}
 			}
 	
 				if (ignoreExpression != null)
 					return;
 				ignore = false;
 
 					
 			try {
 				switch (clauseType.getValue()) {
 					case InternalIfElseOperandType.TRUE_CLAUSE_VALUE:
 						ifElseSkipTruePart = false;	// Tell the false condition should be ignored.
 						ignoreExpression = IFELSE_IGNORE;
 						ifElseIgnoreNestCount = ifElseNesting;
 						break;
 					case InternalIfElseOperandType.ELSE_CLAUSE_VALUE:
 						// There's nothing to do, if it was ignored due to true, we wouldn't of gotton here.
 						// If it wasn't ignored, then the result of the false expression is on the stack, which is what it should be.
 						break;
 				}
 			} catch (RuntimeException e) {
 				processException(e);
 			}
 		} finally {
 			if (traceOn)
 				printTraceEnd();
 		}
 	}
 
 	/**
 	 * Push the instanceof expression.  The type passed in is either a String (with classname to test against) or the
 	 * type to test against.
 	 * @param type To test against.
 	 * @since 1.0.0
 	 */
 	public final void pushInstanceof(Class type) {
 		boolean ignore = (ignoreExpression != null || errorOccurred);
 		if (traceOn)
 			printTrace("Instanceof type: "+type, ignore); //$NON-NLS-1$
 		try {
 			if (ignore)
 				return;
 			
 			try {
 				Object exp = popExpression();
 				Class exptype = popExpressionType(false);
 				pushExpressionValue(Boolean.valueOf(isInstance(type, exp, exptype)), Boolean.TYPE);
 			} catch (NoExpressionValueException e) {
 				processSyntaxException(e);
 			} catch (RuntimeException e) {
 				processException(e);
 			}
 		} finally {
 			if (traceOn)
 				printTraceEnd();
 		}
 	}
 	
 	/**
 	 * Test if instance of. It will make sure that primitive to non-primitive is not permitted.
 	 * This is a true instance of, which means null IS NOT AN instance of any type. This is
 	 * different then assignable from, in that case null can be assigned to any class type.
 	 * <p>
 	 * This is a helper method for expression processer to do isInstance. Since it is a helper method it doesn't
 	 * check nor process exceptions.
 
 	 * @param type
 	 * @param bean
 	 * @param beanType
 	 * @return
 	 * 
 	 * @since 1.0.0
 	 */
 	protected final boolean isInstance(Class type, Object bean, Class beanType) {
 		if (type.isPrimitive())
 			return beanType.isPrimitive() && type == beanType;	// Can't use isInstance because for a primitive type isInstance returns false.
 		else 
 			return type.isInstance(bean);
 	}
 	
 	/**
 	 * Push new instance from string.
 	 * @param initializationString
 	 * @param resultType expected result type. If it isn't of that type, a classcast will be processed. 
 	 * @param classloader classloader to use for finding classes, or <code>null</code> to use classloader of InitializationStringParser.class.
 	 * 
 	 * @since 1.1.0
 	 */
 	public final void pushNewInstanceFromString(String initializationString, Class resultType, ClassLoader classloader) {
 		boolean ignore = (ignoreExpression != null || errorOccurred);
 		if (traceOn)
 			printTrace("New instance from string: \""+initializationString+"\" Type="+resultType, ignore); //$NON-NLS-1$ //$NON-NLS-2$
 		try {
 			if (ignore)
 				return;
 			
 			try {
 				InitializationStringParser parser = InitializationStringParser.createParser(initializationString, classloader);
 				Object newValue = parser.evaluate();
 				newValue = castBean(resultType, newValue, parser.getExpectedType());
 				pushExpressionValue(newValue, resultType);
 			} catch (RuntimeException e) {
 				processException(e);
 			} catch (InitializationStringEvaluationException e) {
 				processException(e);
 			}
 		} finally {
 			if (traceOn)
 				printTraceEnd();
 		}
 
 	}
 
 	/**
 	 * Push prefix expression.
 	 * @param operator 
 	 * @since 1.0.0
 	 */
 	public final void pushPrefix(PrefixOperator operator) {
 		try {
 			if (ignoreExpression != null || errorOccurred) {
 				if (traceOn)
 					printTrace("Prefix: \'"+operator+"\'", true); //$NON-NLS-1$ //$NON-NLS-2$
 				return;
 			}
 			
 			if (operator == PrefixOperator.PRE_PLUS)
 				return;	// Do nothing. "+" doesn't affect the result of the current top expression.
 	
 			if (traceOn)
 				printTrace("Prefix: \'"+operator+"\' ", false); //$NON-NLS-1$ //$NON-NLS-2$
 	
 			try {
 				Object exp = popExpression();
 				Class exptype = popExpressionType(false);
 				if (!exptype.isPrimitive())
 					throwInvalidPrefix(operator, exp);
 				
 				int primTypeEnum = getEnumForPrimitive(exptype);
 				switch (operator.getValue()) {
 					case PrefixOperator.PRE_MINUS_VALUE:
 						switch (primTypeEnum) {
 							case BOOLEAN:
 								throwInvalidPrefix(operator, exp);						
 							case BYTE:
 								exp = new Integer(-((Number) exp).byteValue());
 								break;
 							case CHAR:
 								exp = new Integer(-((Character) exp).charValue());
 								break;
 							case DOUBLE:
 								exp = new Double(-((Number) exp).doubleValue());
 								break;
 							case FLOAT:
 								exp = new Float(-((Number) exp).floatValue());
 								break;
 							case INT:
 								exp = new Integer(-((Number) exp).intValue());
 								break;
 							case LONG:
 								exp = new Long(-((Number) exp).longValue());
 								break;
 							case SHORT:
 								exp = new Integer(-((Number) exp).shortValue());
 								break;
 						}
 						exptype = getPrimitiveType(exp);	// It can actually change the type.				
 						break;
 						
 					case PrefixOperator.PRE_COMPLEMENT_VALUE:
 						switch (primTypeEnum) {
 							case BOOLEAN:
 							case DOUBLE:
 							case FLOAT:
 								throwInvalidPrefix(operator, exp);						
 							case BYTE:
 								exp = new Integer(~((Number) exp).byteValue());
 								break;
 							case CHAR:
 								exp = new Integer(~((Character) exp).charValue());
 								break;
 							case INT:
 								exp = new Integer(~((Number) exp).intValue());
 								break;
 							case LONG:
 								exp = new Long(~((Number) exp).longValue());
 								break;
 							case SHORT:
 								exp = new Integer(~((Number) exp).shortValue());
 								break;
 						}
 						exptype = getPrimitiveType(exp);	// It can actually change the type.
 						break;
 					case PrefixOperator.PRE_NOT_VALUE:
 						switch (primTypeEnum) {
 							case BOOLEAN:
 								exp = !((Boolean) exp).booleanValue() ? Boolean.TRUE : Boolean.FALSE;
 								break;
 							case BYTE:
 							case CHAR:
 							case DOUBLE:
 							case FLOAT:
 							case INT:
 							case LONG:
 							case SHORT:
 								throwInvalidPrefix(operator, exp);						
 						}				
 						break;
 					}
 				
 				if (traceOn)
 					printObjectAndType(exp, exptype);
 				pushExpressionValue(exp, exptype);	// Push the result back on the stack.
 	
 			} catch (IllegalArgumentException e) {
 				processSyntaxException(e);
 			} catch (NoExpressionValueException e) {
 				processSyntaxException(e);
 			} catch (RuntimeException e) {
 				processException(e);
 			} 
 		} finally {
 			if (traceOn)
 				printTraceEnd();
 		}
 			
 	}
 	
 	/**
 	 * Assign the right expression to the left expression.
 	 * @since 1.1.0
 	 */
 	public final void pushAssignment() {
 		if (ignoreExpression != null || errorOccurred) {
 			if (traceOn) {
 				printTrace("Assignment", true); //$NON-NLS-1$
 				printTraceEnd();
 			}
 			return;
 		}
 		
 		try {
 			// KLUDGE: The only reason leftValue/refType are outside of try/finally is because
 			// of tracing. pushExpression() does its own trace statements, so we need to end
 			// our trace before calling pushExpression. 
 			Object leftValue;
 			Class refType;
 			try {
 				if (traceOn)
 					printTrace("Assignment: ", false);			 //$NON-NLS-1$	
 				// The order on the stack is right then left operand.
 				// First the right operand
 				Object value = popExpression();
 				Class type = popExpressionType(false);
 
 				// Next the left operand, should be a reference.
 				VariableReference left = (VariableReference) popExpression(false); // Don't dereference it.
 				refType = popExpressionType(false);
 
 				if (traceOn)
 					printObjectAndType(left, refType);
 
 				leftValue = left.set(value, type);
 			} finally {
 				if (traceOn)
 					printTraceEnd();
 			}
 			
 			// Now do assignment and return the value to the stack.
 			pushExpression(leftValue, refType);	// The type of the result is the type of the reference.
 
 		} catch (IllegalArgumentException e) {
 			processException(e);
 		} catch (NoExpressionValueException e) {
 			processSyntaxException(e);
 		} catch (IllegalAccessException e) {
 			processException(e);
 		} catch (RuntimeException e) {
 			processException(e);
 		}
 			
 	}
 	
 	/**
 	 * Assign the expression proxy to the top expression value.
 	 * 
 	 * @param proxy
 	 * 
 	 * @since 1.1.0
 	 */
 	public final void pushAssignment(InternalExpressionProxy proxy) {
 		boolean ignore = (ignoreExpression != null || errorOccurred);
 		try {
 			if (traceOn) {
 				printTrace("Assign to Proxy #"+proxy.getProxyID(), ignore); //$NON-NLS-1$
 			}
 			if (ignore)
 				return;
 			
 			try {
 				assignToExpressionProxyFromTopStackEntry(proxy);
 				if (traceOn)
 					printObjectAndType(proxy.getValue(), proxy.getType());
 			} catch (NoExpressionValueException e) {
 				processSyntaxException(e);
 			} catch (RuntimeException e) {
 				processException(e);
 			}
 		} finally {
 			if (traceOn)
 				printTraceEnd();
 		}
 
 	}
 
 	/**
 	 * Assign the top stack entry to the new expression proxy and allocate it for callback later.
 	 * @param proxy
 	 * @throws NoExpressionValueException
 	 * 
 	 * @since 1.1.0
 	 */
 	protected void assignToExpressionProxyFromTopStackEntry(InternalExpressionProxy proxy) throws NoExpressionValueException {
 		Object value = getExpression(1);
 		Class type = getExpressionType(1, true);
 		if (value instanceof VariableReference)
 			value = ((VariableReference) value).dereference();	// Here we want the final current value.
 
 		proxy.setProxy(value, type);
 		allocateExpressionProxy(proxy);
 	}
 
 	/**
 	 * Allocate an expression proxy. This is used to make an expression proxy known to the processor. The expression proxy must
 	 * have been setProxy() at this point. This is used to assign from the top of the stack or to add from outside an evaluated proxy
 	 * to be used later by others.
 	 * 
 	 * @param proxy
 	 * 
 	 * @since 1.1.0
 	 */
 	public void allocateExpressionProxy(InternalExpressionProxy proxy) {
 		int minSize = proxy.getProxyID()+1;
 		if (expressionProxies == null)
 			expressionProxies = new ArrayList(minSize+10);	// Allow room to grow ten more.
 		else if (expressionProxies.size() < minSize)
 			expressionProxies.ensureCapacity(minSize+10);	// Allow room to grow ten more.
 		int fill = minSize-expressionProxies.size();	// Number of "null" fill entries needed. Probably shouldn't occur, but to be safe.
 		if (fill > 0) {
 			while (--fill > 0)
 				expressionProxies.add(null);
 			expressionProxies.add(proxy);
 		} else
 			expressionProxies.set(proxy.getProxyID(), proxy);	// Already large enough, replace entry.
 
 	}
 
 	/**
 	 * The primitive enums. 
 	 * NOTE: Their order must not changed. They are in order of permitted widening.
 	 * 
 	 */
 	protected static final int
 		BOOLEAN = 0,
 		BYTE = 1,
 		SHORT = 2,	
 		CHAR = 3,
 		INT = 4,
 		LONG = 5,
 		FLOAT = 6,
 		DOUBLE = 7;
 		
 		
 	
 	/**
 	 * Get the enum constant for the type of primitive passed in.
 	 * <p>
 	 * This is a helper method for expression processer to get the enum for the primitive type. Since it is a helper method it doesn't
 	 * check nor process the exception. It throws it. Callers must handle it as they see fit.
 
 	 * @param primitiveType
 	 * @return
 	 * @throws IllegalArgumentException if type is not a primitive.
 	 * 
 	 * @see ExpressionProcesser#BOOLEAN
 	 * @since 1.0.0
 	 */
 	protected final int getEnumForPrimitive(Class primitiveType) throws IllegalArgumentException {
 		if (primitiveType == Boolean.TYPE)
 			return BOOLEAN;
 		else if (primitiveType == Integer.TYPE)
 			return INT;
 		else if (primitiveType == Byte.TYPE)
 			return BYTE;
 		else if (primitiveType == Character.TYPE)
 			return CHAR;
 		else if (primitiveType == Double.TYPE)
 			return DOUBLE;
 		else if (primitiveType == Float.TYPE)
 			return FLOAT;
 		else if (primitiveType == Long.TYPE)
 			return LONG;
 		else if (primitiveType == Short.TYPE)
 			return SHORT;
 		else
 			throw new IllegalArgumentException(primitiveType != null ? primitiveType.getName() : "null"); //$NON-NLS-1$
 	}
 	
 	private void throwInvalidPrefix(PrefixOperator operator, Object exp) throws IllegalArgumentException {
 		throw new IllegalArgumentException(MessageFormat.format(InitparserTreeMessages.getString("ExpressionProcesser.InvalidOperandOfPrefixOperator_EXC_"), new Object[] {exp != null ? exp.toString() : null, operator.toString()})); //$NON-NLS-1$
 	}		
 	
 	private static final Object INFIX_IGNORE = "INFIX IGNORE";	// Flag for infix in ingore //$NON-NLS-1$
 	private int infixNesting = 0;	// Nesting of infix expressions.
 	private int infixIgnoreNestCount = 0;	// When ignoring infix expressions, ignore until this nest count.
 	/**
 	 * Push the infix expression onto the stack.
 	 * @param operator
 	 * @param operandType The operator type. Left, right, other.
 	 * @since 1.0.0
 	 */
 	public final void pushInfix(InfixOperator operator, InternalInfixOperandType operandType) {
 		try {
 			boolean ignore = true;
 			try {
 				if (errorOccurred) {
 					return;
 				}
 				// Slightly different here in that if an ignored occurred we still need to process at least part of it so that
 				// we can get the expression grouping correct.
 				if (operandType == InternalInfixOperandType.INFIX_LEFT_OPERAND)
 					infixNesting++;
 				else if (operandType == InternalInfixOperandType.INFIX_LAST_OPERAND) {
 					int currentNest = infixNesting--;
 					if (ignoreExpression == INFIX_IGNORE && currentNest == infixIgnoreNestCount) {
 						// We were ignoring, and it was this expression that was being ignore.
 						// We have received the last operand of the nested expression that was being ignored,
 						// so we can stop ignoring. But we still leave since the value of the expression is on the
 						// top of the stack.
 						ignoreExpression = null;
 						return;
 					}
 				}
 	
 				if (ignoreExpression != null)
 					return;
 				ignore = false;
 			} finally {
 				if (traceOn)
 					printTrace("Infix: "+operator, ignore); //$NON-NLS-1$
 			}
 			
 			try {
 				Object right = null;
 				Class rightType = null;
 				if (operandType != InternalInfixOperandType.INFIX_LEFT_OPERAND) {
 					// We are not the left operand, so the stack has the right on the top, followed by the left.
 					right = popExpression();
 					rightType = popExpressionType(false);
 				} 
 				
 				Object value = popExpression();
 				Class valueType = popExpressionType(false);
 	
 				switch (operator.getValue()) {
 					case InfixOperator.IN_AND_VALUE:
 						if (operandType == InternalInfixOperandType.INFIX_LEFT_OPERAND)
 							break;	// Do nothing with first operand
 						
 						testValidBitType(valueType, InfixOperator.IN_AND);
 						testValidBitType(rightType, InfixOperator.IN_AND);
 						if (valueType == Long.TYPE || rightType == Long.TYPE) {
 							// If either side is long, the result will be long.
 							value = new Long(getLong(value) & getLong(right));
 							valueType = Long.TYPE;
 						} else {
 							// Else it is int. (even two shorts together produce an int).
 							value = new Integer(getInt(value) & getInt(right));
 							valueType = Integer.TYPE;
 						}
 						break;
 					case InfixOperator.IN_CONDITIONAL_AND_VALUE:
 						// This is tricky.
 						// First if this is left type, then just continue.
 						// Else if this other or last, then need to make it the new value.
 						if (operandType != InternalInfixOperandType.INFIX_LEFT_OPERAND) {
 							value = right;
 							valueType = rightType;
 						}
 							
 						//If the value is now false, we need to ignore the rest.
 						if (valueType != Boolean.TYPE)
 							throwInvalidInfix(operator, value);
 						if (!((Boolean) value).booleanValue() && operandType != InternalInfixOperandType.INFIX_LAST_OPERAND)
 							startInfixIgnore();	// Start ignoring because we know the value of the expression at this point. It is false.
 						break;
 					case InfixOperator.IN_CONDITIONAL_OR_VALUE:
 						// This is tricky.
 						// First if this is left type, then just continue.
 						// Else if this other or last, then need to make it the new value.
 						if (operandType != InternalInfixOperandType.INFIX_LEFT_OPERAND) {
 							value = right;
 							valueType = rightType;
 						}
 						
 						//If the value is now true, we need to ignore the rest.
 						if (valueType != Boolean.TYPE)
 							throwInvalidInfix(operator, value);
 						if (((Boolean) value).booleanValue() && operandType != InternalInfixOperandType.INFIX_LAST_OPERAND)
 							startInfixIgnore(); // Start ignoring because we know the value of the expression at this point. It is true.
 						break;
 					case InfixOperator.IN_DIVIDE_VALUE:
 						if (operandType == InternalInfixOperandType.INFIX_LEFT_OPERAND)
 							break;	// Do nothing with first operand
 						
 						testValidArithmeticType(valueType, InfixOperator.IN_DIVIDE);
 						testValidArithmeticType(rightType, InfixOperator.IN_DIVIDE);
 						if (valueType == Double.TYPE || rightType == Double.TYPE) {
 							// If either side is double, the result will be double.
 							value = new Double(getDouble(value) / getDouble(right));
 							valueType = Double.TYPE;
 						} else if (valueType == Float.TYPE || rightType == Float.TYPE) {
 							// If either side is float, the result will be float.
 							value = new Float(getFloat(value) / getFloat(right));
 							valueType = Float.TYPE;
 						} else if (valueType == Long.TYPE || rightType == Long.TYPE) {
 							// If either side is long, the result will be long.
 							value = new Long(getLong(value) / getLong(right));
 							valueType = Long.TYPE;
 						} else {
 							// Else it will result in an int, even if both sides are short.
 							value = new Integer(getInt(value) / getInt(right));
 							valueType = Integer.TYPE;
 						}
 						break;
 					case InfixOperator.IN_EQUALS_VALUE:
 						if (operandType == InternalInfixOperandType.INFIX_LEFT_OPERAND)
 							break;	// Do nothing with first operand
 						// We should never get extended operator for this, but we'll ignore the possibility.
 						if (valueType.isPrimitive() && rightType.isPrimitive()) {
 							// Primitives require more testing than just ==. boolean primitives 
 							if (valueType == Boolean.TYPE || rightType == Boolean.TYPE) {
 								// If either side is a boolean, then the other side needs to be boolean for it to even try to be true.
 								if (valueType != Boolean.TYPE || valueType != Boolean.TYPE)
 									value = Boolean.FALSE;
 								else
 									value = (((Boolean) value).booleanValue() == ((Boolean) right).booleanValue()) ? Boolean.TRUE : Boolean.FALSE;
 							} else {
 								// Now do number tests since not boolean primitive, only numbers are left
 								if (valueType == Double.TYPE || rightType == Double.TYPE) {
 									// If either side is double, compare as double.
 									value = (getDouble(value) == getDouble(right)) ? Boolean.TRUE : Boolean.FALSE;
 								} else if (valueType == Float.TYPE || rightType == Float.TYPE) {
 									// If either side is float, compare as float.
 									value = (getFloat(value) == getFloat(right)) ? Boolean.TRUE : Boolean.FALSE;
 								} else if (valueType == Long.TYPE || rightType == Long.TYPE) {
 									// If either side is long, the compare as long.
 									value = (getLong(value) == getLong(right)) ? Boolean.TRUE : Boolean.FALSE;
 								} else {
 									// Else it will compare as int, even if both sides are short.
 									value = (getInt(value) == getInt(right)) ? Boolean.TRUE : Boolean.FALSE;
 								}
 							}
 						} else if (valueType.isPrimitive() || rightType.isPrimitive())
 							value = Boolean.FALSE;	// Can't be true if one side prim and the other isn't
 						else {
 							// Just do object ==
 							value = (value == right) ? Boolean.TRUE : Boolean.FALSE;
 						}
 						valueType = Boolean.TYPE;	// We know result will be a boolean.
 						break;
 					case InfixOperator.IN_GREATER_VALUE:
 						if (operandType == InternalInfixOperandType.INFIX_LEFT_OPERAND)
 							break;	// Do nothing with first operand
 						
 						testValidArithmeticType(valueType, InfixOperator.IN_GREATER);
 						testValidArithmeticType(rightType, InfixOperator.IN_GREATER);
 						if (valueType == Double.TYPE || rightType == Double.TYPE) {
 							// If either side is double, compare will be double.
 							value = (getDouble(value) > getDouble(right)) ? Boolean.TRUE : Boolean.FALSE;
 						} else if (valueType == Float.TYPE || rightType == Float.TYPE) {
 							// If either side is float, compare will be float.
 							value = (getFloat(value) > getFloat(right)) ? Boolean.TRUE : Boolean.FALSE;
 						} else if (valueType == Long.TYPE || rightType == Long.TYPE) {
 							// If either side is long, compare will be long.
 							value = (getLong(value) > getLong(right)) ? Boolean.TRUE : Boolean.FALSE;
 						} else {
 							// Else compare will be int, even if both sides are short.
 							value = (getInt(value) > getInt(right)) ? Boolean.TRUE : Boolean.FALSE;
 						}
 						valueType = Boolean.TYPE;	// We know result will be a boolean.
 						break;
 					case InfixOperator.IN_GREATER_EQUALS_VALUE:
 						if (operandType == InternalInfixOperandType.INFIX_LEFT_OPERAND)
 							break;	// Do nothing with first operand
 						
 						testValidArithmeticType(valueType, InfixOperator.IN_GREATER_EQUALS);
 						testValidArithmeticType(rightType, InfixOperator.IN_GREATER_EQUALS);
 						if (valueType == Double.TYPE || rightType == Double.TYPE) {
 							// If either side is double, compare will be double.
 							value = (getDouble(value) >= getDouble(right)) ? Boolean.TRUE : Boolean.FALSE;
 						} else if (valueType == Float.TYPE || rightType == Float.TYPE) {
 							// If either side is float, compare will be float.
 							value = (getFloat(value) >= getFloat(right)) ? Boolean.TRUE : Boolean.FALSE;
 						} else if (valueType == Long.TYPE || rightType == Long.TYPE) {
 							// If either side is long, compare will be long.
 							value = (getLong(value) >= getLong(right)) ? Boolean.TRUE : Boolean.FALSE;
 						} else {
 							// Else compare will be int, even if both sides are short.
 							value = (getInt(value) >= getInt(right)) ? Boolean.TRUE : Boolean.FALSE;
 						}
 						valueType = Boolean.TYPE;	// We know result will be a boolean.
 						break;
 					case InfixOperator.IN_LEFT_SHIFT_VALUE:
 						if (operandType == InternalInfixOperandType.INFIX_LEFT_OPERAND)
 							break;	// Do nothing with first operand
 						
 						testValidBitType(valueType, InfixOperator.IN_LEFT_SHIFT);
 						testValidBitType(rightType, InfixOperator.IN_LEFT_SHIFT);
 						if (valueType == Long.TYPE || rightType == Long.TYPE) {
 							// If either side is long, the result will be long.
 							value = new Long(getLong(value) << getLong(right));
 							valueType = Long.TYPE;
 						} else {
 							// Else it is int. (even two shorts together produce an int).
 							value = new Integer(getInt(value) << getInt(right));
 							valueType = Integer.TYPE;
 						}
 						break;
 					case InfixOperator.IN_LESS_VALUE:
 						if (operandType == InternalInfixOperandType.INFIX_LEFT_OPERAND)
 							break;	// Do nothing with first operand
 						
 						testValidArithmeticType(valueType, InfixOperator.IN_LESS);
 						testValidArithmeticType(rightType, InfixOperator.IN_LESS);
 						if (valueType == Double.TYPE || rightType == Double.TYPE) {
 							// If either side is double, compare will be double.
 							value = (getDouble(value) < getDouble(right)) ? Boolean.TRUE : Boolean.FALSE;
 						} else if (valueType == Float.TYPE || rightType == Float.TYPE) {
 							// If either side is float, compare will be float.
 							value = (getFloat(value) < getFloat(right)) ? Boolean.TRUE : Boolean.FALSE;
 						} else if (valueType == Long.TYPE || rightType == Long.TYPE) {
 							// If either side is long, compare will be long.
 							value = (getLong(value) < getLong(right)) ? Boolean.TRUE : Boolean.FALSE;
 						} else {
 							// Else compare will be int, even if both sides are short.
 							value = (getInt(value) < getInt(right)) ? Boolean.TRUE : Boolean.FALSE;
 						}
 						valueType = Boolean.TYPE;	// We know result will be a boolean.
 						break;
 					case InfixOperator.IN_LESS_EQUALS_VALUE:
 						if (operandType == InternalInfixOperandType.INFIX_LEFT_OPERAND)
 							break;	// Do nothing with first operand
 						
 						testValidArithmeticType(valueType, InfixOperator.IN_LESS_EQUALS);
 						testValidArithmeticType(rightType, InfixOperator.IN_LESS_EQUALS);
 						if (valueType == Double.TYPE || rightType == Double.TYPE) {
 							// If either side is double, compare will be double.
 							value = (getDouble(value) <= getDouble(right)) ? Boolean.TRUE : Boolean.FALSE;
 						} else if (valueType == Float.TYPE || rightType == Float.TYPE) {
 							// If either side is float, compare will be float.
 							value = (getFloat(value) <= getFloat(right)) ? Boolean.TRUE : Boolean.FALSE;
 						} else if (valueType == Long.TYPE || rightType == Long.TYPE) {
 							// If either side is long, compare will be long.
 							value = (getLong(value) <= getLong(right)) ? Boolean.TRUE : Boolean.FALSE;
 						} else {
 							// Else compare will be int, even if both sides are short.
 							value = (getInt(value) <= getInt(right)) ? Boolean.TRUE : Boolean.FALSE;
 						}
 						valueType = Boolean.TYPE;	// We know result will be a boolean.
 						break;
 					case InfixOperator.IN_MINUS_VALUE:
 						if (operandType == InternalInfixOperandType.INFIX_LEFT_OPERAND)
 							break;	// Do nothing with first operand
 						
 						testValidArithmeticType(valueType, InfixOperator.IN_MINUS);
 						testValidArithmeticType(rightType, InfixOperator.IN_MINUS);
 						if (valueType == Double.TYPE || rightType == Double.TYPE) {
 							// If either side is double, the result will be double.
 							value = new Double(getDouble(value) - getDouble(right));
 							valueType = Double.TYPE;
 						} else if (valueType == Float.TYPE || rightType == Float.TYPE) {
 							// If either side is float, the result will be float.
 							value = new Float(getFloat(value) - getFloat(right));
 							valueType = Float.TYPE;
 						} else if (valueType == Long.TYPE || rightType == Long.TYPE) {
 							// If either side is long, the result will be long.
 							value = new Long(getLong(value) - getLong(right));
 							valueType = Long.TYPE;
 						} else {
 							// Else it will result in an int, even if both sides are short.
 							value = new Integer(getInt(value) - getInt(right));
 							valueType = Integer.TYPE;
 						}
 						break;
 					case InfixOperator.IN_NOT_EQUALS_VALUE:
 						if (operandType == InternalInfixOperandType.INFIX_LEFT_OPERAND)
 							break;	// Do nothing with first operand
 						// We should never get extended operator for this, but we'll ignore the possibility.
 						if (valueType.isPrimitive() && rightType.isPrimitive()) {
 							// Primitives require more testing than just ==. boolean primitives 
 							if (valueType == Boolean.TYPE || rightType == Boolean.TYPE) {
 								// If either side is a boolean, then the other side needs to be boolean for it to even try to be true.
 								if (valueType != Boolean.TYPE || valueType != Boolean.TYPE)
 									value = Boolean.TRUE;
 								else
 									value = (((Boolean) value).booleanValue() != ((Boolean) right).booleanValue()) ? Boolean.TRUE : Boolean.FALSE;
 							} else {
 								// Now do number tests since not boolean primitive, only numbers are left
 								if (valueType == Double.TYPE || rightType == Double.TYPE) {
 									// If either side is double, compare as double.
 									value = (getDouble(value) != getDouble(right)) ? Boolean.TRUE : Boolean.FALSE;
 								} else if (valueType == Float.TYPE || rightType == Float.TYPE) {
 									// If either side is float, compare as float.
 									value = (getFloat(value) != getFloat(right)) ? Boolean.TRUE : Boolean.FALSE;
 								} else if (valueType == Long.TYPE || rightType == Long.TYPE) {
 									// If either side is long, the compare as long.
 									value = (getLong(value) != getLong(right)) ? Boolean.TRUE : Boolean.FALSE;
 								} else {
 									// Else it will compare as int, even if both sides are short.
 									value = (getInt(value) != getInt(right)) ? Boolean.TRUE : Boolean.FALSE;
 								}
 							}
 						} else if (valueType.isPrimitive() || rightType.isPrimitive())
 							value = Boolean.TRUE;	// Must be true if one side prim and the other isn't
 						else {
 							// Just do object !=
 							value = (value != right) ? Boolean.TRUE : Boolean.FALSE;
 						}
 						valueType = Boolean.TYPE;	// We know result will be a boolean.
 						break;
 					case InfixOperator.IN_OR_VALUE:
 						if (operandType == InternalInfixOperandType.INFIX_LEFT_OPERAND)
 							break;	// Do nothing with first operand
 						
 						testValidBitType(valueType, InfixOperator.IN_OR);
 						testValidBitType(rightType, InfixOperator.IN_OR);
 						if (valueType == Long.TYPE || rightType == Long.TYPE) {
 							// If either side is long, the result will be long.
 							value = new Long(getLong(value) | getLong(right));
 							valueType = Long.TYPE;
 						} else {
 							// Else it is int. (even two shorts together produce an int).
 							value = new Integer(getInt(value) | getInt(right));
 							valueType = Integer.TYPE;
 						}
 						break;
 					case InfixOperator.IN_PLUS_VALUE:
 						if (operandType == InternalInfixOperandType.INFIX_LEFT_OPERAND) {
 							if (valueType == String.class) {
 								// Special. left argument is a string, so we want to store a string buffer instead
 								// since we know we will be appending to it. 
 								value = new StringBuffer((String) value);
 							}	
 							break;	// Do nothing with first operand
 						}
 						
 						testValidPlusType(valueType, rightType);
 						if (valueType == String.class || rightType == String.class) {
 							// Special we have a string on one side. Need to do it as strings instead.
 							// We are going to be tricky in that we will store a StringBuffer on the stack (if not last operand)
 							// but call it a string.
 							StringBuffer sb = null;
 							if (valueType == String.class) {
 								sb = (StringBuffer) value;	// We know that if the value (left) is string type, we've already converted it to buffer.
 							} else {
 								// The right is the one that introduces the string, so we change the value over to a string buffer.
 								sb = new StringBuffer(((String) right).length()+16);	// We can't put the value in yet, need to get left into it.
 								appendToBuffer(sb, value, valueType);	// Put the left value in now
 								value = sb;
 								valueType = String.class;	// Make it a string class
 							}
 							appendToBuffer(sb, right, rightType);
 							// Now if we are the last operand, we should get rid of the buffer and put a true string back in.
 							if (operandType == InternalInfixOperandType.INFIX_LAST_OPERAND)
 								value = sb.toString();
 						} else if (valueType == Double.TYPE || rightType == Double.TYPE) {
 							// If either side is double, the result will be double.
 							value = new Double(getDouble(value) + getDouble(right));
 							valueType = Double.TYPE;
 						} else if (valueType == Float.TYPE || rightType == Float.TYPE) {
 							// If either side is float, the result will be float.
 							value = new Float(getFloat(value) + getFloat(right));
 							valueType = Float.TYPE;
 						} else if (valueType == Long.TYPE || rightType == Long.TYPE) {
 							// If either side is long, the result will be long.
 							value = new Long(getLong(value) + getLong(right));
 							valueType = Long.TYPE;
 						} else {
 							// Else it will result in an int, even if both sides are short.
 							value = new Integer(getInt(value) + getInt(right));
 							valueType = Integer.TYPE;
 						}
 						break;
 					case InfixOperator.IN_REMAINDER_VALUE:
 						if (operandType == InternalInfixOperandType.INFIX_LEFT_OPERAND)
 							break;	// Do nothing with first operand
 						
 						testValidArithmeticType(valueType, InfixOperator.IN_REMAINDER);
 						testValidArithmeticType(rightType, InfixOperator.IN_REMAINDER);
 						if (valueType == Double.TYPE || rightType == Double.TYPE) {
 							// If either side is double, the result will be double.
 							value = new Double(getDouble(value) % getDouble(right));
 							valueType = Double.TYPE;
 						} else if (valueType == Float.TYPE || rightType == Float.TYPE) {
 							// If either side is float, the result will be float.
 							value = new Float(getFloat(value) % getFloat(right));
 							valueType = Float.TYPE;
 						} else if (valueType == Long.TYPE || rightType == Long.TYPE) {
 							// If either side is long, the result will be long.
 							value = new Long(getLong(value) % getLong(right));
 							valueType = Long.TYPE;
 						} else {
 							// Else it will result in an int, even if both sides are short.
 							value = new Integer(getInt(value) % getInt(right));
 							valueType = Integer.TYPE;
 						}
 						break;
 					case InfixOperator.IN_RIGHT_SHIFT_SIGNED_VALUE:
 						if (operandType == InternalInfixOperandType.INFIX_LEFT_OPERAND)
 							break;	// Do nothing with first operand
 						
 						testValidBitType(valueType, InfixOperator.IN_RIGHT_SHIFT_SIGNED);
 						testValidBitType(rightType, InfixOperator.IN_RIGHT_SHIFT_SIGNED);
 						if (valueType == Long.TYPE || rightType == Long.TYPE) {
 							// If either side is long, the result will be long.
 							value = new Long(getLong(value) >> getLong(right));
 							valueType = Long.TYPE;
 						} else {
 							// Else it is int. (even two shorts together produce an int).
 							value = new Integer(getInt(value) >> getInt(right));
 							valueType = Integer.TYPE;
 						}
 						break;
 					case InfixOperator.IN_RIGHT_SHIFT_UNSIGNED_VALUE:
 						if (operandType == InternalInfixOperandType.INFIX_LEFT_OPERAND)
 							break;	// Do nothing with first operand
 						
 						testValidBitType(valueType, InfixOperator.IN_RIGHT_SHIFT_UNSIGNED);
 						testValidBitType(rightType, InfixOperator.IN_RIGHT_SHIFT_UNSIGNED);
 						if (valueType == Long.TYPE || rightType == Long.TYPE) {
 							// If either side is long, the result will be long.
 							value = new Long(getLong(value) >>> getLong(right));
 							valueType = Long.TYPE;
 						} else {
 							// Else it is int. (even two shorts together produce an int).
 							value = new Integer(getInt(value) >>> getInt(right));
 							valueType = Integer.TYPE;
 						}
 						break;
 					case InfixOperator.IN_TIMES_VALUE:
 						if (operandType == InternalInfixOperandType.INFIX_LEFT_OPERAND)
 							break;	// Do nothing with first operand
 						
 						testValidArithmeticType(valueType, InfixOperator.IN_TIMES);
 						testValidArithmeticType(rightType, InfixOperator.IN_TIMES);
 						if (valueType == Double.TYPE || rightType == Double.TYPE) {
 							// If either side is double, the result will be double.
 							value = new Double(getDouble(value) * getDouble(right));
 							valueType = Double.TYPE;
 						} else if (valueType == Float.TYPE || rightType == Float.TYPE) {
 							// If either side is float, the result will be float.
 							value = new Float(getFloat(value) * getFloat(right));
 							valueType = Float.TYPE;
 						} else if (valueType == Long.TYPE || rightType == Long.TYPE) {
 							// If either side is long, the result will be long.
 							value = new Long(getLong(value) * getLong(right));
 							valueType = Long.TYPE;
 						} else {
 							// Else it will result in an int, even if both sides are short.
 							value = new Integer(getInt(value) * getInt(right));
 							valueType = Integer.TYPE;
 						}
 						break;
 					case InfixOperator.IN_XOR_VALUE:
 						if (operandType == InternalInfixOperandType.INFIX_LEFT_OPERAND)
 							break;	// Do nothing with first operand
 						
 						testValidBitType(valueType, InfixOperator.IN_XOR);
 						testValidBitType(rightType, InfixOperator.IN_XOR);
 						if (valueType == Long.TYPE || rightType == Long.TYPE) {
 							// If either side is long, the result will be long.
 							value = new Long(getLong(value) ^ getLong(right));
 							valueType = Long.TYPE;
 						} else {
 							// Else it is int. (even two shorts together produce an int).
 							value = new Integer(getInt(value) ^ getInt(right));
 							valueType = Integer.TYPE;
 						}
 						break;
 					} 
 				
 				if (traceOn)
 					printObjectAndType(value, valueType);
 				pushExpressionValue(value, valueType);	// Push the result back on the stack.
 	
 			} catch (IllegalArgumentException e) {
 				processException(e);
 			} catch (NoExpressionValueException e) {
 				processSyntaxException(e);
 			} catch (RuntimeException e) {
 				processException(e);
 			}
 		} finally {
 			if (traceOn)
 				printTraceEnd();
 		}
 			
 	}
 
 	/**
 	 * Start ignoring rest of the current infix expression.
 	 * 
 	 * @since 1.1.0
 	 */
 	private void startInfixIgnore() {
 		ignoreExpression = INFIX_IGNORE;
 		infixIgnoreNestCount = infixNesting;	// Ignore until we get back to the current nesting.
 
 	}
 
 	/**
 	 * Get int value of the primitive wrapper bean passed in (must be either a <code>Number/code> or <code>Character</code>.
 	 * Anything else will cause a class cast error.
 	 * <p>
 	 * This is a helper method for expression processer to get the int value of the object. Since it is a helper method it doesn't
 	 * check nor process the exception. It throws it. Callers must handle it as they see fit.
 	 * @param bean
 	 * @return the int value of the number/character
 	 * @throws ClassCastException
 	 * 
 	 * @since 1.0.0
 	 */
 	protected final int getInt(Object bean) throws ClassCastException {
 		return (bean instanceof Number) ? ((Number) bean).intValue() : ((Character) bean).charValue();
 	}
 	
 	/**
 	 * Get float value of the primitive wrapper bean passed in (must be either a <code>Number/code> or <code>Character</code>.
 	 * Anything else will cause a class cast error.
 	 * <p>
 	 * This is a helper method for expression processer to get the float value of the object. Since it is a helper method it doesn't
 	 * check nor process the exception. It throws it. Callers must handle it as they see fit.
 	 * @param bean 
 	 * @return float value of the Number/character
 	 * @throws ClassCastException
 	 * @since 1.0.0
 	 */
 	protected final float getFloat(Object bean) throws ClassCastException {
 		return (bean instanceof Number) ? ((Number) bean).floatValue() : ((Character) bean).charValue();
 	}
 
 	/**
 	 * Get double value of the primitive wrapper bean passed in (must be either a <code>Number/code> or <code>Character</code>.
 	 * Anything else will cause a class cast error.
 	 * <p>
 	 * This is a helper method for expression processer to get the float value of the object. Since it is a helper method it doesn't
 	 * check nor process the exception. It throws it. Callers must handle it as they see fit.
 	 * 
 	 * @param bean
 	 * @return double value of the Number/Character.
 	 * @throws ClassCastException
 	 * @since 1.0.0
 	 */
 	protected final double getDouble(Object bean) throws ClassCastException {
 		return (bean instanceof Number) ? ((Number) bean).doubleValue() : ((Character) bean).charValue();
 	}
 	
 	/**
 	 * Get long value of the primitive wrapper bean passed in (must be either a <code>Number/code> or <code>Character</code>.
 	 * Anything else will cause a class cast error.
 	 * <p>
 	 * This is a helper method for expression processer to get the float value of the object. Since it is a helper method it doesn't
 	 * check nor process the exception. It throws it. Callers must handle it as they see fit.
 	 * 
 	 * @param bean
 	 * @return
 	 * @throws ClassCastException
 	 * @since 1.0.0
 	 */
 	protected final long getLong(Object bean) throws ClassCastException {
 		return (bean instanceof Number) ? ((Number) bean).longValue() : ((Character) bean).charValue();
 	}
 	
 	private void throwInvalidInfix(InfixOperator operator, Object value) throws IllegalArgumentException {
 		throw new IllegalArgumentException(MessageFormat.format(InitparserTreeMessages.getString("ExpressionProcesser.InvalidOperandOfOperator_EXC_"), new Object[] {value != null ? value.toString() : null, operator.toString()})); //$NON-NLS-1$
 	}
 	
 	private void testValidBitType(Class type, InfixOperator operator) {
 		if (!type.isPrimitive() || type == Boolean.TYPE || type == Double.TYPE|| type == Float.TYPE)
 			throwInvalidInfix(operator, type);
 	}
 	
 	private void testValidArithmeticType(Class type, InfixOperator operator) {
 		if (!type.isPrimitive() || type == Boolean.TYPE)
 			throwInvalidInfix(operator, type);
 	}
 
 	private void testValidPlusType(Class left, Class right) {
 		// Plus is special in that string objects are also valid.
 		if (left == String.class || right == String.class)
 			return;	// As long as one side is string. Anything is valid.
 		// If neither is string, then standard arithmetic test.
 		testValidArithmeticType(left, InfixOperator.IN_PLUS);
 		testValidArithmeticType(right, InfixOperator.IN_PLUS);
 	}
 	
 	private void appendToBuffer(StringBuffer sb, Object value, Class valueType) {
 		if (value == null)
 			sb.append((Object)null);
 		else if (valueType == String.class)
 			sb.append((String) value);
 		else if (valueType.isPrimitive()) {
 			switch (getEnumForPrimitive(valueType)) {
 				case BOOLEAN:
 					sb.append(((Boolean) value).booleanValue());
 					break;
 				case BYTE:
 					sb.append(((Number) value).byteValue());
 					break;
 				case CHAR:
 					sb.append(((Character) value).charValue());
 					break;
 				case DOUBLE:
 					sb.append(((Number) value).doubleValue());
 					break;
 				case FLOAT:
 					sb.append(((Number) value).floatValue());
 					break;
 				case INT:
 					sb.append(((Number) value).intValue());
 					break;
 				case LONG:
 					sb.append(((Number) value).longValue());
 					break;
 				case SHORT:
 					sb.append(((Number) value).shortValue());
 					break;		
 			}
 		} else {
 			// Just an object.
 			sb.append(value);
 		}
 	}
 	
 	/**
 	 * Push the array access expression.
 	 * 
 	 * @param indexCount Number of dimensions being accessed
 	 * @since 1.0.0
 	 */
 	public final void pushArrayAccess(int indexCount) {
 		boolean ignore = (ignoreExpression != null || errorOccurred);
 		
 		if (traceOn) {
 			printTrace("Array Access["+indexCount+']', ignore); //$NON-NLS-1$
 		}
 		try {
 			if (ignore)
 				return;
 			
 			try {
 				// We need to pop off the args. The topmost will be the rightmost index, and the bottom most will be the array itself.
 				int[] arguments = new int[indexCount];
 				// Fill the arg array in reverse order.
 				for(int i=indexCount-1; i >= 0; i--) {
 					Object index = popExpression();
 					Class indexType = popExpressionType(false);
 					if (indexType.isPrimitive() && (indexType == Integer.TYPE || indexType == Short.TYPE || indexType == Character.TYPE || indexType == Byte.TYPE)) {
 						arguments[i] = getInt(index);
 					} else
 						throwClassCast(Integer.TYPE, index);
 				}
 				
 				Object array = popExpression();
 				Class arrayType = popExpressionType(false);
 				if (arrayType.isArray()) {
 					// First figure out how many dimensions are available. Stop when we hit indexcount because we won't be going further.
 					int dimcount = 0;
 					Class[] componentTypes = new Class[indexCount];	// 
 					Class componentType = arrayType;
 					while (dimcount < indexCount && componentType.isArray()) {
 						componentTypes[dimcount++] = componentType = componentType.getComponentType();
 					}
 					
 					if (dimcount < indexCount)
 						throw new IllegalArgumentException(MessageFormat.format(InitparserTreeMessages.getString("ExpressionProcesser.XIsGreaterThanNumberOfDimensionsInArray_EXC_"), new Object[] {new Integer(indexCount), new Integer(dimcount)})); //$NON-NLS-1$
 					
 					// Now start accessing one index at a time, stop just before the last one. The last one will be turned into an ArrayAccessReference.
 					Object value = array;	// Final value, start with full array.
 					int pullCount = indexCount-1;
 					for(int i=0; i<pullCount; i++) {
 						value = Array.get(value, arguments[i]);
 					}
 					ArrayAccessReference arrayValue = ArrayAccessReference.createArrayAccessReference(value, arguments[pullCount]);
 					if (traceOn)
 						printObjectAndType(arrayValue, componentTypes[pullCount]);
 					pushExpressionValue(arrayValue, componentTypes[pullCount]);
 				}  else
 					 throw new IllegalArgumentException(MessageFormat.format(InitparserTreeMessages.getString("ExpressionProcesser.NotAnArray_EXC_"), new Object[] {arrayType})); //$NON-NLS-1$
 	
 			} catch (NoExpressionValueException e) {
 				processSyntaxException(e);
 			} catch (RuntimeException e) {
 				processException(e);
 			}
 		} finally {
 			if (traceOn)
 				printTraceEnd();
 		}
 	}
 	
 	/**
 	 * Push the array creation request.
 	 * 
 	 * @param arrayType The type of the array
 	 * @param dimensionCount The number of dimensions being initialized. Zero if using an initializer.
 	 * @since 1.0.0
 	 */
 	public final void pushArrayCreation(Class arrayType, int dimensionCount) {
 		boolean ignore = (ignoreExpression != null || errorOccurred);
 		if (traceOn)
 			printTrace("Array Creation: "+arrayType.getName()+'['+dimensionCount+']', ignore); //$NON-NLS-1$
 		
 		try {
 			if (ignore)
 				return;
 			
 			try {
 				if (dimensionCount == 0) {
 					// The top value is the array itself, from the array initializer.
 					// So we do nothing.
 				} else {
 					// Strip off dimensionCounts from the array type, e.g.
 					// ArrayType is int[][][]
 					// Dimensioncount is 2
 					// Then we need to strip two componenttypes off of the array type
 					// wind up with int[]
 					// This is necessary because Array.new will add those dimensions back
 					// on through the dimension count.
 					Class componentType = arrayType;
 					for(int i=0; i < dimensionCount && componentType != null; i++)
 						componentType = componentType.getComponentType();
 					if (componentType == null)
 						throw new IllegalArgumentException(MessageFormat.format(InitparserTreeMessages.getString("ExpressionProcesser.ArraytypeHasFewerDimensionsThanRequested_EXC_"), new Object[] {arrayType, new Integer(dimensionCount)})); //$NON-NLS-1$
 					
 					// We need to pull in the dimension initializers. They are stacked in reverse order.
 					int[] dimInit = new int[dimensionCount];
 					for(int i=dimensionCount-1; i >= 0; i--) {
 						Object index = popExpression();
 						Class dimType = popExpressionType(false);
 						if (dimType.isPrimitive() && (dimType == Integer.TYPE || dimType == Short.TYPE || dimType == Character.TYPE || dimType == Byte.TYPE)) {
 							dimInit[i] = getInt(index);
 						} else
 							throwClassCast(Integer.TYPE, index);
 					}
 					
 					// Finally create the array.
 					Object array = Array.newInstance(componentType, dimInit);
 					pushExpressionValue(array, arrayType);
 				}
 			} catch (RuntimeException e) {
 				processException(e);
 			} catch (NoExpressionValueException e) {
 				processSyntaxException(e);
 			}
 		} finally {
 			if (traceOn)
 				printTraceEnd();
 		}
 			
 	}
 
 	/**
 	 * Push the array initializer request.
 	 * 
 	 * @param arrayType The original type of the array to create.
 	 * @param stripCount the count of how many dimensions to strip to get the type needed for this initializer.
 	 * @param expressionCount
 	 * @since 1.0.0
 	 */
 	public final void pushArrayInitializer(Class arrayType, int stripCount, int expressionCount) {
 		boolean ignore = (ignoreExpression != null || errorOccurred);
 		
 		if (traceOn)
 			printTrace("Initialize Array: "+arrayType.getName()+'{'+expressionCount+'}', ignore); //$NON-NLS-1$
 		
 		try {
 			if (ignore)
 				return;
 	
 			try {
 				if (!arrayType.isArray()) {
 					// It is not an array type.
 					throw new ClassCastException(MessageFormat.format(InitparserTreeMessages.getString("ExpressionProcesser.CannotCastXToY_EXC_"), new Object[] {arrayType, "array"})); //$NON-NLS-1$ //$NON-NLS-2$
 				} 
 				// Strip off the number of dimensions specified.
 				while(stripCount-->0) {
 					arrayType = arrayType.getComponentType();
 				}
 				Object[] dimValues = null;
 				if (expressionCount > 0) {
 					// We need to pull in the initializers. They are stacked in reverse order.
 					dimValues = new Object[expressionCount];
 					for (int i = expressionCount - 1; i >= 0; i--) {
 						Object dimValue = dimValues[i] = popExpression();
 						Class dimType = popExpressionType(false);
 						if (arrayType.isPrimitive()) {
 							if (dimValue == null || !dimType.isPrimitive())
 								throwClassCast(arrayType, dimType);
 							// A little trickier. Can assign short to an int, but can't assign long to an int. Widening is permitted.
 							if (arrayType != dimType) {
 								int compEnum = getEnumForPrimitive(arrayType);
 								int dimEnum = getEnumForPrimitive(dimType);
 								if (compEnum == BOOLEAN || dimEnum == BOOLEAN)
 									throwClassCast(arrayType, dimType);
 								int dimValueAsInt = getInt(dimValue);
 								switch (compEnum) {
 									case BYTE :
 										// Can accept byte, short, char, or int as long as value is <= byte max. Can't accept long, double, float at all.
 										// Note: This isn't actually true. The max/min test is only valid if the value is a literal, not an expression,
 										// however, at this point in time we no longer know this. So we will simply allow it.
 										if (dimEnum > INT || dimValueAsInt > Byte.MAX_VALUE || dimValueAsInt < Byte.MIN_VALUE)
 											throwClassCast(arrayType, dimType);
 										// But need to be changed to appropriate type for the array.set to work.									
 										dimValues[i] = new Byte((byte)dimValueAsInt);
 										break;
 									case SHORT :								
 										// Can accept byte, short, char, or int as long as value is <= byte max. Can't accept long, double, float at all.
 										// Note: This isn't actually true. The max/min test is only valid if the value is a literal, not an expression,
 										// however, at this point in time we no longer know this. So we will simply allow it.
 										if (dimEnum > INT || dimValueAsInt > Short.MAX_VALUE || dimValueAsInt < Short.MIN_VALUE)
 											throwClassCast(arrayType, dimType);
 										// But need to be changed to appropriate type for the array.set to work.									
 										dimValues[i] = new Short((short)dimValueAsInt);
 										break;
 									case CHAR :
 										// Can accept byte, short, char, or int as long as value is <= byte max. Can't accept long, double, float at all.
 										// Note: This isn't actually true. The max/min test is only valid if the value is a literal, not an expression,
 										// however, at this point in time we no longer know this. So we will simply allow it.
 										if (dimEnum > INT || dimValueAsInt > Character.MAX_VALUE || dimValueAsInt < Character.MIN_VALUE)
 											throwClassCast(arrayType, dimType);
 										// But need to be changed to appropriate type for the array.set to work.									
 										dimValues[i] = new Character((char)dimValueAsInt);
 										break;
 									case INT :
 										// Can accept byte, short, char, or int. Can't accept long, double, float at all.
 										if (dimEnum > INT)
 											throwClassCast(arrayType, dimType);
 										// But need to be changed to appropriate type for the array.set to work.									
 										dimValues[i] = new Integer(dimValueAsInt);
 										break;
 									case LONG :
 										// Can accept byte, short, char, int, or long. Can't accept double, float at all.
 										if (dimEnum > LONG)
 											throwClassCast(arrayType, dimType);
 										// But need to be changed to appropriate type for the array.set to work.									
 										dimValues[i] = new Long(getLong(dimValue));
 										break;
 									case FLOAT :
 										// Can accept byte, short, char, int, long, or float. Can't accept double at all.
 										if (dimEnum > FLOAT)
 											throwClassCast(arrayType, dimType);
 										// But need to be changed to appropriate type for the array.set to work.									
 										dimValues[i] = new Float(getFloat(dimValue));
 										break;
 									case DOUBLE :
 										// But need to be changed to appropriate type for the array.set to work.									
 										dimValues[i] = new Double(getDouble(dimValue));
 										break;
 	
 								}
 							}
 							// Compatible, so ok.
 						} else if (dimType != MethodHelper.NULL_TYPE && !arrayType.isAssignableFrom(dimType)) {
 							// If it is NULL_TYPE, then this is a pushed null. This is always assignable to a non-primitive.
 							// So we don't enter here in that case. However, a null that was returned from some expression
 							// won't have a NULL_TYPE, it will instead have the expected return type. That must be used
 							// in the assignment instead. That is because in java it uses the expected type to determine
 							// compatibility, not the actual type.
 							throwClassCast(arrayType, dimType);
 						}
 					}
 					
 				}
 				
 				// Now we finally create the array.
 				Object array = Array.newInstance(arrayType, new int[] {expressionCount});
 				for (int i = 0; i < expressionCount; i++) {
 					Array.set(array, i, dimValues[i]);
 				}
 				
 				pushExpressionValue(array, array.getClass());	// Adjust to true array type, not the incoming type (which is one dimension too small).
 	
 			} catch (RuntimeException e) {
 				processException(e);
 			} catch (NoExpressionValueException e) {
 				processSyntaxException(e);
 			}
 		} finally {
 			if (traceOn)
 				printTraceEnd();
 		}
 			
 	}
 
 	/**
 	 * Push the class instance creation request.
 	 * 
 	 * @param type The type to create an instance of
 	 * @param argumentCount The number of arguments (which are stored on the stack).	 * @throws NoExpressionValueException
 	 * @since 1.0.0
 	 */
 	public final void pushClassInstanceCreation(Class type, int argumentCount) {
 		boolean ignore = (ignoreExpression != null || errorOccurred);
 		
 		if (traceOn)
 			printTrace("Create Class: "+type+" (", ignore); //$NON-NLS-1$ //$NON-NLS-2$
 		try {
 			if (ignore)
 				return;
 						
 			try {
 				// We need to pull in the arguments. They are stacked in reverse order.
 				Object value = null;	// The new instance.
 				if (argumentCount > 0) {
 					Object[]  args = new Object[argumentCount];
 					Class[] argTypes = new Class[argumentCount];
 					for (int i = argumentCount - 1; i >= 0; i--) {
 						args[i] = popExpression();
 						argTypes[i] = popExpressionType(false);
 					}
 					
 					// Now we need to find the appropriate constructor.
 					Constructor ctor;
 					ctor = MethodHelper.findCompatibleConstructor(type, argTypes);
 					if (traceOn) {
 						System.out.print(ctor);
 						System.out.print(')');
 					}
 					value = ctor.newInstance(args);
 				} else {
 					// No args, just do default ctor.
 					if (traceOn) {
 						System.out.print("Default ctor)"); //$NON-NLS-1$
 					}
 					value = type.newInstance();
 				}
 				
 				pushExpressionValue(value, type);
 			} catch (RuntimeException e) {
 				processException(e);
 			} catch (NoExpressionValueException e) {
 				processSyntaxException(e);
 			} catch (InstantiationException e) {
 				processException(e);
 			} catch (IllegalAccessException e) {
 				processException(e);
 			} catch (InvocationTargetException e) {
 				processException(e);
 			} catch (NoSuchMethodException e) {
 				processException(e);
 			} catch (AmbiguousMethodException e) {
 				processException(e);
			} catch (LinkageError e) {
				processException(e);
 			}
 		} finally {
 			if (traceOn)
 				printTraceEnd();
 		}
 			
 	}
 
 	/**
 	 * Push the field access expression.
 	 * @param field String for fieldname, or a java.lang.reflect.Field.
 	 * @param fieldIsString <code>true</code> if field is a string name, and not a java.lang.reflect.Field.
 	 * @param hasReceiver
 	 * 
 	 * @since 1.0.0
 	 */
 	public final void pushFieldAccess(Object field, boolean fieldIsString, boolean hasReceiver) {
 		try {
 			if (ignoreExpression != null || errorOccurred) {
 				if (traceOn)
 					printTrace("Field Access", true); //$NON-NLS-1$
 				return;
 			}
 		
 			if (traceOn)
 				printTrace("Field Access: ", false); //$NON-NLS-1$
 			try {
 				// Get the receiver off of the stack.
 				Object receiver = null;
 				Class receiverType = null;
 				if (hasReceiver) {
 					receiver = popExpression();
 					receiverType = popExpressionType(false);
 				}
 				
 				// Find the field.
 				Field reflectField = fieldIsString ? receiverType.getField((String) field) : (Field) field;
 				// Access the field.
 				Object value = FieldAccessReference.createFieldAccessReference(reflectField, receiver);
 				Class valueType = reflectField.getType();
 				if (traceOn) {
 					System.out.print("Field: "); //$NON-NLS-1$
 					if (fieldIsString)
 						System.out.print("(looked up) "); //$NON-NLS-1$
 					System.out.print(reflectField);
 					System.out.print(">"); //$NON-NLS-1$
 					printObjectAndType(value, valueType);
 				}
 					
 				pushExpressionValue(value, valueType);
 			} catch (RuntimeException e) {
 				processException(e);
 			} catch (NoExpressionValueException e) {
 				processSyntaxException(e);
 			} catch (NoSuchFieldException e) {
 				processException(e);
 			}
 		} finally {
 			if (traceOn)
 				printTraceEnd();
 		}
 			
 	}
 
 	/**
 	 * Push the method invocation expression.
 	 * @param method
 	 * @param methodIsString <code>true</code> if method is a string (so string name) or else it is a java.lang.reflect.Method.
 	 * @param hasReceiver
 	 * @param argCount
 	 * @since 1.0.0
 	 */
 	public final void pushMethodInvocation(Object method, boolean methodIsString, boolean hasReceiver, int argCount) {
 		try {
 			if (ignoreExpression != null || errorOccurred) {
 				if (traceOn)
 					printTrace("Invoke", true); //$NON-NLS-1$
 				return;
 			}
 			
 			if (traceOn)
 				printTrace("Invoke: ", false); //$NON-NLS-1$
 			
 			try {
 				// We need to pull in the arguments. They are stacked in reverse order.
 				Object[]  args = new Object[argCount];
 				Class[] argTypes = new Class[argCount];
 				for (int i = argCount - 1; i >= 0; i--) {
 					args[i] = popExpression();
 					argTypes[i] = popExpressionType(false);
 				}
 				
 				// Now get receiver
 				Object receiver = null;
 				Class receiverType = null;
 				if (hasReceiver) {
 					receiver = popExpression();
 					receiverType = popExpressionType(false);
 				}
 				
 				// Now we need to find the appropriate method. If it is a string then there must be a receiver, otherwise no way to know.
 				Method reflectMethod;
 				if (methodIsString) {
 					reflectMethod = MethodHelper.findCompatibleMethod(receiverType, (String) method, argTypes);
 				} else
 					reflectMethod = (Method) method;
 				
 				if (traceOn && reflectMethod != null) {
 					System.out.print("Method: "); //$NON-NLS-1$
 					if (methodIsString)
 						System.out.print("(looked up) "); //$NON-NLS-1$
 					System.out.print(reflectMethod);					
 				}
 				
 				if (!Modifier.isStatic(reflectMethod.getModifiers()) && receiver == null)
 					throw new NullPointerException("No receiver for non-static method: "+reflectMethod.toString());
 					
 				Object value = reflectMethod.invoke(receiver, args);
 				
 				if (traceOn) {
 					System.out.print(" returns: "); //$NON-NLS-1$
 					printObjectAndType(value, reflectMethod.getReturnType());
 				}
 				pushExpressionValue(value, reflectMethod.getReturnType());
 			} catch (RuntimeException e) {
 				processException(e);
 			} catch (NoExpressionValueException e) {
 				processSyntaxException(e);
 			} catch (IllegalAccessException e) {
 				processException(e);
 			} catch (InvocationTargetException e) {
 				processException(e);
 			} catch (NoSuchMethodException e) {
 				processException(e);
 			} catch (AmbiguousMethodException e) {
 				processException(e);
 			}
 		} finally {
 			if (traceOn)
 				printTraceEnd();
 		}
 			
 	}
 
 	private static final Object CONDITIONAL_IGNORE = "CONDITIONAL IGNORE";	// Flag for conditional in ingore //$NON-NLS-1$
 	private int conditionalNesting = 0;	// Nesting of conditional expressions.
 	private int conditionalIgnoreNestCount = 0;	// When ignoring conditional expressions, ignore until this nest count.
 	private boolean skipTruePart;
 
 	/**
 	 * Push a conditional expression. It can be any clause of the conditional (test, true, or false clause).
 	 * @param expressionType
 	 * 
 	 * @since 1.0.0
 	 */
 	public final void pushConditional(InternalConditionalOperandType expressionType) {
 		try {
 			boolean ignore = true;
 			try {
 				if (errorOccurred)
 					return;
 				// Slightly different here in that if an ignoring occurred we still need to process at least part of it so that
 				// we can get the expression grouping correct.
 				switch (expressionType.getValue()) {
 					case InternalConditionalOperandType.CONDITIONAL_TEST_VALUE:
 						conditionalNesting++;
 						break;
 					case InternalConditionalOperandType.CONDITIONAL_TRUE_VALUE:
 						if (skipTruePart && ignoreExpression == CONDITIONAL_IGNORE && conditionalIgnoreNestCount == conditionalNesting) {
 							// stop ignoring, we've ignored the true condition of interest.
 							ignoreExpression = null;
 							return; // However, leave because since this condition was ignored.
 						}
 						break;
 					case InternalConditionalOperandType.CONDITIONAL_FALSE_VALUE:
 						int currentNesting = conditionalNesting--;
 						if (ignoreExpression == CONDITIONAL_IGNORE && conditionalIgnoreNestCount == currentNesting) {
 							// stop ignoring, we've ignored the false condition of interest.
 							ignoreExpression = null;
 							return; // However, leave because since this condition was ignored.
 						}
 				}
 	
 				if (ignoreExpression != null)
 					return;
 				ignore = false;
 			} finally {
 				if (traceOn)
 					printTrace("Conditional "+expressionType, ignore); //$NON-NLS-1$
 			}
 					
 			try {
 				switch (expressionType.getValue()) {
 					case InternalConditionalOperandType.CONDITIONAL_TEST_VALUE:
 						Object condition = popExpression();
 						Class type = popExpressionType(false);
 						if (type != Boolean.TYPE)
 							throwClassCast(Boolean.TYPE, condition);
 						if (((Boolean) condition).booleanValue()) {
 							// Condition was true.
 							// Do nothing. Let true condition be processed.
 						} else {
 							// Condition was false.
 							skipTruePart = true;	// Tell the true condition should be ignored.
 							ignoreExpression = CONDITIONAL_IGNORE;
 							conditionalIgnoreNestCount = conditionalNesting;
 						}
 						// We don't put anything back on the stack because the condition test is not ever returned.
 						// The appropriate true or false condition evaluation will be left on the stack.
 						break;
 					case InternalConditionalOperandType.CONDITIONAL_TRUE_VALUE:
 						skipTruePart = false;	// Tell the false condition should be ignored.
 						ignoreExpression = CONDITIONAL_IGNORE;
 						conditionalIgnoreNestCount = conditionalNesting;
 						break;
 					case InternalConditionalOperandType.CONDITIONAL_FALSE_VALUE:
 						// There's nothing to do, if it was ignored due to true, we wouldn't of gotton here.
 						// If it wasn't ignored, then the result of the false expression is on the stack, which is what it should be.
 						break;
 				}
 			} catch (RuntimeException e) {
 				processException(e);
 			} catch (NoExpressionValueException e) {
 				processSyntaxException(e);
 			}
 		} finally {
 			if (traceOn)
 				printTraceEnd();
 		}
 	}
 	
 	private static final Object BLOCK_IGNORE = "BLOCK IGNORE"; //$NON-NLS-1$
 	private int[] blocks;	// Stack of block numbers currently evaluating.
 	private int topBlock = -1;	// Top block index.
 	private int breakBlock = -1;	// Block number we are breaking to.
 	
 	/**
 	 * Push a begin block.
 	 * @param blockNumber
 	 * 
 	 * @since 1.1.0
 	 */
 	public final void pushBlockBegin(int blockNumber) {
 		if (traceOn) {
 			printTrace("Begin Block #"+blockNumber, errorOccurred); //$NON-NLS-1$
 			indent(true);
 		}
 		try {
 			if (errorOccurred)
 				return;
 			// We are not checking ignore because this is a structural concept instead of executable expressions, so we need to keep track of these.
 			if (blocks == null)
 				blocks = new int[10];
 			if (++topBlock >= blocks.length) {
 				int[] newList = new int[blocks.length*2];
 				System.arraycopy(blocks, 0, newList, 0, blocks.length);
 				blocks = newList;
 			}
 			blocks[topBlock] = blockNumber;
 		} finally {
 			if (traceOn)
 				printTraceEnd();
 		}
 	}
 	
 	/**
 	 * Push a block end. The current block must be the given number, or it is an error.
 	 * 
 	 * @param blockNumber
 	 * 
 	 * @since 1.1.0
 	 */
 	public final void pushBlockEnd(int blockNumber) {
 		try {
 			if (traceOn) {
 				indent(false);
 				printTrace("End Block #"+blockNumber, errorOccurred); //$NON-NLS-1$
 			}
 			if (errorOccurred)
 				return;
 			// We are not checking ignore because this is a structural concept instead of executable expressions, so we need to keep track of these.
 			if (blocks == null || topBlock < 0 || blocks[topBlock] != blockNumber) {
 				processSyntaxException(new IllegalStateException(InitparserTreeMessages.getString("ExpressionProcesser.PushBlockEnd.ReceivedEndBlocksOutOfOrder_EXC_"))); //$NON-NLS-1$
 			} else {
 				topBlock--;
 				if (ignoreExpression == BLOCK_IGNORE && blockNumber == breakBlock) {
 					ignoreExpression = null;
 				}
 			}
 		} finally {
 			if (traceOn)
 				printTraceEnd();
 		}			
 	}
 	
 	/**
 	 * Skip all following until we hit the requested block number.
 	 * @param blockNumber
 	 * 
 	 * @since 1.1.0
 	 */
 	public final void pushBlockBreak(int blockNumber) {
 		try {
 			if (traceOn)
 				printTrace("Break Block #"+blockNumber, errorOccurred); //$NON-NLS-1$
 			if (errorOccurred)
 				return;
 			if (ignoreExpression == null) {
 				ignoreExpression = BLOCK_IGNORE;	// Start ignoring expressions until we hit the block number end block.
 				breakBlock = blockNumber;
 			}
 		} finally {
 			if (traceOn)
 				printTraceEnd();
 		}
 	}
 	
 	private static final Object TRY_THROW_IGNORE = "TRY THROW IGNORE"; //$NON-NLS-1$
 	private static final Object TRY_FINAL_IGNORE = "TRY FINAL IGNORE"; //$NON-NLS-1$
 	private int[] trys;	// Stack of try numbers currently evaluating.
 	// Stack of trys in catch clause (i.e. starting executing a catch/final clause for the try). Corresponds with try from same index in trys. Contains the throwable for the catch.
 	// This is used to know we are executing a catch (entry not null) and for the rethrow short-hand to rethrow the same exception within the catch.
 	private Throwable[] trysInCatch;	
 	private int topTry = -1;	// Top try index.
 	private int breakTry = -1;	// Try number we are breaking to.
 	private Throwable catchThrowable;	// The throwable to check catches against.
 	
 	/**
 	 * Push a try statement.
 	 * @param tryNumber
 	 * 
 	 * @since 1.1.0
 	 */
 	public final void pushTryBegin(int tryNumber) {
 		try {
 			if (traceOn) {
 				printTrace("Begin Try #"+tryNumber, errorOccurred); //$NON-NLS-1$
 				indent(true);
 			}
 	
 			if (errorOccurred)
 				return;
 			// We are not checking ignore because this is a structural concept instead of executable expressions, so we need to keep track of these.
 			if (trys == null) {
 				trys = new int[10];
 				trysInCatch = new Throwable[10];
 			}
 			if (++topTry >= trys.length) {
 				int[] newList = new int[trys.length*2];
 				System.arraycopy(trys, 0, newList, 0, trys.length);
 				trys = newList;
 				Throwable[] newCatches = new Throwable[trys.length];
 				System.arraycopy(trysInCatch, 0, newCatches, 0, trysInCatch.length);
 				trysInCatch = newCatches;
 			}
 			trys[topTry] = tryNumber;
 			trysInCatch[topTry] = null;
 		} finally {
 			if (traceOn)
 				printTraceEnd();
 		}
 	}
 
 	/**
 	 * Throw the top stack entry. It must be an exception.
 	 * 
 	 * 
 	 * @since 1.1.0
 	 */
 	public final void pushThrowException() {
 		try {
 			boolean ignore = (ignoreExpression != null || errorOccurred);
 			if (traceOn)
 				printTrace("Throw exception: ", ignore); //$NON-NLS-1$
 			
 			if (ignore)
 				return;
 	
 			try {
 				Object t = popExpression();
 				popExpressionType(false);
 				if (traceOn) {
 					System.out.print(t);
 				}
 				throwException((Throwable) t);
 			} catch (NoExpressionValueException e) {
 				processSyntaxException(e);
 			} catch (ClassCastException e) {
 				processException(e);
 			}
 		} finally {
 			if (traceOn)
 				printTraceEnd();
 		}			
 	}
 	
 	/**
 	 * Throw this exception (means throw through the expression being processed, not throw for this thread).
 	 * @param exception
 	 * 
 	 * @since 1.1.0
 	 */
 	protected final void throwException(Throwable exception) {
 		if (topTry == -1) {
 			// There are no tries, so treat this as a syntax error.
 			processSyntaxException(exception);
 		} else if (trysInCatch[topTry] == null) {
 			// We are not in a catch clause of the top try. So do a throw ignore for toptry.
 			ignoreExpression = TRY_THROW_IGNORE;
 			breakTry = trys[topTry];
 			catchThrowable = exception;
 		} else {
 			// We are in a catch of the top try. So do a throw to finally instead.
 			ignoreExpression = TRY_FINAL_IGNORE;
 			trysInCatch[topTry] = FINAL_CATCH;
 			breakTry = trys[topTry];
 			catchThrowable = exception;
 		}
 	}
 	
 	/**
 	 * Push a catch clause
 	 * @param tryNumber
 	 * @param exceptionType
 	 * @param expressionProxy
 	 * 
 	 * @since 1.1.0
 	 */
 	public final void pushTryCatchClause(int tryNumber, Class exceptionType, InternalExpressionProxy expressionProxy) {
 		try {
 			if (traceOn) {
 				indent(false);
 				if (expressionProxy == null)
 					printTrace("Catch Try #"+tryNumber+" ("+exceptionType+')', errorOccurred); //$NON-NLS-1$ //$NON-NLS-2$
 				else
 					printTrace("Catch Try #"+tryNumber+" ("+exceptionType+") Return exception in proxy #"+expressionProxy.getProxyID(), errorOccurred); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				indent(true);
 			}
 			
 			if (errorOccurred)
 				return;
 			
 			// We are not checking ignore because this is a structural concept instead of executable expressions, so we need to keep track of these.
 			if (trys == null || topTry < 0 || trys[topTry] != tryNumber) {
 				processSyntaxException(new IllegalStateException(InitparserTreeMessages.getString("ExpressionProcesser.PushTryCatchClause.CatchReceivedOutOfOrder_EXC_"))); //$NON-NLS-1$
 			} else {
 				if (ignoreExpression == null) {
 					// Normal flow, no throw in progress, so just ignore now until the finally or end try reached.
 					ignoreExpression = TRY_FINAL_IGNORE;
 					breakTry = tryNumber;
 				} else if (ignoreExpression == TRY_THROW_IGNORE && tryNumber == breakTry) {
 					// We are here due to a throw occuring in this try block, see if for us, and if it is, stop ignoring.
 					// Else just continue ignoring.
 					if (exceptionType.isInstance(catchThrowable)) {
 						// For us, so just turn everything back on, except mark that we are in the catch phase.
 						ignoreExpression = null;
 						trysInCatch[topTry] = catchThrowable; // This is so that we know if we throw again that we should not catch anything.
 						breakTry = -1;
 						if (expressionProxy != null) {
 							expressionProxy.setProxy(catchThrowable, catchThrowable.getClass());
 							allocateExpressionProxy(expressionProxy);
 						}
 						if (traceOn) {
 							System.out.print(" Caught: "); //$NON-NLS-1$
 							System.out.print(catchThrowable);
 						}
 						catchThrowable = null;
 					}
 				}
 			}
 		} finally {
 			if (traceOn)
 				printTraceEnd();
 		}			
 	}
 	
 	// This is used only so that finally clause can indicate it was executing so that end expression knows this.
 	private static final Throwable FINAL_CATCH = new RuntimeException();
 
 	/**
 	 * Push the try finally clause.
 	 * @param tryNumber
 	 * 
 	 * @since 1.1.0
 	 */
 	public final void pushTryFinallyClause(int tryNumber) {
 		try {
 			if (traceOn) {
 				indent(false);
 				printTrace("Finally Try #"+tryNumber, errorOccurred); //$NON-NLS-1$
 				indent(true);
 			}
 			if (errorOccurred)
 				return;
 			// We are not checking ignore because this is a structural concept instead of executable expressions, so we need to keep track of these.
 			if (trys == null || topTry < 0 || trys[topTry] != tryNumber) {
 				processSyntaxException(new IllegalStateException(InitparserTreeMessages.getString("ExpressionProcesser.PushTryFinallyClause.FinallyReceivedOutOfOrder_EXC_"))); //$NON-NLS-1$
 			} else {
 				if (tryNumber == breakTry && (ignoreExpression == TRY_THROW_IGNORE || ignoreExpression == TRY_FINAL_IGNORE)) {
 					// We are here due to a throw occuring in this try block or a catch was reached (in which case all intervening catch's were ignored).
 					// Now do a normal execution. If we are here due to a throw that wasn't cleared (either no catch or another throw occured within the catch)
 					// then we leave it uncleared so that try/end may rethrow it.
 					ignoreExpression = null;
 					trysInCatch[topTry] = FINAL_CATCH; // We are in the finally clause of a exception being thrown within this try.
 					breakTry = -1;
 					if (traceOn)
 						System.out.print(" Executing finally."); //$NON-NLS-1$
 				}
 			}
 		} finally {
 			if (traceOn)
 				printTraceEnd();
 		}			
 	}
 	
 	/**
 	 * Rethrow the caught exception. This is a shortcut for:
 	 * } catch (Exception e) {
 	 *   ... do stuff ...
 	 *   throw e;
 	 * }
 	 * @param tryNumber
 	 * 
 	 * @since 1.1.0
 	 */
 	public final void pushTryRethrow(int tryNumber) {
 		if (traceOn)
 			printTrace("Rethrow Try #"+tryNumber, errorOccurred  || ignoreExpression != null); //$NON-NLS-1$
 		
 		try {
 			if (errorOccurred)
 				return;
 			// We are not checking ignore because we need to make sure this is not called out of order.
 			if (trys == null || topTry < 0 || trys[topTry] != tryNumber) {
 				processSyntaxException(new IllegalStateException(InitparserTreeMessages.getString("ExpressionProcesser.PushTryRethrow.RethrowReceivedOutOfOrder_EXC_"))); //$NON-NLS-1$
 			} else if (ignoreExpression == null) {
 				if (trysInCatch[topTry] == null || trysInCatch[topTry] == FINAL_CATCH)
 					processSyntaxException(new IllegalStateException(InitparserTreeMessages.getString("ExpressionProcesser.PushTryRethrow.RetryReceivedOutOfExecutingCatchClause_EXC_"))); //$NON-NLS-1$
 				else {
 					throwException(trysInCatch[topTry]);
 				}
 			}
 		} finally {
 			if (traceOn)
 				printTraceEnd();
 		}		
 	}
 	
 	public final void pushTryEnd(int tryNumber) {
 		if (traceOn) {
 			indent(false);
 			printTrace("End Try #"+tryNumber, errorOccurred); //$NON-NLS-1$
 		}
 		try {
 			if (errorOccurred)
 				return;
 			// We are not checking ignore because this is a structural concept instead of executable expressions, so we need to keep track of these.
 			if (trys == null || topTry < 0 || trys[topTry] != tryNumber) {
 				processSyntaxException(new IllegalStateException(InitparserTreeMessages.getString("ExpressionProcesser.PushTryEnd.TryEndReceivedOutOfOrder_EXC_"))); //$NON-NLS-1$
 			} else {
 				boolean inCatch = trysInCatch[topTry] != null;
 				trysInCatch[topTry] = null;
 				topTry--;
 				if (inCatch || (tryNumber == breakTry && (ignoreExpression == TRY_THROW_IGNORE || ignoreExpression == TRY_FINAL_IGNORE))) {
 					// We are here due to a throw or normal flow through a catch. Either way if there is a throwable still pending, we rethrow.
 					ignoreExpression = null;
 					breakTry = -1;
 					if (catchThrowable != null)
 						throwException(catchThrowable);
 				} 
 			}
 		} finally {
 			if (traceOn)
 				printTraceEnd();
 		}			
 	}
 	
 	/**
 	 * Class used to save the state at time of mark. It will
 	 * be used to restore state if error.
 	 * 
 	 * @since 1.1.0
 	 */
 	protected class SaveState {
 		public int markID;
 		
 		// Block state
 		public int topBlock;
 		public int breakBlock;
 
 		// Subexpression state
 		public int topSubexpression;
 
 		// Try state
 		public int topTry;
 		public int breakTry;
 		public Throwable catchThrowable;
 		
 		// Error state
 		public boolean errorOccurred;
 		public boolean novalueException;
 		public Throwable exception;
 		public Object ignoreExpression;
 		
 		// Expression stack state
 		public int expressionStackPos;
 		
 		// If/else state
 		public int ifElseNesting;
 		public int ifElseIgnoreNestCount;
 		public boolean ifElseSkipTruePart;
 		
 		// Other
 		public int indent;
 		public int expressionProxyPos;
 		
 		/**
 		 * Construct and save the state.
 		 * 
 		 * @param markNumber
 		 * 
 		 * @since 1.1.0
 		 */
 		public SaveState(int markID) {
 			this.markID = markID;
 			
 			ExpressionProcesser ep = ExpressionProcesser.this;
 			this.topBlock = ep.topBlock;
 			this.breakBlock = ep.breakBlock;
 			
 			this.topSubexpression = ep.topSubexpression;
 			
 			this.topTry = ep.topTry;
 			this.breakTry = ep.breakTry;
 			this.catchThrowable = ep.catchThrowable;
 			
 			this.errorOccurred = ep.errorOccurred;
 			this.novalueException = ep.novalueException;
 			this.exception = ep.exception;
 			this.ignoreExpression = ep.ignoreExpression;
 			
 			this.expressionStackPos = expressionStack.size()-1;
 			
 			this.ifElseNesting = ep.ifElseNesting;
 			this.ifElseIgnoreNestCount = ep.ifElseIgnoreNestCount;
 			this.ifElseSkipTruePart = ep.ifElseSkipTruePart;
 			
 			this.indent = ep.indent;
 			this.expressionProxyPos = expressionProxies != null ? expressionProxies.size()-1 : -1;
 		}
 		
 		/**
 		 * Restore the state.
 		 * 
 		 * 
 		 * @since 1.1.0
 		 */
 		public void restoreState() {
 			ExpressionProcesser ep = ExpressionProcesser.this;
 			ep.topBlock = this.topBlock;
 			ep.breakBlock = this.breakBlock;
 
 			ep.topSubexpression = this.topSubexpression;
 			
 			ep.topTry = this.topTry;
 			ep.breakTry = this.breakTry;
 			ep.catchThrowable = this.catchThrowable;
 			if (trysInCatch != null) {
 				for (int i = topTry + 1; i < ep.trysInCatch.length; i++) {
 					ep.trysInCatch[i] = null;
 				}
 			}
 
 			ep.errorOccurred = this.errorOccurred;
 			ep.novalueException = ep.novalueException;
 			ep.exception = this.exception;
 			ep.ignoreExpression = this.ignoreExpression;
 			
 			// Pop stack down to saved state.
 			for (int i = expressionStack.size()-1; i > this.expressionStackPos; i--) {
 				expressionStack.remove(i);
 				expressionTypeStack.remove(i);
 			}
 			
 			ep.ifElseNesting = this.ifElseNesting;
 			ep.ifElseIgnoreNestCount = this.ifElseIgnoreNestCount;
 			ep.ifElseSkipTruePart = this.ifElseSkipTruePart;
 			
 			ep.indent = this.indent;
 			
 			if (expressionProxies != null) {
 				for (int i = expressionProxies.size() - 1; i > this.expressionProxyPos; i--) {
 					expressionProxies.remove(i);
 				}
 			}
 			
 			// These settings can't cross mark boundaries, so reset them to not set. This is in case we were in this state somewhere
 			// in the mark when the restore occurred.
 			ep.conditionalIgnoreNestCount = 0;
 			ep.conditionalNesting = 0;
 			ep.skipTruePart = false;
 			
 			ep.infixIgnoreNestCount = 0;
 			ep.infixNesting = 0;
 		}
 	}
 	
 	/**
 	 * Create the save state with the given id.
 	 * @param markID
 	 * @return
 	 * 
 	 * @since 1.1.0
 	 */
 	protected SaveState createSaveState(int markID) {
 		return new SaveState(markID);
 	}
 	
 	/**
 	 * Push the start of a mark.
 	 * @param markNumber
 	 * 
 	 * @since 1.1.0
 	 */
 	public final void pushMark(int markNumber) {
 		if (traceOn)
 			printTrace("Mark#"+markNumber, false); //$NON-NLS-1$
 		
 		if (saveStates == null)
 			saveStates = new ArrayList();
 		saveStates.add(createSaveState(markNumber));
 		
 		if (traceOn)
 			printTraceEnd();
 	}
 	
 	/**
 	 * Push the end mark. If there is no error, it will simply
 	 * remove it and all save states in the map after it. If there
 	 * is an error it will do this plus it will restore the state.
 	 * <p>
 	 * It is assumed that the calls are coming in correct order from
 	 * the server so we won't check validity.
 	 * 
 	 * @param markID
 	 * @param restore
 	 * 
 	 * @since 1.1.0
 	 */
 	public final void pushEndmark(int markID, boolean restore) {
 		if (traceOn)
 			printTrace("End Mark#"+markID+" Restored="+restore, false); //$NON-NLS-1$ //$NON-NLS-2$
 		
 		try {
 			if (saveStates != null) {
 				// Start from the end (since that is where it most likely will be) and start
 				// search, removing the end one until we reach the markID.
 				for (int i = saveStates.size() - 1; i >= 0; i--) {
 					SaveState state = (SaveState) saveStates.remove(i);
 					if (state.markID == markID) {
 						// We found it.
 						if (restore)
 							state.restoreState();
 						return;
 					}
 				}
 				// But to be safe, if we got here, this is bad. We tried restore a mark we didn't have.
 				processSyntaxException(new IllegalStateException(MessageFormat.format(InitparserTreeMessages.getString("ExpressionProcesser.PushEndmark.EndMarkOnNonExistingID_EXC_"), new Object[]{new Integer(markID)}))); //$NON-NLS-1$
 			}
 		} finally {
 			if (traceOn)
 				printTraceEnd();
 		}
 	}
 	
 	private int[] subexpressions;	// Stack of subexpression numbers currently evaluating.
 	private int[] subexpressionStackPos;	// Stack of the expression stack positions (next entry index) for currently evaluating expressions. The evaluation stack cannot be popped beyond the current top. And at end it will be cleaned up to the position.
 	private int topSubexpression = -1;	// Top subexpression index.
 
 	
 	/**
 	 * Push a begin subexpression.
 	 * @param subexpressionNumber
 	 * 
 	 * @since 1.1.0
 	 */
 	public final void pushSubexpressionBegin(int subexpressionNumber) {
 		if (traceOn) {
 			printTrace("Begin Subexpression #"+subexpressionNumber, errorOccurred); //$NON-NLS-1$
 			indent(true);
 		}
 		try {
 			if (errorOccurred)
 				return;
 			// We are not checking ignore because this is a structural concept instead of executable expressions, so we need to keep track of these.
 			if (subexpressions == null) {
 				subexpressions = new int[10];
 				subexpressionStackPos = new int[10];
 			}
 			if (++topSubexpression >= subexpressions.length) {
 				int[] newList = new int[subexpressions.length*2];
 				System.arraycopy(subexpressions, 0, newList, 0, subexpressions.length);
 				subexpressions = newList;
 				newList = new int[subexpressionStackPos.length*2];
 				System.arraycopy(subexpressionStackPos, 0, newList, 0, subexpressionStackPos.length);
 				subexpressionStackPos = newList;				
 			}
 			subexpressions[topSubexpression] = subexpressionNumber;
 			subexpressionStackPos[topSubexpression] = expressionStack.size();
 		} finally {
 			if (traceOn)
 				printTraceEnd();
 		}
 	}
 	
 	/**
 	 * Push a subexpression end. The current subexpression must be the given number, or it is an error.
 	 * 
 	 * @param subexpressionNumber
 	 * 
 	 * @since 1.1.0
 	 */
 	public final void pushSubexpressionEnd(int subexpressionNumber) {
 		try {
 			if (traceOn) {
 				indent(false);
 				printTrace("End Subexpression #"+subexpressionNumber, errorOccurred); //$NON-NLS-1$
 			}
 			if (errorOccurred)
 				return;
 			// We are not checking ignore because this is a structural concept instead of executable expressions, so we need to keep track of these.
 			if (subexpressions == null || topSubexpression < 0 || subexpressions[topSubexpression] != subexpressionNumber) {
 				processSyntaxException(new IllegalStateException(InitparserTreeMessages.getString("ExpressionProcesser.PushSubexpressionEnd.ReceivedEndSubexpressionsOutOfOrder_EXC_"))); //$NON-NLS-1$
 			} else {
 				try {
 					popExpressions(expressionStack.size()-subexpressionStackPos[topSubexpression]);
 					topSubexpression--;
 				} catch (NoExpressionValueException e) {
 					processSyntaxException(e);
 				}
 			}
 		} finally {
 			if (traceOn)
 				printTraceEnd();
 		}			
 	}
 
 
 }
