 package com.arykow.applications.solver;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.arykow.applications.solver.BoardFormatterFactory.BoardFormatter;
 import com.arykow.applications.solver.visitors.BoardVisitor;
 import com.arykow.applications.solver.visitors.CellVisitor;
 import com.arykow.applications.solver.visitors.CellsVisitor;
 import com.arykow.applications.solver.visitors.RegionVisitor;
 import com.arykow.applications.solver.visitors.RegionsVisitor;
 
 public class Board {
 	public class Cell {
 		private Integer value;
 		private final Region row;
 		private final Region column;
 		private final Region square;
 
 		public Cell(Integer value, Region row, Region column, Region square) {
 			super();
 			this.value = value;
 			this.row = row;
 			this.column = column;
 			this.square = square;
 		}
 
 		public void acceptCellVisitor(CellVisitor visitor) {
 			if (visitor != null) {
 				visitor.visitValue(value);
 				row.acceptRegionVisitor(visitor.createRowRegionVisitor());
 				column.acceptRegionVisitor(visitor.createColumnRegionVisitor());
 				square.acceptRegionVisitor(visitor.createSquareRegionVisitor());
 			}
 		}
 
 		public void updateValue(int value) {
 			this.value = value;
 		}
 	}
 
 	public class Cells {
 		private int index = 0;
 		private Map<Integer, Cell> cells = new HashMap<Integer, Cell>(size * size * size * size);
 
 		public void insertCell(Integer value, Region row, Region column, Region square) {
 			Cell cell = new Cell(value, row, column, square);
 			row.addItem(cell);
 			column.addItem(cell);
 			square.addItem(cell);
 			cells.put(index++, cell);
 		}
 
 		public void acceptCellsVisitor(CellsVisitor visitor) {
 			if (visitor != null) {
 				for (Integer index : cells.keySet()) {
 					cells.get(index).acceptCellVisitor(visitor.createCellVisitor(index));
 				}
 			}
 		}
 
 		private void updateValue(int index, int value) {
 			cells.get(index).updateValue(value);
 		}
 	}
 
 	public class Region {
 		private List<Cell> cells = new ArrayList<Cell>(size * size);
 
 		public Region() {
 			super();
 		}
 
 		public void addItem(Cell cell) {
 			cells.add(cell);
 		}
 
 		public void acceptRegionVisitor(RegionVisitor visitor) {
 			if (visitor != null) {
 				int index = 0;
 				for (Cell cell : cells) {
 					cell.acceptCellVisitor(visitor.createCellVisitor(index++));
 				}
 			}
 		}
 	}
 
 	public class Regions {
 		private final Map<Integer, Region> regions = new HashMap<Integer, Region>(size * size);
 
 		public Regions() {
 			super();
 		}
 
 		public Region getRegion(int index) {
 			if (!regions.containsKey(index)) {
 				regions.put(index, new Region());
 			}
 			return regions.get(index);
 		}
 
 		public void acceptRegionsVisitor(RegionsVisitor visitor) {
 			if (visitor != null) {
 				for (Integer index : regions.keySet()) {
 					regions.get(index).acceptRegionVisitor(visitor.createRegionVisitor(index));
 				}
 			}
 		}
 	}
 
 	private final int size;
 	private final Regions rows = new Regions();
 	private final Regions columns = new Regions();
 	private final Regions squares = new Regions();
 	private final Cells cells = new Cells();
 
 	public Board(int size) {
 		this.size = size;
 	}
 
 	public Board(String values) {
 		this(calculateSize(values));
 		for (int a = 0; a < size; a++) {
 			for (int b = 0; b < size; b++) {
 				for (int c = 0; c < size; c++) {
 					for (int d = 0; d < size; d++) {
 						int rowIndex = a * size + b;
 						int columnIndex = c * size + d;
 						int squareIndex = a * size + c;
 						char value = values.charAt(a * size * size * size + b * size * size + c * size + d);
 						addValue(rowIndex, columnIndex, squareIndex, value == ' ' ? null : Integer.parseInt(new String(new char[] { value })));
 					}
 				}
 			}
 		}
 	}
 
 	private static int calculateSize(String values) {
 		if (values == null) {
 			throw new RuntimeException("The string can't be null.");
 		}
 		int result = Double.valueOf(Math.sqrt(Math.sqrt(values.length()))).intValue();
 		if (result * result * result * result != values.length()) {
 			throw new RuntimeException(String.format("The string \"%s\" is not valid.", values));
 		}
 		return result;
 	}
 
 	private void addValue(int rowIndex, int columnIndex, int squareIndex, Integer value) {
 		cells.insertCell(value, rows.getRegion(rowIndex), columns.getRegion(columnIndex), squares.getRegion(squareIndex));
 	}
 
 	public void acceptBoardVisitor(BoardVisitor visitor) {
 		if (visitor != null) {
 			visitor.visitSize(size);
 			rows.acceptRegionsVisitor(visitor.createRowRegionsVisitor());
 			columns.acceptRegionsVisitor(visitor.createColumnRegionsVisitor());
 			squares.acceptRegionsVisitor(visitor.createSquareRegionsVisitor());
 			cells.acceptCellsVisitor(visitor.createCellsVisitor());
 		}
 	}
 
 	public String toString() {
 		return BoardFormatterFactory.newTableFormatter().format(this);
 	}
 
 	public Board cloneBoardAndUpdateValue(boolean normalize, int index, int value) {
 		Map<Integer, Integer> values = new HashMap<Integer, Integer>();
 		values.put(index, value);
 		return cloneBoardAndUpdateValues(normalize, values);
 	}
 
 	public Board cloneBoardAndUpdateValues(boolean normalize, Map<Integer, Integer> values) {
 		Board result = cloneBoard(false);
 		if(values != null) {
 			for (Integer index : values.keySet()) {
 				result.updateValue(index, values.get(index));
 			}
 		}
 		return result.cloneBoard(normalize);
 	}
 
 	private void updateValue(int index, int value) {
 		cells.updateValue(index, value);
 	}
 
 	public Board cloneBoard(boolean normalize) {
 		String result = createInlineString(false);
 		if(normalize) {
 			for(String next = createInlineString(true); !next.equals(result); next = new Board(result).createInlineString(true)) {
 				result = next;
 			}
 		}
 		return new Board(result);
 	}
 
 	private String createInlineString(boolean normalize) {
 		final char[] result = BoardFormatterFactory.newInlineFormatter().format(this).toCharArray();
 		if(normalize) {
 			EmptyInformationsVisitorBuilder.buildEmptyInformations(this).acceptEmptyInformationsVisitor(new EmptyInformationsVisitor() {
				boolean stop = false;
 				public void visitInformation(Integer length, Integer index, int value) {
					if(!stop && length == 1) {
 						result[index] = String.valueOf(value).charAt(0);
						stop = true;
 					}
 				}
 			});
 		}
 		return new String(result);
 	}
 	
 	public boolean isSolved() {
 		return EmptyInformationsVisitorBuilder.buildEmptyInformations(this).isSolved();
 	}
 
 	public boolean isValid() {
 		return EmptyInformationsVisitorBuilder.buildEmptyInformations(this).isValid();
 	}
 
 	public boolean equals(Object object) {
 		boolean result = object != null && object instanceof Board;
 		if (result) {
 			Board board = Board.class.cast(object);
 			BoardFormatter formatter = BoardFormatterFactory.newInlineFormatter();
 			result = formatter.format(this).equals(formatter.format(board));
 		}
 		return result;
 	}
 
 }
