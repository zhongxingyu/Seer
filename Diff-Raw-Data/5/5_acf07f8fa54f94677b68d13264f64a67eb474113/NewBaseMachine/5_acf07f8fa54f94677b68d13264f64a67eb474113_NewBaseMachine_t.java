 package com.prealpha.dcputil.emulator;
 
 import static com.prealpha.dcputil.emulator.EmulatorHelper.*;
 import static com.prealpha.dcputil.emulator.NewBaseMachine.PointerType.*;
 import static com.prealpha.dcputil.emulator.Opcodes.*;
 import static com.prealpha.dcputil.emulator.Valuecodes.*;
 import static com.prealpha.dcputil.util.PrintUtilities.convertHex;
 
 /**
  * User: Ty
  * Date: 7/19/12
  * Time: 11:46 AM
  */
 class NewBaseMachine {
     public static enum PointerType{
         POINTER_MEMORY,
         POINTER_REGISTER,
         POINTER_PC,
         POINTER_SP,
         POINTER_EX,
         POINTER_LITERAL,
     }
     private class Pointer {
         private final PointerType type;
         private final char pointer;
 
         public Pointer(PointerType type){
             this.type    = type;
             this.pointer = 0xffff;
         }
         public Pointer(PointerType type, char pointerValue){
             this.type = type;
             this.pointer = pointerValue;
         }
 
         public char get(){
             switch(type){
                 case POINTER_MEMORY:
                     return memory[pointer];
                 case POINTER_REGISTER:
                     return registers[pointer];
                 case POINTER_PC:
                     return pc;
                 case POINTER_SP:
                     return sp;
                 case POINTER_EX:
                     return ex;
                 case POINTER_LITERAL:
                     return pointer;
                 default:
                     return 0xffff;
             }
         }
 
         public void set(char data){
             switch(type){
                 case POINTER_MEMORY:
                     memory[pointer]   = data;
                     modified[pointer] = true;
                     break;
                 case POINTER_REGISTER:
                     registers[pointer] = data;
                     break;
                 case POINTER_PC:
                     pc = data;
                     break;
                 case POINTER_SP:
                     sp = data;
                     break;
                 case POINTER_EX:
                     ex = data;
                     break;
                 case POINTER_LITERAL:
                     // Silently fail...
             }
         }
     }
 
     protected boolean isRunning = false;
 
     private static final char A_SIZE  = 6;
     private static final char B_SIZE  = 5;
     private static final char OP_SIZE = 5;
 
     protected char[] registers = new char[0x07+1];
     protected char   sp = 0x0;
     protected char   pc = 0x0;
     protected char   ex = 0x0;
 
     protected char[] lastProgram;
     protected char[] memory = new char[0xffff+1];
     protected boolean[] modified = new boolean[memory.length];
 
     public void load(char[] program){
         System.arraycopy(program, 0, memory, 0, program.length);
         lastProgram = program.clone();
     }
 
     public void step() throws EmulatorException {
         char instruction = memory[pc++];
         char opcode   = clear(instruction, A_SIZE+B_SIZE, 0);
         char opA      = clear(instruction, 0, B_SIZE+OP_SIZE);
         char opB      = clear(instruction, A_SIZE, B_SIZE);
 
        int offset = 0;
         Pointer pa = getPointer(opA, offset, true);
         offset+= getOffset(opA);
         Pointer pb = getPointer(opB, offset, false);
         offset+= getOffset(opB);
 
        pc += offset;
 
         char a = pa.get();
         char b = pb.get();
 
 
         short shortB = (short) a;
         short shortA = (short) b;
 
         switch (opcode){
             case SET:
                 b = a;
                 break;
 
             // Math
             case ADD:
                 ex = over(b+a)? (char) 0x0001 : (char) 0x0;
                 b += a;
                 break;
             case SUB:
                 ex = over(b-a)? (char) 0xffff : (char) 0x0;
                 b -= a;
                 break;
             case MUL:
                 ex = (char)(((b*a)>>16)&0xffff);
                 b *= a;
                 break;
             case MLI:
                 ex = (char)(((shortB*shortA)>>16)&0xffff);
                 shortB *= shortA;
                 b = (char) shortB;
                 break;
             case DIV:
                 if(a==0){
                     b = 0;
                     ex = 0;
                 }
                 else{
                     ex = (char) (((b<<16)/a)&0xffff);
                     b /= a;
                 }
                 break;
             case DVI:
                 if(shortA==0){
                     shortB = 0;
                     b = (char)shortB;
                     ex = 0;
                 }
                 else{
                     ex = (char)(((shortB/shortA)>>16)&0xffff);
                     shortB /= shortA;
                     b = (char) shortB;
                 }
                 break;
             case MOD:
                 if(a==0){
                     b = 0;
                 }
                 else{
                     b %= a;
                 }
                 break;
             case MDI:
                 if(shortA==0){
                     shortB = 0;
                     b = (char) shortB;
                 }
                 else{
                     shortB %= shortA;
                     b = (char) shortB;
                 }
                 break;
 
             // Bit fiddling
             case AND:
                 b &= a;
                 break;
             case BOR:
                 b|=a;
                 break;
             case XOR:
                 b^=a;
                 break;
             case SHR:
                 ex = (char) (((b<<16)>>a)&0xffff);
                 b>>>=a;
                 break;
             case ASR:
                 ex = (char) (((b<<16)>>>a)&0xffff);
                 b>>=a;
                 break;
             case SHL:
                 ex = (char) (((b<<a)>>16)&0xffff);
                 b <<= a;
                 break;
 
 
             // IF statements
             case IFB:
                 if((b&a)!=0){
                     return;
                 }
                 else{
                     skipUntilNonConditional(0);
                 }
                 break;
             case IFC:
                 if((b&a)==0){
                     return;
                 }else{
                     skipUntilNonConditional(0);
                 }
                 break;
             case IFE:
                 if(b==a){
                     return;
                 }else{
                     skipUntilNonConditional(0);
                 }
                 break;
             case IFN:
                 if(b!=a){
                     return;
                 }else{
                     skipUntilNonConditional(0);
                 }
                 break;
             case IFG:
                 if(b>a){
                     return;
                 }
                 else{
                     skipUntilNonConditional(0);
                 }
                 break;
             case IFA:
                 if(b>a){
                     return;
                 }
                 else{
                     skipUntilNonConditional(0);
                 }
                 break;
             case IFL:
                 if(b>a){
                     return;
                 }
                 else{
                     skipUntilNonConditional(0);
                 }
                 break;
             case IFU:
                 if(b<a){
                     return;
                 }
                 else{
                     skipUntilNonConditional(0);
                 }
                 break;
             case ADX:
                 ex = (char) (over(b+a+ex)? 0x0001 : 0x0);
                 b+=a;
                 b+=ex;
                 break;
             case SBX:
                 ex = (char) (over(b-a+ex)? 0xFFFF : 0x0);
                 b-=a;
                 b+=ex;
                 break;
 
             case STI:
                 b = a;
                 registers[registers.length-1] = (char) (registers[registers.length-1]+1);
                 registers[registers.length-2] = (char) (registers[registers.length-2]+1);
                 break;
             case STD:
                 b = a;
                 registers[registers.length-1] = (char) (registers[registers.length-1]-1);
                 registers[registers.length-2] = (char) (registers[registers.length-2]-1);
                 break;
             case SPECIAL:
                 switch (opB){
                     case JSR:
                         opB = PUSH_POP;
                         b = pc;
                         pc = a;
                         break;
                     case BRK:
                         this.isRunning = false;
                         return;
                     case INT:
                     case IAG:
                     case IAS:
                     case FRI:
                     case IAQ:
                     case HWN:
                     case HWQ:
                     case HWI:
                         throw new EmulatorException("Operation not accepted"+convertHex(opB),pc);
                 }
 
         }
 
         pb.set(b);
     }
 
     private Pointer getPointer(char input, int offset, boolean isA) throws EmulatorException {
         if(input <= REGISTER_MAX){
             return new Pointer(POINTER_REGISTER, input);
         }
         if(input <= POINTER_REGISTER_MAX){
             return new Pointer(POINTER_MEMORY,registers[input-POINT_A]);
         }
         if(input <= POINTER_REGISTER_NEXT_MAX){
             return new Pointer(POINTER_MEMORY, (char) (registers[input-POINT_A_NEXT]+memory[pc+offset]));
         }
         if(input == PUSH_POP){
             // POP
             if(isA){
                 return new Pointer(POINTER_MEMORY, (char) (sp++));
             }
             // PUSH
             else{
                 return new Pointer(POINTER_MEMORY, (char) (--sp));
             }
         }
         if(input == PEEK){
             return new Pointer(POINTER_MEMORY, sp);
         }
         if(input==PICK){
             return new Pointer(POINTER_MEMORY, (char)(sp+memory[pc+offset]));
 
         }
         if(input == SP){
             return new Pointer(POINTER_SP);
         }
         if(input == PC){
             return new Pointer(POINTER_PC);
         }
         if(input == EX){
             return new Pointer(POINTER_EX);
         }
         if(input == POINT_NEXT){
             return new Pointer(POINTER_MEMORY, memory[pc+offset]);
         }
         if(input == NEXT){
             return new Pointer(POINTER_MEMORY, (char) (pc+offset));
         }
         if(input>=LITERAL_MIN && input<=LITERAL_MAX){
             return new Pointer(POINTER_LITERAL, (char) (input-0x21));
         }
 
         throw new EmulatorException("cant identify ValueCode: "+(int) input,pc);
     }
 
 //    private void set(char opB, char data){
 //        if(opB <= REGISTER_MAX){
 //            registers[opB] = data;
 //            return;
 //        }
 //        if(opB <= POINTER_REGISTER_MAX){
 //            memory[registers[opB-POINT_A]] = data;
 //            return;
 //        }
 //        if(opB <= POINTER_REGISTER_NEXT_MAX){
 //            memory[registers[opB-POINT_A_NEXT]+memory[pc++]] = data;
 //            return;
 //        }
 //        if(opB == PUSH_POP){
 //            memory[--sp] = data;
 //            modified[sp] = true;
 //            return;
 //        }
 //        if(opB == PEEK){
 //            memory[sp] = data;
 //            modified[sp] = true;
 //            return;
 //        }
 //        if(opB==PICK){
 //            char place = (char) (sp+memory[pc++]);
 //            memory[place] = data;
 //            modified[place] = true;
 //            return;
 //        }
 //        if(opB == SP){
 //            sp = data;
 //            return;
 //        }
 //        if(opB == PC){
 //            pc = data;
 //            return;
 //        }
 //        if(opB == EX){
 //            ex = data;
 //            return;
 //        }
 //        if(opB == POINT_NEXT){
 //            memory[memory[pc++]] = data;
 //        }
 //        if(opB == NEXT){
 //            memory[pc++] = data;
 //        }
 //    }
 //
 //    private char valueOf(char input, boolean isA) throws EmulatorException {
 //        if(input <= REGISTER_MAX){
 //            return registers[input];
 //        }
 //        if(input <= POINTER_REGISTER_MAX){
 //            return memory[registers[input-POINT_A]];
 //        }
 //        if(input <= POINTER_REGISTER_NEXT_MAX){
 //            return memory[registers[input-POINT_A_NEXT]+memory[pc++]];
 //        }
 //        if(input == PUSH_POP){
 //            if(isA){
 //                return memory[sp++];
 //            }
 //            else{
 //                return 0xffff;
 //            }
 //        }
 //        if(input == PEEK){
 //            return memory[sp];
 //        }
 //        if(input==PICK){
 //            char place = 0;
 //            if(isA){
 //                 place = (char)(sp+memory[pc++]);
 //            }
 //            else{
 //                 place = (char)(sp+memory[pc+1]);
 //            }
 //            return place;
 //
 //        }
 //        if(input == SP){
 //            return sp;
 //        }
 //        if(input == PC){
 //            return pc;
 //        }
 //        if(input == EX){
 //            return ex;
 //        }
 //        if(input == POINT_NEXT){
 //            return memory[memory[pc++]];
 //        }
 //        if(input == NEXT){
 //            return memory[pc++];
 //        }
 //        if(input>=LITERAL_MIN && input<=LITERAL_MAX){
 //            return (char)(input-0x21);
 //        }
 //        throw new EmulatorException("can not decode value: "+(int)input,pc);
 //    }
 
     private char getOffset(char valueCode){
         switch(valueCode){
             case POINT_A_NEXT:
             case POINT_B_NEXT:
             case POINT_C_NEXT:
             case POINT_X_NEXT:
             case POINT_Y_NEXT:
             case POINT_Z_NEXT:
             case POINT_I_NEXT:
             case POINT_J_NEXT:
 
             case PICK:
             case POINT_NEXT:
             case NEXT:
                 return 1;
             default:
                 return 0;
         }
     }
 
     private void skipUntilNonConditional(int runs) throws EmulatorException {
         char instruction = memory[pc++];
         char opcode   = clear(instruction, A_SIZE+B_SIZE, 0);
         char opA      = clear(instruction, 0, B_SIZE+OP_SIZE);
         char opB      = clear(instruction, A_SIZE, B_SIZE);
 
         boolean cond = isConditional(opcode);
 
 
         if(cond){
             pc += (getOffset(opA)+getOffset(opB));
             skipUntilNonConditional(0);
         }
         else{
             if(runs==0){
                 pc += (getOffset(opA)+getOffset(opB));
                 return;
             }
             else{
                 pc--;
                 return;
             }
         }
 
     }
 
 }
