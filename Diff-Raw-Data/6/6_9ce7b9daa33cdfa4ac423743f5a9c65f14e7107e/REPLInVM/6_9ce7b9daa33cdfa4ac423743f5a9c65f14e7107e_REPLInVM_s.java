 package ash.repl;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 
 import ash.compiler.Compiler;
 import ash.lang.BasicType;
 import ash.parser.Parser;
 import ash.vm.VM;
 import ash.vm.VMFrame;
 import bruce.common.utils.CommonUtils;
 
 public final class REPLInVM {
 
 	public static void main(String[] args) {
 		if (VMFrame.debugging) {
 			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			VM vm = new VM();
 
 			while (true) {
 				try {
 					String readIn;
 					System.out.print("> ");
 					readIn = br.readLine();
 					if (readIn == null) break;
 					if (!CommonUtils.isStringNullOrWriteSpace(readIn)) {
 						Object val = vm.batchRunInMain(Compiler.batchCompile(Parser.split(readIn)));
 						System.out.println(BasicType.asString(val));
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 					return;
 				}
 			}
 		} else
			new VM().batchRunInMain(Compiler.batchCompile(Parser.split("(load \"repl.scm\")")));
 	}
 }
