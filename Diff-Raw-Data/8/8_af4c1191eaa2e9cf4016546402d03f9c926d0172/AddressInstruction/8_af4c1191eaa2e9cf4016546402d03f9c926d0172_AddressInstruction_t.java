 import java.util.*;
 
 public class AddressInstruction extends Instruction
 {
   public Register src;
   public Register dest;
   public String offset;
   public int sparcOffset;
   
   public AddressInstruction(String name, Register src, Register dest, String offset, int sparcOffset)
   {
     super(name);
     this.src = src;
     this.dest = dest;
     this.offset = offset;
     this.sparcOffset = sparcOffset;
   }
   
   public String toSparc()
   {
     if(name.equals("storeai")) {
       return new String("st " + src.sparcName + ", [" + dest.sparcName + "+" + sparcOffset + "]");
     } else if(name.equals("loadai")) {
       return new String("ld [" + src.sparcName + "+" + sparcOffset + "], " + dest.sparcName);
     } else {
       return new String("cheater");
     }
   }
   
   public String toString()
   {
     if(name.equals("storeai")) {
       return new String(name + " " + src + ", " + dest + ", " + offset);
     } else if(name.equals("loadai")) {
       return new String(name + " " + src + ", " + offset + ", " + dest);
     } else {
       return new String(name + " " + src + ", " + dest + ", " + offset);
     }
   }
   
   public ArrayList<Register> getSources(){
     ArrayList<Register> ret = new ArrayList<Register>();
    if(name.equals("storeai")) {
      ret.add(dest);
    }
     ret.add(src);
     return ret;
   }
   
   public ArrayList<Register> getDests(){
     ArrayList<Register> ret = new ArrayList<Register>();
    if(name.equals("loadai")) {
      ret.add(dest);
    }
     return ret;
   }
 }
