 import java.util.*;
 
 public class MNB_classification
 {
     public static double TRAINING_RATIO = .8;
     MNB_probability odds;
     Map<String,Pair<String,Map<String, Integer>>>
         DC_test;
     Map<String,Pair<String,Map<String, Integer>>>
         DC_training;
     Map<String,Pair<String,Map<String, Integer>>>
         test_set;
     Map<String,Pair<String,Map<String, Integer>>>
         training_set;
 
     public MNB_classification(Map<String,Pair<String,Map<String, Integer>>>
         DC)
     {
         // ID -> (Class,(Tokens -> Count))
         DC_test =
             new HashMap<String,Pair<String,Map<String,Integer>>>();
         DC_training =
             new HashMap<String,Pair<String,Map<String,Integer>>>();
         LoadTrainingAndTest(DC,
                 DC_training,
                 DC_test);
 
         training_set = DC_training;
         test_set = DC_test;
         odds = new MNB_probability(training_set);
     }
 
     public Map<String,Pair<String,Map<String, Integer>>>
         getTestSet()
     {
         return test_set;
     }
 
     public Map<String,Pair<String,Map<String, Integer>>>
         getTrainingSet()
     {
         return training_set;
     }
 
     public void train()
     {
         odds.computeWordProbability();
         odds.computeClassProbability();
     }
 
 
    private Set<String> featureSelection(
            int M)
     {
         Map<String, Integer> DC_ClassCounts = new HashMap<String,Integer>();
         Map<String, Integer> DC_WordCounts = new HashMap<String,Integer>();
         Map<String, Integer> DC_NotWordCounts = new HashMap<String,Integer>();
 
         // word -> (class -> count)
         Map<String,
             Map<String, Integer>> DC_WordClassCounts =
                 Utilities.<String,String,Integer>MapMapInit(
                         odds.getVocabulary());
         Map<String,
             Map<String, Integer>> DC_NotWordClassCounts =
                 Utilities.<String,String,Integer>MapMapInit(
                         odds.getVocabulary());

         for (String w : odds.getVocabulary())
         {
             DC_WordClassCounts.put(w, new HashMap<String, Integer>());
             DC_NotWordClassCounts.put(w, new HashMap<String, Integer>());
         }

         for (Map.Entry<String,Pair<String,Map<String,Integer>>>
                 entry : DC_training.entrySet())
         {
             String ObservedClass = entry.getValue().First();
             Set<String> ObservedWords = entry.getValue().Second().keySet();
             for (String word : odds.getVocabulary())
             {
                 Map<String, Integer> ClassCountsGivenWord =
                     DC_WordClassCounts.get(word);
                 Map<String, Integer> ClassCountsGivenNotWord =
                     DC_NotWordClassCounts.get(word);
                 if (ObservedWords.contains(word))
                 {
                     Utilities.MapIncrementCount(
                             DC_WordCounts,
                             word);
                     Utilities.MapIncrementCount(
                             ClassCountsGivenWord,
                             ObservedClass);
                 }
                 else
                 {
                     Utilities.MapIncrementCount(
                             DC_NotWordCounts,
                             word);
                     Utilities.MapIncrementCount(
                             ClassCountsGivenNotWord,
                             ObservedClass);
                 }
                 DC_WordClassCounts.remove(word);
                 DC_NotWordClassCounts.remove(word);
                 DC_WordClassCounts.put(word, ClassCountsGivenWord);
                 DC_NotWordClassCounts.put(word, ClassCountsGivenNotWord);
 
             }
             Utilities.MapIncrementCount(DC_ClassCounts,
                     ObservedClass);
         }
 
         Map<String, Double> P_c = new HashMap<String,Double>();
         for (String c : odds.getClassifications())
         {
             Integer Count = DC_ClassCounts.get(c);
             if (Count == null)
             {
                 Count = new Integer(0);
                 DC_ClassCounts.put(c, Count);
             }
             P_c.put(c, Count.doubleValue() /
                 (double) DC_training.size());
         }
 
         Map<String, Double> P_w = new HashMap<String,Double>();
         for (String word : odds.getVocabulary())
         {
             Integer Count = DC_WordCounts.get(word);
             if (Count == null)
             {
                 Count = new Integer(0);
                 DC_WordCounts.put(word, Count);
             }
             P_w.put(word, Count.doubleValue() /
                 (double) DC_training.size());
         }
 
         Map<String, Double> P_not_w = new HashMap<String,Double>();
         for (String word : odds.getVocabulary())
         {
             Integer Count = DC_NotWordCounts.get(word);
             if (Count == null)
             {
                 Count = new Integer(0);
                 DC_NotWordCounts.put(word, Count);
             }
             P_not_w.put(word, Count.doubleValue() /
                 (double) DC_training.size());
         }
 
         Map<String, Map<String, Double>>
             P_c_given_w =
             Utilities.<String,String,Double>MapMapInit(odds.getVocabulary());
         Map<String, Map<String, Double>>
             P_c_given_not_w =
             Utilities.<String,String,Double>MapMapInit(odds.getVocabulary());
 
         for (String word : odds.getVocabulary())
         {
             Map<String, Double> ClassProbsGivenWord =
                 new HashMap<String,Double>();
             Map<String, Double> ClassProbsGivenNotWord =
                 new HashMap<String,Double>();
 
             Map<String, Integer>
                 WordClassCounts = DC_WordClassCounts.get(word);
             Map<String, Integer>
                 NotWordClassCounts = DC_NotWordClassCounts.get(word);
             if (WordClassCounts == null)
             {
                 WordClassCounts = new HashMap<String,Integer>();
             }
             if (NotWordClassCounts == null)
             {
                 NotWordClassCounts = new HashMap<String,Integer>();
             }
             Integer WordDocCount =
                 DC_WordCounts.get(word);
             Integer NotWordDocCount =
                 DC_NotWordCounts.get(word);
 
             for (String c : odds.getClassifications())
             {
                 Integer ClassCountGivenWord = WordClassCounts.get(c);
                 if (ClassCountGivenWord == null)
                 {
                     ClassCountGivenWord = new Integer(0);
                     WordClassCounts.put(c,ClassCountGivenWord);
                 }
                 ClassProbsGivenWord.put(
                         c,
                         ClassCountGivenWord.doubleValue() /
                         WordDocCount.doubleValue());
 
                 Integer ClassCountGivenNotWord = NotWordClassCounts.get(c);
                 if (ClassCountGivenNotWord == null)
                 {
                     ClassCountGivenNotWord = new Integer(0);
                     NotWordClassCounts.put(c,ClassCountGivenNotWord);
                 }
                 ClassProbsGivenNotWord.put(
                         c,
                         ClassCountGivenNotWord.doubleValue() /
                         NotWordDocCount.doubleValue());
             }
 
             P_c_given_w.put(word, ClassProbsGivenWord);
             P_c_given_not_w.put(word, ClassProbsGivenNotWord);
         }
 
         System.out.println("P(c): " + P_c);
         System.out.println("P(w): " + P_w);
         System.out.println("P(~w): " + P_not_w);
         System.out.println("P(c|w): " + P_c_given_w);
         System.out.println("P(c|~w): " + P_c_given_not_w);
 
         // NOW, compute information gain
         List<Pair<Double, String>> WordInformationGain =
             new ArrayList<Pair<Double, String>>();
         for (String word : odds.getVocabulary())
         {
             double IGw = 0.0;
             double log_coef = Math.log(2.0);
             double CurrentPw = P_w.get(word).doubleValue();
             double CurrentPnotw = P_not_w.get(word).doubleValue();
 
             Map<String, Double> ClassProbGivenWord =
                 P_c_given_w.get(word);
             Map<String, Double> ClassProbGivenNotWord =
                 P_c_given_not_w.get(word);
 
             for (String c : odds.getClassifications())
             {
                 System.out.println("IGw first: " + IGw);
                 IGw -= P_c.get(c).doubleValue() *
                     Math.log(P_c.get(c).doubleValue()) / log_coef;
                 System.out.println("IGw next: " + IGw);
                 IGw += CurrentPw * ClassProbGivenWord.get(c).doubleValue() *
                     Math.log(ClassProbGivenWord.get(c).doubleValue()) /
                     log_coef;
                 System.out.println("IGw again: " + IGw);
                 IGw += CurrentPnotw *
                     ClassProbGivenNotWord.get(c).doubleValue() *
                     Math.log(ClassProbGivenNotWord.get(c).doubleValue()) /
                     log_coef;
                 System.out.println("IGw finally: " + IGw);
             }
             WordInformationGain.add(new Pair<Double,String>(IGw,word));
         }
         System.out.println("RANKS:" +
                 WordInformationGain);
         java.util.Collections.sort(WordInformationGain,
                 new ReverseComparator<Pair<Double,String>>());
         Set<String> selectedFeatures = new HashSet<String>();
         for (int i = 0; i < M; i++)
         {
             selectedFeatures.add(WordInformationGain.get(i).Second());
         }
 
         System.out.println("Narrowed features down to: " +
                 selectedFeatures);
 
         training_set = new HashMap<String,
                      Pair<String, Map<String, Integer>>>();
         test_set = new HashMap<String,
                      Pair<String, Map<String, Integer>>>();
         NarrowFeatureDocSet(DC_training, training_set, selectedFeatures);
         NarrowFeatureDocSet(DC_test, test_set, selectedFeatures);
 
         return selectedFeatures;
     }
 
     private void NarrowFeatureDocSet(
             Map<String,Pair<String,Map<String, Integer>>> raw_set,
             Map<String,Pair<String,Map<String, Integer>>> set,
             Set<String> selectedFeatures)
     {
         for (Map.Entry<String,Pair<String,Map<String, Integer>>>
                 Doc : raw_set.entrySet())
         {
             Pair<String, Map<String, Integer>> DocPair = Doc.getValue();
             Map<String, Integer> DocFeatures = DocPair.Second();
             Map<String, Integer> newFeatures = new HashMap<String, Integer>();
             for (String feature : selectedFeatures)
             {
                 if (DocFeatures.containsKey(feature))
                 {
                     newFeatures.put(feature, DocFeatures.get(feature));
                 }
             }
             set.put(Doc.getKey(),
                     new Pair<String, Map<String, Integer>> (
                         DocPair.First(),
                         newFeatures));
         }
     }
 
     private void LoadTrainingAndTest(
         Map<String,Pair<String,Map<String, Integer>>>
             DC,
         Map<String,Pair<String,Map<String, Integer>>>
             DC_training,
         Map<String,Pair<String,Map<String, Integer>>>
             DC_test)
     {
         Random rng = new Random();
         double dice;
 
         // Choose random K algorithm --
         // Note how many documents I will sift through to create the training
         // set.
         int RestOfTheDocuments = DC.size();
 
         // Find out how many documents I need in the training set ("K").
         int LeftTrainingToPick = (int)(TRAINING_RATIO * RestOfTheDocuments);
 
         // Guard against divide by zero
         if (RestOfTheDocuments == 0)
             return;
 
         for (Map.Entry<String,Pair<String,Map<String,Integer>>>
                 Doc : DC.entrySet())
         {
             double ratio = ((double)LeftTrainingToPick) /
                 ((double)RestOfTheDocuments);
             dice = rng.nextFloat();
             if (dice < ratio)
             // We pick a document for the training set in this case.
             // decrease the number of documents we still need to find (numerator)
             // and decrease the number of documents we still need to sift
             // through (denominator).
             {
                 DC_training.put(
                         Doc.getKey(),
                         Doc.getValue());
                 LeftTrainingToPick--;
                 RestOfTheDocuments--;
             }
             else
             {
             // We pick a document for NOT the training set (the test set)
             // decrease the number of documents we still need to sift through
             // to find the correct amount of training set documents.
                 RestOfTheDocuments--;
                 DC_test.put(
                         Doc.getKey(),
                         new Pair<String,Map<String,Integer>>(
                             Doc.getValue().First(),
                             Doc.getValue().Second()));
             }
         }
 
     }
 
     // document -> classification
     public String label(Map<String, Integer> document)
     {
         List<Pair<Double,String>> PdcPc =
             new LinkedList<Pair<Double,String>>();
 
         for (String c : odds.getClassifications())
         {
             double Pc = odds.getClassProbability(c);
 
             double P_d_given_c_reduce = 1.0;
             for (String word : document.keySet())
             {
                 double P_w_given_c = odds.getWordProbability(word,c);
                 Integer retrieve = document.get(word);
                 if (retrieve == null || retrieve.intValue() == 0)
                     continue;
                 P_d_given_c_reduce *= Math.pow(P_w_given_c,retrieve.doubleValue());
             }
             double P_d_given_c = P_d_given_c_reduce;
             PdcPc.add(new Pair<Double,String>(P_d_given_c*Pc,c));
         }
         double PdcPc_sum = 0.0;
         for (Pair<Double,String> d : PdcPc)
         {
             PdcPc_sum += d.First().doubleValue();
         }
         List<Pair<Double,String>> Pdc =
             new LinkedList<Pair<Double,String>>();
 
         for (Pair<Double,String> n : PdcPc)
         {
             Pdc.add(new Pair<Double,String>(
                         n.First().doubleValue() / PdcPc_sum,
                         n.Second()));
         }
         return Collections.max(Pdc).Second();
     }
 }
