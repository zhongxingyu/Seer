 package ideah.compiler;
 
 import com.intellij.openapi.diagnostic.Logger;
 import com.intellij.openapi.module.Module;
 import com.intellij.openapi.vfs.VirtualFile;
 import ideah.util.CompilerLocation;
 import ideah.util.GHCUtil;
 import ideah.util.ProcessLauncher;
 import org.jetbrains.annotations.NotNull;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.*;
 
 public final class LaunchGHC {
 
     private static final Logger LOG = Logger.getInstance("ideah.compiler.LaunchGHC");
 
     static final String EOLN = "\n";
 
     public static List<GHCMessage> compileAndGetGhcMessages(VirtualFile output, String fileName, @NotNull Module module, boolean tests) {
         try {
             CompilerLocation compiler = CompilerLocation.get(module);
             if (compiler == null)
                return Collections.emptyList();
             List<String> args = new ArrayList<String>();
             args.add(compiler.exe);
             args.addAll(Arrays.asList(
                 "-m", "Compile",
                 "-g", compiler.libPath,
                 "-s", GHCUtil.rootsAsString(module, tests)
             ));
 
             GHCUtil.addGhcOptions(module, args, "-W");
 
             if (output != null) {
                 args.addAll(Arrays.asList(
                     "-o", output.getPath()
                 ));
             }
             args.add(fileName);
 
             ProcessLauncher launcher = new ProcessLauncher(false, null, args);
             String stdOut = launcher.getStdOut();
             return parseMessages(stdOut);
         } catch (Exception ex) {
             LOG.error(ex);
             return Collections.singletonList(new GHCMessage(ex.toString(), fileName));
         }
     }
 
     private static List<GHCMessage> parseMessages(String output) throws IOException {
         List<StringBuilder> buffers = new ArrayList<StringBuilder>();
         List<GHCMessage> ghcMessages = new ArrayList<GHCMessage>();
         BufferedReader ghcErrorReader = new BufferedReader(new StringReader(output));
         StringBuilder tmpBuffer = new StringBuilder();
         String line = ghcErrorReader.readLine();
         while (line != null) {
             if (line.startsWith(ProcessLauncher.NEW_MSG_INDICATOR)) {
                 tmpBuffer = new StringBuilder();
                 buffers.add(tmpBuffer);
             } else {
                 tmpBuffer.append(line).append(EOLN);
             }
             line = ghcErrorReader.readLine();
         }
         for (StringBuilder buffer : buffers) {
             ghcMessages.add(new GHCMessage(buffer.toString()));
         }
         return ghcMessages;
     }
 }
