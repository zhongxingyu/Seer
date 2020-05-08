 package gameBoy.cpu;
 
 import gameBoy.interfaces.IRegister;
 
 public class GameBoyRegisters implements IRegister {
 	//Array to hold registers
 	private byte registers[];
 	//Enum used to refer to registers
 	public enum Register {
 		A, B, C, D, E, F, H, L, AF, BC, DE, HL, SP, PC
 	}
 	
 	public GameBoyRegisters() {
 		registers = new byte[12]; 
 	}
 	
 	public void setRegister(Register reg, short data) {
 		switch (reg) {
 		case AF:
 			this.set16BitRegisterFromTwo8Bit(Register.A, Register.F, data);
 			break;
 		case BC:
			this.set16BitRegisterFromTwo8Bit(Register.B, Register.C, data);
 			break;
 		case DE:
			this.set16BitRegisterFromTwo8Bit(Register.D, Register.E, data);
 			break;
 		case HL:
			this.set16BitRegisterFromTwo8Bit(Register.H, Register.L, data);
 			break;
 		case PC:
 			this.set16BitRegister(reg, data);
 			break;
 		case SP:
 			this.set16BitRegister(reg, data);
 		default:
 			this.set8BitRegister(reg, data);
 			break;
 		}
 	}
 	
 	private void set8BitRegister(Register reg, short data) {
 		registers[this.getRegisterIndex(reg)] = (byte)data;
 	}
 	
 	private void set16BitRegisterFromTwo8Bit(Register x, Register y, short data) {
 		byte xData, yData;
		xData = (byte) ((data & 0xFF00) >> 8);
		yData = (byte) (data & 0x00FF);
 		registers[this.getRegisterIndex(x)] = xData;
 		registers[this.getRegisterIndex(y)] = yData;
 	}
 	
 	private void set16BitRegister(Register reg, short data) {
 		byte first, second;
		first = (byte) ((data & 0xFF00) >> 8);
		second = (byte) (data & 0x00FF);
 		int index = this.getRegisterIndex(reg);
 		registers[index] = first;
 		registers[index + 1] = second;
 	}
 	
 	/**
 	 * Returns the data in the specified register
 	 * @param reg desired register
 	 * @return data in register
 	 */
 	public short getRegister(Register reg) {
 		short register = 0;
 
 		switch (reg) {
 		case AF:
			register = this.combine8BitRegisters(Register.A, Register.F);
 			break;
 		case BC:
			register = this.combine8BitRegisters(Register.B, Register.C);
 			break;
 		case DE:
 			register = this.combine8BitRegisters(Register.D, Register.E);
 			break;
 		case HL:
 			register = this.combine8BitRegisters(Register.H, Register.L);
 			break;
		case PC:
		case SP:
			// since PC and SP are 16 bit registers, we need to concatenate 
			// the data at index and index + 1
			register = (short) ( this.registers[this.getRegisterIndex( reg )] << 8 |
							this.registers[this.getRegisterIndex( reg ) + 1] );
			break;
 		default:
 			register = registers[this.getRegisterIndex(reg)];
 			break;
 		}
 		
 		return register;
 	}
 	
 	/**
 	 * Gets the index of the register in registers
 	 * @param reg Game boy register
 	 * @return index of register in registers
 	 */
 	private int getRegisterIndex(Register reg) {
 		int index = 0;
 		
 		switch (reg) {
 		case A:
 			index = 0;
 			break;
 		case B:
 			index = 1;
 			break;
 		case C:
 			index = 2;
 			break;
 		case D:
 			index = 3;
 			break;
 		case E:
 			index = 4;
 			break;
 		case F:
 			index = 5;
 			break;
 		case H:
 			index = 6;
 			break;
 		case L:
 			index = 7;
 			break;
 		case PC:
 			index = 8;
 			break;
 		case SP:
 			index = 10;
 			break;
 		default:
 			// All other cases are not necessary
 			break;
 		
 		}
 		return index;
 	}
 	
 	/**
 	 * Combines register 1 and 2 by shifting register 1 to the left 
 	 * and or'ing register 2
 	 * @param x register 1
 	 * @param y register 2
 	 * @return register 1 and register 2 combined
 	 */
 	private short combine8BitRegisters(Register x, Register y) {
 		
 		short X = this.registers[this.getRegisterIndex(x)];
 		short Y = this.registers[this.getRegisterIndex(y)];
 		return (short) (X << 8 | Y);
 	}
 }
