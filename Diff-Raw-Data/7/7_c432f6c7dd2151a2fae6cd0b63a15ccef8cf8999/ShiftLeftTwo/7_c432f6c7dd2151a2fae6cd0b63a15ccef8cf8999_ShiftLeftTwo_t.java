 public class ShiftLeftTwo implements Clockable{
 	Pin in = new Pin();
 	Pin out = new Pin();
 	int outputBits;
 	
 	ShiftLeftTwo(int outputBits){
 		this.outputBits = outputBits;
 	}
 
 	public void clockEdge() {
		if(outputBits == 32){
			out.setValue(in.getValue().shiftLeft().shiftLeft());
		}
		else{
			out.setValue(in.getValue().shiftLeftChangeSize().shiftLeftChangeSize());
		}
 	}
 	
 	
 }
