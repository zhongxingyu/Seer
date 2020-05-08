 package de.schelklingen2008.reversi.ai.evaluation;
 
 import de.schelklingen2008.reversi.model.GameModel;
 import de.schelklingen2008.reversi.model.Player;
 
 public class EvaluationFunctionFaMa implements EvaluationFunction
 {
 
    private int mReturn = 0;

     public int evaluatePosition(GameModel gameModel, Player player)
     {
        if (gameModel.isFinished() && gameModel.isWinner(player)) return Integer.MAX_VALUE;
        if (gameModel.isFinished() && !gameModel.isWinner(player)) return Integer.MIN_VALUE;
 
         int countPieces = gameModel.countPieces(player);
 
         mReturn += countPieces;
 
         // if(!gameModel.isTurnHolder(player) && !gameModel.hasLegalMoves()){
         // mReturn +=10;
         // }
 
         return mReturn;
     }
 }
