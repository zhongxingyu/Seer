 package gameBoy.cpu;
 
 import gameBoy.interfaces.IRegister;
 
 public class GameBoyRegisters implements IRegister {
 	private byte registers[];
 	
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
 	
 	public void setFlagTo(Flag flag, boolean state) {
 		short F = this.getRegister(Register.F);
 		byte shift = 0;
 		switch (flag) {
 			case Z: // bit 7
 				shift = 7;
 				break;
 			case N: // bit 6
 				shift = 6;
 				break;
 			case H: // bit 5
 				shift = 5;
 				break;
 			case C: // bit 4
 				shift = 4;
 				break;
 		}
 		
 		if( state ) {
 			F |= 1 << shift;
 		} else {
 			F &= ~( 1 << shift );
 		}
 		
 		this.set8BitRegister(Register.F, F);
 	}
 	
 	public byte getFlag(Flag flag) {
 		byte data = 0;
 		byte F = (byte) this.getRegister(Register.F);
 		switch (flag) {
 			case Z: // bit 7
 				data = (byte) ((F & 0x80) >> 7);
 				break;
 			case N: // bit 6
 				data = (byte) ((F & 0x40) >> 6);
 				break;
 			case H: // bit 5
 				data = (byte) ((F & 0x20) >> 5);
 				break;
 			
 			case C: // bit 4
 				data = (byte) ((F & 0x10) >> 4);
 				break;
 		}
 		return data;
 	}
 	
 	public void clearFlags() {
 		this.set8BitRegister( Register.F, (byte) 0 );
 	}
 	
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
 	
 	private short combine8BitRegisters(Register x, Register y) {
 		
		int X = this.registers[this.getRegisterIndex(x)];
		int Y = this.registers[this.getRegisterIndex(y)];
		return (short) ( ( X << 8 ) | ( Y & 0xFF ) );	// silly things because of Java casting
 	}
 }
