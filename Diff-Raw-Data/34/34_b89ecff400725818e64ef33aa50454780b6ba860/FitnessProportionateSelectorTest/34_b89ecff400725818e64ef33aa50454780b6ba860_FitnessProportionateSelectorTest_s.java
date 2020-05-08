 package to.richard.tsp.test;
 
 import org.junit.Test;
 import static org.junit.Assert.assertEquals;
 
 import to.richard.tsp.*;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * Author: Richard To
  * Date: 2/7/13
  */
 public class FitnessProportionateSelectorTest {
     @Test
     public void testSelectParents() throws Exception {
         int[][] costMatrixArray = {{20, 30, 10}, {40, 21, 10}, {42, 11, 101}};
         CostMatrix costMatrix = new CostMatrix(costMatrixArray);
 
         MockRandom random = new MockRandom();
         ArrayList<Double> sequence = new ArrayList<Double>(Arrays.asList(
                 new Double[]{.93, .55}));
         random.setDoubleSequence(sequence);
         RouletteWheel<Genotype> wheel = new RouletteWheel<Genotype>(random);
 
         FitnessEvaluator fitnessEvaluator = new FitnessEvaluator(costMatrix);
         FitnessProportionateSelector fps = new FitnessProportionateSelector(
                 fitnessEvaluator, wheel, random);
 
         //82 + 61 = 143
         ArrayList<Genotype> genotypes = new ArrayList<Genotype>() {{
             // 0,1 - 1,2 - 2,0
             // 30 + 10 + 42 = 82 (57%)
             add(new Genotype(new Allele[]{new Allele(1), new Allele(2)}));
             // 0,2 - 2,1 - 1,0
             // 10 + 11 + 40 = 61 (43%)
             add(new Genotype(new Allele[]{new Allele(2), new Allele(1)}));
         }};
 
         List<Genotype> parentGenotypes = fps.selectParents(genotypes);
         assertEquals("21", parentGenotypes.get(0).toString());
         assertEquals("12", parentGenotypes.get(1).toString());
     }
 
     @Test
     public void testFPSMinimization() throws Exception {
         FPSMinimization fpsMinimization = new FPSMinimization();
         ArrayList<Pair<Double, Genotype>> pairs = new ArrayList<Pair<Double, Genotype>>() {{
             add(new Pair<Double, Genotype>(10.0, new MutableGenotype(3)));
             add(new Pair<Double, Genotype>(50.0, new MutableGenotype(3)));
             add(new Pair<Double, Genotype>(90.0, new MutableGenotype(3)));
         }};
 
         List<Pair<Double, Genotype>> newPairs = fpsMinimization.transform(pairs);
         assertEquals(140.0, newPairs.get(0).getFirstValue(), 0.1);
         assertEquals(100.0, newPairs.get(1).getFirstValue(), 0.1);
         assertEquals(60.0, newPairs.get(2).getFirstValue(), 0.1);
     }
 
     @Test
     public void testFPSLinearNormalization() throws Exception {
         FPSLinearNormalization fpsTransform = new FPSLinearNormalization(new FitnessMinimizationComparator());
         ArrayList<Pair<Double, Genotype>> pairs = new ArrayList<Pair<Double, Genotype>>() {{
             add(new Pair<Double, Genotype>(90.0, new MutableGenotype(new Allele[]{new Allele(1)})));
             add(new Pair<Double, Genotype>(10.0, new MutableGenotype(new Allele[]{new Allele(2)})));
             add(new Pair<Double, Genotype>(50.0, new MutableGenotype(new Allele[]{new Allele(3)})));
             add(new Pair<Double, Genotype>(50.0, new MutableGenotype(new Allele[]{new Allele(4)})));
         }};
 
         List<Pair<Double, Genotype>> newPairs = fpsTransform.transform(pairs);
         assertEquals(4, newPairs.get(0).getFirstValue(), 0.1);
         assertEquals(2.5, newPairs.get(1).getFirstValue(), 0.1);
         assertEquals(2.5, newPairs.get(2).getFirstValue(), 0.1);
         assertEquals(1, newPairs.get(3).getFirstValue(), 0.1);
 
         assertEquals("2", newPairs.get(0).getSecondValue().toString());
         assertEquals("1", newPairs.get(3).getSecondValue().toString());
 
     }
 }
