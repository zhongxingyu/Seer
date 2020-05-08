 package edu.osu.cse.mmxi.sim;
 
 import java.util.ArrayList;
import java.util.HashMap;
 import java.util.List;
 
 import edu.osu.cse.mmxi.common.Utilities;
 import edu.osu.cse.mmxi.common.error.Error;
 import edu.osu.cse.mmxi.sim.error.SimCodes;
 import edu.osu.cse.mmxi.sim.loader.SimpleLoader;
 import edu.osu.cse.mmxi.sim.machine.Machine;
 import edu.osu.cse.mmxi.sim.ui.SimUI;
 import edu.osu.cse.mmxi.sim.ui.SimUI.UIMode;
 
 public final class Simulator {
     public static int          MAX_CLOCK_COUNT = 10000;
     private static final short FILL            = (short) 0xED6E;
 
     /**
      * <p>
      * The Simulator begins stepping the Machine's clock.
      * </p>
      * 
      * <p>
      * The Simulator controls the Machine's clock by telling it when it is ready to
      * advance to the next instruction and execute it.
      * </p>
      * 
      * <p>
      * If the Machine has halted or run beyond the maximum number of instructions, the
      * clock stops.
      * </p>
      * 
      * <p>
      * During Step or Trace mode, we pass information from the Machine to the UI during
      * each clock step.
      * </p>
      */
     public static void startClockLoop(final Machine m) {
         clockloop: while (!m.hasHalted()) {
             if (m.ui.getMode() == UIMode.TRACE) {
                 if (m.clockCount() % 20 == 1) {
                     m.ui.print(" PC");
                     for (int i = 0; i < 8; i++)
                         m.ui.print("   R" + i);
                     m.ui.print("  nzp inst\n");
                 }
 
                 m.ui.print(Utilities.uShortToHex(m.getPCRegister().getValue()) + " ");
                 for (int i = 0; i < 8; i++)
                     m.ui.print(Utilities.uShortToHex(m.getRegister(i).getValue()) + " ");
                 m.ui.print((m.getFlags().getN() ? "n" : "-")
                     + (m.getFlags().getZ() ? "z" : "-")
                     + (m.getFlags().getP() ? "p" : "-") + " ");
 
                 m.ui.print(Utilities.uShortToHex(m
                     .getMemory(m.getPCRegister().getValue())) + " ");
                 m.ui.print(m.alu.readInstructionAt(m.getPCRegister().getValue()) + "\n");
             }
 
             if (m.clockCount() > MAX_CLOCK_COUNT) {
                 String ans = m.ui.prompt(
                     "Clock limit " + MAX_CLOCK_COUNT + " reached. Continue? ")
                     .toLowerCase();
                 while (true)
                     if ("no".startsWith(ans))
                         break clockloop;
                     else if ("yes".startsWith(ans)) {
                         MAX_CLOCK_COUNT *= 2;
                         break;
                     } else
                         ans = m.ui.prompt("Please answer 'yes' or 'no'. ").toLowerCase();
             }
 
             m.stepClock();
         }
         m.ui.print("Machine halted after " + (m.clockCount() - 1) + " steps.");
     }
 
     /**
      * <p>
      * This function reads the command line arguments passed in, and parses them to
      * extract the file to be processed and any ther optional arguments.
      * </p>
      * 
      * <p>
      * The format of the instruction is:
      * </p>
      * 
      * <pre>
      *    java Simulator [-c<i>num</i>|--max-clock-count <i>num</i>]
      *                   [-s|-t|-q|--step|--trace|--quiet]
      *                   [-z|-f|-r|--zero|--fill|--rand]
      *                   <i>file.txt</i>
      * </pre>
      * 
      * <p>
      * The <code>--max-clock-count</code> argument (short name <code>-c</code>) sets the
      * maximum number of instructions to be executed before quitting (assuming a
      * <code>TRAP HALT</code> command hasn't already been executed). If this limit is
      * reached, a prompt is given to optionally allow continued operation of the program.
      * The default value for <i>num</i> is 10000. If a negative value is given for
      * <i>num</i>, the machine is never halted.
      * </p>
      * 
      * <p>
      * The <code>--step</code>, <code>--trace</code>, and <code>--quiet</code> modes
      * (short names <code>-s</code>, <code>-t</code>, and <code>-q</code>) are mutually
      * exclusive and control the operation of the machine. In quiet mode, all instructions
      * are executed normally and the only output is that driven by the program. In trace
      * mode, every instruction, along with the current register state, is printed as it is
      * executed, for debugging purposes. In step mode, a detailed view of the register
      * states and the current page of memory is printed after every instruction, and the
      * machine stops and waits for a command to continue each time.
      * </p>
      * 
      * <p>
      * The <code>--zero</code>, <code>--fill</code> and <code>--rand</code> flags (short
      * names <code>-z</code>, <code>-f</code>, and <code>-r</code>) are mutually exclusive
      * and control whether to randomize memory, the registers, and the condition codes,
      * fill them with an easily recognizable repeated hex code ('ED6E'), or zero them all.
      * The default behavior is <code>--rand</code>.
      * </p>
      * 
      * <p>
      * Sample valid command line strings:
      * </p>
      * 
      * <pre>
      *    java Simulator prog.txt
      *    java Simulator -zc100000 prog.txt
      *    java Simulator -rs prog.txt -c 100000
      *    java Simulator -f --max-clock-count 100000 prog.txt --step
      * </pre>
      * 
      * @param args
      *            the arguments in the command line
      */
     public static String processArgs(final String[] args, final Machine m) {
         boolean clockMode = false, clockSet = false, fillSet = false;
         String file = null;
 
         final List<Error> errors = new ArrayList<Error>();
 
         words: for (int i = 0; i < args.length; i++) {
             String word = args[i];
             if (clockMode) {
                 clockMode = false;
                 if (clockSet)
                     errors.add(new Error("clock setting '" + word
                         + "' found; ignoring...", SimCodes.UI_MULTI_CLOCK));
                 else
                     try {
                         if (word.length() > 2
                             && word.substring(0, 2).toLowerCase().equals("0x"))
                             MAX_CLOCK_COUNT = Integer.parseInt(word.substring(2), 16);
                         else
                             MAX_CLOCK_COUNT = Integer.parseInt(word);
                         clockSet = true;
                     } catch (final NumberFormatException e) {
                         errors.add(new Error("in invalid format; ignoring...",
                             SimCodes.UI_MAX_CLOCK));
                     }
             } else if (word.length() > 1 && word.charAt(0) == '-') {
                 if (word.length() > 2 && word.charAt(1) == '-') {
                     word = word.substring(2);
                     if (word.equals("max-clock-count"))
                         clockMode = true;
                     else if (word.equals("quiet"))
                         setMode(m.ui, UIMode.QUIET, errors);
                     else if (word.equals("trace"))
                         setMode(m.ui, UIMode.TRACE, errors);
                     else if (word.equals("step"))
                         setMode(m.ui, UIMode.STEP, errors);
                     else if (word.equals("zero"))
                         fillSet = setFill(m, (short) 0, fillSet, errors);
                     else if (word.equals("fill"))
                         fillSet = setFill(m, FILL, fillSet, errors);
                     else if (word.equals("rand"))
                         fillSet = setFill(m, null, fillSet, errors);
                     else
                         errors
                             .add(new Error("command is --" + word, SimCodes.UI_UNKN_CMD));
                 } else
                     for (int j = 1; j < word.length(); j++)
                         switch (word.charAt(j)) {
                         case 'c':
                             clockMode = true;
                             if (j == word.length() - 1)
                                 break;
                             else {
                                 args[i--] = word.substring(j + 1);
                                 continue words;
                             }
                         case 'q':
                             setMode(m.ui, UIMode.QUIET, errors);
                             break;
                         case 't':
                             setMode(m.ui, UIMode.TRACE, errors);
                             break;
                         case 's':
                             setMode(m.ui, UIMode.STEP, errors);
                             break;
                         case 'z':
                             fillSet = setFill(m, (short) 0, fillSet, errors);
                             break;
                         case 'f':
                             fillSet = setFill(m, FILL, fillSet, errors);
                             break;
                         case 'r':
                             fillSet = setFill(m, null, fillSet, errors);
                             break;
                         default:
                             errors.add(new Error("command is -" + word.charAt(j)
                                 + " from " + word, SimCodes.UI_UNKN_CMD));
                         }
             } else if (file == null)
                 file = word;
             else
                 errors.add(new Error("ignoring " + word + ".", SimCodes.UI_MULTI_FILE));
         }
         if (m.ui.getMode() == null)
             m.ui.setMode(file == null ? UIMode.STEP : UIMode.QUIET);
         if (file == null && m.ui.getMode() != UIMode.STEP)
             errors.add(new Error(SimCodes.UI_NO_FILE));
         if (errors.size() != 0) {
             errors.add(new Error("Proper syntax:\n"
                 + "java Simulator [-c num|--max-clock-ticks num]\n"
                 + "               [-s|-t|-q|--step|--trace|--quiet]\n"
                 + "               [-z|-f|-r|--zero|--fill|--rand]\n"
                 + "               file.txt", SimCodes.MSG_SYNTAX));
 
             m.ui.printErrors(errors);
         }
         if (file == null && m.ui.getMode() != UIMode.STEP)
             System.exit(1);
         if (MAX_CLOCK_COUNT < 0) // /// // // // // Using this value means that the
             MAX_CLOCK_COUNT = Integer.MAX_VALUE; // clockCount() <= MAX comparison above
                                                  // will always be true due to overflow
         return file;
     }
 
     private static void setMode(final SimUI ui, final UIMode mode,
         final List<Error> errors) {
         if (!ui.setMode(mode))
             errors.add(new Error(
                 "Overriding old run mode; setting to " + mode + " mode.",
                 SimCodes.UI_MULTI_SETTINGS));
     }
 
     private static boolean setFill(final Machine m, final Short fill,
         final boolean fillSet, final List<Error> errors) {
         m.reset(fill);
         if (fillSet)
             errors.add(new Error("Overriding old fill mode; setting to "
                 + (fill == 0 ? "zero-" : fill == -1 ? "randomized " : "repeat-")
                 + "fill mode.", SimCodes.UI_MULTI_SETTINGS));
         return true;
     }
 
     public static void main(final String[] args) {
 
         final Machine machine = new Machine();
         final String file = processArgs(args, machine);
 
         if (machine.ui.getMode() == UIMode.STEP)
             new Console(machine, file);
         else {
             if (file != null)
                machine.ui.printErrors(SimpleLoader.load(file, machine,
                    new HashMap<Short, Integer>(), new HashMap<String, Short>()));
             startClockLoop(machine);
         }
     }
 }
