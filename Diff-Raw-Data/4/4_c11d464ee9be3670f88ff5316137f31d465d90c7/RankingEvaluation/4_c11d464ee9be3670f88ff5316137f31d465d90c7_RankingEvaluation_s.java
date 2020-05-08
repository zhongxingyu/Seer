 /**
  * Copyright (c) 2010 Jens Haase <je.haase@googlemail.com>
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package de.tudarmstadt.ukp.teaching.uima.nounDecompounding.evaluation;
 
 import java.io.IOException;
 import java.util.List;
 
 import de.tudarmstadt.ukp.teaching.uima.nounDecompounding.ranking.IRankList;
 import de.tudarmstadt.ukp.teaching.uima.nounDecompounding.ranking.IRankListAndTree;
 import de.tudarmstadt.ukp.teaching.uima.nounDecompounding.ranking.IRankTree;
 import de.tudarmstadt.ukp.teaching.uima.nounDecompounding.splitter.ISplitAlgorithm;
 import de.tudarmstadt.ukp.teaching.uima.nounDecompounding.splitter.Split;
 
 /**
  * Evaluation class for ranking algorithms
  * @author Jens Haase <je.haase@googlemail.com>
  */
 public class RankingEvaluation {
 
 	private CcorpusReader reader;
 	
 	/**
 	 * Stores the result of a evaluation
 	 * @author Jens Haase <je.haase@googlemail.com>
 	 */
 	public class Result {
 		public float recall;
 		public float recallWithoutMorpheme;
 		public float recallAt2;
 		public float recallAt2WithoutMorpheme;
 		public float recallAt3;
 		public float recallAt3WithoutMorpheme;
 		
 		public String toString() {
 			StringBuffer buf = new StringBuffer();
 			buf.append("Result:\n");
 			buf.append("\tCorrect: "+recall+" (without Morpheme: " +recallWithoutMorpheme+")\n");
 			buf.append("\tCorrect@2: "+recallAt2+" (without Morpheme: " +recallAt2WithoutMorpheme+")\n");
 			buf.append("\tCorrect@3: "+recallAt3+" (without Morpheme: " +recallAt3WithoutMorpheme+")");
 			
 			return buf.toString();
 		}
 	}
 
 	public RankingEvaluation(CcorpusReader aReader) {
 		this.reader = aReader;
 	}
 	
 	/**
 	 * Evaluates a IRankList algorithm, with a given split algorithm
 	 * @param splitter The splitting algorithm
 	 * @param ranker The ranking algorithm
 	 * @param limit How many splits should be evaluated
 	 * @return
 	 */
 	public Result evaluateList(ISplitAlgorithm splitter, IRankList ranker, int limit) {
 		int total = 0, correct = 0, correctWithoutMorpheme = 0,
 			correctAt2 = 0, correctAt2WithoutMorpheme = 0,
 			correctAt3 = 0, correctAt3WithoutMorpheme = 0;
 		
 		try {
 			Split split;
 			List<Split> result;
 			
 			
 			while ((split = reader.readSplit()) != null && total < limit) {
 				result = ranker.rank(splitter.split(split.getWord()).getAllSplits());
 				
 				if (!result.get(0).equals(split)) {
 					System.out.println(total + ": " + split + " -> " + result.get(0) + " " + this.getResultList(result));
 				}
 				
 				if (result.get(0).equals(split)) {
 					correct++; correctAt2++; correctAt3++;
 				} else if (result.size() > 2 && result.get(1).equals(split)) {
 					correctAt2++; correctAt3++;
 				} else if (result.size() > 3 && result.get(2).equals(split)) {
 					correctAt3++;
 				}
 				
 				if (result.get(0).equalWithoutMorpheme(split)) {
 					correctWithoutMorpheme++; correctAt2WithoutMorpheme++; correctAt3WithoutMorpheme++;
 				} else if (result.size() > 2 && result.get(1).equalWithoutMorpheme(split)) {
 					correctAt2WithoutMorpheme++; correctAt3WithoutMorpheme++;
 				} else if (result.size() > 3 && result.get(2).equalWithoutMorpheme(split)) {
 					correctAt3WithoutMorpheme++;
 				}
 				
 				total++;
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		Result r = new Result();
 		r.recall = (float) correct / (float) total;
 		r.recallWithoutMorpheme = (float) correctWithoutMorpheme / (float) total;
 		r.recallAt2 = (float) correctAt2 / (float) total;
 		r.recallAt2WithoutMorpheme = (float) correctAt2WithoutMorpheme / (float) total;
 		r.recallAt3 = (float) correctAt3 / (float) total;
 		r.recallAt3WithoutMorpheme = (float) correctAt3WithoutMorpheme / (float) total;
 		return r;
 	}
 	
 	/**
 	 * Evaluates a IRankTree algorithm
 	 * @param splitter The splitting algorithm
 	 * @param ranker The ranking algorithm
 	 * @param limit How many splits should be evaluated
 	 * @return
 	 */
 	public Result evaluateTree(ISplitAlgorithm splitter, IRankTree ranker, int limit) {
 		int total = 0, correct = 0, correctWithoutMorpheme = 0;
 		
 		try {
 			Split split, result;
 			while ((split = reader.readSplit()) != null && total < limit) {
 				result = ranker.highestRank((splitter.split(split.getWord())));
 				
 				if (result.equals(split)) {
 					correct++;
 				} else {
 					System.out.println(total + ": " + split + " -> " + result);
 				}
 				
 				if (result.equalWithoutMorpheme(split)) {
 					correctWithoutMorpheme++;
 				}
 				
 				total++;
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		Result r = new Result();
 		r.recall = (float) correct / (float) total;
 		r.recallWithoutMorpheme = (float) correctWithoutMorpheme / (float) total;
 		
 		return r;
 	}
 	
 	/**
 	 * Evaluates a IRankListAndTree algorithm, with a given split algorithm
 	 * 
 	 * This is mostly the same as evaluteList and evaluteTree. But each
 	 * word is evaluted directly by both method. This has the advantage
 	 * that the cache contains the result and calculation is faster.
 	 * 
 	 * @param splitter The splitting algorithm
 	 * @param ranker The ranking algorithm
 	 * @param limit How many splits should be evaluated
 	 * @return A array of result with size 2. The first entry is the result for the list, second for the tree
 	 */
 	public Result[] evaluateListAndTree(ISplitAlgorithm splitter, IRankListAndTree ranker, int limit) {
 		int total = 0, correct = 0, correctWithoutMorpheme = 0,
 			correctAt2 = 0, correctAt2WithoutMorpheme = 0,
 			correctAt3 = 0, correctAt3WithoutMorpheme = 0;
 		
 		int correctTree = 0, correctTreeWithoutMorpheme = 0;
 		
 		try {
 			Split split;
 			List<Split> resultList;
 			Split resultTree;
 			
 			
 			while ((split = reader.readSplit()) != null && total < limit) {
 				resultList = ranker.rank(splitter.split(split.getWord()).getAllSplits());
 				
 				// Evaluate List
 				if (!resultList.get(0).equals(split)) {
 					System.out.println(total + ": " + split + " -> " + resultList.get(0) + " " + this.getResultList(resultList));
 				}
 				
 				if (resultList.get(0).equals(split)) {
 					correct++; correctAt2++; correctAt3++;
 				} else if (resultList.size() > 2 && resultList.get(1).equals(split)) {
 					correctAt2++; correctAt3++;
 				} else if (resultList.size() > 3 && resultList.get(2).equals(split)) {
 					correctAt3++;
 				}
 				
 				if (resultList.get(0).equalWithoutMorpheme(split)) {
 					correctWithoutMorpheme++; correctAt2WithoutMorpheme++; correctAt3WithoutMorpheme++;
 				} else if (resultList.size() > 2 && resultList.get(1).equalWithoutMorpheme(split)) {
 					correctAt2WithoutMorpheme++; correctAt3WithoutMorpheme++;
 				} else if (resultList.size() > 3 && resultList.get(2).equalWithoutMorpheme(split)) {
 					correctAt3WithoutMorpheme++;
 				}
 				
 				// Evaluate Tree
 				resultTree = ranker.highestRank((splitter.split(split.getWord())));
 				if (resultTree.equals(split)) {
 					correctTree++;
 				} else {
 					System.out.println(total + ": " + split + " -> " + resultTree);
 				}
 				
 				if (resultTree.equalWithoutMorpheme(split)) {
 					correctTreeWithoutMorpheme++;
 				}
 				
 				total++;
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		Result resultList = new Result();
 		resultList.recall = (float) correct / (float) total;
 		resultList.recallWithoutMorpheme = (float) correctWithoutMorpheme / (float) total;
 		resultList.recallAt2 = (float) correctAt2 / (float) total;
 		resultList.recallAt2WithoutMorpheme = (float) correctAt2WithoutMorpheme / (float) total;
 		resultList.recallAt3 = (float) correctAt3 / (float) total;
 		resultList.recallAt3WithoutMorpheme = (float) correctAt3WithoutMorpheme / (float) total;
 		
 		Result resultTree = new Result();
		resultTree.recall = (float) correct / (float) total;
		resultTree.recallWithoutMorpheme = (float) correctWithoutMorpheme / (float) total;
 		
 		return new Result[]{resultList, resultTree};
 	}
 	
 	/**
 	 * Used to output a list of splits
 	 * @param splits
 	 * @return
 	 */
 	private String getResultList(List<Split> splits) {
 		StringBuffer buffer = new StringBuffer();
 		buffer.append("[");
 		
 		for (int i = 0; i < splits.size(); i++) {
 			buffer.append(splits.get(i).toString());
 			buffer.append(":");
 			buffer.append(splits.get(i).getWeight());
 			if (i < splits.size()-1) {
 				buffer.append(", ");
 			}
 		}
 		
 		buffer.append("]");
 		
 		return buffer.toString();
 	}
 	
 	/**
 	 * Evaluates a IRankList algorithm, with a given split algorithm
 	 * @param splitter The splitting algorithm
 	 * @param ranker The ranking algorithm
 	 * @return
 	 */
 	public Result evaluateList(ISplitAlgorithm splitter, IRankList ranker) {
 		return this.evaluateList(splitter, ranker, Integer.MAX_VALUE);
 	}
 	
 	/**
 	 * Evaluates a IRankTree algorithm, with a given split algorithm
 	 * @param splitter The splitting algorithm
 	 * @param ranker The ranking algorithm
 	 * @return
 	 */
 	public Result evaluateTree(ISplitAlgorithm splitter, IRankTree ranker) {
 		return this.evaluateTree(splitter, ranker, Integer.MAX_VALUE);
 	}
 	
 	/**
 	 * Evaluates a IRankListAndTree algorithm, with a given split algorithm
 	 * 
 	 * This is mostly the same as evaluteList and evaluteTree. But each
 	 * word is evaluted directly by both method. This has the advantage
 	 * that the cache contains the result and calculation is faster.
 	 * 
 	 * @param splitter The splitting algorithm
 	 * @param ranker The ranking algorithm
 	 * @return A array of result with size 2. The first entry is the result for the list, second for the tree
 	 */
 	public Result[] evaluateListAndTree(ISplitAlgorithm splitter, IRankListAndTree ranker) {
 		return this.evaluateListAndTree(splitter, ranker, Integer.MAX_VALUE);
 	}
 }
