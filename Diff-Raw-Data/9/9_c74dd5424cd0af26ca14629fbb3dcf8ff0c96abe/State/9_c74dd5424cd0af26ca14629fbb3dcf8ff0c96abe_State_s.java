 /**
  * State.java
  * 
  * Created on 16.05.2013
  */
 
 package at.pria.koza.harmonic;
 
 
 import static java.lang.String.*;
 import at.pria.koza.harmonic.proto.HarmonicP.StateP;
 import at.pria.koza.polybuf.PolybufConfig;
 import at.pria.koza.polybuf.PolybufException;
 import at.pria.koza.polybuf.PolybufIO;
 import at.pria.koza.polybuf.PolybufInput;
 import at.pria.koza.polybuf.PolybufOutput;
 import at.pria.koza.polybuf.PolybufSerializable;
 import at.pria.koza.polybuf.proto.Polybuf.Obj;
 
 import com.google.protobuf.GeneratedMessage.GeneratedExtension;
 
 
 /**
  * <p>
  * The class State.
  * </p>
  * 
  * @version V0.0 16.05.2013
  * @author SillyFreak
  */
 public class State implements PolybufSerializable {
     public static final int                             FIELD     = StateP.STATE_FIELD_NUMBER;
     public static final GeneratedExtension<Obj, StateP> EXTENSION = StateP.state;
     
     public static PolybufIO<State> getIO(Engine engine) {
         return new IO(engine);
     }
     
     public static void configure(PolybufConfig config, Engine engine) {
         config.put(FIELD, getIO(engine));
     }
     
     private final Engine engine;
     private final long   id;
     private final State  parent;
     private final Action action;
     
     /**
      * <p>
      * Creates a root state for the given engine.
      * </p>
      * 
      * @param engine the engine for which this is the root state
      */
     State(Engine engine) {
         this(engine, null, 0, null);
     }
     
     /**
      * <p>
      * Creates a new state, using the {@linkplain Engine#nextStateId() next generated state ID} for that engine.
      * </p>
      * 
      * @param parent the parent state for this new state
      * @param action the action leading to this new state
      */
     public State(State parent, Action action) {
         this(parent.getEngine(), parent, parent.getEngine().nextStateId(), action);
     }
     
     /**
      * <p>
      * This constructor is only directly called by the {@link IO}. In contrast to a newly created state, a
      * deserialized state has an ID assigned by a different engine.
      * </p>
      * 
      * @param engine the engine for which this state is created/deserialized
      * @param parent the parent state for this state
      * @param id the ID assigned to this state by the engine that originally created it
      * @param action the action leading to this state
      */
    private State(Engine engine, State parent, long id, Action action) {
         this.engine = engine;
         this.id = id;
         this.parent = parent;
         this.action = action;
         engine.putState(this);
     }
     
     /**
      * <p>
      * Returns the engine in which this state resides. This may be different from the engine which originally
      * created the state.
      * </p>
      * 
      * @return the engine in which this state resides
      */
     public Engine getEngine() {
         return engine;
     }
     
     /**
      * <p>
      * Returns the unique ID assigned to this state. For the root state of any engine, this is zero. Otherwise, the
      * upper four bytes identify the original engine that created it, the lower four bytes is a sequentially
      * assigned number chosen by that engine.
      * </p>
      * 
      * @return the unique ID assigned to this state
      */
     public long getId() {
         return id;
     }
     
     /**
      * <p>
      * Returns the id of the engine that created this state, extracted from this state's id.
      * </p>
      * 
      * @return
      */
     public int getEngineId() {
         return (int) (id >> 32);
     }
     
     /**
      * <p>
      * Returns this state's parent. Only the root state has {@code null} as its parent.
      * </p>
      * 
      * @return this state's parent
      */
     public State getParent() {
         return parent;
     }
     
     /**
      * <p>
      * Returns the action that led from the parent to this state.
      * </p>
      * 
      * @return the action that led from the parent to this state
      */
     public Action getAction() {
         return action;
     }
     
     @Override
     public int getTypeId() {
         return FIELD;
     }
     
     /**
      * <p>
      * Computes and returns the nearest common predecessor between this and another state. More formally, this
      * returns the state that is a predecessor of both {@code this} and {@code other}, but whose children are not.
      * </p>
      * <p>
      * This method may return the root state of the engine, but never {@code null}.
      * </p>
      * 
      * @param other the other state for which to find the nearest common predecessor
      * @return the nearest common predecessor state
      * @see <a href="http://twistedoakstudios.com/blog/Post3280__">Algorithm source</a>
      */
     public State getCommonPredecessor(State other) {
         if(getEngine() != other.getEngine()) throw new IllegalArgumentException();
         
         //code taken from
         //http://twistedoakstudios.com/blog/Post3280_intersecting-linked-lists-faster
         
         // find *any* common node, and the distances to it
         State node0 = this, node1 = other;
         int dist0 = 0, dist1 = 0;
         int stepSize = 1;
         
         while(node0 != node1) {
             // advance each node progressively farther, watching for the other node
             for(int i = 0; i < stepSize; i++) {
                 if(node0.getId() == 0) break;
                 if(node0 == node1) break;
                 node0 = node0.getParent();
                 dist0 += 1;
             }
             stepSize *= 2;
             for(int i = 0; i < stepSize; i++) {
                 if(node1.getId() == 0) break;
                 if(node0 == node1) break;
                 node1 = node1.getParent();
                 dist1 += 1;
             }
             stepSize *= 2;
         }
         
         node0 = this;
         node1 = other;
         // align heads to be an equal distance from the first common node
         int r = dist1 - dist0;
         while(r < 0) {
             node0 = node0.getParent();
             r += 1;
         }
         while(r > 0) {
             node1 = node1.getParent();
             r -= 1;
         }
         
         // advance heads until they meet at the first common node
         while(node0 != node1) {
             node0 = node0.getParent();
             node1 = node1.getParent();
         }
         return node0;
     }
     
     @Override
     public String toString() {
         return format("%s@%016X: %s", getClass().getSimpleName(), id, action);
     }
     
     private static class IO implements PolybufIO<State> {
         private final Engine engine;
         
         public IO(Engine engine) {
             this.engine = engine;
         }
         
         @Override
         public void serialize(PolybufOutput out, State object, Obj.Builder obj) throws PolybufException {
             StateP.Builder b = StateP.newBuilder();
             b.setId(object.id);
             b.setParent(object.parent.id);
             b.setAction(out.writeObject(object.action));
             
             obj.setExtension(EXTENSION, b.build());
         }
         
         @Override
         public State initialize(PolybufInput in, Obj obj) throws PolybufException {
             StateP p = obj.getExtension(EXTENSION);
             long id = p.getId();
             State parent = engine.getState(p.getParent());
             //the action won't have a reference to this state, so this line is safe
             Action action = (Action) in.readObject(p.getAction());
             
             return new State(engine, parent, id, action);
         }
         
         @Override
         public void deserialize(PolybufInput in, Obj obj, State object) throws PolybufException {}
     }
 }
