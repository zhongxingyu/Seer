 public class Game {
     private Input          input;
     private Output         output;
     private DecisionEngine engine;
     private ResultDatabase history;
     private int            roundsRemaining;
 
     public Game() {
         this.input   = new TextInput();
         this.output  = new TextOutput();
         this.engine  = new RandomEngine();
         this.history = new ResultDatabase();
        this.roundsRemaining = 1;
     }
     
     public void setRounds(int rounds) {
         this.roundsRemaining = rounds;
     }
 
     public static void main(String[] args) {
         Game g = new Game();
        if (args.length > 0) {
            try {
                g.setRounds(Integer.parseInt(args[0]));
            }
            catch (NumberFormatException ex) {
                System.out.println("Invalid argument. Must be a positive integer.");
                System.exit(1);
            }
         }
         g.output.displayStartup();
         
         Choice userChoice, compChoice;
         Result result;
         while (g.roundsRemaining > 0)
         {
            g.output.displayPrompt();
             userChoice = g.input.getUserChoice();
            if (userChoice == null)
                System.out.println("Invalid command.");
            else if (userChoice.getValue() < 4) {
                 --g.roundsRemaining;
                 compChoice = g.engine.getComputerChoice();
                 result     = new Result(userChoice, compChoice);
 
                 g.history.add(result);
                 g.output.displayResult(result);
             }
             else if (userChoice.equals(Choice.HELP))
                 g.output.displayHelp();
             else if (userChoice.equals(Choice.SCORE))
                 g.output.displayScore();
             else if (userChoice.equals(Choice.EXIT))
                break;
         }
     }
 }
