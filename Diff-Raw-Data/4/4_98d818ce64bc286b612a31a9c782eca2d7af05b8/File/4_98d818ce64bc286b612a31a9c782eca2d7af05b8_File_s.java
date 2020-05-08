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
 
 import net.oneandone.lavender.config.Cluster;
 import net.oneandone.lavender.config.Docroot;
 import net.oneandone.lavender.config.Net;
 import net.oneandone.lavender.config.Pool;
 import net.oneandone.lavender.config.Settings;
 import net.oneandone.lavender.config.Target;
 import net.oneandone.lavender.index.Distributor;
 import net.oneandone.lavender.index.Index;
 import net.oneandone.lavender.modules.DefaultModule;
 import net.oneandone.lavender.modules.Module;
 import net.oneandone.lavender.modules.SvnProperties;
 import net.oneandone.sushi.cli.ArgumentException;
 import net.oneandone.sushi.cli.Console;
 import net.oneandone.sushi.cli.Option;
 import net.oneandone.sushi.cli.Value;
 import net.oneandone.sushi.fs.Node;
 import net.oneandone.sushi.fs.file.FileNode;
 import net.oneandone.sushi.fs.filter.Filter;
 import net.oneandone.sushi.fs.filter.Predicate;
 import net.oneandone.sushi.util.Strings;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 public class File extends Base {
     @Value(name = "file", position = 1)
     private FileNode file;
 
     @Value(name = "type", position = 2)
     private String type;
 
     @Value(name = "cluster", position = 3)
     private String clusterName;
 
     @Option("prefix")
     private String prefix;
 
     @Option("name")
     private String explicitName;
 
     public File(Console console, Settings settings, Net net) {
         super(console, settings, net);
     }
 
     @Override
     public void invoke() throws IOException {
         final Node exploded;
         Cluster cluster;
         Docroot docroot;
         Target target;
         Filter filter;
         Module module;
         Distributor distributor;
         long changed;
         Index index;
         String name;
 
         cluster = net.get(clusterName);
         file.checkExists();
         if (file.isFile()) {
             exploded = file.openZip();
         } else {
             exploded = file;
         }
         docroot = cluster.docroot(type);
         target = new Target(cluster, docroot, docroot.aliases().get(0));
         filter = new Filter();
         filter.includeAll();
         filter.predicate(Predicate.FILE);
        name = explicitName == null ? getName() : explicitName;
         module = new DefaultModule(type, name, false, "", prefix, filter) {
             @Override
             protected Map<String, Node> scan(Filter filter) throws Exception {
                 Map<String, Node> result;
 
                 result = new HashMap<>();
                 for (Node node : exploded.find(filter)) {
                     result.put(node.getPath(), node);
                 }
                 return result;
             }
         };
 
         try (Pool pool = pool()) {
             distributor = target.open(pool, name);
             changed = module.publish(distributor);
             index = distributor.close();
             module.saveCaches();
         }
         console.info.println("done: " + changed + "/" + index.size() + " files changed");
     }
 
     private String getName() {
         String name;
         int idx;
 
         name = file.getName();
         idx = name.lastIndexOf(".");
         if (idx != -1) {
             name = name.substring(0, idx);
         }
        name = name + ".idx";
         return name;
     }
 }
