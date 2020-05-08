 import java.awt.Image;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Collections;
 
 public abstract class Animal extends Organism {
     protected int age;
     protected double hunger;
     protected int breedingTime;
     
     protected abstract double getMaxHunger();
     protected abstract int getMaxAge();
     protected abstract int getSightDistance();
     protected abstract int getMoveDistance();
     protected abstract ArrayList<String> getPredators();
     protected abstract ArrayList<String> getPrey();
     protected abstract ArrayList<String> getHidingSpots();
 
     public abstract double getCalories();
     public abstract Image getImage();
     public abstract int getMaxBreedingTime();
     public abstract void addMyType(Grid grid, GridSquare square);
     
     public void step(Grid grid){
         if(getLocation() != null) {
             age++;
             hunger+= getCalories()/4;
             if(isOld() || isStarving()) {
                 if(isOld()){
                     System.out.println("Animal at "+getLocation()+" died due to old age");
                 } else {
                     System.out.println("Animal at "+getLocation()+" died due to hunger");
                 }
                 grid.removeAnimal(getLocation());
             } else {
                 if (!breed(grid)){
                     act(grid);
                 }
             }
         }
     }
     //Can override for non-default behavior
     public void act(Grid grid){
         GridSquare mySquare = grid.get(getLocation());
         List<DistanceSquarePair> visibleSquares = grid.getAdjacentSquares(getLocation(), getSightDistance());
         List<DistanceSquarePair> predatorSquares = grid.getOrganismSquares(visibleSquares, getPredators());
         
         if(predatorSquares.size() > 0) {
             GridSquare predatorSquare = predatorSquares.get(0).gridSquare;
             GridSquare moveSquare = grid.getOptimalMoveSquare(getLocation(), predatorSquare.getLocation(), getMoveDistance()*2, false);
             if((moveSquare != null) && 
                (grid.distance(getLocation(), predatorSquare.getLocation()) < grid.distance(moveSquare.getLocation(), predatorSquare.getLocation()))){
                 Debug.echo("Running");
                 move(grid, moveSquare);
                 return;
             } else {
                 //I'm as far as I'm able to be
             }
         }
         
         if (hunger > getMaxHunger()/4) {
             Organism bestAdjacentPrey = bestPreyInDistance(grid, getPrey(), getMoveDistance(), true);
             Organism bestVisiblePrey = bestPreyInDistance(grid, getPrey(), getSightDistance(), true);
             
             if (bestVisiblePrey != null && (bestAdjacentPrey == null || bestVisiblePrey.getCalories() > bestAdjacentPrey.getCalories())) {
                 //Move toward bestVisiblePrey?
                 GridSquare moveSquare = grid.getOptimalMoveSquare(getLocation(), bestVisiblePrey.getLocation(), getMoveDistance()*2, true);
                 if((moveSquare != null) && 
                    (grid.distance(getLocation(), bestVisiblePrey.getLocation()) > grid.distance(moveSquare.getLocation(), bestVisiblePrey.getLocation()))){
                     Debug.echo("Chasing");
                     move(grid, moveSquare);
                     return;
                 } else { 
                     //I'm as close as I'm able to be
                 }
             }
             if(bestAdjacentPrey != null) {
                 eat(bestAdjacentPrey, grid);
                 return;
             } else {
                 //No prey in sight
             }
         }
         
         //No prey in sight or not hungry. Wander?
         List<DistanceSquarePair> emptyReachableSquares = grid.getEmptySquares(getLocation(), getMoveDistance());
         if (emptyReachableSquares.size() > 0) {
             move(grid, emptyReachableSquares.get(Util.randInt(emptyReachableSquares.size())).gridSquare);
         }
     }
     public boolean breed(Grid grid) {
         List<DistanceSquarePair> emptySquares = grid.getEmptySquares(getLocation(), getMoveDistance());
         Collections.shuffle(emptySquares);
         if (breedingTime > 0) breedingTime--;
         if (breedingTime == 0 && hunger <= getMaxHunger()/2) {
             if (emptySquares.size() > 0){
                 Debug.echo("Breeding!");
                 addMyType(grid, emptySquares.get(0).gridSquare);
                 breedingTime = getMaxBreedingTime();
                 return true;
             }
         }
         return false;
     }
 
     public boolean isOld(){
         return age >= getMaxAge();
     }
     public boolean isStarving(){
         return hunger >= getMaxHunger();
     }
     
     private void addHungerFromMovement(int distance){
         hunger += (getCalories()/50)*distance;
     }
     protected void eat(double amount){
         hunger -= amount;
         if(hunger < 0) {
             hunger = 0;
         }
     }
     protected void eat(Organism o, Grid grid){
         if (!((o instanceof Grass) || (o instanceof Carrot))) {
             System.out.print(toString()+" at location "+getLocation()+" is eating "+o+" at location "+o.getLocation()+" ");
         }
         if(o instanceof Plant){
             move(grid, o.getLocation());
             ((Plant)o).getEaten();
             eat(o.getCalories());
         } else {
             eat(o.getCalories());
             Location newLoc = o.getLocation();
             grid.removeAnimal(newLoc);
             move(grid, newLoc);
         }
         if (!((o instanceof Grass) || (o instanceof Carrot))) {
             System.out.print("Now at location "+getLocation()+".");
         }
     }
     protected void move(Grid grid, Location newLocation){
         addHungerFromMovement(grid.distance(getLocation(), newLocation));
         grid.removeAnimal(getLocation());
         grid.addAnimal(this, newLocation);
         setLocation(newLocation);
     }
     protected void move(Grid grid, GridSquare newGridSquare){
         move(grid, newGridSquare.getLocation());
     }
     
     protected Organism bestPreyInDistance(Grid grid, ArrayList<String> prey, int distance, boolean closest){
         GridSquare mySquare = grid.get(getLocation());
 
         Organism bestAdjacentPrey = null;
         
         if (mySquare.getPlant() != null && mySquare.getPlant().isAlive() && prey.contains(mySquare.getPlant().getClass().getName())){
             bestAdjacentPrey = mySquare.getPlant();
         }
     
         List<DistanceSquarePair> reachableSquares = grid.getAdjacentSquares(getLocation(), distance);
         List<DistanceSquarePair> preySquares = grid.getOrganismSquares(reachableSquares, prey);
         List<DistanceSquarePair> emptySquares = grid.getEmptySquares(preySquares);
         
         Organism temp;
         for(DistanceSquarePair pair: preySquares){
             if(emptySquares.contains(pair)){
                 temp = pair.gridSquare.getPlant();
                 if (bestAdjacentPrey == null ||
                     temp.getCalories() > bestAdjacentPrey.getCalories() ||
                     (!closest && temp.getCalories() == bestAdjacentPrey.getCalories())){
 
                     bestAdjacentPrey = temp;
                 }
             } else {
                 temp = pair.gridSquare.getAnimal();
                 if (prey.contains(temp.getClass().getName())) {
                     if (bestAdjacentPrey == null ||
                         temp.getCalories() > bestAdjacentPrey.getCalories() ||
                         (!closest && temp.getCalories() == bestAdjacentPrey.getCalories())){
 
                         bestAdjacentPrey = temp;
                     }
                 } else {
                     //I want to eat the plant, but the square is occupied... Oh well.
                 }
             }
         }
         return bestAdjacentPrey;
     }
     
     protected void init(Location loc) {
         setLocation(loc);
         hunger = getMaxHunger() * 0.75;
         age = 0;
         breedingTime = getMaxBreedingTime();
     }
 
     public boolean isHiding(Grid grid) {
        Plant plant = grid.get(location).getPlant();
        return plant != null && hidingSpots.contains(plant.getClass().getName());
     }
 }
