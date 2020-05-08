 package codeGeneration;
 
 import org.antlr.runtime.tree.Tree;
 
 import semantics_checks.SemanticsUtils;
 import symbol_table.DATA_TYPES;
 import symbol_table.SymbolTable;
 import symbol_table.VariableSTValue;
 
 public class Statement
 {
 	/**
 	 * @field BUFF_SIZE User-defined buffer size to limit input
 	 * @field count		This field is used to create unique labels for printing 
 	 * 					strings.
 	 */
 	private static final int BUFF_SIZE = 100;
 	public static int count = 0;
 
 	/**
 	 * Checks statements and passes arguments to generateStatementCode to
 	 * convert it to LLVM code
 	 * 
 	 * @param node
 	 *            Current node
 	 * @param table
 	 *            Current Symbol-Table
 	 * @param gen
 	 *            Current LabelGenerator
 	 * @return returns last node
 	 */
 	public static Tree checkAllStatements(Tree node, SymbolTable table,
 			LabelGenerator gen)
 	{
 		Tree current = node;
 		boolean end_of_statements = false;
 		while (current != null && !end_of_statements)
 		{
 			end_of_statements = generateStatementCode(current, table, gen);
 			if (end_of_statements)
 			{
 				return current;
 			}
 			current = SemanticsUtils.getNextChild(current);
 		}
 		return current;
 
 	}
 
 	/**
 	 * Calls Helper method converting the MAlice code to LLVM code
 	 * 
 	 * @param node
 	 *            Current node
 	 * @param table
 	 *            Current SymbolTable
 	 * @param gen
 	 *            Current LabelGenerator
 	 * @return 'true' if end of all statements, else 'true'
 	 */
 	private static boolean generateStatementCode(Tree node, SymbolTable table,
 			LabelGenerator gen)
 	{
 		if (node.getText() == null || node.getChildCount() == 0)
 		{
 			return true;
 		}
 
 		if (node.getText().contentEquals("was"))
 		{
 			writeWasCode(node, table, gen);
 			return false;
 		} else if (node.getText().contentEquals("ate")
 				|| node.getText().contentEquals("drank"))
 		{
 			writeAteAndDrankCode(node, table, gen,
 					(node.getText().contentEquals("ate") ? "add" : "sub"));
 			return false;
 		} else if (node.getText().contentEquals("became"))
 		{
 			writeBecameCode(node, table, gen);
 			return false;
 		} else if (node.getText().contentEquals("spoke")
 				|| node.getText().contentEquals("said"))
 		{
 			writePrintStatementCode(node, table,
 					gen);
 			return false;
 		} else if (node.getText().contentEquals("what"))
 		{
 			writeWhatCode(node, table, gen);
 			return false;
 		} else if (node.getText().contentEquals("found"))
 		{
 			writeFoundCode(node, table, gen);
 			return false;
 		} else if (node.getText().contentEquals("had"))
 		{
 			return false;
 		} else if (node.getText().contentEquals("perhaps"))
 		{
 			ControlStructure.writePerhapsStatements(node, table, gen);
 			return false;
 		} else if (node.getText().contentEquals("either"))
 		{
 			ControlStructure.writeEitherStatement(node, table, gen);
 			return false;
 		} else if (node.getText().contentEquals("eventually"))
 		{
 			ControlStructure.writeEventuallyStatement(node, table, gen);
 			return false;
 		} else if (node.getText().contentEquals("opened"))
 		{
 			return false;
 		} else if (node.getChildCount() >= 2
 				&& node.getChild(0).getText().contentEquals("(")
 				&& node.getChild(node.getChildCount() - 1).getText().contentEquals(")"))
 		{
 			CodeGenerator.addInstruction("call void "
 					+ table.lookup(node.getText()).getLocationReg() + "("
 					+ Expression.getParamsToFunction(node, table, gen) + ")");
 
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Translates WHAT read-statement to LLVM assembly code
 	 * 
 	 * @param node
 	 *            Current node
 	 * @param table
 	 *            Current SymbolTable
 	 * @param gen
 	 *            Current LabelGenerator
 	 */
 	private static void writeWhatCode(Tree node, SymbolTable table,
 			LabelGenerator gen)
 	{
 		CodeGenerator.includeStdin();
 		if (node.getChild(0).getChildCount() == 0)
 		{
 			String arg1 = node.getChild(0).getText();
 			VariableSTValue v = (VariableSTValue) table.lookup(arg1);
 			DATA_TYPES type = v.getType();
 
 			if (type == DATA_TYPES.NUMBER)
 			{
 				String tempReg = gen.getUniqueLabel();
 				String buff = tempReg;
 				CodeGenerator.addInstruction(tempReg + " = alloca ["
 						+ BUFF_SIZE + " x i8], align 16");
 				String regId1 = gen.getUniqueLabel();
 				addGetElementPtrIns(buff, regId1);
 				String regId2 = gen.getUniqueLabel();
 				addLoadIOFileIns(regId2);
 				tempReg = writeCallFGetsIns(gen, regId1, regId2);
 				regId1 = gen.getUniqueLabel();
 				addGetElementPtrIns(buff, regId1);
 				regId2 = gen.getUniqueLabel();
 				CodeGenerator.addInstruction(regId2 + " = call i32 @atoi(i8* "
 						+ regId1 + ") nounwind readonly");
 				CodeGenerator.addInstruction("store i32 " + regId2 + ", i32* "
 						+ v.getLocationReg() + ", align 4");
 				CodeGenerator.includeATOI();
 				CodeGenerator.includeRead();
 			} else if (type == DATA_TYPES.LETTER)
 			{
 				String tempReg = gen.getUniqueLabel();
 				String buff = tempReg;
 				CodeGenerator.addInstruction(tempReg + " = alloca ["
 						+ BUFF_SIZE + " x i8], align 16");
 				String regId1 = gen.getUniqueLabel();
 				addGetElementPtrIns(buff, regId1);
 				String regId2 = gen.getUniqueLabel();
 				addLoadIOFileIns(regId2);
 				tempReg = writeCallFGetsIns(gen, regId1, regId2);
 				regId1 = gen.getUniqueLabel();
 				addGetElementPtrIns(buff, regId1);
 				regId2 = gen.getUniqueLabel();
 				CodeGenerator.addInstruction(regId2 + " = load i8* " + regId1
 						+ ", align 1");
 				CodeGenerator.addInstruction("store i8 " + regId2 + ", i8* "
 						+ v.getLocationReg() + ", align 1");
 				CodeGenerator.includeRead();
 
 			} else if (type == DATA_TYPES.SENTENCE)
 			{
 				String regId1 = gen.getUniqueLabel();
 				String buff = regId1;
 				CodeGenerator.addInstruction(regId1
 						+ " = alloca [100 x i8], align 16");
 				String regId2 = gen.getUniqueLabel();
 				addGetElementPtrIns(regId1, regId2);
 				regId1 = gen.getUniqueLabel();
 				addLoadIOFileIns(regId1);
 				regId1 = writeCallFGetsIns(gen, regId2, regId1);
 				regId2 = gen.getUniqueLabel();
 				addGetElementPtrIns(buff, regId2);
 				CodeGenerator.addInstruction("store i8* " + regId2 + ", i8** "
 						+ v.getLocationReg() + ", align 8");
 
 				CodeGenerator.includeRead();
 			}
 		}
 
 	}
 
 	private static String writeCallFGetsIns(LabelGenerator gen, String regId1,
 			String regId2)
 	{
 		String uniqueLabel = gen.getUniqueLabel();
 		CodeGenerator.addInstruction(uniqueLabel + " = call i8* @fgets(i8* "
 				+ regId1 + ", i32 " + BUFF_SIZE + ", %struct._IO_FILE* "
 				+ regId2 + ")");
 		return uniqueLabel;
 	}
 
 	private static void addGetElementPtrIns(String location, String regId1)
 	{
 		CodeGenerator.addInstruction(regId1 + " = getelementptr inbounds ["
 				+ BUFF_SIZE + " x i8]* " + location + ", i32 0, i32 0 ");
 	}
 
 	private static void addLoadIOFileIns(String regId2)
 	{
 		CodeGenerator.addInstruction(regId2
 				+ " = load %struct._IO_FILE** @stdin, align 8");
 	}
 
 	/**
 	 * Translates WAS declaration-statement to LLVM assembly code
 	 * 
 	 * @param node
 	 *            Current node
 	 * @param table
 	 *            Current SymbolTable
 	 * @param gen
 	 *            Current LabelGenerator
 	 */
 	private static void writeWasCode(Tree node, SymbolTable table,
 			LabelGenerator gen)
 	{
 		Tree storable = node.getChild(2);
 		String arg1 = node.getChild(0).getText();
 		String arg2 = node.getChild(1).getText();
 		if (table.getCurrentScopeLevel() == 0)
 		{
 			if (arg2.equals("number"))
 			{
 				table.lookup(arg1).setLocationReg("@" + arg1);
 				if (storable != null)
 				{
 					CodeGenerator.addInstruction("@" + arg1 + " = global i32 "
 							+ Expression.getResultReg(storable, table, gen)
 							+ ", align 4");
 				}
 			} else if (arg2.equals("letter"))
 			{
 				table.lookup(arg1).setLocationReg("@" + arg1);
 				if (storable != null)
 				{
 					CodeGenerator.addInstruction("@" + arg1 + " = global i8 "
 							+ (int) (storable.getText().charAt(1))
 							+ ", align 1");
 				} else
 				{
 					CodeGenerator.addInstruction("@" + arg1
 							+ " = global i8 0, align 1");
 				}
 			} else if (arg2.equals("sentence"))
 			{
 				table.lookup(arg1).setLocationReg("@" + arg1);
 				if (storable != null)
 				{
 					int strLen = storable.getText().length() - 1;
 					String effective = storable.getText().substring(1, strLen);
 					CodeGenerator.addInstruction("@.at" + arg1
 							+ table.getCurrentScopeLevel()
 							+ " = private unnamed_addr constant [" + strLen
 							+ " x i8] c\"" + effective + '\\' + "00"
 							+ "\", align 1", 0);
 					CodeGenerator.addInstruction("@" + arg1
 							+ " = global i8* getelementptr inbounds (["
 							+ strLen + " x i8]* @.at" + arg1
 							+ table.getCurrentScopeLevel()
 							+ ", i32 0, i32 0), align 8");
 				} else
 				{
 					CodeGenerator.addInstruction("@" + arg1
 							+ " = common global i8* null, align 8");
 				}
 			}
 		} else
 		{
 			if (arg2.equals("number"))
 			{
 				table.lookup(arg1).setLocationReg("%" + arg1);
 				CodeGenerator.addInstruction("%" + arg1
 						+ " = alloca i32, align 4");
 				if (storable != null)
 				{
 					CodeGenerator.addInstruction("store i32 "
 							+ Expression.getResultReg(storable, table, gen)
 							+ ", i32* %" + arg1 + ", align 4");
 				}
 			} else if (arg2.equals("letter"))
 			{
 				table.lookup(arg1).setLocationReg("%" + arg1);
 				CodeGenerator.addInstruction("%" + arg1
 						+ " = alloca i8, align 1");
 				if (storable != null)
 				{
 					CodeGenerator.addInstruction("store i8 "
 							+ (int) (storable.getText().charAt(1)) + ", i8* %"
 							+ arg1 + ", align 1");
 				}
 			} else if (arg2.equals("sentence"))
 			{
 				table.lookup(arg1).setLocationReg("%" + arg1);
 				CodeGenerator.addInstruction("%" + arg1
 						+ " = alloca i8*, align 8");
 				if (storable != null)
 				{
 					int strLen = storable.getText().length() - 1;
 					String effective = storable.getText().substring(1, strLen);
 					CodeGenerator.addInstruction("@.at" + arg1
 							+ table.getCurrentScopeLevel()
 							+ " = private unnamed_addr constant [" + strLen
 							+ " x i8] c\"" + effective + '\\' + "00"
 							+ "\", align 1", 0);
 					CodeGenerator
 							.addInstruction("store i8* getelementptr inbounds" 
 									+ " (["	+ strLen + " x i8]* @.at" + arg1
 									+ table.getCurrentScopeLevel() + ", i32 0," 
 									+ " i32 0), i8** %" + arg1 + ", align 8");
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Generates LLVM code for ate and drank statements.
 	 * 
 	 * @param node
 	 *            Current Tree node.
 	 * @param table
 	 *            Current symbol table.
 	 * @param gen
 	 *            Current label generator.
 	 * @param action
 	 *            Either "add" or "sub"
 	 */
 	public static void writeAteAndDrankCode(Tree node, SymbolTable table,
 			LabelGenerator gen, String action)
 	{
 		String uniqueReg = gen.getUniqueRegisterID();
 		String currReg = Utils.getVarReg(node.getChild(0), table, gen);
 		CodeGenerator.addInstruction(uniqueReg + " = load i32* " + currReg
 				+ ", align 4");
 		currReg = uniqueReg;
 		Expression.writeOperationExpressions(gen, action, currReg, "1");
 		currReg = Utils.getVarReg(node.getChild(0), table, gen);
 		CodeGenerator.addInstruction("store i32 " + uniqueReg + ", i32* "
 				+ currReg + ", align 4");
 	}
 
 	
 	/**
 	 * Generates LLVM code for Became statements.
 	 * 
 	 * @param node	Current Tree node.
 	 * @param table Current symbol table.
 	 * @param gen	Current label generator.
 	 */
 	public static void writeBecameCode(Tree node, SymbolTable table, 
 			LabelGenerator gen) {
 
 		DATA_TYPES type = Utils.getValueType(node.getChild(1), table);
 		String currReg = Utils.getVarReg(node.getChild(0), table, gen);
 
 		if (type == DATA_TYPES.LETTER || type == DATA_TYPES.ARRAY_LETTER)
 		{
 			CodeGenerator.addInstruction("store i8 "
 					+ (int) node.getChild(1).getText().charAt(1) + ", i8* "
 					+ currReg + ", align 1");
 		} else if (type == DATA_TYPES.SENTENCE
 				|| type == DATA_TYPES.ARRAY_SENTENCE)
 		{
 			String curr = node.getChild(1).getText();
 			curr = curr.substring(1, curr.length() - 1);
 			int size = curr.length() + 1;
 
 			(table.lookup(node.getChild(0).getText())).setStringSize(size);
 			String newLabel = "@." + node.getChild(0).getText() + "_" + count;
 			count++;
 			CodeGenerator.addGlobalInstruction(newLabel + " = private "
 					+ "unnamed_addr constant [" + size + " x i8] c\"" + curr
 					+ "\\00\", " + "align 1");
 			CodeGenerator.addInstruction("store i8* getelementptr inbounds (["
 					+ size + " x i8]* " + newLabel + ", i32 0, i32 0), i8** "
 					+ currReg + ", align 8");
 		} else
 		{
 			CodeGenerator.addInstruction("store i32 "
 					+ Expression.getResultReg(node.getChild(1), table, gen)
 					+ ", i32* " + currReg + ", align 4");
 		}
 	}
 
 	/**
 	 * Generates print statements for 'said Alice' and 'spoke' statements.
 	 * 
 	 * @param node	Current Tree node.
 	 * @param table Current symbol table
 	 * @param gen	Current label generator
 	 */
 	public static void writePrintStatementCode(Tree node, SymbolTable table,
 			LabelGenerator gen)
 	{
 		CodeGenerator.includePrint();
 		String uniqueReg;
 		DATA_TYPES nodeType = Utils.getValueType(node.getChild(0), table);
 
 		if (nodeType == DATA_TYPES.SENTENCE)
 		{
 			uniqueReg = gen.getUniqueRegisterID();
 			sentenceAtNode(node, uniqueReg, ACTION.PRINT);
 			return;
 		} else if (nodeType == DATA_TYPES.LETTER)
 		{
 			uniqueReg = gen.getUniqueRegisterID();
 			String charToPrint = "" + node.getChild(0).getText().charAt(1);
 			CodeGenerator.includePrintString();
 			String newLabel = "@.str_" + count;
 			count++;
 			CodeGenerator.addGlobalInstruction(newLabel + " = private "
 					+ "unnamed_addr constant [2 x i8] c\"" + charToPrint
 					+ "\\00\", align 1");
 			CodeGenerator
 					.addInstruction(uniqueReg
 							+ " = call i32 (i8*, ...)* "
 							+ "@printf(i8* getelementptr inbounds ([3 x i8]* "
 							+ "@.printString, i32 0, i32 0), i8* getelementptr" 
 							+ " inbounds ([2 x i8]* " + newLabel + ", i32 0," 
 							+ " i32 0))");
 
 			return;
 		}
 		try
 		{
 			if ((VariableSTValue) 
 					table.lookup(node.getChild(0).getText()) != null)
 			{
 				uniqueReg = gen.getUniqueRegisterID();
 				DATA_TYPES type = (table.lookup(node.getChild(0).getText()))
 						.getType();
 				String currentReg = Utils.getVarReg(node.getChild(0), table,
 						gen);
 				// (table.lookup(node.getChild(0).getText())).getLocationReg();
 
 				if (type == DATA_TYPES.SENTENCE)
 				{
 					CodeGenerator.addInstruction(uniqueReg + " = load i8** "
 							+ currentReg + ", align 8");
 					currentReg = uniqueReg;
 					uniqueReg = gen.getUniqueRegisterID();
 					CodeGenerator
 							.addInstruction(uniqueReg
 									+ " = call i32 (i8*, ...)* @printf(i8*" 
 									+ " getelementptr inbounds ([3 x i8]*" 
 									+ " @.printString, i32 0, i32 0), i8* "
 									+ currentReg + ")");
 					CodeGenerator.includePrintString();
 
 				} else
 				{
 					CodeGenerator.addInstruction(uniqueReg + " = load "
 							+ getBits(type) + "* " + currentReg + ", align "
 							+ getAlignValue(type));
 					currentReg = uniqueReg;
 					uniqueReg = gen.getUniqueRegisterID();
 					if (type == DATA_TYPES.LETTER)
 					{
 						CodeGenerator.addInstruction(uniqueReg + " = sext i8 "
 								+ currentReg + " to i32");
 						currentReg = uniqueReg;
 						uniqueReg = gen.getUniqueRegisterID();
 						CodeGenerator
 								.addInstruction(uniqueReg
 										+ " = call i32 (i8*, ...)* @printf(i8*" 
 										+ " getelementptr inbounds ([3 x i8]*" 
 										+ " @.printChar, i32 0, i32 0), i32 "
 										+ currentReg + ")");
 						CodeGenerator.includePrintChar();
 					} else
 					{
 						CodeGenerator
 								.addInstruction(uniqueReg
 										+ " = call i32 (i8*, ...)* @printf(i8* " 
 										+ "getelementptr inbounds ([3 x i8]* " 
 										+ "@.printInt, i32 0, i32 0), i32 "
 										+ currentReg + ")");
 						CodeGenerator.includePrintInt();
 					}
 				}
 				return;
 			}
 		} catch (ClassCastException e)
 		{
 		}
 
 		CodeGenerator.includePrintString();
 		String currReg = Expression.getResultReg(node.getChild(0), table, gen);
 		uniqueReg = gen.getUniqueRegisterID();
 		String newLabel = "@.str_" + count;
 		count++;
 		try {
 			int i = Integer.parseInt(currReg);
 			CodeGenerator.addGlobalInstruction(newLabel + " = private "
 					+ "unnamed_addr constant [" + (currReg.length() + 1)
 					+ " x i8] c\"" + i + "\\00\", align 1");
 			CodeGenerator.addInstruction(uniqueReg + " = call i32 (i8*, ...)* "
 					+ "@printf(i8* getelementptr inbounds ([3 x i8]* "
 					+ "@.printString, i32 0, i32 0), i8* getelementptr inbounds"
 					+ " ([" + (currReg.length() + 1) + " x i8]* " + newLabel
 					+ ", i32 0, i32 0))");
 		} catch (NumberFormatException e)
 		{
 		}
 		CodeGenerator.includePrintInt();
 		CodeGenerator.addInstruction(uniqueReg + " = call i32 (i8*, ...)* " 
 				+ "@printf(i8* getelementptr inbounds ([3 x i8]* @.printInt," 
 				+ " i32 0, i32 0), i32 " + currReg + ")");
 	}
 
 	/**
 	 * Generates LLVM code for 'said Alice', 'spoke' and 'Alice found' 
 	 * statements, depending on an enum value passed in the parameter. This 
 	 * function is only to be used if the child node of the Tree passed in the 
 	 * method is a sentence (as opposed to a variable, letter, number or 
 	 * expression.) 
 	 * 
 	 * @param node		Current Tree node.
 	 * @param uniqueReg String representing the current unique register. 
 	 * @param action	Enum value indicating if we want a code for print or
 	 * 					return statements.
 	 */
 	private static void sentenceAtNode(Tree node, String uniqueReg,
 			ACTION action)
 	{
 		String curr = node.getChild(0).getText();
 		curr = curr.substring(1, curr.length() - 1);
 		int size = curr.length() + 1;
 		String newLabel = "@.str_" + count;
 		count++;
 		CodeGenerator.addGlobalInstruction(newLabel + " = private "
 				+ "unnamed_addr constant [" + size + " x i8] c\"" + curr
 				+ "\\00\", align 1");
 		if (action == ACTION.PRINT)
 		{
 			CodeGenerator.addInstruction(uniqueReg + " = call i32 (i8*, ...)* "
 					+ "@printf(i8* getelementptr inbounds ([" + size
 					+ " x i8]* " + newLabel + ", i32 0, i32 0))");
 		} else
 		{
 			CodeGenerator.addInstruction("ret i8* getelementptr inbounds (["
 					+ size + " x i8*]* " + newLabel + ", i32 0, i32 0");
 		}
 	}
 
 	/**
 	 * Returns the number of bytes showing how they are aligned in the memory.
 	 * 
 	 * @param type	The data type for which we need to find the align value.
 	 * @return		The align value.
 	 */
 	private static String getAlignValue(DATA_TYPES type)
 	{
 		if (type == DATA_TYPES.LETTER)
 		{
 			return "1";
 		} else if (type == DATA_TYPES.NUMBER)
 		{
 			return "4";
 		}
 		return null;
 	}
 
 	/**
 	 * Returns a String representing the number of bits in the type.
 	 * 
 	 * @param type 	Data type for which we want to find the number of bits.
 	 * @return		The number of bits corresponding to the given type, 
 	 * 				preceded by the letter 'i'.
 	 */
 	private static String getBits(DATA_TYPES type)
 	{
 		if (type == DATA_TYPES.LETTER)
 		{
 			return "i8";
 		} else if (type == DATA_TYPES.NUMBER)
 		{
 			return "i32";
 		}
 		return null;
 	}
 
 	/**
 	 * Generates the LLVM code for 'Alice found' statements.
 	 * 
 	 * @param node	Current Tree node.
 	 * @param table Current symbol table.
 	 * @param gen	Current label generator.
 	 */
 	public static void writeFoundCode(Tree node, SymbolTable table,
 			LabelGenerator gen)
 	{
 		DATA_TYPES nodeType = Utils.getValueType(node.getChild(0), table);
 
 		if (nodeType == DATA_TYPES.SENTENCE)
 		{
 			sentenceAtNode(node, gen.getUniqueRegisterID(), ACTION.RETURN);
 
 			return;
 		} else if (nodeType == DATA_TYPES.LETTER)
 		{
 			CodeGenerator.addInstruction("ret i8 "
 					+ (int) node.getChild(0).getText().charAt(1));
 			return;
 			/****************************************
 			 * MIGHT NEED TO INCLUDE A DOWN CAST AND TRY AND CATCH FOR
 			 * CLASSCASTEXCEPTION
 			 ****************************************/
 		} else if (table.lookup(node.getChild(0).getText()) != null)
 		{
 			String uniqueReg = gen.getUniqueRegisterID();
 			String currentReg = Utils.getVarReg(node.getChild(0), table, gen);
 			// (table.lookup(node.getChild(0).getText())).getLocationReg();
 			DATA_TYPES type = (table.lookup(node.getChild(0).getText()))
 					.getType();
 
 			if (type == DATA_TYPES.LETTER)
 			{
 				CodeGenerator.addInstruction(uniqueReg + " = load i8* "
 						+ currentReg + ", align 1");
 				currentReg = uniqueReg;
 				uniqueReg = gen.getUniqueRegisterID();
 				CodeGenerator.addInstruction(uniqueReg + " = sext i8 "
 						+ currentReg + " to i32");
 				CodeGenerator.addInstruction("ret i32 " + uniqueReg);
 			} else if (type == DATA_TYPES.SENTENCE)
 			{
 				CodeGenerator.addInstruction(uniqueReg + " = load i8** "
 						+ currentReg + ", align 8");
 				currentReg = uniqueReg;
 				uniqueReg = gen.getUniqueRegisterID();
 				CodeGenerator.addInstruction(uniqueReg + " = ptrtoint i8* "
 						+ currentReg + " to i32");
 				CodeGenerator.addInstruction("ret i32 " + uniqueReg);
 			} else
 			{
 				CodeGenerator.addInstruction(uniqueReg + " = load i32* "
 						+ currentReg + ", align 4");
 				CodeGenerator.addInstruction("ret i32 " + uniqueReg);
 			}
 			return;
 		}
 		String currentReg = Expression.getResultReg(node.getChild(0), table,
 				gen);
 		CodeGenerator.addInstruction("ret i32 " + currentReg);
 	}
 
 	/**
 	 * Enum representing which action we are currently undertaking.
 	 */
 	private enum ACTION
 	{
 		PRINT, RETURN;
 	}
 }
