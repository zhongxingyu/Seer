 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package proyecto1;
 
 /**
  *
  * @author NIGHTMARE
  */
 public class Jugadores {
     public static int contador=0;
     public static String jugadorrojo;
     public static String jugadorverde;
     public static int difpiezas=0;
     public static String ganador1 = ("EL Jugador 1: "+Jugadores.jugadorrojo+" HA ¡TRIUNFADO! SE COMIO AL REY Y A "+Jugadores.difpiezas+" Piezas Mas del Jugador 2: "+Jugadores.jugadorverde);
     public static String ganador2 = ("EL Jugador 2: "+Jugadores.jugadorverde+" HA ¡TRIUNFADO! SE COMIO AL REY Y A "+Jugadores.difpiezas+" Piezas Mas del Jugador 1: "+Jugadores.jugadorrojo);
    public String ganadores[][]= new String[10][2];
     
     public Jugadores(String n1,String n2){
         
         contador++;
         jugadorrojo = n1;
         jugadorverde = n2;
         
     }
     
     public void setNombre1(String n){
         jugadorrojo =n;
     }
     
     public void setNombre2(String n){
         jugadorverde=n;
     }
     
     public static int DifPiezas(int turno){
         if(turno == 0){
             difpiezas = (16-Fichas.contarMuertes)-(16-Fichas.contarMuertes2);
                 if(difpiezas > 0){
                     return difpiezas;
                 }else{
                     return difpiezas * -1;
                 }    
         }else{
             difpiezas = (16-Fichas.contarMuertes2)-(16-Fichas.contarMuertes);
                 if(difpiezas > 0){
                     return difpiezas;
                 }else{
                     return difpiezas * -1;
                 }
         }
     }    
     public void ImprimirStats(int turno){
             GameOver.Derrota(turno);
         }
     
     public void getGanadores(int turno, int contador){
         if(turno == 0){
            ganadores[contador][0]= ganador1;
         }else{
            ganadores[contador][1]= ganador2;
         }
       }
     }
         
     
 
 
