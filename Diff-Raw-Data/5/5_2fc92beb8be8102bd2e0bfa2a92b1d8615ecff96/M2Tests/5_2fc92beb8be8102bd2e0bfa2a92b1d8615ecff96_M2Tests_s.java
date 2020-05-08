 package swp_compiler_ss13.fuc.semantic_analyser;
 
 import static org.junit.Assert.assertFalse;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import swp_compiler_ss13.common.ast.AST;
 import swp_compiler_ss13.common.ast.nodes.binary.ArithmeticBinaryExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.binary.AssignmentNode;
 import swp_compiler_ss13.common.ast.nodes.binary.LogicBinaryExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.binary.BinaryExpressionNode.BinaryOperator;
 import swp_compiler_ss13.common.ast.nodes.leaf.BasicIdentifierNode;
 import swp_compiler_ss13.common.ast.nodes.leaf.LiteralNode;
 import swp_compiler_ss13.common.ast.nodes.marynary.BlockNode;
 import swp_compiler_ss13.common.ast.nodes.ternary.BranchNode;
 import swp_compiler_ss13.common.ast.nodes.unary.DeclarationNode;
 import swp_compiler_ss13.common.ast.nodes.unary.LogicUnaryExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.unary.PrintNode;
 import swp_compiler_ss13.common.ast.nodes.unary.ReturnNode;
 import swp_compiler_ss13.common.ast.nodes.unary.UnaryExpressionNode.UnaryOperator;
 import swp_compiler_ss13.common.parser.SymbolTable;
 import swp_compiler_ss13.common.types.primitive.BooleanType;
 import swp_compiler_ss13.common.types.primitive.LongType;
 import swp_compiler_ss13.common.types.primitive.StringType;
 import swp_compiler_ss13.fuc.ast.ASTImpl;
 import swp_compiler_ss13.fuc.ast.ArithmeticBinaryExpressionNodeImpl;
 import swp_compiler_ss13.fuc.ast.AssignmentNodeImpl;
 import swp_compiler_ss13.fuc.ast.BasicIdentifierNodeImpl;
 import swp_compiler_ss13.fuc.ast.BlockNodeImpl;
 import swp_compiler_ss13.fuc.ast.BranchNodeImpl;
 import swp_compiler_ss13.fuc.ast.DeclarationNodeImpl;
 import swp_compiler_ss13.fuc.ast.LiteralNodeImpl;
 import swp_compiler_ss13.fuc.ast.LogicBinaryExpressionNodeImpl;
 import swp_compiler_ss13.fuc.ast.LogicUnaryExpressionNodeImpl;
 import swp_compiler_ss13.fuc.ast.PrintNodeImpl;
 import swp_compiler_ss13.fuc.ast.ReturnNodeImpl;
 import swp_compiler_ss13.fuc.parser.errorHandling.ReportLogImpl;
 import swp_compiler_ss13.fuc.symbolTable.SymbolTableImpl;
 
 // TODO refine ASTs
 public class M2Tests {
 
 	private SemanticAnalyser analyser;
 	private ReportLogImpl log;
 	
 	public M2Tests() {
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
 	 * # returns 10<br/>
 	 * # prints nothing<br/>
 	 * long a;<br/>
 	 * long b;<br/>
 	 * long c;<br/>
 	 * <br/>
 	 * a = 4;<br/>
 	 * b = 3;<br/>
 	 * c = 2;<br/>
 	 * <br/>
 	 * a = b = 4;<br/>
 	 * c = a + b + c;<br/>
 	 * <br/>
 	 * return c;
 	 */
 	@Test
 	public void testAssignmentProg() {
 		SymbolTable symbolTable = new SymbolTableImpl();
 		symbolTable.insert("a", new LongType());
 		symbolTable.insert("b", new LongType());
 		symbolTable.insert("c", new LongType());
 		
 		DeclarationNode declaration_a = new DeclarationNodeImpl();
 		declaration_a.setIdentifier("a");
 		DeclarationNode declaration_b = new DeclarationNodeImpl();
 		declaration_b.setIdentifier("b");
 		DeclarationNode declaration_c = new DeclarationNodeImpl();
 		declaration_c.setIdentifier("c");
 		
 		BasicIdentifierNode identifier_a = new BasicIdentifierNodeImpl();
 		identifier_a.setIdentifier("a");
 		BasicIdentifierNode identifier_b = new BasicIdentifierNodeImpl();
 		identifier_b.setIdentifier("b");
 		BasicIdentifierNode identifier_c = new BasicIdentifierNodeImpl();
 		identifier_c.setIdentifier("c");
 		
 		LiteralNode literal_4 = new LiteralNodeImpl();
 		literal_4.setLiteral("4");
 		literal_4.setLiteralType(new LongType());
 		LiteralNode literal_3 = new LiteralNodeImpl();
 		literal_3.setLiteral("3");
 		literal_3.setLiteralType(new LongType());
 		LiteralNode literal_2 = new LiteralNodeImpl();
 		literal_2.setLiteral("2");
 		literal_2.setLiteralType(new LongType());
 		
 		AssignmentNode assignment_a1 = new AssignmentNodeImpl();
 		assignment_a1.setLeftValue(identifier_a);
 		assignment_a1.setRightValue(literal_4);
 		AssignmentNode assignment_b1 = new AssignmentNodeImpl();
 		assignment_b1.setLeftValue(identifier_b);
 		assignment_b1.setRightValue(literal_3);
 		AssignmentNode assignment_c1 = new AssignmentNodeImpl();
 		assignment_c1.setLeftValue(identifier_c);
 		assignment_c1.setRightValue(literal_2);
 		
 		ArithmeticBinaryExpressionNode add2 = new ArithmeticBinaryExpressionNodeImpl();
 		add2.setOperator(BinaryOperator.ADDITION);
 		add2.setLeftValue(identifier_a);
 		add2.setRightValue(identifier_b);
 		ArithmeticBinaryExpressionNode add1 = new ArithmeticBinaryExpressionNodeImpl();
 		add1.setOperator(BinaryOperator.ADDITION);
 		add1.setLeftValue(add2);
 		add1.setRightValue(identifier_c);
 		
 		AssignmentNode assignment_b2 = new AssignmentNodeImpl();
 		assignment_b2.setLeftValue(identifier_b);
 		assignment_b2.setRightValue(literal_4);
 		AssignmentNode assignment_a2 = new AssignmentNodeImpl();
 		assignment_a2.setLeftValue(identifier_a);
 		assignment_a2.setRightValue(assignment_b2);
 		AssignmentNode assignment_c2 = new AssignmentNodeImpl();
 		assignment_c2.setLeftValue(identifier_c);
 		assignment_c2.setRightValue(literal_2);
 		
 		ReturnNode returnNode = new ReturnNodeImpl();
 		returnNode.setRightValue(identifier_c);
 		
 		BlockNode blockNode = new BlockNodeImpl();
 		blockNode.addDeclaration(declaration_a);
 		blockNode.addDeclaration(declaration_b);
 		blockNode.addDeclaration(declaration_c);
 		blockNode.addStatement(assignment_a1);
 		blockNode.addStatement(assignment_b1);
 		blockNode.addStatement(assignment_c1);
 		blockNode.addStatement(assignment_a2);
 		blockNode.addStatement(assignment_c2);
 		blockNode.addStatement(returnNode);
 		blockNode.setSymbolTable(symbolTable);
 		
 		AST ast = new ASTImpl();
 		ast.setRootNode(blockNode);
 		
 		analyser.analyse(ast);
 		
 		assertFalse(log.hasErrors());
 	}
 	
 	/**
 	 * # return 5<br/>
 	 * # prints nothing<br/>
 	 * <br/>
 	 * bool b;<br/>
 	 * bool c;<br/>
 	 * long l;<br/>
 	 * <br/>
 	 * b = true;<br/>
 	 * c = false;<br/>
 	 * <br/>
 	 * l = 4;<br/>
 	 * <br/>
 	 * # dangling-else should be resolved as given by indentation<br/>
 	 * <br/>
 	 * if ( b )<br/>
 	 *   if ( c || ! b )<br/>
 	 *     print "bla";<br/>
 	 *   else<br/>
 	 *     l = 5;<br/>
 	 * <br/>
 	 * return l;
 	 */
 	@Test
 	public void testCondProg() {
 		SymbolTable symbolTable = new SymbolTableImpl();
 		symbolTable.insert("b", new BooleanType());
 		symbolTable.insert("c", new BooleanType());
 		symbolTable.insert("l", new LongType());
 		// TODO undo handling bla as identifier or change associated code
 		symbolTable.insert("bla", new StringType(new Long(8)));
 		
 		DeclarationNode declaration_b = new DeclarationNodeImpl();
 		declaration_b.setIdentifier("b");
 		DeclarationNode declaration_c = new DeclarationNodeImpl();
 		declaration_c.setIdentifier("c");
 		DeclarationNode declaration_l = new DeclarationNodeImpl();
 		declaration_l.setIdentifier("l");
 		DeclarationNode declaration_bla = new DeclarationNodeImpl();
 		declaration_bla.setIdentifier("bla");
 		
 		BasicIdentifierNode identifier_b = new BasicIdentifierNodeImpl();
 		identifier_b.setIdentifier("b");
 		BasicIdentifierNode identifier_c = new BasicIdentifierNodeImpl();
 		identifier_c.setIdentifier("c");
 		BasicIdentifierNode identifier_l = new BasicIdentifierNodeImpl();
 		identifier_l.setIdentifier("l");
 		BasicIdentifierNode identifier_bla = new BasicIdentifierNodeImpl();
 		identifier_bla.setIdentifier("bla");
 		
 		LiteralNode literal_4 = new LiteralNodeImpl();
 		literal_4.setLiteral("4");
 		literal_4.setLiteralType(new LongType());
 		LiteralNode literal_5 = new LiteralNodeImpl();
 		literal_5.setLiteral("5");
 		literal_5.setLiteralType(new LongType());
 		LiteralNode literal_bla = new LiteralNodeImpl();
 		literal_bla.setLiteral("bla");
 		literal_bla.setLiteralType(new StringType(new Long(8)));
 		LiteralNode literal_true = new LiteralNodeImpl();
 		literal_true.setLiteral("true");
 		literal_true.setLiteralType(new BooleanType());
 		LiteralNode literal_false = new LiteralNodeImpl();
 		literal_false.setLiteral("false");
 		literal_false.setLiteralType(new BooleanType());
 		
 		AssignmentNode assignment_b = new AssignmentNodeImpl();
 		assignment_b.setLeftValue(identifier_b);
 		assignment_b.setRightValue(literal_true);
 		AssignmentNode assignment_c = new AssignmentNodeImpl();
 		assignment_c.setLeftValue(identifier_c);
 		assignment_c.setRightValue(literal_false);
 		AssignmentNode assignment_l1 = new AssignmentNodeImpl();
 		assignment_l1.setLeftValue(identifier_l);
 		assignment_l1.setRightValue(literal_4);
 		AssignmentNode assignment_bla = new AssignmentNodeImpl();
 		assignment_bla.setLeftValue(identifier_bla);
 		assignment_bla.setRightValue(literal_bla);
 		
 		LogicUnaryExpressionNode not_b = new LogicUnaryExpressionNodeImpl();
 		not_b.setOperator(UnaryOperator.LOGICAL_NEGATE);
 		LogicBinaryExpressionNode c_or_not_b = new LogicBinaryExpressionNodeImpl();
 		c_or_not_b.setOperator(BinaryOperator.LOGICAL_OR);
 		c_or_not_b.setLeftValue(identifier_c);
 		c_or_not_b.setRightValue(not_b);
 		
 		PrintNode print = new PrintNodeImpl();
 		print.setRightValue(identifier_bla);
 		AssignmentNode assignment_l2 = new AssignmentNodeImpl();
 		assignment_l2.setLeftValue(identifier_l);
 		assignment_l2.setRightValue(literal_5);
 		BranchNode innerBranch = new BranchNodeImpl();
 		innerBranch.setCondition(c_or_not_b);
 		innerBranch.setStatementNodeOnTrue(print);
 		innerBranch.setStatementNodeOnFalse(assignment_l2);
 		
 		BranchNode outerBranch = new BranchNodeImpl();
 		outerBranch.setCondition(identifier_b);
 		outerBranch.setStatementNodeOnTrue(innerBranch);
 		
 		ReturnNode returnNode = new ReturnNodeImpl();
 		returnNode.setRightValue(identifier_l);
 		
 		BlockNode blockNode = new BlockNodeImpl();
 		blockNode.addDeclaration(declaration_b);
 		blockNode.addDeclaration(declaration_c);
 		blockNode.addDeclaration(declaration_l);
 		blockNode.addDeclaration(declaration_bla);
 		blockNode.addStatement(assignment_b);
 		blockNode.addStatement(assignment_c);
 		blockNode.addStatement(assignment_l1);
 		blockNode.addStatement(assignment_bla);
 		blockNode.addStatement(outerBranch);
 		blockNode.addStatement(returnNode);
 		blockNode.setSymbolTable(symbolTable);
 		
 		AST ast = new ASTImpl();
 		ast.setRootNode(blockNode);
 		
 		analyser.analyse(ast);
 		
 		assertFalse(log.hasErrors());
 	}
 
 	/**
 	 * # return 0<br/>
 	 * # prints:<br/>
 	 * # true<br/>
 	 * # 18121313223<br/>
 	 * # -2.323e-99<br/>
	 * # jagrEttString"<br/>
 	 * <br/>
 	 * long l;<br/>
 	 * double d;<br/>
 	 * string s;<br/>
 	 * bool b;<br/>
 	 * <br/>
 	 * b = true;<br/>
 	 * l = 18121313223;<br/>
 	 * d = -23.23e-100;<br/>
	 * s = "jagrEttString\"\n"; # c-like escaping in strings<br/>
 	 * <br/>
 	 * print b; print "\n";<br/>
 	 * print l; print "\n"; # print one digit left of the radix point<br/>
 	 * print d; print "\n";<br/>
 	 * print s;<br/>
 	 * <br/>
 	 * return; # equivalent to return EXIT_SUCCESS
 	 */
 	@Test
 	public void testPrintProg(){
 		// TODO implement me!
 	}
 }
