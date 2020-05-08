 public class ALU {
 	public String performOperation(String data1, String data2, int fifi){
 		long temp = 0;
 		String binaryValue;
 		switch (fifi){
 			case 100000: 
 				temp = Long.parseLong(data1,2)+Long.parseLong(data2,2);
 				binaryValue = Long.toBinaryString(temp);
 				return binaryValue;
 			case 100111:
 				temp = (Long.parseLong(data1,2)|Long.parseLong(data2,2));
 				String temp2 = Long.toBinaryString(temp);
 				binaryValue = "";
 				for(int i = 0; i < temp2.length(); i++ ) {
 					if(temp2.charAt(i) == '1') {
 						binaryValue += "0";
 					} else {
 						binaryValue += "1";
 					}
 				}
 				return binaryValue;
 		}
 		return "";
 	}
 }
