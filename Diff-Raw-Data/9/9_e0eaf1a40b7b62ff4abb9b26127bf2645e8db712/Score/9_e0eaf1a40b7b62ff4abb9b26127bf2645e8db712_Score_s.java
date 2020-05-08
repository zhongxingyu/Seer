 package pl.edu.agh.to1.dice.logic;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 import pl.edu.agh.to1.dice.logic.figures.IFigure;
 import pl.edu.agh.to1.dice.logic.figures.IFigureManager;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Score class agregates the points for every figure
  * @author Michal Partyka
  */
 @Component
 @Scope(BeanDefinition.SCOPE_PROTOTYPE)
 public class Score {
     @Autowired
     private IFigureManager figureManager;
 
     private final Map<IFigure, Integer> points = new HashMap<IFigure, Integer>(16);
    private final List<IFigure> countForBonus = figureManager.countForBonus();
 
     /**
      * Assign points for the figure with givenDiceBox
      * @param figure figure for which points will be assigned
      * @param diceBox diceBox for which points will be counted
      * @return score which was assigned
      */
     public int add(IFigure figure, DiceBox diceBox) {
         if (points.containsKey(figure)) {
             throw new IllegalStateException("Trying assign points for the already filled figure");
         }
         final Integer score = figure.getScore(diceBox);
         points.put(figure, score);
         return score;
     }
 
     /**
      * Count user result with bonus points
      * @return Result
      */
     public Result getResult() {
         int sum=0, bonusPoints=0, bonus=0;
         for (Map.Entry<IFigure, Integer> figureIntegerEntry : points.entrySet()) {
             sum += figureIntegerEntry.getValue();
             if (countForBonus.contains(figureIntegerEntry.getKey())) {
                 bonusPoints += figureIntegerEntry.getValue();
             }
         }
         if (bonusPoints > 63) {
             bonus = 35;
         }
 
         return new Result(sum, bonus);
     }
 
     /**
      * Returning stream with currentStock of the user showing how many points he/she will obtain for provided diceBox
       * @param diceBox diceBox for which calculation would be performed
      * @return String with the description of currentStock
      */
     public String currentStock(DiceBox diceBox) {
         String defualt = "                   |  ";
         final StringBuilder stringBuilder = new StringBuilder();
         for (IFigure figure : figureManager.values()) {
             stringBuilder.append(figure).append(defualt.substring(figure.toString().length()));
             if(!points.containsKey(figure)) {
                 stringBuilder.append(figure.getScore(diceBox)).append("\n");
             } else {
                 stringBuilder.append("-").append("\n");
             }
         }
         return stringBuilder.toString();
     }
 
     /**
      * get currently assigned score for the provided figure
      * @param figure for which score would be counted
      * @return integer - the score
      */
     public int getScore(IFigure figure) {
         Integer ret = points.get(figure);
         return ret == null ? 0 : ret;
     }
 
     /**
      * check whether given figure was already used or not
      * @param figure
      * @return true if used, false otherwise
      */
     public boolean isUsed(IFigure figure) {
         return points.containsKey(figure);
     }
 }
