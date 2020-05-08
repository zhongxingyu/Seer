 package fr.aumgn.dac2.game;
 
 import java.util.HashMap;
 import java.util.Map;
 
 
 import fr.aumgn.dac2.DAC;
import fr.aumgn.dac2.exceptions.UnknownGameType;
 import fr.aumgn.dac2.game.classic.ClassicGame;
 import fr.aumgn.dac2.game.colonnisation.Colonnisation;
 import fr.aumgn.dac2.game.start.GameStartData;
 import fr.aumgn.dac2.game.training.Training;
 
 public abstract class GameFactory {
 
     private static Map<String, GameFactory> factories =
             new HashMap<String, GameFactory>();
     private static Map<String, GameFactory> byAliases =
             new HashMap<String, GameFactory>();
 
     public static void register(String name, GameFactory factory,
             String... aliases) {
         factories.put(name, factory);
 
         byAliases.put(name, factory);
         for (String alias : aliases) {
             byAliases.put(alias, factory);
         }
     }
 
     public static GameFactory get(String name) {
         return factories.get(name);
     }
 
    public static GameFactory getByAlias(DAC dac, String alias) {
        if (!byAliases.containsKey(alias)) {
            throw new UnknownGameType(dac, alias);
        }
         return byAliases.get(alias);
     }
 
     public int getMinimumPlayers() {
         return 2;
     }
 
     public abstract Game createGame(DAC dac, GameStartData data);
 
     static {
         GameFactory.register("classic", new GameFactory() {
 
             @Override
             public Game createGame(DAC dac, GameStartData data) {
                 return new ClassicGame(dac, data);
             }
         }, "cl", "default", "def");
 
         GameFactory.register("training", new GameFactory() {
 
             @Override
             public int getMinimumPlayers() {
                 return 1;
             }
 
             @Override
             public Game createGame(DAC dac, GameStartData data) {
                 return new Training(dac, data);
             }
         }, "tr", "t");
 
         GameFactory.register("colonnisation", new GameFactory() {
 
             @Override
             public Game createGame(DAC dac, GameStartData data) {
                 return new Colonnisation(dac, data);
             }
         }, "col", "c");
     }
 }
