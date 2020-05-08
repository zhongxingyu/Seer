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
 package net.oneandone.lavender.publisher.svn;
 
 import net.oneandone.lavender.publisher.Source;
 import net.oneandone.lavender.publisher.config.Filter;
 import net.oneandone.sushi.fs.Node;
 import net.oneandone.sushi.fs.World;
 import net.oneandone.sushi.fs.file.FileNode;
 import net.oneandone.sushi.util.Strings;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 public class SvnSourceConfig {
     private static final Logger LOG = LoggerFactory.getLogger(SvnSourceConfig.class);
 
     private static final String SVN_PREFIX = "svn.";
 
     public final String folder;
     public final Filter filter;
     public String svnurl;
     public String storage = Source.DEFAULT_STORAGE;
     public boolean lavendelize = true;
     public String pathPrefix = "";
 
    public SvnSourceConfig(String name, Filter filter) {
        this.folder = name;
         this.filter = filter;
     }
 
     public static Collection<SvnSourceConfig> parse(Properties properties) {
         String key;
         String value;
         String name;
         SvnSourceConfig config;
         Map<String, SvnSourceConfig> result;
 
         result = new HashMap<>();
         for (Map.Entry<Object, Object> entry : properties.entrySet()) {
             key = (String) entry.getKey();
             if (key.startsWith(SVN_PREFIX)) {
                 key = key.substring(SVN_PREFIX.length());
                 value = (String) entry.getValue();
                 int idx = key.indexOf('.');
                 if (idx == -1) {
                     name = key;
                     key = null;
                 } else {
                     name = key.substring(0, idx);
                     key = key.substring(idx + 1);
                 }
                 config = result.get(name);
                 if (config == null) {
                     config = new SvnSourceConfig(name, Filter.forProperties(properties, SVN_PREFIX + name, null));
                     result.put(name, config);
                 }
                 if (key == null) {
                     config.svnurl = Strings.removeLeftOpt((String) entry.getValue(), "scm:svn:");
                 } else {
                     if (key.equals("pathPrefix")) {
                         config.pathPrefix = value;
                     } else if (key.equals("storage")) {
                         config.storage = value;
                     } else if (key.equals("lavendelize")) {
                         if ("true".equals(value)) {
                             config.lavendelize = true;
                         } else if ("false".equals(value)) {
                             config.lavendelize = false;
                         } else {
                             throw new IllegalArgumentException("illegal value: " + value);
                         }
                     }
                 }
             }
         }
         return result.values();
     }
 
     public SvnSource create(World world, String svnUsername, String svnPassword) throws IOException {
         FileNode lavender;
         String svnpath;
         FileNode dest;
         List<Node> resources;
 
         if (svnurl == null) {
             throw new IllegalArgumentException("missing svn url");
         }
         if (folder.startsWith("/") || folder.endsWith("/")) {
             throw new IllegalArgumentException();
         }
         lavender = (FileNode) world.getHome().join(".cache/lavender");
         lavender.mkdirsOpt();
         try {
             svnpath = simplify(new URI(svnurl).getPath());
             svnpath = svnpath.replace('/', '.');
             svnpath = Strings.removeLeftOpt(svnpath, ".svn.");
         } catch (URISyntaxException e) {
             throw new IllegalArgumentException(svnurl, e);
         }
         dest = lavender.join(svnpath);
         try {
             LOG.info("using svn cache at " + dest);
             if (dest.exists()) {
                 LOG.info("svn switch " + svnurl);
                 LOG.info(dest.exec("svn", "switch", "--non-interactive", "--no-auth-cache",
                         "--username", svnUsername, "--password", svnPassword, svnurl));
             } else {
                 LOG.info("svn checkout " + svnurl);
                 LOG.info(lavender.exec("svn", "checkout", "--non-interactive", "--no-auth-cache",
                         "--username", svnUsername, "--password", svnPassword, svnurl, dest.getName()));
             }
         } catch (IOException e) {
             throw new IOException("error creating/updating svn checkout at " + dest + ": " + e.getMessage(), e);
         }
         dest.checkDirectory();
         resources = new ArrayList<>();
         for (Node file : dest.find("**/*")) {
             if (file.isFile()) {
                 resources.add(file);
             }
         }
         return new SvnSource(filter, storage, lavendelize, pathPrefix, resources, folder, dest);
     }
 
     private static final String TAGS = "/tags/";
 
     public static String simplify(String path) {
         int idx;
         int end;
 
         path = path.replace("/trunk/", "/");
         idx = path.indexOf(TAGS);
         if (idx != -1) {
             end = path.indexOf('/', idx + TAGS.length());
             path = path.substring(0, idx) + path.substring(end);
         }
         return path;
     }
 }
