 package coalitiongames;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.gnu.glpk.GLPK;
 import org.gnu.glpk.GLPKConstants;
 import org.gnu.glpk.SWIGTYPE_p_double;
 import org.gnu.glpk.SWIGTYPE_p_int;
 import org.gnu.glpk.glp_iocp;
 import org.gnu.glpk.glp_prob;
 
 abstract class MipGenerator {
 
     public static final double MIN_BUDGET = 100.0;
     
     public static final boolean DEBUGGING = true;
     
     /**
      * 
      * @param values a list of all the other agents, 
      * other than the demanding agent.
      * @param prices a list of prices for all the other agents, 
      * excluding the demanding agent.
      * @param budget budget of the demanding agent
      * @param kMax 1 more than the maximum agents that can be demanded. 
      * in other words, the max
      * agents per team.
      * @param kMin the min agents per team. 1 more than the 
      * minimum agents that can be demanded.
      * @param maxPrice maximum allowable price, used only 
      * for assertions in guard code.
      * @return
      */
     public static MipResult getLpSolution(
         final List<Double> values,
         final List<Double> prices,
         final double budget,
         final int kMax,
         final int kMin,
         final double maxPrice
     ) {
         assert values.size() >= 2;
         assert values.size() == prices.size();
         assert budget >= MIN_BUDGET;
         assert kMax >= kMin;
         assert kMin >= 0;
         assert checkKRange(values.size() + 1, kMin, kMax);
         
         // NB: values does not include the self agent,
         // so kMax can equal values.size().
         assert kMax <= values.size(); 
         
         if (DEBUGGING) {
             for (Double value: values) {
                 if (value < 0) {
                     throw new IllegalArgumentException();
                 }
             }
             
             for (Double price: prices) {
                 if (price < 0 || price > maxPrice) {
                     throw new IllegalArgumentException();
                 }
             }
         }
         
         final int n = values.size();
         final glp_prob lp = GLPK.glp_create_prob();
         
         GLPK.glp_add_cols(lp, n);
         for (int i = 1; i <= n; i++) {
             GLPK.glp_set_col_name(lp, i, "x" + i);
             GLPK.glp_set_col_kind(lp, i, GLPKConstants.GLP_IV);
             GLPK.glp_set_col_bnds(lp, i, GLPKConstants.GLP_DB, 0, 1);
         }
         
         int countRows = 2;
         if (kMin > 1) {
             countRows++;
         }
         GLPK.glp_add_rows(lp, countRows);
         
         SWIGTYPE_p_int ind;
         SWIGTYPE_p_double val;
         
         // sum i from 1->n: x_i <= kMax - 1
         GLPK.glp_set_row_name(lp, 1, "c1");
         GLPK.glp_set_row_bnds(lp, 1, GLPKConstants.GLP_UP, 0, kMax - 1);
         ind = GLPK.new_intArray(n);
         val = GLPK.new_doubleArray(n);
         for (int i = 1; i <= n; i++) {
             GLPK.intArray_setitem(ind, i, i);
             GLPK.doubleArray_setitem(val, i, 1);
         }
         GLPK.glp_set_mat_row(lp, 1, n, ind, val);
         
         // sum i from 1->n: x_i prices_i <= budget
         GLPK.glp_set_row_name(lp, 2, "c2");
         GLPK.glp_set_row_bnds(lp, 2, GLPKConstants.GLP_UP, 0, budget);
         ind = GLPK.new_intArray(n);
         val = GLPK.new_doubleArray(n);
         for (int i = 1; i <= n; i++) {
             GLPK.intArray_setitem(ind, i, i);
             GLPK.doubleArray_setitem(val, i, prices.get(i - 1));
         }
         GLPK.glp_set_mat_row(lp, 2, n, ind, val);
         
         if (kMin > 1) {
             // sum i from 1->n: x_i >= kMin - 1
             final int thirdColNumber = 3;
             GLPK.glp_set_row_name(lp, thirdColNumber, "c3");
             GLPK.glp_set_row_bnds(
                 lp, 
                 thirdColNumber, 
                 GLPKConstants.GLP_LO, 
                 kMin - 1, // lower bound
                 kMax - 1 // upper bound (ignored)
             );
             ind = GLPK.new_intArray(n);
             val = GLPK.new_doubleArray(n);
             for (int i = 1; i <= n; i++) {
                 GLPK.intArray_setitem(ind, i, i);
                 GLPK.doubleArray_setitem(val, i, 1);
             }
             GLPK.glp_set_mat_row(lp, thirdColNumber, n, ind, val);
         }
         
         GLPK.glp_set_obj_name(lp, "obj");
         GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MAX);
         GLPK.glp_set_obj_coef(lp, 0, 0);
         for (int i = 1; i <= n; i++) {
             GLPK.glp_set_obj_coef(lp, i, values.get(i - 1));
         }
         
         final glp_iocp iocp = new glp_iocp();
         GLPK.glp_init_iocp(iocp);
         iocp.setPresolve(GLPKConstants.GLP_ON);
         iocp.setMsg_lev(GLPKConstants.GLP_MSG_OFF);
         final int ret = GLPK.glp_intopt(lp, iocp);
         
         if (ret == 0) {
             final MipResult result = new MipResult(lp, n);
             GLPK.glp_delete_prob(lp);
             
             if (DEBUGGING) {
                 final int testIterations = 10000;
                 boolean testResult = checkLpSolution(
                     result, 
                     values, 
                     prices, 
                     budget, 
                     kMax, 
                     kMin, 
                     testIterations
                 );
                 if (!testResult) {
                     throw new IllegalStateException();
                 }
             }
             
             return result;
         } 
           
         GLPK.glp_delete_prob(lp);
         throw new IllegalStateException();
     }
     
     public static boolean checkLpSolution(
         final MipResult solution,
         final List<Double> values,
         final List<Double> prices,
         final double budget,
         final int kMax,
         final int kMin,
         final int iterations
     ) {
         final List<Double> columnValues = solution.getColumnValues();
         
         // check if number of selected agents is in (kMin, kMax)
         int countOnes = 0;
         final double epsilon = 0.00001; // tolerance for floating point
         for (Double columnValue: columnValues) {
             if (Math.abs(columnValue) > epsilon 
                 && Math.abs(columnValue - 1.0) > epsilon
             ) {
                 System.out.println("Value not in {0, 1}: " + columnValue);
                 return false;
             }
             if (Math.abs(columnValue - 1.0) <= epsilon) {
                 countOnes++;
             }
         }
         
         if (countOnes < kMin - 1 || countOnes > kMax - 1) {
             System.out.println("Wrong number of ones");
             return false;
         }
         
         double total = 0;
         for (int i = 0; i < prices.size(); i++) {
             total += columnValues.get(i) * prices.get(i);
         }
         if (total > budget) {
             System.out.println("Over budget: " + total);
             return false;
         }
         
         double value = 0.0;
         for (int i = 0; i < values.size(); i++) {
             value += columnValues.get(i) * values.get(i);
         }      
         final double referenceValue = value;
         
         // for "iterations" number of trials, (pick kMax - 1) items 
         // at random and, if the
         // set is affordable, test if it is preferred to the given set.
         final int[] demand = new int[prices.size()];
         for (int iter = 0; iter < iterations; iter++) {
             // pick (kMax - 1) items at random
             // initialize all items to 0, not picked
             for (
                 int demandIndex = 0; 
                 demandIndex < demand.length; 
                 demandIndex++
             ) {
                 demand[demandIndex] = 0;
             }
             // pick kMax - 1 items.
             int ones = kMax - 1;
             
             for (int index = 0; index < demand.length; index++) {
                 // pick an an item from {0, 1, last - # already picked}
                 final int randIndex = 
                     (int) (Math.random() * (demand.length - index));
                 // if this number is <= the number of 1's "left" to be picked,
                 // count it as drawing a 1, and set the current index to 1.
                 // decrement the number of 1's left to pick.
                 if (randIndex < ones) {
                     demand[index] = 1;
                     ones--;
                 }
             }
             
             // get cost of the random bundle
             total = 0;
            for (int i = 0; i < demand.length; i++) {
                total += demand[i] * prices.get(i);
             }
             // test if bundle is affordable
             if (total <= budget) {
                 // get value of the random bundle
                 double iterValue = 0.0;
                 for (int i = 0; i < demand.length; i++) {
                     iterValue += demand[i] * values.get(i);
                 }
                 if (iterValue > referenceValue) {
                     System.out.println(
                         "Preferred set: " + Arrays.toString(demand)
                     );
                     System.out.println("Preferred set value: " + iterValue);
                     System.out.println("Reference value: " + referenceValue);
                     return false;
                 }
             }
         }
         
         return true;
     }
     
     /*
      * n should be the total agent count, not "total agents - 1"
      */
     private static boolean checkKRange(
         final int n,
         final int kMin,
         final int kMax
     ) {
         if (kMin < 0 || kMin > kMax || kMax > n) {
             throw new IllegalArgumentException();
         }
         
         return (kMin < 2) || (n % kMin == 0) 
             || (n % kMax == 0) || (n / kMin != n / kMax);
     }
 }
