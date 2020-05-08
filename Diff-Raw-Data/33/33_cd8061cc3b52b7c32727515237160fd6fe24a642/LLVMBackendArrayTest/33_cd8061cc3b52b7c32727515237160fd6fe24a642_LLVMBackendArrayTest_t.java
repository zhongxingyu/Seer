 package swp_compiler_ss13.fuc.backend;
 
 import org.junit.Test;
 import swp_compiler_ss13.common.backend.BackendException;
 import java.io.*;
 
 import static swp_compiler_ss13.common.backend.Quadruple.*;
 
 /**
  * Tests for LLVMBackend: Arrays
  */
 public class LLVMBackendArrayTest extends TestBase {
 	/* test test */
 	@Test public void arrays__declare_array__long() throws IOException, BackendException {
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#70", EmptyArgument, "x"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_LONG, EmptyArgument, EmptyArgument, null));
 		String mainFunctionCode = "" +
 				"  %x = alloca [70 x i64]\n" +
 				"  ret i64 0\n";
 		expectMain(mainFunctionCode, generateCodeAsString(tac));
 	}
 	@Test public void arrays__declare_array__double() throws IOException, BackendException {
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#25", EmptyArgument, "x"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_DOUBLE, EmptyArgument, EmptyArgument, null));
 		String mainFunctionCode = "" +
 				"  %x = alloca [25 x double]\n" +
 				"  ret i64 0\n";
 		expectMain(mainFunctionCode, generateCodeAsString(tac));
 	}
 	@Test public void arrays__declare_array__double_and_long() throws IOException, BackendException {
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#25", EmptyArgument, "x"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_DOUBLE, EmptyArgument, EmptyArgument, null));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#35", EmptyArgument, "y"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_LONG, EmptyArgument, EmptyArgument, null));
 		String mainFunctionCode = "" +
 				"  %x = alloca [25 x double]\n" +
 				"  %y = alloca [35 x i64]\n" +
 				"  ret i64 0\n";
 		expectMain(mainFunctionCode, generateCodeAsString(tac));
 	}
 	@Test public void arrays__declare_array__double_prim_long() throws IOException, BackendException {
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#25", EmptyArgument, "x"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_DOUBLE, EmptyArgument, EmptyArgument, null));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_DOUBLE, EmptyArgument, EmptyArgument, "z"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#35", EmptyArgument, "y"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_LONG, EmptyArgument, EmptyArgument, null));
 		String mainFunctionCode = "" +
 				"  %x = alloca [25 x double]\n" +
 				"  %z = alloca double\n" +
 				"  %y = alloca [35 x i64]\n" +
 				"  ret i64 0\n";
 		expectMain(mainFunctionCode, generateCodeAsString(tac));
 	}
 
 	@Test public void arrays__declare_array__3d_long() throws IOException, BackendException {
 	   	tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#70", EmptyArgument, "x"));
 	   	tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#600", EmptyArgument, null));
 	   	tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#2", EmptyArgument, null));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_LONG, EmptyArgument, EmptyArgument, null));
 		String mainFunctionCode = "" +
 				"  %x = alloca [70 x [600 x [2 x i64]]]\n" +
 				"  ret i64 0\n";
 		expectMain(mainFunctionCode, generateCodeAsString(tac));
  	}
 
 	@Test public void arrays__get__double() throws IOException, BackendException {
 		tac.add(new QuadrupleImpl(Operator.DECLARE_DOUBLE, EmptyArgument, EmptyArgument, "x"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#20", EmptyArgument, "a"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_DOUBLE, EmptyArgument, EmptyArgument, null));
 		tac.add(new QuadrupleImpl(Operator.ARRAY_GET_DOUBLE, "a", "#12", "x"));
 		expectMain("  %x = alloca double"+"\n"+
 				   "  %a = alloca [20 x double]"+"\n"+
 				   "  %.tmp.0 = getelementptr [20 x double]* %a, i64 0, i64 12"+"\n"+
 				   "  %.tmp.1 = load double* %.tmp.0"+"\n"+
 		           "  store double %.tmp.1, double* %x"+"\n"+
 		           "  ret i64 0"+"\n",
 				   generateCodeAsString(tac));
     }
 
 	@Test public void arrays__set__double() throws IOException, BackendException {
 		tac.add(new QuadrupleImpl(Operator.DECLARE_DOUBLE,"#66.5", EmptyArgument, "x"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#20", EmptyArgument, "a"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_DOUBLE, EmptyArgument, EmptyArgument, null));
 		tac.add(new QuadrupleImpl(Operator.ARRAY_SET_DOUBLE, "a", "#12", "x"));
 		expectMain("  %x = alloca double"+"\n"+
 				   "  store double 66.5, double* %x"+"\n"+
 				   "  %a = alloca [20 x double]"+"\n"+
 				   "  %.tmp.0 = getelementptr [20 x double]* %a, i64 0, i64 12"+"\n"+
 		           "  %x.0 = load double* %x"+"\n"+
 		           "  store double %x.0, double* %.tmp.0"+"\n"+
 		           "  ret i64 0"+"\n",
 				   generateCodeAsString(tac));
     }
 
 	@Test public void arrays__set__const_double() throws IOException, BackendException {
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#20", EmptyArgument, "a"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_DOUBLE, EmptyArgument, EmptyArgument, null));
 		tac.add(new QuadrupleImpl(Operator.ARRAY_SET_DOUBLE, "a", "#12", "#88.6"));
 		expectMain("  %a = alloca [20 x double]"+"\n"+
 				   "  %.tmp.0 = getelementptr [20 x double]* %a, i64 0, i64 12"+"\n"+
 		           "  store double 88.6, double* %.tmp.0"+"\n"+
 		           "  ret i64 0"+"\n",
 				   generateCodeAsString(tac));
     }
 
 	@Test public void arrays__set__const_long() throws IOException, BackendException {
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#20", EmptyArgument, "a"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_LONG, EmptyArgument, EmptyArgument, null));
 		tac.add(new QuadrupleImpl(Operator.ARRAY_SET_LONG, "a", "#12", "#17"));
 		expectMain("  %a = alloca [20 x i64]"+"\n"+
 				   "  %.tmp.0 = getelementptr [20 x i64]* %a, i64 0, i64 12"+"\n"+
 		           "  store i64 17, i64* %.tmp.0"+"\n"+
 		           "  ret i64 0"+"\n",
 				   generateCodeAsString(tac));
     }
 
 	@Test public void arrays__declare_reference() throws IOException, BackendException {
 		// reference declarations are completely superfluous, so we just ignore them
 		//  (in llvm, we store a pointer when we dereference a multidimensional array,
 		//   but since we have to pass the whole array type (and the sub-array type) when
 		//   we dereference it, they fact IF we get a reference and the TYPE of the reference
 		//   are 100% determined by the context. The reference declaration does not provide us
 		//   with the type anyways....
 		//  )
 		tac.add(new QuadrupleImpl(Operator.DECLARE_REFERENCE, EmptyArgument, EmptyArgument, "r"));
 		expectMain("  ret i64 0"+"\n",
 				   generateCodeAsString(tac));
 	}
 
 	@Test public void arrays__get_reference__2d_double() throws IOException, BackendException {
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#20", EmptyArgument, "a"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#30", EmptyArgument, null));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_DOUBLE, EmptyArgument, EmptyArgument, null));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_REFERENCE, EmptyArgument, EmptyArgument, "r"));
 		tac.add(new QuadrupleImpl(Operator.ARRAY_GET_REFERENCE, "a", "#15", "r"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_DOUBLE, EmptyArgument, EmptyArgument, "v"));
 		tac.add(new QuadrupleImpl(Operator.ARRAY_GET_DOUBLE, "r", "#25", "v"));
 		expectMain(
 				   "  %a = alloca [20 x [30 x double]]"+"\n"+
 				   "  %r.0 = getelementptr [20 x [30 x double]]* %a, i64 0, i64 15"+"\n"+
 				   "  %v = alloca double"+"\n"+
 				   "  %.tmp.0 = getelementptr [30 x double]* %r.0, i64 0, i64 25"+"\n"+
 				   "  %.tmp.1 = load double* %.tmp.0"+"\n"+
 		           "  store double %.tmp.1, double* %v"+"\n"+
 		           "  ret i64 0"+"\n",
 				   generateCodeAsString(tac));
     }
 
 	@Test public void arrays__use_reference_two_times() throws IOException, BackendException {
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#20", EmptyArgument, "a"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#30", EmptyArgument, null));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_DOUBLE, EmptyArgument, EmptyArgument, null));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_REFERENCE, EmptyArgument, EmptyArgument, "r"));
 		tac.add(new QuadrupleImpl(Operator.ARRAY_GET_REFERENCE, "a", "#15", "r"));
 		tac.add(new QuadrupleImpl(Operator.ARRAY_GET_REFERENCE, "a", "#16", "r"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_DOUBLE, EmptyArgument, EmptyArgument, "v"));
 		tac.add(new QuadrupleImpl(Operator.ARRAY_GET_DOUBLE, "r", "#25", "v"));
 		expectMain(
 				   "  %a = alloca [20 x [30 x double]]"+"\n"+
 				   "  %r.0 = getelementptr [20 x [30 x double]]* %a, i64 0, i64 15"+"\n"+
 				   "  %r.1 = getelementptr [20 x [30 x double]]* %a, i64 0, i64 16"+"\n"+
 				   "  %v = alloca double"+"\n"+
 				   "  %.tmp.0 = getelementptr [30 x double]* %r.1, i64 0, i64 25"+"\n"+
 				   "  %.tmp.1 = load double* %.tmp.0"+"\n"+
 		           "  store double %.tmp.1, double* %v"+"\n"+
 		           "  ret i64 0"+"\n",
 				   generateCodeAsString(tac));
     }
 	@Test public void arrays__reference_self_resolve() throws IOException, BackendException {
 		tac.add(new QuadrupleImpl(Operator.DECLARE_REFERENCE, EmptyArgument, EmptyArgument, "r"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#20", EmptyArgument, "a"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#30", EmptyArgument, null));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#40", EmptyArgument, null));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_LONG, EmptyArgument, EmptyArgument, null));
 		tac.add(new QuadrupleImpl(Operator.ARRAY_GET_REFERENCE, "a", "#15", "r"));
 		tac.add(new QuadrupleImpl(Operator.ARRAY_GET_REFERENCE, "r", "#20", "r"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_LONG, EmptyArgument, EmptyArgument, "v"));
 		tac.add(new QuadrupleImpl(Operator.ARRAY_GET_DOUBLE, "r", "#25", "v"));
 		expectMain(
 				   "  %a = alloca [20 x [30 x [40 x i64]]]"+"\n"+
 				   "  %r.0 = getelementptr [20 x [30 x [40 x i64]]]* %a, i64 0, i64 15"+"\n"+
 				   "  %r.1 = getelementptr [30 x [40 x i64]]* %r.0, i64 0, i64 20"+"\n"+
 				   "  %v = alloca i64"+"\n"+
 				   "  %.tmp.0 = getelementptr [40 x i64]* %r.1, i64 0, i64 25"+"\n"+
 				   "  %.tmp.1 = load i64* %.tmp.0"+"\n"+
 		           "  store i64 %.tmp.1, i64* %v"+"\n"+
 		           "  ret i64 0"+"\n",
 				   generateCodeAsString(tac));
     }
	@Test public void arrays__reference_self_resolve_set_long() throws IOException, BackendException {
		tac.add(new QuadrupleImpl(Operator.DECLARE_REFERENCE, EmptyArgument, EmptyArgument, "r"));
		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#20", EmptyArgument, "a"));
		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#30", EmptyArgument, null));
		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#40", EmptyArgument, null));
		tac.add(new QuadrupleImpl(Operator.DECLARE_LONG, EmptyArgument, EmptyArgument, null));
		tac.add(new QuadrupleImpl(Operator.ARRAY_GET_REFERENCE, "a", "#15", "r"));
		tac.add(new QuadrupleImpl(Operator.ARRAY_GET_REFERENCE, "r", "#20", "r"));
		tac.add(new QuadrupleImpl(Operator.ARRAY_SET_DOUBLE, "r", "#25", "#999"));
		expectMain(
				   "  %a = alloca [20 x [30 x [40 x i64]]]"+"\n"+
				   "  %r.0 = getelementptr [20 x [30 x [40 x i64]]]* %a, i64 0, i64 15"+"\n"+
				   "  %r.1 = getelementptr [30 x [40 x i64]]* %r.0, i64 0, i64 20"+"\n"+
				   "  %.tmp.0 = getelementptr [40 x i64]* %r.1, i64 0, i64 25"+"\n"+
		           "  store i64 999, i64* %.tmp.0"+"\n"+
		           "  ret i64 0"+"\n",
				   generateCodeAsString(tac));
    }
 }
