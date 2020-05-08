 package tron.ai.michaellossos.interceptor;
 
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import tron.ai.michaellossos.CompassDirection;
 import tron.ai.michaellossos.Coord;
 import tron.ai.michaellossos.IBotAI;
 import tron.ai.michaellossos.astar.AStarSearch;
 import tron.ai.michaellossos.astar.Path;
 import tron.ai.michaellossos.state.IBotState;
 import tron.ai.michaellossos.state.IGameState;
 import tron.ai.michaellossos.state.IStepState;
 
 public class InterceptorAI implements IBotAI {
     private Logger getLogger() {
         return Logger.getLogger(getClass().getName());
     }
 
     private CompassDirection chooseAlternateDirection(Set<CompassDirection> validDirections) {
         if (validDirections == null || validDirections.isEmpty()) {
             getLogger().log(Level.WARNING, "No valid directions. No available moves.");
             return CompassDirection.NORTH;
         }
         return validDirections.iterator().next();
     }
 
     private CompassDirection validateDirection(CompassDirection newDirection, String errorLogMessage,
         Set<CompassDirection> validDirections, IStepState stepState) {
         CompassDirection returnDirection = newDirection;
         if (!validDirections.contains(returnDirection)) {
             getLogger().log(
                 Level.WARNING,
                 "Chose an invalid direction: " + newDirection + " from: " + stepState.getOurCoord() + " direction: "
                     + stepState.getOurDirection() + "  Context: " + errorLogMessage);
             // Fix it by taking a valid direction 
             // TODO This is bad
             returnDirection = chooseAlternateDirection(validDirections);
         }
         return returnDirection;
     }
 
     @Override
     public CompassDirection getNextDirection(IGameState gameState, IBotState botState, IStepState stepState) {
         if (botState.getDestination() == null) {
             // TODO Determine initial destination
             botState.setDestination(new Coord(0, 0));
         }
 
         // Determine valid moves for later validation.
         ValidNextMoves validNextMoves = new ValidNextMoves(gameState);
         Map<Coord, CompassDirection> validMoves = validNextMoves.getValidNextMoves(stepState.getOurCoord(), stepState
             .getOurDirection());
         Set<CompassDirection> validDirections = new HashSet<CompassDirection>(validMoves.values());
 
         // Search for a path to destination.
         AStarSearch astar = new AStarSearch();
         Path path = astar.calculateShortestPath(gameState, stepState.getOurDirection(), stepState.getOurCoord(),
             botState.getDestination(), gameState.getStepTimeoutMillis());
         CompassDirection returnDirection = null;
        if (path != null && path.getPreviousSteps() != null && path.getPreviousSteps().size() > 1) {
            // The first element at 0 is the current position. Take the element at 1.
            returnDirection = path.getPreviousSteps().get(1).getDirection();
 
             // Verify the move is valid (should be)
             returnDirection = validateDirection(returnDirection, "Path search returned invalid direction.",
                 validDirections, stepState);
         }
 
         if (returnDirection == null) {
            returnDirection = validateDirection(returnDirection, "Path search found no viable direction.",
                 validDirections, stepState);
         }
         return returnDirection;
     }
 
 }
