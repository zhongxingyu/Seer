 package codeGeneration;
 
 import org.antlr.runtime.tree.Tree;
 
 import semantics_checks.SemanticsUtils;
 import symbol_table.DATA_TYPES;
 import symbol_table.FunctionSTValue;
 import symbol_table.SymbolTable;
 
 public class Function
 {
 	public static Tree writeCodeForFunctions(Tree node, SymbolTable table, LabelGenerator gen) 
 	{
 		if (node.getText().contentEquals("looking"))
 		{
 			return writeCodeForProcedure(node, table, gen);
 		} else if (node.getText().contentEquals("room")){
 			return writeCodeForFunction(node, table,gen);
 		}
 		return node;
 	}
 
 	private static Tree writeCodeForProcedure(Tree node, SymbolTable table,LabelGenerator gen )
 	{
 		Tree current = node.getChild(0);
 		FunctionSTValue fVal = 
 			(FunctionSTValue) table.lookup(node.getChild(0).getText());
 		fVal.setLocationReg(current.getText().contentEquals("hatta")? 
 				"@main" : "@" + current);
 		current = SemanticsUtils.getNextChild(current);
 		String params = getParamsForFunctions(current,table);
 		writeFunctionHeader("i32", fVal.getLocationReg(), params);
 		CodeGenerator.incrementIdentLevel();
 		//DO ALL STATEMENTS
 		current = Statement.checkAllStatements(current, table, gen);
 		//DO ALL STATEMENTS
 		//current = writeNestedFunctions(table, current,gen);
 		writeReturnStatement("i32", "0");
 		CodeGenerator.decrementIdentLevel();
 		CodeGenerator.addInstruction("}");
 		return current;
 	}
 	
 	private static Tree writeCodeForFunction(Tree node, SymbolTable table,LabelGenerator gen)
 	{
 		Tree current = node.getChild(0);
 		System.out.println(node.getChild(0).getText());
 		FunctionSTValue fVal = 
 			(FunctionSTValue) table.lookup(node.getChild(0).getText());
 		fVal.setLocationReg("@" + current);
 		current = SemanticsUtils.getNextChild(current);
		String params = getParamsForFunctions(current,fVal.getTable());
 		current = SemanticsUtils.getNextChild(
 				SemanticsUtils.getNextChild(current));
 		writeFunctionHeader(
 				Utils.getReturnTypeOfFunction(fVal.getType())
 				, fVal.getLocationReg()
 				, params);
 		
 		CodeGenerator.incrementIdentLevel();
 		//DO ALL STATEMENTS
		current = Statement.checkAllStatements(current, fVal.getTable(), gen);
 		//DO ALL STATEMENTS
 		//current = writeNestedFunctions(table, current,gen);
 		CodeGenerator.decrementIdentLevel();
 		CodeGenerator.addInstruction("}");
 		return current;
 	}
 
 	private static Tree writeNestedFunctions(SymbolTable table, Tree current,LabelGenerator gen)
 	{
 		try
 		{
 			Tree temp = current;
 			while (true)
 			{
 				temp = writeCodeForFunction(current , table,gen);
 				if (temp == current)
 				{
 					break;
 				}
 				//current = StatementChecker.checkAllStatements(curr, table);
 				//THIS HAS TO BE WRITE CODE FOR ALL STATEMENTS
 				current = temp;
 				//THIS HAS TO BE WRITE CODE FOR ALL STATEMENTS
 				
 			}
 		} catch (NullPointerException e)
 		{
 		}
 		return current;
 	}
 
 	private static String getParamsForFunctions(Tree child, SymbolTable table)
 	{
 		Tree current = child;
 		String params = "";
 		while (current!=null)
 		{
 			DATA_TYPES type = getType(current.getText()) ;
 			if (type == null) break;
 			params += Utils.getReturnTypeOfFunction(type) + " "
 			+ "%" + current.getChild(0).getText()
 			+ ", ";
 			current = SemanticsUtils.getNextChild(current);
 		}
 		return (params.length()!=0)? "(" + params.substring(0, params.length()-2) + ")" : "()";
 	}
 	
 	private static DATA_TYPES getType(String type) 
 	{
 		if(type.contentEquals("number"))
 		{
 			return DATA_TYPES.NUMBER;
 		} else if(type.contentEquals("letter"))
 		{
 			return DATA_TYPES.LETTER;
 		} else if (type.contentEquals("sentence"))
 		{
 			return DATA_TYPES.SENTENCE;
 		} 
 		return null;
 	}
 	private static void writeFunctionHeader(String ret_type, String name, String params)
 	{
 		CodeGenerator.addInstruction(
 				"define " + 
 				ret_type + " " +
 				name + " " + params + " " +
 				"nounwind uwtable readnone {"
 				);
 	}
 	
 	private static void writeReturnStatement(String ret_type, String val)
 	{
 		CodeGenerator.addInstruction("ret " + ret_type + " " + val);
 	}
 }
