 package com.gigamonkeys.go;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 
 /*
  * Copyright (c) 2013 Peter Seibel
  */
 
 /**
  * The virtual machine for running compiled critters. we actually
  * lightly "compile" the critters, mostly to remove NOPs which
  * includes the actual NOP opcode and also the 217 byte values that
  * aren't legal opcodes, and to make sure all targets are actual start
  * of instructions. (E.g. some instructions, e.g. PUSH have operands
  * and we don't want anyone to jump to the operand and treat it as an
  * instruction. We obviously could let the VM chew on such bytecodes
  * with no problem but if we later decide to do a more serious
  * optimizing compilation of some sort, these semantics will be a lot
  * easier to recreate so instead the address in the bytes is
  * translated to the address of the first instruction at an address
  * greater than or equal to the encoded address.
  */
 public class VM {
 
     // N.B. Certain aspects of the structure of this file are
     // necessary for the script vm.pl to be able to act as a special
     // purpose macro processor for this one file. Lovely, I know.
 
     // No op.
     public final static byte NOP            = 0;
 
     // Memory ops
     public final static byte LOAD           = 1;
     public final static byte STORE          = 2;
 
     // Math ops
     public final static byte ADD            = 3;
     public final static byte SUB            = 4;
     public final static byte MUL            = 5;
     public final static byte DIV            = 6;
     public final static byte MOD            = 7;
     public final static byte INC            = 8;
     public final static byte DEC            = 9;
     public final static byte RAND           = 10;
 
     // Movement ops
     public final static byte FORWARD        = 11;
     public final static byte TURN_AROUND    = 12;
     public final static byte TURN_RIGHT     = 13;
     public final static byte TURN_LEFT      = 14;
     public final static byte POSITION       = 15;
 
     // Logic ops -- kick it CL style.
     public final static byte BOOLE_1        = 16;
     public final static byte BOOLE_2        = 17;
     public final static byte BOOLE_ANDC1    = 18;
     public final static byte BOOLE_ANDC2    = 19;
     public final static byte BOOLE_AND      = 20;
     public final static byte BOOLE_C1       = 21;
     public final static byte BOOLE_C2       = 22;
     public final static byte BOOLE_CLR      = 23;
     public final static byte BOOLE_EQV      = 24;
     public final static byte BOOLE_IOR      = 25;
     public final static byte BOOLE_NAND     = 26;
     public final static byte BOOLE_NOR      = 27;
     public final static byte BOOLE_ORC1     = 28;
     public final static byte BOOLE_ORC2     = 29;
     public final static byte BOOLE_SET      = 30;
     public final static byte BOOLE_XOR      = 31;
 
     // And this one, that doesn't consume two stack items.
     public final static byte NOT            = 32;
 
     // Stack ops -- kick it Forth Style[1]
     public final static byte POP            = 33;
     public final static byte SWAP           = 34;
     public final static byte ROT            = 35;
     public final static byte DUP            = 36;
     public final static byte OVER           = 37;
     public final static byte TUCK           = 38;
     public final static byte PUSH           = 39;
 
     // Flow control ops
     public final static byte IFZERO         = 40;
     public final static byte IFPOS          = 41;
     public final static byte IFNEG          = 42;
     public final static byte IFNZERO        = 43;
     public final static byte IFNPOS         = 44;
     public final static byte IFNNEG         = 45;
     public final static byte GOTO           = 46;
     public final static byte CALL           = 47;
     public final static byte RET            = 48;
     public final static byte STOP           = 49;
 

     // Perception ops
     public final static byte MINE           = 50;
     public final static byte THEIRS         = 51;
     public final static byte EMPTY          = 52;
     public final static byte MINE_GRADIENT  = 53;
     public final static byte THEIR_GRADIENT = 54;
     public final static byte EMPTY_GRADIENT = 55;
 

     public final static String[] NAMES = {
         "NOP",
         "LOAD",
         "STORE",
         "ADD",
         "SUB",
         "MUL",
         "DIV",
         "MOD",
         "INC",
         "DEC",
         "RAND",
         "FORWARD",
         "TURN_AROUND",
         "TURN_RIGHT",
         "TURN_LEFT",
         "POSITION",
         "BOOLE_1",
         "BOOLE_2",
         "BOOLE_ANDC1",
         "BOOLE_ANDC2",
         "BOOLE_AND",
         "BOOLE_C1",
         "BOOLE_C2",
         "BOOLE_CLR",
         "BOOLE_EQV",
         "BOOLE_IOR",
         "BOOLE_NAND",
         "BOOLE_NOR",
         "BOOLE_ORC1",
         "BOOLE_ORC2",
         "BOOLE_SET",
         "BOOLE_XOR",
         "NOT",
         "POP",
         "SWAP",
         "ROT",
         "DUP",
         "OVER",
         "TUCK",
         "PUSH",
         "IFZERO",
         "IFPOS",
         "IFNEG",
         "IFNZERO",
         "IFNPOS",
         "IFNNEG",
         "GOTO",
         "CALL",
         "RET",
         "STOP",
         "MINE",
         "THEIRS",
         "EMPTY",
         "MINE_GRADIENT",
         "THEIR_GRADIENT",
         "EMPTY_GRADIENT",
     };
 
     private final int stackDepth;
     private final int callstackDepth;
     private final int memorySize;
     private final int maxCycles;
     private final Random random = new Random();
 
     VM(int stackDepth, int callstackDepth, int memorySize, int maxCycles) {
         this.stackDepth     = stackDepth;
         this.callstackDepth = callstackDepth;
         this.memorySize     = memorySize;
         this.maxCycles      = maxCycles;
     }
 
     /**
      * The representation of an actual Op. The compile function will
      * convert a byte[] into a DAG of these and return the initial
      * Op. The execute method will then take that Op and execute the
      * DAG. Note that we use the operand field to hold the int value
      * encoded in the two bytes following certain instructions but for
      * all but the PUSH op, that operand is actually an address which
      * the compiler translates into a direct link to the appropriate
      * Op object and stores in the next2 field.
      */
     public static class Op implements Comparable<Op> {
 
         public final byte opcode;
         public final int address;
 
         public int operand;
         public Op next;  // Always the next instruction in the bytecode
         public Op next2; // The other sucessor for branching and jumping instructions.
 
         Op(byte opcode, int address) {
             this.opcode  = opcode;
             this.address = address;
         }
 
         public int compareTo(Op other) {
             return this.address - other.address;
         }
 
         public boolean takesOperand() {
             // Opcodes are arranged to have all the instructions with
             // operands consecutive, making this more efficient.
             return PUSH <= opcode && opcode <= CALL;
         }
 
         private boolean isBranchOrJump() {
             return IFZERO <= opcode && opcode <= CALL;
         }
 
         public String toString() {
             String name = NAMES[opcode];
             if (!takesOperand()) {
                 return name;
             } else if (isBranchOrJump()) {
                 return name + " <" + next2.address + ">";
             } else {
                 return name + " " + operand;
             }
         }
     }
 
     /**
      * Compile bytecodes and return the Op object representing the first instruction.
      */
     public List<Op> compile(byte[] bytecodes) {
         // We're going to collect the Ops so we can resolve jump
         // targets. But in the end we only need to return the starting
         // Op.
         List<Op> ops = new ArrayList<Op>();
 
         // Null object so we don't have to check previous == null all
         // the time during the first loop.
         Op previous = new Op(NOP,-1);
 
         // First pass we pick out the actual opcodes and generate Op
         // objects for each of them. For ops that take an in-bytecode
         // operand (PUSH and all the branching ops) we just store it
         // in operand as a number for now.
         int i = 0;
         while (i < bytecodes.length) {
             byte b = bytecodes[i++];
             if (isOpcode(b)) {
                 Op op = new Op(b, i - 1);
                 try {
                     if (op.takesOperand()) {
                         op.operand = ((bytecodes[i++] & 0xff) << 8) | (bytecodes[i++] & 0xff);
                     }
                     ops.add(op);
                     previous.next = op;
                     previous      = op;
                 } catch (ArrayIndexOutOfBoundsException aioobe) {
                     // Ended with op code for op with operand but not
                     // enough bytes left. We'll just ignore that op.
                 }
             }
         }
 
         // Fill in next2 pointers by finding the first actual Op at an
         // address >= to the address in the bytecode. We add a STOP
         // instruction at the end of the list of ops and then clamp
         // the possible addresses so that anything that jumps past the
         // end of the bytecodes will instead jump to the STOP.
         ops.add(new Op(STOP, bytecodes.length));
         for (Op op: ops) {
             if (op.isBranchOrJump()) {
                 op.next2 = findTargetOp(Math.min(op.operand, bytecodes.length), ops);
             }
         }
         return ops;
     }
 
     private Op findTargetOp(int address, List<Op> ops) {
         int pos = Collections.binarySearch(ops, new Op(NOP, address));
         return pos >= 0 ? ops.get(pos) : ops.get(-1 * (pos + 1));
     }
 
     private boolean isOpcode(byte b) {
         // NOP is an opcode that execute actually can execute but we
         // don't generate an Op for it. And everything greater than
         // the last opcode is also a no-op.
        return NOP < b && b <= RET;
     }
 
     public int execute(Op start,
                        Board board,
                        Color color,
                        int position,
                        int direction,
                        int[] mine,
                        int[] theirs,
                        int[] empty,
                        int[] mineGradient,
                        int[] theirsGradient,
                        int[] emptyGradient)
 
     {
         int tos        = 0;
         int sp         = 0;
         int csp        = 0;
         int cycles     = 0;
 
         int[] stack    = new int[stackDepth];
         Op[] callstack = new Op[callstackDepth];
         int[] memory   = new int[memorySize];
         int size       = board.size;
 
         int tmp;
 
         Op op = start;
 
         try {
             while (op != null) {
                 if (cycles++ > maxCycles) break;
 
                 Op next = null;
 
                 switch (op.opcode) {
                 case NOP:
                     break;
                 case LOAD:
                     tos = memory[tos % memory.length];
                     break;
                 case STORE:
                     memory[tos % memory.length] = stack[--sp];
                     tos = stack[--sp];
                     break;
                 case ADD:
                     tos += stack[--sp];
                     break;
                 case SUB:
                     tos -= stack[--sp];
                     break;
                 case MUL:
                     tos *= stack[-sp];
                     break;
                 case DIV:
                     tmp = stack[--sp];
                     tos = tmp == 0 ? 0 : tos / tmp;
                     break;
                 case MOD:
                     tmp = stack[--sp];
                     tos = tmp == 0 ? 0 : tos % tmp;
                     break;
                 case INC:
                     tos++;
                     break;
                 case DEC:
                     tos--;
                     break;
                 case RAND:
                     stack[sp++] = tos;
                     tos = random.nextInt();
                     break;
                 case FORWARD:
                     switch (direction) {
                     case 0: // North
                         if (position >= size) position -= size;
                         break;
                     case 1: // East
                         if ((position % size) < (size - 1)) position += 1;
                         break;
                     case 2: // South
                         if (position < (size * (size - 1))) position += size;
                         break;
                     case 3: // West
                         if ((position % size) > 0) position -= 1;
                         break;
                     }
                     break;
                 case TURN_AROUND:
                     direction = (direction + 2) % 4;
                     break;
                 case TURN_RIGHT:
                     direction = (direction + 1) % 4;
                     break;
                 case TURN_LEFT:
                     // Not -1 because of % actually being rem not mod.
                     direction = (direction + 3) % 4;
                     break;
                 case POSITION:
                     stack[sp++] = tos;
                     tos = position;
                     break;
                 case BOOLE_1:
                     sp--;
                     break;
                 case BOOLE_2:
                     tos = stack[--sp];
                     break;
                 case BOOLE_ANDC1:
                     tos = ~tos & stack[--sp];
                     break;
                 case BOOLE_ANDC2:
                     tos = tos & ~stack[--sp];
                     break;
                 case BOOLE_AND:
                     tos = tos & stack[--sp];
                     break;
                 case BOOLE_C1:
                     tos = ~tos;
                     sp--;
                     break;
                 case BOOLE_C2:
                     tos = ~stack[--sp];
                     break;
                 case BOOLE_CLR:
                     tos = 0;
                     sp--;
                     break;
                 case BOOLE_EQV:
                     tos = ~(tos ^ stack[--sp]);
                     break;
                 case BOOLE_IOR:
                     tos = tos | stack[--sp];
                     break;
                 case BOOLE_NAND:
                     tos = ~(tos & stack[--sp]);
                     break;
                 case BOOLE_NOR:
                     tos = ~(tos | stack[--sp]);
                     break;
                 case BOOLE_ORC1:
                     tos = ~tos | stack[--sp];
                     break;
                 case BOOLE_ORC2:
                     tos = tos | ~stack[--sp];
                     break;
                 case BOOLE_SET:
                     tos = 0xffffffff;
                     sp--;
                     break;
                 case BOOLE_XOR:
                     tos = tos ^ stack[--sp];
                     break;
                 case NOT:
                     tos = ~tos;
                     break;
                 case POP:
                     tos = stack[--sp];
                     break;
                 case SWAP:
                     tmp = tos;
                     tos = stack[sp - 1];
                     stack[sp - 1] = tmp;
                     break;
                 case ROT:
                     tmp = stack[sp - 3];
                     stack[sp - 3] = stack[sp - 2];
                     stack[sp - 1] = tos;
                     tos = tmp;
                     break;
                 case DUP:
                     stack[sp++] = tos;
                     break;
                 case OVER:
                     stack[sp++] = tos;
                     tos = stack[sp - 2];
                     break;
                 case TUCK:
                     stack[sp] = stack[sp - 1];
                     stack[sp - 1] = tos;
                     sp++;
                     break;
                 case PUSH:
                     stack[sp++] = tos;
                     tos = op.operand;
                     break;
                 case IFZERO:
                     next = tos == 0 ? op.next2 : op.next;
                     tos = stack[--sp];
                     break;
                 case IFPOS:
                     next = tos > 0 ? op.next2 : op.next;
                     tos = stack[--sp];
                     break;
                 case IFNEG:
                     next = tos < 0 ? op.next2 : op.next;
                     tos = stack[--sp];
                     break;
                 case IFNZERO:
                     next = tos != 0 ? op.next2 : op.next;
                     tos = stack[--sp];
                     break;
                 case IFNPOS:
                     next = tos <= 0 ? op.next2 : op.next;
                     tos = stack[--sp];
                     break;
                 case IFNNEG:
                     next = tos >= 0 ? op.next2 : op.next;
                     tos = stack[--sp];
                     break;
                 case GOTO:
                     next = op.next2;
                     break;
                 case CALL:
                     callstack[csp++] = op.next;
                     next = op.next2;
                     break;
                 case RET:
                     next = callstack[--csp];
                     break;
                 case STOP:
                     return position;
                 case MINE:
                     stack[sp++] = tos;
                     tos = mine[position];
                     break;
                 case THEIRS:
                     stack[sp++] = tos;
                     tos = theirs[position];
                     break;
                 case EMPTY:
                     stack[sp++] = tos;
                     tos = empty[position];
                     break;
                 case MINE_GRADIENT:
                     stack[sp++] = tos;
                     tos = mineGradient[position];
                     break;
                 case THEIR_GRADIENT:
                     stack[sp++] = tos;
                     tos = theirsGradient[position];
                     break;
                 case EMPTY_GRADIENT:
                     stack[sp++] = tos;
                     tos = emptyGradient[position];
                     break;
                 default:
                     throw new RuntimeException("Illegal opcode: " + op.opcode);
                 }
                 // Default case, if next hasn't been set, is to simply
                 // move to next instruction.
                 op = next != null ? next : op.next;
             }
         } catch (ArrayIndexOutOfBoundsException aioobe) {
             // Unless we've screwed something up, this can only happen
             // when we overflow or underflow either the operand or
             // call stack. So we'll just treat that as an unorthodox
             // wy of stoping and return the current position. (This is
             // because all other array accesses are constrained such
             // that the indices should always--modulo bugs--be legit.)
         }
         return position;
     }
 
     // [1] see http://galileo.phys.virginia.edu/classes/551.jvn.fall01/primer.htm#param)
 
 }
