 package nios;
 
 public class PC {
 
 	//int PCparts[];
 	String sPCparts[] = new String[6];
         private String linha = "00000000000000000000000000000000";
 	private int opCode, rA, rB, rC, opx, imm16, imm5;
 	
	
 	public void setPC(String linha){
             this.linha = linha;
             splitLinha();
         }
        
         private void splitLinha(){
                 sPCparts[0] = linha.substring(0, 5);   // rA              5bits
 		sPCparts[1] = linha.substring(5, 10);   // rB              5bits
 		sPCparts[2] = linha.substring(10, 15); // rC        imm16 5bits
 		sPCparts[3] = linha.substring(15, 21); // opx       imm16 5bits
 		sPCparts[4] = linha.substring(21, 26); // opx imm5  imm16 6bits
 		sPCparts[5] = linha.substring(26, 32); // opCode          6bits
 		
 		this.opCode = Integer.parseInt(sPCparts[5], 2);
 		this.rA = Integer.parseInt(sPCparts[0], 2);
 		this.rB = Integer.parseInt(sPCparts[1], 2);
                 this.imm16 = Integer.parseInt(sPCparts[2] + sPCparts[3] + sPCparts[4], 2);
                 this.rC = Integer.parseInt(sPCparts[2], 2);
 		this.opx = Integer.parseInt(sPCparts[3], 2);
 		this.imm5 = Integer.parseInt(sPCparts[4], 2);
         }
        
 
 	public int getOpCode() {
 		return opCode;
 	}
 
 
 	public int getrA() {
 		return rA;
 	}
 
 
 	public int getrB() {
 		return rB;
 	}
 
 
 	public int getrC() {
 		return rC;
 	}
 
 
 	public int getOpx() {
 		return opx;
 	}
 
 
 	public int getImm16() {
 		return imm16;
 	}
 	
         public int getImm5() {
             return imm5;
         }
 	
 	
 	
 }
