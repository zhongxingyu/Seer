 package swp_compiler_ss13.fuc.backend;
 
 import swp_compiler_ss13.common.backend.Backend;
 import swp_compiler_ss13.common.backend.BackendException;
 import swp_compiler_ss13.common.backend.Quadruple;
 import swp_compiler_ss13.common.types.Type;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * This implements a backend for LLVM IR.
  *
  */
 public class LLVMBackend implements Backend
 {
 
 
 	/**
 	 * This function generates LLVM IR code for
 	 * given three address code.
 	 *
 	 * @param tac the three address code
 	 * @return the generated LLVM IR code.
 	 */
 	@Override
 	public Map<String, InputStream> generateTargetCode(String baseFileName, List<Quadruple> tac) throws BackendException {
 		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
 		PrintWriter out = new PrintWriter(outStream);
 
 		Module m = new Module(out);
 
 		/* Write printf format strings for primitive types */
		out.println("@.string_format_long = private unnamed_addr constant [4 x i8] c\"%d\\0A\\00\"");
 		out.println("@.string_format_double = private unnamed_addr constant [4 x i8] c\"%e\\0A\\00\"");
 		out.println("@.string_boolean_false = private unnamed_addr constant [7 x i8] c\"false\\0A\\00\"");
 		out.println("@.string_boolean_true = private unnamed_addr constant [6 x i8] c\"true\\0A\\00\"");
 		out.println("@.string_format_string = private unnamed_addr constant [4 x i8] c\"%s\\0A\\00\"");
 		out.println("");
 
 		out.println("define void @print_boolean(i8) {");
 		out.println("  %condition = trunc i8 %0 to i1");
 		out.println("  br i1 %condition, label %IfTrue, label %IfFalse");
 		out.println("  IfTrue:");
 		out.println("    %true = getelementptr [6 x i8]* @.string_boolean_true, i64 0, i64 0");
 		out.println("    call i32 (i8*, ...)* @printf(i8* %true)");
 		out.println("    br label %End");
 		out.println("  IfFalse:");
 		out.println("    %false = getelementptr [7 x i8]* @.string_boolean_false, i64 0, i64 0");
 		out.println("    call i32 (i8*, ...)* @printf(i8* %false)");
 		out.println("    br label %End");
 		out.println("  End:");
 		out.println("  ret void");
 		out.println("}\n");
 
 		/* Write builtin printer function (llvm lib) */
 		out.println("declare i32 @printf(i8* noalias nocapture, ...)");
 		out.println("");
 
 		/* Write begin for main function */
 		out.println("define i64 @main() {");
 
 		m.addPrimitiveDeclare(
 			Type.Kind.STRING,
 			".tmp.string",
 			Quadruple.EmptyArgument);
 		m.addPrimitiveDeclare(
 			Type.Kind.STRING,
 			".format.string",
 			Quadruple.EmptyArgument);
 
 		for(Quadruple q : tac)
 		{
 			switch(q.getOperator())
 			{
 				/* Variable declaration */
 				case DECLARE_LONG:
 					m.addPrimitiveDeclare(
 						Type.Kind.LONG,
 						q.getResult(),
 						q.getArgument1());
 					break;
 				case DECLARE_DOUBLE:
 					m.addPrimitiveDeclare(
 						Type.Kind.DOUBLE,
 						q.getResult(),
 						q.getArgument1());
 					break;
 				case DECLARE_BOOLEAN:
 					m.addPrimitiveDeclare(
 						Type.Kind.BOOLEAN,
 						q.getResult(),
 						Module.toIRBoolean(q.getArgument1()));
 					break;
 				case DECLARE_STRING:
 					m.addPrimitiveDeclare(
 						Type.Kind.STRING,
 						q.getResult(),
 						Module.toIRString(q.getArgument1()));
 					break;
 
 				/* Type conversion */
 				case LONG_TO_DOUBLE:
 					m.addPrimitiveConversion(
 						Type.Kind.LONG,
 						q.getArgument1(),
 						Type.Kind.DOUBLE,
 						q.getResult());
 					break;
 				case DOUBLE_TO_LONG:
 					m.addPrimitiveConversion(
 						Type.Kind.DOUBLE,
 						q.getArgument1(),
 						Type.Kind.LONG,
 						q.getResult());
 					break;
 
 				/* Unindexed copy */
 				case ASSIGN_LONG:
 					m.addPrimitiveAssign(
 						Type.Kind.LONG,
 						q.getResult(),
 						q.getArgument1());
 					break;
 				case ASSIGN_DOUBLE:
 					m.addPrimitiveAssign(
 						Type.Kind.DOUBLE,
 						q.getResult(),
 						q.getArgument1());
 					break;
 				case ASSIGN_BOOLEAN:
 					m.addPrimitiveAssign(
 						Type.Kind.BOOLEAN,
 						q.getResult(),
 						Module.toIRBoolean(q.getArgument1()));
 					break;
 				case ASSIGN_STRING:
 					m.addPrimitiveAssign(
 						Type.Kind.STRING,
 						q.getResult(),
 						Module.toIRString(q.getArgument1()));
 					break;
 
 				/* Arithmetic */
 				case ADD_LONG:
 					m.addPrimitiveBinaryInstruction(
 						q.getOperator(),
 						Type.Kind.LONG,
 						q.getArgument1(),
 						q.getArgument2(),
 						q.getResult());
 					break;
 				case ADD_DOUBLE:
 					m.addPrimitiveBinaryInstruction(
 						q.getOperator(),
 						Type.Kind.DOUBLE,
 						q.getArgument1(),
 						q.getArgument2(),
 						q.getResult());
 					break;
 				case SUB_LONG:
 					m.addPrimitiveBinaryInstruction(
 						q.getOperator(),
 						Type.Kind.LONG,
 						q.getArgument1(),
 						q.getArgument2(),
 						q.getResult());
 					break;
 				case SUB_DOUBLE:
 					m.addPrimitiveBinaryInstruction(
 						q.getOperator(),
 						Type.Kind.DOUBLE,
 						q.getArgument1(),
 						q.getArgument2(),
 						q.getResult());
 					break;
 				case MUL_LONG:
 					m.addPrimitiveBinaryInstruction(
 						q.getOperator(),
 						Type.Kind.LONG,
 						q.getArgument1(),
 						q.getArgument2(),
 						q.getResult());
 					break;
 				case MUL_DOUBLE:
 					m.addPrimitiveBinaryInstruction(
 						q.getOperator(),
 						Type.Kind.DOUBLE,
 						q.getArgument1(),
 						q.getArgument2(),
 						q.getResult());
 					break;
 				case DIV_LONG:
 					m.addPrimitiveBinaryInstruction(
 						q.getOperator(),
 						Type.Kind.LONG,
 						q.getArgument1(),
 						q.getArgument2(),
 						q.getResult());
 					break;
 				case DIV_DOUBLE:
 					m.addPrimitiveBinaryInstruction(
 						q.getOperator(),
 						Type.Kind.DOUBLE,
 						q.getArgument1(),
 						q.getArgument2(),
 						q.getResult());
 					break;
 				case NOT_BOOLEAN:
 					m.addBooleanNot(Module.toIRBoolean(q.getArgument1()), q.getResult());
 					break;
 				case OR_BOOLEAN:
 					m.addPrimitiveBinaryInstruction(
 							q.getOperator(),
 							Type.Kind.BOOLEAN,
 							Module.toIRBoolean(q.getArgument1()),
 							Module.toIRBoolean(q.getArgument2()),
 							q.getResult());
 					break;
 				case AND_BOOLEAN:
 					m.addPrimitiveBinaryInstruction(
 							q.getOperator(),
 							Type.Kind.BOOLEAN,
 							Module.toIRBoolean(q.getArgument1()),
 							Module.toIRBoolean(q.getArgument2()),
 							q.getResult());
 					break;
 
 				/* Comparisons */
 				case COMPARE_LONG_E:
 				m.addPrimitiveBinaryInstruction(
 						q.getOperator(),
 						Type.Kind.LONG,
 						q.getArgument1(),
 						q.getArgument2(),
 						q.getResult());
 				break;
 				case COMPARE_LONG_G:
 					m.addPrimitiveBinaryInstruction(
 							q.getOperator(),
 							Type.Kind.LONG,
 							q.getArgument1(),
 							q.getArgument2(),
 							q.getResult());
 					break;
 				case COMPARE_LONG_L:
 					m.addPrimitiveBinaryInstruction(
 							q.getOperator(),
 							Type.Kind.LONG,
 							q.getArgument1(),
 							q.getArgument2(),
 							q.getResult());
 					break;
 				case COMPARE_LONG_GE:
 					m.addPrimitiveBinaryInstruction(
 							q.getOperator(),
 							Type.Kind.LONG,
 							q.getArgument1(),
 							q.getArgument2(),
 							q.getResult());
 					break;
 				case COMPARE_LONG_LE:
 					m.addPrimitiveBinaryInstruction(
 							q.getOperator(),
 							Type.Kind.LONG,
 							q.getArgument1(),
 							q.getArgument2(),
 							q.getResult());
 					break;
 				case COMPARE_DOUBLE_E:
 					m.addPrimitiveBinaryInstruction(
 							q.getOperator(),
 							Type.Kind.DOUBLE,
 							q.getArgument1(),
 							q.getArgument2(),
 							q.getResult());
 					break;
 				case COMPARE_DOUBLE_G:
 					m.addPrimitiveBinaryInstruction(
 							q.getOperator(),
 							Type.Kind.DOUBLE,
 							q.getArgument1(),
 							q.getArgument2(),
 							q.getResult());
 					break;
 				case COMPARE_DOUBLE_L:
 					m.addPrimitiveBinaryInstruction(
 							q.getOperator(),
 							Type.Kind.DOUBLE,
 							q.getArgument1(),
 							q.getArgument2(),
 							q.getResult());
 					break;
 				case COMPARE_DOUBLE_GE:
 					m.addPrimitiveBinaryInstruction(
 							q.getOperator(),
 							Type.Kind.DOUBLE,
 							q.getArgument1(),
 							q.getArgument2(),
 							q.getResult());
 					break;
 				case COMPARE_DOUBLE_LE:
 					m.addPrimitiveBinaryInstruction(
 							q.getOperator(),
 							Type.Kind.DOUBLE,
 							q.getArgument1(),
 							q.getArgument2(),
 							q.getResult());
 					break;
 
 				/* Control flow */
 				case RETURN:
 					m.addMainReturn(q.getArgument1());
 					break;
 				case LABEL:
 					m.addLabel(q.getArgument1());
 					break;
 				case BRANCH:
 					m.addBranch(q.getArgument1(),
 							q.getArgument2(),
 							q.getResult());
 					break;
 
 				/* Print */
 				case PRINT_LONG:
 					m.addPrint(q.getArgument1(), Type.Kind.LONG);
 					break;
 				case PRINT_DOUBLE:
 					m.addPrint(q.getArgument1(), Type.Kind.DOUBLE);
 					break;
 				case PRINT_BOOLEAN:
 					m.addPrint(Module.toIRBoolean(q.getArgument1()), Type.Kind.BOOLEAN);
 					break;
 				case PRINT_STRING:
 					m.addPrint(Module.toIRString(q.getArgument1()), Type.Kind.STRING);
 					break;
 			}
 		}
 
 		/* Write return 0 if last operation is not a return */
 		if(tac.size() == 0 || tac.get(tac.size() - 1).getOperator() != Quadruple.Operator.RETURN)
 		{
 			out.println("  ret i64 0");
 		}
 
 		/* Write end for main function */
 		out.println("}");
 
 		/* Finish writing */
 		out.close();
 
 		/* Convert written target code to readable input format */
 		Map<String,InputStream> map = new HashMap<String,InputStream>();
 		map.put(baseFileName+".ll", new ByteArrayInputStream(outStream.toByteArray()));
 
 		/* Return the map containing the readable target code */
 		return map;
 	}
 }
