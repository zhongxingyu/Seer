 import java.util.*;
 
 public class LocalValueNumbering {
    private Map<String, String> numberings;
    private Map<Instruction, String> computed;
   private Map<String, Instruction> computed2;
    private Integer nextNumber = 1;
 
    public void optimize(Node block) {
      while (number(block)) { 
         System.out.println("number again");
      } 
   }

   public boolean number(Node block) {
       numberings = new HashMap<String, String>();
       computed = new HashMap<Instruction, String>();
      computed2 = new HashMap<String, Instruction>();
       String exp, target, dest, val;
       ArrayList<Instruction> instructions = block.getInstr();
       Instruction instr, temp;
 
       Set<String> valuesToSave = new HashSet<String>();
 
       for (int ndx = 0; ndx < instructions.size(); ndx++) {
          instr = instructions.get(ndx);
 
          if (isOptimizable(instr)) { // TODO clean this up.
             if (!(instr instanceof MovInstruction)) {
                exp = getValueOf(instr);
                if (numberings.get(exp) != null) {
                   //System.out.println("found optimization!");
                   //System.out.println(numberings.get(exp) + " instead of " + exp);
                   valuesToSave.add(numberings.get(exp));
 
                   temp = new MovInstruction();
                   temp.addSource(new Register("LVN" + numberings.get(exp)));
                   temp.addDest(instr.getDestinations().get(0));
 
                   instructions.set(ndx, temp); 
                } else {
                   // Keep track of the computed value.
                   target = getTargetOf(instr);
                   numberings.put(exp, target);
                   computed.put(instr, target);
                  computed2.put(target, instr);
                }
             }
 
             // Set the destination to the value as well.
             dest = instr.getDestinations().get(0).toString();
             val = numberings.get(getValueOf(instr));
             numberings.put(dest, val);
          }
       }
 
       // Second pass to add temp registers and mov instructions.
       for (int ndx = 0; ndx < instructions.size(); ndx++) {
          instr = instructions.get(ndx);
          target = computed.get(instr);
 
          if (target != null) {
             if (valuesToSave.contains(target)) {
                // We need the temp register.
                temp = new MovInstruction();
                temp.addSource(instr.getDestinations().get(0));
                temp.addDest(new Register("LVN" + target));
 
                instructions.add(ndx + 1, temp);
             }
          }
       }

      // Return true if optimization happened.
      return !valuesToSave.isEmpty();
    }
 
    /** For debugging purposes only. */
    public String toString() {
       String ret = "";
 
       for (String key : numberings.keySet()) {
          ret += key + " -> " + numberings.get(key) + "\n";
       }
 
       return ret;
    }
 
    private String getValueOf(Instruction instr) {
       List<Operand> ops = instr.getOperands();
       String ret = "";
 
       if (instr instanceof MovInstruction) {  // HACK
          return instr.getSources().get(0).toString(); 
       }
 
       ArrayList<String> srcValues = new ArrayList<String>();
 
       for (int ndx = 0; ndx < ops.size(); ndx++) {
          if (ndx != ops.size() - 1) { // exclude the destination.
             srcValues.add(getValueOf(ops.get(ndx)));
          }
       }
 
       ret = getOpSymbol(instr);

      if (isAssociative(instr)) {
         Collections.sort(srcValues);
      }

       for (int ndx = 0; ndx < srcValues.size(); ndx++) {
          ret += srcValues.get(ndx);
 
          if (ndx != srcValues.size() - 1) {
             ret += ", ";
          }
       }
 
       return ret;
    }
 
    private String getTargetOf(Instruction instr) {
       return getValueOf(instr.getDestinations().get(0));
    }
 
    /**
     * Get string representation of expression.
     * @returns non empty string when instruciton is 
     * arithmetic.
     * Otherwise returns an empty string.
     */
    private String getOpSymbol(Instruction instr) {
       return instr.getClass().getName().substring(0, 3) + " ";
    }
 
    private boolean isOptimizable(Instruction instr) {
       return (instr instanceof AddInstruction
        || instr instanceof AddiInstruction
        || instr instanceof DivInstruction
        || instr instanceof MultInstruction
        || instr instanceof SubInstruction
        || instr instanceof AndInstruction
        || instr instanceof OrInstruction
        || instr instanceof XoriInstruction
        || instr instanceof MovInstruction);
    }
 
   private boolean isAssociative(Instruction instr) {
      return (instr instanceof AddInstruction
       || instr instanceof AddiInstruction
       || instr instanceof MultInstruction
       || instr instanceof AndInstruction
       || instr instanceof OrInstruction
       || instr instanceof XoriInstruction);
   }

    /**
     * If a operand is already numbered, get that number.
     * Otherwise return a new number value and put
     * pair into numberings.
     */
    private String getValueOf(Operand op) {
       String opString = op.toString();
       String value;
 
       if (op instanceof Immediate) { // HACK
          opString = "i" + opString;
       }
 
       value = numberings.get(opString);
 
       if (value == null) {
          numberings.put(opString, nextNumber.toString());
 
          value = "" + nextNumber++;
       }
 
       return value;
    }
 }
