 package neuralNets.testcases.circleMoveToXYDirection;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Random;
 
 import neuralNets.lib.components.extended.networkFactories.SigmoidFactory;
 import neuralNets.lib.graphics.entities.Line;
 import neuralNets.lib.graphics.networks.DrawableSigmoidNetwork;
 import neuralNets.lib.learning.GA.AbstractChromoPool;
 import neuralNets.lib.learning.GA.chromosomes.Chromosome;
 import neuralNets.lib.learning.GA.chromosomes.DoublesChromosome;
 import neuralNets.main.Renderer;
 import neuralNets.matlab.Settings;
 import neuralNets.testcases.TestCase;
 
 /**
  * Testcase CircleMoveToXY
  * Goal: The circles must move to a certain X Y coordinate
  * Network: Sigmoid network
  * Learning: Genetic algorithm with doubles (the weights) as genes
  * @author Maarten Slenter
  */
 public class CircleMoveToXYDirection extends TestCase
 {
     /**
      * The circle entity instances
      */
     ArrayList<CircleEntity> circleEntities = new ArrayList<CircleEntity>();
     
     /**
      * The settings
      */
     private Settings settings;
     
     /**
      * The line that represents the x coordinate goal
      */
     private Line goalLineX;
     
     /**
      * The line that represents the y coordinate goal
      */
     private Line goalLineY;
     
     /**
      * The chromosome pool to use for the genetic algorithm
      */
     private AbstractChromoPool chromoPool;
     
     /**
      * Counts the amount of updates that have taken place.
      * After 100 updates the neural networks will be updated with new weights.
      */
     private int updateCount = 0;
     
     {
         networkFactory = new SigmoidFactory();
         networkFactory.setTopology(2, 2, 1, 6, 1, 1, 10);
     }
     
     public CircleMoveToXYDirection(Settings settings)
     {
         this.settings = settings;
         goalLineX = new Line(settings.getInt("circleMoveToXYDirection.goalX"), 0, settings.getInt("circleMoveToXYDirection.goalX"), settings.getInt("windowHeight"), Color.green);
         goalLineY = new Line(0, settings.getInt("circleMoveToXYDirection.goalY"), settings.getInt("windowHeight"), settings.getInt("circleMoveToXYDirection.goalY"), Color.green);
         chromoPool = AbstractChromoPool.createChromoPool(settings.getString("GAVersion"));
         chromoPool.initialize(settings.getDouble("circleMoveToXYDirection.crossOverRate"), settings.getDouble("circleMoveToXYDirection.mutationRate"), settings);
     }
 
     /**
      * Initializes the test case, instantiating all circle entities.
      * Does not add any visualization, use for batch testing.
      */
     @Override
     public void initialize()
     {
         for(int i = 1; i <= settings.getInt("circleMoveToXYDirection.popSize"); i++)
         {
             CircleEntity circleEntity = new CircleEntity((int) (400), (int) (400), settings.getInt("circleMoveToXYDirection.goalX"), settings.getInt("circleMoveToXYDirection.goalX"), 5, networkFactory.create());
             circleEntities.add(circleEntity);
         }
     }
 
     /**
      * Initializes the test case, instantiating all circle entities and 
      * @param renderer The renderer instance
      */
     @Override
     public void initialize(Renderer renderer)
     {
         initialize();
         
         renderer.addDrawable(goalLineX);
         renderer.addDrawable(goalLineY);
         
         for(CircleEntity circleEntity : circleEntities)
         {
             renderer.addDrawable(circleEntity.getCircle());
         }
         
         Random random = new Random();
         
         for(int i = 0; i <= settings.getInt("circleMoveToXYDirection.visualizeAmount") - 1; i++)
         {
             int index = (int) random.nextInt(settings.getInt("circleMoveToXYDirection.visualizeAmount"));
             
             DrawableSigmoidNetwork.drawNetwork(circleEntities.get(index).getNetwork(), renderer, 300 * (i % 5), 200 * (int) (i / 5), 2, Color.black);
             circleEntities.get(index).getCircle().setColor(Color.red);
             circleEntities.get(index).getCircle().setFill(true);
         }
     }
 
     @Override
     public void reinitialize()
     {
         circleEntities.clear();
         updateCount = 0;
         generation = 0;
         chromoPool.reset();
         initialize();
     }
     
     /**
      * Reinitializes the test case
      * @param renderer The renderer instance
      */
     @Override 
     public void reinitialize(Renderer renderer)
     {
         renderer.clearDrawables();
        reinitialize();
         initialize(renderer);
     }
 
     /**
      * Updates the testcase.
      * Loops through all circle entities and updates them
      */
     @Override
     public void update()
     {        
         updateCount++;
         
         for(CircleEntity circleEntity : circleEntities)
         {
             circleEntity.update();
         }
         
         if(updateCount == 100)
         {            
             generation++;
             
             Chromosome workingChromo = null;
             for(CircleEntity circleEntity : circleEntities)
             {
                 double fitness = 1 / (Math.abs(settings.getInt("circleMoveToXYDirection.goalX") - circleEntity.getX()) + Math.abs(settings.getInt("circleMoveToXYDirection.goalY") - circleEntity.getY()));
                 Chromosome chromosome = new DoublesChromosome(fitness, circleEntity.getNetwork().getAdjustables());
                 if(fitness == Double.POSITIVE_INFINITY)
                 {
                     workingChromo = chromosome.clone();
                 }
                 chromoPool.addChromosome(chromosome);
                 circleEntity.reset();
             }
             
             ArrayList<Chromosome> newChromosomes = chromoPool.update();
             
             if((workingChromo != null && chromoPool.getAvgFitness() != Double.POSITIVE_INFINITY) || workingChromo == null && chromoPool.getAvgFitness() == Double.POSITIVE_INFINITY)
             {
                 throw new RuntimeException("Fitnesses are incorrect");
             }
             
             if(workingChromo != null)
             {
                 for(int i = 0; i <= newChromosomes.size() - 1; i++)
                 {
                     circleEntities.get(i).getNetwork().setAdjustables(((DoublesChromosome) workingChromo).getDoubles());
                 }
             }
             else
             {
                 for(int i = 0; i <= newChromosomes.size() - 1; i++)
                 {
                     circleEntities.get(i).getNetwork().setAdjustables(((DoublesChromosome) newChromosomes.get(i)).getDoubles());
                 }
             }
             
             updateCount = 0;
         }
     }
     
     /**
      * Proxy method for chromoPool.getAvgFitness
      * @return The average fitness of the chromosome pool
      */
     @Override
     public double getAvgFitness()
     {
         return chromoPool.getAvgFitness();
     }
     
     @Override
     public CircleMoveToXYDirection clone()
     {
         CircleMoveToXYDirection clone = new CircleMoveToXYDirection(settings);
        clone.initialize();
         return clone;
     }
 }
