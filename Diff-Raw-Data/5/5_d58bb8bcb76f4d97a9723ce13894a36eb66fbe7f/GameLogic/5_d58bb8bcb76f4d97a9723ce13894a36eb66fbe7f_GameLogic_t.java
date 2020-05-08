 package dungeonCrawler;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.BitSet;
 import java.util.Iterator;
 
 import javax.swing.Timer;
 
 import dungeonCrawler.GameElements.Bullet;
 import dungeonCrawler.GameElements.Player;
 import dungeonCrawler.GameElements.Spell;
 
 public class GameLogic implements KeyListener, ActionListener {
 
 	private Vector2d direction = new Vector2d(0,0);
 	private Vector2d lastDirection = new Vector2d(1,0);
 	private Vector2d checkPoint = new Vector2d(0,0);
 	private int[] delay = new int[1000];
 	private GameContent level;
 	private BitSet keys;
 	protected Timer timer;
 	public App app;
 	Player player;
 	private DirtyShopSystem shop = null;
 
 	public GameLogic(App app) {
 		// TODO Auto-generated constructor stub
 		keys = new BitSet();
 		keys.clear();
 		timer = new Timer(10, this);
 		timer.setActionCommand("Timer");
 		timer.stop();
 		this.app = app;
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		// TODO Auto-generated method stub
 		keys.set(e.getKeyCode());
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		// TODO Auto-generated method stub
 		keys.clear(e.getKeyCode());
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		// TODO Auto-generated method stub
 		if (e.getKeyChar() == 'h') {// "h" key typed
 			keys.clear(72);
 			Iterator<GameObject> it = player.getInventar().iterator();
 			boolean b = true;
 			GameObject o;
 			while (it.hasNext() && b) {
 				o = it.next();
 				if (o.getClass().getName().equalsIgnoreCase("dungeonCrawler.GameObjects.HealthPotion")) {
 					System.out.println("HealthPotion");
 					o.performOn(player);
 					player.getInventar().remove(o);
 					b = false;
 				}
 			}
 		}
 
 	}
 
 	public GameContent getLevel() {
 		return level;
 	}
 
 	public void setLevel(GameContent level) {
 		this.level = level;
 	}
 
 	private void handleCollision(GameElement active, GameElement passive){
 		//TODO generate GameEvents
 		GameEvent e = new GameEvent(passive, EventType.COLLISION, this);
 		active.GameEventPerformed(e);
 		e = new GameEvent(active, EventType.COLLISION, this);
 		passive.GameEventPerformed(e);
 	}
 
 	public boolean moveElement(GameElement e, Vector2d direction){
 		if(e.type.contains(ElementType.MOVABLE)){ //TODO call handleCollision only once per GameElement
 			//			System.out.println("test" + collisioncheck.type.toString());
 			e.setPosition(e.position.add(new Vector2d(direction.getX(), 0)));
 			for(GameElement collisioncheck : level.getGameElements()){
 				if(e.collision(collisioncheck)){
 					if(!collisioncheck.type.contains(ElementType.WALKABLE)){
 						e.setPosition(e.position.add(new Vector2d(-direction.getX(), 0)));
 					}
 					handleCollision(e, collisioncheck); //handle collision (e.g. traps, exit ...)
 				}
 			}
 			//if(level.getGameElements().contains(e)){
 			e.setPosition(e.position.add(new Vector2d(0, direction.getY())));
 			for(GameElement collisioncheck : level.getGameElements()){
 				if(e.collision(collisioncheck)){
 					if(!collisioncheck.type.contains(ElementType.WALKABLE)){
 						e.setPosition(e.position.add(new Vector2d(0, -direction.getY())));
 					}
 					handleCollision(e, collisioncheck); //handle collision (e.g. traps, exit ...)
 				}
 			}
 			//}
 			//e.setPosition(direction.add(e.position));
 			return true;
 		}
 		else
 			return false;
 	}
 
 	public boolean teleportElement(GameElement e, Vector2d position){
 		e.setPosition(position);
 		return false;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		// TODO: abfragen, welche Bits gesetzt sind und ensprechend handeln
 
 		player = (Player)level.getPlayer();
 		Vector2d position = player.getPosition();
 		if(!(direction.getX() == 0 && direction.getY() == 0)){
 			lastDirection = direction;
 		}
 		direction = new Vector2d(0,0);
 		if (keys.get(100)) {// cheat left
 			player.setPosition(position.addX(-10));
 			System.out.println("CHEAT LEFT");
 		}		
 		if (keys.get(104)) {// cheat up
 			player.setPosition(position.addY(-10));
 			System.out.println("CHEAT UP");
 		}
 		if (keys.get(98)) {// cheat down
 			player.setPosition(position.addY(10));
 			System.out.println("CHEAT DOWN");
 		}
 		if (keys.get(102)) {// cheat right
 			player.setPosition(position.addX(10));
 			System.out.println("CHEAT RIGHT");
 		}
 		if (keys.get(101)) {// cheat Leben
 			player.setHealth(player.getHealth()+1000);
 			System.out.println("CHEAT Leben");
 		}
 
 		if (keys.get(103)) {// position output
 			System.out.println("x= " + player.getPosition().getX() + "y= " + player.getPosition().getY());
 		}
 
 		if (keys.get(99)) {// Exit
 			if (level.getExit() != null){
 				level.getPlayer().setPosition(level.getExit().getPosition().addX(10).addY(60));}
 			System.out.println("CHEAT EXIT");
 		}
 		if (keys.get(37)) {// left arrow
 			direction = direction.addX(-1);
 			System.out.println("LEFT");
 		}
 		if (keys.get(38)) {// up arrow
 			direction = direction.addY(-1);
 			System.out.println("UP");
 		}
 		if (keys.get(39)) {// right arrow
 			direction = direction.addX(1);
 			System.out.println("RIGHT");
 		}
 		if (keys.get(40)) {// down arrow
 			direction = direction.addY(1);
 			System.out.println("DOWN");
 		}
 
 
 		if (keys.get(83)) { // s
 			keys.clear();
 			if (level.getPlayer() != null) {
 				
 				if (shop == null) {
 					// initialize shop
 					shop = new DirtyShopSystem((Player)level.getPlayer());
 					shop.setvermoegen(100);
 					shop.gui(shop.getvermoegen());
 				}
 				System.out.println("Shop Visable you have " + player.getMoney() + " Geld");
 				shop.startDirtyShop();
 				player.setMoney(shop.getvermoegen());
 			}
 		}
 
 		if(delay[32] >= 0){
 			delay[32] -= 1;
 		}
 		if (keys.get(32)){
 			if(delay[32] < 0){
 				delay[32] = 70;
 
 				Vector2d pos = new Vector2d(position.add(player.size.mul(0.5)).add(new Vector2d(-5, -5)));
 				if(lastDirection.getX() > 0)
 					pos = pos.add(new Vector2d(player.size.getX()-2,0));
 				if(lastDirection.getX() < 0)
 					pos = pos.add(new Vector2d(-player.size.getX()+2,0));
 				if(lastDirection.getY() > 0)
 					pos = pos.add(new Vector2d(0,player.size.getX()-2));
 				if(lastDirection.getY() < 0)
 					pos = pos.add(new Vector2d(0,-player.size.getX()+2));
 				Bullet tmp = new Bullet(pos, new Vector2d(10, 10));
				tmp.setDirection(lastDirection.mul(3));
 				level.addGameElement(tmp);
 			}
 		}
 		if(delay[KeyEvent.VK_Q] >= 0){
 			delay[KeyEvent.VK_Q] -= 1;
 		}
 		if (keys.get(KeyEvent.VK_Q)){
 			if(delay[KeyEvent.VK_Q] < 0 && player.reduceMana(8, this)){
 				delay[KeyEvent.VK_Q] = 70;
 
 				Vector2d pos = new Vector2d(position.add(player.size.mul(0.5)).add(new Vector2d(-5, -5)));
 				if(lastDirection.getX() > 0)
 					pos = pos.add(new Vector2d(player.size.getX()-2,0));
 				if(lastDirection.getX() < 0)
 					pos = pos.add(new Vector2d(-player.size.getX()+2,0));
 				if(lastDirection.getY() > 0)
 					pos = pos.add(new Vector2d(0,player.size.getX()-2));
 				if(lastDirection.getY() < 0)
 					pos = pos.add(new Vector2d(0,-player.size.getX()+2));
 				Spell tmp = new Spell(pos, new Vector2d(10, 10));
				tmp.setDirection(lastDirection.mul(2));
 				level.addGameElement(tmp);
 			}
 		}
 		if(!keys.isEmpty()) moveElement(player, direction);
 		if (((Player) player).getHealth()<=0){
 			app.cp.removeAll();
 			app.cp.validate();
 			app.gameContent = new GameContent();
 			app.loader = new LevelLoader(app.gameContent, app);
 			this.timer.stop();
 			app.startMainMenu();
 		}
 		if (e.getActionCommand() == "Timer"){
 			GameElement tmpRem = null;
 			for(GameElement element : level.getGameElements()){
 				GameEvent event = new GameEvent(null, EventType.TIMER, this);
 				element.GameEventPerformed(event);
 				if(element.size.getX() == 0 || element.size.getY() == 0){
 					tmpRem = element;
 				}
 			}
 			if (tmpRem != null) {
 				level.removeElement(tmpRem);
 			}
 			app.camera.repaint();
 		}
 	}
 
 	public Vector2d getCheckPoint() {
 		return checkPoint;
 	}
 
 	public void setCheckPoint(Vector2d checkPoint) {
 		this.checkPoint = checkPoint;
 	}
 
 }
