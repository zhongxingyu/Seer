 package swp_compiler_ss13.fuc.ir.test;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.List;
 
 import junit.extensions.PA;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import swp_compiler_ss13.common.ast.AST;
 import swp_compiler_ss13.common.ast.nodes.binary.BinaryExpressionNode.BinaryOperator;
 import swp_compiler_ss13.common.backend.Quadruple;
 import swp_compiler_ss13.common.ir.IntermediateCodeGeneratorException;
 import swp_compiler_ss13.common.types.derived.ArrayType;
 import swp_compiler_ss13.common.types.primitive.BooleanType;
 import swp_compiler_ss13.common.types.primitive.DoubleType;
 import swp_compiler_ss13.common.types.primitive.LongType;
 import swp_compiler_ss13.common.types.primitive.StringType;
 import swp_compiler_ss13.fuc.ast.ASTFactory;
 import swp_compiler_ss13.fuc.ir.IntermediateCodeGeneratorImpl;
 import swp_compiler_ss13.fuc.symbolTable.SymbolTableImpl;
 
 public class ArrayTest {
 
 	private IntermediateCodeGeneratorImpl irgen;
 	private AST ast_declare_one_dimensional;
 	private AST ast_declare_multi_dimensional;
 	private AST ast_print_and_return;
 	private AST ast_print_computed_index;
 	private AST ast_assign_array_one_dim_to_var;
 	private AST ast_assign_array_two_dim_to_var;
 	private AST ast_assign_var_to_one_dim_array;
 	private AST ast_assign_var_to_two_dim_array;
 	private AST ast_assign_array_to_array_one_dim;
 	private AST ast_assign_array_to_array_two_dim;
 	private AST ast_assign_array_trigger_cast;
 
 	@Before
 	public void setUp() throws Exception {
 		// reset symbol table variable counter
 		PA.setValue(SymbolTableImpl.class, "ext", 0);
 
 		this.irgen = new IntermediateCodeGeneratorImpl();
 		ASTFactory f;
 
 		f = new ASTFactory();
 		f.addDeclaration("a1", new ArrayType(new LongType(), 5));
 		f.addDeclaration("a2", new ArrayType(new DoubleType(), 6));
 		f.addDeclaration("a3", new ArrayType(new BooleanType(), 7));
 		f.addDeclaration("a4", new ArrayType(new StringType(6L), 8));
 		this.ast_declare_one_dimensional = f.getAST();
 
 		f = new ASTFactory();
 		f.addDeclaration("a1", new ArrayType(new ArrayType(new LongType(), 5), 3));
 		f.addDeclaration("a2", new ArrayType(new ArrayType(new DoubleType(), 10), 15));
 		f.addDeclaration("a3", new ArrayType(new ArrayType(new ArrayType(new StringType(1L), 1), 2), 3));
 		this.ast_declare_multi_dimensional = f.getAST();
 
 		f = new ASTFactory();
 		f.addDeclaration("a", new ArrayType(new LongType(), 5));
 		f.addDeclaration("b", new ArrayType(new ArrayType(new DoubleType(), 7), 9));
 		f.addPrint(f.newArrayIdentifier(f.newLiteral("1", new LongType()), f.newBasicIdentifier("a")));
 		f.addPrint(f.newArrayIdentifier(f.newLiteral("1", new LongType()),
 				f.newArrayIdentifier(f.newLiteral("2", new LongType()), f.newBasicIdentifier("b"))));
 		f.addReturn(f.newArrayIdentifier(f.newLiteral("1", new LongType()), f.newBasicIdentifier("a")));
 		f.addReturn(f.newArrayIdentifier(f.newLiteral("1", new LongType()),
 				f.newArrayIdentifier(f.newLiteral("2", new LongType()), f.newBasicIdentifier("b"))));
 		this.ast_print_and_return = f.getAST();
 
 		f = new ASTFactory();
 		f.addDeclaration("a", new ArrayType(new LongType(), 5));
 		f.addDeclaration("b", new ArrayType(new ArrayType(new DoubleType(), 7), 9));
 		f.addDeclaration("i", new LongType());
 		f.addAssignment(f.newBasicIdentifier("i"), f.newLiteral("3", new LongType()));
 		f.addPrint(f.newArrayIdentifier(
 				f.newBinaryExpression(BinaryOperator.ADDITION, f.newBasicIdentifier("i"),
 						f.newLiteral("1", new LongType())), f.newBasicIdentifier("a")));
 		f.addPrint(f.newArrayIdentifier(
 				f.newBinaryExpression(BinaryOperator.SUBSTRACTION, f.newBasicIdentifier("i"),
 						f.newLiteral("2", new LongType())),
 				f.newArrayIdentifier(f.newBinaryExpression(BinaryOperator.ADDITION, f.newBasicIdentifier("i"),
 						f.newLiteral("2", new LongType())), f.newBasicIdentifier("b"))));
 		this.ast_print_computed_index = f.getAST();
 
 		f = new ASTFactory();
 		f.addDeclaration("a", new ArrayType(new LongType(), 10));
 		f.addDeclaration("i", new LongType());
 		f.addDeclaration("target", new LongType());
 
 		f.addAssignment(f.newBasicIdentifier("target"),
 				f.newArrayIdentifier(f.newBasicIdentifier("i"), f.newBasicIdentifier("a")));
 		f.addAssignment(f.newBasicIdentifier("target"),
 				f.newArrayIdentifier(f.newBinaryExpression(BinaryOperator.ADDITION, f.newBasicIdentifier("i"),
 						f.newLiteral("1", new LongType())), f.newBasicIdentifier("a")));
 		this.ast_assign_array_one_dim_to_var = f.getAST();
 
 		f = new ASTFactory();
 		f.addDeclaration("a", new ArrayType(new ArrayType(new LongType(), 10), 20));
 		f.addDeclaration("i", new LongType());
 		f.addDeclaration("target", new LongType());
 
 		f.addAssignment(
 				f.newBasicIdentifier("target"),
 				f.newArrayIdentifier(f.newBasicIdentifier("i"),
 						f.newArrayIdentifier(f.newLiteral("2", new LongType()), f.newBasicIdentifier("a"))));
 
 		f.addAssignment(
 				f.newBasicIdentifier("target"),
 				f.newArrayIdentifier(f.newBinaryExpression(BinaryOperator.SUBSTRACTION, f.newBasicIdentifier("i"),
 						f.newLiteral("8", new LongType())),
 						f.newArrayIdentifier(f.newLiteral("2", new LongType()), f.newBasicIdentifier("a"))));
 		this.ast_assign_array_two_dim_to_var = f.getAST();
 
 		f = new ASTFactory();
 		f.addDeclaration("source", new StringType(1L));
 		f.addDeclaration("target", new ArrayType(new StringType(1L), 42));
 		f.addDeclaration("i", new LongType());
 
 		f.addAssignment(f.newArrayIdentifier(f.newBasicIdentifier("i"), f.newBasicIdentifier("target")),
 				f.newBasicIdentifier("source"));
 
 		f.addAssignment(
 				f.newArrayIdentifier(
 						f.newBinaryExpression(BinaryOperator.ADDITION, f.newBasicIdentifier("i"),
 								f.newLiteral("4", new LongType())), f.newBasicIdentifier("target")),
 				f.newBasicIdentifier("source"));
 
 		f.addAssignment(
 				f.newArrayIdentifier(
 						f.newArrayIdentifier(f.newBasicIdentifier("i"), f.newBasicIdentifier("target")),
 						f.newBasicIdentifier("target")),
 				f.newBasicIdentifier("source"));
 
 		this.ast_assign_var_to_one_dim_array = f.getAST();
 
 		f = new ASTFactory();
 		f.addDeclaration("source", new LongType());
 		f.addDeclaration("target", new ArrayType(new ArrayType(new LongType(), 5), 4));
 		f.addDeclaration("i", new LongType());
 
 		f.addAssignment(
 				f.newArrayIdentifier(f.newBasicIdentifier("i"),
 						f.newArrayIdentifier(f.newLiteral("4", new LongType()), f.newBasicIdentifier("target"))),
 				f.newBasicIdentifier("source"));
 
 		f.addAssignment(
 				f.newArrayIdentifier(
 						f.newBinaryExpression(BinaryOperator.ADDITION, f.newBasicIdentifier("i"),
 								f.newLiteral("2", new LongType())),
 						f.newArrayIdentifier(
 								f.newBinaryExpression(BinaryOperator.SUBSTRACTION, f.newBasicIdentifier("i"),
 										f.newLiteral("1", new LongType())), f.newBasicIdentifier("target"))),
 				f.newBasicIdentifier("source"));
 
 		this.ast_assign_var_to_two_dim_array = f.getAST();
 
 		f = new ASTFactory();
 		f.addDeclaration("source", new ArrayType(new LongType(), 6));
 		f.addDeclaration("target", new ArrayType(new LongType(), 7));
 		f.addDeclaration("i", new LongType());
 		f.addAssignment(
 				f.newArrayIdentifier(f.newLiteral("2", new LongType()), f.newBasicIdentifier("target")),
 				f.newArrayIdentifier(f.newLiteral("3", new LongType()), f.newBasicIdentifier("source")));
 
 		f.addAssignment(
 				f.newArrayIdentifier(f.newBinaryExpression(BinaryOperator.ADDITION, f.newBasicIdentifier("i"),
 						f.newLiteral("2", new LongType())), f.newBasicIdentifier("target")),
 				f.newArrayIdentifier(f.newBinaryExpression(BinaryOperator.ADDITION, f.newBasicIdentifier("i"),
 						f.newLiteral("3", new LongType())), f.newBasicIdentifier("source")));
 		this.ast_assign_array_to_array_one_dim = f.getAST();
 
 		f = new ASTFactory();
 		f.addDeclaration("source", new ArrayType(new ArrayType(new LongType(), 6), 7));
 		f.addDeclaration("target", new ArrayType(new ArrayType(new LongType(), 7), 12));
 		f.addDeclaration("i", new LongType());
 		f.addAssignment(
 				f.newArrayIdentifier(f.newLiteral("2", new LongType()),
 						f.newArrayIdentifier(f.newLiteral("4", new LongType()), f.newBasicIdentifier("target"))),
 				f.newArrayIdentifier(f.newLiteral("3", new LongType()),
 						f.newArrayIdentifier(f.newLiteral("5", new LongType()), f.newBasicIdentifier("source"))));
 
 		f.addAssignment(
 				f.newArrayIdentifier(f.newBinaryExpression(BinaryOperator.ADDITION, f.newBasicIdentifier("i"),
 						f.newLiteral("2", new LongType())),
 						f.newArrayIdentifier(f.newLiteral("4", new LongType()), f.newBasicIdentifier("target"))),
 				f.newArrayIdentifier(f.newBinaryExpression(BinaryOperator.ADDITION, f.newBasicIdentifier("i"),
 						f.newLiteral("3", new LongType())),
 						f.newArrayIdentifier(f.newLiteral("7", new LongType()), f.newBasicIdentifier("source"))));
 		this.ast_assign_array_to_array_two_dim = f.getAST();
 
 		f = new ASTFactory();
 		f.addDeclaration("a1", new ArrayType(new LongType(), 10));
 		f.addDeclaration("a2", new ArrayType(new DoubleType(), 15));
 		f.addDeclaration("v1", new LongType());
 		f.addDeclaration("v2", new DoubleType());
 
 		f.addAssignment(f.newBasicIdentifier("v1"),
 				f.newArrayIdentifier(f.newLiteral("2", new LongType()),
 						f.newBasicIdentifier("a2")));
 		f.addAssignment(f.newBasicIdentifier("v2"),
 				f.newArrayIdentifier(f.newLiteral("2", new LongType()),
 						f.newBasicIdentifier("a1")));
 		f.addAssignment(f.newArrayIdentifier(f.newLiteral("2", new
 				LongType()), f.newBasicIdentifier("a2")),
 				f.newBasicIdentifier("v1"));
 		f.addAssignment(f.newArrayIdentifier(f.newLiteral("2", new
 				LongType()), f.newBasicIdentifier("a1")),
 				f.newBasicIdentifier("v2"));
 		f.addAssignment(f.newArrayIdentifier(f.newLiteral("2", new LongType()), f.newBasicIdentifier("a1")),
 				f.newArrayIdentifier(f.newLiteral("2", new LongType()), f.newBasicIdentifier("a2")));
 		f.addAssignment(f.newArrayIdentifier(f.newLiteral("2", new LongType()), f.newBasicIdentifier("a2")),
 				f.newArrayIdentifier(f.newLiteral("2", new LongType()), f.newBasicIdentifier("a1")));
 		this.ast_assign_array_trigger_cast = f.getAST();
 	}
 
 	private String getString(List<Quadruple> quadruples) {
 		StringBuilder builder = new StringBuilder();
 		for (Quadruple q : quadruples) {
 			builder.append(q).append("\n");
 		}
 		return builder.toString();
 	}
 
 	@Test
 	public void declareOneDimensional() throws IntermediateCodeGeneratorException {
 		List<Quadruple> q = this.irgen.generateIntermediateCode(this.ast_declare_one_dimensional);
 		String actual = this.getString(q);
 		String expected = "Quadruple: (DECLARE_ARRAY | #5 | ! | a1)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | !)\n" +
 				"Quadruple: (DECLARE_ARRAY | #6 | ! | a2)\n" +
 				"Quadruple: (DECLARE_DOUBLE | ! | ! | !)\n" +
 				"Quadruple: (DECLARE_ARRAY | #7 | ! | a3)\n" +
 				"Quadruple: (DECLARE_BOOLEAN | ! | ! | !)\n" +
 				"Quadruple: (DECLARE_ARRAY | #8 | ! | a4)\n" +
 				"Quadruple: (DECLARE_STRING | ! | ! | !)\n";
 		assertEquals(expected, actual);
 	}
 
 	@Test
 	public void declareMultiDimensional() throws IntermediateCodeGeneratorException {
 		List<Quadruple> q = this.irgen.generateIntermediateCode(this.ast_declare_multi_dimensional);
 		String actual = this.getString(q);
 		String expected = "Quadruple: (DECLARE_ARRAY | #3 | ! | a1)\n" +
 				"Quadruple: (DECLARE_ARRAY | #5 | ! | !)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | !)\n" +
 				"Quadruple: (DECLARE_ARRAY | #15 | ! | a2)\n" +
 				"Quadruple: (DECLARE_ARRAY | #10 | ! | !)\n" +
 				"Quadruple: (DECLARE_DOUBLE | ! | ! | !)\n" +
 				"Quadruple: (DECLARE_ARRAY | #3 | ! | a3)\n" +
 				"Quadruple: (DECLARE_ARRAY | #2 | ! | !)\n" +
 				"Quadruple: (DECLARE_ARRAY | #1 | ! | !)\n" +
 				"Quadruple: (DECLARE_STRING | ! | ! | !)\n";
 		assertEquals(expected, actual);
 	}
 
 	@Test
 	public void printAndReturn() throws IntermediateCodeGeneratorException {
 		List<Quadruple> q = this.irgen.generateIntermediateCode(this.ast_print_and_return);
 		String actual = this.getString(q);
 		String expected = "Quadruple: (DECLARE_ARRAY | #5 | ! | a)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | !)\n" +
 				"Quadruple: (DECLARE_ARRAY | #9 | ! | b)\n" +
 				"Quadruple: (DECLARE_ARRAY | #7 | ! | !)\n" +
 				"Quadruple: (DECLARE_DOUBLE | ! | ! | !)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | tmp0)\n" +
 				"Quadruple: (ARRAY_GET_LONG | a | #1 | tmp0)\n" +
 				"Quadruple: (DECLARE_STRING | ! | ! | tmp1)\n" +
 				"Quadruple: (LONG_TO_STRING | tmp0 | ! | tmp1)\n" +
 				"Quadruple: (PRINT_STRING | tmp1 | ! | !)\n" +
 				"Quadruple: (DECLARE_REFERENCE | ! | ! | tmp2)\n" +
 				"Quadruple: (ARRAY_GET_REFERENCE | b | #2 | tmp2)\n" +
 				"Quadruple: (DECLARE_DOUBLE | ! | ! | tmp3)\n" +
 				"Quadruple: (ARRAY_GET_DOUBLE | tmp2 | #1 | tmp3)\n" +
 				"Quadruple: (DECLARE_STRING | ! | ! | tmp4)\n" +
 				"Quadruple: (DOUBLE_TO_STRING | tmp3 | ! | tmp4)\n" +
 				"Quadruple: (PRINT_STRING | tmp4 | ! | !)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | tmp5)\n" +
 				"Quadruple: (ARRAY_GET_LONG | a | #1 | tmp5)\n" +
 				"Quadruple: (RETURN | tmp5 | ! | !)\n" +
 				"Quadruple: (DECLARE_REFERENCE | ! | ! | tmp6)\n" +
 				"Quadruple: (ARRAY_GET_REFERENCE | b | #2 | tmp6)\n" +
 				"Quadruple: (DECLARE_DOUBLE | ! | ! | tmp7)\n" +
 				"Quadruple: (ARRAY_GET_DOUBLE | tmp6 | #1 | tmp7)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | tmp8)\n" +
 				"Quadruple: (DOUBLE_TO_LONG | tmp7 | ! | tmp8)\n" +
 				"Quadruple: (RETURN | tmp8 | ! | !)\n";
 		assertEquals(expected, actual);
 	}
 
 	@Test
 	public void printComputedIndex() throws IntermediateCodeGeneratorException {
 		List<Quadruple> q = this.irgen.generateIntermediateCode(this.ast_print_computed_index);
 		String actual = this.getString(q);
 		String expected = "Quadruple: (DECLARE_ARRAY | #5 | ! | a)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | !)\n" +
 				"Quadruple: (DECLARE_ARRAY | #9 | ! | b)\n" +
 				"Quadruple: (DECLARE_ARRAY | #7 | ! | !)\n" +
 				"Quadruple: (DECLARE_DOUBLE | ! | ! | !)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | i)\n" +
 				"Quadruple: (ASSIGN_LONG | #3 | ! | i)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | tmp0)\n" +
 				"Quadruple: (ADD_LONG | i | #1 | tmp0)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | tmp1)\n" +
 				"Quadruple: (ARRAY_GET_LONG | a | tmp0 | tmp1)\n" +
 				"Quadruple: (DECLARE_STRING | ! | ! | tmp2)\n" +
 				"Quadruple: (LONG_TO_STRING | tmp1 | ! | tmp2)\n" +
 				"Quadruple: (PRINT_STRING | tmp2 | ! | !)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | tmp3)\n" +
 				"Quadruple: (SUB_LONG | i | #2 | tmp3)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | tmp4)\n" +
 				"Quadruple: (ADD_LONG | i | #2 | tmp4)\n" +
 				"Quadruple: (DECLARE_REFERENCE | ! | ! | tmp5)\n" +
 				"Quadruple: (ARRAY_GET_REFERENCE | b | tmp4 | tmp5)\n" +
 				"Quadruple: (DECLARE_DOUBLE | ! | ! | tmp6)\n" +
 				"Quadruple: (ARRAY_GET_DOUBLE | tmp5 | tmp3 | tmp6)\n" +
 				"Quadruple: (DECLARE_STRING | ! | ! | tmp7)\n" +
 				"Quadruple: (DOUBLE_TO_STRING | tmp6 | ! | tmp7)\n" +
 				"Quadruple: (PRINT_STRING | tmp7 | ! | !)\n";
 		assertEquals(expected, actual);
 	}
 
 	@Test
 	public void assignOneDimArrayToVariable() throws IntermediateCodeGeneratorException {
 		List<Quadruple> q = this.irgen.generateIntermediateCode(this.ast_assign_array_one_dim_to_var);
 		String actual = this.getString(q);
 		String expected = "Quadruple: (DECLARE_ARRAY | #10 | ! | a)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | !)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | i)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | target)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | tmp0)\n" +
 				"Quadruple: (ARRAY_GET_LONG | a | i | tmp0)\n" +
 				"Quadruple: (ASSIGN_LONG | tmp0 | ! | target)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | tmp1)\n" +
 				"Quadruple: (ADD_LONG | i | #1 | tmp1)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | tmp2)\n" +
 				"Quadruple: (ARRAY_GET_LONG | a | tmp1 | tmp2)\n" +
 				"Quadruple: (ASSIGN_LONG | tmp2 | ! | target)\n";
 		assertEquals(expected, actual);
 	}
 
 	@Test
 	public void assignTwoDimArrayToVariable() throws IntermediateCodeGeneratorException {
 		List<Quadruple> q = this.irgen.generateIntermediateCode(this.ast_assign_array_two_dim_to_var);
 		String actual = this.getString(q);
 		String expected = "Quadruple: (DECLARE_ARRAY | #20 | ! | a)\n" +
 				"Quadruple: (DECLARE_ARRAY | #10 | ! | !)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | !)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | i)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | target)\n" +
 				"Quadruple: (DECLARE_REFERENCE | ! | ! | tmp0)\n" +
 				"Quadruple: (ARRAY_GET_REFERENCE | a | #2 | tmp0)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | tmp1)\n" +
 				"Quadruple: (ARRAY_GET_LONG | tmp0 | i | tmp1)\n" +
 				"Quadruple: (ASSIGN_LONG | tmp1 | ! | target)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | tmp2)\n" +
 				"Quadruple: (SUB_LONG | i | #8 | tmp2)\n" +
 				"Quadruple: (DECLARE_REFERENCE | ! | ! | tmp3)\n" +
 				"Quadruple: (ARRAY_GET_REFERENCE | a | #2 | tmp3)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | tmp4)\n" +
 				"Quadruple: (ARRAY_GET_LONG | tmp3 | tmp2 | tmp4)\n" +
 				"Quadruple: (ASSIGN_LONG | tmp4 | ! | target)\n";
 		assertEquals(expected, actual);
 	}
 
 	@Test
 	public void assignVariableToOneDimArray() throws IntermediateCodeGeneratorException {
 		List<Quadruple> q = this.irgen.generateIntermediateCode(this.ast_assign_var_to_one_dim_array);
 		String actual = this.getString(q);
 		String expected = "Quadruple: (DECLARE_STRING | ! | ! | source)\n" +
 				"Quadruple: (DECLARE_ARRAY | #42 | ! | target)\n" +
 				"Quadruple: (DECLARE_STRING | ! | ! | !)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | i)\n" +
 				"Quadruple: (ARRAY_SET_STRING | target | i | source)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | tmp0)\n" +
 				"Quadruple: (ADD_LONG | i | #4 | tmp0)\n" +
 				"Quadruple: (ARRAY_SET_STRING | target | tmp0 | source)\n" +
				"Quadruple: (DECLARE_STRING | ! | ! | tmp1)\n" +
				"Quadruple: (ARRAY_GET_STRING | target | i | tmp1)\n" +
 				"Quadruple: (ARRAY_SET_STRING | target | tmp1 | source)\n";
 		assertEquals(expected, actual);
 	}
 
 	@Test
 	public void assignVariableToTwoDimArray() throws IntermediateCodeGeneratorException {
 		List<Quadruple> q = this.irgen.generateIntermediateCode(this.ast_assign_var_to_two_dim_array);
 		String actual = this.getString(q);
 		String expected = "Quadruple: (DECLARE_LONG | ! | ! | source)\n" +
 				"Quadruple: (DECLARE_ARRAY | #4 | ! | target)\n" +
 				"Quadruple: (DECLARE_ARRAY | #5 | ! | !)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | !)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | i)\n" +
 				"Quadruple: (DECLARE_REFERENCE | ! | ! | tmp0)\n" +
 				"Quadruple: (ARRAY_GET_REFERENCE | target | #4 | tmp0)\n" +
 				"Quadruple: (ARRAY_SET_LONG | tmp0 | i | source)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | tmp1)\n" +
 				"Quadruple: (ADD_LONG | i | #2 | tmp1)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | tmp2)\n" +
 				"Quadruple: (SUB_LONG | i | #1 | tmp2)\n" +
 				"Quadruple: (DECLARE_REFERENCE | ! | ! | tmp3)\n" +
 				"Quadruple: (ARRAY_GET_REFERENCE | target | tmp2 | tmp3)\n" +
 				"Quadruple: (ARRAY_SET_LONG | tmp3 | tmp1 | source)\n";
 		assertEquals(expected, actual);
 	}
 
 	@Test
 	public void assignArrayToArrayOneDim() throws IntermediateCodeGeneratorException {
 		List<Quadruple> q = this.irgen.generateIntermediateCode(this.ast_assign_array_to_array_one_dim);
 		String actual = this.getString(q);
 		String expected = "Quadruple: (DECLARE_ARRAY | #6 | ! | source)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | !)\n" +
 				"Quadruple: (DECLARE_ARRAY | #7 | ! | target)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | !)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | i)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | tmp0)\n" +
 				"Quadruple: (ARRAY_GET_LONG | source | #3 | tmp0)\n" +
 				"Quadruple: (ARRAY_SET_LONG | target | #2 | tmp0)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | tmp1)\n" +
 				"Quadruple: (ADD_LONG | i | #2 | tmp1)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | tmp2)\n" +
 				"Quadruple: (ADD_LONG | i | #3 | tmp2)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | tmp3)\n" +
 				"Quadruple: (ARRAY_GET_LONG | source | tmp2 | tmp3)\n" +
 				"Quadruple: (ARRAY_SET_LONG | target | tmp1 | tmp3)\n";
 		assertEquals(expected, actual);
 	}
 
 	@Test
 	public void assignArrayToArrayTwoDim() throws IntermediateCodeGeneratorException {
 		List<Quadruple> q = this.irgen.generateIntermediateCode(this.ast_assign_array_to_array_two_dim);
 		String actual = this.getString(q);
 		String expected = "Quadruple: (DECLARE_ARRAY | #7 | ! | source)\n" +
 				"Quadruple: (DECLARE_ARRAY | #6 | ! | !)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | !)\n" +
 				"Quadruple: (DECLARE_ARRAY | #12 | ! | target)\n" +
 				"Quadruple: (DECLARE_ARRAY | #7 | ! | !)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | !)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | i)\n" +
 				"Quadruple: (DECLARE_REFERENCE | ! | ! | tmp0)\n" +
 				"Quadruple: (ARRAY_GET_REFERENCE | target | #4 | tmp0)\n" +
 				"Quadruple: (DECLARE_REFERENCE | ! | ! | tmp1)\n" +
 				"Quadruple: (ARRAY_GET_REFERENCE | source | #5 | tmp1)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | tmp2)\n" +
 				"Quadruple: (ARRAY_GET_LONG | tmp1 | #3 | tmp2)\n" +
 				"Quadruple: (ARRAY_SET_LONG | tmp0 | #2 | tmp2)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | tmp3)\n" +
 				"Quadruple: (ADD_LONG | i | #2 | tmp3)\n" +
 				"Quadruple: (DECLARE_REFERENCE | ! | ! | tmp4)\n" +
 				"Quadruple: (ARRAY_GET_REFERENCE | target | #4 | tmp4)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | tmp5)\n" +
 				"Quadruple: (ADD_LONG | i | #3 | tmp5)\n" +
 				"Quadruple: (DECLARE_REFERENCE | ! | ! | tmp6)\n" +
 				"Quadruple: (ARRAY_GET_REFERENCE | source | #7 | tmp6)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | tmp7)\n" +
 				"Quadruple: (ARRAY_GET_LONG | tmp6 | tmp5 | tmp7)\n" +
 				"Quadruple: (ARRAY_SET_LONG | tmp4 | tmp3 | tmp7)\n";
 		assertEquals(expected, actual);
 	}
 
 	@Test
 	public void assignArrayTriggerCast() throws IntermediateCodeGeneratorException {
 		List<Quadruple> q = this.irgen.generateIntermediateCode(this.ast_assign_array_trigger_cast);
 		String actual = this.getString(q);
 		System.out.println(actual);
 		String expected = "Quadruple: (DECLARE_ARRAY | #10 | ! | a1)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | !)\n" +
 				"Quadruple: (DECLARE_ARRAY | #15 | ! | a2)\n" +
 				"Quadruple: (DECLARE_DOUBLE | ! | ! | !)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | v1)\n" +
 				"Quadruple: (DECLARE_DOUBLE | ! | ! | v2)\n" +
 				"Quadruple: (DECLARE_DOUBLE | ! | ! | tmp0)\n" +
 				"Quadruple: (ARRAY_GET_DOUBLE | a2 | #2 | tmp0)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | tmp1)\n" +
 				"Quadruple: (DOUBLE_TO_LONG | tmp0 | ! | tmp1)\n" +
 				"Quadruple: (ASSIGN_LONG | tmp1 | ! | v1)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | tmp2)\n" +
 				"Quadruple: (ARRAY_GET_LONG | a1 | #2 | tmp2)\n" +
 				"Quadruple: (DECLARE_DOUBLE | ! | ! | tmp3)\n" +
 				"Quadruple: (LONG_TO_DOUBLE | tmp2 | ! | tmp3)\n" +
 				"Quadruple: (ASSIGN_DOUBLE | tmp3 | ! | v2)\n" +
 				"Quadruple: (DECLARE_DOUBLE | ! | ! | tmp4)\n" +
 				"Quadruple: (LONG_TO_DOUBLE | v1 | ! | tmp4)\n" +
 				"Quadruple: (ARRAY_SET_DOUBLE | a2 | #2 | tmp4)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | tmp5)\n" +
 				"Quadruple: (DOUBLE_TO_LONG | v2 | ! | tmp5)\n" +
 				"Quadruple: (ARRAY_SET_LONG | a1 | #2 | tmp5)\n" +
 				"Quadruple: (DECLARE_DOUBLE | ! | ! | tmp6)\n" +
 				"Quadruple: (ARRAY_GET_DOUBLE | a2 | #2 | tmp6)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | tmp7)\n" +
 				"Quadruple: (DOUBLE_TO_LONG | tmp6 | ! | tmp7)\n" +
 				"Quadruple: (ARRAY_SET_LONG | a1 | #2 | tmp7)\n" +
 				"Quadruple: (DECLARE_LONG | ! | ! | tmp8)\n" +
 				"Quadruple: (ARRAY_GET_LONG | a1 | #2 | tmp8)\n" +
 				"Quadruple: (DECLARE_DOUBLE | ! | ! | tmp9)\n" +
 				"Quadruple: (LONG_TO_DOUBLE | tmp8 | ! | tmp9)\n" +
 				"Quadruple: (ARRAY_SET_DOUBLE | a2 | #2 | tmp9)\n";
 		assertEquals(expected, actual);
 	}
 }
