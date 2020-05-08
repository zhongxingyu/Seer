 /*
  * Copyright (C) 2008-2010 Institute for Computational Biomedicine,
  *                         Weill Medical College of Cornell University
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.bdval;
 
 import com.martiansoftware.jsap.JSAP;
 import com.martiansoftware.jsap.JSAPException;
 import com.martiansoftware.jsap.JSAPResult;
 import edu.mssm.crover.cli.CLI;
 import edu.rit.pj.IntegerForLoop;
 import edu.rit.pj.ParallelRegion;
 import edu.rit.pj.ParallelTeam;
 import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
 import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
 import it.unimi.dsi.fastutil.objects.ObjectArrayList;
 import it.unimi.dsi.fastutil.objects.ObjectArraySet;
 import it.unimi.dsi.fastutil.objects.ObjectIterator;
 import it.unimi.dsi.fastutil.objects.ObjectList;
 import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
 import it.unimi.dsi.fastutil.objects.ObjectSet;
 import it.unimi.dsi.io.FastBufferedReader;
 import it.unimi.dsi.io.LineIterator;
 import it.unimi.dsi.logging.ProgressLogger;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.Iterator;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Performs cross-validation with the consensus features generated for each final model.
  * Evaluates performance measures with such consensus features and write statistics in
  * the maqcii-submission file format.
  *
  * @author Fabien Campagne
  *         Date: June 26, 2008
  */
 public class CrossValidateConsensusFeatures {
     /**
      * Used to log debug and informational messages.
      */
     private static final Logger LOG = Logger.getLogger(CrossValidateConsensusFeatures.class);
     private final ObjectSet<String> ignoreDataset;
     private arguments toolArgs;
 
     public CrossValidateConsensusFeatures() {
         super();
         ignoreDataset = new ObjectArraySet<String>();
     }
 
     public static void main(final String[] args) {
         final CrossValidateConsensusFeatures tool = new CrossValidateConsensusFeatures();
         tool.process(args);
     }
 
     class arguments {
         String modelConditionsFilename;
         String resultsDirectoryPath;
         String submissionFilename;
         String folds;
         String cvRepeats;
         public String propertiesFilename;
         public OptionalModelId[] optionalModelIds;
     }
 
     private void process(final String[] args) {
         final arguments toolsArgs = new arguments();
         toolsArgs.modelConditionsFilename = CLI.getOption(args, "--model-conditions", "model-conditions.txt");
         toolsArgs.resultsDirectoryPath = CLI.getOption(args, "--results-directory", "results");
         toolsArgs.submissionFilename = CLI.getOption(args, "--submission-file", "maqcii-consensus-features-cv.txt");
         toolsArgs.propertiesFilename = CLI.getOption(args, "--properties", null);
 
         toolsArgs.folds = CLI.getOption(args, "--folds", "5");
         toolsArgs.cvRepeats = CLI.getOption(args, "--cv-repeats", "20");
         // the consensus method: features, models or models for PCA pathway runs only (default)
 
         final String featuresDirectoryPath = toolsArgs.resultsDirectoryPath + "/consensus-features";
         System.out.println("Will look for features in directory " + featuresDirectoryPath);
 
         toolsArgs.optionalModelIds = GenerateFinalModels.loadProperties(toolsArgs.propertiesFilename);
 
         final ProgressLogger pg = new ProgressLogger(LOG);
         pg.itemsName = "model conditions";
         final String[] lines = readLines(toolsArgs.modelConditionsFilename);
         if (lines == null) {
             System.out.println("model conditions file cannot be read. " + toolsArgs.modelConditionsFilename);
             System.exit(1);
         }
         pg.expectedUpdates = lines.length;
         pg.priority = Level.INFO;
         pg.start("Processing model conditions");
 
         final ModelParallelRegion region = new ModelParallelRegion(featuresDirectoryPath,
                 pg, LOG, lines, toolsArgs);
 
         try {
             getParallelTeam().execute(region);
         } catch (Exception e) {
             LOG.error("An exception occurred.", e);
         }
 
         pg.stop("Model condition processing complete");
     }
 
     ParallelTeam team;
 
     private class ModelParallelRegion extends ParallelRegion {
         private final int maxIndex;
         private final String[] lines;
 
 
         final String featuresDirectoryPath;
         final ProgressLogger pg;
         final Logger log;
         private final arguments toolArgs;
 
         private ModelParallelRegion(final String featuresDirectoryPath,
                                     final ProgressLogger pg, final Logger log,
                                     final String[] lines, final arguments toolArgs) {
             super();
             this.featuresDirectoryPath = featuresDirectoryPath;
             this.pg = pg;
             this.log = log;
             this.lines = lines;
             this.maxIndex = lines.length - 1;
             this.toolArgs = toolArgs;
 
         }
 
         @Override
         public void run() throws Exception {
             execute(0, maxIndex /* end index is inclusive, this is counter-intuitive */, new IntegerForLoop() {
                 @Override
                 public void run(final int startIndex, final int endIndex) {
                     for (int lineIndex = startIndex; lineIndex <= endIndex; ++lineIndex) {
                         processOneLine(featuresDirectoryPath,
                                 pg,
                                 lines[lineIndex], toolArgs);
 
                     }
                 }
             });
         }
     }
 
     protected synchronized ParallelTeam getParallelTeam() {
 
         if (team == null) {
             team = new ParallelTeam();
             LOG.info("Executing on " + team.getThreadCount() + " threads.");
         }
         return team;
     }
 
     private void processOneLine(final String featuresDirectoryPath,
                                 final ProgressLogger pg, final String conditionLine, final arguments toolArgs) {
         final String[] tokens = conditionLine.split("[\t]");
         final Object2ObjectMap<String, String> map = new Object2ObjectOpenHashMap<String, String>();
 
         if (tokens.length > 0) {
             parse(tokens, map);
 
 
             final ObjectSet<String> featureFilenames = collectFeatureFilenames(featuresDirectoryPath, map);
             final String modelId = map.get("model-id");
             final String datasetName = map.get("dataset-name");
             if (featureFilenames != null) {
                 final String label = extractLabel(datasetName, featureFilenames);
                 this.toolArgs = toolArgs;
                 final String consensusFeatureFilename = featureFilenames.iterator().next();
                 crossValidate(featuresDirectoryPath, map, label, consensusFeatureFilename);
             }
         }
     }
 
 
     private String[] readLines(final String modelConditionsFilename) {
         try {
             final ObjectList<String> lines = new ObjectArrayList<String>();
             final int count = 0;
             final LineIterator it = new LineIterator(new FastBufferedReader(new FileReader(modelConditionsFilename)));
             while (it.hasNext()) {
                 lines.add(it.next().toString());
             }
             return lines.toArray(new String[lines.size()]);
         } catch (FileNotFoundException e) {
             return null;
         }
     }
 
 
     private void crossValidate(final String featuresDirectoryPath,
                                final Object2ObjectMap<String, String> map, final String label,
                                final String consensusFeaturesFilename) {
         System.out.print("Cross-validate consensus features..");
         final String[] args = generateCrossValidatelParameters(featuresDirectoryPath, map, label,
                 consensusFeaturesFilename);
         final JSAP jsap = new JSAP();
         JSAPResult result = null;
         try {
             final CrossValidationMode crossValidationMode = new CrossValidationMode();
             final DAVMode dMode = new DAVMode();
             dMode.defineOptions(jsap);
             crossValidationMode.defineOptions(jsap);
             final DAVOptions options = new DAVOptions();
 
             result = jsap.parse(args);
             if (!result.success()) {
                 final Iterator errors = result.getErrorMessageIterator();
                 while (errors.hasNext()) {
                     final String errorMessage = (String) errors.next();
                     System.out.println(errorMessage);
                 }
             }
             crossValidationMode.interpretArguments(jsap, result, options);
             crossValidationMode.process(options);
             System.out.println("done.");
             System.out.flush();
         } catch (JSAPException e) {
 
             System.out.println("An error occurred executing write-model.");
             e.printStackTrace();
             System.exit(1);
         }
 
 
     }
 
     private String[] generateCrossValidatelParameters(final String featuresDirectoryPath,
                                                       final Object2ObjectMap<String, String> map,
                                                       final String label, final String consensusFeaturesFilename) {
         final Object2ObjectMap<String, String> cvMap = filterMapForCrossValidation(featuresDirectoryPath, map, label,
                 consensusFeaturesFilename);
         return convertToArgs(cvMap);
     }
 
     private Object2ObjectMap<String, String> filterMapForCrossValidation(final String featuresDirectoryPath,
                                                                          final Object2ObjectMap<String, String> map,
                                                                          final String label,
                                                                          final String consensusFeaturesFilename) {
         final Object2ObjectMap<String, String> newMap = new Object2ObjectOpenHashMap<String, String>();
         for (final String key : map.keySet()) {
 
             if ("splits".equals(key)) {
                 continue;
             }
             if ("split-id".equals(key)) {
                 continue;
             }
             if ("split-type".equals(key)) {
                 continue;
             }
             if ("sequence-file".equals(key)) {
                 continue;
             }
             if ("num-features".equals(key)) {
                 continue;
             }
             if ("alpha".equals(key)) {
                 continue;
             }
             if ("feature-selection-mode".equals(key)) {
                 continue;
             }
             if ("evaluate-statistics".equals(key)) {
                 continue;
             }
             if (isAnOptionalModelId(key)) {
                 continue;
             }
             newMap.put(key, map.get(key));
         }
         final String datasetName = map.get("dataset-name");
         final String modelId = map.get("model-id");
         newMap.put("gene-list", modelId + "|" + featuresDirectoryPath + "/" + datasetName + "/" + consensusFeaturesFilename);
         newMap.put("label", label);
         newMap.put("submission-file", toolArgs.submissionFilename);
         newMap.put("folds", toolArgs.folds);
         newMap.put("cv-repeats", toolArgs.cvRepeats);
         return newMap;
     }
 
     private boolean isAnOptionalModelId(final String key) {
         for (final OptionalModelId optionalId : toolArgs.optionalModelIds) {
             if (optionalId.columnIdentifier.equalsIgnoreCase(key)) {
                 return true;
             }
         }
         return false;
     }
 
     private String extractLabel(final String datasetName, final ObjectSet<String> featureFilenames) {
         if (featureFilenames.size() == 0) {
             return null;
         }
         final ObjectSet<String> labels = new ObjectArraySet<String>();
         for (final String filename : featureFilenames) {
 
             final String guessedLabel = guessLabel(datasetName, filename);
             if (guessedLabel != null) {
                 labels.add(guessedLabel);
             }
         }
         assert labels.size() == 1 : " exactly one label can be found across all filenames for a model id. labels were " + printlabels(labels);
         final ObjectIterator<String> objectIterator = labels.iterator();
         objectIterator.hasNext();
         return objectIterator.next();
     }
 
     private String printlabels(final ObjectSet<String> labels) {
         if (labels.size() == 0) {
             return "no labels extracted";
         }
         final StringBuilder sb = new StringBuilder();
         for (final String label : labels) {
             sb.append(label);
             sb.append(' ');
         }
         return sb.toString();
     }
 
     public String guessLabel(final String datasetName, final String filename) {
         // try to guess from predictions filename:
         //e.g., UAMS_EFS_MO-9-pathways-global-svm-weights-GHRXV-features.txt
         final Pattern pattern = Pattern.compile(datasetName + "-(.*)-(.....)-consensus-features.txt");
 
         final Matcher m = pattern.matcher(filename);
         if (!m.matches()) {
             return null;
         }
         final String label = m.group(1);
         System.out.println("label: " + label);
         return label;
     }
 
 
     private String[] convertToArgs(final Object2ObjectMap<String, String> map) {
         map.put("output", "-");
         if (map.containsKey("gene2probes")) {
             map.put("gene-to-probes", map.get("gene2probes"));
             map.remove("gene2probes");
         }
         final ObjectSet<String> ignoreList = new ObjectOpenHashSet<String>();
         ignoreList.add("which-gene-list");
         ignoreList.add("max-intermediate-features");
         ignoreList.add("phi");
         ignoreList.add("maximize");
         ignoreList.add("population-size");
         ignoreList.add("number-of-steps");
         ignoreList.add("ratio");
         ignoreList.add("weka-class");
         ignoreList.add("cache-enabled");
 
         for (final String key : ignoreList) {
             map.remove(key);
         }
 
 
         final ObjectList<String> result = new ObjectArrayList<String>();
         for (final String key : map.keySet()) {
             result.add("--" + key);
             final String value = map.get(key);
             result.add(value);
         }
         final String modelId = map.get("model-id");
 
         System.out.print(modelId + " Executing command with arguments: ");
         for (final String arg : result) {
             System.out.print(arg);
             System.out.print(" ");
         }
         return result.toArray(new String[result.size()]);
     }
 
     private synchronized ObjectSet<String> collectFeatureFilenames(final String featuresDirectoryPath,
                                                                    final Object2ObjectMap<String, String> map) {
 
         final String modelId = map.get("model-id");
         if (modelId == null) {
             LOG.warn("model id cannot be determined for feature directory: " + featuresDirectoryPath);
             return null;
         }
         final String datasetName = map.get("dataset-name");
         if (ignoreDataset.contains(datasetName)) {
             return null;
         }
 
         final File directory = new File(featuresDirectoryPath + "/" + datasetName);
         if (!directory.exists()) {
             System.out.println("Could not find features for dataset " + datasetName);
             // ignoreDataset.add(datasetName);
             return null;
         }
         final ObjectSet<String> featureFilenames = new ObjectArraySet<String>();
         final File[] files = directory.listFiles();
         for (final File file : files) {
             final String filename = file.getName();
             if (filename != null && filename.contains(modelId)) {
                 if (filename.contains("-intermediate-")) {
                     continue;
                 }
 
                 featureFilenames.add(filename);
 
                 System.out.println("Processing model-id: " + modelId + " feature file:" + file.getName());
             }
         }
         return featureFilenames.size() == 0 ? null : featureFilenames;
 
     }
 
     private void parse(final String[] modelConditionTokens, final Object2ObjectMap<String, String> map) {
         for (final String keyValue : modelConditionTokens) {
             //       String[] tokens = keyValue.split("[=]");
             final Pattern p = Pattern.compile("([^=]+)=(.*)");    // greedy match to capture the longest value possible (values may include '=').
             final Matcher matcher = p.matcher(keyValue);
             if (!matcher.matches()) {
                 return;
             }
             final String key = matcher.group(1);
 
 
             final String value = matcher.group(2);
             //  System.out.println("key: " + key + " value: " + value);
             map.put(key, value);
         }
     }
 }
