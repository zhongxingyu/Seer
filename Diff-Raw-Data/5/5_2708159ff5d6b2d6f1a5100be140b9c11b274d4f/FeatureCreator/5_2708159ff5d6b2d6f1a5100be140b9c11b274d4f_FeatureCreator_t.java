 /**
  * 
  */
 
 package uk.ac.cam.eng.rulebuilding.features;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.io.DoubleWritable;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.SortedMapWritable;
 
 import uk.ac.cam.eng.extraction.datatypes.Rule;
 import uk.ac.cam.eng.extraction.hadoop.datatypes.GeneralPairWritable3;
 import uk.ac.cam.eng.extraction.hadoop.features.MapReduceFeature;
 import uk.ac.cam.eng.extraction.hadoop.features.MapReduceFeatureCreator;
 
 /**
  * @author jmp84 This class creates a set of features given a list of rules
  */
 public class FeatureCreator {
 
     // list of all features
     private Map<String, Feature> features;
     // list of selected features in order
     private String[] selectedFeatures;
     // configuration (features, feature indices)
     private Configuration conf;
 
     public FeatureCreator(Configuration conf) {
         this.conf = conf;
         features = new HashMap<String, Feature>();
         features.put("source2target_probability",
                 new Source2TargetProbability());
         features.put("target2source_probability",
                 new Target2SourceProbability());
         features.put("word_insertion_penalty", new WordInsertionPenalty());
         features.put("rule_insertion_penalty", new RuleInsertionPenalty());
         features.put("glue_rule", new GlueRule());
         features.put("insert_scale", new InsertScale());
         features.put("rule_count_1", new RuleCount1());
         features.put("rule_count_2", new RuleCount2());
         features.put("rule_count_greater_than_2", new RuleCountGreaterThan2());
         features.put("source2target_lexical_probability",
                 new Source2TargetLexicalProbability());
         features.put("target2source_lexical_probability",
                 new Target2SourceLexicalProbability());
         features.put("unaligned_source_words", new UnalignedSourceWords());
         features.put("unaligned_target_words", new UnalignedTargetWords());
         features.put("binary_provenance", new BinaryProvenance());
         features.put("source2target_probability_prior",
                 new Source2TargetProbabilityPrior());
         features.put("target2source_probability_prior",
                 new Target2SourceProbabilityPrior());
         String provenance = conf.get("provenance");
         if (provenance != null) {
             String[] provenances = provenance.split(",");
             for (String prov: provenances) {
                 features.put("provenance_source2target_probability-" + prov,
                         new ProvenanceSource2TargetProbability(prov));
                 features.put("provenance_target2source_probability-" + prov,
                         new ProvenanceTarget2SourceProbability(prov));
                 features.put("provenance_source2target_lexical_probability-"
                         + prov, new ProvenanceSource2TargetLexicalProbability(
                         prov));
                 features.put("provenance_target2source_lexical_probability-"
                         + prov, new ProvenanceTarget2SourceLexicalProbability(
                         prov));
             }
         }
         String selectedFeaturesString = conf.get("features");
         if (selectedFeaturesString == null) {
             System.err.println("Missing property " +
                     "'features' in the config");
             System.exit(1);
         }
         selectedFeatures = selectedFeaturesString.split(",");
         // initial feature index is zero, then increments with the number of
         // features of each feature type. nextFeatureIndex is used to prevent
         // conf to be overwritten before being used.
         int featureIndex = 0, nextFeatureIndex = 0;
         for (String selectedFeature: selectedFeatures) {
             if (selectedFeature.equals(
                     "provenance_source2target_lexical_probability")
                     || selectedFeature
                             .equals("provenance_target2source_lexical_probability")
                     || selectedFeature
                             .equals("provenance_source2target_probability")
                     || selectedFeature
                             .equals("provenance_target2source_probability")) {
                 for (String prov: conf.get("provenance").split(",")) {
                     featureIndex = nextFeatureIndex;
                     nextFeatureIndex +=
                             features.get(selectedFeature + "-" + prov)
                                     .getNumberOfFeatures(conf);
                     conf.setInt(selectedFeature + "-" + prov,
                             featureIndex);
                 }
             }
             else {
                 featureIndex = nextFeatureIndex;
                 nextFeatureIndex +=
                         features.get(selectedFeature).getNumberOfFeatures(conf);
                 // TODO change this (maybe something like
                 // conf.setInt(selectedFeature+"-"+"startindex", featureIndex))
                 conf.setInt(selectedFeature + "-nbfeats",
                         features.get(selectedFeature).getNumberOfFeatures(conf));
                 conf.setInt(selectedFeature, featureIndex);
             }
         }
         String mapreduceFeaturesString = conf.get("mapreduce_features");
         if (mapreduceFeaturesString == null) {
             System.err.println("Missing property " +
                     "'mapreduce_features' in the config");
             System.exit(1);
         }
         String[] mapreduceFeatures = mapreduceFeaturesString.split(",");
         MapReduceFeatureCreator featureCreator =
                 new MapReduceFeatureCreator(conf);
         featureIndex = 0;
         nextFeatureIndex = 0;
         for (String mapreduceFeature: mapreduceFeatures) {
             if (mapreduceFeature.equals(
                     "provenance_source2target_lexical_probability")
                     || mapreduceFeature
                             .equals("provenance_target2source_lexical_probability")
                     || mapreduceFeature
                             .equals("provenance_source2target_probability")
                     || mapreduceFeature
                             .equals("provenance_target2source_probability")) {
                 for (String prov: conf.get("provenance").split(",")) {
                     featureIndex = nextFeatureIndex;
                     MapReduceFeature featureJob =
                             featureCreator.getFeatureJob(mapreduceFeature + "-"
                                     + prov);
                     nextFeatureIndex += featureJob.getNumberOfFeatures(conf);
                     conf.setInt(mapreduceFeature + "-" + prov + "-mapreduce",
                             featureIndex);
                 }
             }
             else {
                 featureIndex = nextFeatureIndex;
                 MapReduceFeature featureJob =
                         featureCreator.getFeatureJob(mapreduceFeature);
                 nextFeatureIndex += featureJob.getNumberOfFeatures(conf);
                 // add "-mapreduce" to avoid name clashing
                 conf.setInt(mapreduceFeature + "-mapreduce", featureIndex);
             }
         }
     }
 
     private Map<Integer, Number> createFeatures(String featureName,
             GeneralPairWritable3 ruleAndMapReduceFeatures) {
         return features.get(featureName).value(
                 new Rule(ruleAndMapReduceFeatures.getFirst()),
                 ruleAndMapReduceFeatures.getSecond(), conf);
     }
 
     private Map<Integer, Number> createFeatureAsciiOovDeletion(
             String featureName, GeneralPairWritable3 asciiOovDeletionRule) {
         return features.get(featureName).valueAsciiOovDeletion(
                 new Rule(asciiOovDeletionRule.getFirst()),
                 asciiOovDeletionRule.getSecond(), conf);
     }
 
     private Map<Integer, Number> createFeatureGlueRule(String featureName,
             GeneralPairWritable3 glueRule) {
         return features.get(featureName).valueGlue(
                 new Rule(glueRule.getFirst()), glueRule.getSecond(), conf);
     }
 
     private GeneralPairWritable3
             createFeatures(GeneralPairWritable3 ruleAndMapReduceFeatures) {
         GeneralPairWritable3 res = new GeneralPairWritable3();
         res.setFirst(ruleAndMapReduceFeatures.getFirst());
         SortedMapWritable allFeatures = new SortedMapWritable();
         for (String featureName: selectedFeatures) {
             if (featureName.equals(
                     "provenance_source2target_lexical_probability")
                     || featureName
                             .equals("provenance_target2source_lexical_probability")
                     || featureName
                             .equals("provenance_source2target_probability")
                     || featureName
                             .equals("provenance_target2source_probability")) {
                 for (String provenance: conf.get("provenance").split(",")) {
                     Map<Integer, Number> features =
                             createFeatures(featureName + "-" + provenance,
                                     ruleAndMapReduceFeatures);
                     for (Integer featureIndex: features.keySet()) {
                         IntWritable featureIndexWritable =
                                 new IntWritable(featureIndex);
                         if (allFeatures.containsKey(featureIndexWritable)) {
                             System.err.println(
                                     "ERROR: feature index already exists: "
                                             + featureIndex);
                             System.exit(1);
                         }
                         Number feature = features.get(featureIndex);
                         allFeatures.put(featureIndexWritable,
                                 new DoubleWritable(feature.doubleValue()));
                     }
                 }
             }
             else {
                 Map<Integer, Number> features =
                         createFeatures(featureName, ruleAndMapReduceFeatures);
                 for (Integer featureIndex: features.keySet()) {
                     IntWritable featureIndexWritable =
                             new IntWritable(featureIndex);
                     if (allFeatures.containsKey(featureIndexWritable)) {
                         System.err
                                 .println("ERROR: feature index already exists: "
                                         + featureIndex);
                         System.exit(1);
                     }
                     Number feature = features.get(featureIndex);
                     allFeatures.put(featureIndexWritable, new DoubleWritable(
                             feature.doubleValue()));
                 }
             }
         }
         res.setSecond(allFeatures);
         return res;
     }
 
     private GeneralPairWritable3 createFeaturesAsciiOovDeletion(
             GeneralPairWritable3 asciiOovDeletionRule) {
         GeneralPairWritable3 res = new GeneralPairWritable3();
         res.setFirst(asciiOovDeletionRule.getFirst());
         SortedMapWritable allFeatures = new SortedMapWritable();
         for (String featureName: selectedFeatures) {
             if (featureName.equals(
                     "provenance_source2target_lexical_probability")
                     || featureName
                             .equals("provenance_target2source_lexical_probability")
                     || featureName
                             .equals("provenance_source2target_probability")
                     || featureName
                             .equals("provenance_target2source_probability")) {
                 for (String provenance: conf.get("provenance").split(",")) {
                     Map<Integer, Number> features =
                             createFeatureAsciiOovDeletion(featureName + "-"
                                     + provenance, asciiOovDeletionRule);
                     for (Integer featureIndex: features.keySet()) {
                         IntWritable featureIndexWritable =
                                 new IntWritable(featureIndex);
                         if (allFeatures.containsKey(featureIndexWritable)) {
                             System.err.println(
                                     "ERROR: feature index already exists: "
                                             + featureIndex);
                             System.exit(1);
                         }
                         Number feature = features.get(featureIndex);
                         allFeatures.put(featureIndexWritable,
                                 new DoubleWritable(feature.doubleValue()));
                     }
                 }
             }
             else {
                 Map<Integer, Number> features =
                         createFeatureAsciiOovDeletion(featureName,
                                 asciiOovDeletionRule);
                 for (Integer featureIndex: features.keySet()) {
                     IntWritable featureIndexWritable =
                             new IntWritable(featureIndex);
                     if (allFeatures.containsKey(featureIndexWritable)) {
                         System.err
                                 .println("ERROR: feature index already exists: "
                                         + featureIndex);
                         System.exit(1);
                     }
                     Number feature = features.get(featureIndex);
                     allFeatures.put(featureIndexWritable, new DoubleWritable(
                             feature.doubleValue()));
                 }
             }
         }
         res.setSecond(allFeatures);
         return res;
     }
 
     private GeneralPairWritable3 createFeaturesGlueRule(
             GeneralPairWritable3 glueRule) {
         GeneralPairWritable3 res = new GeneralPairWritable3();
         res.setFirst(glueRule.getFirst());
         SortedMapWritable allFeatures = new SortedMapWritable();
         for (String featureName: selectedFeatures) {
             if (featureName.equals(
                     "provenance_source2target_lexical_probability")
                     || featureName
                             .equals("provenance_target2source_lexical_probability")
                     || featureName
                             .equals("provenance_source2target_probability")
                     || featureName
                             .equals("provenance_target2source_probability")) {
                 for (String provenance: conf.get("provenance").split(",")) {
                     Map<Integer, Number> features =
                             createFeatureGlueRule(featureName + "-"
                                     + provenance, glueRule);
                     for (Integer featureIndex: features.keySet()) {
                         IntWritable featureIndexWritable =
                                 new IntWritable(featureIndex);
                         if (allFeatures.containsKey(featureIndexWritable)) {
                             System.err.println(
                                     "ERROR: feature index already exists: "
                                             + featureIndex);
                             System.exit(1);
                         }
                         Number feature = features.get(featureIndex);
                         allFeatures.put(featureIndexWritable,
                                 new DoubleWritable(feature.doubleValue()));
                     }
                 }
             }
             else {
                 Map<Integer, Number> features =
                         createFeatureGlueRule(featureName, glueRule);
                 for (Integer featureIndex: features.keySet()) {
                     IntWritable featureIndexWritable =
                             new IntWritable(featureIndex);
                     if (allFeatures.containsKey(featureIndexWritable)) {
                         System.err
                                 .println("ERROR: feature index already exists: "
                                         + featureIndex);
                         System.exit(1);
                     }
                     Number feature = features.get(featureIndex);
                     allFeatures.put(featureIndexWritable, new DoubleWritable(
                             feature.doubleValue()));
                 }
             }
         }
         res.setSecond(allFeatures);
         return res;
     }
 
     public List<GeneralPairWritable3> createFeatures(
             List<GeneralPairWritable3> rulesAndMapReduceFeatures) {
         List<GeneralPairWritable3> res = new ArrayList<GeneralPairWritable3>();
         for (GeneralPairWritable3 ruleAndMapReduceFeatures: rulesAndMapReduceFeatures) {
             GeneralPairWritable3 ruleAndFeatures =
                     createFeatures(ruleAndMapReduceFeatures);
             res.add(ruleAndFeatures);
         }
         return res;
     }
 
     public List<GeneralPairWritable3> createFeaturesAsciiOovDeletion(
             List<GeneralPairWritable3> asciiOovDeletionRules) {
         List<GeneralPairWritable3> res = new ArrayList<>();
         for (GeneralPairWritable3 asciiOovDeletionRule: asciiOovDeletionRules) {
             GeneralPairWritable3 asciiOovDeletionRuleAndFeatures =
                     createFeaturesAsciiOovDeletion(asciiOovDeletionRule);
             res.add(asciiOovDeletionRuleAndFeatures);
         }
         return res;
     }
 
     public List<GeneralPairWritable3> createFeaturesGlueRules(
             List<GeneralPairWritable3> glueRules) {
         List<GeneralPairWritable3> res = new ArrayList<>();
         for (GeneralPairWritable3 glueRule: glueRules) {
             GeneralPairWritable3 glueRuleAndFeatures =
                     createFeaturesGlueRule(glueRule);
             res.add(glueRuleAndFeatures);
         }
         return res;
     }
 }
