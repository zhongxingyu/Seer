 package Learning.Towers.AI;
 
 import Learning.Towers.Entities.Entity;
 import Learning.Towers.Entities.EntityFactory;
 import Learning.Towers.Entities.Meta.Group;
 import Learning.Towers.Faction;
 import Learning.Towers.Influence.InfluenceMap;
 import Learning.Towers.Main;
 import Learning.Towers.Vector2D;
 
 /**
  * Primary AI class
  * <p/>
  * Gives the actual orders
  */
 public class Commander {
 
     private Faction faction;
     private InfluenceMap influence;
 
     private AttackFinder attackFinder;
     private DefenseFinder defenseFinder;
 
     public Commander(Faction faction) {
         this.faction = faction;
 
         attackFinder = new AttackFinder(this, 30);
         defenseFinder = new DefenseFinder(this, 30);
     }
 
     public void update() {
         attackFinder.update();
         defenseFinder.update();
     }
 
     public Faction getFaction() {
         return faction;
     }
 
     public void groupFilled(Group group) {
         System.out.println("Group filled");
         group.switchToFollow(attackFinder.nextTarget);
     }
 
     public void buildTower() {
         if (defenseFinder.nextTarget != null) {
             Entity tower = EntityFactory.getTower(faction, defenseFinder.nextTarget);
             Main.GAME_LOOP.addEntity(tower);
 
             defenseFinder.nextTarget = null;
         }
     }
 }
 
 abstract class TacticalAnalysis {
     private int tickFrequency, currentTick;
 
     protected Commander commander;
 
     public TacticalAnalysis(Commander commander, int tickFrequency) {
         this.commander = commander;
         this.tickFrequency = tickFrequency;
     }
 
     public final void update() {
         currentTick = (currentTick == tickFrequency) ? 0 : currentTick + 1;
         if (currentTick == 0) updateSpecialisation();
     }
 
     public abstract void updateSpecialisation();
 }
 
 /**
  * This will churn out attack orders
  */
 class AttackFinder extends TacticalAnalysis {
     double[][] enemyInfluence;
 
     Vector2D nextTarget;
 
     AttackFinder(Commander commander, int tickFrequency) {
         super(commander, tickFrequency);
     }
 
     public void updateSpecialisation() {
         enemyInfluence = Main.INFLUENCE_MAP.getEnemyInfluence(commander.getFaction());
 
         int lowX = 0, lowY = 0;
         boolean foundSomewhere = false;
         for (int x = 0; x < enemyInfluence.length; x++) {
             for (int y = 0; y < enemyInfluence[x].length; y++) {
                 if (enemyInfluence[x][y] != 0) {
                     if (!foundSomewhere) {
                         foundSomewhere = true;
                         lowX = x;
                         lowY = y;
                     } else if (enemyInfluence[x][y] < enemyInfluence[lowX][lowY]) {
                         lowX = x;
                         lowY = y;
                     }
                 }
             }
         }
 
         // It is possible not to find anything at all
         if (foundSomewhere) {
             nextTarget = new Vector2D(lowX * Main.INFLUENCE_MAP.getCellSize(), lowY * Main.INFLUENCE_MAP.getCellSize());
         }
     }
 }
 
 /**
  * This class will locate the next location to build a tower
  */
 class DefenseFinder extends TacticalAnalysis {
     double[][] influence;
 
     Vector2D nextTarget;
 
     DefenseFinder(Commander commander, int tickFrequency) {
         super(commander, tickFrequency);
     }
 
     public void updateSpecialisation() {
         influence = Main.INFLUENCE_MAP.getInfluence(commander.getFaction());
 
         int lowX = 0, lowY = 0;
         boolean foundSomewhere = false;
         for (int x = 0; x < influence.length; x++) {
             for (int y = 0; y < influence[x].length; y++) {
                if (influence[x][y] > 1) {
                    if (!foundSomewhere) foundSomewhere = true;
                     if (influence[x][y] < influence[lowX][lowY]) {
                         lowX = x;
                         lowY = y;
                     }
                 }
             }
         }
 
         if (foundSomewhere) {
             nextTarget = new Vector2D(lowX * Main.INFLUENCE_MAP.getCellSize(), lowY * Main.INFLUENCE_MAP.getCellSize());
 
             System.out.println("Found somewhere");
             System.out.println("Location: " + nextTarget);
             System.out.println("Value: " + influence[lowX][lowY]);
 
             System.out.println();
         }
     }
 }
