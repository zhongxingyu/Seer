 //JAVA
 import java.io.InputStream;
 //AVAJ
 
 class GtScriptRunner {
 	public static String LoadFile(String Path) {
 		if(LangDeps.HasFile(Path)) {
 			return LangDeps.LoadFile(Path);
 		}
 		return null;
 	}
 	public static String ExecuteScript(String Path, String Target) {
 		/*local*/String[] cmd = {"java", "-jar", "GreenTea.jar", "--" + Target, Path};
 		/*local*/String Result = "";
 		try {
 			/*local*/Process proc = new ProcessBuilder(cmd).start();
			proc.wait(5);
 			if(proc.exitValue() != 0) {
 				return null;
 			}
 			/*local*/InputStream stdout = proc.getInputStream();
 			/*local*/byte[] buffer = new byte[512];
 			/*local*/int read = 0;
 			while(read > -1) {
 				read = stdout.read(buffer, 0, buffer.length);
 				if(read > -1) {
 					Result += new String(buffer, 0, read);
 				}
 			}
 		} catch (Exception e) {
 			throw new RuntimeException();
 		}
 		return Result;
 	}
 
 	public static void Test(String Target, String ScriptPath, String ResultPath) {
 		/*local*/String Expected = LoadFile(ResultPath);
 		/*local*/String Actual   = ExecuteScript(ScriptPath, Target);
 		LangDeps.Assert(Expected.equals(Actual));
 	}
 }
 
 public class GreenTeaScriptTest {
 	final static void TestToken(GtContext Context, String Source, String[] TokenTestList) {
 		/*local*/GtNameSpace NameSpace = Context.DefaultNameSpace;
 		/*local*/TokenContext TokenContext = new TokenContext(NameSpace, Source, 1);
 		/*local*/int i = 0;
 		while(i < TokenTestList.length) {
 			/*local*/String TokenText = TokenTestList[i];
 			LangDeps.Assert(TokenContext.MatchToken(TokenText));
 			i = i + 1;
 		}
 	}
 
 	public static final GtContext CreateContext() {
 		/*local*/String CodeGeneratorName = "Java";
 		/*local*/CodeGenerator Generator = LangDeps.CodeGenerator(CodeGeneratorName);
 		return new GtContext(new KonohaGrammar(), Generator);
 	}
 
 	public static final void TokenizeOperator0() {
 		GtContext Context = CreateContext();
 		/*local*/String[] TokenTestList0 = {"1", "||", "2"};
 		TestToken(Context, "1 || 2", TokenTestList0);
 
 		/*local*/String[] TokenTestList1 = {"1", "==", "2"};
 		TestToken(Context, "1 == 2", TokenTestList1);
 
 		/*local*/String[] TokenTestList2 = {"1", "!=", "2"};
 		TestToken(Context, "1 != 2", TokenTestList2);
 
 		/*local*/String[] TokenTestList3 = {"1", "*", "=", "2"};
 		TestToken(Context, "1 *= 2", TokenTestList3);
 
 		/*local*/String[] TokenTestList4 = {"1", "=", "2"};
 		TestToken(Context, "1 = 2", TokenTestList4);
 	}
 	
 	public static final void TokenizeStatement() {
 		GtContext Context = CreateContext();
 		/*local*/String[] TokenTestList0 = {"int", "+", "(", "int", "x", ")", ";"};
 		TestToken(Context, "int + (int x);", TokenTestList0);
 	}
 
 	public static void main(String[] args) {
 		if(args.length != 3) {
 			TokenizeOperator0();
 			TokenizeStatement();
 		} else {
 			GtScriptRunner.Test(args[0], args[1], args[2]);
 		}
 	}
 }
