 package ch.zhaw.inf3.emulator;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 @SuppressWarnings("unused")
 public class MiniPPC_CPU {
 	private int cycle_count = 0;
 	private short register[] = new short[4];
 	private short RAM[];
 	private short IP; // Next Instruction Pointer
 	private short IR; // Instruction Register
 	private boolean carry;
 	private InstructionSetArchitecture isa;
 	private InsImplAnnotationParser annotaion_parser;
 
 	public MiniPPC_CPU(int memory){
 		this.RAM = new short[memory];
 		this.isa = new InstructionSetArchitecture();
 		this.IP = isa.start_offset;
 		
 		annotaion_parser = new InsImplAnnotationParser();
 		annotaion_parser.parse(MiniPPC_CPU.class);
 	}
 	
 	public void runCycle(){
 		// Instruction Fetch
 		IR = RAM[IP];
 		
 		// Instruction Decode
 		Instruction op = isa.decodeWord(IR);
 		
 		// Increment IP
		IP = (short) (IP + isa.word_width);
 		
 		// Execute
 		executeInstruction(op);
 		
 		cycle_count = cycle_count + 1;
 	}
 
 	public void loadCode(short[] words){
 		for (int i = 0; i < words.length; i++) {
 			RAM[100+i] = words[i];
 		}
 	}
 	
 	public void loadDataAtOffset(short[] words, int offset){
 		for (int i = 0; i < words.length; i++) {
 			RAM[offset+i] = words[i];
 		}
 	}
 	
 	public void printRegisters(){
 		for (int i = 0; i < register.length; i++) {
 			System.out.println("R"+i+": "+ register[i]);
 		}
 		System.out.println("IR: "+ IR );
 	}
 
 	private void executeInstruction(Instruction ins) {
 		if (ins != null){
 			System.out.print("execute " + ins.mnemonic);
 			for (int i = 0; i < ins.num_operands; i++) {
 				System.out.print(" " +ins.operands[i]);
 			}
 			System.out.println();
 			
 			String method_name = annotaion_parser.instruction_map.get(ins.mnemonic);
 			System.out.println(annotaion_parser.instruction_map.size() +" "+  method_name);
 			try {
 				Method m = this.getClass().getDeclaredMethod(method_name, Instruction.class);
 				m.invoke(this, ins);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			
 		}
 	}
 	
 	
 	// Instruction Implementations
 	
 	@InstructionImpl("ADD")
 	private void add(Instruction ins){
 		register[0] += register[ins.operands[0]];
 		// check for overflow, set carry
 	}
 	
 	private void end(){}
 	
 	private void shiftLeftLogical(){}
 
 	@InstructionImpl("LWDD")
 	private void loadWordDirect(Instruction ins){
 		System.out.println("invoked lwdd with op1:" + ins.operands[0] + " op2:" + ins.operands[1]);
 	}
 	
 	private void storeWordDirect(){}
 }
