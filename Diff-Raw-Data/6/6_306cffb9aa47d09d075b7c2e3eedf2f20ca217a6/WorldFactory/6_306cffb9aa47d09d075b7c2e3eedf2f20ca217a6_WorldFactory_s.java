 /*
  * Copyright (c) 2009-2011 Daniel Oom, see license.txt for more info.
  */
 
 package game.factories;
 
 import game.CacheTool;
 import game.components.Message;
 import game.entities.Entity;
 import game.entities.IEntity;
 import game.entities.InvisibleRectangle;
 import game.events.impl.DamageEntitiesEvent;
 import game.events.impl.RemoveEvent;
 import game.triggers.ICondition;
 import game.triggers.IEffect;
 import game.triggers.Trigger;
 import game.triggers.condition.AllInactiveCondition;
import game.triggers.condition.AlwaysTrueCondition;
 import game.triggers.condition.AnyInactiveCondition;
 import game.triggers.condition.TimerCondition;
import game.triggers.effects.AddTriggersEffect;
 import game.triggers.effects.ExecuteWithDelayEffect;
 import game.triggers.effects.LevelCompleteEffect;
 import game.triggers.effects.MainMenuEffect;
 import game.triggers.effects.SetForegroundEffect;
 import game.triggers.effects.SpawnWithSend;
 import game.world.World;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 import loader.ICache;
 import loader.data.DataException;
 import loader.data.json.CreepsData;
 import loader.data.json.CreepsData.CreepData;
 import loader.data.json.LevelData;
 import loader.data.json.LevelData.CreepSpawnData;
 import loader.parser.ParserException;
 import main.Locator;
 import math.Rectangle;
 
 import org.newdawn.slick.Image;
 
 import states.GameState;
 import states.StateManager;
 
 public class WorldFactory {
   private static final int PLAYER_DAMAGE = 10;
   private static final float TRIGGER_DELAY = 5.0f;
 
   private static final String GAME_OVER_IMAGE = "textures/text/gameover.png";
 
   private final GameState gameMode;
   private final StateManager stateManager;
 
   private final Rectangle rect;
   private final List<Entity> players;
 
   private EntityFactory entityFactory;
 
   private World world;
 
   public WorldFactory(GameState gameMode, StateManager stateManager,
                       EntityFactory entityFactory, Rectangle rect,
                       List<Entity> players) {
     this.gameMode      = gameMode;
     this.stateManager  = stateManager;
     this.entityFactory = entityFactory;
     this.rect          = rect;
     this.players       = players;
   }
 
   /**
    * Adds two basic rectangles to the world. rectBig, the rectangle that kills
    * anything that goes to far away from the visible area (to save memory).
    * rectCreep kills creeps and deals damage to the players when the creeps have
    * reached their goal.
    */
   private void addRectangles() {
     /**
      * The layout of rectangles:
      * |---------------------------------|
      * | rectBig                         |
      * |                                 |
      * |                                 |
      * |-----------|-----------|         |
      * | rectCreep | rectWorld |         |
      * |-----------|-----------|         |
      * |                                 |
      * |                                 |
      * |                                 |
      * |---------------------------------|
      */
 
     InvisibleRectangle rectBig = new InvisibleRectangle(
       -1 * rect.getWidth(), -1 * rect.getHeight(),
        3 * rect.getWidth(),  3 * rect.getHeight()
     );
 
     InvisibleRectangle rectCreepKiller = new InvisibleRectangle(
       -rect.getWidth(), 0,
        rect.getWidth(), rect.getHeight()
     );
 
     rectBig.onNotContainsEvent.add(new RemoveEvent());
     rectCreepKiller.onContainsEvent.add(new RemoveEvent());
     rectCreepKiller.onContainsEvent.add(new DamageEntitiesEvent(players, PLAYER_DAMAGE));
 
     world.addLast(rectBig);
     world.addLast(rectCreepKiller);
   }
 
   /**
    * Adds creep spawning triggers to the world.
    * Also returns a list of the creeps that will be spawned.
    * @param world the world to add the triggers to
    * @param rect the bounding rectangle of the world
    * @param spawnsData the data used for creating the creeps
    * @return a list of the creeps that will be spawned
    */
   private List<Entity> addCreepTriggers(List<CreepSpawnData> spawnsData)
       throws DataException, ParserException, IOException {
     assert spawnsData != null;
 
     CreepsData creepsData = CacheTool.getCreeps(Locator.getCache());
     LinkedList<Entity> result = new LinkedList<>();
 
     for (CreepSpawnData spawnData : spawnsData) {
       Trigger t = new Trigger(false);
       t.addCondition(new TimerCondition(0, spawnData.spawnTime));
 
       CreepData creepData = creepsData.getCreep(spawnData.creep);
       float x = rect.getX2() + creepData.hitbox.width;
       float y = Locator.getRandom().nextFloat(
         rect.getY1(),
         rect.getY2() - creepData.hitbox.height
       );
 
       Entity creep = entityFactory.makeCreep(x, y, (float) -Math.PI, creepData);
 
       result.add(creep);
 
       t.addEffect(new SpawnWithSend(creep, Message.START_ANIMATION, null));
     }
 
     return result;
   }
 
   private Trigger makeDelayedTrigger(float delay, ICondition condition,
                                      List<? extends IEffect> preEffects,
                                      List<? extends IEffect> postEffects) {
     Trigger delayedTrigger = new Trigger(false);
     delayedTrigger.addCondition(condition);
     delayedTrigger.addAllEffects(preEffects);
     delayedTrigger.addEffect(new ExecuteWithDelayEffect(delay, postEffects));
 
     return delayedTrigger;
   }
 
   private void addLevelTriggers(LevelData level, List<Entity> creeps)
       throws ParserException, IOException {
     ICache cache           = Locator.getCache();
    Image imgLevelStart    = CacheTool.getImage(cache, level.level);
     Image imgLevelComplete = CacheTool.getImage(cache, level.completed);
     Image imgGameOver      = CacheTool.getImage(cache, GAME_OVER_IMAGE);
 
     world.addTrigger(makeDelayedTrigger(
       TRIGGER_DELAY,
       new AllInactiveCondition(creeps),
       Arrays.asList(new SetForegroundEffect(Locator.getUI(), imgLevelComplete)),
       Arrays.asList(new LevelCompleteEffect(gameMode))));
 
     world.addTrigger(makeDelayedTrigger(
       TRIGGER_DELAY,
       new AnyInactiveCondition(players),
       Arrays.asList(new SetForegroundEffect(Locator.getUI(), imgGameOver)),
       Arrays.asList(new MainMenuEffect(stateManager))));
   }
 
   public World makeLevel(LevelData level)
       throws DataException, ParserException, IOException {
 
     world = new World();
 
     addRectangles();
 
     for (IEntity p : players) {
       world.addLast(p);
     }
 
     List<Entity> creeps = addCreepTriggers(level.creeps);
 
     addLevelTriggers(level, creeps);
 
     return world;
   }
 }
