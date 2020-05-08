 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package tira.harjoitustyo.ui;
 
 import java.util.InputMismatchException;
 import java.util.Scanner;
 import tira.harjoitustyo.map.MapGenerator;
 import tira.harjoitustyo.map.MazeGenerator;
 import tira.harjoitustyo.map.Renderer;
 import tira.harjoitustyo.solvers.Solver;
 
 /**
  *
  * @author tuure
  */
 public class TextUi implements Ui{
     Scanner sc;
 
     /**
      * TextUi asks the user for a map type and size, generates the map, solves it with the given sovlers and renders the result with the given renderer.
      */
     public TextUi() {
         sc = new Scanner(System.in);
     }
     
     /**
      *
      * @param renderer the renderer to be used for rendering the solved map
      * @param solvers the solvers to be used for solving the map
      */
     @Override
     public void run(Renderer renderer, Solver[] solvers) {
         while(true){
             
             int action = askForNumber("  1 map  |  2 maze  |  3 exit", 1, 3);
            if (action==3) break;
            int size = askForNumber("Please enter the desired width (4-100)", 4, 100);
             
             char[][] map = null;
             
             if (action==1){
                 //generate a map
                 map = MapGenerator.generate(size);
             } else if (action==2){
                 //generate a maze
                 map = MazeGenerator.generate(size);
             }
 
             //go through all the solvers
             for (Solver s:solvers){
                 if (s == null) continue;
 
                 //print the solvers name
                 System.out.println("--------------------");
                 System.out.println(s.getName());
                 System.out.println("--------------------");
                 //calculate the solution
                 char[][] solved = s.solve(map);
                 //render the solution or the map with no route
                 if (s.isSolved()){
                     renderer.render(solved);
                     //print the path length
                     System.out.println("Path length: "+s.getSolutionLength());
                 }else{
                     renderer.render(map);
                     System.out.println("No solution found :(");
                 }
                 //print the amount of nodes checked
                 System.out.println("Nodes checked: "+s.getNodesChecked()+"/"+(map.length*map[0].length));
             }
         }
     }
     
     private int askForNumber(String help){
         return askForNumber(help, Integer.MIN_VALUE, Integer.MIN_VALUE);
     }
     
     private int askForNumber(String help, int min, int max){
         Integer number = null;
         while(number == null || number < min || number > max){
             //help
             System.out.println(help);
             //read user input
             try {
                 number = sc.nextInt();
             } catch (InputMismatchException e) {
                 System.out.print(e.getMessage());
                 sc.next();
             }
             
         }
         return number;
     }
 }
