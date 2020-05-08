 package de.schelklingen2008.reversi.ai.evaluation;
 
 import de.schelklingen2008.reversi.model.GameModel;
 import de.schelklingen2008.reversi.model.Player;
 
 public class EvaluationFunctionFaMa implements EvaluationFunction
 {
 
     public int evaluatePosition(GameModel gameModel, Player player)
     {
        int mReturn = 0;
        if (gameModel.isFinished() && gameModel.isWinner(player)) return 10000;
        if (gameModel.isFinished() && !gameModel.isWinner(player)) return -10000;
 
         int countPieces = gameModel.countPieces(player);
 
         mReturn += countPieces;
 
         // if(!gameModel.isTurnHolder(player) && !gameModel.hasLegalMoves()){
         // mReturn +=10;
         // }
 
         return mReturn;
     }
 }
