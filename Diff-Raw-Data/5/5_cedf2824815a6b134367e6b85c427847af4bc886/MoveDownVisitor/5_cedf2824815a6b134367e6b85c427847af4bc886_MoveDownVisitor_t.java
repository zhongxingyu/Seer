 package optimisation;
 
 import java.util.*;
 
 import relationenalgebra.*;
 import main.*;
 
 import static main.Database.trace;
 import static main.Database.traceExpression;
 
 /** Moves Selection objects as far down the expression tree as possible. */
 public class MoveDownVisitor extends ModifyVisitor {
   public MoveDownVisitor (Optimisations optimisations) {
     this.optimisations = optimisations;
     done = new HashSet <ITreeNode> ();
   }
 
   public Object visit (Selection x) {
     if (done.contains (x))
       return x;
 
     // traceExpression (x);
 
     /* switch selections, continuing with this selection though */
     if (x.child instanceof Selection) {
       Selection result = (Selection) x.child;
       x.child = result.child;
       result.child = (ITreeNode) dispatch (x);
       done.add (x);
      return dispatch (result);
     }
     else if (x.child instanceof Projection) {
       Projection result = (Projection) x.child;
       x.child = result.child;
       result.child = (ITreeNode) dispatch (x);
       done.add (x);
      return dispatch (result);
     }
     else if (x.child instanceof CrossProduct) {
       CrossProduct result = (CrossProduct) x.child;
 
       Collection <ColumnName> selected = optimisations.columnNames (x.expression),
 	names = optimisations.columnNames (result.first);
 
       // trace ("selected = " + selected);
       // trace ("names = " + names);
       // trace ("names.containsAll (selected) = " + names.containsAll (selected));
 
       if (names.containsAll (selected)) {
 	x.child = result.first;
 	result.first = (ITreeNode) dispatch (x);
 	done.add (x);
 	return dispatch (result);
       }
 
       names = optimisations.columnNames (result.second);
 
       // trace ("names = " + names);
       // trace ("names.containsAll (selected) = " + names.containsAll (selected));
 
       if (names.containsAll (selected)) {
 	x.child = result.second;
 	result.second = (ITreeNode) dispatch (x);
 	done.add (x);
 	return dispatch (result);
       }
     }
 
     x.child = (ITreeNode) dispatch (x.child);
 
     done.add (x);
     return x;
   }
 
   private Optimisations optimisations;
   /** Marks already visited objects, which shouldn't be visited again
       because of loops (e.g. two selections which could be in any order). */
   private Set <ITreeNode> done;
 }
