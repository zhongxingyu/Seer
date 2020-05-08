 package Moblima;
 
 import java.util.*;
 import java.io.*;
 
 class Cinema {
     private final Integer row;
     private final Integer column;
     private String nameOfCinema;
     private int numOfSeat;
     private String classOfCinema;
     private Seat[][] seat;
     private int numOfEmptySeat;
     private String[] description;
     private BufferedReader br;
     
     /*public Cinema() {
         seat = new Seat[row][column];
         for (int i = 0; i < row; i++) {
             for (int j = 0; j < column; j++) {
                 seat[i][j] = new Seat(i, j);
             }
         }
         theClass = 0;
         }*/
     public Cinema(int row, int column, String nameOfCinema) {
         seat = new Seat[row][column];
         this.setNumOfSeat();
         for (int i = 0; i < row; i++) {
             for (int j = 0; j < column; j++) {
                 seat[i][j] = new Seat(i, j);
             }
         }
         numOfEmptySeat = numOfSeat;
         br = new BufferedReader(new FileReader(nameOfCinema + ".txt"));
         this.setNameOfCinema(nameOfCinema);
         this.setClassOfCinema();
         this.setDescription();
     }
 
     public static void presentSeat(Cinema ci) {
         System.out.print("  ");
         for (int j = 0; j < ci.column; j++)
             System.out.print(" " + j);
         System.out.println();
         for (int j = 0; j < ci.column; j++)
             System.out.print(" _");
         System.out.println();
         for (int i = 0; i < ci.row; i++) {
             System.out.print("\n" + i + "|");
             for (int j = 0; j < ci.column; j++)
                if (seat[i][j].getAssign())
                     System.out.print(" O");
                 else
                     System.out.print(" x");
         }
         System.out.println();
     }
     
     public boolean setNumOfSeat() {
         this.numOfSeat = row * column;
         return true;
     }
     public int getNumOfSeat() {
         return this.numOfSeat;
     }
     
     public int getNumOfEmptySeat() {
         return this.numOfEmptySeat;
     }
     
     public boolean setNameOfCinema(String nameOfCinema) {
         this.nameOfCinema = nameOfCinema;
         return true;
     }
     public String getNameOfCinema() {
         return this.nameOfCinema;
     }
     
     public boolean setClassOfCinema() {
        String cinemaClass = br.nextLine();
         this.classOfCinema = cinemaClass;
         return true;
     }
     public String getClassOfCinema() {
         return this.classOfCinema;
     }
     
     public boolean setDescription() {
         String[] cinemaDescription
                 try {
             StringBuilder sb = new StringBuilder();
             String line = this.br.readLine();
             
             while (line != null) {
                 sb.append(line);
                 sb.append("\n");
                 line = this.br.readLine();
             }
             cinemaDescription = sb.toString().split("\n");
         } finally {
             this.br.close();
         }
         this.description = cinemaDescription;
         return true;
     }
     public String[] getDescription() {
         return this.description;
     }
     
     public boolean assignSeat(int row, int column) {
         if (this.seat[row][column].getAssign()) {
             System.out.println("Seat already assgined to a customer.");
            return;
         }
         this.seat[row][column].assign();
         numOfEmptySeat--;
         System.out.println("Seat Assigned!");
         return true;
     }
     public boolean unAssignSeat(Integer row, Integer column) {
         seat[row][column].unAssign();
         numOfEmptySeat++;
         System.out.println("Seat Unassigned!");
         return true;
     }
 }
