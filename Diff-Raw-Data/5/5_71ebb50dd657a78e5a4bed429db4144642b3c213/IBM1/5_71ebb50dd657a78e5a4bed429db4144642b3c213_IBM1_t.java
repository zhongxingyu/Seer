 package cs224n.wordaligner;  
 
 import cs224n.util.*;
 import java.util.List;
 
 /**
  * IBM1 models the problem
  * 
  * @author Francois Chaubard
  */
 
 public class IBM1 implements WordAligner {
 
 	// Count of sentence pairs that contain source and target words
 	private CounterMap<String,String> target_source_prob; // p(f|e)
 	// Count of source words appearing in the training set
 	//private Counter<String> source_count;
 
 	public Alignment align(SentencePair sentencePair) {
 		Alignment alignment = new Alignment();
 		
 		int numSourceWords = sentencePair.getSourceWords().size();
 		int numTargetWords = sentencePair.getTargetWords().size();
 
 		for (int targetIdx = 0; targetIdx < numTargetWords; targetIdx++) {
 			String target = sentencePair.getTargetWords().get(targetIdx);
 			//Find source word with maximum alignment likelihood
 			double currentMax = 0;
 			int maxSourceIdx = 0;
 			for (int srcIndex = 0; srcIndex < numSourceWords; srcIndex++) {
 				String source = sentencePair.getSourceWords().get(srcIndex);
 				double ai = target_source_prob.getCount(target, source) ;
 
 				if (currentMax < ai){
 					currentMax = ai;
 					maxSourceIdx = srcIndex;
 				}
 			}
 			
 			//If probability of an alignment is greater than P(source, NULL)
 			if (currentMax > target_source_prob.getCount(target,NULL_WORD)) {
 				// Add the alignment
 				alignment.addPredictedAlignment(targetIdx, maxSourceIdx);
 			} 
 		}
 		return alignment;
 	}
 
 	public void setAllInCounterMap(List<SentencePair> trainingPairs, CounterMap<String,String> counterMap, double initValue){
 		for(SentencePair pair : trainingPairs){
 			List<String> targetWords = pair.getTargetWords();
 			List<String> sourceWords = pair.getSourceWords();
 			//Add a Null word to the source list
 			sourceWords.add(NULL_WORD);
 			for(String source : sourceWords){
 				for(String target : targetWords){
 					counterMap.setCount(target,  source, initValue);
 				}
 			}
 		}
 	}
 	public void train(List<SentencePair> trainingPairs) {
 		//Initalize counters
 		target_source_prob= new CounterMap<String,String>();; // p(f|e)
 		CounterMap<String,String> target_source_count = new CounterMap<String,String>(); // c(f|e)
 		boolean converged = false;
 		
 		// initialize the probability to uniform
 		setAllInCounterMap(trainingPairs,target_source_prob,1.0);
 		target_source_prob = Counters.conditionalNormalize(target_source_prob);
 		
 		double posterior_sum = 0.0;
 		int count=0;
 		while(!converged){
 			count++;
 
			target_source_count = new CounterMap<String,String>(); 
 			
 			//For each sentence pair increment the counters
 			for(SentencePair pair : trainingPairs){
 				List<String> targetWords = pair.getTargetWords();
 				List<String> sourceWords = pair.getSourceWords();
 				//Add a Null word to the source list
 				sourceWords.add(NULL_WORD);
 				for(String source : sourceWords){
					posterior_sum = 0.0;
 					for(String target : targetWords){
 						posterior_sum+=target_source_prob.getCount(target, source);
 					}
 							
 					for(String target : targetWords){
 						
 						target_source_count.incrementCount(target, source,  (target_source_prob.getCount(target, source)/posterior_sum));
 						
 					}
 				}
 				
 				
 			}
 			
 			// normalize the probabilities
 			target_source_count = Counters.conditionalNormalize(target_source_count);
 			
 			// check if converged
 			double error =0.0;
 			for(SentencePair pair : trainingPairs){
 				List<String> targetWords = pair.getTargetWords();
 				List<String> sourceWords = pair.getSourceWords();
 				//Add a Null word to the source list
 				sourceWords.add(NULL_WORD);
 				for(String source : sourceWords){
 					for(String target : targetWords){
 						error += Math.pow(target_source_count.getCount(target, source) - target_source_prob.getCount(target, source) ,2);
 					}
 				}
 			}
 			if (error<10){
 				converged=true;
 			}
 			
 			target_source_prob = target_source_count;
 				
 			System.out.printf("iteration number %d  error %f \n", count, error );
 			
 			
 		}
 	}
 }
 
