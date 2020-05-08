 package SELMA;
 
 import SELMA.SELMATree.SR_Kind;
 import SELMA.SELMATree.SR_Type;
 import SELMA.SELMATree.SR_Func;
 
 public class CompilerEntry extends CheckerEntry {
 	public int addr;
     public int val;
     public String signature;
 
 	public CompilerEntry(SR_Type type, SR_Kind kind, int addr) {
 		super(type, kind);
 		this.addr = addr;
 	}
 
     public CompilerEntry(SR_Type type, SR_Kind kind, int addr, SR_Func func) {
         super(type, kind, func);
         this.addr = addr;
     }
 
     public CompilerEntry setVal(String intval) {
         val = Integer.parseInt(intval);
         return this;
     }
 
     public CompilerEntry setBool(String bool) {
         val = bool.equals("true") ? 1 : 0;
         return this;
     }
 
    public CompilerEntry setChar(int c) {
        val = c;
        // System.err.println("Setting " + c + " on " + addr + " = " + val);
         return this;
     }
 }
