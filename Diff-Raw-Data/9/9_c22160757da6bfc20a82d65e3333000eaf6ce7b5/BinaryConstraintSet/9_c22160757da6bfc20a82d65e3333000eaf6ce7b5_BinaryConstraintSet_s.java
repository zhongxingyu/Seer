 package interiores.business.models.constraints;
 
 import interiores.business.models.backtracking.FurnitureVariable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ListIterator;
 
 
 // FurnitureVariable hashCode and equals should be indepenedent of the value it has
 // and only consider part of it???
 
 /**
  * This class maintains a set of binary constraints that can be accessed by giving two FurnitureVariables
  * @author alvaro
  */
 public class BinaryConstraintSet {
     
     /**
      * This class is an unordered pair of FurnitureVariables. It's unordered because
      * <f1,f2> equals <f2,f1> and also has the same hashCode. It's useful to not duplicate restrictions
      * and being able to access it from <f1,f2> and also <f2,f1>
      */
     public class UnorderedFurnitureVariablePair {
         
         private final FurnitureVariable first, second;
      
         public UnorderedFurnitureVariablePair(FurnitureVariable first, FurnitureVariable second) {
             super();
             this.first = first;
             this.second = second;
         }
 
         @Override
         public int hashCode() {
             // We assure that both <p1, p2> and <p2,p1> have the same Hash
             int hashFirst = first.hashCode();
             int hashSecond = second.hashCode();
             return (hashFirst + hashSecond);
         }
 
         @Override
         public boolean equals(Object other) {
             // To be consistent with the hashCode we have to assure that <f1,f2> == <f2,f1>
             if (other instanceof UnorderedFurnitureVariablePair) {
                     UnorderedFurnitureVariablePair otherPair = (UnorderedFurnitureVariablePair) other;
                     return ( (this.first.equals(otherPair.first) && this.second.equals(otherPair.second)) ||
                              (this.first.equals(otherPair.second) && this.second.equals(otherPair.first)) );
             }
             else return false;
         }
 
         @Override
         public String toString() { 
                return "(" + first + ", " + second + ")"; 
         }
 
 
     }
     
     // The set itself. A pair of variable can have multilple restrictions
     private HashMap<UnorderedFurnitureVariablePair, List<BinaryConstraint>>binConstraintsSet;
     
     /**
      * Void constructor
      */
     public BinaryConstraintSet() {
        
     }
     
     /**
      * Creates a BinaryConstraintSet from another BinaryConstraintSet, copying its content
      * @param other The other BinaryConstraintSet to be copied
      */
     public BinaryConstraintSet(BinaryConstraintSet other) {
         this.binConstraintsSet = other.binConstraintsSet;
     }
     
     /**
      * Adds a constraint to the set
      * @param fvariable1 A furniture variable
      * @param fvariable2 Another furniture variable
      * @param bc The binary constraint that associates fvariable1 with fvariable2
      * @return True if everything went correctly
      */
     public boolean addConstraint(FurnitureVariable fvariable1, FurnitureVariable fvariable2, BinaryConstraint bc) {
         
         UnorderedFurnitureVariablePair pair = new UnorderedFurnitureVariablePair(fvariable1,fvariable2);
         
         if (binConstraintsSet.get(pair) == null) {
             List<BinaryConstraint> list = new ArrayList<BinaryConstraint>();
             list.add(bc);
            binConstraintsSet.put(pair, list);
                
         }
         else {
             binConstraintsSet.get(pair).add(bc);
         }
         return true;
     }
     
     /**
      * Checks if all the constraints between fvariable1 and fvariable2. If they have no common constraint returns true.
      * @param fvariable1 A furniture variable
      * @param fvariable2 Another furniture variable
      * @return True if no constraint is violated, false otherwise
      */
     public boolean isSatisfied(FurnitureVariable fvariable1, FurnitureVariable fvariable2) {
         
         UnorderedFurnitureVariablePair pair = new UnorderedFurnitureVariablePair(fvariable1, fvariable2);
         
         // If the list is void we should return true
        if (binConstraintsSet != null && binConstraintsSet.containsKey(pair)) {
             List<BinaryConstraint> binConsL = binConstraintsSet.get(pair);
             for (BinaryConstraint bc : binConsL) {
                 if (! bc.isSatisfied(fvariable1, fvariable2)) return false;
             }
         }
         return true;                       
     }
 }
