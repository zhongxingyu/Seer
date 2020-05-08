 package ideah.util;
 
 import com.intellij.execution.configurations.CommandLineTokenizer;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
public class LocationUtil {
 
     private final String exe;
     private final String libPath;
     private final String ghcOptions;
 
    LocationUtil(String exe, String libPath, String ghcOptions) {
         this.exe = exe;
         this.libPath = libPath;
         this.ghcOptions = ghcOptions;
     }
 
    public List<String> getCompileOptionsList(String... additionalArgs) {
         List<String> args = new ArrayList<String>();
         args.add(exe);
         args.addAll(Arrays.asList("-g", libPath));
        CommandLineTokenizer tokenizer = new CommandLineTokenizer(ghcOptions, " ");
         while (tokenizer.hasMoreTokens()) {
             args.addAll(Arrays.asList("-c", tokenizer.nextToken()));
         }
         args.addAll(Arrays.asList(additionalArgs));
         return args;
     }
 }
