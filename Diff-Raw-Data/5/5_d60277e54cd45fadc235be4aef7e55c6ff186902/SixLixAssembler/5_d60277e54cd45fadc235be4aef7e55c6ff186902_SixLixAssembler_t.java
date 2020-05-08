 import java.io.IOException;
 import java.util.HashMap;
 import java.lang.Math;
 
 public class SixLixAssembler extends Assembler {
 
   public HashMap<String, Long> labelMap;
   public HashMap<String, String> regMap;
 
   public SixLixAssembler(String[] args) throws IOException {
     super(args);
     labelMap = new HashMap<String, Long>();
     regMap = new HashMap<String, String>();
   }
 
   @Override
   void processLabel(String sourceCode) {
     // Place label in hashMap that resolves to current address
     labelMap.put(sourceCode, programCounter);
   }
 
   /*
    * Prints out invalid operand error message and the passed instruction and
    * exits program with code 1
    */
   private void printOpErr(Instruction instr) {
     System.err.println("Error: Invalid operand at pc " + programCounter);
     instr.print();
     System.out.flush();
     System.exit(1);
   }
 
   /*
    * Prints out immediate range error with the actual size vs required size, the
    * passed instruction, and exits with code 1
    */
   private void printRangeErr(Instruction instr, int reqSize, int actSize) {
     System.err.println("Error: Immedate out of range at pc " + programCounter);
     System.err.println("Immediate should fit in " + reqSize
         + " bits. Is actually " + actSize);
     instr.print();
     System.out.flush();
     System.exit(1);
   }
 
   /*
    * Returns the passed number num as a binary string of bit size, size if num
    * does not fit in size prints the appropriate error and exits program with
    * code 1. The returned string will size characters long padded appropriately
    * with 0's or 1's.
    */
   private String numToStr(Instruction instr, Long num, int size) {
     String numStr = Long.toBinaryString(Math.abs(num));
     if (numStr.length() > size) { // num in binary > size so we need to error
       printRangeErr(instr, size, numStr.length());
     } else if (numStr.length() < size) { // num needs to be padded
       if (num < 0) { // need to pad with 1's
         numStr = Long.toBinaryString(num); // toBinary will sign extend to size
                                            // of long in bits
         // truncate to get correct size
         numStr = numStr.substring(numStr.length() - size, numStr.length());
       } else { // need to pad with 0's
         int len = numStr.length();
         for (int i = 0; i < size - len; i++) { // pad with 0's until
                                                // size is correct
           numStr = "0" + numStr;
         }
       }
     }
     return numStr;
   }
 
   /*
    * Gets temporary register number num and exits program if temporary reg is
    * invalid. Also used for the offset since the same number range is used.
    */
   private String getTemp(Instruction instr, int num) {
     if (num == 0)
       return "00";
     if (num == 1)
       return "01";
     if (num == 2)
       return "10";
     if (num == 3)
       return "11";
     System.err.println("Error: Temp or offset out of range at pc "
         + programCounter);
     instr.print();
     System.out.flush();
     System.exit(1);
     return "uh.... god, whut? fucking errors EVERYWHERWER";
   }
 
   String generateCode(Instruction instruction) {
 
     String code = null; // Machine code string
     String op = instruction.operator; // operator name
     Operand[] ops = instruction.operands; // opperands
     if (op.equalsIgnoreCase("addi")) { // add immediate
       if (!getOperandType(ops[0].name).equals("register") /*
                                                            * ||
                                                            * !getOperandType(ops
                                                            * [ 1]. name).equals(
                                                            * "immediate" )
                                                            */) {
         printOpErr(instruction);
       }
       Long imm = Long.decode(ops[1].name);
       String immStr = numToStr(instruction, imm, 6);
       code = "1110" + getReg(instruction, ops[0].name) + immStr; // opcode +
                                                                  // register +
                                                                  // immediate
 
     } else if (op.equalsIgnoreCase("bne")) {
       // verify opperand(s) are correct
       if (!getOperandType(ops[0].name).equals("register")) {
         printOpErr(instruction);
       }
       Long imm = Long.decode(ops[1].name);
       String immStr = numToStr(instruction, imm, 6);
       code = "1100" + getReg(instruction, ops[0].name) + immStr; // opcode +
                                                                  // register +
                                                                  // immediate
                                                                  // (offset)
 
     } else if (op.equalsIgnoreCase("blt")) {
       // verify opperand(s) are correct
       if (!getOperandType(ops[0].name).equals("register")) {
         printOpErr(instruction);
       }
       Long imm = Long.decode(ops[1].name);
       String immStr = numToStr(instruction, imm, 6);
       code = "1101" + getReg(instruction, ops[0].name) + immStr; // opcode +
                                                                  // register +
                                                                  // immediate
                                                                  // (offset)
 
     } else if (op.equalsIgnoreCase("sra")) {
       // verify opperand(s) are correct
       if (!getOperandType(ops[0].name).equals("register")) {
         printOpErr(instruction);
       }
       Long imm = Long.decode(ops[1].name);
       String immStr = numToStr(instruction, imm, 4);
       code = "1001" + getReg(instruction, ops[0].name) + immStr + "01"; // opcode
                                                                         // +
                                                                         // register
                                                                         // +
                                                                         // immediate
                                                                         // +
                                                                         // function
                                                                         // code
 
     } else if (op.equalsIgnoreCase("jor")) {
       // verify opperand(s) are correct
       if (!getOperandType(ops[0].name).equals("register")) {
         printOpErr(instruction);
       }
       Long imm = Long.decode(ops[1].name);
       String immStr = numToStr(instruction, imm, 4);
       code = "1001" + getReg(instruction, ops[0].name) + immStr + "11"; // opcode
                                                                         // +
                                                                         // register
                                                                         // +
                                                                         // immediate
                                                                         // +
                                                                         // function
                                                                         // code
 
     } else if (op.equalsIgnoreCase("jr")) {
       // verify opperand(s) are correct
       if (!getOperandType(ops[0].name).equals("register")) {
         printOpErr(instruction);
       }
       code = "1011" + getReg(instruction, ops[0].name) + "000000"; // opcode +
                                                                    // register +
                                                                    // padding
 
     } else if (op.equalsIgnoreCase("lw")) {
       // verify opperand(s) are correct
       if (!getOperandType(ops[0].name).equals("register")
           || !getOperandType(ops[1].name).equals("register")) {
         printOpErr(instruction);
       }
       int num = ops[1].offset;
       String tempReg = getTemp(instruction, num);
       code = "0110" + getReg(instruction, ops[0].name)
           + getReg(instruction, ops[1].name) + tempReg; // opcode + register +
                                                         // reigster + offset
                                                         // (tempReg)
 
     } else if (op.equalsIgnoreCase("sw")) {
       // verify opperand(s) are correct
       if (!getOperandType(ops[0].name).equals("register")
           || !getOperandType(ops[1].name).equals("register")) {
         printOpErr(instruction);
       }
       int num = ops[1].offset;
       String tempReg = getTemp(instruction, num);
       code = "0111" + getReg(instruction, ops[0].name)
           + getReg(instruction, ops[1].name) + tempReg; // opcode + register +
                                                         // register + offset
                                                         // (tempReg)
 
     } else if (op.equalsIgnoreCase("lix")) {
       // verify opperand(s) are correct
       if (!getOperandType(ops[0].name).equals("register")
           || !getOperandType(ops[1].name).equals("register")
           || !getOperandType(ops[2].name).equals("label")) {
         printOpErr(instruction);
       }
       int num = Integer.valueOf(ops[2].name);
       String tempReg = getTemp(instruction, num);
       code = "0100" + getReg(instruction, ops[0].name)
           + getReg(instruction, ops[1].name) + tempReg; // opcode + register +
                                                         // register + temp reg #
 
     } else if (op.equalsIgnoreCase("six")) {
       // verify opperand(s) are correct
       if (!getOperandType(ops[0].name).equals("register")
           || !getOperandType(ops[1].name).equals("register")
           || !getOperandType(ops[2].name).equals("label")) {
         printOpErr(instruction);
       }
       int num = Integer.valueOf(ops[2].name);
       String tempReg = getTemp(instruction, num);
       code = "0101" + getReg(instruction, ops[0].name)
           + getReg(instruction, ops[1].name) + tempReg; // opcode + register +
                                                         // register + temp reg #
 
     } else if (op.equalsIgnoreCase("add")) {
       // verify opperand(s) are correct
       if (!getOperandType(ops[0].name).equals("register")
           || !getOperandType(ops[1].name).equals("register")) {
         printOpErr(instruction);
       }
       code = "0000" + getReg(instruction, ops[0].name)
           + getReg(instruction, ops[1].name) + "00";// opcode + register +
                                                     // register + function code
 
     } else if (op.equalsIgnoreCase("sub")) {
       // verify opperand(s) are correct
       if (!getOperandType(ops[0].name).equals("register")
           || !getOperandType(ops[1].name).equals("register")) {
         printOpErr(instruction);
       }
       code = "0000" + getReg(instruction, ops[0].name)
           + getReg(instruction, ops[1].name) + "01";// opcode + register +
                                                     // register + function code
 
     } else if (op.equalsIgnoreCase("nor")) {
       // verify opperand(s) are correct
       if (!getOperandType(ops[0].name).equals("register")
           || !getOperandType(ops[1].name).equals("register")) {
         printOpErr(instruction);
       }
       code = "0000" + getReg(instruction, ops[0].name)
           + getReg(instruction, ops[1].name) + "10";// opcode + register +
                                                     // register + function code
 
     } else if (op.equalsIgnoreCase("mv")) {
       // verify opperand(s) are correct
       if (!getOperandType(ops[0].name).equals("register")
           || !getOperandType(ops[1].name).equals("register")) {
         printOpErr(instruction);
       }
       code = "0000" + getReg(instruction, ops[0].name)
           + getReg(instruction, ops[1].name) + "11";// opcode + register +
                                                     // register + function code
 
     } else if (op.equalsIgnoreCase("in")) {
       // verify opperand(s) are correct
       if (!getOperandType(ops[0].name).equals("register")
           || !getOperandType(ops[1].name).equals("register")) {
         printOpErr(instruction);
       }
       code = "0001" + getReg(instruction, ops[0].name)
           + getReg(instruction, ops[1].name) + "00"; // opcode + register +
                                                      // register + function code
 
     } else if (op.equalsIgnoreCase("out")) {
       // verify opperand(s) are correct
       if (!getOperandType(ops[0].name).equals("register")
           || !getOperandType(ops[1].name).equals("register")) {
         printOpErr(instruction);
       }
       code = "0001" + getReg(instruction, ops[0].name)
           + getReg(instruction, ops[1].name) + "01"; // opcode + register +
                                                      // register + function code
 
     } else if (op.equalsIgnoreCase("j")) {
       Long imm = Long.decode(ops[0].name);
       String immStr = numToStr(instruction, imm, 9);
       code = "1000" + immStr + "0"; // opcode + immediate + function code
 
     } else if (op.equalsIgnoreCase("jal")) {
       Long imm = Long.decode(ops[0].name);
       String immStr = numToStr(instruction, imm, 9);
       code = "1000" + immStr + "1"; // opcode + immediate + function code
 
     } else if (op.equalsIgnoreCase("sloi")) {
       Long imm = Long.decode(ops[0].name);
       String immStr = numToStr(instruction, imm, 10);
       code = "1010" + immStr; // opcode + immediate
 
     } else if (op.equalsIgnoreCase("halt")) {
       code = "1111" + "0000000000"; // opcode + padding
 
     } else if (op.equalsIgnoreCase("li")) {
       // li can be a dynamic number of instructions depending on the size of
       // the immediate thus is a bit more complex.
       int reqCodeLen = 0; // sets how big variable code should be when doing
                           // length check
       int numInst = 0; // sets the number of instructions used
       long num = Long.decode(instruction.operands[0].name);
       String numBits = numToStr(instruction, num, 34);
      if (num < 1024 && num >= 0) { // if the immediate is bigger than 10 bits
         reqCodeLen = 17 * 2 + 2;
         code = "0000000" + getReg(instruction, "$v0")
             + getReg(instruction, "$0") + "11" + ",\n";
         // need to shift in to be "shifted" in: 10 bits
         code += "0001010" + numBits.substring(24, 34);
         numInst = 2;
 
      } else if (num < 1024 * 1024 && num >= 0) { // if immediate is bigger than 20bits
         reqCodeLen = 17 * 3 + 2 * 2;
         code = "0000000" + getReg(instruction, "$v0")
             + getReg(instruction, "$0") + "11" + ",\n";
         // total bits needed to be "shifted" in: 20 bits
         code += "0001010" + numBits.substring(14, 24) + ",\n"; // shift in 10
                                                                // MSB
         code += "0001010" + numBits.substring(24, 34); // shift in next 10 bits
         numInst = 3;
 
       } else { // we need to use the max number of instructions
         reqCodeLen = 17 * 4 + 2 * 3; // Used to check if variable
                                      // code is the
                                      // expected size
 
         // total bits needed to be "shifted" in: 34 bits
         code = "0001010" + "000000" + numBits.substring(0, 4) + ",\n"; // "shift"
         // in 4 MSB
         code += "0001010" + numBits.substring(4, 14) + ",\n"; // "shift" in next
                                                               // 10
         // bits
         code += "0001010" + numBits.substring(14, 24) + ",\n"; // "shift" in
                                                                // next
         // other 10
         code += "0001010" + numBits.substring(24, 34); // "shift" in next last
                                                        // 10
         // bits
         // total "shifted" bits 34
         numInst = 4;
       }
       // verify the Machine code for instruction is the right size error out
       // otherwise
       if (code.length() != reqCodeLen) {
         System.err.println("Error processing instruction at pc "
             + programCounter);
         System.err.println("Machine code is " + code.length()
             + " bits but should be " + reqCodeLen + " bits");
         instruction.print();
         System.out.flush();
         System.exit(1);
       }
       programCounter += numInst;
       return code;
     } else if (op.equalsIgnoreCase("la")) {
       Long num = Long.decode(ops[0].name);
       String numBits = numToStr(instruction, num, 34); // Check for correct
                                                        // size, convert to
                                                        // bitfield
       code = "0001010" + "000000" + numBits.substring(0, 4) + ",\n";
       /* numToStr(instruction, (num>>30)%1024, 10) */
       code += "0001010" + numBits.substring(4, 14) + ",\n";
       /* numToStr(instruction, (num>>20)%1024, 10) */
       code += "0001010" + numBits.substring(14, 24) + ",\n";
       /* numToStr(instruction, (num>>10)%1024, 10) */
       code += "0001010" + numBits.substring(24, 34);
       /* numToStr(instruction, num%1024, 10) */
       if (code.length() != 17 * 4 + 2 * 3) {
         System.err.println("Error processing instruction at pc "
             + programCounter);
         System.err.println("Machine code is " + code.length()
             + " bits but should be 34 bits");
         instruction.print();
         System.out.flush();
         System.exit(1);
       }
       programCounter += 4;
       return code;
 
     } else {
       // no instruction is not defined in our ISA
       System.err.println("Error unrecognized instruction at pc "
           + programCounter);
       instruction.print();
       System.out.flush();
       System.exit(1);
     }
     code = "000" + code;
     // all instructions except for li an la hit this section
 
     // verifies Machine code is in binary otherwise errors out
     if (!isBinary(code)) {
       System.err
           .println("Error processing instruction at pc " + programCounter);
       System.err
           .println("Machine code is not binary. Machine code is: " + code);
       instruction.print();
       System.out.flush();
       System.exit(1);
     }
     // verifies machine code is 14 bits otherwise errors out
     if (code.length() != 17) {
       System.err
           .println("Error processing instruction at pc " + programCounter);
       System.err.println("Machine code is " + code.length()
           + " bits but should be 17 bits");
       instruction.print();
       System.out.flush();
       System.exit(1);
     }
     programCounter += 1; // increment PC for error messages
     return code;
   }
 
   @Override
   void updateProgramCounter(Instruction instruction) {
     // la takes 4 sloi instructions
     if (instruction.operator.equalsIgnoreCase("la")) {
       programCounter += 4;
     }
     // dynamic pseudo instruction li varies depending on immediate size
     else if (instruction.operator.equalsIgnoreCase("li")) {
       // long num = Long.parseLong(instruction.operands[0].name, 16);
       long num = Long.decode(instruction.operands[0].name);
       if (num < 1024) {
         programCounter += 2; // zero out and a soli
       } else if (num < 1024 * 1024) {
         programCounter += 3; // zero out and 2 soli
       } else {
         programCounter += 4; // zero out or 4 solis
       }
     } else {
       programCounter += 1; // no pseudo instructions
     }
   }
 
   @Override
   void initialization() throws IOException {
     // set our register opcodes
     regMap.put("$0", "0000");
     regMap.put("$s0", "0001");
     regMap.put("$s1", "0010");
     regMap.put("$s2", "0011");
     regMap.put("$s3", "0100");
     regMap.put("$s4", "0101");
     regMap.put("$t0", "0110");
     regMap.put("$t1", "0111");
     regMap.put("$t2", "1000");
     regMap.put("$t3", "1001");
     regMap.put("$a0", "1010");
     regMap.put("$a1", "1011");
     regMap.put("$v0", "1100");
     regMap.put("$v1", "1101");
     regMap.put("$sp", "1110");
     regMap.put("$ra", "1111");
   }
 
   /*
    * Gets the register named "string" for instruction instr. Prints error and
    * quits program with code 1 if register is invalid
    */
   private String getReg(Instruction instr, String reg) {
     String res = regMap.get(reg);
     if (res == null) {
       System.err.println("Error looking up register: " + reg + " at PC "
           + programCounter);
       instr.print();
       System.out.flush();
       System.exit(1);
     }
     return res;
   }
 
   /*
    * check if a string contains only 1's and 0'sused to make sure machine code
    * is binary
    */
   private boolean isBinary(String s) {
     for (int i = 0; i < s.length(); i++) {
       if (s.charAt(i) != '0' && s.charAt(i) != '1') {
         return false;
       }
     }
     return true;
   }
 
   @Override
   void replaceInstructionLabel(Instruction instruction) {
 
     if (instruction.operator.equalsIgnoreCase("jal")
         || instruction.operator.equalsIgnoreCase("j")) {
       // Check that operand is label
       if (!getOperandType(instruction.operands[0].name).equals("label")) {
         printOpErr(instruction);
       }
       // Get address of label from map
       Long addr = labelMap.get(instruction.operands[0].name);
       // Check label exists
       if (addr == null) {
         System.err.println("No label named" + instruction.operands[0].name
             + "\n With pc value: " + programCounter);
         System.exit(1);
       }
       long offset = addr - programCounter;
       // Check for overflow
       if (offset > 127 || offset < -128) {
         System.err.println("Offset Overflow for label: "
             + instruction.operands[0].name + "\n With pc value: "
             + programCounter);
         System.exit(1);
       }
       // subtract programCounter from addr to get offset for jump
       instruction.operands[0].name = String.valueOf(offset);
     }
 
     else if (instruction.operator.equalsIgnoreCase("bne")
         || instruction.operator.equalsIgnoreCase("blt")) {
       // Check that operand is label
       if (!getOperandType(instruction.operands[1].name).equals("label")) {
         printOpErr(instruction);
       }
       // Get address of label from map
       Long addr = labelMap.get(instruction.operands[1].name);
       // Check label exists
       if (addr == null) {
         System.err.println("No label named" + instruction.operands[1].name
             + "\n With pc value: " + programCounter);
         System.exit(1);
       }
       long offset = addr - programCounter;
       // Check for overflow
       if (offset > 31 || offset < -32) {
         System.err.println("Offset Overflow for label: "
             + instruction.operands[1].name + "\n With pc value: "
             + programCounter);
         System.exit(1);
       }
 
       // subtract programCounter from addr to get offset for jump
       instruction.operands[1].name = String.valueOf(offset);
     } else if (instruction.operator.equalsIgnoreCase("la")) {
       // Check that operand is label
       if (!getOperandType(instruction.operands[0].name).equals("label")) {
         printOpErr(instruction);
       }
       // Get address of label from map
       Long addr = labelMap.get(instruction.operands[0].name);
       // Check label exists
       if (addr == null) {
         System.err.println("No label named" + instruction.operands[0].name
             + "\n With pc value: " + programCounter);
         System.exit(1);
       }
       // subtract programCounter from addr to get offset for jump
       instruction.operands[0].name = String.valueOf(addr);
     }
   }
 
   @Override
   void replaceMemoryLabel() {
 
   }
 
   public static void main(String[] arg) throws IOException {
     SixLixAssembler assembler = new SixLixAssembler(arg);
     assembler.AssembleCode(arg);
   }
 
 }
