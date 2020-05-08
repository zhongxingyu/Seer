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
 package net.oneandone.lavender.publisher.cli;
 
 import net.oneandone.lavender.index.Index;
 import net.oneandone.lavender.publisher.Distributor;
 import net.oneandone.lavender.publisher.config.Cluster;
 import net.oneandone.lavender.publisher.config.Filter;
 import net.oneandone.lavender.publisher.config.Net;
 import net.oneandone.lavender.publisher.config.Settings;
 import net.oneandone.lavender.publisher.config.Vhost;
 import net.oneandone.lavender.publisher.svn.SvnSource;
 import net.oneandone.lavender.publisher.svn.SvnSourceConfig;
 import net.oneandone.sushi.cli.ArgumentException;
 import net.oneandone.sushi.cli.Console;
 import net.oneandone.sushi.cli.Value;
 
 import java.io.IOException;
 
 public class Svn extends Base {
     @Value(name = "cluster", position = 1)
     private String clusterName;
 
     @Value(name = "directory", position = 2)
     private String directory;
 
     public Svn(Console console, Settings settings, Net net) {
         super(console, settings, net);
     }
 
     @Override
     public void invoke() throws IOException {
         String url;
 
         url = net.svn.get(directory);
         if (url == null) {
             throw new ArgumentException("unknown svn directory: " + directory);
         }
         invoke(url);
     }
 
     private void invoke(String svnurl) throws IOException {
         Cluster cluster;
         Vhost vhost;
         Filter filter;
         SvnSourceConfig ec;
         SvnSource e;
         Distributor storage;
         long changed;
         Index index;
 
         cluster = net.cluster(clusterName);
         vhost = cluster.vhost("svn");
         filter = new Filter();
         filter.setIncludes("*");
         filter.setExcludes();
         ec = new SvnSourceConfig("svn", filter);
         ec.pathPrefix = "";
        ec.svnurl = svnurl;
         ec.lavendelize = false;
         e = ec.create(console.world, settings.svnUsername, settings.svnPassword);
         storage = Distributor.open(console.world, vhost.docroot, cluster.hosts, directory + ".idx");
         changed = e.run(storage);
         index = storage.close();
         console.info.println("done: " + changed + "/" + index.size() + " files changed");
     }
 }
