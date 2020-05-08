 import static org.junit.Assert.*;
 
 import java.util.Vector;
 
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.game.YGame;
 import edu.berkeley.gamesman.game.YGame.Node;
 
 /**
  * @author Daniel and hEADcRASH
  *
  */
 public class TestY {
     /*
      * Array exp[][] is of the form { {TRIANGLE#, INDEX#}, {TRIANGLE#, INDEX#} ...}
      */
 
     private Configuration conf24, conf36, conf48;
     private YGame ygame24, ygame36, ygame48;
 
 
     @Before
     public void setUp() throws ClassNotFoundException{
         this.conf24 = new Configuration("jobs/YGame24.job");
         this.ygame24 = (YGame)this.conf24.getGame();     
         this.ygame24.fillBoardWithPlayer('X');
 
         this.conf36 = new Configuration("jobs/YGame36.job");
         this.ygame36 = (YGame)this.conf36.getGame();     
         this.ygame36.fillBoardWithPlayer('X');
 
         this.conf48 = new Configuration("jobs/YGame48.job");
         this.ygame48 = (YGame)this.conf48.getGame();     
         this.ygame48.fillBoardWithPlayer('X');
     }
 
     @Test
     public void Test_2by4_Tri0_Ind0()  {
         Vector<Node> neighbors = this.ygame24.getNeighbors(0, 0, 'X');
         int exp[][] = { {0,1}, {0,5} , {1,8}, {1,0}, {1,1}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_2by4_Tri0_Ind1()  {
         Vector<Node> neighbors = this.ygame24.getNeighbors(0, 1, 'X');
         int exp[][] = { {0,2}, {0,3} , {0,5}, {0,0}, {1,1}, {1,2}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
 
     @Test
     public void Test_2by4_Tri0_Ind2()  {
         Vector<Node> neighbors = this.ygame24.getNeighbors(0, 2, 'X');
         int exp[][] = { {0,3}, {0,1} , {1,2}, {1,3}, {1,4}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_2by4_Tri0_Ind3()  {
         Vector<Node> neighbors = this.ygame24.getNeighbors(0, 3, 'X');
         int exp[][] = { {0,4}, {0,5} , {0,1}, {0,2}, {1,4}, {1,5} };
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_2by4_Tri0_Ind4()  {
         Vector<Node> neighbors = this.ygame24.getNeighbors(0, 4, 'X');
         int exp[][] = { {0,5}, {0,3} , {1,5}, {1,6}, {1,7}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_2by4_Tri0_Ind5()  {
         Vector<Node> neighbors = this.ygame24.getNeighbors(0, 5, 'X');
         int exp[][] = { {0,0}, {0,1} , {0,3}, {0,4}, {1,7}, {1,8}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
 
     @Test
     public void Test_2by4_Tri1_Ind0()  {
         Vector<Node> neighbors = this.ygame24.getNeighbors(1, 0, 'X');
         int exp[][] =
         {
                 { 1, 1 },
                 { 0, 0 },
                 { 1, 8 },
                 { 2, 11 },
                 { 2, 0 },
                 { 2, 1 } };
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
 
     @Test
     public void Test_2by4_Tri1_Ind1()  {
         Vector<Node> neighbors = this.ygame24.getNeighbors(1, 1, 'X');
         int exp[][] = { {1,2}, {0,1} , {0,0}, {1,0}, {2,1}, {2,2}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_2by4_Tri1_Ind2()  {
         Vector<Node> neighbors = this.ygame24.getNeighbors(1, 2, 'X');
         int exp[][] = { {1,3}, {0,2} , {0,1}, {1,1}, {2,2}, {2,3}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_2by4_Tri1_Ind3()  {
         Vector<Node> neighbors = this.ygame24.getNeighbors(1, 3, 'X');
         int exp[][] = { {1,4}, {0,2} , {1,2}, {2,3}, {2,4}, {2,5}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_2by4_Tri1_Ind4()  {
         Vector<Node> neighbors = this.ygame24.getNeighbors(1, 4, 'X');
         int exp[][] = { {1,5}, {0,3} , {0,2}, {1,3}, {2,5}, {2,6}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_2by4_Tri1_Ind5()  {
         Vector<Node> neighbors = this.ygame24.getNeighbors(1, 5, 'X');
         int exp[][] = { {1,6}, {0,4} , {0,3}, {1,4}, {2,6}, {2,7}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_2by4_Tri1_Ind6()  {
         Vector<Node> neighbors = this.ygame24.getNeighbors(1, 6, 'X');
         int exp[][] = { {1,7}, {0,4} , {1,5}, {2,7}, {2,8}, {2,9}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_2by4_Tri1_Ind7()  {
         Vector<Node> neighbors = this.ygame24.getNeighbors(1, 7, 'X');
         int exp[][] = { {1,8}, {0,5} , {0,4}, {1,6}, {2,9}, {2,10}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_2by4_Tri1_Ind8()  {
         Vector<Node> neighbors = this.ygame24.getNeighbors(1, 8, 'X');
         int exp[][] = { {1,0}, {0,0} , {0,5}, {1,7}, {2,10}, {2,11}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_2by4_Tri2_Ind0()  {
         Vector<Node> neighbors = this.ygame24.getNeighbors(2, 0,'X');
         int exp[][] = { {2,1}, {1,0}, {2,11}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
 
     @Test
     public void Test_2by4_Tri2_Ind1()  {
         Vector<Node> neighbors = this.ygame24.getNeighbors(2, 1,'X');
         int exp[][] = { {2,2}, {1,1}, {1,0}, {2,0}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_2by4_Tri2_Ind2()  {
         Vector<Node> neighbors = this.ygame24.getNeighbors(2, 2,'X');
         int exp[][] = { {2,3}, {1,2}, {1,1}, {2,1}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_2by4_Tri2_Ind3()  {
         Vector<Node> neighbors = this.ygame24.getNeighbors(2, 3,'X');
         int exp[][] = { {2,4}, {1,3}, {1,2}, {2,2}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_2by4_Tri2_Ind4()  {
         Vector<Node> neighbors = this.ygame24.getNeighbors(2,4 ,'X');
         int exp[][] = { {2,5}, {1,3}, {2,3}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_2by4_Tri2_Ind5()  {
         Vector<Node> neighbors = this.ygame24.getNeighbors(2, 5,'X');
         int exp[][] = { {2,6}, {1,4}, {1,3}, {2,4}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_2by4_Tri2_Ind6()  {
         Vector<Node> neighbors = this.ygame24.getNeighbors(2,  6,'X');
         int exp[][] = { {2,7}, {1,5}, {1,4}, {2,5}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_2by4_Tri2_Ind7()  {
         Vector<Node> neighbors = this.ygame24.getNeighbors(2, 7 ,'X');
         int exp[][] = { {2,8}, {1,6}, {1,5}, {2,6}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_2by4_Tri2_Ind8()  {
         Vector<Node> neighbors = this.ygame24.getNeighbors(2, 8,'X');
         int exp[][] = { {2,9}, {1,6}, {2,7}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_2by4_Tri2_Ind9()  {
         Vector<Node> neighbors = this.ygame24.getNeighbors(2, 9,'X');
         int exp[][] = { {2,10}, {1,7}, {1,6}, {2,8}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_2by4_Tri2_Ind10()  {
         Vector<Node> neighbors = this.ygame24.getNeighbors(2, 10,'X');
         int exp[][] = { {2,11}, {1,8}, {1,7}, {2,9}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_2by4_Tri2_Ind11()  {
         Vector<Node> neighbors = this.ygame24.getNeighbors(2, 11,'X');
         int exp[][] = { {2,0}, {1,0}, {1,8}, {2,10}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
 
     /*
      * 
      * 
      * 
      * Next test case: 3by6 YGame
      * 
      * 
      * 
      */
 
     @Test
     public void Test_3by6_Tri0_Ind0()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(0, 0,'X');
         int exp[][] = { {1,1}, {1,2}, {1,4}, {1,5}, {1,7}, {1,8}}; 
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
 
     @Test
     public void Test_3by6_Tri1_Ind0()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(1, 0,'X');
         int exp[][] = { {1,1}, {1,8}, {2,11}, {2,0}, {2,1}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
 
     @Test
     public void Test_3by6_Tri1_Ind1()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(1, 1,'X');
         int exp[][] = { {1,2}, {0,0}, {1,8}, {1,0}, {2,1}, {2,2}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
 
     @Test
     public void Test_3by6_Tri1_Ind2()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(1, 2,'X');
         int exp[][] = { {1,3}, {1,4}, {0,0}, {1,1}, {2,2}, {2,3}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
 
     @Test
     public void Test_3by6_Tri1_Ind3()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(1, 3,'X');
         int exp[][] = { {1,4}, {1,2}, {2,3}, {2,4}, {2,5}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
 
     @Test
     public void Test_3by6_Tri1_Ind4()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(1, 4,'X');
         int exp[][] = { {1,5}, {0,0}, {1,2}, {1,3}, {2,5}, {2,6}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
 
     @Test
     public void Test_3by6_Tri1_Ind5()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(1, 5,'X');
         int exp[][] = { {1,6}, {1,7}, {0,0}, {1,4}, {2,6}, {2,7}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
 
     @Test
     public void Test_3by6_Tri1_Ind6()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(1, 6,'X');
         int exp[][] = { {1,7}, {1,5}, {2,7}, {2,8}, {2,9}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
 
     @Test
     public void Test_3by6_Tri1_Ind7()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(1, 7,'X');
         int exp[][] = { {1,8}, {0,0}, {1,5}, {1,6}, {2,9}, {2,10}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
 
     @Test
     public void Test_3by6_Tri1_Ind8()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(1, 8,'X');
         int exp[][] = { {1,0}, {1,1}, {0,0}, {1,7}, {2,10}, {2,11}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
 
     @Test
     public void Test_3by6_Tri2_Ind0()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(2, 0,'X');
         int exp[][] = { {2,1}, {1,0}, {2,11}, {3,14}, {3,0}, {3,1}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
 
     @Test
     public void Test_3by6_Tri2_Ind1()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(2, 1,'X');
         int exp[][] = { {2,2}, {1,1}, {1,0}, {2,0}, {3,1}, {3,2}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri2_Ind2()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(2, 2,'X');
         int exp[][] = { {2,3}, {1,2}, {1,1}, {2,1}, {3,2}, {3,3}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri2_Ind3()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(2, 3,'X');
         int exp[][] = { {2,4}, {1,3}, {1,2}, {2,2}, {3,3}, {3,4}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri2_Ind4()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(2, 4,'X');
         int exp[][] = { {2,5}, {1,3}, {2,3}, {3,4}, {3,5}, {3,6}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri2_Ind5()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(2, 5,'X');
         int exp[][] = { {2,6}, {1,4}, {1,3}, {2,4}, {3,6}, {3,7}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri2_Ind6()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(2, 6,'X');
         int exp[][] = { {2,7}, {1,5}, {1,4}, {2,5}, {3,7}, {3,8}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri2_Ind7()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(2, 7,'X');
         int exp[][] = { {2,8}, {1,6}, {1,5}, {2,6}, {3,8}, {3,9}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri2_Ind8()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(2, 8,'X');
         int exp[][] = { {2,9}, {1,6}, {2,7}, {3,9}, {3,10}, {3,11}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri2_Ind9()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(2, 9,'X');
         int exp[][] = { {2,10}, {1,7}, {1,6}, {2,8}, {3,11}, {3,12}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri2_Ind10()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(2, 10,'X');
         int exp[][] = { {2,11}, {1,8}, {1,7}, {2,9}, {3,12}, {3,13}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri2_Ind11()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(2, 11,'X');
         int exp[][] = { {2,0}, {1,0}, {1,8}, {2,10}, {3,13}, {3,14}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
 
     @Test
     public void Test_3by6_Tri3_Ind0()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(3, 0,'X');
         int exp[][] = { {3,1}, {2,0}, {3,14}, {4,17}, {4,0}, {4,1}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
 
     @Test
     public void Test_3by6_Tri3_Ind1()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(3, 1,'X');
         int exp[][] = { {3,2}, {2,1}, {2,0}, {3,0}, {4,1}, {4,2}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri3_Ind2()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(3, 2,'X');
         int exp[][] = { {3,3}, {2,2}, {2,1}, {3,1}, {4,2}, {4,3}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri3_Ind3()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(3, 3,'X');
         int exp[][] = { {3,4}, {2,3}, {2,2}, {3,2}, {4,3}, {4,4}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri3_Ind4()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(3, 4,'X');
         int exp[][] = { {3,5}, {2,4}, {2,3}, {3,3}, {4,4}, {4,5}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri3_Ind5()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(3, 5,'X');
         int exp[][] = { {3,6}, {2,4}, {3,4}, {4,5}, {4,6}, {4,7}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri3_Ind6()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(3, 6,'X');
         int exp[][] = { {3,7}, {2,5}, {2,4}, {3,5}, {4,7}, {4,8}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri3_Ind7()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(3, 7,'X');
         int exp[][] = { {3,8}, {2,6}, {2,5}, {3,6}, {4,8}, {4,9}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri3_Ind8()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(3, 8,'X');
         int exp[][] = { {3,9}, {2,7}, {2,6}, {3,7}, {4,9}, {4,10}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri3_Ind9()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(3, 9,'X');
         int exp[][] = { {3,10}, {2,8}, {2,7}, {3,8}, {4,10}, {4,11}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri3_Ind10()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(3, 10,'X');
         int exp[][] = { {3,11}, {2,8}, {3,9}, {4,11}, {4,12}, {4,13}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri3_Ind11()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(3, 11,'X');
         int exp[][] = { {3,12}, {2,9}, {2,8}, {3,10}, {4,13}, {4,14}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri3_Ind12()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(3, 12,'X');
         int exp[][] = { {3,13}, {2,10}, {2,9}, {3,11}, {4,14}, {4,15}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri3_Ind13()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(3, 13,'X');
         int exp[][] = { {3,14}, {2,11}, {2,10}, {3,12}, {4,15}, {4,16}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri3_Ind14()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(3, 14,'X');
         int exp[][] = { {3,0}, {2,0}, {2,11}, {3,13}, {4,16}, {4,17}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
 
     @Test
     public void Test_3by6_Tri4_Ind0()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(4, 0,'X');
         int exp[][] = { {4,1}, {3,0}, {4,17}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
 
     @Test
     public void Test_3by6_Tri4_Ind1()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(4, 1,'X');
         int exp[][] = { {4,2}, {3,1}, {3,0}, {4,0}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri4_Ind2()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(4, 2,'X');
         int exp[][] = { {4,3}, {3,2}, {3,1}, {4,1}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri4_Ind3()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(4, 3,'X');
         int exp[][] = { {4,4}, {3,3}, {3,2}, {4,2}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri4_Ind4()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(4, 4,'X');
         int exp[][] = { {4,5}, {3,4}, {3,3}, {4,3}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri4_Ind5()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(4, 5,'X');
         int exp[][] = { {4,6}, {3,5}, {3,4}, {4,4}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri4_Ind6()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(4, 6,'X');
         int exp[][] = { {4,7}, {3,5}, {4,5}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri4_Ind7()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(4, 7,'X');
         int exp[][] = { {4,8}, {3,6}, {3,5}, {4,6}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri4_Ind8()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(4, 8,'X');
         int exp[][] = { {4,9}, {3,7}, {3,6}, {4,7}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri4_Ind9()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(4, 9,'X');
         int exp[][] = { {4,10}, {3,8}, {3,7}, {4,8}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri4_Ind10()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(4, 10,'X');
         int exp[][] = { {4,11}, {3,9}, {3,8}, {4,9}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri4_Ind11()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(4, 11,'X');
         int exp[][] = { {4,12}, {3,10}, {3,9}, {4,10}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri4_Ind12()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(4, 12,'X');
         int exp[][] = { {4,13}, {3,10}, {4,11}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri4_Ind13()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(4, 13,'X');
         int exp[][] = { {4,14}, {3,11}, {3,10}, {4,12}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri4_Ind14()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(4, 14,'X');
         int exp[][] = { {4,15}, {3,12}, {3,11}, {4,13}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri4_Ind15()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(4, 15,'X');
         int exp[][] = { {4,16}, {3,13}, {3,12}, {4,14}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri4_Ind16()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(4, 16,'X');
         int exp[][] = { {4,17}, {3,14}, {3,13}, {4,15}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_3by6_Tri4_Ind17()  {
         Vector<Node> neighbors = this.ygame36.getNeighbors(4, 17,'X');
         int exp[][] = { {4,0}, {3,0}, {3,14}, {4,16}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
 
     @Test
     public void Test_4by8_Tri0_Ind0()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(0, 0,'X');
         int exp[][] = { {0,1}, {0,2}, {1,10}, {1,11}, {1,1}, {1,2}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
 
     @Test
     public void Test_4by8_Tri0_Ind1()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(0, 1,'X');
         int exp[][] = { {0,2}, {0,0}, {1,2}, {1,3}, {1,5}, {1,6}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_4by8_Tri0_Ind2()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(0, 2,'X');
         int exp[][] = { {0,0}, {0,1}, {1,6}, {1,7}, {1,9}, {1,10}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_4by8_Tri1_Ind0()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(1, 0,'X');
         int exp[][] = { {1,1}, {1,11}, {2,14}, {2,0}, {2,1}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
 
     @Test
     public void Test_4by8_Tri1_Ind1()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(1, 1,'X');
         int exp[][] = { {1,2}, {0,0}, {1,11}, {1,0}, {2,1}, {2,2}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_4by8_Tri1_Ind2()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(1, 2,'X');
         int exp[][] = { {1,3}, {0,1}, {0,0}, {1,1}, {2,2}, {2,3}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_4by8_Tri1_Ind3()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(1, 3,'X');
         int exp[][] = { {1,4}, {1,5}, {0,1}, {1,2}, {2,3}, {2,4}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_4by8_Tri1_Ind4()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(1, 4,'X');
         int exp[][] = { {1,5}, {1,3}, {2,4}, {2,5}, {2,6}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_4by8_Tri1_Ind5()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(1, 5,'X');
         int exp[][] = { {1,6}, {0,1}, {1,3}, {1,4}, {2,6}, {2,7}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_4by8_Tri1_Ind6()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(1, 6,'X');
         int exp[][] = { {1,7}, {0,2}, {0,1}, {1,5}, {2,7}, {2,8}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_4by8_Tri1_Ind7()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(1, 7,'X');
         int exp[][] = { {1,8}, {1,9}, {0,2}, {1,6}, {2,8}, {2,9}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_4by8_Tri1_Ind8()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(1, 8,'X');
         int exp[][] = { {1,9}, {1,7}, {2,9}, {2,10}, {2,11}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_4by8_Tri1_Ind9()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(1, 9,'X');
         int exp[][] = { {1,10}, {0,2}, {1,7}, {1,8}, {2,11}, {2,12}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_4by8_Tri1_Ind10()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(1, 10,'X');
         int exp[][] = { {1,11}, {0,0}, {0,2}, {1,9}, {2,12}, {2,13}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_4by8_Tri1_Ind11()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(1, 11,'X');
         int exp[][] = { {1,0}, {1,1}, {0,0}, {1,10}, {2,13}, {2,14}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
 
     @Test
     public void Test_4by8_Tri2_Ind0()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(2, 0,'X');
         int exp[][] = { {2,1}, {1,0}, {2,14}, {3,17}, {3,0}, {3,1}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_4by8_Tri2_Ind1()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(2, 1,'X');
         int exp[][] = { {2,2}, {1,1}, {1,0}, {2,0}, {3,1}, {3,2}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_4by8_Tri2_Ind2()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(2, 2,'X');
         int exp[][] = { {2,3}, {1,2}, {1,1}, {2,1}, {3,2}, {3,3}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_4by8_Tri2_Ind3()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(2, 3,'X');
         int exp[][] = { {2,4}, {1,3}, {1,2}, {2,2}, {3,3}, {3,4}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_4by8_Tri2_Ind4()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(2, 4,'X');
         int exp[][] = { {2,5}, {1,4}, {1,3}, {2,3}, {3,4}, {3,5}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_4by8_Tri2_Ind5()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(2, 5,'X');
         int exp[][] = { {2,6}, {1,4}, {2,4}, {3,5}, {3,6}, {3,7}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_4by8_Tri2_Ind6()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(2, 6,'X');
         int exp[][] = { {2,7}, {1,5}, {1,4}, {2,5}, {3,7}, {3,8}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_4by8_Tri2_Ind7()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(2, 7,'X');
         int exp[][] = { {2,8}, {1,6}, {1,5}, {2,6}, {3,8}, {3,9}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_4by8_Tri2_Ind8()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(2, 8,'X');
         int exp[][] = { {2,9}, {1,7}, {1,6}, {2,7}, {3,9}, {3,10}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_4by8_Tri2_Ind9()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(2, 9,'X');
         int exp[][] = { {2,10}, {1,8}, {1,7}, {2,8}, {3,10}, {3,11}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_4by8_Tri2_Ind10()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(2, 10,'X');
         int exp[][] = { {2,11}, {1,8}, {2,9}, {3,11}, {3,12}, {3,13}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_4by8_Tri2_Ind11()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(2, 11,'X');
         int exp[][] = { {2,12}, {1,9}, {1,8}, {2,10}, {3,13}, {3,14}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_4by8_Tri2_Ind12()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(2, 12,'X');
         int exp[][] = { {2,13}, {1,10}, {1,9}, {2,11}, {3,14}, {3,15}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_4by8_Tri2_Ind13()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(2, 13,'X');
         int exp[][] = { {2,14}, {1,11}, {1,10}, {2,12}, {3,15}, {3,16}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_4by8_Tri2_Ind14()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(2, 14,'X');
         int exp[][] = { {2,0}, {1,0}, {1,11}, {2,13}, {3,16}, {3,17}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
 
     @Test
     public void Test_4by8_Tri3_Ind0()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(3, 0,'X');
         int exp[][] = { {3,1}, {2,0}, {3,17}, {4,20}, {4,0}, {4,1}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_4by8_Tri3_Ind1()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(3, 1,'X');
         int exp[][] = { {3,2}, {2,1}, {2,0}, {3,0}, {4,1}, {4,2}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri3_Ind2()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(3, 2,'X');
         int exp[][] = { {3,3}, {2,2}, {2,1}, {3,1}, {4,2}, {4,3}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri3_Ind3()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(3, 3,'X');
         int exp[][] = { {3,4}, {2,3}, {2,2}, {3,2}, {4,3}, {4,4}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri3_Ind4()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(3, 4,'X');
         int exp[][] = { {3,5}, {2,4}, {2,3}, {3,3}, {4,4}, {4,5}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri3_Ind5()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(3, 5,'X');
         int exp[][] = { {3,6}, {2,5}, {2,4}, {3,4}, {4,5}, {4,6}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri3_Ind6()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(3, 6,'X');
         int exp[][] = { {3,7}, {2,5}, {3,5}, {4,6}, {4,7}, {4,8}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri3_Ind7()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(3, 7,'X');
         int exp[][] = { {3,8}, {2,6}, {2,5}, {3,6}, {4,8}, {4,9}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri3_Ind8()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(3, 8,'X');
         int exp[][] = { {3,9}, {2,7}, {2,6}, {3,7}, {4,9}, {4,10}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri3_Ind9()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(3, 9,'X');
         int exp[][] = { {3,10}, {2,8}, {2,7}, {3,8}, {4,10}, {4,11}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri3_Ind10()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(3, 10,'X');
         int exp[][] = { {3,11}, {2,9}, {2,8}, {3,9}, {4,11}, {4,12}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri3_Ind11()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(3, 11,'X');
         int exp[][] = { {3,12}, {2,10}, {2,9}, {3,10}, {4,12}, {4,13}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri3_Ind12()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(3, 12,'X');
         int exp[][] = { {3,13}, {2,10}, {3,11}, {4,13}, {4,14}, {4,15}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri3_Ind13()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(3, 13,'X');
         int exp[][] = { {3,14}, {2,11}, {2,10}, {3,12}, {4,15}, {4,16}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri3_Ind14()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(3, 14,'X');
         int exp[][] = { {3,15}, {2,12}, {2,11}, {3,13}, {4,16}, {4,17}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri3_Ind15()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(3, 15,'X');
         int exp[][] = { {3,16}, {2,13}, {2,12}, {3,14}, {4,17}, {4,18}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri3_Ind16()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(3, 16,'X');
         int exp[][] = { {3,17}, {2,14}, {2,13}, {3,15}, {4,18}, {4,19}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri3_Ind17()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(3, 17,'X');
         int exp[][] = { {3,0}, {2,0}, {2,14}, {3,16}, {4,19}, {4,20}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
 
 
     @Test
     public void Test_4by8_Tri4_Ind0()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(4, 0,'X');
         int exp[][] = { {4,1}, {3,0}, {4,20}, {5,23}, {5,0}, {5,1}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_4by8_Tri4_Ind1()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(4, 1,'X');
         int exp[][] = { {4,2}, {3,1}, {3,0}, {4,0}, {5,1}, {5,2}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri4_Ind2()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(4, 2,'X');
         int exp[][] = { {4,3}, {3,2}, {3,1}, {4,1}, {5,2}, {5,3}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri4_Ind3()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(4, 3,'X');
         int exp[][] = { {4,4}, {3,3}, {3,2}, {4,2}, {5,3}, {5,4}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri4_Ind4()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(4, 4,'X');
         int exp[][] = { {4,5}, {3,4}, {3,3}, {4,3}, {5,4}, {5,5}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri4_Ind5()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(4, 5,'X');
         int exp[][] = { {4,6}, {3,5}, {3,4}, {4,4}, {5,5}, {5,6}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri4_Ind6()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(4, 6,'X');
         int exp[][] = { {4,7}, {3,6}, {3,5}, {4,5}, {5,6}, {5,7}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri4_Ind7()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(4, 7,'X');
         int exp[][] = { {4,8}, {3,6}, {4,6}, {5,7}, {5,8}, {5,9}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri4_Ind8()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(4, 8,'X');
         int exp[][] = { {4,9}, {3,7}, {3,6}, {4,7}, {5,9}, {5,10}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri4_Ind9()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(4, 9,'X');
         int exp[][] = { {4,10}, {3,8}, {3,7}, {4,8}, {5,10}, {5,11}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri4_Ind10()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(4, 10,'X');
         int exp[][] = { {4,11}, {3,9}, {3,8}, {4,9}, {5,11}, {5,12}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri4_Ind11()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(4, 11,'X');
         int exp[][] = { {4,12}, {3,10}, {3,9}, {4,10}, {5,12}, {5,13}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri4_Ind12()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(4, 12,'X');
         int exp[][] = { {4,13}, {3,11}, {3,10}, {4,11}, {5,13}, {5,14}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri4_Ind13()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(4, 13,'X');
         int exp[][] = { {4,14}, {3,12}, {3,11}, {4,12}, {5,14}, {5,15}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri4_Ind14()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(4,14 ,'X');
         int exp[][] = { {4,15}, {3,12}, {4,13}, {5,15}, {5,16}, {5,17}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri4_Ind15()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(4, 15,'X');
         int exp[][] = { {4,16}, {3,13}, {3,12}, {4,14}, {5,17}, {5,18}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri4_Ind16()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(4, 16,'X');
         int exp[][] = { {4,17}, {3,14}, {3,13}, {4,15}, {5,18}, {5,19}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri4_Ind17()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(4, 17,'X');
         int exp[][] = { {4,18}, {3,15}, {3,14}, {4,16}, {5,19}, {5,20}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri4_Ind18()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(4, 18,'X');
         int exp[][] = { {4,19}, {3,16}, {3,15}, {4,17}, {5,20}, {5,21}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri4_Ind19()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(4, 19,'X');
         int exp[][] = { {4,20}, {3,17}, {3,16}, {4,18}, {5,21}, {5,22}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri4_Ind20()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(4, 20,'X');
         int exp[][] = { {4,0}, {3,0}, {3,17}, {4,19}, {5,22}, {5,23}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
 
     @Test
     public void Test_4by8_Tri5_Ind0()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(5, 0,'X');
         int exp[][] = { {5,1}, {4,0}, {5,23}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     @Test
     public void Test_4by8_Tri5_Ind1()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(5, 1,'X');
         int exp[][] = { {5,2}, {4,1}, {4,0}, {5,0}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri5_Ind2()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(5, 2,'X');
         int exp[][] = { {5,3}, {4,2}, {4,1}, {5,1}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri5_Ind3()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(5, 3,'X');
        int exp[][] = { {5,4}, {4,3}, {4,3}, {5,2}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri5_Ind4()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(5, 4,'X');
         int exp[][] = { {5,5}, {4,4}, {4,3}, {5,3}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri5_Ind5()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(5, 5,'X');
         int exp[][] = { {5,6}, {4,5}, {4,4}, {5,4}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri5_Ind6()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(5, 6,'X');
         int exp[][] = { {5,7}, {4,6}, {4,5}, {5,5}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri5_Ind7()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(5, 7,'X');
         int exp[][] = { {5,8}, {4,7}, {4,6}, {5,6}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri5_Ind8()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(5, 8,'X');
         int exp[][] = { {5,9}, {4,7}, {5,7}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri5_Ind9()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(5, 9,'X');
         int exp[][] = { {5,10}, {4,8}, {4,7}, {5,8}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri5_Ind10()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(5, 10,'X');
         int exp[][] = { {5,11}, {4,9}, {4,8}, {5,9}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri5_Ind11()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(5, 11,'X');
         int exp[][] = { {5,12}, {4,10}, {4,9}, {5,10}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri5_Ind12()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(5, 12,'X');
         int exp[][] = { {5,13}, {4,11}, {4,10}, {5,11}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri5_Ind13()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(5, 13,'X');
         int exp[][] = { {5,14}, {4,12}, {4,11}, {5,12}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri5_Ind14()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(5, 14,'X');
         int exp[][] = { {5,15}, {4,13}, {4,12}, {5,13}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri5_Ind15()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(5, 15,'X');
         int exp[][] = { {5,16}, {4,14}, {4,13}, {5,14}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri5_Ind16()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(5, 16,'X');
         int exp[][] = { {5,17}, {4,14}, {5,15}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri5_Ind17()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(5, 17,'X');
         int exp[][] = { {5,18}, {4,15}, {4,14}, {5,16}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri5_Ind18()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(5, 18,'X');
         int exp[][] = { {5,19}, {4,16}, {4,15}, {5,17}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri5_Ind19()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(5, 19,'X');
         int exp[][] = { {5,20}, {4,17}, {4,16}, {5,18}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri5_Ind20()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(5, 20,'X');
         int exp[][] = { {5,21}, {4,18}, {4,17}, {5,19}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri5_Ind21()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(5, 21,'X');
         int exp[][] = { {5,22}, {4,19}, {4,18}, {5,20}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri5_Ind22()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(5, 22,'X');
         int exp[][] = { {5,23}, {4,20}, {4,19}, {5,21}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }    
     @Test
     public void Test_4by8_Tri5_Ind23()  {
         Vector<Node> neighbors = this.ygame48.getNeighbors(5, 23,'X');
         int exp[][] = { {5,0}, {4,0}, {4,20}, {5,22}};
 
         for (int i = 0; i < exp.length; i++){
             Assert.assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             Assert.assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
 
     /*
      *  isWin testing...
      */
 
     @Test
     public void Test_2by4_fullWinBoard()
     {
         this.ygame24.fillBoardWithPlayer('X');
 
         Assert.assertTrue(this.ygame24.isWin('X'));   	
     }
 
     @Test
     public void Test_2by4_fullLoseBoard()
     {
         this.ygame24.fillBoardWithPlayer('O');
 
         //ygame24.setPlayerAt(triangle, index, player)
         Assert.assertFalse(this.ygame24.isWin('X'));
     }
 
     @Test
     public void Test_2by4_winBoard1()
     {
         this.ygame24.fillBoardWithPlayer(' ');
         int moves[][] =
         {
                 { 2, 2 },
                 { 1, 2 },
                { 2, 0 },
                 { 1, 4 },
                 { 2, 6 },
                 { 1, 5 },
                 { 0, 4 },
                 { 1, 7 },
                 { 2, 10 } };
 
         for (int[] move : moves)
         {
             this.ygame24.setPlayerAt(move[0], move[1], 'X');
         }
 
         Assert.assertTrue(this.ygame24.isWin('X'));
     }
 
     @Test
     public void Test_2by4_isWin1() {
     	ygame24.fillBoardWithPlayer('X');
     	
     	assertTrue(ygame24.isWin('X'));   	
     }
     
     @Test
     public void Test_2by4_isWin2() {
     	ygame24.fillBoardWithPlayer('O');
     	
     	assertFalse(ygame24.isWin('X'));   	
     }
   
     @Test
     public void Test_2by4_isWin3() {
     	ygame24.fillBoardWithPlayer('O');
     	
     	ygame24.setPlayerAt(2, 0, 'X');
     	ygame24.setPlayerAt(1, 0, 'X');
     	ygame24.setPlayerAt(0, 0, 'X');
     	ygame24.setPlayerAt(0, 1, 'X');
     	ygame24.setPlayerAt(0, 2, 'X');
     	ygame24.setPlayerAt(1, 4, 'X');
     	ygame24.setPlayerAt(2, 5, 'X');
     	
     	assertTrue(ygame24.isWin('X'));   	
     }
     
     @Test
     public void Test_2by4_isWin4() {
     	ygame24.fillBoardWithPlayer('O');
     	
     	ygame24.setPlayerAt(2, 0, 'X');
     	ygame24.setPlayerAt(1, 0, 'X');
     	ygame24.setPlayerAt(1, 8, 'X');
     	ygame24.setPlayerAt(1, 7, 'X');
     	ygame24.setPlayerAt(1, 6, 'X');
     	ygame24.setPlayerAt(1, 5, 'X');
     	ygame24.setPlayerAt(2, 7, 'X');
     	
     	assertTrue(ygame24.isWin('X'));   	
     }
     
     @Test
     public void Test_2by4_isWin5() {
     	ygame24.fillBoardWithPlayer('O');
     	
     	ygame24.setPlayerAt(2, 0, 'X');
     	ygame24.setPlayerAt(2, 11, 'X');
     	ygame24.setPlayerAt(2, 10, 'X');
     	ygame24.setPlayerAt(2, 9, 'X');
     	ygame24.setPlayerAt(2, 8, 'X');
     	
     	assertTrue(ygame24.isWin('X'));   	
     }
     
     @Test
     public void Test_2by4_isWin6() {
     	ygame24.fillBoardWithPlayer('O');
     	
     	ygame24.setPlayerAt(2, 0, 'X');
     	ygame24.setPlayerAt(1, 0, 'X');
     	ygame24.setPlayerAt(0, 0, 'X');
     	ygame24.setPlayerAt(1, 1, 'X');
     	ygame24.setPlayerAt(0, 1, 'X');
     	ygame24.setPlayerAt(1, 2, 'X');
     	ygame24.setPlayerAt(0, 2, 'X');
     	ygame24.setPlayerAt(1, 3, 'X');
     	ygame24.setPlayerAt(2, 4, 'X');
     	
     	assertTrue(ygame24.isWin('X'));   	
     }
     
     @Test
     public void Test_2by4_isWin7() {
     	ygame24.fillBoardWithPlayer('O');
     	
     	ygame24.setPlayerAt(2, 2, 'X');
     	ygame24.setPlayerAt(1, 2, 'X');
     	ygame24.setPlayerAt(0, 3, 'X');
     	ygame24.setPlayerAt(0, 2, 'X');
     	ygame24.setPlayerAt(1, 3, 'X');
     	ygame24.setPlayerAt(1, 4, 'X');
     	ygame24.setPlayerAt(1, 5, 'X');
     	ygame24.setPlayerAt(1, 6, 'X');
     	ygame24.setPlayerAt(1, 7, 'X');
     	ygame24.setPlayerAt(2, 10, 'X');
     	
     	assertFalse(ygame24.isWin('X'));   	
     }
     
     @Test
     public void Test_2by4_isWin8() {
     	ygame24.fillBoardWithPlayer('O');
     	
     	ygame24.setPlayerAt(2, 4, 'X');
     	ygame24.setPlayerAt(1, 3, 'X');
     	ygame24.setPlayerAt(1, 4, 'X');
     	ygame24.setPlayerAt(0, 3, 'X');
     	ygame24.setPlayerAt(1, 5, 'X');
     	ygame24.setPlayerAt(0, 2, 'X');
     	ygame24.setPlayerAt(0, 1, 'X');
     	ygame24.setPlayerAt(1, 1, 'X');
     	ygame24.setPlayerAt(0, 4, 'X');
     	ygame24.setPlayerAt(1, 6, 'X');
     	ygame24.setPlayerAt(2, 8, 'X');    	
     	
     	assertTrue(ygame24.isWin('X'));   	
     }
     
     @Test
     public void Test_2by4_isWin9() {
     	ygame24.fillBoardWithPlayer('O');
     	
     	ygame24.setPlayerAt(2, 1, 'X');
     	ygame24.setPlayerAt(1, 0, 'X');
     	ygame24.setPlayerAt(2, 11, 'X');
     	ygame24.setPlayerAt(2, 3, 'X');
     	ygame24.setPlayerAt(1, 3, 'X');
     	ygame24.setPlayerAt(1, 4, 'X');
     	ygame24.setPlayerAt(1, 5, 'X');
     	ygame24.setPlayerAt(2, 7, 'X');    	
     	
     	assertFalse(ygame24.isWin('X'));   	
     }
     
     @Test
     public void Test_2by4_isWin10() {
     	ygame24.fillBoardWithPlayer('O');
     	
     	ygame24.setPlayerAt(1, 0, 'X');
     	ygame24.setPlayerAt(1, 1, 'X');
     	ygame24.setPlayerAt(1, 2, 'X');
     	ygame24.setPlayerAt(1, 3, 'X');
     	ygame24.setPlayerAt(1, 4, 'X');
     	ygame24.setPlayerAt(1, 5, 'X');
     	ygame24.setPlayerAt(1, 6, 'X');
     	ygame24.setPlayerAt(1, 7, 'X');
     	ygame24.setPlayerAt(1, 8, 'X');    	
     	
     	assertFalse(ygame24.isWin('X'));   	
     }
     
     @Test
     public void Test_2by4_isWin11() {
     	ygame24.fillBoardWithPlayer('O');
     	
     	ygame24.setPlayerAt(2, 2, 'X');
     	ygame24.setPlayerAt(1, 0, 'X');
     	ygame24.setPlayerAt(1, 1, 'X');
     	ygame24.setPlayerAt(1, 2, 'X');
     	ygame24.setPlayerAt(1, 3, 'X');
     	ygame24.setPlayerAt(1, 4, 'X');
     	ygame24.setPlayerAt(1, 5, 'X');
     	ygame24.setPlayerAt(1, 6, 'X');
     	ygame24.setPlayerAt(1, 7, 'X');
     	ygame24.setPlayerAt(1, 8, 'X');    	
     	
     	assertFalse(ygame24.isWin('X'));   	
     }
     
     @Test
     public void Test_2by4_isWin12() {
     	ygame24.fillBoardWithPlayer('O');
     	
     	ygame24.setPlayerAt(2, 2, 'X');
     	ygame24.setPlayerAt(2, 6, 'X');
     	ygame24.setPlayerAt(2, 10, 'X');    	
     	ygame24.setPlayerAt(1, 0, 'X');
     	ygame24.setPlayerAt(1, 1, 'X');
     	ygame24.setPlayerAt(1, 2, 'X');
     	ygame24.setPlayerAt(1, 3, 'X');
     	ygame24.setPlayerAt(1, 4, 'X');
     	ygame24.setPlayerAt(1, 5, 'X');
     	ygame24.setPlayerAt(1, 6, 'X');
     	ygame24.setPlayerAt(1, 7, 'X');
     	ygame24.setPlayerAt(1, 8, 'X');    	
     	
     	assertTrue(ygame24.isWin('X'));   	
     }
     
     @Test
     public void Test_3by6_isWin1() {
     	ygame36.fillBoardWithPlayer('O');
     	
     	assertTrue(ygame36.isWin('O'));   	
     }
     
     @Test
     public void Test_3by6_isWin2() {
     	ygame36.fillBoardWithPlayer('X');
     	
     	ygame36.setPlayerAt(4, 1, 'O');
     	ygame36.setPlayerAt(4, 2, 'O');
     	ygame36.setPlayerAt(4, 3, 'O');
     	ygame36.setPlayerAt(4, 4, 'O');
     	ygame36.setPlayerAt(4, 5, 'O');
     	ygame36.setPlayerAt(2, 0, 'O');
     	ygame36.setPlayerAt(2, 1, 'O');
     	ygame36.setPlayerAt(2, 2, 'O');
     	ygame36.setPlayerAt(3, 3, 'O');
     	ygame36.setPlayerAt(2, 4, 'O');
     	ygame36.setPlayerAt(2, 3, 'O');
     	ygame36.setPlayerAt(0, 0, 'O');
     	ygame36.setPlayerAt(1, 1, 'O');
     	ygame36.setPlayerAt(1, 5, 'O');
     	ygame36.setPlayerAt(1, 6, 'O');
     	ygame36.setPlayerAt(2, 8, 'O');
     	ygame36.setPlayerAt(3, 10, 'O');
     	ygame36.setPlayerAt(4, 12, 'O');
     	
     	assertTrue(ygame36.isWin('O'));   	
     }
     
     @Test
     public void Test_3by6_isWin3() {
     	ygame36.fillBoardWithPlayer('X');
     	
     	ygame36.setPlayerAt(4, 3, 'O');
     	ygame36.setPlayerAt(3, 3, 'O');
     	ygame36.setPlayerAt(2, 3, 'O');
     	ygame36.setPlayerAt(1, 3, 'O');
     	ygame36.setPlayerAt(1, 4, 'O');
     	ygame36.setPlayerAt(1, 5, 'O');
     	ygame36.setPlayerAt(1, 6, 'O');
     	ygame36.setPlayerAt(2, 9, 'O');
     	ygame36.setPlayerAt(3, 12, 'O');
     	ygame36.setPlayerAt(4, 15, 'O');
     	ygame36.setPlayerAt(2, 3, 'O');
     	ygame36.setPlayerAt(0, 0, 'O');
     	ygame36.setPlayerAt(1, 1, 'O');
     	ygame36.setPlayerAt(2, 1, 'O');
     	ygame36.setPlayerAt(4, 5, 'O');
     	//ygame36.setPlayerAt(4, 8, 'O');
     	ygame36.setPlayerAt(3, 5, 'O');
     	ygame36.setPlayerAt(3, 6, 'O');
     	ygame36.setPlayerAt(3, 7, 'O');
     	ygame36.setPlayerAt(3, 8, 'O');
     	ygame36.setPlayerAt(3, 9, 'O');
     	ygame36.setPlayerAt(3, 10, 'O');
     	ygame36.setPlayerAt(4, 13, 'O');
     	
     	assertFalse(ygame36.isWin('O'));   	
     }
     
     @Test
     public void Test_3by6_isWin4() {
     	ygame36.fillBoardWithPlayer('X');
     	
     	ygame36.setPlayerAt(4, 3, 'O');
     	ygame36.setPlayerAt(3, 3, 'O');
     	ygame36.setPlayerAt(2, 3, 'O');
     	ygame36.setPlayerAt(1, 3, 'O');
     	ygame36.setPlayerAt(1, 4, 'O');
     	ygame36.setPlayerAt(1, 5, 'O');
     	ygame36.setPlayerAt(1, 6, 'O');
     	ygame36.setPlayerAt(2, 9, 'O');
     	ygame36.setPlayerAt(3, 12, 'O');
     	ygame36.setPlayerAt(4, 15, 'O');
     	ygame36.setPlayerAt(2, 3, 'O');
     	ygame36.setPlayerAt(0, 0, 'O');
     	ygame36.setPlayerAt(1, 1, 'O');
     	ygame36.setPlayerAt(2, 1, 'O');
     	ygame36.setPlayerAt(4, 5, 'O');
     	//ygame36.setPlayerAt(4, 8, 'O');
     	ygame36.setPlayerAt(3, 5, 'O');
     	ygame36.setPlayerAt(3, 6, 'O');
     	ygame36.setPlayerAt(3, 7, 'O');
     	ygame36.setPlayerAt(3, 8, 'O');
     	ygame36.setPlayerAt(3, 9, 'O');
     	ygame36.setPlayerAt(3, 10, 'O');
     	ygame36.setPlayerAt(4, 13, 'O');
     	//ygame36.setPlayerAt(2, 16, 'O');
     	ygame36.setPlayerAt(4, 9, 'O');    	
     	
     	assertTrue(ygame36.isWin('O'));   	
     }
     
     @AfterClass
     public static void oneTimeTearDown() {}
 
 }
