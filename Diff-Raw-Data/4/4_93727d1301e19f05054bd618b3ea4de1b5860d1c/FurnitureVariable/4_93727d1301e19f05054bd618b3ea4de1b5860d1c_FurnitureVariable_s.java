 package interiores.business.models.backtracking;
 
 import interiores.business.models.FurnitureModel;
 import interiores.business.models.Orientation;
 import interiores.business.models.OrientedRectangle;
 import interiores.business.models.Room;
 import interiores.business.models.constraints.UnaryConstraint;
 import interiores.core.Debug;
 import interiores.shared.backtracking.Value;
 import interiores.shared.backtracking.Variable;
 import interiores.utils.Dimension;
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 
 public class FurnitureVariable
 	implements Variable
 {
     private String identifier;
 
     /**
     * This vector of lists contains all models available for this variable.
     * The models belong to the furniture type associated to the variable.
     * 
     * Moreover, it gives information about what models have been discarded and,
     * in such cases, in which iteration of the algorithm they were discarded:
     * At any given iteration i (depth = i) of the algorithm, all models which
     * have not been discarded are in the list at the i position of the vector.
     * Models which have been discarded are in the list at the position
     * correspondent to the iteration in which they were discarded.
     * Lists beyond the position i are empty.
     * 
     * The vector size never changes and is equal to the amount of iterations of
     * the algorithm.
     */
     public Collection<FurnitureModel> domainModels;
 
     /**
     * This vector of hash sets contains all positions available for this variable.
     * 
     * Moreover, it gives information about what positions have been discarded and,
     * in such cases, in which iteration of the algorithm they were discarded:
     * At any given iteration i (depth = i) of the algorithm, all positions which
     * have not been discarded are in the set at the i position of the vector.
     * Positions which have been discarded are in the set at the position
     * correspondent to the iteration in which they were discarded.
     * Sets beyond the position i are empty.
     * 
     * The vector size never changes and is equal to the amount of iterations of
     * the algorithm.
     */
     public HashSet<Point>[] domainPositions;
     
     /**
      * This list contains all four possible orientations.
      */
     public List<Orientation> orientations;
     
     /**
      * This list contains the constraints regarding the variable.
      */
     public Collection<UnaryConstraint> unaryConstraints;
 
     /**
     * Represents the value taken by the variable, in case it is assigned.
     * Only valid when isAssigned is true.
     */
     public Value assignedValue;
     boolean isAssigned;
     
     /**
     * Represents the iteration of the algorithm.
     */
     public int iteration;
     
     // The following variables are used to iterate over the domain.
     //Iteration is done in this order: 1) Position, 2) Orientation, 3) Models
     private Iterator positionIterator;
     private Iterator orientationIterator;
     private Iterator modelIterator;
     
     private Point currentPosition;
     private Orientation currentOrientation;
     private FurnitureModel currentModel;
     
     private boolean firstValueIteration;
     
     
     /**
      * Default Constructor. The resulting variable has as domain the models in
      * "models", every position in room and all orientations.
      * The set of restrictions is "unaryConstraints". Its resolution defaults to 5.
      * @pre the iteration of the variableSet is 0
      */
     public FurnitureVariable(String id, Collection<FurnitureModel> models, Dimension roomSize,
             Collection<UnaryConstraint> unaryConstraints, int variableCount) {
         this(id, models, roomSize, unaryConstraints, variableCount, 5);
     }
     
     
     
     public FurnitureVariable(String id, Collection<FurnitureModel> models, Dimension roomSize,
             Collection<UnaryConstraint> unaryConstraints, int variableCount, int resolution)
     {    
         identifier = id;
         
         isAssigned = false;
         iteration = 0;
     
         domainPositions = new HashSet[variableCount];
         for(int i = 0; i < variableCount; ++i)
             domainPositions[i] = null;        
         
         orientations = new ArrayList<Orientation>();
         defaultOrientations();
         
         domainModels = models;
         this.unaryConstraints = unaryConstraints;
 
         //add all positions in the room
         domainPositions[0] = new HashSet<Point>();
        for (int i = 0; i < roomSize.depth; i += resolution) {
            for (int j = 0; j < roomSize.width; j += resolution)
                 domainPositions[0].add(new Point(i,j));
         }
         
         currentPosition = null;
         currentOrientation = null;
         currentModel = null;
         
         positionIterator = domainPositions[0].iterator();
         orientationIterator = orientations.iterator();
         modelIterator = domainModels.iterator();
     }
 
 
     //Pre: we have not iterated through all domain values yet.
     @Override
     public Value getNextDomainValue() {
         
         //1) iterate
         if (firstValueIteration) {
             firstValueIteration = false;
         }
         else if (positionIterator.hasNext()) {
             currentPosition = (Point) positionIterator.next();
         }
         else if (orientationIterator.hasNext()) {
             positionIterator = domainPositions[iteration].iterator();
             currentPosition = (Point) positionIterator.next();
             currentOrientation = (Orientation) orientationIterator.next();
         }
         else if (modelIterator.hasNext()) {
             positionIterator = domainPositions[iteration].iterator();
             currentPosition = (Point) positionIterator.next();
             orientationIterator = orientations.iterator();
             currentOrientation = (Orientation) orientationIterator.next();
             currentModel = (FurnitureModel) modelIterator.next();
         }
         else {
             throw new UnsupportedOperationException("There are no more domain values");
         }
         
         //2) return the new current value
         OrientedRectangle area = new OrientedRectangle(currentPosition,
             currentModel.getSize(), Orientation.S);
         area.setOrientation(currentOrientation);
         
         return new FurnitureValue(area, currentModel);
     }
 
     
     //Pre: the 3 iterators point to valid values
     @Override
     public boolean hasMoreValues() {
         if(domainModels.isEmpty() || domainPositions[iteration].isEmpty() || orientations.isEmpty())
             return false;
         
         return modelIterator.hasNext() || positionIterator.hasNext() || orientationIterator.hasNext();
     }
 
     
     @Override
     public void assignValue(Value value) {
         isAssigned = true;
         assignedValue = value;
         //Duda: no deberia crearse una copia de value? -No veo por qué
     }
 
     
     @Override
     public void undoAssignValue() {
         isAssigned = false;
         assignedValue = null;        
     }
   
     /**
      * Moves positions, models and orientations which are still valid to the
      * next level.
      * All positions from the HashSet at the position "iteration" which are
      * still valid must be moved to the HashSet in the position "iteration"+1.
      * To do this operation, we move all positions preliminarily, and then move
      * back those that are not valid. We estimate this reduces the amount of
      * HashSet operations.
      * All models from the List at the position "iteration" which are still 
      * valid must be moved to the List in the position "iteration"+1.
      */
     //pre: variable has an assigned value.
     //pre: if trimDomain or undoTrimDomain has already been called once,
     //     "iteration" value must be related to the value of "iteration" of the
     //     previous call (+1 if it was a trimDomain or equal if it was a
     //     undoTrimDomain).
     //     otherwise, it must be 0.
     //
     @Override
     public void trimDomain(Variable variable, int iteration) {
         // 0) update internal iteration
         this.iteration = iteration;
        
         // 1) preliminar move of all positions
         domainPositions[iteration+1] = domainPositions[iteration];
         domainPositions[iteration] = new HashSet<Point>();
         
         // 2) send the affected positions back
         FurnitureValue value = (FurnitureValue) variable.getAssignedValue();
         OrientedRectangle area = value.getArea();
         int x = area.x;
         int y = area.y;
         int x_max = x+area.width;
         int y_max = y+area.height;
         for (int i = x; i < x_max; ++i) {
             for (int j = y; j < y_max; ++j) {
                 Point p = new Point(i,j);
                 if (domainPositions[iteration+1].contains(p)) {
                     domainPositions[iteration].add(p);
                     domainPositions[iteration+1].remove(p);
                 }
             }
         }
         
     }
 
     
     /**
      * Merges back values from step "iteration"+1 to "iteration" level.
      * To do this operation, we swap the containers first if the destination
      * level's container has less elements.
      */
     //pre: trimDomain has already been called once.
     //     "iteration" value must be related to the value of "iteration" of the
     //     previous call to trimDomain or undoTrimDomain (equal if it was
     //     trimDomain or -1 if it was undoTrimDomain).
     @Override
     public void undoTrimDomain(Variable variable, Value value, int iteration) {
         // 0) update internal iteration
         this.iteration = iteration;
 
         // 1) check if swap is beneficial
         boolean shouldSwap = domainPositions[iteration].size() <
                              domainPositions[iteration+1].size();
         
         // 2) swap
         if (shouldSwap) {
             HashSet<Point> aux = domainPositions[iteration];
             domainPositions[iteration] = domainPositions[iteration+1];
             domainPositions[iteration+1] = aux;
         }
         
         // 3) merge
         domainPositions[iteration].addAll(domainPositions[iteration+1]);
         domainPositions[iteration+1] = null;
         
     }
 
     
     @Override
     public boolean isAssigned() {
         return isAssigned;
     }
 
     
     @Override
     public Value getAssignedValue() {
         return assignedValue;
     }
 
     public String getID() {
         return identifier;
     }
     
     /**
      * Initializes the orientations list with all available orientations.
      */
     private void defaultOrientations() {
         orientations.add(Orientation.N);
         orientations.add(Orientation.E);
         orientations.add(Orientation.S);
         orientations.add(Orientation.W);
     }	
 	
 
     /**
      * Resets the iterators so that they will iterate through all of the
      * variables' domain, for the iteration "iteration" of the algorithm.
      */
     public void resetIterators(int iteration) {
         
         this.iteration = iteration;
         
         positionIterator = domainPositions[iteration].iterator();
         orientationIterator = orientations.iterator();
         modelIterator = domainModels.iterator();
         
         // 
         if(positionIterator.hasNext() && modelIterator.hasNext() && orientationIterator.hasNext()) {
             currentPosition = (Point) positionIterator.next();
             currentModel = (FurnitureModel) modelIterator.next();
             currentOrientation = (Orientation) orientationIterator.next();
         }
         
         firstValueIteration = true;
     }
     
     
     @Override
     public String toString() {
         StringBuilder result = new StringBuilder();
         String NEW_LINE = System.getProperty("line.separator");
 
         result.append(this.getClass().getName() + ":" + NEW_LINE);
         result.append("Assigned value: ");
         if (isAssigned) result.append(assignedValue.toString() + NEW_LINE);
         else result.append("none" + NEW_LINE);
         
         result.append(" Models available" + NEW_LINE);
         for (FurnitureModel model : domainModels)
                 result.append(model.getName() + " ");
         
         
 //        result.append(" Positions available by iteration" + NEW_LINE);
 //        result.append("iteration    positions" + NEW_LINE);
 //        for (int i = 0; i < domainPositions.length && domainPositions[i] != null; ++i) {
 //            result.append(i + "             [ ");
 //            for (Point point : domainPositions[i]) {
 //                result.append("(" + point.x + "," + point.y + ") ");
 //            }
 //             result.append("]" + NEW_LINE);    
 //        }        
         
         result.append(" Constraints:" + NEW_LINE);
         for (UnaryConstraint constraint : unaryConstraints) {
             result.append(constraint.toString());
         }
 
         return result.toString();
     }
     
       
 }
