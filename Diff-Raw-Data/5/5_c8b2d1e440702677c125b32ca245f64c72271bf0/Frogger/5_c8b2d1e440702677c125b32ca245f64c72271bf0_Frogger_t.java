 package ph.sm.frogger;
 
 import android.graphics.Color;
 import android.graphics.Point;
 import ch.aplu.android.*;
 
 public class Frogger extends GameGrid
 {
 
   public void main() {
 	paintBg();
     setSimulationPeriod(80);
     Frog frog = new Frog();
     addActor(frog, new Location(400, 590), Location.NORTH);
     frog.setCollisionRectangle(new Point(0, 0), 30, 30);
 
     Car[] cars = new Car[20];
     for (int i = 0; i < 10; i++)
     {
       cars[i] = new Car(i%5);
       cars[i].setHorzMirror(true);
       frog.addCollisionActor(cars[i]);
     }
     for (int i = 0; i < 10; i++)
     {
       cars[10 + i] = new Car(i%5);
       frog.addCollisionActor(cars[10 + i]);
     }
 
     
     for (int i = 0; i < 5; i++)
       addActor(cars[i], new Location(350 * i, 120), Location.WEST);
     for (int i = 5; i < 10; i++)
       addActor(cars[i], new Location(350 * (i - 5), 370), Location.WEST);
     for (int i = 10; i < 15; i++)
       addActor(cars[i], new Location(350 * (i - 10), 250), Location.EAST);
     for (int i = 15; i < 20; i++)
       addActor(cars[i], new Location(350 * (i - 15), 500), Location.EAST);
 
     addTouchListener(frog, GGTouch.press);
     setTitle("Frogger");
     showToast("Tap to make the frog walk into the tapped direction");
     doRun();
   }
   
   private void paintBg() {
 	getBg().clear(GRAY);
 	paintStreet(75, 220);
 	paintStreet(325, 220);
 	getBg().setPaintColor(Color.BLUE);
 	getBg().fillCircle(new Point(300, -200), 235);
 }
   
 private void paintStreet(int height, int size) {
 	GGBackground bg = getBg();
 	bg.setPaintColor(Color.BLACK);
 	bg.fillRectangle(new Point(0, height), new Point(1000, height + size));
 	bg.setPaintColor(Color.YELLOW);
 	bg.drawLine(new Point(0,height + size/2 - 2), new Point(1000,height + size/2 - 2));
 	bg.drawLine(new Point(0,height + size/2 + 2), new Point(1000,height + size/2 + 2));
 }
 
 public Frogger()
   {
     super(600,600, 1);
    setScreenOrientation(PORTRAIT);
   }
 
   public static void main(String[] args)
   {
     new Frogger();
   }
 }
 
 //---------------class Car.java ---------------------------------------
 
 class Car extends Actor
 {
   public Car(int  type)
   {
     super("car", 5);
     show(type);
   }
 
   public void act()
   {
     move();
     if (getLocation().x < -100)
       setLocation(new Location(1650, getLocation().y));
     if (getLocation().x > 1650)
       setLocation(new Location(-100, getLocation().y));
   }
 }
 
 // ---------------- class Frog.java-----------------------------------
 
 class Frog extends Actor implements GGTouchListener
 {
   private boolean isFinished = false;
 
   public Frog()
   {
     super(true, "frog");
   }
 
   public void act()
   {
     if (getY() < 25 && getX() < 380 && getX() > 220)
     {
       if (!isFinished)
       {
         isFinished = true;
         gameGrid.showToast("You safely reached the lake!");
         gameGrid.playSound("ping");
       }
     }
     else {
       isFinished = false;
     }
   }
 
   public boolean touchEvent(GGTouch touch)
   {
      if (isFinished)
 	return true;
      Location touchLoc = gameGrid.toLocation(touch.getX(), touch.getY());
      double dir = getLocation().getDirectionTo(touchLoc);
      setDirection(dir);	
      move(10);
     return true;
   }
 
   public int collide(Actor actor1, Actor actor2)
   {
     gameGrid.playSound("boing");
     setLocation(new Location(400, 560));
     setDirection(Location.NORTH);
     return 0;
   }
 } 
