 package com.lemoulinstudio.gfa.core.cpu.instruction;
 
 import com.lemoulinstudio.gfa.core.cpu.ArmReg;
 import com.lemoulinstudio.gfa.core.memory.MemoryInterface;
 
 public abstract class ArmStateMove extends ArmStateDataProcessing {
 
   public ArmStateMove(ArmReg[][] regs, MemoryInterface memory) {
     super(regs, memory);
   }
 
   public void execute() {
     if (!isPreconditionSatisfied()) return;
     
     tmpCPSR.set(CPSR);
     
     int operand2;
     destinationRegister = getRegister((opcode & DestinationMask) >>> 12);
     
     if ((opcode & ImmediateBit) == 0) { // operand 2 is a register
       ArmReg rm = getRegister(opcode & Op2RegisterMask);
       operand2 = rm.get();
       if (rm == PC) operand2 += 4;
       
       int shiftType = opcode & ShiftTypeMask;
       int shiftAmount;
       
       if ((opcode & ShiftModeBit) == 0) { // The shift amount is a value.
         shiftAmount = (opcode & ShiftAmountMask) >>> 7;
 	
 	if (shiftAmount == 0) {
           if (shiftType == LogicalLeftBits) {
             // Do nothing : the old cFlagBit must be conserved.
             // Ne fait rien : l'ancien cFlagBit doit etre conserve.
 	  }
 	  else if (shiftType == LogicalRightBits) {
             tmpCPSR.setBit(cFlagBit, ((operand2 & 0x80000000) != 0));
 	    operand2 = 0;
 	  }
 	  else if (shiftType == ArithmRightBits) {
             tmpCPSR.setBit(cFlagBit, ((operand2 & 0x80000000) != 0));
             // fill all with the bit 31 of operand2.
 	    operand2 >>= 31;
 	  }
 	  else if (shiftType == RotateRightBits) {
             boolean isCFlagBit = tmpCPSR.isBitSet(cFlagBit);
 	    tmpCPSR.setBit(cFlagBit, ((operand2 & 0x00000001) != 0));
 	    operand2 >>>= 1;
 	    if (isCFlagBit) operand2 |= 0x80000000;
 	  }
 	}
 	else {
           if (shiftType == LogicalLeftBits) {
             tmpCPSR.setBit(cFlagBit, ((operand2 & (1 << (32 - shiftAmount))) != 0));
 	    operand2 <<= shiftAmount;
 	  }
 	  else if (shiftType == LogicalRightBits) {
             tmpCPSR.setBit(cFlagBit, ((operand2 & (1 << (shiftAmount - 1))) != 0));
 	    operand2 >>>= shiftAmount;
 	  }
 	  else if (shiftType == ArithmRightBits) {
             tmpCPSR.setBit(cFlagBit, ((operand2 & (1 << (shiftAmount - 1))) != 0));
 	    operand2 >>= shiftAmount;
 	  }
 	  else if (shiftType == RotateRightBits) {
             operand2 = (operand2 >>> shiftAmount) | (operand2 << (32 - shiftAmount));
 	    tmpCPSR.setBit(cFlagBit, ((operand2 & 0x80000000) != 0));
 	  }
 	}
       }
       else { // Register specified shift amount.
         ArmReg regToShift = getRegister((opcode & ShiftRegisterMask) >>> 8);
 	shiftAmount = regToShift.get();
 	if (shiftAmount < 0) shiftAmount = (shiftAmount & 0x0000001f) | 64; // avoid the case of a negative value.
 	if (regToShift == PC) shiftAmount += 8;
 	
 	if (shiftAmount == 0)
           ; // Ne rien faire : conservation de l'ancien cFlag;
 	else if (shiftAmount < 32) {
           if (shiftType == LogicalLeftBits) {
             tmpCPSR.setBit(cFlagBit, ((operand2 & (1 << (32 - shiftAmount))) != 0));
 	    operand2 <<= shiftAmount;
 	  }
 	  else if (shiftType == LogicalRightBits) {
             tmpCPSR.setBit(cFlagBit, ((operand2 & (1 << (shiftAmount - 1))) != 0));
 	    operand2 >>>= shiftAmount;
 	  }
 	  else if (shiftType == ArithmRightBits) {
             tmpCPSR.setBit(cFlagBit, ((operand2 & (1 << (shiftAmount - 1))) != 0));
 	    operand2 >>= shiftAmount;
 	  }
 	  else if (shiftType == RotateRightBits) {
             operand2 = (operand2 >>> shiftAmount) | (operand2 << (32 - shiftAmount));
 	    tmpCPSR.setBit(cFlagBit, ((operand2 & 0x80000000) != 0));
 	  }
 	}
 	else if (shiftAmount == 32) {
           if (shiftType == LogicalLeftBits) {
 	    tmpCPSR.setBit(cFlagBit, ((operand2 & 0x00000001) != 0));
 	    operand2 = 0;
 	  }
 	  else if (shiftType == LogicalRightBits) {
             tmpCPSR.setBit(cFlagBit, ((operand2 & 0x80000000) != 0));
 	    operand2 = 0;
 	  }
 	  else if (shiftType == ArithmRightBits) {
             tmpCPSR.setBit(cFlagBit, ((operand2 & 0x80000000) != 0));
 	    operand2 >>= 31;
 	  }
 	  else if (shiftType == RotateRightBits) {
             tmpCPSR.setBit(cFlagBit, ((operand2 & 0x80000000) != 0));
           }
 	}
 	else if (shiftAmount > 32) {
 	  if (shiftType == LogicalLeftBits) {
             tmpCPSR.setOff(cFlagBit);
 	    operand2 = 0;
 	  }
 	  else if (shiftType == LogicalRightBits) {
             tmpCPSR.setOff(cFlagBit);
 	    operand2 = 0;
 	  }
 	  else if (shiftType == ArithmRightBits) {
             tmpCPSR.setBit(cFlagBit, ((operand2 & 0x80000000) != 0));
 	    operand2 >>= 31;
 	  }
 	  else if (shiftType == RotateRightBits) {
 	    shiftAmount = ((shiftAmount - 1) & 0x0000001f) + 1; // put shiftAmount in the range [1..32]
 	    operand2 = (operand2 >>> shiftAmount) | (operand2 << (32 - shiftAmount));
 	    tmpCPSR.setBit(cFlagBit, ((operand2 & 0x80000000) != 0));
 	  }
 	}
       }
     }
     else { // operand 2 is an immediate value
       int immediateValue = opcode & Op2ImmediateMask;
       int twiceRotateValue = ((opcode & Op2RotateMask) >>> 8) * 2;
       operand2 = (immediateValue >>> twiceRotateValue) | (immediateValue << (32 - twiceRotateValue));
     }
     
     applyOperation(operand2);
     
     if ((opcode & SetConditionBit) != 0) {
      if (destinationRegister == PC) {
        ArmReg spsr = getSPSR();
        if (spsr != null)
          CPSR.set(spsr);
      }
       else
         CPSR.set(tmpCPSR);
     }
   }
 
   abstract protected void applyOperation(int operand2);
   protected void applyOperation(int operand1, int operand2){}
 
   public String disassemble(int offset) {
     int opcode = getOpcode(offset);
     String instru = getInstructionName() + preconditionToString(opcode);
     if ((opcode & SetConditionBit) != 0) instru += "s";
     String rd = getRegisterName((opcode & DestinationMask) >>> 12);
     String op2 = disassembleOp2(opcode);
     
     return instru + " " + rd + ", " + op2;
   }
 
   abstract protected String getInstructionName();
 
 }
