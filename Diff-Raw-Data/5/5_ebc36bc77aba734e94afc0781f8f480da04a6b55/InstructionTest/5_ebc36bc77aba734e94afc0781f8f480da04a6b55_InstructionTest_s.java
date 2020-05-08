 package edu.osu.cse.mmxi.junit;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.osu.cse.mmxi.machine.Machine;
 import edu.osu.cse.mmxi.machine.Register;
 import edu.osu.cse.mmxi.machine.interpreter.instructions.Instruction.ADD;
 import edu.osu.cse.mmxi.machine.interpreter.instructions.Instruction.ADDimm;
 import edu.osu.cse.mmxi.machine.interpreter.instructions.Instruction.AND;
 import edu.osu.cse.mmxi.machine.interpreter.instructions.Instruction.ANDimm;
 import edu.osu.cse.mmxi.machine.interpreter.instructions.Instruction.BRx;
 import edu.osu.cse.mmxi.machine.interpreter.instructions.Instruction.DBUG;
 import edu.osu.cse.mmxi.machine.interpreter.instructions.Instruction.JSR;
 import edu.osu.cse.mmxi.machine.interpreter.instructions.Instruction.JSRR;
 import edu.osu.cse.mmxi.machine.interpreter.instructions.Instruction.LD;
 import edu.osu.cse.mmxi.machine.interpreter.instructions.Instruction.LDI;
 import edu.osu.cse.mmxi.machine.interpreter.instructions.Instruction.LDR;
 import edu.osu.cse.mmxi.machine.interpreter.instructions.Instruction.LEA;
 import edu.osu.cse.mmxi.machine.interpreter.instructions.Instruction.NOT;
 import edu.osu.cse.mmxi.machine.interpreter.instructions.Instruction.RET;
 import edu.osu.cse.mmxi.machine.interpreter.instructions.Instruction.ST;
 import edu.osu.cse.mmxi.machine.interpreter.instructions.Instruction.STI;
 import edu.osu.cse.mmxi.machine.interpreter.instructions.Instruction.STR;
 import edu.osu.cse.mmxi.machine.interpreter.instructions.Instruction.TRAP;
 
 /**
  * Tests the instruction directly, instruction parser should probably be checked as well.
  * 
  * @author Will Smelser
  * 
  */
 public class InstructionTest {
     // private final short help = ///Short.parseShort("FEDCBA9876543210", 16);
     private final short   maskAdd    = Short.parseShort("000100000000000", 2);
     private final short   maskAddimm = Short.parseShort("000100000100000", 2);
     private final short   maskAnd    = Short.parseShort("010100000000000", 2);
     private final short   maskAndimm = Short.parseShort("010100000100000", 2);
     private final short   maskBrx    = Short.parseShort("000000000000000", 2);
     private final short   maskDbug   = Short.parseShort("100000000000000", 2);
     private final short   maskJsr    = Short.parseShort("010000000000000", 2);
     private final short   maskJsrR   = Short.parseShort("110000000000000", 2);
     private final short   maskLd     = Short.parseShort("001000000000000", 2);
     private final short   maskLdi    = Short.parseShort("101000000000000", 2);
     private final short   maskLdr    = Short.parseShort("011000000000000", 2);
     private final short   maskLea    = Short.parseShort("111000000000000", 2);
     private final short   maskNot    = Short.parseShort("100100000000000", 2);
     private final short   maskRet    = Short.parseShort("110100000000000", 2);
     private final short   maskSt     = Short.parseShort("001100000000000", 2);
     private final short   maskSti    = Short.parseShort("101100000000000", 2);
     private final short   maskStr    = Short.parseShort("011100000000000", 2);
     private final short   maskTrap   = Short.parseShort("111100000000000", 2);
 
     private final byte    r0         = 0;
     private final byte    r1         = 1;
     private final byte    r2         = 2;
     private final byte    r3         = 3;
     private final byte    r4         = 4;
     private final byte    r5         = 5;
     private final byte    r6         = 6;
     private final byte    r7         = 7;
 
     // private final InstructionParser parser = new InstructionParser();
     private final Machine m          = new Machine();
 
     // silly eclipse was setting all registers to final!
     @Before
     public final void setUp() {
 
     }
 
     /**
      * ADD
      */
 
     @Test
     public final void AddSimpleTest() {
         m.getRegister(r0).setValue((short) 1);
         m.getRegister(r1).setValue((short) 1);
 
         final ADD add = new ADD(r0, r0, r1);
         add.execute(m);
 
         final Register res = m.getRegister(r0);
 
         assertEquals("equal", (short) 2, res.getValue());
     }
 
     // test that the value wraps
     @Test
     public final void AddLimitTest() {
         m.getRegister(r0).setValue(Short.MAX_VALUE);
         m.getRegister(r1).setValue((short) 1);
 
         final ADD add = new ADD(r0, r0, r1);
         add.execute(m);
 
         final Register res = m.getRegister(r0);
 
         assertEquals("equal", Short.MIN_VALUE, res.getValue());
     }
 
     // test 0 + 0
     @Test
     public final void AddZeroTest() {
         m.getRegister(r0).setValue((short) 0);
         m.getRegister(r1).setValue((short) 0);
 
         final ADD add = new ADD(r0, r0, r1);
         add.execute(m);
 
         final Register res = m.getRegister(r0);
 
         assertEquals("equal", 0, res.getValue());
     }
 
     // test 1 + 0
     @Test
     public final void AddZero2Test() {
         m.getRegister(r0).setValue((short) 1);
         m.getRegister(r1).setValue((short) 0);
 
         final ADD add = new ADD(r0, r0, r1);
         add.execute(m);
 
         final Register res = m.getRegister(r0);
 
         assertEquals("equal", 1, res.getValue());
     }
 
     /**
      * ADD IMMEDIATE
      */
 
     @Test
     public final void AddSimpleImmTest() {
         m.getRegister(r0).setValue((short) 1);
         m.getRegister(r1).setValue((short) 1);
 
         // m.setMemory((byte) 0, (short) 1, (short) 1);
 
         final ADDimm add = new ADDimm(r0, r1, 1);
         add.execute(m);
 
         final Register res = m.getRegister(r0);
 
         assertEquals("equal", (short) 2, res.getValue());
     }
 
     @Test
     public final void AddSimpleLimitImmTest() {
         m.getRegister(r0).setValue((short) 1);
         m.getRegister(r1).setValue((short) 1);
 
         // m.setMemory((byte) 0, (short) 1, (short) 15);
 
         final ADDimm add = new ADDimm(r0, r1, 15);
         add.execute(m);
 
         final Register res = m.getRegister(r0);
 
         assertEquals("equal", (short) 16, res.getValue());
     }
 
     @Test
     public final void AddSimpleLimit2ImmTest() {
         m.getRegister(r0).setValue((short) 1);
         m.getRegister(r1).setValue((short) 1);
 
         m.setMemory((byte) 0, (short) 1, (short) 1);
 
         final ADDimm add = new ADDimm(r0, r1, 16);
         add.execute(m);
 
         final Register res = m.getRegister(r0);
 
         assertEquals("equal", (short) -15, res.getValue());
     }
 
     /**
      * AND TESTING
      */
 
     @Test
     public final void AndNotNotTest() {
         m.getRegister(r0).setValue((short) 0);
         m.getRegister(r1).setValue((short) 0);
 
         final AND and = new AND(r0, r1, r0);
         and.execute(m);
 
         final Register res = m.getRegister(r0);
 
         assertEquals("equal", (short) 0, res.getValue());
     }
 
     @Test
     public final void AndNotTrueTest() {
         m.getRegister(r0).setValue((short) 0);
         m.getRegister(r1).setValue((short) 1);
 
         final AND and = new AND(r0, r1, r0);
         and.execute(m);
 
         final Register res = m.getRegister(r0);
 
         assertEquals("equal", (short) 0, res.getValue());
     }
 
     @Test
     public final void AndTrueTrueTest() {
         m.getRegister(r0).setValue((short) 1);
         m.getRegister(r1).setValue((short) 1);
 
         final AND and = new AND(r0, r1, r0);
         and.execute(m);
 
         final Register res = m.getRegister(r0);
 
         assertEquals("equal", (short) 1, res.getValue());
     }
 
     /*
      * And testing with more than just right most bit
      */
 
     @Test
     public final void AndNotTrue2Test() {
         m.getRegister(r0).setValue((short) 0);
         m.getRegister(r1).setValue((short) 2);
 
         final AND and = new AND(r0, r1, r0);
         and.execute(m);
 
         final Register res = m.getRegister(r0);
 
         assertEquals("equal", (short) 0, res.getValue());
     }
 
     @Test
     public final void AndTrueTrue2Test() {
         m.getRegister(r0).setValue((short) 2);
         m.getRegister(r1).setValue((short) 2);
 
         final AND and = new AND(r0, r1, r0);
         and.execute(m);
 
         final Register res = m.getRegister(r0);
 
         assertEquals("equal", (short) 2, res.getValue());
     }
 
     /**
      * IMM
      */
 
     @Test
     public final void AndNotNotImmTest() {
         m.getRegister(r0).setValue((short) 0);
         m.getRegister(r1).setValue((short) 0);
 
         final ANDimm and = new ANDimm(r0, r0, 0);
         and.execute(m);
 
         final Register res = m.getRegister(r0);
 
         assertEquals("equal", (short) 0, res.getValue());
     }
 
     @Test
     public final void AndNotTrueImmTest() {
         m.getRegister(r0).setValue((short) 0);
         m.getRegister(r1).setValue((short) 1);
 
         final ANDimm and = new ANDimm(r0, r0, 1);
         and.execute(m);
 
         final Register res = m.getRegister(r0);
 
         assertEquals("equal", (short) 0, res.getValue());
     }
 
     @Test
     public final void AndTrueTrueImmTest() {
         m.getRegister(r0).setValue((short) 1);
         m.getRegister(r1).setValue((short) 1);
 
         final ANDimm and = new ANDimm(r0, r0, 1);
         and.execute(m);
 
         final Register res = m.getRegister(r0);
 
         assertEquals("equal", (short) 1, res.getValue());
     }
 
     /*
      * And testing with more than just right most bit
      */
 
     @Test
     public final void AndNotTrue2ImmTest() {
         m.getRegister(r0).setValue((short) 0);
         m.getRegister(r1).setValue((short) 2);
 
         final ANDimm and = new ANDimm(r0, r0, 2);
         and.execute(m);
 
         final Register res = m.getRegister(r0);
 
         assertEquals("equal", (short) 0, res.getValue());
     }
 
     @Test
     public final void AndTrueTrue2ImmTest() {
         m.getRegister(r0).setValue((short) 2);
         m.getRegister(r1).setValue((short) 2);
 
         final ANDimm and = new ANDimm(r0, r1, 2);
         and.execute(m);
 
         final Register res = m.getRegister(r0);
 
         assertEquals("equal", (short) 2, res.getValue());
 
     }
 
     /**
      * BranchX MAX = 111111111 = 511 = 0x1FF; atleast 1 of NZP must be set;
      */
     @Test
     public final void BranchAlwaysSimpleTest() {
         m.getPCRegister().setValue((short) 0);
 
         // 1 register must always be set
         m.getFlags().setN(true);
         m.getFlags().setP(false);
         m.getFlags().setZ(false);
 
         final short start = m.getPCRegister().getValue();
         final BRx brx = new BRx(7, 10);
         brx.execute(m);
 
         final Register pc = m.getPCRegister();
 
         assertEquals("equal", start + 10, pc.getValue());
     }
 
     @Test
     public final void BranchNbitSimpleTest() {
         m.getPCRegister().setValue((short) 0);
 
         m.getFlags().setN(true);
         m.getFlags().setP(false);
         m.getFlags().setZ(false);
 
         final short start = m.getPCRegister().getValue();
         final BRx brx = new BRx(4, 10);
         brx.execute(m);
 
         final Register pc = m.getPCRegister();
 
         assertEquals("equal", start + 10, pc.getValue());
     }
 
     @Test
     public final void BranchZbitSimpleTest() {
         m.getPCRegister().setValue((short) 0);
 
         m.getFlags().setN(false);
         m.getFlags().setZ(true);
         m.getFlags().setP(false);
 
         final short start = m.getPCRegister().getValue();
         final BRx brx = new BRx(2, 10);
         brx.execute(m);
 
         final Register pc = m.getPCRegister();
 
         assertEquals("equal", start + 10, pc.getValue());
     }
 
     @Test
     public final void BranchPbitSimpleTest() {
         m.getPCRegister().setValue((short) 0);
 
         m.getFlags().setN(false);
         m.getFlags().setZ(false);
         m.getFlags().setP(true);
 
         final short start = m.getPCRegister().getValue();
         final BRx brx = new BRx(1, 10);
         brx.execute(m);
 
         final Register pc = m.getPCRegister();
 
         assertEquals("equal", start + 10, pc.getValue());
     }
 
     // try to offset to last entry on page
     @Test
     public final void BranchPageLimitSimpleTest() {
         m.getPCRegister().setValue((short) 0);
 
         m.getFlags().setN(false);
         m.getFlags().setZ(true);
         m.getFlags().setP(false);
 
         final short start = m.getPCRegister().getValue();
         final BRx brx = new BRx(2, 511);
         brx.execute(m);
 
         final Register pc = m.getPCRegister();
 
         assertEquals("equal", start + 511, pc.getValue());
     }
 
     /**
      * DEBUG Have to look at console to verify
      * 
      * R0 0001 R1 0002 R2 0003 R3 0004 FLAGS n R4 0005 R5 0006 R6 0007 R7 0008 PC 03E7
      */
     @Test
     public final void dbugTest() {
         m.getFlags().setN(true);
         m.getFlags().setZ(false); // default
         m.getFlags().setP(false);
         m.getPCRegister().setValue((short) 999); // 999=x3E7
         m.getMemory((short) 1);
         m.setMemory((short) 1, (short) 2);
         m.getRegister(r0).setValue((short) 1);
         m.getRegister(r1).setValue((short) 2);
         m.getRegister(r2).setValue((short) 3);
         m.getRegister(r3).setValue((short) 4);
         m.getRegister(r4).setValue((short) 5);
         m.getRegister(r5).setValue((short) 6);
         m.getRegister(r6).setValue((short) 7);
         m.getRegister(r7).setValue((short) 8);
 
         final DBUG dbug = new DBUG();
         dbug.execute(m);
 
     }
 
     /**
      * JSR tests
      */
     @Test
     public final void jsrSimpleNoLinkTest() {
         m.getPCRegister().setValue((short) 0);
         final short start = m.getPCRegister().getValue();
 
         final JSR jsr = new JSR(false, 100);
         jsr.execute(m);
 
         final Register pc = m.getPCRegister();
 
         assertEquals("equal", start + 100, pc.getValue());
 
     }
 
     @Test
     public final void jsrSimpleLinkTest() {
         m.getPCRegister().setValue((short) 0);
         final short start = m.getPCRegister().getValue();
 
         final JSR jsr = new JSR(true, 100);
         jsr.execute(m);
 
         final Register pc = m.getPCRegister();
 
         assertEquals("equal", start + 100, pc.getValue());
         assertEquals(start, m.getRegister(r7).getValue());
     }
 
     /*
      * JSRR
      */
     @Test
     public final void jsrrSimpleNoLinkTest() {
         m.getRegister(r0).setValue((short) 100);
 
         m.getPCRegister().setValue((short) 0);
         final short start = m.getPCRegister().getValue();
 
         final JSRR jsrr = new JSRR(false, r0, 100);
         jsrr.execute(m);
 
         final Register pc = m.getPCRegister();
 
         assertEquals("equal", start + 200, pc.getValue());
 
     }
 
     @Test
     public final void jsrrSimpleLinkTest() {
         m.getRegister(r0).setValue((short) 100);
 
         m.getPCRegister().setValue((short) 0);
         final short start = m.getPCRegister().getValue();
 
         final JSRR jsrr = new JSRR(true, r0, 100);
         jsrr.execute(m);
 
         final Register pc = m.getPCRegister();
 
         assertEquals("equal", start + 200, pc.getValue());
         assertEquals(start, m.getRegister(r7).getValue());
     }
 
     /**
      * LD testing Load memory to register
      */
     @Test
     public final void ldSimpleTest() {
         m.getPCRegister().setValue((short) 0);
         final short start = m.getPCRegister().getValue();
         m.setMemory((short) 100, (short) 99);
 
         final LD ld = new LD(r0, (short) 100);
         ld.execute(m);
 
         assertEquals((short) 99, m.getRegister(r0).getValue());
     }
 
     /**
      * LDI testing Load memory indirect
      */
     @Test
     public final void ldiSimpleTest() {
         m.getPCRegister().setValue((short) 0);
         final short start = m.getPCRegister().getValue();
         m.setMemory((short) 100, (short) 99);
 
         final LDI ldi = new LDI(r0, (short) 100);
         ldi.execute(m);
 
         assertEquals(m.getMemory((short) 99), m.getRegister(r0).getValue());
     }
 
     /**
      * LDR
      */
     @Test
     public final void ldrNoOffsetSimpleTest() {
         m.getPCRegister().setValue((short) 0);
         m.setMemory((short) 100, (short) 12);
 
         m.getRegister(r0).setValue((short) 9);
         m.getRegister(r2).setValue((short) 100);
 
         final LDR ldr = new LDR(r0, r2, 0);
         ldr.execute(m);
 
         assertEquals((short) 12, m.getRegister(r0).getValue());
     }
 
     /**
      * LEA load the PC + offset => registerX
      */
     @Test
     public final void leaSimpleTest() {
         m.getPCRegister().setValue((short) 0);
         m.setMemory((short) 100, (short) 12);
         m.getRegister(r0).setValue((short) 9);
 
         final LEA lea = new LEA(r0, 100);
         lea.execute(m);
        assertEquals((short) 12, m.getRegister(r0).getValue());
     }
 
     /**
      * NOT
      */
     @Test
     public final void notSimpleTest() {
         m.getRegister(r0).setValue((short) 2);
         m.getRegister(r1).setValue((short) 2);
 
         final NOT not = new NOT(r0, r1);
         not.execute(m);
         assertEquals((short) -3, m.getRegister(r0).getValue());
     }
 
     @Test
     public final void notBigNegSimpleTest() {
         m.getRegister(r0).setValue((short) 2);
         m.getRegister(r1).setValue((short) (0xFFFF - 1));
 
         final NOT not = new NOT(r0, r1);
         not.execute(m);
         assertEquals((short) 1, m.getRegister(r0).getValue());
     }
 
     /**
      * RET Put R7 into PC
      */
     public final void retSimpleTest() {
         m.getPCRegister().setValue((short) 100);
         m.getRegister(r7).setValue((short) 200);
 
         final RET ret = new RET();
         ret.execute(m);
 
         assertEquals((short) 200, m.getPCRegister().getValue());
     }
 
     /**
      * ST - take SR and store into memory at pageOffset
      */
     public final void stSimpleTest() {
         m.getPCRegister().setValue((short) 0);
         m.getRegister(r0).setValue((short) 100);
 
         final ST st = new ST(r0, 200);
         st.execute(m);
 
         assertEquals(100, m.getMemory((short) 100));
     }
 
     /**
      * STI - take
      */
     public final void stiSimpleTest() {
         m.getPCRegister().setValue((short) 0);
         m.setMemory((short) 100, (short) 200);
         m.getRegister(r0).setValue((short) 201);
 
         final STI sti = new STI(r0, 100);
         sti.execute(m);
 
         assertEquals(201, m.getMemory((short) 200));
     }
 
     /**
      * STR
      */
     public final void strSimpleTest() {
         m.getPCRegister().setValue((short) 0);
         m.setMemory((short) 100, (short) 99);
         m.getRegister(r0).setValue((short) 200);
         m.getRegister(r1).setValue((short) 100);
 
         final STR str = new STR(r0, r1, (short) 0);
         str.execute(m);
 
         assertEquals(m.getMemory((short) 100), 200);
     }
 
     public final void strSimpleWithOffsetTest() {
         m.getPCRegister().setValue((short) 0);
         m.setMemory((short) 101, (short) 99);
         m.getRegister(r0).setValue((short) 200);
         m.getRegister(r1).setValue((short) 100);
 
         final STR str = new STR(r0, r1, (short) 1);
         str.execute(m);
 
         assertEquals(m.getMemory((short) 100), 200);
     }
 
     /**
      * TRAPS - SEE ManualJUnitTest.java
      */
 
     @Test
     public final void trapx21NoError() {
         m.getRegister(r0).setValue((short) 0x73);// s
 
         final TRAP trap = new TRAP(0x21);
         trap.execute(m);
     }
 
     // cannot really test this, look for console for exiting
     @Test
     public final void trapx25() {
 
         assertEquals(m.hasHalted(), false);
 
         final TRAP trap = new TRAP(0x25);
         trap.execute(m);
 
         assertEquals(m.hasHalted(), true);
     }
 
     // Tested Loading short 5 into offset 10
     @Test
     public final void LDTest() {
         short fin;
         m.getRegister(r0).setValue((short) 1);
         m.setMemory((short) (m.getPCRegister().getValue() & 0xfe00 | (short) 10),
             (short) 5);
         final LD ld = new LD(r0, 10);
         ld.execute(m);
         fin = m.getMemory((short) (m.getPCRegister().getValue() & 0xfe00 | (short) 10));
 
         assertEquals("equals", (short) 5, fin);
 
     }
 
     // Tested Loading Indirect, First i loaded the offset Address 10 with value of 5.
     // Then loaded the address of 5 with value of 2
     @Test
     public final void LDITest() {
         short fin;
         m.getRegister(r0).setValue((short) 1);
         m.setMemory((short) (m.getPCRegister().getValue() & 0xfe00 | (short) 10),
             (short) 5);
         m.setMemory((short) (m.getPCRegister().getValue() & 0xfe00 | (short) 5),
             (short) 2);
         final LDI ldi = new LDI(r0, 10);
         ldi.execute(m);
         fin = m.getMemory((short) (m.getPCRegister().getValue() & 0xfe00 | (short) 5));
 
         assertEquals("equals", (short) 2, fin);
 
     }
 
     /**
      * Testing LDR, added index of 4 to register 1.
      */
     @Test
     public final void LDRTest() {
 
         m.getRegister(r0).setValue((short) 1);
         m.getRegister(r1).setValue((short) 2);
         m.getRegister(r5).setValue((short) 2);
         final LDR ldr = new LDR(r0, r2, 4);
         ldr.execute(m);
 
         final Register fin = m.getRegister(r5);
 
         assertEquals("equals", (short) 2, fin.getValue());
 
     }
 
     /**
      * Testing LEA... not really sure how to.... sort of???
      */
     @Test
     public final void LEATest() {
         final short fin;
         m.getPCRegister().setValue((short) 0);
         m.setMemory((short) 10, (short) 99);
         m.getRegister(r0).setValue((short) 1);
         m.getRegister(r1).setValue((short) 2);
 
         final LEA lea = new LEA(r0, 10);
         lea.execute(m);
        assertEquals("equals", (short) 99, m.getRegister(r0).getValue());
     }
 
     /**
      * Testing NOT
      */
     @Test
     public final void NOTTest() {
         m.getRegister(r0).setValue((short) 1);
         m.getRegister(r1).setValue((short) 2);
 
         final NOT not = new NOT(r0, r1);
         not.execute(m);
 
         final Register res = m.getRegister(r0);
         assertEquals("equal", ~2, res.getValue());
 
     }
 
     /**
      * Testing Return
      */
     @Test
     public final void RETTest() {
         m.getRegister(r7).setValue((short) 1);
         final RET ret = new RET();
         ret.execute(m);
         final Register pc = m.getPCRegister();
 
         assertEquals("equal", 1, pc.getValue());
 
     }
 }
