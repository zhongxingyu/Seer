 package vm;
 
 public class Start {
 	public static void main(String[] args) {
 //		byte[] instructions = 
 //			{0x00, 0x00, 0x00, 0x00, 				//0-3:  4-bytes header 
 //				0x02, 0x01, 						//4-5:   load 1 
 //				//:add_loop
 //				0x02, 0x01, 						//6-7:   load 1 
 //				0x01,	    						//8:     add
 //				
 //				0x02, 0x00, 						//9-10:  load 0
 //				0x22,       						//11:    load stack[sp - stack[sp]]
 //				0x02, 0x04, 						//12-13: load 4
 //				
 //				0x04, 								//14:    cmp
 //				
 //				0x02, 0x06,							//15-16
 //				0x02, 0x00,							//17-18
 //				0x02, 0x00,							//19-20
 //				0x02, 0x00,							//20-21
 //				(byte)0xB5, 						//21: 	jmpnz 0x00000006 (:add_loop)
 //
 //				0x16}; 								//20:    halt
 		
 		byte[] instructions = 
 			{0x00, 0x00, 0x00, 0x00, 				//0-3:  4-bytes header 
				0x02, 0x02, 						//4-5:   load 1
 				0x02, 0x01, 						//6-7:   load 1 
				0x71,	    						//8:     sub
 				0x16}; 								//20:    halt
 		
 		VM vm = new VM();
 		
 		int processID = vm.createProcess(instructions);
 		Process process = vm.getProcess(processID);
 			
 		while(process.isRunning()) {
 			vm.iterate();
 		}
 		
 		System.out.println("1 + 1 + 1 + 1 = " + process.getStack()[process.getSp()]);
 		System.out.println("flags are: " + process.getFlags() + " (0x1 = ZERO/EQUAL, 0x2 = OVERFLOW/GREATER)");
 	}
 }
