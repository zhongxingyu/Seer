 public class Inverter implements Clockable{
 	Pin in = new Pin();
 	Pin out = new Pin();
 	Pin branchBNE = new Pin();
 	
 	public Inverter(){
		in.setValue(new BinaryNum("0").pad(32));
		out.setValue(new BinaryNum("0").pad(32));
 		branchBNE.setValue(new BinaryNum("0"));
 	}
 	public void clockEdge(){
 		if(branchBNE.getValue().equals(new BinaryNum("1"))){
 			if(in.getValue().equals(new BinaryNum("1"))){
 				out.setValue(new BinaryNum("0"));
 			}else out.setValue(new BinaryNum("1"));
 		} else out.setValue(in.getValue());
 	}
 }
