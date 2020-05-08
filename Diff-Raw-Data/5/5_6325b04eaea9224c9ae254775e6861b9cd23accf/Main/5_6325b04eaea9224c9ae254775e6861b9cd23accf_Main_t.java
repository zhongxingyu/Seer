 package MiniRE;
 import MiniRE.RecursiveDescent.MiniParser;
 import MiniRE.VirtualMachine.MiniVM;
 
 
 public class Main 
 {
 	public static void main(String[] args) throws Exception 
 	{
		String re = args[0];
		
		System.out.println(re);
		
 		MiniVM mvm = new MiniVM();
 		MiniParser mp = new MiniParser();
 		AST tree = mp.parseFile(re);
 		mvm.run(tree);
 	}
 }
