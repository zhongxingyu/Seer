 public class ShiftLeftTwo implements Clockable{
 	Pin in = new Pin();
 	Pin out = new Pin();
 	int outputBits;
 	
 	ShiftLeftTwo(int outputBits){
 		this.outputBits = outputBits;
 	}
 
 	public void clockEdge() {
		out.setValue(in.getValue().shiftLeft());
 	}
 	
 	
 }
