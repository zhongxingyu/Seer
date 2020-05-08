 package cell;
 
 import java.awt.Point;
 
 import board.Board;
 import board.Direction;
 
 /**
  * Clase que representa un cubo de hielo
  * @author santi698
  *
  */
 public class IceBlock extends CellContent {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	/**
 	 * Mueve el cubo de hielo. 
 	 * El mismo se debe mover hasta alcanzar una celda que no 
 	 * es {@link ContainerCell} o hasta alcanzar una de tipo {@link Water}
 	 * @param board
 	 * @param direction
 	 * @return El resultado del movimiento 
 	 * @see {@link MoveReturnValue}
 	 */
 	//TODO REVISAR!
 	public MoveReturnValue move(Board board, Direction direction) {
 		Cell nextCell = board.getCell(position.x + direction.x, position.y + direction.y);
 		if(!(nextCell instanceof ContainerCell) || !nextCell.isEmpty())
 			return MoveReturnValue.UNABLE_TO_MOVE;
 		while(nextCell instanceof ContainerCell && nextCell.isEmpty()) {
 			if (nextCell instanceof Water) {
 				board.getCell(position.x, position.y).setContent(null);
 				return MoveReturnValue.MOVED;
 			}
 			if (nextCell instanceof IceBlockTarget) {
				((IceBlockTarget) nextCell).setVisible();
 			}
 			board.getCell(position.x, position.y).setContent(null);
 			nextCell.setContent(this);
 			setPosition(new Point(position.x + direction.x, position.y + direction.y));
 			nextCell = board.getCell(position.x + direction.x, position.y + direction.y);
 		}
 		return MoveReturnValue.MOVED;
 	}
 	/**
 	 * Para debugging
 	 */
 	public String toString() {
 		return "Ice Block";
 	}
 
 }
