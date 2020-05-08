 import java.util.*;
 
 public class UnaryInstruction extends Instruction
 {
   public Register reg;
   public Register sparcRegister;
   public Register sparcRegister2;
   
   public UnaryInstruction(String name, Register reg)
   {
     super(name);
     this.reg = reg;
     sparcRegister = new Register("%o0");
     sparcRegister2 = new Register("%o1");
   }
   
   public String toSparc(){
     if(name.equals("del")){
      return new String("mov " + reg.sparcName + ", " + sparcRegister.sparcName + "\n  call free\n  nop");
     } else if(name.equals("print")) {
       sparcRegister.sparcName = "%o0";
       return new String("mov " + reg.sparcName + ", %o1\n  sethi %hi(.LLC0), " + sparcRegister.sparcName + "\n  or " + sparcRegister.sparcName + ", %lo(.LLC0), " + sparcRegister.sparcName + "\n  call printf\n  nop");
     } else if(name.equals("println")) {
       sparcRegister.sparcName = "%o0";
       return new String("mov " + reg.sparcName + ", %o1\n  sethi %hi(.LLC1), " + sparcRegister.sparcName + "\n  or " + sparcRegister.sparcName + ", %lo(.LLC1), " + sparcRegister.sparcName + "\n  call printf\n  nop");
     } else if(name.equals("read")) {
       sparcRegister.sparcName = "%o0";
       return new String("set _read, %o1\n  sethi %hi(.LLC2), " + sparcRegister.sparcName + "\n  or " + sparcRegister.sparcName + ", %lo(.LLC2), " + sparcRegister.sparcName + "\n  call scanf\n  nop\n  set _read, %o1\n  ldsw [%o1], " + reg.sparcName);
     }
     return "";
   }
   
   public String toString()
   {
     return new String(name + " " + reg);
   }
   
   public ArrayList<Register> getSources(){
     ArrayList<Register> ret = new ArrayList<Register>();
     if(name.equals("del") || name.equals("print") || name.equals("println")){
        ret.add(reg);
     }
     return ret;
   }
   
   public ArrayList<Register> getDests(){
     ArrayList<Register> ret = new ArrayList<Register>();
     if(name.equals("read")){
       ret.add(reg);
       ret.add(sparcRegister);
       ret.add(sparcRegister2);
     }
     else if(name.equals("print") || name.equals("println")){
       ret.add(sparcRegister);
       ret.add(sparcRegister2);
     }
     return ret;
   }
 }
