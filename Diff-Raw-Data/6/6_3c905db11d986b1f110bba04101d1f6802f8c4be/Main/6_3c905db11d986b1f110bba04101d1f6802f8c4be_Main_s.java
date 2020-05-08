 import java.util.Iterator;
 import java.util.List;
 
 
 public class Main {
 
 	/**
 	 * Entry point for program
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		
 		//Read instructions from program file
 		List<Long> instructions = ProgramReader.getInstructions(args[0]);
 		
 		//load instructions into instruction memory
 		Long numInstr = (long) instructions.size();
 		Long firstInstrAddr = Long.parseLong("1000", 16);
 		Memory instrMemory = new Memory(firstInstrAddr,firstInstrAddr+(numInstr*4-1));
 
 		for(int i = 0; i < numInstr ; i++){
			System.out.println(firstInstrAddr+i*4);
 			instrMemory.storeWord(firstInstrAddr+i*4, instructions.get(i));
 		}
 		
 		//initialize data memory
 		Memory dataMemory = new Memory(Long.parseLong("0", 16),Long.parseLong("ffc", 16));
 		
 		//initialize everything
 		Mux aluSrcMux = new Mux();
 		Mux memToRegMux = new Mux();
 		Mux regDstMux = new Mux();
 		Mux jumpMux = new Mux();
 		Mux branchMux = new Mux();
 		ShiftLeftTwo slt1 = new ShiftLeftTwo();
 		ShiftLeftTwo slt2 = new ShiftLeftTwo();
 		ALUControl aluControl = new ALUControl();
 		ALU alu = new ALU();
 		ALU aluAdd = new ALU();
 		aluAdd.control.setValue((long)2);
 		ALU aluP4 = new ALU();
 		aluP4.control.setValue((long)2);
 		aluP4.input2.setValue((long) 4);
 		MemoryIO memoryIo = new MemoryIO(dataMemory);
 		Control control = new Control();
 		RegisterFile regFile = new RegisterFile();
 		Fetch fetch = new Fetch(instrMemory);
 		And branchMuxAnd = new And();
 		ProgramCounter pc = new ProgramCounter();
 		Decode decode = new Decode();
 		
 		// Connect output of PC
 		pc.pcOut.connectTo(fetch.pc);
 		
 		// Connect output of fetch
 		fetch.instr.connectTo(decode.instruction);
 		
 		// Connect the outputs of the decode
 		decode.opcode.connectTo(control.opcode);
 		decode.rs.connectTo(regFile.readReg1);
 		decode.rt.connectTo(regFile.readReg2);
 		decode.rt.connectTo(regDstMux.input0);
 		decode.rd.connectTo(regDstMux.input1);
 		decode.target.connectTo(slt1.in);
 		decode.offset.connectTo(slt2.in);
 		decode.funct.connectTo(aluControl.func);
 		
 		// Connect outputs of regFile
 		regFile.readData1.connectTo(alu.input1);
 		regFile.readData2.connectTo(aluSrcMux.input0);
 		regFile.readData2.connectTo(memoryIo.writeData);
 		
 		// Connect outputs of ALU and related
 		aluSrcMux.output.connectTo(alu.input2);
 		alu.result.connectTo(memoryIo.address);
 		alu.result.connectTo(memToRegMux.input0);
 		alu.zero.connectTo(branchMuxAnd.input1);
 		
 		// Connect output of memoryIo
 		memoryIo.readData.connectTo(memToRegMux.input1);
 		
 		memToRegMux.output.connectTo(regFile.writeData);
 		regDstMux.output.connectTo(regFile.writeReg);
 		slt1.out.connectTo(jumpMux.input1);
 		slt2.out.connectTo(aluAdd.input2);
 		branchMuxAnd.output.connectTo(branchMux.switcher);
 		jumpMux.output.connectTo(pc.pcIn);
 		
 		// connect the control signals
 		control.regDst.connectTo(regDstMux.switcher);
 		control.jump.connectTo(jumpMux.switcher);
 		control.branch.connectTo(branchMuxAnd.input0);
 		control.memRead.connectTo(memoryIo.memRead);
 		control.memToReg.connectTo(memToRegMux.input1);
 		control.aluOp.connectTo(aluControl.aluOp);
 		control.memWrite.connectTo(memoryIo.memWrite);
 		control.aluSrc.connectTo(aluSrcMux.switcher);
 		control.regWrite.connectTo(regFile.regWrite);
 		aluControl.aluControl.connectTo(alu.control);
 		
 		pc.pcIn.setValue(firstInstrAddr);
 		for(Long instruction : instructions){
 			pc.clockEdge();
 			// fetch the instruction
 			fetch.clockEdge();
 			
 			// decode the instruction
 			decode.clockEdge();
 			if(decode.isHalt()){
 				break;
 			}
 			
 			// set the output control signals
 			control.setSignals();
 			
 			// clock the mux that takes regDST as switcher
 			regDstMux.clockEdge();
 			
 			// clock the regfile
 			regFile.clockEdge();
 		}
 		System.out.println("Final register values:");
		for(int x = 0 ; x < 32 ; x++){
			System.out.println("    $r" + x + ": 0x" + Long.toHexString(regFile.getVal(x)));
 		}
 	}
 
 }
