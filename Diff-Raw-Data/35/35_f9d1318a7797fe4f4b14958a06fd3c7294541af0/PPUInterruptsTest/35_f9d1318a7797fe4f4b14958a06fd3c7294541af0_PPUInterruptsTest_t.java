 package org.perturbed.nesjs.core.blargg;
 
 import org.junit.*;
 import org.perturbed.nesjs.core.blargg.BlarggTestROMTest;
 
 public class PPUInterruptsTest extends BlarggTestROMTest
 {
     @Override
     public String getROMRootPath()
     {
         return "/ppu_vbl_nmi/rom_singles/";
     }
 
     @Test
     public void test01VBLBasics() throws Exception
     {
         runTestROM("01-vbl_basics.nes");
     }
 
     @Test
     public void test02VBLSetTime() throws Exception
     {
         runTestROM("02-vbl_set_time.nes");
     }
 
     @Test
     public void test03VBLClearTime() throws Exception
     {
         runTestROM("03-vbl_clear_time.nes");
     }
 
     @Test
     @Ignore("PPU VBL not implemented yet")
     public void test04NMIControl() throws Exception
     {
         runTestROM("04-nmi_control.nes");
     }
 
     @Test
     @Ignore("PPU VBL not implemented yet")
     public void test05NMITiming() throws Exception
     {
         runTestROM("05-nmi_timing.nes");
     }
 
     @Test
     @Ignore("PPU VBL not implemented yet")
     public void test06Suppression() throws Exception
     {
         runTestROM("06-suppression.nes");
     }
 
     @Test
     @Ignore("PPU VBL not implemented yet")
     public void test07NMIOnTiming() throws Exception
     {
         runTestROM("07-nmi_on_timing.nes");
     }
 
     @Test
     @Ignore("PPU VBL not implemented yet")
     public void test08NMIOffTiming() throws Exception
     {
         runTestROM("08-nmi_off_timing.nes");
     }
 
     @Test
     @Ignore("PPU VBL not implemented yet")
     public void test09EvenOddFrames() throws Exception
     {
         runTestROM("09-even_odd_frames.nes");
     }
 
     @Test
     @Ignore("PPU VBL not implemented yet")
     public void test10EvenOddTiming() throws Exception
     {
         runTestROM("10-even_odd_timing.nes");
     }
 }
