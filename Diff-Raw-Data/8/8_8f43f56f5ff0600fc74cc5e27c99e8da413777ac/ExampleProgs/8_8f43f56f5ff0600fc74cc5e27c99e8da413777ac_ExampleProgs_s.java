 package swp_compiler_ss13.fuc.test;
 
 
 /**
  *
  * All the examples from common/example with expected exitcodes and expected output.
  *
  * @author Jens V. Fischer
  */
 public class ExampleProgs {
 
 	/* M1 progs */
 
 	public static Object[] simpleAddProg() {
 		String prog = "" +
 				"# returns 6\n" +
 				"long l;\n" +
 				"l = 3 + 3;\n" +
 				"return l;";
 		int expectedExitcode = 6;
 		String expectedOutput = "";
 		return new Object[]{prog, expectedExitcode, expectedOutput};
 	}
 
 	public static Object[] addProg(){
 		String prog = "" +
 				"# return 27\n" +
 				"long l;\n" +
 				"l = 10 +\n" +
 				"        23 # - 23\n" +
 				"- 23\n" +
 				"+ 100 /\n" +
 				"\n" + "2\n" +
 				"-       30 \n" +
 				"      - 9 / 3;\n" +
 				"return l;";
 		int expectedExitcode = 27;
 		String expectedOutput = "";
 		return new Object[]{prog, expectedExitcode, expectedOutput};
 	}
 
 	public static Object[] simpleMulProg(){
 		String prog = "" +
 				"# return 9\n" +
 				"long l;\n" +
 				"l = 3 * 3;\n" +
 				"return l;";
 		int expectedExitcode = 9;
 		String expectedOutput = "";
 		return new Object[]{prog, expectedExitcode, expectedOutput};
 	}
 
 	public static Object[] parenthesesProg(){
 		String prog = "" +
 				"# returns 8 or does it?\n" +
 				"long l;\n" +
 				"l = ( 3 + 3 ) * 2 - ( l = ( 2 + ( 16 / 8 ) ) );\n" +
 				"return l;";
 		int expectedExitcode = 8;
 		String expectedOutput = "";
 		return new Object[]{prog, expectedExitcode, expectedOutput};
 	}
 
 
 	/* M1 progs producing errors */
 
 	public static Object[] doubleDeclaration(){
 		String prog = "" +
 				"# error: two decls for same id i\n" +
 				"long i;\n" +
 				"long i;\n";
 
 		int expectedExitcode = -1;
 		String expectedOutput = "ERROR (DOUBLE_DECLARATION): The variable 'i' of type 'LongType' has been declared twice!";
 		return new Object[]{prog, expectedExitcode, expectedOutput};
 	}
 
 	public static Object[] invalidIds(){
 		String prog = "" +
 				"# error: invalid ids\n" +
 				"long foo$bar;\n" +
 				"long spam_ham;\n" +
 				"long 2fooly;\n" +
 				"long return;\n" +
 				"long string;\n" +
 				"long bool;\n" +
 				"long fü_berlin;";
 
 		int expectedExitcode = -1;
		String expectedOutput = "ERROR (UNDEFINED): Found undefined token 'foo$bar'!";
 		return new Object[]{prog, expectedExitcode, expectedOutput};
 	}
 
 	public static Object[] multipleMinusENotation(){
 		String prog = "" +
 				"# error: id foo has multiple minus in expontent notation\n" +
 				"long foo;\n" +
 				"foo = 10e----1;";
 		int expectedExitcode = -1;
		String expectedOutput = "ERROR (UNDEFINED): Found undefined token '10e----1'!";
 		return new Object[]{prog, expectedExitcode, expectedOutput};
 	}
 
 	public static Object[] multiplePlusesInExp(){
 		String prog = "" +
 				"# error: too many pluses in an expression\n" +
 				"long foo;\n" +
 				"long bar;\n" +
 				"foo = 3;\n" +
 				"bar = foo ++ 1;";
 		int expectedExitcode = -1;
		String expectedOutput = "ERROR (UNDEFINED): Found undefined token '++'!";
 		return new Object[]{prog, expectedExitcode, expectedOutput};
 	}
 
 	public static Object[] undefReturn(){
 		String prog = "" +
 				"# error: id spam is not initialized and returned\n" +
 				"long spam;\n" +
 				"return spam;";
 		int expectedExitcode = 0;
 		String expectedOutput = "WARNNING (UNDEFINED): Variable “spam” may be used without initialization.";
 		return new Object[]{prog, expectedExitcode, expectedOutput};
 	}
 
 	/* M2 progs */
 
 	public static Object[] assignmentProg(){
 		String prog = "" +
 				"# returns 10\n" +
 				"# prints nothing\n" +
 				"long a;\n" +
 				"long b;\n" +
 				"long c;\n" +
 				"\n" +
 				"a = 4;\n" +
 				"b = 3;\n" +
 				"c = 2;\n" +
 				"\n" +
 				"a = b = 4;\n" +
 				"c = a + b + c;\n" +
 				"\n" +
 				"return c;";
 		int expectedExitcode = 10;
 		String expectedOutput = "";
 		return new Object[]{prog, expectedExitcode, expectedOutput};
 	}
 
 	public static Object[] condProg(){
 		String prog = "" +
 				"# return 5\n" +
 				"# prints nothing\n" +
 				"\n" +
 				"bool b;\n" +
 				"bool c;\n" +
 				"long l;\n" +
 				"\n" +
 				"b = true;\n" +
 				"c = false;\n" +
 				"\n" +
 				"l = 4;\n" +
 				"\n" +
 				"# dangling-else should be resolved as given by indentation\n" +
 				"\n" +
 				"if ( b )\n" +
 				"  if ( c || ! b )\n" +
 				"    print \"bla\";\n" +
 				"  else\n" +
 				"    l = 5;\n" +
 				"\n" +
 				"return l;";
 		int expectedExitcode = 5;
 		String expectedOutput = "";
 		return new Object[]{prog, expectedExitcode, expectedOutput};
 	}
 
 	public static Object[] printProg(){
 		String prog = "" +
 				"# return 0\n" +
 				"# prints:\n" +
 				"# true\n" +
 				"# 18121313223\n" +
 				"# -2.323e-99\n" +
 				"# jagÄrEttString\"\n" +
 				"\n" +
 				"long l;\n" +
 				"double d;\n" +
 				"string s;\n" +
 				"bool b;\n" +
 				"\n" +
 				"b = true;\n" +
 				"l = 18121313223;\n" +
 				"d = -23.23e-100;\n" +
 				"s = \"jagÄrEttString\\\"\\n\";  # c-like escaping in strings\n" +
 				"\n" +
 				"print b; print \"\\n\";\n" +
 				"print l; print \"\\n\";       # print one digit left of the radix point\n" +
 				"print d; print \"\\n\";\n" +
 				"print s;\n" +
 				"\n" +
 				"return;                    # equivalent to return EXIT_SUCCESS";
 		int expectedExitcode = 0;
 		String expectedOutput = "" +
 				"true\n" +
 				"18121313223\n" +
 				"-2.323e-99\n" +
 				"jagÄrEttString\"";
 		return new Object[]{prog, expectedExitcode, expectedOutput};
 	}
 
 }
