 package net.ian.dcpu;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 public class Assembler {
 	public static final String[] basicOps = { "SET", "ADD", "SUB", "MUL", "DIV", "MOD", "SHL", "SHR", "AND", "BOR", "XOR", "IFE", "IFN", "IFG", "IFB" };
 	public static final String[] specialOps = { "EXIT", "JSR" };
 	public static final String[] registers;
 	public static final String[] special = { "SP", "PC", "O" };
 	
 	static {
 		DCPU.Register regs[] = DCPU.Register.values();
 		registers = new String[regs.length];
 		for (DCPU.Register r : regs)
 			registers[r.ordinal()] = r.toString();
 	}
 	
 	public static List<Integer> assemble(String code) {
 		ArrayList<Integer> instructions = new ArrayList<Integer>();
 		String[] lines = code.split("\n");
 		for (String line : lines) {
 			if (line.startsWith(";"))
 				continue;
 			String[] tokens = line.split(",?\\s+");
 			if (tokens.length < 2)
 				continue;
 			String op = tokens[0];
 			String arg1 = tokens[1];
 			String arg2 = null;
 			if (tokens.length > 2)
 				arg2 = tokens[2];
 			instructions.addAll(assemble(op, arg1, arg2));
 		}
 		return instructions;
 	}
 	
 	public static List<Integer> assemble(String sOp, String sArg1, String sArg2) {
 		boolean isBasic = (sArg2 != null);
		int op = Arrays.asList(isBasic ? basicOps : specialOps).indexOf(sOp.toUpperCase()) + 1;
 		int a, b = -1;
 		int[] argsA = handleArgument(sArg1.toUpperCase());
 
 		a = argsA[0];
 			
 		int[] argsB = null;
 		if (isBasic) {
 			argsB = handleArgument(sArg2.toUpperCase());
 			b = argsB[0];
 		}
 		
 		ArrayList<Integer> words = new ArrayList<Integer>();
 		words.add(compile(op, a, b));
 		if (argsA.length > 1)
 			words.add(argsA[1]);
		if (argsB.length > 1)
 			words.add(argsB[1]);
 				
 		return words;
 	}
 	
 	public static List<Integer> assemble(String op, String arg) {
 		return assemble(op, arg, null);
 	}
 	
 	private static int[] single(int n) {
 		int[] s = {n};
 		return s;
 	}
 	
 	private static int[] pair(int a, int b) {
 		int[] p = {a, b};
 		return p;
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
 	 
 	private static int[] handleArgument(String arg) {
 		int index = Arrays.asList(registers).indexOf(arg);
 		if (index != -1)
 			return single(index);
 		
 		if ((index = Arrays.asList(special).indexOf(arg)) != -1)
 			return single(index + 0x1b);
 		
 		int n = parseInt(arg);
 		if (n != -1) {
 			if (n < 31)
 				return single(n + 0x20);
 			return pair(0x1f, n);
 		}
 		
 		if (arg.startsWith("[")) {
 			if (!arg.endsWith("]"))
 				return single(-1);
 			arg = arg.substring(1, arg.length() - 1);
 			if ((index = Arrays.asList(registers).indexOf(arg)) != -1)
 				return single(index + 0x8);
 			if ((n = parseInt(arg)) != -1) {
 				return pair(0x1e, n);
 			}
 		}
 		return null;
 	}
 	
 	// Changes arguments into machine code. This is nice because arguments are 6 bits.
 	public static int compile(int op, int arg1, int arg2) {
 		boolean isBasic = (arg2 != -1);
 		int opLength = isBasic ? 4 : 6;
		String sOp = String.format("%0" + opLength + "d", Integer.parseInt(Integer.toBinaryString(op)));
 		String sArg1 = String.format("%06d", Integer.parseInt(Integer.toBinaryString(arg1)));
		String sArg2 = isBasic ? String.format("%06d", Integer.parseInt(Integer.toBinaryString(arg2))) : "0000";
 		return Integer.parseInt(sArg2 + sArg1 + sOp, 2);
 	}
 	
 	public static int compile(int op, int arg) {
 		return compile(op, arg, -1);
 	}
 }
