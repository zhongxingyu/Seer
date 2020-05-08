 package com.lemoulinstudio.gfa.core.cpu;
 
 import com.lemoulinstudio.gfa.core.memory.MemoryInterface;
 
 public class Arm7TdmiGen2 extends Arm7Tdmi {
 
   public InstructionDecoder decoder;
 
   public Arm7TdmiGen2() {
     super();
   }
 
   @Override
   public void connectToMemory(MemoryInterface memory) {
     super.connectToMemory(memory);
     decoder = new InstructionDecoder(allRegisters, memory);
   }
   
   @Override
   public String disassembleInstruction(int offset, ExecutionState executionState) {
     if (executionState == ExecutionState.Thumb)
       return decoder.decodeThumbInstruction(memory.directLoadHalfWord(offset)).disassemble(offset);
     else
       return decoder.decodeArmInstruction(memory.directLoadWord(offset)).disassemble(offset);
   }
   
   public void step() {
     int instructionTime;
 
     // Handle IRQ
     if (!CPSR.isBitSet(iFlagBit) &&
	(memory.loadByte(REG_IME_Address) != 0) &&
 	((memory.loadHalfWord(REG_IE_Address) & memory.loadHalfWord(REG_IF_Address)) != 0))
	//(memory.loadHalfWord(REG_IF_Address) != 0))
     {
      //System.out.println("IRQ in (PC = " + PC + ")");
       getRegister(14, irqModeBits).set(PC.get() + 4); // LR <- PC + 4
       SPSR_irq.set(CPSR);     // SPSR_irq <- CPSR
       setMode(irqModeBits);   // CPSR changed to mode irq
       PC.set(irqVectorAddress);
       setArmState();          // interrupt handler is written in armState asm.
       CPSR.setOn(iFlagBit);   // inhibe interrupts
       instructionTime = 4;    // inaccurate
      //System.out.println("IRQ out");
      //stopPlease();
     }
     
     else {
       ExecutionState executionState = getExecutionState();
       
       if (executionState == ExecutionState.Thumb) {
         short opcode = memory.directLoadHalfWord(PC.get());
         PC.add(executionState.getInstructionSize());
         decoder.decodeThumbInstruction(opcode).execute();
 
         // Todo: Fix this arbitrary number.
         instructionTime = 2;
       }
       else {
         int opcode = memory.directLoadWord(PC.get());
         PC.add(executionState.getInstructionSize());
         decoder.decodeArmInstruction(opcode).execute();
         
         // Todo: Fix this arbitrary number.
         instructionTime = 4;
       }
     }
 
     time.addTime(instructionTime);
   }
 
 }
