 package cs4120.der34dlc287lg342.xi.tiles;
 
 import java.util.ArrayList;
 
 import cs4120.der34dlc287lg342.xi.assembly.Assembly;
 import cs4120.der34dlc287lg342.xi.assembly.OPER;
 import cs4120.der34dlc287lg342.xi.ir.context.TempRegister;
 
 public class ModTile extends BinopTile {
 	public ModTile(Tile left, Tile right){
 		super(left, right);
 	}
 	
 	public ArrayList<Assembly> att(){
 		ArrayList<Assembly> asm = new ArrayList<Assembly>();
 		//TempRegister a = new TempRegister(), d = new TempRegister();
 		asm.addAll(left.att());
 		asm.addAll(right.att());
 
 		asm.add(new OPER("movq %s0, %rax", left.out, null));
		asm.add(new OPER("cqto", new TempRegister[]{}, null)); // wtf is this anyways?
 
 		asm.add(new OPER("idivq %s0", right.out, null));
 		out = new TempRegister();
 		asm.add(new OPER("movq %rdx, %d0", new TempRegister[]{}, out));
 		
 		return asm;
 	}
 }
