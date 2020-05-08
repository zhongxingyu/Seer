 package logic;
 
 import data.Graph;
 import data.Node;
 import data.tree.Trie;
 import data.tree.TrieList;
 import data.tree.TrieNode;
 import data.tree.TrieNodeList;
 import views.TrieVisualizer;
 
 import java.util.ArrayList;
 
public class AcoHBF {
     private Trie Tib;
     private Trie Trb;
     private Trie Tbs;
 
     private Graph graph;
     private int ant_totals;
     private ArrayList<Ant> threads;
     private TrieList trees;
     private double phero_max;
     private double phero_min;
     private TrieVisualizer trieVisualizer;
     private static double p = 0.1; /* Evaporation rate between 0 and 1*/
 
     public AcoHBF() {
         this.phero_max = 1;
         this.phero_min = 0.001;
         trieVisualizer = new TrieVisualizer(-1000,0);
         this.trees = new TrieList();
         threads = new ArrayList<Ant>();
     }
 
     public Trie run(Graph graph, int ants) {
         this.ant_totals = ants;
         this.graph = graph;
 
         for (int i = 0; i < 50; i++) {
             /*Construct trees */
             run_ants();
             if (Tib == null || Tib.cost() > find_best_tree().cost())
                 Tib = find_best_tree();
 
             Update();
 
             ApplyPheromoneUpdate();
 
             evaporate_pheromones();
             
 
             
         }
 
         try {
             Thread.sleep(50000);
 
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
 
         return Tbs;
     }
 
     private double gamma(Trie t,Node n1, Node n2){
      
     }
     private void  ApplyPheromoneUpdate(){
 
     }
 
     private void Update(){
         if( Tib.cost() < Trb.cost() && Tib.cost() < Tbs.cost()){
             Trb = Tib;
             Tbs = Tib;
         }
     }
 
 /* performs an evaporation on each possible edge*/
     private void evaporate_pheromones() {
         for (Node n1 : graph.getNodes()) {
             for (Node n2 : graph.getNodes()) {
                 graph.update_pheromone_value(
                         n1.getId(),
                         n2.getId(),
                         calculate_pheromone_update_for_evap(n1, n2)
                 );
             }
         }
     }
 
 /* calculates evaporation for the given edge according to this formula: Tij(t+1) = (1-p) * Tij(t)*/
     private double calculate_pheromone_update_for_evap(Node n1, Node n2) {
         double tmp = graph.get_pheromone_for_edge(n1.getId(), n2.getId()) * (1-p);
         return check_for_phero_limits(tmp);
     }
 
     private double check_for_phero_limits(double tmp) {
         return Math.max(Math.min(tmp,phero_max), phero_min);
     }
 
     private Trie find_best_tree() {
         Trie t = trees.get(0);
 
         for (int i = 1; i < trees.size(); i++) {
             if (t.cost() > trees.get(i).cost())
                 t = trees.get(i);
         }
         return t;
     }
 
   
 
     private TrieList run_ants() {
         this.trees = new TrieList();
         threads = new ArrayList<Ant>();
 
         start_ants();
         wait_or_start_ants();
 
         return trees;
     }
 
     private void start_ants() {
         for (int i = 0; i < Utility.available_processor(); i++) {
             start_ant();
         }
     }
 
     private boolean start_ant() {
         synchronized (threads) {
             if (should_start_ant()) {
                 Ant a = new Ant(threads.size(), this, graph);
                 a.start();
                 threads.add(a);
                 return true;
             } else
                 return false;
         }
     }
 
     private void wait_or_start_ants() {
         boolean b = true;
 
         while (b) {
             if (this.ant_count() < this.ant_totals) // creates less t.isAlive() checks/loops
                 continue;
 
             b = false;
             synchronized (threads) {
                 for (Thread t : threads) {
                     if (t.isAlive()) {
                         b = true;
                         break;
                     }
                 }
             }
         }
     }
 
     private int ant_count() {
         synchronized (threads) {
             return threads.size();
         }
     }
 
     // used from within the ant algorithm when the ant is finished
     public synchronized void ant_done(Ant ant) {
         this.trees.add(ant.getTree());
         start_ant();
     }
 
     private boolean should_start_ant() {
         return ant_count() < this.ant_totals * Utility.available_processor();
     }
 }
