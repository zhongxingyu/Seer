 package to.richard.tsp.test;
 
 import org.junit.Test;
 
 import to.richard.tsp.CostMatrix;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  * Author: Richard To
  * Date: 2/6/13
  */
 public class CostMatrixTest {
 
     @Test
     public void testGetCostAndCityName() throws Exception {
         int[][] matrix = {{10, 20}, {30, 40}};
         String[] cityNames = {"City0", "City1"};
         CostMatrix costMatrix = new CostMatrix(matrix, cityNames);
 
         int cost = costMatrix.getCost(0, 1);
         cost = 0;
         assertEquals(20, costMatrix.getCost(0, 1));
 
         String city1 = costMatrix.getCityName(1);
         city1 = "City2";
         assertEquals("City1", costMatrix.getCityName(1));
     }
 
     @Test
     public void testGetCityNameIndex() throws Exception {
         int[][] matrix = {{10, 20}, {30, 40}};
         CostMatrix costMatrix = new CostMatrix(matrix);
         assertEquals(20, costMatrix.getCost(0, 1));
         assertEquals("1", costMatrix.getCityName(1));
     }
 
     @Test(expected = IndexOutOfBoundsException.class)
     public void testCityNameIndexBounds() throws Exception {
         int[][] matrix = {{10, 20}, {30, 40}};
         String[] cityNames = {"City0", "City1"};
         CostMatrix costMatrix = new CostMatrix(matrix, cityNames);
         costMatrix.getCityName(5);
     }
 
     @Test(expected = IndexOutOfBoundsException.class)
     public void testIndexOutOfBounds() throws Exception {
         int[][] matrix = {{10, 20}, {30, 40}};
         String[] cityNames = {"City0", "City1"};
         CostMatrix costMatrix = new CostMatrix(matrix, cityNames);
         costMatrix.getCost(5, 2);
     }
 
    @Test(expected = Exception.class)
     public void testCityNamesNotEqualToMatrix() throws Exception {
         int[][] matrix = {{10, 20}, {30, 40}};
         String[] cityNames = {"City0", "City1", "City3"};
         CostMatrix costMatrix = new CostMatrix(matrix, cityNames);
     }
 }
