 package dungeonCrawler;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.BitSet;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import javax.swing.Timer;
 
 import dungeonCrawler.GameElements.Bow;
 import dungeonCrawler.GameElements.CheckPoint;
 import dungeonCrawler.GameElements.Enemy;
 import dungeonCrawler.GameElements.Exit;
 import dungeonCrawler.GameElements.Healthpot;
 import dungeonCrawler.GameElements.Manapot;
 import dungeonCrawler.GameElements.Money;
 import dungeonCrawler.GameElements.NPC;
 import dungeonCrawler.GameElements.Player;
 import dungeonCrawler.GameElements.Trap;
 import dungeonCrawler.GameElements.Wall;
 import dungeonCrawler.GameElements.Active;
 import dungeonCrawler.GameElements.FireBolt;
 import dungeonCrawler.GameElements.IceBolt;
 import dungeonCrawler.GameElements.WarpPoint;
 
 //import dungeonCrawler.GameElements.Spell;
 
 public class GameLogic implements KeyListener, ActionListener {
 
 	//	private long time;
 	private Vector2d direction = new Vector2d(0,0);
 	private Vector2d lastDirection = new Vector2d(0,1);
 	private Vector2d checkPoint = new Vector2d(0,0);
 
 	private SettingSet settings = new SettingSet(); 
 	private int[] delay = new int[1000];
 	private int[] max_delay = new int[1000];
 	protected GameContent level;
 	private BitSet keys;
 	protected Timer timer;
 	public App app;
 	protected Player player;
 	public ShopSystem new_shop = null;
 //	public ShopItem new_item;
 	public int Money;
 	public LinkedList<GameObject> Inventar = new LinkedList<GameObject>();	
 	private Vector2d startpos= new Vector2d(0,0);
 	private Vector2d endpos= new Vector2d(0,0);
 	private Vector2d oldendpos= new Vector2d(0,0);
 	private Vector2d oldstartpos= new Vector2d(0,0);
 	private boolean setze=false; // für editor, wenn variable gesetzt ist, dann nochmal drücken um das gameelement hinzu zu fügen
 	public File file;
 	int movedelay=0;
 	int editspeed=5;
 	boolean tpset=false;
 
 	public GameLogic(App app) {
 		// TODO Auto-generated constructor stub
 		keys = new BitSet();
 		keys.clear();
 		timer = new Timer(1, this);
 		timer.setActionCommand("Timer");
 		timer.stop();
 		this.app = app;
 		Money = 0;
 
 		for(int i=0;i<1000;i++){
 			max_delay[i] = 3; 
 		}
 		max_delay[settings.SHOOT] = 200;
 		max_delay[settings.USE_HEALTHPOT] = 500;
 		max_delay[settings.USE_MANAPOT] = 500;
 		max_delay[settings.CHANGE_ARMOR] = 50;
 	}
 
 	private void reduceDelay(){
 		for(int i=0;i<1000;i++){
 			if(delay[i] > 0) delay[i]--;
 		}
 	}
 
 	public boolean checkKey(int i){
 		if(delay[i]<=0 && keys.get(i)){
 			delay[i] = max_delay[i];
 			return true;
 		}
 		return false;
 	}
 
 
 	private void createElement(int sx,int sy,int px,int py,String elementtype){
 		if (!setze) { startpos=level.getPlayer().getPosition(); setze=true;System.out.println("startpos gesetzt");}
 		else {
 			level.removeElement(level.getPlayer());
 			calculatepositions(sx,sy,px,py);
 			switch (elementtype){
 			case "Bow":			level.addGameElement(new Bow(startpos,endpos));			setze=false;	break;
 			case "Checkpoint":	level.addGameElement(new CheckPoint(startpos,endpos));	setze=false;	break;
 			case "Enemy":		level.addGameElement(new Enemy(startpos,endpos));		setze=false;	break;
 			case "Exit":		level.addGameElement(new Exit(startpos,endpos));		setze=false;	break;
 			case "Healthpot":	level.addGameElement(new Healthpot(startpos,endpos));	setze=false;	break;
 			case "Manapot":		level.addGameElement(new Manapot(startpos,endpos));		setze=false;	break;
 			case "Money":		level.addGameElement(new Money(startpos,endpos));		setze=false;	break;
 			case "NPC":			level.addGameElement(new NPC(startpos,endpos));			setze=false;	break;
 			case "Trap":		level.addGameElement(new Trap(startpos,endpos));		setze=false;	break;
 			case "Wall":		level.addGameElement(new Wall(startpos,endpos));		setze=false;	break;
 			case "Warppoint":	if(tpset){level.addGameElement(new WarpPoint(oldstartpos,oldendpos,new Vector2d(px,py)));tpset=false;setze=false;	break;}
 			else {tpset=true;oldstartpos=startpos;oldendpos=endpos;break;}
 			}
 			level.addGameElement(player);
 		}
 	}
 
 	private void calculatepositions(int sx,int sy,int px,int py){
 		/**
 		 * berechnet die start- und endposition für ein zu erzeugendes Element
 		 * */
 		if (px> sx && py>sy){
 			startpos=	new Vector2d(sx,sy);
 			endpos=	new Vector2d(px-sx,py-sy);
 		}
 		else if (px< sx && py>sy){
 			startpos=	new Vector2d(px,sy);
 			endpos=	new Vector2d(sx-px,py-sy);
 		}
 		else if (px< sx && py<sy){
 			startpos=	new Vector2d(px,py);
 			endpos=	new Vector2d(sx-px,sy-py);
 		}
 		else if (px> sx && py<sy){
 			startpos=	new Vector2d(sx,py);
 			endpos=	new Vector2d(px-sx,sy-py);
 		}
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		// TODO Auto-generated method stub
 		keys.set(e.getKeyCode());
 		if (app.editmode==true){
 
 			int sx = startpos.getX();
 			int sy = startpos.getY();
 			int px = level.getPlayer().getPosition().getX();
 			int py = level.getPlayer().getPosition().getY();
 
 			switch (e.getKeyCode()) {
 			case 66:setze=true;createElement(px,py,px+5,py+5,"Bow");			break;	//Bow
 			case 67:setze=true;createElement(px,py,px+30,py+30,"Enemy");		break;	//Enemy
 			case 69:createElement(sx,sy,px,py,"Exit");							break;	//Exit
 			case 71:setze=true;createElement(px,py,px+10,py+10,"Money");			break;	//Money
 			case 72:setze=true;createElement(px,py,px+10,py+10,"Healthpot");		break;	//Healthpot
 			case 77:setze=true;createElement(px,py,px+10,py+10,"Manapot");		break;	//Manapot
 			case 78:setze=true;createElement(px,py,px+30,py+30,"NPC");			break;	//NPC
 			case 83:setze=true;createElement(px,py,px+30,py+30,"Checkpoint");	break;	//CheckPoint
 			case 84:createElement(sx,sy,px,py,"Trap");							break;	//Trap
 			case 87:createElement(sx,sy,px,py,"Wall");							break;	//Wall
 			case 90:createElement(sx,sy,px,py,"Warppoint");					break;	//Wall
 			}
 		}
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		// TODO Auto-generated method stub
 		keys.clear(e.getKeyCode());
 		if(e.getKeyCode() == settings.USE_HEALTHPOT)
 			delay[settings.USE_HEALTHPOT] = 0;
 		if(e.getKeyCode() == settings.USE_MANAPOT)
 			delay[settings.USE_MANAPOT] = 0;
 	}
 	private String convertLvltoStr(int currentLevel) {
 		String str;
 		try {
 			str = 0 +  Integer.toString(currentLevel);
 			str = str.substring(str.length()-2);
 			System.out.println(str);
 
 		} catch (Exception e) {
 			return "99";
 		}
 		return str;
 	}
 
 
 	private void writeLvl(){
 		LinkedList<GameElement> currentelement;
 		//file.delete();
 		file = new File("Levels"+File.separator+"level" + convertLvltoStr(app.currentLevel) +".lvl");
 		//FileWriter writer = null;
 		try {
 			PrintWriter outputstream = new PrintWriter(file);
 			while ((currentelement=level.getGameElements())!=null && level.getPlayer() != currentelement.getFirst()){
 
 				if (currentelement.element().getName()=="WARPPOINT"){
 					outputstream.println(currentelement.element().getName()+","+currentelement.element().getPosition().getX()+","+currentelement.element().getPosition().getY()+","+currentelement.element().getSize().getX()+","+currentelement.element().getSize().getY()+","+((WarpPoint)currentelement.getFirst()).getTarget().getX()+","+((WarpPoint)currentelement.getFirst()).getTarget().getY());
 					level.removeElement(currentelement.getFirst());
 					System.out.println(currentelement.element().getName()+","+currentelement.element().getPosition().getX()+","+currentelement.element().getPosition().getY()+","+currentelement.element().getSize().getX()+","+currentelement.element().getSize().getY());
 					outputstream.flush();
 				}
 				else{
 					outputstream.println(currentelement.element().getName()+","+currentelement.element().getPosition().getX()+","+currentelement.element().getPosition().getY()+","+currentelement.element().getSize().getX()+","+currentelement.element().getSize().getY());
 					level.removeElement(currentelement.getFirst());
 					System.out.println(currentelement.element().getName()+","+currentelement.element().getPosition().getX()+","+currentelement.element().getPosition().getY()+","+currentelement.element().getSize().getX()+","+currentelement.element().getSize().getY());
 					outputstream.flush();
 				}
 
 			}
 			outputstream.println(currentelement.element().getName()+","+currentelement.element().getPosition().getX()+","+currentelement.element().getPosition().getY()+","+currentelement.element().getSize().getX()+","+currentelement.element().getSize().getY());
 			System.out.println(currentelement.element().getName()+","+currentelement.element().getPosition().getX()+","+currentelement.element().getPosition().getY()+","+currentelement.element().getSize().getX()+","+currentelement.element().getSize().getY());
 			outputstream.flush();
 			outputstream.close();
 			level.removeElement(currentelement.getFirst());
 		} catch (IOException e1) {
 
 		}		
 
 	}
 
 
 
 	private void increaselevels(){
 		file = new File("Levels"+File.separator+"level.lvl");
 
 
 		FileWriter writer = null;
 		try {
 			writer = new FileWriter(file);
 			writer.write(String.valueOf(app.level));
 		} catch (IOException e1) {
 			e1.printStackTrace(); 
 		} finally {
 			if (writer != null) try { writer.close(); } catch (IOException ignore) {}
 		}
 		System.out.printf("File is located at %s%n", file.getAbsolutePath());				
 
 
 
 	}
 
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		// TODO Auto-generated method stub
 
 
 
 
 		if (app.editmode==true){
 			if(keys.get(27)){
 				writeLvl();
 				System.out.println("geht weiter");
 				app.editmode=false;
 				app.currentLevel=app.level;
 				app.startGame();
 				increaselevels();
 			}
 			if(keys.get(68)){
 				for(GameElement collisioncheck : level.getGameElements()){
 					if(level.getPlayer().collision(collisioncheck) && collisioncheck!=level.getPlayer()){
 						level.removeElement(collisioncheck);
 					}
 				}
 			}
 		}
 		if (app.editmode == false){		
 
 			if (keys.get(37)) {// left
 				direction=direction.addX(-1);
 				System.out.println("LEFT");
 			}		
 			if (keys.get(38)) {// up
 				direction=direction.addY(-1);
 				System.out.println("UP");
 			}
 			if (keys.get(39)) {// right
 				direction=direction.addX(1);
 				System.out.println("RIGHT");
 			}
 			if (keys.get(40)) {// down
 				direction=direction.addY(1);
 				System.out.println("DOWN");
 			}
 			if(keys.get(27)){
 				if (timer.isRunning()){
 					timer.stop();
 				}
 				else {
 					timer.start();
 				}
 			}
 			if (e.getKeyChar() == 'h') {// "h" for health
 				keys.clear(72);
 				Iterator<GameObject> it = player.getInventar().iterator();
 				boolean b = true;
 				GameObject obj;
 				while (it.hasNext() && b) {
 					obj = it.next();
 					if (obj.getClass().getName().equalsIgnoreCase("dungeonCrawler.GameObjects.HealthPotion")) {
 						obj.performOn(player);
 						player.getInventar().remove(obj);
 						b = false;
 					}
 				}
 			}
 			if (e.getKeyChar() == 'm') {// "m" for mana
 				keys.clear(77);
 				Iterator<GameObject> it = player.getInventar().iterator();
 				boolean b = true;
 				GameObject obj;
 				while (it.hasNext() && b) {
 					obj = it.next();
 					if (obj.getClass().getName().equalsIgnoreCase("dungeonCrawler.GameObjects.ManaPotion")) {
 						obj.performOn(player);
 						player.getInventar().remove(obj);
 						b = false;
 					}
 				}
 			}
 			if (e.getKeyChar() == 'p') {// print string
 				System.out.println(player.getString());
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
 		if (app.editmode==false){
 			if(e.type.contains(ElementType.MOVABLE)){ //TODO call handleCollision only once per GameElement
 				//			System.out.println("test" + collisioncheck.type.toString());
 				HashSet<GameElement> collides = new HashSet<GameElement>();
 				e.setPosition(e.position.add(new Vector2d(direction.getX(), 0)));
 				for(GameElement collisioncheck : level.getGameElements()){
 					if(e.collision(collisioncheck)){
 						if(!collisioncheck.type.contains(ElementType.WALKABLE)){
 							e.setPosition(e.position.add(new Vector2d(-direction.getX(), 0)));
 						}
 						collides.add(collisioncheck);
 					}
 				}
 				//if(level.getGameElements().contains(e)){
 				e.setPosition(e.position.add(new Vector2d(0, direction.getY())));
 				for(GameElement collisioncheck : level.getGameElements()){
 					if(e.collision(collisioncheck)){
 						if(!collisioncheck.type.contains(ElementType.WALKABLE)){
 							e.setPosition(e.position.add(new Vector2d(0, -direction.getY())));
 						}
 						collides.add(collisioncheck);
 					}
 				}
 				for(GameElement col : collides){
 					handleCollision(e, col);
 				}
 				//}
 				//e.setPosition(direction.add(e.position));
 				return true;
 			}
 			else
 
 				return false;
 		}
 		else return true;
 	}
 
 	public boolean teleportElement(GameElement e, Vector2d position){
 		e.setPosition(position);
 		return false;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		player = (Player)level.getPlayer();
 		Vector2d position = player.getPosition();
 		// TODO: abfragen, welche Bits gesetzt sind und ensprechend handeln
 
 
 		if (app.editmode==true){
 			movedelay=movedelay-1;
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
 
 
 			if (keys.get(107) && editspeed > 10) {// left
 				editspeed-=10;
 				System.out.println(editspeed);
 				keys.clear();
 			}			
 			else if (keys.get(107) && editspeed > 0) {// left
 				editspeed-=1;
 				System.out.println(editspeed);
 				keys.clear();
 			}	
 			if (keys.get(109) && editspeed < 10) {// left
 				editspeed+=1;
 				keys.clear();
 				System.out.println(editspeed);
 
 			}	
 			else if (keys.get(109) && editspeed < 100) {// left
 				editspeed+=10;
 				keys.clear();
 				System.out.println(editspeed);
 
 			}
 
 
 
 			if (keys.get(37) && movedelay <=0) {// left
 				player.setPosition(position.addX(-1));
 				movedelay=editspeed;
 				System.out.println("LEFT");
 			}		
 			if (keys.get(38) && movedelay <=0) {// up
 				player.setPosition(position.addY(-1));
 				movedelay=editspeed;
 				System.out.println("UP");
 			}
 			if (keys.get(39) && movedelay <=0) {// right
 				player.setPosition(position.addX(1));
 				movedelay=editspeed;
 				System.out.println("RIGHT");
 			}
 			if (keys.get(40) && movedelay <=0) {// down
 				player.setPosition(position.addY(1));
 				movedelay=editspeed;
 				System.out.println("DOWN");
 			}
 
 
 			if (keys.get(103)) {// position output
 				System.out.println("x= " + player.getPosition().getX() + " ; y= " + player.getPosition().getY());
 			}
 
 
 
 		}
 		if (app.editmode==false){
 
 
 			reduceDelay();
 			player = (Player)level.getPlayer();
 			if(!(direction.getX() == 0 && direction.getY() == 0)){
 				lastDirection = direction;
 			}
 			direction = new Vector2d(0,0);
 
 			for(Active active : level.getActives()){
 				active.interaction(this, settings, keys);
 			}
 
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
 				//System.out.println("LEFT");
 			}
 			if (keys.get(38)) {// up arrow
 				direction = direction.addY(-1);
 				//System.out.println("UP");
 			}
 			if (keys.get(39)) {// right arrow
 				direction = direction.addX(1);
 				//System.out.println("RIGHT");
 			}
 			if (keys.get(40)) {// down arrow
 				direction = direction.addY(1);
 				//System.out.println("DOWN");
 			}
 
 
 
 			if (checkKey(83)) { // s
 				keys.clear();
 				//timer.stop();
 				if (level.getPlayer() != null) {
 					if (new_shop == null) {
 						// initialize shop
 						new_shop = new ShopSystem((Player)level.getPlayer());
						new_shop.fillShop(new_shop.getvermoegen(), new ShopItem("Armor",20), new ShopItem("Health", 10), new ShopItem("Mana", 5));
 					}
 					else{
 						System.out.println("Shop Visable you have " + player.getMoney() + " Geld");
						new_shop.fillShop(new_shop.getvermoegen(), new ShopItem("Armor",20), new ShopItem("Health", 10), new ShopItem("Mana", 5));
 						player.setMoney(new_shop.getvermoegen());
 					}
 				}
 			}
 
 
 			if (keys.get(KeyEvent.VK_Q)){// q (fire bolt)
 				System.out.println(delay[KeyEvent.VK_Q]);
 				if(delay[KeyEvent.VK_Q] <= 0 && player.reduceMana(20, this)){
 					delay[KeyEvent.VK_Q] = 250;
 
 					Vector2d pos = new Vector2d(position.add(player.size.mul(0.5)).add(new Vector2d(-5, -5)));
 					if(lastDirection.getX() > 0)
 						pos = pos.add(new Vector2d(player.size.getX()-2,0));
 					if(lastDirection.getX() < 0)
 						pos = pos.add(new Vector2d(-player.size.getX()+2,0));
 					if(lastDirection.getY() > 0)
 						pos = pos.add(new Vector2d(0,player.size.getX()-2));
 					if(lastDirection.getY() < 0)
 						pos = pos.add(new Vector2d(0,-player.size.getX()+2));
 					FireBolt tmp = new FireBolt(pos, new Vector2d(10, 10));
 					tmp.setDirection(lastDirection.mul(1));
 					level.addGameElement(tmp);
 				}
 			}
 			if (keys.get(KeyEvent.VK_W)){// w (ice bolt)
 				System.out.println(delay[KeyEvent.VK_W]);
 				if(delay[KeyEvent.VK_W] <= 0 && player.reduceMana(10, this)){
 					delay[KeyEvent.VK_W] = 250;
 
 					Vector2d pos = new Vector2d(position.add(player.size.mul(0.5)).add(new Vector2d(-5, -5)));
 					if(lastDirection.getX() > 0)
 						pos = pos.add(new Vector2d(player.size.getX()-2,0));
 					if(lastDirection.getX() < 0)
 						pos = pos.add(new Vector2d(-player.size.getX()+2,0));
 					if(lastDirection.getY() > 0)
 						pos = pos.add(new Vector2d(0,player.size.getX()-2));
 					if(lastDirection.getY() < 0)
 						pos = pos.add(new Vector2d(0,-player.size.getX()+2));
 					IceBolt tmp = new IceBolt(pos, new Vector2d(10, 10));
 					tmp.setDirection(lastDirection.mul(1));
 					level.addGameElement(tmp);
 				}
 
 			}
 			if (((Player) player).getHealth()<=0){
 				app.cp.removeAll();
 				app.cp.validate();
 				app.gameContent = new GameContent(this);
 				app.loader = new LevelLoader(app.gameContent, app);
 				this.timer.stop();
 				app.startMainMenu();
 
 			}
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
 			//			System.out.println("Timediff " + (System.currentTimeMillis() - time));
 		}
 	}
 
 	public void addGameElement(GameElement element){
 		level.addGameElement(element);
 	}
 
 
 
 	public void startMainMenu(){
 		app.cp.removeAll();
 		app.cp.validate();
 		app.gameContent = new GameContent(this);
 		app.loader = new LevelLoader(app.gameContent, app);
 		this.timer.stop();
 		app.startMainMenu();
 	}
 
 	public void lost(Player player) {
 		startMainMenu();
 	}
 
 	public void changeTimer(boolean newStatus){
 		if(newStatus &&(!timer.isRunning())) timer.start();
 		if((!newStatus) &&timer.isRunning()) timer.stop();
 
 
 	}
 
 }
