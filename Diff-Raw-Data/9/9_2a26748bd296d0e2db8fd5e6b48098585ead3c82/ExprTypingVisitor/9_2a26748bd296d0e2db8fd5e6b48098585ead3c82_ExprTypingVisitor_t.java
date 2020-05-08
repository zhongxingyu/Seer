 package cd.semantic;
 
 import cd.exceptions.SemanticFailure;
 import cd.exceptions.SemanticFailure.Cause;
 import cd.ir.ExprVisitor;
 import cd.ir.ast.Ast;
 import cd.ir.ast.BinaryOp;
 import cd.ir.ast.BooleanConst;
 import cd.ir.ast.BuiltInRead;
 import cd.ir.ast.BuiltInReadFloat;
 import cd.ir.ast.Cast;
 import cd.ir.ast.Expr;
 import cd.ir.ast.Field;
 import cd.ir.ast.FloatConst;
 import cd.ir.ast.Index;
 import cd.ir.ast.IntConst;
 import cd.ir.ast.MethodCallExpr;
 import cd.ir.ast.NewArray;
 import cd.ir.ast.NewObject;
 import cd.ir.ast.NullConst;
 import cd.ir.ast.ThisRef;
 import cd.ir.ast.UnaryOp;
 import cd.ir.ast.Var;
 import cd.ir.ast.BinaryOp.BOp;
 import cd.ir.symbols.ArrayTypeSymbol;
 import cd.ir.symbols.ClassSymbol;
 import cd.ir.symbols.MethodSymbol;
 import cd.ir.symbols.TypeSymbol;
 import cd.ir.symbols.VariableSymbol;
 
 public class ExprTypingVisitor extends
 		ExprVisitor<TypeSymbol, SymbolTable<VariableSymbol>> {
 
 	private final TypeSymbolTable typeSymbols;
 
 	public ExprTypingVisitor(TypeSymbolTable typeSymbols) {
 		this.typeSymbols = typeSymbols;
 	}
 
 	@Override
 	public TypeSymbol visit(Expr ast, SymbolTable<VariableSymbol> scope) {
 		TypeSymbol type = super.visit(ast, scope);
 		ast.setType(type);
 		return type;
 	}
 
 	/**
 	 * Convenience alias for {@link #visit(Expr, SymbolTable)}.
 	 */
 	public TypeSymbol type(Expr expr, SymbolTable<VariableSymbol> scope) {
 		return visit(expr, scope);
 	}
 
 	/**
 	 * @todo Copied from {@link TypeChecker}. Once the method does not depend on
 	 *       the type symbol table anymore, it should be possible to get rid of
 	 *       this redundancy again
 	 */
 	public void checkType(Expr ast, TypeSymbol expectedType,
 			SymbolTable<VariableSymbol> scope) {
 		TypeSymbol actualType = type(ast, scope);
 		checkType(expectedType, actualType);
 	}
 
 	public void checkType(TypeSymbol expectedType, TypeSymbol actualType) {
 		if (!typeSymbols.isSubType(expectedType, actualType)) {
 			throw new SemanticFailure(Cause.TYPE_ERROR,
 					"Expected %s but type was %s", expectedType, actualType);
 		}
 	}
 
 	/**
 	 * Checks whether two expressions have the same type and throws an exception
 	 * if not.
 	 * 
 	 * @return the common type of the two expression
 	 */
 	public TypeSymbol typeEquality(Expr leftExpr, Expr rightExpr,
 			SymbolTable<VariableSymbol> scope) {
 		TypeSymbol leftType = type(leftExpr, scope);
 		TypeSymbol rightType = type(rightExpr, scope);
 
 		if (leftType != rightType) {
 			throw new SemanticFailure(Cause.TYPE_ERROR,
 					"Expected operand types to be equal but found %s, %s",
 					leftType, rightType);
 		}
 
 		return leftType;
 
 	}
 
 	public void typeIsPrimitive(TypeSymbol type) {
		// TODO: This method should not know the exhaustive list of primitive
		// numerical types. DRY baby, DRY!
		if (!typeSymbols.isSubType(typeSymbols.getIntType(), type)
				&& !typeSymbols.isSubType(typeSymbols.getFloatType(), type)) {
 			throw new SemanticFailure(Cause.TYPE_ERROR,
 					"Expected %s or %s for operands but found type %s",
 					typeSymbols.getIntType(), typeSymbols.getFloatType(), type);
 		}
 	}
 
 	public void typeIsValidForOperator(TypeSymbol type, BOp operator) {
 		if (operator == BOp.B_MOD && type == typeSymbols.getFloatType()) {
 			throw new SemanticFailure(Cause.TYPE_ERROR,
 					"Operation %s not valid for type %s", operator, type);
 		}
 	}
 
 	public ArrayTypeSymbol asArray(TypeSymbol type) {
 		if (type instanceof ArrayTypeSymbol) {
 			return (ArrayTypeSymbol) type;
 		}
 		throw new SemanticFailure(Cause.TYPE_ERROR,
 				"An array type was required, but %s was found", type);
 	}
 
 	@Override
 	public TypeSymbol binaryOp(BinaryOp binaryOp,
 			SymbolTable<VariableSymbol> scope) {
 		switch (binaryOp.operator) {
 		case B_TIMES:
 		case B_DIV:
 		case B_MOD:
 		case B_PLUS:
 		case B_MINUS: {
 			TypeSymbol type = typeEquality(binaryOp.left(), binaryOp.right(),
 					scope);
 			typeIsPrimitive(type);
 			typeIsValidForOperator(type, binaryOp.operator);
 			return type;
 		}
 		case B_AND:
 		case B_OR:
 			checkType(binaryOp.left(), typeSymbols.getBooleanType(), scope);
 			checkType(binaryOp.right(), typeSymbols.getBooleanType(), scope);
 			return typeSymbols.getBooleanType();
 
 		case B_EQUAL:
 		case B_NOT_EQUAL:
 			TypeSymbol left = type(binaryOp.left(), scope);
 			TypeSymbol right = type(binaryOp.right(), scope);
 			if (typeSymbols.isSubType(left, right)
 					|| typeSymbols.isSubType(right, left))
 				return typeSymbols.getBooleanType();
 			throw new SemanticFailure(Cause.TYPE_ERROR,
 					"Types %s and %s could never be equal", left, right);
 
 		case B_LESS_THAN:
 		case B_LESS_OR_EQUAL:
 		case B_GREATER_THAN:
 		case B_GREATER_OR_EQUAL: {
 			TypeSymbol type = typeEquality(binaryOp.left(), binaryOp.right(),
 					scope);
 			typeIsPrimitive(type);
 			return typeSymbols.getBooleanType();
 		}
 		}
 		throw new RuntimeException("Unhandled operator " + binaryOp.operator);
 	}
 
 	@Override
 	public TypeSymbol booleanConst(BooleanConst booleanConst,
 			SymbolTable<VariableSymbol> scope) {
 		return typeSymbols.getBooleanType();
 	}
 
 	@Override
 	public TypeSymbol builtInRead(BuiltInRead read,
 			SymbolTable<VariableSymbol> scope) {
 		return typeSymbols.getIntType();
 	}
 
 	@Override
 	public TypeSymbol builtInReadFloat(BuiltInReadFloat readFloat,
 			SymbolTable<VariableSymbol> scope) {
 		return typeSymbols.getFloatType();
 	}
 
 	@Override
 	public TypeSymbol cast(Cast cast, SymbolTable<VariableSymbol> scope) {
 		TypeSymbol argType = type(cast.arg(), scope);
 		cast.typeSym = typeSymbols.getType(cast.typeName);
 
 		if (typeSymbols.isSubType(argType, cast.typeSym)
 				|| typeSymbols.isSubType(cast.typeSym, argType)) {
 			return cast.typeSym;
 		}
 
 		throw new SemanticFailure(Cause.TYPE_ERROR,
 				"Types %s and %s in cast are completely unrelated.", argType,
 				cast.typeSym);
 	}
 
 	@Override
 	protected TypeSymbol dfltExpr(Expr expr, SymbolTable<VariableSymbol> scope) {
 		throw new RuntimeException("Unhandled type");
 	}
 
 	@Override
 	public TypeSymbol field(Field field, SymbolTable<VariableSymbol> scope) {
 		// Class of the receiver of the field access
 		ClassSymbol argType = TypeChecker.asClass(type(field.arg(), scope));
 		field.sym = argType.getField(field.fieldName);
 		if (field.sym == null) {
 			throw new SemanticFailure(Cause.NO_SUCH_FIELD,
 					"Type %s has no field %s", argType, field.fieldName);
 		}
 		return field.sym.getType();
 	}
 
 	@Override
 	public TypeSymbol index(Index index, SymbolTable<VariableSymbol> scope) {
 		ArrayTypeSymbol argType = asArray(type(index.left(), scope));
 		checkType(index.right(), typeSymbols.getIntType(), scope);
 		return argType.elementType;
 	}
 
 	@Override
 	public TypeSymbol intConst(IntConst intConst,
 			SymbolTable<VariableSymbol> scope) {
 		return typeSymbols.getIntType();
 	}
 
 	@Override
 	public TypeSymbol floatConst(FloatConst floatConst,
 			SymbolTable<VariableSymbol> scope) {
 		return typeSymbols.getFloatType();
 	}
 
 	@Override
 	public TypeSymbol newArray(NewArray newArray,
 			SymbolTable<VariableSymbol> scope) {
 		checkType(newArray.arg(), typeSymbols.getIntType(), scope);
 		return typeSymbols.getType(newArray.typeName);
 	}
 
 	@Override
 	public TypeSymbol newObject(NewObject newObject,
 			SymbolTable<VariableSymbol> scope) {
 		return typeSymbols.getType(newObject.typeName);
 	}
 
 	@Override
 	public TypeSymbol nullConst(NullConst nullConst,
 			SymbolTable<VariableSymbol> scope) {
 		return typeSymbols.getNullType();
 	}
 
 	@Override
 	public TypeSymbol thisRef(ThisRef thisRef, SymbolTable<VariableSymbol> scope) {
 		VariableSymbol vsym = scope.get("this");
 		return vsym.getType();
 	}
 
 	@Override
 	public TypeSymbol unaryOp(UnaryOp unaryOp, SymbolTable<VariableSymbol> scope) {
 		TypeSymbol type = type(unaryOp.arg(), scope);
 		switch (unaryOp.operator) {
 		case U_PLUS:
 		case U_MINUS:
 			typeIsPrimitive(type);
 			return type;
 		case U_BOOL_NOT:
 			checkType(typeSymbols.getBooleanType(), type);
			return type;
 		}
 		throw new RuntimeException("Unknown unary op " + unaryOp.operator);
 	}
 
 	@Override
 	public TypeSymbol var(Var var, SymbolTable<VariableSymbol> scope) {
 		VariableSymbol symbol = scope.get(var.getName());
 		if (symbol == null) {
 			throw new SemanticFailure(Cause.NO_SUCH_VARIABLE,
 					"No variable %s was found", var.getName());
 		}
 		var.setSymbol(symbol);
 		return symbol.getType();
 	}
 
 	@Override
 	public TypeSymbol methodCall(MethodCallExpr methodCall,
 			SymbolTable<VariableSymbol> scope) {
 		ClassSymbol rcvrType = TypeChecker.asClass(type(methodCall.receiver(),
 				scope));
 		MethodSymbol mthd = rcvrType.getMethod(methodCall.methodName);
 		if (mthd == null) {
 			throw new SemanticFailure(Cause.NO_SUCH_METHOD,
 					"Class %s has no method %s()", rcvrType.name,
 					methodCall.methodName);
 		}
 
 		methodCall.sym = mthd;
 
 		// Check that the number of arguments is correct.
 		if (methodCall.argumentsWithoutReceiver().size() != mthd
 				.getParameters().size()) {
 			throw new SemanticFailure(Cause.WRONG_NUMBER_OF_ARGUMENTS,
 					"Method %s() takes %d arguments, but was invoked with %d",
 					methodCall.methodName, mthd.getParameters().size(),
 					methodCall.argumentsWithoutReceiver().size());
 		}
 
 		// Check that the arguments are of correct type.
 		int i = 0;
 		for (Ast argAst : methodCall.argumentsWithoutReceiver()) {
 			checkType((Expr) argAst, mthd.getParameters().get(i++).getType(),
 					scope);
 		}
 
 		return methodCall.sym.returnType;
 
 	}
 
 }
