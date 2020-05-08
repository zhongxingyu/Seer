 package program.mapping;
 
 import java.util.ArrayList;
 import lejos.nxt.comm.RConsole;
 
 public class Mapper {
 
     public Mapper() {
     	
     }
 
     public ArrayList<Step> getPath(Road current, ArrayList<Goal> goals) throws Exception {
         if (goals.size() < 1) {
             throw new Exception("No goals given! Must have at least one goal!");
         }   
         // Find goals
         ArrayList<Step> path = findPath(current, goals);
         
         // Find path to exit
         Road last = path.get(path.size() - 1).getRoad();
         Map.resetMap();
         ArrayList<Goal> start = new ArrayList<Goal>();
         start.add(new Goal("R23", 0, Direction.Left));       
         ArrayList<Step> pathToStart = findPath(last, start);
         
         path.addAll(pathToStart);       
         
        // Change last park direction to exit city direction
        path.remove(path.size() - 1);
        path.add(new Step());        
         
         // Manually add in the first step
         Step step = new Turn(current, Direction.Straight);        
         path.add(0, step);
         
         return path;//return the complete path from the start to all the goals and back again
     }
 
     private ArrayList<Step> findPath(Road current, ArrayList<Goal> goals) throws Exception { // Runs the A* search, puts result in path
     	RConsole.println("FIND PATH...");    	
 
         ArrayList<Road> open = new ArrayList<Road>(); // The nodes that need to be expanded
         ArrayList<Road> closed = new ArrayList<Road>(); // The nodes that have already been expanded
         
         Road goal = null;
         current.setG_value(0);
         open.add(current);
         
         while (goal == null) {// While a goal has not been found        
             goal = expand(current, open, closed, goals);            
             if (goal == null) current = getBestNode(open);            
         }
         
         ArrayList<Step> path = genPath(goal, goals);
 
         
         if (goals.size() > 0) {
         	Map.resetMap();
             ArrayList<Step> nextPath = findPath(goal, goals);//Recursively call findPath to find the next goal
             //store the next part of the path in path
             for (int i = 0; i < nextPath.size(); i++) {
                 path.add(nextPath.get(i));
             }
         }       
         
         return path;
     }
     
     //takes the expanded map and works backward from goal to the start and generates the directions
     private ArrayList<Step> genPath(Road goal, ArrayList<Goal> goals) throws Exception {
     	RConsole.println("GEN PATH...");
         assert goal != null;
         assert goals.size() > 0;
         assert goal.hasExpandedLeftParent() || goal.hasExpandedLeftParent() || goal.hasExpandedStraightParent();
         assert goal.getG_value() > 0;
 
         ArrayList<Step> path = new ArrayList<Step>();
         Goal goalInfo = Goal.getGoal(goals, goal);
         assert goalInfo != null;        
         //give parking information
         path.add(new Park(goal, goalInfo.getDirection(), goalInfo.getPark()));
 
         goals.remove(goalInfo);//we already found this goal, so remove it
         Road current = goal;
         RConsole.println(goal.getName());
         while (current.getG_value() != 0) {//while we have not found our starting point: a g_value of 0
             //place all the parents of current in convenient variables
             //get_parent returns null if no parent
         	ArrayList<Road> right = current.getExpandedRightParents();
         	ArrayList<Road> left = current.getExpandedLeftParents();
             Road straight = current.getStraightParent();
             RConsole.println("Right:");
             for(Road r : right) {
             	RConsole.print("(" + r.getName() + ", " + String.valueOf(r.getG_value()) + "), ");
             }
             RConsole.println("Left:");
             for(Road r : left) {
             	RConsole.print("(" + r.getName() + ", " + String.valueOf(r.getG_value()) + "), ");
             }
 
             ArrayList<Road> parents = new ArrayList<Road>();//put all parents in here to run through the getBestNode function
             //only add parent to parents if it is not null. makes sure you don't accidently choose a -1 node
             if (current.hasExpandedRightParent()) {
                 parents.addAll(right);
             }
             if (current.hasExpandedLeftParent()) {
                 parents.addAll(left);
             }
             if (current.hasExpandedStraightParent()) {
                 parents.add(straight);
             }
 
             assert parents.size() > 0;//all parents are null
             RConsole.print("Parents: ");
             for(Road r : parents) {
             	RConsole.print("(" + r.getName() + ", " + String.valueOf(r.getG_value()) + "), ");
             }
             RConsole.println("");
             
             
 
             Road best = getBestNode(parents);//find the node with the least g_value that is not -1
             RConsole.println("Best: (" + best.getName() + ", " + best.getG_value() + ")");
             RConsole.println("------------------------------------------------------------------------");
 
             //record in path, reverses directions because this function travels backwards: from goal to start
             if (right.contains(best)) {
                 path.add(0, new Turn(current, Direction.Left));
             } else if (left.contains(best)) {
                 path.add(0, new Turn(current, Direction.Right));
             } else if (best == straight) {
                 path.add(0, new Turn(current, Direction.Straight));
             } else {
                 assert false;//code should not reach this!
             }
             current = best;// record best in current so we can continue traveling backwards toward the start
 
         }
         assert current.getG_value() == 0;
         assert path.size() > 0;
         assert path.get(path.size() - 1) instanceof Park;
         return path;
     }
 
     // returns the node in open with the smallest gvalue + h(x) where h(x) is the heuristic
     private Road getBestNode(ArrayList<Road> open) {
         assert open.size() > 0;
         Road best = open.get(0);
         for (int i = 0; i < open.size(); i++) {
             Road road = open.get(i);
             if (road.getG_value() < best.getG_value()) {
                 best = road;
             }
         }
         assert best.getG_value() != -1;
         assert best != null;
         return best;
     }
 
     //expands open into the nodes surrounding current. Returns the first goal hit, if one is hit and returns null otherwise
     private Road expand(Road current, ArrayList<Road> open, ArrayList<Road> closed, ArrayList<Goal> goals) {
         assert current != null;
         assert goals.size() > 0;
         assert !closed.contains(current);
         assert open.contains(current);
         assert current.hasLeftChild() || current.hasRightChild() || current.hasStraightChild();
         assert !Goal.isGoal(goals, current);
 
         //getChild returns null if no child
         Road right = current.getChildRight();
         Road left = current.getChildLeft();
         Road straight = current.getChildStraight();
 
         //if child exists and we have not expanded it yet
         if (current.hasRightChild() && !closed.contains(right) && !open.contains(right)) {
         	assert !closed.contains(right);
         	right.setG_value(current.getG_value() + right.getCost());//record the cost of getting here
         	RConsole.println(current.getName() + "->" + right.getName()+ ", G: " + right.getG_value());
             
             if (Goal.isGoal(goals, right)) {
                 
                 return right; //check if we hit one of the goals, if so return it
             }
             
             open.add(right);//add road to the open list so it can be expanded in the future
         }
 
         if (current.hasLeftChild() && !closed.contains(left) && !open.contains(left)) {
         	assert !closed.contains(left);
             left.setG_value(current.getG_value() + left.getCost());
             RConsole.println(current.getName() + "->" + left.getName()+ ", G: " + left.getG_value());
             if (Goal.isGoal(goals, left)) {
                 return left;
             }
             open.add(left);
         }
 
         if (current.hasStraightChild() && !closed.contains(straight)  && !open.contains(straight)) {
         	assert !closed.contains(straight);
             straight.setG_value(current.getG_value() + straight.getCost());
             RConsole.println(current.getName() + "->" + straight.getName() + ", G: " + straight.getG_value());
             if (Goal.isGoal(goals, straight)) {
                 return straight;
             }
             open.add(straight);
         }     
         
         closed.add(current);//make sure we don't expand current again
         open.remove(current);//remove from open because it has already been expanded
         assert open.size() > 0;
         return null;//null because we did not find the goal yet
     }
 
 }
