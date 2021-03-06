 package eu.lindenbaum.maven;
 
 import static eu.lindenbaum.maven.util.ErlConstants.ERLC;
 import static eu.lindenbaum.maven.util.ErlUtils.exec;
 import static eu.lindenbaum.maven.util.FileUtils.getDependencies;
 import static eu.lindenbaum.maven.util.FileUtils.getFilesRecursive;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import eu.lindenbaum.maven.util.Observer;
 
 import org.apache.maven.plugin.Mojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugin.logging.Log;
 
 /**
  * Abstract base class for {@link Mojo}s compiling erlang code.
  * 
  * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
  * @author Olle Törnström <olle.toernstroem@lindenbaum.eu>
  */
 abstract class AbstractCompilerMojo extends AbstractErlangMojo {
   /**
    * Compile files ending with a specific suffix.
    * 
    * @param inputDir path where sources reside
    * @param outputDir path to output compiled files
    * @param includeDir optional additional include directory, maybe {@code null}
    * @param inputSuffix suffix of source files
    * @param outputSuffix suffix of comiled artifacts
    * @param options optional compiler options to be passed to the compiler,
    *          maybe {@code null}
    * @return number of compiled files
    * @throws MojoFailureException
    * @throws MojoExecutionException
    */
   protected final int compile(File inputDir,
                               File outputDir,
                               File includeDir,
                               String inputSuffix,
                               String outputSuffix,
                               List<String> options) throws MojoExecutionException, MojoFailureException {
     Log log = getLog();
     List<File> sources = getFilesRecursive(inputDir, inputSuffix);
     int numSources = sources.size();
     if (numSources > 0) {
       log.info("Compiling " + numSources + " " + inputSuffix + "-file" + (numSources > 1 ? "s" : "")
                + " into " + outputSuffix + " (" + outputDir.getAbsolutePath() + ")");
       List<String> command = getCommandLine(outputDir, includeDir, options, sources);
       exec(command, log, null, new Observer() {
         @Override
         public String handle(int exitValue, String result) throws MojoExecutionException {
           if (exitValue != 0) {
             throw new MojoExecutionException("Compiler returned with " + exitValue);
           }
           return null;
         }
       });
     }
     return numSources;
   }
 
   /**
    * Constructs the erlang command line to use for compilation.
    * 
    * @param outputDir output directory for artifacts
    * @param include optional additional include path, maybe {@code null}
    * @param options optional compiler options to be passed to the compiler,
    *          maybe {@code null}
    * @param sources list of sources to compile
    * @return the constructed command line array
    */
   private List<String> getCommandLine(File outputDir, File include, List<String> options, List<File> sources) {
     List<String> command = new ArrayList<String>();
     command.add(ERLC);
     command.add("-I");
     command.add(this.srcMainInclude.getPath());
     command.add("-I");
     command.add(this.targetInclude.getPath());
     if (include != null) {
       command.add("-I");
       command.add(include.getPath());
     }
    for (File lib : getDependencies(this.targetLib)) {
       command.add("-pa");
      command.add(lib.getAbsolutePath());
     }
     command.add("-o");
     command.add(outputDir.getPath());
     command.add("+report_errors");
     command.add("+report_warnings");
     if (options != null) {
       command.addAll(options);
     }
     for (File source : sources) {
       command.add(source.getPath());
     }
     return command;
   }
 }
