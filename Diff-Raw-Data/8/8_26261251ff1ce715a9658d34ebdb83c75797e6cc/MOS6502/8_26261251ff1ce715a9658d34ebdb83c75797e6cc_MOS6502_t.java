 package org.nesjs.core;
 
 
 
 /**
  * MOS Technology 6502 core
  *
  * Custom sound hardware and a restricted DMA controller on-die
  * 
  * Runs at 1.79 MHz
  * 8 bit, little-endian
  * 16 bit address bus
  *
  * Registers a: accumulator
  *           x, y: index registers
  *           p: processor status
  */
 public final class MOS6502
 {
     private static final int NMI_VECTOR   = 0xFFFA;
     private static final int RESET_VECTOR = 0xFFFC;
     private static final int IRQ_VECTOR   = 0xFFFE;
     
     private int a, x, y;
     private int s, pc;    
     private int carry, not_zero, interruptDisable, decimal, overflow, negative;
     private boolean resetIRQ;
     
     private Memory memory;
 
     public MOS6502(Memory aMemory)
     {
     	memory = aMemory;    	
     	memory.resetLowMemory();
     	
     	a = 0;
         x = 0;
         y = 0;
 
         // flags
         carry            = 0;
         not_zero         = 1;
         interruptDisable = 1;
         decimal          = 0;
         overflow         = 0;
         negative         = 0;
         
         resetIRQ = false;
         
         s = 0x01FF - 2;
         pc = readWord(RESET_VECTOR);
     }
     
     public final void reset()
     {
         interruptDisable = 1;
         resetIRQ = true;       
     }
     
     public final int getRegisterA()
     {
         return a;
     }
     
     public final int getRegisterX()
     {
         return x;
     }
     
     public final int getRegisterY()
     {
         return y;
     }
     
     /**
      * Status Register   7  6  5  4  3  2  1  0
      *                   N  V  U  B  D  I  Z  C
      */
     public final int getRegisterP()
     {
         return getRegisterP(0);
     }
     
     public final int getRegisterS()
     {
         return s;
     }
     
     public final int getRegisterPC()
     {
         return pc;
     }
     
     public final void setRegisterPC(int aPC)
     {
         pc = aPC;
     }
     
     public final void step()
     {
         execute(1);
     }
     
     public final int execute(int aNumberOfCycles)
     {
         int _clocksRemain = aNumberOfCycles;
 
         while(_clocksRemain > 0)
         {
             if(resetIRQ)
             {
                 resetIRQ = false;
                 
                 pc = readWord(RESET_VECTOR);
                 
                 /**
                  * On reset, pc (2 bytes) and p are pushed onto the stack.  But the values should not actually
                  * be written to memory.
                  */
                 s -= 3;
             }
             
             
             _clocksRemain--;
             
             int _opcode = memory.readByte(pc++);
 
             switch(_opcode)
             {
                 case 0x69: opcode_ADC(immediate()); break;
                 case 0x65: opcode_ADC(zeroPage()); break;
                 case 0x75: opcode_ADC(zeroPageX()); break;
                 case 0x6D: opcode_ADC(absolute()); break;
                 case 0x7D: opcode_ADC(absoluteX()); break;
                 case 0x79: opcode_ADC(absoluteY()); break;
                 case 0x61: opcode_ADC(indirectX()); break;
                 case 0x71: opcode_ADC(indirectY()); break;
                 case 0x4B: opcode_ALR(immediate()); break;
                 case 0x0B:
                 case 0x2B: opcode_ANC(immediate()); break;
                 case 0x29: opcode_AND(immediate()); break;
                 case 0x25: opcode_AND(zeroPage()); break;
                 case 0x35: opcode_AND(zeroPageX()); break;
                 case 0x2D: opcode_AND(absolute()); break;
                 case 0x3D: opcode_AND(absoluteX()); break;
                 case 0x39: opcode_AND(absoluteY()); break;
                 case 0x21: opcode_AND(indirectX()); break;
                 case 0x31: opcode_AND(indirectY()); break;
                 case 0x6B: opcode_ARR(immediate()); break;
                 case 0x0A: opcode_ASL_accumulator(); break;
                 case 0x06: opcode_ASL(zeroPage()); break;
                 case 0x16: opcode_ASL(zeroPageX()); break;
                 case 0x0E: opcode_ASL(absolute()); break;
                 case 0x1E: opcode_ASL(absoluteX()); break;
                 case 0xAB: opcode_ATX(immediate()); break;
                 case 0xCB: opcode_AXS(immediate()); break;
                 case 0x90: opcode_BCC_relative(); break;
                 case 0xB0: opcode_BCS_relative(); break;
                 case 0xF0: opcode_BEQ_relative(); break;
                 case 0x24: opcode_BIT(zeroPage()); break;
                 case 0x2C: opcode_BIT(absolute()); break;
                 case 0x30: opcode_BMI_relative(); break;
                 case 0xD0: opcode_BNE_relative(); break;
                 case 0x10: opcode_BPL_relative(); break;
                 case 0x00: opcode_BRK_implied(); break;
                 case 0x50: opcode_BVC_relative(); break;
                 case 0x70: opcode_BVS_relative(); break;
                 case 0x18: opcode_CLC_implied(); break;
                 case 0xD8: opcode_CLD_implied(); break;
                 case 0x58: opcode_CLI_implied(); break;
                 case 0xB8: opcode_CLV_implied(); break;
                 case 0xC9: opcode_CMP(immediate()); break;
                 case 0xC5: opcode_CMP(zeroPage()); break;
                 case 0xD5: opcode_CMP(zeroPageX()); break;
                 case 0xCD: opcode_CMP(absolute()); break;
                 case 0xDD: opcode_CMP(absoluteX()); break;
                 case 0xD9: opcode_CMP(absoluteY()); break;
                 case 0xC1: opcode_CMP(indirectX()); break;
                 case 0xD1: opcode_CMP(indirectY()); break;
                 case 0xE0: opcode_CPX(immediate()); break;
                 case 0xE4: opcode_CPX(zeroPage()); break;
                 case 0xEC: opcode_CPX(absolute()); break;
                 case 0xC0: opcode_CPY(immediate()); break;
                 case 0xC4: opcode_CPY(zeroPage()); break;
                 case 0xCC: opcode_CPY(absolute()); break;
                 case 0xC7: opcode_DCP(zeroPage()); break;
                 case 0xD7: opcode_DCP(zeroPageX()); break;
                 case 0xCF: opcode_DCP(absolute()); break;
                 case 0xDF: opcode_DCP(absoluteX()); break;
                 case 0xDB: opcode_DCP(absoluteY()); break;
                 case 0xC3: opcode_DCP(indirectX()); break;
                 case 0xD3: opcode_DCP(indirectY()); break;
                 case 0xC6: opcode_DEC(zeroPage()); break;
                 case 0xD6: opcode_DEC(zeroPageX()); break;
                 case 0xCE: opcode_DEC(absolute()); break;
                 case 0xDE: opcode_DEC(absoluteX()); break;
                 case 0xCA: opcode_DEX_implied(); break;
                 case 0x88: opcode_DEY_implied(); break;
                 case 0x49: opcode_EOR(immediate()); break;
                 case 0x45: opcode_EOR(zeroPage()); break;
                 case 0x55: opcode_EOR(zeroPageX()); break;
                 case 0x4D: opcode_EOR(absolute()); break;
                 case 0x5D: opcode_EOR(absoluteX()); break;
                 case 0x59: opcode_EOR(absoluteY()); break;
                 case 0x41: opcode_EOR(indirectX()); break;
                 case 0x51: opcode_EOR(indirectY()); break;
                 case 0xE6: opcode_INC(zeroPage()); break;
                 case 0xF6: opcode_INC(zeroPageX()); break;
                 case 0xEE: opcode_INC(absolute()); break;
                 case 0xFE: opcode_INC(absoluteX()); break;
                 case 0xE8: opcode_INX_implied(); break;
                 case 0xC8: opcode_INY_implied(); break;
                 case 0xE7: opcode_ISB(zeroPage()); break;
                 case 0xF7: opcode_ISB(zeroPageX()); break;
                 case 0xEF: opcode_ISB(absolute()); break;
                 case 0xFF: opcode_ISB(absoluteX()); break;
                 case 0xFB: opcode_ISB(absoluteY()); break;
                 case 0xE3: opcode_ISB(indirectX()); break;
                 case 0xF3: opcode_ISB(indirectY()); break;
                 case 0x4C: opcode_JMP(absolute()); break;
                 case 0x6C: opcode_JMP(indirect()); break;
                 case 0x20: opcode_JSR(absolute()); break;
                 case 0xA7: opcode_LAX(zeroPage()); break;
                 case 0xB7: opcode_LAX(zeroPageY()); break;
                 case 0xAF: opcode_LAX(absolute()); break;
                 case 0xBF: opcode_LAX(absoluteY()); break;
                 case 0xA3: opcode_LAX(indirectX()); break;
                 case 0xB3: opcode_LAX(indirectY()); break;
                 case 0xA9: opcode_LDA(immediate()); break;
                 case 0xA5: opcode_LDA(zeroPage()); break;
                 case 0xB5: opcode_LDA(zeroPageX()); break;
                 case 0xAD: opcode_LDA(absolute()); break;
                 case 0xBD: opcode_LDA(absoluteX()); break;
                 case 0xB9: opcode_LDA(absoluteY()); break;
                 case 0xA1: opcode_LDA(indirectX()); break;
                 case 0xB1: opcode_LDA(indirectY()); break;
                 case 0xA2: opcode_LDX(immediate()); break;
                 case 0xA6: opcode_LDX(zeroPage()); break;
                 case 0xB6: opcode_LDX(zeroPageY()); break;
                 case 0xAE: opcode_LDX(absolute()); break;
                 case 0xBE: opcode_LDX(absoluteY()); break;
                 case 0xA0: opcode_LDY(immediate()); break;
                 case 0xA4: opcode_LDY(zeroPage()); break;
                 case 0xB4: opcode_LDY(zeroPageX()); break;
                 case 0xAC: opcode_LDY(absolute()); break;
                 case 0xBC: opcode_LDY(absoluteX()); break;
                 case 0x4A: opcode_LSR_accumulator(); break;
                 case 0x46: opcode_LSR(zeroPage()); break;
                 case 0x56: opcode_LSR(zeroPageX()); break;
                 case 0x4E: opcode_LSR(absolute()); break;
                 case 0x5E: opcode_LSR(absoluteX()); break;
                 case 0xEA:
                 case 0x1A:
                 case 0x3A:
                 case 0x5A:
                 case 0x7A:
                 case 0xDA:
                 case 0xFA: opcode_NOP_implied(); break;
                 case 0x80:
                 case 0x82:
                 case 0x89:
                 case 0xC2:
                 case 0xE2: opcode_NOP(immediate()); break;
                 case 0x04:
                 case 0x44:
                 case 0x64: opcode_NOP(zeroPage()); break;
                 case 0x0C: opcode_NOP(absolute()); break;
                 case 0x1C:
                 case 0x3C:
                 case 0x5C:
                 case 0x7C:
                 case 0xDC:
                 case 0xFC: opcode_NOP(absoluteX()); break;
                 case 0x14:
                 case 0x34:
                 case 0x54:
                 case 0x74:
                 case 0xD4:
                 case 0xF4: opcode_NOP(zeroPageX()); break;
                 case 0x09: opcode_ORA(immediate()); break;
                 case 0x05: opcode_ORA(zeroPage()); break;
                 case 0x15: opcode_ORA(zeroPageX()); break;
                 case 0x0D: opcode_ORA(absolute()); break;
                 case 0x1D: opcode_ORA(absoluteX()); break;
                 case 0x19: opcode_ORA(absoluteY()); break;
                 case 0x01: opcode_ORA(indirectX()); break;
                 case 0x11: opcode_ORA(indirectY()); break;
                 case 0x48: opcode_PHA_implied(); break;
                 case 0x08: opcode_PHP_implied(); break;
                 case 0x68: opcode_PLA_implied(); break;
                 case 0x28: opcode_PLP_implied(); break;
                 case 0x27: opcode_RLA(zeroPage()); break;
                 case 0x37: opcode_RLA(zeroPageX()); break;
                 case 0x2F: opcode_RLA(absolute()); break;
                 case 0x3F: opcode_RLA(absoluteX()); break;
                 case 0x3B: opcode_RLA(absoluteY()); break;
                 case 0x23: opcode_RLA(indirectX()); break;
                 case 0x33: opcode_RLA(indirectY()); break;
                 case 0x2A: opcode_ROL_accumulator(); break;
                 case 0x26: opcode_ROL(zeroPage()); break;
                 case 0x36: opcode_ROL(zeroPageX()); break;
                 case 0x2E: opcode_ROL(absolute()); break;
                 case 0x3E: opcode_ROL(absoluteX()); break;
                 case 0x6A: opcode_ROR_accumulator(); break;
                 case 0x66: opcode_ROR(zeroPage()); break;
                 case 0x76: opcode_ROR(zeroPageX()); break;
                 case 0x6E: opcode_ROR(absolute()); break;
                 case 0x7E: opcode_ROR(absoluteX()); break;
                 case 0x67: opcode_RRA(zeroPage()); break;
                 case 0x77: opcode_RRA(zeroPageX()); break;
                 case 0x6F: opcode_RRA(absolute()); break;
                 case 0x7F: opcode_RRA(absoluteX()); break;
                 case 0x7B: opcode_RRA(absoluteY()); break;
                 case 0x63: opcode_RRA(indirectX()); break;
                 case 0x73: opcode_RRA(indirectY()); break;
                 case 0x40: opcode_RTI_implied(); break;
                 case 0x60: opcode_RTS_implied(); break;
                 case 0x87: opcode_SAX(zeroPage()); break;
                 case 0x97: opcode_SAX(zeroPageY()); break;
                 case 0x8F: opcode_SAX(absolute()); break;
                 case 0x83: opcode_SAX(indirectX()); break;
                 case 0xE9:
                 case 0xEB: opcode_SBC(immediate()); break;
                 case 0xE5: opcode_SBC(zeroPage()); break;
                 case 0xF5: opcode_SBC(zeroPageX()); break;
                 case 0xED: opcode_SBC(absolute()); break;
                 case 0xFD: opcode_SBC(absoluteX()); break;
                 case 0xF9: opcode_SBC(absoluteY()); break;
                 case 0xE1: opcode_SBC(indirectX()); break;
                 case 0xF1: opcode_SBC(indirectY()); break;
                 case 0x38: opcode_SEC_implied(); break;
                 case 0xF8: opcode_SED_implied(); break;
                 case 0x78: opcode_SEI_implied(); break;
                 case 0x07: opcode_SLO(zeroPage()); break;
                 case 0x17: opcode_SLO(zeroPageX()); break;
                 case 0x0F: opcode_SLO(absolute()); break;
                 case 0x1F: opcode_SLO(absoluteX()); break;
                 case 0x1B: opcode_SLO(absoluteY()); break;
                 case 0x03: opcode_SLO(indirectX()); break;
                 case 0x13: opcode_SLO(indirectY()); break;
                 case 0x47: opcode_SRE(zeroPage()); break;
                 case 0x57: opcode_SRE(zeroPageX()); break;
                 case 0x4F: opcode_SRE(absolute()); break;
                 case 0x5F: opcode_SRE(absoluteX()); break;
                 case 0x5B: opcode_SRE(absoluteY()); break;
                 case 0x43: opcode_SRE(indirectX()); break;
                 case 0x53: opcode_SRE(indirectY()); break;
                 case 0x85: opcode_STA(zeroPage()); break;
                 case 0x95: opcode_STA(zeroPageX()); break;
                 case 0x8D: opcode_STA(absolute()); break;
                 case 0x9D: opcode_STA(absoluteX()); break;
                 case 0x99: opcode_STA(absoluteY()); break;
                 case 0x81: opcode_STA(indirectX()); break;
                 case 0x91: opcode_STA(indirectY()); break;
                 case 0x86: opcode_STX(zeroPage()); break;
                 case 0x96: opcode_STX(zeroPageY()); break;
                 case 0x8E: opcode_STX(absolute()); break;
                 case 0x84: opcode_STY(zeroPage()); break;
                 case 0x94: opcode_STY(zeroPageX()); break;
                 case 0x8C: opcode_STY(absolute()); break;
                 case 0x9E: opcode_SXA(absoluteY()); break;
                 case 0x9C: opcode_SYA(absoluteX()); break;
                 case 0xAA: opcode_TAX_implied(); break;
                 case 0xA8: opcode_TAY_implied(); break;
                 case 0xBA: opcode_TSX_implied(); break;
                 case 0x8A: opcode_TXA_implied(); break;
                 case 0x9A: opcode_TXS_implied(); break;
                 case 0x98: opcode_TYA_implied(); break;
 
                 default: throw new RuntimeException("Unhandled opcode [" + Utils.toHexString(_opcode) + "]");
             }
             
             // Mask to 16 bit
             pc &= 0xFFFF;
             
             assert a >= 0 && a <= 0xFF        : "A out of bounds";
             assert x >= 0 && x <= 0xFF        : "X out of bounds";
             assert y >= 0 && y <= 0xFF        : "Y out of bounds";
             assert s >= 0x0100 && s <= 0x01FF : "S out of bounds";
         }
         
         return _clocksRemain;
     }
     
 //-------------------------------------------------------------
 // Processor status flags
 //-------------------------------------------------------------     
     
     private final boolean isZeroFlagSet()
     {
         return not_zero == 0;
     }
     
     private final boolean isCarryFlagSet()
     {
         return carry > 0;
     }
     
     private final boolean isNegativeFlagSet()
     {
         return negative > 0;
     }
     
     private final boolean isOverflowFlagSet()
     {
         return overflow > 0;
     }
     
     private final void setNZFlag(int aValue)
     {
         negative = (aValue >> 7) & 1;
         not_zero = aValue & 0xFF;
     }
     
     public final int getRegisterP(int aBRKValue)
     {
         assert aBRKValue == 0 || aBRKValue == 1;
         
         int _zero = isZeroFlagSet() ? 1 : 0;
         return carry << 0 | (_zero << 1) | (interruptDisable << 2) | (decimal << 3) | (aBRKValue << 4) | (1 << 5) | (overflow << 6) | (negative << 7);
     }
     
 //-------------------------------------------------------------
 // Addressing
 //-------------------------------------------------------------    
     
     /**
      * Immediate addressing allows the programmer to directly specify an 8 bit constant within the instruction. 
      */
     private final int immediate()
     {
         return pc++;
     }
     
     /**
      * An instruction using zero page addressing mode has only an 8 bit address operand. This limits it to addressing 
      * only the first 256 bytes of memory (e.g. $0000 to $00FF) where the most significant byte of the address is always 
      * zero. In zero page mode only the least significant byte of the address is held in the instruction making it 
      * shorter by one byte (important for space saving) and one less memory fetch during execution (important for speed).
      * 
      * An assembler will automatically select zero page addressing mode if the operand evaluates to a zero page address
      * and the instruction supports the mode (not all do).
      */
     private final int zeroPage()
     {        
         return memory.readByte(pc++);
     }
     
     /**
      * The address to be accessed by an instruction using indexed zero page addressing is calculated by taking the 8 bit 
      * zero page address from the instruction and adding the current value of the X register to it. For example if the X 
      * register contains $0F and the instruction LDA $80,X is executed then the accumulator will be loaded from $008F 
      * (e.g. $80 + $0F => $8F).
      * 
      * NB:
      * The address calculation wraps around if the sum of the base address and the register exceed $FF. If we repeat the 
      * last example but with $FF in the X register then the accumulator will be loaded from $007F (e.g. $80 + $FF => $7F) 
      * and not $017F.
      */
     private final int zeroPageX()
     {
         return (memory.readByte(pc++) + x) & 0xFF;        
     }
     
     /**
      * The address to be accessed by an instruction using indexed zero page addressing is calculated by  taking the 8 bit 
      * zero page address from the instruction and adding the current value of the Y register to it. This mode can only be 
      * used with the LDX and STX instructions.
      */
     private final int zeroPageY()
     {
         return (memory.readByte(pc++) + y) & 0xFF;        
     }
     
     /**
      * Relative addressing mode is used by branch instructions (e.g. BEQ, BNE, etc.) which contain a signed 8 bit relative 
      * offset (e.g. -128 to +127) which is added to program counter if the condition is true. As the program counter itself 
      * is incremented during instruction execution by two the effective address range for the target instruction must be 
      * with -126 to +129 bytes of the branch.
      */
     private final int relative()
     {
         return pc++; 
     }
     
     /**
      * Instructions using absolute addressing contain a full 16 bit address to identify the target location.
      */
     private final int absolute()
     {
         int _result = readWord(pc);
         pc += 2;
         return _result;
     }
     
     /**
      * The address to be accessed by an instruction using X register indexed absolute addressing is computed by taking 
      * the 16 bit address from the instruction and added the contents of the X register. For example if X contains $92 
      * then an STA $2000,X instruction will store the accumulator at $2092 (e.g. $2000 + $92).
      */
     private final int absoluteX()
     {
         int _result = readWord(pc) + x;
         pc += 2;
        return _result & 0xFFFF;
     }
     
     /**
      * The Y register indexed absolute addressing mode is the same as the previous mode only with the contents of the Y 
      * register added to the 16 bit address from the instruction.
      */
     private final int absoluteY()
     {
         int _result = readWord(pc) + y;
         pc += 2;
        return _result & 0xFFFF;
     }
         
     /**
      * JMP is the only 6502 instruction to support indirection. The instruction contains a 16 bit address which identifies 
      * the location of the least significant byte of another 16 bit memory address which is the real target of the instruction.
      * 
      * For example if location $0120 contains $FC and location $0121 contains $BA then the instruction JMP ($0120) will cause 
      * the next instruction execution to occur at $BAFC (e.g. the contents of $0120 and $0121).
      * 
      * N.B.
      * An original 6502 has does not correctly fetch the target address if the indirect vector falls on a page boundary 
      * (e.g. $xxFF where xx is and value from $00 to $FF). In this case fetches the LSB from $xxFF as expected but takes 
      * the MSB from $xx00. This is fixed in some later chips like the 65SC02 so for compatibility always ensure the indirect 
      * vector is not at the end of the page.
      */
     private final int indirect()
     {
         int _address = readWord(pc++);
         
         if((_address & 0x00FF) == 0xFF)
         {
             // Page boundary bug
             int _msb = _address - 0xFF;            
             return readWord(_address, _msb);               
         }
         else
         {
             return readWord(_address);    
         }
     }    
     
     /**
      * Indexed indirect addressing is normally used in conjunction with a table of address held on zero page. The address 
      * of the table is taken from the instruction and the X register added to it (with zero page wrap around) to give the 
      * location of the least significant byte of the target address.
      */
     private final int indirectX()
     {
         int _address = (memory.readByte(pc++) + x) & 0xFF;       
         return readWordZeroPageWrap(_address);
     }
     
     /**
      * Indirect indirect addressing is the most common indirection mode used on the 6502. In instruction contains the zero 
      * page location of the least significant byte of 16 bit address. The Y register is dynamically added to this value 
      * to generated the actual target address for operation.
      */
     private final int indirectY()
     {
         int _address = memory.readByte(pc++);
         return readWordZeroPageWrap(_address) + y;        
     }    
     
 //-------------------------------------------------------------
 // Stack
 //-------------------------------------------------------------    
 
     private final void push(int aByte)
     {
         memory.writeByte(aByte, s);
         s = 0x0100 | (--s & 0xFF);
     }
     
     private final int pop()
     {
         s = 0x0100 | (++s & 0xFF);
         return memory.readByte(s);
     }
     
     private final void pushWord(int aWord)
     {
         push((aWord >> 8) & 255);
         push(aWord & 255);
     }
     
     private final int popWord()
     {
         int _byte1 = pop();
         return (pop() << 8) | _byte1;
     }
 
 //-------------------------------------------------------------
 // Utils
 //-------------------------------------------------------------    
     
     private final int readWord(int anAddress)
     {
         return readWord(anAddress, anAddress + 1);
     }
     
     private final int readWordZeroPageWrap(int anAddress)
     {
         return readWord(anAddress, (anAddress + 1) & 0xFF);
     }
     
     private final int readWord(int anAddress, int aSecondAddress)
     {
         return memory.readByte(anAddress) | (memory.readByte(aSecondAddress) << 8);
     }
     
     private final void branchOnCondition(boolean isBranch)
     {
         int _address = relative();
         if(isBranch)
         {
             int _value = memory.readByte(_address);
             int _signedByte = _value < 0x80 ? _value : _value - 256;
             pc += _signedByte;
         }
     }
     
 //-------------------------------------------------------------
 // Opcodes
 //-------------------------------------------------------------    
     
     // Add with Carry
     private void opcode_ADC(int anAddress)
     {
         int _value = memory.readByte(anAddress);
         int _temp = a + _value + carry;
         
         overflow = ((!(((a ^ _value) & 0x80) != 0) && (((a ^ _temp) & 0x80)) != 0) ? 1 : 0);
         a = _temp & 0xFF;
         
         carry = _temp > 0xFF ? 1 : 0;
         setNZFlag(_temp);
     }
     
     // Equivalent to AND #i then LSR A.
     private final void opcode_ALR(int anAddress)
     {
         opcode_AND(anAddress);
         opcode_LSR_accumulator();
     }
  
     // AND followed by Copy N (bit 7) to C.
     private final void opcode_ANC(int anAddress)
     {
         opcode_AND(anAddress); 
 
         carry = (a >> 7) & 1;
     }
     
     // Logical AND
     private void opcode_AND(int anAddress)
     {
         a &= memory.readByte(anAddress);
         setNZFlag(a);
     }
 
     // AND byte with accumulator, then rotate one bit right in accumulator
     private final void opcode_ARR(int anAddress)
     {
         opcode_AND(anAddress);
         opcode_ROR_accumulator();
         
         /**
          * Adapted from http://nesdev.parodius.com/bbs/viewtopic.php?t=3831&postdays=0&postorder=asc&start=15
          */
         switch (a & 0x60) 
         { 
             case 0x00: carry = 0; overflow = 0; break; // bit 5 and bit 6 clear
             case 0x20: carry = 0; overflow = 1; break; // bit 5 set, bit 6 clear
             case 0x40: carry = 1; overflow = 1; break; // bit 6 set, bit 5 clear
             case 0x60: carry = 1; overflow = 0; break; // bit 5 and bit 6 set
         }
     }    
     
     // Arithmetic Shift Left
     private void opcode_ASL_accumulator()
     {
         carry = (a >> 7) & 1;
         a = (a << 1) & 0xFF;
         
         setNZFlag(a);
     }
 
     // Arithmetic Shift Left
     private void opcode_ASL(int anAddress)
     {
         int _value = memory.readByte(anAddress);
         
         carry = (_value >> 7) & 1;
         _value = (_value << 1) & 0xFF;
         
         memory.writeByte(_value, anAddress);
         
         setNZFlag(_value);
     }
  
     // AND byte with accumulator, then transfer accumulator to X register.
     private final void opcode_ATX(int anAddress)
     {
         a = 0xFF; // TODO not sure about this, maybe |= value from anAddress? 
         opcode_AND(anAddress);
         x = a;
     }
     
     // AND X register with accumulator and store result in X register, then subtract byte from X register (without borrow).
     private final void opcode_AXS(int anAddress)
     {
         int _value = memory.readByte(anAddress);
         int _temp = (a & x) - _value;
         
         carry = _temp < 0 ? 0 : 1;
         
         x = _temp & 0xFF;
         setNZFlag(x);
     }
     
     // Branch if Carry Clear
     private void opcode_BCC_relative()
     {
         branchOnCondition(!isCarryFlagSet());
     }
  
     // Branch if Carry Set
     private void opcode_BCS_relative()
     {
         branchOnCondition(isCarryFlagSet());
     }
 
     // Branch if Equal
     private void opcode_BEQ_relative()
     {
         branchOnCondition(isZeroFlagSet());
     }
 
     // Bit Test
     private void opcode_BIT(int anAddress)
     {
         int _value = memory.readByte(anAddress);
         
         negative = (_value >> 7) & 1;
         overflow = (_value >> 6) & 1;
         _value &= a;
         
         not_zero = _value;
     }
 
     // Branch if Minus
     private void opcode_BMI_relative()
     {
         branchOnCondition(isNegativeFlagSet());
     }
 
     // Branch if Not Equal
     private void opcode_BNE_relative()
     {
         branchOnCondition(!isZeroFlagSet());
     }
 
     // Branch if Positive
     private void opcode_BPL_relative()
     {
         branchOnCondition(!isNegativeFlagSet());
     }
 
     // Force Interrupt
     private void opcode_BRK_implied()
     {
         pushWord(pc + 1);
         
         opcode_PHP_implied();
         opcode_SEI_implied();
         opcode_JMP(readWord(IRQ_VECTOR));        
     }
 
     // Branch if Overflow Clear
     private void opcode_BVC_relative()
     {
         branchOnCondition(!isOverflowFlagSet());
     }
 
     // Branch if Overflow Set
     private void opcode_BVS_relative()
     {
         branchOnCondition(isOverflowFlagSet());
     }
     
     // Clear Carry Flag
     private void opcode_CLC_implied()
     {
         carry = 0;
     }
 
     // Clear Decimal Mode
     private void opcode_CLD_implied()
     {
         decimal = 0;
     }
 
     // Clear Interrupt Disable
     private void opcode_CLI_implied()
     {
         interruptDisable = 0;
     }
 
     // Clear Overflow Flag
     private void opcode_CLV_implied()
     {
         overflow = 0;
     }
 
     // Compare
     private void opcode_CMP(int anAddress)
     {
         int _temp = a - memory.readByte(anAddress);
         
         carry = (_temp >= 0 ? 1:0);
         setNZFlag(_temp);
     }
 
     // Compare X Register
     private void opcode_CPX(int anAddress)
     {
         int _temp = x - memory.readByte(anAddress);
         
         carry = (_temp >= 0 ? 1:0);
         setNZFlag(_temp);
     }
 
     // Compare Y Register
     private void opcode_CPY(int anAddress)
     {
         int _temp = y - memory.readByte(anAddress);
         
         carry = (_temp >= 0 ? 1:0);
         setNZFlag(_temp);
     }
     
     // DEC value then CMP value
     private void opcode_DCP(int anAddress)
     {
         opcode_DEC(anAddress);
         opcode_CMP(anAddress);
     }
 
     // Decrement Memory
     private void opcode_DEC(int anAddress)
     {
         int _value = (memory.readByte(anAddress) - 1) & 0xFF;
         memory.writeByte(_value, anAddress);
         
         setNZFlag(_value);
     }
 
     // Decrement X Register
     private void opcode_DEX_implied()
     {
         x = (x - 1) & 0xFF;
         setNZFlag(x);
     }
 
     // Decrement Y Register
     private void opcode_DEY_implied()
     {
         y = (y - 1) & 0xFF;
         setNZFlag(y);
     }
 
     // Exclusive OR
     private void opcode_EOR(int anAddress)
     {
         a ^=  memory.readByte(anAddress);
         setNZFlag(a);
     }
 
     // Increment Memory
     private void opcode_INC(int anAddress)
     {    
         int _value = (memory.readByte(anAddress) + 1) & 0xFF;
         memory.writeByte(_value, anAddress);
         
         setNZFlag(_value);
     }
 
     // Increment X Register
     private void opcode_INX_implied()
     {
         x = (x + 1) & 0xFF;
         setNZFlag(x);
     }
 
     // Increment Y Register
     private void opcode_INY_implied()
     {
         y = (y + 1) & 0xFF;
         setNZFlag(y);
     }
 
     // INC value then SBC value
     private void opcode_ISB(int anAddress)
     {
         opcode_INC(anAddress);
         opcode_SBC(anAddress);
     }
     
     // Jump to target address
     private void opcode_JMP(int anAddress)
     {
         pc = anAddress;
     }
 
     // Jump to subroutine
     private void opcode_JSR(int anAddress)
     {
         pushWord(pc - 1);        
         pc = anAddress;
     }
 
     // Load Accumulator and X with memory
     private void opcode_LAX(int anAddress)
     {
         opcode_LDA(anAddress);
         opcode_LDX(anAddress);
     }
     
     // Load Accumulator
     private void opcode_LDA(int anAddress)
     {
         a = memory.readByte(anAddress);
         setNZFlag(a);
     }
 
     // Load X with memory
     private void opcode_LDX(int anAddress)
     {
         x = memory.readByte(anAddress);
         setNZFlag(x);
     }
 
     // Load Y Register
     private void opcode_LDY(int anAddress)
     {
         y = memory.readByte(anAddress);
         setNZFlag(y);
     }
 
     // Logical Shift Right
     private void opcode_LSR_accumulator()
     {
         carry = a & 1; // old bit 0       
         a >>= 1;
         
         not_zero = a;
         negative = 0;
     }
 
     // Logical Shift Right
     private void opcode_LSR(int anAddress)
     {
         int _value = memory.readByte(anAddress);
         
         carry = _value & 1; // old bit 0       
         _value >>= 1;
         
         memory.writeByte(_value, anAddress);
         
         not_zero = _value;
         negative = 0;
     }
 
     // No operation
     private void opcode_NOP_implied()
     {        
     }
     
     // No operation
     private void opcode_NOP(int anAddress)
     {
     }
     
     // Logical Inclusive OR
     private void opcode_ORA(int anAddress)
     {
         a |=  memory.readByte(anAddress);
         setNZFlag(a);
     }
 
     // Push Accumulator
     private void opcode_PHA_implied()
     {
         push(a);
     }
 
     // Push Processor Status
     private void opcode_PHP_implied()
     {
         push(getRegisterP(1));
     }
 
     // Pull Accumulator
     private void opcode_PLA_implied()
     {
         a = pop();
         setNZFlag(a);
     }
 
     // Pull Processor Status
     private void opcode_PLP_implied()
     {
         int _temp = pop();
     	
     	carry            = (_temp) & 1;
         not_zero         = ((_temp >> 1) & 1) == 1 ? 0 : 1;
         interruptDisable = (_temp >> 2) & 1;
         decimal          = (_temp >> 3) & 1; 
         overflow         = (_temp >> 6) & 1;
         negative         = (_temp >> 7) & 1;        
     }
 
     // ROL value then AND value
     private void opcode_RLA(int anAddress)
     {
         opcode_ROL(anAddress);
         opcode_AND(anAddress);
     }
     
     // Rotate Left
     private void opcode_ROL_accumulator()
     {
 		int _add = carry;
 		
 		carry = (a >> 7) &1;
 		
 		a = ((a << 1) & 0xFF) + _add;
 		
 		setNZFlag(a);
     }
 
     // Rotate Left
     private void opcode_ROL(int anAddress)
     {
         int _value = memory.readByte(anAddress);
         int _add = carry;
         
         carry = (_value >> 7) &1;
         _value = ((_value << 1) & 0xFF) + _add;
         
         memory.writeByte(_value, anAddress);
         
         setNZFlag(_value);
     }
 
     // Rotate Right
     private void opcode_ROR_accumulator()
     {
         int _add = carry << 7;
     	
 		carry = a & 1;
 		a = (a >> 1) + _add;
 		
 		setNZFlag(a);
     }
 
     // Rotate Right
     private void opcode_ROR(int anAddress)
     {
         int _value = memory.readByte(anAddress);
         int _add = carry << 7;
         
         carry = _value & 1;
         _value = (_value >> 1) + _add;
         
         memory.writeByte(_value, anAddress);
         
         setNZFlag(_value);
     }
     
     // ROR value then ADC value
     private void opcode_RRA(int anAddress)
     {
         opcode_ROR(anAddress);
         opcode_ADC(anAddress);
     }
 
     // Return from Interrupt
     private void opcode_RTI_implied()
     {
         int _temp = pop();
         
         carry            = (_temp) & 1;
         not_zero         = ((_temp >> 1) & 1) == 1 ? 0 : 1;
         interruptDisable = (_temp >> 2) & 1;
         decimal          = (_temp >> 3) & 1; 
         overflow         = (_temp >> 6) & 1;
         negative         = (_temp >> 7) & 1;
         
         pc = popWord();
     }
 
     // Return from Subroutine
     private void opcode_RTS_implied()
     {
         pc = popWord() + 1;        
     }
 
     // Store A and X bitwise
     private void opcode_SAX(int anAddress)
     {
         memory.writeByte(a & x, anAddress);
     }
 
     // Subtract with Carry
     private void opcode_SBC(int anAddress)
     {
         int _value = memory.readByte(anAddress);
         int _temp = a - _value - (1 - carry);
         
         carry = _temp < 0 ? 0 : 1;
         overflow = ((((a ^ _temp) & 0x80) != 0 && ((a ^ _value) & 0x80) != 0) ? 1 : 0);
         
         setNZFlag(_temp);
         
         a = _temp & 0xFF;
     }
 
     // Set the carry flag
     private void opcode_SEC_implied()
     {
         carry = 1;
     }
 
     // Set Decimal Flag
     private void opcode_SED_implied()
     {
         decimal = 1;
     }
 
     // Set Interrupt Disable
     private void opcode_SEI_implied()
     {
         interruptDisable = 1;
     }
 
     // ASL value then ORA value
     private void opcode_SLO(int anAddress)
     {
         opcode_ASL(anAddress);
         opcode_ORA(anAddress);
     }
     
     // Equivalent to LSR value then EOR value
     private void opcode_SRE(int anAddress)
     {
         opcode_LSR(anAddress);
         opcode_EOR(anAddress);
     }
     
     // Store Accumulator
     private void opcode_STA(int anAddress)
     {
        memory.writeByte(a, anAddress);        
     }
 
     // Store X register
     private void opcode_STX(int anAddress)
     {
         memory.writeByte(x, anAddress);
     }
 
     // Store Y Register
     private void opcode_STY(int anAddress)
     {
         memory.writeByte(y, anAddress);        
     }
 
     // AND X register with the high byte of the target address of the argument + 1.
     private final void opcode_SXA(int anAddress)
     {
         int _value = (x & ((anAddress >> 8) + 1)) & 0xFF;
         
         /**
          * Ignore the write on page boundary crosses.
          * http://nesdev.parodius.com/bbs/viewtopic.php?t=8107
          */
         if((y + memory.readByte(pc - 2)) <= 0xFF)
         {
             memory.writeByte(_value, anAddress);
         }
     }
     
     // AND Y register with the high byte of the target address of the argument + 1.
     private final void opcode_SYA(int anAddress)
     {
         int _result = (y & (((anAddress) >> 8) + 1)) & 0xFF;
         
         /**
          * Ignore the write on page boundary crosses.
          * http://nesdev.parodius.com/bbs/viewtopic.php?t=8107
          */
         if((x + memory.readByte(pc - 2)) <= 0xFF)
         {
             memory.writeByte(_result, anAddress);
         }
     }
 
     // Transfer Accumulator to X
     private void opcode_TAX_implied()
     {
         x = a;
         setNZFlag(x);
     }
 
     // Transfer Accumulator to Y
     private void opcode_TAY_implied()
     {
         y = a;
         setNZFlag(y);
     }
 
     // Transfer Stack Pointer to X
     private void opcode_TSX_implied()
     {
         x = s & 0xFF; // Only transfer the lower 8 bits        
         setNZFlag(x);
     }
    
     // Transfer X to Accumulator
     private void opcode_TXA_implied()
     {
         a = x;
         setNZFlag(a);
     }
 
     // Transfer X to Stack Pointer
     private void opcode_TXS_implied()
     {
         s = x | 0x0100;
     }
 
     // Transfer Y to Accumulator
     private void opcode_TYA_implied()
     {
         a = y;
         setNZFlag(a);
     }    
 }
