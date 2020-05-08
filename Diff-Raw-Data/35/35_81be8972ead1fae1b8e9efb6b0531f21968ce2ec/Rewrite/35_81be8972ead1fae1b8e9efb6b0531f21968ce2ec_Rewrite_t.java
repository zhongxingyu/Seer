 package cs4120.der34dlc287lg342.xi.cfg;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Hashtable;
 
 import cs4120.der34dlc287lg342.xi.assembly.Assembly;
 import cs4120.der34dlc287lg342.xi.assembly.MOVE;
 import cs4120.der34dlc287lg342.xi.assembly.OPER;
 import cs4120.der34dlc287lg342.xi.ir.context.TempRegister;
 
 public class Rewrite {
 	ArrayList<Assembly> instrs; 
 	HashSet<TempRegister> spills;
 	private int locals;
 	public Rewrite(ArrayList<Assembly> instrs, HashSet<TempRegister> spills, int locals){
 		this.instrs = instrs;
 		this.spills = spills;
 		this.locals = locals;
 	}
 	
 	public void replace(TempRegister src[], TempRegister o, TempRegister n){
 		for (int i = 0; i < src.length; i ++){
 			if (src[i] != null && src[i].equals(o)){
 				src[i] = n;
 				return;
 			}
 		}
 	}
 	
 	public ArrayList<Assembly> rewrite(){
 		ArrayList<Assembly> new_instrs = new ArrayList<Assembly>();
 		Hashtable<TempRegister, Integer> map = new Hashtable<TempRegister, Integer>();
 		int i = 1;
 		for (TempRegister r : spills){
			int next = -(locals+(i++))*8;
 			map.put(r, next);
 		}
 		
 		// check each instruction
 		for (Assembly asm : instrs){
 			if (asm instanceof MOVE){
 				// simple case
 				MOVE mov = (MOVE)asm;
 				if (spills.contains(mov.src)){
 					// add a fetch
 					TempRegister d = new TempRegister();
 					new_instrs.add(new OPER("movq "+map.get(mov.src)+"(%rbp), %d0", new TempRegister[]{}, d));
 					mov.src = d;
 				}
 				new_instrs.add(mov);
 				if (spills.contains(mov.dest)){
 					// add a store
 					TempRegister s = new TempRegister();
 					new_instrs.add(new OPER("movq %s0, "+map.get(mov.dest)+"(%rbp)", s, null));
 					mov.dest = s;
 				}
 			} else if (asm instanceof OPER){
 				OPER oper = (OPER)asm;
				TempRegister s = null;
 				for (TempRegister u : oper.use()){
 					if (spills.contains(u)){
 						// add a fetch
 						TempRegister d = new TempRegister();
						if (u.equals(oper.dest))
							s = d;
 						new_instrs.add(new OPER("movq "+map.get(u)+"(%rbp), %d0", new TempRegister[]{}, d));
 						replace(oper.src, u, d);
 					}
 				}
 				new_instrs.add(oper);
 				if (spills.contains(oper.dest)){
 					// add a store
					if (s == null)
						s = new TempRegister();
 					new_instrs.add(new OPER("movq %s0, "+map.get(oper.dest)+"(%rbp)", s, null));
 					oper.dest = s;
 				}
 			} else {
 				new_instrs.add(asm);
 			}
 		}
 		
 		return new_instrs;
 	}
 }
