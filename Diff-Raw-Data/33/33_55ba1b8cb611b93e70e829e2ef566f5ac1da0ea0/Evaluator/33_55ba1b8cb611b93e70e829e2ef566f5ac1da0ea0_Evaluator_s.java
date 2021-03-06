 
 package org.meta_environment.rascal.interpreter;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 
 import org.eclipse.imp.pdb.facts.IBool;
 import org.eclipse.imp.pdb.facts.IDouble;
 import org.eclipse.imp.pdb.facts.IInteger;
 import org.eclipse.imp.pdb.facts.IList;
 import org.eclipse.imp.pdb.facts.IListWriter;
 import org.eclipse.imp.pdb.facts.IMap;
 import org.eclipse.imp.pdb.facts.IMapWriter;
 import org.eclipse.imp.pdb.facts.INode;
 import org.eclipse.imp.pdb.facts.IRelation;
 import org.eclipse.imp.pdb.facts.IRelationWriter;
 import org.eclipse.imp.pdb.facts.ISet;
 import org.eclipse.imp.pdb.facts.ISetWriter;
 import org.eclipse.imp.pdb.facts.ISourceRange;
 import org.eclipse.imp.pdb.facts.IString;
 import org.eclipse.imp.pdb.facts.ITree;
 import org.eclipse.imp.pdb.facts.ITuple;
 import org.eclipse.imp.pdb.facts.IValue;
 import org.eclipse.imp.pdb.facts.IValueFactory;
 import org.eclipse.imp.pdb.facts.type.FactTypeError;
 import org.eclipse.imp.pdb.facts.type.ListType;
 import org.eclipse.imp.pdb.facts.type.MapType;
 import org.eclipse.imp.pdb.facts.type.NamedTreeType;
 import org.eclipse.imp.pdb.facts.type.ParameterType;
 import org.eclipse.imp.pdb.facts.type.RelationType;
 import org.eclipse.imp.pdb.facts.type.SetType;
 import org.eclipse.imp.pdb.facts.type.TreeNodeType;
 import org.eclipse.imp.pdb.facts.type.TupleType;
 import org.eclipse.imp.pdb.facts.type.Type;
 import org.eclipse.imp.pdb.facts.type.TypeFactory;
 import org.meta_environment.rascal.ast.ASTFactory;
 import org.meta_environment.rascal.ast.Case;
 import org.meta_environment.rascal.ast.Declaration;
 import org.meta_environment.rascal.ast.Declarator;
 import org.meta_environment.rascal.ast.FunctionDeclaration;
 import org.meta_environment.rascal.ast.FunctionModifier;
 import org.meta_environment.rascal.ast.Generator;
 import org.meta_environment.rascal.ast.Import;
 import org.meta_environment.rascal.ast.Module;
 import org.meta_environment.rascal.ast.Name;
 import org.meta_environment.rascal.ast.NullASTVisitor;
 import org.meta_environment.rascal.ast.QualifiedName;
 import org.meta_environment.rascal.ast.Signature;
 import org.meta_environment.rascal.ast.Statement;
 import org.meta_environment.rascal.ast.Toplevel;
 import org.meta_environment.rascal.ast.TypeArg;
 import org.meta_environment.rascal.ast.ValueProducer;
 import org.meta_environment.rascal.ast.Variant;
 import org.meta_environment.rascal.ast.Assignable.Constructor;
 import org.meta_environment.rascal.ast.Assignable.FieldAccess;
 import org.meta_environment.rascal.ast.Declaration.Annotation;
 import org.meta_environment.rascal.ast.Declaration.Data;
 import org.meta_environment.rascal.ast.Declaration.Function;
 import org.meta_environment.rascal.ast.Declaration.Rule;
 import org.meta_environment.rascal.ast.Declaration.Tag;
 import org.meta_environment.rascal.ast.Declaration.Variable;
 import org.meta_environment.rascal.ast.Declaration.View;
 import org.meta_environment.rascal.ast.Expression.Addition;
 import org.meta_environment.rascal.ast.Expression.Ambiguity;
 import org.meta_environment.rascal.ast.Expression.And;
 import org.meta_environment.rascal.ast.Expression.Area;
 import org.meta_environment.rascal.ast.Expression.AreaInFileLocation;
 import org.meta_environment.rascal.ast.Expression.AreaLocation;
 import org.meta_environment.rascal.ast.Expression.Bracket;
 import org.meta_environment.rascal.ast.Expression.CallOrTree;
 import org.meta_environment.rascal.ast.Expression.Closure;
 import org.meta_environment.rascal.ast.Expression.ClosureCall;
 import org.meta_environment.rascal.ast.Expression.Composition;
 import org.meta_environment.rascal.ast.Expression.Comprehension;
 import org.meta_environment.rascal.ast.Expression.Division;
 import org.meta_environment.rascal.ast.Expression.Equivalence;
 import org.meta_environment.rascal.ast.Expression.Exists;
 import org.meta_environment.rascal.ast.Expression.FieldUpdate;
 import org.meta_environment.rascal.ast.Expression.FileLocation;
 import org.meta_environment.rascal.ast.Expression.ForAll;
 import org.meta_environment.rascal.ast.Expression.FunctionAsValue;
 import org.meta_environment.rascal.ast.Expression.GreaterThan;
 import org.meta_environment.rascal.ast.Expression.GreaterThanOrEq;
 import org.meta_environment.rascal.ast.Expression.IfDefined;
 import org.meta_environment.rascal.ast.Expression.Implication;
 import org.meta_environment.rascal.ast.Expression.In;
 import org.meta_environment.rascal.ast.Expression.Intersection;
 import org.meta_environment.rascal.ast.Expression.LessThan;
 import org.meta_environment.rascal.ast.Expression.LessThanOrEq;
 import org.meta_environment.rascal.ast.Expression.Lexical;
 import org.meta_environment.rascal.ast.Expression.List;
 import org.meta_environment.rascal.ast.Expression.Literal;
 import org.meta_environment.rascal.ast.Expression.Match;
 import org.meta_environment.rascal.ast.Expression.Modulo;
 import org.meta_environment.rascal.ast.Expression.Negation;
 import org.meta_environment.rascal.ast.Expression.Negative;
 import org.meta_environment.rascal.ast.Expression.NoMatch;
 import org.meta_environment.rascal.ast.Expression.NonEmptyBlock;
 import org.meta_environment.rascal.ast.Expression.NotIn;
 import org.meta_environment.rascal.ast.Expression.OperatorAsValue;
 import org.meta_environment.rascal.ast.Expression.Or;
 import org.meta_environment.rascal.ast.Expression.Product;
 import org.meta_environment.rascal.ast.Expression.Range;
 import org.meta_environment.rascal.ast.Expression.Set;
 import org.meta_environment.rascal.ast.Expression.StepRange;
 import org.meta_environment.rascal.ast.Expression.Subscript;
 import org.meta_environment.rascal.ast.Expression.Subtraction;
 import org.meta_environment.rascal.ast.Expression.TransitiveClosure;
 import org.meta_environment.rascal.ast.Expression.TransitiveReflexiveClosure;
 import org.meta_environment.rascal.ast.Expression.Tuple;
 import org.meta_environment.rascal.ast.Expression.TypedVariable;
 import org.meta_environment.rascal.ast.Expression.Visit;
 import org.meta_environment.rascal.ast.Expression.VoidClosure;
 import org.meta_environment.rascal.ast.Header.Parameters;
 import org.meta_environment.rascal.ast.IntegerLiteral.DecimalIntegerLiteral;
 import org.meta_environment.rascal.ast.Literal.Boolean;
 import org.meta_environment.rascal.ast.Literal.Double;
 import org.meta_environment.rascal.ast.Literal.Integer;
 import org.meta_environment.rascal.ast.LocalVariableDeclaration.Default;
 import org.meta_environment.rascal.ast.Rule.Arbitrary;
 import org.meta_environment.rascal.ast.Rule.Guarded;
 import org.meta_environment.rascal.ast.Rule.Replacing;
 import org.meta_environment.rascal.ast.Statement.All;
 import org.meta_environment.rascal.ast.Statement.Assert;
 import org.meta_environment.rascal.ast.Statement.Assignment;
 import org.meta_environment.rascal.ast.Statement.Block;
 import org.meta_environment.rascal.ast.Statement.Break;
 import org.meta_environment.rascal.ast.Statement.Continue;
 import org.meta_environment.rascal.ast.Statement.DoWhile;
 import org.meta_environment.rascal.ast.Statement.Fail;
 import org.meta_environment.rascal.ast.Statement.First;
 import org.meta_environment.rascal.ast.Statement.For;
 import org.meta_environment.rascal.ast.Statement.GlobalDirective;
 import org.meta_environment.rascal.ast.Statement.IfThen;
 import org.meta_environment.rascal.ast.Statement.IfThenElse;
 import org.meta_environment.rascal.ast.Statement.Insert;
 import org.meta_environment.rascal.ast.Statement.Solve;
 import org.meta_environment.rascal.ast.Statement.Switch;
 import org.meta_environment.rascal.ast.Statement.Throw;
 import org.meta_environment.rascal.ast.Statement.Try;
 import org.meta_environment.rascal.ast.Statement.TryFinally;
 import org.meta_environment.rascal.ast.Statement.VariableDeclaration;
 import org.meta_environment.rascal.ast.Statement.While;
 import org.meta_environment.rascal.ast.Toplevel.DefaultVisibility;
 import org.meta_environment.rascal.ast.Toplevel.GivenVisibility;
 import org.meta_environment.rascal.ast.Visit.DefaultStrategy;
 import org.meta_environment.rascal.ast.Visit.GivenStrategy;
 import org.meta_environment.rascal.parser.ASTBuilder;
 import org.meta_environment.rascal.parser.Parser;
 import org.meta_environment.uptr.Factory;
 
 public class Evaluator extends NullASTVisitor<EvalResult> {
 	public static final String RASCAL_FILE_EXT = ".rsc";
 	final IValueFactory vf;
 	final TypeFactory tf = TypeFactory.getInstance();
 	final TypeEvaluator te = TypeEvaluator.getInstance();
 	private final RegExpPatternEvaluator re = new RegExpPatternEvaluator();
 	private final TreePatternEvaluator pe;
 	GlobalEnvironment env = GlobalEnvironment.getInstance();
 	
 	private final ASTFactory af;
 	private final JavaFunctionCaller javaFunctionCaller;
 
 	public Evaluator(IValueFactory f, ASTFactory astFactory, Writer errorWriter) {
 		this.vf = f;
 		this.af = astFactory;
 		javaFunctionCaller = new JavaFunctionCaller(errorWriter, te);
 		this.pe = new TreePatternEvaluator(this);
 	}
 	
 	/**
 	 * Clean the global environment for the benefit of repeated testing.
 	 */
 	public void clean(){
 		GlobalEnvironment.clean();
 	}
 	
 	/**
 	 * Evaluate a statement
 	 * @param stat
 	 * @return
 	 */
 	public IValue eval(Statement stat) {
 		EvalResult r = stat.accept(this);
         if(r != null){
         	return r.value;
         } else {
         	throw new RascalTypeError("Not yet implemented: " + stat.getTree());
         }
 	}
 	
 	/**
 	 * Evaluate a declaration
 	 * @param declaration
 	 * @return
 	 */
 	public IValue eval(Declaration declaration) {
 		EvalResult r = declaration.accept(this);
         if(r != null){
         	return r.value;
         } else {
         	throw new RascalBug("Not yet implemented: " + declaration.getTree());
         }
 	}
 	
 	/**
 	 * Evaluate an import
 	 * @param imp
 	 * @return
 	 */
 	public IValue eval(org.meta_environment.rascal.ast.Import imp) {
 		EvalResult r = imp.accept(this);
         if(r != null){
         	return r.value;
         } else {
         	throw new RascalBug("Not yet implemented: " + imp.getTree());
         }
 	}
 
 	/* First a number of general utility methods */
 	EvalResult result(Type t, IValue v) {
 		Map<ParameterType, Type> bindings = env.getTypes();
 		Type instance;
 		
 		if (bindings.size() > 0) {
 		    instance = t.instantiate(bindings);
 		}
 		else {
 			instance = t;
 		}
 		
 		if (v != null) {
 			checkType(v.getType(), instance);
 		}
 
 		return new EvalResult(instance, v);
 	}
 
 	EvalResult result(IValue v) {
 		Type type = v.getType();
 		
 		if (type.isRelationType() 
 				|| type.isSetType() 
 				|| type.isMapType()
 				|| type.isListType()) {
 			throw new RascalBug("Should not used run-time type for type checking!!!!");
 		}
 				
 		return new EvalResult(v != null ? type : null, v);
 	}
 	
 	private EvalResult result() {
 		return new EvalResult(null, null);
 	}
 	
 	private EvalResult notImplemented(String s){
 		throw new RascalBug(s + " not yet implemented");
 	}
 	
 	private void checkInteger(EvalResult val) {
 		checkType(val, tf.integerType());
 	}
 	
 	private void checkDouble(EvalResult val) {
 		checkType(val, tf.doubleType());
 	}
 	
 	private void checkString(EvalResult val) {
 		checkType(val, tf.stringType());
 	}
 	
 	private void checkType(EvalResult val, Type expected) {
 		checkType(val.type, expected);
 	}
 	
 	private void checkType(Type given, Type expected) {
 		if (!given.isSubtypeOf(expected)) {
 			throw new RascalTypeError("Expected " + expected + ", got " + given);
 		}
 	}
 	
 	private int intValue(EvalResult val) {
 		checkInteger(val);
 		return ((IInteger) val.value).getValue();
 	}
 	
 	private double doubleValue(EvalResult val) {
 		checkDouble(val);
 		return ((IDouble) val.value).getValue();
 	}
 	
 	private String stringValue(EvalResult val) {
 		checkString(val);
 		return ((IString) val.value).getValue();
 	}
 	
 	private void bindTypeParameters(TupleType actualTypes, TupleType formals) {
 		try {
 			Map<ParameterType, Type> bindings = new HashMap<ParameterType, Type>();
 			formals.match(actualTypes, bindings);
 			env.storeTypes(bindings);
 		}
 		catch (FactTypeError e) {
 			throw new RascalTypeError("Could not bind type parameters in " + formals + " to " + actualTypes, e);
 		}
 	}
 
 	// Ambiguity ...................................................
 	
 	@Override
 	public EvalResult visitExpressionAmbiguity(Ambiguity x) {
 		throw new RascalBug("Ambiguous expression: " + x);
 	}
 	
 	@Override
 	public EvalResult visitStatementAmbiguity(
 			org.meta_environment.rascal.ast.Statement.Ambiguity x) {
 		throw new RascalBug("Ambiguous statement: " + x);
 	}
 	
 	// Modules -------------------------------------------------------------
 	
 	@Override
 	public EvalResult visitImportDefault(
 			org.meta_environment.rascal.ast.Import.Default x) {
 		// TODO support for full complexity of import declarations
 		String name = x.getModule().getName().toString();
 		env.addImport(name);
 		Parser p = Parser.getInstance();
 		ASTBuilder b = new ASTBuilder(af);
 		
 		try {
 			String fileName = name.replaceAll("::","/") + RASCAL_FILE_EXT;
 			File file = new File(fileName);
 			
 			if (!file.exists()) {
 				fileName = "src/StandardLibrary/" + fileName;
 				file = new File(fileName);
 				if (!file.exists()) {
 					throw new RascalTypeError("Can not find file for module " + name);
 				}
 			}
 			
 			INode tree = p.parse(new FileInputStream(file));
 			
 			if (tree.getTreeNodeType() == Factory.ParseTree_Summary) {
 				throw new RascalTypeError("Parse error in module " + name + ":\n" + tree);
 			}
 			
 			Module m = b.buildModule(tree);
 		
 			// TODO reactivate checking module name
 //			ModuleName declaredNamed = m.getHeader().getName();
 //			if (!declaredNamed.toString().equals(name)) {
 //				throw new RascalTypeError("Module " + declaredNamed + " should be in a file called " + declaredNamed + RASCAL_FILE_EXT + ", not " + name + RASCAL_FILE_EXT);
 //			}
 			return m.accept(this);
 		} catch (FactTypeError e) {
 			throw new RascalTypeError("Something went wrong during parsing of " + name + ": ", e);
 		} catch (FileNotFoundException e) {
 			throw new RascalTypeError("Could not import module", e);
 		} catch (IOException e) {
 			throw new RascalTypeError("Could not import module", e);
 		}
 	}
 	
 	@Override 
 	public EvalResult visitModuleDefault(
 			org.meta_environment.rascal.ast.Module.Default x) {
 		String name = x.getHeader().getName().toString();
 
 		env.addModule(name);
 		env.pushModule(name);
 
 		x.getHeader().accept(this);
 
 		java.util.List<Toplevel> decls = x.getBody().getToplevels();
 		for (Toplevel l : decls) {
 			l.accept(this);
 		}
 
 		env.popModule();
 		return result();
 	}
 	
 	@Override
 	public EvalResult visitHeaderDefault(
 			org.meta_environment.rascal.ast.Header.Default x) {
 		visitImports(x.getImports());
 		return result();
 	}
 	
 	private void visitImports(java.util.List<Import> imports) {
 		for (Import i : imports) {
 			i.accept(this);
 		}
 	}
 	
 	@Override
 	public EvalResult visitHeaderParameters(Parameters x) {
 		visitImports(x.getImports());
 		return result();
 	}
 	
 	@Override
 	public EvalResult visitToplevelDefaultVisibility(DefaultVisibility x) {
 		return x.getDeclaration().accept(this);
 	}
 
 	@Override
 	public EvalResult visitToplevelGivenVisibility(GivenVisibility x) {
 		// order dependent code here:
 		x.getDeclaration().accept(this);
 		// TODO implement visibility stuff
 		return result();
 	}
 	
 	@Override
 	public EvalResult visitDeclarationFunction(Function x) {
 		return x.getFunctionDeclaration().accept(this);
 	}
 	
 	@Override
 	public EvalResult visitDeclarationVariable(Variable x) {
 		Type declaredType = x.getType().accept(te);
 		EvalResult r = result();
 
 		for (org.meta_environment.rascal.ast.Variable var : x.getVariables()) {
 			if (var.isUnInitialized()) {  
 				throw new RascalTypeError("Module variable is not initialized: " + x);
 			} else {
 				EvalResult v = var.getInitial().accept(this);
 				if(v.type.isSubtypeOf(declaredType)){
 					r = result(declaredType, v.value);
 					env.storeVariable(var.getName(), r);
 				} else {
 					throw new RascalTypeError("variable " + var + ", declared type " + declaredType + " incompatible with initial type " + v.type);
 				}
 			}
 		}
 		
 		return r;
 	}
 	
 	@Override
 	public EvalResult visitDeclarationAnnotation(Annotation x) {
 		Type annoType = x.getType().accept(te);
 		String name = x.getName().toString();
 		
 		for (org.meta_environment.rascal.ast.Type type : x.getTypes()) {
 		  tf.declareAnnotation(type.accept(te), name, annoType);	
 		}
 		
 		return result();
 	}
 	
 	@Override
 	public EvalResult visitDeclarationData(Data x) {
 		String name = x.getUser().getName().toString();
 		NamedTreeType sort = tf.namedTreeType(name);
 		env.storeType(sort);
 		
 		for (Variant var : x.getVariants()) {
 			String altName = var.getName().toString();
 			
 			if (altName.startsWith("\\")) {
 				altName = altName.substring(1);
 			}
 			
 		    if (var.isNAryConstructor()) {
 		    	java.util.List<TypeArg> args = var.getArguments();
 		    	Type[] fields = new Type[args.size()];
 		    	String[] labels = new String[args.size()];
 
 		    	for (int i = 0; i < args.size(); i++) {
 		    		TypeArg arg = args.get(i);
 					fields[i] = arg.getType().accept(te);
 		    		labels[i] = arg.getName().toString();
 		    	}
 
 		    	TupleType children = tf.tupleType(fields, labels);
 		    	env.storeType(tf.treeNodeType(sort, altName, children));
 		    }
 		    else if (var.isNillaryConstructor()) {
 		    	env.storeType(tf.treeNodeType(sort, altName, new Object[] { }));
 		    }
 		    else if (var.isAnonymousConstructor()) {
 		    	Type argType = var.getType().accept(te);
 		    	String label = var.getName().toString();
 		    	env.storeType(tf.anonymousTreeType(sort, altName, argType, label));
 		    }
 		}
 		
 		return result();
 	}
 	
 	@Override
 	public EvalResult visitDeclarationType(
 			org.meta_environment.rascal.ast.Declaration.Type x) {
 		// TODO add support for parameterized types
 		String user = x.getUser().getName().toString();
 		Type base = x.getBase().accept(te);
 		tf.namedType(user, base);
 		return result();
 	}
 	
 	@Override
 	public EvalResult visitDeclarationView(View x) {
 		// TODO implement
 		throw new RascalTypeError("views are not yet implemented");
 	}
 	
 	@Override
 	public EvalResult visitDeclarationRule(Rule x) {
 		return x.getRule().accept(this);
 	}
 	
 	@Override
 	public EvalResult visitRuleArbitrary(Arbitrary x) {
 		env.storeRule(x.getPattern().accept(this).type, x);
 		return result();
 	}
 	
 	@Override
 	public EvalResult visitRuleReplacing(Replacing x) {
 		env.storeRule(x.getPattern().accept(this).type, x);
 		return result();
 	}
 	
 	@Override
 	public EvalResult visitRuleGuarded(Guarded x) {
 		EvalResult result = x.getRule().getPattern().getPattern().accept(this);
 		if (!result.type.isSubtypeOf(x.getType().accept(te))) {
 			throw new RascalTypeError("Declared type of rule does not match type of left-hand side: " + x);
 		}
 		return x.getRule().accept(this);
 	}
 	
 	@Override
 	public EvalResult visitDeclarationTag(Tag x) {
 		throw new RascalTypeError("tags are not yet implemented");
 	}
 	
 	// Variable Declarations -----------------------------------------------
 
 	@Override
 	public EvalResult visitLocalVariableDeclarationDefault(Default x) {
 		// TODO deal with dynamic variables
 		return x.getDeclarator().accept(this);
 	}
 
 	@Override
 	public EvalResult visitDeclaratorDefault(
 			org.meta_environment.rascal.ast.Declarator.Default x) {
 		Type declaredType = x.getType().accept(te);
 		EvalResult r = result();
 
 		for (org.meta_environment.rascal.ast.Variable var : x.getVariables()) {
 			if (var.isUnInitialized()) {  // variable declaration without initialization
 				r = result(declaredType, null);
 				env.storeVariable(var.getName(), r);
 			} else {                     // variable declaration with initialization
 				EvalResult v = var.getInitial().accept(this);
 				if(v.type.isSubtypeOf(declaredType)){
 					r = result(declaredType, v.value);
 					env.storeVariable(var.getName(), r);
 				} else {
 					throw new RascalTypeError("variable " + var.getName() + ": declared type " + declaredType + " incompatible with initialization type " + v.type);
 				}
 			}
 		}
 		return r;
 	}
 	
 	// Function calls and tree constructors
 	
 	@Override
 	public EvalResult visitExpressionClosureCall(ClosureCall x) {
 		EvalResult func = x.getClosure().getExpression().accept(this);
 		java.util.List<org.meta_environment.rascal.ast.Expression> args = x.getArguments();
 			
 		 IValue[] actuals = new IValue[args.size()];
 		 Type[] types = new Type[args.size()];
 
 		 for (int i = 0; i < args.size(); i++) {
 			 EvalResult resultElem = args.get(i).accept(this);
 			 types[i] = resultElem.type;
 			 actuals[i] = resultElem.value;
 		 }
 		 
 		 TupleType actualTypes = tf.tupleType(types);
 		
 		if (te.isFunctionType(func.type)) {
 			String name = ((IString) func.value).getValue();
 			FunctionDeclaration decl = env.getFunction(name, actualTypes);
 			if (decl == null) {
 				throw new RascalTypeError("Call to undefined function: " + name);
 			}
 			return call(decl, actuals, actualTypes);
 		}
 		else {
 			throw new RascalBug("Closures are not implemented yet");
 		}
 	}
 	
 	@Override
 	public EvalResult visitExpressionCallOrTree(CallOrTree x) {
 		 java.util.List<org.meta_environment.rascal.ast.Expression> args = x.getArguments();
 		 QualifiedName name = x.getQualifiedName();
 		 
 		 IValue[] actuals = new IValue[args.size()];
 		 Type[] types = new Type[args.size()];
 
 		 for (int i = 0; i < args.size(); i++) {
 			 EvalResult resultElem = args.get(i).accept(this);
 			 types[i] = resultElem.type;
 			 actuals[i] = resultElem.value;
 		 }
 		 
 		 TupleType signature = tf.tupleType(types);
 		 
 		 if (isTreeConstructorName(name)) {
 			 return constructTree(name, actuals, signature);
 		 }
 		 else {
 			 return call(name, actuals, signature);
 		 }
 	}
 	private EvalResult call(QualifiedName name, IValue[] actuals, TupleType actualTypes) {
 		 FunctionDeclaration func = env.getFunction(name, actualTypes);
 
 		 if (func != null) {
 			 env.pushModuleForFunction(name, actualTypes);
 			 EvalResult res = call(func, actuals, actualTypes);
 			 env.popModule();
 			 return res;
 		 }
 		 else {
 			 return constructTree(name, actuals, actualTypes);
 		 }
 	}
 
 	private boolean isTreeConstructorName(QualifiedName name) {
 		java.util.List<Name> names = name.getNames();
 		
 		if (names.size() > 1) {
 			String sort = Names.sortName(name);
 			NamedTreeType sortType = tf.lookupNamedTreeType(sort);
 			
 			if (sortType != null) {
 				String cons = Names.consName(name);
 				
 				if (tf.lookupTreeNodeType(sortType, cons).size() > 0) {
 					return true;
 				}
 			}
 			else {
 				if (env.getModule(sort) == null) {
 					throw new RascalTypeError("Qualified name is neither module name nor data type name");
 				}
 				else {
 				  return false; // it's a module name
 				}
 			}
 		}
 		
 		String cons = Names.consName(name);
 			
 		if (tf.lookupTreeNodeType(cons).size() > 0) {
 			return true;
 		}
 
 		return false;
 	}
 	
 	/**
 	 * A top-down algorithm is needed to type check a constructor call since the
 	 * result type of constructors can be overloaded. We bring down the expected type
 	 * of each argument.
 	 * @param expected
 	 * @param functionName
 	 * @param args
 	 * @return
 	 * 
 	 * TODO: code does not deal with import structure, rather data def's are global.
 	 */
 	private EvalResult constructTree(QualifiedName functionName, IValue[] actuals, TupleType signature) {
 		java.util.List<Name> parts = functionName.getNames();
 		String sort;
 		String cons;
 		
 		cons = parts.get(parts.size() - 1).toString();
 		sort = parts.size() > 1 ? parts.get(parts.size() - 2).toString() : null;
 
 		if (cons.startsWith("\\")) {
 			cons = cons.substring(1);
 		}
 		
 		java.util.List<TreeNodeType> candidates;
 	
 		if (sort != null) {
 			NamedTreeType sortType = tf.lookupNamedTreeType(sort);
 			
 			if (sortType != null) {
 			  candidates = tf.lookupTreeNodeType(sortType, cons);
 			}
 			else {
 			  return result(tf.treeType(), vf.tree(cons, actuals));
 			}
 		}
 		else {
 		    candidates = tf.lookupTreeNodeType(cons);
 		}
 		
 		if (candidates.size() != 0) {
 			java.util.List<TreeNodeType> matches = new LinkedList<TreeNodeType>();
 
 			for (TreeNodeType candidate : candidates) {
 				if (signature.isSubtypeOf(candidate.getChildrenTypes())) {
 					matches.add(candidate);
 				}
 			}
 
 			if (matches.size() > 1) {
 				StringBuffer buf = new StringBuffer();
 				for (TreeNodeType m : matches) {
 					buf.append(m.getSuperType() + "::" + cons);
 				}
 
 				throw new RascalTypeError(cons + " is ambiguous, could be one of " + buf);
 			}
 			else if (matches.size() == 1) {
 				return result(matches.get(0).make(vf, actuals));
 			}
 		}
 		
 		return result(tf.treeType(), vf.tree(cons, actuals));
 	}
 	
 	@Override
 	public EvalResult visitExpressionFunctionAsValue(FunctionAsValue x) {
 		Type functionType = te.getFunctionType();
 		return result(functionType, functionType.make(vf, x.getFunction().getName().toString()));
 	}
 	
 	private EvalResult call(FunctionDeclaration func, IValue[] actuals, TupleType actualTypes) {
 		if (isJavaFunction(func)) { 
 			return callJavaFunction(func, actuals, actualTypes);
 		}
 		else {
 			return callRascalFunction(func, actuals, actualTypes);
 		}
 	}
 
 	private boolean isJavaFunction(FunctionDeclaration func) {
 		java.util.List<FunctionModifier> mods = func.getSignature().getModifiers().getModifiers();
 		for (FunctionModifier m : mods) {
 			if (m.isJava()) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 
 	private EvalResult callJavaFunction(FunctionDeclaration func,
 			IValue[] actuals, TupleType actualTypes) {
 		Type type = func.getSignature().getType().accept(te);
 		TupleType formals = (TupleType) func.getSignature().getParameters().accept(te);
 		env.pushFrame();
 		bindTypeParameters(actualTypes, formals);
 		IValue result = javaFunctionCaller.callJavaMethod(func, actuals);
 		Type resultType = type.instantiate(env.getTypes());
 		env.popFrame();
 		return result(resultType, result);
 	}
 
 	private EvalResult callRascalFunction(FunctionDeclaration func,
 			IValue[] actuals, TupleType actualTypes) {
 		try {
 			env.pushFrame();
 			TupleType formals = (TupleType) func.getSignature().accept(te);
 			bindTypeParameters(actualTypes, formals);
 			
 			for (int i = 0; i < formals.getArity(); i++) {
 				Type formal = formals.getFieldType(i);
 				EvalResult result = result(formal, actuals[i]);
 				env.storeVariable(formals.getFieldName(i), result);
 			}
 			
 			if (func.getBody().isDefault()) {
 			  func.getBody().accept(this);
 			}
 			else {
 				throw new RascalTypeError("Java method body without a java function modifier in:\n" + func);
 			}
 			
 			throw new RascalTypeError("Function definition:" + func + "\n does not have a return statement.");
 		}
 		catch (ReturnException e) {
 			EvalResult result = e.getValue();
 			result.type = result.type.instantiate(env.getTypes());
 			env.popFrame();
 			return result;
 		}
 	}
 
 
 
 	@Override
 	public EvalResult visitFunctionBodyDefault(
 			org.meta_environment.rascal.ast.FunctionBody.Default x) {
 		EvalResult result = result();
 		
 		for (Statement statement : x.getStatements()) {
 			result = statement.accept(this);
 		}
 		
 		return result;
 	}
 	
    // Statements ---------------------------------------------------------
 	
 	@Override
 	public EvalResult visitStatementAssert(Assert x) {
 		String msg = x.getMessage().toString();
 		EvalResult r = x.getExpression().accept(this);
 		if(r.type.equals(tf.boolType())){
 			if(r.value.equals(vf.bool(false))){
 				System.err.println("Assertion failed: " + msg + "\n");
 			}
 		} else {
 			throw new RascalTypeError("expression in assertion should be bool instead of " + r.type);
 		}
 		return r;	
 	}
 	
 	@Override
 	public EvalResult visitStatementVariableDeclaration(VariableDeclaration x) {
 		return x.getDeclaration().accept(this);
 	}
 	
 	@Override
 	public EvalResult visitStatementExpression(Statement.Expression x) {
 		return x.getExpression().accept(this);
 	}
 	
 	@Override
 	public EvalResult visitStatementFunctionDeclaration(
 			org.meta_environment.rascal.ast.Statement.FunctionDeclaration x) {
 		return x.getFunctionDeclaration().accept(this);
 	}
 	
 	EvalResult assignVariable(QualifiedName name, EvalResult right){
 		System.err.println("assignVariable: " + name + ", " + right);
 		EvalResult previous = env.getVariable(name);
 		if (previous != null) {
 			if (right.type.isSubtypeOf(previous.type)) {
 				right.type = previous.type;
 			} else {
 				throw new RascalTypeError("Variable " + name
 						+ " has type " + previous.type
 						+ "; cannot assign value of type " + right.type);
 			}
 		}
 		env.storeVariable(name, right);
 		return right;
 	}
 	
 	@Override
 	public EvalResult visitExpressionSubscript(Subscript x) {
 		EvalResult subs = x.getSubscript().accept(this);
 		EvalResult expr = x.getExpression().accept(this);
 
 		Type exprBase = expr.type.getBaseType();
 		Type subsBase = subs.type.getBaseType();
 
 		if (exprBase.isListType() && subsBase.isIntegerType()) {
 			int index = intValue(subs);
 			Type elementType = ((ListType) exprBase).getElementType();
 			try {
 				IValue element = ((IList) expr.value).get(index);
 				return result(elementType, element);
 			} catch (IndexOutOfBoundsException e) {
 				throw new RascalTypeError("Subscript out of bounds", e);
 			}
 		} else if ((exprBase.isNamedTreeType() || exprBase.isTreeNodeType())
 				&& subsBase.isIntegerType()) {
 			int index = intValue(subs);
 			Type elementType = ((INode) expr.value).getTreeNodeType()
 					.getChildType(index);
 			IValue element = ((ITree) expr.value).get(index);
 			return result(elementType, element);
 		} else if (exprBase.isTreeType() && subsBase.isIntegerType()) {
 			int index = intValue(subs);
 			Type elementType = tf.valueType();
 			try {
 				IValue element = ((ITree) expr.value).get(index);
 				return result(elementType, element);
 			} catch (IndexOutOfBoundsException e) {
 				throw new RascalTypeError("Subscript out of bounds", e);
 			}
 		} else if (exprBase.isMapType()
 				&& subsBase.isSubtypeOf(((MapType) exprBase).getKeyType())) {
 			Type valueType = ((MapType) exprBase).getValueType();
 			return result(valueType, ((IMap) expr.value).get(subs.value));
 		} else if (exprBase.isTupleType() && subsBase.isIntegerType()) {
 			int index = intValue(subs);
 			Type elementType = ((TupleType) exprBase).getFieldType(index);
 			IValue element = ((ITuple) expr.value).get(index);
 			return result(elementType, element);
 		}
 
 		else if (exprBase.isRelationType()) {
 			Type elementType = ((RelationType) exprBase).getFieldType(0);
 
 			if (subsBase.isSubtypeOf(elementType)) {
 				int arity = ((RelationType) exprBase).getArity();
 
 				if (arity == 2) {
 					Type resultType = tf.setType(((RelationType) exprBase)
 							.getFieldType(1));
 					ISetWriter w = resultType.writer(vf);
 					for (IValue v : ((IRelation) expr.value)) {
 						ITuple tup = (ITuple) v;
 						if (tup.get(0).equals(subs.value)) {
 							w.insert(tup.get(1));
 						}
 					}
 					return result(resultType, w.done());
 
 				} else {
 					Type fieldTypes[] = new Type[arity - 1];
 					for (int i = 1; i < arity; i++) {
 						fieldTypes[i - 1] = ((RelationType) exprBase)
 								.getFieldType(i);
 					}
 					Type resultType = tf.relType(fieldTypes);
 
 					IRelationWriter w = resultType.writer(vf);
 
 					for (IValue v : ((IRelation) expr.value)) {
 						ITuple tup = (ITuple) v;
 						if (tup.get(0).equals(subs.value)) {
 							IValue args[] = new IValue[arity - 1];
 							for (int i = 1; i < arity; i++) {
 								args[i - 1] = tup.get(i);
 							}
 							ITuple res = vf.tuple(args);
 							w.insert(res);
 						}
 					}
 					return result(resultType, w.done());
 				}
 			} else {
 				throw new RascalTypeError("Type of subscript " + subsBase
 						+ "  incompatible with relation element type "
 						+ elementType);
 			}
 		}
 		throw new RascalBug("Not yet implemented subscript: " + x);
 	}
 
 	@Override
 	public EvalResult visitExpressionFieldAccess(
 			org.meta_environment.rascal.ast.Expression.FieldAccess x) {
 		EvalResult expr = x.getExpression().accept(this);
 		String field = x.getField().toString();
 		
 		if (expr.type.isTupleType()) {
 			TupleType tuple = (TupleType) expr.type;
 			if (!tuple.hasFieldNames()) {
 				throw new RascalTypeError("Tuple does not have field names: " + tuple);
 			}
 			
 			return result(tuple.getFieldType(field), ((ITuple) expr.value).get(field));
 		}
 		else if (expr.type.isRelationType()) {
 			TupleType tuple = ((RelationType) expr.type).getFieldTypes();
 			
 			try {
 				ISetWriter w = vf.setWriter(tuple.getFieldType(field));
 				for (IValue e : (ISet) expr.value) {
 					w.insert(((ITuple) e).get(field));
 				}
 				return result(tf.setType(tuple.getFieldType(field)), w.done());
 			}
 			catch (FactTypeError e) {
 				throw new RascalTypeError(e.getMessage(), e);
 			}
 		}
 		else if (expr.type.isNamedTreeType() || expr.type.isTreeNodeType()) {
 			TreeNodeType node = ((INode) expr.value).getTreeNodeType();
 			
 			try {
 				return result(node.getChildType(node.getChildIndex(field)),((INode) expr.value).get(field));
 			}
 			catch (FactTypeError e) {
 				throw new RascalTypeError(e.getMessage(), e);
 			}
 		}
 		
 		throw new RascalTypeError("Field selection is not allowed on " + expr.type);
 	}
 	@Override
 	public EvalResult visitStatementFail(Fail x) {
 		if (x.getFail().isWithLabel()) {
 			throw FailureException.getInstance(x.getFail().getLabel().toString());
 		}
 		else {
 		  throw FailureException.getInstance();
 		}
 	}
 	
 	@Override
 	public EvalResult visitStatementReturn(
 			org.meta_environment.rascal.ast.Statement.Return x) {
 		org.meta_environment.rascal.ast.Return r = x.getRet();
 		
 		if (r.isWithExpression()) {
 		  throw ReturnException.getInstance(x.getRet().getExpression().accept(this));
 		}
 		else {
 			throw ReturnException.getInstance(result(tf.voidType(), null));
 		}
 	}
 	
 	@Override
 	public EvalResult visitStatementAll(All x) {
 		throw new RascalBug("NYI all" + x); // TODO
 	}
 	
 	@Override
 	public EvalResult visitStatementBreak(Break x) {
 		throw new RascalBug("NYI break" + x); // TODO
 	}
 	
 	@Override
 	public EvalResult visitStatementContinue(Continue x) {
 		throw new RascalBug("NYI" + x); // TODO
 	}
 	
 	@Override
 	public EvalResult visitStatementFirst(First x) {
 		throw new RascalBug("NYI" + x); // TODO
 	}
 	
 	@Override
 	public EvalResult visitStatementGlobalDirective(GlobalDirective x) {
 		throw new RascalBug("NYI" + x); // TODO
 	}
 	
 	@Override
 	public EvalResult visitStatementThrow(Throw x) {
 		throw new RascalBug("NYI" + x); // TODO
 	}
 	
 	@Override
 	public EvalResult visitStatementTry(Try x) {
 		throw new RascalBug("NYI" + x); // TODO
 	}
 	
 	@Override
 	public EvalResult visitStatementTryFinally(TryFinally x) {
 		throw new RascalBug("NYI" + x); // TODO
 	}
 	
 	@Override
 	public EvalResult visitStatementVisit(
 			org.meta_environment.rascal.ast.Statement.Visit x) {
 		throw new RascalBug("NYI" + x); // TODO
 	}
 	
 	@Override
 	public EvalResult visitStatementInsert(Insert x) {
 		throw InsertException.getInstance(x.getExpression().accept(this));
 	}
 	
 	@Override
 	public EvalResult visitStatementAssignment(Assignment x) {
 		EvalResult right = x.getExpression().accept(this);
 		return x.getAssignable().accept(new AssignableEvaluator(env, right, this));
 	}
 	
 	@Override
 	public EvalResult visitStatementBlock(Block x) {
 		EvalResult r = result();
 		for(Statement stat : x.getStatements()){
 			r = stat.accept(this);
 		}
 		return r;
 	}
   
 	@Override
 	public EvalResult visitAssignableVariable(
 			org.meta_environment.rascal.ast.Assignable.Variable x) {
 		return env.getVariable(x.getQualifiedName().toString());
 	}
 	
 	@Override
 	public EvalResult visitAssignableFieldAccess(FieldAccess x) {
 		EvalResult receiver = x.getReceiver().accept(this);
 		String label = x.getField().toString();
 	
 		if (receiver.type.isTupleType()) {
 			IValue result = ((ITuple) receiver.value).get(label);
 			Type type = ((TupleType) ((ITuple) receiver.value).getType()).getFieldType(label);
 			return result(type, result);
 		}
 		else if (receiver.type.isTreeNodeType() || receiver.type.isNamedTreeType()) {
 			IValue result = ((INode) receiver.value).get(label);
 			TreeNodeType treeNodeType = ((INode) receiver.value).getTreeNodeType();
 			Type type = treeNodeType.getChildType(treeNodeType.getChildIndex(label));
 			return result(type, result);
 		}
 		else {
 			throw new RascalTypeError(x.getReceiver() + " has no field named " + label);
 		}
 	}
 	
 	@Override
 	public EvalResult visitAssignableAnnotation(
 			org.meta_environment.rascal.ast.Assignable.Annotation x) {
 		EvalResult receiver = x.getReceiver().accept(this);
 		String label = x.getAnnotation().toString();
 		Type type = tf.getAnnotationType(receiver.type, label);
 		IValue value = receiver.value.getAnnotation(label);
 		return result(type, value);
 	}
 	
 	@Override
 	public EvalResult visitAssignableConstructor(Constructor x) {
 		throw new RascalBug("constructor assignable does not represent a value:" + x);
 	}
 	
 	@Override
 	public EvalResult visitAssignableIfDefined(
 			org.meta_environment.rascal.ast.Assignable.IfDefined x) {
 		throw new RascalBug("ifdefined assignable does not represent a value");
 	}
 	
 	@Override
 	public EvalResult visitAssignableSubscript(
 			org.meta_environment.rascal.ast.Assignable.Subscript x) {
 		EvalResult receiver = x.getReceiver().accept(this);
 		EvalResult subscript = x.getSubscript().accept(this);
 		
 		if (receiver.type.getBaseType().isListType() && subscript.type.getBaseType().isIntegerType()) {
 			IList list = (IList) receiver.value;
 			IValue result = list.get(intValue(subscript));
 			Type type = ((ListType) receiver.type).getElementType();
 			return result(type, result);
 		}
 		else if (receiver.type.getBaseType().isMapType()) {
 			Type keyType = ((MapType) receiver.type).getKeyType();
 			
 			if (subscript.type.isSubtypeOf(keyType)) {
 				IValue result = ((IMap) receiver.value).get(subscript.value);
 				Type type = ((MapType) receiver.type).getValueType();
 				return result(type, result);
 			}
 		}
 		
 		// TODO implement other subscripts
 
 		throw new RascalTypeError("Illegal subscript " + x.getSubscript() + " for receiver " + x.getReceiver());
 	}
 	
 	@Override
 	public EvalResult visitAssignableTuple(
 			org.meta_environment.rascal.ast.Assignable.Tuple x) {
 		throw new RascalBug("tuple in assignable does not represent a value:" + x);
 	}
 	
 	@Override
 	public EvalResult visitAssignableAmbiguity(
 			org.meta_environment.rascal.ast.Assignable.Ambiguity x) {
 		throw new RascalBug("ambiguous Assignable: " + x);
 	}
 	
 	@Override
 	public EvalResult visitFunctionDeclarationDefault(
 			org.meta_environment.rascal.ast.FunctionDeclaration.Default x) {
 		Signature sig = x.getSignature();
 		String name = sig.getName().toString();
 		
 		if (isJavaFunction(x)) {
 			javaFunctionCaller.compileJavaMethod(x);
 		}
 		else if (!x.getBody().isDefault()) {
 			throw new RascalTypeError("Java function body without java function modifier in: " + x);
 		}
 		
 		env.storeFunction(name,(org.meta_environment.rascal.ast.FunctionDeclaration)x);
 		return result();
 	}
 	
 	@Override
 	public EvalResult visitStatementIfThenElse(IfThenElse x) {
 		for (org.meta_environment.rascal.ast.Expression expr : x
 				.getConditions()) {
 			EvalResult cval = expr.accept(this);
 			if (cval.type.isBoolType()) {
 				if (cval.value.equals(vf.bool(false))) {
 					return x.getElseStatement().accept(this);
 				}
 			} else {
 				throw new RascalTypeError("Condition " + expr + " has type "
 						+ cval.type + " but should be bool");
 			}
 		}
 		return x.getThenStatement().accept(this);
 	}
 
 	@Override
 	public EvalResult visitStatementIfThen(IfThen x) {
 		for (org.meta_environment.rascal.ast.Expression expr : x
 				.getConditions()) {
 			EvalResult cval = expr.accept(this);
 			if (cval.type.isBoolType()) {
 				if (cval.value.equals(vf.bool(false))) {
 					return result();
 				}
 			} else {
 				throw new RascalTypeError("Condition " + expr + " has type "
 						+ cval.type + " but should be bool");
 			}
 		}
 		return x.getThenStatement().accept(this);
 	}
 
 	@Override
 	public EvalResult visitStatementWhile(While x) {
 		org.meta_environment.rascal.ast.Expression expr = x.getCondition();
 		EvalResult statVal = result();
 		do {
 			EvalResult cval = expr.accept(this);
 			if (cval.type.isBoolType()) {
 				if (cval.value.equals(vf.bool(false))) {
 					return statVal;
 				} else {
 					statVal = x.getBody().accept(this);
 				}
 			} else {
 				throw new RascalTypeError("Condition " + expr + " has type "
 						+ cval.type + " but should be bool");
 			}
 		} while (true);
 	}
 	
 	@Override
 	public EvalResult visitStatementDoWhile(DoWhile x) {
 		org.meta_environment.rascal.ast.Expression expr = x.getCondition();
 		do {
 			EvalResult result = x.getBody().accept(this);
 			EvalResult cval = expr.accept(this);
 			if (cval.type.isBoolType()) {
 				if (cval.value.equals(vf.bool(false))) {
 					return result;
 				}
 			} else {
 				throw new RascalTypeError("Condition " + expr + " has type "
 						+ cval.type + " but should be bool");
 			}
 		} while (true);
 	}
 	
 	// TODO: in progress ...
 	
 	@Override
 	public EvalResult visitStatementSwitch(Switch x) {
 		EvalResult subject = x.getExpression().accept(this);
 
 		for(Case cs : x.getCases()){
 			if(cs.isDefault()){
 				return cs.getStatement().accept(this);
 			}
 			org.meta_environment.rascal.ast.Rule rl = cs.getRule();
 			if(rl.isArbitrary()){
 				org.meta_environment.rascal.ast.Expression pat = rl.getPattern();
 				if(match(subject.value, pat)){
 					return rl.getStatement().accept(this);
 				}
 			} else if(rl.isGuarded())	{
 				org.meta_environment.rascal.ast.Type tp = rl.getType();
 				Type t = tp.accept(te);
 				rl = rl.getRule();
 				org.meta_environment.rascal.ast.Expression pat = rl.getPattern();
 				if(subject.type.isSubtypeOf(t) && match(subject.value, pat)){
 					return rl.getStatement().accept(this);
 				}
 			} else if(rl.isReplacing()){
 				throw new RascalBug("Replacing Rule not yet implemented: " + rl);
 			}
 		}
 		return null;
 	}
 	
     @Override
     public EvalResult visitExpressionMatch(Match x) {
     	org.meta_environment.rascal.ast.Expression pat = x.getPattern();
     	EvalResult subj = x.getExpression().accept(this);
     	return result(vf.bool(match(subj.value, pat)));
     }
     
     @Override
     public EvalResult visitExpressionNoMatch(NoMatch x) {
     	org.meta_environment.rascal.ast.Expression pat = x.getPattern();
     	EvalResult subj = x.getExpression().accept(this);
     	return result(vf.bool(!match(subj.value, pat)));
     }
 	
 	// ----- General method for matching --------------------------------------------------
     
     private PatternValue evalPattern(org.meta_environment.rascal.ast.Expression pat){
     	if(pe.isPattern(pat)){
     		//System.err.println("tree pattern: " + pat);
     		return pat.accept(pe);
     	} else if(re.isRegExpPattern(pat)){ 
     		//System.err.println("regexp: " + pat);
 			return pat.accept(re);
 		} else {
 			throw new RascalTypeError("pattern expected instead of " + pat);
 		}
     }
 	
 	private boolean match(IValue subj, org.meta_environment.rascal.ast.Expression pat){
 		System.err.println("match: pat : " + pat);
 		
 		return evalPattern(pat).match(subj, this);
 	}
 
 	// Expressions -----------------------------------------------------------
 
 	@Override
 	public EvalResult visitExpressionLiteral(Literal x) {
 		return x.getLiteral().accept(this);
 	}
 
 	@Override
 	public EvalResult visitLiteralInteger(Integer x) {
 		return x.getIntegerLiteral().accept(this);
 	}
 
 	@Override
 	public EvalResult visitLiteralDouble(Double x) {
 		String str = x.getDoubleLiteral().toString();
 		return result(vf.dubble(java.lang.Double.parseDouble(str)));
 	}
 
 	@Override
 	public EvalResult visitLiteralBoolean(Boolean x) {
 		String str = x.getBooleanLiteral().toString();
 		return result(vf.bool(str.equals("true")));
 	}
 
 	@Override
 	public EvalResult visitLiteralString(
 			org.meta_environment.rascal.ast.Literal.String x) {
 		String str = x.getStringLiteral().toString();
 		return result(vf.string(deescape(str)));
 	}
 
 	private String deescape(String str) {
 		byte[] bytes = str.getBytes();
 		StringBuffer result = new StringBuffer();
 		
 		for (int i = 1; i < bytes.length - 1; i++) {
 			char b = (char) bytes[i];
 			switch (b) {
 			case '\\':
 				switch (bytes[++i]) {
 				case '\\':
 					b = '\\'; 
 					break;
 				case 'n':
 					b = '\n'; 
 					break;
 				case '"':
 					b = '"'; 
 					break;
 				case 't':
 					b = '\t'; 
 					break;
 				case 'b':
 					b = '\b'; 
 					break;
 				case 'f':
 					b = '\f'; 
 					break;
 				case 'r':
 					b = '\r'; 
 					break;
 				case '<':
 					b = '<'; 
 					break;
 				case '0':
 				case '1':
 				case '2':
 				case '3':
 				case '4':
 				case '5':
 				case '6':
 				case '7':
 					b = (char) (bytes[i] - '0');
 					if (i < bytes.length - 1 && Character.isDigit(bytes[i+1])) {
 						b = (char) (b * 8 + (bytes[++i] - '0'));
 						
 						if (i < bytes.length - 1 && Character.isDigit(bytes[i+1])) {
 							b = (char) (b * 8 + (bytes[++i] - '0'));
 						}
 					}
 					break;
 				case 'u':
 					// TODO unicode escape
 					break;
 				default:
 				    b = '\\';	
 				}
 			}
 			
 			result.append((char) b);
 		}
 		
 		return result.toString();
 	}
 
 	@Override
 	public EvalResult visitIntegerLiteralDecimalIntegerLiteral(
 			DecimalIntegerLiteral x) {
 		String str = x.getDecimal().toString();
 		return result(vf.integer(java.lang.Integer.parseInt(str)));
 	}
 	
 	@Override
 	public EvalResult visitExpressionQualifiedName(
 			org.meta_environment.rascal.ast.Expression.QualifiedName x) {
 		if (isTreeConstructorName(x.getQualifiedName())) {
 			return constructTree(x.getQualifiedName(), new IValue[0], tf.tupleType(new Type[0]));
 		}
 		else {
 			EvalResult result = env.getVariable(x.getQualifiedName());
 
 			if (result != null && result.value != null) {
 				return result;
 			} else {
 				throw new RascalTypeError("Uninitialized variable: " + x);
 			}
 		}
 	}
 	
 	
 	@Override
 	public EvalResult visitExpressionList(List x) {
 		java.util.List<org.meta_environment.rascal.ast.Expression> elements = x
 				.getElements();
 		java.util.List<IValue> results = new LinkedList<IValue>();
 		Type elementType = evaluateElements(elements, results);
 
 		ListType resultType = tf.listType(elementType);
 		IListWriter w = resultType.writer(vf);
 		w.appendAll(results);
 		return result(resultType, w.done());
 	}
 
 	@Override
 	public EvalResult visitExpressionSet(Set x) {
 		java.util.List<org.meta_environment.rascal.ast.Expression> elements = x
 				.getElements();
 		java.util.List<IValue> results = new LinkedList<IValue>();
 		Type elementType = evaluateElements(elements, results);
 
 		SetType resultType = tf.setType(elementType);
 		ISetWriter w = resultType.writer(vf);
 		w.insertAll(results);
 		return result(resultType, w.done());
 	}
 
 	private Type evaluateElements(
 			java.util.List<org.meta_environment.rascal.ast.Expression> elements,
 			java.util.List<IValue> results) {
 		Type elementType = tf.voidType();
 
 		for (org.meta_environment.rascal.ast.Expression expr : elements) {
 			EvalResult resultElem = expr.accept(this);
 			elementType = elementType.lub(resultElem.type);
 			results.add(results.size(), resultElem.value);
 		}
 		return elementType;
 	}
 
 	@Override
 	public EvalResult visitExpressionMap(
 			org.meta_environment.rascal.ast.Expression.Map x) {
 
 		java.util.List<org.meta_environment.rascal.ast.Mapping> mappings = x
 				.getMappings();
 		Map<IValue,IValue> result = new HashMap<IValue,IValue>();
 		Type keyType = tf.voidType();
 		Type valueType = tf.voidType();
 
 		for (org.meta_environment.rascal.ast.Mapping mapping : mappings) {
 			EvalResult keyResult = mapping.getFrom().accept(this);
 			EvalResult valueResult = mapping.getTo().accept(this);
 			
 			keyType = keyType.lub(keyResult.type);
 			valueType = valueType.lub(valueResult.type);
 			
 			result.put(keyResult.value, valueResult.value);
 		}
 		
 		MapType type = tf.mapType(keyType, valueType);
 		IMapWriter w = type.writer(vf);
 		w.putAll(result);
 		
 		return result(type, w.done());
 	}
 
 	
 	@Override
 	public EvalResult visitExpressionNonEmptyBlock(NonEmptyBlock x) {
 		// TODO: shouldn't this be a closure?
 		EvalResult r = result();
 		for(Statement stat : x.getStatements()){
 			r = stat.accept(this);
 		}
 		return r;
 	}
 
 	@Override
 	public EvalResult visitExpressionTuple(Tuple x) {
 		java.util.List<org.meta_environment.rascal.ast.Expression> elements = x
 				.getElements();
 
 		IValue[] values = new IValue[elements.size()];
 		Type[] types = new Type[elements.size()];
 
 		for (int i = 0; i < elements.size(); i++) {
 			EvalResult resultElem = elements.get(i).accept(this);
 			types[i] = resultElem.type;
 			values[i] = resultElem.value;
 		}
 
 		return result(tf.tupleType(types), vf.tuple(values));
 	}
 	
 	@Override
 	public EvalResult visitExpressionAnnotation(
 			org.meta_environment.rascal.ast.Expression.Annotation x) {
 		  EvalResult expr = x.getExpression().accept(this);
 		String name = x.getName().toString();
 
 		Type annoType = tf.getAnnotationType(expr.type, name);
 
 		if (annoType == null) {
 			throw new RascalTypeError("No annotation " + x.getName()
 					+ " declared on " + expr.type);
 		}
 
 		IValue annoValue = expr.value.getAnnotation(name);
 		
 		if (annoValue == null) {
 			// TODO: make this a Rascal exception that can be caught by the programmer
 			throw new RascalTypeError("This " + expr.type + " does not have a " + name + " annotation set");
 		}
 		return result(annoType, annoValue);
 	}
 	
     @Override
 	public EvalResult visitExpressionAddition(Addition x) {
 		EvalResult left = x.getLhs().accept(this);
 		EvalResult right = x.getRhs().accept(this);
 		Type resultType = left.type.lub(right.type);
 
 		if (left.type.isIntegerType() && right.type.isIntegerType()) {
 			return result(((IInteger) left.value).add((IInteger) right.value));
 		} else if (left.type.isDoubleType() && right.type.isDoubleType()) {
 			return result(((IDouble) left.value).add((IDouble) right.value));
 		} else if (left.type.isStringType() && right.type.isStringType()) {
 			return result(vf.string(((IString) left.value).getValue()
 					+ ((IString) right.value).getValue()));
 		} else if (left.type.isListType() && right.type.isListType()) {
 			
 			return result(resultType, ((IList) left.value)
 					.concat((IList) right.value));
 		} else if (left.type.isSetType() && right.type.isSetType()) {
 				return result(resultType, ((ISet) left.value)
 						.union((ISet) right.value));
 		} else if (left.type.isMapType() && right.type.isMapType()) {
 			return result(resultType, ((IMap) left.value)              //TODO: is this the right operation?
 					.join((IMap) right.value));
 		} else if (left.type.isRelationType() && right.type.isRelationType()) {
 				return result(resultType, ((ISet) left.value)
 						.union((ISet) right.value));
 		} else {
 			throw new RascalTypeError("Operands of + have illegal types: "
 					+ left.type + ", " + right.type);
 		}
 	}
     
 	public EvalResult visitExpressionSubtraction(Subtraction x) {
 		EvalResult left = x.getLhs().accept(this);
 		EvalResult right = x.getRhs().accept(this);
 		Type resultType = left.type.lub(right.type);
 
 		if (left.type.isIntegerType() && right.type.isIntegerType()) {
 			return result(((IInteger) left.value).subtract((IInteger) right.value));
 		} else if (left.type.isDoubleType() && right.type.isDoubleType()) {
 			return result(((IDouble) left.value).subtract((IDouble) right.value));
 		} else if (left.type.isListType() && right.type.isListType()) {
 			notImplemented("- on list");
 		} else if (left.type.isSetType() && right.type.isSetType()) {
 				return result(resultType, ((ISet) left.value)
 					.subtract((ISet) right.value));
 		} else if (left.type.isMapType() && right.type.isMapType()) {
 			return result(resultType, ((IMap) left.value)
 					.remove((IMap) right.value));
 		} else if (left.type.isRelationType() && right.type.isRelationType()) {
 			return result(resultType, ((ISet) left.value)
 					.subtract((ISet) right.value));
 		} else {
 			throw new RascalTypeError("Operands of - have illegal types: "
 					+ left.type + ", " + right.type);
 		}
 		return result();
 	}
 	
 	@Override
 	public EvalResult visitExpressionNegative(Negative x) {
 		EvalResult arg = x.getArgument().accept(this);
 		if (arg.type.isIntegerType()) {
 			return result(vf.integer(- intValue(arg)));
 		}
 		else if (arg.type.isDoubleType()) {
 				return result(vf.dubble(- doubleValue(arg)));
 		} else {
 			throw new RascalTypeError(
 					"Operand of unary - should be integer or double instead of: " + arg.type);
 		}
 	}
 	
 	@Override
 	public EvalResult visitExpressionProduct(Product x) {
 		EvalResult left = x.getLhs().accept(this);
 		EvalResult right = x.getRhs().accept(this);
 
 		if (left.type.isIntegerType() && right.type.isIntegerType()) {
 			return result(((IInteger) left.value).multiply((IInteger) right.value));
 		} 
 		else if (left.type.isDoubleType() && right.type.isDoubleType()) {
 			return result(((IDouble) left.value).multiply((IDouble) right.value));
 		}
 		else if (left.type.isDoubleType() && right.type.isIntegerType()) {
 			return result(((IDouble) left.value).multiply(((IInteger) right.value).toDouble()));
 		}
 		else if (left.type.isIntegerType() && right.type.isDoubleType()) {
 			return result(((IInteger) left.value).toDouble().multiply((IDouble) right.value));
 		} 
 		
 		else if (left.type.isListType() && right.type.isListType()){
 			Type leftElementType = ((ListType) left.type).getElementType();
 			Type rightElementType = ((ListType) right.type).getElementType();
 			Type resultType = tf.listType(tf.tupleType(leftElementType, rightElementType));
 			IListWriter w = resultType.writer(vf);
 			
 			for(IValue v1 : (IList) left.value){
 				for(IValue v2 : (IList) right.value){
 					w.append(vf.tuple(v1, v2));	
 				}
 			}
 			return result(resultType, w.done());	
 		}
 		else if (left.type.isSetType() && right.type.isSetType()){
 			Type leftElementType = ((SetType) left.type).getElementType();
 			Type rightElementType = ((SetType) right.type).getElementType();
 			Type resultType = tf.relType(leftElementType, rightElementType);
 			IRelationWriter w = resultType.writer(vf);
 			
 			for(IValue v1 : (ISet) left.value){
 				for(IValue v2 : (ISet) right.value){
 					w.insert(vf.tuple(v1, v2));	
 				}
 			}
 			return result(resultType, w.done());	
 		}
 		else {
 			throw new RascalTypeError("Operands of * have illegal types: "
 					+ left.type + ", " + right.type);
 		}
 	}
 	
 	@Override
 	public EvalResult visitExpressionDivision(Division x) {
 		EvalResult left = x.getLhs().accept(this);
 		EvalResult right = x.getRhs().accept(this);
 
 		if (left.type.isIntegerType() && right.type.isIntegerType()) {
 			return result(((IInteger) left.value).divide((IInteger) right.value));
 		} 
 		else if (left.type.isDoubleType() && right.type.isDoubleType()) {
 			return result(((IDouble) left.value).divide((IDouble) right.value));
 		}
 		else if (left.type.isDoubleType() && right.type.isIntegerType()) {
 			return result(((IDouble) left.value).divide(((IInteger) right.value).toDouble()));
 		}
 		else if (left.type.isIntegerType() && right.type.isDoubleType()) {
 			return result(((IInteger) left.value).toDouble().divide((IDouble) right.value));
 		} 
 		else {
 			throw new RascalTypeError("Operands of / have illegal types: "
 					+ left.type + ", " + right.type);
 		}
 	}
 	
 	@Override
 	public EvalResult visitExpressionModulo(Modulo x) {
 		EvalResult left = x.getLhs().accept(this);
 		EvalResult right = x.getRhs().accept(this);
 
 		if (left.type.isIntegerType() && right.type.isIntegerType()) {
 			return result(((IInteger) left.value).remainder((IInteger) right.value));
 		} 
 		else {
 			throw new RascalTypeError("Operands of % have illegal types: "
 					+ left.type + ", " + right.type);
 		}
 	}
 	
 	@Override
 	public EvalResult visitExpressionBracket(Bracket x) {
 		return x.getExpression().accept(this);
 	}
 	
 	@Override
 	public EvalResult visitExpressionIntersection(Intersection x) {
 		EvalResult left = x.getLhs().accept(this);
 		EvalResult right = x.getRhs().accept(this);
 		Type resultType = left.type.lub(right.type);
 
 		if (left.type.isSetType() && right.type.isSetType()) {
 				return result(resultType, ((ISet) left.value)
 				.intersect((ISet) right.value));
 		} else if (left.type.isMapType() && right.type.isMapType()) {
 			return result(resultType, ((IMap) left.value)
 					.common((IMap) right.value));
 
 		} else if (left.type.isRelationType() && right.type.isRelationType()) {
 			return result(resultType, ((ISet) left.value)
 				.intersect((ISet) right.value));
 		} else {
 			throw new RascalTypeError("Operands of & have illegal types: "
 					+ left.type + ", " + right.type);
 		}
 	}
 
 	@Override
 	public EvalResult visitExpressionOr(Or x) {
 		EvalResult left = x.getLhs().accept(this);
 		EvalResult right = x.getRhs().accept(this);
 		if (left.type.isBoolType() && right.type.isBoolType()) {
 			return result(((IBool) left.value).or((IBool) right.value));
 		} else {
 			throw new RascalTypeError(
 					"Operands of || should be boolean instead of: " + left.type
 							+ ", " + right.type);
 		}
 	}
 
 	@Override
 	public EvalResult visitExpressionAnd(And x) {
 		EvalResult left = x.getLhs().accept(this);
 		EvalResult right = x.getRhs().accept(this);
 		if (left.type.isBoolType() && right.type.isBoolType()) {
 			return result(((IBool) left.value).and((IBool) right.value));
 		} else {
 			throw new RascalTypeError(
 					"Operands of && should be boolean instead of: " + left.type
 							+ ", " + right.type);
 		}
 	}
 
 	@Override
 	public EvalResult visitExpressionNegation(Negation x) {
 		EvalResult arg = x.getArgument().accept(this);
 		if (arg.type.isBoolType()) {
 			return result(((IBool) arg.value).not());
 		} else {
 			throw new RascalTypeError(
 					"Operand of ! should be boolean instead of: " + arg.type);
 		}
 	}
 	
 	@Override
 	public EvalResult visitExpressionImplication(Implication x) {
 		EvalResult left = x.getLhs().accept(this);
 		EvalResult right = x.getRhs().accept(this);
 		if(left.type.isBoolType() && right.type.isBoolType()){
 			if(left.value.equals(vf.bool(true)) &&
 			   right.value.equals(vf.bool(false))){
 				return result(vf.bool(false));
 			} else {
 				return result(vf.bool(true));
 			}
 		} else {
 			throw new RascalTypeError(
 					"Operands of ==> should be boolean instead of: " + left.type + ", " + right.type);
 		}
 	}
 	
 	@Override
 	public EvalResult visitExpressionEquivalence(Equivalence x) {
 		EvalResult left = x.getLhs().accept(this);
 		EvalResult right = x.getRhs().accept(this);
 		if(left.type.isBoolType() && right.type.isBoolType()){
 			if(left.value.equals(right.value)) {
 				return result(vf.bool(true));
 			} else {
 				return result(vf.bool(false));
 			}
 		} else {
 			throw new RascalTypeError(
 					"Operands of <==> should be boolean instead of: " + left.type + ", " + right.type);
 		}
 	}
 	
 	boolean equals(EvalResult left, EvalResult right){
 		if (left.type.comparable(right.type)
 				//|| left.type.isValueType()                   //TODO: is this necessary?
 				//|| right.type.isValueType()
 		) {
 			return left.value.equals(right.value);
 		} else {
 			throw new RascalTypeError(
 					"Operands of == have incompatible types: " + left.type + ", " + right.type);
 		}
 	}
 
 	@Override
 	public EvalResult visitExpressionEquals(
 			org.meta_environment.rascal.ast.Expression.Equals x) {
 		EvalResult left = x.getLhs().accept(this);
 		EvalResult right = x.getRhs().accept(this);
 		
 		return result(vf.bool(equals(left, right)));
 	}
 	
 	@Override
 	public EvalResult visitExpressionOperatorAsValue(OperatorAsValue x) {
 		// TODO
 		throw new RascalBug("Operator as value not yet implemented:" + x);
 	}
 	
 	@Override
 	public EvalResult visitExpressionArea(Area x) {
 		return x.getArea().accept(this);
 	}
 	
 	@Override
 	public EvalResult visitAreaDefault(
 			org.meta_environment.rascal.ast.Area.Default x) {
 		EvalResult beginLine = x.getBeginLine().accept(this);
 		EvalResult endLine = x.getEndLine().accept(this);
 		EvalResult beginColumn = x.getBeginColumn().accept(this);
 		EvalResult endColumn = x.getEndColumn().accept(this);
 		EvalResult length = x.getLength().accept(this);
 		EvalResult offset = x.getOffset().accept(this);
 		
 		int iOffset = intValue(offset);
 		int iLength = intValue(length);
 		int iEndColumn = intValue(endColumn);
 		int iBeginColumn = intValue(beginColumn);
 		int iEndLine = intValue(endLine);
 		int iBeginLine = intValue(beginLine);
 	
 		ISourceRange r = vf.sourceRange(iOffset, iLength, iBeginLine, iEndLine, iBeginColumn, iEndColumn);
 		return result(tf.sourceRangeType(), r);
 	}
 	
 	@Override
 	public EvalResult visitExpressionAreaInFileLocation(AreaInFileLocation x) {
 	   EvalResult area = x.getAreaExpression().accept(this);
 	   EvalResult file = x.getFilename().accept(this);
 	   
 	   checkType(area, tf.sourceRangeType());
 	   checkType(file, tf.stringType());
 	   
 	   ISourceRange range = (ISourceRange) area.value;
 	   
 	   return result(tf.sourceLocationType(), vf.sourceLocation(stringValue(file), range));
 	}
 	
 	@Override
 	public EvalResult visitExpressionAreaLocation(AreaLocation x) {
 		// TODO I think this is a bug in the grammar (double area(area(...)))
 		throw new RascalBug("Area in file nyi:" + x);
 	}
 	
 	@Override
 	public EvalResult visitExpressionClosure(Closure x) {
 		// TODO
 		throw new RascalBug("Closures NYI " + x);
 	}
 	
 	@Override
 	public EvalResult visitExpressionFieldUpdate(FieldUpdate x) {
 		EvalResult expr = x.getExpression().accept(this);
 		EvalResult repl = x.getReplacement().accept(this);
 		String name = x.getKey().toString();
 		
 		try {
 			if (expr.type.getBaseType().isTupleType()) {
 				TupleType tuple = (TupleType) expr.type.getBaseType();
 				Type argType = tuple.getFieldType(name);
 				ITuple value = (ITuple) expr.value;
 				
 				checkType(repl.type, argType);
 				
 				return result(expr.type, value.set(name, repl.value));
 			}
 			else if (expr.type.isNamedTreeType() || expr.type.isTreeNodeType()) {
 				TupleType tuple = (TupleType) ((INode) expr.type.getBaseType()).getChildrenTypes();
 				Type argType = tuple.getFieldType(name);
 				INode value = (INode) expr.value;
 				
 				checkType(repl.type, argType);
 				
 				return result(expr.type, value.set(name, repl.value));
 			}
 			else {
 				throw new RascalTypeError("Field updates only work on tuples with labeled fields, relations with labeled fields and data constructors");
 			}
 		} catch (FactTypeError e) {
 			throw new RascalTypeError(e.getMessage(), e);
 		}
 	}
 	
 	@Override
 	public EvalResult visitExpressionFileLocation(FileLocation x) {
 		throw new RascalBug("Fileloc NYI: " + x);// TODO
 	}
 	
 	@Override
 	public EvalResult visitExpressionLexical(Lexical x) {
 		throw new RascalBug("Lexical NYI: " + x);// TODO
 	}
 	
 	@Override
 	public EvalResult visitExpressionRange(Range x) {
 		IListWriter w = vf.listWriter(tf.integerType());
 		EvalResult from = x.getFirst().accept(this);
 		EvalResult to = x.getLast().accept(this);
 
 		int iFrom = intValue(from);
 		int iTo = intValue(to);
 		
 		if (iTo < iFrom) {
 			for (int i = iFrom; i >= iTo; i--) {
 				w.append(vf.integer(i));
 			}	
 		}
 		else {
 			for (int i = iFrom; i <= iTo; i++) {
 				w.append(vf.integer(i));
 			}
 		}
 		
 		return result(tf.listType(tf.integerType()), w.done());
 	}
 	
 	@Override
 	public EvalResult visitExpressionStepRange(StepRange x) {
 		IListWriter w = vf.listWriter(tf.integerType());
 		EvalResult from = x.getFirst().accept(this);
 		EvalResult to = x.getLast().accept(this);
 		EvalResult second = x.getSecond().accept(this);
 
 		int iFrom = intValue(from);
 		int iSecond = intValue(second);
 		int iTo = intValue(to);
 		
		int diff = Math.abs(iFrom - iSecond);
 		
		if (iTo < iFrom) {
			for (int i = iFrom; i >= iTo; i -= diff) {
				w.append(vf.integer(i));
			}	
		}
		else {
			for (int i = iFrom; i <= iTo; i += diff) {
 				w.append(vf.integer(i));
 			}
 		}
 		
 		return result(tf.listType(tf.integerType()), w.done());
 		
 	}
 	
 	@Override
 	public EvalResult visitExpressionTypedVariable(TypedVariable x) {
 		throw new RascalTypeError("Use of typed variable outside matching context");
 	}
 	
 	@Override
 	public EvalResult visitExpressionVisit(Visit x) {
 		return x.getVisit().accept(this);
 	}
 	
 	@Override
 	public EvalResult visitVisitDefaultStrategy(DefaultStrategy x) {
 		throw new RascalBug("NYI"); // TODO
 	}
 	
 	@Override
 	public EvalResult visitVisitGivenStrategy(GivenStrategy x) {
 		throw new RascalBug("NYI"); // TODO
 	}
 	
 	@Override
 	public EvalResult visitExpressionVoidClosure(VoidClosure x) {
 		// TODO
 		throw new RascalBug("void closure NYI");
 	}
 	
 	@Override
 	public EvalResult visitExpressionNonEquals(
 			org.meta_environment.rascal.ast.Expression.NonEquals x) {
 		EvalResult left = x.getLhs().accept(this);
 		EvalResult right = x.getRhs().accept(this);
 		
 		return result(vf.bool(!equals(left, right)));
 	}
 	
 	
 	private int compare(EvalResult left, EvalResult right){
 		
 		
 		if (left.type.isBoolType() && right.type.isBoolType()) {
 			boolean lb = ((IBool) left.value).getValue();
 			boolean rb = ((IBool) right.value).getValue();
 			return (lb == rb) ? 0 : ((!lb && rb) ? -1 : 1);
 		}
 		if (left.type.isIntegerType() && right.type.isIntegerType()) {
 			return ((IInteger) left.value).compare((IInteger) right.value);
 		}
 		if (left.type.isDoubleType() && right.type.isDoubleType()) {
 			return ((IDouble) left.value).compare((IDouble) right.value);
 		}
 		if (left.type.isStringType() && right.type.isStringType()) {
 			return ((IString) left.value).compare((IString) right.value);
 		}
 		if (left.type.isListType() && right.type.isListType()) {
 			return compareList(((IList) left.value).iterator(), ((IList) left.value).length(),
 					            ((IList) right.value).iterator(), ((IList) right.value).length());
 		}
 		if (left.type.isSetType() && right.type.isSetType()) {
 			return compareSet((ISet) left.value, (ISet) right.value);
 		}
 		if (left.type.isMapType() && right.type.isMapType()) {
 			return compareMap((IMap) left.value, (IMap) right.value);
 		}
 		if (left.type.isTupleType() && right.type.isTupleType()) {
 			return compareList(((ITuple) left.value).iterator(), ((ITuple) left.value).arity(),
 		            ((ITuple) right.value).iterator(), ((ITuple) right.value).arity());
 		} 
 		if (left.type.isRelationType() && right.type.isRelationType()) {
 			return compareSet((ISet) left.value, (ISet) right.value);
 		}
 		
 		if (left.type.isSubtypeOf(tf.treeType()) && right.type.isSubtypeOf(tf.treeType())){
 			String leftName = ((ITree) left.value).getName().toString();
 			String rightName = ((ITree) right.value).getName().toString();
 			int compare = leftName.compareTo(rightName);
 			
 			if(compare != 0){
 				return compare;
 			}
 			return compareList(((ITree) left.value).iterator(), ((ITree) left.value).arity(),
 		            ((ITree) right.value).iterator(), ((ITree) right.value).arity());
 		}
 		
 		// SourceLocation
 		// NamedType
 		// NamedTreeType
 		// NamedTreeType
 		// TreeNodeType
 		// VoidType
 		// ValueType
 	
 		//return compareDifferentlyTypedValues(left.type, right.type);
 		throw new RascalTypeError("Operands of comparison have unequal types: "
 					+ left.type + ", " + right.type);
 	}
 	
 	private Type typeOrder[] = {
 			tf.voidType(),
 			tf.boolType(),
 			tf.integerType(),
 			tf.doubleType(),
 			tf.stringType(),
 			tf.sourceLocationType(),
 			tf.voidType(),  // tupleType
 			tf.listType(tf.valueType()),
 			tf.setType(tf.valueType()),
 			tf.mapType(tf.valueType(), tf.valueType()),
 			tf.treeType(),
 			tf.valueType()
 			};
 	
 	private int typeIndex(Type type){
 		if(type.isTupleType()){
 			return 6;
 		}
 		for(int i = 0; i < typeOrder.length; i++){
 			if(type.isSubtypeOf(typeOrder[i])){
 				return i;
 			}
 		}
 		throw new RascalBug("Cannot determine type index of " + type);
 	}
 	
 	private int compareDifferentlyTypedValues(Type type1, Type type2) {
 		int i1 = typeIndex(type1);
 		int i2 = typeIndex(type2);
 		
 		if(i1 == i2){
 			throw new RascalBug("unequal types " + type1 + " and " + type2 + " have equal index " + i1);
 		}
 		return i1 < i2 ? -1 : 1;
 	}
 	private int compareSet(ISet value1, ISet value2) {
 		if (value1.equals(value2)) {
 			return 0;
 		}
 		else if (value1.isSubSet(value2)) {
 			return -1;
 		}
 		else {
 			return 1;
 		}
 	}
 	
 	private int compareMap(IMap value1, IMap value2) {
 		if (value1.equals(value2)) {
 			return 0;
 		}
 		else if (value1.isSubMap(value2)) {
 			return -1;
 		}
 		else {
 			return 1;
 		}
 	}
 
 	private int compareList(Iterator<IValue> left, int leftLen, Iterator<IValue> right, int rightLen){
 		
 		if(leftLen == 0){
 			return rightLen == 0 ? 0 : -1;
 		}
 		if(rightLen == 0){
 			return 1;
 		}
 		int m = (leftLen > rightLen) ? rightLen : leftLen;  
 		int compare = 0;
 		
 		for(int i = 0; i < m; i++){
 			IValue leftVal = left.next();
 			IValue rightVal = right.next();
 			EvalResult vl = result(leftVal.getType(), leftVal);
 			EvalResult vr = result(rightVal.getType(), rightVal);
 			int c = compare(vl, vr);
 
 			if(compare == c){
 				continue;
 			} else
 				if(i == 0){
 					compare = c;
 				} else {
 						return c;
 				}
 		}
 		
 		if(compare == 0 && leftLen != rightLen){
 			compare = leftLen < rightLen ? -1 : 1;
 		}
 			
 		return compare;
 	}
 	
 	@Override
 	public EvalResult visitExpressionLessThan(LessThan x) {
 		EvalResult left = x.getLhs().accept(this);
 		EvalResult right = x.getRhs().accept(this);
 		
 		return result(vf.bool(compare(left, right) < 0));
 	}
 	
 	@Override
 	public EvalResult visitExpressionLessThanOrEq(LessThanOrEq x) {
 		EvalResult left = x.getLhs().accept(this);
 		EvalResult right = x.getRhs().accept(this);
 		
 		return result(vf.bool(compare(left, right) <= 0));
 	}
 	@Override
 	public EvalResult visitExpressionGreaterThan(GreaterThan x) {
 		EvalResult left = x.getLhs().accept(this);
 		EvalResult right = x.getRhs().accept(this);
 		
 		return result(vf.bool(compare(left, right) > 0));
 	}
 	
 	@Override
 	public EvalResult visitExpressionGreaterThanOrEq(GreaterThanOrEq x) {
 		EvalResult left = x.getLhs().accept(this);
 		EvalResult right = x.getRhs().accept(this);
 		
 		return result(vf.bool(compare(left, right) >= 0));
 	}
 	
 	@Override
 	public EvalResult visitExpressionIfThenElse(
 			org.meta_environment.rascal.ast.Expression.IfThenElse x) {
 		EvalResult cval = x.getCondition().accept(this);
 	
 		if (cval.type.isBoolType()) {
 			if (cval.value.equals(vf.bool(true))) {
 				return x.getThenExp().accept(this);
 			}
 		} else {
 			throw new RascalTypeError("Condition has type "
 					+ cval.type + " but should be bool");
 		}
 		return x.getElseExp().accept(this);
 	}
 	
 	@Override
 	public EvalResult visitExpressionIfDefined(IfDefined x) {
 		try {
 			EvalResult res = x.getLhs().accept(this);
 			return res;
 		} catch (Exception e) {   //TODO: make this more restrictive
 			EvalResult res = x.getRhs().accept(this);
 			return res;
 		}
 	}
 	
 	private boolean in(org.meta_environment.rascal.ast.Expression expression, org.meta_environment.rascal.ast.Expression expression2){
 		EvalResult left = expression.accept(this);
 		EvalResult right = expression2.accept(this);
 		
 		if(right.type.isListType() &&
 		    left.type.isSubtypeOf(((ListType) right.type).getElementType())){
 			IList lst = (IList) right.value;
 			IValue val = left.value;
 			for(int i = 0; i < lst.length(); i++){
 				if(lst.get(i).equals(val))
 					return true;
 			}
 			return false;
 		} else if(right.type.isSetType() && 
 				   left.type.isSubtypeOf(((SetType) right.type).getElementType())){
 			return ((ISet) right.value).contains(left.value);
 			
 		} else if(right.type.isMapType() && left.type.isSubtypeOf(((MapType) right.type).getValueType())){
 			return ((IMap) right.value).containsValue(left.value);
 		} else if(right.type.isRelationType() && left.type.isSubtypeOf(((SetType) right.type).getElementType())){
 			return ((ISet) right.value).contains(left.value);
 		} else {
 			throw new RascalTypeError("Operands of in have wrong types: "
 					+ left.type + ", " + right.type);
 		}
 	}
 	
 	@Override
 	public EvalResult visitExpressionIn(In x) {
 		return result(vf.bool(in(x.getLhs(), x.getRhs())));
 
 	}
 	
 	@Override
 	public EvalResult visitExpressionNotIn(NotIn x) {
 		return result(vf.bool(!in(x.getLhs(), x.getRhs())));
 
 	}
 	
 	
 	
 	@Override
 	public EvalResult visitExpressionComposition(Composition x) {
 		EvalResult left = x.getLhs().accept(this);
 		EvalResult right = x.getRhs().accept(this);
 	
 		if(left.type.isRelationType() && 
 			right.type.isRelationType()){
 			RelationType leftrelType = (RelationType) left.type; 
 
 			RelationType rightrelType = (RelationType) right.type;
 			
 
 			// ALARM: not using declared types
 			if (leftrelType.getArity() == 2
 					&& rightrelType.getArity() == 2
 					&& leftrelType.getFieldType(1).equals(
 							rightrelType.getFieldType(0))) {
 				RelationType resultType = leftrelType.compose(rightrelType);
 				return result(resultType, ((IRelation) left.value)
 						.compose((IRelation) right.value));
 			}
 			if(((IRelation)left.value).size() == 0)
 				return left;
 			if(((IRelation)right.value).size() == 0)
 				return right;
 		}
 		else if (left.type.isSetType() && ((SetType) left.type).getElementType().isVoidType()) {
 			return left;
 		}
 		else if (right.type.isSetType() && ((SetType) right.type).getElementType().isVoidType()) {
 			return right;
 		}
 		
 		throw new RascalTypeError("Operands of o have wrong types: "
 				+ left.type + ", " + right.type);
 	}
 
 	private EvalResult closure(EvalResult arg, boolean reflexive) {
 
 		if (arg.type.isRelationType()) {
 			RelationType relType = (RelationType) arg.type;
 			Type fieldType1 = relType.getFieldType(0);
 			Type fieldType2 = relType.getFieldType(1);
 			String fieldName1 = relType.getFieldName(0);
 			String fieldName2 = relType.getFieldName(1);
 			
 			if (relType.getArity() == 2
 					&& (fieldType1.isSubtypeOf(fieldType2)
 							|| fieldType2.isSubtypeOf(fieldType1))) {
 				Type lub = fieldType1.lub(fieldType2);
 				
 				RelationType resultType = fieldName1 != null ? tf.relType(lub, fieldName1, lub, fieldName2) : tf.relType(lub,lub);
 				return result(resultType, reflexive ? ((IRelation) arg.value).closureStar()
 						: ((IRelation) arg.value).closure());
 			}
 		}
 		else if (arg.type.isSetType() && ((SetType) arg.type).getElementType().isVoidType()) {
 			return arg;
 		}
 		throw new RascalTypeError("Operand of + or * closure has wrong type: "
 				+ arg.type);
 	}
 	
 	@Override
 	public EvalResult visitExpressionTransitiveClosure(TransitiveClosure x) {
 		return closure(x.getArgument().accept(this), false);
 	}
 	
 	@Override
 	public EvalResult visitExpressionTransitiveReflexiveClosure(
 			TransitiveReflexiveClosure x) {
 		return closure(x.getArgument().accept(this), true);
 	}
 	
 	// Comprehensions ----------------------------------------------------
 	
 	@Override
 	public EvalResult visitExpressionComprehension(Comprehension x) {
 		return x.getComprehension().accept(this);
 	}
 
 	//TODO: parameterize with an indicator for list/set/map generators;
 	
 	private class GeneratorEvaluator {
 		private boolean isValueProducer;
 		private boolean firstTime = true;
 		private org.meta_environment.rascal.ast.Expression expr;
 		private PatternValue pat;
 		private org.meta_environment.rascal.ast.Expression patexpr;
 		private Evaluator evaluator;
 		private Iterator<?> iter;
 		private Type elementType;
 		
 		void make(ValueProducer vp, Evaluator ev){
 			evaluator = ev;
 			isValueProducer = true;
 			
 			pat = evalPattern(vp.getPattern());
 			patexpr = vp.getExpression();
 			EvalResult r = patexpr.accept(ev);
 			if(r.type.isListType()){
 				elementType = ((ListType) r.type).getElementType();
 				iter = ((IList) r.value).iterator();
 			} else 	if(r.type.isSetType()){
 				elementType = ((SetType) r.type).getElementType();
 				iter = ((ISet) r.value).iterator();
 			
 			} else if(r.type.isTreeType()){
 				iter = new ITreeIterator((ITree) r.value, false);
 			} else {
 				// TODO: add more generator types here	in the future
 				throw new RascalTypeError("expression in generator should be of type list/set");
 			}
 		}
 		
 		GeneratorEvaluator(ValueProducer vp, Evaluator ev){
 			make(vp, ev);
 		}
 
 		GeneratorEvaluator(Generator g, Evaluator ev){
 			if(g.isProducer()){
 				make(g.getProducer(), ev);
 			} else {
 				evaluator = ev;
 				isValueProducer = false;
 				expr = g.getExpression();
 			}
 		}
 
 		public boolean getNext(){
 			if(isValueProducer){
 				while(iter.hasNext()){
 					if(pat.match((IValue)iter.next(), evaluator)){
 						return true;
 					}
 				}
 				return false;
 			} else {
 				if(firstTime){
 					/* Evaluate expression only once */
 					firstTime = false;
 					EvalResult v = expr.accept(evaluator);
 					if(v.type.isBoolType()){
 						return v.value.equals(vf.bool(true));
 					} else {
 						throw new RascalTypeError("Expression as generator should have type bool");
 					}
 				} else {
 					return false;
 				}
 			}
 		}
 	}
 	
 	@Override
 	public EvalResult visitComprehensionList(
 			org.meta_environment.rascal.ast.Comprehension.List x) {
 		org.meta_environment.rascal.ast.Expression resultExpr = x.getResult();
 		java.util.List<Generator> generators = x.getGenerators();
 		int size = generators.size();
 		GeneratorEvaluator[] gens = new GeneratorEvaluator[size];
 		Type elementType = tf.voidType();
 		Type resultType = tf.setType(elementType);
 		IListWriter res = null;
 		
 		int i = 0;
 		gens[0] = new GeneratorEvaluator(generators.get(0), this);
 		while(i >= 0 && i < size){		
 			if(gens[i].getNext()){
 				if(i == size - 1){
 					EvalResult r = resultExpr.accept(this);
 					if(res == null){
 						elementType = r.type.lub(elementType);
 						resultType = tf.listType(elementType);
 						res = resultType.writer(vf);
 					}
 					if(r.type.isSubtypeOf(elementType)){
 						elementType = elementType.lub(r.type);	
 						res.append(r.value);
 					}  else {
 							throw new RascalTypeError("Cannot add value of type " + r.type + " to list comprehension with element type " + elementType);
 						}
 				} else {
 					i++;
 					gens[i] = new GeneratorEvaluator(generators.get(i), this);
 				}
 			} else {
 				i--;
 			}
 		}
 		return (res == null) ? result(tf.listType(tf.voidType()), vf.list()) : 
 			                     result(tf.listType(elementType), res.done());
 	}
 	
 	@Override
 	public EvalResult visitComprehensionSet(
 			org.meta_environment.rascal.ast.Comprehension.Set x) {
 		org.meta_environment.rascal.ast.Expression resultExpr = x.getResult();
 		java.util.List<Generator> generators = x.getGenerators();
 		int size = generators.size();
 		GeneratorEvaluator[] gens = new GeneratorEvaluator[size];
 		Type elementType = tf.voidType();
 		Type resultType = tf.setType(elementType);
 		ISetWriter res = null;
 		
 		int i = 0;
 		gens[0] = new GeneratorEvaluator(generators.get(0), this);
 		while(i >= 0 && i < size){		
 			if(gens[i].getNext()){
 				if(i == size - 1){
 					EvalResult r = resultExpr.accept(this);
 					if(res == null){
 						elementType = r.type.lub(elementType);
 						resultType = tf.setType(elementType);
 						res = resultType.writer(vf);
 					}
 					if(r.type.isSubtypeOf(elementType)){
 						elementType = elementType.lub(r.type);	
 						res.insert(r.value);
 					}  else {
 							throw new RascalTypeError("Cannot add value of type " + r.type + " to set comprehension with element type " + elementType);
 						}
 				} else {
 					i++;
 					gens[i] = new GeneratorEvaluator(generators.get(i), this);
 				}
 			} else {
 				i--;
 			}
 		}
 		return (res == null) ? result(tf.setType(tf.voidType()), vf.set()) : 
 			                     result(tf.setType(elementType), res.done());
 	}
 	
 	@Override
 	public EvalResult visitComprehensionMap(
 			org.meta_environment.rascal.ast.Comprehension.Map x) {
 		org.meta_environment.rascal.ast.Expression fromExpr = x.getFrom();
 		org.meta_environment.rascal.ast.Expression toExpr = x.getTo();
 		java.util.List<Generator> generators = x.getGenerators();
 		int size = generators.size();
 		GeneratorEvaluator[] gens = new GeneratorEvaluator[size];
 		Type elementFromType = tf.voidType();
 		Type elementToType =tf.voidType();
 		Type resultType = tf.mapType(elementFromType, elementToType);
 		IMapWriter res = null;
 		
 		int i = 0;
 		gens[0] = new GeneratorEvaluator(generators.get(0), this);
 		while(i >= 0 && i < size){		
 			if(gens[i].getNext()){
 				if(i == size - 1){
 					EvalResult rfrom = fromExpr.accept(this);
 					EvalResult rto = toExpr.accept(this);
 					if(res == null){
 						elementFromType = rfrom.type.lub(elementFromType);
 						elementToType = rto.type.lub(elementToType);
 						resultType = tf.mapType(elementFromType, elementToType);
 						res = resultType.writer(vf);
 					}
 					if(rfrom.type.isSubtypeOf(elementFromType) && rto.type.isSubtypeOf(elementToType)){
 						elementFromType = elementFromType.lub(rfrom.type);	
 						elementFromType = elementToType.lub(rfrom.type);	
 						res.put(rfrom.value, rto.value);
 					}  else {
 							throw new RascalTypeError("Cannot add pair of type (" + rfrom.type + ":" + rto.type  + 
 									") to map comprehension with element type (" + elementFromType + ":"+ elementToType + ")");
 						}
 				} else {
 					i++;
 					gens[i] = new GeneratorEvaluator(generators.get(i), this);
 				}
 			} else {
 				i--;
 			}
 		}
 		return (res == null) ? result(tf.mapType(tf.voidType(), tf.voidType()), vf.map(tf.voidType(),tf.voidType())) : 
 			                     result(tf.mapType(elementFromType, elementToType), res.done());
 	}
 	
 	
 	@Override
 	public EvalResult visitStatementFor(For x) {
 		Statement body = x.getBody();
 		java.util.List<Generator> generators = x.getGenerators();
 		int size = generators.size();
 		GeneratorEvaluator[] gens = new GeneratorEvaluator[size];
 		EvalResult result = result();
 		
 		int i = 0;
 		gens[0] = new GeneratorEvaluator(generators.get(0), this);
 		while(i >= 0 && i < size){		
 			if(gens[i].getNext()){
 				if(i == size - 1){
 					result = body.accept(this);
 				} else {
 					i++;
 					gens[i] = new GeneratorEvaluator(generators.get(i), this);
 				}
 			} else {
 				i--;
 			}
 		}
 		return result;
 	}
 	
 	@Override
 	public EvalResult visitExpressionExists(Exists x) {
 		ValueProducer vp = x.getProducer();
 		org.meta_environment.rascal.ast.Expression exp = x .getExpression();
 		GeneratorEvaluator ge = new GeneratorEvaluator(vp, this);
 		while(ge.getNext()){
 			EvalResult r = exp.accept(this);
 			if(!r.type.isSubtypeOf(tf.boolType())){
 				throw new RascalTypeError("expression in exists should yield bool instead of " + r.type);
 			}
 			if(r.value.equals(vf.bool(true))){
 				return result(vf.bool(true));
 			}
 		}
 		return result(vf.bool(false));
 	}
 	
 	@Override
 	public EvalResult visitExpressionForAll(ForAll x) {
 		ValueProducer vp = x.getProducer();
 		org.meta_environment.rascal.ast.Expression exp = x .getExpression();
 		GeneratorEvaluator ge = new GeneratorEvaluator(vp, this);
 		while(ge.getNext()){
 			EvalResult r = exp.accept(this);
 			if(!r.type.isSubtypeOf(tf.boolType())){
 				throw new RascalTypeError("expression in forall should yield bool instead of " + r.type);
 			}
 			if(r.value.equals(vf.bool(false))){
 				return result(vf.bool(false));
 			}
 		}
 		return result(vf.bool(true));
 	}
 	
 	// ------------ solve -----------------------------------------
 	
 	@Override
 	public EvalResult visitStatementSolve(Solve x) {
 		java.util.ArrayList<Name> vars = new java.util.ArrayList<Name>();
 		
 		for(Declarator d : x.getDeclarations()){
 			for(org.meta_environment.rascal.ast.Variable v : d.getVariables()){
 				vars.add(v.getName());
 			}
 			d.accept(this);
 		}
 		IValue currentValue[] = new IValue[vars.size()];
 		for(int i = 0; i < vars.size(); i++){
 			currentValue[i] = null;
 		}
 		
 		Statement body = x.getBody();
 		EvalResult bodyResult = null;
 		
 		boolean change = true;
 		while (change){
 			change = false;
 			bodyResult = body.accept(this);
 			for(int i = 0; i < vars.size(); i++){
 				EvalResult v = env.getVariable(vars.get(i));
 				if(!v.value.equals(currentValue[i])){
 					change = true;
 					currentValue[i] = v.value;
 				}
 			}
 		}
 		return bodyResult;
 	}
 
 	
 }
