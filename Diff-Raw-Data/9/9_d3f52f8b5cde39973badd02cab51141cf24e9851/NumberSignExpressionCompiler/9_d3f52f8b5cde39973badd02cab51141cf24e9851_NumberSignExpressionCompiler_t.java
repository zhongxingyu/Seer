 /*
  * Copyright (c) 2005 Einar Pehrson <einar@pehrson.nu>.
  *
  * This file is part of
  * CleanSheets - a spreadsheet application for the Java platform.
  *
  * CleanSheets is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * CleanSheets is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with CleanSheets; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 package csheets.core.formula.compiler;
 
 import java.io.StringReader;
 import java.text.ParseException;
 import java.util.*;
 
 import antlr.ANTLRException;
 import antlr.collections.AST;
 import csheets.core.*;
 import csheets.core.formula.*;
 import csheets.core.formula.lang.*;
 
 /**
  * A compiler that generates '#'-style formulas from strings. (adapted from
  * Einar Pehrson class, ExcelExpressionCompiler; changes are indicated below)
  * 
  * @author João Carreira
  * @see cshhets.core.formula.compiler.ExcelExpressionCompiler
  */
 public class NumberSignExpressionCompiler implements ExpressionCompiler {
 
 	/** The character that signals that a cell's content is a formula ('#') */
 	/* changed from = to # */
 	public static final char FORMULA_STARTER = '#';
 
 	/**
 	 * Creates the Excel expression compiler.
 	 */
 	public NumberSignExpressionCompiler() {
 	}
 
 	@Override
 	public char getStarter() {
 		return FORMULA_STARTER;
 	}
 
 	@Override
 	public Expression compile(Cell cell, String source)
 			throws FormulaCompilationException {
 		// Creates the lexer and parser
 		/* adapted to support the new parser and lexer */
 		NumberSignFormulaParser parser = new NumberSignFormulaParser(
 				new NumberSignFormulaLexer(new StringReader(source)));
 		try {
 			// Attempts to match an expression
 			parser.expression();
 		} catch (ANTLRException e) {
 			throw new FormulaCompilationException(e);
 		}
 		// Converts the expression and returns it
 		return convert(cell, parser.getAST());
 	}
 
 	/**
 	 * Converts the given ANTLR AST to an expression.
 	 * 
 	 * @param node
 	 *            the abstract syntax tree node to convert
 	 * @return the result of the conversion
 	 */
 	protected Expression convert(Cell cell, AST node)
 			throws FormulaCompilationException {
 		// System.out.println("Converting node '" + node.getText() +
 		// "' of tree '" + node.toStringTree() + "' with " +
 		// node.getNumberOfChildren() + " children.");
 		if (node.getNumberOfChildren() == 0) {
 			try {
 				switch (node.getType()) {
 				case NumberSignFormulaParserTokenTypes.NUMBER:
 					return new Literal(Value.parseNumericValue(node.getText()));
 				case NumberSignFormulaParserTokenTypes.STRING:
 					return new Literal(Value.parseValue(node.getText(),
 							Value.Type.BOOLEAN, Value.Type.DATE));
 				case NumberSignFormulaParserTokenTypes.CELL_REF:
 					return new CellReference(cell.getSpreadsheet(),
 							node.getText());
 				case NumberSignFormulaParserTokenTypes.NAME:
 					/*
 					 * return cell.getSpreadsheet().getWorkbook().
 					 * getRange(node.getText()) (Reference)
 					 */
 				}
 			} catch (ParseException e) {
 				throw new FormulaCompilationException(e);
 			}
 		}
 
 		// Convert function call
 		Function function = null;
 		try {
 			function = Language.getInstance().getFunction(node.getText());
 		} catch (UnknownElementException e) {
 		}
 
 		if (function != null) {
 			List<Expression> args = new ArrayList<Expression>();
 			AST child = node.getFirstChild();
 			if (function instanceof Eval) {
 
 				try {
 
 					Expression expression = convert(cell, node.getFirstChild());
 					cell.setContent(expression.evaluate().toText());
 				} catch (IllegalValueTypeException e) {
 				}
 
 			}
 			if (child != null) {
 				args.add(convert(cell, child));
 				while ((child = child.getNextSibling()) != null)
 					args.add(convert(cell, child));
 			}
 
 			Expression[] argArray = args.toArray(new Expression[args.size()]);
 
 			if (function instanceof Whiledo) {
 
 				try {
 					while (argArray[0].evaluate().toBoolean()) {
 						convert(cell, node);
 					}
 				} catch (IllegalValueTypeException e) {
 				}
 			}
 			if (function instanceof Dowhile) {
 
 				try {
 					while (argArray[argArray.length - 1].evaluate().toBoolean()) {
 						convert(cell, node);
 					}
 				} catch (IllegalValueTypeException e) {
 				}
 			}
 			return new FunctionCall(function, argArray);
 		}
 
 		if (node.getNumberOfChildren() == 1)
 			// Convert unary operation
 			return new UnaryOperation(Language.getInstance().getUnaryOperator(
 					node.getText()), convert(cell, node.getFirstChild()));
 		else if (node.getNumberOfChildren() >= 2) {
 			// Convert binary operation
 			if (node.getType() == NumberSignFormulaParserTokenTypes.SEMI) {
 				convert(cell, node.getFirstChild());
 				return convert(cell, node.getFirstChild().getNextSibling());
 			} else {
 				BinaryOperator operator = Language.getInstance()
 						.getBinaryOperator(node.getText());
 				if (operator instanceof RangeReference) {
 
 					return new ReferenceOperation((Reference) convert(cell,
 							node.getFirstChild()), (RangeReference) operator,
 							(Reference) convert(cell, node.getFirstChild()
 									.getNextSibling()));
 				} else if (operator instanceof RelationalOperatorImpl) {
 					return new RelationalOperatorImpl((Reference) convert(cell,
 							node.getFirstChild()), (RangeReference) operator,
 							(Reference) convert(cell, node.getFirstChild()
 									.getNextSibling()));
 				}
 				/* verifies is operator is ':=' */
 				else if (operator instanceof Attribution) {
 					try {
 						CellReference cellRef = new CellReference(
 								cell.getSpreadsheet(), node.getFirstChild()
 										.getText());
 						Cell destinationCell = cellRef.getCell();
 						AST next = node.getFirstChild();
 						Expression exp = convert(destinationCell,
 								next.getNextSibling());
 						Value val = exp.evaluate();
 						UpdateCellContent.getInstance().triggerUpdate(
 								destinationCell, val);
 					} catch (ParseException e) {
 						throw new FormulaCompilationException(e);
 					} catch (IllegalValueTypeException e) {
 						throw new FormulaCompilationException(e);
 					}
 					return new BinaryOperation(convert(cell,
 							node.getFirstChild()), operator, convert(cell, node
 							.getFirstChild().getNextSibling()));
 				} else {
 					return new BinaryOperation(convert(cell,
 							node.getFirstChild()), operator, convert(cell, node
 							.getFirstChild().getNextSibling()));
 				}
 			}
 		} else {
 			// Shouldn't happen
 			throw new FormulaCompilationException();
 		}
 	}
 }
