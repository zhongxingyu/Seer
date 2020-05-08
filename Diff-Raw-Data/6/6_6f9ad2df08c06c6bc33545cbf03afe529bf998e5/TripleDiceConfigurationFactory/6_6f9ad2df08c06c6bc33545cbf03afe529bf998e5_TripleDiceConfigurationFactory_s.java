 package pl.edu.agh.to1.dice.logic.figures;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * @author Michal Partyka
  */
 public class TripleDiceConfigurationFactory extends AbstractConfigurationFactory {
     private final Integer requireForBonus;
     private final Integer bonus;
 
     public TripleDiceConfigurationFactory() {
         requireForBonus = 65;
         bonus = 35;
     }
 
     public TripleDiceConfigurationFactory(Integer requireForBonus, Integer bonus) {
         this.requireForBonus = requireForBonus;
         this.bonus = bonus;
     }
 
     @Override
     public List<IFigure> createFigures() {
         final List<IFigure> figureDecorators = new ArrayList<IFigure>(figures.size()*2);
         for (IFigure figure : figures) {
             figureDecorators.add(new FigureDecorator(figure, 2));
             figureDecorators.add(new FigureDecorator(figure, 3));
         }
         figures.addAll(figureDecorators);
         return figures;
     }
 
     @Override
     public Collection<Bonus> createBonuses() {
         final List<Bonus> bonuses = new ArrayList<Bonus>(3);
 
         final List<IFigure> doubledFigures = new ArrayList<IFigure>(figures.size());
         final List<IFigure> tripleFigures = new ArrayList<IFigure>(figures.size());
         for (IFigure figure : figures) {
            doubledFigures.add(new FigureDecorator(figure, 2));
            tripleFigures.add(new FigureDecorator(figure, 2));
         }
 
         bonuses.add(new Bonus(figuresCountedForBonus, requireForBonus, bonus, "Bonus"));
         bonuses.add(new Bonus(doubledFigures, 2*requireForBonus, 2*bonus, "Double bonus"));
         bonuses.add(new Bonus(tripleFigures, 3*requireForBonus, 3*bonus, "Triple bonus"));
 
         return bonuses;
     }
 
 }
