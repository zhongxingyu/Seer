 package game;
 
 import java.util.ArrayDeque;
 import java.util.Deque;
 import java.util.logging.Logger;
 import model.GameAction;
 import model.GameData;
 
 public class TargetStrategy implements Strategy {
 
     private static Logger log = Logger.getLogger(TargetStrategy.class.getName());
 
     public TargetStrategy() {
     }
 
     public String toString() {
         return "target";
     }
 
     @Override
     public Deque<GameAction> execute(GameData gameData) {
         String enemy = gameData.getEnemy();
         log.fine(enemy);
         Deque<GameAction> deque = new ArrayDeque<>();
        GameAction backstab = new GameAction("backstab " + enemy);
         GameAction confuse = new GameAction("confuse " + enemy);
         GameAction heartplunge = new GameAction("heartplunge " + enemy);
         GameAction enervate = new GameAction("enervate " + enemy);
         deque.add(backstab);
         deque.add(confuse);
         deque.add(heartplunge);
         deque.add(enervate);
         return deque;
     }
 }
