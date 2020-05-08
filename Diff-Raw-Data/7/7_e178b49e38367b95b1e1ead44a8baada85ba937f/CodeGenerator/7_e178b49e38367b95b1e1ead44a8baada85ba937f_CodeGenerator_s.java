 package ca.uwaterloo.joos.codegen;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.Stack;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import ca.uwaterloo.joos.Main;
 import ca.uwaterloo.joos.ast.ASTNode;
 import ca.uwaterloo.joos.ast.ASTNode.ChildTypeUnmatchException;
 import ca.uwaterloo.joos.ast.FileUnit;
 import ca.uwaterloo.joos.ast.Modifiers;
 import ca.uwaterloo.joos.ast.Modifiers.Modifier;
 import ca.uwaterloo.joos.ast.decl.BodyDeclaration;
 import ca.uwaterloo.joos.ast.decl.ClassDeclaration;
 import ca.uwaterloo.joos.ast.decl.ConstructorDeclaration;
 import ca.uwaterloo.joos.ast.decl.FieldDeclaration;
 import ca.uwaterloo.joos.ast.decl.LocalVariableDeclaration;
 import ca.uwaterloo.joos.ast.decl.MethodDeclaration;
 import ca.uwaterloo.joos.ast.decl.OnDemandImport;
 import ca.uwaterloo.joos.ast.decl.PackageDeclaration;
 import ca.uwaterloo.joos.ast.decl.ParameterDeclaration;
 import ca.uwaterloo.joos.ast.decl.SingleImport;
 import ca.uwaterloo.joos.ast.decl.TypeDeclaration;
 import ca.uwaterloo.joos.ast.decl.VariableDeclaration;
 import ca.uwaterloo.joos.ast.expr.AssignmentExpression;
 import ca.uwaterloo.joos.ast.expr.ClassCreateExpression;
 import ca.uwaterloo.joos.ast.expr.Expression;
 import ca.uwaterloo.joos.ast.expr.InfixExpression;
 import ca.uwaterloo.joos.ast.expr.InfixExpression.InfixOperator;
 import ca.uwaterloo.joos.ast.expr.MethodInvokeExpression;
 import ca.uwaterloo.joos.ast.expr.UnaryExpression;
 import ca.uwaterloo.joos.ast.expr.name.Name;
 import ca.uwaterloo.joos.ast.expr.name.QualifiedName;
 import ca.uwaterloo.joos.ast.expr.name.SimpleName;
 import ca.uwaterloo.joos.ast.expr.primary.ArrayAccess;
 import ca.uwaterloo.joos.ast.expr.primary.ArrayCreate;
 import ca.uwaterloo.joos.ast.expr.primary.FieldAccess;
 import ca.uwaterloo.joos.ast.expr.primary.LiteralPrimary;
 import ca.uwaterloo.joos.ast.expr.primary.LiteralPrimary.LiteralType;
 import ca.uwaterloo.joos.ast.expr.primary.Primary;
 import ca.uwaterloo.joos.ast.expr.primary.ThisPrimary;
 import ca.uwaterloo.joos.ast.statement.Block;
 import ca.uwaterloo.joos.ast.statement.ForStatement;
 import ca.uwaterloo.joos.ast.statement.IfStatement;
 import ca.uwaterloo.joos.ast.statement.ReturnStatement;
 import ca.uwaterloo.joos.ast.statement.WhileStatement;
 import ca.uwaterloo.joos.ast.type.PrimitiveType;
 import ca.uwaterloo.joos.ast.type.ReferenceType;
 import ca.uwaterloo.joos.ast.type.Type;
 import ca.uwaterloo.joos.checker.HierarchyChecker;
 import ca.uwaterloo.joos.symboltable.BlockScope;
 import ca.uwaterloo.joos.symboltable.Scope;
 import ca.uwaterloo.joos.symboltable.SemanticsVisitor;
 import ca.uwaterloo.joos.symboltable.SymbolTable;
 import ca.uwaterloo.joos.symboltable.TableEntry;
 import ca.uwaterloo.joos.symboltable.TypeScope;
 
 import com.google.common.io.Files;
 
 public class CodeGenerator extends SemanticsVisitor {
 	public static final Logger logger = Main.getLogger(CodeGenerator.class);
 
 	protected static final String BOOLEAN_TRUE = "0xffffffff";
 	protected static final String BOOLEAN_FALSE = "0x0";
 	protected static final String NULL = "0x0";
 
 	protected File asmFile = null;
 	protected static File startFile = null;
 	protected Set<String> externs = null;
 	protected List<String> texts = null;
 	protected List<String> data = null;
 	protected List<String> statics = null;
 	protected static List<String> staticInit = new ArrayList<String>();
 
 	private String methodLabel = null;
 	private Integer literalCount = 0;
 	private Integer comparisonCount = 0;
 	private Integer loopCount = 0;
 	private Integer conditionCount = 0;
 	private Boolean dereferenceVariable = true;
 	private Boolean referenceCurrentObject = true;
 
 	private Set<Class<?>> complexNodes = null;
 
 	public CodeGenerator(SymbolTable table) {
 		super(table);
 		logger.setLevel(Level.FINER);
 
 		this.complexNodes = new HashSet<Class<?>>();
 		this.complexNodes.add(ReferenceType.class);
 		this.complexNodes.add(PackageDeclaration.class);
 		this.complexNodes.add(SingleImport.class);
 		this.complexNodes.add(OnDemandImport.class);
 
 	}
 
 	private void initialize() {
 		this.asmFile = null;
 		this.statics = new ArrayList<String>();
 		this.externs = new HashSet<String>();
 		this.texts = new ArrayList<String>();
 		this.data = new ArrayList<String>();
 		this.methodLabel = null;
 		this.literalCount = 0;
 		this.comparisonCount = 0;
 		this.loopCount = 0;
 		this.dereferenceVariable = true;
 		this.referenceCurrentObject = true;
 
 		// Place the runtime.s externs
 		this.externs.add("__malloc");
 		this.externs.add("__debexit");
 		this.externs.add("__exception");
 		this.externs.add("NATIVEjava.io.OutputStream.nativeWrite");
 		this.externs.add("SubtypeTable");
 		this.texts.add("");
 		this.texts.add("section .text");
 		this.texts.add("");
 
 		this.data.add("");
 		this.data.add("section .data");
 		this.data.add("");
 	}
 
 	private void addExtern(String label, String declarationName) {
 		if (!this.getCurrentScope().getParentTypeScope().getSymbols().containsKey(declarationName)) {
 			logger.fine("Adding extern " + declarationName + " within scope " + this.getCurrentScope().getParentTypeScope());
 			this.externs.add(label);
 		}
 	}
 
 	private void addVtable(String fullyQualifiedTypeName) {
 		if (!this.getCurrentScope().getParentTypeScope().getName().equals(fullyQualifiedTypeName)) {
 			this.externs.add(fullyQualifiedTypeName + "_VTABLE");
 		}
 	}
 
 	@Override
 	public void willVisit(ASTNode node) throws Exception {
 		super.willVisit(node);
 
 		if (node instanceof FileUnit) {
 			this.initialize();
 		} else if (node instanceof TypeDeclaration) {
 			// Construct output file
 			// String filename = this.getCurrentScope().getName();
 			String filename = node.getIdentifier();
 			// filename = filename.replace('.', '/');
 			filename = "./output/" + filename + ".s";
 			logger.finer(filename);
 			this.asmFile = new File(filename);
 		} else if (node instanceof MethodDeclaration) {
 			Modifiers modifiers = ((MethodDeclaration) node).getModifiers();
 			if (!modifiers.containModifier(Modifier.NATIVE) && !modifiers.containModifier(Modifier.ABSTRACT)) {
 				// Define method labels
 				this.methodLabel = methodLabel(this.getCurrentScope().getName());
 				if (((MethodDeclaration) node).getName().getSimpleName().equals("test") && modifiers.containModifier(Modifier.STATIC)) {
 					this.methodLabel = "_start";
 					startFile = this.asmFile;
 				}
 
 				this.texts.add("global " + this.methodLabel);
 				this.texts.add(this.methodLabel + ":");
 
 				// Preamble
 				this.texts.add("push ebp\t\t\t; Preamble");
 				this.texts.add("mov ebp, esp");
 
 				// Allocate space for local variables
 				this.texts.add("sub esp, " + (((MethodDeclaration) node).totalLocalVariables * 4));
 
 				// Push registers
 				// this.texts.add("push eax"); // Leave eax as return value
 				this.texts.add("push ebx");
 				this.texts.add("push ecx");
 				this.texts.add("push edx");
 				this.texts.add("");
 				if (((MethodDeclaration) node).getName().getSimpleName().equals("test") && modifiers.containModifier(Modifier.STATIC)) {
 					this.texts.add("call Start_StaticInit");
 				}
 				if (node instanceof ConstructorDeclaration) {
 					// Call any superclass constructor
 					// TODO call super constructor...
 
 					// Get the class holding the constructor
 					ClassDeclaration cd = (ClassDeclaration) this.getCurrentScope().getParentTypeScope().getReferenceNode();
 					List<FieldDeclaration> fds = cd.getBody().getFields();
 
 					this.texts.add("push eax\t\t\t; Push the new object address");
 					this.texts.add("mov ebx, [ebp + 8]\t\t; Current Object");
 					this.texts.add("add ebx, 4\t\t\t; First space reserved");
 
 					// Initialize field variables here
 					for (FieldDeclaration fd : fds) {
 						if (fd.getInitial() != null) {
 							this.texts.add("push ebx\t\t\t; Push address of field");
 							fd.getInitial().accept(this);
 							this.texts.add("pop ebx\t\t\t; Pop LHS");
 							this.texts.add("mov [ebx], eax");
 						}
 						this.texts.add("add ebx, 4\t\t\t; ");
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public boolean visit(ASTNode node) throws Exception {
 		logger.finest("Visiting " + node);
 
 		if (node instanceof MethodInvokeExpression) {
 			this.generateMethodInvoke((MethodInvokeExpression) node);
 			return false;
 		} else if (node instanceof ClassCreateExpression) {
 			this.generateClassCreate((ClassCreateExpression) node);
 			return false;
 		} else if (node instanceof InfixExpression) {
 			this.generateInfixExpression((InfixExpression) node);
 			return false;
 		} else if (node instanceof ArrayCreate) {
 			this.generateArrayCreate((ArrayCreate) node);
 			return false;
 		} else if (node instanceof ArrayAccess) {
 			this.generateArrayAccess((ArrayAccess) node);
 			return false;
 		} else if (node instanceof ThisPrimary) {
 			this.texts.add("mov eax, [ebp + 8]\t; Current object");
 			return false;
 		} else if (node instanceof LiteralPrimary) {
 			this.generateLiteral((LiteralPrimary) node);
 			return false;
 		} else if (node instanceof UnaryExpression) {
 			this.generateUnaryExpression((UnaryExpression) node);
 			return false;
 		} else if (node instanceof LocalVariableDeclaration) {
 			this.generateLocalVariableDeclaration((LocalVariableDeclaration) node);
 			return false;
 		} else if (node instanceof AssignmentExpression) {
 			this.generateAssignmentExpression((AssignmentExpression) node);
 			return false;
 		} else if (node instanceof ForStatement) {
 			this.generateForLoop((ForStatement) node);
 			return false;
 		} else if (node instanceof WhileStatement) {
 			this.generateWhileStatement((WhileStatement) node);
 			return false;
 		} else if (node instanceof IfStatement) {
 			this.generateIfStatement((IfStatement) node);
 			return false;
 		} else if (node instanceof FieldDeclaration) {
 			if (((FieldDeclaration) node).getModifiers().containModifier(Modifier.STATIC)) {
 				this.generateStaticFieldDeclaration((FieldDeclaration) node);
 			}
 			return false;
 		} else if (node instanceof FieldAccess) {
 			this.generateFieldAccess((FieldAccess) node);
 			return false;
 		} else if (node instanceof MethodDeclaration) {
 			Block body = ((MethodDeclaration) node).getBody();
 			if (body != null) {
 				body.accept(this);
 			}
 			return false;
 		} else if (node instanceof Name) {
 			this.generateVariableAccess((Name) node);
 			return false;
 		}
 
 		return !this.complexNodes.contains(node.getClass());
 	}
 
 	@Override
 	public void didVisit(ASTNode node) throws Exception {
 		if (node instanceof FileUnit) {
 			// File content generated, write to file
 			File dir = this.asmFile.getParentFile();
 			for (String label : this.statics) {
 				staticInit.add("\t" + label + "_INIT\n");
 				staticInit.add("\tcall " + label + "_INIT" + '\n');
 			}
 			if (dir != null) {
 				dir.mkdirs();
 			}
 			this.asmFile.createNewFile();
 			BufferedWriter asmWriter = new BufferedWriter(new FileWriter(this.asmFile));
 			for (String line : this.externs) {
 				asmWriter.write("extern " + line);
 				asmWriter.newLine();
 			}
 
 			for (String line : this.texts) {
 				if (!line.startsWith("global") && !line.startsWith("section")) {
 					line = "\t" + line;
 					if (!line.endsWith(":")) {
 						line = "\t" + line;
 					}
 				}
 				asmWriter.write(line);
 				asmWriter.newLine();
 			}
 
 			for (String line : this.data) {
 				asmWriter.write(line);
 				asmWriter.newLine();
 			}
 			asmWriter.close();
 		} else if (node instanceof TypeDeclaration) {
 			this.texts.add("global " + this.getCurrentScope().getName() + "_VTABLE");
 			this.texts.add(this.getCurrentScope().getName() + "_VTABLE:");
 			this.texts.add("dd " + ((TypeDeclaration) node).getHierarchyTableIndex());
 
 			for (Entry<Integer, Scope> entry : ((TypeDeclaration) node).getSignatures().entrySet()) {
 				Scope methodScope = entry.getValue();
 
 				if (!this.getCurrentScope().getSymbols().containsKey(methodScope.getName())) {
 					this.externs.add(methodLabel(methodScope.getName()));
 				}
 				if (((MethodDeclaration) methodScope.getReferenceNode()).getModifiers().containModifier(Modifier.STATIC) && ((MethodDeclaration) methodScope.getReferenceNode()).getName().getName().equals("test")) {
 					this.externs.add("Start_StaticInit");
 					this.texts.add("dd _start");
 					startFile = asmFile;
 				} else
 					this.texts.add("dd " + methodLabel(methodScope.getName()));
 
 			}
 			this.texts.add("");
 		} else if (node instanceof MethodDeclaration) {
 			Modifiers modifiers = ((MethodDeclaration) node).getModifiers();
 			if (node instanceof ConstructorDeclaration) {
 				this.texts.add("pop eax\t\t\t; Restore THIS pointer to eax");
 			}
 			if (!modifiers.containModifier(Modifier.NATIVE) && !modifiers.containModifier(Modifier.ABSTRACT)) {
 				// Postamble
 				this.texts.add(this.methodLabel + "_END:");
 				// Pop registers
 				this.texts.add("pop edx\t\t\t\t; Postamble");
 				this.texts.add("pop ecx");
 				this.texts.add("pop ebx");
 				// this.texts.add("pop eax"); // Leave eax as return value
 
 				// Deallocate space for local variables
 				this.texts.add("add esp, " + (((MethodDeclaration) node).totalLocalVariables * 4));
 
 				// Restore frame pointer
 				this.texts.add("pop ebp");
 
 				if (this.methodLabel.equals("_start")) {
 					this.texts.add("call __debexit");
 				} else {
 					this.texts.add("ret");
 				}
 				this.texts.add("");
 			}
 		} else if (node instanceof ReturnStatement) {
 			this.texts.add("jmp " + this.methodLabel + "_END");
 		}
 		super.didVisit(node);
 	}
 
 	private static String methodLabel(String methodSignature) {
 		String label = methodSignature.replaceAll("[(),]", "_");
 		label = label.replaceAll("\\[\\]", "_ARRAY");
 		return label;
 	}
 
 	private static String staticLabel(String fieldName) {
 		String label = "STATIC" + fieldName;
 		return label;
 	}
 
 	private void generateVariableDereference(TableEntry entry) throws Exception {
 		VariableDeclaration varDecl = (VariableDeclaration) entry.getNode();
 		if (varDecl instanceof ParameterDeclaration) {
 			this.texts.add("mov eax, [ebp + " + (4 + varDecl.getIndex() * 4) + "]\t; Accessing parameter: " + entry.getName());
 		} else if (varDecl instanceof FieldDeclaration) {
 			if (varDecl.getModifiers().containModifier(Modifier.STATIC)) {
 				String label = staticLabel(entry.getName());
 				this.addExtern(label, entry.getName());
 				this.texts.add("mov eax, [" + label + "]\t; Accessing static: " + entry.getName());
 			} else {
 				this.texts.add("mov eax, [eax + " + (varDecl.getIndex() * 4) + "]\t; Accessing field: " + entry.getName());
 			}
 		} else if (varDecl instanceof LocalVariableDeclaration) {
 			this.texts.add("mov eax, [ebp - " + (varDecl.getIndex() * 4) + "]\t; Accessing local: " + entry.getName());
 		}
 	}
 
 	private void generateVariableAddress(TableEntry entry) throws Exception {
 		VariableDeclaration varDecl = (VariableDeclaration) entry.getNode();
 		if (varDecl instanceof ParameterDeclaration) {
 			this.texts.add("mov eax, ebp");
 			this.texts.add("add eax, " + (4 + varDecl.getIndex() * 4) + "\t\t\t; Address of parameter: " + entry.getName());
 		} else if (varDecl instanceof FieldDeclaration) {
 			if (varDecl.getModifiers().containModifier(Modifier.STATIC)) {
 				String label = staticLabel(entry.getName());
 				this.addExtern(label, entry.getName());
 				this.texts.add("mov eax, " + label + "\t; Address of static: " + entry.getName());
 			} else {
 				this.texts.add("add eax, " + (varDecl.getIndex() * 4) + "\t\t\t; Address of field: " + entry.getName());
 
 			}
 		} else if (varDecl instanceof LocalVariableDeclaration) {
 			this.texts.add("mov eax, ebp");
 			this.texts.add("sub eax, " + (varDecl.getIndex() * 4) + "\t\t\t; Address of local: " + entry.getName());
 		}
 	}
 
 	private void generateVariableAccess(Name name) throws Exception {
 		int i = 0;
 
 		if (name instanceof Name && this.referenceCurrentObject) {
 			this.texts.add("mov eax, [ebp + 8]\t; Current object");
 		}
 
 		if (name instanceof SimpleName) {
 			TableEntry entry = ((SimpleName) name).getOriginalDeclaration();
 			if (entry == null) {
 				String field = ((SimpleName) name).getName();
 				if (field.equals("length")) {
 					this.texts.add("mov eax, [eax + 4]\t\t; Fetch array length");
 				} else {
 					throw new Exception("Unknown field " + field);
 				}
 			} else if (this.dereferenceVariable) {
 				this.generateVariableDereference(entry);
 			} else {
 				this.generateVariableAddress(entry);
 			}
 		} else if (name instanceof QualifiedName) {
 			TableEntry entry = ((QualifiedName) name).getOriginalDeclaration();
 			if (entry.getNode() instanceof VariableDeclaration) {
 				this.generateVariableDereference(entry);
 			}
 
 			List<TableEntry> originalDeclarations = ((QualifiedName) name).originalDeclarations;
 			for (i = 0; i < originalDeclarations.size(); i++) {
 				entry = originalDeclarations.get(i);
 				if (i != originalDeclarations.size() - 1 || this.dereferenceVariable) {
 					this.generateVariableDereference(entry);
 				} else {
 					this.generateVariableAddress(entry);
 				}
 			}
 
 			List<String> components = ((QualifiedName) name).getComponents();
 			if (components.size() - originalDeclarations.size() > 1) {
 				String field = components.get(components.size() - 1);
 				if (field.equals("length")) {
 					this.texts.add("mov eax, [eax + 4]\t\t; Fetch array size");
 				} else {
 					throw new Exception("Unknown field " + field);
 				}
 			}
 		}
 	}
 
 	private void generateMethodInvoke(MethodInvokeExpression methodInvoke) throws Exception {
 
 		// Push parameters to stack
 		List<Expression> args = methodInvoke.getArguments();
 		int i = args.size();
 		for (i--; i >= 0; i--) {
 			Expression arg = args.get(i);
 			// Generate code for arg
 			arg.accept(this);
 			this.texts.add("push eax\t\t\t; Push parameter #" + (i + 1) + " to stack");
 		}
 
 		String methodName = methodInvoke.fullyQualifiedName;
 		String methodLabel = methodLabel(methodName);
 		if (methodLabel.equals("java.io.OutputStream.nativeWrite_INT__")) {
 			// Calling native write
 			methodLabel = "NATIVEjava.io.OutputStream.nativeWrite";
 			this.texts.add("pop eax\t\t\t\t; Pop parameter for native write");
 			this.texts.add("push ebx");
 			this.texts.add("push ecx");
 			this.texts.add("push edx");
 			this.texts.add("call " + methodLabel);
 			this.texts.add("pop edx");
 			this.texts.add("pop ecx");
 			this.texts.add("pop ebx");
 			this.texts.add("");
 			return;
 		}
 
 		// Push THIS to stack, THIS should be the address of the object
 		Primary primary = methodInvoke.getPrimary();
 		Name name = methodInvoke.getName();
 		if (primary != null) {
 			// If primary is not null, means is invoking method on a primary
 			primary.accept(this);
 		} else if (name instanceof QualifiedName) {
 			logger.finest("Generating method invoke for name " + name + " with #" + ((QualifiedName) name).originalDeclarations.size() + " entries");
 			this.texts.add("mov eax, [ebp + 8]\t; Current object");
 			List<TableEntry> originalDeclarations = ((QualifiedName) name).originalDeclarations;
 			for (TableEntry entry : originalDeclarations) {
 				this.generateVariableDereference(entry);
 			}
 		} else if (name instanceof SimpleName) {
 			// Invoking method within same Type, THIS is parameter #0
 			logger.finest("Generating method invoke for simple name " + name);
 			this.texts.add("mov eax, [ebp + 8]\t; Current object");
 		}
 		this.texts.add("push eax\t\t\t; Push THIS as parameter #0");
 
 		// Invoke the method
 		BlockScope methodBlock = this.table.getBlock(methodInvoke.fullyQualifiedName);
 		BodyDeclaration methodNode = (BodyDeclaration) methodBlock.getReferenceNode();
 
 		if (methodNode.getModifiers().containModifier(Modifier.STATIC)) {
 			this.texts.add("mov edx, " + methodBlock.getParentTypeScope().getName() + "_VTABLE");
 		} else {
 			this.texts.add("mov edx, [eax]\t; Dereference for the address of VTable");
 		}
 		this.texts.add("call [edx + " + (methodNode.getIndex() * 4 + 4) + "]\t; Call " + methodInvoke.fullyQualifiedName);
 
 		// Pop THIS from stack
 		this.texts.add("pop edx\t\t\t\t; Pop THIS");
 		// Pop parameters from stack
 		for (i = 0; i < args.size(); i++) {
 			this.texts.add("pop edx\t\t\t\t; Pop parameter #" + (i + 1) + " from stack");
 		}
 
 		// Add the vtable containing the method label
 		this.addVtable(methodBlock.getParentTypeScope().getName());
 		this.texts.add("");
 	}
 
 	private void generateClassCreate(ClassCreateExpression classCreate) throws Exception {
 		// Push parameters to stack
 		List<Expression> args = classCreate.getArguments();
 		int i = args.size();
 		for (i--; i >= 0; i--) {
 			Expression arg = args.get(i);
 			// Generate code for arg
 			arg.accept(this);
 			this.texts.add("push eax\t\t\t; Push parameter #" + i + " to stack");
 		}
 
 		// Allocate space for the new object
 		TypeScope typeScope = this.table.getType(classCreate.getType().getFullyQualifiedName());
 		TypeDeclaration typeDecl = (TypeDeclaration) typeScope.getReferenceNode();
 		this.texts.add("mov eax, " + (4 + typeDecl.totalFieldDeclarations * 4) + "\t\t\t; Size of the object");
 		this.texts.add("call __malloc");
 		this.texts.add("push eax\t\t\t; Push new object pointer as THIS");
 
 		this.addVtable(typeScope.getName());
 		this.texts.add("mov ebx, " + typeScope.getName() + "_VTABLE");
 		this.texts.add("mov [eax], ebx");
 
 		// Invoke the constructor
 		String constructorName = classCreate.fullyQualifiedName;
 		String constructorLabel = methodLabel(constructorName);
 		this.texts.add("call " + constructorLabel);
 
 		// Pop THIS from stack
 		this.texts.add("pop edx\t\t\t\t; Pop THIS");
 		// Pop parameters from stack
 		for (i = 0; i < args.size(); i++) {
 			this.texts.add("pop edx\t\t\t\t; Pop parameters #" + i + " from stack");
 		}
 
 		// Add to extern if is not local method
 		if (!this.getCurrentScope().getParentTypeScope().getSymbols().containsKey(constructorName)) {
 			this.externs.add(constructorLabel);
 		}
 		this.texts.add("");
 	}
 
 	private void generateValueToString(Type exprType) throws Exception {
 		if (exprType instanceof PrimitiveType) {
 			String valueOfLabel = "java.lang.String.valueOf_" + exprType.getFullyQualifiedName() + "__";
 			this.texts.add("push eax\t\t\t; Push the primitive as perameter #1");
 			this.texts.add("push eax\t\t\t; Push something as a fake THIS");
 			this.texts.add("call " + valueOfLabel);
 			this.addExtern(valueOfLabel, "java.lang.String.valueOf(" + exprType.getFullyQualifiedName() + ",)");
 			this.texts.add("pop edx");
 			this.texts.add("pop edx");
 		} else {
 			this.texts.add("push eax\t\t\t; Push the reference variable address as THIS");
 			this.texts.add("mov eax, [eax]\t; Obtain VTable address");
 			this.texts.add("add eax, 12\t\t; Shift to the index of toString");
 			this.texts.add("mov eax, [eax]\t; Dereference address of toString");
 			this.texts.add("call eax");
 			this.texts.add("pop edx");
 		}
 	}
 
 	private void generateStringConcat(Expression op1, Expression op2) throws Exception {
 		Type op1Type = op1.exprType;
 		Type op2Type = op2.exprType;
 
 		// op1 at EAX, op2 at EDX
 		this.texts.add("push edx\t\t\t; Push op2 to stack first");
 		this.generateValueToString(op1Type);
 		this.texts.add("pop edx\t\t\t\t; Pop op2");
 
 		// op1 String on stack, op2 at EDX
 		this.texts.add("push eax\t\t\t; Push op1 String to stack");
 		this.texts.add("mov eax, edx");
 		this.generateValueToString(op2Type);
 		this.texts.add("mov edx, eax");
 		this.texts.add("pop eax\t\t\t\t; Pop op1 String");
 
 		// op1 String at EAX, op2 String at EDX. Invoke
 		// java.lang.String.concat(java.lang.String) now.
 		this.texts.add("push edx");
 		this.texts.add("push eax");
 		this.texts.add("call java.lang.String.concat_java.lang.String__");
 		this.addExtern("java.lang.String.concat_java.lang.String__", "java.lang.String.concat(java.lang.String,)");
 		this.texts.add("pop edx");
 		this.texts.add("pop edx");
 	}
 
 	private void generateInfixExpression(InfixExpression infixExpr) throws Exception {
 		InfixOperator operator = infixExpr.getOperator();
 		List<Expression> operands = infixExpr.getOperands();
 		int comparisonCount = this.comparisonCount;
 		this.comparisonCount++;
 
 		// Instance of
 		if (operator.equals(InfixOperator.INSTANCEOF)) {
 			Expression operand = operands.get(0);
 			Type rhsType = infixExpr.getRHS();
 
 			operand.accept(this);
 			this.texts.add("mov eax,[eax]\t\t\t; get Vtable of current object");
 			this.texts.add("mov eax,[eax]\t\t\t; get the index of current object");
 			TypeScope typeScope = this.table.getType(rhsType.getFullyQualifiedName());
 			TypeDeclaration typeNode = (TypeDeclaration) typeScope.getReferenceNode();
 
 			this.texts.add("mov ebx," + typeNode.getHierarchyTableIndex() + "\t; get the index of RHS of instanceof");
 			this.texts.add("mov edx," + HierarchyChecker.getTotalClassNum() + "\t; get the fixed shift");
 			this.texts.add("mov ecx, SubtypeTable\t; get the subtypeTable");
 			this.texts.add("imul eax, edx\t; Multiply row with row width");
 			this.texts.add("add eax, ecx");
 			this.texts.add("add eax, ebx");
 			this.texts.add("mov eax, [eax]\t; get the subtype flag value");
 			return;
 		}
 
 		if (operator.equals(InfixOperator.AND) || operator.equals(InfixOperator.OR)) {
 			operands.get(0).accept(this);
 		} else {
 			// Generate code for the first operand and push to the stack
 			operands.get(0).accept(this);
 			this.texts.add("push eax\t\t\t; Push first operand value");
 
 			// Generate code for the second operand and save in edx
 			operands.get(1).accept(this);
 			this.texts.add("mov edx, eax\t\t; Move second operand result to edx");
 			this.texts.add("pop eax\t\t\t\t; Pop first operand value to eax");
 		}
 
 		switch (operator) {
 		case AND:
 			this.texts.add("cmp eax, " + BOOLEAN_FALSE);
 			this.texts.add("je " + "__COMPARISON_FALSE_" + comparisonCount + "\t; If Lazy AND reach false, skip second operands");
 			operands.get(1).accept(this);
 			this.texts.add("__COMPARISON_FALSE_" + comparisonCount + ":");
 			break;
 		case BAND:
 			this.texts.add("and eax, edx");
 			break;
 		case BOR:
 			this.texts.add("or eax, edx");
 			break;
 		case EQ:
 			this.texts.add("cmp eax, edx");
 			this.texts.add("je " + "__COMPARISON_TRUE_" + comparisonCount);
 			this.texts.add("mov eax, " + BOOLEAN_FALSE);
 			this.texts.add("jmp " + "__COMPARISON_FALSE_" + comparisonCount);
 			this.texts.add("__COMPARISON_TRUE_" + comparisonCount + ":");
 			this.texts.add("mov eax, " + BOOLEAN_TRUE);
 			this.texts.add("__COMPARISON_FALSE_" + comparisonCount + ":");
 			break;
 		case GEQ:
 			this.texts.add("cmp eax, edx");
 			this.texts.add("jge " + "__COMPARISON_TRUE_" + comparisonCount);
 			this.texts.add("mov eax, " + BOOLEAN_FALSE);
 			this.texts.add("jmp " + "__COMPARISON_FALSE_" + comparisonCount);
 			this.texts.add("__COMPARISON_TRUE_" + comparisonCount + ":");
 			this.texts.add("mov eax, " + BOOLEAN_TRUE);
 			this.texts.add("__COMPARISON_FALSE_" + comparisonCount + ":");
 			break;
 		case GT:
 			this.texts.add("cmp eax, edx");
 			this.texts.add("jg " + "__COMPARISON_TRUE_" + comparisonCount);
 			this.texts.add("mov eax, " + BOOLEAN_FALSE);
 			this.texts.add("jmp " + "__COMPARISON_FALSE_" + comparisonCount);
 			this.texts.add("__COMPARISON_TRUE_" + comparisonCount + ":");
 			this.texts.add("mov eax, " + BOOLEAN_TRUE);
 			this.texts.add("__COMPARISON_FALSE_" + comparisonCount + ":");
 			break;
 		case LEQ:
 			this.texts.add("cmp eax, edx");
 			this.texts.add("jle " + "__COMPARISON_TRUE_" + comparisonCount);
 			this.texts.add("mov eax, " + BOOLEAN_FALSE);
 			this.texts.add("jmp " + "__COMPARISON_FALSE_" + comparisonCount);
 			this.texts.add("__COMPARISON_TRUE_" + comparisonCount + ":");
 			this.texts.add("mov eax, " + BOOLEAN_TRUE);
 			this.texts.add("__COMPARISON_FALSE_" + comparisonCount + ":");
 			break;
 		case LT:
 			this.texts.add("cmp eax, edx");
 			this.texts.add("jl " + "__COMPARISON_TRUE_" + comparisonCount);
 			this.texts.add("mov eax, " + BOOLEAN_FALSE);
 			this.texts.add("jmp " + "__COMPARISON_FALSE_" + comparisonCount);
 			this.texts.add("__COMPARISON_TRUE_" + comparisonCount + ":");
 			this.texts.add("mov eax, " + BOOLEAN_TRUE);
 			this.texts.add("__COMPARISON_FALSE_" + comparisonCount + ":");
 			break;
 		case MINUS:
 			// eax = first operand - second operand
 			this.texts.add("sub eax, edx");
 			break;
 		case NEQ:
 			this.texts.add("cmp eax, edx");
 			this.texts.add("jne " + "__COMPARISON_TRUE_" + comparisonCount);
 			this.texts.add("mov eax, " + BOOLEAN_FALSE);
 			this.texts.add("jmp " + "__COMPARISON_FALSE_" + comparisonCount);
 			this.texts.add("__COMPARISON_TRUE_" + comparisonCount + ":");
 			this.texts.add("mov eax, " + BOOLEAN_TRUE);
 			this.texts.add("__COMPARISON_FALSE_" + comparisonCount + ":");
 			break;
 		case OR:
 			this.texts.add("cmp eax, " + BOOLEAN_FALSE);
 			this.texts.add("jne " + "__COMPARISON_TRUE_" + comparisonCount + "\t; If Lazy OR reach true, skip second operands");
 			operands.get(1).accept(this);
 			this.texts.add("__COMPARISON_TRUE_" + comparisonCount + ":");
 			break;
 		case PERCENT:
 			// eax = first operand % second operand
 			this.texts.add("cmp edx, 0\t\t\t; Check zero divider");
 			this.texts.add("je __exception\t\t; Throw exception");
 			this.texts.add("mov ebx, 0");
 			this.texts.add("xchg edx, ebx\t\t; Set edx to 0, and ebx to be the divider");
 			this.texts.add("idiv ebx\t\t\t; Divide edx:eax with ebx");
 			this.texts.add("mov eax, edx\t\t; Move the remainder to eax");
 			break;
 		case PLUS:
 			// eax = first operand + second operand
 			if (operands.get(0).exprType.getFullyQualifiedName().equals("java.lang.String") || operands.get(1).exprType.getFullyQualifiedName().equals("java.lang.String")) {
 				this.generateStringConcat(operands.get(0), operands.get(1));
 			} else {
 				this.texts.add("add eax, edx");
 			}
 			break;
 		case SLASH:
 			// eax = first operand / second operand
 			this.texts.add("cmp edx, 0\t\t\t; Check zero divider");
 			this.texts.add("je __exception\t\t; Throw exception");
 			this.texts.add("cmp eax, 0");
 			this.texts.add("jl __DIVISION_LT_ZERO_" + comparisonCount);
 			this.texts.add("mov ebx, 0");
 			this.texts.add("jmp __DIVISION_GE_ZERO_" + comparisonCount);
 			this.texts.add("__DIVISION_LT_ZERO_" + comparisonCount + ":");
 			this.texts.add("mov ebx, 0xffffffff");
 			this.texts.add("__DIVISION_GE_ZERO_" + comparisonCount + ":");
 			this.texts.add("xchg edx, ebx\t\t; Set edx to 0, and ebx to be the divider");
 			this.texts.add("idiv ebx\t\t\t; Divide edx:eax with ebx, quotient will be in eax");
 			break;
 		case STAR:
 			// eax = first operand * second operand
 			this.texts.add("imul eax, edx");
 			break;
 		default:
 			throw new Exception("Unkown infix operator type " + operator);
 		}
 		this.texts.add("");
 	}
 
 	private void generateArrayCreate(ArrayCreate arrayCreate) throws Exception {
 		arrayCreate.getDimension().accept(this);
 		this.addVtable("java.lang.Object");
 		this.texts.add("push eax\t\t\t; Push array dimension to stack");
 		this.texts.add("imul eax, 4\t\t\t; Multiply the array dimension by byte size");
 		this.texts.add("add eax, 8\t\t\t; Extra space to store Vtable and array length");
 		this.texts.add("call __malloc\t\t; Malloc space for the array");
 		this.texts.add("mov ebx, java.lang.Object_VTABLE");
 		this.texts.add("mov [eax], ebx\t; Save Object VTable");
 		this.texts.add("pop ebx\t\t\t\t; Pop array dimension");
 		this.texts.add("mov [eax + 4], ebx\t\t; Save array dimension");
 
 		if (arrayCreate.getType().getType() instanceof ReferenceType) {
 			this.addExtern("__NULL_LIT_", "");
 			this.texts.add("push eax\t\t\t\t; Save the array address");
 			this.texts.add("add eax, 8");
 			this.texts.add("mov ecx, ebx\t\t; Loop through array dimension");
 			this.texts.add("mov edx, __NULL_LIT_");
 			this.texts.add("__ARRAY_INIT_LOOP_" + this.loopCount + ":");
 			this.texts.add("mov [eax], edx");
 			this.texts.add("add eax, 4");
 			this.texts.add("loop __ARRAY_INIT_LOOP_" + this.loopCount);
 			this.texts.add("pop eax\t\t\t; Restore the array address");
 			this.loopCount++;
 		}
 	}
 
 	private void generateArrayAccess(ArrayAccess arrayAccess) throws Exception {
 		boolean originDereferenceSetting = this.dereferenceVariable;
 
 		this.dereferenceVariable = true;
 		arrayAccess.getExpression().accept(this);
 		this.texts.add("push eax\t\t\t; Push array address to stack first");
 
 		arrayAccess.getIndex().accept(this);
 		this.texts.add("pop edx");
 		this.texts.add("add edx, 8\t\t\t; Shift for array length");
 
 		this.texts.add("imul eax, 4\t\t\t; Multiply the array index by byte size");
 		this.texts.add("add edx, eax\t\t; Shift to index eax");
 
 		this.dereferenceVariable = originDereferenceSetting;
 		if (this.dereferenceVariable) {
 			this.texts.add("mov eax, [edx]\t\t; Dereference the array element");
 		} else {
 			this.texts.add("mov eax, edx\t\t; Address of the array element");
 		}
 	}
 
 	private void generateLiteral(LiteralPrimary literal) throws Exception {
 		char c = '\0';
 		int i = 0;
 		String value = literal.getValue();
 		switch (literal.getLiteralType()) {
 		case BOOLLIT:
 			if (value.equals("true")) {
 				this.texts.add("mov eax, " + BOOLEAN_TRUE);
 			} else {
 				this.texts.add("mov eax, " + BOOLEAN_FALSE);
 			}
 			break;
 		case CHARLIT:
 			c = value.charAt(1);
 			if (c == '\\' && value.length() > 3) {
 				c = value.charAt(2);
 				if (c == 'b')
 					c = '\b';
 				else if (c == 't')
 					c = '\t';
 				else if (c == 'n')
 					c = '\n';
 				else if (c == 'f')
 					c = '\f';
 				else if (c == 'r')
 					c = '\r';
 				else if (c == '"')
 					c = '"';
 				else if (c == '\'')
 					c = '\'';
 				else if (c == '\\')
 					c = '\\';
 				else
 					c = '\0';
 			}
 			this.texts.add("mov eax, " + ((int) c));
 			break;
 		case INTLIT:
 			// Assuming int literal within interger range
 			this.texts.add("mov eax, " + Integer.valueOf(value));
 			break;
 		case NULL:
 			this.texts.add("mov eax, __NULL_LIT_");
 			this.addExtern("__NULL_LIT_", "");
 			break;
 		case STRINGLIT:
 			this.addVtable("java.lang.String");
 			this.addVtable("java.lang.Object");
 			this.data.add("__STRING_" + this.literalCount + " dd java.lang.String_VTABLE");
 			this.data.add("dd " + "__STRING_LIT_" + this.literalCount);
 			this.data.add("__STRING_LIT_" + this.literalCount + " dd java.lang.Object_VTABLE");
 			this.data.add("dd " + (value.length() - 2));
 			for (i = 1; i < value.length() - 1; i++) {
 				this.data.add("dd " + ((int) value.charAt(i)));
 			}
 			this.texts.add("mov eax, " + "__STRING_" + this.literalCount);
 			this.literalCount++;
 			break;
 		default:
 			break;
 		}
 	}
 
 	private void generateUnaryExpression(UnaryExpression unaryExpr) throws Exception {
 		Expression operand = unaryExpr.getOperand();
 		switch (unaryExpr.getOperator()) {
 		case MINUS:
 			if (operand instanceof LiteralPrimary) {
 				// Assuming is int literal
 				if (((LiteralPrimary) operand).getLiteralType().equals(LiteralType.INTLIT)) {
 					this.texts.add("mov eax, " + Integer.parseInt("-" + ((LiteralPrimary) operand).getValue()));
 				}
 
 				if (((LiteralPrimary) operand).getLiteralType().equals(LiteralType.CHARLIT)) {
 
 					int charNumber = (int) ((LiteralPrimary) operand).getValue().charAt(1);
 					this.texts.add("mov eax, " + Integer.parseInt("-" + charNumber));
 				}
 
 			} else {
 				operand.accept(this);
 				this.texts.add("neg eax");
 			}
 			break;
 		case NOT:
 			operand.accept(this);
 			this.texts.add("not eax");
 			break;
 		default:
 			break;
 		}
 	}
 
 	private void generateLocalVariableDeclaration(LocalVariableDeclaration decl) throws Exception {
 		Expression initialization = decl.getInitial();
 		if (initialization != null) {
 			this.dereferenceVariable = false;
 			decl.getName().accept(this);
 			this.dereferenceVariable = true;
 			this.texts.add("push eax\t\t\t; Push LHS to stack");
 			initialization.accept(this);
 			this.texts.add("pop ebx");
 			this.texts.add("mov [ebx], eax");
 			this.texts.add("");
 		}
 	}
 
 	private void generateAssignmentExpression(AssignmentExpression assignExpr) throws Exception {
 		this.dereferenceVariable = false;
 		((ASTNode) assignExpr.getLeftHand()).accept(this);
 		this.dereferenceVariable = true;
 		this.texts.add("push eax\t\t\t; Push LHS to stack");
 		assignExpr.getExpression().accept(this);
 		this.texts.add("pop ebx");
 		this.texts.add("mov [ebx], eax");
 		this.texts.add("");
 	}
 
 	private void generateForLoop(ForStatement forStatement) throws Exception {
 		Integer loopCount = this.loopCount++;
 		// Init
 		this.texts.add("__LOOP_INIT_" + loopCount + ":");
 		((ASTNode) forStatement.getForInit()).accept(this);
 
 		this.texts.add("__LOOP_CONDITION_" + loopCount + ":");
 		forStatement.getForCondition().accept(this);
 		this.texts.add("cmp eax, " + BOOLEAN_FALSE);
 		this.texts.add("je __LOOP_END_" + loopCount);
 
 		this.texts.add("__LOOP_STATEMENT_" + loopCount + ":");
 		forStatement.getForStatement().accept(this);
 
 		this.texts.add("__LOOP_UPDATE_" + loopCount + ":");
 		forStatement.getForUpdate().accept(this);
 		this.texts.add("jmp __LOOP_CONDITION_" + loopCount);
 
 		this.texts.add("__LOOP_END_" + loopCount + ":");
 	}
 
 	private void generateWhileStatement(WhileStatement whileStatement) throws ChildTypeUnmatchException, Exception {
 		Integer loopCount = this.loopCount++;
 		this.texts.add("__LOOP_CONDITION_" + loopCount + ":");
 		whileStatement.getWhileCondition().accept(this);
 
 		this.texts.add("cmp eax, " + BOOLEAN_FALSE);
 		this.texts.add("je __LOOP_END_" + loopCount);
 
 		this.texts.add("__LOOP_STATEMENT_" + loopCount + ":");
 		whileStatement.getWhileStatement().accept(this);
 		this.texts.add("jmp __LOOP_CONDITION_" + loopCount);
 
 		this.texts.add("__LOOP_END_" + loopCount + ":");
 	}
 
 	private void generateIfStatement(IfStatement ifStatement) throws ChildTypeUnmatchException, Exception {
 		Integer conditionCount = this.conditionCount++;
 		this.texts.add("__IF_CONDITION_" + conditionCount + ":");
 		ifStatement.getIfCondition().accept(this);
 
 		this.texts.add("cmp eax, " + BOOLEAN_FALSE);
 		this.texts.add("je __ELSE_STATEMENT_" + conditionCount);
 
 		this.texts.add("__IF_STATEMENT_" + conditionCount + ":");
 		ifStatement.getIfStatement().accept(this);
 		this.texts.add("jmp __IF_END_" + conditionCount);
 
 		this.texts.add("__ELSE_STATEMENT_" + conditionCount + ":");
 		if (ifStatement.getElseStatement() != null) {
 			ifStatement.getElseStatement().accept(this);
 		}
 		this.texts.add("__IF_END_" + conditionCount + ":");
 	}
 
 	private void generateFieldAccess(FieldAccess fieldAccess) throws Exception {
 		fieldAccess.getPrimary().accept(this);
 		this.referenceCurrentObject = false;
 		fieldAccess.getName().accept(this);
 		this.referenceCurrentObject = true;
 	}
 
 	private void generateStaticFieldDeclaration(FieldDeclaration decl) throws Exception {
 		// TODO Move below
 		TableEntry entry = this.getCurrentScope().getParentTypeScope().getFieldDecl((FieldDeclaration) decl);
 		String label = null;
 		label = staticLabel(entry.getName());
 		this.texts.add("global " + label + "_INIT");
 		this.texts.add(label + "_INIT:");
 		this.data.add("global " + label);
 		this.data.add(label + ": dd 0x0");
 		// Place the node into a list...
 		statics.add(label);
 		Expression initialization = decl.getInitial();
 		if (initialization != null) {
 			this.dereferenceVariable = false;
 			decl.getName().accept(this);
 			this.dereferenceVariable = true;
 			this.texts.add("push eax\t\t\t; Push LHS to stack");
 			initialization.accept(this);
 			this.texts.add("pop ebx");
 			this.texts.add("mov [ebx], eax");
 		}
 		this.texts.add("ret");
 	}
 
 	public void writeStaticInit() throws Exception {
 		this.texts = new ArrayList<String>();
 		this.externs = new HashSet<String>();
 		asmFile = new File("./output/staticInit.s");
 
 		this.asmFile.createNewFile();
 		this.texts.add("global Start_StaticInit\n");
 		this.texts.add("Start_StaticInit:\n");
 		for (int i = 0; i < staticInit.size(); i = i + 2) {
 			this.externs.add(staticInit.get(i));
 			this.texts.add(staticInit.get(i + 1));
 		}
 		BufferedWriter asmWriter = new BufferedWriter(new FileWriter(this.asmFile));
 		for (String line : this.externs) {
 			asmWriter.write("extern " + line);
 
 		}
 		asmWriter.newLine();
 		for (String line : this.texts) {
 			if (!line.startsWith("global") && !line.startsWith("section")) {
 				line = "\t" + line;
 				if (!line.endsWith(":")) {
 					line = "\t" + line;
 				}
 			}
 			asmWriter.write(line);
 
 		}
 		asmWriter.newLine();
 		asmWriter.write("ret\n");
 		asmWriter.close();
 
 	}
 
 	public void generateSubtypeTable() throws Exception {
 		LinkedHashMap<TypeDeclaration, Stack<TypeScope>> classHierachyChain = HierarchyChecker.getLinkedClassHierachyChain();
 		// System.out.println(HierarchyChecker.getLinkedClassHierachyChain());
 		asmFile = new File("./output/subtypeTable.s");
 		this.asmFile.createNewFile();
 		BufferedWriter asmWriter = new BufferedWriter(new FileWriter(this.asmFile));
 		this.texts = new ArrayList<String>();
 		this.texts.add("global SubtypeTable\n");
 		this.texts.add("SubtypeTable:\n");
 
 		int classNum = HierarchyChecker.getTotalClassNum();
 		boolean[] subtypeArr = new boolean[classNum * classNum];
 		int i = 0;
 		for (Entry<TypeDeclaration, Stack<TypeScope>> entry : classHierachyChain.entrySet()) {
 			// System.out.println(entry+"entry:");
 			// entry.
 			Stack<TypeScope> classScopes = entry.getValue();
 
 			while (!classScopes.empty()) {
 				TypeScope classScope = classScopes.pop();
 				if (classScope.getReferenceNode() instanceof TypeDeclaration) {
 					int index = ((TypeDeclaration) classScope.getReferenceNode()).getHierarchyTableIndex();
 					subtypeArr[i * classNum + index] = true;
 				}
 
 			}
 			i++;
 
 		}
 		for (i = 0; i < subtypeArr.length; i++) {
 			if (subtypeArr[i]) {
 				this.texts.add("dd 0xffffffff");
 			} else {
 				this.texts.add("dd 0x0");
 			}
 
 		}
 		for (String line : this.texts) {
 
 			asmWriter.write(line);
 			asmWriter.newLine();
 
 		}
 		asmWriter.close();
 	}
 
 	public void copyNullAsm() throws Exception {
 		File nullAsm = new File("resources/null.s");
 		File outputAsm = new File("output/null.s");
 		Files.copy(nullAsm, outputAsm);
 	}
 }
