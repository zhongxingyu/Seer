 /*
  * Copyright 2009 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.google.jstestdriver.idea;
 
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;

 import com.google.inject.AbstractModule;
 import com.google.inject.Module;
 import com.google.inject.name.Names;
 import com.google.jstestdriver.ActionRunner;
 import com.google.jstestdriver.BrowserInfo;
 import com.google.jstestdriver.ConfigurationParser;
 import com.google.jstestdriver.DryRunInfo;
 import com.google.jstestdriver.IDEPluginActionBuilder;
 import com.google.jstestdriver.Plugin;
 import com.google.jstestdriver.PluginLoader;
 import com.google.jstestdriver.Response;
 import com.google.jstestdriver.ResponseStream;
 import com.google.jstestdriver.ResponseStreamFactory;
 import com.google.jstestdriver.TestResult;
 import com.google.jstestdriver.TestResultGenerator;
 
 /**
  * Run JSTD in its own process, and stream messages to a server that lives in the IDEA process,
  * which will update the UI with our results.
  *
  * @author alexeagle@google.com (Alex Eagle)
  */
 public class TestRunner {
   private final String serverURL;
   private final String settingsFile;
   private final File baseDirectory;
   private final ObjectOutputStream out;
   private static final int TIMEOUT_MILLIS = 2 * 1000; // 2 seconds
   public static final int RETRIES = 5;
 
   public TestRunner(String serverURL, String settingsFile, File baseDirectory,
                     ObjectOutputStream out) {
     this.serverURL = serverURL;
     this.settingsFile = settingsFile;
     this.baseDirectory = baseDirectory;
     this.out = out;
   }
 
   public void execute() {
     ResponseStreamFactory responseStreamFactory = createResponseStreamFactory();
     final ActionRunner dryRunRunner =
         makeActionBuilder(responseStreamFactory)
           .dryRunFor(Arrays.asList("all")).build();
     final ActionRunner testRunner =
         makeActionBuilder(responseStreamFactory)
             .addAllTests().build();
     //TODO(alexeagle): support client-side reset action
     final ActionRunner resetRunner =
         makeActionBuilder(responseStreamFactory)
             .resetBrowsers().build();
 
     dryRunRunner.runActions();
     testRunner.runActions();
   }
 
   private ResponseStreamFactory createResponseStreamFactory() {
     return new ResponseStreamFactory() {
       public ResponseStream getRunTestsActionResponseStream(String s) {
         return new ResponseStream() {
           public void stream(Response response) {
             for (TestResult testResult : new TestResultGenerator().getTestResults(response)) {
               try {
                 out.writeObject(TestResultProtocolMessage.fromTestResult(testResult));
               } catch (IOException e) {
                 throw new RuntimeException(e);
               }
             }
           }
 
           public void finish() {
           }
         };
       }
 
       public ResponseStream getDryRunActionResponseStream() {
         return new ResponseStream() {
           public void stream(Response response) {
             BrowserInfo browser = response.getBrowser();
             DryRunInfo dryRunInfo = DryRunInfo.fromJson(response);
             for (String testName : dryRunInfo.getTestNames()) {
               try {
                 out.writeObject(TestResultProtocolMessage.fromDryRun(testName, browser));
               } catch (IOException e) {
                 throw new RuntimeException(e);
               }
             }
           }
 
           public void finish() {
           }
         };
       }
 
       public ResponseStream getEvalActionResponseStream() {
         return null;
       }
 
       public ResponseStream getResetActionResponseStream() {
         return null;
       }
     };
   }
 
   private IDEPluginActionBuilder makeActionBuilder(ResponseStreamFactory responseStreamFactory) {
     try {
       FileReader fileReader = new FileReader(settingsFile);
      ConfigurationParser configurationParser = new ConfigurationParser(baseDirectory, fileReader,
          new DefaultPathRewriter());
       IDEPluginActionBuilder builder = new IDEPluginActionBuilder(
      		configurationParser, serverURL, responseStreamFactory);
       builder.install(new AbstractModule() {
         @Override
         protected void configure() {
           bind(File.class).annotatedWith(Names.named("basePath")).toInstance(baseDirectory);
         }
       });
       for (Module module : new PluginLoader().load(parsePluginsFromConfFile())) {
         builder.install(module);
       }
       return builder;
     } catch (FileNotFoundException e) {
       throw new RuntimeException("Failed to read settings file " + settingsFile, e);
     }
   }
 
   private List<Plugin> parsePluginsFromConfFile() throws FileNotFoundException {
     ConfigurationParser parser =
         new ConfigurationParser(baseDirectory, new FileReader(settingsFile),
             new DefaultPathRewriter());
     parser.parse();
     return parser.getPlugins();
   }
 
   public static void main(String[] args) throws IOException {
     String serverURL = args[0];
     String settingsFile = args[1];
     int port = Integer.parseInt(args[2]);
     Socket socket = new Socket();
     int retries = RETRIES;
     do {
       try {
         socket.connect(new InetSocketAddress(InetAddress.getLocalHost(), port), TIMEOUT_MILLIS);
         break;
       } catch (SocketException e) {
         retries--;
       }
     } while (retries > 0);
 
     new TestRunner(serverURL, settingsFile, new File(""),
         new ObjectOutputStream(socket.getOutputStream())).execute();
   }
 }
