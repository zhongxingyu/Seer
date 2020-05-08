 package board;
 
 import java.awt.Point;
 import java.io.Serializable;
 import java.util.Arrays;
 
 import cell.Box;
 import cell.Cell;
 import cell.Character;
 import cell.ContainerCell;
 import cell.IceBlock;
 import cell.IceBlockTarget;
 import cell.MoveReturnValue;
 import cell.Target;
 import cell.Tree;
 import cell.Water;
 
 /** 
  * Clase que representa el tablero de juego.
  * 
  * @author santi698
  *
  */
 public class Board implements Serializable{
 
 	private static final long serialVersionUID = -6426728114234690441L;
 
 	/**
 	 * El contenido del tablero de modela con una matriz de listas, 
 	 * donde cada elemento es una capa del tablero
 	 */
 	public final int rows, columns;
 	private Cell[][] dataMatrix;
 	private Character character;
 	private Target targetCell;
 	
 	/**
 	 * Crea un tablero de las dimensiones especificadas.
 	 * @param rows Cantidad de filas
 	 * @param columns Cantidad de columnas
 	 */
 	Board(int rows, int columns) {
 		this.rows = rows;
 		this.columns = columns;
 		dataMatrix = new Cell[rows][columns];
 	}
 	/**
 	 * 
 	 * @param s Un array de {@code String}s donde cada elemento es una fila del tablero
 	 * @throws InvalidLevelException Cuando el nivel no respeta el formato estándar
 	 */
 	//TODO MODULARIZAR/REESCRIBIR
 	public Board(String[] s) throws InvalidLevelException {
 		this(s.length, s[0].length()); 
 		int chCount = 0, dCount = 0, ibCount = 0, intCount = 0;
 		for (int i = 0; i < rows; i++) {
 			if (s[i].length() != columns)
 				throw new InvalidLevelException("El tablero debe ser rectangular.");
 			for (int j = 0; j < columns; j++) {
 				char c = s[i].charAt(j);
 				Cell actualCell = charToCell(c);
 				dataMatrix[i][j] = actualCell;
 				switch (c) {
 				case '@': 
 					chCount++;
 					character = (Character)actualCell.getContent();
 					character.setPosition(new Point(j, i));
 					break;
 				case 'G': dCount++; targetCell = (Target) actualCell; break;
 				case 'C': 
 					ibCount++;
 					actualCell.getContent().setPosition(new Point(j, i));
 					break;
 					
 				case 'K': intCount++; break;
 				case 'B': actualCell.getContent().setPosition(new Point(j, i));
 				}
 			}
 		}
 		if (chCount != 1 || ibCount == 0 || dCount != 1 || intCount > 1)
 			throw new InvalidLevelException("Debe haber exactamente un personaje, un destino" +
 					", al menos un cubo de hielo, y no más de un interruptor." +
 					"\n Personajes: " + chCount + ", Destinos: " + dCount + ", Cubos de hielo: " +
 							"" + ibCount + ", Interruptores:" + intCount + ".");
 		}
 	//TODO Pasar a una clase aparte
 	private static Cell charToCell(char c) throws InvalidLevelException {
 		switch (c) {
 		case 'T': return new Tree();
 		case '#': return new Water();
 		case 'K': return new IceBlockTarget();
 		case 'C': return new ContainerCell(new IceBlock());
 		case 'B': return new ContainerCell(new Box());
 		case 'G': return new Target();
 		case '@': return new ContainerCell(new Character());
 		case ' ': return new ContainerCell();
 		default: throw new InvalidLevelException("Caracter Inválido.");
 		}
 	}
 	/**
 	 * Retorna la celda en la posición (x, y)
 	 * @param x 
 	 * @param y
 	 * @return la celda en la posición (x, y)
 	 */
 	public Cell getCell(int x, int y) {
		if (x < columns && y < rows)
 			return dataMatrix[y][x];
 		return null;
 	}
 	/**
 	 * Reemplaza el contenido de la celda en la posicion (x, y) por {@code cell}
 	 * @param x
 	 * @param y
 	 * @param cell la celda que va a reemplazar a la que esta en la posición (x, y)
 	 */
 	public void setCell(int x, int y, Cell cell) {
 		if (x < columns && y < rows)
 			dataMatrix[y][x] = cell;
 	}
 	/**
 	 * Obtiene una referencia a la celda destino del tablero.
 	 * @return La celda destino
 	 * @see {@link Target}
 	 */
 	public Cell getTargetCell() {
 		return targetCell;
 	}
 	/**
 	 * Para debugging
 	 */
 	@Override
 	public String toString() {
 		StringBuilder s = new StringBuilder();
 		for (Cell[] row : dataMatrix) {
 			s.append(Arrays.toString(row));
 			s.append("\n");
 		}
 		return s.toString();
 	}
 	/**
 	 * Mueve al personaje en la direccion especificada. 
 	 * @param direction
 	 * @return el resultado del movimiento 
 	 * @see {@link cell.MoveReturnValue}
 	 * @see {@link Direction}
 	 */
 	public MoveReturnValue moveCharacter(Direction direction) {
 		return character.move(this, direction);
 	}
 
 	
 }
