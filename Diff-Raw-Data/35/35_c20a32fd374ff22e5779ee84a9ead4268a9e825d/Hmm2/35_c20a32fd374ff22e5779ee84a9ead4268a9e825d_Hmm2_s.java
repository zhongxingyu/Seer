 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author Yuri
  */
 public class Hmm2 {
     
     
     // Read the lines
     public static void main(String[] args) throws IOException {
         HMM hmm = new HMM();
         ArrayList<String> b = new ArrayList<>();
 
         String line;
 
         BufferedReader br = new BufferedReader(
                 new InputStreamReader(System.in));
 
         while (!br.ready());
         while (br.ready()) {
             line = br.readLine();
             b.add(line);
         } // End while
 
         hmm.parseTransition(b.get(0));
         hmm.parseEmission(b.get(1));
         hmm.parseInitial(b.get(2));
         hmm.parseSequence(b.get(3));
         
 //        double prob = hmm.getForwardProb();
 //        System.out.println(prob);
 //        double prob2 = hmm.getBackWardProb();
 //        System.out.println(prob2);
 //        
 //        Viterbi test = new Viterbi(hmm);
 //        ArrayList<Integer> test2 = test.getStateSeq(hmm.sequence);
 //        
 //        StringBuilder newB = new StringBuilder();
 //        for(int i : test2) {
 //            newB.append(i);
 //            newB.append(" ");
 //        }
 //        System.out.println(newB.toString().trim());
         
        //hmm.forwardProc();
        //hmm.backwardProc();
        //System.out.println(hmm.test());
        //double oldlogprob = Double.NEGATIVE_INFINITY;
        //double logprob = hmm.test();
        
         
        for(int i=0; i<500; i++) {
            
            hmm.train();
            
        }
         
         
         //System.out.println(hmm.test());
         
         
 //        prob = hmm.getForwardProb();
 //        System.out.println(prob);
         System.out.println(hmm.printTransition());
         System.out.println(hmm.printEmission());
 
 
     }
     
 }
