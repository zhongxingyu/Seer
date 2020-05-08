 package ll1;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import sg.fa.Transition;
 
 public class RuleSet
 {
     private HashMap<String, Rule> rules;
     private HashMap<String, ArrayList<String>> firstSets;
     
     
     public RuleSet(){
         this.rules = new HashMap<String, Rule>();
         this.firstSets = new HashMap<String, ArrayList<String>>();
     }
     
     public void addRule(Rule r){
         if(this.rules.keySet().contains(r.getLHS())){
             this.rules.get(r.getLHS()).merge(r);
         } else {
             this.rules.put(r.getLHS(), r);
         }
     }
     
     public void init(String filename) throws Exception {
         BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
         String s ;
         while(null != (s=br.readLine())){
             Rule r = new Rule();
             r.init(s);
             this.addRule(r);
         }
         br.close();
     }
     
     public String toString(){
         StringBuilder sb = new StringBuilder();
         for(String s : rules.keySet()){
             sb.append(this.rules.get(s).toString() + "\n");
         }
         return sb.toString();
     }
     
     public static void main(String[] args) throws Exception {
         RuleSet rs = new RuleSet();
        rs.init("SampleGrammer3.txt");
         System.out.println(rs);
       
         rs.generateFirstSets();
         rs.printFirstSets();
     }
     
     public void generateFirstSets(){
     	
     	for (String rl: rules.keySet()){
     		Rule current = rules.get(rl);
     		int k = 0;
     		while(k < current.getRHS().size()){
     			ArrayList<String> ruleK = current.getRHS().get(k);
     			int c = 0;
     			while(c < ruleK.size()){
     				ArrayList<String> firsts = first(ruleK.get(c));
     				if(!firsts.contains(""+Transition.EPSILON)){
 //    					System.out.println("No epsilons");
 //    					printArray(firsts);
     					current.addToFirst(firsts);
     					break;
     				}else{
 //    					System.out.println("Contains epsilons");
     					firsts = removeAllEpsilon(firsts);
     					current.addToFirst(firsts);
     					c++;
     				}
     			}
     			if(c>=ruleK.size()){
     				current.addToFirst(""+Transition.EPSILON);
     			}
     			k++;
     		}
     	}
     	    	
     }
     
     public ArrayList<String> removeAllEpsilon(ArrayList<String> input){
     	while(input.contains(""+Transition.EPSILON)){
     		input.remove(""+Transition.EPSILON);
     	}
     	return input;
     }
     
     public ArrayList<String> first(String x){
     	ArrayList<String> toReturn = new ArrayList<>();
     	if(!Rule.isRule(x)){
     		toReturn.add(x);
     		return toReturn;
     	}
     	Rule temp = this.rules.get(x);
    	if(temp!= null){
	    	for(ArrayList<String> s: temp.getRHS()){
	    		toReturn.addAll(first(s.get(0)));
	    	}
     	}
     	
     	return toReturn;
     }
     
     public void printFirstSets(){
     	for(String rl : this.rules.keySet()){
     		this.rules.get(rl).printFirstSet();
     	}
     }
     
     public void printArray(ArrayList<String> input){
     	System.out.print("{");
     	for(String s: input){
     		System.out.print(" "+s+" ");
     	}
     	System.out.println("}");
     }
     
     
     
     
 }
