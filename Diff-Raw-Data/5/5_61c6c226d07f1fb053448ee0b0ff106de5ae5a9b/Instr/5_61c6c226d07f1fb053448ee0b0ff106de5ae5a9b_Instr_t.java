 /**
  * Copyright (c) 2004, Regents of the University of California
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  *
  * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  *
  * Neither the name of the University of California, Los Angeles nor the
  * names of its contributors may be used to endorse or promote products
  * derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package avrora.core;
 
 
 /**
  * The <code>Instr</code> class and its descendants represent instructions within the
  * assembly code. The visitor pattern is applied here. Each instruction has an
  * <code>accept()</code> method that allows it to be visited with double dispatch
  * by a <code>InstrVisitor</code>. Each instruction in the AVR instruction set
  * is represented by an inner class whose source has been generated from a simple
  * specification.
  *
  * @author Ben L. Titzer
  * @see InstrVisitor
  */
 public abstract class Instr implements InstrPrototype {
 
     public final InstrProperties properties;
 
     public Instr(InstrProperties ip) {
         properties = ip;
     }
 
     /**
      * The <code>getOperands()</code> method returns a string representation
      * of the operands of the instruction. This is useful for printing and
      * tracing of instructions as well as generating listings.
      *
      * @return a string representing the operands of the instruction
      */
     public abstract String getOperands();
 
     /**
      * The <code>getVariant()</code> method returns the variant name of the
      * instruction as a string. Since instructions like load and store have
      * multiple variants, they each have specific variant names to distinguish
      * them internally in the core of Avrora. For example, for "ld x+, (addr)",
      * the variant is "ldpi" (load with post increment), but the actual instruction
      * is "ld", so this method will return "ldpi".
      *
      * @return the variant of the instruction that this prototype represents
      */
     public String getVariant() {
         return properties.variant;
     }
 
     /**
      * The <code>getSize()</code> method returns the size of the instruction
      * in bytes.
      *
      * @return the size of this instruction in bytes
      */
     public int getSize() {
         return properties.size;
     }
 
     /**
      * The <code>getName()</code> method returns the name of the instruction as
      * a string. For instructions that are variants of instructions, this method
      * returns the actual name of the instruction. For example, for "ld x+, (addr)",
      * the variant is "ldpi" (load with post increment), but the actual instruction
      * is "ld", so this method will return "ld".
      *
      * @return the name of the instruction
      */
     public String getName() {
         return properties.name;
     }
 
     public String toString() {
         return getVariant() + " " + getOperands();
     }
 
     /**
      * The <code>getCycles()</code> method returns the number of cylces consumed
      * by the instruction in the default case. Most instructions consume the same
      * amount of clock cycles no matter what behavior. For example, 8-bit arithmetic
      * takes one cycle, load and stores take two cycles, etc. Some instructions like
      * the branch and skip instructions take more cycles if they are taken or not
      * taken. In that case, this count returned is the smallest number of cycles
      * that can be consumed by this instruction.
      *
      * @return the number of cycles that this instruction consumes
      */
     public int getCycles() {
         return properties.cycles;
     }
 
     /**
      * The <code>accept()</code> method is part of the visitor pattern for
      * instructions. The visitor pattern uses two virtual dispatches combined
      * with memory overloading to achieve dispatching on multiple types. The
      * result is clean and modular code.
      *
      * @param v the visitor to accept
      */
     public abstract void accept(InstrVisitor v);
 
     /**
      * The <code>InvalidOperand</code> class represents a runtime error
      * thrown by the constructor of an instruction or the <code>build</code>
      * method of a prototype when an operand does not meet the restrictions
      * imposed by the AVR instruction set architecture.
      */
     public static class InvalidOperand extends RuntimeException {
         /**
          * The <code>number</code> field of the <code>InvalidOperand</code>
          * instance records which operand this error refers to. For example,
          * if the first operand was the source of the problem, then this
          * field will be set to 1.
          */
         public final int number;
 
         InvalidOperand(int num, String msg) {
             super("invalid operand #" + num + ": " + msg);
             number = num;
         }
     }
 
     /**
      * The <code>InvalidRegister</code> class represents an error in
      * constructing an instance of <code>Instr</code> where a register
      * operand does not meet the instruction set specification. For
      * example, the "ldi" instruction can only load values into the
      * upper 16 registers; attempting to create a <code>Instr.LDI</code>
      * instance with a destination register of <code>Register.RO</code>
      * will generate this exception.
      */
     public static class InvalidRegister extends InvalidOperand {
         /**
          * The <code>set</code> field records the expected register set
          * for the operand.
          */
         public final Register.Set set;
 
         /**
          * The <code>register</code> field records the offending register
          * that was found not to be in the expected register set.
          */
         public final Register register;
 
         public InvalidRegister(int num, Register reg, Register.Set s) {
             super(num, "must be one of " + s.contents);
             set = s;
             register = reg;
         }
     }
 
     /**
      * The <code>InvalidImmediate</code> class represents an error in
      * construction of an instance of <code>Instr</code> where the
      * given immediate operand is not within the range that
      * is specified by the instruction set manual. For example, the
      * "sbic" instruction skips the next instruction if the specified
      * bit in the status register is clear. Its operand is expected
      * to be in the range [0, ..., 7]. If the specified operand is
      * not in the range, then this exception will be thrown.
      */
     public static class InvalidImmediate extends InvalidOperand {
 
         /**
          * The <code>low</code> field stores the lowest value that is
          * allowed for this operand.
          */
         public final int low;
 
         /**
          * The <code>high</code> field stores the highest value that is
          * allowed for this operand.
          */
         public final int high;
 
         /**
          * The <code>value</code> field stores the actual value that was
          * passed during the attempeted construction of this instruction.
          */
         public final int value;
 
         public InvalidImmediate(int num, int v, int l, int h) {
             super(num, "value out of required range [" + l + ", " + h + "]");
             low = l;
             high = h;
             value = v;
         }
     }
 
     /**
      * The <code>RegisterRequired</code> class represents an error
      * in construction of an instance of <code>Instr</code> where the
      * given operand is expected to be a register but is not.
      */
     public static class RegisterRequired extends RuntimeException {
 
         public final Operand operand;
 
         RegisterRequired(Operand o) {
             super("register required");
             operand = o;
         }
     }
 
     /**
      * The <code>ImmediateRequired</code> class represents an error
      * in construction of an instance of <code>Instr</code> where the
      * given operand is expected to be an immediate but is not.
      */
     public static class ImmediateRequired extends RuntimeException {
 
         public final Operand operand;
 
         ImmediateRequired(Operand o) {
             super("immediate required");
             operand = o;
         }
     }
 
     /**
      * The <code>WrongNumberOfOperands</code> class represents a runtime
      * error thrown by the <code>build</code> method of a prototype when
      * the wrong number of operands is passed to build an instruction.
      */
     public static class WrongNumberOfOperands extends RuntimeException {
         public final int expected;
         public final int found;
 
         WrongNumberOfOperands(int f, int e) {
             super("wrong number of operands, expected " + e + " and found " + f);
             expected = e;
             found = f;
         }
     }
 
 
     /**
      * U T I L I T Y   F U N C T I O N S
      * ------------------------------------------------------------
      * <p/>
      * These utility functions help in the checking of operands
      * in individual instructions.
      */
     private static void need(int num, Operand[] ops) {
         if (ops.length != num)
             throw new WrongNumberOfOperands(ops.length, num);
     }
 
     private static Register GPR(int num, Register reg) {
         return checkReg(num, reg, Register.GPR_set);
     }
 
     private static Register HGPR(int num, Register reg) {
         return checkReg(num, reg, Register.HGPR_set);
     }
 
     private static Register MGPR(int num, Register reg) {
         return checkReg(num, reg, Register.MGPR_set);
     }
 
     private static Register ADR(int num, Register reg) {
         return checkReg(num, reg, Register.ADR_set);
     }
 
     private static Register RDL(int num, Register reg) {
         return checkReg(num, reg, Register.RDL_set);
     }
 
     private static Register EGPR(int num, Register reg) {
         return checkReg(num, reg, Register.EGPR_set);
     }
 
     private static Register YZ(int num, Register reg) {
         return checkReg(num, reg, Register.YZ_set);
     }
 
     private static Register Z(int num, Register reg) {
         return checkReg(num, reg, Register.Z_set);
     }
 
     private static int IMM3(int num, int val) {
         return checkImm(num, val, 0, 7);
     }
 
     private static int IMM5(int num, int val) {
         return checkImm(num, val, 0, 31);
     }
 
     private static int IMM6(int num, int val) {
         return checkImm(num, val, 0, 63);
     }
 
     private static int IMM8(int num, int val) {
         return checkImm(num, val, 0, 255);
     }
 
     private static int SREL(int pc, int num, int val) {
         return checkImm(num, val - pc - 1, -64, 63);
     }
 
     private static int LREL(int pc, int num, int val) {
         return checkImm(num, val - pc - 1, -2048, 2047);
     }
 
     private static int DADDR(int num, int val) {
         return checkImm(num, val, 0, 65536);
     }
 
     private static int PADDR(int num, int val) {
         // TODO: fix checking of program addresses
         return checkImm(num, val, 0, 65536);
     }
 
     private static int checkImm(int num, int val, int low, int high) {
         if (val < low || val > high) throw new InvalidImmediate(num, val, low, high);
         return val;
 
     }
 
     private static Register checkReg(int num, Register reg, Register.Set set) {
         if (set.contains(reg)) return reg;
         throw new InvalidRegister(num, reg, set);
     }
 
     private static Register REG(Operand o) {
         Operand.Register r = o.asRegister();
         if (r == null) throw new RegisterRequired(o);
         return r.getRegister();
     }
 
     private static int IMM(Operand o) {
         Operand.Constant c = o.asConstant();
         if (c == null) throw new ImmediateRequired(o);
         return c.getValue();
     }
 
     private static int WORD(Operand o) {
         Operand.Constant c = o.asConstant();
         if (c == null) throw new ImmediateRequired(o);
         return c.getValueAsWord();
     }
 
     /**
      * A B S T R A C T   C L A S S E S
      * --------------------------------------------------------
      * <p/>
      * These abstract implementations of the instruction simplify
      * the specification of each individual instruction considerably.
      */
     public abstract static class REGREG_class extends Instr {
         public final Register r1;
         public final Register r2;
 
         REGREG_class(InstrProperties p, Register _r1, Register _r2) {
             super(p);
             r1 = _r1;
             r2 = _r2;
         }
 
         public String getOperands() {
             return r1 + ", " + r2;
         }
 
         public Instr build(int pc, Operand[] ops) {
             need(2, ops);
             return allocate(pc, REG(ops[0]), REG(ops[1]));
         }
 
         abstract Instr allocate(int pc, Register r1, Register r2);
     }
 
     public abstract static class REGIMM_class extends Instr {
         public final Register r1;
         public final int imm1;
 
         REGIMM_class(InstrProperties p, Register r, int i) {
             super(p);
             r1 = r;
             imm1 = i;
         }
 
         public String getOperands() {
             return r1 + ", " + imm1;
         }
 
         public Instr build(int pc, Operand[] ops) {
             need(2, ops);
             return allocate(pc, REG(ops[0]), IMM(ops[1]));
         }
 
         abstract Instr allocate(int pc, Register r1, int imm1);
     }
 
     public abstract static class IMMREG_class extends Instr {
         public final Register r1;
         public final int imm1;
 
         IMMREG_class(InstrProperties p, int i, Register r) {
             super(p);
             r1 = r;
             imm1 = i;
         }
 
         public String getOperands() {
             return imm1 + ", " + r1;
         }
 
         public Instr build(int pc, Operand[] ops) {
             need(2, ops);
             return allocate(pc, IMM(ops[0]), REG(ops[1]));
         }
 
         abstract Instr allocate(int pc, int imm1, Register r1);
     }
 
     public abstract static class REG_class extends Instr {
         public final Register r1;
 
         REG_class(InstrProperties p, Register r) {
             super(p);
             r1 = r;
         }
 
         public String getOperands() {
             return r1.toString();
         }
 
         public Instr build(int pc, Operand[] ops) {
             need(1, ops);
             return allocate(pc, REG(ops[0]));
         }
 
         abstract Instr allocate(int pc, Register r1);
     }
 
     public abstract static class IMMIMM_class extends Instr {
         public final int imm1;
         public final int imm2;
 
         IMMIMM_class(InstrProperties p, int i1, int i2) {
             super(p);
             imm1 = i1;
             imm2 = i2;
         }
 
         public String getOperands() {
             return imm1 + ", " + imm2;
         }
 
         public Instr build(int pc, Operand[] ops) {
             need(2, ops);
             return allocate(pc, IMM(ops[0]), IMM(ops[1]));
         }
 
         abstract Instr allocate(int pc, int imm1, int imm2);
     }
 
     public abstract static class IMMWORD_class extends Instr {
         public final int imm1;
         public final int imm2;
 
         IMMWORD_class(InstrProperties p, int i1, int i2) {
             super(p);
             imm1 = i1;
             imm2 = i2;
         }
 
         public String getOperands() {
             return imm1 + ", " + imm2;
         }
 
         public Instr build(int pc, Operand[] ops) {
             need(2, ops);
             return allocate(pc, IMM(ops[0]), WORD(ops[1]));
         }
 
         abstract Instr allocate(int pc, int imm1, int imm2);
     }
 
     public abstract static class IMM_class extends Instr {
         public final int imm1;
 
         IMM_class(InstrProperties p, int i1) {
             super(p);
             imm1 = i1;
         }
 
         public String getOperands() {
             return "" + imm1;
         }
 
         public Instr build(int pc, Operand[] ops) {
             need(1, ops);
             return allocate(pc, IMM(ops[0]));
         }
 
         abstract Instr allocate(int pc, int imm1);
     }
 
     public abstract static class WORD_class extends Instr {
         public final int imm1;
 
         WORD_class(InstrProperties p, int i1) {
             super(p);
             imm1 = i1;
         }
 
         public String getOperands() {
             return "" + imm1;
         }
 
         public Instr build(int pc, Operand[] ops) {
             need(1, ops);
             return allocate(pc, WORD(ops[0]));
         }
 
         abstract Instr allocate(int pc, int imm1);
     }
 
 
     public abstract static class REGREGIMM_class extends Instr {
         public final Register r1;
         public final Register r2;
         public final int imm1;
 
         REGREGIMM_class(InstrProperties p, Register r1, Register r2, int i1) {
             super(p);
             this.r1 = r1;
             this.r2 = r2;
             imm1 = i1;
         }
 
         public String getOperands() {
            return r1 + ", " + r2 + "+" + imm1;
         }
 
         public Instr build(int pc, Operand[] ops) {
             need(3, ops);
             return allocate(pc, REG(ops[0]), REG(ops[1]), IMM(ops[2]));
         }
 
         abstract Instr allocate(int pc, Register r1, Register r2, int imm1);
     }
 
     public abstract static class REGIMMREG_class extends Instr {
         public final Register r1;
         public final Register r2;
         public final int imm1;
 
         REGIMMREG_class(InstrProperties p, Register r1, int i1, Register r2) {
             super(p);
             this.r1 = r1;
             this.r2 = r2;
             imm1 = i1;
         }
 
         public String getOperands() {
            return r1 + "+" + imm1 + ", " + r2;
         }
 
         public Instr build(int pc, Operand[] ops) {
             need(3, ops);
             return allocate(pc, REG(ops[0]), IMM(ops[1]), REG(ops[2]));
         }
 
         abstract Instr allocate(int pc, Register r1, int imm1, Register r2);
     }
 
     public abstract static class NONE_class extends Instr {
 
         NONE_class(InstrProperties p) {
             super(p);
         }
 
         public String getOperands() {
             return "";
         }
 
         public Instr build(int pc, Operand[] ops) {
             need(0, ops);
             return allocate(pc);
         }
 
         abstract Instr allocate(int pc);
     }
 
     private static int IMM3_default = 0;
     private static int IMM5_default = 0;
     private static int IMM6_default = 0;
     private static int IMM8_default = 0;
     private static int SREL_default = 0;
     private static int LREL_default = 0;
     private static int PADDR_default = 0;
     private static int DADDR_default = 0;
     private static Register GPR_default = Register.R0;
     private static Register MGPR_default = Register.R16;
     private static Register HGPR_default = Register.R16;
     private static Register EGPR_default = Register.R0;
     private static Register ADR_default = Register.X;
     private static Register RDL_default = Register.R24;
     private static Register YZ_default = Register.Y;
     private static Register Z_default = Register.Z;
 
 
     /**
      * I N S T R U C T I O N   D E S C R I P T I O N S
      * ----------------------------------------------------------------
      * <p/>
      * These are the actual instruction descriptions that contain the
      * constraints on operands and sizes, etc.
      * <p/>
      * DO NOT MODIFY THIS CODE!!!!
      */
 //--BEGIN INSTR GENERATOR--
     public static class ADC extends REGREG_class { // add register to register with carry
         static final InstrProperties props = new InstrProperties("adc", "adc", 2, 1);
         static final InstrPrototype prototype = new ADC(0, GPR_default, GPR_default);
 
         Instr allocate(int pc, Register a, Register b) {
             return new ADC(pc, a, b);
         }
 
         public ADC(int pc, Register a, Register b) {
             super(props, GPR(1, a), GPR(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class ADD extends REGREG_class { // add register to register
         static final InstrProperties props = new InstrProperties("add", "add", 2, 1);
         static final InstrPrototype prototype = new ADD(0, GPR_default, GPR_default);
 
         Instr allocate(int pc, Register a, Register b) {
             return new ADD(pc, a, b);
         }
 
         public ADD(int pc, Register a, Register b) {
             super(props, GPR(1, a), GPR(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class ADIW extends REGIMM_class { // add immediate to word register
         static final InstrProperties props = new InstrProperties("adiw", "adiw", 2, 2);
         static final InstrPrototype prototype = new ADIW(0, RDL_default, IMM6_default);
 
         Instr allocate(int pc, Register a, int b) {
             return new ADIW(pc, a, b);
         }
 
         public ADIW(int pc, Register a, int b) {
             super(props, RDL(1, a), IMM6(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class AND extends REGREG_class { // and register with register
         static final InstrProperties props = new InstrProperties("and", "and", 2, 1);
         static final InstrPrototype prototype = new AND(0, GPR_default, GPR_default);
 
         Instr allocate(int pc, Register a, Register b) {
             return new AND(pc, a, b);
         }
 
         public AND(int pc, Register a, Register b) {
             super(props, GPR(1, a), GPR(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class ANDI extends REGIMM_class { // and register with immediate
         static final InstrProperties props = new InstrProperties("andi", "andi", 2, 1);
         static final InstrPrototype prototype = new ANDI(0, HGPR_default, IMM8_default);
 
         Instr allocate(int pc, Register a, int b) {
             return new ANDI(pc, a, b);
         }
 
         public ANDI(int pc, Register a, int b) {
             super(props, HGPR(1, a), IMM8(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class ASR extends REG_class { // arithmetic shift right
         static final InstrProperties props = new InstrProperties("asr", "asr", 2, 1);
         static final InstrPrototype prototype = new ASR(0, GPR_default);
 
         Instr allocate(int pc, Register a) {
             return new ASR(pc, a);
         }
 
         public ASR(int pc, Register a) {
             super(props, GPR(1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class BCLR extends IMM_class { // clear bit in status register
         static final InstrProperties props = new InstrProperties("bclr", "bclr", 2, 1);
         static final InstrPrototype prototype = new BCLR(0, IMM3_default);
 
         Instr allocate(int pc, int a) {
             return new BCLR(pc, a);
         }
 
         public BCLR(int pc, int a) {
             super(props, IMM3(1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class BLD extends REGIMM_class { // load bit from T flag into register
         static final InstrProperties props = new InstrProperties("bld", "bld", 2, 1);
         static final InstrPrototype prototype = new BLD(0, GPR_default, IMM3_default);
 
         Instr allocate(int pc, Register a, int b) {
             return new BLD(pc, a, b);
         }
 
         public BLD(int pc, Register a, int b) {
             super(props, GPR(1, a), IMM3(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class BRBC extends IMMWORD_class { // branch if bit in status register is clear
         static final InstrProperties props = new InstrProperties("brbc", "brbc", 2, 1);
         static final InstrPrototype prototype = new BRBC(0, IMM3_default, SREL_default);
 
         Instr allocate(int pc, int a, int b) {
             return new BRBC(pc, a, b);
         }
 
         public BRBC(int pc, int a, int b) {
             super(props, IMM3(1, a), SREL(pc, 2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class BRBS extends IMMWORD_class { // branch if bit in status register is set
         static final InstrProperties props = new InstrProperties("brbs", "brbs", 2, 1);
         static final InstrPrototype prototype = new BRBS(0, IMM3_default, SREL_default);
 
         Instr allocate(int pc, int a, int b) {
             return new BRBS(pc, a, b);
         }
 
         public BRBS(int pc, int a, int b) {
             super(props, IMM3(1, a), SREL(pc, 2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class BRCC extends WORD_class { // branch if carry flag is clear
         static final InstrProperties props = new InstrProperties("brcc", "brcc", 2, 1);
         static final InstrPrototype prototype = new BRCC(0, SREL_default);
 
         Instr allocate(int pc, int a) {
             return new BRCC(pc, a);
         }
 
         public BRCC(int pc, int a) {
             super(props, SREL(pc, 1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class BRCS extends WORD_class { // branch if carry flag is set
         static final InstrProperties props = new InstrProperties("brcs", "brcs", 2, 1);
         static final InstrPrototype prototype = new BRCS(0, SREL_default);
 
         Instr allocate(int pc, int a) {
             return new BRCS(pc, a);
         }
 
         public BRCS(int pc, int a) {
             super(props, SREL(pc, 1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class BREAK extends NONE_class { // break
         static final InstrProperties props = new InstrProperties("break", "break", 2, 1);
         static final InstrPrototype prototype = new BREAK(0);
 
         Instr allocate(int pc) {
             return new BREAK(pc);
         }
 
         public BREAK(int pc) {
             super(props);
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class BREQ extends WORD_class { // branch if equal
         static final InstrProperties props = new InstrProperties("breq", "breq", 2, 1);
         static final InstrPrototype prototype = new BREQ(0, SREL_default);
 
         Instr allocate(int pc, int a) {
             return new BREQ(pc, a);
         }
 
         public BREQ(int pc, int a) {
             super(props, SREL(pc, 1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class BRGE extends WORD_class { // branch if greater or equal (signed)
         static final InstrProperties props = new InstrProperties("brge", "brge", 2, 1);
         static final InstrPrototype prototype = new BRGE(0, SREL_default);
 
         Instr allocate(int pc, int a) {
             return new BRGE(pc, a);
         }
 
         public BRGE(int pc, int a) {
             super(props, SREL(pc, 1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class BRHC extends WORD_class { // branch if H flag is clear
         static final InstrProperties props = new InstrProperties("brhc", "brhc", 2, 1);
         static final InstrPrototype prototype = new BRHC(0, SREL_default);
 
         Instr allocate(int pc, int a) {
             return new BRHC(pc, a);
         }
 
         public BRHC(int pc, int a) {
             super(props, SREL(pc, 1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class BRHS extends WORD_class { // branch if H flag is set
         static final InstrProperties props = new InstrProperties("brhs", "brhs", 2, 1);
         static final InstrPrototype prototype = new BRHS(0, SREL_default);
 
         Instr allocate(int pc, int a) {
             return new BRHS(pc, a);
         }
 
         public BRHS(int pc, int a) {
             super(props, SREL(pc, 1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class BRID extends WORD_class { // branch if interrupts are disabled
         static final InstrProperties props = new InstrProperties("brid", "brid", 2, 1);
         static final InstrPrototype prototype = new BRID(0, SREL_default);
 
         Instr allocate(int pc, int a) {
             return new BRID(pc, a);
         }
 
         public BRID(int pc, int a) {
             super(props, SREL(pc, 1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class BRIE extends WORD_class { // branch if interrupts are enabled
         static final InstrProperties props = new InstrProperties("brie", "brie", 2, 1);
         static final InstrPrototype prototype = new BRIE(0, SREL_default);
 
         Instr allocate(int pc, int a) {
             return new BRIE(pc, a);
         }
 
         public BRIE(int pc, int a) {
             super(props, SREL(pc, 1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class BRLO extends WORD_class { // branch if lower
         static final InstrProperties props = new InstrProperties("brlo", "brlo", 2, 1);
         static final InstrPrototype prototype = new BRLO(0, SREL_default);
 
         Instr allocate(int pc, int a) {
             return new BRLO(pc, a);
         }
 
         public BRLO(int pc, int a) {
             super(props, SREL(pc, 1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class BRLT extends WORD_class { // branch if less than zero (signed)
         static final InstrProperties props = new InstrProperties("brlt", "brlt", 2, 1);
         static final InstrPrototype prototype = new BRLT(0, SREL_default);
 
         Instr allocate(int pc, int a) {
             return new BRLT(pc, a);
         }
 
         public BRLT(int pc, int a) {
             super(props, SREL(pc, 1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class BRMI extends WORD_class { // branch if minus
         static final InstrProperties props = new InstrProperties("brmi", "brmi", 2, 1);
         static final InstrPrototype prototype = new BRMI(0, SREL_default);
 
         Instr allocate(int pc, int a) {
             return new BRMI(pc, a);
         }
 
         public BRMI(int pc, int a) {
             super(props, SREL(pc, 1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class BRNE extends WORD_class { // branch if not equal
         static final InstrProperties props = new InstrProperties("brne", "brne", 2, 1);
         static final InstrPrototype prototype = new BRNE(0, SREL_default);
 
         Instr allocate(int pc, int a) {
             return new BRNE(pc, a);
         }
 
         public BRNE(int pc, int a) {
             super(props, SREL(pc, 1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class BRPL extends WORD_class { // branch if positive
         static final InstrProperties props = new InstrProperties("brpl", "brpl", 2, 1);
         static final InstrPrototype prototype = new BRPL(0, SREL_default);
 
         Instr allocate(int pc, int a) {
             return new BRPL(pc, a);
         }
 
         public BRPL(int pc, int a) {
             super(props, SREL(pc, 1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class BRSH extends WORD_class { // branch if same or higher
         static final InstrProperties props = new InstrProperties("brsh", "brsh", 2, 1);
         static final InstrPrototype prototype = new BRSH(0, SREL_default);
 
         Instr allocate(int pc, int a) {
             return new BRSH(pc, a);
         }
 
         public BRSH(int pc, int a) {
             super(props, SREL(pc, 1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class BRTC extends WORD_class { // branch if T flag is clear
         static final InstrProperties props = new InstrProperties("brtc", "brtc", 2, 1);
         static final InstrPrototype prototype = new BRTC(0, SREL_default);
 
         Instr allocate(int pc, int a) {
             return new BRTC(pc, a);
         }
 
         public BRTC(int pc, int a) {
             super(props, SREL(pc, 1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class BRTS extends WORD_class { // branch if T flag is set
         static final InstrProperties props = new InstrProperties("brts", "brts", 2, 1);
         static final InstrPrototype prototype = new BRTS(0, SREL_default);
 
         Instr allocate(int pc, int a) {
             return new BRTS(pc, a);
         }
 
         public BRTS(int pc, int a) {
             super(props, SREL(pc, 1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class BRVC extends WORD_class { // branch if V flag is clear
         static final InstrProperties props = new InstrProperties("brvc", "brvc", 2, 1);
         static final InstrPrototype prototype = new BRVC(0, SREL_default);
 
         Instr allocate(int pc, int a) {
             return new BRVC(pc, a);
         }
 
         public BRVC(int pc, int a) {
             super(props, SREL(pc, 1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class BRVS extends WORD_class { // branch if V flag is set
         static final InstrProperties props = new InstrProperties("brvs", "brvs", 2, 1);
         static final InstrPrototype prototype = new BRVS(0, SREL_default);
 
         Instr allocate(int pc, int a) {
             return new BRVS(pc, a);
         }
 
         public BRVS(int pc, int a) {
             super(props, SREL(pc, 1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class BSET extends IMM_class { // set flag in status register
         static final InstrProperties props = new InstrProperties("bset", "bset", 2, 1);
         static final InstrPrototype prototype = new BSET(0, IMM3_default);
 
         Instr allocate(int pc, int a) {
             return new BSET(pc, a);
         }
 
         public BSET(int pc, int a) {
             super(props, IMM3(1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class BST extends REGIMM_class { // store bit in register into T flag
         static final InstrProperties props = new InstrProperties("bst", "bst", 2, 1);
         static final InstrPrototype prototype = new BST(0, GPR_default, IMM3_default);
 
         Instr allocate(int pc, Register a, int b) {
             return new BST(pc, a, b);
         }
 
         public BST(int pc, Register a, int b) {
             super(props, GPR(1, a), IMM3(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class CALL extends WORD_class { // call absolute address
         static final InstrProperties props = new InstrProperties("call", "call", 4, 4);
         static final InstrPrototype prototype = new CALL(0, PADDR_default);
 
         Instr allocate(int pc, int a) {
             return new CALL(pc, a);
         }
 
         public CALL(int pc, int a) {
             super(props, PADDR(1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class CBI extends IMMIMM_class { // clear bit in IO register
         static final InstrProperties props = new InstrProperties("cbi", "cbi", 2, 2);
         static final InstrPrototype prototype = new CBI(0, IMM5_default, IMM3_default);
 
         Instr allocate(int pc, int a, int b) {
             return new CBI(pc, a, b);
         }
 
         public CBI(int pc, int a, int b) {
             super(props, IMM5(1, a), IMM3(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class CBR extends REGIMM_class { // clear bits in register
         static final InstrProperties props = new InstrProperties("cbr", "cbr", 2, 1);
         static final InstrPrototype prototype = new CBR(0, HGPR_default, IMM8_default);
 
         Instr allocate(int pc, Register a, int b) {
             return new CBR(pc, a, b);
         }
 
         public CBR(int pc, Register a, int b) {
             super(props, HGPR(1, a), IMM8(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class CLC extends NONE_class { // clear C flag
         static final InstrProperties props = new InstrProperties("clc", "clc", 2, 1);
         static final InstrPrototype prototype = new CLC(0);
 
         Instr allocate(int pc) {
             return new CLC(pc);
         }
 
         public CLC(int pc) {
             super(props);
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class CLH extends NONE_class { // clear H flag
         static final InstrProperties props = new InstrProperties("clh", "clh", 2, 1);
         static final InstrPrototype prototype = new CLH(0);
 
         Instr allocate(int pc) {
             return new CLH(pc);
         }
 
         public CLH(int pc) {
             super(props);
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class CLI extends NONE_class { // clear I flag
         static final InstrProperties props = new InstrProperties("cli", "cli", 2, 1);
         static final InstrPrototype prototype = new CLI(0);
 
         Instr allocate(int pc) {
             return new CLI(pc);
         }
 
         public CLI(int pc) {
             super(props);
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class CLN extends NONE_class { // clear N flag
         static final InstrProperties props = new InstrProperties("cln", "cln", 2, 1);
         static final InstrPrototype prototype = new CLN(0);
 
         Instr allocate(int pc) {
             return new CLN(pc);
         }
 
         public CLN(int pc) {
             super(props);
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class CLR extends REG_class { // clear register (set to zero)
         static final InstrProperties props = new InstrProperties("clr", "clr", 2, 1);
         static final InstrPrototype prototype = new CLR(0, GPR_default);
 
         Instr allocate(int pc, Register a) {
             return new CLR(pc, a);
         }
 
         public CLR(int pc, Register a) {
             super(props, GPR(1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class CLS extends NONE_class { // clear S flag
         static final InstrProperties props = new InstrProperties("cls", "cls", 2, 1);
         static final InstrPrototype prototype = new CLS(0);
 
         Instr allocate(int pc) {
             return new CLS(pc);
         }
 
         public CLS(int pc) {
             super(props);
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class CLT extends NONE_class { // clear T flag
         static final InstrProperties props = new InstrProperties("clt", "clt", 2, 1);
         static final InstrPrototype prototype = new CLT(0);
 
         Instr allocate(int pc) {
             return new CLT(pc);
         }
 
         public CLT(int pc) {
             super(props);
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class CLV extends NONE_class { // clear V flag
         static final InstrProperties props = new InstrProperties("clv", "clv", 2, 1);
         static final InstrPrototype prototype = new CLV(0);
 
         Instr allocate(int pc) {
             return new CLV(pc);
         }
 
         public CLV(int pc) {
             super(props);
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class CLZ extends NONE_class { // clear Z flag
         static final InstrProperties props = new InstrProperties("clz", "clz", 2, 1);
         static final InstrPrototype prototype = new CLZ(0);
 
         Instr allocate(int pc) {
             return new CLZ(pc);
         }
 
         public CLZ(int pc) {
             super(props);
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class COM extends REG_class { // one's compliment register
         static final InstrProperties props = new InstrProperties("com", "com", 2, 1);
         static final InstrPrototype prototype = new COM(0, GPR_default);
 
         Instr allocate(int pc, Register a) {
             return new COM(pc, a);
         }
 
         public COM(int pc, Register a) {
             super(props, GPR(1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class CP extends REGREG_class { // compare registers
         static final InstrProperties props = new InstrProperties("cp", "cp", 2, 1);
         static final InstrPrototype prototype = new CP(0, GPR_default, GPR_default);
 
         Instr allocate(int pc, Register a, Register b) {
             return new CP(pc, a, b);
         }
 
         public CP(int pc, Register a, Register b) {
             super(props, GPR(1, a), GPR(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class CPC extends REGREG_class { // compare registers with carry
         static final InstrProperties props = new InstrProperties("cpc", "cpc", 2, 1);
         static final InstrPrototype prototype = new CPC(0, GPR_default, GPR_default);
 
         Instr allocate(int pc, Register a, Register b) {
             return new CPC(pc, a, b);
         }
 
         public CPC(int pc, Register a, Register b) {
             super(props, GPR(1, a), GPR(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class CPI extends REGIMM_class { // compare register with immediate
         static final InstrProperties props = new InstrProperties("cpi", "cpi", 2, 1);
         static final InstrPrototype prototype = new CPI(0, HGPR_default, IMM8_default);
 
         Instr allocate(int pc, Register a, int b) {
             return new CPI(pc, a, b);
         }
 
         public CPI(int pc, Register a, int b) {
             super(props, HGPR(1, a), IMM8(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class CPSE extends REGREG_class { // compare registers and skip if equal
         static final InstrProperties props = new InstrProperties("cpse", "cpse", 2, 1);
         static final InstrPrototype prototype = new CPSE(0, GPR_default, GPR_default);
 
         Instr allocate(int pc, Register a, Register b) {
             return new CPSE(pc, a, b);
         }
 
         public CPSE(int pc, Register a, Register b) {
             super(props, GPR(1, a), GPR(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class DEC extends REG_class { // decrement register by one
         static final InstrProperties props = new InstrProperties("dec", "dec", 2, 1);
         static final InstrPrototype prototype = new DEC(0, GPR_default);
 
         Instr allocate(int pc, Register a) {
             return new DEC(pc, a);
         }
 
         public DEC(int pc, Register a) {
             super(props, GPR(1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class EICALL extends NONE_class { // extended indirect call
         static final InstrProperties props = new InstrProperties("eicall", "eicall", 2, 4);
         static final InstrPrototype prototype = new EICALL(0);
 
         Instr allocate(int pc) {
             return new EICALL(pc);
         }
 
         public EICALL(int pc) {
             super(props);
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class EIJMP extends NONE_class { // extended indirect jump
         static final InstrProperties props = new InstrProperties("eijmp", "eijmp", 2, 2);
         static final InstrPrototype prototype = new EIJMP(0);
 
         Instr allocate(int pc) {
             return new EIJMP(pc);
         }
 
         public EIJMP(int pc) {
             super(props);
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class ELPM extends NONE_class { // extended load program memory to r0
         static final InstrProperties props = new InstrProperties("elpm", "elpm", 2, 3);
         static final InstrPrototype prototype = new ELPM(0);
 
         Instr allocate(int pc) {
             return new ELPM(pc);
         }
 
         public ELPM(int pc) {
             super(props);
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class ELPMD extends REGREG_class { // extended load program memory to register
         static final InstrProperties props = new InstrProperties("elpm", "elpmd", 2, 3);
         static final InstrPrototype prototype = new ELPMD(0, GPR_default, Z_default);
 
         Instr allocate(int pc, Register a, Register b) {
             return new ELPMD(pc, a, b);
         }
 
         public ELPMD(int pc, Register a, Register b) {
             super(props, GPR(1, a), Z(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class ELPMPI extends REGREG_class { // extended load program memory to register and post-increment
         static final InstrProperties props = new InstrProperties("elpm", "elpmpi", 2, 3);
         static final InstrPrototype prototype = new ELPMPI(0, GPR_default, Z_default);
 
         Instr allocate(int pc, Register a, Register b) {
             return new ELPMPI(pc, a, b);
         }
 
         public ELPMPI(int pc, Register a, Register b) {
             super(props, GPR(1, a), Z(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class EOR extends REGREG_class { // exclusive or register with register
         static final InstrProperties props = new InstrProperties("eor", "eor", 2, 1);
         static final InstrPrototype prototype = new EOR(0, GPR_default, GPR_default);
 
         Instr allocate(int pc, Register a, Register b) {
             return new EOR(pc, a, b);
         }
 
         public EOR(int pc, Register a, Register b) {
             super(props, GPR(1, a), GPR(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class FMUL extends REGREG_class { // fractional multiply register with register to r0
         static final InstrProperties props = new InstrProperties("fmul", "fmul", 2, 2);
         static final InstrPrototype prototype = new FMUL(0, MGPR_default, MGPR_default);
 
         Instr allocate(int pc, Register a, Register b) {
             return new FMUL(pc, a, b);
         }
 
         public FMUL(int pc, Register a, Register b) {
             super(props, MGPR(1, a), MGPR(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class FMULS extends REGREG_class { // signed fractional multiply register with register to r0
         static final InstrProperties props = new InstrProperties("fmuls", "fmuls", 2, 2);
         static final InstrPrototype prototype = new FMULS(0, MGPR_default, MGPR_default);
 
         Instr allocate(int pc, Register a, Register b) {
             return new FMULS(pc, a, b);
         }
 
         public FMULS(int pc, Register a, Register b) {
             super(props, MGPR(1, a), MGPR(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class FMULSU extends REGREG_class { // signed/unsigned fractional multiply register with register to r0
         static final InstrProperties props = new InstrProperties("fmulsu", "fmulsu", 2, 2);
         static final InstrPrototype prototype = new FMULSU(0, MGPR_default, MGPR_default);
 
         Instr allocate(int pc, Register a, Register b) {
             return new FMULSU(pc, a, b);
         }
 
         public FMULSU(int pc, Register a, Register b) {
             super(props, MGPR(1, a), MGPR(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class ICALL extends NONE_class { // indirect call through Z register
         static final InstrProperties props = new InstrProperties("icall", "icall", 2, 3);
         static final InstrPrototype prototype = new ICALL(0);
 
         Instr allocate(int pc) {
             return new ICALL(pc);
         }
 
         public ICALL(int pc) {
             super(props);
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class IJMP extends NONE_class { // indirect jump through Z register
         static final InstrProperties props = new InstrProperties("ijmp", "ijmp", 2, 2);
         static final InstrPrototype prototype = new IJMP(0);
 
         Instr allocate(int pc) {
             return new IJMP(pc);
         }
 
         public IJMP(int pc) {
             super(props);
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class IN extends REGIMM_class { // read from IO register into register
         static final InstrProperties props = new InstrProperties("in", "in", 2, 1);
         static final InstrPrototype prototype = new IN(0, GPR_default, IMM6_default);
 
         Instr allocate(int pc, Register a, int b) {
             return new IN(pc, a, b);
         }
 
         public IN(int pc, Register a, int b) {
             super(props, GPR(1, a), IMM6(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class INC extends REG_class { // increment register by one
         static final InstrProperties props = new InstrProperties("inc", "inc", 2, 1);
         static final InstrPrototype prototype = new INC(0, GPR_default);
 
         Instr allocate(int pc, Register a) {
             return new INC(pc, a);
         }
 
         public INC(int pc, Register a) {
             super(props, GPR(1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class JMP extends WORD_class { // absolute jump
         static final InstrProperties props = new InstrProperties("jmp", "jmp", 4, 3);
         static final InstrPrototype prototype = new JMP(0, PADDR_default);
 
         Instr allocate(int pc, int a) {
             return new JMP(pc, a);
         }
 
         public JMP(int pc, int a) {
             super(props, PADDR(1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class LD extends REGREG_class { // load from SRAM
         static final InstrProperties props = new InstrProperties("ld", "ld", 2, 2);
         static final InstrPrototype prototype = new LD(0, GPR_default, ADR_default);
 
         Instr allocate(int pc, Register a, Register b) {
             return new LD(pc, a, b);
         }
 
         public LD(int pc, Register a, Register b) {
             super(props, GPR(1, a), ADR(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class LDD extends REGREGIMM_class { // load from SRAM with displacement
         static final InstrProperties props = new InstrProperties("ldd", "ldd", 2, 2);
         static final InstrPrototype prototype = new LDD(0, GPR_default, YZ_default, IMM6_default);
 
         Instr allocate(int pc, Register a, Register b, int c) {
             return new LDD(pc, a, b, c);
         }
 
         public LDD(int pc, Register a, Register b, int c) {
             super(props, GPR(1, a), YZ(2, b), IMM6(3, c));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class LDI extends REGIMM_class { // load immediate into register
         static final InstrProperties props = new InstrProperties("ldi", "ldi", 2, 1);
         static final InstrPrototype prototype = new LDI(0, HGPR_default, IMM8_default);
 
         Instr allocate(int pc, Register a, int b) {
             return new LDI(pc, a, b);
         }
 
         public LDI(int pc, Register a, int b) {
             super(props, HGPR(1, a), IMM8(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class LDPD extends REGREG_class { // load from SRAM with pre-decrement
         static final InstrProperties props = new InstrProperties("ld", "ldpd", 2, 2);
         static final InstrPrototype prototype = new LDPD(0, GPR_default, ADR_default);
 
         Instr allocate(int pc, Register a, Register b) {
             return new LDPD(pc, a, b);
         }
 
         public LDPD(int pc, Register a, Register b) {
             super(props, GPR(1, a), ADR(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class LDPI extends REGREG_class { // load from SRAM with post-increment
         static final InstrProperties props = new InstrProperties("ld", "ldpi", 2, 2);
         static final InstrPrototype prototype = new LDPI(0, GPR_default, ADR_default);
 
         Instr allocate(int pc, Register a, Register b) {
             return new LDPI(pc, a, b);
         }
 
         public LDPI(int pc, Register a, Register b) {
             super(props, GPR(1, a), ADR(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class LDS extends REGIMM_class { // load direct from SRAM
         static final InstrProperties props = new InstrProperties("lds", "lds", 4, 2);
         static final InstrPrototype prototype = new LDS(0, GPR_default, DADDR_default);
 
         Instr allocate(int pc, Register a, int b) {
             return new LDS(pc, a, b);
         }
 
         public LDS(int pc, Register a, int b) {
             super(props, GPR(1, a), DADDR(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class LPM extends NONE_class { // load program memory into r0
         static final InstrProperties props = new InstrProperties("lpm", "lpm", 2, 3);
         static final InstrPrototype prototype = new LPM(0);
 
         Instr allocate(int pc) {
             return new LPM(pc);
         }
 
         public LPM(int pc) {
             super(props);
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class LPMD extends REGREG_class { // load program memory into register
         static final InstrProperties props = new InstrProperties("lpm", "lpmd", 2, 3);
         static final InstrPrototype prototype = new LPMD(0, GPR_default, Z_default);
 
         Instr allocate(int pc, Register a, Register b) {
             return new LPMD(pc, a, b);
         }
 
         public LPMD(int pc, Register a, Register b) {
             super(props, GPR(1, a), Z(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class LPMPI extends REGREG_class { // load program memory into register and post-increment
         static final InstrProperties props = new InstrProperties("lpm", "lpmpi", 2, 3);
         static final InstrPrototype prototype = new LPMPI(0, GPR_default, Z_default);
 
         Instr allocate(int pc, Register a, Register b) {
             return new LPMPI(pc, a, b);
         }
 
         public LPMPI(int pc, Register a, Register b) {
             super(props, GPR(1, a), Z(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class LSL extends REG_class { // logical shift left
         static final InstrProperties props = new InstrProperties("lsl", "lsl", 2, 1);
         static final InstrPrototype prototype = new LSL(0, GPR_default);
 
         Instr allocate(int pc, Register a) {
             return new LSL(pc, a);
         }
 
         public LSL(int pc, Register a) {
             super(props, GPR(1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class LSR extends REG_class { // logical shift right
         static final InstrProperties props = new InstrProperties("lsr", "lsr", 2, 1);
         static final InstrPrototype prototype = new LSR(0, GPR_default);
 
         Instr allocate(int pc, Register a) {
             return new LSR(pc, a);
         }
 
         public LSR(int pc, Register a) {
             super(props, GPR(1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class MOV extends REGREG_class { // copy register to register
         static final InstrProperties props = new InstrProperties("mov", "mov", 2, 1);
         static final InstrPrototype prototype = new MOV(0, GPR_default, GPR_default);
 
         Instr allocate(int pc, Register a, Register b) {
             return new MOV(pc, a, b);
         }
 
         public MOV(int pc, Register a, Register b) {
             super(props, GPR(1, a), GPR(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class MOVW extends REGREG_class { // copy two registers to two registers
         static final InstrProperties props = new InstrProperties("movw", "movw", 2, 1);
         static final InstrPrototype prototype = new MOVW(0, EGPR_default, EGPR_default);
 
         Instr allocate(int pc, Register a, Register b) {
             return new MOVW(pc, a, b);
         }
 
         public MOVW(int pc, Register a, Register b) {
             super(props, EGPR(1, a), EGPR(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class MUL extends REGREG_class { // multiply register with register to r0
         static final InstrProperties props = new InstrProperties("mul", "mul", 2, 2);
         static final InstrPrototype prototype = new MUL(0, GPR_default, GPR_default);
 
         Instr allocate(int pc, Register a, Register b) {
             return new MUL(pc, a, b);
         }
 
         public MUL(int pc, Register a, Register b) {
             super(props, GPR(1, a), GPR(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class MULS extends REGREG_class { // signed multiply register with register to r0
         static final InstrProperties props = new InstrProperties("muls", "muls", 2, 2);
         static final InstrPrototype prototype = new MULS(0, HGPR_default, HGPR_default);
 
         Instr allocate(int pc, Register a, Register b) {
             return new MULS(pc, a, b);
         }
 
         public MULS(int pc, Register a, Register b) {
             super(props, HGPR(1, a), HGPR(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class MULSU extends REGREG_class { // signed/unsigned multiply register with register to r0
         static final InstrProperties props = new InstrProperties("mulsu", "mulsu", 2, 2);
         static final InstrPrototype prototype = new MULSU(0, MGPR_default, MGPR_default);
 
         Instr allocate(int pc, Register a, Register b) {
             return new MULSU(pc, a, b);
         }
 
         public MULSU(int pc, Register a, Register b) {
             super(props, MGPR(1, a), MGPR(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class NEG extends REG_class { // two's complement register
         static final InstrProperties props = new InstrProperties("neg", "neg", 2, 1);
         static final InstrPrototype prototype = new NEG(0, GPR_default);
 
         Instr allocate(int pc, Register a) {
             return new NEG(pc, a);
         }
 
         public NEG(int pc, Register a) {
             super(props, GPR(1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class NOP extends NONE_class { // do nothing operation
         static final InstrProperties props = new InstrProperties("nop", "nop", 2, 1);
         static final InstrPrototype prototype = new NOP(0);
 
         Instr allocate(int pc) {
             return new NOP(pc);
         }
 
         public NOP(int pc) {
             super(props);
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class OR extends REGREG_class { // or register with register
         static final InstrProperties props = new InstrProperties("or", "or", 2, 1);
         static final InstrPrototype prototype = new OR(0, GPR_default, GPR_default);
 
         Instr allocate(int pc, Register a, Register b) {
             return new OR(pc, a, b);
         }
 
         public OR(int pc, Register a, Register b) {
             super(props, GPR(1, a), GPR(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class ORI extends REGIMM_class { // or register with immediate
         static final InstrProperties props = new InstrProperties("ori", "ori", 2, 1);
         static final InstrPrototype prototype = new ORI(0, HGPR_default, IMM8_default);
 
         Instr allocate(int pc, Register a, int b) {
             return new ORI(pc, a, b);
         }
 
         public ORI(int pc, Register a, int b) {
             super(props, HGPR(1, a), IMM8(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class OUT extends IMMREG_class { // write from register to IO register
         static final InstrProperties props = new InstrProperties("out", "out", 2, 1);
         static final InstrPrototype prototype = new OUT(0, IMM6_default, GPR_default);
 
         Instr allocate(int pc, int a, Register b) {
             return new OUT(pc, a, b);
         }
 
         public OUT(int pc, int a, Register b) {
             super(props, IMM6(1, a), GPR(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class POP extends REG_class { // pop from the stack to register
         static final InstrProperties props = new InstrProperties("pop", "pop", 2, 2);
         static final InstrPrototype prototype = new POP(0, GPR_default);
 
         Instr allocate(int pc, Register a) {
             return new POP(pc, a);
         }
 
         public POP(int pc, Register a) {
             super(props, GPR(1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class PUSH extends REG_class { // push register to the stack
         static final InstrProperties props = new InstrProperties("push", "push", 2, 2);
         static final InstrPrototype prototype = new PUSH(0, GPR_default);
 
         Instr allocate(int pc, Register a) {
             return new PUSH(pc, a);
         }
 
         public PUSH(int pc, Register a) {
             super(props, GPR(1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class RCALL extends WORD_class { // relative call
         static final InstrProperties props = new InstrProperties("rcall", "rcall", 2, 3);
         static final InstrPrototype prototype = new RCALL(0, LREL_default);
 
         Instr allocate(int pc, int a) {
             return new RCALL(pc, a);
         }
 
         public RCALL(int pc, int a) {
             super(props, LREL(pc, 1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class RET extends NONE_class { // return to caller
         static final InstrProperties props = new InstrProperties("ret", "ret", 2, 4);
         static final InstrPrototype prototype = new RET(0);
 
         Instr allocate(int pc) {
             return new RET(pc);
         }
 
         public RET(int pc) {
             super(props);
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class RETI extends NONE_class { // return from interrupt
         static final InstrProperties props = new InstrProperties("reti", "reti", 2, 4);
         static final InstrPrototype prototype = new RETI(0);
 
         Instr allocate(int pc) {
             return new RETI(pc);
         }
 
         public RETI(int pc) {
             super(props);
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class RJMP extends WORD_class { // relative jump
         static final InstrProperties props = new InstrProperties("rjmp", "rjmp", 2, 2);
         static final InstrPrototype prototype = new RJMP(0, LREL_default);
 
         Instr allocate(int pc, int a) {
             return new RJMP(pc, a);
         }
 
         public RJMP(int pc, int a) {
             super(props, LREL(pc, 1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class ROL extends REG_class { // rotate left through carry flag
         static final InstrProperties props = new InstrProperties("rol", "rol", 2, 1);
         static final InstrPrototype prototype = new ROL(0, GPR_default);
 
         Instr allocate(int pc, Register a) {
             return new ROL(pc, a);
         }
 
         public ROL(int pc, Register a) {
             super(props, GPR(1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class ROR extends REG_class { // rotate right through carry flag
         static final InstrProperties props = new InstrProperties("ror", "ror", 2, 1);
         static final InstrPrototype prototype = new ROR(0, GPR_default);
 
         Instr allocate(int pc, Register a) {
             return new ROR(pc, a);
         }
 
         public ROR(int pc, Register a) {
             super(props, GPR(1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class SBC extends REGREG_class { // subtract register from register with carry
         static final InstrProperties props = new InstrProperties("sbc", "sbc", 2, 1);
         static final InstrPrototype prototype = new SBC(0, GPR_default, GPR_default);
 
         Instr allocate(int pc, Register a, Register b) {
             return new SBC(pc, a, b);
         }
 
         public SBC(int pc, Register a, Register b) {
             super(props, GPR(1, a), GPR(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class SBCI extends REGIMM_class { // subtract immediate from register with carry
         static final InstrProperties props = new InstrProperties("sbci", "sbci", 2, 1);
         static final InstrPrototype prototype = new SBCI(0, HGPR_default, IMM8_default);
 
         Instr allocate(int pc, Register a, int b) {
             return new SBCI(pc, a, b);
         }
 
         public SBCI(int pc, Register a, int b) {
             super(props, HGPR(1, a), IMM8(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class SBI extends IMMIMM_class { // set bit in IO register
         static final InstrProperties props = new InstrProperties("sbi", "sbi", 2, 2);
         static final InstrPrototype prototype = new SBI(0, IMM5_default, IMM3_default);
 
         Instr allocate(int pc, int a, int b) {
             return new SBI(pc, a, b);
         }
 
         public SBI(int pc, int a, int b) {
             super(props, IMM5(1, a), IMM3(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class SBIC extends IMMIMM_class { // skip if bit in IO register is clear
         static final InstrProperties props = new InstrProperties("sbic", "sbic", 2, 1);
         static final InstrPrototype prototype = new SBIC(0, IMM5_default, IMM3_default);
 
         Instr allocate(int pc, int a, int b) {
             return new SBIC(pc, a, b);
         }
 
         public SBIC(int pc, int a, int b) {
             super(props, IMM5(1, a), IMM3(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class SBIS extends IMMIMM_class { // skip if bit in IO register is set
         static final InstrProperties props = new InstrProperties("sbis", "sbis", 2, 1);
         static final InstrPrototype prototype = new SBIS(0, IMM5_default, IMM3_default);
 
         Instr allocate(int pc, int a, int b) {
             return new SBIS(pc, a, b);
         }
 
         public SBIS(int pc, int a, int b) {
             super(props, IMM5(1, a), IMM3(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class SBIW extends REGIMM_class { // subtract immediate from word
         static final InstrProperties props = new InstrProperties("sbiw", "sbiw", 2, 2);
         static final InstrPrototype prototype = new SBIW(0, RDL_default, IMM6_default);
 
         Instr allocate(int pc, Register a, int b) {
             return new SBIW(pc, a, b);
         }
 
         public SBIW(int pc, Register a, int b) {
             super(props, RDL(1, a), IMM6(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class SBR extends REGIMM_class { // set bits in register
         static final InstrProperties props = new InstrProperties("sbr", "sbr", 2, 1);
         static final InstrPrototype prototype = new SBR(0, HGPR_default, IMM8_default);
 
         Instr allocate(int pc, Register a, int b) {
             return new SBR(pc, a, b);
         }
 
         public SBR(int pc, Register a, int b) {
             super(props, HGPR(1, a), IMM8(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class SBRC extends REGIMM_class { // skip if bit in register cleared
         static final InstrProperties props = new InstrProperties("sbrc", "sbrc", 2, 1);
         static final InstrPrototype prototype = new SBRC(0, GPR_default, IMM3_default);
 
         Instr allocate(int pc, Register a, int b) {
             return new SBRC(pc, a, b);
         }
 
         public SBRC(int pc, Register a, int b) {
             super(props, GPR(1, a), IMM3(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class SBRS extends REGIMM_class { // skip if bit in register set
         static final InstrProperties props = new InstrProperties("sbrs", "sbrs", 2, 1);
         static final InstrPrototype prototype = new SBRS(0, GPR_default, IMM3_default);
 
         Instr allocate(int pc, Register a, int b) {
             return new SBRS(pc, a, b);
         }
 
         public SBRS(int pc, Register a, int b) {
             super(props, GPR(1, a), IMM3(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class SEC extends NONE_class { // set C (carry) flag
         static final InstrProperties props = new InstrProperties("sec", "sec", 2, 1);
         static final InstrPrototype prototype = new SEC(0);
 
         Instr allocate(int pc) {
             return new SEC(pc);
         }
 
         public SEC(int pc) {
             super(props);
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class SEH extends NONE_class { // set H (half carry) flag
         static final InstrProperties props = new InstrProperties("seh", "seh", 2, 1);
         static final InstrPrototype prototype = new SEH(0);
 
         Instr allocate(int pc) {
             return new SEH(pc);
         }
 
         public SEH(int pc) {
             super(props);
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class SEI extends NONE_class { // set I (interrupt enable) flag
         static final InstrProperties props = new InstrProperties("sei", "sei", 2, 1);
         static final InstrPrototype prototype = new SEI(0);
 
         Instr allocate(int pc) {
             return new SEI(pc);
         }
 
         public SEI(int pc) {
             super(props);
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class SEN extends NONE_class { // set N (negative) flag
         static final InstrProperties props = new InstrProperties("sen", "sen", 2, 1);
         static final InstrPrototype prototype = new SEN(0);
 
         Instr allocate(int pc) {
             return new SEN(pc);
         }
 
         public SEN(int pc) {
             super(props);
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class SER extends REG_class { // set bits in register
         static final InstrProperties props = new InstrProperties("ser", "ser", 2, 1);
         static final InstrPrototype prototype = new SER(0, GPR_default);
 
         Instr allocate(int pc, Register a) {
             return new SER(pc, a);
         }
 
         public SER(int pc, Register a) {
             super(props, GPR(1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class SES extends NONE_class { // set S (signed) flag
         static final InstrProperties props = new InstrProperties("ses", "ses", 2, 1);
         static final InstrPrototype prototype = new SES(0);
 
         Instr allocate(int pc) {
             return new SES(pc);
         }
 
         public SES(int pc) {
             super(props);
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class SET extends NONE_class { // set T flag
         static final InstrProperties props = new InstrProperties("set", "set", 2, 1);
         static final InstrPrototype prototype = new SET(0);
 
         Instr allocate(int pc) {
             return new SET(pc);
         }
 
         public SET(int pc) {
             super(props);
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class SEV extends NONE_class { // set V (overflow) flag
         static final InstrProperties props = new InstrProperties("sev", "sev", 2, 1);
         static final InstrPrototype prototype = new SEV(0);
 
         Instr allocate(int pc) {
             return new SEV(pc);
         }
 
         public SEV(int pc) {
             super(props);
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class SEZ extends NONE_class { // set Z (zero) flag
         static final InstrProperties props = new InstrProperties("sez", "sez", 2, 1);
         static final InstrPrototype prototype = new SEZ(0);
 
         Instr allocate(int pc) {
             return new SEZ(pc);
         }
 
         public SEZ(int pc) {
             super(props);
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class SLEEP extends NONE_class { // enter sleep mode
         static final InstrProperties props = new InstrProperties("sleep", "sleep", 2, 1);
         static final InstrPrototype prototype = new SLEEP(0);
 
         Instr allocate(int pc) {
             return new SLEEP(pc);
         }
 
         public SLEEP(int pc) {
             super(props);
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class SPM extends NONE_class { // store to program memory from r0
         static final InstrProperties props = new InstrProperties("spm", "spm", 2, 1);
         static final InstrPrototype prototype = new SPM(0);
 
         Instr allocate(int pc) {
             return new SPM(pc);
         }
 
         public SPM(int pc) {
             super(props);
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class ST extends REGREG_class { // store from register to SRAM
         static final InstrProperties props = new InstrProperties("st", "st", 2, 2);
         static final InstrPrototype prototype = new ST(0, ADR_default, GPR_default);
 
         Instr allocate(int pc, Register a, Register b) {
             return new ST(pc, a, b);
         }
 
         public ST(int pc, Register a, Register b) {
             super(props, ADR(1, a), GPR(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class STD extends REGIMMREG_class { // store from register to SRAM with displacement
         static final InstrProperties props = new InstrProperties("std", "std", 2, 2);
         static final InstrPrototype prototype = new STD(0, YZ_default, IMM6_default, GPR_default);
 
         Instr allocate(int pc, Register a, int b, Register c) {
             return new STD(pc, a, b, c);
         }
 
         public STD(int pc, Register a, int b, Register c) {
             super(props, YZ(1, a), IMM6(2, b), GPR(3, c));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class STPD extends REGREG_class { // store from register to SRAM with pre-decrement
         static final InstrProperties props = new InstrProperties("st", "stpd", 2, 2);
         static final InstrPrototype prototype = new STPD(0, ADR_default, GPR_default);
 
         Instr allocate(int pc, Register a, Register b) {
             return new STPD(pc, a, b);
         }
 
         public STPD(int pc, Register a, Register b) {
             super(props, ADR(1, a), GPR(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class STPI extends REGREG_class { // store from register to SRAM with post-increment
         static final InstrProperties props = new InstrProperties("st", "stpi", 2, 2);
         static final InstrPrototype prototype = new STPI(0, ADR_default, GPR_default);
 
         Instr allocate(int pc, Register a, Register b) {
             return new STPI(pc, a, b);
         }
 
         public STPI(int pc, Register a, Register b) {
             super(props, ADR(1, a), GPR(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class STS extends IMMREG_class { // store direct to SRAM
         static final InstrProperties props = new InstrProperties("sts", "sts", 4, 2);
         static final InstrPrototype prototype = new STS(0, DADDR_default, GPR_default);
 
         Instr allocate(int pc, int a, Register b) {
             return new STS(pc, a, b);
         }
 
         public STS(int pc, int a, Register b) {
             super(props, DADDR(1, a), GPR(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class SUB extends REGREG_class { // subtract register from register
         static final InstrProperties props = new InstrProperties("sub", "sub", 2, 1);
         static final InstrPrototype prototype = new SUB(0, GPR_default, GPR_default);
 
         Instr allocate(int pc, Register a, Register b) {
             return new SUB(pc, a, b);
         }
 
         public SUB(int pc, Register a, Register b) {
             super(props, GPR(1, a), GPR(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class SUBI extends REGIMM_class { // subtract immediate from register
         static final InstrProperties props = new InstrProperties("subi", "subi", 2, 1);
         static final InstrPrototype prototype = new SUBI(0, HGPR_default, IMM8_default);
 
         Instr allocate(int pc, Register a, int b) {
             return new SUBI(pc, a, b);
         }
 
         public SUBI(int pc, Register a, int b) {
             super(props, HGPR(1, a), IMM8(2, b));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class SWAP extends REG_class { // swap nibbles in register
         static final InstrProperties props = new InstrProperties("swap", "swap", 2, 1);
         static final InstrPrototype prototype = new SWAP(0, GPR_default);
 
         Instr allocate(int pc, Register a) {
             return new SWAP(pc, a);
         }
 
         public SWAP(int pc, Register a) {
             super(props, GPR(1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class TST extends REG_class { // test for zero or minus
         static final InstrProperties props = new InstrProperties("tst", "tst", 2, 1);
         static final InstrPrototype prototype = new TST(0, GPR_default);
 
         Instr allocate(int pc, Register a) {
             return new TST(pc, a);
         }
 
         public TST(int pc, Register a) {
             super(props, GPR(1, a));
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 
     public static class WDR extends NONE_class { // watchdog timer reset
         static final InstrProperties props = new InstrProperties("wdr", "wdr", 2, 1);
         static final InstrPrototype prototype = new WDR(0);
 
         Instr allocate(int pc) {
             return new WDR(pc);
         }
 
         public WDR(int pc) {
             super(props);
         }
 
         public void accept(InstrVisitor v) {
             v.visit(this);
         }
     }
 //--END INSTR GENERATOR--
 
 }
