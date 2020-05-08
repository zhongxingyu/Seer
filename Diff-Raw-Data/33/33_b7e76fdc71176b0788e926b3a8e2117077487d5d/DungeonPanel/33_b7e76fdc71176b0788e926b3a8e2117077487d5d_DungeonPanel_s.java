 package front;
 
 import static professorShipSrc.ImageUtils.drawString;
 import static professorShipSrc.ImageUtils.loadImage;
 import static professorShipSrc.ImageUtils.overlap;
 
 import java.awt.Color;
 import java.awt.Image;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JOptionPane;
 
 import professorShipSrc.GamePanel;
 import back.BloodyFloor;
 import back.Bonus;
 import back.Character;
 import back.Floor;
 import back.Game;
 import back.Monster;
 import back.MoveTypes;
 import back.Point;
 import back.Putable;
 import back.Wall;
 
 /**
  * @author tmehdi Class that extends the professor ship class GamePanel. This
  *         class is used for the Dungeon panel that is into the
  *         DungeonGameFrame.
  */
 public class DungeonPanel extends GamePanel {
 
 	private static final long serialVersionUID = 1L;
 	static final int CELL_SIZE = 30;
 
 	private Image playerImage;
 	private Map<Class<? extends Putable>, Image> boardImagesByClass = new HashMap<Class<? extends Putable>, Image>();
 	private Map<String, Image> monsterImagesByName = new HashMap<String, Image>();
 	private Map<String, Image> bonusImagesByName = new HashMap<String, Image>();
 	private Monster monsterUnderMouse = null;
 
 	/**
 	 * @param game
 	 * @param dataPanel
 	 * @param dungeonListener
 	 *            Call the super constructor and draw the pane. The interface
 	 *            DungeonPanelListener that extends the professor ship interface
 	 *            GamePanelListener is used to make an implementation of the
 	 *            "onMouseMoved" method. It allows us to know in what cell is
 	 *            and make the different actions.
 	 */
 	public DungeonPanel(Game game, DataPanel dataPanel,
 			DungeonPanelListener dungeonListener) {
 		super(game.getBoardDimension().x - 2, game.getBoardDimension().y - 2,
 				CELL_SIZE, dungeonListener, Color.BLACK);
 		playerImage();
 		boardImagesByClass();
 		monstersImagesInitialize();
 		bonusImagesInitialize();
 		drawDungeon(game);
 		setVisible(true);
 	}
 
 	/**
 	 * @param monsterUnderMouse
 	 *            Setter of the monster under mouse.
 	 */
 	public void setMonsterUnderMouse(Monster monsterUnderMouse) {
 		this.monsterUnderMouse = monsterUnderMouse;
 	}
 
 	/**
 	 * @param dungeonGameFrame
 	 *            Draw the full dungeon panel.
 	 */
 	public void dwarFullDungeon(DungeonGameFrame dungeonGameFrame) {
 		Image image;
 		Image floorImage = boardImagesByClass.get(Floor.class);
 		Image bloodyFloorImage = overlap(floorImage, boardImagesByClass
 				.get(BloodyFloor.class));
 		int row = dungeonGameFrame.game.getBoardDimension().x - 2;
 		int col = dungeonGameFrame.game.getBoardDimension().y - 2;
 
 		for (int i = 1; i <= row; i++) {
 			for (int j = 1; j <= col; j++) {
 				Putable cell = dungeonGameFrame.game.getBoard()[i][j];
 				if (cell.getClass().equals(Monster.class)) {
 					image = monsterImagesByName.get(((Monster) cell)
 							.getMonsterType().toString());
 					image = overlap(floorImage, image);
 					image = drawString(image, ((Character) cell).getLevel()
 							.toString(), Color.WHITE);
 					put(image, i - 1, j - 1);
 				} else if (cell.getClass().equals(Bonus.class)) {
 					image = bonusImagesByName.get(((Bonus) cell).getBonusType()
 							.toString());
 					image = overlap(floorImage, image);
 					image = drawString(image, (((Bonus) cell).getBonusType()
 							.getBonusAmount()).toString(), Color.RED);
 					put(image, i - 1, j - 1);
 				} else {
 					image = boardImagesByClass.get(cell.getClass());
 					if (cell.getClass().equals(Wall.class)) {
 						put(image, i - 1, j - 1);
 					} else if (cell.getClass().equals(BloodyFloor.class)) {
 						put(bloodyFloorImage, i - 1, j - 1);
 					} else {
 						put(floorImage, i - 1, j - 1);
 					}
 				}
 			}
 		}
 
 		Point p = new Point(dungeonGameFrame.game.getPlayer().getPosition());
 
 		if (dungeonGameFrame.game.getBoard()[p.x][p.y] instanceof BloodyFloor) {
 			image = overlap(bloodyFloorImage, playerImage);
 		}
 		image = overlap(floorImage, playerImage);
 		image = drawString(image, dungeonGameFrame.game.getPlayer().getLevel()
 				.toString(), Color.WHITE);
 		put(image, p.x - 1, p.y - 1);
 	}
 
 	/**
 	 * @param dungeonGameFrame
 	 * 
 	 *            Draw the dungeon panel when a game begins.
 	 */
 	private void drawDungeon(Game game) {
 		drawRestOfDungeon(game);
 		drawDungeonArroundPlayer(game);
 
 	}
 
 	/**
 	 * @param dungeonGameFrame
 	 *            Draw all the visible cells (it's just for loaded games in this
 	 *            game implementation)
 	 */
 	private void drawRestOfDungeon(Game game) {
 		Image image;
 		List<Point> points = new ArrayList<Point>();
 		Image floorImage = boardImagesByClass.get(Floor.class);
 		Image bloodyFloorImage = overlap(floorImage, boardImagesByClass
 				.get(BloodyFloor.class));
 
 		int row = game.getBoardDimension().x - 2;
 		int col = game.getBoardDimension().y - 2;
 
 		for (int i = 1; i <= row; i++) {
 			for (int j = 1; j <= col; j++) {
 				Putable cell = game.getBoard()[i][j];
 				if (cell.isVisible() && cell.getClass().equals(Monster.class)) {
 					image = monsterImagesByName.get(((Monster) cell)
 							.getMonsterType().toString());
 					image = overlap(floorImage, image);
 					image = drawString(image, ((Character) cell).getLevel()
 							.toString(), Color.WHITE);
 					put(image, i - 1, j - 1);
 					points.add(new Point(i, j));
 				} else if (cell.isVisible()
 						&& cell.getClass().equals(Bonus.class)) {
 					image = bonusImagesByName.get(((Bonus) cell).getBonusType()
 							.toString());
 					image = overlap(floorImage, image);
 					image = drawString(image, (((Bonus) cell).getBonusType()
 							.getBonusAmount()).toString(), Color.RED);
 					put(image, i - 1, j - 1);
 					points.add(new Point(i, j));
 				} else {
 					if (cell.isVisible() && cell.getClass().equals(Wall.class)) {
 						image = boardImagesByClass.get(cell.getClass());
 						put(image, i - 1, j - 1);
 						points.add(new Point(i, j));
 					} else if (cell.isVisible()
 							&& cell.getClass().equals(BloodyFloor.class)) {
 						put(bloodyFloorImage, i - 1, j - 1);
 						points.add(new Point(i, j));
 					} else if (cell.isVisible()) {
 						put(floorImage, i - 1, j - 1);
 						points.add(new Point(i, j));
 					}
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * @param dungeonGameFrame
 	 *            Draw the 8 cells around the player and the cell under the
 	 *            player. Before that draw the player
 	 */
 	private void drawDungeonArroundPlayer(Game game) {
 		Image image;
 		Image floorImage = boardImagesByClass.get(Floor.class);
 		Image bloodyFloorImage = overlap(floorImage, boardImagesByClass
 				.get(BloodyFloor.class));
 
 		Point pPos = game.getPlayer().getPosition();
 		pPos = pPos.sub(2, 2);
 
 		for (int i = 1; i <= 3; i++) {
 			for (int j = 1; j <= 3; j++) {
 				if (pPos.x + i > 0 && pPos.x+i < game.getBoardDimension().x-1
 						&& pPos.y + j > 0 && pPos.x+j < game.getBoardDimension().y-1) {
 					Putable cell = game.getBoard()[pPos.x + i][pPos.y + j];
 					if (cell.getClass().equals(Monster.class)) {
 						image = monsterImagesByName.get(((Monster) cell)
 								.getMonsterType().toString());
 						image = overlap(floorImage, image);
 						image = drawString(image, ((Character) cell).getLevel()
 								.toString(), Color.WHITE);
 						put(image, pPos.x + i - 1, pPos.y + j - 1);
 					} else if (cell.getClass().equals(Bonus.class)) {
 						image = bonusImagesByName.get(((Bonus) cell)
 								.getBonusType().toString());
 						image = overlap(floorImage, image);
 						image = drawString(image, (((Bonus) cell)
 								.getBonusType().getBonusAmount()).toString(),
 								Color.RED);
 						put(image, pPos.x + i - 1, pPos.y + j - 1);
 					} else {
 						image = boardImagesByClass.get(cell.getClass());
 						if (cell.getClass().equals(Wall.class)) {
 							put(image, pPos.x + i - 1, pPos.y + j - 1);
 						} else if (cell.getClass().equals(BloodyFloor.class)) {
 							put(bloodyFloorImage, pPos.x + i - 1, pPos.y + j
 									- 1);
 						} else {
 							put(floorImage, pPos.x + i - 1, pPos.y + j - 1);
 						}
 					}
 				}
 			}
 		}
 
 		Point p = new Point(game.getPlayer().getPosition());
 
 		if (game.getBoard()[p.x][p.y] instanceof BloodyFloor) {
 			image = overlap(bloodyFloorImage, playerImage);
 		}
 		image = overlap(floorImage, playerImage);
 		image = drawString(image, game.getPlayer().getLevel().toString(),
 				Color.WHITE);
 		put(image, p.x - 1, p.y - 1);
 	}
 
 	/**
 	 * @return Getter of the monsterUnderMouse.
 	 */
 	public Monster getMonsterUnderMouse() {
 		return monsterUnderMouse;
 	}
 
 	/**
 	 * @param game
 	 *            of class Game
 	 * @param moveType
 	 *            instance of enumerative MoveTypes
 	 * 
 	 *            Redraw if necessary the DungeonPanel.
 	 */
 	public void drawPlayerMove(Game game, MoveTypes moveType) {
 		Image bloodyFloor;
 		Image floor;
 		Point afterMove = new Point(game.getPlayer().getPosition().x, game
 				.getPlayer().getPosition().y);
 		Point beforeMove = afterMove.sub(moveType.getDirection());
 		floor = boardImagesByClass.get(Floor.class);
 		bloodyFloor = boardImagesByClass.get(BloodyFloor.class);
 		bloodyFloor = overlap(floor, bloodyFloor);
 		clear(beforeMove.x - 1, beforeMove.y - 1);
 		if (game.getBoard()[beforeMove.x][beforeMove.y].getClass().equals(
 				BloodyFloor.class)) {
 			put(bloodyFloor, beforeMove.x - 1, beforeMove.y - 1);
 		} else {
 			put(floor, beforeMove.x - 1, beforeMove.y - 1);
 		}
 
 		clear(afterMove.x - 1, afterMove.y - 1);
 		Image image;
 		if (game.getBoard()[afterMove.x][afterMove.y].getClass().equals(
 				BloodyFloor.class)) {
 			image = overlap(bloodyFloor, playerImage);
 			image = drawString(image, game.getPlayer().getLevel().toString(),
 					Color.WHITE);
 			put(image, afterMove.x - 1, afterMove.y - 1);
 		} else {
 			image = overlap(floor, playerImage);
 			image = drawString(image, game.getPlayer().getLevel().toString(),
 					Color.WHITE);
 
 			put(image, afterMove.x - 1, afterMove.y - 1);
 		}
 		updateUI();
 	}
 
 	/**
 	 * @param p
 	 * 
 	 *            Draw blood on the floor where a character die.
 	 */
 	public void drawDiedCharacter(Point p) {
 		Image imagFloor = boardImagesByClass.get(Floor.class);
 		Image imagBloodFloor = boardImagesByClass.get(BloodyFloor.class);
 		clear(p.x - 1, p.y - 1);
 		put(overlap(imagFloor, imagBloodFloor), p.x - 1, p.y - 1);
 		repaint();
 
 	}
 
 	/**
 	 * @param p
 	 * 
 	 *            Remove the image of the bonus and draw a floor.
 	 */
 	public void drawGrabedBonus(Point p) {
 		Image floor = boardImagesByClass.get(Floor.class);
 		clear(p.x - 1, p.y - 1);
 		put(overlap(floor, playerImage), p.x - 1, p.y - 1);
 		repaint();
 
 	}
 
 	public void drawDiscoveredCell(Game game, MoveTypes dir) {
 		Point pPos = game.getPlayer().getPosition();
 		List<Point> points = new ArrayList<Point>();
 		points.add(pPos.add(dir.getDirection()));
 		if (dir == MoveTypes.LEFT || dir == MoveTypes.RIGHT) {
 			points.add(pPos.add(1, 0).add(dir.getDirection()));
 			points.add(pPos.sub(1, 0).add(dir.getDirection()));
 		} else {
 			points.add(pPos.add(0, 1).add(dir.getDirection()));
 			points.add(pPos.sub(0, 1).add(dir.getDirection()));
 		}
 
 		Image image;
 		Image floorImage = boardImagesByClass.get(Floor.class);
 		Image bloodyFloorImage = overlap(floorImage, boardImagesByClass
 				.get(BloodyFloor.class));
 
 		for (Point p : points) {
 			if (p.x > 0 && p.x < game.getBoardDimension().x - 1 && p.y > 0
 					&& p.y < game.getBoardDimension().y - 1) {
 				if (game.getBoard()[p.x][p.y].isVisible()) {
 					game.getBoard()[p.x][p.y].setVisible();
 					Putable cell = game.getBoard()[p.x][p.y];
 					if (cell.getClass().equals(Monster.class)) {
 						image = monsterImagesByName.get(((Monster) cell)
 								.getMonsterType().toString());
 						image = overlap(floorImage, image);
 						image = drawString(image, ((Character) cell).getLevel()
 								.toString(), Color.WHITE);
 						put(image, p.x - 1, p.y - 1);
 					} else if (cell.getClass().equals(Bonus.class)) {
 						image = bonusImagesByName.get(((Bonus) cell)
 								.getBonusType().toString());
 						image = overlap(floorImage, image);
 						image = drawString(image, (((Bonus) cell)
 								.getBonusType().getBonusAmount()).toString(),
 								Color.RED);
 						put(image, p.x - 1, p.y - 1);
 					} else {
 						image = boardImagesByClass.get(cell.getClass());
 						if (cell.getClass().equals(Wall.class)) {
 							put(image, p.x - 1, p.y - 1);
 						} else if (cell.getClass().equals(BloodyFloor.class)) {
 							put(bloodyFloorImage, p.x - 1, p.y - 1);
 						} else {
 							put(floorImage, p.x - 1, p.y - 1);
 						}
 					}
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * Method to initialize player image.
 	 */
 	private void playerImage() {
 		try {
			playerImage = loadImage("./resources/images/hero.png");
 		} catch (IOException e) {
 			JOptionPane.showMessageDialog(null, "Unexpected Error", "Error",
 					JOptionPane.ERROR_MESSAGE);
 		}
 	}
 
 	/**
 	 * Method to initialize board images.
 	 */
 	private void boardImagesByClass() {
 		try {
 			boardImagesByClass.put(Wall.class,
					loadImage("./resources/images/wall.png"));
 			boardImagesByClass.put(Floor.class,
					loadImage("./resources/images/background.png"));
 			boardImagesByClass.put(BloodyFloor.class,
					loadImage("./resources/images/blood.png"));
 		} catch (IOException e) {
 			JOptionPane.showMessageDialog(null, "Unexpected Error", "Error",
 					JOptionPane.ERROR_MESSAGE);
 		}
 	}
 
 	/**
 	 * Method to initialize bonus images.
 	 */
 	private void bonusImagesInitialize() {
 		try {
 			bonusImagesByName.put("LIFE",
					loadImage("./resources/images/healthBoost.png"));
 			bonusImagesByName.put("STRENGTH",
					loadImage("./resources/images/attackBoost.png"));
 		} catch (IOException e) {
 			JOptionPane.showMessageDialog(null, "Unexpected Error", "Error",
 					JOptionPane.ERROR_MESSAGE);
 		}
 	}
 
 	/**
 	 * Method to initialize monsters images.
 	 */
 	private void monstersImagesInitialize() {
 		try {
 			monsterImagesByName.put("GOLEM",
					loadImage("./resources/images/golem.png"));
 			monsterImagesByName.put("DRAGON",
					loadImage("./resources/images/dragon.png"));
 			monsterImagesByName.put("SNAKE",
					loadImage("./resources/images/serpent.png"));
 		} catch (IOException e) {
 			JOptionPane.showMessageDialog(null, "Unexpected Error", "Error",
 					JOptionPane.ERROR_MESSAGE);
 		}
 	}
 
 	public void drawLevelUp(Game game) {
 		Image image;
 		Image bloodyFloor;
 		Image floor;
 		Point playerPos = new Point(game.getPlayer().getPosition().x, game
 				.getPlayer().getPosition().y);
 		floor = boardImagesByClass.get(Floor.class);
 		bloodyFloor = boardImagesByClass.get(BloodyFloor.class);
 		bloodyFloor = overlap(floor, bloodyFloor);
 
 		clear(playerPos.x - 1, playerPos.y - 1);
 		if (game.getBoard()[playerPos.x][playerPos.y] instanceof BloodyFloor) {
 			image = overlap(bloodyFloor, playerImage);
 			image = drawString(image, game.getPlayer().getLevel().toString(),
 					Color.WHITE);
 			put(image, playerPos.x - 1, playerPos.y - 1);
 		} else {
 			image = overlap(floor, playerImage);
 			image = drawString(image, game.getPlayer().getLevel().toString(),
 					Color.WHITE);
 
 			put(image, playerPos.x - 1, playerPos.y - 1);
 		}
 		updateUI();
 	}
 
 }
