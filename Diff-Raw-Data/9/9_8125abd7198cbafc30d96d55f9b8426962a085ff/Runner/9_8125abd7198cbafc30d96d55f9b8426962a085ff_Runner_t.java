 /**
  * Copyright 1&1 Internet AG, https://github.com/1and1/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.oneandone.jasmin.main;
 
 import net.oneandone.jasmin.model.Engine;
 import net.oneandone.jasmin.model.Module;
 import net.oneandone.jasmin.model.Repository;
 import net.oneandone.jasmin.model.Resolver;
 import net.oneandone.sushi.fs.Node;
 import net.oneandone.sushi.fs.World;
 import net.oneandone.sushi.fs.file.FileNode;
 import org.apache.log4j.Logger;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Random;
 
 public class Runner {
     public static final Random RANDOM = new Random();
     public static final Logger LOG = Logger.getLogger(Runner.class);
 
     public static Runner create(String name, World world) throws IOException {
         FileNode base;
         Application application;
         FileNode localhost;
         Engine engine;
 
         base = world.guessProjectHome(Runner.class);
        application = new Application(null, new Resolver(world), null, null);
         localhost = world.getTemp().createTempDirectory();
         engine = application.createEngineSimple(base.getParent(), localhost);
         return new Runner(name, engine);
     }
 
     //--
 
     private final String name;
     private final Engine engine;
     private final List<String> paths;
 
     public Runner(String name, Engine engine) {
         this.name = name;
         this.engine = engine;
         this.paths = new ArrayList<String>();
     }
 
     public int pathCount() {
         return paths.size();
     }
 
     public Runner add(Node log, String application, int max) throws IOException {
         for (String line : log.readLines()) {
             line = line.trim();
             line = line.substring(line.indexOf('|') + 1);
             if (line.startsWith(application)) {
                 line = line.substring(application.length());
                 line = line.substring(0, line.indexOf(' '));
                 paths.add(line);
                 if (paths.size() == max) {
                     break;
                 }
             }
         }
         return this;
     }
 
     public Runner addAll() {
         Repository repository;
         List<String> variants;
 
         repository = engine.repository;
         variants = repository.getVariants();
         for (Module module : repository.modules()) {
             addTypes(module, "head");
             for (String variant : variants) {
                 addTypes(module, variant);
             }
         }
         return this;
     }
 
     public void addTypes(Module module, String variant) {
         add(module.getName() + "/js/" + variant);
         add(module.getName() + "/js-min/" + variant);
         add(module.getName() + "/css/" + variant);
         add(module.getName() + "/css-min/" + variant);
     }
 
     public Runner add(String... pathList) {
         this.paths.addAll(Arrays.asList(pathList));
         return this;
     }
 
 
     public Engine getEngine() {
         return engine;
     }
 
     //--
 
     public void invoke() throws Exception {
         invoke(1, paths.size(), false, false, true);
     }
 
     public void invoke(int clientCount, int requestCount) throws Exception {
         invoke(clientCount, requestCount, true, true, true);
     }
 
     public void invoke(int clientCount, int requestCount, boolean random, boolean lastModified, boolean stats)
             throws Exception {
         long started;
         Client[] clients;
 
         if (stats) {
             System.out.println("[" + name + "]\t" + clientCount + " client(s) a " + requestCount + " requests (random="
                     + random + ", lastModified=" + lastModified + ")");
         }
         started = System.currentTimeMillis();
         clients = new Client[clientCount];
         for (int i = 0; i < clients.length; i++) {
             clients[i] = new Client(engine, paths, requestCount, random, lastModified);
             clients[i].start();
         }
         for (Client client : clients) {
             client.finish();
         }
         if (stats) {
             stats(started);
         }
     }
 
     //--
 
     private void stats(long started) {
         long used;
 
         System.out.println();
         usedMemory();
         runGC();
         // CAUTION: I need a println before the next "used" call to get accurate numbers!?
         System.out.println("  ms:          " + (System.currentTimeMillis() - started));
         used = usedMemory();
         engine.free();
         runGC();
         System.out.println("  mem:         " + (used - usedMemory()));
         System.out.println();
         System.out.println();
     }
 
     private static void runGC() {
         // It helps to call Runtime.gc()
         // using several method calls:
         for (int r = 0; r < 4; ++r) {
             doRunGC();
         }
     }
 
     private static void doRunGC() {
         long used;
         long prev;
 
         used = usedMemory();
         prev = Long.MAX_VALUE;
         for (int i = 0; (used < prev) && (i < 500); i++) {
             Runtime.getRuntime().runFinalization();
             Runtime.getRuntime().gc();
             try {
                 Thread.sleep(100);
             } catch (InterruptedException e) {
                 throw new RuntimeException(e);
             }
             prev = used;
             used = usedMemory();
         }
     }
 
     private static long usedMemory() {
         return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
     }
 }
