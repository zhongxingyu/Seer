 import greenfoot.*;
 
 public abstract class Player extends Actor {
     private int speed;
     private int initialSpeed;
     private Bag.BagType bagType;
 
     protected GreenfootImage image1;
     protected GreenfootImage image2;
     protected GreenfootImage image3;
     protected GreenfootImage image4;
     protected GreenfootImage image5;
     protected GreenfootImage image6;
     protected GreenfootImage image7;
     protected GreenfootImage image8;
 
     protected Player(int speed) {
         this.initialSpeed = speed;
         this.speed = speed;
         setBagType(Bag.BagType.SANDBAG);
     }
 
     @Override
     public void act() {
         if (speed / 4 <= 0) speed = 4;
         
         if (Greenfoot.isKeyDown("w")) {
             move(0, -speed / 4);
             switchImageStraight();
         }
 
         if (Greenfoot.isKeyDown("s")) {
             move(0, +speed / 4);
             switchImageBack();
         }
 
         if (Greenfoot.isKeyDown("a")) {
             move(-speed / 4, 0);
             switchImageLeft();
         }
 
         if (Greenfoot.isKeyDown("d")) {
             move(+speed / 4, 0);
             switchImageRight();
         }
 
         if (Greenfoot.isKeyDown("1")) {
             setBagType(Bag.BagType.SANDBAG);
         }
         
         if (Greenfoot.isKeyDown("2")) {
             setBagType(Bag.BagType.CEMENT_BAG);
         }
         
         if (Greenfoot.isKeyDown("3")) {
             setBagType(Bag.BagType.GRAVEL_BAG);
         }
         
         if (Greenfoot.isKeyDown("4")) {
             setBagType(Bag.BagType.WOODEN_DIVIDER);
         }
 
         if (Greenfoot.isKeyDown("5")) {
             setBagType(Bag.BagType.IRON_DIVIDER);
         }
         
         if (Greenfoot.isKeyDown("6")) {
             setBagType(Bag.BagType.CONCRETE_DIVIDER);
         }
  
         if(Greenfoot.mouseClicked(null)) {
             Bag bag = Bag.createBag(bagType);
             ((FloodWorld)getWorld()).coinCounter.add(bag.getCost());
             getWorld().addObject(bag, getX(), getY());
             Greenfoot.playSound("sandbag.wav");
         }
         
         Actor water = getOneObjectAtOffset(0, -1, Water.class);
         if (water != null) {
             move(0, 1);
         }
             
         Actor coin = getOneObjectAtOffset(0, 0, Coin.class);
         if (coin != null) {
             getWorld().removeObject(coin);
             Greenfoot.playSound("Coin.mp3");
             ((FloodWorld)getWorld()).coinCounter.add(1);
         }
     }
 
     private void move(int dx, int dy) {
         if(getY() > 69) {
             setLocation(getX() + dx, 69);
             return;
         }
         
         setLocation(getX() + dx, getY() + dy);
     }
 
     public void carryBag(Bag bag) {
         if (bag.getWorld() != null) bag.getWorld().removeObject(bag);
         speed = initialSpeed - bag.getWeight();
         bagType = bag.getType();
     }
     
     public void setBagType(Bag.BagType type) {
         carryBag(Bag.createBag(type));
     }
 
     protected void switchImageLeft()
     {
         if (getImage() == image1)
         {
             setImage(image2);
         }
         else
         {
             setImage(image1);
         }
     }
 
     protected void switchImageRight()
     {
         if (getImage() == image3)
         {
             setImage(image4);
         }
         else
         {
             setImage(image3);
         }
     }
 
     protected void switchImageStraight()
     {
         if (getImage() == image5)
         {
             setImage(image6);
         }
         else
         {
             setImage(image5);
         }
     }
 
     protected void switchImageBack()
     {
         if (getImage() == image7)
         {
             setImage(image8);
         }
         else
         {
             setImage(image7);
         }
     }
 }
