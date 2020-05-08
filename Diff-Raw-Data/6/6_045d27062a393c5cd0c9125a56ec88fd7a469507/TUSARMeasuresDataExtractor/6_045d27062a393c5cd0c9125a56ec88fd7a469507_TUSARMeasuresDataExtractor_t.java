 /*******************************************************************************
  * Copyright (c) 2010 Thales Corporate Services SAS                             *
  *                                                                              *
  * Permission is hereby granted, free of charge, to any person obtaining a copy *
  * of this software and associated documentation files (the "Software"), to deal*
  * in the Software without restriction, including without limitation the rights *
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell    *
  * copies of the Software, and to permit persons to whom the Software is        *
  * furnished to do so, subject to the following conditions:                     *
  *                                                                              *
  * The above copyright notice and this permission notice shall be included in   *
  * all copies or substantial portions of the Software.                          *
  *                                                                              *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
  * THE SOFTWARE.                                                                *
  *******************************************************************************/
 
 package com.thalesgroup.sonar.plugins.tusar.sensors;
 
 import com.thalesgroup.sonar.lib.model.v2.DuplicationsComplexType;
 import com.thalesgroup.sonar.lib.model.v2.SizeComplexType;
 import com.thalesgroup.sonar.lib.model.v2.Sonar;
 import com.thalesgroup.sonar.plugins.tusar.TUSARLanguage;
 import com.thalesgroup.sonar.plugins.tusar.TUSARResource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sonar.api.batch.SensorContext;
 import org.sonar.api.measures.CoreMetrics;
 import org.sonar.api.measures.Measure;
 import org.sonar.api.measures.Metric;
 import org.sonar.api.resources.Directory;
 import org.sonar.api.resources.Project;
 import org.sonar.api.resources.Resource;
 import org.sonar.api.utils.ParsingUtils;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Contains methods to extract TUSAR tests data.
  */
 public class TUSARMeasuresDataExtractor {
 
     private static Logger logger = LoggerFactory
             .getLogger(TUSARMeasuresDataExtractor.class);
 
     private static HashMap<String, Metric> metricsMapping = constructMetricsMapping();
 
     private static void processSize(SizeComplexType size,
                                     SensorContext context, Project project) throws ParseException {
         for (com.thalesgroup.sonar.lib.model.v2.SizeComplexType.Resource element : size
                 .getResource()) {
             Resource<?> resource = constructResource(element.getType(),
                     element.getValue(), project);
             if (resource == null) {
                 logger.debug(
                         "Path is not valid, {} resource {} does not exists.",
                         element.getType(), element.getValue());
             } else {
                 for (com.thalesgroup.sonar.lib.model.v2.SizeComplexType.Resource.Measure measure : element
                         .getMeasure()) {
                     double measureValue = ParsingUtils.parseNumber(measure
                             .getValue());
                     String measureKey = measure.getKey();
                     Metric sonarMetric = metricsMapping.get(measureKey);
                     if (sonarMetric != null) {
                         context.saveMeasure(resource, new Measure(sonarMetric,
                                 measureValue));
                     }
                 }
             }
         }
     }
 
     static Map<Resource, ResourceData> duplicationsData = new HashMap<Resource, ResourceData>();
 
     static class ResourceData {
 
         private double duplicatedLines;
         private double duplicatedBlocks;
         private List<StringBuilder> duplicationXMLEntries = new ArrayList<StringBuilder>();
 
         private SensorContext context;
         private Resource resource;
 
         public ResourceData(SensorContext context, Resource resource) {
             this.context = context;
             this.resource = resource;
         }
 
         public void cumulate(Resource targetResource,
                              Double targetDuplicationStartLine, Double duplicationStartLine,
                              Double duplicatedLines) {
             Resource resolvedResource = context.getResource(targetResource);
             if (resolvedResource != null) {
                 StringBuilder xml = new StringBuilder();
                 xml.append("<duplication lines=\"")
                         .append(duplicatedLines.intValue())
                         .append("\" start=\"")
                         .append(duplicationStartLine.intValue())
                         .append("\" target-start=\"")
                         .append(targetDuplicationStartLine.intValue())
                         .append("\" target-resource=\"")
                         .append(resolvedResource.getEffectiveKey())
                         .append("\"/>");
                 duplicationXMLEntries.add(xml);
                 this.duplicatedLines += duplicatedLines;
                 this.duplicatedBlocks++;
             }
         }
 
         public void saveUsing(SensorContext context) {
             context.saveMeasure(resource, CoreMetrics.DUPLICATED_FILES, 1d);
             context.saveMeasure(resource, CoreMetrics.DUPLICATED_LINES,
                     duplicatedLines);
             context.saveMeasure(resource, CoreMetrics.DUPLICATED_BLOCKS,
                     duplicatedBlocks);
             StringBuilder duplicationXML = new StringBuilder("<duplications>");
             for (StringBuilder xmlEntry : duplicationXMLEntries) {
                 duplicationXML.append(xmlEntry);
             }
             duplicationXML.append("</duplications>");
             context.saveMeasure(resource, new Measure(
                     CoreMetrics.DUPLICATIONS_DATA, duplicationXML.toString()));
         }
     }
 
     private static void processDuplications(
             DuplicationsComplexType duplications, SensorContext context,
             Project project) throws ParseException {
         for (DuplicationsComplexType.Set duplicationSet : duplications.getSet()) {
             try {
 
                 // Record for each resource which is a file
                 //for (int i = 0; i < duplicationSet.getResource().size(); i++) {
 
                 // Resource1
                 DuplicationsComplexType.Set.Resource duplicationResource1 = duplicationSet
                         .getResource().get(0);
                 Resource<?> resource1 = constructResource("FILE",
                         duplicationResource1.getPath(), project);
                 // Resource1
                 DuplicationsComplexType.Set.Resource duplicationResource2 = duplicationSet
                         .getResource().get(1);
                 Resource<?> resource2 = constructResource("FILE",
                         duplicationResource2.getPath(), project);
 
                 processClassMeasure(context, resource1, resource2,
                         Double.parseDouble(duplicationResource2.getLine()),
                         Double.parseDouble(duplicationResource1.getLine()),
                         Double.parseDouble(duplicationSet.getLines()));
 
                 processClassMeasure(context, resource2, resource1,
                         Double.parseDouble(duplicationResource1.getLine()),
                         Double.parseDouble(duplicationResource2.getLine()),
                         Double.parseDouble(duplicationSet.getLines()));
 
                 /*
                          * if (resource == null) { logger.debug(
                          * "Path is not valid, {} resource {} does not exists."
                          * ,"FILE", duplicationResource.getPath()); } else {
                          *
                          * //context.saveMeasure(resource,
                          * CoreMetrics.DUPLICATED_FILES, 1d);
                          * context.saveMeasure(resource,
                          * CoreMetrics.DUPLICATED_LINES, duplicatedLines);
                          * context.saveMeasure(resource,
                          * CoreMetrics.DUPLICATED_BLOCKS, 0d);
                          * context.saveMeasure(resource, new
                          * Measure(CoreMetrics.DUPLICATIONS_DATA,
                          * duplicationSet.getCodefragment())); }
                          */
                 //}
 
             } catch (NumberFormatException nfe) {
 
             }
 
         }
         for (ResourceData data : duplicationsData.values()) {
             data.saveUsing(context);
         }
     }
 
     private static void processClassMeasure(SensorContext context,
                                             Resource<?> resource, Resource<?> targetResource,
                                             Double targetDuplicationStartLine, Double duplicationStartLine,
                                             Double duplicatedLines) throws ParseException {
 
         ResourceData data = duplicationsData.get(resource);
         if (data == null) {
             data = new ResourceData(context, resource);
             duplicationsData.put(resource, data);
         }
         data.cumulate(targetResource, targetDuplicationStartLine,
                 duplicationStartLine, duplicatedLines);
 
     }
 
     public static void saveToSonarMeasuresData(Sonar model,
                                                SensorContext context, Project project) throws ParseException {
 
         // Size
         SizeComplexType size = model.getMeasures().getSize();
         if (size != null) {
             processSize(size, context, project);
         }
 
         // Duplications
         DuplicationsComplexType duplications = model.getMeasures()
                 .getDuplications();
         if (duplications != null) {
             processDuplications(duplications, context, project);
         }
     }
 
     private static HashMap<String, Metric> constructMetricsMapping() {
         HashMap<String, Metric> map = new HashMap<String, Metric>();
         map.put("LINES", CoreMetrics.LINES);
         map.put("NCLOC", CoreMetrics.NCLOC);
         map.put("CLASSES", CoreMetrics.CLASSES);
         map.put("FILES", CoreMetrics.FILES);
         map.put("DIRECTORIES", CoreMetrics.DIRECTORIES);
         map.put("PACKAGES", CoreMetrics.PACKAGES);
         map.put("FUNCTIONS", CoreMetrics.FUNCTIONS);
         map.put("STATEMENTS", CoreMetrics.STATEMENTS);
         map.put("PUBLIC_API", CoreMetrics.PUBLIC_API);
         map.put("PACKAGES", CoreMetrics.PACKAGES);
         map.put("COMPLEXITY", CoreMetrics.COMPLEXITY);
         map.put("CLASS_COMPLEXITY", CoreMetrics.CLASS_COMPLEXITY);
         map.put("FUNCTION_COMPLEXITY", CoreMetrics.FUNCTION_COMPLEXITY);
         map.put("FILE_COMPLEXITY", CoreMetrics.FILE_COMPLEXITY);
         map.put("CLASS_COMPLEXITY_DISTRIBUTION",
                 CoreMetrics.CLASS_COMPLEXITY_DISTRIBUTION);
         map.put("FUNCTION_COMPLEXITY_DISTRIBUTION",
                 CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION);
         map.put("COMMENT_LINES", CoreMetrics.COMMENT_LINES);
         map.put("COMMENT_LINES_DENSITY", CoreMetrics.COMMENT_LINES_DENSITY);
         map.put("PUBLIC_DOCUMENTED_API_DENSITY",
                 CoreMetrics.PUBLIC_DOCUMENTED_API_DENSITY);
         map.put("PUBLIC_UNDOCUMENTED_API", CoreMetrics.PUBLIC_UNDOCUMENTED_API);
         map.put("COMMENTED_OUT_CODE_LINES",
                 CoreMetrics.COMMENTED_OUT_CODE_LINES);
         return map;
     }
 
     private static Resource<?> constructResource(String resourceType,
                                                  String resourcePath, Project project) {
 
         if ("PROJECT".equals(resourceType)) {
             // TBD : nom du projet dans le fichier xml doit correspondre au nom
             // du projet dans hudson/sonar
            //return new Project(resourcePath);
            return project;
         } else if ("DIRECTORY".equals(resourceType)) {
             return new Directory(resourcePath, new TUSARLanguage());
         } else if ("FILE".equals(resourceType)) {
            // for file, we uses the TUSARResource element, which is as a FILE
             return TUSARResource.fromAbsOrRelativePath(resourcePath, project,
                     false);
         }
 
         /*
            * else if ("PACKAGE".equals(resourceType)){ // TDB : package uniquement
            * utilisable avec java, mettre des protections. return new
            * JavaPackage(resourcePath); } else if ("CLASS".equals(resourceType)){
            * // TDB : class uniquement utilisable avec java, mettre des
            * protections. return JavaFile.fromAbsolutePath(resourcePath,
            * project.getFileSystem().getSourceDirs(), false); } else if
            * ("UNIT_TEST_CLASS".equals(resourceType)){ // TDB : package uniquement
            * utilisable avec java, mettre des protections. return
            * JavaFile.fromAbsolutePath(resourcePath,
            * project.getFileSystem().getSourceDirs(), true); }
            */
         else {
             return null;
         }
     }
 }
