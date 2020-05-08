 package states;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.Music;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.state.transition.FadeInTransition;
 import org.newdawn.slick.state.transition.FadeOutTransition;
 import org.newdawn.slick.tiled.TiledMap;
 
 import projectiles.Projectile;
 import weapons.Attack;
 import weapons.Coin;
 import weapons.Fist;
 import weapons.Weapon;
 import weapons.Wizard;
 import core.MainGame;
 import core.Text;
 import dudes.Dude;
 import dudes.Monster;
 import dudes.Monster.enemyState;
 import dudes.Player;
 
 public class AreaState extends BasicGameState {
     protected Player[]                      players;
     protected TiledMap                      bgImage;
     protected Image							hud;
     protected Image							battleMsg;
     protected ArrayList<ArrayList<Monster>> monsters;
     protected ArrayList<Monster>            currBattle;
     protected Image							princess;
     private ArrayList<Weapon>               floorweapons;
     private ArrayList<Coin>					floorcoins;
     private boolean                         inBattle;
     private boolean                         completed;
     private int                             progression;
     protected int[]                         battleStops;
     protected int                           areaLength;
     private ArrayList<Player>               sPlayers;
     private final int                       PLAYER_STUN_LENGTH = 500;
     private final int						MONSTER_STUN_LENGTH = 200;
     private ArrayList<Projectile>           liveProjectiles;
     private ArrayList<Projectile>           monsterProjectiles;
     private ArrayList<Text>                 screenTexts;
     protected Sound                         attackNoise;
     protected Sound							pickupJewelNoise;
     protected Music							loop;
     protected HashMap<String, Sound>		pickupWeaponSounds;
     protected HashMap<String, Sound>		deathWeaponSounds;
     protected boolean         				debug;
     
     public AreaState(int stateID) {
         super();
     }
     
     @Override
     public void init(GameContainer container, StateBasedGame game) throws SlickException {
     	debug = true;
         progression = 0;
         players = MainGame.players;
         monsters = new ArrayList<ArrayList<Monster>>();
         currBattle = new ArrayList<Monster>();
         floorweapons = new ArrayList<Weapon>();
         liveProjectiles = new ArrayList<Projectile>();
         monsterProjectiles = new ArrayList<Projectile>();
         floorcoins = new ArrayList<Coin>();
         inBattle = false;
         completed = false;
         areaLength = 0;
         
         hud = new Image("Assets/JewelsAndMisc/HUD.png");
         battleMsg = new Image("Assets/JewelsAndMisc/Battle_banner.png");
         
         sPlayers = new ArrayList<Player>();
         sPlayers.add(players[0]);
         sPlayers.add(players[1]);
         
         screenTexts = new ArrayList<Text>();
         
         Weapon.initSoundEffects();
         pickupJewelNoise = new Sound("Assets/Sound/Pickup_Jewel.wav");
         
         //Initialize HashMaps to contain weapons SFX.
         pickupWeaponSounds = new HashMap<String,Sound>();
         deathWeaponSounds = new HashMap<String,Sound>();
         initWeaponSounds();
     }
     
     @Override
     public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
     	int top = 24-container.getHeight()*24/768;
         bgImage.render(-progression % 32, 0, progression / 32, top, 32 + 1, 24);
     	
         //RENDER HUD
         hud.draw(MainGame.GAME_WIDTH/2 - 270, 0);
         
         Collections.sort(sPlayers);
         for (Player p : sPlayers) {
             p.render(g);
             if (debug) {
                 g.draw(p.weapon.getPlayerHitBox(p.pos[0], p.pos[1]));
 	            if (p.weapon.attack!=null) {
 	            	g.draw(p.weapon.attack.hitbox);
 	            }
             }
             
             int hudVal = (p.playerID == 0) ? -168 : 127;
             int itemVal = (p.playerID == 0) ? -75 : 33;
             int pointVal = (p.playerID == 0) ? -140 : 95;
             Image hudPlayerPic = new Image("Assets/players/player"+p.playerID+"/player"+p.playerID+".png");
             
             if (p.weapon.groundSprite != null) {
             	p.weapon.groundSprite.draw(MainGame.GAME_WIDTH/2 + itemVal, 5, 35, 35);
             }
             
             hudPlayerPic.draw(MainGame.GAME_WIDTH/2 + hudVal, 5, 40, 40);
                         
             g.setColor(Color.black);
             g.drawString(""+p.score, MainGame.GAME_WIDTH/2 + pointVal, 70);
 
             if (p.weapon.name.equals("Wizard")) {
         		((Wizard)p.weapon).render(container, game, g);
         	}
         }
         
         for (Text t : screenTexts) {
         	t.render(g);
         }
         
         if (inBattle) {
         	battleMsg.draw(MainGame.GAME_WIDTH/2 - 19, 0);
             
             Collections.sort(currBattle);
             for (Monster m : currBattle) {
                 m.render(g);
             }
         }
         
         for (Weapon i : floorweapons) {
             i.draw();
         }
         
         for (Coin c : floorcoins){
         	c.Draw();
         }
         
         for (Projectile p : liveProjectiles) {
         	p.render(g);
         }
         
         for (Projectile p : monsterProjectiles) {
         	p.render(g);
         }
         
         if (completed && game.getCurrentStateID()==4){
         	princess.draw(container.getWidth()-100, container.getHeight() - 80);
         	if(players[0].score > players[1].score){
             	g.setColor(Color.green);
                 container.getGraphics().drawString("THANK YOU FOR SAVING ME PLAYER 1. YOU WIN!", container.getWidth()/2-200, 170);
         	} else if(players[1].score > players[0].score){
             	g.setColor(Color.green);
                 g.drawString("THANK YOU FOR SAVING ME PLAYER 2. YOU WIN!", container.getWidth()/2-200, 170);
         	} else{
             	g.setColor(Color.green);
                 g.drawString("A TIE? WELL I GUESS YOU'RE BOTH OUT OF LUCK. THANKS THOUGH!", container.getWidth()/2-200, 170);
         	}
         }
     }
     
     @Override
     public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
         if (container.getInput().isKeyPressed(Input.KEY_ESCAPE)) {
             container.exit();
         }
         
         for (int i = 0; i < players.length; i++) {
             players[i].move(container.getInput(), delta, players, currBattle);
             runOverCoins(players[i]);
         }
         
         for (Projectile p : liveProjectiles) {
         	p.move();
         }
 
         for (Projectile p : monsterProjectiles) {
         	p.move();
         }
         
         removeTexts();
         for (Text t : screenTexts) {
         	t.update();
         }
         
         float backPlayerPos = Math.min(players[0].pos[0], players[1].pos[0]);
         
         if (progression > 20 * (areaLength - 32)) {// don't scroll if you're at the end of the screen
             completed = true;
         } else {
             if (backPlayerPos > MainGame.GAME_WIDTH / 10) {// don't scroll unless both players are far right enough
                 if (!inBattle) {
                     // float shift = backPlayerPos - MainGame.GAME_WIDTH/3;
                     float shift = 4;
                     for (int stop : battleStops) {
                         if (progression < stop && progression + shift >= stop) {
                             progression = stop;
                             inBattle = true;
                             currBattle = monsters.remove(0);
                         }
                     }
                     if (!inBattle) {
                         progression += shift;
                         players[0].pos[0] -= shift;
                         players[1].pos[0] -= shift;
                         
                         floorItemsMove(shift);                        
                     }
                 }
             }
         }
         
         for (Player p : players) {
         	while (p.weapon.projectiles.size() > 0) {
         		liveProjectiles.add(p.weapon.projectiles.get(0));
         		p.weapon.projectiles.remove(0);
         	}
         }
         
         for (Monster m : currBattle) {
         	while (m.weapon.projectiles.size() > 0) {
         		monsterProjectiles.add(m.weapon.projectiles.get(0));
         		m.weapon.projectiles.remove(0);
         	}
         }
         
         removeProjectiles();
         for (Projectile p: liveProjectiles) {
         	for (Monster monster : this.currBattle) {
         		if (p.getHitbox().intersects(monster.getHitBox()) && p.hasHit == false) {
         			monster.hurt(p.damage, 500);
         			monster.setLastHit(p.owner);
         			p.hasHit = true;
         		}
         	}
         	
         	 
         	for (Player player : players) {
         		if (p.getHitbox().intersects(player.getHitBox()) && p.hasHit == false) {
         			if (p.owner != player){
         				if (!player.isRespawning) {
         					player.hurt(player.weapon.damage, PLAYER_STUN_LENGTH);
         					p.hasHit = true;
         				}
         			}
         		}
         	}
         }
         
         for (Projectile p : monsterProjectiles) {
         	for (Player player : players) {
         		if (p.getHitbox().intersects(player.getHitBox()) && p.hasHit == false) {
         			if (p.owner != player){
         				if (!player.isRespawning) {
         					player.hurt(player.weapon.damage, PLAYER_STUN_LENGTH);
         					p.hasHit = true;
         				}
         			}
         		}
         	}
         }
         
         for (int i = 0; i < players.length; i++) {
             Player player = players[i];
             if (player.weapon.attack != null) {
             	player.weapon.attack.update(delta);
             }
             player.invincibleTimer += delta;
             for (Monster monster : this.currBattle) {
             	if (player.weapon.attack!=null) {
 	                if (player.weapon.attack.hitbox.intersects(monster.getHitBox())) {
 	                	monster.pushback(player.pos[0], player.weapon.pushback, MainGame.GAME_WIDTH, players, currBattle);
 	                    monster.hurt(player.weapon.damage, MONSTER_STUN_LENGTH);
 	                    
 	                    monster.setLastHit(player);
 	                }
             	}
             }
             if (player.weapon.attack!=null) {
 	            if (player.weapon.attack.hitbox.intersects(players[(i + 1) % 2].getHitBox())) {
 	            	if (!players[(i + 1) % 2].isRespawning) {
 	                	players[(i + 1) % 2].pushback(player.pos[0], player.weapon.pushback, MainGame.GAME_WIDTH, players, currBattle);	                	
 	            		players[(i + 1) % 2].hurt(player.weapon.damage, PLAYER_STUN_LENGTH);
 	            	}
 	            }
             }
         }
         
         for (Monster monster : this.currBattle) {
             monster.invincibleTimer += delta;
             if (monster.state == enemyState.ALIVE) {
             	monster.aiLoop(players, this.currBattle, delta);
                 for (Player player : players) {
                 	if (monster.weapon.attack!=null) {
 	                    if (monster.weapon.attack.hitbox.intersects(player.getHitBox())) {
 	                    	if (!player.isRespawning) {
 	                    		player.hurt(monster.weapon.damage, PLAYER_STUN_LENGTH);
 	                    	}
 	                    }
                 	}
                 }
             }
         }
         
         ArrayList<Weapon> remove = new ArrayList<Weapon>();
         ArrayList<Weapon> add = new ArrayList<Weapon>();
         for (Player p : players) {
             if (container.getInput().isKeyPressed(p.buttons.get("pickup"))) {
                 for (Weapon w : floorweapons) {
                     if (p.getHitBox().intersects(w.getHitBox()) ) {
                     	if (p.weapon.name.equals("Fireman")) {
                     		break;
                     	}
                     	
                     	if (w.owner != null) {
                     		break;
                     	}
             			p.pos[1] -= (w.spriteSizeY - p.weapon.spriteSizeY);
                         p.weapon.drop();
                         p.weapon = w;
                         w.assignOwner(p);
                         w.init();
                         p.itemTimer = w.itemTimer;
                         remove.add(w);
                         if (pickupWeaponSounds.get(w.name) != null){
                         	pickupWeaponSounds.get(w.name).play();
                         }
                         break;
                         //play Sound
                         
                     }
                 }
             }
             for (Weapon r : remove) {
                 floorweapons.remove(r);
             }
         }
         
         checkIfMonsterDead();
         for (Player p : players) {
         	
         	p.itemCheck(delta);
         	
         	p.deathCheck(delta);
         	if(p.isRespawning && !p.weapon.isFist){
         		Weapon w = new Fist(p.pos[0],p.pos[1]);
         		if (deathWeaponSounds.get(p.weapon.name) != null){
                 	deathWeaponSounds.get(p.weapon.name).play();
                 }
         		p.weapon.drop();
                 p.weapon = w;
                 w.assignOwner(p);
                 w.init();
         	}
         }
         
         
         
         for (Weapon a : add) {
             floorweapons.add(a);
         }
         
         if (currBattle.size() == 0 && inBattle) {
             inBattle = false;
         }
         
         if (completed && game.getCurrentStateID()==2){
         	game.enterState(3, new FadeOutTransition(), new FadeInTransition());
         } 
         if (completed && game.getCurrentStateID()==3){
         	game.enterState(4, new FadeOutTransition(), new FadeInTransition());
         }
     }
     
     public ArrayList<Weapon> makeInitItems() throws SlickException {
         return new ArrayList<Weapon>();
     }
     
     public void runOverCoins(Player p){
         ArrayList<Coin> out = new ArrayList<Coin>();
         for (Coin c : floorcoins){
             if (p.getHitBox().intersects(c.getHitBox())) {
             	pickupJewelNoise.play();
             	p.score += c.value;
             	this.screenTexts.add(new Text(p.pos, Integer.toString(c.value), c.color));
                 out.add(c);
             }
         }
         for (Coin c : out){
         	floorcoins.remove(c);
         }
     }
     
     public void floorItemsMove(float shift){
         ArrayList<Weapon> remove = new ArrayList<Weapon>();
         for (Weapon i : floorweapons) {
             i.x -= shift;
             if (i.x < 0) {
                 remove.add(i);
             }
         }
 
         for (Weapon i : remove) {
             floorweapons.remove(i);
         }
         
         ArrayList<Coin> rid = new ArrayList<Coin>();
         for (Coin c : floorcoins) {
             c.pos[0] -= shift;
             if (c.pos[0] < 0) {
                 rid.add(c);
             }
         }
         
         for (Coin i : rid) {
             floorcoins.remove(i);
         }
     }
     
     public void removeTexts() {
     	ArrayList<Text> removeTexts = new ArrayList<Text>();
     	for (Text t : screenTexts) {
     		if (t.duration < 0) {
     			removeTexts.add(t);
     		}
     		
     	}
     	
     	for (Text t : removeTexts) {
     		screenTexts.remove(t);
     	}
     }
     
     public void removeProjectiles() {
     	ArrayList<Projectile> removeProjectiles = new ArrayList<Projectile>();
     	for (Projectile p : liveProjectiles) {
     		if (p.hasHit) {
     			removeProjectiles.add(p);
     		} else if (p.pos[0] > MainGame.GAME_WIDTH + 200) {
     			removeProjectiles.add(p);
     		}    		
     		
     	}
     	
     	
     	for (Projectile p : removeProjectiles) {
     		liveProjectiles.remove(p);
     	}
     	
     	
     	removeProjectiles = new ArrayList<Projectile>();
     	for (Projectile p : monsterProjectiles) {
     		if (p.hasHit) {
     			removeProjectiles.add(p);
     		} else if (p.pos[0] > MainGame.GAME_WIDTH + 200) {
     			removeProjectiles.add(p);
     		}    		
     		
     	}
     	
     	
     	for (Projectile p : removeProjectiles) {
     		monsterProjectiles.remove(p);
     	}
     }
 
     public void checkIfMonsterDead() throws SlickException{
         ArrayList<Monster> removeMonster = new ArrayList<Monster>();
         for (Monster m : this.currBattle) {
             if (m.state == enemyState.ALIVE && m.health <= 0.1) {
             	System.out.println(m.health);
             	System.out.println(m.health);
         		m.state = enemyState.DEAD;
         		m.renderDeath();
                 m.getLastHit().incrementScore(m.value);
 
             	this.screenTexts.add(new Text(m.getLastHit().pos, Integer.toString(m.value), Color.red));
             }
             else if (m.state == enemyState.DYING && m.currentAnimation.isStopped()) {
             	m.state = enemyState.DEAD;
             }
             else if (m.state == enemyState.DEAD) {
         		removeMonster.add(m);
             }
         }
         
         monsterDrop(removeMonster);
     }
    
     
     public void monsterDrop(ArrayList<Monster> removeMonster) throws SlickException{
         for (Monster m : removeMonster) {
         	m.renderDeath();
             this.currBattle.remove(m);
             double rand = Math.random();
             Weapon w = m.getDropWeapon();
             if (w == null) {
             	
             } else {
             	w.y = Math.max(MainGame.GAME_HEIGHT - 32 * 8- (w.groundSprite.getHeight()) + 15, w.y);            	
          		floorweapons.add(w);
             }            
             
             Coin c = m.getDropCoin();
             if (c == null) {
             	
             } else {
             	c.pos[1] = Math.max(MainGame.GAME_HEIGHT - 32 * 8- (c.sprite.getHeight()) + 15, c.pos[1]);            
                 floorcoins.add(c);	
             }
             
         }
     }
     
     /**
      * Method that initializes HashMap to contain all sounds corresponding to weapons.
      * @throws SlickException 
      */
     private void initWeaponSounds() throws SlickException {
 		Sound pickupBear = new Sound("Assets/Sound/pickupBear.wav");
 		Sound pickupDiglet = new Sound("Assets/Sound/pickupDiglet.wav");
 		Sound pickupMecha = new Sound("Assets/Sound/pickupMecha.wav");
 		
 		Sound deathBear = new Sound("Assets/Sound/deathBear.wav");
 		Sound deathMecha = new Sound("Assets/Sound/deathMecha.wav");
 		
 		//Initialize array for picking up weapons
     	pickupWeaponSounds.put("Bear", pickupBear);
     	pickupWeaponSounds.put("Diglet", pickupDiglet);
     	pickupWeaponSounds.put("Mecha", pickupMecha);
     	
     	deathWeaponSounds.put("Bear", deathBear);
     	deathWeaponSounds.put("Mecha", deathMecha);
     }
     
     @Override
     public int getID(){return 0;}
 
 	@Override
 	public void enter(GameContainer container, StateBasedGame game) throws SlickException {
 		loop.loop(1, 0);
 		loop.fade(1000, (float) .5, false);
 	}
 	
 	@Override
 	public void leave(GameContainer container, StateBasedGame game) {
 		loop.fade(100, 0, true);
 	}
 }
