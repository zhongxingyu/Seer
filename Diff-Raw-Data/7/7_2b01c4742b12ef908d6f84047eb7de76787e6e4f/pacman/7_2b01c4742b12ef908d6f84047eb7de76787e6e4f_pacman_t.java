 package shiny.ninja;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * 
  * @author Computer Science and Engineering Faculty
  * @version 1.0
  * Copyright(C) 2013. All rights reserved.
  * 
  * 
  * Notes: You can write any additional functions/ methods / classes if you want
  * BUT: everything must be in that pacman.java file only!
  * 
  */
 
 public class pacman {
 
   public char[][] maze;
   public String[][] maze_path;
 
   public int row;
   public int column;
   public coordinate pacman_coordinate;
   public coordinate temp_coordinate;
   public coordinate target_coordinate;
   public path path;
  public double T = 0.1;
   public double pp;
   public double k = 3.6;
   
   public pacman(){
     pacman_coordinate = new coordinate();
     temp_coordinate   = new coordinate();
     target_coordinate = new coordinate();
   }
 
   public class coordinate{
     public int x = 0;
     public int y = 0;
     public int g_weight;
     public int h_weight;
     public coordinate next;
     public coordinate parent;
     
     public coordinate(){
       next = null;
       parent = null;
     }   
 
     public coordinate(int x1, int y1){
       x = x1;
       y = y1;
       next = null;
       g_weight = 0;
     }
   
     public coordinate(int x1, int y1, coordinate parent1){
       x = x1;
       y = y1;
       next = null;
       parent = parent1;
       g_weight = 0;
     }
     
     public coordinate(int x1, int y1, coordinate parent1, int n, String searchType){
       x = x1;
       y = y1;
       next = null;
       parent = parent1;
       if(searchType.equals("AStar")){
         g_weight = n;
       }
       else{
         h_weight = n;
       }
     }
   }
   
   public class path{
     public coordinate head;
     public coordinate tail;
     public int count;
     public path(){
       head = null;
       tail = null;
       count = 0;
     }
     public void add_to_path(int x, int y){
       if(head == null){
         head = new coordinate(x, y);
         tail = head;
       }
       else{
         coordinate temp = tail;
         tail = new coordinate(x,y);
         temp.next = tail;
       }
       count++;
     } 
     public void add_to_path(int x, int y, coordinate parent){
       if(head == null){
         head = new coordinate(x, y, parent);
         tail = head;
       }
       else{
         coordinate temp = tail;
         tail = new coordinate(x,y, parent);
         temp.next = tail;
       }
       count++;
     } 
     public void add_to_path(coordinate temp){
       if(head == null){
         head = temp;
         tail = head;
       }
       else{
         tail.next = temp;
         tail = temp;
       }
       count++;
     } 
     public void add_to_path_reverse(int x, int y){
       if(head == null){
         head = new coordinate(x, y);
         tail = head;
       }
       else{
         coordinate temp = head;
         head = new coordinate(x,y);
         head.next = temp;
       }
       count++;
     }
     public boolean contain(int x, int y){
       coordinate temp = head;
       while(temp != null){
         if(temp.x == x && temp.y == y){
           return true;
         }
         else{
           temp = temp.next;
         }
       }
       return false;
     }
     
     public coordinate get_coordinate(int x, int y){
       coordinate temp = head;
       while(temp != null){
         if(temp.x == x && temp.y == y){
           break;
         }
         else{
           temp = temp.next;
         }
       }
       return temp;
     }
     
     public boolean complete(){
       if(head == null){
         return false;
       }
       else{
         return (tail.x == target_coordinate.x && tail.y == target_coordinate.y);
       }
     }
   }
   
   public void readInput() {
     FileReader f = null;
     BufferedReader reader = null;
     try {
       f = new FileReader("maze.txt");
     } catch (FileNotFoundException ex) {
       Logger.getLogger(pacman.class.getName()).log(Level.SEVERE, null, ex);
     }
 
     reader = new BufferedReader(f);
     int m = 0;
     int n = 0;
     
     while(true){
       try {
         String test = reader.readLine();
         if(test == null){
           break;
         }
         if(test.length() > n){
           m = test.length();              // so cot, x
         }
         System.out.println(test);
         n++;                              // so hang, y
       } catch (IOException ex) {
         Logger.getLogger(pacman.class.getName()).log(Level.SEVERE, null, ex);
       }
     }
     
     System.out.println(m);
     System.out.println(n);
 
     maze = new char[m][n];    
     maze_path = new String[m][n];
 
     
     try {
       reader.close();
     } catch (IOException ex) {
       Logger.getLogger(pacman.class.getName()).log(Level.SEVERE, null, ex);
     }
     
     try {
       f.close();
     } catch (IOException ex) {
       Logger.getLogger(pacman.class.getName()).log(Level.SEVERE, null, ex);
     }
     
     try {
       f = new FileReader("maze.txt");
     } catch (FileNotFoundException ex) {
       Logger.getLogger(pacman.class.getName()).log(Level.SEVERE, null, ex);
     }
 
     reader = new BufferedReader(f);
 
     for(int i = n-1; i >= 0; i--){
       String str = null;
       try {
         str = reader.readLine();
       } catch (IOException ex) {
         Logger.getLogger(pacman.class.getName()).log(Level.SEVERE, null, ex);
       }
 
       for(int j = 0; j < m; j++) {
         if(str.charAt(j) == '%' || str.charAt(j) == ' ' || str.charAt(j) == 'P' || str.charAt(j) == '*') {
           maze[j][i] = str.charAt(j);
           maze_path[j][i] = String.valueOf(str.charAt(j));
           System.out.print(maze[j][i]);
         }
         
         if(str.charAt(j) == 'P'){
           pacman_coordinate.x = j;
           pacman_coordinate.y = i;
           temp_coordinate.x = j;
           temp_coordinate.y = i;
         }
         
         if(str.charAt(j) == '*'){
           target_coordinate.x = j;
           target_coordinate.y = i;
         }
       }
       System.out.println();
     }
     
     row = n;
     column = m;
   }
 
   public void generateOutput() {
     ArrayList<Character> arr = null;
     FileWriter f = null;
     BufferedWriter writer = null;
     try {
       f = new FileWriter("path.txt");
       writer = new BufferedWriter(f);
       // myoutput
       // 1
       arr = DFS();
       writer.write(arr.toString().replace(" ", ""), 0, arr.toString().replace(" ", "").length());
       writer.newLine();
       writer.flush();
       
       // 2
       arr = BFS();
       writer.write(arr.toString().replace(" ", ""), 0, arr.toString().replace(" ", "").length());
       writer.newLine();
       writer.flush();
       
       // 3
       arr = BestFS();
       writer.write(arr.toString().replace(" ", ""), 0, arr.toString().replace(" ", "").length());   
       writer.newLine();
       writer.flush();
 
       // 4
       arr = AStar();
       writer.write(arr.toString().replace(" ", ""), 0, arr.toString().replace(" ", "").length());      
       writer.newLine();    
       writer.flush();
 
       // 5
       arr = HillClimbing();
       writer.write(arr.toString().replace(" ", ""), 0, arr.toString().replace(" ", "").length());       
       writer.newLine();
       writer.flush();
       
       // 6
       arr = SteepestHillClimbing();
       writer.write(arr.toString().replace(" ", ""), 0, arr.toString().replace(" ", "").length());       
       writer.newLine();
       writer.flush();
 
       // 7
       arr = SimulatedAnnealing();
       writer.write(arr.toString().replace(" ", ""), 0, arr.toString().replace(" ", "").length());       
       writer.flush();
       
       // newline at the end of file?
       
       writer.close();
       f.close();
     } catch (IOException ex) {
       Logger.getLogger(pacman.class.getName()).log(Level.SEVERE, null, ex);
     }
   }
 
   public boolean adjacent(coordinate slot1, coordinate slot2){
     return (Math.abs(slot1.x - slot2.x) + Math.abs(slot1.y - slot2.y) < 2 && Math.abs(slot1.x - slot2.x) + Math.abs(slot1.y - slot2.y) > 0);
   }
   
   // di tu target len, chi lay 1 duong di den dich, bo qua cac duong cut
   public path gettruepath() {
     path truepath = new path();
     coordinate temp;
     if(path.tail.x == target_coordinate.x  && path.tail.y == target_coordinate.y){
       temp = path.tail;
       while(temp != null){
         truepath.add_to_path_reverse(temp.x, temp.y);
         temp = temp.parent;
       }
     }
     
     return truepath;
   }
   
   public void print_path(path in_path){
     int i = 0;
     coordinate temp = in_path.head;
     String[][] maze_path_temp = new String[column][row];
 
     for(int k = row-1; k >= 0; k--){
       for(int j = 0; j < column; j++) {
         maze_path_temp[j][k] = maze_path[j][k];
       }
     }
     
     while(temp!=null){
       maze_path_temp[temp.x][temp.y] = String.valueOf(i);
       i++;
       temp = temp.next;
     }
     // in ra maze va duong di
     for(int k = row-1; k >= 0; k--){
       for(int j = 0; j < column; j++) {
         switch(maze_path_temp[j][k].length()){
           case 1:
             System.out.print("  " +maze_path_temp[j][k] + " ");
             break;
           case 2:
             System.out.print(" " +maze_path_temp[j][k] + " ");
             break;
           case 3:
             System.out.print(maze_path_temp[j][k] + " ");
             break;
           default:
             break;
         }
       }
       System.out.println();
     }
   }
 
   public boolean notin(ArrayList<coordinate> arr, int x, int y) {
     int n = arr.size();
     for(int i = 0; i < n; i++){
       coordinate temp = arr.get(i);
       if(temp.x == x && temp.y == y){
         return false;
       }
     }
     return true;
   }
   
   public char awayback(char chr){
     switch(chr){
       case 'u':
         return 'd';
       case 'd':
         return 'u';
       case 'l':
         return 'r';
       case 'r':
         return 'l';
       default:
         return 'n';
     }
   }
   
   public int mahattan_distance(coordinate slot1, coordinate slot2){
     return Math.abs(slot1.x - slot2.x) + Math.abs(slot1.y - slot2.y);
   }
   
   public int getbestcoordinate(ArrayList<coordinate> arr){                      // fix here
     int k = 0;
     int d, i;
     int min = -1;
     int n = arr.size();
     if(n>0){
       for(i = 0; i < n; i++){
         d = mahattan_distance(arr.get(i), target_coordinate); 
         if(d < min || min == -1){                                               // khac biet!!!, lay phan tu sau, moi nhat
           min = d;
           k = i;
         }
       }
       return k;
     }
     else{
       return -1;
     }
   }
   
   public int getASTARcoordinate(ArrayList<coordinate> arr) {
     int k = 0;
     int h, i;
     int min = -1;
     int n = arr.size();
     if(n>0){
       for(i = 0; i < n; i++){
         h = mahattan_distance(arr.get(i), target_coordinate) + arr.get(i).g_weight;
         if(h < min || min == -1){                             // khac biet!!!, lay phan tu sau, moi nhat
           min = h;
           k = i;
         }
       }
       return k;
     }
     else{
       return -1;
     }
   }
 
   public coordinate getfrom(ArrayList<coordinate> set, int x, int y) {
     int n = set.size();
     for(int i = 0; i < n; i++){
       if(set.get(i).x == x && set.get(i).y == y){
         return set.get(i);
       }
     }
     return null;
   }
   
   public ArrayList<Character> getdirection(path somepath){
     ArrayList<Character> arr = new ArrayList();
     arr.clear();
     coordinate temp = somepath.head;
     
     if(somepath.complete()){
       while(temp != null && temp.next != null){
         if(adjacent(temp, temp.next)){
           if(temp.y - temp.next.y == 1){
             arr.add('d');
           }
           if(temp.y - temp.next.y == -1){
             arr.add('u');
           }
           if(temp.x - temp.next.x == 1){
             arr.add('l');
           }
           if(temp.x - temp.next.x == -1){
             arr.add('r');
           }
         }
         else{
           arr.add('e');
         }
         temp = temp.next;
       }
     }
     return arr;
   }
   
   //////////////////////////////////////////////////////////////////////////////
   public ArrayList<Character> HillClimbing() {
     temp_coordinate = new coordinate(pacman_coordinate.x, pacman_coordinate.y, null, mahattan_distance(pacman_coordinate, target_coordinate), "HillClimbing");
     path = new path();
     subHillClimbing(null);
     System.out.println("HillClimbing the number of slots travelled: " + path.count);
 
     print_path(path);
     return getdirection(path);
   }
   
   public void subHillClimbing(coordinate parent){
     coordinate temp = new coordinate(temp_coordinate.x, temp_coordinate.y, parent, mahattan_distance(temp_coordinate, target_coordinate), "HillClimbing");
     path.add_to_path(temp);
     // System.out.println("(" + temp.x + ", " + temp.y + ") has heuristic "  + temp.h_weight);
     
     boolean chosen = false;
     
     temp_coordinate.y++;
     if(!chosen && temp_coordinate.y <= row-1 && maze[temp_coordinate.x][temp_coordinate.y] != '%' && !path.contain(temp_coordinate.x, temp_coordinate.y) && !path.complete()){
       if(mahattan_distance(temp_coordinate, target_coordinate) < temp.h_weight){
         chosen = true;
         subHillClimbing(temp);
       }
     }
     temp_coordinate.y--;    
     temp_coordinate.y--;
     if(!chosen && temp_coordinate.y >= 0 && maze[temp_coordinate.x][temp_coordinate.y] != '%' && !path.contain(temp_coordinate.x, temp_coordinate.y) && !path.complete()){
       if(mahattan_distance(temp_coordinate, target_coordinate) < temp.h_weight){
         chosen = true;
         subHillClimbing(temp);
       }
     }
     temp_coordinate.y++;
     temp_coordinate.x--;
     if(!chosen && temp_coordinate.x >= 0 && maze[temp_coordinate.x][temp_coordinate.y] != '%' && !path.contain(temp_coordinate.x, temp_coordinate.y) && !path.complete()){
       if(mahattan_distance(temp_coordinate, target_coordinate) < temp.h_weight){
         chosen = true;
         subHillClimbing(temp);
       }
     }
     temp_coordinate.x++;    
     temp_coordinate.x++;
     if(!chosen && temp_coordinate.x <= column-1 && maze[temp_coordinate.x][temp_coordinate.y] != '%' && !path.contain(temp_coordinate.x, temp_coordinate.y) && !path.complete()){
       if(mahattan_distance(temp_coordinate, target_coordinate) < temp.h_weight){
         subHillClimbing(temp);
       }  
     }
     temp_coordinate.x--;
   }
   
   //////////////////////////////////////////////////////////////////////////////
   public ArrayList<Character> SimulatedAnnealing() {
     temp_coordinate = new coordinate(pacman_coordinate.x, pacman_coordinate.y, null, mahattan_distance(pacman_coordinate, target_coordinate), "HillClimbing");
     path = new path();
     Random rand = new Random(40);
     pp = rand.nextFloat();
     subSimulatedAnnealing(null);
     System.out.println("subSimulatedAnnealing the number of slots travelled: " + path.count);
    
     print_path(path);
     return getdirection(path);
   }
 
   public boolean anneal(coordinate parent){
    double fl = (float)(parent.h_weight - mahattan_distance(temp_coordinate, target_coordinate)) / (T*k);  // phai la so am, parent te hon temp, h lon hon
     System.out.println(fl + "    " + Math.exp(fl) + "   " + pp);
     T-=0.0001;
     if(T <= 0.0001){
       T = 0.0001;
     }
     if(Math.exp(fl) >= pp){
       subSimulatedAnnealing(parent);
       return true;
     }
     else{
       return false;
     }
   }
 
   public void subSimulatedAnnealing(coordinate parent){
     coordinate temp = new coordinate(temp_coordinate.x, temp_coordinate.y, parent, mahattan_distance(temp_coordinate, target_coordinate), "HillClimbing");
     path.add_to_path(temp);
     // System.out.println("(" + temp.x + ", " + temp.y + ") has heuristic "  + temp.h_weight);
     
     boolean chosen = false;
     
     temp_coordinate.y++;
     if(!chosen && temp_coordinate.y <= row-1 && maze[temp_coordinate.x][temp_coordinate.y] != '%' && !path.contain(temp_coordinate.x, temp_coordinate.y) && !path.complete()){
       if(mahattan_distance(temp_coordinate, target_coordinate) < temp.h_weight){
         chosen = true;
         subSimulatedAnnealing(temp);
       }
       else{
         chosen = anneal(temp);
       }
     }
     temp_coordinate.y--;    
     temp_coordinate.y--;
     if(!chosen && temp_coordinate.y >= 0 && maze[temp_coordinate.x][temp_coordinate.y] != '%' && !path.contain(temp_coordinate.x, temp_coordinate.y) && !path.complete()){
       if(mahattan_distance(temp_coordinate, target_coordinate) < temp.h_weight){
         chosen = true;
         subSimulatedAnnealing(temp);
       }
       else{
         chosen = anneal(temp);
       }
     }
     temp_coordinate.y++;
     temp_coordinate.x--;
     if(!chosen && temp_coordinate.x >= 0 && maze[temp_coordinate.x][temp_coordinate.y] != '%' && !path.contain(temp_coordinate.x, temp_coordinate.y) && !path.complete()){
       if(mahattan_distance(temp_coordinate, target_coordinate) < temp.h_weight){
         chosen = true;
         subSimulatedAnnealing(temp);
       }
       else{
         chosen = anneal(temp);
       }
     }
     temp_coordinate.x++;    
     temp_coordinate.x++;
     if(!chosen && temp_coordinate.x <= column-1 && maze[temp_coordinate.x][temp_coordinate.y] != '%' && !path.contain(temp_coordinate.x, temp_coordinate.y) && !path.complete()){
       if(mahattan_distance(temp_coordinate, target_coordinate) < temp.h_weight){
         subSimulatedAnnealing(temp);
       }
       else{
         anneal(temp);
       }
     }
     temp_coordinate.x--;
   }
 
   //////////////////////////////////////////////////////////////////////////////
   public ArrayList<Character> SteepestHillClimbing() {
     temp_coordinate = new coordinate(pacman_coordinate.x, pacman_coordinate.y, null, mahattan_distance(pacman_coordinate, target_coordinate), "HillClimbing");
     path = new path();
     subSteepestHillClimbing(null);
     System.out.println("SteepestHillClimbing the number of slots travelled: " + path.count);
     
     print_path(path);
     return getdirection(path);
   } 
         
   public int steepestnearby(){
     int h_weight = mahattan_distance(temp_coordinate, target_coordinate);
     int result = -1;
 
     temp_coordinate.y++;
     if(temp_coordinate.y <= row-1 && maze[temp_coordinate.x][temp_coordinate.y] != '%' && !path.contain(temp_coordinate.x, temp_coordinate.y) && !path.complete()){
       if(mahattan_distance(temp_coordinate, target_coordinate) < h_weight){
         h_weight = mahattan_distance(temp_coordinate, target_coordinate);
         result = 1;
       }
     }
     temp_coordinate.y--;    
     temp_coordinate.y--;
     if(temp_coordinate.y >= 0 && maze[temp_coordinate.x][temp_coordinate.y] != '%' && !path.contain(temp_coordinate.x, temp_coordinate.y) && !path.complete()){
       if(mahattan_distance(temp_coordinate, target_coordinate) < h_weight){
         h_weight = mahattan_distance(temp_coordinate, target_coordinate);
         result = 2;
       }
     }
     temp_coordinate.y++;
     temp_coordinate.x--;
     if(temp_coordinate.x >= 0 && maze[temp_coordinate.x][temp_coordinate.y] != '%' && !path.contain(temp_coordinate.x, temp_coordinate.y) && !path.complete()){
       if(mahattan_distance(temp_coordinate, target_coordinate) <  h_weight ){
         h_weight = mahattan_distance(temp_coordinate, target_coordinate);
         result = 3;
       }
     }
     temp_coordinate.x++;    
     temp_coordinate.x++;
     if(temp_coordinate.x <= column-1 && maze[temp_coordinate.x][temp_coordinate.y] != '%' && !path.contain(temp_coordinate.x, temp_coordinate.y) && !path.complete()){
       if(mahattan_distance(temp_coordinate, target_coordinate) < h_weight){
         result = 4;
       }  
     }
     temp_coordinate.x--;
 
     return result;
   }
   
   public void subSteepestHillClimbing(coordinate parent){
     coordinate temp = new coordinate(temp_coordinate.x, temp_coordinate.y, parent, mahattan_distance(temp_coordinate, target_coordinate), "HillClimbing");
     path.add_to_path(temp);
     // System.out.println("(" + temp.x + ", " + temp.y + ") has heuristic "  + temp.h_weight);
     
     while(true){
       int i = steepestnearby();
 
       if(i == -1){
         break;
       }
 
       switch(i){
         case 1:
           temp_coordinate.y++;
           subSteepestHillClimbing(temp);
           temp_coordinate.y--;
           break;
         case 2:
           temp_coordinate.y--;
           subSteepestHillClimbing(temp);
           temp_coordinate.y++;
           break;
         case 3:
           temp_coordinate.x--;
           subSteepestHillClimbing(temp);
           temp_coordinate.x++;
           break;
         case 4:
           temp_coordinate.x++;
           subSteepestHillClimbing(temp);
           temp_coordinate.x--;
           break;
         default:
           break;
       }
       if(!path.complete()){
         path.add_to_path(new coordinate(temp.x, temp.y, path.tail, mahattan_distance(temp, target_coordinate), "HillClimbing"));
       }
     }
   }
   
   //////////////////////////////////////////////////////////////////////////////
   public ArrayList<Character> BestFS(){
     temp_coordinate = new coordinate(pacman_coordinate.x, pacman_coordinate.y);
     path = new path();
     coordinate temp;
     ArrayList<coordinate> set = new ArrayList();
     set.add(new coordinate(temp_coordinate.x, temp_coordinate.y));
     
     while(true){
       int i = getbestcoordinate(set);
       temp = set.get(i);
       set.remove(i);
       
       if(temp.y < row-1 && maze[temp.x][temp.y+1] != '%' && !path.contain(temp.x, temp.y+1) && !path.complete()){
         if(notin(set, temp.x, temp.y+1)){
           set.add(new coordinate(temp.x, temp.y + 1, temp));
         }
       }
       if(temp.y > 0 && maze[temp.x][temp.y-1] != '%' && !path.contain(temp.x, temp.y-1) && !path.complete()){
         if(notin(set, temp.x, temp.y-1)){
           set.add(new coordinate(temp.x, temp.y - 1, temp));
         }
       }
       if(temp.x > 0 && maze[temp.x-1][temp.y] != '%' && !path.contain(temp.x-1, temp.y) && !path.complete()){
         if(notin(set, temp.x-1, temp.y)){
           set.add(new coordinate(temp.x - 1, temp.y, temp));
         }
       } 
       if(temp.x < column-1 && maze[temp.x+1][temp.y] != '%' && !path.contain(temp.x+1, temp.y) && !path.complete()){
         if(notin(set, temp.x+1, temp.y)){
           set.add(new coordinate(temp.x + 1, temp.y, temp));
         }
       }
 
       path.add_to_path(temp);
       
       if(set.isEmpty() || path.complete()){
         break;
       }
     }
     System.out.println("BestFS the number of slots travelled: " + path.count);
     
     print_path(path);
     return getdirection(gettruepath());
   }
   
   //////////////////////////////////////////////////////////////////////////////
   public ArrayList<Character> AStar() {
     temp_coordinate = new coordinate(pacman_coordinate.x, pacman_coordinate.y);
     path = new path();
     coordinate temp;
     ArrayList<coordinate> set = new ArrayList();
     set.add(new coordinate(temp_coordinate.x, temp_coordinate.y));
     
     while(true){
       int i = getASTARcoordinate(set);
       temp = set.get(i);
       set.remove(i);
       
       // System.out.println("(" + temp.x + ", " + temp.y + ") has parent "  + "(" + ( temp.parent == null ? "" : temp.parent.x) + ", " + (temp.parent == null ? "" : temp.parent.y) + ")");
       
       // la con, khong thuoc close, khong thuoc open, va van dang tiep tuc giai thuat
       if(temp.y < row-1 && maze[temp.x][temp.y+1] != '%' && !path.contain(temp.x, temp.y+1) && !path.complete()){
         if(notin(set, temp.x, temp.y+1)){
           set.add(new coordinate(temp.x, temp.y + 1, temp, temp.g_weight + 1, "AStar"));
         }
       }
       if(temp.y > 0 && maze[temp.x][temp.y-1] != '%' && !path.contain(temp.x, temp.y-1) && !path.complete()){
         if(notin(set, temp.x, temp.y-1)){
           set.add(new coordinate(temp.x, temp.y - 1, temp, temp.g_weight + 1, "AStar"));
         }
       }
       if(temp.x > 0 && maze[temp.x-1][temp.y] != '%' && !path.contain(temp.x-1, temp.y) && !path.complete()){
         if(notin(set, temp.x-1, temp.y)){
           set.add(new coordinate(temp.x - 1, temp.y, temp, temp.g_weight + 1, "AStar"));
         }
       } 
       if(temp.x < column-1 && maze[temp.x+1][temp.y] != '%' && !path.contain(temp.x+1, temp.y) && !path.complete()){
         if(notin(set, temp.x+1, temp.y)){
           set.add(new coordinate(temp.x + 1, temp.y, temp, temp.g_weight + 1, "AStar"));
         }
       }
 
       path.add_to_path(temp);
       
       if(set.isEmpty() || path.complete()){
         break;
       }
     }
     System.out.println("ASTAR the number of slots travelled: " + path.count);
     
     print_path(path);
     return getdirection(gettruepath());
   }
   
   //////////////////////////////////////////////////////////////////////////////
   public ArrayList<Character> BFS() {//find best solution
     temp_coordinate = new coordinate(pacman_coordinate.x, pacman_coordinate.y);
     path = new path();
     
     ArrayList<coordinate> queue = new ArrayList();
     queue.add(new coordinate(temp_coordinate.x, temp_coordinate.y));
     
     while(true){
       coordinate temp = queue.get(0);
       queue.remove(0);
 
       path.add_to_path(temp);
       
       if(temp.y < row-1 && maze[temp.x][temp.y+1] != '%' && !path.contain(temp.x, temp.y+1) && notin(queue, temp.x, temp.y+1) && !path.complete()){
         queue.add(new coordinate(temp.x, temp.y + 1, temp));
       }
       if(temp.y > 0 && maze[temp.x][temp.y-1] != '%' && !path.contain(temp.x, temp.y-1) && notin(queue, temp.x, temp.y-1) && !path.complete()){
         queue.add(new coordinate(temp.x, temp.y - 1, temp));
       }
       if(temp.x > 0 && maze[temp.x-1][temp.y] != '%' && !path.contain(temp.x-1, temp.y) && notin(queue, temp.x-1, temp.y) && !path.complete()){
         queue.add(new coordinate(temp.x - 1, temp.y, temp));
       } 
       if(temp.x < column-1 && maze[temp.x+1][temp.y] != '%' && !path.contain(temp.x+1, temp.y) && notin(queue, temp.x+1, temp.y) && !path.complete()){
         queue.add(new coordinate(temp.x + 1, temp.y, temp));
       }
       
       if(queue.isEmpty() || path.complete()){
         break;
       }
     }
     System.out.println("BFS the number of slots travelled: " + path.count);
     
     print_path(path);
     return getdirection(gettruepath());
   }
   
   //////////////////////////////////////////////////////////////////////////////
   public ArrayList<Character> DFS() {//like backtracking
     temp_coordinate = new coordinate(pacman_coordinate.x, pacman_coordinate.y);
     path = new path();
     subDFS(null);
     System.out.println("DFS the number of slots travelled: " + path.count);
     print_path(path);
     return getdirection(path);
   }
   
   //////////////////////////////////////////////////////////////////////////////
   public void subDFS(coordinate parent){
     coordinate temp = new coordinate(temp_coordinate.x, temp_coordinate.y, parent);
     path.add_to_path(temp);
     if(temp_coordinate.y < row-1 && maze[temp_coordinate.x][temp_coordinate.y+1] != '%' && !path.contain(temp_coordinate.x, temp_coordinate.y+1) && !path.complete()){
       temp_coordinate.y++;
       subDFS(temp);
       temp_coordinate.y--;
       if(!path.complete()){
         path.add_to_path(temp.x, temp.y, path.tail);
       }
     }
     if(temp_coordinate.y > 0 && maze[temp_coordinate.x][temp_coordinate.y-1] != '%' && !path.contain(temp_coordinate.x, temp_coordinate.y-1) && !path.complete()){
       temp_coordinate.y--;
       subDFS(temp);
       temp_coordinate.y++;
       if(!path.complete()){
         path.add_to_path(temp.x, temp.y, path.tail);
       }
     }
     if(temp_coordinate.x > 0 && maze[temp_coordinate.x-1][temp_coordinate.y] != '%' && !path.contain(temp_coordinate.x-1, temp_coordinate.y) && !path.complete()){
       temp_coordinate.x--;
       subDFS(temp);
       temp_coordinate.x++;
       if(!path.complete()){
         path.add_to_path(temp.x, temp.y, path.tail);
       }
     }
     if(temp_coordinate.x < column-1 && maze[temp_coordinate.x+1][temp_coordinate.y] != '%' && !path.contain(temp_coordinate.x+1, temp_coordinate.y) && !path.complete()){
       temp_coordinate.x++;
       subDFS(temp);     
       temp_coordinate.x--;
       if(!path.complete()){
         path.add_to_path(temp.x, temp.y, path.tail);
       }
     }
   }
 
   //////////////////////////////////////////////////////////////////////////////
   public static void main(String[] args) {
     /**
      * Modify this method if you want
      */
     pacman pacman = new pacman();
     pacman.readInput();
     
     pacman.generateOutput();
       
   }
 }
