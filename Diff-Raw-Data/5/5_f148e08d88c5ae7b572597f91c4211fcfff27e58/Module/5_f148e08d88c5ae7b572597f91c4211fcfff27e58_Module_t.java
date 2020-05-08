 /*
  * Copyright (c) 2012 Jim Svensson <jimpa@tla.se>
  *
  * Permission to use, copy, modify, and distribute this software for any
  * purpose with or without fee is hereby granted, provided that the above
  * copyright notice and this permission notice appear in all copies.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
  * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
  * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
  * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
  * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
  * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 
 package se.tla.mavenversionbumper;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.Namespace;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.XMLOutputter;
 
 /**
  * Represents a Maven project file, pom.xml.
  *
  * It contains methods to easily view and manipulate dependency information.
  */
 public class Module {
     private static final String SNAPSHOTPATTERN = "-SNAPSHOT";
     final private Document document;
     final private File pomFile;
     final private Element root;
     final private Namespace nameSpace;
     final private String moduleName;
     final private String originalVersion;
     private String label;
     private String commitMessage;
     private boolean labelOnlyPomXml = false;
 
     /**
      * Constructor.
      *
      * @param baseDirName Filename of the base directory of the Maven module.
      * @param moduleName The symbolic name of the Maven module.
      * @throws JDOMException Problem reading the pom.xml file.
      * @throws IOException Problem reading the pom.xml file.
      */
     public Module(String baseDirName, String moduleName) throws JDOMException, IOException {
         this.moduleName = moduleName;
         File dir = openDir(null, baseDirName);
         if (moduleName.length() > 0) {
             dir = openDir(dir, moduleName);
         }
         File baseDir = dir;
         pomFile = new File(baseDir, "pom.xml");
         SAXBuilder builder = new SAXBuilder();
         document = builder.build(pomFile);
         root = document.getRootElement();
         nameSpace = root.getNamespace();
         originalVersion = version();
     }
 
     /**
      * Constructor used only by the sub class ReadonlyModule to satisfy the compiler. Should never be used.
      */
     protected Module() {
         document = null;
         pomFile = null;
         root = null;
         nameSpace = null;
         moduleName = null;
         originalVersion = null;
     }
 
     private File openDir(File base, String name) {
         File dir;
         if (base != null) {
              dir = new File(base, name);
         } else {
             dir = new File(name);
         }
         if (!dir.isDirectory()) {
             throw new IllegalArgumentException("No such directory: " + dir.getName());
         }
         return dir;
     }
 
     /**
      * @return GAV-coordinates. GroupId, ArtifactId, Version.
      */
     public String gav() {
         return groupId() + ":" + artifactId() + ":" + version();
     }
 
     public String groupId() {
         return myOrParent("groupId");
     }
 
     /**
      * @param groupId New GroupId.
      */
     public void groupId(@SuppressWarnings("SameParameterValue") String groupId) {
         root.getChild("groupId", nameSpace).setText(groupId);
     }
 
     public String artifactId() {
         return root.getChildText("artifactId", nameSpace);
     }
 
     /**
      * @param artifactId New ArtifactId.
      */
     public void artifactId(@SuppressWarnings("SameParameterValue") String artifactId) {
         root.getChild("artifactId", nameSpace).setText(artifactId);
     }
 
     public String version() {
         return myOrParent("version");
     }
 
     /**
      * @param version New Version.
      */
     public void version(String version) {
         if (commitMessage == null) {
             commitMessage = "Bump " + originalVersion + " -> " + version;
         }
         root.getChild("version", nameSpace).setText(version);
     }
 
     public String parentVersion() {
         Element parent = root.getChild("parent", nameSpace);
         if (parent == null) {
             return null;
         }
         return parent.getChildText("version", nameSpace);
     }
 
     /**
      * @param parentVersion New parentVersion.
      */
     public void parentVersion(String parentVersion) {
         Element parent = root.getChild("parent", nameSpace);
         if (parent == null) {
             throw new IllegalArgumentException("No parent defined in module");
         }
         Element version = parent.getChild("version", nameSpace);
         if (version == null) {
             throw new IllegalStateException("No version defined for parent.");
         }
         version.setText(parentVersion);
     }
 
     /**
      * Update the parent version to that of this Module.
      *
      * @param parent Use this modules version.
      */
     public void parentVersion(Module parent) {
         parentVersion(parent.version());
     }
 
     private String myOrParent(String itemName) {
         String item = root.getChildText(itemName, nameSpace);
         if (item != null) {
             return item;
         }
         Element parent = root.getChild("parent", nameSpace);
         if (parent == null) {
             return null;
         }
         return parent.getChildText(itemName, nameSpace);
     }
 
     /**
      * Find this module in either the modules dependency management list or in the dependency list.
      *
      * If the found dependency refers to a property for the version, an updateProperty is tried on that property name.
      *
      * @param moduleToUpdate Module to find and update version for.
      * @throws IllegalArgumentException If the moduleToUpdate can't be found in either list.
      */
     public void updateDependency(Module moduleToUpdate) {
 
         // Look in dependencyManagement
         Element dep = findDependencyElement(moduleToUpdate, "dependencyManagement", "dependencies");
 
         if (dep == null) {
             // Look i dependencies
             dep = findDependencyElement(moduleToUpdate, "dependencies");
         }
 
         if (dep == null) {
            throw new IllegalArgumentException("No such dependency found in " + gav() + ": " + moduleToUpdate.gav());
         }
 
         Element version = dep.getChild("version", nameSpace);
         if (version == null) {
             throw new IllegalArgumentException("In " + gav() + ", no version defined for " + moduleToUpdate.gav() +
             ". Probably defined elsewhere in a dependencyManagement.");
         }
 
         String versionText = version.getText();
         if (versionText.startsWith("${") && versionText.endsWith("}")) {
             String propertyName = versionText.substring(2).substring(0, versionText.length() - 3);
 
             updateProperty(propertyName, moduleToUpdate.version());
             return;
         }
 
         version.setText(moduleToUpdate.version());
     }
 
     /**
      * Find this plugin in either the modules plugin management list or in the plugin list.
      *
      * @param pluginToUpdate Plugin to find and update version for.
      * @throws IllegalArgumentException If the moduleToUpdate can't be found in either list.
      */
     public void updatePluginDependency(Module pluginToUpdate) {
 
         // Look in pluginManagement
         Element dep = findDependencyElement(pluginToUpdate, "build", "pluginManagement", "plugins");
 
         if (dep == null) {
             // Look i plugins
             dep = findDependencyElement(pluginToUpdate, "build", "plugins");
         }
 
         if (dep == null) {
            throw new IllegalArgumentException("No such plugin dependency found in " + gav() + ": " + pluginToUpdate.gav());
         }
 
         Element version = dep.getChild("version", nameSpace);
         if (version == null) {
             throw new IllegalArgumentException("In " + gav() + ", no version defined for " + pluginToUpdate.gav() +
                     ". Probably defined elsewhere in a pluginManagement.");
         }
 
         if (version.getText().startsWith("${") && version.getText().endsWith("}")) {
             throw new IllegalArgumentException("In " + gav() + ", the plugin dependency to " + pluginToUpdate.gav() +
                     "'s version is controlled by a property. Use updateProperty instead.");
         }
 
         version.setText(pluginToUpdate.version());
     }
 
     /**
      * Find the named property and update its value.
      *
      * @param propertyName Name.
      * @param value Value.
      * @throws IllegalArgumentException if named property can't be found.
      */
     public void updateProperty(String propertyName, String value) {
         Element properties = root.getChild("properties", nameSpace);
         if (properties == null) {
             throw new IllegalArgumentException("No properties defined in module " + gav());
         }
 
         Element property = properties.getChild(propertyName, nameSpace);
         if (property == null) {
             throw new IllegalArgumentException("No property " + propertyName + " defined in module " + gav());
         }
 
         property.setText(value);
     }
 
     /**
      * Save this module back to its original pom.xml file.
      *
      * If a VersionControl was provided while creating this Module, the pom.xml is first
      * committed and then, optionally, labeled.
      *
      * @throws IOException in case of IO-related problems.
      */
     public void save() throws IOException {
         XMLOutputter o = new XMLOutputter();
         // TODO Make sure that the line endings are preserved.
         o.getFormat().setLineSeparator("\n"); // Nicht funktioniren
         // TODO Make sure that the character encoding of the pom.xml is preserved.
         FileUtils.write(pomFile, o.outputString(document), "utf-8");
     }
 
     /**
      * Apply this label to the Module when it is saved. Requires that a VersionControl was provided to work.
      *
      * @param label Label.
      */
     public void label(String label) {
         this.label = label;
     }
 
     public String label() {
         return label;
     }
 
     /**
      * Use this commit message if a commit to VersionControl is performed. If no custom message is provided,
      * a default message is used.
      *
      * @param commitMessage Custom message to use during a commit.
      */
     public void commitMessage(String commitMessage) {
         this.commitMessage = commitMessage;
     }
 
     /**
      * @return The message to use during a commit.
      */
     public String commitMessage() {
         return commitMessage;
     }
 
     private Element findDependencyElement(Module moduleToFind, String ... path) {
         for (Element dep : getDependencyElements(path)) {
             String groupId = dep.getChildText("groupId", nameSpace);
             String artifactId = dep.getChildText("artifactId", nameSpace);
 
             if (groupId.equals(moduleToFind.groupId()) && artifactId.equals(moduleToFind.artifactId())) {
                 return dep;
             }
         }
 
         return null;
     }
 
     private List<Element> getDependencyElements(String ... path) {
         Element cur = root;
 
         for (String pathPart : path) {
             cur = cur.getChild(pathPart, nameSpace);
             if (cur == null) {
                 return new LinkedList<Element>();
             }
         }
 
         return (List<Element>) cur.getChildren();
     }
 
     public List<String> findSnapshots() {
         List<String> result = new LinkedList<String>();
 
         // Own version
         if (version().endsWith(SNAPSHOTPATTERN)) {
             result.add("Module version");
         }
 
         // Parent Version
         String parentVersion = parentVersion();
         if (parentVersion != null && parentVersion.endsWith(SNAPSHOTPATTERN)) {
             result.add("Parent version " + parentVersion);
         }
 
         // Properties
         Element properties = root.getChild("properties", nameSpace);
         if (properties != null) {
             for (Element child : (List<Element>) properties.getChildren()) {
                 String text = child.getText();
                 if (text != null && text.endsWith(SNAPSHOTPATTERN)) {
                     result.add("Property " + child.getName() + ":" + text);
                 }
             }
         }
 
         // Dependencies
         for (Element dep : getDependencyElements("dependencies")) {
             String version = extractText(dep, "version");
             if (version != null && version.endsWith(SNAPSHOTPATTERN)) {
                 result.add("Dependency " + extractText(dep, "groupId") + ":" + extractText(dep, "artifactId") + ":" + version);
             }
         }
 
         // Dependency management
         for (Element dep : getDependencyElements("dependencyManagement", "dependencies")) {
             String version = extractText(dep, "version");
             if (version != null && version.endsWith(SNAPSHOTPATTERN)) {
                 result.add("Dependency management " + extractText(dep, "groupId") + ":" + extractText(dep, "artifactId") + ":" + version);
             }
         }
 
         // Plugins
         for (Element dep : getDependencyElements("build", "plugins")) {
             String version = extractText(dep, "version");
             if (version != null && version.endsWith(SNAPSHOTPATTERN)) {
                 result.add("Plugin " + extractText(dep, "groupId") + ":" + extractText(dep, "artifactId") + ":" + version);
             }
         }
 
         // Plugin management
         for (Element dep : getDependencyElements("build", "pluginManagement", "plugins")) {
             String version = extractText(dep, "version");
             if (version != null && version.endsWith(SNAPSHOTPATTERN)) {
                 result.add("Plugin management " + extractText(dep, "groupId") + ":" + extractText(dep, "artifactId") + ":" + version);
             }
         }
 
         return result;
     }
 
     private String extractText(Element dep, String elementName) {
         return dep.getChildText(elementName, nameSpace);
     }
 
     /**
      * Controls how much will be labeled if labeling is required.
      *
      * Please observe that this functionality isn't implemented in all VersionControl implementations since
      * they simply don't support labeling of sub trees.
      *
      * @param labelOnlyPomXml If false (default) label whole Module recursively, if true only label the pom.xml.
      */
     public void labelOnlyPomXml(boolean labelOnlyPomXml) {
         this.labelOnlyPomXml = labelOnlyPomXml;
     }
 
     /**
      * @return If true, only label the pom.xml, if false, label whole file tree recursively, including any sub module..
      */
     public boolean labelOnlyPomXml() {
         return labelOnlyPomXml;
     }
 
     /**
      * @return The modules pom.xml file.
      */
     public File pomFile() {
         return pomFile;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String toString() {
         return moduleName;
     }
 }
