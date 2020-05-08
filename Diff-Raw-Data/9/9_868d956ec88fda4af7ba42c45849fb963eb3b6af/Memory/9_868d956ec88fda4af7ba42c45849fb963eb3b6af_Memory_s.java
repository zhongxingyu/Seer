 /*
  * Copyright (C) 2011-2012  Christian Roesch
  * 
  * This file is part of micro-debug.
  * 
  * micro-debug is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * micro-debug is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with micro-debug.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.github.croesch.mic1.mem;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 import java.util.Map;
 
 import com.github.croesch.error.FileFormatException;
 import com.github.croesch.i18n.Text;
 import com.github.croesch.mic1.io.Input;
 import com.github.croesch.mic1.io.Output;
 import com.github.croesch.mic1.register.Register;
 import com.github.croesch.misc.Printer;
 import com.github.croesch.misc.Settings;
 import com.github.croesch.misc.Utils;
 
 /**
  * Represents the main memory of the processor.
  * 
  * @author croesch
  * @since Date: Nov 21, 2011
  */
 public final class Memory {
 
   /** a mask to deselect byte 0 */
   private static final int MASK_BYTE_0 = 0xFFFFFF00;
 
   /** a mask to deselect byte 1 */
   private static final int MASK_BYTE_1 = 0xFFFF00FF;
 
   /** a mask to deselect byte 2 */
   private static final int MASK_BYTE_2 = 0xFF00FFFF;
 
   /** a mask to deselect byte 3 */
   private static final int MASK_BYTE_3 = 0x00FFFFFF;
 
   /** mask to select a byte from an int */
   private static final int BYTE_MASK = 0xFF;
 
   /** address which isn't an address in the memory, but is connected to memory mapped io */
   public static final int MEMORY_MAPPED_IO_ADDRESS = 0xFFFFFFFD;
 
   /** the representation of the memory */
   private final int[] memory;
 
   /** stores the initial state of the memory for reset purpose */
   private final int[] initialMemory;
 
   /** the input signal that enforces the memory to read a word */
   private boolean read = false;
 
   /** the input signal that enforces the memory to write a word */
   private boolean write = false;
 
   /** the input signal that enforces the memory to read a byte */
   private boolean fetch = false;
 
   /** the address of the word, where to read/write */
   private int wordAddress = -1;
 
   /** the word to write, or read from the memory */
   private int wordValue = -1;
 
   /** the address of the byte, where to read */
   private int byteAddress = -1;
 
   /** the byte read from the memory */
   private byte byteValue = -1;
 
   /** the map that contains the configuration with addresses in micro code and the belonging command */
   private Map<Integer, IJVMCommand> commands = null;
 
   /** the magic number that is needed at the begin of a binary ijvm-file */
   public static final int IJVM_MAGIC_NUMBER = 0x1DEADFAD;
 
   /**
    * Constructs a new memory containing the given number of words. The initial memory will contain only zeros and then
    * filled with the bytes read from the given input stream.
    * 
    * @since Date: Nov 23, 2011
    * @param maxSize the size of the memory in words (32-bit-values)
    * @param programStream the input stream
    * @throws FileFormatException if the stream doesn't provide valid data.
    */
   public Memory(final int maxSize, final InputStream programStream) throws FileFormatException {
     this.memory = new int[maxSize];
     this.initialMemory = new int[maxSize];
     initMemory(programStream);
     System.arraycopy(this.memory, 0, this.initialMemory, 0, maxSize);
   }
 
   /**
    * Resets the {@link Memory} so that it behaves as when started.
    * 
    * @since Date: Jan 27, 2012
    */
   public void reset() {
     // copy initial memory state
     System.arraycopy(this.initialMemory, 0, this.memory, 0, this.memory.length);
     // set values
     this.read = false;
     this.fetch = false;
     this.write = false;
     this.wordAddress = -1;
     this.wordValue = -1;
     this.byteAddress = -1;
     this.byteValue = -1;
   }
 
   /**
    * Initialises the memory with the data fetched from the given {@link InputStream}.
    * 
    * @since Date: Nov 26, 2011
    * @param stream the stream that provides the data to fill the memory with
    * @throws FileFormatException if
    *         <ul>
    *         <li>the stream does only contain less or equal than four bytes</li>
    *         <li>the magic number isn't correct</li>
    *         <li>the format of the data is invalid</li>
    *         <li>an {@link IOException} occurs</li>
    *         </ul>
    */
   private void initMemory(final InputStream stream) throws FileFormatException {
     Utils.checkMagicNumber(stream, IJVM_MAGIC_NUMBER);
 
     final byte[] bytes = new byte[4];
     try {
       while (stream.read(bytes) != -1) {
         final int startAddress = Utils.bytesToInt(bytes[0], bytes[1], bytes[2], bytes[3]);
 
         if (stream.read(bytes) == -1) {
           throw new FileFormatException(Text.WRONG_FORMAT_UNEXPECTED_END);
         }
         final int blockLength = Utils.bytesToInt(bytes[0], bytes[1], bytes[2], bytes[3]);
 
         readBlock(startAddress, blockLength, stream);
       }
     } catch (final IOException e) {
       throw new FileFormatException(e.getMessage(), e);
     }
   }
 
   /**
    * Reads a block of data from the given stream and stores it in the memory.
    * 
    * @since Date: Nov 26, 2011
    * @param start a byte address in the memory where to start storing the data
    * @param length the number of bytes to read
    * @param stream the {@link InputStream} to read the data from
    * @throws FileFormatException if an error occurs
    */
   private void readBlock(final int start, final int length, final InputStream stream) throws FileFormatException {
     try {
       for (int i = 0; i < length; ++i) {
         int val = stream.read();
 
         if (val == -1) {
           throw new FileFormatException(Text.WRONG_FORMAT_UNEXPECTED_END_OF_BLOCK);
         }
 
         final int addr = start + i;
         final int mask;
         final int word = this.memory[addr / 4];
         switch (addr % 4) {
           case 0:
             val <<= Byte.SIZE * 3;
             mask = MASK_BYTE_3;
             break;
           case 1:
             val <<= Byte.SIZE * 2;
             mask = MASK_BYTE_2;
             break;
           case 2:
             val <<= Byte.SIZE;
             mask = MASK_BYTE_1;
             break;
           default:
             mask = MASK_BYTE_0;
             break;
         }
         this.memory[addr / 4] = (word & mask) | val;
       }
     } catch (final IOException e) {
       throw new FileFormatException(e.getMessage(), e);
     }
   }
 
   /**
    * Sets the input signal <code>read</code>, that enforces the main memory to read a word from the memory.
    * 
    * @since Date: Nov 21, 2011
    * @param rd <code>true</code>, if the memory should read a word.
    * @see #setWordAddress(int)
    */
   public void setRead(final boolean rd) {
     this.read = rd;
   }
 
   /**
    * Sets the input signal <code>write</code>, that enforces the main memory to write a word to the memory.
    * 
    * @since Date: Nov 21, 2011
    * @param wr <code>true</code>, if the memory should write a word.
    * @see #setWordAddress(int)
    * @see #setWordValue(int)
    */
   public void setWrite(final boolean wr) {
     this.write = wr;
   }
 
   /**
    * Sets the input signal <code>fetch</code>, that enforces the main memory to read a byte from the memory.
    * 
    * @since Date: Nov 21, 2011
    * @param ft <code>true</code>, if the memory should read a byte.
    * @see #setByteAddress(int)
    */
   public void setFetch(final boolean ft) {
     this.fetch = ft;
   }
 
   /**
    * Sets the address of the word, where to read from or write to in the memory.
    * 
    * @since Date: Nov 21, 2011
    * @param addr 32-bit-value that defines the address where to read from or write to a word.
    * @see #setRead(boolean)
    * @see #setWrite(boolean)
    */
   public void setWordAddress(final int addr) {
     this.wordAddress = addr;
   }
 
   /**
    * Sets the value of the word to write to the memory.
    * 
    * @since Date: Nov 21, 2011
    * @param value the word value to write to the memory.
    * @see #setWrite(boolean)
    * @see #setWordAddress(int)
    */
   public void setWordValue(final int value) {
     this.wordValue = value;
   }
 
   /**
    * Sets the address of the byte, where to read from in the memory.
    * 
    * @since Date: Nov 21, 2011
    * @param addr 32-bit-value that defines the address where to read a byte from.
    * @see #setFetch(boolean)
    */
   public void setByteAddress(final int addr) {
     this.byteAddress = addr;
   }
 
   /**
    * If at least the signal <code>read</code> or <code>fetch</code> has been set in the memory. The values read will be
    * stored in the given registers.<br />
    * <b>Note:</b> You have to call {@link #poke()} before invoking this method.
    * 
    * @since Date: Nov 21, 2011
    * @param wordRegister if the signal <code>read</code> has been set, will be filled with the value read from the
    *        memory
    * @param byteRegister if the signal <code>fetch</code> has been set, will be filled with the value read from the
    *        memory
    * @see #poke()
    */
   public void fillRegisters(final Register wordRegister, final Register byteRegister) {
     if (this.read) {
       wordRegister.setValue(this.wordValue);
     }
     if (this.fetch) {
       byteRegister.setValue(this.byteValue & BYTE_MASK);
     }
   }
 
   /**
    * Tells the memory to do its work. Based on the signals <code>read</code>, <code>write</code> and <code>fetch</code>
    * it will do some work. It will store the value to write into the memory and the values read from the memory into
    * variables.
    * 
    * @since Date: Nov 21, 2011
    * @see #fillRegisters(Register, Register)
    */
   public void poke() {
     if (this.write) {
       write();
     }
     if (this.read) {
       read();
     }
     if (this.fetch) {
       fetch();
     }
   }
 
   /**
    * Performs the fetch operation of a byte on the memory.
    * 
    * @since Date: Nov 23, 2011
    */
   private void fetch() {
     this.byteValue = (byte) getByte(this.byteAddress);
   }
 
   /**
    * Returns a single byte, unsigned, read from the address in the memory.
    * 
    * @since Date: Jan 22, 2012
    * @param addr the byte address of the byte to read from the memory
    * @return the byte from the memory at the given address.
    */
   public int getByte(final int addr) {
     int word = this.memory[addr / 4];
     switch (addr % 4) {
       case 0:
         word >>= Byte.SIZE * 3;
         break;
       case 1:
         word >>= Byte.SIZE * 2;
         break;
       case 2:
         word >>= Byte.SIZE;
         break;
       default:
         // nothing to do, because the value we want is already at word[7:0]
         break;
     }
     return word & BYTE_MASK;
   }
 
   /**
    * Performs the read operation of a word on the memory.
    * 
    * @since Date: Nov 23, 2011
    */
   private void read() {
     if (this.wordAddress == MEMORY_MAPPED_IO_ADDRESS) {
       this.wordValue = Input.read() & BYTE_MASK;
     } else {
       this.wordValue = this.memory[this.wordAddress];
     }
   }
 
   /**
    * Performs the write operation of a word on the memory.
    * 
    * @since Date: Nov 23, 2011
    */
   private void write() {
     if (this.wordAddress == MEMORY_MAPPED_IO_ADDRESS) {
       Output.print((byte) this.wordValue);
     } else {
       this.memory[this.wordAddress] = this.wordValue;
     }
   }
 
   /**
    * Returns the word value at the given address.
    * 
    * @since Date: Jan 21, 2012
    * @param addr the address, from where to fetch the word
    * @return the word value read from the memory
    */
   public int getWord(final int addr) {
     if (isAddressValid(addr)) {
       return this.memory[addr];
     }
     return -1;
   }
 
   /**
    * Sets the word value at the given address to the given value.
    * 
    * @since Date: Jan 21, 2012
    * @param addr the address, where to write the word to
    * @param value the word to write to the memory
    */
   public void setWord(final int addr, final int value) {
     if (isAddressValid(addr)) {
       this.memory[addr] = value;
     }
   }
 
   /**
    * Returns whether the given address is a valid address in the memory.
    * 
    * @since Date: Jan 21, 2012
    * @param addr the address to check
    * @return <code>true</code> if the address is valid<br>
    *         <code>false</code> otherwise
    */
   private boolean isAddressValid(final int addr) {
     final boolean valid = addr >= 0 && addr < this.memory.length;
     if (!valid) {
       Printer.printErrorln(Text.INVALID_MEM_ADDR.text(Utils.toHexString(addr)));
     }
     return valid;
   }
 
   /**
    * Prints the assembler code stored in the memory to the user.
    * 
    * @since Date: Jan 22, 2012
    */
   public void printCode() {
    final int start = getFirstPossibleCodeAddress();
    final int end = refineEndOfCode(getLastPossibleCodeAddress());
     printCode(start, end);
   }
 
   /**
    * Prints the given number lines of assembler code stored in the memory to the user around the given line.
    * 
    * @since Date: Jan 26, 2012
    * @param line the line around to print the code
    * @param scope the number of lines to print before and after the given line
    */
   public void printCodeAroundLine(final int line, final int scope) {
     printCode(line - scope, line + scope);
   }
 
   /**
    * Prints the assembler code stored in the memory to the user between the given lines.
    * 
    * @since Date: Jan 26, 2012
    * @param pos1 the first line to print
    * @param pos2 the last line to print
    */
   public void printCode(final int pos1, final int pos2) {
     // correct arguments
    final int start = Math.max(getFirstPossibleCodeAddress(), Math.min(pos1, pos2));
    final int end = Math.min(refineEndOfCode(getLastPossibleCodeAddress()), Math.max(pos1, pos2));
 
     for (int i = start; i <= end; ++i) {
       i += printCodeLine(i);
     }
   }
 
   /**
    * Returns the address where the last possible code is stored.
    * 
    * @since Date: Jan 26, 2012
    * @return the address of the last byte in the code section of the memory.
    */
   private int getLastPossibleCodeAddress() {
     return Utils.getNextHigherValue(getFirstPossibleCodeAddress(), Settings.MIC1_REGISTER_CPP_DEFVAL.getValue(),
                                     Settings.MIC1_REGISTER_SP_DEFVAL.getValue(),
                                     Settings.MIC1_MEMORY_MAXSIZE.getValue(),
                                     Settings.MIC1_REGISTER_LV_DEFVAL.getValue()) - 1;
   }
 
   /**
    * Returns the address where the first possible code is stored.
    * 
    * @since Date: Jan 26, 2012
    * @return the address of the first byte in the code section of the memory.
    */
   private int getFirstPossibleCodeAddress() {
     return Settings.MIC1_REGISTER_PC_DEFVAL.getValue() + 1;
   }
 
   /**
    * Returns the address of the last assembler instruction that is only followed by <code>NOP</code>s
    * 
    * @since Date: Jan 26, 2012
    * @param end the end of the code area in the memory
    * @return the address of last assembler instruction
    */
   private int refineEndOfCode(final int end) {
     int refEnd = end;
     while (getByte(refEnd) == 0) {
       --refEnd;
     }
     return refEnd;
   }
 
   /**
    * Reads the value of the memory at the given address, builds the representing {@link String} and prints it to the
    * user.
    * 
    * @since Date: Jan 22, 2012
    * @param addr the absolute address of the code instruction to print
    * @return the number of bytes read as arguments to the command byte
    */
   private int printCodeLine(final int addr) {
     final StringBuilder formattedArgs = new StringBuilder();
 
     final int cmdCode = getByte(addr);
     final IJVMCommand cmd = lookupCommand(cmdCode);
 
     final String name = buildNameForCommand(cmd);
     final int bytesRead = readArgumentsIfAny(addr, cmd, formattedArgs);
 
     final String formattedAddr = formatIntToHex(addr, Settings.MIC1_CODE_MACRO_HEX_WIDTH.getValue());
     final String formattedCmdCode = formatIntToHex(cmdCode, Settings.MIC1_CODE_MICRO_HEX_WIDTH.getValue());
 
     Printer.println(Text.CODE_LINE.text(formattedAddr, formattedCmdCode, name, formattedArgs.toString()));
 
     return bytesRead;
   }
 
   /**
    * If the command is valid, this will read all argument bytes and build a formatted string with them.
    * 
    * @since Date: Jan 23, 2012
    * @param addr the address of the command byte
    * @param cmd the fetched {@link IJVMCommand} the command byte stands for
    * @param sb the {@link StringBuilder} that stores the formatted arguments
    * @return the number of bytes read as arguments
    */
   private int readArgumentsIfAny(final int addr, final IJVMCommand cmd, final StringBuilder sb) {
     if (cmd != null) {
       return createArgumentList(cmd.getArgs(), addr, sb);
     }
     return 0;
   }
 
   /**
    * Returns the name for the given command to display.
    * 
    * @since Date: Jan 23, 2012
    * @param cmd the {@link IJVMCommand} to fetch the name from, may be <code>null</code>
    * @return the name of the {@link IJVMCommand},<br>
    *         or an text to indicate, that the command is unknown, if the given command is <code>null</code>
    * @see IJVMCommand#getName()
    */
   private String buildNameForCommand(final IJVMCommand cmd) {
     if (cmd == null) {
       return Text.UNKNOWN_IJVM_INSTRUCTION.text();
     }
     return cmd.getName();
   }
 
   /**
    * Builds the formatted {@link String} based on the arguments to read.
    * 
    * @since Date: Jan 23, 2012
    * @param args the argument list that define the number of bytes to read, may not be <code>null</code>
    * @param addr the address of the command byte
    * @param sb the {@link StringBuilder} that stores the formatted arguments
    * @return the number of bytes read as arguments
    */
   private int createArgumentList(final List<IJVMCommandArgument> args, final int addr, final StringBuilder sb) {
     int bytesRead = 0;
     for (final IJVMCommandArgument arg : args) {
       int value = 0;
       for (int i = 1; i <= arg.getNumberOfBytes(); ++i) {
         value |= getByte(addr + i) << (Byte.SIZE * (arg.getNumberOfBytes() - i));
         ++bytesRead;
       }
       sb.append(" ").append(arg.getRepresentationOfArgument(value, this));
     }
     return bytesRead;
   }
 
   /**
    * Formats the given number to a hexadecimal number and returns an right aligned string with the given width.
    * 
    * @since Date: Jan 22, 2012
    * @param number the number to format
    * @param width the width the formatted string should at least have
    * @return the formatted string containing the hexadecimal value of the given number and at least <i>width</i>
    *         characters.
    */
   private String formatIntToHex(final int number, final int width) {
     return String.format("%" + width + "s", Utils.toHexString(number));
   }
 
   /**
    * Returns the {@link IJVMCommand} that belongs to the given address.
    * 
    * @since Date: Jan 22, 2012
    * @param addr the address where the command is placed in the micro code
    * @return the {@link IJVMCommand} describing the command at the given address<br>
    *         or <code>null</code> if no command is configured for this address
    */
   private IJVMCommand lookupCommand(final int addr) {
     if (this.commands == null) {
       // read configuration file the first time
       final InputStream in = getClass().getClassLoader().getResourceAsStream("ijvm.conf");
       this.commands = new IJVMConfigReader().readConfig(in);
     }
     return this.commands.get(Integer.valueOf(addr));
   }
 }
