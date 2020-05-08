 package ch.nevill.boxroyal.server;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import ch.nevill.boxroyal.proto.Box;
 import ch.nevill.boxroyal.proto.Bullet;
 import ch.nevill.boxroyal.proto.Direction;
 import ch.nevill.boxroyal.proto.GameLog;
 import ch.nevill.boxroyal.proto.GameState;
 import ch.nevill.boxroyal.proto.Operation;
 import ch.nevill.boxroyal.proto.OperationError;
 import ch.nevill.boxroyal.proto.Player;
 import ch.nevill.boxroyal.proto.Point;
 import ch.nevill.boxroyal.proto.Point.Builder;
 import ch.nevill.boxroyal.proto.Round;
 import ch.nevill.boxroyal.proto.Size;
 import ch.nevill.boxroyal.proto.Soldier;
 import ch.nevill.boxroyal.proto.SoldierOrBuilder;
 import ch.nevill.boxroyal.proto.View;
 
 import com.google.common.base.Function;
 import com.google.common.base.Optional;
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Ordering;
 
 public class Match implements Runnable {
 
   private static final Log log = LogFactory.getLog(Match.class);
 
   private static final int MAX_ROUNDS = 200;
 
   private static class OperationException extends Exception {
     private static final long serialVersionUID = -2405849614106891603L;
     private final OperationError code;
 
     public OperationException(OperationError code) {
       this.code = code;
     }
 
     public OperationError getCode() {
       return code;
     }
   }
 
   private static class MatchClient {
     public final Client client;
     public final Player player;
     public MatchClient(int index, Client client, Player player) {
       this.client = client;
       this.player = player;
     }
   }
 
   private static Point applyDirection(Point point, Direction direction) {
     Builder builder = point.toBuilder();
     switch (direction.getNumber()) {
       case Direction.NORTH_VALUE:
         builder.setY(point.getY() + 1);
         break;
       case Direction.EAST_VALUE:
         builder.setX(point.getX() + 1);
         break;
       case Direction.SOUTH_VALUE:
         builder.setY(point.getY() - 1);
         break;
       case Direction.WEST_VALUE:
         builder.setX(point.getX() - 1);
         break;
       default:
         throw new IllegalArgumentException();
     }
     return builder.build();
   }
 
   private static boolean pointInArea(Point point, Size area) {
     return point.getX() >= 0
         && point.getY() >= 0
         && point.getX() < area.getWidth()
         && point.getY() < area.getHeight();
   }
 
   private static boolean pointInPath(Point start, Direction direction, Point target) {
     if (start.getX() != target.getX() && start.getY() != target.getY()) {
       return false;
     }
     if (start.getX() == target.getX() && start.getY() == target.getY()) {
       return true;
     }
 
     switch (direction.getNumber()) {
       case Direction.NORTH_VALUE:
         return target.getY() > start.getY();
       case Direction.EAST_VALUE:
         return target.getX() > start.getX();
       case Direction.SOUTH_VALUE:
         return target.getY() < start.getY();
       case Direction.WEST_VALUE:
         return target.getX() < start.getX();
       default:
         throw new IllegalArgumentException();
     }
   }
 
   private final GameState.Builder simulationState;
   private GameLog.Builder gameLog;
   private final int roundId = 0;
   private final ImmutableList<MatchClient> players;
   private final int matchId;
   private final Map<Integer, Soldier.Builder> soldierIdMap;
 
   public Match(int matchId, List<Client> players, GameState startState) {
     if (players.size() != startState.getPlayerCount()) {
       throw new IllegalArgumentException();
     }
     ImmutableList.Builder<MatchClient> playersBuilder = ImmutableList.builder();
     for (int i = 0; i < players.size(); i++) {
       playersBuilder.add(new MatchClient(i, players.get(i), startState.getPlayer(i)));
     }
     this.players = playersBuilder.build();
     this.matchId = matchId;
     this.simulationState = startState.toBuilder();
     this.soldierIdMap = new HashMap<>();
     for (Soldier.Builder s : this.simulationState.getSoldierBuilderList()) {
       this.soldierIdMap.put(s.getSoldierId(), s);
     }
   }
 
   private Optional<Soldier.Builder> getSoldierById(int soldierId) {
     return Optional.fromNullable(soldierIdMap.get(soldierId));
   }
 
   class SimulationStep {
 
     private final GameState entryState;
 
     public SimulationStep() {
       this.entryState = simulationState.build();
     }
 
     private Box getBoxAt(Point p) {
       return entryState.getBoxList().get(p.getX() + entryState.getSize().getWidth() * p.getY());
     }
 
     private void applyOperation(int playerId, Operation operation) throws OperationException {
       if (operation.hasMove() == operation.hasShoot()) {
         throw new OperationException(OperationError.INVALID_FIELD);
       }
 
       if (operation.hasShoot()) {
         if (!operation.getShoot().hasSoldierId()) {
           throw new OperationException(OperationError.INVALID_FIELD);
         }
 
         Optional<Soldier.Builder> soldier = getSoldierById(operation.getShoot().getSoldierId());
         if (!soldier.isPresent()) {
           throw new OperationException(OperationError.INVALID_ID);
         }
         if (soldier.get().getPlayerId() != playerId) {
           throw new OperationException(OperationError.WRONG_PLAYER);
         }
 
         simulationState.addBulletBuilder()
             .setPosition(soldier.get().getPosition())
             .setDirection(operation.getShoot().getDirection());
       }
 
       if (operation.hasMove()) {
         if (!operation.getMove().hasSoldierId()) {
           throw new OperationException(OperationError.INVALID_FIELD);
         }
 
         Optional<Soldier.Builder> soldier = getSoldierById(operation.getMove().getSoldierId());
         if (!soldier.isPresent()) {
           throw new OperationException(OperationError.INVALID_ID);
         }
         if (soldier.get().getPlayerId() != playerId) {
           throw new OperationException(OperationError.WRONG_PLAYER);
         }
 
         Point dest = applyDirection(
             soldier.get().getPosition(),
             operation.getMove().getDirection());
         if (!pointInArea(dest, simulationState.getSize())) {
           throw new OperationException(OperationError.INVALID_MOVEMENT);
         }
 
         Box destBox = getBoxAt(dest);
         if (destBox.getBlocking()) {
           throw new OperationException(OperationError.INVALID_MOVEMENT);
         }
 
         soldier.get().setPosition(dest);
       }
     }
 
     private void runPreStep() {
       simulationState.clearBullet();
     }
 
     private void runPlayerOperation(int playerId, Operation operation) {
       OperationError error = OperationError.NONE;
       Round.Builder round = gameLog.getRoundBuilder(gameLog.getRoundCount()-1);
 
       try {
         if (round.getRoundId() != operation.getRoundId()) {
           error = OperationError.WRONG_ROUND;
         }
         else {
           applyOperation(playerId, operation);
         }
       } catch (OperationException exc) {
         error = exc.getCode();
       }
 
       round.addOperationBuilder()
           .setOperation(operation)
           .setError(error)
           .setPlayerId(playerId);
     }
 
     private void runPostStep() {
       List<Bullet> oldBullets = entryState.getBulletList();
       for (final Bullet bullet : oldBullets) {
 
         final Iterable<Soldier.Builder> soldiersInPath
             = Iterables.filter(simulationState.getSoldierBuilderList(),
                                new Predicate<Soldier.Builder>() {
           @Override
           public boolean apply(Soldier.Builder soldier) {
             return pointInPath(bullet.getPosition(), bullet.getDirection(), soldier.getPosition());
           }
         });
 
         if (Iterables.isEmpty(soldiersInPath)) {
           continue;
         }
 
         final Ordering<SoldierOrBuilder> proximityOrdering
             = Ordering.natural().onResultOf(new Function<SoldierOrBuilder, Integer>() {
           @Override
           public Integer apply(SoldierOrBuilder soldier) {
             return Math.abs(soldier.getPosition().getX() - bullet.getPosition().getX())
                 + Math.abs(soldier.getPosition().getY() - bullet.getPosition().getY());
           }
         });
 
         final List<Soldier.Builder> hitOrderedSoldiers
             = proximityOrdering.sortedCopy(soldiersInPath);
         List<Soldier.Builder> hits = new ArrayList<>();
         for (Soldier.Builder target : hitOrderedSoldiers) {
           if (!target.getPosition().equals(hitOrderedSoldiers.get(0).getPosition())) {
             break;
           }
           hits.add(target);
         }
 
         for (Soldier.Builder hit : hits) {
           if (hit.getPlayerId() == bullet.getOwnerId()) {
             log.info(String.format(
               "Match %d:%d: Soldier %d blocked bullet from %d.",
                   matchId, roundId, hit.getSoldierId(), bullet.getOwnerId()));
           }
           else {
          // TODO: "kill" target
          log.info(String.format(
            "Match %d:%d: Soldier %d killed by %d.",
                matchId, roundId, hit.getSoldierId(), bullet.getOwnerId()));
           }
         }
 
       }
     }
 
   }
 
   @Override
   public void run() {
     gameLog.setStartState(simulationState.build());
     for (MatchClient player : players) {
       try {
         player.client.transmitState(View.newBuilder().setState(gameLog.getStartState()).build());
       } catch (IOException e) {
         log.error(String.format("Match %d: Error transmitting initial state to player %s",
           matchId, player.client.getName()), e);
       }
     }
 
     while (roundId < MAX_ROUNDS) {
       SimulationStep step = new SimulationStep();
       step.runPreStep();
 
       for (MatchClient player : players) {
         gameLog.addRoundBuilder().setRoundId(roundId);
         try {
           for (Operation operation : player.client.receiveOperations()) {
             step.runPlayerOperation(player.player.getId(), operation);
           }
         } catch (IOException e) {
           log.warn(String.format("Match %d:%d: Error receiving turn from player %d",
               matchId, roundId, player.client.getName()), e);
         }
       }
 
       step.runPostStep();
       GameState roundEnd = simulationState.build();
 
       for (MatchClient player : players) {
         try {
           player.client.transmitState(View.newBuilder().setState(roundEnd).build());
         } catch (IOException e) {
           log.warn(String.format("Match %d:%d: Error transmitting result to player %d",
               matchId, roundId, player.client.getName()), e);
         }
       }
     }
   }
 
 }
