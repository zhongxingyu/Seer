 import java.io.BufferedWriter;
 import java.io.FileWriter;
 
 public class DebuggerPR{
 	
 	public IFID ifid;
 	public IDEX idex;
 	public EXMEM exmem;
 	public MEMWB memwb;
 	public String output = "";
 	
 	public DebuggerPR(IFID ifid, IDEX idex, EXMEM exmem, MEMWB memwb){
 		this.ifid = ifid;
 		this.idex = idex;
 		this.exmem = exmem;
 		this.memwb = memwb;
 	}
 	
 	public void debugCycle(int cycleCount){
 
 		output += "################################# End of Cycle " + cycleCount +" #################################\n";
 		output += "----------------------- IF/ID -----------------------\n";
 		output += "PC+4: " + print(ifid.PC4) + "\t\t" + printDecimal(ifid.PC4) + "\n";
 		output += "instruction:" + print(ifid.instruction) + "\n";
 		output += "out PC+4: " + print(ifid.outPC4) + "\t\t" + printDecimal(ifid.PC4) + "\n";
 		output += "out instruction:" + print(ifid.outInstr) + "\n";
 		output += "----------------------- ID/EX -----------------------\n";
 		output += "\t\t\tData information\n";
 		output += "PC+4 in:" + print(idex.PC4) + "\t\t" + printDecimal(ifid.PC4) + "\n";
 		output += "PC+4 out: " + print(idex.outPC4) + "\t\t" + printDecimal(ifid.PC4) + "\n";
 		output += "readData1 in:" + print(idex.readData1) + "\n";
 		output += "readData1 out:" + print(idex.outReadData1) + "\n";
 		output += "readData2 in:" + print(idex.readData2) + "\n";
 		output += "readData2 out:" + print(idex.outReadData2) + "\n";
 		output += "signExtended in:" + print(idex.signExtended) + "\n";
 		output += "signExtended out:" + print(idex.outSignExtended) + "\n";
 		output += "rt in:" + print(idex.rt) + "\n";
 		output += "rt out:" + print(idex.outRt) + "\n";
 		output += "rd in:" + print(idex.rd) + "\n";
 		output += "rd out:" + print(idex.outRd) + "\n";
 		output += "funct in:" + print(idex.funct) + "\n";
 		output += "funct out:" + print(idex.outFunct) + "\n";
 		output += "rs in:" + print(idex.rs) + "\n";
 		output += "rs out:" + print(idex.outRs) + "\n";
 		output += "\t\t\t Control information\n";
 		output += "regWrite in:" + print(idex.regWrite) + "\n";
 		output += "regWrite out:" + print(idex.outregWrite) + "\n";
 		output += "memToReg in:" + print(idex.memToReg) + "\n";
 		output += "memToReg out:" + print(idex.outmemToReg) + "\n";
 		output += "jump in:" + print(idex.jump) + "\n";
 		output += "jump out:" + print(idex.outjump) + "\n";
 		output += "jumpReg in:" + print(idex.jumpReg) + "\n";
 		output += "jumpReg out:" + print(idex.outjumpReg) + "\n";
 		output += "memWrite in:" + print(idex.memWrite) + "\n";
 		output += "memWrite out:" + print(idex.outmemWrite) + "\n";
 		output += "memRead in:" + print(idex.memRead) + "\n";
 		output += "memRead out:" + print(idex.outmemRead) + "\n";
 		output += "branch in:" + print(idex.branch) + "\n";
 		output += "branch out:" + print(idex.outbranch) + "\n";
 		output += "regDST in:" + print(idex.regDST) + "\n";
 		output += "regDST out:" + print(idex.outregDST) + "\n";
 		output += "aluOP in:" + print(idex.aluOp) + "\n";
 		output += "aluOP out:" + print(idex.outaluOp) + "\n";
 		output += "aluSrc in:" + print(idex.aluSrc) + "\n";
 		output += "aluSrc out:" + print(idex.outaluSrc) + "\n";
 		output += "branchBNE in:" + print(idex.branchBNE) + "\n";
 		output += "branchBNE out:" + print(idex.outbranchBNE) + "\n";
 		output += "----------------------- EX/MEM -----------------------\n";
 		output += "add ALU result:" + print(exmem.addALUresult) + "\n";
 		output += "zero:" + print(exmem.zero) + "\n";
 		output += "general ALU result:" + print(exmem.genALUResult) + "\n";
 		output += "reaData2:" + print(exmem.readData2) + "\n";
 		output += "out add ALU result:" + print(exmem.outAddALUresult) + "\n";
 		output += "out zero:" + print(exmem.outZero) + "\n";
 		output += "out general ALU result:" + print(exmem.outGenALUresult) + "\n";
 		output += "out reaData2:" + print(exmem.outReadData2) + "\n";
 		output += "\t\t\t Control information\n";
 		output += "regWrite in:" + print(exmem.regWrite) + "\n";
 		output += "regWrite out:" + print(exmem.outregWrite) + "\n";
 		output += "memToReg in:" + print(exmem.memToReg) + "\n";
 		output += "memToReg out:" + print(exmem.outmemToReg) + "\n";
 		output += "jump in:" + print(exmem.jump) + "\n";
 		output += "jump out:" + print(exmem.outjump) + "\n";
 		output += "jumpReg in:" + print(exmem.jumpReg) + "\n";
 		output += "jumpReg out:" + print(exmem.outjumpReg) + "\n";
 		output += "memWrite in:" + print(exmem.memWrite) + "\n";
 		output += "memWrite out:" + print(exmem.outmemWrite) + "\n";
 		output += "memRead in:" + print(exmem.memRead) + "\n";
 		output += "memRead out:" + print(exmem.outmemRead) + "\n";
 		output += "branch in:" + print(exmem.branch) + "\n";
 		output += "branch out:" + print(exmem.outbranch) + "\n";
 		output += "----------------------- MEM/WB -----------------------\n";
 		output += "readData:" + print(memwb.readData) + "\n";
 		output += "general ALU result:" + print(memwb.genALUresult) + "\n";
 		output += "out readData:" + print(memwb.outReadData) + "\n";
 		output += "out general ALU result:" + print(memwb.outGenALUresult) + "\n";
 		output += "\t\t\t Control information\n";
 		output += "regWrite in:" + print(memwb.regWrite) + "\n";
 		output += "regWrite out:" + print(memwb.outregWrite) + "\n";
 		output += "memToReg in:" + print(memwb.memToReg) + "\n";
 		output += "memToReg out:" + print(memwb.outmemToReg) + "\n\n";
 
 	}
 	
 	public String print(Pin p){
 		if(p.getValue() == null){
 			return "null";
 		}else{
 			return p.getValue().toString();
 		}
 			
 	}
 	
	public long printDecimal(Pin p){
 		if(p.getValue() == null){
 			return -1;
 		}else{
			return p.getValue().toLong();
 		}
 	}
 	
 	public void dump(String fileName){
 		try{
 			// Create file 
 			FileWriter fstream = new FileWriter(fileName);
 			BufferedWriter out = new BufferedWriter(fstream);
 			out.write(output);
 			//Close the output stream
 			out.close();
 			}catch (Exception e){//Catch exception if any
 			System.err.println("Error: " + e.getMessage());
 			}
 		}
 }
