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
 package net.oneandone.lavender.cli;
 
 import com.jcraft.jsch.JSchException;
 import net.oneandone.lavender.config.Cluster;
 import net.oneandone.lavender.config.Connection;
 import net.oneandone.lavender.config.Docroot;
 import net.oneandone.lavender.config.Net;
 import net.oneandone.lavender.config.Pool;
 import net.oneandone.lavender.config.Settings;
 import net.oneandone.lavender.index.Hex;
 import net.oneandone.lavender.index.Index;
 import net.oneandone.lavender.index.Label;
 import net.oneandone.sushi.cli.Console;
 import net.oneandone.sushi.cli.Option;
 import net.oneandone.sushi.cli.Value;
 import net.oneandone.sushi.fs.FileNotFoundException;
 import net.oneandone.sushi.fs.Node;
 import net.oneandone.sushi.fs.file.FileNode;
 import net.oneandone.sushi.fs.ssh.SshNode;
 import net.oneandone.sushi.io.OS;
 import net.oneandone.sushi.util.Separator;
 import net.oneandone.sushi.util.Strings;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class Fsck extends Base {
     @Option("md5")
     private boolean md5check;
 
     @Option("mac")
     private boolean mac;
 
     @Option("gc")
     private boolean gc;
 
     @Value(name = "cluster", position = 1)
     private String clusterName;
 
     public Fsck(Console console, Settings settings, Net net) {
         super(console, settings, net);
     }
 
     @Override
     public void invoke() throws IOException {
         Cluster cluster;
         Node docrootNode;
         boolean problem;
         Map<String, Index> prevIndexes;
         Map<String, Index> indexes;
         Index left;
         Index right;
 
         problem = false;
         cluster = net.get(clusterName);
         try (Pool pool = pool()) {
             for (Docroot docroot : cluster.docroots()) {
                 prevIndexes = null;
                 for (Connection connection : cluster.connect(pool)) {
                     console.info.println(connection.getHost() + " " + docroot.aliases().get(0).getName());
                     docrootNode = docroot.node(connection);
                     if (docrootNode.exists()) {
                         indexes = filesAndReferences(connection, docrootNode, docroot);
                         if (indexes == null) {
                             problem = true;
                         } else {
                             if (prevIndexes != null) {
                                 if (!prevIndexes.keySet().equals(indexes.keySet())) {
                                     console.error.println("index file list differs: " + prevIndexes.keySet() + " vs " + indexes.keySet());
                                     problem = true;
                                 } else {
                                     for (String name : prevIndexes.keySet()) {
                                         left = prevIndexes.get(name);
                                         right = indexes.get(name);
                                         if (!left.equals(right)) {
                                             console.error.println("index files differ: " + name);
                                             problem = true;
                                         }
                                     }
                                 }
                             }
                             prevIndexes = indexes;
                         }
                     }
                 }
             }
         }
         if (problem) {
             throw new IOException("VALIDATE FAILED");
         } else {
             console.info.println("validate ok");
         }
     }
 
     /** @return Indexes on this docroot (file name mapped to Index object). Null if a problem was detected. */
     private Map<String, Index> filesAndReferences(Connection connection, Node docroot, Docroot docrootObj) throws IOException {
         boolean problem;
         Set<String> references;
         List<String> files;
         Index index;
         List<String> tmp;
         Index all;
         Index allLoaded;
         Map<String, Index> result;
        Node repaired;
 
         result = new HashMap<>();
         problem = false;
         references = new HashSet<>();
         console.verbose.println("  docroot"  + docroot.getURI().toString());
         console.info.print("  files: ");
         files = find(docroot, "-type", "f");
         console.info.println(files.size());
         console.info.print("  references: ");
         all = new Index();
         for (Node file : docrootObj.indexList(connection)) {
             index = Index.load(file);
             result.put(file.getName(), index);
             for (Label label : index) {
                 references.add(label.getLavendelizedPath());
                 all.addReference(label.getLavendelizedPath(), label.md5());
             }
         }
         try {
             allLoaded = Index.load(docrootObj.index(connection, Index.ALL_IDX));
         } catch (FileNotFoundException e) {
             console.info.println(Index.ALL_IDX + " is missing");
             allLoaded = new Index();
             problem = true;
         }
         if (!all.equals(allLoaded)) {
            repaired = repairedLocation(docrootObj.index(connection, Index.ALL_IDX));
            repaired.getParent().mkdirsOpt();
             // TODO: change this into a problem when all shops use lavender 2
             // problem = true;
             console.error.println("all-index is broken");
            all.save(repaired);
         }
         console.info.println(references.size());
         tmp = new ArrayList<>(references);
         tmp.removeAll(files);
         console.error.println("  dangling references: " + tmp.size());
         if (!tmp.isEmpty()) {
             problem = true;
             for (String path : tmp) {
                 console.verbose.println("    " + path);
             }
             removeReferences(connection, docrootObj, tmp);
         }
         if (md5check) {
             if (md5check(docroot, all)) {
                 problem = true;
             }
         }
         tmp = new ArrayList<>(files);
         tmp.removeAll(references);
         console.error.println("  unreferenced files: " + tmp.size());
         if (!tmp.isEmpty()) {
             if (gc) {
                 if (problem) {
                     throw new IOException("garbage collection not allowed - fix the above problems first");
                 }
                 gc(docroot, tmp);
             } else {
                 problem = true;
                 for (String path : tmp) {
                     console.verbose.println("    " + path);
                 }
             }
         }
         return problem ? null : result;
     }
 
     private void removeReferences(Connection connection, Docroot docrootObj, List<String> references) throws IOException {
         Index orig;
         Index repaired;
         Node repairedFile;
 
         for (Node file : docrootObj.indexList(connection)) {
             orig = Index.load(file);
             repaired = new Index();
             for (Label label : orig) {
                 if (!references.contains(label.getLavendelizedPath())) {
                     repaired.add(label);
                 }
             }
             if (orig.size() != repaired.size()) {
                 repairedFile = repairedLocation(file);
                 console.info.println("writing repaired index: " + repairedFile);
                 repairedFile.getParent().mkdirsOpt();
                 repaired.save(repairedFile);
             }
         }
 
     }
 
     private Node repairedLocation(Node file) {
         return file.getParent().getParent().getParent().join("repaired-indexes", file.getParent().getName(), file.getName());
     }
 
     private boolean md5check(Node docroot, Index index) throws IOException {
         boolean problem;
         List<String> paths;
         List<String> expecteds;
 
         problem = false;
         console.info.print("  md5 check: ");
         paths = new ArrayList<>();
         expecteds = new ArrayList<>();
         for (Label label : index) {
             paths.add(label.getOriginalPath());
             expecteds.add(Hex.encodeString(label.md5()));
             if (paths.size() > 500) {
                 if (md5check(docroot, paths, expecteds)) {
                     problem = true;
                 }
                 paths.clear();
                 expecteds.clear();
             }
         }
         if (paths.size() > 0) {
             if (md5check(docroot, paths, expecteds)) {
                 problem = true;
             }
         }
         console.info.println(problem ? "failed" : "ok");
         return problem;
     }
 
     private static final Separator SEPARATOR = Separator.RAW_LINE;
 
     private boolean md5check(Node docroot, List<String> paths, List<String> expecteds) throws IOException {
         String md5all;
         List<String> computeds;
         boolean problem;
         int i;
         String expected;
 
         problem = false;
         md5all = exec(docroot, Strings.append(mac ? new String[] { "md5", "-q" } : new String[] { "md5sum" }, Strings.toArray(paths)));
         computeds = SEPARATOR.split(md5all);
         if (expecteds.size() != computeds.size()) {
             throw new IllegalStateException(expecteds + " vs " + computeds);
         }
         i = 0;
         for (String computed : computeds) {
             computed = computed.trim();
             if (!mac) {
                 // because md5sum prints the checksum followed by the path
                 computed = computed.substring(0, computed.indexOf(' '));
             }
             expected = expecteds.get(i);
             if (!expected.equals(computed)) {
                 console.error.println(paths.get(i)+ ": md5 broken: expected " + expected + ", got " + computed);
                 problem = true;
             }
             i++;
         }
         return problem;
     }
 
     public static List<String> find(Node base, String ... args) throws IOException {
         String str;
         List<String> lst;
 
         str = exec(base, Strings.append(new String[] { "find", "." }, args));
         lst = new ArrayList<>();
         for (String path : OS.CURRENT.lineSeparator.split(str)) {
             if (path.startsWith("./")) {
                 path = path.substring(2);
             }
             path = path.trim();
             if (!path.isEmpty()) {
                 lst.add(path);
             }
         }
         return lst;
     }
 
     private static String exec(Node dir, String ... cmd) throws IOException {
         if (dir instanceof SshNode) {
             try {
                 return ((SshNode) dir).getRoot().exec(Strings.append(new String[] { "cd", "/" + dir.getPath(), "&&" }, escape(cmd)));
             } catch (JSchException e) {
                 throw new IOException();
             }
         } else if (dir instanceof FileNode) {
             return ((FileNode) dir).exec(cmd);
         } else {
             throw new UnsupportedOperationException("exec on " + dir.getClass());
         }
     }
 
     // TODO: jsch problem -- it takes the argument as a string ...
     private static String[] escape(String[] args) {
         String[] result;
         String arg;
 
         result = new String[args.length];
         for (int i = 0; i < result.length; i++) {
             arg = args[i];
             if (arg.contains(" ")) {
                 arg = "\"" + arg + "\"";
             }
             result[i] = arg;
         }
         return result;
     }
 
     //--
 
     private void gc(Node base, List<String> files) throws IOException {
         gcFiles(base, files);
         gcDirectories(base);
     }
 
     private void gcFiles(Node base, List<String> files) throws IOException {
         console.info.print("scanning files ...");
         console.info.println(" done: " + files.size());
         for (String file : files) {
             console.verbose.println("rm " + file);
             base.join(file).deleteFile();
         }
         console.info.println(files.size() + " unreferenced files deleted.");
     }
 
     private void gcDirectories(Node base) throws IOException {
         List<String> paths;
 
         console.info.print("scanning empty directories ...");
         paths = Fsck.find(base, "-type", "d", "-empty");
         console.info.println(" done: " + paths.size());
         for (String path : paths) {
             rmdir(base, base.join(path));
         }
         console.info.println(paths.size() + " empty directories deleted.");
     }
 
     private void rmdir(Node base, Node dir) throws IOException {
         while (true) {
             if (dir.equals(base)) {
                 return;
             }
             console.verbose.println("rmdir " + dir.getPath());
             dir.deleteDirectory();
             dir = dir.getParent();
             if (dir.list().size() > 0) {
                 return;
             }
         }
     }
 
 }
