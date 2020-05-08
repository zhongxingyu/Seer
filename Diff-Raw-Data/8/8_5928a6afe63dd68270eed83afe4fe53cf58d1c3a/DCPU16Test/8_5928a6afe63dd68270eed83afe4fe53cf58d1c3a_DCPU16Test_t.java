 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.stackframe.pattymelt;
 
 import com.stackframe.pattymelt.DCPU16.Register;
 import org.junit.*;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author mcculley
  */
 public class DCPU16Test {
 
     public DCPU16Test() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
 
     /**
      * Test of initial state.
      */
     @Test
     public void initialStateTest() {
         System.out.println("Testing initial state.");
         DCPU16 cpu = new DCPU16Emulator();
         short[] memory = cpu.memory();
         for (int i = 0; i < 0x10000; i++) {
             assertEquals("memory at " + i, 0, memory[i]);
         }
 
         for (DCPU16.Register r : DCPU16.Register.values()) {
             assertEquals(r.name(), 0, cpu.register(r));
         }
 
         assertEquals("PC", 0, cpu.PC());
        assertEquals("SP", 0, (short) cpu.SP());
         assertEquals("O", 0, cpu.O());
     }
 
     /**
      * Test of simple program defined in v1.1 of the specification.
      */
     @Test
     public void simpleProgramTest() throws IllegalOpcodeException {
         System.out.println("Testing simple program.");
         DCPU16 cpu = new DCPU16Emulator();
         int[] program = new int[]{
             0x7c01,
             0x0030,
             0x7de1,
             0x1000,
             0x0020,
             0x7803,
             0x1000,
             0xc00d,
             0x7dc1,
             0x001a,
             0xa861,
             0x7c01,
             0x2000,
             0x2161,
             0x2000,
             0x8463,
             0x806d,
             0x7dc1,
             0x000d,
             0x9031,
             0x7c10,
             0x0018,
             0x7dc1,
             0x001a,
             0x9037,
             0x61c1,
             0x7dc1,
             0x001a,
             0x0000,
             0x0000,
             0x0000,
             0x0000
         };
         short[] memory = cpu.memory();
         for (int i = 0; i < program.length; i++) {
             memory[i] = (short) program[i];
         }
 
         // SET A, 0x30 ; 7c01 0030
         cpu.step();
         assertEquals("A", 0x30, cpu.A());
         assertEquals("O", 0x0, cpu.O());
         assertEquals("PC", 0x2, cpu.PC());
 
         // SET [0x1000], 0x20 ; 7de1 1000 0020
         cpu.step();
         assertEquals("memory", 0x20, memory[0x1000]);
         assertEquals("O", 0x0, cpu.O());
         assertEquals("PC", 0x5, cpu.PC());
 
         // SUB A, [0x1000] ; 7803 1000
         cpu.step();
         assertEquals("A", 0x10, cpu.A());
         assertEquals("O", 0x0, cpu.O());
         assertEquals("PC", 0x7, cpu.PC());
 
         // IFN A, 0x10      ; c00d 
         //    SET PC, crash ; 7dc1 001a
         cpu.step();
         cpu.step();
         assertEquals("A", 0x10, cpu.A());
         assertEquals("O", 0x0, cpu.O());
         assertEquals("PC", 0xA, cpu.PC());
 
         // SET I, 10 ; a861
         cpu.step();
         assertEquals("I", 0xA, cpu.I());
         assertEquals("O", 0x0, cpu.O());
         assertEquals("PC", 0xB, cpu.PC());
 
         // SET A, 0x2000 ; 7c01 2000
         cpu.step();
         assertEquals("A", 0x2000, cpu.A());
         assertEquals("O", 0x0, cpu.O());
         assertEquals("PC", 0xD, cpu.PC());
 
         // SET [0x2000+I], [A] ; 2161 2000
         cpu.step();
         assertEquals("memory", memory[cpu.A()], memory[0x2000 + 0xA]);
         assertEquals("O", 0x0, cpu.O());
         assertEquals("PC", 0xF, cpu.PC());
 
         // SUB I, 1 ; 8463
         cpu.step();
         assertEquals("I", 0x9, cpu.I());
         assertEquals("O", 0x0, cpu.O());
         assertEquals("PC", 0x10, cpu.PC());
 
         // IFN I, 0        ; 806d
         //    SET PC, loop ; 7dc1 000d [*]
         cpu.step();
         cpu.step();
         assertEquals("I", 0x9, cpu.I());
         assertEquals("O", 0x0, cpu.O());
         assertEquals("PC", 0xD, cpu.PC());
 
         for (int i = 0; i < 9; i++) {
             cpu.step();
             cpu.step();
             cpu.step();
             cpu.step();
         }
 
         assertEquals("PC", 0x13, cpu.PC());
 
         // SET X, 0x4 ; 9031
         cpu.step();
         assertEquals("X", 0x4, cpu.X());
 
         // JSR testsub ; 7c10 0018
         cpu.step();
         assertEquals("PC", 0x18, cpu.PC());
 
         // SHL X, 4 ; 9037
         cpu.step();
         assertEquals("X", 0x40, cpu.X());
 
         // SET PC, POP ; 61c1
         cpu.step();
         assertEquals("PC", 0x16, cpu.PC());
 
         // SET PC, crash ; 7dc1 001a
         cpu.step();
         assertEquals("PC", 0x1A, cpu.PC());
 
         // SET PC, crash ; 7dc1 001a
         cpu.step();
         assertEquals("PC", 0x1A, cpu.PC());
     }
 }
