 
 public class ALU implements Clockable{
 
 	Pin input1 = new Pin();
 	Pin input2 = new Pin();
 	Pin control = new Pin();	
 	Pin immediate = new Pin();
 	Pin result = new Pin();
 	Pin zero = new Pin();
 	
 	public ALU(){}
 	
 	
 	public void clockEdge() {
 		BinaryNum f = null;
 		BinaryNum a = input1.getValue();
 		BinaryNum b = input2.getValue();
 		BinaryNum controlVal = control.getValue();
 
 		if(controlVal.equals(new BinaryNum("000"))){
 			// bitwise and
 			f = a.and(b);
 		} else if(controlVal.equals(new BinaryNum("001"))){
 			// bitwise or
 			f = a.or(b);
 		} else if(controlVal.equals(new BinaryNum("010"))){
 			// add
 			f = a.add(b);
 		} else if(controlVal.equals(new BinaryNum("110"))){
 			// subtract
 			f = a.sub(b);
 		} else if(controlVal.equals(new BinaryNum("111"))){
 			// set if less than
 			f = a.setIfLessThan(b);
 		} else if(controlVal.equals(new BinaryNum("101"))){
 			// bitwise nor
 			f = a.nor(b);
 		}
 			else {
 			throw new RuntimeException("Unhandled ALU control: " + control.getValue().toString());
 		}
		if(f.toString().equals("0")) zero.setValue(new BinaryNum("1"));
 		
 		result.setValue(f);
 	}
 }
