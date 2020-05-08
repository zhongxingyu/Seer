 package edu.osu.cse.mmxi.asm.line;
 
 import edu.osu.cse.mmxi.asm.Literal;
 import edu.osu.cse.mmxi.asm.symb.ArithmeticParser;
 import edu.osu.cse.mmxi.asm.symb.SymbolExpression;
 import edu.osu.cse.mmxi.asm.symb.SymbolExpression.NumExp;
 import edu.osu.cse.mmxi.common.Utilities;
 import edu.osu.cse.mmxi.common.error.ParseException;
 
 /**
  * Parses an assembly line into its opcode and arguments.
  * 
  */
 public class InstructionLine {
     public String     opcode;
     public Argument[] args;
 
     /**
      * Constructor for the instruction line. Will setup opcode and arguments from a string
      * array.
      * 
      * @param line
      *            The parsed string broken in to [0]=label, [1] = opcode, [2+] = arguments
      * @throws ParseException
      */
     public InstructionLine(final String[] line) throws ParseException {
         opcode = line[1];
         args = new Argument[line.length - 2];
         for (int i = 0; i < args.length; i++) {
             final String arg = line[i + 2];
             if (arg.matches("[rR][0-7]"))
                 args[i] = new RegisterArg((short) (arg.charAt(1) - '0'));
             else if (arg.matches("\".*\""))
                 args[i] = new StringArg(arg.substring(1, arg.length() - 1));
             else {
                 String num = arg;
                 final boolean isLiteral = num.charAt(0) == '=';
                 if (isLiteral)
                     num = num.substring(1);
                 if (num.charAt(0) == '#')
                     num = num.substring(1);
                 final Short v = Utilities.parseShort(num);
                 if (v == null)
                     try {
                         args[i] = new ExpressionArg(arg);
                     } catch (final ParseException e) {
                         e.getError().setMessage(
                             "on argument " + (i + 1) + ": " + e.getMessage());
                         throw e;
                     }
                 else if (isLiteral)
                     args[i] = new LiteralArg(v);
                 else
                     args[i] = new ImmediateArg(v);
             }
         }
     }
 
     /**
      * Output a string representation of the instruction
      */
     @Override
     public String toString() {
         String rtn = "";
         for (final Argument arg : args) {
             String tmp = arg.toString();
             tmp = tmp == null ? "null" : tmp;
             rtn += tmp + ",";
         }
         rtn = rtn.substring(0, rtn.length() - 2);
 
         return opcode + ' ' + rtn.toString();
     }
 
     /**
      * Static interface for Argument.
      */
     public static interface Argument {
         /**
          * A method intended to be overwritten in the extension process. Returns 0 on if
          * the argument is not a register.
          * 
          * @return An integer value
          */
         public int isReg();
     }
 
     /**
      * Implementation of Argument Static Class
      */
     public static class StringArg implements Argument {
         public String arg;
 
         /**
          * Constructor for copying argument string rep into arg.
          * 
          * @param s
          *            The string representation of the argument
          */
         public StringArg(final String s) {
             arg = s;
         }
 
         /**
          * A method intended to be overwritten in the extension process. Returns 0 on if
          * the argument is not a register.
          * 
          * @return Integer value, 0 if this is not a register, 1 if it is a register, 2
          *         for expression, 0 for literal, 0 for immediate
          */
         @Override
         public int isReg() {
             return 0;
         }
 
         /**
          * Return the string representation of the argument.
          */
         @Override
         public String toString() {
             return "\"" + arg + "\"";
         }
     }
 
     /**
      * Implementation of Argument for register arguments.
      * 
      */
     public static class RegisterArg implements Argument {
         public short reg;
 
         /**
          * Constructor for saving binary representation of the register. For example 001
          * would be for R1.
          * 
          * @param r
          */
         public RegisterArg(final short r) {
             reg = r;
         }
 
         /**
          * Return what type of argument this is. 1 => register.
          */
         @Override
         public int isReg() {
             return 1;
         }
 
         /**
          * Return a string representation of the binary representation of the register.
          */
         @Override
         public String toString() {
             return "R" + reg;
         }
     }
 
     /**
      * An expression argument implamentation. The assebler accepts expression as
      * arguments. So for example, you can use addition and subtraction as an argument. So
      * mylabel+x5 would give me the "mylabel" value plus a 5 offset.
      * 
      */
     public static class ExpressionArg implements Argument {
         public SymbolExpression val;
 
         /**
          * Constructor. Will use ArithmeticParser for parsing the expression when given a
          * string input into a SymbolExpression.
          * 
          * @param exp
          * @throws ParseException
          */
         public ExpressionArg(final String exp) throws ParseException {
             this(ArithmeticParser.parse(exp));
         }
 
         /**
          * Constructor. Copies a reference of the SymbolExpression into this.
          * 
          * @param exp
          */
         public ExpressionArg(final SymbolExpression exp) {
             val = exp;
         }
 
         /**
          * Returns what type of argument this is. Value of 2 for expression.
          */
         @Override
         public int isReg() {
             return 2;
         }
 
         /**
          * Return the string representation of the SymbolExpression.
          */
         @Override
         public String toString() {
             return val.toString();
         }
     }
 
     /**
      * This class extends the LiteralArg . Converts the short value into a Symbol
      * expression before passing the SymbolExpression up to the ExpressionArg constructor.
      * 
      */
     public static class LiteralArg extends ExpressionArg {
         /**
          * Constructor. Converts the short value for the literal into an symbolExpression
          * and passes to the super class.
          * 
          * @param v
          *            The short representation of a literal value
          */
         public LiteralArg(final short v) {
             super(Literal.getLiteral(v));
         }
 
         /**
          * Override the isReg for super class and return 0.
          */
         @Override
         public int isReg() {
             return 0;
         }
     }
 
     /**
      * Immediate implementation of the ExpressionArg.
      */
     public static class ImmediateArg extends ExpressionArg {
         /**
          * Constructor. COnvert the short immediate rep into a symbolExpression using
          * static NumExp.
          * 
          * @param v
          */
         public ImmediateArg(final short v) {
             super(new NumExp(v));
         }
 
         /**
          * Override the isReg method for super class and always return 0.
          */
         @Override
         public int isReg() {
             return 0;
         }
     }
 }
