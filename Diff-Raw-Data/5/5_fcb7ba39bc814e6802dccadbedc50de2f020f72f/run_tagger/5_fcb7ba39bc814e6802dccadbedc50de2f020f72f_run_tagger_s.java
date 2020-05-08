 import java.io.*;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Map.Entry;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Stack;
 
 class run_tagger {
     
     // nested class for viterbi algorithm
 	static class TrellisNode {
 	    public String tag;
 	    public double prob;
 	    public TrellisNode backPtr;
 	}
 	
 	// nested class for Hidden Markov Model
 	static class HMM {
 	    public ArrayList<String> tagList;
 	    public ArrayList<String> wordList;
 	    public Map<String, Map<String, Integer>> tMatrix;
         public Map<String, Map<String, Integer>> eMatrix;
 	}
     
 	public static void main(String args[]) {
 	    if (args.length != 3) {
 	        System.out.println("error: Wrong number of arguments.");
 	        System.out.println("usage: java run_tagger <sents.test> <model_file> <sents.out>");
 	        System.exit(1);
         }
         // take in params
         String testFile = args[0];
         String modelFile = args[1];
         String outFile = args[2];
         
         
         try {
             // load HMM
             FileInputStream modelStream = new FileInputStream(modelFile);
             DataInputStream modelDataIn = new DataInputStream(modelStream);
             BufferedReader modelBr = new BufferedReader(new InputStreamReader(modelDataIn));
             HMM hmm = new HMM();
             
              // read input            
             FileInputStream testStream = new FileInputStream(testFile);
             DataInputStream testDataIn = new DataInputStream(testStream);
             BufferedReader testBr = new BufferedReader(new InputStreamReader(testDataIn));
             
             // prepare output file
             FileWriter foStream = new FileWriter(outFile);
             BufferedWriter outBw = new BufferedWriter(foStream);
             
             // process input and output results from viterbi algorithm
             String currLine;
             while ((currLine = testBr.readLine()) != null) {
                 // break each line into an array of tokens
                 String[] tokens = currLine.trim().split("\\s+");
                 ArrayList<String> tags = Vite(tokens, hmm);
                 
                 // write to out file
                 String outputLine = "";
                 for (int i = 0; i < tokens.length; i++) {
                    outputLine += tokens[i] + "/" + tags.get(i);
                 }
                outBw.write(outputLine + "\n");
             }
             
             modelBr.close();
             testBr.close();
             outBw.close();
         } catch (Exception e) {
             System.err.println("Error: " + e.getMessage());
         }
 	}
 	
 	// Implementaion of viterbi algorithm
 	public static ArrayList<String> Vite(String[] words, HMM hmm) {
 	    // retrieve model paramenters
 	    ArrayList<String> tagList = hmm.tagList;
 	    ArrayList<String> wordList = hmm.wordList;
 	    Map<String, Map<String, Integer>> TMatrix = hmm.tMatrix;
         Map<String, Map<String, Integer>> EMatrix = hmm.eMatrix;
         
         // set up a trellis while calculating the forward probabilities
         ArrayList<ArrayList<TrellisNode>> trellis = new ArrayList<ArrayList<TrellisNode>>();
         // keep track the max probability for a column
         TrellisNode maxTerminalNode = null;
         for (int i = 0; i < words.length; i++) {
             // set up a column in trellis
             ArrayList<TrellisNode> col = new ArrayList<TrellisNode>();
             for (String tag : tagList) {
                 TrellisNode tNode = new TrellisNode();
                 tNode.tag = tag;
                 tNode.backPtr = (i == 0) ? null : nodeWithMaxPrevTimesTran(trellis.get(i - 1), tNode.tag, TMatrix);
                 tNode.prob = (i == 0) ? EMatrix.get(tag).get(words[i]) : tNode.backPtr.prob * TMatrix.get(tNode.backPtr.tag).get(tag) * EMatrix.get(tag).get(words[i]);
                 col.add(tNode);
             }
             trellis.add(col);
             // reset max terminal node is not the last  column
             if (i == words.length - 1) {
                 maxTerminalNode = nodeWithMaxPrevTimesTran(trellis.get(i - 1), "</s>", TMatrix);
             }
         }
         
         // backtrace from max node
         Stack<String> optimalPathStack = new Stack<String>();
         TrellisNode currNode = maxTerminalNode;
         while(currNode != null) {
             optimalPathStack.push(currNode.tag);
             currNode = currNode.backPtr;
         } 
         // pop stack to constrct the tag list in order
         ArrayList<String> optimalPath = new ArrayList<String>();
         while (!optimalPathStack.empty()) {
             optimalPath.add(optimalPathStack.pop());
         }
 	    
 	    return optimalPath;
 	}
 	
 	public static TrellisNode nodeWithMaxPrevTimesTran(ArrayList<TrellisNode> col, String transToTag, Map<String, Map<String, Integer>> TMatrix) {
 	    TrellisNode maxNode = null;
 	    for (TrellisNode node : col) {
             if (maxNode == null)
                 maxNode = node;
             else {
                 // assume equal probability won't update max
                 if (maxNode.prob * TMatrix.get(maxNode.tag).get(transToTag) < node.prob * TMatrix.get(node.tag).get(transToTag))
                     maxNode = node;
             }
 	    }
 	    return maxNode;
 	}
 }
