 // Copyright © 2012 Steve McCoy under the MIT license.
 package edu.unh.cs.tact;
 
 import java.util.*;
 import org.apache.bcel.*;
 import org.apache.bcel.generic.*;
 
 import static edu.unh.cs.tact.Util.*;
 
 class BasicBlock{
 	public final ConstantPoolGen cp;
 	public final InstructionFactory f;
 	public final InstructionList list;
 	public final InstructionHandle begin, end;
 
 	public BasicBlock(ConstantPoolGen cp, InstructionFactory f, InstructionList list){
 
 		this.f = notNull(f, "f");
 		this.list = notNull(list, "list");
 		this.begin = list.getStart();
 		this.end = list.getEnd();
 		this.cp = notNull(cp, "cp");
 	}
 
 	public boolean insertChecks(){
 		boolean changed = false;
 		InstructionHandle end = this.end.getNext();
 		for(InstructionHandle h = begin; h != end; h = h.getNext()){
 			Instruction code = h.getInstruction();
			if(code instanceof FieldInstruction){
 				FieldInstruction pf = (FieldInstruction)code;
 				if(pf.getType(cp).getSize() == 2){
 					insertCheck64(h);
 				}else{
 					insertCheck32(h);
 				}
 				//TODO: insertCheck(h.getPrev()); for trivial assignments
 				changed = true;
 			}/*else if(code instanceof NEW){
 				insertCheck(h.getNext());
 				changed = true;
 			}*/
 		}
 
 		return changed;
 	}
 
 	private void insertCheck32(InstructionHandle pf){
 		list.insert(pf, new SWAP());
 		list.insert(pf, f.createDup(1));
 		list.insert(
 			pf,
 			f.createInvoke(
 				"edu.unh.cs.tact.Checker",
 				"check",
 				Type.VOID,
 				new Type[]{ Type.OBJECT },
 				Constants.INVOKESTATIC
 			)
 		);
 		list.insert(pf, new SWAP());
 	}
 
 	private void insertCheck64(InstructionHandle pf){
 
 	}
 
 	private void insertCheck(InstructionHandle h){
 		InstructionHandle chk = list.insert(
 			h,
 			f.createInvoke(
 				"edu.unh.cs.tact.Checker",
 				"check",
 				Type.VOID,
 				new Type[]{ Type.OBJECT },
 				Constants.INVOKESTATIC
 			));
 		list.insert(
 			chk,
 			f.createDup(1) // it's a ref, hope 1 is good enough
 		);
 	}
 }
