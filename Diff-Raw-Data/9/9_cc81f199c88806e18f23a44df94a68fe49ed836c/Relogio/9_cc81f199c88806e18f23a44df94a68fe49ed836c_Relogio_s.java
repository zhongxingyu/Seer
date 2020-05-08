 public class Relogio {
     private static final Relogio relogio = new Relogio();
     private static double tempo;
     private static double tempoTotal;
     
     private Relogio(){
        time = 0;
         tempoTotal = 720;
     }
     
     public static Relogio getRelogio(){
         return relogio;
     }
     
     public void increaseTime(double newTime){
        time += newTime;
     }
     
     public double getTotal(){
 		return tempoTotal;
 	}
     
     public double getTime(){
 		return tempo;
 	}    
}
