 package org.put.hd.dmarf.algorithms.apriori.binary;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Collection;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import org.put.hd.dmarf.data.DataRepresentationBase;
 import org.put.hd.dmarf.data.builders.BasicDataBuilder;
 
 /**
  * Encapsulates all operations for the generating, growing the
  * {@link BinaryItemSet} in the apriori algorithm.
  * 
  * @author Piotr
  * 
  */
 public class BinarySetsEngine implements ISetsEngine {
 
 	private SortedSet<BinaryItemSet> levelOneSet;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.put.hd.dmarf.algorithms.apriori.binary.ISetsEngine#getSingleCandidateSets
 	 * (org.put.hd.dmarf.data.DataRepresentationBase)
 	 */
 	public SortedMap<BinaryItemSet, Integer> getSingleCandidateSets(
 			DataRepresentationBase data, Integer supportThreshold) {
 
 		// MT: get the each attribute independently
 		levelOneSet = new TreeSet<BinaryItemSet>();
 		SortedMap<BinaryItemSet, Integer> frequentSets = new TreeMap<BinaryItemSet, Integer>();
 
 		for (Map.Entry<Integer, Integer> entry : data.getAttributesCounter()
 				.entrySet()) {
 
 			Integer value = entry.getValue();
 			Integer key = entry.getKey();
 
 			// add elements only with proper support
 			if (value >= supportThreshold) {
 
 				// smart ass generation of vectors like
 				// 00000100000, 0000000000001000,10000000,0000001 or something
 				// like that
 				List<Integer> attributeList = new LinkedList<Integer>();
 				attributeList.add(key);
 				char[] vector = BasicDataBuilder.generateCharArray(
 						attributeList, data.getMaxAttAligned());
 
 				// just make the item set
 				BinaryItemSet itemSet = new BinaryItemSet(vector, 1);
 				frequentSets.put(itemSet, value);
 				levelOneSet.add(itemSet);
 			}
 		}
 
 		return frequentSets;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.put.hd.dmarf.algorithms.apriori.binary.ISetsEngine#getCandidateSets
 	 * (java.util.SortedMap, int)
 	 */
 	public Set<BinaryItemSet> getCandidateSets(
 			Set<BinaryItemSet> frequentSupportMap, int generation) {
 
 		// set up the candidate output
 		Set<BinaryItemSet> output = new TreeSet<BinaryItemSet>();
 
 //		// FIXME: this can be omitted if the input map or set is only
 //		// generation-1 set
 //		// get only set with (generation - 1) attributes
 //		List<BinaryItemSet> minuseOneSets = new LinkedList<BinaryItemSet>();
 //		for (Map.Entry<BinaryItemSet, Integer> binaryItemSet : frequentSupportMap
 //				.entrySet()) {
 //			BinaryItemSet itemSet = binaryItemSet.getKey();
 //			if (itemSet.getNumberOfAttributes() == generation) {
 //				minuseOneSets.add(itemSet);
 //			}
 //		}
 
 		// for each set with (generation-1) elements
 		// add elements from level one frequent sets
 		// in a smart way do this
 		for (BinaryItemSet itemSet : frequentSupportMap) {
 
 			// create the output set only if the order will be preserved
 			for (BinaryItemSet singleItemSet : this.levelOneSet) {
 
 				// is earlier in collection
 				if (itemSet.compareTo(singleItemSet) < 0) {
 
 					// create the attribute vector
 					char[] vector = itemSet.getAttributeVector();
 					char[] singleVector = singleItemSet.getAttributeVector();
 					int vectorLength = singleVector.length;
 
 					char[] outputVector = new char[vectorLength];
 					for (int i = 0; i < vectorLength; i++) {
 						// hope it will work
 						outputVector[i] = (char) (vector[i] | singleVector[i]);
 					}
 
 					// check this sets to throw out elements with (g-1) elements
 					// which are not marked frequent
 					// meaning cannot be find in the frequent support map
 					boolean hasSupport = true;
 					Collection<BinaryItemSet> items = BinaryItemSet.divideSet(
 							outputVector, generation);
 					
 					for (BinaryItemSet binaryItemSet : items) {
 						if (!frequentSupportMap.contains(binaryItemSet)) {
 							hasSupport = false;
 						}
 					}
 
 					if (hasSupport) {
 						// create candidate
 						BinaryItemSet candidate = new BinaryItemSet(outputVector,
 								generation);
 						output.add(candidate);
 					}
 				}
 			}
 		}
 		return output;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.put.hd.dmarf.algorithms.apriori.binary.ISetsEngine#verifyCandidatesInData
 	 * (org.put.hd.dmarf.data.DataRepresentationBase, java.util.Set)
 	 */
	public SortedMap<BinaryItemSet,Integer> verifyCandidatesInData(
 			DataRepresentationBase data, Set<BinaryItemSet> candidates) {
 		// TODO Auto-generated method stub
 		// TUTAJ SPRAWDZANIE MINIG SUPPORT KTORY MA WYLECIEC NA GPU
 		return null;
 	}
 
 }
