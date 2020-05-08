 package assemblernator;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import assemblernator.Instruction.Operand;
 import assemblernator.Instruction.Usage;
 
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
 	 * <pre>
 	 * symbolTable is a sequence of entry's where an entry = (label, address, usage, string);
 	 * 	for an instruction with the label, 
 	 * 	the address is the location of the instruction,
 	 * 	the usage is how the instruction is used,
 	 * 	and the string is the string of characters that the label is equated to if 
 	 * 	the opcode of the instruction = EQU or EQUe.
 	 * </pre>
 	 */
 	public static class SymbolTable implements
 			Iterable<Map.Entry<String, Instruction>> {
 
 		/**
 		 * let (label, Instruction) = p.
 		 * symbols is a sorted Map of p's.
 		 * Each p is a single entry in SymbolTable, where
 		 * label = Instruction.label,
 		 * address = Instruction.lc,
 		 * usage = Instruction.usage,
 		 * and string = the value of the operand in Instruction.
 		 */
 		private SortedMap<String, Instruction> symbols = new TreeMap<String, Instruction>(
 				String.CASE_INSENSITIVE_ORDER);
 
 		/**
 		 * adds an entry into the symbol table.
 		 * 
 		 * @author Noah
 		 * @date Apr 6, 2012; 1:56:43 PM
 		 * @modified UNMODIFIED
 		 * @tested UNTESTED
 		 * @errors NO ERRORS REPORTED
 		 * @codingStandards Awaiting signature
 		 * @testingStandards Awaiting signature
 		 * @param instr
 		 *            instruction to add.
 		 * @specRef N/A
 		 */
 		public void addEntry(Instruction instr) {
 			symbols.put(instr.label, instr);
 
 		}
 
 		/**
 		 * Returns a reference to the Instruction w/ the label given
 		 * from the symbol table.
 		 * 
 		 * @author Noah
 		 * @date Apr 7, 2012; 10:22:52 PM
 		 * @modified UNMODIFIED
 		 * @tested UNTESTED
 		 * @errors NO ERRORS REPORTED
 		 * @codingStandards Awaiting signature
 		 * @testingStandards Awaiting signature
 		 * @param label
 		 *            label of Instruction to get reference to from symbol
 		 *            table.
 		 * @return Instruction with label, label from symbol table.
 		 * @specRef N/A
 		 */
 		public Instruction getEntry(String label) {
 			return symbols.get(label);
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
 			return symbols.entrySet().iterator();
 		}
 
 
 		/**
 		 * String representation of the symbol table.
 		 * 
 		 * @author Eric
 		 * @date Apr 6, 2012; 8:58:56 AM
 		 * @modified Apr 7, 1:26:50 PM; added opcode to lines. - Noah <br>
 		 *           Apr 6, 11:02:08 AM; removed remove() call to prevent
 		 *           destruction of symbol table, <br>
 		 *           also, cleaned code up. -Noah<br>
 		 * @tested UNTESTED
 		 * @errors NO ERRORS REPORTED
 		 * @codingStandards Awaiting signature
 		 * @testingStandards Awaiting signature
 		 * @return <pre>
 		 * {@code let line = a string of character representing a single entry
 		 * 	in the symbol table, concatenated w/ the opcode of the instruction with
 		 * 	the label in hex format, uppercased.  specifically:
 		 * 		label + " " + address + " " + " " + usage + " " + string + "\n".
 		 * 	each line is unique.
 		 * returns a sequence of lines for all the entries in the symbol table.}
 		 * </pre>
 		 * @specRef N/A
 		 */
 		@Override public String toString() {
 			// way of storing each line of the symbol table
 			ArrayList<String> completeTable = new ArrayList<String>();
 
 			// iterator over elements of set of label, Instruction pairs.
 			Iterator<Entry<String, Instruction>> tableIt = symbols.entrySet()
 					.iterator();
 
 			// loop runs through the whole symbol table
 			while (tableIt.hasNext()) {
 				// gets the set values <K,V> stored into a map entry which can
 				// be used to get the values/key of K and V
 				Map.Entry<String, Instruction> entry = tableIt.next();
 
 				String label = entry.getKey();
 				Instruction instr = entry.getValue();
 				int addr = instr.lc;
 				Usage usage = instr.usage;
 
 				String oneLine = label + " " + addr + " " + usage;
 
 				// since equate are the only one with a string in the symbol
 				// table i use this to get the value of that string
 				if (usage == Usage.EQUATE) {
 					// gets iterator over set of operands.
 					Iterator<Operand> operandsIt = instr.operands.iterator();
 
 					if (operandsIt.hasNext()) {
 						// only one operand if usage = EQUATE.
						oneLine = oneLine + operandsIt.next().expression;
 					}
 				}
 
 				oneLine = oneLine + "\n";
 
 				completeTable.add(oneLine);
 
 			}
 
 			String megaTable = "";
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
 	int startAddr;
 
 	/**
 	 * The length of this module, in instructions, between 0 and 1023, according
 	 * to specification.
 	 * 
 	 * @specRef OB1.3
 	 */
 	int moduleLength;
 
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
 	 * Evaluates an expression in terms of constants and EQU directives.
 	 * 
 	 * @author Noah
 	 * @date Apr 8, 2012; 1:34:17 AM
 	 * @modified Apr 8, 2012; 11:44:46 PM: Changed to support all possible
 	 *           operands to EQU.
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param exp
 	 *            The expression to be evaluated.
 	 * @return The value of the expression.
 	 * @specRef N/A
 	 */
 	public int evaluate(String exp) {
 		exp = exp.trim();
 		if (IOFormat.isValidLabel(exp)) {
 			Instruction i = symbolTable.getEntry(exp);
 			String wdwh = i.getOperand("FC");
 			if (wdwh == null)
 				wdwh = i.getOperand("LR");
 			if (wdwh != null)
 				return evaluate(wdwh);
 			wdwh = i.getOperand("FM");
 			if (wdwh != null)
 				return i.lc;
 			throw new NullPointerException("No correct operand to EQU at line " + i.lineNum + "!");
 		}
 		return Integer.parseInt(exp);
 	}
 
 	/**
 	 * Returns the address of the instruction with the label given.
 	 * 
 	 * @author Noah
 	 * @date Apr 8, 2012; 5:32:46 PM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param label
 	 *            the label of the instruction to look up for its lc.
 	 * @return instr.lc where instr.label = label.
 	 * @specRef N/A
 	 */
 	public int getAddress(String label) {
 		return evaluate(label); // symbolTable.getEntry(label).lc;
 	}
 
 	/**
 	 * Returns a string representation of Module.
 	 * 
 	 * @author Noah
 	 * @date Apr 8, 2012; 12:26:15 PM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @return <pre>
 	 * {@code symbTableStr is the string representation of the symbol table. 
 	 * assemblyStr the string representation of assembly;
 	 * 	The string representation of assembly is a sequence of instrBreaks,
 	 * 		where for an Instruction instr in assembly, the corresponding 
 	 * 			instrBreak = origSourceLine + "Line number: " + instr.LineNum + " " + LC: " + lc + " " + "Label: " + label + ",\n"
 	 * 			+ "instruction/Directive: " + instr.getOpID() + " " + Binary Equivalent: " + binEquiv + "\n" 
 	 * 			+ "operand " + i + operandKeyWord + ":" + operandValue;
 	 * 				where if instr does not have a label, then label = "", 
 	 * 					else label = instr.label, and
 	 * 				if instr is a directive and thus has no opcode, then binEquiv = "------", 
 	 * 					else binEquiv = instr.opcode in binary format, and
 	 * 				i represents the ith operand of instr, and
 	 * 					operandKeyword = the key word for the ith operand;
 	 * 					operandValue = the value associated w the operand with operandKeyword keyword for the ith operand, and
 	 * 				origSourceLine = instr.origSrcLine,
 	 * 				and lc = instr.lc displayed in hexadecimal w/ 4 bits.
 	 * 	
 	 * returns "Symbol Table:\n" + symbTableStr + "\nInstruction breakdowns:\n" + assemblyStr;}
 	 * </pre>
 	 * 
 	 * @specRef N/A
 	 */
 	@Override public String toString() {
 		String rep = "Symbol Table:\n" + symbolTable.toString()
 				+ "\nInstruction breakdowns:\n";
 		Iterator<Instruction> assemblyIt = assembly.iterator();
 
 		while (assemblyIt.hasNext()) {
 			Instruction instr = assemblyIt.next();
 
 			rep = rep + "original source line: " + instr.origSrcLine + "\n";
 
 			String binEquiv = IOFormat.formatBinInteger(instr.getOpcode(), 6); // binary
 																				// equivalent
 																				// of
 																				// opcode
 																				// keyword
 																				// i.e.
 																				// opcode.
 			String label = instr.label;
 			String lc = IOFormat.formatHexInteger(instr.lc, 4);
 
 			// instr is a directive and thus has no opcode.
 			if (instr.getOpcode() == 0xFFFFFFFF) {
 				binEquiv = "------"; // so binary equivalent is non-existant.
 			}
 
 			// if the instruction has no label
 			if (instr.label == null) {
 				label = ""; // no label to print.
 				// also, can't print "------" b/c label may be "------".
 			}
 
 
 			String instrBreak = "Line number: " + instr.lineNum + " " + "LC: "
 					+ lc + " " + "Label: " + label + ",\n"
 					+ "instruction/Directive: " + instr.getOpId() + " "
 					+ "Binary Equivalent: " + binEquiv + "\n";
 
 			Iterator<Operand> operandIt = instr.operands.iterator();
 
 			int i = 1;
 			while (operandIt.hasNext()) {
 				Operand oprnd = operandIt.next();
 
 				instrBreak = instrBreak + "Operand " + i + ": " + oprnd.operand
 						+ ":" + oprnd.expression + "\n";
 
 				i++;
 			}
 
 			instrBreak = instrBreak + "\n";
 
 			rep = rep + instrBreak;
 		}
 
 		return rep;
 	}
 }
