 package net.cscott.sdr;
 
 import net.cscott.jutil.UnmodifiableIterator;
 import net.cscott.sdr.calls.BadCallException;
 import net.cscott.sdr.calls.CallDB;
 import net.cscott.sdr.calls.DanceState;
 import net.cscott.sdr.calls.ast.Apply;
 import java.util.*;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 /** {@link CommandInput} implements the communication between some mechanism
  * for inputting commands (voice recognition, keyboard input, file on disk,
  * etc) and the rest of the SDR system.  In particular, we give the
  * {@code CommandInput} to the input mechanisms, and our {@link App}
  * class will deal with polling it for commands and feeding them to the
  * choreography engine and then to the animation system.
  * <p>
  * Note that a command is actually a lazy list of <i>possible</i> commands,
  * ordered from most-likely to least-likely.  We'll try each possibility
  * and take the first one that works.
  * @author C. Scott Ananian
 * @version $Id: CommandInput.java,v 1.2 2006-10-26 17:33:53 cananian Exp $
  */
 public class CommandInput {
     /** Create a CommandInput object to synchronize communication between
      * input routines and the choreography engine. */
     public CommandInput() { }
     
     /** This is the actual queue we'll use for synchronization. */
     private final BlockingQueue<PossibleCommand> queue =
         new LinkedBlockingQueue<PossibleCommand>();
     /**
      * Called by input methods (voice recognition, keyboard input,
      * filer readers, etc) when they have another (set of)
      * {@link PossibleCommand}(s) to issue.
      */
     public void addCommand(PossibleCommand c) {
         while (true)
             try {
                 queue.put(c);
                 return;
             } catch (InterruptedException e) {
                 assert false : "should never happen";
             }
     }
     /** Called by the main code to get the next command from the input
      * method.  Blocks until a possible command is available.
      * @return the next possible command.
      */
     public PossibleCommand getNextCommand() throws InterruptedException {
         return queue.take();
     }
     // helper methods.
     /** Create a PossibleCommand from a list of possibilities, ordered
      * from best to worst. */
     public PossibleCommand commandFromStrings(final DanceState ds,
                                                final List<String> l) {
         if (l.isEmpty()) return null;
         String best = l.get(0); // our next-best guess.
         Apply a;
         try {
             a = CallDB.INSTANCE.parse(ds.getProgram(), best);
         } catch (BadCallException e) {
             a = null; // we'll skip this one & end up trying the next.
         }
         final Apply aa = a;
         PossibleCommand pc = new PossibleCommand() {
             @Override
             public Apply get() { return aa; }
             @Override
             public PossibleCommand next() {
                 return commandFromStrings(ds, l.subList(1,l.size()));
             }
         };
         if (pc.get()==null) return pc.next();
         return pc;
     }
     
     /** A {@link PossibleCommand} is an {@link Apply} corresponding to
      * the most likely interpretation of the user's input.  If this
      * {@link Apply} is inapplicable to the given formation, we can
     * use the {@link PossibleCommand#link link} method to get the
      * next-most-likely interpretation of the input, and try that
      * before giving up and complaining to the user.
      */
     // XXX: is this really the interface we want?  seems cumbersome.
     public static abstract class PossibleCommand
     implements Iterable<PossibleCommand> {
         /** Return the command possibility. */
         public abstract Apply get();
         /** Return the next possible command, or null if there are no more. */
         public abstract PossibleCommand next();
 
         public final Iterator<PossibleCommand> iterator() {
             return new PCIterator(this);
         };
         private static class PCIterator
         extends UnmodifiableIterator<PossibleCommand> {
             private PossibleCommand pc;
             PCIterator(PossibleCommand pc) { this.pc = pc; }
             @Override
             public boolean hasNext() { return pc!=null; }
             @Override
             public PossibleCommand next() {
                 PossibleCommand npc=pc; pc=pc.next(); return npc;
             }
         }
     }
 }
