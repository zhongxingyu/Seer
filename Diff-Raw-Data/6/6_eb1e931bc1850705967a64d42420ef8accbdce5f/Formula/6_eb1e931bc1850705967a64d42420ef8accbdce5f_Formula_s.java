 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Scanner;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Stack;
 import java.util.EmptyStackException;
 
 /**
  * The Class Formula.
  *
  * @author Trevor Stevens
  * @author Brian Schreiber
  */
 public class Formula {
 
     private Scanner sc;
     private Stack<Boolean> booleanStack;
     private Stack<HashObject> hashObjectStack;
     private float rankArray[][];
     private double powArray[];
     private Object clauseList[];
     private HashMap<Integer, HashObject> hashMap;
     private HashObject hashObj;
     private int numVariables,numClauses,key,unitVar;
     private int shift = 0;
     private boolean clauseSizeZeroResult;
     private boolean justBackTracked = false;
     /**
      * Instantiates a new formula.
      *
      * @param fileName the file name
      */
     Formula(String fileName) {
         importCNF(fileName);
         rankArray = new float[2][numVariables];
         booleanStack = new Stack<Boolean>();
         hashObjectStack = new Stack<HashObject>();
         populateHashMap();
         rankVariables();
     }
 
     /**
      * Import cnf file and store clauses in
      * clauseList.
      *
      * @param fileName the file name
      */
     private void importCNF(String fileName) {
         int clause, i, nextVar, size;
         int tmp[];
         try {
             sc = new Scanner(new File(fileName));
         } catch (FileNotFoundException e) {
             //e.printStackTrace();
             System.exit(1);
         }
 
         while (sc.findInLine("p cnf") == null) {
             sc.nextLine();
         }
 
         sc.findInLine("p cnf");
         numVariables = sc.nextInt();
         numClauses = sc.nextInt();
         clauseList = new Object[numClauses];
         ArrayList<Integer> list = new ArrayList<Integer>(numVariables/4);
 
         /*
          * Populate the clause list
          */
         for (clause = 0; sc.hasNextInt(); clause++) {
             nextVar = sc.nextInt();
             while (nextVar != 0) {
                 list.add(nextVar);
                 if (nextVar == 0) {
                     break;
                 }
                 nextVar = sc.nextInt();
             }
             size = list.size();
             tmp = new int[size];
             for (i = 0; i < size; i++) {
                tmp[i] = list.get(i);
             }
             clauseList[clause] = new Clause(size, tmp );
             list.clear();
         }
         sc.close();
     }
 
     /**
      * Populate the hash map.
      */
     private void populateHashMap() {
         hashMap = new HashMap<Integer, HashObject>(numVariables);
         Clause clauseAtI;
         HashObject hashTmp;
         int clauseVar,clauseVarKey,i,j;
         HashObject prevHashObj;
         int clauseAtISize;
         for (i = 0; i < numClauses; i++) {
             clauseAtI = (Clause) clauseList[i];
             clauseAtISize = clauseAtI.size();
             for (j = 0; j < clauseAtISize; j++) {
                 clauseVar = clauseAtI.get(j);
                 clauseVarKey = Math.abs(clauseVar); //abs of variable for key
                 prevHashObj = hashMap.get(clauseVarKey);
                 if (prevHashObj == null) {
                     hashTmp = new HashObject();
                     hashTmp.variableNumber(clauseVarKey);
                     hashMap.put(clauseVarKey, hashTmp);
                     prevHashObj = hashTmp;
                 }
                 if (clauseVar > 0) {
                     prevHashObj.addClausePos(clauseAtI);
                 } else {
                     prevHashObj.addClauseNeg(clauseAtI);
                 }
             }
         }
     }
 
     /**
      * Fill pow array, with a few of
      * 2^n powers.
      */
     private void fillPowArray(int size) {
         powArray = new double[size];
         for (int i = 0; i < size; i++) {
             powArray[i] = Math.pow(2, i * -1);
         }
     }
 
     /**
      * Rank the variables for the
      * first time.
      */
     private void rankVariables() {
         int clength,size,i,j;
         float sum = 0;
         fillPowArray(30);
         int powLength = powArray.length;
 
         for (i = 1; i <= numVariables; i++) {     // Creates List
             hashObj =  hashMap.get(i);
             if (hashObj != null) {
                 size = hashObj.posSize();
                 for (j = 0; j < size; j++) {        // Sums the rank in the posList
                     Clause tmpClause = hashObj.getP(j);
                     clength = tmpClause.size();
                     if (clength < powLength) {
                         sum += powArray[clength];
                     } else {
                         sum += Math.pow(2, (clength * -1));
                     }
                 }
                 size = hashObj.negSize();
                 for (j = 0; j < size; j++) {        // Sums the rank in the negList
 
                     clength = hashObj.getN(j).size();
                     if (clength < powLength) {
                         sum += powArray[clength];
                     } else {
                         sum += Math.pow(2, (clength * -1));
                     }
                 }
                 rankArray[0][i - 1] = i;            // Stores the Variable in the first column
                 rankArray[1][i - 1] = sum;          // Stores the Ranking in the second column
                 sum = 0;
             }
 
         }
 
         mergeSort();
     }
 
     /**
      * Re-rank variables.
      */
     public void reRankVariables() {
         Clause tmpClause;
         boolean swapLargest = false;
         int clength, i, currentMaxKey, absOne, unitKey;
         int maxValueKey = -1;
         float currentMaxRank;
         float sum = 0;
         double maxValue = 0;
         double checkValue = 0;
         int powLength = powArray.length;
 
         unitKey = lengthOneCheck(); // returns numVariables when none found.
         int pSize, nSize, bigger, s;
         if (unitKey == numVariables) {
             unitVar = 0;
             for (i = shift; i < numVariables; i++) {
                 hashObj = (hashMap.get((int) rankArray[0][i]));
                 if (hashObj == null) {
                     //rankArray[1][i] = 0; //not sure if good/bad
                     continue;
                 }
                 pSize = hashObj.posSize();
                 nSize = hashObj.negSize();
                 bigger = nSize < pSize ? pSize : nSize;
                 for (s = 0; s < bigger; s++) {
                     if (s < pSize) {
                         clength = hashObj.getP(s).size();
                         sum += (clength < powLength) ? powArray[clength] : Math.pow(2, (clength * -1));
                     }
                     if (s < nSize) {
                         clength = hashObj.getN(s).size();
                         sum += (clength < powLength) ? powArray[clength] : Math.pow(2, (clength * -1));
                     }
                 }
                 
                if(maxValue < sum ){
                     maxValueKey = i;
                     maxValue = sum;
                     swapLargest = true;
                 }
 
                 rankArray[1][i] = sum;          // Stores the Ranking in the second column
                 sum = 0;
             }
         } else {
             maxValueKey = Math.abs(unitKey);
            unitVar = (unitKey < 0 ) ? (int) rankArray[0][maxValueKey]*-1 : (int) rankArray[0][maxValueKey];
             maxValue = rankArray[0][maxValueKey];
             swapLargest = true;
         }
         
         //Switch the maxValueKey to the shift position
         currentMaxKey = (int) rankArray[0][shift];
         currentMaxRank = rankArray[1][shift];
         rankArray[0][shift] = rankArray[0][maxValueKey];
         rankArray[1][shift] = rankArray[1][maxValueKey];
 
         rankArray[0][maxValueKey] = currentMaxKey;
         rankArray[1][maxValueKey] = currentMaxRank;
     }
 
     /**
      * Forward tracks down the tree.
      */
    public void forwardTrack() {
        reRankVariables();
        Clause clause;
        HashObject nextVarObj;
        boolean booleanValue;
        int var, absKey, actualSize, j, i, opsitListSize, listSize, varNeg;
        if (unitVar != 0) {
            var = unitVar;
            absKey = Math.abs(var);
            nextVarObj =  hashMap.get(absKey);
            hashMap.remove(absKey);
        } else {
            var = (int) rankArray[0][shift];
            absKey = Math.abs(var);
            nextVarObj =  hashMap.get(absKey);
            hashMap.remove(absKey);
        }
        /*
         * This statement determines whether
         * to branch true or false by checking if it has
         * just back tracked or not.
         */
        booleanValue = (!justBackTracked && var > 0) ? true : false;  //
        var = Math.abs(var); // always positive: p or n
        varNeg = booleanValue ? var * -1 : var; //flip for negitive: pos * -1 = n : neg * -1 = p
        if (booleanValue) {
            listSize = nextVarObj.posSize();
            opsitListSize = nextVarObj.negSize();
        } else {
            listSize = nextVarObj.negSize();
            opsitListSize = nextVarObj.posSize();
        }
 
        for (i = 0; i < listSize; i++) {
            clause = (booleanValue) ? (Clause) nextVarObj.getP(i) : (Clause) nextVarObj.getN(i);
            actualSize = clause.actualSize();
            for (j = 0; j < actualSize; j++) {
                key = clause.get(j);
                absKey = Math.abs(key);
                if (key != 0 && absKey != var) {
                    hashObj = (HashObject) hashMap.get(absKey);
                    if (hashObj != null) {
                        hashObj.removeClause(clause);
                    }
                }
            }
        }
 
        /*
         * This loop removes all varNeg occurrences
         * from all clauses in clauseList.
         */
        for (i = 0; i < opsitListSize; i++) {
           ((booleanValue) ? nextVarObj.getN(i) : nextVarObj.getP(i)).removeVar(varNeg);
        }
 
        hashObjectStack.push(nextVarObj);
        booleanStack.push(booleanValue);
        justBackTracked = false;
        shift++;
    }
 
     /**
      * Back tracks up the tree.
      */
     public void backTrack() throws EmptyStackException {
         //  Reduce runtime overhead && clean up code
         while (!booleanStack.pop()) {
             shift--;
             rePopulate((int) rankArray[0][shift], hashObjectStack.pop(), false);
         }
         shift--;
         rePopulate((int) rankArray[0][shift], hashObjectStack.pop(), true);
         justBackTracked = true;
     }
 
     /**
      * Re-populate the hashMap
      *
      * @param key the key
      * @param rePopObj the object being repopulated
      * @param varSetTo the boolean value of the object
      */
     private void rePopulate(int key, HashObject rePopObj, boolean varSetTo) {
         Clause clause;
         int var, negkey, rVarSize, rClauseSize, i, j, actualSize;
         if (varSetTo) {
             negkey = key * -1;
             rVarSize = rePopObj.negSize();
             rClauseSize = rePopObj.posSize();
             for (i = 0; i < rClauseSize; i++) {
                 clause = rePopObj.getP(i);
                 actualSize = clause.actualSize();
                 for (j = 0; j < actualSize; j++) {
                     var = clause.get(j);
                     if (var != 0) {
                         hashObj = hashMap.get(Math.abs(var));
                         if (hashObj != null){
                           if(var > 0) {
                               hashObj.addClausePos(clause);
                           } else {
                               hashObj.addClauseNeg(clause);
                           }
                         } 
                     }
                 }
             }
             for (i = 0; i < rVarSize; i++) {
                 rePopObj.getN(i).addVar(negkey);
             }
             hashMap.put(key, rePopObj);
         } else {
             negkey = key;
             rVarSize = rePopObj.posSize();
             rClauseSize = rePopObj.negSize();
             for (i = 0; i < rClauseSize; i++) {
                 clause = rePopObj.getN(i);
                 actualSize = clause.actualSize();
                 for (j = 0; j < actualSize; j++) {
                     var = clause.get(j);
                     if (var != 0) {
                         hashObj = hashMap.get(Math.abs(var));
                         if (hashObj != null){
                            if( var > 0) {
                              hashObj.addClausePos(clause);
                            } else {
                              hashObj.addClauseNeg(clause);
                            }
                         }
                     }
                 }
             }
             for (i = 0; i < rVarSize; i++) {
                 rePopObj.getP(i).addVar(negkey);
             }
             hashMap.put(key, rePopObj);
         }
     }
 
     /**
      * Check for if a clause is size zero.
      *
      * @return true, if successful
      */
     public boolean clauseSizeZero() {
         int length = clauseList.length;
         for (int i = 0; i < length; i++) {
             if (((Clause) clauseList[i]).size() == 0) {
                 clauseSizeZeroResult = true;
                 return clauseSizeZeroResult;
             }
         }
         clauseSizeZeroResult = false;
         return clauseSizeZeroResult;
     }
 
     /**
      * Returns result of last method call to clauseSizeZero
      * @return clauseSizeZero last result
      * @see clauseSizeZero()
      */
 
     public boolean getLastClauseSizeResult() {
         return clauseSizeZeroResult;
     }
 
     /**
      * Check if there it is a valid solution.
      *
      * @return true, if successful
      */
     public boolean validSolution() {
         return ( !clauseSizeZero() && (hashMap.isEmpty() || allEmptyKeyMap())); // ? true : false;
     }
 
     /**
      * Check to see if all keys in hashMap are
      * empty.
      *
      * @return true, if successful
      */
     private boolean allEmptyKeyMap() {
         int i;
         HashObject tmp;
         for (i = shift; i < numVariables; i++) {
             tmp = hashMap.get((int) rankArray[0][i]);
             if (tmp != null && (!tmp.posEmpty() || !tmp.negEmpty())) {
                 return false;
             }
         }
         return true;
     }
     /**
      * Finds and returns highest ranked unit variable
      * @return
      */
     private int lengthOneCheck() {
         HashObject tmp;
         int tmpVar, size, i, k;
 
         for (k = shift; k < numVariables; k++) {
             tmp = hashMap.get((int) rankArray[0][k]);
             if (tmp != null) {
                 size = tmp.posSize();
                 for (i = 0; i < size; i++) {
                     tmpVar = tmp.getP(i).lengthOne();
                     if (tmpVar != 0) {
                         return k;
                     }
                 }
                 size = tmp.negSize();
                 for (i = 0; i < size; i++) {
                     tmpVar = tmp.getN(i).lengthOne();
                     if (tmpVar != 0) {
                         return k*-1;
                     }
                 }
             }
         }
         return numVariables;
     }
 
     /**
      * Merge sort.
      */
     private void mergeSort() {
         float temp[][] = new float[2][numVariables];
         mergeSort(temp, 0 + shift, numVariables - 1);
     }
 
     /**
      * Merge sort.
      *
      * @param temp the temp Array
      * @param lowerBound the lower bound
      * @param upperBound the upper bound
      */
     private void mergeSort(float temp[][], int lowerBound, int upperBound) {
         if (lowerBound == upperBound) {
             return;                   // If index == 1 do nothing
         } else {
             int mid = (lowerBound + upperBound) / 2;    // Get midpoint
 
             mergeSort(temp, lowerBound, mid);     // Lower Half
 
             mergeSort(temp, mid + 1, upperBound);     // Upper Half
 
             merge(temp, lowerBound, mid + 1, upperBound); // Merge both Halves
 
         }
     }
 
     /**
      * Merge.
      *
      * @param temp the temp array
      * @param lower the lower
      * @param highMid the high mid
      * @param upperBound the upper bound
      */
     private void merge(float temp[][], int lower, int highMid, int upperBound) {
         int i = 0;              // temp index
 
         int lowerBound = lower;       // saves lower value
 
         int lowMid = highMid - 1;       // sets the lower mid point
 
         int n = upperBound - lowerBound + 1;  // number of ints in range
 
         while (lower <= lowMid && highMid <= upperBound) {
             if (rankArray[1][lower] > rankArray[1][highMid]) {
                 temp[0][i] = rankArray[0][lower];
                 temp[1][i++] = rankArray[1][lower++];
             } else {
                 temp[0][i] = rankArray[0][highMid];
                 temp[1][i++] = rankArray[1][highMid++];
             }
         }
 
         while (lower <= lowMid) {
             temp[0][i] = rankArray[0][lower];
             temp[1][i++] = rankArray[1][lower++];
         }
 
         while (highMid <= upperBound) {
             temp[0][i] = rankArray[0][highMid];
             temp[1][i++] = rankArray[1][highMid++];
         }
 
         for (i = 0; i < n; i++) {
             rankArray[1][lowerBound + i] = temp[1][i];
             rankArray[0][lowerBound + i] = temp[0][i];
         }
     }
 
     /**
      * Print Solution Set.
      */
     public void printSolution() {
         while (!hashObjectStack.isEmpty()) {
             if (booleanStack.pop()) {
                 System.out.print(hashObjectStack.pop().getVariableNumber()+" ");
             } else {
                 // If false negate variable
                 System.out.print(hashObjectStack.pop().getVariableNumber()*-1+" ");
             }
         }
     }
 }
