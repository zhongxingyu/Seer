 package pacman;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.EnumMap;
 import java.util.LinkedList;
 import java.util.Random;
 
 import pacman.controllers.Controller;
 import pacman.controllers.HumanController;
 import pacman.controllers.KeyBoardInput;
 import pacman.controllers.examples.AggressiveGhosts;
 import pacman.controllers.examples.Legacy;
 import pacman.controllers.examples.Legacy2TheReckoning;
 import pacman.controllers.examples.NearestPillPacMan;
 import pacman.controllers.examples.NearestPillPacManVS;
 import pacman.controllers.examples.RandomGhosts;
 import pacman.controllers.examples.RandomNonRevPacMan;
 import pacman.controllers.examples.RandomPacMan;
 import pacman.controllers.examples.StarterGhosts;
 import pacman.controllers.examples.StarterPacMan;
 import pacman.entries.ghosts.MyGhosts;
 import pacman.game.Game;
 import pacman.game.GameView;
 import pacman.game.Constants.GHOST;
 import static pacman.game.Constants.*;
 import edu.ucsc.gameAI.*;
 import edu.ucsc.gameAI.conditions.*;
 import edu.ucsc.gameAI.decisionTrees.binary.BinaryDecision;
 import edu.ucsc.gameAI.fsm.*;
 
 /**
  * This class may be used to execute the game in timed or un-timed modes, with or without
  * visuals. Competitors should implement their controllers in game.entries.ghosts and 
  * game.entries.pacman respectively. The skeleton classes are already provided. The package
  * structure should not be changed (although you may create sub-packages in these packages).
  */
 @SuppressWarnings("unused")
 public class Evaluator
 {    
     
     // could be a function run every frame of runExperiment, accepting game as a parameter
     public static void runUnitTests(Game game, Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController)
     {
         // test 1: is blinky edible
         IsEdible edible = new IsEdible(GHOST.BLINKY);
         // OR if (edible.test(game) == game.isGhostEdible(GHOST.BLINKY))
         if (edible.test(game) == false)
         {
             //System.out.println("Test 1 passed");
         }
         else
         {
             //System.out.println("Test 1 failed");
         }
     
         
         // Game State Condition Tests
         if (new MazeIndex(game.getMazeIndex()).test(game))
             ;//System.out.println("MazeIndex passed");
         else
             System.out.println("MazeIndex failed");
         if (new LevelCount(game.getCurrentLevel()).test(game))
             ;//System.out.println("LevelCount passed");
         else
             System.out.println("LevelCount failed");
         if (new CurrentLevelTime(game.getCurrentLevelTime()-1,game.getCurrentLevelTime()+1).test(game))
             ;//System.out.println("CurrentLevelTime passed");
         else
             System.out.println("CurrentLevelTime failed");
         if (!new TotalTime(game.getTotalTime()-1,game.getTotalTime()+1).test(game))
             System.out.println("TotalTime failed");
         if (!new Score(game.getScore()-1,game.getScore()+1).test(game))
             System.out.println("Score failed");        
         if (!new GhostEatScore(game.getGhostCurrentEdibleScore()-1,game.getGhostCurrentEdibleScore()+1).test(game))
             System.out.println("GhostEatScore failed");
         if (!new TimeOfLastGlobalReversal(game.getTimeOfLastGlobalReversal()-1,game.getTimeOfLastGlobalReversal()+1).test(game))
             System.out.println("TimeOfLastGlobalReversal failed");
         if (!(new PacmanWasEaten().test(game) == game.wasPacManEaten()))
             System.out.println("PacmanWasEaten failed");
         if (!(new PillWasEaten().test(game) == game.wasPillEaten()))
             System.out.println("PillWasEaten failed");
         if (!(new PowerPillWasEaten().test(game) == game.wasPowerPillEaten()))
             System.out.println("PowerPillWasEaten failed");
         for (int pill : game.getPillIndices())
             if (!(new IsPillStillAvailable(pill).test(game) == game.isPillStillAvailable(pill)))
                 System.out.println("IsPillStillAvailable failed");
         for (int pill : game.getPillIndices())
             if (!(new IsPowerPillStillAvailable(pill).test(game) == game.isPowerPillStillAvailable(pill)))
                 System.out.println("IsPowerPillStillAvailable failed");
         for (GHOST ghost : GHOST.values())
             if (!(new GhostEaten(ghost).test(game) == game.isGhostEdible(ghost)))
                 System.out.println("GhostEaten failed");
         
         // Ghost State Conditions
         for (GHOST ghost : GHOST.values())
         {
             if (!(new CurrentGhostNodeIndex(ghost, game.getGhostCurrentNodeIndex(ghost))).test(game))
                 System.out.println("CurrentGhostNodeIndex failed");
             if (!(new EdibleTime(ghost, game.getGhostEdibleTime(ghost)-1,game.getGhostEdibleTime(ghost)+1)).test(game))
                 System.out.println("EdibleTime failed");
             if (!(new LairTime(ghost, game.getGhostLairTime(ghost)-1,game.getGhostLairTime(ghost)+1)).test(game))
                 System.out.println("GhostEaten failed");
             if (!(new GhostLastMove(ghost, game.getGhostLastMoveMade(ghost))).test(game))
                 System.out.println("GhostLastMove failed");
         }
         
         // Ms Pac ManState Conditions
         if (!(new CurrentPacmanNodeIndex(game.getPacmanCurrentNodeIndex())).test(game))
             System.out.println("CurrentPacmanNodeIndex failed");
        if (!(new NumberOfLivesRemaining(
                game.getPacmanNumberOfLivesRemaining(),
                game.getPacmanNumberOfLivesRemaining() ).test(game)));
             System.out.println("NumberOfLivesRemaining failed");
         if (!(new PacmanLastMove(game.getPacmanLastMoveMade()).test(game)))
             System.out.println("PacmanLastMove failed");
     
         // Inference Conditions
         int px;
         int py;
         for (int pill : game.getActivePillsIndices())
         {
             px = game.getNodeXCood(pill);
             py = game.getNodeYCood(pill);
             if (!(new PillInRegion(px-1,py-1,px+1,py+1).test(game)))
                 System.out.println("PillInRegion failed");
         }
         for (int ppill : game.getActivePowerPillsIndices())
         {
             px = game.getNodeXCood(ppill);
             py = game.getNodeYCood(ppill);
             if (!(new PowerPillInRegion(px-1,py-1,px+1,py+1).test(game)))
                 System.out.println("PowerPillInRegion failed");
         }
         for (GHOST ghost : GHOST.values())
         {
             int ig = game.getGhostCurrentNodeIndex(ghost);
             int gx = game.getNodeXCood(ig);
             int gy = game.getNodeYCood(ig);
             if (!(new GhostInRegion(gx-1,gy-1,gx+1,gy+1).test(game)))
                 System.out.println("GhostInRegion failed");
         }
         int ipac = game.getPacmanCurrentNodeIndex();
         int pacx = game.getNodeXCood(ipac);
         int pacy = game.getNodeYCood(ipac);
         if (!(new PacmanInRegion(pacx-1,pacy-1,pacx+1,pacy+1).test(game)))
             System.out.println("PacmanLastMove failed");
         
         // Decision Tree
         BinaryDecision root = new BinaryDecision();
         ipac = game.getPacmanCurrentNodeIndex();
         pacx = game.getNodeXCood(ipac);
         pacy = game.getNodeYCood(ipac);
         root.setCondition(new PacmanInRegion(pacx-1,pacy-1,pacx+1,pacy+1));
         root.setTrueBranch(new GoUpAction());
         root.setFalseBranch(new GoDownAction());
         if (root.makeDecision(game).getClass() != GoUpAction.class)
             System.out.println("Decision Tree failed");
             
         // FSM (up when pacman in the bottom half of screen, down otherwise)
         int ix1 = 0;
         int ix2 = 110;
         int iy1 = 0;
         int iy2 = 60;
         int iy3 = 120;
         State stateRun = new State();
         stateRun.setAction(new GoDownAction());
         State stateChase = new State();
         stateChase.setAction(new GoUpAction());
         Transition transScared = new Transition();
         transScared.setCondition(new PacmanInRegion(ix1,iy1,ix2,iy2)); // top half of the screen
         transScared.setTargetState(stateRun);
         LinkedList<ITransition> listtscared = new LinkedList<ITransition>();
         listtscared.add(transScared);
         Transition transCool = new Transition();
         transCool.setCondition(new PacmanInRegion(ix1,iy2+1,ix2,iy3)); // bottom of screen (screen is (4,4) to (104,116))
         transCool.setTargetState(stateChase);
         LinkedList<ITransition> listtcool = new LinkedList<ITransition>();
         listtcool.add(transCool);
         stateRun.setTransitions(listtcool);
         stateChase.setTransitions(listtscared);
         StateMachine fsm = new StateMachine();
         fsm.setCurrentState(stateChase);
         LinkedList<IAction> actions = (LinkedList<IAction>) fsm.update(game);
         // test result
         int ip = game.getPacmanCurrentNodeIndex();
         pacx = game.getNodeXCood(ip);
         pacy = game.getNodeYCood(ip);
         if (ix1 <= pacx && ix2 >= pacx &&
             iy1 <= pacy && iy2 >= pacy    )
         {
             if (actions.get(0).getClass() != GoDownAction.class)
                 System.out.println("FSM action result failed");
             if (fsm.getCurrentState() != stateRun)
                 System.out.println("FSM state failed");
         }
             
         
         // actions
         if (!(new GoUpAction().getClass() == GoUpAction.class))
             System.out.println("GoUpAction failed");
         if (!(new GoLeftAction().getClass() == GoLeftAction.class))
             System.out.println("GoLeftAction failed");
         if (!(new GoRightAction().getClass() == GoRightAction.class))
             System.out.println("GoRightAction failed");
         if (!(new GoDownAction().getClass() == GoDownAction.class))
             System.out.println("GoDownAction failed");
         
     }
     
 }
