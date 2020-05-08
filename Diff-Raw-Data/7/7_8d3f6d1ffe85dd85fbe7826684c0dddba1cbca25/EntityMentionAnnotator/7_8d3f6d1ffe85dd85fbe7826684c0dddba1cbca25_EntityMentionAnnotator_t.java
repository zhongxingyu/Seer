 package txtfnnl.pipelines;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.apache.uima.UIMAException;
 import org.apache.uima.resource.ExternalResourceDescription;
 
 import org.uimafit.factory.AnalysisEngineFactory;
 
 import txtfnnl.uima.Views;
 import txtfnnl.uima.analysis_component.KnownEntityAnnotator;
 import txtfnnl.uima.collection.XmiWriter;
 import txtfnnl.utils.IOUtils;
 
 /**
  * Annotate known entities, generating the annotations in <a href
  * ="http://uima.apache.org/d/uimaj-2.4.0/references.html#ugr.ref.xmi">UIMA XMI format</a>.
  * <p>
  * The entities are "known", because for each file, the IDs of the contained entities must be
  * provided. These IDs are used to fetch their actual names from a DB resource, and finally, a
  * regular expression is used to detect the presence of those names in the text.
  * 
  * @author Florian Leitner
  */
 public class EntityMentionAnnotator {
   static final String DEFAULT_NAMESPACE = "entity:";
   static final String DEFAULT_DATABASE = "gnamed";
   static final String DEFAULT_MAPPING_FILE = "doc2entity.map";
   static final String DEFAULT_JDBC_DRIVER = "org.postgresql.Driver";
   static final String DEFAULT_DB_PROVIDER = "postgresql";
   static final String[] DEFAULT_SQL_QUERIES = new String[] {
       "SELECT DISTINCT p.value FROM gene_refs AS g "
           + "JOIN genes2proteins AS g2p ON g.id = g2p.gene_id "
           + "JOIN protein_strings AS p ON g2p.protein_id = p.id "
           + "WHERE p.cat IN ('name', 'symbol') " + "AND g.namespace=? AND g.accession=?",
       "SELECT s.value FROM gene_refs AS r " + "JOIN gene_strings AS s ON r.id = s.id "
           + "WHERE s.cat IN ('name', 'symbol') " + "AND r.namespace=? AND r.accession=?" };
 
   private EntityMentionAnnotator() {
     throw new AssertionError("n/a");
   }
 
   public static void main(String[] arguments) {
     final CommandLineParser parser = new PosixParser();
     final Options opts = new Options();
     CommandLine cmd = null;
     Pipeline.addLogHelpAndInputOptions(opts);
     Pipeline.addTikaOptions(opts);
     Pipeline.addOutputOptions(opts);
     Pipeline.addJdbcResourceOptions(opts, DEFAULT_JDBC_DRIVER, DEFAULT_DB_PROVIDER,
         DEFAULT_DATABASE);
     // entity annotator options setup
     opts.addOption("Q", "query-file", true, "file with SQL SELECT queries");
     OptionBuilder.withLongOpt("query");
     OptionBuilder.withArgName("SELECT");
     OptionBuilder.hasArgs();
     OptionBuilder.withDescription("one or more SQL SELECT queries");
     opts.addOption(OptionBuilder.create('q'));
     opts.addOption("m", "entity-map", true, "name of the entity map file [" +
         DEFAULT_MAPPING_FILE + "]");
     opts.addOption("n", "namespace", true, "namespace of the entity annotations [" +
         DEFAULT_NAMESPACE + "]");
     try {
       cmd = parser.parse(opts, arguments);
     } catch (final ParseException e) {
       System.err.println(e.getLocalizedMessage());
       System.exit(1); // == exit ==
     }
     final Logger l = Pipeline.loggingSetup(cmd, opts,
         "txtfnnl entities [options] <directory|files...>\n");
     // output options
     final String encoding = Pipeline.outputEncoding(cmd);
     final boolean overwriteFiles = Pipeline.outputOverwriteFiles(cmd);
     File outputDirectory = Pipeline.outputDirectory(cmd);
     if (outputDirectory == null) {
       outputDirectory = new File(System.getProperty("user.dir"));
     }
     // DB resource
     ExternalResourceDescription jdbcResource = null;
     try {
       jdbcResource = Pipeline.getJdbcResource(cmd, l, DEFAULT_JDBC_DRIVER, DEFAULT_DB_PROVIDER,
           DEFAULT_DATABASE);
     } catch (final IOException e) {
       System.err.println("JDBC resoruce setup failed:");
       System.err.println(e.toString());
       System.exit(1); // == EXIT ==
     } catch (final ClassNotFoundException e) {
       System.err.println("JDBC resoruce setup failed:");
       System.err.println(e.toString());
       System.exit(1); // == EXIT ==
     }
     /* BEGIN entity annotator */
     final String queryFileName = cmd.getOptionValue('Q');
     final String entityMapPath = cmd.getOptionValue('m', DEFAULT_MAPPING_FILE);
     final String namespace = cmd.getOptionValue('n', DEFAULT_NAMESPACE);
     String[] queries = cmd.getOptionValues('q');
     File entityMap; // m
     if (queryFileName != null) {
       final File queryFile = new File(queryFileName);
       if (!queryFile.isFile() || !queryFile.canRead()) {
         System.err.print("cannot read query file ");
         System.err.println(queryFile);
         System.exit(1); // == EXIT ==
       }
       String[] fileQueries = null;
       try {
         fileQueries = IOUtils.read(new FileInputStream(queryFile), encoding).split("\n");
       } catch (final Exception e) {
         System.err.print("cannot read query file ");
         System.err.print(queryFile);
         System.err.print(":");
         System.err.println(e.getLocalizedMessage());
         System.exit(1); // == EXIT ==
       }
       if (queries == null || queries.length == 0) {
         queries = fileQueries;
       } else {
         final String[] tmp = new String[queries.length + fileQueries.length];
         System.arraycopy(queries, 0, tmp, 0, queries.length);
         System.arraycopy(fileQueries, 0, tmp, queries.length, fileQueries.length);
         queries = tmp;
       }
     }
     entityMap = new File(entityMapPath);
     if (!entityMap.isFile() || !entityMap.canRead()) {
       System.err.print("cannot read entity map file ");
       System.err.println(entityMapPath);
       System.exit(1); // == EXIT ==
     }
     if (queries == null || queries.length == 0) {
       queries = DEFAULT_SQL_QUERIES;
     }
     /* END entity annotator */
     try {
       final Pipeline pipeline = new Pipeline(2); // tika and entity detector
       pipeline.setReader(cmd);
       pipeline.configureTika(cmd);
      pipeline.set(1, KnownEntityAnnotator.configure(namespace, queries, entityMap, jdbcResource));
      // pipeline.set(1, AnalysisEngineFactory.createAnalysisEngine(
      //     KnownEntityAnnotator.configure(namespace, queries, entityMap, jdbcResource),
      //     Views.CONTENT_TEXT.toString()));
       pipeline.setConsumer(XmiWriter.configure(outputDirectory, encoding, overwriteFiles, false,
           true));
       pipeline.run();
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
 }
