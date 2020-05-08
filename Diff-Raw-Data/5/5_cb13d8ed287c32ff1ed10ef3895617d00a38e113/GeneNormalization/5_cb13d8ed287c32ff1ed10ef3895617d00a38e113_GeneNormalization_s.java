 package txtfnnl.pipelines;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.apache.uima.UIMAException;
 import org.apache.uima.resource.ExternalResourceDescription;
 import org.apache.uima.resource.ResourceInitializationException;
 
 import txtfnnl.uima.analysis_component.GeneAnnotator;
 import txtfnnl.uima.analysis_component.GeniaTaggerAnnotator;
 import txtfnnl.uima.analysis_component.NOOPAnnotator;
 import txtfnnl.uima.analysis_component.opennlp.SentenceAnnotator;
 import txtfnnl.uima.analysis_component.opennlp.TokenAnnotator;
 import txtfnnl.uima.collection.AnnotationLineWriter;
 import txtfnnl.uima.resource.GnamedGazetteerResource;
 
 /**
  * A pipeline to detect gene and protein names that match names recorded in databases.
  * <p>
  * Input files can be read from a directory or listed explicitly, while output lines are written to
  * some directory or to STDOUT. Output is written as tab-separated values, where each line contains
  * the matched text, the gene ID, the taxon ID, and a confidence value.
  * <p>
  * The default setup assumes gene and/or protein entities found in a <a
  * href="https://github.com/fnl/gnamed">gnamed</a> database.
  * 
  * @author Florian Leitner
  */
 public class GeneNormalization extends Pipeline {
   static final String DEFAULT_DATABASE = "gnamed";
   static final String DEFAULT_JDBC_DRIVER = "org.postgresql.Driver";
   static final String DEFAULT_DB_PROVIDER = "postgresql";
   // default: all known gene and protein symbols
   static final String SQL_QUERY = "SELECT gr.accession, g.species_id, ps.value "
       + "FROM gene_refs AS gr, genes AS g, genes2proteins AS g2p, protein_strings AS ps "
       + "WHERE gr.namespace = 'gi' AND gr.id = g.id AND g.id = g2p.gene_id AND g2p.protein_id = ps.id AND ps.cat = 'symbol' "
       + "UNION SELECT gr.accession, g.species_id, gs.value "
       + "FROM gene_refs AS gr, genes AS g, gene_strings AS gs "
       + "WHERE gr.namespace = 'gi' AND gr.id = g.id AND gr.id = gs.id AND gs.cat = 'symbol'";
 
   private GeneNormalization() {
     throw new AssertionError("n/a");
   }
 
   public static void main(String[] arguments) {
     final CommandLineParser parser = new PosixParser();
     final Options opts = new Options();
     CommandLine cmd = null;
     Pipeline.addLogHelpAndInputOptions(opts);
     Pipeline.addTikaOptions(opts);
     Pipeline.addJdbcResourceOptions(opts, DEFAULT_JDBC_DRIVER, DEFAULT_DB_PROVIDER,
         DEFAULT_DATABASE);
     Pipeline.addOutputOptions(opts);
     // sentence splitter options
     opts.addOption("S", "successive-newlines", false, "split sentences on successive newlines");
     opts.addOption("s", "single-newlines", false, "split sentences on single newlines");
     // tokenizer options setup
     opts.addOption("G", "genia", true,
         "use GENIA (with the dir containing 'morphdic/') instead of OpenNLP");
     // query options
    opts.addOption("q", "query", true, "SQL query that produces gene ID, tax ID, name triplets");
     try {
       cmd = parser.parse(opts, arguments);
     } catch (final ParseException e) {
       System.err.println(e.getLocalizedMessage());
       System.exit(1); // == EXIT ==
     }
     final Logger l = Pipeline.loggingSetup(cmd, opts,
         "txtfnnl gn [options] <directory|files...>\n");
     // sentence splitter
     String splitSentences = null; // S, s
     if (cmd.hasOption('s')) {
       splitSentences = "single";
     } else if (cmd.hasOption('S')) {
       splitSentences = "successive";
     }
     // (GENIA) tokenizer
     final String geniaDir = cmd.getOptionValue('G');
     // query
    final String querySql = cmd.hasOption('q') ? cmd.getOptionValue('q') : SQL_QUERY;
     // DB resource
     ExternalResourceDescription geneGazetteer = null;
     try {
       final String driverClass = cmd.getOptionValue('D', DEFAULT_JDBC_DRIVER);
       Class.forName(driverClass);
       // db url
       final String dbHost = cmd.getOptionValue('H', "localhost");
       final String dbProvider = cmd.getOptionValue('P', DEFAULT_DB_PROVIDER);
       final String dbName = cmd.getOptionValue('d', DEFAULT_DATABASE);
       final String dbUrl = String.format("jdbc:%s://%s/%s", dbProvider, dbHost, dbName);
       l.log(Level.INFO, "JDBC URL: {0}", dbUrl);
       // create builder
       GnamedGazetteerResource.Builder b = GnamedGazetteerResource.configure(dbUrl, driverClass,
           querySql);
       // set username/password options
       if (cmd.hasOption('u')) b.setUsername(cmd.getOptionValue('u'));
       if (cmd.hasOption('p')) b.setPassword(cmd.getOptionValue('p'));
       geneGazetteer = b.create();
     } catch (final ResourceInitializationException e) {
       System.err.println("JDBC resoruce setup failed:");
       System.err.println(e.toString());
       System.exit(1); // == EXIT ==
     } catch (final ClassNotFoundException e) {
       System.err.println("JDBC driver class unknown:");
       System.err.println(e.toString());
       System.exit(1); // == EXIT ==
     }
     // output
     final String geneAnnotationNamespace = "gene";
     AnnotationLineWriter.Builder alw = AnnotationLineWriter.configureTodo()
         .setAnnotatorUri(GeneAnnotator.URI).setAnnotationNamespace(geneAnnotationNamespace);
     alw.setEncoding(Pipeline.outputEncoding(cmd));
     alw.setOutputDirectory(Pipeline.outputDirectory(cmd));
     if (Pipeline.outputOverwriteFiles(cmd)) alw.overwriteFiles();
     try {
       // 0:tika, 1:splitter, 2:tokenizer, (3:NOOP), 4:gazetteer
       final Pipeline gn = new Pipeline(5);
       gn.setReader(cmd);
       gn.configureTika(cmd);
       gn.set(1, SentenceAnnotator.configure(splitSentences));
       if (geniaDir == null) {
         gn.set(2, TokenAnnotator.configure());
         // gn.set(4, BioLemmatizerAnnotator.configure());
         gn.set(3, NOOPAnnotator.configure());
       } else {
         gn.set(2, GeniaTaggerAnnotator.configure().setDirectory(new File(geniaDir)).create());
         // the GENIA Tagger already lemmatizes; nothing to do
         gn.set(3, NOOPAnnotator.configure());
       }
       gn.set(
           4,
           GeneAnnotator.configure(geneAnnotationNamespace, geneGazetteer)
               .setTextNamespace(SentenceAnnotator.NAMESPACE)
               .setTextIdentifier(SentenceAnnotator.IDENTIFIER).create());
       gn.setConsumer(alw.create());
       gn.run();
     } catch (final UIMAException e) {
       l.severe(e.toString());
       System.err.println(e.getLocalizedMessage());
       e.printStackTrace();
       System.exit(1); // == EXIT ==
     } catch (final IOException e) {
       l.severe(e.toString());
       System.err.println(e.getLocalizedMessage());
       System.exit(1); // == EXIT ==
     }
     System.exit(0);
   }
 }
