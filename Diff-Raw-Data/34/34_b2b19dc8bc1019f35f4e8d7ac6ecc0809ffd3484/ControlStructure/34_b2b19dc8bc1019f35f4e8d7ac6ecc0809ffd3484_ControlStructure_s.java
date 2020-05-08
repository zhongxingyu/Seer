 package codeGeneration;
 
 import java.util.ArrayList;
 
 import org.antlr.runtime.tree.Tree;
 
 import semantics_checks.SemanticsUtils;
 import symbol_table.SymbolTable;
 
 public class ControlStructure
 {
 	public static void writeEitherStatement(Tree node, SymbolTable table,
 			LabelGenerator gen)
 	{
 		Tree current = node.getChild(0);
 		String boolExp = Expression.getResultReg(current, table, gen);
 		try
 		{
 			int bool = Integer.parseInt(boolExp);
 
 			if (bool == 1)
 			{
 				// code for then case only
 				// Do all statements then skip to end of either
 				Statement.generateAllStatementCode(node, table, gen);
 				return;
 			} else
 			{
 				// code for else case only
 				while (!current.getText().contentEquals("or"))
 				{
 					current = SemanticsUtils.getNextChild(current);
 				}
 				current = SemanticsUtils.getNextChild(current);
 				// Do all statements then skip to end of either
 				Statement.generateAllStatementCode(current, table, gen);
 				return;
 			}
 		} catch (NumberFormatException e)
 		{
 		}
 
 		String lblThen = gen.getUniqueLabel();
 		int brInsIndex = CodeGenerator.getNumberOfInstructions();
 		int[] endIfLblInserts = new int[2];
 		CodeGenerator.addInstruction(getLabel(lblThen));
 		current = SemanticsUtils.getNextChild(current);
 		current = Statement.generateAllStatementCode(current, table, gen);
 		endIfLblInserts[0] = CodeGenerator.getNumberOfInstructions();
 		String lblElse = gen.getUniqueLabel();
 		CodeGenerator.addInstruction(getLabel(lblElse));
 		current = SemanticsUtils.getNextChild(current);
 		Statement.generateAllStatementCode(current, table, gen);
 		endIfLblInserts[1] = CodeGenerator.getNumberOfInstructions();
 		String lblEndIf = gen.getUniqueLabel();
 		CodeGenerator.addInstruction(getLabel(lblEndIf));
 		CodeGenerator
 				.addInstruction(getBranchIns(lblEndIf), endIfLblInserts[1]);
 		CodeGenerator
 				.addInstruction(getBranchIns(lblEndIf), endIfLblInserts[0]);
 		CodeGenerator.addInstruction(getConditionalBranchIns(boolExp, lblThen,
 				lblElse), brInsIndex);
 	}
 
 	public static void writeEventuallyStatement(Tree node, SymbolTable table,
 			LabelGenerator gen)
 	{
 		String startLbl = gen.getUniqueLabel();
 		int startLblIndex = CodeGenerator.getNumberOfInstructions();
 		Tree current = node.getChild(0);
 		String boolExp = Expression.getResultReg(current, table, gen);
 		try
 		{
 			int bool = Integer.parseInt(boolExp);
 			if (bool == 0)
 				return;
 		} catch (NumberFormatException e){ }
 		CodeGenerator.addInstruction(getBranchIns(startLbl),startLblIndex);
 		CodeGenerator.addInstruction(getLabel(startLbl),startLblIndex+1);
 		int brInsIndex = CodeGenerator.getNumberOfInstructions();
 		String loop = gen.getUniqueLabel();
 		CodeGenerator.addInstruction(getLabel(loop));
 		current = SemanticsUtils.getNextChild(current);
 		current = Statement.generateAllStatementCode(current, table, gen);
 		CodeGenerator.addInstruction(getBranchIns(startLbl));
 		String endLoop = gen.getUniqueLabel();
 		CodeGenerator.addInstruction(getLabel(endLoop));
 		CodeGenerator.addInstruction(getConditionalBranchIns(boolExp, loop,
 				endLoop), brInsIndex);
 	}
 
 	public static void writePerhapsStatements(Tree node, SymbolTable table,
 			LabelGenerator gen)
 	{
 		String bool_exp = Expression.getResultReg(node.getChild(0), table, gen);
 		try
 		{
 			int bool = Integer.parseInt(bool_exp);
 			if (bool == 1)
 			{
 				Statement.generateAllStatementCode(node, table, gen);
 				return;
 			}
 		} catch (NumberFormatException e){ }
 		ArrayList<Integer> endIfInserts = new ArrayList<Integer>();
 		int brInsert = CodeGenerator.getNumberOfInstructions();
 		String startLbl = gen.getUniqueLabel();
 		CodeGenerator.addInstruction(getLabel(startLbl));
 		Tree current = node.getChild(1);
 		current = Statement.generateAllStatementCode(current, table, gen);
 		endIfInserts.add(CodeGenerator.getNumberOfInstructions());
 		String endLbl = gen.getUniqueLabel();
 		CodeGenerator.addInstruction(getLabel(endLbl));
 		while (current != null && current.getText().contentEquals("maybe"))
 		{
 			CodeGenerator.addInstruction(getConditionalBranchIns(bool_exp,
 					startLbl, endLbl), brInsert);
 			current = SemanticsUtils.getNextChild(current);
 			bool_exp = Expression.getResultReg(current, table, gen);
 			try
 			{
 				int bool = Integer.parseInt(bool_exp);
 				if (bool == 1)
 				{
 					// DO ALL STATEMENTS
 					current = Statement.generateAllStatementCode(current, table, gen);
 					// DO ALL STATEMENTS
 					return;
 				}
 			} catch (NumberFormatException e){ }
 			brInsert = CodeGenerator.getNumberOfInstructions();
 			startLbl = gen.getUniqueLabel();
 			CodeGenerator.addInstruction(getLabel(startLbl));
 			// DO ALL STATEMENTS
 			Statement.generateAllStatementCode(current, table, gen);
 			// DO ALL STATEMENTS
 			endIfInserts.add(CodeGenerator.getNumberOfInstructions());
 			endLbl = gen.getUniqueLabel();
 			CodeGenerator.addInstruction(getLabel(endLbl));
 			current = SemanticsUtils.getNextChild(current);
 		}
 
 		CodeGenerator.addInstruction(getConditionalBranchIns(bool_exp,
 				startLbl, endLbl), brInsert);
 		String endIf;
 		if (current != null && current.getText().contentEquals("or"))
 		{
 			current = SemanticsUtils.getNextChild(current);
 			// DO ALL STATEMENTS
 			Statement.generateAllStatementCode(current, table, gen);
 			// DO ALL STATEMENTS
 			endIf = gen.getUniqueLabel();
 			CodeGenerator.addInstruction(getLabel(endIf));
 		} else
 		{
 			endIf = gen.getUniqueLabel();
 			CodeGenerator.addInstruction(getLabel(endIf));
 		}
 		String endIfIns = getBranchIns(endIf);
 		for (int i = endIfInserts.size() - 1; i >= 0; i--)
 		{
 			CodeGenerator.addInstruction(endIfIns, endIfInserts.get(i) + 1);
 		}
 
 	}
 
 	private static String getConditionalBranchIns(String boolExp, String reg1,
 			String reg2)
 	{
 		return "br i1 " + boolExp + ", label " + reg1 + ", label " + reg2;
 	}
 
 	private static String getBranchIns(String lbl)
 	{
 		return "br label " + lbl;
 	}
 
 	private static String getLabel(String lbl)
 	{
 		return ";<label>:" + lbl.substring(1);
 	}
 }
