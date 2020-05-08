 /*
  * Copyright (C) 2007-2009 Institute for Computational Biomedicine,
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
 
 import cern.jet.random.engine.MersenneTwister;
 import com.martiansoftware.jsap.FlaggedOption;
 import com.martiansoftware.jsap.JSAP;
 import com.martiansoftware.jsap.JSAPException;
 import com.martiansoftware.jsap.JSAPResult;
 import com.martiansoftware.jsap.Parameter;
 import com.martiansoftware.jsap.Switch;
 import edu.cornell.med.icb.R.RConnectionPool;
 import edu.cornell.med.icb.R.RUtils;
 import edu.cornell.med.icb.cli.UseModality;
 import edu.cornell.med.icb.geo.AffymetrixSampleData;
 import edu.cornell.med.icb.geo.DefaultSignalAdapter;
 import edu.cornell.med.icb.geo.GEOPlatformIndexed;
 import edu.cornell.med.icb.geo.GeoSoftFamilyParser;
 import edu.cornell.med.icb.geo.SampleDataCallback;
 import edu.cornell.med.icb.geo.tools.ClassificationTask;
 import edu.cornell.med.icb.geo.tools.ConditionIdentifiers;
 import edu.cornell.med.icb.geo.tools.DummyPlatform;
 import edu.cornell.med.icb.geo.tools.FileGeneList;
 import edu.cornell.med.icb.geo.tools.FullGeneList;
 import edu.cornell.med.icb.geo.tools.GEOPlatform;
 import edu.cornell.med.icb.geo.tools.GeneList;
 import edu.cornell.med.icb.geo.tools.MicroarrayTrainEvaluate;
 import edu.cornell.med.icb.identifier.DoubleIndexedIdentifier;
 import edu.cornell.med.icb.identifier.IndexedIdentifier;
 import edu.cornell.med.icb.learning.ClassificationHelper;
 import edu.cornell.med.icb.learning.ClassificationProblem;
 import edu.cornell.med.icb.learning.Classifier;
 import edu.cornell.med.icb.learning.FeatureScaler;
 import edu.cornell.med.icb.learning.FeatureTableScaler;
 import edu.cornell.med.icb.learning.LoadClassificationProblem;
 import edu.cornell.med.icb.learning.MinMaxScalingRowProcessor;
 import edu.cornell.med.icb.learning.PercentileScalingRowProcessor;
 import edu.cornell.med.icb.learning.libsvm.LibSvmClassifier;
 import edu.mssm.crover.tables.AcceptAllRowFilter;
 import edu.mssm.crover.tables.ArrayTable;
 import edu.mssm.crover.tables.ColumnTypeException;
 import edu.mssm.crover.tables.DefineColumnFromRow;
 import edu.mssm.crover.tables.IdentifierSetRowFilter;
 import edu.mssm.crover.tables.InvalidColumnException;
 import edu.mssm.crover.tables.KeepSubSetColumnFilter;
 import edu.mssm.crover.tables.RowFilter;
 import edu.mssm.crover.tables.RowFloorAdjustmentCalculator;
 import edu.mssm.crover.tables.SumOfSquaresCalculatorRowProcessor;
 import edu.mssm.crover.tables.Table;
 import edu.mssm.crover.tables.TypeMismatchException;
 import edu.mssm.crover.tables.readers.CologneReader;
 import edu.mssm.crover.tables.readers.ColumbiaTmmReader;
 import edu.mssm.crover.tables.readers.GeoDataSetReader;
 import edu.mssm.crover.tables.readers.IconixReader;
 import edu.mssm.crover.tables.readers.SyntaxErrorException;
 import edu.mssm.crover.tables.readers.TableReader;
 import edu.mssm.crover.tables.readers.UnsupportedFormatException;
 import edu.mssm.crover.tables.readers.WhiteheadResReader;
 import it.unimi.dsi.fastutil.ints.IntArrayList;
 import it.unimi.dsi.fastutil.ints.IntList;
 import it.unimi.dsi.fastutil.ints.IntSet;
 import it.unimi.dsi.fastutil.objects.Object2DoubleLinkedOpenHashMap;
 import it.unimi.dsi.fastutil.objects.ObjectArrayList;
 import it.unimi.dsi.fastutil.objects.ObjectArraySet;
 import it.unimi.dsi.fastutil.objects.ObjectList;
 import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
 import it.unimi.dsi.fastutil.objects.ObjectSet;
 import it.unimi.dsi.io.FastBufferedReader;
 import it.unimi.dsi.io.LineIterator;
 import it.unimi.dsi.lang.MutableString;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.SystemUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.bdval.cache.TableCache;
 import org.bdval.pathways.AverageAcrossPathwayFeatureAggregator;
 import org.bdval.pathways.PCAFeatureAggregator;
 import org.bdval.pathways.PathwayFeatureAggregator;
 import org.bdval.pathways.PathwayInfo;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.Reader;
 import java.util.*;
 import java.util.zip.GZIPInputStream;
 
 /**
  * Main class used to define and process modes for
  * {@link org.bdval.DiscoverAndValidate}.
 * New modes should override this class.  See the
 * <a href="http://bdval.org">BDVal</a> page for more details.
  *
  * @author Fabien Campagne Date: Oct 19, 2007 Time: 2:14:46 PM
  */
 public class DAVMode extends UseModality<DAVOptions> {
     /**
      * Used to log debug and informational messages.
      */
     private static final Log LOG = LogFactory.getLog(DAVMode.class);
 
     /**
      * Container for options common to {@link org.bdval.DiscoverAndValidate}
      * modes.
      */
     private DAVOptions davOptions;
 
     /**
      * The split plan used for this run.
      */
     protected SplitPlan splitPlan;
 
     /**
      * The type of split for this run.
      */
     private String splitType;
 
     /**
      * The id being processed in this run.
      */
     private int splitId;
 
     /**
      * The name of the file containing the split plan for this run.
      */
     private String splitPlanFilename;
 
     /**
      * A cache for data used during processing.
      */
     private TableCache tableCache;
 
     /**
      * Whether or not caching is enabled for this run.
      */
     private boolean isTableCacheEnabled;
 
     /**
      * Used for aggregation of pathway information.
      */
     private PathwayFeatureAggregator pathwayHelper;
 
     /**
      * Type of pathway aggregation in use (i.e., "PCA", "average").
      */
     private String aggregationType;
 
     /**
      * Synchronization object for pathway processing.
      * TODO: probably should not be a String
      */
     private static final String SEMAPHORE = "global".intern();
     /**
      * BDVal configuration properties, from the --properties argument.
      */
     protected Properties configurationProperties;
 
     /**
      * Define basic command line options for this mode.  Individual modes should override
      * making sure that options are reused or removed appropriately.  Options cannot
      * be defined more than once in {@link com.martiansoftware.jsap.JSAP}.
      *
      * @param jsap the JSAP command line parser
      * @throws JSAPException if there is a problem building the options
      */
     @Override
     public void defineOptions(final JSAP jsap) throws JSAPException {
         super.defineOptions(jsap);    // help option is defined by the superclass call
         final Parameter inputFilenameOption = new FlaggedOption("input")
                 .setStringParser(JSAP.STRING_PARSER)
                 .setDefault(JSAP.NO_DEFAULT)
                 .setRequired(true)
                 .setShortFlag('i')
                 .setLongFlag("input")
                 .setHelp("Input filename. This file contains the measurement data used to"
                         + " discover markers.");
         jsap.registerParameter(inputFilenameOption);
 
         final Parameter outputFlag = new FlaggedOption("output")
                 .setStringParser(JSAP.STRING_PARSER)
                 .setDefault(JSAP.NO_DEFAULT)
                 .setRequired(false)
                 .setShortFlag('o')
                 .setLongFlag("output")
                 .setHelp("Name of the output file. Output is printed to the console when this "
                         + "flag is absent or when the value \"-\" is given.");
         jsap.registerParameter(outputFlag);
 
         final Parameter propertiesFlag = new FlaggedOption("properties")
                 .setStringParser(JSAP.STRING_PARSER)
                 .setDefault(JSAP.NO_DEFAULT)
                 .setRequired(false)
 
                 .setLongFlag("properties")
                 .setHelp("Name of the properties file. A Java properties file with bdval specific configuration properties.");
         jsap.registerParameter(propertiesFlag);
 
         final Parameter outputOverwriteFlag = new FlaggedOption("overwrite-output")
                 .setStringParser(JSAP.BOOLEAN_PARSER)
                 .setDefault("false")
                 .setRequired(true)
                 .setLongFlag("overwrite-output")
                 .setHelp("When true and -o is specified, the output file will be over-written.");
         jsap.registerParameter(outputOverwriteFlag);
 
         final Parameter modelIdFlag = new FlaggedOption("model-id")
                 .setStringParser(JSAP.STRING_PARSER)
                 .setDefault("no_model_id")
                 .setRequired(false)
                 .setLongFlag("model-id")
                 .setHelp("The model-id, created in ExecuteSplitsMode (a hash of the options)");
         jsap.registerParameter(modelIdFlag);
 
         final Parameter tasksFlag = new FlaggedOption("task-list")
                 .setStringParser(JSAP.STRING_PARSER)
                 .setDefault(JSAP.NO_DEFAULT)
                 .setRequired(true)
                 .setShortFlag('t')
                 .setLongFlag("task-list")
                 .setHelp("Name of the file that describes the classification tasks. "
                         + "This file is tab delimited, with one line per task. "
                         + "First column is the input filename. "
                         + "Second column is the name of the first condition. "
                         + "Third column is the name of the second condition. "
                         + "Fourth column is the number of samples in the first condition. "
                         + "Fifth column is the number of samples in the second condition.");
         jsap.registerParameter(tasksFlag);
 
         final Parameter geneListsFlag = new FlaggedOption("gene-lists")
                 .setStringParser(JSAP.STRING_PARSER)
                 .setDefault(JSAP.NO_DEFAULT)
                 .setRequired(false)
                 .setShortFlag('g')
                 .setLongFlag("gene-lists")
                 .setHelp("Name of the file that describes the gene lists. "
                         + "This file is tab delimited, with one line per gene list. "
                         + "First column is the name of the gene list. "
                         + "Second column (optional) is the name of the file which describes "
                         + "the gene list. "
                         + "If the file has only one column, the name of the gene list must be "
                         + "full (for the full array). If the name of the gene "
                         + "list is random, the second field indicates how many random probesets "
                         + "must be selected, and a third field indicates the random seed to use "
                         + "for probeset selection."
                 );
         jsap.registerParameter(geneListsFlag);
 
         final Parameter geneListFlag = new FlaggedOption("gene-list")
                 .setStringParser(JSAP.STRING_PARSER)
                 .setDefault(JSAP.NO_DEFAULT)
                 .setRequired(false)
                 .setLongFlag("gene-list")
                 .setHelp("Argument of the form gene-list-name|filename. "
                         + "The filename points to a single gene list file.");
         jsap.registerParameter(geneListFlag);
 
         final Parameter platformFlag = new FlaggedOption("platform-filenames")
                 .setStringParser(JSAP.STRING_PARSER)
                 .setDefault(JSAP.NO_DEFAULT)
                 .setRequired(true)
                 .setShortFlag('p')
                 .setLongFlag("platform-filenames")
                 .setHelp("Comma separated list of platform filenames.");
         jsap.registerParameter(platformFlag);
 
         final Parameter conditionFlag = new FlaggedOption("conditions")
                 .setStringParser(JSAP.STRING_PARSER)
                 .setDefault(JSAP.NO_DEFAULT)
                 .setRequired(true)
                 .setShortFlag('c')
                 .setLongFlag("conditions")
                 .setHelp("Specify the file with the mapping condition-name column-identifier "
                         + "(tab delimited, with one mapping per line).");
         jsap.registerParameter(conditionFlag);
 
         final Parameter seed = new FlaggedOption("seed")
                 .setStringParser(JSAP.INTEGER_PARSER)
                 .setDefault(JSAP.NO_DEFAULT)
                 .setRequired(false)
                 .setLongFlag("seed")
                 .setHelp("Seed to initialize random generator.");
         jsap.registerParameter(seed);
 
         final Parameter pathways = new FlaggedOption("pathways")
                 .setStringParser(JSAP.STRING_PARSER)
                 .setDefault(JSAP.NO_DEFAULT)
                 .setRequired(false)
                 .setLongFlag("pathways")
                 .setHelp("Filename of the pathway description information. "
                         + "The pathway description information is a file with one line per "
                         + "pathway. Each line is tab delimited. The first field provides a "
                         + "pathway identifier. Subsequent fields on the line are "
                         + "Ensembl gene ids for gene that belong to the pathway. "
                         + "When this option is provided, features are aggregated by pathway "
                         + "and computations are performed in aggregated feature space. "
                         + "Some aggregation algorithms may generate several aggregated "
                         + "features per pathway. When this option is active, the "
                         + "option --gene2probes must be provided on the command line."
                 );
         jsap.registerParameter(pathways);
 
         final Parameter pathwayAggregationMethod = new FlaggedOption("pathway-aggregation-method")
                 .setStringParser(JSAP.STRING_PARSER)
                 .setDefault("PCA")
                 .setRequired(false)
                 .setLongFlag("pathway-aggregation-method")
                 .setHelp("Indicate which method should be used to aggregate features for pathway "
                         + "runs. Two methods are available: PCA or average. PCA performs a "
                         + "principal component analysis for the probesets of each pathway. "
                         + "Average uses a single feature for each pathway calculated as the "
                         + "average of the probeset signal in each pathway. Default is PCA.");
         jsap.registerParameter(pathwayAggregationMethod);
 
         final Parameter gene2Probes = new FlaggedOption("gene2probes")
                 .setStringParser(JSAP.STRING_PARSER)
                 .setDefault(JSAP.NO_DEFAULT)
                 .setRequired(false)
                 .setLongFlag("gene-to-probes")
                 .setHelp("Filename of the gene to probe description information. "
                         + "The pathway description information is a file with one line per gene. "
                         + "Each line is tab delimited. The first field is an ensembl gene id. "
                         + "The second field is a probe id which measures expression of a "
                         + "transcript of the gene. Several lines may share the same gene "
                         + "id, indicating that multiple probe ids exist for the gene. ");
         jsap.registerParameter(gene2Probes);
 
         final Parameter floorParam = new FlaggedOption("floor")
                 .setStringParser(JSAP.DOUBLE_PARSER)
                 .setDefault(JSAP.NO_DEFAULT)
                 .setRequired(false)
                 .setLongFlag("floor")
                 .setHelp("Specify a floor value for the signal. If a signal is lower "
                         + "than the floor, it is set to the floor. If no floor is provided, "
                         + "values are unchanged.");
         jsap.registerParameter(floorParam);
 
         final Parameter twoChannelArray = new FlaggedOption("two-channel-array")
                 .setStringParser(JSAP.BOOLEAN_PARSER)
                 .setDefault(JSAP.NO_DEFAULT)
                 .setRequired(false)
                 .setLongFlag("two-channel-array")
                 .setHelp("Indicate that the data is for a two channel array. This flag affects "
                         + "how the floor value is interpreted. "
                         + "For two channel arrays, values on the array are set to 1.0"
                         + "if (Math.abs(oldValue-1.0)+1)<=floorValue, whereas for one channel "
                         + "array the condition becomes: oldValue<=floorValue.");
         jsap.registerParameter(twoChannelArray);
 
         final Parameter log10Array = new FlaggedOption("log-10-array")
                 .setStringParser(JSAP.BOOLEAN_PARSER)
                 .setDefault(JSAP.NO_DEFAULT)
                 .setRequired(false)
                 .setLongFlag("log-10-array")
                 .setHelp("See --logged-array");
         jsap.registerParameter(log10Array);
 
         final Parameter loggedArray = new FlaggedOption("logged-array")
                 .setStringParser(JSAP.BOOLEAN_PARSER)
                 .setDefault(JSAP.NO_DEFAULT)
                 .setRequired(false)
                 .setLongFlag("logged-array")
                 .setHelp("Indicate that the data on this array has been logged. This option "
                         + "affects flooring for two color aryays. When the option is specified, "
                         + "the floor is applied around a center value of zero. When the option "
                         + "is not specified, two color arrays are floored around a value of 1 "
                         + "(no change).");
         jsap.registerParameter(loggedArray);
 
         final Parameter scalingOption = new FlaggedOption("scale-features")
                 .setStringParser(JSAP.BOOLEAN_PARSER)
                 .setDefault("true")
                 .setRequired(false)
                 .setLongFlag("scale-features")
                 .setHelp("Indicate whether the features should be scaled to the range [-1 1]. "
                         + "If false, no scaling occurs. If true (default), features are scaled.");
 
         jsap.registerParameter(scalingOption);
         final Parameter percentileScalingOption = new FlaggedOption("percentile-scaling")
                 .setStringParser(JSAP.BOOLEAN_PARSER)
                 .setDefault("false")
                 .setRequired(false)
                 .setLongFlag("percentile-scaling")
                 .setHelp("Indicate whether feature scaling is done with percentile and median "
                         + "or full range and average. When percentiles are used, the range "
                         + "of each feature is determined as the range of the "
                         + "20-80 percentile of the data and median is used instead of the mean.");
 
         jsap.registerParameter(percentileScalingOption);
 
         final Parameter scalerClassName = new FlaggedOption("scaler-class-name")
                 .setStringParser(JSAP.STRING_PARSER)
                 .setRequired(false)
                 .setLongFlag("scaler-class-name")
                 .setHelp("The classname of the scaler implementation. Overrides "
                         + "--percentile-scaling if provided.");
         jsap.registerParameter(scalerClassName);
 
         final Parameter normalizeOption = new FlaggedOption("normalize-features")
                 .setStringParser(JSAP.BOOLEAN_PARSER)
                 .setDefault("false")
                 .setRequired(false)
                 .setLongFlag("normalize-features")
                 .setHelp("Indicate whether the feature vectors should be normalized. If false, "
                         + "no normalizing occurs. If true (default), features are normalized.");
         jsap.registerParameter(normalizeOption);
 
         final Parameter classifierClassName = new FlaggedOption("classifier")
                 .setStringParser(JSAP.CLASS_PARSER)
                 .setDefault(LibSvmClassifier.class.getCanonicalName())
                 .setRequired(false)
                 .setShortFlag('l')
                 .setLongFlag("classifier")
                 .setHelp("Fully qualified class name of the classifier implementation.");
         jsap.registerParameter(classifierClassName);
 
         final Parameter classifierParameterArgument = new FlaggedOption("classifier-parameters")
                 .setStringParser(JSAP.STRING_PARSER)
                 .setDefault(JSAP.NO_DEFAULT)
                 .setRequired(false)
                 .setShortFlag('a')
                 .setLongFlag("classifier-parameters")
                 .setHelp("Comma separated list of parameters that will be passed to the "
                         + "classifier. Parameters vary from one classifier to the next. Check "
                         + "the documentation of the classifier and the source code to see "
                         + "which parameters can be set.");
         jsap.registerParameter(classifierParameterArgument);
 
         final Parameter geneFeaturesDirArgument =
                 new FlaggedOption("gene-features-dir")
                         .setStringParser(JSAP.STRING_PARSER)
                         .setDefault("." + SystemUtils.FILE_SEPARATOR)
                         .setRequired(false)
                         .setLongFlag("gene-features-dir")
                         .setHelp("The directory where gene features files will be read from"
                                 + " (when specified in a -gene-lists.txt file).");
         jsap.registerParameter(geneFeaturesDirArgument);
 
         final Parameter datasetNameArgument =
                 new FlaggedOption("dataset-name")
                         .setStringParser(JSAP.STRING_PARSER)
                         .setRequired(false)
                         .setDefault("dataset-name")
                         .setLongFlag("dataset-name")
                         .setHelp("The name of the dataset being run.");
         jsap.registerParameter(datasetNameArgument);
 
         // TODO:  ?? Could we be more precise here. An example?
         final Parameter datasetRootArgument =
                 new FlaggedOption("dataset-root")
                         .setStringParser(JSAP.STRING_PARSER)
                         .setRequired(false)
                         .setDefault("ds-root")
                         .setLongFlag("dataset-root")
                         .setHelp("The root directory where the dataset files exist.");
         jsap.registerParameter(datasetRootArgument);
 
         final Switch outputStatsFromGeneListSwitch = new Switch("output-stats-from-gene-list")
                 .setLongFlag("output-stats-from-gene-list");
         jsap.registerParameter(outputStatsFromGeneListSwitch);
 
         final Parameter rservePortFlag = new FlaggedOption("rserve-port")
                 .setStringParser(JSAP.INTEGER_PARSER)
                 .setDefault("-1")
                 .setRequired(false)
                 .setLongFlag("rserve-port")
                 .setHelp("The Rserve port to use");
         jsap.registerParameter(rservePortFlag);
 
         final Parameter splitIdParam = new FlaggedOption("process-split-id")
                 .setStringParser(JSAP.INTEGER_PARSER)
                 .setRequired(false)
                 .setLongFlag("process-split-id")
                 .setHelp("Restricts execution to a split id. A split execution plan must be "
                         + "provided as well. The split id is used together with the split plan to "
                         + "determine which samples should be processed. Typical usage would be "
                         + "\"--process-split-id 2 --split-plan theplan.txt --split-type training\" "
                         + "This would result in training samples being used that match split #2 "
                         + "in theplan.txt.");
         jsap.registerParameter(splitIdParam);
         final Parameter splitPlanParam = new FlaggedOption("split-plan")
                 .setStringParser(JSAP.STRING_PARSER)
                 .setRequired(false)
                 .setLongFlag("split-plan")
                 .setHelp("Filename for the split plan definition. See process-split-id.");
         jsap.registerParameter(splitPlanParam);
 
         final Parameter splitTypeParam = new FlaggedOption("split-type")
                 .setStringParser(JSAP.STRING_PARSER)
                 .setRequired(false)
                 .setLongFlag("split-type")
                 .setHelp("Split type (i.e., training, test, feature-selection, must match "
                         + "a type listed in the split plan). See process-split-id.");
         jsap.registerParameter(splitTypeParam);
 
         final Parameter cacheDirParam = new FlaggedOption("cache-dir")
                 .setStringParser(JSAP.STRING_PARSER)
                 .setDefault("cache")
                 .setRequired(true)
                 .setLongFlag("cache-dir")
                 .setHelp("Cache directory. Specify a directory when intermediate processed "
                         + "tables will be saved for faster access.");
         jsap.registerParameter(cacheDirParam);
 
         final Parameter cacheEnabledParam = new Switch("cache-enabled")
                 .setLongFlag("enable-cache")
                 .setHelp("Enables caching for faster access to processed tables.");
         jsap.registerParameter(cacheEnabledParam);
 
         final Parameter pathwayComponentDirParam = new FlaggedOption("pathway-components-dir")
                 .setStringParser(JSAP.STRING_PARSER)
                 .setDefault("pathway-components")
                 .setRequired(false)
                 .setLongFlag("pathway-components-dir")
                 .setHelp("Directory where pathway components will be stored. ");
         jsap.registerParameter(pathwayComponentDirParam);
     }
 
     /**
      * Interpret the command line arguments.
      *
      * @param jsap    the JSAP command line parser
      * @param result  the results of command line parsing
      * @param options the options for this mode
      */
     @Override
     public void interpretArguments(final JSAP jsap, final JSAPResult result,
                                    final DAVOptions options) {
         checkArgumentsSound(jsap, result, false);
         setupProperties(jsap, result);
         setupModelId(result, options);
         setupDatasetName(result, options);
         setupDatasetRoot(result, options);
         setupSplitPlan(result, options);
         setupPathwayOptions(result, options);
         setupTableCache(result, options);
         setupTaskAndConditions(result, options);
         setupInput(result, options);
         setupPlatforms(result, options);
         setupGeneFeaturesDir(result, options);
         setupGeneLists(result, options);
         setupRandomArguments(result, options);
         setupArrayAttributes(result, options);
         setupClassifier(result, options);
         setupRservePort(result, options);
         setupOutput(result, options);
         setupScalerOptions(result, options);
     }
 
     private void setupProperties(JSAP jsap, JSAPResult result) {
         if (result.contains("properties")) {
 
             String propsFilename = result.getString("properties");
             System.out.println("Loading BDVal  properties from file " + propsFilename);
             Properties props = new Properties();
             try {
                 props.load(new FileReader(propsFilename));
                 this.configurationProperties = props;
             } catch (IOException e) {
                 LOG.error("Cannot load BDVal properties file " + propsFilename, e);
                 System.exit(1);
             }
         }
     }
 
     /**
      * Initialize state of the cache and the cache itself if enabled.
      *
      * @param result  the results of command line parsing
      * @param options the options for this mode
      */
     protected void setupTableCache(final JSAPResult result, final DAVOptions options) {
         tableCache = null;
         isTableCacheEnabled = result.getBoolean("cache-enabled");
         if (LOG.isInfoEnabled()) {
             LOG.info("Caching is" + (isTableCacheEnabled ? " " : " NOT ") + "enabled.");
         }
 
         if (isTableCacheEnabled) {
             String tableCacheDirectory = result.getString("cache-dir");
             try {
                 tableCacheDirectory += "/pathways=" + Boolean.toString(options.pathways != null);
                 if (options.pathways != null && !"PCA".equalsIgnoreCase(aggregationType)) {
                     // do not append aggregation= for PCA feature aggregation, so that we can use
                     // the cache built in previous versions:
                     tableCacheDirectory += "/aggregation=" + aggregationType;
                 }
 
                 final File cacheDir = new File(tableCacheDirectory);
                 if (!cacheDir.exists()) {
                     FileUtils.forceMkdir(cacheDir);
                 }
                 tableCache = new TableCache(cacheDir);
             } catch (IOException e) {
                 LOG.error("Cannot setup table cache in directory" + tableCacheDirectory, e);
                 System.exit(1);
             }
         }
     }
 
     /**
      * Determine if a sample id should be removed from consideration because it is not in the
      * split partition under consideration.
      *
      * @param sampleId The id to check
      * @return true if the split plan contains the specifid id or there is no split plan
      */
     protected boolean splitPlanContainsSampleId(final String sampleId) {
         return splitPlan == null || splitPlan.getSampleIds(splitId, splitType).contains(sampleId);
     }
 
     public int getRepeatId() {
         return splitPlan == null ? 0 : splitPlan.getRepeatId(splitId);
     }
 
     protected void setupDatasetName(final JSAPResult result, final DAVOptions options) {
         options.datasetName = result.getString("dataset-name");
     }
 
     protected void setupModelId(final JSAPResult result, final DAVOptions options) {
         if (result.contains("model-id")) {
             options.modelId = result.getString("model-id");
         }
     }
 
     protected void setupDatasetRoot(final JSAPResult result, final DAVOptions options) {
         options.datasetRoot = result.getString("dataset-root");
     }
 
     protected void setupSplitPlan(final JSAPResult result, final DAVOptions options) {
         final boolean splitPlanMaybe = result.contains("split-plan")
                 || result.contains("split-type") || result.contains("process-split-id");
         if (splitPlanMaybe) {
             final boolean splitPlanOk = result.contains("split-plan")
                     && result.contains("split-type") && result.contains("process-split-id");
             if (!splitPlanOk) {
                 System.err.println("Split plan options are not consistent. You must provide "
                         + "all options together or none at all. See --help for details.");
                 System.exit(1);
             } else {
                 splitPlan = new SplitPlan();
                 String filename = null;
                 try {
                     filename = result.getString("split-plan");
                     splitPlanFilename = filename;
                     splitPlan.load(filename);
                     if (splitPlan.getMaxSplitIndex() == 0) {
                         System.err.println("The split plan provided contains no split. "
                                 + "Nothing will be executed.");
                     }
                 } catch (IOException e) {
                     System.err.println("Cannot load split plan from filename: " + filename);
                 }
                 splitType = result.getString("split-type");
                 splitId = result.getInt("process-split-id");
             }
         }
     }
 
     protected void setupPathwayOptions(final DAVOptions options,
                                        final String pathwayAggregationMethod,
                                        final String pathwayInfoFilename,
                                        final String geneToProbesFilename,
                                        final String pathwayComponentsDirectory) {
 
         if (pathwayInfoFilename != null && geneToProbesFilename == null
                 || pathwayInfoFilename == null && geneToProbesFilename != null) {
             System.err.println("--pathways, --gene2probes and optionally "
                     + "--pathway-aggregation-method options must be used together.");
         }
 
         if (pathwayAggregationMethod != null) {
             options.pathwayAggregtionMethod = pathwayAggregationMethod;
 
             if ("PCA".equalsIgnoreCase(pathwayAggregationMethod)) {
 
                 pathwayHelper = new PCAFeatureAggregator(pathwayComponentsDirectory);
             } else if ("average".equalsIgnoreCase(pathwayAggregationMethod)) {
                 pathwayHelper = new AverageAcrossPathwayFeatureAggregator();
             } else {
                 System.err.println("--pathway-aggregation-method must be one of: 'PCA' or 'average'.");
                 System.exit(1);
             }
             aggregationType = pathwayAggregationMethod;
         }
 
         if (pathwayInfoFilename != null) {
             loadGene2Probes(options, geneToProbesFilename);
             loadPathways(options, pathwayInfoFilename);
 
             options.pathwaysInfoFilename = pathwayInfoFilename;
             options.geneToProbeFilename = geneToProbesFilename;
         }
     }
 
     protected void setupPathwayOptions(final JSAPResult result, final DAVOptions options) {
         String pathwayComponentsDir = null;
         String pathwayAggregationMethod = null;
         if (result.contains("pathway-aggregation-method")) {
             pathwayAggregationMethod = result.getString("pathway-aggregation-method");
 
             if (result.contains("pathway-components-dir")) {
                 pathwayComponentsDir = result.getString("pathway-components-dir");
             }
         }
 
         String pathwayFilename = null;
         String gene2ProbeFilename = null;
         if (result.contains("pathways")) {
             pathwayFilename = result.getString("pathways");
             gene2ProbeFilename = result.getString("gene2probes");
 
         }
         setupPathwayOptions(options,
                 pathwayAggregationMethod, pathwayFilename, gene2ProbeFilename, pathwayComponentsDir);
     }
 
     public void loadPathways(final DAVOptions options, final String pathwayFilename) {
         LOG.info("Loading pathway information.");
         FastBufferedReader pathwayReader = null;
         try {
             pathwayReader = new FastBufferedReader(new FileReader(pathwayFilename));
             final LineIterator it = new LineIterator(pathwayReader);
             final IndexedIdentifier pathwayIds = new IndexedIdentifier();
             options.pathways = new ObjectArraySet<PathwayInfo>();
             while (it.hasNext()) {
                 final MutableString line = it.next();
                 final String[] tokens = line.toString().split("[\t]");
                 if (tokens.length <= 1) {
                     continue;
                 }
                 final String[] geneIds = tokens[1].split("[ ]");
                 final MutableString pathwayId = new MutableString(tokens[0]).compact();
                 final int pathwayIndex = pathwayIds.registerIdentifier(pathwayId);
                 final PathwayInfo pi = new PathwayInfo();
                 pi.pathwayId = pathwayId;
                 pi.pathwayIndex = pathwayIndex;
                 final ObjectSet<MutableString> probesetIds = new ObjectOpenHashSet<MutableString>();
 
                 for (final String sGeneId : geneIds) {
                     final MutableString geneId = new MutableString(sGeneId).compact();
                     final IntSet set = options.gene2Probe.getProbesetIndices(geneId);
                     if (set != null) {
                         for (final int probesetIndex : set) {
                             final MutableString probesetId = options.probeIndexMapping.getId(probesetIndex);
                             probesetIds.add(probesetId);
                         }
                     }
                 }
                 pi.probesetIds = probesetIds;
                 pi.probesetIndices = null;
                 options.pathways.add(pi);
             }
 
             options.pathwayIndexMapping = new DoubleIndexedIdentifier(pathwayIds);
         } catch (FileNotFoundException e) {
             System.err.println("Cannot find pathway info filename " + pathwayFilename);
             System.exit(10);
         } finally {
             IOUtils.closeQuietly(pathwayReader);
         }
     }
 
     private void loadGene2Probes(final DAVOptions options, final String gene2ProbeFilename) {
         LOG.info("Loading gene-to-probe mapping information.");
         options.gene2Probe = new Gene2Probesets();
         try {
             options.probeIndexMapping = options.gene2Probe.load(gene2ProbeFilename);
         } catch (FileNotFoundException e) {
             LOG.fatal("Cannot find gene2probe filename " + gene2ProbeFilename);
             System.exit(10);
         }
     }
 
     protected void setupClassifier(final JSAPResult result, final DAVOptions options) {
         options.classiferClass = result.getClass("classifier");
         final String classifierParametersResult =
                 StringUtils.defaultString(result.getString("classifier-parameters"));
         options.classifierParameters = classifierParametersResult.split("[,]");
         options.scaleFeatures = result.getBoolean("scale-features");
         options.percentileScaling = result.getBoolean("percentile-scaling");
         if (result.contains("scaler-class-name")) {
             options.scalerClassName = result.getString("scaler-class-name");
             options.percentileScaling = false;
         } else {
             options.scalerClassName = null;
         }
         options.normalizeFeatures = result.getBoolean("normalize-features");
         if (options.scaleFeatures) {
             LOG.info("Features will be scaled.");
         } else {
             LOG.info("Features will not be scaled.");
         }
     }
 
     protected void setupInput(final JSAPResult result, final DAVOptions options) {
         options.input = result.getString("input");
         if (isTableCacheEnabled) {
             assert tableCache != null : "TableCache must be initialized.";
             if (tableCache.isTableCached(getSplitId(), getSplitType(), options.datasetName)) {
                 // do not load the input file if we are going to work on a cached table..
                 return;
             }
         }
 
         try {
             options.inputTable = readInputFile(options.input);
         } catch (IOException e) {
             System.err.println("Cannot read input file \"" + options.input + "\"");
             LOG.fatal("Cannot read input file \"" + options.input + "\"", e);
             System.exit(10);
         } catch (UnsupportedFormatException e) {
             System.err.println("The format of the input file \"" + options.input
                     + "\" is not supported.");
             LOG.fatal("The format of the input file \"" + options.input
                     + "\" is not supported.", e);
             System.exit(10);
         } catch (SyntaxErrorException e) {
             System.err.println("A syntax error was found in the input file \""
                     + options.input + "\"");
             LOG.fatal("A syntax error was found in the input file \""
                     + options.input + "\"", e);
             System.exit(10);
         }
     }
 
     protected void setupRservePort(final JSAPResult result, final DAVOptions options) {
         options.rservePort = result.getInt("rserve-port");
         if (options.rservePort > 0) {
             try {
                 // Startup.
                 LOG.info("++ This JVM will use the Rserve at port " + options.rservePort);
                 RConnectionPool.getInstance(RUtils.makeConfiguration(options.rservePort));
             } catch (ConfigurationException e) {
                 LOG.error("Could not configure to connect to localhost Rserve at port"
                         + options.rservePort, e);
             }
         }
     }
 
     protected void checkArgumentsSound(final JSAP jsap, final JSAPResult result, final boolean requiresGeneList) {
         if (!result.success()) {
             System.out.println("Error parsing command line.");
             printHelp(jsap);
             System.exit(1);
         }
 
         if (requiresGeneList) {
             if (result.getString("gene-lists") == null && result.getString("gene-list") == null) {
                 System.err.println("One of the --gene-lists or --gene-list options must be provided.");
                 printHelp(jsap);
                 System.exit(1);
             }
         }
     }
 
     protected void setupRandomArguments(final JSAPResult result, final DAVOptions options) {
         final int seed = result.contains("seed") ? result
                 .getInt("seed") : (int) new Date().getTime();
         options.randomGenerator = new MersenneTwister(seed);
         options.randomSeed = seed;
 
     }
 
     protected void setupArrayAttributes(final JSAPResult result, final DAVOptions options) {
         options.adjustSignalToFloorValue = false;
         if (result.contains("floor")) {
             options.signalFloorValue = result.getDouble("floor");
             options.adjustSignalToFloorValue = true;
         }
 
         options.oneChannelArray = !result.contains("two-channel-array");
         options.loggedArray = !(result.contains("log-10-array") || result.contains("logged-array"));
     }
 
     protected void setupGeneFeaturesDir(final JSAPResult result, final DAVOptions options) {
         options.setGeneFeaturesDir(result.getString("gene-features-dir"));
     }
 
     protected void setupGeneLists(final JSAPResult result, final DAVOptions options) {
         String singleGeneListFilename = result.getString("gene-list");
         if (singleGeneListFilename != null) {
             String[] tokens = {singleGeneListFilename, singleGeneListFilename};
             final Vector<GeneList> list = new Vector<GeneList>();
 
             try {
                 if (singleGeneListFilename.split("[|]").length == 2) {
                     // single gene list filename may be suffixed with name of gene list
                     // for instance HM200|/path/to/HM200.txt
                     tokens = singleGeneListFilename.split("[|]");
                     singleGeneListFilename = tokens[1];
                 }
                 final GeneList geneList = GeneList.createList(tokens, "");
                 geneList.setPlatforms(options.platforms);
                 list.add(geneList);
             } catch (IOException e) {
                 LOG.error("Cannot read gene list from file " + singleGeneListFilename, e);
             }
 
             options.geneLists = list.toArray(new GeneList[list.size()]);
         } else {
             final String geneListFilename = result.getString("gene-lists");
             if (geneListFilename != null) {
                 readGeneLists(geneListFilename, options.platforms, options);
             } else {     // use all the probesets if no gene list was specified:
                 options.geneLists = new GeneList[1];
                 options.geneLists[0] = new FullGeneList("full");
             }
         }
     }
 
     protected void setupPlatforms(final JSAPResult result, final DAVOptions options) {
         final String platformFilenames = result.getString("platform-filenames");
 
         readPlatforms(platformFilenames, options);
     }
 
     protected void setupTaskAndConditions(final JSAPResult result, final DAVOptions options) {
         final String taskListFilename = result.getString("task-list");
         final String conditionsFilename = result.getString("conditions");
         LOG.info("Loading task-list: " + taskListFilename);
         LOG.info("Loading cids: " + conditionsFilename);
         try {
             options.classificationTasks = MicroarrayTrainEvaluate
                     .readTasksAndConditions(taskListFilename, conditionsFilename);
             if (splitPlan != null) {
                 options.classificationTasks =
                         filterBySplitPlan(options.classificationTasks, splitPlan, splitId, splitType);
             }
         } catch (IOException e) {
             LOG.error("Unable to read task list or condition files.", e);
         }
     }
 
     private ClassificationTask[] filterBySplitPlan(final ClassificationTask[] classificationTasks, final SplitPlan splitPlan, final int splitId, final String splitType) {
         for (int i = 0; i < classificationTasks.length; i++) {
             classificationTasks[i] = filterBySplitPlan(classificationTasks[i], splitPlan, splitId, splitType);
         }
         return classificationTasks;
     }
 
     /**
      * Filter a task to keep only samples that match a split plan, split id and split type.
      *
      * @param task
      * @param splitPlan
      * @param splitId
      * @param splitType
      * @return
      */
     private ClassificationTask filterBySplitPlan(final ClassificationTask task,
                                                  final SplitPlan splitPlan,
                                                  final int splitId,
                                                  final String splitType) {
         final Set<String> samplesForClass0 =
                 task.getConditionsIdentifiers().getLabelGroup(task.getFirstConditionName());
         samplesForClass0.retainAll(splitPlan.getSampleIds(splitId, splitType));
         if (samplesForClass0.size() == 0) {
             throw new IllegalArgumentException("Condition 0 (" + task.getFirstConditionName()
                     + ") must have some samples.");
         }
         final ConditionIdentifiers cids = new ConditionIdentifiers();
         for (final String negativeSample : samplesForClass0) {
             cids.addIdentifier(task.getFirstConditionName().intern(), negativeSample);
         }
 
         final Set<String> samplesForClass1 =
                 task.getConditionsIdentifiers().getLabelGroup(task.getSecondConditionName());
         samplesForClass1.retainAll(splitPlan.getSampleIds(splitId, splitType));
         if (samplesForClass1.size() == 0) {
             throw new IllegalArgumentException("Condition 1 (" + task.getSecondConditionName()
                     + ") must have some samples.");
         }
 
         for (final String positiveSample : samplesForClass1) {
             cids.addIdentifier(task.getSecondConditionName().intern(), positiveSample);
         }
         task.setConditionsIdentifiers(cids);
         task.setNumberSamplesFirstCondition(samplesForClass0.size());
         task.setNumberSamplesSecondCondition(samplesForClass1.size());
         return task;
     }
 
     public static String makeStatsFileFromGeneListsFile(final GeneList[] geneLists, final String defaultVal) {
         if (geneLists == null) {
             System.out.println(">>> geneLists is null");
             return defaultVal;
         }
         if (geneLists.length == 0) {
             System.out.println(">>> geneLists is empty");
             return defaultVal;
         }
         if (!(geneLists[0] instanceof FileGeneList)) {
             System.out.println(">>> geneList[0] is not a FileGeneList");
             return defaultVal;
         }
         final FileGeneList geneList = (FileGeneList) geneLists[0];
         final String geneListFilename = FilenameUtils.getName(geneList.getFilename());
         if (!geneListFilename.endsWith("-features.txt")) {
             System.out.println(">>> geneList[0] filename does not end in -features.txt, is "
                     + geneListFilename);
             return defaultVal;
         }
         final String[] parts = StringUtils.split(geneListFilename, "-");
         final StringBuilder statsFilename = new StringBuilder();
         for (int i = 0; i < parts.length - 1; i++) {
             statsFilename.append(parts[i]);
             statsFilename.append("-");
         }
         statsFilename.append("stats.txt");
         return statsFilename.toString();
     }
 
     public int getSplitId() {
         return splitId;
     }
 
     public String getSplitType() {
         return splitType;
     }
 
     public String getSplitPlanFilename() {
         return splitPlanFilename;
     }
 
     protected void setupOutput(final JSAPResult result, final DAVOptions options) {
         final String output;
         options.overwriteOutput = result.getBoolean("overwrite-output");
         if (result.getBoolean("output-stats-from-gene-list")) {
             output = makeStatsFileFromGeneListsFile(
                     options.geneLists, result.getString("output"));
         } else {
             output = result.getString("output");
         }
         System.out.println(">>> DAVMode.Output (stats) file is " + output);
 
         if (output == null || output.equals("-")) {
             options.output = new PrintWriter(System.out);
             System.err.println("Output will be written to stdout");
         } else {
             try {
                 // Make sure the path of the output file exists.
                 final String path = FilenameUtils.getFullPath(output);
                 if (StringUtils.isNotBlank(path)) {
                     FileUtils.forceMkdir(new File(path));
                 }
                 synchronized (output.intern()) {
                     final File outputFile = new File(output);
                     options.outputFilePreexist = options.overwriteOutput ? false : outputFile.exists();
                     options.output = new PrintWriter(new FileWriter(output, !options.overwriteOutput));
                     System.out.println("Output will be written to file " + output);
                 }
             } catch (IOException e) {
                 System.err.println(
                         "Cannot create output file for filename " + output);
                 //   e.printStackTrace();
             }
         }
     }
 
     private void readPlatforms(final String platformFilenames, final DAVOptions options) {
         // read platforms:
         options.platforms = new Vector<GEOPlatform>();
         final String[] filenames = platformFilenames.split(",");
         for (final String pFilename : filenames) {
             GEOPlatform platform = new GEOPlatform();
             if ("dummy".equals(pFilename)) {
                 platform = new DummyPlatform();
                 options.platforms.add(platform);
                 System.out.println("Will proceed with dummy platform.");
                 return;
             }
             System.out.print("Reading platform " + pFilename + ".. ");
             System.out.flush();
             try {
                 platform.read(pFilename);
                 System.out.println("done.");
                 System.out.flush();
                 options.platforms.add(platform);
             } catch (IOException e) {
                 LOG.error("Problem reading " + pFilename, e);
                 System.exit(10);
             } catch (SyntaxErrorException e) {
                 LOG.error("Syntax error reading " + pFilename, e);
                 System.exit(10);
             }
         }
     }
 
     private void readGeneLists(final String geneListFilename,
                                final Vector<GEOPlatform> platforms,
                                final DAVOptions options) {
         BufferedReader geneListReader = null;
         try {
             // read gene list info:
             geneListReader = new BufferedReader(new FileReader(geneListFilename));
             String line;
             final Vector<GeneList> list = new Vector<GeneList>();
             while ((line = geneListReader.readLine()) != null) {
                 if (line.startsWith("#")) {
                     continue;
                 }
                 final String[] tokens = line.split("[\t]");
                 if (tokens.length < 1) {
                     throw new IllegalArgumentException("Gene list line must have at least 1 field."
                             + " Line was : " + line);
                 }
                 final GeneList geneList = GeneList.createList(tokens, options.getGeneFeaturesDir());
                 geneList.setPlatforms(platforms);
                 list.add(geneList);
             }
             options.geneLists = list.toArray(new GeneList[list.size()]);
         } catch (FileNotFoundException e) {
             LOG.fatal("Cannot find gene list file: " + geneListFilename, e);
             System.exit(1);
         } catch (IOException e) {
             LOG.error("Cannot read gene list file: " + geneListFilename, e);
             System.exit(2);
         } finally {
             IOUtils.closeQuietly(geneListReader);
         }
     }
 
     /**
      * Reads data from a file and stores it into a {@link edu.mssm.crover.tables.Table}.
      *
      * @param fileName Name of the file to read
      * @return A table that contains data read from the input file
      * @throws SyntaxErrorException       if there is an error in the file
      * @throws IOException                if the input file cannot be read
      * @throws UnsupportedFormatException if the file format is not recognized
      */
     protected Table readInputFile(final String fileName) throws
             IOException, SyntaxErrorException, UnsupportedFormatException {
         System.out.print("Reading input " + fileName + "... ");
         final Table table;
         Reader reader = null;
         try {
             // get a reader object for the file and store the file extension
             final InputStream inputStream = FileUtils.openInputStream(new File(fileName));
             String fileExtension = FilenameUtils.getExtension(fileName);
             if ("gz".equalsIgnoreCase(fileExtension)) {
                 reader = new InputStreamReader(new GZIPInputStream(inputStream));
                 // strip the ".gz" to get the "real" extension
                 fileExtension = FilenameUtils.getExtension(FilenameUtils.getBaseName(fileName));
             } else {
                 reader = new InputStreamReader(inputStream);
             }
 
             if ("soft".equalsIgnoreCase(fileExtension)) {
                 if (fileName.contains("GDS")) {          // GEO dataset
                     final TableReader tableReader = new GeoDataSetReader();
                     table = tableReader.read(reader);
                 } else if (fileName.contains("GSE")) {   // GEO series
                     table = readGeoSeries(reader);
                 } else {
                     throw new UnsupportedFormatException(fileName);
                 }
             } else if ("res".equalsIgnoreCase(fileExtension)) {
                 final TableReader tableReader = new WhiteheadResReader();
                 table = tableReader.read(reader);
             } else if ("tmm".equalsIgnoreCase(fileExtension)) {
                 final TableReader tableReader = new ColumbiaTmmReader();
                 table = tableReader.read(reader);
             } else if ("iconix".equalsIgnoreCase(fileExtension)) {
                 final TableReader tableReader = new IconixReader();
                 table = tableReader.read(reader);
             } else if ("cologne".equalsIgnoreCase(fileExtension)) {
                 final TableReader tableReader = new CologneReader();
                 table = tableReader.read(reader);
             } else {
                 throw new UnsupportedFormatException(fileName);
             }
 
             if (table == null) {
                 System.err.println("The input file could not be read. ");
                 System.exit(1);
             }
             System.out.println("done");
         } finally {
             IOUtils.closeQuietly(reader);
         }
         return table;
     }
 
     private Table readGeoSeries(final Reader reader) {
         final ArrayTable result = new ArrayTable();
 
         final GeoSoftFamilyParser parser = new GeoSoftFamilyParser(reader);
         if (parser.skipToDatabaseSection()) {
             final MutableString databaseName = parser.getSectionAttribute();
             System.out.println("Database: " + databaseName);
         } else {
             System.out.println("No database section found.");
         }
         GEOPlatformIndexed platform = null;
         if (parser.skipToPlatformSection()) {
             final MutableString platformName = parser.getSectionAttribute();
             System.out.println("Platform: " + platformName);
             platform = parser.parsePlatform();
             platform.setName(platformName);
             System.out.println("Platform has "
                     + platform.getNumProbeIds() + " probes.");
         } else {
             System.out.println("No database section found.");
         }
         result.setChunk(platform.getNumProbeIds());
         result.setInitialSize(platform.getNumProbeIds());
         result.addColumn("ID_REF", String.class);
 
         // append probeset ids:
         for (int probeIndex = 0; probeIndex < platform.getNumProbeIds(); probeIndex++) {
             final String probeId = platform.getProbesetIdentifier(probeIndex).toString();
             result.appendObject(0, probeId);
         }
 
         final DefaultSignalAdapter formatAdapter = new DefaultSignalAdapter(true);
         while (parser.skipToSampleSection()) {
             // append signal value for each sample:
             final MutableString sampleName = parser.getSectionAttribute();
             final int newColumnIndex = result.addColumn(sampleName.toString(), double.class);
             final SampleDataCallback callback = formatAdapter.getCallback(platform);
             parser.parseSampleData(platform, callback);
             final AffymetrixSampleData data = (AffymetrixSampleData) callback.getParsedData();
 
             for (final float signal : data.signal) {
                 try {
                     result.appendDoubleValue(newColumnIndex, signal);
                     // result.parseAppend(newColumnIndex, Float.toString(signal));
                 } catch (TypeMismatchException e) {
                     throw new InternalError("Column must be of type double.");
                 }
             }
             formatAdapter.analyzeSampleData(platform, callback, sampleName);
 
         }
 
         return result;
     }
 
     /**
      * Removes any column of the input table not required by the classification task.
      *
      * @param inputTable       Input table.
      * @param labelValueGroups
      * @return filtered version of the input table.
      */
     public Table filterInputTable(final Table inputTable, final List<Set<String>> labelValueGroups) {
         try {
             return MicroarrayTrainEvaluate.filterColumnsForTask(inputTable,
                     labelValueGroups, MicroarrayTrainEvaluate.IDENTIFIER_COLUMN_NAME);
         } catch (TypeMismatchException e) {
             LOG.error(e);
             return null;
         } catch (InvalidColumnException e) {
             LOG.error(e);
             return null;
         }
     }
 
     /**
      * Process with options. This method may exit the JVM at any time.
      *
      * @param options Interpreted options.
      */
     @Override
     public void process(final DAVOptions options) {
         davOptions = options;
     }
 
     /**
      * Process an input data table for a specific gene list, options, and binary classification.
      * This method is used when training a model.
      *
      * @param geneList         The gene list to exclude the data with
      * @param inputTable       The entire data set before filtering
      * @param options          DAVMode options for processing
      * @param labelValueGroups Sets of labels for each classification target. Labels of class 0
      *                         appear first, followed by labels of class 1.
      * @return
      * @throws TypeMismatchException
      * @throws InvalidColumnException
      * @throws ColumnTypeException
      */
     protected Table processTable(final GeneList geneList,
                                  final Table inputTable, final DAVOptions options,
                                  final List<Set<String>> labelValueGroups)
             throws TypeMismatchException, InvalidColumnException, ColumnTypeException {
         return processTable(geneList, inputTable, options, labelValueGroups, false);
     }
 
     /**
      * Process an input data table for a specific gene list, options, and binary classification.
      *
      * @param geneList         The gene list to exclude the data with
      * @param inputTable       The entire data set before filtering
      * @param options          DAVMode options for processing
      * @param labelValueGroups Sets of labels for each classification target. Labels of class 0 appear first, followed by labels of class 1.
      * @param predictOnly      When true, no filtering is done by labelValueGroups. Suitable when the table will be used for prediction with a previously trained model.
      * @return
      * @throws TypeMismatchException
      * @throws InvalidColumnException
      * @throws ColumnTypeException
      */
     protected Table processTable(final GeneList geneList,
                                  final Table inputTable, final DAVOptions options,
                                  final List<Set<String>> labelValueGroups,
                                  final boolean predictOnly) throws InvalidColumnException, ColumnTypeException, TypeMismatchException {
         return processTable(geneList, inputTable, options, labelValueGroups, predictOnly, splitId, splitType);
     }
 
     /**
      * Process an input data table for a specific gene list, options, and binary classification.
      *
      * @param geneList         The gene list to exclude the data with
      * @param inputTable       The entire data set before filtering
      * @param options          DAVMode options for processing
      * @param labelValueGroups Sets of labels for each classification target. Labels of class 0 appear first, followed by labels of class 1.
      * @param predictOnly      When true, no filtering is done by labelValueGroups. Suitable when the table will be used for prediction with a previously trained model.
      * @return
      * @throws TypeMismatchException
      * @throws InvalidColumnException
      * @throws ColumnTypeException
      */
     protected Table processTable(final GeneList geneList,
                                  final Table inputTable, final DAVOptions options,
                                  final List<Set<String>> labelValueGroups,
                                  final boolean predictOnly, final int splitId,
                                  final String splitType) throws TypeMismatchException,
             InvalidColumnException, ColumnTypeException {
         final Table table;
         if (isTableCacheEnabled && tableCache.isTableCached(splitId, splitType, options.datasetName)) {
             table = tableCache.getCachedTable(splitId, splitType, options.datasetName, geneList);
             rebuildTrainingPlatform(options, table);
         } else {
             int idColumnIndex = 0;
             final Table taskSpecificTable = predictOnly ? inputTable : filterInputTable(inputTable, labelValueGroups);
 
             try {
                 idColumnIndex = taskSpecificTable.getColumnIndex(options.IDENTIFIER_COLUMN_NAME);
             } catch (InvalidColumnException e) {
                 e.printStackTrace();
                 System.out.println("Column " + options.IDENTIFIER_COLUMN_NAME +
                         " could not be found in input file. Unable to exclude by probeset.");
                 System.exit(10);
             }
 
             final int[] columnSelection = MicroarrayTrainEvaluate.getDoubleColumnIndices(taskSpecificTable);
 
             normalize(options, taskSpecificTable);
             applyFloor(options, taskSpecificTable, columnSelection);
             //   taskSpecificTable = considerFilterTableByGeneList(geneList, options, labelValueGroups, idColumnIndex,
             //           taskSpecificTable);
 
             System.gc();
             final Table transposedTable = transposeTable(options, taskSpecificTable, idColumnIndex);
             final Table aggregatedTable = doPathwayAggregation(options, transposedTable, splitId, splitType);
             rebuildTrainingPlatform(options, aggregatedTable);
             table = aggregatedTable;
             if (isTableCacheEnabled) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("Saving " + options.datasetName + " to cache");
                 }
                 tableCache.saveTableToCache(splitId, splitType, options.datasetName, table);
             }
         }
 
         final Table result = filterByGeneList(geneList, options, labelValueGroups, table);
         //     scaleFeatures(options, predictOnly, aggregated);
         System.gc();
 
         return result;
 
     }
 
     private Table filterByGeneList(final GeneList geneList, final DAVOptions options,
                                    final List<Set<String>> labelValueGroups,
                                    Table aggregated) {
         final ObjectSet<CharSequence> probeIdOnTable = new ObjectOpenHashSet<CharSequence>();
         for (int colIndex = 1; colIndex < aggregated.getColumnNumber(); colIndex++) {
             probeIdOnTable.add(aggregated.getIdentifier(colIndex));
         }
 
         final Set<String> validProbesetIds = geneList.calculateProbeSetSelection(probeIdOnTable);
         aggregated = filterTable(options, aggregated, geneList);           // exclude by gene list now that pathway features are available.
         //   System.out.println("aggregated: "+aggregated.toString(aggregated,false));
         checkPostFiltering(options, labelValueGroups, aggregated);
         rebuildTrainingPlatform(options, aggregated);
         if (!(validProbesetIds != null && validProbesetIds.size() != 0)) {
             System.err.println("there is no overlap between this gene list and the chip. Stop this evaluation here.");
             System.exit(10);
         }
         return aggregated;
     }
 
     private Table doPathwayAggregation(final DAVOptions options, final Table transposed,
                                        final int splitId, final String splitType) {
         final Table aggregated;
         if (options.pathways != null) {
             // Map probesetIds in the pathwayInfo instances to probeset indices in the trainingPlatform:
             for (final PathwayInfo pi : options.pathways) {
                 final IntList probeIndices = new IntArrayList();
                 final ObjectSet<MutableString> probesetIds = pi.probesetIds;
                 // sort probesetIds by alphabetical order
                 final ObjectList<MutableString> sortedProbesetIds = new ObjectArrayList<MutableString>();
                 sortedProbesetIds.addAll(pi.probesetIds);
                 Collections.sort(sortedProbesetIds);
 
                 // probeset indices will be added in the defined order of the probeset ids:
                 for (final MutableString probeId : probesetIds) {
                     final int anIndex = options.trainingPlatform.getProbeIds().getInt(probeId);
                     // remove result produced when probeset not found on platform:
                     if (anIndex != -1) {
                         probeIndices.add(anIndex);
                     }
                 }
 
                 pi.probesetIndices = probeIndices;
             }
         }
 
 
         final String pathwayComponentPrefix = options.datasetName;
         if (splitId == 0) {
             // generating final model from whole training set. We synchronize so that
             // parallel synchronization of models do not interact with each other
             // (i.e., partial writing and reading of compound file will not work).
             synchronized (SEMAPHORE) {
                 aggregated = pathwayHelper.aggregateFeaturesForPathways(options.pathways, transposed,
                         pathwayComponentPrefix, splitType, splitId);
             }
         } else {
             // independent splits, we can do them in parallel:
             aggregated = pathwayHelper.aggregateFeaturesForPathways(options.pathways, transposed,
                     pathwayComponentPrefix, splitType, splitId);
         }
         return aggregated;
     }
 
     private Table transposeTable(final DAVOptions options, final Table taskSpecificTable, final int idColumnIndex) throws InvalidColumnException, TypeMismatchException, ColumnTypeException {
 
         // 3. Transpose source.
         final DefineColumnFromRow columnHelper =
                 new DefineColumnFromRow(idColumnIndex);
 
         final Table transposed = taskSpecificTable.transpose(columnHelper);
         rebuildTrainingPlatform(options, transposed);
         return transposed;
     }
 
     private void normalize(final DAVOptions options, final Table taskSpecificTable) throws TypeMismatchException, InvalidColumnException {
         if (options.normalizeFeatures) {
             MicroarrayTrainEvaluate.normalizeAcrossConditions(taskSpecificTable);
             if (!options.quiet) {
                 System.out.println("Features are normalized.");
             }
         } else {
             if (!options.quiet) {
                 System.out.println("Features are assumed to have been normalized.");
             }
         }
     }
 
     private Table considerFilterTableByGeneList(final GeneList geneList, final DAVOptions options, final List<Set<String>> labelValueGroups, final int idColumnIndex, Table taskSpecificTable) throws TypeMismatchException, InvalidColumnException {
         final Set<String> validProbesetIds;
         if (options.pathways == null) {
             // no pathway processing, we can exclude the input table and keep only probesets in the gene list.
             validProbesetIds = geneList.calculateProbeSetSelection(taskSpecificTable, idColumnIndex);
 
             final RowFilter myFilter =
                     new IdentifierSetRowFilter(validProbesetIds, idColumnIndex);
             final Table filteredNoPathway = taskSpecificTable.copy(myFilter);
 
             assert taskSpecificTable.getRowNumber() != 0 : "Table must have some rows after filtering with gene list.";
             if (!options.quiet) {
                 System.out.println(
                         "Kept " + filteredNoPathway.getRowNumber() + " features after filtering.");
             }
             if (!options.quiet) {
                 System.out.println("Kept " + filteredNoPathway.getColumnNumber()
                         + " columns after filtering.");
             }
             if (filteredNoPathway.getColumnNumber() == 1) {
                 System.out.println("Condition labels must match input column names for " + labelValueGroups);
                 System.exit(10);
             }
             taskSpecificTable = filteredNoPathway;
         }
         return taskSpecificTable;
     }
 
     private void applyFloor(final DAVOptions options, final Table taskSpecificTable, final int[] columnSelection) throws TypeMismatchException, InvalidColumnException {
         if (options.adjustSignalToFloorValue) {
             // Set a floor on signal at signalFloorValue:
             final RowFloorAdjustmentCalculator floorAdjust =
                     new RowFloorAdjustmentCalculator(columnSelection,
                             options.signalFloorValue,
                             options.oneChannelArray,
                             options.loggedArray);
             taskSpecificTable.processRows(floorAdjust);
         }
     }
 
     protected void scaleFeatures(final DAVOptions options, final boolean predictOnly, final Table aggregated) throws TypeMismatchException, InvalidColumnException {
         if (options.scaleFeatures) { // 5. Scale values per column (one column=one probeset).
 
             LOG.debug("Scaling features");
             FeatureTableScaler scaler = null;
             try {
                 scaler = (FeatureTableScaler) options.scalerClass.newInstance();
             } catch (InstantiationException e) {
                 LOG.error(e);
             } catch (IllegalAccessException e) {
                 LOG.error(e);
             }
 
             assert scaler != null : "scaler must be initialized.";
             if (!predictOnly) {
                 options.probesetScaleMeanMap = new Object2DoubleLinkedOpenHashMap<MutableString>();
                 options.probesetScaleRangeMap = new Object2DoubleLinkedOpenHashMap<MutableString>();
                 scaler.setTrainingMode(options.probesetScaleMeanMap, options.probesetScaleRangeMap);
             } else {
                 assert options.probesetScaleMeanMap != null : "probesetScaleMeanMap must have been initialized";
                 assert options.probesetScaleRangeMap != null : "probesetScaleRangeMap must have been initialized";
                 scaler.setTestSetMode(options.probesetScaleMeanMap, options.probesetScaleRangeMap);
 
             }
             scaler.processTable(aggregated, MicroarrayTrainEvaluate.getDoubleColumnIndices(aggregated));
         } else {
             LOG.info("Features are not scaled.");
         }
     }
 
     private void setupScalerOptions(final JSAPResult result, final DAVOptions options) {
         if (options.scaleFeatures) { // 5. Scale values per column (one column=one probeset).
             LOG.debug("Scaling features");
             if (options.scalerClassName == null) {
                 if (!options.percentileScaling) {
                     LOG.debug("Using mean and min/max estimators for scaling features.");
                     options.scalerClass = MinMaxScalingRowProcessor.class;
                 } else { // new code
                     LOG.debug("Using percentile estimators for scaling features.");
                     options.scalerClass = PercentileScalingRowProcessor.class;
                 }
             } else {
                 try {
                     final Class<?> aClass = Class.forName(options.scalerClassName);
                     options.scalerClass = (Class<? extends FeatureScaler>) aClass;
                 } catch (ClassNotFoundException e) {
                     LOG.error("Scaler class cannot be found (classname:" + options.scalerClassName + ").", e);
                     System.exit(1);
                 }
             }
 
         } else {
             LOG.info("Features are not scaled.");
             options.scalerClass = FeatureScaler.class;
         }
     }
 
     private boolean exceptionOnCheckPostFilteringFail;
 
     public void setExceptionOnCheckPostFilteringFail(
             final boolean exceptionOnCheckPostFilteringFail) {
         this.exceptionOnCheckPostFilteringFail = exceptionOnCheckPostFilteringFail;
     }
 
     public boolean getExceptionOnCheckPostFilteringFail() {
         return exceptionOnCheckPostFilteringFail;
     }
 
     private void checkPostFiltering(final DAVOptions options,
                                     final List<Set<String>> labelValueGroups,
                                     final Table filtered) {
         assert filtered.getRowNumber() != 0 : "Table must have some rows after filtering with gene list.";
         if (!options.quiet) {
             System.out.println("Kept " + filtered.getRowNumber() + " samples after filtering.");
         }
         if (!options.quiet) {
             System.out.println(
                     "Kept " + (filtered.getColumnNumber() - 1) + " features after filtering.");
         }
         if (filtered.getColumnNumber() == 1) {
             System.out.println("Condition labels must match input column names for " + labelValueGroups);
             if (exceptionOnCheckPostFilteringFail) {
                 throw new IllegalArgumentException(
                         "Condition labels must match input column names for " + labelValueGroups);
             } else {
                 System.exit(10);
             }
         }
     }
 
     protected Table filterTable(final DAVOptions options, final Table source, final GeneList geneList) {
         try {
             final Set<String> columnsToKeep = new ObjectOpenHashSet<String>();
             columnsToKeep.add("ID_REF");
             for (int colIndex = 1; colIndex < source.getColumnNumber(); colIndex++) {
                 final String colId = source.getIdentifier(colIndex);
                 if (geneList.isProbesetInList(colId)) {
                     columnsToKeep.add(colId);
                 }
             }
             final Table result = source.copy(new AcceptAllRowFilter(), new KeepSubSetColumnFilter(columnsToKeep));
             /*     final IntList columnIndicesToRemove = new IntArrayList();
                final ObjectList<String> columnIdsToRemove = new ObjectArrayList<String>();
 
                int index=0;
                for (final int toRemoveIndex : columnIndicesToRemove) {
                    result.removeColumn(toRemoveIndex, columnIdsToRemove.get(index++));
                }
             */
 
             rebuildTrainingPlatform(options, result);
             return result;
         } catch (TypeMismatchException e) {
             throw new InternalError("Should not happen.");
         } catch (InvalidColumnException e) {
             throw new InternalError("Should not happen.");
         }
 
     }
 
     private void rebuildTrainingPlatform(final DAVOptions options, final Table finalTable) {
         // associate features with probeset through the trainingPlatform instance:
         options.resetTrainingPlatform();
         int featureIndex = 0;
         for (int columnIndex = 0; columnIndex < finalTable.getColumnNumber(); columnIndex++) {
             final ArrayTable.ColumnDescription column =
                     finalTable.getColumnValues(columnIndex);
             if (!column.identifier.equals("ID_REF")) {
                 final int registeredProbesetIndex = options.registerProbeset(column.identifier);
                 if (LOG.isTraceEnabled()) {
                     LOG.trace(String.format("registering probeset %s index %d",
                             column.identifier, registeredProbesetIndex));
                 }
                 if (featureIndex != registeredProbesetIndex) {
                     throw new IllegalArgumentException("Feature index (" + featureIndex
                             + ") must match probeset index (" + registeredProbesetIndex
                             + ") in training platform");
                 }
                 featureIndex++;
             }
         }
     }
 
     /**
      * Obtain a classifier for training with known labels.
      *
      * @param processedTable
      * @param labelValueGroups
      * @return
      * @throws TypeMismatchException
      * @throws InvalidColumnException
      */
     protected ClassificationHelper getClassifier(final Table processedTable,
                                                  final List<Set<String>> labelValueGroups)
             throws TypeMismatchException, InvalidColumnException {
         final ClassificationHelper helper = new ClassificationHelper();
 // libSVM:
 // Calculate SumOfSquares sum over all samples x.x:
         final SumOfSquaresCalculatorRowProcessor calculator =
                 new SumOfSquaresCalculatorRowProcessor(processedTable,
                         davOptions.IDENTIFIER_COLUMN_NAME);
         processedTable.processRows(calculator);
 // use the svmLight default C value, so that results are comparable:
         final double C =
                 processedTable.getRowNumber() / calculator.getSumOfSquares();
         final double gamma = 1d / processedTable.getColumnNumber(); // 1/<number of features> default for libSVM
         try {
             final Classifier classifier = (Classifier) davOptions.classiferClass.newInstance();
 
             final LoadClassificationProblem loader = new LoadClassificationProblem();
             final ClassificationProblem problem = classifier.newProblem(0);
             loader.load(problem, processedTable, "ID_REF", labelValueGroups);
             helper.problem = problem;
 
             if (classifier instanceof LibSvmClassifier) {
                 // set default value of C
                 classifier.getParameters().setParameter("C", C);
                 classifier.getParameters().setParameter("gamma", gamma);
             }
             if (davOptions.classifierParameters.length == 1 && davOptions.classifierParameters[0].length() == 0) {
                 davOptions.classifierParameters = ArrayUtils.EMPTY_STRING_ARRAY;
             }
             helper.parseParameters(classifier, davOptions.classifierParameters);
             helper.classifier = classifier;
             helper.parameters = classifier.getParameters();
             return helper;
         } catch (IllegalAccessException e) {
             LOG.error("Cannot instantiate classifier.", e);
         } catch (InstantiationException e) {
             LOG.error("Cannot instantiate classifier.", e);
         }
         assert false : "Could not instantiate classifier";
         return null;
     }
 
     /**
      * Filter processed table to keep only a subset of samples.
      *
      * @param processedTable
      * @param keepSampleIds  Those sample ids to keep in the filtered table.
      * @return filtered table.
      */
     protected Table filterSamples(final Table processedTable, final ObjectSet<String> keepSampleIds) {
         final int idColumnIndex = 0;
         final RowFilter myFilter =
                 new IdentifierSetRowFilter(keepSampleIds, idColumnIndex);
 
         try {
             return processedTable.copy(myFilter);
         } catch (TypeMismatchException e) {
             throw new InternalError("Must not happen.");
         } catch (InvalidColumnException e) {
             throw new InternalError("Must not happen.");
         }
 
     }
 
     public void removeFromCache(final int splitId, final String splitType,
                                 final String datasetName) {
         if (isTableCacheEnabled) {
             tableCache.clearFromCache(splitId, splitType, datasetName);
         }
     }
 }
