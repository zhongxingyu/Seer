 package cardsAndDecks;
 
 import static com.google.common.base.Preconditions.checkState;
 
 import java.awt.Image;
 import java.util.Map;
 
 import board.Board.Direction;
 import board.Side;
 import cardsAndDecks.functions.CardType;
 
 import com.google.common.collect.Maps;
 
 public class Tile extends AbstractCard {
 
 	private Direction entryDirection;
 
 	private final Side entrySide;
 	private final Side rightOfEntrySide;
 	private final Side leftOfEntrySide;
 	private final Side oppositeOfEntrySide;
 
 	private final Map<Direction, Side> sideAtDirection = Maps
 			.newEnumMap(Direction.class);
 
 	private Side extractSide(String fileName, int codeIndex) {
 		if (fileName.charAt(codeIndex) == 'o') {
 			return Side.OPENING;
 		} else if (fileName.charAt(codeIndex) == 'w') {
 			return Side.WALL;
 		} else if (fileName.charAt(codeIndex) == 'd') {
 			return Side.DOOR;
 		} else if (fileName.charAt(codeIndex) == 'p') {
 			return Side.PORTCULLIS;
 		} else {
 			return Side.OPENING;
 		}
 	}
 
 	public Tile(String name, Image cardImage, CardType cardFunction) {
 		super(name, cardImage, TilePile.get(), cardFunction);
 
 		entrySide = extractSide(name, 3);
 		leftOfEntrySide = extractSide(name, 4);
 		oppositeOfEntrySide = extractSide(name, 5);
 		rightOfEntrySide = extractSide(name, 6);
 
 		sideAtDirection.put(Direction.NORTH, entrySide);
 		sideAtDirection.put(Direction.SOUTH, oppositeOfEntrySide);
 		sideAtDirection.put(Direction.EAST, leftOfEntrySide);
 		sideAtDirection.put(Direction.WEST, rightOfEntrySide);
 	}
 
 	public Side getSideFacingDirection(Direction direction) {
 		return sideAtDirection.get(direction);
 	}
 
 	public Side getEntrySide() {
 		return entrySide;
 	}
 
 	public Side getRightOfEntrySide() {
 		return rightOfEntrySide;
 	}
 
 	public Side getLeftOfEntrySide() {
 		return leftOfEntrySide;
 	}
 
 	public Side getOppositeOfEntrySide() {
 		return oppositeOfEntrySide;
 	}
 
 	public Direction getEntryDirection() {
 		return entryDirection;
 	}
 
 	public void setEntryDirection(Direction entryDirection) {
		this.entryDirection = entryDirection;

 		int turnsClockwise = 0;
 
 		switch (this.entryDirection) {
 		case NORTH:
 			switch (entryDirection) {
 			case WEST:
 				turnsClockwise++;
 			case SOUTH:
 				turnsClockwise++;
 			case EAST:
 				turnsClockwise++;
 			case NORTH:
 				break;
 			}
 			break;
 		case EAST:
 			switch (entryDirection) {
 			case NORTH:
 				turnsClockwise++;
 			case WEST:
 				turnsClockwise++;
 			case SOUTH:
 				turnsClockwise++;
 			case EAST:
 				break;
 			}
 			break;
 		case SOUTH:
 			switch (entryDirection) {
 			case WEST:
 				turnsClockwise++;
 			case NORTH:
 				turnsClockwise++;
 			case EAST:
 				turnsClockwise++;
 			case SOUTH:
 				break;
 			}
 			break;
 		case WEST:
 			switch (entryDirection) {
 			case SOUTH:
 				turnsClockwise++;
 			case EAST:
 				turnsClockwise++;
 			case NORTH:
 				turnsClockwise++;
 			case WEST:
 				break;
 			}
 			break;
 		}
 
 		checkState(turnsClockwise <= 3);
 
 		Side tempSide = null;
 		switch (turnsClockwise) {
 		case 2:
 			tempSide = sideAtDirection.get(Direction.SOUTH);
 			sideAtDirection.put(Direction.SOUTH,
 					sideAtDirection.get(Direction.NORTH));
 			sideAtDirection.put(Direction.NORTH, tempSide);
 
 			tempSide = sideAtDirection.get(Direction.EAST);
 			sideAtDirection.put(Direction.EAST,
 					sideAtDirection.get(Direction.WEST));
 			sideAtDirection.put(Direction.WEST, tempSide);
 
 			break;
 
 		case 1:
 			tempSide = sideAtDirection.get(Direction.NORTH);
 			sideAtDirection.put(Direction.NORTH,
 					sideAtDirection.get(Direction.WEST));
 			sideAtDirection.put(Direction.WEST,
 					sideAtDirection.get(Direction.SOUTH));
 			sideAtDirection.put(Direction.SOUTH,
 					sideAtDirection.get(Direction.EAST));
 			sideAtDirection.put(Direction.EAST, tempSide);
 
 			break;
 
 		case 3:
 			tempSide = sideAtDirection.get(Direction.NORTH);
 			sideAtDirection.put(Direction.NORTH,
 					sideAtDirection.get(Direction.EAST));
 			sideAtDirection.put(Direction.EAST,
 					sideAtDirection.get(Direction.SOUTH));
 			sideAtDirection.put(Direction.SOUTH,
 					sideAtDirection.get(Direction.WEST));
 			sideAtDirection.put(Direction.WEST, tempSide);
 
 			break;
 
 		case 0:
 		default:
 			break;
 		}
 	}
 
 }
