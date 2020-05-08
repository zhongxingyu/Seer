 public class Robot{
   enum Emotions{DestroyAllHumans, Sober, Normal};
   public enum Move {
     Left, Right, Up, Down, Wait, Abort, Shave
   }
 
   private Point position;
   private int lambdaCount;
   private int score;
   private int waterTime;
   private int waterThreshold;
   public Robot(int x, int y) {
     position = new Point(x, y);
     lambdaCount = 0;
     score = 0;
     waterTime = 0;
     waterThreshold = 10;
   }
 
   public Robot(Robot oldRobot) {
     position = new Point(oldRobot.position.x, oldRobot.position.y);
     lambdaCount = oldRobot.lambdaCount;
     score = oldRobot.score;
     waterTime = oldRobot.waterTime;
     waterThreshold = oldRobot.waterThreshold;
   }
     
   public void moveUp() {
     position.y++;
     score--;
   }
     
   public void moveDown() {
     position.y--;
     score--;
   }
     
   public void moveLeft() {
       position.x--;
       score--;
   }
   
   public void moveRight() {
       position.x++;
       score--;
   }
   public void setPosition(int x, int y)
   {
     position.x = x;
     position.y = y;
    score--;
   }
   public void stay() {
       score--;
   }
   
   public void gainLambda() {
       lambdaCount++;
       score += 25;
   }
   
   public void liftLambda() {
       score += 50*lambdaCount;
   }
   
   public void abort() {
       score += 25*lambdaCount;
   }
   
   public int getScore() {
       return score;
   }
   
   public int getLambda() {
       return lambdaCount;
   }
   
   public void cleanWaterTime() {
       waterTime = 0;
   }
   
   public void stayInWater() {
       waterTime++;
   }
   
   public int getWaterTime() {
       return waterTime;
   }
   public int getWaterThreshold()
   {
     return waterThreshold;
   }
   public void setWaterThreshold(int threshold)
   {
     waterThreshold = threshold;
   }
   public Point getPosition()
   {
     return position;
   }
 
 }
