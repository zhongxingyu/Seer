 package eveGP;
 
 import static eveGP.internal.Parameter.*;
 import eveGP.internal.Tree;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 /**
  *
  * @author Evan Verworn (4582938) <ev09qz@brocku.ca>
  */
 public abstract class GPproblem implements Runnable{
     
     private int thread;
     public Tree myTree;
     private int ID;
     private ArrayList<String> vars;
     
     public GPproblem () {}
     
     public void setInfo (int thread) {
         setInfo(thread, thread);
     }
     
     public void setInfo (int thread, int id) {
         this.thread = thread;
         this.ID = id;
         this.vars = new ArrayList<String>();
     }
     public final void setVariable(String s, Object o) {
         String S = "Problem."+ID+"."+s;
         vars.add(S);
         set(S, o);
         //System.out.println("SET 'Problem."+ID+"."+s+"' = "+o.toString());
     }
     public final void setVariable(Tree t, String s, Object o) {
         String S = "Problem."+t.ID+"."+s;
         vars.add(S);
         set(S, o);
     }
     
     public final void cleanUpMemory() {
         for(String s : vars)
             remove(s);
         vars.clear();
     }
     
     public final Object getVariable       (String s) { return get ("Problem."+ID+"."+s);}
     public final int    getIntVariable    (String s) { return getI("Problem."+ID+"."+s);}
     public final String getStringVariable (String s) { return getS("Problem."+ID+"."+s);}
     public final float  getFloatVariable  (String s) { return getF("Problem."+ID+"."+s);}
     
     /**
      * Run once before the first generation.
      */
     public void setup () {
         
     }
     
     public final void eval ( Tree tree ) {
         myTree = tree;
     }
     
     public final void run () {
         myTree.setID(ID);
         myTree.score = evaluate(myTree);
         cleanUpMemory();
     }
     
     
     /**
      *	This is your fitness function. It should return a standardized fitness 
      *  score (0 is best). You can get the tree's result with tree.value();
      * @return Standardized Fitness Value (0 is best)
      */
     public abstract float evaluate ( Tree tree );
 	/*	
 	setVariable("X", 0); // Problem specific variable.
 		// set("Problem."+currentThread()+".X", value);
 	*/
     
     /**
      * This is the function responsible for outputting statistics about the best
      * of generations to a file. You can override it to have your own stats sent
      * to the `stats.file`. May I also recommend running the `super.stats()`
      * function when you do override.
      * @param generation number of generation currently being evaluated.
      * @param trees an array of all trees in this generation. Their score is
      *	    accessed via `trees[i].score`.
      */
     public void stats ( int generation, Tree ... trees ) {
         //TODO clean this up
         float average, std, sum = 0;
         for (int i = 0; i < trees.length;i++) {
             sum += trees[i].score;
         }
         average = sum / (float) trees.length;
         sum = 0;
         for (int i = 0; i < trees.length;i++) {
             sum += Math.pow(average - trees[i].score, 2);
         }
         std = (float) Math.sqrt(sum / trees.length);
         
         try {
             if (generation == 0 && 
                     new File(getS("stats")+".txt").exists()) {
                 new File(getS("stats")+".txt").delete();
             }
             
             FileWriter fw = new FileWriter(getS("stats")+".txt", true);
             if (generation == 0)
                 fw.write("Generation,Best,Average,Standard Dev,Function\n");
             fw.write(generation + "," + Evolve.best.score + "," + average + "," + std + "," + Evolve.best.toString()+"\n");
             fw.close();
         } catch (IOException ex) {
             Logger.getLogger(GPproblem.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         for (int i = 0; i < 3; i++) {
             System.out.println(trees[i].score + " >> " + trees[i].toString());
         }
     }
     
     /**
      * Exactly what it sounds like. This is called ONCE a run on the very best
      * tree we saw.
      * @param t 
      */
     public void best (Tree t) {
         System.out.println("BEST:: " + t.score + " >> " + t.toString());
     }
 }
