 import java.util.*;
 import java.util.concurrent.*;
 
 public class Experimentalist implements Runnable
 {
     private GameWindow gameWindow;
     ArrayList<GameController> games = new ArrayList<GameController>();
     private ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
     ArrayList<Callable<Integer>> tasks = new ArrayList<Callable<Integer>>();
 
     public void attach(GameWindow newGameWindow)
     {
         gameWindow = newGameWindow;
     }
 
     private AI setUpGame(Options options)
     {
         GameController game = new GameController(options);
         games.add(game);
         AI ai = new AI(options);
         ai.attach(game);
 
         return ai;
     }
 
     private void playGame(Options options)
     {
         AI ai = setUpGame(options);
         gameWindow.attach(games.get(games.size() - 1));
         ai.call();
     }
 
     private void addGameToPlay(Options options)
     {
         tasks.add(setUpGame(options));
     }
 
     private double analyzeData(ArrayList<Integer> results)
     {
         int trialSum = 0;
         for(int result : results)
         {
             trialSum += result;
         }
 
         double average = trialSum * 1.0 / results.size();
         System.out.println("Average score was: " + average);
 
         return average;
     }
 
     public double runNormalTrials(Options options)
     {
         ArrayList<Integer> trialResults = new ArrayList<Integer>();
         for(int i = 0; i < Constants.AI_TRIALS; i++)
         {
             addGameToPlay(options.clone());
         }
 
         if(gameWindow != null)
         {
             gameWindow.attach(games.get(0)); // this will get boring after the first game
         }
 
         try
         {
            List<Future<Integer>> futures = es.invokeAll(tasks);
            for(Future<Integer> future : futures)
             {
                trialResults.add(future.get());
             }
         }
         catch (InterruptedException ie)
         {
             System.err.println("Could not wait for threads to end!");
             ie.printStackTrace();
         }
         catch (ExecutionException ee)
         {
             System.err.println("Error playing game!");
             ee.printStackTrace();
         }
         catch (Exception e)
         {
             System.err.println("Error completing a game!");
             e.printStackTrace();
         }
 
         return analyzeData(trialResults);
     }
 
     private void runExactTrials()
     {
         Options optimalPlayOptions = new Options();
         optimalPlayOptions.boardHeightWeight = 0;
         optimalPlayOptions.boardOverhangsWeight = 0;
         int maxMoves = Constants.DEFAULT_SHAPES_QUEUE_SIZE + 1;
         optimalPlayOptions.maxMoves = maxMoves;
         optimalPlayOptions.exactSearch = true;
         playGame(optimalPlayOptions);
         System.out.println("Optimal play gave a score of " + games.get(0).getScore() + ":");
         games.get(0).getBoard().output();
 
         for(int i = 0; i < Constants.DEFAULT_SHAPES_QUEUE_SIZE; i++)
         {
             Options newGameOptions = new Options();
             newGameOptions.shapesQueueSize = i;
             newGameOptions.maxMoves = maxMoves;
             playGame(newGameOptions);
         }
 
         for(int i = 1; i < games.size(); i++)
         {
             System.out.println("For a game with a queue size of " + games.get(i).getShapesQueueSize()
                                 + ", the AI obtained a score of " + games.get(i).getScore() + ":");
             games.get(i).getBoard().output();
         }
     }
 
     @Override
     public void run()
     {
         if(Constants.exact)
         {
             runExactTrials();
         }
         else
         {
             runNormalTrials(new Options());
         }
     }
 }
