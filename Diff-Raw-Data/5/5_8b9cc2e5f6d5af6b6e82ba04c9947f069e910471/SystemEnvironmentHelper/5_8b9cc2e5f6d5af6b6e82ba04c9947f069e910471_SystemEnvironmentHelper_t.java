 package kkckkc.jsourcepad.util.io;
 
 import com.google.common.collect.Maps;
 import kkckkc.jsourcepad.model.Application;
 import kkckkc.jsourcepad.model.settings.ScriptExecutionSettings;
 import kkckkc.jsourcepad.model.settings.SettingsManager;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Future;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class SystemEnvironmentHelper {
 
    private static volatile Map<String, String> systemEnvironment;
 
     public static Map<String, String> getSystemEnvironment() {
         if (systemEnvironment != null) return new HashMap<String, String>(systemEnvironment);
 
         loadSystemEnvironment(Application.get().getThreadPool());
         return getSystemEnvironment();
     }
 
     private static synchronized void loadSystemEnvironment(ExecutorService executor) {
        if (systemEnvironment != null) return;

         systemEnvironment = Maps.newHashMap();
 
         SettingsManager settingsManager = Application.get().getSettingsManager();
         ScriptExecutionSettings settings = settingsManager.get(ScriptExecutionSettings.class);
         if (settings.getEnvironmentCommandLine() == null || settings.getEnvironmentCommandLine().length == 0) {
             return;
         }
 
         try {
             final ProcessBuilder pb = new ProcessBuilder(settings.getEnvironmentCommandLine());
             final Process p = pb.start();
 
             StringWriter stdout = new StringWriter();
             StringWriter stderr = new StringWriter();
 
             final Future<?> stdoutFuture = executor.submit(
                     new GobblerRunnable(p.getInputStream(), stdout));
             final Future<?> stderrFuture = executor.submit(
                     new GobblerRunnable(p.getErrorStream(), stderr));
 
             final Future<Integer> processFuture = executor.submit(new Callable<Integer>() {
                 public Integer call() {
                     try {
                         p.waitFor();
 
                         int exitCode = p.exitValue();
 
                         stdoutFuture.get();
                         stderrFuture.get();
 
                         return exitCode;
                     } catch (InterruptedException e) {
                         p.destroy();
                         return -1;
                     } catch (ExecutionException e) {
                         throw new RuntimeException(e);
                     }
                 }
             });
 
             Integer result = processFuture.get();
             if (result != 0) throw new RuntimeException("Cannot get environment: " + stderr);
 
             Pattern pattern = Pattern.compile("^(.*?)='?(.*?)'?$");
             for (String s : stdout.toString().split("\n")) {
                 Matcher matcher = pattern.matcher(s);
                 if (matcher.matches()) {
                     systemEnvironment.put(matcher.group(1), matcher.group(2));
                 }
             }
         } catch (InterruptedException e) {
             throw new RuntimeException(e);
         } catch (ExecutionException e) {
             throw new RuntimeException(e);
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 }
