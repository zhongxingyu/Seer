 public class Probability {
     public static final int CERTAINTY = 1;
     private double chance;
     private static final double IGNORED_DIFFERENCE = 0.00001;
 
     public Probability(double chance) {
 
         this.chance = chance;
     }
 
     public boolean equals(Object other) {
         if (other == null)
             return false;
         if (other == this)
             return true;
         if (!(other instanceof Probability))
             return false;
 
        return Math.abs(this.chance - ((Probability) other).chance) < IGNORED_DIFFERENCE;
     }
 
     public Probability not() {
         return new Probability(CERTAINTY - chance);
     }
 
     public Probability and(Probability probability) {
         return new Probability(this.chance * probability.chance);
     }
 
     public Probability or(Probability probability) {
         return (this.not().and(probability.not())).not();
     }
 }
