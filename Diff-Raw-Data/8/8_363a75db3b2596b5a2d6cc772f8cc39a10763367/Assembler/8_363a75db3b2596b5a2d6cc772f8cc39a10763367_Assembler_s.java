 
 import java.util.*;
 import java.io.*;
 
 class Assembler{
 
 	public static void main(String[] args)
 		throws Exception
 	{
 		String infile = args.length > 0 ? args[0] : getFilename("Input file");
 		Assembler as = new Assembler(infile);
 		String outfile = args.length > 1 ? args[1] : getFilename("Output file (blank to print to screen)");
 		if (outfile.length() == 0)
 			System.out.println(as.assembleToHex());
 		else
 			as.assemble(outfile);
 	}
 
 	public static String getFilename(String ask){
 		System.out.print(ask + ": ");
 		return System.console().readLine();
 	}
 
 	private String filename;
 	private String[][] lines;
 	private HashMap<String, Integer> labelsToLines = new HashMap<String, Integer>();
 	private HashMap<Integer, ArrayList<String>> linesToLabels = new HashMap<Integer, ArrayList<String>>();
 	private HashMap<Integer, AssembleStructure> structures = new HashMap<Integer, AssembleStructure>();
 	private HashMap<Integer, Integer> linesToBytes = new HashMap<Integer, Integer>();
 	private int[] program;
 
 	public Assembler(String filename){
 		this.filename = filename;
 	}
 
 	private String contents()
 		throws Exception
 	{
 		File file = new File(filename);
 		FileReader reader = new FileReader(file);
 		char[] stringChars = new char[(int)file.length()];
 		reader.read(stringChars, 0, stringChars.length);
 		return new String(stringChars);
 	}
 
 	public String assembleToHex()
 		throws Exception
 	{
 		return Hexer.hexArray(assemble());
 	}
 
 	public void assemble(String outfile)
 		throws Exception
 	{
 		int[] program = assemble();
 		FileOutputStream writer = new FileOutputStream(outfile);
 		for (int i = 0; i < program.length; i++){
 			writer.write((byte)(program[i] >> 8));
 			writer.write((byte)(program[i] & 0xFF));
 		}
 		writer.close();
 	}
 
 	public int[] assemble()
 		throws Exception
 	{
 		if (program != null)
 			return program;
 		lexize();
 		structurize();
 		alignBytes();
 		//showLabelAlignment();
 		//showStructure();
 		programize();
 		return program;
 	}
 
 	private void programize()
 		throws Exception
 	{
 		program = new int[length()];
 		int position = 0;
 		for (int i = 0; i < lines.length; i++){
 			AssembleStructure s = structures.get(i);
 			System.arraycopy(s.toBytes(), 0, program, position, s.length());
 			position += s.length();
 		}
 	}
 
 
 	private int length()
 		throws Exception
 	{
 		int length = 0;
 		for (int i = 0; i < lines.length; i++)
 			length += structures.get(i).length();
 		return length;
 	}
 
 	private void showStructure()
 		throws Exception
 	{
 		for (int i = 0; i < lines.length; i++){
 			AssembleStructure s = structures.get(i);
 			System.out.println(s + " ; " + s.length() + " " + Hexer.hexArray(s.toBytes()));
 		}
 	}
 
 	private void showLabelAlignment()
 		throws Exception
 	{
 		for (String label : labelsToLines.keySet())
 			System.out.println(label + " " + labelToByte(label));
 	}
 
 	private int labelToByte(String label)
 		throws Exception
 	{
 		Integer line = labelsToLines.get(label);
 		if (line == null)
 			throw new Exception("Undefined label " + label);
 		Integer alignment = linesToBytes.get(line);
 		if (alignment == null)
 			throw new Exception("Label refers to non-existant line: " + label);
 		return alignment;
 	}
 
 	public boolean isNumericExpression(String expression){
 		return expression.matches("0x[0-9A-Fa-f]+")
 		 		|| expression.matches("[0-9]+");
 	}
 
 	public int getExpression(String expression)
 		throws Exception
 	{
 		if (expression.matches("0x[0-9A-Fa-f]+"))
 			return Hexer.unhex(expression.substring(2));
 		if (expression.matches("[0-9]+"))
 			return Integer.parseInt(expression);
 		return labelToByte(expression);
 	}
 
 	private void alignBytes()
 		throws Exception
 	{
 		int alignment = 0;
 		for (int i = 0; i < structures.size(); i++){
 			linesToBytes.put(i, alignment);
 			alignment += structures.get(i).length();
 		}
 	}
 
 	private void structurize()
 		throws Exception
 	{
 		for (int i = 0; i < lines.length; i++){
 			if (lines[i][0].equalsIgnoreCase("DAT"))
 				structures.put(i, new AssembleData(lines[i]));
 			else
 				structures.put(i, new AssembleInstruction(lines[i]));
 		}
 	}
 
 	private void addLabel(String label, int line){
 		labelsToLines.put(label, line);
 		ArrayList<String> labels = linesToLabels.get(line);
 		if (labels == null)
 			linesToLabels.put(line, labels = new ArrayList<String>());
 		labels.add(label);
 	}
 
 	private void lexize()
 		throws Exception
 	{
		String contents = contents();
 		StringTokenizer tokens = new StringTokenizer(contents, ", \t\n\r\f\"\\", true);
 		boolean inComment = false;
 		String[][] lines = new String[contents.length()][];
 		int currentLine = 0;
 		String[] line = new String[3];
 		int currentToken = 0;
 		while (tokens.hasMoreTokens()){
 			String token = tokens.nextToken();
 			if (token.equals("\n")){
 				if (currentToken > 0){
 					String[] trimLine = new String[currentToken];
 					System.arraycopy(line, 0, trimLine, 0, currentToken);
 					lines[currentLine++] = trimLine;
 					currentToken = 0;
 					line = new String[3];
 				}
 				inComment = false;
 				continue;
 			}
 			if (inComment)
 				continue;
 			if (token.equals(",") || token.equals(" ") || token.equals("\t") || token.equals("\r") || token.equals("\r"))
 				continue;
 			if (token.equals(";")){
 				inComment = true;
 				continue;
 			}
 			if (token.charAt(0) == '"'){
 				boolean escaping = false;
 				while (tokens.hasMoreTokens()){
 					String subtoken = tokens.nextToken();
 					if (! escaping && subtoken.charAt(0) == '\\'){
 						escaping = true;
 					}
 					else if (! escaping && subtoken.charAt(0) == '"'){
 						token += subtoken;
 						break;
 					}
 					else
 						escaping = false;
 					token += subtoken;
 				}
 			}
 			if (token.charAt(0) == ':'){
 				if (currentToken > 0)
 					throw new Exception("Label not at beginning of line " + token);
 				addLabel(token.substring(1), currentLine);
 				continue;
 			}
 			if (currentToken == 0 && token.equalsIgnoreCase("DAT"))
 				line = new String[tokens.countTokens()];
 			if (currentToken >= line.length)
 				throw new Exception("Too many tokens on line " + currentLine + ": " + joinLine(line) + ", " + line);
 			line[currentToken++] = token;
 		}
 		String[][] trimLines = new String[currentLine][];
 		System.arraycopy(lines, 0, trimLines, 0, currentLine);
 		this.lines = trimLines;
 	}
 
 	public String joinLine(String[] line){
 		String str = "";
 		for (String token : line)
 			str += token + " ";
 		return str;
 	}
 
 	private interface AssembleStructure{
 
 		public int length()
 			throws Exception;
 
 		public String toHexParts()
 			throws Exception;
 
 		public int[] toBytes()
 			throws Exception;
 
 	}
 
 	private class AssembleData implements AssembleStructure{
 
 		private int[][] data;
 
 		public AssembleData(String[] line)
 			throws Exception
 		{
 			data = new int[line.length - 1][];
 			for (int i = 1; i < line.length; i++){
 				if (line[i].charAt(0) == '"'){
 					data[i - 1] = getString(line[i]);
 				}
 				else if (isNumericExpression(line[i])){
 					int[] number = new int[1];
 					number[0] = getExpression(line[i]);
 					data[i - 1] = number;
 				}
 				else
 					throw new Exception("Data not understood: " + line[i]);
 			}
 		}
 
 		private int[] getString(String code){
 			HashMap<Character, Character> escapes = new HashMap<Character, Character>();
 			escapes.put('n', '\n');
 			escapes.put('r', '\r');
 			escapes.put('t', '\t');
 			escapes.put('f', '\f');
 			boolean escaping = false;
 			int[] string = new int[code.length()];
 			int currentChar = 0;
 			for (int i = 1; i < code.length() - 1; i++){ // skips the outer quotes
 				char c = code.charAt(i);
 				if (escaping){
 					escaping = false;
 					Character escaped = escapes.get(c);
 					if (escaped != null)
 						c = escaped;
 				}
 				else if (c == '\\'){
 					escaping = true;
 					continue;
 				}
 				string[currentChar++] = c;
 			}
			string[currentChar++] = 0; // null-char delimited I assume.
 			int[] trimString = new int[currentChar];
 			System.arraycopy(string, 0, trimString, 0, currentChar);
 			return trimString;
 		}
 
 		public int length()
 			throws Exception
 		{
 			int length = 0;
 			for (int i = 0; i < data.length; i++)
 				length += data[i].length;
 			return length;
 		}
 
 		public String toHexParts()
 			throws Exception
 		{
 			return Hexer.hexArray(toBytes());
 		}
 
 		public int[] toBytes()
 			throws Exception
 		{
 			int[] bytes = new int[length()];
 			int position = 0;
 			for (int i = 0; i < data.length; i++){
 				System.arraycopy(data[i], 0, bytes, position, data[i].length);
 				position += data[i].length;
 			}
 			return bytes;
 		}
 
 	}
 
 	private class AssembleInstruction implements AssembleStructure{
 
 		public String instruction;
 		public AssembleInstructionData a;
 		public AssembleInstructionData b;
 
 		private HashMap<String, Integer> instructionBytes;
 
 		private void init(){
 			if (instructionBytes == null){
 				instructionBytes = new HashMap<String, Integer>();
 				instructionBytes.put("JSR", 0x0); // other non-basic instructions will also have 0x0
 				instructionBytes.put("SET", 0x1);
 				instructionBytes.put("ADD", 0x2);
 				instructionBytes.put("SUB", 0x3);
 				instructionBytes.put("MUL", 0x4);
 				instructionBytes.put("DIV", 0x5);
 				instructionBytes.put("MOD", 0x6);
 				instructionBytes.put("SHL", 0x7);
 				instructionBytes.put("SHR", 0x8);
 				instructionBytes.put("AND", 0x9);
 				instructionBytes.put("BOR", 0xA);
 				instructionBytes.put("XOR", 0xB);
 				instructionBytes.put("IFE", 0xC);
 				instructionBytes.put("IFN", 0xD);
 				instructionBytes.put("IFG", 0xE);
 				instructionBytes.put("IFB", 0xF);
 			}
 		}
 
 		public AssembleInstruction(String[] line){
 			init();
 			instruction = line[0];
 			if (line.length > 1)
 				a = new AssembleInstructionData(line[1]);
 			if (line.length > 2)
 				b = new AssembleInstructionData(line[2]);
 		}
 
 		public int length()
 			throws Exception
 		{
 			int length = 1;
 			if (a != null && a.hasExtraByte())
 				length++;
 			if (b != null && b.hasExtraByte())
 				length++;
 			return length;
 		}
 
 		public String toHexParts()
 			throws Exception
 		{
 			String hex = Hexer.hex(instructionByte());
 			if (instructionByte() == 0x0)
 				hex += " " + Hexer.hex(nonbasicInstructionByte());
 			if (a != null)
 				hex += " " + Hexer.hex(a.toByte());
 			if (b != null)
 				hex += " " + Hexer.hex(b.toByte());
 			if (a != null && a.hasExtraByte())
 				hex += " " + Hexer.hex(a.extraByte());
 			if (b != null && b.hasExtraByte())
 				hex += " " + Hexer.hex(b.extraByte());
 			return hex;
 		}
 
 		public int[] toBytes()
 			throws Exception
 		{
 			int[] bytes = new int[length()];
 			int instruction = instructionByte();
 			if (instruction == 0x0){
 				instruction |= nonbasicInstructionByte() << 4;
 				if (a != null)
 					instruction |= a.toByte() << 10;
 				// nonbasics shouldn't have a b.
 			}
 			else{
 				if (a != null)
 					instruction |= a.toByte() << 4;
 				if (b != null)
 					instruction |= b.toByte() << 10;
 			}
 			bytes[0] = instruction;
 			if (a != null && a.hasExtraByte())
 				bytes[1] = a.extraByte();
 			if (b != null && b.hasExtraByte())
 				bytes[bytes.length - 1] = b.extraByte();
 			return bytes;
 		}
 
 		private int nonbasicInstructionByte(){
 			return 0x01; // JSR is the only one defined
 		}
 
 		private int instructionByte()
 			throws Exception
 		{
 			Integer inst = instructionBytes.get(instruction);
 			if (inst == null)
 				throw new Exception("Instruction not recognized " + instruction);
 			return inst;
 		}
 
 		public String toString(){
 			String str = instruction;
 			if (a != null)
 				str += " " + a;
 			if (b != null)
 				str += " " + b;
 			return str;
 		}
 
 		public class AssembleInstructionData{
 
 			private String data;
 
 			private HashMap<String, Integer> staticTransforms;
 			private HashMap<String, Integer> plusRegisters;
 
 			private void staticInit(){
 				if (staticTransforms == null){
 					staticTransforms = new HashMap<String, Integer>();
 					staticTransforms.put("A", 0x0);
 					staticTransforms.put("B", 0x1);
 					staticTransforms.put("C", 0x2);
 					staticTransforms.put("X", 0x3);
 					staticTransforms.put("Y", 0x4);
 					staticTransforms.put("Z", 0x5);
 					staticTransforms.put("I", 0x6);
 					staticTransforms.put("J", 0x7);
 					staticTransforms.put("[A]", 0x8);
 					staticTransforms.put("[B]", 0x9);
 					staticTransforms.put("[C]", 0xA);
 					staticTransforms.put("[X]", 0xB);
 					staticTransforms.put("[Y]", 0xC);
 					staticTransforms.put("[Z]", 0xD);
 					staticTransforms.put("[I]", 0xE);
 					staticTransforms.put("[J]", 0xF);
 					staticTransforms.put("POP", 0x18);
 					staticTransforms.put("PEEK", 0x19);
 					staticTransforms.put("PUSH", 0x1A);
 					// aliases to the above
 						staticTransforms.put("[SP++]", 0x18);
 						staticTransforms.put("[SP]", 0x19);
 						staticTransforms.put("[--SP]", 0x1A);
 					staticTransforms.put("SP", 0x1B);
 					staticTransforms.put("PC", 0x1C);
 					staticTransforms.put("O", 0x1D);
 				}
 				if (plusRegisters == null){
 					plusRegisters = new HashMap<String, Integer>();
 					plusRegisters.put("A", 0x10);
 					plusRegisters.put("B", 0x11);
 					plusRegisters.put("C", 0x12);
 					plusRegisters.put("X", 0x13);
 					plusRegisters.put("Y", 0x14);
 					plusRegisters.put("Z", 0x15);
 					plusRegisters.put("I", 0x16);
 					plusRegisters.put("J", 0x17);
 				}
 			}
 
 			public AssembleInstructionData(String data){
 				staticInit();
 				this.data = data;
 			}
 
 			public String toString(){
 				return data;
 			}
 
 			public int toByte()
 				throws Exception
 			{
 				Integer easy = staticTransforms.get(data.toUpperCase());
 				if (easy != null)
 					return easy;
 				if (data.charAt(0) == '[' && data.charAt(data.length() - 1) == ']'){
 					String meat = data.substring(1, data.length() - 1);
 					int plus = meat.indexOf('+');
 					if (plus >= 0){
 						String register = meat.substring(plus + 1);
 						Integer plusRegister = plusRegisters.get(register.toUpperCase());
 						if (plusRegister == null)
 							throw new Exception("[identifier+register] refers to a bad register " + register);
 						return plusRegister;
 					}
 					else
 						return 0x1E; // [next word]
 				}
 				else{ // literal, but which kind?
 					if (isNumericExpression(data)){ // ignore tokens, they may not be aligned yet.
 						int literal = getExpression(data);
 						if (literal < 0x1F)
 							return literal + 0x20;
 					}
 					return 0x1F; // just doing next word literals right now, no inline literals
 				}
 			}
 
 			public int extraByte()
 				throws Exception
 			{
 				int toByte = toByte();
 				if (! hasExtraByte())
 					throw new Exception("extraByte() requested but none is appropriate");
 				if (toByte >= 0x10 && toByte <= 0x17){
 					String meat = data.substring(1, data.length() - 1);
 					int plus = meat.indexOf('+');
 					return getExpression(meat.substring(0, plus));
 				}
 				else if (toByte == 0x1E)
 					return getExpression(data.substring(1, data.length() - 1));
 				else if (toByte == 0x1F)
 					return getExpression(data);
 				else
 					throw new Exception("I'm broken... I don't know what to do with my own toByte==" + Hexer.hex(toByte));
 			}
 
 			public boolean hasExtraByte() // When this is called, the tokens might not be aligned yet, so we cannot call extraByte(). Besides, extraByte() calls this method.
 				throws Exception
 			{
 				int toByte = toByte();
 				return ! ((toByte >= 0x0 && toByte <= 0xF) || (toByte >= 0x18 && toByte <= 0x1D) || (toByte >= 0x20 && toByte <= 0x3F));
 			}
 		}
 	}
 
 }
