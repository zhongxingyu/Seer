 package vmlang.vm;
 
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.InputStream;
 
 import vmlang.common.Opcodes;
 
 public class VM {
 	
 	// registers
 	private int A, B, SP, FP;
 	private int counter;
 	
 	// memory
 	private byte[] program;
 	private byte[] memory;
 	
 	// io
 	
 	private InputStream in;
 	private PrintStream out;
 	
 	private int stackStart;
 	boolean debug;
 	
 	private static final Opcodes[] opcodes = Opcodes.values(); // inefficient? 
 	
 	public VM(byte[] prog, int heapSize, int stackSize, boolean d) throws InitError {
 		this(prog, heapSize, stackSize, System.in, System.out, d);
 	}
 	
 	public VM(byte[] prog, int heapSize, int stackSize, InputStream is, PrintStream os, boolean d)
 	 																																				throws InitError {
 		if(heapSize % 8 != 0)
 			throw new InitError("Heap size must be divisible by 8 (for malloc)");
 		program = prog;
 		memory = new byte[heapSize + stackSize];
 		stackStart = heapSize;
 		in = is;
 		out = os;
 		debug = d;
 	}
 	
	public void run() throws VMError {
 		int nextAddr;
 		while(true) {
 			Opcodes opcode;
 			try {
 				opcode = opcodes[progReadByte()];
 			} catch(ArrayIndexOutOfBoundsException e) {
 				throw getMalfProgEx();
 			}
 			if(debug)
 				System.out.print(opcode);
 			switch(opcode) {
 
 				// FLOW CONTROL
 
 				// basic
 				case STOP:
					return;
 				case GOTO:
 					counter = progReadInt();
 					break;
 				case GOTO_A:
 					counter = A;
 					break;
 				case GOTO_SP:
 					counter = memReadInt(SP);
 					break;
 				case GOTO_IF_NOT_A:
 					nextAddr = progReadInt();
 					if(A == 0)
 						counter = nextAddr;
 					break;
 
 				// LOAD & STORE (Von-Neumann bottleneck)
 
 				// constants
 				
 				case I_CONST_A:
 					A = progReadInt();
 					break;
 				case I_CONST_B:
 					B = progReadInt();
 					break;
 				case B_CONST_A:
 					A = progReadByte();
 					break;
 				case B_CONST_B:
 					B = progReadByte();
 					break;
 				
 				// load
 				
 				case I_LOAD_A_B:
 				  B = memReadInt(A);
 				  break;
 				case I_LOAD_A_SP:
 				  SP = memReadInt(A);
 				  break;
 				case I_LOAD_A_FP:
 				  FP = memReadInt(A);
 				  break;
 				case I_LOAD_A_A:
 					A = memReadInt(A);
 					break;
 				case I_LOAD_B_A:
 				  A = memReadInt(B);
 				  break;
 				case I_LOAD_B_SP:
 				  SP = memReadInt(B);
 				  break;
 				case I_LOAD_B_FP:
 				  FP = memReadInt(B);
 				  break;
 				case I_LOAD_SP_A:
 				  A = memReadInt(SP);
 				  break;
 				case I_LOAD_SP_B:
 				  B = memReadInt(SP);
 				  break;
 				case I_LOAD_SP_FP:
 				  FP = memReadInt(SP);
 				  break;
 				case I_LOAD_FP_A:
 				  A = memReadInt(FP);
 				  break;
 				case I_LOAD_FP_B:
 				  B = memReadInt(FP);
 				  break;
 				case I_LOAD_FP_SP:
 				  SP = memReadInt(FP);
 				  break;
 				case B_LOAD_A_B:
 				  B = memReadByte(A);
 				  break;
 				case B_LOAD_A_SP:
 				  SP = memReadByte(A);
 				  break;
 				case B_LOAD_A_FP:
 				  FP = memReadByte(A);
 				  break;
 				case B_LOAD_B_A:
 				  A = memReadByte(B);
 				  break;
 				case B_LOAD_B_SP:
 				  SP = memReadByte(B);
 				  break;
 				case B_LOAD_B_FP:
 				  FP = memReadByte(B);
 				  break;
 				case B_LOAD_SP_A:
 				  A = memReadByte(SP);
 				  break;
 				case B_LOAD_SP_B:
 				  B = memReadByte(SP);
 				  break;
 				case B_LOAD_SP_FP:
 				  FP = memReadByte(SP);
 				  break;
 				case B_LOAD_FP_A:
 				  A = memReadByte(FP);
 				  break;
 				case B_LOAD_FP_B:
 				  B = memReadByte(FP);
 				  break;
 				case B_LOAD_FP_SP:
 				  SP = memReadByte(FP);
 				  break;
 				
 				
 				// store
 				
 				case I_STORE_A_B:
 				  memWriteInt(B,A);
 				  break;
 				case I_STORE_A_SP:
 				  memWriteInt(SP,A);
 				  break;
 				case I_STORE_A_FP:
 				  memWriteInt(FP,A);
 				  break;
 				case I_STORE_B_A:
 				  memWriteInt(A,B);
 				  break;
 				case I_STORE_B_SP:
 				  memWriteInt(SP,B);
 				  break;
 				case I_STORE_B_FP:
 				  memWriteInt(FP,B);
 				  break;
 				case I_STORE_SP_A:
 				  memWriteInt(A,SP);
 				  break;
 				case I_STORE_SP_B:
 				  memWriteInt(B,SP);
 				  break;
 				case I_STORE_SP_FP:
 				  memWriteInt(FP,SP);
 				  break;
 				case I_STORE_FP_A:
 				  memWriteInt(A,FP);
 				  break;
 				case I_STORE_FP_B:
 				  memWriteInt(B,FP);
 				  break;
 				case I_STORE_FP_SP:
 				  memWriteInt(SP,FP);
 				  break;
 				case B_STORE_A_B:
 				  memWriteByte(B,(byte)A);
 				  break;
 				case B_STORE_A_SP:
 				  memWriteByte(SP,(byte)A);
 				  break;
 				case B_STORE_A_FP:
 				  memWriteByte(FP,(byte)A);
 				  break;
 				case B_STORE_B_A:
 				  memWriteByte(A,(byte)B);
 				  break;
 				case B_STORE_B_SP:
 				  memWriteByte(SP,(byte)B);
 				  break;
 				case B_STORE_B_FP:
 				  memWriteByte(FP,(byte)B);
 				  break;
 				case B_STORE_SP_A:
 				  memWriteByte(A,(byte)SP);
 				  break;
 				case B_STORE_SP_B:
 				  memWriteByte(B,(byte)SP);
 				  break;
 				case B_STORE_SP_FP:
 				  memWriteByte(FP,(byte)SP);
 				  break;
 				case B_STORE_FP_A:
 				  memWriteByte(A,(byte)FP);
 				  break;
 				case B_STORE_FP_B:
 				  memWriteByte(B,(byte)FP);
 				  break;
 				case B_STORE_FP_SP:
 				  memWriteByte(SP,(byte)FP);
 				  break;
 				
 				// moves
 				
 				case MOVE_COUNTER_A:
 					A = counter;
 					break;
 				case MOVE_A_B:
 				  B = A;
 				  break;
 				case MOVE_A_SP:
 				  SP = A;
 				  break;
 				case MOVE_A_FP:
 				  FP = A;
 				  break;
 				case MOVE_B_A:
 				  A = B;
 				  break;
 				case MOVE_B_SP:
 				  SP = B;
 				  break;
 				case MOVE_B_FP:
 				  FP = B;
 				  break;
 				case MOVE_SP_A:
 				  A = SP;
 				  break;
 				case MOVE_SP_B:
 				  B = SP;
 				  break;
 				case MOVE_SP_FP:
 				  FP = SP;
 				  break;
 				case MOVE_FP_A:
 				  A = FP;
 				  break;
 				case MOVE_FP_B:
 				  B = FP;
 				  break;
 				case MOVE_FP_SP:
 				  SP = FP;
 				  break;
 
 				// REGISTER OPERATIONS
 
 				// int arithmetic
 				case I_ADD:
 					A = A + B;
 					break;
 				case I_SUB:
 					A = A - B;
 					break;
 				case I_MUL:
 					A = A * B;
 					break;
 				case I_DIV:
 					A = A / B;
 					break;
 				case I_MOD:
 					A = A % B;
 					break;
 				case INC_A:
 					A++;
 					break;
 				// float arithmetic
 				case F_ADD:
 				  A = Float.floatToIntBits(Float.intBitsToFloat(A) + Float.intBitsToFloat(B));
 				  break;
 				case F_SUB:
 				  A = Float.floatToIntBits(Float.intBitsToFloat(A) - Float.intBitsToFloat(B));
 				  break;
 				case F_MUL:
 				  A = A = Float.floatToIntBits(Float.intBitsToFloat(A) * Float.intBitsToFloat(B));
 				  break;
 				case F_DIV:
 				  A = Float.floatToIntBits(Float.intBitsToFloat(A) / Float.intBitsToFloat(B));
 				  break;
 				case F_MOD:
 				  A = Float.floatToIntBits(Float.intBitsToFloat(A) % Float.intBitsToFloat(B));
 				  break;
 				// inc/dec
 				case INC_B:
 					B++;
 					break;
 				case INC_SP:
 					SP++;
 					break;
 				case INC_SP_INT:
 					SP += 4;
 					break;
 				case DEC_A:
 					A--;
 					break;
 				case DEC_B:
 					B--;
 					break;
 				case DEC_SP:
 					SP--;
 					break;
 				case DEC_SP_INT:
 					SP -= 4;
 					break;
 				case DEC_SP_BY:
 					SP -= progReadInt();
 					break;
 				// logic (0: false; non-0: true)
 				case NEG_A:
 					if(A == 0)
 						A = 1;
 					else
 						A = 0;
 					break;
 				case EQ_A:
 				  if(A == 0)
 				    A = 1;
 				  else
 				    A = 0;
 				  break;
 				case LT_A:
 				  if(A < 0)
 				    A = 1;
 				  else
 				    A = 0;
 				  break;
 				case GT_A:
 				  if(A > 0)
 				    A = 1;
 				  else
 				    A = 0;
 				  break;
 				case AND:
 					if(A != 0 && B != 0)
 						A = 1;
 					else
 						A = 0;
 					break;
 				case OR:
 					if(A != 0 || B != 0)
 						A = 1;
 					else
 						A = 0;
 					break;
 				
 				case STACK_START:
 					A = stackStart;
 					break;
 				
 				// io
 				case PRINT_CHAR_A:
 					out.print((char)A);
 					break;
 				case PRINT_INT_A:
 				  out.println(A);
 				  break;
 				case READ_CHAR_A:
 					try {
 						A = in.read();
 					} catch(IOException e) {
 						out.println(e.getMessage());
 					}
 					break;
 			}
 			if(debug)
 				System.out.println(" => [A: "+A+" B: "+ B+" FP: " + FP + " SP: " + SP + "]");
 		}
 	}
 	
 	// PROGRAM READERS
 	
 	private byte progReadByte() throws MalformedProgramError {
 		byte result;
 		try {
 			result = program[counter];
 		} catch(ArrayIndexOutOfBoundsException e) {
 			throw getMalfProgEx();
 		}
 		counter++;
 		return result;
 	}
 	
 	private int progReadInt() throws MalformedProgramError {
 		return ((progReadByte() & 0xff) << 24) |
 		       ((progReadByte() & 0xff) << 16) |
 		       ((progReadByte() & 0xff) << 8) |
 			     (progReadByte() & 0xff);
 	}
 	
 	// MEMORY READERS & WRITERS
 	
 	private byte memReadByte(int addr) throws StackOverflowError {
 		try {
 			return memory[addr];
 		} catch(ArrayIndexOutOfBoundsException e) {
 			throw new StackOverflowError();
 		}
 	}
 	
 	private int memReadInt(int addr) throws StackOverflowError {
 		return ((memReadByte(addr) & 0xff) << 24) |
 			     ((memReadByte(addr+1) & 0xff) << 16) |
 			     ((memReadByte(addr+2) & 0xff) << 8) |
 				   (memReadByte(addr+3) & 0xff);
 	}
 	
 	private void memWriteByte(int addr, byte val) throws StackOverflowError {
 		try {
 			memory[addr] = val;
 		} catch(ArrayIndexOutOfBoundsException e) {
 			throw new StackOverflowError();
 		}
 	}
 	
 	private void memWriteInt(int addr, int val) throws StackOverflowError {
 		memWriteByte(addr  ,(byte)(0xff & (val >> 24)));
 		memWriteByte(addr+1,(byte)(0xff & (val >> 16)));
 		memWriteByte(addr+2,(byte)(0xff & (val >> 8)));
 		memWriteByte(addr+3,(byte)(0xff &  val));
 	}
 	
 	private MalformedProgramError getMalfProgEx() {
 		return new MalformedProgramError();
 	}
 	
 }
