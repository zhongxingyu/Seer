 import java.awt.Image;
 import java.awt.Color;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Collections;
 
 public abstract class Animal extends Organism {
     protected int age;
     protected double hunger;
     protected int breedingTime;
     private Color color;
 
     protected abstract double getMaxHunger();
     protected abstract int getMaxAge();
     protected abstract int getSightDistance();
     protected abstract int getMoveDistance();
     protected abstract ArrayList<String> getPredators();
     protected abstract ArrayList<String> getPrey();
     protected abstract ArrayList<String> getHidingSpots();
 
     public abstract double getCalories();
     public abstract Image getImage();
 
     public int getFleeDistance(){
         return getMoveDistance()*2;
     }
     protected double getHungerIncrement(){
         return getCalories()/4;
     }
     
     public void toggleFocus() {
         if (color == null) {
             color = Util.nextColor();
         }
         else {
             color = null;
         }
     }
     public Color getColor() {
         return color;
     }
 
     public abstract int getMaxBreedingTime();
     public abstract void addMyType(Grid grid, GridSquare square);
 
     public void step(Grid grid) {
         if(getLocation() != null) {
             hunger += getHungerIncrement();
             if (isOld() || isStarving()) {
                 if (isOld()) {
                     Debug.echo("Animal at "+getLocation()+" died due to old age");
                     Stats.recordDeath(this, Stats.OLD_AGE);
                 } else {
                     Debug.echo("Animal at "+getLocation()+" died due to starvation");
                     Stats.recordDeath(this, Stats.STARVATION);
                 }
                 grid.removeAnimal(getLocation());
             } else {
                 age++;
                 if (!breed(grid)) {
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
 
         //Check for Predators
         if(predatorSquares.size() > 0) {
             GridSquare predatorSquare = predatorSquares.get(0).gridSquare;
             
             //Hide
             if(hungerPercent() <= 75) {
                 if(isHiding(grid)) return;
                 GridSquare hidingSquare = grid.getOptimalHidingSquare(getLocation(), predatorSquare.getLocation(), getSightDistance(), getHidingSpots());
                 if (hidingSquare != null) {
                     if (grid.distance(getLocation(), hidingSquare.getLocation()) <= getFleeDistance()) {
                         Debug.echo("Hiding");
                         move(grid, hidingSquare);
                         return;
                     } else {
                         GridSquare moveSquare = grid.getOptimalChaseSquare(getLocation(), hidingSquare.getLocation(), getMoveDistance());
                         if (moveSquare != null) {
                             Debug.echo("Running towards Hiding Spot");
                             move(grid, moveSquare);
                             return;
                         }
                     }
                 }
             }
             
             //Run Away
             GridSquare moveSquare = grid.getOptimalFleeSquare(getLocation(), predatorSquare.getLocation(), getFleeDistance());
             if((moveSquare != null) &&
                (grid.distance(getLocation(), predatorSquare.getLocation()) < grid.distance(moveSquare.getLocation(), predatorSquare.getLocation()))){
                 Debug.echo("Running");
                 move(grid, moveSquare);
                 return;
             } else {
                 //I'm as far as I'm able to be
             }
         }
         
         //Check for Prey
         if (hungerPercent() > 25) {
             Organism bestAdjacentPrey = bestPreyInDistance(grid, getMoveDistance(), true);
             Organism bestVisiblePrey = bestPreyInDistance(grid, getSightDistance(), true);
             
             if(bestVisiblePrey != null && sustainsMe(bestVisiblePrey)){
                //If bestVisiblePrey is better than adjacentPrey
                 if (bestAdjacentPrey == null || 
                    !(hungerPercent() > 75 && sustainsMe(bestAdjacentPrey)) ||
                     bestVisiblePrey.getCalories() > bestAdjacentPrey.getCalories()) {
                     //Move toward bestVisiblePrey?
                     GridSquare moveSquare = grid.getOptimalChaseSquare(getLocation(), bestVisiblePrey.getLocation(), getFleeDistance());
                     if((moveSquare != null) &&
                        (grid.distance(getLocation(), bestVisiblePrey.getLocation()) > grid.distance(moveSquare.getLocation(), bestVisiblePrey.getLocation()))){
                         Debug.echo("Chasing");
                         move(grid, moveSquare);
                         return;
                     } else {
                         //I'm as close as I'm able to be
                     }
                 }
                 //If bestAdjacentPrey exists (which means it is == bestVisiblePrey)
                 if(bestAdjacentPrey != null) {
                     eat(bestAdjacentPrey, grid);
                     return;
                 }
             }
             
             //Eat something, as far away (but still adjacent) as possible. (So I can see more land)
             
             Organism wanderOrganism = bestPreyInDistance(grid, getMoveDistance(), false);
             if (wanderOrganism != null) {
                 eat(wanderOrganism, grid);
                 return;
             }
         }
 
         //No prey in sight or not hungry. Wander!
         
         List<DistanceSquarePair> emptyReachableSquares = grid.getEmptySquares(getLocation(), getMoveDistance());
         if (emptyReachableSquares.size() > 0) {
             move(grid, emptyReachableSquares.get(Util.randInt(emptyReachableSquares.size())).gridSquare);
         }
     }
     protected boolean sustainsMe(Organism o){
         return o.getCalories() >= getHungerIncrement();
     }
     public boolean breed(Grid grid) {
         List<DistanceSquarePair> emptySquares = grid.getEmptySquares(getLocation(), getMoveDistance());
         Collections.shuffle(emptySquares);
         if (breedingTime > 0) breedingTime--;
         if (breedingTime == 0 && hungerPercent() <= 50) {
             if (emptySquares.size() > 0){
                 Debug.echo("Breeding!");
                 addMyType(grid, emptySquares.get(0).gridSquare);
                 Stats.increaseCount(this);
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
     private double hungerPercent(){
         return (hunger / getMaxHunger())*100;
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
         if (!(o instanceof Plant)) {
             Debug.print(toString()+" at location "+getLocation()+" is eating "+o+" at location "+o.getLocation()+" ");
         }
         if(o instanceof Plant){
             move(grid, o.getLocation());
             ((Plant)o).getEaten();
             eat(o.getCalories());
         } else {
             eat(o.getCalories());
             Location newLoc = o.getLocation();
             grid.removeAnimal(newLoc);
             Stats.recordDeath(o, Stats.EATEN);
             move(grid, newLoc);
         }
         if (!(o instanceof Plant)) {
             Debug.print("Now at location "+getLocation()+".\n");
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
 
     protected Organism bestPreyInDistance(Grid grid, int distance, boolean closest){
         ArrayList<String> prey = getPrey();
         GridSquare mySquare = grid.get(getLocation());
 
         Organism bestAdjacentPrey = null;
         int bestDist;
         
         if (closest) bestDist = Integer.MAX_VALUE;
         else bestDist = -1;
 
         if (mySquare.getPlant() != null && mySquare.getPlant().isAlive() && prey.contains(mySquare.getPlant().getClass().getName())){
             bestAdjacentPrey = mySquare.getPlant();
             bestDist = 0;
         }
 
         List<DistanceSquarePair> reachableSquares = grid.getAdjacentSquares(getLocation(), distance);
         List<DistanceSquarePair> preySquares = grid.getOrganismSquares(reachableSquares, prey);
         List<DistanceSquarePair> emptySquares = grid.getEmptySquares(preySquares);
 
         Organism temp;
         Collections.shuffle(preySquares);
         for(DistanceSquarePair pair: preySquares){
             if(emptySquares.contains(pair)){
                 temp = pair.gridSquare.getPlant();
                 if ((bestAdjacentPrey == null ||
                     temp.getCalories() > bestAdjacentPrey.getCalories()) ||
                     ((temp.getCalories() == bestAdjacentPrey.getCalories()) && 
                      ((closest && pair.distance < bestDist) || (!closest && pair.distance > bestDist)))) {
                     
                     bestAdjacentPrey = temp;
                 }
             } else {
                 temp = pair.gridSquare.getAnimal();
                 if (prey.contains(temp.getClass().getName())) {
                     if ((bestAdjacentPrey == null ||
                         temp.getCalories() > bestAdjacentPrey.getCalories()) ||
                         ((temp.getCalories() == bestAdjacentPrey.getCalories()) && 
                          ((closest && pair.distance < bestDist) || (!closest && pair.distance > bestDist)))){
 
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
         hunger = getMaxHunger() * 0.50;
         age = 0;
         breedingTime = getMaxBreedingTime();
     }
 
     public boolean isHiding(Grid grid) {
         Plant plant = grid.get(getLocation()).getPlant();
         return plant != null && getHidingSpots().contains(plant.getClass().getName());
     }
 
     public Integer getAge() { return age; }
 }
