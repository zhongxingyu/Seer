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
 import edu.cornell.med.icb.geo.tools.FileGeneList;
 import edu.cornell.med.icb.identifier.DoubleIndexedIdentifier;
 import edu.cornell.med.icb.identifier.IndexedIdentifier;
 import edu.cornell.med.icb.tissueinfo.similarity.ScoredTranscriptBoundedSizeQueue;
 import edu.cornell.med.icb.tissueinfo.similarity.TranscriptScore;
 import edu.mssm.crover.cli.CLI;
 import edu.rit.pj.IntegerForLoop;
 import edu.rit.pj.ParallelRegion;
 import edu.rit.pj.ParallelTeam;
 import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
 import it.unimi.dsi.fastutil.objects.Object2IntMap;
 import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
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
 import it.unimi.dsi.lang.MutableString;
 import it.unimi.dsi.logging.ProgressLogger;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Reader;
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Generate a final model for a feature selection strategy.
  * Combines features obtained with various feature selection methods into
  * a single set of consensus features and train a model with these features and the whole training set.
  * Features are ranked by the score tally+(1-pValue), where tally is the number of times the features
  * is found in the individual splits and pValue is the ttest pValue for the feature in the entire training set.
  * In essence, the features which are found more often in individual splits are given priority, and ties in this ordering
  * are resolved by considering the full training set T-Test P-value for each feature.
  * The k Features with largest scores are used to construct the final model. The parameter k is set to the parameter
  * num-features in the model conditions file for the model-id under consideration. All parameters are set according
  * to the model-conditions file for each model id, ensuring that final models are built in exactly the same manner than
  * the evaluation/individual splits model.
  *
  * @author Fabien Campagne
  *         Date: Apr 26, 2008
  *         Time: 9:44:19 AM
  */
 public class GenerateFinalModels {
     /**
      * Used to log debug and informational messages.
      */
     private static final Logger LOG = Logger.getLogger(GenerateFinalModels.class);
     private final ObjectSet<String> ignoreDataset;
     private String modelsDirectoryPath;
 
     public GenerateFinalModels() {
         super();
         ignoreDataset = new ObjectArraySet<String>();
     }
 
     public static void main(final String[] args) {
         final GenerateFinalModels tool = new GenerateFinalModels();
         tool.process(args);
     }
 
     public String getDirectExtension(ConsensusMethod consensusMethod) {
         return consensusMethod == ConsensusMethod.DIRECT_METHOD ? "-direct" : "";
     }
 
     enum ConsensusMethod {
         MODEL_CONSENSUS,
         FEATURE_CONSENSUS,
         PATHWAY_MODELS_CONSENSUS,
         DIRECT_METHOD
     }
 
     /**
      * The method used to generate a final set of models.
      */
     private ConsensusMethod consensusMethod;
 
     private void process(final String[] args) {
         final String modelConditionsFilename = CLI.getOption(args, "--model-conditions", "model-conditions.txt");
         final String resultsDirectoryPath = CLI.getOption(args, "--results-directory", "results");
         final String propertiesFilename = CLI.getOption(args, "--properties", null);
         // the consensus method: features, models or models for PCA pathway runs only (default)
 
         final String consensusMethodName = CLI.getOption(args, "--consensus", "pathways:models");
 
         final String featuresDirectoryPath = resultsDirectoryPath + "/features";
         modelsDirectoryPath = resultsDirectoryPath + "/models";
         final String featuresOutputDirectoryPath = resultsDirectoryPath + "/consensus-features";
         final String modelsOutputDirectoryPath = resultsDirectoryPath + "/final-models";
         if ("features".equalsIgnoreCase(consensusMethodName)) {
             System.out.println("Will generate feature consensus models.");
             consensusMethod = ConsensusMethod.FEATURE_CONSENSUS;
         } else if ("models".equalsIgnoreCase(consensusMethodName)) {
             System.out.println("Will generate model consensus models.");
             consensusMethod = ConsensusMethod.MODEL_CONSENSUS;
         } else if ("pathways:models".equalsIgnoreCase(consensusMethodName)) {
             System.out.println("Will generate pathway models as model consensus. Other models are treated as feature consensus");
             consensusMethod = ConsensusMethod.PATHWAY_MODELS_CONSENSUS;
         } else if ("direct".equalsIgnoreCase(consensusMethodName)) {
             System.out.println("Will use the feature selection method as is on the full training set to generate the final model.");
             consensusMethod = ConsensusMethod.DIRECT_METHOD;
         }
 
         this.optionalModelIds = loadProperties(propertiesFilename);
         forceCreateDir(featuresOutputDirectoryPath);
         forceCreateDir(modelsOutputDirectoryPath);
 
         System.out.println("Will look for features in directory " + featuresDirectoryPath);
         System.out.println("Will write consensus features in directory " + featuresOutputDirectoryPath);
         System.out.println("Will write final models in directory " + modelsOutputDirectoryPath);
 
         final ProgressLogger pg = new ProgressLogger(LOG);
         pg.itemsName = "model conditions";
         final String[] lines = readLines(modelConditionsFilename);
         if (lines == null) {
             System.out.println("model conditions file cannot be read. " + modelConditionsFilename);
             System.exit(1);
         }
         pg.expectedUpdates = lines.length;
         pg.priority = Level.INFO;
         pg.start("Processing model conditions");
 
         final ModelParallelRegion region = new ModelParallelRegion(
                 featuresDirectoryPath, featuresOutputDirectoryPath,
                 modelsOutputDirectoryPath, pg, LOG, lines, consensusMethod);
 
         try {
             getParallelTeam().execute(region);
         } catch (Exception e) {
             LOG.error("An exception occurred.", e);
         }
 
         pg.stop("Model condition processing complete");
     }
 
     public static OptionalModelId[] loadProperties(final String propertiesFilename) {
         if (propertiesFilename != null) {
             final Properties configurationProperties = new Properties();
             Reader propertiesReader = null;
             try {
                 propertiesReader = new FileReader(propertiesFilename);
                 configurationProperties.load(propertiesReader);
             } catch (IOException e) {
                 System.out.println("Cannot load properties file " + propertiesFilename);
                 System.exit(1);
             } finally {
                 IOUtils.closeQuietly(propertiesReader);
             }
             return ExecuteSplitsMode.parseOptionalModelIdProperties(configurationProperties);
         } else {
             return new OptionalModelId[0];
         }
     }
 
     private ParallelTeam team;
 
     private class ModelParallelRegion extends ParallelRegion {
         private final int maxIndex;
         private final String[] lines;
 
 
         final String featuresDirectoryPath;
         final String featuresOutputDirectoryPath;
         final String modelsOutputDirectoryPath;
         final ProgressLogger pg;
         final Logger log;
         final ConsensusMethod consensusMethod;
 
         private ModelParallelRegion(final String featuresDirectoryPath,
                                     final String featuresOutputDirectoryPath,
                                     final String modelsOutputDirectoryPath,
                                     final ProgressLogger pg, final Logger log,
                                     final String[] lines, final ConsensusMethod consensusMethod) {
             super();
             this.featuresDirectoryPath = featuresDirectoryPath;
             this.featuresOutputDirectoryPath = featuresOutputDirectoryPath;
             this.modelsOutputDirectoryPath = modelsOutputDirectoryPath;
             this.pg = pg;
             this.log = log;
             this.lines = lines;
             this.maxIndex = lines.length - 1;
             this.consensusMethod = consensusMethod;
         }
 
         @Override
         public void run() throws Exception {
             execute(0, maxIndex /* end index is inclusive, this is counter-intuitive */, new IntegerForLoop() {
                 @Override
                 public void run(final int startIndex, final int endIndex) {
                     for (int lineIndex = startIndex; lineIndex <= endIndex; ++lineIndex) {
                         processOneLine(featuresDirectoryPath, featuresOutputDirectoryPath, modelsOutputDirectoryPath,
                                 pg,
                                 lines[lineIndex]);
 
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
 
     private void processOneLine(final String featuresDirectoryPath, final String featuresOutputDirectoryPath,
                                 final String modelsOutputDirectoryPath, final ProgressLogger pg, final String conditionLine) {
         final String[] tokens = conditionLine.split("[\t]");
         final Object2ObjectMap<String, String> map = new Object2ObjectOpenHashMap<String, String>();
         ConsensusMethod consensusMethodThisLine = consensusMethod;
 
         if (tokens.length > 0) {
             parse(tokens, map);
             if (consensusMethod == ConsensusMethod.PATHWAY_MODELS_CONSENSUS) {
                 if (map.get("pathways") != null && (map.containsKey("pathway-aggregation-method") ||
                         "PCA".equals(map.get("pathway-aggregation-method")))) {
                     // if the modeling condition is a pathway run built with PCA pathway aggregation (which used to be
                     // default pathway aggregation methods when some MAQCII runs were performed),
                     // build model consensus:
                     System.out.println("Detected pathway model, activating generation of model consensus.");
                     consensusMethodThisLine = ConsensusMethod.MODEL_CONSENSUS;
 
                 } else {
                     // Otherwise, build feature consensus:
                     consensusMethodThisLine = ConsensusMethod.FEATURE_CONSENSUS;
                 }
             }
 
             final ObjectSet<String> featureFilenames = collectFeatureFilenames(featuresDirectoryPath, map);
             final String modelId = map.get("model-id");
             final String datasetName = map.get("dataset-name");
             if (featureFilenames != null) {
 
                 if (!finalModelExists(modelsOutputDirectoryPath, datasetName, modelId, consensusMethodThisLine)) {
                     final int numFeatures = Integer.parseInt(map.get("num-features"));
                     final String label = extractLabel(datasetName, featureFilenames, modelId);
                     if (label != null) {
                         if (consensusMethodThisLine == ConsensusMethod.FEATURE_CONSENSUS) {
                             final String featureConsensusOutputFilename = String.format("%s/%s/%s-%s-%s-consensus-features.txt",
                                     featuresOutputDirectoryPath, datasetName, datasetName, label, modelId);
 
                             final File consensusFeatureFilename = new File(featureConsensusOutputFilename);
                             final boolean consensusFeatureExist = consensusFeatureFilename.exists();
                             if (!consensusFeatureExist) {
                                 final ObjectSet<String> consensusFeatures = calculateConsensus(featuresDirectoryPath +
                                         "/" + datasetName + "/", featureFilenames, numFeatures, map);
                                 assert consensusFeatures != null : "consensus must exist when label is not null.";
 
                                 final String datasetSpecificConsensusFeatureDir = String.format("%s/%s",
                                         featuresOutputDirectoryPath, datasetName);
                                 PrintWriter output = null;
                                 try {
                                     forceCreateDir(datasetSpecificConsensusFeatureDir);
                                     output = new PrintWriter(featureConsensusOutputFilename);
                                     outputFeatures(output, consensusFeatures);
                                 } catch (IOException e) {
                                     LOG.error("An error occurred writing output file.", e);
                                 } finally {
                                     IOUtils.closeQuietly(output);
                                 }
                             }
 
                             // create final model dataset specific subdirectory
                             final String datasetSpecificFinalModelDir = String.format("%s/%s",
                                     modelsOutputDirectoryPath, datasetName);
                             forceCreateDir(datasetSpecificFinalModelDir);
 
                             trainFinalModel(modelsOutputDirectoryPath, map, featureConsensusOutputFilename, label,consensusMethodThisLine);
                             pg.update();
                         } else if (consensusMethodThisLine == ConsensusMethod.MODEL_CONSENSUS) {
                             final ObjectSet<String> modelComponentPrefixes = collectFeatureFilenames(modelsDirectoryPath, map);
                             PrintWriter writer = null;
                             try {
                                 final File modelListTmpFile = File.createTempFile("model-list", ".txt");
                                 FileUtils.forceDeleteOnExit(modelListTmpFile);
                                 writer = new PrintWriter(modelListTmpFile);
 
                                 for (final String modelPrefix : modelComponentPrefixes) {
                                     LOG.debug("modelPrefix: " + modelPrefix);
                                     // old binary format is ".model" - new format is zipped
                                     if (modelPrefix.endsWith(".model") || modelPrefix.endsWith(".zip")) {
                                         writer.println(modelPrefix);
                                         writer.flush();
                                     }
                                 }
                                 writer.close();
 
                                 // create final model dataset specific subdirectory
                                 final String datasetSpecificFinalModelDir = String.format("%s/%s",
                                         modelsOutputDirectoryPath, datasetName);
                                 forceCreateDir(datasetSpecificFinalModelDir);
 
                                 trainFinalModel(modelsOutputDirectoryPath, map, modelListTmpFile.getCanonicalPath(), label,consensusMethod);
                             } catch (IOException e) {
                                 System.out.println("Fatal error: cannot create temporary file to store list of model components..");
                                 System.exit(1);
                             } finally {
                                 IOUtils.closeQuietly(writer);
                             }
                         } else if (consensusMethodThisLine == ConsensusMethod.DIRECT_METHOD) {
                             try {
                                 // Build final feature and model filenames:
                                 final String datasetSpecificConsensusFeatureDir = String.format("%s-direct/%s",
                                         featuresOutputDirectoryPath, datasetName);
 
                                 forceCreateDir(datasetSpecificConsensusFeatureDir);
                                 final String featureConsensusOutputFilename = String.format("%s-direct/%s/%s-%s-%s-consensus-features.txt",
                                         featuresOutputDirectoryPath, datasetName, datasetName, label, modelId);
                                 final PrintWriter output = new PrintWriter(featureConsensusOutputFilename);
 
                                 // create final model dataset specific subdirectory
                                 final String datasetSpecificFinalModelDir = String.format("%s-direct/%s",
                                         modelsOutputDirectoryPath, datasetName);
                                 //       forceCreateDir(modelsOutputDirectoryPath);
                                 forceCreateDir(datasetSpecificFinalModelDir);
 
                                 // Run sequence mode to generate final model:
                                 trainFinalModelDirectMethod(modelsOutputDirectoryPath, map, featureConsensusOutputFilename, label);
                             } catch (IOException e) {
                                 System.out.println("Fatal error: cannot create temporary file to store list of model components..");
                                 System.exit(1);
                             }
                         }
                     }
                 } else {
                     System.out.println("skipping pre-existing final model for model id: " + modelId);
                 }
             }
         }
     }
 
     public static String[] readLines(final String modelConditionsFilename) {
         FastBufferedReader reader = null;
         try {
             final ObjectList<String> lines = new ObjectArrayList<String>();
             reader = new FastBufferedReader(new FileReader(modelConditionsFilename));
             final LineIterator it = new LineIterator(reader);
             while (it.hasNext()) {
                 lines.add(it.next().toString());
             }
             return lines.toArray(new String[lines.size()]);
         } catch (FileNotFoundException e) {
             return null;
         } finally {
             IOUtils.closeQuietly(reader);
         }
     }
 
     private boolean finalModelExists(final String modelsOutputDirectoryPath,
                                      final String datasetName,
                                      final String modelId, ConsensusMethod consensusMethod) {
         final File dir = new File(modelsOutputDirectoryPath + getDirectExtension(consensusMethod) + "/" + datasetName);
         if (!dir.exists() || !dir.isDirectory()) {
             return false;
         }
 
         final String[] matchingFiles = dir.list(new FilenameFilter() {
             public boolean accept(final File dir, final String name) {
                 return name.contains(modelId);
             }
         });
         return matchingFiles.length > 0;
     }
 
     private void forceCreateDir(final String featuresOutputDirectoryPath) {
         try {
             final File consensusFeaturesDir = new File(featuresOutputDirectoryPath);
             if (!consensusFeaturesDir.exists()) {
                 FileUtils.forceMkdir(consensusFeaturesDir);
             }
         } catch (IOException e) {
             System.err.println("An error occurred trying to create the consensus features directory in the results directory.");
         }
     }
 
     private void trainFinalModel(final String modelsOutputDirectoryPath,
                                  final Object2ObjectMap<String, String> map,
                                  final String consensusFeaturesFilename,
                                  final String label,
                                  final ConsensusMethod consensusMethod) {
         System.out.print("Training final model..");
         final String[] args = generateWriteModelParameters(modelsOutputDirectoryPath, map, label, consensusFeaturesFilename, consensusMethod);
         final JSAP jsap = new JSAP();
         JSAPResult result = null;
         try {
             final WriteModel writeModel = new WriteModel();
             final DAVMode dMode = new DAVMode();
             dMode.defineOptions(jsap);
             writeModel.defineOptions(jsap);
             final DAVOptions options = new DAVOptions();
 
             result = jsap.parse(args);
             if (!result.success()) {
                 final Iterator errors = result.getErrorMessageIterator();
                 while (errors.hasNext()) {
                     final String errorMessage = (String) errors.next();
                     System.out.println(errorMessage);
                 }
             }
             writeModel.interpretArguments(jsap, result, options);
             /*   {
                // force writeModel to use the union features rather than the full array.
               // This speeds up loading the cached data and the computations.
                options.geneLists = new GeneList[1];
                options.geneLists[0] = new FixedGeneList(featureUnion.toArray(new String[featureUnion.size()]));
 
            } */
             writeModel.process(options);
             System.out.println("done.");
             System.out.flush();
         } catch (JSAPException e) {
             System.out.println("An error occurred executing write-model.");
             e.printStackTrace();
             System.exit(1);
         }
     }
 
     private void trainFinalModelDirectMethod(final String modelsOutputDirectoryPath,
                                              final Object2ObjectMap<String, String> map,
                                              final String consensusFeaturesFilename,
                                              final String label) {
         System.out.print("Training final model with the direct method..");
         final String[] args = generateDirectSequenceParameters(modelsOutputDirectoryPath, map, label, consensusFeaturesFilename);
         final JSAP jsap = new JSAP();
         JSAPResult result = null;
         try {
             final SequenceMode sequence = new SequenceMode();
             final DAVMode dMode = new DAVMode();
             sequence.setOriginalArgs(args);
             dMode.defineOptions(jsap);
             sequence.defineOptions(jsap);
             final DAVOptions options = new DAVOptions();
 
             result = jsap.parse(args);
             if (!result.success()) {
                 final Iterator errors = result.getErrorMessageIterator();
                 while (errors.hasNext()) {
                     final String errorMessage = (String) errors.next();
                     System.out.println(errorMessage);
                 }
             }
             sequence.interpretArguments(jsap, result, options);
 
             sequence.process(options);
             System.out.println("done.");
             System.out.flush();
         } catch (JSAPException e) {
             System.out.println("An error occurred executing write-model.");
             e.printStackTrace();
             System.exit(1);
         }
     }
 
     private String[] generateWriteModelParameters(final String modelsOutputDirectoryPath,
                                                   final Object2ObjectMap<String, String> map,
                                                   final String label,
                                                   final String consensusFeaturesFilename,
                                                   ConsensusMethod consensusMethod) {
         final Object2ObjectMap<String, String> ttestMap =
                 filterMapForWriteModel(modelsOutputDirectoryPath, map, label, consensusFeaturesFilename, consensusMethod);
         return convertToArgs(ttestMap);
     }
 
     private String[] generateDirectSequenceParameters(final String modelsOutputDirectoryPath,
                                                       final Object2ObjectMap<String, String> map,
                                                       final String label,
                                                       final String consensusFeaturesFilename) {
         final Object2ObjectMap<String, String> directMethodMap = filterMapForSequence(modelsOutputDirectoryPath,
                 map, label, consensusFeaturesFilename);
         return convertToDirectArgs(directMethodMap);
     }
 
     private Object2ObjectMap<String, String> filterMapForSequence(
             final String modelsOutputDirectoryPath,
             final Object2ObjectMap<String, String> map,
             final String label,
             final String consensusFeaturesFilename) {
         final Object2ObjectMap<String, String> newMap = new Object2ObjectOpenHashMap<String, String>();
         boolean libSVMModel = true;
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
             if ("evaluate-statistics".equals(key)) {
                 continue;
             }
             if ("survival".equals(key)) {
                 continue;
             }
             if (isAnOptionalModelId(key)) {
                 continue;
             }
             if (map.get(key).contains("weka.WekaClassifier")) {
                 // if any value contains  weka.WekaClassifier, it is not a libSVM model
                 libSVMModel = false;
             }
             newMap.put(key, map.get(key));
         }
         final String datasetName = map.get("dataset-name");
         final String modelId = map.get("model-id");
         final String modelPrefix = libSVMModel ? "libSVM" : "weka";
 
         final String finalModelPrefix = String.format("%s-direct/%s/" + modelPrefix + "_%s-%s-final-model-%s",
                 modelsOutputDirectoryPath, datasetName, datasetName, label, modelId);
         newMap.put("model-prefix", finalModelPrefix);
         newMap.put("model-final-features-filename", consensusFeaturesFilename);
         newMap.put("sequence-file", convertSequenceForDirect(map.get("sequence-file")));
 
         return newMap;
     }
 
     private String convertSequenceForDirect(final String sequenceFilename) {
         return sequenceFilename.replace(".sequence", "-final-model.sequence");
     }
 
     private Object2ObjectMap<String, String> filterMapForWriteModel(
             final String modelsOutputDirectoryPath,
             final Object2ObjectMap<String, String> map,
             final String label,
             final String consensusFeaturesFilename, ConsensusMethod consensusMethod) {
         final Object2ObjectMap<String, String> newMap = new Object2ObjectOpenHashMap<String, String>();
         boolean libSVMModel = true;
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
             if ("evaluate-statistics".equals(key)) {
                 continue;
             }
             if ("survival".equals(key)) {
                 continue;
             }
             if ("feature-selection-mode".equals(key)) {
                 continue;
             }
             if (isAnOptionalModelId(key)) {
                 continue;
             }
             if (map.get(key).contains("weka.WekaClassifier")) {
                 // if any value contains  weka.WekaClassifier, it is not a libSVM model
                 libSVMModel = false;
             }
             newMap.put(key, map.get(key));
         }
         final String datasetName = map.get("dataset-name");
         final String modelId = map.get("model-id");
         final String modelPrefix = libSVMModel ? "libSVM" : "weka";
 
         final String finalModelPrefix = String.format("%s/%s/" + modelPrefix + "_%s-%s-final-model-%s",
                 modelsOutputDirectoryPath, datasetName, datasetName, label, modelId);
         newMap.put("model-prefix", finalModelPrefix);
         if (consensusMethod == ConsensusMethod.FEATURE_CONSENSUS) {
             newMap.put("gene-list", modelId + "|" + consensusFeaturesFilename);
         } else if (consensusMethod == ConsensusMethod.MODEL_CONSENSUS) {
             newMap.put("consensus-of-models", consensusFeaturesFilename);
         }
         return newMap;
     }
 
     private boolean isAnOptionalModelId(final String key) {
         for (final OptionalModelId optionalId : optionalModelIds) {
             if (optionalId.columnIdentifier.equalsIgnoreCase(key)) {
                 return true;
             }
         }
         return false;
     }
 
     private OptionalModelId[] optionalModelIds = new OptionalModelId[0];
 
     private String extractLabel(final String datasetName,
                                 final ObjectSet<String> featureFilenames, final String modelId) {
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
         if (labels.size() != 1) {
             System.err.println(" exactly one label can be found across all filenames for a model id (" + modelId + "). labels were "
                     + printlabels(labels));
             return null;
         }
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
             sb.append(" ");
         }
         return sb.toString();
     }
 
     public String guessLabel(final String datasetName, final String filename) {
         // try to guess from predictions filename:
         //e.g., UAMS_EFS_MO-9-pathways-global-svm-weights-GHRXV-features.txt
         final Pattern pattern = Pattern.compile(datasetName + "-([0-9]+)-(.*)-(.....)-features.txt");
 
         final Matcher m = pattern.matcher(filename);
         if (!m.matches()) {
             return null;
         }
         final String label = m.group(2);
         return label;
     }
 
     private void outputFeatures(final PrintWriter output, final ObjectSet<String> features) {
         System.out.println("Writing feature consensus to output..");
         for (final String feature : features) {
             output.println(String.format("\t\t\t%s", feature));
         }
         output.flush();
     }
 
     /**
      * Calculate consensus features and returns a set with each feature id in the consensus.
      */
     private ObjectSet<String> calculateConsensus(final String featuresDirectoryPath,
                                                  final ObjectSet<String> featureFilenames,
                                                  final int numFeatures,
                                                  final Object2ObjectMap<String, String> map) {
 
         final IndexedIdentifier features2Index = new IndexedIdentifier();
         final Object2IntMap<String> tally = new Object2IntOpenHashMap<String>();
         tally.defaultReturnValue(0);
         final ObjectSet<String> featureUnion = new ObjectOpenHashSet<String>();
 
         for (final String filename : featureFilenames) {
             try {
                 final FileGeneList geneList = (FileGeneList) FileGeneList.createList(new String[]{filename, filename},
                         featuresDirectoryPath);
                 final Set<String> probeIds = geneList.getProbeIDsSet();
                 for (final String probeId : probeIds) {
                     tally.put(probeId, tally.getInt(probeId) + 1);
                     features2Index.registerIdentifier(new MutableString(probeId));
                     featureUnion.add(probeId);
                 }
             } catch (IOException e) {
                 System.err.println("An error occurred reading feature/gene list " + filename);
                 e.printStackTrace();
                 System.exit(1);
             }
         }
         final String modelId = map.get("model-id");
         System.out.println("Calculating TTest for model id: " + modelId);
         final Object2DoubleMap<MutableString> probeset2PvalueMap = calculateTTests(map, featureFilenames);
         if (probeset2PvalueMap == null) {
             return null;
         }
         // enqueue features by number of times they were found in the input feature lists, break ties with ttest P-value:
         final DoubleIndexedIdentifier reverseIndex = new DoubleIndexedIdentifier(features2Index);
         final ScoredTranscriptBoundedSizeQueue queue = new ScoredTranscriptBoundedSizeQueue(numFeatures);
         for (final String featureId : tally.keySet()) {
             final MutableString feature = new MutableString(featureId).compact();
             double ttestPValue = 1;
             if (!probeset2PvalueMap.containsKey(feature)) {
                 LOG.warn("Cannot find T-test pValue for feature " + featureId + " for model " + modelId);
                 // this can happen if less SVD components are found in the whole training set. Ideally, we would
                 // aggregate the rotation matrices from the the different splits to avoid this problem, but this has not
                 // been done yet.
 // TODO extend to aggregate rotation matrices for SVD/PCA components.
             } else {
                 ttestPValue = probeset2PvalueMap.get(feature);
             }
 
             final int occurencesInFeatureLists = tally.getInt(feature.toString());
             final double score = occurencesInFeatureLists + (1 - ttestPValue);
             LOG.debug("enqueing feature: " + feature + " score: " + score + " ttest: " + ttestPValue + " occurencesInFeatureLists: " + occurencesInFeatureLists);
             queue.enqueue(features2Index.get(feature), score);
         }
 
         final ObjectSet<String> consensusFeatures = new ObjectOpenHashSet<String>();
         while (!queue.isEmpty()) {
             final TranscriptScore t = queue.dequeue();
             LOG.debug("dequeing feature: " + reverseIndex.getId(t.transcriptIndex) + " score: " + t.score);
 
             consensusFeatures.add(reverseIndex.getId(t.transcriptIndex).toString());
         }
         return consensusFeatures;
     }
 
     private Object2DoubleMap<MutableString> calculateTTests(
             final Object2ObjectMap<String, String> map,
             final ObjectSet<String> featureFilenames) {
         final String modelId = map.get("model-id");
         final String datasetName = map.get("dataset-name");
         final String label = extractLabel(datasetName, featureFilenames, modelId);
         if (label == null) {
             return null;
         }
 
         final String[] args = generateTTestParameters(map);
         final JSAP jsap = new JSAP();
         JSAPResult result = null;
         try {
             final DiscoverWithTTest ttest = new DiscoverWithTTest();
             final DAVMode dMode = new DAVMode();
             dMode.defineOptions(jsap);
             ttest.defineOptions(jsap);
             final DAVOptions options = new DAVOptions();
 
             result = jsap.parse(args);
             if (!result.success()) {
                 final Iterator errors = result.getErrorMessageIterator();
                 while (errors.hasNext()) {
                     final String errorMessage = (String) errors.next();
                     System.out.println(errorMessage);
                 }
             }
             ttest.interpretArguments(jsap, result, options);
 
             ttest.process(options);
             return ttest.probesetPvalues;
         } catch (JSAPException e) {
 
             System.out.println("An error occurred executing ttest.");
             e.printStackTrace();
             System.exit(1);
         }
         return null;
     }
 
     private String[] generateTTestParameters(final Object2ObjectMap<String, String> map) {
         final Object2ObjectMap<String, String> ttestMap = filterMapForTTest(map);
         return convertToArgs(ttestMap);
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
         ignoreList.add("folds");
         ignoreList.add("ratio");
         ignoreList.add("cv-repeats");
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
 
     private String[] convertToDirectArgs(final Object2ObjectMap<String, String> map) {
         map.put("output", "-");
         if (map.containsKey("gene2probes")) {
             map.put("gene-to-probes", map.get("gene2probes"));
             map.remove("gene2probes");
         }
         final ObjectSet<String> ignoreList = new ObjectOpenHashSet<String>();
         ignoreList.add("which-gene-list");
         ignoreList.add("cv-repeats");
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
 
     private Object2ObjectMap<String, String> filterMapForTTest(
             final Object2ObjectMap<String, String> map) {
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
             if ("survival".equals(key)) {
                 continue;
             }
             if ("weka-class".equals(key)) {
                 continue;
             }
             if ("evaluate-statistics".equals(key)) {
                 continue;
             }
             if ("feature-selection-mode".equals(key)) {
                 continue;
             }
             if (isAnOptionalModelId(key)) {
                 continue;
             }
             newMap.put(key, map.get(key));
         }
         // We'll get the P-value in a map, we don't need to see the output for features. So, we put a threshold of 0
         newMap.put("alpha", "0.05");   // change to see some features as produced in complete training set.
         return newMap;
     }
 
 
     private synchronized ObjectSet<String> collectFeatureFilenames(
             final String featuresDirectoryPath,
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
 
     private void parse(final String[] modelConditionTokens,
                        final Object2ObjectMap<String, String> map) {
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
