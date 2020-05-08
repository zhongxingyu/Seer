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
 package net.oneandone.lavender.modules;
 
 import net.oneandone.lavender.config.Docroot;
 import net.oneandone.sushi.fs.Node;
 import net.oneandone.sushi.fs.World;
 import net.oneandone.sushi.fs.file.FileNode;
 import net.oneandone.sushi.fs.filter.Action;
 import net.oneandone.sushi.fs.filter.Filter;
 import net.oneandone.sushi.fs.filter.Predicate;
 import net.oneandone.sushi.xml.Selector;
 import net.oneandone.sushi.xml.XmlException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Element;
 import org.xml.sax.SAXException;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 public abstract class DefaultModule extends Module<Node> {
     private static final Logger LOG = LoggerFactory.getLogger(Module.class);
 
     public static List<Module> fromWebapp(boolean prod, Node webapp, String svnUsername, String svnPassword) throws IOException, SAXException, XmlException {
         Node webappSource;
         List<Module> result;
         WarConfig rootConfig;
         DefaultModule root;
         LavenderProperties lp;
 
         LOG.trace("scanning " + webapp);
         lp = LavenderProperties.loadApp(prod, webapp);
         result = new ArrayList<>();
         rootConfig = WarConfig.fromXml(webapp);
         // add modules before webapp, because they have a prefix
         for (Node jar : webapp.find("WEB-INF/lib/*.jar")) {
             result.addAll(jarModuleOpt(rootConfig, prod, jar, svnUsername, svnPassword));
         }
         webappSource = lp.live(webapp);
         root = warModule(rootConfig, lp.filter, webappSource);
         result.add(root);
         lp.addModules(webapp.getWorld(), svnUsername, svnPassword, result);
         return result;
     }
 
     public static List<Module> jarModuleOpt(WarConfig rootConfig, boolean prod, Node jarOrig,
                                             String svnUsername, String svnPassword) throws IOException, XmlException, SAXException {
         Node configFile;
         final JarConfig config;
         List<Module> result;
         Node jarTmp;
         final Node jarLive;
         Module jarModule;
         Object[] tmp;
         LavenderProperties lp;
         Node exploded;
         final Filter filter;
 
         result = new ArrayList<>();
         if (jarOrig instanceof FileNode) {
             // TODO: expensive
             exploded = ((FileNode) jarOrig).openJar();
             configFile = exploded.join("META-INF/pustefix-module.xml");
             if (!configFile.exists()) {
                 return result;
             }
             try (InputStream src = configFile.createInputStream()) {
                 config = JarConfig.load(jarOrig.getWorld().getXml(), rootConfig, src);
             }
             lp = LavenderProperties.loadModuleOpt(prod, exploded);
             if (prod || lp == null) {
                 jarTmp = jarOrig;
             } else {
                 jarTmp = lp.live(jarOrig);
             }
             if (jarTmp.isFile()) {
                 jarLive = ((FileNode) jarTmp).openJar();
             } else {
                 jarLive = jarTmp;
             }
             filter = lp == null ? LavenderProperties.defaultFilter() : lp.filter;
             jarModule = new DefaultModule(Docroot.WEB, config.getModuleName(), true, config.getResourcePathPrefix(), "", filter) {
                 @Override
                 protected Map<String, Node> scan(Filter filter) throws IOException {
                     return files(filter, config, jarLive);
                 }
             };
         } else {
             if (!prod) {
                 throw new UnsupportedOperationException("live mechanism not supported for jar streams");
             }
             tmp = DefaultModule.fromJarStream(prod, Docroot.WEB, rootConfig, jarOrig);
             if (tmp == null) {
                 // no pustefix module config
                 return result;
             }
             jarModule = (Module) tmp[0];
             lp = (LavenderProperties) tmp[1];
         }
         result.add(jarModule);
         if (lp != null) {
             lp.addModules(jarOrig.getWorld(), svnUsername, svnPassword, result);
         }
         return result;
     }
 
     private static Map<String, Node> files(final Filter filter, final JarConfig config, final Node exploded) throws IOException {
         Filter f;
         final Map<String, Node> result;
 
         result = new HashMap<>();
         f = exploded.getWorld().filter().predicate(Predicate.FILE).includeAll();
         f.invoke(exploded, new Action() {
             public void enter(Node node, boolean isLink) {
             }
 
             public void enterFailed(Node node, boolean isLink, IOException e) throws IOException {
                 throw e;
             }
 
             public void leave(Node node, boolean isLink) {
             }
 
             public void select(Node node, boolean isLink) {
                 String path;
                 String resourcePath;
 
                 path = node.getRelative(exploded);
                 if (filter.matches(path)) {
                     resourcePath = config.getPath(path);
                     if (resourcePath != null) {
                         result.put(resourcePath, node);
                     }
                 }
             }
         });
         return result;
     }
 
     //--
 
     public static DefaultModule warModule(final WarConfig config, final Filter filter, final Node webapp) throws IOException {
         Element root;
         Selector selector;
         String name;
 
         try {
             root = webapp.join("WEB-INF/project.xml").readXml().getDocumentElement();
             selector = webapp.getWorld().getXml().getSelector();
             name = selector.string(root, "project/name");
             return new DefaultModule(Docroot.WEB, name, true, "", "", filter) {
                 @Override
                 protected Map<String, Node> scan(Filter filter) throws IOException {
                     return scanExploded(config, filter, webapp);
                 }
             };
         } catch (SAXException | XmlException e) {
             throw new IOException("cannot load project descriptor: " + e);
         }
     }
 
     private static Map<String, Node> scanExploded(final WarConfig global, final Filter filter, final Node exploded) throws IOException {
         Filter f;
         final Map<String, Node> result;
 
         result = new HashMap<>();
         f = exploded.getWorld().filter().predicate(Predicate.FILE).includeAll();
         f.invoke(exploded, new Action() {
             public void enter(Node node, boolean isLink) {
             }
 
             public void enterFailed(Node node, boolean isLink, IOException e) throws IOException {
                 throw e;
             }
 
             public void leave(Node node, boolean isLink) {
             }
 
             public void select(Node node, boolean isLink) {
                 String path;
 
                 path = node.getRelative(exploded);
                 if (filter.matches(path) && global.isPublicResource(path)) {
                     result.put(path, node);
                 }
             }
         });
         return result;
     }
 
     //--
 
     /** To properly make jars available as a module, I have to load them into memory when the jar is itself packaged into a war. */
     public static Object[] fromJarStream(boolean prod, String type, WarConfig parent, Node jar) throws IOException {
         JarConfig config;
         Node[] tmp;
         Filter filter;
         World world;
         ZipEntry entry;
         String path;
         ZipInputStream src;
         Node root;
         Node child;
         Node propertyNode;
         final Map<String, Node> files;
         String resourcePath;
         LavenderProperties lp;
 
         tmp = LavenderProperties.loadStreamNodes(jar, "META-INF/pustefix-module.xml",
                 LavenderProperties.MODULE_PROPERTIES, "META-INF/pominfo.properties");
         if (tmp[0] == null) {
             return null;
         }
         try (InputStream configSrc = tmp[0].createInputStream()) {
             config = JarConfig.load(jar.getWorld().getXml(), parent, configSrc);
         } catch (SAXException | XmlException e) {
             throw new IOException(jar + ": cannot load module descriptor:" + e.getMessage(), e);
         }
         propertyNode = tmp[1];
         if (propertyNode == null) {
             filter = LavenderProperties.defaultFilter();
             lp = null;
         } else {
             lp = LavenderProperties.loadNode(prod, propertyNode, tmp[2]);
             filter = lp.filter;
         }
         world = jar.getWorld();
         root = world.getMemoryFilesystem().root().node(UUID.randomUUID().toString(), null).mkdir();
         src = new ZipInputStream(jar.createInputStream());
         files = new HashMap<>();
         while ((entry = src.getNextEntry()) != null) {
             path = entry.getName();
             if (!entry.isDirectory()) {
                 if ((resourcePath = config.getPath(path)) != null && filter.matches(path)) {
                     child = root.join(path);
                     child.getParent().mkdirsOpt();
                     world.getBuffer().copy(src, child);
                     files.put(resourcePath, child);
                 }
             }
         }
         return new Object[] { new DefaultModule(type, config.getModuleName(), true, config.getResourcePathPrefix(), "", filter) {
             public Map<String, Node> scan(Filter filter) {
                 // no need to re-scan files from memory
                 return files;
             }
         }, lp };
     }
 
     //--
 
     public DefaultModule(String type, String name, boolean lavendelize, String resourcePathPrefix, String targetPathPrefix, Filter filter) throws IOException {
         super(type, name, lavendelize, resourcePathPrefix, targetPathPrefix, filter);
     }
 
     protected Resource createResource(String resourcePath, Node file) throws IOException {
         return DefaultResource.forNode(file, resourcePath);
     }
 
     @Override
     public void saveCaches() {
         // nothing to do
     }
 }
