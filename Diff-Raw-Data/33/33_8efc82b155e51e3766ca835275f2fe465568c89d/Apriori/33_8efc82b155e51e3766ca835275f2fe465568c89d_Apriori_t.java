 package algos;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import model.Algorithm;
 import model.Dataset;
 import model.HashTreeNode;
 import model.ItemSet;
 import model.MinSup;
 import model.Transaction;
 import model.aprioritid.CandidateItemset;
 import model.aprioritid.CandidateItemsetBar;
 import model.aprioritid.LargeItemset;
 import util.DBReader;
 import util.HashTreeUtils;
 import util.InputReader;
 
 import com.google.common.collect.Lists;
 
 /**
  * Implements the classical Apriori Algorithm for frequent itemset mining.
  * 
  * @author saurabh
  *
  */
 public class Apriori {
 
 	private static int MAX_K;
 	
 	public static void main(String[] args)
 	{
 		runExperiment(Dataset.T5_I2_D100K, MinSup.POINT_TWO_FIVE_PERCENT);
 	}
 	
 
 	/*
 	 * Run Apriori algorithm for the specified experiment parameters.
 	 * 
 	 * @param dataset - Dataset on which the experiment has to be run.
 	 * @param minSup  - Minimum support desired for this experiment run.
 	 * 
 	 * @return Time taken to complete this experiment.
 	 */
 	public static int runExperiment(Dataset dataset, MinSup minSup)
 	{
 		long expStartTime = System.currentTimeMillis();
 		
 		MAX_K = 400 * dataset.getAvgTxnSize();
 		
 		int minSupportCount = (int)(minSup.getMinSupPercentage() * dataset.getNumTxns())/100;
 		
 		InputReader reader = getDatasetReader(dataset);
 		
 		LargeItemset[] largeItemsets = new LargeItemset[MAX_K];
 		CandidateItemset[] candidateItemsets = new CandidateItemset[MAX_K];
 		CandidateItemsetBar[] candidateItemsetBars = new CandidateItemsetBar[MAX_K];
 		
 		candidateItemsets[1] = new CandidateItemset(MAX_K);
 		candidateItemsetBars[1] = new CandidateItemsetBar();
 		largeItemsets[1] = new LargeItemset();
 		
 		getInitialCandidateItemsets(reader, candidateItemsets[1]);
 		getInitialLargeItemsets(candidateItemsets[1], minSupportCount, largeItemsets[1]);
 		
 		print(largeItemsets[1], candidateItemsets[1].getItemsets());
 		
 		for(int k = 2; largeItemsets[k-1].getItemsetIds().size() != 0; k++)
 		{
 			candidateItemsets[k] = new CandidateItemset(MAX_K);
 			candidateItemsets[k].setItemsets(apriori_gen(candidateItemsets[k-1].getItemsets(), largeItemsets[k-1].getItemsetIds(), k - 1));
 			
 			largeItemsets[k] = generateLargeItemsets(getDatasetReader(dataset), candidateItemsets[k], minSupportCount, k);
 			
 			print(largeItemsets[k], candidateItemsets[k].getItemsets());
 		}
 		
 		long expEndTime = System.currentTimeMillis();
 		int timeTaken = (int)((expEndTime - expStartTime) / 1000); 
 		System.out.println("Time taken = " + timeTaken + " seconds.");
 		
 		return timeTaken;
 	}
 	
 	private static void print(LargeItemset l, ItemSet[] allItemsets)
 	{
 		for(Integer i : l.getItemsetIds())
 		{
 			for(Integer j : allItemsets[i].getItems())
 				System.out.print(j + " ");
 			System.out.println("- " + allItemsets[i].getSupportCount());
 		}
 	}
 	
 	private static LargeItemset generateLargeItemsets(InputReader reader, CandidateItemset candidateItemset, int minSupportCount, int currItemsetSize) {
 		
 		//System.out.println("In generateLargeItemsets().");
 		
 		HashTreeNode hashTreeRoot = HashTreeUtils.buildHashTree(candidateItemset.getItemsets(), currItemsetSize);
 		
 		while(reader.hasNextTransaction()) {
 			Transaction txn = reader.getNextTransaction();
 			List<ItemSet> candidateSetsInTrans = HashTreeUtils.findItemsets(hashTreeRoot, txn, 0);
 			for(ItemSet c : candidateSetsInTrans) {
 				c.setSupportCount(c.getSupportCount() + 1);
 			}
 		}
 		
 		LargeItemset largeItemset = new LargeItemset();
 		int index = 0;
 		for(ItemSet itemset : candidateItemset.getItemsets())
 		{
 			if(itemset == null)
 				break;
 			if(itemset.getSupportCount() >= minSupportCount)
 				largeItemset.getItemsetIds().add(index);
 			index++;
 		}
 		
 		return largeItemset;
 	}
 	
 	private static void getInitialCandidateItemsets(InputReader reader, CandidateItemset C) {
 		//System.out.println("In getInitialCandidateItemsets().");
 		SortedMap<Integer, Integer> itemset_support = new TreeMap<Integer, Integer>();
 		
 		//This loop counts support.
 		while(reader.hasNextTransaction())
 		{
 			Transaction t = reader.getNextTransaction();
 		
 			for(Integer i : t.getItems())
 			{
 				if(itemset_support.containsKey(i))
 				{
 					int support = itemset_support.get(i);
 					itemset_support.put(i, support + 1);
 				}
 				else
 				{
 					itemset_support.put(i, 1);
 				}
 			}
 		}
 		
 		//This part sorts and creates candidate itemsets.
 		SortedSet<Integer> keys = new TreeSet<Integer>(itemset_support.keySet());
 		Map<Integer, Integer> itemset_pos = new HashMap<Integer, Integer>(); 
 		int index = 0;
 		for(Integer item : keys)
 		{
 			Integer support = itemset_support.get(item);
 			C.getItemsets()[index] = new ItemSet();
 			C.getItemsets()[index].getItems().add(item);
 			C.getItemsets()[index].setSupportCount(support);
 			
 			itemset_pos.put(item, index);
 			index++;
 		}
 	}
 
 	private static void getInitialLargeItemsets(CandidateItemset C, int minSupportCount, LargeItemset L)
 	{
 		int index = 0;
 		for(ItemSet itemset : C.getItemsets()) {
 			if(itemset == null)
 				break;
 			if(itemset.getSupportCount() >= minSupportCount)
 				L.getItemsetIds().add(index);
 			index++;
 		}
 	}
 	
 	//Given L[k-1], returns C[k]
 	public static List<ItemSet> apriori_gen(ItemSet[] allItemsets, List<Integer> largeItemSets, int itemSetSize)
 	{
 		//System.out.println("In apriori_gen " + itemSetSize);
 		//Collections.sort(largeItemSets);
 		
 		/*
 		 * Generate the candidate itemsets by joining the two itemsets in the large itemsets such that except their
 		 * last items match. Include all the matching items + the last item of both the itemsets to generate a new
 		 * candidate itemset. 
 		 */
 		List<ItemSet> candidateItemSets = Lists.newArrayList();
 		List<Integer> items = null;
 		int new_cand_index = 0;
 		Integer[] largeItemSetsArray = largeItemSets.toArray(new Integer[largeItemSets.size()]);
 		for(int i = 0; i < largeItemSetsArray.length; i++)
 		{
 			for(int j = i + 1; j < largeItemSetsArray.length; j++) {
 				List<Integer> outerItems = allItemsets[largeItemSetsArray[i]].getItems();
 				List<Integer> innerItems = allItemsets[largeItemSetsArray[j]].getItems();
 		
 				if((itemSetSize -1) > 0) {
 					boolean isMatch = true;
 					for(int k=0; k < (itemSetSize -1); k++) {
 						if(!outerItems.get(k).equals(innerItems.get(k))) {
 							isMatch = false;
 							break;
 						}
 					}
 					if(isMatch) {
 						items = Lists.newArrayList();
 						items.addAll(outerItems);
 						items.add(innerItems.get(itemSetSize-1));
 						
 						ItemSet newitemset = new ItemSet(items, 0, largeItemSetsArray[i], largeItemSetsArray[j]);
 						
 						if(prune(allItemsets, largeItemSets, newitemset))
 						{
 							candidateItemSets.add(newitemset);
 							allItemsets[largeItemSetsArray[i]].addExtension(new_cand_index);
 							allItemsets[largeItemSetsArray[j]].addExtension(new_cand_index);
 							new_cand_index++;
 						}
 					}
 				}
 				// Handle the base case for generation of candidate itemsets for k-1 = 1.
 				else {
 					if(outerItems.get(0) < innerItems.get(0)) {
 						items = Lists.newArrayList();
 						items.add(outerItems.get(0));
 						items.add(innerItems.get(0));
 						
 						ItemSet newitemset = new ItemSet(items, 0, largeItemSetsArray[i], largeItemSetsArray[j]);
 						
 						candidateItemSets.add(newitemset);
 						allItemsets[largeItemSetsArray[i]].addExtension(new_cand_index);
 						allItemsets[largeItemSetsArray[j]].addExtension(new_cand_index);
 						new_cand_index++;
 					}
 				}
 			}
 		}
 		
 		return candidateItemSets;
 	}
 
 	private static boolean prune(ItemSet[] allItemsets, List<Integer> largeItemsets, ItemSet query)
 	{
 		/*
 		 * Prune the generated candidate itemsets by removing all such candidate itemsets whose any (K-1) subset
 		 * does not belong to the list of (K-1) large itemsets.
 		 */
 		List<ItemSet> subsets = getSubsets(query);
 			
 		for(ItemSet s : subsets)
 		{
 			boolean contains = false;
 			for(Integer index : largeItemsets)
 			{
 				ItemSet itemset = allItemsets[index];
 				if(itemset.equals(s))
 				{
 					contains = true;
 					break;
 				}
 			}
 			if(!contains)
 				return false;
 		}
 		return true;
 	}
 	
 	/*
 	 * Generate all possible increasing k-1 subsets for this itemset
 	 */
 	private static List<ItemSet> getSubsets(ItemSet itemset)
 	{
 		List<ItemSet> subsets = new ArrayList<ItemSet>();
 		
 		List<Integer> items = itemset.getItems();
 		for(int i = 0; i < items.size(); i++) {
 			List<Integer> currItems = new ArrayList<Integer>(items);
 			currItems.remove(items.size() - 1 - i);
 			subsets.add(new ItemSet(currItems, 0));
 		}
 
 		return subsets;
 	}
 	
 	/*
 	 * Gets a iterative reader to the dataset and algorithm corresponding to the current experiment.
 	 */
 	private static InputReader getDatasetReader(Dataset dataset)
 	{
 		return new DBReader(dataset, Algorithm.APRIORI);
 	}
 }
