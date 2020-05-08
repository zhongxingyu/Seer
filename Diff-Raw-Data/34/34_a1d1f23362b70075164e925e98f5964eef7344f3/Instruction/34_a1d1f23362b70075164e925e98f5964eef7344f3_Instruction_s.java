 package mips;
 
 import java.util.NoSuchElementException;
 import java.util.StringTokenizer;
 
 public class Instruction {
 	public static final short FUNCT_ADD = 0x20;
 	public static final short FUNCT_SUB = 0x22;
 	public static final short FUNCT_AND = 0x24;
 	public static final short FUNCT_OR = 0x25;
 	public static final short FUNCT_NOR = 0x27;
 	public static final short FUNCT_SLT = 0x2a;
 
 	public static final short OPCODE_LW = 0x23;
 	public static final short OPCODE_SW = 0x2b;
 
 	public static final short OPCODE_BEQ = 4;
 
 	private String repr;
 	private short opcode = 0;
 	private short funct = 0;
 	private short rd = 0;
 	private short rs = 0;
 	private short rt = 0;
 	private short addr = 0;
 	private boolean r_type = false;
 	private boolean is_exit;
 
 
 	public Instruction(String line) throws Exception {
 		repr = line;
 
 		line = line.replaceAll(",", "");
 		StringTokenizer tokens = new StringTokenizer(line, " ");
 		String op = "", t1 = "", t2 = "", t3 = "";
 
 		op = tokens.nextToken();
 
 		try {
 			t1 = tokens.nextToken();
 			t2 = tokens.nextToken();
 			t3 = tokens.nextToken();
 		} catch(NoSuchElementException e) {}
 
 		if(op.equalsIgnoreCase("add")) {
 			funct = FUNCT_ADD;
 			r_type = true;
 		} else if(op.equalsIgnoreCase("sub")) {
 			funct = FUNCT_SUB;
 			r_type = true;
 		} else if(op.equalsIgnoreCase("and")) {
 			funct = FUNCT_AND;
 			r_type = true;
 		} else if(op.equalsIgnoreCase("or")) {
 			funct = FUNCT_OR;
 			r_type = true;
 		} else if(op.equalsIgnoreCase("nor")) {
 			funct = FUNCT_NOR;
 			r_type = true;
 		} else if(op.equalsIgnoreCase("slt")) {
 			funct = FUNCT_SLT;
 			r_type = true;
 		}
 
 		else if(op.equalsIgnoreCase("lw")) {
 			opcode = OPCODE_LW;
 		} else if(op.equalsIgnoreCase("sw")) {
 			opcode = OPCODE_SW;
 		}
 
 		else if(op.equalsIgnoreCase("beq")) {
 			opcode = OPCODE_BEQ;
 		}
		
 		else if(op.equalsIgnoreCase("exit")) {
 			is_exit = true;
 		}
 
 
 		// Parse additional parameters
		if((opcode == OPCODE_LW) || (opcode == OPCODE_SW)) {
			rd = parseReg(t1);
			addr = parseAddr(t2);

 		} else if(r_type) {
 			rd = parseReg(t1);
 			rs = parseReg(t2);
 			rt = parseReg(t3);
 
 		} else if(opcode == OPCODE_BEQ) {
 			rs = parseReg(t1);
 			rt = parseReg(t2);
 			addr = parseAddr(t3);
 		}
 
 
 
 
 
 
 	}
 
 
 	private short parseAddr(String address) {
 		if(address.contains("x")) {
 			return Short.parseShort(
 					address.substring(address.indexOf('x')+1), 16);
 		}
 		return Short.parseShort(address);
 	}
 
 
 	private short parseReg(String register) throws Exception {
 		if(register.charAt(0) == '$') {
 			if(register.equalsIgnoreCase("$zero")) {
 				return 0;
 			} else if(register.equalsIgnoreCase("$gp")) {
 				return 28;
 			} else if(register.equalsIgnoreCase("$sp")) {
 				return 29;
 			} else if(register.equalsIgnoreCase("$fp")) {
 				return 30;
 			} else if(register.equalsIgnoreCase("$ra")) {
 				return 31;
 			}
 
 			char prefix = register.charAt(1);
 			short number = Short.parseShort(register.substring(2));
 			switch(prefix) {
 			case 'v':
 				number += 2;
 				break;
 			case 'a':
 				number += 4;
 				break;
 			case 't':
 				number += 8;
 				if(number >= 16) {
 					number += 8;
 				}
 				break;
 			case 's':
 				number += 16;
 				break;
 
 			default:
 				throw new Exception("Invalid register " + register);
 			}
			assert(register.equals(RegisterFile.name(number)));
 			return number;
 		}
 
 		return parseAddr(register);
 	}
 
 
 	public String getRepr() {
 		return repr;
 	}
 
 
 	public short getOpcode() {
 		return opcode;
 	}
 
 
 	public short getFunct() {
 		return funct;
 	}
 
 
 	public short getRd() {
 		return rd;
 	}
 
 
 	public short getRs() {
 		return rs;
 	}
 
 
 	public short getRt() {
 		return rt;
 	}
 
 
 	public short getAddr() {
 		return addr;
 	}
 
 
 	public boolean is_r_type() {
 		return r_type;
 	}
 
 	@Override
 	public String toString() {
 		return String.format("%s", repr);
 	}
 
 
 	public boolean isExit() {
 		return is_exit;
 	}
 }
