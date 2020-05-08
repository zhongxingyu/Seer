 /**
  * Copyright (c) 2004-2005, Regents of the University of California
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
 
 package avrora.sim.mcu;
 
 import avrora.sim.Simulator;
 import avrora.sim.State;
 import avrora.sim.RWRegister;
 import avrora.util.Arithmetic;
 
 import java.util.LinkedList;
 
 /**
  * The USART class implements a Universal Synchronous Asynchronous Receiver/Transmitter, which is a
  * serial device on the ATMega128. The ATMega128 has two USARTs, USART0 and USART1.
  *
  * @author Daniel Lee
  */
 public abstract class USART extends AtmelInternalDevice {
 
     // UNIMPLEMENTED:
     // Synchronous Mode
     // Multi-processor communication mode
 
     /*
       Ways in which this USART is not accurate: Whole frame
       delayed transmission, as opposed to sending single bits
       at a time.  Parity errors are not searched for.
       Presumably, parity errors will not occur. Similarly,
       frame errors are should not occur.
      */
 
 
     final DataRegister UDRn_reg;
     final ControlRegisterA UCSRnA_reg;
     final ControlRegisterB UCSRnB_reg;
     final ControlRegisterC UCSRnC_reg;
     final UBRRnLReg UBRRnL_reg;
     final UBRRnHReg UBRRnH_reg;
 
     final Transmitter transmitter;
     final Receiver receiver;
 
     public USARTDevice connectedDevice;
 
     final int n;
 
     int USARTnRX;
     int USARTnUDRE;
     int USARTnTX;
 
     int[] INTERRUPT_MAPPING;
 
     //boolean UDREnFlagged;
 
     static final int RXCn = 7;
     static final int TXCn = 6;
     static final int UDREn = 5;
     static final int FEn = 4;
     static final int DORn = 3;
     static final int UPEn = 2;
     static final int U2Xn = 1;
     static final int MPCMn = 0;
 
     static final int RXCIEn = 7;
     static final int TXCIEn = 6;
     static final int UDRIEn = 5;
     static final int RXENn = 4;
     static final int TXENn = 3;
     static final int UCSZn2 = 2;
     static final int RXB8n = 1;
     static final int TXB8n = 0;
 
     // bit 7 is reserved
     static final int UMSELn = 6;
     static final int UPMn1 = 5;
     static final int UPMn0 = 4;
     static final int USBSn = 3;
     static final int UCSZn1 = 2;
     static final int UCSZn0 = 1;
     static final int UCPOLn = 0;
 
     // parity modes
     static final int PARITY_DISABLED = 0;
     // 2 is reserved
     static final int PARITY_EVEN = 2;
     static final int PARITY_ODD = 3;
 
     // TODO: Frame sizes are not used!!
     static final int[] SIZE = {5, 6, 7, 8, 8, 8, 8, 9};
 
     int period = 0;
     int UBRRMultiplier = 16;
     int frameSize = 8; // does this default to 5?
 
     /**
      * The <code>USARTDevice</code> interface describes USARTs and other serial devices which can be connected
      * to the USART. For simplicity, a higher-level interface communicating by frames of data is used, rather
      * than bits or a representation of changing voltages.
      */
     public interface USARTDevice {
         /**
          * Transmit a frame from this device.
          *
          * @return the frame for transmission
          */
         public Frame transmitFrame();
 
 
         /**
          * Receive a frame.
          *
          * @param frame the frame to be received
          */
         public void receiveFrame(Frame frame);
 
     }
 
     /**
      * A <code>USARTFrame</code> is a representation of the serial frames being passed between the USART
      * and a connected device.
      */
     public static class Frame {
         public final byte low;
         public final boolean high;
         final int size;
 
         /**
          * Constructor for a USARTFrame. The <code>high</code> bit is used for 9 bit frame sizes.
          */
         public Frame(byte low, boolean high, int size) {
             this.low = low;
             this.high = high;
             this.size = size;
         }
 
         /**
          * Returns the integer value of this data frame.
          *
          * @return intended value of this data frame
          */
         public int value() {
             int value = 0;
             switch (size) {
                 case 9:
                     value = high ? 0x100 : 0x0;
                     value |= 0xff & low;
                     break;
                 case 8:
                     value = 0xff & low;
                     break;
                 case 7:
                     value = 0x7f & low;
                     break;
                 case 6:
                     value = 0x3f & low;
                     break;
                 case 5:
                     value = 0x1f & low;
                     break;
             }
             return value;
         }
 
         public String toString() {
             if (size == 9) {
                 return "" + value();
             } else {
                 return "" + (char)value() + " (" + value() + ')';
             }
         }
     }
 
     /* *********************************************** */
     /* Methods to implement the USARTDevice interface */
 
     public Frame transmitFrame() {
         return new Frame(UDRn_reg.transmitRegister.read(), UCSRnB_reg.readBit(TXB8n), frameSize);
     }
 
     public void receiveFrame(Frame frame) {
         UDRn_reg.receiveRegister.writeFrame(frame);
     }
 
     /* *********************************************** */
 
     /**
      * Initialize the parameters such as interrupt numbers and I/O register numbers that make this
      * USART unique.
      */
     protected abstract void initValues();
 
     protected USART(int n, AtmelMicrocontroller m) {
         super("usart"+n, m);
         this.n = n;
 
         initValues();
 
         UDRn_reg = new DataRegister();
 
         UCSRnA_reg = new ControlRegisterA();
         UCSRnB_reg = new ControlRegisterB();
         UCSRnC_reg = new ControlRegisterC();
         UBRRnL_reg = new UBRRnLReg();
         UBRRnH_reg = new UBRRnHReg();
 
         transmitter = new Transmitter();
         receiver = new Receiver();
 
         installIOReg("UDR"+n, UDRn_reg);
         installIOReg("UCSR"+n+"A", UCSRnA_reg);
         installIOReg("UCSR"+n+"B", UCSRnB_reg);
         installIOReg("UCSR"+n+"C", UCSRnC_reg);
         installIOReg("UBRR"+n+"L", UBRRnL_reg);
         installIOReg("UBRR"+n+"H", UBRRnH_reg);
 
         // USART Receive Complete
         installInterrupt("USART: receive", USARTnRX,
                 new ATMegaFamily.MaskableInterrupt(USARTnRX, UCSRnB_reg, UCSRnA_reg, RXCn, false));
         // USART Data Register Empty
         installInterrupt("USART: empty", USARTnUDRE,
                 new ATMegaFamily.MaskableInterrupt(USARTnUDRE, UCSRnB_reg, UCSRnA_reg, UDREn, false));
         // USART Transmit Complete
         installInterrupt("USART transmit", USARTnTX,
                 new ATMegaFamily.MaskableInterrupt(USARTnTX, UCSRnB_reg, UCSRnA_reg, TXCn, false));
     }
 
     public void connect(USARTDevice d) {
         connectedDevice = d;
     }
 
     void updatePeriod() {
         period = read16(UBRRnH_reg, UBRRnL_reg) + 1;
         period *= UBRRMultiplier;
     }
 
     protected class Transmitter {
         boolean transmitting = false;
         Transmitter.Transmit transmit = new Transmitter.Transmit();
 
         protected void enableTransmit() {
             if (!transmitting) {
                 transmit.frame = transmitFrame();
                 UCSRnA_reg.flagBit(UDREn);
                 transmitting = true;
                 mainClock.insertEvent(transmit, (1 + frameSize + stopBits) * period);
             }
         }
 
         protected class Transmit implements Simulator.Event {
             Frame frame;
 
             public void fire() {
                 connectedDevice.receiveFrame(frame);
 
 
                 if (devicePrinter.enabled)
                     devicePrinter.println("USART: Transmitted frame " + frame /*+ " " + simulator.getState().getCycles()*/);
                 transmitting = false;
                 UCSRnA_reg.flagBit(TXCn);
                 if (!UCSRnA_reg.readBit(UDREn)) {
                     transmitter.enableTransmit();
                 }
             }
         }
     }
 
     /**
      * Initiate a receive between the UART and the connected device.
      */
     public void startReceive() {
         receiver.enableReceive();
     }
 
     protected class Receiver {
 
         boolean receiving = false;
         Receiver.Receive receive = new Receiver.Receive();
 
         protected void enableReceive() {
             if (!receiving) {
                 receive.frame = connectedDevice.transmitFrame();
                 mainClock.insertEvent(receive, (1 + frameSize + stopBits) * period);
                 receiving = true;
             }
         }
 
 
         protected class Receive implements Simulator.Event {
             Frame frame;
 
             public void fire() {
                 receiveFrame(frame);
 
                 if (devicePrinter.enabled)
                     devicePrinter.println("USART: Received frame " + frame + ' ' + UBRRnH_reg.read() + ' ' + UBRRnL_reg.read() + ' ' + UBRRMultiplier + ' ');
 
                 UCSRnA_reg.flagBit(RXCn);
 
                 receiving = false;
             }
         }
     }
 
     /**
      * The <code>DataRegister</code> class represents a Transmit Data Buffer Register for a USART. It
      * is really two registers, a transmit register and a receive register. The transmit register is
      * the destination of data written to the register at this address. The receive register is the
      * source of data read from this address.
      */
     protected class DataRegister extends RWRegister {
         RWRegister transmitRegister;
         DataRegister.TwoLevelFIFO receiveRegister;
 
         DataRegister() {
             transmitRegister = new RWRegister();
             receiveRegister = new DataRegister.TwoLevelFIFO();
         }
 
         public void write(byte val) {
             // check UDREn flag
 
             if (UCSRnA_reg.readBit(UDREn)) {
                 transmitRegister.write(val);
                 UCSRnA_reg.writeBit(UDREn, false);
                 if (UCSRnB_reg.readBit(TXENn)) {
                     transmitter.enableTransmit();
                 }
             }
         }
 
         public void writeBit(int bit, boolean val) {
             // check UDREn flag
             if (UCSRnA_reg.readBit(UDREn)) {
                 transmitRegister.writeBit(bit, val);
                 UCSRnA_reg.writeBit(UDREn, false);
                 if (UCSRnB_reg.readBit(TXENn)) transmitter.enableTransmit();
 
             }
         }
 
         public byte read() {
             UCSRnA_reg.writeBit(RXCn,true);
             return receiveRegister.read();
         }
 
         public boolean readBit(int bit) {
             return receiveRegister.readBit(bit);
         }
 
 
         /**
          * An implementation of the FIFO used to buffer the received frames. This is not quite a
          * two-level FIFO, as the shift-receive register in the actual implementation can act as a
          * third level to the buffer. In order to account for this, the FIFO is implemented as a queue
          * that can hold at most three elements (limited by the implementation). Although the
          * implementation does not mirror the how the hardware does this, functionally it should
          * behave the same way.
          */
         private class TwoLevelFIFO extends RWRegister {
 
             LinkedList readyQueue;
             LinkedList waitQueue;
 
             TwoLevelFIFO() {
                 readyQueue = new LinkedList();
                 waitQueue = new LinkedList();
                 waitQueue.add(new DataRegister.TwoLevelFIFO.USARTFrameWrapper());
                 waitQueue.add(new DataRegister.TwoLevelFIFO.USARTFrameWrapper());
                 waitQueue.add(new DataRegister.TwoLevelFIFO.USARTFrameWrapper());
             }
 
             public boolean readBit(int bit) {
                 return Arithmetic.getBit(bit, read());
             }
 
 
             public byte read() {
                 if (readyQueue.isEmpty()) {
                     return (byte)0;
                 }
                 DataRegister.TwoLevelFIFO.USARTFrameWrapper current = (DataRegister.TwoLevelFIFO.USARTFrameWrapper)readyQueue.removeLast();
                 if (readyQueue.isEmpty()) {
                     UCSRnA_reg.writeBit(RXCn, false);
                 }
                 UCSRnB_reg.writeBit(RXB8n, current.frame.high);
                 waitQueue.add(current);
                 return current.frame.low;
             }
 
             public void writeFrame(Frame frame) {
                 if (waitQueue.isEmpty()) {
                     // data overrun. drop frame
                     UCSRnA_reg.writeBit(DORn, true);
                 } else {
                     DataRegister.TwoLevelFIFO.USARTFrameWrapper current = (DataRegister.TwoLevelFIFO.USARTFrameWrapper)(waitQueue.removeLast());
                     current.frame = frame;
                     readyQueue.addFirst(current);
                 }
             }
 
             protected void flush() {
                 while (!waitQueue.isEmpty()) {
                     // empty the wait queue. fill the ready queue.
                     readyQueue.add(waitQueue.removeLast());
                 }
             }
 
             // TODO: why does this class exist??
             private class USARTFrameWrapper {
                 Frame frame;
             }
 
         }
 
     }
 
 
     /**
      * UCSRnA (<code>ControlRegisterA</code>) is one of three control/status registers for the USART.
      * The high three bits are actually interrupt flag bits.
      */
     protected class ControlRegisterA extends ATMegaFamily.FlagRegister  {
 
         public ControlRegisterA() {
             super(USART.this.interpreter, INTERRUPT_MAPPING);
             value = 0x20; // init UDREn to true
 
         }
 
         public void write(byte val) {
             super.write((byte)(0xe3 & val));
             decode(val);
 
         }
 
         public void writeBit(int bit, boolean val) {
 
             if( bit == 7){
                 //OL: just unpost the int, do not clear RCXn
                 //thus, we cannot use the super call
                 interpreter.unpostInterrupt(INTERRUPT_MAPPING[bit]);
             } else if (bit < 1 || bit > 4) {
                 super.writeBit(bit, val);
             }
             decode(value);
 
         }
 
         protected void decode(byte val) {
             boolean U2XnVal = readBit(U2Xn);
             boolean MPCMnVal = UCSRnC_reg.readBit(UMSELn);
 
             int multiplierState = U2XnVal ? 0x1 : 0;
             multiplierState |= MPCMnVal ? 0x2 : 0;
 
 
             switch (multiplierState) {
                 case 0:
                     UBRRMultiplier = 16;
                     break;
                 case 1:
                     UBRRMultiplier = 8;
                     break;
                 case 2:
                     UBRRMultiplier = 2;
                     break;
                 default:
                     UBRRMultiplier = 2;
                     break;
             }
         }
 
     }
 
     /**
      * UCSRnB (<code>ControlRegisterB</code>) is one of three control/status registers for the USART.
      * The high three bits are actually interrupt mask bits.
      */
     protected class ControlRegisterB extends ATMegaFamily.MaskRegister {
         int count = 0;
 
         ControlRegisterB() {
             super(USART.this.interpreter, INTERRUPT_MAPPING, UCSRnA_reg);
             UCSRnA_reg.maskRegister = this;
         }
 
         public void write(byte val) {
             super.write(val);
             decode(val);
         }
 
         public void writeBit(int bit, boolean val) {
             super.writeBit(bit, val);
             decode(value);
         }
 
         protected void decode(byte value) {
 
             if (readBit(UCSZn2) && UCSRnC_reg.readBit(UCSZn1)
                     && UCSRnC_reg.readBit(UCSZn0)) {
                 frameSize = 9;
             }
         }
     }
 
     int stopBits = 1;
 
 
     /**
      * UCSRnC (<code>ControlRegisterC</code>) is one of three control/status registers for the USART.
      */
     protected class ControlRegisterC extends RWRegister {
 
         protected void decode(byte val) {
 
             stopBits = readBit(USBSn) ? 2 : 1;
 
             int UCSZVal = UCSRnB_reg.readBit(UCSZn2) ? 0x4 : 0x0;
             UCSZVal |= readBit(UCSZn1) ? 0x2 : 0x0;
             UCSZVal |= readBit(UCSZn0) ? 0x1 : 0x0;
 
             //frameSize = SIZE[UCSZVal];
             // why does it look like they are using a 5 bit frame size?
             frameSize = 8;
         }
 
         public void write(byte val) {
             super.write((byte)(0x7f & val));
             decode(val);
         }
 
         public void writeBit(int bit, boolean val) {
             if (bit < 7) {
                 super.writeBit(bit, val);
             }
             decode(value);
         }
 
     }
 
     /**
      * The high byte of the Baud Rate register.
      */
     protected class UBRRnHReg extends RWRegister {
 
         public void write(byte val) {
             super.write((byte)(0x0f & val));
         }
 
         public void writeBit(int bit, boolean val) {
             if (bit < 4) {
                 super.writeBit(bit, val);
             }
         }
     }
 
     /**
      * The low byte of the Baud Rate register. The baud rate is not updated until the low bit is
      * updated.
      */
     protected class UBRRnLReg extends RWRegister {
 
         public void write(byte val) {
             super.write(val);
             updatePeriod();
         }
 
         public void writeBit(int bit, boolean val) {
             super.writeBit(bit, val);
             updatePeriod();
         }
     }
 
     /**
      * A simple implementation of the USARTDevice interface that connects to a USART on the processor.
      * It simply prints out a representation of each frame it receives.
      */
     protected class SerialPrinter implements USARTDevice {
 
         Simulator.Printer serialPrinter = simulator.getPrinter("atmega.usart.printer");
 
         char[] stream = {'h', 'e', 'l', 'l', 'o', 'w', 'o', 'r', 'l', 'd'};
 
         int count = 0;
 
         public Frame transmitFrame() {
             return new Frame((byte)stream[count++ % stream.length], false, 8);
         }
 
         public void receiveFrame(Frame frame) {
             if (serialPrinter.enabled) serialPrinter.println("Serial Printer " + frame.toString());
         }
 
         SerialPrinter() {
             SerialPrinter.PrinterTicker printerTicker = new SerialPrinter.PrinterTicker();
         }
 
         private class PrinterTicker implements Simulator.Event {
             public void fire() {
                 if (UCSRnB_reg.readBit(RXENn)) receiver.enableReceive();
             }
         }
     }
 
 }
