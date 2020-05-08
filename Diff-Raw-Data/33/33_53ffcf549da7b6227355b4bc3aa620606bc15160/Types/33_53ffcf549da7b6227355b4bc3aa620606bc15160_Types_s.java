 package util.type;
 
 import java.util.logging.Logger;
 
 import back_end.TypeCheckingVisitor;
 
 import util.ast.node.ArgumentsNode;
 import util.ast.node.BiOpNode;
 import util.ast.node.ExceptionTypeNode;
 import util.ast.node.FunctionNode;
 import util.ast.node.ParametersNode;
 import util.ast.node.PostfixExpressionNode;
 import util.ast.node.PrimitiveTypeNode;
 import util.ast.node.DerivedTypeNode;
 import util.ast.node.DictTypeNode;
 import util.ast.node.ReservedWordTypeNode;
 import util.ast.node.TypeNode;
 import util.ast.node.UnOpNode;
 import util.ast.node.PostfixExpressionNode.PostfixType;
 import util.error.InvalidFunctionArgumentsError;
 import util.error.FunctionNotDefinedError;
 import util.error.TypeMismatchError;
 import util.symbol_table.FunctionSymbol;
 import util.symbol_table.SymbolTable;
 
 /**
  * A convenience class for defining and manipulating internal type
  * representations.
  * 
  * Types are represented by TypeNode classes {DerivedTypeNode, DictTypeNode,
  * ExceptionTypeNode, PrimitiveTypeNode}, in conjunction with the static enums
  * Primitive, Derived and Exception defined by this class.
  * 
  * @author Samuel Messing
  * 
  */
 public class Types {
 
 	protected final static Logger LOGGER = Logger.getLogger(Types.class
 			.getName());
 
 	/**
 	 * Primitive types in hog.
 	 * 
 	 * The valid primitive types are:
 	 * <ul>
 	 * <li>BOOL : a boolean</li>
 	 * <li>TEXT : a string, wrapped in single- or double-quotes.</li>
 	 * <li>VOID : the null type</li>
 	 * <li>INT : an integer</li>
 	 * <li>REAL : a real number (double)</li>
 	 * <li>CHECK_INNER_TYPE: !TODO! WHAT AM I?</li>
 	 * </ul>
 	 * 
 	 * @author sam, jason
 	 * 
 	 */
 	public static enum Primitive {
 		BOOL, TEXT, VOID, INT, REAL
 	}
 
 	/**
 	 * Derived (collection) types in hog.
 	 * 
 	 * The valid derived types are:
 	 * <ul>
 	 * <li>LIST : an ordered collection</li>
 	 * <li>ITER : an iterator over a collection</li>
 	 * <li>DICT : a key->value dictionary</li>
 	 * <li>MULTISET : an unordered collection, allowing duplicates</li>
 	 * <li>SET : an unordered collection, disallowing duplicates</li>
 	 * </ul>
 	 * 
 	 * @author sam
 	 * 
 	 */
 	public static enum Derived {
 		LIST, ITER, DICT, MULTISET, SET
 	}
 
 	/**
 	 * Exception types in hog.
 	 * 
 	 * The valid expection types are:
 	 * <ul>
 	 * <li>ARITHMETIC : attempting to compute the impossible (most likely
 	 * division by zero),</li>
 	 * <li>ARRAY_OUT_OF_BOUNDS : attempting to access an invalid index into a
 	 * collection,</li>
 	 * <li>FILE_LOAD : error while reading from a file,</li>
 	 * <li>FILE_NOT_FOUND : opening a non-existent file,</li>
 	 * <li>INCORRECT_ARGUMENT : evaluating a function call using incorrect
 	 * (either number or type of) arguments,</li>
 	 * <li>NULL_REFERENCE : attempting to access a null (dead) variable,
 	 * <li>TYPE_MISMATCH : invalid operands and operator type combination</li>
 	 * </ul>
 	 * 
 	 * @author sam
 	 * 
 	 */
 	public static enum Exception {
 		FILE_NOT_FOUND, FILE_LOAD, ARRAY_OUT_OF_BOUNDS, INCORRECT_ARGUMENT, TYPE_MISMATCH, NULL_REFERENCE, ARITHMETIC
 	}
 
 	/**
 	 * 
 	 * @author sam
 	 * 
 	 */
 	public static enum Flags {
 		RESERVED_WORD, CHECK_INNER_TYPE, CHECK_ENTIRE_TYPE
 	}
 
 	/**
 	 * 
 	 * @param one
 	 * @param two
 	 * @return
 	 */
 	public static boolean isSameType(TypeNode one, TypeNode two) {
 
 		if (one.isPrimitive() && two.isPrimitive()) {
 			return isSameType((PrimitiveTypeNode) one, (PrimitiveTypeNode) two);
 		} else if (one.isDict() && two.isDict()) {
 			return isSameType((DictTypeNode) one, (DictTypeNode) two);
 		} else if (one.isDerived() && two.isDerived()) {
 			return isSameType((DerivedTypeNode) one, (DerivedTypeNode) two);
 		} else if (one.isException() && two.isException()) {
 			return isSameType((ExceptionTypeNode) one, (ExceptionTypeNode) two);
 		}
 
 		return false;
 
 	}
 
 	/**
 	 * 
 	 * @param one
 	 * @param two
 	 * @return
 	 */
 	private static boolean isSameType(ExceptionTypeNode one,
 			ExceptionTypeNode two) {
 		return one.getExceptionType() == two.getExceptionType();
 	}
 
 	/**
 	 * 
 	 * @param one
 	 * @param two
 	 * @return
 	 */
 	private static boolean isSameType(DictTypeNode one, DictTypeNode two) {
 		if (one.getKeyType() == two.getKeyType()) {
 			return isSameType(one.getValueType(), two.getValueType());
 		}
 		return false;
 	}
 
 	/**
 	 * 
 	 * @param one
 	 * @param two
 	 * @return
 	 */
 	private static boolean isSameType(PrimitiveTypeNode one,
 			PrimitiveTypeNode two) {
 		return one.getType() == two.getType();
 	}
 
 	/**
 	 * 
 	 * @param one
 	 * @param two
 	 * @return
 	 */
 	private static boolean isSameType(DerivedTypeNode one, DerivedTypeNode two) {
 		if (one.getLocalType() == two.getLocalType()) {
 			return isSameType(one.getInnerTypeNode(), two.getInnerTypeNode());
 		}
 		return false;
 	}
 
 	/**
 	 * 
 	 * @param op
 	 * @param left
 	 * @param right
 	 * @return
 	 */
 	public static boolean isCompatible(BiOpNode.OpType op, TypeNode left,
 			TypeNode right) {
 
 		switch (op) {
 		case ASSIGN:
 			if (left.isNumeric() && right.isNumeric())
 				return true;
 			else if (Types.isSameType(left, right))
 				return true;
 			else
 				return false;
 		case NOT_EQLS:
 			if (left.isNumeric() && right.isNumeric())
 				return true;
 			else if (Types.isSameType(left, right))
 				return true;
 			else
 				return false;
 		case DBL_EQLS:
 			if (left.isNumeric() && right.isNumeric())
 				return true;
 			else if (Types.isSameType(left, right))
 				return true;
 			else
 				return false;
 		case PLUS:
 			if (left.isText() && right.isText())
 				return true;
 			else if (left.isNumeric() && right.isNumeric())
 				return true;
 			else
 				return false;
 		case MINUS:
 			if (left.isNumeric() && right.isNumeric())
 				return true;
 			else
 				return false;
 		case DIVIDE:
 			if (left.isNumeric() && right.isNumeric())
 				return true;
 			else
 				return false;
 		case TIMES:
 			if (left.isNumeric() && right.isNumeric())
 				return true;
 			else
 				return false;
 		case MOD:
 			return left.isNumeric() && right.isNumeric();
 		case OR:
 			return left.isBoolean() && right.isBoolean();
 		case AND:
 			return left.isBoolean() && right.isBoolean();
 		case LESS:
 			return left.isNumeric() && right.isNumeric();
 		case GRTR:
 			return left.isNumeric() && right.isNumeric();
 		case LESS_EQL:
 			return left.isNumeric() && right.isNumeric();
 		case GRTR_EQL:
 			return left.isNumeric() && right.isNumeric();
 
 			// return left.isPrimitive() && right.isPrimitive()
 			// && isSameType(left, right);
 		}
 
 		// should never get here:
 		throw new UnsupportedOperationException("OpType " + op
 				+ "not implemented!");
 	}
 
 	/**
 	 * 
 	 * @param op
 	 * @param left
 	 * @param right
 	 * @return
 	 * @throws TypeMismatchError
 	 */
 	public static TypeNode getResult(BiOpNode.OpType op, TypeNode left,
 			TypeNode right) throws TypeMismatchError {
 
 		if (!isCompatible(op, left, right)) {
 			throw new TypeMismatchError("Tried to call " + op + " on operands "
 					+ left + " (left) and " + right + " (right)!");
 		}
 
 		switch (op) {
 		case ASSIGN:
 			return new PrimitiveTypeNode(Types.Primitive.VOID);
 		case PLUS:
 			if (left.isText() && right.isText())
 				return new PrimitiveTypeNode(Types.Primitive.TEXT);
 		case MINUS:
 		case DIVIDE:
 		case TIMES:
 		case MOD:
 			return getHigherNumericType((PrimitiveTypeNode) left,
 					(PrimitiveTypeNode) right);
 		case OR:
 		case AND:
 		case NOT_EQLS:
 		case DBL_EQLS:
 		case LESS:
 		case GRTR:
 		case LESS_EQL:
 		case GRTR_EQL:
 			return new PrimitiveTypeNode(Types.Primitive.BOOL);
 		}
 
 		// should never get here.
 		throw new UnsupportedOperationException("OpType " + op
 				+ "not implemented!");
 	}
 
 	/**
 	 * 
 	 * @param op
 	 * @param operand
 	 * @return
 	 */
 	public static boolean isCompatible(UnOpNode.OpType op, TypeNode operand) {
 
 		switch (op) {
 		case UMINUS:
 			return operand.isNumeric();
 		case INCR:
 			return operand.isNumeric();
 		case DECR:
 			return operand.isNumeric();
 		case CAST:
 			throw new UnsupportedOperationException("TODO");
 		case NOT:
 			return operand.isBoolean();
 		case NONE:
 			return true;
 		}
 
 		// should never get here.
 		throw new UnsupportedOperationException("OpType " + op
 				+ "not implemented!");
 	}
 
 	/**
 	 * 
 	 * @param op
 	 * @param operand
 	 * @return
 	 * @throws TypeMismatchError
 	 */
 	public static TypeNode getResult(UnOpNode.OpType op, TypeNode operand)
 			throws TypeMismatchError {
 
 		if (!isCompatible(op, operand)) {
 			throw new TypeMismatchError("Tried to call " + op + " on "
 					+ operand + "!");
 		}
 
 		switch (op) {
 		case UMINUS:
 		case INCR:
 		case DECR:
 			return (PrimitiveTypeNode) operand;
 		case CAST:
 			throw new UnsupportedOperationException("TODO");
 		case NOT:
 			return (PrimitiveTypeNode) operand;
 		case NONE:
 			return operand;
 		}
 
 		// should never get here.
 		throw new UnsupportedOperationException("OpType " + op
 				+ "not implemented!");
 	}
 
 	/**
 	 * Find the higher of two numeric types.
 	 * 
 	 * If one (or both) of the types are REAL, then REAL is returned, otherwise,
 	 * INT is returned (unless an exception is thrown).
 	 * 
 	 * @param left
 	 *            a numeric PrimitiveTypeNode
 	 * @param right
 	 *            a numeric PrimitiveTypeNode
 	 * @return a PrimitiveTypeNode representing the higher numeric type.
 	 * @throws TypeMismatchError
 	 *             if one of the passed in types is not numeric.
 	 */
 	public static PrimitiveTypeNode getHigherNumericType(TypeNode left,
 			TypeNode right) throws TypeMismatchError {
 
 		if (!left.isNumeric() || !right.isNumeric()) {
 			throw new TypeMismatchError("Tried to get higher numeric type for "
 					+ (left.isNumeric() ? right : left));
 		}
 
 		if (((PrimitiveTypeNode) left).getType() == Primitive.REAL
 				|| ((PrimitiveTypeNode) right).getType() == Primitive.REAL) {
 			return new PrimitiveTypeNode(Primitive.REAL);
 		}
 		return new PrimitiveTypeNode(Primitive.INT);
 	}
 
 	/**
 	 * Get a lower-case string representation of this node's type.
 	 * 
 	 * Note: for derived type, returns lower-case representation of the
 	 * <i>local</i> type.
 	 * 
 	 * @param node
 	 *            a TypeNode to get the string representation for.
 	 * @return a lower-case string of the TypeNode's type.
 	 */
 	public static String getLowercaseTypeName(TypeNode node) {
 
 		// prim: lower case version
 		// derived: lower case version of outer
 		// exception : lower case version of node
 
 		String typeName = null;
 
 		if (node instanceof PrimitiveTypeNode) {
 
 			typeName = ((PrimitiveTypeNode) node).getType().toString()
 					.toLowerCase();
 
 		} else if (node instanceof DerivedTypeNode) {
 
 			typeName = ((DerivedTypeNode) node).getLocalType().toString()
 					.toLowerCase();
 
 		} else if (node instanceof ExceptionTypeNode) {
 
 			typeName = ((ExceptionTypeNode) node).getExceptionType().toString()
 					.toLowerCase();
 
 		} else {
 			throw new UnsupportedOperationException(
 					"No support for TypeNode type: ");
 		}
 
 		return typeName;
 
 	}
 
 	public static boolean checkTypeHasMethod(TypeNode typeNode, String method) {
 
 		LOGGER.finer("checkTypeHasMethod on method name: " + method);
 		LOGGER.finer(" and Typenode: " + typeNode.toString());
 
 		// get the type name for this type node as a string
 		String typeName = getLowercaseTypeName(typeNode);
 
 		// form a string to check the reserved symbol table for this method
 		String methodToLookup = typeName + "." + method;
 
 		if (SymbolTable.getRootSymbolTable().isDefinedInScope(methodToLookup)) {
 			return true;
 		}
 		throw new FunctionNotDefinedError("The function: " + method
 				+ " is not supported for the type " + typeNode.getName());
 
 	}
 
 	/**
 	 * This takes a postFixExpressionNode and returns true if it is a legal
 	 * function call It will return false if the function is undeclared, if the
 	 * arguments do not match the formal parameters or if it is a method called
 	 * on an unsupported type
 	 * 
 	 * @param postFixExpressionNode
 	 * @return
 	 */
 	public static boolean checkValidFunctionCall(
 			PostfixExpressionNode postFixExpressionNode) {
 
 		// get the postfix type
 		PostfixType postFixType = postFixExpressionNode.getPostfixType();
 
 		FunctionSymbol funSym;
 		// check if the function name exists - this throws exception if it is
 		// not in the symbol table
 		if (postFixExpressionNode.isFunction()) {
 			funSym = (FunctionSymbol) SymbolTable
 					.getSymbolForIdNode(postFixExpressionNode
 							.getNameOfFunctionOrMethod());
 		} else {
 			funSym = SymbolTable.getSymbolForMethodCall(postFixExpressionNode);
 		}
 
 		// if it is a method, check if the type allows the method
 		if (postFixType == PostfixType.METHOD_NO_PARAMS
 				|| postFixType == PostfixType.METHOD_WITH_PARAMS) {
 			checkTypeHasMethod(postFixExpressionNode.getObjectName().getType(),
 					postFixExpressionNode.getMethodName().getIdentifier());
 		}
 
 		// if it has arguments, check if they match the formal arguments
 		if (postFixExpressionNode.hasArguments()) {
 			ArgumentsNode argsNode = (ArgumentsNode) postFixExpressionNode
 					.getArgsList();
 
 			// throw error if not same amount of arguments
 			if (funSym.getParametersNode().getNumParams() != argsNode
 					.getNumArguments()) {
 				throw new InvalidFunctionArgumentsError(postFixExpressionNode
 						.getNameOfFunctionOrMethod().getIdentifier()
 						+ " was called with the wrong number of arguments.");
 			}
 
 			TypeNode innerTypeOfMethodCall = null;
 			if (postFixExpressionNode.isMethod()) {
 				TypeNode typeOfObject = postFixExpressionNode.getObjectOfMethod().getType();
 				if( typeOfObject instanceof DerivedTypeNode ) {
 					innerTypeOfMethodCall = ((DerivedTypeNode) typeOfObject).getInnerTypeNode();
 				} else {
 					innerTypeOfMethodCall = typeOfObject;
 				}
 			}
 
 			// throw an error if the types of the arguments don't match the
 			// types of the formal params
 			if (!argsMatchParams(argsNode, funSym.getParametersNode(),
 					innerTypeOfMethodCall))
 				throw new InvalidFunctionArgumentsError(postFixExpressionNode
 						.getNameOfFunctionOrMethod().getIdentifier()
 						+ " was called with an invalid argument list. \n Node: " 
 						+ postFixExpressionNode.getName()
 						+ " \nArgs Node = " + postFixExpressionNode.getArgsList().getName());
 		}
 
 		// if it doesn't have arguments, ensure that the formal parameters don't
 		// require arguments
 		else {
 			if (funSym.hasParameters())
 				throw new InvalidFunctionArgumentsError(postFixExpressionNode
 						.getNameOfFunctionOrMethod().getIdentifier()
 						+ " was called with an invalid argument list.");
 		}
 
 		return true;
 	}
 
 	/**
 	 * Returns true of the types of the arguments match and are in the same
 	 * order as the types of the parameters
 	 * 
 	 * @param args
 	 * @param params
 	 * @return
 	 */
 
 	private static boolean argsMatchParams(ArgumentsNode args,
 			ParametersNode params, TypeNode innerTypeOfMethodCall) {
 
 		// if they have no sublists, and the type of the param
 		// is CHECK INNER TYPE. we check the type of the argument
 		// and the innertype are the same
 		if (args.getArgumentsNode() == null && params.getParamChild() == null
 				&& params.getType() instanceof ReservedWordTypeNode
 				&& ((ReservedWordTypeNode) params.getType()).getType() == Types.Flags.CHECK_INNER_TYPE)
 			return isSameType(args.getExpressionNode().getType(), innerTypeOfMethodCall);
 		
 		// else if they have no sublists, return if they are the same type
 		else if (args.getArgumentsNode() == null && params.getParamChild() == null)
 			return isSameType(args.getExpressionNode().getType(), params
 					.getType());
 
 		// same number of args/params?
 		if (args.getNumArguments() != params.getNumParams())
 			return false;
 
 		// if the param is CHECK_INNER_TYPE, we need to check the inner type
 		// against the argument
 		if (params.getType() instanceof ReservedWordTypeNode
 				&& ((ReservedWordTypeNode) params.getType()).getType() == Types.Flags.CHECK_INNER_TYPE) {
 			if (isSameType(args.getExpressionNode().getType(),
 					innerTypeOfMethodCall)) {
 				return argsMatchParams((ArgumentsNode) args.getArgumentsNode(),
 						params.getParamChild(), innerTypeOfMethodCall);
 			}
 		}
 		// else, if they have the same type, recurse
 		if (isSameType(args.getType(), params.getType()))
 			return argsMatchParams((ArgumentsNode) args.getArgumentsNode(),
 					params.getParamChild(), innerTypeOfMethodCall);
 
 		else
 			return false;
 
 	}
 
 	/**
 	 * Ask for the java version of this type.
 	 * 
 	 * @param type
 	 *            a TypeNode
 	 * @return the String corresponding to the Java type that this type maps
 	 *         onto.
 	 */
 	public static String getJavaType(TypeNode type) {
 
 		if (type instanceof PrimitiveTypeNode) {
 			return getJavaType((PrimitiveTypeNode) type);
 		}
 
 		throw new UnsupportedOperationException("SAM NEEDS TO FINISH THIS");
 	}
 
 	private static String getJavaType(PrimitiveTypeNode type) {
 
 		switch (type.getType()) {
 		case BOOL:
 			return "boolean";
 		case TEXT:
 			return "String";
 		case VOID:
 			return "void";
 		case INT:
 			return "int";
 		case REAL:
 			return "double";
 		}
 
 		throw new UnsupportedOperationException("This should never happen.");
 	}
 
 	/**
 	 * Ask for the Hadoop type corresponding to this TypeNode.
 	 * 
 	 * @param type
 	 *            a TypeNode
 	 * @return the string corresponding to the Hadoop type.
 	 */
 	public static String getHadoopType(TypeNode type) {
 		
 		if (type instanceof PrimitiveTypeNode) {
 			return getHadoopType((PrimitiveTypeNode) type);
 		} else if (type instanceof DerivedTypeNode) {
 			return getHadoopType((DerivedTypeNode) type);
 		}
 
 		throw new UnsupportedOperationException(type
 				+ " is not allowed for @Map and @Reduce headers.");
 	}
 
 	private static String getHadoopType(PrimitiveTypeNode type) {
 
 		switch (type.getType()) {
 		case BOOL:
 			return "BooleanWritable";
 		case TEXT:
 			return "Text";
 		case INT:
 			return "IntWritable";
 		case REAL:
 			return "DoubleWritable";
 		}
 
 		throw new UnsupportedOperationException(type
 				+ " is not allowed for @Map and @Reduce headers.");
 	}
 
 	private static String getHadoopType(DerivedTypeNode type) {
 
 		switch (type.getLocalType()) {
 		case ITER:
 			return getHadoopType(type.getInnerTypeNode());
 		}
 
 		throw new UnsupportedOperationException(type
 				+ " is not allowed for @Map and @Reduce headers.");
 	}
 
 	/**
 	 * 
 	 * @param type
 	 *            a PrimitiveTypeNode
 	 * @return the string corresponding to the declaration of Hadoop type.
 	 */
 	public static String getNewHadoopTypeDeclaration(PrimitiveTypeNode type) {
 		switch (type.getType()) {
 		case BOOL:
 			return "new BooleanWritable";
 		case TEXT:
 			return "new Text";
 		case INT:
 			return "new IntWritable";
 		case REAL:
 			return "new DoubleWritable";
 		}
 		throw new UnsupportedOperationException(type
 				+ " is not allowed for @Map and @Reduce headers.");
 	}
 
 }
