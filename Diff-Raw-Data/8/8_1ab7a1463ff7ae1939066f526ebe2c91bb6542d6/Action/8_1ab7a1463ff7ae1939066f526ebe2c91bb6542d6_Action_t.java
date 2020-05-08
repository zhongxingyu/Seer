 /**
  * Action.java
  * 
  * Created on 16.05.2013
  */
 
 package at.pria.koza.harmonic;
 
 
 import java.util.Deque;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 
 
 /**
  * <p>
  * The class {@code Action} represents a high-level change that can happen in the {@link Engine}. An {@code Action}
  * causes one or more {@link Modification Modifications} to happen. These are stored in a list, so they can be
  * {@linkplain #revert() reverted} if necessary.
  * </p>
  * <p>
  * As an {@code Action} represents an executable, replayable, and even revertable piece of code, calling it is of
  * course delicate; the goal is to get the same result every time it is applied. To achieve this, it is necessary
  * that the original state of the engine is compatible with the action. For a new action, this is the
  * responsibility of the user. For replaying actions on the same or different engines, Harmonic ensures this by
  * associating the action with the same state as the user originally did; together with Harmonic's requirement of
  * determinism of the engine, this should ensure the expected behavior.
  * </p>
  * 
  * @version V1.0 26.07.2013
  * @author SillyFreak
  */
 public abstract class Action {
     private static final ThreadLocal<Deque<Action>> actions;
     
     static {
         actions = new ThreadLocal<Deque<Action>>() {
             @Override
             protected Deque<Action> initialValue() {
                 return new LinkedList<>();
             }
         };
     }
     
     /**
      * <p>
      * Returns the currently active action. That action is the one that modifications will be added to.
      * </p>
      * 
      * @return the currently active action
      */
     static Action get() {
        Action a = actions.get().peekFirst();
        if(a == null) throw new IllegalStateException("No action active");
        return a;
     }
     
     private static void push(Action a) {
         actions.get().addFirst(a);
     }
     
     private static void pop(Action a) {
        Action a0 = actions.get().pollFirst();
        if(a == null) throw new IllegalStateException("No action active");
         assert a == a0;
     }
     
     
     private final Engine             engine;
     private final List<Modification> modifications;
     
     public Action(Engine engine) {
         this.engine = engine;
         modifications = new LinkedList<Modification>();
     }
     
     /**
      * <p>
      * Returns the engine this action is associated with.
      * </p>
      * 
      * @return the engine this action is associated with
      */
     public Engine getEngine() {
         return engine;
     }
     
     /**
      * <p>
      * Applies the action to the engine. This method invokes {@link #apply0()}, surrounded by calls to
      * {@link #push(Action)}, and {@link #pop(Action)} in a {@code finally} block to always leave the action stack
      * in a well defined way. Note that this does not mean that the engine, too, will be in a defined state; see
      * {@link #apply0()}.
      * </p>
      */
     public void apply() {
         try {
             push(this);
             apply0();
         } finally {
             pop(this);
         }
     }
     
     /**
      * <p>
      * Reverts the action by {@linkplain Modification#revert() reverting} every {@link Modification} in reverse
      * order. This method must only be called by the {@link Engine}. When this method is called, the engine will be
      * in the same state as it was after {@linkplain #apply() applying} this action.
      * </p>
      */
     public void revert() {
         for(ListIterator<Modification> it = modifications.listIterator(modifications.size()); it.hasPrevious();) {
             it.previous().revert();
             it.remove();
         }
     }
     
     /**
      * <p>
      * Called by {@link Modification#addToAction()}. Adds a {@link Modification} to this action so that it can be
      * subsequently reverted.
      * </p>
      * 
      * @param m the {@link Modification} to add
      */
     void addModification(Modification m) {
         modifications.add(m);
     }
     
     /**
      * <p>
      * Performs the actual action. This method must only be called from {@link #apply()}, where it will have an
      * {@code Action} context established for {@link Modification}s via the {@link #get()} method.
      * </p>
      * <p>
      * Note that this method is responsible for leaving the {@link Engine} in a well-defined state. If it does not
      * return normally, and the application needs to be able to roll back the engine, then this method must leave
      * the engine in a state where {@linkplain #revert() reverting} this action really does revert it into the
      * previous state.
      * </p>
      */
     protected abstract void apply0();
 }
