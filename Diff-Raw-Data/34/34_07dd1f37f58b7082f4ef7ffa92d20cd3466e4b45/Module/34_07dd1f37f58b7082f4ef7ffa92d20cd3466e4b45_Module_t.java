 package assemblernator;
 
 import static assemblernator.ErrorReporting.makeError;
 import instructions.UIG_Equated;
import instructions.USI_EXT;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import assemblernator.ErrorReporting.ErrorHandler;
 import assemblernator.ErrorReporting.URBANSyntaxException;
 import assemblernator.Instruction.Operand;
 import assemblernator.Instruction.Operand.ModRecord;
 import assemblernator.Instruction.Operand.ModRecord.Adjustment;
 import assemblernator.Instruction.Usage;
 import assemblernator.Module.Value.ARECLevel;
 
 /**
  * A class representing an assembled urban module.
  * 
  * <pre>
  * {@code assembly = a sequence of Instructions.
  * symbolTable is a Module.SymbolTable
  * startAddr is the integer address at which the module starts.
  * moduleLength is the integer length of the module.}
  * </pre>
  * 
  * Module = (assembly, symbolTable, startAddr, moduleLength).
  * 
  * @author Josh Ventura
  * @date Apr 5, 2012; 7:15:44 PM
  */
 public class Module {
 	/**
 	 * A record representing a object file record.
 	 * 
 	 * @author Noah
 	 * @date May 7, 2012; 12:50:55 AM
 	 */
 	public static class RecordSet {
 		/** the records as an array of bytes. */
 		public byte[] records;
 		/** number of records. */
 		public int size;
 
 		/**
 		 * @param b
 		 *            The bytes of all contained records.
 		 * @param s
 		 *            The number of records enclosed.
 		 */
 		public RecordSet(byte[] b, int s) {
 			records = b;
 			size = s;
 		}
 	}
 
 	/**
 	 * <pre>
 	 * symbolTable is a sequence of entry's where an entry = (label, address, usage, string);
 	 * 	for an instruction with the label, 
 	 * 	the address is the location of the instruction,
 	 * 	the usage is how the instruction is used,
 	 * 	and the string is the string of characters that the label is equated to if 
 	 * 	the opcode of the instruction = EQU or EQUe.
 	 * SymbolTable.symbols union SymbolTable.extEntSymbols = SymbolTable.
 	 * </pre>
 	 */
 	public static class SymbolTable implements
 			Iterable<Map.Entry<String, Instruction>> {
 		/**
 		 * Compares two Map.Entry's.
 		 * 
 		 * @author Noah
 		 * @date Apr 9, 2012; 3:38:54 AM
 		 */
 		private final class MapEntryComparator implements
 				Comparator<Map.Entry<String, Instruction>> {
 			/** @see java.util.Comparator#compare(Object, Object) */
 			@Override public int compare(Map.Entry<String, Instruction> o1,
 					Map.Entry<String, Instruction> o2) {
 				return String.CASE_INSENSITIVE_ORDER.compare(o1.getKey(),
 						o2.getKey()); // same ordering as values.
 			}
 		}
 
 		/**
 		 * let (label, Instruction) = p.
 		 * symbols is a sorted Map of p's.
 		 * Each p is a single entry in SymbolTable, where
 		 * label = Instruction.label,
 		 * address = Instruction.lc,
 		 * usage = Instruction.usage,
 		 * and string = the value of the operand in Instruction.
 		 * 
 		 * does not include Instructions extEntInstr, where instr.opID = "EXT"
 		 * or "ENT".
 		 */
 		private SortedMap<String, Instruction> symbols = new TreeMap<String, Instruction>(
 				String.CASE_INSENSITIVE_ORDER);
 
 		/**
 		 * let (label, Instruction) = p.
 		 * symbols is a sorted Map of p's.
 		 * Each p is a single entry in SymbolTable, where
 		 * label = Instruction.label,
 		 * address = Instruction.lc,
 		 * usage = Instruction.usage,
 		 * and string = the value of the operand in Instruction.
 		 * 
 		 * only includes Instructions extEntInstr, where instr.opID = "EXT" or
 		 * "ENT".
 		 */
 		private Map<String, Instruction> extEntSymbols = new TreeMap<String, Instruction>();
 
 		/**
 		 * adds an entry into the symbol table.
 		 * 
 		 * @author Noah
 		 * @date Apr 6, 2012; 1:56:43 PM
 		 * @modified Apr 18, 2012 PM: Added checks for duplicate symbols. -Noah
 		 * @tested UNTESTED
 		 * @errors NO ERRORS REPORTED
 		 * @codingStandards Awaiting signature
 		 * @testingStandards Awaiting signature
 		 * @param instr
 		 *            instruction to add.
 		 * @param hErr
 		 *            handles errors.
 		 * @specRef N/A
 		 */
 		public void addEntry(Instruction instr, ErrorHandler hErr) {
 			// keep track of instructions w/ opID "ENT" and "EXT" separately.
 			if (instr.getOpId().equalsIgnoreCase("ENT")
 					|| instr.getOpId().equalsIgnoreCase("EXT")) {
 				// put each operand as a separate entry into the symbol table.
 				for (int i = 0; i < instr.countOperand("LR"); i++) {
 					String lbl = instr.getOperand("LR", i);
 					if (instr.usage == Usage.EXTERNAL
 							&& symbols.containsKey(lbl)) { // EXT label and is
 															// already in local
						hErr.reportError(makeError("shadowLabel", lbl, Integer.toString(symbols.get(lbl).lineNum)), instr.lineNum, -1);
 						// don't add.
					} else { // add.
						//add separate instance of instruction for each operand.
						Instruction ext = USI_EXT.getInstance().getNewInstance();
						ext.usage = instr.usage;
						ext.lc = 0;
						extEntSymbols.put(instr.getOperand("LR", i), ext);
 					}
 				}
 			}
 			else {
 				if (extEntSymbols.containsKey(instr.label)
 						&& extEntSymbols.get(instr.label).usage == Usage.EXTERNAL) {
 					// remove ext label from symbol table.
 					Instruction ext = extEntSymbols.remove(instr.label);
					hErr.reportError(makeError("shadowLabel", instr.label, Integer.toString(ext.lineNum)),
 							instr.lineNum, -1);
 					// put local label in symbol table.
 					symbols.put(instr.label, instr);
 				}
 				// no duplicates allowed.
 				else if (!symbols.containsKey(instr.label)) {
 					symbols.put(instr.label, instr);
 				}
 				else {
 					hErr.reportError(
 							makeError("duplicateSymbol", instr.label, Integer
 									.toString(symbols.get(instr.label).lineNum)),
 							instr.lineNum, -1);
 				}
 			}
 
 		}
 
 		/**
 		 * Returns a reference to the Instruction w/ the label given
 		 * from the symbol table.
 		 * Assumes label is in symbol table.
 		 * 
 		 * @author Noah
 		 * @date Apr 7, 2012; 10:22:52 PM
 		 * @modified UNMODIFIED
 		 * @tested UNTESTED
 		 * @errors NO ERRORS REPORTED
 		 * @codingStandards Awaiting signature
 		 * @testingStandards Awaiting signature
 		 * @param label
 		 *            label of Instruction to get reference to from symbol <br>
 		 *            table.
 		 * @return Instruction with label, label from symbol table.
 		 * @specRef N/A
 		 */
 		public Instruction getEntry(String label) {
 			Instruction entry;
 
 			if (symbols.containsKey(label)) {
 				entry = symbols.get(label);
 			}
 			else {
 				entry = extEntSymbols.get(label);
 			}
 			return entry;
 		}
 
 		/**
 		 * Get an ext or ent label.
 		 * 
 		 * @author Noah
 		 * @date Apr 17, 2012; 12:48:36 AM
 		 * @modified UNMODIFIED
 		 * @tested UNTESTED
 		 * @errors NO ERRORS REPORTED
 		 * @codingStandards Awaiting signature
 		 * @testingStandards Awaiting signature
 		 * @param label
 		 *            label of entry to get.
 		 * @return for entry: (label, Instruction), return Instruction.
 		 * @specRef N/A
 		 */
 		public Instruction getEntExtEntry(String label) {
 			return extEntSymbols.get(label);
 		}
 
 		/**
 		 * Checks whether an entry with the label given exists locally.
 		 * 
 		 * @author Noah
 		 * @date Apr 17, 2012; 1:22:33 AM
 		 * @modified UNMODIFIED
 		 * @tested UNTESTED
 		 * @errors NO ERRORS REPORTED
 		 * @codingStandards Awaiting signature
 		 * @testingStandards Awaiting signature
 		 * @param label
 		 *            label of entry to look up.
 		 * @return if entry with label label exists in symbols, return true,
 		 *         else
 		 *         return false.
 		 * @specRef N/A
 		 */
 		public boolean hasLocalEntry(String label) {
 			return symbols.containsKey(label);
 		}
 
 		/**
 		 * Returns all linking records from symbol table.
 		 * 
 		 * @author Noah
 		 * @param progName
 		 *            program name.
 		 * @date May 4, 2012; 9:00:19 PM
 		 * @modified UNMODIFIED
 		 * @tested UNTESTED
 		 * @errors NO ERRORS REPORTED
 		 * @codingStandards Awaiting signature
 		 * @testingStandards Awaiting signature
 		 * @return a two dimensional array of bytes. Each row of the array
 		 *         corresponds to one linking record.
 		 * @specRef OB2
 		 * @specRef OB2.1
 		 * @specRef OB2.2
 		 * @specRef OB2.3
 		 * @specRef OB2.4
 		 * @specRef OB2.5
 		 * @specRef OB2.6
 		 * @specRef OB2.7
 		 */
 		public RecordSet getLinkRecords(String progName) {
 			ByteArrayOutputStream records = new ByteArrayOutputStream();
 			int recordCnt = 0;
 			try {
 				for (Map.Entry<String, Instruction> entry : this.extEntSymbols
 						.entrySet()) {
 					// if the entry is an ENT entry.
 					if (entry.getValue().getOpId().equalsIgnoreCase("ENT")) {
 						records.write((byte) 'L'); // OB2.1
 						records.write((byte) ':'); // OB2.2
 						// add label
 						records.write(entry.getKey().getBytes()); // OB2.3
 
 						records.write((byte) ':'); // OB2.4
 						// address
 						records.write(IOFormat.formatIntegerWithRadix(
 								entry.getValue().lc, 16, 4)); // OB2.5
 
 						records.write((byte) ':'); // OB2.6
 						// program name
 						records.write(progName.getBytes()); // OB2.7
 						records.write((byte) ':');
 
 						recordCnt++;
 					}
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 
 				return new RecordSet(
 						":Something wicked has happened:".getBytes(), recordCnt);
 			}
 
 			return new RecordSet(records.toByteArray(), recordCnt);
 		}
 
 
 		/**
 		 * provides an Iterator over the elements of the symbol table.
 		 * 
 		 * @author Noah
 		 * @date Apr 7, 2012; 4:19:44 PM
 		 * @modified UNMODIFIED
 		 * @tested UNTESTED
 		 * @errors NO ERRORS REPORTED
 		 * @codingStandards Awaiting signature
 		 * @testingStandards Awaiting signature
 		 * @return an Iterator over elements of symbol table.
 		 * @specRef N/A
 		 */
 		@Override public Iterator<Map.Entry<String, Instruction>> iterator() {
 			List<Map.Entry<String, Instruction>> combinedSymbols = new ArrayList<Map.Entry<String, Instruction>>();
 			combinedSymbols.addAll(symbols.entrySet()); // combine
 			combinedSymbols.addAll(extEntSymbols.entrySet()); // combine
 
 			return combinedSymbols.iterator();
 		}
 
 		/**
 		 * Returns a String table representation of the symbol table.
 		 * 
 		 * @author Noah
 		 * @date Apr 15, 2012; 1:19:32 PM
 		 * @modified UNMODIFIED
 		 * @tested UNTESTED
 		 * @errors NO ERRORS REPORTED
 		 * @codingStandards Awaiting signature
 		 * @testingStandards Awaiting signature
 		 * @return as table of symbol table entries, where each entry is
 		 *         represented as a string.
 		 *         The first row of the table = ["Label", "LC", "Usage",
 		 *         "Equate".
 		 * @specRef N/A
 		 */
 		public String[][] toStringTable() {
 			List<Map.Entry<String, Instruction>> combinedSymbols = new ArrayList<Map.Entry<String, Instruction>>();
 			combinedSymbols.addAll(symbols.entrySet()); // combine
 			combinedSymbols.addAll(extEntSymbols.entrySet()); // combine
 
 			Collections.sort(combinedSymbols, new MapEntryComparator()); // sort
 
 			// iterator over elements of set of label, Instruction pairs.
 			Iterator<Entry<String, Instruction>> tableIt = combinedSymbols
 					.iterator();
 
 			// all entries + 1 header entry.
 			String[][] stringTable = new String[combinedSymbols.size() + 1][4];
 
 			int x = 0;
 			stringTable[x][0] = "Label";
 			stringTable[x][1] = "LC";
 			stringTable[x][2] = "Usage";
 			stringTable[x][3] = "Equate";
 
 			while (tableIt.hasNext()) {
 				x++;
 
 				// gets the set values <K,V> stored into a map entry which can
 				// be used to get the values/key of K and V
 				Map.Entry<String, Instruction> entry = tableIt.next();
 
 				String label = entry.getKey();
 				Instruction instr = entry.getValue();
 				String addr = IOFormat.formatHexInteger(instr.lc, 4);
 				Usage usage = instr.usage;
 
 				stringTable[x][0] = label;
 				stringTable[x][1] = addr;
 				stringTable[x][2] = usage.toString();
 
 				// since equate are the only one with a string in the symbol
 				// table i use this to get the value of that string
 				if (usage == Usage.EQUATE) {
 					// gets iterator over set of operands.
 					Iterator<Operand> operandsIt = instr.operands.iterator();
 
 					if (operandsIt.hasNext()) {
 						stringTable[x][3] = operandsIt.next().expression;
 					}
 				}
 			}
 
 			return stringTable;
 		}
 
 		/**
 		 * String representation of the symbol table.
 		 * 
 		 * @author Eric
 		 * @date Apr 6, 2012; 8:58:56 AM
 		 * @modified Apr 9, 11:23:41 AM; combined separate maps into one symbol
 		 *           table, and made a comparator to sort. -Noah <br>
 		 *           Apr 7, 1:26:50 PM; added opcode to lines. - Noah <br>
 		 *           Apr 6, 11:02:08 AM; removed remove() call to prevent
 		 *           destruction of symbol table, also, cleaned code up. -Noah<br>
 		 * @tested UNTESTED
 		 * @errors NO ERRORS REPORTED
 		 * @codingStandards Awaiting signature
 		 * @testingStandards Awaiting signature
 		 * @return <pre>
 		 * {@code let line = a string of character representing a single entry
 		 * 	in the symbol table, concatenated w/ the opcode of the instruction with
 		 * 	the label in hex format, uppercased.  specifically:
 		 * 		label + "\t" + address + "\t" + "\t" + usage + "\t" + string + "\n".
 		 * 	each line is unique.
 		 * returns "Label:\tLC:\tUsage:\tEquString:\n" + a sequence of lines for all the entries in the symbol table.}
 		 * </pre>
 		 * @specRef N/A
 		 */
 		@Override public String toString() {
 
 			List<Map.Entry<String, Instruction>> combinedSymbols = new ArrayList<Map.Entry<String, Instruction>>();
 			combinedSymbols.addAll(symbols.entrySet()); // combine
 			combinedSymbols.addAll(extEntSymbols.entrySet()); // combine
 
 			Collections.sort(combinedSymbols, new MapEntryComparator()); // sort
 
 			// way of storing each line of the symbol table
 			List<String> completeTable = new ArrayList<String>();
 
 			// iterator over elements of set of label, Instruction pairs.
 			Iterator<Entry<String, Instruction>> tableIt = combinedSymbols
 					.iterator();
 
 			String megaTable = "Label:\tLC:\tUsage:\tEquString:\n";
 			// loop runs through the whole symbol table
 			while (tableIt.hasNext()) {
 				// gets the set values <K,V> stored into a map entry which can
 				// be used to get the values/key of K and V
 				Map.Entry<String, Instruction> entry = tableIt.next();
 
 				String label = entry.getKey();
 				Instruction instr = entry.getValue();
 				String addr = IOFormat.formatHexInteger(instr.lc, 4);
 				Usage usage = instr.usage;
 
 				String oneLine = label + "\t " + addr + "\t" + usage;
 
 				// since equate are the only one with a string in the symbol
 				// table i use this to get the value of that string
 				if (usage == Usage.EQUATE) {
 					// gets iterator over set of operands.
 					Iterator<Operand> operandsIt = instr.operands.iterator();
 
 					if (operandsIt.hasNext()) {
 						// only one operand if usage = EQUATE.
 						oneLine = oneLine + "\t" + operandsIt.next().expression;
 					}
 				}
 
 				oneLine = oneLine + "\n";
 
 				completeTable.add(oneLine);
 
 			}
 
 			// makes one big string out of each line of the symbol table
 			while (!completeTable.isEmpty()) {
 				megaTable = megaTable + completeTable.remove(0);
 			}
 
 			return megaTable;
 		}
 
 
 	}
 
 	/** An array of parsed instructions in the order they appeared in the file. **/
 	List<Instruction> assembly = new ArrayList<Instruction>();
 
 	/**
 	 * a symbol table.
 	 */
 	private SymbolTable symbolTable = new SymbolTable();
 
 	/**
 	 * The address at which execution will begin; set from the KICKO instruction
 	 * or by using the AEXS instruction. Must be between 0 and 1023, according
 	 * to specification.
 	 * 
 	 * @specRef OB1.5
 	 */
 	public int execStartAddr;
 	/**
 	 * The address at which program is loaded.
 	 * 
 	 * @specRef OB1.3
 	 */
 	public int loadAddr;
 
 	/**
 	 * The name of the module = label of KICKO directive.
 	 */
 	public String programName;
 	/**
 	 * The length of this module, in instructions, between 0 and 1023, according
 	 * to specification.
 	 * 
 	 * @specRef OB1.3
 	 */
 	private int moduleLength;
 	/** The highest LC contained in this program. */
 	public int highLC = 0;
 	
 	/** The maximum length of module. */
 	private static final int MAX_LEN = 4096;
 
 
 	/**
 	 * Returns a reference to the symbol table.
 	 * 
 	 * @author Noah
 	 * @date Apr 7, 2012; 10:24:23 AM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @return this.symbolTable
 	 * @specRef N/A
 	 */
 	public SymbolTable getSymbolTable() {
 		return symbolTable;
 	}
 
 	/**
 	 * Adds an instruction to module.
 	 * <<<<<<< HEAD
 	 * 
 	 * =======
 	 * moduleLength = moduleLength + instruction.lc.
 	 * >>>>>>> ab0e87755b6c00a0593bc7d7d193e9359c9de48a
 	 * 
 	 * @author Noah
 	 * @date May 6, 2012; 7:14:43 PM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param instr
 	 *            instruction to add to module.
 	 * @param hErr
 	 *            errorhandler that will recieve problems in adding
 	 *            instructions.
 	 * @specRef N/A
 	 */
 	public void addInstruction(Instruction instr, ErrorHandler hErr) {
 		this.moduleLength = this.highLC - this.loadAddr;
 		System.out.println("this.moduleLength = " + this.highLC + " - " + this.loadAddr + ";");
 		if (this.moduleLength > MAX_LEN) {
 			hErr.reportError(makeError("ORmoduleLength", this.programName),
 					instr.lineNum, -1);
 		}
 		this.assembly.add(instr);
 	}
 
 	/**
 	 * Type returned by evaluate().
 	 * 
 	 * @author Josh Ventura
 	 * @date May 4, 2012; 12:00:09 AM
 	 */
 	public static class Value {
 		/** The value gleaned from the expression */
 		public int value;
 		/** The linker AREC flag for the value. */
 		public char arec;
 		/** Any modification records generated for this value. */
 		public ModRecord modRecord;
 
 		/**
 		 * @author Josh Ventura
 		 * @date May 7, 2012; 11:37:33 PM
 		 */
 		public enum ARECLevel {
 			/**
 			 * Only the 'A' flag is allowed when used as a set, or represents
 			 * the 'A' flag in general.
 			 */
 			A(0, 'A'),
 			/** Represents the 'R' flag. */
 			R(1, 'R'),
 			/** Only the 'A' and 'R' flags are allowed. */
 			AR(1),
 			/** Represents the 'E' flag. */
 			E(2, 'E'),
 			/** Flags 'A', 'R', and 'E' are allowed, but not 'C'. */
 			ARE(2),
 			/** Represents the 'C' flag. */
 			C(3, 'C'),
 			/** All AREC flags are permitted. */
 			AREC(3);
 
 			/**
 			 * An integer value for this level which can be compared
 			 * against other levels mathematically.
 			 */
 			int level;
 			/** The character of this flag, if applicable. */
 			char chr;
 
 			/**
 			 * @param lvl
 			 *            The level.
 			 * @param mrecChar
 			 *            The character representation of this flag.
 			 */
 			ARECLevel(int lvl, char mrecChar) {
 				level = lvl;
 				chr = mrecChar;
 			}
 
 			/**
 			 * @param lvl
 			 *            The level.
 			 */
 			private ARECLevel(int lvl) {
 				this(lvl, (char) 0);
 			}
 		}
 
 		/** The location in the instruction of this value, if pertinent. */
 		public enum BitLocation {
 			/** This is the address of a single-address instruction. */
 			Address('S', ARECLevel.R, ARECLevel.AREC, false),
 			/** This is the low-order address of a dual-address instruction. */
 			LowAddress('L', ARECLevel.R, ARECLevel.AREC, false),
 			/** This is the high-order address of a dual-address instruction. */
 			HighAddress('H', ARECLevel.R, ARECLevel.AREC, false),
 			/**
 			 * This is a literal stored in the high-order address of a
 			 * dual-address instruction.
 			 */
 			HighLiteral('H', ARECLevel.A, ARECLevel.AREC, true),
 			/**
 			 * This is a Word-sized literal, given the full Data section,
 			 * Meaning the 16 low-order bits..
 			 */
 			Literal('W', ARECLevel.A, ARECLevel.AREC, true),
 			/** This is an unadjustable type, such as DR, FR, NW, etc. */
 			Other((char) 0, ARECLevel.A, ARECLevel.A, true),
 			/** Similar to `Other`, but allows the result type to be 'R'. */
 			LocalQuery((char) 0, ARECLevel.A, ARECLevel.AR, true);
 
 			/** The location character for this type. */
 			char location;
 			/** The default flag given to this semantic location. */
 			ARECLevel defaultAREC;
 			/** Which AREC flags are allowed for this location. */
 			ARECLevel arecsAllowed;
 			/** Allow negative values with no record generation. */
 			boolean isSigned;
 
 			/**
 			 * @param locChar
 			 *            The location character for this type.
 			 * @param defAREC
 			 *            The default AREC flag for this sematic location.
 			 * @param arecSet
 			 *            Which AREC flags we allow.
 			 * @param signed
 			 *            True if this field has a sign bit.
 			 */
 			BitLocation(char locChar, ARECLevel defAREC, ARECLevel arecSet,
 					boolean signed) {
 				location = locChar;
 				defaultAREC = defAREC;
 				arecsAllowed = arecSet;
 				isSigned = signed;
 			}
 		}
 
 		/** Default constructor; does nothing. */
 		public Value() {}
 
 		/**
 		 * @param v
 		 *            The integer value this instance represents.
 		 * @param flag
 		 *            The AREC flag of this value.
 		 */
 		public Value(int v, char flag) {
 			value = v;
 			arec = flag;
 		}
 	}
 
 	/**
 	 * Evaluates an expression in terms of constants and EQU directives.
 	 * 
 	 * @author Josh, Noah
 	 * @date Apr 8, 2012; 1:34:17 AM
 	 * @modified Apr 8, 2012; 11:44:46 PM: Changed to support all possible
 	 *           operands to EQU. - Josh
 	 *           Apr 12, 2012; 8:50:02 PM: modified for readability. - Noah
 	 *           Apr 13, 2012; 11:23:33 PM: modified for correctness - Noah
 	 *           Apr 13, 2012; 9:34:50 PM: symbolTable.getEntry(exp) can return
 	 *           null, so added i == null check - Noah
 	 * 
 	 *           Apr 17, 2012; 1:59:37 AM: Recoded to support compound
 	 *           expressions. -Josh
 	 * 
 	 *           Apr 18, 2012; 5:35:00 AM: Added check for empty expression.
 	 *           -Josh
 	 * 
 	 *           May 1, 2012; 11:52:22 PM: Fixed issue with expressions
 	 *           containing only an asterisk. -Josh
 	 * 
 	 *           May 4, 2012; 7:34:26 PM: Refactored to be iterative.
 	 * 
 	 *           May 5, 2012; 8:07:08 PM: Fixed issue where it was inferring
 	 *           addition when an operator was missing.
 	 * 
 	 * @tested Apr 17, 2012; 2:33:20 AM: Field tested with five term sums in
 	 *         nested EQUs. Worked provided expression did not contain spaces.
 	 * @errors Reports errors 021-039.
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param exp
 	 *            The expression to be evaluated.
 	 * @param MREF
 	 *            True if this expression is a memory reference. Enabling this
 	 *            parameter enables address referencing for regular labels
 	 *            without warning.
 	 * @param bitLoc
 	 *            The bit location of this parameter, as one of the
 	 *            Value.BitLocation constants.
 	 * @param hErr
 	 *            The error handler which will receive problems in evaluation.
 	 * @param caller
 	 *            The instruction requesting this expression.
 	 * @param pos
 	 *            The position in the given line at which this expression
 	 *            starts.
 	 * @return The value of the expression.
 	 * @specRef N/A
 	 */
 	public Value evaluate(String exp, boolean MREF, Value.BitLocation bitLoc,
 			ErrorHandler hErr, Instruction caller, int pos) {
 		long result = 0;
 		int i = 0;
 		boolean readAnything = false;
 
 		ModRecord mrec = new ModRecord();
 		ArrayList<Adjustment> lrefs = new ArrayList<Adjustment>();
 
 		int sign = 1;
 
 		exploop:
 		for (;; sign = 0) // Iterate the whole expression
 		{
 
 			for (;; ++i) {
 				if (i >= exp.length()) {
 					if (!readAnything)
 						hErr.reportError(makeError("emptyExpr"),
 								caller.lineNum, pos);
 					break exploop;
 				}
 				if (Character.isWhitespace(exp.charAt(i)))
 					continue;
 				if (exp.charAt(i) == '+') {
 					sign = sign == 0 ? 1 : sign;
 					continue;
 				}
 				if (exp.charAt(i) == '-') {
 					sign = sign == 0 ? -1 : -sign;
 					continue;
 				}
 				break;
 			}
 
 			readAnything = true;
 			if (sign == 0) {
 				hErr.reportError(makeError("opratorExp"), caller.lineNum, pos
 						+ i);
 			}
 
 			if (Character.isLetter(exp.charAt(i))) {
 				String lbl;
 				try {
 					lbl = IOFormat.readLabel(exp, i);
 				} catch (URBANSyntaxException e) {
 					hErr.reportError(
 							makeError("compilerError",
 									"Invalid label should have been reported earlier"),
 							caller.lineNum, pos);
 					break;
 				}
 				Instruction instr = symbolTable.getEntry(lbl);
 				if (instr == null)
 					hErr.reportError(
 							makeError((MREF ? "undefLabel" : "undefEqLabel"),
 									lbl), caller.lineNum, pos);
 				else {
 					if (instr.usage == Usage.EQUATE) {
 						UIG_Equated eq = (UIG_Equated) instr;
 						result += sign * eq.value.value;
 						if (eq.value.modRecord != null)
 							for (Adjustment m : eq.value.modRecord.adjustments) {
 								if (m.arec == 'E')
 									mergeInERecord(mrec, sign, lbl);
 								else if (m.arec == 'A')
 									mergeInARecord(lrefs, m.sign, m.label);
 								else
 									hErr.reportError(
 											"COMPILER ERROR: Unexpected flag '"
 													+ m.arec + "' in eqlabel `"
 													+ lbl + "'",
 											caller.lineNum, pos + i);
 							}
 					}
 					else {
 						if (MREF)
 							result += sign * instr.lc;
 						else
 							hErr.reportError(makeError("illegalRefAmp", lbl),
 									caller.lineNum, pos);
 						if (instr.usage == Usage.EXTERNAL) {
 							mergeInERecord(mrec, sign, lbl);
 						}
 						else {
 							mergeInARecord(lrefs, sign, lbl);
 						}
 					}
 				}
 				i += lbl.length();
 			}
 			else if (Character.isDigit(exp.charAt(i))) {
 				final int si = i;
 				while (++i < exp.length() && Character.isDigit(exp.charAt(i)));
 				try {
 					result += sign * Long.parseLong(exp.substring(si, i));
 				} catch (NumberFormatException nfe) {}
 			}
 			else if (exp.charAt(i) == '*') {
 				result += sign * caller.lc;
 				++i;
 			}
 			else
 				hErr.reportError(makeError("unexpSymExp", "" + exp.charAt(i)),
 						caller.lineNum, pos + i);
 		}
 
 		final int exrefs = mrec.adjustments.size();
 		ARECLevel arec = exrefs > 0 ? !lrefs.isEmpty() || exrefs > 1 ? ARECLevel.C
 				: ARECLevel.E
 				: !lrefs.isEmpty() ? ARECLevel.R : bitLoc.defaultAREC;
 
 		if (arec.level > bitLoc.arecsAllowed.level) {
 			hErr.reportError(makeError("arecNotAllowed", "" + arec.chr),
 					caller.lineNum, pos);
 		}
 
 		if (!bitLoc.isSigned) {
 			if (result < 0) {
 				mrec.adjustments.add(0, new Adjustment('-', 'N', "NEGATE"));
 				result *= -1;
 			}
 		}
 
 		if (arec != ARECLevel.R && arec != ARECLevel.A)
 			for (int j = 0; j < lrefs.size(); ++j) {
 				mrec.adjustments.add(lrefs.get(j));
 			}
 
 		if (result > Integer.MAX_VALUE || result < Integer.MIN_VALUE)
 			hErr.reportError(makeError("OORurbanWord", exp), caller.lineNum,
 					pos);
 
 		Value res = new Value((int) result, arec.chr);
 		mrec.address = caller.lc;
 		mrec.addressField = bitLoc.location;
 		res.modRecord = mrec;
 		return res;
 	}
 
 	/**
 	 * @author Josh Ventura
 	 * @date May 8, 2012; 1:11:28 AM
 	 * @param mrec
 	 *            Modification record to which to add the new record.
 	 * @param sign
 	 *            The sign for the new adjustment.
 	 * @param lbl
 	 *            THe label for the new adjustment.
 	 * @specRef N/A
 	 */
 	private void mergeInERecord(ModRecord mrec, int sign, String lbl) {
 		boolean addit = true;
 		for (int j = 0; j < mrec.adjustments.size(); ++j) {
 			final Adjustment mr = mrec.adjustments.get(j);
 			if (mr.label.equals(lbl) && mr.sign == -sign) {
 				addit = false;
 				mrec.adjustments.remove(j--);
 				break;
 			}
 		}
 		if (addit)
 			mrec.adjustments.add(new Adjustment(sign, 'E', lbl));
 	}
 
 	/**
 	 * @author Josh Ventura
 	 * @date May 8, 2012; 1:11:28 AM
 	 * @param lrefs
 	 *            List of local adjustments.
 	 * @param sign
 	 *            The sign for the new adjustment.
 	 * @param lbl
 	 *            THe label for the new adjustment.
 	 * @specRef N/A
 	 */
 	private void mergeInARecord(ArrayList<Adjustment> lrefs, int sign,
 			String lbl) {
 		boolean addit = true;
 		for (int j = 0; j < lrefs.size(); ++j)
 			if (lrefs.get(j).sign == -sign) {
 				addit = false;
 				lrefs.remove(j);
 				break;
 			}
 		if (addit)
 			lrefs.add(new Adjustment(sign, 'R', lbl));
 	}
 
 	/**
 	 * Writes Object File to file with fileName.
 	 * 
 	 * @author Noah
 	 * @date May 7, 2012; 2:07:33 AM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param fileName
 	 *            File to write to.
 	 * @throws IOException
 	 *             When file write goes south.
 	 * @throws Exception
 	 *             In case of general mayhem.
 	 * @throws FileNotFoundException
 	 *             If the file couldn't be opened.
 	 * @specRef N/A
 	 */
 	public void writeObjectFile(String fileName) throws IOException, Exception,
 			FileNotFoundException {
 		OutputStream out = new FileOutputStream(fileName);
 
 		int totalRecords = 0, totalLinkRecords = 0, totalTextRecords = 0, totalModRecords = 0;
 		RecordSet recSet;
 		// write header record.
 		out.write(getHeaderRecord());
 
 		// write linking records.
 		recSet = this.symbolTable.getLinkRecords(this.programName);
 		totalLinkRecords = recSet.size;
 		out.write(recSet.records);
 		// write text records and mod records. adjust record cnts.
 		for (Instruction instr : this.assembly) {
 			RecordSet rs = instr.getTextRecord(this.programName); 
 			out.write(rs.records);
 			totalTextRecords += rs.size;
 
 			recSet = instr.getModRecords(this.programName);
 			out.write(recSet.records);
 			totalModRecords += recSet.size;
 
 		}
 
 		totalRecords = totalLinkRecords + totalTextRecords + totalModRecords
 				+ 2;// includes end record and header record.
 		// write end record
 		out.write(getEndRecord(totalRecords, totalLinkRecords,
 				totalTextRecords, totalModRecords));
 
 	}
 
 	/**
 	 * Writes object file.
 	 * 
 	 * @author Noah
 	 * @date Apr 28, 2012; 6:25:13 PM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param out
 	 *            The output stream of the object file.
 	 * @throws IOException
 	 *             Does not catch IO Exceptions that arise.
 	 * @throws Exception
 	 *             Does not catch general exceptions.
 	 * @specRef N/A
 	 */
 	public void writeObjectFile(OutputStream out) throws IOException, Exception {
 		int totalRecords = 0, totalLinkRecords = 0, totalTextRecords = 0, totalModRecords = 0;
 		RecordSet recSet;
 		// write header record.
 		out.write(getHeaderRecord());
 
 		// write linking records.
 		recSet = this.symbolTable.getLinkRecords(this.programName);
 		totalLinkRecords = recSet.size;
 		out.write(recSet.records);
 		// write text records and mod records. adjust record cnts.
 		for (Instruction instr : this.assembly) {
 			RecordSet rs = instr.getTextRecord(this.programName); 
 			out.write(rs.records);
 			totalTextRecords += rs.size;
 
 			recSet = instr.getModRecords(this.programName);
 			out.write(recSet.records);
 			totalModRecords += recSet.size;
 
 		}
 		totalRecords = totalLinkRecords + totalTextRecords + totalModRecords
 				+ 2;
 		// write end record
 		out.write(getEndRecord(totalRecords, totalLinkRecords,
 				totalTextRecords, totalModRecords));
 
 	}
 
 	/**
 	 * Returns a string representation of Module.
 	 * 
 	 * @author Noah
 	 * @date Apr 8, 2012; 12:26:15 PM: Added output of binary equivalent for
 	 *       operands. Now calls toString() of Instruction.
 	 * @modified Apr 17, output
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @return <pre>
 	 * {@code symbTableStr is the string representation of the symbol table. 
 	 * assemblyStr the string representation of assembly;
 	 * 	The string representation of assembly is a sequence of instrBreaks,
 	 * 		where for an Instruction instr in assembly, the corresponding 
 	 * 			instrBreak = instr.toString() i.e. String representation of Instruction.
 	 * 
 	 * returns "Symbol Table:\n" + symbTableStr + "\nInstruction breakdowns:\n" + assemblyStr;}
 	 * </pre>
 	 * 
 	 * @specRef N/A
 	 */
 	@Override public String toString() {
 		String rep = "Symbol Table:\n" + symbolTable.toString()
 				+ "\nInstruction breakdowns:\n";
 
 		rep = rep + "LC\tObject Code\tAddress Status\tLine Num\tSource Line\n";
 		rep = rep + "(hex)\t(hex)\tsrc:, dest:\t(dec)\n";
 
 		for (Instruction instr : assembly) {
 			rep = rep + instr.toString() + "\n";
 		}
 
 		return rep;
 	}
 
 	/**
 	 * @author Ratul Khosla
 	 * @date May 6, 2012; 7:19:51 PM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @return A one dimensional array of bytes. The row represents the Header
 	 *         Record.
 	 * @specRef OB1
 	 * @specRef OB1.1
 	 * @specRef OB1.2
 	 * @specRef OB1.3
 	 * @specRef OB1.4
 	 * @specRef OB1.5
 	 * @specRef OB1.6
 	 * @specRef OB1.7
 	 * @specRef OB1.8
 	 * @specRef OB1.9
 	 * @specRef OB1.18
 	 * @specRef OB1.19
 	 * @specRef OB1.20
 	 * @specRef OB1.21
 	 */
 	private byte[] getHeaderRecord() {
 		try {
 			ByteArrayOutputStream header = new ByteArrayOutputStream();
 			header.write((byte) 'H');// OB1.1
 			header.write((byte) ':');// OB1.2
 			header.write(programName.getBytes());// OB1.3
 			header.write((byte) ':');// OB1.2
 			header.write(IOFormat.formatIntegerWithRadix(this.loadAddr, 16, 4));// OB1.3
 			header.write((byte) ':');// OB1.2
 			header.write(IOFormat.formatIntegerWithRadix(this.moduleLength, 16, 4));// OB1.3
 			header.write((byte) ':');// OB1.4
 			header.write(IOFormat.formatIntegerWithRadix(this.execStartAddr,
 					16, 4));// OB1.5
 			header.write((byte) ':');// OB1.6
 			header.write(IOFormat.formatDate(new Date()));// OB1.7
 			header.write((byte) ':');// OB1.8
 			header.write(IOFormat.formatIntegerWithRadix(Assembler.VERSION, 16,
 					4));// OB1.9
 			header.write((byte) ':');// OB1.18
 			header.write("URBAN-ASM".getBytes());// OB1.19
 			header.write((byte) ':');// OB1.20
 			header.write(programName.getBytes());// OB1.21
 			header.write((byte) ':');
 			return header.toByteArray();
 		} catch (IOException e) {
 			e.printStackTrace();
 			return ":Error!!!Something wrong has happened:".getBytes();
 		}
 
 	}
 
 	/**
 	 * @author Ratul Khosla
 	 * @date May 6, 2012; 8:03:54 PM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param totRecords
 	 *            The total number of records that are present.
 	 * @param totLinkRecords
 	 *            The total number of Linking Records that are present.
 	 * @param totTextRecords
 	 *            The total number of Text Records that are present.
 	 * @param totModRecords
 	 *            The total number of Modification Records that are present.
 	 * @return
 	 *         A one dimensional array of bytes. The row represents the Ending
 	 *         Record.
 	 * @specRef OB5.1
 	 * @specRef OB5.2
 	 * @specRef OB5.3
 	 * @specRef OB5.4
 	 * @specRef OB5.5
 	 * @specRef OB5.6
 	 * @specRef OB5.7
 	 * @specRef OB5.8
 	 * @specRef OB5.9
 	 * @specRef OB5.10
 	 * @specRef OB5.11
 	 */
 	private byte[] getEndRecord(int totRecords, int totLinkRecords,
 			int totTextRecords, int totModRecords) {
 		try {
 			ByteArrayOutputStream header = new ByteArrayOutputStream();
 			header.write((byte) 'E');// OB5.1
 			header.write((byte) ':');// OB5.2
 			header.write(IOFormat.formatIntegerWithRadix(totRecords, 16, 4));// OB5.3
 			header.write((byte) ':');// OB5.4
 			header.write(IOFormat.formatIntegerWithRadix(totLinkRecords, 16, 4));// OB5.5
 			header.write((byte) ':');// OB5.6
 			header.write(IOFormat.formatIntegerWithRadix(totTextRecords, 16, 4));// OB5.7
 			header.write((byte) ':');// OB5.8
 			header.write(IOFormat.formatIntegerWithRadix(totModRecords, 16, 4));// OB5.9
 			header.write((byte) ':');// OB5.10
 			header.write(programName.getBytes());// OB5.11
 			header.write((byte) ':');
 			return header.toByteArray();
 		} catch (IOException e) {
 			e.printStackTrace();
 			return ":Error!!!Something wrong has happened:".getBytes();
 		}
 	}
 
 }
