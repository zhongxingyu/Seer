 package net.ian.dcpu;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class Assembler {
 	public static final String[] basicOps = { "SET", "ADD", "SUB", "MUL", "DIV", "MOD", "SHL", "SHR", "AND", "BOR", "XOR", "IFE", "IFN", "IFG", "IFB" };
 	public static final String[] specialOps = { "EXIT", "JSR" };
 	public static final String[] registers;
 	public static final String[] special = { "SP", "PC", "O" };
 
 	public ArrayList<Integer> instructions;
 	
 	public Map<String, Integer> labels;
 	public Map<Integer, String> fixes;
 		
 	static {
 		DCPU.Register regs[] = DCPU.Register.values();
 		registers = new String[regs.length];
 		for (DCPU.Register r : regs)
 			registers[r.ordinal()] = r.toString();
 	}
 	
 	private class Argument {
 		public List<Integer> code;
 		public String label = null;
 		
 		public Argument(Integer... args) {
 			code = Arrays.asList(args);
 		}
 		
 		public Argument(String label, Integer... args) {
 			code = Arrays.asList(args);
 			this.label = label;
 		}
 	}
 	
 	public List<Integer> assemble(String code) {
 		instructions = new ArrayList<Integer>();
 		labels = new HashMap<String, Integer>();
 		fixes = new HashMap<Integer, String>();
 		
 		String[] lines = code.trim().split("\\s*\n\\s*");
		for (String line : lines) {
 			// Comments
 			String[] c = line.split(";");

 			if (c.length == 0)
 				continue;
 			line = c[0];
			if (line.isEmpty())
 				continue;
 			
 			// Labels
 			if (line.startsWith(":")) {
				String[] tmp = line.split("\\s+", 2);
				String label = tmp[0].substring(1).toUpperCase();
 				if (labels.containsKey(label))
 					System.out.println("ERROR: Label " + label + " defined twice!");
 				else
 					labels.put(label, instructions.size());
				if (tmp.length < 2)
					continue;
				line = tmp[1];
				if (line.isEmpty())
					continue;
 			}
 			
			String[] tokens = line.split("\\s*(,|\\s)\\s*");
			
 			if (tokens.length < 2)
 				continue;
 			
 			if (tokens[0].toUpperCase().equals("DAT")) {
 				instructions.addAll(parseDat(tokens));
 				continue;
 			}
 			
 			String op = tokens[0];
 			String arg1 = tokens[1];
 			String arg2 = null;
 			if (tokens.length > 2)
 				arg2 = tokens[2];
 			List<Integer> assembled = assemble(op, arg1, arg2);
 			if (assembled == null)
 				instructions.add(-1);
 			else
 				instructions.addAll(assembled);
 		}
 		
 		System.out.println(labels);
 		insertLabels();
 		
 		//for (int instruction : instructions) {
 		//	System.out.println("0x" + Integer.toHexString(instruction));
 		//}
 		
 		return instructions;
 	}
 	
 	private List<Integer> parseDat(String[] tokens) {
 		List<Integer> data = new ArrayList<Integer>();
 		for (String token : Arrays.copyOfRange(tokens, 1, tokens.length))
 			data.add(parseInt(token));
 		return data;
 	}
 
 	// This inserts labels in parts of the program where they were used before they existed,
 	// via the "fixes" map.
 	private void insertLabels() {
 		for (Map.Entry<Integer, String> entry : fixes.entrySet()) {
 			int index = entry.getKey();
 			String label = entry.getValue();
 			System.out.printf("Fixing: %s at %d\n", label, index);
 			Integer loc;
 			if ((loc = labels.get(label)) != null) {
 				instructions.set(index, loc);
 			} else
 				System.out.printf("Error: True assembly error (in insertLabels): %s at %d\n", label, index);
 		}
 	}
 	
 	public List<Integer> assemble(String sOp, String sArg1, String sArg2) {
		sOp = sOp.toUpperCase();
 		boolean isBasic = (sArg2 != null);
		int op = Arrays.asList(isBasic ? basicOps : specialOps).indexOf(sOp) + (isBasic ? 1 : 0);
 		if (op == -1)
			System.out.println("Broken OP! \"" + sOp + "\" isBasic = " + isBasic);
 		int a, b = -1;
 		int instructionCount = instructions.size();
 		
 		sArg1 = sArg1.toUpperCase();
 		if (isBasic)
 			sArg2 = sArg2.toUpperCase();
 		
 		Argument argA = handleArgument(sArg1);
 		List<Integer> codeA = argA.code;
 		instructionCount += codeA.size() - 1;
 		
 		if (argA.label != null)
 			fixes.put(instructionCount, argA.label);
 		
 		a = codeA.get(0);
 			
 		Argument argB = null;
 		List<Integer> codeB = null;
 		if (isBasic) {
 			argB = handleArgument(sArg2);
 			codeB = argB.code;
 			instructionCount += codeB.size() - 1;
 			if (argB.label != null)
 				fixes.put(instructionCount, argB.label);
 			b = codeB.get(0);
 		}
 		
 		ArrayList<Integer> words = new ArrayList<Integer>();
 		words.add(compile(op, a, b));
 		words.addAll(codeA.subList(1, codeA.size()));
 		if (argB != null)
 			words.addAll(codeB.subList(1, codeB.size()));
 				
 		return words;
 	}
 	
 	public List<Integer> assemble(String op, String arg) {
 		return assemble(op, arg, null);
 	}
 	 
 	private Argument handleArgument(String arg) {
 		int index = Arrays.asList(registers).indexOf(arg);
 		if (index != -1)
 			return new Argument(index);
 		
 		if ((index = Arrays.asList(special).indexOf(arg)) != -1)
 			return new Argument(index + 0x1b);
 		
 		if ((index = handleStack(arg)) != -1)
 			return new Argument(index);
 		
 		if (labels.containsKey(arg)) {
 			int loc = labels.get(arg);
 			// No more inline labels. :(
 			return new Argument(0x1f, loc);
 		}
 		
 		int n = parseInt(arg);
 		if (n != -1) {
 			if (n < 31)
 				return new Argument(n + 0x20);
 			return new Argument(0x1f, n);
 		}
 		
 		if (arg.startsWith("[")) {
 			if (!arg.endsWith("]")) {
 				System.out.println("Error: No closing square bracket.");
 				return null;
 			}
 			arg = arg.substring(1, arg.length() - 1);
 			if ((index = Arrays.asList(registers).indexOf(arg)) != -1)
 				return new Argument(index + 0x8);
 			if ((n = parseInt(arg)) != -1)
 				return new Argument(0x1e, n);
 			if (labels.containsKey(arg))
 				return new Argument(0x1e, labels.get(arg));
 			if (arg.contains("+")) {
 				String split[] = arg.split("\\s*\\+\\s*", 2);
 				String other;
 				for (int i = 0; i < 2; i++) {
 					if ((index = Arrays.asList(registers).indexOf(split[i])) != -1) {
 						other = split[1 - i];
 						if ((n = parseInt(other)) != -1)
 							return new Argument(index + 0x10, n);
 						return new Argument(other, index + 0x10, -1);
 					}
 				}
 				
 			}
 			
 			//System.out.println("Error: Invalid Argument (assembly): [" + arg + "] (maybe a label?)");
 			return new Argument(arg, 0x1e, -1);
 		}
 		
 		//System.out.println("Error: Invalid Argument (assembly): " + arg + " (maybe a label?)");
 		return new Argument(arg, 0x1f, -1);
 	}
 	
 	private static int parseInt(String s) {
 		try {
 			int n = Integer.parseInt(s);
 			return n;
 		} catch (NumberFormatException _) {
 			// Whelp, it wasn't a decimal number.
 		}
 		if (s.toLowerCase().startsWith("0x")) {
 			try {
 				int n = Integer.parseInt(s.substring(2), 16);
 				return n;
 			} catch (NumberFormatException _) {
 				// Whelp, it wasn't a hexadecimal number.				
 			}
 		}
 		return -1;
 	}
 	
 	private static int handleStack(String s) {
 		if (s.equals("POP"))
 			return 0x18;
 		if (s.equals("PEEK"))
 			return 0x19;
 		if (s.equals("PUSH"))
 			return 0x1a;
 		return -1;
 	}
 	
 	// Changes arguments into machine code. This is nice because arguments are 6 bits.
 	public static int compile(int op, int arg1, int arg2) {
 		boolean isBasic = (arg2 != -1);
 		int opLength = isBasic ? 4 : 6;
 		
 		String sOp = String.format("%0" + opLength + "d", Integer.parseInt(Integer.toBinaryString(op))) + (isBasic ? "" : "0000");
 		String sArg1 = String.format("%06d", Integer.parseInt(Integer.toBinaryString(arg1)));
 		String sArg2 = isBasic ? String.format("%06d", Integer.parseInt(Integer.toBinaryString(arg2))) : "";
 		
 		return Integer.parseInt(sArg2 + sArg1 + sOp, 2);
 	}
 	
 	public static int compile(int op, int arg) {
 		return compile(op, arg, -1);
 	}
 }
