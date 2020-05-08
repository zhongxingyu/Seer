 package assemblernator;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * The Assembler class parses a file into a Module object.
  * @author Noah
  * @Date Apr 5, 2012 7:48:36PM
  *
  */
 public class Assembler {
 	/** Map of opId's to static Instructions*/
	private Map<String, Instruction> instructions = new HashMap<String, Instruction>();
 	/** Map of opCodes to static Instructions*/ 
	private Map<Integer, Instruction> byteCodes = new HashMap<Integer, Instruction>();
 	/** version of Assembler?*/
	private int version = 1;
 	
 	/**
 	 * Parses a file into a Module.
 	 * @author Noah
 	 * @date Apr 5, 2012; 7:33:45 PM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param file source code for module.
 	 * @return <pre>
 	 * {@code let line = a line of characters in a file.
 	 * let instr = sub-string of characters before a ';' character in a line.
 	 * Instruction i = Instruction.parse(instr);
 	 * startOp = first line of file = name * "KICKO" * "FC:" * address,
 	 * 	where name is a string of characters representing the name of the module, 
 	 * 		and address is a string of characters representing a memory address.
 	 * module = sub-string from startOp to keyword "END".
 	 * returns Module m, where for all lines in file,
 	 * 	m.assembly = sequence of i;
 	 * 	m.symbols = Map of (i.label, i);
 	 * 	m.programName = name;
 	 * 	m.startAddr = number of modules from start of file.
 	 *  m.moduleLength = length of substring from programName to keyword "End" in file.
 	 *  m.execStart = address;}
 	 *  </pre>
 	 * @specRef N/A
 	 */
 	public Module parseFile(File file) {
 		
 		return new Module();
 	}
 	
 }
