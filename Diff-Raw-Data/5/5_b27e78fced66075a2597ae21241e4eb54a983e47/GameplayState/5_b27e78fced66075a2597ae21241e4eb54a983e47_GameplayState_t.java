 package aigilas.states;
 
 import aigilas.classes.PlayerClassRegistry;
 import aigilas.creatures.StatsRegistry;
 import aigilas.creatures.impl.CreatureFactory;
import aigilas.creatures.impl.Player;
 import aigilas.dungeons.Dungeon;
 import aigilas.dungeons.DungeonFloor;
 import aigilas.hud.HudRenderer;
 import aigilas.net.Client;
 import aigilas.reactions.ReactionRegistry;
 import aigilas.skills.SkillRegistry;
 import aigilas.statuses.StatusRegistry;
 import sps.core.Logger;
 import sps.entities.Entity;
 import sps.entities.EntityManager;
 import sps.particles.ParticleEngine;
 import sps.states.State;
 import sps.states.StateManager;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class GameplayState implements State {
     public GameplayState() {
 
     }
 
     List<Entity> players = new ArrayList<Entity>();
 
     @Override
     public void update() {
         players = EntityManager.get().getPlayers();
         boolean allDead = true;
         for (Entity player : players) {
            Player p = (Player) player;
            if (p.isActive() && p.isPlaying()) {
                 allDead = false;
             }
         }
         if (allDead) {
             StateManager.loadState(new GameLoseState());
         }
         EntityManager.get().update();
     }
 
     @Override
     public void asyncUpdate() {
     }
 
     @Override
     public void load() {
         Logger.info("Generating the dungeon...");
         SkillRegistry.get();
         StatusRegistry.get();
         ReactionRegistry.get();
         PlayerClassRegistry.get();
         StatsRegistry.get();
         EntityManager.reset();
         CreatureFactory.reset();
         Dungeon.start();
         DungeonFloor.reset();
         Client.get().dungeonHasLoaded();
         EntityManager.get().loadContent();
         //Gdx.app.exit();
     }
 
     @Override
     public void unload() {
         EntityManager.get().clear();
         HudRenderer.reset();
         ParticleEngine.reset();
     }
 
     @Override
     public String getName() {
         return "GameplayState";
     }
 
     @Override
     public void draw() {
         EntityManager.get().draw();
     }
 }
