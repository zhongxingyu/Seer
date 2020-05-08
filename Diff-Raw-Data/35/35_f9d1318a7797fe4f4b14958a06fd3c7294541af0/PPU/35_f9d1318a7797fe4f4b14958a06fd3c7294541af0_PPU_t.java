 package org.perturbed.nesjs.core.client;
 
 
 public class PPU
 {
     private MOS6502 cpu;
     private int cycles;
 	private Memory objectAttributeMemory;
     private Memory ppuMemory;
 
     
 	private boolean ppuScrollAndAddressLatch;
     
 	// PPU_CTRL
     private int xScrollNameTableSelection = 0;
 	private int yScrollNameTableSelection = 0;
 	private int incrementPPUAddressBy_1_32 = 0;
 	private int objectPatternTableSelection = 0;
 	private int backgroundPatternTableSelection = 0;
 	private int spriteSize_8_16 = 0;
 	private int generateNMIAtVBLStart = 0;
 	
 	// PPU_MASK
 	private int disableColour = 0;
 	private int showBackgroundLeftColumn = 0;
 	private int showObjectsLeftColumn = 0;
 	private int enableBackgroundDisplay = 0;
 	private int enableObjectsDisplay = 0;
 	private int intensifyReds = 0;
 	private int intensifyGreens = 0;
 	private int intensifyBlues = 0;
 	
 	// PPU_STATUS
 	private int spriteOverflow = 0;
 	private int sprite0Hit = 0;
 	private int verticalBlankStarted = 0;
 	
 	// OAM_ADDR
 	private int objectAttributeMemoryAddress;
 
 	
 	// PPU register latches & counters
     private int registerVT; // Vertical tile index latch
     private int registerHT; // Horizontal tile index latch
     private int registerFV; // Fine vertical scroll latch
     private int registerFH; // Fine horizontal scroll latch
     private int registerV;  // Vertical name table selection latch
     private int registerH;  // Horizontal name table selection latch
     private int registerS;  // Background pattern table selection latch
     private int counterFV;
     private int counterV;
     private int counterH;
     private int counterVT;
     private int counterHT;
 
    private boolean getPPUSTATUSCalled;

     public void setCPU(final MOS6502 aCPU)
     {
         cpu = aCPU;
     }
 
     public void setPPUMemory(final Memory memory)
     {
         ppuMemory = memory;
     }
 	
 	public void init() 
 	{
 	    cycles = 0;
 	    objectAttributeMemory = new ObjectAttributeMemory();
         ppuScrollAndAddressLatch = true;
        getPPUSTATUSCalled = false;
 
         registerVT = 0;
         registerHT = 0;
         registerFV = 0;
         registerFH = 0;
         registerV  = 0;
         registerH  = 0;
         registerS  = 0;
 
         counterFV = 0;
         counterV  = 0;
         counterH  = 0;
         counterVT = 0;
         counterHT = 0;
 	}
 
 
     /**
      * 262 scanlines
      *
      *   0..19: nothing
      *      20: dummy
      * 21..260: render (240 lines)
      *     261: dummy
      *
      * @param aClockCycles
      */
 	public void clock(final int aClockCycles)
     {
         // PPU runs at 3x the clock rate of the CPU
 		for(int index = 0; index < aClockCycles * 3; index++)
 		{
 			int scanline = cycles / 341;
             int pixelOf = cycles % 341;
 
            // Caution: Reading PPUSTATUS at the exact start of vertical blank will return a 0 in D7
            // but clear the latch anyway, causing the program to miss frames. See NMI for details.
            if(scanline == 0 && pixelOf == 0 && !getPPUSTATUSCalled)
            {
                verticalBlankStarted = 1;
            }
            else if(scanline < 20)
             {
                 // vertical blank, do nothing
             }
             else if(scanline == 20 && pixelOf == 0)
             {
                 // Start of dummy scanline before 'real' scanlines
                 verticalBlankStarted = 0;
             }
             else if(scanline == 261 && pixelOf == 340)
             {
                 // End of last (dummy) scanline
                 if(generateNMIAtVBLStart == 1)
                 {
                     cpu.requestNMI();
                 }
 
                 // Start of new frame
                 cycles = -1;
             }
 
             cycles++;
            getPPUSTATUSCalled = false;
 		}
     }
     
     public void writeRegister(final int anAddress, final int aByte)
     {
         assert anAddress > 0x1FFF && anAddress < 0x2008  : "Tried to write to an out of range PPU address";
         assert aByte <= 0xFF : "Tried to write a value larger than a byte to a PPU address";
         
         switch(anAddress)
         {
             case 0x2000: setPPUCtrl(aByte); break;
             case 0x2001: setPPUMask(aByte); break;
             case 0x2002: setPPUStatus(aByte); break;
             case 0x2003: setOAMAddr(aByte); break;
             case 0x2004: setOAMData(aByte); break;
             case 0x2005: setPPUScroll(aByte); break;
             case 0x2006: setPPUAddr(aByte); break;
             case 0x2007: setPPUData(aByte); break;
         }
     }
 
     public int readRegister(final int anAddress)
     {
         assert anAddress > 0x1FFF && anAddress < 0x2008  : "Tried to read from an out of range PPU address";
         assert anAddress == 0x2002 || anAddress == 0x2004 || anAddress == 0x2007 : "Tried to read from an invalid PPU address";
 
         switch(anAddress)
         {
             case 0x2002: return getPPUStatus();
             case 0x2004: return getOAMData();
             case 0x2007: return getPPUData();
         }
 
         throw new IllegalStateException("Invalid state in PPU");
     }
 
 
 //----------------------------------------------
 // Private interface
 //----------------------------------------------
 
     private int getPPUStatus()
     {
        getPPUSTATUSCalled = true;

     	// Bits 0-4 are 'bits previously written to a PPU register'  Do I need to worry about them?
     	int status = 0;

     	status = Bits.setBit(status, spriteOverflow, 5);
     	status = Bits.setBit(status, sprite0Hit, 6);
     	status = Bits.setBit(status, verticalBlankStarted, 7);
     	
     	verticalBlankStarted = 0; // cleared by the read
     	ppuScrollAndAddressLatch = true; // reset the latch

         return status;
     }
 
     private void setPPUStatus(final int aByte)
     {
         // You shouldn't be able to set the PPU status...
     }
 
     private void setPPUCtrl(final int aByte)
     {
     	xScrollNameTableSelection       = Bits.getBit(aByte, 0);
     	yScrollNameTableSelection       = Bits.getBit(aByte, 1);
     	incrementPPUAddressBy_1_32      = Bits.getBit(aByte, 2);
     	objectPatternTableSelection     = Bits.getBit(aByte, 3);
     	backgroundPatternTableSelection = Bits.getBit(aByte, 4);
     	spriteSize_8_16                 = Bits.getBit(aByte, 5);
     	generateNMIAtVBLStart           = Bits.getBit(aByte, 7);
 
         registerH = Bits.getBit(aByte, 0);
         registerV = Bits.getBit(aByte, 1);
         registerS = Bits.getBit(aByte, 4);
     }
 
     private void setPPUAddr(final int aByte)
     {
         if(ppuScrollAndAddressLatch)
         {
             registerFV = (aByte >> 4) & 0x3;
             registerV  = Bits.getBit(aByte, 3);
             registerH  = Bits.getBit(aByte, 2);
             registerVT = (registerVT & 7) | ((aByte & 3) << 3); // ??
         }
         else
         {
             registerVT = (registerVT & 0x18) | ((aByte >> 5) & 0x7); // ??
             registerHT = aByte & 0x1F;
 
             // Init counters
             counterFV = registerFV;
             counterV  = registerV;
             counterH  = registerH;
             counterVT = registerVT;
             counterHT = registerHT;
         }
 
         ppuScrollAndAddressLatch = !ppuScrollAndAddressLatch;
     }
     
     private int getPPUData()
     {
         final int ppuAddress = getPPUAddressFromCounters();
         final int value = ppuMemory.readByte(ppuAddress);
 
         incrementPPUAddressAndSetCounters(ppuAddress);
 
         return value;
     }
     
     private void setPPUData(final int aByte)
     {
         final int ppuAddress = getPPUAddressFromCounters();
         ppuMemory.writeByte(ppuAddress, aByte);
 
         incrementPPUAddressAndSetCounters(ppuAddress);
     }
 
     private void setPPUScroll(final int aByte)
     {
     	if(ppuScrollAndAddressLatch) 
         {
         	// horizontal
         	registerHT = aByte >> 3;
             registerFH = aByte & 0x7;
         }
         else
         {
         	// vertical
         	registerFV = aByte & 0x7;
         	registerVT = aByte >> 3;
         }
         
         ppuScrollAndAddressLatch = !ppuScrollAndAddressLatch;
     }
 
     private void setOAMAddr(final int aByte)
     {
         objectAttributeMemoryAddress = aByte;
     }
 
     private int getOAMData()
     {
     	return objectAttributeMemory.readByte(objectAttributeMemoryAddress);
     }
 
     private void setOAMData(final int aByte)
     {
     	objectAttributeMemory.writeByte(objectAttributeMemoryAddress, aByte);
 
         // Increment
         objectAttributeMemoryAddress = (objectAttributeMemoryAddress + 1) & 0xFF; // Should it wrap?
     }
     
     private void setPPUMask(final int aByte)
     {
     	disableColour            = Bits.getBit(aByte, 0);
         showBackgroundLeftColumn = Bits.getBit(aByte, 1);
         showObjectsLeftColumn    = Bits.getBit(aByte, 2);
         enableBackgroundDisplay  = Bits.getBit(aByte, 3);
         enableObjectsDisplay     = Bits.getBit(aByte, 4);
         intensifyReds            = Bits.getBit(aByte, 5);
         intensifyGreens          = Bits.getBit(aByte, 6);
         intensifyBlues           = Bits.getBit(aByte, 7);
     }
 
     private int getPPUAddressFromCounters()
     {
         int address  = (counterFV & 0x03) << 12;
         address     |= (counterV  & 0x01) << 11;
         address     |= (counterH  & 0x01) << 10;
         address     |= (counterVT & 0x1F) << 5;
         address     |= (counterHT & 0x1F);
 
         return address & 0x7FFF; // TODO: why is this wrap needed?
     }
 
     private void setCountersFromPPUAddress(final int ppuAddress)
     {
         assert ppuAddress <= 0xFFFF : "Invalid PPU Address";
 
         counterFV = (ppuAddress >> 12) & 0x03;
         counterV  = (ppuAddress >> 11) & 0x01;
         counterH  = (ppuAddress >> 10) & 0x01;
         counterVT = (ppuAddress >> 5)  & 0x1F;
         counterHT = ppuAddress & 0x1F;
     }
 
     private void incrementPPUAddressAndSetCounters(final int ppuAddress)
     {
         final int incrementBy = incrementPPUAddressBy_1_32 == 1 ? 32 : 1;
         final int newPPUAddress = (ppuAddress + incrementBy) & 0xFFFF;  // Should it wrap?
 
         setCountersFromPPUAddress(newPPUAddress);
     }
 }
