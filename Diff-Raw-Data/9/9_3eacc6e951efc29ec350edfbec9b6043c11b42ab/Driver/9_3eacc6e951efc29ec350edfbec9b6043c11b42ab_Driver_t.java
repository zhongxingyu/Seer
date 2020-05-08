 package cs4120.der34dlc287lg342.xi;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.StringReader;
 
 import cs4120.der34dlc287lg342.xi.ast.AbstractSyntaxTree;
 import cs4120.der34dlc287lg342.xi.ir.Seq;
 import cs4120.der34dlc287lg342.xi.ir.context.IRContextStack;
 import cs4120.der34dlc287lg342.xi.ir.context.InvalidIRContextException;
 import cs4120.der34dlc287lg342.xi.ir.translate.ConstantFolding;
 import cs4120.der34dlc287lg342.xi.ir.translate.IRTranslation;
 import cs4120.der34dlc287lg342.xi.ir.translate.LowerCjump;
 import cs4120.der34dlc287lg342.xi.typechecker.InvalidXiTypeException;
 import cs4120.der34dlc287lg342.xi.typechecker.XiTypechecker;
 
 import edu.cornell.cs.cs4120.xi.AbstractSyntaxNode;
 import edu.cornell.cs.cs4120.xi.CompilationException;
 import edu.cornell.cs.cs4120.xi.parser.Parser;
 
 public class Driver {
 	public static void main(String[] args){
 		AbstractSyntaxTree.PA3 = false;
 		boolean PA4 = false;
 		boolean optimization = true;
 		String file = null;
 		for (String arg : args){
 			if (arg.equals("--dump_ast")){
 				AbstractSyntaxTree.PA3 = true;
 			} else if (arg.equals("--dump_ir")){
 				PA4 = true;
 			} else if (arg.equals("-O")){
 				optimization = false;
 			} else {
 				if (file == null)
 					file = arg;
 				else
 					System.out.println("Ignoring extraneous argument: "+arg);
 			}
 		}
 		
 		if ((!AbstractSyntaxTree.PA3 && !PA4) || file == null){
 			System.out.println("Usage: java -jar Driver.jar [-O] [--dump_ast | --dump_ir] sourcefile.xi");
 			return;
 		}
 		
 		try {
 			FileReader reader = new FileReader(file);
 			String src = "";
 			BufferedReader input =  new BufferedReader(reader);
 			String line = null;
 			while (( line = input.readLine()) != null){
 		          src += line + "\n";
 		    }
			Parser parser = new XiParser(new StringReader(src), file);
 			AbstractSyntaxNode program = parser.parse();
 			XiTypechecker tc = new XiTypechecker(program, src);
 			
 			tc.typecheck();
 			((AbstractSyntaxTree)(tc.ast)).foldConstants();
 			if (AbstractSyntaxTree.PA3){
 				System.out.println("Printing out the AST\n");
 				TypeAnnotatedTreePrinter printer = new TypeAnnotatedTreePrinter(System.out);
 				printer.print(program);
 			}
 			
 			if (PA4){
 				System.out.println("Printing out the IR code\n");
 				IRTranslation tr = ((AbstractSyntaxTree)tc.ast).to_ir(new IRContextStack());
 				Seq program_ir = tr.stmt().lower();
 				LowerCjump lcj = new LowerCjump(program_ir);
 				program_ir = lcj.translate();
 				if (optimization){
 					program_ir = ConstantFolding.foldConstants(program_ir);
 				}
 				
 				System.out.println(program_ir.prettyPrint());
 			}
 			
 			reader.close();
 		} catch (CompilationException e){
 			System.out.println(e);
 		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("File not found: "+file);
 		} catch (IOException e) {
			System.out.println("Malformed file: "+file);
 		} catch (InvalidXiTypeException e) {
 			e.printStackTrace();
 			System.out.println(e);
 		} catch (InvalidIRContextException e) {
 			e.printStackTrace();
 			System.out.println(e);
 		}
 	}
 }
