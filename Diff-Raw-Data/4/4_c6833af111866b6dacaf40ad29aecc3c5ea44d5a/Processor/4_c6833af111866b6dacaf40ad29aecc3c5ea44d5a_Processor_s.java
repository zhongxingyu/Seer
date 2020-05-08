 package mips;
 import java.util.List;
 import java.util.ListIterator;
 
 
 
 public class Processor {
 
 	private Instruction[] instructions = {};
 	private int pc;
 
 	private Control control;
 	private RegisterFile register;
 	private MemoryFile memory;
 	private ALU alu;
 
 	public Processor() {
 		control = new Control();
 		register = new RegisterFile();
 		memory = new MemoryFile();
 		alu = new ALU();
 
 	}
 
 	public void setInstructionSet(List<Instruction> instructions) {
 		this.instructions = new Instruction[instructions.size()];
 		ListIterator<Instruction> iterator = instructions.listIterator();
 		for(int i = 0; i < instructions.size(); i++) {
 			this.instructions[i] = iterator.next();
 		}
 	}
 	
 	public void reset() {
 		pc = 0;
 		register.reset();
 		memory.reset();
 	}
 
 	public void step() {
 		Instruction i;
 		short alu_out = 0;
 		short data_out = 0;
 		short rdv = 0;
 		short rtv = 0;
 		short rsv = 0;
 
 		if((pc/4) >= instructions.length) {
 			return;
 		}
 		i = instructions[pc/4];
 		control.setInstruction(i);
 
 
 		pc += 4;
 
 		rtv = register.get(i.getRt());
 		rsv = register.get(i.getRs());
 		rdv = register.get(i.getRd());
 
 		if(control.isALUOp()) {
 			alu_out = alu.operation(
 					i.getOpcode(),
 					i.getFunct(),
					i.getRt(),
					mux(rtv, i.getAddr(), control.isALUsrc()));
 		}
 
 		if(control.isMemRead()) {
 			data_out = memory.get(alu_out);
 		}
 
 		if(control.isRegWrite()) {
 			register.set(i.getRd(),
 					(byte)mux(alu_out, data_out, control.isMemtoReg()));
 		}
 
 	}
 
 	private short mux(short value1, short value2, boolean getSecond) {
 		if(getSecond) {
 			return value2;
 		}
 		return value1;
 	}
 
 	public boolean isDone() {
 		return (pc/4) >= instructions.length || instructions[pc/4].isExit();
 	}
 
 	/**
 	 * @return the pc
 	 */
 	public int getPc() {
 		return pc;
 	}
 	
 	public byte[] getRegisters() {
 		return register.getBytes();
 	}
 	
 	public byte[] getMemory() {
 		return memory.getBytes();
 	}
 	
 	public List<Integer> getChangedRegisters() {
 		return register.getChangedIndices();
 	}
 	
 	public List<Integer> getChangedMemory() {
 		return memory.getChangedIndices();
 	}
 
 
 }
