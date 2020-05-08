 package edu.osu.cse.mmxi.asm;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import edu.osu.cse.mmxi.asm.error.AsmCodes;
 import edu.osu.cse.mmxi.asm.line.InstructionLine;
 import edu.osu.cse.mmxi.asm.line.InstructionLine.Argument;
 import edu.osu.cse.mmxi.asm.line.InstructionLine.ExpressionArg;
 import edu.osu.cse.mmxi.asm.line.InstructionLine.RegisterArg;
 import edu.osu.cse.mmxi.asm.symb.ArithmeticParser;
 import edu.osu.cse.mmxi.asm.symb.SymbolExpression;
 import edu.osu.cse.mmxi.asm.symb.SymbolExpression.NumExp;
 import edu.osu.cse.mmxi.common.Utilities;
 import edu.osu.cse.mmxi.common.error.ParseException;
 
 /**
  * This contains the format used for all instructions and psuedo operations. This is used
  * to check each line for proper format and parse into Symbols and command.
  * 
  */
 public class InstructionFormat {
 
     /**
      * Static representation of all instructions and their formats including custom
      * instructions. This can be altered to include your own custom instructions. See the
      * developers guide for more information.
      */
     // @formatter:off
     public static final String[][] INST = {
         {"ADD",   "RRR",  "0001A--B--0xxC--"}, // ADD  Rd, Rs1, Rs2
         {"ADD",   "RR5",  "0001A--B--1C----"}, // ADD  Rd, Rs, #imm
         {"AND",   "RRR",  "0101A--B--0xxC--"}, // AND  Rd, Rs1, Rs2
         {"AND",   "RR5",  "0101A--B--1C----"}, // AND  Rd, Rs, #imm
         {"BR",    "9",    "0000000A--------"}, // BR   off
         {"BRn",   "9",    "0000100A--------"}, // BRn  off
         {"BRz",   "9",    "0000010A--------"}, // BRz  off
         {"BRp",   "9",    "0000001A--------"}, // BRp  off
         {"BRnz",  "9",    "0000110A--------"}, // BRnz off
         {"BRnp",  "9",    "0000101A--------"}, // BRnp off
         {"BRzp",  "9",    "0000011A--------"}, // BRzp off
         {"BRnzp", "9",    "0000111A--------"}, // BRnzp off
         {"DBUG",  "",     "1000xxxxxxxxxxxx"}, // DBUG
         {"JMP",   "9",    "01000xxA--------"}, // JMP  off
         {"JSR",   "9",    "01001xxA--------"}, // JSR  off
         {"JMPR",  "R6",   "11000xxA--B-----"}, // JMPR off
         {"JSRR",  "R6",   "11001xxA--B-----"}, // JSRR off
         {"LD",    "R9",   "0010A--B--------"}, // LD   Rd, off
         {"LDI",   "R9",   "1010A--B--------"}, // LDI  Rd, off
         {"LDR",   "RR6",  "0110A--B--C-----"}, // LDR  Rd, Rb, ind
         {"LEA",   "R9",   "1110A--B--------"}, // LEA  Rd, off
         {"NOT",   "RR",   "1001A--B--xxxxxx"}, // NOT  Rd, Rs
         {"RET",   "",     "1101xxxxxxxxxxxx"}, // RET
         {"ST",    "R9",   "0011A--B--------"}, // ST   Rs, off
         {"STI",   "R9",   "1011A--B--------"}, // STI  Rs, off
         {"STR",   "RR6",  "0111A--B--C-----"}, // STR  Rs, Rb, ind
         {"TRAP",  "8",    "1111xxxxA-------"}, // TRAP vect
 
         {"AND",   "RR",   "0101A--A--0xxB--"}, // AND  Rd, Rs       = AND Rd, Rd, Rs
         {"AND",   "R5",   "0101A--A--1B----"}, // AND  Rd, #imm     = AND Rd, Rd, #imm
         {"CLR",   "R",    "0101A--A--100000"}, // CLR  Rd           = AND Rd, #0
         {"CLR",   "9R",   "0101B--B--100000",  // CLR  off, Rj*     = CLR Rj
                           "0101B--B--100000"}, //                     ST Rj, off
         {"DBL",   "RR",   "0001A--B--0xxB--"}, // DBL  Rd, Rs       = ADD Rd, Rs, Rs
         {"DBL",   "R",    "0001A--A--0xxA--"}, // DBL  Rd           = DBL Rd, Rd
         {"DEC*",  "RR",   "1001A--A--xxxxxx",  // DEC  Rd, Rs       = NOT Rd
                           "0001A--A--0xxB--",  //      [s != d]       INC Rd, Rs
                           "1001A--A--xxxxxx"}, //                     NOT Rd
         {"DEC*",  "RR",   "0101A--A--100000"}, // DEC  Rd, Rs [s == d] = CLR Rd
         {"DEC*",  "R5",   "0001A--A--1B----"}, // DEC  Rd, #imm     = ADD Rd, Rd, #-imm
         {"DEC",   "R",    "0001A--A--111111"}, // DEC  Rd           = DEC Rd, #1
         {"INC",   "RR",   "0001A--A--0xxB--"}, // INC  Rd, Rs       = ADD Rd, Rd, Rs
         {"INC",   "R5",   "0001A--A--1B----"}, // INC  Rd, #imm     = ADD Rd, Rd, #imm
         {"INC",   "R",    "0001A--A--100001"}, // INC  Rd           = INC Rd, #1
         {"LDR",   "RR",   "0110A--B--000000"}, // LDR  Rd, Rb       = LDR Rd, Rb, #0
         {"MOV",   "RR",   "0101A--B--111111"}, // MOV  Rd, Rs       = AND Rd, Rs, #-1
         {"NEG",   "RR",   "1001A--B--xxxxxx",  // NEG  Rd, Rs       = NOT Rd, Rs
                           "0001A--A--100001"}, //                     INC Rd
         {"NEG",   "R",    "1001A--A--xxxxxx",  // NEG  Rd           = NOT Rd, Rd
                           "0001A--A--100001"}, //                     INC Rd
         {"NOT",   "R",    "1001A--A--xxxxxx"}, // NOT  Rd           = NOT Rd, Rd
         {"NOP",   "",     "0000000xxxxxxxxx"}, // NOP               = BR x0
         {"OR",    "RRR",  "1001C--C--xxxxxx",  // OR   Rd, Ra, Rb*  = NOT Rb
                           "1001A--B--xxxxxx",  //                     NOT Rd, Ra
                           "0101A--A--0xxC--",  //                     AND Rd, Rb
                           "1001A--A--xxxxxx"}, //                     NOT Rd
         {"POP",   "RR",   "0001B--B--111111",  // POP  Rd, Rstk     = DEC Rstk
                           "0110A--B--000000"}, //                     LDR Rd, Rstk
         {"PRNT",  "9",    "0010000A--------",  // PRNT off          = LD R0, off
                           "1111xxxx00100001"}, //                     TRAP OUT
         {"PRNT",  "R",    "0110000A--000000",  // PRNT Rs           = MOV R0, Rs
                           "1111xxxx00100001"}, //                     TRAP OUT
         {"PRNTR", "R",    "0101000A--111111",  // PRNTR Rs          = LDR R0, Rs
                           "1111xxxx00100001"}, //                     TRAP OUT
         {"PRNTR", "R6",   "0110000A--B-----",  // PRNTR Rs, index   = LDR R0, Rs, index
                           "1111xxxx00100001"}, //                     TRAP OUT
         {"PRNTS", "9",    "1110000A--------",  // PRNTS off         = LEA R0, off
                           "1111xxxx00100010"}, //                     TRAP PUTS
         {"PUSH",  "RR",   "0111A--B--000000",  // PUSH Rs, Rstk     = STR Rs, Rstk
                           "0001B--B--100001"}, //                     INC Rstk
         {"SHL*",  "R4",   "0001A--A--0xxA--"}, // SHL  Rd, 0<=imm<16 = (DBL Rd) [imm times]
         {"STR",   "RR",   "0111A--B--000000"}, // 
         {"SUB*",  "RRR",  "1001A--C--xxxxxx",  // SUB  Rd,Rs1,Rs2   = NOT Rd, Rs2
                           "0001A--A--100001",  //      [d != s1]      INC Rd
                           "0001A--A--0xxB--"}, //                     INC Rd, Rs1
         {"SUB*",  "RRR",  "1001A--A--xxxxxx",  // SUB  Rd,Rs1,Rs2   = NOT Rd
                           "0001A--A--0xxC--",  //      [d == s1]      INC Rd, Rs2
                           "1001A--A--xxxxxx"}, //                     NOT Rd
         {"TST",   "R",    "0101A--A--111111"}, // TST  Rd           = MOV Rd, Rd
         {"XCHG",  "RRR",  "0101C--A--111111",  // XCHG Ra, Rb, Rj*  = MOV Rj, Ra
                           "0101A--B--111111",  //      [j != a, b]    MOV Ra, Rb
                           "0101B--C--111111"}, //                     MOV Rb, Rj
         {"XNOR",  "RRR",  "1001A--B--xxxxxx",  // XNOR Rd,Ra*,Rb*   = NOT Rd, Ra
                           "0101A--A--0xxC--",  //      [a != b]       AND Rd, Rb
                           "1001C--C--xxxxxx",  //                     NOT Rb
                           "0101C--C--0xxB--",  //                     AND Rb, Ra
                           "1001C--C--xxxxxx",  //                     NOT Rb
                           "1001A--A--xxxxxx",  //                     NOT Rd
                           "0101A--A--0xxC--"}, //                     AND Rd, Rb
         {"XNOR",  "RRRR", "1001A--B--xxxxxx",  // XNOR Rd,Ra,Rb,Rj* = NOT Rd, Ra
                           "1001D--C--xxxxxx",  //      [j != a != b]  NOT Rj, Rb
                           "0101D--D--0xxB--",  //                     AND Rj, Ra
                           "0101A--A--0xxC--",  //                     AND Rd, Rb
                           "1001D--D--xxxxxx",  //                     NOT Rj
                           "1001A--A--xxxxxx",  //                     NOT Rd
                           "0101A--A--0xxD--"}, //                     AND Rd, Rj
         {"XOR",   "RRR",  "0101A--B--0xxC--",  // XOR  Rd,Ra*,Rb*   = AND Rd, Ra, Rb
                           "1001B--B--xxxxxx",  //      [a != b]       NOT Ra
                           "1001C--C--xxxxxx",  //                     NOT Rb
                           "0101C--C--0xxB--",  //                     AND Rb, Ra
                           "1001C--C--xxxxxx",  //                     NOT Rb
                           "1001A--A--xxxxxx",  //                     NOT Rd
                           "0101A--A--0xxC--"}, //                     AND Rd, Rb
         {"XOR",   "RRRR", "1001A--B--xxxxxx",  // XOR  Rd,Ra,Rb,Rj* = NOT Rd, Ra
                           "1001D--C--xxxxxx",  //      [j != a != b]  NOT Rj, Rb
                           "0101D--D--0xxA--",  //                     AND Rj, Rd
                           "0101A--B--0xxC--",  //                     AND Rd, Ra, Rb
                           "1001D--D--xxxxxx",  //                     NOT Rj
                           "1001A--A--xxxxxx",  //                     NOT Rd
                           "0101A--A--0xxD--"}};//                     AND Rd, Rj
  //     @formatter:on
     /**
      * Representation of a Map of instrucitons
      */
     public static final Map<String, List<IFRecord>> instructions = new HashMap<String, List<IFRecord>>();
     static {
         for (final String[] inst : INST) {
             final IFRecord r = interpretTextIFRecord(inst);
             final String key = r.name.toUpperCase() + ":" + r.signature.length();
             if (!instructions.containsKey(key))
                 instructions.put(key, new ArrayList<IFRecord>());
             instructions.get(key).add(r);
         }
     }
 
     /**
      * creates an if record from the instruction object
      * 
      * Result contains a list of tuples (arg, index, start, len), where arg is the index
      * of the argument in the argument list, index is the choice of which word (of a
      * multi-word or synthetic instruction) to replace, start is the index of the least
      * significant bit in the word, and len is the number of bits to replace
      * 
      * @param inst
      *            An array of strings created by the instruction parser.
      * @return IFRecord The IFRecord representation.
      */
     private static IFRecord interpretTextIFRecord(final String[] inst) {
         final IFRecord r = new IFRecord();
         if (inst[0].contains("*"))
             r.special = true;
         r.name = inst[0].replace("*", ""); // name contains the opcode name
         r.signature = inst[1]; // signature contains a string with the types of each
                                // argument in the characters
         // template contains the list of shorts before putting values in the fields
         // (all bits are 0 except required 1's)
         r.template = new short[inst.length - 2];
         final List<int[]> l = new ArrayList<int[]>();
         for (int i = 2; i < inst.length; i++) {
             r.template[i - 2] = (short) Integer.parseInt(inst[i].replaceAll("[^1]", "0"),
                 2);
             final Matcher m = Pattern.compile("[A-Z]-*").matcher(inst[i]);
             while (m.find())
                 l.add(new int[] { inst[i].charAt(m.start()) - 'A', i - 2,
                 /**/16 - m.end(), m.end() - m.start() });
         }
         // replacements contains a list of tuples (arg, index, start, len), where arg
         // is the index of the argument in the argument list, index is the choice of
         // which word (of a multi-word or synthetic instruction) to replace, start is
         // the index of the least significant bit in the word, and len is the number
         // of bits to replace
         r.replacements = l.toArray(new int[0][]);
         return r;
     }
 
     private static List<IFRecord> getInstruction(final String name, final int[] isReg)
         throws ParseException {
         final String key = name.toUpperCase() + ":" + isReg.length;
         if (!instructions.containsKey(key))
             throw new ParseException(AsmCodes.IF_BAD_ARG_NUM);
         final List<IFRecord> candidates = new ArrayList<IFRecord>(instructions.get(key));
         loop: for (Iterator<IFRecord> i = candidates.iterator(); i.hasNext();) {
             final IFRecord r = i.next();
             for (int j = 0; j < isReg.length; j++) {
                 final boolean sigIsReg = r.signature.charAt(j) == 'R';
                 if (!sigIsReg && isReg[j] == 2) {
                     isReg[j] = 0;
                     i = candidates.iterator();
                     continue loop;
                 }
                 if (isReg[j] == (sigIsReg ? 0 : 1)) {
                     i.remove();
                     continue loop;
                 }
             }
         }
         return candidates;
     }
 
     /**
      * Get the length.
      * 
      * @param inst
      *            The instruction object
      * @return
      * @throws ParseException
      */
     public static SymbolExpression getLength(final InstructionLine inst)
         throws ParseException {
         final int[] isReg = new int[inst.args.length];
         for (int i = 0; i < isReg.length; i++)
             isReg[i] = inst.args[i].isReg();
         final List<IFRecord> candidates = getInstruction(inst.opcode, isReg);
         if (candidates.size() == 0)
             throw new ParseException(AsmCodes.IF_SIG_INVALID,
                 "Immediate used in place of register or vice-versa");
         final SymbolExpression len = getSpecialLength(inst, candidates);
         if (len == null)
             return new NumExp((short) candidates.get(0).template.length);
         return len;
     }
 
     /**
      * Finds an instruction based on a name and signature. The {@code isReg} parameter
      * contains a list indicating if each parameter had a register (1) or not (0), or if
      * it was a symbol or symbol expression (2), so that it could be either. The
      * {@code values} parameter gives the actual short value of each argument. The return
      * value is a list of words that encode the instruction.
      * 
      * The {@code isReg} and {@code values} arrays must have the same length.
      * 
      * @param name
      *            the opcode
      * @param isReg
      *            whether each argument is a register: 0 = no, 1 = yes, 2 = unknown
      * @param values
      *            the value of each parameter
      * @return the list of words that encode the instruction
      */
     public static short[][] getInstruction(final Location lc, final InstructionLine inst)
         throws ParseException {
         final int[] isReg = new int[inst.args.length];
         final Location[] values = new Location[inst.args.length];
         boolean hasNull = false;
         final Set<Symbol> undef = new HashSet<Symbol>();
         for (int i = 0; i < isReg.length; i++) {
             isReg[i] = inst.args[i].isReg();
             if (inst.args[i] instanceof RegisterArg)
                 values[i] = new Location(false, ((RegisterArg) inst.args[i]).reg);
             else {
                 final SymbolExpression se = ArithmeticParser
                     .simplify(((ExpressionArg) inst.args[i]).val);
                 values[i] = Location.convertToRelative(se);
                 if (values[i] == null) {
                     CommonParser.undefinedSymbols(undef, se);
                     hasNull = true;
                 }
             }
         }
         if (hasNull) {
             CommonParser.errorOnUndefinedSymbols(undef);
             String s = "";
             for (final Argument arg : inst.args)
                 s += ", " + arg;
             throw new ParseException(AsmCodes.IF_ARG_CMX, "Attempted to encode "
                 + inst.opcode + " " + s.substring(2));
         }
         final List<IFRecord> candidates = getInstruction(inst.opcode, isReg);
         final String key = inst.opcode + ":" + isReg.length;
         if (candidates.size() == 0)
             throw new ParseException(AsmCodes.IF_SIG_INVALID,
                 "Immediate used in place of register or vice-versa");
         IFRecord rec = candidates.get(0);
         if (rec.special)
             rec = getSpecialInstruction(key, isReg, values, candidates);
         for (int i = 0; i < values.length; i++) {
             checkRange(i + 1, rec.signature.charAt(i), (short) values[i].address, inst);
             if (rec.signature.charAt(i) == '9') {
                 if (lc.isRelative ^ values[i].isRelative)
                     throw new ParseException(AsmCodes.IF_ABS_ADDR);
                if (((lc.address + 1 ^ values[i].address) & 0xFE00) != 0)
                    throw new ParseException(AsmCodes.IF_OFF_PAGE);
             } else if (values[i].isRelative)
                 throw new ParseException(AsmCodes.IF_ARG_CMX,
                     "relative parameter used in field which does not support it");
         }
         final short[][] ret = new short[rec.template.length][];
         for (int i = 0; i < ret.length; i++)
             ret[i] = new short[] { rec.template[i], -1 };
         for (final int[] rep : rec.replacements) {
             ret[rep[1]][0] |= (values[rep[0]].address & (1 << rep[3]) - 1) << rep[2];
             if (rep[3] == 9 && rep[2] == 0)
                 ret[rep[1]][1] = 0;
         }
         return ret;
     }
 
     private static IFRecord getSpecialInstruction(final String key, final int[] isReg,
         final Location[] values, final List<IFRecord> candidates) {
         IFRecord inst = candidates.get(0);
         if (key.equals("DEC:2")) {
             if (isReg[1] == 0)
                 values[1].address = (short) -values[1].address;
             else if (values[0] == values[1])
                 inst = candidates.get(1);
         } else if (key.equals("SHL:2")) {
             final IFRecord shl = inst;
             inst = new IFRecord();
             inst.template = new short[values[1].address];
             inst.replacements = new int[shl.replacements.length * values[1].address][];
             for (int i = 0; i < values[1].address; i++) {
                 inst.template[i] = shl.template[0];
                 for (int j = 0; j < shl.replacements.length; j++) {
                     final int[] arr = new int[4];
                     arr[0] = shl.replacements[j][0];
                     arr[1] = i;
                     arr[2] = shl.replacements[j][2];
                     arr[3] = shl.replacements[j][3];
                     inst.replacements[i * shl.replacements.length + j] = arr;
                 }
             }
         } else if (key.equals("SUB:3"))
             if (values[0].address == values[1].address)
                 inst = candidates.get(1);
         return inst;
     }
 
     private static SymbolExpression getSpecialLength(final InstructionLine inst,
         final List<IFRecord> candidates) {
         final String key = inst.opcode + ":" + inst.args.length;
         if (key.equals("DEC:2") && inst.args[1] instanceof RegisterArg) {
             final Short reg0, reg1 = ((RegisterArg) inst.args[1]).reg;
             if (inst.args[0] instanceof RegisterArg)
                 reg0 = ((RegisterArg) inst.args[0]).reg;
             else
                 reg0 = ((ExpressionArg) inst.args[0]).val.evaluate();
             if (reg0 != null)
                 return new NumExp((short) (reg0 == reg1 ? 1 : 3));
             else
                 try {
                     return ArithmeticParser.parseF("(:0 - :1 ? 2) + 1",
                         ((ExpressionArg) inst.args[0]).val, reg1);
                 } catch (final ParseException e) {
                     // won't happen
                     throw new RuntimeException("wtf");
                 }
         } else if (key.equals("SHL:2")) {
             final Short val = ((ExpressionArg) inst.args[1]).val.evaluate();
             if (val != null)
                 return new NumExp(val);
             else
                 return ((ExpressionArg) inst.args[1]).val;
         }
         return null;
     }
 
     private static void checkRange(final int index, final char sig, final short val,
         final InstructionLine inst) throws ParseException {
         final String arg = "at argument " + index + ": ";
         switch (sig) {
         case 'R':
             if (val < 0 || val >= 8)
                 throw new ParseException(AsmCodes.IF_ARG_RANGE, arg
                     + "register parameter R" + val + "; instruction: " + inst.toString());
             return;
         case '4':
             if (val < 0 || val >= 16)
                 throw new ParseException(AsmCodes.IF_ARG_RANGE, arg + "shift left by "
                     + val + "; instruction: " + inst.toString());
             return;
         case '5':
             if (val < -16 || val >= 16)
                 throw new ParseException(AsmCodes.IF_ARG_RANGE, arg
                     + "immediate parameter " + val + "; instruction: " + inst.toString());
             return;
         case '6':
             if (val < 0 || val >= 64)
                 throw new ParseException(AsmCodes.IF_ARG_RANGE, arg + "index6 parameter "
                     + (val & 0xFFFF) + "; instruction: " + inst.toString());
             return;
         case '8':
             if (val < 0 || val >= 256)
                 throw new ParseException(AsmCodes.IF_ARG_RANGE, arg + "trap vector "
                     + Utilities.sShortToHex(val) + "; instruction: " + inst.toString());
             return;
         case '9':
             return;
         }
         // should never happen
         throw new RuntimeException("bad signature character");
     }
 
     public static class IFRecord {
         public String  name;
         public String  signature;
         public boolean special;
         public short[] template;
         public int[][] replacements; // set of tuples (arg, word, start, length)
     }
 }
