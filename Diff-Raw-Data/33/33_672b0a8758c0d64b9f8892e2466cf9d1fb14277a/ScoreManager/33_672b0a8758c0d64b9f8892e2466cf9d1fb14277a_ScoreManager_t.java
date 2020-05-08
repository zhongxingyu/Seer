 package ca.couchware.wezzle2d;
 
 import ca.couchware.wezzle2d.util.Util;
 import ca.couchware.wezzle2d.tile.TileEntity;
 import ca.couchware.wezzle2d.tile.Multiply2xTileEntity;
 import ca.couchware.wezzle2d.tile.BombTileEntity;
 import java.util.Iterator;
 import java.util.Set;
 
 /**
  *
  * @author Kevin
  */
  public class ScoreManager
  {
     /**
      * A line.
      */
     final public static int TYPE_LINE = 0;
     
     /**
      * A bomb.
      */
     final public static int TYPE_BOMB = 1;
      
     /**
      * The amount of point per tile in a line.
      */
     final private static int POINTS_PER_LINE_TILE = 50;
 
     /** 
      * The amount of point per tile in a piece.
      */
     final private static int POINTS_PER_PIECE_TILE = 10;
 
     /**
      * The board manager.
      */
     final private BoardManager boardMan;
     
     /**
     * The Property Manager.
     */
    final private PropertyManager propertyMan;
    
    /**
      * The score this level.
      */
     private int levelScore;
 
     /** 
      * The total score. 
      */
     private int totalScore;
 
     /**
      * The target score for this level.
      */
     private int targetLevelScore;
 
     /**
      * The target total score.
      */
     private int targetTotalScore;
 
     /**
      * The high score.
      */
     private int highScore;
 
     /**
      * The constructor.
      * @param properties A property manager to load properties from.
      */
     public ScoreManager(BoardManager boardMan, PropertyManager propertyMan)
     {
         // Store reference to board manager.
         this.boardMan = boardMan;
        this.propertyMan = propertyMan;
         
         // Initialize the scores.
         this.totalScore = 0;
         this.levelScore = 0;
         this.highScore = propertyMan.getIntegerProperty(PropertyManager.HIGH_SCORE);
     }
 
     /**
      * Requires documentation.
      * @param set
      * @param lineType
      * @param cascadeCount
      */
     public void calculateLineScore(Set set, int lineType, int cascadeCount)
     {
         // Initialize tile counts.
         int numNormal = 0;
         int numBomb = 0;
         int numMult4x = 0;
         int numMult3x = 0;
         int numMult2x = 0;		
 		
         // Cycle through the set counting the pieces.
         for (Iterator it = set.iterator(); it.hasNext();)
         {
             // Grab the tile.
             TileEntity t = boardMan.getTile((Integer) it.next());
             
             // See what kinda tile it is.
             if (t != null)
             {
                 if (t.getClass() == TileEntity.class)
                 {
                     numNormal++;
                 }
                 else if (t.getClass() == Multiply2xTileEntity.class)                    
                 {
                     numMult2x++;
                 }
                 else if (t.getClass() == BombTileEntity.class)
                 {
                     numBomb++;
                 }
             } // end if
         } // end for
 
         // Change this when we add bombs and multipliers.
         int deltaScore = (int) (calculateLineTilePoints(numNormal + numBomb + numMult2x + numMult3x + numMult4x, lineType)
                 *  Math.pow(1.5, numBomb)
                 *  Math.pow(2, numMult2x)
                 *  Math.pow(3, numMult3x)
                 *  Math.pow(4, numMult4x)
                 * cascadeCount);
 
         Util.handleMessage("cascadeCount == " + cascadeCount, Thread.currentThread());
         
         // Update progress and text.
         updateScore(deltaScore);
     }
 
     /**
      * Requires documenation.
      * @param numTotal
      * @param lineType
      * @return
      */
     private int calculateLineTilePoints(int numTotal, int lineType)
     {
         // If we have a minimal line, it's just 4 times the points/tile.
         if (numTotal <= 4  || lineType == TYPE_BOMB)
             return numTotal * POINTS_PER_LINE_TILE;
 
         // If we have more, 4 times the points/tile + 100 + 150 + 200 + ...
         int score = 4 * POINTS_PER_LINE_TILE;
 
         for (int i = 0; i < numTotal - 4; i++)
             score += (i + 2) * POINTS_PER_LINE_TILE;
 
         return score;
     }
 
     /**
      * Requires documentation.
      * 
      * @param set A set of tiles.
      */
     public void calculatePieceScore(Set indexSet)
     {
         // Sanity check.
         assert(indexSet != null);
         
         // Initilize deltaScore variable.
         int deltaScore = 0;
 
         // Cycle through the set counting the pieces.
         for (Iterator it = indexSet.iterator(); it.hasNext(); )
         {
             if (it.next() != null)
             {
                 deltaScore += POINTS_PER_PIECE_TILE;
             }
         }
             
         // Update the score if there is a score update.
         if (deltaScore != 0)
         {			
             // Update scores.
             updateScore(deltaScore);
         }
         else
         {
             // Otherwise do nothing.
         }
         
     }
 
     /**
      * Requires documentation.
      * @return
      */
     public int getHighScore() 
     {
         return highScore;
     }
 
     /**
      * Requires documentation.
      * @param highScore
      */
     public void setHighScore(int highScore) 
     {	
         this.highScore = highScore;
        this.propertyMan.setProperty(PropertyManager.HIGH_SCORE, String.valueOf(highScore));
     }
 
     /**
      * A method to reset the high score.
      */
     public void resetHighScore()
     {
         try
         {
            this.propertyMan.setProperty(PropertyManager.HIGH_SCORE, "0");
             this.setHighScore(0);
         }
         catch(Exception e)
         {
                 e.printStackTrace();
         }
     }
 
     /**
      * @return The levelScore.
      */
     public int getLevelScore()
     {
         return levelScore;
     }
 
     /**
      * @param levelScore The levelScore to set.
      */
     public void setLevelScore(int levelScore)
     {
         this.levelScore = levelScore;		
     }
 
     public int getTotalScore()
     {
         return this.totalScore;
     }
 
     public void setTotalScore(int totalScore)
     {
         this.totalScore = totalScore;
     }
 
     /**
      * @return The targetLevelScore.
      */
     public int getTargetLevelScore()
     {
         return targetLevelScore;
     }
 
     /**
      * @param targetLevelScore The targetLevelScore to set.
      */
     public void setTargetLevelScore(int targetLevelScore)
     {		
         this.targetLevelScore = targetLevelScore;
         this.targetTotalScore += targetLevelScore;
     }
 
     /**
      * @return The targetTotalScore.
      */
     public int getTargetTotalScore()
     {
         return targetTotalScore;
     }
 
     /**
      * @param targetTotalScore The targetTotalScore to set.
      */
     public void setTargetTotalScore(int targetTotalScore)
     {
         this.targetTotalScore = targetTotalScore;
     }
 
    
 
     
     /**
      * A method to update all the scores.
      * @param deltaScore A change in score.
      */
     private void updateScore(int deltaScore)	
     {	
             // Update the level score.
             levelScore += deltaScore;
 
             // Update the target score.
             totalScore += deltaScore;
 
             if(totalScore > highScore)
                     setHighScore(totalScore);
     }
 }
