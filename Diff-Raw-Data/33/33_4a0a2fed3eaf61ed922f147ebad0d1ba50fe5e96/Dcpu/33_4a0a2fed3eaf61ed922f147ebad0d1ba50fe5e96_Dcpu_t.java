 
 package dcpu;
 
 import java.util.Arrays;
 
 /**
  * @author miah
  */
 public class Dcpu {
 	/**
 	 * Masks to get clear results from an INT.
 	 */
 	private static final int    OP_MASK = 0xF;
 	private static final int     A_MASK = 0x3F;
 	private static final int     B_MASK = 0x3F;
 	private static final int SHORT_MASK = 0xFFFF;
 	
 	/**
 	 * Shift width to get clear results from an INT.
 	 */
 	private static final int     A_SHIFT = 4;
 	private static final int     B_SHIFT = 10;
 	private static final int SHORT_SHIFT = 16;
 	
 	/**
 	 * Size variables to define the environment.
 	 */
 	private static final int MEMORY_SIZE   = 65536;
 	private static final int NUM_ITERALS   = 32;
 	private static final int NUM_REGISTERS = 8;
 	
 	/**
 	 * Indices for the variable int[][] ram
 	 * 
 	 * @see Dcpu.ram
 	 */
 	private static final int PC  = 0; // program counter
 	private static final int SP  = 1; // stack pointer
 	private static final int OV  = 2; // overflow
 	private static final int REG = 3; // register
 	private static final int LIT = 4; // literals
 	private static final int MEM = 5; // memory
 	
 	/**
 	 * Store environment.
 	 */
 	private int[][] ram = new int[6][];
 	
 	/**
 	 * The current ram position to read from or write to.
 	 * 
 	 * @see Class constants at the top of the class
 	 */
 	private int rp  = MEM; // ram position
 	
 	public Dcpu (Program p) {
 		ram[PC]  = new int[1];
 		ram[SP]  = new int[1];
 		ram[OV]  = new int[1];
 		ram[REG] = new int[NUM_REGISTERS];
 		ram[LIT] = p.lit;
 		ram[MEM] = p.memory;
 	}
 	public Dcpu (int[] program) {
 		ram[PC]  = new int[1];
 		ram[SP]  = new int[1];
 		ram[OV]  = new int[1];
 		ram[REG] = new int[NUM_REGISTERS];
 		ram[LIT] = new int[NUM_ITERALS];
 		ram[MEM] = new int[MEMORY_SIZE];
 		
 		for (int i = 0; i < NUM_ITERALS; i += 1) {
 			ram[LIT][i] = i;
 		}
 		
 		for (int i = 0; i < program.length; i += 1) {
 			ram[MEM][i] = program[i];
 		}
 	}
 	
 	private int parseVar (int code) {
 		if (code < 0x08) {
 			rp = REG;
 			return code;
 		} else if (code < 0x10) {
 			rp = MEM;
 			return ram[REG][code % NUM_REGISTERS];
 		} else if (code < 0x18) {
 			return (ram[MEM][ram[PC][0]++] + ram[REG][code % NUM_REGISTERS]) & SHORT_MASK;
 		} else if (code == 0x18) {
 			rp = MEM;
 			return ram[SP][0]++;
 		} else if (code == 0x19) {
 			rp = MEM;
 			return ram[SP][0];
 		} else if (code == 0x1A) {
 			rp = MEM;
 			return --ram[SP][0];
 		} else if (code == 0x1B) {
 			rp = SP;
 			return 0;
 		} else if (code == 0x1C) {
 			rp = PC;
 			return 0;
 		} else if (code == 0x1D) {
 			rp = OV;
 			return 0;
 		} else if (code == 0x1E) {
 			rp = MEM;
 			return ram[PC][0]++;
 		} else if (code == 0x1F) {
 			rp = PC;
 			ram[PC][0]++;
 			return 0;
 		}
 		
 		rp = LIT;
 		return (code - 0x20) % NUM_ITERALS;
 	}
 	
 	public boolean step () {
 		if (ram[MEM][ram[PC][0]] == 0x0) { return false; }
 		
 		int code = ram[MEM][ram[PC][0]];
 		int op =  code &  OP_MASK;
 		int a  = (code >> A_SHIFT) & A_MASK;
 		int b  = (code >> B_SHIFT) & B_MASK;
 		ram[PC][0] += 1;
 		
 		if (op == 0) {
 			switch (a) {
 				case 0x01:
 					a = parseVar(b);
 					ram[MEM][--ram[SP][0]] = ram[PC][0];
 					ram[PC][0] = a;
 					break;
 				default:
 					System.err.println("0x0000: Illegal operation! ("+ Integer.toHexString(a) +")");
 					System.exit(1);
 			}
 		} else {
 			int[] w, r;
 			
 			a = parseVar(a);
 			w = ram[rp];
 			
 			b = parseVar(b);
 			r = ram[rp];
 
 			switch (op) {
 				case 0x1: w[a]  = r[b];                                       break;
 				case 0x2: w[a] += r[b];                                       break;
 				case 0x3: w[a] -= r[b];                                       break;
 				case 0x4: w[a] *= r[b];                                       break;
 				case 0x5: if (r[b] == 0) { w[a] = 0; } else { w[a] /= r[b]; } break;
 				case 0x6: if (r[b] == 0) { w[a] = 0; } else { w[a] &= r[b]; } break;
 				case 0x7: w[a] <<= r[b];                                      break;
 				case 0x8: w[a] >>= r[b]; w[a] &= SHORT_MASK;                  break;
 				case 0x9: w[a]  &= r[b];                                      break;
 				case 0xA: w[a]  |= r[b];                                      break;
 				case 0xB: w[a]  ^= r[b];                                      break;
 				case 0xC: if (w[a] != r[b])        { ++ram[PC][0]; }          break;
 				case 0xD: if (w[a] == r[b])        { ++ram[PC][0]; }          break;
 				case 0xE: if (!(w[a] >  r[b]))     { ++ram[PC][0]; }          break;
 				case 0xF: if ((w[a] &  r[b]) == 0) { ++ram[PC][0]; }          break;
 				default:
 					System.err.println("0xFFFF: Illegal operation! ("+ Integer.toHexString(op) +")");
 					System.exit(1);
 			}
 			
 			if (op > 0x1 && op < 0x9 && op != 0x6) {
 				ram[OV][0] = (w[a] >> SHORT_SHIFT) & SHORT_MASK;
 			}
 		}
 		
 		return true;
 	}
 	
 	public String getHeader () {
		return "|_OP_|_A__|_B__|||__PC__|__SP__|__OV__|__A___|__B___|__C___|__X___|__Y___|__Z___|__I___|__J___|";
 	}
 	
 	@Override
 	public String toString () {
		return                                                        "| "+
		       hex((ram[MEM][ram[PC][0]] & OP_MASK), 2)             +" | "+
		       hex((ram[MEM][ram[PC][0]] >> A_SHIFT) & A_MASK, 2)   +" | "+
		       hex((ram[MEM][ram[PC][0]] >> B_SHIFT) & B_MASK, 2)  +" ||| "+
		       hex(ram[PC][0])                                      +" | "+
		       hex(ram[SP][0])                                      +" | "+
		       hex(ram[OV][0])                                      +" | "+
		       hex(ram[REG][0])                                     +" | "+
		       hex(ram[REG][1])                                     +" | "+
		       hex(ram[REG][2])                                     +" | "+
		       hex(ram[REG][3])                                     +" | "+
		       hex(ram[REG][4])                                     +" | "+
		       hex(ram[REG][5])                                     +" | "+
		       hex(ram[REG][6])                                     +" | "+
		       hex(ram[REG][7])                                     +" | ";
 	}
 	
 	public static void main(String[] args) {
 		DcpuCompiler c = new DcpuCompiler();
 		Program p = c.compileString(DcpuCompiler.readFile("src/source/test.x10"));
 		
 		Dcpu cpu = new Dcpu(p);
 		
 		System.out.println(cpu.getHeader());
 		while (cpu.step()) {
 			System.out.println(cpu.toString());
 		}
 	}
 	
 	static int word (int op, int a, int b) {
 		return op | (a << 4) | (b << 10);
 	}
 	
 	static String hex (int i) {
 		return hex(i, 4);
 	}
 	static String hex (int i, int l) {
 		String h = Integer.toHexString(i).toUpperCase();
 		char[] c = new char[(l - h.length()) % l];
 		Arrays.fill(c, '0');
 		return String.valueOf(c) + h;
 	}
 }
