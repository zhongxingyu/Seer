 package descent.tests.dstress;
 
import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileWriter;
import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 public class DstressTestGenerator extends DstressTestGeneratorBase {
 	
 	public static void main(String[] args) throws Exception {
 		generateCompile();
		generateNoCompile();
 	}
 	
 	private static void generateCompile() throws Exception {
 		StringBuilder sb = new StringBuilder();
 		sb.append("package descent.tests.dstress;\r\n" + 
 				"\r\n" + 
 				"import java.io.File;\r\n" + 
 				"import java.io.FileReader;\r\n" + 
 				"\r\n" + 
 				"import descent.core.dom.AST;\r\n" + 
 				"import descent.core.dom.CompilationUnitResolver;\r\n" + 
 				"import descent.internal.compiler.parser.Module;\r\n" + 
 				"import descent.internal.compiler.parser.Parser;\r\n" + 
 				"import descent.tests.mars.Parser_Test;\r\n\r\n");
 		sb.append("public class DstressCompile_Test extends Parser_Test implements IDstressConfiguration {\r\n");
 		sb.append("\r\n");
 		
 		List<File> compile = new ArrayList<File>();
 		compile.addAll(listRecursive(new File(DSTRESS_PATH, "compile")));
 		compile.addAll(listRecursive(new File(DSTRESS_PATH, "run")));
 		compile.addAll(listRecursive(new File(DSTRESS_PATH, "norun")));
 		for(File file : compile) {
 			
 			String name = file.getName();
 			if (failures.contains(name)) {
 				continue;
 			}
 			
 			sb.append("\tpublic void test_")
 				.append(file.getName().replace('.', '_'))
 				.append("() throws Exception {\r\n");
 			sb.append("\t\tcompile(\"").append(file.getAbsolutePath().replace("\\", "\\\\")).append("\");\r\n");
 			sb.append("\t}\r\n\r\n");
 		}
 		
 		sb.append("\tprivate void compile(String file) throws Exception {\r\n");
 		sb.append("\t\tchar[] source = getContents(new File(file));\r\n");
 		sb.append("\t\tParser parser = new Parser(AST.D1, source);\r\n"); 
 		sb.append("\t\tModule module = parser.parseModuleObj();\r\n"); 
 		sb.append("\t\tCompilationUnitResolver.resolve(module, false);\r\n");
 		sb.append("\t\tif (!module.problems.isEmpty()) {\r\n");
 		sb.append("\t\t\tfail(module.problems.toString());\r\n");
 		sb.append("\t\t}\r\n");
 		sb.append("\t}\r\n\r\n");
 		
 		sb.append(
 				"\tprivate char[] getContents(File file) throws Exception {\r\n" + 
 				"\t\tchar[] contents = new char[(int) file.length()];\r\n" + 
 				"\t\tFileReader r = new FileReader(file);\r\n" + 
 				"\t\tr.read(contents);\r\n" + 
 				"\t\tr.close();\r\n" + 
 				"\t\treturn contents;\r\n" + 
 				"\t}\r\n");
 		
 		sb.append("\r\n");
 		sb.append("}\r\n");
 		
 		FileWriter fw = new FileWriter("descent/tests/dstress/DstressCompile_Test.java");
 		fw.write(sb.toString());
 		fw.close();
 	}
 	
 	private static void generateNoCompile() throws Exception {
 		StringBuilder sb = new StringBuilder();
 		sb.append("package descent.tests.dstress;\r\n" + 
 				"\r\n" + 
 				"import java.io.File;\r\n" + 
 				"import java.io.FileReader;\r\n" + 
 				"\r\n" + 
 				"import descent.core.dom.AST;\r\n" + 
 				"import descent.core.dom.CompilationUnitResolver;\r\n" + 
 				"import descent.internal.compiler.parser.Module;\r\n" + 
 				"import descent.internal.compiler.parser.Parser;\r\n" + 
 				"import descent.tests.mars.Parser_Test;\r\n\r\n");
 		sb.append("public class DstressNoCompile_Test extends Parser_Test implements IDstressConfiguration {\r\n");
 		sb.append("\r\n");
 		
 		List<File> compile = new ArrayList<File>();
 		compile.addAll(listRecursive(new File(DSTRESS_PATH, "nocompile")));
 		for(File file : compile) {
 			
 			String name = file.getName();
 			if (failures.contains(name)) {
 				continue;
 			}
 			
 			sb.append("\tpublic void test_")
 				.append(file.getName().replace('.', '_'))
 				.append("() throws Exception {\r\n");
			sb.append("\t\tnocompile(");
			sb.append("\"").append(file.getAbsolutePath().replace("\\", "\\\\")).append("\"");
			sb.append(", ");
			sb.append(errors(file));
			sb.append(");\r\n");
 			sb.append("\t}\r\n\r\n");
 		}
 		
		sb.append("\tprivate void nocompile(String file, int expectedErrors) throws Exception {\r\n");
 		sb.append("\t\tchar[] source = getContents(new File(file));\r\n");
 		sb.append("\t\tParser parser = new Parser(AST.D1, source);\r\n"); 
 		sb.append("\t\tModule module = parser.parseModuleObj();\r\n"); 
 		sb.append("\t\tCompilationUnitResolver.resolve(module, false);\r\n");
		sb.append("\t\tif (module.problems.size() != expectedErrors) {\r\n");
		sb.append("\t\t\tfail(\"Expected \" + expectedErrors + \" errors but were \" + module.problems.size() + \": \" + module.problems.toString());\r\n");
 		sb.append("\t\t}\r\n");
 		sb.append("\t}\r\n\r\n");
 		
 		sb.append(
 				"\tprivate char[] getContents(File file) throws Exception {\r\n" + 
 				"\t\tchar[] contents = new char[(int) file.length()];\r\n" + 
 				"\t\tFileReader r = new FileReader(file);\r\n" + 
 				"\t\tr.read(contents);\r\n" + 
 				"\t\tr.close();\r\n" + 
 				"\t\treturn contents;\r\n" + 
 				"\t}\r\n");
 		
 		sb.append("\r\n");
 		sb.append("}\r\n");
 		
 		FileWriter fw = new FileWriter("descent/tests/dstress/DstressNoCompile_Test.java");
 		fw.write(sb.toString());
 		fw.close();
 	}
 	
 	private static List<File> listRecursive(File dir) {
 		List<File> files = new ArrayList<File>();
 		listRecursive(dir, files);		
 		return files;
 	}
 
 	private static void listRecursive(File dir, List<File> acumulator) {
 		File[] subFiles = dir.listFiles();
 		if (subFiles == null) {
 			return;
 		}
 		
 		for(File subFile : subFiles) {
 			if (subFile.isDirectory()) {
 				listRecursive(subFile, acumulator);
 			} else if (subFile.isFile() && subFile.getName().endsWith(".d")
 					// Remove this condition when asm is supported by Descent
 					&& !subFile.getName().startsWith("asm_")
 					) {
 				acumulator.add(subFile);
 			}
 		}
 	}
	
	private static int errors(File file) throws Exception {
		Process process = Runtime.getRuntime().exec("dmd " + file.getAbsolutePath() + " -o-");
		InputStreamReader reader = new InputStreamReader(process.getInputStream());
		BufferedReader br = new BufferedReader(reader);
		
		int count = 0;
		while(br.readLine() != null) {
			count++;
		}
		br.close();
		return count;
	}
 
 }
