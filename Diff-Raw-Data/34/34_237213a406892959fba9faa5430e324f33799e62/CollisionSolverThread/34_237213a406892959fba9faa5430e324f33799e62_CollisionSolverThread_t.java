 package physics;
 
 import java.util.List;
 
 import actor.Actor;
 import actor.ActorSet;
 
 public class CollisionSolverThread extends Thread {
     ActorSet actors;
     protected final int stride, start;
     
     public CollisionSolverThread(ActorSet actors, int start, int stride){
         if(stride <= 0 || start < 0)
             throw new IllegalArgumentException("CollisionSolverThread: Stride must be positive");
         this.actors = actors;
         this.stride = stride;
         this.start = start;
     }
 
 
     public void checkCollisions(){
        // TODO it would be nice use a single list for all our threads
         List<Actor> actorsList = actors.getCopyList();
         // Check our guy we stride to against all the others
         for(int i = start; i < actorsList.size(); i += stride)
             for(int j = i + 1; j < actorsList.size(); ++j){
                 Actor a = actorsList.get(i);
                 Actor b = actorsList.get(j);
 
                 if(a.isColliding(b)){
                     a.handleCollision(b);
                     b.handleCollision(a);
                 }
                 
                 if (Thread.interrupted()) {
                     System.err.println("CollsionSolverThread: Thread " + this.getId() + " interrupted");
                     return;
                 }
             }
     }
 
     public void run(){
         checkCollisions();
     }
 }
