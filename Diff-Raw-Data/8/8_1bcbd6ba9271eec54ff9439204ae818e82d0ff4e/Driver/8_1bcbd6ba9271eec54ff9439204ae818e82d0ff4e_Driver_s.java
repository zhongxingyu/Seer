 package game.ai;
 
 import game.Command;
 import game.Ending;
 import game.State;
 import game.fitness.ScoreFitness;
 import game.selector.SimpleSelector;
 import game.stepper.MultiStepper;
 import interrupt.ExitHandler;
 
 import java.io.File;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.PriorityQueue;
 import java.util.Scanner;
 import java.util.Set;
 
 /**
  * The main driver of the artifical intelligence.
  * 
  * @author Tillmann Rendel
  * @author Thomas Horstmeyer
  */
 
 public class Driver {
 
   public static final int PRIORITY_QUEUE_CAPACITY = 5000;
 
   // state choosing: compute one integer score, store in state, use priority
   // queue.
 
   // TODO is contains on PriorityQueue fast enough?
   public MultiStepper stepper = new MultiStepper();
   public Comparator<State> comparator = new FitnessComparator();
   public PriorityQueue<State> liveStates = new PriorityQueue<State>(PRIORITY_QUEUE_CAPACITY, comparator);
   public Set<State> deadStates = new HashSet<State>();
   public Fitness fitness;
   public Selector strategySelector;
 
   public State bestState;
 
   public Driver(Selector strategySelector, Fitness fitness) {
     this.strategySelector = strategySelector;
     this.fitness = fitness;
   }
 
   public void solve(State initial) {
     strategySelector.prepareState(initial);
     liveStates.add(initial);
     initial.fitness = fitness.evaluate(initial);
 
     bestState = initial;
 
     // TODO when to stop?
 
     // TODO set scorerScore
 
     while (!liveStates.isEmpty()) {
       State state = liveStates.peek();
 
       System.out.printf("%s\n\n", state);
 
       Strategy strategy = strategySelector.selectStrategy(state);
 
       if (strategy == null) {
         killState(state);
       } else {
         // apply strategy to create new state
         List<Command> commands = strategy.apply(state);
 
         if (commands != null) {
           assert (!commands.isEmpty());
           State newState = computeNextState(state, commands, strategy);
           if (!deadStates.contains(newState) && !liveStates.contains(newState)) {
             if (newState.score > bestState.score) {
               bestState = newState;
             }
 
             if (newState.ending == Ending.None && newState.steps < newState.board.width * newState.board.height) {
               liveStates.add(newState);
               newState.fitness = fitness.evaluate(newState);
               strategySelector.prepareState(newState);
             }
           }
         }
       }
     }
 
   }
 
   /**
    * Print known best solution to System.out
    */
   public void printSolution() {
     // TODO: make threadsafe, might be called from exit handler
     // while already outputting... (use StringBuilder perhaps?!)
    Command[] commands = bestState.solution.allCommands();
    for (Command command : commands) {
      System.out.append(command.shortName());
     }
     System.out.println("A");
     System.out.flush();
   }
 
   // kill states that have no strategies left
   private void killState(State state) {
     liveStates.remove(state);
     deadStates.add(state);
   }
 
   private State computeNextState(State state, List<Command> commands, Strategy strategy) {
     State result = stepper.multistep(state, commands);
     result.solution = new Solution(state.solution, commands.toArray(new Command[commands.size()]), strategy);
     return result;
   }
 
   // TODO add exception handling?
   public static void main(String[] args) throws Exception {
     Selector selector = new SimpleSelector();
     Fitness scorer = new ScoreFitness();
     Driver driver = new Driver(selector, scorer);
 
     // from
     // http://weblogs.java.net/blog/pat/archive/2004/10/stupid_scanner_1.html
     String text;
     if (args.length > 0) {
       text = new Scanner(new File(args[0])).useDelimiter("\\A").next();
     } else {
       text = new Scanner(System.in).useDelimiter("\\A").next();
     }
     text = text.replace("\r", "");
     State state = State.parse(text);
 
     ExitHandler.register(driver);
     driver.solve(state);
     driver.printSolution();
   }
 }
