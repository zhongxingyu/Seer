 package org.drools.planner.examples.ras2012;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import org.drools.planner.config.XmlSolverFactory;
 import org.drools.planner.core.Solver;
 import org.drools.planner.core.score.buildin.hardandsoft.HardAndSoftScore;
 import org.drools.planner.examples.ras2012.util.SolutionIO;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Hello world!
  * 
  */
 public class App {
 
     private static final Logger logger = LoggerFactory.getLogger(App.class);
 
     public static void main(final String[] args) throws FileNotFoundException, IOException {
         // read solution
        final File f = new File("src/main/resources/org/drools/planner/examples/ras2012/RDS2.txt");
         final SolutionIO io = new SolutionIO();
         ProblemSolution sol = io.read(f);
         // and now start solving
         final XmlSolverFactory configurer = new XmlSolverFactory();
         configurer.configure(App.class.getResourceAsStream("/solverConfig.xml"));
         final Solver solver = configurer.buildSolver();
         solver.setPlanningProblem(sol);
         solver.solve();
         // output the solution
         final File targetFolder = new File("data/solutions");
         if (!targetFolder.exists()) {
             targetFolder.mkdirs();
         }
         sol = (ProblemSolution) solver.getBestSolution();
         final HardAndSoftScore score = App.recaculateScore(sol);
         if (score.getHardScore() >= 0) { // don't write score that isn't feasible
             io.writeXML(sol, new File(targetFolder, f.getName() + score.getSoftScore() + ".xml"));
             io.writeTex(sol, new File(targetFolder, f.getName() + score.getSoftScore() + ".tex"));
             sol.visualize(new File(targetFolder, f.getName() + score.getSoftScore() + ".png"));
         } else {
             App.logger.warn("Not writing results because solution wasn't feasible: " + score);
         }
     }
 
     private static HardAndSoftScore recaculateScore(final ProblemSolution solution) {
         final ScoreCalculator calc = new ScoreCalculator();
         calc.resetWorkingSolution(solution);
         return calc.calculateScore();
     }
 }
