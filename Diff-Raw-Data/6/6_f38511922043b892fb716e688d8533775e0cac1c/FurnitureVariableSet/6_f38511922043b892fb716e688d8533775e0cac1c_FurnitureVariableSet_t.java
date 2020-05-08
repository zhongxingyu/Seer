 package interiores.business.models.backtracking;
 
 import interiores.business.models.Orientation;
 import interiores.business.models.OrientedRectangle;
 import interiores.business.models.WishList;
 import interiores.business.models.catalogs.NamedCatalog;
 import interiores.business.models.constraints.Constraint;
 import interiores.business.models.constraints.furniture.BinaryConstraintEnd;
 import interiores.business.models.constraints.room.GlobalConstraint;
 import interiores.business.models.constraints.room.RoomBacktrackingTimeTrimmer;
 import interiores.business.models.constraints.room.RoomInexhaustiveTrimmer;
 import interiores.business.models.constraints.room.RoomPreliminarTrimmer;
 import interiores.business.models.constraints.room.global.BudgetConstraint;
 import interiores.business.models.constraints.room.global.EnoughSpaceConstraint;
 import interiores.business.models.constraints.room.global.SameColorConstraint;
 import interiores.business.models.constraints.room.global.SameMaterialConstraint;
 import interiores.business.models.constraints.room.global.SpaceRespectingConstraint;
 import interiores.business.models.constraints.room.global.UnfitModelsPseudoConstraint;
 import interiores.business.models.room.FurnitureModel;
 import interiores.business.models.room.FurnitureType;
 import interiores.business.models.room.elements.WantedFixed;
 import interiores.business.models.room.elements.WantedFurniture;
 import interiores.core.Debug;
 import interiores.core.business.BusinessException;
 import interiores.shared.backtracking.NoSolutionException;
 import interiores.shared.backtracking.Value;
 import interiores.shared.backtracking.VariableSet;
 import interiores.utils.Dimension;
 import java.awt.Point;
 import java.util.AbstractMap.SimpleEntry;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 public class FurnitureVariableSet
 	extends VariableSet
 {	
 
     /**
      * Represents the number of variables.
      */
     private int variableCount;
 
     /**
      * This list contains the variables which do not have an assigned value yet.
      */
     List<FurnitureVariable> unassignedVariables;
     
     /**
      * This list contains the variables which have an assigned value.
      */
     List<FurnitureVariable> assignedVariables;
     
     /**
      * This is the variable that the algorithm is trying to assign in the
      * current iteration.
      * It is not in either of the previous lists.
      */
     protected FurnitureVariable actual;
     
     List<FurnitureConstant> constants;
     
     /**
      * Indicates whether all variables have an assigned value.
      */
     protected boolean allAssigned;
 
     /**
      * Indicates restrictions amongst all variables.
      */
    List<GlobalConstraint> globalConstraints;
     
    /**
     * Holds information about the relationship between constraints of
     * different variables.
     * The first string identifies the ID of the variable that, when assigned,
     * the domain of the variable identified by the second string, will be
     * trimmed proportionally to the dependence stored in this matrix.
     * 
     * Detailed explanation in the header of Constraint class.
     */
    Map<Entry<String, String>, Integer> matrixOfDependence;
     
     /**
      * constants to calibrate the selection of the next actual variable.
      * The importance of each factor is proportional to the value of the
      * constant.
      */
     private static final int DOMAIN_SIZE_FACTOR = 30;
     private static final int SMALLEST_MODEL_FACTOR = 5;
     private static final int BINARY_CONSTRAINTS_FACTOR = 12;
     
     /**
      * Constructor.
      * This constructor does all the initilization work:
      * 
      * roomArea is initialized with the room rectangle.
      * 
      * variableCount is initialized with the number of variables.
      * 
      * allAssigned is initialized false.
      * 
      * assignedVariables is initialized empty.
      * 
      * actual is initialized null.
      * 
      * unassignedVariables is initialized with all variables.
      * Each variable is initialized with:
      * - all the models available for its furniture type.
      * - all the orientations
      * - all the positions of the room (roomArea)
      * - the list of unary constraints it is associated with.
      * 
      * constants is initialized with all FurnitureConstants.
      * 
      * globalConstraints is initialized with all global constraints.
      * The TrimUnfitModelsPseudoContraint is placed last in the list.
      * 
      * The matrix of dependence is built
      */
     public FurnitureVariableSet(WishList wishList, NamedCatalog<FurnitureType> furnitureCatalog)
             throws BusinessException {
 
         Dimension roomSize = wishList.getRoom().getDimension();
         OrientedRectangle roomArea = new OrientedRectangle(new Point(0, 0), roomSize, Orientation.S);
         
         variableCount = wishList.getUnfixedSize();
         
         allAssigned = false;
    
         assignedVariables = new ArrayList<FurnitureVariable>();
         
         actual = null;
         
         unassignedVariables = new ArrayList<FurnitureVariable>();
         addVariables(wishList, furnitureCatalog, roomSize);
         
         constants = new ArrayList<FurnitureConstant>();
         addConstants(wishList);
         
         globalConstraints = new ArrayList<GlobalConstraint>();
         addGlobalConstraints(roomArea);
         
         buildMatrixOfDependence();
 
     }
     
     /**
      * Invoked in the constructor to initialize constants.
      * @param wishList 
      */
     private void addConstants(WishList wishList) {
         for(WantedFixed wantedFixed : wishList.getWantedFixed())
             constants.add(wantedFixed);
     }
     
     /**
      * Invoked in the constructor to initialize variables.
      * @param wishList
      * @param furnitureCatalog
      * @param roomSize 
      */
     private void addVariables(WishList wishList, NamedCatalog<FurnitureType> furnitureCatalog,
             Dimension roomSize)
     {   
         for(WantedFurniture wantedFurniture : wishList.getWantedFurniture()) {
             HashSet<FurnitureModel> models = new HashSet(
                     furnitureCatalog.get(wantedFurniture.getTypeName()).getFurnitureModels());
             
             wantedFurniture.createDomain(models, roomSize, variableCount);
             unassignedVariables.add(wantedFurniture);
         }
     }
     
     /**
      * Invoked in the constructor to initialize global constraints.
      */
     private void addGlobalConstraints(OrientedRectangle roomArea) {
         globalConstraints.add(new SpaceRespectingConstraint(roomArea));
         globalConstraints.add(new EnoughSpaceConstraint(roomArea));
         boolean sameColorConstraintActive = false;
         boolean sameMaterialConstraintActive = false;
         boolean budgetConstraintActive = false;
         for (GlobalConstraint constraint : globalConstraints) {
             if (constraint instanceof SameColorConstraint)
                 sameColorConstraintActive = true;
             else if (constraint instanceof SameMaterialConstraint)
                 sameMaterialConstraintActive = true;
             else if (constraint instanceof BudgetConstraint)
                 budgetConstraintActive = true;
         }
         globalConstraints.add(new UnfitModelsPseudoConstraint(sameColorConstraintActive,
                 sameMaterialConstraintActive, budgetConstraintActive));
         
     }
     
 
     /**
      * Selects a variable from unassignedVariables and sets it
      * as actual variable.
      * 
      * Before setting a new actual variable, the previous one is stored in the
      * list of assignedVariables.
      * 
      * The variable is chosen according to 3 factors:
      * 1) The domain size: the smallest the better
      * 2) The binary constraints with variables that have not been set: the more
      * the better
      * 3) The size of the smallest model: the more the better
      * 
      * Each factor has a weight defined by a global constant, so that it can
      * be adjusted.
      * 
      * The iterators of the actual variable are reset.
      */ 
     @Override
     protected void setActualVariable() {
         if (unassignedVariables.isEmpty())
             allAssigned = true;
         else {
             
             //move previousactual to assigned, unless it is the first iteration
             if (actual != null)
                 assignedVariables.add(actual);
             
             int maxDomainSize = -1;
             int maxBinaryConstraints = -1;
             int maxSmallestModelArea = -1;
             int[] domainSize = new int[unassignedVariables.size()];
             int[] binaryConstraintsLoad = new int[unassignedVariables.size()];
             int[] smallestModelSize = new int[unassignedVariables.size()];
 
             int i = 0;
             //get value of each factor for each variable and compute maximums
             for (FurnitureVariable variable : unassignedVariables) {
                 domainSize[i] = variable.domainSize();
                 if (domainSize[i] > maxDomainSize) maxDomainSize = domainSize[i];
 
                 // the weight of all binary constraints between this variable and
                 // other variables which have not been assigned yet.
                 binaryConstraintsLoad[i] = 0;
                 
                 for (FurnitureVariable otherVariable : unassignedVariables) {
                     binaryConstraintsLoad[i] += getDependence(variable, otherVariable);
                 }
                 
                 if (binaryConstraintsLoad[i] > maxBinaryConstraints)
                     maxBinaryConstraints = binaryConstraintsLoad[i];
 
                 smallestModelSize[i] = variable.smallestModelSize();
                 if (smallestModelSize[i] > maxSmallestModelArea)
                     maxSmallestModelArea = smallestModelSize[i];
                 
                 ++i;
             }
 
             //calculate the weight of each factor for each variable and the final
             //weight of each variable, and compute the index of the variable with
             //the lowest overall weight
             int minimumWeight = -1;
             int minimumWeightVariableIndex = -1;
 
             for (i = 0; i < unassignedVariables.size(); ++i) {
 
                 int domainSizeWeight;
                 if (maxDomainSize > 0) domainSizeWeight =
                         (domainSize[i] * DOMAIN_SIZE_FACTOR) / maxDomainSize;
                 else domainSizeWeight = 0;
                 
                 int binaryConstraintsWeight;
                 if (maxBinaryConstraints > 0) binaryConstraintsWeight = maxBinaryConstraints -
                         (binaryConstraintsLoad[i] * BINARY_CONSTRAINTS_FACTOR) / maxBinaryConstraints;
                 else binaryConstraintsWeight = 0;
                 
                 int smallestModelSizeWeight =
                         (smallestModelSize[i] * SMALLEST_MODEL_FACTOR) / maxSmallestModelArea;
 
                 int variableWeight = domainSizeWeight + binaryConstraintsWeight +
                         smallestModelSizeWeight;
 
                 if (minimumWeight == -1 || variableWeight < minimumWeight) {
                     minimumWeight = variableWeight;
                     minimumWeightVariableIndex = i;
                 }
 
             }
             
             //set actual variable
             actual = unassignedVariables.get(minimumWeightVariableIndex);
             
             //remove it from the set of unassigned variables
             unassignedVariables.remove(minimumWeightVariableIndex);
             
             //reset iterators
             actual.resetIterators(depth);
         }
         
 //        Debug.println("...running SetActualVariable...");
 //        Debug.println("End of setActualVariable:\n" +
 //                "Depth: " + depth + "\n" +
 //                "Current actual: ");
 //        if (actual == null) Debug.println("null\n");
 //        else Debug.println(actual.getName());
 //        Debug.println("unassignedVariables.size() = " + unassignedVariables.size());
 //        Debug.println("assignedVariables.size() = " + assignedVariables.size());
 //        Debug.println("==========================");
     }
  
     @Override
     protected void trimDomains() {
         for (FurnitureVariable variable : unassignedVariables) {
             variable.trimDomain(actual, depth);   
         }
         
         //global constraints that implement the bactracking time trimmer interface
         //are triggered
         for (GlobalConstraint constraint : globalConstraints) {
             if (constraint instanceof RoomBacktrackingTimeTrimmer)
                 ((RoomBacktrackingTimeTrimmer) constraint).trim(
                     assignedVariables, unassignedVariables, constants, actual);
         }
     }
 
     @Override
     protected void undoTrimDomains(Value value) {
         for (FurnitureVariable variable : unassignedVariables) {
             variable.undoTrimDomain(actual, value, depth);
         }
         
         for (GlobalConstraint constraint : globalConstraints) {
             ((RoomInexhaustiveTrimmer) constraint).notifyStepBack(
                     assignedVariables, unassignedVariables, constants, actual, (FurnitureValue) value);
         }
     }
 
     @Override
     protected boolean allAssigned() {
        if (unassignedVariables.isEmpty()) {
            if (actual != null && actual.isAssigned()) {
                allAssigned = true;
            }
         }
         return allAssigned;
     }
 
     @Override
     protected boolean actualHasMoreValues() {
         return actual.hasMoreValues();
     }
     
     @Override
     protected Value getNextActualDomainValue() {
         return actual.getNextDomainValue();
     }
     
     @Override
     protected boolean canAssignToActual(Value value) 
         throws NoSolutionException {
         
         //temporal assignment
         actual.assignValue(value);
         
         //1) check that furniture constraints are satisfied
         if (! actual.constraintsSatisfied()) {
             actual.undoAssignValue();
             return false;
         }
         
         //2) check that room constraints are satisfied
         for (GlobalConstraint constraint : globalConstraints) {
             if (! ((RoomInexhaustiveTrimmer) constraint).isSatisfied(
                 assignedVariables, unassignedVariables, constants, actual))
             {
                 actual.undoAssignValue();
                 return false;
             }
         }
         
         actual.undoAssignValue();
         return true;
     }
 
     
     @Override
     protected void assignToActual(Value value) {        
         actual.assignValue(value);
     }
 
     
     @Override
     protected void undoAssignToActual() {
         actual.undoAssignValue();
     }
     
     //note: trivial implementation. To be optimized.
     @Override
     protected void preliminarTrimDomains() 
         throws NoSolutionException {
         
         //1) each variable triggers its own preliminar trimmers
         for (FurnitureVariable variable : unassignedVariables)
             variable.triggerPreliminarTrimmers();
         
         //2) global constraints that implement the preliminar trimmer interface
         //are triggered
         Iterator<GlobalConstraint> it = globalConstraints.iterator();
         while(it.hasNext()) {
             GlobalConstraint constraint = it.next();
             if (constraint instanceof RoomPreliminarTrimmer) {
                 ((RoomPreliminarTrimmer) constraint).preliminarTrim(unassignedVariables, constants);
             }
             //ditch it if it doesn't implement any other interface
             if (! (constraint instanceof RoomInexhaustiveTrimmer))
                 it.remove();
         }
     }
     
     
     private InterioresVariable getVariable(String name) {
         for (FurnitureVariable variable : unassignedVariables)
             if (variable.getName().equals(name)) return variable;
         for (FurnitureVariable variable : assignedVariables)
             if (variable.getName().equals(name)) return variable;
         if (actual.getName().equals(name)) return actual;
             
         
         throw new BusinessException(name + " variable not found.");
     }
    
     
     public Map<String, FurnitureValue> getVariableValues() {
         Map<String, FurnitureValue> values = new HashMap();
         
         for(FurnitureConstant constant : constants)
             values.put(constant.getName(), constant.getAssignedValue());
         
         for(FurnitureVariable variable : unassignedVariables)
             values.put(variable.getName(), variable.getAssignedValue());
      
         for(FurnitureVariable variable : assignedVariables)
             values.put(variable.getName(), variable.getAssignedValue());
         
         return values;
     }
  
     // Algorithm to build the matrix of dependence:
     // Initialize mod to all 0.
     // For each binary constraint bc:
     //      //assume v is the variable associated with bc
     //      //and w is the otherVariable of bc (bc.getOtherVariable())
     //      if w is not a constant:
     //          mod[w][v] = bc.getWeight()
     //      //this means that if w is assigned a value, the domain of v will be
     //      //restricted proportionally to the weight of bc.
     private void buildMatrixOfDependence() {
 
         matrixOfDependence = new HashMap<Entry<String, String>, Integer>();
                 
         for (FurnitureVariable v : unassignedVariables) {
             for (Constraint constraint : v.getBacktrackingConstraints()) {
                 if (constraint instanceof BinaryConstraintEnd) {
                     BinaryConstraintEnd bc = (BinaryConstraintEnd) constraint;
                     
                     InterioresVariable otherVariable = bc.getOtherVariable();
                     if (otherVariable instanceof FurnitureVariable) {
                         FurnitureVariable w = (FurnitureVariable) otherVariable;
 
                         Entry e = new SimpleEntry(w.getName(), v.getName());
                         matrixOfDependence.put(e, bc.getWeight());
                     }
                 }
             }
         }
     }
     
     
     private int getDependence(FurnitureVariable variable1, FurnitureVariable variable2) {
         Entry e = new SimpleEntry(variable1.getName(), variable2.getName());
         
         if (! matrixOfDependence.containsKey(e))
             return 0;
         
         return matrixOfDependence.get(e);
     }
     
     @Override
     protected void backtracking() throws NoSolutionException {
         super.backtracking();
         
         if(!allAssigned())
             undoSetActualVariable();
     }
     
     protected void undoSetActualVariable() {
         unassignedVariables.add(actual);
         if (! assignedVariables.isEmpty()) {
             actual = assignedVariables.get(assignedVariables.size()-1);
             assignedVariables.remove(assignedVariables.size()-1);
         }
     }
 }
