 import queens.base.*;
 
 public class Main 
 {
     public static void main(String[] args)
     {
         //Die Aufgabe ist es, das IQueensSolver-Interface zu implementieren
         //ein leeres Gerüst findet sich in MyQueensSolver
         //Controler startet lediglich das eigentliche Programm, welches den übergebenen
         //IQueensSolver benutzt, um Lösungen für verschiedene Damen-Anzahlen zu erzeugen
         //und Statistiken zu erstellen.
         IQueensSolver queen = new MyQueensSolver();
		queen.getAvgStepsPerSuccessfulSolution(8, true, 50000);
     }
 }
