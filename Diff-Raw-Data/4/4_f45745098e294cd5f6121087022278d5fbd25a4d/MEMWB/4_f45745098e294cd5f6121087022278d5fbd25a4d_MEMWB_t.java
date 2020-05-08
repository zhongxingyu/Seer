 public class MEMWB implements Clockable{
 	// inputs
 	public Pin readData = new Pin();
 	public Pin genALUresult = new Pin();
 	public Pin rd = new Pin();
 	// outputs
 	public Pin outReadData = new Pin();
 	public Pin outGenALUresult = new Pin();
 	public Pin outRd = new Pin();
 	// control signals (WB)
 	public Pin regWrite = new Pin();
 	public Pin memToReg = new Pin();
 	public Pin outregWrite = new Pin();
 	public Pin outmemToReg = new Pin();
		
	public MEMWB(){
 		readData.setValue(new BinaryNum("0").pad(32));
 		genALUresult.setValue(new BinaryNum("0").pad(32));
 		regWrite.setValue(new BinaryNum("0"));
 		memToReg.setValue(new BinaryNum("0"));
 		rd.setValue(new BinaryNum("0").pad(5));
 		outReadData.setValue(new BinaryNum("0").pad(32));
 		outGenALUresult.setValue(new BinaryNum("0").pad(32));
 		outregWrite.setValue(new BinaryNum("0"));
 		outmemToReg.setValue(new BinaryNum("0"));
 		outRd.setValue(new BinaryNum("0").pad(5));
 	}
 	
 	public void clockEdge(){
 		outReadData.setValue(readData.getValue());
 		outGenALUresult.setValue(genALUresult.getValue());
 		outregWrite.setValue(regWrite.getValue());
 		outmemToReg.setValue(memToReg.getValue());
 		outRd.setValue(rd.getValue());
 	}
 }
