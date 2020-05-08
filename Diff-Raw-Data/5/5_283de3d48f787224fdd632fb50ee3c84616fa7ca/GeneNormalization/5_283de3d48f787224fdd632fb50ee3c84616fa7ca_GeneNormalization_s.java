 package txtfnnl.pipelines;
 
 import org.apache.commons.cli.*;
 import org.apache.uima.UIMAException;
 import org.apache.uima.analysis_engine.AnalysisEngineDescription;
 import org.apache.uima.resource.ExternalResourceDescription;
 import org.apache.uima.resource.ResourceInitializationException;
 import txtfnnl.uima.ConfigurationBuilder;
 import txtfnnl.uima.analysis_component.*;
 import txtfnnl.uima.analysis_component.opennlp.SentenceAnnotator;
 import txtfnnl.uima.analysis_component.opennlp.TokenAnnotator;
 import txtfnnl.uima.collection.AnnotationLineWriter;
 import txtfnnl.uima.collection.OutputWriter;
 import txtfnnl.uima.collection.XmiWriter;
 import txtfnnl.uima.resource.*;
 
 import java.io.*;
 import java.util.LinkedList;
 import java.util.logging.Logger;
 
 /**
  * A pipeline to detect gene and protein names that match names recorded in databases.
  * <p/>
  * Input files can be read from a directory or listed explicitly, while output lines are written to
  * some directory or to STDOUT. Output is written as tab-separated values, where each line contains
  * the matched text, the gene ID, the taxon ID, and a confidence value.
  * <p/>
  * The default setup assumes gene and/or protein entities found in a <a
  * href="https://github.com/fnl/gnamed">gnamed</a> database.
  *
  * @author Florian Leitner
  */
 public
 class GeneNormalization extends Pipeline {
   static final String DEFAULT_DATABASE = "gnamed";
   static final String DEFAULT_JDBC_DRIVER = "org.postgresql.Driver";
   static final String DEFAULT_DB_PROVIDER = "postgresql";
   // default: all known gene and protein symbols
   static final String SQL_QUERY =
       "SELECT g.id, g.species_id, ps.value FROM genes AS g INNER JOIN genes2proteins AS g2p ON (g.id = g2p.gene_id) INNER JOIN protein_strings AS ps ON (g2p.protein_id = ps.id) WHERE ps.cat = 'symbol' UNION SELECT g.id, g.species_id, gs.value FROM genes AS g INNER JOIN gene_strings AS gs USING (id) WHERE gs.cat = 'symbol' ";
 
   private
   GeneNormalization() {
     throw new AssertionError("n/a");
   }
 
   public static
   void main(String[] arguments) {
     final CommandLineParser parser = new PosixParser();
     final Options opts = new Options();
     final String geneAnnotationNamespace = "gene";
     CommandLine cmd = null;
     // standard pipeline options
     Pipeline.addLogHelpAndInputOptions(opts);
     Pipeline.addTikaOptions(opts);
     Pipeline.addJdbcResourceOptions(
         opts, DEFAULT_JDBC_DRIVER, DEFAULT_DB_PROVIDER, DEFAULT_DATABASE
     );
     Pipeline.addOutputOptions(opts);
     Pipeline.addSentenceAnnotatorOptions(opts);
     // tokenizer options setup
     opts.addOption(
         "G", "genia", true, "use GENIA (with the dir containing 'morphdic/') instead of OpenNLP"
     );
     // query options
     opts.addOption("Q", "query", true, "SQL query that produces gene ID, tax ID, name triplets");
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
     opts.addOption("f", "filter-matches", true, "a blacklist (file) of exact matches");
     opts.addOption(
         "F", "whitelist-matches", false, "invert filter matches to behave as a whitelist"
     );
     opts.addOption(
         "c", "cutoff-similarity", true, "min. string similarity required to annotate [0.0]"
     );
     // filter options
     opts.addOption("p", "pos-tags", true, "a whitelist (file) of required PoS tags");
     opts.addOption("t", "filter-tokens", true, "a two-column (file) list of filter matches");
     opts.addOption("T", "whitelist-tokens", false, "invert token filter to behave as a whitelist");
     // species mapping option
     opts.addOption(
         "l", "linnaeus", true,
         "set a Linnaeus property file path to use Linnaeus for species normalization"
     );
     opts.addOption(
         "L", "species-map", true,
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
     final Logger l = Pipeline.loggingSetup(
         cmd, opts, "txtfnnl norm [options] <directory|files...>\n"
     );
     // (GENIA) tokenizer
     final File geniaDir = cmd.getOptionValue('G') == null ? null : new File(
         cmd.getOptionValue('G')
     );
     // DB query used to fetch the gazetteer's entities
     final String querySql = cmd.hasOption('Q') ? cmd.getOptionValue('Q') : SQL_QUERY;
     // DB gazetteer resource setup
     final String dbUrl = Pipeline.getJdbcUrl(cmd, l, DEFAULT_DB_PROVIDER, DEFAULT_DATABASE);
     String dbDriverClassName = null;
     try {
       dbDriverClassName = Pipeline.getJdbcDriver(cmd, DEFAULT_JDBC_DRIVER);
     } catch (final ClassNotFoundException e) {
       System.err.println("JDBC driver class unknown:");
       System.err.println(e.toString());
       System.exit(1); // == EXIT ==
     }
     GnamedGazetteerResource.Builder gazetteer = null;
     // create builder
     gazetteer = GnamedGazetteerResource.configure(dbUrl, dbDriverClassName, querySql);
     if (!cmd.hasOption("unbound")) gazetteer.boundaryMatch();
     if (!cmd.hasOption("noexpand")) gazetteer.disableExpansions();
     if (!cmd.hasOption("nogreekmap")) gazetteer.disableGreekMapping();
     if (cmd.hasOption("varsep")) gazetteer.generateVariants();
     if (cmd.hasOption("idmatch")) gazetteer.idMatching();
     Pipeline.configureAuthentication(cmd, gazetteer);
     // Taxon ID mapping resource
     ExternalResourceDescription taxIdMap = null;
     if (cmd.hasOption('L')) {
       try {
         taxIdMap = QualifiedStringResource.configure("file:" + cmd.getOptionValue('L')).create();
       } catch (ResourceInitializationException e) {
         l.severe(e.toString());
         System.err.println(e.getLocalizedMessage());
         e.printStackTrace();
         System.exit(1); // == EXIT ==
       }
     }
     // Gene Annotator setup
     GeneAnnotator.Builder geneAnnotator = null;
     try {
       geneAnnotator = GeneAnnotator.configure(geneAnnotationNamespace, gazetteer.create());
     } catch (ResourceInitializationException e) {
       l.severe(e.toString());
       System.err.println(e.getLocalizedMessage());
       e.printStackTrace();
       System.exit(1); // == EXIT ==
     }
     double cutoff = cmd.hasOption('c') ? Double.parseDouble(cmd.getOptionValue('c')) : 0.0;
     String[] blacklist = cmd.hasOption('f') ? makeList(cmd.getOptionValue('f'), l) : null;
     geneAnnotator.setTextNamespace(SentenceAnnotator.NAMESPACE).setTextIdentifier(
         SentenceAnnotator.IDENTIFIER
     ).setMinimumSimilarity(cutoff);
     geneAnnotator.setTaxIdMappingResource(taxIdMap);
     if (blacklist != null) {
       if (cmd.hasOption('F')) geneAnnotator.setWhitelist(blacklist);
       else geneAnnotator.setBlacklist(blacklist);
     }
     // Linnaeus setup
     ConfigurationBuilder<AnalysisEngineDescription> linnaeus;
     if (cmd.hasOption('l')) {
       linnaeus = LinnaeusAnnotator.configure(new File(cmd.getOptionValue('l')))
                                   .setIdMappingResource(taxIdMap);
       geneAnnotator.setTaxaAnnotatorUri(LinnaeusAnnotator.URI);
       geneAnnotator.setTaxaNamespace(LinnaeusAnnotator.DEFAULT_NAMESPACE);
     } else {
       linnaeus = NOOPAnnotator.configure();
     }
     // Token Surrounding Filter setup
     ConfigurationBuilder<AnalysisEngineDescription> filterSurrounding;
     if (cmd.hasOption('p') || cmd.hasOption('t')) {
       TokenBasedSemanticAnnotationFilter.Builder tokenFilter = TokenBasedSemanticAnnotationFilter
           .configure();
       if (cmd.hasOption('p')) tokenFilter.setPosTags(makeList(cmd.getOptionValue('p'), l));
       if (cmd.hasOption('t')) {
         if (cmd.hasOption('T')) tokenFilter.whitelist();
         try {
           tokenFilter.setSurroundingTokens(
               QualifiedStringSetResource.configure(
                   "file:" + cmd.getOptionValue('t')
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
       tokenFilter.setNamespace(geneAnnotationNamespace);
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
     // output
     OutputWriter.Builder writer;
     if (Pipeline.rawXmi(cmd)) {
       writer = Pipeline.configureWriter(
           cmd, XmiWriter.configure(Pipeline.ensureOutputDirectory(cmd))
       );
     } else {
       writer = Pipeline.configureWriter(cmd, AnnotationLineWriter.configure())
                        .setAnnotatorUri(GeneAnnotator.URI)
                        .setAnnotationNamespace(geneAnnotationNamespace).printSurroundings()
                        .printPosTag();
     }
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
         geneRanker.setNamespace(geneAnnotationNamespace);
         geneRanker.setAnnotatorUri(GeneAnnotator.URI);
         geneRanker.setTaxaAnnotatorUri(LinnaeusAnnotator.URI);
         ranker = geneRanker;
       } catch (ResourceInitializationException e) {
         l.severe(e.toString());
         System.err.println(e.getLocalizedMessage());
         e.printStackTrace();
         System.exit(1); // == EXIT ==
      } catch (IOException e) {
        l.severe(e.toString());
        System.err.println(e.getLocalizedMessage());
        e.printStackTrace();
        System.exit(1); // == EXIT ==
       }
     } else {
       ranker = NOOPAnnotator.configure();
     }
     try {
       // 0:tika, 1:splitter, 2:tokenizer, 3:linnaeus, 4:gazetteer, 5:filter, 6: mapIDs, 7: rank
       final Pipeline gn = new Pipeline(8);
       gn.setReader(cmd);
       gn.configureTika(cmd);
       gn.set(1, Pipeline.textEngine(Pipeline.getSentenceAnnotator(cmd)));
       if (geniaDir == null) {
         gn.set(2, Pipeline.textEngine(TokenAnnotator.configure().create()));
       } else {
         GeniaTaggerAnnotator.Builder tagger = GeniaTaggerAnnotator.configure();
         tagger.setDirectory(geniaDir);
         gn.set(2, Pipeline.textEngine(tagger.create()));
       }
       gn.set(3, Pipeline.textEngine(linnaeus.create()));
       gn.set(4, Pipeline.textEngine(geneAnnotator.create()));
       gn.set(5, Pipeline.textEngine(filterSurrounding.create()));
       gn.set(6, Pipeline.textEngine(entityMapper.create()));
       gn.set(7, Pipeline.textEngine(ranker.create()));
       gn.setConsumer(Pipeline.textEngine(writer.create()));
       gn.run();
       gn.destroy();
     } catch (final UIMAException e) {
       l.severe(e.toString());
       System.err.println(e.getLocalizedMessage());
       e.printStackTrace();
       System.exit(1); // == EXIT ==
     } catch (final IOException e) {
       l.severe(e.toString());
       System.err.println(e.getLocalizedMessage());
       e.printStackTrace();
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
 }
