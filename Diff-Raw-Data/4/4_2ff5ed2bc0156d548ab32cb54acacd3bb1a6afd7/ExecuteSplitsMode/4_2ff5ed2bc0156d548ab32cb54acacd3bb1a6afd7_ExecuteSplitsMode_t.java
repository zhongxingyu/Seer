 /*
  * Copyright (C) 2008-2009 Institute for Computational Biomedicine,
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
 
 import com.martiansoftware.jsap.Flagged;
 import com.martiansoftware.jsap.FlaggedOption;
 import com.martiansoftware.jsap.JSAP;
 import com.martiansoftware.jsap.JSAPException;
 import com.martiansoftware.jsap.JSAPResult;
 import com.martiansoftware.jsap.Parameter;
 import com.martiansoftware.jsap.Switch;
 import edu.cornell.med.icb.cli.UseModality;
 import edu.cornell.med.icb.iterators.IteratorIterable;
 import edu.mssm.crover.cli.CLI;
 import edu.rit.pj.IntegerForLoop;
 import edu.rit.pj.ParallelRegion;
 import edu.rit.pj.ParallelTeam;
 import it.unimi.dsi.fastutil.objects.ObjectArrayList;
 import it.unimi.dsi.fastutil.objects.ObjectArraySet;
 import it.unimi.dsi.fastutil.objects.ObjectList;
 import it.unimi.dsi.fastutil.objects.ObjectSet;
 import it.unimi.dsi.logging.ProgressLogger;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.bdval.util.ShortHash;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 /**
  * Runs a sequence of biomarker discovery operations against a list of dataset splits.
  * The list of splits can be produced by DAVMode --mode define-splits
  *
  * @author Fabien Campagne
  *         Date: Apr 2, 2008
  *         Time: 4:50:28 PM
  */
 public class ExecuteSplitsMode extends DAVMode {
     /**
      * Used to log debug and error messages.
      */
     private static final Logger LOGGER = Logger.getLogger(ExecuteSplitsMode.class);
     private final SplitPlan splitPlan = new SplitPlan();
     private ObjectList<String> paramKeysNotRequired;
     private String splitPlanFilename;
     private String modelId;
     private boolean evaluateStatistics;
     private OptionalModelId[] optionalModelIds = new OptionalModelId[0];
     private TimeLoggingService timeService;
 
     /**
      * Parse properties to extract optional model id definitions. The format is as follow:
      * <p/>
      * <PRE>
      * define.model-id.column-id=modelid-noScaler
      * define.model-id.modelid-noScaler.exclude=a,b
      * define.model-id.modelid-noScaler.exclude.a.argument=scaler-name
      * define.model-id.modelid-noScaler.exclude.a.skip=1
      * define.model-id.modelid-noScaler.exclude.b.argument=normalizer-name
      * define.model-id.modelid-noScaler.exclude.b.skip=1
      * </PRE>
      * These properties would define one new model-id called, to be written in a column called modelid-noScaler,
      * which excludes two arguments and one value each from the hashcode modelId calculation.
      */
     public static OptionalModelId[] parseOptionalModelIdProperties(final Properties configurationProperties) {
         final ObjectList<OptionalModelId> result = new ObjectArrayList<OptionalModelId>();
         if (configurationProperties != null) {
             // inspect properties to figure out which optional model ids to create:
             final ObjectSet<String> optionalModelIdColumnNames = new ObjectArraySet<String>();
 
             for (final String propertyName : configurationProperties.stringPropertyNames()) {
                 if (propertyName.startsWith("define.model-id.column-id")) {
                     final String columnIdNames = configurationProperties.getProperty(propertyName);
                     final String[] names = columnIdNames.split(",");
                     for (final String name : names) {
                         optionalModelIdColumnNames.add(name);
                     }
                 }
             }
 
             for (final String optionalColumnId : optionalModelIdColumnNames) {
                 final String defineModelIdExcludePropertyName = "define.model-id." + optionalColumnId + ".exclude";
                 final String argumentKeys = configurationProperties.getProperty(defineModelIdExcludePropertyName);
                 final String[] keys;
                 if (argumentKeys == null) {
                     System.err.println("Error parsing properties. Cannot find key=" + defineModelIdExcludePropertyName);
                     keys = ArrayUtils.EMPTY_STRING_ARRAY;
                 } else {
                     keys = argumentKeys.split(",");
                 }
 
                 final OptionalModelId newOne = new OptionalModelId(optionalColumnId);
                 for (String key : keys) {
                     key = key.trim();
                     final String excludeArgumentName = configurationProperties.getProperty(defineModelIdExcludePropertyName + "." + key + ".argument");
                     final String excludeArgumentSkip = configurationProperties.getProperty(defineModelIdExcludePropertyName + "." + key + ".skip");
                     newOne.addExcludeArgument(excludeArgumentName, Integer.parseInt(excludeArgumentSkip));
 
                 }
                 result.add(newOne);
                 LOGGER.info("Defined  modelId: " + newOne);
             }
         }
         return result.toArray(new OptionalModelId[result.size()]);
     }
 
     @Override
     public void interpretArguments(
             final JSAP jsap, final JSAPResult result, final DAVOptions options) {
         timeService = new TimeLoggingService("execute-splits");
         timeService.start();
 
         super.interpretArguments(jsap, result, options);
         optionalModelIds = parseOptionalModelIdProperties(configurationProperties);
 
         evaluateStatistics = result.getBoolean("evaluate-statistics");
         if (!evaluateStatistics) {
             System.out.println("Will not evaluate statistics as run proceed. Run restat on results directory to obtain statistics.");
         } else {
             System.out.println("Will evaluate statistics as run proceed.");
         }
         try {
             final String filename = result.getString("splits");
             splitPlanFilename = filename;
             splitPlan.load(filename);
         } catch (Exception e) {
             LOGGER.error("An error occurred reading splits file. " + result.getString("splits"), e);
         }
         // collect keys for parameters that are not required (have default values)
         paramKeysNotRequired = new ObjectArrayList<String>();
         if (!result.userSpecified("seed")) {
             paramKeysNotRequired.add("--seed");
             paramKeysNotRequired.add(Integer.toString(options.randomSeed));
         }
 
         modelId = ShortHash.shortHash(getOriginalArgs());
         options.modelId = modelId;
         timeService.setModelId(options.modelId);
 
         final Map<String, String> additionalConditionsMap = new HashMap<String, String>();
         additionalConditionsMap.put("model-id", modelId);
 
         for (final OptionalModelId optionalModelId : optionalModelIds) {
             final String[] originalArgs1 = expandShortArgs(getOriginalArgs(), jsap);
             final String[] filteredArgs = filterArgs(originalArgs1, optionalModelId);
             final String optionalModelIdValue = ShortHash.shortHash(filteredArgs);
 
             additionalConditionsMap.put(optionalModelId.columnIdentifier, optionalModelIdValue);
         }
 
         final String modelConditionsFilename = "model-conditions.txt";
         final Set<String> skipJsapConditions = new HashSet<String>();
         skipJsapConditions.add("model-id");
         skipJsapConditions.add("mode");
         try {
             writeConditions(modelConditionsFilename, jsap, result,
                     additionalConditionsMap, skipJsapConditions);
         } catch (IOException e) {
             LOGGER.error("Error writing " + modelConditionsFilename + " file", e);
         }
     }
 
     /*
    Replace short argument name, such as "-m" with long argument names (such as "--mode" )
     */
     private String[] expandShortArgs(final String[] originalArgs, final JSAP jsap) {
         int index = 0;
         for (final String arg : originalArgs) {
             if (!arg.startsWith("--") && arg.startsWith("-")) {
                 final String argShortName = arg.substring(1);
                 if (argShortName.length() == 1) {
                     final char argShortNameCharacter = argShortName.charAt(0);
                     final Flagged param = jsap.getByShortFlag(argShortNameCharacter);
                     originalArgs[index] = "--" + param.getLongFlag();
                 }
             }
             index++;
         }
         return originalArgs;
     }
 
     private String[] filterArgs(final String[] originalArgs, final OptionalModelId optionalModelId) {
         final ObjectList<String> filteredArgs = new ObjectArrayList<String>();
         for (int i = 0; i < originalArgs.length; i++) {
             final String argumentName = originalArgs[i].
                     replaceAll("--", "");
             if (optionalModelId.columnIdentifier.equalsIgnoreCase(argumentName)) {      // skip optional modelIds arguments as well.
                 final int skipNumber = 1;
                 LOGGER.info("For optional modelId: " + optionalModelId.columnIdentifier + " Filtering out argument " + argumentName + " total args skipped: " + skipNumber);
 
                 i += skipNumber; // skip argument name and 'skip' number of arguments.
             }
             if (optionalModelId.excludeArgumentNames.contains(argumentName)) {
                 final int skipNumber = optionalModelId.skipValue(argumentName);
                 LOGGER.info("For optional modelId: " + optionalModelId.columnIdentifier + " Filtering out argument " + argumentName + " total args skipped: " + skipNumber);
 
                 i += skipNumber; // skip argument name and 'skip' number of arguments.
             } else {
                 LOGGER.debug("Adding argument to hashCode: " + originalArgs[i]);
                 filteredArgs.add(originalArgs[i]);
             }
 
             if (i >= originalArgs.length) {
                 break;
             }
 
         }
         // Hashcode will depend on argument order, so we sort them after filtering:
         Collections.sort(filteredArgs);
         return filteredArgs.toArray(new String[filteredArgs.size()]);
     }
 
 
     @SuppressWarnings("unchecked")
     public void addSequenceSpecificOptions(final JSAP jsapConfig) throws JSAPException {
         final String sequenceFilename = CLI.getOption(getOriginalArgs(), "--sequence-file", null);
         if (sequenceFilename != null) {
             List<String> fileLines = null;
             try {
                 fileLines = (List<String>) FileUtils.readLines(new File(sequenceFilename));
             } catch (IOException e) {
                 LOGGER.error("Error reading sequence-file " + sequenceFilename, e);
                 return;
             }
 
             for (final String fileLine : fileLines) {
                 if (fileLine.startsWith("addoption ")) {
                     if (fileLine.contains("split-id:") || fileLine.contains("other-options:")) {
                         // split id and other options are handled differently
                         if (LOGGER.isDebugEnabled()) {
                             LOGGER.debug("Skipping addoption line: " + fileLine);
                         }
                         continue;
                     }
                     SequenceMode.addJsapOption(jsapConfig, fileLine, sequenceFilename);
                 }
             }
         }
     }
 
     /**
      * Define command line options for this mode.
      *
      * @param jsap the JSAP command line parser
      * @throws JSAPException if there is a problem building the options
      */
     @Override
     public void defineOptions(final JSAP jsap) throws JSAPException {
         addSequenceSpecificOptions(jsap);
         final Parameter splitParam =
                 new FlaggedOption("splits")
                         .setStringParser(JSAP.STRING_PARSER)
                         .setDefault(JSAP.NO_DEFAULT)
                         .setRequired(true)
                         .setLongFlag("splits")
                         .setHelp("File with the definitions of splits to run.");
         jsap.registerParameter(splitParam);
 
         final Parameter stepsParam =
                 new FlaggedOption("sequence-file")
                         .setStringParser(JSAP.STRING_PARSER)
                         .setRequired(true)
                         .setLongFlag("sequence-file")
                         .setHelp("File with the sequence of steps to execute for each split. "
                                 + "The sequence file will be passed to "
                                 + "DAVMode --sequence for each split.");
         jsap.registerParameter(stepsParam);
 
         final Parameter noStatsParam =
                 new FlaggedOption("evaluate-statistics")
                         .setStringParser(JSAP.BOOLEAN_PARSER)
                         .setDefault("true")
                         .setRequired(false)
                         .setLongFlag("evaluate-statistics")
                         .setHelp("Indicate whether evaluation statistics should be evaluated after all splits are executed. Setting this property to true requires R/ROCR for statistics evaluation.");
         jsap.registerParameter(noStatsParam);
     }
 
     @Override
     public void process(final DAVOptions options) {
        // TODO Does the method  getOriginalArgs (from icb.cli package) support options that appear multiple
        // times on the command line? When --classifier-parameters are repeated on the command line, all but
        // the first seem to disappear?
         final String[] args = getOriginalArgs();
         final UseModality<DAVOptions> executed;
         final int maxSplitIndex = splitPlan.getMaxSplitIndex();
         final ProgressLogger logger = new ProgressLogger(LOGGER);
         logger.expectedUpdates = maxSplitIndex;
         logger.itemsName = "splits";
         logger.priority = Level.INFO;
         logger.start("Parallel split processing");
 
         final SplitParallelRegion region = new SplitParallelRegion(maxSplitIndex, args, logger);
         try {
             getParallelTeam().execute(region);
         } catch (Exception e) {
             LOGGER.error("An exception occurred.", e);
         }
         logger.stop();
 
         /**
          * Time the duration of the sequence:
          */
         timeService.setModelId(modelId);
         timeService.stop();
 
 
         executed = region.getExecuted();
         if (executed != null && executed instanceof SequenceMode) {
             // if we executed SequenceMode
             final SequenceMode sequenceMode = (SequenceMode) executed;
             if (evaluateStatistics) {
                 final String label = sequenceMode.getValue("label");
 
                 final String statsFilename = sequenceMode.getValue("predictions-filename");
 
                 if (statsFilename != null && label != null) {
                     // and the sequence defined the variables "predictions-filename" and "label"
                     try {
                         final String floorParam;
                         if (options.adjustSignalToFloorValue) {
                             floorParam = "--floor " + options.signalFloorValue;
                         } else {
                             floorParam = "";
                         }
 
                         // extract survival options if any
                         // TODO: clean this up - we should not be checking for "%survival%"
                         final String survivalFileName = sequenceMode.getValue("survival");
                         final String survivalOption;
                         if (StringUtils.isNotBlank(survivalFileName) && !"%survival%".equals(survivalFileName)) {
                             survivalOption = " --survival " + survivalFileName + " ";
                         } else {
                             survivalOption = "";
                         }
 
                         final String featureSelectionCode = getFeatureSelectionCode(label);
                         final String sequenceArgs = String.format(
                                 "--mode stats --predictions %s "
                                         + survivalOption
                                         + "--submission-file %s-maqcii-submission.txt "
                                         + "--feature-selection-method %s "
                                         + "--label %s "
                                         + "--model-id %s "
                                         + "--dataset-name %s --folds %d %s --other-measures prec,rec,F-1,MCC,binary-auc",
                                 statsFilename,
                                 labelPrefix(label),
                                 featureSelectionCode,
                                 label,
                                 modelId,
                                 options.datasetName, options.crossValidationFoldNumber, floorParam);
 
                         LOGGER.debug("Estimating statistics: " + sequenceArgs);
                         DiscoverAndValidate.main(buildArguments(sequenceArgs));
 
                     } catch (Exception e) {
                         LOGGER.error("Error executing --mode stats for all splits", e);
                     }
                 }
             }
         }
     }
 
     static {
         final String label = "111-222";
         final String prefix = labelPrefix(label);
         assert prefix.equals("111") : "prefix was " + prefix + " should have been 111";
     }
 
     private static String labelPrefix(final String label) {
 // get the part of label before the first '-', i.e., baseline, tune, pathways, genelists
         final int indexDash = label.indexOf('-');
         if (indexDash == -1) {
             return label;
         } else {
             return label.substring(0, indexDash);
         }
     }
 
     private String getFeatureSelectionCode(final String label) {
         if (label.contains("genelists")) {
             return "Genelists";
         }
         if (label.contains("pathways")) {
             return "Pathways";
         }
         if (label.contains("ttest")) {
             return "T-Test";
         }
         if (label.contains("foldchange")) {
             return "FC";
         }
         if (label.contains("minmax")) {
             return "MinMax";
         } else {
             return label;
         }
     }
 
     ParallelTeam team;
 
     protected synchronized ParallelTeam getParallelTeam() {
 
         if (team == null) {
             team = new ParallelTeam();
             LOGGER.info("Executing on " + team.getThreadCount() + " threads.");
         }
         return team;
     }
 
     private UseModality<DAVOptions> doOneSplit(
             final String[] args, UseModality<DAVOptions> executed, final int splitIndex) {
         try {
             LOGGER.info("Processing split: " + splitIndex);
 
             final DiscoverAndValidate davTool = new DiscoverAndValidate();
             final UseModality<DAVOptions> mode = davTool.processReturnDavMode(
                     adjustArguments(args, splitIndex, paramKeysNotRequired));
             executed = mode.getExecutedModality();
         } catch (Exception e) {
             LOGGER.error("Error executing steps for splitId " + splitIndex, e);
 
         }
         return executed;
     }
 
     private String[] buildArguments(final String s) {
         return s.split("[\\s]+");
     }
 
     @Override
     protected void setupSplitPlan(final JSAPResult result, final DAVOptions options) {
 
         // do not setup the plan in this mode..
     }
 
     private String[] adjustArguments(
             final String[] args, final int splitIndex, final ObjectList<String> explicitOptions) {
         final ObjectList<String> filteredArgs = new ObjectArrayList<String>();
 
         int ignoreCount = 0;
 
         for (final String arg : args) {
             if (arg.equals("--sequence-file")) {
                 ignoreCount = 1;
                 filteredArgs.add("--sequence-file");
 
             }
             if (arg.equals("--mode") || arg.equals("-m")) {
                 ignoreCount = 2;
                 filteredArgs.add("--mode");
                 filteredArgs.add("sequence");
             }
             if (arg.equals("--splits")) {
                 ignoreCount = 2;
             }
             if (arg.equals("--evaluate-statistics")) {
                 ignoreCount = 2;
             }
             if (ignoreCount > 0) {
                 ignoreCount--;
             } else {
                 filteredArgs.add(arg);
             }
 
         }
         filteredArgs.add("--process-split-id");
         filteredArgs.add(Integer.toString(splitIndex));
         filteredArgs.add("--split-id");
         filteredArgs.add(Integer.toString(splitIndex));
 
         filteredArgs.add("--model-id");
         filteredArgs.add(modelId);
 
         filteredArgs.add("--split-plan");
         filteredArgs.add(splitPlanFilename);
         filteredArgs.add("--other-options");
         final StringBuilder argOtherOptions = new StringBuilder();
 
         for (final String option : explicitOptions) {
             argOtherOptions.append(option);
             argOtherOptions.append(' ');
         }
 
         filteredArgs.add(argOtherOptions.toString());
         return filteredArgs.toArray(new String[filteredArgs.size()]);
     }
 
     @SuppressWarnings("unchecked")
     private static void writeConditions(
             final String conditionsFilename, final JSAP jsap,
             final JSAPResult jsapResult,
             final Map<String, String> additionalConditionsMap,
             final Set<String> skipJsapConditions) throws IOException {
         PrintWriter modelConditionsWriter = null;
         try {
             modelConditionsWriter = new PrintWriter(
                     new FileWriter(conditionsFilename, true));
             boolean firstItem = true;
 
 // Write the additional conditions
             for (final String conditionKey : additionalConditionsMap.keySet()) {
                 final String value = additionalConditionsMap.get(conditionKey);
                 if (firstItem) {
                     firstItem = false;
                 } else {
                     modelConditionsWriter.print("\t");
                 }
                 modelConditionsWriter.printf("%s=%s", conditionKey, value);
             }
 
             // Write the JSAP configuration, as configured for ExecuteSplitsMode
             for (final String id : new IteratorIterable<String>(jsap.getIDMap().idIterator())) {
                 if (skipJsapConditions.contains(id)) {
                     // Skip some of the conditions
                     continue;
                 }
                 final Parameter paramObj = jsap.getByID(id);
                 if (paramObj instanceof Switch) {
                     if (jsapResult.getBoolean(id)) {
                         if (firstItem) {
                             firstItem = false;
                         } else {
                             modelConditionsWriter.print("\t");
                         }
                         modelConditionsWriter.printf("%s=enabled", id);
                     }
                 } else if (paramObj instanceof FlaggedOption) {
 // A flag switch exists. Pass it along.
                     final FlaggedOption flagOpt = (FlaggedOption) paramObj;
                     if (jsapResult.contains(id)) {
                         if (firstItem) {
                             firstItem = false;
                         } else {
                             modelConditionsWriter.print("\t");
                         }
                         final String stringVal = SequenceMode.jsapOptionToString(jsapResult, flagOpt);
                         modelConditionsWriter.printf("%s=%s", id, stringVal);
                     }
                 }
             }
             modelConditionsWriter.println();
         } finally {
             IOUtils.closeQuietly(modelConditionsWriter);
             modelConditionsWriter = null;
         }
     }
 
     private class SplitParallelRegion extends ParallelRegion {
         UseModality<DAVOptions> executed;
         final String[] args;
         private final int maxSplitIndex;
         private final ProgressLogger logger;
 
         public UseModality<DAVOptions> getExecuted() {
             return executed;
         }
 
         public SplitParallelRegion(final int maxSplitIndex, final String[] args, final ProgressLogger logger) {
             super();
             this.maxSplitIndex = maxSplitIndex;
             executed = null;
             this.logger = logger;
             this.args = args;
         }
 
 
         @Override
         public void run() throws Exception {
             execute(1, maxSplitIndex /* end index is inclusive, this is counter intuitive */, new IntegerForLoop() {
                 @Override
                 public void run(final int startIndex, final int endIndex) {
                     for (int splitIndex = startIndex; splitIndex <= endIndex; ++splitIndex) {
                         executed = doOneSplit(args, executed, splitIndex);
                         logger.update();
                     }
                 }
             });
         }
     }
 }
