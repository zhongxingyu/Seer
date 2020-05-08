 /*
  * Sonar, open source software quality management tool.
  * Copyright (C) 2009 SonarSource SA
  * mailto:contact AT sonarsource DOT com
  *
  * Sonar is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * Sonar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with Sonar; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 package org.sonar.plugins.artifactsize;
 
 import org.codehaus.plexus.util.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sonar.api.batch.DependedUpon;
 import org.sonar.api.batch.Sensor;
 import org.sonar.api.batch.SensorContext;
 import org.sonar.api.measures.Measure;
 import org.sonar.api.measures.Metric;
 import org.sonar.api.resources.Project;
 import org.sonar.api.resources.ProjectFileSystem;
 import org.apache.commons.configuration.Configuration;
 import org.apache.maven.project.MavenProject;
 
 import java.io.File;
 
 public class ArtifactSizeSensor implements Sensor {
 
   @DependedUpon
   public Metric generatesArtifactSize() {
     return ArtifactSizeMetrics.ARTIFACT_SIZE;
   }
 
   /**
    * {@inheritDoc}
    */
   public boolean shouldExecuteOnProject(Project project) {
     return true;
   }
 
   public void analyse(Project project, SensorContext context) {
     File file = searchArtifactFile(project.getPom(), project.getFileSystem(), project.getConfiguration());
 
     final Logger logger = LoggerFactory.getLogger(ArtifactSizeSensor.class);
     if (!file.exists()) {
       logger.info("The file {} does not exist", file);
 
     } else {
       logger.info("Checking the size of the file {}", file);
       double size = getSize(file);
       final Measure measure = new Measure(ArtifactSizeMetrics.ARTIFACT_SIZE, size);
       measure.setDescription(file.getName());
       context.saveMeasure(measure);
     }
   }
 
   protected File searchArtifactFile(MavenProject pom, ProjectFileSystem fileSystem, Configuration configuration) {
     File file;
     String artifactPath = configuration.getString(ArtifactSizePlugin.ARTIFACT_PATH);
 
     if (StringUtils.isNotEmpty(artifactPath)) {
       file = buildPathFromConfig(fileSystem, artifactPath);
 
     }
     else {
       String filename = pom.getBuild().getFinalName() + "." + pom.getPackaging();
       file = buildPathFromPom(fileSystem, filename);
     }
     return file;
   }
 
   private File buildPathFromConfig(ProjectFileSystem fileSystem, String artifactPath) {
     return new File(fileSystem.getBasedir(), artifactPath);
 }
 
   private File buildPathFromPom(ProjectFileSystem fileSystem, String artifactPath) {
     return new File(fileSystem.getBuildDir(), artifactPath);
 }
 
   protected double getSize(File file) {
     return ((double) file.length()) / 1024;
   }
 }
