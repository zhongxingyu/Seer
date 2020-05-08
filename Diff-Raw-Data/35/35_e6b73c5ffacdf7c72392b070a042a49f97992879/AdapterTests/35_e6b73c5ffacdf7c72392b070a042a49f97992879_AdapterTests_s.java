 package org.jmlspecs.eclipse.jdt.core.tests.boogie;
 
 
 public class AdapterTests extends TranslationTests {
 	public AdapterTests(String name) {
 		super(name);
 	}
 
 	@Override
 	protected void compareJavaToBoogie(String java, String boogie, String adapterOutput) {
 		if (adapterOutput == null) {
 			fail("Missing adapter test for this test case");
 			return;
 		}
 
 		// always run initial test first to validate boogie
 		super.compareJavaToBoogie(java, boogie, adapterOutput);
 		
 		// run through adapter
 		runNegativeTest(new String[] {"A.java", java}, adapterOutput);
 	}
 
 	// term=ReturnStatement,JmlMethodSpecification,EqualExpression,IntLiteral
 	public void test_0400_postconditionFails() {
 		this.runNegativeTest(new String[] {
 				testsPath + "A.java",
 				"package tests.esc;\n" +
 				"public class A {\n" +
 				"   //@ ensures \\result == 42;\n" + 
 				"	public int n() {\n" +
 				"		return 43;\n" +
 				"	}\n" + 
 				"}"
 				},
 				"----------\n" + 
 				"1. ERROR in " + testsPath + "A.java (at line 3)\n" + 
 				"	//@ ensures \\result == 42;\n" +
 				"	            ^^^^^^^^^^^^^\n" + 
 				"This postcondition might not hold.\n" + 
 				"----------\n" + 
 				"2. ERROR in " + testsPath + "A.java (at line 5)\n" + 
 				"	return 43;\n" +
 				"	       ^^\n" + 
 				"This postcondition might not hold.\n" + 
 				"----------\n");
 	}
 
 	// term=ReturnStatement,JmlAssertStatement,JmlMethodSpecification,EqualExpression,IntLiteral
 	public void test_0400_MultipleFailures() {
 		this.runNegativeTest(new String[] {
 				testsPath + "A.java",
 				"package tests.esc;\n" +
 				"public class A {\n" +
 				"	public void x() {\n" +
 				"		//@ assert false;\n" +
 				"	}\n" + 
 				"\n" +
 				"   //@ ensures \\result == 42;\n" + 
 				"	public int n() {\n" +
 				"		return 43;\n" +
 				"	}\n" + 
 				"}"
 				},
 				"----------\n" + 
 				"1. ERROR in " + testsPath + "A.java (at line 4)\n" + 
 				"	//@ assert false;\n" +
 				"	           ^^^^^\n" + 
 				"This assertion might not hold.\n" + 
 				"----------\n" + 
 				"2. ERROR in " + testsPath + "A.java (at line 7)\n" + 
 				"	//@ ensures \\result == 42;\n" +
 				"	            ^^^^^^^^^^^^^\n" + 
 				"This postcondition might not hold.\n" + 
 				"----------\n" + 
 				"3. ERROR in " + testsPath + "A.java (at line 9)\n" + 
 				"	return 43;\n" +
 				"	       ^^\n" + 
 				"This postcondition might not hold.\n" + 
 				"----------\n");
 	}
 	
 	public void test_0401_UnSupportedError_BitShift() {
 		this.runNegativeTest(new String[] {
 				testsPath + "A.java",
 				"package tests.esc;\n" +
 				"public class A {\n" +
 				"	public void x() {" +
 				"		int x = 1 << 2;\n" +
 				"	}\n" + 
 				"}\n"
 				},
 				"----------\n" + 
 				"1. ERROR in " + testsPath + "A.java (at line 1)\n" + 
 				"	package tests.esc;\n" +
 				"	^\n" + 
 				"Error parsing Java source code (unsuppored syntax?): out.bpl(12,11): syntax error: ) expected\n" + 
 				"----------\n");
 	}
 	
 	public void test_0402_MissingModifies() {
 		this.runNegativeTest(new String[] {
 				testsPath + "A.java",
 				"package tests.esc;\n" +
 				"public class A {\n" +
 				"	public int q;\n" +
 				"	public void x() {\n" +
 				"		q = 1;\n" +
 				"	}\n" + 
 				"}\n"
 				},
 				"----------\n" +
 				"1. ERROR in A.java (at line 5)\n" +
 				"	q = 1;\n" +
 				"	^^^^^\n" +
 				"Missing JML modifies clause for this attribute assignment.\n" +
 				"----------\n");
 	}
 	
 	public void test_Stack() {
 		this.runNegativeTest(new String[] {
 				testsPath + "Stack.java",
 				"package tests.esc;\n" +
 				"public class Stack {\n" +
 				"	private int stack[];\n" +
 				"	private int size;\n" +
 				"	\n" +
 				"	//@ modifies stack;\n" +
 				"	//@ modifies size;\n" +
 				"	public Stack() {\n" +
 				"		stack = new int[20];\n" +
 				"		size = 0;\n" +
 				"	}\n" +
 				"	\n" +
 				"	//@ requires size < 20;\n" +
 				"	//@ ensures size == \\old(size) + 1;\n" +
 				"	//@ ensures stack[\\old(size)] == value;\n" +
 				"	//@ modifies stack;\n" +
 				"	//@ modifies size;\n" +
 				"	public void push(int value) {\n" +
 				"		stack[size] = value;\n" +
 				"		size = size + 1;\n" +
 				"	}\n" +
 				"	\n" +
 				"	//@ requires size >= 0;\n" +
 				"	//@ ensures size == \\old(size) - 1;\n" +
 				"	//@ ensures \\result == stack[\\old(size)];\n" +
 				"	//@ modifies stack;\n" +
 				"	//@ modifies size;\n" +
 				"	public int pop() {\n" +
 				"		int n = stack[size];\n" +
 				"		size = size - 1;\n" +
 				"		return n;\n" +
 				"	}\n" +
 				"}\n"
 				},
 				""
 				);
 	}
 
 	@Override
 	public void test_001_assertFalse() {
 		super.test_001_assertFalse();
 	}
 
 	@Override
 	public void test_002_assertTrue() {
 		super.test_002_assertTrue();
 	}
 
 	@Override
 	public void test_003_assertParam() {
 		super.test_003_assertParam();
 	}
 
 	@Override
 	public void test_004_assert_sequence_and() {
 		super.test_004_assert_sequence_and();
 	}
 
 	@Override
 	public void test_005_assert_sequence_or() {
 		super.test_005_assert_sequence_or();
 	}
 
 	@Override
 	public void test_006_assert_sequence_and_or() {
 		super.test_006_assert_sequence_and_or();
 	}
 
 	@Override
 	public void test_007_assert_sequence_ft() {
 		super.test_007_assert_sequence_ft();
 	}
 
 	@Override
 	public void test_007_assert_sequence_or_and() {
 		super.test_007_assert_sequence_or_and();
 	}
 
 	@Override
 	public void test_008_assert_sequence_ff() {
 		super.test_008_assert_sequence_ff();
 	}
 
 	@Override
 	public void test_008_assert_sequence_tt() {
 		super.test_008_assert_sequence_tt();
 	}
 
 	@Override
 	public void test_009_assert_sequence_tf() {
 		super.test_009_assert_sequence_tf();
 	}
 
 	@Override
 	public void test_009_JavaAssertFalse() {
 		super.test_009_JavaAssertFalse();
 	}
 
 	@Override
 	public void test_010_JavaAssertTrue() {
 		super.test_010_JavaAssertTrue();
 	}
 
 	@Override
 	public void test_0100_assumeFalse() {
 		super.test_0100_assumeFalse();
 	}
 
 	@Override
 	public void test_0101_assumeTrue() {
 		super.test_0101_assumeTrue();
 	}
 
 	@Override
 	public void test_0110_JmlMethodDeclaration_EmptyMethod() {
 		super.test_0110_JmlMethodDeclaration_EmptyMethod();
 	}
 
 	@Override
 	public void test_0111_MethodDefinition() {
 		super.test_0111_MethodDefinition();
 	}
 
 	@Override
 	public void test_0112_DoubleMethodDefinition() {
 		super.test_0112_DoubleMethodDefinition();
 	}
 
 	@Override
 	public void test_0112_JmlMethodDefinition_EnsuresRequires() {
 		super.test_0112_JmlMethodDefinition_EnsuresRequires();
 	}
 	
 	@Override
 	public void test_0113_Requires() {
 		super.test_0113_Requires();
 	}
 
 	@Override
 	public void test_0114_Ensures() {
 		super.test_0114_Ensures();
 	}
 	
 	@Override
 	public void test_0115_MultipleEnsures() {
 		super.test_0115_MultipleEnsures();
 	}
 	
 	@Override
 	public void test_0200_sequence_assume_assert_tt() {
 		super.test_0200_sequence_assume_assert_tt();
 	}
 
 	@Override
 	public void test_0201_sequence_assume_assert_ff() {
 		super.test_0201_sequence_assume_assert_ff();
 	}
 
 	@Override
 	public void test_0202_sequence_assume_assert_ft() {
 		super.test_0202_sequence_assume_assert_ft();
 	}
 
 	@Override
 	public void test_0203_sequence_assume_assert_tf() {
 		super.test_0203_sequence_assume_assert_tf();
 	}
 
 	@Override
 	public void test_0295_LocalDeclarationDefaultInitialization() {
 		super.test_0295_LocalDeclarationDefaultInitialization();
 	}
 
 	@Override
 	public void test_0296_LocalDeclaration_Blocks() {
 		super.test_0296_LocalDeclaration_Blocks();
 	}
 
 	@Override
 	public void test_0297_LocalDeclaration() {
 		super.test_0297_LocalDeclaration();
 	}
 
 	@Override
 	public void test_0298_LocalDeclarationDuplicateSymbol() {
 		super.test_0298_LocalDeclarationDuplicateSymbol();
 	}
 
 	@Override
 	public void test_0299_LocalDeclarationWithInitialization() {
 		super.test_0299_LocalDeclarationWithInitialization();
 	}
 
 	@Override
 	public void test_0300_IfCondition() {
 		super.test_0300_IfCondition();
 	}
 
 	@Override
 	public void test_0301_IfCondition_noBlock() {
 		super.test_0301_IfCondition_noBlock();
 	}
 
 	@Override
 	public void test_0302_IfCondition_ternary() {
 		super.test_0302_IfCondition_ternary();
 	}
 
 	@Override
 	public void test_0310_EmptyStatement() {
 		super.test_0310_EmptyStatement();
 	}
 
 	@Override
 	public void test_0320_UnaryExpression() {
 		super.test_0320_UnaryExpression();
 	}
 
 	@Override
 	public void test_0321_UnaryExpression() {
 		super.test_0321_UnaryExpression();
 	}
 
 	@Override
 	public void test_0350_while() {
 		super.test_0350_while();
 	}
 
 	@Override
 	public void test_0370_while_break_withlabel() {
 		super.test_0370_while_break_withlabel();
 	}
 
 	@Override
 	public void test_0371_while_break() {
 		super.test_0371_while_break();
 	}
 
 	@Override
 	public void test_0372_while_invariant_true() {
 		super.test_0372_while_invariant_true();
 	}
 
 	@Override
 	public void test_0373_while_invariant_expr() {
 		super.test_0373_while_invariant_expr();
 	}
 
 	@Override
 	public void test_0374_while_invariant_break() {
 		super.test_0374_while_invariant_break();
 	}
 
 	@Override
 	public void test_1000_int_eq() {
 		super.test_1000_int_eq();
 	}
 
 	@Override
 	public void test_1000_int_localdeclaration() {
 		super.test_1000_int_localdeclaration();
 	}
 
 	@Override
 	public void test_1001_int_arith() {
 		super.test_1001_int_arith();
 	}
 
 	@Override
 	public void test_1002_arith_cond() {
 		super.test_1002_arith_cond();
 	}
 
 	@Override
 	public void test_1003_boolExpr_cond() {
 		super.test_1003_boolExpr_cond();
 	}
 
 	@Override
 	public void test_1004_implies() {
 		super.test_1004_implies();
 	}
 
 	@Override
 	public void test_1005_int_boundaries() {
 		super.test_1005_int_boundaries();
 	}
 
 	@Override
 	public void test_2000_messageSend() {
 		super.test_2000_messageSend();
 	}
 
 	@Override
 	public void test_2001_messageSend() {
 		super.test_2001_messageSend();
 	}
 
 	@Override
 	public void test_2002_messageSend() {
 		super.test_2002_messageSend();
 	}
 
 	@Override
 	public void test_2003_messageSendStatic() {
 		super.test_2003_messageSendStatic();
 	}
 
 	@Override
 	public void test_2004_messageSendOnReceiver() {
 		super.test_2004_messageSendOnReceiver();
 	}
 
 	@Override
 	public void test_2005_messageSendOnField() {
 		super.test_2005_messageSendOnField();
 	}
 
 	@Override
 	public void test_2005_messageSendOnLocal() {
 		super.test_2005_messageSendOnLocal();
 	}
 
 	@Override
 	public void test_2005_messageSendOnThis() {
 		super.test_2005_messageSendOnThis();
 	}
 
 	@Override
 	public void test_2100_arrayField() {
 		super.test_2100_arrayField();
 	}
 
 	@Override
 	public void test_2101_arrayInitializer() {
 		super.test_2101_arrayInitializer();
 	}
 
 	@Override
 	public void test_2102_arrayAllocation() {
 		super.test_2102_arrayAllocation();
 	}
 
 	@Override
 	public void test_2103_arrayLength() {
 		super.test_2103_arrayLength();
 	}
 
 	@Override
 	public void test_2104_arrayDefaultInitialization() {
 		super.test_2104_arrayDefaultInitialization();
 	}
 
 	@Override
 	public void test_2200_JmlConstructorDeclaration() {
 		super.test_2200_JmlConstructorDeclaration();
 	}
 
 	@Override
 	public void test_2201_ConstructorCall() {
 		super.test_2201_ConstructorCall();
 	}
 	
 	@Override
 	public void test_2202_ConstructorCallFieldModification() {
 		super.test_2202_ConstructorCallFieldModification();
 	}
 	
 	@Override
 	public void test_2203_MultipleClasses() {
 		super.test_2203_MultipleClasses();
 	}
 
 	@Override
 	public void test_297_LocalDeclaration() {
 		super.test_297_LocalDeclaration();
 	}
 
 	@Override
 	public void test_400_do() {
 		super.test_400_do();
 	}
 
 	@Override
 	public void test_401_do_multiline() {
 		super.test_401_do_multiline();
 	}
 
 	@Override
 	public void test_402_do_invariant() {
 		super.test_402_do_invariant();
 	}
 
 	@Override
 	public void test_500_for() {
 		super.test_500_for();
 	}
 
 	@Override
 	public void test_501_for_multi_initialization() {
 		super.test_501_for_multi_initialization();
 	}
 
 	@Override
 	public void test_503_for_invariant() {
 		super.test_503_for_invariant();
 	}
 
 	@Override
 	public void test_600_postFixExpression() {
 		super.test_600_postFixExpression();
 	}
 
 	@Override
 	public void test_601_preFixExpression() {
 		super.test_601_preFixExpression();
 	}
 
 	@Override
 	public void test_602_pre_post_FixExpression() {
 		super.test_602_pre_post_FixExpression();
 	}
 
 	@Override
 	public void test_603_post_pre_FixExpression() {
 		super.test_603_post_pre_FixExpression();
 	}
 
 	@Override
 	public void test_604_multiAssignment() {
 		super.test_604_multiAssignment();
 	}
 
 	@Override
 	public void test_700_localVarDecl_order() {
 		super.test_700_localVarDecl_order();
 	}
 
 	@Override
 	public void test_800_FieldDeclaration() {
 		super.test_800_FieldDeclaration();
 	}
 
 	@Override
 	public void test_801_Static_FieldDeclaration() {
 		super.test_801_Static_FieldDeclaration();
 	}
 
 	@Override
 	public void test_900_JmlResultExpression() {
 		super.test_900_JmlResultExpression();
 	}
 
 	@Override
 	public void test_901_JmlResultExpression() {
 		super.test_901_JmlResultExpression();
 	}
 
 	@Override
 	public void test_910_JmlOldExpression() {
 		super.test_910_JmlOldExpression();
 	}
 
 	@Override
 	public void test_911_JmlOldExpression() {
 		super.test_911_JmlOldExpression();
 	}
 
 	@Override
 	public void testDoubleLiteral() {
 		super.testDoubleLiteral();
 	}
 
 	@Override
 	public void testFalseLiteral() {
 		super.testFalseLiteral();
 	}
 
 	@Override
 	public void testIntLiteral() {
 		super.testIntLiteral();
 	}
 
 	@Override
 	public void testTrueLiteral() {
 		super.testTrueLiteral();
 	}
 	
 	@Override
 	public void test_3000_StandardJavaClass() {
 		super.test_3000_StandardJavaClass();
 	}
 	
 	@Override
 	public void test_3001_TestCounterClass() {
 		super.test_3001_TestCounterClass();
 	}
 
 	@Override
 	public void test_3002_TestCounterClass() {
 		super.test_3002_TestCounterClass();
 	}
 	
 	@Override
 	public void test_3003_TestAttributeMutation() {
 		super.test_3003_TestAttributeMutation();
 	}
 }
