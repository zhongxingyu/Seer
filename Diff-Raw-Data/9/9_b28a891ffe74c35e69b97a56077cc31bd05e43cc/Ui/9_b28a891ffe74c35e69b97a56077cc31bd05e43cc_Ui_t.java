 import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
 import java.awt.Color;
 import java.awt.Font;
 import java.util.ArrayList;
 /**
  * Used to rebuild the specific type of ui for when the tower is selected. 
  * each different type of constructer should counstruct different buttons
  * 
  * 
  * @author James Lu, Terence Lai
  * @version (1.0)
  */
 public class Ui extends Actor
 {
     private Map map;
 
     private GreenfootImage bg;
     private GreenfootImage[] cache;
     private GreenfootImage[] elements;
     private GreenfootImage[] weapons;
     private GreenfootImage cooldown;
 
     private Font generalFont;
     private Font waveFont;
     private Font name;
     private Font descFont;
     private Font statFont;
 
     private Color[] elementColor;
     private Color goldColor;
     private Color lifeColor;
     private Color waveColor;
     private Color nextColor;
     private Color nameColor;
     private Color descColor;
     private Color statColor;
     private int id;                 //which picture to display
 
     private int gold;
     private int income;
     private int lives;
     private String wave;
     private String next;
 
     private Tower tower;
     private Enemy mob;
     private ArrayList<String> desc;
 
     private Button[][] buttons;
     private SellButton sellButton;
     private UpgradeButton upgradeButton;
     private DebuffButton[] debuffs;
 
     private DummyDebuff[] dumDebuff;
     public Ui(){
         bg = new GreenfootImage (1000, 230);
         cache = new GreenfootImage [6];
         cache[0] = new GreenfootImage ("UI/general.png");
         cache[1] = new GreenfootImage ("UI/buildTower.png");
         cache[2] = new GreenfootImage ("UI/sendMobs.png");
         cache[3] = new GreenfootImage ("UI/selectTower.png");
         cache[4] = new GreenfootImage ("UI/selectTower.png");
         cache[5] = new GreenfootImage ("UI/selectMob.png");
 
         elements = new GreenfootImage [4];
         elements[0] = new GreenfootImage ("UI/fire2.png");
         elements[1] = new GreenfootImage ("UI/water2.png");
         elements[2] = new GreenfootImage ("UI/air2.png");
         elements[3] = new GreenfootImage ("UI/earth2.png");
 
         weapons = new GreenfootImage [3];
        weapons[0] = new GreenfootImage ("UI/bullet.png");
        weapons[1] = new GreenfootImage ("UI/laser.png");
        weapons[2] = new GreenfootImage ("UI/shell.png");
 
        cooldown = new GreenfootImage ("UI/cd.png");
 
         generalFont = new Font ("Times New Roman", 1, 20);
         waveFont    = new Font ("Verdana"        , 1, 25);
         name        = new Font ("Vrinda"         , 1, 35);
         descFont    = new Font ("Vrinda"         , 0, 20);
         statFont    = new Font ("Vrinda"         , 1, 25);
 
         elementColor = new Color [5];
         elementColor[0]= new Color (255, 255, 255);
         elementColor[1]= new Color (255, 255, 255);
         elementColor[2]= new Color (0, 0, 255);
         elementColor[3]= new Color (255, 0, 0);
         elementColor[4]= new Color (150, 75, 0);
 
         lifeColor   = new Color (225, 0  , 0);
         goldColor   = new Color (225, 175, 55);
         waveColor   = new Color (255, 255, 255);
         nextColor   = new Color (0  , 0  , 255);
         nameColor   = new Color (20 , 210, 245);
         descColor   = new Color (255, 255, 255);
         statColor   = new Color (45 , 200, 45 );
 
         sellButton    = new SellButton();
         upgradeButton = new UpgradeButton();
         buttons = new Button[2][32];
         for (int i = 0; i < 32; i++){
             buttons[0][i] = new SendCreeps(i+1);
         }
         debuffs = new DebuffButton [5];
         for (int i = 0; i < 5; i++){
             debuffs[i] = new DebuffButton(i);
         }
         dumDebuff = new DummyDebuff[5];
         for (int i = 0; i < 5; i++){
             dumDebuff[i] = new DummyDebuff(i);
         }
 
         id = 0;
         lives   = 20;
         gold    = 100;
         wave    = "Air";
         next    = "Water";
 
         refresh();
     }
 
     protected void addedToWorld(World world){
         map = (Map)world;
     }
 
     public void act(){
         if (id == 3 || id == 5){           
             //if its describing a mob, always refresh to change hp
             //if its selecting a tower, refresh to show cooldown
             refresh();
         }
     }
 
     /**
      * changes the ui to fit the user's selection
      * 0 for general ui
      * 1 for building a tower
      * 2 for sending creeps
      * 3 for selected a tower
      * 4 for selecting a tower during the buy
      * 5 for selecting a mob
      */
     public void changeUi (int id){
         if       (this.id == 2 && id != 2){
             map.removeObjects (map.getObjects (SendCreeps.class) );
         }else if (this.id == 3 && id != 3){
             map.removeObjects (map.getObjects (UIButton.class) );
         }else if (this.id == 5 && id != 5){
             map.removeObjects (map.getObjects (DummyImage.class) );
         }
         this.id = id;
         if (id == 2){            
             for (int i = 0; i < 3; i++){
                 map.addObject (buttons[0][i], 340, 610+50*i);
             }
         }
         else if (id == 3){       //adds buttons when its selecting a tower
             sellButton      .clicked (false);
             upgradeButton   .clicked (false);
             map.addObject (sellButton   , 850, 675);
             map.addObject (upgradeButton, 850, 730);
 
             map.addObject (debuffs[0]   , 800, 620);
             map.addObject (debuffs[tower.getElement()], 860, 620);
             int[] debuffsBought = tower.getDebuffs();
             for (int n = 0; n < debuffs.length; n++){
                 debuffs[n].bought (false);
             }
             for (int i = 0; i < debuffsBought.length; i++){
                 debuffs[debuffsBought[i]].bought (true);
             }
         }
         refresh();
     }
 
     public void setGeneralData(int lives, int gold, int income){
         this.lives = lives;
         this.gold  = gold;
         this.income= income;
         refresh();
     }
 
     public void setWaveData(int current, int Next){
         if          (current == 0){
             wave = "";
         } else if   (current == 1){
             wave = "Air";
         } else if   (current == 2){
             wave = "Water";
         } else if   (current == 3){
             wave = "Fire";
         } else if   (current == 4){
             wave = "Earth";
         }
 
         if          (Next == 1){
             next = "Air";
         } else if   (Next == 2){
             next = "Water";
         } else if   (Next == 3){
             next = "Fire";            
         } else if   (Next == 4){
             next = "Earth";
         }
         waveColor = elementColor[current];
         nextColor = elementColor[Next];
         refresh();
     }
 
     public void setTowerData (Tower t){
         tower = t;
         desc = t.getDesc();
     }
 
     public void setMobData(Enemy e){
         mob = e;
     }
 
     public Enemy getMob(){
         return mob;
     }
 
     private void refresh(){ 
         if (bg != null){
             bg.clear();
         }
         bg.drawImage (cache[id], 0, 0);
 
         int tempX;          //to calculate the middle of the row to draw the strings
         String tempString;  //temp strings to hold the casted int values to calculate tempX
 
         /**-----------------------------------general data---------------------------------**/
         bg.setFont (generalFont);
         //lives
         tempString = Integer.toString (lives);
         //calculating the x coordinates to center the lives     
         tempX = 140 - tempString.length() * 5;                     
         bg.setColor (lifeColor);
         bg.drawString (tempString, tempX, 40);
 
         //money
         tempString = Integer.toString (gold);
         //calculating the x coordinates to center the gold
         tempX = 110 - tempString.length() * 5;
         bg.setColor (goldColor);
         bg.drawString (tempString, tempX, 75);
 
         //income
         tempString = "+ " + Integer.toString (income);
         tempX = 190 - tempString.length() * 5;
         bg.drawString (tempString, tempX, 75);
 
         /**--------------------------------------waves-------------------------------------**/
         bg.setFont  (waveFont);
         //current wave
         tempX = 145 - wave.length() * 7;
         bg.setColor (waveColor);        
         bg.drawString (wave, tempX, 130);
 
         //next wave
         tempX = 140 - next.length() * 7;
         bg.setColor (nextColor);
         bg.drawString (next, tempX, 185);
 
         if        (id == 0){
 
         } else if (id == 1){                    //building towers
             /*bg.drawImage (elements[0], 320, 30);
             bg.drawImage (elements[1], 510, 30);
             bg.drawImage (elements[2], 700, 30);
             bg.drawImage (elements[3], 890, 30);*/
         } else if (id == 2){                    //sending creeps
         } else if (id == 3 || id == 4){         //tower selection
             //name of the tower
             bg.setColor (nameColor);
             bg.setFont  (name);
             tempString = tower.getName();
             bg.drawString (tempString, 250, 60);
 
             //tower description
             bg.setColor (descColor);
             bg.setFont  (descFont);
             tempX = tower.getLevel();                   //level of the tower
             tempString = "Level " + Integer.toString(tempX);
             //cooldown of weapon
             bg.drawString (tempString, 260, 100);      
             bg.drawImage (weapons[tempX-1], 460, 160);                  //weapon id
             if (id == 3){
                 //transparancy of the cd
                 tempX = 255 - Math.round ((float)tower.getCD() / tower.getAttackSpeed() * 255);
                 cooldown.setTransparency (tempX);
                 bg.drawImage (cooldown, 460, 160);                          //cooldown
             }
 
             for (int i = 0; i < desc.size(); i++){
                 tempString = desc.get(i);
                 bg.drawString (tempString, 490, 85 + i * 20);
             }
 
             //stats
             bg.setColor (statColor);
             bg.setFont  (statFont);
             tempString = Integer.toString(tower.getDmg() );      //damage
             bg.drawString (tempString, 375, 152);
             tempString = Integer.toString(tower.getRange() );    //range
             bg.drawString (tempString, 375, 200);
             tempString = Integer.toString(tower.getAttackSpeed());  //attack speed
             bg.drawString (tempString, 550, 152);
         } else if (id == 5){                    //mob selection
             //name of the mob
             bg.setColor (nameColor);
             bg.setFont  (name);
             tempString = mob.getName();
             tempX = 320 - tempString.length() * 6;
             bg.drawString (tempString, tempX, 60);
 
             //stats
             //hp
             bg.setColor (statColor);
             bg.setFont  (statFont);
             tempString = Integer.toString (mob.getHp()) + "/" + Integer.toString (mob.getMaxHp());
             bg.drawString (tempString, 350, 152);
 
             //armor
             tempString = Integer.toString (mob.getArmor());
             bg.drawString (tempString, 350, 200);
 
             //speed
             tempString = Float.toString (mob.getSpeed());
             bg.drawString (tempString, 545, 200);
 
             //type
             bg.setColor (elementColor[mob.getType()]);
             tempString = mob.getStringType();
             bg.drawString (tempString, 500, 100);
 
             //debuffs
             ArrayList<Debuff> debuffs = mob.getDebuffs();
             for (int i = 0; i < debuffs.size(); i++){
                 Debuff d = debuffs.get(i);
                 DummyDebuff dd = new DummyDebuff (d.getId());
                 map.addObject (dd, 600 + 50*i, 700);
             }
         }
         this.setImage (bg);
     }
 
     public int getId(){
         return id;
     }
 }
