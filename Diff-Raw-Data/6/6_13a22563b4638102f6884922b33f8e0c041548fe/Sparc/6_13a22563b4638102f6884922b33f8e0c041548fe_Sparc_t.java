 import java.util.*;
 import java.lang.*;
 
 public abstract class Sparc {
    ArrayList<Operand> operands = new ArrayList<Operand>();
 
    void addSource(Operand in) {
       this.addOp(in);
    }
 
    public void addDest(Operand in) {
       this.addOp(in);
    }
 
    public void addOp(Operand in) {
      if (in instanceof ID) {
         this.operands.add(0, in);
      } else {
         this.operands.add(in);
      }
    }
 
    public ArrayList<Operand> getOperands() {
       return this.operands;
    }
 }
