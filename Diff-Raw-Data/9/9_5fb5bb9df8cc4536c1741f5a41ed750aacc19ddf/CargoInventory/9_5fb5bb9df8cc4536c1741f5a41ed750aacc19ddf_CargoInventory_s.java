 package solution;
 
 import java.util.ArrayList;
 
 /**
  * @author Jacob Evans, Bodaniel Jeanes
  */
 public class CargoInventory {
     int                                  numStacks, maxHeight, maxContainers,
             currentContainers;
     ArrayList<ArrayList<ContainerLabel>> storage = new ArrayList<ArrayList<ContainerLabel>>();
 
     public CargoInventory(Integer numStacks, Integer maxHeight,
             Integer maxContainers) throws CargoException,
             IllegalArgumentException {
 
         // No null parameters supported
         if ((numStacks == null) || (maxHeight == null)
                 || (maxContainers == null)) {
             throw new IllegalArgumentException(
                     "Cannot have null values for any parameter");
         }
 
         // No negative numbers supported
         if ((numStacks < 0) || (maxHeight < 0) || (maxContainers < 0)) {
             throw new CargoException("Cannot have negative values");
         }
 
         this.numStacks = numStacks;
         this.maxHeight = maxHeight;
         this.maxContainers = maxContainers;
         currentContainers = 0;
 
         for (int i = 0; i < numStacks; i++) {
             storage.add(new ArrayList<ContainerLabel>());
         }
     }
 
     public boolean isAccessible(ContainerLabel queryContainer)
             throws IllegalArgumentException {
         ContainerLabel topContainer;
         // may get a null ?
         if (queryContainer == null) {
             throw new IllegalArgumentException();
         }
 
         try {
             final ArrayList<ContainerLabel> stack = storage.get(queryContainer
                     .getKind());
             topContainer = stack.get(stack.size() - 1);
 
             if (topContainer.matches(queryContainer)) {
                 return true;
             }
         } catch (final IndexOutOfBoundsException e) {}
 
         return false;
     }
 
     public boolean isOnboard(ContainerLabel queryContainer)
             throws IllegalArgumentException {
         if (queryContainer != null) {
             // go through the array check if we have the item in the list
             for (ArrayList<ContainerLabel> i : storage) {
                 if (i.contains(queryContainer)) {
                     return true;
                 }
             }
 
             for (ArrayList<ContainerLabel> i : storage) {
                 for (ContainerLabel containerLabel : i) {
                     if (queryContainer.matches(containerLabel)) {
                         return true;
                     }
                 }
             }
             return false;
         } else {
             throw new IllegalArgumentException();
         }
     }
 
     public void loadContainer(ContainerLabel newContainer)
             throws CargoException, IllegalArgumentException {
         if (newContainer == null) {
             throw new IllegalArgumentException("You can not load null");
         }
 
         if (newContainer.getKind() >= numStacks) {
             throw new CargoException(
                     "Container kind can not be stacked on this ship");
         }
 
         if (isOnboard(newContainer)) {
             throw new CargoException("Container is already on board");
         }
 
         if (currentContainers >= maxContainers) {
             throw new CargoException(
                     "The ship is full and can not hold anymore containers");
         }
 
         if (storage.get(newContainer.getKind()).size() >= maxHeight) {
             throw new CargoException(
                     "The stack for this container kind is already at the maximum height");
         }
 
         storage.get(newContainer.getKind()).add(newContainer);
         currentContainers++;
     }
 
     public ContainerLabel[] toArray(Integer kind) throws CargoException,
             IllegalArgumentException {
         if (kind == null) {
             throw new IllegalArgumentException();
         }
 
         if ((kind < 0) || (kind >= numStacks)) {
             throw new CargoException("No such stack on this ship");
         }
 
         final ArrayList<ContainerLabel> stack = storage.get(kind);
         final ContainerLabel[] returnArray = new ContainerLabel[stack.size()];
 
         if (kind < numStacks) {
             return stack.toArray(returnArray);
         } else {
             throw new CargoException("Kind it out of our range");
         }
     }
 
     public void unloadContainer(ContainerLabel oldContainer)
             throws CargoException, IllegalArgumentException {
         // check if the cargo is at the top of the stack
         if (oldContainer == null) {
             throw new IllegalArgumentException();
         }
 
         if (isOnboard(oldContainer)) {
             if (isAccessible(oldContainer)) {
                // remove the container
                storage.get(oldContainer.getKind()).remove(oldContainer);
                 currentContainers--;
             } else {
                 throw new CargoException("Cannot access the container");
             }
         } else {
             throw new CargoException("Container is not on the ship");
         }
     }
 }
