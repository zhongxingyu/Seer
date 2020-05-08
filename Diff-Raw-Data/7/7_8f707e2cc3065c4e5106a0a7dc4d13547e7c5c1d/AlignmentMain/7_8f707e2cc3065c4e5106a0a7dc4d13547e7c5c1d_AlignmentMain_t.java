 package de.bioinformatikmuenchen.pg4.alignment;
 
 import com.google.common.collect.Lists;
 import de.bioinformatikmuenchen.pg4.common.alignment.AlignmentResult;
 import de.bioinformatikmuenchen.pg4.common.distance.IDistanceMatrix;
 import de.bioinformatikmuenchen.pg4.common.distance.QUASARDistanceMatrixFactory;
 import de.bioinformatikmuenchen.pg4.common.sequencesource.ISequenceSource;
 import de.bioinformatikmuenchen.pg4.common.sequencesource.SequenceLibrarySequenceSource;
 import de.bioinformatikmuenchen.pg4.alignment.gap.AffineGapCost;
 import de.bioinformatikmuenchen.pg4.alignment.gap.ConstantGapCost;
 import de.bioinformatikmuenchen.pg4.alignment.gap.IGapCost;
 import de.bioinformatikmuenchen.pg4.alignment.io.AlignmentOutputFormatFactory;
 import de.bioinformatikmuenchen.pg4.alignment.io.DPMatrixExporter;
 import de.bioinformatikmuenchen.pg4.alignment.io.IAlignmentOutputFormatter;
 import de.bioinformatikmuenchen.pg4.alignment.pairfile.PairfileEntry;
 import de.bioinformatikmuenchen.pg4.alignment.pairfile.PairfileParser;
 import de.bioinformatikmuenchen.pg4.alignment.recursive.RecursiveNWAlignmentProcessor;
 import de.bioinformatikmuenchen.pg4.common.Sequence;
 import de.bioinformatikmuenchen.pg4.common.alignment.SequencePairAlignment;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 
 /**
  *
  */
 public class AlignmentMain {
 
     private AlignmentAlgorithm algorithm = null;
     private AlignmentMode mode = null;
     private AlignmentOutputFormat outputFormat = null;
 
     public AlignmentMain(String[] args) throws IOException {
         //Which opts are available
         final Options opts = new Options();
         opts.addOption("g", "go", true, "Gap open")
                 .addOption("h", "help", false, "Print help")
                 .addOption("e", "ge", true, "gapextend")
                 .addOption("d", "dpmatrices", true, "Output dynamic programming matrices to directory")
                 .addOption("p", "pairs", true, "Path to pairs file")
                 .addOption("s", "seqlib", true, "seqlibfile")
                 .addOption("m", "matrixname", true, "matrixname")
                 .addOption("r", "mode", true, "Set the mode (global|local|freeshift)")
                 .addOption("u", "nw", false, "Use Needleman-Wunsch, with gap-open being ignored")
                .addOption("a", "fixedpointalignment", true, "Output FPA to directory")
                 .addOption("t", "min-as-threshold", true, "In FPA, set the minimum of the matrix as heatmap minimum")
                 .addOption("b", "benchmark", false, "Benchmark the selected algorithm versus the recursive Needleman-Wunsch")
                 .addOption("v", "verbose", false, "Print verbose status reports (on stderr)")
                 .addOption("c", "check", false, "Calculate checkscores")
                 .addOption("f", "format", true, "format");
         //Parse the opts
         final CommandLineParser cmdLinePosixParser = new PosixParser();
         CommandLine commandLine = null;
         try {
             commandLine = cmdLinePosixParser.parse(opts, args);
         } catch (ParseException parseException) {    // checked exception  
             System.err.println(
                     "Encountered exception while parsing using PosixParser:\n"
                     + parseException.getMessage());
             System.exit(1);
         }
         //
         //Check if the options are valid
         //
         //Check help
         if (commandLine.hasOption("help")) {
             HelpFormatter formatter = new HelpFormatter();
 //            formatter.printHelp(new PrintWriter(System.err), 100, "gnu", "alignment.jar", opts, 4, 4, "Exiting...");
             formatter.printHelp("alignment.jar", opts);
             System.out.println("--help supplied, exiting...");
             System.exit(1);
         }
         //gapopen
         double gapOpen = Double.NaN;
         try {
             if (commandLine.hasOption("go")) {
                 gapOpen = Double.parseDouble(commandLine.getOptionValue("go"));
             } else { //Default
                 gapOpen = -12;
             }
         } catch (NumberFormatException ex) {
             System.err.println("--go takes a number, not " + commandLine.getOptionValue("go") + "!");
             System.exit(1);
         }
         //ge
         double gapExtend = Double.NaN;
         try {
             if (commandLine.hasOption("ge")) {
                 gapExtend = Double.parseDouble(commandLine.getOptionValue("ge"));
             } else { //Default
                 gapExtend = -1;
             }
         } catch (NumberFormatException ex) {
             System.err.println("--ge takes a number, not " + commandLine.getOptionValue("ge") + "!");
             System.exit(1);
         }
         //check --dpmatrices
         File dpMatrixDir = null;
         if (commandLine.hasOption("dpmatrices")) {
             dpMatrixDir = new File(commandLine.getOptionValue("dpmatrices"));
             if (!dpMatrixDir.exists()) {
                 dpMatrixDir.mkdirs();
             }
             if (dpMatrixDir.exists() && !dpMatrixDir.isDirectory()) {
                 System.err.println("Error: --dpmatrices argument exists and is not a directory");
                 System.exit(1);
             }
             if (!dpMatrixDir.exists()) {
                 dpMatrixDir.mkdirs();
             }
         }
         //check --fixed-point-alignment
         File fpaDir = null;
        if (commandLine.hasOption("fixedpointalignment")) {
            fpaDir = new File(commandLine.getOptionValue("fixedpointalignment"));
             if (fpaDir.exists() && !fpaDir.isDirectory()) {
                 System.err.println("Error: --fixed-point-alignment argument exists and is not a directory");
                 System.exit(1);
             }
             if (!fpaDir.exists()) {
                 fpaDir.mkdirs();
             }
         }
         //check --pairs
         File pairsFile = null;
         if (commandLine.hasOption("pairs")) {
             pairsFile = new File(commandLine.getOptionValue("pairs"));
             if (!pairsFile.exists() || pairsFile.isDirectory()) {
                 System.err.println("Error: --pairs argument is not a file or does not exist!");
                 System.exit(1);
             }
         } else {
             System.err.println("--pairs is mandatory");
             HelpFormatter formatter = new HelpFormatter();
             formatter.printHelp("alignment.jar", opts);
             System.exit(1);
         }
         //check --seqlib
         File seqLibFile = null;
         if (commandLine.hasOption("seqlib")) {
             seqLibFile = new File(commandLine.getOptionValue("seqlib"));
             if (!seqLibFile.exists() || !seqLibFile.isFile()) {
                 System.err.println("Error: --seqlib argument is not a file or does not exist!");
                 System.exit(1);
             }
         } else {
             System.err.println("--seqlib is mandatory");
             HelpFormatter formatter = new HelpFormatter();
             formatter.printHelp("alignment.jar", opts);
             System.exit(1);
         }
         //matrixname
         File substitutionMatrixFile = null;
         if (commandLine.hasOption("matrixname")) {
             substitutionMatrixFile = new File(commandLine.getOptionValue("matrixname"));
             if (!substitutionMatrixFile.exists()) {
                 System.err.println("Error: --matrixname argument '" + substitutionMatrixFile + "' does not exist!");
                 System.exit(1);
             } else if (!substitutionMatrixFile.isFile()) {
 
                 System.err.println("Error: --matrixname argument is not a file!");
                 System.exit(1);
             }
         } else {
             System.err.println("--matrixname is mandatory");
             HelpFormatter formatter = new HelpFormatter();
             formatter.printHelp("alignment.jar", opts);
             System.exit(1);
         }
         //mode
         String modeString = commandLine.getOptionValue("mode").toLowerCase();
         if (modeString != null) {
             if (modeString.equalsIgnoreCase("local")) {
                 mode = AlignmentMode.LOCAL;
             } else if (modeString.equalsIgnoreCase("global")) {
                 mode = AlignmentMode.GLOBAL;
             } else if (modeString.equalsIgnoreCase("freeshift")) {
                 mode = AlignmentMode.FREESHIFT;
             } else {
                 System.err.println("Error: --mode argument " + modeString + " is invalid!");
                 System.exit(1);
             }
         } else {
             System.err.println("--matrixname is mandatory");
             HelpFormatter formatter = new HelpFormatter();
             formatter.printHelp("alignment.jar", opts);
             System.exit(1);
         }
         //mode
         if (!commandLine.hasOption("format")) {
             System.err.println("No --format option given (it's mandatory)");
             System.exit(1);
         } else { //We have a format option
             String outputFormatString = commandLine.getOptionValue("format").toLowerCase();
             if (outputFormatString != null) {
                 if (outputFormatString.equalsIgnoreCase("ali")) {
                     outputFormat = AlignmentOutputFormat.ALI;
                 } else if (outputFormatString.equalsIgnoreCase("html")) {
                     outputFormat = AlignmentOutputFormat.HTML;
                 } else if (outputFormatString.equalsIgnoreCase("scores")) {
                     outputFormat = AlignmentOutputFormat.SCORES;
                 } else {
                     System.err.println("Error: --format argument " + outputFormatString + " is invalid!");
                     System.exit(1);
                 }
             } else {
                 System.err.println("Error: --format is mandatory");
                 HelpFormatter formatter = new HelpFormatter();
                 formatter.printHelp("alignment.jar", opts);
                 System.exit(1);
             }
         }
         //algorithm
         if (commandLine.hasOption("nw")) {
             algorithm = (mode == AlignmentMode.LOCAL ? AlignmentAlgorithm.SMITH_WATERMAN : AlignmentAlgorithm.NEEDLEMAN_WUNSCH);
         } else {
             algorithm = AlignmentAlgorithm.GOTOH;
         }
         //benchmark
         boolean benchmark = commandLine.hasOption("benchmark");
         //verbose
         boolean verbose = commandLine.hasOption("verbose");
         boolean calculateCheckscores = commandLine.hasOption("check");
         //
         //Inter-argument cheks
         //
         boolean haveAffineGapCost = Math.abs(gapOpen - gapExtend) > 0.00000001;
         if(algorithm != AlignmentAlgorithm.GOTOH) {
             haveAffineGapCost = false;
         }
         //If a DP matrix dir is set, we need to copy the SVG graphics there
         if (dpMatrixDir != null) {
             for (String filename : Lists.newArrayList("T.svg", "L.svg", "LT.svg", "B-T.svg", "B-L.svg", "B-LT.svg")) {
                 InputStream istream = AlignmentMain.class.getResourceAsStream("/graphics/" + filename);
                 //Read it...
                 List<String> lines = IOUtils.readLines(istream);
                 //And write it right away
                 FileUtils.writeLines(new File(dpMatrixDir, filename), lines);
             }
         }
         //
         // Read & Collect helper objects
         //
         //
         IAlignmentOutputFormatter formatter = AlignmentOutputFormatFactory.factorize(outputFormat);
         ISequenceSource sequenceSource = new SequenceLibrarySequenceSource(seqLibFile.getAbsolutePath());
         Collection<PairfileEntry> pairfileEntries = PairfileParser.parsePairfile(pairsFile.getAbsolutePath());
         IDistanceMatrix matrix = QUASARDistanceMatrixFactory.factorize(substitutionMatrixFile.getAbsolutePath());
         IGapCost gapCost = (haveAffineGapCost ? new AffineGapCost(gapOpen, gapExtend) : new ConstantGapCost(gapExtend));
         DPMatrixExporter matrixExporter = new DPMatrixExporter(dpMatrixDir, outputFormat);
         FixedPoint fpaProcessor = new FixedPoint(mode, algorithm, matrix, gapCost);
         //Create the processor
         AlignmentProcessor proc = AlignmentProcessorFactory.factorize(mode, algorithm, matrix, gapCost);
         //Handle possible benchmarks if applicable -- benchmark to recursive NW
         if (benchmark) {
             if (verbose) {
                 System.err.println("Benchmarking...");
             }
             proc = new AlignmentProcessorBenchmarkController(proc, new RecursiveNWAlignmentProcessor(mode, algorithm, matrix, gapCost));
             ((AlignmentProcessorBenchmarkController) proc).setVerbose(verbose);
         }
         for (PairfileEntry entry : pairfileEntries) {
             //Get the sequences
             Sequence seq1 = sequenceSource.getSequence(entry.first);
             Sequence seq2 = sequenceSource.getSequence(entry.second);
             //Print status if in verbose mode
             if (verbose) {
                 System.err.println("Aligning " + seq1.getId() + " and " + seq2.getId() + "...");
             }
             //Calculate the alignment
             AlignmentResult result = null;
             try {
                 result = proc.align(seq1, seq2);
             } catch (Exception ex) {
                 System.err.println("Error occured while trying to align " + entry.first + " and " + entry.second);
                 System.err.println("Error: " + ex.getLocalizedMessage());
                 ex.printStackTrace();
             }
             //Either --check and print only incorrect alignment or print all
             if (calculateCheckscores) {
                 for (SequencePairAlignment alignment : result.getAlignments()) {
                     double checkScore = (haveAffineGapCost ? 
                             CheckScoreCalculator.calculateCheckScoreAffine(mode, alignment, matrix, gapCost)
                             : CheckScoreCalculator.calculateCheckScoreNonAffine(mode, alignment, matrix, gapCost));
                     if (Math.abs(checkScore - result.getScore()) > 0.000000001) {
                         //Check failed, print failed alignments ONLY
                         AlignmentResult tmpRes = new AlignmentResult(checkScore, Collections.singletonList(alignment));
                         tmpRes.setQuerySequenceId(result.getQuerySequenceId());
                         tmpRes.setTargetSequenceId(result.getTargetSequenceId());
                         formatter.formatAndPrint(tmpRes);
                         System.err.println("Score " + result.getScore() + " vs " + checkScore);
                     }
                 }
             } else {
                 //Print all alignments (usually one)
                 formatter.formatAndPrint(result);
             }
             //Calculate the FPA if applicable
             if (fpaDir != null) {
                 fpaProcessor.makePlot(seq1, seq2, commandLine.hasOption("min-as-threshold"), fpaDir.getAbsolutePath());
             }
             //Write the dynamic programming matrix if applicable 
             if (dpMatrixDir != null) {
                 proc.writeMatrices(matrixExporter);
             }
         }
         //Print the benchmark results if there are any benchmarks
         if (benchmark) {
             AlignmentProcessorBenchmarkController benchmarkController = ((AlignmentProcessorBenchmarkController) proc);
             long align1Time = benchmarkController.getAp1AlignTime();
             long align2Time = benchmarkController.getAp1AlignTime();
             System.err.println("Main algorithm " + benchmarkController.getAp1ClassName() + " vs Recursive Needleman Wunsch:");
             System.err.println("\t" + benchmarkController.getAp1ClassName() + " took " + align1Time + " ms");
             System.err.println("\t Recursive Needleman Wunsch took " + align2Time + " ms");
         }
     }
 
     public static void main(String[] args) throws IOException {
         AlignmentMain alignmentMain = new AlignmentMain(args);
     }
 }
