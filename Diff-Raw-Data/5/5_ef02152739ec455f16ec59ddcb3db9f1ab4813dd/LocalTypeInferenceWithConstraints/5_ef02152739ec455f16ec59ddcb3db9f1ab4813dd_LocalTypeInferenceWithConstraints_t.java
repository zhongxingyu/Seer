 package cd.semantic.ti;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import cd.exceptions.SemanticFailure;
 import cd.exceptions.SemanticFailure.Cause;
 import cd.ir.AstVisitor;
 import cd.ir.ExprVisitor;
 import cd.ir.ast.Assign;
 import cd.ir.ast.BinaryOp;
 import cd.ir.ast.BinaryOp.BOp;
 import cd.ir.ast.BooleanConst;
 import cd.ir.ast.BuiltInRead;
 import cd.ir.ast.BuiltInReadFloat;
 import cd.ir.ast.BuiltInWrite;
 import cd.ir.ast.BuiltInWriteFloat;
 import cd.ir.ast.Cast;
 import cd.ir.ast.Expr;
 import cd.ir.ast.Field;
 import cd.ir.ast.FloatConst;
 import cd.ir.ast.Index;
 import cd.ir.ast.IntConst;
 import cd.ir.ast.MethodCall;
 import cd.ir.ast.MethodCallExpr;
 import cd.ir.ast.MethodDecl;
 import cd.ir.ast.NewArray;
 import cd.ir.ast.NewObject;
 import cd.ir.ast.NullConst;
 import cd.ir.ast.ReturnStmt;
 import cd.ir.ast.ThisRef;
 import cd.ir.ast.Var;
 import cd.ir.symbols.ArrayTypeSymbol;
 import cd.ir.symbols.ClassSymbol;
 import cd.ir.symbols.MethodSymbol;
 import cd.ir.symbols.TypeSymbol;
 import cd.ir.symbols.VariableSymbol;
 import cd.ir.symbols.VariableSymbol.Kind;
 import cd.semantic.TypeSymbolTable;
 import cd.semantic.ti.constraintSolving.ConstantTypeSet;
 import cd.semantic.ti.constraintSolving.ConstraintSolver;
 import cd.semantic.ti.constraintSolving.ConstraintSystem;
 import cd.semantic.ti.constraintSolving.TypeVariable;
 import cd.semantic.ti.constraintSolving.constraints.ConstraintCondition;
 
 public class LocalTypeInferenceWithConstraints extends LocalTypeInference {
 
 	@Override
 	public void inferTypes(MethodDecl mdecl, TypeSymbolTable typeSymbols) {
 		ConstraintGenerator constraintGen = new ConstraintGenerator(mdecl,
 				typeSymbols);
 		constraintGen.generate();
 		ConstraintSolver solver = new ConstraintSolver(
 				constraintGen.getConstraintSystem());
 		solver.solve();
 		if (!solver.hasSolution()) {
 			throw new SemanticFailure(Cause.TYPE_ERROR,
 					"Type inference was unable to resolve type constraints");
 		} else {
 			for (VariableSymbol varSym : mdecl.sym.getLocals()) {
 				Set<TypeSymbol> possibleTypes = constraintGen
 						.getPossibleTypes(varSym);
 				TypeSymbol type = null;
 				if (possibleTypes.isEmpty()) {
 					// Use the bottom type if there are no types in the type
 					// set. Since the constraint system has been solved
 					// successfully, this usually (always?) means that the
 					// variable symbol is not used at all.
 					type = typeSymbols.getBottomType();
 				} else if (possibleTypes.size() == 1) {
 					type = possibleTypes.iterator().next();
 				} else if (possibleTypes.size() > 1) {
 					// NOTE: we may still try to take the join (lca). This is
 					// sometimes necessary.
 					TypeSymbol[] typesArray = possibleTypes
 							.toArray(new TypeSymbol[possibleTypes.size()]);
 					TypeSymbol lca = typeSymbols.getLCA(typesArray);
 					if (lca != typeSymbols.getTopType()) {
 						type = lca;
 					} else {
 						throw new SemanticFailure(Cause.TYPE_ERROR,
 								"Type inference resulted in ambiguous type for "
 										+ varSym.name);
 					}
 				}
 				varSym.setType(type);
 			}
 		}
 
 	}
 
 	/**
 	 * ConstraintGenerator is responsible for creating as many type variables
 	 * and constraints as necessary for a method.
 	 */
 	public class ConstraintGenerator extends AstVisitor<TypeVariable, Void> {
 		private final TypeSymbolTable typeSymbols;
 		private final MethodDecl mdecl;
 		private final ConstraintSystem constraintSystem;
 		private final MethodSymbolCache methodSymbolCache;
 		private ClassSymbolFieldCache classFieldSymbolCache;
 		
 		private TypeVariable returnTypeVariable;
 		
 		// Map to remember the type variables for our parameters and locals,
 		// i.e. what we are eventually interested in.
 		// Note to avoid confusion: VariableSymbols are symbols for program
 		// variables while
 		// these TypeVariables are constraint solver variables describing the
 		// type of such program variables
 		private final Map<VariableSymbol, TypeVariable> localSymbolVariables = new HashMap<>();
 
 		public ConstraintGenerator(MethodDecl mdecl, TypeSymbolTable typeSymbols) {
 			this.typeSymbols = typeSymbols;
 			this.mdecl = mdecl;
 			this.constraintSystem = new ConstraintSystem();
 			this.methodSymbolCache = MethodSymbolCache.of(typeSymbols);
 			this.classFieldSymbolCache = ClassSymbolFieldCache.of(typeSymbols);
 		}
 
 		public ConstraintSystem getConstraintSystem() {
 			return constraintSystem;
 		}
 
 		public Set<TypeSymbol> getPossibleTypes(VariableSymbol varSym) {
 			return localSymbolVariables.get(varSym).getTypes();
 		}
 
 		public void generate() {
 			// variables and constraints for parameters (types given!)
 			MethodSymbol msym = mdecl.sym;
 			for (VariableSymbol varSym : msym.getParameters()) {
 				TypeVariable typeVar = constraintSystem.addTypeVariable();
 				ConstantTypeSet typeConst = new ConstantTypeSet(
 						varSym.getType());
 				constraintSystem.addConstEquality(typeVar, typeConst);
 				localSymbolVariables.put(varSym, typeVar);
 			}
 
 			// type variables for local variables
 			for (VariableSymbol varSym : msym.getLocals()) {
 				TypeVariable typeVar = constraintSystem.addTypeVariable();
 				localSymbolVariables.put(varSym, typeVar);
 			}
 
 			// type variable and constraints for return value (if any)
 			if (msym.returnType != typeSymbols.getVoidType()) {
 				returnTypeVariable = constraintSystem.addTypeVariable();
 				ConstantTypeSet typeConst = new ConstantTypeSet(msym.returnType);
 				constraintSystem
 						.addConstEquality(returnTypeVariable, typeConst);
 			}
 
 			ConstraintStmtVisitor constraintVisitor = new ConstraintStmtVisitor();
 			mdecl.accept(constraintVisitor, null);
 		}
 
 		public class ConstraintStmtVisitor extends AstVisitor<Void, Void> {
 			ConstraintExprVisitor exprVisitor = new ConstraintExprVisitor();
 
 			@Override
 			public Void returnStmt(ReturnStmt ast, Void arg) {
 				if (ast.arg() != null) {
 					TypeVariable exprType = exprVisitor.visit(ast.arg(), null);
 					constraintSystem.addVarEquality(exprType,
 							returnTypeVariable);
 				}
 				return null;
 			}
 
 			@Override
 			public Void assign(Assign assign, Void arg) {
 				Expr lhs = assign.left();
 				if (lhs instanceof Var) {
 					VariableSymbol varSym = ((Var) lhs).getSymbol();
 					if (varSym.getKind().equals(Kind.LOCAL)) {
 						TypeVariable exprTypeVar = exprVisitor.visit(
 								assign.right(), null);
 						TypeVariable localTypeVar = localSymbolVariables
 								.get(varSym);
 						constraintSystem.addVarInequality(exprTypeVar,
 								localTypeVar);
 					}
 				}
 				return null;
 			}
 			
 			@Override
 			public Void builtInWrite(BuiltInWrite ast, Void arg) {
 				TypeVariable argType = exprVisitor.visit(ast.arg(), null);
 				ConstantTypeSet intTypeSet = new ConstantTypeSet(
 						typeSymbols.getIntType());
 				constraintSystem.addConstEquality(argType, intTypeSet);
 				return null;
 			}
 			
 			@Override
 			public Void builtInWriteFloat(BuiltInWriteFloat ast, Void arg) {
 				TypeVariable argType = exprVisitor.visit(ast.arg(), null);
 				ConstantTypeSet floatTypeSet = new ConstantTypeSet(
 						typeSymbols.getFloatType());
 				constraintSystem.addConstEquality(argType, floatTypeSet);
 				return null;
 			}
 			
 			@Override
 			public Void methodCall(MethodCall call, Void arg) {
 				List<Expr> arguments = call.argumentsWithoutReceiver();
 				Collection<MethodSymbol> methodSymbols = methodSymbolCache.get(call.methodName, arguments.size());
 				Expr receiver = call.receiver();
 				TypeVariable receiverTypeVar = exprVisitor.visit(receiver, null);
 				
 				for (MethodSymbol msym : methodSymbols) {
 					for (int argNum = 0; argNum < arguments.size(); argNum++) {
 						Expr argument = arguments.get(argNum);
 						TypeVariable argTypeVar = exprVisitor.visit(argument, null);
 						// TODO: expand formal argument type to set of all subtypes of that type. 
 						// This is not correct yet, since no subtypes of formal type could be passed as arguments.
 						VariableSymbol paramSym = msym.getParameter(argNum);
 						ConstantTypeSet expectedArgType = new ConstantTypeSet(paramSym.getType());
 						ConstraintCondition condition = new ConstraintCondition(msym.owner, receiverTypeVar);
 						constraintSystem.addUpperBound(argTypeVar, expectedArgType, condition);
 					}
 				}
 				return null;
 			}
 			
 			// TODO: other statements which are necessary
 		}
 
 		public class ConstraintExprVisitor extends
 				ExprVisitor<TypeVariable, Void> {
 			@Override
 			public TypeVariable var(Var ast, Void arg) {
 				VariableSymbol varSym = ast.getSymbol();
 				return localSymbolVariables.get(varSym);
 			}
 
 			@Override
 			public TypeVariable intConst(IntConst ast, Void arg) {
 				TypeVariable typeVar = constraintSystem.addTypeVariable();
 				ConstantTypeSet intTypeSet = new ConstantTypeSet(
 						typeSymbols.getIntType());
 				constraintSystem.addConstEquality(typeVar, intTypeSet);
 				return typeVar;
 			}
 
 			@Override
 			public TypeVariable floatConst(FloatConst ast, Void arg) {
 				TypeVariable typeVar = constraintSystem.addTypeVariable();
 				ConstantTypeSet floatTypeSet = new ConstantTypeSet(
 						typeSymbols.getFloatType());
 				constraintSystem.addConstEquality(typeVar, floatTypeSet);
 				return typeVar;
 			}
 
 			@Override
 			public TypeVariable booleanConst(BooleanConst ast, Void arg) {
 				TypeVariable typeVar = constraintSystem.addTypeVariable();
 				ConstantTypeSet booleanTypeSet = new ConstantTypeSet(
 						typeSymbols.getBooleanType());
 				constraintSystem.addConstEquality(typeVar, booleanTypeSet);
 				return typeVar;
 			}
 			
 			@Override
 			public TypeVariable nullConst(NullConst ast, Void arg) {
 				Set<TypeSymbol> referenceTypeSymbols = new HashSet<>(typeSymbols.getReferenceTypeSymbols());
 				referenceTypeSymbols.remove(typeSymbols.getNullType());
 				ConstantTypeSet allReferenceTypeSet = new ConstantTypeSet(referenceTypeSymbols);
 				TypeVariable nullTypeVar = constraintSystem.addTypeVariable();
 				constraintSystem.addConstEquality(nullTypeVar, allReferenceTypeSet);
 				return nullTypeVar;
 			}
 			
 			@Override
 			public TypeVariable newObject(NewObject ast, Void arg) {
 				TypeVariable typeVar = constraintSystem.addTypeVariable();
 				TypeSymbol classSym = typeSymbols.getType(ast.typeName);
 				ConstantTypeSet classTypeSet = new ConstantTypeSet(classSym);
 				constraintSystem.addConstEquality(typeVar, classTypeSet);
 				return typeVar;
 			}
 			
 			@Override
 			public TypeVariable newArray(NewArray ast, Void arg) {
 				TypeVariable typeVar = constraintSystem.addTypeVariable();
				TypeSymbol arraySym = typeSymbols.get(ast.typeName);
 				ConstantTypeSet arrayTypeSet = new ConstantTypeSet(arraySym);
 				constraintSystem.addConstEquality(typeVar, arrayTypeSet);
 				return typeVar;
 			}
 			
 			@Override
 			public TypeVariable thisRef(ThisRef ast, Void arg) {
 				TypeVariable typeVar = constraintSystem.addTypeVariable();
 				ClassSymbol classSymbol = mdecl.sym.owner;
 				// TODO: This is obviously not correct yet
 				// The actual type of this may of course be a subtype of the
 				// class in which the current method is defined in.
 				ConstantTypeSet classTypeSet = new ConstantTypeSet(classSymbol);
 				constraintSystem.addConstEquality(typeVar, classTypeSet);
 				return typeVar;
 			}
 			
 			@Override
 			public TypeVariable cast(Cast ast, Void arg) {
 				TypeVariable typeVar = constraintSystem.addTypeVariable();
 				// generate constraints for the expression, too
 				visit(ast.arg(), null);
 				TypeSymbol castResultType = typeSymbols.getType(ast.typeName);
 				ConstantTypeSet resultTypeSet = new ConstantTypeSet(castResultType);
 				constraintSystem.addConstEquality(typeVar, resultTypeSet);
 				return typeVar;
 			}
 			
			
			
 			@Override
 			public TypeVariable index(Index idx, Void arg) {
 				Expr lhs = idx.left();
 				if (lhs instanceof Var) {
 					VariableSymbol varSym = ((Var) lhs).getSymbol();
 					if (varSym.getKind().equals(Kind.LOCAL)) {
 						TypeVariable localTypeVar = localSymbolVariables
 								.get(varSym);
 						TypeVariable indexTypeVar = visit(idx.right(), null);
 						constraintSystem.addConstEquality(indexTypeVar, new ConstantTypeSet(typeSymbols.getIntType()));
 						// TODO: maybe exclude types like _bottom[]?
 						Set<ArrayTypeSymbol> allArraySyms = new HashSet<>(typeSymbols.getArrayTypeSymbols());
 						ConstantTypeSet arrayTypesSet = new ConstantTypeSet(allArraySyms);
 						constraintSystem.addConstEquality(localTypeVar, arrayTypesSet);
 						TypeVariable resultVar = constraintSystem.addTypeVariable();
 						// TODO: Constrain resultVar to have the same types as arrayTypesSet, but with the "[]" removed.
 						//	 	 We probably need a new constraint type for that.
 						return resultVar;
 					}
 				}
 				return null;
 			}
 			
 			@Override
 			public TypeVariable builtInRead(BuiltInRead ast, Void arg) {
 				TypeVariable typeVar = constraintSystem.addTypeVariable();
 				ConstantTypeSet intTypeSet = new ConstantTypeSet(
 						typeSymbols.getIntType());
 				constraintSystem.addConstEquality(typeVar, intTypeSet);
 				return typeVar;
 			}
 			
 			@Override
 			public TypeVariable builtInReadFloat(BuiltInReadFloat ast, Void arg) {
 				TypeVariable typeVar = constraintSystem.addTypeVariable();
 				ConstantTypeSet floatTypeSet = new ConstantTypeSet(
 						typeSymbols.getFloatType());
 				constraintSystem.addConstEquality(typeVar, floatTypeSet);
 				return typeVar;
 			}
 			
 			@Override
 			public TypeVariable binaryOp(BinaryOp binaryOp, Void arg) {
 				BOp op = binaryOp.operator;
 				TypeVariable leftTypeVar = visit(binaryOp.left(), null);
 				TypeVariable rightTypeVar = visit(binaryOp.right(), null);
 				TypeVariable resultVar;
 				
 				ConstantTypeSet numTypes = new ConstantTypeSet(
 						new HashSet<>(typeSymbols.getNumericalTypeSymbols()));
 				ConstantTypeSet booleanType = new ConstantTypeSet(typeSymbols.getBooleanType());
 
 				switch (op) {
 				case B_TIMES:
 				case B_DIV:
 				case B_MOD:
 				case B_PLUS:
 				case B_MINUS:
 					constraintSystem.addUpperBound(leftTypeVar, numTypes);
 					constraintSystem.addVarEquality(leftTypeVar, rightTypeVar);
 					resultVar = leftTypeVar;
 					break;
 				case B_AND:
 				case B_OR:
 					constraintSystem.addVarEquality(leftTypeVar, rightTypeVar);
 					constraintSystem.addConstEquality(leftTypeVar, booleanType);
 					resultVar = constraintSystem.addTypeVariable();
 					constraintSystem.addConstEquality(resultVar, booleanType);
 					break;
 				case B_EQUAL:
 				case B_NOT_EQUAL:
 					constraintSystem.addVarEquality(leftTypeVar, rightTypeVar);
 					resultVar = constraintSystem.addTypeVariable();
 					constraintSystem.addConstEquality(resultVar, booleanType);
 					break;
 				case B_LESS_THAN:
 				case B_LESS_OR_EQUAL:
 				case B_GREATER_THAN:
 				case B_GREATER_OR_EQUAL:
 					constraintSystem.addUpperBound(leftTypeVar, numTypes);
 					constraintSystem.addVarEquality(leftTypeVar, rightTypeVar);
 					resultVar = constraintSystem.addTypeVariable();
 					constraintSystem.addConstEquality(resultVar, booleanType);
 					break;
 				default:
 					resultVar = null;
 				}
 				
 				assert(resultVar != null);
 				return resultVar;
 			}
 			
 			
 			@Override
 			public TypeVariable methodCall(MethodCallExpr call, Void arg) {
 				List<Expr> arguments = call.argumentsWithoutReceiver();
 				Collection<MethodSymbol> methodSymbols = methodSymbolCache.get(
 						call.methodName, arguments.size());
 				Expr receiver = call.receiver();
 				TypeVariable receiverTypeVar = visit(receiver, null);
 				TypeVariable returnTypeVar = constraintSystem.addTypeVariable();
 
 
 				for (MethodSymbol msym : methodSymbols) {
 					ConstraintCondition condition = new ConstraintCondition(msym.owner, receiverTypeVar);
 					for (int argNum = 0; argNum < arguments.size(); argNum++) {
 						TypeVariable argTypeVar = visit(arguments.get(argNum),
 								null);
 						// TODO: expand formal argument type to set of all
 						// subtypes of that type.
 						// This is not correct yet, since no subtypes of formal
 						// type could be passed as arguments.
 						VariableSymbol paramSym = msym.getParameter(argNum);
 						ConstantTypeSet expectedArgType = new ConstantTypeSet(paramSym.getType());
 						constraintSystem.addUpperBound(argTypeVar,
 								expectedArgType, condition);
 					}
 					TypeSymbol resultSym = msym.returnType;
 					// TODO: Is it possible to have a call to a method with void
 					// return type here?
 					// If not, remove if.
 					if (resultSym != typeSymbols.getVoidType()) {
 						ConstantTypeSet expectedResultTypes = new ConstantTypeSet(
 								resultSym);
 						constraintSystem.addUpperBound(returnTypeVar,
 								expectedResultTypes, condition);
 					}
 				}
 				return returnTypeVar;
 			}
 			
 
 			// TODO: all expression cases
 		}
 	}
 
 }
