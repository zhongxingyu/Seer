 import java.util.ArrayList;
 import java.util.Iterator;
 
 
 
 public class Main {
 	public static void main(String [ ] args)
 	{
 
 		boolean showItemsets = true;
 		boolean showRules = true;
 		boolean aprioriSkyLine = true;
 		
 		
 		String fileName = "data/out1.csv";
		if(args.length == 1){
 			fileName = args[0];
 		}
 
         ArrayList<Vector> vecList = null;
         vecList = MyParser.parseSparseVectorCSV(fileName);
 
         double minSupport = 0.1;
        if(args.length == 2){
         	minSupport = Double.parseDouble(args[1]);
         }
         else{
         	System.out.println("No argument for minimum support found, using "+minSupport+" as value.");
         }
         double minConfidence = 0.1;
        if(args.length == 3){
         	minConfidence = Double.parseDouble(args[2]);
         }
         else{
         	System.out.println("No argument for minimum confidence found, using "+minConfidence+" as value.");
         }
         
         ArrayList<Vector> apriOut =  Apriori.AprMainLoop(vecList, minSupport, aprioriSkyLine);        
         ArrayList<AssociationRule> rules = genRules.genRulesMainLoop(apriOut, vecList, minConfidence);;
         
         for(int i = 0; i < apriOut.size(); i++)
         {
         	if(showItemsets)
         	{
         		System.out.println("itemset:" + apriOut.get(i).toString() + " Support = " +apriOut.get(i).support);
         	}	
         }
         for(int i = 0; i < rules.size(); i++)
         {
         	if(showRules)
         	{
         		System.out.println(" Rule:" + rules.get(i).toString());
         	}	
         }
 
 	}
 }
 
