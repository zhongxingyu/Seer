 package txtfnnl.pipelines;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.apache.uima.UIMAException;
 
 import txtfnnl.uima.analysis_component.BioLemmatizerAnnotator;
 import txtfnnl.uima.analysis_component.GeniaTaggerAnnotator;
 import txtfnnl.uima.analysis_component.NOOPAnnotator;
 import txtfnnl.uima.analysis_component.SentenceFilterAnnotator;
 import txtfnnl.uima.analysis_component.SyntaxPatternAnnotator;
 import txtfnnl.uima.analysis_component.opennlp.SentenceAnnotator;
 import txtfnnl.uima.analysis_component.opennlp.TokenAnnotator;
 import txtfnnl.uima.collection.SemanticAnnotationWriter;
 import txtfnnl.uima.collection.SentenceLineWriter;
 
 /**
  * A plaintext extractor for (nearly arbitrary) input files to extract sentences or phrases based
  * on pattern matching of its tokens and their lemmas, PoS and chunk tags.
  * <p>
  * Input files can be read from a directory or listed explicitly, while output files are written to
  * some directory or to STDOUT. Sentences are written on a single line. Matching patterns are
  * written on a single line, too, as tab-separated values for the annotated namespace, identifier,
  * and offset, followed by the pattern itself (which may contain tabs, but no newlines).
  * 
  * @author Florian Leitner
  */
 public class PatternExtractor extends Pipeline {
   private PatternExtractor() {
     throw new AssertionError("n/a");
   }
 
   public static void main(String[] arguments) {
     final CommandLineParser parser = new PosixParser();
     final Options opts = new Options();
     CommandLine cmd = null;
     Pipeline.addLogHelpAndInputOptions(opts);
     Pipeline.addTikaOptions(opts);
     Pipeline.addOutputOptions(opts);
     // sentence splitter options
     opts.addOption("S", "successive-newlines", false, "split sentences on successive newlines");
     opts.addOption("s", "single-newlines", false, "split sentences on single newlines");
     // sentence filter options
     opts.addOption("f", "filter-sentences", true, "retain sentences using a file of regex matches");
     opts.addOption("F", "filter-remove", false, "filter removes sentences with matches");
     // tokenizer options setup
     opts.addOption("g", "genia", true,
         "use GENIA (with the dir containing 'morphdic/') instead of OpenNLP");
     // semantic pattern tagger options
     opts.addOption("p", "patterns", true, "match sentences with semantic patterns");
    // ouptput options
     opts.addOption("c", "complete-sentences", false,
         "print complete sentences, not just matching phrases");
     try {
       cmd = parser.parse(opts, arguments);
     } catch (final ParseException e) {
       System.err.println(e.getLocalizedMessage());
       System.exit(1); // == EXIT ==
     }
     final Logger l = Pipeline.loggingSetup(cmd, opts,
         "txtfnnl grep [options] -p <patterns> <directory|files...>\n");
     // sentence splitter
     String splitSentences = null; // S, s
     if (cmd.hasOption('s')) {
       splitSentences = "single";
     } else if (cmd.hasOption('S')) {
       splitSentences = "successive";
     }
     // sentence filter
     final File sentenceFilterPatterns = cmd.hasOption('f') ? new File(cmd.getOptionValue('f'))
         : null;
     final boolean removingSentenceFilter = cmd.hasOption('F');
     // (GENIA) tokenizer
     final String geniaDir = cmd.getOptionValue('g');
     // semantic patterns
     File patterns = null;
     try {
       patterns = new File(cmd.getOptionValue('p'));
     } catch (NullPointerException e) {
       l.severe("no patterns file");
       System.err.println("patterns file missing");
       System.exit(1); // == EXIT ==
     }
     // output (format)
     final boolean completeSentence = cmd.hasOption('c');
     final String encoding = Pipeline.outputEncoding(cmd);
     final File outputDirectory = Pipeline.outputDirectory(cmd);
     final boolean overwriteFiles = Pipeline.outputOverwriteFiles(cmd);
     try {
       // 0:tika, 1:splitter, 2:filter, 3:tokenizer, 4:lemmatizer, 5:patternMatcher
       final Pipeline grep = new Pipeline(6);
       grep.setReader(cmd);
       grep.configureTika(cmd);
       grep.set(1, SentenceAnnotator.configure(splitSentences));
       if (sentenceFilterPatterns == null) grep.set(2, NOOPAnnotator.configure());
       else grep.set(2,
           SentenceFilterAnnotator.configure(sentenceFilterPatterns, removingSentenceFilter));
       if (geniaDir == null) {
         grep.set(3, TokenAnnotator.configure());
         grep.set(4, BioLemmatizerAnnotator.configure());
       } else {
         grep.set(3, GeniaTaggerAnnotator.configure(new File(geniaDir)));
         // the GENIA Tagger already lemmatizes; nothing to do
         grep.set(4, NOOPAnnotator.configure());
       }
       grep.set(5, SyntaxPatternAnnotator.configure(patterns, "\t", completeSentence, null, null));
       if (completeSentence) grep.setConsumer(SentenceLineWriter.configure(outputDirectory,
           encoding, outputDirectory == null, overwriteFiles, true, false));
       else grep.setConsumer(SemanticAnnotationWriter.configure(outputDirectory, encoding,
           outputDirectory == null, overwriteFiles, true, "\t"));
       grep.run();
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
