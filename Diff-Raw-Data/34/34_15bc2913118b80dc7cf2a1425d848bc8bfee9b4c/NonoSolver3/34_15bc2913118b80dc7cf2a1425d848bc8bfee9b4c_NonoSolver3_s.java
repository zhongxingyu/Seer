 package service;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.LinkedList;
 
 import models.Block;
 import models.Column;
 import models.Riddle;
 import models.Row;
 import de.feu.propra.nonogramme.interfaces.INonogramSolver;
 
 public class NonoSolver3 implements INonogramSolver {
 
 	private RiddleLoader riddleLoader;
 
 	private Riddle riddle;
 
 	char[][] matrix;
 
 	/**
 	 * Konstruktor. Er kann mit einer bereits gefüllten matrix aufgerufen
 	 * werden. Sonst wird eine matrix initialisiert.
 	 * 
 	 * @param matrix
 	 */
 	public NonoSolver3(char[][] matrix) {
 
 		if (matrix == null) {
 
 		} else {
 			this.matrix = matrix;
 		}
 
 	}
 
 	@Override
 	public String getEmail() {
 		return "csgt01@gmail.com";
 	}
 
 	@Override
 	public String getMatrNr() {
 		return "8352437";
 	}
 
 	@Override
 	public String getName() {
 		return "Christian Schulte genannt Trux";
 	}
 
 	@Override
 	public void openFile(String arg0) throws IOException {
 		String methodName = "openFile(" + arg0 + ")";
 		System.out.println(methodName);
 		riddleLoader = new RiddleLoader();
 		riddle = riddleLoader.readFile(arg0);
 	}
 
 	@Override
 	public char[][] getSolution() {
 		String methodName = "getSolution()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		setupMatrix();
 		setupBlocks();
 		try {
 			boolean run1 = true;
 			while (run1) {
 				int starCount = getStarCountInRiddle();
 				solveIterative();
 				showMatrix();
 				solveRecursive();
 				showMatrix();
 				if (starCount <= getStarCountInRiddle()) {
 					run1 = false;
 				}
 			}
 
 			System.out.println(riddle);
 			showMatrix();
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.out.println(e);
 			showMatrix();
 			showBlockGoneTrue();
 			showMatrix();
 			System.out.println(riddle);
 		}
 		getSizesOfPossibilities();
 //TODO: recursiv
 //		 if (getStarCountInRiddle() > 0) {
 //		 int rowWithSmallestSizesOfPossibilities =
 //		 getRowWithSmallestSizesOfPossibilities();
 //		 System.out.println(rowWithSmallestSizesOfPossibilities);
 //		 LinkedList<String> first =
 //		 getRows().get(rowWithSmallestSizesOfPossibilities).getPossibilities().get(1);
 //		 getRows().get(rowWithSmallestSizesOfPossibilities).getPossibilities().remove(0);
 //		 try {
 //		 fillListIntoMatrixInRow(rowWithSmallestSizesOfPossibilities, first);
 //		 boolean run1 = true;
 //		 while (run1) {
 //		 int starCount = getStarCountInRiddle();
 //		 solveIterative();
 //		 solveRecursive();
 //		 if (starCount <= getStarCountInRiddle()) {
 //		 run1 = false;
 //		 }
 //		 }
 //		 } catch (Exception e) {
 //		 e.printStackTrace();
 //		 }
 //		 getSizesOfPossibilities();
 //		 }
 
 		showMatrix();
 		showBlockGoneTrue();
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 		return matrix;
 	}
 
 	private void solveIterative() throws Exception {
 		String methodName = "solveIterative()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		boolean run = true;
 		while (run) {
 			int starCountInRiddle = getStarCountInRiddle();
 			showMatrix();
 			findOverlappingAreasInColumn();
 			showMatrix();
 			findOverlappingAreasInRow();
 			showMatrix();
 			fillBlocksOnBeginningOfColumns();
 			showMatrix();
 			fillBlocksOnEndOfColumns();
 			showMatrix();
 			fillBlocksOnBeginningOfRows();
 			showMatrix();
 			fillBlocksOnEndOfRows();
 			showMatrix();
 			checkRowsAndColumnsForGone();
 			showMatrix();
 			checkRowsAndColumnsForGone();
 			showMatrix();
 			if (starCountInRiddle <= getStarCountInRiddle()) {
 				run = false;
 			}
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 	}
 
 	/**
 	 * Erstellt eine neue Matrix mit der Breite und Höhe des Rätsels und füllt
 	 * diese mit '*'.
 	 */
 	private void setupMatrix() {
 		String methodName = "setupBlocks()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		matrix = new char[riddle.getHeight()][riddle.getWidth()];
 		for (int i = 0; i < riddle.getHeight(); i++) {
 			for (int j = 0; j < riddle.getWidth(); j++) {
 				matrix[i][j] = '*';
 			}
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 	}
 
 	/**
 	 * Setzt die initialen Werte für minStartIndex und maxStartIndex. Wichtig
 	 * für {@link #solveRecursive()}.
 	 */
 	private void setupBlocks() {
 		String methodName = "setupBlocks()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		// alle Reihen durchgehen.
 		for (Row row : getRows()) {
 			LinkedList<Block> blocks = row.getBlocks();
 			int index = getIndexOfRow(row);
 			System.out.println(index);
 			// Blöcke durchgehen.
 			setupBlocksInRowAndColumn(blocks, riddle.getWidth());
 		}
 
 		for (Column column : getColumns()) {
 			LinkedList<Block> blocks = column.getBlocks();
 			// Blöcke durchgehen.
 			setupBlocksInRowAndColumn(blocks, riddle.getHeight());
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 	}
 
 	// Iterativ
 
 	/**
 	 * Geht durch die Spalten und sucht nach überlappenden Bereichen. Dabei
 	 * werden die Blöcke aneinander gelegt und bis ans Ende der Spalten
 	 * geschoben. An Stellen die immer gefüllt sind kann die Matrix gefüllt
 	 * werden.
 	 * 
 	 * @throws Exception
 	 *             falls eine Stelle in der Matrix schon mit einem anderen char
 	 *             gefüllt ist.
 	 */
 	private void findOverlappingAreasInColumn() throws Exception {
 		String methodName = "findOverlappingAreasInColumn()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		for (int columnInt = 0; columnInt < riddle.getWidth(); columnInt++) {
 			LinkedList<String> workingList = new LinkedList<String>();
 			LinkedList<String> firstList = new LinkedList<String>();
 			Column column = riddle.getColumns().get(columnInt);
 			System.out.println(column);
 			LinkedList<Block> blocks = column.getBlocks();
 			// nur wenn es Blöcke gibt
 			if (blocks.size() != 0) {
 				Block lastBlock = null;
 				int resultIndex = 0;
 				// Blöcke durchgehen und Größen und mögliche Zwischenräume
 				// addieren
 				for (Block block : blocks) {
 					if (null != lastBlock
 							&& lastBlock.getColour().getName() == block
 									.getColour().getName()) {
 						workingList.add("-");
 						resultIndex++;
 					}
 					for (int i = 0; i < block.getHowMany(); i++) {
 						workingList.add(String.valueOf(block.getColour()
 								.getName()));
 					}
 					resultIndex += block.getHowMany();
 					lastBlock = block;
 				}
 				// Restliche, nicht mit Blöcken besetzte Stellen mit - füllen
 				for (int i = resultIndex; i < riddle.getHeight(); i++) {
 					workingList.add("-");
 				}
 				firstList.addAll(workingList);
 				// nur die Indeces der Farben in die ResultList schreiben
 				LinkedList<Integer> result = new LinkedList<Integer>();
 				for (int i = 0; i < firstList.size(); i++) {
 
 					if (!firstList.get(i).equals("-")) {
 						result.add(i);
 					}
 				}
 				// eine Stelle nach hinten schieben
 				String removed;
 				while (workingList.getLast().equals("-")) {
 					removed = workingList.removeLast();
 					workingList.addFirst(removed);
 					int size = result.size();
 					// der eigentliche Vergleich
 					for (int i = size - 1; i > -1; i--) {
 						Integer index = result.get(i);
 						if (!firstList.get(index)
 								.equals(workingList.get(index))) {
 							result.remove(index);
 						}
 					}
 				}
 				for (Integer rowIndex : result) {
 					char charAt = firstList.get(rowIndex).charAt(0);
 					writeCharInMatrix(rowIndex, charAt, columnInt);
 				}
 				// sonst: keine Blöcke, also mit '-' füllen
 			} else {
 				column.setGone(true);
 				fillAreaInColumnWithChar(columnInt, 0, riddle.getHeight(), '-');
 			}
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 		return;
 	}
 
 	/**
 	 * Geht durch die Spalten und sucht nach überlappenden Bereichen. Dabei
 	 * werden die Blöcke aneinander gelegt und bis ans Ende der Spalten
 	 * geschoben. An Stellen die immer gefüllt sind kann die Matrix gefüllt
 	 * werden.
 	 * 
 	 * @param matrix
 	 * @return
 	 * @throws Exception
 	 */
 	private void findOverlappingAreasInRow() throws Exception {
 		String methodName = "findOverlappingAreasInRow()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		for (int rowInt = 0; rowInt < riddle.getHeight(); rowInt++) {
 			LinkedList<String> workingList = new LinkedList<String>();
 			LinkedList<String> first = new LinkedList<String>();
 			Row row = riddle.getRows().get(rowInt);
 			System.out.println(row);
 			LinkedList<Block> blocks = row.getBlocks();
 			// nur wenn es Blöcke gibt
 			if (blocks.size() != 0) {
 				Block lastBlock = null;
 				int resultIndex = 0;
 				// alle Blöcke so weit wie möglich nach links verschoben
 				workingList = getFirstConditionOfRow(blocks, lastBlock,
 						resultIndex);
 				first.addAll(workingList);
 				LinkedList<Integer> result = new LinkedList<Integer>();
 				for (int i = 0; i < first.size(); i++) {
 					if (!first.get(i).equals("-")) {
 						result.add(i);
 					}
 				}
 				String removed;
 				// so lange nach rechts verschieben, bis Ende erreicht und zu
 				// asd hinzufügen.
 				while (workingList.getLast().equals("-")) {
 					removed = workingList.removeLast();
 					workingList.addFirst(removed);
 					int size = result.size();
 					for (int i = size - 1; i > -1; i--) {
 						Integer index = result.get(i);
 						if (!first.get(index).equals(workingList.get(index))) {
 							result.remove(index);
 						}
 					}
 				}
 				for (Integer column : result) {
 					char charAt = first.get(column).charAt(0);
 					writeCharInMatrix(rowInt, charAt, column);
 				}
 				// sonst: keine Blöcke, also mit - füllen
 			} else {
 				row.setGone(true);
 				fillAreaInRowWithChar(rowInt, 0, riddle.getWidth(), '-');
 			}
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 		return;
 	}
 
 	/**
 	 * Spalte und Reihe wird mit '-' gefüllt, falls gone == true ist.
 	 */
 	private void checkRowsAndColumnsForGone() {
 		String methodName = "checkRowsAndColumnsForGone()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		for (Row row : getRows()) {
 			if (row.isGone()) {
 				fillRowWithFree(row);
 			}
 		}
 		for (Column column : getColumns()) {
 			if (column.isGone()) {
 				fillColumnWithFree(column);
 			}
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 	}
 
 	/**
 	 * Füllt Blöcke am Ende der Spalten.
 	 * 
 	 * @return
 	 * @throws Exception
 	 */
 	private char[][] fillBlocksOnEndOfColumns() throws Exception {
 		String methodName = "fillBlocksOnEndOfColumns()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		for (Column column : riddle.getColumns()) {
 			fillBlocksOnEndOfColumn(column, column.getBlocks().size() - 1,
 					riddle.getHeight() - 1);
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 		return matrix;
 	}
 
 	/**
 	 * Wenn eine Spalte mit einer Farbe endet wird der Block gefüllt. Auch
 	 * angefangene Blöcke danach werden gefüllt.
 	 * 
 	 * @return
 	 * @throws Exception
 	 */
 	private void fillBlocksOnEndOfColumn(Column column, int blockIndex,
 			int rowIndex) throws Exception {
 		String methodName = "fillBlocksOnEndOfColumn()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		int block = blockIndex;
 		int rowInt = rowIndex;
 		boolean run = true;
 		while (run) {
 			if (rowInt > -1 && matrix[rowInt][getIndexOfColumn(column)] == '-') {
 				rowInt--;
 			} else {
 				run = false;
 			}
 		}
 		if (rowInt > -1 && matrix[rowInt][getIndexOfColumn(column)] != '*') {
 			LinkedList<Block> blocks = column.getBlocks();
 			if (blocks != null) {
 				Block colourBlock = blocks.get(block);
 				if (matrix[rowInt][getIndexOfColumn(column)] == colourBlock
 						.getColour().getName()) {
 					fillAreaInColumnWithChar(getIndexOfColumn(column), (rowInt
 							- colourBlock.getHowMany() + 1), (rowInt + 1),
 							colourBlock.getColour().getName());
 					colourBlock.setGone(true, rowInt);
 					rowInt = rowInt - colourBlock.getHowMany();
 					if (-1 < block - 1) {
 						block--;
 						Block nextBlock = blocks.get(block);
 						if (nextBlock != null
 								&& nextBlock.getColour().getName() == colourBlock
 										.getColour().getName()) {
 							fillAreaInColumnWithChar(getIndexOfColumn(column),
 									rowInt, rowInt + 1, '-');
 							rowInt--;
 						}
 						fillBlocksOnEndOfColumn(column, block, rowInt);
 					} else {
 						fillColumnWithFree(column);
 					}
 				} else {
 					throw new DataCollisionException("char "
 							+ matrix[getIndexOfColumn(column)][rowInt]
 							+ " ungleich " + colourBlock.getColour().getName());
 				}
 
 			}
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 	}
 
 	/**
 	 * Füllt Blöcke am Anfang der Spalten.
 	 * 
 	 * @return
 	 * @throws Exception
 	 */
 	private char[][] fillBlocksOnBeginningOfColumns() throws Exception {
 		String methodName = "fillBlocksOnBeginningOfColumns()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		for (Column column : riddle.getColumns()) {
 			fillBlocksOnBeginningOfColumn(column, 0, 0);
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 		return matrix;
 	}
 
 	/**
 	 * Füllt Blöcke am Anfang einer Spalte. Ruft sich rekursiv auf.
 	 * 
 	 * @param column
 	 * @param blockIndex
 	 * @throws Exception
 	 */
 	private void fillBlocksOnBeginningOfColumn(Column column, int blockIndex,
 			int rowIndex) throws Exception {
 		String methodName = "fillBlocksOnBeginningOfColumn()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		if (!column.isGone()) {
 			int block = blockIndex;
 			int rowInt = rowIndex;
 			boolean run = true;
 			// '-' überspringen
 			while (run) {
 				if (rowInt < riddle.getHeight()
 						&& matrix[rowInt][getIndexOfColumn(column)] == '-') {
 					rowInt++;
 				} else {
 					run = false;
 				}
 			}
 			// Wenn kein '*' und nicht hinter dem Ende
 			if (rowInt < riddle.getHeight()
 					&& matrix[rowInt][getIndexOfColumn(column)] != '*') {
 				LinkedList<Block> blocks = column.getBlocks();
 				if (blocks != null) {
 					Block colourBlock = blocks.get(block);
 					// wenn die Farben übereinstimmen Matrix mit Block füllen
 					if ((rowInt + colourBlock.getHowMany()) < riddle
 							.getHeight()
 							&& matrix[rowInt][getIndexOfColumn(column)] == colourBlock
 									.getColour().getName()) {
 						fillAreaInColumnWithChar(getIndexOfColumn(column),
 								rowInt, (rowInt + colourBlock.getHowMany()),
 								colourBlock.getColour().getName());
 						colourBlock.setGone(true, rowInt);
 						rowInt = rowInt + colourBlock.getHowMany();
 						// wenn dahinter noch ein Block Methode wieder aufrufen
 						if (blocks.size() > block + 1) {
 							block++;
 							Block nextBlock = blocks.get(block);
 							if (nextBlock != null
 									&& nextBlock.getColour().getName() == colourBlock
 											.getColour().getName()) {
 								fillAreaInColumnWithChar(
 										getIndexOfColumn(column), rowInt,
 										rowInt + 1, '-');
 								rowInt++;
 							}
 							fillBlocksOnBeginningOfColumn(column, block, rowInt);
 							// falls kein Block dahinter kann Spalte mit '-'
 							// gefüllt werden
 						} else {
 							fillColumnWithFree(column);
 						}
 					} else {
 						if ((rowInt + colourBlock.getHowMany()) < riddle
 								.getHeight()) {
 							throw new DataCollisionException("char "
 									+ matrix[rowInt][getIndexOfColumn(column)]
 									+ " at" + rowInt + "/"
 									+ getIndexOfColumn(column) + " ungleich "
 									+ colourBlock.getColour().getName());
 						}
 					}
 
 				}
 			}
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 	}
 
 	/**
 	 * Füllt Blöcke am Ende der Spalten.
 	 * 
 	 * @return
 	 * @throws Exception
 	 */
 	private char[][] fillBlocksOnEndOfRows() throws Exception {
 		String methodName = "fillBlocksOnEndOfRows()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		for (Row row : riddle.getRows()) {
 			fillBlocksOnEndOfRow(row, row.getBlocks().size() - 1,
 					riddle.getWidth() - 1);
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 		return matrix;
 	}
 
 	/**
 	 * Wenn eine Spalte mit einer Farbe endet wird der Block gefüllt. Auch
 	 * angefangene Blöcke danach werden gefüllt.
 	 * 
 	 * @return
 	 * @throws Exception
 	 */
 	private void fillBlocksOnEndOfRow(Row row, int blockIndex, int columnIndex)
 			throws Exception {
 		String methodName = "fillBlocksOnEndOfRow()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		int block = blockIndex;
 		int columnInt = columnIndex;
 		boolean run = true;
 		while (run) {
 			if (columnInt > -1 && matrix[getIndexOfRow(row)][columnInt] == '-') {
 				columnInt--;
 			} else {
 				run = false;
 			}
 		}
 		if (columnInt > -1 && matrix[getIndexOfRow(row)][columnInt] != '*') {
 			LinkedList<Block> blocks = row.getBlocks();
 			if (blocks != null) {
 				Block colourBlock = blocks.get(block);
 				if (matrix[getIndexOfRow(row)][columnInt] == colourBlock
 						.getColour().getName()) {
 					fillAreaInRowWithChar(getIndexOfRow(row), (columnInt
 							- colourBlock.getHowMany() + 1), (columnInt + 1),
 							colourBlock.getColour().getName());
 					colourBlock.setGone(true, columnInt);
 					columnInt = columnInt - colourBlock.getHowMany();
 					if (-1 < block - 1) {
 						block--;
 						Block nextBlock = blocks.get(block);
 						if (nextBlock != null
 								&& nextBlock.getColour().getName() == colourBlock
 										.getColour().getName()) {
 							fillAreaInRowWithChar(getIndexOfRow(row),
 									columnInt, columnInt + 1, '-');
 							columnInt--;
 						}
 						fillBlocksOnEndOfRow(row, block, columnInt);
 					} else {
 						fillRowWithFree(row);
 					}
 				} else {
 					throw new DataCollisionException("char "
 							+ matrix[getIndexOfRow(row)][columnInt]
 							+ " ungleich " + colourBlock.getColour().getName());
 				}
 
 			}
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 	}
 
 	/**
 	 * Füllt Blöcke am Anfang der Spalten.
 	 * 
 	 * @return
 	 * @throws Exception
 	 */
 	private char[][] fillBlocksOnBeginningOfRows() throws Exception {
 		String methodName = "fillBlocksOnBeginningOfRows()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		for (Row row : riddle.getRows()) {
 			fillBlocksOnBeginningOfRow(row, 0, 0);
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 		return matrix;
 	}
 
 	/**
 	 * Füllt Blöcke am Anfang einer Spalte. Ruft sich rekursiv auf.
 	 * 
 	 * @param row
 	 * @param blockIndex
 	 * @throws Exception
 	 */
 	private void fillBlocksOnBeginningOfRow(Row row, int blockIndex,
 			int columnIndex) throws Exception {
 		String methodName = "fillBlocksOnBeginningOfRow()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		if (!row.isGone()) {
 			showMatrix();
 			int block = blockIndex;
 			int columnInt = columnIndex;
 			boolean run = true;
 			// '-' überspringen
 			while (run) {
 				if (columnInt < riddle.getWidth()
 						&& matrix[getIndexOfRow(row)][columnInt] == '-') {
 					columnInt++;
 				} else {
 					run = false;
 				}
 			}
 			// Wenn kein '*' und nicht hinter dem Ende
 			if (columnInt < riddle.getWidth()
 					&& matrix[getIndexOfRow(row)][columnInt] != '*') {
 				LinkedList<Block> blocks = row.getBlocks();
 				if (blocks != null) {
 					Block colourBlock = blocks.get(block);
 					// wenn die Farben übereinstimmen Matrix mit Block füllen
 					if ((columnInt + colourBlock.getHowMany()) <= riddle
 							.getWidth()
 							&& matrix[getIndexOfRow(row)][columnInt] == colourBlock
 									.getColour().getName()) {
 						fillAreaInRowWithChar(getIndexOfRow(row), columnInt,
 								(columnInt + colourBlock.getHowMany()),
 								colourBlock.getColour().getName());
 						colourBlock.setGone(true, columnInt);
 						columnInt = columnInt + colourBlock.getHowMany();
 						// wenn dahinter noch ein Block Methode wieder aufrufen
 						if (blocks.size() > block + 1) {
 							block++;
 							Block nextBlock = blocks.get(block);
 							if (nextBlock != null
 									&& nextBlock.getColour().getName() == colourBlock
 											.getColour().getName()) {
 								fillAreaInRowWithChar(getIndexOfRow(row),
 										columnInt, columnInt + 1, '-');
 								columnInt++;
 							}
 							fillBlocksOnBeginningOfRow(row, block, columnInt);
 							// falls kein Block dahinter kann Spalte mit '-'
 							// gefüllt werden
 						} else {
 							fillRowWithFree(row);
 						}
 					} else {
 						if ((columnInt + colourBlock.getHowMany()) < riddle
 								.getWidth()) {
 							throw new DataCollisionException("char "
 									+ matrix[columnInt][getIndexOfRow(row)]
 									+ " at" + columnInt + "/"
 									+ getIndexOfRow(row) + " ungleich "
 									+ colourBlock.getColour().getName());
 						}
 					}
 
 				}
 			}
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 	}
 
 	// Rekursiv
 
 	/**
 	 * Sucht nach einer Lösung nach einem rekursiven Ansatz.
 	 * 
 	 * @throws Throwable
 	 */
 	private void solveRecursive() throws Exception {
 		String methodName = "solveRecursive()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		boolean run = true;
 		for (Column column : getColumns()) {
			LinkedList<String> firstConditionOfRow = getFirstConditionOfColumn(
 					column.getBlocks(), null, 0);
 			ArrayList<LinkedList<String>> possibilities = column
 					.getPossibilities();
 			if (possibilities == null || possibilities.size() == 0) {
 				possibilities = getPossibilitiesForRowOrColumn(
						column.getBlocks(), firstConditionOfRow, possibilities,
 						0, 0);
 				possibilities.add(getFirstConditionOfColumn(column.getBlocks(),
 						null, 0));
 			}
 			column.setPossibilities(possibilities);
 		}
 		// läuft, bis keine Änderungen mehr vorkommen
 		while (run) {
 			run = false;
 
 			for (Row row : getRows()) {
 				System.out.println("Row:" + getIndexOfRow(row));
 				// Initiale leere Liste
 				ArrayList<LinkedList<String>> possibilities = row
 						.getPossibilities();
 				// System.out.println("Pos1:" + possibilities);
 				if (row.getBlocks().size() > 0 && !row.isGone()) {
 					// Wenn es noch keine Liste mit Möglichkeiten gibt wir sie
 					// hier
 					// erstellt.
 					if (possibilities == null || possibilities.size() == 0) {
 						LinkedList<String> firstConditionOfRow = getFirstConditionOfRow(
 								row.getBlocks(), null, 0);
 						possibilities = getPossibilitiesForRowOrColumn(
 								row.getBlocks(), firstConditionOfRow,
 								possibilities, 0, 0);
 						// Initiale Möglichkeit muss von HAnd hinzugefügt werden
 						possibilities.add(getFirstConditionOfRow(
 								row.getBlocks(), null, 0));
 					}
 					row.setPossibilities(possibilities);
 					int posibillitiesBefore = possibilities.size();
 					// System.out.println("Pos1 size:" + possibilities.size() +
 					// " " + possibilities);
 					// Unmögliche Möglichkeiten entfernen
 					possibilities = erasePossibilitiesInRow(row,
 							row.getPossibilities());
 					row.setPossibilities(possibilities);
 					// System.out.println("Pos2 size:" + possibilities.size() +
 					// " " + possibilities);
 					int posibillitiesAfter = possibilities.size();
 					if (possibilities.size() > 0) {
 						if (possibilities.size() == 1) {
 							fillListIntoMatrixInRow(getIndexOfRow(row),
 									possibilities.get(0));
 						} else {
 							// TODO: 
 							System.out.println(possibilities);
 							schnittmengeFindenInReihe(getIndexOfRow(row),
 									possibilities);
 						}
 					} else {
 						throw new NotSolvableException("Not solvable!\n"
 								+ "Row " + getIndexOfRow(row) + ":\n" + row);
 					}
 					int difference = posibillitiesBefore - posibillitiesAfter;
 					// System.out.println("Difference:" + difference);
 					if (difference > 0) {
 						run = true;
 					}
 					// System.out.println("------------");
 				} else {
 					fillRowWithFree(row);
 				}
 			}
 			for (Column column : getColumns()) {
 				System.out.println("Column:" + getIndexOfColumn(column));
 				// showMatrix();
 				ArrayList<LinkedList<String>> possibilities = column
 						.getPossibilities();
 				// System.out.println("Pos1:" + possibilities);
 				if (column.getBlocks().size() > 0 && !column.isGone()) {
 					LinkedList<String> firstConditionOfRow = getFirstConditionOfColumn(
 							column.getBlocks(), null, 0);
 					if (possibilities == null || possibilities.size() == 0) {
 						possibilities = getPossibilitiesForRowOrColumn(
 								column.getBlocks(), firstConditionOfRow,
 								possibilities, 0, 0);
 						possibilities.add(getFirstConditionOfColumn(
 								column.getBlocks(), null, 0));
 					}
 					column.setPossibilities(possibilities);
 					int posibillitiesBefore = possibilities.size();
 					// System.out.println("Pos1 size:" + possibilities.size()
 					// + " " + possibilities);
 					possibilities = erasePossibilitiesInColumn(column,
 							column.getPossibilities());
 					column.setPossibilities(possibilities);
 					// System.out.println("Pos2 size:" + possibilities.size()
 					// + " " + possibilities);
 					int posibillitiesAfter = possibilities.size();
 					if (possibilities.size() > 0) {
 						if (possibilities.size() == 1) {
 							fillListIntoMatrixInColumn(
 									getIndexOfColumn(column),
 									possibilities.get(0));
 						} else {
 							System.out.println(possibilities);
 							schnittmengeFindenInSpalte(
 									getIndexOfColumn(column), possibilities);
 						}
 					} else {
 						throw new Exception("Not solvable!\n" + "Column "
 								+ getIndexOfColumn(column) + ":\n" + column);
 					}
 					int difference = posibillitiesBefore - posibillitiesAfter;
 					// System.out.println("Difference:" + difference);
 					if (difference > 0) {
 						run = true;
 					}
 					// System.out.println("+++++++++++++++++++++");
 				}
 			}
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 	}
 
 	/**
 	 * Findet chars, die in allen posibilities gesetzt sind.
 	 * 
 	 * @param indexOfRow
 	 * @param possibilities
 	 * @throws Exception
 	 */
 	private void schnittmengeFindenInReihe(int indexOfRow,
 			ArrayList<LinkedList<String>> possibilities) throws Exception {
 		// 1. Möglichkeit herausnehmen und Indeces in result schreiben.
 		LinkedList<String> firstLinkedList = possibilities.get(0);
 		ArrayList<Integer> results = new ArrayList<Integer>();
		for (int j = 0; j < firstLinkedList.size(); j++) {
 			results.add(new Integer(j));
 		}
 		// wenn strings an derselben Stelle nicht übereinstimmen herausstreichen
 		// aus results
 		for (LinkedList<String> list : possibilities) {
 			for (int i = 0; i < riddle.getWidth(); i++) {
 				if (!firstLinkedList.get(i).equals(list.get(i))) {
 					results.remove(new Integer(i));
 				}
//				if (!firstLinkedList.get(i).equals(String.valueOf(matrix[indexOfRow][i]))) {
//					results.remove(new Integer(i));
//				}
 			}
 		}
 		for (Integer index : results) {
 			writeCharInMatrix(indexOfRow, firstLinkedList.get(index).charAt(0),
 					index);
 		}
 	}
 
 	/**
 	 * Findet chars, die in allen posibilities gesetzt sind.
 	 * 
 	 * @param indexOfColumn
 	 * @param possibilities
 	 * @throws Exception
 	 */
 	private void schnittmengeFindenInSpalte(int indexOfColumn,
 			ArrayList<LinkedList<String>> possibilities) throws Exception {
 		// 1. Möglichkeit herausnehmen und Indeces in result schreiben.
 		LinkedList<String> firstLinkedList = possibilities.get(0);
 		ArrayList<Integer> results = new ArrayList<Integer>();
 		for (int j = 0; j < riddle.getHeight(); j++) {
 			results.add(new Integer(j));
 		}
 		// wenn strings an derselben Stelle nicht übereinstimmen herausstreichen
 		// aus results
 		for (LinkedList<String> list : possibilities) {
			for (int i = 0; i < list.size(); i++) {
 				if (!firstLinkedList.get(i).equals(list.get(i))) {
 					results.remove(new Integer(i));
 				}
//				if (!firstLinkedList.get(i).equals(String.valueOf(matrix[i][indexOfColumn]))) {
//					results.remove(new Integer(i));
//				}
 			}
 		}
 		for (Integer index : results) {
 			writeCharInMatrix(index, firstLinkedList.get(index).charAt(0),
 					indexOfColumn);
 		}
 	}
 
 	/**
 	 * Schreibt alle Möglichkeiten die Reihe oder Spalte mit den Blöcken zu
 	 * belegen. Die Methode ruft sich selber auf, um zwischen den Blöcken zu
 	 * wechseln.
 	 * 
 	 * @param blocks1
 	 * @param aa
 	 * @param possibilities
 	 * @param numberOfBlock
 	 * @param add1
 	 * @return
 	 */
 	private ArrayList<LinkedList<String>> getPossibilitiesForRowOrColumn(
 			LinkedList<Block> blocks1, LinkedList<String> aa,
 			ArrayList<LinkedList<String>> possibilities, int numberOfBlock,
 			int add1) {
 		// String methodName = "getPossibilitiesForRowOrColumn()";
 		// System.out.println(methodName);
 		long startTime = new Date().getTime();
 		int add = add1;
 		LinkedList<Block> blocks = new LinkedList<Block>(blocks1);
 		ArrayList<LinkedList<String>> possibilities2 = new ArrayList<LinkedList<String>>(
 				possibilities);
 		Block block = blocks.get(numberOfBlock);
 		int numberOfBlock2 = numberOfBlock + 1;
 		int start = block.getMinStartIndex() + add;
 		for (int i = start; i + block.getHowMany() <= block.getMaxEndIndex(); i++) {
 			// iterativ die Blöcke durchgehen
 			LinkedList<String> bb = new LinkedList<String>(aa);
 			if (numberOfBlock2 < blocks.size()) {
 				possibilities2 = getPossibilitiesForRowOrColumn(blocks, bb,
 						possibilities2, numberOfBlock2, add);
 			}
 			//
 			if (aa.getLast().equals("-")) {
 				aa.removeLast();
 				aa.add(start, "-");
 				// System.out.println("aaaaa" + aa);
 				possibilities2.add(new LinkedList<String>(aa));
 			} else {
 				// System.out.println(aa);
 				possibilities2.add(new LinkedList<String>(aa));
 			}
 			add++;
 		}
 		// System.out.println("Time for " + methodName + ": " + (new
 		// Date().getTime() - startTime) + " ms");
 		// System.out.println("Possibilities:\n");
 		return possibilities2;
 	}
 
 	/**
 	 * Prüft, ob die Möglichkeit zu der Spielsituation passt.
 	 * 
 	 * TODO: mehr Abfragen.
 	 * 
 	 * @param possibility
 	 * @param rowInt
 	 * @return boolean
 	 */
 	private boolean isPossibilityGoodInRow(LinkedList<String> possibility,
 			int rowInt) {
 		String methodName = "isPossibilityGoodInRow()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		boolean isPossible = true;
 
 		for (int i = 0; i < riddle.getWidth(); i++) {
 			char charInMatrix = matrix[rowInt][i];
 			char charInPoss = possibility.get(i).charAt(0);
 			if (charInMatrix != charInPoss && charInMatrix != '*') {
 				isPossible = false;
 			}
 			if (charInMatrix == '*' && getColumns().get(i).isGone()
 					&& charInPoss != '-') {
 				isPossible = false;
 			}
			if (!checkPossibilityOfRowAgainstColumns(possibility, rowInt)) {
				isPossible = false;
			}
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 		return isPossible;
 	}
 
 	/**
 	 * Prüft, ob die Möglichkeit zu der Spielsituation passt.
 	 * 
 	 * TODO: mehr Abfragen.
 	 * 
 	 * @param possibility
 	 * @param rowInt
 	 * @return boolean
 	 */
 	private boolean isPossibilityGoodInColumn(LinkedList<String> possibility,
 			int columnInt) {
 		String methodName = "isPossibilityGoodInColumn()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		boolean isPossible = true;
 
 		for (int i = 0; i < riddle.getHeight(); i++) {
 			char charInMatrix = matrix[i][columnInt];
 			char charInPoss = possibility.get(i).charAt(0);
 			if (charInMatrix != charInPoss && charInMatrix != '*') {
 				isPossible = false;
 			}
 			if (charInMatrix == '*' && getRows().get(i).isGone()
 					&& charInPoss != '-') {
 				isPossible = false;
 			}
 			if (!checkPossibilityOfColumnAgainstRows(possibility, columnInt)) {
 				isPossible = false;
 			}
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 		return isPossible;
 	}
 
 	// TODO row setEntries mit char...
 	private boolean checkPossibilityOfRowAgainstColumns(
 			LinkedList<String> possibility, int rowInt) {
 		// String methodName = "checkPossibilityOfRowAgainstColumns()";
 		// System.out.println(methodName);
 		// long startTime = new Date().getTime();
 		boolean isPossible = true;
 		// String (Splaten durchgehen)
 		for (int columnInt = 0; columnInt < riddle.getWidth(); columnInt++) {
 			Column column = getColumns().get(
 					columnInt);
 			ArrayList<LinkedList<String>> possibilitiesOfColumn = column.getPossibilities();
 			if (isPossible && possibilitiesOfColumn != null && possibilitiesOfColumn.size() > 0) {
 				boolean isPossibleInColumn = false;
 				for (LinkedList<String> possibilityOfColumn : possibilitiesOfColumn) {
 					if (possibilityOfColumn.get(rowInt).equals(
 							possibility.get(columnInt))) {
 						isPossibleInColumn = true;
 					}
 				}
 				if (isPossible) {
 					isPossible = isPossibleInColumn;
 				}
 			}
 			// if (isPossible) {
 			// char[][] checkMatrix = matrix;
 			// checkMatrix = fillListIntoMatrixInRow(checkMatrix, rowInt,
 			// possibility);
 			// isPossible = checkIfColumnsStateIsPossible(checkMatrix,
 			// columnInt);
 			// }
 		}
 		// System.out.println("Time for " + methodName + ": " + (new
 		// Date().getTime() - startTime) + " ms");
 		return isPossible;
 	}
 
 	// TODO row setEntries mit char...
 	private boolean checkPossibilityOfColumnAgainstRows(
 			LinkedList<String> possibility, int columnInt) {
 		// String methodName = "checkPossibilityOfColumnAgainstRows()";
 		// System.out.println(methodName);
 		// long startTime = new Date().getTime();
 		boolean isPossible = true;
 		// String (Splaten durchgehen)
 		for (int rowInt = 0; rowInt < riddle.getHeight(); rowInt++) {
 			ArrayList<LinkedList<String>> possibilities = getRows().get(rowInt)
 					.getPossibilities();
 			if (isPossible && possibilities != null && possibilities.size() > 0) {
 				boolean isPossibleInColumn = false;
 				for (LinkedList<String> possibilityOfColumn : possibilities) {
 					if (possibilityOfColumn.get(columnInt).equals(
 							possibility.get(rowInt))) {
 						isPossibleInColumn = true;
 					}
 				}
 				if (isPossible) {
 					isPossible = isPossibleInColumn;
 				}
 			}
 			// if (isPossible) {
 			// char[][] checkMatrix = matrix;
 			// checkMatrix = fillListIntoMatrixInRow(checkMatrix, rowInt,
 			// possibility);
 			// isPossible = checkIfColumnsStateIsPossible(checkMatrix,
 			// columnInt);
 			// }
 		}
 		// System.out.println("Time for " + methodName + ": " + (new
 		// Date().getTime() - startTime) + " ms");
 		return isPossible;
 	}
 
 	private boolean checkIfColumnsStateIsPossible(char[][] checkMatrix,
 			int columnInt) {
 		String methodName = "checkIfColumnsStateIsPossible()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		boolean isPossible = true;
 
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 		return isPossible;
 	}
 
 	/**
 	 * Löscht eine Möglichkeit aus der Liste, nachdem sie in
 	 * {@link #isPossibilityGoodInRow(LinkedList, int)} geprüft wurde.
 	 * 
 	 * @param row
 	 * @param possibilities
 	 * @return ArrayList<LinkedList<String>> weniger .
 	 */
 	private ArrayList<LinkedList<String>> erasePossibilitiesInRow(Row row,
 			ArrayList<LinkedList<String>> possibilities) {
 		String methodName = "erasePossibilitiesInRow()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		ArrayList<LinkedList<String>> possibilities2 = new ArrayList<LinkedList<String>>(
 				possibilities);
 		System.out.println("erasePossibilitiesInRow()");
 		System.out.println("Row:" + getIndexOfRow(row));
 		for (LinkedList<String> possibility : possibilities) {
 			System.out.println(possibility);
 			boolean possible = isPossibilityGoodInRow(possibility,
 					getIndexOfRow(row));
 			if (!possible) {
 				possibilities2.remove(possibility);
 				System.out.println(possibility + " erased");
 			}
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 		return possibilities2;
 	}
 
 	/**
 	 * Löscht eine Möglichkeit aus der Liste, nachdem sie in
 	 * {@link #isPossibilityGoodInColumn(LinkedList, int)} geprüft wurde.
 	 * 
 	 * @param row
 	 * @param possibilities
 	 * @return ArrayList<LinkedList<String>> weniger .
 	 */
 	private ArrayList<LinkedList<String>> erasePossibilitiesInColumn(
 			Column column, ArrayList<LinkedList<String>> possibilities) {
 		String methodName = "erasePossibilitiesInColumn()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		ArrayList<LinkedList<String>> possibilities2 = new ArrayList<LinkedList<String>>(
 				possibilities);
 		for (LinkedList<String> possibility : possibilities) {
 			// System.out.println(possibility);
 			boolean possible = isPossibilityGoodInColumn(possibility,
 					getIndexOfColumn(column));
 			if (!possible) {
 				possibilities2.remove(possibility);
 				// System.out.println(possibility + " erased");
 			}
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 		return possibilities2;
 	}
 
 	// Helper
 
 	/**
     * @param asd
     * @param blocks
     * @param lastBlock
     * @param resultIndex
     * @return
     */
    private LinkedList<String> getFirstConditionOfRow(LinkedList<Block> blocks,
          Block lastBlock, int resultIndex) {
       String methodName = "getFirstConditionOfRow()";
       System.out.println(methodName);
       long startTime = new Date().getTime();
       LinkedList<String> asd = new LinkedList<String>();
       for (Block block : blocks) {
          if (null != lastBlock
                && lastBlock.getColour().getName() == block.getColour()
                      .getName()) {
             asd.add("-");
             resultIndex++;
          }
 
          for (int i = 0; i < block.getHowMany(); i++) {
             asd.add(String.valueOf(block.getColour().getName()));
          }
 
          resultIndex += block.getHowMany();
          lastBlock = block;
       }
       for (int i = resultIndex; i < riddle.getWidth(); i++) {
          asd.add("-");
       }
       // System.out.println("firstCond:" + asd);
       System.out.println("Time for " + methodName + ": "
             + (new Date().getTime() - startTime) + " ms");
       return asd;
    }
    
    /**
     * @param asd
     * @param blocks
     * @param lastBlock
     * @param resultIndex
     * @return
     */
    private LinkedList<String> getFirstConditionOfColumn(LinkedList<Block> blocks,
          Block lastBlock, int resultIndex) {
       String methodName = "getFirstConditionOfRow()";
       System.out.println(methodName);
       long startTime = new Date().getTime();
       LinkedList<String> asd = new LinkedList<String>();
       for (Block block : blocks) {
          if (null != lastBlock
                && lastBlock.getColour().getName() == block.getColour()
                      .getName()) {
             asd.add("-");
             resultIndex++;
          }
 
          for (int i = 0; i < block.getHowMany(); i++) {
             asd.add(String.valueOf(block.getColour().getName()));
          }
 
          resultIndex += block.getHowMany();
          lastBlock = block;
       }
      for (int i = resultIndex; i < riddle.getHeight(); i++) {
          asd.add("-");
       }
       // System.out.println("firstCond:" + asd);
       System.out.println("Time for " + methodName + ": "
             + (new Date().getTime() - startTime) + " ms");
       return asd;
    }
 
 	/**
 	 * Display the matrix.
 	 * 
 	 * @param matrix
 	 * @throws Exception
 	 */
 	private void showMatrix() {
 		String methodName = "showMatrix()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		for (int i = 0; i < riddle.getHeight(); i++) {
 			String out = "";
 			for (int j = 0; j < riddle.getWidth(); j++) {
 				out += matrix[i][j];
 				out += "  ";
 			}
 			System.out.println(out);
 		}
 		System.out.println();
 		// showBlockGoneTrue(matrix);
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 	}
 
 	private void showBlockGoneTrue() {
 		String methodName = "showBlockGoneTrue()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		for (int rowInt = 0; rowInt < riddle.getHeight(); rowInt++) {
 			Row row = getRows().get(rowInt);
 			System.out.println("Row:" + rowInt + " -- " + row.isGone());
 			LinkedList<Block> blocks = row.getBlocks();
 			if (null != blocks) {
 				for (Block block : blocks) {
 					System.out.println(block);
 				}
 			}
 		}
 		for (int columnInt = 0; columnInt < riddle.getWidth(); columnInt++) {
 			Column column = getColumns().get(columnInt);
 			System.out
 					.println("Column:" + columnInt + " -- " + column.isGone());
 			LinkedList<Block> blocks = column.getBlocks();
 			if (null != blocks) {
 				for (Block block : blocks) {
 					System.out.println(block);
 				}
 			}
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 	}
 
 	/**
 	 * Füllt die Spalte mit '-'.
 	 * 
 	 * @param column
 	 */
 	private void fillColumnWithFree(Column column) {
 		String methodName = "fillColumnWithFree()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		for (int row = 0; row < riddle.getHeight(); row++) {
 			int indexOfColumn = getIndexOfColumn(column);
 			if (matrix[row][indexOfColumn] == '*') {
 				matrix[row][indexOfColumn] = '-';
 			}
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 	}
 
 	/**
 	 * Füllt die Reihe mit '-'.
 	 * 
 	 * @param row
 	 */
 	private void fillRowWithFree(Row row) {
 		String methodName = "fillRowWithFree()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		for (int column = 0; column < riddle.getWidth(); column++) {
 			int indexOfRow = getIndexOfRow(row);
 			if (matrix[indexOfRow][column] == '*') {
 				matrix[indexOfRow][column] = '-';
 			}
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 	}
 
 	/**
 	 * Setzt die initialen Werte für minStartIndex und maxStartIndex für eine
 	 * Reihe oder Spalte.
 	 * 
 	 * @param blocks
 	 * @param size
 	 */
 	private void setupBlocksInRowAndColumn(LinkedList<Block> blocks, int size) {
 		String methodName = "setupBlocksInRowAndColumn()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		for (int i = 0; i < blocks.size(); i++) {
 			Block block = blocks.get(i);
 			if (blocks.size() == 0) {
 				block.setMinStartIndex(0);
 				block.setMaxEndIndex(size - 1);
 			} else if (blocks.size() == 1) {
 			   block.setMinStartIndex(0);
             block.setMaxEndIndex(size - 1);
 			} else {
 				if (i == 0) {
 					block.setMinStartIndex(0);
 					block.setMaxEndIndex(getMaxEndIndexOfBlock(blocks, i, size));
 				} else if (i == blocks.size() - 1) {
 					block.setMinStartIndex(getMinStartIndexOfBlock(blocks, i,
 							size));
 					block.setMaxEndIndex(size - 1);
 				} else {
 					block.setMinStartIndex(getMinStartIndexOfBlock(blocks, i,
 							size));
 					block.setMaxEndIndex(getMaxEndIndexOfBlock(blocks, i, size));
 				}
 			}
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 	}
 
 	/**
 	 * Berechnet den minimalen Startindex eines Blockes, indem die Größen der
 	 * vorherigen Blöcke unter Berücksichtigung etwaiger Zwischenräume addiert
 	 * werden.
 	 * 
 	 * @param blocks
 	 * @param indexOfBlock
 	 * @param widthOfRiddle
 	 * @return
 	 */
 	private int getMinStartIndexOfBlock(LinkedList<Block> blocks,
 			int indexOfBlock, int widthOfRiddle) {
 		String methodName = "getMinStartIndexOfBlock()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		int index = 0;
 		Block lastBlock = blocks.get(indexOfBlock);
 		for (int i = indexOfBlock - 1; i >= 0; i--) {
 			Block block = blocks.get(i);
 			index += block.getHowMany();
 			if (null != lastBlock
 					&& lastBlock.getColour().getName() == block.getColour()
 							.getName()) {
 				index++;
 			}
 			lastBlock = block;
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 		return index;
 	}
 
 	/**
 	 * Berechnet den maximalen Index für einen Block.
 	 * 
 	 * @param blocks
 	 * @param indexOfBlock
 	 * @param size
 	 * @return
 	 */
 	private int getMaxEndIndexOfBlock(LinkedList<Block> blocks,
 			int indexOfBlock, int size) {
 		String methodName = "getMaxEndIndexOfBlock()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		int index = 0;
 		Block lastBlock = blocks.get(indexOfBlock);
 		for (int i = indexOfBlock + 1; i < blocks.size(); i++) {
 			Block block = blocks.get(i);
 			index += block.getHowMany();
 			if (lastBlock.getColour().getName() == block.getColour().getName()) {
 				index++;
 			}
 			lastBlock = block;
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 		return (size - 1 - index);
 	}
 
 	/**
 	 * Gibt alle Spalten des Rätsels zurück.
 	 * 
 	 * @return
 	 */
 	private LinkedList<Column> getColumns() {
 		return riddle.getColumns();
 	}
 
 	/**
 	 * Gibt alle Reihen des Rätsels zurück.
 	 * 
 	 * @return
 	 */
 	private LinkedList<Row> getRows() {
 		return riddle.getRows();
 	}
 
 	private int getIndexOfColumn(Column column) {
 		return getColumns().indexOf(column);
 	}
 
 	private int getIndexOfRow(Row row) {
 		return getRows().indexOf(row);
 	}
 
 	/**
 	 * Füllt den Bereich in der column zwischen rowBegin (inklusive) und rowEnd
 	 * (exklusive) mit dem char c.
 	 * 
 	 * @param matrix
 	 * @param columnIndex
 	 * @param rowBegin
 	 * @param rowEnd
 	 * @param c
 	 * @return
 	 * @throws Exception
 	 */
 	private char[][] fillAreaInColumnWithChar(int columnIndex, int rowBegin,
 			int rowEnd, char c) throws Exception {
 		String methodName = "fillAreaInColumnWithChar()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		for (int row = rowBegin; row < rowEnd; row++) {
 			writeCharInMatrix(row, c, columnIndex);
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 		return matrix;
 	}
 
 	/**
 	 * Füllt den Bereich in der row zwischen columnBegin (inklusive) und
 	 * columnEnd (exklusive) mit dem char c.
 	 * 
 	 * @param matrix
 	 * @param columnIndex
 	 * @param rowBegin
 	 * @param rowEnd
 	 * @param c
 	 * @return
 	 * @throws Exception
 	 */
 	private void fillAreaInRowWithChar(int rowIndex, int columnBegin,
 			int columnEnd, char c) throws Exception {
 		String methodName = "fillAreaInRowWithChar()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		for (int column = columnBegin; column < columnEnd; column++) {
 			writeCharInMatrix(rowIndex, c, column);
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 	}
 
 	/**
 	 * Füllt die Stelle in der Matrix.
 	 * 
 	 * @param rowIndex
 	 *            Nummer der Reihe in der Matrix.
 	 * @param c
 	 *            Farbe
 	 * @param columnIndex
 	 *            rowIndex Nummer der Reihe in der Matrix.
 	 * @throws Exception
 	 *             falls an der Stelle bereits ein anderer char als c oder '*'
 	 *             steht.
 	 */
 	private void writeCharInMatrix(int rowIndex, char c, int columnIndex)
 			throws Exception {
 		String methodName = "writeCharInMatrix()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		if (matrix[rowIndex][columnIndex] != '*'
 				&& matrix[rowIndex][columnIndex] != c) {
 			throw new DataCollisionException("Fehler: row:" + rowIndex
 					+ " column:" + columnIndex + " " + c + " ungleich "
 					+ matrix[rowIndex][columnIndex]);
 		}
 		if (matrix[rowIndex][columnIndex] != c) {
 			matrix[rowIndex][columnIndex] = c;
 			if (matrix[rowIndex][columnIndex] != '-') {
 				if (getRows().get(rowIndex).setEntriesSet()) {
 					fillRowWithFree(getRows().get(rowIndex));
 					LinkedList<String> list = new LinkedList<String>();
 					ArrayList<LinkedList<String>> list2 = new ArrayList<LinkedList<String>>();
 					for (int column = 0; column < riddle.getWidth(); column++) {
 						list.add(String.valueOf(matrix[rowIndex][column]));
 					}
 					list2.add(list);
 					getRows().get(rowIndex).setPossibilities(list2);
 				}
 				if (getColumns().get(columnIndex).setEntriesSet()) {
 					fillColumnWithFree(getColumns().get(columnIndex));
 					LinkedList<String> list = new LinkedList<String>();
 					ArrayList<LinkedList<String>> list2 = new ArrayList<LinkedList<String>>();
 					for (int row = 0; row < riddle.getHeight(); row++) {
 						list.add(String.valueOf(matrix[row][columnIndex]));
 					}
 					list2.add(list);
 					getColumns().get(columnIndex).setPossibilities(list2);
 				}
 			}
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 	}
 
 	/**
 	 * Füllt den Inhalt einer zutreffenden Möglichkeit aus
 	 * {@link #solveRecursive()} in die Matrix.
 	 * 
 	 * @param row
 	 * @param possibilitiy
 	 * @throws Throwable
 	 */
 	private void fillListIntoMatrixInRow(int row,
 			LinkedList<String> possibilitiy) throws Exception {
 		String methodName = "fillListIntoMatrixInRow()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		getRows().get(row).setGone(true);
 		for (int i = 0; i < riddle.getWidth(); i++) {
 			char c = possibilitiy.get(i).charAt(0);
 			if (matrix[row][i] != c) {
 				matrix[row][i] = c;
 				if (c != '-') {
 					if (getRows().get(row).setEntriesSet()) {
 						fillRowWithFree(getRows().get(row));
 					}
 					if (getColumns().get(i).setEntriesSet()) {
 						fillColumnWithFree(getColumns().get(i));
 					}
 				}
 			}
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 	}
 
 	/**
 	 * Füllt den Inhalt einer zutreffenden Möglichkeit aus
 	 * {@link #solveRecursive()} in die Matrix.
 	 * 
 	 * @param row
 	 * @param possibilitiy
 	 * @throws Throwable
 	 */
 	private char[][] fillListIntoMatrixInRow(char[][] checkMatrix, int row,
 			LinkedList<String> possibilitiy) throws Exception {
 		String methodName = "fillListIntoMatrixInRow()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		getRows().get(row).setGone(true);
 		for (int i = 0; i < riddle.getWidth(); i++) {
 			char c = possibilitiy.get(i).charAt(0);
 			if (checkMatrix[row][i] != c) {
 				checkMatrix[row][i] = c;
 				if (c != '-') {
 					if (getRows().get(row).setEntriesSet()) {
 						fillRowWithFree(getRows().get(row));
 					}
 					if (getColumns().get(i).setEntriesSet()) {
 						fillColumnWithFree(getColumns().get(i));
 					}
 				}
 			}
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 		return checkMatrix;
 	}
 
 	/**
 	 * Füllt den Inhalt einer zutreffenden Möglichkeit aus
 	 * {@link #solveRecursive()} in die Matrix.
 	 * 
 	 * @param row
 	 * @param possibilitiy
 	 */
 	private void fillListIntoMatrixInColumn(int column,
 			LinkedList<String> possibilities) {
 		String methodName = "fillListIntoMatrixInColumn()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		getColumns().get(column).setGone(true);
 		for (int i = 0; i < riddle.getHeight(); i++) {
 			char c = possibilities.get(i).charAt(0);
 			if (matrix[i][column] != c) {
 				matrix[i][column] = c;
 				if (c != '-') {
 					if (getRows().get(i).setEntriesSet()) {
 						fillRowWithFree(getRows().get(i));
 					}
 					if (getColumns().get(column).setEntriesSet()) {
 						fillColumnWithFree(getColumns().get(column));
 					}
 				}
 			}
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 	}
 
 	/**
 	 * Gibt die Anzahl der nicht belegten Felder zurück.
 	 * 
 	 * @return
 	 */
 	private int getStarCountInRiddle() {
 		String methodName = "getStarCountInRiddle()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		int starCount = 0;
 
 		for (Row row : getRows()) {
 			for (int i = 0; i < riddle.getWidth(); i++) {
 				if (matrix[getIndexOfRow(row)][i] == '*') {
 					starCount++;
 				}
 			}
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 		return starCount;
 	}
 
 	private int getRowWithSmallestSizesOfPossibilities() {
 		String methodName = "getSizesOfPossibilities()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		int smallestRowInt = 0;
 		Integer sizesmallestRowInt = null;
 		for (Row row : getRows()) {
 			int size = row.getPossibilities().size();
 			if (sizesmallestRowInt == null
 					|| (size < sizesmallestRowInt && size != 1)) {
 				sizesmallestRowInt = size;
 				smallestRowInt = getIndexOfRow(row);
 			}
 			System.out.println("Row " + getIndexOfRow(row)
 					+ " pssibilities size:" + size);
 		}
 		return smallestRowInt;
 	}
 
 	private void getSizesOfPossibilities() {
 		String methodName = "getSizesOfPossibilities()";
 		System.out.println(methodName);
 		long startTime = new Date().getTime();
 		for (Row row : getRows()) {
 			System.out.println("Row " + getIndexOfRow(row)
 					+ " pssibilities size:" + row.getPossibilities().size());
 			System.out.println(row.getPossibilities());
 		}
 		for (Column column : getColumns()) {
 			System.out.println("Column " + getIndexOfColumn(column)
 					+ " pssibilities size:" + column.getPossibilities().size());
 			System.out.println(column.getPossibilities());
 			System.out.println();
 		}
 		System.out.println("Time for " + methodName + ": "
 				+ (new Date().getTime() - startTime) + " ms");
 	}
 
 }
