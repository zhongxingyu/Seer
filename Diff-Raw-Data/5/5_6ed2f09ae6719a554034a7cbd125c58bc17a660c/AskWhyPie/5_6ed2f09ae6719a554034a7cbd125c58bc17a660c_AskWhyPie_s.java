 package fi.askwhypie;
 
 import java.util.ArrayList;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.Music;
 import org.newdawn.slick.SlickException;
 
 /**
  *
  * @author panos
  */
 public class AskWhyPie extends BasicGame {
 
     Menu m;
     Beginning b;
     HandleAct handleAct;
     Player player;
     ArrayList<Enemy> enemies;
     Fireball fireball;
     FinaleScreen fs;
     float fireballTimer;
     ListenerForKeyes listener;
     String[] maps;
     int map;
     float spawnEnemy;
     int enemyX;
     int enemyY;
     String finLayer;
 
     public AskWhyPie() {
         super("AskWhy game");
         listener = new ListenerForKeyes();
         maps = new String[]{"data/map/grasslevel.tmx", "data/map/level3.tmx"};
         map = 0;
         enemies = new ArrayList<Enemy>();
         fireballTimer = 100.0f;
 
     }
 
     public static void main(String[] args) {
         try {
             AppGameContainer app = new AppGameContainer(new AskWhyPie());
             app.setTargetFrameRate(60);
             app.setDisplayMode(1312, 768, false);
             app.start();
         } catch (SlickException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public void init(GameContainer container) throws SlickException {
         m = new Menu(container);
         b = new Beginning(container);
         handleAct = new HandleAct(container);
         fs = new FinaleScreen(container);
     }
 
     @Override
     public void update(GameContainer container, int delta) throws SlickException {
         if (GameStatus.isBeginActOne()) {
             container.getInput().addKeyListener(listener);
             if (Input.KEY_ENTER == listener.keyValue()) {
                 spawnEnemy = 100;
                 enemyX = 512;
                 enemyY = 386;
                 player = new Player(256, 256);
                 player.setSpeed(0.5f);
                 for (int i = 0; i < 16; i++) {
                     enemies.add(new Enemy(enemyX, enemyY));
                 }
                 finLayer = "dildo layer";
                 container.getInput().removeAllKeyListeners();
                 GameStatus.gameState = 2;
             }
         }
         if (GameStatus.isAct()) {
             player.move(1.5f);
             player.checkWallCollision(handleAct.getMap());
 
             if (!player.getStopTime()) {
                 spawnEnemy -= 0.5;
             }
             if (spawnEnemy <= 0) {
                 enemies.add(new Enemy(enemyX, enemyY));
                 spawnEnemy = 100;
             }
 
             if (fireball != null) {
                 fireball.move();
 
                 Enemy hittedEnemy = null;
 
                 for (Enemy enemy : enemies) {
                     if (fireball.checkCollision(enemy)) {
                         fireball.hits();
                         hittedEnemy = enemy;
                     }
                 }
                 if (hittedEnemy != null) {
                     enemies.remove(hittedEnemy);
                 }
             }
 
             if (player.getHealth() <= 0) {
                 GameStatus.gameState = 4;
             }
 
             if (listener.keyValue() == Input.KEY_UP || listener.keyValue() == Input.KEY_DOWN || listener.keyValue() == Input.KEY_LEFT || listener.keyValue() == Input.KEY_RIGHT) {
                 player.setFacing(listener.arrowKeyValue());
             }
             if (listener.keyValue() == Input.KEY_X) {
                 player.stopTime();
             }
             if (listener.keyValue() == Input.KEY_Z) {
                 if (fireballTimer >= 99.9f) {
                     fireball = new Fireball(player);
                     fireballTimer = 0.0f;
                 }
                 listener.keyPressed(666, 'i');
             }
             if (listener.keyValue() == Input.KEY_Q) {
                 handleAct.stopMusic(map);
                 container.exit();
             }
             if (listener.keyValue() == Input.KEY_D) {
                 handleAct.stopMusic(map);
                 map = 1;
             }
             if (listener.keyValue() == Input.KEY_A) {
                 handleAct.stopMusic(map);
                 map = 0;
             }
             if (player.getStopTime()) {
                 if (player.pausePower <= 0) {
                     player.continueTime();
                     listener.keyPressed(666, 'c');
                 }
                 player.pausePower -= 0.7;
             }
             if (player.pausePower < 100) {
                 player.pausePower += 0.1f;
             }
 
             fireballTimer += 0.5f;
             if (fireballTimer > 100.0f) {
                 fireballTimer = 100.0f;
             }
             if (handleAct.getMap() != null && handleAct.getMap().getTileId((int) player.getBorderLeft() / 32, (int) player.getBorderUp() / 32, handleAct.getMap().getLayerIndex(finLayer)) != 0) {
                 if (finLayer.contains("pie")) {
                     handleAct.stopMusic(map);
                     map = 0;
                     GameStatus.gameState = 6;
 
                 } else {
                     handleAct.stopMusic(map);
                     finLayer = "pie layer";
                     map = 1;
                    GameStatus.gameState = 6;
                 }
             }
         }
         if (GameStatus.gameState == 5) {
             container.getInput().addKeyListener(listener);
             if (Input.KEY_ENTER == listener.keyValue()) {
                 enemies = new ArrayList<Enemy>();
                 spawnEnemy = 100;
                 enemyX = 368;
                 enemyY = 480;
                 player.setX(80);
                 player.setY(480);
                 player.setSpeed(0.5f);
                 for (int i = 0; i < 8; i++) {
                     enemies.add(new Enemy(enemyX, enemyY));
                 }
                 enemyX = 500;
                 enemyY = 128;
                 for (int i = 0; i < 8; i++) {
                     enemies.add(new Enemy(enemyX, enemyY));
                 }
                 finLayer = "pie layer";
                 container.getInput().removeAllKeyListeners();
                 GameStatus.gameState = 2;
             }
         }
 
 
     }
 
     public void render(GameContainer container, Graphics g) throws SlickException {
 
         if (GameStatus.isInGameMenu()) {
             m.drawWholeMenu(g);
             m.playMenuMusic();
         } else if (GameStatus.isBeginActOne()) {
             m.stopMenuMusic();
             b.drawBeginnings(g);
         } else if (GameStatus.gameState == 5) {
             fs.drawFinaleScreen(g);
         } else if (GameStatus.isAct()) {
             handleAct.setMap(maps[map]);
             handleAct.drawAct(map);
             g.drawString("Player:", 1075, 20);
             g.drawString(player.getHealth() + " health", 1100, 50);
             g.drawString((int) player.pausePower + " paussiPower", 1100, 80);
             g.drawString((int) fireballTimer + " fireball", 1100, 110);
             g.drawAnimation(player.getAnimation(), player.getX(), player.getY());
 
             if (player.getStopTime()) {
                 Color color = g.getColor();
                 g.setColor(Color.darkGray);
 
                 for (int y = 0; y < 5; ++y) {
                     for (int x = 0; x < 5; ++x) {
 
                         g.drawString("PAUSSI", x * 200 + 70, y * 170 + 40);
                     }
                 }
                 g.setColor(color);
             }
 
             if (fireball != null) {
                 boolean success = fireball.draw(g);
 
 //		g.drawLine(fireball.getBorderLeft(), 0, fireball.getBorderLeft(), 1000);
 //		g.drawLine(fireball.getBorderRight(), 0, fireball.getBorderRight(), 1000);
 //		g.drawLine(0, fireball.getBorderUp(), 1000, fireball.getBorderUp());
 //		g.drawLine(0, fireball.getBorderDown(), 1000, fireball.getBorderDown());
 
                 if (!success) {
                     fireball = null;
                 }
             }
             for (Enemy e : enemies) {
                 e.draw(g);
 
                 if (!player.getStopTime()) {
                     e.setFacing();
                     e.move(2f);
                     e.checkWallCollision(handleAct.getMap());
                 }
                 if (e.checkCollision(player)) {
                    player.setHealth(player.getHealth() - 0);
                 }
             }
 
 
         } else if (GameStatus.isCredits()) {
             Image im = new Image("data/background.jpg");
             container.getInput().addKeyListener(listener);
             im.draw(0, 0, container.getWidth(), container.getHeight());
             g.drawString("Pisteet Kurisulle hienoista efekteistä," + "\n"
                     + "Hallolle, JMorrowlle, Numppalle ja Mazalle kiitokset."
                     + "\n" + "Tää on placeholder.", 100, 100);
             if (Input.KEY_Q == listener.keyValue()) {
                 GameStatus.gameState = 0;
             }
         } else if (GameStatus.isGameOver()) {
             Image im = new Image("data/background.jpg");
             Image ko = new Image("data/gameover.png");
             map = 0;
             finLayer = "dildo layer";
             handleAct.setMap(maps[map]);
             im.draw(0, 0, container.getWidth(), container.getHeight());
             ko.draw(0, 0, 1000, 500);
             if (Input.KEY_Q == listener.keyValue() || Input.KEY_ENTER == listener.keyValue()) {
                 enemies = new ArrayList<Enemy>();
                 handleAct.stopMusic(map);
                 m = new Menu(container);
                 GameStatus.gameState = 0;
             }
         } else if (GameStatus.gameState == 6) {
             Image im = new Image("data/background.jpg");
             Image ko = new Image("data/madeit.png");
             Image di = new Image("data/map/dildo.png");
             Image pi = new Image("data/map/ladyv2.png");
             map = 0;
             finLayer = "dildo layer";
             handleAct.setMap(maps[map]);
             im.draw(0, 0, container.getWidth(), container.getHeight());
             ko.draw(0, 0, 1000, 500);
             pi.draw(800, 10, 544, 430);
             di.draw(990, 170, 184, 450);
             if (Input.KEY_Q == listener.keyValue() || Input.KEY_ENTER == listener.keyValue()) {
                 enemies = new ArrayList<Enemy>();
                 handleAct.stopMusic(map);
                 m = new Menu(container);
                 GameStatus.gameState = 0;
             }
         }
     }
 }
