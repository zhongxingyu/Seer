 package edu.osu.cse.mmxi.machine;
 
 import edu.osu.cse.mmxi.machine.memory.MemoryUtilities;
 
 /**
  * A specific Register that comes with an atomic operation for retrieving and incrementing
  * the Program Counter as an atomic operation.
  */
 public class FlagsRegister extends Register {
 
     public FlagsRegister(final int fill) {
         this(false, false, false);
         if (fill == -1)
            registerValue = (short) (1 << MemoryUtilities.randomShort() % 3);
         else
             setFlags((short) fill);
     }
 
     public FlagsRegister(final boolean initialN, final boolean initialZ,
         final boolean initialP) {
         super((short) ((initialN ? 4 : 0) + (initialZ ? 4 : 0) + (initialP ? 4 : 0)));
     }
 
     public boolean getN() {
         return getFlag(2);
     }
 
     public boolean getZ() {
         return getFlag(1);
     }
 
     public boolean getP() {
         return getFlag(0);
     }
 
     private boolean getFlag(final int index) {
         return (registerValue & 1 << index) != 0;
     }
 
     public void setN(final boolean val) {
         setFlag(2, val);
     }
 
     public void setZ(final boolean val) {
         setFlag(1, val);
     }
 
     public void setP(final boolean val) {
         setFlag(0, val);
     }
 
     private void setFlag(final int index, final boolean val) {
         if (val)
             registerValue |= 1 << index;
         else
             registerValue &= ~(1 << index);
     }
 
     public void setFlags(final Register register) {
         setFlags(register.getValue());
     }
 
     public void setFlags(final short s) {
         setN(s < 0);
         setZ(s == 0);
         setP(s > 0);
     }
 
     @Override
     public String toString() {
         return "FLAGS " + (getN() ? "n" : "") + (getZ() ? "z" : "") + (getN() ? "p" : "");
     }
 }
