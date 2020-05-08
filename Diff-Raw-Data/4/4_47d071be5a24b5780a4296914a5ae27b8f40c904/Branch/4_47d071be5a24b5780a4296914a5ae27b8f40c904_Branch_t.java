 package com.github.Icyene.bytecode.generation;
 
 import com.github.Icyene.bytecode.introspection.util.ByteStream;
import com.github.Icyene.bytecode.introspection.util.Bytes;
 
 public class Branch extends Instruction {
     int jump;
 
     public Branch(int opcode, boolean wide, int address, int jump) {
         this.opcode = opcode;
         this.address = address;
         this.jump = jump;
         this.wide = wide;
     }
 
     public byte[] getArguments() {
        return wide ? Bytes.toByteArray(jump) : Bytes.toByteArray((short) jump);
     }
 
     public int getTarget() {
         return jump;
     }
 
     public void setTarget(int target) {
         this.jump = target;
     }
 
     public String toString() {
         return String.format("[Branch @ %s of type %s JUMPS to %s]", address, opcode, jump);
     }
 }
