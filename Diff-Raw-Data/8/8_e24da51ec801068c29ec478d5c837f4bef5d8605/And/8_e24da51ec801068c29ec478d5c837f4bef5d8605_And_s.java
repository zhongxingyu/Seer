 
 public class And implements Clockable {
 	public final Pin output = new Pin(); 
 	
 	public final Pin input0 = new Pin();
 	
 	public final Pin input1 = new Pin();
 	
 	And(){}
 	
 	public void clockEdge() {
		if(input0.getValue().equals(1) && input1.getValue().equals(1)){
 			output.setValue((long)1);
 		} else {
 			output.setValue((long)0);
 		}
		
		System.out.println("[DEBUG] Class:And" +
						  "\noutput:" + BinaryUtil.pad(Long.toBinaryString(output.getValue()), 32) +
				          "\ninput0:" + BinaryUtil.pad(Long.toBinaryString(output.getValue()), 32) +
						  "\ninput1:" + BinaryUtil.pad(Long.toBinaryString(output.getValue()), 32));
 	}
 
 }
