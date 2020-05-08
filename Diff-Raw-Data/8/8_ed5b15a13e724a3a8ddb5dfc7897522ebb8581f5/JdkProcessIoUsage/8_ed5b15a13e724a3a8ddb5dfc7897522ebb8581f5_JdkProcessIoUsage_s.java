 package org.whiskeysierra.process;
 
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TemporaryFolder;
 
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 
 import static org.hamcrest.Matchers.hasItem;
 import static org.junit.Assume.assumeThat;
 
 public final class JdkProcessIoUsage {
 
     @Rule
     public final TemporaryFolder temp = new TemporaryFolder();
 
     @Test
     public void test() throws IOException {
         assumeThat(Os.getCurrent().getFamilies(), hasItem(Family.UNIX));
 
         final ProcessService service = Primal.createService();
 
         final Path input = Paths.get("src/test/resources/lorem-ipsum.txt");
        final Path output = Paths.get("output");
 
         final ManagedProcess managed = service.prepare("cat");
 
         managed.redirect(Stream.INPUT, Redirection.from(input));
         managed.redirect(Stream.ERROR, Redirection.NULL);
 
         final RunningProcess process = managed.call();
 
        Files.copy(process.getStandardOutput(), output);
 
         process.await();
     }
 
 }
