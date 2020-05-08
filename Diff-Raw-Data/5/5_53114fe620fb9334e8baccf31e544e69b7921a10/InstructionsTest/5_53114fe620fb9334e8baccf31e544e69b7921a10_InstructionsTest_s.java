 package tests;
 
 import static org.junit.Assert.assertEquals;
 import mips.Instruction;
 
 import org.junit.Test;
 
 public class InstructionsTest {
 
 	@Test
 	public void add() throws Exception {
 		Instruction i = new Instruction("add $v0, $v1, $a0");
 		assertEquals("funct", Instruction.FUNCT_ADD, i.getFunct());
 		assertEquals("rd", 2, i.getRd());
 		assertEquals("rs", 3, i.getRs());
 		assertEquals("rt", 4, i.getRt());
 	}
 
 	@Test
 	public void sub() throws Exception {
 		Instruction i = new Instruction("sub $a1, $a2, $a3");
 		assertEquals("funct", Instruction.FUNCT_SUB, i.getFunct());
 		assertEquals("rd", 5, i.getRd());
 		assertEquals("rs", 6, i.getRs());
 		assertEquals("rt", 7, i.getRt());
 	}
 
 	@Test
 	public void and() throws Exception {
 		Instruction i = new Instruction("and $t0, $t1, $t2");
 		assertEquals("funct", Instruction.FUNCT_AND, i.getFunct());
 		assertEquals("rd", 8, i.getRd());
 		assertEquals("rs", 9, i.getRs());
 		assertEquals("rt", 10, i.getRt());
 	}
 
 	@Test
 	public void or() throws Exception {
 		Instruction i = new Instruction("or $t3, $t4, $t5");
 		assertEquals("funct", Instruction.FUNCT_OR, i.getFunct());
 		assertEquals("rd", 11, i.getRd());
 		assertEquals("rs", 12, i.getRs());
 		assertEquals("rt", 13, i.getRt());
 	}
 
 	@Test
 	public void nor() throws Exception {
 		Instruction i = new Instruction("nor $t6, $t7, $s0");
 		assertEquals("funct", Instruction.FUNCT_NOR, i.getFunct());
 		assertEquals("rd", 14, i.getRd());
 		assertEquals("rs", 15, i.getRs());
 		assertEquals("rt", 16, i.getRt());
 	}
 
 	@Test
 	public void slt() throws Exception {
 		Instruction i = new Instruction("slt $s1, $s2, $s3");
 		assertEquals("funct", Instruction.FUNCT_SLT, i.getFunct());
 		assertEquals("rd", 17, i.getRd());
 		assertEquals("rs", 18, i.getRs());
 		assertEquals("rt", 19, i.getRt());
 	}
 
 
 	@Test
 	public void lw() throws Exception {
 		Instruction i = new Instruction("lw $t8, 0x50($t9)");
 		assertEquals("opcode", Instruction.OPCODE_LW, i.getOpcode());
		assertEquals("rd", 24, i.getRd());
 		assertEquals("rs", 25, i.getRs());
 		assertEquals("addr", 0x50,  i.getAddr());
 	}
 
 	@Test
 	public void sw() throws Exception {
 		Instruction i = new Instruction("sw $gp, 4($ra)");
 		assertEquals("opcode", Instruction.OPCODE_SW, i.getOpcode());
		assertEquals("rd", 28, i.getRd());
 		assertEquals("rs", 31, i.getRs());
 		assertEquals("addr", 4, i.getAddr());
 	}
 
 	@Test
 	public void beq() throws Exception {
 		Instruction i = new Instruction("beq $sp, $fp, 0x01");
 		assertEquals("opcode", Instruction.OPCODE_BEQ, i.getOpcode());
 		assertEquals("rs", 29, i.getRs());
 		assertEquals("rt", 30, i.getRt());
 		assertEquals("addr", 0x01, i.getAddr());
 	}
 
 	@Test
 	public void exit() throws Exception {
 		Instruction i = new Instruction("exit");
 		assertEquals("exit", true, i.isExit());
 	}
 
 
 
 }
