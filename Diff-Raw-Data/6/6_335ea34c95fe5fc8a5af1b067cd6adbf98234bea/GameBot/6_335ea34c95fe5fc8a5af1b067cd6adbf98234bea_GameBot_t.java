 package game;
 
 import com.jme3.math.Quaternion;
 import com.jme3.math.Vector3f;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import java.math.*;
 
 /**
  *
  * @author JP
  * @author NM
  */
 public class GameBot
 {
     static enum ActionType
     {
         POSITION,
         ROTATION
     };
     
     private class Action
     {
         ActionType type;
         Vector3f data;
         
         Action(ActionType type, Vector3f data)
         {
             this.type = type;
             this.data = data;
         }
     };
     
     public static void main(String[] args)
     {
         System.out.println("Starting");
         GameBot bot = new GameBot();
         bot.load();
         bot.run();
     }
     Random rand;
     GameModel model;
     boolean amIStuckInOneRegion;
     int middleOfRegionX;
     int middleOfRegionY;
     List<Action> actions = new LinkedList<Action>();
     String animal[] = new String[]{"Bear","Lion","Monkey","Fish","Squirrel","Tiger","Panther","Dog","Wolf"};
     String color[] = new String[]{"Red","Blue","Gold","Bronze","Green","Orange","Silver","Black","Purple"};
         
     GameBot()
     {
         model = new GameModel(null);
         rand = new Random();
         amIStuckInOneRegion = rand.nextBoolean();
         if (amIStuckInOneRegion){
           middleOfRegionX = (rand.nextInt(11)*100-517)+40;
           middleOfRegionY = (rand.nextInt(11)*100-517)+40;                
         }       
        System.out.println("AmIStuck: "+amIStuckInOneRegion);
     }
     
     public void load()
     {
         //actions.add(new Action(ActionType.POSITION, new Vector3f(-300, -300, 30)));
     }
     
     public void run()
     { 
         model.name = color[rand.nextInt(color.length)]+"_"+animal[rand.nextInt(animal.length)]+"_"+rand.nextInt(10);
         model.position = new Vector3f();
         model.rotation = new Quaternion();
         
         int connectPort = 5000 + rand.nextInt(2800);
         
         model.connect("GEOFF-LENOVO", 8000, connectPort);
         System.out.println("connected");
  
         Vector3f targetPosition = new Vector3f();
         Vector3f startPosition = new Vector3f();
         Vector3f currentPosition = new Vector3f();
         boolean inAction = false;
         
         long lastTime = System.nanoTime();
         boolean running = true;
         while(running)
         {
             // get timestep
             long thisTime = System.nanoTime();
             long difference = thisTime - lastTime;
             double ts = (double)difference / 1000000000.f;
             lastTime = thisTime;
             
             if(inAction)
             {
                 targetPosition.z -= ts;
                 boolean doneWithMove = false;
                 
                 if(targetPosition.z <= 0)
                 {
                     doneWithMove = true;
                 }
                 else
                 {
                     float percentDone = (startPosition.z - targetPosition.z) / startPosition.z;
                     currentPosition.x = (targetPosition.x - startPosition.x) * percentDone + startPosition.x;
                     currentPosition.y = (targetPosition.y - startPosition.y) * percentDone + startPosition.y;
                     model.position = new Vector3f(currentPosition.x, 0, currentPosition.y);
                     model.positionChanged = true;                                        
                 }
                 
                 if(doneWithMove)
                 {
                     inAction = false;
                     model.position = new Vector3f(targetPosition.x, 0, targetPosition.y);
                     model.positionChanged = true;
                 }
                 
                 if(model.positionChanged)
                 {
                   Region oldRegion = model.region;
                   Region newRegion = GameModel.getPlayersRegion(model.getPlayerPosition());
                   if (newRegion.x != oldRegion.x || newRegion.y != oldRegion.y)
                   {
                     model.regionChanged=true;
                   }
                 }
                                                 
                 //System.out.println("moved to " + model.position);
                 
             }
             else if(actions.size() > 0)
             {
                 targetPosition = actions.get(0).data;
                 startPosition = new Vector3f(model.position.x, model.position.z, targetPosition.z);
                 currentPosition = new Vector3f(startPosition);
                 
                 actions.remove(0);
                 inAction = true;
                 
                 //System.out.println("===============================");
                 //System.out.println("move from " + startPosition + " to " + targetPosition);
             }
             else
             {
                 if (amIStuckInOneRegion)
                 {
                   int newX = middleOfRegionX+(rand.nextInt(40)-20);
                   int newY = middleOfRegionY+(rand.nextInt(40)-20);
                   double distance = Math.sqrt(Math.pow(newX-model.position.x, 2)+Math.pow(newY-model.position.z,2));
                   int time = ((int)distance/20)+1;                
                   actions.add(new Action(ActionType.POSITION,new Vector3f(newX,newY,time)));                                  
                 }
                 else
                 {
                  int newX = rand.nextInt(1000)-517;
                  int newY = rand.nextInt(1000)-517;
                   double distance = Math.sqrt(Math.pow(newX-model.position.x, 2)+Math.pow(newY-model.position.z,2));
                   int time = ((int)distance/20)+1;                
                   actions.add(new Action(ActionType.POSITION,new Vector3f(newX,newY,time)));
                 }
                 //running = false;
             }
             
             try
             {
                 Thread.sleep(100);
             }
             catch(Exception e)
             {
             }
         }
         
         model.shutdown();
     }
 }
