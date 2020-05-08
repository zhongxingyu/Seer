 package hmmtagger;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 /**
  *
  * @author Nikolay Borisov
  */
 public class HMMTagger {
 
     private static final int COUNT = 0;
     private static final int TYPE = 1;
     private static final int TOKEN_TYPE = 2;
     private static final int WORD = 3;
     private static final String WORDTAG = "WORDTAG";
     private static Map<String, Map<String, Double>> emissionParams = new HashMap<>(); //contains the result for e(y|x)
     private static Map<String, Integer> elementCounts = new HashMap<>();    //holds count of different NGRAMs/WORDTAGs
     private static Map<String, Double> ngramParam = new HashMap<>();   //holds q(Yi | Yi-2, Yi-1) for each ngram 
     
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws FileNotFoundException, IOException {
         
         countWords(args[0]);
         teachTagger(args[0]);
 //        System.out.println("e = " + getEmissionParameter("in", State.I_GENE));
 //        System.out.println("q = " + ngramParam.get("O I-GENE I-GENE"));
 //        System.out.println("Result: " + 8.72732885782e-7 * ngramParam.get("O I-GENE I-GENE")  * getEmissionParameter("in", State.I_GENE));
          tagFile(args[1]);
         
     }
     
     
     private static void teachTagger(String countFile) throws IOException {
         
         String line;
         Map<String, Integer> tagCount = countTags(countFile);
 
         //now we need to go through the file again and compute the 
         // emission parameters for each x|y
         BufferedReader in = new BufferedReader(new FileReader(countFile));
         
         while((line = in.readLine()) != null) {
             String tokens[] = line.split(" ");
             
             if (tokens[TYPE].equals(WORDTAG)) {
                 //compute the emission parameters for each x|y
                 Map<String, Double> m;
                 int countY = tagCount.get(tokens[TOKEN_TYPE]);
                 int observationCount = Integer.parseInt(tokens[COUNT]);
                 //added this to not overwrite a certain hashmap
                 if (emissionParams.get(tokens[WORD]) != null) { m = emissionParams.get(tokens[WORD]); } 
                 else { m = new HashMap<>(1); }
                 
                 m.put(tokens[TOKEN_TYPE], ((double) observationCount / (double)countY));
                 emissionParams.put(tokens[WORD], m);
                 
             } else if(tokens[TYPE].equals("3-GRAM")) {
                 //compute parameters for q(Yi|Yi-2, Yi-1)
                 int divident = elementCounts.get(getNgramId(tokens));
                 int divisor = elementCounts.get(tokens[2] + " " + tokens[3]);
                 ngramParam.put(getNgramId(tokens), ((double) divident / (double) divisor));
                 
             }  
         }
         
         in.close();
     }
     
     /**
      * Count the number of times a particular tag is seen in the data set
      * @param input
      * @return
      * @throws FileNotFoundException
      * @throws IOException 
      */
     private static Map<String, Integer> countTags(String input) throws FileNotFoundException, IOException {
         String line;
         Map<String, Integer> tagCount = new HashMap<>();
 
         BufferedReader in = new BufferedReader(new FileReader(input));
         
         while ((line = in.readLine()) != null) {
             String tokens[] = line.split(" ");
             if (tokens[TYPE].equals(WORDTAG)) {
                 if (tagCount.get(tokens[TOKEN_TYPE]) == null) {
                     tagCount.put(tokens[TOKEN_TYPE], Integer.parseInt(tokens[COUNT]));
                 } else {
                     int oldCount = tagCount.get(tokens[TOKEN_TYPE]);
                     tagCount.put(tokens[TOKEN_TYPE],  oldCount + Integer.parseInt(tokens[COUNT]));
                 }
             }
         }
         
         in.close();
 
         return tagCount;
     }
    
     /**
      * Prases the count file into a hashmap, making it convenient to work 
      * with the data
      * @param countFile
      * @throws FileNotFoundException
      * @throws IOException 
      */
     private static void countWords(String countFile) throws FileNotFoundException, IOException {
 
         String line;
         BufferedReader in = new BufferedReader(new FileReader(countFile));
 
         while ((line = in.readLine()) != null) {
             String elements[] = line.split(" ");
 
             if (elements[TYPE].contains("GRAM")) {
                 //WE HAVE an NGRAM ENTRY
                 elementCounts.put(getNgramId(elements), Integer.parseInt(elements[COUNT]));
             } else {
                 //we have a WORDTAG entry 
                 if (elementCounts.get(elements[WORD]) == null) {
                     elementCounts.put(elements[WORD], Integer.parseInt(elements[COUNT]));
                 } else {
                     int oldCount = elementCounts.get(elements[WORD]);
                     elementCounts.put(elements[WORD], oldCount + Integer.parseInt(elements[COUNT]));
                 }
             }
         }
         in.close();
     }
     
     /**
      * Utility function to get the ngram id out of an string array
      * @param elements
      * @return 
      */
     private static String getNgramId(String[] elements) {
         String ngram = elements[2];
         for (int i = 3; i < elements.length; i++) {
             ngram += " " + elements[i];
         }
         
         return ngram;
     }
     
     /**
      * Used for part 1 of the assignment
      * @param x
      * @return
      * @throws IOException 
      */
     public static String getMaxEmissionParameter(String x) throws IOException {
         Map<String, Double> m;
         double bestScore = -1;
         String bestType = "";
         
         m = (elementCounts.get(x) == null || elementCounts.get(x) < 5) ? emissionParams.get("_RARE_") : emissionParams.get(x);
 
         //find the highest scoring tag
         for (Entry<String, Double> candidate : m.entrySet()) {
             if (candidate.getValue() > bestScore) {
                 bestScore = candidate.getValue();
                 bestType = candidate.getKey();
             }
         }
         
         return bestType;
     }
     
     public static double getEmissionParameter(String x, State state) throws IOException {
         Map<String, Double> m;
        double score = 0;
 
         m = (elementCounts.get(x) == null || elementCounts.get(x) < 5) ? emissionParams.get("_RARE_") : emissionParams.get(x);
 
         //find the highest scoring tag
         for (Entry<String, Double> candidate : m.entrySet()) {
             if (candidate.getKey().equals(state.getName())) {
                 score = candidate.getValue();
             }
         }
 
         return score;
     }
     
     public static void tagFile(String inputFile) throws IOException {
 
         BufferedReader in = new BufferedReader(new FileReader(inputFile));
         BufferedWriter out = new BufferedWriter(new FileWriter(inputFile + ".tagged"));
         List<String> sentence = new ArrayList<>(15);
         State[] tags = {State.I_GENE, State.O };
         String line;
         while ((line = in.readLine()) != null) {
             // read a sentence
             if (!line.equals("")) {
                 sentence.add(line);
             } else {
                 int[] res = viterbi(sentence, tags);
 
                 for (int i = 0; i < res.length; i++) {
                     out.write(sentence.get(i) + " " + State.getStateFromId(res[i]).getName());
                     out.newLine();
                 }
                 out.newLine();
                 sentence.clear();
             }
         }
         in.close();
         out.close();
     }
     
     /**
      * runs the viterbi algorithm. The transition probabilities and 
      * emission probabilities are global static vars 
      * @param sentence - a list of sentence to tag
      * @param tags - possible tags
      * @return 
      */
     private static int[] viterbi(List<String> sentence, State[] tags) throws IOException {
         int[] result = new int[sentence.size()];
         int[][][] backpointer = new int[sentence.size()][tags.length][tags.length];
         for (int i = 0; i < sentence.size(); i++) {
             for (int j = 0; j < tags.length; j++) {
                 for (int k = 0; k < tags.length; k++) {
                     backpointer[i][j][k] = -1;
                 }                
             }
         }
         
         double[][][] Pi = new double[sentence.size()][tags.length][tags.length];
         
        boolean debug = false;
         /*
          * 
          * make Pi[0][all][all] to be all 1
          * then from 1 to N+1 will be the actual words
          */
         System.out.println("Sentence has " + sentence.size() + " words");
         //for every word
         for (int k = 0; k < sentence.size(); k++) {
             //for each state U
             for (int u = 0; u < tags.length; u++) {
                 //for each state V
                 for (int v = 0; v < tags.length; v++) {
                     if (debug) System.out.println("k = " + k + " U = "  + u + " V = " + v);
                     Pi[k][u][v] = findW(Pi, backpointer, sentence, k, u , v, debug); //call a function which will search for all allowed states at k - 1
                 }
             }
         }
         
         System.out.println("=======================");
         double maxProb = -1;
         for (int u = 0; u < tags.length; u++) {
             for (int v = 0; v < tags.length; v++) {
                 System.out.println(" U = "  + u + " V = " + v);
                 double currProb = Pi[sentence.size() - 1][u][v] * ngramParam.get(tags[u].getName() + " " + tags[v].getName() + " STOP");
                 System.out.println(" Calculating Pi[" + (sentence.size() - 1) + "," + tags[u].getName() + "," + tags[v].getName() +"] * q(STOP|" + tags[u].getName() + "," + tags[v].getName()+ ") = " + currProb);
                 if (currProb > maxProb) {
                     result[result.length - 2] = u;
                     result[result.length - 1] = v;
                     maxProb = currProb;
                 }
             }
         }
         
         System.out.println("Taken max end sentence probability = " + maxProb);
         
         for (int k = result.length - 3; k >= 0; k--) {
             int Yk1 = result[k+2];
             int Yk2 = result[k+1];
             result[k] = backpointer[k+2][Yk1][Yk2];
         }
         
         return result;
     }
     
     
     private static double findW(double[][][] Pi, int[][][] bp, List<String> sentence, int k, int u, int v, boolean debug) throws IOException {
 
         double maxProb = -1;
         short argMax = -1;
         
         for (short w = 0; w < State.getStateSize(); w++) {
             double prevProbability;
             double qParam;
             double e;
             
             //for the first word we always assume the previous probaility is 1 
             if (k == 0) {
                 prevProbability = 1;
                 qParam = ngramParam.get("* * " + State.getStateFromId(v).getName());
                 e = getEmissionParameter(sentence.get(k), State.getStateFromId(v));
                 maxProb = prevProbability * qParam * e;
                 if (debug) {
                     System.out.println(" Pi[0, *, *] = 1" );
                     System.out.println(" q(" + v + "|*, *) = " + qParam);
                     System.out.println(" e("+ sentence.get(k) + " | " + State.getStateFromId(v)+ ") = " + getEmissionParameter(sentence.get(k), State.getStateFromId(v)));
                     System.out.println(" Calculating Pi[0, *, *] * q(" + v + "|*, *) * e("+ sentence.get(k) + " | " + State.getStateFromId(v)+ ")");
                 }
                 break;
             } else if (k == 1) {
                 prevProbability = Pi[k - 1][w][u];
                 String ngram = "* " + State.getStateFromId(u).getName() + " " + State.getStateFromId(v).getName();
                 qParam = ngramParam.get(ngram);
                 e = getEmissionParameter(sentence.get(k), State.getStateFromId(v));
                 maxProb = prevProbability * qParam * e;
                 if (debug) {
                     System.out.println(" q(" + v + "|*, " + State.getStateFromId(u).getName() + ") = " + qParam );
                     System.out.println(" e("+ sentence.get(k) + " | " + State.getStateFromId(v) + ") = " + getEmissionParameter(sentence.get(k), State.getStateFromId(v)));
                     System.out.println(" Pi[" + (k - 1) + ", *," +  State.getStateFromId(u).getName() + "] = " + prevProbability);
                     System.out.println(" Calculating Pi[" + (k - 1) + ", *," +  State.getStateFromId(u).getName() + "] * q(" + v + "|*, " + State.getStateFromId(u).getName() + ") * e("+ sentence.get(k) + " | " + State.getStateFromId(v) + ")");
                 }
                 break;
             }
                 
             prevProbability = Pi[k - 1][w][u];
             e = getEmissionParameter(sentence.get(k), State.getStateFromId(v));
             qParam = ngramParam.get(State.getStateFromId(w).getName() + " " + State.getStateFromId(u).getName() + " " + State.getStateFromId(v).getName());
             double currentProb = prevProbability * qParam * e;
             
             if (debug) {
                 System.out.println("-------------------------------------");
                 System.out.println(" Pi[" + (k - 1) + ","+  State.getStateFromId(w).getName() + "," +  State.getStateFromId(u).getName() + "] = " + prevProbability);
                 System.out.println(" q(" + State.getStateFromId(w).getName() + "|" + State.getStateFromId(u).getName() + ", " + State.getStateFromId(v).getName() + ") = " + qParam);
                 System.out.println(" e("+ sentence.get(k) + " | " + State.getStateFromId(v) + ") = " + getEmissionParameter(sentence.get(k), State.getStateFromId(v)));
                 System.out.println(" For W = " + State.getStateFromId(w).getName() + " Calculating Pi[" + (k - 1) + ","+  State.getStateFromId(w).getName() + "," +  State.getStateFromId(u).getName() + "] * q(" + State.getStateFromId(v).getName() + "|" + State.getStateFromId(w).getName() + ", " + State.getStateFromId(u).getName() + ") * e("+ sentence.get(k) + " | " + State.getStateFromId(v) + ") = " + currentProb);
             }    
             
             if (currentProb > maxProb) {
                 maxProb = currentProb;
                 bp[k][u][v] = w;
                 argMax = w;
             }
         }
 
         if (debug) {
             System.out.println();
             if (argMax < 0) {
                 System.out.println(" Taken max probability = " + maxProb);
             } else {
                 System.out.println(" Taken max probability = " + maxProb + " => BP[" + k + "," + State.getStateFromId(u).getName() + "," + State.getStateFromId(v).getName() + "] = " + State.getStateFromId(argMax).getName());
             }
             
             System.out.println("==========================================");
         }
         return maxProb;
     }
 }
 
