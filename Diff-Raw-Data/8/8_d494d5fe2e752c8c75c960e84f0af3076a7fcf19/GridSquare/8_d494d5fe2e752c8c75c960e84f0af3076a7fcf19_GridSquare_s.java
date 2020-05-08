 public class GridSquare {
     private Plant plant;
     private Animal animal;
 
     public GridSquare() {
         Debug.echo("Making a GridSquare");
     }
 
     public GridSquare(Plant p, Animal a) {
         Debug.echo("Making a GridSquare with arguments");
         plant  = p;
         animal = a;
     }
     
     public void step(Location loc, Grid g){
        plant.step(loc, g);
        animal.step(loc, g);
     }
 
     public Plant getPlant()   { return plant;  }
     public Animal getAnimal() { return animal; }
 
     public void setPlant(Plant p)   { plant = p;  }
     public void setAnimal(Animal a) { animal = a; }
 }
