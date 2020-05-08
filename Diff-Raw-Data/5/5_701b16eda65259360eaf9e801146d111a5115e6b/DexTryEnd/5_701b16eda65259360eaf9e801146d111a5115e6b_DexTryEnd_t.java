 package uk.ac.cam.db538.dexter.dex.code.elem;
 
import java.util.Collections;
import java.util.Set;

 import lombok.Getter;
import uk.ac.cam.db538.dexter.dex.code.InstructionList;
 
 public class DexTryEnd extends DexCodeElement {
 
   @Getter private final int id;
 	
   public DexTryEnd(int id) {
     this.id = id;
   }
 
   @Override
   public String toString() {
     return "TRYEND" + Integer.toString(this.getId());
   }
 
   @Override
   public boolean cfgEndsBasicBlock() {
     return true;
   }
 
   // The following methods fix a problem of DexTryEnd not having
   // a successor if it is the last element in the instruction list. 
   // This way, it has no successors and also does not exit the method, which
   // creates an isolated CFG basic block, connected only to the EXIT block.
 
   @Override
   protected Set<? extends DexCodeElement> cfgJumpTargets(InstructionList code) {
 	  if (code.isLast(this))
 		  return Collections.emptySet();
 	  else
 		  return super.cfgJumpTargets(code);
   }
 
   @Override
   public boolean cfgExitsMethod(InstructionList code) {
 	  return false;
   }
 }
