 package txtfnnl.pipelines;
 
 import java.io.*;
 import java.util.LinkedList;
 import java.util.logging.Logger;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.apache.commons.io.IOUtils;
 import org.apache.uima.UIMAException;
 import org.apache.uima.analysis_engine.AnalysisEngineDescription;
 import org.apache.uima.resource.ExternalResourceDescription;
 import org.apache.uima.resource.ResourceInitializationException;
 
 import txtfnnl.uima.ConfigurationBuilder;
 import txtfnnl.uima.analysis_component.*;
 import txtfnnl.uima.analysis_component.opennlp.SentenceAnnotator;
 import txtfnnl.uima.analysis_component.opennlp.TokenAnnotator;
 import txtfnnl.uima.collection.OutputWriter;
 import txtfnnl.uima.collection.RelationshipWriter;
 import txtfnnl.uima.collection.XmiWriter;
 import txtfnnl.uima.resource.*;
 
 /**
  * A pattern-based relationship extractor between normalized entities.
  * <p>
  * Input files can be read from a directory or listed explicitly, while output lines are written to
  * some directory or to STDOUT. Relationships are written on a single line. A relationship consists
  * of the document URL, the relationship type, the actor entity ID, the target entity ID, and the
  * sentence evidence where the relationship was found, all separated by tabs.
  * <p>
  * The default setup assumes gene and/or protein entities found in a <a
  * href="https://github.com/fnl/gnamed">gnamed</a> database.
  * 
  * @author Florian Leitner
  */
 public class RelationshipExtractor extends Pipeline {
   static final String DEFAULT_DATABASE = "gnamed";
   static final String DEFAULT_JDBC_DRIVER = "org.postgresql.Driver";
   static final String DEFAULT_DB_PROVIDER = "postgresql";
   // default entity sets: all human, mouse, and rat gene symbols (roughly 500k symbols)
   static final String REGULATOR_SQL_QUERY = 
        "SELECT g.id, g.species_id, ps.value FROM genes AS g INNER JOIN genes2proteins AS g2p ON (g.id = g2p.gene_id) INNER JOIN protein_strings AS ps ON (g2p.protein_id = ps.id) WHERE ps.cat = 'symbol' UNION SELECT g.id, g.species_id, gs.value FROM genes AS g INNER JOIN gene_strings AS gs USING (id) WHERE gs.cat = 'symbol' AND g.species_id IN (9606, 10090, 10116) ";
   static final String TARGET_SQL_QUERY =
        "SELECT g.id, g.species_id, ps.value FROM genes AS g INNER JOIN genes2proteins AS g2p ON (g.id = g2p.gene_id) INNER JOIN protein_strings AS ps ON (g2p.protein_id = ps.id) WHERE ps.cat = 'symbol' UNION SELECT g.id, g.species_id, gs.value FROM genes AS g INNER JOIN gene_strings AS gs USING (id) WHERE gs.cat = 'symbol' AND g.species_id IN (9606, 10090, 10116) ";
 
   private RelationshipExtractor() {
     throw new AssertionError("n/a");
   }
 
   public static void main(String[] arguments) {
     final CommandLineParser parser = new PosixParser();
     final Options opts = new Options();
     final String regulatorNamespace = "regulator";
     final String targetNamespace = "target";
     CommandLine cmd = null;
     Pipeline.addLogHelpAndInputOptions(opts);
     Pipeline.addTikaOptions(opts);
     Pipeline.addJdbcResourceOptions(opts, DEFAULT_JDBC_DRIVER, DEFAULT_DB_PROVIDER,
         DEFAULT_DATABASE);
     Pipeline.addOutputOptions(opts);
     // sentence splitter options
     Pipeline.addSentenceAnnotatorOptions(opts);
     // sentence filter options
     opts.addOption("sentences", true, "retain sentences using a file of regex matches");
     opts.addOption("removesentences", false, "filter removes sentences with matches");
     // tokenizer options setup
     opts.addOption("G", "genia", true,
         "use GENIA (with the dir containing 'morphdic/') instead of OpenNLP");
     // semantic patterns - REQUIRED!
     opts.addOption("p", "patterns", true, "match sentences with semantic patterns");
     // regulator and target ID, name pairs
     opts.addOption("regulatorsql", true, "SQL query that produces regulator ID, name pairs");
     opts.addOption("targetsql", true, "SQL query that produces target ID, name pairs");
     // gazetteer matching options
     opts.addOption("unbound", false, "disable matching names at token boundaries only");
     opts.addOption("noexpand", false, "do not process list expansions ('gene A-Z')");
     opts.addOption(
         "nogreekmap", false, "do not map Greek letter names ('alpha' -> '\u03b1')"
     );
     opts.addOption(
         "varsep", false, "allow variable token-separators ('', '-', and ' ')"
     );
     opts.addOption("idmatch", false, "match the DB IDs of the genes themselves");
     // gene annotator options
     opts.addOption("matches", true, "a blacklist (file) of exact matches");
     opts.addOption(
         "whitelistmatches", false, "invert filter matches to behave as a whitelist"
     );
     // filter options
     opts.addOption("postags", true, "a whitelist (file) of required PoS tags");
     opts.addOption("tokenfilter", true, "a two-column (file) list of filter matches");
     opts.addOption("whitelisttokens", false, "invert token filter to behave as a whitelist");
     // species mapping option
     opts.addOption(
         "linnaeus", true,
         "set a Linnaeus property file path to use Linnaeus for species normalization"
     );
     opts.addOption(
         "speciesmap", true,
         "a map of taxonomic IDs to another, applied to both gene and species anntoations"
     );
     // gene ranking options (all or none required)
     opts.addOption("rankermodel", true, "file containing the gene ranker's RankLib model");
     opts.addOption("generefs", true, "file containing the number of references per gene (ID)");
     opts.addOption("genesymbols", true, "file containing the symbol count per gene (ID)");
     opts.addOption("symbols", true, "file containing a count per (gene) symbol");
 
     try {
       cmd = parser.parse(opts, arguments);
     } catch (final ParseException e) {
       System.err.println(e.getLocalizedMessage());
       System.exit(1); // == EXIT ==
     }
     final Logger l = Pipeline.loggingSetup(cmd, opts,
         "txtfnnl ginx [options] -p <patterns> <directory|files...>\n");
     // sentence filter
     LineBasedStringArrayResource.Builder sentenceFilterResource = null;
     if (cmd.hasOption("sentences")) {
       sentenceFilterResource = LineBasedStringArrayResource.configure("file:" +
           new File(cmd.getOptionValue("sentences")).getAbsolutePath());
     }
     // (GENIA) tokenizer
     final String geniaDir = cmd.getOptionValue('G');
     // semantic patterns
     File patterns = null;
     try {
       patterns = new File(cmd.getOptionValue('p'));
     } catch (NullPointerException e) {
       l.severe("no patterns file");
       System.err.println("patterns file missing");
       System.exit(1); // == EXIT ==
     }
     // entity queries
     String regulatorSQL = cmd.hasOption("regulatorsql") ? cmd.getOptionValue("regulatorsql") : REGULATOR_SQL_QUERY;
     try {
       if (!regulatorSQL.trim().startsWith("SELECT"))
        regulatorSQL = readFile(regulatorSQL, "UTF-8");
     } catch (final IOException e) {
       System.err.println("regulator SQL parameter neither a SELECT statement or a file:");
       System.err.println(regulatorSQL);
     }
     String targetSQL = cmd.hasOption("targetsql") ? cmd.getOptionValue("targetsql") : TARGET_SQL_QUERY;
     try {
       if (!targetSQL.trim().startsWith("SELECT"))
        targetSQL = readFile(targetSQL, "UTF-8");
     } catch (final IOException e) {
       System.err.println("target SQL parameter neither a SELECT statement or a file:");
       System.err.println(targetSQL);
     }
     // DB resource
     final String dbUrl = Pipeline.getJdbcUrl(cmd, l, DEFAULT_DB_PROVIDER, DEFAULT_DATABASE);
     String dbDriverClassName = null;
     try {
       dbDriverClassName = Pipeline.getJdbcDriver(cmd, DEFAULT_JDBC_DRIVER);
     } catch (final ClassNotFoundException e) {
       System.err.println("JDBC driver class unknown:");
       System.err.println(e.toString());
       System.exit(1); // == EXIT ==
     }
     GnamedGazetteerResource.Builder regulatorGazetteer = GnamedGazetteerResource.configure(dbUrl, dbDriverClassName, regulatorSQL);
     if (!cmd.hasOption("unbound")) regulatorGazetteer.boundaryMatch();
     if (!cmd.hasOption("noexpand")) regulatorGazetteer.disableExpansions();
     if (!cmd.hasOption("nogreekmap")) regulatorGazetteer.disableGreekMapping();
     if (cmd.hasOption("varsep")) regulatorGazetteer.generateVariants();
     if (cmd.hasOption("idmatch")) regulatorGazetteer.idMatching();
     Pipeline.configureAuthentication(cmd, regulatorGazetteer);
     GnamedGazetteerResource.Builder targetGazetteer = GnamedGazetteerResource.configure(dbUrl, dbDriverClassName, targetSQL);
     if (!cmd.hasOption("unbound")) targetGazetteer.boundaryMatch();
     if (!cmd.hasOption("noexpand")) targetGazetteer.disableExpansions();
     if (!cmd.hasOption("nogreekmap")) targetGazetteer.disableGreekMapping();
     if (cmd.hasOption("varsep")) targetGazetteer.generateVariants();
     if (cmd.hasOption("idmatch")) targetGazetteer.idMatching();
     Pipeline.configureAuthentication(cmd, targetGazetteer);
     //double cutoff = cmd.hasOption("cutoff") ? Double.parseDouble(cmd.getOptionValue("cutoff")) : 0.0;
     String[] blacklist = cmd.hasOption("matches") ? makeList(cmd.getOptionValue("matches"), l) : null;
     // Taxon ID mapping resource
     ExternalResourceDescription taxIdMap = null;
     if (cmd.hasOption("speciesmap")) {
       try {
         taxIdMap = QualifiedStringResource.configure("file:" + cmd.getOptionValue("speciesmap")).create();
       } catch (ResourceInitializationException e) {
         l.severe(e.toString());
         System.err.println(e.getLocalizedMessage());
         e.printStackTrace();
         System.exit(1); // == EXIT ==
       }
     }
     // Regulator Gene Annotator setup
     GeneAnnotator.Builder regulatorAnnotator = null;
     try {
       regulatorAnnotator = GeneAnnotator.configure(regulatorNamespace, regulatorGazetteer.create());
     } catch (ResourceInitializationException e) {
       l.severe(e.toString());
       System.err.println(e.getLocalizedMessage());
       e.printStackTrace();
       System.exit(1); // == EXIT ==
     }
     regulatorAnnotator.setTextNamespace(SentenceAnnotator.NAMESPACE).setTextIdentifier(
         SentenceAnnotator.IDENTIFIER
     );//.setMinimumSimilarity(cutoff);
     regulatorAnnotator.setTaxIdMappingResource(taxIdMap);
     if (blacklist != null) {
       if (cmd.hasOption("whitelistmatches")) regulatorAnnotator.setWhitelist(blacklist);
       else regulatorAnnotator.setBlacklist(blacklist);
     }
     // Target Gene Annotator setup
     GeneAnnotator.Builder targetAnnotator = null;
     try {
       targetAnnotator = GeneAnnotator.configure(targetNamespace, targetGazetteer.create());
     } catch (ResourceInitializationException e) {
       l.severe(e.toString());
       System.err.println(e.getLocalizedMessage());
       e.printStackTrace();
       System.exit(1); // == EXIT ==
     }
     targetAnnotator.setTextNamespace(SentenceAnnotator.NAMESPACE).setTextIdentifier(
         SentenceAnnotator.IDENTIFIER
     );//.setMinimumSimilarity(cutoff);
     targetAnnotator.setTaxIdMappingResource(taxIdMap);
     if (blacklist != null) {
       if (cmd.hasOption("whitelistmatches")) targetAnnotator.setWhitelist(blacklist);
       else targetAnnotator.setBlacklist(blacklist);
     }
     // Linnaeus setup
     ConfigurationBuilder<AnalysisEngineDescription> linnaeus;
     if (cmd.hasOption("linnaeus")) {
       linnaeus = LinnaeusAnnotator.configure(new File(cmd.getOptionValue("linnaeus")))
                                   .setIdMappingResource(taxIdMap);
       regulatorAnnotator.setTaxaAnnotatorUri(LinnaeusAnnotator.URI);
       regulatorAnnotator.setTaxaNamespace(LinnaeusAnnotator.DEFAULT_NAMESPACE);
       targetAnnotator.setTaxaAnnotatorUri(LinnaeusAnnotator.URI);
       targetAnnotator.setTaxaNamespace(LinnaeusAnnotator.DEFAULT_NAMESPACE);
     } else {
       linnaeus = NOOPAnnotator.configure();
     }
     // Token Surrounding Filter setup
     ConfigurationBuilder<AnalysisEngineDescription> filterSurrounding;
     if (cmd.hasOption("postags") || cmd.hasOption("tokenfilter")) {
       TokenBasedSemanticAnnotationFilter.Builder tokenFilter = TokenBasedSemanticAnnotationFilter
           .configure();
       if (cmd.hasOption("postags")) tokenFilter.setPosTags(makeList(cmd.getOptionValue("postags"), l));
       if (cmd.hasOption("tokenfilter")) {
         if (cmd.hasOption("whitelisttokens")) tokenFilter.whitelist();
         try {
           tokenFilter.setSurroundingTokens(
               QualifiedStringSetResource.configure(
                   "file:" + cmd.getOptionValue("tokenfilter")
               ).create()
           );
         } catch (ResourceInitializationException e) {
           l.severe(e.toString());
           System.err.println(e.getLocalizedMessage());
           e.printStackTrace();
           System.exit(1); // == EXIT ==
         }
       }
       tokenFilter.setAnnotatorUri(GeneAnnotator.URI);
       //tokenFilter.setNamespace(targetNamespace);
       filterSurrounding = tokenFilter;
     } else {
       // no filter parameters have been specified - nothing to do
       filterSurrounding = NOOPAnnotator.configure();
     }
     // Gene ID mapping setup
     GnamedRefAnnotator.Builder entityMapper = null;
     try {
       entityMapper = GnamedRefAnnotator.configure(
           JdbcConnectionResourceImpl.configure(dbUrl, dbDriverClassName).create()
       );
     } catch (ResourceInitializationException e) {
       l.severe(e.toString());
       System.err.println(e.getLocalizedMessage());
       e.printStackTrace();
       System.exit(1); // == EXIT ==
     }
     entityMapper.setAnnotatorUri(GeneAnnotator.URI);
     // Gene Ranking setup
     ConfigurationBuilder<AnalysisEngineDescription> ranker = null;
     if (cmd.hasOption("rankermodel")) {
       try {
         GeneRankAnnotator.Builder geneRanker = GeneRankAnnotator.configure(
             RankLibRanker.configure("file:" + cmd.getOptionValue("rankermodel")).create()
         );
         geneRanker.setGeneLinkCounts(
             CounterResource.configure("file:" + cmd.getOptionValue("generefs")).create()
         );
         geneRanker.setGeneSymbolCounts(
             StringCounterResource.configure("file:" + cmd.getOptionValue("genesymbols")).create()
         );
         geneRanker.setSymbolCounts(
             CounterResource.configure("file:" + cmd.getOptionValue("symbols")).create()
         );
         //geneRanker.setNamespace(targetNamespace);
         geneRanker.setAnnotatorUri(GeneAnnotator.URI);
         geneRanker.setTaxaAnnotatorUri(LinnaeusAnnotator.URI);
         geneRanker.setGeneAnnotatorUri(GeniaTaggerAnnotator.URI);
         ranker = geneRanker;
       } catch (ResourceInitializationException e) {
         l.severe(e.toString());
         System.err.println(e.getLocalizedMessage());
         e.printStackTrace();
         System.exit(1); // == EXIT ==
       }
     } else {
       ranker = NOOPAnnotator.configure();
     }
     // output (format)
     OutputWriter.Builder writer;
     if (Pipeline.rawXmi(cmd)) {
       writer = Pipeline.configureWriter(cmd,
           XmiWriter.configure(Pipeline.ensureOutputDirectory(cmd)));
     } else {
       writer = Pipeline.configureWriter(cmd, RelationshipWriter.configure());
     }
     try {
       ExternalResourceDescription patternResource = LineBasedStringArrayResource.configure(
           "file:" + patterns.getCanonicalPath()).create();
       // 0:tika, 1:splitter, 2:filter, 3:tokenizer, 4:lemmatizer, 5:patternMatcher,
       // 6:regulator gazetteer, 7:target gazetteer, 8:filter regulator 9: filter target
       final Pipeline rex = new Pipeline(14);
       rex.setReader(cmd);
       rex.configureTika(cmd);
       rex.set(1, Pipeline.textEngine(Pipeline.getSentenceAnnotator(cmd)));
       if (sentenceFilterResource != null) {
         SentenceFilter.Builder sentenceFilter = SentenceFilter.configure(sentenceFilterResource.create());
         if (cmd.hasOption('F')) sentenceFilter.removeMatches();
         rex.set(2, Pipeline.textEngine(sentenceFilter.create()));
       } else {
         rex.set(2, Pipeline.textEngine(NOOPAnnotator.configure().create()));
       }
       if (geniaDir == null) {
         rex.set(3, Pipeline.textEngine(TokenAnnotator.configure().create()));
         rex.set(4, Pipeline.textEngine(BioLemmatizerAnnotator.configure().create()));
       } else {
         rex.set(
             3,
             Pipeline.textEngine(GeniaTaggerAnnotator.configure().setDirectory(new File(geniaDir))
                 .create()));
         // the GENIA Tagger already lemmatizes; nothing to do
         rex.set(4, Pipeline.multiviewEngine(NOOPAnnotator.configure().create()));
       }
       SyntaxPatternAnnotator.Builder spab = SyntaxPatternAnnotator.configure(patternResource);
       rex.set(5, Pipeline.textEngine(spab.removeUnmatched().create()));
       rex.set(6, Pipeline.textEngine(linnaeus.create()));
       rex.set(7, Pipeline.textEngine(regulatorAnnotator.create()));
       rex.set(8, Pipeline.textEngine(targetAnnotator.create()));
       rex.set(9, Pipeline.textEngine(filterSurrounding.create()));
       rex.set(10, Pipeline.textEngine(entityMapper.create()));
       rex.set(11, Pipeline.textEngine(ranker.create()));
 
       rex.set(
           12,
           Pipeline.textEngine(RelationshipFilter.configure()
               .setRelationshipAnnotatorUri(SyntaxPatternAnnotator.URI)
               .setRelationshipNamespace("event").setRelationshipIdentifier("tre")
               .setMappingAnnotatorUri(SyntaxPatternAnnotator.URI).setMappingNamespace("actor")
               .setMappingIdentifier(regulatorNamespace).setEntityAnnotatorUri(GeneAnnotator.URI)
               .setEntityNamespace(regulatorNamespace).create()));
       rex.set(
           13,
           Pipeline.textEngine(RelationshipFilter.configure()
               .setRelationshipAnnotatorUri(SyntaxPatternAnnotator.URI)
               .setRelationshipNamespace("event").setRelationshipIdentifier("tre")
               .setMappingAnnotatorUri(SyntaxPatternAnnotator.URI).setMappingNamespace("actor")
               .setMappingIdentifier(targetNamespace).setEntityAnnotatorUri(GeneAnnotator.URI)
               .setEntityNamespace(targetNamespace).create()));
       rex.setConsumer(Pipeline.textEngine(writer.create()));
       rex.run();
       rex.destroy();
     } catch (final UIMAException e) {
       l.severe(e.toString());
       System.err.println(e.getLocalizedMessage());
       System.exit(1); // == EXIT ==
     } catch (final IOException e) {
       l.severe(e.toString());
       System.err.println(e.getLocalizedMessage());
       System.exit(1); // == EXIT ==
     }
     System.exit(0);
   }
 
   private static
   String[] makeList(String filename, final Logger l) {
     String[] theList;
     BufferedReader reader;
     LinkedList<String> list = new LinkedList<String>();
     String line;
     int idx = 0;
     try {
       reader = new BufferedReader(new FileReader(new File(filename)));
       while ((line = reader.readLine()) != null) list.add(line);
     } catch (FileNotFoundException e) {
       l.severe(e.toString());
       System.err.println(e.getLocalizedMessage());
       System.exit(1); // == EXIT ==
     } catch (IOException e) {
       l.severe(e.toString());
       System.err.println(e.getLocalizedMessage());
       System.exit(1); // == EXIT ==
     }
     theList = new String[list.size()];
     for (String name : list)
       theList[idx++] = name;
     return theList;
   }
 
  private static String readFile(String path, String encoding) throws IOException {
     return IOUtils.toString(new FileInputStream(new File(path)), encoding);
   }
 }
