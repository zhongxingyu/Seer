 public class RegisterFile implements Clockable{
 	
 	public Register[] regFile;
 	
 	public Pin writeReg = new Pin();
 	public Pin readReg1 = new Pin();
 	public Pin readReg2 = new Pin();	
 	public Pin writeData = new Pin();
 	public Pin regWrite = new Pin();
 	public Pin readData1 = new Pin();
 	public Pin readData2 = new Pin();
 	
 	public RegisterFile(){
 		
 		// initializes with 32 words of memory set to 0
 		regFile = new Register[32];
 		for(int x=0; x<32; x++){
 			regFile[x] = new Register(0);
 		}
 	}
 	
 	// gets the value stored at register $'index'
 	public long getVal(int index){
 		return regFile[index].getValue();
 	}
 	
 	// updates value at register $'index' with 'val'
 	public void setRegister(int index, long val){
 		if(index >=1 && index < 32){
 			regFile[index].setValue(val);
 		}
 		else throw new Error("Cannot access $r" + index);
 	}
 	
 	public void clockEdge(){		
 		// if regWrite is 1, then we write to the register
 		// which is stored in writeReg. Data is stored in writeData;
 		if(regWrite.getValue() != null && regWrite.getValue() == (long)1){
 			setRegister(writeReg.getValue().intValue(), writeData.getValue());
 		}
 		
		readData1.setValue(getVal(readReg1.getValue().intValue()));
		readData2.setValue(getVal(readReg2.getValue().intValue()));
 	}
 	
 }
