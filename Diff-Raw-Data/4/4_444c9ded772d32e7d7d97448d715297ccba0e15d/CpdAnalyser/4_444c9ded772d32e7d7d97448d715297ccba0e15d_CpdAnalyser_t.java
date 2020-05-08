 /*
  * Sonar, open source software quality management tool.
  * Copyright (C) 2008-2011 SonarSource
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
 package org.sonar.plugins.cpd;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sonar.api.batch.SensorContext;
 import org.sonar.api.measures.CoreMetrics;
 import org.sonar.api.measures.Measure;
 import org.sonar.api.resources.JavaFile;
 import org.sonar.api.resources.Project;
 import org.sonar.api.resources.Resource;
 import org.sonar.duplications.index.Clone;
 import org.sonar.duplications.index.ClonePart;
 
 import java.io.File;
 import java.util.*;
 
 public class CpdAnalyser {
 
   private static final Logger LOG = LoggerFactory.getLogger(CpdAnalyser.class);
 
   private SensorContext context;
   private Project project;
 
   public CpdAnalyser(Project project, SensorContext context) {
     this.context = context;
     this.project = project;
   }
 
   public void analyse(List<Clone> clones) {
     Map<Resource, DuplicationsData> duplicationsData = new HashMap<Resource, DuplicationsData>();
 
     for (Clone clone : clones) {
       for (ClonePart firstPart : clone.getCloneParts()) {
         String firstAbsolutePath = firstPart.getResourceId();
         int firstLine = firstPart.getLineStart();
         int firstEnd = firstPart.getLineEnd();
 
         Resource firstFile = getResource(new File(firstAbsolutePath));
         if (firstFile == null) {
           LOG.warn("CPD - File not found : {}", firstAbsolutePath);
           continue;
         }
 
         DuplicationsData firstFileData = getDuplicationsData(duplicationsData, firstFile);
         firstFileData.incrementDuplicatedBlock();
 
         for (ClonePart clonePart : clone.getCloneParts()) {
           String secondAbsolutePath = clonePart.getResourceId();
 
           int secondLine = clonePart.getLineStart();
           if (secondAbsolutePath.equals(firstAbsolutePath) && firstLine == secondLine) {
             continue;
           }
           Resource secondFile = getResource(new File(secondAbsolutePath));
           if (secondFile == null) {
             LOG.warn("CPD - File not found : {}", secondAbsolutePath);
             continue;
           }
           firstFileData.cumulate(secondFile, secondLine, firstLine, firstEnd - firstLine + 1);
         }
       }
     }
 
     for (DuplicationsData data : duplicationsData.values()) {
       data.saveUsing(context);
     }
   }
 
   private Resource getResource(File file) {
     return JavaFile.fromIOFile(file, project.getFileSystem().getSourceDirs(), false);
   }
 
   private DuplicationsData getDuplicationsData(Map<Resource, DuplicationsData> fileContainer, Resource file) {
     DuplicationsData data = fileContainer.get(file);
     if (data == null) {
       data = new DuplicationsData(file, context);
       fileContainer.put(file, data);
     }
     return data;
   }
 
   private static final class DuplicationsData {
 
     protected Set<Integer> duplicatedLines = new HashSet<Integer>();
     protected double duplicatedBlocks = 0;
     protected Resource resource;
     private SensorContext context;
     private List<StringBuilder> duplicationXMLEntries = new ArrayList<StringBuilder>();
 
     private DuplicationsData(Resource resource, SensorContext context) {
       this.context = context;
       this.resource = resource;
     }
 
     protected void cumulate(Resource targetResource, int targetDuplicationStartLine, int duplicationStartLine, int duplicatedLines) {
       StringBuilder xml = new StringBuilder();
       xml.append("<duplication lines=\"").append(duplicatedLines).append("\" start=\"").append(duplicationStartLine)
           .append("\" target-start=\"").append(targetDuplicationStartLine).append("\" target-resource=\"")
          .append(context.saveResource(targetResource)).append("\"/>");
 
       duplicationXMLEntries.add(xml);
 
       for (int duplicatedLine = duplicationStartLine; duplicatedLine < duplicationStartLine + duplicatedLines; duplicatedLine++) {
         this.duplicatedLines.add(duplicatedLine);
       }
     }
 
     protected void incrementDuplicatedBlock() {
       duplicatedBlocks++;
     }
 
     protected void saveUsing(SensorContext context) {
       context.saveMeasure(resource, CoreMetrics.DUPLICATED_FILES, 1d);
       context.saveMeasure(resource, CoreMetrics.DUPLICATED_LINES, (double) duplicatedLines.size());
       context.saveMeasure(resource, CoreMetrics.DUPLICATED_BLOCKS, duplicatedBlocks);
       context.saveMeasure(resource, new Measure(CoreMetrics.DUPLICATIONS_DATA, getDuplicationXMLData()));
     }
 
     private String getDuplicationXMLData() {
       StringBuilder duplicationXML = new StringBuilder("<duplications>");
       for (StringBuilder xmlEntry : duplicationXMLEntries) {
         duplicationXML.append(xmlEntry);
       }
       duplicationXML.append("</duplications>");
       return duplicationXML.toString();
     }
   }
 }
