 package se.chalmers.kangaroo.view;
 
 import java.util.HashMap;
 
 import javax.swing.ImageIcon;
 
 import se.chalmers.kangaroo.constants.Constants;
 import se.chalmers.kangaroo.model.GameModel;
 import se.chalmers.kangaroo.model.creatures.Creature;
 import se.chalmers.kangaroo.model.iobject.InteractiveObject;
 import se.chalmers.kangaroo.model.kangaroo.Item;
 import se.chalmers.kangaroo.model.utils.Position;
 import se.chalmers.kangaroo.utils.Sound;
 
 /**
  * This is the normal view which renders the model.
  * 
  * @author alburgh
  * @modifiedby simonal
  * @modifiedby arvidk
  */
 public class GameView extends JPanelWithBackground {
 	private GameModel gm;
 	private HashMap<Creature, Animation> creatureAnimations;
 	private KangarooAnimation ka;
 	private boolean isPaused = false;
 	private PauseView pv;
 	private Sound lv1Music;
 	private VictoryView vv;
 	private boolean newLevel = false;
 
 	/**
 	 * Create the view when the rendering shall begin.
 	 * 
 	 * @param imagepath
 	 *            , the path name to the background
 	 * @param gm
 	 *            , the gamemodel it shall render
 	 * @param cv
 	 *            , the changeview in order for the menu to work.
 	 */
 	public GameView(String imagepath, GameModel gm, ChangeView cv) {
 		super(imagepath);
 		this.gm = gm;
 		creatureAnimations = new HashMap<Creature, Animation>();
 		AnimationFactory af = new AnimationFactory();
 		for (int i = 0; i < gm.getGameMap().getCreatureSize(); i++) {
 			Creature c = gm.getGameMap().getCreatureAt(i);
 			creatureAnimations.put(c, af.getAnimation(c));
 		}
 
 		ka = new KangarooAnimation(gm.getKangaroo(), 58, 64);
 		pv = new PauseView("resources/images/pausebackground.png", cv);
 		pv.setVisible(isPaused);
 		pv.setOpaque(isPaused);
 		this.add(pv);
 	}
 
 	@Override
 	public void paintComponent(java.awt.Graphics g) {
 		super.paintComponent(g);
 		Position p = gm.getKangaroo().getPosition();
 
 		int drawFrom = getLeftX();
 		int fixPosition = p.getX() / Constants.TILE_SIZE < 16
 				|| drawFrom == gm.getGameMap().getTileWidth() - 33 ? 0 : p
 				.getX() % 32;
 
 		g.drawString("" + gm.getTime(), 10, 10);
 		g.drawString("Deaths: " + gm.getDeathCount(), 100, 10);
 		// paintIcon(null, g
 		/* Render the tiles */
 		for (int y = 0; y < gm.getGameMap().getTileHeight(); y++)
 			for (int x = drawFrom; x < drawFrom + 33; x++) {
 				ImageIcon i = new ImageIcon("../gfx/tiles/tile_"
 						+ gm.getGameMap().getTile(x, y).getId() + ".png");
 				i.paintIcon(null, g, (x - drawFrom) * 32 - fixPosition,
 						(y - 2) * 32);
 			}
 		/* Render the items */
 		for (int i = 0; i < gm.getGameMap().amountOfItems(); i++) {
 			Item item = gm.getGameMap().getItem(i);
 			if (item.getPosition().getX() > drawFrom
 					&& item.getPosition().getX() < drawFrom + 32) {
 				ImageIcon img = new ImageIcon("../gfx/tiles/tile_"
 						+ item.getId() + ".png");
 				img.paintIcon(null, g, (item.getPosition().getX() - drawFrom)
 						* 32 - fixPosition,
 						(item.getPosition().getY() - 2) * 32);
 			}
 		}
 		/* Render the interactive objects */
 		for (int i = 0; i < gm.getGameMap().getIObjectSize(); i++) {
 			InteractiveObject io = gm.getGameMap().getIObject(i);
 			if (io.getPosition().getX() > drawFrom
 					&& io.getPosition().getX() < drawFrom + 32) {
 				ImageIcon img = new ImageIcon("../gfx/tiles/tile_" + io.getId()
 						+ ".png");
 				img.paintIcon(null, g, (io.getPosition().getX() - drawFrom)
 						* 32 - fixPosition, (io.getPosition().getY() - 2) * 32);
 			}
 		}
 		/* Render the creatures */
 		for (int i = 0; i < gm.getGameMap().getCreatureSize(); i++) {
 			Creature c = gm.getGameMap().getCreatureAt(i);
 			if (c.getPosition().getX() > drawFrom * 32
 					&& c.getPosition().getX() < (drawFrom + 32) * 32) {
 				int xP = c.getPosition().getX();
 				int yP = c.getPosition().getY();
 				if (creatureAnimations.containsKey(c)) {
 					creatureAnimations.get(c).drawSprite(g,
 							xP - drawFrom * 32 - fixPosition, yP - 64);
 				}
 			}
 		}
 		/* Draw the kangaroo based on where you are */
 		if (drawFrom == 0
 				&& gm.getKangaroo().getPosition().getX() / Constants.TILE_SIZE != 16) {
 			ka.drawSprite(g, p.getX(), p.getY());
 		} else if (drawFrom == gm.getGameMap().getTileWidth() - 33) {
 			ka.drawSprite(g, p.getX() - drawFrom * 32 - fixPosition, p.getY());
 		} else {
 			ka.drawSprite(g, p.getX() - drawFrom * 32 - fixPosition, p.getY());
 		}
 	}
 
 	/* Private method for calculate the left side to render */
 	private int getLeftX() {
 		int kPos = gm.getKangaroo().getPosition().getX() / Constants.TILE_SIZE;
 		if (kPos < 16)
 			return 0;
 		if (gm.getGameMap().getTileWidth() - kPos < 17)
 			return gm.getGameMap().getTileWidth() - 33;
 		return kPos - 16;
 	}
 
 	/**
 	 * When the game pauses a menu will appear.
 	 */
 	public void togglePause() {
 		this.isPaused = !isPaused;
 		pv.setVisible(isPaused);
 		pv.setOpaque(isPaused);
 	}
 	
 	public void showVictoryView(){
 		vv = new VictoryView("resources/images/pausebackground.png", gm.getDeathCount(), gm.getTime(), this);
 		vv.setVisible(true); 
 		vv.setOpaque(true);
 		this.add(vv);
		this.repaint();
		this.revalidate();
		this.validate();
		this.revalidate();
 	}
 	
 	public void setNewLevel(boolean b){
 		newLevel = b; 
 	}
 	
 	public boolean startNewLevel(){
 		return newLevel;
 	}
 }
