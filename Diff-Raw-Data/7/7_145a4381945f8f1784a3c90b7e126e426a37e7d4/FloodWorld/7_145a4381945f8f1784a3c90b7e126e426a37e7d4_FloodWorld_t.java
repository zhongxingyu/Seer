 import greenfoot.*;
 
 public class FloodWorld extends World {
     public boolean stopped;
     public Counter scoreCounter;
     private Player player;
     private Coins coinCounter;
     private Overlay overlay;
 
     //GreenfootSound backgroundMusic = new GreenfootSound("zeerstoer.mp3");
 
     public FloodWorld(Player.PlayerType playerType)  {
         super(80, 80, 10);
         //backgroundMusic.playLoop();
 
         setPaintOrder(GameOverScreen.class, Overlay.class, Counter.class, Coins.class, MenuBar.class, Player.class, Bag.class, Coin.class, Water.class, Floodbank.class);
 
         for(int i=0; i<=80; i++) {
             for(int j=0; j<=30; j++) {
 
                 addObject(new Water(), i, j);
             }
         }
 
         for(int i = 0; i <= 80; i++) {
             for(int j = 50; j <= 70; j++) {
 
                 addObject(new Meadow(), i, j);
             }
         }
 
        addObject(new MenuBar(), 39, 75);
 
         for(int i = 0; i <= 80; i++) {
             for(int j = 30; j <= 50; j++) {
 
                 addObject(new Floodbank(), i, j);
             }
         }
 
         addObject(player = Player.createPlayer(playerType), 40, 67);
 
         scoreCounter = new Counter("Score: ");
         addObject(scoreCounter, 6, 74);
 
         coinCounter = new Coins("Coins: ");
         addObject(coinCounter, 6, 76);
     }
 
     public void act(){
         int randomNumber = Greenfoot.getRandomNumber(30);
         if(randomNumber == 0) {
               int randomX = Greenfoot.getRandomNumber(80);
               int randomY = 50 + Greenfoot.getRandomNumber(70 - 50);
               addObject(new Coin(), randomX, randomY);
         }
         scoreCounter.add(2);
 
     }
 
     public Coins getCoinCounter() {
         return coinCounter;
     }
 
     public Player getPlayer() {
         return player;
     }
 
     public void setOverlayLocation(int x, int y) {
         if (overlay == null) {
            overlay = new Overlay();
            addObject(overlay, 20, 75);
         }
        overlay.setLocation(x, y);
     }
 }
