 package interiores.business.models.backtracking;
 
 import interiores.business.models.Orientation;
 import interiores.business.models.backtracking.area.Area;
 import interiores.business.models.room.FurnitureModel;
 import interiores.shared.backtracking.Value;
 import interiores.utils.Dimension;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * A Stage represents a subset of the domain with the values that were
  * available until a given iteration.
  * 
  * * A domain is composed by:
  *  - An area representing the valid positions
  *  - A list of valid orientations
  *  - A list of valid furniture models
  * 
  * Each of these elements is iterable on its own. That is, it must implement java's
  * iterator interface.
  * 
  * With these properties in mind, StageAlt implements its own operations to iterate
  * through an ordered collection of valid domain values.
  * Iteration is done in this order: 1) Position, 2) Orientation, 3) Models
  * 
  * @author larribas
  * @author nil.mamano
  */
 public class Stage {
     
     private Area positions; // The set of valid positions
     private Area iterablePositions; // The set of points through which we
         // iterate. It is a more restricted version than positions as it takes
         // into account the properties of the model and orientation we are
         // iterating through at a given point
     private Iterator<Point> positionIterator; // Iterator through the valid positions
     private Point currentPosition; // The current position in the iteration process
     
     private HashSet<Orientation> orientations; // The set of valid orientations
     private List<Orientation> iterableOrientations; // The list of orientations through
         //which we will iterate
     private Iterator<Orientation> orientationIterator; // Iterator through the valid orientations
     private Orientation currentOrientation; // The curent orientation
     
     private HashSet<FurnitureModel> models; // The set of valid models
     private List<FurnitureModel> iterableModels; // The list of models through
         //which we will iteate
     private Iterator<FurnitureModel> modelIterator; // Iterator through the valid models
     private FurnitureModel currentModel; // The current model under consideration
     
     private FurnitureValue nextValue; // The computed nextValue
     
     /**
      * Builds a Stage with all positions, a list of models and the 4 default orientations.
      * This constructor is used for the stage correspondent to the first iteration.
      */
     public Stage(HashSet<FurnitureModel> models, Dimension roomSize) {
            
         // initialize models
         this.models = models;
         
         // initialize positions
         positions = new Area(new Rectangle(0, 0, roomSize.width, roomSize.depth));
         
         //initialize orientations
         orientations = defaultOrientations();
     }
     
     /**
      * Default constructor. Empty stage.
      * This constructor is used for all stages except thet first one.
      */
     public Stage() {        
         models = new HashSet();
         positions = new Area();
         orientations = new HashSet();      
     }
     
     /**
      * Returns a Furniture Value with the next value of the domain of this variable
      * @return Furniture Value with the next value of the domain of this variable
      */
     public Value getNextDomainValue() {
         return nextValue;
     }
     
     private boolean computeNextDomainValue() {
         // If there is a next position, we simply advance to it and return
         if (positionIterator.hasNext()) currentPosition = positionIterator.next();            
         else {
             // If, however, there are not more positions, we have to change the orientation
             // or the model under cosideration. Thus, we have two alternatives:
             //  1) The next model-orientation combination has the same dimensions as the previous,
             //     And therefore we don't need to build a new Area
             //  2) The dimensions do change, and therefore we need to build a new Area
             boolean hasSameSize = true;
             
             // This piece of code is difficult to comment. Say, it has undercome a number of optimizations
             // in order to minimize the number of lines. I'll try, though:
             //     A) If there is a new orientation, we store the previous orientation and iterate to the next one
             //        If it has the same size (which is true if we haven't changed the model), and the orientation
             //        doesn't make a difference (it doesn't if we switch from N to S, or from W to E; it does otherwise)
             //        we don't need to rebuild the area, and can reset the positions iterator and call this very
             //        function recursively (this allows us to reuse the position iteration code).
             //        In any other case, we need to rebuild the position and continue.
             //     B) If it is the model that need to change, we get the next model and see if it has the same size as
             //        the previous one. This allows us to know if we have to rebuild the positions.
             //        Also, we reset the orientation iterator and continue.
             //
             //    Note: This code is tested and works correctly. I would recommend touching it the least when possible
             if (orientationIterator.hasNext());
             else {
                 if (modelIterator.hasNext()) {
                     Dimension prev_d = currentModel.getSize();
                     currentModel = modelIterator.next();
                     if (prev_d.equals(currentModel.getSize()))
                         Collections.sort(iterableOrientations, Collections.reverseOrder(new oComparator()));
                     else hasSameSize = false;
                    
                     orientationIterator = orientations.iterator();
                 }
                 else
                     return false; // NO MORE DOMAIN VALUES
             }
             int prev_o = currentOrientation.ordinal();
             currentOrientation = orientationIterator.next();
             
             if (hasSameSize && prev_o + currentOrientation.ordinal() % 2 == 0) {
                 buildIterablePositions(); // build new Extended Area
             }
             
             positionIterator = iterablePositions.iterator();
             
             return computeNextDomainValue();
         }
         
         nextValue = new FurnitureValue(currentPosition, currentModel, currentOrientation);
         
         return true;
     }
     
     /**
      * Finds out whether there are more values left in the domain of the variable at this stage of the algorithm
      * @return 'true' if there are more positions. 'false' otherwise
      */
     public boolean hasMoreValues() {
         if(iterableModels.isEmpty() || iterableOrientations.isEmpty())
             return false;
         
         if(! (modelIterator.hasNext() || orientationIterator.hasNext() || positionIterator.hasNext()))
             return false;
         
         return computeNextDomainValue();
     }
     
     
     /**
      * Initializes all the iterators. It should be called from the constructor, when building StageAlt
      */
     public void initializeIterators() {
         
         iterableModels = new ArrayList<FurnitureModel>();
         iterableOrientations = new ArrayList<Orientation>();
         //we put them in lists so we can sort them
         iterableModels.addAll(models);
         iterableOrientations.addAll(orientations);
         
         // Sorting of models and orientations is performed in order to
         // minimize the number of times we have to build ExtendedAreas
         sortOrientations();
         sortModels();
         
         modelIterator = iterableModels.iterator();
         orientationIterator = iterableOrientations.iterator();
         
         if (modelIterator.hasNext() && orientationIterator.hasNext()) {
             currentModel = modelIterator.next();
             currentOrientation = orientationIterator.next();
             buildIterablePositions();
             positionIterator = iterablePositions.iterator();
         }        
     }
 
     /**
      * Returns the dimension of the model if we rotate it from its starting orientation.
      * It is intended to give the valid dimension of a model when the orientation is W or E
      * @param m The model we want get rotated
      * @return A dimension representing the one of the model when rotated to a W or E orientation
      */
     private Dimension rotateModel(FurnitureModel m) {
         return new Dimension(m.getSize().depth,m.getSize().width);
     }
 
     /**
      * Takes into account the current model and orientation, and builds the
      * iterable positions.
      * 
      * Area intersections optimization: this method performs a double
      * intersection process whose outcome is the area resulting from the intersection of:
      *   - The base area (the one we have built the class with)
      *   - The base area translated 'h_offset' units to the left
      *   - The base area translated 'v_offset' units to the top
      * This process eliminates many invalid positions the variable could take on.
      */
     private void buildIterablePositions() {
         Dimension d = (currentOrientation.ordinal() % 2 == 0)?
                 currentModel.getSize() : rotateModel(currentModel);
         iterablePositions = new Area(positions);
         
         //Area intersections optimization
         Area shiftedLeft = new Area(positions);
        shiftedLeft.shift(d.width, Orientation.W);
         Area shiftedUp = new Area(positions);
        shiftedUp.shift(d.depth, Orientation.N);
         iterablePositions.intersection(shiftedLeft);
         iterablePositions.intersection(shiftedUp);
     }
     
     /**
      * Sorts the orientations in a certain order such that when iterating them we have to perform
      * the minimum amount of changes. For example, N,S,W,E, so that the dimension of the furniture
      * rotates once (between S and W) instead of 3.
      */
     private void sortOrientations() {
         Collections.sort(iterableOrientations, new oComparator());
     }
     
     /**
      * Sorts the models in ascending order of sizes (first width, then depth)
      */
     private void sortModels() {
         Collections.sort(iterableModels, new mComparator());
     }
     
     
     /**
      * Comparator of orientations that gives the right order
      */
     private static class oComparator implements Comparator<Orientation> {
 
         @Override
         public int compare(Orientation o1, Orientation o2) {
             return (o1.ordinal() % 2 == 0)? 1 : (o2.ordinal() % 2 != 0)? 0 : -1;
         }
     }
         
     /**
      * Comparator of models in ascending order
      */
     private static class mComparator implements Comparator<FurnitureModel> {
 
         @Override
         public int compare(FurnitureModel m1, FurnitureModel m2) {
             Dimension d1 = m1.getSize(); Dimension d2 = m2.getSize();
             
             if ( d1.width < d2.width ) return -1;
             else if ( d1.width > d2.width ) return 1;
             else if ( d1.depth < d2.depth ) return -1;
             else if ( d1.depth > d2.depth ) return 1;
             else return 0;
         }
     }
     
     /**
      * Returns a list with all orientations.
      */
     private HashSet<Orientation> defaultOrientations() {
         HashSet<Orientation> allOrientations = new HashSet<Orientation>();
         allOrientations.add(Orientation.E);
         allOrientations.add(Orientation.W);
         allOrientations.add(Orientation.S);
         allOrientations.add(Orientation.N);
         return allOrientations;
     }
 
     HashSet<FurnitureModel> getModels() {
         return models;
     }
     
     Area getPositions() {
         return positions;
     }
     
     HashSet<Orientation> getOrientations() {
         return orientations;
     }
 
     int size() {
         int modelCount = models.size();
         int oriCount = orientations.size();
         int areaSize = positions.areaSize();
         return modelCount * oriCount * areaSize;
     }
     
     //SET OPERATION: THE DEFINITIVE ONES
     
         void swapPositions(Stage stage) {
         Area aux = this.positions;
         this.positions = stage.positions;
         stage.positions = aux;
         
 //        Iterator it = positionIterator;
 //        positionIterator = stage.positionIterator;
 //        stage.positionIterator = it;
     }
 
     void swapModels(Stage stage) {
         HashSet<FurnitureModel> aux = this.models;
         this.models = stage.models;
         stage.models = aux;
         
 //        Iterator it = modelIterator;
 //        modelIterator = stage.modelIterator;
 //        stage.modelIterator = it;
     }
 
     void swapOrientations(Stage stage) {
         HashSet<Orientation> aux = this.orientations;
         this.orientations = stage.orientations;
         stage.orientations = aux;
         
 //        Iterator it = orientationIterator;
 //        orientationIterator = stage.orientationIterator;
 //        stage.orientationIterator = it;
     }
     
     
     /**
      * Makes the intersection of positions and returns the positions not
      * contained in the intersection.
      * @param validPositions
      * @return 
      */
     Area intersectionP(Area area) {
         Area startingPositions = new Area(positions);
         positions.intersection(area);
         startingPositions.difference(positions);
         return startingPositions;
     }
 
     /**
      * Makes the intersection of models and returns the models not
      * contained in the intersection.
      * @param validPositions
      * @return 
      */
     HashSet<FurnitureModel> intersectionM(HashSet<FurnitureModel> validModels) {
         HashSet<FurnitureModel> notContainedModels = new HashSet<FurnitureModel>();
         for (FurnitureModel model : models) {
             if (! validModels.contains(model)) {
                 notContainedModels.add(model);
                 models.remove(model);
             }
         }
         return notContainedModels;
     }
     
     /**
      * Makes the intersection of orientations and returns the orientations not
      * contained in the intersection.
      * @param validPositions
      * @return 
      */
     HashSet<Orientation> intersectionO(HashSet<Orientation> validOrientations) {
         HashSet<Orientation> notContainedOrientations = new HashSet<Orientation>();
         for (Orientation orientation : orientations) {
             if (! validOrientations.contains(orientation)) {
                 notContainedOrientations.add(orientation);
                 orientations.remove(orientation);
             }
         }
         return notContainedOrientations;
     }
         
     void unionP(Area area) {
         positions.union(area);
     }
     
     void unionM(HashSet<FurnitureModel> newModels) {
         //we know that there aren't repeated models because each model
         //is found only one in each domain
         models.addAll(newModels);
     }
     
     void unionO(HashSet<Orientation> newOrientations) {
         //we know that there aren't repeated orienttations because each
         //orientation is found only one in each domain
         orientations.addAll(newOrientations);        
     }
     
     void union(Stage stage) {
         unionP(stage.positions);
         unionM(stage.models);
         unionO(stage.orientations);
     }
 
     Area difference(Area area) {
         Area startingPositions = new Area(positions);
         positions.difference(area);
         startingPositions.difference(positions);
         return startingPositions;
     }
     
 
     void eliminateExceptM(HashSet<FurnitureModel> validModels) {
         Iterator<FurnitureModel> it = models.iterator();
         while(it.hasNext())
             if (! validModels.contains(it.next()))
                 it.remove();
     }
 
     void eliminateExceptO(HashSet<Orientation> validOrientations) {
         Iterator<Orientation> it = orientations.iterator();
         while(it.hasNext())
             if (! validOrientations.contains(it.next()))
                 it.remove();
     }
     
     void eliminateExceptP(Area validPositions) {
         positions.intersection(validPositions);
     }
 
 }
