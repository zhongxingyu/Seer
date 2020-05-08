 package swp_compiler_ss13.fuc.backend;
 
 import swp_compiler_ss13.common.backend.Backend;
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
 	public Map<String,InputStream> generateTargetCode(List<Quadruple> tac)
 	{
 		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
 		PrintWriter out = new PrintWriter(outStream);
 
 		Module m = new Module(out);
 
 		/* Write begin for main function */
 		out.println("define i64 @main() {");
 
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
 						q.getArgument1());
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
 						q.getArgument1());
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
 
 
 				/* Control flow */
 				case RETURN:
 					m.addMainReturn(q.getArgument1());
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
 		map.put(".ll", new ByteArrayInputStream(outStream.toByteArray()));
 
 		/* Return the map containing the readable target code */
 		return map;
 	}
 }
