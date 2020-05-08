 package cs4120.der34dlc287lg342.xi.assembly;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 
 import cs4120.der34dlc287lg342.xi.ir.context.TempRegister;
 
 public class RegAlloc {
 	public static String allocate(ArrayList<Assembly> instrs, Hashtable<TempRegister, Integer> coloring){
 		String str = "";
 		for (Assembly asm : instrs){
			str += asm.simple_assem(coloring) + "\n";
 		}
 		return str;
 	}
 }
