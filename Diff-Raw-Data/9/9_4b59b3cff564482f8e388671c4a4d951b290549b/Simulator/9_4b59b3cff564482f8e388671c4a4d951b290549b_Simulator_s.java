 package edu.osu.cse.mmxi;
 
 import java.io.IOException;
 
 import edu.osu.cse.mmxi.loader.SimpleLoader;
 import edu.osu.cse.mmxi.loader.parser.ParseException;
 import edu.osu.cse.mmxi.machine.Machine;
 import edu.osu.cse.mmxi.machine.memory.MemoryUtilities;
 import edu.osu.cse.mmxi.ui.UI;
 import edu.osu.cse.mmxi.ui.UI.UIMode;
 
 /** TODO: Write decent high level description of the Simulator as the main driver. */
 public final class Simulator {
 
     private static int       MAX_CLOCK_COUNT = 10000;
     private static final int FILL            = 0xED6E;
 
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
 
         int memTrack = -1;
         clockloop: while (!m.hasHalted()) {
             if (m.ui.getMode() == UIMode.TRACE) {
                 if (m.clockCount() % 20 == 1) {
                     m.ui.print(" PC");
                     for (int i = 0; i < 8; i++)
                         m.ui.print("   R" + i);
                     m.ui.print("  nzp inst\n");
                 }
 
                 m.ui.print(MemoryUtilities.uShortToHex(m.getPCRegister().getValue())
                     + " ");
                 for (int i = 0; i < 8; i++)
                     m.ui.print(MemoryUtilities.uShortToHex(m.getRegister(i).getValue())
                         + " ");
                 m.ui.print((m.getFlags().getN() ? "n" : "-")
                     + (m.getFlags().getZ() ? "z" : "-")
                     + (m.getFlags().getP() ? "p" : "-") + " ");
 
                 m.ui.print(MemoryUtilities.uShortToHex(m.getMemory(m.getPCRegister()
                     .getValue())) + " ");
                 m.ui.print(m.alu.readInstructionAt(m.getPCRegister().getValue()) + "\n");
             } else if (m.ui.getMode() == UIMode.STEP) {
                 short mem;
                 if (memTrack == -1)
                     mem = (short) ((m.getPCRegister().getValue() & 0xfff8) - 8);
                 else
                     mem = (short) memTrack;
                 m.ui.print("\n                           ");
                 for (int j = 0; j < 8; j++)
                     m.ui.print(" --" + (mem + j & 7) + "-");
                 m.ui.print("\n");
                 for (int i = 0; i < 4; i++) {
                     m.ui.print("R" + 2 * i + ": ");
                     m.ui.print(MemoryUtilities.uShortToHex(m.getRegister(2 * i)
                         .getValue()) + "  ");
                     m.ui.print("R" + (2 * i + 1) + ": ");
                     m.ui.print(MemoryUtilities.uShortToHex(m.getRegister(2 * i + 1)
                         .getValue()) + "   ");
                     m.ui.print(MemoryUtilities.uShortToHex((short) (mem + 8 * i)) + " | ");
                     for (int j = 0; j < 8; j++)
                         m.ui.print(MemoryUtilities.uShortToHex(m.getMemory((short) (mem
                             + 8 * i + j)))
                             + " ");
                     m.ui.print("\n");
                 }
                 m.ui.print("\n          PC: "
                     + MemoryUtilities.uShortToHex(m.getPCRegister().getValue()) + "  ");
                 m.ui.print((m.getFlags().getN() ? "n" : "-")
                     + (m.getFlags().getZ() ? "z" : "-")
                     + (m.getFlags().getP() ? "p" : "-") + "  ");
                 m.ui.print(MemoryUtilities.uShortToHex(m.getMemory(m.getPCRegister()
                     .getValue())) + ": ");
                 m.ui.print(m.alu.readInstructionAt(m.getPCRegister().getValue()) + "\n\n");
                 while (true) {
                     String s = m.ui.prompt("Press ENTER to step, "
                         + "or a hex address or 'pc' to track memory:\n> ");
                     if (s.length() != 0) {
                         memTrack = -2;
                         while (true) {
                             try {
                                 if (s.equalsIgnoreCase("pc"))
                                     memTrack = -1;
                                 else {
                                     memTrack = Integer.parseInt(s, 16);
                                     if ((memTrack & 0xffff0000) != 0)
                                         memTrack = -2;
                                 }
                             } catch (final NumberFormatException e) {
                             }
                             if (memTrack == -2)
                                 s = m.ui.prompt("Invalid hex or "
                                     + "number out of range.\n> ");
                             else
                                 break;
                         }
                         if (memTrack == -1)
                             mem = (short) ((m.getPCRegister().getValue() & 0xfff8) - 8);
                         else
                             mem = (short) memTrack;
                         m.ui.print("\n      ");
                         for (int j = 0; j < 16; j++)
                             m.ui.print(" --"
                                 + Integer.toHexString(mem + j & 15).toUpperCase() + "-");
                         m.ui.print("\n");
                         for (int i = 0; i < 8; i++) {
                             m.ui.print(MemoryUtilities
                                 .uShortToHex((short) (mem + 16 * i)) + " | ");
                             for (int j = 0; j < 16; j++)
                                 m.ui.print(MemoryUtilities.uShortToHex(m
                                     .getMemory((short) (mem + 16 * i + j))) + " ");
                             m.ui.print("\n");
                         }
                         m.ui.print("\n");
                     } else
                         break;
                 }
             }
 
             if (m.clockCount() > MAX_CLOCK_COUNT && m.ui.getMode() != UIMode.STEP) {
                 String ans = m.ui.prompt(
                     "Clock limit " + MAX_CLOCK_COUNT + " reached. Continue? ")
                     .toLowerCase();
                 while (true)
                     if (ans.equals("y") || ans.equals("yes")) {
                         MAX_CLOCK_COUNT *= 2;
                         break;
                     } else if (ans.equals("") || ans.equals("n") || ans.equals("no"))
                         break clockloop;
                     else
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
         boolean clockMode = false, error = false;
         String file = null;
         words: for (int i = 0; i < args.length; i++) {
             String word = args[i];
             if (clockMode) {
                 clockMode = false;
                 try {
                     if (word.length() > 2
                         && word.substring(0, 2).toLowerCase().equals("0x"))
                         MAX_CLOCK_COUNT = Integer.parseInt(word.substring(2), 16);
                     else
                         MAX_CLOCK_COUNT = Integer.parseInt(word);
                 } catch (final NumberFormatException e) {
                     error = true;
                     m.ui.warn("--max-clock-count argument "
                         + "in invalid format; ignoring...");
                 }
             } else if (word.length() > 1 && word.charAt(0) == '-')
                 if (word.length() > 2 && word.charAt(1) == '-') {
                     word = word.substring(2);
                     if (word.equals("max-clock-count"))
                         clockMode = true;
                     else if (word.equals("quiet"))
                         error |= setMode(m.ui, UIMode.QUIET);
                     else if (word.equals("trace"))
                         error |= setMode(m.ui, UIMode.TRACE);
                     else if (word.equals("step"))
                         error |= setMode(m.ui, UIMode.STEP);
                     else if (word.equals("zero"))
                         error |= setFill(m, 0);
                     else if (word.equals("fill"))
                         error |= setFill(m, FILL);
                     else if (word.equals("rand"))
                         error |= setFill(m, -1);
                     else {
                         error = true;
                         m.ui.warn("Unknown command --" + word + "; ignoring...");
                     }
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
                             error |= setMode(m.ui, UIMode.QUIET);
                             break;
                         case 't':
                             error |= setMode(m.ui, UIMode.TRACE);
                             break;
                         case 's':
                             error |= setMode(m.ui, UIMode.STEP);
                             break;
                         case 'z':
                             error |= setFill(m, 0);
                             break;
                         case 'f':
                             error |= setFill(m, FILL);
                             break;
                         case 'r':
                             error |= setFill(m, -1);
                             break;
                         }
             else if (file == null)
                 file = word;
             else {
                 error = true;
                 m.ui.warn("More than one file given; ignoring \"" + word + "\"...");
             }
         }
         if (file == null) {
             error = true;
             m.ui.warn("No files given!");
         }
         if (error) {
             m.ui.warn("Proper syntax:");
             m.ui.warn("java Simulator [-c num|--max-clock-ticks num]");
             m.ui.warn("               [-s|-t|-q|--step|--trace|--quiet]");
             m.ui.warn("               [-z|-f|-r|--zero|--fill|--rand]");
             m.ui.warn("               file.txt");
         }
         if (file == null)
             System.exit(1);
         if (m.ui.getMode() == null)
             m.ui.setMode(UIMode.QUIET);
        if (MAX_CLOCK_COUNT < -1) // // // // // // Using this value means that the
             MAX_CLOCK_COUNT = Integer.MAX_VALUE; // clockCount() <= MAX comparison above
                                                  // will always be true due to overflow
         return file;
     }
 
     private static boolean setMode(final UI ui, final UIMode mode) {
         boolean error = false;
         if (!ui.setMode(mode)) {
             error = true;
             ui.warn("More than one mode setting found. Setting " + mode + " mode...");
         }
         return error;
     }
 
     private static boolean setFill(final Machine m, final int fill) {
         // KNOWN BUG: No way to track or detect multiple conflicting settings with this
         // design, so a -fzfrrfrzfr option will cause long loading times and cause no
         // warnings.
         m.reset(fill);
         return false;
     }
 
     public static void main(final String[] args) {
 
         final Machine machine = new Machine();
         final String file = processArgs(args, machine);
 
         try {
 
             SimpleLoader.load(file, machine);
 
         } catch (final ParseException e) {
             machine.ui.error(e.getMessage());
         } catch (final IOException e) {
             machine.ui.error("I/O Error: " + e.getMessage());
         }
 
         startClockLoop(machine);
     }
 }
