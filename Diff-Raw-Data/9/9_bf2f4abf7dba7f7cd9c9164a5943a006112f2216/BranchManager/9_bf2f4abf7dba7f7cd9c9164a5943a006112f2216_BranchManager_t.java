 /**
  * BranchManager.java
  * 
  * Created on 29.07.2013
  */
 
 package at.pria.koza.harmonic;
 
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Set;
 
 import at.pria.koza.harmonic.proto.HarmonicP.StateP;
 import at.pria.koza.polybuf.PolybufConfig;
 import at.pria.koza.polybuf.PolybufException;
 import at.pria.koza.polybuf.PolybufIO;
 import at.pria.koza.polybuf.PolybufInput;
 import at.pria.koza.polybuf.PolybufOutput;
 import at.pria.koza.polybuf.PolybufSerializable;
 import at.pria.koza.polybuf.proto.Polybuf.Obj;
 
 
 /**
  * <p>
  * A {@code BranchManager} is a wrapper for an {@link Engine}. It enables easy and efficient synchronization
  * between multiple BranchManagers by managing metadata for the states in the engine, and named branches for which
  * updates may be published between engines. At the moment, a branch manager does not operate on existing and
  * possibly already modified engines. Instead, it creates a new engine and controls all access to it. This may
  * change in the future.
  * </p>
  * <p>
  * This class does not provide network protocols for achieving this ends. It is the backend containing logic to
  * create and process the information necessary for such messages, but does not mandate any specific protocol
  * formats to be used to transport that information.
  * </p>
  * 
  * @version V1.0 29.07.2013
  * @author SillyFreak
  */
 public class BranchManager {
     private final Engine                   engine;
     private final Map<String, MetaState[]> branches = new HashMap<>();
     private final Map<Long, MetaState>     states   = new HashMap<>();
     private final PolybufConfig            config   = new PolybufConfig();
     private String                         currentBranch;
     
     //ctors & misc
     
     /**
      * <p>
      * Creates a new branch manager
      * </p>
      * 
      * @see Engine#Engine()
      */
     public BranchManager() {
         this(new Engine());
     }
     
     /**
      * <p>
      * Creates a new branch manager.
      * </p>
      * 
      * @param spectating whether the engine will only spectate or also execute actions
      * 
      * @see Engine#Engine(boolean)
      */
     public BranchManager(boolean spectating) {
         this(new Engine(spectating));
     }
     
     /**
      * <p>
      * Creates a new branch manager.
      * </p>
      * 
      * @param id the ID to be used for the engine
      * 
      * @see Engine#Engine(int)
      */
     public BranchManager(int id) {
         this(new Engine(id));
     }
     
     /**
      * <p>
      * Creates a new branch manager.
      * </p>
      */
     private BranchManager(Engine engine) {
         this.engine = engine;
         configure(config);
         
         //put the root
         currentBranch = "default";
         createBranch(currentBranch, engine.getState(0l));
     }
     
     /**
      * <p>
      * Returns the engine underlying this BranchManager. The engine must not be modified mannually.
      * </p>
      * 
      * @return
      */
     public Engine getEngine() {
         return engine;
     }
     
     /**
      * <p>
      * Returns the {@link PolybufConfig} used by this BranchManager to serialize and deserialize states (and thus
      * also actions). A custom {@link PolybufIO IO} for {@linkplain State states} is already registered, and must
      * not be overwritten!
      * </p>
      * 
      * @return the {@link PolybufConfig} used by this BranchManager
      */
     public PolybufConfig getConfig() {
         return config;
     }
     
     //branch mgmt
     
     public void createBranchHere(String branch) {
         createBranch(branch, getBranchTip(currentBranch));
     }
     
     public void createBranch(String branch, State state) {
         if(state.getEngine() != engine) throw new IllegalArgumentException();
         if(branches.containsKey(branch)) throw new IllegalArgumentException();
         branches.put(branch, new MetaState[] {put(state)});
     }
     
     public State getBranchTip(String branch) {
         MetaState[] tip = branches.get(branch);
         if(tip == null) throw new IllegalArgumentException();
         return tip[0].state;
     }
     
     public String getCurrentBranch() {
         return currentBranch;
     }
     
     public void setCurrentBranch(String branch) {
         State tip = getBranchTip(branch);
         engine.setHead(tip);
         currentBranch = branch;
     }
     
     public <T extends Action> T execute(T action) {
         MetaState[] tip = branches.get(currentBranch);
         State state = new State(tip[0].state, action);
         engine.setHead(state);
         tip[0] = put(state);
         return action;
     }
     
     //receive branch sync
     
     /**
      * <p>
      * Receives an update offer from a remote branch manager. The branch to be updated consists of the branch
      * owner's ID in hex (16 digits), a slash and a branch name. The {@code state} contains the full information
      * about the branch's tip as the remote BranchManager knows it, and the {@code ancestors} array contains state
      * IDs that the remote BranchManager thought this BranchManager already knew, as to allow to communicate deltas
      * as small as possible. In the case that this BranchManager was already up to date, or had the parent of the
      * new state, and could therefore update immediately, the return value will be the new state's id. Otherwise,
      * it will be the latest state's id of which the manager knows it's on the branch; likely either an element of
      * the {@code ancestors} array, or {@code 0}.
      * </p>
      * 
      * @param engine the id of the offering BranchManager's engine
      * @param branch the branch this update belongs to
      * @param state the state being the tip of this update
      * @param ancestors a list of ancestor state IDs the remote branch manager thought this branch manager might
      *            already be aware of; most recent first
      * @return the most recent state id that this BranchManager knows for the given branch; {@code 0} if the branch
      *         is unknown; the {@code state}'s id if the full branch is known
      */
     public long receiveUpdate(int engine, String branch, Obj state, long... ancestors) {
         MetaState newHead = deserialize(state);
         put(newHead);
         if(newHead.resolve()) {
             //we have all we need
             newHead.addEngine(engine);
             
             MetaState[] head = branches.get(branch);
             if(head == null) branches.put(branch, head = new MetaState[1]);
             head[0] = newHead;
            if(currentBranch.equals(branch)) this.engine.setHead(head[0].state);
             
             return newHead.stateId;
             
         } else {
             //we need additional states
             for(long l:ancestors)
                 if(states.containsKey(l)) return l;
             
             //we know none of the given ancestors
             return 0;
         }
     }
     
     /**
      * <p>
      * Receives the missing states for a previous {@link #receiveUpdate(int, String, Obj, long...) receiveUpdate()}
      * call. The BranchManager does not save any transient state between {@code receiveUpdate()} and
      * {@code receiveMissing()}, so some information must be added to the parameters again: the source of the
      * update; and the branch being updated. To find again the state which was already received, the id of the head
      * state of the update must be transmitted again. In addition, a list of states containing the delta between
      * the remote and this BranchManager's branch is transmitted.
      * </p>
      * 
      * @param engine the id of the offering BranchManager's engine
      * @param branch the branch this update belongs to
      * @param state the id of the state being the tip of this update
      * @param ancestors a list of ancestor states that is missing from the local branch, in chronological order
      */
     public void receiveMissing(int engine, String branch, long state, Obj... ancestors) {
         for(Obj obj:ancestors) {
             MetaState s = deserialize(obj);
             put(s);
             if(!s.resolve()) throw new AssertionError();
             s.addEngine(engine);
         }
         
         MetaState newHead = states.get(state);
         if(!newHead.resolve()) throw new AssertionError();
         newHead.addEngine(engine);
         
         MetaState[] head = branches.get(branch);
         head[0] = newHead;
        if(currentBranch.equals(branch)) this.engine.setHead(head[0].state);
     }
     
     //send branch sync
     
     /**
      * <p>
      * Determines which data has to be sent to the {@link BranchManager} identified by {@code engine} to update the
      * given branch. If there is anything to update, this method provides this data to the caller through
      * {@link SyncCallback#sendUpdateCallback(int, String, Obj, long...) callback.sendUpdateCallback()}.
      * </p>
      * 
      * @param engine the engine which should be updated
      * @param branch the branch for which updates should be provided
      * @param callback a callback to provide the data to the caller
      */
     public void sendUpdate(int engine, String branch, SyncCallback callback) {
         MetaState[] head = branches.get(branch);
         if(head == null || head[0] == null) throw new IllegalArgumentException();
         
         MetaState state = head[0];
         Integer id = engine;
         while(state != null && !state.engines.contains(id))
             state = state.parent;
         if(state == head[0]) return;
         
        head[0].addEngine(engine);
         long[] ancestors = state == null? new long[0]:new long[] {state.stateId};
         callback.sendUpdateCallback(this.engine.getId(), branch, serialize(head[0]), ancestors);
     }
     
     /**
      * <p>
      * Determines which states are missing at the {@link BranchManager} identified by {@code engine} provided the
      * known ancestor. If there is anything to update, this method provides this data to the caller through
      * {@link SyncCallback#sendMissingCallback(int, String, long, Obj...) callback.sendMissingCallback()}.
      * </p>
      * 
      * @param engine the engine which should be updated
      * @param branch the branch for which updates should be provided
      * @param ancestor the ancestor the remote branch manager reported it knew
      * @param callback a callback to provide the data to the caller
      */
     public void sendMissing(int engine, String branch, long ancestor, SyncCallback callback) {
         MetaState[] head = branches.get(branch);
         if(head == null || head[0] == null) throw new IllegalArgumentException();
         
         long headId = head[0].stateId;
         if(headId == ancestor) return;
         
         LinkedList<Obj> ancestors = new LinkedList<>();
         for(MetaState state = head[0].parent; state.stateId != ancestor; state = state.parent) {
            state.addEngine(engine);
             ancestors.addFirst(serialize(state));
         }
         
         callback.sendMissingCallback(this.engine.getId(), branch, headId,
                 ancestors.toArray(new Obj[ancestors.size()]));
     }
     
     public static interface SyncCallback {
         /**
          * <p>
          * Reports the data needed to call {@link BranchManager#receiveUpdate(int, String, Obj, long...)
          * receiveUpdate()} on the receiving BranchManager.
          * </p>
          */
         public void sendUpdateCallback(int engine, String branch, Obj state, long... ancestors);
         
         /**
          * <p>
          * Reports the data needed to call {@link BranchManager#receiveMissing(int, String, long, Obj...)
          * receiveMissing()} on the receiving BranchManager.
          * </p>
          */
         public void sendMissingCallback(int engine, String branch, long state, Obj... ancestors);
     }
     
     //state mgmt
     
     private MetaState deserialize(Obj state) {
         try {
             PolybufInput in = new PolybufInput(config);
             return (MetaState) in.readObject(state);
         } catch(PolybufException | ClassCastException ex) {
             throw new IllegalArgumentException(ex);
         }
     }
     
     private Obj serialize(MetaState state) {
         try {
             PolybufOutput out = new PolybufOutput(config);
             return out.writeObject(state);
         } catch(PolybufException ex) {
             throw new IllegalArgumentException(ex);
         }
     }
     
     private void put(MetaState state) {
         states.put(state.stateId, state);
     }
     
     private MetaState put(State state) {
         Long id = state.getId();
         MetaState result = states.get(id);
         if(result == null) {
             states.put(id, result = new MetaState(state));
         }
         return result;
     }
     
     private class MetaState implements PolybufSerializable {
         private final long         stateId, parentId;
         
         private Action             action;
         
         private MetaState          parent;
         private State              state;
         
         //set of engines known to know this meta state
         private final Set<Integer> engines = new HashSet<>();
         
         /**
          * <p>
          * Used to add states created by the engine managed by this branch manager.
          * </p>
          * 
          * @param state the state to be added
          */
         public MetaState(State state) {
             this.state = state;
             addEngine(engine.getId());
             
             stateId = state.getId();
             if(stateId != 0) {
                 State parentState = state.getParent();
                 parentId = parentState.getId();
                 parent = put(parentState);
             } else {
                 parentId = 0;
             }
         }
         
         /**
          * <p>
          * Used to add states received from an engine other than managed by this branch manager.
          * {@linkplain #resolve() Resolving} will be necessary before this MetaState can be used.
          * </p>
          * 
          * @param state the protobuf serialized form of the state to be added
          * @param action the action extracted from that protobuf extension
          */
         public MetaState(StateP state, Action action) {
             stateId = state.getId();
             parentId = state.getParent();
             this.action = action;
         }
         
         @Override
         public int getTypeId() {
             return State.FIELD;
         }
         
         /**
          * <p>
          * Resolves this state. Returns true when the MetaState is now fully initialized, false if it is still not.
          * This method must be called for states received from another engine, as the parent state may not be
          * present at the time it is received. After all necessary ancestor states were received, then resolving
          * will be successful and the state will be added to the underlying engine.
          * </p>
          * 
          * @return {@code true} if the MetaState was resolved, so that there is now a corresponding {@link State}
          *         in the underlying engine; {@code false} otherwise
          */
         public boolean resolve() {
             if(state != null) return true;
             assert stateId != 0;
             if(parent == null) parent = states.get(parentId);
             if(parent == null || !parent.resolve()) return false;
             
             state = new State(parent.state, action);
             addEngine(engine.getId());
             addEngine(state.getEngineId());
             
             return true;
         }
         
         public void addEngine(int id) {
             engines.add(id);
         }
     }
     
     //polybuf
     
     private PolybufIO<MetaState> getIO() {
         return new IO();
     }
     
     private void configure(PolybufConfig config) {
         config.put(State.FIELD, getIO());
     }
     
     private class IO implements PolybufIO<MetaState> {
         private final PolybufIO<State> delegate;
         
         public IO() {
             delegate = State.getIO(engine);
         }
         
         @Override
         public void serialize(PolybufOutput out, MetaState object, Obj.Builder obj) throws PolybufException {
             delegate.serialize(out, object.state, obj);
         }
         
         @Override
         public MetaState initialize(PolybufInput in, Obj obj) throws PolybufException {
             StateP p = obj.getExtension(State.EXTENSION);
             Action action = (Action) in.readObject(p.getAction());
             return new MetaState(p, action);
         }
         
         @Override
         public void deserialize(PolybufInput in, Obj obj, MetaState object) throws PolybufException {}
     }
 }
