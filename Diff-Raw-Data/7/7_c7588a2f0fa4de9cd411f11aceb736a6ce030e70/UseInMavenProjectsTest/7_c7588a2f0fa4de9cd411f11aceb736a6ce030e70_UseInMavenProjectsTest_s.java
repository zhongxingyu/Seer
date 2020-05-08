 /**
  * Licensed to the Austrian Association for Software Tool Integration (AASTI)
  * under one or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information regarding copyright
  * ownership. The AASTI licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 import org.apache.commons.io.IOUtils;
 import org.junit.Test;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringBufferInputStream;
 import java.io.StringReader;
 import java.net.URL;
 import java.sql.Array;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Properties;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.FutureTask;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 public class UseInMavenProjectsTest {
 
     @Test
     public void testMultiProject() throws Exception {
         assertThat(new MavenProcessHandler("projects/good/multiproject/pom.xml").execute(), is(0L));
     }
 
     @Test
     public void testSimple() throws Exception {
         MavenProcessHandler mavenProcessHandler = new MavenProcessHandler("projects/good/sample-project/pom.xml");
         mavenProcessHandler.start();
         mavenProcessHandler.waitForLineInOutput("SUCCESS");
         assertThat(mavenProcessHandler.waitForBuildToFinish(), is(0L));
     }
 
     @Test
     public void testConnectsToDatabaseDuringTest() throws Exception {
         MavenProcessHandler handler = new MavenProcessHandler("projects/good/remote-project/pom.xml");
         handler.start();
         handler.waitForLineInOutput("ACCEPTING CONNECTION");
         Class.forName("org.h2.Driver");
         String port = readPortFromProperties();
         Connection conn = DriverManager.getConnection("jdbc:h2:tcp://localhost:" + port + "/mem:remote;DB_CLOSE_DELAY=-1");
         Statement statement = conn.createStatement();
         statement.execute("SELECT * FROM TESTMODEL");
         ResultSet resultSet = statement.getResultSet();
         resultSet.last();
         assertThat(resultSet.getRow(), is(1));
     }
 
     private String readPortFromProperties() throws IOException {
         InputStream stream = ClassLoader.getSystemResourceAsStream("projects/good/remote-project/src/test/resources/ports.properties");
         Properties properties = new Properties();
         properties.load(stream);
         return (String) properties.get("h2.tcp.port");
     }
 
     private class MavenProcessHandler {
         private String name;
         private Process process;
         private FutureTask<Void> outputTask;
         private final List<String> lines = new ArrayList<String>();
 
         private MavenProcessHandler(String name) {
             this.name = name;
         }
 
         private void start() throws IOException {
             String m2Home = System.getenv("M2_HOME");
             String mvnExecutable = "mvn";
             if (m2Home != null) {
                 mvnExecutable = new File(m2Home, "bin/" + mvnExecutable).getAbsolutePath();
             }
             URL good = ClassLoader.getSystemResource(name);
            ProcessBuilder processBuilder = new ProcessBuilder(mvnExecutable, "-f", good.getFile(), "install");
             processBuilder.redirectErrorStream(true);
             process = processBuilder.start();
             outputTask = new FutureTask<Void>(new Callable<Void>() {
                 @Override
                 public Void call() throws Exception {
                     InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
                     BufferedReader br = new BufferedReader(inputStreamReader);
                     String line;
                     while((line = br.readLine()) != null) {
                         synchronized (lines) {
                             lines.add(line);
                             lines.notifyAll();
                         }
                         System.out.println(line);
                     }
                     return null;
                 }
             });
             new Thread(outputTask).start();
         }
 
         private void waitForLineInOutput(String text) throws InterruptedException {
             synchronized (lines) {
                 int i = 0;
                 while (true) {
                     for(int j = i; j < lines.size(); j++) {
                         String line = lines.get(j);
                         if (line.contains(text)) {
                             return;
                         }
                     }
                     lines.wait();
                 }
 
             }
         }
 
         private long waitForBuildToFinish() throws ExecutionException, InterruptedException {
             long result = process.waitFor();
             outputTask.get();
             return result;
         }
 
         private long execute() throws IOException, ExecutionException, InterruptedException {
             start();
             return waitForBuildToFinish();
         }
     }
 
 }
