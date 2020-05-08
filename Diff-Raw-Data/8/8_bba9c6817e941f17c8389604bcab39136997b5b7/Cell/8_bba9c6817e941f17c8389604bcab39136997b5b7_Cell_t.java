 package model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.ConcurrentModificationException;
 import java.util.HashMap;
 import java.util.Stack;
 
 import com.graphbuilder.math.Expression;
 import com.graphbuilder.math.ExpressionParseException;
 import com.graphbuilder.math.ExpressionTree;
 import com.graphbuilder.math.FuncMap;
 import com.graphbuilder.math.VarMap;
 
 public class Cell implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1242802920028523182L;
 
 	public static final boolean DEBUG = false;
 
 	private int row;
 
 	private int column;
 
 	private String rawExpression;
 
 	private Object calculatedValue;
 
 	private Sheet sheet;
 
 	private ArrayList<Cell> cellsListeningToMe;
 
 	private HashMap<String, Cell> cellsIReferTo;
 
 	private ArrayList<Cell> cellsIHaveRegisteredListenersWith;
 
 	private transient Expression cellExpression;
 
 	private transient VarMap varmap;
 
 	private String cellLocation;
 
 	private transient FuncMap functionMap;
 
 	public Cell(Sheet sheet, int row, int col, Object value) {
 		this.sheet = sheet;
 		this.row = row;
 		this.column = col;
 		this.cellLocation = sheet.getModelCellLocation(row, col);
 		this.calculatedValue = value;
 		this.cellsIReferTo = new HashMap<String, Cell>();
 		this.cellsListeningToMe = new ArrayList<Cell>();
 		this.cellsIHaveRegisteredListenersWith = new ArrayList<Cell>();
 		this.cellExpression = ExpressionTree.parse("0");
 		functionMap = new FuncMap();
 		functionMap.loadDefaultFunctions();
 	}
 
 	/**
 	 * Raw meaning before being processed by our parser.
 	 * 
 	 * @return The actual text the user has put into this cell, uncompiled.
 	 */
 	public String getRawExpression() {
 		return rawExpression;
 	}
 
 	/**
 	 * Add a cell to the list of cells that are listening to this cell for
 	 * changes.
 	 * 
 	 * @param newListeningCell
 	 *            Cell who's expression references this cell
 	 */
 	public void addListener(Cell newListeningCell) {
 		cellsListeningToMe.add(newListeningCell);
 	}
 
 	/**
 	 * Remove a cell from the listener list.
 	 * 
 	 * @param listenerCell
 	 */
 	public void removeListener(Cell listenerCell) {
 		cellsListeningToMe.remove(listenerCell);
 	}
 
 	/**
 	 * Checks if the rawExpression starts with an '='. If it does then it does
 	 * nothing (right now). If it doesn't start with that then it sets our
 	 * calculatedValue to the rawExpression (since no calculations are needed)
 	 * and then sets new rawExpression.
 	 * 
 	 * @param rawExpression
 	 */
 	public void setRawExpression(String rawExpression) {
 
 		this.rawExpression = rawExpression;
 
 		varmap = new VarMap();
 
 		functionMap = new FuncMap();
 		functionMap.loadDefaultFunctions();
 
 		// I (might) have a new list of cells I am listening to.
 		// let the old list know I am no longer listening to them
 		for (Cell c : this.cellsIHaveRegisteredListenersWith) {
 			c.removeListener(this);
 		}
 
 		// nothing entered into cell,
 		// set calculated value to raw expression
 		// and parse the (blank) expression
 		if (rawExpression.length() == 0) {
			calculatedValue = rawExpression;
			cellExpression = ExpressionTree.parse("0");
 		} else if(rawExpression.equals("=")){
 			// why would you enter in just '='? Oh well, we
 			// handle that anyway ;-)
			calculatedValue = rawExpression;
			cellExpression = ExpressionTree.parse("0");
 
 		} else {
 
 			// does this raw expression start with "="?
 			boolean startsWithEquals = rawExpression.substring(0, 1)
 					.equals("=");
 
 			// if yes, this is an expression that needs to be recursively
 			// compiled
 			if (startsWithEquals) {
 				String rawExpressionWithoutEquals = rawExpression.substring(1,
 						rawExpression.length());
 				// compile this expression (after removing the leading "=")
 				calculatedValue = compileRawExpression(rawExpressionWithoutEquals);
 
 			} else {
 				// the data entered into this cell isn't an expression
 
 				calculatedValue = rawExpression;
 				try {
 					Double.parseDouble(String.valueOf(calculatedValue));
 					cellExpression = ExpressionTree.parse(String
 							.valueOf(calculatedValue));
 				} catch (NumberFormatException nfe) {
 					cellExpression = ExpressionTree.parse("0");
 				}
 
 			}
 		}
 
 		// recompile every cell listening to me
 		for (Cell c : this.cellsListeningToMe) {
 			if (DEBUG) {
 				System.out.println("recompiling " + c);
 			}
 			try {
 				if (c != this) {
 
 					c.recompile(this, new Stack<Cell>());
 				}
 			} catch (ConcurrentModificationException e) {
 				try {
 					Thread.sleep(250);
 				} catch (InterruptedException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 
 			}
 		}
 
 	}
 
 	/**
 	 * Recompile this cell.
 	 * 
 	 */
 	private void recompile(Cell cellThatINeedToUpdate,
 			Stack<Cell> previouslyRecompiledCells) {
 		if (DEBUG) {
 			System.out.println("\tThis cell's raw expression: "
 					+ this.rawExpression);
 			System.out.println("\tCompiled expression before recompile: "
 					+ this.calculatedValue);
 		}
 		if (previouslyRecompiledCells.search(this) != -1) {
 			return;
 		} else {
 			previouslyRecompiledCells.push(this);
 			try {
 				String[] namesOfCellsIReferTo = cellExpression
 						.getVariableNames();
 				for (String nameOfCellIReferTo : namesOfCellsIReferTo) {
 					Cell cellIReferTo = sheet.getCells()
 							.get(nameOfCellIReferTo);
 					varmap.setValue(nameOfCellIReferTo, cellIReferTo
 							.getValueRecursively(new Stack<Cell>()));
 
 				}
 
 				calculatedValue = cellExpression.eval(varmap, functionMap);
 
 				Double thisValue = (Double) calculatedValue;
 				Integer thisIntValue = thisValue.intValue();
 
 				// if casting this cell's value to an integer doesn't change its
 				// value, change the caluclatedValue to this new int value
 				if (Double.compare(thisValue, thisIntValue.doubleValue()) == 0) {
 					calculatedValue = thisIntValue;
 				}
 			} catch (CircularityException ce) {
 				calculatedValue = "ERR: Circularity!";
 			} catch (NumberFormatException nfe) {
 				calculatedValue = "ERR: non-number references";
 			} catch (RuntimeException re) {
 				calculatedValue = re.getMessage();
 			}
 
 			for (Cell c : this.cellsListeningToMe) {
 				if (c != this) {
 					c.recompile(this, previouslyRecompiledCells);
 					previouslyRecompiledCells.pop();
 				}
 			}
 			if (DEBUG) {
 				System.out.println("\tCompiled expression after recompile: "
 						+ this.calculatedValue);
 			}
 		}
 	}
 
 	/**
 	 * Returns the compiled form of rawExpression
 	 * 
 	 * @param rawExpression
 	 *            The expression the be compiled, without the leading "="
 	 */
 	private Object compileRawExpression(String rawExpression) {
 
 		if (DEBUG) {
 			System.out.println("compiling raw expression...");
 		}
 
 		// turn rawExpression into an Expression object
 		try {
 			cellExpression = ExpressionTree.parse(rawExpression);
 		} catch (ExpressionParseException e) {
 			// this expression was syntacticaly invalid
 			return "ERR";
 		}
 
 		String[] cellsReferencedInExpression = cellExpression
 				.getVariableNames();
 
 		if (DEBUG) {
 			System.out.println("\t" + cellsReferencedInExpression.length
 					+ " variable(s) referenced");
 		}
 
 		// lookup each cell referenced in the expression, storing
 		// them in cellsIReferTo
 		for (String cellName : cellsReferencedInExpression) {
 			if (DEBUG) {
 				System.out.println("\tLooking up value of'" + cellName + "':");
 			}
 			Cell thisReferencedCell = sheet.getCells().get(cellName);
 
 			// failed to lookup cell by name
 			// the user probably entered an expression with a bad cell name
 			if (thisReferencedCell == null) {
 				if (DEBUG) {
 					System.out.println("\tERROR: bad cell name");
 				}
 				return "ERR: bad cell name";
 			}
 
 			// successfuly found cell by name
 			// add this cell to cellsIReferTo
 			// and register a listener with that cell
 			else {
 				if (DEBUG) {
 					System.out
 							.println("\t\tAbout to add cell to 'cells i refer to': "
 									+ thisReferencedCell);
 				}
 				cellsIReferTo.put(cellName, thisReferencedCell);
 				cellsIHaveRegisteredListenersWith.add(thisReferencedCell);
 				thisReferencedCell.addListener(this);
 			}
 		}
 
 		// now that 'cellsIReferTo' is populated with verified cell objects,
 		// get and return the value of this cell (start recursive call)
 		try {
 			Stack<Cell> previouslyVisitedCells = new Stack<Cell>();
 			try {
 				calculatedValue = getValueRecursively(previouslyVisitedCells);
 
 			} catch (NumberFormatException e) {
 				return "ERR: non-number references";
 			} catch (NullPointerException npe) {
 				return "ERR: " + npe.getMessage();
 			} catch (RuntimeException e) {
 				calculatedValue = "ERR: undefined function";
 				return "ERR: undefined function";
 			}
 
 		} catch (CircularityException e) {
 
 			return "ERR: Circularity!";
 		}
 
 		Double thisValue = (Double) calculatedValue;
 		Integer thisIntValue = thisValue.intValue();
 
 		// if casting this cell's value to an integer doesn't change its
 		// value, return the integer version
 		// otherwise, return the double version
 		if (Double.compare(thisValue, thisIntValue.doubleValue()) == 0) {
 			return thisIntValue;
 		} else {
 			return calculatedValue;
 		}
 
 	}
 
 	private double getValueRecursively(Stack<Cell> visitedCells)
 			throws CircularityException, NumberFormatException {
 
 		// if this cell is already in the stack, throw CircilarityException
 		if (visitedCells.search(this) != -1) {
 			throw new CircularityException();
 		} else {
 
 			visitedCells.push(this);
 
 			// if this cell contains an Expression object
 			if (cellExpression != null) {
 				varmap = new VarMap();
 
 				String[] cellNamesInExpression = cellExpression
 						.getVariableNames();
 
 				for (String cellName : cellNamesInExpression) {
 					Cell cellIReferTo = cellsIReferTo.get(cellName);
 					if (cellIReferTo == null) {
 						throw new NullPointerException("ERR: bad cell name");
 					}
 					double value = cellIReferTo
 							.getValueRecursively(visitedCells);
 					varmap.setValue(cellName, value);
 				}
 
 				// now that the varmap is populated, use it to evaluate the
 				// expression
 				return cellExpression.eval(varmap, functionMap);
 			} else {
 				// System.out.println(calculatedValue.getClass());
 				return Double.parseDouble((String) this.calculatedValue);
 			}
 		}
 	}
 
 	/**
 	 * Value calculated when we set the raw expression. No additional
 	 * calculation or work is necessary here because will already be calculated.
 	 * 
 	 * @return The object that we ended up with at the end of the
 	 *         setRawExpression method. Could be String, Int, Double, etc.
 	 */
 	public Object getCalculatedValue() {
 
 		return calculatedValue;
 	}
 
 	public String toString() {
 		String returnMe = new String();
 
 		returnMe += "Cell at " + this.row + "," + this.column
 				+ " (row,column). Value = " + this.getRawExpression();
 
 		return returnMe;
 	}
 
 	public void initializeCell() {
 		if (rawExpression == null) {
 			this.rawExpression = "";
 			this.cellExpression = ExpressionTree.parse("0");
 		} else {
 			setRawExpression(rawExpression);
 		}
 	}
 
 }
