 import java.util.ArrayList;
 import java.util.Collections;
 
 
 public class Apriori
 {
 	//first pass F1:= {{i}|i e I; support({i})>= minSup);
 
 	public static ArrayList<Vector> AprMainLoop(ArrayList<Vector> vec, double minSupp)
 	{
         ArrayList<Vector> answer = new ArrayList<Vector>();
 
         //get frequent itemset size of 1
         ArrayList<Item> freqItem = firstPass(vec, minSupp);
         //System.out.println("Frequent itemset size of 1 are: "+freqItem.toString());
 
         int largestItemSetSize = freqItem.size();
         //System.out.println("Largest item set size: "+largestItemSetSize);
 
         //make a item group of 1 from frequent item lists
         ArrayList<ArrayList<Item>> itemGroups = new ArrayList<ArrayList<Item>>();
         for(int itemIdx = 0; itemIdx < freqItem.size(); itemIdx++){
             ArrayList<Item> temp = new ArrayList<Item>();
             temp.add(freqItem.get(itemIdx));
             itemGroups.add(temp);
         }
 
         ArrayList<ArrayList<Item>> tempCandidate = new ArrayList<ArrayList<Item>>(itemGroups);
         //main loop
         for(int itemSize = 1; itemSize < largestItemSetSize; itemSize++){
             //candidate frequent itemsets
         	if(tempCandidate.size() == 0){
         		break;
         	}
             itemGroups = candidateGen(tempCandidate, itemSize);
 
             //initialize counts
             int [] groupFreqAr = new int[itemGroups.size()];
 
             //for each receipt
             for(int vecIdx = 0; vecIdx < vec.size(); vecIdx++){
                 //for every item number
                 for(int itemGroupsIdx = 0; itemGroupsIdx < itemGroups.size(); itemGroupsIdx++){
                     if(vecContainsItems(vec.get(vecIdx),itemGroups.get(itemGroupsIdx))){
                         groupFreqAr[itemGroupsIdx]++;
                         //System.out.println("Match at "+vec.get(vecIdx).getElement(0)+" for "+itemGroups.get(itemGroupsIdx));
                     }
                 }
             }
 
             tempCandidate = new ArrayList<ArrayList<Item>>();
             //go through the array with counts of the frequency of set of groups
             //and add it to the final return AL of vector if the item group is
             //above minSupp
             for(int groupIdx = 0; groupIdx < itemGroups.size(); groupIdx++){
                 double supp = (double)groupFreqAr[groupIdx]/(double)vec.size();
                 if(supp > minSupp){
                 	System.out.println("support for "+itemGroups.get(groupIdx)+" is "+supp);
                     Vector temp = new Vector();
                     tempCandidate.add(new ArrayList<Item>());
                     for(int itemIdx = 0; itemIdx < itemGroups.get(groupIdx).size(); itemIdx++){
                         temp.addElement(itemGroups.get(groupIdx).get(itemIdx).itemID);
                         tempCandidate.get(tempCandidate.size()-1).add(itemGroups.get(groupIdx).get(itemIdx));
                     }
                     answer.add(temp);
                 }
             }
             
 
         }
 		return answer;
 	}
 
 	private static ArrayList<Item> firstPass(ArrayList<Vector> vec, double minSupp){
 		ArrayList<Item> itemFreq = new ArrayList<Item>();
         //ArrayList level vector
 		for(int arListIndex = 0; arListIndex < vec.size(); arListIndex++)
 		{
 			//check for frequency
 			boolean contains = false;
 			int containsIndex = 0;
             //prints out the receipt ID of this vector
             //System.out.println("receipt ID:"+vec.get(arListIndex).getElement(0));
             //Vector level loop
 			for(int vecIndex = 1; vecIndex < vec.get(arListIndex).getSize(); vecIndex++)
 			{
                 //For each item in itemFreq AL
 				for(int j = 0; j< itemFreq.size(); j++)
 				{
 					if(vec.get(arListIndex).getElement(vecIndex) == itemFreq.get(j).itemID)
 					{
 						contains = true;
 						containsIndex = j;
 						break;
 					}
 				}
 				if(contains)
 				{
 					itemFreq.get(containsIndex).addOne();
 				}
 				else
 				{
 					itemFreq.add(new Item(vec.get(arListIndex).getElement(vecIndex)));
 				}
 			}
 		}
 
         int numOfReceipts = vec.size();
         //System.out.println("numOfReceipts="+numOfReceipts);
         for(int itemIdx = 0; itemIdx < itemFreq.size(); itemIdx++){
             Item tempItem = itemFreq.get(itemIdx);
             double suppVal = tempItem.freq/(double)numOfReceipts;
             //System.out.println("itemID: "+tempItem.itemID+", freq="+tempItem.freq+", supp="+suppVal);
            if(tempItem.freq/(double)numOfReceipts <= minSupp){
                 //System.out.println(tempItem.itemID+" removed with "+suppVal+" frequency");
                 itemFreq.remove(itemIdx);
                 itemIdx--;
             }
         }
        
 		return itemFreq;
 	}
 
     //takes in AL of AL of items with the item group size, spits out item group of itemSize+1
 	private static ArrayList<ArrayList<Item>> candidateGen(ArrayList<ArrayList<Item>> itemVec, int itemSize)
 	{
         ArrayList<ArrayList<Item>> answer = new ArrayList<ArrayList<Item>>();
         for(int idxOne = 0; idxOne < itemVec.size(); idxOne++){
             for(int idxTwo = idxOne+1; idxTwo < itemVec.size(); idxTwo++){
                 //can't join with it's self
                 //System.out.println(itemVec.get(idxOne).toString()+"+"+itemVec.get(idxTwo).toString());
                 ArrayList<Item> joinedItems = joinItems(itemVec.get(idxOne),itemVec.get(idxTwo));
                 //System.out.println("joinedItems="+joinedItems.toString());
                 if(joinedItems.size() == itemSize + 1){
                     if(!answer.contains(joinedItems)){
                         answer.add(joinedItems);
                     }
 
                 }
             }
         }
         System.out.println("canGen size="+answer.size()+"|"+answer);
 		return answer;
 	}
 
     private static ArrayList<Item> joinItems(ArrayList<Item> a, ArrayList<Item> b){
         ArrayList<Item> answer = new ArrayList<Item>(a);
         //for each item in AL a
         for(int bIndex = 0; bIndex < b.size(); bIndex++){
             boolean dupFound = false;
             for(int answerIndex = 0; answerIndex < answer.size(); answerIndex++){
                 //check to see if they are the same item
                 if(answer.get(answerIndex).itemID == b.get(bIndex).itemID){
                    dupFound = true;
                 }
             }
             if(!dupFound){
             	answer.add(b.get(bIndex));
             }
 
         }
 
         Collections.sort(answer);
         return answer;
     }
 
     private static boolean vecContainsItems(Vector vec, ArrayList<Item> items){
         for(int itemIdx = 0; itemIdx < items.size(); itemIdx++){
             boolean found = false;
             for(int vecIdx = 1; vecIdx < vec.getSize(); vecIdx++){
                 if(items.get(itemIdx).itemID == vec.getElement(vecIdx)){
                     found = true;
                 }
             }
             if(!found){
                 return false;
             }
         }
         return true;
     }
 
 }
