 /**
  * Copyright (C) 2013 Future Invent Informationsmanagement GmbH. All rights
  * reserved. <http://www.fuin.org/>
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 3 of the License, or (at your option) any
  * later version.
  *
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.fuin.srcgen4j.commons;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElementWrapper;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Configuration that maps generator output to projects.
  */
 @XmlAccessorType(XmlAccessType.FIELD)
 @XmlRootElement(name = "srcgen4j-config")
 @XmlType(propOrder = { "generators", "parsers", "projects", "classpath", "variables" })
 public class SrcGen4JConfig {
 
     private static final Logger LOG = LoggerFactory.getLogger(SrcGen4JConfig.class);
 
     @XmlElementWrapper(name = "variables")
     @XmlElement(name = "variable")
     private List<Variable> variables;
 
     @XmlElement(name = "classpath")
     private Classpath classpath;
 
     @XmlElementWrapper(name = "projects")
     @XmlElement(name = "project")
     private List<Project> projects;
 
     @XmlElementWrapper(name = "parsers")
     @XmlElement(name = "parser")
     private List<ParserConfig> parsers;
 
     @XmlElement(name = "generators")
     private Generators generators;
 
     private transient Map<String, String> varMap;
 
     private transient boolean initialized = false;
 
     /**
      * Returns a list of variables.
      * 
      * @return Variables or NULL.
      */
     public final List<Variable> getVariables() {
         return variables;
     }
 
     /**
      * Returns a map of variables.
      * 
      * @return Map of variables or NULL.
      */
     public final Map<String, String> getVarMap() {
         return varMap;
     }
 
     /**
      * Sets a list of variables.
      * 
      * @param variables
      *            Variables or NULL.
      */
     public final void setVariables(final List<Variable> variables) {
         this.variables = variables;
     }
 
     /**
      * Returns the class path.
      * 
      * @return Class path.
      */
     public final Classpath getClasspath() {
         return classpath;
     }
 
     /**
      * Sets the class path to a new value.
      * 
      * @param classpath
      *            Value to set or NULL.
      */
     public final void setClasspath(final Classpath classpath) {
         this.classpath = classpath;
     }
 
     /**
      * Returns a list of projects.
      * 
      * @return Projects or NULL.
      */
     public final List<Project> getProjects() {
         return projects;
     }
 
     /**
      * Sets a list of projects.
      * 
      * @param projects
      *            Projects or NULL.
      */
     public final void setProjects(final List<Project> projects) {
         this.projects = projects;
     }
 
     /**
      * Returns the list of parsers.
      * 
      * @return Parsers or NULL.
      */
     public final List<ParserConfig> getParsers() {
         return parsers;
     }
 
     /**
      * Sets the list of parsers.
      * 
      * @param parsers
      *            Parsers or NULL.
      */
     public final void setParsers(final List<ParserConfig> parsers) {
         this.parsers = parsers;
     }
 
     /**
      * Adds a parser to the configuration. If the list of parsers does not exist
      * it will be created.
      * 
      * @param parser
      *            Parser to add - Never NULL.
      */
     public final void addParser(final ParserConfig parser) {
         if (parsers == null) {
             parsers = new ArrayList<ParserConfig>();
         }
         parsers.add(parser);
     }
 
     /**
      * Returns a set of generators.
      * 
      * @return Generators or NULL.
      */
     public final Generators getGenerators() {
         return generators;
     }
 
     /**
      * Sets a the of generators.
      * 
      * @param generators
      *            Generators or NULL.
      */
     public final void setGenerators(final Generators generators) {
         this.generators = generators;
     }
 
     /**
      * Returns the information if the object has been initialized.
      * 
      * @return If the method {@link #init()} was called TRUE, else FALSE.
      */
     public final boolean isInitialized() {
         return initialized;
     }
 
     private void initVarMap(final File rootDir) {
         if (variables == null) {
             varMap = new HashMap<String, String>();
             varMap.put("rootDir", rootDir.toString());
         } else {
             final Variable rootDirVar = new Variable("rootDir", rootDir.toString());
             final int idx = variables.indexOf(rootDirVar);
             if (idx > -1) {
                 final Variable old = variables.set(idx, rootDirVar);
                 LOG.warn("Replaced existing root directory variable '" + old.getValue()
                         + "' with: " + rootDir);
             } else {
                 variables.add(rootDirVar);
             }
             varMap = new VariableResolver(variables).getResolved();
         }
     }
 
     /**
      * Initializes this object and it's childs.<br>
      * <br>
      * <b>CAUTION:</b> Elements contained in this configuration may be changed.
      * If you serialize the object after calling this method the new state will
      * be saved.
      * 
      * @param rootDir
      *            Root directory that is available as variable 'rootDir' -
      *            Cannot be NULL.
      * 
      * @return This instance.
      */
     public final SrcGen4JConfig init(final File rootDir) {
         initVarMap(rootDir);
         if (variables != null) {
             for (final Variable variable : variables) {
                 variable.init(varMap);
             }
         }
         if (projects != null) {
             for (final Project project : projects) {
                 project.init(this, varMap);
             }
         }
         if (generators != null) {
             generators.init(this, varMap);
         }
         if (parsers != null) {
             for (final ParserConfig parser : parsers) {
                 parser.init(this, varMap);
             }
         }
         initialized = true;
         return this;
     }
 
     /**
      * Returns a target directory for a given combination of generator name and
      * artifact name.
      * 
      * @param generatorName
      *            Name of the generator.
      * @param artifactName
      *            Name of the artifact.
      * 
      * @return Target file based on the configuration.
      * 
      * @throws GeneratorNotFoundException
      *             The given generator name was not found in the configuration.
      * @throws ArtifactNotFoundException
      *             The given artifact was not found in the generator.
      * @throws ProjectNameNotDefinedException
      *             No project name is bound to the given combination.
      * @throws FolderNameNotDefinedException
      *             No folder name is bound to the given combination.
      * @throws ProjectNotFoundException
      *             The project name based on the selection is unknown.
      * @throws FolderNotFoundException
      *             The folder in the project based on the selection is unknown.
      */
     public final Folder findTargetFolder(final String generatorName, final String artifactName)
             throws ProjectNameNotDefinedException, ArtifactNotFoundException,
             FolderNameNotDefinedException, GeneratorNotFoundException, ProjectNotFoundException,
             FolderNotFoundException {
 
         return findTargetFolder(generatorName, artifactName, null);
 
     }
 
     /**
      * Returns a target directory for a given combination of generator name,
      * artifact name and target sub path.
      * 
      * @param generatorName
      *            Name of the generator.
      * @param artifactName
      *            Name of the artifact.
      * @param targetPath
      *            Current path and file.
      * 
      * @return Target file based on the configuration.
      * 
      * @throws GeneratorNotFoundException
      *             The given generator name was not found in the configuration.
      * @throws ArtifactNotFoundException
      *             The given artifact was not found in the generator.
      * @throws ProjectNameNotDefinedException
      *             No project name is bound to the given combination.
      * @throws FolderNameNotDefinedException
      *             No folder name is bound to the given combination.
      * @throws ProjectNotFoundException
      *             The project name based on the selection is unknown.
      * @throws FolderNotFoundException
      *             The folder in the project based on the selection is unknown.
      */
     public final Folder findTargetFolder(final String generatorName, final String artifactName,
             final String targetPath) throws ProjectNameNotDefinedException,
             ArtifactNotFoundException, FolderNameNotDefinedException, GeneratorNotFoundException,
             ProjectNotFoundException, FolderNotFoundException {
 
         final GeneratorConfig generator = generators.findByName(generatorName);
 
         int idx = generator.getArtifacts().indexOf(new Artifact(artifactName));
         if (idx < 0) {
             throw new ArtifactNotFoundException(generatorName, artifactName);
         }
         final Artifact artifact = generator.getArtifacts().get(idx);
 
         final String projectName;
         final String folderName;
         final String targetPattern;
         final Target target = artifact.findTargetFor(targetPath);
         if (target == null) {
             projectName = artifact.getDefProject();
             folderName = artifact.getDefFolder();
             targetPattern = null;
         } else {
             projectName = target.getDefProject();
             folderName = target.getDefFolder();
             targetPattern = target.getPattern();
         }
 
         if (projectName == null) {
             throw new ProjectNameNotDefinedException(generatorName, artifactName, targetPattern);
         }
         if (folderName == null) {
             throw new FolderNameNotDefinedException(generatorName, artifactName, targetPattern);
         }
 
         idx = projects.indexOf(new Project(projectName));
         if (idx < 0) {
             throw new ProjectNotFoundException(generatorName, artifactName, targetPattern,
                     projectName);
         }
         final Project project = projects.get(idx);
 
         idx = project.getFolders().indexOf(new Folder(folderName));
         if (idx < 0) {
             throw new FolderNotFoundException(generatorName, artifactName, targetPattern,
                     projectName, folderName);
         }
         return project.getFolders().get(idx);
 
     }
 
     /**
      * Find all generators that are connected to a given parser.
      * 
      * @param parserName
      *            Name of the parser to return the generators for.
      * 
      * @return List of generators.
      */
     public final List<GeneratorConfig> findGeneratorsForParser(final String parserName) {
         final List<GeneratorConfig> list = new ArrayList<GeneratorConfig>();
         final List<GeneratorConfig> gcList = generators.getList();
         for (final GeneratorConfig gc : gcList) {
             if (gc.getParser().equals(parserName)) {
                 list.add(gc);
             }
         }
         return list;
     }
 
     /**
      * Creates a new configuration with a single project and a Maven directory
      * structure.
      * 
      * @param projectName
      *            Name of the one and only project.
      * @param rootDir
      *            Root directory that is available as variable 'srcgen4jRootDir'
      *            - Cannot be NULL.
      * 
      * @return New initialized configuration instance.
      */
     public static SrcGen4JConfig createMavenStyleSingleProject(final String projectName,
             final File rootDir) {
         final SrcGen4JConfig config = new SrcGen4JConfig();
         final List<Project> projects = new ArrayList<Project>();
         final Project project = new Project(projectName, ".");
         project.setMaven(true);
         projects.add(project);
         config.setProjects(projects);
         config.init(rootDir);
         return config;
     }
 
 }
