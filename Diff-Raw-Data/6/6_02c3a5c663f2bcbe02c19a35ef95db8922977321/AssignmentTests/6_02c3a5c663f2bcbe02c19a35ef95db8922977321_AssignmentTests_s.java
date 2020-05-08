 package swp_compiler_ss13.fuc.semantic_analyser;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import swp_compiler_ss13.common.ast.AST;
 import swp_compiler_ss13.common.ast.nodes.binary.AssignmentNode;
 import swp_compiler_ss13.common.ast.nodes.leaf.BasicIdentifierNode;
 import swp_compiler_ss13.common.ast.nodes.leaf.LiteralNode;
 import swp_compiler_ss13.common.ast.nodes.marynary.BlockNode;
 import swp_compiler_ss13.common.ast.nodes.ternary.BranchNode;
 import swp_compiler_ss13.common.ast.nodes.unary.DeclarationNode;
 import swp_compiler_ss13.common.parser.SymbolTable;
 import swp_compiler_ss13.common.types.primitive.BooleanType;
 import swp_compiler_ss13.common.types.primitive.LongType;
 import swp_compiler_ss13.fuc.ast.ASTImpl;
 import swp_compiler_ss13.fuc.ast.AssignmentNodeImpl;
 import swp_compiler_ss13.fuc.ast.BasicIdentifierNodeImpl;
 import swp_compiler_ss13.fuc.ast.BlockNodeImpl;
 import swp_compiler_ss13.fuc.ast.BranchNodeImpl;
 import swp_compiler_ss13.fuc.ast.DeclarationNodeImpl;
 import swp_compiler_ss13.fuc.ast.LiteralNodeImpl;
 import swp_compiler_ss13.fuc.errorLog.ReportLogImpl;
 import swp_compiler_ss13.fuc.symbolTable.SymbolTableImpl;
 
 public class AssignmentTests {
 
 	private SemanticAnalyser analyser;
 	private ReportLogImpl log;
 
 	public AssignmentTests() {
 
 	}
 
 	@Before
 	public void setUp() {
 		log = new ReportLogImpl();
 		analyser = new SemanticAnalyser(this.log);
 	}
 
 	@After
 	public void tearDown() {
 		analyser = null;
 		log = null;
 	}
 
 	/**
 	 * # error: assignment of boolean to long-identifier<br/>
 	 * long l;<br/>
 	 * l = true;<br/>
 	 */
 	@Test
 	public void testSimpleTypeError() {
 		// long l;
 		DeclarationNode declaration_l = new DeclarationNodeImpl();
 		declaration_l.setIdentifier("l");
 		declaration_l.setType(new LongType());
 
 		// l = true;
 		BasicIdentifierNode identifier_l = new BasicIdentifierNodeImpl();
 		identifier_l.setIdentifier("l");
 		LiteralNode literal_true = new LiteralNodeImpl();
 		literal_true.setLiteral("true");
 		literal_true.setLiteralType(new BooleanType());
 
 		AssignmentNode assignment_l = new AssignmentNodeImpl();
 		assignment_l.setLeftValue(identifier_l);
 		assignment_l.setRightValue(literal_true);
 		identifier_l.setParentNode(assignment_l);
 		literal_true.setParentNode(assignment_l);
 
 		// main block
 		SymbolTable symbolTable = new SymbolTableImpl();
 		symbolTable.insert("l", new LongType());
 
 		BlockNode blockNode = new BlockNodeImpl();
 		blockNode.addDeclaration(declaration_l);
 		blockNode.addStatement(assignment_l);
 		blockNode.setSymbolTable(symbolTable);
 		declaration_l.setParentNode(blockNode);
 		assignment_l.setParentNode(blockNode);
 
 		// analyse AST
 		AST ast = new ASTImpl();
 		ast.setRootNode(blockNode);
 
 		analyser.analyse(ast);
 
 		// TODO better error check
 		assertEquals(log.getErrors().size(), 1);
 	}
 
 	/**
 	 * # no errors expected<br/>
 	 * long l1;<br/>
 	 * long l2;<br/>
 	 * l1 = l2 = 1;<br/>
 	 */
 	@Test
 	public void testAssignmentInAssignment() {
 		// long l1; long l2;
 		DeclarationNode declaration_l1 = new DeclarationNodeImpl();
 		declaration_l1.setIdentifier("l1");
 		declaration_l1.setType(new LongType());
 		DeclarationNode declaration_l2 = new DeclarationNodeImpl();
 		declaration_l2.setIdentifier("l1");
 		declaration_l2.setType(new LongType());
 
 		// l1 = l2 = 1;
 		BasicIdentifierNode identifier_l1 = new BasicIdentifierNodeImpl();
 		identifier_l1.setIdentifier("l1");
 		BasicIdentifierNode identifier_l2 = new BasicIdentifierNodeImpl();
 		identifier_l2.setIdentifier("l2");
 		LiteralNode literal_1 = new LiteralNodeImpl();
 		literal_1.setLiteral("1");
 		literal_1.setLiteralType(new LongType());
 
 		AssignmentNode assignment_l2 = new AssignmentNodeImpl();
 		assignment_l2.setLeftValue(identifier_l2);
 		assignment_l2.setRightValue(literal_1);
 		identifier_l2.setParentNode(assignment_l2);
 		literal_1.setParentNode(assignment_l2);
 
 		AssignmentNode assignment_l1 = new AssignmentNodeImpl();
 		assignment_l1.setLeftValue(identifier_l1);
 		assignment_l1.setRightValue(assignment_l2);
 		identifier_l1.setParentNode(assignment_l1);
 		assignment_l2.setParentNode(assignment_l1);
 
 		// main block
 		SymbolTable symbolTable = new SymbolTableImpl();
 		symbolTable.insert("l1", new LongType());
 		symbolTable.insert("l2", new LongType());
 
 		BlockNode blockNode = new BlockNodeImpl();
 		blockNode.addDeclaration(declaration_l1);
 		blockNode.addDeclaration(declaration_l2);
 		blockNode.addStatement(assignment_l1);
 		blockNode.setSymbolTable(symbolTable);
 		declaration_l1.setParentNode(blockNode);
 		declaration_l2.setParentNode(blockNode);
 		assignment_l1.setParentNode(blockNode);
 
 		// analyse AST
 		AST ast = new ASTImpl();
 		ast.setRootNode(blockNode);
 
 		analyser.analyse(ast);
 
 		assertFalse(log.hasErrors());
 	}
 
 	/**
 	 * # error: assignment of long-assignment to bool<br/>
 	 * long l;<br/>
 	 * long b;<br/>
 	 * b = l = 1;<br/>
 	 */
 	@Test
 	public void testAssignmentInAssignmentTypeError() {
 		// long l1; long l2;
 		DeclarationNode declaration_l = new DeclarationNodeImpl();
 		declaration_l.setIdentifier("l");
 		declaration_l.setType(new LongType());
 		DeclarationNode declaration_b = new DeclarationNodeImpl();
 		declaration_b.setIdentifier("b");
 		declaration_b.setType(new BooleanType());
 
 		// b = l = 1;
 		BasicIdentifierNode identifier_l = new BasicIdentifierNodeImpl();
 		identifier_l.setIdentifier("l");
 		BasicIdentifierNode identifier_b = new BasicIdentifierNodeImpl();
 		identifier_b.setIdentifier("b");
 		LiteralNode literal_1 = new LiteralNodeImpl();
 		literal_1.setLiteral("1");
 		literal_1.setLiteralType(new LongType());
 
 		AssignmentNode assignment_l = new AssignmentNodeImpl();
 		assignment_l.setLeftValue(identifier_l);
 		assignment_l.setRightValue(literal_1);
 		identifier_l.setParentNode(assignment_l);
 		literal_1.setParentNode(assignment_l);
 
 		AssignmentNode assignment_b = new AssignmentNodeImpl();
 		assignment_b.setLeftValue(identifier_b);
 		assignment_b.setRightValue(assignment_l);
 		identifier_b.setParentNode(assignment_b);
 		assignment_l.setParentNode(assignment_b);
 
 		// main block
 		SymbolTable symbolTable = new SymbolTableImpl();
 		symbolTable.insert("l", new LongType());
 		symbolTable.insert("b", new BooleanType());
 
 		BlockNode blockNode = new BlockNodeImpl();
 		blockNode.addDeclaration(declaration_l);
 		blockNode.addDeclaration(declaration_b);
 		blockNode.addStatement(assignment_b);
 		blockNode.setSymbolTable(symbolTable);
 		declaration_l.setParentNode(blockNode);
 		declaration_b.setParentNode(blockNode);
 		assignment_b.setParentNode(blockNode);
 
 		// analyse AST
 		AST ast = new ASTImpl();
 		ast.setRootNode(blockNode);
 
 		analyser.analyse(ast);
 
 		// TODO better error check
 		System.out.println(log);
 		assertEquals(log.getErrors().size(), 1);
 	}
 
 	/**
 	 * # error: assignment of wrong type inside a new block<br/>
 	 * bool b;<br/>
 	 * <br/>
 	 * if (true){<br/>
 	 * b = 1;<br/>
 	 * }
 	 */
 	@Test
 	public void testInnerBlockTypeError() {
 		SymbolTable symbolTable = new SymbolTableImpl();
 		symbolTable.insert("b", new BooleanType());
 
 		// bool b;
 		DeclarationNode declaration_b = new DeclarationNodeImpl();
 		declaration_b.setIdentifier("b");
 		declaration_b.setType(new BooleanType());
 
 		// b = 1;
 		BasicIdentifierNode identifier_b = new BasicIdentifierNodeImpl();
 		identifier_b.setIdentifier("b");
 		LiteralNode literal_1 = new LiteralNodeImpl();
 		literal_1.setLiteral("1");
 		literal_1.setLiteralType(new LongType());
 
 		AssignmentNode assignment_b = new AssignmentNodeImpl();
 		assignment_b.setLeftValue(identifier_b);
 		assignment_b.setRightValue(literal_1);
 		identifier_b.setParentNode(assignment_b);
 		literal_1.setParentNode(assignment_b);
 
 		// if (true) {...}
 		LiteralNode literal_true = new LiteralNodeImpl();
 		literal_true.setLiteral("true");
 		literal_true.setLiteralType(new BooleanType());
 
 		SymbolTable branchBlockTable = new SymbolTableImpl(symbolTable);
 		BlockNode branchBlock = new BlockNodeImpl();
 		branchBlock.addStatement(assignment_b);
 		branchBlock.setSymbolTable(branchBlockTable);
 		assignment_b.setParentNode(branchBlock);
 
 		BranchNode branch = new BranchNodeImpl();
 		branch.setCondition(literal_true);
 		branch.setStatementNodeOnTrue(branchBlock);
 		literal_true.setParentNode(branch);
 		branchBlock.setParentNode(branch);
 
 		// main block
 		BlockNode mainBlock = new BlockNodeImpl();
 		mainBlock.addDeclaration(declaration_b);
 		mainBlock.addStatement(branch);
 		mainBlock.setSymbolTable(symbolTable);
 		declaration_b.setParentNode(mainBlock);
 		branch.setParentNode(mainBlock);
 
 		// analyse AST
 		AST ast = new ASTImpl();
 		ast.setRootNode(mainBlock);
 
 		analyser.analyse(ast);
 
 		// TODO better error check
 		System.out.println(log);
 		assertEquals(log.getErrors().size(), 1);
 	}
 
 	/**
 	 * # no errors expected<br/>
 	 * bool b;<br/>
 	 * long l;<br/>
 	 * <br/>
 	 * if (true){<br/>
 	 * long b;<br/>
 	 * bool l;<br/>
 	 * b = 1;<br/>
 	 * }<br/>
 	 * l = 1;
 	 */
 	@Test
 	public void testInnerBlockDeclaration() {
 		SymbolTable symbolTable = new SymbolTableImpl();
 		symbolTable.insert("b", new BooleanType());
 		symbolTable.insert("l", new LongType());
 
 		// bool b; long l;
 		DeclarationNode declaration_b1 = new DeclarationNodeImpl();
 		declaration_b1.setIdentifier("b");
 		declaration_b1.setType(new BooleanType());
 		DeclarationNode declaration_l1 = new DeclarationNodeImpl();
 		declaration_l1.setIdentifier("l");
 		declaration_l1.setType(new LongType());
 
 		// long b; bool l;
 		DeclarationNode declaration_b2 = new DeclarationNodeImpl();
 		declaration_b2.setIdentifier("b");
 		declaration_b2.setType(new LongType());
 		DeclarationNode declaration_l2 = new DeclarationNodeImpl();
 		declaration_l2.setIdentifier("l");
 		declaration_l2.setType(new BooleanType());
 
 		// b = 1;
 		BasicIdentifierNode identifier_b = new BasicIdentifierNodeImpl();
 		identifier_b.setIdentifier("b");
 		LiteralNode literal_1_1 = new LiteralNodeImpl();
 		literal_1_1.setLiteral("1");
 		literal_1_1.setLiteralType(new LongType());
 
 		AssignmentNode assignment_b = new AssignmentNodeImpl();
 		assignment_b.setLeftValue(identifier_b);
 		assignment_b.setRightValue(literal_1_1);
 		identifier_b.setParentNode(assignment_b);
 		literal_1_1.setParentNode(assignment_b);
 
 		// l = 1;
 		BasicIdentifierNode identifier_l = new BasicIdentifierNodeImpl();
		identifier_l.setIdentifier("b");
 		LiteralNode literal_1_2 = new LiteralNodeImpl();
 		literal_1_2.setLiteral("1");
 		literal_1_2.setLiteralType(new LongType());
 
 		AssignmentNode assignment_l = new AssignmentNodeImpl();
 		assignment_l.setLeftValue(identifier_l);
 		assignment_l.setRightValue(literal_1_2);
 		identifier_l.setParentNode(assignment_l);
 		literal_1_2.setParentNode(assignment_l);
 
 		// if (true) {...}
 		LiteralNode literal_true = new LiteralNodeImpl();
 		literal_true.setLiteral("true");
 		literal_true.setLiteralType(new BooleanType());
 
 		SymbolTable branchBlockTable = new SymbolTableImpl(symbolTable);
 		BlockNode branchBlock = new BlockNodeImpl();
 		branchBlock.addDeclaration(declaration_b2);
 		branchBlock.addDeclaration(declaration_l2);
 		branchBlock.addStatement(assignment_b);
 		branchBlock.setSymbolTable(branchBlockTable);
 		declaration_b2.setParentNode(branchBlock);
 		declaration_l2.setParentNode(branchBlock);
 		assignment_b.setParentNode(branchBlock);
 
 		BranchNode branch = new BranchNodeImpl();
 		branch.setCondition(literal_true);
 		branch.setStatementNodeOnTrue(branchBlock);
 		literal_true.setParentNode(branch);
 		branchBlock.setParentNode(branch);
 
 		// main block
 		BlockNode mainBlock = new BlockNodeImpl();
 		mainBlock.addDeclaration(declaration_b1);
 		mainBlock.addDeclaration(declaration_l1);
 		mainBlock.addStatement(branch);
 		mainBlock.addStatement(assignment_l);
 		mainBlock.setSymbolTable(symbolTable);
 		declaration_b1.setParentNode(mainBlock);
 		declaration_b1.setParentNode(mainBlock);
 		declaration_l1.setParentNode(mainBlock);
 		branch.setParentNode(mainBlock);
 		assignment_l.setParentNode(mainBlock);
 
 		// analyse AST
 		AST ast = new ASTImpl();
 		ast.setRootNode(mainBlock);
 
 		analyser.analyse(ast);
 
 		assertFalse(log.hasErrors());
 	}
 
 }
