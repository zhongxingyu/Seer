 package checkerssolitaire; 
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 /*
  * Provides several preset initial configurations
  * for the RushHour puzzle. All are at Expert level.
  */
 
 public class Configurations {
 	
 	public static ArrayList theConfigs = new ArrayList();
 		
 	public static void addAll() {
 	
 		
 	   LinkedList set1 = new LinkedList(); //cross
 	   set1.add(new CheckerNode(3, 1, 'C'));
 	   set1.add(new CheckerNode(3, 2, 'C'));
 	   set1.add(new CheckerNode(3, 3, 'C'));
 	   set1.add(new CheckerNode(3, 4, 'C'));
 	   set1.add(new CheckerNode(2, 2, 'C'));
 	   set1.add(new CheckerNode(4, 2, 'C'));
 	   theConfigs.add(set1);
 	   
 	   LinkedList set2 = new LinkedList(); //plus
 	   set2.add(new CheckerNode(3, 1, 'C'));
 	   set2.add(new CheckerNode(3, 2, 'C'));
 	   set2.add(new CheckerNode(3, 3, 'C'));
 	   set2.add(new CheckerNode(3, 4, 'C'));
 	   set2.add(new CheckerNode(3, 5, 'C'));
 	   set2.add(new CheckerNode(1, 3, 'C'));
 	   set2.add(new CheckerNode(2, 3, 'C'));
 	   set2.add(new CheckerNode(3, 3, 'C'));
 	   set2.add(new CheckerNode(4, 3, 'C'));
 	   set2.add(new CheckerNode(5, 3, 'C'));
 	   theConfigs.add(set2);
 	   
 	   LinkedList set3 = new LinkedList();//fireplace
 	   set3.add(new CheckerNode(2,0,'C'));
 	   set3.add(new CheckerNode(3,0,'C'));
 	   set3.add(new CheckerNode(4,0,'C'));
 	   set3.add(new CheckerNode(2,1,'C'));
 	   set3.add(new CheckerNode(3,1,'C'));
 	   set3.add(new CheckerNode(4,1,'C'));
 	   set3.add(new CheckerNode(2,2,'C'));
 	   set3.add(new CheckerNode(3,2,'C'));
 	   set3.add(new CheckerNode(4,2,'C'));
 	   set3.add(new CheckerNode(2,3,'C'));
 	   set3.add(new CheckerNode(4,3,'C'));
 	   theConfigs.add(set3);
 	   
 	   LinkedList set4 = new LinkedList(); //arrow
 	   set4.add(new CheckerNode(3,0,'C'));
 	   set4.add(new CheckerNode(3,1,'C'));
 	   set4.add(new CheckerNode(2,1,'C'));
 	   set4.add(new CheckerNode(4,1,'C'));
 	   set4.add(new CheckerNode(1,2,'C'));
 	   set4.add(new CheckerNode(2,2,'C'));
 	   set4.add(new CheckerNode(3,2,'C'));
 	   set4.add(new CheckerNode(4,2,'C'));
 	   set4.add(new CheckerNode(5,2,'C'));
 	   set4.add(new CheckerNode(3,3,'C'));
 	   set4.add(new CheckerNode(3,4,'C'));
 	   set4.add(new CheckerNode(3,5,'C'));
 	   set4.add(new CheckerNode(2,5,'C'));
 	   set4.add(new CheckerNode(4,5,'C'));
 	   set4.add(new CheckerNode(2,6,'C'));
 	   set4.add(new CheckerNode(3,6,'C'));
 	   set4.add(new CheckerNode(4,6,'C'));
 	   theConfigs.add(set4);
 	   
 	   LinkedList set5 = new LinkedList(); //pyramid
 	   set5.add(new CheckerNode(3,1,'C'));
 	   set5.add(new CheckerNode(2,2,'C'));
 	   set5.add(new CheckerNode(3,2,'C'));
 	   set5.add(new CheckerNode(4,2,'C'));
 	   set5.add(new CheckerNode(1,3,'C'));
 	   set5.add(new CheckerNode(2,3,'C'));
 	   set5.add(new CheckerNode(3,3,'C'));
 	   set5.add(new CheckerNode(4,3,'C'));
 	   set5.add(new CheckerNode(5,3,'C'));
 	   set5.add(new CheckerNode(0,4,'C'));
 	   set5.add(new CheckerNode(1,4,'C'));
 	   set5.add(new CheckerNode(2,4,'C'));
 	   set5.add(new CheckerNode(3,4,'C'));
 	   set5.add(new CheckerNode(4,4,'C'));
 	   set5.add(new CheckerNode(5,4,'C'));
 	   set5.add(new CheckerNode(6,4,'C'));
 	   theConfigs.add(set5);
 	   
 	   LinkedList set6 = new LinkedList(); //diamond
 	   for(int i=0; i<7; i++) {
 		   for(int j=0; j<7; j++) {
 			   if((i<2 || i>4) && (j<2 || j>4)) continue;
 			   if((i==0 || i==6) && (j==2 || j==4)) continue;
 			   if((i==2 || i==4) && (j==0 || j==6)) continue;
 			   if(i==3 && j==3) continue;
 			   set6.add(new CheckerNode(i,j,'C'));
 		   }
 	   }
 	   theConfigs.add(set6);
 	}
 }
